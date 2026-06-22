package com.micsig.tbook.ui.top.view.radioGroup; // 包声明：顶部视图单选按钮组组件所属包路径

import android.annotation.SuppressLint; // 导入SuppressLint注解，用于抑制Android Lint警告
import android.content.Context; // 导入Context上下文类，用于访问应用资源
import android.content.res.ColorStateList; // 导入ColorStateList颜色状态列表类，用于根据状态改变颜色
import android.content.res.TypedArray; // 导入TypedArray类型数组类，用于读取XML属性
import android.util.AttributeSet; // 导入AttributeSet属性集类，用于获取XML属性
import android.util.TypedValue; // 导入TypedValue类型值类，用于单位转换
import android.view.Gravity; // 导入Gravity重力类，用于设置控件内容对齐方式
import android.view.MotionEvent; // 导入MotionEvent触摸事件类，用于处理触摸操作
import android.view.View; // 导入View视图基类
import android.view.ViewGroup; // 导入ViewGroup视图组基类
import android.widget.LinearLayout; // 导入LinearLayout线性布局类，作为父类
import android.widget.RadioButton; // 导入RadioButton单选按钮类
import android.widget.RadioGroup; // 导入RadioGroup单选按钮组类
import android.widget.TextView; // 导入TextView文本视图类

import com.micsig.tbook.ui.MTextView; // 导入MTextView自定义文本视图类
import com.micsig.tbook.ui.R; // 导入R资源类，用于访问应用资源
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入TopBeanChannel通道数据模型类
import com.micsig.tbook.ui.util.ScreenUtil; // 导入ScreenUtil屏幕工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入StrUtil字符串工具类

import java.util.ArrayList; // 导入ArrayList动态数组类
import java.util.List; // 导入List列表接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                   TopViewRadioGroup 类说明文档                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   顶部视图单选按钮组组件 - MHO系列示波器UI层的单选控制组件                    │
 * │   用于在顶部菜单栏中提供单选功能，支持头部标题显示                           │
 * │   位于 com.micsig.tbook.ui.top.view.radioGroup 包下                         │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 单选功能：提供RadioButton组，实现单选互斥功能                           │
 * │   2. 头部标题：支持左侧头部标题显示，增强UI可读性                            │
 * │   3. 自定义样式：支持自定义宽度、高度、背景、文字颜色等样式属性              │
 * │   4. 选择状态管理：管理RadioButton的选择状态，支持设置和获取选中项          │
 * │   5. 事件回调：通过OnCheckChangedListener接口提供点击事件回调               │
 * │   6. 提示功能：支持为特定按钮设置提示状态，触发提示回调                     │
 * │   7. 只读模式：支持只读模式，禁用非选中按钮的交互                           │
 * │   8. 状态控制：支持整体启用/禁用，以及单个按钮的启用/禁用                   │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                       继承关系                                   │       │
 * │   │  LinearLayout (父类 - Android线性布局)                          │       │
 * │   │       ↓                                                          │       │
 * │   │  TopViewRadioGroup (子类 - 单选按钮组视图)                      │       │
 * │   │       ├── headView: MTextView (头部标题视图)                    │       │
 * │   │       ├── radioGroup: RadioGroup (单选按钮组容器)               │       │
 * │   │       ├── array: CharSequence[] (选项文本数组)                  │       │
 * │   │       └── listChecked: List<Boolean> (选择状态列表)            │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                     核心组件结构                                 │       │
 * │   │  ┌──────────────┐  ┌──────────────────────────────────────┐    │       │
 * │   │  │  headView    │  │         radioGroup                   │    │       │
 * │   │  │  (头部标题)  │  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐        │    │       │
 * │   │  │              │  │  │ RB │ │ RB │ │ RB │ │ RB │ ...    │    │       │
 * │   │  └──────────────┘  │  └────┘ └────┘ └────┘ └────┘        │    │       │
 * │   │                    └──────────────────────────────────────┘    │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   XML属性 → initView() → 成员变量 → updateView() → UI渲染                  │
 * │   用户点击 → itemTouchListener → itemClickListener → 回调接口 → 业务处理   │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖：LinearLayout (Android SDK)                                      │
 * │   内部依赖：MTextView, TopBeanChannel, ScreenUtil, StrUtil                 │
 * │   资源依赖：R.layout.view_selectradiogroupwithhead (布局文件)              │
 * │             R.drawable.bg_radiobutton_left/middle/right (背景资源)         │
 * │             R.drawable.selector_text_color (文字颜色选择器)                │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // XML布局中使用                                                          │
 * │   <com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup               │
 * │       android:id="@+id/radioGroup"                                          │
 * │       app:head="通道选择"                                                   │
 * │       app:headWidth="100dp"                                                 │
 * │       app:itemWidth="120dp"                                                 │
 * │       app:itemHeight="60dp"                                                 │
 * │       app:array="@array/channel_array" />                                   │
 * │                                                                             │
 * │   // Java代码中使用                                                         │
 * │   TopViewRadioGroup radioGroup = findViewById(R.id.radioGroup);            │
 * │   radioGroup.setData("通道", new String[]{"CH1", "CH2", "CH3"}, listener); │
 * │   TopBeanChannel selected = radioGroup.getSelected();                      │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 继承自LinearLayout，采用水平布局（HORIZONTAL）                         │
 * │   2. 单选互斥：同一时刻只能有一个RadioButton被选中                          │
 * │   3. 与TopVIewMultiSelectRadioButton的区别：本类为单选，后者为多选          │
 * │   4. 特殊符号处理：对"<", ">", "<>", "=", "≠"等符号使用更大的字体          │
 * │   5. 触摸事件处理：支持滑动取消点击（moveOut机制）                          │
 * │   6. 非线程安全类，UI操作需在主线程执行                                     │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by Administrator on 2017/4/5                                      │
 * │   Last Modified: 2024                                                       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 顶部视图单选按钮组组件
 * <p>
 * 继承自LinearLayout，提供单选按钮组功能，支持头部标题显示。
 * 主要用于示波器顶部菜单栏的单选控制场景，如通道选择、触发源选择等。
 * <p>
 * 核心特性：
 * - 单选互斥：同一时刻只能有一个RadioButton被选中
 * - 头部标题：支持左侧显示标题文本，增强UI可读性
 * - 自定义样式：支持通过XML属性或Java代码设置样式
 * - 事件回调：通过OnCheckChangedListener接口回调选择事件
 * - 提示功能：支持为特定按钮设置提示状态
 * - 只读模式：支持只读模式，禁用非选中按钮的交互
 *
 * @author Administrator
 * @version 1.0
 * @since 2017/4/5
 * @see LinearLayout
 * @see RadioGroup
 * @see TopVIewMultiSelectRadioButton
 */
