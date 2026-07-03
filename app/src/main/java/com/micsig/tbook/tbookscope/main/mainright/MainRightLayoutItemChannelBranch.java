package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context; //
import android.content.res.TypedArray; //
import android.text.SpannableStringBuilder; //
import android.util.AttributeSet; //
import android.util.TypedValue; //
import android.view.View; //
import android.widget.Button; //
import android.widget.ImageView; //
import android.widget.LinearLayout; //
import android.widget.TextView; //

import androidx.constraintlayout.widget.ConstraintLayout; //

import com.micsig.tbook.tbookscope.R; //

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                       通道分支项布局控件                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   示波器主界面右侧面板中的通道分支项布局控件，负责单个通道项的UI展示与交互      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 显示通道基本信息（通道号、选中状态）                                     │
 * │   2. 显示通道参数（反相、带宽、耦合、阻抗、探头详情、偏移）                    │
 * │   3. 根据通道类型设置不同的颜色主题和背景样式                                 │
 * │   4. 处理通道项的点击事件                                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   继承自LinearLayout的自定义控件，采用组合模式：                              │
 * │   - 通过XML属性配置初始状态                                                  │
 * │   - 内部持有多个TextView和ImageView用于显示各类参数                          │
 * │   - 通过setChecked方法控制展开/折叠状态                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   外部调用setter方法 → 更新内部控件状态 → 刷新UI显示                          │
 * │   用户点击按钮 → 触发OnClickListener回调 → 通知父容器处理                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   - 依赖layout_mainright_channel_branch.xml布局文件                          │
 * │   - 依赖R.styleable.MainRightLayoutItemChannelBranch自定义属性               │
 * │   - 依赖R.color和R.drawable中的通道颜色和背景资源                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用场景】                                                                 │
 * │   在MainRightLayout等父容器中使用，用于显示示波器的各个通道项：               │
 * │   - CH1~CH4：物理通道1-4                                                    │
 * │   - M1：数学运算通道                                                         │
 * │   - R1：参考波形通道                                                         │
 * │   - S1/S2：串口解码通道                                                      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainRightLayoutItemChannelBranch extends LinearLayout {
    private Context context; // 上下文对象，用于获取资源和inflate布局
    private boolean checked = true; // 通道项的选中状态，默认为true（选中）
    private String text; // 通道标识文本，如"1"、"2"、"M1"、"R1"、"S1"等

    private ConstraintLayout bgBranch; // 通道项背景容器，用于设置选中/未选中背景
    private ConstraintLayout layoutBranch; // 通道详细信息布局容器，选中时显示
    private TextView tvChannelText; // 通道编号文本视图（如"CH1"）
    private TextView tvInvert; // 反相标识文本视图
    private TextView tvBandWidth; // 带宽文本视图
    private ImageView tvCouple; // 耦合方式图标视图
    private TextView tvImped; // 阻抗文本视图
    private TextView tvProbeDetailText; // 探头详情文本视图
    private TextView tvOffset; // 偏移值文本视图
    private TextView textBranch; // 折叠时显示的通道标识文本视图
    private OnClickListener onClickListener; // 点击事件监听器
    private boolean fromExternalKey; // 标识是否来自外部按键触发

    /**
     * 设置点击事件监听器
     * 
     * @param onClickListener 点击事件回调接口
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener; // 保存点击监听器引用
    }

    /**
     * 判断是否来自外部按键触发
     * 
     * @return true表示来自外部按键，false表示非外部按键触发
     */
    public boolean isFromExternalKey() {
        return fromExternalKey; // 返回外部按键标识
    }

    /**
     * 设置是否来自外部按键触发
     * 
     * @param fromExternalKey true表示来自外部按键，false表示非外部按键
     */
    public void setFromExternalKey(boolean fromExternalKey) {
        this.fromExternalKey = fromExternalKey; // 设置外部按键标识
    }

    /**
     * 单参数构造函数
     * 
     * @param context 上下文对象
     */
    public MainRightLayoutItemChannelBranch(Context context) {
        this(context, null); // 调用双参数构造函数，attrs传null
    }

    /**
     * 双参数构造函数
     * 
     * @param context 上下文对象
     * @param attrs 属性集，从XML布局中读取
     */
    public MainRightLayoutItemChannelBranch(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造函数，defStyleAttr传0
    }

    /**
     * 三参数构造函数（完整构造函数）
     * 
     * @param context 上下文对象
     * @param attrs 属性集，从XML布局中读取
     * @param defStyleAttr 默认样式属性
     */
    public MainRightLayoutItemChannelBranch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造函数
        init(context, attrs, defStyleAttr); // 初始化控件
    }

    /**
     * 初始化控件
     * 
     * @param context 上下文对象
     * @param attrs 属性集，从XML布局中读取
     * @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context; // 保存上下文引用
        inflate(context, R.layout.layout_mainright_channel_branch, this); // 加载布局文件到当前控件
        text = "1"; // 默认通道标识为"1"
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemChannelBranch); // 获取自定义属性数组
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemChannelBranch_checked, true); // 读取checked属性，默认true
        text = ta.getString(R.styleable.MainRightLayoutItemChannelBranch_text); // 读取text属性

        ta.recycle(); // 回收TypedArray资源

        initView(); // 初始化视图控件
        setChecked(checked); // 设置初始选中状态
    }

    /**
     * 初始化视图控件
     * 获取布局中各个控件的引用并设置初始状态
     */
    private void initView() {
        setBackgroundColor(getResources().getColor(R.color.bg_main_outside)); // 设置控件背景色

        bgBranch = (ConstraintLayout) findViewById(R.id.bgBranch); // 获取背景容器
        layoutBranch = (ConstraintLayout) findViewById(R.id.layoutBranch); // 获取详细信息布局容器
        tvChannelText = (TextView) findViewById(R.id.channelText); // 获取通道编号文本
        tvInvert = (TextView) findViewById(R.id.invert); // 获取反相标识文本
        tvBandWidth = (TextView) findViewById(R.id.bandWidth); // 获取带宽文本
        tvCouple = (ImageView) findViewById(R.id.couple); // 获取耦合方式图标
        tvImped = (TextView) findViewById(R.id.Imped); // 获取阻抗文本
        tvProbeDetailText = (TextView) findViewById(R.id.probeDetailText); // 获取探头详情文本
        tvOffset = (TextView) findViewById(R.id.offset); // 获取偏移值文本
        textBranch = (TextView) findViewById(R.id.textBranch); // 获取折叠状态文本

        ((Button) findViewById(R.id.btnBranch)).setOnClickListener(new OnClickListener() { // 为按钮设置点击监听器
            @Override
            public void onClick(View v) {
                onClickListener.onClick(MainRightLayoutItemChannelBranch.this); // 触发外部注册的点击事件
            }
        });

        tvChannelText.setText(text); // 设置通道编号文本
        textBranch.setText(text); // 设置折叠状态文本
        tvInvert.setVisibility(INVISIBLE); // 默认隐藏反相标识
        tvBandWidth.setText(""); // 清空带宽文本
        tvCouple.setImageResource(0); // 清空耦合图标
        tvImped.setText(""); // 清空阻抗文本
        tvProbeDetailText.setText(""); // 清空探头详情文本
        tvOffset.setText(""); // 清空偏移值文本

        setBackground(); // 根据通道类型设置背景样式
    }

    /**
     * 根据通道类型和选中状态设置背景样式和文本颜色
     * 不同通道类型使用不同的主题颜色：
     * - CH1：黄色系
     * - CH2/CH3：蓝色系
     * - CH4：绿色系
     * - M1：紫色系
     * - R1：红色系
     * - S1/S2：灰色系
     */
    private void setBackground() {
        if ("1".equals(text)) { // 通道1
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch1)); // 设置通道编号颜色（黄色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch1)); // 设置折叠文本颜色（黄色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_have); // 设置选中背景
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_nothing); // 设置未选中背景
            }
        } else if ("2".equals(text)) { // 通道2
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch2)); // 设置通道编号颜色（蓝色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch2)); // 设置折叠文本颜色（蓝色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have); // 设置选中背景
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing); // 设置未选中背景
            }
        } else if ("3".equals(text)) { // 通道3
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch3)); // 设置通道编号颜色（蓝色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch3)); // 设置折叠文本颜色（蓝色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have); // 设置选中背景（与CH2相同）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing); // 设置未选中背景（与CH2相同）
            }
        } else if ("4".equals(text)) { // 通道4
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Ch4)); // 设置通道编号颜色（绿色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Ch4)); // 设置折叠文本颜色（绿色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_have); // 设置选中背景
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_nothing); // 设置未选中背景
            }
        } else if ("M1".equals(text)) { // 数学运算通道
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Math)); // 设置通道编号颜色（紫色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Math)); // 设置折叠文本颜色（紫色）
            textBranch.setText("MATH"); // 设置显示文本为"MATH"
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_have); // 设置选中背景（使用CH1样式）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch1_nothing); // 设置未选中背景（使用CH1样式）
            }
        } else if ("R1".equals(text)) { // 参考波形通道
            tvChannelText.setTextColor(getResources().getColor(R.color.color_R1)); // 设置通道编号颜色（红色）
            textBranch.setTextColor(getResources().getColor(R.color.color_R1)); // 设置折叠文本颜色（红色）
            textBranch.setText("REF"); // 设置显示文本为"REF"
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have); // 设置选中背景（使用CH2样式）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing); // 设置未选中背景（使用CH2样式）
            }
        } else if ("S1".equals(text)) { // 串口解码通道1
            tvChannelText.setTextColor(getResources().getColor(R.color.color_S1)); // 设置通道编号颜色（灰色）
            textBranch.setTextColor(getResources().getColor(R.color.color_S1)); // 设置折叠文本颜色（灰色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have); // 设置选中背景（使用CH2样式）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing); // 设置未选中背景（使用CH2样式）
            }
        } else if ("S2".equals(text)) { // 串口解码通道2
            tvChannelText.setTextColor(getResources().getColor(R.color.color_S2)); // 设置通道编号颜色（灰色）
            textBranch.setTextColor(getResources().getColor(R.color.color_S2)); // 设置折叠文本颜色（灰色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_have); // 设置选中背景（使用CH4样式）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch4_nothing); // 设置未选中背景（使用CH4样式）
            }
        } else { // 其他类型通道
            tvChannelText.setTextColor(getResources().getColor(R.color.color_Math)); // 设置通道编号颜色（紫色）
            textBranch.setTextColor(getResources().getColor(R.color.color_Math)); // 设置折叠文本颜色（紫色）
            if (checked) { // 选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_have); // 设置选中背景（使用CH2样式）
            } else { // 未选中状态
                bgBranch.setBackgroundResource(R.drawable.ic_rectangle_5_data_ch2_3_nothing); // 设置未选中背景（使用CH2样式）
            }
        }
    }

    /**
     * 模拟点击操作
     * 外部调用此方法可触发点击事件
     */
    public void clickOnClick() {
        onClickListener.onClick(MainRightLayoutItemChannelBranch.this); // 直接触发点击监听器
    }

    /**
     * 获取通道标识文本
     * 
     * @return 通道标识文本，如"1"、"2"、"M1"、"R1"、"S1"等
     */
    public String getText() {
        return text; // 返回通道标识
    }

    /**
     * 设置选中状态
     * 选中时展开显示详细信息，未选中时折叠显示通道标识
     * 
     * @param checked true表示选中，false表示未选中
     */
    public void setChecked(boolean checked) {
        this.checked = checked; // 保存选中状态
        if (checked) { // 选中状态
            layoutBranch.setVisibility(VISIBLE); // 显示详细信息布局
            textBranch.setVisibility(INVISIBLE); // 隐藏折叠文本
        } else { // 未选中状态
            layoutBranch.setVisibility(INVISIBLE); // 隐藏详细信息布局
            textBranch.setVisibility(VISIBLE); // 显示折叠文本
        }
        setBackground(); // 更新背景样式
    }

    /**
     * 获取选中状态
     * 
     * @return true表示选中，false表示未选中
     */
    public boolean isChecked() {
        return checked; // 返回选中状态
    }

    /**
     * 设置反相标识显示状态
     * 
     * @param invert true显示反相标识，false隐藏
     */
    public void setInvert(boolean invert) {
        tvInvert.setVisibility(invert ? VISIBLE : INVISIBLE); // 根据参数设置反相标识可见性
    }

    /**
     * 设置带宽文本
     * 
     * @param bandWidth 带宽值字符串
     */
    public void setBandWidth(String bandWidth) {
        tvBandWidth.setText(bandWidth); // 设置带宽文本
    }

    /**
     * 设置耦合方式图标资源
     * 
     * @param coupleResId 耦合图标资源ID
     */
    public void setCoupleResId(int coupleResId) {
        tvCouple.setImageResource(coupleResId); // 设置耦合方式图标
    }

    /**
     * 设置阻抗文本
     * 
     * @param imped 阻抗值字符串
     */
    public void setImped(String imped) {
        tvImped.setText(imped); // 设置阻抗文本
    }

    /**
     * 设置探头详情文本
     * 
     * @param probeDetailText 探头详情字符串
     */
    public void setProbeDetail(String probeDetailText) {
        tvProbeDetailText.setText(probeDetailText); // 设置探头详情文本
    }

    /**
     * 设置偏移值文本
     * 
     * @param offset 偏移值字符串
     */
    public void setOffset(String offset) {
        tvOffset.setText(offset); // 设置偏移值文本
    }


    /**
     * 设置数学/参考通道的偏移值（时基）
     * 
     * @param timebase 时基值字符串
     */
    public void setMathRefOffset(String timebase) {
        tvOffset.setText(timebase); // 设置时基文本到偏移位置
    }

    /**
     * 设置数学/参考通道的消息文本
     * 
     * @param msg 消息内容字符串
     * @param textSmall true使用小字体，false使用正常字体
     */
    public void setMathRefMsg(String msg, boolean textSmall) {
        setMathRefMsgTextSize(textSmall); // 设置文本大小
        tvImped.setText(msg); // 设置消息文本到阻抗位置
    }

    /**
     * 设置数学/参考通道的消息文本（支持富文本）
     * 
     * @param msg SpannableStringBuilder格式的消息内容
     * @param textSmall true使用小字体，false使用正常字体
     */
    public void setMathRefMsg(SpannableStringBuilder msg, boolean textSmall) {
        setMathRefMsgTextSize(textSmall); // 设置文本大小
        tvImped.setText(msg); // 设置富文本消息到阻抗位置
    }

    /**
     * 设置数学/参考通道消息的文本大小
     * 
     * @param textSmall true使用小字体（9px，最多3行），false使用正常字体（14px/13px，单行）
     */
    private void setMathRefMsgTextSize(boolean textSmall) {
        if (textSmall) { // 小字体模式
            tvImped.setMaxLines(3); // 阻抗文本最多显示3行
            tvImped.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 阻抗文本字号9像素
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 9); // 偏移文本字号9像素
        } else { // 正常字体模式
            tvImped.setMaxLines(1); // 阻抗文本最多显示1行
            tvImped.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14); // 阻抗文本字号14像素
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13); // 偏移文本字号13像素
        }
    }

    /**
     * 设置串口通道的标题文本
     * 
     * @param title 标题字符串
     */
    public void setSerialsTitle(String title) {
        tvBandWidth.setText(title); // 设置标题到带宽位置
    }

    /**
     * 设置串口通道的中间消息文本
     * 
     * @param msg 消息字符串
     */
    public void setSerialsMsgMiddle(String msg) {
        tvImped.setText(msg); // 设置消息到阻抗位置
    }

    /**
     * 设置串口通道的底部消息文本
     * 根据通道类型和协议类型自动调整字体大小
     * 
     * @param msg 消息字符串
     */
    public void setSerialsMsgBottom(String msg) {
        if (("S1".equals(text) || "S2".equals(text)) && "UART".contentEquals(tvBandWidth.getText())) { // S1/S2通道且协议为UART
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12); // 使用12像素字号
        } else { // 其他情况
            tvOffset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13); // 使用13像素字号
        }
        tvOffset.setText(msg); // 设置底部消息文本
    }

    /**
     * 设置串口通道的中间消息文本（支持富文本）
     * 
     * @param msg SpannableStringBuilder格式的消息内容
     */
    public void setSerialsMsgMiddle(SpannableStringBuilder msg) {
        tvImped.setText(msg); // 设置富文本消息到阻抗位置
    }

    /**
     * 设置串口通道的底部消息文本（支持富文本）
     * 
     * @param msg SpannableStringBuilder格式的消息内容
     */
    public void setSerialsMsgBottom(SpannableStringBuilder msg) {
        tvOffset.setText(msg); // 设置富文本消息到偏移位置
    }
}