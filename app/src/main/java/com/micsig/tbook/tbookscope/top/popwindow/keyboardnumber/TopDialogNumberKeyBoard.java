package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber; // 数字键盘对话框所在包

import android.content.Context; // 上下文类
import android.text.Editable; // 可编辑文本类
import android.text.InputFilter; // 输入过滤器
import android.text.InputType; // 输入类型常量
import android.text.TextWatcher; // 文本变化监听器
import android.util.AttributeSet; // 属性集类
import android.view.ActionMode; // 操作模式类
import android.view.KeyEvent; // 按键事件类
import android.view.Menu; // 菜单类
import android.view.MenuItem; // 菜单项类
import android.view.MotionEvent; // 触摸事件类
import android.view.View; // 视图基类
import android.view.ViewGroup; // 视图组基类
import android.widget.AbsoluteLayout; // 绝对布局类
import android.widget.Button; // 按钮类
import android.widget.EditText; // 编辑框类
import android.widget.LinearLayout; // 线性布局类
import android.widget.RadioButton; // 单选按钮类
import android.widget.RelativeLayout; // 相对布局类

import com.micsig.base.keyevent.KeyEventUtil; // 按键事件工具类
import com.micsig.base.Logger; // 日志工具类
import com.micsig.base.filter.FilterFactory; // 过滤器工厂类
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图组类
import com.micsig.tbook.tbookscope.R; // 资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxBus事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 播放声音工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类

