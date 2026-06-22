package com.micsig.tbook.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.micsig.tbook.ui.main.MainBeanTopRight;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
 *                                    MTriggerStateBar - 触发状态栏控件
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 示波器UI组件模块，用于显示触发状态信息的自定义视图控件。
 *
 * 【核心职责】
 * 1. 显示触发相关的状态信息（通道号、触发类型等）
 * 2. 支持多通道状态信息的动态展示
 * 3. 提供滚动动画效果（当状态项超过显示容量时）
 * 4. 使用OpenGL ES进行硬件加速渲染
 *
 * 【架构设计】
 * 继承自GLTextureView，利用OpenGL ES进行高性能渲染：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    MTriggerStateBar                              │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │ 数据管理层   │  │ 渲染层      │  │ 动画层                  │  │
 * │  │ showList    │  │ onGLDraw   │  │ ObjectAnimator         │  │
 * │  │ MainBeanTop │  │ Canvas/Bit │  │ translationY动画       │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * 外部数据源 → setData() → showList更新 → draw()重绘 → requestRender() → onGLDraw()渲染
 *
 * 【依赖关系】
 * - GLTextureView: OpenGL ES纹理视图基类
 * - MainBeanTopRight: 状态信息数据模型
 * - TChan: 通道常量定义
 * - BitmapUtil: 位图工具类
 *
 * 【使用示例】
 * MTriggerStateBar stateBar = new MTriggerStateBar(context);
 * ArrayList<MainBeanTopRight> list = new ArrayList<>();
 * list.add(new MainBeanTopRight("Trigger", TChan.Ch1, R.color.ch1_color));
 * stateBar.setData(list);
 * stateBar.setOnClickListener((view, item) -> {
 *     // 处理点击事件
 * });
 *
 * 【注意事项】
 * 1. 使用GLTextureView需要在AndroidManifest中声明硬件加速
 * 2. 数据更新通过setData方法，内部会自动触发重绘
 * 3. 当状态项超过4个时，会自动启动滚动动画
 * 4. 渲染模式为RENDERMODE_WHEN_DIRTY，需要手动调用requestRender()
 *
 * @author liwb
 * @version 1.0
 * @since 2018/2/1
 */
public class MTriggerStateBar extends GLTextureView {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义区
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于调试输出 */
    public static final String TAG = MTriggerStateBar.class.getSimpleName();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 上下文与尺寸
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 应用上下文，用于获取资源 */
    private Context context;

    /** 控件宽度 */
    private int width, height;

    /** 项目间距 */
    private int margin;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 数据与渲染
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示数据列表，存储所有待显示的状态信息 */
    private ArrayList<MainBeanTopRight> showList = new ArrayList<>();

    /** 渲染用的位图缓冲区 */
    private Bitmap bmp;

    /** 背景矩形位图 */
    private Bitmap rectangleBmp;

    /** 绘制用的Canvas对象 */
    private Canvas canvas;

    /** 普通画笔，用于绘制线条和基本图形 */
    private Paint paint;

    /** 文本画笔，用于绘制文字 */
    private TextPaint tPaint;

    /** 裁剪区域矩形 */
    private RectF clipRect = new RectF();

    /** 背景颜色 */
    private int backColor = Color.RED;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 事件与动画
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 点击事件监听器 */
    private OnClickListener onClickListener;

    /** 标记位图是否需要更新 */
    private boolean isChanageBitmap = false;

    /** 线程同步锁，保护showList的并发访问 */
    private final Object lock = new Object();

    /** Y轴平移动画器，用于滚动效果 */
    private ObjectAnimator animator;

    /** 重置动画器，将控件恢复到初始位置 */
    private ObjectAnimator resetAnimator;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 点击事件监听接口
     * 用于处理状态栏项目的点击事件
     */
    public interface OnClickListener {
        /**
         * 当状态栏项目被点击时回调
         *
         * @param view 被点击的视图
         * @param item 被点击的数据项
         */
        void onClick(MTriggerStateBar view, MainBeanTopRight item);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Getter/Setter方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取点击事件监听器
     *
     * @return 当前的点击事件监听器
     */
    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    /**
     * 设置点击事件监听器
     *
     * @param onClickListener 点击事件监听器
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 简单构造方法
     *
     * @param context 应用上下文
     */
    public MTriggerStateBar(Context context) {
        this(context, null);
    }

