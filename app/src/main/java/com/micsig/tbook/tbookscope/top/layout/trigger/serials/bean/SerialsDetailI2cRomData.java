package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道Bean，封装触发条件选项

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailI2cRomData（I2C ROM数据触发详情）                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailI2cRomData.java                          ║
 * ║ 核心职责: 封装I2C ROM数据触发条件的详情数据                                     ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有TopBeanChannel+DataBean字段           ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合TopBeanChannel和DataBean，实现ISerialsDetail                     ║
 * ║ 使用场景: I2C ROM数据触发时传递条件选项和数据值                                 ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cRomData implements ISerialsDetail { // I2C ROM数据触发详情，实现ISerialsDetail
    private TopBeanChannel i2cRomDataCondition; // I2C ROM数据触发条件选项
    private DataBean i2cRomDataData; // I2C ROM数据值
    private String i2cRomDataConditionTitle; // 条件字段标题
    private String i2cRomDataDataTitle; // 数据字段标题

    /**
     * 获取条件字段标题
     * @return 标题字符串
     */
    public String getI2cRomDataConditionTitle() { // 获取条件标题
        return i2cRomDataConditionTitle; // 返回标题
    }

    /**
     * 设置条件字段标题
     * @param i2cRomDataConditionTitle 标题字符串
     */
    public void setI2cRomDataConditionTitle(String i2cRomDataConditionTitle) { // 设置条件标题
        this.i2cRomDataConditionTitle = i2cRomDataConditionTitle; // 赋值标题
    }

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getI2cRomDataDataTitle() { // 获取数据标题
        return i2cRomDataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param i2cRomDataDataTitle 标题字符串
     */
    public void setI2cRomDataDataTitle(String i2cRomDataDataTitle) { // 设置数据标题
        this.i2cRomDataDataTitle = i2cRomDataDataTitle; // 赋值标题
    }

    /**
     * 获取I2C ROM数据触发条件选项
     * @return TopBeanChannel 条件选项对象
     */
    public TopBeanChannel getI2cRomDataCondition() { // 获取条件选项
        return i2cRomDataCondition; // 返回TopBeanChannel
    }

    /**
     * 设置I2C ROM数据触发条件选项
     * @param i2cRomDataCondition 条件选项对象
     */
    public void setI2cRomDataCondition(TopBeanChannel i2cRomDataCondition) { // 设置条件选项
        this.i2cRomDataCondition = i2cRomDataCondition; // 赋值条件选项
    }

    /**
     * 获取I2C ROM数据值
     * @return DataBean 数据对象
     */
    public DataBean getI2cRomDataData() { // 获取数据值
        return i2cRomDataData; // 返回DataBean
    }

    /**
     * 设置I2C ROM数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setI2cRomDataData(int digits, String value) { // 设置数据值
        if (this.i2cRomDataData == null) { // 如果DataBean为空
            this.i2cRomDataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.i2cRomDataData.setDigits(digits); // 设置进制
        this.i2cRomDataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailI2cRomData{" + // 开始构建字符串
                "i2cRomDataCondition=" + i2cRomDataCondition + // 拼接条件字段
                ", i2cRomDataData='" + i2cRomDataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
