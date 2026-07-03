package com.micsig.tbook.tbookscope.wavezone.display; // 示波器波形显示区显示模块包

import android.content.Context; // 导入Android上下文类

import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类，提供事件ID
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂，管理事件注册和分发
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者接口
import com.micsig.tbook.scope.Scope; // 导入示波器核心类，提供Zoom状态和缩放倍数
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类，提供屏幕宽度等基础参数
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴管理器，提供时间位置/像素转换
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol; // 导入外部按键协议，提供返回状态管理
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，提供键值存取
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase; // 导入触发时基管理器

/*
 * +===========================================================================================+
 * |                            WaveMaskLayer_YTZoomAction 类说明                                |
 * +-------------------------------------------------------------------------------------------+
 * | 模块定位 : YT模式Zoom遮罩层的动作控制器，负责事件监听与遮罩层状态同步                             |
 * | 核心职责 : 1.监听Zoom进入、时基缩放、时基位移、采样图更新等事件                                    |
 * |           2.根据事件类型计算并更新遮罩层位置(layerX)、宽度(layerWidth)、触发位置(layerTimebaseX)    |
 * |           3.响应遮罩层X坐标移动事件，同步更新水平轴位置和触发时基位置                                |
 * | 架构设计 : 事件驱动模式，通过EventFactory注册观察者，接收事件后驱动WaveMaskLayer_YTZoom状态更新      |
 * | 数据流向 : EventFactory事件 → eventUIObserver.update() → 计算遮罩参数 →                        |
 * |           WaveMaskLayer_YTZoom.set*() → draw()                                                |
 * |           遮罩层移动 → onZoomEventListener → 水平轴/触发时基同步更新                              |
 * | 依赖关系 : WaveMaskLayer_YTZoom（遮罩层单例）、HorizontalAxis（水平轴）、TriggerTimebase（触发时基）、|
 * |            Scope（示波器状态）、EventFactory（事件总线）、CacheUtil（缓存）、                      |
 * |            ExternalKeysProtocol（外部按键）                                                    |
 * | 使用场景 : Zoom模式下，响应用户操作（进入Zoom、调整时基、移动波形）自动同步遮罩层显示                 |
 * +===========================================================================================+
 */
public class WaveMaskLayer_YTZoomAction {
    private static final String TAG = "WaveMaskLayer_YTZoomAction"; // 日志标签
    private Context context; // Android上下文引用
    WaveMaskLayer_YTZoom waveMaskLayer_ytZoom = null; // Zoom遮罩层单例引用

