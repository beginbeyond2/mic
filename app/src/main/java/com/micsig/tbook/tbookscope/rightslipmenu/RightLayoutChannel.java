package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;                                                                // SuppressLint注解支持
import android.content.Context;                                                                        // 上下文环境
import android.content.res.TypedArray;                                                                 // XML自定义属性数组
import android.os.Handler;                                                                             // Handler消息机制
import android.os.Message;                                                                             // Handler消息对象
import android.util.AttributeSet;                                                                      // XML属性集
import android.util.Log;                                                                               // 日志工具
import android.view.View;                                                                              // 视图基类
import android.view.ViewGroup;                                                                         // 视图容器基类
import android.widget.Button;                                                                          // 按钮控件
import android.widget.ImageView;                                                                       // 图片视图控件
import android.widget.RelativeLayout;                                                                  // 相对布局
import android.widget.TextView;                                                                        // 文本视图控件

import androidx.constraintlayout.widget.ConstraintLayout;                                              // 约束布局

import com.micsig.base.Logger;                                                                         // 日志工具类
import com.micsig.tbook.scope.Calibrate.ProbeCalibrate;                                               // 探头校准管理器
import com.micsig.tbook.scope.Display.Display;                                                         // 显示模式管理
import com.micsig.tbook.scope.Event.EventBase;                                                        // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                                     // 事件工厂（观察者模式管理）
import com.micsig.tbook.scope.Event.EventUIObserver;                                                  // 事件UI观察者接口
import com.micsig.tbook.scope.Scope;                                                                   // 示波器核心控制
import com.micsig.tbook.scope.ScopeBase;                                                               // 示波器基础参数
import com.micsig.tbook.scope.channel.Channel;                                                        // 通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory;                                                 // 通道工厂（创建/获取通道实例）
import com.micsig.tbook.scope.probe.BaseProbe;                                                        // 探头基类
import com.micsig.tbook.scope.probe.ProbeNotifyInfo;                                                  // 探头通知信息Bean
import com.micsig.tbook.scope.probe.ProbeUpgradeInfo;                                                 // 探头升级信息Bean
import com.micsig.tbook.scope.vertical.VerticalAxis;                                                  // 垂直轴参数管理
import com.micsig.tbook.tbookscope.GlobalVar;                                                         // 全局变量
import com.micsig.tbook.tbookscope.LoadCache;                                                         // 缓存加载标记类
import com.micsig.tbook.tbookscope.MainActivity;                                                      // 主Activity
import com.micsig.tbook.tbookscope.MainViewGroup;                                                     // 主视图容器
import com.micsig.tbook.tbookscope.R;                                                                 // 资源ID
import com.micsig.tbook.tbookscope.config.ScopeConfig;                                                // 示波器配置
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;                                              // 确认对话框
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;                               // 右侧通道开关消息
import com.micsig.tbook.tbookscope.middleware.MiddleMain;                                             // 中间层主控制器
import com.micsig.tbook.tbookscope.middleware.command.Command;                                        // 命令发送器（向FPGA/底层）
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;                                 // 命令到UI的消息Bean
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;                                             // 消息队列枚举
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChOpenClose;                                  // 通道开关消息
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBandWidthHz;                            // 带宽频率选择对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;                           // 通道标签输入对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeInterface;                         // 探头接口信息对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeMultiple;                          // 探头倍率选择对话框
import com.micsig.tbook.tbookscope.rxjava.RxBus;                                                     // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;                                             // RxBus注册器
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                                                    // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound;                                                   // 按键音效播放
import com.micsig.tbook.tbookscope.tools.ScreenControls;                                              // 屏幕锁屏/进度控制
import com.micsig.tbook.tbookscope.tools.Tools;                                                       // 通用工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;                // 浮动键盘输入对话框
import com.micsig.tbook.tbookscope.util.App;                                                          // 应用工具类
import com.micsig.tbook.tbookscope.util.CacheUtil;                                                   // 缓存工具（参数持久化）
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;                                                // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;                                             // 工作模式Bean
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;                                           // 工作模式管理器
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;                                     // 光标管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;                                          // 波形管理器
import com.micsig.tbook.ui.MSwitchBox;                                                                // 自定义开关控件
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;                                             // 右侧菜单选择项Bean
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;                                             // 自定义选择视图控件
import com.micsig.tbook.ui.top.view.TopViewEdit;                                                      // 顶部可编辑文本控件
import com.micsig.tbook.ui.util.ScreenUtil;                                                           // 屏幕工具类
import com.micsig.tbook.ui.util.StrUtil;                                                              // 字符串工具类
import com.micsig.tbook.ui.util.TBookUtil;                                                            // 示波器单位转换工具
import com.micsig.tbook.ui.wavezone.IChan;                                                            // 通道接口
import com.micsig.tbook.ui.wavezone.TChan;                                                            // 通道常量（Ch1-Ch8等编号映射）

import java.lang.ref.WeakReference;                                                                   // 弱引用（防止Handler内存泄漏）
import java.util.HashMap;                                                                             // 哈希映射表
import java.util.List;                                                                                // 列表接口
import java.util.Observable;                                                                          // 被观察者基类
import java.util.Observer;                                                                            // 观察者接口

import io.reactivex.rxjava3.annotations.NonNull;                                                      // 非空注解
import io.reactivex.rxjava3.functions.Consumer;                                                       // RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        RightLayoutChannel                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 模拟通道参数设置面板                                  ║
 * ║ 核心职责：管理单个模拟通道的全部参数设置界面，包括通道开关、反相、微调、         ║
 * ║          耦合方式、探头类型/倍率、带宽、垂直基准、阻抗、标签、延迟、             ║
 * ║          偏移、位置、微调幅度等，并处理探头自动识别和UI更新                     ║
 * ║ 架构设计：继承RelativeLayout的自定义视图，通过XML属性channelNumber(1-8)区分     ║
 * ║          不同通道实例。采用RxBus订阅外部事件（约10个）+ EventFactory            ║
 * ║          观察者（约16个事件ID）的双重事件驱动架构。内部包含MyHandler弱引用       ║
 * ║          Handler用于延迟恢复按钮背景，Consumer/EventUIObserver处理UI同步        ║
 * ║ 数据流向：UI操作 → onItemClick/onToggleStateChanged → Command → FPGA          ║
 * ║          UI操作 → RightMsgChannel → RxBus → 订阅方                           ║
 * ║          外部事件 → RxBus/EventUIObserver → UI更新 → Command/RightMsgChannel  ║
 * ║          探头事件 → EventUIObserver → updateProbeUI → 探头布局切换/参数更新     ║
 * ║ 依赖关系：RxBus, EventFactory, CacheUtil, Command, ChannelFactory,            ║
 * ║           BaseProbe/ProbeCalibrate, WorkModeManage, ScreenControls,           ║
 * ║           TBookUtil, TChan, RightMsgChannel, 各种Dialog对话框                 ║
 * ║ 使用场景：用户在右侧滑出菜单中设置模拟通道参数时显示此面板，每个通道一个实例    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/9.
 */

/**
 * 模拟通道参数设置面板
 * <p>
 * 继承RelativeLayout，提供单个模拟通道的全部参数设置界面，包括：
 * - 通道开关(btnchCheck)、反相(btnInvert)、微调开关(btnFineSwitch)
 * - 耦合方式(rgCouple): DC/AC/GND
 * - 探头类型(rgProbeType): 电压/电流
 * - 探头倍率(probeMultiple): 1X/10X/100X等
 * - 带宽限制(rgBandWidth): Full/200M/20M/Highpass/Lowpass
 * - 垂直基准(rgVerBase): 垂直模式选择
 * - 阻抗(rgImped): 1MΩ/50Ω
 * - 标签(chLabel)、延迟(chDelay)、偏移(chOffset)、位置(chPosition)、微调幅度(chFineExtent)
 * - 探头自动识别(MSP/MDP/MRCP/MOIP)和UI动态切换
 * </p>
 * <p>每个通道实例通过XML属性channelNumber(1-8)区分，支持8通道独立参数设置。</p>
 */
public class RightLayoutChannel extends RelativeLayout {
    /** 日志标签 */
    private static final String TAG = "RightLayoutChannel";

    /** 上下文引用 */
    private Context context;
    /** 通道开关按钮 */
    private MSwitchBox btnchCheck;
    /** 反相开关按钮 */
    private MSwitchBox btnInvert;
    /** 微调开关按钮 */
    private MSwitchBox btnFineSwitch;
    /** 耦合方式选择控件（DC/AC/GND） */
    private RightViewSelect rgCouple;
    /** 探头类型选择控件（电压/电流） */
    private RightViewSelect rgProbeType;
    /** 带宽限制选择控件（Full/200M/20M/Highpass/Lowpass） */
    private RightViewSelect rgBandWidth;
    /** 垂直基准选择控件 */
    private RightViewSelect rgVerBase;
    /** 阻抗选择控件（1MΩ/50Ω） */
    private RightViewSelect rgImped;
    /** 通道标题文本（如"Ch1"） */
    private TextView channelVisibleHead;
    /** 探头倍率显示文本（如"10X"） */
    private TextView probeMultiple;
    /** 带宽编辑文本（Highpass/Lowpass时的具体频率） */
    private TextView bandWidthEdit;
    /** 探头型号文本 */
    private TextView probeTypeModel;
    /** 探头序列号文本 */
    private TextView probeTypeSN;
    /** 探头倍率文本（MSP探头专用） */
    private TextView probeTypeX;
    /** 探头校准按钮文本 */
    private TextView probeTypeCall;
    /** 通道标签编辑控件 */
    private TopViewEdit chLabel;
    /** 通道延迟编辑控件 */
    private TopViewEdit chDelay;
    /** 通道偏移编辑控件 */
    private TopViewEdit chOffset;
    /** 通道位置编辑控件 */
    private TopViewEdit chPosition;
    /** 微调幅度编辑控件 */
    private TopViewEdit chFineExtent;
    /** 探头倍率选择对话框 */
    private DialogProbeMultiple dialogProbeMultiple;
    /** 带宽频率选择对话框 */
    private DialogBandWidthHz dialogBandWidthHz;
    /** 通道标签输入对话框 */
    private DialogChannelLabel dialogChannelLabel;
    /** 探头接口信息对话框 */
    private DialogProbeInterface dialogProbeInterface;
    /** 浮动键盘输入对话框 */
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    /** 确认对话框（用于探头校准/升级结果提示） */
    private DialogOk dialogOk;
    /** 档位上调节按钮 */
    private Button btnTop;
    /** 档位下调节按钮 */
    private Button btnBottom;
    /** 通道面板背景图片 */
    private ImageView ivBackground;
    /** 反相指示灯图片 */
    private ImageView ivInvertLight;
    /** 弱引用Handler，用于延迟恢复按钮背景 */
    private MyHandler myHandler;

    /**
     * value 1-8
     * TChan.Ch1
     * TChan.Ch2
     * TChan.Ch3
     * TChan.Ch4
     * TChan.Ch5
     * TChan.Ch6
     * TChan.Ch7
     * TChan.Ch8
     */
    /** 当前通道编号（1-8，对应TChan.Ch1-Ch8） */
    private int channelNumber;
    /** 通道消息封装对象，用于通过RxBus传递参数变更 */
    private RightMsgChannel msgChannel;

    /** 前一次探头倍率值（用于计算倍率变化比例） */
    private String preProbeMul;
    /** 根视图容器 */
    private ViewGroup rootViewGroup;

    /** MDP700探头专用布局 */
    private ConstraintLayout mdp700Layout;
    /** 通用探头布局（无智能探头时显示） */
    private ConstraintLayout commonLayout;
    /** MSP500探头专用布局 */
    private ConstraintLayout msp500Layout;
    /** MRCP探头专用布局 */
    private ConstraintLayout mrcpLayout;
    /** MDP700探头倍率选择控件 */
    private RightViewSelect mdp700Select;
    /** MDP700探头信息按钮 */
    private Button mdp700Info;
    /** MDP700探头型号文本 */
    private TextView mdp700Model;
    /** MRCP探头倍率文本 */
    private TextView mrcpProbeTypeX;
    /** MRCP探头校准按钮文本 */
    private TextView mrcpProbeTypeCall;
    /** MRCP探头模式文本 */
    private TextView mrcpMode;

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public RightLayoutChannel(Context context) {
        this(context, null);                                                                            // 委托给两参数构造
    }

