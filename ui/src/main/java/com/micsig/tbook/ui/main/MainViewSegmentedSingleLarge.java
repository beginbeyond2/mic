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
import android.view.VelocityTracker;
import android.view.View;

import androidx.annotation.IntDef;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                          分段单次大视图（垂直滚动）
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 分段存储模式下的大尺寸帧选择控件，垂直滚动显示帧列表，支持播放、暂停、手动滑动
 * 选择帧，使用OpenGL ES渲染。相比小视图，大视图显示更多帧（27帧），支持惯性滑动。
 *
 * 【核心职责】
 * 1. 垂直滚动显示帧列表（显示27帧）
 * 2. 支持播放/暂停模式切换
 * 3. 支持手动滑动选择帧，带惯性效果
 * 4. 支持多种播放速度（1X/2X/4X/8X）
 * 5. 支持正向/反向播放
 * 6. 使用OpenGL ES高效渲染
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                 MainViewSegmentedSingleLarge                    │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  渲染层: GLTextureView / onGLDraw() / draw()                   │
 * │  播放层: isPlay / isPlayOrder / curPlaySpeed                   │
 * │  触摸层: onTouchEvent() / onStopTouchEvent() / onPlayTouchEvent│
 * │  数据层: list<SegmentedSingleBean> / curFrame                  │
 * │  惯性层: VelocityTracker / maxVelocity                         │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * setList() → 数据加载 → onGLDraw() → OpenGL渲染 → 显示帧列表
 *
 * 【与小视图的区别】
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  特性          │  小视图(Small)     │  大视图(Large)           │
 * ├──────────────────────────────────────────────────────────────────┤
 * │  显示帧数      │  9帧               │  27帧                    │
 * │  滚动方向      │  水平              │  垂直                    │
 * │  惯性滑动      │  不支持            │  支持(VelocityTracker)   │
 * │  点击选择      │  仅中心帧          │  可点击任意可见帧        │
 * │  帧尺寸        │  160x60            │  160x60                  │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ GLTextureView│────>│ OpenGL ES渲染        │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────────┐ ┌──────────────────────┐
 * │SegmentedSingleBean│─>│ 帧数据模型          │
 * └──────────────────┘ └──────────────────────┘
 * ┌──────────────┐     ┌──────────────────────┐
 * │VelocityTracker│────>│ 惯性滑动支持        │
 * └──────────────┘     └──────────────────────┘
 *
 * 【使用示例】
 * MainViewSegmentedSingleLarge view = new MainViewSegmentedSingleLarge(context);
 * view.setList(frameList);           // 设置帧列表
 * view.setCurFrame(10);              // 设置当前帧
 * view.setPlay(true);                // 开始播放
 * view.setPlaySpeed(PLAYSPEEP_2X);   // 设置2倍速
 * view.setOnEvents(listener);        // 设置事件监听
 *
 * 【注意事项】
 * 1. 显示帧数固定为27帧（SHOWCOUNT=27）
 * 2. 帧宽度160像素，高度60像素
 * 3. 支持惯性滑动，最大速度8000
 * 4. 点击中心帧区域触发onClick回调
 * 5. 点击其他帧区域直接跳转到该帧
 *
 * @author Liwb
 * @since 2020/5/28 10:57
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainViewSegmentedSingleLarge extends GLTextureView {
    // ═════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═════════════════════════════════════════════════════════════════════════════

    private static final String TAG = MainViewSegmentedSingleLarge.class.getSimpleName();

    /** 显示帧数量 */
    private static final int SHOWCOUNT = 27;

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

    /** Y轴偏移量（用于滑动效果） */
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

    /** 速度追踪器（用于惯性滑动） */
    private VelocityTracker vTracker = null;

    /** 触摸点ID */
    private int pointerId;

    /** 最大滑动速度 */
    private int maxVelocity = 8000;

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 刷新渲染
     */
    public void onRefresh(){
        requestRender();
    }

    /**
     * 获取事件回调接口
     */
    public OnEvents getOnEvents() {
        return onEvents;
    }

    /**
     * 设置事件回调接口
     */
    public void setOnEvents(OnEvents onEvents) {
        this.onEvents = onEvents;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 回调接口定义
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 事件回调接口
     */
    public interface OnEvents {
        void onClick(int curFrame);
        void onCurrFrameChange(int currFrame);
        void onOrderChange(boolean order);
        void onVisibleChange(int visibility);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    public MainViewSegmentedSingleLarge(Context context) {
        this(context, null);
    }

    public MainViewSegmentedSingleLarge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 完整参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainViewSegmentedSingleLarge实例，初始化所有资源。
     *
     * 【初始化流程】
     * 1. 设置帧尺寸（160x60像素）
     * 2. 创建画笔和位图（高度=帧高度*27）
     * 3. 加载颜色资源
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainViewSegmentedSingleLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置单帧宽度
        width = 160;
        // 设置单帧高度（总高度=height*9用于显示区域）
        height = 60;
        // 设置内边距
        padding = 10;
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
        // 创建渲染位图（高度=帧高度*显示数量）
        bitmap = Bitmap.createBitmap(width, height * SHOWCOUNT, Bitmap.Config.ARGB_8888);
        // 创建CPU画布
        canvas = new Canvas(bitmap);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // View生命周期方法
    // ═════════════════════════════════════════════════════════════════════════════

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        isVisible = visibility;
        if (onEvents != null) onEvents.onVisibleChange(visibility);
        onRefresh();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // OpenGL渲染方法
    // ═════════════════════════════════════════════════════════════════════════════

    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        if (isVisible == View.GONE) return;

        if (isPlay && isPlayOption == false) {
            playDraw(canvas);
        } else {
            stopDraw(canvas);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理
    // ═════════════════════════════════════════════════════════════════════════════

    /** 触摸起始Y坐标 */
    private int startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPlay || isPlayOption) {
            onPlayTouchEvent(event);
        } else {
            onStopTouchEvent(event);
        }
        return true;
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
     * 在播放状态下自动滚动绘制帧列表（垂直方向）。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void playDraw(ICanvasGL canvas) {
        // 计算每帧偏移量（基于60fps）
        int offset = (int) (height * curPlaySpeed / 60.0f);

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
            draw(-playOffset);
        } else {
            draw(playOffset);
        }

        synchronized (bitmap) {
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            canvas.drawBitmap(bitmap, 0, 0);
            isRedraw = false;
        }

        // 滚动到下一帧
        if (playOffset >= height) {
            playOffset -= height;
            if (isPlayOrder) {
                curFrame++;
                curFrame = checkCurFrame(curFrame);
            } else {
                curFrame--;
                curFrame = checkCurFrame(curFrame);
            }
            if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
        }
    }

    /**
     * 停止状态绘制
     */
    private void stopDraw(ICanvasGL canvas) {
        synchronized (bitmap) {
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            canvas.drawBitmap(bitmap, 0, 0);
            isRedraw = false;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 播放状态触摸处理
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理播放状态下的触摸事件，支持暂停滑动和改变播放方向。
     * 注意：此处使用Y坐标计算偏移。
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void onPlayTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                startY = (int) event.getY();
                isPlayOption = true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                // 使用Y坐标计算偏移（垂直滚动）
                offsetX = (int) (event.getY() - startY);
                onStopDraw();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                offsetX = (int) (event.getY() - startY);
                setPlayOrder(offsetX > 0 ? false : true);
                if (onEvents != null) onEvents.onOrderChange(offsetX > 0 ? false : true);

                int offsetFrame;
                if (offsetX > 0) {
                    if (offsetX % height > offsetX / 2) {
                        offsetFrame = offsetX / height + 1;
                    } else {
                        offsetFrame = offsetX / height;
                    }
                } else {
                    offsetX *= -1;
                    if (offsetX % height > offsetX / 2) {
                        offsetFrame = offsetX / height + 1;
                    } else {
                        offsetFrame = offsetX / height;
                    }
                    offsetX *= -1;
                    offsetFrame *= -1;
                }
                offsetX = 0;
                curFrame -= offsetFrame;
                curFrame = checkCurFrame(curFrame);
                if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                onStopDraw();
                isPlayOption = false;

            }
            break;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 停止状态触摸处理
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理停止状态下的触摸事件，支持惯性滑动和点击选择帧。
     *
     * 【处理逻辑】
     * - ACTION_DOWN: 记录起始位置，初始化速度追踪器
     * - ACTION_MOVE: 更新偏移量，追踪速度
     * - ACTION_UP: 计算惯性偏移，处理点击事件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void onStopTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录触摸起始位置
                startY = (int) event.getY();
                // 初始化或重置速度追踪器
                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                } else {
                    vTracker.clear();
                }
                vTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算Y偏移量（垂直滚动）
                offsetX = (int) (event.getY() - startY);
                // 追踪速度
                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                } else {
                    vTracker.clear();
                }
                vTracker.addMovement(event);
                // 重绘
                onStopDraw();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 计算最终偏移量
                offsetX = (int) (event.getY() - startY);
                // 计算惯性速度
                vTracker.computeCurrentVelocity(1000, maxVelocity);
                float vX = vTracker.getXVelocity();
                float vY = vTracker.getYVelocity();
                // 应用惯性效果
                if (vY != 0) {
                    // 限制最大速度
                    vY = vY > height * 5 ? height * 5 : vY;
                    vY = vY < -height * 5 ? -height * 5 : vY;
                    offsetX += (int) vY;
                }
                // 回收速度追踪器
                if (vTracker != null) {
                    vTracker.clear();
                    vTracker.recycle();
                    vTracker = null;
                }

                if (offsetX == 0) {
                    // 点击事件
                    int y = (int) event.getY();
                    if (y > height * 4 && y < height * 5) {
                        // 点击中心帧区域
                        if (onEvents != null) {
                            onEvents.onClick(curFrame);
                        }
                    } else {
                        // 点击其他帧区域，直接跳转
                        int offsetFrame = y / height - 4;
                        int cur = this.curFrame;
                        curFrame += offsetFrame;
                        curFrame = checkCurFrame(curFrame);
                        animSetCurFrame(cur);
                        if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                    }
                } else {
                    // 滑动事件
                    int offsetFrame;
                    if (offsetX > 0) {
                        if (offsetX % height > offsetX / 2) {
                            offsetFrame = offsetX / height + 1;
                        } else {
                            offsetFrame = offsetX / height;
                        }
                    } else {
                        offsetX *= -1;
                        if (offsetX % height > offsetX / 2) {
                            offsetFrame = offsetX / height + 1;
                        } else {
                            offsetFrame = offsetX / height;
                        }
                        offsetX *= -1;
                        offsetFrame *= -1;
                    }
                    Logger.i(TAG,"offsetFrame:"+offsetFrame+",curFrame:"+curFrame);
                    offsetX = 0;
                    int cur = this.curFrame;
                    curFrame -= offsetFrame;
                    curFrame = checkCurFrame(curFrame);
                    animSetCurFrame(cur);
                    if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                }
                break;
        }
    }

    /**
     * 停止状态绘制入口
     */
    private void onStopDraw() {
        draw(this.offsetX);
    }

    /** 中心帧显示位置（Y坐标） */
    private  int SHOWPOSTION0 =60 *4;

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 绘制帧列表
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 绘制帧列表到CPU画布，支持垂直偏移效果。
     *
     * 【绘制流程】
     * 1. 清除画布
     * 2. 遍历绘制可见帧
     * 3. 计算帧索引（支持循环）
     * 4. 绘制帧背景和文本
     * 5. 绘制中心帧选中边框
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void draw(int offsetX) {
        if (list == null) return;
        synchronized (bitmap) {
            // 清除画布（使用背景色填充）
            canvas.drawColor(noSelectKuangColor);

            // 计算实际显示帧数
            int len = SHOWCOUNT;
            int lenHalf = SHOWCOUNTHALF;
            if (list.size() < len) {
                len = list.size();
                lenHalf = len / 2;
            }

            // 遍历绘制可见帧
            for (int i = 0; i < len; i++) {
                // 计算帧的Y偏移
                int offset = (i - lenHalf) * height + offsetX + SHOWPOSTION0;
                // 计算帧索引（中心帧为curFrame）
                int idx = curFrame + (i - lenHalf);

                // 处理循环索引
                while (idx < 0 && list.size() != 0) {
                    idx = list.size() + idx;
                }
                while (idx >= list.size() && list.size() != 0) {
                    idx = idx - list.size();
                }

                // 获取帧数据
                String curText = String.valueOf(list.get(idx).getFrameId());
                String curTime = list.get(idx).getTimeMs();

                // 绘制帧内部黑色背景
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.BLACK);
                this.canvas.drawRect(1, 1 + offset, width - 1, height + offset - 1, paint);

                // 绘制帧ID文本（中心帧使用特殊颜色）
                paint.setTextSize(24);
                paint.setStyle(Paint.Style.FILL);
                int y = height / 2 + offset - 5;
                if (y < height * 5 && y - getTextHeight(curText) > height * 4) {
                    // 中心帧区域
                    paint.setColor(kuangColor);
                } else {
                    paint.setColor(textColor);
                }
                this.canvas.drawText(curText, width / 2 - getTextWidth(curText) / 2, y, paint);

                // 绘制帧时间文本
                paint.setTextSize(20);
                y = height - padding + offset ;
                if (y < height * 5 && y - getTextHeight(curText) > height * 4) {
                    paint.setColor(kuangColor);
                } else {
                    paint.setColor(textColor);
                }
                this.canvas.drawText(curTime, width / 2 - getTextWidth(curTime) / 2, y, paint);
            }

            // 绘制中心帧选中边框
            paint.setColor(kuangColor);
            paint.setStyle(Paint.Style.STROKE);
            this.canvas.drawRect(1, SHOWPOSTION0 + 1, width - 1, SHOWPOSTION0 + height - 1, paint);

            // 设置重绘标志
            isRedraw = true;
            // 请求渲染
            onRefresh();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═════════════════════════════════════════════════════════════════════════════

    private Rect rect = new Rect();

    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    private int getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }

    /**
     * 检查并修正帧索引
     */
    private int checkCurFrame(int curFrame) {
        int count = list.size();
        if (count > 0) {
            if (curFrame >= count) {
                curFrame = curFrame % count;
            } else if (curFrame < 0) {
                curFrame = count + curFrame;
            }
        }
        return curFrame;
    }

    public int getCurFrame() {
        return curFrame;
    }

    public SegmentedSingleBean getCurBean() {
        if (list != null && curFrame < list.size()) {
            return list.get(curFrame);
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 动画Handler
    // ═════════════════════════════════════════════════════════════════════════════

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 10) {
                draw(0);
                return;
            }
            int x = ((int) msg.obj) * msg.what;
            draw(x);
            Message msg1 = handler.obtainMessage();
            msg1.what = msg.what + 1;
            msg1.obj = msg.obj;
            handler.sendMessageDelayed(msg1, 20);
        }
    };

    /**
     * 带动画设置当前帧（内部使用）
     */
    private void animSetCurFrame(int curFrame) {
        int offsetCount = 5 * height;
        if (Math.abs(curFrame - this.curFrame) < 5 && Math.abs(curFrame - this.curFrame) > 1) {
            offsetCount = Math.abs(this.curFrame - curFrame) * height;
        } else if (Math.abs(curFrame - this.curFrame) == 1) {
            draw(0);
            return;
        }
        int step = this.curFrame > curFrame ? -offsetCount / 10 : offsetCount / 10;
        Message msg = Message.obtain();
        msg.obj = step;
        msg.what = 1;
        handler.sendMessage(msg);
    }

    /**
     * 设置当前帧（带动画）
     */
    public void setCurFrame(int curFrame) {
        if (curFrame == this.curFrame) {
            return;
        }
        curFrame = checkCurFrame(curFrame);
        int offsetCount = 5 * height;
        if (Math.abs(curFrame - this.curFrame) < 5 && Math.abs(curFrame - this.curFrame) > 1) {
            offsetCount = Math.abs(this.curFrame - curFrame) * height;
        } else if (Math.abs(curFrame - this.curFrame) == 1) {
            this.curFrame = checkCurFrame(curFrame);
            draw(0);
            return;
        }
        int step = this.curFrame > curFrame ? offsetCount / 10 : -offsetCount / 10;
        this.curFrame = checkCurFrame(curFrame);
        Message msg = Message.obtain();
        msg.obj = step;
        msg.what = 1;
        handler.sendMessage(msg);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 属性Getter/Setter方法
    // ═════════════════════════════════════════════════════════════════════════════

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
        if (!isPlay) {
            draw(0);
        }
        onRefresh();
    }

    public boolean isPlayOrder() {
        return isPlayOrder;
    }

    public void setPlayOrder(boolean order) {
        isPlayOrder = order;
    }

    /**
     * 设置播放速度
     * @param playSpeed 播放速度（PLAYSPEEP_1X/2X/4X/8X）
     */
    public void setPlaySpeed(@MainViewSegmentedSingleSmall.PLAYSPEED int playSpeed) {
        curPlaySpeed = playSpeed;
    }

    /**
     * 设置帧数据列表
     */
    public void setList(List<SegmentedSingleBean> list) {
        this.list.clear();
        this.list.addAll(list);
        if (curFrame >= list.size()) curFrame = 0;
        if (list.size() < SHOWCOUNT) {
            List<SegmentedSingleBean> list1 = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                list1.add(list.get(i));
            }
            int t = ((int) Math.ceil(SHOWCOUNT / (float) list.size()));
            for (int i = 0; i < t; i++) {
                this.list.addAll(list1);
            }
        }
        onStopDraw();
    }

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