public class TopViewRadioGroup extends LinearLayout { // 继承LinearLayout，实现单选按钮组视图

    /**
     * 日志标签
     * 用途：用于Log日志输出，标识当前类
     * 取值："TopViewRadioGroup"
     */
    private final String TAG = "TopViewRadioGroup"; // 日志标签，用于调试输出

    /**
     * 上下文对象
     * 用途：用于访问应用资源、创建视图等操作
     * 生命周期：与视图生命周期一致
     */
    private Context context; // Android上下文对象，用于资源访问和视图创建

    /**
     * 头部标题文本
     * 用途：显示在单选按钮组左侧的标题文本
     * 示例："通道选择"、"触发源"等
     * 可为null，为null时头部视图隐藏
     */
    private String head; // 头部标题文本内容

    /**
     * 选项文本数组
     * 用途：存储所有RadioButton的显示文本
     * 来源：XML属性或Java代码设置
     * 示例：["CH1", "CH2", "CH3"]、["上升沿", "下降沿"]
     */
    private CharSequence[] array; // 选项文本数组，用于创建RadioButton

    /**
     * 单选按钮组容器
     * 用途：包含和管理所有RadioButton，实现单选互斥功能
     * 初始化：在updateView()方法中获取引用
     */
    private RadioGroup radioGroup; // RadioGroup容器，用于管理RadioButton

    /**
     * 选择状态变化监听器
     * 用途：回调选择事件给外部调用者
     * 可为null，为null时不触发回调
     */
    private OnCheckChangedListener onCheckChangedListener; // 选择状态变化监听器接口

    /**
     * 头部标题视图
     * 用途：显示头部标题文本
     * 初始化：在initView()方法中获取引用
     */
    private MTextView headView; // 头部标题视图，用于显示head文本

    /**
     * 头部视图宽度
     * 用途：设置头部标题视图的宽度（单位：像素）
     * 默认值：100px
     * 来源：XML属性headWidth或Java代码设置
     */
    private int headWidth; // 头部视图宽度，单位为像素

    /**
     * 单选项宽度
     * 用途：设置每个RadioButton的宽度（单位：像素）
     * 默认值：120px
     * 来源：XML属性itemWidth或Java代码设置
     */
    private int itemWidth; // 单选项宽度，单位为像素

    /**
     * 单选项高度
     * 用途：设置每个RadioButton的高度（单位：像素）
     * 默认值：60px
     * 来源：XML属性itemHeight或Java代码设置
     */
    private int itemHeight; // 单选项高度，单位为像素

    /**
     * RadioGroup背景资源ID
     * 用途：设置RadioGroup容器的背景drawable资源
     * 默认值：R.drawable.bg_radiogroup_selectwithhead
     * 来源：XML属性bgRadioGroup
     */
    private int bgRadioGroup; // RadioGroup背景资源ID

    /**
     * RadioButton背景资源ID
     * 用途：设置RadioButton的背景drawable资源
     * 默认值：R.drawable.bg_radiobutton_middle
     * 实际使用：根据位置动态设置left/middle/right背景
     * 来源：XML属性bgRadioButton
     */
    private int bgRadioButton; // RadioButton背景资源ID

    /**
     * 单选项文字颜色
     * 用途：设置RadioButton的文字颜色
     * 默认值：R.color.textColorNewTopViewEnable
     * 来源：XML属性itemTextColor或Java代码设置
     */
    private int itemTextColor; // 单选项文字颜色值

