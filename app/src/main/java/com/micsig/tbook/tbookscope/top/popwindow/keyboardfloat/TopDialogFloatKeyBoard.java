package com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat; // 包声明：浮点键盘子包

import android.content.Context; // 导入上下文类
import android.text.Editable; // 导入可编辑文本类
import android.text.InputFilter; // 导入输入过滤器类
import android.text.InputType; // 导入输入类型类
import android.text.TextWatcher; // 导入文本变化监听类
import android.util.AttributeSet; // 导入属性集类
import android.util.Log; // 导入日志类
import android.view.ActionMode; // 导入操作模式类
import android.view.KeyEvent; // 导入按键事件类
import android.view.Menu; // 导入菜单类
import android.view.MenuItem; // 导入菜单项类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.Button; // 导入按钮类
import android.widget.EditText; // 导入编辑框类
import android.widget.LinearLayout; // 导入线性布局类
import android.widget.RelativeLayout; // 导入相对布局类
import android.widget.SeekBar; // 导入滑动条类
import android.widget.Switch; // 导入开关类
import android.widget.TextView; // 导入文本视图类

import com.micsig.base.DoubleUtil; // 导入双精度工具类
import com.micsig.base.keyevent.KeyEventUtil; // 导入按键事件工具类
import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.base.NumberUnitParser; // 导入数字单位解析器
import com.micsig.base.filter.UnitInputFilter; // 导入单位输入过滤器
import com.micsig.tbook.scope.math.MathNative; // 导入数学原生库
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import java.math.BigDecimal; // 导入大数类
import java.text.NumberFormat; // 导入数字格式化类
import java.util.ArrayList; // 导入数组列表类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：浮点键盘 - 浮点数输入对话框                               │
 * │ 核心职责：提供浮点数/整数输入的虚拟键盘，支持单位选择和符号切换       │
 * │ 架构设计：继承AbsoluteLayout，实现IKeyBoardFloat接口，             │
 * │          支持多种输入模式（p/n/u/m/k单位、偏移模式、整数模式等）    │
 * │ 数据流向：setFloatData* → 用户输入 → onDismiss回调返回格式化值     │
 * │ 依赖关系：IKeyBoardFloat, KeyBoardNumberUtil, RxBus              │
 * │ 使用场景：触发电平、偏移量、延迟时间等参数的数值输入                 │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/4/28.
 */
public class TopDialogFloatKeyBoard extends AbsoluteLayout implements IKeyBoardFloat { // 浮点键盘对话框
    private static final String TAG = "TopDialogFloatKeyBoard"; // 日志标签
    private Context context; // 上下文对象
    private View fromView; // 触发键盘的源视图
    private LinearLayout showLayout; // 显示区域布局
    private TextView tvSymbol; // 正负号文本视图
    private EditText tvShow; // 输入显示编辑框
    private RelativeLayout gridView; // 按键网格布局
    private ArrayList<Button> list; // 按键按钮列表
    private OnDismissListener onDismissListener; // 关闭回调监听器
    private boolean first = false;//是否是本次打开后第一点击 // 首次点击标记
    private SeekBar seekBar; // 滑动条（延迟调节用）
    private static final int pMax = 999; // p模式最大值
    private static final int nMax = 100; // n模式最大值
    private int valueMax = nMax; // 当前最大值
    private int valueMin = -nMax; // 当前最小值
    private int seekBarMax = Math.abs(valueMax) + Math.abs(valueMin); // 滑动条最大值
    private boolean isShowSeekBar = false; // 是否显示滑动条
    private boolean isFromUser = false; // 是否来自用户操作
    public static int INPUT_LENGTH_LIMIT = 15; // 输入长度限制
    public boolean isChDelay = false; // 是否为通道延迟模式
    private boolean isRefDelay = false; // 是否为参考延迟模式
    private boolean isIntNumber = false; // 是否为整数输入模式

    /**
     * fineExtent 中使用到M，所以将N改为M使用，即复用n to M
     */
    private boolean isReuse_INDEX_n_to_M=false; // 是否将n按键复用为M

