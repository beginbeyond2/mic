package com.micsig.tbook.tbookscope.wavezone.display;  // 示波器波形显示区域的网格管理包

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口，用于GPU加速绘制
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口定义
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理器


/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  模块定位：示波器网格管理器（外观模式）                                      │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  核心职责：统一管理 XY / YT / YTZoom 三种工作模式下的网格绘制策略，           │
 * │           对外提供一致的网格操作接口（IGrid），内部根据当前工作模式              │
 * │           委托给对应的网格实现类。                                           │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  架构设计：外观模式（Facade），单例模式                                       │
 * │           - 实现IWorkMode接口：响应工作模式切换，转发给子网格                  │
 * │           - 实现IGrid接口：提供网格属性、亮度、刷新、绘制等统一操作             │
 * │           - 持有三个子网格对象：WaveGrid_XY / WaveGrid_YT / WaveGrid_YTZoom │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  数据流向：外部调用 → WaveGridManage(分发) → WaveGrid_XY/YT/YTZoom(执行)    │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  依赖关系：                                                                │
 * │           - IWorkMode：工作模式常量与切换接口                                │
 * │           - WorkModeManage：获取当前工作模式                                 │
 * │           - IGrid：网格绘制与属性接口                                       │
 * │           - WaveGrid_XY / WaveGrid_YT / WaveGrid_YTZoom：具体网格实现       │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  使用场景：示波器界面初始化时获取单例，在绘制循环中调用draw()刷新网格，        │
 * │           在模式切换时调用switchWorkMode()，在设置面板中调整网格属性/亮度。    │
 * └────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2017/11/8.
 * 网格管理
 */

public class WaveGridManage implements IWorkMode, IGrid {  // 实现工作模式接口和网格接口
    //region 创建单例
    private static class WaveGridManageHolder {  // 静态内部类持有单例，实现懒加载
        private static final WaveGridManage instance = new WaveGridManage();  // JVM保证线程安全的单例实例
    }

    public static final WaveGridManage getInstance() {  // 获取单例实例的唯一入口
        return WaveGridManageHolder.instance;  // 返回内部类中持有的实例
    }
    //endregion

    private WaveGrid_XY waveGrid_xy;  // XY模式下的网格绘制器
    private WaveGrid_YT waveGrid_yt;  // YT模式下的网格绘制器
    private WaveGrid_YTZoom waveGrid_ytZoom;  // YT缩放模式下的网格绘制器

    /**
     * 构造函数，初始化三种工作模式对应的网格对象
     */
    public WaveGridManage(){
        waveGrid_xy=new WaveGrid_XY();  // 创建XY模式网格实例
        waveGrid_yt=new WaveGrid_YT();  // 创建YT模式网格实例
        waveGrid_ytZoom=new WaveGrid_YTZoom();  // 创建YT缩放模式网格实例
    }

    /**
     * 初始化方法（当前为空实现，预留扩展接口）
     */
    public void init(){}

    //region interface IWorkMode

    /**
     * 切换工作模式，根据模式类型将切换请求转发给对应的网格实现
     *
     * @param workMode 工作模式常量，取值为 WorkMode_XY / WorkMode_YT / WorkMode_YTZOOM
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
          switch (workMode){  // 根据工作模式进行分发
              case IWorkMode.WorkMode_XY:  // XY模式
                  waveGrid_xy.switchWorkMode(workMode);  // 委托给XY网格处理模式切换
                  break;
              case IWorkMode.WorkMode_YT:  // YT模式
              case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式（YT和YTZoom共用YT网格，内部区分）
                  waveGrid_yt.switchWorkMode(workMode);  // 委托给YT网格处理模式切换
                  break;
          }
    }
    //endregion

    //region interface IGrid


    /**
     * 刷新所有网格，重新绘制全部模式的网格位图
     */
    @Override
    public void refresh() {
        waveGrid_xy.refresh();  // 刷新XY模式网格
        waveGrid_yt.refresh();  // 刷新YT模式网格
        waveGrid_ytZoom.refresh();  // 刷新YT缩放模式网格
    }

