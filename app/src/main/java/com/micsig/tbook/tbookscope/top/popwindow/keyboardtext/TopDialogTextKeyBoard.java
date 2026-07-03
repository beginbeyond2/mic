package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

import android.content.Context; // 导入上下文类
import android.text.Editable; // 导入可编辑文本类
import android.text.InputFilter; // 导入输入过滤器类
import android.text.InputType; // 导入输入类型类
import android.text.TextUtils; // 导入文本工具类
import android.text.TextWatcher; // 导入文本变化监听类
import android.util.AttributeSet; // 导入属性集类
import android.view.ActionMode; // 导入操作模式类
import android.view.Gravity; // 导入对齐方式类
import android.view.KeyEvent; // 导入按键事件类
import android.view.Menu; // 导入菜单类
import android.view.MenuItem; // 导入菜单项类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.view.inputmethod.InputMethodManager; // 导入输入法管理器类
import android.widget.Button; // 导入按钮类
import android.widget.EditText; // 导入编辑框类
import android.widget.LinearLayout; // 导入线性布局类
import android.widget.TextView; // 导入文本视图类

import androidx.annotation.Nullable; // 导入可空注解类
import androidx.recyclerview.widget.GridLayoutManager; // 导入网格布局管理器类
import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView类

import com.micsig.base.keyevent.KeyEventUtil; // 导入按键事件工具类
import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.base.filter.FilterFactory; // 导入过滤器工厂类
import com.micsig.base.filter.UnitInputFilter; // 导入单位输入过滤器类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import java.util.ArrayList; // 导入数组列表类
import java.util.Arrays; // 导入数组工具类
import java.util.List; // 导入列表类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 文本输入对话框                                │
 * │ 核心职责：提供文本/数字/符号输入的虚拟键盘，支持中英文和拼音输入     │
 * │ 架构设计：继承LinearLayout，实现SpecialKey接口，                   │
 * │          内嵌RecyclerView网格键盘 + TopDialogCandidatesWord候选词   │
 * │          支持多种输入模式（全键盘/纯字母/纯数字/双精度/限制符号）   │
 * │ 数据流向：setData → 用户输入 → onDismiss回调返回文本               │
 * │ 依赖关系：KeyBoardTextAdapter, TopDialogCandidatesWord, PinyinIME  │
 * │ 使用场景：通道标签、文件名、数学表达式等文本输入                    │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/11/30.
 */

public class TopDialogTextKeyBoard extends LinearLayout implements KeyBoardTextItem.SpecialKey { // 文本键盘对话框
    private static final String TAG = "TopDialogTextKeyBoard"; // 日志标签
    public static final int INPUT_TYPE_ALL = 21;//全部可点击 // 全类型输入（所有按键可点击）
    public static final int INPUT_TYPE_LETTER = 22;//只有字母可点击 // 仅字母输入
    public static final int INPUT_TYPE_NUMBER_INT = 23;//只有数字可点击 // 仅整数输入
    public static final int INPUT_TYPE_NUMBER_DOUBLE = 24;//只有数字和小数点可点击 // 仅双精度数输入
    public static final int INPUT_TYPE_ALL_BUT_SYMBOL = 25;//除了不可切换到符号页面之外都可以 // 除符号外可输入
    public static final int INPUT_TYPE_ALL_POINT_LINE = 26;//字母数字下划线小数点可点击 // 字母数字下划线小数点

    public static final int HANDLE_TYPE_CHANNEL_LABEL = 1;//标签长度 // 处理类型：通道标签
    public static final int HANDLE_TYPE_SAVE_SESSION = 2;//一键存储中 文件名长度 // 处理类型：文件名
    public static final int HANDLE_TYPE_MATH = 3; //高级数学/axb 中单位 // 处理类型：数学表达式
    public int currentHandleType = 0; // 当前处理类型

    private Context context; // 上下文对象
    private EditText tvShow; // 输入显示编辑框
    private KeyBoardTextAdapter adapter; // 按键适配器
    private TopDialogCandidatesWord layoutCandidatesWord; // 候选词面板
    private boolean isEnglish = false;//强制只能输入英文 // 是否强制英文模式

