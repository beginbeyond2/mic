package com.micsig.tbook.ui.top.view.frequency; // 定义包路径，属于顶部视图频率选择模块

import android.content.Context; // 导入Android上下文类，用于获取资源和系统服务
import android.graphics.Canvas; // 导入画布类，用于自定义绘制
import android.graphics.Color; // 导入颜色类，用于颜色处理
import android.graphics.Paint; // 导入画笔类，用于绑定绘制样式
import android.graphics.Rect; // 导入矩形类，用于文本边界测量
import android.util.AttributeSet; // 导入属性集类，用于XML属性解析
import android.util.Log; // 导入日志类，用于调试输出
import android.view.MotionEvent; // 导入触摸事件类，用于处理用户交互
import android.view.View; // 导入视图基类，提供基础视图功能

import com.micsig.tbook.ui.R; // 导入资源类，用于访问颜色等资源

import java.text.DecimalFormat; // 导入十进制格式化类，用于数值格式化
import java.text.DecimalFormatSymbols; // 导入十进制格式化符号类，用于设置区域符号
import java.util.Locale; // 导入区域类，用于设置本地化参数

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                              类文档说明                                        │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：示波器UI模块 - 顶部视图 - 频率/带宽选择组件                           │
 * │ 文件名称：TopViewBandWidthHzLarge.java                                        │
 * │ 创建时间：2017/4/14                                                           │
 * │ 创建作者：yangj                                                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                      │
 * │   1. 实现自定义View绘制带宽/频率选择标尺                                         │
 * │   2. 支持触摸滑动选择时间/频率值                                                 │
 * │   3. 提供时间范围限制功能                                                        │
 * │   4. 通过回调接口通知外部选择结果                                                 │
 * │   5. 支持按键左右移动选择                                                        │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                      │
 * │   - 继承自Android View基类，实现自定义视图                                       │
 * │   - 采用MVC模式，View负责绘制和交互，Controller通过回调接口实现                  │
 * │   - 使用刻度尺模型表示频率范围（10Hz ~ 1GHz）                                    │
 * │   - 支持多单位切换（ns/us/ms/s）                                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                      │
 * │   用户触摸/按键 → onTouchEvent/moveXxx → 计算新位置 → onListener回调           │
 * │   外部设置值 → setValue → 计算像素位置 → invalidate重绘                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                      │
 * │   - TopUtilBandWidthHz：时间单位常量和转换工具类                                │
 * │   - R.color.*：颜色资源定义                                                     │
 * │   - OnRulerChangedListener：值变化回调接口                                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                      │
 * │   TopViewBandWidthHzLarge view = new TopViewBandWidthHzLarge(context);        │
 * │   view.setOnRulerChangedListener((value, unit, item) -> {                     │
 * │       // 处理频率变化                                                           │
 * │   });                                                                          │
 * │   view.setTimeRange(minNs, maxNs); // 设置时间范围限制                          │
 * │   view.setValue(1.0, TopUtilBandWidthHz.UNIT_MS, 1); // 设置当前值             │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author yangj
 * @version 1.0
 * @since 2017/4/14
 */
public class TopViewBandWidthHzLarge extends View { // 继承View基类，实现自定义视图组件
    
    // ==================== 常量定义 ====================
    
    /**
     * 刻度尺名称数组
     * 表示频率刻度的标签，从10Hz到1GHz
     * 索引0-8分别对应：10Hz, 100Hz, 1kHz, 10kHz, 100kHz, 1MHz, 10MHz, 100MHz, 1GHz
     */
    //    private static final String[] RULERNAME = {"0", "1us", "10us", "100us", "1ms", "10ms", "100ms", "1s", "10s"}; // 旧版时间刻度（已废弃）
    private static final String[] RULERNAME = {"10", "100", "1k", "10k", "100k", "1M", "10M", "100M", "1G"}; // 频率刻度标签数组

