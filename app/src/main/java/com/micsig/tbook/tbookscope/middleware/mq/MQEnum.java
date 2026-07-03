package com.micsig.tbook.tbookscope.middleware.mq;

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    MQEnum — MQ消息类型枚举                               ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：middleware层 · 消息队列(MQ)子系统 · 消息类型定义                ║
 * ║ 核心职责：定义所有MQ消息的语义类型枚举，为RxBus消息提供统一的类型标识      ║
 * ║ 架构设计：枚举单例模式，每个枚举值对应一种具体的消息语义，                  ║
 * ║          配合MQBase.mqType字段标识消息的具体含义                          ║
 * ║ 数据流向：Command层设置mqType → MQBase子类携带 → RxBus传递 → UI层判断    ║
 * ║ 依赖关系：无外部依赖，被MQBase、MQChanSelectorManage等广泛引用            ║
 * ║ 使用场景：在构建MQ消息时指定消息类型，在消费端根据mqType分支处理不同逻辑   ║
 * ║ 枚举分组：                                                               ║
 * ║   · NULL/LOAD_*  — 初始化与加载（FPGA加载/UI加载/加载完成）              ║
 * ║   · CH_*         — 通道相关（激活/开关/位置/反相/耦合/探头/带宽/阻抗等） ║
 * ║   · MATH_*       — 数学运算相关（开关/类型/源/算符/FFT/高级运算等）       ║
 * ║   · REF_*        — 参考波形相关（开关/范围/位置/时基等）                 ║
 * ║   · SERIAL_*     — 串行总线相关（UART/LIN/CAN/SPI/I2C/A429/M1553B）     ║
 * ║   · TRIGGER_*    — 触发器相关（源变更/类型变更/电平变更/移动完成）        ║
 * ║   · THRESHOLD_*  — 阈值相关（电平变更/源变更）                           ║
 * ║   · COMPLETE     — 操作完成通用标识                                      ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * @auother Liwb
 * @description: MQ消息类型枚举，定义所有MQ消息的语义类型标识
 * @data:2024-2-29 10:45
 */
public enum MQEnum {

    // ==================== 空值与加载阶段 ====================

    /** 空值，默认/未定义消息类型 */ // 空值枚举
    NULL("NULL"), // 空值，表示未定义或默认消息类型

    /** 示波器FPGA加载中 */ // FPGA加载阶段
    LOAD_SCOPE_FPGA("LOAD_SCOPE_FPGA"), // FPGA固件加载阶段

    /** 示波器UI加载中 */ // UI加载阶段
    LOAD_SCOPE_UI("LOAD_SCOPE_UI"), // UI界面加载阶段

    /** 示波器加载完成 */ // 加载完成
    LOAD_SCOPE_COMPLETE("LOAD_SCOPE_COMPLETE"), // FPGA和UI均加载完成

    // ==================== 通道（CH）相关 ====================

    /** 通道激活 */ // 通道激活
    CH_ACTIVE("CH_ACTIVE"), // 通道被激活（选中为当前活动通道）

    /** 通道打开 */ // 通道打开
    CH_OPEN("CH_OPEN"), // 通道打开（显示波形）

    /** 通道关闭 */ // 通道关闭
    CH_CLOSE("CH_CLOSE"), // 通道关闭（隐藏波形）

    /** 通道垂直位置变更 */ // 通道位置
    CH_POSITION("CH_POSITION"), // 通道波形垂直位置调整

    /** 通道反相开关变更 */ // 通道反相
    CH_INVERT("CH_INVERT"), // 通道反相开关状态变更

    /** 通道耦合方式变更 */ // 通道耦合
    CH_COUPLE("CH_COUPLE"), // 通道耦合方式（AC/DC/GND）变更

    /** 通道探头类型变更 */ // 通道探头类型
    CH_PROBE("CH_PROBE"), // 通道探头类型（1X/10X/100X等）变更

