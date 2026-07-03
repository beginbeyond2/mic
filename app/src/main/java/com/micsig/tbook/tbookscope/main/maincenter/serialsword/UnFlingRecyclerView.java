package com.micsig.tbook.tbookscope.main.maincenter.serialsword;  // 定义禁用滑动RecyclerView类的包路径

import android.content.Context;  // 导入上下文类，用于获取应用环境和资源
import android.util.AttributeSet;  // 导入属性集类，用于XML布局属性解析
import androidx.annotation.Nullable;  // 导入Nullable注解，标记可空参数
import androidx.recyclerview.widget.RecyclerView;  // 导入RecyclerView基类

/**
 * ***********************************************************************************
 * * 禁用滑动手势的RecyclerView控件类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   自定义视图控件 - 串口文本显示列表的防滑动优化控件
 * *
 * * 【核心职责】
 * *   1. 继承RecyclerView，保留列表显示和滚动功能
 * *   2. 禁用fling快速滑动手势，防止数据跳跃
 * *   3. 仅支持精准的触摸滚动和外部按键滚动
 * *   4. 确保串口文本数据的稳定可控显示
 * *
 * * 【架构设计】
 * *   继承RecyclerView，重写fling方法返回false：
 * *   - 完全禁用惯性滑动机制
 * *   - 保留RecyclerView的其他所有功能（布局、适配器、滚动等）
 * *   - 配合外部按键和精准触摸控制实现平滑显示
 * *
 * * 【数据流向】
 * *   输入：
 * *     → 用户触摸事件（滚动）
 * *     → 外部按键事件（精准移动）
 * *   输出：
 * *     → 列表精准滚动到目标位置
 * *     → 拒绝fling手势的惯性滑动
 * *
 * * 【依赖关系】
 * *   上层依赖：各协议Fragment（UART/LIN/CAN/SPI/I2C/M429/M1553B）
 * *   下层依赖：RecyclerView基类和Android框架
 * *   平级依赖：无
 * *
 * * 【使用场景】
 * *   用于串口总线文本显示列表，防止用户快速滑动导致数据跳跃丢失
 * *   在Run模式实时刷新时避免滑动冲突，在Stop模式确保历史数据可控浏览
 * ***********************************************************************************
 */
public class UnFlingRecyclerView extends RecyclerView {
    /**
     * 单参数构造方法
     * @param context 应用上下文对象
     */
    public UnFlingRecyclerView(Context context) { super(context); }  // 调用父类RecyclerView构造方法

    /**
     * 双参数构造方法，用于XML布局解析
     * @param context 应用上下文对象
     * @param attrs XML属性集
     */
    public UnFlingRecyclerView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }  // 调用父类RecyclerView构造方法

    /**
     * 三参数构造方法，包含默认样式
     * @param context 应用上下文对象
     * @param attrs XML属性集
     * @param defStyle 默认样式属性
     */
    public UnFlingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }  // 调用父类RecyclerView构造方法

    /**
     * 重写fling方法，禁用快速滑动手势
     * @param velocityX X方向滑动速度
     * @param velocityY Y方向滑动速度
     * @return false表示拒绝fling手势，不执行惯性滑动
     */
    @Override
    public boolean fling(int velocityX, int velocityY) { return false; }  // 返回false禁用fling快速滑动
}