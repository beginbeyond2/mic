package com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat; // 包声明：全功能浮点键盘子包

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
 * │ 模块定位：全功能浮点键盘 - 浮点数输入对话框（含f/T/G/M全单位）      │
 * │ 核心职责：提供浮点数输入的虚拟键盘，支持f/p/n/u/m/k/M/G/T全单位选择 │
 * │ 架构设计：继承AbsoluteLayout，实现IFullKeyBoardFloat接口，         │
 * │          24按键布局（4行6列），无SeekBar，支持逐位编辑              │
 * │ 数据流向：setFloatData* → 用户输入 → onDismiss回调返回格式化值     │
 * │ 依赖关系：IFullKeyBoardFloat, KeyBoardNumberUtil, RxBus           │
 * │ 使用场景：需要全范围单位（f~T）的参数输入，如频率、时间等            │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/4/28.
 */
public class TopDialogFullFloatKeyBoard extends AbsoluteLayout implements IFullKeyBoardFloat { // 全功能浮点键盘对话框
    private static final String TAG = "TopDialogFullFloatKeyBoard"; // 日志标签
    private Context context; // 上下文对象
    private View fromView; // 触发键盘的源视图
    private LinearLayout showLayout; // 显示区域布局
    private TextView tvSymbol; // 正负号文本视图
    private EditText tvShow; // 输入显示编辑框
    private RelativeLayout gridView; // 按键网格布局
    private ArrayList<Button> list; // 按键按钮列表
    private OnDismissListener onDismissListener; // 关闭回调监听器
    private boolean first = false;//是否是本次打开后第一点击 // 首次点击标记
    private boolean isFromUser = false; // 是否来自用户操作
    public static final int INPUT_LENGTH_LIMIT = 15; // 输入长度限制


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
    public TopDialogFullFloatKeyBoard(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogFullFloatKeyBoard(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogFullFloatKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
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
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_full_keyboard_float, this); // 加载全功能浮点键盘布局

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
                    char[] unit = {'f','p', 'n', 'u', 'm', 'k', 'M', 'G', 'T'}; // 全单位字符数组（含f/G/T）
                    double[] unitVal = {1e-15,1e-12, 1e-9, 1e-6, 1e-3, 1e3, 1e6, 1e9, 1e12}; // 全单位值数组
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
                if (((Switch) list.get(INDEX_f)).isChecked() // 如果f单位选中
                        || ((Switch) list.get(INDEX_p)).isChecked() // 或p单位选中
                        || ((Switch) list.get(INDEX_n)).isChecked() // 或n单位选中
                        || ((Switch) list.get(INDEX_u)).isChecked() // 或u单位选中
                        || ((Switch) list.get(INDEX_m)).isChecked() // 或m单位选中
                        || ((Switch) list.get(INDEX_k)).isChecked() // 或k单位选中
                        || ((Switch) list.get(INDEX_M)).isChecked() // 或M单位选中
                        || ((Switch) list.get(INDEX_G)).isChecked() // 或G单位选中
                        || ((Switch) list.get(INDEX_T)).isChecked() // 或T单位选中
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
     * 获取当前选中的单位乘数值（全单位版本）
     * @return 单位乘数值
     */
    private double getUnitVal() { // 获取单位值方法
        double unitVal = 1; // 默认单位值为1
        if (((Switch) list.get(INDEX_f)).isChecked()) { // f（飞）单位选中
            unitVal = 1e-15; // 飞
        }
        if (((Switch) list.get(INDEX_p)).isChecked()) { // p（皮）单位选中
            unitVal = 1e-12; // 皮
        }
        if (((Switch) list.get(INDEX_n)).isChecked()) { // n（纳）单位选中
            unitVal = 1e-9; // 纳
        }
        if (((Switch) list.get(INDEX_u)).isChecked()) { // u（微）单位选中
            unitVal = 1e-6; // 微
        }
        if (((Switch) list.get(INDEX_m)).isChecked()) { // m（毫）单位选中
            unitVal = 1e-3; // 毫
        }
        if (((Switch) list.get(INDEX_k)).isChecked()) { // k（千）单位选中
            unitVal = 1e3; // 千
        }
        if (((Switch) list.get(INDEX_M)).isChecked()) { // M（兆）单位选中
            unitVal = 1e6; // 兆
        }
        if (((Switch) list.get(INDEX_G)).isChecked()) { // G（吉）单位选中
            unitVal = 1e9; // 吉
        }
        if (((Switch) list.get(INDEX_T)).isChecked()) { // T（太）单位选中
            unitVal = 1e12; // 太
        }
        return unitVal; // 返回单位值
    }

    /**
     * 显示键盘
     */
    public void show() { // 显示方法
        moveViewToPosition(showLayout, 0, context.getResources().getDimensionPixelOffset(R.dimen.rightDialogFloatKeyBoardRow0)); // 移动显示区域到指定位置
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FLOATKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogFullFloatKeyBoard",rootViewGroup); // 打印控件位置信息
    }

    /**
     * 隐藏键盘
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FLOATKEYBOARD); // 发送对话框关闭事件
    }



    //改变光标选中的值 注意边界 以及 小数点
    /**
     * 改变光标选中的位置，跳过小数点
     * @param temp 偏移量（+1或-1）
     * @param isRight 是否向右移动
     */
    public void changeSelection(int temp, boolean isRight) { // 改变选中位置方法
        int selection = getSelection() + temp; // 计算新选中位置
        selection = Math.min(selection, tvShow.getText().length()); // 不超过文本长度
        selection = Math.max(selection, 1); // 不小于1
        if (".".equals(tvShow.getText().toString().substring(selection - 1, selection))) { // 如果新位置是小数点
            if (isRight) { // 向右移动
                selection++; // 再右移一位跳过小数点
            } else { // 向左移动
                selection--; // 再左移一位跳过小数点
            }
        }
        tvShow.setSelection(selection, selection - 1); // 设置选中范围（单字符高亮）
    }

    /**
     * 获取当前光标选中位置
     * @return 选中位置的较大端
     */
    public int getSelection() { // 获取选中位置方法
        int selectStart = tvShow.getSelectionStart(); // 获取选中起始位置
        int selectEnd = tvShow.getSelectionEnd(); // 获取选中结束位置
        if (selectStart != selectEnd) { // 如果有选中范围
            return Math.max(selectStart, selectEnd); // 返回较大端
        } else { // 没有选中范围
            if (selectStart == 0) { // 如果光标在最前面
                return 1; // 返回1（避免0位置）
            } else { // 光标在中间
                return Math.max(selectStart, selectEnd); // 返回光标位置
            }
        }
    }

    //计算变化之后的值，并处理新的选中位
    /**
     * 逐位增减当前值，并处理进位/借位后的选中位
     * @param unitTest 未使用参数
     * @param isRight true为增加，false为减少
     */
    public void changeNowUnitNumber(int unitTest, boolean isRight) { // 逐位增减方法
        String oldText = tvShow.getText().toString(); // 获取当前文本
        int oldLength = oldText.length(); // 当前文本长度
        int pointIndex = oldText.indexOf(".") + 1; // 小数点位置（+1转为1起始索引）
        int selectIndex = getSelection(); // 当前选中位
        boolean isReduce = (isSymbolVisible() && isRight) || (!isSymbolVisible() && !isRight); // 判断是否为减少操作
        if(selectIndex== 1 && oldLength > 1 && oldText.charAt(0) == '1' && isReduce) return;//当选中最高位为1时不能减少
        StringBuilder unitNumber = new StringBuilder(""); // 构建单位数字字符串
        for (int i = 1; i <= oldLength; i++) { // 遍历每一位
            unitNumber.append(i == pointIndex ? "." : (i == selectIndex ? "1" : "0")); // 选中位为1，小数点位为.，其余为0
        }
        BigDecimal unit = new BigDecimal(unitNumber.toString()); // 单位值（如0.01或10）
        BigDecimal oldBig = new BigDecimal(oldText); // 当前值
        if (isSymbolVisible()) { // 如果有负号
            oldBig = oldBig.multiply(new BigDecimal("-1")); // 取负
        }
        String finalBigStr = isRight ? oldBig.add(unit).toPlainString() : oldBig.subtract(unit).toPlainString(); // 增加或减少单位值
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
            tvShow.setSelection(selectIndex, selectIndex - 1); // 保持原选中位
        } else if (newLength == oldLength + 1) { //有进位
            tvShow.setSelection(selectIndex + 1, selectIndex); // 选中位右移一位
        } else if (newLength == oldLength - 1) { //最高位借位
            if (selectIndex - 2 < 0) {//处理类似 100 -> 0 的变化情况
                tvShow.setSelection(1, 0); // 选中第一个字符
            } else {
                tvShow.setSelection(selectIndex - 1, selectIndex - 2); // 选中位左移一位
            }
        } else {
            tvShow.setSelection(1, 0); // 默认选中第一个字符
        }
        clickEnter(); // 触发确认回调
    }


    /**
     * 将视图移动到绝对布局中的指定位置
     * @param view 要移动的视图
     * @param x 横坐标
     * @y 纵坐标
     */
    private void moveViewToPosition(View view, int x, int y) { // 移动视图到指定位置方法
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) view.getLayoutParams(); // 获取布局参数
        if (layoutParams == null) { // 如果布局参数为空
            layoutParams = new AbsoluteLayout.LayoutParams( // 创建新布局参数
                    view.getWidth(), // 宽度
                    view.getHeight(), // 高度
                    x, // 横坐标
                    y // 纵坐标
            );
            view.setLayoutParams(layoutParams); // 设置布局参数
        } else { // 布局参数不为空
            layoutParams.x = x; // 设置横坐标
            layoutParams.y = y; // 设置纵坐标
            view.setLayoutParams(layoutParams); // 更新布局参数
        }
    }

