package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║          SerialsDetailArinc429LabelData（ARINC429标签+数据触发详情）            ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429LabelData.java                ║
 * ║ 核心职责: 封装ARINC429标签+数据组合触发条件的详情数据                              ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有2个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429标签+数据组合触发时传递标签和数据值                              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429LabelData implements ISerialsDetail { // ARINC429标签+数据触发详情，实现ISerialsDetail
    private DataBean arinc429LabelDataLabel; // ARINC429标签值
    private DataBean arinc429LabelDataData; // ARINC429数据值
    private String arinc429LabelDataLabelTitle; // 标签字段标题
    private String arinc429LabelDataDataTitle; // 数据字段标题

    /**
     * 获取标签字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelDataLabelTitle() { // 获取标签标题
        return arinc429LabelDataLabelTitle; // 返回标题
    }

    /**
     * 设置标签字段标题
     * @param arinc429LabelDataLabelTitle 标题字符串
     */
    public void setArinc429LabelDataLabelTitle(String arinc429LabelDataLabelTitle) { // 设置标签标题
        this.arinc429LabelDataLabelTitle = arinc429LabelDataLabelTitle; // 赋值标题
    }

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getArinc429LabelDataDataTitle() { // 获取数据标题
        return arinc429LabelDataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param arinc429LabelDataDataTitle 标题字符串
     */
    public void setArinc429LabelDataDataTitle(String arinc429LabelDataDataTitle) { // 设置数据标题
        this.arinc429LabelDataDataTitle = arinc429LabelDataDataTitle; // 赋值标题
    }

    /**
     * 获取ARINC429标签值
     * @return DataBean标签对象
     */
    public DataBean getArinc429LabelDataLabel() { // 获取标签值
        return arinc429LabelDataLabel; // 返回DataBean
    }

    /**
     * 设置ARINC429标签值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelDataLabel(int digits, String value) { // 设置标签值
        if (this.arinc429LabelDataLabel == null) { // 如果DataBean为空
            this.arinc429LabelDataLabel = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelDataLabel.setDigits(digits); // 设置进制
        this.arinc429LabelDataLabel.setValue(value); // 设置值
    }

    /**
     * 获取ARINC429数据值
     * @return DataBean数据对象
     */
    public DataBean getArinc429LabelDataData() { // 获取数据值
        return arinc429LabelDataData; // 返回DataBean
    }

    /**
     * 设置ARINC429数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429LabelDataData(int digits, String value) { // 设置数据值
        if (this.arinc429LabelDataData == null) { // 如果DataBean为空
            this.arinc429LabelDataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429LabelDataData.setDigits(digits); // 设置进制
        this.arinc429LabelDataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429LabelData{" + // 开始构建字符串
                "arinc429LabelDataLabel='" + arinc429LabelDataLabel + '\'' + // 拼接标签字段
                ", arinc429LabelDataData='" + arinc429LabelDataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
