package com.micsig.tbook.tbookscope.main.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by yangj on 2017/6/27.
 */

/*
 * +===========================================================================+
 * |                        MainDialogMenuHalf                                 |
 * |                          半屏菜单对话框组件                                 |
 * +===========================================================================+
 * | 模块定位: 示波器半屏快捷菜单对话框                                          |
 * | 核心职责: 提供时基、触发电平、光标和通道位置的快捷设置按钮                     |
 * | 架构设计: 继承RelativeLayout，采用RxJava事件驱动，响应外部命令                |
 * | 数据流向: 外部命令/点击按钮 -> MainDialogMenuHalf -> 各功能管理类            |
 * | 依赖关系: Context, RxBus, WaveManage, TriggerTimebase, CursorManage        |
 * | 使用场景: 用户点击屏幕50%按钮位置时弹出，提供快速操作入口                     |
 * +===========================================================================+
 */
public class MainDialogMenuHalf extends RelativeLayout {
    private static final String TAG = "MainDialogMenuHalf";  // 日志标签

    private Context context;  // 应用上下文
    private OnDismissListener onDismissListener;  // 关闭回调监听

    private Button timeBase, level, cursorH, cursorV;  // 时基、电平、水平光标、垂直光标按钮
    private Button ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8;  // 各通道按钮

    private ViewGroup rootViewGroup;  // 根视图容器
    private Rect rectBtnPer50;  // 50%按钮区域矩形

    private final int channelCount = GlobalVar.get().getChannelsCount();  // 通道总数

    /**
     * 设置50%按钮区域矩形
     * @param rectBtnPer50 按钮区域矩形
     */
    public void setRectBtnPer50(Rect rectBtnPer50){
        this.rectBtnPer50=rectBtnPer50;  // 保存按钮区域
    }

    /**
     * 关闭回调监听接口
     */
    public interface OnDismissListener {
        void onDismiss();  // 关闭回调方法
    }

