package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.GLPaint;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.micsig.base.Logger;
import com.micsig.tbook.ui.util.BitmapUtil;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
 *                                    MTriggerTime - 触发时刻控件
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 示波器UI组件模块，用于显示触发时刻和采样相关信息的自定义视图控件。
 *
 * 【核心职责】
 * 1. 显示触发时刻位置（T图标和尖角标记）
 * 2. 显示存储深度、采样类型、采样率等参数
 * 3. 显示分段存储信息和触发时刻数值
 * 4. 支持可移动和不可移动两种模式
 * 5. 使用OpenGL ES进行硬件加速渲染
 *
 * 【架构设计】
 * 继承自GLTextureView，采用分层渲染架构：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    MTriggerTime                                  │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │ 数据层      │  │ 渲染层      │  │ 配置层                  │  │
 * │  │ content1~5  │  │ onGLDraw   │  │ 可见性/位置参数        │  │
 * │  │ 各类标志位  │  │ drawXxx方法│  │ 链式设置方法           │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * 外部设置参数 → setXxx方法 → 参数更新 → requestRender() → onGLDraw() → 分层渲染
 *
 * 【渲染层次】
 * 1. 背景层：drawBackColor() - 清除缓冲区
 * 2. 线条层：drawLine() - 绘制时间轴基线
 * 3. 图形层：drawPic() - 绘制括号和尖角标记
 * 4. 图标层：drawIconT() - 绘制触发T图标（可移动模式下）
 * 5. 内容层：drawContent() - 绘制参数文本
 *
 * 【依赖关系】
 * - GLTextureView: OpenGL ES纹理视图基类
 * - GLPaint: OpenGL画笔
 * - BitmapUtil: 位图工具类
 *
 * 【使用示例】
 * MTriggerTime triggerTime = new MTriggerTime(context);
 * triggerTime.setCanMove(true)
 *            .setTrigger("0ps")
 *            .setMemoryDepth("110Mpts")
 *            .setSampleType("Normal")
 *            .setSampleRate("500Msa/s")
 *            .updateUI();
 *
 * 【注意事项】
 * 1. 使用GLTextureView需要在AndroidManifest中声明硬件加速
 * 2. 所有set方法返回this，支持链式调用
 * 3. 参数更新后需调用updateUI()触发重绘
 * 4. 渲染模式为RENDERMODE_WHEN_DIRTY，需要手动调用requestRender()
 *
 * @author liwb
 * @version 1.0
 * @since 2018/1/29
 */
public class MTriggerTime extends GLTextureView {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 上下文与尺寸
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 应用上下文 */
    private Context context;

    /** 控件宽度和高度 */
    private int width, height;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 渲染资源
    // ═══════════════════════════════════════════════════════════════════════════════

    /** OpenGL画笔 */
    private GLPaint paint;

    /** 触发T图标位图 */
    private Bitmap iconTBitMap, contentBitmap;

    /** 内容背景位图 */
    private Bitmap bitmapContent;

    /** 尖角标记位图 */
    private Bitmap bitmap_tip;

    /** 普通画笔，用于绘制文本 */
    private Paint mPaint;

    /** 内容画布 */
    private Canvas contentCanvas;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 显示内容
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示存储深度，如"110Mpts" */
    private String content1 = "110Mpts";

    /** 显示采样类型，如"Normal" */
    private String content2 = "Normal";

    /** 显示采样率，如"500MSa/s" */
    private String content3 = "500Msa/s";

    /** 显示分段存储，如"190/1000" */
    private String content4 = "190/1000";

    /** 显示触发时刻的数字，如"0ps" */
    private String content5 = "0ps";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 可见性标志
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 分段存储是否显示 */
    private boolean segmentVisible = false;

    /** 存储深度是否可显 */
    private boolean memoryDepthVisible = true;

    /** 采样频率是否可显 */
    private boolean sampleRateVisible = true;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 布局参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 两侧空白的宽度 */
    private int spaceWidth;

    /** 时间轴基线的颜色 */
    private int passagewayColor;

