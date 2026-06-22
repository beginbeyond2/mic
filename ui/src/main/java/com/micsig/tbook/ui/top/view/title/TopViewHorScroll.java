package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          TopViewHorScroll                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 顶部标题栏水平滚动视图                                              ║
 * ║ 核心职责: 提供支持滑动监听的水平滚动容器                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌──────────────────┐                                                      ║
 * ║   │  TopViewHorScroll │ ──继承──▶ HorizontalScrollView                      ║
 * ║   └──────────────────┘                                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   触摸事件 ──▶ onInterceptTouchEvent() ──▶ OnScrollListener回调             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - TopViewTitleWithScroll: 父容器，使用此类实现标题栏滚动                   ║
 * ║   - HorizontalScrollView: Android水平滚动视图基类                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   TopViewHorScroll scrollView = findViewById(R.id.scrollView);              ║
 * ║   scrollView.setOnScrollListener(new OnScrollListener() {                   ║
 * ║       @Override                                                              ║
 * ║       public void onScrollChanged(TopViewHorScroll v, int x, int y,         ║
 * ║                                    int oldx, int oldy) {                     ║
 * ║           // 处理滚动事件                                                    ║
 * ║       }                                                                      ║
 * ║       @Override                                                              ║
 * ║       public void onStop() {                                                ║
 * ║           // 滚动停止                                                        ║
 * ║       }                                                                      ║
 * ║   });                                                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 注意事项:                                                                    ║
 * ║   1. 通过拦截触摸事件实现滑动监听                                            ║
 * ║   2. MinSliderRange定义最小滑动距离阈值                                      ║
 * ║   3. 支持滑动开始、滑动中、滑动停止三种状态回调                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopViewHorScroll extends HorizontalScrollView {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 最小滑动距离阈值，超过此值才触发滑动回调 */
    private static final int MinSliderRange=20;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 滚动监听器 */
    private OnScrollListener onScrollListener;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法（单参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    public TopViewHorScroll(Context context) {
        super(context);
    }

    /**
     * 构造方法（双参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     * @param attrs   属性集
     */
    public TopViewHorScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造方法（三参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context      上下文对象
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewHorScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 滚动监听接口
     */
    public interface OnScrollListener {
        /**
         * 滚动变化回调
         * ══════════════════════════════════════════════════════════════════════════
         * @param scrollView 滚动视图
         * @param x          当前X坐标
         * @param y          当前Y坐标
         * @param oldx       之前X坐标
         * @param oldy       之前Y坐标
         */
        void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy);

        /**
         * 滚动停止回调
         * ══════════════════════════════════════════════════════════════════════════
         */
        void onStop();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 监听器方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取滚动监听器
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 滚动监听器
     */
    public OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    /**
     * 设置滚动监听器
     * ══════════════════════════════════════════════════════════════════════════════
     * @param onScrollListener 滚动监听器
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 当前坐标和上一次坐标 */
    int l, oldl,t,oldt,downX;

    /**
     * 拦截触摸事件
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据触摸事件类型，触发相应的滚动回调
     * ══════════════════════════════════════════════════════════════════════════════
     * @param ev 触摸事件
     * @return 是否拦截事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 手指抬起时，检查是否超过最小滑动距离
            if (onScrollListener != null && Math.abs(downX-(int) ev.getX())>MinSliderRange) {
                onScrollListener.onStop();
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 记录按下位置
            oldl = (int) ev.getX();
            oldt=(int)ev.getY();
            downX=oldl;
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            // 移动时，检查是否超过最小滑动距离
            l=(int) ev.getX();
            t=(int) ev.getY();
            if (onScrollListener != null && Math.abs(downX-(int) ev.getX())>MinSliderRange) {
                onScrollListener.onScrollChanged(this, l, 0, oldl, 0);
            }
            oldl=(int) ev.getX();
            return false;
        } else {
            return false;
        }
    }
}
