package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║              SerialsDetailArinc429Sdi（ARINC429 SDI触发详情）                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429Sdi.java                      ║
 * ║ 核心职责: 封装ARINC429 SDI触发条件的详情数据                                    ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429 SDI触发时传递SDI值                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Sdi implements ISerialsDetail { // ARINC429 SDI触发详情，实现ISerialsDetail
    private DataBean arinc429SdiLabel; // ARINC429 SDI值
    private String arinc429SdiLabelTitle; // SDI字段标题

    /**
     * 获取SDI字段标题
     * @return 标题字符串
     */
    public String getArinc429SdiLabelTitle() { // 获取SDI标题
        return arinc429SdiLabelTitle; // 返回标题
    }

    /**
     * 设置SDI字段标题
     * @param arinc429SdiLabelTitle 标题字符串
     */
    public void setArinc429SdiLabelTitle(String arinc429SdiLabelTitle) { // 设置SDI标题
        this.arinc429SdiLabelTitle = arinc429SdiLabelTitle; // 赋值标题
    }

    /**
     * 获取ARINC429 SDI值
     * @return DataBean SDI对象
     */
    public DataBean getArinc429SdiLabel() { // 获取SDI值
        return arinc429SdiLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429 SDI值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429SdiLabel(int digits, String value) { // 设置SDI值
        if (this.arinc429SdiLabel == null) { // 如果DataBean为空
            this.arinc429SdiLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429SdiLabel.setDigits(digits); // 设置进制
        this.arinc429SdiLabel.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429Sdi{" + // 开始构建字符串
                "arinc429SdiLabel='" + arinc429SdiLabel + '\'' + // 拼接SDI字段
                '}'; // 结束字符串构建
    }
}
