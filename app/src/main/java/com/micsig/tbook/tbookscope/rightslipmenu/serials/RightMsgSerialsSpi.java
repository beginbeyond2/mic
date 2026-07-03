package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                         SPI串口协议详情消息                                |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— SPI协议参数的数据封装              |
 * | 核心职责: 封装SPI总线配置的CLK/DATA/CS通道选择、位数、各信号极性及        |
 * |          CS使能开关参数，并管理RxMsg选中状态以驱动UI高亮                   |
 * | 架构设计: 实现 ISerialsDetails 接口，作为 RightMsgSerials 的多态详情字段   |
 * | 数据流向: RightLayoutSerialsSpi → RightMsgSerialsSpi → RightMsgSerials   |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: RxBooleanWithSelect, RightBeanSelect, ISerialsDetails           |
 * | 使用场景: SPI协议参数变更时，封装变更数据传递给上层                        |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * SPI串口协议详情消息
 * <p>
 * 封装SPI总线的时钟(CLK)、数据(DATA)、片选(CS)通道选择、
 * 数据位数、各信号低电平有效标志及CS使能开关等参数。
 * </p>
 */
public class RightMsgSerialsSpi implements ISerialsDetails {

    /** 时钟线通道选择 */
    private RightBeanSelect clk; // 时钟线通道选择
    /** 数据线通道选择 */
    private RightBeanSelect data; // 数据线通道选择
    /** 片选线通道选择 */
    private RightBeanSelect cs; // 片选线通道选择
    /** 数据位数选择 */
    private RightBeanSelect bit; // 数据位数选择
    /** 时钟线低电平有效标志 */
    private RxBooleanWithSelect clkLow; // 时钟线低电平有效标志
    /** 数据线低电平有效标志 */
    private RxBooleanWithSelect dataLow; // 数据线低电平有效标志
    /** 片选线低电平有效标志 */
    private RxBooleanWithSelect csLow; // 片选线低电平有效标志
    /** 片选使能开关 */
    private RxBooleanWithSelect csSwitch; // 片选使能开关

    /**
     * 获取时钟线通道选择项
     *
     * @return CLK通道选择项
     */
    public RightBeanSelect getClk() {
        return clk; // 返回CLK通道选择
    }

    /**
     * 设置时钟线通道选择项
     *
     * @param clk CLK通道选择项
     */
    public void setClk(RightBeanSelect clk) {
        if (this.clk == null) { // 首次赋值
            this.clk = clk; // 直接赋值
        } else { // 非首次赋值
            this.clk = clk; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.clk.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取数据线通道选择项
     *
     * @return DATA通道选择项
     */
    public RightBeanSelect getData() {
        return data; // 返回DATA通道选择
    }

    /**
     * 设置数据线通道选择项
     *
     * @param data DATA通道选择项
     */
    public void setData(RightBeanSelect data) {
        if (this.data == null) { // 首次赋值
            this.data = data; // 直接赋值
        } else { // 非首次赋值
            this.data = data; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.data.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取片选线通道选择项
     *
     * @return CS通道选择项
     */
    public RightBeanSelect getCs() {
        return cs; // 返回CS通道选择
    }

    /**
     * 设置片选线通道选择项
     *
     * @param cs CS通道选择项
     */
    public void setCs(RightBeanSelect cs) {
        if (this.cs == null) { // 首次赋值
            this.cs = cs; // 直接赋值
        } else { // 非首次赋值
            this.cs = cs; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.cs.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取数据位数选择项
     *
     * @return 位数选择项
     */
    public RightBeanSelect getBit() {
        return bit; // 返回位数选择
    }

    /**
     * 设置数据位数选择项
     *
     * @param bit 位数选择项
     */
    public void setBit(RightBeanSelect bit) {
        if (this.bit == null) { // 首次赋值
            this.bit = bit; // 直接赋值
        } else { // 非首次赋值
            this.bit = bit; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.bit.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取时钟线低电平有效标志
     *
     * @return 时钟线极性封装对象
     */
    public RxBooleanWithSelect getClkLow() {
        return clkLow; // 返回时钟线极性
    }

    /**
     * 设置时钟线低电平有效标志
     *
     * @param low true=低电平有效，false=高电平有效
     */
    public void setClkLow(boolean low) {
        if (this.clkLow == null) { // 首次赋值
            this.clkLow = new RxBooleanWithSelect(low); // 创建新对象
        } else { // 非首次赋值
            this.clkLow.setValue(low); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.clkLow.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取数据线低电平有效标志
     *
     * @return 数据线极性封装对象
     */
    public RxBooleanWithSelect getDataLow() {
        return dataLow; // 返回数据线极性
    }

    /**
     * 设置数据线低电平有效标志
     *
     * @param low true=低电平有效，false=高电平有效
     */
    public void setDataLow(boolean low) {
        if (this.dataLow == null) { // 首次赋值
            this.dataLow = new RxBooleanWithSelect(low); // 创建新对象
        } else { // 非首次赋值
            this.dataLow.setValue(low); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.dataLow.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取片选线低电平有效标志
     *
     * @return 片选线极性封装对象
     */
    public RxBooleanWithSelect getCsLow() {
        return csLow; // 返回片选线极性
    }

    /**
     * 设置片选线低电平有效标志
     *
     * @param low true=低电平有效，false=高电平有效
     */
    public void setCsLow(boolean low) {
        if (this.csLow == null) { // 首次赋值
            this.csLow = new RxBooleanWithSelect(low); // 创建新对象
        } else { // 非首次赋值
            this.csLow.setValue(low); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.csLow.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取片选使能开关状态
     *
     * @return CS开关封装对象
     */
    public RxBooleanWithSelect getCsSwitch() {
        return csSwitch; // 返回CS开关状态
    }

    /**
     * 设置片选使能开关状态
     *
     * @param csSwitch true=启用CS，false=禁用CS
     */
    public void setCsSwitch(boolean csSwitch) {
        if (this.csSwitch == null) { // 首次赋值
            this.csSwitch = new RxBooleanWithSelect(csSwitch); // 创建新对象
        } else { // 非首次赋值
            this.csSwitch.setValue(csSwitch); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.csSwitch.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 清除所有字段的RxMsg选中标记
     */
    private void setAllUnSelect() {
        clk.setRxMsgSelect(false); // 清除clk选中标记
        data.setRxMsgSelect(false); // 清除data选中标记
        cs.setRxMsgSelect(false); // 清除cs选中标记
        bit.setRxMsgSelect(false); // 清除bit选中标记
        clkLow.setRxMsgSelect(false); // 清除clkLow选中标记
        dataLow.setRxMsgSelect(false); // 清除dataLow选中标记
        csLow.setRxMsgSelect(false); // 清除csLow选中标记
        csSwitch.setRxMsgSelect(false); // 清除csSwitch选中标记
    }

    /**
     * 获取数据位数的整型值
     *
     * @return 位数整型值（如8、16等）
     */
    public int getIntBit() {
        return Integer.parseInt(bit.getText().replace("bit", "")); // 去除"bit"后缀并转换为整数
    }

    /**
     * 返回SPI详情消息的字符串表示
     *
     * @return 包含所有SPI参数的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsSpi{" + // 构建字符串
                "clk=" + clk + // 时钟线
                ", data=" + data + // 数据线
                ", cs=" + cs + // 片选线
                ", bit=" + bit + // 位数
                ", clkLow='" + clkLow + '\'' + // 时钟极性
                ", dataLow='" + dataLow + '\'' + // 数据极性
                ", csLow='" + csLow + '\'' + // 片选极性
                ", csSwitch=" + csSwitch + // CS使能
                '}';
    }
}
