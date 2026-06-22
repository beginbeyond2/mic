package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.util.AttributeSet;  // XML属性集类
import android.view.MotionEvent;  // 触摸事件类

import androidx.appcompat.widget.AppCompatRadioButton;  // 兼容性单选按钮基类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              MRadioButton                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 自定义单选按钮控件                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承AppCompatRadioButton，提供带防抖动功能的单选按钮                       │
 * │   2. 实现触摸事件的精确控制，区分点击和滑动操作                                   │
 * │   3. 支持小范围抖动仍视为有效点击的容错机制                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │ AppCompatRadio  │  ← 继承自Android兼容性单选按钮                           │
 * │   │     Button      │                                                       │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐                                                       │
 * │   │  MRadioButton   │  ← 扩展触摸事件处理，添加防抖动逻辑                        │
 * │   └─────────────────┘                                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   用户触摸 → onTouchEvent() → 计算偏移量 → 判断是否为有效点击 → 触发点击事件      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - AppCompatRadioButton: 基类，提供单选按钮基础功能                            │
 * │   - Context: Android上下文                                                   │
 * │   - AttributeSet: XML属性解析                                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MRadioButton                                         │
 * │       android:id="@+id/radio_btn"                                           │
 * │       android:layout_width="wrap_content"                                   │
 * │       android:layout_height="wrap_content"                                  │
 * │       android:text="选项1" />                                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author Liwb                                                                 │
 * │ @date 2022-2-12 9:13                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MRadioButton extends AppCompatRadioButton {
    
    // =========================== 类常量定义 ===========================
    
    /** 日志标签，用于调试输出 */  // 类名简写作为TAG
    private static final String TAG = MRadioButton.class.getSimpleName();

    /** 最小点击范围阈值(像素)，小于此值的抖动视为有效点击 */  // 防抖动阈值，10像素范围内视为点击
    private static final int MinClickRangePx = 10;

    // =========================== 成员变量定义 ===========================
    
    /** 按下时的X坐标，用于计算移动偏移量 */  // 记录ACTION_DOWN时的X位置
    private int oldX, oldY;  // oldY: 按下时的Y坐标，用于计算移动偏移量

    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MRadioButton(Context context) {
        this(context, null, android.R.attr.textViewStyle);  // 调用三参数构造，使用默认textViewStyle
    }

    /**
     * 双参数构造方法
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public MRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);  // 调用三参数构造，使用默认textViewStyle
    }

    /**
     * 三参数构造方法（完整构造）
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集，从布局文件中解析的属性
     * @param defStyleAttr 默认样式属性
     */
    public MRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造方法完成初始化
    }

    // =========================== 触摸事件处理 ===========================
    
    /**
     * 触摸事件处理方法
     * 实现防抖动点击检测，只有小范围移动才视为有效点击
     * 
     * @param event 触摸事件对象，包含动作类型和坐标信息
     * @return true表示事件已处理，false表示未处理
     * 
     * 处理流程:
     * 1. ACTION_DOWN: 记录按下时的坐标
     * 2. ACTION_UP: 计算移动偏移量，判断是否为有效点击
     * 3. 其他事件: 调用父类处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {  // 获取动作类型，支持多点触控
            case MotionEvent.ACTION_DOWN: {  // 手指按下事件
                oldX = (int) event.getRawX();  // 记录按下时的屏幕绝对X坐标
                oldY = (int) event.getRawY();  // 记录按下时的屏幕绝对Y坐标
            } break;  // 跳出case分支
            
            case MotionEvent.ACTION_UP: {  // 手指抬起事件
                int offsetX = Math.abs((int) event.getRawX() - oldX);  // 计算X方向移动距离的绝对值
                int offsetY = Math.abs((int) event.getRawY() - oldY);  // 计算Y方向移动距离的绝对值
                // Logger.i(TAG,"offsetX:"+offsetX+",offsetY:"+offsetY);  // 调试日志，输出偏移量
                if (offsetX < MinClickRangePx && offsetY < MinClickRangePx && isEnabled()) {  // 判断是否为有效点击：偏移量小于阈值且控件可用
                    // Logger.i(TAG,"Click Name:"+getText());  // 调试日志，输出点击的按钮文本
                    super.performClick();  // 触发父类的点击事件，执行点击回调
                }
                return true;  // 返回true表示事件已处理
            }
        }

        return super.onTouchEvent(event);  // 其他事件调用父类处理
    }
}
