package com.micsig.tbook.scope.surface;

import android.opengl.GLES20;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.OpenGLUtil;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.GLES20Canvas;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   AfterglowFilter - 余辉滤镜类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的OpenGL纹理滤镜类，实现示波器波形的余辉效果。                   ║
 * ║   余辉效果模拟传统模拟示波器的磷光屏特性，使波形产生渐隐的视觉效果。          ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 实现余辉片段着色器（Fragment Shader）                                   ║
 * ║   2. 管理余辉衰减参数                                                        ║
 * ║   3. 管理余辉纹理ID                                                          ║
 * ║   4. 实现新旧帧混合渲染                                                      ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │ BasicTextureFilter │ ← OpenGL纹理滤镜基类        ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                                   ▼                                          ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │ AfterglowFilter │ ← 本类：余辉滤镜               ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【余辉效果原理】                                                             ║
 * ║   余辉效果模拟传统模拟示波器的磷光屏特性：                                    ║
 * ║   - 模拟示波器的磷光屏在电子束扫过后会持续发光一段时间                        ║
 * ║   - 新波形出现时，旧波形逐渐变暗消失                                         ║
 * ║   - 通过混合当前帧和上一帧实现余辉效果                                       ║
 * ║                                                                              ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                     余辉效果示意图                                   │   ║
 * ║   │                                                                     │   ║
 * ║   │   当前帧（新波形）    上一帧（旧波形）    混合结果                    │   ║
 * ║   │                                                                     │   ║
 * ║   │      ┌───┐              ┌───┐              ┌───┐                   │   ║
 * ║   │      │   │              │   │              │   │                   │   ║
 * ║   │      │   │    +         │   │    =         │   │                   │   ║
 * ║   │   ┌──┘   └──┐        ┌──┘   └──┐        ┌──┘   └──┐               │   ║
 * ║   │   │         │        │(淡)     │        │         │               │   ║
 * ║   │   └─────────┘        └─────────┘        └─────────┘               │   ║
 * ║   │                                                                     │   ║
 * ║   │   亮度 = max(新波形, 旧波形 - 衰减值)                               │   ║
 * ║   │   衰减值越大，余辉消失越快                                          │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【着色器算法说明】                                                           ║
 * ║   片段着色器实现以下算法：                                                   ║
 * ║   1. 采样当前帧纹理（tvalNew）                                              ║
 * ║   2. 采样上一帧纹理（tvalOld）                                              ║
 * ║   3. 对旧帧进行衰减：tvalOld -= uVal                                        ║
 * ║   4. 取新旧帧的最大值：color = max(tvalNew, tvalOld)                        ║
 * ║   5. 输出最终颜色                                                           ║
 * ║                                                                              ║
 * ║ 【衰减值说明】                                                               ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 衰减值         │ 效果                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 0.0            │ 余辉永不消失，所有历史波形叠加显示                  │  ║
 * ║   │ 0.01~0.1       │ 余辉缓慢消失，适用于观察缓慢变化的信号              │  ║
 * ║   │ 0.1~0.5        │ 余辉中等速度消失，适用于一般观察                    │  ║
 * ║   │ 1.0            │ 余辉立即消失，只显示当前帧                          │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【OpenGL渲染流程】                                                           ║
 * ║   1. 用户设置余辉纹理ID（上一帧的纹理）                                      ║
 * ║   2. 用户设置衰减值                                                          ║
 * ║   3. onPreDraw()绑定余辉纹理和设置uniform变量                                ║
 * ║   4. 片段着色器执行混合算法                                                  ║
 * ║   5. 输出带有余辉效果的波形                                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → BasicTextureFilter: OpenGL纹理滤镜基类                                  ║
 * ║   → GLES20: OpenGL ES 2.0 API                                               ║
 * ║   → ICanvasGL: OpenGL画布接口                                                ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 示波器波形显示时，用户开启余辉功能                                      ║
 * ║   2. 用户调整余辉时间（通过衰减值控制）                                      ║
 * ║   3. 实时渲染波形时应用余辉滤镜                                              ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   必须在OpenGL渲染线程中调用相关方法。                                       ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-4-2                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class AfterglowFilter extends BasicTextureFilter {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 着色器常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 旧纹理采样器uniform变量名
     * 用于在片段着色器中采样上一帧的纹理
     */
    private static final String TEXTURE_SAMPLER_UNIFORM_OLD = "uTextureSamplerOld";

    /**
     * 衰减值uniform变量名
     * 用于控制余辉消失的速度
     */
    private static final String UNIFORM_VAL = "uVal";

    /**
     * 余辉片段着色器源码
     *
     * <p><b>着色器逻辑：</b>
     * <ol>
     *   <li>声明精度为高精度浮点数</li>
     *   <li>声明纹理坐标varying变量</li>
     *   <li>声明当前帧纹理采样器</li>
     *   <li>声明旧帧纹理采样器</li>
     *   <li>声明衰减值uniform变量</li>
     *   <li>main函数：采样新旧纹理，对旧纹理衰减，取最大值输出</li>
     * </ol>
     *
     * <p><b>GLSL代码说明：</b>
     * <pre>
     *   tvalNew = texture2D(uTextureSampler, vTextureCoord);  // 采样当前帧
     *   tvalOld = texture2D(uTextureSamplerOld, vTextureCoord); // 采样上一帧
     *   tvalOld -= uVal;  // 对旧帧进行衰减
     *   color = max(tvalNew, tvalOld);  // 取最大值（新波形优先显示）
     *   gl_FragColor = color;  // 输出最终颜色
     * </pre>
     */
    private static final String AFTERGLOW_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying vec2 " + VARYING_TEXTURE_COORD + ";\n" +
            "uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n" +
            "uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM_OLD + ";\n" +
            "uniform float " + UNIFORM_VAL + ";\n" +
            "void main() {\n" +
            "   vec4 tvalNew, tvalOld, color;\n" +
            "   tvalNew = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", " + VARYING_TEXTURE_COORD + ");\n" +
            "   tvalOld = texture2D(" + TEXTURE_SAMPLER_UNIFORM_OLD + ", " + VARYING_TEXTURE_COORD + ");\n" +
            "   tvalOld -= "+ UNIFORM_VAL +";\n" +
            "   color = max(tvalNew,tvalOld);\n" +
            "   gl_FragColor = color;\n" +
            "}\n";

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
        return super.getVertexShader();
    }

    /**
     * 获取片段着色器
     * 返回余辉效果的片段着色器
     *
     * @return 余辉片段着色器源码
     */
    @Override
    public String getFragmentShader() {
        return AFTERGLOW_FRAGMENT_SHADER;
    }

    /**
     * 获取OES片段着色器程序
     * 用于处理外部纹理（如相机纹理）
     *
     * @return 余辉片段着色器源码
     */
    @Override
    public String getOesFragmentProgram() {
        return AFTERGLOW_FRAGMENT_SHADER;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 渲染前处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制前处理
     * 在绘制前绑定余辉纹理和设置uniform变量
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>调用父类的onPreDraw方法</li>
     *   <li>激活纹理单元（GL_TEXTURE3）</li>
     *   <li>绑定余辉纹理（上一帧的纹理）</li>
     *   <li>设置衰减值uniform变量</li>
     *   <li>设置旧纹理采样器uniform变量</li>
     * </ol>
     *
     * @param program OpenGL程序ID
     * @param texture 当前纹理
     * @param canvas OpenGL画布
     */
    @Override
    public void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas) {
        super.onPreDraw(program, texture, canvas);

        GLES20.glActiveTexture(textureId);
        GLES20Canvas.checkError();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, afterglowId);
        GLES20Canvas.checkError();
        OpenGLUtil.setFloat(GLES20.glGetUniformLocation(program,UNIFORM_VAL), weakenVal);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, TEXTURE_SAMPLER_UNIFORM_OLD), afterglowId);

    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 衰减值
     * 控制余辉消失的速度
     * 取值范围：0.0 ~ 1.0
     * 默认值：0.0（余辉永不消失）
     *
     * 值越大，余辉消失越快
     */
    private float weakenVal = 0;

    /**
     * 余辉纹理ID
     * 存储上一帧的纹理ID
     * 默认值：0（无效纹理）
     */
    private int afterglowId = 0;

    /**
     * 纹理单元ID
     * 指定使用的纹理单元
     * 默认值：GLES20.GL_TEXTURE3（纹理单元3）
     */
    public int textureId = GLES20.GL_TEXTURE3;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 参数设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置衰减值
     * 控制余辉消失的速度
     *
     * @param val 衰减值
     *            取值范围：0.0 ~ 1.0
     *            0.0: 余辉永不消失
     *            1.0: 余辉立即消失
     *
     * @note 方法内部会自动将值限制在有效范围内
     */
    public void setWeakenVal(float val){
        weakenVal = val <= 0f ? 0.0f : Math.min(val, 1f);
    }

    /**
     * 设置余辉纹理ID
     * 指定上一帧的纹理ID，用于实现余辉效果
     *
     * @param Id 余辉纹理ID（OpenGL纹理ID）
     */
    public void setAfterglowId(int Id){
        afterglowId = Id;
    }

    /**
     * 设置纹理单元
     * 指定使用的纹理单元
     *
     * @param texture 纹理单元ID
     *                如 GLES20.GL_TEXTURE0, GLES20.GL_TEXTURE1, ...
     */
    public void setTexture(int texture){
        this.textureId = texture;
    }


}
