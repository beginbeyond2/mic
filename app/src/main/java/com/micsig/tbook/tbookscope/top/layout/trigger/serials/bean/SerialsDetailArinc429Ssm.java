package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║              SerialsDetailArinc429Ssm（ARINC429 SSM触发详情）                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429Ssm.java                      ║
 * ║ 核心职责: 封装ARINC429 SSM触发条件的详情数据                                    ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429 SSM触发时传递SSM值                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Ssm implements ISerialsDetail { // ARINC429 SSM触发详情，实现ISerialsDetail
    private DataBean arinc429SsmLabel; // ARINC429 SSM值
    private String arinc429SsmLabelTitle; // SSM字段标题

    /**
     * 获取SSM字段标题
     * @return 标题字符串
     */
    public String getArinc429SsmLabelTitle() { // 获取SSM标题
        return arinc429SsmLabelTitle; // 返回标题
    }

    /**
     * 设置SSM字段标题
     * @param arinc429SsmLabelTitle 标题字符串
     */
    public void setArinc429SsmLabelTitle(String arinc429SsmLabelTitle) { // 设置SSM标题
        this.arinc429SsmLabelTitle = arinc429SsmLabelTitle; // 赋值标题
    }

    /**
     * 获取ARINC429 SSM值
     * @return DataBean SSM对象
     */
    public DataBean getArinc429SsmLabel() { // 获取SSM值
        return arinc429SsmLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429 SSM值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429SsmLabel(int digits, String value) { // 设置SSM值
        if (this.arinc429SsmLabel == null) { // 如果DataBean为空
            this.arinc429SsmLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429SsmLabel.setDigits(digits); // 设置进制
        this.arinc429SsmLabel.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429Ssm{" + // 开始构建字符串
                "arinc429SsmLabel='" + arinc429SsmLabel + '\'' + // 拼接SSM字段
                '}'; // 结束字符串构建
    }
}
