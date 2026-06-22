package com.micsig.tbook.ui.top.view.radioGroup; // 定义包路径，属于UI顶层视图的单选按钮组模块

import android.annotation.SuppressLint; // 导入SuppressLint注解，用于抑制lint警告
import android.content.Context; // 导入上下文类，用于获取资源和系统服务
import android.content.res.ColorStateList; // 导入颜色状态列表，用于根据状态改变颜色
import android.content.res.TypedArray; // 导入类型数组，用于读取自定义属性
import android.util.AttributeSet; // 导入属性集，用于XML属性解析
import android.util.Log; // 导入日志工具类
import android.util.TypedValue; // 导入类型值转换工具
import android.view.Gravity; // 导入重力常量，用于设置控件内容对齐方式
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.LinearLayout; // 导入线性布局
import android.widget.RadioButton; // 导入单选按钮控件
import android.widget.RadioGroup; // 导入单选按钮组控件

import com.micsig.tbook.ui.MTextView; // 导入自定义TextView控件
import com.micsig.tbook.ui.R; // 导入资源文件R类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入频道数据Bean类
import com.micsig.tbook.ui.util.ScreenUtil; // 导入屏幕工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import java.util.ArrayList; // 导入ArrayList集合类
import java.util.List; // 导入List接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        TopVIewMultiSelectRadioButton                         ║
 * ║                        多选单选按钮组视图组件                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI顶层视图组件 - 多选单选按钮组                                              ║
 * ║   位于 com.micsig.tbook.ui.top.view.radioGroup 包下                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供支持多选功能的RadioButton组视图                                        ║
 * ║   2. 支持头部标题显示与自定义                                                   ║
 * ║   3. 提供丰富的样式自定义能力（宽度、高度、背景、文字颜色等）                        ║
 * ║   4. 管理多选状态，支持选中项的增删查                                            ║
 * ║   5. 提供点击事件回调接口                                                       ║
 * ║   6. 支持提示功能（Prompt）                                                     ║
 * ║   7. 支持只读模式，禁用未选中按钮                                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系：LinearLayout（线性布局）                                            ║
 * ║   组合关系：包含MTextView（头部标题）+ LinearLayout（按钮容器）                   ║
 * ║   设计模式：观察者模式（OnCheckChangedListener回调）                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   XML属性/setData() → 成员变量存储 → updateView()渲染 → 用户交互 → 回调通知      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   - MTextView：自定义TextView，用于头部标题显示                                 ║
 * ║   - TopBeanChannel：频道数据Bean，用于回调传递选中项信息                          ║
 * ║   - ScreenUtil：屏幕工具类，用于获取视图位置                                     ║
 * ║   - StrUtil：字符串工具类，用于空值判断                                          ║
 * ║   - R.layout.view_selectmultigroupwithhead：布局文件                           ║
 * ║   - R.styleable.TopViewRadioGroup：自定义属性集                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   // XML方式使用                                                               ║
 * ║   <com.micsig.tbook.ui.top.view.radioGroup.TopVIewMultiSelectRadioButton      ║
 * ║       android:layout_width="wrap_content"                                      ║
 * ║       android:layout_height="wrap_content"                                     ║
 * ║       app:head="标题"                                                          ║
 * ║       app:array="@array/my_array" />                                           ║
 * ║                                                                               ║
 * ║   // Java代码方式使用                                                          ║
 * ║   TopVIewMultiSelectRadioButton radioGroup = new TopVIewMultiSelectRadioButton║
 * ║   radioGroup.setData("标题", new String[]{"选项1", "选项2"}, listener);         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   - 选中状态通过selectedRadio列表维护，存储选中项的索引                           ║
 * ║   - 特殊符号（<、>、<>、=、≠）会使用更大的字体                                    ║
 * ║   - setEnabled(false)时，未选中的按钮会被禁用，已选中的按钮保持显示但不可点击        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * @author micsig
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TopVIewMultiSelectRadioButton extends LinearLayout { // 继承LinearLayout，实现水平排列的按钮组

    // ================================ 常量定义 ================================

    /**
     * 日志标签
     * 用于在Logcat中标识当前类的日志输出
     */
    private final String TAG = "TopViewRadioGroup"; // 定义日志标签，用于调试输出

    // ================================ 成员变量 ================================

    /**
     * 上下文对象
     * 用于获取资源、启动Activity等操作
     */
    private Context context; // 保存Activity或Application上下文引用

    /**
     * 头部标题文本
     * 显示在按钮组左侧的标题文字
     */
    private String head; // 头部标题内容

    /**
     * 选项文本数组
     * 存储所有RadioButton的显示文本
     */
    private CharSequence[] array; // 存储选项文本数组

    /**
     * 单选按钮组容器
     * 用于承载所有RadioButton的LinearLayout容器
     */
    private LinearLayout radioGroup; // 按钮组容器，从布局文件中获取

    /**
     * 选中状态变化监听器
     * 当用户点击按钮时触发回调
     */
    private TopVIewMultiSelectRadioButton.OnCheckChangedListener onCheckChangedListener; // 点击事件回调接口

    /**
     * 头部标题TextView
     * 用于显示头部标题的自定义TextView
     */
    private MTextView headView; // 头部标题视图控件

    // ================================ 样式属性 ================================

    /**
     * 头部标题宽度（像素）
     * 控制头部标题区域的宽度
     */
    private int headWidth; // 头部标题宽度，单位：像素

    /**
     * 单个按钮项宽度（像素）
     * 控制每个RadioButton的宽度
     */
    private int itemWidth; // 按钮项宽度，单位：像素

    /**
     * 单个按钮项高度（像素）
     * 控制每个RadioButton的高度
     */
    private int itemHeight; // 按钮项高度，单位：像素

    /**
     * 按钮组背景资源ID
     * 整个按钮组的背景drawable资源
     */
    private int bgRadioGroup; // 按钮组背景资源ID

    /**
     * 单个按钮背景资源ID
     * 每个RadioButton的背景drawable资源
     */
    private int bgRadioButton; // 单个按钮背景资源ID

    /**
     * 按钮文字颜色
     * RadioButton文本的颜色值
     */
    private int itemTextColor; // 按钮文字颜色值

    /**
     * 提示文字颜色
     * 头部标题在提示状态下的颜色
     */
    private int promptTxtColor; // 提示文字颜色值

    /**
     * 按钮文字大小（像素）
     * RadioButton文本的字体大小
     */
    private int itemTextSize; // 按钮文字大小，单位：像素

    // ================================ 状态管理 ================================

    /**
     * 已选中按钮索引列表
     * 存储所有选中RadioButton的位置索引
     * 注意：使用public修饰，外部可直接访问
     */
    public List<Integer> selectedRadio = new ArrayList<Integer>(); // 存储选中项索引的列表

    // ================================ 接口定义 ================================

    /**
     * 选中状态变化监听器接口
     * 定义按钮点击事件的回调方法
     *
     * @author micsig
     * @version 1.0.0
     */
    public interface OnCheckChangedListener { // 定义选中状态变化监听接口

        /**
         * 按钮点击回调
         * 当用户点击某个RadioButton时触发
         *
         * @param view 当前TopVIewMultiSelectRadioButton实例
         * @param item 被点击的按钮对应的数据Bean，包含位置和文本信息
         */
        void onClick(TopVIewMultiSelectRadioButton view, TopBeanChannel item); // 按钮点击事件回调

        /**
         * 播放声音回调
         * 用于在点击时播放音效
         *
         * @param isCheckedSuccess 是否选择成功：
         *                          true - 选择操作成功（从未选中变为选中）
         *                          false - 取消选择或选择失败（已选中再次点击）
         */
        void onClickSound(boolean isCheckedSuccess); // 声音播放回调

        /**
         * 触发提示回调
         * 当按钮设置了提示标记时，点击会触发此回调而非onClick
         */
        void onPrompt(TopVIewMultiSelectRadioButton view); // 提示触发回调
    }

    // ================================ 构造方法 ================================

    /**
     * 单参数构造方法
     * 用于Java代码中直接实例化
     *
     * @param context 上下文对象，通常是Activity或Application
     */
    public TopVIewMultiSelectRadioButton(Context context) { // 单参数构造方法
        this(context, null); // 调用双参数构造方法，attrs传null
    }

    /**
     * 双参数构造方法
     * 用于XML布局文件中实例化（无默认样式）
     *
     * @param context 上下文对象
     * @param attrs   XML属性集，包含布局文件中定义的属性
     */
    public TopVIewMultiSelectRadioButton(Context context, AttributeSet attrs) { // 双参数构造方法
        this(context, attrs, 0); // 调用三参数构造方法，defStyleAttr传0
    }

    /**
     * 三参数构造方法
     * 用于XML布局文件中实例化（带默认样式）
     *
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopVIewMultiSelectRadioButton(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        this.context = context; // 保存上下文引用
        initView(context, attrs, defStyleAttr); // 初始化视图
    }

    // ================================ 初始化方法 ================================

    /**
     * 初始化视图
     * 加载布局文件并解析自定义属性
     *
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 初始化视图方法
        View.inflate(context, R.layout.view_selectmultigroupwithhead, this); // 加载布局文件到当前View
        setOrientation(HORIZONTAL); // 设置布局方向为水平排列
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewRadioGroup); // 获取自定义属性数组
        head = ta.getString(R.styleable.TopViewRadioGroup_head); // 读取头部标题属性
        array = ta.getTextArray(R.styleable.TopViewRadioGroup_array); // 读取选项数组属性
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_headWidth, 100); // 读取头部宽度，默认100px
        itemWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemWidth, 120); // 读取按钮宽度，默认120px
        itemHeight = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemHeight, 60); // 读取按钮高度，默认60px
        bgRadioGroup = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioGroup, R.drawable.bg_radiogroup_selectwithhead); // 读取按钮组背景，使用默认值
        bgRadioButton = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioButton, R.drawable.bg_radiobutton_middle); // 读取按钮背景，使用默认值
        itemTextColor = ta.getColor(R.styleable.TopViewRadioGroup_itemTextColor, getResources().getColor(R.color.textColorNewTopViewEnable)); // 读取文字颜色，使用默认值
        itemTextSize = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_android_textSize, 20); // 读取文字大小，默认20px
        ta.recycle(); // 回收TypedArray，释放资源

        headView = findViewById(R.id.head); // 获取头部标题TextView
        headView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize); // 设置头部标题文字大小

        if (array != null && array.length > 0) { // 判断选项数组是否有效
            updateView(); // 更新视图，创建RadioButton
        }
    }

    // ================================ 监听器设置 ================================

    /**
     * 设置选中状态变化监听器
     *
     * @param onCheckChangedListener 监听器实例，用于接收点击事件回调
     */
    public void setOnListener(TopVIewMultiSelectRadioButton.OnCheckChangedListener onCheckChangedListener) { // 设置监听器方法
        this.onCheckChangedListener = onCheckChangedListener; // 保存监听器引用
    }

    // ================================ 样式设置方法 ================================

    /**
     * 设置按钮项宽度
     *
     * @param itemWidth 按钮宽度值，单位：像素
     */
    public void setItemWidth(int itemWidth) { // 设置按钮宽度方法
        this.itemWidth = itemWidth; // 更新成员变量
    }

    /**
     * 设置头部标题宽度
     *
     * @param headWidth 头部宽度值，单位：像素
     */
    public void setHeadWidth(int headWidth) { // 设置头部宽度方法
        this.headWidth = headWidth; // 更新成员变量
    }

    // ================================ 数据设置方法 ================================

    /**
     * 设置数据（字符串数组形式）
     * 通过代码动态设置头部标题和选项内容
     *
     * @param head     头部标题文本
     * @param array    选项文本数组
     * @param listener 点击事件监听器
     */
    public void setData(String head, String[] array, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) { // 设置数据方法（字符串数组）
        this.head = head; // 设置头部标题
        this.array = array; // 设置选项数组
        this.onCheckChangedListener = listener; // 设置监听器
        updateView(); // 更新视图
    }

    /**
     * 设置数据（资源ID形式）
     * 通过资源ID设置头部标题和选项内容
     *
     * @param headResId  头部标题字符串资源ID
     * @param arrayResId 选项数组资源ID
     * @param listener   点击事件监听器
     */
    public void setData(int headResId, int arrayResId, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) { // 设置数据方法（资源ID）
        this.head = context.getString(headResId); // 从资源获取头部标题
        this.array = context.getResources().getStringArray(arrayResId); // 从资源获取选项数组
        this.onCheckChangedListener = listener; // 设置监听器
        updateView(); // 更新视图
    }

    /**
     * 设置数据（混合形式）
     * 头部标题使用字符串，选项使用资源ID
     *
     * @param head      头部标题文本
     * @param arrayResId 选项数组资源ID
     * @param listener  点击事件监听器
     */
    public void setData(String head, int arrayResId, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) { // 设置数据方法（混合形式）
        this.head = head; // 设置头部标题
        this.array = context.getResources().getStringArray(arrayResId); // 从资源获取选项数组
        this.onCheckChangedListener = listener; // 设置监听器
        updateView(); // 更新视图
    }

    // ================================ 视图更新方法 ================================

    /**
     * 更新视图
     * 根据当前数据重新创建并渲染所有RadioButton
     * 注意：此方法会移除所有已存在的RadioButton并重新创建
     */
    @SuppressLint("ResourceType") // 抑制资源ID类型检查警告
    private void updateView() { // 更新视图方法
        MTextView headView = (MTextView) findViewById(R.id.head); // 获取头部标题TextView
        ViewGroup.LayoutParams headParams = headView.getLayoutParams(); // 获取头部布局参数
        headParams.width = headWidth; // 设置头部宽度
        headView.setLayoutParams(headParams); // 应用布局参数
        if (!StrUtil.isEmpty(head)) { // 判断头部标题是否为空
            headView.setText(head); // 设置头部标题文本
        } else { // 头部标题为空
            headView.setVisibility(View.GONE); // 隐藏头部标题
        }

        radioGroup = (LinearLayout) findViewById(R.id.topViewMultiRadioGroup); // 获取按钮组容器
        radioGroup.setBaselineAligned(false); // 禁用基线对齐，提高布局性能
        radioGroup.removeAllViews(); // 移除所有已存在的子视图
        if (array != null && array.length >= 2) { // 判断选项数组有效性（至少2个选项）
            for (int i = 0; i < array.length; i++) { // 遍历所有选项
                RadioButton radioButton = new RadioButton(context); // 创建RadioButton实例
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, itemHeight); // 创建布局参数
                radioButton.setLayoutParams(layoutParams); // 设置布局参数
                radioButton.setGravity(Gravity.CENTER); // 设置文字居中对齐
                radioButton.setButtonDrawable(null); // 移除默认的单选按钮图标
                // 根据位置设置不同的背景样式
                if (i == 0) { // 第一个按钮
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_left); // 使用左侧背景（左圆角）
                } else if (i == array.length - 1) { // 最后一个按钮
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_right); // 使用右侧背景（右圆角）
                } else { // 中间按钮
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_middle); // 使用中间背景（无圆角）
                }
                radioButton.setText(array[i]); // 设置按钮文本
                if (isSpecialSymbol(array[i])) { // 判断是否为特殊符号
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize + 4); // 特殊符号使用更大字体
                } else { // 普通文本
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize); // 使用标准字体大小
                }

                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 设置文字颜色选择器
                radioButton.setTag(false); // 设置Tag为false，用于标记是否显示提示
                radioGroup.addView(radioButton); // 将RadioButton添加到容器中
                radioButton.setOnClickListener(v -> { // 设置点击监听器（Lambda表达式）
                    int position = radioGroup.indexOfChild(radioButton); // 获取当前按钮在容器中的位置
                    Log.d(TAG, "updateView: " + position); // 输出调试日志
                    radioButton.setSelected(!radioButton.isSelected()); // 切换选中状态
                    if (radioButton.isSelected()) { // 判断是否被选中
                        selectedRadio.add(radioGroup.indexOfChild(radioButton)); // 将选中位置添加到列表
                        radioButton.setChecked(true); // 设置RadioButton为选中状态
                    } else { // 取消选中
                        // 注意：remove有两种类型，remove(int)会按照坐标remove，需要显式装箱为Integer对象
                        selectedRadio.remove(Integer.valueOf(radioGroup.indexOfChild(radioButton))); // 从列表中移除选中位置
                        radioButton.setChecked(false); // 设置RadioButton为未选中状态
                    }
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 更新文字颜色
                });
            }
        }
    }

    // ================================ 工具方法 ================================

    /**
     * 判断是否为特殊符号
     * 特殊符号包括：<、>、<>、=、≠
     * 这些符号会使用更大的字体显示
     *
     * @param charSequence 待判断的字符序列
     * @return true - 是特殊符号；false - 不是特殊符号
     */
    private boolean isSpecialSymbol(CharSequence charSequence) { // 判断特殊符号方法
        return "<".contentEquals(charSequence) || ">".contentEquals(charSequence) // 判断是否为 < 或 >
                || "<>".contentEquals(charSequence) // 判断是否为 <>
                || "=".contentEquals(charSequence) || "≠".contentEquals(charSequence); // 判断是否为 = 或 ≠
    }

    // ================================ 提示功能方法 ================================

    /**
     * 设置指定位置按钮的提示状态
     * 当按钮设置了提示标记后，点击会触发onPrompt回调而非onClick
     *
     * @param index 按钮位置索引（从0开始）
     * @param tag   是否启用提示功能：true - 启用提示；false - 禁用提示
     * @return true - 设置成功；false - 设置失败（索引越界）
     */
    public boolean setRadioButtonOnPromptState(int index, Boolean tag) { // 设置按钮提示状态方法
        if (index < radioGroup.getChildCount()) { // 判断索引是否有效
            radioGroup.getChildAt(index).setTag(tag); // 设置按钮的Tag标记
            return true; // 返回设置成功
        }
        return false; // 索引越界，返回设置失败
    }

    // ================================ 颜色工具方法 ================================

    /**
     * 创建颜色状态列表
     * 根据不同的视图状态返回不同的颜色值
     *
     * @param normal  正常状态颜色
     * @param pressed 按下状态颜色
     * @param focused 获得焦点状态颜色
     * @param unable  禁用状态颜色
     * @return ColorStateList 颜色状态列表对象
     */
    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) { // 创建颜色状态列表方法
        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal}; // 定义各状态对应的颜色数组
        int[][] states = new int[6][]; // 定义状态数组，共6种状态
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}; // 状态0：按下且可用
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}; // 状态1：可用且获得焦点
        states[2] = new int[]{android.R.attr.state_enabled}; // 状态2：可用
        states[3] = new int[]{android.R.attr.state_focused}; // 状态3：获得焦点
        states[4] = new int[]{android.R.attr.state_window_focused}; // 状态4：窗口获得焦点
        states[5] = new int[]{}; // 状态5：默认状态（空状态数组）
        return new ColorStateList(states, colors); // 创建并返回ColorStateList对象
    }

    // ================================ 样式更新方法 ================================

    /**
     * 设置按钮文字颜色
     * 更新按钮文字颜色并重新渲染视图
     *
     * @param textColor 文字颜色值
     */
    public void setItemTextColor(int textColor) { // 设置按钮文字颜色方法
        this.itemTextColor = textColor; // 更新成员变量
        updateView(); // 重新渲染视图
    }

    /**
     * 设置提示文字颜色
     * 更新头部标题的文字颜色
     *
     * @param textColor 提示文字颜色值
     */
    public void setPromptTxtColor(int textColor) { // 设置提示文字颜色方法
        this.promptTxtColor = textColor; // 更新成员变量
        this.headView.setTextColor(textColor); // 设置头部标题文字颜色
    }

    // ================================ 状态查询方法 ================================

    /**
     * 获取指定位置按钮的启用状态
     *
     * @param index 按钮位置索引
     * @return true - 按钮组容器和指定按钮都可用；false - 任一不可用
     * @deprecated 建议使用 {@link #isEnabled(int)} 方法
     */
    public boolean getEnabled(int index) { // 获取启用状态方法（已废弃）
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled(); // 返回容器和按钮的启用状态
    }

    /**
     * 判断指定位置按钮是否可用
     *
     * @param index 按钮位置索引
     * @return true - 按钮组容器和指定按钮都可用；false - 任一不可用
     */
    public boolean isEnabled(int index) { // 判断指定按钮是否可用方法
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled(); // 返回容器和按钮的启用状态
    }

    /**
     * 判断整个按钮组是否可用
     *
     * @return true - 按钮组容器可用；false - 按钮组容器不可用
     */
    public boolean isEnabled() { // 判断按钮组是否可用方法
        return radioGroup.isEnabled(); // 返回容器的启用状态
    }

    // ================================ 触摸事件处理 ================================

    /**
     * 按钮项触摸监听器
     * 处理按钮的触摸事件，支持滑动取消和提示功能
     * 注意：此监听器当前未在代码中使用
     */
    private View.OnTouchListener itemTouchListener = new View.OnTouchListener() { // 创建触摸监听器
        View v = null; // 记录按下时的视图
        boolean moveOut = false; // 标记是否滑出视图范围

        @Override
        public boolean onTouch(View v, MotionEvent event) { // 触摸事件处理方法
            switch (event.getAction() & MotionEvent.ACTION_MASK) { // 获取触摸动作类型
                case MotionEvent.ACTION_DOWN: // 按下事件
                    this.v = v; // 记录按下的视图
                    moveOut = false; // 重置滑出标记
                    break; // 跳出switch
                case MotionEvent.ACTION_MOVE: // 移动事件
                    if (event.getX() < 0 || event.getX() > v.getWidth() // 判断是否滑出视图水平范围
                            || event.getY() < 0 || event.getY() > v.getHeight()) { // 判断是否滑出视图垂直范围
                        moveOut = true; // 设置滑出标记为true
                    }
                    break; // 跳出switch
                case MotionEvent.ACTION_UP: // 抬起事件
                    if (((Boolean) v.getTag())) { // 判断是否设置了提示标记
                        if (onCheckChangedListener != null) { // 判断监听器是否存在
                            onCheckChangedListener.onPrompt(TopVIewMultiSelectRadioButton.this); // 触发提示回调
                        }
                        return true; // 消费事件，不继续处理
                    }
                    if (this.v == null || this.v != v) break; // 判断是否为同一个视图
                    if (!moveOut) { // 判断是否未滑出视图范围
                        itemClickListener.onClick(v); // 触发点击事件
                    } else { // 已滑出视图范围
                        return true; // 消费事件，不触发点击
                    }
                    break; // 跳出switch
            }
            return false; // 不消费事件，允许继续传递
        }
    };

    /**
     * 按钮项点击监听器
     * 处理按钮的点击事件，触发回调通知
     * 注意：此监听器当前未在代码中使用
     */
    private View.OnClickListener itemClickListener = new OnClickListener() { // 创建点击监听器
        @Override
        public void onClick(View v) { // 点击事件处理方法
            for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有按钮
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取当前按钮
                ScreenUtil.getViewLocation(radioButton); // 获取按钮在屏幕上的位置
                if (onCheckChangedListener != null) { // 判断监听器是否存在
                    if (v.getId() == radioButton.getId()) { // 判断是否为被点击的按钮
                        onCheckChangedListener.onClick(TopVIewMultiSelectRadioButton.this, new TopBeanChannel(i, radioButton.getText().toString())); // 触发点击回调
                    }
                }
            }
        }
    };

    // ================================ 启用/禁用控制 ================================

    /**
     * 设置按钮组的启用状态
     * 当禁用时，未选中的按钮会变灰且不可点击，已选中的按钮保持显示但不可点击
     * 当启用时，所有按钮恢复正常状态
     *
     * @param enabled true - 启用；false - 禁用
     */
    @SuppressLint("ResourceType") // 抑制资源ID类型检查警告
    @Override
    public void setEnabled(boolean enabled) { // 重写设置启用状态方法
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有按钮
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取当前按钮
            if (!enabled) { // 判断是否要禁用
                if (!radioButton.isSelected()) { // 判断按钮是否未被选中
                    radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用文字颜色
                    radioButton.setEnabled(false); // 禁用按钮
                } else { // 按钮已被选中
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 保持选中状态的颜色
                    radioButton.setClickable(false); // 设置不可点击，但保持显示
                }
            } else { // 启用按钮
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 恢复正常颜色
                radioButton.setEnabled(true); // 启用按钮
                radioButton.setClickable(true); // 设置可点击
            }
        }
    }
}
