package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailI2cFrame2（I2C帧2触发详情）                           ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailI2cFrame2.java                         ║
 * ║ 核心职责: 封装I2C帧2（地址+2字节数据）触发条件的详情数据                          ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有3个DataBean字段(地址+数据1+数据2)       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: I2C帧2触发时传递地址和两字节数据值                                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cFrame2 implements ISerialsDetail { // I2C帧2触发详情，实现ISerialsDetail
    private DataBean i2cFrame2Addr; // I2C帧2地址值
    private DataBean i2cFrame2Data1; // I2C帧2数据1值
    private DataBean i2cFrame2Data2; // I2C帧2数据2值
    private String i2cFrame2AddrTitle; // 地址字段标题
    private String i2cFrame2Data1Title; // 数据1字段标题
    private String i2cFrame2Data2Title; // 数据2字段标题

    /**
     * 获取地址字段标题
     * @return 标题字符串
     */
    public String getI2cFrame2AddrTitle() { // 获取地址标题
        return i2cFrame2AddrTitle; // 返回标题
    }

    /**
     * 设置地址字段标题
     * @param i2cFrame2AddrTitle 标题字符串
     */
    public void setI2cFrame2AddrTitle(String i2cFrame2AddrTitle) { // 设置地址标题
        this.i2cFrame2AddrTitle = i2cFrame2AddrTitle; // 赋值标题
    }

    /**
     * 获取数据1字段标题
     * @return 标题字符串
     */
    public String getI2cFrame2Data1Title() { // 获取数据1标题
        return i2cFrame2Data1Title; // 返回标题
    }

    /**
     * 设置数据1字段标题
     * @param i2cFrame2Data1Title 标题字符串
     */
    public void setI2cFrame2Data1Title(String i2cFrame2Data1Title) { // 设置数据1标题
        this.i2cFrame2Data1Title = i2cFrame2Data1Title; // 赋值标题
    }

    /**
     * 获取数据2字段标题
     * @return 标题字符串
     */
    public String getI2cFrame2Data2Title() { // 获取数据2标题
        return i2cFrame2Data2Title; // 返回标题
    }

    /**
     * 设置数据2字段标题
     * @param i2cFrame2Data2Title 标题字符串
     */
    public void setI2cFrame2Data2Title(String i2cFrame2Data2Title) { // 设置数据2标题
        this.i2cFrame2Data2Title = i2cFrame2Data2Title; // 赋值标题
    }

    /**
     * 获取I2C帧2地址值
     * @return DataBean地址对象
     */
    public DataBean getI2cFrame2Addr() { // 获取地址值
        return i2cFrame2Addr; // 返回DataBean
    }

    /**
     * 设置I2C帧2地址值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2cFrame2Addr(int digits, String value) { // 设置地址值
        if (this.i2cFrame2Addr == null) { // 如果DataBean为空
            this.i2cFrame2Addr = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2cFrame2Addr.setDigits(digits); // 设置进制
        this.i2cFrame2Addr.setValue(value); // 设置值
    }

    /**
     * 获取I2C帧2数据1值
     * @return DataBean数据1对象
     */
    public DataBean getI2cFrame2Data1() { // 获取数据1值
        return i2cFrame2Data1; // 返回DataBean
    }

    /**
     * 设置I2C帧2数据1值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2cFrame2Data1(int digits, String value) { // 设置数据1值
        if (this.i2cFrame2Data1 == null) { // 如果DataBean为空
            this.i2cFrame2Data1 = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2cFrame2Data1.setDigits(digits); // 设置进制
        this.i2cFrame2Data1.setValue(value); // 设置值
    }

    /**
     * 获取I2C帧2数据2值
     * @return DataBean数据2对象
     */
    public DataBean getI2cFrame2Data2() { // 获取数据2值
        return i2cFrame2Data2; // 返回DataBean
    }

    /**
     * 设置I2C帧2数据2值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2cFrame2Data2(int digits, String value) { // 设置数据2值
        if (this.i2cFrame2Data2 == null) { // 如果DataBean为空
            this.i2cFrame2Data2 = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2cFrame2Data2.setDigits(digits); // 设置进制
        this.i2cFrame2Data2.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailI2cFrame2{" + // 开始构建字符串
                "i2cFrame2Addr='" + i2cFrame2Addr + '\'' + // 拼接地址字段
                ", i2cFrame2Data1='" + i2cFrame2Data1 + '\'' + // 拼接数据1字段
                ", i2cFrame2Data2='" + i2cFrame2Data2 + '\'' + // 拼接数据2字段
                '}'; // 结束字符串构建
    }
}