    /** 时间轴基线的Y坐标 */
    private int passagewayY;

    /** 括住部分占整个控件的比例 */
    private float bracketMultiple;

    /** 正常时候括号占整个控件的比例 */
    private float normalBracketMultiple;

    /** 括号的宽度 */
    private int bracketWidth;

    /** 括号的颜色 */
    private int bracketColor;

    /** T图标位于整个控件的百分比位置（0-100） */
    private float iconTPercent;

    /** T图标的可见性 */
    private boolean iconTVisible;

    /** 下半部内容可见性 */
    private boolean contentVisible;

    /** 是否可以移动 */
    private boolean canMove;

    /** 尖角标记的宽度 */
    private int tipWidth;

    /** 尖角标记位于整个控件的百分比位置（0-1） */
    private float tipPercent;

    /** 文本区域显示Y坐标 */
    private int contentLayoutY;

    /** 文本的大小矩形，自动测量 */
    private Rect ContentRect = new Rect();

    /** 箭头绘制高度 */
    private int bitmap_arrowY = 0;

    /** 背景颜色 */
    private int backColor = Color.BLACK;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 简单构造方法
     *
     * @param context 应用上下文
     */
    public MTriggerTime(Context context) {
        this(context, null);
    }

    /**
     * XML属性构造方法
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     */
    public MTriggerTime(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    /**
     * 初始化视图
     * 设置默认参数和加载资源
     */
    private void initView() {
        // 初始化OpenGL画笔
        paint = new GLPaint();

        // 设置默认参数
        passagewayColor = Color.GRAY;
        bracketMultiple = 1;
        normalBracketMultiple = 1;
        bracketWidth = 5;
        bracketColor = 0xFF5D6273;
        iconTPercent = 50;
        tipWidth = 12;
        tipPercent = 0.5f;
        spaceWidth = 11;
        iconTVisible = true;
        contentLayoutY = 15;
        contentVisible = true;
        bitmap_arrowY = 4;

        // 创建内容位图缓冲区（320x68像素）
        contentBitmap = Bitmap.createBitmap(320, 68, Bitmap.Config.ARGB_8888);

        // 创建绑定到位图的Canvas
        contentCanvas = new Canvas(contentBitmap);

        // 加载背景位图
        bitmapContent = BitmapUtil.getBitmapFromDrawable(context, R.drawable.ic_rectangle_trigger_time_bottom);
        bitmapContent = BitmapUtil.scaleBitmap(bitmapContent, contentBitmap.getWidth(), contentBitmap.getHeight());

        // 加载尖角标记位图
        bitmap_tip = BitmapFactory.decodeResource(getResources(), R.drawable.ic_tip);
        tipWidth = bitmap_tip.getWidth();

        // 加载触发T图标
        Bitmap tBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mini_time_trigger);
        backColor = context.getResources().getColor(R.color.color_Backcolor_MainMenu1);

        // 缩放T图标
        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f);
        iconTBitMap = Bitmap.createBitmap(tBitmap, 0, 0, tBitmap.getWidth(), tBitmap.getHeight(), matrix, false);

