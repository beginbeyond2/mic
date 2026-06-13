package com.micsig.tbook.tbookscope.wavezone;

import static com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand.MCUTOARM_SCREENCAPTURE;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.micsig.base.DoubleUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.surface.SlideFinger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.MiddleMain;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.bean.YTZoomMsgDisplay;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.display.RulerManage;
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2017/6/9.
 */

/**
 * 波形显示区域YT模式类，负责示波器YT模式下的OpenGL波形绘制和触摸交互，1
 */
public class WaveZoneDisplay_YT extends GLTextureView implements IWorkMode {
    /**
     * 日志标签，用于标识当前类的日志输出，1
     */
    private static final String TAG = "WaveZoneDisplay_YT";

    /**
     * Android上下文对象，用于访问应用资源和系统服务，1
     */
    private Context context;

    /**
     * 主视图容器，用于管理UI组件的层级结构，1
     */
    private ViewGroup mainViewGroup;

    /**
     * YT模式的缩放控制器，处理波形的缩放操作，1
     */
    private WaveZoneDisplay_YTZoom waveZoneDisplayYtZoom;

    /**
     * 强制刷新标志位，控制是否执行清屏重绘操作，1
     */
    private boolean isForceRefresh = false;

    /**
     * 设置YT模式的缩放控制器实例
     * @param waveZoneDisplayYtZoom YT模式缩放控制器对象，用于处理波形缩放逻辑，1
     */
    public void setWaveZoneDisplayYtZoom(WaveZoneDisplay_YTZoom waveZoneDisplayYtZoom) {
        this.waveZoneDisplayYtZoom = waveZoneDisplayYtZoom;
    }

    /**
     * 获取当前YT模式的缩放控制器实例
     * @return 返回YT模式缩放控制器对象，如果未设置则返回null，1
     */
    public WaveZoneDisplay_YTZoom getWaveZoneDisplayYtZoom() {
        return waveZoneDisplayYtZoom;
    }

    /**
     * 性能计时的开始时间戳，单位为毫秒，1
     */
    private long startTime, endTime, dt;

    /**
     * Android消息处理器，用于处理工作模式切换等异步消息，1
     */
    private Handler handler;

    /**
     * 构造方法，初始化波形显示区域的OpenGL环境和消息处理器
     * @param context Android上下文对象，用于访问系统资源和服务，1
     * @param mainViewGroup 主视图容器，用于管理UI组件的层级结构，1
     */
    public WaveZoneDisplay_YT(Context context, ViewGroup mainViewGroup) {
        super(context); // 调用父类GLTextureView的构造方法，1
        this.context = context; // 保存传入的上下文对象，1
        this.mainViewGroup = mainViewGroup; // 保存传入的主视图容器，1
        //this.setZOrderOnTop(true);
        this.setBackgroundColor(Color.TRANSPARENT); // 设置背景色为透明，1
        this.setOpaque(false); // 设置为非不透明，支持透明渲染，1

        startTime = System.currentTimeMillis(); // 记录启动时间戳，1

        handler = new Handler() { // 创建消息处理器实例，1
            @Override
            public void handleMessage(Message msg) { // 处理接收到的消息，1
                int w = WorkModeManage.getInstance().getmWorkMode(); // 获取当前工作模式，1
                switch (msg.what) { // 根据消息类型进行分支处理，1
                    case 0: // 消息类型为0时，1
                        WorkModeManage.getInstance().switchWorkMode(w); // 切换到指定工作模式，1
                        break;
                    case 1: // 消息类型为1时，1
                        break;
                }

                // super.handleMessage(msg);
            }
        };
    }

    //region IWorkMode interface
    /**
     * 当前工作模式标识，默认为YT模式，1
     */
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;

    /**
     * 切换工作模式，同步更新所有相关组件的工作状态
     * @param workMode 目标工作模式标识，定义在IWorkMode接口中，1
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        synchronized (this) { // 同步代码块，确保线程安全，1
            //if (mWorkMode == workMode) return;

            this.mWorkMode = workMode; // 更新当前工作模式，1
            //WorkModeManage.get().setmWorkMode(workMode);
            WorkModeManage.getInstance().setWorkModeManageState(WorkModeManage.WorkModeManage_BeginSwitchMode); // 设置工作模式管理状态为开始切换，1
            RulerManage.getIns().switchWorkMode(workMode); // 标尺管理器切换工作模式，1
            CursorManage.getInstance().switchWorkMode(workMode); // 光标管理器切换工作模式，1
            VoltageLineManage.getInstance().switchWorkMode(workMode); // 电压线管理器切换工作模式，1
            WaveManage.get().switchWorkMode(workMode); // 波形管理器切换工作模式，1
            SerialBusManage.getInstance().switchWorkMode(workMode); // 串行总线管理器切换工作模式，1
            MeasureManage.getInstance().switchWorkMode(workMode); // 测量管理器切换工作模式，1

            WaveGridManage.getInstance().switchWorkMode(workMode); // 波形网格管理器切换工作模式，1

            WorkModeManage.getInstance().setWorkModeManageState(WorkModeManage.WorkModeManage_Ready); // 设置工作模式管理状态为就绪，1
        }
    }

    //endregion

    /**
     * 强制绘制GL画布内容，同步执行所有波形组件的绘制操作
     */
    public void forceDrawGlCanvas() {
        queueEvent(new Runnable() { // 将绘制任务加入GL线程队列，1
            @Override
            public void run() { // 在线程中执行的run方法，1
                synchronized (WaveZoneDisplay_YT.this) { // 同步代码块，确保线程安全，1
                    WaveZoneDisplay_YT.this.mCanvas.clearBuffer(); // 清空画布缓冲区，1
                    WaveGridManage.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制波形网格，1
                    CursorManage.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制光标，1
                    MeasureManage.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制测量信息，1
                    TriggerTimebase.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制触发时基，1
                    VoltageLineManage.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制电压线，1
                    WaveManage.get().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制波形，1
                    SerialBusManage.getInstance().draw(WaveZoneDisplay_YT.this.mCanvas); // 绘制串行总线，1
                }
            }
        });
    }

