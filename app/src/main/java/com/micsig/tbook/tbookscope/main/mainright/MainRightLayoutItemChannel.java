package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.MButton;
import com.micsig.tbook.ui.MButton_CheckBox_ThreeClick;
import com.micsig.tbook.ui.util.TBookUtil;

/**
 * +=============================================================================================+
 * |  模块定位：示波器主界面右侧面板 - 通道配置布局项                                             |
 * +=============================================================================================+
 * |  核心职责：                                                                                  |
 * |  1. 展示单个通道的配置信息和状态                                                             |
 * |  2. 提供通道开关、垂直灵敏度调节的UI交互                                                     |
 * |  3. 显示通道反相、耦合方式、带宽限制、探头倍数等参数                                         |
 * |  4. 支持外部按键和触摸屏的双重输入方式                                                       |
 * +=============================================================================================+
 * |  架构设计：                                                                                  |
 * |  - 自定义View组件（继承AbsoluteLayout）                                                      |
 * |  - 采用Viewholder模式管理子视图引用                                                          |
 * |  - 通过回调接口实现与父容器的交互                                                            |
 * |  - 支持属性配置（通过XML.styleable）                                                         |
 * +=============================================================================================+
 * |  数据流向：                                                                                  |
 * |  输入：XML属性配置 → TypedArray解析 → UI组件初始化                                           |
 * |  交互：按钮点击 → 回调接口 → 父容器处理 → 业务逻辑执行                                       |
 * |  显示：通道参数更新 → UI组件刷新 → 用户界面呈现                                              |
 * +=============================================================================================+
 * |  依赖关系：                                                                                  |
 * |  - Channel/ChannelFactory：获取通道数据和配置                                                |
 * |  - MButton/MButton_CheckBox_ThreeClick：自定义按钮组件                                       |
 * |  - TBookUtil：工具类，用于数值格式化                                                         |
 * |  - R.layout.layout_mainright_channel：布局资源文件                                           |
 * |  - R.styleable.MainLayoutRightChannel：自定义属性资源                                        |
 * +=============================================================================================+
 * |  使用场景：                                                                                  |
 * |  1. 在主界面右侧通道列表中展示各通道状态                                                     |
 * |  2. 用户通过点击按钮开关通道或调节垂直灵敏度                                                 |
 * |  3. 外部物理按键触发通道状态切换                                                             |
 * |  4. 通道参数变更后的UI实时更新                                                               |
 * +=============================================================================================+
 * Created by yangj on 2017/5/11.
 */

public class MainRightLayoutItemChannel extends AbsoluteLayout {
    // ==================== 成员变量定义 ====================
    private Context context; // 上下文对象，用于访问资源和创建视图
    private MButton_CheckBox_ThreeClick channelButton; // 通道名称按钮，支持三种点击状态（关闭/打开/打开菜单）
    private RelativeLayout showLayout; // 参数显示区域布局容器
    private TextView tvInvert; // 反相状态显示文本
    private TextView tvChannelText; // 通道名称显示文本（如CH1、CH2）
    private ImageView tvCouple; // 耦合方式图标显示（DC/AC/GND）
    private TextView tvProbeTypeNum, tvProbeTypeUtil; // 探头倍数数值和单位显示
    private TextView tvBandWidth; // 带宽限制显示文本（如20M、Full）
    private MButton btnMV, btnV; // 垂直灵敏度调节按钮（减小/增大档位）
    private TextView tvProbeDetailText; // 探头详细配置信息显示
    private View tvProbeDetailBg; // 探头详细信息背景视图

    private boolean checked = false; // 通道开关状态标志（true=打开，false=关闭）

    private int chIndex; // 通道索引（0-7对应CH1-CH8）
    private String text; // 通道名称字符串（如"CH1"）
    private boolean isInvert; // 反相状态标志（true=反相，false=正常）
    private int coupleResId; // 耦合方式图标资源ID
    private String strBandWidth; // 带宽限制字符串
    private String strProbeDetail; // 探头详细配置字符串
    private OnButtonClickListener onButtonClickListener; // 按钮点击回调接口

