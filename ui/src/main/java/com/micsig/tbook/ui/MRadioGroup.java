package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.content.res.TypedArray;  // 类型化数组，用于读取自定义属性
import android.util.AttributeSet;  // XML属性集类
import android.view.View;  // Android视图基类
import android.widget.TextView;  // 文本视图控件

import androidx.annotation.NonNull;  // 非空注解
import androidx.annotation.Nullable;  // 可空注解
import androidx.constraintlayout.widget.ConstraintLayout;  // 约束布局
import androidx.recyclerview.widget.LinearLayoutManager;  // 线性布局管理器
import androidx.recyclerview.widget.RecyclerView;  // RecyclerView列表控件
import androidx.recyclerview.widget.StaggeredGridLayoutManager;  // 瀑布流布局管理器

import com.micsig.tbook.ui.bean.RadioButtonBean;  // 单选按钮数据Bean类

import java.util.ArrayList;  // 动态数组列表
import java.util.List;  // List接口
import java.util.function.Consumer;  // 消费者函数式接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              MRadioGroup                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 自定义单选按钮组控件                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承ConstraintLayout，提供可配置的单选按钮组容器                            │
 * │   2. 支持水平/垂直两种排列方式                                                  │
 * │   3. 支持多种显示样式：独立按钮、两端圆角、连续圆角等                              │
 * │   4. 支持自定义颜色、尺寸、边距等属性                                           │
 * │   5. 通过RecyclerView实现高效列表渲染                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │ ConstraintLayout│  ← 继承自约束布局                                       │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐      ┌───────────────────┐                            │
 * │   │   MRadioGroup   │ ───→ │ MRadioGroupAdapter│  RecyclerView适配器         │
 * │   └────────┬────────┘      └───────────────────┘                            │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐      ┌───────────────────┐                            │
 * │   │    RecyclerView │ ───→ │   RadioButtonBean │  数据模型                   │
 * │   └─────────────────┘      └───────────────────┘                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   XML属性 → TypedArray解析 → 初始化列表 → Adapter绑定 → 用户点击 → 回调通知     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - ConstraintLayout: 基类，提供约束布局功能                                   │
 * │   - RecyclerView: 列表容器                                                   │
 * │   - MRadioGroupAdapter: 列表适配器                                           │
 * │   - RadioButtonBean: 单选按钮数据模型                                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MRadioGroup                                          │
 * │       android:id="@+id/radio_group"                                         │
 * │       app:arrays="@array/options"                                           │
 * │       app:itemWidth="120dp"                                                 │
 * │       app:itemHeight="60dp" />                                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author Liwb                                                                 │
 * │ @date 2024-2-27 11:11                                                       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MRadioGroup extends ConstraintLayout {
    
    // =========================== 成员变量定义 ===========================
    
    /** Android上下文环境 */  // 用于资源访问和视图创建
    private Context context;
    
    /** 单选按钮显示文本数组 */  // 从XML属性读取的文本选项
    private CharSequence[] arrays;
    
    /** 单选按钮颜色数组 */  // 每个按钮的前景色
    private int[] arraysColor;
    
    /** 单选项尺寸和边距 */  // itemWidth: 单选项宽度(像素)
    private int itemWidth, itemHeight, itemLeftMargin, itemRightMargin, itemTopMargin, itemBottomMargin;  // itemHeight: 单选项高度; itemLeftMargin: 左边距; itemRightMargin: 右边距; itemTopMargin: 上边距; itemBottomMargin: 下边距
    
    /** RecyclerView列表控件 */  // 用于展示单选按钮列表
    private RecyclerView listView;
    
    /** 列表适配器 */  // 负责绑定数据到视图
    private MRadioGroupAdapter adapter;
    
    /** 显示样式和布局参数 */  // showStyle: 显示样式(0:独立按钮, 1:两端圆角, 2:连续圆角)
    private int showStyle, firstLineShowCount, perLineShowTotalCount;  // firstLineShowCount: 首行显示数量; perLineShowTotalCount: 每行总显示数量
    
    /** 单选按钮数据列表 */  // 存储所有RadioButtonBean对象
    private List<RadioButtonBean> list;
    
    /** 提示文本控件 */  // 显示在列表左侧的提示文字
    private TextView txtPrompt;
    
    /** 排列方向 */  // 0:水平排列, 1:垂直排列
    private int orientation;
    
    /** 提示文本内容 */  // 显示在列表前的提示文字
    private String promptText;
    
    /** 提示文本与列表的间距 */  // 像素值
    private int promptInterval;
    
    /** 选中状态变化回调 */  // 使用Consumer函数式接口
    public Consumer<RadioButtonBean> OnIndexChange;

    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MRadioGroup(@NonNull Context context) {
        this(context, null);  // 调用双参数构造
    }

    /**
     * 双参数构造方法
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     */
    public MRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造
    }

    /**
     * 三参数构造方法（完整构造）
     * 解析XML自定义属性并初始化视图
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;  // 保存上下文引用
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MRadioGroup);  // 获取自定义属性数组
        arrays = ta.getTextArray(R.styleable.MRadioGroup_arrays);  // 读取文本数组属性

        itemWidth = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemWidth, 120);  // 读取单选项宽度，默认120px
        itemHeight = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemHeight, 60);  // 读取单选项高度，默认60px
        itemLeftMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemLeftMargin, 0);  // 读取左边距，默认0
        itemTopMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemTopMargin, 0);  // 读取上边距，默认0
        itemRightMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemRightMargin, 0);  // 读取右边距，默认0
        itemBottomMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemBottomMargin, 0);  // 读取下边距，默认0

        promptText = ta.getString(R.styleable.MRadioGroup_promptText);  // 读取提示文本
        promptInterval = ta.getDimensionPixelSize(R.styleable.MRadioGroup_promptInterval, 0);  // 读取提示文本间距
        showStyle = ta.getInt(R.styleable.MRadioGroup_showStyle, 0);  // 读取显示样式，默认0
        perLineShowTotalCount = ta.getInt(R.styleable.MRadioGroup_perLineShowTotalCount, 4);  // 读取每行显示数量，默认4
        firstLineShowCount = ta.getInt(R.styleable.MRadioGroup_firstLineShowCount, perLineShowTotalCount);  // 读取首行显示数量，默认等于每行数量

        // 默认为0，即水平，horizontal
        orientation = ta.getInt(R.styleable.MRadioGroup_android_orientation, 0);  // 读取排列方向，默认水平
        ta.recycle();  // 回收TypedArray资源
        initView();  // 初始化视图
    }

    // =========================== 视图初始化 ===========================
    
    /**
     * 初始化视图组件
     * 加载布局文件，配置RecyclerView和适配器
     */
    private void initView() {
        View.inflate(context, R.layout.control_micsig_radiogroup, this);  // 加载布局文件到当前容器
        listView = findViewById(R.id.listView);  // 获取RecyclerView引用
        txtPrompt = findViewById(R.id.txtPrompt);  // 获取提示文本引用
        if (promptText == null) {  // 如果没有设置提示文本
            txtPrompt.setVisibility(GONE);  // 隐藏提示文本
        } else {  // 有提示文本
            txtPrompt.setText(promptText);  // 设置提示文本内容
            txtPrompt.setVisibility(VISIBLE);  // 显示提示文本
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) listView.getLayoutParams();  // 获取列表布局参数
            layoutParams.setMargins(promptInterval, 0, 0, 0);  // 设置列表左边距
            listView.setLayoutParams(layoutParams);  // 应用布局参数
            this.requestLayout();  // 请求重新布局
        }

        listView.setLayoutManager(getLayoutManager());  // 设置布局管理器
        list = getInitList();  // 初始化数据列表
        adapter = new MRadioGroupAdapter(context, itemWidth, itemHeight, list);  // 创建适配器
        listView.setAdapter(adapter);  // 设置适配器
        adapter.notifyDataSetChanged();  // 通知数据变化
    }

    // =========================== 布局管理 ===========================
    
    /**
     * 设置瀑布流的列数
     * 
     * @param count 列数
     */
    public void setSpanSize(int count) {
        ((StaggeredGridLayoutManager) listView.getLayoutManager()).setSpanCount(count);  // 设置瀑布流列数
    }
    
    /**
     * 获取初始列数
     * 
     * @return 每行显示的总数量
     */
    public int getInitSpanSize() {
        return perLineShowTotalCount;  // 返回每行显示数量
    }

    // =========================== 数据设置方法 ===========================
    
    /**
     * 设置显示文本数组
     * 
     * @param array 文本数组
     */
    public void setArray(CharSequence[] array) {
        if (array != null && array.length > 0) {  // 检查数组有效性
            this.arrays = array;  // 保存数组引用
            updateView();  // 更新视图
        }
    }

    /**
     * 设置颜色数组
     * 
     * @param colors 颜色值数组
     */
    public void setColors(int[] colors) {
        if (colors != null && colors.length > 0) {  // 检查数组有效性
            this.arraysColor = colors;  // 保存颜色数组
            updateView();  // 更新视图
        }
    }

    /**
     * 设置预选中的选项
     * 根据文本内容匹配并选中对应选项
     * 
     * @param preString 要选中的选项文本
     */
    public void setPreString(String preString) {
        boolean flag = false;  // 是否找到匹配项的标志
        for (RadioButtonBean item : list) {  // 遍历所有选项
            if (item.getText().equals(preString)) {  // 检查文本是否匹配
                flag = true;  // 找到匹配项
                break;  // 跳出循环
            }
            if (item.isUserDefine(context)) {  // 检查是否为用户自定义项
                flag = true;  // 找到匹配项
                break;  // 跳出循环
            }
        }
        if (flag) {  // 如果找到匹配项
            boolean isCheck = false;  // 是否已选中的标志
            for (RadioButtonBean item : list) {  // 遍历所有选项
                if (item.getText().equals(preString)) {  // 检查文本是否匹配
                    item.setCheck(true);  // 设置为选中
                    isCheck = true;  // 标记已选中
                } else {  // 其他选项
                    item.setCheck(false);  // 设置为未选中
                }
            }
            if (!isCheck) {  // 如果没有直接匹配的文本
                for (RadioButtonBean item : list) {  // 遍历所有选项
                    if (item.isUserDefine(context)) {  // 检查是否为用户自定义项
                        item.setCheck(true);  // 选中用户自定义项
                    }
                }
            }
            adapter.notifyDataSetChanged();  // 通知适配器数据变化
        }
    }

    // =========================== 列表初始化 ===========================
    
    /**
     * 获取初始化的数据列表
     * 根据排列方向和显示样式创建RadioButtonBean列表
     * 
     * @return 初始化后的RadioButtonBean列表
     */
    private List<RadioButtonBean> getInitList() {
        List<RadioButtonBean> list = new ArrayList<>();  // 创建空列表
        boolean enableBeforeColor = this.arraysColor != null && (this.arraysColor.length == this.arrays.length) ? true : false;  // 检查颜色数组是否有效
        if (orientation == 0) { // 水平排列
            if (showStyle == 0) {  // 独立按钮样式
                int diff = perLineShowTotalCount - firstLineShowCount;  // 计算首行需要填充的空白项数量
                for (int i = 0; i < diff; i++) {  // 添加空白占位项
                    list.add(new RadioButtonBean(-1, "", false, false, 0, 0, null));  // 创建空白项，index为-1
                }
                for (int i = 0; i < arrays.length; i++) {  // 遍历文本数组
                    if (enableBeforeColor) {  // 如果启用了前置颜色
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_button_common, this::OnClick));  // 创建带颜色的选项
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);  // 设置边距
                    } else {  // 未启用前置颜色
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_button_common, this::OnClick));  // 创建普通选项
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);  // 设置边距
                    }
                }
            } else if (showStyle == 1) {  // 两端圆角样式（仅两个选项）
                if (enableBeforeColor) {  // 如果启用了前置颜色
                    list.add(new RadioButtonBean(0, arrays[0].toString(), false, true, arraysColor[0], R.drawable.selector_radio_circle_left, this::OnClick));  // 左侧圆角按钮
                    list.add(new RadioButtonBean(1, arrays[1].toString(), false, true, arraysColor[1], R.drawable.selector_radio_circle_right, this::OnClick));  // 右侧圆角按钮
                } else {  // 未启用前置颜色
                    list.add(new RadioButtonBean(0, arrays[0].toString(), false, false, 0, R.drawable.selector_radio_circle_left, this::OnClick));  // 左侧圆角按钮
                    list.add(new RadioButtonBean(1, arrays[1].toString(), false, false, 0, R.drawable.selector_radio_circle_right, this::OnClick));  // 右侧圆角按钮
                }
            } else if (showStyle == 2) {  // 连续圆角样式
                for (int i = 0; i < arrays.length; i++) {  // 遍历文本数组
                    if (enableBeforeColor) {  // 如果启用了前置颜色
                        if (i == 0) {  // 第一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_rect_left, this::OnClick));  // 左侧圆角
                        } else if (i == arrays.length - 1) {  // 最后一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_rect_right, this::OnClick));  // 右侧圆角
                        } else {  // 中间选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_rect_middle, this::OnClick));  // 中间样式
                        }
                    } else {  // 未启用前置颜色
                        if (i == 0) {  // 第一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_rect_left, this::OnClick));  // 左侧圆角
                        } else if (i == arrays.length - 1) {  // 最后一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_rect_right, this::OnClick));  // 右侧圆角
                        } else {  // 中间选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_rect_middle, this::OnClick));  // 中间样式
                        }
                    }
                }
            }
        } else { // 垂直排列
            if (showStyle == 0) {  // 独立按钮样式
                int diff = perLineShowTotalCount - firstLineShowCount;  // 计算首行需要填充的空白项数量
                for (int i = 0; i < diff; i++) {  // 添加空白占位项
                    list.add(new RadioButtonBean(-1, "", false, false, 0, 0, null));  // 创建空白项
                }
                for (int i = 0; i < arrays.length; i++) {  // 遍历文本数组
                    if (enableBeforeColor) {  // 如果启用了前置颜色
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_button_common, this::OnClick));  // 创建带颜色的选项
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);  // 设置边距
                    } else {  // 未启用前置颜色
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_button_common, this::OnClick));  // 创建普通选项
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);  // 设置边距
                    }
                }
            } else {  // 垂直连续样式
                for (int i = 0; i < arrays.length; i++) {  // 遍历文本数组
                    if (enableBeforeColor) {  // 如果启用了前置颜色
                        if (i == 0) {  // 第一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_vertical_rect_top, this::OnClick));  // 顶部圆角
                        } else if (i == arrays.length - 1) {  // 最后一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_vertical_rect_middle, this::OnClick));  // 底部圆角
                        } else {  // 中间选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, true, arraysColor[i], R.drawable.selector_radio_round_vertical_rect_bottom, this::OnClick));  // 中间样式
                        }
                    } else {  // 未启用前置颜色
                        if (i == 0) {  // 第一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_vertical_rect_top, this::OnClick));  // 顶部圆角
                        } else if (i == arrays.length - 1) {  // 最后一个选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_vertical_rect_middle, this::OnClick));  // 底部圆角
                        } else {  // 中间选项
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false, false, 0, R.drawable.selector_radio_round_vertical_rect_bottom, this::OnClick));  // 中间样式
                        }
                    }
                }
            }
        }
        return list;  // 返回初始化后的列表
    }

    /**
     * 更新视图
     * 重新配置提示文本和数据列表
     */
    private void updateView() {
        if (promptText == null) {  // 如果没有提示文本
            txtPrompt.setVisibility(GONE);  // 隐藏提示文本
        } else {  // 有提示文本
            txtPrompt.setText(promptText);  // 设置提示文本内容
            txtPrompt.setVisibility(VISIBLE);  // 显示提示文本
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) listView.getLayoutParams();  // 获取列表布局参数
            layoutParams.setMargins(promptInterval, 0, 0, 0);  // 设置列表左边距
            listView.setLayoutParams(layoutParams);  // 应用布局参数
            this.requestLayout();  // 请求重新布局
        }

        list = getInitList();  // 重新生成数据列表
        adapter.setList(list);  // 更新适配器数据
        adapter.notifyDataSetChanged();  // 通知数据变化
    }

    /**
     * 初始化数据（完整参数）
     * 
     * @param head 提示文本
     * @param array 选项文本数组
     * @param colors 选项颜色数组
     * @param OnIndexChange 选中变化回调
     */
    public void initData(String head, String[] array, int[] colors, Consumer<RadioButtonBean> OnIndexChange) {
        this.promptText = head;  // 设置提示文本
        this.arrays = array;  // 设置文本数组
        this.arraysColor = colors;  // 设置颜色数组
        this.OnIndexChange = OnIndexChange;  // 设置回调
        updateView();  // 更新视图
    }

    // =========================== 点击事件处理 ===========================
    
    /**
     * 选项点击事件处理
     * 更新选中状态并触发回调
     * 
     * @param itemView 被点击的视图
     * @param item 被点击的数据项
     */
    private void OnClick(View itemView, RadioButtonBean item) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (item.getIndex() == bean.getIndex()) {  // 找到被点击的选项
                bean.setCheck(true);  // 设置为选中
                if (OnIndexChange != null) OnIndexChange.accept(bean);  // 触发回调
            } else {  // 其他选项
                bean.setCheck(false);  // 设置为未选中
            }
        }

        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }
    
    /**
     * 通过索引触发点击事件
     * 
     * @param index 选项索引
     */
    public void OnClick(int index) {
        RadioButtonBean bean = list.get(index);  // 获取指定索引的数据项
        for (int i = 0; i < list.size(); i++) {  // 遍历所有选项
            RadioButtonBean item = list.get(i);  // 获取当前选项
            if (item.getIndex() == bean.getIndex() && item.getVisible() == VISIBLE) {  // 检查索引匹配且可见
                bean.setCheck(true);  // 设置为选中
                if (OnIndexChange != null) OnIndexChange.accept(bean);  // 触发回调
            } else {  // 其他选项
                bean.setCheck(false);  // 设置为未选中
            }
        }
    }

    // =========================== 布局管理器 ===========================
    
    /**
     * 获取布局管理器
     * 根据排列方向和显示样式返回合适的LayoutManager
     * 
     * @return RecyclerView.LayoutManager实例
     */
    private RecyclerView.LayoutManager getLayoutManager() {
        if (orientation == 0) { // 水平排列
            if (showStyle == 0) {  // 独立按钮样式
                return new StaggeredGridLayoutManager(perLineShowTotalCount, StaggeredGridLayoutManager.VERTICAL);  // 瀑布流布局
            } else {  // 连续样式
                return new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);  // 水平线性布局
            }
        } else { // 垂直排列
            if (showStyle == 0) {  // 独立按钮样式
                return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);  // 单列瀑布流
            } else {  // 连续样式
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);  // 垂直线性布局
            }
        }
    }

    // =========================== 选中状态管理 ===========================
    
    /**
     * 设置选中项索引
     * 
     * @param index 要选中的选项索引
     */
    public void setSelectIndex(int index) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.getIndex() == index) {  // 找到目标选项
                bean.setCheck(true);  // 设置为选中
            } else {  // 其他选项
                bean.setCheck(false);  // 设置为未选中
            }
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }

    /**
     * 获取当前选中项索引
     * 
     * @return 选中项索引，未选中返回-1
     */
    public int getSelectIndex() {
        int index = -1;  // 默认返回-1表示未选中
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.isCheck()) {  // 检查是否选中
                index = bean.getIndex();  // 获取选中项索引
            }
        }
        return index;  // 返回选中项索引
    }
    
    /**
     * 检查指定索引是否选中
     * 
     * @param idx 要检查的索引
     * @return true表示选中，false表示未选中
     */
    public boolean getChecked(int idx) {
        for (int i = 0; i < list.size(); i++) {  // 遍历所有选项
            if (i == idx) {  // 找到目标索引
                return list.get(i).isCheck();  // 返回选中状态
            }
        }
        return false;  // 索引不存在，返回false
    }

    /**
     * 清除所有选中状态
     */
    public void clearSelect() {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            bean.setCheck(false);  // 设置为未选中
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }

    /**
     * 检查是否所有选项都未选中
     * 
     * @return true表示全部未选中，false表示有选中项
     */
    public boolean isAllUnCheck() {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.isCheck()) {  // 检查是否选中
                return false;  // 有选中项，返回false
            }
        }
        return true;  // 全部未选中，返回true
    }

    // =========================== 可见性管理 ===========================
    
    /**
     * 获取第一个可见项的索引
     * 
     * @return 第一个可见项索引，无可见项返回-1
     */
    public int getFirstVisibleIdx() {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.getVisible() == VISIBLE) {  // 检查是否可见
                return bean.getIndex();  // 返回可见项索引
            }
        }
        return -1;  // 无可见项，返回-1
    }

    /**
     * 获取指定索引项的可见性
     * 
     * @param index 选项索引
     * @return 可见性状态（VISIBLE/GONE/INVISIBLE）
     */
    public int getItemVisible(int index) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.getIndex() == index) {  // 找到目标选项
                return bean.getVisible();  // 返回可见性
            }
        }
        return View.GONE;  // 未找到，返回GONE
    }

    /**
     * 设置指定索引项的可见性
     * 
     * @param index 选项索引
     * @param visible 可见性状态（VISIBLE/GONE/INVISIBLE）
     */
    public void setItemVisible(int index, int visible) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.getIndex() == index) {  // 找到目标选项
                bean.setVisible(visible);  // 设置可见性
            }
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }
    
    /**
     * 设置指定索引项的启用状态
     * 
     * @param index 选项索引
     * @param enable true表示启用，false表示禁用
     */
    public void setItemEnable(int index, boolean enable) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            if (bean.getIndex() == index) {  // 找到目标选项
                bean.setEnable(enable);  // 设置启用状态
            }
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }

    /**
     * 设置整个控件组的启用状态
     * 
     * @param enabled true表示启用，false表示禁用
     */
    @Override
    public void setEnabled(boolean enabled) {
        for (RadioButtonBean bean : list) {  // 遍历所有选项
            bean.setEnable(enabled);  // 设置启用状态
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变化
    }
}
