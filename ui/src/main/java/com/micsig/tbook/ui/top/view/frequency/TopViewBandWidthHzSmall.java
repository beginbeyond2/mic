package com.micsig.tbook.ui.top.view.frequency; // 包声明：示波器顶部视图频率选择组件 //

import android.content.Context; // 导入Android上下文类，用于获取资源和系统服务 //
import android.graphics.Bitmap; // 导入位图类，用于加载和绘制刻度图片 //
import android.graphics.BitmapFactory; // 导入位图工厂类，用于从资源解码位图 //
import android.graphics.Canvas; // 导入画布类，用于自定义视图绘制 //
import android.graphics.Color; // 导入颜色类，用于颜色常量定义 //
import android.graphics.Matrix; // 导入矩阵类，用于位图变换 //
import android.graphics.Paint; // 导入画笔类，用于绑定绘制样式 //
import android.graphics.Rect; // 导入矩形类，用于文本边界测量 //
import android.util.AttributeSet; // 导入属性集类，用于XML属性解析 //
import android.util.Log; // 导入日志类，用于调试输出 //
import android.view.MotionEvent; // 导入触摸事件类，用于处理用户交互 //
import android.view.VelocityTracker; // 导入速度追踪器类，用于追踪手指滑动速度 //
import android.view.View; // 导入视图基类，提供基础视图功能 //
import android.view.ViewConfiguration; // 导入视图配置类，用于获取系统标准值 //
import android.widget.Scroller; // 导入滚动器类，用于实现惯性滚动效果 //

import androidx.annotation.Nullable; // 导入可空注解，用于参数可空性标记 //

import com.micsig.base.Logger; // 导入日志工具类，用于统一日志输出 //
import com.micsig.tbook.ui.R; // 导入资源类，用于访问颜色、图片等资源 //
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器通用工具类，用于数值格式化 //

import java.text.DecimalFormat; // 导入十进制格式化类，用于数值格式化 //
import java.text.DecimalFormatSymbols; // 导入十进制格式化符号类，用于设置区域符号 //
import java.util.Locale; // 导入区域类，用于设置本地化参数 //

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                   TopViewBandWidthHzSmall - 小型带宽/频率选择视图              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   所属模块: ui.top.view.frequency                                            │
 * │   模块职责: 示波器顶部视图小型带宽/频率选择组件，提供紧凑的时间/频率选择界面      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                   │
 * │   1. 刻度尺绘制：使用位图资源绘制紧凑的刻度尺背景和刻度线                        │
 * │   2. 触摸交互：支持手指滑动选择时间/频率值，支持惯性滚动                         │
 * │   3. 单位自动切换：根据当前值自动在ns/μs/ms/s单位间切换                         │
 * │   4. 范围限制：确保选择值在最小/最大时间范围内                                  │
 * │   5. 回调通知：通过监听器接口通知外部值变化                                     │
 * │   6. 按键操作：支持外部调用左右移动一步                                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                   │
 * │   - 继承自Android View基类，实现自定义视图组件                                 │
 * │   - 采用位图绘制方式，使用预制的刻度图片提高绘制效率                            │
 * │   - 使用Scroller实现惯性滚动效果，提升用户体验                                  │
 * │   - 使用VelocityTracker追踪手指速度，判断是否触发惯性滚动                       │
 * │   - 通过OnRulerChangedListener回调接口与外部通信                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                   │
 * │   用户触摸滑动 → onTouchEvent → 计算移动偏移 → 更新curValue → onListener回调   │
 * │   外部设置值 → setValue → 计算偏移位置 → invalidate重绘                        │
 * │   惯性滚动 → computeScroll → 持续更新curValue → 重绘直到停止                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【与TopViewBandWidthHzLarge的区别】                                           │
 * │   - 尺寸更小：宽度500px，高度90px（Large约876px x 120px）                      │
 * │   - 使用位图绘制：使用预制刻度图片而非动态绘制刻度线                            │
 * │   - 显示范围更窄：显示当前值前后20个刻度，而非全范围显示                         │
 * │   - 适合空间有限的场景：如弹窗、小型面板等                                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                   │
 * │   - TopUtilBandWidthHz：时间单位常量和转换工具类                               │
 * │   - TBookUtil：示波器通用工具类，用于数值格式化                                │
 * │   - R.color.*：颜色资源定义                                                   │
 * │   - R.drawable.small_scale：小型刻度尺位图资源                                │
 * │   - R.drawable.indicator_small_scale：小型指示器位图资源                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                   │
 * │   // 创建视图实例                                                              │
 * │   TopViewBandWidthHzSmall view = new TopViewBandWidthHzSmall(context);       │
 * │                                                                              │
 * │   // 设置值变化监听器                                                          │
 * │   view.setOnRulerChangedListener((value, unit, item) -> {                   │
 * │       Log.d("TAG", "当前值: " + value + unit);                                │
 * │   });                                                                        │
 * │                                                                              │
 * │   // 设置时间范围限制                                                          │
 * │   view.setTimeRange(300, 10_000_000_000L); // 300ns ~ 10s                   │
 * │                                                                              │
 * │   // 设置当前值                                                               │
 * │   view.setValue(1.0, TopUtilBandWidthHz.UNIT_MS, 1);                        │
 * │                                                                              │
 * │   // 按键移动                                                                 │
 * │   view.moveLeftOneStep();  // 向左移动一步                                    │
 * │   view.moveRightOneStep(); // 向右移动一步                                    │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @author yangj
 * @version 1.0
 * @since 2017/6/19
 * @see TopViewBandWidthHzLarge 大型带宽/频率选择视图
 * @see TopUtilBandWidthHz 时间单位转换工具类
 */
