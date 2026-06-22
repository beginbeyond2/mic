package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 包声明：串口总线文本解码数据结构所属包路径

import com.micsig.base.Logger; // 导入日志工具类，用于调试信息输出
import com.micsig.tbook.scope.Bus.LinBus; // 导入LIN总线协议常量类，提供LIN协议版本类型定义
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage; // 导入串口总线管理类，管理多通道串口总线解码
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类，提供时间格式转换等通用方法
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类，定义通道编号常量与通道名称映射

/**
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │                        SerialBusTxtStruct 类说明文档                                 │
 * ├─────────────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                         │
 * │   串口总线文本解码数据结构定义类 - MHO系列示波器串口总线协议解码系统的文本版数据容器     │
 * │   位于 wavedata 包中，与 SerialBusStruct（图形版数据结构）互为补充，                    │
 * │   SerialBusTxtStruct 专注于文本列表模式下的解码数据承载与CSV导出                        │
 * │                                                                                     │
 * │ 【核心职责】                                                                         │
 * │   1. 定义7种串口总线协议（UART/LIN/CAN/SPI/I2C/MIL-STD-1553B/ARINC429）的文本版       │
 * │      解码数据结构，每种协议对应一个内部类                                               │
 * │   2. 提供统一的ISerialBusTxtCSV接口，支持文本数据的CSV格式导出                         │
 * │   3. 定义FPGA与ARM之间的协议位定义常量，用于解析FPGA下发的码字数据                      │
 * │   4. 通过Holder单例模式确保全局唯一实例，避免重复创建                                   │
 * │                                                                                     │
 * │ 【架构设计】                                                                         │
 * │   采用"外部类+内部类"结构：                                                           │
 * │   - 外部类 SerialBusTxtStruct：单例入口，持有协议类型常量和位定义常量                    │
 * │   - ISerialBusTxtCSV 接口：定义CSV导出规范（toCSVHead/toCSV/getCh）                    │
 * │   - 7个协议内部类：各自实现 ISerialBusTxtCSV，承载特定协议的解码结果                     │
 * │   - 每个内部类均为非静态内部类，可访问外部类实例                                       │
 * │                                                                                     │
 * │ 【数据流】                                                                           │
 * │   FPGA原始码字 → SerialBusTxtStructParse（解析器）→ 填充各协议Struct内部类字段          │
 * │   → SerialTxtBuffer（文本缓冲区）→ UI列表展示 / CSV文件导出                            │
 * │                                                                                     │
 * │ 【依赖关系】                                                                         │
 * │   - LinBus：LIN总线协议版本类型常量（LIN_TYPE_1_3等）                                  │
 * │   - TChan：通道编号与名称映射（S1/S2/S3/S4 → "S1"/"S2"等）                            │
 * │   - TBookUtil：时间格式转换工具（10μs时间戳 → 可读时间字符串）                          │
 * │   - SerialBusTxtStructParse：本类的消费者，负责解析FPGA数据并填充Struct字段              │
 * │   - SerialTxtBuffer：本类的消费者，负责将Struct数据缓存并传递给UI层                     │
 * │   - SerialBusManage：总线管理器，协调解析器与UI的交互                                   │
 * │                                                                                     │
 * │ 【使用场景】                                                                         │
 * │   1. 串口总线文本列表模式：用户在示波器上选择串口解码后，文本列表页展示解码结果          │
 * │   2. CSV数据导出：用户将解码结果导出为CSV文件，用于离线分析                             │
 * │   3. 触发条件判断：通过Serial_5_okTriggerCondition等位标志判断帧是否满足触发条件        │
 * │                                                                                     │
 * │ 【与SerialBusStruct的区别】                                                           │
 * │   - SerialBusStruct：图形版数据结构，包含BeginX/EndX等屏幕坐标，用于波形绘制           │
 * │   - SerialBusTxtStruct：文本版数据结构，包含CurTime等时间戳，用于列表展示和CSV导出      │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialBusTxtStruct {

    //region 单例模式 - 使用静态内部类Holder实现线程安全的懒加载单例
    /**
     * 单例持有者类 - 利用JVM类加载机制保证线程安全的延迟初始化
     * 当SerialBusTxtStruct类被加载时，SerialBusTxtStructHolder不会被立即加载；
     * 只有调用getInstance()时才会触发Holder类的加载和instance的创建
     */
    public static class SerialBusTxtStructHolder {
        /** 单例实例，全局唯一，final确保不可被重新赋值 */ // 单例实例
        public static final SerialBusTxtStruct instance = new SerialBusTxtStruct(); // 创建唯一实例
    }

    /**
     * 获取SerialBusTxtStruct单例实例
     *
     * @return SerialBusTxtStruct 全局唯一实例
     * 业务意义：所有串口总线文本解码数据结构共享同一实例，避免重复创建带来的内存开销
     */
    public static SerialBusTxtStruct getInstance() { // 获取单例实例
        return SerialBusTxtStructHolder.instance; // 返回Holder中持有的唯一实例
    }
    //endregion

    //region CSV导出接口定义
    /**
     * 串口总线文本版CSV导出接口
     *
     * 业务意义：为7种总线协议的文本解码数据提供统一的CSV导出规范，
     * 使得不同协议的解码结果可以以统一的格式导出为CSV文件，便于离线分析。
     * 与SerialBusStruct中的ISerialBusCSV接口相比，本接口额外包含getCh()方法，
     * 因为文本版需要按通道维度进行CSV导出。
     *
     * 实现类：UartStruct、LinStruct、CanStruct、SpiStruct、I2cStruct、
     *         MilSTD1553bStruct、Arinc429Struct
     */
    public interface ISerialBusTxtCSV {
        // void frameEnd(); // 已废弃：帧结束回调方法，原用于帧结束时缓存CSV字符串
        // String getCsv(); // 已废弃：获取已缓存的CSV字符串，原用于避免重复生成CSV
        /**
         * 获取CSV文件的表头行
         *
         * @return CSV表头字符串，各字段以英文逗号分隔
         * 业务意义：CSV文件第一行，定义各列含义，不同协议的表头字段不同
         */
        String toCSVHead(); // 返回CSV表头

        /**
         * 将当前解码数据转换为CSV数据行
         *
         * @return CSV数据行字符串，各字段以英文逗号分隔，与toCSVHead()的列一一对应
         * 业务意义：将单帧解码结果格式化为CSV行，用于批量导出
         */
        String toCSV(); // 返回CSV数据行

        /**
         * 获取当前解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"、"S2"等
         * 业务意义：文本版CSV导出需要按通道维度组织数据，此方法提供通道标识
         * 与ISerialBusCSV接口的区别：ISerialBusCSV无此方法，因为图形版不需要按通道导出
         */
        String getCh(); // 返回通道名称
    }
    //endregion

    //region 总线协议类型常量定义
    /** UART协议类型编号，取值：1 */ // UART串口总线协议
    public static final int SerialBusType_UART = 1; // UART协议类型=1
    /** LIN协议类型编号，取值：2 */ // LIN本地互联网络协议
    public static final int SerialBusType_LIN = 2; // LIN协议类型=2
    /** CAN协议类型编号，取值：3 */ // CAN控制器局域网协议
    public static final int SerialBusType_CAN = 3; // CAN协议类型=3
    /** SPI协议类型编号，取值：4 */ // SPI串行外设接口协议
    public static final int SerialBusType_SPI = 4; // SPI协议类型=4
    /** I2C协议类型编号，取值：5 */ // I2C集成电路间总线协议
    public static final int SerialBusType_I2C = 5; // I2C协议类型=5
    /** ARINC429协议类型编号，取值：6 */ // ARINC429航空电子数字总线协议
    public static final int SerialBusType_429 = 6; // ARINC429协议类型=6
    /** MIL-STD-1553B协议类型编号，取值：7 */ // MIL-STD-1553B军用航空数据总线协议
    public static final int SerialBusType_1553B = 7; // MIL-STD-1553B协议类型=7
    //endregion

    //region FPGA协议位定义常量 - 用于解析FPGA下发的16bit码字
    /**
     * 第7位是0时，为数据类型（此常量未使用，仅作文档说明）
     * 当bit[7]=0时，低7位表示数据值
     */
    //public static final int Serial_7_DataType=0; // 已注释：bit[7]=0表示数据类型，值为0无需定义常量

    /**
     * 第7位时间戳标志位掩码，取值：0x80（二进制10000000）
     *
     * 当FPGA下发的16bit码字中bit[7]=1时，表示当前码字为时间戳类型，
     * 此时bit[6:0]与下一个码字组合共15bit表示时间值。
     * FPGA在以下两种情况下会给出时间戳：
     *   1. 帧起始时刻：标记一帧数据的开始时间
     *   2. 长时间无码字：当总线空闲较长时间后，插入时间戳标记时间间隔
     *
     * 业务意义：解码器通过此标志位区分"数据码字"和"时间戳码字"，
     * 从而正确还原总线通信的时间信息
     */
    public static final int Serial_7_TimeKey = 0x80; // bit[7]=1时为时间戳标志，掩码值0x80

    /**
     * 第6位帧结束标志位掩码，取值：0x40（二进制01000000）
     *
     * 当FPGA下发的16bit码字中bit[6]=1时，表示当前帧已结束，
     * 此时bit[5]有意义（见Serial_5_okTriggerCondition）。
     *
     * 业务意义：解码器通过此标志位判断一帧数据是否接收完毕，
     * 帧结束时需要将当前累积的解码结果提交到文本缓冲区
     */
    public static final int Serial_6_keyEnd = 0x40; // bit[6]=1时为帧结束标志，掩码值0x40

    /**
     * 本次总线通信不符合触发条件（此常量未使用，仅作文档说明）
     * 当bit[6]=1（帧结束）且bit[5]=0时，表示本帧不满足触发条件
     */
    //public static final int Serial_5_errorTriggerCondition=0; // 已注释：bit[5]=0表示不满足触发条件，值为0无需定义常量

    /**
     * 第5位触发条件匹配标志位掩码，取值：0x20（二进制00100000）
     *
     * 当FPGA下发的16bit码字中bit[6]=1（帧结束）且bit[5]=1时，
     * 表示本次总线通信符合用户设置的触发条件。
     *
     * 业务意义：示波器触发功能依赖此标志位判断是否应触发捕获，
     * 符合触发条件的帧会在UI上高亮显示，帮助用户快速定位感兴趣的通信事件
     */
    public static final int Serial_5_okTriggerCondition = 0x20; // bit[5]=1时满足触发条件，掩码值0x20

    //endregion

    //region 各协议CSV表头静态方法
    /**
     * 获取UART协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,Data,Color"
     * 业务意义：UART解码结果导出时，CSV文件包含通道、数据、颜色三列
     */
    public static String toCSVTitleUart() { // 返回UART的CSV表头
        return "Ch,Data,Color"; // 通道,数据(十六进制),颜色值
    }

    /**
     * 获取LIN协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurTime,Id,Data,Error,Check,Trigger"
     * 业务意义：LIN解码结果导出时，CSV文件包含通道、时间、ID、数据、错误、校验和、触发标志七列
     */
    public static String toCSVTitleLin() { // 返回LIN的CSV表头
        return "Ch,CurTime,Id,Data,Error,Check,Trigger"; // 通道,时间,ID,数据,错误,校验和,触发标志
    }

    /**
     * 获取CAN协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum"
     * 业务意义：CAN解码结果导出时，CSV文件包含通道、时间、ID、类型、DLC、数据、CRC、错误、触发、帧结束十列
     */
    public static String toCSVTitleCan() { // 返回CAN的CSV表头
        return "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum"; // 通道,时间,ID,类型枚举,DLC,数据,CRC,错误枚举,触发标志,帧结束枚举
    }

    /**
     * 获取SPI协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurrTime,Data,Trigger"
     * 业务意义：SPI解码结果导出时，CSV文件包含通道、时间、数据、触发标志四列
     */
    public static String toCSVTitleSpi() { // 返回SPI的CSV表头
        return "Ch,CurrTime,Data,Trigger"; // 通道,时间,数据,触发标志
    }

    /**
     * 获取I2C协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot"
     * 业务意义：I2C解码结果导出时，CSV文件包含通道、时间、地址、数据、应答、触发、重启七列
     */
    public static String toCSVTitleI2c() { // 返回I2C的CSV表头
        return "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot"; // 通道,时间,地址,数据,应答确认,触发标志,重启标志
    }

    /**
     * 获取MIL-STD-1553B协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurTime,Type,RAddr,Data,Trigger,Error"
     * 业务意义：1553B解码结果导出时，CSV文件包含通道、时间、类型、远程地址、数据、触发、错误七列
     */
    public static String toCSVTitleM1553b() { // 返回MIL-STD-1553B的CSV表头
        return "Ch,CurTime,Type,RAddr,Data,Trigger,Error"; // 通道,时间,类型,远程地址,数据,触发标志,错误
    }

    /**
     * 获取ARINC429协议的CSV表头
     *
     * @return CSV表头字符串 "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger"
     * 业务意义：ARINC429解码结果导出时，CSV文件包含通道、时间、标签、SDI、数据、SSM、错误、触发八列
     * 注意：方法名为toCSVTitleArinc492，实际对应ARINC429协议，方法名中的492为历史命名遗留
     */
    public static String toCSVTitleArinc492() { // 返回ARINC429的CSV表头（方法名492为历史遗留）
        return "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger"; // 通道,时间,标签,SDI源目标标识,数据,SSM符号状态矩阵,错误,触发标志
    }
    //endregion

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                        UartStruct 内部类说明文档                                 │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   UART串口总线文本解码数据结构 - 承载UART协议单帧解码的文本结果                     │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储UART单帧解码结果：通道、颜色、数据值、帧结束标志                          │
     * │   2. 定义UART校验与停止位组合的4种状态常量                                        │
     * │   3. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → 填充UartStruct字段                     │
     * │   → SerialTxtBuffer缓存 → UI列表展示 / CSV导出                                   │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   UART协议解码时，每接收一个字节即产生一个UartStruct实例，                         │
     * │   包含该字节的校验结果和停止位状态                                                 │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class UartStruct implements ISerialBusTxtCSV { // UART文本结构，实现CSV导出接口

        /**
         * 校验正确且无停止位，取值：0x02（二进制010）
         *
         * FPGA码字低3位为"010"时表示：校验位正确，但缺少停止位
         * 业务含义：数据校验通过但帧格式不完整，属于异常帧
         */
        public static final int CHECK_OK_NoStop = 0x02; // 校验正确+无停止位，低3位=010

        /**
         * 校验错误且无停止位，取值：0x03（二进制011）
         *
         * FPGA码字低3位为"011"时表示：校验位错误，且缺少停止位
         * 业务含义：数据校验失败且帧格式不完整，属于严重异常帧
         */
        public static final int CHECK_Error_NoStop = 0x03; // 校验错误+无停止位，低3位=011

        /**
         * 校验正确且有停止位，取值：0x06（二进制110）
         *
         * FPGA码字低3位为"110"时表示：校验位正确，且有停止位
         * 业务含义：正常帧，数据校验通过且帧格式完整
         */
        public static final int CHECK_OK_Stop = 0x06; // 校验正确+有停止位，低3位=110

        /**
         * 校验错误但有停止位，取值：0x07（二进制111）
         *
         * FPGA码字低3位为"111"时表示：校验位错误，但有停止位
         * 业务含义：帧格式完整但数据校验失败，数据内容不可信
         */
        public static final int CHECK_Error_Stop = 0x07; // 校验错误+有停止位，低3位=111


        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值：TChan.getChannelName(TChan.S1) = "S1"
         * 业务含义：标识当前UART解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 数据颜色值，取值范围：0x000000 ~ 0xFFFFFF（ARGB格式的RGB部分）
         * 默认值：0（未设置）
         * 业务含义：用于UI列表中该行数据的文本颜色，根据校验/停止位状态分配不同颜色
         *   - 校验正确+有停止位：绿色（正常）
         *   - 校验错误：红色（异常）
         *   - 无停止位：黄色（警告）
         */
        public int Color; // 数据显示颜色，RGB格式

        /**
         * UART数据值，取值范围：0x00 ~ 0xFF（单字节数据）
         * 默认值：0
         * 业务含义：UART通信中传输的一个字节数据，以十六进制形式展示
         */
        public int Data; // UART单字节数据值

        /**
         * CSV缓存字符串，取值范围：null或CSV格式字符串
         * 默认值：null
         * 业务含义：原设计用于帧结束时缓存CSV字符串避免重复生成，当前未使用
         */
        public String csv = null; // CSV缓存，当前未使用，保留字段

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前UartStruct是否为一帧的最后一个数据，
         * 当FPGA下发帧结束标志位时置为true
         */
        private boolean FlagFrameEnd=false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }
        //endregion

        /**
         * 无参构造函数
         *
         * 业务意义：创建UartStruct实例，字段保持默认值，
         * 由解析器后续逐字段填充
         */
        public UartStruct() { // 无参构造，字段保持默认值
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的UartStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public UartStruct clean() { // 重置所有字段为默认值
            Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            Color = 0xFFFFFF; // 重置颜色为白色
            Data = 0; // 重置数据值为0
            FlagFrameEnd=false; // 重置帧结束标志为false
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前UartStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、数据（十六进制）、颜色（十六进制）
         * 业务意义：调试时快速查看UART解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch" + Ch + // 通道名称
                    "Data:" + Integer.toHexString(Data) + // 数据值（十六进制）
                    "Color:0x" + Integer.toHexString(Color); // 颜色值（十六进制）
        }


        // @Override // 已废弃：帧结束回调
        // public void frameEnd(){ // 帧结束时触发
        //       csv= toCSV(); // 缓存CSV字符串
        // }
        // @Override // 已废弃：获取缓存CSV
        // public String getCsv(){return csv;} // 返回缓存的CSV字符串

        /**
         * 获取UART协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,Data,Color"
         * 业务意义：与静态方法toCSVTitleUart()返回值一致，
         * 通过接口统一调用，便于多态处理
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,Data,Color"; // 通道,数据,颜色
        }

        /**
         * 将当前UART解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,数据(十六进制),0x颜色(十六进制)
         * 业务意义：将单帧UART解码结果格式化为CSV行，用于批量导出
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + Integer.toHexString(Data) + "," + "0x" + Integer.toHexString(Color); // 通道,数据hex,0x颜色hex
        }

        /**
         * 获取当前UART解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                        LinStruct 内部类说明文档                                  │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   LIN总线文本解码数据结构 - 承载LIN协议单帧解码的文本结果                           │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储LIN单帧解码结果：通道、时间、ID、数据、错误、校验和、触发标志             │
     * │   2. 定义LIN协议FPGA码字低4位的状态常量（ID校验/数据/校验和）                      │
     * │   3. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   4. 支持数据拼接：通过sbData和appendData()支持多字节逐步拼接                     │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → appendData()逐步拼接数据               │
     * │   → updateData()提交拼接结果 → SerialTxtBuffer缓存 → UI/CSV                     │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   LIN协议解码时，一帧包含ID+数据+校验和，                                         │
     * │   解析器逐码字填充各字段，帧结束时提交完整结果                                     │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class LinStruct implements ISerialBusTxtCSV { // LIN文本结构，实现CSV导出接口

        /**
         * [3:0]="0001" 奇偶校验正确的ID，取值：0x01
         *
         * FPGA码字低4位为0001时表示：当前码字为ID字段，且ID的奇偶校验正确
         * bit[3]=0表示校验通过
         */
        public static final int Lin_30_CheckYes = 0x01; // ID奇偶校验正确，低4位=0001

        /**
         * [3:0]="1001" 奇偶校验错误的ID，取值：0x09
         *
         * FPGA码字低4位为1001时表示：当前码字为ID字段，但ID的奇偶校验错误
         * bit[3]=1表示校验失败
         */
        public static final int Lin_30_CheckNo = 0x09; // ID奇偶校验错误，低4位=1001

        /**
         * [3:0]="0010" 数据且有停止位，取值：0x02
         *
         * FPGA码字低4位为0010时表示：当前码字为数据字段，且数据后有停止位
         * bit[3]=0表示有停止位
         */
        public static final int Lin_30_DataYes = 0x02; // 数据+有停止位，低4位=0010

        /**
         * [3:0]="1010" 数据且无停止位，取值：0x0A
         *
         * FPGA码字低4位为1010时表示：当前码字为数据字段，但数据后无停止位
         * bit[3]=1表示无停止位
         */
        public static final int Lin_30_DataNo = 0x0A; // 数据+无停止位，低4位=1010

        /**
         * [3:0]="0011" 正确校验和，取值：0x03
         *
         * FPGA码字低4位为0011时表示：当前码字为校验和字段，且校验和正确
         * bit[3]=0表示校验和正确
         */
        public static final int Lin_30_CheckSumYes = 0x03; // 校验和正确，低4位=0011

        /**
         * [3:0]="1011" 错误校验和，取值：0x0B
         *
         * FPGA码字低4位为1011时表示：当前码字为校验和字段，但校验和错误
         * bit[3]=1表示校验和错误
         */
        public static final int Lin_30_CheckSumNo = 0x0B; // 校验和错误，低4位=1011

        /**
         * LIN协议版本类型，取值范围：LinBus.LIN_TYPE_1_3 / 其他版本常量
         * 默认值：LinBus.LIN_TYPE_1_3（LIN 1.3版本）
         * 业务含义：不同LIN版本的校验和计算方式不同，
         *   LIN 1.3：校验和仅覆盖数据字段
         *   LIN 2.x：校验和覆盖ID+数据字段
         * 此字段影响toCSV()中是否裁剪末尾校验位数据
         */
        public int linType = LinBus.LIN_TYPE_1_3; // LIN协议版本，默认1.3

        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前LIN解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs，
         * 通过TBookUtil.getStringFrom10us()转换为可读时间字符串
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * LIN帧ID，取值范围：0x00 ~ 0x3F（6位标识符）
         * 默认值：0
         * 业务含义：LIN帧的标识符，用于区分不同的信号，
         * 在CSV中以十六进制形式输出
         */
        public int Id = 0; // LIN帧ID，6位

        /**
         * LIN帧数据，取值范围：十六进制字符串，如"AA BB CC"
         * 默认值：空字符串""
         * 业务含义：LIN帧携带的有效数据，多个字节以空格分隔的十六进制形式展示
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * 错误信息，取值范围：错误描述字符串或空字符串
         * 默认值：空字符串""
         * 业务含义：记录LIN帧解码过程中检测到的错误信息，
         * 如"校验和错误"、"ID奇偶校验错误"等
         */
        public String Error = ""; // 错误信息描述

        /**
         * 校验和值，取值范围：0x00 ~ 0xFF
         * 默认值：0
         * 业务含义：LIN帧的校验和值，在CSV中以十六进制形式输出
         */
        public int Check = 0; // 校验和值

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件，
         * 满足触发条件的帧在UI上高亮显示
         */
        public boolean Trigger = false; // 触发条件匹配标志

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前LinStruct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd = false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }

        //endregion

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：LIN帧数据可能由多个FPGA码字逐步给出，
         * 每个码字通过appendData()追加到sbData，帧结束时通过updateData()提交到Data字段
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前LinStruct实例（this），支持链式调用
         * 业务意义：LIN帧数据由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public LinStruct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段，
         * 完成多字节数据的拼接
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            this.Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的LinStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public LinStruct clean() { // 重置所有字段为默认值
            Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            CurTime = 0; // 重置时间戳为0
            Id = 0; // 重置ID为0
            Data = ""; // 重置数据为空字符串
            Error = ""; // 重置错误信息为空字符串
            Check = 0; // 重置校验和为0
            Trigger = false; // 重置触发标志为false
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            FlagFrameEnd=false; // 重置帧结束标志为false
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前LinStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、ID、数据、错误、校验和、触发标志
         * 业务意义：调试时快速查看LIN解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch:" + Ch + // 通道名称
                    " CurTime:" + CurTime + // 时间戳
                    " Id:" + Id + // 帧ID
                    " Data:" + Data + // 帧数据
                    " Error:" + Error + // 错误信息
                    " Check:" + Check + // 校验和
                    " Trigger:" + Trigger // 触发标志
                    ;
        }

        /**
         * 获取LIN协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurTime,Id,Data,Error,Check,Trigger"
         * 业务意义：与静态方法toCSVTitleLin()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurTime,Id,Data,Error,Check,Trigger"; // 通道,时间,ID,数据,错误,校验和,触发标志
        }

        /**
         * 将当前LIN解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,ID(十六进制),数据,错误,校验和(十六进制),触发标志
         * 业务意义：将单帧LIN解码结果格式化为CSV行，用于批量导出。
         * 注意：非LIN1.3版本时，FPGA传过来的data会在末尾多出校验位数据，此处做裁剪处理
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            if (linType != LinBus.LIN_TYPE_1_3) { // FIXME 非LIN1.3时FPGA传过来的data会在末尾多出来校验位数据，这里去掉
                if (Data.length() > 2) { // 数据长度大于2时才裁剪，避免空数据或单字节数据被误裁剪
                    Data = Data.substring(0, Data.length() - 3); // 裁剪末尾3个字符（1个校验位数据+空格分隔符）
                }
            }
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Integer.toHexString(Id) + "," + Data + "," + Error + "," + Integer.toHexString(Check) + "," + Trigger; // 通道,时间字符串,IDhex,数据,错误,校验和hex,触发
        }

        /**
         * 获取当前LIN解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                        CanStruct 内部类说明文档                                  │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   CAN总线文本解码数据结构 - 承载CAN协议单帧解码的文本结果                           │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储CAN单帧解码结果：通道、时间、ID、类型、DLC、数据、CRC、错误、触发、帧结束  │
     * │   2. 定义CAN协议FPGA码字低4位的状态常量（标准ID/扩展ID/DLC/DATA/CRC/错误/过载）    │
     * │   3. 定义CAN帧类型常量（标准帧/扩展帧/标准远程帧/扩展远程帧）                      │
     * │   4. 定义CAN错误类型常量（位填充/格式/ACK/CRC错误）                               │
     * │   5. 定义CAN帧结束类型常量（有确认/无确认/数据帧/远程帧）                          │
     * │   6. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   7. 支持数据拼接和错误类型/帧类型/ID格式化输出                                   │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → 逐步填充各字段                         │
     * │   → appendData()拼接数据 → SerialTxtBuffer缓存 → UI/CSV                          │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   CAN协议解码时，一帧包含ID+DLC+数据+CRC+ACK等字段，                              │
     * │   解析器逐码字填充各字段，帧结束时提交完整结果                                     │
     * │   支持标准帧（11位ID）和扩展帧（29位ID）                                          │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class CanStruct implements ISerialBusTxtCSV { // CAN文本结构，实现CSV导出接口

        //region CAN帧类型枚举常量
        /**
         * 标准数据帧（Standard Frame Format），取值：0x00
         * 业务含义：CAN 2.0A标准帧，ID为11位
         */
        public static final int Type_STDID = 0x00; // 标准数据帧

        /**
         * 扩展数据帧（Extended Frame Format），取值：0x01
         * 业务含义：CAN 2.0B扩展帧，ID为29位
         */
        public static final int Type_EXTID = 0x01; // 扩展数据帧

        /**
         * 标准远程帧（Standard Remote Frame），取值：0x02
         * 业务含义：标准ID远程请求帧，无数据段，仅请求指定ID的数据
         */
        public static final int Type_STDREMOTEID=0x02; // 标准远程帧

        /**
         * 扩展远程帧（Extended Remote Frame），取值：0x03
         * 业务含义：扩展ID远程请求帧，无数据段，仅请求指定ID的数据
         */
        public static final int Type_EXTREMOTEID=0x03; // 扩展远程帧
        //endregion

        //region CAN FPGA码字低4位状态常量
        /**
         * [3:0]="0001" 标准ID，取值：0x01
         *
         * FPGA码字低4位为0001时表示：当前码字为标准ID字段（11位）
         */
        public static final int Can_30_stdId = 0x01; // 标准ID，低4位=0001

        /**
         * [3:0]="0110" 扩展ID，取值：0x06
         *
         * FPGA码字低4位为0110时表示：当前码字为扩展ID字段（29位）
         * 扩展ID可能由多个码字拼接而成
         */
        public static final int Can_30_extId = 0x06; // 扩展ID，低4位=0110

        /**
         * [3:0]="0010" DLC（数据长度码），取值：0x02
         *
         * FPGA码字低4位为0010时表示：当前码字为DLC字段，
         * DLC指示数据段的字节数（0~8）
         */
        public static final int Can_30_DLC = 0x02; // DLC数据长度码，低4位=0010

        /**
         * [3:0]="0011" DATA（数据段），取值：0x03
         *
         * FPGA码字低4位为0011时表示：当前码字为数据段，
         * 数据段长度由DLC指定，最多8字节
         */
        public static final int Can_30_DATA = 0x03; // DATA数据段，低4位=0011

        /**
         * [3:0]="0100" CRC（循环冗余校验），取值：0x04
         *
         * FPGA码字低4位为0100时表示：当前码字为CRC字段，
         * CAN协议使用15位CRC加1位定界符
         */
        public static final int Can_30_CRC = 0x04; // CRC校验字段，低4位=0100

        /**
         * [3:0]="0101" 错误帧，取值：0x05
         *
         * FPGA码字低4位为0101时表示：当前码字为错误类型，
         * 具体错误类型由数据段进一步区分（见Can_Error_1~4）
         */
        public static final int Can_30_Error = 0x05; // 错误帧，低4位=0101

        /**
         * [3:0]="0111" 过载帧，取值：0x07
         *
         * FPGA码字低4位为0111时表示：当前码字为过载帧标志，
         * 过载帧用于请求延迟下一数据帧或远程帧的发送
         */
        public static final int Can_30_Overload = 0x07; // 过载帧，低4位=0111

        // 以下为已注释的帧结束相关常量，原设计用于区分帧结束时的确认状态和帧类型
        // 实际帧结束通过Can_FrameEnd_*系列常量处理
