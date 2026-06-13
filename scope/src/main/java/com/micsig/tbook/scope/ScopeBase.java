package com.micsig.tbook.scope;

import com.micsig.base.Logger;

/**
 * 示波器基础配置类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope（示波器核心模块）</li>
 *   <li>架构层级：基础设施层 - 配置常量</li>
 *   <li>设计模式：工具类模式</li>
 *   <li>职责类型：基础配置提供</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义示波器显示区域的尺寸常量</li>
 *   <li>定义网格参数常量</li>
 *   <li>提供UI与FPGA之间的坐标转换系数</li>
 *   <li>提供数值精度处理工具方法</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>集中管理示波器显示相关的常量配置</li>
 *   <li>便于不同分辨率屏幕的适配</li>
 *   <li>统一UI坐标与FPGA坐标的转换逻辑</li>
 *   <li>提供全局访问的基础参数</li>
 * </ul>
 * 
 * <p><b>显示区域尺寸说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 区域类型      │ 宽度    │ 高度    │ 说明                      │
 * ├───────────────┼─────────┼─────────┼────────────────────────────┤
 * │ 主窗口        │ 1800    │ 1000    │ 12格×150像素/格           │
 * │ Zoom窗口      │ 1800    │ 750     │ 12格×150像素/格           │
 * │ XY模式        │ 800     │ 800     │ 正方形显示区域            │
 * │ Zoom缩略区    │ -       │ 250     │ 主窗口高度-Zoom高度       │
 * │ Zoom放大区    │ -       │ 750     │ Zoom窗口高度              │
 * └───────────────┴─────────┴─────────┴────────────────────────────┘
 * </pre>
 * 
 * <p><b>网格参数说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 参数              │ 主窗口 │ Zoom窗口 │ 说明                  │
 * ├───────────────────┼────────┼──────────┼──────────────────────┤
 * │ 水平格数          │ 12     │ 12       │ 时间轴方向            │
 * │ 垂直格数          │ 10     │ 10       │ 幅度轴方向            │
 * │ 每格水平像素      │ 150    │ 150      │ 水平分辨率            │
 * │ 每格垂直像素      │ 100    │ 75       │ 垂直分辨率            │
 * └───────────────────┴────────┴──────────┴──────────────────────┘
 * </pre>
 * 
 * <p><b>坐标转换说明：</b>
 * <pre>
 * UI坐标系统：屏幕显示坐标，受屏幕分辨率影响
 * FPGA坐标系统：固定坐标系统，用于波形数据处理
 * 
 * 转换公式：
 *   UI坐标 = FPGA坐标 × ToUICoff
 *   FPGA坐标 = UI坐标 × ToFPGACoff
 * 
 * XY模式下固定使用1.0系数
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>波形绘制时获取显示区域尺寸</li>
 *   <li>坐标转换时获取转换系数</li>
 *   <li>网格绘制时获取网格参数</li>
 *   <li>Zoom模式布局计算</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/16
 * @see Scope 示波器核心管理类
 */
public class ScopeBase {

    /**
     * XY模式显示宽度
     */
    private static final int MS_XY_W = 800;
    
    /**
     * XY模式显示高度
     */
    private static final int MS_XY_H = 800;
    
    /**
     * 水平网格数量（时间轴方向）
     */
    private static final int MS_GRID_H =            12;
    
    /**
     * 垂直网格数量（幅度轴方向）
     */
    private static final int MS_GRID_V =            10;
    
    /**
     * 每格水平像素数
     */
    private static final int MS_GRID_PerH =         150;
    
    /**
     * 每格垂直像素数
     */
    private static final int MS_GRID_PerV =         100;
    
    /**
     * Zoom模式水平网格数量
     */
    private static final int MS_ZOOM_GRID_H =       MS_GRID_H;
    
    /**
     * Zoom模式垂直网格数量
     */
    private static final int MS_ZOOM_GRID_V =      MS_GRID_V;
    
    /**
     * Zoom模式每格水平像素数
     */
    private static final int MS_ZOOM_GRID_PerH =   MS_GRID_PerH;
    
    /**
     * Zoom模式每格垂直像素数
     */
    private static final int MS_ZOOM_GRID_PerV =  75;
    
    /**
     * 示波器主窗口宽度
     * 
     * <p><b>计算公式：</b>MS_GRID_PerH × MS_GRID_H = 150 × 12 = 1800
     */
    private static final int OSCILLO_WIDTH =       (MS_GRID_PerH*MS_GRID_H);
    
    /**
     * 示波器主窗口高度
     * 
     * <p><b>计算公式：</b>MS_GRID_PerV × MS_GRID_V = 100 × 10 = 1000
     */
    private static final int OSCILLO_HEIGHT =      (MS_GRID_PerV*MS_GRID_V);
    
