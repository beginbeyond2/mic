package com.micsig.tbook.tbookscope.wavezone.trigger; // 触发线模块包

import android.graphics.Bitmap; // 导入Bitmap类，用于图片资源

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Data.WaveData; // 导入波形数据常量类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类，用于注册/分发事件
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者接口
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基础参数类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，用于获取通道信息
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath; // 导入数学通道水平轴类
import com.micsig.tbook.scope.math.MathWave; // 导入数学波形常量类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone; // 导入顶部右侧隐藏消息类
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令转发到UI的消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.util.App; // 导入应用上下文工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式消息Bean
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理类
import com.micsig.tbook.ui.util.BitmapUtil; // 导入Bitmap工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道号常量类

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer接口


/**
 * Created by liwb on 2017/5/18.
 * 触发时刻
 */

/*
 * +=============================================================================+
 * |                          TriggerTimebase                                    |
 * +=============================================================================+
 * | 模块定位   : 示波器波形显示区域 - 触发时刻线（水平位置指示器）                |
 * | 核心职责   : 管理触发时刻线的水平位置，同步更新水平轴偏移与缓存              |
 * | 架构设计   : 继承 VerticalChannelBase，采用单例模式，通过 RxBus 订阅         |
 * |            : 多种事件（缓存加载、工作模式切换、SCPI命令等）驱动位置更新       |
 * | 数据流向   : 用户操作/SCPI命令/事件总线 → setX/setX_dis →                    |
 * |            : HorizontalAxis/RefChannel 位置同步 → 缓存持久化 → UI重绘        |
 * | 依赖关系   : VerticalChannelBase(父类), HorizontalAxis, HorizontalAxisMath,  |
 * |            : ChannelFactory, RefChannel, CursorManage, CacheUtil, RxBus     |
 * | 使用场景   : 用户拖动触发时刻线、SCPI远程设置触发位置、缓存恢复触发位置、     |
 * |            : 工作模式切换后触发线位置同步                                    |
 * +=============================================================================+
 */
public class TriggerTimebase extends VerticalChannelBase {
    private static final String TAG = "TriggerTimebase"; // 日志标签

//    private static final Bitmap resBmp = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap();
//    private static final Bitmap resBmpLeft = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap();
//    private static final Bitmap resBmpRight = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap();

    private static Bitmap resBmp[]; // 触发时刻线图片资源数组（6张：选中3张+未选中3张）
    private MainWaveMsgTriggerTimeBase mainWaveMsgTriggerTimeBase = new MainWaveMsgTriggerTimeBase(x); // 触发时刻线消息Bean，用于RxBus通知

    //region 单例
    /**
     * 触发时刻线单例持有者（静态内部类实现懒加载单例）
     */
    public static class TriggerTimebaseHolder {
        public static final TriggerTimebase instance = new TriggerTimebase(); // 单例实例
    }

    /**
     * 获取触发时刻线单例实例
     * @return TriggerTimebase单例
     */
    public static TriggerTimebase getInstance() {
        return TriggerTimebaseHolder.instance; // 返回单例
    }
    //endregion


