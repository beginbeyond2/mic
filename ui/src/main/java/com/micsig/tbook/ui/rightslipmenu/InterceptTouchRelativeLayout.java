package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                          触摸拦截相对布局
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 可拦截触摸事件的RelativeLayout，用于右侧滑菜单中需要自定义触摸处理的场景，
 * 提供触摸事件回调接口。
 *
 * 【核心职责】
 * 1. 继承RelativeLayout提供基本布局功能
 * 2. 提供触摸事件拦截回调
 * 3. 消费所有触摸事件
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                 InterceptTouchRelativeLayout                    │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  继承层: extends RelativeLayout                                 │
 * │  回调层: OnInterceptTouchListener                               │
 * │  事件层: dispatchTouchEvent / onInterceptTouchEvent / onTouchEvent│
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【事件传递流程】
 * dispatchTouchEvent → onInterceptTouchEvent → onTouchEvent → 回调
 *
 * 【使用示例】
 * InterceptTouchRelativeLayout layout = findViewById(R.id.interceptLayout);
 * layout.setOnInterceptTouchListener(new OnInterceptTouchListener() {
 *     @Override
 *     public void onTouch(MotionEvent event, View view) {
 *         // 处理触摸事件
 *     }
 * });
 *
 * 【注意事项】
 * 1. onTouchEvent始终返回true，消费所有触摸事件
 * 2. 适用于需要拦截子View触摸事件的场景
 *
 * @author yangj
 * @since 2017/5/5
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class InterceptTouchRelativeLayout extends RelativeLayout {
    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /** 触摸拦截回调 */
    private OnInterceptTouchListener onInterceptTouchListener;

    // ═════════════════════════════════════════════════════════════════════════════
    // 回调接口
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 触摸拦截回调接口
     */
    public interface OnInterceptTouchListener {
        /**
         * 触摸事件回调
         * @param event 触摸事件
         * @param view  触摸的视图
         */
        void onTouch(MotionEvent event, View view);
    }

    /**
     * 设置触摸拦截回调
     * @param onInterceptTouchListener 回调接口
     */
    public void setOnInterceptTouchListener(OnInterceptTouchListener onInterceptTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    public InterceptTouchRelativeLayout(Context context) {
        super(context);
    }

    public InterceptTouchRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptTouchRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 触摸事件方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 分发触摸事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 拦截触摸事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 处理触摸事件
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 处理触摸事件并触发回调，始终消费事件。
     *
     * 【参数说明】
     * @param event 触摸事件
     *
     * 【返回值】
     * @return 始终返回true，表示消费事件
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 触发回调
        if (onInterceptTouchListener != null) {
            onInterceptTouchListener.onTouch(event, InterceptTouchRelativeLayout.this);
        }
        // 调用父类方法
        super.onTouchEvent(event);
        // 始终消费事件
        return true;
    }
}
