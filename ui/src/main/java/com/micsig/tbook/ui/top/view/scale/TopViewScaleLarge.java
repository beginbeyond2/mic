package com.micsig.tbook.ui.top.view.scale; // 包名：时间刻度视图组件包

import android.content.Context; // 导入：Android上下文类，用于访问资源和服务
import android.graphics.Canvas; // 导入：画布类，用于绑定绘制操作
import android.graphics.Color; // 导入：颜色类，提供颜色常量和颜色操作方法
import android.graphics.Paint; // 导入：画笔类，定义绘制样式（颜色、线宽、字体等）
import android.graphics.Rect; // 导入：矩形类，用于文本测量和区域计算
import android.util.AttributeSet; // 导入：属性集类，用于XML属性解析
import android.view.MotionEvent; // 导入：触摸事件类，处理用户触摸交互
import android.view.View; // 导入：视图基类，所有UI组件的父类

import com.micsig.tbook.ui.R; // 导入：资源文件R类，访问应用资源

import java.text.DecimalFormat; // 导入：十进制格式化类，用于数值格式化
import java.text.DecimalFormatSymbols; // 导入：十进制格式化符号类，定义数字格式符号
import java.util.Locale; // 导入：地区类，用于本地化设置

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     TopViewScaleLarge - 时间刻度选择视图                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属模块：UI层 - 顶部视图 - 刻度选择组件                                    │
 * │   文件路径：ui/src/main/java/com/micsig/tbook/ui/top/view/scale/            │
 * │   设计模式：自定义View模式、观察者模式                                         │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 绘制时间刻度尺视图，支持从纳秒到秒的时间范围选择                          │
 * │   2. 提供触摸滑动交互，用户可通过滑动选择时间值                                │
 * │   3. 提供按键移动功能，支持左右步进调整时间值                                  │
 * │   4. 实现时间范围限制，确保选择值在有效范围内                                  │
 * │   5. 通过回调接口通知外部时间选择结果                                         │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌──────────────────────────────────────────────────────────────┐         │
 * │   │                    TopViewScaleLarge                        │         │
 * │   │  ┌────────────────────────────────────────────────────────┐  │         │
 * │   │  │              绘制层 (onDraw)                            │  │         │
 * │   │  │  - drawBg(): 绘制背景和边框                            │  │         │
 * │   │  │  - drawRuler(): 绘制刻度线和刻度标签                   │  │         │
 * │   │  │  - drawProgress(): 绘制选择进度条                      │  │         │
 * │   │  └────────────────────────────────────────────────────────┘  │         │
 * │   │  ┌────────────────────────────────────────────────────────┐  │         │
 * │   │  │              交互层 (onTouchEvent)                      │  │         │
 * │   │  │  - ACTION_MOVE: 实时更新选择位置                       │  │         │
 * │   │  │  - ACTION_UP: 对齐到刻度线并触发回调                   │  │         │
 * │   │  └────────────────────────────────────────────────────────┘  │         │
 * │   │  ┌────────────────────────────────────────────────────────┐  │         │
 * │   │  │              转换层 (Conversion)                        │  │         │
 * │   │  │  - getPxFromValue(): 时间值 → 像素位置                 │  │         │
 * │   │  │  - getValueFromPx(): 像素位置 → 时间值                 │  │         │
 * │   │  └────────────────────────────────────────────────────────┘  │         │
 * │   └──────────────────────────────────────────────────────────────┘         │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户触摸 → onTouchEvent → pointX更新 → onListener → 回调通知             │
 * │       ↓                                                                     │
 * │   invalidate → onDraw → 重绘刻度尺和进度条                                  │
 * │                                                                             │
 * │ 【刻度范围】                                                                 │
 * │   段1: 0-50ns (索引0-9)     段2: 1μs (索引10-18)                           │
 * │   段3: 10μs (索引19-27)     段4: 100μs (索引28-36)                         │
 * │   段5: 1ms (索引37-45)      段6: 10ms (索引46-54)                          │
 * │   段7: 100ms (索引55-63)    段8: 1s-10s (索引64-72)                        │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   - 继承: android.view.View                                                │
 * │   - 依赖: TopUtilScale (时间单位常量和转换工具)                              │
 * │   - 资源: R.color.bg_slip_backcolor, R.color.scaleDivider等                │
 * │   - 回调: OnRulerChangedListener (时间选择变化监听器)                       │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   TopViewScaleLarge scaleView = new TopViewScaleLarge(context);            │
 * │   scaleView.setTimeRange(1000L, 1000000000L); // 设置范围1μs到1s           │
 * │   scaleView.setOnRulerChangedListener((value, unit, item) -> {             │
 * │       Log.d("TAG", "Selected: " + value + " " + unit);                     │
 * │   });                                                                       │
 * │   scaleView.setValue(10.0, TopUtilScale.UNIT_MS, 1); // 设置当前值10ms     │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 刻度线间隔固定为twoLineInterval，共73个间隔点                          │
 * │   2. 选择位置会自动对齐到半刻度线（twoLineInterval/2）                       │
 * │   3. 时间范围限制通过minNs和maxNs控制，影响可滑动区域                        │
 * │   4. 支持外部调用moveLeftOneStep/moveRightOneStep进行步进调整               │
 * │                                                                             │
 * │ 【版本历史】                                                                 │
 * │   v1.0.0 - 2017/04/14 - yangj - 初始版本，实现基础时间刻度选择功能          │
 * │                                                                             │
 * │ @author yangj                                                               │
 * │ @version 1.0.0                                                              │
 * │ @since 2017/04/14                                                           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopViewScaleLarge extends View { // 继承View类，实现自定义时间刻度选择视图
    
    // ==================== 常量定义 ====================
    
    /**
     * 刻度名称数组
     * 用途：存储刻度尺上显示的标签文本
     * 索引对应关系：
     *   [0]="0"     - 起始点
     *   [1]="1μs"   - 第1个长刻度线
     *   [2]="10μs"  - 第2个长刻度线
     *   [3]="100μs" - 第3个长刻度线
     *   [4]="1ms"   - 第4个长刻度线
     *   [5]="10ms"  - 第5个长刻度线
     *   [6]="100ms" - 第6个长刻度线
     *   [7]="1s"    - 第7个长刻度线
     *   [8]="10s"   - 第8个长刻度线（终点）
     */
    private static final String[] RULERNAME = {"0", "1μs", "10μs", "100μs", "1ms", "10ms", "100ms", "1s", "10s"};
    
    // ==================== 成员变量 ====================
    
    /**
     * 上下文对象
     * 用途：访问应用资源和系统服务
     */
    private Context context;
    
    /**
     * 画笔对象
     * 用途：定义绘制样式（颜色、线宽、字体大小等）
     */
    private Paint paint;
    
    /**
     * 背景颜色
     * 用途：刻度尺背景填充色
     * 来源：R.color.bg_slip_backcolor
     */
    private int bgColor;
    
    /**
     * 刻度线颜色
     * 用途：刻度线和边框绘制颜色
     * 来源：R.color.scaleDivider
     */
    private int lineColor;
    
    /**
     * 文本颜色
     * 用途：刻度标签文字颜色
     * 来源：R.color.textColorTop
     */
    private int textColor;
    
    /**
     * 视图高度（像素）
     * 用途：存储测量后的视图高度，用于绘制计算
     */
    private int height;
    
    /**
     * 视图宽度（像素）
     * 用途：存储测量后的视图宽度，用于绘制计算
     */
    private int width;
    
    /**
     * 线条宽度（像素）
     * 用途：定义刻度线和边框的线条粗细
     * 默认值：1像素
     */
    private int strokeWidth;
    
    /**
     * 底部内边距（像素）
     * 用途：刻度标签与视图底部的间距
     * 默认值：10像素
     */
    private int bottomPadding;
    
    /**
     * 长刻度线高度（像素）
     * 用途：定义主要刻度线（每9格）的高度
     * 默认值：60像素
     */
    private int longLineHeight;
    
    /**
     * 中等刻度线高度（像素）
     * 用途：定义中等刻度线（每9格中间位置）的高度
     * 默认值：30像素
     */
    private int middleLineHeight;
    
    /**
     * 短刻度线高度（像素）
     * 用途：定义普通刻度线的高度
     * 默认值：20像素
     */
    private int shortLineHeight;
    
    /**
     * 两条刻度线之间的间隔（像素）
     * 用途：定义刻度线之间的水平间距
     * 默认值：11像素
     * 计算依据：总宽度 = twoLineInterval × 73
     */
    private float twoLineInterval;
    
    /**
     * 当前选择位置的X坐标（像素）
     * 用途：记录用户当前选择的时间位置，用于绘制进度条和计算时间值
     * 范围：0 到 width
     */
    private float pointX;
    
    /**
     * 最小时间值（纳秒）
     * 用途：限制可选择的最小时间值
     * 默认值：TopUtilScale.DEFAULT_MIN_TIME
     */
    private long minNs = TopUtilScale.DEFAULT_MIN_TIME;
    
    /**
     * 最大时间值（纳秒）
     * 用途：限制可选择的最大时间值
     * 默认值：TopUtilScale.DEFAULT_MAX_TIME
     */
    private long maxNs = TopUtilScale.DEFAULT_MAX_TIME;
    
    /**
     * 刻度变化监听器
     * 用途：当用户选择的时间值发生变化时，通过此接口通知外部
     */
    private OnRulerChangedListener onRulerChangedListener;
    
    // ==================== 接口定义 ====================
    
    /**
     * 刻度变化监听器接口
     * 用途：定义时间选择变化的回调接口
     * 
     * 使用场景：
     *   当用户通过触摸滑动或按键移动改变时间选择时，
     *   通过此接口将新的时间值通知给外部调用者
     */
    public interface OnRulerChangedListener {
        /**
         * 刻度值变化回调方法
         * 
         * @param value 当前选择的时间数值（不带单位）
         *              例如：10.5 表示 10.5ms（当unit为UNIT_MS时）
         * @param unit  时间单位字符串
         *              取值范围：TopUtilScale.UNIT_NS, UNIT_US, UNIT_MS, UNIT_S
         * @param item  刻度单位值，表示当前刻度段的单位步长
         *              例如：1表示每格1单位，10表示每格10单位，100表示每格100单位
         */
        void rulerChanged(double value, String unit, double item);
    }
    
    // ==================== Getter/Setter方法 ====================
    
    /**
     * 获取刻度变化监听器
     * 
     * @return 当前注册的刻度变化监听器，未注册时返回null
     */
    public OnRulerChangedListener getOnRulerChangedListener() {
        return onRulerChangedListener; // 返回当前监听器实例
    }
    
    /**
     * 设置刻度变化监听器
     * 
     * @param onRulerChangedListener 刻度变化监听器实例
     *                               传入null可移除监听器
     */
    public void setOnRulerChangedListener(OnRulerChangedListener onRulerChangedListener) {
        this.onRulerChangedListener = onRulerChangedListener; // 保存监听器引用
    }
    
    // ==================== 构造方法 ====================
    
    /**
     * 构造方法（单参数）
     * 用途：在代码中动态创建视图时使用
     * 
     * @param context 上下文对象，用于访问资源和系统服务
     */
    public TopViewScaleLarge(Context context) {
        this(context, null); // 调用双参数构造方法，attrs传null
    }
    
    /**
     * 构造方法（双参数）
     * 用途：在XML布局文件中使用时由系统调用
     * 
     * @param context 上下文对象
     * @param attrs XML属性集，包含布局文件中定义的属性
     */
    public TopViewScaleLarge(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法，defStyleAttr传0
    }
    
    /**
     * 构造方法（三参数）
     * 用途：完整的构造方法，支持自定义默认样式
     * 
     * @param context 上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewScaleLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        this.context = context; // 保存上下文引用
        initView(context, attrs, defStyleAttr); // 初始化视图
    }
    
    // ==================== 初始化方法 ====================
    
    /**
     * 初始化视图
     * 功能：设置视图的初始参数，包括颜色、尺寸和画笔属性
     * 
     * @param context 上下文对象，用于获取资源
     * @param attrs XML属性集（当前未使用）
     * @param defStyleAttr 默认样式属性（当前未使用）
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        // 初始化颜色资源
        bgColor = getResources().getColor(R.color.bg_slip_backcolor); // 获取背景颜色
        lineColor = getResources().getColor(R.color.scaleDivider); // 获取刻度线颜色
        textColor = getResources().getColor(R.color.textColorTop); // 获取文本颜色
        
        // 初始化尺寸参数
        strokeWidth = 1; // 线条宽度：1像素
        bottomPadding = 10; // 底部内边距：10像素
        longLineHeight = 60; // 长刻度线高度：60像素
        middleLineHeight = 30; // 中等刻度线高度：30像素
        shortLineHeight = 20; // 短刻度线高度：20像素
        twoLineInterval = 11f; // 刻度线间隔：11像素
        
        // 初始化画笔
        paint = new Paint(); // 创建画笔实例
        paint.setStrokeWidth(strokeWidth); // 设置线条宽度
        paint.setAntiAlias(true); // 启用抗锯齿，使绘制更平滑
        paint.setTextSize(20); // 设置文本大小：20像素
    }
    
    // ==================== 测量方法 ====================
    
    /**
     * 测量视图尺寸
     * 功能：根据测量规格计算视图的宽度和高度
     * 
     * @param widthMeasureSpec 宽度测量规格，包含模式和尺寸
     * @param heightMeasureSpec 高度测量规格，包含模式和尺寸
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 调用setMeasuredDimension设置测量后的宽高
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }
    
    /**
     * 测量视图宽度
     * 功能：根据测量规格计算视图宽度
     * 
     * 计算逻辑：
     *   默认宽度 = twoLineInterval × 73（共73个刻度间隔）
     *   EXACTLY模式：取默认宽度和测量尺寸的较大值
     *   AT_MOST模式：取默认宽度和测量尺寸的较小值
     * 
     * @param widthMeasureSpec 宽度测量规格
     * @return 测量后的宽度值（像素）
     */
    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec); // 获取测量模式
        int measureSize = MeasureSpec.getSize(widthMeasureSpec); // 获取测量尺寸
        int result = (int) (twoLineInterval * 73); // 计算默认宽度：11×73=803像素
        
        // 根据测量模式调整结果
        switch (measureMode) {
            case MeasureSpec.EXACTLY: // 精确模式：父容器指定了确切尺寸
                result = Math.max(result, measureSize); // 取较大值
                break;
            case MeasureSpec.AT_MOST: // 最大模式：父容器指定了最大尺寸
                result = Math.min(result, measureSize); // 取较小值
                break;
            // UNSPECIFIED模式：使用默认值result
        }
        
        width = result; // 保存宽度值
        return result; // 返回测量结果
    }
    
    /**
     * 测量视图高度
     * 功能：根据测量规格计算视图高度
     * 
     * 计算逻辑：
     *   默认高度 = longLineHeight × 2（长刻度线高度的两倍）
     *   EXACTLY模式：取默认高度和测量尺寸的较大值
     *   AT_MOST模式：取默认高度和测量尺寸的较小值
     * 
     * @param heightMeasureSpec 高度测量规格
     * @return 测量后的高度值（像素）
     */
    private int measureHeight(int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(heightMeasureSpec); // 获取测量模式
        int measureSize = MeasureSpec.getSize(heightMeasureSpec); // 获取测量尺寸
        int result = longLineHeight * 2; // 计算默认高度：60×2=120像素
        
        // 根据测量模式调整结果
        switch (measureMode) {
            case MeasureSpec.EXACTLY: // 精确模式：父容器指定了确切尺寸
                result = Math.max(result, measureSize); // 取较大值
                break;
            case MeasureSpec.AT_MOST: // 最大模式：父容器指定了最大尺寸
                result = Math.min(result, measureSize); // 取较小值
                break;
            // UNSPECIFIED模式：使用默认值result
        }
        
        height = result; // 保存高度值
        return result; // 返回测量结果
    }
    
    // ==================== 绘制方法 ====================
    
    /**
     * 绘制视图
     * 功能：按照背景、刻度、进度的顺序绘制视图内容
     * 
     * 绘制顺序：
     *   1. drawBg() - 绘制背景和边框
     *   2. drawRuler() - 绘制刻度线和标签
     *   3. drawProgress() - 绘制选择进度条
     * 
     * @param canvas 画布对象，用于执行绑定绘制操作
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); // 调用父类绘制方法
        drawBg(canvas); // 绘制背景
        drawRuler(canvas); // 绘制刻度
        drawProgress(canvas); // 绘制进度
    }
    
    /**
     * 绘制进度条
     * 功能：绘制从起始位置到当前选择位置的进度区域
     * 
     * 实现方式：
     *   使用半透明的文本颜色填充矩形区域
     *   矩形范围：从x=1到x=pointX
     * 
     * @param canvas 画布对象
     */
    private void drawProgress(Canvas canvas) {
        // 设置进度条颜色：文本颜色的60%透明度
        paint.setColor(Color.argb(0x99, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        // 绘制进度矩形：左边界1像素，上边界1像素，右边界为pointX（最小为2），下边界为height
        canvas.drawRect(1, 1, pointX == 0 ? 2 : pointX, height, paint);
    }
    
    /**
     * 绘制刻度尺
     * 功能：绘制刻度线和刻度标签
     * 
     * 刻度线类型：
     *   - 长刻度线（i%9==0且i!=0）：高度60像素，每9格一个
     *   - 中等刻度线（i%9==4）：高度30像素，位于长刻度线中间
     *   - 短刻度线（其他）：高度20像素，普通刻度线
     * 
     * 刻度标签：
     *   在长刻度线位置显示对应的时间单位标签
     *   起始点和终点标签单独处理
     * 
     * @param canvas 画布对象
     */
    private void drawRuler(Canvas canvas) {
        // 第一阶段：绘制刻度线
        paint.setColor(lineColor); // 设置刻度线颜色
        for (int i = 0; i < 72; i++) { // 遍历72个刻度位置（0-71）
            float x = twoLineInterval * (i + 1); // 计算当前刻度线的X坐标
            
            // 根据刻度线类型绘制不同高度的线
            if (i % 9 == 0 && i != 0) { // 长刻度线：每9格一个，跳过起始位置
                canvas.drawLine(x, 1, x, longLineHeight, paint); // 绘制长刻度线（高度60像素）
            } else if (i % 9 == 4) { // 中等刻度线：位于长刻度线中间
                canvas.drawLine(x, 1, x, middleLineHeight, paint); // 绘制中等刻度线（高度30像素）
            } else { // 短刻度线：普通刻度线
                canvas.drawLine(x, 1, x, shortLineHeight, paint); // 绘制短刻度线（高度20像素）
            }
        }
        
        // 第二阶段：绘制刻度标签
        paint.setColor(textColor); // 设置文本颜色
        int j = 0; // 标签索引，用于从RULERNAME数组获取标签文本
        
        for (int i = 0; i < 72; i++) { // 遍历72个刻度位置
            float x = twoLineInterval * (i + 1); // 计算当前刻度线的X坐标
            
            if (i % 9 == 0 && i != 0) { // 在长刻度线位置绘制标签
                String text = RULERNAME[++j]; // 获取标签文本，索引先自增
                // 绘制标签文本：水平居中对齐，垂直位置距离底部bottomPadding
                canvas.drawText(text, x - getTextWidth(text) / 2, height - bottomPadding, paint);
            }
        }
        
        // 绘制起始点标签"0"
        canvas.drawText(RULERNAME[0], bottomPadding, height - bottomPadding, paint);
        
        // 绘制终点标签（最后一个标签）
        String lastText = RULERNAME[RULERNAME.length - 1]; // 获取最后一个标签文本
        // 绘制终点标签：右对齐，距离右边界bottomPadding
        canvas.drawText(lastText, width - getTextWidth(lastText) - bottomPadding, height - bottomPadding, paint);
    }
    
    /**
     * 获取文本宽度
     * 功能：测量指定文本的像素宽度
     * 
     * 实现方式：
     *   使用Paint.getTextBounds()方法获取文本边界矩形
     *   返回矩形的宽度值
     * 
     * @param text 要测量的文本字符串
     * @return 文本的像素宽度
     */
    private int getTextWidth(String text) {
        Rect rect = new Rect(); // 创建矩形对象用于存储文本边界
        paint.getTextBounds(text, 0, text.length(), rect); // 测量文本边界
        int w = rect.width(); // 获取文本宽度
        int h = rect.height(); // 获取文本高度（当前未使用）
        return w; // 返回文本宽度
    }
    
    /**
     * 绘制背景
     * 功能：绘制视图背景和边框
     * 
     * 实现方式：
     *   1. 使用背景色填充整个视图区域
     *   2. 使用线条色绘制边框（1像素宽度的矩形边框）
     * 
     * @param canvas 画布对象
     */
    private void drawBg(Canvas canvas) {
        // 绘制背景填充
        paint.setColor(bgColor); // 设置背景颜色
        canvas.drawRect(0, 0, width, height, paint); // 填充整个视图区域
        
        // 绘制边框
        paint.setColor(lineColor); // 设置边框颜色
        paint.setStyle(Paint.Style.STROKE); // 设置画笔样式为描边（只绘制边框）
        canvas.drawRect(1, 1, width - 1, height - 1, paint); // 绘制1像素宽度的矩形边框
        paint.setStyle(Paint.Style.FILL); // 恢复画笔样式为填充（为后续绘制做准备）
    }
    
    // ==================== 触摸事件处理 ====================
    
    /**
     * 触摸事件处理
     * 功能：处理用户的触摸滑动操作，更新选择位置并触发回调
     * 
     * 事件处理逻辑：
     *   - ACTION_DOWN: 按下事件，不做处理
     *   - ACTION_MOVE: 移动事件，实时更新选择位置并触发回调
     *   - ACTION_UP: 抬起事件，对齐到最近的刻度线并触发最终回调
     * 
     * @param event 触摸事件对象，包含事件类型和坐标信息
     * @return true表示消费了触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) { // 根据事件类型分发处理
            case MotionEvent.ACTION_DOWN: // 按下事件
                break; // 不做处理
                
            case MotionEvent.ACTION_MOVE: // 移动事件
                pointX = checkRange(event.getX()); // 更新选择位置，并进行范围检查
                onListener(); // 触发回调通知
                invalidate(); // 请求重绘视图
                break;
                
            case MotionEvent.ACTION_UP: // 抬起事件
                // 对齐到最近的半刻度线位置（twoLineInterval/2为半刻度间隔）
                pointX = checkRange(event.getX() - event.getX() % (twoLineInterval / 2));
                onListener(); // 触发回调通知
                invalidate(); // 请求重绘视图
                break;
        }
        return true; // 返回true表示消费了触摸事件
    }
    
    // ==================== 回调通知方法 ====================
    
    /**
     * 触发刻度变化回调
     * 功能：根据当前选择位置计算时间值，并通过监听器回调通知
     * 
     * 位置到时间值的映射逻辑：
     *   刻度尺分为8个时间段，每段9个刻度间隔：
     *   - 段1 (px < 10×twoLineInterval): 0-50ns
     *   - 段2 (px < 19×twoLineInterval): 1μs范围
     *   - 段3 (px < 28×twoLineInterval): 10μs范围
     *   - 段4 (px < 37×twoLineInterval): 100μs范围
     *   - 段5 (px < 46×twoLineInterval): 1ms范围
     *   - 段6 (px < 55×twoLineInterval): 10ms范围
     *   - 段7 (px < 64×twoLineInterval): 100ms范围
     *   - 段8 (px <= 73×twoLineInterval): 1s-10s范围
     */
    private void onListener() {
        if (onRulerChangedListener != null) { // 检查监听器是否已注册
            // 对齐到半刻度线位置
            float px = pointX - pointX % (twoLineInterval / 2);
            
            // 根据位置范围计算对应的时间值
            if (px < 0) { // 位置小于0，无效范围
                // 不做处理
                
            } else if (px < twoLineInterval * 10) { // 段1: 0-50ns
                // 参数：像素位置, 起始刻度索引(0), 半刻度值(50ns), 起始值(0)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 0, 50, 0), TopUtilScale.UNIT_NS, 100);
                
            } else if (px < twoLineInterval * 19) { // 段2: 1μs范围
                // 参数：像素位置, 起始刻度索引(10), 半刻度值(0.5μs), 起始值(1μs)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 10, 0.5, 1), TopUtilScale.UNIT_US, 1);
                
            } else if (px < twoLineInterval * 28) { // 段3: 10μs范围
                // 参数：像素位置, 起始刻度索引(19), 半刻度值(5μs), 起始值(10μs)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 19, 5, 10), TopUtilScale.UNIT_US, 10);
                
            } else if (px < twoLineInterval * 37) { // 段4: 100μs范围
                // 参数：像素位置, 起始刻度索引(28), 半刻度值(50μs), 起始值(100μs)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 28, 50, 100), TopUtilScale.UNIT_US, 100);
                
            } else if (px < twoLineInterval * 46) { // 段5: 1ms范围
                // 参数：像素位置, 起始刻度索引(37), 半刻度值(0.5ms), 起始值(1ms)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 37, 0.5, 1), TopUtilScale.UNIT_MS, 1);
                
            } else if (px < twoLineInterval * 55) { // 段6: 10ms范围
                // 参数：像素位置, 起始刻度索引(46), 半刻度值(5ms), 起始值(10ms)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 46, 5, 10), TopUtilScale.UNIT_MS, 10);
                
            } else if (px < twoLineInterval * 64) { // 段7: 100ms范围
                // 参数：像素位置, 起始刻度索引(55), 半刻度值(50ms), 起始值(100ms)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 55, 50, 100), TopUtilScale.UNIT_MS, 100);
                
            } else if (px <= twoLineInterval * 73) { // 段8: 1s-10s范围
                // 参数：像素位置, 起始刻度索引(64), 半刻度值(0.5s), 起始值(1s)
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 64, 0.5, 1), TopUtilScale.UNIT_S, 1);
            }
        }
    }
    
    // ==================== 移动方法 ====================
    
    /**
     * 向左移动一步
     * 功能：将选择位置向左移动半个刻度间隔，并触发回调
     * 
     * 使用场景：
     *   用户按下左方向键时调用此方法
     *   每次移动距离为twoLineInterval/2（半个刻度间隔）
     */
    public void moveLeftOneStep() {
        pointX = checkRange(pointX - twoLineInterval / 2); // 向左移动半个刻度，并检查范围
        onListener(); // 触发回调通知
        invalidate(); // 请求重绘视图
    }
    
    /**
     * 向右移动一步
     * 功能：将选择位置向右移动半个刻度间隔，并触发回调
     * 
     * 使用场景：
     *   用户按下右方向键时调用此方法
     *   每次移动距离为twoLineInterval/2（半个刻度间隔）
     */
    public void moveRightOneStep() {
        pointX = checkRange(pointX + twoLineInterval / 2); // 向右移动半个刻度，并检查范围
        onListener(); // 触发回调通知
        invalidate(); // 请求重绘视图
    }
    
    // ==================== 设置方法 ====================
    
    /**
     * 设置时间范围
     * 功能：设置可选择的最小和最大时间值
     * 
     * 使用场景：
     *   根据设备能力或用户需求限制时间选择范围
     *   例如：限制在1μs到1s之间
     * 
     * @param minNs 最小时间值（纳秒）
     * @param maxNs 最大时间值（纳秒）
     */
    public void setTimeRange(long minNs, long maxNs) {
        this.minNs = minNs; // 保存最小时间值
        this.maxNs = maxNs; // 保存最大时间值
    }
    
    /**
     * 设置当前时间值
     * 功能：根据给定的时间值设置选择位置
     * 
     * 实现逻辑：
     *   根据时间单位和刻度单位值，确定所属的刻度段
     *   调用getPxFromValue计算对应的像素位置
     *   对齐到半刻度线并更新视图
     * 
     * @param curValue 当前时间数值（不带单位）
     * @param itemUnit 时间单位字符串（TopUtilScale.UNIT_NS/US/MS/S）
     * @param itemValue 刻度单位值（1/10/100）
     */
    public void setValue(double curValue, String itemUnit, double itemValue) {
        float px = 0; // 初始化像素位置
        
        // 根据时间单位和刻度单位值，确定所属刻度段并计算像素位置
        if (TopUtilScale.UNIT_NS.equals(itemUnit) && itemValue == 100) { // 纳秒段（100ns单位）
            px = getPxFromValue(curValue, 0, 50, 0); // 参数：值, 起始刻度索引, 半刻度值, 起始值
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 1) { // 微秒段（1μs单位）
            px = getPxFromValue(curValue, 1, 0.5, 10); // 参数：值, 起始值(1μs), 半刻度值(0.5μs), 起始刻度索引(10)
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 10) { // 微秒段（10μs单位）
            px = getPxFromValue(curValue, 10, 5, 19); // 参数：值, 起始值(10μs), 半刻度值(5μs), 起始刻度索引(19)
        } else if (TopUtilScale.UNIT_US.equals(itemUnit) && itemValue == 100) { // 微秒段（100μs单位）
            px = getPxFromValue(curValue, 100, 50, 28); // 参数：值, 起始值(100μs), 半刻度值(50μs), 起始刻度索引(28)
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 1) { // 毫秒段（1ms单位）
            px = getPxFromValue(curValue, 1, 0.5, 37); // 参数：值, 起始值(1ms), 半刻度值(0.5ms), 起始刻度索引(37)
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 10) { // 毫秒段（10ms单位）
            px = getPxFromValue(curValue, 10, 5, 46); // 参数：值, 起始值(10ms), 半刻度值(5ms), 起始刻度索引(46)
        } else if (TopUtilScale.UNIT_MS.equals(itemUnit) && itemValue == 100) { // 毫秒段（100ms单位）
            px = getPxFromValue(curValue, 100, 50, 55); // 参数：值, 起始值(100ms), 半刻度值(50ms), 起始刻度索引(55)
        } else if (TopUtilScale.UNIT_S.equals(itemUnit) && itemValue == 1) { // 秒段（1s单位）
            px = getPxFromValue(curValue, 1, 0.5, 64); // 参数：值, 起始值(1s), 半刻度值(0.5s), 起始刻度索引(64)
        }
        
        // 如果计算出了有效的像素位置，则更新选择位置
        if (px != 0) {
            pointX = checkRange(px - px % (twoLineInterval / 2)); // 对齐到半刻度线并检查范围
            invalidate(); // 请求重绘视图
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 检查并限制选择位置范围
     * 功能：确保选择位置在有效的时间范围内
     * 
     * 范围限制逻辑：
     *   1. 如果最小时间值小于1μs，计算对应的最小像素位置
     *   2. 如果最大时间值大于1s，计算对应的最大像素位置
     *   3. 将选择位置限制在计算出的范围内
     * 
     * @param willPointX 待检查的像素位置
     * @return 限制后的有效像素位置
     */
    private float checkRange(float willPointX) {
        // 检查最小时间限制
        if (minNs < TopUtilScale.TIME_US2NS) { // 最小值小于1μs
            // 计算最小时间值对应的像素位置
            // 公式：minNs / 50ns * 半刻度间隔
            long minPointX = (long) (minNs / 50 * (twoLineInterval / 2));
            pointX = Math.max(willPointX, minPointX); // 确保不小于最小位置
        }
        
        // 检查最大时间限制
        if (maxNs > TopUtilScale.TIME_S2NS) { // 最大值大于1s
            // 计算最大时间值对应的像素位置
            // 公式：(maxNs / 0.5s - 2 + 64*2) * 半刻度间隔
            // 解释：从1s刻度位置（索引64）开始计算超出部分
            long maxPointX = (long) ((maxNs / (TopUtilScale.TIME_S2NS / 2) - 2 + 64 * 2) * (twoLineInterval / 2));
            pointX = Math.min(pointX, maxPointX); // 确保不大于最大位置
        }
        
        return pointX; // 返回限制后的位置
    }
    
    /**
     * 时间值转换为像素位置
     * 功能：根据时间值计算对应的像素坐标
     * 
     * 转换公式：
     *   px = ((value - headValue) / halfItemValue + beforeHeadCount * 2) * (twoLineInterval / 2)
     * 
     * 参数说明：
     *   - value: 当前时间值
     *   - headValue: 当前刻度段的起始值（如1μs段的起始值为1）
     *   - halfItemValue: 半刻度值（如1μs段的半刻度值为0.5μs）
     *   - beforeHeadCount: 当前刻度段之前的刻度数量（如1μs段之前有10个刻度）
     * 
     * @param value 时间值
     * @param headValue 刻度段起始值
     * @param halfItemValue 半刻度值
     * @param beforeHeadCount 起始刻度索引
     * @return 对应的像素位置
     */
    private float getPxFromValue(double value, int headValue, double halfItemValue, int beforeHeadCount) {
        // 计算公式：((值 - 起始值) / 半刻度值 + 起始刻度索引*2) * 半刻度间隔
        return (float) (((value - headValue) / halfItemValue + beforeHeadCount * 2) * (twoLineInterval / 2));
    }
    
    /**
     * 十进制格式化器
     * 用途：格式化时间值，保留两位小数
     * 配置：使用中国地区格式，模式为"###0.00"
     */
    DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA));
    
    /**
     * 像素位置转换为时间值
     * 功能：根据像素坐标计算对应的时间值
     * 
     * 转换公式：
     *   value = (px / (twoLineInterval / 2) - beforeHeadCount * 2) * halfItemValue + headValue
     * 
     * 参数说明：
     *   - px: 像素位置
     *   - beforeHeadCount: 当前刻度段的起始刻度索引
     *   - halfItemValue: 半刻度值（每半个刻度代表的时间值）
     *   - headValue: 当前刻度段的起始时间值
     * 
     * @param px 像素位置
     * @param beforeHeadCount 起始刻度索引
     * @param haifItemValue 半刻度值（注意：参数名拼写错误，应为halfItemValue）
     * @param headValue 刻度段起始值
     * @return 对应的时间值（保留两位小数）
     */
    private double getValueFromPx(float px, int beforeHeadCount, double haifItemValue, int headValue) {
        // 计算公式：(像素位置 / 半刻度间隔 - 起始刻度索引*2) * 半刻度值 + 起始值
        // 使用DecimalFormat格式化结果，保留两位小数
        return Double.parseDouble(df.format((px / (twoLineInterval / 2) - beforeHeadCount * 2) * haifItemValue + headValue));
    }
}