    /**
     * 构造函数：初始化图片资源、注册事件观察者、订阅RxBus事件
     */
    public TriggerTimebase() {
        super(); // 调用父类构造函数
        resBmp = new Bitmap[6]; // 分配6个图片槽位
        resBmp[0] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time); // 选中态-中间图片
        resBmp[1] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time_l); // 选中态-左侧图片
        resBmp[2] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time_r); // 选中态-右侧图片

        resBmp[3] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time); // 未选中态-中间图片
        resBmp[4] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time_l); // 未选中态-左侧图片
        resBmp[5] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time_r); // 未选中态-右侧图片

        init(resBmp); // 初始化父类图片资源
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_POS, eventUIObserver); // 注册时间位置变化事件观察者
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerMainTopRightGone); // 订阅顶部右侧隐藏事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPCENTER_TEXT_GONE).subscribe(consumerMainTopRightGone); // 订阅顶部中央文本隐藏事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅SCPI命令转发到UI事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式切换事件
    }

    /**
     * 空初始化方法（预留扩展）
     */
    public void init() {
    }

    /**
     * 从缓存恢复触发时刻线位置
     */
    public void setCache() {
        setX_dis(getCacheForTimeBasePosition()); // 从缓存读取位置并设置（不带回算）
    }

    //x轴移动50%功能
    /**
     * 将触发时刻线重置到屏幕水平50%位置
     * 处理参考通道独立时基偏移的回算逻辑
     */
    public void rstX_50percentt() {
        CursorManage.getInstance().setCursorTrace(true); // 开启光标追踪

        int ch_idx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 读取参考通道时基模式（0=跟随，非0=独立）
        if(ChannelFactory.isRefCh(ch_idx) && temp != 0){ //独立情况 // 如果是参考通道且为独立时基模式
            setX(ScopeBase.getWidth() / 2 - ChannelFactory.getRefChannel(ch_idx).getTimePoseOfViewPix_original_nowScale()); // 减去参考通道原始偏移量
        }else{ // 普通通道或参考通道跟随模式
            setX(ScopeBase.getWidth() / 2); // 直接设置为屏幕宽度一半
        }

        CursorManage.setCursorByTimebaseTrace(); // 根据触发时刻线位置更新光标
        CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
    }

    /**
     * 带回算的设置timeBase的位置...
     * 设置触发时刻线X位置，同时同步水平轴位置并处理纠正回算
     * 根据当前通道类型（参考通道/数学FFT通道/普通通道）分别处理
     * @param x 触发时刻线的目标像素位置
     */
    @Override
    public void setX(long x) {
        long pix = x; // 实际设置像素值（可能因纠正而回算）
        int ch_idx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 读取参考通道时基模式
        if (ChannelFactory.isRefCh(ch_idx)) { // 当前激活的是参考通道
            RefChannel refChannel = ChannelFactory.getRefChannel(ch_idx); // 获取参考通道对象
            if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) { // 参考通道为FFT类型
                refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x); // 设置参考通道FFT水平偏移
                refChannel.correctTimePose();//某些情况下需要纠正 // 纠正时间位置
                pix = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();//因为可能被纠正，所以回算 // 回算纠正后的实际像素位置
            } else { // 参考通道非FFT类型
                if (temp ==0 ) { //跟随 // 参考通道跟随模式
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步水平轴位置
                    HorizontalAxis.getInstance().correctTimePose_poseMove(); //某些情况下需要纠正 // 纠正水平轴位置
                    pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); //因为可能被纠正，所以回算 // 回算纠正后的实际像素位置
                } else { // 参考通道独立时基模式
                    refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x); // 设置参考通道水平偏移
                    refChannel.correctTimePose();//某些情况下需要纠正 // 纠正时间位置
                    pix = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();//因为可能被纠正，所以回算 // 回算纠正后的实际像素位置
                }
            }
        } else { // 非参考通道
            if (ChannelFactory.isMath_FFT_Ch(ch_idx)) { // 数学FFT通道
                //数学FFT
                HorizontalAxisMath horizontalAxisFFT = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT(); // 获取数学通道FFT水平轴
                horizontalAxisFFT.setXPosOfView(ScopeBase.getWidth() / 2 - x); // 设置FFT水平轴位置
                horizontalAxisFFT.correctXPose(); // 纠正X位置
                pix = ScopeBase.getWidth() / 2 - horizontalAxisFFT.getXPosOfView(); // 回算纠正后的实际像素位置
            } else { // 普通通道
                HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步水平轴位置
                HorizontalAxis.getInstance().correctTimePose_poseMove(); //某些情况下需要纠正 // 纠正水平轴位置
                pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); //因为可能被纠正，所以回算 // 回算纠正后的实际像素位置
            }
        }

        setX_dis(pix); // 调用不带回算的设置方法，完成最终位置设置
    }

    /**
     * 不带回算的设置timeBase的位置...
     * 直接设置触发时刻线位置，同步所有关联通道的水平偏移，并持久化到缓存
     * @param x 触发时刻线的目标像素位置
     */
    public void setX_dis(long x) {

        int ch_idx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 读取参考通道时基模式
        RefChannel refChannel = ChannelFactory.getRefChannel(ch_idx); // 获取参考通道对象
        if(ChannelFactory.isRefCh(ch_idx)){ // 当前激活的是参考通道
            if (temp == 0 && refChannel.getRefType() != WaveData.FFT_WAVE) {//跟随 // 参考通道跟随模式且非FFT
                if (HorizontalAxis.getInstance().getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) { // 水平轴位置需要更新
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步水平轴位置
                }
                for (int i = ChannelFactory.REF1; i <= ChannelFactory.REF8; i++) { // 遍历所有参考通道
                    refChannel = ChannelFactory.getRefChannel(i); // 获取参考通道对象
                    if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE && refChannel.getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) { // 非FFT且位置不同
                        refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步参考通道水平偏移
                        putCacheForTimeBasePosition(x, i); // 持久化参考通道的触发位置缓存
                    }
                }
            } else { // 参考通道独立模式或FFT类型
                if (refChannel != null && refChannel.getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) { // 参考通道位置需要更新
                    refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x); // 设置参考通道水平偏移
                }
            }
        }else{ // 非参考通道
            if(ChannelFactory.isMath_FFT_Ch(ch_idx)){ // 数学FFT通道
                //数学FFT
                HorizontalAxisMath horizontalAxisFFT = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT(); // 获取数学通道FFT水平轴
                if (horizontalAxisFFT.getXPosOfView() != ScopeBase.getWidth() / 2 - x) { // FFT水平轴位置需要更新
                    horizontalAxisFFT.setXPosOfView(ScopeBase.getWidth() / 2 - x); // 设置FFT水平轴位置
                }
            } else { // 普通通道
                if (HorizontalAxis.getInstance().getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) { // 水平轴位置需要更新
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步水平轴位置
                }
                if (temp == 0) {//跟随 // 参考通道跟随模式
                    for (int i = ChannelFactory.REF1; i <= ChannelFactory.REF8; i++) { // 遍历所有参考通道
                        refChannel = ChannelFactory.getRefChannel(i); // 获取参考通道对象
                        if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE) { // 非FFT类型参考通道
                            refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x); // 同步参考通道水平偏移
                            putCacheForTimeBasePosition(x, i); // 持久化参考通道的触发位置缓存
                        }
                    }
                }
            }
        }

        super.setX(x); // 调用父类设置X位置（触发重绘和移动事件）

        //if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YT) // zoom 为啥不保存
        {
            putCacheForTimeBasePosition(x); // 持久化当前通道的触发位置缓存
        }
        mainWaveMsgTriggerTimeBase.setX(x); // 更新消息Bean中的位置值
        RxBus.getInstance().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, mainWaveMsgTriggerTimeBase); // 通过事件总线通知触发时刻线位置变化
        CursorManage.getInstance().timeBaseMove(); // 通知光标管理器触发时刻线已移动
    }

    /**
     * 从事件总线设置触发时刻线位置（不带回算）
     * 供外部事件驱动调用，内部委托给setX_dis
     * @param x 触发时刻线的目标像素位置
     */
    public void setX_disFromEventBus(long x) {
        setX_dis(x); // 委托给setX_dis处理
        //   if (!getVisible())
        //       return;
//        super.setXFromEventBus(x);
        //if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YT)  // zoom 为啥不保存
//        {
//            putCacheForTimeBasePosition(x);
//        }
//        mainWaveMsgTriggerTimeBase.setX(x);
//        RxBus.getInstance().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, mainWaveMsgTriggerTimeBase);
//        CursorManage.getInstance().timeBaseMove();
    }

    /**
     * 绘制激活状态的触发时刻线
     * 从缓存恢复位置并绘制，支持区分是否来自事件总线
     * @param isFromEventBus 是否来自事件总线（区分绘制方式）
     */
    public void drawActiveTrigTime(boolean isFromEventBus) {
        long x = getCacheForTimeBasePosition(); // 从缓存读取触发位置
        CursorManage.getInstance().timeBaseMove(); // 通知光标管理器触发时刻线移动
        if (!getVisible()) // 触发线不可见
            return; // 直接返回
        if (isFromEventBus) { // 来自事件总线
            setXFromEventBus(x); // 使用事件总线方式设置位置
        } else { // 非事件总线方式
            super.setX(x); // 直接设置位置
        }
    }

    /**
     * 持久化当前通道的触发时刻线位置到缓存
     * @param x 触发位置像素值
     */
    private void putCacheForTimeBasePosition(long x) {
        putCacheForTimeBasePosition(x, ChannelFactory.getChActivate()); // 委托给带通道索引的重载方法
    }

    /**
     * 持久化指定通道的触发时刻线位置到缓存
     * 根据通道类型（动态通道/数学通道/参考通道）分别存储到不同缓存键
     * @param x 触发位置像素值
     * @param chIDX 通道索引
     */
    public void putCacheForTimeBasePosition(long x, int chIDX) {
        //Logger.d(TAG, "putCacheForTimeBasePosition() called with: x = [" + x + "], chIDX = [" + chIDX + "]");
        if(ChannelFactory.isDynamicCh(chIDX) || ChannelFactory.isSerialCh(chIDX)){ // 动态通道或串行通道
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x)); // 存入普通通道位置缓存
        }else if(ChannelFactory.isMathCh(chIDX)){ // 数学通道
            if (ChannelFactory.getMathChannel(chIDX).getMathType() != MathWave.MATH_FFTWAVE) { // 数学通道非FFT类型
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x)); // 存入普通通道位置缓存
            } else { // 数学通道FFT类型
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_FFTMOD, String.valueOf(x)); // 存入FFT模式位置缓存
            }
        }else if(ChannelFactory.isRefCh(chIDX)){ // 参考通道
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.toUiChNo(chIDX), String.valueOf(x)); // 存入参考通道位置缓存（含通道号）
            int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 读取参考通道时基模式
            if (refTimeBaseIndex == 0) { // 跟随模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x)); // 同步存入普通通道位置缓存
            }
        }
    }