    /**
     * 两参数构造方法
     * @param context 上下文
     * @param attrs XML属性集
     */
    public RightLayoutChannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                                        // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终初始化入口）
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                                            // 调用父类构造
        this.context = context;                                                                         // 保存上下文引用
        initView(attrs, defStyleAttr);                                                                  // 初始化视图控件
        initControl();                                                                                  // 初始化事件订阅
        myHandler = new MyHandler(RightLayoutChannel.this);                                             // 创建弱引用Handler
    }

    /** 位置字符串（用于位置编辑控件的文本缓存） */
    public String positionString;

    /**
     * 初始化视图控件
     * <p>加载布局文件、绑定控件引用、设置监听器、初始化数据</p>
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootViewGroup = (ViewGroup) View.inflate(context, R.layout.layout_right_channel, this);         // 填充通道布局文件

        setClickable(true);                                                                             // 设置可点击（拦截触摸事件）
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutChannel);          // 获取自定义属性
        channelNumber = ta.getInt(R.styleable.RightLayoutChannel_channelNumber, TChan.Ch1);             // 读取通道编号，默认Ch1
        ta.recycle();                                                                                   // 回收TypedArray
        RelativeLayout chanelLayout = (RelativeLayout) findViewById(R.id.chanelLayout);                 // 通道布局容器
        channelVisibleHead = (TextView) findViewById(R.id.channelVisibleHead);                          // 通道标题
        btnchCheck = (MSwitchBox) findViewById(R.id.channelVisibleDetail);                              // 通道开关
        btnInvert = (MSwitchBox) findViewById(R.id.invertDetail);                                       // 反相开关
        btnFineSwitch=(MSwitchBox)findViewById(R.id.chFineSwitch);                                      // 微调开关
        rgCouple = (RightViewSelect) findViewById(R.id.ch1Couple);                                      // 耦合方式
        rgProbeType = (RightViewSelect) findViewById(R.id.ch1ProbeType);                                // 探头类型
        rgBandWidth = (RightViewSelect) findViewById(R.id.bandWidth);                                   // 带宽限制
        rgVerBase = (RightViewSelect) findViewById(R.id.chVerticalBaseDetail);                          // 垂直基准
        rgImped = (RightViewSelect) findViewById(R.id.chImpedDetail);                                   // 阻抗
        chLabel = (TopViewEdit) findViewById(R.id.chLabel);                                             // 通道标签
        chDelay = (TopViewEdit) findViewById(R.id.chDelay);                                             // 通道延迟
        chPosition = (TopViewEdit) findViewById(R.id.chPosition);                                       // 通道位置
        chOffset = (TopViewEdit) findViewById(R.id.chOffset);                                           // 通道偏移
        chFineExtent=(TopViewEdit)findViewById(R.id.chFineExtent);                                      // 微调幅度
        btnTop = (Button) findViewById(R.id.btnTop);                                                    // 档位上调节按钮
        btnBottom = (Button) findViewById(R.id.btnBottom);                                              // 档位下调节按钮
        ivBackground = findViewById(R.id.img_back_src);                                                 // 面板背景图
        ivInvertLight = findViewById(R.id.img_invert_light);                                            // 反相指示灯

        probeMultiple = (TextView) findViewById(R.id.probeMultiple);                                    // 探头倍率文本
        bandWidthEdit = (TextView) findViewById(R.id.bandWidthEdit);                                    // 带宽编辑文本
        dialogProbeMultiple = (DialogProbeMultiple) ((MainActivity) context).findViewById(R.id.dialogProbeMultiple);     // 探头倍率对话框
        dialogBandWidthHz = (DialogBandWidthHz) ((MainActivity) context).findViewById(R.id.dialogBandWidthHz);           // 带宽频率对话框

        dialogOk = (DialogOk) ((MainActivity) context).findViewById(R.id.dialogOk);                     // 确认对话框
        dialogProbeInterface = (DialogProbeInterface) ((MainActivity) context).findViewById(R.id.dialogProbeInterface);  // 探头接口对话框


        probeTypeModel = findViewById(R.id.probeTypeModel);                                             // 探头型号
        probeTypeSN = findViewById(R.id.probeTypeSN);                                                   // 探头序列号
        probeTypeX = findViewById(R.id.probeTypeX);                                                     // 探头倍率（MSP）
        probeTypeCall = findViewById(R.id.probeTypeCall);                                               // 探头校准按钮
        mrcpMode=findViewById(R.id.mrcpMode);                                                           // MRCP探头模式

        btnchCheck.setOnToggleStateChangedListener(onToggleStateChangedListener);                       // 通道开关键听
        btnInvert.setOnToggleStateChangedListener(onToggleStateChangedListener);                        // 反相开关监听
        btnFineSwitch.setOnToggleStateChangedListener(onToggleStateChangedListener);                    // 微调开关监听
        rgCouple.setOnItemClickListener(onItemClickListener);                                           // 耦合方式选择监听
        rgProbeType.setOnItemClickListener(onItemClickListener);                                        // 探头类型选择监听
        probeMultiple.setOnClickListener(onClickListener);                                              // 探头倍率点击监听
        rgBandWidth.setOnItemClickListener(onItemClickListener);                                        // 带宽选择监听
        bandWidthEdit.setOnClickListener(onClickListener);                                              // 带宽编辑点击监听
        rgVerBase.setOnItemClickListener(onItemClickListener);                                          // 垂直基准选择监听
        rgImped.setOnItemClickListener(onItemClickListener);                                            // 阻抗选择监听
        chLabel.setOnClickEditListener(onClickEditListener);                                            // 标签编辑监听
        chDelay.setOnClickEditListener(onClickEditListener);                                            // 延迟编辑监听
        chPosition.setOnClickEditListener(onClickEditListener);                                         // 位置编辑监听
        chOffset.setOnClickEditListener(onClickEditListener);                                           // 偏移编辑监听
        chFineExtent.setOnClickEditListener(onClickEditListener);                                       // 微调幅度编辑监听
        btnTop.setOnClickListener(onClickListener);                                                     // 档位上调节监听
        btnBottom.setOnClickListener(onClickListener);                                                  // 档位下调节监听

        probeTypeCall.setOnClickListener(onClickListener);                                              // 探头校准按钮监听

        bandWidthEdit.setEnabled(false);                                                                // 默认禁用带宽编辑（Full/200M/20M时不可编辑）

        chanelLayout.setBackgroundColor(getResources().getColor(R.color.frame_color));                   // 设置通道布局背景色

        commonLayout=findViewById(R.id.commonLayout);                                                   // 通用探头布局
        msp500Layout=findViewById(R.id.msp500Layout);                                                   // MSP500探头布局
        mrcpLayout=findViewById(R.id.mrcpLayout);                                                       // MRCP探头布局

        mdp700Layout =findViewById(R.id.mdp700layout);                                                  // MDP700探头布局
        mdp700Select =findViewById(R.id.mdp700ProbeSelect);                                             // MDP700探头倍率选择
        mdp700Model=findViewById(R.id.mdp700Model);                                                     // MDP700探头型号
        mdp700Info =findViewById(R.id.mdp700Info);                                                      // MDP700探头信息按钮
        mdp700Select.setOnItemClickListener(onItemClickListener);                                       // MDP700倍率选择监听
        mdp700Info.setOnClickListener(onClickListener);                                                 // MDP700信息按钮监听

        mrcpProbeTypeX=findViewById(R.id.mrcpProbeTypeX);                                               // MRCP探头倍率
        mrcpProbeTypeCall=findViewById(R.id.mrcpProbeTypeCall);                                         // MRCP探头校准按钮
        mrcpProbeTypeCall.setOnClickListener(onClickListener);                                          // MRCP校准按钮监听

        initData();                                                                                     // 初始化消息数据
        msp500Layout.setVisibility(GONE);                                                               // 隐藏MSP500布局
        mdp700Layout.setVisibility(GONE);                                                               // 隐藏MDP700布局
        mrcpLayout.setVisibility(GONE);                                                                 // 隐藏MRCP布局

        channelVisibleHead.setText("Ch" + channelNumber);                                               // 设置通道标题文本
        setControlColorByChIdx(channelNumber);                                                          // 根据通道索引设置控件颜色
    }

    /**
     * 根据通道索引设置控件颜色
     * <p>将所有开关和选择控件的颜色设置为对应通道的主题色</p>
     * @param chIdx 通道索引（1-8）
     */
    private void setControlColorByChIdx(int chIdx){
        btnchCheck.setControlColorByChIdx(chIdx);                                                      // 通道开关颜色
        btnInvert.setControlColorByChIdx(chIdx);                                                       // 反相开关颜色
        btnFineSwitch.setControlColorByChIdx(chIdx);                                                   // 微调开关颜色
        rgCouple.setControlColorByChIdx(chIdx);                                                        // 耦合方式颜色
        rgProbeType.setControlColorByChIdx(chIdx);                                                     // 探头类型颜色
        rgBandWidth.setControlColorByChIdx(chIdx);                                                     // 带宽颜色
        rgVerBase.setControlColorByChIdx(chIdx);                                                       // 垂直基准颜色
        rgImped.setControlColorByChIdx(chIdx);                                                         // 阻抗颜色
        mdp700Select.setControlColorByChIdx(chIdx);                                                    // MDP700倍率颜色
    }

    /**
     * 初始化事件订阅
     * <p>订阅RxBus事件（约10个）和EventFactory观察者（约16个事件ID），
     * 注册通道活动变化回调和鼠标点击事件</p>
     */
    private void initControl() {

        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);                                 // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx);                             // 订阅扩展缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);                       // 订阅通道开关消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);                    // 订阅工作模式切换
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);                                  // 订阅命令到UI消息
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL_BANDWIDTH).subscribe(consumerChannelBandWidth);              // 订阅带宽变更
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_VERNIER).subscribe(consumerExternalkeysVernier);                    // 订阅外部旋钮微调
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CLOSE).subscribe(onDialogClose);                                         // 订阅对话框关闭
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale);                // 订阅垂直档位调整
        RxBus.getInstance().getObservable(RxEnum.MSG_CHANNEL_SLIP_POSITION).subscribe(consumerChannelPosition);                  // 订阅通道位置消息

        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_RESISTANCETYPE, eventUIObserver);                               // 监听阻抗类型变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_BANDWIDTH, eventUIObserver);                                    // 监听带宽变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_INVERT, eventUIObserver);                                       // 监听反相变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_COUPLE, eventUIObserver);                                       // 监听耦合方式变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE, eventUIObserver);                                       // 监听垂直档位变更
        EventFactory.addEventObserver(EventFactory.EVENT_VERTICAL_MODE, eventUIObserver);                                        // 监听垂直模式变更
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_START, eventUIObserver);                                           // 监听自动设置开始
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver);                                            // 监听自动设置结束
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE, eventObserver);                                           // 监听显示模式变更
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_EVENT, eventUIObserver);                                          // 监听探头事件
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,OnChannelVscalUserChange);                          // 监听用户手动垂直档位变更
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_ZERO, eventUIObserver);                                           // 监听探头校零
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_UPGRADE, eventUIObserver);                                        // 监听探头升级
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_CALIBRATE_END, eventUIObserver);                                  // 监听探头校准结束
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_ALARM, eventUIObserver);                                          // 监听探头报警
//        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OFFSET,eventUIObserver);                                        // 监听偏移变更

        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_PLUG,this::OnProbePlug);                                          // 监听探头插入
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_UNPLUG,this::OnProbeUnPlug);                                      // 监听探头拔出

        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);                            // 注册通道活动变化回调
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick);                     // 订阅鼠标点击位置
    }




    /**
     * 初始化消息数据
     * <p>创建RightMsgChannel对象，设置各参数的初始值</p>
     */
    private void initData() {
        msgChannel = new RightMsgChannel();                                                             // 创建通道消息对象
        msgChannel.setChannelNumber(channelNumber);                                                     // 设置通道编号
        msgChannel.setChCheck(true);                                                                    // 默认通道开启
        boolean invert = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber);    // 从缓存读取反相状态
        msgChannel.setInvert(invert);                                                                   // 设置反相状态
        msgChannel.setCouple(rgCouple.getSelectItem());                                                 // 设置耦合方式
//        msgChannel.setImped(rgCouple.getSelectItem());//阻抗设置放到耦合方式里
        msgChannel.setProbeType(rgProbeType.getSelectItem());                                           // 设置探头类型
        msgChannel.setProbeMultiple(probeMultiple.getText().toString());                                // 设置探头倍率
        msgChannel.setBandWidth(rgBandWidth.getSelectItem());                                           // 设置带宽
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());                                // 设置带宽编辑值
        msgChannel.setImped(rgImped.getSelectItem());                                                   // 设置阻抗
        msgChannel.setLabel("");                                                                        // 标签初始为空
        msgChannel.setDelay("");                                                                        // 延迟初始为空
        msgChannel.setFineExtent("");                                                                   // 微调幅度初始为空
        msgChannel.setFineSwitch(false);                                                                // 微调开关默认关闭
        setInvertLight(invert);                                                                         // 设置反相指示灯
    }

    /**
     * 设置反相指示灯
     * <p>根据反相状态显示/隐藏指示灯，并根据通道编号设置对应颜色的指示灯图片</p>
     * @param invert 是否反相
     */
    private void setInvertLight(boolean invert) {
        ivInvertLight.setVisibility(invert ? View.VISIBLE : View.INVISIBLE);                           // 根据反相状态控制可见性
        switch (channelNumber) {
            case TChan.Ch1:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch1_light);                            // Ch1指示灯
                break;
            case TChan.Ch2:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch2_light);                            // Ch2指示灯
                break;
            case TChan.Ch3:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch3_light);                            // Ch3指示灯
                break;
            case TChan.Ch4:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch4_light);                            // Ch4指示灯
                break;
            case TChan.Ch5:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch5_light);                            // Ch5指示灯
                break;
            case TChan.Ch6:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch6_light);                            // Ch6指示灯
                break;
            case TChan.Ch7:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch7_light);                            // Ch7指示灯
                break;
            case TChan.Ch8:
                ivInvertLight.setImageResource(R.drawable.ic_bg_ch8_light);                            // Ch8指示灯
                break;
            default:
                ivInvertLight.setBackground(null);                                                     // 默认无背景
                break;
        }
    }

    /**
     * 扩展缓存恢复
     * <p>仅恢复垂直基准和阻抗参数，用于部分参数更新场景</p>
     */
    private void setCacheEx() {
        int verBase = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE + channelNumber);    // 读取垂直基准缓存
        int imped = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);             // 读取阻抗缓存
        this.rgVerBase.setSelectIndex(verBase);                                                        // 设置垂直基准选中项
        this.rgImped.setSelectIndex(imped);                                                            // 设置阻抗选中项
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
        if (channel == null) return;                                                                    // 通道为空则返回
        channel.setVerticalMode(verBase);                                                               // 设置通道垂直模式

        String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);// 读取微调幅度缓存
        channel.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));                                    // 设置通道VScale值

        updateProbeUI(channel);                                                                        // 更新探头UI

    }

    /** 标识是否正在加载缓存（加载期间屏蔽某些事件响应） */
    boolean isLoadCache=false;

    /**
     * 主缓存恢复
     * <p>从CacheUtil读取全部通道参数缓存，恢复UI状态，发送Command到FPGA，
     * 更新Channel数据模型，发送RightMsgChannel通知订阅方</p>
     */
    private void setCache() {
        isLoadCache=true;                                                                               // 标记正在加载缓存
//        setProbeTypeLayout();

        boolean enableHighLowFilter = isEnableHighLowFilter();                                          // 是否启用高通/低通滤波器

        boolean invert = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber);    // 反相状态
        int couple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);            // 耦合方式索引
        int probeType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);    // 探头类型索引
        String probeMultiple = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);   // 探头倍率
        int bandWidth = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);     // 带宽索引
        String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);   // 高通频率
        String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);    // 低通频率
        int verBase = 1;                                                                                // 垂直基准默认值
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber);       // 通道标签
        String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber);       // 通道延迟
        String offset = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber);     // 通道偏移
//        String position = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_POSITION +channelNumber);
        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);         // 阻抗索引
        String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);// 微调幅度
        fineExtent+=probeType==0?"V":"A";                                                              // 添加单位后缀
        boolean fineSwitch= CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber);   // 微调开关

//        offset=offset.replace("A","").replace("V","").replace(" ","");
        preProbeMul = probeMultiple;                                                                    // 保存前次探头倍率

        this.btnInvert.setState(invert);                                                                // 设置反相开关状态
        this.rgCouple.setSelectIndex(couple);                                                          // 设置耦合方式选中项
        this.rgProbeType.setSelectIndex(probeType);                                                    // 设置探头类型选中项
        this.probeMultiple.setText(probeMultiple);                                                     // 设置探头倍率文本
        this.rgVerBase.setSelectIndex(verBase);                                                        // 设置垂直基准选中项
        this.rgImped.setSelectIndex(impedance);                                                        // 设置阻抗选中项
        chLabel.setText(label);                                                                         // 设置标签文本
        chDelay.setText(delay);                                                                         // 设置延迟文本
//        chOffset.setText(offset);
//        chOffset.setText(position);
        btnFineSwitch.setState(fineSwitch);                                                             // 设置微调开关状态
        chFineExtent.setText(fineExtent);                                                               // 设置微调幅度文本
//        chFineExtent.setEnabled(fineSwitch);
        if (!enableHighLowFilter) {                                                                     // 未启用高通/低通滤波
            if (bandWidth == 3 || bandWidth == 4) {                                                    // 如果缓存是highpass/lowpass
                this.rgBandWidth.setSelectIndex(0);                                                    // 重置为Full
            }
            this.rgBandWidth.setEnabled(3, false);                                                     // 禁用highpass选项
            this.rgBandWidth.setEnabled(4, false);                                                     // 禁用lowpass选项
        } else {
            this.rgBandWidth.setSelectIndex(bandWidth);                                                // 设置带宽选中项
        }
        this.bandWidthEdit.setEnabled(bandWidth == 3 || bandWidth == 4);                               // highpass/lowpass时启用编辑
        this.bandWidthEdit.setText(this.bandWidthEdit.isEnabled() ? (bandWidth == 3 ? bandWidthHighEdit : bandWidthLowEdit) : "");   // 设置带宽编辑文本

        boolean channelVisible = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + channelNumber);    // 通道开关状态
        btnchCheck.setState(channelVisible);                                                            // 设置通道开关状态


        Command.get().getChannel().Display(TChan.toFpgaChNo(channelNumber), channelVisible, false);    // 发送通道显示命令
        Command.get().getChannel().Inverse(TChan.toFpgaChNo(channelNumber), invert, false);            // 发送反相命令
        Command.get().getChannel().Couple(TChan.toFpgaChNo(channelNumber), couple, false);             // 发送耦合方式命令
        Command.get().getChannel().Prty(TChan.toFpgaChNo(channelNumber), probeType, false);            // 发送探头类型命令
        Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), (float) TBookUtil.getDoubleFromX(probeMultiple), false);    // 发送探头倍率命令
        if (bandWidth == 1) {                                                                           // 200M带宽
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000), false);   // 发送带宽命令
        } else if (bandWidth == 2) {                                                                    // 20M带宽
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000), false);    // 发送带宽命令
        } else {                                                                                        // 其他带宽
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), bandWidth, (float) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000), false);   // 发送带宽命令
        }
        Command.get().getChannel().Vref(TChan.toFpgaChNo(channelNumber), verBase, false);              // 发送垂直基准命令
        Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), impedance, false);        // 发送阻抗命令
        Command.get().getChannel().Vernier(TChan.toFpgaChNo(channelNumber),fineSwitch,false);          // 发送微调开关命令
        double _offset= TBookUtil.getDoubleFromM(offset.replace("A", "").replace("V", "").replace(" ", ""));   // 解析偏移值
        Command.get().getChannel().Offset(TChan.toFpgaChNo(channelNumber),_offset,false);              // 发送偏移命令

        double _position= TBookUtil.getDoubleFromM(positionString.replace("A", "").replace("V", "").replace(" ", ""));  // 解析位置值
        Command.get().getChannel().Position(TChan.toFpgaChNo(channelNumber),_position,false);          // 发送位置命令

        double _delay=TBookUtil.getDoubleFromM(delay.replace("s", ""))  + 0.1;                        // 解析延迟值
        Command.get().getChannel().Delay(TChan.toFpgaChNo(channelNumber),_delay,false);                // 发送延迟命令
        Command.get().getChannel().Label(TChan.toFpgaChNo(channelNumber), label, false);               // 发送标签命令

        WaveManage.get().setChannelLabel(channelNumber, label);                                        // 设置波形标签
        setChannelLabel(TChan.toFpgaChNo(channelNumber), label);                                       // 设置通道标签


        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
        if (channel == null) return;                                                                    // 通道为空则返回
        channel.setVerticalMode(verBase);                                                               // 设置垂直模式

        channel.setInvert(invert);                                                                      // 设置反相
        channel.setCoupleType(matchCouple(couple));                                                     // 设置耦合类型
        channel.setProbeType(probeType);                                                                // 设置探头类型
        channel.setProbeRate(TBookUtil.getDoubleFromX(probeMultiple));                                  // 设置探头倍率
        channel.setResistanceType(impedance);                                                           // 设置阻抗
        channel.setVerticalMode(verBase);                                                               // 设置垂直模式
        fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber);       // 重新读取微调幅度
        channel.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));                                    // 设置VScale值
        switch (bandWidth) {                                                                            // 根据带宽索引设置带宽类型
            case 0:                                                                                     // Full
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, Channel.getMaxBandWidth());       // 设置全带宽
                break;
            case 1:                                                                                     // 200M
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, (TBookUtil.getMHzFromHz("200" + TBookUtil.UNIT_MHZ) * 1000 * 1000));   // 设置200M带宽
                break;
            case 2:                                                                                     // 20M
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, (TBookUtil.getMHzFromHz("20" + TBookUtil.UNIT_MHZ) * 1000 * 1000));     // 设置20M带宽
                break;
            case 3:                                                                                     // Highpass
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000));         // 设置高通带宽
                break;
            case 4:                                                                                     // Lowpass
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000));           // 设置低通带宽
                break;
            default:
                break;
        }

