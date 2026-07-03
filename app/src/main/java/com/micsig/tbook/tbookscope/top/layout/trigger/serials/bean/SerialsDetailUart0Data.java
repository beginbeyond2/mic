package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道Bean，封装触发条件选项

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailUart0Data（UART通道0数据触发详情）                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailUart0Data.java                           ║
 * ║ 核心职责: 封装UART通道0数据触发条件的详情数据                                    ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有TopBeanChannel+DataBean字段           ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合TopBeanChannel和DataBean，实现ISerialsDetail                     ║
 * ║ 使用场景: UART通道0数据触发时传递条件选项和编辑值                               ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUart0Data implements ISerialsDetail { // UART通道0数据触发详情，实现ISerialsDetail
    private TopBeanChannel uart0DataCondition; // UART通道0数据触发条件选项
    private DataBean uart0DataEdit; // UART通道0数据编辑值
    private String uart0DataConditionTitle; // 条件字段标题
    private String uart0DataEditTitle; // 编辑值字段标题

    /**
     * 获取条件字段标题
     * @return 标题字符串
     */
    public String getUart0DataConditionTitle() { // 获取条件标题
        return uart0DataConditionTitle; // 返回标题
    }

    /**
     * 设置条件字段标题
     * @param uart0DataConditionTitle 标题字符串
     */
    public void setUart0DataConditionTitle(String uart0DataConditionTitle) { // 设置条件标题
        this.uart0DataConditionTitle = uart0DataConditionTitle; // 赋值标题
    }

    /**
     * 获取编辑值字段标题
     * @return 标题字符串
     */
    public String getUart0DataEditTitle() { // 获取编辑值标题
        return uart0DataEditTitle; // 返回标题
    }

    /**
     * 设置编辑值字段标题
     * @param uart0DataEditTitle 标题字符串
     */
    public void setUart0DataEditTitle(String uart0DataEditTitle) { // 设置编辑值标题
        this.uart0DataEditTitle = uart0DataEditTitle; // 赋值标题
    }

    /**
     * 获取UART通道0数据触发条件选项
     * @return TopBeanChannel 条件选项对象
     */
    public TopBeanChannel getUart0DataCondition() { // 获取条件选项
        return uart0DataCondition; // 返回TopBeanChannel
    }

    /**
     * 设置UART通道0数据触发条件选项
     * @param uart0DataCondition 条件选项对象
     */
    public void setUart0DataCondition(TopBeanChannel uart0DataCondition) { // 设置条件选项
        this.uart0DataCondition = uart0DataCondition; // 赋值条件选项
    }

    /**
     * 获取UART通道0数据编辑值
     * @return DataBean 编辑值对象
     */
    public DataBean getUart0DataEdit() { // 获取编辑值
        return uart0DataEdit; // 返回DataBean
    }

    /**
     * 设置UART通道0数据编辑值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setUart0DataEdit(int digits, String value) { // 设置编辑值
        if (this.uart0DataEdit == null) { // 如果DataBean为空
            this.uart0DataEdit = new DataBean(); // 懒初始化创建DataBean
        }
        this.uart0DataEdit.setDigits(digits); // 设置进制
        this.uart0DataEdit.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailUart0Data{" + // 开始构建字符串
                "uart0DataCondition=" + uart0DataCondition + // 拼接条件字段
                ", uart0DataEdit='" + uart0DataEdit + '\'' + // 拼接编辑值字段
                '}'; // 结束字符串构建
    }
}