        // 初始化普通画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setARGB(255, 0x25, 0x6F, 0x71);  // 设置默认颜色
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(20);
        mPaint.setAntiAlias(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 链式设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置T图标位置百分比
     *
     * @param iconTPercent 位置百分比（0-100）
     * @return this，支持链式调用
     */
    public MTriggerTime setIconTPercent(float iconTPercent) {
        this.iconTPercent = iconTPercent;
        return this;
    }

    /**
     * 是否可以移动
     *
     * @return true表示可移动
     */
    public boolean isCanMove() {
        return canMove;
    }

    /**
     * 设置是否可以移动
     *
     * @param canMove true表示可移动
     */
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    /**
     * 设置下半部内容是否可见
     *
     * @param contentVisible true表示可见
     * @return this，支持链式调用
     */
    public MTriggerTime setContentVisible(boolean contentVisible) {
        this.contentVisible = contentVisible;
        return this;
    }

    /**
     * 设置尖角标记位置百分比
     *
     * @param tipPercent 位置百分比（0-1）
     * @return this，支持链式调用
     */
    public MTriggerTime setTipPercent(float tipPercent) {
        this.tipPercent = tipPercent;
        return this;
    }

    /**
     * 设置括号缩放比例
     *
     * @param bracketMultiple 缩放比例
     * @return this，支持链式调用
     */
    public MTriggerTime setMultiple(float bracketMultiple) {
        this.bracketMultiple = bracketMultiple;
        return this;
    }

    /**
     * 设置正常括号缩放比例
     *
     * @param normalBracketMultiple 缩放比例
     * @return this，支持链式调用
     */
    public MTriggerTime setNormalMultiple(float normalBracketMultiple) {
        this.normalBracketMultiple = normalBracketMultiple;
        return this;
    }

    /**
     * 设置时间轴基线颜色
     *
     * @param passagewayColor 颜色值
     * @return this，支持链式调用
     */
    public MTriggerTime setPassagewayColor(int passagewayColor) {
        this.passagewayColor = passagewayColor;
        return this;
    }

    /**
     * 设置存储深度显示文本
     *
     * @param memoryDepth 存储深度字符串，如"110Mpts"
     * @return this，支持链式调用
     */
    public MTriggerTime setMemoryDepth(String memoryDepth) {
        this.content1 = memoryDepth;
        return this;
    }

    /**
     * 设置采样类型显示文本
     *
     * @param sampleType 采样类型字符串，如"Normal"
     * @return this，支持链式调用
     */
    public MTriggerTime setSampleType(String sampleType) {
        this.content2 = sampleType;
        return this;
    }

    /**
     * 设置采样率显示文本
     *
     * @param sample 采样率字符串，如"500MSa/s"
     * @return this，支持链式调用
     */
    public MTriggerTime setSampleRate(String sample) {
        this.content3 = sample;
        return this;
    }

    /**
     * 设置分段存储显示文本
     *
     * @param segmentedBuffer 分段存储字符串，如"190/1000"
     * @return this，支持链式调用
     */
    public MTriggerTime setSegmentedBuffer(String segmentedBuffer) {
        this.content4 = segmentedBuffer;
        return this;
    }

    /**
     * 获取分段存储显示文本
     *
     * @return 分段存储字符串
     */
    public String getSegmentedBuffer() {
        return this.content4;
    }

    /**
     * 采样率是否可见
     *
     * @return true表示可见
     */
    public boolean isSampleRateVisible() {
        return sampleRateVisible;
    }

    /**
     * 设置采样率是否可见
     *
     * @param sampleRateVisible true表示可见
     */
    public void setSampleRateVisible(boolean sampleRateVisible) {
        this.sampleRateVisible = sampleRateVisible;
    }

    /**
     * 存储深度是否可见
     *
     * @return true表示可见
     */
    public boolean isMemoryDepthVisible() {
        return memoryDepthVisible;
    }

    /**
     * 设置存储深度是否可见
     *
     * @param memoryDepthVisible true表示可见
     */
    public void setMemoryDepthVisible(boolean memoryDepthVisible) {
        this.memoryDepthVisible = memoryDepthVisible;
    }

    /**
     * 分段存储是否可见
     *
     * @return true表示可见
     */
    public boolean isSegmentVisible() {
        return segmentVisible;
    }

    /**
     * 设置分段存储是否显示
     * 分段存储显示时，与时刻左右分散显示
     * 分段存储不显示时，时刻居中显示
     *
     * @param segmentVisible true:显示 false:不显示
     */
    public void setSegmentVisible(boolean segmentVisible) {
        this.segmentVisible = segmentVisible;
    }

    /**
     * 设置触发时刻显示文本
     *
     * @param string 触发时刻字符串，如"0ps"
     * @return this，支持链式调用
     */
    public MTriggerTime setTrigger(String string) {
        this.content5 = string;
        return this;
    }

    /**
     * 获取触发时刻显示文本
     *
     * @return 触发时刻字符串
     */
    public String getTrigger() {
        return this.content5;
    }

    /**
     * 更新UI
     * 触发OpenGL重绘
     */
    public void updateUI() {
        requestRender();
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        int result = 256;  // 默认宽度

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
        width = result;
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
        int result = 15;  // 默认高度

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
        height = result;
        passagewayY = height / 2;  // 计算时间轴基线Y坐标
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // OpenGL渲染方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * OpenGL绘制回调
     * 按层次顺序渲染各个元素
     *
     * @param canvas OpenGL画布
     */
    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        if (canMove) {
            // 可移动模式：绘制所有元素
            drawBackColor(canvas);
            drawLine(canvas);
            drawPic(canvas);
            drawIconT(canvas);
            drawContent(canvas);
        } else {
            // 不可移动模式：不绘制T图标
            drawBackColor(canvas);
            drawLine(canvas);
            drawPic(canvas);
            drawContent(canvas);
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

    /**
     * 绘制时间轴基线
     *
     * @param canvas OpenGL画布
     */
    private void drawLine(ICanvasGL canvas) {
        paint.setColor(passagewayColor);
        // 绘制水平基线，从左侧空白到右侧空白
        canvas.drawLine(spaceWidth + 1, height / 2, width - spaceWidth, height / 2, paint);
    }

    /** 触发时刻文本的显示区域 */
    private final Rect triggerRect = new Rect();

    /**
     * 绘制内容文本
     * 包括存储深度、采样类型、采样率、分段存储和触发时刻
     *
     * @param canvas OpenGL画布
     */
    private synchronized void drawContent(ICanvasGL canvas) {
        // 清除上半部分区域（存储深度、采样类型、采样率）
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(backColor);
        contentCanvas.drawRect(0,0,contentCanvas.getWidth(),28,mPaint);

        // 清除下半部分区域（分段存储、触发时刻）
        contentCanvas.drawRect(0,48,contentCanvas.getWidth(),contentCanvas.getHeight(),mPaint);

        // 绘制存储深度（左上角）
        if (memoryDepthVisible==true) {
            mPaint.getTextBounds(content1, 0, content1.length(), ContentRect);
            mPaint.setColor(getResources().getColor(R.color.textColor));
            contentCanvas.drawText(content1, 10, 20, mPaint);
        }

        // 绘制采样类型（中间）
        mPaint.getTextBounds(content2, 0, content2.length(), ContentRect);
        mPaint.setColor(getResources().getColor(R.color.textColor));
        contentCanvas.drawText(content2, (contentBitmap.getWidth() - ContentRect.width()) / 2 - 3, 20, mPaint);

        // 绘制采样率（右上角）
        if (sampleRateVisible==true) {
            mPaint.getTextBounds(content3, 0, content3.length(), ContentRect);
            mPaint.setColor(getResources().getColor(R.color.textColor));
            contentCanvas.drawText(content3, contentBitmap.getWidth() - ContentRect.width() - 10, 20, mPaint);
        }

        // 可移动模式下绘制分段存储和触发时刻
        if (canMove){
            if (segmentVisible) {
                // 分段存储显示时，与时刻左右分散显示
                // 绘制分段存储（左下角）
                mPaint.getTextBounds(content4, 0, content4.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content4, 10, contentBitmap.getHeight()-2, mPaint);

                // 绘制触发时刻（右下角）
                mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content5, contentBitmap.getWidth() - ContentRect.width() - 10, contentBitmap.getHeight()-4, mPaint);

                // 计算触发时刻文本的显示区域（用于触摸检测）
                triggerRect.left  =  contentBitmap.getWidth() - ContentRect.width() - 10 + ContentRect.left;
                triggerRect.top = contentBitmap.getHeight()-4 + ContentRect.top;
                triggerRect.right = contentBitmap.getWidth() - ContentRect.width() - 10 + ContentRect.right;
                triggerRect.bottom = contentBitmap.getHeight()-4 + ContentRect.bottom;
            }else {
                // 分段存储不显示时，时刻居中显示
                mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content5, ((width-ContentRect.width())/2), contentBitmap.getHeight()-4, mPaint);

                // 计算触发时刻文本的显示区域
                triggerRect.left = ((width - ContentRect.width()) / 2) + ContentRect.left;
                triggerRect.top = contentBitmap.getHeight() - 4 + ContentRect.top;
                triggerRect.right = ((width - ContentRect.width()) / 2) + ContentRect.right;
                triggerRect.bottom = contentBitmap.getHeight() - 4 + ContentRect.bottom;
            }
        }

        // 刷新纹理并绘制位图
        canvas.invalidateTextureContent(contentBitmap,null);
        canvas.drawBitmap(contentBitmap, 0, 0);
    }

    /**
     * 绘制图形元素（括号和尖角标记）
     *
     * @param canvas OpenGL画布
     */
    private void drawPic(ICanvasGL canvas) {
        // 获取当前参数
        float tipPercent = this.tipPercent;
        float bracketMultiple = this.bracketMultiple * this.normalBracketMultiple;

        // 如果T图标不可见，使用默认参数
        if (!iconTVisible) {
            tipPercent = 0.5f;
            bracketMultiple = 1;
        }

        int barWidth = width - spaceWidth * 2;

        // 计算尖角标记左侧宽度
        float tipLeftWidth = barWidth * tipPercent;

        // 绘制左括号
        int lWidth = (int) (tipLeftWidth - barWidth / 2 / bracketMultiple + spaceWidth + 1);
        if (lWidth >= -bracketWidth) {
            paint.setColor(bracketColor);
            // 上横线
            canvas.drawLine(lWidth, height / 2 - bracketWidth, lWidth + bracketWidth - 1, height / 2 - bracketWidth, paint);
            // 下横线
            canvas.drawLine(lWidth - 1, height / 2 + bracketWidth, lWidth + bracketWidth - 1, height / 2 + bracketWidth, paint);
            // 竖线
            canvas.drawLine(lWidth, height / 2 - bracketWidth, lWidth, height / 2 + bracketWidth, paint);
        }

        // 绘制右括号
        int rWidth = (int) (tipLeftWidth + barWidth / 2 / bracketMultiple + spaceWidth);
        if (lWidth <= spaceWidth * 2 + barWidth + bracketWidth) {
            paint.setColor(bracketColor);
            // 上横线
            canvas.drawLine(rWidth, height / 2 - bracketWidth, rWidth - bracketWidth, height / 2 - bracketWidth, paint);
            // 下横线
            canvas.drawLine(rWidth, height / 2 + bracketWidth, rWidth - bracketWidth, height / 2 + bracketWidth, paint);
            // 竖线
            canvas.drawLine(rWidth, height / 2 - bracketWidth, rWidth, height / 2 + bracketWidth, paint);
        }

        // 绘制尖角标记
        int place = (int) tipLeftWidth - tipWidth / 2 + spaceWidth;
        if (place >= tipWidth && place <= spaceWidth * 2 + barWidth + tipWidth) {
            canvas.drawBitmap(bitmap_tip, place-1, (height-bitmap_tip.getHeight())/2);
        }
    }

    /**
     * 绘制触发T图标
     *
     * @param canvas OpenGL画布
     */
    private void drawIconT(ICanvasGL canvas) {
        if (iconTVisible) {
            int barWidth = width - spaceWidth * 2;

            // 计算T图标的X位置
            float tWidth = spaceWidth + barWidth * iconTPercent - iconTBitMap.getWidth() / 2;

            // 限制在控件范围内
            if (tWidth < 1) {
                tWidth = 1;
            } else if (tWidth > width - spaceWidth - iconTBitMap.getWidth() / 2) {
                tWidth = width - spaceWidth - iconTBitMap.getWidth() / 2;
            }

            // 绘制T图标，垂直居中
            canvas.drawBitmap(iconTBitMap, (int) tWidth-1, (height-iconTBitMap.getHeight())/2);
        }
    }

    /**
     * 获取触发时刻文本的显示区域
     *
     * @return 触发时刻文本的矩形区域
     */
    public Rect getTriggerRect() {
        return triggerRect;
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