//        channel.setPos(TBookUtil.getDoubleFromM(positionString.replace("A","").replace("V","").replace(" ","")));
        channel.setChOffsetVal(TBookUtil.getDoubleFromM(offset.replace("A","").replace("V","").replace(" ","")));      // 设置偏移值
        channel.setDelay((int)(TBookUtil.getDoubleFromM(delay.replace("s", "")) * 1e12 + 0.1));        // 设置延迟值（皮秒）
        boolean xy = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;          // 是否XY模式
        btnchCheck.setEnabled(!xy);                                                                     // XY模式下禁用通道开关
        updateProbeUI(channel);                                                                         // 更新探头UI

        msgChannel.setChCheck(channelVisible);                                                          // 设置消息通道开关
        msgChannel.setInvert(invert);                                                                   // 设置消息反相
        msgChannel.setCouple(this.rgCouple.getSelectItem());                                            // 设置消息耦合方式
//        msgChannel.setImped(this.rgCouple.getSelectItem());
        msgChannel.setProbeType(this.rgProbeType.getSelectItem());                                      // 设置消息探头类型
        msgChannel.setProbeMultiple(probeMultiple);                                                     // 设置消息探头倍率
        msgChannel.setBandWidth(this.rgBandWidth.getSelectItem());                                      // 设置消息带宽
        msgChannel.setBandWidthEdit(this.bandWidthEdit.getText().toString());                            // 设置消息带宽编辑值
        msgChannel.setImped(this.rgImped.getSelectItem());                                              // 设置消息阻抗
        msgChannel.setLabel(label);                                                                     // 设置消息标签
        msgChannel.setDelay(delay);                                                                     // 设置消息延迟
        msgChannel.setFineExtent(fineExtent);                                                           // 设置消息微调幅度
        msgChannel.setFineSwitch(fineSwitch);                                                           // 设置消息微调开关
        sendMsgChannel(false);                                                                          // 发送通道消息
        isLoadCache=false;                                                                              // 清除缓存加载标记
        setInvertLight(invert);                                                                         // 更新反相指示灯
    }

    /** 探头插入前的参数保存映射表 */
    HashMap<String,String> beforeMap=new HashMap<>();

    /**
     * 保存当前参数（探头插入前调用）
     * <p>将当前探头类型、倍率、带宽、耦合、阻抗等参数保存到beforeMap中，
     * 以便探头拔出后恢复</p>
     */
    private void saveParam() {

        String probeTypeIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);       // 读取探头类型
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, probeTypeIdx);                            // 保存探头类型
        String probeMultiple = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);   // 读取探头倍率
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, probeMultiple);                      // 保存探头倍率
        String bandWidthIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);         // 读取带宽索引
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, bandWidthIdx);                             // 保存带宽索引
        String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);   // 读取高通频率
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, bandWidthHighEdit);             // 保存高通频率
        String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);    // 读取低通频率
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, bandWidthLowEdit);               // 保存低通频率
        String coupleIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);               // 读取耦合索引
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber, coupleIdx);                                  // 保存耦合索引
        String impedanceIdx = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);             // 读取阻抗索引
        beforeMap.put(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, impedanceIdx);                                // 保存阻抗索引

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,"");                          // 清空探头带宽缓存
    }

    /**
     * 恢复参数（探头拔出后调用）
     * <p>从beforeMap恢复探头插入前的参数，并重新加载缓存</p>
     */
    private void recoverParam(){
        isLoadCache=true;                                                                               // 标记正在加载缓存
        String probeTypeIdx= beforeMap.get(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);         // 读取保存的探头类型
        if (StrUtil.isEmpty(probeTypeIdx)) probeTypeIdx=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber);   // 缓存中没有则从CacheUtil读取
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber,probeTypeIdx);        // 恢复探头类型

        String probeMultiple = beforeMap.get(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);   // 读取保存的探头倍率
        if (StrUtil.isEmpty(probeMultiple)) probeMultiple= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber);   // 缓存中没有则从CacheUtil读取
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber,probeMultiple);   // 恢复探头倍率

//        String bandWidthIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber );
//        if (StrUtil.isEmpty(bandWidthIdx)) bandWidthIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber,bandWidthIdx);
//
//        String bandWidthHighEdit =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber );
//        if (StrUtil.isEmpty(bandWidthHighEdit)) bandWidthHighEdit= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber,bandWidthHighEdit);
//
//        String bandWidthLowEdit =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber );
//        if (StrUtil.isEmpty(bandWidthLowEdit)) bandWidthLowEdit= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber,bandWidthLowEdit);

        String coupleIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber );              // 读取保存的耦合索引
        if (StrUtil.isEmpty(coupleIdx)) coupleIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);   // 缓存中没有则从CacheUtil读取
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber,coupleIdx);               // 恢复耦合索引

        String impedanceIdx =beforeMap.get(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);              // 读取保存的阻抗索引
        if (StrUtil.isEmpty(impedanceIdx)) impedanceIdx= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);   // 缓存中没有则从CacheUtil读取
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber,impedanceIdx);             // 恢复阻抗索引

        //recreate
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber-1);                            // 获取通道实例

        double d= channel.getVScaleVal()/channel.getProbeRate();                                        // 计算原始VScale值
        double dResult = TBookUtil.getDoubleFromX(probeMultiple);                                       // 新倍率的数值
        String recoverFineExtent= TBookUtil.getMFromDouble(d*dResult);                                  // 计算恢复后的微调幅度
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+channelNumber,recoverFineExtent);    // 保存恢复的微调幅度
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,"");               // 清空探头带宽缓存
        btnchCheck.post(()->{                                                                           // 在UI线程执行
            setCache();                                                                                 // 重新加载缓存
        });


    }

    /** 探头类型常量：无智能探头 */
    public static final int ProbeType_NONE=0;
    /** 探头类型常量：MSP系列探头 */
    public static final int ProbeType_MSP =1;
    /** 探头类型常量：MDP系列探头 */
    public static final int probeType_MDP =2;
    /** 探头类型常量：MRCP系列探头 */
    public static final int ProbeType_MRCP=3;
    /** 探头类型常量：MOIP系列探头 */
    public static final int ProbeType_MOIP =4;


    /**
     * 获取探头类型（实例方法）
     * <p>根据通道的BaseProbe判断探头类型：MSP/MDP/MRCP/MOIP</p>
     * @param channel 通道实例
     * @return 探头类型常量（ProbeType_NONE/ProbeType_MSP/probeType_MDP/ProbeType_MRCP/ProbeType_MOIP）
     */
    private int getProbeType(Channel channel) {
        if (channel == null || !channel.isOpen()) return 0;                                             // 通道为空或关闭则返回0
        BaseProbe baseProbe = channel.getProbe();                                                       // 获取探头实例
        if (isProbeInterface() && baseProbe != null) {                                                  // 有智能探头接口且探头非空
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.getProbeType: %s",baseProbe.getModeName() ));
            if (baseProbe.getModeName().equals("MSP")) {                                                // MSP系列探头
                return ProbeType_MSP;
            } else if (baseProbe.getModeName().equals("MDP")) {                                         // MDP系列探头
                return probeType_MDP;
            } else if (baseProbe.getModeName().equals("MRCP")) {                                        // MRCP系列探头
                return ProbeType_MRCP;
            } else if (baseProbe.getModeName().equals("MOIP")) {                                        // MOIP系列探头
                return ProbeType_MOIP;
            }
        }
        return ProbeType_NONE;                                                                          // 无智能探头
    }

    /**
     * 获取探头类型（静态方法）
     * <p>根据通道索引判断探头类型</p>
     * @param chIdx 通道索引（FPGA编号）
     * @return 探头类型常量
     */
    public static int getProbeType(int chIdx) {
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);                                      // 获取通道实例
        if (channel == null || !channel.isOpen()) return 0;                                             // 通道为空或关闭则返回0
        BaseProbe baseProbe = channel.getProbe();                                                       // 获取探头实例
        if (RightLayoutChannel.isProbeInterface(chIdx) && baseProbe != null) {                          // 有智能探头接口且探头非空
            if (baseProbe.getModeName().equals("MSP")) {                                                // MSP系列探头
                return ProbeType_MSP;
            } else if (baseProbe.getModeName().equals("MDP")) {                                         // MDP系列探头
                return probeType_MDP;
            } else if (baseProbe.getModeName().equals("MRCP")) {                                        // MRCP系列探头
                return ProbeType_MRCP;
            } else if (baseProbe.getModeName().equals("MOIP")) {                                        // MOIP系列探头
                return ProbeType_MOIP;
            }
        }
        return ProbeType_NONE;                                                                          // 无智能探头
    }

    /**
     * 设置MDP700探头倍率选择控件的可见性
     * <p>当MDP/MOIP探头支持自动倍率控制时隐藏倍率选择控件</p>
     */
    private void setMdp700SelectVisible(){
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
        int probeType=getProbeType(channel);                                                            // 获取探头类型
        if (probeType== probeType_MDP || probeType== ProbeType_MOIP) {                                 // MDP或MOIP探头
            BaseProbe bp = channel.getProbe();                                                          // 获取探头实例
            if (bp != null && bp.isAutoRateCtrl()) {                                                    // 支持自动倍率控制
                mdp700Select.setVisibility(GONE);                                                       // 隐藏倍率选择
            } else {                                                                                    // 不支持自动倍率
                mdp700Select.setVisibility(VISIBLE);                                                    // 显示倍率选择
            }
        }
    }

    /**
     * 刷新探头UI（仅更新倍率显示，不切换布局）
     * <p>根据探头类型更新对应的倍率显示文本</p>
     * @param channel 通道实例
     */
    private void refreshProbeUI(Channel channel){
        int probeType=getProbeType(channel);                                                            // 获取探头类型
        if (probeType== ProbeType_MSP || probeType==ProbeType_MRCP){                                    // MSP或MRCP探头
            String sx= TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());                     // 获取倍率字符串
            probeTypeX.setText(sx);                                                                     // 设置MSP倍率文本
        }else if (probeType== probeType_MDP || probeType== ProbeType_MOIP){                             // MDP或MOIP探头
            String s= TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());                      // 获取倍率字符串
            mdp700Select.setSelectText(s);                                                              // 设置MDP倍率选择文本
            setMdp700SelectVisible();                                                                   // 更新可见性
        }else {
            //none
        }
    }

    /**
     * 更新探头UI（不带EventBus来源标记）
     * <p>委托给带isFromEventBus参数的重载方法</p>
     * @param channel 通道实例
     */
    private void updateProbeUI(Channel channel){
        updateProbeUI(channel,false);
    }

    /**
     * 更新探头UI（核心方法）
     * <p>根据探头类型切换探头布局（通用/MSP/MRP/MDP），更新探头型号/序列号/倍率/阻抗等参数，
     * 设置带宽，发送消息通知订阅方</p>
     * @param channel 通道实例
     * @param isFromEventBus 是否来自EventBus事件（影响是否更新Channel数据模型）
     */
    private void updateProbeUI(Channel channel, boolean isFromEventBus){
        try {
            if (!ChannelFactory.isDynamicCh(channel.getChId())) {                                      // 非动态通道则返回
                return;
            }
            if (!channel.isOpen() || channel.getChId() != channelNumber - 1) return;                    // 通道未开启或ID不匹配则返回

            setProbeTypeLayout(channel);                                                                // 根据探头类型切换布局
            BaseProbe baseProbe = channel.getProbe();                                                   // 获取探头实例
            int probeType = getProbeType(channel);                                                      // 获取探头类型

//        if (isProbeInterface() && baseProbe != null) {
            if (probeType == ProbeType_MSP) {                                                           // MSP系列探头
                String model = baseProbe.getProbeName();                                                // 获取探头型号
                String sn = baseProbe.getSN();                                                          // 获取序列号
                String version = baseProbe.getVersion();                                                // 获取版本号
                List<String> probeX = baseProbe.getProbeX();                                            // 获取倍率列表
                double probeRate = baseProbe.getProbeRate();                                            // 获取倍率值
                baseProbe.setProbeRate(probeRate);                                                      // 设置倍率值


                probeTypeModel.setText(model);                                                          // 显示型号
                probeTypeSN.setText(sn);                                                                // 显示序列号
                String sx = TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());                // 获取倍率字符串
                probeTypeX.setText(sx);                                                                 // 显示倍率
//            probeTypeCall.setText(R.string.Information);
                if (channel.getResistanceType() != Channel.RESISTANCE_1M) {                             // MSP探头强制1MΩ阻抗
                    channel.setResistanceType(Channel.RESISTANCE_1M);
                }
                msgChannel.setImped(rgImped.getSelectItem());                                           // 更新消息阻抗
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));  // 缓存探头类型
                sendMsgChannel(true);                                                                   // 发送消息（来自事件总线）
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, "");   // 清空自定义倍率
                int impIdx = baseProbe.isImped50() ? 1 : 0;                                            // 探头支持50Ω则设1
                rgImped.setSelectIndex(impIdx);                                                         // 设置阻抗选中项
                if (baseProbe.isImped50()) {                                                            // 探头支持50Ω
                    rgImped.setEnabled(0, false);                                                       // 禁用1MΩ选项
                    rgImped.setEnabled(1, true);                                                        // 启用50Ω选项
                } else {                                                                                // 探头不支持50Ω
                    rgImped.setEnabled(0, true);                                                        // 启用1MΩ选项
                    rgImped.setEnabled(1, false);                                                       // 禁用50Ω选项
                }
                ChannelFactory.chActivate(channel.getChId());                                           // 激活通道
            } else if (probeType == ProbeType_MRCP) {                                                   // MRCP系列探头
                String model = baseProbe.getProbeName();                                                // 获取型号
                String sn = baseProbe.getSN();                                                          // 获取序列号
                String version = baseProbe.getVersion();                                                // 获取版本号
                List<String> probeX = baseProbe.getProbeX();                                            // 获取倍率列表
                double probeRate = baseProbe.getProbeRate();                                            // 获取倍率值
                baseProbe.setProbeRate(probeRate);                                                      // 设置倍率值

                String sx = TBookUtil.getXFromDouble(channel.getProbe().getProbeRate());                // 获取倍率字符串
                mrcpProbeTypeX.setText(sx);                                                             // 显示MRCP倍率
                mrcpMode.setText("Model:" +model);                                                      // 显示MRCP型号
                if (channel.getResistanceType() != Channel.RESISTANCE_1M) {                             // MRCP探头强制1MΩ
                    channel.setResistanceType(Channel.RESISTANCE_1M);
                }
//                msgChannel.setImped(rgCouple.getSelectItem());
                msgChannel.setImped(rgImped.getSelectItem());                                           // 更新消息阻抗
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));  // 缓存探头类型
                sendMsgChannel(true);                                                                   // 发送消息
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, "");   // 清空自定义倍率
                int impIdx = baseProbe.isImped50() ? 1 : 0;                                            // 探头支持50Ω则设1
                rgImped.setSelectIndex(impIdx);                                                         // 设置阻抗选中项
                if (baseProbe.isImped50()) {                                                            // 探头支持50Ω
                    rgImped.setEnabled(0, false);                                                       // 禁用1MΩ选项
                    rgImped.setEnabled(1, true);                                                        // 启用50Ω选项
                } else {                                                                                // 探头不支持50Ω
                    rgImped.setEnabled(0, true);                                                        // 启用1MΩ选项
                    rgImped.setEnabled(1, false);                                                       // 禁用50Ω选项
                }
                ChannelFactory.chActivate(channel.getChId());                                           // 激活通道
            } else if (probeType == probeType_MDP || ProbeType_MOIP == probeType) {                     // MDP或MOIP探头

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(baseProbe.getProbeType()));  // 缓存探头类型
                mdp700Model.setText("Model:" + baseProbe.getProbeName());                               // 显示MDP型号
                String sx = TBookUtil.getXFromDouble(baseProbe.getProbeRate());                         // 获取倍率字符串

                String[] s = baseProbe.getScaleNames().toArray(new String[0]);                          // 获取倍率列表
                mdp700Select.setArray(s);                                                               // 设置MDP倍率选择列表
                int index = Tools.indexOf(s, i -> i.equals(sx));                                        // 查找当前倍率的索引
                if (index >= 0 && index <= 1) {                                                         // 索引有效
                    mdp700Select.setSelectIndex(index);                                                 // 设置选中项
                }
                setMdp700SelectVisible();                                                               // 更新MDP倍率选择可见性

                int impIdx = baseProbe.isImped50() ? 1 : 0;                                            // 探头支持50Ω则设1
                rgImped.setSelectIndex(impIdx);                                                         // 设置阻抗选中项
                if (baseProbe.isImped50()) {                                                            // 探头支持50Ω
                    rgImped.setEnabled(0, false);                                                       // 禁用1MΩ
                    rgImped.setEnabled(1, true);                                                        // 启用50Ω
                } else {                                                                                // 探头不支持50Ω
                    rgImped.setEnabled(0, true);                                                        // 启用1MΩ
                    rgImped.setEnabled(1, false);                                                       // 禁用50Ω
                }
                // set bandwidth
                setProbeBandWidth(baseProbe);                                                           // 设置探头带宽

                msgChannel.setBandWidth(rgBandWidth.getSelectItem());                                   // 更新消息带宽
                msgChannel.setBandWidthEdit(TBookUtil.getHz3FromHz(baseProbe.getBandWidth()));          // 更新消息带宽编辑值
                msgChannel.setProbeMultiple(sx);                                                        // 更新消息倍率
