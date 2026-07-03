package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                         LIN串口协议详情消息                                |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— LIN协议参数的数据封装              |
 * | 核心职责: 封装LIN总线配置的信号源、LIN类型、空闲电平、波特率参数，         |
 * |          并管理RxMsg选中状态以驱动UI高亮                                   |
 * | 架构设计: 实现 ISerialsDetails 接口，作为 RightMsgSerials 的多态详情字段   |
 * | 数据流向: RightLayoutSerialsLin → RightMsgSerialsLin → RightMsgSerials   |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: RxStringWithSelect, RightBeanSelect, ISerialsDetails            |
 * | 使用场景: LIN协议参数变更时，封装变更数据传递给上层                        |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * LIN串口协议详情消息
 * <p>
 * 封装LIN总线的信号源通道、LIN协议版本类型、空闲电平、
 * 波特率及自定义波特率等参数。
 * </p>
 */
public class RightMsgSerialsLin implements ISerialsDetails {

    /** 信号源通道选择 */
    private RightBeanSelect source; // 信号源通道选择
    /** LIN协议版本类型选择 */
    private RightBeanSelect linType; // LIN协议版本类型选择
    /** 空闲电平选择 */
    private RightBeanSelect idleLevel; // 空闲电平选择
    /** 标准波特率选择 */
    private RightBeanSelect baudRate; // 标准波特率选择
    /** 自定义波特率值（为空则使用baudRate预设值） */
    private RxStringWithSelect baudRateDefine; // 自定义波特率值
    //baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

    /**
     * 获取LIN协议版本类型选择项
     *
     * @return LIN类型选择项
     */
    public RightBeanSelect getLinType() {
        return linType; // 返回LIN类型选择
    }

    /**
     * 设置LIN协议版本类型选择项
     *
     * @param linType LIN类型选择项
     */
    public void setLinType(RightBeanSelect linType) {
        if (this.linType == null) { // 首次赋值
            this.linType = linType; // 直接赋值
        } else { // 非首次赋值
            this.linType = linType; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.linType.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取信号源通道选择项
     *
     * @return 信号源选择项
     */
    public RightBeanSelect getSource() {
        return source; // 返回信号源选择
    }

    /**
     * 设置信号源通道选择项
     *
     * @param source 信号源选择项
     */
    public void setSource(RightBeanSelect source) {
        if (this.source == null) { // 首次赋值
            this.source = source; // 直接赋值
        } else { // 非首次赋值
            this.source = source; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.source.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取空闲电平选择项
     *
     * @return 空闲电平选择项
     */
    public RightBeanSelect getIdleLevel() {
        return idleLevel; // 返回空闲电平选择
    }

    /**
     * 设置空闲电平选择项
     *
     * @param idleLevel 空闲电平选择项
     */
    public void setIdleLevel(RightBeanSelect idleLevel) {
        if (this.idleLevel == null) { // 首次赋值
            this.idleLevel = idleLevel; // 直接赋值
        } else { // 非首次赋值
            this.idleLevel = idleLevel; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.idleLevel.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取标准波特率选择项
     *
     * @return 波特率选择项
     */
    public RightBeanSelect getBaudRate() {
        return baudRate; // 返回波特率选择
    }

    /**
     * 设置标准波特率选择项
     *
     * @param baudRate 波特率选择项
     */
    public void setBaudRate(RightBeanSelect baudRate) {
        if (this.baudRate == null) { // 首次赋值
            this.baudRate = baudRate; // 直接赋值
        } else { // 非首次赋值
            this.baudRate = baudRate; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.baudRate.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取自定义波特率值
     *
     * @return 自定义波特率封装对象
     */
    public RxStringWithSelect getBaudRateDefine() {
        return baudRateDefine; // 返回自定义波特率
    }

    /**
     * 设置自定义波特率值
     *
     * @param baudRateDefine 自定义波特率字符串（如"19.2kb/s"）
     */
    public void setBaudRateDefine(String baudRateDefine) {
        if (this.baudRateDefine == null) { // 首次赋值
            this.baudRateDefine = new RxStringWithSelect(baudRateDefine); // 创建新对象
        } else { // 非首次赋值
            this.baudRateDefine.setValue(baudRateDefine); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.baudRateDefine.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 清除所有字段的RxMsg选中标记
     */
    private void setAllUnSelect() {
        source.setRxMsgSelect(false); // 清除source选中标记
        linType.setRxMsgSelect(false); // 清除linType选中标记
        idleLevel.setRxMsgSelect(false); // 清除idleLevel选中标记
        baudRate.setRxMsgSelect(false); // 清除baudRate选中标记
        baudRateDefine.setRxMsgSelect(false); // 清除baudRateDefine选中标记
    }

    /**
     * 返回LIN详情消息的字符串表示
     *
     * @return 包含所有LIN参数的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsLin{" + // 构建字符串
                "source=" + source + // 信号源
                ", linType=" + linType + // LIN类型
                ", idleLevel=" + idleLevel + // 空闲电平
                ", baudRate=" + baudRate + // 波特率
                '}';
    }
}
