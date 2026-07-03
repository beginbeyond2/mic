package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.math.MathChannel;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.menu.SliderZone;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSetChannelInfo;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     右侧通道主项布局控件                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   示波器右侧面板中的通道主项布局控件，负责显示和交互物理通道、数学通道、      │
 * │   参考通道的信息和控制项。                                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 显示通道状态：选中/未选中、展开/收起、启用/禁用                        │
 * │   2. 显示通道参数：垂直档位、耦合方式、带宽、阻抗、探头倍率、位置等        │
 * │   3. 处理用户交互：单击、双击、滑动关闭通道等手势操作                      │
 * │   4. 管理通道视觉样式：颜色、背景、指示灯等UI元素                           │
 * │   5. 支持滑动手势：左滑关闭通道、右滑打开设置面板                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   - 继承自LinearLayout，采用组合布局方式                                    │
 * │   - 使用ConstraintLayout作为根布局容器                                      │
 * │   - 通过监听器模式（OnRightMasterListener、OnRightSmallListener）处理回调   │
 * │   - 使用Handler处理延时消息和UI更新                                         │
 * │   - 通过RxBus实现跨组件通信                                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   外部设置(setXXX方法) → 内部状态更新 → UI刷新 → 用户交互 →                │
 * │   监听器回调 → RxBus消息 → 外部业务逻辑处理                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   - ChannelFactory: 获取通道实例和通道索引常量                              │
 * │   - GlobalVar: 全局配置和状态管理                                           │
 * │   - RxBus: 事件总线，发送通道操作消息                                       │
 * │   - Command: 底层硬件命令接口                                               │
 * │   - TChan: 通道类型判断工具                                                 │
 * │   - WorkModeManage: 工作模式管理                                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用场景】                                                                 │
 * │   1. 示波器主界面右侧通道列表显示                                          │
 * │   2. 支持物理通道(CH1-CH8)、数学通道(MATH1-8)、参考通道(REF1-8)三种类型     │
 * │   3. 响应用户触摸操作：单击选中、双击打开设置、滑动关闭                      │
 * │   4. 实时显示通道的电压档位、位置、带宽、耦合等参数信息                      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainRightLayoutItemChannelMaster extends LinearLayout implements SliderZone.ISliderZone {
    // 日志标签
    private static final String TAG = MainRightLayoutItemChannelMaster.class.getSimpleName(); // 获取类名作为TAG
    // 上下文引用
    private Context context; // Android上下文对象
    // 是否为普通样式（true:物理通道 false:Math/Ref通道）
    private boolean isNormalStyle = true; // 默认为普通样式
    // 通道是否被选中
    private boolean checked = false; // 初始状态未选中
    // 是否展开显示详细信息
    private boolean large = false; // 初始状态收起
    // 通道文本标识（如"1"、"M1"、"R1"）
    private String text; // 通道显示文本
    // 通道索引
    private int chIndex; // 通道索引值
    // 是否反相显示
    private boolean invert = false; // 初始不反相
    // 是否禁用状态
    private boolean disable = false; // 初始可用

    // 根布局容器
    private ConstraintLayout itemMaster; // 主容器布局
    // 通道文本显示控件
    private TextView tvChannelText; // 通道标识文本（CH1、M1、R1等）
    // 数据信息布局容器
    private ConstraintLayout dataLayout; // 包含档位、位置等信息的布局
    // 时基显示控件
    private TextView tvTimeBase; // Math/Ref时基显示
    // 触发时间显示控件
    private TextView tvTriggerTime; // Math/Ref触发时间显示
    // 探头倍率布局容器
    private LinearLayout llProbe; // 探头倍率容器
    // 探头倍率数值显示
    private TextView tvProbeTypeNum; // 探头倍率数值（如"1"、"10"）
    // 探头倍率单位显示
    private TextView tvProbeTypeUnit; // 探头倍率单位（如"X"、"mV"）
    // 耦合方式图标
    private ImageView ivCouple; // 耦合方式指示图标（AC/DC/GND）
    // 带宽显示控件
    private TextView tvBandWidth; // 带宽限制文本
    // 阻抗显示控件
    private TextView tvImpedance; // 阻抗值文本
    // 探头倍率显示
    private TextView tvProbe; // 探头倍率完整显示
    // 左侧位置显示
    private TextView tvLeftPosition; // 垂直位置值显示
    // 背景图片
    private ImageView ivBackground, ivSmallLight; // 背景图和指示灯图标
    // 小按钮
    private Button btnSmall; // 可点击的小按钮区域
    // 主项点击监听器
    private OnRightMasterListener onRightMasterListener; // 上部/下部点击回调
    // 小按钮点击监听器
    private OnRightSmallListener onRightSmallListener; // 小按钮单击/双击回调
    // 滑动关闭监听器
    private SlidCloseChannelListener mSlideCloseListener; // 滑动关闭通道回调
    // 是否被选中（用于背景切换）
    private boolean isSelect = false; // 初始未选中
    // 通道设置对话框
    private DialogSetChannelInfo dialogSetChannelInfo; // 通道参数设置弹窗

    //region interface 滑动区域接口实现
    // 可用滑动区域矩形
    private Rect availableSliderRect = null; // 滑动区域范围

    /**
     * 获取可用的滑动区域矩形
     * 用于确定手势滑动的有效区域范围
     *
     * @return 滑动区域的矩形范围
     */
    @Override
    public Rect getAvailableSliderRect() {

        //10008只有一种状态，范围左右固定 left=1800 ,right=1920
        availableSliderRect = Tools.getViewRect(this); // 获取当前视图的矩形范围
//            availableSliderRect.left = GlobalVar.get().getMainWave().x;
        availableSliderRect.right = GlobalVar.get().getScreen().right; // 设置右边界为屏幕右边界

        return availableSliderRect; // 返回滑动区域
    }
    //endregion


    /**
     * 右侧主项点击监听器接口
     * 用于处理通道上部和下部的点击事件
     */
    public interface OnRightMasterListener {
        /**
         * 上部点击回调
         *
         * @param v 被点击的视图
         */
        void onTopClick(MainRightLayoutItemChannelMaster v);

        /**
         * 下部点击回调
         *
         * @param v 被点击的视图
         */
        void onBottomClick(MainRightLayoutItemChannelMaster v);
    }

    /**
     * 右侧小按钮点击监听器接口
     * 用于处理小按钮的单击和双击事件
     */
    public interface OnRightSmallListener {
        /**
         * 小按钮单击回调
         *
         * @param v 被点击的视图
         */
        void onSmallClick(MainRightLayoutItemChannelMaster v);

        /**
         * 小按钮双击回调
         *
         * @param v 被点击的视图
         */
        void onSmallDoubleClick(MainRightLayoutItemChannelMaster v);
    }

    /**
     * 滑动关闭通道监听器接口
     * 用于处理左滑关闭通道的手势事件
     */
    public interface SlidCloseChannelListener {
        /**
         * 滑动关闭通道回调
         *
         * @param v 触发关闭的视图
         */
        void onSlidCloseChannel(MainRightLayoutItemChannelMaster v);
    }

    /**
     * 设置右侧主项点击监听器
     *
     * @param onRightMasterListener 监听器实例
     */
    public void setOnRightMasterListener(OnRightMasterListener onRightMasterListener) {
        this.onRightMasterListener = onRightMasterListener; // 保存监听器引用
    }

    /**
     * 设置右侧小按钮点击监听器
     *
     * @param onRightSmallListener 监听器实例
     */
    public void setOnRightSmallListener(OnRightSmallListener onRightSmallListener) {
        this.onRightSmallListener = onRightSmallListener; // 保存监听器引用
    }

    /**
     * 设置滑动关闭通道监听器
     *
     * @param mSlideCloseListener 监听器实例
     */
    public void setOnSlidCloseChannelListener(SlidCloseChannelListener mSlideCloseListener) {
        this.mSlideCloseListener = mSlideCloseListener; // 保存监听器引用
    }

    /**
     * 单参数构造方法
     *
     * @param context Android上下文
     */
    public MainRightLayoutItemChannelMaster(Context context) {
        this(context, null); // 调用双参数构造方法
    }

    /**
     * 双参数构造方法
     *
     * @param context Android上下文
     * @param attrs   XML属性集
     */
    public MainRightLayoutItemChannelMaster(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * 三参数构造方法（完整构造方法）
     *
     * @param context      Android上下文
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainRightLayoutItemChannelMaster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        init(context, attrs, defStyleAttr); // 初始化控件
    }

    // 探头倍率列表
    private String[] probeList; // 探头倍率选项数组

    /**
     * 初始化方法
     * 读取自定义属性、加载布局、初始化视图
     *
     * @param context      Android上下文
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context; // 保存上下文引用
        text = "1"; // 默认通道文本为"1"
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemChannelMaster); // 获取自定义属性数组
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_checked, false); // 读取checked属性
        large = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_large, false); // 读取large属性
        text = ta.getString(R.styleable.MainRightLayoutItemChannelMaster_text); // 读取text属性
        isNormalStyle = ta.getBoolean(R.styleable.MainRightLayoutItemChannelMaster_isNormal, true); // 读取isNormal属性
        ta.recycle(); // 回收TypedArray
        Logger.i("text= " + text); // 打印日志
        if (!isNormalStyle) {//底部Math/Ref对应的布局
//            if (text.contains("R")) {
            inflate(context, R.layout.layout_item_mainright_ref, this); // 加载Math/Ref布局
//            } else {
//                inflate(context, R.layout.layout_item_mainright_math_and_ref, this);
//            }
        } else {
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {//右侧CH 8通道对应布局
                inflate(context, R.layout.layout_item_mainright_channel, this); // 加载8通道布局
            } else {
                inflate(context, R.layout.layout_mainright_channel_master, this); // 加载主通道布局
            }
        }
        probeList = getResources().getStringArray(R.array.channelProbeTypeMultiple); // 获取探头倍率数组
        initView(); // 初始化视图组件
        setState(checked, large); // 设置初始状态
    }

    /**
     * 初始化视图组件
     * 绑定布局中的各个控件到成员变量
     */
    private void initView() {
        itemMaster = findViewById(R.id.item_channel_master); // 获取根布局容器
        tvChannelText = (TextView) findViewById(R.id.channelText); // 获取通道文本控件
        dataLayout = (ConstraintLayout) findViewById(R.id.dataLayout); // 获取数据布局容器
        tvTimeBase = findViewById(R.id.refTimebase); // 获取时基显示控件
        tvTriggerTime = findViewById(R.id.refTriggerTime); // 获取触发时间控件
        llProbe = findViewById(R.id.ll_probe); // 获取探头倍率布局
        tvProbeTypeNum = (TextView) findViewById(R.id.probeTypeNum); // 获取探头倍率数值
        tvProbeTypeUnit = (TextView) findViewById(R.id.probeTypeUnit); // 获取探头倍率单位
        ivCouple = (ImageView) findViewById(R.id.rightCouple); // 获取耦合方式图标
        tvBandWidth = (TextView) findViewById(R.id.rightBandWidth); // 获取带宽控件
        tvImpedance = findViewById(R.id.impedance); // 获取阻抗控件
        tvProbe = findViewById(R.id.prob); // 获取探头倍率控件
        tvLeftPosition = findViewById(R.id.leftPosition); // 获取位置控件
        ivBackground = findViewById(R.id.background); // 获取背景图片控件
        ivSmallLight = (ImageView) findViewById(R.id.smallLight); // 获取指示灯控件
        btnSmall = (Button) findViewById(R.id.btnSmall); // 获取小按钮控件

        btnSmall.setOnTouchListener(onTouchListener); // 设置触摸监听器

        dialogSetChannelInfo = (DialogSetChannelInfo) ((MainActivity) context).findViewById(R.id.dialogSetChannelInfo); // 获取对话框引用
        tvChannelText.setText(text); // 设置通道文本
        chIndex = getChIndex(); // 获取通道索引
        setContentColor(chIndex); // 设置内容颜色
    }

    /**
     * 设置内容颜色
     * 根据通道类型设置对应的颜色和可见性
     *
     * @param chIndex 通道索引
     */
    public void setContentColor(int chIndex) {

        int[] chColors = SvgNodeInfo.getColorsIntForView(); // 获取通道颜色数组
        int[] drawableResources = { // 通道指示灯背景资源数组
                R.drawable.ic_bg_ch1_light, R.drawable.ic_bg_ch2_light,
                R.drawable.ic_bg_ch3_light, R.drawable.ic_bg_ch4_light,
                R.drawable.ic_bg_ch5_light, R.drawable.ic_bg_ch6_light,
                R.drawable.ic_bg_ch7_light, R.drawable.ic_bg_ch8_light
        };

        switch (chIndex) { // 根据通道索引设置样式
            case ChannelFactory.CH1: // 通道1
            case ChannelFactory.CH2: // 通道2
            case ChannelFactory.CH3: // 通道3
            case ChannelFactory.CH4: // 通道4
            case ChannelFactory.CH5: // 通道5
            case ChannelFactory.CH6: // 通道6
            case ChannelFactory.CH7: // 通道7
            case ChannelFactory.CH8: // 通道8
                ivSmallLight.setImageResource(drawableResources[chIndex]); // 设置指示灯背景
                tvChannelText.setTextColor(chColors[chIndex]); // 设置通道文本颜色
                tvProbeTypeNum.setTextColor(chColors[chIndex]); // 设置探头数值颜色
                tvProbeTypeUnit.setTextColor(chColors[chIndex]); // 设置探头单位颜色
                tvTimeBase.setTextColor(chColors[chIndex]); // 设置时基颜色
                tvLeftPosition.setTextColor(chColors[chIndex]); // 设置位置颜色
                tvTimeBase.setVisibility(View.GONE); // 隐藏时基
                tvTriggerTime.setVisibility(View.GONE); // 隐藏触发时间
                tvLeftPosition.setVisibility(View.VISIBLE); // 显示位置
                setOtherTextVisible(true); // 显示其他文本
                break;
            case ChannelFactory.MATH1: // 数学通道1
            case ChannelFactory.MATH2: // 数学通道2
            case ChannelFactory.MATH3: // 数学通道3
            case ChannelFactory.MATH4: // 数学通道4
            case ChannelFactory.MATH5: // 数学通道5
            case ChannelFactory.MATH6: // 数学通道6
            case ChannelFactory.MATH7: // 数学通道7
            case ChannelFactory.MATH8: // 数学通道8
                ivSmallLight.setImageResource(R.drawable.ic_bg_math_light); // 设置MATH指示灯背景
                tvChannelText.setVisibility(VISIBLE); // 显示通道文本
                tvChannelText.setTextColor(chColors[chIndex]); // 设置通道文本颜色
                tvProbeTypeNum.setTextColor(chColors[chIndex]); // 设置探头数值颜色
                tvProbeTypeUnit.setTextColor(chColors[chIndex]); // 设置探头单位颜色
                tvTimeBase.setTextColor(chColors[chIndex]); // 设置时基颜色
                tvTriggerTime.setTextColor(chColors[chIndex]); // 设置触发时间颜色
                tvLeftPosition.setTextColor(chColors[chIndex]); // 设置位置颜色

                tvTimeBase.setVisibility(View.VISIBLE); // 显示时基
                tvTriggerTime.setVisibility(View.VISIBLE); // 显示触发时间
                tvLeftPosition.setVisibility(View.VISIBLE); // 显示位置
                setOtherTextVisible(false); // 隐藏其他文本
                break;
            case ChannelFactory.REF1: // 参考通道1
            case ChannelFactory.REF2: // 参考通道2
            case ChannelFactory.REF3: // 参考通道3
            case ChannelFactory.REF4: // 参考通道4
            case ChannelFactory.REF5: // 参考通道5
            case ChannelFactory.REF6: // 参考通道6
            case ChannelFactory.REF7: // 参考通道7
            case ChannelFactory.REF8: // 参考通道8
                ivSmallLight.setImageResource(R.drawable.ic_bg_ref_light); // 设置REF指示灯背景
                tvChannelText.setVisibility(VISIBLE); // 显示通道文本
                tvChannelText.setTextColor(chColors[chIndex]); // 设置通道文本颜色
                tvProbeTypeNum.setTextColor(chColors[chIndex]); // 设置探头数值颜色
                tvProbeTypeUnit.setTextColor(chColors[chIndex]); // 设置探头单位颜色
                tvTimeBase.setTextColor(chColors[chIndex]); // 设置时基颜色
                tvTriggerTime.setTextColor(chColors[chIndex]); // 设置触发时间颜色
                tvLeftPosition.setTextColor(chColors[chIndex]); // 设置位置颜色

                tvTimeBase.setVisibility(View.VISIBLE); // 显示时基
                tvTriggerTime.setVisibility(View.VISIBLE); // 显示触发时间
                tvLeftPosition.setVisibility(View.VISIBLE); // 显示位置
                setOtherTextVisible(false); // 隐藏其他文本
                break;
            default: // 默认情况
                tvChannelText.setTextColor(chColors[chIndex]); // 设置通道文本颜色
                tvChannelText.setVisibility(VISIBLE); // 显示通道文本
                ivSmallLight.setImageResource(R.drawable.ic_bg_math_light); // 设置默认指示灯背景
                tvLeftPosition.setVisibility(View.GONE); // 隐藏位置
                setOtherTextVisible(false); // 隐藏其他文本
                break;
        }

        if (ChannelFactory.isRefCh(chIndex)) { //改变颜色之后的处理
            RefChannel refChannel = ChannelFactory.getRefChannel(chIndex); // 获取参考通道实例
            if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) { // 如果是FFT类型
                tvTriggerTime.setVisibility(View.GONE); // 隐藏触发时间
            }
        }

        if (ChannelFactory.isMath_FFT_Ch(chIndex)) { //改变颜色之后的处理
            MathChannel mathChannel = ChannelFactory.getMathChannel(chIndex); // 获取数学通道实例
            if (mathChannel != null && mathChannel.getMathType() == MathWave.MATH_FFTWAVE) { // 如果是FFT类型
                tvTriggerTime.setVisibility(View.GONE); // 隐藏触发时间
            }
        }

    }


    /**
     * 设置其他文本的可见性
     * 控制带宽、阻抗、探头、耦合等控件的显示/隐藏
     *
     * @param b true显示，false隐藏
     */
    private void setOtherTextVisible(boolean b) {
        tvBandWidth.setVisibility(b ? VISIBLE : GONE); // 设置带宽可见性
        tvImpedance.setVisibility(b ? VISIBLE : GONE); // 设置阻抗可见性
        tvProbe.setVisibility(b ? VISIBLE : GONE); // 设置探头可见性
//        tvLeftPosition.setVisibility(b ? VISIBLE : GONE);
        ivCouple.setVisibility(b ? VISIBLE : GONE); // 设置耦合图标可见性
        if (!b) {
            ivCouple.setImageResource(0); // 清空耦合图标
        }
    }

    /**
     * 设置布局右对齐到边齐。
     * 解决：tvProbeTypeUnit在xml布局中，右边控件ivCouple动态隐藏后，不能对齐的问题。
     *
     * @param parent 父类布局
     * @param view   要操作的控件
     */
    private void setRightToRight(ConstraintLayout parent, View view) {
        ConstraintSet set = new ConstraintSet(); // 创建约束集
        set.clone(parent); // 克隆父布局约束
        set.connect(view.getId(), ConstraintSet.RIGHT, 0, ConstraintSet.LEFT, 0); // 连接右边到左边
        set.connect(view.getId(), ConstraintSet.RIGHT, parent.getId(), ConstraintSet.RIGHT, 0); // 连接右边到父布局右边
        set.applyTo(parent); // 应用约束集
    }

    /**
     * 设置左右居中对齐
     * 将两个控件水平居中排列
     *
     * @param parent    父布局
     * @param leftView  左侧控件
     * @param rightView 右侧控件
     */
    private void setLeftRightCenter(ConstraintLayout parent, View leftView, View rightView) {
        ConstraintSet set = new ConstraintSet(); // 创建约束集
        set.clone(parent); // 克隆父布局约束
        set.connect(leftView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0); // 连接左侧控件左边到父布局左边
        set.connect(leftView.getId(), ConstraintSet.RIGHT, rightView.getId(), ConstraintSet.LEFT, 0); // 连接左侧控件右边到右侧控件左边
        set.setHorizontalChainStyle(leftView.getId(), ConstraintSet.CHAIN_PACKED); // 设置链样式为紧凑

        set.connect(rightView.getId(), ConstraintSet.LEFT, leftView.getId(), ConstraintSet.RIGHT, 0); // 连接右侧控件左边到左侧控件右边
        set.connect(rightView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0); // 连接右侧控件右边到父布局右边

        set.applyTo(parent); // 应用约束集
    }


    /**
     * 设置上边距
     *
     * @param parent 父布局
     * @param view   要设置的控件
     */
    private void setMarginTop(ConstraintLayout parent, View view) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams(); // 获取布局参数
        lp.setMargins(0, 50, 0, 0); // 设置上边距为50
        view.setLayoutParams(lp); // 应用布局参数
    }

    /**
     * 获取通道索引
     * 根据通道文本标识转换为通道索引常量
     *
     * @return 通道索引值
     */
    public int getChIndex() {
        int chIndex; // 定义通道索引变量
        switch (tvChannelText.getText().toString()) { // 根据文本判断通道类型
            case "1": chIndex = ChannelFactory.CH1; break; // 物理通道1
            case "2": chIndex = ChannelFactory.CH2; break; // 物理通道2
            case "3": chIndex = ChannelFactory.CH3; break; // 物理通道3
            case "4": chIndex = ChannelFactory.CH4; break; // 物理通道4
            case "5": chIndex = ChannelFactory.CH5; break; // 物理通道5
            case "6": chIndex = ChannelFactory.CH6; break; // 物理通道6
            case "7": chIndex = ChannelFactory.CH7; break; // 物理通道7
            case "8": chIndex = ChannelFactory.CH8; break; // 物理通道8
            case "M1": chIndex = ChannelFactory.MATH1; break; // 数学通道1
            case "M2": chIndex = ChannelFactory.MATH2; break; // 数学通道2
            case "M3": chIndex = ChannelFactory.MATH3; break; // 数学通道3
            case "M4": chIndex = ChannelFactory.MATH4; break; // 数学通道4
            case "M5": chIndex = ChannelFactory.MATH5; break; // 数学通道5
            case "M6": chIndex = ChannelFactory.MATH6; break; // 数学通道6
            case "M7": chIndex = ChannelFactory.MATH7; break; // 数学通道7
            case "M8": chIndex = ChannelFactory.MATH8; break; // 数学通道8
            case "R1": chIndex = ChannelFactory.REF1; break; // 参考通道1
            case "R2": chIndex = ChannelFactory.REF2; break; // 参考通道2
            case "R3": chIndex = ChannelFactory.REF3; break; // 参考通道3
            case "R4": chIndex = ChannelFactory.REF4; break; // 参考通道4
            case "R5": chIndex = ChannelFactory.REF5; break; // 参考通道5
            case "R6": chIndex = ChannelFactory.REF6; break; // 参考通道6
            case "R7": chIndex = ChannelFactory.REF7; break; // 参考通道7
            case "R8": chIndex = ChannelFactory.REF8; break; // 参考通道8
            default: chIndex = ChannelFactory.CH1; break; // 默认为通道1
        }
        return chIndex; // 返回通道索引
    }

    /**
     * 设置背景资源
     * 根据选中状态和通道类型设置指示灯的可见性
     */
    private void setBgResId() {
//        ivLight.setVisibility(INVISIBLE);
        ivSmallLight.setVisibility(INVISIBLE); // 先隐藏指示灯
//        ivLightInvert.setVisibility(INVISIBLE);
        setShowTextVisible(checked, large); // 设置文本可见性
        if (checked) { // 如果通道被选中
            switch (chIndex) { // 根据通道索引处理
                case ChannelFactory.CH1: // 通道1
                case ChannelFactory.CH2: // 通道2
                case ChannelFactory.CH3: // 通道3
                case ChannelFactory.CH4: // 通道4
                case ChannelFactory.CH5: // 通道5
                case ChannelFactory.CH6: // 通道6
                case ChannelFactory.CH7: // 通道7
                case ChannelFactory.CH8: // 通道8
                    if (invert) { // 如果反相
                        ivSmallLight.setVisibility(VISIBLE); // 显示指示灯
                    } else {
                        ivSmallLight.setVisibility(INVISIBLE); // 隐藏指示灯
                    }
                    break;
                case ChannelFactory.MATH1: // 数学通道1
                case ChannelFactory.MATH2: // 数学通道2
                case ChannelFactory.MATH3: // 数学通道3
                case ChannelFactory.MATH4: // 数学通道4
                case ChannelFactory.MATH5: // 数学通道5
                case ChannelFactory.MATH6: // 数学通道6
                case ChannelFactory.MATH7: // 数学通道7
                case ChannelFactory.MATH8: // 数学通道8
                case ChannelFactory.REF1: // 参考通道1
                case ChannelFactory.REF2: // 参考通道2
                case ChannelFactory.REF3: // 参考通道3
                case ChannelFactory.REF4: // 参考通道4
                case ChannelFactory.REF5: // 参考通道5
                case ChannelFactory.REF6: // 参考通道6
                case ChannelFactory.REF7: // 参考通道7
                case ChannelFactory.REF8: // 参考通道8
                default: // 默认情况
                    ivSmallLight.setVisibility(INVISIBLE); // 隐藏指示灯
                    break;
            }
        }
    }

    /**
     * 设置显示文本的可见性
     * 根据通道状态控制各控件的显示
     *
     * @param checked 是否选中
     * @param large   是否展开
     */
    private void setShowTextVisible(boolean checked, boolean large) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        if (checked) { // 如果选中
            if (chIndex == ChannelFactory.MATH1 || chIndex == ChannelFactory.MATH2 // 判断是否为MATH通道
                    || chIndex == ChannelFactory.MATH3 || chIndex == ChannelFactory.MATH4
                    || chIndex == ChannelFactory.MATH5 || chIndex == ChannelFactory.MATH6
                    || chIndex == ChannelFactory.MATH7 || chIndex == ChannelFactory.MATH8
                    || chIndex == ChannelFactory.REF1 || chIndex == ChannelFactory.REF2 // 判断是否为REF通道
                    || chIndex == ChannelFactory.REF3 || chIndex == ChannelFactory.REF4
                    || chIndex == ChannelFactory.REF5 || chIndex == ChannelFactory.REF6
                    || chIndex == ChannelFactory.REF7 || chIndex == ChannelFactory.REF8
            ) {
                tvChannelText.setVisibility(VISIBLE); // 显示通道文本
            } else {
                tvChannelText.setVisibility(GONE); // 隐藏通道文本
            }
            dataLayout.setVisibility(VISIBLE); // 显示数据布局
        } else {
            //通道 关闭的显示位置
            tvChannelText.setVisibility(VISIBLE); // 显示通道文本
            dataLayout.setVisibility(GONE); // 隐藏数据布局
        }

        if (!isNormalStyle) { // 如果不是普通样式（Math/Ref）
            if (checked) { // 如果选中
                tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18); // 设置文本大小为18
            } else {
                tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26); // 设置文本大小为26
            }
        } else {
            tvChannelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26); // 设置文本大小为26
        }