public class TopViewBandWidthHzSmall extends View { // 继承View基类，实现自定义视图组件 //

    // ==================== 上下文与绘制资源 ==================== //

    /**
     * Android上下文对象
     * <p>用于获取资源、系统服务等</p>
     */
    private Context context; // Android上下文引用 //

    /**
     * 刻度尺位图资源
     * <p>预制的刻度尺背景图片，包含刻度线</p>
     */
    private Bitmap lineBitmap; // 刻度尺位图 //

    /**
     * 指示器位图资源
     * <p>显示在视图中央的三角形指示器图片</p>
     */
    private Bitmap indicatorBitmap; // 指示器位图 //

    /**
     * 画笔对象
     * <p>用于绑定绘制样式（颜色、线宽、字体大小等）</p>
     */
    private Paint paint; // 绘制画笔 //

    /**
     * 滚动器对象
     * <p>用于实现惯性滚动效果，在手指离开后继续滑动</p>
     */
    private Scroller scroller; // 惯性滚动器 //

    /**
     * 最大滑动速度
     * <p>从系统获取的最大滑动速度，用于限制惯性滚动速度</p>
     */
    private int maxVelocity; // 最大滑动速度 //

    /**
     * 十进制格式化器
     * <p>用于格式化数值，保留两位小数</p>
     */
    private DecimalFormat df2; // 数值格式化器 //

    /**
     * 速度追踪器
     * <p>用于追踪手指滑动速度，判断是否触发惯性滚动</p>
     */
    private VelocityTracker velocityTracker; // 速度追踪器 //

    // ==================== 时间范围限制 ==================== //

    /**
     * 最小时间值（纳秒）
     * <p>可选择的最小时间范围限制，默认300ns</p>
     */
    private long minNs = TopUtilBandWidthHz.DEFAULT_MIN_TIME; // 最小时间限制（纳秒） //

    /**
     * 最大时间值（纳秒）
     * <p>可选择的最大时间范围限制，默认10秒</p>
     */
    private long maxNs = TopUtilBandWidthHz.DEFAULT_MAX_TIME; // 最大时间限制（纳秒） //

    // ==================== 视图尺寸参数 ==================== //

    /**
     * 视图宽度（像素）
     * <p>固定宽度：500像素（12.5 * 40）</p>
     */
    private int width; // 视图宽度（像素） //

    /**
     * 视图高度（像素）
     * <p>固定高度：90像素</p>
     */
    private int height; // 视图高度（像素） //

    /**
     * 两条刻度线之间的像素距离
     * <p>每个刻度间隔12.5像素</p>
     */
    private float twoLinePx; // 刻度线间隔（像素） //

    // ==================== 颜色资源 ==================== //

    /**
     * 背景颜色
     * <p>从资源文件读取的背景色值</p>
     */
    private int bgColor; // 背景色值 //

    /**
     * 刻度线颜色
     * <p>用于绘制边框和刻度线的颜色值</p>
     */
    private int lineColor; // 刻度线颜色值 //

    /**
     * 文本颜色
     * <p>用于绘制刻度标签的颜色值</p>
     */
    private int textColor; // 文本颜色值 //

    /**
     * 指示器颜色
     * <p>中央指示器的颜色，默认红色</p>
     */
    private int indicatorColor; // 指示器颜色值 //

    // ==================== 绘制参数 ==================== //

    /**
     * 指示器长度
     * <p>三角形指示器的边长（像素）</p>
     */
    private int indicatorLength; // 指示器长度（像素） //

    /**
     * 文本内边距
     * <p>文本距离视图边缘的间距（像素）</p>
     */
    private int textPadding; // 文本内边距（像素） //

    // ==================== 当前状态值 ==================== //

    /**
     * 当前选择的时间值
     * <p>与curUnit配合表示完整时间，如curValue=1.0, curUnit="ms"表示1毫秒</p>
     */
    private double curValue; // 当前时间值 //

