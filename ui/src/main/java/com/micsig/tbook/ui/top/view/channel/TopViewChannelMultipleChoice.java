package com.micsig.tbook.ui.top.view.channel; // 包名：顶部视图通道选择组件

import android.annotation.SuppressLint; // 导入：SuppressLint注解，用于抑制API警告
import android.content.Context; // 导入：上下文对象，用于访问资源和服务
import android.graphics.drawable.Drawable; // 导入：Drawable图形对象，用于设置图标
import android.util.TypedValue; // 导入：TypedValue，用于尺寸单位转换
import android.view.Gravity; // 导入：Gravity，用于控件对齐方式
import android.view.View; // 导入：View，Android基础视图类
import android.widget.CheckBox; // 导入：CheckBox，多选框控件
import android.widget.CompoundButton; // 导入：CompoundButton，复合按钮基类
import android.widget.LinearLayout; // 导入：LinearLayout，线性布局容器
import android.widget.RadioButton; // 导入：RadioButton，单选按钮（未使用）

import com.micsig.tbook.ui.R; // 导入：R资源类，访问应用资源
import com.micsig.tbook.ui.util.svg.SelectorUtil; // 导入：SelectorUtil，选择器工具类
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // 导入：SvgNodeInfo，SVG节点信息类
import com.micsig.tbook.ui.wavezone.TChan; // 导入：TChan，通道转换工具类

import java.util.ArrayList; // 导入：ArrayList，动态数组集合
import java.util.List; // 导入：List，列表接口

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                     TopViewChannelMultipleChoice                            │
 * │                        多选通道选择视图类                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属模块：UI层 -> 顶部视图 -> 通道选择组件                                 │
 * │   文件路径：ui/src/main/java/com/micsig/tbook/ui/top/view/channel/          │
 * │   核心职责：提供多选通道选择功能，支持CheckBox多选和"ALL"全选功能            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 多选通道列表显示：使用CheckBox实现通道的多选功能                        │
 * │   2. 全选功能支持：提供"ALL"按钮实现一键全选/取消全选                        │
 * │   3. 通道颜色管理：根据通道类型设置不同的显示颜色                            │
 * │   4. 可见性控制：支持动态控制各通道项的显示/隐藏                             │
 * │   5. 只读模式：支持设置只读状态，禁用未选中的通道                            │
 * │   6. 多通道类型支持：支持CH、Math、Ref三种通道类型                           │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │                                                                             │
 * │   ┌─────────────────────────────────────────────────────────────────┐      │
 * │   │                    TopViewChannelMultipleChoice                 │      │
 * │   ├─────────────────────────────────────────────────────────────────┤      │
 * │   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐   │      │
 * │   │  │  数据层     │  │  视图层     │  │  事件层              │   │      │
 * │   │  │ - arrayString│  │ - inflate  │  │ - onlyClickListener  │   │      │
 * │   │  │ - arrayColor │  │ - llMuti   │  │ - onTestListener     │   │      │
 * │   │  │ - chType     │  │   Check    │  │ - checkedChange      │   │      │
 * │   │  │              │  │   Channel  │  │   Listener           │   │      │
 * │   │  └─────────────┘  └─────────────┘  └──────────────────────┘   │      │
 * │   └─────────────────────────────────────────────────────────────────┘      │
 * │                                                                             │
 * │   设计模式：Builder模式（通过多个构造函数和setData方法构建对象）            │
 * │   视图模式：自定义视图组合模式（组合CheckBox实现多选功能）                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │                                                                             │
 * │   ┌──────────┐    setData()    ┌──────────────────────┐                  │
 * │   │ 外部调用 │ ──────────────> │ 初始化通道数据       │                  │
 * │   └──────────┘                 └──────────┬───────────┘                  │
 * │                                           │                               │
 * │                                           v                               │
 * │   ┌──────────────────────────────────────────────────────────┐           │
 * │   │                    updateView()                           │           │
 * │   │  1. 清空现有子视图                                        │           │
 * │   │  2. 创建CheckBox项（包含"ALL"按钮）                       │           │
 * │   │  3. 设置文本、颜色、图标                                  │           │
 * │   │  4. 绑定事件监听器                                        │           │
 * │   └──────────────────────────────────────────────────────────┘           │
 * │                                           │                               │
 * │                                           v                               │
 * │   ┌──────────┐    用户交互    ┌──────────────────────┐                  │
 * │   │ 事件触发 │ <───────────── │ CheckBox点击/选中    │                  │
 * │   └──────────┘                 └──────────┬───────────┘                  │
 * │                                           │                               │
 * │                                           v                               │
 * │   ┌──────────────────────────────────────────────────────────┐           │
 * │   │              回调外部监听器 / 更新ALL状态                  │           │
 * │   └──────────────────────────────────────────────────────────┘           │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖类：                                                                   │
 * │     - Context：Android上下文，用于资源访问和视图创建                        │
 * │     - TChan：通道转换工具，处理通道索引转换                                  │
 * │     - SelectorUtil：选择器工具，创建通道图标Drawable                         │
 * │     - SvgNodeInfo：SVG节点信息，获取通道颜色数组                            │
 * │                                                                             │
 * │   被依赖：                                                                   │
 * │     - 顶部视图相关Fragment/Activity                                          │
 * │     - 通道选择对话框                                                         │
 * │     - CSV导出对话框                                                          │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【通道类型说明】                                                             │
 * │   chType = 0：CH通道（物理通道，CH1-CH8）                                   │
 * │   chType = 1：Math通道（数学运算通道，Math1-Math8）                         │
 * │   chType = 2：Ref通道（参考通道，Ref1-Ref8）                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │                                                                             │
 * │   // 示例1：基本使用（显示ALL按钮）                                         │
 * │   TopViewChannelMultipleChoice choice = new TopViewChannelMultipleChoice(  │
 * │       context);                                                             │
 * │   choice.setData(R.array.channel_names, R.array.channel_colors);           │
 * │   List<Integer> selected = choice.getSelectedIndex();                      │
 * │                                                                             │
 * │   // 示例2：不显示ALL按钮                                                   │
 * │   TopViewChannelMultipleChoice choice = new TopViewChannelMultipleChoice(  │
 * │       context, false);                                                      │
 * │   choice.setData(R.array.channel_names, R.array.channel_colors);           │
 * │                                                                             │
 * │   // 示例3：带单击监听器                                                     │
 * │   TopViewChannelMultipleChoice choice = new TopViewChannelMultipleChoice(  │
 * │       context, true, (checkBox) -> {                                        │
 * │           // 处理单击事件                                                   │
 * │       });                                                                   │
 * │                                                                             │
 * │   // 示例4：设置Math通道类型                                                 │
 * │   choice.setData(R.array.math_names, R.array.colors, 100, 90, 1);          │
 * │                                                                             │
 * │   // 示例5：获取选中通道                                                     │
 * │   List<CheckBox> selected = choice.getSelectedCheckBox();                  │
 * │   List<Integer> indexes = choice.getSelectedIndex();                       │
 * │   int count = choice.getSelectCount();                                      │
 * │                                                                             │
 * │   // 示例6：设置只读模式                                                     │
 * │   choice.setReadOnly(false); // 禁用未选中的通道                            │
 * │   choice.setReadOnly(true);  // 恢复正常状态                                │
 * │                                                                             │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【注意事项】                                                                 │
 * │   1. 必须在UI线程调用所有方法                                               │
 * │   2. setData()方法必须在构造后调用，否则视图为空                            │
 * │   3. 当showAll=true时，最后一个CheckBox为"ALL"按钮                          │
 * │   4. 通道索引从0开始，对应CH1/Math1/Ref1                                    │
 * │   5. 颜色数组由SvgNodeInfo统一管理，确保颜色一致性                          │
 * │   6. 只读模式下，已选中的通道不可取消选中                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【版本历史】                                                                 │
 * │   v1.0.0 - 2017/04/05 - 初始版本，实现基本多选功能                          │
 * │   v1.1.0 - 新增Math和Ref通道类型支持                                        │
 * │   v1.2.0 - 新增只读模式和可见性控制                                         │
 * │   v1.3.0 - 新增通道颜色动态更新功能                                         │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @author Administrator
 * @version 1.3.0
 * @since 2017/04/05
 */