    /**
     * 构造方法：初始化遮罩层引用，注册事件观察者和缩放事件监听器
     *
     * @param context Android上下文对象
     */
    public WaveMaskLayer_YTZoomAction(Context context) {
        this.context = context; // 保存上下文引用
        waveMaskLayer_ytZoom = WaveMaskLayer_YTZoom.getInstance(); // 获取Zoom遮罩层单例
        waveMaskLayer_ytZoom.setOnZoomEventListenerListener(onZoomEventListener); // 注册遮罩层移动事件监听器
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_ZOOM_ENTER, eventUIObserver); // 注册Zoom进入事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE, eventUIObserver); // 注册时基缩放事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_POS, eventUIObserver); // 注册时基位移事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_UI_SAMPLE_GRAPH, eventUIObserver); // 注册采样图更新事件观察者
    }

    /**
     * 设置外部按键返回状态为Zoom弹出状态
     * 用于通知外部按键模块当前处于Zoom模式
     */
    private void setExternalKeyBackStateFocus(){
        ExternalKeysProtocol.BackStateUpdate(ExternalKeysProtocol.BACKSTATE_ZOOMUP); // 更新外部按键返回状态为Zoom弹出状态
    }

    /**
     * Zoom遮罩层X坐标移动事件监听器
     * 当遮罩层被拖动时，同步更新水平轴位置和触发时基位置
     */
    private WaveMaskLayer_YTZoom.OnZoomEventListener onZoomEventListener = new WaveMaskLayer_YTZoom.OnZoomEventListener() {
        /**
         * 遮罩层X坐标变化回调：将像素偏移量转换为时间偏移量，更新水平轴和触发时基位置
         *
         * @param offset 遮罩层X方向移动的像素偏移量
         */
        @Override
        public void onLayerXChangeEvent(int offset) {
            CacheUtil.get().setLastObjectIsCursor(false); // 标记最近操作对象不是光标，避免光标相关逻辑干扰
            setExternalKeyBackStateFocus(); // 更新外部按键返回状态为Zoom模式
            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴管理器单例
            long lx = horizontalAxis.getTimePose(HorizontalAxis.WPI_STANDARD, offset); // 将像素偏移量转换为标准视图下的时间偏移量
            horizontalAxis.setTimePosOfView(horizontalAxis.getTimePosOfView() + lx); // 将时间偏移量叠加到当前视图时间位置
            horizontalAxis.correctTimePose_poseMove(); // 修正时间位置，确保在合法范围内
            long pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 计算触发时基线距离屏幕中心的像素偏移
            TriggerTimebase.getInstance().setX_disFromEventBus(pix); // 通过事件总线设置触发时基线的X像素位置
        }
    };


    /**
     * UI事件观察者：监听Zoom相关事件并更新遮罩层参数
     * 处理的事件包括：Zoom进入、时基缩放、时基位移、采样图更新
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        /**
         * 事件更新回调：根据事件ID执行对应的遮罩层参数计算和更新
         *
         * @param data 事件数据对象，包含事件ID
         */
        @Override
        public void update(Object data) {
            boolean updata = false; // 是否需要重绘遮罩层标志
            if (!Scope.getInstance().isZoom()) // 如果当前不在Zoom模式下
                return; // 直接返回，不处理事件

            if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_ZOOM_ENTER) { // 处理Zoom进入事件
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴管理器单例

                waveMaskLayer_ytZoom.setLayerTimebaseX(ScopeBase.getWidth() / 2 - horizontalAxis.getTimePoseOfGrid(HorizontalAxis.WPI_STANDARD)); // 计算并设置触发位置：屏幕中心减去触发位置在标准网格上的时间偏移

                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE); // 标准视图时间位置 - 大视图时间位置 = Zoom窗口中心与触发位置的偏移
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx); // 将时间偏移量按标准视图时基刻度转换为像素偏移
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx; // 屏幕中心减去像素偏移，得到Zoom窗口中心X坐标
                waveMaskLayer_ytZoom.setLayerX(lx); // 设置遮罩层中心X坐标

                //计算宽度
                double dx = Scope.getInstance().screenNum_zoom(); // 获取Zoom缩放倍数（屏幕数）
                int width = (int) (ScopeBase.getWidth() / dx); // 屏幕宽度除以缩放倍数 = Zoom窗口像素宽度
                waveMaskLayer_ytZoom.setLayerWidth(width); // 设置遮罩层窗口宽度

                updata = true; // 标记需要重绘
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_TIME_SCALE) { // 处理时基缩放事件
                double dx = Scope.getInstance().screenNum_zoom(); // 获取Zoom缩放倍数（屏幕数）
                int width = (int) (ScopeBase.getWidth() / dx); // 屏幕宽度除以缩放倍数 = Zoom窗口像素宽度
                waveMaskLayer_ytZoom.setLayerWidth(width); // 更新遮罩层窗口宽度
                updata = true; // 标记需要重绘
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_TIME_POS) { // 处理时基位移事件
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴管理器单例
                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE); // 标准视图时间位置 - 大视图时间位置 = Zoom窗口中心与触发位置的偏移
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx); // 将时间偏移量按标准视图时基刻度转换为像素偏移
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx; // 屏幕中心减去像素偏移，得到Zoom窗口中心X坐标
                waveMaskLayer_ytZoom.setLayerX(lx); // 更新遮罩层中心X坐标
                updata = true; // 标记需要重绘
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_UI_SAMPLE_GRAPH) { // 处理采样图更新事件
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴管理器单例


                long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL); // 从缓存中读取主窗口时基位置（标准模式）
                long tempTomePos = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD); // 获取当前标准视图的时间位置
                if (tempTomePos != ScopeBase.getWidth() / 2 - l) { // 如果当前时间位置与缓存计算的期望位置不一致
                    horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, ScopeBase.getWidth() / 2 - l); // 修正标准视图时间位置为期望值
                }

                long layerTimebaseX = ScopeBase.getWidth() / 2 - horizontalAxis.getTimePoseOfGrid(HorizontalAxis.WPI_STANDARD); // 计算触发位置X坐标：屏幕中心减去触发位置网格偏移
                if (waveMaskLayer_ytZoom.layerTimebaseX != layerTimebaseX) { // 如果触发位置与当前遮罩层触发位置不一致
                    waveMaskLayer_ytZoom.setLayerTimebaseX(layerTimebaseX); // 更新遮罩层触发位置
                }

                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE); // 标准视图时间位置 - 大视图时间位置 = Zoom窗口中心与触发位置的偏移
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx); // 将时间偏移量按标准视图时基刻度转换为像素偏移
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx; // 屏幕中心减去像素偏移，得到Zoom窗口中心X坐标
                waveMaskLayer_ytZoom.setLayerX(lx); // 更新遮罩层中心X坐标

                double dx = Scope.getInstance().screenNum_zoom(); // 获取Zoom缩放倍数（屏幕数）
                int width = (int) (ScopeBase.getWidth() / dx); // 屏幕宽度除以缩放倍数 = Zoom窗口像素宽度
                if (width != waveMaskLayer_ytZoom.layerWidth) { // 如果计算宽度与当前遮罩层宽度不一致
                    waveMaskLayer_ytZoom.setLayerWidth(width); // 更新遮罩层窗口宽度
                }

                updata = true; // 标记需要重绘
            }
            if (updata) // 如果有任何参数变更需要重绘
                waveMaskLayer_ytZoom.draw(); // 重新绘制遮罩层
        }
    };
}
