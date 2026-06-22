package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.util.AttributeSet; // 属性集，用于XML属性解析

import androidx.annotation.Nullable; // 可空性注解

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                  MButton_CheckBox_ThreeClick - 三击复选框控件                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 复选框组件 > 三击复选框                               ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 继承MButton_CheckBox，扩展为支持三击功能的复选框                          ║
 * ║   2. 通过OnThreeClickListener接口，允许外部控制选中状态                        ║
 * ║   3. 支持复杂的交互逻辑，如需要确认的操作                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MButton_CheckBox_ThreeClick extends MButton_CheckBox              ║
 * ║   设计模式: 模板方法模式，重写onSingleClick实现不同的点击行为                   ║
 * ║   扩展机制: 通过OnThreeClickListener接口实现状态控制的外部委托                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   点击事件 → onSingleClick → OnThreeClickListener.onThreeClick → 返回新状态   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   父类依赖: MButton_CheckBox                                                  ║
 * ║   外部依赖: Android SDK (View, Context等)                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   Java代码:                                                                  ║
 * ║   checkBox.setOnThreeClickListener((isChecked) -> {                          ║
 * ║       // 返回新的选中状态，可以实现确认对话框等逻辑                              ║
 * ║       return showConfirmDialog();                                             ║
 * ║   });                                                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 如果不设置OnThreeClickListener，行为与普通复选框相同                      ║
 * ║   2. OnThreeClickListener返回值决定最终的选中状态                             ║
 * ║   3. 适用于需要二次确认或复杂状态切换的场景                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author yangj
 * @date 2017/6/28
 * @version 1.0
 */

public class MButton_CheckBox_ThreeClick extends MButton_CheckBox {
    // ================================ 成员变量定义 ================================
    
    private OnThreeClickListener onThreeClickListener; // 三击事件监听器，用于控制选中状态

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 接口：OnThreeClickListener
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   三击事件监听器接口，用于处理点击事件并返回新的选中状态
     *   允许外部控制复选框的最终状态
     * 
     * 【方法说明】
     *   onThreeClick: 处理点击事件，返回新的选中状态
     */
    public interface OnThreeClickListener { // 三击事件监听器接口
        /**
         * 处理三击事件
         * @param check 当前选中状态
         * @return 新的选中状态
         */
        boolean onThreeClick(boolean check); // 处理三击事件，返回新的选中状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getOnThreeClickListener
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取当前设置的三击事件监听器
     * 
     * 【返回值】
     *   @return 当前的OnThreeClickListener实例，可能为null
     */
    public OnThreeClickListener getOnThreeClickListener() { // 获取三击监听器
        return onThreeClickListener; // 返回监听器实例
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setOnThreeClickListener
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置三击事件监听器
     * 
     * 【参数说明】
     *   @param onThreeClickListener 三击事件监听器实例
     */
    public void setOnThreeClickListener(OnThreeClickListener onThreeClickListener) { // 设置三击监听器
        this.onThreeClickListener = onThreeClickListener; // 保存监听器引用
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法，用于代码中动态创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     */
    public MButton_CheckBox_ThreeClick(Context context) { // 构造方法：仅传入Context
        super(context); // 调用父类构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法，用于XML布局文件中创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     */
    public MButton_CheckBox_ThreeClick(Context context, @Nullable AttributeSet attrs) { // 构造方法：传入Context和属性集
        super(context, attrs); // 调用父类构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     */
    public MButton_CheckBox_ThreeClick(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onSingleClick
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   重写父类的单击处理方法，实现三击功能
     *   如果设置了OnThreeClickListener，则由监听器决定新的选中状态
     *   否则执行默认的切换行为
     * 
     * 【覆写说明】
     *   重写MButton_CheckBox.onSingleClick方法
     * 
     * 【处理逻辑】
     *   1. 检查是否设置了OnThreeClickListener
     *   2. 如果设置了，调用监听器的onThreeClick方法获取新状态
     *   3. 如果未设置，执行默认的切换行为
     */
    @Override
    protected void onSingleClick() { // 处理单击事件（重写父类方法）
        if (onThreeClickListener != null) { // 如果设置了三击监听器
            checked = onThreeClickListener.onThreeClick(checked); // 由监听器决定新的选中状态
        } else { // 如果未设置监听器
            checked = !checked; // 执行默认的切换行为
        }
    }
}