    /**
     * 强制清屏操作，清除GL画布上的所有内容并设置为透明背景
     */
    public void forceClearGlCanvas() {
        this.isForceRefresh = true; // 设置强制刷新标志位，1
        queueEvent(new Runnable() { // 将清屏任务加入GL线程队列，1
            @Override
            public void run() { // 在线程中执行的run方法，1
                synchronized (this) { // 同步代码块，确保线程安全，1
                    WaveZoneDisplay_YT.this.mCanvas.clearBuffer(Color.TRANSPARENT); // 使用透明色清空画布缓冲区，1
                }
            }
        });
    }

    /**
     * 初始化OpenGL渲染环境，调用父类的初始化方法
     */
    @Override
    protected void init() {
        super.init(); // 调用父类GLTextureView的初始化方法，1
    }

    /**
     * OpenGL绘制回调方法，按顺序绘制所有波形组件
     * @param canvas OpenGL画布对象，用于执行绘制操作，1
     */
    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        if (this.isForceRefresh) { // 检查是否需要强制刷新，1
            mCanvas.clearBuffer(Color.TRANSPARENT); // 使用透明色清空画布，1
            this.isForceRefresh = false; // 重置强制刷新标志位，1
            return; // 直接返回，不执行后续绘制，1
        }

        //判断
        synchronized (this) { // 同步代码块，确保绘制操作的线程安全，1

            //绘制
            WaveGridManage.getInstance().draw(IWorkMode.WorkMode_YT, canvas); // 绘制YT模式的波形网格，1
            RulerManage.getIns().draw(canvas); // 绘制标尺，1
            CursorManage.getInstance().draw(canvas); // 绘制光标，1
            MeasureManage.getInstance().draw(canvas); // 绘制测量信息，1
            TriggerTimebase.getInstance().draw(canvas); // 绘制触发时基，1

            VoltageLineManage.getInstance().draw(canvas); // 绘制电压线，1
            SerialBusManage.getInstance().draw(canvas); // 绘制串行总线，1
            WaveManage.get().draw(canvas); // 绘制波形，1
        }

        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_WaveZoneDisplayManage, true); // 设置波形显示管理器的加载菜单状态为完成，1
    }

    //region onTouchEvent变量
    /**
     * 当前选中的线条索引号，-1表示未选中任何线条，1
     */
    int index = -1;    //线号

    /**
     * 上一次触摸事件的X和Y坐标值，用于计算移动距离，1
     */
    int oldX, oldY;    //上一个点的坐标

    /**
     * 是否选中了标签的标志位，控制标签相关的交互行为，1
     */
    boolean isSelectLabel = false;

    /**
     * 是否需要显示时间值提示的标志位，1
     */
    boolean needShowTValueTip = false;

    /**
     * 向一个方向滑动后，向另一个方向滑动就不管用。
     * 滑动方向标识，-1表示没有点击，1表示左右滑动，2表示上下滑动，1
     */
    int slideDirection = -1; // -1 为没有点击 1，左右，2上下

    /**
     * 触摸按下时的初始X和Y坐标值，用于计算滑动距离，1
     */
    int downX, downY;

    /**
     * 最小移动距离阈值，超过此值才认为是有效的滑动操作，1
     */
    private final static int MOVESIZE = 20;

    /**
     * 左右滑动方向的常量标识，值为1，1
     */
    public final static int MOVE_LEFTRIGHT = 1;

    /**
     * 上下滑动方向的常量标识，值为2，1
     */
    public final static int MOVE_UPDOWN = 2;

    /**
     * 精细调节按钮的文本显示控件引用，1
     */
    private TextView btnFineText;

    //多点触摸的记录

    //endregion

    /**
     * 上次点击的时间戳，用于检测双击操作，1
     */
    private long ClickTS = 0;

    /**
     * 上次点击的X坐标值，用于双击位置判断，1
     */
    private int X = 0;

    /**
     * 上次点击的Y坐标值，用于双击位置判断，1
     */
    private int Y = 0;

    /**
     * 处理双击事件，检测快速连续点击并触发相应操作
     * @param x 第一次点击的X坐标值，用于位置判断，1
     * @param y 第一次点击的Y坐标值，用于位置判断，1
     */
    private void doubleClick(int x, int y) {
        Logger.i(TAG, "doubleClick x= " + x + " ,y= " + y); // 记录双击事件的日志信息，1
        if (TChan.isSerial(index)) { // 检查是否为串行通道，1
            return; // 如果是串行通道则直接返回，不处理双击，1
        }
        long ts = SystemClock.elapsedRealtime(); // 获取当前系统运行时间戳，1

        if ((ts - ClickTS) < 400) { // 判断两次点击的时间间隔是否小于400毫秒，1
            if (Math.abs(x - X) < 50 && Math.abs(y - Y) < 50) { // 判断两次点击的位置距离是否小于50像素，1
                Scope.getInstance().doubleClicked(x); // 触发示波器的双击事件，1
            }
            ClickTS = 0; // 重置点击时间戳，1
        } else {
            ClickTS = ts; // 更新点击时间戳为当前时间，1
        }
        this.X = x; // 保存当前点击的X坐标，1
        this.Y = y; // 保存当前点击的Y坐标，1
    }



    /**
     * 单指滑动操作的实例管理器，处理单手指的滑动手势，1
     */
    SlideFinger slideFinger = SlideFinger.getInstance();

    /**
     * 双指操作监听器实例，处理双指的滑动和缩放手势，1
     */
    DoubleFinger doubleFinger = new DoubleFinger(new DoubleFinger.DoubleFingerlistener() {
        /**
         * 水平滑动回调方法，处理左右滑动手势
         * @param val 滑动的数值增量，正数向右滑动，负数向左滑动，1
         */
        @Override
        public void onHorizontalSlide(double val) {

            int v = (int) DoubleUtil.floor(val); // 将滑动值向下取整转换为整数，1
            if(slideDirection == -1){ // 检查滑动方向是否未设置，1
                slideDirection = MOVE_LEFTRIGHT; // 设置滑动方向为左右滑动，1
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向改变的事件，1
                if (TChan.isCursor6(index)==false) { // 检查是否不是六光标模式，1
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime); // 发送最后操作对象为触发时间的消息，1
                } else {
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                }
            }

            if(slideDirection == MOVE_LEFTRIGHT) { // 确认滑动方向为左右滑动，1

                switch (index) { // 根据当前选中的索引进行分支处理，1
                    case TChan.Cursor_col_1: // 列光标1，1
                    case TChan.Cursor_col_2: // 列光标2，1
                    case TChan.Cursor_col_3: // 列光标3，1
                    case TChan.Cursor_row_1: // 行光标1，1
                    case TChan.Cursor_row_2: // 行光标2，1
                    case TChan.Cursor_row_3: // 行光标3，1
                        CursorManage.getInstance().moveSelectCursor(v, 0); // 移动选中的光标，只在水平方向移动，1
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                        break;
                    default: // 默认情况，处理非光标对象，1
                        TriggerTimebase.getInstance().setOffsetX(v); // 设置触发时基的水平偏移量，1
                        double times = Tools.PixToTimebase((int) TriggerTimebase.getInstance().getX()); // 将像素坐标转换为时间值，1
                        TChan.foreachMath(mathChan -> { //TODO 数学多通道 ？
                            int mathIndex = TChan.toFpgaChNo(mathChan); // 将数学通道转换为FPGA通道号，1
                            Command.get().getMath_base().Offset(mathIndex, times, false); // 设置基础数学运算的偏移量，1
                            Command.get().getMath_fft().Offset(mathIndex, times, false); // 设置FFT数学运算的偏移量，1
                            Command.get().getMath_axb().Offset(mathIndex, times, false); // 设置axb数学运算的偏移量，1
                            Command.get().getMath_advanced().Offset(mathIndex, times, false); // 设置高级数学运算的偏移量，1
                        });
                        break;
                }
            }
        }

        /**
         * 水平缩放回调方法，处理水平方向的缩放手势（当前未实现）
         * @param y 垂直方向的坐标值，预留参数，1
         * @param v 缩放的数值增量，预留参数，1
         */
        @Override
        public void onHorizontalZoom(double y, double v) {

        }

        /**
         * 垂直滑动回调方法，处理上下滑动手势
         * @param val 滑动的数值增量，正数向上滑动，负数向下滑动，1
         */
        @Override
        public void onVerticalSlide(double val) {
            int v = (int) DoubleUtil.floor(val); // 将滑动值向下取整转换为整数，1
            if(slideDirection == -1){ // 检查滑动方向是否未设置，1
                slideDirection = MOVE_UPDOWN; // 设置滑动方向为上下滑动，1
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向改变的事件，1
                if (TChan.isCursor6(index)==false) { // 检查是否不是六光标模式，1
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1); // 发送最后操作对象为通道1的消息，1
                } else {
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                }
            }
            if(slideDirection == MOVE_UPDOWN){ // 确认滑动方向为上下滑动，1
                switch (index) { // 根据当前选中的索引进行分支处理，1
                    case TChan.Cursor_col_1: // 列光标1，1
                    case TChan.Cursor_col_2: // 列光标2，1
                    case TChan.Cursor_col_3: // 列光标3，1
                    case TChan.Cursor_row_1: // 行光标1，1
                    case TChan.Cursor_row_2: // 行光标2，1
                    case TChan.Cursor_row_3: // 行光标3，1
                        CursorManage.getInstance().moveSelectCursor(0, v); // 移动选中的光标，只在垂直方向移动，1
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                        break;
                    default: // 默认情况，处理非光标对象，1
                        if (TChan.isSerial(index)) { // 检查是否为串行通道，1
                            SerialBusManage.getInstance().setOffsetY(v); // 设置串行总线的垂直偏移量，1
                        } else {
                            WaveManage.get().setOffsetY(v); // 设置波形的垂直偏移量，1
                        }
                        break;
                }
            }
        }

        /**
         * 垂直缩放回调方法，处理垂直方向的电压缩放手势
         * @param y 垂直方向的坐标值，用于确定缩放中心点，1
         * @param v 缩放的数值增量，控制电压刻度的放大或缩小，1
         */
        @Override
        public void onVerticalZoom(double y, double v) {
            Scope.getInstance().Vfine(y,v); // 调用示波器的垂直精细调节方法，1
        }

        /**
         * 手势开始回调方法，在精细调节开始时调用
         */
        @Override
        public void onBegin() {
            Scope.getInstance().Sfine(); // 调用示波器开始精细调节的方法，1
        }

        /**
         * 手势结束回调方法，在精细调节结束时调用
         */
        @Override
        public void onEnd() {
            Scope.getInstance().Efine(); // 调用示波器结束精细调节的方法，1
        }
    });

    @Override
       /**
     * 触摸事件处理方法，处理各种手势操作包括滑动、缩放、光标选择等
     * @param event 触摸事件对象，包含触摸点坐标、动作类型等信息，1
     * @return 返回true表示事件已处理，false表示需要传递给父类处理，1
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) { // 检查是否为按下动作，1
            Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]"); // 记录触摸事件开始的日志，1
        }
        if (((MainViewGroup) mainViewGroup).isEnableWaveOption()==false) { // 检查波形选项是否被禁用，1
            return true; // 如果禁用则直接返回，不处理后续逻辑，1
        }
        if(doubleFinger.onDoubleFinger(event)){ // 处理双指手势操作，1

            return true; // 如果双指手势已处理则返回，1
        }
//        MeasureManage.getInstance().getMeasureItem().dealCursorItem(event);
        int x = (int) event.getX(); // 获取当前触摸点的X坐标，1
        int y = (int) event.getY(); // 获取当前触摸点的Y坐标，1
        int selectCh=WaveManage.get().getCurCh(); // 获取当前选中的通道索引，1
        switch (event.getAction() & MotionEvent.ACTION_MASK) { // 根据触摸动作类型进行分支处理，1
            case MotionEvent.ACTION_DOWN: { // 处理单指按下事件，1
                //初始化 变量

                downX = oldX = (int) event.getX(); // 记录按下时的X坐标作为初始值和旧值，1
                downY = oldY = (int) event.getY(); // 记录按下时的Y坐标作为初始值和旧值，1
                slideDirection = -1; // 重置滑动方向为未设置状态，1
                slideFinger.setSlideDirection(slideDirection); // 设置单指滑动器的滑动方向，1
                slideFinger.fingerDownXY(event.getX(),event.getY()); // 记录单指滑动器的按下坐标，1
                //  2023-9-7 光标选中功能是否屏蔽 根据主菜单光标选中项
                index = CursorManage.getInstance().selectCursor(x, y); // 尝试选中光标并获取索引，1
                if (index >= 0) { // 如果成功选中光标，1

                    CursorManage.getInstance().setSelectCursor(index); // 设置当前选中的光标，1
                    CursorManage.getInstance().setSelectHighColor(index); // 设置选中光标的高亮颜色，1
                    if (TChan.isCursorRow2(index)) { // 检查是否为行光标，1
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向的事件，1
                    } else {
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动方向的事件，1
                    }

                } else if (!Command.get().getMenu().ChannelSelectorQ() && (index = WaveManage.get().selectCursor(x, y)) >= 0) { // 如果未选中光标但选中了波形，1
                    if (selectCh!=WaveManage.get().getCurCh()) { // 检查选中的通道是否发生变化，1
                        this.postDelayed(new Runnable() { // 延迟执行任务，1
                            @Override
                            public void run() { // 在延迟后执行的run方法，1
                                CursorManage.getInstance().setCursorTrace(true); // 启用光标追踪，1
                                WaveManage.get().setSelectCursor(index); // 设置选中的波形光标，1
                                CursorManage.getInstance().setCursorTrace(false); // 禁用光标追踪，1
                                ChannelFactory.setRefActive(index - 1); // 设置激活的参考通道，1
//                            RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(index));
                                MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(index - 1)); // 设置活动通道，1
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向的事件，1
                            }
                        }, 1); // 延迟1毫秒执行，1
                    }
                } else if (Command.get().getMenu().ChannelSelectorQ()) { // 如果通道选择器处于激活状态，1
                    index = ChannelFactory.getInstance().getChActivate() + 1; // 获取当前激活通道的索引，1
                    WaveManage.get().setSelectCursor(index); // 设置选中的波形光标，1
                    ChannelFactory.setRefActive(index - 1); // 设置激活的参考通道，1
//                    RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(index));
                    MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(index-1)); // 设置活动通道，1
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向的事件，1
                }
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象的事件，1
                isSelectLabel = WaveManage.get().isLabelContains((int) event.getX(), (int) event.getY()); // 检查是否点击了标签，1
                if (MeasureManage.getInstance().isCursorTValueTrace()) { // 检查是否启用了时间值追踪，1
                    needShowTValueTip = true; // 设置需要显示时间值提示的标志位，1
                }
             }
            break;
            case MotionEvent.ACTION_MOVE: { // 处理手指移动事件，1

                if (isSelectLabel) { // 如果选中了标签，1
                    slideFinger.fingerMoveLabel(event.getX(), event.getY()); // 移动标签位置，1
                } else {
                    if (slideDirection == -1) { // 如果滑动方向未确定，1
                        int dValueY = Math.abs((int) event.getY() - downY); // 计算Y方向的移动距离，1
                        int dValueX = Math.abs((int) event.getX() - downX); // 计算X方向的移动距离，1

                        if (dValueY > MOVESIZE && (dValueY > dValueX)) { // 如果Y方向移动距离超过阈值且大于X方向，1
                            slideDirection = MOVE_UPDOWN; // 设置滑动方向为上下滑动，1
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向改变的事件，1
                            if (!TChan.isCursor6(index)) { // 检查是否不是六光标模式，1
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1); // 发送最后操作对象为通道1的消息，1
                            } else {
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                            }
                            slideFinger.setChIdx(index - 1); // 设置单指滑动器的通道索引，1
                            slideFinger.setSlideDirection(slideDirection); // 设置单指滑动器的滑动方向，1
                            slideFinger.fingerDownXY(oldX, oldY); // 重新记录单指滑动器的按下坐标，1
                        } else if (dValueX > MOVESIZE && (dValueX > dValueY)) { // 如果X方向移动距离超过阈值且大于Y方向，1
                            slideDirection = MOVE_LEFTRIGHT; // 设置滑动方向为左右滑动，1
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向改变的事件，1
                            if (!TChan.isCursor6(index)) { // 检查是否不是六光标模式，1
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime); // 发送最后操作对象为触发时间的消息，1
                            } else {
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                            }
                            slideFinger.setChIdx(index - 1); // 设置单指滑动器的通道索引，1
                            slideFinger.setSlideDirection(slideDirection); // 设置单指滑动器的滑动方向，1
                            slideFinger.fingerDownXY(oldX, oldY); // 重新记录单指滑动器的按下坐标，1
                        }

                    }
                    slideFinger.fingerMoveXY(event.getX(), event.getY()); // 更新单指滑动器的移动坐标，1

                    boolean fine = TBookUtil.isFine(); // 检查是否处于精细调节模式，1
                    int numFine = TBookUtil.getNumFine(); // 获取精细调节的倍数，1

                    if (event.getPointerCount() == 2) { // 如果是双指操作，1
                        dealCursor(slideDirection); // 处理光标相关逻辑，1
                    }
                    isCursor(index); // 检查当前索引是否为光标，1
                    switch (index) { // 根据当前选中的索引进行分支处理，1
                        case TChan.Cursor_col_1: // 列光标1，1
                        case TChan.Cursor_col_2: // 列光标2，1
                            if (!MeasureManage.getInstance().isCursorTValueTrace() || true) { // 检查是否未启用时间值追踪，1
                                cursorSingleMove(fine, numFine, event); // 执行光标单体移动，1
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                            } else {
                                if (needShowTValueTip) { // 如果需要显示时间值提示，1
                                    DToast.get().show(context.getResources().getString(R.string.measure_tvalue_trace)); // 显示时间值追踪的提示信息，1
                                    needShowTValueTip = false; // 重置提示标志位，1
                                }
                            }
                            break;
                        case TChan.Cursor_row_1: // 行光标1，1
                        case TChan.Cursor_row_2: // 行光标2，1
                            cursorSingleMove(fine, numFine, event); // 执行光标单体移动，1
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                            break;
                        case TChan.Cursor_col_3: // 列光标3，1
                        case TChan.Cursor_col_4: { // 列光标4，1
                            if (slideDirection == -1) break; // 如果滑动方向未设置则跳出，1
                            if (!MeasureManage.getInstance().isCursorTValueTrace() || true) { // 检查是否未启用时间值追踪，1
                                if ((slideDirection == MOVE_UPDOWN && (index == TChan.Cursor_col_4 || index == TChan.SingleRect)) // 如果是上下滑动且为特定光标，1
                                        || (slideDirection == MOVE_LEFTRIGHT && (index == TChan.Cursor_row_4 || index == TChan.SingleRect))) { // 如果是左右滑动且为特定光标，1
                                    int offsetX = oldX - (int) event.getX(); // 计算X方向的偏移量，1
                                    int offsetY = oldY - (int) event.getY(); // 计算Y方向的偏移量，1
                                    CursorManage.getInstance().MoveLabel(offsetX, offsetY); // 移动光标标签，1
                                } else {
                                    cursorLinkMove(fine, numFine, event, index); // 执行光标联动移动，1
                                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                                }
                            } else {
                                if (needShowTValueTip) { // 如果需要显示时间值提示，1
                                    DToast.get().show(context.getResources().getString(R.string.measure_tvalue_trace)); // 显示时间值追踪的提示信息，1
                                    needShowTValueTip = false; // 重置提示标志位，1
                                }
                            }
                            oldX = (int) event.getX(); // 更新旧X坐标，1
                            oldY = (int) event.getY(); // 更新旧Y坐标，1
                        }
                        break;
                        case TChan.Cursor_row_3: // 行光标3，1
                        case TChan.Cursor_row_4: // 行光标4，1
                        case TChan.Cursor_all: // 所有光标，1
                        case TChan.SingleRect:{ // 单矩形光标，1
                            if (slideDirection == -1) break; // 如果滑动方向未设置则跳出，1
                            if ((slideDirection == MOVE_UPDOWN && (index == TChan.Cursor_col_4 || index == TChan.SingleRect)) // 如果是上下滑动且为特定光标，1
                                    || (slideDirection == MOVE_LEFTRIGHT && (index == TChan.Cursor_row_4 || index == TChan.SingleRect))) { // 如果是左右滑动且为特定光标，1
                                int offsetX = oldX - (int) event.getX(); // 计算X方向的偏移量，1
                                int offsetY = oldY - (int) event.getY(); // 计算Y方向的偏移量，1
                                CursorManage.getInstance().MoveLabel(offsetX, offsetY); // 移动光标标签，1
                            } else {
                                cursorLinkMove(fine, numFine, event, index); // 执行光标联动移动，1
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象为当前索引的消息，1
                            }
                            oldX = (int) event.getX(); // 更新旧X坐标，1
                            oldY = (int) event.getY(); // 更新旧Y坐标，1
                        }
                        break;


                        default: { // 默认情况，处理非光标对象，1
                            int offsetX = oldX - (int) event.getX(); // 计算X方向的偏移量，1
                            int offsetY = oldY - (int) event.getY(); // 计算Y方向的偏移量，1
                            if (slideDirection == MOVE_LEFTRIGHT) { // 如果是左右滑动，1
                                if (fine) { // 如果处于精细调节模式，1
                                    offsetX = offsetX / numFine; // 将偏移量除以精细调节倍数，1
                                }

                                TriggerTimebase.getInstance().setOffsetX(offsetX); // 设置触发时基的水平偏移量，1

                                double times = Tools.PixToTimebase((int) TriggerTimebase.getInstance().getX()); // 将像素坐标转换为时间值，1
                                TChan.foreachMath(mathChan -> { //TODO 数学多通道 ？
                                    int mathIndex = TChan.toFpgaChNo(mathChan); // 将数学通道转换为FPGA通道号，1
                                    Command.get().getMath_base().Offset(mathIndex, times, false); // 设置基础数学运算的偏移量，1
                                    Command.get().getMath_fft().Offset(mathIndex, times, false); // 设置FFT数学运算的偏移量，1
                                    Command.get().getMath_axb().Offset(mathIndex, times, false); // 设置axb数学运算的偏移量，1
                                    Command.get().getMath_advanced().Offset(mathIndex, times, false); // 设置高级数学运算的偏移量，1
                                });
                                //zoom的界面也跟着移动
                            } else if (slideDirection == MOVE_UPDOWN) { // 如果是上下滑动，1
                                if (fine) { // 如果处于精细调节模式，1
                                    if (TChan.isSerial(index)) { // 检查是否为串行通道，1
                                        SerialBusManage.getInstance().setOffsetY(offsetY / numFine); // 设置串行总线的垂直偏移量（精细），1
                                    } else {
                                        WaveManage.get().setOffsetY(offsetY / numFine); // 设置波形的垂直偏移量（精细），1
                                    }
                                } else {
                                    if (TChan.isSerial(index)) { // 检查是否为串行通道，1
                                        SerialBusManage.getInstance().setOffsetY(offsetY); // 设置串行总线的垂直偏移量，1
                                    } else {
                                        int channelIndex = TChan.toFpgaChNo(index); // 将通道索引转换为FPGA通道号，1
                                        Channel channel = ChannelFactory.getDynamicChannel(channelIndex); // 获取动态通道对象，1
                                        if (GlobalVar.get().isEnableChannelZero()) { // 检查是否启用了通道零点功能，1

                                            channel.setZero(channel.getZero() + offsetY); // 更新通道的零点位置，1
                                            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + (channel.getChId() + 1) + channel.getVScaleId(), String.valueOf(channel.getZero())); // 保存通道零点位置到缓存，1
                                            //channel.setPos(channel.getChannelZeroPos() + offsetY);
                                            if (btnFineText == null) { // 如果精细调节文本控件未初始化，1
                                                btnFineText = (TextView) mainViewGroup.findViewById(R.id.fineText); // 查找并初始化精细调节文本控件，1
                                            }
                                            btnFineText.setText(TBookUtil.getD4FromD(channel.getMoveZeroVal())); // 更新精细调节文本显示，1
                                        } else {
                                            WaveManage.get().setOffsetY(offsetY); // 设置波形的垂直偏移量，1
//                                            WaveManage.get().setPositionY(channelIndex,offsetY);
                                        }
                                    }
                                }
                            }
                            if (fine) { // 如果处于精细调节模式，1
                                if (Math.abs(offsetX) > numFine) { // 如果X方向偏移量超过精细调节倍数，1
                                    oldX = (int) event.getX(); // 更新旧X坐标，1
                                }
                            } else {
                                oldX = (int) event.getX(); // 更新旧X坐标，1
                            }
                            if (fine) { // 如果处于精细调节模式，1
                                if (Math.abs(offsetY) > numFine) { // 如果Y方向偏移量超过精细调节倍数，1
                                    oldY = (int) event.getY(); // 更新旧Y坐标，1
                                }
                            } else {
                                oldY = (int) event.getY(); // 更新旧Y坐标，1
                            }
                        }
                    }
                    break;
                }

            }
            break;
            case MotionEvent.ACTION_UP: // 处理手指抬起事件，1
                if (!isSelectLabel) { // 如果未选中标签，1
                    switch (index) { // 根据当前选中的索引进行分支处理，1
                        case TChan.Cursor_col_1: // 列光标1，1
                        case TChan.Cursor_col_2: // 列光标2，1
                        case TChan.Cursor_col_3: // 列光标3，1
                        case TChan.Cursor_row_1: // 行光标1，1
                        case TChan.Cursor_row_2: // 行光标2，1
                        case TChan.Cursor_row_3: // 行光标3，1
                        case TChan.Cursor_col_4: // 列光标4，1
                        case TChan.Cursor_row_4: // 行光标4，1
                        case TChan.Cursor_all: // 所有光标，1
                            CursorManage.getInstance().moveFinish(); // 完成光标移动操作，1
                            break;
                    }
                    if (event.getActionIndex() == 0 && slideDirection == -1) { // 如果是第一个手指抬起且未滑动，1
                        doubleClick(x, y); // 处理双击事件，1
                    }
                    slideFinger.fingerUp(event.getX(), event.getY()); // 通知单指滑动器手指抬起，1
                    slideDirection = -1; // 重置滑动方向，1
                    slideFinger.setSlideDirection(slideDirection); // 设置单指滑动器的滑动方向，1
                }
                isSelectLabel = false; // 重置标签选中标志位，1
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 处理其他手指按下事件，1

                downX = oldX = (int) event.getX(0); // 记录第一个手指的X坐标，1
                downY = oldY = (int) event.getY(0); // 记录第一个手指的Y坐标，1
                break;
            case MotionEvent.ACTION_POINTER_UP: // 处理其他手指抬起事件，1

                int id = event.getActionIndex(); // 获取抬起的手指索引，1
                if (id == 0) { // 如果是第一个手指抬起，1
                    downX = oldX = (int) event.getX(1); // 更新为第二个手指的X坐标，1
                    downY = oldY = (int) event.getY(1); // 更新为第二个手指的Y坐标，1
                } else {
                    downX = oldX = (int) event.getX(0); // 更新为第一个手指的X坐标，1
                    downY = oldY = (int) event.getY(0); // 更新为第一个手指的Y坐标，1
                }

                if (TChan.isCursor4(index)) { // 检查是否为四光标模式，1
                    CursorManage.getInstance().setSelectCursor(index); // 设置选中的光标，1
                    if (TChan.isCursorRow2(index)) { // 检查是否为行光标，1
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向的事件，1
                    } else {
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动方向的事件，1
                    }
                } else if (index == TChan.Cursor_col_3 || index == TChan.Cursor_row_3) { // 如果是列光标3或行光标3，1
                    CursorManage.getInstance().setCursorTracking(index); // 设置光标追踪，1
                    if (index == TChan.Cursor_row_3) { // 如果是行光标3，1
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向的事件，1
                    } else {
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动方向的事件，1
                    }
                }
                if(event.getActionIndex() == 0 && slideDirection == -1) { // 如果是第一个手指抬起且未滑动，1
                    doubleClick(x, y); // 处理双击事件，1
                }
                break;
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN) { // 检查是否为按下动作，1
            Log.d(TAG, "onTouchEvent() called with: event = [" + event + "] end"); // 记录触摸事件结束的日志，1
        }
        return true; // 返回true表示事件已处理，1
        //return super.onTouchEvent(event);
    }

    /**
     * 检查当前索引是否为光标类型，并更新缓存状态
     * @param index 要检查的通道或光标索引，1
     * @return 返回true表示是光标类型，false表示不是光标类型，1
     */
    //补丁：按键
    private boolean isCursor(int index){
        if (TChan.isCursor8(index) ||  index== TChan.Cursor_all){ // 检查是否为八光标模式或所有光标，1
            CacheUtil.get().setLastObjectIsCursor(true); // 设置缓存中最后操作对象为光标，1
            return true; // 返回true表示是光标，1
        }
        CacheUtil.get().setLastObjectIsCursor(false); // 设置缓存中最后操作对象不是光标，1
        return false; // 返回false表示不是光标，1
    }

    /**
     * 光标单体滑动处理方法，根据精细调节模式移动单个光标
     * @param fine 是否处于精细调节模式，1
     * @param numFine 精细调节的倍数，1
     * @param event 触摸事件对象，用于获取当前坐标，1
     */
    //光标单体滑动
    private void cursorSingleMove(boolean fine,int numFine,MotionEvent event){
        int offsetX = oldX - (int) event.getX(); // 计算X方向的偏移量，1
        int offsetY = oldY - (int) event.getY(); // 计算Y方向的偏移量，1
        if (fine) { // 如果处于精细调节模式，1
            if (Math.abs(offsetX) > numFine) { // 如果X方向偏移量超过精细调节倍数，1
                CursorManage.getInstance().moveSelectCursor(offsetX / numFine, offsetY / numFine); // 移动选中的光标（精细），1
                oldX = (int) event.getX(); // 更新旧X坐标，1
            }
            if (Math.abs(offsetY) > numFine) { // 如果Y方向偏移量超过精细调节倍数，1
                CursorManage.getInstance().moveSelectCursor(offsetX / numFine, offsetY / numFine); // 移动选中的光标（精细），1
                oldY = (int) event.getY(); // 更新旧Y坐标，1
            }
        } else {
            CursorManage.getInstance().moveSelectCursor(offsetX, offsetY); // 移动选中的光标（普通），1
            oldX = (int) event.getX(); // 更新旧X坐标，1
            oldY = (int) event.getY(); // 更新旧Y坐标，1
        }
    }

    /**
     * 光标联动滑动处理方法，根据精细调节模式移动多个关联光标
     * @param fine 是否处于精细调节模式，1
     * @param numFine 精细调节的倍数，1
     * @param event 触摸事件对象，用于获取当前坐标，1
     * @param cursorIdx 当前光标的索引，1
     */
    //光标联动滑动
    private void cursorLinkMove(boolean fine,int numFine,MotionEvent event,int cursorIdx){
        int offsetX = oldX - (int) event.getX(); // 计算X方向的偏移量，1
        int offsetY = oldY - (int) event.getY(); // 计算Y方向的偏移量，1
        if (fine) { // 如果处于精细调节模式，1
            if (Math.abs(offsetX) > numFine) { // 如果X方向偏移量超过精细调节倍数，1
                CursorManage.getInstance().moveMultiSelectCursor(offsetX / numFine, offsetY / numFine,cursorIdx); // 移动多个选中的光标（精细），1
                oldX = (int) event.getX(); // 更新旧X坐标，1
            }
            if (Math.abs(offsetY) > numFine) { // 如果Y方向偏移量超过精细调节倍数，1
                CursorManage.getInstance().moveMultiSelectCursor(offsetX / numFine, offsetY / numFine,cursorIdx); // 移动多个选中的光标（精细），1
                oldY = (int) event.getY(); // 更新旧Y坐标，1
            }
        } else {
            CursorManage.getInstance().moveMultiSelectCursor(offsetX, offsetY,cursorIdx); // 移动多个选中的光标（普通），1
            oldX = (int) event.getX(); // 更新旧X坐标，1
            oldY = (int) event.getY(); // 更新旧Y坐标，1
        }
    }

    /**
     * ZOOM界面是否打开。
     *
     * @param event
     * @return 三个状态。-1： 不处理，0：显示 1：不显示 other::处理动画
     */
    private int isShowZoom(MotionEvent event) {
        if (3 == event.getPointerCount()) { // 检查是否为三指操作，1
            int tem = (int) event.getY(2) - downY; // 计算第三个手指的Y坐标与初始Y坐标的差值，1
            if (tem > 100) { // 如果差值大于100，1
                return 0; // 返回0表示显示ZOOM界面，1
            } else if (tem < -100) { // 如果差值小于-100，1
                return 1; // 返回1表示隐藏ZOOM界面，1
            } else {
                return tem; // 返回差值用于处理动画，1
            }
        }
        return -1; // 返回-1表示不处理，1
    }

    /**
     * 处理光标相关逻辑，根据滑动方向自动选择对应的光标
     * @param slideDirection 当前的滑动方向，1
     * @return 返回true表示已处理光标选择，false表示未处理，1
     */
    private boolean dealCursor( int slideDirection) {

        if (slideDirection == MOVE_LEFTRIGHT) { // 如果是左右滑动，1
            int i = CursorManage.getInstance().getCurrSelectCursor(); // 获取当前选中的光标索引，1
            if (i > 0 && CursorManage.getInstance().getColVisible()) { // 如果有选中光标且列光标可见，1
                index = TChan.Cursor_col_3; // 设置索引为列光标3，1
                CursorManage.getInstance().setCursorTracking(index); // 设置光标追踪，1
                return true; // 返回true表示已处理，1
            }
        } else if (slideDirection == MOVE_UPDOWN) { // 如果是上下滑动，1
            int i = CursorManage.getInstance().getCurrSelectCursor(); // 获取当前选中的光标索引，1
            if (i > 0 && CursorManage.getInstance().getRowVisible()) { // 如果有选中光标且行光标可见，1
                index = TChan.Cursor_row_3; // 设置索引为行光标3，1
                CursorManage.getInstance().setCursorTracking(index); // 设置光标追踪，1
                return true; // 返回true表示已处理，1
            }
        }

        return false; // 返回false表示未处理，1
    }

    /**
     * 处理ZOOM界面的显示和隐藏逻辑
     * @param event 触摸事件对象，用于判断手势，1
     * @return 返回true表示已处理ZOOM逻辑，false表示未处理，1
     */
    //处理ZOOM
    private boolean dealZoom(MotionEvent event) {
        int state = isShowZoom(event); // 获取ZOOM界面状态，1
        if (state == 0) { // 如果状态为0，表示需要显示ZOOM，1
            Logger.i("more touch zoom state:" + state); // 记录日志信息，1
//            WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_YTZOOM);
            RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, new YTZoomMsgDisplay(true)); // 发送显示ZOOM界面的事件，1
            return true; // 返回true表示已处理，1
        } else if (state == 1) { // 如果状态为1，表示需要隐藏ZOOM，1
            Logger.i("more touch zoom state:" + state); // 记录日志信息，1
            RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, new YTZoomMsgDisplay(false)); // 发送隐藏ZOOM界面的事件，1
//            WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_YT);
            return true; // 返回true表示已处理，1
        } else if (state > 1 && state <= 100) { // 如果状态在1到100之间，1
            Logger.i("more touch zoom state:" + state); // 记录日志信息，1
            return true; // 返回true表示已处理动画，1
        } else if (state >= -100 && state < -1) { // 如果状态在-100到-1之间，1
            Logger.i("more touch zoom state:" + state); // 记录日志信息，1
            return true; // 返回true表示已处理动画，1
        }
        return false; // 返回false表示未处理，1
    }

    /**
     * 处理截屏手势，四指向下滑动触发截屏
     * @param event 触摸事件对象，用于判断手势，1
     * @return 返回true表示已触发截屏，false表示未触发，1
     */
    private boolean dealScreenCapture(MotionEvent event) {
        if (4 == event.getPointerCount()) { // 检查是否为四指操作，1
            int tem = Math.abs((int) event.getY(2) - downY); // 计算第三个手指的Y坐标与初始Y坐标的差值绝对值，1
            if (tem > 100) { // 如果差值大于100，1
                RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_SCREENCAPTURE); // 发送截屏命令到ARM，1
                return true; // 返回true表示已触发截屏，1
            }
        }
        return false; // 返回false表示未触发截屏，1
    }
}