    /**
     * p --> M
     */
    private boolean isReuse_INDEX_p_to_M = false; // 是否将p按键复用为M

    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 关闭回调接口，返回输入结果
     */
    public interface OnDismissListener { // 关闭回调接口
        /**
         * 键盘关闭时回调
         * @param fromView 触发键盘的源视图
         * @param show 输入结果字符串
         */
        void onDismiss(View fromView, String show); // 关闭回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogFloatKeyBoard(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogFloatKeyBoard(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogFloatKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    //[0, 240]	608	360
    /**
     * 初始化视图
     */
    private void init() { // 初始化方法
        setClickable(true); // 设置可点击
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_keyboard_float, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v) -> { // 外部区域点击监听
            hide(); // 隐藏键盘
        });
        initView(rootViewGroup); // 初始化子视图
        hide(); // 初始隐藏
    }

    /**
     * 初始化子视图和事件监听
     * @param view 根视图
     */
    private void initView(View view) { // 初始化子视图方法
        showLayout = (LinearLayout) view.findViewById(R.id.showKeyBoardLayout); // 获取显示区域布局
        seekBar = (SeekBar) view.findViewById(R.id.seekBar); // 获取滑动条
        tvSymbol = (TextView) view.findViewById(R.id.symbol); // 获取正负号文本
        tvShow = (EditText) view.findViewById(R.id.show); // 获取输入编辑框
        tvShow.setShowSoftInputOnFocus(false); // 禁用系统软键盘
        tvShow.setLongClickable(false); // 禁用长按（防止粘贴菜单）
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD); // 设置密码输入类型（隐藏光标选择）
        tvShow.setCustomSelectionActionModeCallback(new ActionMode.Callback() { // 禁用文本选择操作模式
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) { // 创建操作模式
                return false; // 返回false禁止创建
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { // 准备操作模式
                return false; // 返回false禁止准备
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) { // 操作项点击
                return false; // 返回false不处理
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) { // 销毁操作模式（空实现）

            }
        });

        tvShow.setOnKeyListener(new OnKeyListener() { // 设置物理按键监听
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) { // 按键事件回调
                Logger.d(TAG, "event= " + event.getKeyCode()); // 打印按键码日志
                if (event.getAction() == KeyEvent.ACTION_DOWN) { // 按键按下事件
                    if (first) { // 如果是首次按键
                        first = false; // 重置首次标记
                        tvShow.setText(""); // 清空输入
                        tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                    }
                    if (KeyEventUtil.isBackKey(event)) { // 如果是返回/删除键
                        if (onItemClickListener != null) { // 触发删除按钮点击
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del));
                        }
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isNegative(event)) { // 如果是负号键
                        if (onItemClickListener != null) { // 触发负号按钮点击
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_negative));
                        }
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isPositive(event)) { // 如果是正号键
                        if (onItemClickListener != null) { // 触发正号按钮点击
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_positive));
                        }
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isConfirmKey(event)) { // 如果是确认键
                        inputDone(getUnitVal()); // 完成输入
                        return true; // 消费事件
                    }
                }
                return false; // 不消费其他事件
            }
        });

        tvShow.addTextChangedListener(new TextWatcher() { // 添加文本变化监听
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 文本变化前（空实现）

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // 文本变化中（空实现）

            }

            @Override
            public void afterTextChanged(Editable s) { // 文本变化后
                if(s.length() > 0) { // 如果文本非空
                    char lastChar = s.charAt(s.length() - 1); // 获取最后一个字符
                    char[] unit = {'p', 'n', 'u', 'm', 'k', 'M'}; // 单位字符数组
                    double[] unitVal = {1e-12, 1e-9, 1e-6, 1e-3, 1e3, 1e6}; // 单位值数组
                    for (int i = 0; i < unit.length; i++) { // 遍历单位
                        if (unit[i] == lastChar) { // 如果最后字符是单位
                            inputDone(unitVal[i]); // 自动完成输入
                            break; // 跳出循环
                        }
                    }
                }
            }
        });

        gridView = (RelativeLayout) view.findViewById(R.id.gridView); // 获取按键网格布局

        list = new ArrayList<Button>(); // 创建按钮列表
        for (int i = 0; i < gridView.getChildCount(); i++) { // 遍历网格子视图
            gridView.getChildAt(i).setOnClickListener(onItemClickListener); // 设置点击监听
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_del) { // 如果是删除键
                gridView.getChildAt(i).setOnLongClickListener(onLongClickListener); // 设置长按监听
            }
            for (int j = 0; j < gridView.getChildCount(); j++) { // 按tag排序
                if ((Integer.parseInt((String) gridView.getChildAt(j).getTag())) == i) { // 找到tag等于i的按钮
                    list.add((Button) gridView.getChildAt(i)); // 添加到列表
                    break; // 跳出内层循环
                }
            }
        }
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener); // 设置滑动条监听
    }

    /**
     * 输入完成处理，格式化并回调结果
     * @param v 单位乘数
     */
    private void inputDone(double v) { // 输入完成方法
        String text = tvShow.getText().toString(); // 获取输入文本
        if (onDismissListener != null) { // 判断回调不为空
            if (!StrUtil.isEmpty(text)) { // 如果文本非空
                if (text.startsWith(".")) { // 如果以小数点开头
                    text = "0" + text; // 前面补0
                }
                if (text.endsWith(".")) { // 如果以小数点结尾
                    text = text.replace(".", ""); // 移除小数点
                    if (StrUtil.isEmpty(text)) { // 如果移除后为空
                        text = "0"; // 设为0
                    }
                }
                if (isSymbolVisible() && !"0".equals(text)) { // 如果有负号且非0
                    text = "-" + text; // 添加负号
                }
                NumberUnitParser numberUnitParser = new NumberUnitParser(text); // 解析数字和单位
                if (((Switch) list.get(INDEX_p)).isChecked() // 如果p单位选中
                        || ((Switch) list.get(INDEX_n)).isChecked() // 或n单位选中
                        || ((Switch) list.get(INDEX_u)).isChecked() // 或u单位选中
                        || ((Switch) list.get(INDEX_m)).isChecked() // 或m单位选中
                        || ((Switch) list.get(INDEX_k)).isChecked() // 或k单位选中
                ) {

                    onDismissListener.onDismiss(fromView, getValueFromD(numberUnitParser.getNumberAsDouble() * v)); // 回调带单位的格式化值
                } else { // 没有单位选中
                    onDismissListener.onDismiss(fromView, text); // 直接回调文本
                }
            } else { // 文本为空
                onDismissListener.onDismiss(fromView, String.valueOf(0)); // 回调0
            }
        }
        hide(); // 隐藏键盘
    }


    /**
     * 获取当前选中的单位乘数值
     * @return 单位乘数值
     */
    private double getUnitVal() { // 获取单位值方法
        double unitVal = 1; // 默认单位值为1
        if (((Switch) list.get(INDEX_p)).isChecked()) { // p单位选中
            if (isReuse_INDEX_p_to_M) { // 如果p复用为M
                unitVal = 1e6; // 兆
            } else { // 正常p
                unitVal = 1e-12; // 皮
            }
        }
        if (((Switch) list.get(INDEX_n)).isChecked()) { // n单位选中
            if (isReuse_INDEX_n_to_M) { // 如果n复用为M
                unitVal = 1e6; // 兆
            } else { // 正常n
                unitVal = 1e-9; // 纳
            }
        }
        if (((Switch) list.get(INDEX_u)).isChecked()) { // u单位选中
            unitVal = 1e-6; // 微
        }
        if (((Switch) list.get(INDEX_m)).isChecked()) { // m单位选中
            unitVal = 1e-3; // 毫
        }
        if (((Switch) list.get(INDEX_k)).isChecked()) { // k单位选中
            unitVal = 1e3; // 千
        }
        return unitVal; // 返回单位值
    }

    /**
     * 显示键盘
     */
    public void show() { // 显示方法
        moveViewToPosition(showLayout, 0, isShowSeekBar ? 618 : context.getResources().getDimensionPixelOffset(R.dimen.rightDialogFloatKeyBoardRow0)); // 移动显示区域到指定位置
        seekBar.setVisibility(isShowSeekBar ? VISIBLE : GONE); // 根据标记显示或隐藏滑动条
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FLOATKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogFloatKeyBoard",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏键盘，重置所有状态
     */
    public void hide() { // 隐藏方法
        isChDelay = false; // 重置通道延迟标记
        isShowSeekBar = false; // 重置滑动条显示标记
        isRefDelay = false; // 重置参考延迟标记
        INPUT_LENGTH_LIMIT = 15; // 恢复默认输入长度限制
        isIntNumber = false; // 重置整数模式标记
        list.get(INDEX_point).setEnabled(true); // 恢复小数点按钮可用
        seekBar.setVisibility(GONE); // 隐藏滑动条
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FLOATKEYBOARD); // 发送对话框关闭事件
    }


    /**
     * 设置m/k单位模式的数据
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatmkData(String number, View fromView, OnDismissListener onDismissListener) { // 设置m/k单位数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        isReuse_INDEX_n_to_M=false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("mk"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器，仅允许m和k
        ((Switch) list.get(INDEX_n)).setText("n"); // 恢复n按键文本
        ((Switch) list.get(INDEX_p)).setText("p"); // 恢复p按键文本

        (list.get(INDEX_positive)).setEnabled(false); // 禁用正号按钮
        (list.get(INDEX_negative)).setEnabled(false); // 禁用负号按钮


        ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p单位
        ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
        ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
        ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位
        ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位

        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
        if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
            ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m
            number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m
            number =number.replace(" ",""); // 移除空格
        } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
            ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k
            number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k
            number =number.replace(" ",""); // 移除空格
        }

        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置

        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }

    /**
     * 设置滑动条进度
     * @param i 进度值
     */
    public void setProgress(int i) { // 设置滑动条进度
        isFromUser = true; // 标记来自用户操作
        //progress 0--200 转化为 -100--100
        int actualValue = i - Math.abs(valueMin); // 计算实际进度值（偏移最小值）
//        Logger.d("limh", "actualValue:" + actualValue);
        seekBar.setProgress(actualValue); // 设置进度
    }

    /**
     * 获取滑动条当前进度
     * @return 进度值
     */
    public int getProgress() { // 获取滑动条进度
        return seekBar.getProgress(); // 返回当前进度
    }

    /**
     * 判断当前是否为ps类型（滑动条范围大于200）
     * @return true表示ps类型
     */
    public boolean getIsPType() { //获取当前选中是否是 ps
        return seekBarMax > 200; // 滑动条最大值大于200则为ps类型
    }

    //改变光标选中的值 注意边界 以及 小数点
    /**
     * 改变光标选中位置
     * @param temp 位置偏移量
     * @param isRight 是否向右移动
     */
    public void changeSelection(int temp, boolean isRight) { // 改变光标选中位置
//        if (!isShowSeekBar) return;
        if (!isChDelay) return; // 非通道延迟模式不处理
        int selection = getSelection() + temp; // 计算新选中位置
        selection = Math.min(selection, tvShow.getText().length()); // 不超过文本长度
        selection = Math.max(selection, 1); // 不小于1
        if (".".equals(tvShow.getText().toString().substring(selection - 1, selection))) { // 如果新位置是小数点
            if (isRight) { // 向右移动
                selection++; // 跳过小数点
            } else { // 向左移动
                selection--; // 跳过小数点
            }
        }
        tvShow.setSelection(selection, selection - 1); // 设置选中范围（高亮一位）
    }

    /**
     * 获取当前光标选中位置
     * @return 选中位置索引
     */
    public int getSelection() { // 获取选中位置
        int selectStart = tvShow.getSelectionStart(); // 获取选中起始位置
        int selectEnd = tvShow.getSelectionEnd(); // 获取选中结束位置
        if (selectStart != selectEnd) { // 如果有选中范围
            return Math.max(selectStart, selectEnd); // 返回较大位置
        } else { // 没有选中范围
            if (selectStart == 0) { // 如果在起始位置
                return 1; // 返回1
            } else { // 其他位置
                return Math.max(selectStart, selectEnd); // 返回当前位置
            }
        }
    }

    //计算变化之后的值，并处理新的选中位
    /**
     * 计算数值变化后的新值，并更新选中位
     * @param unitTest 单位测试参数（未使用）
     * @param isRight true为增加，false为减少
     */
    public void changeNowUnitNumber(int unitTest, boolean isRight) { // 计算变化后的值
//        if (!isShowSeekBar) return;
        if (!isChDelay) return; // 非通道延迟模式不处理
        String oldText = tvShow.getText().toString(); // 获取当前文本
        int oldLength = oldText.length(); // 当前文本长度
        int pointIndex = oldText.indexOf(".") + 1; // 小数点位置（+1转为1-based）
        int selectIndex = getSelection(); // 当前选中位置
        boolean isReduce = (isSymbolVisible() && isRight) || (!isSymbolVisible() && !isRight); // 判断是否为减少操作
        if(selectIndex== 1 && oldLength > 1 && oldText.charAt(0) == '1' && isReduce) return;//当选中最高位为1时不能减少
        StringBuilder unitNumber = new StringBuilder(""); // 构建单位数字字符串
        for (int i = 1; i <= oldLength; i++) { // 遍历每一位
            unitNumber.append(i == pointIndex ? "." : (i == selectIndex ? "1" : "0")); // 小数点位置放"."，选中位置放"1"，其他放"0"
        }
        BigDecimal unit = new BigDecimal(unitNumber.toString()); // 将单位数字转为BigDecimal
        BigDecimal oldBig = new BigDecimal(oldText); // 将原值转为BigDecimal
        if (isSymbolVisible()) { // 如果有负号
            oldBig = oldBig.multiply(new BigDecimal("-1")); // 乘以-1
        }
        String finalBigStr = isRight ? oldBig.add(unit).toPlainString() : oldBig.subtract(unit).toPlainString(); // 计算增加或减少后的值
        if (finalBigStr.contains("-")) { // 如果结果为负
            finalBigStr = finalBigStr.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 结果为正
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }
        if (finalBigStr.length() > INPUT_LENGTH_LIMIT) { // 如果超过长度限制
            finalBigStr = finalBigStr.substring(0, INPUT_LENGTH_LIMIT); // 截断
        }
        tvShow.setText(finalBigStr); // 设置新文本
        int newLength = tvShow.getText().toString().length(); // 新文本长度
        if (newLength == oldLength) { //位数没变
            tvShow.setSelection(selectIndex, selectIndex - 1); // 选中位不变
        } else if (newLength == oldLength + 1) { //有进位
            tvShow.setSelection(selectIndex + 1, selectIndex); // 选中位右移
        } else if (newLength == oldLength - 1) { //最高位借位
            if (selectIndex - 2 < 0) {//处理类似 100 -> 0 的变化情况
                tvShow.setSelection(1, 0); // 选中第一位
            } else {
                tvShow.setSelection(selectIndex - 1, selectIndex - 2); // 选中位左移
            }
        } else {
            tvShow.setSelection(1, 0); // 默认选中第一位
        }
        clickEnter(); // 触发确认回调
    }


    /**
     * 设置p/n单位模式的数据（带延迟调节）
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatpnData(String number, View fromView, OnDismissListener onDismissListener) { // 设置p/n单位数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        isReuse_INDEX_n_to_M=false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("pn"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器，仅允许p和n
        ((Switch) list.get(INDEX_n)).setText("n"); // 设置n按键文本
        ((Switch) list.get(INDEX_p)).setText("p"); // 设置p按键文本

        (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
        (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮



        ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p单位
        ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n单位
        ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
        ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
        ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位

        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
        if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
            ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p
            number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p
            number=number.replace(" ",""); // 移除空格
            changeSeekBarLimit(true); // 切换滑动条范围为p模式
        } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
            ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n
            number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n
            number =number.replace(" ",""); // 移除空格
            changeSeekBarLimit(false); // 切换滑动条范围为n模式
        }
//        isShowSeekBar = true;
        isChDelay = true; // 设置通道延迟模式
        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        if (isShowSeekBar) { // 如果显示滑动条
            dealInitNumber(number); // 处理初始数字到滑动条
        }
        show(); // 显示键盘
    }

    //处理成seekbar需要的的数据
    /**
     * 将初始数字转换为滑动条进度值
     * @param number 数字字符串
     */
    private void dealInitNumber(String number) { // 处理初始数字到滑动条
        if (number.contains(".")) { // 如果包含小数点
            number = number.split("\\.")[0]; // 只取整数部分
        }
        int temp = (int) Integer.parseInt(number); // 解析为整数
        temp = isSymbolVisible() ? Math.abs(valueMin) - temp : Math.abs(valueMin) + temp; // 根据正负号计算进度值
//        Logger.d("limh", "temp:" + temp);
        seekBar.setProgress(temp); // 设置滑动条进度
    }

    /**
     * 切换滑动条范围限制
     * @param isP true为p模式（范围大），false为n模式（范围小）
     */
    private void changeSeekBarLimit(boolean isP) { // 切换滑动条范围
        valueMax = isP ? pMax : nMax; // 设置最大值
        valueMin = isP ? -pMax : -nMax; // 设置最小值
        seekBarMax = Math.abs(valueMax) + Math.abs(valueMin); // 计算滑动条最大值
        seekBar.setMax(seekBarMax); // 设置滑动条最大值
    }

    /**
     * 将视图移动到指定位置
     * @param view 要移动的视图
     * @param x X坐标
     * @param y Y坐标
     */
    private void moveViewToPosition(View view, int x, int y) { // 移动视图到指定位置
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) view.getLayoutParams(); // 获取布局参数
        if (layoutParams == null) { // 如果参数为空
            layoutParams = new AbsoluteLayout.LayoutParams( // 创建新参数
                    view.getWidth(), // 宽度
                    view.getHeight(), // 高度
                    x, // X坐标
                    y // Y坐标
            );
            view.setLayoutParams(layoutParams); // 设置布局参数
        } else { // 参数不为空
            layoutParams.x = x; // 设置X坐标
            layoutParams.y = y; // 设置Y坐标
            view.setLayoutParams(layoutParams); // 更新布局参数
        }
    }

    /**
     * 判断负号是否可见
     * @return true表示负号可见
     */
    private boolean isSymbolVisible() { // 判断负号是否可见
        return tvSymbol.getVisibility() == View.VISIBLE; // 返回负号可见性
    }

    /**
     * 设置无单位浮点数据
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData_NoUnit(String number, View fromView, OnDismissListener onDismissListener) { // 设置无单位浮点数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        isReuse_INDEX_n_to_M=false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter(""), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器，不允许单位
        ((Switch) list.get(INDEX_n)).setText("n"); // 恢复n按键文本
        ((Switch) list.get(INDEX_p)).setText("p"); // 恢复p按键文本

        (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
        (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮



        ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p单位
        ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
        ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
        ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
        ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位

        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
        if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
            ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p
            number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p
            number=number.replace(" ",""); // 移除空格
        } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
            ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n
            number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n
            number =number.replace(" ",""); // 移除空格
        }

        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }

    /**
     * 设置纯数字浮点数据（无正负号、无单位）
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData_OnlyNum(String number, View fromView, OnDismissListener onDismissListener) { // 设置纯数字浮点数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        isReuse_INDEX_n_to_M=false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter(), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器，仅数字和小数点
        ((Switch) list.get(INDEX_n)).setText("n"); // 恢复n按键文本
        ((Switch) list.get(INDEX_p)).setText("p"); // 恢复p按键文本

        (list.get(INDEX_positive)).setEnabled(false); // 禁用正号按钮
        (list.get(INDEX_negative)).setEnabled(false); // 禁用负号按钮



        ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p单位
        ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
        ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
        ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
        ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位

        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
        if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
            ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p
            number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p
            number=number.replace(" ",""); // 移除空格
        } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
            ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n
            number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n
            number =number.replace(" ",""); // 移除空格
        }

        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }


    /**
     * 设置偏移量模式浮点数据（p按键复用为M）
     * @param number 初始数值字符串
     * @param isUnit 是否启用单位选择
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData_Offset(String number, boolean isUnit, View fromView, OnDismissListener onDismissListener) { // 设置偏移量模式数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        //将n的按键改为大写的M
        ((Switch) list.get(INDEX_p)).setText("M"); // 将p按键文本改为M

        isReuse_INDEX_n_to_M = false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M= true; // 设置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("Mnumk"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器

        ((Switch) list.get(INDEX_n)).setText("n"); // 设置n按键文本

        if (isUnit) { // 如果启用单位选择
            (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
            (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮

            ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p(M)单位
            ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(true); // 启用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位

            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p(M)选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
            if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以M结尾
                ((Switch) list.get(INDEX_p)).setChecked(true); // 选中M
                number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除M
            } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
                ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n
                number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n
            } else if (number.endsWith(list.get(INDEX_u).getText().toString())) { // 如果以u结尾
                ((Switch) list.get(INDEX_u)).setChecked(true); // 选中u
                number = number.replace(list.get(INDEX_u).getText().toString(), ""); // 移除u
            } else if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
                ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m
                number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m
            } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
                ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k
                number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k
            }
            number =number.replace(" ",""); // 移除空格
        } else { // 不启用单位选择
            ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p(M)单位
            ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位
        }
        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }


    /**
     * 设置扩展单位模式浮点数据（n按键复用为M）
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData_Extent(String number, View fromView, OnDismissListener onDismissListener) { // 设置扩展单位模式数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        //将n的按键改为大写的M
        ((Switch) list.get(INDEX_n)).setText("M"); // 将n按键文本改为M
        isReuse_INDEX_n_to_M=true; // 设置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("umkM"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器
        ((Switch) list.get(INDEX_p)).setText("p"); // 恢复p按键文本

        (list.get(INDEX_positive)).setEnabled(false); // 禁用正号按钮
        (list.get(INDEX_negative)).setEnabled(false); // 禁用负号按钮

        ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p单位
        ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n(M)单位
        ((Switch) list.get(INDEX_u)).setEnabled(true); // 启用u单位
        ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位
        ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位

        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n(M)选中
        ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
        if (number.endsWith(list.get(INDEX_u).getText().toString())) { // 如果以u结尾
            ((Switch) list.get(INDEX_u)).setChecked(true); // 选中u
            number = number.replace(list.get(INDEX_u).getText().toString(), ""); // 移除u
        } else if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
            ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m
            number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m
        }else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
            ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k
            number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k
        }else if (number.endsWith(list.get(INDEX_n).getText().toString())){ // 如果以M结尾
            ((Switch)list.get(INDEX_n)).setChecked(true); // 选中M
            number=number.replace(list.get(INDEX_n).getText().toString(),""); // 移除M
        }

        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }

    /**
     * 设置浮点数据（默认启用单位）
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData(String number, View fromView, OnDismissListener onDismissListener) { // 设置浮点数据（默认启用单位）
        setFloatData(number, true, fromView, onDismissListener); // 委托给完整版方法
    }

    /**
     * 设置参考延迟浮点数据
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setRefFloatData(String number, View fromView, OnDismissListener onDismissListener) { // 设置参考延迟浮点数据
        this.isRefDelay = true; // 标记为参考延迟模式
        setFloatData(number, true, fromView, onDismissListener); // 委托给完整版方法
    }

    /**
     * 设置浮点数据（完整版）
     * @param number 初始数值字符串
     * @param isUnit 是否启用单位选择
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setFloatData(String number, boolean isUnit, View fromView, OnDismissListener onDismissListener) { // 设置浮点数据（完整版）
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        isReuse_INDEX_n_to_M=false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M=false; // 重置p复用为M标记

        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("pnumk"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器

        ((Switch) list.get(INDEX_n)).setText("n"); // 设置n按键文本
        ((Switch) list.get(INDEX_p)).setText("p"); // 设置p按键文本

        if (isUnit) { // 如果启用单位选择
            (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
            (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮

            ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p单位
            ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(true); // 启用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位

            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
            if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
                ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p
                number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p
            } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
                ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n
                number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n
            } else if (number.endsWith(list.get(INDEX_u).getText().toString())) { // 如果以u结尾
                ((Switch) list.get(INDEX_u)).setChecked(true); // 选中u
                number = number.replace(list.get(INDEX_u).getText().toString(), ""); // 移除u
            } else if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
                ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m
                number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m
            } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
                ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k
                number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k
            }
            number =number.replace(" ",""); // 移除空格
        } else { // 不启用单位选择
            ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p单位
            ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位
        }
        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }


    /**
     * 设置整数数据
     * @param number 初始数值字符串
     * @param isUnit 是否启用单位选择
     * @param limit 输入长度限制
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调监听器
     */
    public void setNumberData(String number, boolean isUnit, int limit, View fromView, OnDismissListener onDismissListener) { // 设置整数数据
        INPUT_LENGTH_LIMIT = limit; // 设置输入长度限制
        isIntNumber = true; // 标记为整数模式
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        //将n的按键改为大写的M
        ((Switch) list.get(INDEX_p)).setText("M"); // 将p按键文本改为M

        isReuse_INDEX_n_to_M = false; // 重置n复用为M标记
        isReuse_INDEX_p_to_M= true; // 设置p复用为M标记
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("Mnumk"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器

        ((Switch) list.get(INDEX_n)).setText("n"); // 设置n按键文本

        if (isUnit) { // 如果启用单位选择
            (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
            (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮

            ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p(M)单位
            ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位

            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p(M)选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
            if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以M结尾
                ((Switch) list.get(INDEX_p)).setChecked(true); // 选中M
                number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除M
            } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
                ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k
                number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k
            }
            list.get(INDEX_point).setEnabled(false); // 禁用小数点按钮（整数模式）
            number =number.replace(" ",""); // 移除空格
        } else { // 不启用单位选择
            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p(M)选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中

            ((Switch) list.get(INDEX_p)).setEnabled(false); // 禁用p(M)单位
            ((Switch) list.get(INDEX_n)).setEnabled(false); // 禁用n单位
            ((Switch) list.get(INDEX_u)).setEnabled(false); // 禁用u单位
            ((Switch) list.get(INDEX_m)).setEnabled(false); // 禁用m单位
            ((Switch) list.get(INDEX_k)).setEnabled(false); // 禁用k单位
            list.get(INDEX_point).setEnabled(false); // 禁用小数点按钮
        }
        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(number); // 设置初始文本
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 按键网格请求焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }


    //添加操作
    /**
     * 在光标位置插入文本
     * @param add 要插入的文本
     */
    private void addText(String add) { // 插入文本方法
        int index = tvShow.getSelectionStart(); // 获取光标位置
        Editable editable = tvShow.getText(); // 获取可编辑文本
//        直接复制粘贴的问题，需要继承edittext，重写下面这个方法
//        tvShow.onTextContextMenuItem()
        editable.insert(index, add); // 在光标位置插入文本
        int interval = KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10); // 获取数字分组间隔
        tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10)); // 重新计算空格分组
        if ((index + 2) % (interval + 1) == 0 || (index + 1) % (interval + 1) == 0) { // 如果插入位置在分组边界
            tvShow.setSelection(index + 2); // 光标跳过空格
        } else { // 非边界位置
            tvShow.setSelection(index + 1); // 光标移到插入文本后
        }
    }

    //删除操作
    /**
     * 删除光标前一个字符
     */
    private void delText() { // 删除文本方法
        int index = tvShow.getSelectionStart(); // 获取光标位置
        Log.d("Command", "delText index: "+index); // 打印光标位置日志
        if (index > 0) { // 如果光标不在起始位置
            Editable editable = tvShow.getText(); // 获取可编辑文本
            if (index >= 2 && ' ' == editable.charAt(index - 1)) { // 如果光标前是空格
                editable.delete(index - 2, index); // 连同空格前一位一起删除
            } else { // 光标前不是空格
                editable.delete(index - 1, index); // 只删除前一位
            }
            tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10)); // 重新计算空格分组
            if (index >= 2 && index % (KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10) + 1) == 0) { // 如果在分组边界
                tvShow.setSelection(index - 2); // 光标跳过空格
            } else { // 非边界位置
                tvShow.setSelection(index - 1); // 光标前移一位
            }
        }
    }

    /**
     * 判断指定索引是否为数字按键
     * @param index 按键索引
     * @return true表示是数字按键
     */
    public boolean isNumber(int index) { // 判断是否为数字按键
        return index != INDEX_enter && index != INDEX_del && index != INDEX_negative // 不是确认、删除、负号
                && index != INDEX_positive && index != INDEX_point // 不是正号、小数点
                && index != INDEX_p && index != INDEX_n // 不是p、n单位
                && index != INDEX_u && index != INDEX_m && index != INDEX_k; // 不是u、m、k单位
    }

    /**
     * 判断指定索引是否为确认按键
     * @param index 按键索引
     * @return true表示是确认按键
     */
    public boolean isEnter(int index) { // 判断是否为确认按键
        return index == INDEX_enter; // 返回是否为确认索引
    }

    /**
     * 判断指定索引是否为小数点按键
     * @param index 按键索引
     * @return true表示是小数点按键
     */
    public boolean isPoint(int index) { // 判断是否为小数点按键
        return index == INDEX_point; // 返回是否为小数点索引
    }

    /**
     * 判断指定索引是否为删除按键
     * @param index 按键索引
     * @return true表示是删除按键
     */
    public boolean isDelete(int index) { // 判断是否为删除按键
        return index == INDEX_del; // 返回是否为删除索引
    }

    /**
     * 判断指定索引是否为正负号按键
     * @param index 按键索引
     * @return true表示是正负号按键
     */
    public boolean isSymbol(int index) { // 判断是否为正负号按键
        return index == INDEX_positive || index == INDEX_negative; // 返回是否为正号或负号索引
    }

    /**
     * 判断指定索引是否为单位按键
     * @param index 按键索引
     * @return true表示是单位按键
     */
    public boolean isUnit(int index) { // 判断是否为单位按键
        return index == INDEX_p || index == INDEX_n || index == INDEX_u // p、n、u单位
                || index == INDEX_m || index == INDEX_k; // m、k单位
    }

    /**
     * 获取当前选中的单位按键索引
     * @return 单位按键索引，无选中返回-1
     */
    public int hasUnit() { // 获取选中的单位索引
        for (int i = 0; i < list.size(); i++) { // 遍历所有按键
            if (isUnit(i)) { // 如果是单位按键
                if (((Switch) list.get(i)).isEnabled() && ((Switch) list.get(i)).isChecked()) { // 如果启用且选中
                    return i; // 返回该索引
                }
            }
        }
        return -1; // 无选中单位返回-1
    }

    /**
     * 根据文本和单位索引计算实际值
     * @param text 数值文本
     * @param unit 单位索引
     * @return 计算后的double值
     */
    private double getValue(String text, int unit) { // 计算实际值
        double dText = Double.valueOf(text); // 将文本转为double
        double dUnit; // 单位乘数
        switch (unit) { // 根据单位索引选择乘数
            case INDEX_p: // p（皮）
                dUnit = 0.001 * 0.001 * 0.001 * 0.001; // 1e-12
                if (isReuse_INDEX_p_to_M) { // 如果p复用为M
                    dUnit = 1e6; // 1e6
                }
                break;
            case INDEX_n: // n（纳）
                dUnit = 0.001 * 0.001 * 0.001; // 1e-9
                if (isReuse_INDEX_n_to_M){ // 如果n复用为M
                    dUnit=1e6; // 1e6
                }
                break;
            case INDEX_u: // u（微）
                dUnit = 0.001 * 0.001; // 1e-6
                break;
            case INDEX_m: // m（毫）
                dUnit = 0.001; // 1e-3
                break;
            case INDEX_k: // k（千）
                dUnit = 1000; // 1e3
                break;
            default: // 默认无单位
                dUnit = 1; // 乘数为1
                break;
        }
        return DoubleUtil.mul(dText, dUnit); // 返回文本值乘以单位值
    }

    /**
     * 将double值格式化为整数结果字符串（带单位）
     * @param d double值
     * @return 格式化后的整数结果字符串
     */
    private String getIntValueFromD(double d) { // 格式化整数值
        if (DoubleUtil.compareTo(d, 0d) < 0) { // 如果为负数
            return "-" + getIntValueFromD(DoubleUtil.mul(d, -1d)); // 递归处理绝对值，加负号
        } else if (DoubleUtil.compareTo(d, 0d) == 0) { // 如果为0
            return "1 "; // 返回1（最小值）
        } else if (DoubleUtil.compareTo(d, 1_000_000d) >= 0 && (isReuse_INDEX_n_to_M || isReuse_INDEX_p_to_M)) { // 大于等于1M且M单位可用
            if (DoubleUtil.compareTo(d, 5d * 1000 * 1000) >= 0) { // 大于等于5M
                return "5 M"; // 返回最大值5M
            } else { // 1M到5M之间
                d = DoubleUtil.mul(d, 1e-6); // 转换为M单位
                return get8Bit(d) + " M"; // 返回8位精度+M单位
            }
        } else if (DoubleUtil.compareTo(d, 1000d) >= 0) { // 大于等于1k
            if (DoubleUtil.compareTo(d, 1000d * 1000 * 1000) >= 0) { // 大于等于1G
                return "999 k"; // 返回最大值999k
            } else { // 1k到1G之间
                d = DoubleUtil.mul(d, 0.001); // 转换为k单位
                return get7Bit(d) + " k"; // 返回7位精度+k单位
            }
        } else { // 小于1k
            return get5Bit(d) + " "; // 返回5位精度+空格
        }
    }

    /**
     * 格式化为8位精度字符串
     * @param d double值
     * @return 格式化后的字符串
     */
    private String get8Bit(double d) { // 8位精度格式化
        String s = String.valueOf(d); // 转为字符串
        if (s.length() >= 8) { // 如果长度超过8
            s = s.substring(0, 8); // 截取前8位
        }
        if (s.contains(".")) { // 如果包含小数点
            while (s.endsWith("0")) { // 移除末尾0
                s = s.substring(0, s.length() - 1); // 移除一个0
            }
            if (s.endsWith(".")) { // 如果小数点后全为0
                s = s.replace(".", ""); // 移除小数点
            }
        }
        return s; // 返回格式化后的字符串
    }

    /**
     * 格式化为7位精度字符串
     * @param d double值
     * @return 格式化后的字符串
     */
    private String get7Bit(double d) { // 7位精度格式化
        String s = String.valueOf(d); // 转为字符串
        if (s.length() >= 7) { // 如果长度超过7
            s = s.substring(0, 7); // 截取前7位
        }
        if (s.contains(".")) { // 如果包含小数点
            while (s.endsWith("0")) { // 移除末尾0
                s = s.substring(0, s.length() - 1); // 移除一个0
            }
            if (s.endsWith(".")) { // 如果小数点后全为0
                s = s.replace(".", ""); // 移除小数点
            }
        }
        return s; // 返回格式化后的字符串
    }




    /**
     * 将double值格式化为浮点结果字符串（带单位）
     * @param d double值
     * @return 格式化后的浮点结果字符串
     */
    private String getValueFromD(double d) { // 格式化浮点值
        if (DoubleUtil.compareTo(d, 0d) < 0) { // 如果为负数
            return "-" + getValueFromD(DoubleUtil.mul(d, -1d)); // 递归处理绝对值，加负号
        } else if (DoubleUtil.compareTo(d, 0d) == 0) { // 如果为0
            return "0 "; // 返回0
        }else if (DoubleUtil.compareTo(d,1000000d)>=0 && (isReuse_INDEX_n_to_M || isReuse_INDEX_p_to_M)){ // 大于等于1M且M单位可用
            if (DoubleUtil.compareTo(d,1000d*1000*1000)>=0){ // 大于等于1G
                return "999 M"; // 返回最大值999M
            }else { // 1M到1G之间
                d=DoubleUtil.mul(d,1e-6); // 转换为M单位
                return get5Bit(d)+" M"; // 返回5位精度+M单位
            }
        }
        else if (DoubleUtil.compareTo(d, 1000d) >= 0) { // 大于等于1k
            if (list.get(INDEX_k).isEnabled() &&  (!list.get(INDEX_p).isEnabled() || isReuse_INDEX_p_to_M)) { // k单位可用且p不可用或p复用为M
                if (DoubleUtil.compareTo(d, 1000d * 1000) >= 0) { // 大于等于1M
                    return "999 k"; // 返回最大值999k
                } else { // 1k到1M之间
                    d = DoubleUtil.mul(d, 0.001); // 转换为k单位
                    return get5Bit(d) + " k"; // 返回5位精度+k单位
                }
            } else if (isRefDelay) { // 参考延迟模式
                if (DoubleUtil.compareTo(d, 12d * 1000) >= 0) { // 大于等于12k
                    return "12 k"; // 返回最大值12k
                } else { // 1k到12k之间
                    d = DoubleUtil.mul(d, 0.001); // 转换为k单位
                    return get5Bit(d) + " k"; // 返回5位精度+k单位
                }
            } else { // 其他情况
                return "1 k"; // 返回最小值1k
            }
        } else if (DoubleUtil.compareTo(d, 1d) >= 0) { // 大于等于1
            return get5Bit(d)+" "; // 返回5位精度+空格
        } else if (DoubleUtil.compareTo(d, 0.001) >= 0) { // 大于等于1m
            d = DoubleUtil.mul(d, 1000d); // 转换为m单位
            return get5Bit(d) + " m"; // 返回5位精度+m单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001) >= 0) { // 大于等于1u
            d = DoubleUtil.mul(d, 1000d * 1000d); // 转换为u单位
            return get5Bit(d) + " μ"; // 返回5位精度+μ单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001) >= 0) { // 大于等于1n
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d); // 转换为n单位
            String s = get5Bit(d); // 获取5位精度
            if (s.contains(".")) { // 如果包含小数点
                String[] split = s.split("\\."); // 按小数点分割
                if (split[1].length() > 3) { // 小数部分超过3位
                    split[1] = split[1].substring(0, 3); // 截取前3位
                    s = split[0] + "." + split[1]; // 重新拼接
                }
            }
            return s + " n"; // 返回+n单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001) >= 0) { // 大于等于1p
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d); // 转换为p单位
            return get5Bit(((int) d)) + " p"; // 返回整数+p单位
        } else { // 小于1p
            return "1 p"; // 返回最小值1p
        }
    }

    /**
     * 格式化为5位精度字符串
     * @param d double值
     * @return 格式化后的字符串
     */
    private String get5Bit(double d) { // 5位精度格式化
        String s = String.valueOf(d); // 转为字符串
        if (s.length() >= 6) { // 如果长度超过6（含小数点）
            s = s.substring(0, 6); // 截取前6位
        }
        if (s.contains(".")) { // 如果包含小数点
            while (s.endsWith("0")) { // 移除末尾0
                s = s.substring(0, s.length() - 1); // 移除一个0
            }
            if (s.endsWith(".")) { // 如果小数点后全为0
                s = s.replace(".", ""); // 移除小数点
            }
        }
        return s; // 返回格式化后的字符串
    }

    /**
     * 按键点击监听器
     */
    private OnClickListener onItemClickListener = new OnClickListener() { // 按键点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            int index = Integer.parseInt((String) v.getTag()); // 获取按键索引
            String str = ((Button) v).getText().toString(); // 获取按键文本

            if (isNumber(index)) { // 如果是数字按键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                    tvShow.setText(""); // 清空输入
                }
                if (tvShow.getText().toString().equals("0")) { // 如果当前是0
                    delText(); // 先删除0
                }
                if (tvShow.getText().toString().length() >= INPUT_LENGTH_LIMIT) { // 如果超过长度限制
                    return; // 不处理
                }
                addText(str); // 插入数字
            } else if (isDelete(index)) { // 如果是删除按键
                if (StrUtil.isEmpty(tvShow.getText().toString())) { // 如果输入为空
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                } else { // 输入非空
                    delText(); // 删除一个字符
                }
            } else if (isSymbol(index)) { // 如果是正负号按键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                    tvShow.setText(""); // 清空输入
                }
                if (index == INDEX_negative) { // 如果是负号
                    tvSymbol.setVisibility(VISIBLE); // 显示负号
                } else if (index == INDEX_positive) { // 如果是正号
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
            } else if (isUnit(index)) { // 如果是单位按键
                if (!((Switch) list.get(index)).isChecked()) { // 如果当前未选中（取消选中）
                    ((Switch) list.get(index)).setChecked(false); // 保持未选中
                } else { // 当前选中了新单位
                    for (int i = 0; i < list.size(); i++) { // 遍历所有按键
                        if (isUnit(i)) { // 如果是单位按键
                            if (index == i) { // 当前按键
                                ((Switch) list.get(i)).setChecked(true); // 选中
                            } else { // 其他单位按键
                                ((Switch) list.get(i)).setChecked(false); // 取消选中（互斥）
                            }
                        }
                    }
                }
                if (isShowSeekBar) { // 如果显示滑动条
                    if (((Switch) list.get(INDEX_p)).isChecked()) { // 如果p选中
                        changeSeekBarLimit(true); // 切换为p模式范围
                    } else if (((Switch) list.get(INDEX_n)).isChecked()) { // 如果n选中
                        changeSeekBarLimit(false); // 切换为n模式范围
                    }
                    dealInitNumber(tvShow.getText().toString()); // 更新滑动条进度
                    clickEnter(); // 触发确认回调
                }
            } else if (isPoint(index)) { // 如果是小数点按键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                    tvShow.setText("0"); // 先输入0
                    tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
                if (tvShow.getText().toString().isEmpty()) { // 如果输入为空
                    tvShow.setText("0"); // 先输入0
                    tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
                if (tvShow.getText().toString().length() >= INPUT_LENGTH_LIMIT) { // 如果超过长度限制
                    return; // 不处理
                }
                if (!tvShow.getText().toString().contains(str)) { // 如果不包含小数点
                    addText(str); // 插入小数点
                }
            } else if (isEnter(index)) { // 如果是确认按键
                if (onDismissListener != null) { // 如果回调不为空
                    clickEnter(); // 触发确认回调
                }
                hide(); // 隐藏键盘
            }
        }
    };

    /**
     * 确认回调处理，格式化输入值并回调
     */
    private void clickEnter() { // 确认回调方法
        String text = tvShow.getText().toString(); // 获取输入文本
        if (!StrUtil.isEmpty(text)) { // 如果文本非空
            if (text.startsWith(".")) { // 如果以小数点开头
                text = "0" + text; // 前面补0
            }
            if (text.endsWith(".")) { // 如果以小数点结尾
                text = text.replace(".", ""); // 移除小数点
                if (StrUtil.isEmpty(text)) { // 如果移除后为空
                    text = "0"; // 设为0
                }
            }
            if (isIntNumber) { // 如果是整数模式
                if (isSymbolVisible() && !"0".equals(text)) { // 如果有负号且非0
                    text = "-" + text; // 添加负号
                }
                if (((Switch) list.get(INDEX_p)).isChecked() || ((Switch) list.get(INDEX_k)).isChecked()) { //M k // 如果M或k单位选中
                    int hasUnit = hasUnit(); // 获取选中单位索引
                    onDismissListener.onDismiss(fromView, getIntValueFromD((int) getValue(text, hasUnit))); // 回调整数格式化值
                } else { // 无单位选中
                    onDismissListener.onDismiss(fromView, getIntValueFromD((int) getValue(text, 1))); // 回调整数格式化值（无单位）
                }
            } else { // 浮点模式
                if (((Switch) list.get(INDEX_n)).isChecked() && !((Switch) list.get(INDEX_u)).isChecked() && !isRefDelay  && !isReuse_INDEX_n_to_M) { // n单位选中且非参考延迟且非M复用
                    double max = 500 * 0.001 * 0.001 * 0.001; // n模式最大值500ns
                   // double max = 100 * 0.001 * 0.001 * 0.001;
                    if (DoubleUtil.compareTo(getValue(text, hasUnit()), max) > 0) { // 如果超过最大值
                       // text = "100";
                        text = "500"; // 限制为500
                        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p选中
                        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n选中
                        ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u选中
                        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m选中
                        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k选中
                        ((Switch) list.get(INDEX_n)).setChecked(true); // 重新选中n
                    }
                }
                if (isSymbolVisible() && !"0".equals(text)) { // 如果有负号且非0
                    text = "-" + text; // 添加负号
                }
                if (((Switch)list.get(INDEX_p)).isChecked() // 如果p单位选中
                        || ((Switch)list.get(INDEX_n)).isChecked() // 或n单位选中
                        || ((Switch)list.get(INDEX_u)).isChecked() // 或u单位选中
                        || ((Switch)list.get(INDEX_m)).isChecked() // 或m单位选中
                        || ((Switch)list.get(INDEX_k)).isChecked()) { // 或k单位选中
                    int hasUnit = hasUnit(); // 获取选中单位索引
                    onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, hasUnit))); // 回调浮点格式化值
                } else { // 无单位选中
                    onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, 1))); // 回调浮点格式化值（无单位）
                }
            }
        } else { // 文本为空
            onDismissListener.onDismiss(fromView, String.valueOf(0)); // 回调0
        }
    }

    /**
     * 删除键长按监听器，清空输入
     */
    private OnLongClickListener onLongClickListener = new OnLongClickListener() { // 长按监听器
        @Override
        public boolean onLongClick(View v) { // 长按回调
            if (!StrUtil.isEmpty(tvShow.getText().toString())) { // 如果输入非空
                PlaySound.getInstance().playButton(); // 播放按钮音效
                tvShow.setText("0"); // 重置为0
                tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
            }
            return false; // 不消费长按事件
        }
    };

    /**
     * 滑动条变化监听器
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // 滑动条监听器
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 进度变化回调
            //这里处理seekBar的数据变化
//            ScreenUtil.getViewLocation(seekBar);
            if (fromUser || isFromUser) { // 如果来自用户操作
                int actualValue = progress - Math.abs(valueMin); // 计算实际值（偏移最小值）
//                Logger.d("limh", "progress=" + progress + ",actualValue=" + actualValue);
                if(actualValue < 0) { // 如果实际值为负
                    actualValue = Math.abs(actualValue); // 取绝对值
                    tvSymbol.setVisibility(View.VISIBLE); // 显示负号
                } else { // 实际值为正
                    tvSymbol.setVisibility(View.INVISIBLE); // 隐藏负号
                }
                tvShow.setText(String.valueOf(actualValue)); // 更新输入文本
                tvShow.setSelection(tvShow.getText().length()); // 光标移到末尾
                clickEnter(); // 触发确认回调
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始触摸滑动条（空实现）

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止触摸滑动条（空实现）

        }
    };

    /**
     * 格式化double值，取消科学计数法
     * @param d double值
     * @return 格式化后的字符串
     */
    private static String formatDouble(double d) { // 格式化double值
        NumberFormat nf = NumberFormat.getInstance(); // 获取数字格式化器
        //设置保留多少位小数
        nf.setMaximumFractionDigits(20); // 最多20位小数
        // 取消科学计数法
        nf.setGroupingUsed(false); // 不使用分组
        //返回结果
        return nf.format(d); // 返回格式化结果
    }
}