    /**
     * XML属性构造方法
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     */
    public MTriggerStateBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // 初始化间距参数
        margin = 3;

        // 设置默认尺寸
        width = 88;
        height = 68;

        // 初始化普通画笔
        paint = new Paint();
        paint.setStrokeWidth(1);        // 线宽1像素
        paint.setTextSize(18);          // 文字大小18sp
        paint.setAntiAlias(false);      // 关闭抗锯齿（性能优化）
        paint.setColor(getResources().getColor(R.color.color_divider_mainwave));

        // 初始化文本画笔
        tPaint = new TextPaint();
        tPaint.setTextSize(18);
        tPaint.setAntiAlias(true);      // 文字开启抗锯齿
        tPaint.setColor(paint.getColor());
        tPaint.setTextAlign(Paint.Align.LEFT);

        // 创建位图缓冲区（223x100像素）
        bmp = Bitmap.createBitmap(223, 100, Bitmap.Config.ARGB_8888);

        // 加载并缩放背景矩形位图
        rectangleBmp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.ic_rectangle_bg_trigger_state_bar);
        rectangleBmp = BitmapUtil.scaleBitmap(rectangleBmp, bmp.getWidth(), bmp.getHeight());

        // 创建绑定到位图的Canvas
        canvas = new Canvas(bmp);

        // 设置裁剪区域为整个位图
        clipRect.set(0, 0, bmp.getWidth(), bmp.getHeight());

        // 设置填充样式和背景颜色
        paint.setStyle(Paint.Style.FILL);
        backColor = context.getResources().getColor(R.color.color_Backcolor_MainMenu2);

        // 初始化滚动动画（Y轴向上平移32像素）
        animator = ObjectAnimator.ofFloat(this, "translationY", 0f, -32f);
        animator.setDuration(6000);                     // 动画时长6秒
        animator.setRepeatMode(ValueAnimator.RESTART);  // 重复模式：重新开始
        animator.setRepeatCount(ValueAnimator.INFINITE);// 无限循环

        // 初始化重置动画（回到初始位置）
        resetAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置显示数据列表
     * 线程安全方法，会触发重绘
     *
     * @param list 待显示的状态信息列表
     */
    public void setData(ArrayList<MainBeanTopRight> list) {
        synchronized (lock) {
            showList = list;        // 更新数据
            draw();                 // 重绘位图
            requestRender();        // 请求OpenGL渲染
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 测量控件尺寸
     *
     * @param widthMeasureSpec 宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * 测量宽度
     *
     * @param widthMeasureSpec 宽度测量规格
     * @return 测量后的宽度值
     */
    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = 220;  // 默认宽度

        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                // 精确模式：取测量值和默认值的较大者
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                // 最大模式：取测量值和默认值的较小者
                result = Math.min(result, measureSize);
                break;
        }
        return result;
    }

