package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailSpiData（SPI数据触发详情）                            ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailSpiData.java                             ║
 * ║ 核心职责: 封装SPI数据触发条件的详情数据                                        ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: SPI总线数据触发时传递MOSI/MISO数据值                                 ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailSpiData implements ISerialsDetail { // SPI数据触发详情，实现ISerialsDetail
    private DataBean spiDataData; // SPI数据值
    private String spiDataDataTitle; // 数据字段标题

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getSpiDataDataTitle() { // 获取数据标题
        return spiDataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param spiDataDataTitle 标题字符串
     */
    public void setSpiDataDataTitle(String spiDataDataTitle) { // 设置数据标题
        this.spiDataDataTitle = spiDataDataTitle; // 赋值标题
    }

    /**
     * 获取SPI数据值
     * @return DataBean 数据对象
     */
    public DataBean getSpiDataData() { // 获取数据值
        return spiDataData; // 返回DataBean
    }

    /**
     * 设置SPI数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setSpiDataData(int digits, String value) { // 设置数据值
        if (this.spiDataData == null) { // 如果DataBean为空
            this.spiDataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.spiDataData.setDigits(digits); // 设置进制
        this.spiDataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailSpiData{" + // 开始构建字符串
                "spiDataData='" + spiDataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
