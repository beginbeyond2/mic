package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                         UART串口协议详情消息                               |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— UART协议参数的数据封装             |
 * | 核心职责: 封装UART总线配置的RX通道、空闲电平、校验位、数据位、             |
 * |          显示格式、波特率及自定义波特率参数，并管理RxMsg选中状态           |
 * | 架构设计: 实现 ISerialsDetails 和 IDigits 接口，作为 RightMsgSerials 的   |
 * |          多态详情字段，同时提供进制转换和位宽解析支持                       |
 * | 数据流向: RightLayoutSerialsUart → RightMsgSerialsUart → RightMsgSerials |
 * |          → RxBus → 消费方                                                 |
 * | 依赖关系: RxStringWithSelect, RightBeanSelect, ISerialsDetails, IDigits   |
 * | 使用场景: UART协议参数变更时，封装变更数据传递给上层                       |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * UART串口协议详情消息
 * <p>
 * 封装UART总线的接收通道(RX)、空闲电平、校验方式、数据位宽、
 * 显示格式、波特率及自定义波特率等参数，
 * 实现ISerialsDetails接口以支持多态传递，实现IDigits接口以支持进制转换。
 * </p>
 */
public class RightMsgSerialsUart implements ISerialsDetails,IDigits {

    /** 接收通道选择 */
    private RightBeanSelect rx; // 接收通道选择
    /** 空闲电平选择 */
    private RightBeanSelect idleLevel; // 空闲电平选择
    /** 校验方式选择（无校验/奇校验/偶校验） */
    private RightBeanSelect check; // 校验方式选择
    /** 数据位宽选择 */
    private RightBeanSelect bits; // 数据位宽选择
    /** 波特率选择 */
    private RightBeanSelect baudRate; // 波特率选择
    /** 显示格式选择（HEX/BIN/ASC） */
    private RightBeanSelect display; // 显示格式选择
    /** 自定义波特率值（为空则使用baudRate预设值） */
    private RxStringWithSelect baudRateDefine;//baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

    /**
     * 获取接收通道选择项
     *
     * @return RX通道选择项
     */
    public RightBeanSelect getRx() {
        return rx; // 返回RX通道选择
    }

    /**
     * 设置接收通道选择项
     *
     * @param rx RX通道选择项
     */
    public void setRx(RightBeanSelect rx) {
        if (this.rx == null) { // 首次赋值
            this.rx = rx; // 直接赋值
        } else { // 非首次赋值
            this.rx = rx; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.rx.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
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
     * 获取校验方式选择项
     *
     * @return 校验方式选择项
     */
    public RightBeanSelect getCheck() {
        return check; // 返回校验方式选择
    }

    /**
     * 设置校验方式选择项
     *
     * @param check 校验方式选择项
     */
    public void setCheck(RightBeanSelect check) {
        if (this.check == null) { // 首次赋值
            this.check = check; // 直接赋值
        } else { // 非首次赋值
            this.check = check; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.check.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取数据位宽选择项
     *
     * @return 数据位宽选择项
     */
    public RightBeanSelect getBits() {
        return bits; // 返回数据位宽选择
    }

    /**
     * 设置数据位宽选择项
     *
     * @param bits 数据位宽选择项
     */
    public void setBits(RightBeanSelect bits) {
        if (this.bits == null) { // 首次赋值
            this.bits = bits; // 直接赋值
        } else { // 非首次赋值
            this.bits = bits; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.bits.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 获取波特率选择项
     *
     * @return 波特率选择项
     */
    public RightBeanSelect getBaudRate() {
        return baudRate; // 返回波特率选择
    }

    /**
     * 设置波特率选择项
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
     * @param baudRateDefine 自定义波特率字符串（如"115.2kb/s"）
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
     * 获取显示格式选择项
     *
     * @return 显示格式选择项
     */
    public RightBeanSelect getDisplay() {
        return display; // 返回显示格式选择
    }

    /**
     * 设置显示格式选择项
     *
     * @param display 显示格式选择项
     */
    public void setDisplay(RightBeanSelect display) {
        if (this.display == null) { // 首次赋值
            this.display = display; // 直接赋值
        } else { // 非首次赋值
            this.display = display; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.display.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
        }
    }

    /**
     * 清除所有字段的RxMsg选中标记
     */
    private void setAllUnSelect() {
        rx.setRxMsgSelect(false); // 清除rx选中标记
        idleLevel.setRxMsgSelect(false); // 清除idleLevel选中标记
        check.setRxMsgSelect(false); // 清除check选中标记
        bits.setRxMsgSelect(false); // 清除bits选中标记
        baudRate.setRxMsgSelect(false); // 清除baudRate选中标记
        display.setRxMsgSelect(false); // 清除display选中标记
        baudRateDefine.setRxMsgSelect(false); // 清除baudRateDefine选中标记
    }

    /**
     * 获取数据位宽的整型值
     *
     * @return 位宽整型值（如5、6、7、8等）
     */
    public int getIntBits() {
        return Integer.parseInt(bits.getText().replace("bit", "")); // 去除"bit"后缀并转换为整数
    }

    /**
     * 根据显示格式获取对应的进制位数
     *
     * @param context 上下文，用于获取字符串资源
     * @return 进制位数常量（DIGITS_16或DIGITS_2）
     */
    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsUartDisplay); // 获取UART显示格式选项数组
        if (display.getText().equals(ss[0])) { // 第一个选项（HEX十六进制）
            return DIGITS_16; // 返回十六进制位数
        } else if (display.getText().equals(ss[1])) { // 第二个选项（BIN二进制）
            return DIGITS_2; // 返回二进制位数
        } else { // 其他情况（ASC等）
            return DIGITS_16; // 默认返回十六进制位数
        }
    }

    /**
     * 返回UART详情消息的字符串表示
     *
     * @return 包含所有UART参数的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsUart{" + // 构建字符串
                "rx=" + rx + // 接收通道
                ", idleLevel=" + idleLevel + // 空闲电平
                ", check=" + check + // 校验方式
                ", bits=" + bits + // 数据位宽
                ", baudRate='" + baudRate + '\'' + // 波特率
                ", display=" + display + // 显示格式
                '}';
    }
}