    /**
     * 设置关闭回调监听
     * @param onDismissListener 关闭回调监听
     */
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;  // 保存回调监听
    }

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public MainDialogMenuHalf(Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public MainDialogMenuHalf(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图和控制逻辑
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainDialogMenuHalf(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        initView();  // 初始化视图
        initControl();  // 初始化控制
    }

    /**
     * 初始化视图组件
     * 加载布局并绑定按钮事件
     */
    private void initView() {
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {  // 8通道型号
            rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_menuhalf_eight, this);  // 加载8通道布局
        } else {  // 非8通道型号
            rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_menuhalf_four, this);  // 加载4通道布局
        }
        findViewById(R.id.menuhalf_outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (rectBtnPer50!=null && rectBtnPer50.contains((int)event.getX(),(int)event.getY())){  // 触摸点在50%按钮区域
                    return false;  // 不拦截，传递事件
                }
                hide();  // 隐藏菜单
                return false;  // 不拦截事件
            }
        });

        timeBase = (Button) rootViewGroup.findViewById(R.id.menuhalf_timeBase);  // 获取时基按钮
        level = (Button) rootViewGroup.findViewById(R.id.menuhalf_level);  // 获取电平按钮
        cursorH = (Button) rootViewGroup.findViewById(R.id.menuhalf_cursorH);  // 获取水平光标按钮
        cursorV = (Button) rootViewGroup.findViewById(R.id.menuhalf_cursorV);  // 获取垂直光标按钮
        ch1 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch1);  // 获取CH1按钮
        ch2 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch2);  // 获取CH2按钮
        ch3 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch3);  // 获取CH3按钮
        ch4 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch4);  // 获取CH4按钮
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {  // 8通道型号
            ch5 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch5);  // 获取CH5按钮
            ch6 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch6);  // 获取CH6按钮
            ch7 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch7);  // 获取CH7按钮
            ch8 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch8);  // 获取CH8按钮
        }

        // 设置按钮点击监听
        timeBase.setOnClickListener(onClickListener);  // 设置时基按钮监听
        level.setOnClickListener(onClickListener);  // 设置电平按钮监听
        cursorH.setOnClickListener(onClickListener);  // 设置水平光标按钮监听
        cursorV.setOnClickListener(onClickListener);  // 设置垂直光标按钮监听
        ch1.setOnClickListener(onClickListener);  // 设置CH1按钮监听
        ch2.setOnClickListener(onClickListener);  // 设置CH2按钮监听
        ch3.setOnClickListener(onClickListener);  // 设置CH3按钮监听
        ch4.setOnClickListener(onClickListener);  // 设置CH4按钮监听
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {  // 8通道型号
            ch5.setOnClickListener(onClickListener);  // 设置CH5按钮监听
            ch6.setOnClickListener(onClickListener);  // 设置CH6按钮监听
            ch7.setOnClickListener(onClickListener);  // 设置CH7按钮监听
            ch8.setOnClickListener(onClickListener);  // 设置CH8按钮监听
        }

        // 根据通道数量设置按钮可见性
        if (channelCount != GlobalVar.CHANNEL_COUNT_8) {  // 非8通道型号
            if (channelCount == GlobalVar.CHANNEL_COUNT_2) {  // 2通道型号
                ch3.setVisibility(View.INVISIBLE);  // 隐藏CH3按钮
                ch4.setVisibility(View.INVISIBLE);  // 隐藏CH4按钮
            } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) {  // 4通道型号
                ch3.setVisibility(View.VISIBLE);  // 显示CH3按钮
                ch4.setVisibility(View.VISIBLE);  // 显示CH4按钮
            }
        }
    }

    /**
     * 初始化事件控制
     * 订阅外部命令消息
     */
    public void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);  // 订阅外部命令事件
    }



    /**
     * 显示半屏菜单
     * 根据工作模式设置按钮可用状态
     */
    public void show() {
        if (WorkModeManage.getInstance().isXyMode()){  // XY模式
            timeBase.setEnabled(false);  // 禁用时基按钮
            level.setEnabled(false);  // 禁用电平按钮
            cursorH.setEnabled(false);  // 禁用水平光标按钮
            cursorV.setEnabled(false);  // 禁用垂直光标按钮
        }else{  // 非XY模式
            timeBase.setEnabled(true);  // 启用时基按钮
            level.setEnabled(true);  // 启用电平按钮
            cursorH.setEnabled(true);  // 启用水平光标按钮
            cursorV.setEnabled(true);  // 启用垂直光标按钮
        }
        setVisibility(VISIBLE);  // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MENUHALF);  // 发送对话框打开事件
        Tools.PrintControlsLocation("MainDialogMenuHalf",rootViewGroup);  // 打印控件位置信息
    }

    /**
     * 隐藏半屏菜单
     */
    public void hide() {
        if (onDismissListener != null) {  // 检查回调监听
            onDismissListener.onDismiss();  // 执行关闭回调
        }
        setVisibility(GONE);  // 设置隐藏
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MENUHALF);  // 发送对话框关闭事件
    }

    /**
     * 按钮点击事件监听器
     * 处理各按钮点击并执行对应功能
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();  // 播放按钮音效
            Logger.i("MainDialogMenuHalf");  // 记录日志
            Screen.getViewLocation(v);  // 获取按钮位置
            switch (v.getId()) {  // 根据按钮ID分发处理
                case R.id.menuhalf_timeBase:  // 时基按钮
                    TriggerTimebase.getInstance().rstX_50percentt();  // 复位时基到50%
                    break;
                case R.id.menuhalf_level:  // 电平按钮
                    ExternalKeysCommand.get().clickTriggerLevelCenter();  // 触发电平居中
                    break;
                case R.id.menuhalf_cursorH:  // 水平光标按钮
                    CursorManage.getInstance().initCursorY();  // 初始化水平光标
                    break;
                case R.id.menuhalf_cursorV:  // 垂直光标按钮
                    CursorManage.getInstance().initCursorX();  // 初始化垂直光标
                    break;
                case R.id.menuhalf_ch1:  // CH1按钮
                    WaveManage.get().setCenterChY(TChan.Ch1);  // CH1位置居中
                    break;
                case R.id.menuhalf_ch2:  // CH2按钮
                    WaveManage.get().setCenterChY(TChan.Ch2);  // CH2位置居中
                    break;
                case R.id.menuhalf_ch3:  // CH3按钮
                    WaveManage.get().setCenterChY(TChan.Ch3);  // CH3位置居中
                    break;
                case R.id.menuhalf_ch4:  // CH4按钮
                    WaveManage.get().setCenterChY(TChan.Ch4);  // CH4位置居中
                    break;
                case R.id.menuhalf_ch5:  // CH5按钮
                    WaveManage.get().setCenterChY(TChan.Ch5);  // CH5位置居中
                    break;
                case R.id.menuhalf_ch6:  // CH6按钮
                    WaveManage.get().setCenterChY(TChan.Ch6);  // CH6位置居中
                    break;
                case R.id.menuhalf_ch7:  // CH7按钮
                    WaveManage.get().setCenterChY(TChan.Ch7);  // CH7位置居中
                    break;
                case R.id.menuhalf_ch8:  // CH8按钮
                    WaveManage.get().setCenterChY(TChan.Ch8);  // CH8位置居中
                    break;
            }
            hide();  // 隐藏菜单
        }
    };

    /**
     * 外部命令消费者
     * 接收外部命令并执行对应功能
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {  // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_MENU_HALF_CHANNEL: {  // 通道位置居中命令
                    //将通道位置设置为垂直零点位置（波形显示区垂直中心)
                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());  // 解析通道索引
                    WaveManage.get().setCenterChY(chIndex);  // 通道位置居中
                }
                break;
                case CommandMsgToUI.FLAG_MENU_TRIGPOS: {  // 触发位置复位命令
//                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());
                    TriggerTimebase.getInstance().rstX_50percentt();  // 复位时基到50%
                }
                break;
                case CommandMsgToUI.FLAG_MENU_XCURSOR: {  // 垂直光标初始化命令
                    CursorManage.getInstance().initCursorX();  // 初始化垂直光标
                }
                break;
                case CommandMsgToUI.FLAG_MENU_YCURSOR: {  // 水平光标初始化命令
                    CursorManage.getInstance().initCursorY();  // 初始化水平光标
                }
                break;
                case CommandMsgToUI.FLAG_MENU_LEVEL: {  // 触发电平居中命令
                    //将触发电平设置为触发信号幅值的中间位置
//                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());
                    ExternalKeysCommand.get().clickTriggerLevelCenter();  // 触发电平居中
                }
                break;

            }
        }
    };
}