//        Rect probeTypeRect = Tools.getTextRect(tvProbeTypeNum.getText().toString() + tvProbeTypeUnit.getText().toString(), tvProbeTypeNum.getPaint());
//        int rightMargin = (layout.getMeasuredWidth() - probeTypeRect.width()) / 2;
        // 显示一堆的参数
//        if (checked && large) {
            // 选中且展开形式
//            if (chIndex == MATH || chIndex == REF) {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 85, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            } else {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 90, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            }
//        } else if (checked) {
//            if (chIndex == MATH || chIndex == REF) {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 85, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            } else {
//                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//                layoutParams.setMargins(0, 90, 0, 0);
//                dataLayout.setLayoutParams(layoutParams);
//            }
//        } else {
//            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dataLayout.getLayoutParams();
//            layoutParams.setMargins(0, 90, 0, 0);
//            dataLayout.setLayoutParams(layoutParams);
//        }
    }

    /**
     * 判断是否禁用
     *
     * @return 禁用状态
     */
    public boolean isDisable() {
        return disable; // 返回禁用状态
    }

    /**
     * 设置禁用状态
     *
     * @param disable 是否禁用
     */
    public void setDisable(boolean disable) {
        this.disable = disable; // 设置禁用状态
    }


    /**
     * 设置通道状态
     *
     * @param checked 是否选中
     * @param large   是否展开
     */
    public void setState(boolean checked, boolean large) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        this.checked = checked; // 设置选中状态
        this.large = large; // 设置展开状态
        btnSmall.setVisibility(VISIBLE); // 显示小按钮
        btnSmall.setClickable(true); // 设置按钮可点击
        setBgResId(); // 设置背景资源