    /**
     * Zoom窗口宽度
     */
    private static final int OSCILLO_ZOOM_WIDTH =  OSCILLO_WIDTH;
    
    /**
     * Zoom窗口高度
     * 
     * <p><b>计算公式：</b>MS_ZOOM_GRID_PerV × MS_ZOOM_GRID_V = 75 × 10 = 750
     */
    private static final int OSCILLO_ZOOM_HEIGHT = (MS_ZOOM_GRID_PerV*MS_ZOOM_GRID_V);
    
    /**
     * FPGA到UI的坐标转换系数
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于将FPGA坐标转换为UI显示坐标</li>
     *   <li>默认值为1.0</li>
     *   <li>XY模式下固定使用1.0</li>
     * </ul>
     */
    private static double ToUICoff = 1.0f;
    
    /**
     * UI到FPGA的坐标转换系数
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于将UI坐标转换为FPGA坐标</li>
     *   <li>默认值为1.0</li>
     *   <li>XY模式下固定使用1.0</li>
     * </ul>
     */
    private static double ToFPGACoff = 1.0f;

    /**
     * 获取FPGA到UI的坐标转换系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>XY模式固定返回1.0</li>
     *   <li>其他模式返回当前转换系数</li>
     * </ul>
     * 
     * @return 坐标转换系数
     */
    public static double getToUICoff() {
        if (Scope.getInstance().isInXYMode()) {
            return 1.0;
        } else {
            return ToUICoff;
        }
    }

    /**
     * 获取UI到FPGA的坐标转换系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>XY模式固定返回1.0</li>
     *   <li>其他模式返回当前转换系数</li>
     * </ul>
     * 
     * @return 坐标转换系数
     */
    public static double getToFPGACoff() {
        if (Scope.getInstance().isInXYMode()) {
            return 1.0;
        } else {
            return ToFPGACoff;
        }
    }

    /**
     * 设置坐标转换系数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据新的显示高度计算转换系数</li>
     *   <li>ToUICoff = 新高度 / 1000</li>
     *   <li>ToFPGACoff = 1000 / 新高度</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>屏幕分辨率变化时</li>
     *   <li>显示区域大小调整时</li>
     * </ul>
     * 
     * @param newHeight 新的显示高度
     */
    public static void setConvertScale(double newHeight) {
        ScopeBase.ToUICoff = newHeight / 1000;
        ScopeBase.ToFPGACoff = 1000 / newHeight;
        Logger.d("ScopeBase", "ScopeBase ToUICoff= " + ToUICoff + " ,ToFPGACoff= " + ToFPGACoff);
    }

    /**
     * 获取主窗口宽度
     * 
     * <p><b>返回值：</b>1800像素
     * 
     * @return 主窗口宽度（像素）
     */
    public static int getWidth(){
        return OSCILLO_WIDTH;
    }

    /**
     * 获取Zoom窗口宽度
     * 
     * <p><b>返回值：</b>1800像素
     * 
     * @return Zoom窗口宽度（像素）
     */
    public static int getZoomWidth(){
        return OSCILLO_ZOOM_WIDTH;
    }
    
    /**
     * 获取主窗口高度
     * 
     * <p><b>返回值：</b>1000像素
     * 
     * @return 主窗口高度（像素）
     */
    public static int getHeight(){
        return OSCILLO_HEIGHT;
    }
    
    /**
     * 获取Zoom窗口高度
     * 
     * <p><b>返回值：</b>750像素
     * 
     * @return Zoom窗口高度（像素）
     */
    public static int getZoomHeight(){
        return OSCILLO_ZOOM_HEIGHT;
    }
    
    /**
     * 获取水平网格数量
     * 
     * <p><b>返回值：</b>12格
     * 
     * @return 水平网格数量
     */
    public static int getHorizonGridCnt(){
        return MS_GRID_H;
    }
    
    /**
     * 获取垂直网格数量
     * 
     * <p><b>返回值：</b>10格
     * 
     * @return 垂直网格数量
     */
    public static int getVerticalGridCnt(){
        return MS_GRID_V;
    }
    
    /**
     * 获取每格水平像素数
     * 
     * <p><b>返回值：</b>150像素/格
     * 
     * @return 每格水平像素数
     */
    public static int getHorizonPerGridPixels(){
        return MS_GRID_PerH;
    }
    
    /**
     * 获取每格垂直像素数
     * 
     * <p><b>返回值：</b>100像素/格
     * 
     * @return 每格垂直像素数
     */
    public static int getVerticalPerGridPixels(){
        return MS_GRID_PerV;
    }

    /**
     * 获取Zoom模式每格水平像素数
     * 
     * <p><b>返回值：</b>150像素/格
     * 
     * @return Zoom模式每格水平像素数
     */
    public static int getZoomHorizonPerGridPixels(){
        return MS_ZOOM_GRID_PerH;
    }
    
