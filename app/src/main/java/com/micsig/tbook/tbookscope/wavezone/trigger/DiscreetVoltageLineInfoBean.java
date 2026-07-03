package com.micsig.tbook.tbookscope.wavezone.trigger;

import static com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage.VoltageLineType_Value1;

/*
 * +=============================================================================+
 * |                        DiscreetVoltageLineInfoBean                          |
 * +=============================================================================+
 * | 模块定位 : 触发电平线信息数据载体                                              |
 * | 核心职责 : 封装预值电平线在界面上的显示信息，供绘制与状态查询使用                    |
 * | 架构设计 : 纯数据Bean，无业务逻辑，由DiscreetVoltageLine.getShowChannelInfo()生成 |
 * | 数据流向 : DiscreetVoltageLine → 本Bean → 上层调用者读取                     |
 * | 依赖关系 : VoltageLineManage(常量)、ITriggerLine(常量)                        |
 * | 使用场景 : 获取当前电平线在各通道的显示配置信息（通道号、模式、索引等）              |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/7/11.
 * 预值电平线信息Bean
 */
public class DiscreetVoltageLineInfoBean {
    //图标的颜色
    /** 通道ID，对应TChan.Ch1~Ch8 */ // // 图标的颜色
    public int ChannelId; // // 通道标识，决定电平线图标颜色与所属通道

    /** 电平线名称，默认为Value1 */
    public String VoltageLineName=VoltageLineType_Value1; // // 电平线类型名称，默认为第一阈值电平

    /** 显示模式，默认为单箭头模式 */
    public int ShowMode=ITriggerLine.ShowMode_One; // // 显示模式：One=单箭头，Two=高低双箭头

    /**
     * 电平触发索引，TWo的时候是高，低。其它时候为Normal
     *
     */
    /** 电平触发索引，区分高/低/普通电平 */
    public int VoltageLineChannelIndex=ITriggerLine.VoltageLine_Normal; // // 电平线子索引：Normal=普通，High=高，Low=低

    /**
     * 返回Bean的字符串描述，用于调试日志输出
     * @return 包含所有字段值的字符串
     */
    @Override
    public String toString() { // // 重写toString，输出所有字段便于调试
        return "DiscreetVoltageLineInfoBean{" + // // 拼接类名
                "ChannelId=" + ChannelId + // // 拼接通道ID
                ", VoltageLineName='" + VoltageLineName + '\'' + // // 拼接电平线名称
                ", ShowMode=" + ShowMode + // // 拼接显示模式
                ", VoltageLineChannelIndex=" + VoltageLineChannelIndex + // // 拼接电平触发索引
                '}'; // // 结束大括号
    }
}