import java.util.ArrayList; // 动态数组类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                       TopDialogNumberKeyBoard                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：数字键盘对话框，支持多进制输入和波特率输入的虚拟键盘               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                 │
 * │   1. 多进制输入：二进制/四进制/八进制/十进制/十六进制，含扩展X模式            │
 * │   2. 浮点数输入：带最小/最大值范围限制和小数位精度控制                        │
 * │   3. 波特率输入：b/s/kb/s/Mb/s三种单位互斥切换                              │
 * │   4. 字母映射：changeLetter模式下ABCDEF映射为12/16/20/24/32/48/64           │
 * │   5. 自动格式化：根据进制类型自动插入空格分组、限制输入位数                   │
 * │   6. 输入过滤：通过正则表达式和InputFilter限制非法字符输入                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                 │
 * │   继承AbsoluteLayout，实现IDigits和IKeyBoardNumber接口                      │
 * │   通过RxBus发送对话框打开/关闭事件，与MainViewGroup协同管理对话框状态         │
 * │   使用TextWatcher实现实时格式化，使用InputFilter实现输入验证                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流：                                                                   │
 * │   调用方 → setDecimalData/setFloatData/setBaudRateData → 显示键盘           │
 * │   用户点击按键 → onItemClickListener → addText/delText → TextWatcher格式化  │
 * │   点击确认 → OnDismissListener.onDismiss → 返回结果给调用方                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖：IDigits、IKeyBoardNumber、KeyBoardNumberUtil、FilterFactory、RxBus     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：示波器参数设置中需要数字输入的各种场景（通道参数、触发参数等）       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopDialogNumberKeyBoard extends AbsoluteLayout implements IDigits, IKeyBoardNumber { // 继承绝对布局，实现进制常量和按键索引接口
    private static final String TAG = "TopDialogNumberKeyBoard"; // 日志标签
    public static final String KEYBOARD_BS = "b/s"; // 波特率单位：比特每秒
    public static final String KEYBOARD_KBS = "kb/s"; // 波特率单位：千比特每秒
    public static final String KEYBOARD_MBS = "Mb/s"; // 波特率单位：兆比特每秒
    private final int BITS_FOR_BAUDRATE = 20;//波特率长度限制 // 波特率模式下的最大输入位数

    private Context context; // 上下文对象
    private LinearLayout showLayout; // 显示区域布局
    private EditText tvShow; // 输入显示框
    private RelativeLayout gridView; // 按键网格布局
    private ArrayList<Button> list; // 按键按钮列表（按索引排序）
    private OnDismissListener onDismissListener; // 确认关闭回调
    private OnOriginalDismissListener onOriginalDismissListener; // 保留原始数据的确认关闭回调
    private int digits; // 当前进制类型
    private int bits = -1;//输入的限制位数，进制模式下使用，-1为不限制 // 输入位数限制
    private String bs; // 当前波特率单位
    private int minBaudRate, maxBaudRate;//输入的限制大小，波特率模式使用 // 波特率最小/最大值
    private double minFloat, maxFloat;//输入的限制大小，float模式使用 // 浮点数最小/最大值
    private boolean first = false;//是否是本次打开后第一点击 // 首次点击标志
    private boolean changeLetter = false;//ABCDEF六个字母是否需要变成十进制显示和使用 // 字母映射模式标志
    private boolean isFormatting = false; // 格式化进行中标志，防止递归

    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 确认关闭回调接口
     */
    public interface OnDismissListener { // 确认关闭回调接口
        /**
         * @param result 用户确认后的输入结果字符串
         */
        void onDismiss(String result); // 确认关闭时回调，返回格式化后的结果
    }

    /**
     * 保留原始数据的确认关闭回调接口
     */
    public interface OnOriginalDismissListener {//保留相对原始的输入数据，只补齐偶数位的0。 // 保留原始输入数据的回调接口
        /**
         * @param result 格式化后的结果字符串
         * @param originalResult 保留原始格式的结果字符串（仅补齐偶数位0）
         */
        void onOriginalDismiss(String result, String originalResult); // 确认关闭时回调，返回格式化结果和原始结果
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public TopDialogNumberKeyBoard(Context context) { // 单参数构造方法
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs 属性集
     */
    public TopDialogNumberKeyBoard(Context context, AttributeSet attrs) { // 双参数构造方法
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造方法
     * @param context 上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogNumberKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    //[0, 240]	608	360 // 布局位置注释
    /**
     * 初始化键盘视图
     */
    private void init() { // 初始化方法
        setClickable(true); // 设置可点击
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_keyboard_number, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v) -> { // 点击外部区域
            hide(); // 隐藏键盘
            RxBus.getInstance().post(RxEnum.MSG_HIDE_INPUT_BACKGROUND, false); // 发送隐藏输入背景事件
        });
        initView(rootViewGroup); // 初始化视图
        hide(); // 初始状态隐藏
    }

    /**
     * 初始化视图控件和事件监听
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图控件
        showLayout = (LinearLayout) view.findViewById(R.id.showKeyBoardLayout); // 获取显示区域布局
        tvShow = (EditText) view.findViewById(R.id.show); // 获取输入显示框
        tvShow.setShowSoftInputOnFocus(false); // 禁止系统软键盘弹出
        tvShow.setLongClickable(false); // 禁止长按
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD); // 设置密码输入类型（隐藏光标闪烁）
        tvShow.setCustomSelectionActionModeCallback(new ActionMode.Callback() { // 禁用文本选择操作菜单
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) { // 创建操作模式
                return false; // 返回false禁止创建
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { // 准备操作模式
                return false; // 返回false禁止准备
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) { // 点击操作项
                return false; // 返回false不处理
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) { // 销毁操作模式
            }
        });

        tvShow.setOnKeyListener(new OnKeyListener() { // 设置物理按键监听
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) { // 按键回调
                if (event.getAction() == KeyEvent.ACTION_DOWN) { // 只处理按下事件
                    if (first) { // 如果是首次点击
                        first = false; // 重置标志
                        tvShow.setText(""); // 清空输入框
                    }
                    if (KeyEventUtil.isConfirmKey(event)) { // 确认键
                        if (onItemClickListener != null) { // 监听器非空
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_ENTER)); // 模拟点击确认键
                        }
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isBackKey(event)) { // 退格键
                        if (onItemClickListener != null) { // 监听器非空
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_ZUO)); // 模拟点击退格键
                        }
                        return true; // 消费事件
                    }
                }
                return false; // 不消费其他事件
            }
        });

        tvShow.addTextChangedListener(new TextWatcher() { // 添加文本变化监听器
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 文本变化前
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // 文本变化中

            }

            @Override
            public void afterTextChanged(Editable s) { // 文本变化后
                if (isFormatting) return; // 如果正在格式化则跳过，防止递归
                isFormatting = true; // 设置格式化标志
                int interval = KeyBoardNumberUtil.getInterval(digits); // 获取当前进制的空格间隔
                int num = bits / interval; // 计算完整分组的数量
                int remain = bits % interval; // 计算余数位数
                int finalBits = num * (interval + 1) + remain; // 计算含空格的总长度
                if (digits == IDigits.DIGITS_10) { // 十进制不需要空格分组
                    finalBits = bits; // 直接使用位数
                }
                String str = s.toString(); // 获取当前文本
                if (str.length() > finalBits && finalBits > 0) { // 超过最大长度
                    str = str.substring(0, finalBits); // 截取到最大长度
                }
                String finalText = KeyBoardNumberUtil.reCalculateSpace(str, digits); // 重新计算空格

                if (!s.toString().equals(finalText)) { // 如果文本有变化
                    s = Editable.Factory.getInstance().newEditable(finalText); // 创建新的可编辑文本
                    int selection = finalText.length(); // 光标位置设为末尾
                    if (selection > finalBits && finalBits > 0) { // 如果光标超过最大长度
                        selection = finalBits; // 限制光标位置
                    }
                    tvShow.setText(finalText); // 设置格式化后的文本
                    tvShow.setSelection(selection); // 设置光标位置
                }
                isFormatting = false; // 重置格式化标志

            }
        });

        gridView = (RelativeLayout) view.findViewById(R.id.gridView); // 获取按键网格布局

        list = new ArrayList<Button>(); // 初始化按键列表
        for (int i = 0; i < gridView.getChildCount(); i++) { // 遍历网格中的所有子视图
            gridView.getChildAt(i).setOnClickListener(onItemClickListener); // 设置点击监听
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ZUO) { // 退格键
                gridView.getChildAt(i).setOnLongClickListener(onLongClickListener); // 设置长按监听（长按清空）
            }
            for (int j = 0; j < gridView.getChildCount(); j++) { // 遍历所有子视图以按索引排序
                if ((Integer.parseInt((String) gridView.getChildAt(j).getTag())) == i) { // 找到tag等于当前索引的按钮
                    list.add((Button) gridView.getChildAt(i)); // 添加到列表
                    break; // 跳出内层循环
                }
            }
        }
    }

    /**
     * 显示键盘
     */
    public void show() { // 显示键盘
//        moveViewToPosition(showLayout, 0, ViewUtils.getDialogNumberOffset()); // 移动到指定位置（已注释）
        setVisibility(VISIBLE); // 设置可见
        tvShow.setSelection(tvShow.getText().length()); // 光标移到末尾
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_NUMBERKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogNumberKeyBoard",rootViewGroup); // 打印控件位置信息
    }

    /**
     * 隐藏键盘
     */
    public void hide() { // 隐藏键盘
        tvShow.setText(""); // 清空输入框
        tvShow.setSelection(0); // 光标移到开头
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_NUMBERKEYBOARD); // 发送对话框关闭事件
    }

    /**
     * 设置进制数据参数（无前置文本）
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param onDismissListener 确认关闭回调
     */
    public void setDecimalData(int bits, int digits, OnDismissListener onDismissListener) { // 设置进制数据（无前置文本）
        setDecimalData(null, bits, digits, onDismissListener); // 委托给完整方法
    }

    /**
     * 设置进制数据参数（带字母映射）
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param changeLetter 是否启用字母映射
     * @param onDismissListener 确认关闭回调
     */
    public void setDecimalData(int bits, int digits, boolean changeLetter, OnDismissListener onDismissListener) { // 设置进制数据（带字母映射）
        setDecimalData(null, bits, digits, changeLetter, false, onDismissListener); // 委托给完整方法
    }

    /**
     * 设置进制数据参数（有前置文本）
     * @param preText 前置文本
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param onDismissListener 确认关闭回调
     */
    public void setDecimalData(String preText, int bits, int digits, OnDismissListener onDismissListener) { // 设置进制数据（有前置文本）
        setDecimalData(preText, bits, digits, false, false, onDismissListener); // 委托给完整方法
    }

    /**
     * 设置进制数据参数（有前置文本和右对齐）
     * @param preText 前置文本
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param isRight 是否右对齐
     * @param onDismissListener 确认关闭回调
     */
    public void setDecimalData(String preText, int bits, int digits, boolean isRight, OnDismissListener onDismissListener) { // 设置进制数据（有前置文本和右对齐）
        setDecimalData(preText, bits, digits, false, isRight, onDismissListener); // 委托给完整方法
    }


    /**
     * 设置原始进制数据参数（无前置文本）
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param onOriginalDismissListener 保留原始数据的确认关闭回调
     */
    public void setOriginalDecimalData(int bits, int digits, OnOriginalDismissListener onOriginalDismissListener) { // 设置原始进制数据（无前置文本）
        setOriginalDecimalData(null, bits, digits, onOriginalDismissListener); // 委托给完整方法
    }

    /**
     * 设置原始进制数据参数（有前置文本）
     * @param preText 前置文本
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param onOriginalDismissListener 保留原始数据的确认关闭回调
     */
    public void setOriginalDecimalData(String preText, int bits, int digits, OnOriginalDismissListener onOriginalDismissListener) { // 设置原始进制数据（有前置文本）
        setOriginalDecimalData(preText, bits, digits, false, false, onOriginalDismissListener); // 委托给完整方法
    }

    /**
     * 设置数据为进制数据类时的参数
     * @param preText 前置文本
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param changeLetter 是否启用字母映射
     * @param isRight 是否右对齐
     * @param onDismissListener 确认关闭回调
     */
    private void setDecimalData(String preText, int bits, int digits, boolean changeLetter, boolean isRight, OnDismissListener onDismissListener) { // 设置进制数据的完整方法
        first = true; // 设置首次点击标志
        this.bits = bits; // 保存位数限制
        this.digits = digits; // 保存进制类型
        this.changeLetter = changeLetter; // 保存字母映射标志
        tvShow.requestFocus(); // 请求焦点
        if (preText != null) { // 如果有前置文本
            preText = KeyBoardNumberUtil.reCalculateSpace(preText, digits); // 重新计算空格
        }
        tvShow.setText(preText); // 设置前置文本
        setFilter(digits, bits); // 设置输入过滤器
        if (preText != null) { // 如果有前置文本
            tvShow.setSelection(Math.min(preText.length(), tvShow.getText().length())); // 设置光标位置
        }
        gridView.requestFocus(); // 按键网格获取焦点
        list.get(INDEX_BS).setEnabled(false); // 禁用b/s单位键
        list.get(INDEX_KBS).setEnabled(false); // 禁用kb/s单位键
        list.get(INDEX_MBS).setEnabled(false); // 禁用Mb/s单位键
        ((RadioButton) list.get(INDEX_BS)).setChecked(false); // 取消b/s选中
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false); // 取消kb/s选中
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false); // 取消Mb/s选中
        this.onOriginalDismissListener = null; // 清空原始数据回调
        this.onDismissListener = onDismissListener; // 保存确认关闭回调
        setLayoutPosition(isRight); // 设置布局位置
        setChangeLetter(); // 设置字母映射
        setDigits(); // 设置按键启用状态
        show(); // 显示键盘
    }

    /**
     * 设置数据为进制数据类时的参数，保留原始的输入数据
     * @param preText 前置文本
     * @param bits 输入位数限制
     * @param digits 进制类型
     * @param changeLetter 是否启用字母映射
     * @param isRight 是否右对齐
     * @param onOriginalDismissListener 保留原始数据的确认关闭回调
     */
    private void setOriginalDecimalData(String preText, int bits, int digits, boolean changeLetter, boolean isRight, OnOriginalDismissListener onOriginalDismissListener) { // 设置原始进制数据的完整方法
        this.bits = bits; // 保存位数限制
        first = true; // 设置首次点击标志
        this.digits = digits; // 保存进制类型
        this.changeLetter = changeLetter; // 保存字母映射标志
        tvShow.requestFocus(); // 请求焦点
        if (preText != null) { // 如果有前置文本
            preText = KeyBoardNumberUtil.reCalculateSpace(preText, digits); // 重新计算空格
        }
        tvShow.setText(preText); // 设置前置文本
        setFilter(digits, bits); // 设置输入过滤器
        if (preText != null) { // 如果有前置文本
            tvShow.setSelection(Math.min(preText.length(), tvShow.getText().length())); // 设置光标位置
        }
        gridView.requestFocus(); // 按键网格获取焦点
        list.get(INDEX_BS).setEnabled(false); // 禁用b/s单位键
        list.get(INDEX_KBS).setEnabled(false); // 禁用kb/s单位键
        list.get(INDEX_MBS).setEnabled(false); // 禁用Mb/s单位键
        ((RadioButton) list.get(INDEX_BS)).setChecked(false); // 取消b/s选中
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false); // 取消kb/s选中
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false); // 取消Mb/s选中
        this.onDismissListener = null; // 清空普通回调
        this.onOriginalDismissListener = onOriginalDismissListener; // 保存原始数据回调
        setLayoutPosition(isRight); // 设置布局位置
        setChangeLetter(); // 设置字母映射
        setDigits(); // 设置按键启用状态
        show(); // 显示键盘
    }

    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param minFloat          数据的最小值，小数点后有几位就是保留几位小数
     * @param maxFloat          数据的最大值
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(double number, double minFloat, double maxFloat, OnDismissListener onDismissListener) { // 设置浮点数据
        this.bs = ""; // 清空波特率单位
        this.minFloat = minFloat; // 保存最小浮点值
        this.maxFloat = maxFloat; // 保存最大浮点值
        this.bits = BITS_FOR_BAUDRATE; // 设置位数限制
        this.digits = DIGITS_FLOAT; // 设置进制类型为浮点
        first = true; // 设置首次点击标志
        changeLetter = false; // 不使用字母映射
        list.get(INDEX_MBS).setEnabled(false); // 禁用Mb/s单位键
        list.get(INDEX_BS).setEnabled(false); // 禁用b/s单位键
        list.get(INDEX_KBS).setEnabled(false); // 禁用kb/s单位键
        ((RadioButton) list.get(INDEX_BS)).setChecked(false); // 取消b/s选中
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false); // 取消kb/s选中
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false); // 取消Mb/s选中
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(String.valueOf(number)); // 设置初始数值
        setFilter(digits, bits); // 设置输入过滤器
        tvShow.setSelection(Math.min(String.valueOf(number).length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格获取焦点
        this.onDismissListener = onDismissListener; // 保存确认关闭回调
        setLayoutPosition(true); // 右对齐
        setChangeLetter(); // 设置字母映射
        setDigits(); // 设置按键启用状态
        show(); // 显示键盘
    }

    /**
     * 设置数据为b/s类时的参数
     *
     * @param number            数据
     * @param bs                单位
     * @param minBs             数据的最小值，单位b/s
     * @param maxBs             数据的最大值，单位b/s
     * @param onDismissListener 关闭时的回调
     */
    public void setBaudRateData(double number, String bs, int minBs, int maxBs, OnDismissListener onDismissListener) { // 设置波特率数据
        this.minBaudRate = minBs; // 保存最小波特率
        this.maxBaudRate = maxBs; // 保存最大波特率
        this.bits = BITS_FOR_BAUDRATE; // 设置位数限制
        this.digits = DIGITS_BAUDRATE; // 设置进制类型为波特率
        first = true; // 设置首次点击标志
        changeLetter = false; // 不使用字母映射
        ((RadioButton) list.get(INDEX_BS)).setEnabled(true); // 启用b/s单位键
        ((RadioButton) list.get(INDEX_KBS)).setEnabled(true); // 启用kb/s单位键
        ((RadioButton) list.get(INDEX_MBS)).setEnabled(true); // 启用Mb/s单位键
        if (KEYBOARD_BS.equals(bs)) { // 如果单位是b/s
            ((RadioButton) list.get(INDEX_BS)).setChecked(true); // 选中b/s
            ((RadioButton) list.get(INDEX_KBS)).setChecked(false); // 取消kb/s
            ((RadioButton) list.get(INDEX_MBS)).setChecked(false); // 取消Mb/s
            this.bs = KEYBOARD_BS; // 保存当前单位
        } else if (KEYBOARD_KBS.equals(bs)) { // 如果单位是kb/s
            ((RadioButton) list.get(INDEX_KBS)).setChecked(true); // 选中kb/s
            ((RadioButton) list.get(INDEX_BS)).setChecked(false); // 取消b/s
            ((RadioButton) list.get(INDEX_MBS)).setChecked(false); // 取消Mb/s
            this.bs = KEYBOARD_KBS; // 保存当前单位
        } else if (KEYBOARD_MBS.equals(bs)) { // 如果单位是Mb/s
            ((RadioButton) list.get(INDEX_MBS)).setChecked(true); // 选中Mb/s
            ((RadioButton) list.get(INDEX_BS)).setChecked(false); // 取消b/s
            ((RadioButton) list.get(INDEX_KBS)).setChecked(false); // 取消kb/s
            this.bs = KEYBOARD_MBS; // 保存当前单位
        } else { // 其他无效单位
            return; // 直接返回
        }
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(String.valueOf(number)); // 设置初始数值
        setFilter(digits, bits); // 设置输入过滤器
        tvShow.setSelection(Math.min(String.valueOf(number).length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格获取焦点
        this.onDismissListener = onDismissListener; // 保存确认关闭回调
        setLayoutPosition(true); // 右对齐
        setChangeLetter(); // 设置字母映射
        setDigits(); // 设置按键启用状态
        show(); // 显示键盘
    }

    /**
     * 设置字母映射模式下的按键文本
     */
    private void setChangeLetter() { // 设置字母映射模式下按键显示文本
        list.get(INDEX_9).setText(changeLetter ? "12" : "9"); // 9键映射为12
        list.get(INDEX_A).setText(changeLetter ? "16" : "A"); // A键映射为16
        list.get(INDEX_B).setText(changeLetter ? "20" : "B"); // B键映射为20
        list.get(INDEX_C).setText(changeLetter ? "24" : "C"); // C键映射为24
        list.get(INDEX_D).setText(changeLetter ? "32" : "D"); // D键映射为32
        list.get(INDEX_E).setText(changeLetter ? "48" : "E"); // E键映射为48
        list.get(INDEX_F).setText(changeLetter ? "64" : "F"); // F键映射为64
    }

    /**
     * 根据当前进制类型设置按键启用/禁用状态
     */
    private void setDigits() { // 设置按键启用状态
        for (int i = 0; i < list.size(); i++) { // 遍历所有按键
            Button item = list.get(i); // 获取按键
            item.setEnabled(KeyBoardNumberUtil.isEnabled(digits, i)); // 根据进制类型判断是否启用
        }
    }

    /**
     * 设置显示区域布局位置
     * @param isRight 是否右对齐
     */
    private void setLayoutPosition(boolean isRight) { // 设置布局位置
        AbsoluteLayout.LayoutParams layoutParams = (LayoutParams) showLayout.getLayoutParams(); // 获取布局参数
        if (isRight) { // 右对齐
//            layoutParams.y = 472; // 右对齐Y坐标（已注释）
        } else { // 左对齐
//            layoutParams.y = 472; // 左对齐Y坐标（已注释）
        }
        showLayout.setLayoutParams(layoutParams); // 应用布局参数
    }

    /**
     * 判断输入是否超过位数限制
     * @return 是否已超过位数限制
     */
    private boolean isOverBits() { // 判断是否超过位数限制
        String s = tvShow.getText().toString().replace(" ", ""); // 获取去除空格后的文本
        if (digits == DIGITS_2 || digits == DIGITS_2X) { // 二进制模式
            return bits != -1 && s.length() >= bits; // 检查是否达到位数限制
        } else if (digits == DIGITS_16 || digits == DIGITS_16X) { // 十六进制模式
            return bits != -1 && s.length() >= bits; // 检查是否达到位数限制
        } else { // 其他模式
            return bits != -1 && s.length() >= bits; // 检查是否达到位数限制
        }
    }

    //添加操作 // 添加字符操作
    /**
     * 在光标位置插入字符
     * @param add 要插入的字符
     */
    private void addText(String add) { // 在光标位置插入字符
        int index = tvShow.getSelectionStart(); // 获取当前光标位置
        Editable editable = tvShow.getText(); // 获取可编辑文本
//        直接复制粘贴的问题，需要继承edittext，重写下面这个方法 // 复制粘贴问题注释
//        tvShow.onTextContextMenuItem() // 文本上下文菜单注释
        editable.insert(index, add); // 在光标位置插入字符
        int interval = KeyBoardNumberUtil.getInterval(digits); // 获取空格间隔
        tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), digits)); // 重新计算空格
        if ((index + 2) % (interval + 1) == 0 || (index + 1) % (interval + 1) == 0) { // 如果光标在空格位置附近
            tvShow.setSelection(index + 2); // 跳过空格，光标后移2位
        } else { // 不在空格位置
            tvShow.setSelection(index + 1); // 光标后移1位
        }
    }

    //删除操作 // 删除字符操作
    /**
     * 删除光标前的一个字符（含空格跳过处理）
     */
    private void delText() { // 删除光标前的字符
        int index = tvShow.getSelectionStart(); // 获取当前光标位置
        if (index > 0) { // 光标不在开头
            Editable editable = tvShow.getText(); // 获取可编辑文本
            if (index >= 2 && ' ' == editable.charAt(index - 1)) { // 光标前是空格
                editable.delete(index - 2, index); // 连同空格前一个字符一起删除
            } else { // 光标前不是空格
                editable.delete(index - 1, index); // 只删除前一个字符
            }
            tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), digits)); // 重新计算空格
            if (index >= 2 && index % (KeyBoardNumberUtil.getInterval(digits) + 1) == 0) { // 光标在空格位置
                tvShow.setSelection(index - 2); // 跳过空格，光标前移2位
            } else { // 不在空格位置
                tvShow.setSelection(index - 1); // 光标前移1位
            }
        }
    }

    /**
     * 按键点击监听器
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 按键点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            int index = Integer.parseInt((String) v.getTag()); // 获取按键索引
            String str = ((Button) v).getText().toString(); // 获取按键文本
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (KeyBoardNumberUtil.isNumber(index)) { // 数字键
                if (first) { // 首次点击
                    first = false; // 重置标志
                    tvShow.setText(""); // 清空输入框
                }
                if (!isOverBits()) { // 未超过位数限制
                    addText(str); // 添加字符
                    if (changeLetter) { // 字母映射模式
                        tvShow.setSelection(tvShow.getText().length()); // 光标移到末尾
                    }
                }
            } else if (KeyBoardNumberUtil.isDelete(index)) { // 退格键
                delText(); // 删除字符
            } else if (KeyBoardNumberUtil.isBs(index)) { // b/s单位键
                bs = KEYBOARD_BS; // 设置当前单位为b/s
                for (int i = 0; i < list.size(); i++) { // 遍历所有按键
                    if (i == INDEX_BS) { // b/s键
                        ((RadioButton) list.get(i)).setChecked(true); // 选中
                    } else if (i == INDEX_KBS || i == INDEX_MBS) { // 其他单位键
                        ((RadioButton) list.get(i)).setChecked(false); // 取消选中
                    }
                }
            } else if (KeyBoardNumberUtil.isKbs(index)) { // kb/s单位键
                bs = KEYBOARD_KBS; // 设置当前单位为kb/s
                for (int i = 0; i < list.size(); i++) { // 遍历所有按键
                    if (i == INDEX_KBS) { // kb/s键
                        ((RadioButton) list.get(i)).setChecked(true); // 选中
                    } else if (i == INDEX_BS || i == INDEX_MBS) { // 其他单位键
                        ((RadioButton) list.get(i)).setChecked(false); // 取消选中
                    }
                }
            } else if (KeyBoardNumberUtil.isMbs(index)) { // Mb/s单位键
                bs = KEYBOARD_MBS; // 设置当前单位为Mb/s
                for (int i = 0; i < list.size(); i++) { // 遍历所有按键
                    if (i == INDEX_MBS) { // Mb/s键
                        ((RadioButton) list.get(i)).setChecked(true); // 选中
                    } else if (i == INDEX_KBS || i == INDEX_BS) { // 其他单位键
                        ((RadioButton) list.get(i)).setChecked(false); // 取消选中
                    }
                }
            } else if (KeyBoardNumberUtil.isPoint(index)) { // 小数点键
                if (first) { // 首次点击
                    first = false; // 重置标志
                    tvShow.setText(""); // 清空输入框
                }
                if (isOverBits()) return; // 超过位数限制则返回
                if (!tvShow.getText().toString().contains(str)) { // 文本中不包含小数点
                    addText(str); // 添加小数点
                }
            } else if (KeyBoardNumberUtil.isEnter(index)) { // 确认键
                if (digits == DIGITS_FLOAT) { // 浮点模式
                    String text = tvShow.getText().toString(); // 获取输入文本
                    String result = ""; // 结果字符串
                    if (!StrUtil.isEmpty(text) && !"0".equals(text) && !"0.0".equals(text)) { // 非空且非零
                        if (text.startsWith(".")) { // 以小数点开头
                            text = "0" + text; // 前面补0
                        }
                        if (text.endsWith(".")) { // 以小数点结尾
                            text = text.replace(".", ""); // 去掉小数点
                            if (StrUtil.isEmpty(text)) { // 如果为空
                                text = "0"; // 设为0
                            }
                        }
                        double d = Double.parseDouble(text); // 解析为double
                        d = Math.max(d, minFloat); // 不小于最小值
                        d = Math.min(d, maxFloat); // 不大于最大值
                        int length = String.valueOf(minFloat).split("\\.")[1].length(); // 获取小数位数
                        String[] results = String.valueOf(d).split("\\."); // 分割整数和小数部分
                        if (results[1].length() > length) { // 小数位数超过限制
                            results[1] = results[1].substring(0, length); // 截取到限制位数
                        }
                        result = results[0] + "." + results[1]; // 拼接结果
                    } else { // 空或零
                        result = String.valueOf(minFloat); // 使用最小值
                    }
                    if (onDismissListener != null) { // 回调非空
                        onDismissListener.onDismiss(result); // 返回结果
                    }
                } else if (digits == DIGITS_BAUDRATE) { // 波特率模式
                    String text = tvShow.getText().toString(); // 获取输入文本
                    if (!StrUtil.isEmpty(text)) { // 非空
                        if (text.startsWith(".")) { // 以小数点开头
                            text = "0" + text; // 前面补0
                        }
                        if (text.endsWith(".")) { // 以小数点结尾
                            text = text.replace(".", ""); // 去掉小数点
                            if (StrUtil.isEmpty(text)) { // 如果为空
                                text = "0"; // 设为0
                            }
                        }
                        int baudRate = TBookUtil.getIntFromBaudRate(text + bs); // 将文本+单位转为波特率整数
                        baudRate = Math.max(baudRate, minBaudRate); // 不小于最小值
                        baudRate = Math.min(baudRate, maxBaudRate); // 不大于最大值
                        if (onDismissListener != null) { // 回调非空
                            onDismissListener.onDismiss(TBookUtil.getBaudRateFromInt(baudRate)); // 返回格式化的波特率字符串
                        }
                    } else { // 空输入
                        if (onDismissListener != null) { // 回调非空
                            onDismissListener.onDismiss(TBookUtil.getBaudRateFromInt(minBaudRate)); // 返回最小波特率
                        }
                    }
                } else { // 进制模式
                    if (changeLetter) { // 字母映射模式
                        if (onDismissListener != null) { // 回调非空
                            onDismissListener.onDismiss(KeyBoardNumberUtil.reCalculateSpace( // 重新计算空格后返回
                                    tvShow.getText().toString().replace(" ", ""), digits).trim()); // 去除空格后格式化
                        }
                    } else { // 普通进制模式
                        if (onDismissListener != null) { // 普通回调非空
                            onDismissListener.onDismiss( // 返回补齐位数后的结果
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toBits(tvShow.getText().toString().replace(" ", ""), bits), digits).trim() // 补齐位数后格式化
                            );
                        }
                        if (onOriginalDismissListener != null) { // 原始数据回调非空
                            onOriginalDismissListener.onOriginalDismiss( // 返回补齐位数后的结果
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toBits(tvShow.getText().toString().replace(" ", ""), bits), digits).trim(), // 补齐位数后格式化
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toEvenNumberLength(tvShow.getText().toString().replace(" ", "")), digits).trim() // 仅补齐偶数位后格式化
                            );
                        }
                    }
                }
                hide(); // 隐藏键盘
            }
        }
    };

    /**
     * 长按监听器（长按退格键清空输入）
     */
    private OnLongClickListener onLongClickListener = new OnLongClickListener() { // 长按监听器
        @Override
        public boolean onLongClick(View v) { // 长按回调
            if (!StrUtil.isEmpty(tvShow.getText().toString())) { // 输入框非空
                PlaySound.getInstance().playButton(); // 播放按键音效
                tvShow.setText(""); // 清空输入框
            }
            return false; // 不消费事件，允许后续处理
        }
    };

    /**
     * 移动视图到指定位置
     * @param view 要移动的视图
     * @param x X坐标
     * @param y Y坐标
     */
    private void moveViewToPosition(View view, int x, int y) { // 移动视图到指定位置
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) view.getLayoutParams(); // 获取布局参数
        if (layoutParams == null) { // 布局参数为空
            layoutParams = new AbsoluteLayout.LayoutParams( // 创建新的布局参数
                    view.getWidth(), // 宽度
                    view.getHeight(), // 高度
                    x, // X坐标
                    y // Y坐标
            );
            view.setLayoutParams(layoutParams); // 应用布局参数
        } else { // 布局参数非空
            layoutParams.x = x; // 设置X坐标
            layoutParams.y = y; // 设置Y坐标
            view.setLayoutParams(layoutParams); // 应用布局参数
        }
    }


    /**
     * 根据进制类型设置输入过滤器
     * @param digits 进制类型
     * @param bits 位数限制
     */
    private void setFilter(int digits, int bits) { // 设置输入过滤器
        String regex = ""; // 正则表达式
        switch (digits) { // 根据进制类型选择正则
            case IDigits.DIGITS_2: // 二进制
                regex = FilterFactory.BINARY_REGEX; // 二进制正则
                break;
            case IDigits.DIGITS_2X: // 二进制扩展
                regex = FilterFactory.BINARY_X_REGEX; // 二进制扩展正则
                break;
            case IDigits.DIGITS_8: // 八进制
                regex = FilterFactory.OCTAL_REGEX; // 八进制正则
                break;
            case IDigits.DIGITS_10: // 十进制
                regex = FilterFactory.DECIMAL_REGEX; // 十进制正则
                break;
            case IDigits.DIGITS_16: // 十六进制
                regex = FilterFactory.HEX_REGEX; // 十六进制正则
                break;
            case IDigits.DIGITS_16X: // 十六进制扩展
                regex = FilterFactory.HEX_X_REGEX; // 十六进制扩展正则
                break;
            case IDigits.DIGITS_BAUDRATE: // 波特率
            case IDigits.DIGITS_FLOAT: // 浮点
                regex = FilterFactory.BAUDRATE_REGEX; // 波特率/浮点正则
                break;
        }

        int interval = KeyBoardNumberUtil.getInterval(digits); // 获取空格间隔
        int num = bits / interval; // 计算完整分组数
        int remain = bits % interval; // 计算余数位数
        int finalBits = num * (interval + 1) + remain; // 计算含空格的总长度
        if (digits == IDigits.DIGITS_10) { // 十进制不需要空格分组
            finalBits = bits; // 直接使用位数
        }
        //bits有效位数 // bits有效位数注释
        Logger.d(TAG, "bits=" + bits + " ,interval=" + interval + " ,finalBits=" + finalBits + " ,digits=" + digits); // 打印日志

        if (bits != -1) { // 有位数限制
            tvShow.setFilters(new InputFilter[]{FilterFactory.getFilter(regex), new InputFilter.LengthFilter(finalBits + 1)}); // 设置正则过滤器和长度过滤器
        } else { // 无位数限制
            tvShow.setFilters(new InputFilter[]{FilterFactory.getFilter(regex)}); // 只设置正则过滤器
        }

    }
}