//                msgChannel.setImped(rgCouple.getSelectItem());
                msgChannel.setImped(rgImped.getSelectItem());                                           // 更新消息阻抗
                sendMsgChannel(true);                                                                   // 发送消息
                ChannelFactory.chActivate(channel.getChId());                                           // 激活通道
            } else {                                                                                    // 无智能探头
                dialogProbeInterface.hide();                                                            // 隐藏探头接口对话框
                int unit = channel.getProbeType();                                                      // 获取探头类型（电压/电流）
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(unit));   // 缓存探头类型
                msgChannel.setChCheck(channel.isOpen());                                                // 更新消息通道开关
                sendMsgChannel(true);                                                                   // 发送消息
                rgImped.setEnabled(true);                                                               // 启用阻抗所有选项
                unLockScreen();                                                                         // 解锁屏幕
            }
            if(!isFromEventBus) {                                                                       // 非来自EventBus
//                channel.setResistanceType(rgCouple.getSelectIndex());
                channel.setResistanceType(rgImped.getSelectIndex());                                    // 更新通道阻抗
            }
//            RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(channel.getChId() + IWave.Ch1, false, true));
            MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(channel.getChId()),isFromEventBus);   // 设置活动通道
            refreshOffset();                                                                            // 刷新偏移显示
        }catch (Exception e){                                                                           // 捕获异常防止崩溃

        }

        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber);         // 读取阻抗缓存
        int couple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber);           // 读取耦合缓存
        this.rgImped.setEnabled(1, matchCouple(couple) != Channel.COUPLE_TYPE_AC);//耦合方式选择AC时，阻抗50Ω置灰
        this.rgCouple.setEnabled(1, impedance != Channel.RESISTANCE_50);//阻抗选择50Ω时， 耦合方式AC置灰
    }

    /**
     * 设置探头带宽（探头插入时调用）
     * <p>根据当前带宽选中项和探头带宽，设置通道的带宽类型和值</p>
     * @param baseProbe 探头实例
     */
    private void setProbeBandWidth(BaseProbe baseProbe) {
        if (baseProbe == null) return;                                                                  // 探头为空则返回
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);                         // 获取通道实例
        if (channel == null) return;                                                                    // 通道为空则返回
        if (rgBandWidth.getSelectIndex() == 0 || rgBandWidth.getSelectIndex() == 3) {                  // Full或Highpass
            rgBandWidth.setSelectIndex(0);                                                              // 重置为Full
            bandWidthEdit.setEnabled(false);                                                            // 禁用带宽编辑
            bandWidthEdit.setText("");                                                                  // 清空编辑文本
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, getMaxBandWidth());                   // 设置全带宽

        } else if (rgBandWidth.getSelectIndex() == 4) {                                                // Lowpass
            long probeHz = baseProbe.getBandWidth();                                                    // 获取探头带宽
            String probeBandwidth = TBookUtil.getHz3FromHz(probeHz);                                    // 转换为频率字符串
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH + channelNumber, probeBandwidth);   // 缓存探头带宽

            double Hz = getBandWidthCurScopeMaxProbe();                                                 // 获取当前示波器和最大探头的最小带宽
            String showBandwidth = TBookUtil.getHz3FromHz(Hz);                                         // 转换为频率字符串

            bandWidthEdit.setText(showBandwidth);                                                       // 显示带宽
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, Hz);                               // 设置低通带宽
        }
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(rgBandWidth.getSelectIndex()));   // 缓存带宽选中项
    }

    /**
     * 设置探头带宽变更（带宽选项切换时调用）
     * <p>根据当前带宽选中项和探头带宽，更新通道带宽类型和值</p>
     * @param baseProbe 探头实例
     */
    private void setProbeBandWidth_Change(BaseProbe baseProbe){
        if (baseProbe == null) return;                                                                  // 探头为空则返回
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);                         // 获取通道实例
        if (channel == null) return;                                                                    // 通道为空则返回
        if (rgBandWidth.getSelectIndex() == 0 /*|| rgBandWidth.getSelectIndex()==1*/) {                // Full
            rgBandWidth.setSelectIndex(0);                                                              // 设置为Full
            bandWidthEdit.setEnabled(false);                                                            // 禁用编辑
            bandWidthEdit.setText("");                                                                  // 清空文本
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, getMaxBandWidth());                   // 设置全带宽

        } else if (rgBandWidth.getSelectIndex() == 4) {                                                // Lowpass
            long probeHz = baseProbe.getBandWidth();                                                    // 获取探头带宽
            long scopeHz = getScopeBandWidth();                                                         // 获取示波器带宽
            long Hz = Math.min(scopeHz, probeHz);                                                      // 取较小值
            String showBandwidth = TBookUtil.getHz3FromHz(Hz);                                         // 转换为频率字符串
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.setProbeBandWidth_Change: %s",showBandwidth ));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, showBandwidth);   // 缓存低通频率
            bandWidthEdit.setText(showBandwidth);                                                       // 显示带宽
            channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, Hz);                               // 设置低通带宽
        }
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(rgBandWidth.getSelectIndex()));   // 缓存带宽选中项
    }

    /**
     * 获取最大带宽
     * <p>取示波器最大带宽和探头带宽的较小值</p>
     * @return 最大带宽值（Hz）
     */
    private long getMaxBandWidth(){
        long scopeBandWidth = (long) Channel.getMaxBandWidth();                                         // 示波器最大带宽
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);                         // 获取通道实例
        if (channel == null) return scopeBandWidth;                                                     // 通道为空则返回示波器带宽
        int probeType = getProbeType(channel);                                                          // 获取探头类型
        long probeBandWidth = 0;                                                                        // 探头带宽初始值
        if (probeType != ProbeType_NONE && probeType != ProbeType_MSP) {                                // 非无探头和非MSP
            probeBandWidth = channel.getProbe().getBandWidth();                                         // 获取探头带宽

        }
        if (probeType == ProbeType_NONE || probeType == ProbeType_MSP) {                               // 无探头或MSP
            return scopeBandWidth;                                                                      // 返回示波器带宽
        } else {                                                                                        // 其他探头
            return Math.min(scopeBandWidth, probeBandWidth);                                            // 取较小值
        }
    }

    /**
     * 刷新偏移显示
     * <p>根据探头类型（电压/电流）更新偏移文本的单位</p>
     */
    private void refreshOffset(){
        Channel channel= ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));             // 获取通道实例
        String text=chOffset.getText();                                                                 // 获取当前偏移文本
        String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";                        // 根据探头类型选择单位
        text= text.replace("A",unit).replace("V",unit);                                                 // 替换单位
        chOffset.setText(text);                                                                         // 更新偏移文本
    }

    /**
     * 将UI耦合索引映射为Channel耦合类型常量
     * @param couple UI耦合索引（0=DC, 1=AC, 2=GND）
     * @return Channel耦合类型常量
     */
    private int matchCouple(int couple) {
        if (couple == 0) return Channel.COUPLE_TYPE_DC;                                                // 0 → DC
        if (couple == 1) return Channel.COUPLE_TYPE_AC;                                                // 1 → AC
        return Channel.COUPLE_TYPE_GND;                                                                 // 2 → GND
    }

    /**
     * 将Channel耦合类型常量映射为UI耦合索引
     * @param scopeCouple Channel耦合类型常量
     * @return UI耦合索引（0=DC, 1=AC, 2=GND）
     */
    private int unMatchCouple(int scopeCouple) {
        if (scopeCouple == Channel.COUPLE_TYPE_DC) return 0;                                           // DC → 0
        if (scopeCouple == Channel.COUPLE_TYPE_AC) return 1;                                           // AC → 1
        return 2;                                                                                       // GND → 2
    }

    /**
     * 判断是否启用高通/低通滤波器
     * @return true=启用，false=不启用
     */
    private boolean isEnableHighLowFilter() {
        return ScopeConfig.getConfig().isEnableHighLowFilter() || App.IsDebug();                        // 配置启用或调试模式
    }

    /**
     * 发送通道消息
     * <p>根据设备通道数量过滤无效通道（2通道设备不发Ch3-Ch8，4通道设备不发Ch5-Ch8），
     * 通过RxBus发送RightMsgChannel消息</p>
     * @param isFromEventBus 是否来自EventBus
     */
    private void sendMsgChannel(boolean isFromEventBus) {
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {                          // 2通道设备
            if (channelNumber == TChan.Ch3 || channelNumber == TChan.Ch4
                    || channelNumber == TChan.Ch5 || channelNumber == TChan.Ch6
                    || channelNumber == TChan.Ch7 || channelNumber == TChan.Ch8)
            {
                return;                                                                                 // 2通道设备不发送Ch3-Ch8
            }
        } else if(GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {                    // 4通道设备
            if (channelNumber == TChan.Ch5 || channelNumber == TChan.Ch6
                    || channelNumber == TChan.Ch7 || channelNumber == TChan.Ch8)
            {
                return;                                                                                 // 4通道设备不发送Ch5-Ch8
            }
        }
        msgChannel.setFromEventBus(isFromEventBus);                                                     // 设置来源标记
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_CHANNEL, msgChannel);                               // 发送通道消息
    }

    /**
     * 根据探头类型切换探头布局
     * <p>根据探头类型（NONE/MSP/MRCP/MDP/MOIP）显示对应的布局，
     * 并设置带宽选项的可用性</p>
     * @param channel 通道实例
     */
    private void setProbeTypeLayout(Channel channel) {
        int probeType = getProbeType(channel);                                                          // 获取探头类型
        switch (probeType) {
            case ProbeType_NONE: {                                                                      // 无智能探头
                commonLayout.setVisibility(VISIBLE);                                                    // 显示通用布局
                msp500Layout.setVisibility(GONE);                                                       // 隐藏MSP布局
                mdp700Layout.setVisibility(GONE);                                                       // 隐藏MDP布局
                mrcpLayout.setVisibility(GONE);                                                         // 隐藏MRCP布局
                this.rgBandWidth.setEnabled(0, true);//full                                              // 启用Full选项
                this.rgBandWidth.setEnabled(3, true);//highpass                                          // 启用Highpass选项
            }
            break;
            case ProbeType_MSP: {                                                                       // MSP探头
                commonLayout.setVisibility(GONE);                                                       // 隐藏通用布局
                msp500Layout.setVisibility(VISIBLE);                                                    // 显示MSP布局
                mdp700Layout.setVisibility(GONE);                                                       // 隐藏MDP布局
                mrcpLayout.setVisibility(GONE);                                                         // 隐藏MRCP布局
                this.rgBandWidth.setEnabled(0,true);                                                    // 启用Full选项
                this.rgBandWidth.setEnabled(3, true);                                                   // 启用Highpass选项
            }
            break;
            case ProbeType_MRCP:{                                                                       // MRCP探头
                commonLayout.setVisibility(GONE);                                                       // 隐藏通用布局
                msp500Layout.setVisibility(GONE);                                                       // 隐藏MSP布局
                mdp700Layout.setVisibility(GONE);                                                       // 隐藏MDP布局
                mrcpLayout.setVisibility(VISIBLE);                                                      // 显示MRCP布局
                this.rgBandWidth.setEnabled(0,true);                                                    // 启用Full选项
                this.rgBandWidth.setEnabled(3, false);                                                  // 禁用Highpass选项
            }break;
            case ProbeType_MOIP:                                                                        // MOIP探头
            case probeType_MDP: {                                                                       // MDP探头
                commonLayout.setVisibility(GONE);                                                       // 隐藏通用布局
                msp500Layout.setVisibility(GONE);                                                       // 隐藏MSP布局
                mdp700Layout.setVisibility(VISIBLE);                                                    // 显示MDP布局
                mrcpLayout.setVisibility(GONE);                                                         // 隐藏MRCP布局
                this.rgBandWidth.setEnabled(0,true);                                                    // 启用Full选项
                this.rgBandWidth.setEnabled(3, false);                                                  // 禁用Highpass选项
            }
            break;
        }
    }

    /**
     * 判断当前通道是否有智能探头接口（实例方法）
     * @return true=有智能探头接口
     */
    private boolean isProbeInterface() {
        return isProbeInterface(TChan.toFpgaChNo(channelNumber));                                       // 委托给静态方法
    }

    /**
     * 判断指定通道是否有智能探头接口（静态方法）
     * @param idx 通道索引（FPGA编号）
     * @return true=有智能探头接口
     */
    public static boolean isProbeInterface(int idx) {
        Channel channel = ChannelFactory.getDynamicChannel(idx);                                        // 获取通道实例
        return channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());   // 通道非空+自动探头+有序列号