    /**
     * 提示文本颜色
     * 用途：设置头部标题的提示文本颜色
     * 来源：Java代码设置setPromptTxtColor()
     */
    private int promptTxtColor; // 提示文本颜色值

    /**
     * 单选项文字大小
     * 用途：设置RadioButton的文字大小（单位：像素）
     * 默认值：20px
     * 来源：XML属性android:textSize或Java代码设置
     */
    private int itemTextSize; // 单选项文字大小，单位为像素

    /**
     * 选择状态列表
     * 用途：记录每个RadioButton的选择状态历史
     * 说明：用于判断选择是否成功（状态是否改变）
     * 大小：与array长度一致
     */
    private List<Boolean> listChecked = new ArrayList<Boolean>(); // 选择状态列表，记录每个RadioButton的选中状态

    /**
     * 选择状态变化监听器接口
     * <p>
     * 功能：定义选择状态变化的回调方法
     * 用途：外部实现此接口，接收选择事件回调
     * 方法：
     * - onClick: 选择事件回调
     * - onClickSound: 播放声音回调
     * - onPrompt: 提示事件回调
     */
    public interface OnCheckChangedListener { // 选择状态变化监听器接口定义

        /**
         * 选择事件回调
         * <p>
         * 功能：当用户点击RadioButton时触发
         * 说明：无论选择是否成功都会触发此回调
         *
         * @param view 当前TopViewRadioGroup实例
         * @param item 选中的项数据，包含索引和文本
         */
        void onClick(TopViewRadioGroup view, TopBeanChannel item); // 选择事件回调方法

        /**
         * 播放声音回调
         * <p>
         * 功能：通知外部播放选择声音
         * 说明：根据选择是否成功播放不同的声音
         *
         * @param isCheckedSuccess 是否选择成功
         *                          true: 选择成功（状态改变）
         *                          false: 选择失败（状态未改变，如重复选择同一项）
         */
        void onClickSound(boolean isCheckedSuccess); // 播放声音回调方法

        /**
         * 提示事件回调
         * <p>
         * 功能：当点击带有提示标记的RadioButton时触发
         * 说明：用于显示提示信息，如帮助文本、警告等
         *
         * @param view 当前TopViewRadioGroup实例
         */
        void onPrompt(TopViewRadioGroup view); // 提示事件回调方法
    }

    /**
     * 构造函数（单参数）
     * <p>
     * 功能：创建TopViewRadioGroup实例，用于Java代码动态创建
     * 说明：调用三参数构造函数，attrs和defStyleAttr使用默认值
     *
     * @param context 上下文对象，用于访问应用资源
     */
    public TopViewRadioGroup(Context context) { // 单参数构造函数
        this(context, null); // 调用双参数构造函数，attrs传null
    } // 构造函数结束

    /**
     * 构造函数（双参数）
     * <p>
     * 功能：创建TopViewRadioGroup实例，用于XML布局填充
     * 说明：调用三参数构造函数，defStyleAttr使用默认值
     *
     * @param context 上下文对象，用于访问应用资源
     * @param attrs XML属性集，包含布局文件中定义的属性
     */
    public TopViewRadioGroup(Context context, AttributeSet attrs) { // 双参数构造函数
        this(context, attrs, 0); // 调用三参数构造函数，defStyleAttr传0
    } // 构造函数结束

