package com.micsig.tbook.ui.bean; // UI组件库bean子包，包含数据模型类

import android.content.Context; // Android上下文对象
import android.view.View; // Android视图基类

import com.micsig.tbook.ui.R; // 资源类

import java.util.function.BiConsumer; // 双参数消费者函数式接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     RadioButtonBean - 单选按钮数据模型                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > bean > 数据模型                                                  ║
 * ║   MHO系列示波器软件数据模型之一                                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 存储单选按钮的数据状态                                                   ║
 * ║   2. 管理按钮的选中、启用、可见等状态                                          ║
 * ║   3. 提供点击事件回调机制                                                     ║
 * ║   4. 支持自定义背景和颜色配置                                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   设计模式: JavaBean模式                                                      ║
 * ║   回调机制: 使用BiConsumer函数式接口处理点击事件                                ║
 * ║   可扩展性: 支持自定义边距、背景、颜色等属性                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用场景】                                                                  ║
 * ║   1. TopViewRadioGroup等单选按钮组的数据绑定                                  ║
 * ║   2. 菜单选项的数据模型                                                       ║
 * ║   3. 需要选中状态管理的UI组件                                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   RadioButtonBean bean = new RadioButtonBean(                                 ║
 * ║       0, "选项1", false, true, Color.RED, R.drawable.bg_button,              ║
 * ║       (view, bean) -> { /* 处理点击 *\/ }                                     ║
 * ║   );                                                                          ║
 * ║   bean.setCheck(true);                                                        ║
 * ║   bean.setEnable(true);                                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. onClick回调使用BiConsumer接口，支持Lambda表达式                           ║
 * ║   2. isUserDefine方法用于判断是否为用户自定义选项                              ║
 * ║   3. beforeColor用于存储启用前的颜色，便于状态恢复                             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author Liwb
 * @date 2024-2-27 11:13
 * @version 1.0
 */

public class RadioButtonBean {
    // ================================ 成员变量定义 ================================
    
    private int index; // 按钮索引，用于标识按钮在组中的位置
    private String text; // 按钮显示的文本内容
    private boolean check; // 按钮是否被选中
    private boolean enable; // 按钮是否可用（可交互）
    private int visible; // 按钮的可见性状态
    private String simpleText; // 简化文本，用于短显示
    private int ResIdBackGround; // 背景资源ID
    private boolean enableBeforeColor; // 是否启用前置颜色功能
    private int beforeColor; // 启用前的颜色值，用于状态恢复

    private BiConsumer<View, RadioButtonBean> onClick = null; // 点击事件回调，接收View和RadioButtonBean参数

    // 边距配置
    private int itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin; // 按钮的四边边距

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   创建RadioButtonBean实例
     * 
     * 【参数说明】
     *   @param index 按钮索引
     *   @param text 按钮文本
     *   @param check 初始选中状态
     *   @param enableBeforeColor 是否启用前置颜色
     *   @param beforeColor 前置颜色值
     *   @param resIdBackGround 背景资源ID
     *   @param onClick 点击事件回调
     */
    public RadioButtonBean(int index, String text, boolean check, boolean enableBeforeColor, int beforeColor, int resIdBackGround, BiConsumer<View, RadioButtonBean> onClick) { // 构造方法
        this.index = index; // 设置索引
        this.text = text; // 设置文本
        this.check = check; // 设置选中状态
        this.enable = true; // 默认启用
        this.enableBeforeColor = enableBeforeColor; // 设置前置颜色启用标志
        this.beforeColor = beforeColor; // 设置前置颜色
        this.ResIdBackGround = resIdBackGround; // 设置背景资源
        this.onClick = onClick; // 设置点击回调
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setItemMargin
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按钮的四边边距
     * 
     * 【参数说明】
     *   @param itemLeftMargin 左边距
     *   @param itemTopMargin 上边距
     *   @param itemRightMargin 右边距
     *   @param itemBottomMargin 下边距
     */
    public void setItemMargin(int itemLeftMargin, int itemTopMargin, int itemRightMargin, int itemBottomMargin) { // 设置边距
        this.itemLeftMargin = itemLeftMargin; // 设置左边距
        this.itemTopMargin = itemTopMargin; // 设置上边距
        this.itemRightMargin = itemRightMargin; // 设置右边距
        this.itemBottomMargin = itemBottomMargin; // 设置下边距
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：isUserDefine
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   判断当前按钮是否为用户自定义选项
     *   通过比较文本内容与预定义的用户自定义字符串
     * 
     * 【参数说明】
     *   @param context Android上下文对象，用于获取字符串资源
     * 
     * 【返回值】
     *   @return true如果是用户自定义选项，false否则
     */
    public boolean isUserDefine(Context context) { // 判断是否为用户自定义
        return context.getString(R.string.serialsUserDefine).equals(text); // 比较文本
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：isEnable
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取按钮的启用状态
     * 
     * 【返回值】
     *   @return true如果按钮可用，false否则
     */
    public boolean isEnable() { // 获取启用状态
        return enable; // 返回启用状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setEnable
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按钮的启用状态
     * 
     * 【参数说明】
     *   @param enable true=启用，false=禁用
     */
    public void setEnable(boolean enable) { // 设置启用状态
        this.enable = enable; // 更新启用状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getIndex
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取按钮索引
     * 
     * 【返回值】
     *   @return 按钮索引值
     */
    public int getIndex() { // 获取索引
        return index; // 返回索引
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setIndex
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按钮索引
     * 
     * 【参数说明】
     *   @param index 新的索引值
     */
    public void setIndex(int index) { // 设置索引
        this.index = index; // 更新索引
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取按钮文本
     * 
     * 【返回值】
     *   @return 按钮文本字符串
     */
    public String getText() { // 获取文本
        return text; // 返回文本
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按钮文本
     * 
     * 【参数说明】
     *   @param text 新的文本内容
     */
    public void setText(String text) { // 设置文本
        this.text = text; // 更新文本
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：isCheck
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取按钮选中状态
     * 
     * 【返回值】
     *   @return true如果选中，false否则
     */
    public boolean isCheck() { // 获取选中状态
        return check; // 返回选中状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setCheck
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按钮选中状态
     * 
     * 【参数说明】
     *   @param check true=选中，false=未选中
     */
    public void setCheck(boolean check) { // 设置选中状态
        this.check = check; // 更新选中状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getSimpleText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取简化文本
     * 
     * 【返回值】
     *   @return 简化文本字符串
     */
    public String getSimpleText() { // 获取简化文本
        return simpleText; // 返回简化文本
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setSimpleText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置简化文本
     * 
     * 【参数说明】
     *   @param simpleText 简化文本内容
     */
    public void setSimpleText(String simpleText) { // 设置简化文本
        this.simpleText = simpleText; // 更新简化文本
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getResIdBackGround
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取背景资源ID
     * 
     * 【返回值】
     *   @return 背景资源ID
     */
    public int getResIdBackGround() { // 获取背景资源ID
        return ResIdBackGround; // 返回背景资源ID
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setResIdBackGround
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置背景资源ID
     * 
     * 【参数说明】
     *   @param resIdBackGround 背景资源ID
     */
    public void setResIdBackGround(int resIdBackGround) { // 设置背景资源ID
        ResIdBackGround = resIdBackGround; // 更新背景资源ID
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getOnClick
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取点击事件回调
     * 
     * 【返回值】
     *   @return 点击事件回调BiConsumer实例
     */
    public BiConsumer<View, RadioButtonBean> getOnClick() { // 获取点击回调
        return onClick; // 返回点击回调
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setOnClick
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置点击事件回调
     * 
     * 【参数说明】
     *   @param onClick 点击事件回调BiConsumer实例
     */
    public void setOnClick(BiConsumer<View, RadioButtonBean> onClick) { // 设置点击回调
        this.onClick = onClick; // 更新点击回调
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getItemLeftMargin
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取左边距
     * 
     * 【返回值】
     *   @return 左边距值
     */
    public int getItemLeftMargin() { // 获取左边距
        return itemLeftMargin; // 返回左边距
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getItemTopMargin
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取上边距
     * 
     * 【返回值】
     *   @return 上边距值
     */
    public int getItemTopMargin() { // 获取上边距
        return itemTopMargin; // 返回上边距
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getItemRightMargin
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取右边距
     * 
     * 【返回值】
     *   @return 右边距值
     */
    public int getItemRightMargin() { // 获取右边距
        return itemRightMargin; // 返回右边距
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getItemBottomMargin
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取下边距
     * 
     * 【返回值】
     *   @return 下边距值
     */
    public int getItemBottomMargin() { // 获取下边距
        return itemBottomMargin; // 返回下边距
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：isEnableBeforeColor
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取是否启用前置颜色功能
     * 
     * 【返回值】
     *   @return true如果启用前置颜色，false否则
     */
    public boolean isEnableBeforeColor() { // 获取前置颜色启用状态
        return enableBeforeColor; // 返回启用状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setEnableBeforeColor
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置是否启用前置颜色功能
     * 
     * 【参数说明】
     *   @param enableBeforeColor true=启用，false=禁用
     */
    public void setEnableBeforeColor(boolean enableBeforeColor) { // 设置前置颜色启用状态
        this.enableBeforeColor = enableBeforeColor; // 更新启用状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getBeforeColor
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取前置颜色值
     * 
     * 【返回值】
     *   @return 前置颜色值
     */
    public int getBeforeColor() { // 获取前置颜色
        return beforeColor; // 返回前置颜色
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setBeforeColor
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置前置颜色值
     * 
     * 【参数说明】
     *   @param beforeColor 前置颜色值
     */
    public void setBeforeColor(int beforeColor) { // 设置前置颜色
        this.beforeColor = beforeColor; // 更新前置颜色
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getVisible
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取可见性状态
     * 
     * 【返回值】
     *   @return 可见性状态值
     */
    public int getVisible() { // 获取可见性
        return visible; // 返回可见性
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setVisible
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置可见性状态
     * 
     * 【参数说明】
     *   @param visible 可见性状态值
     */
    public void setVisible(int visible) { // 设置可见性
        this.visible = visible; // 更新可见性
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：toString
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   返回对象的字符串表示，用于调试和日志输出
     * 
     * 【返回值】
     *   @return 格式化的字符串表示
     */
    @Override
    public String toString() { // 转换为字符串
        final StringBuilder sb = new StringBuilder("RadioButtonBean{"); // 创建StringBuilder
        sb.append("index=").append(index); // 添加索引
        sb.append(", text='").append(text).append('\''); // 添加文本
        sb.append(", check=").append(check); // 添加选中状态
        sb.append(", enable=").append(enable); // 添加启用状态
        sb.append(", visible=").append(visible); // 添加可见性
        sb.append(", simpleText='").append(simpleText).append('\''); // 添加简化文本
        sb.append(", beforeColor=").append(beforeColor); // 添加前置颜色
        sb.append('}'); // 添加结束括号
        return sb.toString(); // 返回字符串
    }
}