//        updateBackground(large);
    }

    /**
     * 更新背景显示
     * 根据选中状态设置不同的背景资源
     *
     * @param isSelect 是否选中
     */
    public void updateBackground(boolean isSelect) {
        this.isSelect = isSelect; // 设置选中状态
//        new RuntimeException("limh item" + getChIndex() + " isSelect=" + isSelect).printStackTrace();
        ivBackground.setVisibility(View.VISIBLE); // 显示背景图片
        if (isSelect) { // 如果选中
            if (TChan.isChan(TChan.toUiChNo(chIndex))) { // 如果是物理通道
                ivBackground.setImageResource(R.drawable.svg_right_ch1234_select); // 设置选中背景
            } else if (TChan.isRef(TChan.toUiChNo(chIndex)) || TChan.isMath(TChan.toUiChNo(chIndex))) { // 如果是Ref或Math通道
                ivBackground.setVisibility(View.GONE); // 隐藏背景图片
                itemMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_select)); // 设置容器背景
            } else {
                ivBackground.setImageResource(R.drawable.layer_button_select); // 设置选中背景
            }
        } else {
            if (TChan.isChan(TChan.toUiChNo(chIndex))) { // 如果是物理通道
                ivBackground.setImageResource(R.drawable.svg_right_ch1234_close); // 设置关闭背景
            }  else if (TChan.isRef(TChan.toUiChNo(chIndex)) || TChan.isMath(TChan.toUiChNo(chIndex))) { // 如果是Ref或Math通道
                ivBackground.setVisibility(View.GONE); // 隐藏背景图片
                itemMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_disable)); // 设置禁用背景
            } else {
                ivBackground.setImageResource(R.drawable.layer_button_disable); // 设置禁用背景
            }
        }
    }

    /**
     * 判断是否被选中
     *
     * @return 选中状态
     */
    public boolean isSelect() {
        return isSelect; // 返回选中状态
    }

    /**
     * 判断是否选中
     *
     * @return 选中状态
     */
    public boolean isChecked() {
        return checked; // 返回选中状态
    }

    /**
     * 设置选中状态
     *
     * @param checked 是否选中
     */
    public void setChecked(boolean checked) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        setState(checked, true); // 设置状态为选中并展开
    }

    /**
     * 判断是否展开
     *
     * @return 展开状态
     */
    public boolean isLarge() {
        return large; // 返回展开状态
    }

    /**
     * 设置展开状态
     *
     * @param large 是否展开
     */
    public void setLarge(boolean large) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        setState(checked, large); // 设置展开状态
    }

    /**
     * 设置垂直档位值
     *
     * @param vScale 垂直档位值
     */
    public void setVScale(double vScale) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        Channel channel = ChannelFactory.getDynamicChannel(Integer.parseInt(text.replace("CH", "")) - 1); // 获取通道实例
        channel.setVScaleVal(vScale); // 设置垂直档位值
        String probeEvery = TBookUtil.getMFromDouble(channel.getVScaleVal()); // 格式化档位值
        setProbeTypeNum(probeEvery); // 显示档位值
    }

    /**
     * 设置探头倍率数值
     *
     * @param probeTypeNum 探头倍率数值文本
     */
    public void setProbeTypeNum(String probeTypeNum) {
        Logger.d("index= " + getChIndex() + " TypeNum= " + probeTypeNum); // 打印日志
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        if (StrUtil.isEmpty(probeTypeNum)) { // 如果为空
            tvProbeTypeNum.setVisibility(GONE); // 隐藏数值控件
            tvProbeTypeUnit.setVisibility(GONE); // 隐藏单位控件
        } else {
            tvProbeTypeNum.setVisibility(VISIBLE); // 显示数值控件
            tvProbeTypeUnit.setVisibility(VISIBLE); // 显示单位控件
        }
        tvProbeTypeNum.setText(probeTypeNum); // 设置数值文本
        changeProbeTypeTextSize(); // 调整文本大小
    }

    /**
     * 设置探头倍率单位
     *
     * @param probeTypeUnit 探头倍率单位文本
     */
    public void setProbeTypeUnit(String probeTypeUnit) {
        Logger.d("index= " + getChIndex() + " TypeUnit= " + probeTypeUnit); // 打印日志
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        tvProbeTypeUnit.setText(probeTypeUnit); // 设置单位文本
        changeProbeTypeTextSize(); // 调整文本大小
    }

    /**
     * 设置Math/Ref触发位置
     *
     * @param triggerTime 触发时间文本
     */
    //Math/Ref触发位置
    public void setTriggerTime(String triggerTime) {
        Logger.d(TAG, "index= " + getChIndex() + " triggerTime= " + triggerTime); // 打印日志
        if (disable) return; // 如果禁用直接返回
        if (StrUtil.isEmpty(triggerTime)) { // 如果为空
            tvTriggerTime.setVisibility(View.GONE); // 隐藏触发时间控件
            return; // 返回
        }
        tvTriggerTime.setText(triggerTime); // 设置触发时间文本
        tvTriggerTime.setVisibility(View.VISIBLE); // 显示触发时间控件
    }

    /**
     * 设置Math/Ref显示时基
     *
     * @param timeBase 时基文本
     */
    //Math/Ref显示时基
    public void setTimeBase(String timeBase) {
        Logger.d(TAG, "index= " + getChIndex() + " TimeBase= " + timeBase); // 打印日志
        if (disable) return; // 如果禁用直接返回
        if (StrUtil.isEmpty(timeBase)) { // 如果为空
            tvTimeBase.setVisibility(View.GONE); // 隐藏时基控件
            return; // 返回
        }
        tvTimeBase.setText(timeBase); // 设置时基文本
        tvTimeBase.setVisibility(View.VISIBLE); // 显示时基控件
    }

    /**
     * 获取探头倍率完整文本
     *
     * @return 探头倍率文本（数值+单位）
     */
    public String getProbeType() {
        return tvProbeTypeNum.getText().toString() + tvProbeTypeUnit.getText().toString(); // 拼接返回完整文本
    }

    /**
     * 调整探头倍率文本大小
     * 根据文本宽度自动调整字体大小
     */
    private void changeProbeTypeTextSize() {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        String probeType = getProbeType(); // 获取完整探头倍率文本
        tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置默认字体大小16
        tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置默认字体大小16
        Rect typeNumRect = Tools.getTextRect(probeType, tvProbeTypeNum.getPaint()); // 获取文本矩形范围
        Rect typeUnitRect = Tools.getTextRect(probeType, tvProbeTypeUnit.getPaint()); // 获取文本矩形范围
        if (!isNormalStyle) { // 如果不是普通样式（Math/Ref）
            if (typeNumRect.width() + typeUnitRect.width() > 120) { // 如果文本宽度大于120
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14); // 设置字体大小为14
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14); // 设置字体大小为14
                tvTimeBase.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14); // 设置字体大小为14
            } else {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置字体大小为16
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置字体大小为16
                tvTimeBase.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置字体大小为16
            }
        } else {
            if (typeNumRect.width() > 100) { // 如果文本宽度大于100
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置字体大小为16
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16); // 设置字体大小为16
            } else {
                tvProbeTypeNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24); // 设置字体大小为24
                tvProbeTypeUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24); // 设置字体大小为24
            }
        }
        setShowTextVisible(checked, large); // 更新文本可见性
    }

    /**
     * 设置耦合方式图标资源
     *
     * @param resId 图标资源ID
     */
    public void setCoupleResId(int resId) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        ivCouple.setImageResource(resId); // 设置耦合方式图标
    }

    /**
     * 设置带宽文本
     *
     * @param bandWidth 带宽文本
     */
    public void setBandWidth(String bandWidth) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        tvBandWidth.setText(bandWidth); // 设置带宽文本
    }

    /**
     * 设置阻抗文本
     *
     * @param impedance 阻抗文本
     */
    public void setTvImpedance(String impedance) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        tvImpedance.setText(impedance); // 设置阻抗文本
    }

    /**
     * 设置探头倍率
     *
     * @param probeMultiple 探头倍率文本
     */
    public void setProbeMultiple(String probeMultiple) {
//        if (probeMultiple.contains("500mX")) {
//            probeMultiple = probeMultiple.replace("500mX", "0.5X");
//        } else if (probeMultiple.contains("200mX")) {
//            probeMultiple = probeMultiple.replace("200mX", "0.2X");
//        } else if (probeMultiple.contains("100mX")) {
//            probeMultiple = probeMultiple.replace("100mX", "0.1X");
//        }
//        Log.d(Command.TAG, String.format("setProbeMultiple: %s ,list:%s",probeMultiple , Arrays.toString(probeList)));
        int index= Tools.indexOf(probeList,s->s.equals(probeMultiple)); // 查找探头倍率在列表中的索引
        if (index>0){ // 如果是标准的探头倍率
            //如果是标准的，就选择标准的
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.toUiChNo(chIndex), ""); // 清空自定义探头倍率缓存
        }
        tvProbe.setText(probeMultiple); // 设置探头倍率文本
    }

    /**
     * 设置左侧位置文本
     *
     * @param leftPosition 位置文本
     */
    public void setLeftPosition(String leftPosition) {
        Logger.d("channelIndex= " + getChIndex() + " leftPosition= " + leftPosition); // 打印日志
        tvLeftPosition.setText(leftPosition); // 设置位置文本
        RxBus.getInstance().post(RxEnum.MSG_CHANNEL_SLIP_POSITION,leftPosition +";" + chIndex); // 发送位置变更消息
    }

    /**
     * 设置左侧位置控件可见性
     *
     * @param visible 是否可见
     */
    public void setLeftPositionVisible(boolean visible) {
        tvLeftPosition.setVisibility(visible ? VISIBLE : INVISIBLE); // 设置可见性
    }

    /**
     * 设置反相状态
     *
     * @param invert 是否反相
     */
    public void setInvert(boolean invert) {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        this.invert = invert; // 设置反相状态
        setBgResId(); // 更新背景资源
    }

    /**
     * 处理上部按钮点击事件
     */
    public void onBtnTopClick() {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        if (handler.hasMessages(HANDLE_MSG)) { // 如果Handler中有消息
            handler.removeMessages(HANDLE_MSG); // 移除消息
        }
        handler.sendEmptyMessageDelayed(HANDLE_MSG, 500);//改背景 延迟500ms发送消息
        if (onRightMasterListener != null) { // 如果监听器不为空
            onRightMasterListener.onTopClick(MainRightLayoutItemChannelMaster.this); // 回调上部点击事件
        }
    }

    /**
     * 处理下部按钮点击事件
     */
    public void onBtnBottomClick() {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        if (handler.hasMessages(HANDLE_MSG)) { // 如果Handler中有消息
            handler.removeMessages(HANDLE_MSG); // 移除消息
        }
        handler.sendEmptyMessageDelayed(HANDLE_MSG, 500); // 延迟500ms发送消息
        if (onRightMasterListener != null) { // 如果监听器不为空
            onRightMasterListener.onBottomClick(MainRightLayoutItemChannelMaster.this); // 回调下部点击事件
        }
    }

    /**
     * 处理小按钮单击事件
     */
    public void onSmallClick() {
        if (disable) { // 如果禁用
            return; // 直接返回
        }
        if (onRightSmallListener != null) { // 如果监听器不为空
            onRightSmallListener.onSmallClick(MainRightLayoutItemChannelMaster.this); // 回调单击事件
        }
    }

    /**
     * 触摸事件监听器
     * 处理单击、双击、滑动关闭等手势
     */
    private OnTouchListener onTouchListener = new OnTouchListener() {
        private View curView; // 当前触摸的视图
        private int oldX, oldY; // 触摸起始坐标
        private int clickCount = 0; // 点击计数

        /**
         * 触摸事件处理
         *
         * @param view        触摸的视图
         * @param motionEvent 触摸事件
         * @return 是否消费事件
         */
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean isFromMouse = motionEvent.isFromSource(InputDevice.SOURCE_MOUSE);//判断是否来自鼠标点击
//            Log.d(Tag.Debug, String.format("MainRightLayoutItemChannelMaster.onTouch: %b",disable ));
            if (disable) return false; // 如果禁用直接返回
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) { // 处理触摸事件
                case MotionEvent.ACTION_DOWN: // 按下事件
                    this.oldX = (int) motionEvent.getX(); // 记录起始X坐标
                    this.oldY = (int) motionEvent.getY(); // 记录起始Y坐标
                    this.curView = view; // 记录当前视图
                    break;
                case MotionEvent.ACTION_UP: // 抬起事件
                    float offsetX = (float) (motionEvent.getX() - oldX); // 计算X偏移量
                    float offsetY = (float) (motionEvent.getY() - oldY); // 计算Y偏移量
                    int i = getChIndex(); // 获取通道索引
                    if (TChan.isChan(TChan.toUiChNo(i))) { // 如果是物理通道
                        offsetY = Math.abs(offsetY); // 取Y偏移量绝对值
                        if (offsetX > 10 && (offsetY <= 5 || offsetX / offsetY >= 1.0f)) { // 判断是否为左滑手势
                            CloseChannel(view); // 关闭通道
                            break; // 跳出
                        }
                    } else { // 如果是Math/Ref通道
                        if (offsetY > 10 && (offsetX <= 5 || offsetY / offsetX >= 1.0f)) { // 判断是否为下滑手势
//                            CloseChannel(view);//关闭通道
                            RxBus.getInstance().post(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL, TChan.toUiChNo(i));//删除通道 发送删除通道消息
                            break; // 跳出
                        }
                    }
                    clickCount++; // 增加点击计数
                    int timeout = 300; // 双击判定超时时间300ms
                    Rect rect = Tools.getViewRect(view); // 获取视图矩形范围
                    if (curView != null && curView == view && (Math.abs(offsetX) < 10 && Math.abs(offsetY) < 10) /*rect.contains((int)motionEvent.getRawX(), (int) motionEvent.getRawY())*/) { // 判断是否为点击（非滑动）
                        OnMouseUp(view); // 处理鼠标抬起事件
                        handler.postDelayed(() -> { // 延迟处理点击计数
                            if (clickCount == 1) { // 单击
                            } else if (clickCount > 1) { // 双击或多击
                                Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY()); // 创建点击坐标点
                                onDoubleMouseUp(view, isFromMouse, point); // 处理双击事件
                            }
                            handler.removeCallbacksAndMessages(null);//清空handler延时 清空所有消息
                            clickCount = 0;//计数清零 点击计数清零
                        }, timeout);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL: // 取消事件
                    break;
            }

            return false; // 返回false表示不消费事件
        }

        /**
         * 关闭通道
         *
         * @param view 触发的视图
         */
        private void CloseChannel(View view) {
            //Log.d(Tag.Debug, String.format("MainRightLayoutItemChannelMaster.CloseChannel: %d",getChIndex()));
            if (disable) { // 如果禁用
                return; // 直接返回
            }
            PlaySound.getInstance().playSlide(); // 播放滑动音效
            if (mSlideCloseListener != null) { // 如果监听器不为空
                mSlideCloseListener.onSlidCloseChannel(MainRightLayoutItemChannelMaster.this); // 回调滑动关闭事件
            }
            int i = getChIndex(); // 获取通道索引
            Command.get().getChannel().Display(i, false, true); // 发送关闭通道命令
        }