    private int inputType = INPUT_TYPE_ALL; // 当前输入类型
    private int inputLength = -1; // 输入长度限制（-1为不限制）
    private int inputDBig = 1;//输入类型为double时，最大值的整数部分位数 // 双精度整数部分最大位数
    private int inputDSmall = 1;//输入类型为double时，最大值的小数部分位数 // 双精度小数部分最大位数

    private List<KeyBoardTextItem> curList = new ArrayList<>(); // 当前显示的按键列表
    private List<KeyBoardTextItem> letterLowerList = new ArrayList<>(); // 小写字母按键列表
    private List<KeyBoardTextItem> letterUpperList = new ArrayList<>(); // 大写字母按键列表
    private List<KeyBoardTextItem> numberList = new ArrayList<>(); // 数字按键列表
    private List<KeyBoardTextItem> symbolList = new ArrayList<>(); // 符号按键列表
    private OnDialogDismissListener dismissListener; // 关闭回调监听器

    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 关闭回调接口，返回输入结果
     */
    public interface OnDialogDismissListener { // 关闭回调接口
        /**
         * 键盘关闭时回调
         * @param result 输入结果字符串
         */
        void onDismiss(String result); // 关闭回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogTextKeyBoard(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogTextKeyBoard(Context context, @Nullable AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogTextKeyBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() { // 初始化方法
        setOrientation(VERTICAL); // 设置垂直方向
        setClickable(true); // 设置可点击
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_keyboard_text, this); // 加载文本键盘布局

