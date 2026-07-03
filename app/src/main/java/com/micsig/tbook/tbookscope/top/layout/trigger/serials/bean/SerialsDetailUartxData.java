package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道Bean，封装触发条件选项

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailUartxData（UART扩展通道数据触发详情）                   ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailUartxData.java                           ║
 * ║ 核心职责: 封装UART扩展通道数据触发条件的详情数据                                  ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有TopBeanChannel+DataBean字段           ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合TopBeanChannel和DataBean，实现ISerialsDetail                     ║
 * ║ 使用场景: UART扩展通道(UARTx)数据触发时传递条件选项和编辑值                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUartxData implements ISerialsDetail { // UART扩展通道数据触发详情，实现ISerialsDetail
    private TopBeanChannel uartxDataCondition; // UART扩展通道数据触发条件选项
    private DataBean uartxDataEdit; // UART扩展通道数据编辑值
    private String uartxDataConditionTitle; // 条件字段标题
    private String uartxDataEditTitle; // 编辑值字段标题

    /**
     * 获取条件字段标题
     * @return 标题字符串
     */
    public String getUartxDataConditionTitle() { // 获取条件标题
        return uartxDataConditionTitle; // 返回标题
    }

    /**
     * 设置条件字段标题
     * @param uartxDataConditionTitle 标题字符串
     */
    public void setUartxDataConditionTitle(String uartxDataConditionTitle) { // 设置条件标题
        this.uartxDataConditionTitle = uartxDataConditionTitle; // 赋值标题
    }

    /**
     * 获取编辑值字段标题
     * @return 标题字符串
     */
    public String getUartxDataEditTitle() { // 获取编辑值标题
        return uartxDataEditTitle; // 返回标题
    }

    /**
     * 设置编辑值字段标题
     * @param uartxDataEditTitle 标题字符串
     */
    public void setUartxDataEditTitle(String uartxDataEditTitle) { // 设置编辑值标题
        this.uartxDataEditTitle = uartxDataEditTitle; // 赋值标题
    }

    /**
     * 获取UART扩展通道数据触发条件选项
     * @return TopBeanChannel 条件选项对象
     */
    public TopBeanChannel getUartxDataCondition() { // 获取条件选项
        return uartxDataCondition; // 返回TopBeanChannel
    }

    /**
     * 设置UART扩展通道数据触发条件选项
     * @param uartxDataCondition 条件选项对象
     */
    public void setUartxDataCondition(TopBeanChannel uartxDataCondition) { // 设置条件选项
            this.uartxDataCondition = uartxDataCondition; // 赋值条件选项
    }

    /**
     * 获取UART扩展通道数据编辑值
     * @return DataBean 编辑值对象
     */
    public DataBean getUartxDataEdit() { // 获取编辑值
        return uartxDataEdit; // 返回DataBean
    }

    /**
     * 设置UART扩展通道数据编辑值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setUartxDataEdit(int digits, String value) { // 设置编辑值
        if (this.uartxDataEdit == null) { // 如果DataBean为空
            this.uartxDataEdit = new DataBean(); // 懒初始化创建DataBean
        }
        this.uartxDataEdit.setDigits(digits); // 设置进制
        this.uartxDataEdit.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailUartxData{" + // 开始构建字符串
                "uartxDataCondition=" + uartxDataCondition + // 拼接条件字段
                ", uartxDataEdit='" + uartxDataEdit + '\'' + // 拼接编辑值字段
                '}'; // 结束字符串构建
    }
}
