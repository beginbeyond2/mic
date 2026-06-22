package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包，包含协议解析、数据结构定义和缓存管理

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                    ICharacterEncoding                          │
 * ├──────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 字符编码常量定义接口                        │
 * │ 核心职责：定义串口总线解码中使用的字符编码类型常量                     │
 * │ 架构设计：作为编码类型的统一常量池，供SerialBusStruct、              │
 * │          SerialBusStructParse等类引用                             │
 * │ 数据流：  UI编码选择 → 此接口常量 → 解析器编码转换                   │
 * │ 依赖关系：被SerialBusStruct内部类SettingStruct引用                 │
 * │ 使用场景：UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B解码时       │
 * │          根据用户选择的编码方式显示数据                               │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 字符编码常量接口，定义串口总线解码中支持的5种数据编码方式。
 * 编码值与FPGA协议中的编码标识对应，用于将二进制数据转换为可读字符串。
 *
 * Created by liwb on 2017/10/26.
 */
public interface ICharacterEncoding {

    /** 十六进制编码，值0x16，最常用的编码方式，每字节显示为2位十六进制字符 */
    public static final int Hex=0x16; // 十六进制编码常量，如0xFF显示为"FF"

    /** 二进制编码，值0x02，每字节显示为8位二进制字符 */
    public static final int Binary=0x02; // 二进制编码常量，如0xA5显示为"10100101"

    /** 八进制编码，值0x08，每字节显示为3位八进制字符 */
    public static final int Octal =0x08; // 八进制编码常量，如0xFF显示为"377"

    /** 十进制编码，值0x10，每字节显示为十进制数值 */
    public static final int Decimal=0x10; // 十进制编码常量，如0xFF显示为"255"

    /** ASCII编码，值0xAC，将字节作为ASCII字符显示 */
    public static final int ASCII=0xAC; // ASCII编码常量，如0x41显示为"A"

}
