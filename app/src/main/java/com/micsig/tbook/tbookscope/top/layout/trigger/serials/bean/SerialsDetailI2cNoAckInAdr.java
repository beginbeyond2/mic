package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailI2cNoAckInAdr（I2C地址无应答触发详情）                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailI2cNoAckInAdr.java                     ║
 * ║ 核心职责: 封装I2C地址无应答触发条件的详情数据                                    ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: I2C通信中地址无应答（NACK）触发时传递地址数据                           ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cNoAckInAdr implements ISerialsDetail { // I2C地址无应答触发详情，实现ISerialsDetail
    private DataBean i2cNoAckInAdrData; // I2C地址无应答数据值
    private String i2cNoAckInAdrDataTitle; // 地址数据字段标题

    /**
     * 获取地址数据字段标题
     * @return 标题字符串
     */
    public String getI2cNoAckInAdrDataTitle() { // 获取地址数据标题
        return i2cNoAckInAdrDataTitle; // 返回标题
    }

    /**
     * 设置地址数据字段标题
     * @param i2cNoAckInAdrDataTitle 标题字符串
     */
    public void setI2cNoAckInAdrDataTitle(String i2cNoAckInAdrDataTitle) { // 设置地址数据标题
        this.i2cNoAckInAdrDataTitle = i2cNoAckInAdrDataTitle; // 赋值标题
    }

    /**
     * 获取I2C地址无应答数据值
     * @return DataBean 地址数据对象
     */
    public DataBean getI2cNoAckInAdrData() { // 获取地址数据值
        return i2cNoAckInAdrData; // 返回DataBean
    }

    /**
     * 设置I2C地址无应答数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2cNoAckInAdrData(int digits, String value) { // 设置地址数据值
        if (this.i2cNoAckInAdrData == null) { // 如果DataBean为空
            this.i2cNoAckInAdrData = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2cNoAckInAdrData.setDigits(digits); // 设置进制
        this.i2cNoAckInAdrData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailI2cNoAckInAdr{" + // 开始构建字符串
                "i2cNoAckInAdrData='" + i2cNoAckInAdrData + '\'' + // 拼接地址数据字段
                '}'; // 结束字符串构建
    }
}