    /**
     * 构造函数（三参数）
     * <p>
     * 功能：创建TopViewRadioGroup实例，完整初始化流程
     * 说明：这是最终被调用的构造函数，执行初始化逻辑
     *
     * @param context 上下文对象，用于访问应用资源
     * @param attrs XML属性集，包含布局文件中定义的属性
     * @param defStyleAttr 默认样式属性，用于应用默认样式
     */
    public TopViewRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造函数
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout的构造函数
        this.context = context; // 保存上下文对象到成员变量
        initView(context, attrs, defStyleAttr); // 调用初始化视图方法
    } // 构造函数结束

    /**
     * 初始化视图
     * <p>
     * 功能：执行视图的初始化操作，包括加载布局、读取属性、初始化控件
     * 流程：
     * 1. 加载布局文件
     * 2. 设置布局方向为水平
     * 3. 读取XML属性
     * 4. 初始化头部视图
     * 5. 如果有数据则更新视图
     *
     * @param context 上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 初始化视图方法
        View.inflate(context, R.layout.view_selectradiogroupwithhead, this); // 加载布局文件到当前视图
        setOrientation(HORIZONTAL); // 设置布局方向为水平排列
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewRadioGroup); // 获取自定义属性数组
        head = ta.getString(R.styleable.TopViewRadioGroup_head); // 读取头部标题属性
        array = ta.getTextArray(R.styleable.TopViewRadioGroup_array); // 读取选项文本数组属性
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_headWidth, 100); // 读取头部宽度属性，默认100px
        itemWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemWidth, 120); // 读取单选项宽度属性，默认120px
        itemHeight = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemHeight, 60); // 读取单选项高度属性，默认60px
        bgRadioGroup = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioGroup, R.drawable.bg_radiogroup_selectwithhead); // 读取RadioGroup背景资源ID，使用默认值
        bgRadioButton = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioButton, R.drawable.bg_radiobutton_middle); // 读取RadioButton背景资源ID，使用默认值
        itemTextColor = ta.getColor(R.styleable.TopViewRadioGroup_itemTextColor, getResources().getColor(R.color.textColorNewTopViewEnable)); // 读取文字颜色属性，使用默认值
        itemTextSize = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_android_textSize, 20); // 读取文字大小属性，默认20px
        ta.recycle(); // 回收TypedArray，释放资源

        headView = findViewById(R.id.head); // 获取头部视图引用
        headView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize); // 设置头部视图文字大小

        if (array != null && array.length > 0) { // 判断是否有选项数据
            updateView(); // 有数据则更新视图
        } // if结束
    } // initView方法结束

    /**
     * 设置选择状态变化监听器
     * <p>
     * 功能：设置外部监听器，用于接收选择事件回调
     * 说明：可在任何时候调用，会覆盖之前的监听器
     *
     * @param onCheckChangedListener 监听器实例，可为null
     */
    public void setOnListener(OnCheckChangedListener onCheckChangedListener) { // 设置监听器方法
        this.onCheckChangedListener = onCheckChangedListener; // 保存监听器到成员变量
    } // setOnListener方法结束

    /**
     * 设置单选项宽度
     * <p>
     * 功能：动态设置每个RadioButton的宽度
     * 说明：需要在setData()之前调用才能生效
     *
     * @param itemWidth 宽度值，单位为像素
     */
    public void setItemWidth(int itemWidth) { // 设置单选项宽度方法
        this.itemWidth = itemWidth; // 保存宽度值到成员变量
    } // setItemWidth方法结束

    /**
     * 设置头部视图宽度
     * <p>
     * 功能：动态设置头部标题视图的宽度
     * 说明：需要在setData()之前调用才能生效
     *
     * @param headWidth 宽度值，单位为像素
     */
    public void setHeadWidth(int headWidth) { // 设置头部宽度方法
        this.headWidth = headWidth; // 保存宽度值到成员变量
    } // setHeadWidth方法结束

    /**
     * 清除选择状态
     * <p>
     * 功能：取消所有RadioButton的选中状态
     * 说明：调用RadioGroup的clearCheck()方法
     */
    public void clearCheck() { // 清除选择方法
        radioGroup.clearCheck(); // 调用RadioGroup的清除选择方法
    } // clearCheck方法结束

    /**
     * 设置数据（字符串数组）
     * <p>
     * 功能：设置头部标题、选项数组和监听器，并更新视图
     * 说明：这是最常用的数据设置方法
     *
     * @param head 头部标题文本，可为null
     * @param array 选项文本数组，不能为null
     * @param listener 选择状态变化监听器，可为null
     */
    public void setData(String head, String[] array, OnCheckChangedListener listener) { // 设置数据方法（字符串数组）
        this.head = head; // 保存头部标题
        this.array = array; // 保存选项数组
        this.onCheckChangedListener = listener; // 保存监听器
        updateView(); // 更新视图
    } // setData方法结束

    /**
     * 设置数据（资源ID）
     * <p>
     * 功能：通过资源ID设置头部标题和选项数组
     * 说明：头部标题和选项数组都从资源文件读取
     *
     * @param headResId 头部标题字符串资源ID
     * @param arrayResId 选项文本数组资源ID
     * @param listener 选择状态变化监听器，可为null
     */
    public void setData(int headResId, int arrayResId, OnCheckChangedListener listener) { // 设置数据方法（资源ID）
        this.head = context.getString(headResId); // 从资源ID获取头部标题字符串
        this.array = context.getResources().getStringArray(arrayResId); // 从资源ID获取选项数组
        this.onCheckChangedListener = listener; // 保存监听器
        updateView(); // 更新视图
    } // setData方法结束

    /**
     * 设置数据（混合参数）
     * <p>
     * 功能：设置头部标题（字符串）和选项数组（资源ID）
     * 说明：头部标题直接传入字符串，选项数组从资源文件读取
     *
     * @param head 头部标题文本，可为null
     * @param arrayResId 选项文本数组资源ID
     * @param listener 选择状态变化监听器，可为null
     */
    public void setData(String head, int arrayResId, OnCheckChangedListener listener) { // 设置数据方法（混合参数）
        this.head = head; // 保存头部标题
        this.array = context.getResources().getStringArray(arrayResId); // 从资源ID获取选项数组
        this.onCheckChangedListener = listener; // 保存监听器
        updateView(); // 更新视图
    } // setData方法结束

    /**
     * 更新视图
     * <p>
     * 功能：根据当前数据更新UI显示
     * 流程：
     * 1. 更新头部视图（设置宽度、文本、可见性）
     * 2. 清空RadioGroup中的所有RadioButton
     * 3. 根据array创建RadioButton并添加到RadioGroup
     * 4. 设置RadioButton的样式、文本、监听器
     * 5. 默认选中第一个RadioButton
     * <p>
     * 说明：使用@SuppressLint("ResourceType")抑制资源ID检查警告
     */
    @SuppressLint("ResourceType") // 抑制Android Lint警告：资源ID检查
    private void updateView() { // 更新视图方法
        MTextView headView = (MTextView) findViewById(R.id.head); // 获取头部视图
        ViewGroup.LayoutParams headParams = headView.getLayoutParams(); // 获取头部视图的布局参数
        headParams.width = headWidth; // 设置头部视图宽度
        headView.setLayoutParams(headParams); // 应用布局参数
        if (!StrUtil.isEmpty(head)) { // 判断头部标题是否为空
            headView.setText(head); // 不为空则设置文本
        } else { // 头部标题为空
            headView.setVisibility(View.GONE); // 隐藏头部视图
        } // if-else结束

        radioGroup = (RadioGroup) findViewById(R.id.topViewRadioGroup); // 获取RadioGroup容器
//        radioGroup.setBackgroundResource(bgRadioGroup); // 注释代码：设置RadioGroup背景（已禁用）
        radioGroup.setBaselineAligned(false); // 设置不对齐基线，避免布局问题
        radioGroup.removeAllViews(); // 移除所有子视图，准备重新创建
        if (array != null && array.length >= 2) { // 判断数组是否有效（至少2个选项）
            for (int i = 0; i < array.length; i++) { // 遍历数组，创建RadioButton
                RadioButton radioButton = new RadioButton(context); // 创建RadioButton实例
                LayoutParams layoutParams = new LayoutParams(itemWidth, itemHeight); // 创建布局参数，设置宽高
                radioButton.setLayoutParams(layoutParams); // 应用布局参数
                radioButton.setGravity(Gravity.CENTER); // 设置文字居中对齐
                radioButton.setButtonDrawable(null); // 隐藏RadioButton默认的圆形按钮
//                radioButton.setBackgroundResource(bgRadioButton); // 注释代码：设置RadioButton背景（已禁用）
                if (i == 0) { // 判断是否为第一个RadioButton
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_left); // 设置左侧背景样式
                } else if (i == array.length - 1) { // 判断是否为最后一个RadioButton
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_right); // 设置右侧背景样式
                } else { // 中间的RadioButton
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_middle); // 设置中间背景样式
                } // if-else结束
                radioButton.setText(array[i]); // 设置RadioButton显示文本
                if (isSpecialSymbol(array[i])) { // 判断是否为特殊符号
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize + 4); // 特殊符号使用更大的字体
                } else { // 普通文本
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize); // 设置正常字体大小
                } // if-else结束