    /**
     * 判断负号是否可见
     * @return true表示负号可见
     */
    private boolean isSymbolVisible() { // 判断负号可见方法
        return tvSymbol.getVisibility() == View.VISIBLE; // 返回负号可见性
    }


    /**
     * 设置数据为扩展模式（禁用正负号按钮，启用全单位）
     * @param number 初始数值字符串
     * @param fromView 触发键盘的源视图
     * @param onDismissListener 关闭回调
     */
    public void setFloatData_Extent(String number, View fromView, OnDismissListener onDismissListener) { // 扩展模式设置数据
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("fpnumkMGT"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器（全单位+长度限制）

        (list.get(INDEX_positive)).setEnabled(false); // 禁用正号按钮
        (list.get(INDEX_negative)).setEnabled(false); // 禁用负号按钮

        ((Switch) list.get(INDEX_f)).setEnabled(true); // 启用f单位开关
        ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p单位开关
        ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n单位开关
        ((Switch) list.get(INDEX_u)).setEnabled(true); // 启用u单位开关
        ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位开关
        ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位开关
        ((Switch) list.get(INDEX_M)).setEnabled(true); // 启用M单位开关
        ((Switch) list.get(INDEX_G)).setEnabled(true); // 启用G单位开关
        ((Switch) list.get(INDEX_T)).setEnabled(true); // 启用T单位开关


        ((Switch) list.get(INDEX_f)).setChecked(false); // 取消f单位选中
        ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p单位选中
        ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n单位选中
        ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u单位选中
        ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m单位选中
        ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k单位选中
        ((Switch) list.get(INDEX_M)).setChecked(false); // 取消M单位选中
        ((Switch) list.get(INDEX_G)).setChecked(false); // 取消G单位选中
        ((Switch) list.get(INDEX_T)).setChecked(false); // 取消T单位选中

        if (number.endsWith(list.get(INDEX_f).getText().toString())) { // 如果以f结尾
            ((Switch) list.get(INDEX_f)).setChecked(true); // 选中f单位
            number = number.replace(list.get(INDEX_f).getText().toString(), ""); // 移除f后缀
        } else if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
            ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p单位
            number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p后缀
        } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
            ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n单位
            number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n后缀
        } else if (number.endsWith(list.get(INDEX_u).getText().toString())) { // 如果以u结尾
            ((Switch) list.get(INDEX_u)).setChecked(true); // 选中u单位
            number = number.replace(list.get(INDEX_u).getText().toString(), ""); // 移除u后缀
        } else if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
            ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m单位
            number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m后缀
        } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
            ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k单位
            number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k后缀
        } else if (number.endsWith(list.get(INDEX_M).getText().toString())) { // 如果以M结尾
            ((Switch) list.get(INDEX_M)).setChecked(true); // 选中M单位
            number = number.replace(list.get(INDEX_M).getText().toString(), ""); // 移除M后缀
        } else if (number.endsWith(list.get(INDEX_G).getText().toString())) { // 如果以G结尾
            ((Switch) list.get(INDEX_G)).setChecked(true); // 选中G单位
            number = number.replace(list.get(INDEX_G).getText().toString(), ""); // 移除G后缀
        } else if (number.endsWith(list.get(INDEX_T).getText().toString())) { // 如果以T结尾
            ((Switch) list.get(INDEX_T)).setChecked(true); // 选中T单位
            number = number.replace(list.get(INDEX_T).getText().toString(), ""); // 移除T后缀
        }

        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求输入框焦点
        tvShow.setText(number); // 设置显示数值
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 请求按键网格焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }

    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param fromView          触发键盘的源视图
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(String number, View fromView, OnDismissListener onDismissListener) { // 设置浮点数据（默认启用单位）
        setFloatData(number, true, fromView, onDismissListener); // 委托给带单位开关的方法
    }

    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param isUnit            是否启用单位选择
     * @param fromView          触发键盘的源视图
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(String number, boolean isUnit, View fromView, OnDismissListener onDismissListener) { // 设置浮点数据（含单位开关）
        this.fromView = fromView; // 保存源视图
        if (number.contains("-")) { // 如果包含负号
            number = number.replace("-", ""); // 移除负号
            tvSymbol.setVisibility(VISIBLE); // 显示负号
        } else { // 不包含负号
            tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
        }

        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("fpnumkMGT"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)}); // 设置输入过滤器（全单位+长度限制）


        if (isUnit) { // 如果启用单位选择
            (list.get(INDEX_positive)).setEnabled(true); // 启用正号按钮
            (list.get(INDEX_negative)).setEnabled(true); // 启用负号按钮

            ((Switch) list.get(INDEX_f)).setEnabled(true); // 启用f单位开关
            ((Switch) list.get(INDEX_p)).setEnabled(true); // 启用p单位开关
            ((Switch) list.get(INDEX_n)).setEnabled(true); // 启用n单位开关
            ((Switch) list.get(INDEX_u)).setEnabled(true); // 启用u单位开关
            ((Switch) list.get(INDEX_m)).setEnabled(true); // 启用m单位开关
            ((Switch) list.get(INDEX_k)).setEnabled(true); // 启用k单位开关
            ((Switch) list.get(INDEX_M)).setEnabled(true); // 启用M单位开关
            ((Switch) list.get(INDEX_G)).setEnabled(true); // 启用G单位开关
            ((Switch) list.get(INDEX_T)).setEnabled(true); // 启用T单位开关


            ((Switch) list.get(INDEX_f)).setChecked(false); // 取消f单位选中
            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p单位选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n单位选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u单位选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m单位选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k单位选中
            ((Switch) list.get(INDEX_M)).setChecked(false); // 取消M单位选中
            ((Switch) list.get(INDEX_G)).setChecked(false); // 取消G单位选中
            ((Switch) list.get(INDEX_T)).setChecked(false); // 取消T单位选中

            if (number.endsWith(list.get(INDEX_f).getText().toString())) { // 如果以f结尾
                ((Switch) list.get(INDEX_f)).setChecked(true); // 选中f单位
                number = number.replace(list.get(INDEX_f).getText().toString(), ""); // 移除f后缀
            } else if (number.endsWith(list.get(INDEX_p).getText().toString())) { // 如果以p结尾
                ((Switch) list.get(INDEX_p)).setChecked(true); // 选中p单位
                number = number.replace(list.get(INDEX_p).getText().toString(), ""); // 移除p后缀
            } else if (number.endsWith(list.get(INDEX_n).getText().toString())) { // 如果以n结尾
                ((Switch) list.get(INDEX_n)).setChecked(true); // 选中n单位
                number = number.replace(list.get(INDEX_n).getText().toString(), ""); // 移除n后缀
            } else if (number.endsWith(list.get(INDEX_u).getText().toString())) { // 如果以u结尾
                ((Switch) list.get(INDEX_u)).setChecked(true); // 选中u单位
                number = number.replace(list.get(INDEX_u).getText().toString(), ""); // 移除u后缀
            } else if (number.endsWith(list.get(INDEX_m).getText().toString())) { // 如果以m结尾
                ((Switch) list.get(INDEX_m)).setChecked(true); // 选中m单位
                number = number.replace(list.get(INDEX_m).getText().toString(), ""); // 移除m后缀
            } else if (number.endsWith(list.get(INDEX_k).getText().toString())) { // 如果以k结尾
                ((Switch) list.get(INDEX_k)).setChecked(true); // 选中k单位
                number = number.replace(list.get(INDEX_k).getText().toString(), ""); // 移除k后缀
            } else if (number.endsWith(list.get(INDEX_M).getText().toString())) { // 如果以M结尾
                ((Switch) list.get(INDEX_M)).setChecked(true); // 选中M单位
                number = number.replace(list.get(INDEX_M).getText().toString(), ""); // 移除M后缀
            } else if (number.endsWith(list.get(INDEX_G).getText().toString())) { // 如果以G结尾
                ((Switch) list.get(INDEX_G)).setChecked(true); // 选中G单位
                number = number.replace(list.get(INDEX_G).getText().toString(), ""); // 移除G后缀
            } else if (number.endsWith(list.get(INDEX_T).getText().toString())) { // 如果以T结尾
                ((Switch) list.get(INDEX_T)).setChecked(true); // 选中T单位
                number = number.replace(list.get(INDEX_T).getText().toString(), ""); // 移除T后缀
            }
            number =number.replace(" ",""); // 移除空格
        } else { // 不启用单位选择
            ((Switch) list.get(INDEX_f)).setChecked(false); // 取消f单位选中
            ((Switch) list.get(INDEX_p)).setChecked(false); // 取消p单位选中
            ((Switch) list.get(INDEX_n)).setChecked(false); // 取消n单位选中
            ((Switch) list.get(INDEX_u)).setChecked(false); // 取消u单位选中
            ((Switch) list.get(INDEX_m)).setChecked(false); // 取消m单位选中
            ((Switch) list.get(INDEX_k)).setChecked(false); // 取消k单位选中
            ((Switch) list.get(INDEX_M)).setChecked(false); // 取消M单位选中
            ((Switch) list.get(INDEX_G)).setChecked(false); // 取消G单位选中
            ((Switch) list.get(INDEX_T)).setChecked(false); // 取消T单位选中
        }
        first = true; // 设置首次点击标记
        tvShow.requestFocus(); // 请求输入框焦点
        tvShow.setText(number); // 设置显示数值
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length())); // 设置光标位置
        gridView.requestFocus(); // 请求按键网格焦点
        this.onDismissListener = onDismissListener; // 保存回调
        show(); // 显示键盘
    }

    //添加操作
    /**
     * 在当前光标位置插入字符
     * @param add 要插入的字符
     */
    private void addText(String add) { // 插入字符方法
        int index = tvShow.getSelectionStart(); // 获取当前光标位置
        Editable editable = tvShow.getText(); // 获取可编辑文本
//        直接复制粘贴的问题，需要继承edittext，重写下面这个方法
//        tvShow.onTextContextMenuItem()
        editable.insert(index, add); // 在光标位置插入字符
        int interval = KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10); // 获取千分位间隔
        tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10)); // 重新计算千分位空格
        if ((index + 2) % (interval + 1) == 0 || (index + 1) % (interval + 1) == 0) { // 如果光标在空格位置
            tvShow.setSelection(index + 2); // 跳过空格
        } else { // 不在空格位置
            tvShow.setSelection(index + 1); // 正常后移一位
        }
    }

    //删除操作
    /**
     * 删除当前光标前的一个字符
     */
    private void delText() { // 删除字符方法
        int index = tvShow.getSelectionStart(); // 获取当前光标位置
        Log.d("Command", "delText index: "+index); // 打印光标位置日志
        if (index > 0) { // 如果光标不在最前面
            Editable editable = tvShow.getText(); // 获取可编辑文本
            if (index >= 2 && ' ' == editable.charAt(index - 1)) { // 如果光标前是空格
                editable.delete(index - 2, index); // 删除空格及其前面的字符
            } else { // 光标前不是空格
                editable.delete(index - 1, index); // 删除前一个字符
            }
            tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10)); // 重新计算千分位空格
            if (index >= 2 && index % (KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10) + 1) == 0) { // 如果光标在空格位置
                tvShow.setSelection(index - 2); // 跳过空格
            } else { // 不在空格位置
                tvShow.setSelection(index - 1); // 正常前移一位
            }
        }
    }

    /**
     * 判断指定索引是否为数字按键
     * @param index 按键索引
     * @return true表示是数字按键
     */
    public boolean isNumber(int index) { // 判断是否为数字方法
        return index != INDEX_enter && index != INDEX_del && index != INDEX_negative // 不是确认、删除、负号
                && index != INDEX_positive && index != INDEX_point // 不是正号、小数点
                && index != INDEX_f && index != INDEX_p && index != INDEX_n // 不是f/p/n单位
                && index != INDEX_u && index != INDEX_m && index != INDEX_k // 不是u/m/k单位
                && index != INDEX_M && index != INDEX_G && index != INDEX_T; // 不是M/G/T单位
    }

    /**
     * 判断指定索引是否为确认按键
     * @param index 按键索引
     * @return true表示是确认按键
     */
    public boolean isEnter(int index) { // 判断是否为确认方法
        return index == INDEX_enter; // 返回是否为确认键
    }

    /**
     * 判断指定索引是否为小数点按键
     * @param index 按键索引
     * @return true表示是小数点按键
     */
    public boolean isPoint(int index) { // 判断是否为小数点方法
        return index == INDEX_point; // 返回是否为小数点键
    }

    /**
     * 判断指定索引是否为删除按键
     * @param index 按键索引
     * @return true表示是删除按键
     */
    public boolean isDelete(int index) { // 判断是否为删除方法
        return index == INDEX_del; // 返回是否为删除键
    }

    /**
     * 判断指定索引是否为正负号按键
     * @param index 按键索引
     * @return true表示是正负号按键
     */
    public boolean isSymbol(int index) { // 判断是否为正负号方法
        return index == INDEX_positive || index == INDEX_negative; // 返回是否为正号或负号
    }

    /**
     * 判断指定索引是否为单位按键
     * @param index 按键索引
     * @return true表示是单位按键
     */
    public boolean isUnit(int index) { // 判断是否为单位方法
        return index == INDEX_f || index == INDEX_p || index == INDEX_n // f/p/n单位
                || index == INDEX_u || index == INDEX_m || index == INDEX_k // u/m/k单位
                || index == INDEX_M || index == INDEX_G || index == INDEX_T; // M/G/T单位
    }

    /**
     * 检查是否有单位被选中
     * @return 被选中的单位索引，-1表示无单位选中
     */
    public int hasUnit() { // 检查单位选中方法
        for (int i = 0; i < list.size(); i++) { // 遍历所有按钮
            if (isUnit(i)) { // 如果是单位按钮
                if (((Switch) list.get(i)).isEnabled() && ((Switch) list.get(i)).isChecked()) { // 如果启用且选中
                    return i; // 返回单位索引
                }
            }
        }
        return -1; // 无单位选中返回-1
    }

    /**
     * 根据文本和单位索引计算实际数值
     * @param text 数值文本
     * @param unit 单位索引
     * @return 带单位的实际数值
     */
    private double getValue(String text, int unit) { // 获取带单位值方法
        double dText = Double.valueOf(text); // 解析文本为双精度数
        double dUnit = 1.0d; // 默认单位乘数为1
        switch (unit) { // 根据单位索引选择乘数
            case INDEX_f: // f（飞）
                dUnit = 0.001 * 0.001 * 0.001 * 0.001 * 0.001; // 1e-15
                break;
            case INDEX_p: // p（皮）
                dUnit = 0.001 * 0.001 * 0.001 * 0.001; // 1e-12
                break;
            case INDEX_n: // n（纳）
                dUnit = 0.001 * 0.001 * 0.001; // 1e-9
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
            case INDEX_M: // M（兆）
                dUnit = 1000_000d; // 1e6
                break;
            case INDEX_G: // G（吉）
                dUnit = 1000_000_000d; // 1e9
                break;
            case INDEX_T: // T（太）
                dUnit = 1000_000_000_000d; // 1e12
                break;
            default: // 默认
                dUnit = 1; // 无单位
                break;
        }
        return DoubleUtil.mul(dText, dUnit); // 返回数值乘以单位
    }

    /**
     * 将双精度数转换为工程记数法字符串（含f~T全单位）
     * @param d 原始双精度数值
     * @return 格式化后的工程记数法字符串
     */
    private String getValueFromD(double d) { // 双精度转工程记数法方法
        if (DoubleUtil.compareTo(d, 0d) < 0) { // 如果为负数
            return "-" + getValueFromD(DoubleUtil.mul(d, -1d)); // 递归处理绝对值并加负号
        } else if (DoubleUtil.compareTo(d, 0d) == 0) { // 如果为零
            return "0 "; // 返回"0 "
        } else if (DoubleUtil.compareTo(d, 1000_000_000_000d) >= 0) { // 大于等于1T
            d = DoubleUtil.mul(d, 1e-12); // 转换为太
            return get5Bit(d) + " T"; // 返回带T单位
        } else if (DoubleUtil.compareTo(d, 1000_000_000d) >= 0) { // 大于等于1G
            d = DoubleUtil.mul(d, 1e-9); // 转换为吉
            return get5Bit(d) + " G"; // 返回带G单位
        } else if (DoubleUtil.compareTo(d, 1000_000d) >= 0) { // 大于等于1M
            d = DoubleUtil.mul(d, 1e-6); // 转换为兆
            return get5Bit(d) + " M"; // 返回带M单位
        } else if (DoubleUtil.compareTo(d, 1000d) >= 0) { // 大于等于1k
            d = DoubleUtil.mul(d, 0.001); // 转换为千
            return get5Bit(d) + " k"; // 返回带k单位
        } else if (DoubleUtil.compareTo(d, 1d) >= 0) { // 大于等于1
            return get5Bit(d)+" "; // 返回无单位
        } else if (DoubleUtil.compareTo(d, 0.001) >= 0) { // 大于等于1m
            d = DoubleUtil.mul(d, 1000d); // 转换为毫
            return get5Bit(d) + " m"; // 返回带m单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001) >= 0) { // 大于等于1u
            d = DoubleUtil.mul(d, 1000d * 1000d); // 转换为微
            return get5Bit(d) + " μ"; // 返回带μ单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001) >= 0) { // 大于等于1n
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d); // 转换为纳
            String s = get5Bit(d); // 获取5位有效数字
            if (s.contains(".")) { // 如果包含小数点
                String[] split = s.split("\\."); // 按小数点分割
                if (split[1].length() > 3) { // 如果小数部分超过3位
                    split[1] = split[1].substring(0, 3); // 截取3位
                    s = split[0] + "." + split[1]; // 重新组合
                }
            }
            return s + " n"; // 返回带n单位
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001) >= 0) { // 大于等于1p
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d); // 转换为皮
            return get5Bit(((int) d)) + " p"; // 返回带p单位（取整）
        }  else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001 * 0.001) >= 0) { // 大于等于1f
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d * 1000d); // 转换为飞
            return get5Bit(((int) d)) + " f"; // 返回带f单位（取整）
        }

        else { // 小于1f
            return "1 p"; // 返回最小显示值"1 p"
        }
    }

    /**
     * 获取最多5位有效数字的字符串表示
     * @param d 数值
     * @return 格式化后的字符串
     */
    private String get5Bit(double d) { // 获取5位有效数字方法
        String s = String.valueOf(d); // 转为字符串
        if (s.length() >= 6) { // 如果长度超过6
            s = s.substring(0, 6); // 截取前6个字符
        }
        if (s.contains(".")) { // 如果包含小数点
            while (s.endsWith("0")) { // 移除末尾的0
                s = s.substring(0, s.length() - 1); // 去掉末尾0
            }
            if (s.endsWith(".")) { // 如果去掉0后以小数点结尾
                s = s.replace(".", ""); // 移除小数点
            }
        }
        return s; // 返回格式化字符串
    }

    /**
     * 按键点击事件监听器
     */
    private OnClickListener onItemClickListener = new OnClickListener() { // 按键点击监听器
        @Override
        public void onClick(View v) { // 点击事件回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            int index = Integer.parseInt((String) v.getTag()); // 获取按键索引
            String str = ((Button) v).getText().toString(); // 获取按键文本

            if (isNumber(index)) { // 如果是数字键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                    tvShow.setText(""); // 清空输入
                }
                if (tvShow.getText().toString().equals("0")) { // 如果当前为0
                    delText(); // 删除0
                }
                if (tvShow.getText().toString().length() >= INPUT_LENGTH_LIMIT) { // 如果达到长度限制
                    return; // 不再添加
                }
                addText(str); // 添加数字
            } else if (isDelete(index)) { // 如果是删除键
                if (StrUtil.isEmpty(tvShow.getText().toString())) { // 如果输入为空
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                } else { // 输入不为空
                    delText(); // 删除一个字符
                }
            } else if (isSymbol(index)) { // 如果是正负号键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                }
                if (index == INDEX_negative) { // 如果是负号
                    tvSymbol.setVisibility(VISIBLE); // 显示负号
                } else if (index == INDEX_positive) { // 如果是正号
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
            } else if (isUnit(index)) { // 如果是单位键
                if (!((Switch) list.get(index)).isChecked()) { // 如果单位未被选中（取消选中）
                    ((Switch) list.get(index)).setChecked(false); // 取消选中
                } else { // 单位被选中
                    for (int i = 0; i < list.size(); i++) { // 遍历所有按钮
                        if (isUnit(i)) { // 如果是单位按钮
                            if (index == i) { // 当前点击的单位
                                ((Switch) list.get(i)).setChecked(true); // 设为选中
                            } else { // 其他单位
                                ((Switch) list.get(i)).setChecked(false); // 取消选中（互斥）
                            }
                        }
                    }
                }
            } else if (isPoint(index)) { // 如果是小数点键
                if (first) { // 如果是首次点击
                    first = false; // 重置首次标记
                    tvShow.setText("0"); // 设置为0
                    tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
                if (tvShow.getText().toString().isEmpty()) { // 如果输入为空
                    tvShow.setText("0"); // 设置为0
                    tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                    tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
                }
                if (!tvShow.getText().toString().contains(str)) { // 如果不包含小数点
                    addText(str); // 添加小数点
                }
            } else if (isEnter(index)) { // 如果是确认键
                if (onDismissListener != null) { // 如果回调不为空
                    clickEnter(); // 触发确认回调
                }
                hide(); // 隐藏键盘
            }
        }
    };

    /**
     * 确认按钮回调处理，格式化并返回输入值
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
            if (isSymbolVisible() && !"0".equals(text)) { // 如果有负号且非0
                text = "-" + text; // 添加负号
            }
            if (((Switch)list.get(INDEX_f)).isChecked() // 如果f单位选中
                    || ((Switch)list.get(INDEX_p)).isChecked() // 或p单位选中
                    || ((Switch)list.get(INDEX_n)).isChecked() // 或n单位选中
                    || ((Switch)list.get(INDEX_u)).isChecked() // 或u单位选中
                    || ((Switch)list.get(INDEX_m)).isChecked() // 或m单位选中
                    || ((Switch)list.get(INDEX_k)).isChecked() // 或k单位选中
                    || ((Switch)list.get(INDEX_M)).isChecked() // 或M单位选中
                    || ((Switch)list.get(INDEX_G)).isChecked() // 或G单位选中
                    || ((Switch)list.get(INDEX_T)).isChecked() // 或T单位选中
            ) {
                int hasUnit = hasUnit(); // 获取选中的单位索引
                onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, hasUnit))); // 回调带单位的格式化值
            } else { // 没有单位选中
                onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, 1))); // 回调无单位的格式化值
            }
        } else { // 文本为空
            onDismissListener.onDismiss(fromView, String.valueOf(0)); // 回调0
        }
    }

    /**
     * 长按删除键监听器，清空所有输入
     */
    private OnLongClickListener onLongClickListener = new OnLongClickListener() { // 长按监听器
        @Override
        public boolean onLongClick(View v) { // 长按事件回调
            if (!StrUtil.isEmpty(tvShow.getText().toString())) { // 如果输入不为空
                PlaySound.getInstance().playButton(); // 播放按键音效
                tvShow.setText("0"); // 重置为0
                tvShow.setSelection(tvShow.getSelectionStart() + 1); // 光标后移
                tvSymbol.setVisibility(INVISIBLE); // 隐藏负号
            }
            return false; // 不消费长按事件（允许后续点击）
        }
    };

    /**
     * 格式化双精度数为字符串，取消科学计数法
     * @param d 双精度数值
     * @return 格式化后的字符串
     */
    private static String formatDouble(double d) { // 格式化双精度方法
        NumberFormat nf = NumberFormat.getInstance(); // 获取数字格式化器
        //设置保留多少位小数
        nf.setMaximumFractionDigits(20); // 设置最大小数位数为20
        // 取消科学计数法
        nf.setGroupingUsed(false); // 禁用千分位分组
        //返回结果
        return nf.format(d); // 返回格式化结果
    }
}