    /**
     * 是否来自外部按键
     */
    private boolean isFromExternalKey = false; // 标识输入源是否为外部物理按键

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  接口：OnButtonClickListener - 按钮点击事件回调                                          |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  定义通道布局项中各按钮的点击事件回调方法，由父容器实现具体逻辑                           |
     * +-----------------------------------------------------------------------------------------+
     */
    public interface OnButtonClickListener {
        /**
         * +-------------------------------------------------------------------------------------+
         * |  方法：onNameClick - 通道名称按钮点击回调                                            |
         * +-------------------------------------------------------------------------------------+
         * |  功能描述：                                                                          |
         * |  处理通道名称按钮的点击事件，实现三态切换逻辑                                         |
         * +-------------------------------------------------------------------------------------+
         * |  参数说明：                                                                          |
         * |  @param layout      当前布局项实例                                                   |
         * |  @param checked     目标开关状态                                                     |
         * |  @param before      此check是否是此次操作改变之前的状态                               |
         * |                      即如果为true则此check代表此次操作还未执行切换                     |
         * |  @param sound       是否播放声音                                                     |
         * +-------------------------------------------------------------------------------------+
         * |  返回值：                                                                            |
         * |  @return boolean  check的此次操作之后的最终状态                                       |
         * +-------------------------------------------------------------------------------------+
         */
        boolean onNameClick(MainRightLayoutItemChannel layout, boolean checked, boolean before, boolean sound);

        /**
         * +-------------------------------------------------------------------------------------+
         * |  方法：onMVClick - 垂直灵敏度减小按钮点击回调                                        |
         * +-------------------------------------------------------------------------------------+
         * |  功能描述：                                                                          |
         * |  处理垂直灵敏度减小按钮（mV）的点击事件                                               |
         * +-------------------------------------------------------------------------------------+
         * |  参数说明：                                                                          |
         * |  @param layout  当前布局项实例                                                       |
         * +-------------------------------------------------------------------------------------+
         */
        void onMVClick(MainRightLayoutItemChannel layout);