    /**
     * 获取Zoom模式每格垂直像素数
     * 
     * <p><b>返回值：</b>75像素/格
     * 
     * @return Zoom模式每格垂直像素数
     */
    public static int getZoomVerticalPerGridPixels(){
        return MS_ZOOM_GRID_PerV;
    }

    /**
     * 获取Zoom模式缩略波形区高度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>Zoom模式下显示原始波形的区域高度</li>
     *   <li>计算公式：主窗口高度 - Zoom窗口高度 = 1000 - 750 = 250</li>
     * </ul>
     * 
     * <p><b>Zoom模式布局：</b>
     * <pre>
     * ┌─────────────────────────────────────┐
     * │                                     │
     * │     缩略波形区 (250像素高)          │
     * │     显示完整波形的缩略图            │
     * │                                     │
     * ├─────────────────────────────────────┤ ← 分隔线
     * │                                     │
     * │                                     │
     * │     放大波形区 (750像素高)          │
     * │     显示选中区域的放大波形          │
     * │                                     │
     * │                                     │
     * └─────────────────────────────────────┘
     * </pre>
     * 
     * @return 缩略波形区高度（像素）
     */
    public static int zoomYGrid_suolue(){
        return OSCILLO_HEIGHT-OSCILLO_ZOOM_HEIGHT;
    }

    /**
     * 获取Zoom模式放大波形区高度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>Zoom模式下显示放大波形的区域高度</li>
     *   <li>等于Zoom窗口高度 = 750像素</li>
     * </ul>
     * 
     * @return 放大波形区高度（像素）
     */
    public static int zoomYGrid_fangda(){
        return OSCILLO_ZOOM_HEIGHT;
    }

    /**
     * 获取波形显示区域高度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>等于主窗口高度 = 1000像素</li>
     *   <li>用于波形绘制的高度参考</li>
     * </ul>
     * 
     * @return 波形显示区域高度（像素）
     */
    public static int YGridWave(){
        return OSCILLO_HEIGHT;
    }

    /**
     * 获取XY模式显示宽度
     * 
     * <p><b>返回值：</b>800像素
     * 
     * @return XY模式显示宽度（像素）
     */
    public static int getXYWidth(){
        return MS_XY_W;
    }

    /**
     * 获取XY模式显示高度
     * 
     * <p><b>返回值：</b>800像素
     * 
     * @return XY模式显示高度（像素）
     */
    public static int getXYHeight(){
        return MS_XY_H;
    }

    /**
     * 获取转换后的主窗口高度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>应用坐标转换系数后的实际显示高度</li>
     *   <li>计算公式：主窗口高度 × ToUICoff</li>
     * </ul>
     * 
     * @return 转换后的主窗口高度（像素）
     */
    public static int getNewHeight(){
        return (int) (getHeight() * getToUICoff());
    }

    /**
     * 获取转换后的Zoom窗口高度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>应用坐标转换系数后的实际Zoom高度</li>
     *   <li>计算公式：Zoom窗口高度 × ToUICoff</li>
     * </ul>
     * 
     * @return 转换后的Zoom窗口高度（像素）
     */
    public static int getNewZoomHeight() {
        return (int) (getZoomHeight() * getToUICoff());
    }

    /**
     * 数值精度处理
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据数值大小自动选择合适的精度</li>
     *   <li>用于显示格式化</li>
     * </ul>
     * 
     * <p><b>精度规则：</b>
     * <pre>
     * ┌────────────────────────────────────────────────────────────────┐
     * │ 数值范围              │ 处理方式              │ 有效位数      │
     * ├───────────────────────┼───────────────────────┼───────────────┤
     * │ |value| < 10E-6       │ 保留9位小数           │ 纳秒级        │
     * │ 10E-6 ≤ |value| < 10E-3│ 保留6位小数          │ 微秒级        │
     * │ |value| ≥ 10E-3       │ 保留3位小数           │ 毫秒级        │
     * └───────────────────────┴───────────────────────┴───────────────┘
     * </pre>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>时基值显示格式化</li>
     *   <li>测量结果显示格式化</li>
     *   <li>参数配置显示</li>
     * </ul>
     * 
     * @param originalValue 原始值
     * @return 处理后的值
     */
    public static double changeAccuracy(double originalValue) {
        if (Math.abs(originalValue) < 10E-6) {
            return Math.round(originalValue * 1000000000) / 1000000000.0;
        } else if (Math.abs(originalValue) < 10E-3) {
            return Math.round(originalValue * 1000000) / 1000000.0;
        } else {
            return Math.round(originalValue * 1000) / 1000.0;
        }
    }

}
