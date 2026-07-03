package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                         CAN串口协议详情消息                                |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— CAN/CAN-FD协议参数的数据封装       |
 * | 核心职责: 封装CAN总线配置的信号源、信号类型、波特率、FD波特率、            |
 * |          采样点及ISO模式参数，并管理RxMsg选中状态                          |
 * | 架构设计: 实现 ISerialsDetails 接口，作为 RightMsgSerials 的多态详情字段   |
 * | 数据流向: RightLayoutSerialsCan → RightMsgSerialsCan → RightMsgSerials   |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: RxStringWithSelect, RightBeanSelect, ISerialsDetails            |
 * | 使用场景: CAN/CAN-FD协议参数变更时，封装变更数据传递给上层                 |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * CAN串口协议详情消息
 * <p>
 * 封装CAN/CAN-FD总线的信号源、信号类型、标准波特率、FD波特率、
 * 自定义波特率、采样点及ISO模式等参数。
 * </p>
 */
public class RightMsgSerialsCan implements ISerialsDetails {

    /** 信号源通道选择 */
    private RightBeanSelect source; // 信号源通道选择
    /** 信号类型（CAN_H/CAN_L/CAN_DIFF）选择 */
    private RightBeanSelect signal; // 信号类型选择
    /** 标准波特率选择 */
    private RightBeanSelect baudRate; // 标准波特率选择
    /** 自定义波特率值（为空则使用baudRate预设值） */
    private RxStringWithSelect baudRateDefine; // 自定义波特率值
    /** CAN-FD数据相位波特率选择 */
    private RightBeanSelect fdBaudRate; // CAN-FD数据相位波特率选择
    /** CAN-FD自定义波特率值（为空则使用fdBaudRate预设值） */
    private RxStringWithSelect fdBaudRateDefine; // CAN-FD自定义波特率值
    /** ISO模式选择 */
    private RightBeanSelect iSO; // ISO模式选择
    //baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

    /**
     * 获取信号源通道选择项
     *
     * @return 信号源通道选择项
     */
    public RightBeanSelect getSource() {
        return source; // 返回信号源选择
    }

    /**
     * 设置信号源通道选择项
     *
     * @param source 信号源通道选择项
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
     * 获取信号类型选择项
     *
     * @return 信号类型选择项
     */
    public RightBeanSelect getSignal() {
        return signal; // 返回信号类型选择
    }

    /**
     * 设置信号类型选择项
     *
     * @param signal 信号类型选择项
     */
    public void setSignal(RightBeanSelect signal) {
        if (this.signal == null) { // 首次赋值
            this.signal = signal; // 直接赋值
        } else { // 非首次赋值
            this.signal = signal; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.signal.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取标准波特率选择项
     *
     * @return 标准波特率选择项
     */
    public RightBeanSelect getBaudRate() {
        return baudRate; // 返回标准波特率选择
    }

    /**
     * 设置标准波特率选择项
     *
     * @param baudRate 标准波特率选择项
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
     * @param baudRateDefine 自定义波特率字符串（如"500kb/s"）
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
     * 获取CAN-FD数据相位波特率选择项
     *
     * @return CAN-FD波特率选择项
     */
    public RightBeanSelect getFDBaudRate() {
        return fdBaudRate; // 返回FD波特率选择
    }

    /**
     * 设置CAN-FD数据相位波特率选择项
     *
     * @param fdBaudRate CAN-FD波特率选择项
     */
    public void setFDBaudRate(RightBeanSelect fdBaudRate) {
        if (this.fdBaudRate == null) { // 首次赋值
            this.fdBaudRate = fdBaudRate; // 直接赋值
        } else { // 非首次赋值
            this.fdBaudRate = fdBaudRate; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.fdBaudRate.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取CAN-FD自定义波特率值
     *
     * @return CAN-FD自定义波特率封装对象
     */
    public RxStringWithSelect getFDBaudRateDefine() {
        return fdBaudRateDefine; // 返回FD自定义波特率
    }

    /**
     * 设置CAN-FD自定义波特率值
     *
     * @param fdBaudRateDefine CAN-FD自定义波特率字符串
     */
    public void setFDBaudRateDefine(String fdBaudRateDefine) {
        if (this.fdBaudRateDefine == null) { // 首次赋值
            this.fdBaudRateDefine = new RxStringWithSelect(fdBaudRateDefine); // 创建新对象
        } else { // 非首次赋值
            this.fdBaudRateDefine.setValue(fdBaudRateDefine); // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.fdBaudRateDefine.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取ISO模式选择项
     *
     * @return ISO模式选择项
     */
    public RightBeanSelect getISO() {
        return iSO; // 返回ISO模式选择
    }

    /**
     * 设置ISO模式选择项
     *
     * @param iSO ISO模式选择项
     */
    public void setISO(RightBeanSelect iSO) {
        if (this.iSO == null) { // 首次赋值
            this.iSO = iSO; // 直接赋值
        } else { // 非首次赋值
            this.iSO = iSO; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.iSO.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 清除所有字段的RxMsg选中标记
     */
    private void setAllUnSelect() {
        this.source.setRxMsgSelect(false); // 清除source选中标记
        this.signal.setRxMsgSelect(false); // 清除signal选中标记
        this.baudRate.setRxMsgSelect(false); // 清除baudRate选中标记
        this.fdBaudRate.setRxMsgSelect(false); // 清除fdBaudRate选中标记
        this.iSO.setRxMsgSelect(false); // 清除iSO选中标记
        baudRateDefine.setRxMsgSelect(false); // 清除baudRateDefine选中标记
        fdBaudRateDefine.setRxMsgSelect(false); // 清除fdBaudRateDefine选中标记
    }

    /**
     * 返回CAN详情消息的字符串表示
     *
     * @return 包含所有CAN参数的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsCan{" + // 构建字符串
                "source=" + source + // 信号源
                ", signal=" + signal + // 信号类型
                ", baudRate=" + baudRate + // 标准波特率
                ", baudRateDefine=" + baudRateDefine + // 自定义波特率
                ", fdBaudRate=" + fdBaudRate + // FD波特率
                ", fdBaudRateDefine=" + fdBaudRateDefine + // FD自定义波特率
                ", iSO=" + iSO + // ISO模式
                '}';
    }
}