        View outView = rootViewGroup.findViewById(R.id.outView); // 获取外部区域
        outView.setOnClickListener((v)->{ // 外部区域点击监听
            hide(); // 隐藏键盘
            layoutCandidatesWord.hide(); // 隐藏候选词面板
        });
        tvShow = (EditText) rootViewGroup.findViewById(R.id.show); // 获取输入编辑框
        tvShow.setShowSoftInputOnFocus(false); // 禁用系统软键盘
        tvShow.setLongClickable(false); // 禁用长按
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
                    boolean needHandle = currentHandleType == HANDLE_TYPE_CHANNEL_LABEL || currentHandleType == HANDLE_TYPE_MATH; // 判断是否需要特殊处理
                    if (KeyEventUtil.ignoreKeyForLabel(event) && needHandle) { // 如果是需要忽略的按键且需要特殊处理
                        return true; // 消费事件
                    }
                    if (KeyEventUtil.isConfirmKey(event)) { // 如果是确认键
                        inputDone(); // 完成输入
                        return true; // 消费事件
                    }
                }
                return false; // 不消费其他事件
            }
        });


        tvShow.addTextChangedListener(new TextWatcher() { // 添加文本变化监听
            final String cnSymbols ="～｀！＠＃￥％…＆＊（）＿＋—－＝【】［］｛｝｜、：；"'"《》？，。／"; // 中文符号集合
            final String enSymbols =" ~`!@#$%^&*()_-+=[]\\;',./<>?:\"{}|"; // 英文符号集合
            private  String lastText=""; // 上次文本内容
            private void SendDelEventInputMethod(){ // 发送删除事件给输入法
                post(new Runnable() { // 在主线程执行
                    @Override
                    public void run() { // 运行方法
                        InputMethodManager imm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE); // 获取输入法管理器
                        if(imm!=null){ // 如果管理器不为空
                            tvShow.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL)); // 发送删除按下事件
                            tvShow.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL)); // 发送删除释放事件
                        }
                    }
                });
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 文本变化前（空实现）
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // 文本变化中（空实现）

            }

            @Override
            public void afterTextChanged(Editable s3) { // 文本变化后
                String currentText= s3.toString(); // 获取当前文本
                int currentPos=currentText.length(); // 当前文本长度
                if(currentText.length()>lastText.length()){ // 如果文本变长（新增字符）
                    String newChar=currentText.substring(currentPos-1); // 获取新增字符
                    //何种情况均不允许中文符号出现
                    if(cnSymbols.contains(newChar)){ // 如果是中文符号
                        SendDelEventInputMethod(); // 发送删除事件
                    }
                    if (inputType == INPUT_TYPE_ALL_BUT_SYMBOL && enSymbols.contains(newChar)){ // 如果是限制符号模式且是英文符号
                        SendDelEventInputMethod(); // 发送删除事件
                    }
                }
                if (inputType == INPUT_TYPE_NUMBER_DOUBLE) { // 如果是双精度输入模式
                    String s = s3.toString(); // 获取文本
                    if (s.contains(".")) { // 如果包含小数点
                        String[] split = s.split("\\."); // 按小数点分割
                        if (split[0].length() > inputDBig || (split.length == 2 && split[1].length() > inputDSmall)) { // 如果整数或小数部分超长
                            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除最后输入的字符
                        }
                    } else { // 不包含小数点
                        if (s.length() > inputDBig) { // 如果整数部分超长
                            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除最后输入的字符
                        }
                    }
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) rootViewGroup.findViewById(R.id.gridView); // 获取按键网格
        recyclerView.requestFocus(); // 请求焦点
        GridLayoutManager layoutManager = new GridLayoutManager(context, 22) { // 创建22列网格布局管理器
            @Override
            public boolean canScrollVertically() { // 是否可垂直滚动
                return false;//禁止垂直滚动
            }
        };
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() { // 设置SpanSize查询
            @Override
            public int getSpanSize(int position) { // 获取指定位置的SpanSize
                return adapter.getSpanSize(position); // 委托给适配器
            }
        });
        recyclerView.setLayoutManager(layoutManager); // 设置布局管理器
        curList.clear(); // 清空当前列表
        isEnglish = true; // 默认英文模式
        initKeyBoardData(false); // 初始化键盘数据
        curList.addAll(letterLowerList); // 默认显示小写字母
        adapter = new KeyBoardTextAdapter(context, curList); // 创建适配器
        adapter.setOnItemClickListener(onItemClickListener); // 设置按键点击监听
        recyclerView.setAdapter(adapter); // 设置适配器
        hide(); // 初始隐藏

        layoutCandidatesWord = (TopDialogCandidatesWord) rootViewGroup.findViewById(R.id.candidatesWordView); // 获取候选词面板
    }

    /**
     * 获取候选词面板
     * @return 候选词面板实例
     */
    public TopDialogCandidatesWord getLayoutCandidatesWord() { // 获取候选词面板方法
        return layoutCandidatesWord; // 返回候选词面板
    }

    /**
     * 显示键盘
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TEXTKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogTextKeyBoard",rootViewGroup); // 打印控件位置信息
    }

    /**
     * 隐藏键盘
     */
    public void hide() { // 隐藏方法
        tvShow.setInputType(InputType.TYPE_CLASS_TEXT); // 恢复文本输入类型
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TEXTKEYBOARD); // 发送对话框关闭事件
    }

    /**
     * 判断候选词面板是否正在显示
     * @return true表示候选词面板正在显示
     */
    public boolean isCandidatesWordShow(){ // 判断候选词是否显示方法
        return layoutCandidatesWord.isShowing(); // 返回候选词面板显示状态
    }

    /**
     * 内部设置数据方法
     * @param text 初始文本
     * @param inputType 输入类型
     * @param dismissListener 关闭回调
     */
    private void setData(String text, int inputType, OnDialogDismissListener dismissListener) { // 内部设置数据方法
        this.inputType = inputType; // 保存输入类型
        this.dismissListener = dismissListener; // 保存回调
        tvShow.requestFocus(); // 请求输入框焦点
        tvShow.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputLength)}); // 设置长度过滤器
        tvShow.setText(text); // 设置初始文本
        tvShow.setSelection(Math.min(text.length(), tvShow.getText().length())); // 设置光标位置
        switch (this.inputType) { // 根据输入类型切换键盘布局
            case INPUT_TYPE_ALL: // 全类型
            case INPUT_TYPE_ALL_BUT_SYMBOL: // 除符号外
            case INPUT_TYPE_LETTER: // 仅字母
            case INPUT_TYPE_ALL_POINT_LINE: // 字母数字下划线小数点
                adapter.setUpper(false); // 重置大写状态
                curList.clear(); // 清空当前列表
                curList.addAll(letterLowerList); // 显示小写字母
                adapter.notifyDataSetChanged(); // 通知数据变化
                break;
            case INPUT_TYPE_NUMBER_INT: // 仅整数
                adapter.setUpper(false); // 重置大写状态
                curList.clear(); // 清空当前列表
                curList.addAll(numberList); // 显示数字
                adapter.notifyDataSetChanged(); // 通知数据变化
                tvShow.setInputType(InputType.TYPE_CLASS_NUMBER); // 设置数字输入类型
                break;
            case INPUT_TYPE_NUMBER_DOUBLE: // 仅双精度
                adapter.setUpper(false); // 重置大写状态
                curList.clear(); // 清空当前列表
                curList.addAll(numberList); // 显示数字
                adapter.notifyDataSetChanged(); // 通知数据变化
                break;
        }
        show(); // 显示键盘
        layoutCandidatesWord.hide(); // 隐藏候选词面板
    }

    /**
     * 设置数据（全类型，不限长度）
     * @param text 初始文本
     * @param dismissListener 关闭回调
     */
    public void setData(String text, OnDialogDismissListener dismissListener) { // 设置数据（全类型）
        this.inputLength = -1; // 不限长度
        initKeyBoardData(false); // 初始化键盘数据
        setData(text, INPUT_TYPE_ALL, dismissListener); // 委托给内部方法
    }

    /**
     * 设置数据（指定输入类型和长度）
     * @param text 初始文本
     * @param inputType 输入类型
     * @param inputLength 输入长度限制
     * @param dismissListener 关闭回调
     */
    public void setData(String text, int inputType, int inputLength, OnDialogDismissListener dismissListener) { // 设置数据（含长度限制）
        this.inputLength = inputLength; // 保存长度限制
        initKeyBoardData(false); // 初始化键盘数据
        setData(text, inputType, dismissListener); // 委托给内部方法
    }

    /**
     * 设置数据（指定处理类型、输入类型和长度）
     * @param text 初始文本
     * @param handleType 处理类型
     * @param inputType 输入类型
     * @param inputLength 输入长度限制
     * @param dismissListener 关闭回调
     */
    public void setData(String text, int handleType, int inputType, int inputLength, OnDialogDismissListener dismissListener) { // 设置数据（含处理类型）
        this.inputLength = inputLength; // 保存长度限制
        this.currentHandleType = handleType; // 保存处理类型
        initKeyBoardData(false); // 初始化键盘数据
        setData(text, inputType, dismissListener); // 委托给内部方法
    }

    /**
     * 设置英文模式数据
     * @param handleType 处理类型
     * @param text 初始文本
     * @param inputLength 输入长度限制
     * @param dismissListener 关闭回调
     */
    public void setDataEnglish(int handleType, String text, int inputLength, OnDialogDismissListener dismissListener) { // 设置英文模式数据
        this.currentHandleType = handleType; // 保存处理类型
        this.inputLength = inputLength; // 保存长度限制
        inputType = INPUT_TYPE_ALL; // 设置为全类型
        initKeyBoardData(true); // 初始化英文键盘数据
        setData(text, inputType, dismissListener); // 委托给内部方法
    }

    /**
     * 设置双精度数数据
     * @param text 初始值
     * @param maxValueBig 整数部分最大位数
     * @param maxValueSmall 小数部分最大位数
     * @param dismissListener 关闭回调
     */
    public void setDataDouble(double text, int maxValueBig, int maxValueSmall, OnDialogDismissListener dismissListener) { // 设置双精度数据
        inputDBig = maxValueBig; // 保存整数部分位数
        inputDSmall = maxValueSmall; // 保存小数部分位数
        inputLength = inputDBig + inputDSmall + 1; // 计算总长度（含小数点）
        initKeyBoardData(false); // 初始化键盘数据
        setData(String.valueOf(text), INPUT_TYPE_NUMBER_DOUBLE, dismissListener); // 委托给内部方法
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter(""), new InputFilter.LengthFilter(inputLength)}); // 设置单位过滤器和长度过滤器
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD); // 设置密码输入类型
    }

    /**
     * 初始化键盘按键数据
     * @param isEnglish 是否强制英文模式
     */
    private void initKeyBoardData(boolean isEnglish) { // 初始化键盘数据方法
        if (this.isEnglish == isEnglish) { // 如果语言模式未变化
            return; // 不重新初始化
        }
        this.isEnglish = isEnglish; // 更新语言模式
        letterLowerList.clear(); // 清空小写列表
        letterUpperList.clear(); // 清空大写列表
        numberList.clear(); // 清空数字列表
        symbolList.clear(); // 清空符号列表
        boolean isLangCn = StrUtil.isLangCn(); // 判断系统语言是否中文
        String enter = isLangCn ? ENTER_CN : ENTER; // 确认键文字
        String hide = isLangCn ? HIDE_CN : HIDE; // 隐藏键文字
        String lang = isLangCn && !isEnglish ? LANG_CN : LANG_ENG; // 语言切换键文字
        String[][] letterLowers = { // 小写字母布局
                {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", DELETE}, // 第一行
                {PLACEHOLDER, "a", "s", "d", "f", "g", "h", "j", "k", "l", enter}, // 第二行
                {UPPER, "z", "x", "c", "v", "b", "n", "m", ",", ".", "-"}, // 第三行
                {hide, NUMBER, SPACE, SYMBOL, lang} // 第四行
        };
        String[][] letterUppers = { // 大写字母布局
                {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", DELETE}, // 第一行
                {PLACEHOLDER, "A", "S", "D", "F", "G", "H", "J", "K", "L", enter}, // 第二行
                {UPPER, "Z", "X", "C", "V", "B", "N", "M", ",", ".", "-"}, // 第三行
                {hide, NUMBER, SPACE, SYMBOL, lang} // 第四行
        };
        String[][] numbers = { // 数字布局
                {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", DELETE}, // 第一行
                {PLACEHOLDER, "a", "s", "d", "f", "g", "h", "j", "k", "l", enter}, // 第二行
                {UPPER, "z", "x", "c", "v", "b", "n", "m", ",", ".", "-"}, // 第三行
                {hide, NUMBER, SPACE, SYMBOL, lang} // 第四行
        };
        String[][] symbols = { // 符号布局
                {"[", "]", "{", "}", "#", "%", "^", "*", "+", "=", DELETE}, // 第一行
                {PLACEHOLDER, "_", "\\", "|", "~", "<", ">", "?", "!", "'", enter}, // 第二行
                {UPPER, "·", "/", ":", ";", "(", ")", "$", "&", "@", "`"}, // 第三行
                {hide, NUMBER, SPACE, SYMBOL, lang} // 第四行
        };
        for (int i = 0; i < letterLowers[0].length; i++) { // 遍历第一行
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[0][i], 2)); // 添加小写按键（占2格）
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[0][i], 2)); // 添加大写按键
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[0][i], 2)); // 添加数字按键
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[0][i], 2)); // 添加符号按键
        }
        for (int i = 0; i < letterLowers[1].length; i++) { // 遍历第二行
            int size; // 占用格数
            if (letterLowers[1][i].equals(PLACEHOLDER)) { // 如果是占位键
                size = 1; // 占1格
            } else if (letterLowers[1][i].equals(enter)) { // 如果是确认键
                size = 3; // 占3格
            } else { // 其他按键
                size = 2; // 占2格
            }

            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[1][i], size)); // 添加小写按键
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[1][i], size)); // 添加大写按键
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[1][i], size)); // 添加数字按键
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[1][i], size)); // 添加符号按键
        }
        for (int i = 0; i < letterLowers[2].length; i++) { // 遍历第三行
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[2][i], 2)); // 添加小写按键
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[2][i], 2)); // 添加大写按键
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[2][i], 2)); // 添加数字按键
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[2][i], 2)); // 添加符号按键
        }
        for (int i = 0; i < letterLowers[3].length; i++) { // 遍历第四行
            int size; // 占用格数
            if (letterLowers[3][i].equals(SPACE)) { // 如果是空格键
                size = 10; // 占10格
            } else { // 其他按键
                size = 3; // 占3格
            }
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[3][i], size)); // 添加小写按键
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[3][i], size)); // 添加大写按键
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[3][i], size)); // 添加数字按键
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[3][i], size)); // 添加符号按键
        }
        Logger.i("letterLowerList:" + Arrays.toString(letterLowerList.toArray())); // 打印小写列表日志
    }

    /**
     * 按键点击事件监听器
     */
    private KeyBoardTextAdapter.OnItemClickListener onItemClickListener = new KeyBoardTextAdapter.OnItemClickListener() { // 按键点击监听器
        @Override
        public void onQuickDelete() { // 长按删除回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (layoutCandidatesWord.isShowing() && !StrUtil.isEmpty(layoutCandidatesWord.getPinyin())) { // 如果候选词面板显示中且有拼音
                layoutCandidatesWord.setData("", onDialogClickWordListener); // 清空候选词
            } else if (tvShow.getSelectionStart() != 0) { // 如果输入框有内容
                tvShow.setText(""); // 清空所有文本
            }
        }

        @Override
        public void onDelete() { // 单次删除回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (layoutCandidatesWord.isShowing() && !StrUtil.isEmpty(layoutCandidatesWord.getPinyin())) { // 如果候选词面板显示中且有拼音
                String pinyin = layoutCandidatesWord.getPinyin(); // 获取当前拼音
                layoutCandidatesWord.setData(pinyin.substring(0, pinyin.length() - 1), onDialogClickWordListener); // 删除最后一个拼音字母
            } else if (tvShow.getSelectionStart() != 0) { // 如果输入框有内容
                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除光标前一个字符
            }
        }

        @Override
        public void onEnter() { // 确认回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            String pinyin = layoutCandidatesWord.getPinyin(); // 获取当前拼音
            if(!StrUtil.isEmpty(pinyin)){ // 如果有未选中的拼音
                //如果在打中文，未选择时点enter，直接打出英文
                tvShow.getText().insert(tvShow.getSelectionStart(), pinyin); // 将拼音作为英文插入
                layoutCandidatesWord.hide(); // 隐藏候选词面板
                return; // 不执行确认关闭
            }
            String result = tvShow.getText().toString(); // 获取输入文本
            Logger.i("tvShow=" + tvShow.getText().toString() + " pinyin=" + pinyin); // 打印日志

            if (currentHandleType == HANDLE_TYPE_SAVE_SESSION) { // 如果是文件名模式
                if (result.isEmpty()) { // 如果文件名为空
                    DToast.get().show(R.string.file_name_not_null); // 提示文件名不能为空
                    return; // 不关闭
                }
            }

            if (dismissListener != null) { // 如果回调不为空
                dismissListener.onDismiss(result); // 回调输入结果
            }
            hide(); // 隐藏键盘
            layoutCandidatesWord.hide(); // 隐藏候选词面板
        }

        @Override
        public void onUpper(boolean isUpper) { // 大写切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (isUpper) { // 切换到大写
                curList.clear(); // 清空当前列表
                curList.addAll(letterUpperList); // 显示大写字母
                adapter.notifyDataSetChanged(); // 通知数据变化
            } else { // 切换到小写
                curList.clear(); // 清空当前列表
                curList.addAll(letterLowerList); // 显示小写字母
                adapter.notifyDataSetChanged(); // 通知数据变化
            }
        }

        @Override
        public void onHide() { // 隐藏回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            hide(); // 隐藏键盘
            layoutCandidatesWord.hide(); // 隐藏候选词面板
        }

        @Override
        public void onNumber() { // 数字切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            adapter.setUpper(false); // 重置大写状态
            if (curList.contains(numberList.get(0))) { // 如果当前已是数字键盘
                curList.clear(); // 清空当前列表
                curList.addAll(letterLowerList); // 切换回小写字母
                adapter.notifyDataSetChanged(); // 通知数据变化
            } else { // 当前不是数字键盘
                curList.clear(); // 清空当前列表
                curList.addAll(numberList); // 切换到数字
                adapter.notifyDataSetChanged(); // 通知数据变化
            }
        }

        @Override
        public void onSymbol() { // 符号切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (inputType == INPUT_TYPE_ALL_BUT_SYMBOL // 如果是限制符号模式
                    || inputType == INPUT_TYPE_NUMBER_INT // 或整数模式
                    || inputType == INPUT_TYPE_NUMBER_DOUBLE // 或双精度模式
            ) return; // 不允许切换到符号
            adapter.setUpper(false); // 重置大写状态
            if (curList.contains(symbolList.get(0))) { // 如果当前已是符号键盘
                curList.clear(); // 清空当前列表
                curList.addAll(letterLowerList); // 切换回小写字母
                adapter.notifyDataSetChanged(); // 通知数据变化
            } else { // 当前不是符号键盘
                boolean isEng = curList.get(INDEX_LANG).getWord().equals(LANG_ENG); // 记录当前语言状态
                curList.clear(); // 清空当前列表
                curList.addAll(symbolList); // 切换到符号
                if (isEng) { // 如果是英文
                    curList.get(INDEX_LANG).setWord(LANG_ENG); // 设置英文标识
                    curList.get(INDEX_ENTER).setWord(ENTER); // 设置英文确认
                    curList.get(INDEX_HIDE).setWord(HIDE); // 设置英文隐藏
                } else { // 中文
                    curList.get(INDEX_LANG).setWord(LANG_CN); // 设置中文标识
                    curList.get(INDEX_ENTER).setWord(ENTER_CN); // 设置中文确认
                    curList.get(INDEX_HIDE).setWord(HIDE_CN); // 设置中文隐藏
                }
                adapter.notifyDataSetChanged(); // 通知数据变化
            }
        }

        @Override
        public void onEnglish(boolean isEng) { // 语言切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (!StrUtil.isLangCn() || isEnglish) { // 如果系统不是中文或强制英文模式
                return; // 不切换
            }
            layoutCandidatesWord.setData("", onDialogClickWordListener); // 重置候选词
            if (isEng) { // 当前是英文，切换到中文
                curList.get(INDEX_LANG).setWord(LANG_CN); // 设置中文标识
                curList.get(INDEX_ENTER).setWord(ENTER_CN); // 设置中文确认
                curList.get(INDEX_HIDE).setWord(HIDE_CN); // 设置中文隐藏
            } else { // 当前是中文，切换到英文
                curList.get(INDEX_LANG).setWord(LANG_ENG); // 设置英文标识
                curList.get(INDEX_ENTER).setWord(ENTER); // 设置英文确认
                curList.get(INDEX_HIDE).setWord(HIDE); // 设置英文隐藏
            }
            adapter.notifyDataSetChanged(); // 通知数据变化
        }

        @Override
        public void onOtherKey(String keyWord) { // 其他按键回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (currentHandleType == HANDLE_TYPE_CHANNEL_LABEL // 如果是通道标签模式
                    || currentHandleType == HANDLE_TYPE_SAVE_SESSION) { // 或文件名模式
                if (tvShow.getText().toString().getBytes().length >= inputLength) { // 如果字节长度达到限制
                    DToast.get().show(R.string.keyBoardMsgBeyond); // 提示超出限制
                    return; // 不输入
                }
            }
            if (inputType != INPUT_TYPE_NUMBER_DOUBLE) { // 如果不是双精度模式
                if (inputLength != -1 && tvShow.getText().toString().length() >= inputLength) { // 如果字符长度达到限制
                    DToast.get().show(R.string.keyBoardMsgBeyond); // 提示超出限制
                    return; // 不输入
                }
            }
            switch (inputType) { // 根据输入类型处理
                case INPUT_TYPE_ALL_POINT_LINE: // 字母数字下划线小数点
                    if (!StrUtil.isNumberLetter(keyWord)) { // 如果不是数字或字母
                        if (!".".equals(keyWord) && !"_".equals(keyWord)) { // 如果不是小数点或下划线
                            break; // 不输入
                        }
                    }
                case INPUT_TYPE_ALL_BUT_SYMBOL: // 除符号外
                    String curLang = curList.get(INDEX_LANG).getWord(); // 获取当前语言
                    if (curLang.equals(LANG_CN) && StrUtil.isLetter(keyWord)) { // 如果是中文模式且输入字母
                        layoutCandidatesWord.setData(layoutCandidatesWord.getPinyin() + keyWord,
                                onDialogClickWordListener); // 追加拼音并显示候选词
                    } else { // 非字母或英文模式
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord); // 直接插入文本
                    }
                    break;
                case INPUT_TYPE_ALL: // 全类型
                    boolean needHandle = currentHandleType == HANDLE_TYPE_CHANNEL_LABEL || currentHandleType == HANDLE_TYPE_MATH; // 判断是否需要特殊处理
                    if (ignoreSymbols(keyWord) && needHandle) break; // 如果是需要忽略的符号且需要特殊处理
                    String curLangAll = curList.get(INDEX_LANG).getWord(); // 获取当前语言
                    if (curLangAll.equals(LANG_CN) && StrUtil.isLetter(keyWord)) { // 如果是中文模式且输入字母
                        layoutCandidatesWord.setData(layoutCandidatesWord.getPinyin() + keyWord,
                                onDialogClickWordListener); // 追加拼音并显示候选词
                    } else { // 非字母或英文模式
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord); // 直接插入文本
                    }
                    break;
                case INPUT_TYPE_LETTER: // 仅字母
                    if (StrUtil.isLetter(keyWord)) { // 如果是字母
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord); // 插入字母
                    }
                    break;
                case INPUT_TYPE_NUMBER_INT: // 仅整数
                    if (StrUtil.isNumber(keyWord)) { // 如果是数字
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord); // 插入数字
                    }
                    break;
                case INPUT_TYPE_NUMBER_DOUBLE: // 双精度

                    if ((".".equals(keyWord) && !StrUtil.isEmpty(tvShow.getText().toString()) && !tvShow.getText().toString().contains(".")) // 如果是小数点且文本非空且不包含小数点
                            || StrUtil.isNumber(keyWord)) { // 或是数字
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord); // 插入字符

                        String s = tvShow.getText().toString(); // 获取当前文本
                        if (s.contains(".")) { // 如果包含小数点
                            String[] split = s.split("\\."); // 按小数点分割
                            if (split[0].length() > inputDBig || (split.length == 2 && split[1].length() > inputDSmall)) { // 如果整数或小数部分超长
                                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除最后输入的字符
                            }
                        } else { // 不包含小数点
                            if (s.length() > inputDBig) { // 如果整数部分超长
                                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除最后输入的字符
                            }
                        }
                    }
                    break;
            }
        }
    };

    /**
     * 判断是否是需要忽略的符号
     * @param keyWord 按键文字
     * @return true表示需要忽略
     */
    private boolean ignoreSymbols(String keyWord) {//限制输入的符号
        return ",".equals(keyWord) || ":".equals(keyWord) || ";".equals(keyWord); // 逗号、冒号、分号需要忽略
    }

    /**
     * 候选词选中回调监听器
     */
    private TopDialogCandidatesWord.OnDialogClickWordListener onDialogClickWordListener = new TopDialogCandidatesWord.OnDialogClickWordListener() { // 候选词选中监听器
        @Override
        public boolean onClickWord(String s) { // 候选词选中回调

            if (currentHandleType == HANDLE_TYPE_CHANNEL_LABEL // 如果是通道标签模式
                    || currentHandleType == HANDLE_TYPE_SAVE_SESSION // 或文件名模式
            ) {
                String newInput = tvShow.getText().toString().trim() + s.trim(); // 计算新输入内容
                if (inputLength != -1 && newInput.getBytes().length >= inputLength) { // 如果字节长度达到限制
                    DToast.get().show(R.string.keyBoardMsgBeyond); // 提示超出限制
                    return false; // 返回false表示不成功
                }
            }

            if (inputLength != -1 && (tvShow.getText().toString().length() + s.length()) > inputLength) { // 如果字符长度超限
                DToast.get().show(R.string.keyBoardMsgBeyond); // 提示超出限制
                return false; // 返回false表示不成功
            }
            tvShow.getText().insert(tvShow.getSelectionStart(), s); // 插入选中的文字
            return true; // 返回true表示成功
        }
    };

    /**
     * 获取EditText光标所在的位置
     * @return 光标位置
     */
    private int getEditTextCursorIndex() { // 获取光标位置方法
        return tvShow.getSelectionStart(); // 返回光标起始位置
    }

    /**
     * 向EditText指定光标位置插入字符串
     * @param mText 要插入的文本
     */
    private void insertText(String mText) { // 插入文本方法
        tvShow.getText().insert(getEditTextCursorIndex(), mText); // 在光标位置插入
    }

    /**
     * 向EditText指定光标位置删除字符串
     */
    private void deleteText() { // 删除文本方法
        if (!TextUtils.isEmpty(tvShow.getText().toString())) { // 如果文本非空
            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart()); // 删除光标前一个字符
        }
    }

    /**
     * 显示创建文件夹标题
     */
    public void showTitle(){ // 显示标题方法
        TextView createTile = findViewById(R.id.createFolderTitle); // 获取标题文本
        createTile.setGravity(Gravity.CENTER_HORIZONTAL); // 设置水平居中
        createTile.setVisibility(VISIBLE); // 设置可见
    }

    /**
     * 物理确认键完成输入
     */
    private void inputDone() { // 输入完成方法
        String text = tvShow.getText().toString(); // 获取输入文本
        if (dismissListener != null) { // 如果回调不为空
            if (!StrUtil.isEmpty(text)) { // 如果文本非空
                dismissListener.onDismiss(text); // 回调文本
            } else { // 文本为空
                dismissListener.onDismiss(""); // 回调空字符串
            }
        }
        hide(); // 隐藏键盘
    }

}
