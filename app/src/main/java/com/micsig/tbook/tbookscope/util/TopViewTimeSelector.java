package com.micsig.tbook.tbookscope.util; // 工具包，存放示波器应用的各类工具类

import android.content.Context; // Android上下文，用于视图创建和属性获取
import android.content.res.TypedArray; // 自定义属性数组，用于读取XML中声明的自定义属性
import android.graphics.Color; // 颜色类，提供标准颜色常量
import android.graphics.drawable.GradientDrawable; // 渐变/形状Drawable，用于创建输入框选中边框
import android.util.AttributeSet; // XML属性集，用于自定义视图的XML属性解析
import android.util.Log; // Android日志类
import android.view.View; // 视图基类，用于inflate布局和点击事件
import android.widget.LinearLayout; // 线性布局，本类的父类
import android.widget.TextView; // 文本视图，用于年/月/日/时/分/秒输入显示

import com.micsig.tbook.tbookscope.MainActivity; // 主Activity，用于获取数字键盘对话框
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 基于RxJava的事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxBus消息枚举定义
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 数字键盘位数定义接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 数字键盘对话框

import com.micsig.tbook.ui.MTextView; // 自定义TextView（当前未使用）
import com.micsig.tbook.ui.R; // UI模块资源R类（布局和自定义属性）

import java.time.LocalDateTime; // Java8日期时间类，用于时间计算和校验
import java.time.YearMonth; // Java8年月类，用于计算指定年月的最大天数