    /** 通道探头比值变更 */ // 通道探头比值
    CH_PROBE_VALUE("CH_PROBE_VALUE"), // 通道探头衰减比值变更

    /** 通道带宽限制开关变更 */ // 通道带宽限制开关
    CH_BANDWIDTH("CH_BANDWIDTH"), // 通道带宽限制开关（开/关）变更

    /** 通道带宽限制值变更 */ // 通道带宽限制值
    CH_BANDWIDTH_VALUE("CH_BANDWIDTH_VALUE"), // 通道带宽限制具体数值变更

    /** 通道输入阻抗变更 */ // 通道输入阻抗
    CH_IMP("CH_IMP"), // 通道输入阻抗（1MΩ/50Ω）变更

    /** 通道垂直挡位参考变更 */ // 通道垂直挡位参考
    CH_V_SCALE_REF("CH_V_SCALE_REF"), // 通道垂直挡位参考值变更

    /** 通道标签变更 */ // 通道标签
    CH_LABEL("CH_LABEL"), // 通道标签（显示名称）变更

    /** 通道延迟变更 */ // 通道延迟
    CH_DELAY("CH_DELAY"), // 通道延迟参数变更

    /** 通道偏移变更 */ // 通道偏移
    CH_OFFSET("CH_OFFSET"), // 通道偏移参数变更

    /** 通道微调开关变更 */ // 通道微调开关
    CH_FINE("CH_FINE"), // 通道微调开关（开/关）变更

    /** 通道微调值变更 */ // 通道微调值
    CH_FINE_VALUE("CH_FINE_VALUE"), // 通道微调具体数值变更

    /** 通道扩展变更 */ // 通道扩展
    CH_EXTENT("CH_EXTENT"), // 通道扩展（垂直范围）变更

    // ==================== 数学运算（MATH）相关 ====================

    /** 数学通道打开 */ // 数学通道打开
    MATH_OPEN("MATH_OPEN"), // 数学运算通道打开

    /** 数学通道关闭 */ // 数学通道关闭
    MATH_CLOSE("MATH_CLOSE"), // 数学运算通道关闭

    /** 数学运算类型变更 */ // 数学运算类型
    MATH_TYPE("MATH_TYPE"), // 数学运算类型（双波形运算/FFT/高级运算）变更

    /** 数学双波形源1变更 */ // 数学双波形源1
    MATH_DW_SOURCE1("MATH_DW_SOURCE1"), // 双波形运算的源通道1变更

    /** 数学双波形源2变更 */ // 数学双波形源2
    MATH_DW_SOURCE2("MATH_DW_SOURCE2"), // 双波形运算的源通道2变更

    /** 数学双波形算符变更 */ // 数学双波形算符
    MATH_DW_OPERATOR("MATH_DW_OPERATOR"), // 双波形运算的算符（+/-/*/÷）变更

    /** 数学FFT类型变更 */ // 数学FFT类型
    MATH_FFT_TYPE("MATH_FFT_TYPE"), // FFT运算类型变更

    /** 数学FFT源通道变更 */ // 数学FFT源通道
    MATH_FFT_SOURCE("MATH_FFT_SOURCE"), // FFT运算的源通道变更

    /** 数学FFT窗函数变更 */ // 数学FFT窗函数
    MATH_FFT_WINDOW("MATH_FFT_WINDOW"), // FFT窗函数（Rectangular/Hanning/Hamming等）变更

    /** 数学FFT持久化类型变更 */ // 数学FFT持久化类型
    MATH_FFT_PERSIST_TYPE("MATH_FFT_PERSIST_TYPE"), // FFT持久化显示类型变更

    /** 数学FFT持久化值变更 */ // 数学FFT持久化值
    MATH_FFT_PERSIST_VALUE("MATH_FFT_PERSIST_VALUE"), // FFT持久化显示数值变更

    /** 数学A×B运算单位变更 */ // 数学A×B运算单位
    MATH_AXB_UNIT("MATH_AXB_UNIT"), // A×B运算结果的单位变更