//    public long setXFromCurCh() {
//        long position = getCacheForTimeBasePosition();
//        setX(position);
//        return position;
//    }

    /**
     * 从缓存读取当前通道的触发时刻线位置
     * 根据当前激活通道类型从不同缓存键读取
     * @return 触发位置像素值
     */
    private long getCacheForTimeBasePosition() {
        long l; // 缓存值
        int curIdx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        if(ChannelFactory.isRefCh(curIdx)){ // 参考通道
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.toUiChNo(curIdx)); // 从参考通道缓存读取
        }else if(ChannelFactory.isMath_FFT_Ch(curIdx)){ // 数学FFT通道
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_FFTMOD); // 从FFT模式缓存读取
        }else { // 普通通道
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL); // 从普通通道缓存读取
        }

        Logger.d(TAG, "getCacheForTimeBasePosition() called ch:" + ChannelFactory.getChActivate() + "," + l); // 打印调试日志
        return l; // 返回缓存位置值
    }

    /**
     * 时间位置变化事件观察者
     * 当水平轴位置被外部修改时，同步更新触发时刻线位置
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 转换事件数据类型
            if (eventBase.getId() == EventFactory.EVENT_TIME_POS) { // 时间位置变化事件
                long pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 从水平轴反算触发时刻线位置
//                Logger.i(TAG, "eventUIObserver() ==>"
//                        +" pix:"+pix+" - "+getCacheForTimeBasePosition());
                if (pix != getCacheForTimeBasePosition()) { // 位置与缓存不一致时才更新
                    setX_disFromEventBus(pix); // 通过事件总线方式设置位置
                }
            }
        }
    };

    /**
     * 工作模式切换事件消费者
     * 切换工作模式后刷新触发时刻线显示
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            update(); // 刷新触发时刻线显示
        }
    };

    /**
     * 缓存加载事件消费者
     * 应用启动或场景切换时从缓存恢复触发时刻线位置
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache(); // 从缓存恢复触发时刻线位置
        }
    };

    /**
     * 顶部右侧隐藏事件消费者
     * 处理UI可见性变化时的触发时刻线显示逻辑
     */
    private Consumer<MainTopMsgRightGone> consumerMainTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone msgTopRightGone) throws Exception {
            boolean preVisible = getVisible(); // 获取之前可见状态
//            setVisible(msgTopRightGone.isVisible());
//            if (!preVisible && msgTopRightGone.isVisible()) {
//                setX_dis(getX());
//            }else if(preVisible && !msgTopRightGone.isVisible()){
//                Scope scope = Scope.getInstance();
//                if(scope.isRun() && scope.isInScrollMode()){
//                    setX(ScopeBase.getWidth() / 2);
//                }
//            }
        }
    };

    /**
     * SCPI命令转发到UI事件消费者
     * 处理远程命令设置触发位置、数学FFT位置、参考通道位置
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_TIMEBASE_POSITION: { // 普通触发时刻位置命令
                    double d = Double.valueOf(commandMsgToUI.getParam()); // 解析时间位置参数
                    long x = HorizontalAxis.getInstance().SCPIQueryPixInScreenFromTImePosVal(d); // 将时间值转换为屏幕像素偏移
                    CursorManage.getInstance().setCursorTrace(true); // 开启光标追踪
                    setX(GlobalVar.get().getMainWave().x / 2 - x); // 设置触发时刻线位置（屏幕中心减偏移）
                    CursorManage.setCursorByTimebaseTrace(); // 根据触发时刻线更新光标
                    CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFT_Position:{ // 数学FFT位置命令
                    String[] param= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 拆分参数（通道号+时间值）
                    int ch_idx=Integer.parseInt(param[0]); // 解析通道索引
                    double d = Double.parseDouble(param[1]); // 解析时间位置值
                    int activeCh=ChannelFactory.getChActivate(); // 保存当前激活通道
                    ChannelFactory.chActivate(ch_idx); // 切换到目标数学通道
                    long x = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT().SCPIQueryPixInScreenFromTImePosVal(d); // FFT时间值转像素偏移
//                    Logger.i(Command.TAG,"x:"+x+",d:"+d);
                    setX(GlobalVar.get().getMainWave().x / 2 - x); // 设置触发时刻线位置
                    ChannelFactory.chActivate(activeCh); // 恢复之前激活的通道
                }break;
                case CommandMsgToUI.FLAG_REF_Timebase_Position:{ // 参考通道触发时刻位置命令
                    String[] param= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 拆分参数（通道号+时间值）
                    int ch_idx=Integer.parseInt(param[0]); // 解析通道索引
                    double d = Double.valueOf(param[1]); // 解析时间位置值

                    int activeCh=ChannelFactory.getChActivate(); // 保存当前激活通道
                    ChannelFactory.chActivate(ch_idx); // 切换到目标参考通道
                    ChannelFactory.setRefActive(ch_idx); // 设置参考通道为激活状态

//                    Logger.i(Command.TAG,"perPix:"+ ChannelFactory.getRefChannel(ch_idx).getRefTimePerPix());
//                    Logger.i(Command.TAG,"scaleVal:"+ChannelFactory.getRefChannel(ch_idx).getRefTimeScaleVal());
                    long x=(long) (d/ChannelFactory.getRefChannel(ch_idx).getRefTimePerPix()); // 时间值除以每像素时间得到像素偏移

//                    long x = HorizontalAxis.getInstance().SCPIQueryPixInScreenFromTImePosVal(d);
                    setX(GlobalVar.get().getMainWave().x / 2 - x); // 设置触发时刻线位置
                    ChannelFactory.chActivate(activeCh); // 恢复之前激活的通道

                }break;
            }
        }
    };

    /**
     * 通过偏移量设置触发时刻线X位置
     * 限制数学FFT通道不超过屏幕中心
     * @param offsetX 偏移量（正数左移，负数右移）
     */
    public void setOffsetX(int offsetX) {
        CursorManage.getInstance().setCursorTrace(true); // 开启光标追踪

        if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate()) && (getX()-offsetX)>GlobalVar.get().getMainWave().x/2){ // 数学FFT通道且移动后超过屏幕中心
            offsetX=(int)(getX()-GlobalVar.get().getMainWave().x/2); // 限制偏移量，不超过屏幕中心
        }

        putCacheForTimeBasePosition(this.x - offsetX); // 预存缓存
        super.setOffsetX(offsetX); // 调用父类偏移设置
        CursorManage.setCursorByTimebaseTrace(); // 根据触发时刻线更新光标
        CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
    }

    /**
     * 通过像素增量移动触发时刻线
     * 限制数学FFT通道不超过屏幕中心
     * @param px 移动像素量（正数右移，负数左移）
     */
    @Override
    public void movePix(double px) {
        CursorManage.getInstance().setCursorTrace(true); // 开启光标追踪
        if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate()) && (getX()+px)>GlobalVar.get().getMainWave().x/2){ // 数学FFT通道且移动后超过屏幕中心
            px=(int)(GlobalVar.get().getMainWave().x/2-getX()); // 限制移动量，不超过屏幕中心
        }
        super.movePix(px); // 调用父类像素移动
        CursorManage.setCursorByTimebaseTrace(); // 根据触发时刻线更新光标
        CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
    }
    //    //region 属性
