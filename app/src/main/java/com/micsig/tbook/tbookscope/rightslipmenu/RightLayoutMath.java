package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;                                                 // SuppressLint注解
import android.content.Context;                                                         // 上下文环境
import android.content.res.TypedArray;                                                  // XML自定义属性数组
import android.os.Handler;                                                              // 消息处理器
import android.os.Message;                                                              // 消息对象
import android.text.TextUtils;                                                          // 文本工具类
import android.util.AttributeSet;                                                       // XML属性集
import android.view.View;                                                               // 视图基类
import android.view.ViewGroup;                                                          // 视图组基类
import android.widget.Button;                                                           // 按钮控件
import android.widget.ImageView;                                                        // 图片视图
import android.widget.LinearLayout;                                                     // 线性布局
import android.widget.RadioButton;                                                      // 单选按钮
import android.widget.RelativeLayout;                                                   // 相对布局
import android.widget.TextView;                                                         // 文本视图

import androidx.constraintlayout.widget.ConstraintLayout;                               // 约束布局

import com.micsig.base.Logger;                                                          // 日志工具
import com.micsig.tbook.scope.Data.WaveData;                                            // 波形数据
import com.micsig.tbook.scope.Event.EventBase;                                          // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                       // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver;                                    // 事件UI观察者
import com.micsig.tbook.scope.ScopeBase;                                                // 示波器基类
import com.micsig.tbook.scope.channel.ChannelFactory;                                   // 通道工厂
import com.micsig.tbook.scope.channel.MathChannel;                                      // 数学通道
import com.micsig.tbook.scope.channel.RefChannel;                                       // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;                                // 水平轴
import com.micsig.tbook.scope.math.MathDualWave;                                        // 双波形数学运算
import com.micsig.tbook.scope.math.MathExprWave;                                        // 表达式数学运算（AX+B/高级数学）
import com.micsig.tbook.scope.math.MathFFTWave;                                         // FFT数学运算
import com.micsig.tbook.scope.math.MathWave;                                            // 数学波形基类
import com.micsig.tbook.tbookscope.GlobalVar;                                           // 全局变量
import com.micsig.tbook.tbookscope.MainActivity;                                        // 主Activity
import com.micsig.tbook.tbookscope.MainMsgSlip;                                         // 滑出菜单消息
import com.micsig.tbook.tbookscope.MainViewGroup;                                       // 主视图组
import com.micsig.tbook.tbookscope.R;                                                   // 资源ID
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;               // 底部时基消息
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;                    // 底部持有者
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;                   // 右侧其他消息
import com.micsig.tbook.tbookscope.middleware.command.Command;                           // 命令模式入口
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;                    // 命令消息转UI
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;              // 通道标签对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogMathFFTPersist;            // FFT持久化对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSelectColor;               // 颜色选择对话框
import com.micsig.tbook.tbookscope.rxjava.RxBus;                                       // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                                      // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound;                                     // 按键音效
import com.micsig.tbook.tbookscope.tools.Tools;                                         // 通用工具
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;                    // 顶部显示消息
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayFftInfo;              // FFT信息显示消息
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogNumberPicker;                  // 数字选择对话框
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;   // 浮点键盘对话框
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFullFloatKeyBoard; // 全精度浮点键盘
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.KeyBoardFormulaUtil;    // 公式键盘工具
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.TopDialogFormulaKeyBoard; // 公式键盘对话框
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;     // 文本键盘对话框
import com.micsig.tbook.tbookscope.util.App;                                            // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;                                      // 缓存工具
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;                       // 测量管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;                             // 波形管理
import com.micsig.tbook.ui.MSwitchBox;                                                  // 自定义开关控件
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;                                // 右侧选择项Bean
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;                                // 右侧选择视图
import com.micsig.tbook.ui.top.view.TopViewEdit;                                        // 顶部编辑视图
import com.micsig.tbook.ui.util.TBookUtil;                                              // 示波器工具类
import com.micsig.tbook.ui.util.svg.SelectorUtil;                                        // 颜色选择器工具
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;                                         // SVG节点信息（颜色管理）
import com.micsig.tbook.ui.wavezone.TChan;                                               // 通道常量

import java.lang.ref.WeakReference;                                                     // 弱引用
import java.util.concurrent.atomic.AtomicInteger;                                       // 原子整数

import io.reactivex.rxjava3.annotations.NonNull;                                        // 非空注解
import io.reactivex.rxjava3.functions.Consumer;                                         // RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          RightLayoutMath                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 数学通道参数设置面板                                  ║
 * ║ 核心职责：管理4种数学运算类型（双波形/FFT/AX+B/高级数学）的参数设置界面，       ║
 * ║          通过RxBus将参数变更发送给业务层，通过Command向FPGA发送控制指令          ║
 * ║ 架构设计：继承ConstraintLayout的自定义视图，采用4个子布局切换显示，             ║
 * ║          包含多种键盘对话框（文本/浮点/全精度/公式/数字选择），                 ║
 * ║          通过RxBus订阅外部事件（缓存加载/时基/通道/命令/FFT更新等）            ║
 * ║ 数据流向：UI操作 → onItemClick/onClick → Command → FPGA                       ║
 * ║          UI操作 → RightMsgMath → RxBus → 订阅方                              ║
 * ║          外部事件 → RxBus/EventUIObserver/CommandMsgToUI → UI同步更新          ║
 * ║ 依赖关系：RxBus, CacheUtil, Command, ChannelFactory, MathChannel,             ║
 * ║           MathDualWave, MathFFTWave, MathExprWave, MSwitchBox,               ║
 * ║           RightViewSelect, TopDialog*KeyBoard, TChan, MeasureManage          ║
 * ║ 使用场景：用户在右侧菜单中设置数学通道参数时显示此面板，支持M1-M8共8个数学通道  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/3.
 * 数学通道参数设置面板
 * <p>
 * 继承ConstraintLayout，提供4种数学运算类型的参数设置界面：
 * - 双波形运算(DoubleWave)：源1/源2/运算符(+,-,*,/)
 * - FFT运算：类型(dB/RMS)/源/窗函数/持久化
 * - AX+B运算：源/系数A/系数B/单位
 * - 高级数学(AdvanceMath)：公式/变量1/变量2/单位
 * <p>
 * 支持M1-M8共8个数学通道，每个通道独立配置。
 * 包含通道开关、标签、颜色选择、垂直档位调节等通用功能。
 * </p>
 */
public class RightLayoutMath extends ConstraintLayout {
    /** 日志标签 */
    private static final String TAG = "RightLayoutMath";
    /** 上下文引用 */
    private Context context;
    /** 数学类型选择控件：区分当前选中 DoubleWave/FFT/AX+B/AdvanceMath */
    private RightViewSelect mathChannelType;//区分当前选中:DoubleWave/FFT/AX+B/AdvanceMath
    /** 双波形运算布局 */
    private RelativeLayout dWLayout;//math type layout
    /** FFT运算布局 */
    private RelativeLayout fftLayout;
    /** AX+B运算布局 */
    private RelativeLayout axbLayout;
    /** 高级数学运算布局 */
    private RelativeLayout aMLayout;
    /** 内容区域容器（包含4个子布局） */
    private ConstraintLayout contentZone;

    /** 双波形源1选择控件 */
    private RightViewSelect dWSource1;//double wave
    /** 双波形源2选择控件 */
    private RightViewSelect dWSource2;
    /** 双波形运算符选择控件 */
    private RightViewSelect dWSymbol;
    /** FFT类型选择控件（dB/RMS） */
    private RightViewSelect fftType;
    /** FFT窗函数选择控件 */
    private RightViewSelect fftWindow;
    /** FFT源选择控件 */
    private RightViewSelect fftSource;
    /** FFT持久化选择控件 */
    private RightViewSelect fftPersist;//fft
    /** AX+B源选择控件 */
    private RightViewSelect axbSource;//axb
    /** AX+B单位文本显示 */
    private TextView axbUnit;
    /** AX+B系数A文本显示 */
    private TextView axbA;
    /** AX+B系数B文本显示 */
    private TextView axbB;
    /** FFT持久化值文本显示 */
    private TextView fftPersistValue;
    /** 高级数学单位文本显示 */
    private TextView aMUnit;
    /** 高级数学公式文本显示 */
    private TextView aMFormula;
    /** 高级数学变量1数值文本显示 */
    private TextView aMVar1Number;
    /** 高级数学变量1指数文本显示 */
    private TextView aMVar1Power;
    /** 高级数学变量2数值文本显示 */
    private TextView aMVar2Number;
    /** 高级数学变量2指数文本显示 */
    private TextView aMVar2Power;
    /** 数学通道垂直档位选择控件 */
    private RightViewSelect mathVerticalDetail;
    /** 数学通道消息封装对象 */
    private RightMsgMath msgMath;
    /** 文本键盘对话框（用于输入AX+B单位、高级数学单位） */
    private TopDialogTextKeyBoard layoutTextKeyBoard;
    /** 浮点键盘对话框（用于输入AX+B系数A/B等） */
    private TopDialogFloatKeyBoard layoutFloatKeyBoard;
    /** 全精度浮点键盘对话框（用于AX+B/高级数学的垂直档位和位置） */
    private TopDialogFullFloatKeyBoard layoutFullFloatKeyBoard;
    /** 公式键盘对话框（用于输入高级数学公式） */
    private TopDialogFormulaKeyBoard layoutFormulaKeyBoard;
    /** 数字选择对话框（用于设置变量1/2的数值和指数） */
    private TopDialogNumberPicker layoutNumberPicker;
    /** FFT持久化值设置对话框 */
    private DialogMathFFTPersist layoutMathFFTPersist;

    /** 通道开关容器布局 */
    private LinearLayout llDisplaySwitch;
    /** 通道开关控件 */
    private MSwitchBox mSwitchBox;
    /** 当前数学通道编号，默认Math1 */
    private int mathChannelNumber = TChan.Math1;

    /** 档位调节按钮容器 */
    private ConstraintLayout clImgBtn;
    /** 档位向上调节按钮 */
    private Button btnTop;
    /** 档位向下调节按钮 */
    private Button btnBottom;
    /** 档位按钮背景图 */
    private ImageView ivBackground;

    /** 是否为单独设置M1...M8模式 */
    private boolean isSingleMath = false;//是否为单独设置M1.....M8模式

    /** 弱引用Handler，用于延迟恢复按钮背景 */
    private MyHandler myHandler;
    /** 通道标题栏容器 */
    private ConstraintLayout topChannelTitle;
    /** 顶部通道组容器（Math/Ref/Serials切换） */
    private ConstraintLayout clTopChannelGroup;
    /** 通道标题文本（如"M1"） */
    private TextView channelTitle;
    /** 空占位视图 */
    private View space;
    /** 删除通道按钮 */
    private TextView btnDeleteChannel;
    /** 添加通道按钮 */
    private TextView btnAddChannel;
    /** 数学通道RadioButton */
    private RadioButton channelMath;
    /** 参考通道RadioButton */
    private RadioButton channelRef;
    /** 串口通道RadioButton */
    private RadioButton channelSerials;
    /** 是否需要更新MasterView位置标记 */
    private boolean needUpdateMasterLocation = false;
    /** 当前时基字符串（高级数学公式中可能引用） */
    private String timeBase = "";
    /** 是否显示FFT测量信息标记 */
    private boolean isShowFftInfo = true;
    /** 根视图组引用 */
    private ViewGroup rootView;
    /** 通道标签编辑控件 */
    private TopViewEdit chLabel;
    /** 颜色选择编辑控件 */
    private TopViewEdit selectColor;
    /** 用于双击事件的隐藏编辑控件 */
    private TopViewEdit forDoubleClick;
    /** 通道标签对话框 */
    private DialogChannelLabel dialogChannelLabel;
    /** 颜色选择对话框 */
    private DialogSelectColor dialogSelectColor;

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public RightLayoutMath(Context context) {
        this(context, null);                                                            // 委托给两参数构造
    }