//        return true;
    }

    /**
     * 对话框关闭消费者
     * <p>当探头接口对话框关闭时，更新MDP700倍率选择可见性</p>
     */
    private Consumer<Integer> onDialogClose=(i)->{
        if (i== MainViewGroup.DIALOG_PROBE_INTERFACE ){                                                 // 探头接口对话框关闭
           setMdp700SelectVisible();                                                                    // 更新MDP700倍率选择可见性
        }
    };

    /**
     * 外部旋钮微调消费者
     * <p>处理外部旋钮切换微调开关的事件</p>
     */
    private Consumer<Boolean> consumerExternalkeysVernier = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {

            int chidx = ChannelFactory.getChActivate();                                                // 获取当前活动通道
            if(ChannelFactory.isDynamicCh(chidx)){                                                     // 是动态通道
                if(chidx == channelNumber - 1){                                                        // 是当前通道
                    PlaySound.getInstance().playButton();                                               // 播放按键音
                    boolean state = !btnFineSwitch.isState();                                           // 切换微调开关状态
                    btnFineSwitch.setState(state);                                                      // 设置微调开关
//                    chFineExtent.setEnabled(state);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));   // 缓存微调开关
                    Command.get().getChannel().Vernier(chidx,state,false);                              // 发送微调命令
                    msgChannel.setFineSwitch(state);                                                    // 更新消息微调开关
                    sendMsgChannel(false);                                                              // 发送通道消息
                }
            }
        }
    };

    /**
     * 缓存加载消费者
     * <p>加载全部通道参数缓存</p>
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();                                                                                 // 加载缓存
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutChannel, true);                  // 标记已加载
        }
    };

    /**
     * 扩展缓存加载消费者
     * <p>仅加载垂直基准和阻抗参数</p>
     */
    private Consumer<LoadCache> consumerLoadCacheEx = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCacheEx();                                                                               // 加载扩展缓存
        }
    };

    /**
     * 工作模式切换消费者
     * <p>根据工作模式（XY/非XY）启用或禁用通道控件</p>
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            boolean isNoXY = workModeBean.getNextWorkMode() != IWorkMode.WorkMode_XY;                  // 是否非XY模式
            if (!isNoXY) {                                                                              // XY模式
                btnchCheck.setState(true);                                                              // 强制开启通道
                btnchCheck.setEnabled(false);                                                           // 禁用通道开关
                rgVerBase.setEnabled(false);                                                            // 禁用垂直基准
                chLabel.setEnabled(false);                                                              // 禁用标签
                chDelay.setEnabled(false);                                                              // 禁用延迟
                chOffset.setEnabled(false);                                                             // 禁用偏移
                chPosition.setEnabled(false);                                                           // 禁用位置
                chFineExtent.setEnabled(false);                                                         // 禁用微调幅度
                btnFineSwitch.setEnabled(false);                                                        // 禁用微调开关
            } else {                                                                                    // 非XY模式
                btnchCheck.setEnabled(true);                                                            // 启用通道开关
                rgVerBase.setEnabled(true);                                                             // 启用垂直基准
                chLabel.setEnabled(true);                                                               // 启用标签
                chDelay.setEnabled(true);                                                               // 启用延迟
                chOffset.setEnabled(true);                                                              // 启用偏移
                chPosition.setEnabled(true);                                                            // 启用位置
                chFineExtent.setEnabled(true);                                                          // 启用微调幅度
                btnFineSwitch.setEnabled(true);                                                         // 启用微调开关
            }
        }
    };

    /**
     * 通道开关消息消费者
     * <p>接收MainRightMsgChannels消息，根据通道编号更新开关状态</p>
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels)  {
            switch (channelNumber) {                                                                    // 根据通道编号分发
                case TChan.Ch1:                                                                         // CH1
                    if (msgChannels.getCh1().isValue() == btnchCheck.isState()) {                       // 状态未变则跳过
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh1().isValue());                                // 设置开关状态
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {         // 非XY模式
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch1, String.valueOf(btnchCheck.isState()));   // 缓存开关状态
                    }
                    msgChannel.setChCheck(btnchCheck.isState());                                        // 更新消息开关
                    break;
                case TChan.Ch2:                                                                         // CH2
                    if (msgChannels.getCh2().isValue() == btnchCheck.isState()) {                       // 状态未变则跳过
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh2().isValue());                                // 设置开关状态
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {         // 非XY模式
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch2, String.valueOf(btnchCheck.isState()));   // 缓存开关状态
                    }
                    msgChannel.setChCheck(btnchCheck.isState());                                        // 更新消息开关
                    break;
                case TChan.Ch3:                                                                         // CH3
                    if (msgChannels.getCh3().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh3().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch3, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch4:                                                                         // CH4
                    if (msgChannels.getCh4().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh4().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch4, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch5:                                                                         // CH5
                    if (msgChannels.getCh5().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh5().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch5, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch6:                                                                         // CH6
                    if (msgChannels.getCh6().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh6().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch6, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch7:                                                                         // CH7
                    if (msgChannels.getCh7().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh7().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch7, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
                case TChan.Ch8:                                                                         // CH8
                    if (msgChannels.getCh8().isValue() == btnchCheck.isState()) {
                        return;
                    }
                    btnchCheck.setState(msgChannels.getCh8().isValue());
                    if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE+TChan.Ch8, String.valueOf(btnchCheck.isState()));
                    }
                    msgChannel.setChCheck(btnchCheck.isState());
                    break;
            }
        }
    };

    /**
     * 带宽变更消费者
     * <p>接收带宽变更消息（格式：频率,通道索引），更新带宽缓存和FPGA</p>
     */
    private Consumer<String> consumerChannelBandWidth=new Consumer<String>() {
        @Override
        public void accept(String s) throws Exception {
            String[] param= s.split(",");                                                               // 解析参数（频率,通道索引）
            String fb=param[0];                                                                         // 频率值
            int chIdx=Integer.parseInt(param[1]);                                                       // 通道索引
            if (chIdx!=channelNumber-1) return;                                                         // 非当前通道则跳过
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber,fb);           // 缓存探头带宽

            double bandWidth= getBandWidthCurScopeCurProbe();                                           // 获取当前带宽
//            Log.d("Tag.Debug", String.format("RightLayoutChannel.accept: %f",bandWidth ));
            sendChannelBandWidth(bandWidth);                                                            // 发送带宽到通道
        }
    };

    /**
     * 垂直档位调整消费者
     * <p>接收垂直档位调整消息，更新背景图和发送档位切换消息</p>
     */
    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        //接收垂直档位调整消息
        @Override
        public void accept(String adjustStr) throws Throwable {

            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);                              // 解析参数
            boolean isClickTop = Boolean.parseBoolean(param[0]); // 解析参数，判断是向上还是向下调整
            int chan = Integer.parseInt(param[1]);                                                      // 通道编号
            if (chan != channelNumber) return;                                                          // 非当前通道则跳过

            if (isClickTop) {                                                                           // 向上调整
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);                // 显示向上箭头背景
                msgChannel.setUpClick(true);                                                            // 设置向上标记
                postChange();// 调用postChange()进行档位切换
            } else {                                                                                    // 向下调整
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);                // 显示向下箭头背景
                msgChannel.setUpClick(false);                                                           // 设置向下标记
                postChange();// 调用postChange()进行档位切换
            }
        }
    };

    /**
     * 通道位置消息消费者
     * <p>接收位置字符串和通道ID，更新位置编辑控件</p>
     */
    private Consumer<String> consumerChannelPosition = new Consumer<String>() {

        @Override
        public void accept(String messageStr) throws Throwable {
            String [] parts = messageStr.split(";",2);                                                  // 解析参数（位置;通道ID）
            String positionStr = parts[0].trim();                                                       // 位置字符串
            int chId = Integer.parseInt(parts[1].trim());                                               // 通道ID
            if(chId+1 == channelNumber){                                                                // 是当前通道
                chPosition.setText(positionStr);                                                        // 设置位置文本
                positionString = positionStr;                                                           // 缓存位置字符串
            }
        }
    };

    /**
     * 当前探头选择带宽与示波器最大带宽比较
     * <p>取探头带宽和示波器带宽的较小值</p>
     * @return 实际带宽值（Hz）
     */
    private double getBandWidthCurScopeCurProbe(){
        long probeBandWidth= getProbeBandWidth();                                                       // 获取探头带宽
        long scopeBandWidth= getScopeBandWidth();                                                       // 获取示波器带宽
        //        Log.d("Tag.Debug", String.format("RightLayoutChannel.getFinalBandWidth probe: %d ,scope: %d",probeBandWidth,scopeBandWidth ));
        return Math.min(probeBandWidth, scopeBandWidth);                                                // 取较小值
    }

    /**
     * 获取当前示波器带宽与最大探头带宽的较小值
     * <p>用于MDP/MOIP探头的带宽计算</p>
     * @return 实际带宽值（Hz）
     */
    private double getBandWidthCurScopeMaxProbe(){
        long scopeBandWidth= getScopeBandWidth();                                                       // 获取示波器带宽
        BaseProbe baseProbe= ChannelFactory.getDynamicChannel(channelNumber-1).getProbe();              // 获取探头实例
        long probeBandWidth=Long.MAX_VALUE;                                                             // 默认探头带宽无限制
        if (baseProbe!=null){                                                                           // 探头非空
            probeBandWidth=baseProbe.getBandWidth();                                                    // 获取探头带宽
        }
        return Math.min(scopeBandWidth, probeBandWidth);                                                // 取较小值
    }

    /**
     * 获取探头带宽（从缓存读取探头倍率对应的带宽）
     * @return 探头带宽值（Hz），无缓存时返回Long.MAX_VALUE
     */
    private long getProbeBandWidth(){
        Channel channel=ChannelFactory.getDynamicChannel(channelNumber-1);                              // 获取通道实例
        String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+channelNumber);    // 读取探头带宽缓存
        if (StrUtil.isEmpty(fb) || channel.getProbe()==null ) return Long.MAX_VALUE;                    // 无缓存或无探头则返回最大值
        long probeBandWidth=  (long) (TBookUtil.getMHzFromHz(fb) * 1000 * 1000);                       // 转换为Hz
        return probeBandWidth;
    }

    /**
     * 获取示波器带宽
     * <p>根据带宽选中项（Full/200M/20M/Highpass/Lowpass）计算示波器带宽</p>
     * @return 示波器带宽值（Hz）
     */
    private long getScopeBandWidth() {
        long scopeBandWidth = (long) 20e6;                                                              // 默认20MHz
        switch (rgBandWidth.getSelectIndex()) {                                                         // 根据带宽选中项
            case 0://full
                scopeBandWidth = getMaxBandWidth();                                                     // 全带宽
                break;
            case 1://200M
                scopeBandWidth = (long) 200e6;                                                          // 200MHz
                break;
            case 2://20M
                scopeBandWidth = (long) 20e6;                                                           // 20MHz
                break;
            case 3://highpass
                String bandWidthHighEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber);   // 读取高通频率
                scopeBandWidth = (long) (TBookUtil.getMHzFromHz(bandWidthHighEdit) * 1000 * 1000);      // 转换为Hz
                break;
            case 4://lowpass
                String bandWidthLowEdit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber);     // 读取低通频率
                scopeBandWidth = (long) (TBookUtil.getMHzFromHz(bandWidthLowEdit) * 1000 * 1000);       // 转换为Hz
                break;
        }
        return scopeBandWidth;
    }

    /**
     * 命令到UI消费者
     * <p>接收来自底层/远程控制的命令消息，处理约13种FLAG：
     * CHANNEL_DISPLAY/INVERSE/BANDWIDTH/PROBETYPE/PROBEMULTIPLE/COUPLE/INPUTRES/VREF/
     * LABEL/LABEL_CLEAR/DELAY/OFFSET/VERNIER</p>
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {                                                         // 根据命令FLAG分发
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {                                             // 通道显示开关
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    boolean isOpen = Boolean.parseBoolean(params[1]);                                   // 开关状态
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    if (!btnchCheck.isEnabled()) {                                                      // 开关被禁用
                        return;
                    }
                    btnchCheck.setState(isOpen);                                                        // 设置开关状态
                    operationUI_ToggleState(btnchCheck, isOpen, false);                                 // 执行UI操作
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_INVERSE: {                                             // 反相开关
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    boolean isOpen = Boolean.parseBoolean(params[1]);                                   // 开关状态
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    btnInvert.setState(isOpen);                                                         // 设置反相状态
                    operationUI_ToggleState(btnInvert, isOpen, false);                                  // 执行UI操作
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_BANDWIDTH: {                                           // 带宽设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    int bandIndex = Integer.parseInt(params[1]);                                        // 带宽索引
                    double bandDetail = Double.parseDouble(params[2]);                                  // 带宽详情值
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    if (!isEnableHighLowFilter() && (bandIndex == 3 || bandIndex == 4)) {               // 未启用高通/低通
                        return;
                    }
                    rgBandWidth.setSelectIndex(bandIndex);                                              // 设置带宽选中项
                    setBandWidth(bandDetail);                                                           // 设置带宽值
//                    if (bandIndex == 3) {//highpass
//                        bandWidthEdit.setEnabled(true);
//                        bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber));
//                    } else if (bandIndex == 4) {//lowpass
//                        bandWidthEdit.setEnabled(true);
//                        bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber));
//                    }
                    onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), false);               // 触发带宽选择回调
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_PROBETYPE: {                                           // 探头类型设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    int probeTypeIndex = Integer.parseInt(params[1]);                                   // 探头类型索引
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    rgProbeType.setSelectIndex(probeTypeIndex);                                         // 设置探头类型选中项
                    onItemClick(rgProbeType.getId(), rgProbeType.getSelectItem(), false);               // 触发探头类型选择回调
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_PROBEMULTIPLE: {                                       // 探头倍率设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    double multiple = Double.parseDouble(params[1]);                                    // 倍率值
                    String probeRate = TBookUtil.getXFromDouble(multiple);                              // 转换为X格式
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    if (!dialogProbeMultiple.isExistProbeRate(probeRate)) return;                       // 倍率不在可选列表中
                    onTextClick(probeRate, false);                                                      // 触发倍率选择
                    dialogProbeMultiple.hide();                                                         // 隐藏倍率对话框
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_COUPLE: {                                              // 耦合方式设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    int coupleIndex = Integer.parseInt(params[1]);                                      // 耦合索引
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    rgCouple.setSelectIndex(coupleIndex);                                               // 设置耦合选中项
                    onItemClick(rgCouple.getId(), rgCouple.getSelectItem(), false);                     // 触发耦合选择回调
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_INPUTRES: {                                            // 阻抗设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    int inp = Integer.parseInt(params[1]);                                              // 阻抗索引
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    rgImped.setSelectIndex(inp);                                                        // 设置阻抗选中项
                    onItemClick(rgImped.getId(), rgImped.getSelectItem(), false);                       // 触发阻抗选择回调
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_VREF: {                                                // 垂直基准设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    int vref = Integer.parseInt(params[1]);                                             // 垂直基准索引
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    rgVerBase.setSelectIndex(vref);                                                     // 设置垂直基准选中项
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), false);                   // 触发垂直基准选择回调
                    break;
                }
                case CommandMsgToUI.FLAG_CHANNEL_LABEL: {                                               // 标签设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    String result = params[1];                                                          // 标签文本
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    chLabel.setText(result);                                                            // 设置标签文本
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);      // 缓存标签
                    WaveManage.get().setChannelLabel(channelNumber, result);                            // 设置波形标签
                    setChannelLabel(chIndex, result);                                                   // 设置通道标签
                }
                break;
                case CommandMsgToUI.FLAG_CHANNEL_LABEL_CLEAR: {                                         // 标签清除
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    String result = "";                                                                 // 空标签
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    chLabel.setText(result);                                                            // 清空标签
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);      // 缓存空标签
                    WaveManage.get().setChannelLabel(channelNumber, result);                            // 清空波形标签
                    setChannelLabel(chIndex, result);                                                   // 清空通道标签
                }
                break;
                case CommandMsgToUI.FLAG_CHANNEL_DELAY:{                                                // 延迟设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    double result = Double.parseDouble( params[1]);                                     // 延迟值（秒）
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过
                    String show= TBookUtil.getTimeFromS(result);                                        // 转换为时间字符串
                    int delay= (int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1);    // 转换为皮秒
                    if(-Channel.MAX_DELAY_PS > delay || delay > Channel.MAX_DELAY_PS) {                // 超出延迟范围
                       // show="100 ns";
                        show="500 ns";                                                                   // 限制为500ns
                    }

                    chDelay.setText(show);                                                              // 设置延迟文本
                    msgChannel.setDelay(show);                                                          // 更新消息延迟
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber, show);        // 缓存延迟
                    Channel channel = ChannelFactory.getDynamicChannel(chIndex);                        // 获取通道实例
                    if(channel != null){                                                                // 通道非空
                        channel.setDelay((int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1));   // 设置通道延迟（皮秒）
                    }
                }break;
                case CommandMsgToUI.FLAG_CHANNEL_OFFSET:{                                               // 偏移设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    double result = Double.parseDouble( params[1]);                                     // 偏移值
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过

                    Channel channel = ChannelFactory.getDynamicChannel(chIndex);                        // 获取通道实例
                    String unit = ChannelFactory.getProbeType(channel.getChId());                       // 获取探头类型单位
                    String show = TBookUtil.getMFromDouble(Math.abs(result));                           // 转换为带单位值
                    if(result < 0) show = "-" + show;                                                   // 负值加负号
                    if (show.equals("")){                                                               // 空值处理
                        show="0 "+unit;
                    }else {
                        show+=unit;                                                                     // 添加单位
                    }
                    chOffset.setText(show);                                                             // 设置偏移文本
                    if (channel!=null) {                                                                // 通道非空
                        channel.setChOffsetVal(result);                                                 // 设置通道偏移值
                    }
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);      // 缓存偏移

                }break;
                case CommandMsgToUI.FLAG_CHANNEL_VERNIER:{                                              // 微调开关设置
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);      // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                                          // 通道索引
                    boolean state = Boolean.parseBoolean( params[1]);                                   // 微调状态
                    if (chIndex != TChan.toFpgaChNo(channelNumber)) return;                             // 非当前通道则跳过

                    btnFineSwitch.setState(state);                                                      // 设置微调开关
                    chFineExtent.setEnabled(state);                                                     // 微调幅度可用性
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));   // 缓存微调状态
                    msgChannel.setFineSwitch(state);                                                    // 更新消息微调开关
                    sendMsgChannel(false);                                                              // 发送通道消息

                }break;
            }
        }
    };

    /**
     * 设置通道标签
     * @param chNo 通道编号（FPGA编号）
     * @param label 标签文本
     */
    public void setChannelLabel(int chNo, String label) {
        Channel channel = ChannelFactory.getDynamicChannel(chNo);                                      // 获取通道实例
        if(channel != null){                                                                            // 通道非空
            channel.setLabel(label);                                                                    // 设置标签
        }
    }

    /**
     * 开关UI操作处理
     * <p>根据开关控件ID分发：反相/通道开关/微调开关，
     * 更新缓存、发送Command、更新消息、发送通知</p>
     * @param view 开关控件
     * @param state 开关状态
     * @param isFromEventBus 是否来自EventBus
     */
    private void operationUI_ToggleState(MSwitchBox view, boolean state, boolean isFromEventBus) {
        if (view.getId() == btnInvert.getId()) {                                                        // 反相开关
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_INVERT + channelNumber, String.valueOf(state));  // 缓存反相状态
            Command.get().getChannel().Inverse(TChan.toFpgaChNo(channelNumber), state, false);         // 发送反相命令
            if (!isFromEventBus) {                                                                      // 非来自EventBus
                ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber)).setInvert(state);    // 更新通道反相
            }
            msgChannel.setInvert(state);                                                                // 更新消息反相
            setInvertLight(state);                                                                      // 更新反相指示灯
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        } else if (view.getId() == btnchCheck.getId()) {                                                // 通道开关
            if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {                 // 非XY模式
                CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_OPEN_STATE + channelNumber, String.valueOf(btnchCheck.isState()));   // 缓存开关状态
            }
            if (!btnchCheck.isState()) {                                                                // 关闭通道
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();                        // 隐藏所有对话框
            }
            msgChannel.setChCheck(btnchCheck.isState());                                                // 更新消息开关
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        }else if (view.getId()==btnFineSwitch.getId()){                                                 // 微调开关
//            chFineExtent.setEnabled(state);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE + channelNumber, String.valueOf(state));   // 缓存微调状态
            Command.get().getChannel().Vernier(TChan.toFpgaChNo(channelNumber),state,false);           // 发送微调命令
            msgChannel.setFineSwitch(state);                                                            // 更新消息微调开关
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        }
    }

    /**
     * 开关状态变更监听器
     * <p>开关状态变更时播放按键音，并调用operationUI_ToggleState处理</p>
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();                                                       // 播放按键音
            operationUI_ToggleState(view, state, false);                                                // 处理开关操作
        }
    };

    /**
     * 按钮点击监听器
     * <p>处理探头倍率/带宽编辑/探头校准/MDP信息/档位上/下等按钮点击</p>
     */
    private final OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();                                                       // 播放按键音
            ScreenUtil.getViewLocation(v);                                                              // 获取视图位置
            switch (v.getId()) {
                case R.id.probeMultiple:                                                                // 探头倍率点击
                    dialogProbeMultiple.setData(channelNumber, probeMultiple.getText().toString(), CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + channelNumber, onProbeMultipleDismissListener);   // 设置倍率对话框数据
                    break;
                case R.id.bandWidthEdit:                                                                // 带宽编辑点击
                    long maxBandWidth = getMaxBandWidth();                                              // 获取最大带宽
                    String s = rgBandWidth.getSelectItem().getText();                                   // 获取当前带宽文本
                    dialogBandWidthHz.setValue(s, bandWidthEdit.getText().toString()
                            , 30, maxBandWidth
                            , onBandWidthDismissListener);                                              // 设置带宽对话框数据
                    break;
                case R.id.probeTypeCall:                                                                // 探头校准按钮点击
                    //Logger.d(TAG,"ch:" + channelNumber);
                    double[] param = {channelNumber - 1, VerticalAxis.DANG_50mV};                       // 校准参数（通道索引, 50mV档位）
                    BaseProbe baseProbe = ChannelFactory.getDynamicChannel(channelNumber - 1).getProbe();   // 获取探头
                    if (baseProbe != null && baseProbe.isDa()) {                                        // 探头非空且支持DA
                        ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();                   // 获取校准实例
                        if (!probeCalibrate.isCalibrate()) {                                            // 未在校准中
                            ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();            // 隐藏所有对话框
                            workMode = WorkModeManage.getInstance().getmWorkMode();                     // 保存当前工作模式
                            if (workMode != IWorkMode.WorkMode_YT) {                                    // 非YT模式
                                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false); // 切换到YT模式
                            }
                            ProbeCalibrate.getInstance().begin(param);                                  // 开始校准
                            lockScreen();                                                               // 锁屏
                        }
                    }
                    break;
                case R.id.mdp700Info:                                                                   // MDP700信息按钮
                case R.id.mrcpProbeTypeCall:                                                            // MRCP校准按钮
                    Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);              // 获取通道实例
                    if (channel == null) return;                                                        // 通道为空则返回
                    int type = getProbeType(channel);                                                   // 获取探头类型
                    dialogProbeInterface.show(channel, type);                                           // 显示探头接口对话框
                    break;
                case R.id.btnTop:                                                                       // 档位上调节
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);            // 显示向上箭头背景
                    msgChannel.setUpClick(true);                                                        // 设置向上标记
                    postChange();                                                                       // 发送档位变更
                    break;
                case R.id.btnBottom:                                                                    // 档位下调节
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);            // 显示向下箭头背景
                    msgChannel.setUpClick(false);                                                       // 设置向下标记
                    postChange();                                                                       // 发送档位变更
                    break;
            }
        }
    };

    /**
     * 延迟发送档位变更消息
     * <p>先移除已有消息，200ms后发送，实现去抖动效果</p>
     */
    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {                                                       // 已有待发送消息
            myHandler.removeMessages(HANDLE_MSG);                                                      // 移除旧消息
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);                                            // 延迟200ms发送
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_CHX, msgChannel); // 发送消息到消息队列
    }

    /** 保存校准前的工作模式 */
    int workMode = IWorkMode.WorkMode_YT;

    /**
     * 锁屏（探头校准/校零期间）
     * <p>使用通道号移位作为锁屏标志</p>
     */
    private void lockScreen() {

        ScreenControls screenControls = ScreenControls.getInstance();                                   // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROBE << (channelNumber-1));                      // 锁屏（按通道号移位）
    }

    /**
     * 解锁屏幕
     * <p>校准/校零结束后解锁</p>
     */
    private void unLockScreen() {
        ScreenControls screenControls = ScreenControls.getInstance();                                   // 获取屏幕控制实例
        screenControls.unLockScreen(ScreenControls.LOCK_PROBE << (channelNumber-1));                    // 解锁
    }

    /**
     * 带宽对话框关闭监听器
     * <p>对话框关闭时更新带宽设置和FPGA</p>
     */
    private DialogBandWidthHz.OnDismissListener onBandWidthDismissListener = new DialogBandWidthHz.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
