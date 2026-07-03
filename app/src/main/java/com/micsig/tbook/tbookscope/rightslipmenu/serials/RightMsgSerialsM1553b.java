package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * +--------------------------------------------------------------------------+
 * |                      MIL-STD-1553B串口协议详情消息                         |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— M1553B协议参数的数据封装           |
 * | 核心职责: 封装MIL-STD-1553B总线配置的信号源和显示格式参数，               |
 * |          并管理RxMsg选中状态以驱动UI高亮                                   |
 * | 架构设计: 实现 ISerialsDetails 和 IDigits 接口，作为 RightMsgSerials 的   |
 * |          多态详情字段，同时提供进制转换支持                                 |
 * | 数据流向: RightLayoutSerialsM1553B → RightMsgSerialsM1553b              |
 * |          → RightMsgSerials → RxBus → 消费方                               |
 * | 依赖关系: RightBeanSelect, ISerialsDetails, IDigits                       |
 * | 使用场景: M1553B协议的信号源/显示格式变更时，封装变更数据传递给上层        |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * MIL-STD-1553B串口协议详情消息
 * <p>
 * 封装MIL-STD-1553B总线的信号源通道和显示格式（二进制/十六进制）参数，
 * 实现ISerialsDetails接口以支持多态传递，实现IDigits接口以支持进制转换。
 * </p>
 */
public class RightMsgSerialsM1553b implements ISerialsDetails,IDigits {

    /** 信号源通道选择 */
    private RightBeanSelect source; // 信号源通道选择
    /** 显示格式选择（二进制/十六进制） */
    private RightBeanSelect display; // 显示格式选择

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
        source.setRxMsgSelect(false); // 清除source选中标记
        display.setRxMsgSelect(false); // 清除display选中标记
    }

    /**
     * 根据显示格式获取对应的进制位数
     *
     * @param context 上下文，用于获取字符串资源
     * @return 进制位数常量（DIGITS_2或DIGITS_16）
     */
    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsM1553bDisplay); // 获取M1553B显示格式选项数组
        if (display.getText().equals(ss[0])) { // 第一个选项（二进制）
            return DIGITS_2; // 返回二进制位数
        } else if (display.getText().equals(ss[1])) { // 第二个选项（十六进制）
            return DIGITS_16; // 返回十六进制位数
        } else { // 其他情况
            return DIGITS_16; // 默认返回十六进制位数
        }
    }

    /**
     * 返回M1553B详情消息的字符串表示
     *
     * @return 包含信号源和显示格式的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerialsM1553b{" + // 构建字符串
                "source=" + source + // 信号源
                ", display=" + display + // 显示格式
                '}';
    }
}