    /** 数学A×B运算源变更 */ // 数学A×B运算源
    MATH_AXB_SOUCE("MATH_AXB_SOUCE"), // A×B运算的源通道变更（注：原代码拼写为SOUCE）

    /** 数学A×B系数A变更 */ // 数学A×B系数A
    MATH_AXB_A("MATH_AXB_A"), // A×B运算的系数A变更

    /** 数学A×B系数B变更 */ // 数学A×B系数B
    MATH_AXB_B("MATH_AXB_B"), // A×B运算的系数B变更

    /** 数学高级运算表达式变更 */ // 数学高级运算表达式
    MATH_ADV_EXPRESS("MATH_ADV_EXPRESS"), // 高级运算表达式变更

    /** 数学高级运算值1变更 */ // 数学高级运算值1
    MATH_ADV_VALUE1("MATH_ADV_VALUE1"), // 高级运算的值1变更

    /** 数学高级运算值2变更 */ // 数学高级运算值2
    MATH_ADV_VALUE2("MATH_ADV_VALUE2"), // 高级运算的值2变更

    /** 数学高级运算单位变更 */ // 数学高级运算单位
    MATH_ADV_UNIT("MATH_ADV_UNIT"), // 高级运算结果的单位变更

    /** 数学垂直模式变更 */ // 数学垂直模式
    MATH_VERMODE("MATH_VERMODE"), // 数学通道垂直模式变更

    /** 数学通道垂直位置变更 */ // 数学通道位置
    MATH_POSITION("MATH_POSITION"), // 数学通道波形垂直位置调整

    /** 数学通道扩展变更 */ // 数学通道扩展
    MATH_EXTENT("MATH_EXTENT"), // 数学通道扩展（垂直范围）变更

    // ==================== 参考波形（REF）相关 ====================

    /** 参考波形打开 */ // 参考波形打开
    REF_OPEN("REF_OPEN"), // 参考波形通道打开

    /** 参考波形关闭 */ // 参考波形关闭
    REF_CLOSE("REF_CLOSE"), // 参考波形通道关闭

    /** 参考波形扩展变更 */ // 参考波形扩展
    REF_EXTENT("REF_EXTENT"), // 参考波形的垂直扩展变更

    /** 参考波形位置变更 */ // 参考波形位置
    REF_POSITION("REF_POSITION"), // 参考波形的垂直位置变更

    /** 参考波形时基X变更 */ // 参考波形时基X
    REF_TIMEBASE_X("REF_TIMEBASE_X"), // 参考波形的水平时基变更

    /** 参考波形时基扩展变更 */ // 参考波形时基扩展
    REF_TIMEBASE_EXTENT("REF_TIMEBASE_EXTENT"), // 参考波形的水平时基扩展变更

    // ==================== 串行总线（SERIAL）相关 ====================

    /** 串行总线打开 */ // 串行总线打开
    SERIAL_OPEN("SERIAL_OPEN"), // 串行总线解码通道打开

    /** 串行总线关闭 */ // 串行总线关闭
    SERIAL_CLOSE("SERIAL_CLOSE"), // 串行总线解码通道关闭

    /** 串行总线类型变更 */ // 串行总线类型
    SERIAL_TYPE("SERIAL_TYPE"), // 串行总线类型（UART/LIN/CAN/SPI/I2C/A429/M1553B）变更

    // --- UART子参数 ---

    /** UART源通道变更 */ // UART源通道
    SERIAL_UART_SOURCE("SERIAL_UART_SOURCE"), // UART解码的源通道变更

    /** UART空闲电平变更 */ // UART空闲电平
    SERIAL_UART_IDLE_LEVEL("SERIAL_UART_IDLE_LEVEL"), // UART空闲电平（高/低）变更

    /** UART校验位变更 */ // UART校验位
    SERIAL_UART_CHECK("SERIAL_UART_CHECK"), // UART校验位（无/奇/偶）变更