//            PlaySound.getInstance().playButton();
            if (!isEnableHighLowFilter() && (rgBandWidth.getSelectIndex() == 3 || rgBandWidth.getSelectIndex() == 4)) {   // 未启用高通/低通
                return;
            }
            bandSwitchChange(result);                                                                   // 执行带宽切换
        }

        /**
         * 带宽切换处理
         * @param fb 频率字符串
         */
        public void bandSwitchChange(String fb) {
            long probeBandWidth= getProbeBandWidth();                                                   // 获取探头带宽
            long scopeBandwidth=(long) (TBookUtil.getMHzFromHz(fb)*1e6);                               // 转换为Hz
            double bandWidth= scopeBandwidth>=probeBandWidth?probeBandWidth:scopeBandwidth;             // 取较小值
            setBandWidth(fb);                                                                           // 设置带宽显示
            sendChannelBandWidth(bandWidth);                                                            // 发送带宽到通道
        }
    };

    /**
     * 选择项点击处理
     * <p>根据控件ID分发：耦合/探头类型/带宽/垂直基准/阻抗/MDP700倍率，
     * 更新缓存、发送Command、更新Channel、发送消息</p>
     * @param viewId 控件ID
     * @param item 选中的选项Bean
     * @param isFromEventBus 是否来自EventBus
     */
    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("RightLayoutChannel", rootViewGroup);                               // 打印控件位置（调试用）
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
        if(channel == null) return;                                                                     // 通道为空则返回
        if (viewId == rgCouple.getId()) {                                                               // 耦合方式
            //设置耦合方式
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_COUPLE + channelNumber, String.valueOf(item.getIndex()));   // 缓存耦合索引
            Command.get().getChannel().Couple(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);   // 发送耦合命令
            if (!isFromEventBus) {                                                                      // 非来自EventBus
//                int resistanceType = Channel.RESISTANCE_1M;
//                int coupleType = Channel.COUPLE_TYPE_DC;
//                switch(item.getIndex()){
//                    case 1:
//                        coupleType = Channel.COUPLE_TYPE_AC;
//                        break;
//                    case 2:
//                        resistanceType = Channel.RESISTANCE_50;
//                        break;
//                }
                channel.setCoupleType(matchCouple(item.getIndex()));                                    // 设置通道耦合类型
//                channel.setResistanceType(resistanceType);
            }
            this.rgImped.setEnabled(1, matchCouple(item.getIndex()) != Channel.COUPLE_TYPE_AC);//选择AC时50Ω置灰
            msgChannel.setCouple(item);                                                                 // 更新消息耦合
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, String.valueOf(item.getIndex()));
//            Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);
//            msgChannel.setImped(item);

            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        } else if (viewId == rgProbeType.getId()) {                                                    // 探头类型
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + channelNumber, String.valueOf(item.getIndex()));   // 缓存探头类型
            Command.get().getChannel().Prty(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);  // 发送探头类型命令
            if (!isFromEventBus) {                                                                      // 非来自EventBus
                channel.setProbeType(item.getIndex());                                                  // 设置通道探头类型
            }
            msgChannel.setProbeType(item);                                                              // 更新消息探头类型
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
            if (chOffset.getText().equals("")!=true){                                                   // 偏移非空
                String text=chOffset.getText();                                                         // 获取偏移文本
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";                // 根据探头类型选择单位
                text= text.replace("A",unit).replace("V",unit);                                         // 替换单位
                chOffset.setText(text);                                                                 // 更新偏移文本
            }
            if (chPosition.getText().equals("")!=true){                                                 // 位置非空
                String text=chPosition.getText();                                                       // 获取位置文本
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";                // 根据探头类型选择单位
                text= text.replace("A",unit).replace("V",unit);                                         // 替换单位
                chPosition.setText(text);                                                               // 更新位置文本
            }
            refreshFineExtent();                                                                        // 刷新微调幅度
        } else if (viewId == rgBandWidth.getId()) {                                                    // 带宽限制
            if (!isEnableHighLowFilter() && (item.getIndex() == 3 || item.getIndex() == 4)) {          // 未启用高通/低通
                return;
            }
            setProbeBandWidth_Change(channel.getProbe());                                               // 设置探头带宽变更

            double hz = TBookUtil.getMHzFromHz(bandWidthEdit.getText().toString());                     // 获取带宽频率值
            Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), item.getIndex(), hz, false);   // 发送带宽命令

            switch (item.getIndex()) {                                                                  // 根据带宽索引
                case 0://Full
                    bandWidthEdit.setEnabled(false);                                                    // 禁用带宽编辑
                    bandWidthEdit.setText("");                                                          // 清空文本
                    if (!isFromEventBus) {                                                              // 非来自EventBus
                        double d = getBandWidthCurScopeCurProbe();                                      // 获取当前带宽
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, d);                       // 设置全带宽
                    }
                    break;
                case 1://200M
                    bandWidthEdit.setEnabled(false);                                                    // 禁用带宽编辑
                    bandWidthEdit.setText("");                                                          // 清空文本
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();                                      // 获取当前带宽
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, d);                       // 设置200M带宽
                    }
                    break;
                case 2://20M
                    bandWidthEdit.setEnabled(false);                                                    // 禁用带宽编辑
                    bandWidthEdit.setText("");                                                          // 清空文本
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();                                      // 获取当前带宽
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, d);                        // 设置20M带宽
                    }
                    break;
                case 3://Highpass
                    bandWidthEdit.setEnabled(true);                                                     // 启用带宽编辑
                    bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber));   // 显示高通频率
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, bandWidthEdit.getText().toString());   // 缓存高通频率
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();                                      // 获取当前带宽
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, d);                   // 设置高通带宽
                    }
                    break;
                case 4://Lowpass
                    bandWidthEdit.setEnabled(true);                                                     // 启用带宽编辑
                    bandWidthEdit.setText(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber));    // 显示低通频率
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, bandWidthEdit.getText().toString());    // 缓存低通频率
                    if (!isFromEventBus) {
                        double d = getBandWidthCurScopeCurProbe();                                      // 获取当前带宽
                        channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, d);                    // 设置低通带宽
                    }
                    break;
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH + channelNumber, String.valueOf(item.getIndex()));   // 缓存带宽索引
            msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());                            // 更新消息带宽编辑值
            msgChannel.setBandWidth(item);                                                              // 更新消息带宽
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        } else if (viewId == rgVerBase.getId()) {                                                      // 垂直基准
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE + channelNumber, String.valueOf(item.getIndex()));   // 缓存垂直基准
            Command.get().getChannel().Vref(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);  // 发送垂直基准命令
            if (!isFromEventBus) {                                                                      // 非来自EventBus
                channel.setVerticalMode(item.getIndex());                                               // 设置通道垂直模式
            }
        } else if (viewId == rgImped.getId()) {                                                        // 阻抗
            int resistanceType = Channel.RESISTANCE_1M;                                                 // 默认1MΩ
            if (!isFromEventBus) {                                                                      // 非来自EventBus
                if (item.getIndex() == 0) {                                                             // 1MΩ
                    resistanceType = Channel.RESISTANCE_1M;
                } else {                                                                                // 50Ω
                    resistanceType = Channel.RESISTANCE_50;
                }
                Log.d(TAG,"调整----------------------------------resistanceType:"+resistanceType);       // 调试日志
                channel.setResistanceType(resistanceType);                                              // 设置通道阻抗
            }
            this.rgCouple.setEnabled(1, resistanceType != Channel.RESISTANCE_50);//选择50Ω时AC置灰
            item = rgImped.getSelectItem();                                                             // 重新获取选中项
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_IMPED + channelNumber, String.valueOf(item.getIndex()));   // 缓存阻抗
            Command.get().getChannel().Inputres(TChan.toFpgaChNo(channelNumber), item.getIndex(), false);   // 发送阻抗命令
            msgChannel.setImped(item);                                                                  // 更新消息阻抗
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
        } else if (viewId == mdp700Select.getId()) {                                                   // MDP700倍率选择
            String result= mdp700Select.getSelectItem().getText().toString();                           // 获取选中倍率文本
            if (StrUtil.isEmpty(result)) return;                                                        // 空值则返回
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, result);     // 缓存倍率
            probeMultiple.setText(result);                                                              // 设置倍率文本
            double dResult = TBookUtil.getDoubleFromX(result);                                          // 转换为数值
            Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), dResult, false);          // 发送倍率命令
            if (!isFromEventBus) {                                                                      // 非来自EventBus
                channel.setProbeRate(TBookUtil.getDoubleFromX(result));                                 // 设置通道倍率
            }
            msgChannel.setProbeMultiple(probeMultiple.getText().toString());                            // 更新消息倍率
            msgChannel.setProbeMulScale(TBookUtil.getDoubleFromX(preProbeMul) / TBookUtil.getDoubleFromX(result));   // 计算倍率比例
            sendMsgChannel(isFromEventBus);                                                             // 发送通道消息
            preProbeMul = result;                                                                       // 更新前次倍率

            refreshFineExtent();                                                                        // 刷新微调幅度
        }
    }

    /**
     * 选择项点击监听器
     * <p>转发给onItemClick方法处理</p>
     */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();                                                       // 播放按键音
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            RightLayoutChannel.this.onItemClick(viewId, item, false);                                   // 转发给处理方法
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    /**
     * 探头倍率文本点击处理
     * <p>格式化倍率字符串（去除尾零），发送Command和更新消息</p>
     * @param result 倍率字符串（如"10X"）
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextClick(String result, boolean isFromEventBus) {
        result = result.replace("X", "");                                                               // 去除X后缀
        if (result.contains(".")) {                                                                     // 包含小数点
            while (result.endsWith("0")) {                                                              // 去除尾零
                result = result.substring(0, result.length() - 1);
            }
            if (result.endsWith(".")) {                                                                 // 去除末尾小数点
                result = result.substring(0, result.length() - 1);
            }
        }
        result = result + "X";                                                                          // 添加X后缀

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + channelNumber, result);         // 缓存倍率
        probeMultiple.setText(result);                                                                  // 设置倍率文本
        double dResult = TBookUtil.getDoubleFromX(result);                                              // 转换为数值
        Command.get().getChannel().Probe(TChan.toFpgaChNo(channelNumber), dResult, false);              // 发送倍率命令
        if (!isFromEventBus) {                                                                          // 非来自EventBus
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));        // 获取通道实例
            channel.setProbeRate(TBookUtil.getDoubleFromX(result));                                     // 设置通道倍率
        }
        msgChannel.setProbeMultiple(probeMultiple.getText().toString());                                // 更新消息倍率
        msgChannel.setProbeMulScale(TBookUtil.getDoubleFromX(preProbeMul) / TBookUtil.getDoubleFromX(result));   // 计算倍率比例
        sendMsgChannel(isFromEventBus);                                                                 // 发送通道消息
        preProbeMul = result;                                                                           // 更新前次倍率

        refreshFineExtent();                                                                            // 刷新微调幅度

    }

    /**
     * 发送通道带宽到Channel数据模型
     * <p>根据带宽选中项设置通道的带宽类型和值</p>
     * @param d 带宽值（Hz）
     */
    private void sendChannelBandWidth(double d) {
        Channel channel = ChannelFactory.getDynamicChannel(channelNumber - 1);                          // 获取通道实例
        if (channel == null) return;                                                                    // 通道为空则返回
        switch (rgBandWidth.getSelectIndex()) {                                                         // 根据带宽选中项
            case 0:                                                                                     // Full
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_FULL, d);
                break;
            case 1:                                                                                     // 200M
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_200M, d);
                break;
            case 2:                                                                                     // 20M
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_20M, d);
                break;
            case 3:                                                                                     // Highpass
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_HIGHPASS, d);
                break;
            case 4:                                                                                     // Lowpass
                channel.setBandWidthType(Channel.BANDWIDTH_TYPE_LOWPASS, d);
                break;
        }
    }

    /**
     * 刷新微调幅度显示
     * <p>根据通道VScale值和探头类型更新微调幅度文本</p>
     */
    private void refreshFineExtent(){
        Channel channel = ChannelFactory.getDynamicChannel(msgChannel.getChannelNumber() - 1);          // 获取通道实例
        String s=TBookUtil.getMFromDouble(channel.getVScaleVal());                                      // 转换VScale为带单位值
        String unit= channel.getProbeType()==0?"V":"A";                                                 // 根据探头类型选择单位
        chFineExtent.setText(s+unit);                                                                   // 设置微调幅度文本
    }

    /**
     * 编辑控件点击监听器
     * <p>处理标签/延迟/偏移/微调幅度/位置的编辑点击，
     * 弹出对应的输入对话框</p>
     */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();                                                       // 播放按键音
            if (v.getId() == chLabel.getId()) {                                                         // 标签编辑
                if (dialogChannelLabel == null) {                                                       // 对话框未初始化
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel);   // 获取标签对话框
                }
                dialogChannelLabel.setData(channelNumber, chLabel.getText()
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + channelNumber
                        , DialogChannelLabel.FROM_CHANNEL
                        , result -> {
                            PlaySound.getInstance().playButton();                                       // 播放按键音
                            chLabel.setText(result);                                                    // 设置标签文本
                            msgChannel.setLabel(result);                                                // 更新消息标签
                            Command.get().getChannel().Label(channelNumber - 1, result, false);         // 发送标签命令
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + channelNumber, result);   // 缓存标签
                            WaveManage.get().setChannelLabel(channelNumber, result);                    // 设置波形标签
                            setChannelLabel(TChan.toFpgaChNo(channelNumber), result);                   // 设置通道标签
                        });
            } else if (v.getId() == chDelay.getId()) {                                                  // 延迟编辑
                if (dialogFloatKeyBoard == null) {                                                      // 对话框未初始化
                    dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);   // 获取浮动键盘
                }
                dialogFloatKeyBoard.setFloatpnData(chDelay.getText().replace("s", "").replace(" ",""), chDelay, new TopDialogFloatKeyBoard.OnDismissListener() {   // 设置浮点数据
                    @Override
                    public void onDismiss(View fromView, String show) {
//                        boolean isP = chDelay.getText().contains("p");
                        PlaySound.getInstance().playButton();                                           // 播放按键音
                        if ("0".equals(show.trim())) {                                                  // 输入为0
//                            show = isP ? "0 ps" : "0 ns";
                            show = "0 ns";                                                               // 默认0ns
                        } else {
                            show = show + "s";                                                           // 添加秒单位后缀
                        }
                        int delay= (int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1);    // 转换为皮秒
                        if (delay > Channel.MAX_DELAY_PS) {                                             // 超出最大延迟
                            //show="100 ns";
                            show="500 ns";                                                               // 限制为500ns
                        }
                        if (-Channel.MAX_DELAY_PS > delay) {                                            // 超出最小延迟
                            //show="-100 ns";
                            show="-500 ns";                                                              // 限制为-500ns
                        }
                        chDelay.setText(show);                                                          // 设置延迟文本
                        msgChannel.setDelay(show);                                                      // 更新消息延迟
                        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + channelNumber, show);    // 缓存延迟
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));   // 获取通道实例
                        if(channel != null){                                                            // 通道非空
                            channel.setDelay((int)(TBookUtil.getDoubleFromM(show.replace("s", "")) * 1e12 + 0.1));   // 设置通道延迟
                        }
                        double _delay=TBookUtil.getDoubleFromM(show.replace("s",""));                   // 转换为秒
                        Command.get().getChannel().Delay(TChan.toFpgaChNo(channelNumber),_delay,false); // 发送延迟命令


                    }
                });
            } else if (v.getId() == chOffset.getId()) {                                                 // 偏移编辑
                if (WorkModeManage.getInstance().isXyMode()) return;                                    // XY模式下不允许编辑偏移
                setChOffset(chOffset);                                                                  // 弹出偏移输入对话框

            }else if (v.getId()==chFineExtent.getId()){                                                 // 微调幅度编辑
                fineExtentDialog(chFineExtent);                                                         // 弹出微调幅度输入对话框
            } else if (v.getId() == chPosition.getId()){                                                // 位置编辑
                if (WorkModeManage.getInstance().isXyMode()) return;                                    // XY模式下不允许编辑位置
                setChPosition(chPosition);                                                              // 弹出位置输入对话框
            }
        }
    };

    /**
     * 设置通道偏移输入对话框
     * <p>弹出浮动键盘输入偏移值，确认后更新通道偏移</p>
     * @param topViewEdit 偏移编辑控件
     */
    private void setChOffset(TopViewEdit topViewEdit) {
        if (dialogFloatKeyBoard == null) {                                                              // 对话框未初始化
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);      // 获取浮动键盘
        }
        dialogFloatKeyBoard.setFloatData(chOffset.getText().replace("A","").replace("V","").replace(" ",""), chOffset, new TopDialogFloatKeyBoard.OnDismissListener() {   // 设置浮点数据
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                                   // 播放按键音
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));   // 获取通道实例
                if(channel != null){                                                                    // 通道非空
                    String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";            // 根据探头类型选择单位
                    double val = TBookUtil.getDoubleFromM(show);                                        // 转换为数值
                    channel.setChOffsetVal(val);                                                        // 设置通道偏移值
                    onRefreshOffset(channel);                                                           // 刷新偏移显示
                    Command.get().getChannel().Offset(TChan.toFpgaChNo(channelNumber),val,false);       // 发送偏移命令
                }
            }
        });
    }

    /**
     * 设置通道位置输入对话框
     * <p>弹出浮动键盘输入位置值，确认后更新通道位置</p>
     * @param topViewEdit 位置编辑控件
     */
    private void setChPosition(TopViewEdit topViewEdit) {
        if (dialogFloatKeyBoard == null) {                                                              // 对话框未初始化
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);      // 获取浮动键盘
        }
        dialogFloatKeyBoard.setFloatData_Offset(topViewEdit.getText().replace("A", "").replace("V", "").replace(" ", ""), true, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {   // 设置偏移数据
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                                   // 播放按键音
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));   // 获取通道实例
                if(channel != null){                                                                    // 通道非空
                    String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";            // 根据探头类型选择单位
                    double d = channel.getVerticalPerPix();                                             // 获取每像素垂直值
                    double val = TBookUtil.getDoubleFromM(show);                                        // 转换输入为数值
                    double offsetPix = (Tools.isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2.0 - (val / d);   // 计算像素偏移
//                    Tools.putYTChannelPosition(channelNumber,offsetPix);
//                    double y = Tools.getYTChannelPosition((channelNumber));
//                    channel.setPos(y);

                    CursorManage.getInstance().setScpiChanIdx(channelNumber);                           // 设置SCPI通道索引
                    CursorManage.getInstance().setCursorTrace(true);                                    // 启用光标追踪
                    WaveManage.get().setPositionY(channelNumber,offsetPix);                             // 设置波形位置Y
                    CursorManage.setCursorByScaleTrace();                                               // 按档位追踪设置光标
                    CursorManage.getInstance().setCursorTrace(false);                                   // 禁用光标追踪
//                    Command.get().getChannel().Position(TChan.toFpgaChNo(channelNumber),offsetPix,false);

                    onRefreshPosition(channel);                                                         // 刷新位置显示
                }
            }
        });

    }


    /**
     * 微调幅度输入对话框
     * <p>弹出浮动键盘输入微调幅度值，确认后更新通道VScale</p>
     * @param topViewEdit 微调幅度编辑控件
     */
    private void fineExtentDialog(TopViewEdit topViewEdit){
        if (dialogFloatKeyBoard == null) {                                                              // 对话框未初始化
            dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);      // 获取浮动键盘
        }
        String txt=topViewEdit.getText().replace("V", "").replace("A","").replace(" ","");              // 去除单位
        dialogFloatKeyBoard.setFloatData_Extent(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {   // 设置幅度数据
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                                   // 播放按键音
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));   // 获取通道实例
                String unit=channel.getProbeType()==VerticalAxis.PROBE_TYPE_VOL?"V":"A";                // 根据探头类型选择单位
                topViewEdit.setText(show+unit);                                                         // 设置微调幅度文本
                msgChannel.setFineExtent(show+unit);                                                    // 更新消息微调幅度
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT + channelNumber, show);      // 缓存微调幅度
                double d=TBookUtil.getDoubleFromM(show);                                                // 转换为数值
                d= getVerticalRange(d,channel);                                                         // 获取有效档位值
                channel.setVScaleVal(d);                                                                // 设置通道VScale值

