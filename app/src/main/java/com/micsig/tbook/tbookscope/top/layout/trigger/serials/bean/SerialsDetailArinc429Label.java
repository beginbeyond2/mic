package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║             SerialsDetailArinc429Label（ARINC429标签触发详情）                ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429Label.java                    ║
 * ║ 核心职责: 封装ARINC429标签触发条件的详情数据                                     ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429标签触发时传递标签值                                         ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Label implements ISerialsDetail { // ARINC429标签触发详情，实现ISerialsDetail
    private DataBean arinc429LabelLabel; // ARINC429标签值
    private String arinc429LabelLabelTitle; // 标签字段标题

    /**
     * 获取标签字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelLabelTitle() { // 获取标签标题
        return arinc429LabelLabelTitle; // 返回标题
    }

    /**
     * 设置标签字段标题
     * @param arinc429LabelLabelTitle 标题字符串
     */
    public void setArinc429LabelLabelTitle(String arinc429LabelLabelTitle) { // 设置标签标题
        this.arinc429LabelLabelTitle = arinc429LabelLabelTitle; // 赋值标题
    }

    /**
     * 获取ARINC429标签值
     * @return DataBean标签对象
     */
    public DataBean getArinc429LabelLabel() { // 获取标签值
        return arinc429LabelLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429标签值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelLabel(int digits, String value) { // 设置标签值
        if (this.arinc429LabelLabel == null) { // 如果DataBean为空
            this.arinc429LabelLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelLabel.setDigits(digits); // 设置进制
        this.arinc429LabelLabel.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429Label{" + // 开始构建字符串
                "arinc429LabelLabel='" + arinc429LabelLabel + '\'' + // 拼接标签字段
                '}'; // 结束字符串构建
    }
}
