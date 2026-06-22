package com.micsig.tbook.ui.top.view; // 顶部视图组件包，存放示波器顶部操作栏的自定义组合控件

import android.content.Context;          // 上下文对象，用于获取资源、系统服务等
import android.content.res.TypedArray;    // 类型化数组，用于读取XML自定义属性
import android.util.AttributeSet;         // 属性集，XML声明式构造时传入的属性集合
import android.view.ActionMode;           // 操作模式回调，用于拦截文本选择菜单（复制/粘贴等）
import android.view.Gravity;              // 对齐方式常量，控制子视图在父容器中的对齐
import android.view.Menu;                 // 菜单接口，ActionMode回调中的菜单对象
import android.view.MenuItem;             // 菜单项接口，ActionMode回调中的菜单项
import android.view.View;                 // 视图基类，Android所有UI组件的根类
import android.widget.Button;             // 按钮控件，用于确认操作
import android.widget.LinearLayout;       // 线性布局，本控件的父类，水平排列子视图
import android.widget.TextView;           // 文本视图，用于显示标题和输入内容

import com.micsig.tbook.ui.R;            // UI模块资源R类，引用布局和自定义属性

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                            TopViewSave                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：UI组件层 / 顶部操作栏 / 保存类组合控件                              │
 * │ 所属包  ：com.micsig.tbook.ui.top.view                                     │
 * │ 父类    ：LinearLayout（水平线性布局）                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                   │
 * │   提供一个"标题 + 输入框 + 确认按钮"的三段式组合控件，用于示波器顶部操作栏      │
 * │   中的保存/命名场景。标题标签说明输入项含义，输入框显示当前值，确认按钮触发       │
 * │   保存操作。输入框点击时通过回调通知外部，由外部决定弹出键盘或其他交互。          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                   │
 * │   - 继承LinearLayout，水平排列三个子视图：title(标题)、input(输入)、confirm(按钮)│
 * │   - 通过XML自定义属性(headWidth/editWidth/buttonWidth)控制三段宽度             │
 * │   - 通过OnSaveClickListener回调接口将用户交互事件委托给外部处理者              │
 * │   - 输入框使用TextView而非EditText，点击行为完全由外部回调控制，避免系统键盘     │
 * │     自动弹出，适配示波器自定义键盘场景                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                   │
 * │   外部 → setData() → 更新title/input/confirm文本和宽度 → 界面刷新              │
 * │   用户点击输入框 → onInputClickListener → OnSaveClickListener.inputClick()     │
 * │   用户点击确认按钮 → onClick → OnSaveClickListener.clickConfirm()              │
 * │   外部 → setText()/getText() → 直接读写input文本内容                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                   │
 * │   - 布局文件：R.layout.view_savewithhead（merge布局，含title/input/confirm）   │
 * │   - 自定义属性：R.styleable.TopViewSave（headWidth/editWidth/buttonWidth）     │
 * │   - 使用方：TopLayoutSaveTxt（TXT保存页面，用于文件命名输入）                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                   │
 * │   XML声明：                                                                  │
 * │     &lt;com.micsig.tbook.ui.top.view.TopViewSave                             │
 * │         android:id="@+id/saveName"                                           │
 * │         app:headWidth="60px"                                                 │
 * │         app:editWidth="200px"                                                │
 * │         app:buttonWidth="96px" /&gt;                                          │
 * │   Java调用：                                                                 │
 * │     TopViewSave saveName = findViewById(R.id.saveName);                      │
 * │     saveName.setData("文件名", "默认名", "保存", listener);                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 布局结构（view_savewithhead.xml）：                                          │
 * │   [title:TextView] [input:TextView] [confirm:Button]                         │
 * │    ← headWidth → ← editWidth → ← buttonWidth →                              │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopViewSave extends LinearLayout { // 继承LinearLayout，水平排列标题/输入/按钮三段式控件

    /** 上下文对象，用于获取字符串资源等系统服务 */
    private Context context; // 保存Activity/Fragment上下文引用

    /** 输入框视图，使用TextView而非EditText，避免系统软键盘自动弹出，由外部回调控制输入交互 */
    private TextView input; // 显示当前输入值的文本视图，点击时触发外部回调

    /** 标题文本内容，显示在输入框左侧，说明输入项的含义（如"文件名"） */
    private String titleString; // 标题标签文本

    /** 输入框预填充文本内容，作为初始值或占位提示 */
    private String inputString; // 输入框默认/预填充文本

    /** 确认按钮文本内容，标识操作类型（如"保存"、"确定"） */
    private String confirmString; // 确认按钮显示文本

    /** 标题区域宽度（像素），通过XML属性headWidth配置，默认100px */
    private int headWidth; // 标题TextView的布局宽度

    /** 输入区域宽度（像素），通过XML属性editWidth配置，默认200px */
    private int editWidth; // 输入TextView的布局宽度

    /** 按钮区域宽度（像素），通过XML属性buttonWidth配置，默认120px */
    private int buttonWidth; // 确认Button的布局宽度

    /** 保存操作点击监听器，外部通过此回调接收输入框点击和确认按钮点击事件 */
    private OnSaveClickListener onSaveClickListener; // 用户交互事件回调接口实例

    /**
     * 保存操作点击监听器接口。
     * <p>
     * 外部实现此接口以接收TopViewSave的两种用户交互事件：
     * 1. 输入框被点击时触发 inputClick()，通常用于弹出自定义键盘
     * 2. 确认按钮被点击时触发 clickConfirm()，通常用于执行保存操作
     * </p>
     */
    public interface OnSaveClickListener { // 交互事件回调接口定义

        /**
         * 确认按钮点击回调。
         * <p>
         * 当用户点击确认按钮时触发，外部应在此方法中执行保存/提交逻辑。
         * </p>
         *
         * @param view  触发事件的TopViewSave实例，便于区分多个TopViewSave
         * @param input 当前输入框中的文本内容
         */
        void clickConfirm(TopViewSave view, String input); // 确认按钮点击事件回调

        /**
         * 输入框点击回调。
         * <p>
         * 当用户点击输入区域时触发，外部应在此方法中弹出自定义键盘或其他输入交互界面。
         * 由于输入框使用TextView而非EditText，系统软键盘不会自动弹出，
         * 需要外部自行处理输入交互。
         * </p>
         *
         * @param view 触发事件的TopViewSave实例，便于区分多个TopViewSave
         * @param text 当前输入框中的文本内容
         */
        void inputClick(TopViewSave view, String text); // 输入框点击事件回调
    }

    /**
     * 单参数构造函数。
     * <p>
     * 用于代码中动态创建TopViewSave实例，委托给三参数构造函数。
     * </p>
     *
     * @param context 上下文对象
     */
    public TopViewSave(Context context) { // 代码动态创建时调用的构造函数
        this(context, null); // 委托给双参数构造，attrs传null
    }

    /**
     * 双参数构造函数。
     * <p>
     * 用于XML布局中声明TopViewSave时由系统反射调用，委托给三参数构造函数。
     * </p>
     *
     * @param context 上下文对象
     * @param attrs   XML属性集合，包含自定义属性headWidth/editWidth/buttonWidth
     */
    public TopViewSave(Context context, AttributeSet attrs) { // XML声明式创建时调用的构造函数
        this(context, attrs, 0); // 委托给三参数构造，defStyleAttr传0（使用默认样式）
    }

    /**
     * 三参数构造函数（完整构造）。
     * <p>
     * 所有构造函数最终委托至此。完成上下文保存和视图初始化。
     * </p>
     *
     * @param context      上下文对象
     * @param attrs        XML属性集合
     * @param defStyleAttr 默认样式属性，0表示不使用默认样式
     */
    public TopViewSave(Context context, AttributeSet attrs, int defStyleAttr) { // 完整构造函数
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout构造函数
        this.context = context; // 保存上下文引用，后续用于获取字符串资源
        initView(context, attrs, defStyleAttr); // 执行视图初始化：加载布局、读取属性、更新UI
    }

    /**
     * 初始化视图。
     * <p>
     * 执行以下步骤：
     * 1. 加载布局文件 view_savewithhead（merge布局，含title/input/confirm三个子视图）
     * 2. 设置LinearLayout为水平方向、子视图垂直居中
     * 3. 从XML自定义属性中读取三段宽度配置（headWidth/editWidth/buttonWidth）
     * 4. 回收TypedArray资源
     * 5. 调用updateView()刷新界面
     * </p>
     *
     * @param context      上下文对象
     * @param attrs        XML属性集合
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 视图初始化方法
        View.inflate(context, R.layout.view_savewithhead, this); // 将merge布局inflate到当前LinearLayout中
        setOrientation(HORIZONTAL); // 设置水平排列方向，标题→输入→按钮从左到右
        setGravity(Gravity.CENTER_VERTICAL); // 子视图垂直居中对齐
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSave); // 获取自定义属性类型化数组
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_headWidth, 100); // 读取标题宽度，默认100px
        editWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_editWidth, 200); // 读取输入宽度，默认200px
        buttonWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_buttonWidth, 120); // 读取按钮宽度，默认120px
        ta.recycle(); // 回收TypedArray，避免内存泄漏
        updateView(); // 刷新子视图的文本和宽度
    }

    /**
     * 设置数据（字符串参数版本）。
     * <p>
     * 设置标题文本、输入框预填充文本、确认按钮文本和事件监听器，
     * 然后刷新界面显示。适用于外部已获取字符串资源的场景。
     * </p>
     *
     * @param title    标题文本，显示在输入框左侧
     * @param preInput 输入框预填充文本，作为初始值
     * @param confirm  确认按钮文本，标识操作类型
     * @param listener 事件监听器，接收输入框点击和确认按钮点击回调
     */
    public void setData(String title, String preInput, String confirm, OnSaveClickListener listener) { // 设置数据（字符串版本）
        this.titleString = title; // 保存标题文本
        this.inputString = preInput; // 保存输入框预填充文本
        this.confirmString = confirm; // 保存确认按钮文本
        this.onSaveClickListener = listener; // 保存事件监听器
        updateView(); // 刷新界面显示
    }

    /**
     * 设置数据（资源ID版本）。
     * <p>
     * 通过字符串资源ID设置标题、输入框预填充文本和确认按钮文本，
     * 内部将资源ID转换为字符串后委托给字符串版本逻辑。
     * 适用于XML字符串资源引用场景，便于国际化。
     * </p>
     *
     * @param titleResId    标题文本的字符串资源ID（如R.string.save_name）
     * @param preInputResId 输入框预填充文本的字符串资源ID
     * @param confirmResId  确认按钮文本的字符串资源ID（如R.string.btn_save）
     * @param listener      事件监听器，接收输入框点击和确认按钮点击回调
     */
    public void setData(int titleResId, int preInputResId, int confirmResId, OnSaveClickListener listener) { // 设置数据（资源ID版本）
        this.titleString = context.getString(titleResId); // 将标题资源ID转换为字符串
        this.inputString = context.getString(preInputResId); // 将输入框预填充资源ID转换为字符串
        this.confirmString = context.getString(confirmResId); // 将确认按钮资源ID转换为字符串
        this.onSaveClickListener = listener; // 保存事件监听器
        updateView(); // 刷新界面显示
    }

    /**
     * 更新视图显示。
     * <p>
     * 根据当前成员变量值刷新所有子视图的文本内容和布局宽度，
     * 并设置输入框和确认按钮的点击监听器。
     * 此方法在initView()和setData()中被调用。
     * </p>
     * <p>
     * 注意：input使用TextView而非EditText，因此点击时不会弹出系统软键盘，
     * 而是通过onInputClickListener回调通知外部，由外部决定输入交互方式
     * （如弹出自定义数字键盘或文本键盘）。
     * </p>
     */
    private void updateView() { // 刷新子视图文本、宽度和点击监听器
        TextView title = (TextView) findViewById(R.id.title); // 获取标题TextView
        input = (TextView) findViewById(R.id.input); // 获取输入TextView
        Button confirm = (Button) findViewById(R.id.confirm); // 获取确认Button
        title.setText(titleString); // 设置标题文本
        input.setText(inputString); // 设置输入框预填充文本
        confirm.setText(confirmString); // 设置确认按钮文本
        LinearLayout.LayoutParams lpTitle = (LayoutParams) title.getLayoutParams(); // 获取标题布局参数
        lpTitle.width = headWidth; // 设置标题宽度为自定义属性值
        title.setLayoutParams(lpTitle); // 应用修改后的布局参数
        LinearLayout.LayoutParams lpInput = (LayoutParams) input.getLayoutParams(); // 获取输入框布局参数
        lpInput.width = editWidth; // 设置输入框宽度为自定义属性值
        input.setLayoutParams(lpInput); // 应用修改后的布局参数
        LinearLayout.LayoutParams lpConfirm = (LayoutParams) confirm.getLayoutParams(); // 获取确认按钮布局参数
        lpConfirm.width = buttonWidth; // 设置确认按钮宽度为自定义属性值
        confirm.setLayoutParams(lpConfirm); // 应用修改后的布局参数

//      input.setShowSoftInputOnFocus(false); // 已注释：禁止输入框获取焦点时弹出软键盘
//      //下面两行作用：屏蔽edittext的复制粘贴功能
//      input.setLongClickable(false); // 已注释：禁止长按弹出复制粘贴菜单
//      input.setCustomSelectionActionModeCallback(onInputActionModeListener); // 已注释：自定义选择操作模式回调
        input.setOnClickListener(onInputClickListener); // 设置输入框点击监听器，点击时通知外部

        confirm.setOnClickListener(new View.OnClickListener() { // 设置确认按钮点击监听器
            @Override
            public void onClick(View v) { // 确认按钮点击事件处理
//              int[] ints1 = new int[2]; // 已注释：调试用，获取输入框屏幕坐标
//              input.getLocationOnScreen(ints1); // 已注释：获取输入框在屏幕上的位置
//              Logger.i("TopViewChannel,input:" + Arrays.toString(ints1) + "\t" + input.getWidth() + "\t" + input.getHeight()); // 已注释：打印输入框位置和尺寸
//              int[] ints2 = new int[2]; // 已注释：调试用，获取按钮屏幕坐标
//              v.getLocationOnScreen(ints2); // 已注释：获取按钮在屏幕上的位置
//              Logger.i("TopViewSave,confirm:" + Arrays.toString(ints2) + "\t" + v.getWidth() + "\t" + v.getHeight()); // 已注释：打印按钮位置和尺寸
                if (onSaveClickListener != null) { // 安全检查：监听器不为空时才回调
                    onSaveClickListener.clickConfirm(TopViewSave.this, input.getText().toString()); // 回调确认点击，传入当前实例和输入框文本
                }
            }
        });
    }

    /**
     * 设置输入框文本内容。
     * <p>
     * 外部通过此方法更新输入框显示的文本，通常在自定义键盘输入完成后调用。
     * </p>
     *
     * @param text 要设置的文本内容
     */
    public void setText(String text) { // 设置输入框文本
        input.setText(text); // 更新input TextView的显示文本
    }

    /**
     * 获取输入框当前文本内容。
     * <p>
     * 外部通过此方法读取输入框中的文本，通常在确认保存时获取用户输入的值。
     * </p>
     *
     * @return 输入框中的文本字符串
     */
    public String getText() { // 获取输入框文本
        return input.getText().toString(); // 返回input TextView的文本内容
    }

    /**
     * 输入框点击监听器。
     * <p>
     * 当用户点击输入区域时，通过OnSaveClickListener.inputClick()回调通知外部。
     * 外部通常在此回调中弹出自定义键盘（数字键盘或文本键盘），
     * 因为input使用TextView而非EditText，系统软键盘不会自动弹出。
     * </p>
     */
    private OnClickListener onInputClickListener = new OnClickListener() { // 输入框点击监听器实例
        @Override
        public void onClick(View v) { // 输入框点击事件处理
            if (onSaveClickListener != null) { // 安全检查：监听器不为空时才回调
                onSaveClickListener.inputClick(TopViewSave.this, input.getText().toString()); // 回调输入框点击，传入当前实例和输入框文本
            }
        }
    };

    /**
     * 输入框ActionMode回调（当前已禁用）。
     * <p>
     * 原设计用于屏蔽TextView的文本选择菜单（复制/粘贴/全选等），
     * 所有回调方法均返回false，表示不创建/不处理ActionMode。
     * 当前代码中未使用此回调（setCustomSelectionActionModeCallback已注释），
     * 保留此成员以备后续需要时启用。
     * </p>
     */
    private ActionMode.Callback onInputActionModeListener = new ActionMode.Callback() { // 文本选择菜单拦截回调（已禁用）
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) { // 创建ActionMode时回调
            return false; // 返回false，阻止ActionMode创建，屏蔽文本选择菜单
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) { // 准备ActionMode菜单时回调
            return false; // 返回false，不准备任何菜单项
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) { // ActionMode菜单项点击时回调
            return false; // 返回false，不处理任何菜单项点击
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) { // ActionMode销毁时回调
            // 无需处理，留空 // 销毁时无需额外操作
        }
    };
}