//                CommandMsgToUI msgToUI = Command.get().getMsgToUI();
//                msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_EXTENT);
//                String param = String.valueOf(channel.getChId()) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d);
//                msgToUI.setParam(param);
//                RxBus.getInstance().post(RxSendBean.COMMAND_TO_UI, msgToUI);
            }
        });
    }

    /**
     * 返回有效档位值
     * <p>限制输入值在通道最小/最大档位范围内</p>
     * @param input 输入档位值
     * @param chan 通道实例
     * @return 限制后的有效档位值
     */
    private double getVerticalRange(double input,Channel chan){
        double min=0;                                                                                   // 最小档位值
        double max=0;                                                                                   // 最大档位值

        min= VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear())*chan.getProbeRate();             // 计算最小档位值（含探头倍率）
        max=VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear());                                  // 获取最大档位值
        if (chan.getResistanceType()==Channel.RESISTANCE_50){                                           // 50Ω阻抗
            max=VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1V);                                   // 最大1V档位
        }
        max*=chan.getProbeRate();                                                                       // 乘以探头倍率

        if (input<min){                                                                                 // 低于最小值
            input=min;                                                                                  // 限制为最小值
        }
        if (input>max){                                                                                 // 高于最大值
            input=max;                                                                                  // 限制为最大值
        }
        return input;

    }

    /**
     * 探头倍率对话框关闭监听器
     * <p>对话框关闭时触发倍率选择处理</p>
     */
    private DialogProbeMultiple.OnDismissListener onProbeMultipleDismissListener = new DialogProbeMultiple.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            PlaySound.getInstance().playButton();                                                       // 播放按键音
            onTextClick(result, false);                                                                 // 触发倍率选择
        }
    };


    /**
     * 设置带宽（字符串参数，对话框关闭时调用）
     * <p>仅对Highpass和Lowpass有效，更新带宽编辑值和缓存</p>
     * @param fb 带宽频率字符串
     */
    private void setBandWidth(String fb) { //DialogBandWidthHz dismiss时候设置值用，所以只会有highpass 和 lowpass
        double hz = TBookUtil.getMHzFromHz(bandWidthEdit.getText().toString());                         // 获取当前带宽频率
        Command.get().getChannel().Band(TChan.toFpgaChNo(channelNumber), rgBandWidth.getSelectIndex(), hz, false);   // 发送带宽命令
        switch (rgBandWidth.getSelectIndex()) {                                                         // 根据带宽选中项
            case 0:                                                                                     // Full
            case 1:                                                                                     // 200M
            case 2:                                                                                     // 20M
                break;                                                                                  // 这些选项不需要编辑
            case 3:                                                                                     // Highpass
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, fb);   // 缓存高通频率
                break;
            case 4:                                                                                     // Lowpass
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, fb);    // 缓存低通频率
                break;
        }
        bandWidthEdit.setText(fb);                                                                      // 设置带宽编辑文本
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());                                // 更新消息带宽编辑值
        sendMsgChannel(false);                                                                          // 发送通道消息
    }

    /**
     * 设置带宽（double参数，命令到UI时调用）
     * <p>根据带宽Hz值更新带宽编辑文本和Channel数据模型</p>
     * @param Hz 带宽值（Hz）
     */
    private void setBandWidth(double Hz) {
        String fb = TBookUtil.getHz3FromHz(Hz);                                                        // 转换为频率字符串
        int bandWidthType = Channel.BANDWIDTH_TYPE_FULL;                                                // 默认带宽类型
        switch (rgBandWidth.getSelectIndex()) {                                                         // 根据带宽选中项
            case 0:                                                                                     // Full
                bandWidthType = Channel.BANDWIDTH_TYPE_FULL;
                break;
            case 1:                                                                                     // 200M
                bandWidthType = Channel.BANDWIDTH_TYPE_200M;
                break;
            case 2:                                                                                     // 20M
                bandWidthType = Channel.BANDWIDTH_TYPE_20M;
                break;
            case 3:                                                                                     // Highpass
                bandWidthEdit.setEnabled(true);                                                         // 启用带宽编辑
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + channelNumber, fb);   // 缓存高通频率
                break;
            case 4:                                                                                     // Lowpass
                bandWidthEdit.setEnabled(true);                                                         // 启用带宽编辑
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + channelNumber, fb);    // 缓存低通频率
                break;
        }
        bandWidthEdit.setText(fb);                                                                      // 设置带宽编辑文本
        msgChannel.setBandWidthEdit(bandWidthEdit.getText().toString());                                // 更新消息带宽编辑值
        sendMsgChannel(false);                                                                          // 发送通道消息
        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
        if (channel != null) {                                                                          // 通道非空
            channel.setBandWidthType(bandWidthType, Hz);                                                // 设置通道带宽类型和值
        }
    }

    /**
     * 显示模式事件观察者
     * <p>监听EVENT_DISPLAY_MODE事件，XY模式下禁用垂直基准，非XY模式下启用</p>
     */
    private Observer eventObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {

            EventBase eventBase = (EventBase)(data);                                                    // 转换事件数据
            int evId = eventBase.getId();                                                               // 获取事件ID

            // Logger.d(TAG,"evId:" + evId);
            if (evId == EventFactory.EVENT_DISPLAY_MODE) {                                              // 显示模式变更
                if (eventBase.getData() != null) {                                                      // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                              // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                               // 非当前通道则跳过
                }
                rgVerBase.post(new Runnable() {                                                         // 在UI线程执行
                    @Override
                    public void run() {
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));   // 获取通道实例
                        if (Display.getInstance().isXYMode()) {                                         // XY模式
                            channel.setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);                     // 设置为零模式
                            if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {               // 垂直基准不一致
                                rgVerBase.setSelectIndex(channel.getVerticalMode());                     // 更新垂直基准
                                onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);         // 触发回调
                            }
                            rgVerBase.setEnabled(false);                                                 // 禁用垂直基准
                        } else {                                                                        // 非XY模式
                            rgVerBase.setEnabled(true);                                                  // 启用垂直基准
                        }
                    }
                });
            }
        }
    };

    /**
     * 通道活动变化回调
     * <p>当通道被激活时更新探头UI</p>
     * @param obj 消息对象
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);                                                  // 解析消息枚举
        if (mqEnum==MQEnum.CH_OPEN){                                                                    // 通道打开
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));        // 获取通道实例
            int chIdx = ((MsgChOpenClose)obj).getChan().getValue();                                     // 获取通道编号
            if(chIdx == channel.getChId()) {                                                            // 是当前通道
                updateProbeUI(channel,true);                                                            // 更新探头UI
            }
        }
    }

    /**
     * 事件UI观察者
     * <p>处理约16种EventFactory事件ID：阻抗变更/带宽变更/反相变更/耦合变更/
     * 垂直档位变更/偏移变更/垂直模式变更/自动设置开始/停止/探头事件/
     * 探头校准结束/探头校零/探头升级/探头报警/通道开关等</p>
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));           // 获取通道实例
            if(channel == null) return;                                                                    // 通道为空则返回
            EventBase eventBase = (EventBase) data;                                                       // 转换事件数据
            int evId = eventBase.getId();                                                                 // 获取事件ID
            if (evId == EventFactory.EVENT_CHANNEL_RESISTANCETYPE) {                                      // 阻抗类型变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                if (rgImped.getSelectItem().getIndex() != channel.getResistanceType() && channel.getProbe() == null) {   // 阻抗不一致且无探头
                    rgImped.setSelectIndex(channel.getResistanceType());                                   // 更新阻抗选中项
                    onItemClick(rgImped.getId(), rgImped.getSelectItem(), true);                           // 触发回调
                } else {                                                                                  // 有探头
                    updateProbeUI(channel, true);                                                         // 更新探头UI
                }
            } else if (evId == EventFactory.EVENT_CHANNEL_BANDWIDTH) {                                   // 带宽变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                switch (channel.getBandWidthType()) {                                                     // 根据带宽类型
                    case Channel.BANDWIDTH_TYPE_FULL:                                                     // Full
                        if (rgBandWidth.getSelectIndex() != 0) {                                          // 当前非Full
                            rgBandWidth.setSelectIndex(0);                                                // 设置为Full
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);           // 触发回调
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_200M:                                                     // 200M
                        if (rgBandWidth.getSelectIndex() != 1) {                                          // 当前非200M
                            rgBandWidth.setSelectIndex(1);                                                // 设置为200M
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);           // 触发回调
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_20M:                                                      // 20M
                        if (rgBandWidth.getSelectIndex() != 2) {                                          // 当前非20M
                            rgBandWidth.setSelectIndex(2);                                                // 设置为20M
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);           // 触发回调
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_HIGHPASS:                                                 // Highpass
                        if (rgBandWidth.getSelectIndex() != 3) {                                          // 当前非Highpass
                            rgBandWidth.setSelectIndex(3);                                                // 设置为Highpass
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);           // 触发回调
                        }
                        break;
                    case Channel.BANDWIDTH_TYPE_LOWPASS:                                                  // Lowpass
                        if (rgBandWidth.getSelectIndex() != 4) {                                          // 当前非Lowpass
                            rgBandWidth.setSelectIndex(4);                                                // 设置为Lowpass
                            onItemClick(rgBandWidth.getId(), rgBandWidth.getSelectItem(), true);           // 触发回调
                        }
                        break;
                }
            } else if (((EventBase) data).getId() == EventFactory.EVENT_CHANNEL_INVERT) {                 // 反相变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                if (btnInvert.isState() != channel.isInvert()) {                                          // 反相状态不一致
                    btnInvert.setState(channel.isInvert());                                                // 更新反相开关
                    operationUI_ToggleState(btnInvert, btnInvert.isState(), true);                         // 执行UI操作
                }
            } else if (evId == EventFactory.EVENT_CHANNEL_COUPLE) {                                      // 耦合方式变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                if (rgCouple.getSelectIndex() != unMatchCouple(channel.getCoupleType())) {                // 耦合不一致
                    rgCouple.setSelectIndex(unMatchCouple(channel.getCoupleType()));                       // 更新耦合选中项
                    onItemClick(rgCouple.getId(), rgCouple.getSelectItem(), true);                         // 触发回调
                }
            }
            else if(evId == EventFactory.EVENT_CHANNEL_OFFSET ){                                         // 偏移变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                onRefreshOffset(channel);                                                                 // 刷新偏移显示
            }
            else if (evId == EventFactory.EVENT_CHANNEL_VSCALE) {                                        // 垂直档位变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                if (channel.isOpen() && isLoadCache==false) {                                             // 通道开启且非加载缓存中
                    String probeRate = TBookUtil.getXFromDouble(channel.getProbeRate());                   // 获取倍率字符串
//                Logger.d(TAG, probeRate);
                    if (!probeMultiple.getText().toString().equals(probeRate)) {                           // 倍率不一致
                        probeMultiple.setText(probeRate);                                                  // 更新倍率文本
                        onTextClick(probeRate, true);                                                      // 触发倍率选择
                        refreshProbeUI(channel);                                                          // 刷新探头UI
                        preProbeMul = probeRate;                                                           // 更新前次倍率
                    }
                    onRefreshOffset(channel);                                                             // 刷新偏移显示
                }
            } else if (evId == EventFactory.EVENT_VERTICAL_MODE) {                                       // 垂直模式变更
                if (eventBase.getData() != null) {                                                        // 事件数据非空
                    int chIdx = (int) eventBase.getData();                                                // 获取通道索引
                    if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                 // 非当前通道则跳过
                }
                if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {                            // 垂直基准不一致
                    rgVerBase.setSelectIndex(channel.getVerticalMode());                                   // 更新垂直基准
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);                       // 触发回调
                }
            } else if (evId == EventFactory.EVENT_AUTO_START) {                                          // 自动设置开始
                if (rgVerBase.getSelectIndex() != channel.getVerticalMode()) {                            // 垂直基准不一致
                    rgVerBase.setSelectIndex(channel.getVerticalMode());                                   // 更新垂直基准
                    onItemClick(rgVerBase.getId(), rgVerBase.getSelectItem(), true);                       // 触发回调
                }
                rgVerBase.setEnabled(false);                                                              // 禁用垂直基准
            } else if (evId == EventFactory.EVENT_AUTO_STOP) {                                           // 自动设置结束
                rgVerBase.setEnabled(true);                                                               // 启用垂直基准
            }else if (evId == EventFactory.EVENT_PROBE_EVENT) {                                          // 探头事件
                if (channel.isOpen()) {                                                                   // 通道开启
                    int chIdx = (int) ((EventBase) data).getData();                                       // 获取通道索引
                    if (chIdx != channel.getChId()) return;                                               // 非当前通道则跳过
                    updateProbeUI(channel,true);                                                          // 更新探头UI
                }
            } else if (evId == EventFactory.EVENT_PROBE_CALIBRATE_END) {                                 // 探头校准结束
                ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();                             // 获取校准实例
                int code = probeCalibrate.getErrorCode();                                                 // 获取错误码
                switch (code) {
                    case 0:                                                                               // 校准成功
                        dialogOk.setData(R.string.probeCalibrationSuccessful, null, null);                // 显示成功提示
                        break;
                    case 1001: //超时
                        dialogOk.setData(R.string.probeAbnormal, null, null);                             // 显示超时提示
                        break;
                    case 705:                                                                             // 信号异常
                        dialogOk.setData(R.string.probeSignalAbnormal, null, null);                       // 显示信号异常提示
                        break;
                    default:                                                                              // 其他错误
                        dialogOk.setData(R.string.probeCalibrationFailed, null, null);                    // 显示校准失败提示
                        break;
                }

                if(workMode != IWorkMode.WorkMode_YT){                                                    // 非YT模式
                    WorkModeManage.getInstance().setWorkMode(workMode, false);                             // 恢复之前的工作模式
                    if(workMode == IWorkMode.WorkMode_YTZOOM){                                            // 之前是YTZOOM模式
                        Scope.getInstance().setZoom(true);                                                 // 恢复Zoom
                    }else if(workMode == IWorkMode.WorkMode_XY){                                          // 之前是XY模式
                        Display.getInstance().setDisplayMode(Display.DISPLAY_XY);                         // 恢复XY显示
                    }
                }
                Scope.getInstance().setRun(true);                                                         // 恢复运行
                unLockScreen();                                                                           // 解锁屏幕
            }else if (evId == EventFactory.EVENT_PROBE_ZERO){                                             // 探头校零


                ProbeNotifyInfo probeNotifyInfo = (ProbeNotifyInfo) eventBase.getData();                   // 获取探头通知信息
                if (probeNotifyInfo != null && probeNotifyInfo.chIdx == (channelNumber-1)) {              // 是当前通道

                    if (probeNotifyInfo.Id != ProbeNotifyInfo.ZERO_ING) {                                 // 非校零中
                        unLockScreen();                                                                   // 解锁屏幕
                        int code = probeNotifyInfo.Id;                                                    // 获取通知ID
                        switch (code) {
                            case ProbeNotifyInfo.ZERO_SUCCESS:                                            // 校零成功
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeCalibrationSuccessful), null, null);   // 显示成功提示
                                break;
                            case ProbeNotifyInfo.ZERO_FAIL1:                                              // 校零失败1
                            case ProbeNotifyInfo.ZERO_FAIL2:                                              // 校零失败2
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeCalibrationFailed), null, null);   // 显示失败提示
                                break;
                        }
                    }else{                                                                                // 校零中
                        lockScreen();                                                                     // 锁屏
                    }
                }
            }else if(evId == EventFactory.EVENT_PROBE_UPGRADE){                                          // 探头升级

                ProbeUpgradeInfo probeUpgradeInfo = (ProbeUpgradeInfo) eventBase.getData();               // 获取升级信息
                if(probeUpgradeInfo != null){                                                             // 升级信息非空
                    switch (probeUpgradeInfo.getState()){                                                 // 根据升级状态
                        case ProbeUpgradeInfo.UPGRADE_BEGIN:                                              // 升级开始
                            if(channel.getChId() == probeUpgradeInfo.getChIdx()) {                        // 是当前通道
                                dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgrade), null, null);   // 显示升级提示
                                lockScreen();                                                             // 锁屏
                            }
                            break;
                        case ProbeUpgradeInfo.UPGRADE_END:                                                // 升级结束
                            if(channel.getChId() == probeUpgradeInfo.getChIdx()) {                        // 是当前通道
                                if(probeUpgradeInfo.getProgress() == 100) {                               // 升级完成
                                    dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgradeSuccessful), null, null);   // 显示成功提示
                                }else{                                                                    // 升级中断
                                    dialogOk.setData("CH" + channelNumber + " " + getResources().getString(R.string.probeUpgradeAbort), null, null);   // 显示中断提示
                                }
                                unLockScreen();                                                           // 解锁屏幕
                            }
                            break;
                    }
                }
            }else if (evId==EventFactory.EVENT_CHANNEL_OPEN){                                             // 通道开关
//                EventBase eventBase = (EventBase) data;
//                int chIdx = (int)eventBase.getData();
//                if(chIdx == channel.getChId()) {
//                    updateProbeUI(channel,true);
//                }
            }else if (evId == EventFactory.EVENT_PROBE_ALARM){                                            // 探头报警

                ProbeNotifyInfo probeNotifyInfo = (ProbeNotifyInfo) eventBase.getData();                   // 获取探头通知信息
                if (probeNotifyInfo != null && probeNotifyInfo.chIdx == (channelNumber -1)) {             // 是当前通道
                    int chIdx = probeNotifyInfo.chIdx;                                                    // 通道索引
                    String s = "";                                                                        // 通道名称
                    switch (chIdx) {                                                                      // 根据通道索引获取名称
                        case ChannelFactory.CH1:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh1);
                            break;
                        case ChannelFactory.CH2:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh2);
                            break;
                        case ChannelFactory.CH3:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh3);
                            break;
                        case ChannelFactory.CH4:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh4);
                            break;
                        case ChannelFactory.CH5:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh5);
                            break;
                        case ChannelFactory.CH6:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh6);
                            break;
                        case ChannelFactory.CH7:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh7);
                            break;
                        case ChannelFactory.CH8:
                            s = getResources().getString(R.string.mainCenterRadioButtonCh8);
                            break;
                    }
                    int msgResId = 0;                                                                     // 报警消息资源ID
                    switch (probeNotifyInfo.Id) {                                                         // 根据报警类型
                        case ProbeNotifyInfo.ALARM_RANGE_OUT:                                             // 超出量程
                            msgResId = R.string.channelProbeAlarmMsg1RangeOut;
                            break;
                        case ProbeNotifyInfo.ALARM_BATTERY_LOW:                                           // 电池电量低
                            msgResId = R.string.channelProbeAlarmMsg2BatteryLow;
                            break;
                        case ProbeNotifyInfo.ALARM_COMM_ABNORMAL:                                         // 通信异常
                            msgResId = R.string.channelProbeAlarmMsg3CommAbnormal;
                            break;
                        case ProbeNotifyInfo.ALARM_50_SWITCH:                                             // 50Ω开关
                            msgResId = R.string.channelProbeAlarmMsg4FiftySwitch;
                            break;
                        case ProbeNotifyInfo.ALARM_CONV_HIGH_TEMP:                                        // 转换器高温
                            msgResId = R.string.channelProbeAlarmMsg5ConvHighTemp;
                            break;
                        case ProbeNotifyInfo.ALARM_COMP_HIGH_TEMP:                                        // 比较器高温
                            msgResId = R.string.channelProbeAlarmMsg6CompHighTemp;
                            break;
                        case ProbeNotifyInfo.ALARM_ACDC_ABNORMAL:                                         // AC/DC异常
                            msgResId = R.string.channelProbeAlarmMsg7AcDcAbnormal;
                            break;
                        case ProbeNotifyInfo.ALARM_REPLACE_BATTERY:                                       // 更换电池
                            msgResId = R.string.channelProbeAlarmMsgAreplaceBattery;
                            break;
                        case ProbeNotifyInfo.ALARM_MISMATCH:                                              // 不匹配
                            msgResId = R.string.channelProbeAlarmMsgBMismatch;
                            break;
                        case ProbeNotifyInfo.ALARM_ATTENUATOR_ERROR:                                      // 衰减器错误
                            msgResId = R.string.channelProbeAlarmMsgCAttenuator;
                            break;
                    }

                    if (msgResId != 0) {                                                                  // 有效的报警消息
                        dialogOk.setData(s + " " + getResources().getString(msgResId), null, null);        // 显示报警提示
                    }
                }
            }
        }
    };

    /**
     * 用户手动垂直档位变更观察者
     * <p>监听EVENT_CHANNEL_VSCALE_USER事件，更新微调幅度和位置显示</p>
     */
    private EventUIObserver OnChannelVscalUserChange=new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;                                                       // 转换事件数据
            if (eventBase.getData() != null) {                                                            // 事件数据非空
                int chIdx = (int) eventBase.getData();                                                    // 获取通道索引
                if (chIdx != TChan.toFpgaChNo(channelNumber)) return;                                     // 非当前通道则跳过
            }
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(channelNumber));         // 获取通道实例
            if (channel == null) return;                                                                   // 通道为空则返回
            String s = TBookUtil.getMFromDouble(channel.getVScaleVal());                                   // 转换VScale为带单位值
            String unit = channel.getProbeType() == VerticalAxis.PROBE_TYPE_VOL ? "V" : "A";              // 根据探头类型选择单位
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT + channelNumber, s);                // 缓存微调幅度
            s += unit;                                                                                     // 添加单位
            chFineExtent.setText(s);                                                                       // 设置微调幅度文本
            onRefreshPosition(channel);                                                                    // 刷新位置显示
        }
    };

    /**
     * 探头插入回调
     * <p>探头插入时保存当前参数</p>
     * @param observable 被观察者
     * @param data 事件数据
     */
    private void OnProbePlug(Observable observable, Object data) {
        int evId = ((EventBase) data).getId();                                                            // 获取事件ID
        int chIdx=(int)((EventBase) data).getData();                                                     // 获取通道索引
        if (chIdx==channelNumber-1) {                                                                    // 是当前通道
            saveParam();                                                                                  // 保存当前参数
        }
    }

    /**
     * 探头拔出回调
     * <p>探头拔出时恢复参数</p>
     * @param observable 被观察者
     * @param data 事件数据
     */
    private void OnProbeUnPlug(Observable observable, Object data) {
        int evId = ((EventBase) data).getId();                                                            // 获取事件ID
        int chIdx=(int)((EventBase) data).getData();                                                     // 获取通道索引
        if (chIdx==channelNumber-1) {                                                                    // 是当前通道
            recoverParam();                                                                               // 恢复参数

        }
    }


    /**
     * 刷新偏移显示
     * <p>根据通道偏移值和探头类型更新偏移文本</p>
     * @param channel 通道实例
     */
    private void onRefreshOffset(Channel channel){
        double val = channel.getChOffsetVal();                                                            // 获取偏移值
        String unit = ChannelFactory.getProbeType(channel.getChId());                                     // 获取探头类型单位

        String show = TBookUtil.getMFromDouble(Math.abs(val));                                            // 转换为带单位值
        if(val < 0) show = "-" + show;                                                                    // 负值加负号
        if (show.equals("")){                                                                             // 空值处理
            show="0 "+unit;
        }else {
            show+=unit;                                                                                   // 添加单位
        }
//        Log.d("00112233","val:" + val + ",show:" + show);
        chOffset.setText(show);                                                                           // 设置偏移文本
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);                     // 缓存偏移
    }

    /**
     * 刷新位置显示
     * <p>根据通道位置和垂直每像素值计算位置文本</p>
     * @param channel 通道实例
     */
    private void onRefreshPosition(Channel channel){
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);                                        // 缩放高度
        double leftPos = Tools.getChannelPositionUI(channel.getChId());                                   // 获取通道UI位置
        double val = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight())/2 - leftPos;         // 计算位置值
        String unit = ChannelFactory.getProbeType(channel.getChId());                                     // 获取探头类型单位
        String show = TBookUtil.getFourFromD_Trim0(val * channel.getVerticalPerPix());                    // 转换为位置字符串
        if (show.equals("")){                                                                             // 空值处理
            show="0 "+unit;
        }else {
            show+=unit;                                                                                   // 添加单位
        }
        if(positionString != show){                                                                       // 位置字符串不一致
            chPosition.setText(positionString);                                                           // 显示缓存的positionString
        }else {
            chPosition.setText(show);                                                                     // 显示新计算的值
        }
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_OFFSET + channelNumber, show);
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_POSITION + channelNumber, show);
    }

    /** Handler消息标识 */
    private static final int HANDLE_MSG = 1;

    /**
     * 弱引用Handler内部类
     * <p>使用WeakReference防止Activity泄漏，延迟200ms后恢复按钮背景图片</p>
     */
    public static class MyHandler extends Handler {
        /** 弱引用持有RightLayoutChannel实例 */
        private final WeakReference<RightLayoutChannel> rightLayoutHandler;

        /**
         * 构造方法
         * @param layoutChannel RightLayoutChannel实例
         */
        public MyHandler(RightLayoutChannel layoutChannel) {
            rightLayoutHandler = new WeakReference<RightLayoutChannel>(layoutChannel);                     // 创建弱引用
        }

        /**
         * 处理消息
         * <p>恢复按钮背景为默认状态</p>
         * @param msg 消息对象
         */
        @Override
        public void handleMessage(Message msg) {
            if (rightLayoutHandler.get() != null) {                                                       // 弱引用未回收
                if (msg.what == HANDLE_MSG) {                                                             // 档位变更消息
                    RightLayoutChannel layoutChannel = (RightLayoutChannel) rightLayoutHandler.get();      // 获取实例
                    layoutChannel.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174);  // 恢复默认背景
                }
            }
        }
    }

    /**
     * 鼠标点击位置消费者
     * <p>接收鼠标点击位置消息，根据点击位置（0=垂直档位, 1=垂直位置）
     * 弹出对应的输入对话框</p>
     */
    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");                                                         // 解析参数（通道索引;点击位置）
            int chIdx = Integer.parseInt(info[0]);                                                        // 通道索引
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);                       // 打印调试日志
            if (TChan.toUiChNo(chIdx) != channelNumber) return;                                          // 非当前通道则跳过
            if (clickPos == 0) { //垂直档位
                fineExtentDialog(chFineExtent);                                                           // 弹出微调幅度对话框
            }
            if (clickPos == 1) { //垂直位置
                setChPosition(chPosition);                                                                // 弹出位置输入对话框
            }
        }
    };

}