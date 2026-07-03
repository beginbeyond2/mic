package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║        SerialsDetailI2c10WriteFrame（I2C 10位写帧触发详情）                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailI2c10WriteFrame.java                   ║
 * ║ 核心职责: 封装I2C 10位地址写帧触发条件的详情数据                                  ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有2个DataBean字段(地址+数据)              ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: I2C 10位地址写帧触发时传递地址和数据值                                   ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2c10WriteFrame implements ISerialsDetail { // I2C 10位写帧触发详情，实现ISerialsDetail
    private DataBean i2c10WriteFrameAddr; // I2C 10位地址值
    private DataBean i2c10WriteFrameData; // I2C写帧数据值
    private String i2c10WriteFrameAddrTitle; // 地址字段标题
    private String i2c10WriteFrameDataTitle; // 数据字段标题

    /**
     * 获取地址字段标题
     * @return 标题字符串
     */
    public String getI2c10WriteFrameAddrTitle() { // 获取地址标题
        return i2c10WriteFrameAddrTitle; // 返回标题
    }

    /**
     * 设置地址字段标题
     * @param i2c10WriteFrameAddrTitle 标题字符串
     */
    public void setI2c10WriteFrameAddrTitle(String i2c10WriteFrameAddrTitle) { // 设置地址标题
        this.i2c10WriteFrameAddrTitle = i2c10WriteFrameAddrTitle; // 赋值标题
    }

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getI2c10WriteFrameDataTitle() { // 获取数据标题
        return i2c10WriteFrameDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param i2c10WriteFrameDataTitle 标题字符串
     */
    public void setI2c10WriteFrameDataTitle(String i2c10WriteFrameDataTitle) { // 设置数据标题
        this.i2c10WriteFrameDataTitle = i2c10WriteFrameDataTitle; // 赋值标题
    }

    /**
     * 获取I2C 10位地址值
     * @return DataBean地址对象
     */
    public DataBean getI2c10WriteFrameAddr() { // 获取地址值
        return i2c10WriteFrameAddr; // 返回DataBean
    }

    /**
     * 设置I2C 10位地址值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2c10WriteFrameAddr(int digits, String value) { // 设置地址值
        if (this.i2c10WriteFrameAddr == null) { // 如果DataBean为空
            this.i2c10WriteFrameAddr = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2c10WriteFrameAddr.setDigits(digits); // 设置进制
        this.i2c10WriteFrameAddr.setValue(value); // 设置值
    }

    /**
     * 获取I2C写帧数据值
     * @return DataBean数据对象
     */
    public DataBean getI2c10WriteFrameData() { // 获取数据值
        return i2c10WriteFrameData; // 返回DataBean
    }

    /**
     * 设置I2C写帧数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2c10WriteFrameData(int digits, String value) { // 设置数据值
        if (this.i2c10WriteFrameData == null) { // 如果DataBean为空
            this.i2c10WriteFrameData = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2c10WriteFrameData.setDigits(digits); // 设置进制
        this.i2c10WriteFrameData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailI2c10WriteFrame{" + // 开始构建字符串
                "i2c10WriteFrameAddr='" + i2c10WriteFrameAddr + '\'' + // 拼接地址字段
                ", i2c10WriteFrameData='" + i2c10WriteFrameData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