    /**
     * 测量高度
     *
     * @param heightMeasureSpec 高度测量规格
     * @return 测量后的高度值
     */
    private int measureHeight(int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
        int result = 92;  // 默认高度

        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                // 精确模式：取测量值和默认值的较大者
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                // 最大模式：取测量值和默认值的较小者
                result = Math.min(result, measureSize);
                break;
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 文本测量辅助方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取文本宽度
     *
     * @param text 待测量的文本
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    /** 文本测量用的临时矩形 */
    private Rect rect = new Rect();

    /**
     * 获取文本高度
     *
     * @param text 待测量的文本
     * @return 文本高度（像素）
     */
    private int getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // OpenGL渲染方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * OpenGL绘制回调
     * 在OpenGL线程中调用，执行实际的渲染操作
     *
     * @param canvas OpenGL画布
     */
    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        // 无数据时不绘制
        if (showList.size() == 0) return;

        synchronized (lock) {
            // 绘制背景颜色
            drawBackColor(canvas);

            // 如果位图有更新，通知OpenGL刷新纹理
            if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);

            // 绘制状态信息位图
            canvas.drawBitmap(bmp, 4, 3);
            isChanageBitmap = false;
        }
    }

    /**
     * 绘制背景颜色
     * 清除缓冲区并填充背景色
     *
     * @param canvas OpenGL画布
     */
    private void drawBackColor(ICanvasGL canvas) {
        canvas.clearBuffer(backColor);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 混合模式
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 清除混合模式 */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    /** 源混合模式 */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制逻辑
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取可见项目数量和是否包含数字类型
     *
     * @return 元组：第一个元素为可见项目数，第二个元素为是否包含数字类型
     */
    private Tuple<Integer, Boolean> GetShowListCount() {
        int count = 0;
        boolean isNumber = false;

        // 遍历列表统计可见项目
        for (int i = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;
            if (showList.get(i).isShowNumber()) {
                isNumber = true;
            }
            count++;
        }

        Tuple<Integer, Boolean> tuple = new Tuple<>(count, isNumber);
        return tuple;
    }

    /**
     * 核心绘制方法
     * 根据数据类型和数量选择不同的绘制策略
     */
    private void draw() {
        Tuple<Integer, Boolean> tuple = GetShowListCount();

        // 清除画布，填充背景色
        this.paint.setColor(backColor);
        this.canvas.drawRect(clipRect, paint);

        if (tuple.getSecond()) {
            // 数字类型（触发状态）
            if (tuple.getFirst() > 4) {
                // 超过4个，使用紧凑布局
                int showNum = drawMore3Num(tuple.getFirst());
                if (showNum > 4) {
                    startAnimator();    // 启动滚动动画
                } else {
                    stopAnimator();     // 停止滚动动画
                }
            } else {
                // 4个或以下，使用宽松布局
                drawLess3Num(tuple.getFirst());
                stopAnimator();
            }
        } else {
            // 普通字符串类型（串行解码指示符）
            drawString();
            stopAnimator();
        }

        // 标记位图需要更新
        isChanageBitmap = true;
    }

    /**
     * 绘制少量数字状态（4个或以下）
     * 使用垂直布局，每个项目独占一行
     *
     * @param listSize 列表大小
     */
    private void drawLess3Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textHeight = 0;
        int textX = 0;
        int textY = 2;
        int lineBottomY = 0;
        int lineTopY = -14;

        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;

            String number = "";
            j++;
            int startHeight = textHeight;
            int listCount = listSize + 1;
            if (listCount >= 4) listCount = 4;  // 逻辑触发中，只显示3个

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));

            // 根据通道号设置显示的编号
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
                case TChan.Ch8 + 1: number = " ●";break;
            }

            // 计算起始高度
            startHeight = (int) (height / listCount * (j - 0.5));

            // 根据是否有编号调整显示位置
            if (!number.equalsIgnoreCase("")) {
                startHeight += textY + j * 4;
                numWidth = getTextWidth(number);
            } else {
                startHeight += textY + 10;
            }

            // 绘制编号和文本
            drawText(number, textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(), textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            // 绘制上划线或下划线
            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(textWidth, startHeight + lineTopY, w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(textWidth, lineBottomY + startHeight+3, w + textWidth, lineBottomY + startHeight+3, paint);
            }

            textHeight = getTextHeight(item.getText()) + margin + startHeight;
        }
    }

    /**
     * 绘制4个数字状态
     * 使用两列布局，每列2个项目
     *
     * @param listSize 列表大小
     */
    private void draw4Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textHeight = 0;
        int textX = 0;
        int textY = -2;
        int lineBottomY = 0;
        int lineTopY = -14;

        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;

            String number = "";
            j++;
            int listCount = 3;

            int startHeight = textHeight;
            int startWidth = 0;

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));

            // 后两个项目放在右列
            if (j>2){
                startWidth=105;
            }

            // 根据通道号设置显示的编号
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
            }

            startHeight = height / listCount * (j>2?j-2:j);

            // 根据是否有编号调整显示位置
            if (!number.equalsIgnoreCase("")) {
                startHeight += textY + (j>2?j-2:j) * 4;
                numWidth = getTextWidth(number);
            } else {
                startHeight += textY + 10;
            }

            // 绘制编号和文本
            drawText(number,startWidth+ textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(),startWidth+ textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            // 绘制上划线或下划线
            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(startWidth+textWidth, startHeight + lineTopY, startWidth+w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(startWidth+textWidth, lineBottomY + startHeight+3, startWidth+w + textWidth, lineBottomY + startHeight+3, paint);
            }

            textHeight = getTextHeight(item.getText()) + margin + startHeight;
        }
    }

    /**
     * 绘制多个数字状态（超过4个）
     * 使用两列紧凑布局
     *
     * @param listSize 列表大小
     * @return 实际显示的项目数
     */
    private int drawMore3Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textX = 0;
        int lineBottomY = 0;
        int lineTopY = -14;
        int showNum = 0;

        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;

            String number = "";
            j++;
            int startHeight;
            int startWidth;

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));

            // 根据通道号设置显示的编号
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
                case TChan.Ch8 + 1: number = " ●";break;
            }

            // 奇数项在左列，偶数项在右列
            if ((j % 2) == 1) {
                startWidth = 0;
            } else {
                startWidth = 105;
            }

            // 计算起始高度
            startHeight = 23 + 23 * ((j - 1) / 2);

            // 根据是否有编号调整显示位置
            if (!number.equalsIgnoreCase("")) {
                numWidth = getTextWidth(number);
            }

            // 绘制编号和文本
            drawText(number, startWidth + textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(), startWidth + textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            // 绘制上划线或下划线
            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(startWidth + textWidth, startHeight + lineTopY, startWidth + w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(startWidth + textWidth, lineBottomY + startHeight + 3, startWidth + w + textWidth, lineBottomY + startHeight + 3, paint);
            }

            showNum = j;
        }

        return showNum;
    }

    /**
     * 绘制字符串类型的状态信息
     * 用于串行解码指示符等非数字类型
     */
    private void drawString() {
        int startHeight = 18;

        for (int i = 0; i < showList.size(); i++){
            if (!showList.get(i).isVisible()) continue;

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));
            drawText(item.getText(),0,startHeight,paint);
        }
    }

    /**
     * 绘制文本
     * 支持多行文本，使用StaticLayout进行布局
     *
     * @param text 待绘制的文本
     * @param x X坐标
     * @param y Y坐标
     * @param p 画笔
     */
    private void drawText(String text, int x, int y, Paint p) {
        tPaint.setColor(p.getColor());
        text = text.replaceAll("\n", "  ");  // 将换行符替换为空格

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M及以上版本使用StaticLayout.Builder
            StaticLayout.Builder b = StaticLayout.Builder.obtain(text, 0, text.length(), tPaint, bmp.getWidth()-2);
            b.setAlignment(Layout.Alignment.ALIGN_NORMAL);
            StaticLayout sl = b.build();

            canvas.save();
            canvas.translate(x, y - 18);
            sl.draw(canvas);
            canvas.restore();
        } else {
            // 低版本直接绘制文本
            this.canvas.drawText(text, x, y, p);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 动画控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 启动滚动动画
     */
    private void startAnimator() {
        animator.start();
    }

    /**
     * 停止滚动动画并重置位置
     */
    private void stopAnimator() {
        if (animator.isRunning()) {
            animator.cancel();
        }
        resetAnimator.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 渲染模式配置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取渲染模式
     * 使用按需渲染模式，节省资源
     *
     * @return 渲染模式
     */
    @Override
    protected int getRenderMode() {
        return GLThread.RENDERMODE_WHEN_DIRTY;
    }
}