public class TopViewChannelMultipleChoice {

    // ================================ 成员变量定义 ================================

    /**
     * Android上下文对象
     * 用途：用于访问资源、创建视图、获取系统服务
     * 生命周期：随对象创建而初始化，不可变
     */
    private final Context context;

    /**
     * 根视图对象
     * 用途：从布局文件加载的视图容器，包含所有CheckBox子视图
     * 布局文件：R.layout.view_muti_check_channel
     */
    private View inflate;

    /**
     * 通道名称字符串数组
     * 用途：存储各通道的显示名称（如"CH1"、"CH2"等）
     * 数据来源：通过setData()方法从资源文件加载
     */
    private String[] arrayString;

    /**
     * 通道颜色整型数组
     * 用途：存储各通道的显示颜色值（ARGB格式）
     * 数据来源：通过SvgNodeInfo.getColorsIntForView()获取
     */
    private int[] arrayColor;

    /**
     * 多选通道容器布局
     * 用途：包含所有CheckBox子视图的线性布局容器
     * 布局方向：水平排列（根据XML配置）
     */
    private LinearLayout llMutiCheckChannel;

    /**
     * 是否显示"ALL"全选按钮
     * 用途：控制是否在通道列表末尾添加"ALL"按钮
     * 默认值：true（显示ALL按钮）
     * 影响：影响子视图数量计算和事件处理逻辑
     */
    private boolean showAll = true;

    /**
     * 每个通道项的宽度（像素）
     * 用途：控制CheckBox的显示宽度
     * 默认值：0（使用WRAP_CONTENT）
     * 单位：像素（px）
     */
    private int itemWidth = 0;

    /**
     * 每个通道项的高度（像素）
     * 用途：控制CheckBox的显示高度
     * 默认值：90像素
     * 单位：像素（px）
     */
    private int itemHeight = 90;

    /**
     * 单击事件监听器
     * 用途：处理CheckBox的单击事件（非选中状态变化）
     * 触发时机：当showAll=false时，点击CheckBox触发
     * 可选：可为null，表示不处理单击事件
     */
    private onItemOnlyClickListener onlyClickListener;

    /**
     * 测试事件监听器
     * 用途：处理CheckBox选中状态变化事件
     * 触发时机：当CheckBox选中状态改变时触发
     * 可选：可为null，表示不处理选中事件
     */
    private onTestListener onTestListener;

    /**
     * 通道类型标识
     * 用途：标识当前处理的通道类型，影响图标和颜色显示
     * 取值范围：
     *   0 -> CH通道（物理通道）
     *   1 -> Math通道（数学运算通道）
     *   2 -> Ref通道（参考通道）
     * 默认值：0（CH通道）
     */
    private int chType = 0;

