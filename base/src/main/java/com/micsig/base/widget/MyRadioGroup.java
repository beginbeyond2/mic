package com.micsig.base.widget; // 自定义控件包

/**
 * @Description: 扩展RadioGroup，可设置最大行数，多了换列 // 原有注释保留
 * @Author: limh // 原有作者信息
 * @CreateDate: 2024/3/22 9:04 // 原有创建日期
 */

import android.content.Context; // Android上下文类
import android.content.res.TypedArray; // 类型化数组，用于读取自定义属性
import android.graphics.drawable.Drawable; // 可绘制对象类
import android.util.AttributeSet; // 属性集类，用于XML属性解析
import android.view.View; // Android视图基类
import android.widget.RadioGroup; // Android单选按钮组控件

import com.micsig.base.R; // 资源文件引用类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                             MyRadioGroup                                    │
 * │                          自定义单选按钮组                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   base模块 → widget子包 → 自定义控件                                         │
 * │   MHO系列示波器软件UI组件                                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 扩展Android原生RadioGroup，支持多行多列布局                             │
 * │   2. 支持设置最大行数，超过后自动换列                                         │
 * │   3. 自定义测量和布局逻辑，实现灵活的单选按钮排列                              │
 * │   4. 支持动态设置最大行数                                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────────┐   │
 * │   │                     继承扩展模式                                     │   │
 * │   │  ┌────────────────┐                                                 │   │
 * │   │  │   RadioGroup   │  Android原生单选按钮组                          │   │
 * │   │  └───────┬────────┘                                                 │   │
 * │   │          │ extends                                                  │   │
 * │   │  ┌───────▼────────┐                                                 │   │
 * │   │  │  MyRadioGroup  │  扩展多行多列布局                                │   │
 * │   │  │  ┌───────────┐ │                                                 │   │
 * │   │  │  │ maxRows   │ │  最大行数配置                                   │   │
 * │   │  │  ├───────────┤ │                                                 │   │
 * │   │  │  │ onMeasure │ │  重写测量逻辑                                   │   │
 * │   │  │  ├───────────┤ │                                                 │   │
 * │   │  │  │ onLayout  │ │  重写布局逻辑                                   │   │   │
 * │   │  │  └───────────┘ │                                                 │   │
 * │   │  └────────────────┘                                                 │   │
 * │   └─────────────────────────────────────────────────────────────────────┘   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【布局算法】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────────┐   │
 * │   │  假设maxRows=3，有6个RadioButton:                                    │   │
 * │   │  ┌──────┐                                                           │   │
 * │   │  │  1   │ 第1行                                                      │   │
 * │   │  ├──────┤                                                           │   │
 * │   │  │  2   │ 第2行                                                      │   │
 * │   │  ├──────┤                                                           │   │
 * │   │  │  3   │ 第3行                                                      │   │
 * │   │  └──────┴──────┐                                                    │   │
 * │   │          │  4   │ 第1行(新列)                                        │   │
 * │   │          ├──────┤                                                   │   │
 * │   │          │  5   │ 第2行                                              │   │
 * │   │          ├──────┤                                                   │   │
 * │   │          │  6   │ 第3行                                              │   │
 * │   │          └──────┘                                                   │   │
 * │   └─────────────────────────────────────────────────────────────────────┘   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   XML属性 → 构造函数解析 → maxRows配置                                       │
 * │        ↓                                                                     │
 * │   onMeasure() → 测量子View → 计算总尺寸                                      │
 * │        ↓                                                                     │
 * │   onLayout() → 按行列规则布局子View                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖:                                                                      │
 * │     - android.widget.RadioGroup: 父类，提供单选功能                          │
 * │     - android.content.Context: 上下文环境                                   │
 * │     - com.micsig.base.R: 自定义属性资源                                     │
 * │   被依赖:                                                                    │
 * │     - 各UI界面: 用于需要多行多列单选按钮的场景                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                  │
 * │   <!-- XML布局 -->                                                          │
 * │   <com.micsig.base.widget.MyRadioGroup                                     │
 * │       android:id="@+id/radio_group"                                        │
 * │       android:layout_width="wrap_content"                                  │
 * │       android:layout_height="wrap_content"                                 │
 * │       app:maxRows="3">                                                     │
 * │                                                                            │
 * │       <RadioButton android:text="选项1" />                                 │
 * │       <RadioButton android:text="选项2" />                                 │
 * │       <RadioButton android:text="选项3" />                                 │
 * │       <RadioButton android:text="选项4" />                                 │
 * │   </com.micsig.base.widget.MyRadioGroup>                                  │
 * │                                                                            │
 * │   // Java代码动态设置                                                       │
 * │   myRadioGroup.setMaxRows(4);                                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【注意事项】                                                                  │
 * │   1. 子View必须是RadioButton或其子类                                        │
 * │   2. maxRows默认值为Integer.MAX_VALUE(不限制)                               │
 * │   3. 调用setMaxRows()会触发重新布局                                         │
 * │   4. 支持margin和padding属性                                               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * @author limh
 * @version 1.0.0
 * @since 2024-03-22
 */
