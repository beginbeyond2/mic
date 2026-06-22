package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.util.AttributeSet; // 属性集，用于XML属性解析

import androidx.appcompat.widget.AppCompatEditText; // 兼容性EditText基类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    MSelectionEditText - 带选择监听的编辑框控件                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 输入控件                                             ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 继承AppCompatEditText，扩展选择变化监听功能                               ║
 * ║   2. 提供光标位置变化的回调接口                                                ║
 * ║   3. 支持程序化设置选择位置而不触发回调                                        ║
 * ║   4. 支持设置文本而不触发选择变化回调                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MSelectionEditText extends AppCompatEditText                      ║
 * ║   设计模式: 观察者模式，通过OnSelectionChanged接口通知选择变化                   ║
 * ║   回调控制: 通过临时置空监听器实现静默操作                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用场景】                                                                  ║
 * ║   1. 需要监听用户光标移动的场景                                               ║
 * ║   2. 需要实现自定义选择行为的编辑框                                           ║
 * ║   3. 需要区分用户操作和程序操作的编辑框                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   MSelectionEditText editText = findViewById(R.id.edit_text);                 ║
 * ║   editText.setOnSelectionChanged((selStart, selEnd) -> {                      ║
 * ║       // 处理选择变化                                                        ║
 * ║       Log.d("TAG", "Selection: " + selStart + " - " + selEnd);                ║
 * ║   });                                                                        ║
 * ║   // 程序化设置选择位置（不触发回调）                                          ║
 * ║   editText.setSelectionFromUser(5);                                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. setSelectionFromUser和setText方法不会触发选择变化回调                     ║
 * ║   2. 回调在onSelectionChanged中触发，可能频繁调用                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @version 1.0
 */

public class MSelectionEditText extends AppCompatEditText {
    // ================================ 成员变量定义 ================================
    
    private OnSelectionChanged onSelectionChanged; // 选择变化监听器

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 接口：OnSelectionChanged
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   选择变化回调接口，用于通知文本选择位置的变化
     */
    public interface OnSelectionChanged { // 选择变化监听器接口
        /**
         * 选择位置变化回调
         * @param selStart 选择起始位置
         * @param selEnd 选择结束位置
         */
        void onSelectionChanged(int selStart, int selEnd); // 选择变化回调方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getOnSelectionChanged
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取当前设置的选择变化监听器
     * 
     * 【返回值】
     *   @return 当前的OnSelectionChanged实例，可能为null
     */
    public OnSelectionChanged getOnSelectionChanged() { // 获取选择变化监听器
        return onSelectionChanged; // 返回监听器实例
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setOnSelectionChanged
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置选择变化监听器
     * 
     * 【参数说明】
     *   @param onSelectionChanged 选择变化监听器实例
     */
    public void setOnSelectionChanged(OnSelectionChanged onSelectionChanged) { // 设置选择变化监听器
        this.onSelectionChanged = onSelectionChanged; // 保存监听器引用
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     */
    public MSelectionEditText(Context context) { // 构造方法：仅传入Context
        super(context); // 调用父类构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     */
    public MSelectionEditText(Context context, AttributeSet attrs) { // 构造方法：传入Context和属性集
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
    public MSelectionEditText(Context context, AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onSelectionChanged
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   选择位置变化时的回调方法
     *   当用户或程序改变文本选择位置时被调用
     * 
     * 【参数说明】
     *   @param selStart 选择起始位置
     *   @param selEnd 选择结束位置
     * 
     * 【覆写说明】
     *   重写AppCompatEditText.onSelectionChanged方法，添加监听器回调
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) { // 选择位置变化回调
        super.onSelectionChanged(selStart, selEnd); // 调用父类方法
        if (onSelectionChanged != null) { // 如果设置了监听器
            onSelectionChanged.onSelectionChanged(selStart, selEnd); // 触发回调
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setSelectionFromUser
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   程序化设置选择位置，不触发选择变化回调
     *   用于在代码中设置光标位置时避免触发监听器
     * 
     * 【参数说明】
     *   @param index 选择位置（光标位置）
     * 
     * 【实现细节】
     *   临时置空监听器，设置选择位置后再恢复
     */
    public void setSelectionFromUser(int index) { // 程序化设置选择位置（静默）
        if (index > getText().length()) { // 如果索引超出文本长度
            index = getText().length(); // 修正为文本末尾
        }
        OnSelectionChanged onSelectionChanged = this.onSelectionChanged; // 保存当前监听器
        this.onSelectionChanged = null; // 临时置空监听器
        setSelection(index); // 设置选择位置
        this.onSelectionChanged = onSelectionChanged; // 恢复监听器
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置文本内容，不触发选择变化回调
     *   用于在代码中设置文本时避免触发监听器
     * 
     * 【参数说明】
     *   @param text 要设置的文本内容
     *   @param type 缓冲区类型
     * 
     * 【覆写说明】
     *   重写TextView.setText方法，添加静默设置逻辑
     * 
     * 【实现细节】
     *   临时置空监听器，设置文本后再恢复
     */
    @Override
    public void setText(CharSequence text, BufferType type) { // 设置文本（静默）
        OnSelectionChanged onSelectionChanged = this.onSelectionChanged; // 保存当前监听器
        this.onSelectionChanged = null; // 临时置空监听器
        super.setText(text, type); // 调用父类方法设置文本
        this.onSelectionChanged = onSelectionChanged; // 恢复监听器
    }
}