//                radioButton.setTextColor(itemTextColor); // 注释代码：设置文字颜色（已禁用）
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 设置文字颜色选择器，根据状态改变颜色
                //radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,itemTextSize); // 注释代码：设置文字大小（重复代码）
                radioButton.setTag(false); // 设置Tag为false，表示无提示状态
                radioButton.setOnTouchListener(itemTouchListener); // 设置触摸监听器

                radioGroup.addView(radioButton); // 将RadioButton添加到RadioGroup
                if (i == 0) { // 判断是否为第一个RadioButton
                    radioGroup.check(radioButton.getId()); // 默认选中第一个RadioButton
                } // if结束
                listChecked.add(radioButton.isChecked()); // 将初始选中状态添加到列表
            } // for循环结束
        } // if结束
    } // updateView方法结束

    /**
     * 判断是否为特殊符号
     * <p>
     * 功能：判断文本是否为需要特殊处理的符号
     * 说明：特殊符号使用更大的字体显示，提升可读性
     * 特殊符号列表：<, >, <>, =, ≠
     *
     * @param charSequence 待判断的文本
     * @return true: 是特殊符号; false: 不是特殊符号
     */
    private boolean isSpecialSymbol(CharSequence charSequence) { // 判断特殊符号方法
        return "<".contentEquals(charSequence) || ">".contentEquals(charSequence) // 判断是否为<或>
                || "<>".contentEquals(charSequence) // 判断是否为<>
                || "=".contentEquals(charSequence) || "≠".contentEquals(charSequence); // 判断是否为=或≠
    } // isSpecialSymbol方法结束

    /**
     * 设置RadioButton的提示状态
     * <p>
     * 功能：为指定位置的RadioButton设置提示标记
     * 说明：当点击带有提示标记的RadioButton时，会触发onPrompt回调
     * 用途：用于显示帮助信息、警告提示等
     *
     * @param index RadioButton的索引位置，从0开始
     * @param tag true: 有提示; false: 无提示
     * @return true: 设置成功; false: 设置失败（索引越界）
     */
    public boolean setRadioButtonOnPromptState(int index, Boolean tag) { // 设置提示状态方法
        if (index < radioGroup.getChildCount()) { // 判断索引是否有效
            radioGroup.getChildAt(index).setTag(tag); // 设置RadioButton的Tag
            return true; // 返回成功
        } // if结束
        return false; // 索引越界，返回失败
    } // setRadioButtonOnPromptState方法结束

    /**
     * 创建颜色状态列表
     * <p>
     * 功能：根据不同状态创建颜色状态列表
     * 说明：用于实现根据控件状态改变文字颜色的效果
     * 状态顺序：pressed, focused, enabled, focused, window_focused, normal
     *
     * @param normal 正常状态颜色
     * @param pressed 按下状态颜色
     * @param focused 获得焦点状态颜色
     * @param unable 不可用状态颜色
     * @return ColorStateList颜色状态列表
     */
    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) { // 创建颜色状态列表方法
        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal}; // 定义颜色数组，对应不同状态
        int[][] states = new int[6][]; // 定义状态数组，共6种状态
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}; // 状态0：按下且可用
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}; // 状态1：可用且获得焦点
        states[2] = new int[]{android.R.attr.state_enabled}; // 状态2：可用
        states[3] = new int[]{android.R.attr.state_focused}; // 状态3：获得焦点
        states[4] = new int[]{android.R.attr.state_window_focused}; // 状态4：窗口获得焦点
        states[5] = new int[]{}; // 状态5：默认状态
        return new ColorStateList(states, colors); // 创建并返回ColorStateList
    } // createColorStateList方法结束

    /**
     * 设置整体启用状态
     * <p>
     * 功能：设置整个RadioGroup及其所有RadioButton的启用状态
     * 说明：禁用时所有RadioButton变为灰色且不可点击
     * 用途：用于控制整个组件的可用性
     * <p>
     * 说明：使用@SuppressLint("ResourceType")抑制资源ID检查警告
     *
     * @param enabled true: 启用; false: 禁用
     */
    @SuppressLint("ResourceType") // 抑制Android Lint警告：资源ID检查
    @Override // 重写父类方法
    public void setEnabled(boolean enabled) { // 设置启用状态方法
        radioGroup.setEnabled(enabled); // 设置RadioGroup的启用状态
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton
            radioButton.setEnabled(enabled); // 设置RadioButton的启用状态
            if (!enabled) { // 判断是否禁用
                if (radioGroup.getCheckedRadioButtonId() == radioButton.getId()) { // 判断是否为选中的RadioButton
//                    radioButton.setTextColor(getResources().getColor(R.color.bgNewTopAllLayout)); // 注释代码：设置文字颜色（已禁用）
                    radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用状态文字颜色
                } else { // 未选中的RadioButton
                    radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用状态文字颜色
                } // if-else结束
            } else { // 启用状态
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 设置启用状态文字颜色选择器
            } // if-else结束
        } // for循环结束
    } // setEnabled方法结束

    /**
     * 设置指定RadioButton的启用状态
     * <p>
     * 功能：设置指定索引位置的RadioButton的启用状态
     * 说明：只影响指定位置的RadioButton，其他RadioButton不受影响
     * 用途：用于单独控制某个RadioButton的可用性
     * <p>
     * 说明：使用@SuppressLint("ResourceType")抑制资源ID检查警告
     *
     * @param index RadioButton的索引位置，从0开始
     * @param enabled true: 启用; false: 禁用
     * @return true: 状态发生了改变; false: 状态未改变
     */
    @SuppressLint("ResourceType") // 抑制Android Lint警告：资源ID检查
    public boolean setEnabled(int index, boolean enabled) { // 设置指定RadioButton启用状态方法
        boolean change = false; // 初始化状态改变标志为false
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            if (i == index) { // 判断是否为指定索引
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton
                change = radioButton.isEnabled() != enabled; // 判断状态是否改变
                radioButton.setEnabled(enabled); // 设置RadioButton的启用状态
                if (!enabled) { // 判断是否禁用
                    if (radioGroup.getCheckedRadioButtonId() == radioButton.getId()) { // 判断是否为选中的RadioButton
//                        radioButton.setTextColor(getResources().getColor(R.color.bgNewTopAllLayout)); // 注释代码：设置文字颜色（已禁用）
                        radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用状态文字颜色
                    } else { // 未选中的RadioButton
                        radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用状态文字颜色
                    } // if-else结束
                } else { // 启用状态
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 设置启用状态文字颜色选择器
                } // if-else结束
            } // if结束
        } // for循环结束
        return change; // 返回状态是否改变
    } // setEnabled方法结束


    /**
     * 设置只读模式
     * <p>
     * 功能：设置组件为只读模式，只允许查看当前选中项，不允许更改选择
     * 说明：
     * - 只读模式（enabled=false）：未选中的RadioButton禁用，选中的RadioButton不可点击但保持正常颜色
     * - 非只读模式（enabled=true）：所有RadioButton恢复正常
     * 用途：用于显示当前选择状态但不允许用户修改
     * <p>
     * 说明：使用@SuppressLint("ResourceType")抑制资源ID检查警告
     *
     * @param enabled true: 非只读模式（正常模式）; false: 只读模式
     */
    @SuppressLint("ResourceType") // 抑制Android Lint警告：资源ID检查
    public void setReadOnly(boolean enabled) { // 设置只读模式方法
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton
            if (!enabled) { // 判断是否为只读模式
                if (!radioButton.isChecked()) { // 判断是否未选中
                    radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用状态文字颜色
                    radioButton.setEnabled(false); // 禁用RadioButton
                } else { // 已选中的RadioButton
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 保持正常文字颜色
                    radioButton.setClickable(false); // 设置不可点击，但保持启用状态
                } // if-else结束
            } else { // 非只读模式（正常模式）
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color)); // 设置正常文字颜色选择器
                radioButton.setEnabled(true); // 启用RadioButton
                radioButton.setClickable(true); // 设置可点击
            } // if-else结束
        } // for循环结束
    } // setReadOnly方法结束

    /**
     * 设置单选项文字颜色
     * <p>
     * 功能：动态设置RadioButton的文字颜色，并刷新视图
     * 说明：会触发updateView()重新创建所有RadioButton
     *
     * @param textColor 文字颜色值
     */
    public void setItemTextColor(int textColor) { // 设置单选项文字颜色方法
        this.itemTextColor = textColor; // 保存颜色值到成员变量
        updateView(); // 更新视图
    } // setItemTextColor方法结束

    /**
     * 设置提示文本颜色
     * <p>
     * 功能：设置头部标题的提示文本颜色
     * 说明：用于在提示状态下改变头部标题的颜色
     *
     * @param textColor 文字颜色值
     */
    public void setPromptTxtColor(int textColor) { // 设置提示文本颜色方法
        this.promptTxtColor = textColor; // 保存颜色值到成员变量
        this.headView.setTextColor(textColor); // 设置头部视图的文字颜色
    } // setPromptTxtColor方法结束

    /**
     * 获取指定RadioButton的启用状态
     * <p>
     * 功能：判断指定索引位置的RadioButton是否可用
     * 说明：需要同时满足RadioGroup和RadioButton都启用
     *
     * @param index RadioButton的索引位置，从0开始
     * @return true: 可用; false: 不可用
     */
    public boolean getEnabled(int index) { // 获取指定RadioButton启用状态方法
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled(); // 返回RadioGroup和RadioButton的启用状态
    } // getEnabled方法结束

    /**
     * 判断指定RadioButton是否启用
     * <p>
     * 功能：判断指定索引位置的RadioButton是否可用
     * 说明：与getEnabled()方法功能相同，提供不同的方法名
     *
     * @param index RadioButton的索引位置，从0开始
     * @return true: 可用; false: 不可用
     */
    public boolean isEnabled(int index) { // 判断指定RadioButton是否启用方法
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled(); // 返回RadioGroup和RadioButton的启用状态
    } // isEnabled方法结束

    /**
     * 判断整体是否启用
     * <p>
     * 功能：判断整个RadioGroup是否可用
     * 说明：只判断RadioGroup的启用状态，不判断单个RadioButton
     *
     * @return true: 可用; false: 不可用
     */
    public boolean isEnabled() { // 判断整体是否启用方法
        return radioGroup.isEnabled(); // 返回RadioGroup的启用状态
    } // isEnabled方法结束


    /**
     * 获取选项文本数组
     * <p>
     * 功能：返回所有RadioButton的显示文本数组
     * 说明：返回的是原始数组引用，修改会影响内部数据
     *
     * @return 选项文本数组
     */
    public CharSequence[] getArray() { // 获取选项文本数组方法
        return array; // 返回选项文本数组
    } // getArray方法结束

    /**
     * 获取选中项的文本
     * <p>
     * 功能：返回当前选中的RadioButton的文本内容
     * 说明：遍历所有RadioButton，找到选中项并返回其文本
     *
     * @return 选中项的文本，如果没有选中项则返回null
     */
    public String getSelectedString() { // 获取选中项文本方法
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) { // 判断是否为选中的RadioButton
                return ((RadioButton) radioGroup.getChildAt(i)).getText().toString(); // 返回选中项的文本
            } // if结束
        } // for循环结束
        return null; // 没有选中项，返回null
    } // getSelectedString方法结束

    /**
     * 获取选中项数据
     * <p>
     * 功能：返回当前选中的RadioButton的数据封装对象
     * 说明：返回TopBeanChannel对象，包含索引和文本信息
     * 用途：用于获取选中项的完整信息
     *
     * @return TopBeanChannel对象，包含索引和文本；如果没有选中项则返回null
     */
    public TopBeanChannel getSelected() { // 获取选中项数据方法
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) { // 判断是否为选中的RadioButton
                return new TopBeanChannel(i, ((RadioButton) radioGroup.getChildAt(i)).getText().toString()); // 创建并返回TopBeanChannel对象
            } // if结束
        } // for循环结束
        return null; // 没有选中项，返回null
    } // getSelected方法结束

    /**
     * 获取RadioGroup容器
     * <p>
     * 功能：返回内部的RadioGroup容器对象
     * 说明：用于外部需要直接操作RadioGroup的场景
     * 用途：高级用法，允许外部直接访问RadioGroup
     *
     * @return RadioGroup容器对象
     */
    public RadioGroup getRadioGroup() { // 获取RadioGroup容器方法
        return radioGroup; // 返回RadioGroup容器
    } // getRadioGroup方法结束

    /**
     * 设置选中项索引
     * <p>
     * 功能：通过索引设置选中的RadioButton
     * 说明：只有启用的RadioButton才能被选中
     * 流程：
     * 1. 遍历所有RadioButton，找到指定索引且启用的RadioButton
     * 2. 调用RadioGroup.check()设置选中
     * 3. 更新listChecked列表中的选择状态
     *
     * @param index 要选中的RadioButton索引，从0开始
     */
    public void setSelectedIndex(int index) { // 设置选中项索引方法
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            if (i == index && radioGroup.getChildAt(i).isEnabled()) { // 判断是否为指定索引且启用
                radioGroup.check(radioGroup.getChildAt(i).getId()); // 设置选中
            } // if结束
        } // for循环结束
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton
            listChecked.set(i, radioButton.isChecked()); // 更新选择状态列表
        } // for循环结束
    } // setSelectedIndex方法结束

    /**
     * 获取头部标题
     * <p>
     * 功能：返回头部标题文本
     * 说明：返回的是原始字符串引用
     *
     * @return 头部标题文本，可能为null
     */
    public String getHead() { // 获取头部标题方法
        return head; // 返回头部标题
    } // getHead方法结束

    /**
     * RadioButton触摸监听器
     * <p>
     * 功能：处理RadioButton的触摸事件
     * 说明：
     * - 支持滑动取消点击（moveOut机制）
     * - 支持提示状态触发
     * - 处理DOWN、MOVE、UP事件
     * <p>
     * 逻辑流程：
     * 1. DOWN: 记录触摸的View，重置moveOut标志
     * 2. MOVE: 判断是否滑出控件范围
     * 3. UP: 判断是否有提示标记，有则触发onPrompt；否则判断是否滑出，未滑出则触发onClick
     */
    private View.OnTouchListener itemTouchListener = new View.OnTouchListener() { // 创建触摸监听器
        View v = null; // 记录触摸的View
        boolean moveOut = false; // 滑出标志，true表示手指滑出了控件范围

        @Override // 重写接口方法
        public boolean onTouch(View v, MotionEvent event) { // 触摸事件处理方法
            switch (event.getAction() & MotionEvent.ACTION_MASK) { // 获取触摸事件类型
                case MotionEvent.ACTION_DOWN: // 手指按下事件
                    this.v = v; // 记录触摸的View
                    moveOut = false; // 重置滑出标志为false
                    break; // 跳出switch
                case MotionEvent.ACTION_MOVE: // 手指移动事件
                    if (event.getX() < 0 || event.getX() > v.getWidth() // 判断X坐标是否超出控件范围
                            || event.getY() < 0 || event.getY() > v.getHeight()) { // 判断Y坐标是否超出控件范围
                        moveOut = true; // 设置滑出标志为true
                    } // if结束
                    break; // 跳出switch
                case MotionEvent.ACTION_UP: // 手指抬起事件
                    if (((Boolean) v.getTag())) { // 判断是否有提示标记
                        if (onCheckChangedListener != null) { // 判断监听器是否为空
                            onCheckChangedListener.onPrompt(TopViewRadioGroup.this); // 触发提示回调
                        } // if结束
                        return true; // 返回true，消费事件
                    } // if结束
                    if (this.v == null || this.v != v) break; // 判断触摸的View是否有效，无效则跳出
                    if (!moveOut) { // 判断是否未滑出控件范围
                        itemClickListener.onClick(v); // 未滑出则触发点击事件
                    } else { // 已滑出控件范围
                        return true; // 返回true，消费事件但不触发点击
                    } // if-else结束
                    break; // 跳出switch
            } // switch结束
            return false; // 返回false，不消费事件
        } // onTouch方法结束
    }; // itemTouchListener定义结束

    /**
     * RadioButton点击监听器
     * <p>
     * 功能：处理RadioButton的点击事件
     * 说明：
     * - 遍历所有RadioButton，找到被点击的RadioButton
     * - 设置选中状态
     * - 触发onClickSound和onClick回调
     * - 更新listChecked列表
     * <p>
     * 逻辑流程：
     * 1. 遍历所有RadioButton
     * 2. 找到被点击的RadioButton
     * 3. 设置选中状态
     * 4. 判断选择是否成功（状态是否改变）
     * 5. 触发回调
     * 6. 更新选择状态列表
     */
    private View.OnClickListener itemClickListener = new OnClickListener() { // 创建点击监听器
        @Override // 重写接口方法
        public void onClick(View v) { // 点击事件处理方法
            for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton
                ScreenUtil.getViewLocation(radioButton); // 获取RadioButton的屏幕位置（可能用于其他用途）
                if (onCheckChangedListener != null) { // 判断监听器是否为空
                    if (v.getId() == radioButton.getId()) { // 判断是否为被点击的RadioButton
                        radioGroup.check(radioButton.getChildAt(i).getId()); // 设置选中状态
                        onCheckChangedListener.onClickSound(listChecked.get(i) != radioButton.isChecked()); // 触发声音回调，判断选择是否成功
                        onCheckChangedListener.onClick(TopViewRadioGroup.this, new TopBeanChannel(i, radioButton.getText().toString())); // 触发选择回调
                    } // if结束
                } // if结束
                listChecked.set(i, radioButton.isChecked()); // 更新选择状态列表
            } // for循环结束
        } // onClick方法结束
    }; // itemClickListener定义结束
} // TopViewRadioGroup类结束