    // ================================ 接口定义 ================================

    /**
     * 单击事件监听接口
     * 用途：定义CheckBox单击事件的回调方法
     * 使用场景：当showAll=false时，用于处理CheckBox的单击事件
     *
     * 使用示例：
     *   choice = new TopViewChannelMultipleChoice(context, false, (checkBox) -> {
     *       // 处理单击事件
     *       Log.d("TAG", "Clicked: " + checkBox.getText());
     *   });
     */
    public interface onItemOnlyClickListener {
        /**
         * 单击事件回调方法
         *
         * @param checkBox 被单击的CheckBox对象，可通过getText()获取通道名称
         */
        void onlyClick(CheckBox checkBox);
    }

    /**
     * 测试事件监听接口
     * 用途：定义CheckBox选中状态变化的回调方法
     * 使用场景：当需要响应CheckBox选中状态变化时使用
     *
     * 使用示例：
     *   choice.setData(R.array.channels, R.array.colors, (checkBox) -> {
     *       if (checkBox.isChecked()) {
     *           // 处理选中事件
     *       }
     *   });
     */
    public interface onTestListener {
        /**
         * 选中状态变化回调方法
         *
         * @param checkBox 状态变化的CheckBox对象，可通过isChecked()获取当前状态
         */
        void onTest(CheckBox checkBox);
    }

    // ================================ 构造方法 ================================

    /**
     * 构造方法：创建默认配置的多选通道选择器
     * 功能：初始化上下文并加载视图，默认显示"ALL"按钮
     *
     * @param context Android上下文对象，用于资源访问和视图创建
     *                不能为null，否则会抛出NullPointerException
     *
     * 使用示例：
     *   TopViewChannelMultipleChoice choice = new TopViewChannelMultipleChoice(context);
     *   choice.setData(R.array.channels, R.array.colors);
     */
    public TopViewChannelMultipleChoice(Context context) {
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图，加载布局文件
    }

    /**
     * 构造方法：创建指定"ALL"按钮显示状态的多选通道选择器
     * 功能：初始化上下文、设置是否显示"ALL"按钮，并加载视图
     *
     * @param context Android上下文对象，用于资源访问和视图创建
     *                不能为null，否则会抛出NullPointerException
     * @param showAll 是否显示"ALL"全选按钮
     *                true - 在通道列表末尾显示"ALL"按钮
     *                false - 不显示"ALL"按钮
     *
     * 使用示例：
     *   // 显示ALL按钮
     *   TopViewChannelMultipleChoice choice1 = new TopViewChannelMultipleChoice(context, true);
     *   // 不显示ALL按钮
     *   TopViewChannelMultipleChoice choice2 = new TopViewChannelMultipleChoice(context, false);
     */
    public TopViewChannelMultipleChoice(Context context, boolean showAll) {
        this.context = context; // 保存上下文引用
        this.showAll = showAll; // 设置是否显示ALL按钮
        initView(); // 初始化视图，加载布局文件
    }

    /**
     * 构造方法：创建完整配置的多选通道选择器
     * 功能：初始化上下文、设置"ALL"按钮显示状态、设置单击监听器，并加载视图
     *
     * @param context Android上下文对象，用于资源访问和视图创建
     *                不能为null，否则会抛出NullPointerException
     * @param showAll 是否显示"ALL"全选按钮
     *                true - 在通道列表末尾显示"ALL"按钮
     *                false - 不显示"ALL"按钮
     * @param onlyClickListener 单击事件监听器，可为null
     *                          当showAll=false时，点击CheckBox会触发此监听器
     *
     * 使用示例：
     *   TopViewChannelMultipleChoice choice = new TopViewChannelMultipleChoice(
     *       context,
     *       false,
     *       (checkBox) -> {
     *           Log.d("TAG", "Clicked: " + checkBox.getText());
     *       }
     *   );
     */
    public TopViewChannelMultipleChoice(Context context, boolean showAll, onItemOnlyClickListener onlyClickListener) {
        this.context = context; // 保存上下文引用
        this.showAll = showAll; // 设置是否显示ALL按钮
        this.onlyClickListener = onlyClickListener; // 保存单击监听器引用
        initView(); // 初始化视图，加载布局文件
    }

    // ================================ 数据设置方法 ================================

    /**
     * 设置通道数据（基本方法）
     * 功能：从资源文件加载通道名称和颜色，初始化视图
     *
     * @param arrayResId 通道名称字符串数组资源ID
     *                   例如：R.array.channel_names
     * @param arrayColorResId 通道颜色数组资源ID（当前未使用，颜色从SvgNodeInfo获取）
     *                        例如：R.array.channel_colors
     *
     * 注意事项：
     *   1. 必须在构造方法后调用
     *   2. 会清空现有视图并重新创建
     *   3. 颜色实际从SvgNodeInfo.getColorsIntForView()获取，arrayColorResId参数未使用
     *
     * 使用示例：
     *   choice.setData(R.array.channel_names, R.array.channel_colors);
     */
    public void setData(int arrayResId, int arrayColorResId) {
        this.setData(arrayResId, arrayColorResId, null); // 调用重载方法，监听器为null
    }

