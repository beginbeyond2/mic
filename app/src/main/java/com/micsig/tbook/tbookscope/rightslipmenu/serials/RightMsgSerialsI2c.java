package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                         I2C串口协议详情消息                                |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— I2C协议参数的数据封装              |
 * | 核心职责: 封装I2C总线配置的SDA/SCL通道选择参数，                           |
 * |          并管理RxMsg选中状态以驱动UI高亮                                   |
 * | 架构设计: 实现 ISerialsDetails 接口，作为 RightMsgSerials 的多态详情字段   |
 * | 数据流向: RightLayoutSerialsI2c → RightMsgSerialsI2c → RightMsgSerials   |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: RightBeanSelect, ISerialsDetails                                |
 * | 使用场景: I2C协议的SDA/SCL通道选择变更时，封装变更数据传递给上层           |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * I2C串口协议详情消息
 * <p>
 * 封装I2C总线的SDA数据线和SCL时钟线通道选择参数，
 * 实现ISerialsDetails接口以支持多态传递。
 * </p>
 */
public class RightMsgSerialsI2c implements ISerialsDetails {

    /** SDA数据线通道选择 */
    private RightBeanSelect sda; // SDA数据线通道选择
    /** SCL时钟线通道选择 */
    private RightBeanSelect scl; // SCL时钟线通道选择

    /**
     * 获取SDA数据线通道选择项
     *
     * @return SDA通道选择项
     */
    public RightBeanSelect getSda() {
        return sda; // 返回SDA通道选择
    }

    /**
     * 设置SDA数据线通道选择项
     *
     * @param sda SDA通道选择项
     */
    public void setSda(RightBeanSelect sda) {
        if (this.sda == null) { // 首次赋值
            this.sda = sda; // 直接赋值
        } else { // 非首次赋值
            this.sda = sda; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.sda.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取SCL时钟线通道选择项
     *
     * @return SCL通道选择项
     */
    public RightBeanSelect getScl() {
        return scl; // 返回SCL通道选择
    }

    /**
     * 设置SCL时钟线通道选择项
     *
     * @param scl SCL通道选择项
     */
    public void setScl(RightBeanSelect scl) {
        if (this.scl == null) { // 首次赋值
            this.scl = scl; // 直接赋值
        } else { // 非首次赋值
            this.scl = scl; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.scl.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 清除所有字段的RxMsg选中标记，再由外部设置新选中项
     * <p>
     * 注意：I2C只有两个字段，此方法保留SDA选中、清除SCL选中，
     * 是因为I2C通常以SDA为主导变更项。
     * </p>
     */
    private void setAllUnSelect() {
        this.sda.setRxMsgSelect(true); // SDA保留选中
        this.scl.setRxMsgSelect(false); // SCL清除选中
    }

    /**
     * 返回I2C详情消息的字符串表示
     *
     * @return 包含SDA和SCL信息的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsI2c{" + // 构建字符串
                "sda=" + sda + // SDA信息
                ", scl=" + scl + // SCL信息
                '}';
    }
}