public class MyRadioGroup extends RadioGroup { // 继承Android原生RadioGroup

    /** 日志标签，用于标识当前类的日志输出 */ // 日志标签常量
    private static final String TAG = "MyRadioGroup"; // TAG = "MyRadioGroup"

    /** 最大行数，超过后换列显示，默认为1(横向显示) */ // 最大行数成员变量
    private int maxRows = 1;//默认1，即横向显示 // 初始化为1，表示横向排列

    /** 上下文对象，用于资源访问 */ // 上下文成员变量
    private Context context; // 保存Context引用

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 构造方法：单参数构造                                                      │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   用于代码中创建MyRadioGroup实例                                          │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param context 上下文环境                                              │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【实现逻辑】                                                              │
     * │   调用双参数构造方法，attrs传入null                                       │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    public MyRadioGroup(Context context) { // 单参数构造方法开始
        this(context, null); // 调用双参数构造方法
    } // 单参数构造方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 构造方法：双参数构造                                                      │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   用于XML布局文件中创建MyRadioGroup实例，解析自定义属性                     │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param context 上下文环境                                              │
     * │   @param attrs XML属性集                                                 │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【实现逻辑】                                                              │
     * │   1. 调用父类构造方法                                                    │
     * │   2. 保存Context引用                                                     │
     * │   3. 从XML属性中读取maxRows自定义属性                                     │
     * │   4. 回收TypedArray资源                                                 │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    public MyRadioGroup(Context context, AttributeSet attrs) { // 双参数构造方法开始
        super(context, attrs); // 调用父类构造方法
        this.context = context; // 保存Context引用
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyRadioGroup); // 获取自定义属性数组
        maxRows = ta.getInt(R.styleable.MyRadioGroup_maxRows, Integer.MAX_VALUE); // 读取maxRows属性，默认为最大值
        ta.recycle(); // 回收TypedArray资源，避免内存泄漏
    } // 双参数构造方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 方法：onMeasure - 测量控件尺寸                                           │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   重写测量方法，根据子View和maxRows计算控件的总尺寸                         │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param widthMeasureSpec 宽度测量规格                                   │
     * │   @param heightMeasureSpec 高度测量规格                                  │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【测量算法】                                                              │
     * │   1. 测量所有子View                                                      │
     * │   2. 遍历子View，按maxRows规则计算行列                                    │
     * │   3. 累计总宽度和总高度                                                  │
     * │   4. 加上padding值                                                      │
     * │   5. 设置最终测量尺寸                                                    │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    @Override // 重写父类方法
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // onMeasure方法开始
        int widthSize = MeasureSpec.getSize(widthMeasureSpec); // 获取宽度尺寸
        int widthMode = MeasureSpec.getMode(widthMeasureSpec); // 获取宽度模式
        int heightSize = MeasureSpec.getSize(heightMeasureSpec); // 获取高度尺寸
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); // 获取高度模式

        //调用ViewGroup的方法，测量子view // 测量所有子View
        measureChildren(widthMeasureSpec, heightMeasureSpec); // 调用父类方法测量子View

        int currentRow = 1;//当前行 // 当前行计数器，从1开始

        int maxLineWidth = 0;//最大列宽 // 记录单列最大宽度
        int maxLineHeight = 0;//最大行高 // 记录单行最大高度

        int totalWidth = 0;//累积宽 // 累计总宽度
        int totalHeight = 0;//累积高 // 累计总高度
        int perHeight = 0;//每列的高 // 当前列的累计高度

        int count = getChildCount(); // 获取子View数量
        //假设 widthMode和heightMode都是AT_MOST // 注释说明测量模式假设
        for (int i = 0; i < count; i++) { // 遍历所有子View
            View child = getChildAt(i); // 获取当前子View
//            if(child.getVisibility() != View.VISIBLE) continue; // 注释掉的可见性检查
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams(); // 获取子View的布局参数

            int deltaX = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;//当前宽 // 计算子View宽度(含margin)
            int deltaY = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;//当前高 // 计算子View高度(含margin)

            maxLineWidth = Math.max(maxLineWidth, deltaX);//更新最大列宽 // 更新最大列宽
            maxLineHeight = Math.max(maxLineHeight, deltaY);//更新最大行高 // 更新最大行高
            if (currentRow > maxRows) { //换列 // 当前行数超过最大行数，需要换列
                currentRow = 1; //重置行数 // 重置行计数器
                totalWidth += deltaX;//累加宽度 // 累加到总宽度
                perHeight = deltaY; // 重置列高度
            } else { // 不需要换列
                currentRow++; //当前行数 // 行计数器加1
                perHeight +=  deltaY; // 累加列高度
                totalHeight = Math.max(totalHeight, perHeight); // 更新最大总高度
                totalWidth = Math.max(totalWidth, deltaX); // 更新最大总宽度
            } // 换列判断结束
        } // 子View遍历结束

        //加上当前容器的padding值 // 添加容器padding
        totalWidth += getPaddingLeft() + getPaddingRight(); // 加上左右padding
        totalHeight += getPaddingTop() + getPaddingBottom(); // 加上上下padding
        setMeasuredDimension(totalWidth, totalHeight); // 设置最终测量尺寸

    } // onMeasure方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 方法：onLayout - 布局子View                                              │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   重写布局方法，根据maxRows规则对子View进行行列布局                         │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param changed 布局是否发生变化                                        │
     * │   @param l 左边界位置                                                    │
     * │   @param t 上边界位置                                                    │
     * │   @param r 右边界位置                                                    │
     * │   @param b 下边界位置                                                    │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【布局算法】                                                              │
     * │   1. 从左上角开始布局                                                    │
     * │   2. 每行从上到下排列                                                    │
     * │   3. 超过maxRows后换列                                                  │
     * │   4. 新列从顶部重新开始                                                  │
     * │   5. 记录每列最大宽度用于下一列定位                                       │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    @Override // 重写父类方法
    protected void onLayout(boolean changed, int l, int t, int r, int b) { // onLayout方法开始
        int count = getChildCount(); // 获取子View数量
        //pre为前面所有的child的相加后的位置 // 注释说明pre变量用途
        int preLeft = getPaddingLeft(); // 初始化左侧位置(考虑padding)
        int preTop = getPaddingTop(); // 初始化顶部位置(考虑padding)

        int maxHeight = 0;//记录每一行的最高值 // 记录每行最大高度
        int maxWidth = 0;//记录每一列的最宽值 // 记录每列最大宽度

        int currentRow = 1;//当前行 // 当前行计数器

        for (int i = 0; i < count; i++) { // 遍历所有子View
            View child = getChildAt(i); // 获取当前子View
            if (child.getVisibility() != View.VISIBLE) continue; // 跳过不可见的子View
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams(); // 获取布局参数
            int currentWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin; // 计算当前子View宽度
            int currentHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin; // 计算当前子View高度
            if (currentRow > maxRows) { //换列 // 当前行超过最大行数，换列
                currentRow = 1; //重置行数 // 重置行计数器
                preTop = getPaddingTop();//重置preTop // 重置顶部位置
                preLeft = preLeft + maxWidth;//选择child的width最大的作为设置 // 左移一列
                maxWidth = getChildAt(i).getMeasuredWidth() + params.leftMargin + params.rightMargin; // 更新当前列最大宽度
            } else { // 不需要换列
                currentRow++; // 行计数器加1
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + params.leftMargin + params.rightMargin); // 更新列最大宽度
            } // 换列判断结束

            //left坐标 // 计算子View的left坐标
            int left = preLeft + params.leftMargin; // left = 前一位置 + 左margin
            //top坐标 // 计算子View的top坐标
            int top = preTop + params.topMargin; // top = 前一位置 + 上margin
            int right = left + child.getMeasuredWidth(); // 计算right坐标
            int bottom = top + child.getMeasuredHeight(); // 计算bottom坐标

            //为子view布局 // 布局子View
            child.layout(left, top, right, bottom); // 调用子View的layout方法
            //计算布局结束后，preLeft的值 // 注释说明
//            preLeft += params.leftMargin + child.getMeasuredWidth() + params.rightMargin; // 注释掉的代码
            preTop += params.topMargin + child.getMeasuredHeight() + params.bottomMargin; // 更新下一个子View的top位置
        } // 子View遍历结束


    } // onLayout方法结束

