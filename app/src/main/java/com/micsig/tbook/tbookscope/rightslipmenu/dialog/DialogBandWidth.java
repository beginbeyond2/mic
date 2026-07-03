package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.util.TBookUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/4/24.
 */

/*
 * +----------------------------------------------------------------------+
 * |                        DialogBandWidth                               |
 * |                        带宽设置对话框                                  |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供示波器通道带宽参数的交互式设置界面，                           |
 * |          支持MHz/KHz单位切换、整数/小数部分分别调节                        |
 * | 架构设计: 继承AbsoluteLayout的自定义对话框视图，                          |
 * |          通过SeekBar+RadioButton组合实现带宽值的粗调与微调                 |
 * | 数据流向: 外部调用setData()传入当前值 -> 用户交互修改 ->                   |
 * |          OnDismissListener回调返回修改后的带宽字符串                      |
 * | 依赖关系: RxBus(对话框开关事件), PlaySound(按键音效),                    |
 * |          TBookUtil(单位常量/格式化), Tools(布局调试)                     |
 * | 使用场景: 通道带宽设置菜单项点击后弹出，用户选择/调整带宽值后关闭            |
 * +----------------------------------------------------------------------+
 */
public class DialogBandWidth extends AbsoluteLayout {
    public static final String UNIT_MHZ = TBookUtil.UNIT_MHZ;  // MHz单位常量
    public static final String UNIT_KHZ = TBookUtil.UNIT_KHZ;  // KHz单位常量

    private int integerPart;   // 带宽值的整数部分（MHz）
    private int decimalPart;   // 带宽值的小数部分（KHz，0-999）
    private String curUnit;    // 当前选中的单位（MHz或KHz）

    private Context context;   // 上下文引用
    private TextView title, show;  // title:对话框标题, show:当前带宽值显示
    private RadioButton mhz, khz;  // MHz/KHz单位选择单选按钮
    private Button subtract, add;   // 减少按钮 / 增加按钮
    private SeekBar seekBar;        // 滑动条，用于调节带宽值
    private OnDismissListener onDismissListener;  // 对话框关闭回调监听器
    private boolean isFromUser;     // 标记进度变化是否由用户操作触发

    private ViewGroup rootViewGroup;  // 根视图组，用于布局调试