    /**
     * 当前刻度项的基准值
     * <p>每个刻度间隔对应的数值增量，如0.01表示每移动一个刻度增加0.01个单位</p>
     */
    private double itemValue; // 刻度项基准值 //

    /**
     * 当前时间单位
     * <p>可选值：UNIT_NS、UNIT_US、UNIT_MS、UNIT_S</p>
     */
    private String curUnit; // 当前时间单位 //

    /**
     * 移动偏移量（像素）
     * <p>当前触摸移动产生的像素偏移</p>
     */
    private float moveOffset; // 移动偏移量（像素） //

    /**
     * 触摸起始X坐标
     * <p>手指按下时的X坐标，用于计算总移动距离</p>
     */
    private float startX; // 触摸起始X坐标 //

    /**
     * 上一次触摸的X坐标
     * <p>用于计算单次移动的增量</p>
     */
    private float lastX; // 上一次触摸X坐标 //

    /**
     * 上一次的刻度偏移量
     * <p>用于计算刻度图的绘制位置，实现循环滚动效果</p>
     */
    float lastOffset = 0; // 上一次刻度偏移量 //

    /**
     * 滑动起始时的值
     * <p>手指按下时记录的当前值，用于计算滑动后的新值</p>
     */
    double startValue = 0; // 滑动起始值 //

    // ==================== 触摸事件处理 ==================== //

    /**
     * 触摸点ID
     * <p>用于多点触控时追踪特定触点</p>
     */
    private int pointerId; // 触摸点ID //

    /**
     * 刻度变化监听器
     * <p>当用户选择值变化时回调通知外部</p>
     */
    private OnRulerChangedListener onRulerChangedListener; // 值变化监听器 //

    // ==================== 接口定义 ==================== //

    /**
     * 刻度变化监听接口
     * <p>用于通知外部用户选择的值发生变化</p>
     *
     * @author yangj
     * @since 2017/6/19
     */
    public interface OnRulerChangedListener { // 定义值变化回调接口 //

        /**
         * 当刻度值变化时回调
         *
         * @param value   当前选择的数值字符串（如"1.500"表示1.5ms）
         * @param unit    当前单位（ns/μs/ms/s）
         * @param curItem 当前刻度项的基准值（如1/10/100）
         */
        void rulerChanged(String value, String unit, double curItem); // 值变化回调方法 //
    }

    // ==================== Getter/Setter方法 ==================== //

    /**
     * 获取刻度变化监听器
     *
     * @return 当前的监听器实例，可能为null
     */
    public OnRulerChangedListener getOnRulerChangedListener() { // 获取监听器方法 //
        return onRulerChangedListener; // 返回当前监听器 //
    }

    /**
     * 设置刻度变化监听器
     *
     * @param onRulerChangedListener 监听器实例，用于接收值变化通知
     */
    public void setOnRulerChangedListener(OnRulerChangedListener onRulerChangedListener) { // 设置监听器方法 //
        this.onRulerChangedListener = onRulerChangedListener; // 保存监听器引用 //
    }

    // ==================== 构造方法 ==================== //

    /**
     * 构造方法（单参数）
     * <p>用于代码中直接创建实例</p>
     *
     * @param context Android上下文对象
     */
    public TopViewBandWidthHzSmall(Context context) { // 单参数构造方法 //
        this(context, null); // 调用双参数构造方法 //
    }

    /**
     * 构造方法（双参数）
     * <p>用于XML布局中创建实例</p>
     *
     * @param context Android上下文对象
     * @param attrs   XML属性集
     */
    public TopViewBandWidthHzSmall(Context context, @Nullable AttributeSet attrs) { // 双参数构造方法 //
        this(context, attrs, 0); // 调用三参数构造方法 //
    }

