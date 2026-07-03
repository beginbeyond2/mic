package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 进制常量接口

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                        DataBean（数据封装Bean）                             ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/DataBean.java                                       ║
 * ║ 核心职责: 封装串行触发详情中一个输入字段的数据（进制+值）                          ║
 * ║ 架构设计: 实现IDigits接口，持有进制和值的简单POJO                               ║
 * ║ 数据流向: 详情Fragment → DataBean → Command下发                             ║
 * ║ 依赖关系: 实现IDigits接口，被所有SerialsDetail*类使用                          ║
 * ║ 使用场景: 串行触发详情中每个可编辑输入框的数据载体                                ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class DataBean implements IDigits { // 数据封装Bean，实现进制常量接口

    /**
     * 进制常量说明:
     * DIGITS_10 = 10;  // 输入是10进制
     * DIGITS_16 = 16;  // 输入是16进制
     * DIGITS_16X = 17; // 输入是16进制和x
     * DIGITS_8 = 8;    // 输入是8进制
     * DIGITS_4 = 4;    // 输入是4进制
     * DIGITS_2 = 2;    // 输入是2进制
     * DIGITS_2X = 3;   // 输入是2进制和x
     * DIGITS_0_8 = 9;  // 输入是0到9
     */
    private int digits = DIGITS_10; // 输入进制，默认10进制
    private String value; // 输入值字符串

    /**
     * 无参构造方法
     */
    public DataBean() { // 无参构造方法
    }

    /**
     * 全参构造方法
     * @param digits 进制
     * @param value 值
     */
    public DataBean(int digits, String value) { // 全参构造方法
        this.digits = digits; // 设置进制
        this.value = value; // 设置值
    }

    /**
     * 获取进制
     * @return 进制值
     */
    public int getDigits() { // 获取进制的getter方法
        return digits; // 返回进制值
    }

    /**
     * 设置进制
     * @param digits 进制值
     */
    public void setDigits(int digits) { // 设置进制的setter方法
        this.digits = digits; // 赋值进制
    }

    /**
     * 获取值
     * @return 值字符串
     */
    public String getValue() { // 获取值的getter方法
        return value; // 返回值
    }

    /**
     * 设置值
     * @param value 值字符串
     */
    public void setValue(String value) { // 设置值的setter方法
        this.value = value; // 赋值
    }

    @Override
    public String toString() { // 重写toString方法
        return "DataBean{" + // 开始构建字符串
                "digits=" + digits + // 拼接进制字段
                ", value='" + value + '\'' + // 拼接值字段
                '}'; // 结束字符串构建
    }
}