    /** UART数据位变更 */ // UART数据位
    SERIAL_UART_BIT("SERIAL_UART_BIT"), // UART数据位（7/8/9）变更

    /** UART波特率变更 */ // UART波特率
    SERIAL_UART_BAUD_RATE("SERIAL_UART_BAUD_RATE"), // UART波特率变更

    /** UART显示格式变更 */ // UART显示格式
    SERIAL_UART_DISPLAY_FORMAT("SERIAL_UART_DISPLAY_FORMAT"), // UART解码数据显示格式（Hex/ASCII等）变更

    // --- LIN子参数 ---

    /** LIN源通道变更 */ // LIN源通道
    SERIAL_LIN_SOURCE("SERIAL_LIN_SOURCE"), // LIN解码的源通道变更

    /** LIN空闲电平变更 */ // LIN空闲电平
    SERIAL_LIN_IDLE_LEVEL("SERIAL_LIN_IDLE_LEVEL"), // LIN空闲电平（高/低）变更

    /** LIN波特率变更 */ // LIN波特率
    SERIAL_LIN_BAUD_RATE("SERIAL_LIN_BAUD_RATE"), // LIN波特率变更

    // --- CAN子参数 ---

    /** CAN源通道变更 */ // CAN源通道
    SERIAL_CAN_SOURCE("SERIAL_CAN_SOURCE"), // CAN解码的源通道变更

    /** CAN信号类型变更 */ // CAN信号类型
    SERIAL_CAN_SIGNAL_TYPE("SERIAL_CAN_SIGNAL_TYPE"), // CAN信号类型（CAN/CAN FD）变更

    /** CAN波特率变更 */ // CAN波特率
    SERIAL_CAN_BAUD_RATE("SERIAL_CAN_BAUD_RATE"), // CAN标准波特率变更

    /** CAN波特率百分比变更 */ // CAN波特率百分比
    SERIAL_CAN_BAUD_RATE_PERCENT("SERIAL_CAN_BAUD_RATE_PERCENT"), // CAN波特率采样点百分比变更

    /** CAN FD波特率变更 */ // CAN FD波特率
    SERIAL_CAN_FD_BAUD_RATE("SERIAL_CAN_FD_BAUD_RATE"), // CAN FD数据阶段波特率变更

    /** CAN FD波特率百分比变更 */ // CAN FD波特率百分比
    SERIAL_CAN_FD_BAUD_RATE_PERCENT("SERIAL_CAN_FD_BAUD_RATE_PERCENT"), // CAN FD数据阶段采样点百分比变更

    /** CAN标准变更 */ // CAN标准
    SERIAL_CAN_STANDARD("SERIAL_CAN_STANDARD"), // CAN协议标准（ISO 11898等）变更

    // --- SPI子参数 ---

    /** SPI时钟源变更 */ // SPI时钟源
    SERIAL_SPI_CLOCK_SOURCE("SERIAL_SPI_SOURCE"), // SPI时钟线源通道变更（注：枚举名与value不完全一致）

    /** SPI时钟电平变更 */ // SPI时钟电平
    SERIAL_SPI_CLOCK_LEVEL("SERIAL_SPI_SOURCE_LEVEL"), // SPI时钟线空闲电平变更（注：枚举名与value不完全一致）

    /** SPI数据源变更 */ // SPI数据源
    SERIAL_SPI_DATA("SERIAL_SPI_DATA"), // SPI数据线源通道变更

    /** SPI数据电平变更 */ // SPI数据电平
    SERIAL_SPI_DATA_LEVEL("SERIAL_SPI_DATA_LEVEL"), // SPI数据线空闲电平变更

    /** SPI片选使能变更 */ // SPI片选使能
    SERIAL_SPI_CS_ENABLE("SERIAL_SPI_CS_ENABLE"), // SPI片选使能开关变更