    /**
     * 构造方法（三参数）
     * <p>完整的构造方法，执行初始化</p>
     *
     * @param context      Android上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewBandWidthHzSmall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 三参数构造方法 //
        super(context, attrs, defStyleAttr); // 调用父类构造方法 //
        this.context = context; // 保存上下文引用 //
        init(attrs, defStyleAttr); // 执行初始化 //
    }

    // ==================== 初始化方法 ==================== //

    /**
     * 初始化视图
     * <p>设置尺寸参数、颜色资源、加载位图、创建画笔和滚动器</p>
     *
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void init(AttributeSet attrs, int defStyleAttr) { // 初始化视图方法 //
        twoLinePx = 12.5f; // 设置刻度线间隔为12.5像素 //
        width = (int) (twoLinePx * 40); // 计算视图宽度：40个刻度间隔 = 500像素 //
        height = 90; // 设置视图高度为90像素 //
        bgColor = getResources().getColor(R.color.bg_slip_backcolor); // 从资源获取背景色 //
        lineColor = getResources().getColor(R.color.scaleDivider); // 从资源获取刻度线颜色 //
        textColor = getResources().getColor(R.color.textColor); // 从资源获取文本颜色 //
        indicatorColor = Color.RED; // 设置指示器颜色为红色 //
        indicatorLength = 10; // 设置指示器长度为10像素 //

        textPadding = 10; // 设置文本内边距为10像素 //
        curValue = 5.5; // 设置默认当前值为5.5 //
        itemValue = 0.01; // 设置默认刻度项基准值为0.01 //
        curUnit = TopUtilBandWidthHz.UNIT_US; // 设置默认单位为微秒 //
        moveOffset = 0; // 初始化移动偏移量为0 //

        lineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.small_scale); // 从资源解码刻度尺位图 //
        indicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_small_scale); // 从资源解码指示器位图 //
        Matrix matrix = new Matrix(); // 创建矩阵对象用于位图变换 //
        matrix.postScale(1, 1); // 设置缩放比例为1:1（不缩放） //
        lineBitmap = Bitmap.createBitmap(lineBitmap, 0, 0, lineBitmap.getWidth(), lineBitmap.getHeight(), matrix, true); // 应用矩阵变换创建新位图 //

        paint = new Paint(); // 创建画笔对象 //
        paint.setStrokeWidth(1); // 设置画笔线条宽度为1像素 //
        paint.setAntiAlias(true); // 启用抗锯齿，使绘制更平滑 //
        paint.setTextSize(20); // 设置文本大小为20像素 //

        scroller = new Scroller(context); // 创建滚动器对象 //
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity(); // 获取系统最大滑动速度 //
        df2 = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA)); // 创建数值格式化器，保留两位小数 //
    }

    // ==================== 测量方法 ==================== //

    /**
     * 测量视图尺寸
     * <p>设置固定的视图宽高</p>
     *
     * @param widthMeasureSpec  宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 测量视图尺寸方法 //
        setMeasuredDimension(width, height); // 设置固定的测量宽高 //
    }

    // ==================== 绘制方法 ==================== //

    /**
     * 绘制视图
     * <p>按顺序执行范围检查、绘制背景、绘制刻度尺、绘制指示器</p>
     *
     * @param canvas 画布对象，用于绑定绘制
     */
    @Override
    protected void onDraw(Canvas canvas) { // 绘制视图方法 //
        super.onDraw(canvas); // 调用父类绘制方法 //
        // 以下检查当前值是否超出范围，如果超出则修正并重置偏移 //
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && curValue < minNs) { // 检查ns单位是否小于最小值 //
            curValue = minNs; // 修正为最小值 //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS)) { // 检查μs单位是否小于最小值 //
            curValue = 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS; // 修正为最小值（转换为μs） //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS))) { // 检查μs单位（10μs量级）是否小于最小值 //
            curValue = 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS); // 修正为最小值 //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS))) { // 检查μs单位（100μs量级）是否小于最小值 //
            curValue = 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS); // 修正为最小值 //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
        }
        else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && curValue > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS)) { // 检查s单位是否大于最大值 //
            curValue = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS; // 修正为最大值（转换为s） //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
        }

        drawBg(canvas); // 绘制背景 //
        drawRuler(canvas); // 绘制刻度尺 //
        drawIndicator(canvas); // 绘制指示器 //
    }

    /**
     * 绘制指示器
     * <p>在视图中央绘制三角形指示器位图</p>
     *
     * @param canvas 画布对象
     */
    private void drawIndicator(Canvas canvas) { // 绘制指示器方法 //
        // 以下为原绘制三角形指示器的代码（已注释，现使用位图代替） //
        //        paint.setColor(indicatorColor); // 设置指示器颜色 //
        //        Path path = new Path(); // 创建路径对象 //
        //        path.moveTo(width / 2 + indicatorLength, 0); // 移动到右上角 //
        //        path.lineTo(width / 2 + indicatorLength, indicatorLength); // 画线到右中 //
        //        path.lineTo(width / 2, indicatorLength * 2); // 画线到中下 //
        //        path.lineTo(width / 2 - indicatorLength, indicatorLength); // 画线到左中 //
        //        path.lineTo(width / 2 - indicatorLength, 0); // 画线到左上 //
        //        path.close(); // 闭合路径 //
        //        canvas.drawPath(path, paint); // 绘制路径 //
        canvas.drawBitmap(indicatorBitmap, width / 2 - indicatorBitmap.getWidth() / 2, 1, paint); // 在视图中央绘制指示器位图 //
    }

    /**
     * 绘制刻度尺
     * <p>绘制刻度尺位图和左右两侧的刻度标签</p>
     *
     * @param canvas 画布对象
     */
    private void drawRuler(Canvas canvas) { // 绘制刻度尺方法 //
        changeUnit(); // 检查并自动切换单位 //

        String left; // 左侧刻度标签 //
        String right; // 右侧刻度标签 //
        int leftOff = 0; // 左侧标签偏移量 //
        int rightOff = 0; // 右侧标签偏移量 //
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS)) { // 当前单位为纳秒 //
            left = ((int) (curValue - itemValue * 20)) + curUnit; // 计算左侧标签值（当前值减20个刻度） //
            right = ((int) (curValue + itemValue * 20)) + curUnit; // 计算右侧标签值（当前值加20个刻度） //
            if (((int) (curValue - itemValue * 20)) < 0) { // 如果左侧值小于0 //
                Logger.i("drawRuler,zeroOff:" + (int) (curValue - itemValue * 20) + "," + lastOffset); // 输出调试日志 //
                leftOff = (int) (((int) (itemValue * 20 - curValue)) * twoLinePx); // 计算左侧偏移量 //
                left = 0 + curUnit; // 左侧标签设为0 //
                canvas.drawBitmap(lineBitmap, leftOff, 1, paint); // 绘制刻度尺位图（带偏移） //
                leftOff = leftOff - getTextWidth(left) / 2; // 调整左侧标签位置使其居中 //
            } else { // 左侧值大于等于0 //
                Logger.i("drawRuler,zeroUnOff:" + (int) (curValue - itemValue * 20) + "," + lastOffset); // 输出调试日志 //
                canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint); // 绘制刻度尺位图（使用偏移实现滚动效果） //
            }
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S)) { // 当前单位为秒 //
            left = TBookUtil.getD3FromD(curValue - itemValue * 20) + curUnit; // 计算左侧标签值（格式化为3位小数） //
            double maxS = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS; // 计算最大秒值 //
            if (curValue + itemValue * 20 < maxS) { // 右侧值未超过最大值 //
                right = TBookUtil.getD3FromD(curValue + itemValue * 20) + curUnit; // 计算右侧标签值 //
                canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint); // 绘制刻度尺位图 //
            } else { // 右侧值超过最大值 //
                double rightLineValue = curValue + itemValue * 20; // 计算右侧刻度值 //
                int offPx = (int) ((rightLineValue - maxS) / itemValue * twoLinePx+1); // 计算右侧偏移量 //
                right = TBookUtil.getD3FromD(maxS) + "s"; // 右侧标签设为最大值 //
                rightOff = -offPx; // 设置右侧标签偏移 //
                canvas.drawBitmap(lineBitmap, -width * 2 - offPx, 1, paint); // 绘制刻度尺位图（带偏移） //
            }
        } else { // 当前单位为μs或ms //
            left = TBookUtil.getD3FromD(curValue - itemValue * 20) + curUnit; // 计算左侧标签值 //
            right = TBookUtil.getD3FromD(curValue + itemValue * 20) + curUnit; // 计算右侧标签值 //
            canvas.drawBitmap(lineBitmap, -width + lastOffset, 1, paint); // 绘制刻度尺位图 //
        }

        paint.setColor(textColor); // 设置文本颜色 //
        left = TopUtilBandWidthHz.getHzFromS(left); // 将左侧时间标签转换为频率标签 //
        right = TopUtilBandWidthHz.getHzFromS(right); // 将右侧时间标签转换为频率标签 //
        canvas.drawText(left, textPadding + leftOff, height - textPadding, paint); // 绘制左侧标签 //
        canvas.drawText(right, width - textPadding - getTextWidth(right) + rightOff, height - textPadding, paint); // 绘制右侧标签 //
    }

    /**
     * 绘制背景
     * <p>绘制背景色和边框</p>
     *
     * @param canvas 画布对象
     */
    private void drawBg(Canvas canvas) { // 绘制背景方法 //
        paint.setColor(bgColor); // 设置背景颜色 //
        canvas.drawRect(0, 0, width, height, paint); // 绘制背景矩形 //
        paint.setColor(lineColor); // 设置边框颜色 //
        paint.setStyle(Paint.Style.STROKE); // 设置画笔样式为描边 //
        canvas.drawRect(1, 1, width - 1, height - 1, paint); // 绘制边框（留1像素边距） //
        paint.setStyle(Paint.Style.FILL); // 恢复画笔样式为填充 //
    }

    // ==================== 触摸事件处理 ==================== //

    /**
     * 处理触摸事件
     * <p>响应手指按下、移动、抬起事件，实现滑动选择和惯性滚动</p>
     *
     * @param event 触摸事件对象
     * @return true表示事件已处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 触摸事件处理方法 //
        if (null == velocityTracker) { // 检查速度追踪器是否为空 //
            velocityTracker = VelocityTracker.obtain(); // 创建速度追踪器 //
        }
        velocityTracker.addMovement(event); // 将当前事件添加到速度追踪器 //
        final VelocityTracker verTracker = velocityTracker; // 保存速度追踪器引用 //
        switch (event.getAction()) { // 根据动作类型处理 //
            case MotionEvent.ACTION_DOWN: // 手指按下事件 //
                startValue = curValue; // 记录按下时的当前值 //
                startX = event.getX(); // 记录按下时的X坐标 //
                lastX = event.getX(); // 记录上次触摸位置 //
                //获取第一个触点 //
                pointerId = event.getPointerId(0); // 获取触点ID //
                break; // 跳出switch //
            case MotionEvent.ACTION_MOVE: // 手指移动事件 //
                moveOffset = event.getX() - lastX; // 计算本次移动的偏移量 //
                lastX = event.getX(); // 更新上次触摸位置 //
                if (!checkCanMove()) { // 检查是否可以移动（是否超出范围） //
                    return true; // 不能移动，直接返回 //
                }
                curValue = getCurValueFromMoveOffset(event.getX() - startX); // 根据总移动距离计算新值 //
                lastOffset = getLastOffsetFromMoveOffset(moveOffset); // 更新刻度偏移量 //
                invalidate(); // 请求重绘 //
                onListener(); // 触发回调通知 //
                break; // 跳出switch //
            case MotionEvent.ACTION_UP: // 手指抬起事件 //
                if (!checkCanMove()) { // 检查是否可以移动 //
                    return true; // 不能移动，直接返回 //
                }
                verTracker.computeCurrentVelocity(1000, maxVelocity); // 计算当前滑动速度 //
                float velocityX = verTracker.getXVelocity(pointerId); // 获取X方向速度 //
                if (Math.abs(velocityX) > 100) { // 手指离开时大于一定速度的才会发生惯性滑动 //
                    velocityX = velocityX > 1000 ? 1000 : velocityX; // 限制最大正向速度为1000 //
                    velocityX = velocityX < -1000 ? -1000 : velocityX; // 限制最大负向速度为-1000 //
                    startValue = curValue; // 记录惯性滚动起始值 //
                    scroller.fling((int) lastX, (int) event.getY(), (int) velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0); // 启动惯性滚动 //
                } else { // 速度不够，不触发惯性滚动 //
                    drawLastState(); // 绘制最终状态 //
                }
                releaseVelocityTracker(); // 释放速度追踪器 //
                break; // 跳出switch //
            case MotionEvent.ACTION_CANCEL: // 触摸取消事件 //
                releaseVelocityTracker(); // 释放速度追踪器 //
                break; // 跳出switch //
            default: // 其他事件 //
                return super.onTouchEvent(event); // 调用父类处理 //
        }
        return true; // 返回true表示事件已处理 //
    }

    /**
     * 是否为惯性滚动的最后一帧
     * <p>用于判断惯性滚动是否结束</p>
     */
    boolean isLast = false; // 惯性滚动结束标志 //

    /**
     * 计算滚动
     * <p>在惯性滚动过程中持续更新值和位置</p>
     */
    @Override
    public void computeScroll() { // 计算滚动方法 //
        if (scroller.computeScrollOffset()) { // 检查滚动是否还在进行 //
            isLast = true; // 标记为惯性滚动中 //
            moveOffset = scroller.getCurrX() - scroller.getStartX(); // 计算当前滚动偏移 //
            curValue = getCurValueFromMoveOffset(moveOffset); // 根据偏移计算新值 //
            lastOffset = getLastOffsetFromMoveOffset(moveOffset); // 更新刻度偏移量 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调通知 //
        } else { // 滚动结束 //
            if (isLast) { // 如果是惯性滚动的最后一帧 //
                isLast = false; // 重置标志 //
                drawLastState(); // 绘制最终状态 //
            }
        }
        super.computeScroll(); // 调用父类方法 //
    }

    /**
     * 检查是否可以继续移动
     * <p>检查当前值是否超出范围限制</p>
     *
     * @return true表示可以继续移动，false表示已到达边界
     */
    private boolean checkCanMove() { // 检查是否可移动方法 //
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && curValue < minNs && moveOffset > 0) { // ns单位，小于最小值且向右移动 //
            curValue = minNs; // 修正为最小值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS) && moveOffset > 0) { // μs单位（1μs量级），小于最小值且向右移动 //
            curValue = 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS; // 修正为最小值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) { // μs单位（10μs量级），小于最小值且向右移动 //
            curValue = 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS); // 修正为最小值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        } else if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && curValue < (1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) { // μs单位（100μs量级），小于最小值且向右移动 //
            curValue = 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS); // 修正为最小值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        }else if (curUnit.equals(TopUtilBandWidthHz.UNIT_MS) && curValue < (1.0 * minNs / (1000 * TopUtilBandWidthHz.TIME_US2NS)) && moveOffset > 0) { // ms单位，小于最小值且向右移动 //
            curValue = 1.0 * minNs / (1000 * TopUtilBandWidthHz.TIME_US2NS); // 修正为最小值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        }
        else if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && curValue > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS) && moveOffset < 0) { // s单位，大于最大值且向左移动 //
            curValue = 1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS; // 修正为最大值 //
            moveOffset = 0; // 重置偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
            return false; // 返回不可移动 //
        } else { // 在有效范围内 //
            return true; // 返回可移动 //
        }
    }

    /**
     * 绘制最终状态
     * <p>在滑动或惯性滚动结束后，重置偏移并重绘</p>
     */
    private void drawLastState() { // 绘制最终状态方法 //
        if (checkCanMove()) { // 再次检查是否在有效范围内 //
            moveOffset = 0; // 重置移动偏移 //
            lastOffset = getLastOffsetFromCurValue(); // 根据当前值重新计算刻度偏移 //
            invalidate(); // 请求重绘 //
            onListener(); // 触发回调 //
        }
    }

    /**
     * 根据移动偏移计算当前值
     * <p>将像素偏移转换为时间值变化</p>
     *
     * @param moveOffset 移动偏移量（像素）
     * @return 计算后的当前时间值
     */
    private double getCurValueFromMoveOffset(float moveOffset) { // 根据偏移计算当前值方法 //
        curValue = startValue + (-moveOffset / twoLinePx * itemValue); // 计算新值：起始值 + 偏移量对应的值变化 //
        curValue = Double.parseDouble(df2.format(curValue)); // 格式化为两位小数 //
        //        Logger.i("getCurValueFromMoveOffset:" + moveOffset + "," + itemValue + "," + curValue); // 调试日志（已注释） //
        return curValue; // 返回计算后的值 //
    }

    /**
     * 根据移动偏移计算刻度偏移量
     * <p>实现刻度图的循环滚动效果</p>
     *
     * @param moveOffset 移动偏移量（像素）
     * @return 计算后的刻度偏移量
     */
    private float getLastOffsetFromMoveOffset(float moveOffset) { // 根据偏移计算刻度偏移方法 //
        lastOffset += moveOffset; // 累加移动偏移 //
        while (lastOffset >= twoLinePx * 5) { // 如果偏移超过5个刻度（正向） //
            lastOffset -= (twoLinePx * 10); // 回退10个刻度，实现循环 //
        }
        while (lastOffset <= -twoLinePx * 5) { // 如果偏移超过5个刻度（负向） //
            lastOffset += (twoLinePx * 10); // 前进10个刻度，实现循环 //
        }
        return lastOffset; // 返回计算后的偏移 //
    }

    /**
     * 根据当前值计算刻度偏移量
     * <p>用于设置值时计算刻度图的绘制位置</p>
     *
     * @return 刻度偏移量（像素）
     */
    private int getLastOffsetFromCurValue() { // 根据当前值计算刻度偏移方法 //
        int curValue = (int) (this.curValue * 100); // 将当前值乘以100转换为整数 //
        int itemValue = (int) (this.itemValue * 100); // 将刻度项值乘以100转换为整数 //
        int offsetNumber = curValue % (itemValue * 10) / itemValue; // 计算在10个刻度中的位置 //
        return (int) (-offsetNumber * twoLinePx); // 转换为像素偏移 //
    }

    /**
     * 释放速度追踪器
     * <p>清理并回收速度追踪器资源</p>
     */
    private void releaseVelocityTracker() { // 释放速度追踪器方法 //
        if (null != velocityTracker) { // 检查是否非空 //
            velocityTracker.clear(); // 清除追踪数据 //
            velocityTracker.recycle(); // 回收资源 //
            velocityTracker = null; // 置空引用 //
        }
    }

    /**
     * 触发监听器回调
     * <p>通知外部当前值发生变化</p>
     */
    private void onListener() { // 触发监听器回调方法 //
        if (onRulerChangedListener != null) { // 检查监听器是否存在 //
            if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS)) { // ns单位使用整数显示 //
                onRulerChangedListener.rulerChanged(String.valueOf((int) curValue), curUnit, itemValue); // 回调整数值 //
            } else { // 其他单位使用小数显示 //
                onRulerChangedListener.rulerChanged(TBookUtil.getD3FromD(curValue), curUnit, itemValue); // 回调格式化值 //
            }
        }
    }

    // ==================== 公共API方法 ==================== //

    /**
     * 设置时间范围
     * <p>限制用户可选择的时间范围</p>
     *
     * @param minNs 最小时间值（纳秒）
     * @param maxNs 最大时间值（纳秒）
     */
    public void setTimeRange(long minNs, long maxNs) { // 设置时间范围方法 //
        this.minNs = minNs; // 保存最小时间限制 //
        this.maxNs = maxNs; // 保存最大时间限制 //
    }

    /**
     * 设置当前值
     * <p>根据给定的值和单位设置选择位置</p>
     *
     * @param curNumber 当前时间数值
     * @param curUnit   当前时间单位（ns/μs/ms/s）
     * @param curItem   当前刻度项基准值（如0.01/0.1/1/10/100）
     */
    public void setValue(double curNumber, String curUnit, double curItem) { // 设置当前值方法 //
        this.curValue = curNumber; // 设置当前时间值 //
        this.curUnit = curUnit; // 设置当前时间单位 //
        this.itemValue = curItem; // 设置刻度项基准值 //
        moveOffset = 0; // 重置移动偏移 //
        lastOffset = getLastOffsetFromCurValue(); // 计算刻度偏移 //
        invalidate(); // 请求重绘 //
    }

    /**
     * 向左移动一步
     * <p>将选择值减少一个刻度项</p>
     */
    public void moveLeftOneStep() { // 向左移动一步方法 //
        // 检查是否已到达最小值边界 //
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_NS) && (curValue - itemValue) < minNs
        && minNs < TopUtilBandWidthHz.TIME_US2NS) { // ns单位，减一步后小于最小值且最小值小于1μs //
            return; // 不执行移动 //
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / TopUtilBandWidthHz.TIME_US2NS
                && (minNs > TopUtilBandWidthHz.TIME_US2NS && minNs < 10 * TopUtilBandWidthHz.TIME_US2NS)) { // μs单位（1μs量级），减一步后小于最小值 //
            return; // 不执行移动 //
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / (10 * TopUtilBandWidthHz.TIME_US2NS)
                && (minNs > 10 * TopUtilBandWidthHz.TIME_US2NS && minNs < 100 * TopUtilBandWidthHz.TIME_US2NS)) { // μs单位（10μs量级），减一步后小于最小值 //
            return; // 不执行移动 //
        }
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_US) && (curValue - itemValue) < 1.0 * minNs / (100 * TopUtilBandWidthHz.TIME_US2NS)
                && (minNs > 100 * TopUtilBandWidthHz.TIME_US2NS && minNs < TopUtilBandWidthHz.TIME_MS2NS)) { // μs单位（100μs量级），减一步后小于最小值 //
            return; // 不执行移动 //
        }
        curValue = curValue - itemValue; // 减少一个刻度项 //
        moveOffset = 0; // 重置移动偏移 //
        lastOffset = getLastOffsetFromCurValue(); // 计算刻度偏移 //
        changeUnit(); // 检查并切换单位 //
        invalidate(); // 请求重绘 //
        onListener(); // 触发回调 //
    }

    /**
     * 向右移动一步
     * <p>将选择值增加一个刻度项</p>
     */
    public void moveRightOneStep() { // 向右移动一步方法 //
        // 检查是否已到达最大值边界 //
        if (curUnit.equals(TopUtilBandWidthHz.UNIT_S) && (curValue + itemValue) > (1.0 * maxNs / TopUtilBandWidthHz.TIME_S2NS)) { // s单位，加一步后大于最大值 //
            return; // 不执行移动 //
        }
        curValue = curValue + itemValue; // 增加一个刻度项 //
        moveOffset = 0; // 重置移动偏移 //
        lastOffset = getLastOffsetFromCurValue(); // 计算刻度偏移 //
        changeUnit(); // 检查并切换单位 //
        invalidate(); // 请求重绘 //
        onListener(); // 触发回调 //
    }

    // ==================== 单位切换方法 ==================== //

    /**
     * 改变当前的数据单位
     * <p>根据当前值自动选择合适的单位，实现单位自动切换</p>
     */
    private void changeUnit() { // 改变单位方法 //
        long ns = TopUtilBandWidthHz.getNSFromValue(curValue + curUnit); // 将当前值转换为纳秒 //
        TopUtilBandWidthHz.ScaleValue scaleValue = new TopUtilBandWidthHz().createScaleValue(); // 创建ScaleValue对象 //
        TopUtilBandWidthHz.getValueFromNS(ns, scaleValue); // 根据纳秒值获取合适的单位和值 //
        if (itemValue != scaleValue.itemValue / 100) { // 检查刻度项值是否变化（需要切换单位） //
            this.curValue = scaleValue.value; // 更新当前值 //
            this.curUnit = scaleValue.itemUnit; // 更新当前单位 //
            this.itemValue = scaleValue.itemValue / 100; // 更新刻度项值（除以100是因为Small视图使用更小的步长） //
            startValue = curValue; // 更新起始值 //
        }
    }

    // ==================== 辅助方法 ==================== //

    /**
     * 获取该字符串的宽度（像素）
     * <p>使用画笔测量文本的边界宽度</p>
     *
     * @param text 要测量的文本
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) { // 获取文本宽度方法 //
        Rect rect = new Rect(); // 创建矩形对象用于存储边界 //
        paint.getTextBounds(text, 0, text.length(), rect); // 获取文本边界 //
        int w = rect.width(); // 获取边界宽度 //
        int h = rect.height(); // 获取边界高度（未使用） //
        return w; // 返回文本宽度 //
    }
} // 类结束 //
