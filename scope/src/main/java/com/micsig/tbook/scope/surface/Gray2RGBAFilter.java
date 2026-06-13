package com.micsig.tbook.scope.surface;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.micsig.tbook.scope.ScopeBase;

import java.nio.FloatBuffer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                Gray2RGBAFilter - 灰度转RGBA滤镜类                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的OpenGL纹理滤镜类，实现灰度图像到RGBA彩色图像的转换。           ║
 * ║   为示波器各通道波形分配不同的颜色，实现多通道彩色显示效果。                   ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 实现灰度到彩色的片段着色器                                               ║
 * ║   2. 管理8个通道的颜色配置                                                   ║
 * ║   3. 支持多通道叠加显示                                                      ║
 * ║   4. 支持缩放和偏移功能                                                      ║
 * ║   5. 支持Z序（通道优先级）管理                                                ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │ BasicTextureFilter │ ← OpenGL纹理滤镜基类        ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                                   ▼                                          ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │ Gray2RGBAFilter │ ← 本类：灰度转RGBA滤镜         ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【通道颜色配置】                                                             ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 通道           │ 颜色                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ CH1 (通道1)    │ 黄色 #FFFF00 (RGB: 255, 255, 0)                    │  ║
 * ║   │ CH2 (通道2)    │ 青色 #00FFFF (RGB: 0, 255, 255)                    │  ║
 * ║   │ CH3 (通道3)    │ 品红 #FF00FF (RGB: 255, 0, 255)                    │  ║
 * ║   │ CH4 (通道4)    │ 绿色 #00FF00 (RGB: 0, 255, 0)                      │  ║
 * ║   │ CH5 (通道5)    │ 橙色 #FF8000 (RGB: 255, 128, 0)                    │  ║
 * ║   │ CH6 (通道6)    │ 天蓝 #0055FF (RGB: 0, 85, 255)                     │  ║
 * ║   │ CH7 (通道7)    │ 粉红 #FF667F (RGB: 255, 102, 127)                  │  ║
 * ║   │ CH8 (通道8)    │ 青绿 #26FFB7 (RGB: 38, 255, 183)                   │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【着色器算法】                                                               ║
 * ║   1. 采样纹理获取灰度值                                                      ║
 * ║   2. 根据zorder确定优先通道                                                  ║
 * ║   3. 根据通道索引获取颜色                                                    ║
 * ║   4. 输出最终颜色（灰度值 × 通道颜色）                                       ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器波形彩色显示                                                      ║
 * ║   2. 多通道叠加显示                                                          ║
 * ║   3. 区分不同通道的信号                                                      ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   使用synchronized关键字保护关键方法和volatile修饰共享变量。                 ║
 * ║   必须在OpenGL渲染线程中调用相关方法。                                       ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-4-3                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class Gray2RGBAFilter extends BasicTextureFilter {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 着色器uniform变量名常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Z序（通道优先级）uniform变量名
     * 用于指定多通道显示时的优先级顺序
     */
    private static final String UNIFORM_ZORDER = "uZorder";

    /**
     * 偏移量uniform变量名
     * 用于实现缩放和平移功能
     */
    private static final String UNIFORM_OFFSET = "uOffset";

    /**
     * 通道索引uniform变量名
     * 用于指定当前通道的颜色索引
     */
    private static final String UNIFORM_INDEX = "uIndex";

    /**
     * 最大通道数量
     * 每个滤镜实例支持最多4个通道
     */
    private static final int MAX_NUMS = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Z序（通道优先级）数组
     * 存储当前通道的优先级顺序
     * 使用volatile保证多线程可见性
     */
    private volatile int[] zorderVal = new int[MAX_NUMS];

    // ═══════════════════════════════════════════════════════════════════════════════
    // 片段着色器源码
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 灰度转RGBA片段着色器源码
     * 
     * <p><b>通道颜色定义：</b></p>
     * <ul>
     *   <li>Ch1: 黄色 #FFFF00 (RGB: 255, 255, 0)</li>
     *   <li>Ch2: 青色 #00FFFF (RGB: 0, 255, 255)</li>
     *   <li>Ch3: 品红 #FF00FF (RGB: 255, 0, 255)</li>
     *   <li>Ch4: 绿色 #00FF00 (RGB: 0, 255, 0)</li>
     *   <li>Ch5: 橙色 #FF8000 (RGB: 255, 128, 0)</li>
     *   <li>Ch6: 天蓝 #0055FF (RGB: 0, 85, 255)</li>
     *   <li>Ch7: 粉红 #FF667F (RGB: 255, 102, 127)</li>
     *   <li>Ch8: 青绿 #26FFB7 (RGB: 38, 255, 183)</li>
     * </ul>
     * 
     * <p><b>着色器功能：</b></p>
     * <ol>
     *   <li>定义8个通道的颜色数组</li>
     *   <li>采样纹理获取灰度值</li>
     *   <li>根据zorder确定优先通道</li>
     *   <li>根据通道索引获取颜色</li>
     *   <li>输出最终颜色（灰度值 × 通道颜色）</li>
     * </ol>
     */
    private static final String GRAY2RGBA_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +                                              // 声明高精度浮点数
            "varying vec2 " + VARYING_TEXTURE_COORD + ";\n" +                         // 纹理坐标varying变量
            "uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n" +                  // 当前纹理采样器
            "uniform int " + UNIFORM_INDEX + ";\n" +                                  // 通道索引
            "uniform int " + UNIFORM_ZORDER + "[4];\n" +                              // Z序数组（通道优先级）
            "uniform float " + UNIFORM_OFFSET + "[4];\n" +                            // 偏移量数组
            "vec4 colorArray[8];\n"+                                                  // 声明颜色数组（8个通道）
            "void main() {\n" +                                                       // main函数入口
            "   vec4 color,color1;\n" +                                               // 声明颜色变量
            "   vec2 p,p1;\n" +                                                       // 声明纹理坐标变量
            "   float c = 1.0;\n" +                                                   // 声明灰度值变量，初始为1.0
            "   int i ,idx ;\n"+                                                      // 声明索引变量
            "   idx = 0;\n"+                                                          // 初始化idx为0
            // 初始化颜色数组 - 8个通道的颜色
            "   colorArray[0] = vec4(1.0,1.0,0.0,1.0);\n"+                            // CH1: 黄色 (R=1.0, G=1.0, B=0.0)
            "   colorArray[1] = vec4(0.0,1.0,1.0,1.0);\n"+                            // CH2: 青色 (R=0.0, G=1.0, B=1.0)
            "   colorArray[2] = vec4(1.0,0.0,1.0,1.0);\n"+                            // CH3: 品红 (R=1.0, G=0.0, B=1.0)
            "   colorArray[3] = vec4(0.0,1.0,0.0,1.0);\n"+                            // CH4: 绿色 (R=0.0, G=1.0, B=0.0)
            "   colorArray[4] = vec4(1.0,0.50196,0.0,1.0);\n"+                        // CH5: 橙色 (R=1.0, G=0.5, B=0.0)
            "   colorArray[5] = vec4(0.0,0.33333,1.0,1.0);\n"+                        // CH6: 天蓝 (R=0.0, G=0.33, B=1.0)
            "   colorArray[6] = vec4(1.0,0.4,0.498039,1.0);\n"+                       // CH7: 粉红 (R=1.0, G=0.4, B=0.5)
            "   colorArray[7] = vec4(0.0,0.666667,1.0,1.0);\n"+                       // CH8: 青绿 (R=0.0, G=0.67, B=1.0)
            // 缩放偏移处理
            "   if(" + UNIFORM_OFFSET + "[3] > 0.01){\n" +                            // 如果有缩放高度
            "       if(" + VARYING_TEXTURE_COORD + ".y > " + UNIFORM_OFFSET + "[3]){\n" + // 如果y坐标大于缩放高度
            "           p = " + VARYING_TEXTURE_COORD + ";\n"+                        // 使用原始坐标
            "       }else{\n"+                                                        // 否则
            "           p = " + VARYING_TEXTURE_COORD  + " + vec2(" + UNIFORM_OFFSET + "[0]," +UNIFORM_OFFSET + "[1]);\n" + // 应用偏移
            "           if( p.y > " + UNIFORM_OFFSET + "[3]){\n" +                    // 如果偏移后y坐标大于缩放高度
            "               c = 0.0;\n"+                                              // 设置灰度为0（透明）
            "           }\n"+
            "       }\n"+
            "   }else{\n"+                                                            // 无缩放
            "       p = " + VARYING_TEXTURE_COORD  + " + vec2(" + UNIFORM_OFFSET + "[0]," +UNIFORM_OFFSET + "[1]);\n" + // 直接应用偏移
            "   }\n"+
            // 纹理采样
            "   color1 = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", p);\n" +          // 采样偏移后的纹理
            "   if(" + UNIFORM_OFFSET + "[2] > 0.5){\n" +                             // 如果有偏移标志
            "       color = color1;\n" +                                              // 使用偏移后的颜色
            "   }else{\n"+                                                            // 否则
            "       color = texture2D(" + TEXTURE_SAMPLER_UNIFORM + "," + VARYING_TEXTURE_COORD + ");\n" + // 采样原始纹理
            "   }\n"+
            // 多通道优先级处理
            "   idx = " + UNIFORM_ZORDER + "[0];\n" +                                 // 获取第一个通道索引
            "   c = color1[idx] * c;\n" +                                             // 获取灰度值
            "   if(c < 0.003){\n" +                                                   // 如果灰度值太小
            "       for( i = 1;  i < 4; i++ ) {\n" +                                  // 遍历其他通道
            "           idx = " + UNIFORM_ZORDER + "[i];\n" +                         // 获取通道索引
            "           c = color[idx];\n" +                                          // 获取灰度值
            "           if(c > 0.003){\n" +                                           // 如果灰度值足够大
            "               break;\n"+                                                // 跳出循环
            "           }\n"+
            "       }\n"+
            "   }\n"+
            // 输出颜色
            "   idx = idx + " + UNIFORM_INDEX + ";\n" +                               // 计算最终通道索引
            "   gl_FragColor = vec4(colorArray[idx].r*c,colorArray[idx].g*c,colorArray[idx].b*c,step(0.003,c));\n" + // 输出最终颜色（灰度×通道颜色）
            "}\n";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造灰度转RGBA滤镜实例
     * 
     * @param idx 实例索引，用于多通道显示时标识当前通道组
     *            取值范围：0 ~ (通道组数-1)
     *            每个实例支持4个通道，idx=0对应CH1-CH4，idx=1对应CH5-CH8
     */
    public Gray2RGBAFilter(int idx){
        super();                                                                   // 调用父类构造方法
        this.index = idx;                                                          // 设置实例索引
        for(int i=0;i<MAX_NUMS;i++){                                               // 遍历4个通道
            zorderVal[i] = i + idx * 4;                                            // 初始化Z序数组
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 着色器程序方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取顶点着色器
     * 使用父类的默认顶点着色器
     *
     * @return 顶点着色器源码
     */
    @Override
    public String getVertexShader() {
        return super.getVertexShader();                                            // 返回父类顶点着色器
    }

    /**
     * 获取片段着色器
     * 返回灰度转RGBA的片段着色器
     *
     * @return 灰度转RGBA片段着色器源码
     */
    @Override
    public String getFragmentShader() {
        return GRAY2RGBA_FRAGMENT_SHADER;                                          // 返回片段着色器
    }

    /**
     * 获取OES片段着色器程序
     * 用于处理外部纹理（如相机纹理）
     *
     * @return 父类OES片段着色器源码
     */
    @Override
    public String getOesFragmentProgram() {
        return super.getOesFragmentProgram();                                      // 返回父类OES着色器
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 渲染前处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制前处理
     * 在绘制前设置uniform变量和纹理参数
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>调用父类的onPreDraw方法</li>
     *   <li>设置Z序uniform变量</li>
     *   <li>设置通道索引uniform变量</li>
     *   <li>设置纹理边框颜色</li>
     *   <li>计算并设置偏移量uniform变量</li>
     * </ol>
     *
     * @param program OpenGL程序ID
     * @param texture 当前纹理
     * @param canvas OpenGL画布
     */
    @Override
    public void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas) {
        super.onPreDraw(program, texture, canvas);                                 // 调用父类方法
        synchronized (this) {                                                      // 同步块，保证线程安全
            // 设置Z序uniform变量（通道优先级数组）
            GLES20.glUniform1iv(GLES20.glGetUniformLocation(program, UNIFORM_ZORDER), zorderVal.length, zorderVal, 0);

            // 设置通道索引uniform变量（用于计算颜色索引）
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, UNIFORM_INDEX), index * 4);

            // 设置纹理边框颜色（黑色透明）
            float[] borderColor = {0, 0, 0, 0};                                     // 边框颜色：黑色透明
            float[] xyOffset = {0, 0, 0, 0};                                        // 偏移量数组

            // 设置纹理边框颜色
            GLES32.glTexParameterfv(GLES20.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BORDER_COLOR, borderColor, 0);

            // 计算归一化偏移量
            xyOffset[0] = (float) xOffset / texture.getTextureWidth();              // X偏移（归一化）
            xyOffset[1] = bTop ? (float) yOffset / texture.getTextureHeight() : 0;  // Y偏移（归一化，仅在顶部模式时有效）
            xyOffset[2] = Math.abs(xOffset) > 0 ? 1 : 0;                            // 是否有偏移标志
            xyOffset[3] = bZoom ? (float) ScopeBase.getZoomHeight() / texture.getTextureHeight() : 0; // 缩放高度（归一化）

            // 设置偏移量uniform变量
            GLES20.glUniform1fv(GLES20.glGetUniformLocation(program, UNIFORM_OFFSET), xyOffset.length, xyOffset, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 状态标志
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 顶部模式标志
     * true: Y偏移应用于顶部
     * false: Y偏移不应用
     * 使用volatile保证多线程可见性
     */
    private volatile boolean bTop = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参数设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置顶部模式
     * 控制Y偏移是否应用于顶部
     *
     * @param bTop 是否启用顶部模式
     *             true: Y偏移应用于顶部
     *             false: Y偏移不应用
     */
    public synchronized void setTop(boolean bTop){
        this.bTop = bTop;                                                          // 设置顶部模式标志
    }

    /**
     * 设置Z序（通道优先级）
     * 更新通道显示的优先级顺序
     *
     * @param zorder Z序数组，包含所有通道的优先级
     *               数组大小应为：通道总数
     */
    public synchronized void setZorder(int [] zorder){
        for(int i=0;i<zorderVal.length;i++){                                       // 遍历4个通道
            zorderVal[i] = zorder[i + index * 4] - index * 4;                      // 更新Z序值
        }
    }

    /**
     * 设置偏移量
     * 用于实现波形平移功能
     *
     * @param x X方向偏移量（像素）
     * @param y Y方向偏移量（像素）
     */
    public synchronized void setOffset(int x,int y){
        xOffset = x;                                                               // 设置X偏移
        yOffset = y;                                                               // 设置Y偏移
    }

    /**
     * 设置缩放状态
     * 启用或禁用缩放功能
     *
     * @param bZoom 是否启用缩放
     *              true: 启用缩放
     *              false: 禁用缩放
     */
    public synchronized void setZoom(boolean bZoom){
        this.bZoom = bZoom;                                                        // 设置缩放状态
    }

    /**
     * 设置通道索引
     * 用于多通道显示时标识当前通道组
     *
     * @param index 通道索引
     *              0: CH1-CH4
     *              1: CH5-CH8
     */
    public synchronized void setIndex(int index){
        this.index = index;                                                        // 设置通道索引
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 偏移和缩放参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /** X方向偏移量（像素），使用volatile保证多线程可见性，默认值：0 */
    private volatile int xOffset = 0;

    /** Y方向偏移量（像素），使用volatile保证多线程可见性，默认值：0 */
    private volatile int yOffset = 0;

    /** 是否启用缩放，使用volatile保证多线程可见性，默认值：false */
    private volatile boolean bZoom = false;

    /** 通道索引，使用volatile保证多线程可见性，默认值：0 */
    private volatile int index = 0;
}
