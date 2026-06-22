package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.content.res.TypedArray;  // 类型化数组，用于读取自定义属性
import android.graphics.Bitmap;  // 位图类
import android.graphics.Canvas;  // 画布类
import android.graphics.Paint;  // 画笔类
import android.graphics.Rect;  // 矩形类
import android.util.AttributeSet;  // XML属性集类
import android.view.MotionEvent;  // 触摸事件类
import android.view.View;  // Android视图基类

import androidx.annotation.Nullable;  // 可空注解

import com.micsig.base.Logger;  // 日志工具类
import com.micsig.tbook.ui.util.BitmapUtil;  // 位图工具类
import com.micsig.tbook.ui.util.ScreenUtil;  // 屏幕工具类
import com.micsig.tbook.ui.util.svg.SvgManager;  // SVG管理器
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;  // SVG节点信息
import com.micsig.tbook.ui.wavezone.TChan;  // 通道常量类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              MSwitchBox                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 自定义开关控件                                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承View，提供自定义开关控件绘制功能                                       │
 * │   2. 支持开/关/禁用三种状态的视觉呈现                                          │
 * │   3. 支持根据通道索引设置开关颜色                                              │
 * │   4. 提供状态变化监听接口                                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │      View       │  ← 继承自Android视图基类                                │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐                                                       │
 * │   │    MSwitchBox   │  ← 自定义绘制开关状态                                   │
 * │   └─────────────────┘                                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   触摸事件 → 状态切换 → 回调通知 → onDraw() → 绘制对应状态位图                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - View: 基类，提供视图框架                                                  │
 * │   - Bitmap: 位图资源                                                         │
 * │   - SvgManager: SVG管理器，用于动态生成带颜色的开关图标                         │
 * │   - TChan: 通道常量，提供通道颜色                                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MSwitchBox                                           │
 * │       android:id="@+id/switch_box"                                          │
 * │       android:layout_width="wrap_content"                                   │
 * │       android:layout_height="wrap_content"                                  │
 * │       app:switchWidth="72dp"                                                │
 * │       app:switchHeight="36dp" />                                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author liwb                                                                 │
 * │ @date 2017/4/11                                                             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MSwitchBox extends View {
    
    // =========================== 类常量定义 ===========================
    
    /** 日志标签 */  // 用于调试输出
    private static final String TAG = "MSwitchBox";

    // =========================== 成员变量定义 ===========================
    
    /** 开关状态位图 */  // openBitmap: 打开状态位图
    private Bitmap openBitmap, closeBitmap, disableBitmap;  // closeBitmap: 关闭状态位图; disableBitmap: 禁用状态位图
    
    /** 触摸坐标和尺寸 */  // lastX: 上次触摸X坐标
    private int lastX, lastY, switchWidth, switchHeight;  // lastY: 上次触摸Y坐标; switchWidth: 开关宽度; switchHeight: 开关高度

    /** 当前开关状态 */  // true表示打开，false表示关闭
    private boolean currState;
    
    /** 是否启用 */  // true表示可交互，false表示禁用
    private boolean enable;
    
    /** Android上下文环境 */  // 用于资源访问
    private Context context;
    
    /** 状态变化监听器 */  // 外部设置的回调
    private OnToggleStateChangedListener onToggleStateChangedListener = null;

    // =========================== 监听器设置方法 ===========================
    
    /**
     * 设置状态变化监听器
     * 
     * @param onToggleStateChangedListener 监听器实例
     */
    public void setOnToggleStateChangedListener(OnToggleStateChangedListener onToggleStateChangedListener) {
        this.onToggleStateChangedListener = onToggleStateChangedListener;  // 保存监听器引用
    }

    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MSwitchBox(Context context) {
        this(context, null);  // 调用双参数构造
    }

    /**
     * 双参数构造方法
     * 解析自定义属性并初始化
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     */
    public MSwitchBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造
    }

    /**
     * 三参数构造方法（完整构造）
     * 解析自定义属性并加载位图资源
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MSwitchBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;  // 保存上下文引用
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MSwitchBox);  // 获取自定义属性数组
        switchWidth = ta.getDimensionPixelSize(R.styleable.MSwitchBox_switchWidth, 72);  // 读取开关宽度，默认72px
        switchHeight = ta.getDimensionPixelSize(R.styleable.MSwitchBox_switchHeight, 36);  // 读取开关高度，默认36px
        openBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_open, switchWidth, switchHeight);  // 加载打开状态位图
        closeBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_close, switchWidth, switchHeight);  // 加载关闭状态位图
        disableBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_disable, switchWidth, switchHeight);  // 加载禁用状态位图
        ta.recycle();  // 回收TypedArray资源
        enable = true;  // 默认启用
    }

    // =========================== 颜色设置方法 ===========================
    
    /**
     * 根据通道索引设置开关颜色
     * 仅对Ch1-S4通道有效，其他通道使用默认颜色
     * 
     * @param chId 通道索引（使用TChan常量）
     */
    public void setControlColorByChIdx(int chId) {
        if (TChan.isCh1ToS4(chId)) {  // 检查是否为Ch1-S4通道
            int color = TChan.getChannelColor(context, chId);  // 获取通道颜色
            // Logger.i(TAG, "Switch color = " + SvgNodeInfo.getAllBaseColor(chId) + " ,chIndex= " + chId + " ,switchWidth= " + switchWidth + " ,switchHeight= " + switchHeight);  // 调试日志
            openBitmap = SvgManager.createScaleSvg(SvgNodeInfo.getSwitchOpenPaths(), SvgNodeInfo.getSwitchOpenColors(chId), SvgNodeInfo.SWITCH_WIDTH, SvgNodeInfo.SWITCH_HEIGHT, switchWidth, switchHeight);  // 使用SVG动态生成带颜色的打开状态位图
        } else {  // 非Ch1-S4通道
            openBitmap = BitmapUtil.getBitmapFromDrawable(getContext(), R.drawable.svg_switch_open, switchWidth, switchHeight);  // 使用默认打开状态位图
        }
        invalidate();  // 触发重绘
    }

    // =========================== 绘制方法 ===========================
    
    /**
     * 绘制方法
     * 根据当前状态绘制对应的位图
     * 
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);  // 调用父类绘制
        Paint paint = new Paint();  // 创建画笔
        int top = getMeasuredHeight() / 2 - openBitmap.getHeight() / 2;  // 计算垂直居中位置
        if (!enable) {  // 禁用状态
            canvas.drawBitmap(disableBitmap, 0, top, paint);  // 绘制禁用状态位图
        } else if (currState) {  // 打开状态
            canvas.drawBitmap(openBitmap, 0, top, paint);  // 绘制打开状态位图
        } else {  // 关闭状态
            canvas.drawBitmap(closeBitmap, 0, top, paint);  // 绘制关闭状态位图
        }
    }

    // =========================== 触摸事件处理 ===========================
    
    /**
     * 触摸事件处理方法
     * 实现点击切换开关状态
     * 
     * @param event 触摸事件
     * @return true表示事件已处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {  // 根据动作类型处理
            case MotionEvent.ACTION_DOWN:  // 按下事件
                lastX = (int) event.getX();  // 记录按下X坐标
                lastY = (int) event.getY();  // 记录按下Y坐标
                break;
            case MotionEvent.ACTION_UP:  // 抬起事件
                if (getViewRect(this).contains((int) event.getX(), (int) event.getY()) &&  // 检查抬起位置是否在控件内
                        getViewRect(this).contains(lastX, lastY)) {  // 检查按下位置是否在控件内
                    if (!enable) {  // 如果禁用
                        break;  // 不处理
                    }
                    currState = !currState;  // 切换状态
                    if (onToggleStateChangedListener != null) {  // 如果设置了监听器
                        Logger.d(TAG, "onToggleStateChangedListener," + ScreenUtil.getViewLocation(MSwitchBox.this));  // 调试日志
                        onToggleStateChangedListener.onToggleStateChanged(MSwitchBox.this, currState);  // 触发状态变化回调
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:  // 移动事件
                break;
        }
        invalidate();  // 触发重绘
        return true;  // 返回true表示事件已处理
    }

    // =========================== 工具方法 ===========================
    
    /**
     * 获取视图的矩形区域
     * 
     * @param view 视图对象
     * @return 视图的矩形区域
     */
    private static Rect getViewRect(View view) {
        return new Rect(0, 0, view.getWidth(), view.getHeight());  // 返回从(0,0)到(width,height)的矩形
    }

    // =========================== 状态访问方法 ===========================
    
    /**
     * 获取当前开关状态
     * 
     * @return true表示打开，false表示关闭
     */
    public boolean isState() {
        return currState;  // 返回当前状态
    }

    /**
     * 设置开关状态
     * 
     * @param state true表示打开，false表示关闭
     */
    public void setState(boolean state) {
        currState = state;  // 更新状态
        invalidate();  // 触发重绘
    }

    /**
     * 设置启用状态
     * 
     * @param enabled true表示启用，false表示禁用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);  // 调用父类方法
        this.enable = enabled;  // 更新内部状态
        invalidate();  // 触发重绘
    }

    /**
     * 获取启用状态
     * 
     * @return true表示启用，false表示禁用
     */
    public boolean isEnabled() {
        return enable;  // 返回启用状态
    }

    // =========================== 接口定义 ===========================
    
    /**
     * 开关状态变化监听接口
     */
    public interface OnToggleStateChangedListener {
        /**
         * 状态变化回调
         * 
         * @param view 触发变化的开关视图
         * @param state 新的状态
         */
        public void onToggleStateChanged(MSwitchBox view, boolean state);
    }
}