//          /**  [3:0]：   "1XX1"：帧结束，有确认；  */
//          public static final int Can_30_FrameEnd_Confirm=0x08 | 0x01; // 帧结束+有确认
//          /**  [3:0]：   "1XX0"：帧结束，无确认；（当无确认时，会在之前先给出"错误"）  */
//          public static final int Can_30_FrameEnd_NoConfirm=0x08; // 帧结束+无确认
//          /**  [3:0]：   "1X0X"：帧结束，数据帧；  */
//          public static final int Can_30_ // 帧结束+数据帧（未完成定义）
//          /**  [3:0]：   "1X1X"：帧结束，远程帧；  */
//          public static final int Can_30_ // 帧结束+远程帧（未完成定义）
//          /**  [3:0]：   "1XXX"：帧结束  */
//          public static final int Can_30_FrameEnd= // 帧结束通用标志（未完成定义）


        //region CAN错误类型常量
        /**
         * 位填充错误（Bit Stuffing Error），取值：0x01
         *
         * 当[3:0]="0101"（错误）时，数据段为0x01表示位填充错误
         * 业务含义：CAN协议要求连续5个相同位后必须插入填充位，
         * 违反此规则即产生位填充错误
         */
        public static final int Can_Error_1 = 0x01; // 位填充错误

        /**
         * 格式错误（Format Error），取值：0x02
         *
         * 当[3:0]="0101"（错误）时，数据段为0x02表示格式错误
         * 业务含义：CAN帧的固定格式位（如CRC定界符、ACK定界符等）出错
         */
        public static final int Can_Error_2 = 0x02; // 格式错误

        /**
         * ACK错误（Acknowledgment Error），取值：0x03
         *
         * 当[3:0]="0101"（错误）时，数据段为0x03表示ACK错误
         * 业务含义：发送节点在ACK段未收到任何节点的应答信号
         */
        public static final int Can_Error_3 = 0x03; // ACK错误

        /**
         * CRC错误（CRC Error），取值：0x04
         *
         * 当[3:0]="0101"（错误）时，数据段为0x04表示CRC错误
         * 业务含义：接收节点计算的CRC与发送节点的CRC不匹配
         */
        public static final int Can_Error_4 = 0x04; // CRC错误

        /**
         * 错误类型掩码，取值：0x0F
         *
         * 用于从FPGA码字中提取低4位错误类型字段
         */
        public static final int Can_Error_MASK = 0x0F; // 低4位掩码
        //endregion

        //region CAN帧结束类型常量
        /**
         * 帧结束-有确认（ACK），取值：0
         * 业务含义：帧结束时收到了其他节点的应答确认，通信正常
         */
        public static final int Can_FrameEnd_Confirm = 0; // 帧结束+有确认

        /**
         * 帧结束-无确认（No ACK），取值：1
         * 业务含义：帧结束时未收到任何节点的应答确认，可能总线无其他节点
         */
        public static final int Can_FrameEnd_NoConfirm = 1; // 帧结束+无确认

        /**
         * 帧结束-数据帧，取值：2
         * 业务含义：帧结束时确认为数据帧（携带数据段）
         */
        public static final int Can_FrameEnd_Data = 2; // 帧结束+数据帧

        /**
         * 帧结束-远程帧，取值：3
         * 业务含义：帧结束时确认为远程帧（无数据段，仅请求）
         */
        public static final int Can_FrameEnd_Remote = 3; // 帧结束+远程帧
        //endregion


        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前CAN解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * CAN帧ID，取值范围：
         *   标准帧：0x000 ~ 0x7FF（11位）
         *   扩展帧：0x00000000 ~ 0x1FFFFFFF（29位）
         * 默认值：0
         * 业务含义：CAN帧的标识符，用于区分不同的消息
         */
        public int ID = 0; // CAN帧ID

        /**
         * 帧类型枚举，取值范围：Type_STDID(0)/Type_EXTID(1)/Type_STDREMOTEID(2)/Type_EXTREMOTEID(3)
         * 默认值：0（Type_STDID）
         * 业务含义：标识当前帧的类型（标准/扩展/数据/远程），
         * 影响ID的显示格式和CSV输出中的类型字段
         */
        public int TypeEnum = 0; // 帧类型枚举，默认标准数据帧

        /**
         * DLC（Data Length Code），取值范围：0 ~ 8
         * 默认值：0
         * 业务含义：CAN帧数据段的字节长度，
         * 远程帧的DLC表示请求的数据长度
         */
        public int DLC = 0; // 数据长度码

        /**
         * CAN帧数据段，取值范围：十六进制字符串，如"AA BB CC DD"
         * 默认值：空字符串""
         * 业务含义：CAN帧携带的有效数据，多个字节以空格分隔的十六进制形式展示
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * CRC校验值，取值范围：0x0000 ~ 0x7FFF（15位CRC）
         * 默认值：0
         * 业务含义：CAN帧的CRC校验值，在CSV中以十六进制形式输出
         */
        public int CRC = 0; // CRC校验值

        /**
         * 错误类型枚举，取值范围：Can_Error_1(1)/Can_Error_2(2)/Can_Error_3(3)/Can_Error_4(4)/0(无错误)
         * 默认值：0（无错误）
         * 业务含义：CAN帧的错误类型，0表示无错误，
         * 非零值对应位填充/格式/ACK/CRC错误
         */
        public int ErrorEnum = 0; // 错误类型枚举，默认无错误

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件
         */
        public boolean Trigger = false; // 触发条件匹配标志

        /**
         * 帧结束类型枚举，取值范围：Can_FrameEnd_Confirm(0)/Can_FrameEnd_NoConfirm(1)/Can_FrameEnd_Data(2)/Can_FrameEnd_Remote(3)
         * 默认值：0（有确认）
         * 业务含义：帧结束时的确认状态和帧类型
         */
        public int FrameEndEnum = 0; // 帧结束类型枚举，默认有确认

        /**
         * CRC宽度，取值范围：11 / 16 / 17 / 21
         * 默认值：16
         * 业务含义：CAN FD协议支持不同宽度的CRC，
         * CAN 2.0固定为15位CRC，此字段预留用于CAN FD扩展
         */
        public int crc_w = 16; // CRC宽度，默认16位

        /**
         * 标准ID解析次数计数器，取值范围：0 ~ Integer.MAX_VALUE
         * 默认值：0
         * 业务含义：记录标准ID字段被解析的次数，
         * 用于解决多帧拼接问题：当连续出现多个标准ID码字时，
         * 需要根据stdIdtimes判断是否为同一帧的ID重传还是新帧的ID
         */
        public int stdIdtimes=0; // 标准ID解析次数计数器

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前CanStruct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd = false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }
        //endregion

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：CAN帧数据段可能由多个FPGA码字逐步给出，
         * 每个码字通过appendData()追加到sbData，帧结束时通过updateData()提交到Data字段
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前CanStruct实例（this），支持链式调用
         * 业务意义：CAN帧数据段由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public CanStruct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            this.Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的CanStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public CanStruct clean() { // 重置所有字段为默认值
            Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            CurTime = 0; // 重置时间戳为0
            ID = 0; // 重置帧ID为0
            TypeEnum = 0; // 重置帧类型枚举为标准数据帧
            DLC = 0; // 重置DLC为0
            Data = ""; // 重置数据为空字符串
            CRC = 0; // 重置CRC为0
            ErrorEnum = 0; // 重置错误类型枚举为无错误
            Trigger = false; // 重置触发标志为false
            FrameEndEnum = 0; // 重置帧结束类型枚举为有确认
            FlagFrameEnd=false; // 重置帧结束标志为false
            stdIdtimes=0; // 重置标准ID解析次数为0
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前CanStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、ID、类型、DLC、数据、CRC、错误、触发、帧结束
         * 业务意义：调试时快速查看CAN解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch:" + Ch + // 通道名称
                    " CurTime:" + CurTime + // 时间戳
                    " ID:" + ID + // 帧ID
                    " TypeEnum:" + TypeEnum + // 帧类型枚举
                    " DLC:" + DLC + // 数据长度码
                    " Data:" + Data + // 帧数据
                    " CRC:" + CRC + // CRC校验值
                    " ErrorEnum:" + ErrorEnum + // 错误类型枚举
                    " Trigger:" + Trigger + // 触发标志
                    " FrameEndEnum:" + FrameEndEnum // 帧结束类型枚举
                    ;
        }

        /**
         * 获取CAN协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum"
         * 业务意义：与静态方法toCSVTitleCan()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum"; // 通道,时间,ID,类型,DLC,数据,CRC,错误,触发,帧结束
        }

        /**
         * 将当前CAN解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,ID(格式化),类型缩写,DLC,数据,CRC(十六进制),错误缩写,触发标志,帧结束类型
         * 业务意义：将单帧CAN解码结果格式化为CSV行，用于批量导出。
         * ID和类型、错误字段经过格式化处理，提高可读性
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + // 通道名
                    TBookUtil.getStringFrom10us(CurTime) + "," + // 时间字符串（10μs转可读格式）
                    getId() + "," + // ID（格式化输出，标准帧3位hex，扩展帧8位hex）
                    getTypeEnum() + "," + // 帧类型缩写（SFF/SRF/EFF/ERF）
                    DLC + "," + // 数据长度码
                    Data + "," + // 帧数据
                    Integer.toHexString(CRC) + "," + // CRC校验值（十六进制）
                    getErrorEnum() + "," + // 错误类型缩写（Bit/Fmt/Ack/CRC）
                    Trigger + "," + // 触发标志
                    FrameEndEnum; // 帧结束类型枚举值
        }

        /**
         * 获取当前CAN解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }

        /**
         * 获取错误类型的可读缩写字符串
         *
         * @return 错误类型缩写："Bit"(位填充错误) / "Fmt"(格式错误) / "Ack"(ACK错误) / "CRC"(CRC错误) / ""(无错误)
         * 业务意义：将数字枚举值转换为人类可读的错误类型缩写，
         * 用于CSV导出和UI显示
         */
        public String getErrorEnum(){ // 获取错误类型缩写
            switch (ErrorEnum){ // 根据错误类型枚举值匹配
                case Can_Error_1:return "Bit"; // 位填充错误 → "Bit"
                case Can_Error_2:return "Fmt"; // 格式错误 → "Fmt"
                case Can_Error_3:return "Ack"; // ACK错误 → "Ack"
                case Can_Error_4:return "CRC"; // CRC错误 → "CRC"
            }
            return ""; // 无错误返回空字符串
        }

        /**
         * 获取帧类型的可读缩写字符串
         *
         * @return 帧类型缩写："SFF"(标准数据帧) / "SRF"(标准远程帧) / "EFF"(扩展数据帧) / "ERF"(扩展远程帧) / ""(未知)
         * 业务意义：将数字枚举值转换为CAN协议标准的帧类型缩写，
         * SFF=Standard Frame Format, EFF=Extended Frame Format,
         * SRF=Standard Remote Frame, ERF=Extended Remote Frame
         */
        public String getTypeEnum(){ // 获取帧类型缩写
            switch (this.TypeEnum){ // 根据帧类型枚举值匹配
                case Type_STDID:return "SFF"; // 标准数据帧 → "SFF"
                case Type_STDREMOTEID:return "SRF"; // 标准远程帧 → "SRF"
                case Type_EXTID:return "EFF"; // 扩展数据帧 → "EFF"
                case Type_EXTREMOTEID:return "ERF"; // 扩展远程帧 → "ERF"
                default:return ""; // 未知类型返回空字符串
            }
        }

        /**
         * 获取格式化的ID字符串
         *
         * @return 格式化ID字符串：
         *   标准帧/标准远程帧：3位十六进制大写，如"1A3"
         *   扩展帧/扩展远程帧：8位十六进制大写，如"00001A3F"
         * 业务意义：不同帧类型的ID位数不同，需要格式化为固定位数便于对齐显示。
         * 标准帧11位→3个hex字符，扩展帧29位→8个hex字符
         */
        public String getId(){ // 获取格式化的ID字符串
            String id=""; // 初始化ID字符串
            switch (this.TypeEnum){ // 根据帧类型枚举值匹配
                case Type_STDID: // 标准数据帧
                case Type_STDREMOTEID: // 标准远程帧
                    //3个字符，11位 // 标准帧ID为11位，格式化为3个十六进制字符
                    id= String.format("%03x",ID).toUpperCase(); // 格式化为3位hex并转大写
                    break; // 跳出switch
                default: // 扩展帧
                    //8个字符，29位 // 扩展帧ID为29位，格式化为8个十六进制字符
                    id= String.format("%08x",ID).toUpperCase(); // 格式化为8位hex并转大写
                    break; // 跳出switch
            }
            return id; // 返回格式化后的ID字符串
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                        SpiStruct 内部类说明文档                                  │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   SPI总线文本解码数据结构 - 承载SPI协议单帧解码的文本结果                           │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储SPI单帧解码结果：通道、时间、数据、触发标志                              │
     * │   2. 定义SPI协议FPGA码字的状态常量（触发条件/数据/多字节拼接结束）                 │
     * │   3. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   4. 支持组数据概念：多字节SPI传输可组成一组数据                                   │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → appendData()逐步拼接数据               │
     * │   → updateData()提交拼接结果 → SerialTxtBuffer缓存 → UI/CSV                     │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   SPI协议解码时，数据以字节为单位逐步给出，                                         │
     * │   多个字节可组成一组数据（由FlagGroupDataEnd标记组结束）                            │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class SpiStruct implements ISerialBusTxtCSV { // SPI文本结构，实现CSV导出接口

        /**
         * 组数据显示分隔符，取值："--"
         * 业务含义：当一组SPI数据尚未完成时，用"--"作为占位符显示在数据列中，
         * 表示后续还有更多数据字节待接收
         */
        public static final String FLAGShow_GroupData="--"; // 组数据未完成时的占位显示符

        /**
         * [2]="1" 触发条件匹配标志，取值：0x04（二进制100）
         *
         * FPGA码字bit[2]=1时表示：当前数据符合用户设置的触发条件
         * 业务含义：用于判断当前SPI数据是否应触发示波器捕获
         */
        public static final int SPI_2_OkTriggerCondition = 0x04; // 触发条件匹配，bit[2]=1

        /**
         * [1:0]="01" 总线数据，取值：0x01（二进制01）
         *
         * FPGA码字低2位为01时表示：当前码字为SPI总线数据字节
         * 业务含义：普通数据字节，后续可能还有更多数据
         */
        public static final int SPI_10_Data = 0x01; // 总线数据，低2位=01

        /**
         * [1:0]="11" 多字节拼接的最后一个数据，取值：0x03（二进制11）
         *
         * FPGA码字低2位为11时表示：当前码字为SPI总线数据字节，
         * 且是多字节拼接的最后一个字节
         * 业务含义：标志一组SPI数据传输完成，可提交显示
         */
        public static final int SPI_10_MULDataEnd = 0x03; // 多字节拼接结束，低2位=11

        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前SPI解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * SPI帧数据，取值范围：十六进制字符串，如"AA BB CC"
         * 默认值：空字符串""
         * 业务含义：SPI传输的数据字节，多个字节以空格分隔的十六进制形式展示
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件
         */
        public boolean Trigger = false; // 触发条件匹配标志

        //region 帧结束与组数据结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前SpiStruct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd = false; // 帧结束标志，默认false

        /**
         * 一组数据是否解析完成标志，取值范围：true（组数据已结束）/ false（组数据未结束）
         * 默认值：false
         * 业务含义：SPI协议支持多字节组成一组数据传输，
         * 当一组数据的最后一个字节解析完成时置为true，
         * UI层根据此标志判断是否将"--"占位符替换为实际数据
         */
        private boolean FlagGroupDataEnd=false; // 组数据结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }

        /**
         * 获取组数据结束标志
         *
         * @return true表示一组数据已解析完成，false表示组数据未完成
         * 业务意义：UI层根据此标志判断组数据是否完整可显示
         */
        public boolean isFlagGroupDataEnd() { // 判断组数据是否结束
            return FlagGroupDataEnd; // 返回组数据结束标志
        }

        /**
         * 设置组数据结束标志
         *
         * @param flagGroupDataEnd true表示一组数据已解析完成，false表示组数据未完成
         * 业务意义：由解析器在检测到多字节拼接结束标志时调用
         */
        public void setFlagGroupDataEnd(boolean flagGroupDataEnd) { // 设置组数据结束标志
            FlagGroupDataEnd = flagGroupDataEnd; // 赋值组数据结束标志
        }

        //endregion

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：SPI帧数据可能由多个FPGA码字逐步给出，
         * 每个码字通过appendData()追加到sbData，帧结束时通过updateData()提交到Data字段
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前SpiStruct实例（this），支持链式调用
         * 业务意义：SPI帧数据由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public SpiStruct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            this.Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }


        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的SpiStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public SpiStruct clean() { // 重置所有字段为默认值
            this.Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            CurTime = 0; // 重置时间戳为0
            Data = ""; // 重置数据为空字符串
            Trigger = false; // 重置触发标志为false
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            FlagFrameEnd = false; // 重置帧结束标志为false
            FlagGroupDataEnd=false; // 重置组数据结束标志为false
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前SpiStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、数据、触发标志、组数据结束标志
         * 业务意义：调试时快速查看SPI解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch:" + Ch + // 通道名称
                    "CurrTime:" + CurTime + // 时间戳
                    "Data:" + Data + // 帧数据
                    "Trigger:" + Trigger+ // 触发标志
                    "FlagGroupDataEnd:"+FlagGroupDataEnd // 组数据结束标志
                    ;
        }

        /**
         * 获取SPI协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurrTime,Data,Trigger"
         * 业务意义：与静态方法toCSVTitleSpi()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurrTime,Data,Trigger"; // 通道,时间,数据,触发标志
        }

        /**
         * 将当前SPI解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,数据,触发标志
         * 业务意义：将单帧SPI解码结果格式化为CSV行，用于批量导出
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + // 通道名
                    TBookUtil.getStringFrom10us(CurTime) + "," + // 时间字符串（10μs转可读格式）
                    Data + "," + // 帧数据
                    Trigger // 触发标志
                    ;
        }

        /**
         * 获取当前SPI解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                        I2cStruct 内部类说明文档                                  │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   I2C总线文本解码数据结构 - 承载I2C协议单帧解码的文本结果                           │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储I2C单帧解码结果：通道、时间、地址、数据、ACK、触发、重启标志              │
     * │   2. 定义I2C协议FPGA码字的状态常量（帧结束/重启/应答/读/写/数据）                  │
     * │   3. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   4. 支持I2C协议的重复启动（Repeated Start）场景                                  │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → appendData()逐步拼接数据               │
     * │   → updateData()提交拼接结果 → SerialTxtBuffer缓存 → UI/CSV                     │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   I2C协议解码时，一帧包含地址+数据+ACK等字段，                                     │
     * │   支持读/写/重复启动等操作类型                                                    │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class I2cStruct implements ISerialBusTxtCSV { // I2C文本结构，实现CSV导出接口

        /**
         * [3]=1时，表示帧结束，取值：0x08（二进制1000）
         *
         * FPGA码字bit[3]=1时表示：I2C帧结束（STOP条件）
         * 此时低3位表示帧结束时的附加信息（如重启标志）
         */
        public static final int I2C_3_FrameEnd = 0x08; // 帧结束标志，bit[3]=1

        /**
         * 帧结束时的重启标志，取值：0x02（二进制010）
         *
         * 当[3]=1（帧结束）时，[2:0]中的bit[1]=1表示：
         * 当前帧结束是因为检测到重复启动（Repeated Start）条件，
         * 而非正常的STOP条件
         * 业务含义：I2C协议中，Repeated Start允许在不释放总线的情况下
         * 开始新的通信，常用于读操作中先写地址再读数据
         */
        public static final int I2C_2_FrameEnd_Reboot = 0x02; // 帧结束+重启，bit[1]=1

        /**
         * 应答信息标志，取值：0x04（二进制100）
         *
         * 当[3]=0（非帧结束）时，bit[2]=1表示：
         * 从机发送了应答信号（ACK），表示成功接收数据
         */
        public static final int I2C_2_Other_respond = 0x04; // 应答标志，bit[2]=1

        /**
         * 写地址操作，取值：0x01（二进制01）
         *
         * 当[3]=0且[2:0]的低2位为01时表示：
         * 当前码字为写地址操作（主机向从机写数据）
         */
        public static final int I2C_10_Other_Write = 0x01; // 写地址，低2位=01

        /**
         * 读地址操作，取值：0x02（二进制10）
         *
         * 当[3]=0且[2:0]的低2位为10时表示：
         * 当前码字为读地址操作（主机从从机读数据）
         */
        public static final int I2C_10_Other_Read = 0x02; // 读地址，低2位=10

        /**
         * 数据传输，取值：0x03（二进制11）
         *
         * 当[3]=0且[2:0]的低2位为11时表示：
         * 当前码字为数据传输（非地址，是有效数据字节）
         */
        public static final int I2C_10_Other_Data = 0x03; // 数据传输，低2位=11

        /**
         * 低2位掩码，取值：0x03（二进制11）
         *
         * 用于从FPGA码字中提取低2位的操作类型字段（写/读/数据）
         */
        public static final int I2C_10_Other_Mask = 0x03; // 低2位掩码

        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前I2C解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * I2C设备地址，取值范围：十六进制字符串如"50" / "--"（无地址）
         * 默认值："--"（无地址）
         * 业务含义：I2C通信的目标设备地址（7位或10位），
         * "--"表示当前码字不包含地址信息
         */
        public String Addr = "--"; // 设备地址，默认"--"表示无地址

        /**
         * I2C帧数据，取值范围：十六进制字符串，如"AA BB"
         * 默认值：空字符串""
         * 业务含义：I2C通信传输的数据字节，多个字节以空格分隔的十六进制形式展示
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * ACK应答确认标志，取值范围：true（从机应答ACK）/ false（从机无应答NACK）
         * 默认值：false
         * 业务含义：I2C通信中从机是否应答，
         * ACK=true表示从机成功接收，NACK=false表示从机未应答或拒绝
         */
        public boolean Confirm = false; // ACK应答标志

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件
         */
        public boolean Trigger = false; // 触发条件匹配标志

        /**
         * 重复启动（Repeated Start）标志，取值范围：true（检测到重启）/ false（无重启）
         * 默认值：false
         * 业务含义：I2C协议中Repeated Start条件标志，
         * 表示当前帧结束是因为检测到重复启动而非STOP条件
         */
        public boolean Reboot = false; // 重复启动标志

        /**
         * ACK显示颜色，取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB格式）
         * 默认值：0xFFFFFFFF（白色）
         * 业务含义：UI列表中ACK/NACK标志的文本颜色，
         * ACK通常为绿色，NACK通常为红色
         */
        public int Color = 0xFFFFFFFF; // ACK颜色，ARGB格式，默认白色

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：I2C帧数据可能由多个FPGA码字逐步给出
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前I2cStruct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd=false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }
        //endregion

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前I2cStruct实例（this），支持链式调用
         * 业务意义：I2C帧数据由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public I2cStruct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            this.Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的I2cStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public I2cStruct clean() { // 重置所有字段为默认值
            Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            CurTime = 0; // 重置时间戳为0
            Addr = "--"; // 重置地址为默认无地址
            Data = ""; // 重置数据为空字符串
            Confirm = false; // 重置ACK应答标志为false
            Trigger = false; // 重置触发标志为false
            Reboot = false; // 重置重复启动标志为false
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            FlagFrameEnd=false; // 重置帧结束标志为false
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前I2cStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、地址、数据、应答、触发、重启
         * 业务意义：调试时快速查看I2C解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch:" + Ch + // 通道名称
                    "CurTime:" + CurTime + // 时间戳
                    "Addr:" + Addr + // 设备地址
                    "Data:" + Data + // 帧数据
                    "Confirm:" + Confirm + // ACK应答标志
                    "Trigger:" + Trigger + // 触发标志
                    "Reboot:" + Reboot // 重复启动标志
                    ;
        }

        /**
         * 获取I2C协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot"
         * 业务意义：与静态方法toCSVTitleI2c()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot"; // 通道,时间,地址,数据,应答,触发,重启
        }

        /**
         * 将当前I2C解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,地址,数据,应答标志,触发标志,重启标志
         * 业务意义：将单帧I2C解码结果格式化为CSV行，用于批量导出
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Addr + "," + Data + "," + Confirm + "," + Trigger + "," + Reboot; // 通道,时间字符串,地址,数据,ACK,触发,重启
        }

        /**
         * 获取当前I2C解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                     MilSTD1553bStruct 内部类说明文档                              │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   MIL-STD-1553B总线文本解码数据结构 - 承载1553B协议单帧解码的文本结果               │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储1553B单帧解码结果：通道、时间、类型、远程地址、数据、触发、错误            │
     * │   2. 定义1553B协议FPGA码字低4位的状态常量                                         │
     * │     （远程地址/指令状态字6位/指令状态字5位/数据字1/数据字2/帧结束/曼切斯特码错误）  │
     * │   3. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   4. 支持临时数据暂存（temData），用于多码字拼接指令/状态字                        │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → 逐步填充各字段                         │
     * │   → appendData()拼接数据 → SerialTxtBuffer缓存 → UI/CSV                          │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   MIL-STD-1553B协议解码时，一帧包含远程终端地址+指令/状态字+数据字，               │
     * │   解析器逐码字填充各字段，帧结束时提交完整结果                                     │
     * │   1553B协议广泛应用于军用航空电子系统，采用曼切斯特编码                            │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class MilSTD1553bStruct implements ISerialBusTxtCSV { // MIL-STD-1553B文本结构，实现CSV导出接口

        /**
         * [3:0]="0001" 远程终端地址，取值：0x01
         *
         * FPGA码字低4位为0001时表示：当前码字为远程终端地址字段
         * 业务含义：1553B总线中每个远程终端有唯一地址（0~30，31为广播地址）
         */
        public static final int MilSTD1553b_RemoteAddr = 0x01; // 远程终端地址，低4位=0001

        /**
         * [3:0]="0010" 指令/状态字9-14位（6bit），取值：0x02
         *
         * FPGA码字低4位为0010时表示：当前码字为指令/状态字的第9-14位
         * 业务含义：指令字中为子地址/方式命令字段，状态字中为状态位字段
         */
        public static final int MilSTD1553b_CommandStateByte6 = 0x02; // 指令/状态字9-14位，低4位=0010

        /**
         * [3:0]="0011" 指令/状态字15-19位（5bit），取值：0x03
         *
         * FPGA码字低4位为0011时表示：当前码字为指令/状态字的第15-19位
         * 业务含义：指令字中为数据字计数/方式码字段，状态字中为状态信息字段
         */
        public static final int MilSTD1553b_CommandStateByte5 = 0x03; // 指令/状态字15-19位，低4位=0011

        /**
         * [3:0]="0100" 数据字1，取值：0x04
         *
         * FPGA码字低4位为0100时表示：当前码字为数据字1
         * 业务含义：1553B消息中的第一个数据字（16位）
         */
        public static final int MilSTD1553b_Data1 = 0x04; // 数据字1，低4位=0100

        /**
         * [3:0]="0101" 数据字2，取值：0x05
         *
         * FPGA码字低4位为0101时表示：当前码字为数据字2
         * 业务含义：1553B消息中的第二个数据字（16位）
         */
        public static final int MilSTD1553b_Data2 = 0x05; // 数据字2，低4位=0101

        /**
         * [3:0]="1111" 帧结束且奇校验正确，取值：0x0F
         *
         * FPGA码字低4位为1111时表示：帧结束，且奇校验通过
         * 业务含义：正常帧结束，数据完整可信
         */
        public static final int MilSTD1553b_FrameEnd_CheckSuccess = 0x0F; // 帧结束+校验正确，低4位=1111

        /**
         * [3:0]="0111" 帧结束且奇校验错误，取值：0x07
         *
         * FPGA码字低4位为0111时表示：帧结束，但奇校验失败
         * 业务含义：帧格式完整但数据可能损坏，需标记错误
         */
        public static final int MilSTD1553b_FrameEnd_CheckError = 0x07; // 帧结束+校验错误，低4位=0111

        /**
         * [3:0]="0110" 曼切斯特码错误，取值：0x06
         *
         * FPGA码字低4位为0110时表示：曼切斯特编码错误
         * 业务含义：1553B采用曼切斯特编码，每个位应有电平跳变，
         * 若检测到无跳变或非法跳变则为曼切斯特码错误
         */
        public static final int MilSTD1553b_ManchesterEncodingError = 0x06; // 曼切斯特码错误，低4位=0110

        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前1553B解码数据来源于哪个串口通道
         */
        public String Ch =TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * 字类型描述，取值范围：字符串，如"命令字"/"状态字"/"数据字"
         * 默认值：空字符串""
         * 业务含义：1553B协议中每个字有不同类型（指令字/状态字/数据字），
         * 此字段记录当前帧的字类型
         */
        public String Type = ""; // 字类型描述

        /**
         * 远程终端地址，取值范围："0"~"30"（十进制字符串）/ "N/A"（无地址）
         * 默认值："N/A"
         * 业务含义：1553B消息中远程终端的地址，
         * "N/A"表示当前码字不包含远程终端地址信息
         */
        public String RAddr = "N/A"; // 远程终端地址，默认N/A

        /**
         * 1553B帧数据，取值范围：十六进制字符串
         * 默认值：空字符串""
         * 业务含义：1553B消息中的数据字内容
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件
         */
        public boolean Trigger = false; // 触发条件匹配标志

        /**
         * 错误信息，取值范围：错误描述字符串或空字符串
         * 默认值：空字符串""
         * 业务含义：记录1553B帧解码过程中检测到的错误信息，
         * 如"曼切斯特码错误"、"奇校验错误"等
         */
        public String Error = ""; // 错误信息描述

        /**
         * 临时数据暂存，取值范围：0 ~ Integer.MAX_VALUE
         * 默认值：0
         * 业务含义：用于多码字拼接指令/状态字时暂存中间数据，
         * 1553B指令/状态字由多个码字组成，解析器逐码字累积后拼接
         */
        private int temData = 0; // 临时数据暂存

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前MilSTD1553bStruct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd=false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }
        //endregion

        /**
         * 设置临时数据
         *
         * @param data 要暂存的中间数据值
         * 业务意义：解析器在拼接多码字指令/状态字时，暂存中间结果
         */
        public void setTemData(int data) { // 设置临时数据
            this.temData = data; // 赋值临时数据
        }

        /**
         * 获取临时数据
         *
         * @return 暂存的中间数据值
         * 业务意义：解析器在拼接多码字指令/状态字时，读取暂存的中间结果
         */
        public int getTemData() { // 获取临时数据
            return this.temData; // 返回临时数据
        }

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：1553B帧数据可能由多个FPGA码字逐步给出
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前MilSTD1553bStruct实例（this），支持链式调用
         * 业务意义：1553B帧数据由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public MilSTD1553bStruct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的MilSTD1553bStruct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public MilSTD1553bStruct clean() { // 重置所有字段为默认值
            this.Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            this.CurTime = 0; // 重置时间戳为0
            this.Type = ""; // 重置字类型为空字符串
            this.RAddr = "N/A"; // 重置远程终端地址为N/A
            this.Data = ""; // 重置数据为空字符串
            this.Trigger = false; // 重置触发标志为false
            this.Error = ""; // 重置错误信息为空字符串
            this.temData = 0; // 重置临时数据为0
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            FlagFrameEnd=false; // 重置帧结束标志为false
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前MilSTD1553bStruct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、类型、远程地址、数据、错误
         * 注意：toString()中"Error:"后输出的是Trigger而非Error，疑似历史遗留bug
         * 业务意义：调试时快速查看1553B解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return " Ch:" + Ch + // 通道名称
                    " CurTime:" + CurTime + // 时间戳
                    " Type:" + Type + // 字类型
                    " RAddr:" + RAddr + // 远程终端地址
                    " Data:" + Data + // 帧数据
                    " Error:" + Trigger + // 注意：此处输出Trigger而非Error，疑似历史遗留bug
                    ""; // 空字符串结尾
        }

        /**
         * 获取MIL-STD-1553B协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurTime,Type,RAddr,Data,Trigger,Error"
         * 业务意义：与静态方法toCSVTitleM1553b()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurTime,Type,RAddr,Data,Trigger,Error"; // 通道,时间,类型,远程地址,数据,触发,错误
        }

        /**
         * 将当前1553B解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,类型,远程地址,数据,触发标志,错误
         * 业务意义：将单帧1553B解码结果格式化为CSV行，用于批量导出
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Type + "," + RAddr + "," + Data + "," + Trigger+","+Error; // 通道,时间字符串,类型,远程地址,数据,触发,错误
        }

        /**
         * 获取当前1553B解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────┐
     * │                      Arinc429Struct 内部类说明文档                                │
     * ├─────────────────────────────────────────────────────────────────────────────────┤
     * │ 【模块定位】                                                                     │
     * │   ARINC429总线文本解码数据结构 - 承载ARINC429协议单帧解码的文本结果                 │
     * │                                                                                 │
     * │ 【核心职责】                                                                     │
     * │   1. 存储429单帧解码结果：通道、时间、Label、SDI、数据、SSM、错误、触发             │
     * │   2. 定义429协议的解码格式常量（Label+Data / Label+D+SSM / Label+Data+SSM+SDI）  │
     * │   3. 定义429协议FPGA码字低4位的状态常量                                           │
     * │     （LABEL/数据1/数据2/数据3/校验错误/校验正确/错误帧）                          │
     * │   4. 实现ISerialBusTxtCSV接口，支持CSV导出                                       │
     * │   5. 支持三段数据暂存（data1/data2/data3），用于多码字拼接32位数据                 │
     * │                                                                                 │
     * │ 【数据流】                                                                       │
     * │   FPGA码字 → SerialBusTxtStructParse解析 → 逐步填充各字段                         │
     * │   → appendData()拼接数据 → SerialTxtBuffer缓存 → UI/CSV                          │
     * │                                                                                 │
     * │ 【使用场景】                                                                     │
     * │   ARINC429协议解码时，一帧32位数据由多个FPGA码字逐步给出，                         │
     * │   解析器逐码字填充Label/SDI/Data/SSM等字段，帧结束时提交完整结果                   │
     * │   ARINC429协议广泛应用于民用航空电子系统                                           │
     * └─────────────────────────────────────────────────────────────────────────────────┘
     */
    public class Arinc429Struct implements ISerialBusTxtCSV { // ARINC429文本结构，实现CSV导出接口

        //region ARINC429解码格式常量
        /**
         * 解码格式：Label+Data，取值：0x00
         *
         * 业务含义：仅解码Label和数据字段，不显示SDI和SSM
         * 适用于简单的ARINC429数据查看场景
         */
        public static final int Arinc429_Format_LabelDATA = 0x00; // 格式：Label+Data

        /**
         * 解码格式：Label+D+SSM，取值：0x01
         *
         * 业务含义：解码Label、数据和SSM（符号状态矩阵）字段
         * SSM用于指示数据的正负号和状态信息
         */
        public static final int Arinc429_Format_LDSSM = 0x01; // 格式：Label+D+SSM

        /**
         * 解码格式：Label+Data+SSM+SDI，取值：0x02
         *
         * 业务含义：完整解码所有字段，包括Label、数据、SSM和SDI（源目标标识）
         * SDI用于标识数据来源或目标设备
         */
        public static final int Arinc429_Format_LDSSMSDI = 0x02; // 格式：Label+Data+SSM+SDI
        //endregion

        //region ARINC429 FPGA码字低4位状态常量
        /**
         * [3:0]="0001" LABEL字段，取值：0x01
         *
         * FPGA码字低4位为0001时表示：当前码字为LABEL字段
         * 业务含义：ARINC429的LABEL为8位，标识数据类型，
         * 通常以八进制表示（如205=气压高度）
         */
        public static final int Arinc429_Label = 0x01; // LABEL字段，低4位=0001

        /**
         * [3:0]="0010" 数据1，取值：0x02
         *
         * FPGA码字低4位为0010时表示：当前码字为数据段第1部分
         * 业务含义：ARINC429的32位数据由多个码字组成，此为第一段
         */
        public static final int Arinc429_Data1 = 0x02; // 数据1，低4位=0010

        /**
         * [3:0]="0011" 数据2，取值：0x03
         *
         * FPGA码字低4位为0011时表示：当前码字为数据段第2部分
         * 业务含义：ARINC429的32位数据的第二段
         */
        public static final int Arinc429_Data2 = 0x03; // 数据2，低4位=0011

        /**
         * [3:0]="0110" 数据3且奇偶校验错误，取值：0x06
         *
         * FPGA码字低4位为0110时表示：当前码字为数据段第3部分，
         * 且奇校验失败
         * 业务含义：帧数据完整但校验错误，数据可能损坏
         */
        public static final int Arinc429_CheckError = 0x06; // 数据3+校验错误，低4位=0110

        /**
         * [3:0]="0100" 数据3且奇偶校验正确，取值：0x04
         *
         * FPGA码字低4位为0100时表示：当前码字为数据段第3部分，
         * 且奇校验通过
         * 业务含义：正常帧结束，数据完整可信
         */
        public static final int Arinc429_CheckSuccess = 0x04; // 数据3+校验正确，低4位=0100

        /**
         * [3:0]="0101" 错误帧，取值：0x05
         *
         * FPGA码字低4位为0101时表示：当前帧为错误帧
         * 业务含义：帧格式错误，无法正常解析
         */
        public static final int Arinc429_FrameError = 0x05; // 错误帧，低4位=0101
        //endregion


        /**
         * 通道名称，取值范围："S1"/"S2"/"S3"/"S4"
         * 默认值："S1"
         * 业务含义：标识当前ARINC429解码数据来源于哪个串口通道
         */
        public String Ch = TChan.getChannelName(TChan.S1); // 通道名称，默认S1

        /**
         * 当前帧时间戳，取值范围：0 ~ Long.MAX_VALUE
         * 默认值：0
         * 业务含义：帧起始时刻的时间戳，单位为10μs
         */
        public long CurTime = 0; // 帧时间戳，单位10μs

        /**
         * ARINC429 LABEL值，取值范围：0 ~ 255（8位，通常以八进制表示）
         * 默认值：0
         * 业务含义：ARINC429的LABEL标识数据类型，
         * 如八进制205表示气压高度，206表示惯性垂直速度等，
         * 在CSV中以八进制形式输出
         */
        public int Label = 0; // LABEL值，8位

        /**
         * SDI（源目标标识），取值范围：字符串"00"/"01"/"10"/"11"/""
         * 默认值：空字符串""
         * 业务含义：2位SDI标识数据的来源或目标设备，
         * 用于同一LABEL下区分不同设备实例
         */
        public String SDI = ""; // SDI源目标标识

        /**
         * ARINC429帧数据，取值范围：十六进制字符串
         * 默认值：空字符串""
         * 业务含义：ARINC429消息中的有效数据（19位BNR/BCD编码）
         */
        public String Data = ""; // 帧数据，十六进制字符串

        /**
         * SSM（符号状态矩阵），取值范围：字符串"00"/"01"/"10"/"11"/""
         * 默认值：空字符串""
         * 业务含义：2位SSM指示数据的正负号和状态：
         *   00=正/正常，01=无计算数据，10=功能测试，11=负/故障
         */
        public String SSM = ""; // SSM符号状态矩阵

        /**
         * 错误信息，取值范围：错误描述字符串或空字符串
         * 默认值：空字符串""
         * 业务含义：记录ARINC429帧解码过程中检测到的错误信息
         */
        public String Error = ""; // 错误信息描述

        /**
         * 触发标志，取值范围：true（满足触发条件）/ false（不满足触发条件）
         * 默认值：false
         * 业务含义：标记当前帧是否满足用户设置的触发条件
         */
        public boolean Trigger = false; // 触发条件匹配标志

        /**
         * 数据解析次数计数器，取值范围：0 ~ Integer.MAX_VALUE
         * 默认值：0
         * 业务含义：记录当前帧已解析的数据码字次数，
         * 用于跟踪多码字拼接进度
         */
        public int Times=0; // 数据解析次数计数器

        //region 帧结束标志属性
        /**
         * 帧结束标志，取值范围：true（帧已结束）/ false（帧未结束）
         * 默认值：false
         * 业务含义：标记当前Arinc429Struct是否为一帧的最后一个数据
         */
        private boolean FlagFrameEnd=false; // 帧结束标志，默认false

        /**
         * 获取帧结束标志
         *
         * @return true表示当前帧已结束，false表示帧未结束
         * 业务意义：UI层根据此标志判断是否需要换行显示下一帧数据
         */
        public boolean isFlagFrameEnd() { // 判断帧是否结束
            return FlagFrameEnd; // 返回帧结束标志
        }

        /**
         * 设置帧结束标志
         *
         * @param flagFrameEnd true表示帧已结束，false表示帧未结束
         * 业务意义：由解析器在检测到FPGA帧结束标志位时调用
         */
        public void setFlagFrameEnd(boolean flagFrameEnd) { // 设置帧结束标志
            FlagFrameEnd = flagFrameEnd; // 赋值帧结束标志
        }
        //endregion

        /**
         * 错误编号，取值范围：0（无错误）/ 其他（错误码）
         * 默认值：0
         * 业务含义：记录ARINC429帧的具体错误类型编号
         */
        private int errorNo; // 错误编号

        /**
         * 获取错误编号
         *
         * @return 错误编号，0表示无错误
         * 业务意义：获取当前帧的具体错误类型编号
         */
        public int getErrorNo() { // 获取错误编号
            return errorNo; // 返回错误编号
        }

        /**
         * 设置错误编号
         *
         * @param errorNo 错误编号，0表示无错误
         * 业务意义：由解析器在检测到错误时设置具体错误类型编号
         */
        public void setErrorNo(int errorNo) { // 设置错误编号
            this.errorNo = errorNo; // 赋值错误编号
        }

        /**
         * 数据段第1部分暂存，取值范围：0 ~ Integer.MAX_VALUE
         * 默认值：0
         * 业务含义：ARINC429的32位数据由多个FPGA码字组成，
         * data1暂存第一个数据码字的原始值
         */
        private int data1, data2, data3; // 数据段三部分暂存，用于多码字拼接32位数据

        /**
         * 设置数据段第1部分
         *
         * @param data1 数据段第1部分的原始值
         * 业务意义：解析器在解析到Arinc429_Data1类型码字时调用
         */
        public void setData1(int data1) { // 设置数据1
            this.data1 = data1; // 赋值数据1
        }

        /**
         * 设置数据段第2部分
         *
         * @param data2 数据段第2部分的原始值
         * 业务意义：解析器在解析到Arinc429_Data2类型码字时调用
         */
        public void setData2(int data2) { // 设置数据2
            this.data2 = data2; // 赋值数据2
        }

        /**
         * 设置数据段第3部分
         *
         * @param data3 数据段第3部分的原始值
         * 业务意义：解析器在解析到Arinc429_CheckSuccess/Arinc429_CheckError类型码字时调用
         */
        public void setData3(int data3) { // 设置数据3
            this.data3 = data3; // 赋值数据3
        }

        /**
         * 获取数据段第1部分
         *
         * @return 数据段第1部分的原始值
         * 业务意义：用于拼接完整的32位ARINC429数据
         */
        public int getData1() { // 获取数据1
            return this.data1; // 返回数据1
        }

        /**
         * 获取数据段第2部分
         *
         * @return 数据段第2部分的原始值
         * 业务意义：用于拼接完整的32位ARINC429数据
         */
        public int getData2() { // 获取数据2
            return this.data2; // 返回数据2
        }

        /**
         * 获取数据段第3部分
         *
         * @return 数据段第3部分的原始值
         * 业务意义：用于拼接完整的32位ARINC429数据
         */
        public int getData3() { // 获取数据3
            return this.data3; // 返回数据3
        }

        /**
         * 数据拼接缓冲区，用于逐步拼接多字节数据
         * 业务含义：ARINC429帧数据可能由多个FPGA码字逐步给出
         */
        private StringBuilder sbData = new StringBuilder(); // 数据拼接缓冲区

        /**
         * 追加数据到拼接缓冲区，返回自身引用以支持链式调用
         *
         * @param data 要追加的数据字符串（通常为十六进制格式）
         * @return 当前Arinc429Struct实例（this），支持链式调用
         * 业务意义：ARINC429帧数据由多个码字逐步给出，解析器每解析一个数据码字即调用此方法追加
         */
        public Arinc429Struct appendData(String data) { // 追加数据到缓冲区
            sbData.append(data); // 将数据追加到StringBuilder
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将拼接缓冲区内容提交到Data字段
         *
         * 业务意义：帧结束时调用，将sbData中累积的所有数据一次性赋值给Data字段
         */
        public void updateData() { // 提交拼接缓冲区到Data字段
            this.Data = sbData.toString(); // 将StringBuilder内容转为字符串赋值给Data
        }

        /**
         * 重置所有字段为默认值，返回自身引用以支持链式调用
         *
         * @return 重置后的Arinc429Struct实例（this）
         * 业务意义：由于使用单例模式，每次解码新帧前必须调用clean()重置字段，
         * 避免上一帧的残留数据污染当前帧的解码结果
         */
        public Arinc429Struct clean() { // 重置所有字段为默认值
            this.Ch = TChan.getChannelName(TChan.S1); // 重置通道名称为默认S1
            this.CurTime = 0; // 重置时间戳为0
            this.Label = 0; // 重置LABEL为0
            this.SDI = ""; // 重置SDI为空字符串
            this.Data = ""; // 重置数据为空字符串
            this.SSM = ""; // 重置SSM为空字符串
            this.Error = ""; // 重置错误信息为空字符串
            this.Trigger = false; // 重置触发标志为false

            this.data1 = 0; // 重置数据1暂存为0
            data2 = 0; // 重置数据2暂存为0
            data3 = 0; // 重置数据3暂存为0
            sbData.delete(0, sbData.length()); // 清空数据拼接缓冲区
            FlagFrameEnd=false; // 重置帧结束标志为false
            Times=0; // 重置数据解析次数为0
            return this; // 返回自身引用，支持链式调用
        }

        /**
         * 将当前Arinc429Struct转换为字符串表示，用于调试输出
         *
         * @return 格式化字符串，包含通道、时间、Label、SDI、数据、SSM、错误、触发
         * 业务意义：调试时快速查看ARINC429解码结果
         */
        @Override
        public String toString() { // 转换为调试字符串
            return "Ch:" + Ch + // 通道名称
                    " CurTime:" + CurTime + // 时间戳
                    " Label:" + Label + // LABEL值
                    " SDI:" + SDI + // SDI源目标标识
                    " Data:" + Data + // 帧数据
                    " SSM:" + SSM + // SSM符号状态矩阵
                    " Error:" + Error + // 错误信息
                    " Trigger:" + Trigger + // 触发标志
                    ""; // 空字符串结尾
        }

        /**
         * 获取ARINC429协议的CSV表头
         *
         * @return CSV表头字符串 "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger"
         * 业务意义：与静态方法toCSVTitleArinc492()返回值一致
         */
        @Override
        public String toCSVHead() { // 返回CSV表头
            return "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger"; // 通道,时间,LABEL,SDI,数据,SSM,错误,触发
        }

        /**
         * 将当前ARINC429解码数据转换为CSV数据行
         *
         * @return CSV数据行，格式：通道名,时间字符串,LABEL(八进制),SDI,数据,SSM,错误,触发标志
         * 业务意义：将单帧ARINC429解码结果格式化为CSV行，用于批量导出。
         * 注意：LABEL以八进制形式输出，这是ARINC429协议的标准表示法
         */
        @Override
        public String toCSV() { // 转换为CSV数据行
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Integer.toString(Label,8) + "," + SDI + "," + Data + "," + SSM + "," + Error + "," + Trigger; // 通道,时间字符串,LABEL八进制,SDI,数据,SSM,错误,触发
        }

        /**
         * 获取当前ARINC429解码数据所属的通道名称
         *
         * @return 通道名称字符串，如"S1"
         * 业务意义：CSV导出时按通道维度组织数据
         */
        @Override
        public String getCh() { // 返回通道名称
            return Ch; // 返回通道名称字段
        }
    }

}
