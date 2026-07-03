package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║           SerialsDetailArinc429LabelSdi（ARINC429标签+SDI触发详情）            ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429LabelSdi.java                 ║
 * ║ 核心职责: 封装ARINC429标签+SDI组合触发条件的详情数据                              ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有2个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429标签+SDI组合触发时传递标签和SDI值                              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429LabelSdi implements ISerialsDetail { // ARINC429标签+SDI触发详情，实现ISerialsDetail
    private DataBean arinc429LabelSdiLabel; // ARINC429标签值
    private DataBean arinc429LabelSdiSdi; // ARINC429 SDI值
    private String arinc429LabelSdiLabelTitle; // 标签字段标题
    private String arinc429LabelSdiSdiTitle; // SDI字段标题

    /**
     * 获取标签字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelSdiLabelTitle() { // 获取标签标题
        return arinc429LabelSdiLabelTitle; // 返回标题
    }

    /**
     * 设置标签字段标题
     * @param arinc429LabelSdiLabelTitle 标题字符串
     */
    public void setArinc429LabelSdiLabelTitle(String arinc429LabelSdiLabelTitle) { // 设置标签标题
        this.arinc429LabelSdiLabelTitle = arinc429LabelSdiLabelTitle; // 赋值标题
    }

    /**
     * 获取SDI字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelSdiSdiTitle() { // 获取SDI标题
        return arinc429LabelSdiSdiTitle; // 返回标题
    }

    /**
     * 设置SDI字段标题
     * @param arinc429LabelSdiSdiTitle 标题字符串
     */
    public void setArinc429LabelSdiSdiTitle(String arinc429LabelSdiSdiTitle) { // 设置SDI标题
        this.arinc429LabelSdiSdiTitle = arinc429LabelSdiSdiTitle; // 赋值标题
    }

    /**
     * 获取ARINC429标签值
     * @return DataBean标签对象
     */
    public DataBean getArinc429LabelSdiLabel() { // 获取标签值
        return arinc429LabelSdiLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429标签值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelSdiLabel(int digits, String value) { // 设置标签值
        if (this.arinc429LabelSdiLabel == null) { // 如果DataBean为空
            this.arinc429LabelSdiLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelSdiLabel.setDigits(digits); // 设置进制
        this.arinc429LabelSdiLabel.setValue(value); // 设置值
    }

    /**
     * 获取ARINC429 SDI值
     * @return DataBean SDI对象
     */
    public DataBean getArinc429LabelSdiSdi() { // 获取SDI值
        return arinc429LabelSdiSdi; // 返回DataBean
    }

    /**
     * 设置ARINC429 SDI值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelSdiSdi(int digits, String value) { // 设置SDI值
        if (this.arinc429LabelSdiSdi == null) { // 如果DataBean为空
            this.arinc429LabelSdiSdi = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelSdiSdi.setDigits(digits); // 设置进制
        this.arinc429LabelSdiSdi.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429LabelSdi{" + // 开始构建字符串
                "arinc429LabelSdiLabel='" + arinc429LabelSdiLabel + '\'' + // 拼接标签字段
                ", arinc429LabelSdiSdi='" + arinc429LabelSdiSdi + '\'' + // 拼接SDI字段
                '}'; // 结束字符串构建
    }
}