//    int x, y;
//    private int LineNameID = IWave.TriggerTime;
//
//    public int getLineNameID() {
//        return LineNameID;
//    }
//
//    public int getX() {
//        return x;
//    }
//
//
//    public void setX(int x) {
//        this.x = x;
//        RxBus.get().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, new MainWaveMsgTriggerTimeBase(x));
//        draw();
//    }
//
//    public void initX() {
//        setX(GlobalVar.get().getMainWave().x / 2);
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public void setY(int y) {
//        this.y = y;
//    }
//    //endregion
//
//    private static Bitmap bmp;
//    private Canvas mCanvas;
//    private Paint paint;
//    private boolean isChanageBitmap = false;
//
//
//    public TriggerTimebase() {
//        bmp = Bitmap.createBitmap(resBmpLeft.getWidth(), resBmpLeft.getHeight(), Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(bmp);
//        paint = new Paint();
//        x = GlobalVar.get().getMainWave().x / 2;
//        y = 0;
//        draw();
//    }
//
//    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmpLeft.getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, null);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth(), 0, null);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp.getWidth() / 2, 0, null);
//            }
//
//        }
//    }
//
//    public void draw(ICanvasGL canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmpLeft.getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, isChanageBitmap);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth(), 0, isChanageBitmap);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp.getWidth() / 2, 0, isChanageBitmap);
//            }
//            isChanageBitmap = false;
//        }
//    }
//
//    private void draw() {
//        synchronized (bmp) {
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            mCanvas.drawPaint(paint);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
//            if (this.x <= resBmpLeft.getWidth()) {
//                mCanvas.drawBitmap(resBmpLeft, 0, 0, paint);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                mCanvas.drawBitmap(resBmpRight, 0, 0, paint);
//            } else {
//                mCanvas.drawBitmap(resBmp, 0, 0, paint);
//            }
//
//            isChanageBitmap = true;
//        }
//
//    }
//
//    public void movePix() {
//        setX(x + 1);
//    }
//
//    public void subPixMove() {
//        setX(x - 1);
//    }

}