    // ==================== 成员变量定义 ====================
    
    /**
     * 上下文对象
     * 用于获取资源和系统服务
     */
    private Context context; // Android上下文引用
    
    /**
     * 画笔对象
     * 用于绑定绘制样式（颜色、线宽、字体大小等）
     */
    private Paint paint; // 绘制画笔
    
    /**
     * 背景颜色
     * 从资源文件读取的背景色值
     */
    private int bgColor; // 背景色值
    
    /**
     * 刻度线颜色
     * 用于绘制刻度线的颜色值
     */
    private int lineColor; // 刻度线颜色值
    
    /**
     * 文本颜色
     * 用于绘制刻度标签的颜色值
     */
    private int textColor; // 文本颜色值
    
    /**
     * 视图高度
     * 测量后的视图高度（像素）
     */
    private int height; // 视图高度（像素）
    
    /**
     * 视图宽度
     * 测量后的视图宽度（像素）
     */
    private int width; // 视图宽度（像素）

    /**
     * 线条宽度
     * 绘制线条的宽度（像素）
     */
    private int strokeWidth; // 线条宽度（像素）
    
    /**
     * 底部内边距
     * 文本距离底部的间距（像素）
     */
    private int bottomPadding; // 底部内边距（像素）
    
    /**
     * 长刻度线高度
     * 主要刻度线的长度（像素）
     */
    private int longLineHeight; // 长刻度线高度（像素）
    
    /**
     * 中刻度线高度
     * 中等刻度线的长度（像素）
     */
    private int middleLineHeight; // 中刻度线高度（像素）
    
    /**
     * 短刻度线高度
     * 次要刻度线的长度（像素）
     */
    private int shortLineHeight; // 短刻度线高度（像素）
    
    /**
     * 两条刻度线之间的间隔
     * 相邻刻度线之间的像素距离
     */
    private float twoLineInterval; // 刻度线间隔（像素）
    
    /**
     * 当前选择位置的X坐标
     * 表示用户选择的频率位置（像素）
     */
    private float pointX; // 选择位置X坐标（像素）

    /**
     * 最小时间值（纳秒）
     * 可选择的最小时间范围限制
     */
    private long minNs = TopUtilBandWidthHz.DEFAULT_MIN_TIME; // 最小时间限制（纳秒）
    
    /**
     * 最大时间值（纳秒）
     * 可选择的最大时间范围限制
     */
    private long maxNs = TopUtilBandWidthHz.DEFAULT_MAX_TIME; // 最大时间限制（纳秒）

    /**
     * 刻度变化监听器
     * 当用户选择值变化时回调通知外部
     */
    private OnRulerChangedListener onRulerChangedListener; // 值变化监听器

    // ==================== 接口定义 ====================
    
    /**
     * 刻度变化监听接口
     * 用于通知外部用户选择的值发生变化
     * 
     * @author yangj
     * @since 2017/4/14
     */
    public interface OnRulerChangedListener { // 定义值变化回调接口
        
        /**
         * 当刻度值变化时回调
         * 
         * @param value 当前选择的数值（如1.5表示1.5ms）
         * @param unit 当前单位（ns/us/ms/s）
         * @param item 当前刻度项的基准值（如1/10/100）
         */
        void rulerChanged(double value, String unit, double item); // 值变化回调方法
    }

    // ==================== Getter/Setter方法 ====================
    
    /**
     * 获取刻度变化监听器
     * 
     * @return 当前的监听器实例，可能为null
     */
    public OnRulerChangedListener getOnRulerChangedListener() { // 获取监听器方法
        return onRulerChangedListener; // 返回当前监听器
    }

    /**
     * 设置刻度变化监听器
     * 
     * @param onRulerChangedListener 监听器实例，用于接收值变化通知
     */
    public void setOnRulerChangedListener(OnRulerChangedListener onRulerChangedListener) { // 设置监听器方法
        this.onRulerChangedListener = onRulerChangedListener; // 保存监听器引用
    }

