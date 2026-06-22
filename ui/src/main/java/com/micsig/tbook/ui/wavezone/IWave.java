package com.micsig.tbook.ui.wavezone;

import android.graphics.Canvas;

import com.chillingvan.canvasgl.ICanvasGL;

import java.util.function.Consumer;

/**
 * 波形绘制接口
 * 
 * <p>定义示波器波形绘制的基本接口，所有波形元素都需要实现此接口。
 * 该接口提供了波形的位置、颜色、可见性、选择状态等基本属性的管理，
 * 以及波形的移动、绘制等核心操作。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>波形移动：支持X/Y方向的偏移移动</li>
 *     <li>波形绘制：支持普通Canvas和OpenGL两种绘制方式</li>
 *     <li>位置管理：设置和获取波形的X/Y坐标</li>
 *     <li>外观设置：设置颜色、可见性、选择状态</li>
 *     <li>事件回调：选择状态变化和波形移动的事件监听</li>
 * </ul>
 * 
 * <p>实现类示例：</p>
 * <pre>{@code
 * public class MyWave implements IWave {
 *     private long x;
 *     private double y;
 *     private int color;
 *     private boolean visible;
 *     private boolean selected;
 *     
 *     @Override
 *     public void draw(Canvas canvas) {
 *         if (visible) {
 *             // 绘制波形
 *         }
 *     }
 *     
 *     // ... 其他方法实现
 * }
 * }</pre>
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/5
 * @see IChan 通道标识枚举
 * @see Canvas Android画布
 * @see ICanvasGL OpenGL画布
 */
public interface IWave {
    // ==================== 移动方法 ====================
    /**
     * 移动波形位置
     * 
     * <p>根据指定的偏移量移动波形的位置。</p>
     * 
     * @param offsetX X方向的偏移量（像素）
     * @param offsetY Y方向的偏移量（像素）
     */
    void moveLine(int offsetX, int offsetY);

    // ==================== 绘制方法 ====================
    /**
     * 在普通Canvas上绘制波形
     * 
     * <p>使用Android标准Canvas绘制波形，适用于普通绘制场景。</p>
     * 
     * @param canvas Android画布对象
     */
    void draw(Canvas canvas);

    /**
     * 在OpenGL Canvas上绘制波形
     * 
     * <p>使用OpenGL Canvas绘制波形，适用于需要硬件加速的绘制场景。</p>
     * 
     * @param canvas OpenGL画布对象
     */
    void draw(ICanvasGL canvas);

    // ==================== 位置管理方法 ====================
    /**
     * 重置波形到初始位置
     * 
     * <p>将波形重置到屏幕的50%位置（中心位置）。</p>
     */
    void resultIniRect();

    /**
     * 设置波形的名称ID
     * 
     * @param nameId 名称资源ID或标识符
     */
    void setLineNameId(int nameId);

    /**
     * 获取波形的名称ID
     * 
     * @return 名称资源ID或标识符
     */
    int getLineNameID();

    /**
     * 获取波形的X坐标
     * 
     * @return X坐标值
     */
    long getX();

    /**
     * 获取波形的Y坐标
     * 
     * @return Y坐标值
     */
    double getY();

    /**
     * 设置波形的X坐标
     * 
     * @param x X坐标值
     */
    void setX(long x);

    /**
     * 设置波形的Y坐标
     * 
     * @param y Y坐标值
     */
    void setY(double y);

    // ==================== 外观设置方法 ====================
    /**
     * 设置波形的颜色
     * 
     * @param color 颜色值（ARGB格式）
     */
    void setColor(int color);

    /**
     * 获取波形的颜色
     * 
     * @return 颜色值（ARGB格式）
     */
    int getColor();

    /**
     * 设置波形的可见性
     * 
     * @param visible true表示可见，false表示隐藏
     */
    void setVisible(boolean visible);

    /**
     * 获取波形的可见性
     * 
     * @return true表示可见，false表示隐藏
     */
    boolean getVisible();

    /**
     * 设置波形的选择状态
     * 
     * @param selected true表示已选中，false表示未选中
     */
    void setSelected(boolean selected);

    /**
     * 获取波形的选择状态
     * 
     * @return true表示已选中，false表示未选中
     */
    boolean isSelected();

    /**
     * 按像素移动波形
     * 
     * @param px 移动的像素值
     */
    void movePix(double px);

    // ==================== 事件监听方法 ====================
    /**
     * 设置选择状态变化事件监听器
     * 
     * @param onSelectChangeEvent 选择状态变化事件监听器
     */
    void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent);

    /**
     * 设置波形移动事件监听器
     * 
     * @param onMovingWaveEvent 波形移动事件监听器
     */
    void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent);

    // ==================== 回调接口 ====================
    /**
     * 选择状态变化事件监听接口
     * 
     * @deprecated 使用更现代的事件处理方式替代
     */
    interface OnSelectChangeEvent {
        /**
         * 选择状态变化回调方法
         * 
         * @param iWave 发生变化的波形对象
         * @param isSelect 新的选择状态
         */
        void OnSelectChange(IWave iWave, boolean isSelect);
    }

    /**
     * 波形移动事件监听接口
     * 
     * @deprecated 使用更现代的事件处理方式替代
     */
    interface OnMovingWaveEvent {
        /**
         * 波形移动回调方法
         * 
         * @param iWave 移动的波形对象
         * @param x 新的X坐标
         * @param y 新的Y坐标
         * @param isSwitchWorkMode 是否切换工作模式
         * @param isFromEventBus 是否来自EventBus事件
         */
        void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus);
    }
}