    /**
     * 设置垂直方向每格像素高度，同时刷新所有网格
     *
     * @param heightDiv 每格的像素高度
     */
    @Override
    public void setHeightDiv(int heightDiv) {
        waveGrid_yt.setHeightDiv(heightDiv);  // 设置YT网格的垂直格高
        waveGrid_ytZoom.setHeightDiv(heightDiv);  // 设置YT缩放网格的垂直格高
        waveGrid_xy.setHeightDiv(heightDiv);  // 设置XY网格的垂直格高
        refresh();  // 重新绘制所有网格
    }

    /**
     * 获取当前工作模式下的网格线属性（十字线、十字点、全网点、边框的组合标志）
     *
     * @return 网格线属性位掩码
     */
    @Override
    public int getGridLine_Attr() {
        switch (WorkModeManage.getInstance().getmWorkMode()){  // 获取当前工作模式
            case IWorkMode.WorkMode_XY:  // XY模式
                return waveGrid_xy.getGridLine_Attr();  // 返回XY网格的属性
            case IWorkMode.WorkMode_YT:  // YT模式
            case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式
                return waveGrid_yt.getGridLine_Attr();  // 返回YT网格的属性
        }
        return 0;  // 未知模式返回0
    }

    /**
     * 设置所有网格的网格线属性
     *
     * @param gridLine_Attr 网格线属性位掩码（十字线|十字点|全网点|边框）
     */
    @Override
    public void setGridLine_Attr(int gridLine_Attr) {
        waveGrid_xy.setGridLine_Attr(gridLine_Attr);  // 设置XY网格属性
        waveGrid_yt.setGridLine_Attr(gridLine_Attr);  // 设置YT网格属性
        waveGrid_ytZoom.setGridLine_Attr(gridLine_Attr);  // 设置YT缩放网格属性
    }

    /**
     * 获取当前工作模式下的网格线亮度
     *
     * @return 亮度百分比值
     */
    @Override
    public int getGridLine_Bright() {
        switch(WorkModeManage.getInstance().getmWorkMode()){  // 获取当前工作模式
            case IWorkMode.WorkMode_XY:  // XY模式
                return waveGrid_xy.getGridLine_Bright();  // 返回XY网格亮度

            case IWorkMode.WorkMode_YT:  // YT模式
            case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式
                return waveGrid_yt.getGridLine_Bright();  // 返回YT网格亮度
        }
        return 0;  // 未知模式返回0
    }

    /**
     * 设置所有网格的网格线亮度
     *
     * @param gridLine_Bright 亮度百分比值
     */
    @Override
    public void setGridLine_Bright(int gridLine_Bright) {
        waveGrid_xy.setGridLine_Bright(gridLine_Bright);  // 设置XY网格亮度
        waveGrid_yt.setGridLine_Bright(gridLine_Bright);  // 设置YT网格亮度
        waveGrid_ytZoom.setGridLine_Bright(gridLine_Bright);  // 设置YT缩放网格亮度
    }

    /**
     * 绘制网格（无模式参数版本，当前为空实现，已弃用）
     *
     * @param canvas OpenGL画布
     */
    @Override
    public void draw(ICanvasGL canvas) {
//        switch (WorkModeManage.get().getmWorkMode()){
//            case IWorkMode.WorkMode_XY:
//                waveGrid_xy.draw(canvas);
//                break;
//            case IWorkMode.WorkMode_YT:waveGrid_yt.draw(canvas);break;
//            case IWorkMode.WorkMode_YTZOOM:waveGrid_ytZoom.draw(canvas); break;
//        }
    }

    /**
     * 根据当前画布模式绘制对应的网格到OpenGL画布上
     *
     * @param currCanvas 当前画布对应的工作模式
     * @param canvas     OpenGL画布
     */
    public void draw(int currCanvas,ICanvasGL canvas){
        switch (currCanvas){  // 根据传入的画布模式分发绘制
            case IWorkMode.WorkMode_XY:  // XY模式
                waveGrid_xy.draw(canvas);  // 绘制XY网格
                break;
            case IWorkMode.WorkMode_YT:waveGrid_yt.draw(canvas);break;  // YT模式：绘制YT网格
            case IWorkMode.WorkMode_YTZOOM:waveGrid_ytZoom.draw(canvas); break;  // YT缩放模式：绘制YT缩放网格
        }
    }
    //endregion
}