//        private void OnMouseDown(View v) {
//            if (v.getId() == btnTop.getId()) {
//                if (invert) {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_up);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_up);
//                }
//            } else if (v.getId() == btnBottom.getId()) {
//                if (invert) {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_down);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_channel_down);
//                }
//            } else if (v.getId() == btnSmall.getId()) {
//
//            }
//
//        }

//        private void OnMouseCancel(View v){
//            if (v.getId()==btnTop.getId() || v.getId()==btnBottom.getId()) {
//                if (large) {
//                    if (invert) {
//                        ivBackground.setImageResource(R.drawable.svg_right_channel);
//                    } else {
//                        ivBackground.setImageResource(R.drawable.svg_right_channel);
//                    }
//                    ivSmallLight.setVisibility(INVISIBLE);
//                } else {
//                    ivBackground.setImageResource(R.drawable.svg_right_ch1234_close);
//                    if (invert) {
//                        ivSmallLight.setVisibility(VISIBLE);
//                    } else {
//                        ivSmallLight.setVisibility(INVISIBLE);
//                    }
//                }
//            }
//        }

        /**
         * 处理鼠标抬起事件（单击）
         *
         * @param v 触发的视图
         */
        private void OnMouseUp(View v) {
//            DToast.get().show("单击");
            if (disable) { // 如果禁用
                return; // 直接返回
            }
            if (v.getId() == btnSmall.getId()) { // 如果是小按钮
                if (onRightSmallListener != null) { // 如果监听器不为空
                    onRightSmallListener.onSmallClick(MainRightLayoutItemChannelMaster.this); // 回调单击事件
                }
            }
        }

        /**
         * 处理鼠标双击事件
         *
         * @param v            触发的视图
         * @param isFromMouse   是否来自鼠标
         * @param point         点击坐标点
         */
        private void onDoubleMouseUp(View v, boolean isFromMouse, Point point) {
//            DToast.get().show("双击");
            if (disable) { // 如果禁用
                return; // 直接返回
            }
            if (v.getId() == btnSmall.getId()) { // 如果是小按钮
                if (onRightSmallListener == null) return; // 监听器为空直接返回
                if (isFromMouse) { // 如果来自鼠标
                    checkClickPosition(point); // 检查点击位置
                } else {
                    openSlideZone(); // 打开滑动区域
                }
            }
        }
    };


    /**
     * 打开滑动区域
     * 发送消息隐藏垂直调节按钮，并回调双击事件
     */
    private void openSlideZone() {
        //关闭垂直调节按钮
        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true); // 发送消息强制隐藏垂直调节按钮
        onRightSmallListener.onSmallDoubleClick(MainRightLayoutItemChannelMaster.this); // 回调双击事件
    }

    /**
     * 扩展位置矩形范围
     * 增加点击判定区域
     *
     * @param rect 原始矩形
     * @return 扩展后的矩形
     */
    private Rect expandPosition(Rect rect) {
        rect.left = rect.left - 10; // 左边界左移10
        rect.top = rect.top - 3; // 上边界上移3
        rect.right = rect.right + 10; // 右边界右移10
        rect.bottom = rect.bottom + 3; // 下边界下移3
        return rect; // 返回扩展后的矩形
    }


    /**
     * 检查点击位置
     * 根据点击位置判断点击的是哪个控件
     *
     * @param point 点击坐标点
     */
    private void checkClickPosition(Point point) {
        Rect llProbeRect = Screen.getViewLocation(llProbe); // 获取探头倍率布局位置
        Rect leftPositionRect = Screen.getViewLocation(tvLeftPosition); // 获取位置控件位置
        Rect refTimeBaseRect = Screen.getViewLocation(tvTimeBase); // 获取时基控件位置
        Rect refTriggerTimeRect = Screen.getViewLocation(tvTriggerTime); // 获取触发时间控件位置
        String clickInfo = ""; // 点击信息字符串
        if (llProbeRect.contains(point.x, point.y)) {//垂直挡位 判断是否点击探头倍率区域
            clickInfo = getChIndex() + ";" + 0; // 设置点击信息为"通道索引;0"
        }
        if (leftPositionRect.contains(point.x, point.y)) {//垂直位置 判断是否点击位置区域
            clickInfo = getChIndex() + ";" + 1; // 设置点击信息为"通道索引;1"
        }
        if (refTimeBaseRect.contains(point.x, point.y)) {//水平档位 判断是否点击时基区域
            clickInfo = getChIndex() + ";" + 2; // 设置点击信息为"通道索引;2"
        }
        if (refTriggerTimeRect.contains(point.x, point.y)) {//水平位置 判断是否点击触发时间区域
            clickInfo = getChIndex() + ";" + 3; // 设置点击信息为"通道索引;3"
        }
        Logger.i(TAG, "MouseDoubleClickInfo= " + clickInfo); // 打印日志

        if (!clickInfo.isEmpty() && !WorkModeManage.getInstance().isXyMode()) { // 如果点击信息不为空且不是XY模式
            RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true); // 发送消息强制隐藏垂直调节按钮
            RxBus.getInstance().post(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION, clickInfo); // 发送鼠标点击位置消息
//            if (dialogSetChannelInfo == null) {//这是弹窗中统一处理的方案
//                dialogSetChannelInfo = (DialogSetChannelInfo) ((MainActivity) context).findViewById(R.id.dialogSetChannelInfo);
//            }
//            dialogSetChannelInfo.setData(getChIndex(), new DialogSetChannelInfo.OnDismissListener() {
//                @Override
//                public void onDismiss(int chIdx, HashMap<Integer, String> infoMap) {
//                }
//            });
        } else {
          openSlideZone(); // 打开滑动区域
        }
    }


//    private OnClickListener onClickListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (disable) {
//                return;
//            }
//            if (v.getId() == btnSmall.getId()) {
//                onSmallClick();
//            }
//        }
//    };

    // Handler消息常量
    private static final int HANDLE_MSG = 1; // Handler消息标识
    /**
     * Handler处理器
     * 处理延时消息和UI更新
     */
    private Handler handler = new Handler() {
        /**
         * 处理消息
         *
         * @param msg 消息对象
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg); // 调用父类处理方法
            switch (msg.what) { // 根据消息类型处理
                case HANDLE_MSG: // 处理HANDLE_MSG消息
                    setBgResId(); // 设置背景资源
                    break;
            }
        }
    };

}