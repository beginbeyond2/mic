package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║           SerialsDetailArinc429LabelSsm（ARINC429标签+SSM触发详情）            ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429LabelSsm.java                 ║
 * ║ 核心职责: 封装ARINC429标签+SSM组合触发条件的详情数据                              ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有2个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429标签+SSM组合触发时传递标签和SSM值                              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429LabelSsm implements ISerialsDetail { // ARINC429标签+SSM触发详情，实现ISerialsDetail
    private DataBean arinc429LabelSsmLabel; // ARINC429标签值
    private DataBean arinc429LabelSsmSsm; // ARINC429 SSM值
    private String arinc429LabelSsmLabelTitle; // 标签字段标题
    private String arinc429LabelSsmSsmTitle; // SSM字段标题

    /**
     * 获取标签字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelSsmLabelTitle() { // 获取标签标题
        return arinc429LabelSsmLabelTitle; // 返回标题
    }

    /**
     * 设置标签字段标题
     * @param arinc429LabelSsmLabelTitle 标题字符串
     */
    public void setArinc429LabelSsmLabelTitle(String arinc429LabelSsmLabelTitle) { // 设置标签标题
        this.arinc429LabelSsmLabelTitle = arinc429LabelSsmLabelTitle; // 赋值标题
    }

    /**
     * 获取SSM字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelSsmSsmTitle() { // 获取SSM标题
        return arinc429LabelSsmSsmTitle; // 返回标题
    }

    /**
     * 设置SSM字段标题
     * @param arinc429LabelSsmSsmTitle 标题字符串
     */
    public void setArinc429LabelSsmSsmTitle(String arinc429LabelSsmSsmTitle) { // 设置SSM标题
        this.arinc429LabelSsmSsmTitle = arinc429LabelSsmSsmTitle; // 赋值标题
    }

    /**
     * 获取ARINC429标签值
     * @return DataBean标签对象
     */
    public DataBean getArinc429LabelSsmLabel() { // 获取标签值
        return arinc429LabelSsmLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429标签值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelSsmLabel(int digits, String value) { // 设置标签值
        if (this.arinc429LabelSsmLabel == null) { // 如果DataBean为空
            this.arinc429LabelSsmLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelSsmLabel.setDigits(digits); // 设置进制
        this.arinc429LabelSsmLabel.setValue(value); // 设置值
    }

    /**
     * 获取ARINC429 SSM值
     * @return DataBean SSM对象
     */
    public DataBean getArinc429LabelSsmSsm() { // 获取SSM值
        return arinc429LabelSsmSsm; // 返回DataBean
    }

    /**
     * 设置ARINC429 SSM值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelSsmSsm(int digits, String value) { // 设置SSM值
        if (this.arinc429LabelSsmSsm == null) { // 如果DataBean为空
            this.arinc429LabelSsmSsm = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelSsmSsm.setDigits(digits); // 设置进制
        this.arinc429LabelSsmSsm.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429LabelSsm{" + // 开始构建字符串
                "arinc429LabelSsmLabel='" + arinc429LabelSsmLabel + '\'' + // 拼接标签字段
                ", arinc429LabelSsmSsm='" + arinc429LabelSsmSsm + '\'' + // 拼接SSM字段
                '}'; // 结束字符串构建
    }
}
