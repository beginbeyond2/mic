package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.MButton;
import com.micsig.tbook.ui.MButton_CheckBox_ThreeClick;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/5/11.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     MainRightLayoutItemOthers 类说明                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   示波器右侧菜单布局控件 - Math/Ref/Serials(串口)功能项的容器布局            │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 管理Math数学运算、Ref参考波形、Serials串口协议菜单项的UI显示与交互     │
 * │   2. 处理菜单项的选中/未选中状态切换及视觉效果                               │
 * │   3. 显示和更新串口协议相关参数(通道、波特率、电平等)                        │
 * │   4. 响应用户点击事件并通过回调接口通知外部处理逻辑                          │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   继承自AbsoluteLayout绝对布局，采用复合视图设计模式：                      │
 * │   - 左侧区域：名称按钮(选中/未选中两种状态)                                  │
 * │   - 右侧区域：分为Math/Ref模式和Serials串口模式                             │
 * │     • Math/Ref模式：显示mV/V按钮、时基、档位信息                            │
 * │     • Serials模式：显示多行串口参数信息(最多4行)                            │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   外部调用setter方法 → 更新UI状态 → 用户交互触发点击 → 回调接口通知外部     │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   • UI组件: MButton, MButton_CheckBox_ThreeClick, TextView                  │
 * │   • 工具类: Tools(文本测量), StrUtil(字符串处理)                             │
 * │   • 资源: R.layout.layout_mainright, R.styleable.MainLayoutRight            │
 * │   • 串口协议: RightLayoutSerials(定义UART/SPI/I2C/CAN等协议类型)             │
 * │                                                                             │
 * │ 【使用场景】                                                                 │
 * │   在示波器主界面右侧菜单中，用于显示和管理Math运算、Ref参考波形、           │
 * │   Serials串口协议解析等功能项，支持动态切换选中状态和参数显示               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainRightLayoutItemOthers extends AbsoluteLayout {
    private Context context; // 上下文对象，用于获取资源和创建视图
    private MButton_CheckBox_ThreeClick tvNameUnCheck; // 左侧未选中状态的名称按钮(支持三击)
    private LinearLayout leftLayout; // 左侧布局容器，包含选中后的名称和参数显示
    private AbsoluteLayout mathRefRightLayout; // Math/Ref模式下的右侧布局
    private LinearLayout serialsRightLayout; // Serials串口模式下的右侧布局
    private TextView tvNameCheck, tvScale, tvTimebase; // 选中后的名称文本、垂直档位、时基显示
    private MButton btnMV, btnV; // mV和V单位切换按钮
    private TextView tv1, tv2, tv3, tv4; // 串口模式下的4行参数显示文本

    private int serialsType = RightLayoutSerials.SERIALS_UART; // 串口协议类型，默认UART
    private int text1Ch = 1;//1-4 // 第一行文本对应的通道号(1-4)
    private int text2Ch = 1; // 第二行文本对应的通道号
    private int text3Ch = 1; // 第三行文本对应的通道号
    private boolean isSerials; // 是否为串口模式
    private boolean checked; // 当前是否选中状态
    private boolean rightEnabled = true; // 右侧按钮区域是否可用
    private String strNameUnCheck, strNameCheck; // 未选中/选中状态的名称文本
    private String strTimeBase; // 时基字符串
    private SpannableStringBuilder strSerialsMsg; // 串口消息的富文本内容
    private OnButtonClickListener onButtonClickListener; // 按钮点击事件回调监听器

    /**
     * 是否来自外部按键
     */
    private boolean isFromExternalKey = false;

    /**
     * 按钮点击事件回调接口
     * 定义了各类按钮点击事件的回调方法
     */
    public interface OnButtonClickListener {
        /**
         * 名称按钮点击回调
         * @param before 此check是否是此次操作改变之前的状态，
         *               即如果为true则此check代表此次操作还未执行切换，需要在该接口实现里进行切换状态
         * @return check的此次操作之后的最终状态
         */
        boolean onNameClick(MainRightLayoutItemOthers layout, boolean checked, boolean before);

        /**
         * mV按钮点击回调
         */
        void onMVClick(MainRightLayoutItemOthers layout);

        /**
         * V按钮点击回调
         */
        void onVClick(MainRightLayoutItemOthers layout);

        /**
         * 串口区域点击回调
         */
        void onSerialsClick(MainRightLayoutItemOthers layout);

        /**
         * 当当前控件enable属性为false时，点击控件所使用的回调
         */
        void onEnableFalseClick(MainRightLayoutItemOthers layout);
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public MainRightLayoutItemOthers(Context context) {
        this(context, null); // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs XML属性集
     */
    public MainRightLayoutItemOthers(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造函数
    }

    /**
     * 三参数构造函数(完整构造函数)
     * @param context 上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainRightLayoutItemOthers(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造函数
        this.context = context; // 保存上下文引用
        initView(attrs, defStyleAttr); // 初始化视图
    }

    /**
     * 初始化视图
     * 从XML加载布局并初始化所有UI组件和属性
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_mainright, this); // 加载布局文件
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainLayoutRight); // 获取自定义属性集
        checked = ta.getBoolean(R.styleable.MainLayoutRight_checked, false); // 读取checked属性，默认false
        isSerials = ta.getBoolean(R.styleable.MainLayoutRight_isSerials, false); // 读取isSerials属性，默认false
        strNameUnCheck = ta.getString(R.styleable.MainLayoutRight_text); // 读取未选中状态的名称文本
        if (isSerials) { // 如果是串口模式
            strNameCheck = strNameUnCheck + "\nUART"; // 选中后添加UART后缀
        } else { // 非串口模式
            strNameCheck = strNameUnCheck; // 选中后名称与未选中相同
        }
        int bgLeftChecked = ta.getResourceId(R.styleable.MainLayoutRight_bgLeftChecked, R.drawable.l_math); // 选中状态左侧背景
        int bgLeftUnChecked = ta.getResourceId(R.styleable.MainLayoutRight_bgLeftUnChecked, R.drawable.l_unclick); // 未选中状态左侧背景
        int bgRightTopTouchDown = ta.getResourceId(R.styleable.MainLayoutRight_bgRightTopTouchDown, R.drawable.math_mv_1); // 右上按钮按下背景
        int bgRightTopTouchUp = ta.getResourceId(R.styleable.MainLayoutRight_bgRightTopTouchUp, R.drawable.math_mv); // 右上按钮抬起背景
        int bgRightBottomTouchDown = ta.getResourceId(R.styleable.MainLayoutRight_bgRightBottomTouchDown, R.drawable.math_v_1); // 右下按钮按下背景
        int bgRightBottomTouchUp = ta.getResourceId(R.styleable.MainLayoutRight_bgRightBottomTouchUp, R.drawable.math_v); // 右下按钮抬起背景
        int text_x = ta.getDimensionPixelSize(R.styleable.MainLayoutRight_text_x, 5); // 文本X坐标偏移量
        ta.recycle(); // 回收TypedArray资源

        tvNameUnCheck = (MButton_CheckBox_ThreeClick) findViewById(R.id.nameUnCheck); // 获取未选中状态名称按钮
        leftLayout = (LinearLayout) findViewById(R.id.leftLayout); // 获取左侧布局容器
        mathRefRightLayout = (AbsoluteLayout) findViewById(R.id.mathRefRightLayout); // 获取Math/Ref右侧布局
        serialsRightLayout = (LinearLayout) findViewById(R.id.serialsRightLayout); // 获取串口右侧布局
        tvNameCheck = (TextView) findViewById(R.id.nameCheck); // 获取选中后名称文本
        tvScale = (TextView) findViewById(R.id.vertical); // 获取垂直档位文本
        tvTimebase = (TextView) findViewById(R.id.timebase); // 获取时基文本
        btnMV = (MButton) findViewById(R.id.btnmV); // 获取mV按钮
        btnV = (MButton) findViewById(R.id.btnV); // 获取V按钮
        tv1 = (TextView) findViewById(R.id.text1); // 获取串口参数第1行文本
        tv2 = (TextView) findViewById(R.id.text2); // 获取串口参数第2行文本
        tv3 = (TextView) findViewById(R.id.text3); // 获取串口参数第3行文本
        tv4 = (TextView) findViewById(R.id.text4); // 获取串口参数第4行文本
        tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 设置第1行文本抗锯齿
        tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 设置第2行文本抗锯齿
        tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 设置第3行文本抗锯齿
        tv4.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 设置第4行文本抗锯齿

//        tvNameUnCheck.setText_x(text_x);
        tvNameUnCheck.setCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftChecked)); // 设置选中状态位图
        tvNameUnCheck.setUnCheckBitmap(BitmapFactory.decodeResource(getResources(), bgLeftUnChecked)); // 设置未选中状态位图
        btnMV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchDown)); // 设置mV按钮按下位图
        btnMV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightTopTouchUp)); // 设置mV按钮抬起始图
        btnV.setTouchDownBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchDown)); // 设置V按钮按下位图
        btnV.setTouchUpBitmap(BitmapFactory.decodeResource(getResources(), bgRightBottomTouchUp)); // 设置V按钮抬起始图
        if (!StrUtil.isEmpty(strNameUnCheck)) { // 如果名称不为空
            if (strNameUnCheck.contains("S1")) { // 如果包含S1标识
                serialsRightLayout.setBackgroundColor(getResources().getColor(R.color.color_S1)); // 设置S1通道颜色
            } else if (strNameUnCheck.contains("S2")) { // 如果包含S2标识
                serialsRightLayout.setBackgroundColor(getResources().getColor(R.color.color_S2)); // 设置S2通道颜色
            }
        }

        setChecked(); // 设置选中状态
        tvNameUnCheck.setOnThreeClickListener(onThreeClickListener); // 设置三击监听器
        btnMV.setOnClickListener(onCheckedChangeListener); // 设置mV按钮点击监听器
        btnV.setOnClickListener(onCheckedChangeListener); // 设置V按钮点击监听器
        serialsRightLayout.setOnClickListener(onCheckedChangeListener); // 设置串口布局点击监听器
    }

    /**
     * 获取是否来自外部按键
     * @return true表示来自外部按键，false表示来自内部操作
     */
    public boolean isFromExternalKey() {
        return isFromExternalKey; // 返回外部按键标志
    }

    /**
     * 设置是否来自外部按键
     * @param fromExternalKey true表示来自外部按键，false表示来自内部操作
     */
    public void setFromExternalKey(boolean fromExternalKey) {
        isFromExternalKey = fromExternalKey; // 设置外部按键标志
    }

    /**
     * 三击事件监听器
     * 用于处理名称按钮的三击事件
     */
    private MButton_CheckBox_ThreeClick.OnThreeClickListener onThreeClickListener = new MButton_CheckBox_ThreeClick.OnThreeClickListener() {
        @Override
        public boolean onThreeClick(boolean check) {
            return MainRightLayoutItemOthers.this.onThreeClick(check); // 转发到外部方法处理
        }
    };

    /**
     * 处理三击事件
     * @param check 当前选中状态
     * @return 处理后的选中状态
     */
    public boolean onThreeClick(boolean check) {
        if (!isEnabled()) { // 如果控件不可用
            if (onButtonClickListener != null) { // 如果设置了回调监听器
                onButtonClickListener.onEnableFalseClick(MainRightLayoutItemOthers.this); // 触发不可用状态点击回调
            }
            return false; // 返回false表示未处理
        }
        if (onButtonClickListener != null) { // 如果设置了回调监听器
            check = onButtonClickListener.onNameClick(MainRightLayoutItemOthers.this, check, true); // 通过回调决定最终状态
        } else { // 未设置回调监听器
            check = !check; // 直接切换状态
        }
        setChecked(check); // 更新选中状态
        return check; // 返回最终状态
    }

    /**
     * 按钮点击事件监听器
     * 统一处理mV、V按钮和串口区域的点击事件
     */
    private View.OnClickListener onCheckedChangeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEnabled() || !rightEnabled) return; // 如果控件不可用或右侧区域禁用，直接返回
            if (v.getId() == btnMV.getId()) { // 如果是mV按钮
                if (onButtonClickListener != null) { // 如果设置了回调监听器
                    onButtonClickListener.onMVClick(MainRightLayoutItemOthers.this); // 触发mV按钮点击回调
                }
            } else if (v.getId() == btnV.getId()) { // 如果是V按钮
                if (onButtonClickListener != null) { // 如果设置了回调监听器
                    onButtonClickListener.onVClick(MainRightLayoutItemOthers.this); // 触发V按钮点击回调
                }
            } else if (v.getId() == serialsRightLayout.getId()) { // 如果是串口布局
                if (onButtonClickListener != null) { // 如果设置了回调监听器
                    onButtonClickListener.onSerialsClick(MainRightLayoutItemOthers.this); // 触发串口点击回调
                }
            }
        }
    };

    /**
     * 设置右侧按钮区域是否可用
     * @param enabled true表示可用，false表示禁用
     */
    public void setRightEnabled(boolean enabled) {
        this.rightEnabled = enabled; // 设置右侧区域可用标志
    }

    /**
     * 设置垂直档位文本
     * 根据文本宽度自适应调整文本大小
     * @param scale 档位文本内容
     */
    public void setTvScale(String scale) {
        Paint paint = new Paint(); // 创建画笔对象
        paint.setTextSize(14); // 设置文本大小为14
        Rect textRect = Tools.getTextRect(scale, paint); // 测量文本边界
        if (textRect.width() >= 59) { // 如果文本宽度大于等于59像素
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 11); // 设置文本大小为11像素
        } else if (textRect.width() >= 48) { // 如果文本宽度大于等于48像素
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12); // 设置文本大小为12像素
        } else { // 文本宽度小于48像素
            tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14); // 设置文本大小为14像素
        }
        tvScale.setText(scale); // 设置档位文本内容
    }

    /**
     * 设置时基文本
     * @param timeBase 时基文本内容
     */
    public void setTvTimebase(String timeBase) {
        tvTimebase.setText(timeBase); // 设置时基文本内容
    }

    /**
     * 获取垂直档位文本
     * @return 当前档位文本内容
     */
    public String getTvScale() {
        return tvScale.getText().toString(); // 返回档位文本
    }

    /**
     * 根据选中状态更新UI显示
     * 切换选中/未选中状态下的布局和文本显示
     */
    private void setChecked() {
        if (checked) { // 如果选中状态
            tvNameUnCheck.setText(""); // 清空未选中按钮文本
            tvNameUnCheck.setChecked(true); // 设置按钮为选中状态
            leftLayout.setVisibility(VISIBLE); // 显示左侧布局
            tvNameCheck.setText(strNameCheck); // 设置选中后名称文本
            if (isSerials) { // 如果是串口模式
                tvScale.setVisibility(GONE); // 隐藏档位文本
                tvTimebase.setVisibility(GONE); // 隐藏时基文本
                serialsRightLayout.setVisibility(VISIBLE); // 显示串口右侧布局
                mathRefRightLayout.setVisibility(GONE); // 隐藏Math/Ref右侧布局
            } else { // 非串口模式
                tvScale.setVisibility(VISIBLE); // 显示档位文本
                tvTimebase.setVisibility(VISIBLE); // 显示时基文本
                mathRefRightLayout.setVisibility(VISIBLE); // 显示Math/Ref右侧布局
                serialsRightLayout.setVisibility(GONE); // 隐藏串口右侧布局
            }
        } else { // 未选中状态
            tvNameUnCheck.setText(strNameUnCheck); // 设置未选中按钮文本
            tvNameUnCheck.setChecked(false); // 设置按钮为未选中状态
            leftLayout.setVisibility(GONE); // 隐藏左侧布局
            serialsRightLayout.setVisibility(GONE); // 隐藏串口右侧布局
            mathRefRightLayout.setVisibility(GONE); // 隐藏Math/Ref右侧布局
        }
    }


    /**
     * 设置阈值电平值的显示，只有当当前显示的channel与当前改变的channel相同时，才会修改
     *
     * @param curCh     本次修改的channel
     * @param levelType 本次修改channel的px值
     */
    public void setCommonValueLevel(int curCh, int levelType, int levelMode) {
        String val = Tools.getChannelLevel(curCh, levelType, levelMode); // 获取通道电平值字符串
        switch (serialsType) { // 根据串口类型分发处理
            case RightLayoutSerials.SERIALS_UART: // UART协议
            case RightLayoutSerials.SERIALS_LIN: // LIN协议
            case RightLayoutSerials.SERIALS_CAN: // CAN协议
            case RightLayoutSerials.SERIALS_M1553B: // M1553B协议
                if (text1Ch == curCh) { // 如果第1行通道号匹配
                    setSerialsTextLine1(val); // 设置第1行文本
                }
                break;
            case RightLayoutSerials.SERIALS_SPI: // SPI协议
                if (text1Ch == curCh) { // 如果第1行通道号匹配
                    setSerialsTextLine1(val); // 设置第1行文本
                } else if (text2Ch == curCh) { // 如果第2行通道号匹配
                    setSerialsTextLine2(val); // 设置第2行文本
                } else if (text3Ch == curCh && tv3.getVisibility() == VISIBLE) { // 如果第3行通道号匹配且可见
                    setSerialsTextLine3(val); // 设置第3行文本
                }
                break;
            case RightLayoutSerials.SERIALS_I2C: // I2C协议
                if (text1Ch == curCh) { // 如果第1行通道号匹配
                    setSerialsTextLine1(val); // 设置第1行文本
                } else if (text2Ch == curCh) { // 如果第2行通道号匹配
                    setSerialsTextLine2(val); // 设置第2行文本
                }
                break;
            case RightLayoutSerials.SERIALS_M429: // M429协议
                if (text1Ch == curCh) { // 如果第1行通道号匹配
//                    Logger.i("setCommonValueLevel:" + curCh +"\t" + levelType + "\t" + val);
//                    try {
//                        throw new IllegalAccessException();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
                    if (levelType == Tools.LevelType_High) { // 如果是高电平
                        setSerialsTextLine1(val); // 设置第1行为高电平值
                    } else { // 如果是低电平
                        setSerialsTextLine2(val); // 设置第2行为低电平值
                    }
                }
                break;
        }
    }

    /**
     * @return 结果为：1u、1m、1等不带单位的数据
     */