    /**
     * 设置通道数据（带测试监听器）
     * 功能：从资源文件加载通道名称和颜色，设置测试监听器，初始化视图
     *
     * @param arrayResId 通道名称字符串数组资源ID
     *                   例如：R.array.channel_names
     * @param arrayColorResId 通道颜色数组资源ID（当前未使用，颜色从SvgNodeInfo获取）
     *                        例如：R.array.channel_colors
     * @param onTestListener 测试事件监听器，当CheckBox选中状态变化时触发
     *                        可为null，表示不处理选中事件
     *
     * 注意事项：
     *   1. 必须在构造方法后调用
     *   2. 会清空现有视图并重新创建
     *   3. 颜色实际从SvgNodeInfo.getColorsIntForView()获取
     *
     * 使用示例：
     *   choice.setData(R.array.channel_names, R.array.channel_colors, (checkBox) -> {
     *       if (checkBox.isChecked()) {
     *           Log.d("TAG", "Selected: " + checkBox.getText());
     *       }
     *   });
     */
    public void setData(int arrayResId, int arrayColorResId, onTestListener onTestListener) {
        this.onTestListener = onTestListener; // 保存测试监听器引用
        this.arrayString = context.getResources().getStringArray(arrayResId); // 从资源加载通道名称数组
        this.arrayColor = SvgNodeInfo.getColorsIntForView(); // 从SVG节点信息获取颜色数组
        updateView(context); // 更新视图，创建CheckBox子视图
    }

    /**
     * 设置通道数据（完整配置）
     * 功能：从资源文件加载通道名称和颜色，设置视图尺寸和通道类型，初始化视图
     *
     * @param arrayResId 通道名称字符串数组资源ID
     *                   例如：R.array.channel_names
     * @param arrayColorResId 通道颜色数组资源ID（当前未使用，颜色从SvgNodeInfo获取）
     *                        例如：R.array.channel_colors
     * @param width 每个通道项的宽度（像素）
     *              0表示使用WRAP_CONTENT
     *              例如：100
     * @param height 每个通道项的高度（像素）
     *               例如：90
     * @param chType 通道类型标识
     *               0 - CH通道（物理通道）
     *               1 - Math通道（数学运算通道）
     *               2 - Ref通道（参考通道）
     *
     * 注意事项：
     *   1. 必须在构造方法后调用
     *   2. 会清空现有视图并重新创建
     *   3. 通道类型影响图标显示和颜色映射
     *
     * 使用示例：
     *   // 设置CH通道
     *   choice.setData(R.array.ch_names, R.array.colors, 100, 90, 0);
     *   // 设置Math通道
     *   choice.setData(R.array.math_names, R.array.colors, 100, 90, 1);
     *   // 设置Ref通道
     *   choice.setData(R.array.ref_names, R.array.colors, 100, 90, 2);
     */
    public void setData(int arrayResId, int arrayColorResId, int width, int height, int chType) {
        this.arrayString = context.getResources().getStringArray(arrayResId); // 从资源加载通道名称数组
        this.arrayColor = SvgNodeInfo.getColorsIntForView(); // 从SVG节点信息获取颜色数组
        this.itemWidth = width; // 设置通道项宽度
        this.itemHeight = height; // 设置通道项高度
        this.chType = chType; // 设置通道类型
        updateView(context); // 更新视图，创建CheckBox子视图
    }

    // ================================ 视图获取方法 ================================

    /**
     * 获取指定索引位置的CheckBox视图
     * 功能：根据索引获取容器中的CheckBox对象
     *
     * @param index CheckBox的索引位置，从0开始
     *               当showAll=true时，最后一个索引为"ALL"按钮
     *               范围：0 到 llMutiCheckChannel.getChildCount()-1
     *
     * @return CheckBox对象，如果索引越界则返回null
     *
     * 注意事项：
     *   1. 索引从0开始
     *   2. 当showAll=true时，最后一个CheckBox是"ALL"按钮
     *   3. 如果索引越界，返回null
     *
     * 使用示例：
     *   CheckBox ch1 = choice.getShowViewIndex(0); // 获取第一个通道
     *   CheckBox all = choice.getShowViewIndex(8); // 获取ALL按钮（当showAll=true时）
     */
    public CheckBox getShowViewIndex(int index) {
        // 检查索引是否在有效范围内
        if (index >= 0 && index < llMutiCheckChannel.getChildCount()) {
            return (CheckBox) llMutiCheckChannel.getChildAt(index); // 返回指定位置的CheckBox
        } else {
            return null; // 索引越界，返回null
        }
    }