    /**
     * 对话框关闭监听接口。
     * <p>
     * 当对话框关闭时触发onDismiss回调返回结果，
     * 当带宽切换变化时触发bandSwitchChange回调通知实时变化。
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回最终带宽字符串 */
        void onDismiss(String result);
        /** 带宽值实时变化时回调，通知外部更新UI */
        void bandSwitchChange(String fb);
    }

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogBandWidth(Context context) {
        this(context, null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogBandWidth(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并调用init()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogBandWidth(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;               // 保存上下文引用
        init();                               // 执行初始化
    }

    //[50, 387]	608	125

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_bandwidth布局，设置外部区域点击关闭监听，
     * 初始化内部视图控件，然后隐藏对话框。
     */
    private void init() {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_bandwidth, this);  // 填充布局

        findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onDismissListener != null) {  // 如果设置了关闭监听器
                    String showString;
                    if (integerPart >= 1) {  // 整数部分>=1，直接拼接显示字符串
                        showString = getShowString();  // 获取格式化后的显示字符串
                    } else {  // 整数部分<1，小数部分最小30KHz
                        decimalPart = Math.max(decimalPart, 30);  // 确保小数部分不低于30
                        showString = decimalPart + UNIT_KHZ;  // 拼接小数部分+KHz单位
                    }
                    onDismissListener.onDismiss(showString);  // 回调关闭监听，传递结果
                }
                hide();  // 隐藏对话框
                return false;  // 不消费触摸事件
            }
        });
        initView(rootViewGroup);  // 初始化内部视图控件
        hide();  // 默认隐藏对话框
    }

    /**
     * 初始化内部视图控件，绑定ID和事件监听器。
     *
     * @param view 根视图，用于查找子控件
     */
    private void initView(View view) {
        title = (TextView) view.findViewById(R.id.title);         // 对话框标题文本
        show = (TextView) view.findViewById(R.id.show);           // 带宽值显示文本
        mhz = (RadioButton) view.findViewById(R.id.mhz);         // MHz单选按钮
        khz = (RadioButton) view.findViewById(R.id.khz);         // KHz单选按钮
        subtract = (Button) view.findViewById(R.id.subtract);    // 减少按钮
        add = (Button) view.findViewById(R.id.add);              // 增加按钮
        seekBar = (SeekBar) view.findViewById(R.id.progress);    // 滑动条

        mhz.setOnClickListener(onCheckedListener);   // MHz按钮点击监听
        khz.setOnClickListener(onCheckedListener);   // KHz按钮点击监听
        subtract.setOnClickListener(onCheckedListener);  // 减少按钮点击监听
        add.setOnClickListener(onCheckedListener);       // 增加按钮点击监听
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);  // 滑动条变化监听
    }

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BANDWIDTH);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogBandWidth",rootViewGroup);  // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BANDWIDTH);  // 发送对话框关闭事件
    }

    /**
     * 设置SeekBar的进度值。
     *
     * @param i 进度值
     */
    public void setProgress(int i) {
        isFromUser = true;  // 标记为用户操作触发
        seekBar.setProgress(i);  // 设置滑动条进度
    }

    /**
     * 获取SeekBar当前的进度值。
     *
     * @return 当前进度值
     */
    public int getProgress() {
        return seekBar.getProgress();  // 返回滑动条当前进度
    }

    /**
     * 设置对话框数据并显示。
     * <p>
     * 解析传入的带宽字符串，分离整数和小数部分，
     * 根据当前单位设置SeekBar的范围和进度，然后显示对话框。
     *
     * @param titleString      对话框标题
     * @param numberString     当前带宽值字符串（如"100MHz"或"500KHz"）
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(String titleString, String numberString, OnDismissListener onDismissListener) {
        if (numberString.endsWith(UNIT_KHZ)) {  // 如果以KHz结尾
            curUnit = UNIT_MHZ;  // 当前单位设为MHz（KHz视为MHz的小数部分）
            integerPart = 0;  // 整数部分为0
            decimalPart = Integer.valueOf(numberString.replace(UNIT_KHZ, ""));  // 提取小数部分数值
        } else if (numberString.endsWith(UNIT_MHZ)) {  // 如果以MHz结尾
            double curNumber = Double.valueOf(numberString.replace(UNIT_MHZ, ""));  // 解析完整数值
            curUnit = UNIT_MHZ;  // 当前单位设为MHz
            integerPart = (int) curNumber;  // 提取整数部分
            decimalPart = (int) (curNumber * 1000 % 1000);  // 提取小数部分（转换为KHz）
        } else {  // 其他情况（无法解析）
            curUnit = UNIT_MHZ;  // 默认MHz
            integerPart = 0;  // 整数部分为0
            decimalPart = 0;  // 小数部分为0
        }
        this.onDismissListener = onDismissListener;  // 保存关闭监听器
        title.setText(titleString);  // 设置标题文本
        show.setText(getShowString());  // 设置显示值
        isFromUser = false;  // 标记非用户操作
        if (mhz.isChecked()) {  // 如果当前选中MHz
            seekBar.setMax(100);  // 整数部分最大100
            seekBar.setProgress(integerPart);  // 设置整数部分进度
        } else if (khz.isChecked()) {  // 如果当前选中KHz
            seekBar.setMax(1000);  // 小数部分最大1000
            seekBar.setProgress(decimalPart);  // 设置小数部分进度
        }
        show();  // 显示对话框
    }

    /**
     * 根据整数部分和小数部分生成格式化的显示字符串。
     * <p>
     * 将整数和小数部分合并为带3位小数的浮点数，
     * 上限100，格式化为"X.XXXMHz"形式。
     *
     * @return 格式化的带宽显示字符串
     */
    private String getShowString() {
        double curNumber = integerPart + decimalPart * 1.0 / 1000;  // 合并整数和小数部分为浮点数
        curNumber = Math.min(curNumber, 100);  // 限制最大值为100
        DecimalFormat df = new DecimalFormat("###0.000", new DecimalFormatSymbols(Locale.CHINA));  // 创建3位小数格式化器
        return TBookUtil.getDMinus0(Double.parseDouble(df.format(curNumber))) + curUnit;  // 格式化并拼接单位
    }

    /**
     * 单选按钮和加减按钮的点击事件监听器。
     * <p>
     * 处理MHz/KHz单位切换以及增加/减少带宽值的逻辑。
     * 单位切换时会同步更新SeekBar的范围和进度。
     */
    private View.OnClickListener onCheckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();  // 播放按键音效
            if (mhz.isChecked() && khz.getId() == v.getId()) {  // 当前MHz，点击KHz -> 切换到KHz
                khz.setChecked(true);  // 选中KHz
                mhz.setChecked(false);  // 取消选中MHz
                isFromUser = false;  // 标记非用户拖动
                seekBar.setMax(1000);  // 切换到小数部分，范围0-1000
                seekBar.setProgress(decimalPart);  // 设置小数部分进度
            } else if (khz.isChecked() && mhz.getId() == v.getId()) {  // 当前KHz，点击MHz -> 切换到MHz
                mhz.setChecked(true);  // 选中MHz
                khz.setChecked(false);  // 取消选中KHz
                isFromUser = false;  // 标记非用户拖动
                seekBar.setMax(100);  // 切换到整数部分，范围0-100
                seekBar.setProgress(integerPart);  // 设置整数部分进度
            } else if (add.getId() == v.getId()) {  // 点击增加按钮
                isFromUser = true;  // 标记为用户操作
                seekBar.setProgress(seekBar.getProgress() + 1);  // 进度+1
            } else if (subtract.getId() == v.getId()) {  // 点击减少按钮
                isFromUser = true;  // 标记为用户操作
                seekBar.setProgress(seekBar.getProgress() - 1);  // 进度-1
            }
        }
    };

    /**
     * SeekBar进度变化监听器。
     * <p>
     * 当进度变化时，根据当前选中的单位更新整数或小数部分，
     * 同时确保带宽值不低于30KHz的下限。
     * 每次变化都会实时通知bandSwitchChange回调。
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isFromUser || fromUser) {  // 由用户操作触发的进度变化
                if (mhz.isChecked()) {  // 当前MHz模式
                    integerPart = progress;  // 更新整数部分
                } else if (khz.isChecked()) {  // 当前KHz模式
                    decimalPart = progress;  // 更新小数部分
                }
            }

            if (integerPart < 1 && decimalPart < 30) {  // 整数<1且小数<30，低于最小值
                decimalPart = Math.max(decimalPart, 30);  // 强制小数部分最小为30
                seekBar.setProgress(30);  // 设置进度为30
            }
            String str=getShowString();  // 获取格式化显示字符串
            onDismissListener.bandSwitchChange(str);  // 实时通知带宽变化
            show.setText(str);  // 更新显示文本
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 开始拖动滑动条时的回调（未使用）
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 停止拖动滑动条时的回调（未使用）
        }
    };
}