    /**
     * 两参数构造方法
     * @param context 上下文
     * @param attrs XML属性集
     */
    public RightLayoutMath(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                        // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终初始化入口）
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutMath(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                            // 调用父类构造
        this.context = context;                                                         // 保存上下文引用
        initView(attrs, defStyleAttr);                                                  // 初始化视图控件
        initControl();                                                                  // 初始化事件订阅
        myHandler = new MyHandler(RightLayoutMath.this);                                // 创建弱引用Handler
    }

    /**
     * 初始化视图控件
     * <p>加载布局、绑定控件引用、设置监听器、初始化键盘对话框引用、
     * 设置通道标题、初始化消息数据、设置控件颜色</p>
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_math, this); // 填充数学通道布局
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutMath); // 获取自定义属性
        int mathIndex = ta.getInt(R.styleable.RightLayoutMath_mathChannelNumber, 1);    // 获取数学通道索引，默认1
        boolean isNormal = ta.getBoolean(R.styleable.RightLayoutMath_isNormal, false);  // 获取是否为普通模式
        ta.recycle();                                                                   // 回收TypedArray
        mathChannelNumber = TChan.toMathChan(mathIndex);                                // 将索引转换为数学通道编号
        space = findViewById(R.id.space);                                               // 空占位视图
        topChannelTitle = findViewById(R.id.top_channel_title);                         // 通道标题栏容器
        btnDeleteChannel = findViewById(R.id.btn_delete_channel);                       // 删除通道按钮
        channelTitle = findViewById(R.id.channel_title);                                // 通道标题文本
        llDisplaySwitch = findViewById(R.id.ll_display_switch);                         // 开关容器布局
        mSwitchBox = findViewById(R.id.channel_switch);                                 // 通道开关控件
        clImgBtn = findViewById(R.id.cl_img_btn);                                       // 档位调节按钮容器
        btnTop = findViewById(R.id.btnTop);                                             // 档位向上按钮
        btnBottom = findViewById(R.id.btnBottom);                                       // 档位向下按钮
        ivBackground = findViewById(R.id.img_back_src);                                 // 档位按钮背景图

        mathChannelType = (RightViewSelect) findViewById(R.id.math_channel_type);       // 数学类型选择控件
        contentZone = findViewById(R.id.right_content_zone);                            // 内容区域容器
        dWLayout = (RelativeLayout) findViewById(R.id.doubleWaveLayout);                // 双波形布局
        fftLayout = (RelativeLayout) findViewById(R.id.fftLayout);                      // FFT布局
        axbLayout = (RelativeLayout) findViewById(R.id.axbLayout);                      // AX+B布局
        aMLayout = (RelativeLayout) findViewById(R.id.advanceMathLayout);               // 高级数学布局

        dWSource1 = (RightViewSelect) findViewById(R.id.doubleWaveSource1);             // 双波形源1
        dWSource2 = (RightViewSelect) findViewById(R.id.doubleWaveSource2);             // 双波形源2
        dWSymbol = (RightViewSelect) findViewById(R.id.doubleWaveSymbol);               // 双波形运算符

        fftWindow = (RightViewSelect) findViewById(R.id.fftWindow);                     // FFT窗函数
        fftSource = (RightViewSelect) findViewById(R.id.fftSource);                     // FFT源
        fftPersist = findViewById(R.id.fftPersist);                                     // FFT持久化

        fftType = (RightViewSelect) findViewById(R.id.fftType);                         // FFT类型
        fftPersistValue = findViewById(R.id.fftPersistValue);                           // FFT持久化值

        axbSource = (RightViewSelect) findViewById(R.id.axbSource);                     // AX+B源
        axbUnit = (TextView) findViewById(R.id.axbUnitDetail);                          // AX+B单位
        axbA = (TextView) findViewById(R.id.axbADetail);                                // AX+B系数A
        axbB = (TextView) findViewById(R.id.axbBDetail);                                // AX+B系数B

        aMUnit = (TextView) findViewById(R.id.advanceMathUnitDetail);                   // 高级数学单位
        aMFormula = (TextView) findViewById(R.id.advanceMathFormula);                   // 高级数学公式
        aMVar1Number = (TextView) findViewById(R.id.advanceMathVar1Number);             // 变量1数值
        aMVar1Power = (TextView) findViewById(R.id.advanceMathVar1Power);               // 变量1指数
        aMVar2Number = (TextView) findViewById(R.id.advanceMathVar2Number);             // 变量2数值
        aMVar2Power = (TextView) findViewById(R.id.advanceMathVar2Power);               // 变量2指数
        chLabel = findViewById(R.id.chLabel);                                           // 通道标签
        selectColor = findViewById(R.id.select_color);                                  // 颜色选择
        forDoubleClick = findViewById(R.id.for_doubleClick);                            // 双击辅助控件

        mathVerticalDetail = (RightViewSelect) findViewById(R.id.mathVerticalBaseDetail); // 垂直档位选择

        String[] channels = GlobalVar.get().getChannelsName();                          // 获取通道名称数组
        if (channels.length == 2) {                                                     // 仅2通道时调整间距
            dWSource1.setItemMarginTop(25);                                             // 双波形源1增加上边距
            dWSource2.setItemMarginTop(25);                                             // 双波形源2增加上边距
        }
        dWSource1.setArray(channels);                                                   // 设置双波形源1选项
        dWSource2.setArray(channels);                                                   // 设置双波形源2选项
        fftSource.setArray(channels);                                                   // 设置FFT源选项
        axbSource.setArray(channels);                                                   // 设置AX+B源选项

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group);                    // 顶部通道组容器
        channelMath = findViewById(R.id.channelMath);                                   // 数学通道RadioButton
        channelRef = findViewById(R.id.channelRef);                                     // 参考通道RadioButton
        channelSerials = findViewById(R.id.channelSerials);                             // 串口通道RadioButton
        btnAddChannel = findViewById(R.id.btn_add_channel);                             // 添加通道按钮
        channelMath.setOnClickListener(onClickListener);                                // 数学通道点击监听
        channelRef.setOnClickListener(onClickListener);                                 // 参考通道点击监听
        channelSerials.setOnClickListener(onClickListener);                             // 串口通道点击监听
        btnAddChannel.setOnClickListener(onClickListener);                              // 添加通道点击监听

        mSwitchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);        // 开关状态变更监听
        mathChannelType.setOnItemClickListener(onRightSlipViewSelectItemClickListener);   // 数学类型选择监听
        dWSource1.setOnItemClickListener(onRightSlipViewSelectItemClickListener);         // 双波形源1选择监听
        dWSource2.setOnItemClickListener(onRightSlipViewSelectItemClickListener);         // 双波形源2选择监听
        dWSymbol.setOnItemClickListener(onRightSlipViewSelectItemClickListener);          // 双波形运算符选择监听
        fftWindow.setOnItemClickListener(onRightSlipViewSelectItemClickListener);         // FFT窗函数选择监听
        fftSource.setOnItemClickListener(onRightSlipViewSelectItemClickListener);         // FFT源选择监听
        fftPersist.setOnItemClickListener(onRightSlipViewSelectItemClickListener);        // FFT持久化选择监听
        fftType.setOnItemClickListener(onRightSlipViewSelectItemClickListener);           // FFT类型选择监听
        fftPersistValue.setOnClickListener(onClickListener);                             // FFT持久化值点击监听
        axbSource.setOnItemClickListener(onRightSlipViewSelectItemClickListener);         // AX+B源选择监听
        axbUnit.setOnClickListener(onClickListener);                                    // AX+B单位点击监听
        axbA.setOnClickListener(onClickListener);                                       // AX+B系数A点击监听
        axbB.setOnClickListener(onClickListener);                                       // AX+B系数B点击监听
        aMUnit.setOnClickListener(onClickListener);                                     // 高级数学单位点击监听
        aMFormula.setOnClickListener(onClickListener);                                  // 高级数学公式点击监听
        aMVar1Number.setOnClickListener(onClickListener);                               // 变量1数值点击监听
        aMVar1Power.setOnClickListener(onClickListener);                                // 变量1指数点击监听
        aMVar2Number.setOnClickListener(onClickListener);                               // 变量2数值点击监听
        aMVar2Power.setOnClickListener(onClickListener);                                // 变量2指数点击监听
        mathVerticalDetail.setOnItemClickListener(onRightSlipViewSelectItemClickListener); // 垂直档位选择监听
        btnTop.setOnClickListener(onClickListener);                                     // 档位向上按钮点击监听
        btnBottom.setOnClickListener(onClickListener);                                  // 档位向下按钮点击监听
        btnDeleteChannel.setOnClickListener(onClickListener);                            // 删除通道按钮点击监听
        chLabel.setOnClickEditListener(onClickEditListener);                             // 通道标签编辑监听
        selectColor.setOnClickEditListener(onClickEditListener);                         // 颜色选择编辑监听
        forDoubleClick.setOnClickEditListener(onClickEditListener);                      // 双击编辑监听

        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);     // 文本键盘对话框
        layoutFloatKeyBoard = (TopDialogFloatKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);   // 浮点键盘对话框
        layoutFullFloatKeyBoard = (TopDialogFullFloatKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard); // 全精度浮点键盘
        layoutFormulaKeyBoard = (TopDialogFormulaKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFormulaKeyBoard); // 公式键盘对话框
        layoutNumberPicker = (TopDialogNumberPicker) ((MainActivity) context).findViewById(R.id.dialogNumberPicker);      // 数字选择对话框
        layoutMathFFTPersist = ((MainActivity) context).findViewById(R.id.dialogMathFFTPersist);                          // FFT持久化对话框

        channelTitle.setText("M" + TChan.toMathNumber(mathChannelNumber));              // 设置通道标题，如"M1"
        initData();                                                                     // 初始化消息数据
        setControlColorByChIdx(mathChannelNumber);                                      // 设置控件颜色
    }

    /**
     * 根据通道索引设置所有控件的颜色主题
     * <p>设置标题、开关、选择控件等为对应通道的颜色，并恢复缓存中的数学类型选中项</p>
     * @param chIdx 通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx));               // 标题颜色
        mSwitchBox.setControlColorByChIdx(chIdx);                                       // 开关颜色
        mathChannelType.setControlColorByChIdx(chIdx);                                  // 数学类型颜色
        dWSource1.setControlColorByChIdx(chIdx);                                        // 双波形源1颜色
        dWSource2.setControlColorByChIdx(chIdx);                                        // 双波形源2颜色
        dWSymbol.setControlColorByChIdx(chIdx);                                         // 双波形运算符颜色
        fftWindow.setControlColorByChIdx(chIdx);                                        // FFT窗函数颜色
        fftSource.setControlColorByChIdx(chIdx);                                        // FFT源颜色
        fftPersist.setControlColorByChIdx(chIdx);                                       // FFT持久化颜色
        fftType.setControlColorByChIdx(chIdx);                                          // FFT类型颜色
        axbSource.setControlColorByChIdx(chIdx);                                        // AX+B源颜色
        mathVerticalDetail.setControlColorByChIdx(chIdx);                               // 垂直档位颜色
        channelMath.setTextColor(TChan.getChannelColor(context, chIdx));                // Math RadioButton颜色
//        selectColor.setEditColor(SvgNodeInfo.getAllBaseColor(chIdx));
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);  // 从缓存读取数学类型
        mathChannelType.setSelectIndex(mathType);                                       // 恢复数学类型选中项
    }


    /**
     * 仅设置控件颜色（不触发选中项变化），用于颜色变更时刷新
     * @param chIdx 通道索引
     */
    private void setOnlyControlColorByChIdx(int chIdx) {
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx));               // 标题颜色
        mSwitchBox.setControlColorByChIdx(chIdx);                                       // 开关颜色
        mathChannelType.setOnlyControlColorByChIdx(chIdx);                              // 数学类型颜色（不触发选中变更）
        dWSource1.setOnlyControlColorByChIdx(chIdx);                                    // 双波形源1颜色
        dWSource2.setOnlyControlColorByChIdx(chIdx);                                    // 双波形源2颜色
        dWSymbol.setOnlyControlColorByChIdx(chIdx);                                     // 双波形运算符颜色
        fftWindow.setOnlyControlColorByChIdx(chIdx);                                    // FFT窗函数颜色
        fftSource.setOnlyControlColorByChIdx(chIdx);                                    // FFT源颜色
        fftPersist.setOnlyControlColorByChIdx(chIdx);                                   // FFT持久化颜色
        fftType.setOnlyControlColorByChIdx(chIdx);                                      // FFT类型颜色
        axbSource.setOnlyControlColorByChIdx(chIdx);                                    // AX+B源颜色
        mathVerticalDetail.setOnlyControlColorByChIdx(chIdx);                           // 垂直档位颜色
        channelMath.setTextColor(TChan.getChannelColor(context, chIdx));                // Math RadioButton颜色
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);  // 从缓存读取数学类型
        mathChannelType.setSelectIndex(mathType);                                       // 恢复数学类型选中项
    }

    /**
     * 初始化消息数据
     * <p>创建RightMsgMath对象并从UI控件读取当前状态填充到消息对象中</p>
     */
    private void initData() {
        msgMath = new RightMsgMath();                                                   // 创建消息对象
        msgMath.setMathCheck(mSwitchBox.isState());                                     // 开关状态
        msgMath.setMathType(CacheUtil.MATHTYPE_DW);//TODO 后续去掉 用setMathTypeSelect代替 // 数学类型
        msgMath.setMathTypeSelect(mathChannelType.getSelectItem());                     // 数学类型选择项
        msgMath.setDwSource1(dWSource1.getSelectItem());                                // 双波形源1
        msgMath.setDwSource2(dWSource2.getSelectItem());                                // 双波形源2
        msgMath.setDwSymbol(dWSymbol.getSelectItem());                                  // 双波形运算符
        msgMath.setFftType(fftType.getSelectItem().getText());                          // FFT类型文本
        msgMath.setFftSource(fftSource.getSelectItem());                                // FFT源
        msgMath.setFftWindow(fftWindow.getSelectItem());                                // FFT窗函数
        msgMath.setFftPersist(fftPersist.getSelectItem());                              // FFT持久化
        msgMath.setAxbUnit(axbUnit.getText().toString());                               // AX+B单位
        msgMath.setAxbSource(axbSource.getSelectItem());                                // AX+B源
        msgMath.setAxbA(axbA.getText().toString());                                     // AX+B系数A
        msgMath.setAxbB(axbB.getText().toString());                                     // AX+B系数B
        msgMath.setAmUnit(aMUnit.getText().toString());                                 // 高级数学单位
        msgMath.setAmFormula(aMFormula.getText().toString());                           // 高级数学公式
        msgMath.setAmVar1Number(aMVar1Number.getText().toString());                     // 变量1数值
        msgMath.setAmVar1Power(aMVar1Power.getText().toString());                       // 变量1指数
        msgMath.setAmVar2Number(aMVar2Number.getText().toString());                     // 变量2数值
        msgMath.setAmVar2Power(aMVar2Power.getText().toString());                       // 变量2指数
        msgMath.setMathChannelNumber(mathChannelNumber);                                // 数学通道编号
        msgMath.setLabel(chLabel.getText());                                            // 通道标签
    }

    /**
     * 初始化事件订阅
     * <p>订阅RxBus事件和EventFactory事件，包括：
     * 缓存加载、时基变更、通道开关、命令转UI、正常状态显示、
     * 通道添加、垂直档位、顶部显示、FFT波形更新、通道删除、颜色选择、鼠标点击等</p>
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);             // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase); // 订阅时基变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);       // 订阅右侧其他消息（通道开关）
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);             // 订阅命令转UI消息
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState); // 订阅正常/共享布局切换
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_MATH).subscribe(consumerOtherChannelCanAdd); // 订阅通道添加可用性
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale); // 订阅垂直档位调节
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);        // 订阅顶部显示消息
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_WAVE_UPDATE, eventUIObserver);               // 注册FFT波形更新事件观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel); // 订阅通道删除
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅颜色选择
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick); // 订阅鼠标点击位置
    }

    /** 数学类型选项数组（双波形/FFT/AX+B/高级数学） */
    String[] mathTypeArray = App.get().getResources().getStringArray(R.array.math_channel_type);
    /** 通道源名称数组（CH1-CH8等） */
    String[] sourceArray = App.get().getResources().getStringArray(R.array.channelsNameEight);
    /** 双波形运算符数组（+,-,*,/） */
    String[] dWSymbolArray = App.get().getResources().getStringArray(R.array.mathSymbol);
    /** FFT类型数组（dB/RMS） */
    String[] fftTypeArray = App.get().getResources().getStringArray(R.array.mathFftType);
    /** FFT窗函数数组（Hanning/Hamming/Blackman/Rectangular等） */
    String[] fftWindowArray = App.get().getResources().getStringArray(R.array.mathWindow);
    /** FFT持久化选项数组 */
    String[] fftPersistArray = App.get().getResources().getStringArray(R.array.mathFftPersist);

    /**
     * 从缓存恢复所有数学通道参数
     * <p>读取CacheUtil中的数学通道参数，同步到UI控件、消息对象和底层通道对象，
     * 并通过Command向FPGA发送初始控制指令</p>
     */
    private void setCache() {
        boolean isAddByUser = isAddByUser(mathChannelNumber);                           // 是否由用户添加
        boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber); // 开关状态
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber); // 数学类型
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber); // 通道标签
        //DoubleWave
        int dwS1 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE1 + mathChannelNumber); // 双波形源1
        int dwS2 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE2 + mathChannelNumber); // 双波形源2
        int dwSymbol = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SYMBOL + mathChannelNumber); // 双波形运算符
        //FFT
        int fftType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber); // FFT类型
        int fftSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_SOURCE + mathChannelNumber); // FFT源
        int fftWindow = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_WINDOW + mathChannelNumber); // FFT窗函数
        int fftPersist = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST + mathChannelNumber); // FFT持久化
        String fftPersistValue = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + mathChannelNumber); // FFT持久化值
        //AX+B
        String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber); // AX+B单位
        int axbSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + mathChannelNumber); // AX+B源
        String axbA = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber); // AX+B系数A
        String axbB = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber); // AX+B系数B
        //AdvanceMath
        String amUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber); // 高级数学单位
        String amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + mathChannelNumber); // 高级数学公式
        String amVar1Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + mathChannelNumber); // 变量1数值
        String amVar1Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + mathChannelNumber); // 变量1指数
        String amVar2Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + mathChannelNumber); // 变量2数值
        String amVar2Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + mathChannelNumber); // 变量2指数

        //V-Scale Ref
        int mathVerticalIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_VERTICALBASE + mathChannelNumber); // 垂直档位索引

        mSwitchBox.setState(mathCheck);                                                 // 恢复开关状态
        dWSource1.setSelectIndex(dwS1);                                                // 恢复双波形源1
        dWSource2.setSelectIndex(dwS2);                                                // 恢复双波形源2
        dWSymbol.setSelectIndex(dwSymbol);                                             // 恢复双波形运算符
        this.fftType.setSelectIndex(fftType);                                          // 恢复FFT类型
        this.fftSource.setSelectIndex(fftSource);                                      // 恢复FFT源
        this.fftWindow.setSelectIndex(fftWindow);                                      // 恢复FFT窗函数
        this.fftPersist.setSelectIndex(fftPersist);                                    // 恢复FFT持久化
        this.fftPersistValue.setText(fftPersistValue);                                 // 恢复FFT持久化值
        setFftPersistValueEnable();                                                    // 根据持久化选项设置持久化值可用性
        this.axbUnit.setText(axbUnit);                                                 // 恢复AX+B单位
        this.axbSource.setSelectIndex(axbSource);                                      // 恢复AX+B源
        this.axbA.setText(axbA);                                                       // 恢复AX+B系数A
        this.axbB.setText(axbB);                                                       // 恢复AX+B系数B
        this.aMUnit.setText(amUnit);                                                   // 恢复高级数学单位
        this.aMFormula.setText(amFormula);                                             // 恢复高级数学公式
        this.aMVar1Number.setText(amVar1Number);                                       // 恢复变量1数值
        this.aMVar1Power.setText(amVar1Power);                                         // 恢复变量1指数
        this.aMVar2Number.setText(amVar2Number);                                       // 恢复变量2数值
        this.aMVar2Power.setText(amVar2Power);                                         // 恢复变量2指数
        this.mathVerticalDetail.setSelectIndex(mathVerticalIndex);                      // 恢复垂直档位
        this.chLabel.setText(label);                                                   // 恢复通道标签
        mathChannelType.setSelectIndex(mathType);                                      // 恢复数学类型

        WaveManage.get().setChannelLabel(mathChannelNumber, label);                     // 同步标签到波形管理
        setChannelLabel(TChan.toFpgaChNo(mathChannelNumber), label);                    // 同步标签到数学通道对象

        Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), mathCheck && isAddByUser, false); // 发送显示命令
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道对象
        if (mathChannel == null) return;                                                // 通道为空则返回
        mathChannel.setVerticalMode(mathVerticalIndex);                                 // 设置垂直模式
        MathDualWave dualWave = mathChannel.getMathDualWave();                          // 获取双波形对象
        dualWave.setSource1(dwS1);                                                      // 设置双波形源1
        dualWave.setSource2(dwS2);                                                      // 设置双波形源2
        dualWave.setOperator(dwSymbol);                                                 // 设置双波形运算符
        MathFFTWave fftWave = mathChannel.getMathFFTWave();                             // 获取FFT对象
        fftWave.setFFTType(fftType);                                                    // 设置FFT类型
        fftWave.setSource(fftSource);                                                   // 设置FFT源
        fftWave.setFFTWindow(fftWindow);                                                // 设置FFT窗函数

        setScopeAxb(mathChannelNumber);                                                // 设置AX+B到示波器底层

        MathExprWave mathExprWave = mathChannel.getMathExprWave();                      // 获取表达式波形对象
        setScopeAM(mathChannelNumber);                                                  // 设置高级数学到示波器底层
        double var1 = Double.parseDouble(amVar1Number) * Math.pow(10, Double.parseDouble(amVar1Power)); // 计算变量1实际值
        mathExprWave.setVar1(var1);                                                     // 设置变量1
        double var2 = Double.parseDouble(amVar2Number) * Math.pow(10, Double.parseDouble(amVar2Power)); // 计算变量2实际值
        mathExprWave.setVar2(var2);                                                     // 设置变量2

        setShowMathTypeLayout(mathType);                                                // 显示对应数学类型的布局
        setCacheMathType(mathType, false, mathChannelNumber);                           // 设置缓存的数学类型

        Command.get().getMath().VRef(TChan.toFpgaChNo(mathChannelNumber), mathVerticalIndex, false); // 发送垂直参考命令
        Command.get().getMath().Mode(TChan.toFpgaChNo(mathChannelNumber), mathType, false); // 发送数学模式命令

        Command.get().getMath_base().S1(TChan.toFpgaChNo(mathChannelNumber), dwS1, false); // 发送双波形源1命令
        Command.get().getMath_base().S2(TChan.toFpgaChNo(mathChannelNumber), dwS2, false); // 发送双波形源2命令
        Command.get().getMath_base().Operator(TChan.toFpgaChNo(mathChannelNumber), dwSymbol, false); // 发送双波形运算符命令

        Command.get().getMath_fft().Type(TChan.toFpgaChNo(mathChannelNumber), fftType, false); // 发送FFT类型命令
        Command.get().getMath_fft().Source(TChan.toFpgaChNo(mathChannelNumber), fftSource, false); // 发送FFT源命令
        Command.get().getMath_fft().Window(TChan.toFpgaChNo(mathChannelNumber), fftWindow, false); // 发送FFT窗函数命令

        Command.get().getMath_axb().Source(TChan.toFpgaChNo(mathChannelNumber), axbSource, false); // 发送AX+B源命令
        Command.get().getMath_axb().A(TChan.toFpgaChNo(mathChannelNumber), axbA, false); // 发送AX+B系数A命令
        Command.get().getMath_axb().B(TChan.toFpgaChNo(mathChannelNumber), axbB, false); // 发送AX+B系数B命令
        Command.get().getMath_axb().Unit(TChan.toFpgaChNo(mathChannelNumber), axbUnit, false); // 发送AX+B单位命令

        Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), amFormula, false); // 发送高级数学公式命令
        Command.get().getMath_advanced().Var1(TChan.toFpgaChNo(mathChannelNumber), var1, false); // 发送变量1命令
        Command.get().getMath_advanced().Var2(TChan.toFpgaChNo(mathChannelNumber), var2, false); // 发送变量2命令
        Command.get().getMath_advanced().Unit(TChan.toFpgaChNo(mathChannelNumber), amUnit, false); // 发送高级数学单位命令

        String colorStr = CacheUtil.get().getString(CacheUtil.MAIN_CHANNEL_COLOR + mathChannelNumber); // 读取通道颜色
        selectColor.setEditColor(colorStr);                                             // 设置颜色选择显示

        msgMath.setMathCheck(mathCheck);                                                // 设置消息-开关状态
        msgMath.setMathType(mathType);                                                  // 设置消息-数学类型
        msgMath.setMathTypeSelect(new RightBeanSelect(mathType, mathTypeArray[mathType], mathCheck)); // 设置消息-数学类型选择项
        msgMath.setDwSource1(new RightBeanSelect(dwS1, sourceArray[dwS1], true));       // 设置消息-双波形源1
        msgMath.setDwSource2(new RightBeanSelect(dwS2, sourceArray[dwS2], true));       // 设置消息-双波形源2
        msgMath.setDwSymbol(new RightBeanSelect(dwSymbol, dWSymbolArray[dwSymbol], true)); // 设置消息-双波形运算符
        msgMath.setFftType(fftTypeArray[fftType]);                                      // 设置消息-FFT类型
        msgMath.setFftSource(new RightBeanSelect(fftSource, sourceArray[fftSource], true)); // 设置消息-FFT源
        msgMath.setFftWindow(new RightBeanSelect(fftWindow, fftWindowArray[fftWindow], true)); // 设置消息-FFT窗函数
        msgMath.setFftPersist(new RightBeanSelect(fftPersist, fftPersistArray[fftPersist], true)); // 设置消息-FFT持久化
        msgMath.setAxbUnit(axbUnit);                                                    // 设置消息-AX+B单位
        msgMath.setAxbSource(new RightBeanSelect(axbSource, sourceArray[axbSource], true)); // 设置消息-AX+B源
        msgMath.setAxbA(axbA);                                                          // 设置消息-AX+B系数A
        msgMath.setAxbB(axbB);                                                          // 设置消息-AX+B系数B
        msgMath.setAmUnit(amUnit);                                                      // 设置消息-高级数学单位
        msgMath.setAmFormula(amFormula);                                                // 设置消息-高级数学公式
        msgMath.setAmVar1Number(amVar1Number);                                          // 设置消息-变量1数值
        msgMath.setAmVar1Power(amVar1Power);                                            // 设置消息-变量1指数
        msgMath.setAmVar2Number(amVar2Number);                                          // 设置消息-变量2数值
        msgMath.setAmVar2Power(amVar2Power);                                            // 设置消息-变量2指数
        msgMath.setLabel(label);                                                        // 设置消息-通道标签
        sendMsg();                                                                      // 发送消息通知订阅方
    }

    /**
     * 根据FFT持久化选项设置持久化值输入框的可用性
     * <p>当选择"Off"或"Infinite"时禁用持久化值输入，其他选项启用</p>
     */
    private void setFftPersistValueEnable() {
        if (fftPersist.getSelectIndex() == 0 || fftPersist.getSelectIndex() == 1) {     // Off或Infinite
            this.fftPersistValue.setEnabled(false);                                      // 禁用输入
        } else {                                                                        // 其他选项
            this.fftPersistValue.setEnabled(true);                                       // 启用输入
        }
    }

    /**
     * 设置AX+B运算到示波器底层通道
     * <p>根据缓存和UI控件的值构建AX+B表达式并设置到MathExprWave</p>
     * @param chan 对应通道 TChan.Math1---TChan.Math8
     */
    private void setScopeAxb(int chan) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(chan)); // 获取数学通道对象
        if (mathChannel == null) return;                                                 // 通道为空则返回
        String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + chan); // 从缓存读取AX+B单位
        int axbSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + chan); // 从缓存读取AX+B源
        String axbA = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_A + chan); // 从缓存读取AX+B系数A
        String axbB = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_B + chan); // 从缓存读取AX+B系数B
        double a = TBookUtil.getDoubleFromM(axbA);                                      // 解析系数A为double
        double b = TBookUtil.getDoubleFromM(axbB);                                      // 解析系数B为double
        if (!TextUtils.isEmpty(this.axbA.getText().toString())) {                        // 如果UI上的系数A非空
            a = TBookUtil.getDoubleFromM(this.axbA.getText().toString());                // 优先使用UI上的值
        }
        if (!TextUtils.isEmpty(this.axbB.getText().toString())) {                        // 如果UI上的系数B非空
            b = TBookUtil.getDoubleFromM(this.axbB.getText().toString());                // 优先使用UI上的值
        }
        MathExprWave exprWave = mathChannel.getMathExprWave();                           // 获取表达式波形对象
        exprWave.clearSource();                                                          // 清除已有源
        exprWave.addSource(axbSource);                                                   // 添加AX+B源通道
        mathChannel.setProbeStr(axbUnit);                                                // 设置探头字符串（单位）
        exprWave.setExprString("(" + a + ") * " + getChannelName(axbSource) + " + (" + b + ")"); // 构建AX+B表达式
    }


    /**
     * 设置高级数学运算到示波器底层通道
     * <p>解析公式中的通道引用（ch1-ch8），设置源通道、变量值和表达式</p>
     * @param chan 对应通道 TChan.Math1---TChan.Math8
     */
    private void setScopeAM(int chan) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(chan)); // 获取数学通道对象
        if (mathChannel == null) return;                                                 // 通道为空则返回
        String amUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + chan); // 从缓存读取高级数学单位
        String amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + chan); // 从缓存读取高级数学公式
        String amVar1Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + chan); // 变量1数值
        String amVar1Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + chan); // 变量1指数
        String amVar2Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + chan); // 变量2数值
        String amVar2Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + chan); // 变量2指数

        MathExprWave mathExprWave = mathChannel.getMathExprWave();                      // 获取表达式波形对象
        mathExprWave.clearSource();                                                      // 清除已有源
        if (amFormula.contains("ch1")) {                                                 // 公式包含ch1
            mathExprWave.addSource(ChannelFactory.CH1);                                  // 添加CH1源
        }
        if (amFormula.contains("ch2")) {                                                 // 公式包含ch2
            mathExprWave.addSource(ChannelFactory.CH2);                                  // 添加CH2源
        }
        if (amFormula.contains("ch3")) {                                                 // 公式包含ch3
            mathExprWave.addSource(ChannelFactory.CH3);                                  // 添加CH3源
        }
        if (amFormula.contains("ch4")) {                                                 // 公式包含ch4
            mathExprWave.addSource(ChannelFactory.CH4);                                  // 添加CH4源
        }
        if (amFormula.contains("ch5")) {                                                 // 公式包含ch5
            mathExprWave.addSource(ChannelFactory.CH5);                                  // 添加CH5源
        }
        if (amFormula.contains("ch6")) {                                                 // 公式包含ch6
            mathExprWave.addSource(ChannelFactory.CH6);                                  // 添加CH6源
        }
        if (amFormula.contains("ch7")) {                                                 // 公式包含ch7
            mathExprWave.addSource(ChannelFactory.CH7);                                  // 添加CH7源
        }
        if (amFormula.contains("ch8")) {                                                 // 公式包含ch8
            mathExprWave.addSource(ChannelFactory.CH8);                                  // 添加CH8源
        }
        mathChannel.setProbeStr(amUnit);                                                 // 设置探头字符串（单位）
        mathExprWave.setVar1(Double.parseDouble(amVar1Number)                           // 设置变量1（数值×10^指数）
                * Math.pow(10, Double.parseDouble(amVar1Power)));
        mathExprWave.setVar2(Double.parseDouble(amVar2Number)                           // 设置变量2（数值×10^指数）
                * Math.pow(10, Double.parseDouble(amVar2Power)));
        amFormula = amFormula.replace(App.get().getResources().getString(R.string.key_formula_tb), handleTimeBase(MainHolderBottom.getCenterTimeBase())); // 替换时间基占位符
        amFormula = KeyBoardFormulaUtil.amFormulaToScope(amFormula, MainHolderBottom.getCenterTimeBase()); // 将公式转换为示波器格式
        mathExprWave.setExprString(amFormula);                                           // 设置表达式字符串
    }

    /**
     * 判断float数据是否有效（非无穷大且非NaN）
     * @param f 待检测的float值
     * @return true=有效, false=无效
     */
    private boolean isFloatValid(float f) {
        return !(Float.isInfinite(f) || Float.isNaN(f));                                // 排除无穷大和NaN
    }

    /**
     * 根据通道索引获取通道名称（用于公式表达式）
     * <p>注意：索引0对应ch1，索引1对应ch2，以此类推</p>
     * @param channelIndex 通道索引（0-based）
     * @return 通道名称字符串
     */
    private String getChannelName(int channelIndex) {
        switch (channelIndex) {
            case 1:
                return "ch2";                                                            // 索引1→ch2
            case 2:
                return "ch3";                                                            // 索引2→ch3
            case 3:
                return "ch4";                                                            // 索引3→ch4
            case 4:
                return "ch5";                                                            // 索引4→ch5
            case 5:
                return "ch6";                                                            // 索引5→ch6
            case 6:
                return "ch7";                                                            // 索引6→ch7
            case 7:
                return "ch8";                                                            // 索引7→ch8
            default:
                return "ch1";                                                            // 默认→ch1
        }
    }

    /**
     * 缓存加载事件消费者
     * <p>当收到缓存加载事件时，恢复缓存数据并标记加载完成</p>
     */
    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {
            setCache();                                                                  // 从缓存恢复参数
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutMath, true);      // 标记Math面板缓存加载完成
        }
    };


    /**
     * 设置缓存中的数学类型并更新底层通道
     * <p>将数学类型写入缓存，并根据类型更新MathChannel的数学模式</p>
     * @param type 数学类型索引
     * @param isFromEventBus 是否来自EventBus（若是则不更新底层通道）
     * @param channelNumber 数学通道编号
     */
    private void setCacheMathType(int type, boolean isFromEventBus, int channelNumber) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(channelNumber)); // 获取数学通道对象
        if (mathChannel == null) return;                                                 // 通道为空则返回
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_TYPE + channelNumber, String.valueOf(type)); // 写入缓存
        if (!isFromEventBus) {                                                           // 非EventBus来源时更新底层
            if (type == CacheUtil.MATHTYPE_DW) {                                         // 双波形
                mathChannel.setMathType(MathWave.MATH_DUALWAVE);                         // 设置为双波形模式
            } else if (type == CacheUtil.MATHTYPE_FFT) {                                 // FFT
                mathChannel.setMathType(MathWave.MATH_FFTWAVE);                          // 设置为FFT模式
            } else if (type == CacheUtil.MATHTYPE_AXB) {                                 // AX+B
                mathChannel.setProbeStr(axbUnit.getText().toString());                   // 设置探头字符串
                mathChannel.setMathType(MathWave.MATH_EXPR);                             // 设置为表达式模式
                setScopeAxb(channelNumber);                                              // 更新AX+B底层参数
            } else if (type == CacheUtil.MATHTYPE_AM) {                                  // 高级数学
                mathChannel.setProbeStr(aMUnit.getText().toString());                    // 设置探头字符串
                mathChannel.setMathType(MathWave.MATH_EXPR);                             // 设置为表达式模式
                setScopeAM(channelNumber);                                               // 更新高级数学底层参数
            }
        }
    }

    /**
     * 根据数学类型显示对应的子布局
     * <p>先隐藏所有子布局，再根据类型显示对应布局，同时控制FFT测量信息显示</p>
     * @param mathType 数学类型索引
     */
    private void setShowMathTypeLayout(int mathType) {
        for (int i = 0; i < contentZone.getChildCount(); i++) {                          // 遍历内容区子视图
            contentZone.getChildAt(i).setVisibility(GONE);                               // 隐藏所有子布局
        }
        //dWLayout, fftLayout, axbLayout, aMLayout
        boolean selectFft = false;                                                       // FFT选中标记
        switch (mathType) {
            case CacheUtil.MATHTYPE_FFT:                                                 // FFT类型
                selectFft = true;                                                        // 标记FFT选中
                fftLayout.setVisibility(VISIBLE);                                        // 显示FFT布局
                break;
            case CacheUtil.MATHTYPE_AXB:                                                 // AX+B类型
                axbLayout.setVisibility(VISIBLE);                                        // 显示AX+B布局
                break;
            case CacheUtil.MATHTYPE_AM:                                                  // 高级数学类型
                aMLayout.setVisibility(VISIBLE);                                         // 显示高级数学布局
                break;
            default:                                                                     // 默认双波形
                dWLayout.setVisibility(VISIBLE);                                         // 显示双波形布局
                break;
        }
        ShowFFTMeasureInfo(selectFft);                                                   // 控制FFT测量信息显示
    }

    /**
     * 设置数学通道位置
     * <p>在切换数学类型时，临时切换垂直模式以重新计算通道位置</p>
     * @param mathType 数学类型索引
     */
    private void setMathPos(int mathType) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道对象
        if(mathChannel == null) return;                                                  // 通道为空则返回
        boolean change = false;                                                          // 是否需要切换垂直模式
        if (mathChannel.getVerticalMode() == MathChannel.VERTICAL_MODE_SCREEN_CENTER) {  // 当前为屏幕中心模式
            mathChannel.setVerticalMode(MathChannel.VERTICAL_MODE_CH_ZERO);              // 临时切换到通道零点模式
            change = true;                                                               // 标记需要恢复
        }
        setMathWaveVScaleId(mathType);                                                   // 设置垂直档位ID
        mathChannel.setPos(Tools.getYTChannelPosition(mathChannelNumber));               // 计算并设置通道位置
        if (change) {                                                                    // 需要恢复时
            mathChannel.setVerticalMode(MathChannel.VERTICAL_MODE_SCREEN_CENTER);         // 恢复为屏幕中心模式
        }
    }

    /**
     * 根据数学类型设置对应的垂直档位ID
     * <p>不同数学类型使用不同的VScaleId缓存键</p>
     * @param mathType 数学类型索引
     */
    private void setMathWaveVScaleId(int mathType) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道对象
        if (mathChannel == null) return;                                                 // 通道为空则返回
        if (mathType == CacheUtil.MATHTYPE_AXB) {                                        // AX+B类型
            MathExprWave exprWave = mathChannel.getMathExprWave();                       // 获取表达式波形
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AXB_VSCALE_ID + mathChannelNumber)); // 设置AX+B垂直档位ID
        } else if (mathType == CacheUtil.MATHTYPE_DW) {                                  // 双波形类型
            MathDualWave dualWave = mathChannel.getMathDualWave();                       // 获取双波形对象
            dualWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_DW_VSCALE_ID + mathChannelNumber)); // 设置双波形垂直档位ID
        } else if (mathType == CacheUtil.MATHTYPE_AM) {                                  // 高级数学类型
            MathExprWave exprWave = mathChannel.getMathExprWave();                       // 获取表达式波形
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AM_VSCALE_ID + mathChannelNumber)); // 设置高级数学垂直档位ID
        } else {                                                                         // FFT类型
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber) == 1) { // dB类型
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();                  // 获取FFT波形对象
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + mathChannelNumber)); // 设置FFT dB垂直档位ID
            } else {                                                                     // RMS类型
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();                  // 获取FFT波形对象
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + mathChannelNumber)); // 设置FFT RMS垂直档位ID
            }
        }
    }

    /**
     * 发送数学通道消息
     * <p>通过RxBus将当前RightMsgMath对象发送给所有订阅方</p>
     */
    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_MATH, msgMath);                     // 发送数学通道消息
    }

    /**
     * 命令转UI消息消费者
     * <p>接收来自Command层的反馈消息，同步更新UI控件和消息对象。
     * 处理的标志包括：通道显示、数学运算符、数学模式、双波形源、FFT参数、
     * AX+B参数、高级数学参数、垂直参考、标签等。</p>
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {                              // 通道显示开关
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    boolean isOpen = Boolean.parseBoolean(params[1]);                     // 开关状态
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    mSwitchBox.setState(isOpen);                                          // 更新开关状态
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState()); // 触发开关监听
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_OPERATOR: {                           // 双波形运算符变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int index = Integer.parseInt(params[1]);                              // 运算符索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    setShowMathTypeLayout(CacheUtil.MATHTYPE_DW);                         // 切换到双波形布局
                    dWSymbol.setSelectIndex(index);                                       // 更新运算符选中项
                    selectItemIndex(dWSymbol.getId(), dWSymbol.getSelectItem());          // 处理选中项变更
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_MODE: {                                    // 数学模式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int index = Integer.parseInt(params[1]);                              // 模式索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    mathChannelType.setSelectIndex(index);                                // 更新数学类型选中项
                    switch (index) {
                        case 0:                                                           // 双波形
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_DW);                 // 显示双波形布局
                            setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber); // 设置缓存
                            setMathPos(CacheUtil.MATHTYPE_DW);                            // 设置通道位置
                            msgMath.setMathType(CacheUtil.MATHTYPE_DW);                   // 更新消息
                            break;
                        case 1:                                                           // FFT
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_FFT);                // 显示FFT布局
                            setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber); // 设置缓存
                            setMathPos(CacheUtil.MATHTYPE_FFT);                           // 设置通道位置
                            msgMath.setMathType(CacheUtil.MATHTYPE_FFT);                  // 更新消息
                            break;
                        case 2:                                                           // AX+B
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_AXB);                // 显示AX+B布局
                            setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber); // 设置缓存
                            setMathPos(CacheUtil.MATHTYPE_AXB);                           // 设置通道位置
                            msgMath.setMathType(CacheUtil.MATHTYPE_AXB);                  // 更新消息
                            break;
                        case 3:                                                           // 高级数学
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_AM);                 // 显示高级数学布局
                            setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber); // 设置缓存
                            setMathPos(CacheUtil.MATHTYPE_AM);                            // 设置通道位置
                            msgMath.setMathType(CacheUtil.MATHTYPE_AM);                   // 更新消息
                            break;
                    }
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_S1:                                   // 双波形源1
                case CommandMsgToUI.FLAG_MATH_ADDS1:                                     // 加法源1
                case CommandMsgToUI.FLAG_MATH_SUBS1:                                     // 减法源1
                case CommandMsgToUI.FLAG_MATH_MULS1:                                     // 乘法源1
                case CommandMsgToUI.FLAG_MATH_DIVS1: {                                   // 除法源1
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    dWSource1.setSelectIndex(selectIndex);                                // 更新双波形源1
                    selectItemIndex(dWSource1.getId(), dWSource1.getSelectItem());        // 处理选中项变更
                    msgMath.setDwSource1(dWSource1.getSelectItem());                      // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_S2:                                   // 双波形源2
                case CommandMsgToUI.FLAG_MATH_ADDS2:                                     // 加法源2
                case CommandMsgToUI.FLAG_MATH_SUBS2:                                     // 减法源2
                case CommandMsgToUI.FLAG_MATH_MULS2:                                     // 乘法源2
                case CommandMsgToUI.FLAG_MATH_DIVS2: {                                   // 除法源2
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    dWSource2.setSelectIndex(selectIndex);                                // 更新双波形源2
                    selectItemIndex(dWSource2.getId(), dWSource2.getSelectItem());        // 处理选中项变更
                    msgMath.setDwSource2(dWSource2.getSelectItem());                      // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTSOURCE: {                               // FFT源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    fftSource.setSelectIndex(selectIndex);                                // 更新FFT源
                    selectItemIndex(fftSource.getId(), fftSource.getSelectItem());        // 处理选中项变更
                    msgMath.setFftSource(fftSource.getSelectItem());                      // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTWINDOW: {                               // FFT窗函数变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    fftWindow.setSelectIndex(selectIndex);                                // 更新FFT窗函数
                    selectItemIndex(fftWindow.getId(), fftWindow.getSelectItem());        // 处理选中项变更
                    msgMath.setFftWindow(fftWindow.getSelectItem());                      // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTTYPE: {                                 // FFT类型变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    fftType.setSelectIndex(selectIndex);                                  // 更新FFT类型
                    selectItemIndex(fftType.getId(), fftType.getSelectItem());            // 处理选中项变更
                    msgMath.setFftType(fftType.getSelectItem().getText());                // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_Source: {                              // AX+B源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int selectIndex = Integer.parseInt(params[1]);                        // 选中索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    axbSource.setSelectIndex(selectIndex);                                // 更新AX+B源
                    selectItemIndex(axbSource.getId(), axbSource.getSelectItem());        // 处理选中项变更
                    msgMath.setAxbSource(axbSource.getSelectItem());                      // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_A: {                                   // AX+B系数A变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String show = String.valueOf(params[1]);                              // 系数A值
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    axbA.setText(show);                                                   // 更新UI显示
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber, show); // 写入缓存
                    setScopeAxb(mathChannelNumber);                                       // 更新AX+B底层参数
                    msgMath.setAxbA(axbA.getText().toString());                           // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_B: {                                   // AX+B系数B变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String show = String.valueOf(params[1]);                              // 系数B值
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    axbB.setText(show);                                                   // 更新UI显示
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber, show); // 写入缓存
                    setScopeAxb(mathChannelNumber);                                       // 更新AX+B底层参数
                    msgMath.setAxbB(axbB.getText().toString());                           // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_UNIT: {                                // AX+B单位变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String result = String.valueOf(params[1]);                            // 单位值
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    axbUnit.setText(result);                                              // 更新UI显示
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber, result); // 写入缓存
                    setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);  // 更新缓存和底层
                    msgMath.setAxbUnit(result);                                           // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Express: {                             // 高级数学公式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String param = String.valueOf(params[1]);                             // 公式值
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    aMFormula.setText(param);                                             // 更新UI显示
                    setScopeAM(TChan.toUiChNo(chIndex));                                  // 更新高级数学底层参数
                    msgMath.setAmFormula(aMFormula.getText().toString());                 // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Var1: {                                // 高级数学变量1变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String param2 = String.valueOf(params[1]);                            // 变量1值（格式：numberEpower）
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    String[] param = param2.split("E");                                   // 分离数值和指数
                    aMVar1Number.setText(param[0]);                                       // 更新数值显示
                    aMVar1Power.setText(param[1]);                                        // 更新指数显示
                    msgMath.setAmVar1Number(aMVar1Number.getText().toString());           // 更新消息-数值
                    msgMath.setAmVar1Power(aMVar1Power.getText().toString());             // 更新消息-指数
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Var2: {                                // 高级数学变量2变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String param2 = String.valueOf(params[1]);                            // 变量2值（格式：numberEpower）
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    String[] param = param2.split("E");                                   // 分离数值和指数
                    aMVar2Number.setText(param[0]);                                       // 更新数值显示
                    aMVar2Power.setText(param[1]);                                        // 更新指数显示
                    msgMath.setAmVar2Number(aMVar2Number.getText().toString());           // 更新消息-数值
                    msgMath.setAmVar2Power(aMVar2Power.getText().toString());             // 更新消息-指数
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Unit: {                                // 高级数学单位变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String param = String.valueOf(params[1]);                             // 单位值
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    aMUnit.setText(param);                                                // 更新UI显示
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber, param); // 写入缓存
                    setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber);   // 更新缓存和底层
                    msgMath.setAmUnit(aMUnit.getText().toString());                       // 更新消息
                    sendMsg();                                                            // 发送消息
                }
                break;
                case CommandMsgToUI.FLAG_MATH_VREF: {                                    // 数学通道垂直参考变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    int param = Integer.parseInt(params[1]);                              // 垂直参考索引
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    mathVerticalDetail.setSelectIndex(param);                             // 更新垂直档位选中项
                    onRightSlipViewSelectItemClickListener.onItemClick(mathVerticalDetail.getId(), mathVerticalDetail.getSelectItem()); // 触发选中监听
                }
                break;
                case CommandMsgToUI.FLAG_MATH_LABEL: {                                   // 数学通道标签变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String result = params[1];                                            // 标签文本
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    chLabel.setText(result);                                              // 更新UI显示
                    msgMath.setLabel(result);                                             // 更新消息
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result); // 写入缓存
                    WaveManage.get().setChannelLabel(mathChannelNumber, result);          // 同步到波形管理
                    setChannelLabel(chIndex, result);                                     // 同步到通道对象
                }
                break;
                case CommandMsgToUI.FLAG_MATH_LABEL_CLEAR: {                             // 数学通道标签清空
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                            // 通道索引
                    String result = "";                                                   // 空标签
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;          // 非当前通道则忽略
                    chLabel.setText(result);                                              // 清空标签显示
                    msgMath.setLabel(result);                                             // 更新消息
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result); // 写入缓存
                    WaveManage.get().setChannelLabel(mathChannelNumber, result);          // 同步到波形管理
                    setChannelLabel(chIndex, result);                                     // 同步到通道对象
                }

            }
        }
    };

    /**
     * 时基变更消费者
     * <p>当当前激活通道为数学通道且为高级数学模式时，更新高级数学表达式</p>
     */
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            int chIdx = ChannelFactory.getChActivate();                                  // 获取当前激活通道
            if (ChannelFactory.isMathCh(chIdx) && mathChannelNumber == chIdx + 1) {     // 是数学通道且匹配当前面板
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber); // 获取数学类型
                if (mathChannelNumber == TChan.toUiChNo(chIdx) && mathType == CacheUtil.MATHTYPE_AM) { // 高级数学模式

                    timeBase = msgTimeBase.getTimeBase();                                 // 更新时基字符串
                    //时基变化处理  待验证
                    Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), aMFormula.getText().toString(), false); // 重新发送公式命令

                    setScopeAM(mathChannelNumber);                                       // 更新高级数学底层参数