    /**
     * 获取所有选中的CheckBox列表
     * 功能：遍历所有通道CheckBox，返回选中且可见的CheckBox列表
     *
     * @return 选中的CheckBox列表，如果没有选中项则返回空列表
     *
     * 注意事项：
     *   1. 只返回选中（isChecked()=true）且可见（getVisibility()=VISIBLE）的CheckBox
     *   2. 不包含"ALL"按钮
     *   3. 返回的列表是新建的ArrayList，修改不影响原视图
     *
     * 使用示例：
     *   List<CheckBox> selected = choice.getSelectedCheckBox();
     *   for (CheckBox cb : selected) {
     *       Log.d("TAG", "Selected: " + cb.getText());
     *   }
     */
    public List<CheckBox> getSelectedCheckBox() {
        List<CheckBox> checkBoxes = new ArrayList<>(); // 创建结果列表
        // 计算通道数量：如果显示ALL按钮，则减1
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        // 遍历所有通道CheckBox
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 检查是否选中且可见
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                checkBoxes.add(checkBox); // 添加到结果列表
            }
        }
        return checkBoxes; // 返回结果列表
    }

    /**
     * 获取选中通道的数量
     * 功能：统计选中且可见的通道数量
     *
     * @return 选中的通道数量，范围：0 到 通道总数
     *
     * 注意事项：
     *   1. 只统计选中（isChecked()=true）且可见（getVisibility()=VISIBLE）的CheckBox
     *   2. 不包含"ALL"按钮
     *
     * 使用示例：
     *   int count = choice.getSelectCount();
     *   if (count == 0) {
     *       Toast.makeText(context, "请至少选择一个通道", Toast.LENGTH_SHORT).show();
     *   }
     */
    public int getSelectCount() {
        int selectCount = 0; // 初始化选中计数器
        // 计算通道数量：如果显示ALL按钮，则减1
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        // 遍历所有通道CheckBox
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 检查是否选中且可见
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                selectCount++; // 选中计数加1
            }
        }
        return selectCount; // 返回选中数量
    }

    /**
     * 获取所有选中通道的索引列表
     * 功能：遍历所有通道CheckBox，返回选中且可见的通道索引列表
     *
     * @return 选中的通道索引列表，从0开始，如果没有选中项则返回空列表
     *
     * 注意事项：
     *   1. 只返回选中（isChecked()=true）且可见（getVisibility()=VISIBLE）的CheckBox索引
     *   2. 不包含"ALL"按钮的索引
     *   3. 返回的列表是新建的ArrayList，修改不影响原视图
     *
     * 使用示例：
     *   List<Integer> indexes = choice.getSelectedIndex();
     *   for (int index : indexes) {
     *       Log.d("TAG", "Selected index: " + index);
     *   }
     */
    public List<Integer> getSelectedIndex() {
        List<Integer> selectS = new ArrayList<>(); // 创建结果列表
        // 计算通道数量：如果显示ALL按钮，则减1
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        // 遍历所有通道CheckBox
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 检查是否选中且可见
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                selectS.add(i); // 添加索引到结果列表
            }
        }
        return selectS; // 返回结果列表
    }

    /**
     * 设置指定索引的CheckBox为选中状态
     * 功能：将指定索引的CheckBox设置为选中，其他CheckBox状态不变
     *
     * @param checkedIndex 要设置为选中的CheckBox索引，从0开始
     *                     范围：0 到 llMutiCheckChannel.getChildCount()-1
     *
     * 注意事项：
     *   1. 此方法不会取消其他CheckBox的选中状态
     *   2. 如果需要单选效果，需要先调用unCheckAll()取消所有选中
     *   3. 索引越界时不会抛出异常，但不会有效果
     *
     * 使用示例：
     *   choice.setChecked(0); // 选中第一个通道
     */
    public void setChecked(int checkedIndex) {
        // 遍历所有CheckBox
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 如果是目标索引，设置为选中
            if (i == checkedIndex) {
                checkBox.setChecked(true); // 设置为选中状态
            }
        }
    }

    /**
     * 获取根视图对象
     * 功能：返回从布局文件加载的根视图，用于添加到父容器中
     *
     * @return 根视图对象，包含所有CheckBox子视图
     *
     * 使用示例：
     *   View view = choice.getInflate();
     *   parentLayout.addView(view);
     */
    public View getInflate() {
        return inflate; // 返回根视图
    }

    // ================================ 可见性控制方法 ================================

    /**
     * 设置各通道项的可见性
     * 功能：根据布尔数组控制各通道CheckBox的显示/隐藏状态
     *
     * @param visible 可见性布尔数组，长度应与通道数量一致
     *                true - 显示该通道（View.VISIBLE）
     *                false - 隐藏该通道（View.GONE）
     *
     * 注意事项：
     *   1. 数组长度应与通道数量一致，否则可能抛出ArrayIndexOutOfBoundsException
     *   2. 隐藏的通道会自动取消选中状态
     *   3. 会自动更新"ALL"按钮的状态
     *
     * 使用示例：
     *   boolean[] visible = {true, true, false, false, true, true, true, true};
     *   choice.setItemVisible(visible); // 隐藏CH3和CH4
     */
    public void setItemVisible(boolean[] visible) {
        setCheckBoxItemVisible(visible); // 调用内部方法设置可见性
    }

    /**
     * 内部方法：设置CheckBox可见性
     * 功能：实际执行可见性设置的内部方法，处理隐藏时的取消选中逻辑
     *
     * @param visible 可见性布尔数组
     *                true - 显示该通道（View.VISIBLE）
     *                false - 隐藏该通道（View.GONE）
     *
     * 处理逻辑：
     *   1. 遍历所有通道CheckBox
     *   2. 根据数组设置可见性
     *   3. 隐藏的通道自动取消选中
     *   4. 更新"ALL"按钮状态
     */
    private void setCheckBoxItemVisible(boolean[] visible) {
        // 计算通道数量：如果显示ALL按钮，则减1
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        // 遍历所有通道CheckBox
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 设置可见性
            checkBox.setVisibility(visible[i] ? View.VISIBLE : View.GONE);
            // 如果隐藏，则取消选中状态
            if (checkBox.getVisibility() == View.GONE) {
                checkBox.setChecked(false); // 取消选中
            }
        }
        // 更新"ALL"按钮的选中状态
        updateFinalAllstate();
    }

    // ================================ 颜色设置方法 ================================

    /**
     * 设置单个通道的颜色
     * 功能：更新指定通道的颜色和图标，用于通道颜色变化时刷新显示
     *
     * @param chIndex 通道索引（UI索引，0-7对应CH1-CH8）
     * @param colorStr 颜色字符串（当前未使用，颜色从SvgNodeInfo获取）
     *
     * 注意事项：
     *   1. 此方法仅适用于CH通道类型（chType=0）
     *   2. 会重新获取最新的颜色数组
     *   3. 如果通道不存在，方法直接返回
     *
     * 使用示例：
     *   choice.setChannelColor(0, "#FF0000"); // 更新CH1的颜色
     */
    public void setChannelColor(int chIndex, String colorStr) { // 只更新个别通道
        int viewIndex = TChan.toFpgaChNo(chIndex); // 将UI索引转换为FPGA索引
        CheckBox checkBox = getShowViewIndex(viewIndex); // 获取对应的CheckBox
        if(checkBox == null) return; // 如果不存在，直接返回
        arrayColor = SvgNodeInfo.getColorsIntForView(); // 重新获取颜色数组
        checkBox.setTextColor(arrayColor[viewIndex]); // 设置文字颜色
        setBtnDrawable(checkBox, viewIndex); // 设置按钮图标
    }

    /**
     * 设置单个通道的颜色（用于CSV对话框）
     * 功能：更新指定通道的颜色和图标，支持Math和Ref通道类型
     *
     * @param chIndex 通道索引（UI索引）
     * @param colorStr 颜色字符串（当前未使用，颜色从SvgNodeInfo获取）
     *
     * 注意事项：
     *   1. 根据chType自动处理索引转换
     *   2. Math通道：chIndex会转换为Math索引
     *   3. Ref通道：chIndex会转换为Ref索引
     *   4. 如果通道不存在，方法直接返回
     *
     * 使用示例：
     *   // 对于Math通道（chType=1）
     *   choice.setChannelColorForDialogCSv(0, "#FF0000"); // 更新Math1的颜色
     */
    public void setChannelColorForDialogCSv(int chIndex, String colorStr) { // 只更新个别通道
        int viewIndex = TChan.toFpgaChNo(chIndex); // 将UI索引转换为FPGA索引
        CheckBox checkBox = getShowViewIndex(viewIndex); // 获取对应的CheckBox
        // 根据通道类型处理索引转换
        switch (chType) {
            case 1: // Math通道
                viewIndex = TChan.toFpgaChNo(TChan.toMathNumber(chIndex)); // 转换为Math通道索引
                break;
            case 2: // Ref通道
                viewIndex = TChan.toFpgaChNo(TChan.toRefNumber(chIndex)); // 转换为Ref通道索引
                break;
            case 0: // CH通道
            default: // 默认处理
                break;
        }
        checkBox = getShowViewIndex(viewIndex); // 重新获取转换后的CheckBox
        if(checkBox == null) return; // 如果不存在，直接返回
        arrayColor = SvgNodeInfo.getColorsIntForView(); // 重新获取颜色数组
        checkBox.setTextColor(arrayColor[TChan.toFpgaChNo(chIndex)]); // 设置文字颜色
        setBtnDrawable(checkBox, viewIndex); // 设置按钮图标
    }

    // ================================ 通道获取方法 ================================

    /**
     * 获取选中的通道索引列表
     * 功能：返回选中的通道索引，等同于getSelectedIndex()
     *
     * @return 选中的通道索引列表，范围：0-8（对应CH1-CH8）
     *
     * 注意事项：
     *   1. 此方法是getSelectedIndex()的别名
     *   2. 返回值范围：0-8
     *
     * 使用示例：
     *   List<Integer> channels = choice.getSelectChannel();
     *   for (int ch : channels) {
     *       Log.d("TAG", "Channel: CH" + (ch + 1));
     *   }
     */
    public List<Integer> getSelectChannel() {
        return getSelectedIndex(); // 调用getSelectedIndex()方法
    }

    /**
     * 获取CheckBox容器布局
     * 功能：返回包含所有CheckBox的线性布局容器
     *
     * @return LinearLayout容器，包含所有通道CheckBox和"ALL"按钮
     *
     * 使用场景：
     *   1. 需要直接操作容器布局时
     *   2. 需要遍历所有子视图时
     *
     * 使用示例：
     *   LinearLayout container = choice.getCheckBoxs();
     *   int childCount = container.getChildCount();
     */
    public LinearLayout getCheckBoxs() {
        return llMutiCheckChannel; // 返回容器布局
    }

    // ================================ 内部初始化方法 ================================

    /**
     * 初始化视图
     * 功能：从布局文件加载视图，在构造方法中调用
     *
     * 布局文件：R.layout.view_muti_check_channel
     * 包含内容：LinearLayout容器（id: ll_muti_check_channel）
     */
    private void initView() {
        inflate = View.inflate(context, R.layout.view_muti_check_channel, null); // 加载布局文件
    }

    /**
     * 更新视图
     * 功能：清空现有子视图，根据数据创建新的CheckBox子视图
     *
     * @param context Android上下文对象
     *
     * 处理流程：
     *   1. 获取容器布局引用
     *   2. 设置容器内边距
     *   3. 清空所有现有子视图
     *   4. 根据数据创建CheckBox子视图
     *   5. 设置CheckBox属性（文本、颜色、图标、事件）
     *   6. 添加到容器
     *   7. 更新子视图尺寸
     */
    private void updateView(Context context) {
        // 获取容器布局
        llMutiCheckChannel = (LinearLayout) inflate.findViewById(R.id.ll_muti_check_channel);
        // 设置容器内边距：左10px，上0px，右0px，下1px
        llMutiCheckChannel.setPadding(10, 0, 0, 1);
        // 计算现有子视图数量：如果显示ALL按钮，则减1
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        // 从后往前删除所有子视图，避免索引问题
        for (int i = count; i >= 0; i--) {
            llMutiCheckChannel.removeView(llMutiCheckChannel.getChildAt(i)); // 移除第i个子视图
        }
        // 计算新的子视图数量：如果显示ALL按钮，则加1
        int length = showAll ? arrayString.length + 1 : arrayString.length;
        // 创建所有CheckBox子视图
        for (int i = 0; i < length; i++) {
            CheckBox checkBox = new CheckBox(context); // 创建新的CheckBox
            // 创建布局参数：宽度WRAP_CONTENT，高度itemHeight
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
            // 如果不显示ALL按钮，设置左边距30px
            if (!showAll) {
                layoutParams.setMarginStart(30);
            }
            // 设置右边距30px
            layoutParams.setMarginEnd(30);
            int r = 30; // 右内边距
            int tb = 0; // 上下内边距
            // 设置CheckBox内边距：左0，上0，右30，下0
            checkBox.setPadding(0, tb, r, tb);
            // 应用布局参数
            checkBox.setLayoutParams(layoutParams);
            // 设置内容居中对齐
            checkBox.setGravity(Gravity.CENTER);
            // 生成唯一ID
            checkBox.setId(View.generateViewId());
            // 设置CheckBox文本和颜色
            if (showAll && i == length - 1) {
                // 最后一个位置是"ALL"按钮
                checkBox.setText("ALL"); // 设置文本为"ALL"
                // 设置ALL按钮颜色为通用颜色
                checkBox.setTextColor(context.getResources().getColor(R.color.colorChCommon));
            } else {
                // 普通通道CheckBox
                checkBox.setText(arrayString[i]); // 设置通道名称
                checkBox.setTextColor(arrayColor[i]); // 设置通道颜色
            }
            // 设置文字大小为20px
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
            // 清除背景
            checkBox.setBackground(null);
            // 清除默认按钮图标
            checkBox.setButtonDrawable(null);
            // 设置自定义按钮图标
            setBtnDrawable(checkBox, i);
            // 设置图标与文字间距为8px
            checkBox.setCompoundDrawablePadding(8);
            // 设置事件监听器
            if (showAll) {
                // 显示ALL按钮的情况
                if (i != arrayString.length) {
                    // 普通通道：设置选中状态变化监听器
                    checkBox.setOnCheckedChangeListener(checkedChangeListener);
                } else {
                    // ALL按钮：设置点击监听器
                    checkBox.setOnClickListener(clickListener);
                }
            } else {
                // 不显示ALL按钮的情况
                if (i != arrayString.length) {
                    // 普通通道：设置选中状态变化监听器
                    checkBox.setOnCheckedChangeListener(checkedChangeListener);
                }
                // 所有CheckBox都设置点击监听器
                checkBox.setOnClickListener(clickListener);
            }
            // 将CheckBox添加到容器
            llMutiCheckChannel.addView(checkBox);
        }
        // 更新子视图尺寸
        updateChild();
    }

    /**
     * 设置CheckBox按钮图标
     * 功能：根据通道类型和索引创建并设置CheckBox的左侧图标
     *
     * @param checkBox 目标CheckBox对象
     * @param index 通道索引（0-7对应CH1-CH8，或Math1-Math8，或Ref1-Ref8）
     *
     * 处理逻辑：
     *   1. 根据chType确定通道类型
     *   2. 调用SelectorUtil创建对应图标
     *   3. 设置图标边界
     *   4. 设置为左侧图标
     */
    private void setBtnDrawable(CheckBox checkBox, int index) {
        Drawable drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index)); // 默认：CH通道图标
        // 根据通道类型创建对应图标
        if (chType == 0) { // CH通道
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index)); // 创建CH通道图标
        } else if (chType == 1) { // Math通道
            // Math通道图标索引 = UI索引 + 8（CH通道数量）
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.Ch8);
        } else if (chType == 2) { // Ref通道
            // Ref通道图标索引 = UI索引 + 16（CH + Math通道数量）
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.Math8);
        } else { // 默认处理
            // 使用最大索引值
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.R8);
        }
        // 设置图标边界：宽21px，高21px
        drawable.setBounds(0, 0, 21, 21);
        // 设置左侧图标（左、上、右、下）
        checkBox.setCompoundDrawables(drawable,null,null,null);
    }

    // ================================ 事件监听器 ================================

    /**
     * 点击事件监听器
     * 功能：处理CheckBox的点击事件，主要用于"ALL"按钮的全选/取消全选功能
     *
     * 处理逻辑：
     *   1. 如果showAll=true：
     *      - 获取"ALL"按钮的选中状态
     *      - 将所有可见的通道CheckBox设置为相同状态
     *   2. 如果showAll=false：
     *      - 触发onlyClickListener回调
     */
    private final View.OnClickListener clickListener = v -> {
        if(showAll) { // 显示ALL按钮的情况
            // 获取"ALL"按钮（最后一个子视图）
            CheckBox allCheckBox = (CheckBox) llMutiCheckChannel.getChildAt(llMutiCheckChannel.getChildCount() - 1);
            // 获取ALL按钮的选中状态
            boolean isCheck = allCheckBox.isChecked();
            // 遍历所有通道CheckBox（不包括ALL按钮）
            for (int i = 0; i < llMutiCheckChannel.getChildCount() - 1; i++) {
                CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
                // 只设置可见的CheckBox，状态与ALL按钮一致
                checkBox.setChecked(checkBox.getVisibility() == View.VISIBLE && isCheck);
            }
        } else { // 不显示ALL按钮的情况
            // 触发单击监听器回调
            if(onlyClickListener != null) {
                onlyClickListener.onlyClick((CheckBox) v);
            }
        }
    };

    /**
     * 选中状态变化监听器
     * 功能：处理CheckBox选中状态变化事件
     *
     * 处理逻辑：
     *   1. 更新"ALL"按钮的选中状态
     *   2. 触发onTestListener回调
     */
    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // 更新"ALL"按钮的选中状态
            updateFinalAllstate();
            // 触发测试监听器回调
            if (onTestListener != null) {
                onTestListener.onTest((CheckBox) buttonView);
            }
        }
    };

    /**
     * 更新"ALL"按钮的选中状态
     * 功能：检查所有通道CheckBox的选中状态，更新"ALL"按钮的状态
     *
     * 处理逻辑：
     *   1. 如果不显示ALL按钮，直接返回
     *   2. 遍历所有通道CheckBox
     *   3. 如果所有可见的通道都被选中，则ALL按钮选中
     *   4. 否则ALL按钮不选中
     */
    private void updateFinalAllstate() {
        // 如果不显示ALL按钮，直接返回
        if (!showAll) return;
        // 获取"ALL"按钮（最后一个子视图）
        CheckBox finalAllCheckBox = (CheckBox) llMutiCheckChannel.getChildAt(llMutiCheckChannel.getChildCount() - 1);
        boolean isChecked = true; // 默认为选中状态
        // 遍历所有通道CheckBox（不包括ALL按钮）
        for (int i = 0; i < llMutiCheckChannel.getChildCount() - 1; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 如果有可见但未选中的通道，则ALL按钮不选中
            if (checkBox.getVisibility() == View.VISIBLE && !checkBox.isChecked()) {
                isChecked = false; // 设置为不选中
                break; // 跳出循环
            }
        }
        // 更新ALL按钮的选中状态
        finalAllCheckBox.setChecked(isChecked);
    }

    // ================================ 公共操作方法 ================================

    /**
     * 更新子视图尺寸
     * 功能：根据itemWidth和itemHeight更新所有CheckBox的尺寸
     *
     * 注意事项：
     *   1. 只有当itemWidth > 0时才执行更新
     *   2. 同时调整对齐方式和内边距
     *
     * 使用场景：
     *   当需要自定义通道项尺寸时，在setData()中指定width和height
     */
    public void updateChild() {
        // 如果itemWidth为0，不执行更新
        if (itemWidth == 0) return;
        // 遍历所有子视图
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            // 设置对齐方式：垂直居中，水平靠左
            checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            // 清除内边距
            checkBox.setPadding(0, 0, 0, 0);
            // 获取并修改布局参数
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) checkBox.getLayoutParams();
            layoutParams.width = itemWidth; // 设置宽度
            layoutParams.height = itemHeight; // 设置高度
            // 应用新的布局参数
            llMutiCheckChannel.getChildAt(i).setLayoutParams(layoutParams);
        }
    }

    /**
     * 取消所有选中状态
     * 功能：将所有CheckBox（包括"ALL"按钮）设置为未选中状态
     *
     * 使用场景：
     *   1. 重置选择状态
     *   2. 切换通道类型前清空选择
     *
     * 使用示例：
     *   choice.unCheckAll(); // 取消所有选中
     */
    public void unCheckAll() {
        // 遍历所有子视图
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            checkBox.setChecked(false); // 设置为未选中
        }
    }

    /**
     * 设置只读模式
     * 功能：控制通道选择器的只读状态，禁用或启用交互
     *
     * @param enabled 是否启用交互
     *                true - 启用交互，恢复正常状态
     *                false - 禁用交互，进入只读模式
     *
     * 只读模式行为：
     *   - 未选中的通道：禁用，显示为灰色
     *   - 已选中的通道：不可点击，但保持选中状态
     *
     * 恢复正常行为：
     *   - 所有通道恢复可用状态
     *   - 恢复原始颜色
     *   - "ALL"按钮恢复显示
     *
     * 使用示例：
     *   // 进入只读模式
     *   choice.setReadOnly(false);
     *   // 恢复正常状态
     *   choice.setReadOnly(true);
     */
    @SuppressLint("ResourceType") // 抑制资源ID检查警告
    public void setReadOnly(boolean enabled) {
        // 计算子视图数量：如果显示ALL按钮，则加1
        int length = showAll ? arrayString.length + 1 : arrayString.length;
        // 遍历所有子视图
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i); // 获取第i个CheckBox
            if(!enabled) { // 进入只读模式
                if (!checkBox.isChecked()) {
                    // 未选中的通道：设置为禁用状态，显示灰色
                    checkBox.setTextColor(context.getResources().getColor(R.color.textColorNewTopViewDisable));
                    checkBox.setEnabled(false); // 禁用交互
                } else {
                    // 已选中的通道：不可点击，但保持可用状态
                    checkBox.setClickable(false); // 禁止点击
                }
            }
            else { // 恢复正常状态
                // 恢复通道颜色
                checkBox.setTextColor(arrayColor[i]);
                // 启用交互
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                // 如果是"ALL"按钮，恢复特殊颜色
                if (showAll && i == length - 1) {
                    checkBox.setText("ALL"); // 设置文本
                    // 恢复ALL按钮颜色
                    checkBox.setTextColor(context.getResources().getColor(R.color.colorChCommon));
                }
            }
        }
    }
}