        /**
         * +-------------------------------------------------------------------------------------+
         * |  方法：onVClick - 垂直灵敏度增大按钮点击回调                                        |
         * +-------------------------------------------------------------------------------------+
         * |  功能描述：                                                                          |
         * |  处理垂直灵敏度增大按钮（V）的点击事件                                                |
         * +-------------------------------------------------------------------------------------+
         * |  参数说明：                                                                          |
         * |  @param layout  当前布局项实例                                                       |
         * +-------------------------------------------------------------------------------------+
         */
        void onVClick(MainRightLayoutItemChannel layout);
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  构造函数：MainRightLayoutItemChannel（单参数）                                         |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  仅传入Context的构造函数，用于代码动态创建实例                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param context  上下文对象                                                             |
     * +-----------------------------------------------------------------------------------------+
     */
    public MainRightLayoutItemChannel(Context context) {
        this(context, null); // 调用双参数构造函数，attrs传null
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  构造函数：MainRightLayoutItemChannel（双参数）                                         |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  传入Context和AttributeSet的构造函数，用于XML布局解析                                   |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param context  上下文对象                                                             |
     * |  @param attrs    属性集合，从XML布局中读取                                              |
     * +-----------------------------------------------------------------------------------------+
     */
    public MainRightLayoutItemChannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造函数，defStyleAttr传0
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  构造函数：MainRightLayoutItemChannel（三参数）                                         |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  完整构造函数，初始化所有成员变量并加载布局                                              |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param context      上下文对象                                                         |
     * |  @param attrs        属性集合，从XML布局中读取                                          |
     * |  @param defStyleAttr 默认样式属性                                                       |
     * +-----------------------------------------------------------------------------------------+
     * |  业务逻辑：                                                                              |
     * |  1. 设置默认参数值                                                                       |
     * |  2. 调用initView初始化视图                                                               |
     * +-----------------------------------------------------------------------------------------+
     */
    public MainRightLayoutItemChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造函数
        this.context = context; // 保存上下文引用
        text = "CH1"; // 设置默认通道名称
        isInvert = true; // 设置默认反相状态
        coupleResId = R.drawable.coupling_dc; // 设置默认耦合方式图标（DC耦合）
        strBandWidth = "20M"; // 设置默认带宽限制
        strProbeDetail = "1mX"; // 设置默认探头倍数
        initView(attrs, defStyleAttr); // 初始化视图组件
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：initView - 初始化视图组件                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  加载布局资源、解析自定义属性、初始化所有子视图组件并设置监听器                          |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param attrs        属性集合                                                           |
     * |  @param defStyleAttr 默认样式属性                                                       |
     * +-----------------------------------------------------------------------------------------+
     * |  业务逻辑：                                                                              |
     * |  1. 加载布局资源文件                                                                     |
     * |  2. 解析自定义属性（checked、text、marginTop等）                                         |
     * |  3. 初始化所有子视图组件                                                                 |
     * |  4. 设置布局参数和背景资源                                                               |
     * |  5. 注册按钮点击监听器                                                                   |
     * +-----------------------------------------------------------------------------------------+
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_mainright_channel, this); // 加载布局资源并添加到当前视图
//        setBackgroundResource(R.color.color_Backcolor_black);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainLayoutRightChannel); // 获取自定义属性数组
        checked = ta.getBoolean(R.styleable.MainLayoutRightChannel_checked, false); // 解析checked属性（默认false）
        text = ta.getString(R.styleable.MainLayoutRightChannel_text); // 解析text属性（通道名称）
        chIndex = Integer.parseInt(text.replace("CH", "")) - 1; // 计算通道索引（去掉"CH"并减1）
        int showLayoutMarginTop = ta.getDimensionPixelSize(R.styleable.MainLayoutRightChannel_showLayoutMarginTop, 32); // 解析显示区域顶部边距
        int bgLeftChecked = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgLeftChecked, R.drawable.l_ch1); // 解析选中状态左侧背景
        int bgLeftUnChecked = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgLeftUnChecked, R.drawable.l_unclick); // 解析未选中状态左侧背景
        int bgRightTopTouchDown = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightTopTouchDown, R.drawable.ch1_mv_press); // 解析mV按钮按下状态背景
        int bgRightTopTouchUp = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightTopTouchUp, R.drawable.ch1_mv); // 解析mV按钮抬起状态背景
        int bgRightBottomTouchDown = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightBottomTouchDown, R.drawable.ch1_v_press); // 解析V按钮按下状态背景
        int bgRightBottomTouchUp = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgRightBottomTouchUp, R.drawable.ch1_v); // 解析V按钮抬起状态背景
        int bgProbeDetail = ta.getResourceId(R.styleable.MainLayoutRightChannel_bgProbeDetail, R.drawable.ch1_10x); // 解析探头详细信息背景
        ta.recycle(); // 回收TypedArray资源

        channelButton = (MButton_CheckBox_ThreeClick) findViewById(R.id.channelButton); // 获取通道名称按钮引用
        showLayout = (RelativeLayout) findViewById(R.id.showLayout); // 获取参数显示区域布局引用
        tvInvert = (TextView) findViewById(R.id.invert); // 获取反相状态文本引用
        tvChannelText = (TextView) findViewById(R.id.channelText); // 获取通道名称文本引用
        tvCouple = (ImageView) findViewById(R.id.couple); // 获取耦合方式图标引用
        tvProbeTypeNum = (TextView) findViewById(R.id.probeTypeNum); // 获取探头倍数数值引用
        tvProbeTypeUtil = (TextView) findViewById(R.id.probeTypeUnit); // 获取探头倍数单位引用
        tvBandWidth = (TextView) findViewById(R.id.bandWidth); // 获取带宽限制文本引用
        btnMV = (MButton) findViewById(R.id.mV); // 获取mV按钮引用
        btnV = (MButton) findViewById(R.id.v); // 获取V按钮引用
        tvProbeDetailText = (TextView) findViewById(R.id.probeDetailText); // 获取探头详细信息文本引用
        tvProbeDetailBg = findViewById(R.id.probeDetailBg); // 获取探头详细信息背景引用

        AbsoluteLayout.LayoutParams lpShow = (LayoutParams) showLayout.getLayoutParams(); // 获取显示区域布局参数
        lpShow.y = showLayoutMarginTop - 4; // 设置显示区域Y坐标（减4像素调整）
        showLayout.setLayoutParams(lpShow); // 应用布局参数
        setChecked(); // 根据checked状态更新UI显示
        AbsoluteLayout.LayoutParams lpChannel = (LayoutParams) channelButton.getLayoutParams(); // 获取通道按钮布局参数
        lpChannel.y = showLayoutMarginTop - 2; // 设置通道按钮Y坐标（减2像素调整）
        channelButton.setLayoutParams(lpChannel); // 应用布局参数
        channelButton.setText_y(7); // 设置按钮文本Y坐标偏移
        channelButton.setCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftChecked)); // 设置选中状态位图
        channelButton.setUnCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftUnChecked)); // 设置未选中状态位图
        btnMV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchDown)); // 设置mV按钮按下状态位图
        btnMV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchUp)); // 设置mV按钮抬起状态位图
        btnV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchDown)); // 设置V按钮按下状态位图
        btnV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchUp)); // 设置V按钮抬起状态位图
        tvProbeDetailBg.setBackgroundResource(bgProbeDetail); // 设置探头详细信息背景资源

        channelButton.setOnThreeClickListener(onThreeClickListener); // 注册通道按钮三态点击监听器
        btnMV.setOnClickListener(onCheckedChangeListener); // 注册mV按钮点击监听器
        btnV.setOnClickListener(onCheckedChangeListener); // 注册V按钮点击监听器
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  监听器：onThreeClickListener - 通道按钮三态点击监听器                                   |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  实现MButton_CheckBox_ThreeClick的三态点击回调，转发到onThreeClick方法处理              |
     * +-----------------------------------------------------------------------------------------+
     */
    private MButton_CheckBox_ThreeClick.OnThreeClickListener onThreeClickListener = new MButton_CheckBox_ThreeClick.OnThreeClickListener() {
        @Override
        public boolean onThreeClick(boolean checked) {
            return MainRightLayoutItemChannel.this.onThreeClick(checked); // 转发到外部类的onThreeClick方法
        }
    };

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：onThreeClick - 处理通道按钮三态点击                                              |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  处理通道名称按钮的三态点击逻辑，根据checked状态执行相应操作                             |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param checked  目标开关状态                                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  返回值：                                                                                |
     * |  @return boolean  最终的开关状态                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  业务逻辑：                                                                              |
     * |  1. 检查是否可用                                                                         |
     * |  2. 调用回调接口获取最终状态                                                             |
     * |  3. 更新UI显示                                                                           |
     * +-----------------------------------------------------------------------------------------+
     */
    public boolean onThreeClick(boolean checked) {
        if (!isEnabled()) return isChecked(); // 如果不可用，返回当前状态
        if (onButtonClickListener != null) {
            checked = onButtonClickListener.onNameClick(MainRightLayoutItemChannel.this, checked, true, true); // 调用回调获取最终状态
        } else {
            checked = !checked; // 无回调则切换状态
        }
        setChecked(checked); // 更新UI显示
        return checked; // 返回最终状态
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  监听器：onCheckedChangeListener - mV/V按钮点击监听器                                    |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  处理垂直灵敏度调节按钮（mV和V）的点击事件                                               |
     * +-----------------------------------------------------------------------------------------+
     */
    private View.OnClickListener onCheckedChangeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEnabled()) return; // 如果不可用，直接返回
            if (v.getId() == btnMV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onMVClick(MainRightLayoutItemChannel.this); // 调用mV按钮回调
                }
            } else if (v.getId() == btnV.getId()) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onVClick(MainRightLayoutItemChannel.this); // 调用V按钮回调
                }
            }
        }
    };

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setChecked - 根据checked状态更新UI显示                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  根据通道开关状态（checked）更新所有UI组件的显示状态                                     |
     * +-----------------------------------------------------------------------------------------+
     * |  业务逻辑：                                                                              |
     * |  - checked=true：隐藏按钮文本、显示参数区域、显示调节按钮                               |
     * |  - checked=false：显示按钮文本、隐藏参数区域、隐藏调节按钮                              |
     * +-----------------------------------------------------------------------------------------+
     */
    private void setChecked() {
        if (!isEnabled()) return; // 如果不可用，直接返回
        if (checked) {
            channelButton.setText(""); // 清空按钮文本（选中状态）
            tvChannelText.setText(text); // 设置通道名称文本
            channelButton.setChecked(true); // 设置按钮选中状态
            showLayout.setVisibility(VISIBLE); // 显示参数区域
            btnMV.setVisibility(VISIBLE); // 显示mV按钮
            btnV.setVisibility(VISIBLE); // 显示V按钮
            tvProbeDetailText.setVisibility(VISIBLE); // 显示探头详细信息
            tvProbeDetailBg.setVisibility(VISIBLE); // 显示探头详细信息背景
        } else {
            channelButton.setText(text); // 设置按钮文本（未选中状态）
            channelButton.setChecked(false); // 设置按钮未选中状态
            showLayout.setVisibility(INVISIBLE); // 隐藏参数区域
            btnMV.setVisibility(INVISIBLE); // 隐藏mV按钮
            btnV.setVisibility(INVISIBLE); // 隐藏V按钮
            tvProbeDetailText.setVisibility(INVISIBLE); // 隐藏探头详细信息
            tvProbeDetailBg.setVisibility(INVISIBLE); // 隐藏探头详细信息背景
        }
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：isFromExternalKey - 获取外部按键标志                                             |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  返回当前输入是否来自外部物理按键                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  返回值：                                                                                |
     * |  @return boolean  true=外部按键，false=触摸屏                                           |
     * +-----------------------------------------------------------------------------------------+
     */
    public boolean isFromExternalKey() {
        return isFromExternalKey; // 返回外部按键标志
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setFromExternalKey - 设置外部按键标志                                             |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置当前输入是否来自外部物理按键                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param fromExternalKey  外部按键标志                                                   |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setFromExternalKey(boolean fromExternalKey) {
        isFromExternalKey = fromExternalKey; // 设置外部按键标志
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setVScale - 设置垂直灵敏度值                                                     |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置通道的垂直灵敏度值，并更新UI显示                                                    |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param vScale  垂直灵敏度值（单位：V/div）                                              |
     * +-----------------------------------------------------------------------------------------+
     * |  业务逻辑：                                                                              |
     * |  1. 从ChannelFactory获取通道对象                                                        |
     * |  2. 设置垂直灵敏度值                                                                    |
     * |  3. 格式化并更新UI显示                                                                  |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setVScale(double vScale) {
        Channel channel = ChannelFactory.getDynamicChannel(chIndex); // 获取通道对象
        channel.setVScaleVal(vScale); // 设置垂直灵敏度值
        String probeEvery = TBookUtil.getMFromDouble(channel.getVScaleVal()); // 格式化数值
        setStrProbeTypeNum(probeEvery); // 更新UI显示
//        return probeEvery;
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：getText - 获取通道名称文本                                                       |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  返回通道名称字符串（如"CH1"）                                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  返回值：                                                                                |
     * |  @return String  通道名称文本                                                           |
     * +-----------------------------------------------------------------------------------------+
     */
    public String getText() {
        return text; // 返回通道名称
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setChecked - 设置通道开关状态                                                     |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置通道开关状态并更新UI显示                                                            |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param checked  开关状态（true=打开，false=关闭）                                       |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setChecked(boolean checked) {
        if (!isEnabled()) return; // 如果不可用，直接返回
        this.checked = checked; // 设置checked状态
        setChecked(); // 更新UI显示
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：isChecked - 获取通道开关状态                                                     |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  返回通道开关状态                                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  返回值：                                                                                |
     * |  @return boolean  开关状态（true=打开，false=关闭）                                      |
     * +-----------------------------------------------------------------------------------------+
     */
    public boolean isChecked() {
        return checked; // 返回checked状态
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setInvert - 设置反相状态                                                         |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置通道反相状态并更新UI显示                                                            |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param invert  反相状态（true=反相，false=正常）                                        |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setInvert(boolean invert) {
        isInvert = invert; // 设置反相状态
        tvInvert.setVisibility(isInvert ? VISIBLE : INVISIBLE); // 更新反相标志显示
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setCoupleResId - 设置耦合方式图标                                                 |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置耦合方式图标资源ID并更新UI显示                                                      |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param coupleResId  图标资源ID（如R.drawable.coupling_dc）                              |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setCoupleResId(int coupleResId) {
        this.coupleResId = coupleResId; // 保存耦合方式图标ID
        tvCouple.setImageResource(this.coupleResId); // 更新UI显示
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setStrProbeTypeUnit - 设置探头倍数单位                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置探头倍数单位文本（如"V"或"A"）                                                      |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param strProbeTypeUnit  单位文本                                                      |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setStrProbeTypeUnit(String strProbeTypeUnit) {
        tvProbeTypeUtil.setText(strProbeTypeUnit); // 设置单位文本
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setStrProbeTypeNum - 设置探头倍数数值                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置探头倍数数值文本（如"1"、"2"、"5"等）                                               |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param strProbeTypeNum  数值文本                                                      |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setStrProbeTypeNum(String strProbeTypeNum) {
        tvProbeTypeNum.setText(strProbeTypeNum); // 设置数值文本
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setStrBandWidth - 设置带宽限制文本                                               |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置带宽限制文本并更新UI显示                                                            |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param strBandWidth  带宽限制文本（如"20M"、"Full"）                                    |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setStrBandWidth(String strBandWidth) {
        this.strBandWidth = strBandWidth; // 保存带宽限制文本
        tvBandWidth.setText(this.strBandWidth); // 更新UI显示
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setStrProbeDetail - 设置探头详细配置文本                                         |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置探头详细配置文本并更新UI显示                                                        |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param strProbeDetail  探头详细配置文本（如"1mX"、"10X"）                               |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setStrProbeDetail(String strProbeDetail) {
        this.strProbeDetail = strProbeDetail; // 保存探头详细配置文本
        tvProbeDetailText.setText(this.strProbeDetail); // 更新UI显示
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：getStrProbeType - 获取探头倍数完整文本                                           |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  获取探头倍数的完整文本（数值+单位）                                                     |
     * +-----------------------------------------------------------------------------------------+
     * |  返回值：                                                                                |
     * |  @return String  探头倍数完整文本（如"1V"、"2A"）                                        |
     * +-----------------------------------------------------------------------------------------+
     */
    public String getStrProbeType() {
        return tvProbeTypeNum.getText().toString() + tvProbeTypeUtil.getText().toString(); // 拼接数值和单位
    }

    /**
     * +-----------------------------------------------------------------------------------------+
     * |  方法：setOnButtonClickListener - 设置按钮点击回调接口                                 |
     * +-----------------------------------------------------------------------------------------+
     * |  功能描述：                                                                              |
     * |  设置按钮点击事件回调接口                                                                |
     * +-----------------------------------------------------------------------------------------+
     * |  参数说明：                                                                              |
     * |  @param onButtonClickListener  回调接口实例                                             |
     * +-----------------------------------------------------------------------------------------+
     */
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener; // 设置回调接口
    }
}