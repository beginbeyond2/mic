package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                       ARINC429串口协议详情消息                             |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— ARINC429(M429)协议参数的数据封装   |
 * | 核心职责: 封装ARINC429总线配置的信号源、格式、显示格式、波特率参数，       |
 * |          并管理RxMsg选中状态以驱动UI高亮                                   |
 * | 架构设计: 实现 ISerialsDetails 和 IDigits 接口，作为 RightMsgSerials 的   |
 * |          多态详情字段，同时提供进制转换和格式简写支持                       |
 * | 数据流向: RightLayoutSerialsM429 → RightMsgSerialsM429                  |
 * |          → RightMsgSerials → RxBus → 消费方                               |
 * | 依赖关系: RightBeanSelect, ISerialsDetails, IDigits                       |
 * | 使用场景: ARINC429协议参数变更时，封装变更数据传递给上层                   |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * ARINC429串口协议详情消息
 * <p>
 * 封装ARINC429总线的信号源通道、数据格式（LD/LDS/LSDS）、
 * 显示格式（二进制/十六进制）和波特率参数，
 * 实现ISerialsDetails接口以支持多态传递，实现IDigits接口以支持进制转换。
 * </p>
 */
public class RightMsgSerialsM429 implements ISerialsDetails,IDigits {

    /** 信号源通道选择 */
    private RightBeanSelect source; // 信号源通道选择
    /** 数据格式选择（LD/LDS/LSDS） */
    private RightBeanSelect format; // 数据格式选择
    /** 显示格式选择（二进制/十六进制） */
    private RightBeanSelect display; // 显示格式选择
    /** 波特率选择 */
    private RightBeanSelect baudRate; // 波特率选择

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
     * 获取数据格式选择项
     *
     * @return 格式选择项
     */
    public RightBeanSelect getFormat() {
        return format; // 返回格式选择
    }

    /**
     * 设置数据格式选择项
     *
     * @param format 格式选择项
     */
    public void setFormat(RightBeanSelect format) {
        if (this.format == null) { // 首次赋值
            this.format = format; // 直接赋值
        } else { // 非首次赋值
            this.format = format; // 更新值
            setAllUnSelect(); // 清除所有字段的RxMsg选中标记
            this.format.setRxMsgSelect(true); // 标记当前字段为RxMsg选中
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
     * 清除所有字段的RxMsg选中标记
     */
    private void setAllUnSelect() {
        source.setRxMsgSelect(false); // 清除source选中标记
        format.setRxMsgSelect(false); // 清除format选中标记
        display.setRxMsgSelect(false); // 清除display选中标记
        baudRate.setRxMsgSelect(false); // 清除baudRate选中标记
    }

    /**
     * 根据显示格式获取对应的进制位数
     *
     * @param context 上下文，用于获取字符串资源
     * @return 进制位数常量（DIGITS_2或DIGITS_16）
     */
    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsM429Display); // 获取M429显示格式选项数组
        if (display.getText().equals(ss[0])) { // 第一个选项（二进制）
            return DIGITS_2; // 返回二进制位数
        } else if (display.getText().equals(ss[1])) { // 第二个选项（十六进制）
            return DIGITS_16; // 返回十六进制位数
        } else { // 其他情况
            return DIGITS_16; // 默认返回十六进制位数
        }
    }

    /**
     * 返回M429详情消息的字符串表示
     *
     * @return 包含信号源、格式、显示格式和波特率的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsM429{" + // 构建字符串
                "source=" + source + // 信号源
                ", format=" + format + // 格式
                ", display=" + display + // 显示格式
                ", baudRate=" + baudRate + // 波特率
                '}';
    }

    /**
     * 获取数据格式的简写字符串
     *
     * @return 格式简写：LD(0)、LDS(1)、LSDS(2)，未知返回空串
     */
    public String getFormatSimple() {
        switch (format.getIndex()) { // 根据格式索引返回简写
            case 0:
                return "LD"; // Label+Data
            case 1:
                return "LDS"; // Label+Data+SSM
            case 2:
                return "LSDS"; // Label+SDI+Data+SSM
            default:
                return ""; // 未知格式返回空串
        }
    }
}