//                    sendMsg();
                }
            }
        }
    };

    /**
     * 数学通道开关消费者
     * <p>当右侧其他消息中的数学通道开关状态变更时，同步更新UI和FPGA</p>
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            if (mSwitchBox.isState() == mainRightMsgOthers.getMath(mathChannelNumber).isValue()) { // 状态一致则跳过
                return;
            }
            boolean isOpen = mainRightMsgOthers.getMath(mathChannelNumber).isValue();    // 获取目标开关状态
            mSwitchBox.setState(isOpen);                                                  // 更新开关状态
            Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), isOpen && isAddByUser(mathChannelNumber), false); // 发送显示命令
            msgMath.setMathCheck(mSwitchBox.isState());                                   // 更新消息

            if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber)) { // 通道关闭
                ShowFFTMeasureInfo(false);                                                // 隐藏FFT测量信息
            } else {                                                                     // 通道打开
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber); // 获取数学类型
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);                  // 根据类型显示FFT测量信息
            }
        }
    };

    /**
     * FFT波形更新事件观察者
     * <p>当FFT波形数据更新时，计算DC/Amp/Freq值并更新测量信息显示</p>
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_MATH_WAVE_UPDATE) {     // FFT波形更新事件
                int mathNumber = (int) ((EventBase) data).getData();                      // 获取数学通道FPGA编号
                if(TChan.toUiChNo(mathNumber) != mathChannelNumber) return;               // 非当前通道则忽略
                String DC = "---";                                                        // DC默认值
                String Amp = "---";                                                       // Amp默认值
                String Freq = "---";                                                      // Freq默认值
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道
                if (mathChannel == null) return;                                          // 通道为空则返回
                int type = mathChannel.getMathFFTWave().getFFTType();                     // 获取FFT类型
                if (MathFFTWave.FFT_TYPE_DB == type) {                                    // dB模式
                    // db
                    DC = String.format("%.2f", mathChannel.getFFTDCVal()) + "dB";         // DC值（dB）
                    Amp = String.format("%.2f", mathChannel.getFFTMaxVal()) + "dB";       // Amp值（dB）
                } else if (MathFFTWave.FFT_TYPE_RMS == type) {                            // RMS模式
                    // 幅值 v
                    String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE
                            + (TChan.toUiChNo(fftSource.getSelectIndex()))) == 0 ? "V" : "A"; // 根据探头类型确定单位
                    DC = TBookUtil.getFourFromD_(mathChannel.getFFTDCVal()) + unit;       // DC值（V/A）
                    Amp = TBookUtil.getFourFromD_(mathChannel.getFFTMaxVal()) + unit;     // Amp值（V/A）
                }
                Freq = TBookUtil.getFourFromD_(mathChannel.getFFTMaxIdxFreq()) + "Hz";   // Freq值（Hz）
                MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setData(DC, Amp, Freq); // 更新FFT测量数据
            }
        }
    };

    /**
     * 正常/共享布局模式切换消费者
     * <p>根据isNormal参数切换面板的布局样式：
     * normal模式：显示开关、标题、档位按钮；隐藏Math/Ref/Serial切换
     * 共享模式：隐藏开关、标题、档位按钮；显示Math/Ref/Serial切换</p>
     */
    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);                  // 解析参数
            int channelNumber = Integer.parseInt(params[0]);                              // 通道编号
            boolean isNormal = Boolean.parseBoolean(params[1]);                           // 是否为普通模式

            if (channelNumber != mathChannelNumber) return;                               // 非当前通道则忽略
            if (isNormal) {//M1...M8布局样式
                llDisplaySwitch.setVisibility(View.VISIBLE);//switch 显示
                topChannelTitle.setVisibility(View.VISIBLE);//Mx delete按钮 显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.GONE);//Math/Ref/Serial 不显示
                clImgBtn.setVisibility(View.VISIBLE);//显示挡位调节按钮
            } else {//Math/Ref/Serials共同显示布局样式
                llDisplaySwitch.setVisibility(View.INVISIBLE);//switch 不显示
                topChannelTitle.setVisibility(View.INVISIBLE);//Mx delete按钮 不显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.VISIBLE);//Math/Ref/Serial 显示
                clImgBtn.setVisibility(View.INVISIBLE);//不显示挡位调节按钮
            }
            String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber); // 读取标签
            chLabel.setText(label);                                                       // 恢复标签显示
        }
    };

    /**
     * 通道添加可用性消费者
     * <p>根据可用性控制Ref/Serials RadioButton的启用状态，
     * 同时考虑缩放模式和慢时基对串口的限制</p>
     */
    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return; // 控件未初始化
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);               // 解析参数
            int mathSlideIndex = Integer.parseInt(params[0]);                             // 滑出菜单索引
            boolean mathAvailable = Boolean.parseBoolean(params[1]);                      // 数学通道是否可用
            boolean refAvailable = Boolean.parseBoolean(params[2]);                       // 参考通道是否可用
            boolean serialAvailable = Boolean.parseBoolean(params[3]);                    // 串口通道是否可用
            int mathChanNumber = TChan.Math1;                                             // 默认Math1
            switch (mathSlideIndex) {
                case MainViewGroup.RIGHTSLIP_MATH1:
                    mathChanNumber = TChan.Math1;                                         // 第1个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH2:
                    mathChanNumber = TChan.Math2;                                         // 第2个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH3:
                    mathChanNumber = TChan.Math3;                                         // 第3个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH4:
                    mathChanNumber = TChan.Math4;                                         // 第4个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH5:
                    mathChanNumber = TChan.Math5;                                         // 第5个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH6:
                    mathChanNumber = TChan.Math6;                                         // 第6个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH7:
                    mathChanNumber = TChan.Math7;                                         // 第7个数学通道
                    break;
                case MainViewGroup.RIGHTSLIP_MATH8:
                    mathChanNumber = TChan.Math8;                                         // 第8个数学通道
                    break;
            }
            if (mathChanNumber == mathChannelNumber) {                                    // 当前面板对应的通道
                channelRef.setEnabled(refAvailable);                                      // 设置参考通道可用性
                channelSerials.setEnabled(serialAvailable);                                // 设置串口通道可用性
            } else {                                                                     // 其他面板
                channelRef.setEnabled(true);                                              // 启用参考通道
                channelSerials.setEnabled(true);                                          // 启用串口通道
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);  // 是否缩放模式
            boolean slowTimeBase = Tools.isSlowTimeBase();                               // 是否慢时基
            if (zoom && slowTimeBase) {                                                   // 缩放+慢时基时
                channelSerials.setEnabled(false);                                         // 禁用串口通道
            }
        }
    };


    /**
     * 垂直档位调节消费者
     * <p>接收垂直档位调节方向和通道编号，更新按钮背景图并触发档位变更</p>
     */
    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        @Override
        public void accept(String adjustStr) throws Throwable {
            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);               // 解析参数
            boolean isClickTop = Boolean.parseBoolean(param[0]);                          // 是否点击向上
            int chan = Integer.parseInt(param[1]);                                        // 通道编号
            if (chan != mathChannelNumber) return;                                        // 非当前通道则忽略
            if (isClickTop) {                                                             // 点击向上
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);  // 设置向上按钮背景
                msgMath.setUpClick(true);                                                 // 标记向上调节
                postChange();                                                             // 发送档位变更
            } else {                                                                      // 点击向下
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);  // 设置向下按钮背景
                msgMath.setUpClick(false);                                                // 标记向下调节
                postChange();                                                             // 发送档位变更
            }
        }
    };

    /**
     * 顶部显示消息消费者
     * <p>当FFT信息显示索引变更时，更新当前面板的FFT测量信息显示状态</p>
     */
    private Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayFftInfo) { // FFT信息显示消息
                TopMsgDisplayFftInfo displayFftInfo = (TopMsgDisplayFftInfo) topMsgDisplay.getDisplayDetail(); // 获取FFT信息
//                boolean switchState = displayFftInfo.isShowFftInfo().isValue();
//                int mathIndex = displayFftInfo.getFftInfoIndex().getValue();
//                if (switchState) {
//                    isShowFftInfo = mathChannelNumber == TChan.toMathChan(mathIndex + 1);
//                } else {
                int index = displayFftInfo.getFftInfoIndex().getValue();                  // 获取FFT信息索引
                if (index != 0) {                                                         // 索引非0
                    isShowFftInfo = mathChannelNumber == TChan.toMathChan(index);         // 判断是否为当前通道
                } else {                                                                  // 索引为0
                    isShowFftInfo = false;                                                 // 不显示FFT信息
                }
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber); // 获取数学类型
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);                  // 更新FFT测量信息显示
            }
        }
    };

    /**
     * 通道删除消费者
     * <p>当收到通道删除消息且通道编号匹配时，触发删除按钮点击</p>
     */
    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == mathChannelNumber) {                                           // 通道编号匹配
                btnDeleteChannel.performClick();                                          // 模拟点击删除按钮
            }
        }
    };

    /**
     * 显示/隐藏FFT测量信息
     * <p>当满足FFT模式+开关打开+FFT信息显示标记时，显示FFT测量信息标签</p>
     * @param isSelectFftMode 是否选中FFT模式
     */
    private void ShowFFTMeasureInfo(boolean isSelectFftMode) {
        boolean isFftMode = isSelectFftMode && mSwitchBox.isState() && isShowFftInfo;    // 综合判断是否显示
        String DC = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_DC); // DC标签
        String Amp = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_Amp); // Amp标签
        String Freq = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_Freq); // Freq标签
        MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setLabel(DC, Amp, Freq); // 设置标签
        MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setVisible(isFftMode); // 设置可见性
    }

    /**
     * 处理选择项索引变更
     * <p>根据选择控件的ID，更新对应的缓存和底层通道参数</p>
     * @param viewId 触发变更的控件ID
     * @param item 选中的Bean项
     */
    @SuppressLint("NonConstantResourceId")
    private void selectItemIndex(int viewId, RightBeanSelect item) {
        Tools.PrintControlsLocation("RightLayoutMath", rootView);                        // 打印控件位置信息
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道对象
        switch (viewId) {
            case R.id.doubleWaveSource1:                                                  // 双波形源1
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);       // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE1 + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());     // 设置源1
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());     // 更新运算符
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());     // 更新源2
                msgMath.setDwSource1(item);                                               // 更新消息
                break;
            case R.id.doubleWaveSource2:                                                  // 双波形源2
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);       // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE2 + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());     // 更新源1
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());     // 更新运算符
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());     // 设置源2
                msgMath.setDwSource2(item);                                               // 更新消息
                break;
            case R.id.doubleWaveSymbol:                                                   // 双波形运算符
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);       // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SYMBOL + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());     // 更新源1
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());     // 设置运算符
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());     // 更新源2
                msgMath.setDwSymbol(item);                                                // 更新消息
                break;
            case R.id.fftWindow:                                                         // FFT窗函数
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);      // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_WINDOW + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());        // 更新FFT类型
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());    // 设置FFT窗函数
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());       // 更新FFT源
                msgMath.setFftWindow(item);                                               // 更新消息
                break;
            case R.id.fftSource:                                                         // FFT源
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);      // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_SOURCE + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());        // 更新FFT类型
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());    // 更新FFT窗函数
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());       // 设置FFT源
                msgMath.setFftSource(item);                                               // 更新消息
                break;
            case R.id.fftPersist:                                                        // FFT持久化
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);      // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());        // 更新FFT类型
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());    // 更新FFT窗函数
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());       // 更新FFT源
                setFftPersistValueEnable();                                               // 更新持久化值可用性
                msgMath.setFftWindow(item);                                               // 更新消息-窗函数
                msgMath.setFftPersist(fftPersist.getSelectItem());                        // 更新消息-持久化
                break;
            case R.id.axbSource:                                                         // AX+B源
                if(mathChannel == null) return;
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber); // 读取AX+B单位
                this.axbUnit.setText(axbUnit);                                            // 更新单位显示
                msgMath.setAxbUnit(axbUnit);                                              // 更新消息-单位
                setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);      // 设置缓存数学类型
                setScopeAxb(mathChannelNumber);                                           // 更新AX+B底层参数
                msgMath.setAxbSource(item);                                               // 更新消息-源
                break;
            case R.id.fftType:                                                           // FFT类型（dB/RMS）
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);      // 设置缓存数学类型
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());        // 设置FFT类型
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());    // 更新FFT窗函数
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());       // 更新FFT源
                setMathPos(CacheUtil.MATHTYPE_FFT);                                       // 重新计算通道位置
                mathChannel.setProbeType(mathChannel.generateProbeType());                // 更新探头类型
                msgMath.setFftType(item.getText());                                        // 更新消息
                break;
            case R.id.mathVerticalBaseDetail:                                            // 垂直档位
                if(mathChannel == null) return;
                mathChannel.setVerticalMode(item.getIndex());                             // 设置垂直模式
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_VERTICALBASE + mathChannelNumber, String.valueOf(item.getIndex())); // 写入缓存
                break;
            case R.id.math_channel_type:                                                 // 数学类型切换
