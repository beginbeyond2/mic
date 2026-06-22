package com.micsig.tbook.ui.top.view.channel; // 包声明：顶部通道选择视图组件所属包路径

import com.micsig.tbook.ui.bean.RxMsgSelect; // 导入RxJava消息选择基类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     TopBeanChannel 类说明文档                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   顶部通道选择视图数据模型 - MHO系列示波器UI层的通道选择数据封装组件          │
 * │   用于在顶部菜单栏中显示通道选择列表项的数据封装                              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 数据封装：封装通道索引、文本、简化文本等数据                            │
 * │   2. 选择状态管理：继承RxMsgSelect，支持选择状态管理                         │
 * │   3. 对象克隆：实现Cloneable接口，支持对象深拷贝                             │
 * │   4. 文本显示：提供完整文本和简化文本两种显示方式                            │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                       继承关系                                   │       │
 * │   │  RxMsgSelect (父类 - RxJava消息选择基类)                        │       │
 * │   │       ↓                                                          │       │
 * │   │  TopBeanChannel (子类 - 通道选择数据模型)                       │       │
 * │   │       ├── index: int (通道索引)                                 │       │
 * │   │       ├── text: String (完整文本)                               │       │
 * │   │       └── simpleText: String (简化文本)                         │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   用户选择 → TopViewChannel → TopBeanChannel → RxMsgSelect → UI更新         │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖：RxMsgSelect（父类，提供选择状态管理）                            │
 * │   下游依赖：TopViewChannel（通道选择视图）、TopViewChannelMultipleChoice    │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 创建通道选择数据                                                       │
 * │   TopBeanChannel bean = new TopBeanChannel(0, "CH1");                      │
 * │   bean.setSimpleText("C1"); // 设置简化文本                                 │
 * │   bean.setRxMsgSelect(true); // 设置选中状态                                │
 * │                                                                             │
 * │   // 克隆对象                                                               │
 * │   TopBeanChannel cloned = (TopBeanChannel) bean.clone();                   │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 该类实现了Cloneable接口，支持对象克隆                                  │
 * │   2. 继承自RxMsgSelect，具有选择状态管理功能                                │
 * │   3. text和simpleText的区别：text是完整显示文本，simpleText是简化文本       │
 * │   4. 非线程安全类，如需在多线程环境使用需外部同步                            │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by yangj on 2017/5/16                                              │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 顶部通道选择视图数据模型
 * <p>
 * 用于封装顶部菜单栏中通道选择列表项的数据，包括通道索引、文本、简化文本等信息。
 * 继承自RxMsgSelect，支持选择状态管理。实现Cloneable接口，支持对象克隆。
 * <p>
 * 主要用途：
 * - 在TopViewChannel中作为列表项数据模型
 * - 在TopViewChannelMultipleChoice中作为多选列表项数据模型
 * - 支持通道选择、触发源选择等场景
 *
 * @author yangj
 * @version 1.0
 * @since 2017/5/16
 */
public class TopBeanChannel extends RxMsgSelect implements Cloneable { // 继承RxMsgSelect，实现Cloneable接口

    /**
     * 通道索引
     * 用途：标识通道的唯一索引值
     * 取值范围：根据具体业务场景确定，通常为0-7（对应CH1-CH8）
     */
    private int index; // 通道索引，用于标识通道的唯一ID

    /**
     * 完整文本
     * 用途：在UI上显示的完整文本内容
     * 示例："CH1"、"CH2 1V/div"、"Math1"等
     */
    private String text; // 完整显示文本，包含通道名称和可能的其他信息

    /**
     * 简化文本
     * 用途：在空间有限的UI上显示的简化文本
     * 示例："C1"、"C2"、"M1"等
     */
    private String simpleText; // 简化显示文本，用于空间有限的场景

    /**
     * 克隆对象
     * <p>
     * 功能：创建并返回当前对象的副本
     * 实现：调用父类的clone()方法实现浅拷贝
     * 用途：在需要复制对象时使用，避免修改原对象
     *
     * @return 当前对象的克隆副本
     * @throws CloneNotSupportedException 如果对象不支持克隆
     */
    @Override // 重写Object.clone()方法
    public Object clone() throws CloneNotSupportedException { // 克隆方法，抛出克隆不支持异常
        return super.clone(); // 调用父类的clone方法，返回克隆对象
    }

    /**
     * 获取通道索引
     * <p>
     * 功能：返回通道的唯一索引值
     *
     * @return 通道索引值
     */
    public int getIndex() { // 获取通道索引的方法
        return index; // 返回通道索引
    }

    /**
     * 设置通道索引
     * <p>
     * 功能：设置通道的唯一索引值
     *
     * @param index 通道索引值
     */
    public void setIndex(int index) { // 设置通道索引的方法
        this.index = index; // 将参数值赋给成员变量index
    }

    /**
     * 获取完整文本
     * <p>
     * 功能：返回在UI上显示的完整文本内容
     *
     * @return 完整显示文本
     */
    public String getText() { // 获取完整文本的方法
        return text; // 返回完整文本
    }

    /**
     * 设置完整文本
     * <p>
     * 功能：设置在UI上显示的完整文本内容
     *
     * @param text 完整显示文本
     */
    public void setText(String text) { // 设置完整文本的方法
        this.text = text; // 将参数值赋给成员变量text
    }

    /**
     * 获取简化文本
     * <p>
     * 功能：返回在空间有限的UI上显示的简化文本
     *
     * @return 简化显示文本
     */
    public String getSimpleText() { // 获取简化文本的方法
        return simpleText; // 返回简化文本
    }

    /**
     * 设置简化文本
     * <p>
     * 功能：设置在空间有限的UI上显示的简化文本
     *
     * @param simpleText 简化显示文本
     */
    public void setSimpleText(String simpleText) { // 设置简化文本的方法
        this.simpleText = simpleText; // 将参数值赋给成员变量simpleText
    }

    /**
     * 构造函数
     * <p>
     * 功能：创建TopBeanChannel实例，初始化通道索引和文本
     * 说明：simpleText需要单独设置
     *
     * @param index 通道索引值
     * @param text 完整显示文本
     */
    public TopBeanChannel(int index, String text) { // 构造函数，接收索引和文本参数
        this.index = index; // 初始化通道索引
        this.text = text; // 初始化完整文本
    }

    /**
     * 转换为字符串
     * <p>
     * 功能：返回对象的字符串表示，用于调试和日志输出
     * 格式：TopBeanChannel{index=0, text='CH1', rxMsgSelect='true'}
     *
     * @return 对象的字符串表示
     */
    @Override // 重写Object.toString()方法
    public String toString() { // 转换为字符串的方法
        return "TopBeanChannel{" + // 返回类名和左大括号
                "index=" + index + // 拼接索引字段
                ", text='" + text + '\'' + // 拼接文本字段
                ", rxMsgSelect='" + rxMsgSelect + '\'' + // 拼接选择状态字段（继承自父类）
                '}'; // 返回右大括号
    }
}