import io.reactivex.rxjava3.functions.Consumer; // RxJava3消费者接口，用于RxBus事件订阅

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     TopViewTimeSelector - 时间选择器视图                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：MHO示波器Android应用 → 工具模块(util) → 时间选择器                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                    │
 * │   1. 提供年/月/日/时/分/秒6个输入框的时间选择界面                               │
 * │   2. 支持START（开始时间）和STOP（停止时间）两种类型                            │
 * │   3. 点击输入框弹出数字键盘，支持4位年份和2位其他字段输入                        │
 * │   4. 输入校验：年份范围（当前年~当前年+1）、月份1-12、日期根据年月动态计算、     │
 * │      小时0-23、分秒0-59                                                       │
 * │   5. STOP类型输入确认后自动保存停止时间到CacheUtil                              │
 * │   6. 通过RxBus监听隐藏输入背景事件                                             │
 * │   7. 支持只读模式控制                                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                    │
 * │   - 继承LinearLayout，水平排列6个输入框                                        │
 * │   - 通过XML自定义属性(type)区分START/STOP类型                                  │
 * │   - 每个输入框对应独立的OnDismissListener处理输入回调和校验                     │
 * │   - 使用RxBus订阅MSG_HIDE_INPUT_BACKGROUND事件，统一清除输入框选中边框          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                    │
 * │   用户点击输入框 → 弹出数字键盘 → 输入完成 → OnDismissListener校验             │
 * │   → 校验通过：更新resultXxx变量 + TextView显示 + (STOP类型)保存CacheUtil       │
 * │   → 校验失败：DToast提示错误信息                                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                    │
 * │   - TopDialogNumberKeyBoard：数字键盘对话框                                    │
 * │   - IDigits：数字键盘位数定义                                                  │
 * │   - CacheUtil：缓存工具，保存STOP时间                                          │
 * │   - DToast：自定义Toast提示                                                    │
 * │   - RxBus/RxEnum：事件总线，监听隐藏输入背景事件                                │
 * │   - LocalDateTime/YearMonth：Java8时间API，用于校验和计算                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                    │
 * │   // XML布局中使用                                                             │
 * │   <com.micsig.tbook.tbookscope.util.TopViewTimeSelector                      │
 * │       app:type="0" />  <!-- 0=START, 1=STOP -->                               │
 * │   // 代码中设置当前时间                                                        │
 * │   timeSelector.setNowTime();                                                  │
 * │   // 获取选择的时间                                                            │
 * │   LocalDateTime time = timeSelector.getTime();                                │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopViewTimeSelector extends LinearLayout {

    /**
     * 时间选择器类型枚举。
     * START - 开始时间选择器，输入后不自动保存
     * STOP  - 停止时间选择器，输入确认后自动保存到CacheUtil
     */
    public enum Type {START, STOP}; // 类型枚举：START=开始时间，STOP=停止时间

    /** 数字键盘对话框引用，从MainActivity中获取 */
    protected TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框实例

    /**
     * 单参数构造器，代码动态创建时调用。
     * @param context 上下文
     */
    public TopViewTimeSelector(Context context) { // 单参数构造器
        this(context, null); // 委托给双参数构造器
    }

    /**
     * 双参数构造器，XML布局inflate时调用。
     * @param context 上下文
     * @param attrs   XML属性集
     */
    public TopViewTimeSelector(Context context, AttributeSet attrs) { // 双参数构造器
        this(context, attrs, 0); // 委托给三参数构造器
    }

    /**
     * 三参数构造器，完整构造器，执行实际的初始化逻辑。
     * @param context      上下文
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewTimeSelector(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造器
        super(context, attrs, defStyleAttr); // 调用父类构造器
        this.context = context; // 保存上下文引用
        initView(context, attrs, defStyleAttr); // 执行视图初始化
    }

    /** 当前时间选择器的类型（START或STOP） */
    private Type type; // 类型：START=开始时间，STOP=停止时间

    /** 上下文引用，用于资源获取和类型转换 */
    private Context context; // 上下文对象

    /** 用户输入的年份结果值 */
    private int resultYear; // 年份结果

    /** 用户输入的月份结果值 */
    private int resultMonth; // 月份结果

    /** 用户输入的日期结果值 */
    private int resultDay; // 日期结果

    /** 用户输入的小时结果值 */
    private int resultHour; // 小时结果

    /** 用户输入的分钟结果值 */
    private int resultMinute; // 分钟结果

    /** 用户输入的秒数结果值 */
    private int resultSecond; // 秒数结果

    /** 年份输入框TextView */
    private TextView yearInput; // 年份输入框

    /** 月份输入框TextView */
    private TextView monthInput; // 月份输入框

    /** 日期输入框TextView */
    private TextView dayInput; // 日期输入框

    /** 小时输入框TextView */
    private TextView hourInput; // 小时输入框

    /** 分钟输入框TextView */
    private TextView minuteInput; // 分钟输入框

    /** 秒数输入框TextView */
    private TextView secondInput; // 秒数输入框

    /** 输入框选中时的边框Drawable，灰色矩形描边 */
    public GradientDrawable drawable = new GradientDrawable(); // 输入框选中边框样式

    /**
     * 初始化视图。
     * 1. Inflate布局文件
     * 2. 解析XML自定义属性（type）
     * 3. 绑定6个输入框的点击事件
     * 4. 创建选中边框样式
     * 5. 获取数字键盘对话框引用
     * 6. 注册RxBus事件监听
     *
     * @param context      上下文
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 视图初始化
        View view = View.inflate(context, R.layout.view_top_time_selector, this); // 加载布局文件并附加到本LinearLayout
        setOrientation(HORIZONTAL); // 设置水平排列方向
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewTimeSelector); // 获取自定义属性数组
        int typeValue = ta.getInt(R.styleable.TopViewTimeSelector_type, 0); // 读取type属性值，默认0（START）
        this.type = Type.values()[typeValue]; // 将int值转换为Type枚举
        ta.recycle(); // 回收TypedArray，避免内存泄漏
        yearInput = view.findViewById(R.id.yearInput); // 绑定年份输入框
        monthInput = findViewById(R.id.monthInput); // 绑定月份输入框
        dayInput = findViewById(R.id.dayInput); // 绑定日期输入框
        hourInput = view.findViewById(R.id.hourInput); // 绑定小时输入框
        minuteInput = view.findViewById(R.id.minuteInput); // 绑定分钟输入框
        secondInput = view.findViewById(R.id.secondInput); // 绑定秒数输入框
        yearInput.setOnClickListener(onClickListener); // 注册年份输入框点击事件
        monthInput.setOnClickListener(onClickListener); // 注册月份输入框点击事件
        dayInput.setOnClickListener(onClickListener); // 注册日期输入框点击事件
        hourInput.setOnClickListener(onClickListener); // 注册小时输入框点击事件
        minuteInput.setOnClickListener(onClickListener); // 注册分钟输入框点击事件
        secondInput.setOnClickListener(onClickListener); // 注册秒数输入框点击事件
        drawable.setShape(GradientDrawable.RECTANGLE); // 设置边框形状为矩形
        drawable.setStroke(2, Color.GRAY); // 设置描边：2像素宽，灰色
        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById((com.micsig.tbook.tbookscope.R.id.dialogNumberKeyBoard)); // 从MainActivity获取数字键盘对话框引用
        initControl(); // 初始化RxBus事件监听
    }

    /**
     * 初始化RxBus事件监听。
     * 订阅MSG_HIDE_INPUT_BACKGROUND事件，收到事件时清除所有输入框的选中边框。
     */
    public void initControl() { // 初始化控制监听
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_INPUT_BACKGROUND).subscribe(consumerCommandToUI); // 订阅隐藏输入背景事件
    }

    /**
     * 输入框点击事件监听器。
     * 根据点击的输入框，设置数字键盘的位数和回调监听器，并显示选中边框。
     * - 年份：4位数字，DIGITS_10（0-9）
     * - 其他字段：2位数字，DIGITS_10（0-9）
     */
    private OnClickListener onClickListener = new OnClickListener() { // 输入框点击监听器
        @Override
        public void onClick(View v) { // 点击事件处理
             if (v.getId() == yearInput.getId()) { // 点击了年份输入框
                dialogKeyBoard.setDecimalData(4, IDigits.DIGITS_10, onYearListener); // 设置4位数字键盘，0-9数字，年份回调
                yearInput.setBackground(drawable); // 显示选中边框
            } else if (v.getId() == monthInput.getId()) { // 点击了月份输入框
                dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onMonthListener); // 设置2位数字键盘，0-9数字，月份回调
                 monthInput.setBackground(drawable); // 显示选中边框
             } else if (v.getId() == dayInput.getId()) { // 点击了日期输入框
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onDayListener); // 设置2位数字键盘，0-9数字，日期回调
                 dayInput.setBackground(drawable); // 显示选中边框
             } else if (v.getId() == hourInput.getId()) { // 点击了小时输入框
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onHourListener); // 设置2位数字键盘，0-9数字，小时回调
                 hourInput.setBackground(drawable); // 显示选中边框
             } else if (v.getId() == minuteInput.getId()) { // 点击了分钟输入框
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onMinuteListener); // 设置2位数字键盘，0-9数字，分钟回调
                 minuteInput.setBackground(drawable); // 显示选中边框
             } else if (v.getId() == secondInput.getId()) { // 点击了秒数输入框
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onSecondListener); // 设置2位数字键盘，0-9数字，秒数回调
                 secondInput.setBackground(drawable); // 显示选中边框
             }
        }
    };

    /**
     * 数字键盘关闭监听器（空实现，预留扩展用）。
     * 当前未使用，作为默认回调占位。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 空回调监听器
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
        } // 空实现
    };

    /**
     * 年份输入完成回调监听器。
     * 校验规则：输入年份必须在[当前年份, 当前年份+1]范围内。
     * 校验通过：更新resultYear、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确年份"。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onYearListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 年份回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            int inputYear = Integer.parseInt(result.trim()); // 解析输入的年份
            int year = LocalDateTime.now().getYear(); // 获取当前年份
            if (inputYear - year > 1 || inputYear < year) { // 年份超出允许范围（当前年~当前年+1）
                DToast.get().show(R.string.inputCorrectYear); // 提示"请输入正确年份"
            } else { // 年份合法
                resultYear = inputYear; // 更新年份结果值
                yearInput.setText(result); // 显示输入的年份文本
                yearInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            }

        }
    };

    /**
     * 月份输入完成回调监听器。
     * 校验规则：月份必须在1-12范围内。
     * 校验通过：更新resultMonth、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确月份"。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onMonthListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 月份回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            int inputMonth = Integer.parseInt(result.trim()); // 解析输入的月份
            if (inputMonth >= 1 && inputMonth <= 12) { // 月份在1-12范围内
                resultMonth = inputMonth; // 更新月份结果值
                monthInput.setText(result); // 显示输入的月份文本
                monthInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            } else { // 月份不在1-12范围内
                DToast.get().show(R.string.inputCorrectMonth); // 提示"请输入正确月份"
            }
        }
    };

    /**
     * 日期输入完成回调监听器。
     * 校验规则：根据当前输入的年份和月份，使用YearMonth动态计算该月最大天数，
     * 日期必须在1到最大天数范围内（自动处理闰年2月29天等情况）。
     * 校验通过：更新resultDay、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确日期"。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDayListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 日期回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            String inputStringYear = (String) yearInput.getText(); // 获取年份输入框的文本
            String inputStringMonth = (String) monthInput.getText(); // 获取月份输入框的文本
            int inputYear = Integer.parseInt(inputStringYear.trim()); // 解析年份
            int inputMonth = Integer.parseInt(inputStringMonth.trim()); // 解析月份
            int maxDay = YearMonth.of(inputYear, inputMonth).lengthOfMonth(); // 根据年月计算该月最大天数（自动处理闰年）
            int inputDay = Integer.parseInt(result.trim()); // 解析输入的日期
            if (inputDay >= 1 && inputDay <= maxDay) { // 日期在1到最大天数范围内
                resultDay = inputDay; // 更新日期结果值
                dayInput.setText(String.valueOf(inputDay)); // 显示输入的日期文本
                dayInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            } else { // 日期不在合法范围内
                DToast.get().show(R.string.inputCorrectDay); // 提示"请输入正确日期"
            }
        }
    };

    /**
     * 小时输入完成回调监听器。
     * 校验规则：小时必须在0-23范围内（24小时制）。
     * 校验通过：更新resultHour、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确小时"。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onHourListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 小时回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            int inputHour = Integer.parseInt(result.trim()); // 解析输入的小时
            if (inputHour >= 0 && inputHour <= 23) { // 小时在0-23范围内（24小时制）
                resultHour = inputHour; // 更新小时结果值
                hourInput.setText(String.valueOf(inputHour)); // 显示输入的小时文本
                hourInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            } else { // 小时不在0-23范围内
                DToast.get().show(R.string.inputCorrectHour); // 提示"请输入正确小时"
            }
        }
    };

    /**
     * 分钟输入完成回调监听器。
     * 校验规则：分钟必须在0-59范围内。
     * 校验通过：更新resultMinute、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确分钟"。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onMinuteListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 分钟回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            int inputMinute = Integer.parseInt(result.trim()); // 解析输入的分钟
            if (inputMinute >= 0 && inputMinute <= 59) { // 分钟在0-59范围内
                resultMinute = inputMinute; // 更新分钟结果值
                minuteInput.setText(String.valueOf(inputMinute)); // 显示输入的分钟文本
                minuteInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            } else { // 分钟不在0-59范围内
                DToast.get().show(R.string.inputCorrectMinute); // 提示"请输入正确分钟"
            }
        }
    };

    /**
     * 秒数输入完成回调监听器。
     * 校验规则：秒数必须在0-59范围内。
     * 校验通过：更新resultSecond、显示文本、清除边框，STOP类型自动保存。
     * 校验失败：DToast提示"请输入正确分钟"（注意：此处提示文案有误，应为"请输入正确秒数"）。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onSecondListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 秒数回调
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            if (result == null || result.trim().isEmpty()) { // 输入为空或空白
                return; // 直接返回，不做处理
            }
            int inputSecond = Integer.parseInt(result.trim()); // 解析输入的秒数
            if (inputSecond >= 0 && inputSecond <= 59) { // 秒数在0-59范围内
                resultSecond = inputSecond; // 更新秒数结果值
                secondInput.setText(String.valueOf(inputSecond)); // 显示输入的秒数文本
                secondInput.setBackground(null); // 清除选中边框
                if (type == Type.STOP) { // 若为停止时间类型
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME, String.valueOf(getTime())); // 自动保存停止时间到CacheUtil
                }
            } else { // 秒数不在0-59范围内
                DToast.get().show(R.string.inputCorrectMinute); // 提示错误（注意：此处应使用inputCorrectSecond，当前复用了分钟的提示文案）
            }
        }
    };

    /**
     * 获取当前选择器中设置的时间。
     * 将6个字段（年/月/日/时/分/秒）组合为LocalDateTime对象。
     *
     * @return LocalDateTime对象，包含年/月/日/时/分/秒
     */
    public LocalDateTime getTime() { // 获取当前选择的时间
        return LocalDateTime.of(resultYear, resultMonth, resultDay, resultHour, resultMinute, resultSecond); // 用6个字段构造LocalDateTime
    }

    /**
     * 通过ISO格式时间字符串设置时间选择器的值。
     * 字符串格式必须符合LocalDateTime.parse()的ISO格式（如"2024-06-15T14:30:00"）。
     * 解析后分别设置6个输入框的显示文本。
     *
     * @param textTime ISO格式的时间字符串，如"2024-06-15T14:30:00"
     */
    public void setTimeSelectorByString(String textTime) { // 通过字符串设置时间
        LocalDateTime dateTime = LocalDateTime.parse(textTime); // 解析ISO格式时间字符串
        yearInput.setText(String.valueOf(dateTime.getYear())); // 设置年份显示
        monthInput.setText(String.valueOf(dateTime.getMonthValue())); // 设置月份显示
        dayInput.setText(String.valueOf(dateTime.getDayOfMonth())); // 设置日期显示
        hourInput.setText(String.valueOf(dateTime.getHour())); // 设置小时显示
        minuteInput.setText(String.valueOf(dateTime.getMinute())); // 设置分钟显示
        secondInput.setText(String.valueOf(dateTime.getSecond())); // 设置秒数显示
    }

    /**
     * 将时间选择器设置为当前系统时间。
     * 同时更新6个result变量和输入框显示文本。
     */
    public void setNowTime() { // 设置为当前时间
        LocalDateTime now = LocalDateTime.now(); // 获取当前系统时间
        resultYear = now.getYear(); // 缓存当前年份
        resultMonth = now.getMonthValue(); // 缓存当前月份
        resultDay = now.getDayOfMonth(); // 缓存当前日期
        resultHour = now.getHour(); // 缓存当前小时
        resultMinute = now.getMinute(); // 缓存当前分钟
        resultSecond = now.getSecond(); // 缓存当前秒数
        yearInput.setText(String.valueOf(resultYear)); // 显示年份
        monthInput.setText(String.valueOf(resultMonth)); // 显示月份
        dayInput.setText(String.valueOf(resultDay)); // 显示日期
        hourInput.setText(String.valueOf(resultHour)); // 显示小时
        minuteInput.setText(String.valueOf(resultMinute)); // 显示分钟
        secondInput.setText(String.valueOf(resultSecond)); // 显示秒数
    }

    /**
     * RxBus事件消费者，监听MSG_HIDE_INPUT_BACKGROUND事件。
     * 收到事件时清除所有6个输入框的选中边框背景。
     * 该事件通常在用户点击其他区域或切换页面时触发。
     */
    private Consumer<Boolean> consumerCommandToUI = new Consumer<Boolean>() { // RxBus事件消费者
        @Override
        public void accept(Boolean b) throws Exception { // 接收事件
            yearInput.setBackground(null); // 清除年份输入框选中边框
            monthInput.setBackground(null); // 清除月份输入框选中边框
            dayInput.setBackground(null); // 清除日期输入框选中边框
            hourInput.setBackground(null); // 清除小时输入框选中边框
            minuteInput.setBackground(null); // 清除分钟输入框选中边框
            secondInput.setBackground(null); // 清除秒数输入框选中边框
        }
    };


    /**
     * 设置时间选择器的只读模式。
     * enabled=true：所有输入框可点击（可编辑）
     * enabled=false：所有输入框禁用（只读，不可点击弹出键盘）
     *
     * @param enabled true=可编辑，false=只读
     */
    public void setReadOnly(boolean enabled) { // 设置只读模式
        super.setEnabled(enabled); // 设置父布局的启用状态
        if (enabled) { // 启用编辑模式
            yearInput.setEnabled(true); // 启用年份输入框
            monthInput.setEnabled(true); // 启用月份输入框
            dayInput.setEnabled(true); // 启用日期输入框
            hourInput.setEnabled(true); // 启用小时输入框
            minuteInput.setEnabled(true); // 启用分钟输入框
            secondInput.setEnabled(true); // 启用秒数输入框
        } else { // 禁用编辑模式（只读）
            yearInput.setEnabled(false); // 禁用年份输入框
            monthInput.setEnabled(false); // 禁用月份输入框
            dayInput.setEnabled(false); // 禁用日期输入框
            hourInput.setEnabled(false); // 禁用小时输入框
            minuteInput.setEnabled(false); // 禁用分钟输入框
            secondInput.setEnabled(false); // 禁用秒数输入框
        }

    }
}
