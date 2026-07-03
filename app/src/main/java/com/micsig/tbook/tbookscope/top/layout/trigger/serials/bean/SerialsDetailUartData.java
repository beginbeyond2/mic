package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道Bean，封装触发条件选项

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailUartData（UART数据触发详情）                          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailUartData.java                             ║
 * ║ 核心职责: 封装UART数据触发条件的详情数据                                        ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有TopBeanChannel+DataBean字段           ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合TopBeanChannel和DataBean，实现ISerialsDetail                     ║
 * ║ 使用场景: UART通用数据触发时传递条件选项和编辑值                                ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUartData implements ISerialsDetail { // UART数据触发详情，实现ISerialsDetail
    private TopBeanChannel uartDataCondition; // UART数据触发条件选项
    private DataBean uartDataEdit; // UART数据编辑值
    private String uartDataConditionTitle; // 条件字段标题
    private String uartDataEditTitle; // 编辑值字段标题

    /**
     * 获取条件字段标题
     * @return 标题字符串
     */
    public String getUartDataConditionTitle() { // 获取条件标题
        return uartDataConditionTitle; // 返回标题
    }

    /**
     * 设置条件字段标题
     * @param uartDataConditionTitle 标题字符串
     */
    public void setUartDataConditionTitle(String uartDataConditionTitle) { // 设置条件标题
        this.uartDataConditionTitle = uartDataConditionTitle; // 赋值标题
    }

    /**
     * 获取编辑值字段标题
     * @return 标题字符串
     */
    public String getUartDataEditTitle() { // 获取编辑值标题
        return uartDataEditTitle; // 返回标题
    }

    /**
     * 设置编辑值字段标题
     * @param uartDataEditTitle 标题字符串
     */
    public void setUartDataEditTitle(String uartDataEditTitle) { // 设置编辑值标题
        this.uartDataEditTitle = uartDataEditTitle; // 赋值标题
    }

    /**
     * 获取UART数据触发条件选项
     * @return TopBeanChannel 条件选项对象
     */
    public TopBeanChannel getUartDataCondition() { // 获取条件选项
        return uartDataCondition; // 返回TopBeanChannel
    }

    /**
     * 设置UART数据触发条件选项
     * @param uartDataCondition 条件选项对象
     */
    public void setUartDataCondition(TopBeanChannel uartDataCondition) { // 设置条件选项
        this.uartDataCondition = uartDataCondition; // 赋值条件选项
    }

    /**
     * 获取UART数据编辑值
     * @return DataBean 编辑值对象
     */
    public DataBean getUartDataEdit() { // 获取编辑值
        return uartDataEdit; // 返回DataBean
    }

    /**
     * 设置UART数据编辑值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setUartDataEdit(int digits, String value) { // 设置编辑值
        if (this.uartDataEdit == null) { // 如果DataBean为空
            this.uartDataEdit = new DataBean(); // 懒初始化创建DataBean
        }
        this.uartDataEdit.setDigits(digits); // 设置进制
        this.uartDataEdit.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailUartData{" + // 开始构建字符串
                "uartDataCondition=" + uartDataCondition + // 拼接条件字段
                ", uartDataEdit='" + uartDataEdit + '\'' + // 拼接编辑值字段
                '}'; // 结束字符串构建
    }
}