//    @Override // 注释掉的方法
//    public void addView(View child) { // 注释掉的addView方法开始
//        if (child instanceof RadioButton) { // 注释掉的判断
//            radioButtons.add((RadioButton) child); // 注释掉的代码
//        } else { // 注释掉的else
//            throw new IllegalArgumentException("Child views must be instances of RadioButton"); // 注释掉的异常
//        } // 注释掉的判断结束
//        requestLayout(); // 注释掉的代码
//    } // 注释掉的方法结束
//
//    @Override // 注释掉的方法
//    public void removeView(View view) { // 注释掉的removeView方法开始
//        if (view instanceof RadioButton) { // 注释掉的判断
//            radioButtons.remove(view); // 注释掉的代码
//        } // 注释掉的判断结束
//        super.removeView(view); // 注释掉的代码
//        requestLayout(); // 注释掉的代码
//    } // 注释掉的方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 方法：setMaxRows - 设置最大行数                                          │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   动态设置最大行数，超过后自动换列                                         │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param maxRows 最大行数值                                              │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【副作用】                                                                │
     * │   调用requestLayout()触发重新测量和布局                                   │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    public void setMaxRows(int maxRows) { // setMaxRows方法开始
        this.maxRows = maxRows; // 设置新的最大行数
        requestLayout(); // 请求重新布局
    } // setMaxRows方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 方法：getMaxRows - 获取最大行数                                          │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   获取当前配置的最大行数                                                  │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值说明】                                                            │
     * │   @return 当前最大行数值                                                 │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    public int getMaxRows() { // getMaxRows方法开始
        return maxRows; // 返回最大行数
    } // getMaxRows方法结束

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │ 方法：setChildBackGround - 设置子View背景                                │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                              │
     * │   为子View设置不同位置的背景(顶部、中间、底部)                             │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                              │
     * │   @param drawableTop 顶部子View的背景                                    │
     * │   @param drawableMiddle 中间子View的背景                                 │
     * │   @param drawableBottom 底部子View的背景                                 │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 【注意事项】                                                              │
     * │   当前方法实现被注释，暂未启用                                            │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    public void setChildBackGround(Drawable drawableTop, Drawable drawableMiddle, Drawable drawableBottom) { // setChildBackGround方法开始
//        int childCount = getChildCount(); // 注释掉的代码
//        for (int i = 0; i < childCount; i++) { // 注释掉的循环
//            View child = getChildAt(i); // 注释掉的代码
//            if (1 % maxRows == 0) { // 注释掉的判断
//                child.setBackgroundDrawable(drawableTop); // 注释掉的代码
//            } else if (1 % maxRows == maxRows - 1 || i == childCount - 1) { // 注释掉的判断
//                child.setBackgroundDrawable(drawableBottom); // 注释掉的代码
//            } else { // 注释掉的else
//                child.setBackgroundDrawable(drawableMiddle); // 注释掉的代码
//            } // 注释掉的判断结束
//        } // 注释掉的循环结束
//        requestLayout(); // 注释掉的代码
    } // setChildBackGround方法结束
} // MyRadioGroup类结束
