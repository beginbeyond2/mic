package com.micsig.tbook.scope.surface;

import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.RawTexture;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                LayerTextureMath - 数学运算图层纹理类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的数学运算图层纹理类，继承自LayerTexture，                       ║
 * ║   专门用于示波器的数学运算结果显示（如FFT、测量值、数学函数等）。              ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理数学运算结果的纹理显示                                               ║
 * ║   2. 支持纹理偏移绘制（用于波形平移）                                         ║
 * ║   3. 重写父类的纹理设置和绘制方法                                             ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │  LayerTexture   │ ← 父类：图层纹理基类            ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                                   ▼                                          ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │LayerTextureMath │ ← 本类：数学运算图层            ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【与父类的区别】                                                             ║
 * ║   ┌────────────────────┬────────────────────────────────────────────────┐  ║
 * ║   │ 特性               │ 说明                                              │  ║
 * ║   ├────────────────────┼────────────────────────────────────────────────┤  ║
 * ║   │ 纹理存储           │ 使用独立的rawTexture成员变量                     │  ║
 * ║   │ 偏移支持           │ 支持通过translate实现纹理偏移                    │  ║
 * ║   │ SurfaceTexture     │ 不使用SurfaceTexture（数学运算结果直接绘制）     │  ║
 * ║   │ 绘制方式           │ 使用canvas.save/restore保护状态                 │  ║
 * ║   └────────────────────┴────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【绘制流程】                                                                 ║
 * ║   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                ║
 * ║   │ 检查有效性   │───▶│ 保存画布状态 │───▶│ 应用偏移变换 │                ║
 * ║   │   bVaild     │    │ canvas.save  │    │ canvas.translate│             ║
 * ║   └──────────────┘    └──────────────┘    └──────┬───────┘                ║
 * ║                                                   │                         ║
 * ║                                                   ▼                         ║
 * ║   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                ║
 * ║   │ 恢复画布状态 │◀───│ 绘制纹理     │◀───│ 设置绘制区域 │                ║
 * ║   │canvas.restore│    │ drawTexture  │    │ left,top,right,bottom│         ║
 * ║   └──────────────┘    └──────────────┘    └──────────────┘                ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. FFT频谱分析显示                                                         ║
 * ║   2. 数学函数运算结果（A+B、A-B、A×B、A÷B）                                  ║
 * ║   3. 测量值图形显示                                                          ║
 * ║   4. 参考波形显示                                                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   继承父类的线程安全机制，使用volatile修饰共享状态变量。                      ║
 * ║   必须在OpenGL渲染线程中调用绘制方法。                                        ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - LayerTexture: 父类，提供基础图层功能                                     ║
 * ║   - RawTexture: OpenGL纹理对象                                               ║
 * ║   - ICanvasGL: OpenGL画布接口                                                ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LayerTextureMath extends LayerTexture {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 原始纹理对象
     * 存储数学运算结果的纹理数据
     * 与父类的texture变量独立，用于支持偏移绘制
     */
    private RawTexture rawTexture;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造数学运算图层实例（动态通道）
     * 创建一个有效的数学运算图层，但不创建纹理对象
     *
     * @param layerType 图层类型，用于标识图层用途
     *                  例如：LAYER_TYPE_FFT（FFT图层）
     *                        LAYER_TYPE_MATH（数学运算图层）
     */
    public LayerTextureMath(int layerType) {
        super(layerType);                                                           // 调用父类构造方法
    }

    /**
     * 构造数学运算图层实例（指定尺寸）
     * 创建指定尺寸的纹理对象
     *
     * @param width 纹理宽度（像素）
     * @param height 纹理高度（像素）
     * @param layerType 图层类型，用于标识图层用途
     */
    public LayerTextureMath(int width, int height, int layerType) {
        super(width, height, layerType);                                            // 调用父类构造方法
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 重写方法 - 纹理设置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置纹理对象
     * 重写父类方法，将纹理存储到本类的rawTexture变量
     * 用于支持偏移绘制功能
     *
     * @param texture RawTexture纹理对象
     */
    @Override
    public void setTexture(RawTexture texture) {
        rawTexture = texture;                                                       // 存储到本类的纹理变量
    }

    /**
     * 设置SurfaceTexture
     * 重写父类方法，数学运算图层不使用SurfaceTexture
     * 数学运算结果直接绘制到纹理，不需要外部纹理输入
     *
     * @param surfaceTexture SurfaceTexture对象（本类不使用）
     */
    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        // 数学运算图层不使用SurfaceTexture
        // 方法体为空，忽略传入的参数
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 重写方法 - 绘制
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制图层
     * 重写父类方法，支持纹理偏移绘制
     * 
     * <p><b>绘制流程：</b></p>
     * <ol>
     *   <li>检查有效性（bVaild）</li>
     *   <li>如果有rawTexture，使用偏移绘制</li>
     *   <li>否则调用父类绘制方法</li>
     * </ol>
     * 
     * <p><b>偏移绘制原理：</b></p>
     * <pre>
     *   1. canvas.save() - 保存当前画布状态矩阵
     *   2. canvas.translate(offsetX, offsetY) - 平移画布坐标系
     *   3. 绘制纹理 - 在平移后的坐标系中绘制
     *   4. canvas.restore() - 恢复画布状态矩阵
     * </pre>
     *
     * @param canvas OpenGL画布
     */
    @Override
    public void onDraw(ICanvasGL canvas) {
        if(bVaild) {                                                                // 检查图层是否有效
            if(rawTexture != null) {                                                // 检查是否有本地纹理
                canvas.save();                                                      // 保存画布状态（状态矩阵入栈）
                canvas.translate((float) getOffsetX(), (float) getOffsetY());       // 平移画布坐标系（应用偏移）
                // 绘制纹理到指定区域
                // 参数: 纹理, SurfaceTexture(null表示不使用), 左, 上, 右, 下, 滤镜
                canvas.drawSurfaceTexture(rawTexture, null, left, top, left + width, top + height, basicTextureFilter);
                canvas.restore();                                                   // 恢复画布状态（状态矩阵出栈）
            } else {                                                                // 没有本地纹理
                super.onDraw(canvas);                                               // 调用父类绘制方法
            }
        }
    }
}