    // ==================== 构造方法 ====================
    
    /**
     * 构造方法（单参数）
     * 用于代码中直接创建实例
     * 
     * @param context Android上下文对象
     */
    public TopViewBandWidthHzLarge(Context context) { // 单参数构造方法
        this(context, null); // 调用双参数构造方法
    }

    /**
     * 构造方法（双参数）
     * 用于XML布局中创建实例
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集
     */
    public TopViewBandWidthHzLarge(Context context, AttributeSet attrs) { // 双参数构造方法
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * 构造方法（三参数）
     * 完整的构造方法，执行初始化
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewBandWidthHzLarge(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        this.context = context; // 保存上下文引用
        initView(context, attrs, defStyleAttr); // 执行视图初始化
    }

    // ==================== 初始化方法 ====================
    
    /**
     * 初始化视图
     * 设置颜色、尺寸参数和画笔属性
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 初始化视图方法
        bgColor = getResources().getColor(R.color.bg_slip_backcolor); // 从资源获取背景色
        lineColor = getResources().getColor(R.color.scaleDivider); // 从资源获取刻度线颜色
        textColor = getResources().getColor(R.color.textColorTop); // 从资源获取文本颜色

        strokeWidth = 1; // 设置线条宽度为1像素
        bottomPadding = 10; // 设置底部内边距为10像素
        longLineHeight = 60; // 设置长刻度线高度为60像素
        middleLineHeight = 30; // 设置中刻度线高度为30像素
        shortLineHeight = 20; // 设置短刻度线高度为20像素
        twoLineInterval = 12f; // 设置刻度线间隔为12像素

        paint = new Paint(); // 创建画笔对象
        paint.setStrokeWidth(strokeWidth); // 设置画笔线条宽度
        paint.setAntiAlias(true); // 启用抗锯齿，使绘制更平滑
        paint.setTextSize(20); // 设置文本大小为20像素
    }

    // ==================== 测量方法 ====================
    
    /**
     * 测量视图尺寸
     * 根据MeasureSpec计算视图的宽高
     * 
     * @param widthMeasureSpec 宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 测量视图尺寸方法
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec)); // 设置测量后的宽高
    }

    /**
     * 测量视图宽度
     * 根据测量模式和刻度线数量计算宽度
     * 
     * @param widthMeasureSpec 宽度测量规格
     * @return 测量后的宽度值（像素）
     */
    private int measureWidth(int widthMeasureSpec) { // 测量宽度方法
        int measureMode = MeasureSpec.getMode(widthMeasureSpec); // 获取测量模式
        int measureSize = MeasureSpec.getSize(widthMeasureSpec); // 获取测量尺寸
        int result = (int) (twoLineInterval * 73); // 计算默认宽度：73个间隔（72条刻度线+首尾边距）
        switch (measureMode) { // 根据测量模式处理
            case MeasureSpec.EXACTLY: // 精确模式：父容器指定确切大小
                result = Math.max(result, measureSize); // 取默认值和指定值的较大者
                break; // 跳出switch
            case MeasureSpec.AT_MOST: // 最大模式：子视图不能超过指定大小
                result = Math.min(result, measureSize); // 取默认值和指定值的较小者
                break; // 跳出switch
        }
        width = result; // 保存测量后的宽度
        return result; // 返回测量结果
    }

    /**
     * 测量视图高度
     * 根据测量模式和刻度线高度计算高度
     * 
     * @param heightMeasureSpec 高度测量规格
     * @return 测量后的高度值（像素）
     */
    private int measureHeight(int heightMeasureSpec) { // 测量高度方法
        int measureMode = MeasureSpec.getMode(heightMeasureSpec); // 获取测量模式
        int measureSize = MeasureSpec.getSize(heightMeasureSpec); // 获取测量尺寸
        int result = longLineHeight * 2; // 计算默认高度：长刻度线高度的2倍
        switch (measureMode) { // 根据测量模式处理
            case MeasureSpec.EXACTLY: // 精确模式：父容器指定确切大小
                result = Math.max(result, measureSize); // 取默认值和指定值的较大者
                break; // 跳出switch
            case MeasureSpec.AT_MOST: // 最大模式：子视图不能超过指定大小
                result = Math.min(result, measureSize); // 取默认值和指定值的较小者
                break; // 跳出switch
        }
        height = result; // 保存测量后的高度
        return result; // 返回测量结果
    }

    // ==================== 绘制方法 ====================
    
    /**
     * 绘制视图
     * 按顺序绘制背景、刻度尺和进度条
     * 
     * @param canvas 画布对象，用于绑定绘制
     */
    @Override
    protected void onDraw(Canvas canvas) { // 绘制视图方法
        super.onDraw(canvas); // 调用父类绘制方法
        drawBg(canvas); // 绘制背景
        drawRuler(canvas); // 绘制刻度尺
        drawProgress(canvas); // 绘制进度条（选择区域）
    }

    /**
     * 绘制进度条
     * 显示用户选择的区域（从左侧到当前位置）
     * 
     * @param canvas 画布对象
     */
    private void drawProgress(Canvas canvas) { // 绘制进度条方法
        paint.setColor(Color.argb(0x99, Color.red(textColor), Color.green(textColor), Color.blue(textColor))); // 设置半透明文本颜色
        canvas.drawRect(1, 1, pointX == 0 ? 2 : pointX, height, paint); // 绘制矩形进度条，pointX为0时绘制最小宽度2像素
    }

    /**
     * 绘制刻度尺
     * 绘制刻度线和刻度标签
     * 
     * @param canvas 画布对象
     */
    private void drawRuler(Canvas canvas) { // 绘制刻度尺方法
        paint.setColor(lineColor); // 设置刻度线颜色
        for (int i = 0; i < 72; i++) { // 循环绘制72条刻度线
            float x = twoLineInterval * (i + 1); // 计算当前刻度线的X坐标
            if (i % 9 == 0 && i != 0) { // 每9条刻度线绘制一条长刻度线（主刻度）
                canvas.drawLine(x, 1, x, longLineHeight, paint); // 绘制长刻度线
            } else if (i % 9 == 4) { // 在每9条刻度线的中间位置绘制中刻度线
                canvas.drawLine(x, 1, x, middleLineHeight, paint); // 绘制中刻度线
            } else { // 其他位置绘制短刻度线
                canvas.drawLine(x, 1, x, shortLineHeight, paint); // 绘制短刻度线
            }
        }
        paint.setColor(textColor); // 设置文本颜色
        int j = 0; // 刻度名称索引
        for (int i = 0; i < 72; i++) { // 循环绘制刻度标签
            float x = twoLineInterval * (i + 1); // 计算当前刻度线的X坐标
            if (i % 9 == 0 && i != 0) { // 在长刻度线位置绘制标签
                String text = RULERNAME[++j]; // 获取刻度名称
                canvas.drawText(text, x - getTextWidth(text) / 2, height - bottomPadding, paint); // 居中绘制文本
            }
        }
        canvas.drawText(RULERNAME[0], bottomPadding, height - bottomPadding, paint); // 绘制第一个刻度标签（左侧）
        String lastText = RULERNAME[RULERNAME.length - 1]; // 获取最后一个刻度名称
        canvas.drawText(lastText, width - getTextWidth(lastText) - bottomPadding, height - bottomPadding, paint); // 绘制最后一个刻度标签（右侧）
    }


    /**
     * 获取文本宽度
     * 使用画笔测量文本的边界宽度
     * 
     * @param text 要测量的文本
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) { // 获取文本宽度方法
        Rect rect = new Rect(); // 创建矩形对象用于存储边界
        paint.getTextBounds(text, 0, text.length(), rect); // 获取文本边界
        int w = rect.width(); // 获取边界宽度
        int h = rect.height(); // 获取边界高度（未使用）
        return w; // 返回文本宽度
    }

    /**
     * 绘制背景
     * 绘制背景色和边框
     * 
     * @param canvas 画布对象
     */
    private void drawBg(Canvas canvas) { // 绘制背景方法
        paint.setColor(bgColor); // 设置背景颜色
        canvas.drawRect(0, 0, width, height, paint); // 绘制背景矩形
        paint.setColor(lineColor); // 设置边框颜色
        paint.setStyle(Paint.Style.STROKE); // 设置画笔样式为描边
        canvas.drawRect(1, 1, width - 1, height-1, paint); // 绘制边框（留1像素边距）
        paint.setStyle(Paint.Style.FILL); // 恢复画笔样式为填充
    }

    // ==================== 触摸事件处理 ====================
    
    /**
     * 处理触摸事件
     * 响应用户的触摸滑动操作
     * 
     * @param event 触摸事件对象
     * @return true表示事件已处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 触摸事件处理方法
        switch (event.getAction()) { // 根据动作类型处理
            case MotionEvent.ACTION_DOWN: // 按下事件
                break; // 不做处理
            case MotionEvent.ACTION_MOVE: // 移动事件
                pointX = checkRange(event.getX()); // 更新选择位置并检查范围
                onListener(); // 触发回调通知
                invalidate(); // 请求重绘
                break; // 跳出switch
            case MotionEvent.ACTION_UP: // 抬起事件
                pointX = checkRange(event.getX() - event.getX() % (twoLineInterval / 2)); // 对齐到最近的刻度位置
                onListener(); // 触发回调通知
                invalidate(); // 请求重绘
                break; // 跳出switch
        }
        return true; // 返回true表示事件已处理
    }

    /**
     * 触发监听器回调
     * 根据当前位置计算对应的值并通知监听器
     */
    private void onListener() { // 触发监听器回调方法
        if (onRulerChangedListener != null) { // 检查监听器是否存在
            float px = pointX - pointX % (twoLineInterval / 2); // 对齐到最近的刻度位置
            if (px < 0) { // 位置小于0，无效范围
                // 不做处理
            } else if (px < twoLineInterval * 10) { // 第一段：0-10个间隔，对应ns单位
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 0, 50, 0), TopUtilBandWidthHz.UNIT_NS, 100); // 回调ns单位值
            } else if (px < twoLineInterval * 19) { // 第二段：10-19个间隔，对应us单位（1us量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 10, 0.5, 1), TopUtilBandWidthHz.UNIT_US, 1); // 回调us单位值
            } else if (px < twoLineInterval * 28) { // 第三段：19-28个间隔，对应us单位（10us量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 19, 5, 10), TopUtilBandWidthHz.UNIT_US, 10); // 回调us单位值
            } else if (px < twoLineInterval * 37) { // 第四段：28-37个间隔，对应us单位（100us量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 28, 50, 100), TopUtilBandWidthHz.UNIT_US, 100); // 回调us单位值
            } else if (px < twoLineInterval * 46) { // 第五段：37-46个间隔，对应ms单位（1ms量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 37, 0.5, 1), TopUtilBandWidthHz.UNIT_MS, 1); // 回调ms单位值
            } else if (px < twoLineInterval * 55) { // 第六段：46-55个间隔，对应ms单位（10ms量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 46, 5, 10), TopUtilBandWidthHz.UNIT_MS, 10); // 回调ms单位值
            } else if (px < twoLineInterval * 64) { // 第七段：55-64个间隔，对应ms单位（100ms量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 55, 50, 100), TopUtilBandWidthHz.UNIT_MS, 100); // 回调ms单位值
            } else if (px <= twoLineInterval * 73) { // 第八段：64-73个间隔，对应s单位（1s量级）
                onRulerChangedListener.rulerChanged(getValueFromPx(px, 64, 0.5, 1), TopUtilBandWidthHz.UNIT_S, 1); // 回调s单位值
            }
        }
    }

    // ==================== 按键移动方法 ====================
    
    /**
     * 向左移动一步
     * 将选择位置向左移动半个刻度间隔
     */
    public void moveLeftOneStep() { // 向左移动一步方法
        pointX = checkRange(pointX - twoLineInterval / 2); // 更新位置并检查范围
        onListener(); // 触发回调通知
        invalidate(); // 请求重绘
    }

    /**
     * 向右移动一步
     * 将选择位置向右移动半个刻度间隔
     */
    public void moveRightOneStep() { // 向右移动一步方法
        pointX = checkRange(pointX + twoLineInterval / 2); // 更新位置并检查范围
        onListener(); // 触发回调通知
        invalidate(); // 请求重绘
    }

    // ==================== 范围设置方法 ====================
    
    /**
     * 设置时间范围
     * 限制用户可选择的时间范围
     * 
     * @param minNs 最小时间值（纳秒）
     * @param maxNs 最大时间值（纳秒）
     */
    public void setTimeRange(long minNs, long maxNs) { // 设置时间范围方法
        this.minNs = minNs; // 保存最小时间限制
        this.maxNs = maxNs; // 保存最大时间限制
    }

    // ==================== 值设置方法 ====================
    
    /**
     * 设置当前值
     * 根据给定的值和单位设置选择位置
     * 
     * @param curValue 当前值
     * @param itemUnit 单位（ns/us/ms/s）
     * @param itemValue 刻度项基准值（1/10/100）
     */
    public void setValue(double curValue, String itemUnit, double itemValue) { // 设置当前值方法
        float px = 0; // 初始化像素位置
        if (TopUtilBandWidthHz.UNIT_NS.equals(itemUnit) && itemValue == 100) { // ns单位，100ns刻度
            px = getPxFromValue(curValue, 0, 50, 0); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_US.equals(itemUnit) && itemValue == 1) { // us单位，1us刻度
            px = getPxFromValue(curValue, 1, 0.5, 10); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_US.equals(itemUnit) && itemValue == 10) { // us单位，10us刻度
            px = getPxFromValue(curValue, 10, 5, 19); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_US.equals(itemUnit) && itemValue == 100) { // us单位，100us刻度
            px = getPxFromValue(curValue, 100, 50, 28); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_MS.equals(itemUnit) && itemValue == 1) { // ms单位，1ms刻度
            px = getPxFromValue(curValue, 1, 0.5, 37); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_MS.equals(itemUnit) && itemValue == 10) { // ms单位，10ms刻度
            px = getPxFromValue(curValue, 10, 5, 46); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_MS.equals(itemUnit) && itemValue == 100) { // ms单位，100ms刻度
            px = getPxFromValue(curValue, 100, 50, 55); // 计算像素位置
        } else if (TopUtilBandWidthHz.UNIT_S.equals(itemUnit) && itemValue == 1) { // s单位，1s刻度
            px = getPxFromValue(curValue, 1, 0.5, 64); // 计算像素位置
        }
        if (px != 0) { // 如果计算得到有效位置
            pointX = checkRange(px - px % (twoLineInterval / 2)); // 对齐到刻度位置并检查范围
            invalidate(); // 请求重绘
        }
    }

    // ==================== 范围检查方法 ====================
    
    /**
     * 检查并限制选择位置范围
     * 根据设置的最小/最大时间限制调整位置
     * 
     * @param willPointX 期望的位置
     * @return 调整后的有效位置
     */
    private float checkRange(float willPointX) { // 检查范围方法
        pointX = willPointX; // 临时保存期望位置
//        if (minNs < TopUtilBandWidthHz.TIME_US2NS) {//最小值小于1us
//            long minPointX = (long) (minNs / 50 * (twoLineInterval / 2));
//            pointX = Math.max(willPointX, minPointX);
//            Log.d("Tag.Debug", String.format("checkRange: min:%d,max:%f ,minNs:%d,twoLineInterval:%f",minPointX,willPointX,minNs,twoLineInterval ));
//        }
        if (minNs < TopUtilBandWidthHz.TIME_US2NS) { // 最小值小于1us
            long minPointX = (long) (minNs / 50 * (twoLineInterval / 2)); // 计算最小位置
            pointX = Math.max(willPointX, minPointX); // 限制不小于最小位置
        }else if (minNs < TopUtilBandWidthHz.TIME_US2NS*10){ // 最小值小于10us
            long minPointX =(long) getPxFromValue(minNs/1e3f, 1, 0.5, 10); // 计算最小位置（转换为us）
            pointX = Math.max(willPointX, minPointX); // 限制不小于最小位置
        }else if (minNs < TopUtilBandWidthHz.TIME_US2NS*100){ // 最小值小于100us
            long minPointX = (long) getPxFromValue(minNs/1e4f, 10, 5, 19); // 计算最小位置（转换为10us单位）
            pointX = Math.max(willPointX, minPointX); // 限制不小于最小位置
        }else if (minNs < TopUtilBandWidthHz.TIME_US2NS*1e3){ // 最小值小于1ms
            long minPointX = (long) getPxFromValue(minNs/1e5f, 100, 50, 28); // 计算最小位置（转换为100us单位）
            pointX = Math.max(willPointX, minPointX); // 限制不小于最小位置
        }else if (minNs < TopUtilBandWidthHz.TIME_US2NS*1e4){ // 最小值小于10ms
            long minPointX = (long) getPxFromValue(minNs/1e6f, 1, 0.5, 37); // 计算最小位置（转换为ms）
            pointX = Math.max(willPointX, minPointX); // 限制不小于最小位置
        }

        if (maxNs >= TopUtilBandWidthHz.TIME_S2NS) { // 最大值大于等于1s
            long maxPointX = (long) ((maxNs / (TopUtilBandWidthHz.TIME_S2NS / 2) - 2 + 64 * 2) * (twoLineInterval / 2)); // 计算最大位置
            pointX = Math.min(pointX, maxPointX); // 限制不大于最大位置
        }
        return pointX; // 返回调整后的位置
    }

    // ==================== 坐标转换方法 ====================
    
    /**
     * 根据值计算像素位置
     * 将数值转换为对应的X坐标
     * 
     * @param value 数值
     * @param headValue 该段起始值
     * @param halfItemValue 半个刻度对应的值增量
     * @param beforeHeadCount 该段之前的刻度数量（半个刻度为单位）
     * @return 对应的像素位置
     */
    private float getPxFromValue(double value, int headValue, double halfItemValue, int beforeHeadCount) { // 值转像素方法
        return (float) (((value - headValue) / halfItemValue + beforeHeadCount * 2) * (twoLineInterval / 2)); // 计算并返回像素位置
    }

    /**
     * 十进制格式化器
     * 用于格式化数值，保留两位小数
     */
    DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA)); // 创建格式化器，使用中国区域设置

    /**
     * 根据像素位置计算值
     * 将X坐标转换为对应的数值
     * 
     * @param px 像素位置
     * @param beforeHeadCount 该段之前的刻度数量（半个刻度为单位）
     * @param haifItemValue 半个刻度对应的值增量
     * @param headValue 该段起始值
     * @return 对应的数值
     */
    private double getValueFromPx(float px, int beforeHeadCount, double haifItemValue, int headValue) { // 像素转值方法
        return Double.parseDouble(df.format((px / (twoLineInterval / 2) - beforeHeadCount * 2) * haifItemValue + headValue)); // 计算并格式化返回数值
    }
} // 类结束
