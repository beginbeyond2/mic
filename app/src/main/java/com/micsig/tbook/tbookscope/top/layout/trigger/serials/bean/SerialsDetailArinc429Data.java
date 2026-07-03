package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║              SerialsDetailArinc429Data（ARINC429数据触发详情）                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailArinc429Data.java                     ║
 * ║ 核心职责: 封装ARINC429数据触发条件的详情数据                                     ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: ARINC429数据触发时传递数据值                                         ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Data implements ISerialsDetail { // ARINC429数据触发详情，实现ISerialsDetail
    private DataBean arinc429DataData; // ARINC429数据值
    private String arinc429DataDataTitle; // 数据字段标题

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getArinc429DataDataTitle() { // 获取数据标题
        return arinc429DataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param arinc429DataDataTitle 标题字符串
     */
    public void setArinc429DataDataTitle(String arinc429DataDataTitle) { // 设置数据标题
        this.arinc429DataDataTitle = arinc429DataDataTitle; // 赋值标题
    }

    /**
     * 获取ARINC429数据值
     * @return DataBean数据对象
     */
    public DataBean getArinc429DataData() { // 获取数据值
        return arinc429DataData; // 返回DataBean
    }

    /**
     * 设置ARINC429数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setArinc429DataData(int digits, String value) { // 设置数据值
        if (this.arinc429DataData == null) { // 如果DataBean为空
            this.arinc429DataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.arinc429DataData.setDigits(digits); // 设置进制
        this.arinc429DataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailArinc429Data{" + // 开始构建字符串
                "arinc429DataData='" + arinc429DataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
