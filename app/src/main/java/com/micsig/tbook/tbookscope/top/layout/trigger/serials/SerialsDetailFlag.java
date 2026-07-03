package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                   SerialsDetailFlag（串行详情标志常量接口）                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/SerialsDetailFlag.java                                  ║
 * ║ 核心职责: 定义串行触发协议类型和详情子类型的整型常量标志                          ║
 * ║ 架构设计: 纯常量接口，无方法，仅提供协议类型和子类型的枚举值                       ║
 * ║ 数据流向: 被TopLayoutTriggerSerials用于判断显示哪个详情Fragment              ║
 * ║ 依赖关系: 无外部依赖，被serials包内多个类引用                                  ║
 * ║ 使用场景: 串行触发模块中标识协议类型和详情子类型的标志位                          ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/4/27.
 */

public interface SerialsDetailFlag { // 串行详情标志常量接口
    int UART = 0; // UART协议类型标志
    int LIN = 1; // LIN协议类型标志
    int CAN = 2; // CAN协议类型标志
    int SPI = 3; // SPI协议类型标志
    int I2C = 4; // I2C协议类型标志
    int ARINC429 = 5; // ARINC429协议类型标志
    int M1553B = 6; // MIL-STD-1553B协议类型标志

    int NULL = -1; // 无详情页面的标志
    int UART_DATA = 0; // UART数据触发详情标志
    int UART_0DATA = 1; // UART 0数据触发详情标志
    int UART_1DATA = 2; // UART 1数据触发详情标志
    int UART_XDATA = 3; // UART X数据触发详情标志
    int LIN_FRAMEID = 4; // LIN帧ID触发详情标志
    int LIN_IDDATA = 5; // LIN ID+数据触发详情标志
    int CAN_REMOTEID = 6; // CAN远程帧ID触发详情标志
    int CAN_DATAID = 7; // CAN数据帧ID触发详情标志
    int CAN_RDID = 8; // CAN远程数据ID触发详情标志
    int CAN_IDDATA = 9; // CAN ID+数据触发详情标志
    int SPI_DATA = 10; // SPI数据触发详情标志
    int I2C_NOACKINADR = 11; // I2C地址无应答触发详情标志
    int I2C_FRAME1 = 12; // I2C帧1触发详情标志
    int I2C_FRAME2 = 13; // I2C帧2触发详情标志
    int I2C_ROMDATA = 14; // I2C ROM数据触发详情标志
    int I2C_10WRITEFRAME = 15; // I2C 10位写帧触发详情标志
    int ARINC429_LABEL = 16; // ARINC429标签触发详情标志
    int ARINC429_SDI = 17; // ARINC429 SDI触发详情标志
    int ARINC429_DATA = 18; // ARINC429数据触发详情标志
    int ARINC429_SSM = 19; // ARINC429 SSM触发详情标志
    int ARINC429_LABELSDI = 20; // ARINC429标签+SDI触发详情标志
    int ARINC429_LABELDATA = 21; // ARINC429标签+数据触发详情标志
    int ARINC429_LABELSSM = 22; // ARINC429标签+SSM触发详情标志
    int M1553B_CSWORD = 23; // MIL-STD-1553B指令/状态字触发详情标志
    int M1553B_RTADDR = 24; // MIL-STD-1553B远程终端地址触发详情标志
    int M1553B_DATAWORD = 25; // MIL-STD-1553B数据字同步头触发详情标志
    int LIN_PARITY_ERROR = 26; // LIN奇偶校验错误触发详情标志
    int LIN_CHECKSUM_ERROR = 27; // LIN校验和错误触发详情标志
}