//                if (mathChannelType.getSelectItem().getIndex() == item.getIndex()) return;
                PlaySound.getInstance().playSlide();                                      // 播放滑出音效
                setCacheMathType(item.getIndex(), false, mathChannelNumber);              // 设置缓存数学类型
                setShowMathTypeLayout(item.getIndex());                                   // 显示对应布局
                setMathPos(item.getIndex());                                              // 重新计算通道位置
                msgMath.setMathType(item.getIndex());                                     // 更新消息-数学类型
                msgMath.setMathTypeSelect(item);                                          // 更新消息-数学类型选择项
                Command.get().getMath().Mode(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送数学模式命令
                break;
        }
        sendMsg();                                                                       // 发送消息通知订阅方
    }

    /**
     * 开关状态变更监听器
     * <p>当通道开关切换时：
     * 1. 关闭时隐藏所有对话框
     * 2. 控制FFT测量信息显示
     * 3. 向FPGA发送显示命令
     * 4. 更新缓存和消息
     * 5. 关闭时发送强制隐藏垂直档位消息</p>
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            if (view.getId() == R.id.channel_switch) {                                   // 通道开关
                if (!mSwitchBox.isState()) {                                              // 关闭状态
                    ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();      // 隐藏所有对话框
                }
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber); // 获取数学类型
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);                  // 控制FFT测量信息显示
                Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), state && isAddByUser(mathChannelNumber), false); // 发送显示命令
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber, String.valueOf(mSwitchBox.isState())); // 写入缓存
                msgMath.setMathCheck(mSwitchBox.isState());                               // 更新消息
                sendMsg();                                                                // 发送消息
                if (!state) {                                                             // 关闭时
                    //关闭垂直调节按钮
                    RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true); // 强制隐藏垂直档位
                }
//                这里暂时不需要移动MasterView位置
//                updateMaxChannelNumber();
//                if (needUpdateMasterLocation) {
//                    needUpdateMasterLocation = false;
//                    RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_MASTER_LOCATION, mathChannelNumber);
//                }
            }
        }
    };

    /**
     * 点击事件监听器
     * <p>处理AX+B单位/系数A/系数B、高级数学单位/公式/变量、FFT持久化值、
     * 档位按钮、删除通道、Math/Ref/Serial切换、添加通道等点击事件</p>
     */
    private OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            if (layoutTextKeyBoard == null) layoutTextKeyBoard = ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 懒加载文本键盘
            if (layoutFloatKeyBoard == null) layoutFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 懒加载浮点键盘
            if (layoutFullFloatKeyBoard == null) layoutFullFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard); // 懒加载全精度键盘
            if (layoutFormulaKeyBoard == null) layoutFormulaKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFormulaKeyBoard); // 懒加载公式键盘
            if (layoutNumberPicker == null) layoutNumberPicker = ((MainActivity) context).findViewById(R.id.dialogNumberPicker); // 懒加载数字选择器
            if (layoutMathFFTPersist == null) layoutMathFFTPersist = ((MainActivity) context).findViewById(R.id.dialogMathFFTPersist); // 懒加载FFT持久化对话框
            PlaySound.getInstance().playButton();                                         // 播放按钮音效
            switch (v.getId()) {
                case R.id.axbUnitDetail:                                                  // AX+B单位输入
                    layoutTextKeyBoard.setDataEnglish(TopDialogTextKeyBoard.HANDLE_TYPE_MATH,
                            axbUnit.getText().toString(), 3, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            axbUnit.setText(result);                                      // 更新单位显示
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber, result); // 写入缓存
                            setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber); // 更新缓存和底层
                            msgMath.setAxbUnit(result);                                    // 更新消息
                            sendMsg();                                                    // 发送消息
                            Command.get().getMath_axb().Unit(TChan.toFpgaChNo(mathChannelNumber), result, false); // 发送AX+B单位命令
                        }
                    });
                    break;
                case R.id.axbADetail:                                                     // AX+B系数A输入
                    layoutFloatKeyBoard.setFloatData(axbA.getText().toString(), axbA, new TopDialogFloatKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(View fromView, String show) {
                            axbA.setText(show);                                            // 更新系数A显示
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber, show); // 写入缓存
                            setScopeAxb(mathChannelNumber);                                // 更新AX+B底层参数
                            msgMath.setAxbA(axbA.getText().toString());                    // 更新消息
                            sendMsg();                                                    // 发送消息
                            Command.get().getMath_axb().A(TChan.toFpgaChNo(mathChannelNumber), show, true); // 发送系数A命令
                        }
                    });
                    break;
                case R.id.axbBDetail:                                                     // AX+B系数B输入
                    layoutFloatKeyBoard.setFloatData(axbB.getText().toString(), axbB, new TopDialogFloatKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(View fromView, String show) {
                            axbB.setText(show);                                            // 更新系数B显示
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber, show); // 写入缓存
                            setScopeAxb(mathChannelNumber);                                // 更新AX+B底层参数
                            msgMath.setAxbB(axbB.getText().toString());                    // 更新消息
                            sendMsg();                                                    // 发送消息
                            Command.get().getMath_axb().B(TChan.toFpgaChNo(mathChannelNumber), show, true); // 发送系数B命令
                        }
                    });
                    break;
                case R.id.advanceMathUnitDetail:                                          // 高级数学单位输入
                    layoutTextKeyBoard.setDataEnglish(TopDialogTextKeyBoard.HANDLE_TYPE_MATH,
                            aMUnit.getText().toString(), 3, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            aMUnit.setText(result);                                        // 更新单位显示
                            Command.get().getMath_advanced().Unit(TChan.toFpgaChNo(mathChannelNumber), aMUnit.getText().toString(), false); // 发送单位命令
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber, result); // 写入缓存
                            setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber); // 更新缓存和底层
                            msgMath.setAmUnit(result);                                     // 更新消息
                            sendMsg();                                                    // 发送消息
                        }
                    });
                    break;
                case R.id.advanceMathFormula:                                             // 高级数学公式输入
                    if (timeBase.isEmpty()) {                                              // 时基为空
                        timeBase = MainHolderBottom.getCenterTimeBase();                   // 获取当前时基
                    }
                    layoutFormulaKeyBoard.setData(aMFormula.getText().toString(), timeBase, new TopDialogFormulaKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(String show) {
                            aMFormula.setText(show);                                       // 更新公式显示
                            Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), aMFormula.getText().toString(), false); // 发送公式命令
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + mathChannelNumber, show); // 写入缓存
                            setScopeAM(mathChannelNumber);                                 // 更新高级数学底层参数
                            if (show.contains(getResources().getString(R.string.key_formula_diff)) && show.contains("ch")) { // 包含微分和通道
                                // 微分时需要设置采样为平均
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChannelNumber, String.valueOf(true)); // 标记含微分
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + mathChannelNumber, String.valueOf(true)); // 标记微分重置
                            } else {                                                      // 不含微分
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChannelNumber, String.valueOf(false)); // 清除微分标记
                            }
                            msgMath.setAmFormula(show);                                    // 更新消息
                            sendMsg();                                                    // 发送消息
                        }
                    });
                    break;
                case R.id.advanceMathVar1Number:                                          // 变量1数值/指数
                case R.id.advanceMathVar1Power:
                    layoutNumberPicker.setData(aMVar1Number.getText().toString(), aMVar1Power.getText().toString(), new TopDialogNumberPicker.OnDismissListener() {
                        @Override
                        public void onDismiss(String number, String power) {
                            aMVar1Number.setText(number);                                  // 更新变量1数值
                            aMVar1Power.setText(power);                                    // 更新变量1指数
                            double mVar1 = Double.parseDouble(aMVar1Number.getText().toString()) * Math.pow(10, Double.parseDouble(aMVar1Power.getText().toString())); // 计算实际值
                            Command.get().getMath_advanced().Var1(TChan.toFpgaChNo(mathChannelNumber), mVar1, false); // 发送变量1命令
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + mathChannelNumber, number); // 写入缓存-数值
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + mathChannelNumber, power); // 写入缓存-指数
                            setScopeAM(mathChannelNumber);                                 // 更新高级数学底层参数
                            msgMath.setAmVar1Number(number);                               // 更新消息-数值
                            msgMath.setAmVar1Power(power);                                 // 更新消息-指数
                            sendMsg();                                                    // 发送消息
                        }
                    });
                    break;
                case R.id.advanceMathVar2Number:                                          // 变量2数值/指数
                case R.id.advanceMathVar2Power:
                    layoutNumberPicker.setData(aMVar2Number.getText().toString(), aMVar2Power.getText().toString(), new TopDialogNumberPicker.OnDismissListener() {
                        @Override
                        public void onDismiss(String number, String power) {
                            aMVar2Number.setText(number);                                  // 更新变量2数值
                            aMVar2Power.setText(power);                                    // 更新变量2指数
                            double mVar2 = Double.parseDouble(aMVar2Number.getText().toString()) * Math.pow(10, Double.parseDouble(aMVar2Power.getText().toString())); // 计算实际值
                            Command.get().getMath_advanced().Var2(TChan.toFpgaChNo(mathChannelNumber), mVar2, false); // 发送变量2命令
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + mathChannelNumber, number); // 写入缓存-数值
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + mathChannelNumber, power); // 写入缓存-指数
                            setScopeAM(mathChannelNumber);                                 // 更新高级数学底层参数
                            msgMath.setAmVar2Number(number);                               // 更新消息-数值
                            msgMath.setAmVar2Power(power);                                 // 更新消息-指数
                            sendMsg();                                                    // 发送消息
                        }
                    });
                    break;
                case R.id.fftPersistValue:                                                // FFT持久化值设置
                    Tools.getViewRect(fftPersist);                                         // 获取控件位置
                    layoutMathFFTPersist.setData(mathChannelNumber, fftPersistValue.getText().toString(), CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + mathChannelNumber, (result) -> {
                        fftPersistValue.setText(result);                                   // 更新持久化值显示
                    });
                    break;
                case R.id.btnTop:                                                         // 档位向上按钮
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174); // 设置向上背景
                    msgMath.setUpClick(true);                                              // 标记向上调节
                    postChange();                                                          // 发送档位变更
                    break;
                case R.id.btnBottom:                                                      // 档位向下按钮
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174); // 设置向下背景
                    msgMath.setUpClick(false);                                             // 标记向下调节
                    postChange();                                                          // 发送档位变更
                    break;
                case R.id.btn_delete_channel:                                             // 删除通道按钮
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChannelNumber, String.valueOf(false)); // 标记非用户添加
                    mSwitchBox.setState(false);                                            // 关闭开关
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState()); // 触发开关监听
//                    resetDefaultColor(mathChannelNumber);
                    break;
                case R.id.channelMath:                                                    // 切换到数学通道
                    setDisplayState();                                                     // 设置显示状态
                    break;
                case R.id.channelRef:                                                     // 切换到参考通道
                    setDisplayState();                                                     // 设置显示状态
                    hideSlip();                                                            // 隐藏当前面板
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 1);        // 通知显示参考通道
                    break;
                case R.id.channelSerials:                                                 // 切换到串口通道
                    setDisplayState();                                                     // 设置显示状态
                    hideSlip();                                                            // 隐藏当前面板
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 2);        // 通知显示串口通道
                    break;
                case R.id.btn_add_channel:                                                // 添加通道按钮
                    needUpdateMasterLocation = true;                                       // 标记需要更新MasterView位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChannelNumber, String.valueOf(true)); // 标记用户添加
                    mSwitchBox.setState(true);                                             // 打开开关
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState()); // 触发开关监听
                    if (mathChannelType.getSelectIndex() == 0) { //双波形                  // 双波形类型时
                        onRightSlipViewSelectItemClickListener.onItemClick(dWSource1.getId(), dWSource1.getSelectItem()); // 触发源1选择
                    }
                    hideSlip();                                                            // 隐藏当前面板
                    break;
            }
        }
    };

    /**
     * 隐藏当前滑出面板
     * <p>根据当前数学通道编号发送滑出菜单关闭消息</p>
     */
    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_MATH1;                                    // 默认Math1的滑出索引
        switch (mathChannelNumber) {
            case TChan.Math1: slipIndex = MainViewGroup.RIGHTSLIP_MATH1;break;            // Math1
            case TChan.Math2: slipIndex = MainViewGroup.RIGHTSLIP_MATH2;break;            // Math2
            case TChan.Math3: slipIndex = MainViewGroup.RIGHTSLIP_MATH3;break;            // Math3
            case TChan.Math4: slipIndex = MainViewGroup.RIGHTSLIP_MATH4;break;            // Math4
            case TChan.Math5: slipIndex = MainViewGroup.RIGHTSLIP_MATH5;break;            // Math5
            case TChan.Math6: slipIndex = MainViewGroup.RIGHTSLIP_MATH6;break;            // Math6
            case TChan.Math7: slipIndex = MainViewGroup.RIGHTSLIP_MATH7;break;            // Math7
            case TChan.Math8: slipIndex = MainViewGroup.RIGHTSLIP_MATH8;break;            // Math8
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false)); // 发送关闭滑出菜单消息
    }

    /**
     * 检查数学通道是否可用
     * <p>判断下一个可用的数学通道是否存在MathChannel对象</p>
     */
    private void checkMathAvailable() {
        int lastMathChan = getMaxMathChannelNumber();                                     // 获取最大数学通道编号
        Logger.d(TAG, "lastMathChan = " + lastMathChan);                                 // 打印日志
        if (TChan.isMath(lastMathChan + 1)) {                                            // 下一个通道是数学通道
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(lastMathChan + 1)); // 获取通道对象
            if (mathChannel == null) {                                                    // 通道对象不存在
                channelMath.setEnabled(false);                                            // 禁用数学通道选项
            }
        } else {                                                                         // 下一个通道不是数学通道
            channelMath.setEnabled(false);                                                // 禁用数学通道选项
        }
    }

    /**
     * 获取当前最大已添加的数学通道编号
     * <p>遍历所有数学通道，找出由用户添加的通道中编号最大的</p>
     * @return 最大数学通道编号
     */
    public int getMaxMathChannelNumber() {
        final int[] maxMathChan = {TChan.Math1 - TChan.Ch1};                             // 初始值
        TChan.foreachMath(mathChan -> {                                                  // 遍历所有数学通道
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan); // 读取开关状态
//            if (mathCheck) {
            if (isAddByUser(mathChan)) {                                                 // 由用户添加
                maxMathChan[0] = Math.max(maxMathChan[0], mathChan);                     // 更新最大值
            }
        });
        return maxMathChan[0];                                                           // 返回最大编号
    }

    /**
     * 设置通道类型切换按钮的显示状态
     * <p>选中数学通道，取消选中参考通道和串口通道</p>
     */
    private void setDisplayState() {
        channelMath.setChecked(true);                                                    // 选中数学通道
        channelRef.setChecked(false);                                                    // 取消选中参考通道
        channelSerials.setChecked(false);                                                // 取消选中串口通道
    }

    /**
     * RightViewSelect选择项点击监听器
     * <p>处理数学类型、双波形源/运算符、FFT参数、AX+B源、垂直档位等选择项的点击事件。
     * 点击时先通过Command向FPGA发送指令，再调用selectItemIndex更新UI和底层参数。</p>
     */
    private RightViewSelect.OnItemClickListener onRightSlipViewSelectItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();                                         // 播放按钮音效
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            switch (viewId) {
                case R.id.doubleWaveSource1:                                              // 双波形源1
                    Command.get().getMath_base().S1(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送源1命令
                    break;
                case R.id.doubleWaveSource2:                                              // 双波形源2
                    Command.get().getMath_base().S2(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送源2命令
                    break;
                case R.id.doubleWaveSymbol:                                               // 双波形运算符
                    Command.get().getMath_base().Operator(TChan.toFpgaChNo(mathChannelNumber), dWSymbol.getSelectItem().getIndex(), false); // 发送运算符命令
                    break;
                case R.id.fftWindow:                                                     // FFT窗函数
                    Command.get().getMath_fft().Window(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送窗函数命令
                    break;
                case R.id.fftSource:                                                     // FFT源
                    Command.get().getMath_fft().Source(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送源命令
                    break;
                case R.id.axbSource:                                                     // AX+B源
                    Command.get().getMath_axb().Source(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送源命令
                    break;
                case R.id.fftType:                                                       // FFT类型
                    Command.get().getMath_fft().Type(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送类型命令
                    break;
                case R.id.mathVerticalBaseDetail:                                        // 垂直档位
                    Command.get().getMath().VRef(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false); // 发送垂直参考命令
                    break;
            }
            selectItemIndex(viewId, item);                                                // 处理选中项变更
        }

        /**
         * 选择项点击后刷新UI回调
         * <p>当不是强制点击时，根据控件ID更新对应的底层通道参数</p>
         */
        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            if (!isCurClickForce) {                                                       // 非强制点击
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber)); // 获取数学通道对象
                if(mathChannel == null) return;
                if (viewId == dWSource1.getId() || viewId == dWSource2.getId()
                        || viewId == dWSymbol.getId()) {                                  // 双波形参数变更
                    setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);    // 设置缓存数学类型
                    mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());  // 更新源1
                    mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());  // 更新运算符
                    mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());  // 更新源2
                } else if (viewId == fftType.getId() || viewId == fftWindow.getId() || viewId == fftSource.getId()) { // FFT参数变更

                    setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);   // 设置缓存数学类型
                    mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());    // 更新FFT类型
                    mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex()); // 更新FFT窗函数
                    mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());   // 更新FFT源

                } else if (viewId == axbSource.getId()) {                                 // AX+B源变更
                    setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);   // 设置缓存数学类型
                }
            }
            sendMsg();                                                                    // 发送消息通知订阅方
        }

        /**
         * 选择项点击前刷新UI回调（当前未使用）
         */
        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    /**
     * 发送档位变更消息
     * <p>先移除已有的延迟消息，再延迟200ms后恢复按钮背景，
     * 同时通过RxBus发送数学通道档位变更消息</p>
     */
    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {                                         // 已有延迟消息
            myHandler.removeMessages(HANDLE_MSG);                                        // 移除旧消息
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);                              // 延迟200ms恢复背景
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_MATHX, msgMath);                 // 发送数学通道档位变更消息
    }


    /** Handler消息标识：恢复按钮背景 */
    private static final int HANDLE_MSG = 1;
    /**
     * 弱引用Handler内部类
     * <p>使用WeakReference持有外部类引用，防止内存泄漏。
     * 收到HANDLE_MSG消息时恢复档位按钮背景为默认状态。</p>
     */
    public static class MyHandler extends Handler {
        /** 外部类的弱引用 */
        private final WeakReference<RightLayoutMath> weakReference;

        /**
         * 构造方法
         * @param layoutMath 外部类引用
         */
        public MyHandler(RightLayoutMath layoutMath) {
            weakReference = new WeakReference<RightLayoutMath>(layoutMath);              // 创建弱引用
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference.get() != null) {                                            // 外部类未被回收
                if (msg.what == HANDLE_MSG) {                                             // 恢复背景消息
                    RightLayoutMath layoutMath = (RightLayoutMath) weakReference.get();   // 获取外部类引用
                    layoutMath.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174); // 恢复默认背景
                }
            }
        }
    }

    /**
     * 更新最大数学通道编号到缓存
     * <p>遍历所有数学通道，找出由用户添加的最大通道编号并写入缓存</p>
     */
    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.Math1 - 1);                      // 初始值
        TChan.foreachMath(mathChan -> {                                                  // 遍历所有数学通道
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan); // 读取开关状态
//            if (mathCheck) {
            if (isAddByUser(mathChan)) {                                                 // 由用户添加
                maxChan.set(Math.max(maxChan.get(), mathChan));                           // 更新最大值
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_MATH, maxChan.toString()); // 写入缓存
    }

    /**
     * 处理时基字符串
     * <p>提取时基的数值部分并转换为秒值</p>
     * @param timeBase 原始时基字符串（可能含换行符）
     * @return 处理后的时基秒值字符串
     */
    private String handleTimeBase(String timeBase) {
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase;          // 去除换行符前的部分
        if (timeBase.isEmpty()) {                                                         // 空字符串
            timeBase = "1";                                                               // 默认值1
        } else {                                                                         // 非空
            timeBase = (TBookUtil.getSFromTime(timeBase) + "").replaceAll(" ", "");       // 转换为秒值并去空格
        }
//        Logger.d("limh", "timeBase= " + timeBase);
        return timeBase;                                                                  // 返回处理后的时基
    }

    /**
     * 判断指定数学通道是否由用户添加
     * @param mathChan 数学通道编号
     * @return true=由用户添加, false=非用户添加
     */
    private boolean isAddByUser(int mathChan) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan); // 从缓存读取
    }


    /**
     * 编辑点击监听器（通道标签和颜色选择）
     * <p>处理通道标签编辑和颜色选择点击事件</p>
     */
    @SuppressLint("NonConstantResourceId")
    private final TopViewEdit.OnClickEditListener onClickEditListener = (v, text) -> {
        PlaySound.getInstance().playButton();                                             // 播放按钮音效
        switch (v.getId()) {
            case R.id.chLabel:                                                            // 通道标签编辑
                if (dialogChannelLabel == null) {
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel); // 懒加载标签对话框
                }
                dialogChannelLabel.setData(mathChannelNumber, chLabel.getText()           // 设置对话框数据
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + mathChannelNumber    // 用户自定义标签缓存键
                        , DialogChannelLabel.FROM_MATHREF                                  // 来源：数学/参考通道
                        , result -> {
                            PlaySound.getInstance().playButton();                          // 播放按钮音效
                            chLabel.setText(result);                                       // 更新标签显示
                            msgMath.setLabel(result);                                      // 更新消息
                            //Command.get().getChannel().Label(refChannelNumber - 1, result, false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result); // 写入缓存
                            WaveManage.get().setChannelLabel(mathChannelNumber, result);   // 同步到波形管理
                            setChannelLabel(TChan.toFpgaChNo(mathChannelNumber), result);  // 同步到通道对象
                        }
                );
                break;
            case R.id.select_color:                                                       // 颜色选择
                if (dialogSelectColor == null) {
                    dialogSelectColor = (DialogSelectColor) ((MainActivity)context).findViewById(R.id.dialogSelectColor); // 懒加载颜色对话框
                }
                dialogSelectColor.setData(DialogSelectColor.FROM_MATHREF, mathChannelNumber, (chIndex, colorStr) -> { // 设置对话框数据
                    if (mathChannelNumber == chIndex) {                                    // 当前通道匹配
                        Logger.d(TAG, "选中的颜色值为：" + colorStr + " channelNum= " + chIndex); // 打印日志
                        selectColor.setEditColor(colorStr);                                // 更新颜色显示
                    }
                });
                break;
            default:
                break;
        }
    };

    /**
     * 设置数学通道标签
     * @param chNo FPGA通道编号
     * @param label 标签文本
     */
    public void setChannelLabel(int chNo, String label) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(chNo);                   // 获取数学通道对象
        if (mathChannel != null) {                                                        // 通道对象存在
            mathChannel.setLabel(label);                                                  // 设置标签
        }
    }

    /**
     * 颜色选择消息消费者
     * <p>当其他地方变更了通道颜色时，同步更新当前面板的控件颜色和FFT测量信息颜色</p>
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;                                               // 空信息则忽略
            Logger.i(TAG, "selectColorInfo= " + colorInfo);                               // 打印日志
            String[] info = colorInfo.split(";");                                          // 分离通道索引和颜色值
            int chIndex = Integer.parseInt(info[0]);                                       // 通道索引
            String colorStr = info[1];                                                     // 颜色值
            if (chIndex == mathChannelNumber) {                                            // 当前通道匹配
                setOnlyControlColorByChIdx(chIndex);                                       // 更新控件颜色
                MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setColor(SvgNodeInfo.getAllBaseColorInt(mathChannelNumber)); // 更新FFT测量颜色
            }
        }
    };

    /**
     * 重置通道颜色为默认值
     * @param chIndex 通道索引
     */
    private void resetDefaultColor(int chIndex) {
        String colorDefault = SvgNodeInfo.getDefaultColor(chIndex);                      // 获取默认颜色
        SvgNodeInfo.setChannelColor(chIndex, colorDefault);//改变颜色值                    // 更新颜色值
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorDefault);     // 写入缓存
        selectColor.setEditColor(colorDefault);                                            // 更新颜色显示
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorDefault);//发消息通知颜色值改变了 // 发送颜色变更消息
    }

    /**
     * 鼠标点击位置消息消费者
     * <p>当鼠标点击波形区域的数学通道时，根据点击位置（垂直档位/垂直位置/水平位置）
     * 弹出相应的键盘对话框进行数值输入</p>
     */
    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");                                          // 分离通道索引和点击位置
            int chIdx = Integer.parseInt(info[0]);                                         // FPGA通道索引
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置 // 点击位置类型
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);       // 打印日志
            if (layoutFloatKeyBoard == null) {
                layoutFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 懒加载浮点键盘
            }
            if (layoutFullFloatKeyBoard == null) {
                layoutFullFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard); // 懒加载全精度键盘
            }
            if (TChan.toUiChNo(chIdx) != mathChannelNumber) return;                       // 非当前通道则忽略
            String unit = ChannelFactory.getProbeType(chIdx);                              // 获取探头类型单位
            MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);                // 获取数学通道对象
            if (mathChannel == null) return;                                                // 通道为空则返回
            if (clickPos == 0) { //垂直档位                                                 // 点击垂直档位
                double vScaleVal = mathChannel.getVScaleVal();                             // 获取当前垂直档位值
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) { // AX+B或高级数学
                    setChFullVScale(TBookUtil.getMFromDouble(vScaleVal) + unit, chIdx);    // 弹出全精度键盘
                } else {                                                                  // 双波形或FFT
                    setChVScale(TBookUtil.getMFromDouble(vScaleVal) + unit, chIdx);        // 弹出浮点键盘
                }
            }
            if (clickPos == 1) { //垂直位置                                                 // 点击垂直位置
                double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));        // 获取UI上的通道位置
                int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);                  // 缩放模式高度
                double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算偏移量
                String number = TBookUtil.getFourFromD_Trim0(pos * mathChannel.getVerticalPerPix()); // 转换为显示值
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) { // AX+B或高级数学
                    setChFullVPosition(number + unit, chIdx);                              // 弹出全精度键盘
                } else {                                                                  // 双波形或FFT
                    setChVPosition(number + unit, chIdx);                                  // 弹出浮点键盘
                }
            }
            if (clickPos == 3) { //水平位置                                                 // 点击水平位置
                int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 获取跟随通道
                String timePosStr = "";                                                    // 水平位置字符串
                if (mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {                  // 非FFT模式才处理
                    long timePos;
                    timePos = HorizontalAxis.getInstance().getTimePosOfView();              // 获取当前水平位置时间值
                    timePosStr = TBookUtil.getSFrom100Fs(timePos);                          // 转换为时间字符串
                    setChHPosition(timePosStr, chIdx);                                      // 弹出水平位置键盘
                }
            }
        }
    };

    /**
     * 设置水平位置（弹出浮点键盘）
     * @param nowTxt 当前水平位置文本
     * @param chIdx FPGA通道索引
     */
    //水平位置
    public void setChHPosition(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();                                                // 键盘置顶

        String unit = ChannelFactory.getProbeType(chIdx);                                  // 获取探头类型单位
        String txt = nowTxt.toString().replaceAll("(?:s|\\s)", "");                         // 去除s和空格
        layoutFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                      // 播放按钮音效
                double val = TBookUtil.getBigDoubleFromM(show);                             // 解析为double
                Command.get().getTimebase().Position(val, true);                            // 发送水平位置命令
            }
        });
    }

    /**
     * 设置垂直位置（弹出浮点键盘，用于双波形/FFT模式）
     * @param nowTxt 当前垂直位置文本
     * @param chIdx FPGA通道索引
     */
    //垂直位置
    private void setChVPosition(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();                                                // 键盘置顶

        String unit = ChannelFactory.getProbeType(chIdx);                                  // 获取探头类型单位
        String txt = nowTxt.replace(unit, "").replace(" ", "");                            // 去除单位和空格
        layoutFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                      // 播放按钮音效
                double val = TBookUtil.getDoubleFromM(show);                                // 解析为double
//                Logger.d("limh show= " + show + " unit= " + unit);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:                                            // 双波形
                        Command.get().getMath_base().Offset(chIdx, val, true);              // 发送双波形偏移命令
                        break;
                    case CacheUtil.MATHTYPE_FFT:                                           // FFT
                        Command.get().getMath_fft().Offset(chIdx, val, true);               // 发送FFT偏移命令
                        break;
                    case CacheUtil.MATHTYPE_AXB:                                           // AX+B
//                        Logger.d("limh val= " + val);
                        Command.get().getMath_axb().Offset(chIdx, val, true);               // 发送AX+B偏移命令
                        break;
                    case CacheUtil.MATHTYPE_AM:                                            // 高级数学
                        Command.get().getMath_advanced().Offset(chIdx, val, true);          // 发送高级数学偏移命令
                        break;
                }
            }
        });
    }

    /**
     * 设置垂直位置（弹出全精度浮点键盘，用于AX+B/高级数学模式）
     * @param nowTxt 当前垂直位置文本
     * @param chIdx FPGA通道索引
     */
    //垂直位置
    private void setChFullVPosition(String nowTxt, int chIdx) {
        layoutFullFloatKeyBoard.bringToFront();                                            // 键盘置顶

        String unit = ChannelFactory.getProbeType(chIdx);                                  // 获取探头类型单位
        String txt = nowTxt.replace(unit, "").replace(" ", "");                            // 去除单位和空格
        layoutFullFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFullFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                      // 播放按钮音效
                double val = TBookUtil.getDoubleFromM(show);                                // 解析为double
//                Logger.d("limh show= " + show + " unit= " + unit);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:                                            // 双波形
                        Command.get().getMath_base().Offset(chIdx, val, true);              // 发送双波形偏移命令
                        break;
                    case CacheUtil.MATHTYPE_FFT:                                           // FFT
                        Command.get().getMath_fft().Offset(chIdx, val, true);               // 发送FFT偏移命令
                        break;
                    case CacheUtil.MATHTYPE_AXB:                                           // AX+B
//                        Logger.d("limh val= " + val);
                        Command.get().getMath_axb().Offset(chIdx, val, true);               // 发送AX+B偏移命令
                        break;
                    case CacheUtil.MATHTYPE_AM:                                            // 高级数学
                        Command.get().getMath_advanced().Offset(chIdx, val, true);          // 发送高级数学偏移命令
                        break;
                }
            }
        });
    }

    /**
     * 设置垂直档位（弹出全精度浮点键盘，用于AX+B/高级数学模式）
     * @param nowTxt 当前垂直档位文本
     * @param chIdx FPGA通道索引
     */
    private void setChFullVScale(String nowTxt, int chIdx) {
        layoutFullFloatKeyBoard.bringToFront();                                            // 键盘置顶

        String unit = ChannelFactory.getProbeType(chIdx);                                  // 获取探头类型单位
        Logger.d(TAG, "setChFullVScale unit= " + unit + " ,nowTxt= " + nowTxt);            // 打印日志
        String txt = nowTxt.replace(unit, "").replace(" ", "");                            // 去除单位和空格
        layoutFullFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFullFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                      // 播放按钮音效
                double d = TBookUtil.getDoubleFromM(show);                                  // 解析为double
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:                                            // 双波形
                        Command.get().getMath_base().Extent(chIdx, d, true);                // 发送双波形档位命令
                        break;
                    case CacheUtil.MATHTYPE_FFT:                                           // FFT
                        Command.get().getMath_fft().Extent(chIdx, d, true);                 // 发送FFT档位命令
                        break;
                    case CacheUtil.MATHTYPE_AXB:                                           // AX+B
                        Command.get().getMath_axb().Extent(chIdx, d, true);                 // 发送AX+B档位命令
                        break;
                    case CacheUtil.MATHTYPE_AM:                                            // 高级数学
                        Command.get().getMath_advanced().Extent(chIdx, d, true);            // 发送高级数学档位命令
                        break;
                }
            }
        });
    }

    /**
     * 设置垂直档位（弹出浮点键盘，用于双波形/FFT模式）
     * @param nowTxt 当前垂直档位文本
     * @param chIdx FPGA通道索引
     */
    private void setChVScale(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();                                                // 键盘置顶

        String unit = ChannelFactory.getProbeType(chIdx);                                  // 获取探头类型单位
        Logger.d(TAG, "setChVScale unit= " + unit + " ,nowTxt= " + nowTxt);                // 打印日志
        String txt = nowTxt.replace(unit, "").replace(" ", "");                            // 去除单位和空格
        layoutFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                      // 播放按钮音效
                double d = TBookUtil.getDoubleFromM(show);                                  // 解析为double
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学类型
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:                                            // 双波形
                        Command.get().getMath_base().Extent(chIdx, d, true);                // 发送双波形档位命令
                        break;
                    case CacheUtil.MATHTYPE_FFT:                                           // FFT
                        Command.get().getMath_fft().Extent(chIdx, d, true);                 // 发送FFT档位命令
                        break;
                    case CacheUtil.MATHTYPE_AXB:                                           // AX+B
                        Command.get().getMath_axb().Extent(chIdx, d, true);                 // 发送AX+B档位命令
                        break;
                    case CacheUtil.MATHTYPE_AM:                                            // 高级数学
                        Command.get().getMath_advanced().Extent(chIdx, d, true);            // 发送高级数学档位命令
                        break;
                }
            }
        });
    }

}