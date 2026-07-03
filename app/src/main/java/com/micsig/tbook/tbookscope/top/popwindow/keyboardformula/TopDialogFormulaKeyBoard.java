package com.micsig.tbook.tbookscope.top.popwindow.keyboardformula; // 公式键盘对话框所在包

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
import android.widget.RelativeLayout; // 相对布局类
import android.widget.TextView; // 文本视图类

import com.micsig.base.DoubleUtil; // 双精度工具类
import com.micsig.base.keyevent.KeyEventUtil; // 按键事件工具类
import com.micsig.base.Logger; // 日志工具类
import com.micsig.tbook.scope.math.MathExprError; // 数学表达式错误类
import com.micsig.tbook.scope.math.MathExprWave; // 数学表达式验证类
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量类
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图组类
import com.micsig.tbook.tbookscope.R; // 资源类
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom; // 主底部持有者类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxBus事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 播放声音工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.App; // 应用工具类
import com.micsig.tbook.tbookscope.util.Screen; // 屏幕工具类
import com.micsig.tbook.ui.MSelectionEditText; // 自定义选择编辑框
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具类

import java.util.ArrayList; // 动态数组类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      TopDialogFormulaKeyBoard                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：高级数学表达式键盘对话框，支持复杂公式编辑和实时验证               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                 │
 * │   1. 公式编辑：支持数学运算符、函数、通道变量、单位前缀、科学计数法等         │
 * │   2. 上下文感知：根据光标位置和前一个token动态控制按键可用性                 │
 * │   3. 表达式验证：确认时通过MathExprWave验证表达式合法性                      │
 * │   4. 自动格式化：实时将*→×、/→÷、pi→π、u→μ等显示符号转换                   │
 * │   5. 光标控制：左右选择按钮按token粒度移动光标，支持长按跳到首尾            │
 * │   6. 通道适配：根据2ch/4ch机型自动禁用不可用通道按键                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                 │
 * │   继承AbsoluteLayout，实现IKeyBoardFormula接口                              │
 * │   使用MSelectionEditText自定义编辑框，支持按token选择和光标控制              │
 * │   通过RxBus发送对话框打开/关闭事件和按键可见性事件                          │
 * │   使用TextWatcher实现实时格式化，使用KeyBoardFormulaUtil进行解析和可见性判断  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流：                                                                   │
 * │   调用方 → setData → 显示键盘                                              │
 * │   用户点击按键 → onItemClickListener → 插入/删除token → TextWatcher格式化   │
 * │   光标移动 → onSelectionChanged → setViewListVisible → 更新按键可用性       │
 * │   点击确认 → MathExprWave验证 → OnDismissListener.onDismiss → 返回结果      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖：IKeyBoardFormula、KeyBoardFormulaUtil、MathExprWave、MSelectionEditText│
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：示波器高级数学运算功能中编辑CH1+CH2、sqrt(CH1)等数学表达式         │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopDialogFormulaKeyBoard extends AbsoluteLayout implements IKeyBoardFormula { // 继承绝对布局，实现公式键盘按键索引接口
    private static final String TAG = "TopFormulaKeyBoard"; // 日志标签
    /**
     * 该公式最长长度
     */
    private static final int COUNT_FORMULA = 36; // 公式最大长度限制

    private Context context; // 上下文对象
    private MSelectionEditText tvShow; // 自定义选择编辑框
    private TextView tvMsgError; // 错误提示文本
    private RelativeLayout gridView; // 按键网格布局
    private ArrayList<String> showList = new ArrayList<>(); // 当前显示文本的token列表
    private OnDismissListener onDismissListener; // 确认关闭回调
    private String timeBase; // 时基字符串
    private RelativeLayout showKeyBoardLayout; // 键盘显示区域布局
    private boolean isFormatting = false; // 格式化进行中标志，防止递归

    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 确认关闭回调接口
     */
    public interface OnDismissListener { // 确认关闭回调接口
        /**
         * @param show 用户确认后的公式字符串
         */
        void onDismiss(String show); // 确认关闭时回调
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public TopDialogFormulaKeyBoard(Context context) { // 单参数构造方法
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs 属性集
     */
    public TopDialogFormulaKeyBoard(Context context, AttributeSet attrs) { // 双参数构造方法
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造方法
     * @param context 上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogFormulaKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    /**
     * 初始化键盘视图
     */
    private void init() { // 初始化方法
        setClickable(true); // 设置可点击
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_keyboard_formula, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 触摸回调
                hide(); // 隐藏键盘
                return false; // 不消费事件
            }
        });
        initView(rootViewGroup); // 初始化视图
        hide(); // 初始状态隐藏
    }

    /**
     * 初始化视图控件和事件监听
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图控件
        showKeyBoardLayout = view.findViewById(R.id.showKeyBoardLayout); // 获取键盘显示区域布局
        tvShow = (MSelectionEditText) view.findViewById(R.id.formulaEdit); // 获取自定义编辑框
        View leftSelection = view.findViewById(R.id.formulaSelectionLeft); // 获取左选择按钮
        View rightSelection = view.findViewById(R.id.formulaSelectionRight); // 获取右选择按钮
        tvMsgError = (TextView) view.findViewById(R.id.formulaMsgError); // 获取错误提示文本

        leftSelection.setOnClickListener(onClickListener); // 左选择按钮点击监听
        rightSelection.setOnClickListener(onClickListener); // 右选择按钮点击监听
        leftSelection.setOnLongClickListener(onLongClickListener); // 左选择按钮长按监听
        rightSelection.setOnLongClickListener(onLongClickListener); // 右选择按钮长按监听
        tvShow.setShowSoftInputOnFocus(false); // 禁止系统软键盘弹出
        tvShow.setLongClickable(false); // 禁止长按
        tvShow.setOnSelectionChanged(onSelectionChanged); // 设置光标变化监听
        tvShow.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); // 禁止输入法建议
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
                    Logger.d(TAG, "event= " + event.getKeyCode()); // 打印按键码日志
                    if (onItemClickListener == null) return true; // 监听器为空则消费事件
                    if (KeyEventUtil.isConfirmKey(event)) { // 确认键
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_enter)); // 模拟点击确认键
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isBackKey(event)) { // 退格键
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del)); // 模拟点击删除键
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isForwardDel(event)) { // 前向删除键
                        if (tvShow.getSelectionStart() == tvShow.getText().length()) { // 光标在末尾
                            return true; // 无法前向删除，消费事件
                        }
                        rightSelection.performClick(); // 先右移光标
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del)); // 再删除
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isLeft(event)) { // 左方向键
                        leftSelection.performClick(); // 模拟点击左选择按钮
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isRight(event)) { // 右方向键
                        rightSelection.performClick(); // 模拟点击右选择按钮
                        return true; // 消费事件
                    }
                }
                return false; // 不消费其他事件
            }
        });

        tvShow.addTextChangedListener(new TextWatcher() { // 添加文本变化监听器
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 文本变化前
                Logger.d(TAG, "beforeTextChanged=" + s.toString()); // 打印日志
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // 文本变化中
                Logger.d(TAG, "onTextChanged=" + s.toString()); // 打印日志
            }

            @Override
            public void afterTextChanged(Editable s) { // 文本变化后
                if (isFormatting) return; // 如果正在格式化则跳过，防止递归
                isFormatting = true; // 设置格式化标志
                String str = s.toString(); // 获取当前文本
                str = formatExpress(str); // 格式化表达式
                int startSelection = tvShow.getSelectionStart(); // 保存当前光标位置
                if (str.length() > COUNT_FORMULA) { // 超过最大长度
                    str = str.substring(0, COUNT_FORMULA); // 截取到最大长度
                }
                showList = KeyBoardFormulaUtil.getSelectionListFromShowText(str); // 解析token列表
                if (!s.toString().equals(str)) { // 如果文本有变化（格式化导致）
                    s = Editable.Factory.getInstance().newEditable(str); // 创建新的可编辑文本
                    int selection = str.length(); // 光标位置设为末尾
                    if (selection > COUNT_FORMULA) { // 超过最大长度
                        selection = COUNT_FORMULA; // 限制光标位置
                    }
                    tvShow.setText(s.toString()); // 设置格式化后的文本
                    tvShow.setSelection(s.toString().length()); // 光标移到末尾
                } else { // 文本无变化
                    String leftSelection = str.substring(0, startSelection); // 获取光标左侧文本
                    tvShow.setText(str); // 设置文本
                    tvShow.setSelection(leftSelection.length()); // 保持光标位置
//                    tvShow.setText(str); // 已注释的代码
//                    tvShow.setSelection(str.length()); // 已注释的代码
                }
                isFormatting = false; // 重置格式化标志
            }
        });



        gridView = (RelativeLayout) view.findViewById(R.id.formulaGridView); // 获取按键网格布局

        for (int i = 0; i < gridView.getChildCount(); i++) { // 遍历网格中的所有子视图
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2 // 2通道机型
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch3 // 通道3
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch4 // 通道4
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5 // 通道5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6 // 通道6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7 // 通道7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8) // 通道8
            ) {
                gridView.getChildAt(i).setEnabled(false); // 禁用3-8通道按键
            } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4 // 4通道机型
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5 // 通道5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6 // 通道6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7 // 通道7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8) // 通道8
            ) {
                gridView.getChildAt(i).setEnabled(false); // 禁用5-8通道按键
            }
            gridView.getChildAt(i).setOnClickListener(onItemClickListener); // 设置点击监听
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_del) { // 删除键
                gridView.getChildAt(i).setOnLongClickListener(onLongClickListener); // 设置长按监听（长按清空）
            }
        }
    }

    /**
     * 设置公式数据并显示键盘
     * @param showStr 初始公式字符串
     * @param timeBase 时基字符串
     * @param onDismissListener 确认关闭回调
     */
    public void setData(String showStr, String timeBase, OnDismissListener onDismissListener) { // 设置数据并显示
        isFormatting = true; // 设置格式化标志，防止TextWatcher干扰
        if ("0".equals(showStr)) { // 如果初始值为"0"
            showStr = ""; // 清空，表示空表达式
        }
        this.timeBase = timeBase; // 保存时基
        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(showStr); // 解析token列表
        tvShow.requestFocus(); // 请求焦点
        tvShow.setText(showStr); // 设置初始文本
        tvShow.setFilters(new InputFilter[]{new InputFilter.LengthFilter(COUNT_FORMULA)}); // 设置长度过滤器
        tvShow.setSelection(Math.min(showStr.length(), tvShow.getText().length())); // 设置光标位置
        tvMsgError.setText(""); // 清空错误提示
        this.onDismissListener = onDismissListener; // 保存回调
        if (StrUtil.isEmpty(showStr)) { // 如果初始文本为空
            setViewListVisible(INDEX_null); // 设置初始按键可见性
        }
        isFormatting = false; // 重置格式化标志
        show(); // 显示键盘
    }

    /**
     * 显示键盘
     */
    public void show() { // 显示键盘
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD); // 设置密码输入类型
//        moveViewToPosition(showKeyBoardLayout, 0, ViewUtils.getDialogFormulaOffset()); // 移动到指定位置（已注释）
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FORMULAKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogFormulaKeyBoard",rootViewGroup); // 打印控件位置信息
    }

    /**
     * 隐藏键盘
     */
    public void hide() { // 隐藏键盘
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FORMULAKEYBOARD); // 发送对话框关闭事件
    }

    /**
     * 表达式正确的前提下，去除表达式中无效的0
     * @param showText 表达式文本
     * @return 去除无效0后的表达式文本
     */
    private String removeInvalid0(String showText) { // 去除表达式中无效的前导零和末尾零
        ArrayList<String> listStr = new ArrayList<>(); // 存储数字段字符串列表
        ArrayList<Integer> listIndex = new ArrayList<>(); // 存储数字段起始位置列表
        String s = ""; // 当前数字段缓冲
        for (int i = 0; i < showText.length(); i++) { // 遍历表达式每个字符
            if (StrUtil.isNumber(showText.charAt(i)) || showText.charAt(i) == '.') { // 数字或小数点
                s = s + showText.charAt(i); // 拼接到当前数字段
            } else { // 非数字字符
                if (!StrUtil.isEmpty(s)) { // 当前数字段非空
                    listIndex.add(i - s.length()); // 记录数字段起始位置
                    listStr.add(s); // 添加数字段
                    s = ""; // 重置缓冲
                }
            }
        }
        if (!StrUtil.isEmpty(s)) { // 处理末尾的数字段
            listIndex.add(showText.length() - s.length()); // 记录起始位置
            listStr.add(s); // 添加数字段
            s = ""; // 重置缓冲
        }
        Logger.d(TAG, "removeInvalid0() listStr:" + listStr + ",listIndex:" + listIndex + ",s:" + s); // 打印日志
        if (listStr.size() == 0) { // 没有数字段
            Logger.d(TAG, "removeInvalid0() called with: showText = [" + showText + "],return1 = [" + showText + "]"); // 打印日志
            return showText; // 直接返回
        }
        String s2 = ""; // 存储最终结果
        int subStart = 0; // 当前截取起始位置
        for (int i = 0; i < listStr.size(); i++) { // 遍历所有数字段
            int length = listStr.get(i).length(); // 获取数字段原始长度
            if (listStr.get(i).contains(".") || listStr.get(i).contains("E")) { // 包含小数点或E
                String s1 = listStr.get(i); // 取出数字段
                if (s1.contains(".") && !s1.contains("E")) { // 仅包含小数点不包含E
                    while (s1.endsWith("0")) { // 去除末尾0
                        s1 = s1.substring(0, s1.length() - 1); // 截去末尾0
                    }
                    if (s1.endsWith(".")) { // 去除末尾小数点
                        s1 = s1.substring(0, s1.length() - 1); // 截去末尾小数点
                    }
                }
                while (s1.length() >= 2 && s1.charAt(0) == '0' && s1.charAt(1) != '.') { // 去除前导0
                    s1 = s1.substring(1); // 截去前导0
                }
                listStr.set(i, s1); // 更新数字段
            } else { // 不包含小数点和E
                while (listStr.get(i).length() >= 2 && listStr.get(i).charAt(0) == '0' && listStr.get(i).charAt(1) != '.') { // 去除前导0
                    listStr.set(i, listStr.get(i).substring(1)); // 截去前导0
                }
                listStr.set(i, listStr.get(i)); // 更新数字段（无实际变化）
            }
            if (listStr.get(i).startsWith(".")) { // 以小数点开头
                listStr.set(i, "0" + listStr.get(i)); // 前面补0
            }
            s2 = s2 + showText.substring(subStart, listIndex.get(i)) + listStr.get(i); // 拼接非数字部分和处理后的数字段
            subStart = listIndex.get(i) + length; // 更新截取起始位置
        Logger.d(TAG, "removeInvalid0() called with: listStr.get(i) = [" + listStr.get(i) + "],s2 = [" + s2 + "]"); // 打印日志
        }
        s2 = s2 + showText.substring(subStart); // 拼接剩余的非数字部分
        Logger.d(TAG, "removeInvalid0() called with: showText = [" + showText + "],return2 = [" + s2 + "]"); // 打印日志
        return s2; // 返回处理后的表达式
    }

    /**
     * 根据当前光标位置的按键索引，计算并设置各按键的可见性
     * @param index 当前光标位置对应的按键索引
     */
    private void setViewListVisible(int index) { // 设置按键可见性
        Logger.d(TAG, "setViewListVisible() called with: index = [" + index + "]"); // 打印日志
        boolean eAddSubAfterBracketLeft = true; // ±后的左括号等是否可用（默认可用）
        boolean numberAfterPoint = true; // 数字后小数点是否可用（默认可用）
        boolean numberAfterE = true; // 数字后E是否可用（默认可用）
        boolean afterBracketRight = false; // 右括号是否可用（默认不可用）
        boolean zeroAfterZero = true; // 0后0是否可用（默认可用）
        if (index == INDEX_add || index == INDEX_sub) { // 当前是加号或减号
            String[] keyList = KeyBoardFormulaUtil.getKeyList(); // 获取按键显示文本列表
            String s = keyList[index]; // 获取当前按键的显示文本
            if (tvShow.getSelectionStart() >= s.length()) { // 光标不在开头
                String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart() - s.length()); // 获取±之前的文本
                Logger.d(TAG, "setViewListVisible() called with: substring = [" + substring + "]"); // 打印日志
                int index2 = INDEX_null; // ±之前最后一个token的索引
                if (!StrUtil.isEmpty(substring)) { // ±之前文本非空
                    String s2 = ""; // 存储匹配的搜索项
                    String[] searchList = KeyBoardFormulaUtil.getSearchList(); // 获取搜索列表
                    for (int i = 0; i < searchList.length; i++) { // 遍历搜索列表
                        if (substring.endsWith(searchList[i])) { // 如果文本以搜索项结尾
                            s2 = searchList[i]; // 记录匹配项
                            break; // 跳出循环
                        }
                    }
                    index2 = KeyBoardFormulaUtil.getIndex(s2); // 获取匹配项的按键索引
                    Logger.d(TAG, "setViewListVisible() called with: s2 = [" + s2 + "],index2 = [" + index2 + "]"); // 打印日志
                }
                if (index2 == INDEX_E) { // 如果±之前是E（科学计数法）
                    eAddSubAfterBracketLeft = false; // E±后不能跟左括号、函数等
                }
            }
        } else if (KeyBoardFormulaUtil.isNumber(index)) { // 当前是数字键
            String subShowText = tvShow.getText().toString().substring(0, tvShow.getSelectionStart()); // 获取光标左侧文本
            ArrayList<String> listStr = new ArrayList<>(); // 存储数字段列表
            ArrayList<Integer> listIndex = new ArrayList<>(); // 存储数字段起始位置列表
            String s = ""; // 当前数字段缓冲
            for (int i = 0; i < subShowText.length(); i++) { // 遍历光标左侧文本
                if (StrUtil.isNumber(subShowText.charAt(i)) || subShowText.charAt(i) == '.' || subShowText.charAt(i) == 'E') { // 数字、小数点或E
                    s = s + subShowText.charAt(i); // 拼接到当前数字段
                } else if ((subShowText.charAt(i) == '+' || subShowText.charAt(i) == '-') && (i >= 1 && subShowText.charAt(i - 1) == 'E')) { // E后的±号
                    s = s + subShowText.charAt(i); // 也拼接到当前数字段
                } else { // 其他字符
                    if (!StrUtil.isEmpty(s)) { // 当前数字段非空
                        listIndex.add(i - s.length()); // 记录起始位置
                        listStr.add(s); // 添加数字段
                        s = ""; // 重置缓冲
                    }
                }
            }
            if (!StrUtil.isEmpty(s)) { // 处理末尾的数字段
                listIndex.add(subShowText.length() - s.length()); // 记录起始位置
                listStr.add(s); // 添加数字段
                s = ""; // 重置缓冲
            }
            int selection = tvShow.getSelectionStart(); // 获取当前光标位置
            Logger.d(TAG, "setViewListVisible() listStr:" + listStr + ",listIndex:" + listIndex + ",selection:" + selection); // 打印日志
            String E = context.getString(R.string.key_formula_E); // 获取E的显示文本
            for (int i = 0; i < listIndex.size(); i++) { // 遍历所有数字段
                if (listIndex.get(i) < selection && (listIndex.get(i) + listStr.get(i).length()) >= selection) { // 光标在此数字段内
                    if (listStr.get(i).contains(".") || listStr.get(i).contains(E)) { // 包含小数点或E
                        numberAfterPoint = false; // 已有小数点，不能再输入
                    }
                    if (listStr.get(i).contains(E)) { // 包含E
                        numberAfterE = false; // 已有E，不能再输入
                    }
                }
            }
            if (index == INDEX_0) { // 当前是0键
                if (selection >= 2) { // 光标不在开头
                    String substring = tvShow.getText().toString().substring(0, selection); // 获取光标左侧文本
                    String strNumber = ""; // 存储从光标向前连续的数字
                    for (int i = substring.length() - 1; i >= 0; i--) { // 从光标向前遍历
                        if (StrUtil.isNumber(substring.charAt(i)) || substring.charAt(i) == '.') { // 数字或小数点
                            strNumber = substring.charAt(i) + strNumber; // 拼接到前面
                        } else { // 非数字字符
                            break; // 跳出循环
                        }
                    }
                    if (!StrUtil.isEmpty(strNumber) && !strNumber.contains(".") // 连续数字非空且不含小数点
                            && DoubleUtil.compareTo(Double.valueOf(strNumber), 0d) == 0) { // 且数值等于0
                        zeroAfterZero = false; // 纯0数字串，不能再输入0
                    }
                } else { // 光标在开头
                    zeroAfterZero = false; // 开头不能再输入0
                }
            }
        }
        if (index != INDEX_null) { // 非初始状态
            String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart()); // 获取光标左侧文本
            String strLeftBracket = context.getString(R.string.key_formula_bracket_left); // 获取左括号显示文本
            String strRightBracket = context.getString(R.string.key_formula_bracket_right); // 获取右括号显示文本
            int leftBracket = StrUtil.getCountFromString(substring, strLeftBracket); // 统计左括号数量
            int rightBracket = StrUtil.getCountFromString(substring, strRightBracket); // 统计右括号数量
            if (leftBracket > rightBracket) { // 左括号多于右括号
                afterBracketRight = true; // 可以输入右括号
            }
        }
        boolean[] visibleList = KeyBoardFormulaUtil.getVisibleListFromCurSelection(index, eAddSubAfterBracketLeft // 获取可见性列表
                , numberAfterPoint, numberAfterE, afterBracketRight, zeroAfterZero);
        for (int i = 0; i < gridView.getChildCount(); i++) { // 遍历所有按键
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2 // 2通道机型
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch3 // 通道3
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch4 // 通道4
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5 // 通道5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6 // 通道6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7 // 通道7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8) // 通道8
            ) {
                visibleList[i] = false; // 禁用3-8通道
            } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4 // 4通道机型
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5 // 通道5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6 // 通道6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7 // 通道7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8) // 通道8
            ) {
                visibleList[i] = false; // 禁用5-8通道
            }
            gridView.getChildAt(i).setEnabled(visibleList[i]); // 根据可见性设置按键启用状态
        }
        RxBus.getInstance().post(RxEnum.KEYBOARD_FORMULA_ENABLE, visibleList); // 发送按键可见性事件
    }

    /**
     * 按键点击监听器
     */
    private OnClickListener onItemClickListener = new OnClickListener() { // 按键点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            Screen.getViewLocation(v); // 获取视图位置（用于旋钮交互）
            PlaySound.getInstance().playButton(); // 播放按键音效
            int index = Integer.parseInt((String) v.getTag()); // 获取按键索引
            String text = ((Button) v).getText().toString(); // 获取按键显示文本
            String showText = tvShow.getText().toString(); // 获取当前表达式文本
            if (index == INDEX_time_base) {//时基按键 // 时基按键特殊处理
                text = App.get().getResources().getString(R.string.key_formula_tb); // 使用时基显示符号
            }
            switch (index) { // 根据按键索引处理
                case INDEX_add: // 加号
                case INDEX_sub: // 减号
                case INDEX_mul: // 乘号
                case INDEX_div: // 除号
                case INDEX_bracket_left: // 左括号
                case INDEX_less: // 小于号
                case INDEX_greater: // 大于号
                case INDEX_less_equal: // 小于等于号
                case INDEX_greater_equal: // 大于等于号
                case INDEX_bracket_right: // 右括号
                case INDEX_equal: // 等于号
                case INDEX_not_equal: // 不等于号
                case INDEX_and: // 逻辑与
                case INDEX_or: // 逻辑或
                case INDEX_no: // 逻辑非
                case INDEX_sqrt: // 平方根
                case INDEX_abs: // 绝对值
                case INDEX_deg: // 角度转弧度
                case INDEX_rad: // 弧度转角度
                case INDEX_exp: // 指数函数
                case INDEX_diff: // 微分
                case INDEX_ln: // 自然对数
                case INDEX_sine: // 正弦
                case INDEX_cos: // 余弦
                case INDEX_tan: // 正切
                case INDEX_intg: // 积分
                case INDEX_lg: // 常用对数
                case INDEX_arcsin: // 反正弦
                case INDEX_arccos: // 反余弦
                case INDEX_arctan: // 反正切
                case INDEX_ch1: // 通道1
                case INDEX_ch2: // 通道2
                case INDEX_ch3: // 通道3
                case INDEX_ch4: // 通道4
                case INDEX_pi: // 圆周率
                case INDEX_ch5: // 通道5
                case INDEX_ch6: // 通道6
                case INDEX_ch7: // 通道7
                case INDEX_ch8: // 通道8
                case INDEX_time_base: // 时基
                case INDEX_E: // 科学计数法E
                case INDEX_var1: // 变量1
                case INDEX_var2: // 变量2
                case INDEX_7: // 数字7
                case INDEX_8: // 数字8
                case INDEX_9: // 数字9
                case INDEX_m: // 毫(m)
                case INDEX_T: // 太(T)
                case INDEX_4: // 数字4
                case INDEX_5: // 数字5
                case INDEX_6: // 数字6
                case INDEX_u: // 微(μ)
                case INDEX_G: // 吉(G)
                case INDEX_1: // 数字1
                case INDEX_2: // 数字2
                case INDEX_3: // 数字3
                case INDEX_n: // 纳(n)
                case INDEX_M: // 兆(M)
                case INDEX_point: // 小数点
                case INDEX_0: // 数字0
                case INDEX_f: // 飞(f)
                case INDEX_p: // 皮(p)
                case INDEX_k: { // 千(k)
                    if (showText.length() <= COUNT_FORMULA) { // 未超过最大长度
                        isFormatting = true; // 设置格式化标志
                        int selectionStart = tvShow.getSelectionStart(); // 获取当前光标位置
                        String leftSelection = showText.substring(0, selectionStart); // 光标左侧文本
                        String rightSelection = showText.substring(selectionStart); // 光标右侧文本
                        String s = leftSelection + text + rightSelection; // 拼接新文本
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(s); // 解析token列表
                        tvShow.setText(s); // 设置新文本
                        tvShow.setSelection((leftSelection + text).length()); // 光标移到插入文本之后
                        isFormatting = false; // 重置格式化标志
                        setViewListVisible(index); // 更新按键可见性
                    }
                }
                break;
                case INDEX_enter: { // 确认键
                    isFormatting = true; // 设置格式化标志
                    if (StrUtil.isEmpty(showText)) { // 表达式为空
//                        tvMsgError.setText(R.string.msgMathAdvanceFormulaNull); // 已注释的空表达式提示
//                        return; // 已注释的返回
                        showText = "0"; // 空表达式默认为0
                    }
                    String strLeftBracket = context.getString(R.string.key_formula_bracket_left); // 获取左括号显示文本
                    String strRightBracket = context.getString(R.string.key_formula_bracket_right); // 获取右括号显示文本
                    int leftBracket = StrUtil.getCountFromString(showText, strLeftBracket); // 统计左括号数量
                    int rightBracket = StrUtil.getCountFromString(showText, strRightBracket); // 统计右括号数量
                    if (leftBracket > rightBracket) { // 左括号多于右括号
                        for (int i = 0; i < leftBracket - rightBracket; i++) { // 自动补齐右括号
                            showText += strRightBracket; // 添加右括号
                        }
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(showText); // 重新解析token列表
                        tvShow.setText(showText); // 更新显示文本
                    }
                    Logger.i("showText:" + showText); // 打印表达式日志
                    MathExprError mathExprError = MathExprWave.isExprValid( // 验证表达式合法性
                            KeyBoardFormulaUtil.amFormulaToScope(showText, MainHolderBottom.getCenterTimeBase())); // 转换为示波器格式后验证
                    if (mathExprError.isSuccess()) { // 表达式合法
                        if (showText.contains("0") || showText.contains(".")) { // 包含0或小数点
                            showText = removeInvalid0(showText); // 去除无效的0
                        }
                        onDismissListener.onDismiss(showText); // 回调返回结果
                        hide(); // 隐藏键盘
                    } else { // 表达式非法
                        int position = mathExprError.getPosition(); // 获取错误位置
                        String value = mathExprError.getValue(); // 获取错误值
                        String curValue = showText.substring(position, position + value.length()); // 获取错误位置的文本
                        String msg = String.format( // 格式化错误信息
                                getResources().getString(R.string.msgMathAdvanceFormulaError) // 获取错误信息模板
                                , String.valueOf(position + 1), curValue); // 填充位置和错误值
                        tvMsgError.setText(msg); // 显示错误信息
                        tvShow.requestFocus(); // 请求焦点
                        Logger.i("formulaMsgError:" + mathExprError.toString()); // 打印错误日志
                    }
                    isFormatting = false; // 重置格式化标志
                }
                break;
                case INDEX_del: { // 删除键
                    isFormatting = true; // 设置格式化标志
                    int selectionStart = tvShow.getSelectionStart(); // 获取当前光标位置
                    if (selectionStart - 1 >= 0) { // 光标不在开头
                        String[] fanc = KeyBoardFormulaUtil.getSearchList(); // 获取搜索列表
                        String substring = showText.substring(0, selectionStart); // 获取光标左侧文本
                        int count = 1; // 默认删除1个字符
                        for (int i = 0; i < fanc.length; i++) { // 遍历搜索列表
                            if (substring.endsWith(fanc[i])) { // 如果左侧文本以搜索项结尾
                                count = fanc[i].length(); // 删除整个token
                                break; // 跳出循环
                            }
                        }
                        String leftStr = showText.substring(0, selectionStart - count); // 删除后的左侧文本
                        String rightStr = ""; // 删除后的右侧文本
                        if (selectionStart + 1 <= tvShow.length()) { // 光标右侧还有文本
                            rightStr = showText.substring(selectionStart); // 获取右侧文本
                        }
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(leftStr + rightStr); // 重新解析token列表
                        tvShow.setText(leftStr + rightStr); // 设置新文本
                        tvShow.setSelection(leftStr.length()); // 光标移到删除位置

                        String s = ""; // 存储删除后左侧最后一个token
                        for (int i = 0; i < fanc.length; i++) { // 遍历搜索列表
                            if (leftStr.endsWith(fanc[i])) { // 如果左侧文本以搜索项结尾
                                s = fanc[i]; // 记录匹配项
                                break; // 跳出循环
                            }
                        }
                        setViewListVisible(KeyBoardFormulaUtil.getIndex(s)); // 更新按键可见性
                    }
                    isFormatting = false; // 重置格式化标志
                }
                break;
            }
        }
    };

    /**
     * 光标位置变化监听器
     */
    private MSelectionEditText.OnSelectionChanged onSelectionChanged = new MSelectionEditText.OnSelectionChanged() { // 光标变化监听器
        @Override
        public void onSelectionChanged(int selStart, int selEnd) { // 光标变化回调
            int selection = 0; // 当前token的结束位置
            int index = INDEX_null; // 当前token的索引
            for (int i = 0; i < showList.size(); i++) { // 遍历token列表
                index = i; // 记录当前索引
                if (selection >= selStart) { // 当前位置已超过光标位置
                    if (tvShow.getSelectionStart() == selection) { // 光标恰好在token边界
                        break; // 跳出循环
                    }
                    tvShow.setSelectionFromUser(selection); // 将光标对齐到token边界
                    break; // 跳出循环
                } else { // 当前位置未超过光标位置
                    selection += showList.get(i).length(); // 累加token长度
                    if (i == showList.size() - 1) { // 最后一个token
                        index++; // 索引加1
                        if (selection < selStart) { // 光标在最后一个token之后
                            selection = selStart; // 光标位置不变
                        }
                        tvShow.setSelectionFromUser(selection); // 将光标对齐到token边界
                        break; // 跳出循环
                    }
                }
            }
            Logger.d(TAG, "onSelectionChanged() selStart=[" + selStart + "],selection=[" // 打印日志
                    + selection + "],index=[" + index + "],showList:" + showList);
            index--; // 索引减1，得到光标所在token的实际索引
            if (index < INDEX_null) { // 索引小于-1
                return; // 直接返回
            }
            if (index != INDEX_null) { // 索引有效
                int keyIndex = KeyBoardFormulaUtil.getIndex(showList.get(index)); // 获取token对应的按键索引
                Logger.d("key:" + showList.get(index)); // 打印日志
                setViewListVisible(keyIndex); // 更新按键可见性
            } else { // 索引为-1（光标在开头）
                setViewListVisible(INDEX_null); // 设置初始按键可见性
            }
        }
    };

    /**
     * 左右选择按钮点击监听器
     */
    private OnClickListener onClickListener = new OnClickListener() { // 左右选择按钮点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            Screen.getViewLocation(v); // 获取视图位置
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == R.id.formulaSelectionLeft) { // 左选择按钮
                if (tvShow.getSelectionStart() - 1 >= 0) { // 光标不在开头
                    String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart()); // 获取光标左侧文本
                    String[] fanc = KeyBoardFormulaUtil.getSearchList(); // 获取搜索列表
                    int count = 1; // 默认移动1个字符
                    for (int i = 0; i < fanc.length; i++) { // 遍历搜索列表
                        if (substring.endsWith(fanc[i])) { // 如果左侧文本以搜索项结尾
                            count = fanc[i].length(); // 移动整个token的长度
                            break; // 跳出循环
                        }
                    }
                    tvShow.setSelection(tvShow.getSelectionStart() - count); // 光标左移
                }
            } else if (v.getId() == R.id.formulaSelectionRight) { // 右选择按钮
                if (tvShow.getSelectionStart() + 1 <= tvShow.getText().toString().length()) { // 光标不在末尾
                    String substring = tvShow.getText().toString().substring(tvShow.getSelectionStart()); // 获取光标右侧文本
                    String[] fanc = KeyBoardFormulaUtil.getSearchList(); // 获取搜索列表
                    int count = 1; // 默认移动1个字符
                    for (int i = 0; i < fanc.length; i++) { // 遍历搜索列表
                        if (substring.startsWith(fanc[i])) { // 如果右侧文本以搜索项开头
                            count = fanc[i].length(); // 移动整个token的长度
                            break; // 跳出循环
                        }
                    }
                    tvShow.setSelection(tvShow.getSelectionStart() + count); // 光标右移
                }
            }
        }
    };

    /**
     * 长按监听器（长按删除键清空、长按左右选择跳到首尾）
     */
    private OnLongClickListener onLongClickListener = new OnLongClickListener() { // 长按监听器
        @Override
        public boolean onLongClick(View v) { // 长按回调
            if (v.getId() == R.id.key_del) { // 长按删除键
                showList.clear(); // 清空token列表
                tvShow.setText(""); // 清空编辑框
                setViewListVisible(INDEX_null); // 设置初始按键可见性
            } else if (v.getId() == R.id.formulaSelectionLeft) { // 长按左选择按钮
                tvShow.setSelection(0); // 光标移到开头
                setViewListVisible(INDEX_null); // 设置初始按键可见性
            } else if (v.getId() == R.id.formulaSelectionRight) { // 长按右选择按钮
                tvShow.setSelection(tvShow.getText().toString().length()); // 光标移到末尾
                int index = KeyBoardFormulaUtil.getIndex(showList.get(showList.size() - 1)); // 获取最后一个token的索引
                setViewListVisible(index); // 更新按键可见性
            }
            return false; // 不消费事件
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
     * 格式化表达式，将ASCII符号转换为显示符号
     * @param express 原始表达式字符串
     * @return 格式化后的显示字符串
     */
    private String formatExpress(String express) { // 格式化表达式显示
        if (StrUtil.isEmpty(express)) { // 空字符串
            return ""; // 返回空
        }
        express = express // 格式化替换
                .replace("*", "×") // *替换为×
                .replace("/", "÷") // /替换为÷
                .replace("pi", "π") // pi替换为π
                .replace("TB", "tb") // TB替换为tb（统一小写）
                .replace("Tb", "tb") // Tb替换为tb
                .replace("tB", "tb") // tB替换为tb
                .replace("×u", "×μ") // ×u替换为×μ
                .replace("×U", "×μ") // ×U替换为×μ
//                .replace(context.getString(R.string.key_formula_tb), KeyBoardFormulaUtil.handleTimeBase(MainHolderBottom.getCenterTimeBase())) // 已注释的时基替换
        ;
        return express; // 返回格式化后的表达式
    }

}