    /** SPI片选源变更 */ // SPI片选源
    SERIAL_SPI_CS_SOURCE("SERIAL_SPI_CS_SOURCE"), // SPI片选线源通道变更

    /** SPI片选电平变更 */ // SPI片选电平
    SERIAL_SPI_CS_LEVEL("SERIAL_SPI_CS_LEVEL"), // SPI片选线有效电平变更

    /** SPI数据位变更 */ // SPI数据位
    SERIAL_SPI_BIT("SERIAL_SPI_BIT"), // SPI数据位宽度变更

    // --- I2C子参数 ---

    /** I2C数据源变更 */ // I2C数据源
    SERIAL_I2C_DATA_SOURCE("SERIAL_I2C_DATA_SOURCE"), // I2C数据线（SDA）源通道变更

    /** I2C时钟源变更 */ // I2C时钟源
    SERIAL_I2C_CLOCK_SOURCE("SERIAL_I2C_CLOCK_SOURCE"), // I2C时钟线（SCL）源通道变更

    // --- A429子参数 ---

    /** A429源通道变更 */ // A429源通道
    SERIAL_A429_SOURCE("SERIAL_A429_SOURCE"), // ARINC429解码的源通道变更

    /** A429格式变更 */ // A429格式
    SERIAL_A429_FORMAT("SERIAL_A429_FORMAT"), // ARINC429数据格式变更

    /** A429显示格式变更 */ // A429显示格式
    SERIAL_A429_DISPLAY_FORMAT("SERIAL_A429_DISPLAY_FORMAT"), // ARINC429解码数据显示格式变更

    /** A429波特率变更 */ // A429波特率
    SERIAL_A429_BAUD_RATE("SERIAL_A429_BAUD_RATE"), // ARINC429波特率变更

    // --- M1553B子参数 ---

    /** M1553B源通道变更 */ // M1553B源通道
    SERIAL_M1553B_SOURCE("SERIAL_M1553B_SOURCE"), // MIL-STD-1553B解码的源通道变更

    /** M1553B显示格式变更 */ // M1553B显示格式
    SERIAL_M1553B_DISPLAY_FORMAT("SERIAL_M1553B_DISPLAY_FORMAT"), // MIL-STD-1553B解码数据显示格式变更

    // ==================== 触发器（TRIGGER）相关 ====================

    /** 触发源变更 */ // 触发源变更
    TRIGGER_SRC_CHANGE("TRIGGER_SRC_CHANGE"), // 触发源通道变更

    /** 触发类型变更 */ // 触发类型变更
    TRIGGER_TYPE_CHANGE("TRIGGER_TYPE_CHANGE"), // 触发类型（边沿/脉宽/视频等）变更

    /** 触发电平变更 */ // 触发电平变更
    TRIGGER_LEVEL_CHANGE("TRIGGER_LEVEL_CHANGE"), // 触发电平值变更

    /** 触发移动完成 */ // 触发移动完成
    TRIGGER_MOVE_COMPLETE("TRIGGER_MOVE_COMPLETE"), // 触发电平移动操作完成

    // ==================== 阈值（THRESHOLD）相关 ====================

    /** 阈值电平变更 */ // 阈值电平变更
    THRESHOLD_LEVEL_CHANGE("THRESHOLD_LEVEL_CHANGE"), // 阈值电平值变更

    /** 阈值源变更 */ // 阈值源变更
    THRESHOLD_SRC_CHANGE("THRESHOLD_SRC_CHANGE"), // 阈值源通道变更

    // ==================== 通用 ====================

    /** 操作完成通用标识 */ // 操作完成
    COMPLETE("COMPLETE"); // 通用操作完成标识

    /** 枚举值的字符串表示 */ // 枚举值字符串字段
    private String value; // 枚举值对应的字符串标识

    /**
     * 枚举构造方法
     * @param value 枚举值的字符串标识
     */
    private  MQEnum(String value){ // 私有构造方法，枚举专用
        this.value=value; // 赋值字符串标识
    }
}