//    public String addRefVScale() {
//        RefChannel channel = ChannelFactory.getRefChannel(ChannelFactory.getInstance().getTopRefChannel().getChId());
//        channel.setVScaleId(channel.getVScaleId() - 1);
//        double vScale = channel.getVScaleIdVal();
//        int iUnit = channel.getProbeType();
//        setTvScale(TBookUtil.getMFromDouble(vScale) + ChannelFactory.getProbeString(iUnit));
//        return tvScale.getText().toString();
//    }
//
//    /**
//     * @return 结果为：1u、1m、1等不带单位的数据
//     */
//    public String subRefVScale() {
//        RefChannel channel = ChannelFactory.getRefChannel(ChannelFactory.getInstance().getTopRefChannel().getChId());
//        channel.setVScaleId(channel.getVScaleId() + 1);
//        double vScale = channel.getVScaleIdVal();
//        int iUnit = channel.getProbeType();
//        setTvScale(TBookUtil.getMFromDouble(vScale) + ChannelFactory.getProbeString(iUnit));
//        return tvScale.getText().toString();
//    }

    /**
     * 设置当前的总线类型
     * @param serialsType 串口协议类型(UART/SPI/I2C/CAN等)
     */
    public void setSerialsType(int serialsType) {
        this.serialsType = serialsType; // 设置串口协议类型
    }

    /**
     * 设置除了spi、i2c模式以外的模式下，两个通道的通道号
     * @param ch 通道号
     */
    public void setCommonCh(int ch) {
        this.text1Ch = ch; // 设置第1行文本通道号
    }

    /**
     * 设置i2c模式下，两个通道的通道号
     * @param text1Ch 第1行文本通道号
     * @param text2Ch 第2行文本通道号
     */
    public void setI2cCh(int text1Ch, int text2Ch) {
        this.text1Ch = text1Ch; // 设置第1行文本通道号
        this.text2Ch = text2Ch; // 设置第2行文本通道号
    }

    /**
     * 设置spi模式下，两个通道的通道号
     * @param text1Ch 第1行文本通道号
     * @param text2Ch 第2行文本通道号
     * @param text3Ch 第3行文本通道号
     */
    public void setSpiCh(int text1Ch, int text2Ch, int text3Ch) {
        this.text1Ch = text1Ch; // 设置第1行文本通道号
        this.text2Ch = text2Ch; // 设置第2行文本通道号
        this.text3Ch = text3Ch; // 设置第3行文本通道号
    }

    /**
     * 设置选中状态
     * @param checked true表示选中，false表示未选中
     */
    public void setChecked(boolean checked) {
        if (!isEnabled()) return; // 如果控件不可用，直接返回
        if (this.checked != checked) { // 如果状态发生变化
            this.checked = checked; // 更新选中状态
            setChecked(); // 刷新UI显示
        }
    }

    /**
     * 获取选中状态
     * @return true表示选中，false表示未选中
     */
    public boolean isChecked() {
        return checked; // 返回选中状态
    }

    /**
     * 获取名称文本
     * @return 选中状态的名称文本
     */
    public String getName() {
        return strNameCheck; // 返回选中状态名称
    }

    /**
     * 设置名称文本
     * @param strNameCheck 选中状态的名称文本
     */
    public void setName(String strNameCheck) {
        this.strNameCheck = strNameCheck; // 设置选中状态名称
        tvNameCheck.setText(strNameCheck); // 更新名称文本显示
    }

    /**
     * 设置串口参数第1行文本
     * @param text 文本内容
     */
    public void setSerialsTextLine1(String text) {
        if (!StrUtil.isEmpty(text)) { // 如果文本不为空
            text = text.replace("b/s", ""); // 移除"b/s"单位
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            Rect textRect = Tools.getTextRect(text, tv1.getPaint()); // 测量文本边界
            if (textRect.width() >= 45) { // 如果文本宽度大于等于45像素
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            } else { // 文本宽度小于45像素
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            }
            tv1.setVisibility(View.VISIBLE); // 设置文本可见
            tv1.setText(text); // 设置文本内容
        } else { // 文本为空
            tv1.setVisibility(View.GONE); // 隐藏文本
        }
    }

    /**
     * 设置串口参数第2行文本
     * @param text 文本内容
     */
    public void setSerialsTextLine2(String text) {
        if (!StrUtil.isEmpty(text)) { // 如果文本不为空
            text = text.replace("b/s", ""); // 移除"b/s"单位
            tv2.setVisibility(View.VISIBLE); // 设置文本可见
            tv2.setText(text); // 设置文本内容
        } else { // 文本为空
            tv2.setVisibility(View.GONE); // 隐藏文本
        }
    }

    /**
     * 设置串口参数第3行文本
     * @param text 文本内容
     */
    public void setSerialsTextLine3(String text) {
        if (!StrUtil.isEmpty(text)) { // 如果文本不为空
            text = text.replace("b/s", ""); // 移除"b/s"单位
            tv3.setVisibility(View.VISIBLE); // 设置文本可见
            tv3.setText(text); // 设置文本内容
        } else { // 文本为空
            tv3.setVisibility(View.GONE); // 隐藏文本
        }
    }

    /**
     * 获取串口参数第3行可见性
     * @return View.VISIBLE、View.INVISIBLE或View.GONE
     */
    public int getSerialsTextLine3Visible() {
        return tv3.getVisibility(); // 返回第3行文本可见性
    }

    /**
     * 获取串口参数第1行文本
     * @return 第1行文本内容
     */
    public String getSerialsTextLine1() {
        return tv1.getText().toString(); // 返回第1行文本
    }

    /**
     * 获取串口参数第2行文本
     * @return 第2行文本内容
     */
    public String getSerialsTextLine2() {
        return tv2.getText().toString(); // 返回第2行文本
    }

    /**
     * 获取串口参数第3行文本
     * @return 第3行文本内容
     */
    public String getSerialsTextLine3() {
        return tv3.getText().toString(); // 返回第3行文本
    }

    /**
     * 设置串口参数文本(4行完整版本)
     * @param text1 第1行文本内容
     * @param text2 第2行文本内容
     * @param text3 第3行文本内容
     * @param text4 第4行文本内容
     * @param color1 第1行文本颜色
     * @param color2 第2行文本颜色
     * @param color3 第3行文本颜色
     * @param color4 第4行文本颜色
     * @param line1 第1行是否显示下划线
     * @param line2 第2行是否显示下划线
     * @param line3 第3行是否显示下划线
     * @param line4 第4行是否显示下划线
     */
    public void setSerialsText(String text1, String text2, String text3, String text4
            , int color1, int color2, int color3, int color4
            , boolean line1, boolean line2, boolean line3, boolean line4) {
        if (!StrUtil.isEmpty(text1)) { // 如果第1行文本不为空
            text1 = text1.replace("b/s", ""); // 移除"b/s"单位
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            Rect textRect = Tools.getTextRect(text1, tv1.getPaint()); // 测量文本边界
            if (textRect.width() >= 45) { // 如果文本宽度大于等于45像素
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            } else { // 文本宽度小于45像素
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 设置文本大小为9像素
            }
            if (line1) { // 如果需要下划线
                tv1.setText(text1); // 设置文本内容
                tv1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置下划线和抗锯齿标志
            } else { // 不需要下划线
                tv1.setText(text1); // 设置文本内容
                tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 仅设置抗锯齿标志
            }
            tv1.setTextColor(color1); // 设置文本颜色
            tv1.setVisibility(View.VISIBLE); // 设置文本可见
        } else { // 第1行文本为空
            tv1.setVisibility(View.GONE); // 隐藏文本
        }
        if (!StrUtil.isEmpty(text2)) { // 如果第2行文本不为空
            text2 = text2.replace("b/s", ""); // 移除"b/s"单位
            if (line2) { // 如果需要下划线
                tv2.setText(text2); // 设置文本内容
                tv2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置下划线和抗锯齿标志
            } else { // 不需要下划线
                tv2.setText(text2); // 设置文本内容
                tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 仅设置抗锯齿标志
            }
            tv2.setTextColor(color2); // 设置文本颜色
            tv2.setVisibility(View.VISIBLE); // 设置文本可见
        } else { // 第2行文本为空
            tv2.setVisibility(View.GONE); // 隐藏文本
        }
        if (!StrUtil.isEmpty(text3)) { // 如果第3行文本不为空
            text3 = text3.replace("b/s", ""); // 移除"b/s"单位
            if (line3) { // 如果需要下划线
                tv3.setText(text3); // 设置文本内容
                tv3.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置下划线和抗锯齿标志
            } else { // 不需要下划线
                tv3.setText(text3); // 设置文本内容
                tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG); // 仅设置抗锯齿标志
            }
            tv3.setTextColor(color3); // 设置文本颜色
            tv3.setVisibility(View.VISIBLE); // 设置文本可见
        } else { // 第3行文本为空
            tv3.setVisibility(View.GONE); // 隐藏文本
        }
        if (!StrUtil.isEmpty(text4)) { // 如果第4行文本不为空
            text4 = text4.replace("b/s", ""); // 移除"b/s"单位
            if (line4) { // 如果需要下划线
                tv4.setText(Html.fromHtml("<u>" + text4 + "</u>")); // 使用HTML下划线标签设置文本
            } else { // 不需要下划线
                tv4.setText(text4); // 设置文本内容
            }
            tv4.setTextColor(color4); // 设置文本颜色
            tv4.setVisibility(View.VISIBLE); // 设置文本可见
        } else { // 第4行文本为空
            tv4.setVisibility(View.GONE); // 隐藏文本
        }

    }

    /**
     * 设置按钮点击事件回调监听器
     * @param onButtonClickListener 回调监听器对象
     */
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener; // 设置回调监听器
    }
}