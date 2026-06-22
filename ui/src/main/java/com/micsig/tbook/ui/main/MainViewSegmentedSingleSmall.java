package com.micsig.tbook.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                          分段单次小视图（水平滚动）
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 分段存储模式下的小尺寸帧选择控件，水平滚动显示帧列表，支持播放、暂停、手动滑动
 * 选择帧，使用OpenGL ES渲染。
 *
 * 【核心职责】
 * 1. 水平滚动显示帧列表（显示9帧）
 * 2. 支持播放/暂停模式切换
 * 3. 支持手动滑动选择帧
 * 4. 支持多种播放速度（1X/2X/4X/8X）
 * 5. 支持正向/反向播放
 * 6. 使用OpenGL ES高效渲染
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                 MainViewSegmentedSingleSmall                    │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  渲染层: GLTextureView / onGLDraw() / draw()                   │
 * │  播放层: isPlay / isPlayOrder / curPlaySpeed                   │
 * │  触摸层: onTouchEvent() / stopTouchEvent() / playTouchEvent()  │
 * │  数据层: list<SegmentedSingleBean> / curFrame                  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * setList() → 数据加载 → onGLDraw() → OpenGL渲染 → 显示帧列表
 *
 * 【状态转换图】
 * ┌──────────────┐     setPlay(true)      ┌──────────────┐
 * │   停止状态   │ ─────────────────────> │   播放状态   │
 * │  stopDraw()  │                        │  playDraw()  │
 * └──────────────┘ <───────────────────── └──────────────┘
 *                  setPlay(false)
 *
 * 【播放速度定义】
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  速度常量      │  值   │  说明                                   │
 * ├──────────────────────────────────────────────────────────────────┤
 * │  PLAYSPEEP_1X  │  2    │  1倍速播放                              │
 * │  PLAYSPEEP_2X  │  4    │  2倍速播放                              │
 * │  PLAYSPEEP_4X  │  8    │  4倍速播放                              │
 * │  PLAYSPEEP_8X  │  16   │  8倍速播放                              │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ GLTextureView│────>│ OpenGL ES渲染        │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────────┐ ┌──────────────────────┐
 * │SegmentedSingleBean│─>│ 帧数据模型          │
 * └──────────────────┘ └──────────────────────┘
 *
 * 【使用示例】
 * MainViewSegmentedSingleSmall view = new MainViewSegmentedSingleSmall(context);
 * view.setList(frameList);           // 设置帧列表
 * view.setCurFrame(10);              // 设置当前帧
 * view.setPlay(true);                // 开始播放
 * view.setPlaySpeed(PLAYSPEEP_2X);   // 设置2倍速
 * view.setOnEvents(listener);        // 设置事件监听
 *
 * 【注意事项】
 * 1. 显示帧数固定为9帧（SHOWCOUNT=9）
 * 2. 帧宽度160像素，高度60像素
 * 3. 播放时使用帧率控制（约60fps）
 * 4. 触摸事件在播放和停止状态下有不同的处理逻辑
 *
 * @author Liwb
 * @since 2020/5/27 9:21
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainViewSegmentedSingleSmall extends GLTextureView {
    // ═════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═════════════════════════════════════════════════════════════════════════════

    private static final String TAG = MainViewSegmentedSingleSmall.class.getSimpleName();

    /** 显示帧数量 */
    private static final int SHOWCOUNT = 9;

    /** 显示帧数量的半数（用于计算中心帧） */
    private static final int SHOWCOUNTHALF = (SHOWCOUNT - 1) / 2;

    /** 基础播放速度 */
    private static final int BaseSpeed = 2;

    /** 1倍速播放 */
    public static final int PLAYSPEEP_1X = (int) (1 * BaseSpeed);

    /** 2倍速播放 */
    public static final int PLAYSPEEP_2X = (int) (2 * BaseSpeed);

    /** 4倍速播放 */
    public static final int PLAYSPEEP_4X = (int) (4 * BaseSpeed);

    /** 8倍速播放 */
    public static final int PLAYSPEEP_8X = (int) (8 * BaseSpeed);

    /**
     * 播放速度注解
     * 用于编译时类型检查，限制参数只能是预定义的速度常量
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAYSPEEP_1X, PLAYSPEEP_2X, PLAYSPEEP_4X, PLAYSPEEP_8X})
    @interface PLAYSPEED {
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 状态
    // ═════════════════════════════════════════════════════════════════════════════

    /** 是否正在播放 */
    private boolean isPlay = false;

    /** 是否处于播放操作模式（播放时触摸滑动） */
    private boolean isPlayOption = false;

    /** 播放顺序（true正向，false反向） */
    private boolean isPlayOrder = false;

    /** 可见性状态 */
    private int isVisible = View.GONE;

    /** 当前播放速度 */
    private int curPlaySpeed = PLAYSPEEP_1X;

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 绘制
    // ═════════════════════════════════════════════════════════════════════════════

    /** 绘制画笔 */
    private Paint paint;

    /** 单帧宽度 */
    private int width, height;

    /** 内边距 */
    private int padding;

    /** 当前帧索引 */
    private int curFrame;

    /** X轴偏移量（用于滑动效果） */
    private int offsetX;

    /** 文本颜色 */
    private int textColor;

    /** 选中帧边框颜色 */
    private int kuangColor,noSelectKuangColor;

    /** 渲染位图 */
    private Bitmap bitmap;

    /** CPU画布（用于预绘制） */
    private Canvas canvas;

    /** 清除模式（用于擦除位图） */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    /** 源模式（用于绘制） */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    /** 是否需要重绘 */
    private boolean isRedraw = false;

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 数据
    // ═════════════════════════════════════════════════════════════════════════════

    /** 事件回调接口 */
    private OnEvents onEvents;

    /** 帧数据列表 */
    private List<SegmentedSingleBean> list = new ArrayList<>();

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 刷新渲染
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 请求OpenGL渲染一帧。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void onRefresh(){
        requestRender();
    }

    /**
     * 获取事件回调接口
     * @return 事件回调接口
     */
    public OnEvents getOnEvents() {
        return onEvents;
    }

    /**
     * 设置事件回调接口
     * @param onEvents 事件回调接口
     */
    public void setOnEvents(OnEvents onEvents) {
        this.onEvents = onEvents;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 回调接口定义
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 事件回调接口
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 定义帧选择控件的事件回调方法。
     *
     * 【回调方法】
     * - onClick: 点击当前帧
     * - onCurrFrameChange: 当前帧变化
     * - onOrderChange: 播放顺序变化
     * - onVisibleChange: 可见性变化
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public interface OnEvents {
        /**
         * 点击帧回调
         * @param curFrame 点击的帧索引
         */
        void onClick(int curFrame);

        /**
         * 当前帧变化回调
         * @param currFrame 新的当前帧索引
         */
        void onCurrFrameChange(int currFrame);

        /**
         * 播放顺序变化回调
         * @param order true正向，false反向
         */
        void onOrderChange(boolean order);

        /**
         * 可见性变化回调
         * @param visibility 可见性状态
         */
        void onVisibleChange(int visibility);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 单参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainViewSegmentedSingleSmall实例，仅传入Context。
     *
     * 【参数说明】
     * @param context 上下文对象
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainViewSegmentedSingleSmall(Context context) {
        this(context, null);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - XML属性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainViewSegmentedSingleSmall实例，支持XML属性。
     *
     * 【参数说明】
     * @param context 上下文对象
     * @param attrs   XML属性集
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainViewSegmentedSingleSmall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 完整参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainViewSegmentedSingleSmall实例，初始化所有资源。
     *
     * 【参数说明】
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     *
     * 【初始化流程】
     * 1. 设置帧尺寸（160x60像素）
     * 2. 创建画笔和位图
     * 3. 加载颜色资源
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainViewSegmentedSingleSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置单帧宽度
        width = 160;
        // 设置单帧高度
        height = 60;
        // 设置内边距
        padding = 5;
        // 设置默认当前帧（测试值）
        curFrame = 125678;
        // 加载文本颜色
        textColor = getResources().getColor(R.color.textColorNewTopViewEnable);
        // 加载选中帧边框颜色
        kuangColor = getResources().getColor(R.color.textColorCenterSegment);
        // 加载非选中帧边框颜色
        noSelectKuangColor=getResources().getColor(R.color.frame_color);
        // 创建画笔
        paint = new Paint();
        // 启用抗锯齿
        paint.setAntiAlias(true);
        // 创建渲染位图（宽度=帧宽度*显示数量）
        bitmap = Bitmap.createBitmap(width * SHOWCOUNT, height, Bitmap.Config.ARGB_8888);
        // 创建CPU画布
        canvas = new Canvas(bitmap);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // View生命周期方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置可见性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 重写setVisibility方法，同步更新内部可见性状态并触发回调。
     *
     * 【参数说明】
     * @param visibility 可见性状态（View.VISIBLE/GONE/INVISIBLE）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        // 同步内部可见性状态
        isVisible = visibility;
        // 触发可见性变化回调
        if (onEvents != null) onEvents.onVisibleChange(visibility);
        // 请求渲染
        onRefresh();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // OpenGL渲染方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * OpenGL绘制方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * OpenGL ES渲染回调方法，根据播放状态选择不同的绘制逻辑。
     *
     * 【参数说明】
     * @param canvas OpenGL画布
     *
     * 【绘制逻辑】
     * - 不可见时直接返回
     * - 播放状态：调用playDraw()
     * - 停止状态：调用stopDraw()
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        // 不可见时不绘制
        if (isVisible == View.GONE) return;

        if (isPlay && isPlayOption == false) {
            // 播放状态且非触摸操作模式
            playDraw(canvas);
        } else {
            // 停止状态或触摸操作模式
            stopDraw(canvas);
        }

    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理
    // ═════════════════════════════════════════════════════════════════════════════

    /** 触摸起始X坐标 */
    private int startX;

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 触摸事件处理
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理触摸事件，根据播放状态选择不同的触摸处理逻辑。
     *
     * 【参数说明】
     * @param event 触摸事件
     *
     * 【返回值】
     * @return true表示消费事件
     *
     * 【处理逻辑】
     * - 播放状态：调用playTouchEvent()
     * - 停止状态：调用stopTouchEvent()
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPlay || isPlayOption) {
            // 播放状态或播放操作模式
            playTouchEvent(event);
        } else {
            // 停止状态
            stopTouchEvent(event);
        }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 停止状态绘制方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 停止状态绘制
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 在停止状态下绘制帧列表到OpenGL画布。
     *
     * 【参数说明】
     * @param canvas OpenGL画布
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void stopDraw(ICanvasGL canvas) {
        synchronized (bitmap) {
            // 如果需要重绘，通知OpenGL更新纹理
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            // 绘制位图到OpenGL画布
            canvas.drawBitmap(bitmap, 0, 0);
            // 重置重绘标志
            isRedraw = false;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 绘制帧列表
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 绘制帧列表到CPU画布，支持水平偏移效果。
     *
     * 【参数说明】
     * @param offsetX X轴偏移量（像素）
     *
     * 【绘制流程】
     * 1. 清除画布
     * 2. 遍历绘制9帧
     * 3. 计算帧索引（支持循环）
     * 4. 绘制帧边框和文本
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private synchronized void draw(int offsetX) {
        // 检查数据有效性
        if (list == null || list.size() == 0) return;
        synchronized (bitmap) {
            // 设置清除模式
            paint.setXfermode(clearMode);
            // 清除画布
            this.canvas.drawPaint(paint);
            // 设置源模式
            paint.setXfermode(srcMode);

            // 遍历绘制9帧
            for (int i = 0; i < SHOWCOUNT; i++) {
                // 计算帧的X偏移
                int offset = (i - SHOWCOUNTHALF) * width + offsetX;
                // 计算帧索引（中心帧为curFrame）
                int idx = curFrame + (i - SHOWCOUNTHALF);

                // 处理循环索引（负数和超出范围）
                while (idx < 0 && list.size() != 0) {
                    idx = list.size() + idx;
                }
                while (idx >= list.size() && list.size() != 0) {
                    idx = idx - list.size();
                }

                // 获取帧数据
                String curText = String.valueOf(list.get(idx).getFrameId());
                String curTime = list.get(idx).getTimeMs();

                // 绘制帧背景边框
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(noSelectKuangColor);
                this.canvas.drawRect(offset, 0, width + offset , height, paint);

                // 绘制帧内部黑色背景
                paint.setColor(Color.BLACK);
                this.canvas.drawRect(offset+1, 0+1, width + offset-2 , height-2, paint);

                // 绘制帧ID文本
                paint.setColor(textColor);
                paint.setTextSize(24);
                paint.setStyle(Paint.Style.FILL);
                this.canvas.drawText(curText, width / 2 - getTextWidth(curText) / 2 + offset, height / 2 - 5, paint);

                // 绘制帧时间文本
                paint.setTextSize(20);
                this.canvas.drawText(curTime, width / 2 - getTextWidth(curTime) / 2 + offset, height - padding - 5, paint);
            }
            // 设置重绘标志
            isRedraw = true;
            // 请求渲染
            onRefresh();
        }
    }

    /**
     * 停止状态绘制入口
     */
    private void onStopDraw() {
        draw(this.offsetX);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 停止状态触摸处理
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理停止状态下的触摸事件，支持滑动选择帧和点击事件。
     *
     * 【参数说明】
     * @param event 触摸事件
     *
     * 【处理逻辑】
     * - ACTION_DOWN: 记录起始位置
     * - ACTION_MOVE: 更新偏移量并重绘
     * - ACTION_UP: 计算帧偏移并更新当前帧
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void stopTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录触摸起始位置
                startX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算X偏移量
                offsetX = (int) (event.getX() - startX);
                // 重绘
                onStopDraw();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 计算最终偏移量
                offsetX = (int) (event.getX() - startX);
                if (offsetX == 0) {
                    // 点击事件
                    if (onEvents != null) {
                        onEvents.onClick(curFrame);
                    }
                } else {
                    // 滑动事件，计算帧偏移
                    int offsetFrame;
                    if (offsetX > 0) {
                        // 向右滑动（向前翻帧）
                        if (offsetX % width > offsetX / 2) {
                            offsetFrame = offsetX / width + 1;
                        } else {
                            offsetFrame = offsetX / width;
                        }
                    } else {
                        // 向左滑动（向后翻帧）
                        offsetX *= -1;
                        if (offsetX % width > offsetX / 2) {
                            offsetFrame = offsetX / width + 1;
                        } else {
                            offsetFrame = offsetX / width;
                        }
                        offsetX *= -1;
                        offsetFrame *= -1;
                    }
                    // 重置偏移量
                    offsetX = 0;
                    // 更新当前帧
                    curFrame -= offsetFrame;
                    curFrame = checkCurFrame(curFrame);
                    // 触发帧变化回调
                    if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                    // 重绘
                    onStopDraw();
                }
                break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 播放状态方法
    // ═════════════════════════════════════════════════════════════════════════════

    /** 播放偏移量 */
    private int playOffset = 0;

    /** 上次播放时间戳 */
    private long playts = 0;

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 播放状态绘制
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 在播放状态下自动滚动绘制帧列表。
     *
     * 【参数说明】
     * @param canvas OpenGL画布
     *
     * 【播放逻辑】
     * 1. 计算每帧偏移量（基于播放速度）
     * 2. 帧率控制（约60fps）
     * 3. 滚动到下一帧时更新curFrame
     * 4. 支持正向/反向播放
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void playDraw(ICanvasGL canvas) {
        // 计算每帧偏移量（基于60fps）
        int offset = (int) (width * curPlaySpeed / 60);

        // 帧率控制
        long ts = SystemClock.elapsedRealtime();
        if((ts - playts) < 15){
            offset = 0;
        }else {
            playts = ts;
        }

        // 累加播放偏移量
        playOffset += offset;
        if (isPlayOrder) {
            // 反向播放
            draw(-playOffset);
        } else {
            // 正向播放
            draw(playOffset);
        }

        synchronized (bitmap) {
            // 更新纹理并绘制
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            canvas.drawBitmap(bitmap, 0, 0);
            isRedraw = false;
        }

        // 滚动到下一帧
        if (playOffset >= width) {
            playOffset -= width;
            if (isPlayOrder) {
                // 反向播放：帧索引递增
                curFrame++;
                curFrame = checkCurFrame(curFrame);
            } else {
                // 正向播放：帧索引递减
                curFrame--;
                curFrame = checkCurFrame(curFrame);
            }
            // 触发帧变化回调
            if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 播放状态触摸处理
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理播放状态下的触摸事件，支持暂停滑动和改变播放方向。
     *
     * 【参数说明】
     * @param event 触摸事件
     *
     * 【处理逻辑】
     * - ACTION_DOWN: 进入播放操作模式
     * - ACTION_MOVE: 更新偏移量
     * - ACTION_UP: 更新播放方向和当前帧
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void playTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // 记录起始位置
                startX = (int) event.getX();
                // 进入播放操作模式
                isPlayOption = true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                // 更新偏移量
                offsetX = (int) (event.getX() - startX);
                // 重绘
                onStopDraw();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                // 计算最终偏移量
                offsetX = (int) (event.getX() - startX);
                // 根据滑动方向设置播放顺序
                setPlayOrder(offsetX > 0 ? false : true);
                if (onEvents != null) onEvents.onOrderChange(offsetX > 0 ? false : true);

                // 计算帧偏移
                int offsetFrame;
                if (offsetX > 0) {
                    if (offsetX % width > offsetX / 2) {
                        offsetFrame = offsetX / width + 1;
                    } else {
                        offsetFrame = offsetX / width;
                    }
                } else {
                    offsetX *= -1;
                    if (offsetX % width > offsetX / 2) {
                        offsetFrame = offsetX / width + 1;
                    } else {
                        offsetFrame = offsetX / width;
                    }
                    offsetX *= -1;
                    offsetFrame *= -1;
                }
                // 重置偏移量
                offsetX = 0;
                // 更新当前帧
                curFrame -= offsetFrame;
                curFrame = checkCurFrame(curFrame);
                if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                // 重绘
                onStopDraw();
                // 退出播放操作模式
                isPlayOption = false;


            }
            break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═════════════════════════════════════════════════════════════════════════════

    /** 文本边界矩形 */
    private Rect rect = new Rect();

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 获取文本宽度
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 计算文本的像素宽度。
     *
     * 【参数说明】
     * @param text 文本字符串
     *
     * 【返回值】
     * @return 文本宽度（像素）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 检查并修正帧索引
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 检查帧索引是否在有效范围内，超出范围时进行循环修正。
     *
     * 【参数说明】
     * @param curFrame 待检查的帧索引
     *
     * 【返回值】
     * @return 修正后的帧索引
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private int checkCurFrame(int curFrame) {
        if (curFrame >= list.size()) {
            // 超出范围，取模循环
            curFrame = curFrame % list.size();
        } else if (curFrame < 0) {
            // 负数，从末尾循环
            curFrame = list.size() + curFrame;
        }
        return curFrame;
    }

    /**
     * 获取当前帧索引
     * @return 当前帧索引
     */
    public int getCurFrame() {
        return curFrame;
    }

    /**
     * 获取当前帧数据
     * @return 当前帧的SegmentedSingleBean，如果无效返回null
     */
    public SegmentedSingleBean getCurBean() {
        if (list != null && curFrame < list.size()) {
            return list.get(curFrame);
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 动画Handler
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 帧切换动画Handler
     * 用于实现平滑的帧切换动画效果
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 10) {
                // 动画结束，绘制最终状态
                draw(0);
                return;
            }
            // 计算当前动画偏移
            int x = ((int) msg.obj) * msg.what;
            draw(x);
            // 发送下一帧动画消息
            Message msg1 = handler.obtainMessage();
            msg1.what = msg.what + 1;
            msg1.obj = msg.obj;
            handler.sendMessageDelayed(msg1, 20);
        }
    };

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置当前帧（带动画）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置当前帧索引，并播放平滑切换动画。
     *
     * 【参数说明】
     * @param curFrame 新的帧索引
     *
     * 【动画逻辑】
     * 1. 检查帧索引是否变化
     * 2. 计算动画步长
     * 3. 发送动画消息序列
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setCurFrame(int curFrame) {
        // 检查帧索引是否变化
        if (curFrame == this.curFrame) {
            return;
        }
        // 修正帧索引
        curFrame = checkCurFrame(curFrame);
        // 计算动画偏移量
        int offsetCount = 5 * width;
        if (Math.abs(curFrame - this.curFrame) < 5) {
            offsetCount = Math.abs(this.curFrame - curFrame) * width;
        }
        // 计算动画步长
        int step = this.curFrame > curFrame ? offsetCount / 10 : -offsetCount / 10;
        this.curFrame = curFrame;

        // 发送动画消息
        Message msg = Message.obtain();
        msg.obj = step;
        msg.what = 1;
        handler.sendMessage(msg);


    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 属性Getter/Setter方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 获取播放状态
     * @return true表示正在播放
     */
    public boolean isPlay() {
        return isPlay;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置播放状态
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置播放状态，停止时重置绘制状态。
     *
     * 【参数说明】
     * @param play true开始播放，false停止播放
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setPlay(boolean play) {
        if (isPlay != play) {
            isPlay = play;
            if (!isPlay) {
                // 停止时重置绘制
                draw(0);
            }

        }
        onRefresh();
    }

    /**
     * 获取播放顺序
     * @return true正向，false反向
     */
    public boolean isPlayOrder() {
        return isPlayOrder;
    }

    /**
     * 设置播放顺序
     * @param order true正向，false反向
     */
    public void setPlayOrder(boolean order) {
        isPlayOrder = order;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置播放速度
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置播放速度。
     *
     * 【参数说明】
     * @param playSpeed 播放速度（PLAYSPEEP_1X/2X/4X/8X）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setPlaySpeed(@PLAYSPEED int playSpeed) {
        curPlaySpeed = playSpeed;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置帧数据列表
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置帧数据列表，如果帧数不足则循环填充。
     *
     * 【参数说明】
     * @param list 帧数据列表
     *
     * 【处理逻辑】
     * 1. 清空当前列表
     * 2. 添加新数据
     * 3. 如果帧数小于SHOWCOUNT，循环填充
     * 4. 重置当前帧索引
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public synchronized void setList(List<SegmentedSingleBean> list) {
        // 清空当前列表
        this.list.clear();
        // 添加新数据
        this.list.addAll(list);
        // 重置当前帧索引
        if (curFrame >= list.size()) curFrame = 0;

        // 如果帧数不足，循环填充
        if (list.size() < SHOWCOUNT) {
            List<SegmentedSingleBean> list1 = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                list1.add(list.get(i));
            }
            // 计算需要填充的次数
            int t = ((int) Math.ceil(SHOWCOUNT / (float) list.size()));
            for (int i = 0; i < t; i++) {
                this.list.addAll(list1);
            }
        }
        // 重绘
        onStopDraw();
    }

    /**
     * 字符串转换
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "MainViewSegmentedSingleSmall{" +
                "isPlay=" + isPlay +
                ", isPlayOption=" + isPlayOption +
                ", isPlayOrder=" + isPlayOrder +
                ", curPlaySpeed=" + curPlaySpeed +
                ", width=" + width +
                ", height=" + height +
                ", curFrame=" + curFrame +
                '}';
    }

}
