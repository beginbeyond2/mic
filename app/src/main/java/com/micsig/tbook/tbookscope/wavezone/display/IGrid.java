package com.micsig.tbook.tbookscope.wavezone.display;  // 网格显示接口所在包

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口，用于纹理绘制

/*
 * +=============================================================================+
 * |                           IGrid — 网格显示接口                               |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：定义示波器波形区网格的绘制属性与绘制行为契约                         |
 * | 架构设计：纯接口，由具体实现类提供网格绘制的完整逻辑                           |
 * | 数据流向：上层调用 → IGrid.draw() → OpenGL画布渲染网格纹理                   |
 * | 依赖关系：ICanvasGL（OpenGL画布接口）                                         |
 * | 使用场景：示波器波形区背景网格的样式切换、亮度调节、高度分割设置               |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/11/3.
 */

public interface IGrid {

    /** 十字线网格属性标志 */  // 十字线样式
    public static final int GridAttr_CrossLine = 0x01;  // 十字线属性

    /** 十字点网格属性标志 */  // 十字点样式
    public static final int GridAttr_CrossPoint = 0x02;  // 十字点属性

    /** 全点网格属性标志 */  // 全点样式
    public static final int GridAttr_ALLPoint = 0x04;  // 全点属性

    /** 边框网格属性标志 */  // 仅边框样式
    public static final int GridAttr_Frame = 0x08;  // 边框属性

    /**
     * 获取当前网格线属性（样式标志）
     *
     * @return 网格属性值，为GridAttr_*常量的组合
     */
    public int getGridLine_Attr();  // 获取网格线属性

    /**
     * 设置网格线属性（样式标志）
     *
     * @param gridLine_Attr 网格属性值，为GridAttr_*常量的组合
     */
    public void setGridLine_Attr(int gridLine_Attr);  // 设置网格线属性

    /**
     * 获取网格线亮度值
     *
     * @return 亮度百分比（0~100）
     */
    public int getGridLine_Bright();  // 获取网格线亮度

    /**
     * 设置网格线亮度值
     *
     * @param gridLine_Bright 亮度百分比（0~100）
     */
    public void setGridLine_Bright(int gridLine_Bright);  // 设置网格线亮度

    /**
     * 在OpenGL画布上绘制网格
     *
     * @param canvas OpenGL画布对象
     */
    public void draw(ICanvasGL canvas);  // 绘制网格到画布

    /**
     * 刷新网格显示（重新绘制）
     */
    public void refresh();  // 刷新网格

    /**
     * 设置垂直方向的分割数（高度分割数）
     *
     * @param heightDiv 垂直分割数
     */
    public void setHeightDiv(int heightDiv);  // 设置高度分割数
}
