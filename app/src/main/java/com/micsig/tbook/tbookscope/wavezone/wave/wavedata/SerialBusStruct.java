package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包，包含协议解析、数据结构定义和缓存管理

//import android.annotation.IntDef; // 旧版Android注解，已替换为androidx版本


import androidx.annotation.IntDef; // AndroidX注解库，用于定义整型常量注解，确保类型安全

import com.micsig.smart.Property; // 全局属性常量类，定义总线类型索引等系统级常量
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 水平轴管理类，提供像素到时间的转换系数
import com.micsig.tbook.ui.util.TBookUtil; // UI工具类，提供时间格式化等通用方法

import java.lang.annotation.Retention; // 注解保留策略元注解
import java.lang.annotation.RetentionPolicy; // 注解保留策略枚举，SOURCE表示仅源码保留

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                         SerialBusStruct                                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 串口总线图像解码数据结构定义                              │
 * │ 核心职责：定义7种串口总线协议的解码数据结构与配置参数，为示波器总线解码功能        │
 * │          提供统一的数据载体和CSV导出支持                                        │
 * │ 架构设计：采用Holder单例模式，作为非静态内部类的宿主类；                          │
 * │          每种总线协议对应一个XxxStruct内部类（解码数据）和                        │
 * │          一个XxxSettingStruct内部类（配置参数）；                               │
 * │          所有XxxStruct均实现ISerialBusCSV接口以支持CSV导出                      │
 * │ 数据流：  FPGA硬件解码 → C层解析 → Java层Struct封装 → UI渲染/CSV导出            │
 * │ 依赖关系：Property（总线类型常量）、HorizontalAxis（时间轴转换）、               │
 * │          TBookUtil（时间格式化）、ICharacterEncoding（编码方式常量）             │
 * │ 使用场景：示波器串口总线解码功能中，FPGA解码结果经C层JNI回调后，                  │
 * │          由Java层填充Struct字段，再由UI层读取渲染波形叠加层和列表页              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 内部类清单：                                                                  │
 * │   SerialBusStructHolder - 单例持有者（懒加载线程安全）                          │
 * │   UartStruct            - UART串口解码数据结构                                 │
 * │   LinStruct             - LIN总线解码数据结构                                  │
 * │   CanStruct             - CAN总线解码数据结构                                  │
 * │   SpiStruct             - SPI总线解码数据结构                                  │
 * │   I2cStruct             - I2C总线解码数据结构                                  │
 * │   MilSTD1553bStruct     - MIL-STD-1553B总线解码数据结构                        │
 * │   Arinc429Struct        - ARINC429总线解码数据结构                             │
 * │   UartSettingStruct     - UART配置参数                                        │
 * │   LinSettingStruct      - LIN配置参数                                         │
 * │   CanSettingStruct      - CAN配置参数                                         │
 * │   SpiSettingStruct      - SPI配置参数                                         │
 * │   I2cSettingStruct      - I2C配置参数                                         │
 * │   MilSID1553bSettingStruct - MIL-STD-1553B配置参数                            │
 * │   Arinc429SettingStruct - ARINC429配置参数                                    │
 * │   ISerialBusCSV         - CSV导出接口                                         │
 * │   @SerialBusType        - 总线类型注解                                        │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * 串口总线图像解码数据结构定义类（Holder单例）。
 * 包含7种总线协议的内部类，每种协议由数据结构类和配置参数类组成。
 * 数据结构类存储FPGA解码后的帧信息（位置、类型、数据、颜色等），
 * 配置参数类存储用户在界面上设置的编码方式和位长等参数。
 *
 * Created by liwb on 2017/10/17.
 */

public class SerialBusStruct {

    //region 单例模式 —— Holder内部类实现，利用类加载机制保证线程安全
    public static class SerialBusStructHolder { // 单例持有者内部类，JVM类加载时保证线程安全
        public static final SerialBusStruct instance = new SerialBusStruct(); // 静态final实例，类加载时创建，全局唯一
    }

    /**
     * 获取SerialBusStruct单例实例
     *
     * @return SerialBusStruct全局唯一实例，用于访问各总线数据结构和配置参数
     */
    public static SerialBusStruct getInstance() { // 获取单例实例
        return SerialBusStructHolder.instance; // 返回Holder中持有的唯一实例
    }
    //endregion

    //region 总线类型常量定义 —— 与Property类中总线索引值保持一致
    //原定义（已弃用，改用Property中的常量）
//    public static final int SerialBusType_UART=1; // 旧版UART类型常量，值为1
//    public static final int SerialBusType_LIN=2;  // 旧版LIN类型常量，值为2
//    public static final int SerialBusType_CAN=3;  // 旧版CAN类型常量，值为3
//    public static final int SerialBusType_SPI=4;  // 旧版SPI类型常量，值为4
//    public static final int SerialBusType_I2C=5;  // 旧版I2C类型常量，值为5
//    public static final int SerialBusType_429=6;  // 旧版ARINC429类型常量，值为6
//    public static final int SerialBusType_1553B=7; // 旧版1553B类型常量，值为7

    /**
     * 串口总线类型注解，限定参数取值范围为7种总线类型常量。
     * 使用@IntDef实现编译期类型检查，防止传入非法总线类型值。
     */
    @IntDef({SerialBusType_UART,SerialBusType_LIN,SerialBusType_CAN,SerialBusType_SPI,SerialBusType_I2C,SerialBusType_429,SerialBusType_1553B}) // 声明允许的整型常量集合
    @Retention(RetentionPolicy.SOURCE) // 注解仅保留在源码中，不编入class文件
    public @interface SerialBusType{} // 总线类型注解定义，用于方法参数类型约束

    // 以下常量与Property类中总线索引值一一对应，取值范围：0~6
    public static final int SerialBusType_UART= Property.BUS_UART;   // UART串口总线类型，值=0
    public static final int SerialBusType_LIN=Property.BUS_LIN;      // LIN总线类型，值=1
    public static final int SerialBusType_CAN=Property.BUS_CAN;      // CAN总线类型，值=2
    public static final int SerialBusType_SPI=Property.BUS_SPI;      // SPI总线类型，值=3
    public static final int SerialBusType_I2C=Property.BUS_I2C;      // I2C总线类型，值=4
    public static final int SerialBusType_429=Property.BUS_429;      // ARINC429总线类型，值=6
    public static final int SerialBusType_1553B=Property.BUS_1553B;  // MIL-STD-1553B总线类型，值=5
    //endregion

    /**
     * 起始数据类型标识，值0x00。
     * 表示解码帧的起始数据段，用于标识一帧数据的开始位置。
     * 取值范围：0x00
     */
    public static final int DataType_BeginData=0x00; // 起始数据类型常量，标识帧数据起始段


    /**
     * 串口总线CSV导出接口。
     * 所有总线数据结构类均实现此接口，以支持将解码数据导出为CSV格式文件。
     * 导出时先调用toCsvHead()获取表头行，再对每条数据调用toCSV()获取数据行。
     */
    public interface ISerialBusCSV {
        /**
         * 获取CSV表头字符串。
         * 表头定义了导出列的名称，各总线实现类根据自身字段返回不同的表头。
         *
         * @return CSV表头字符串，列名以英文逗号分隔，如"BeginX,EndX,Data,DataColor"
         */
        String toCsvHead(); // 返回CSV文件表头行

        /**
         * 将当前总线解码数据转换为CSV数据行字符串。
         * 时间值由像素坐标转换为可读时间格式，μs符号替换为ASCII的u以兼容CSV编码。
         *
         * @return CSV数据行字符串，字段以英文逗号分隔；若Data为null则返回空字符串
         */
        String toCSV(); // 返回CSV文件数据行
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                         UartStruct                                       │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：UART串口解码数据结构                                              │
     * │ 核心职责：存储UART协议解码后的单帧数据，包含帧位置、校验状态、               │
     * │          数据内容及显示颜色                                                │
     * │ 数据流：  FPGA UART解码 → C层填充 → UartStruct → UI渲染/CSV导出           │
     * │ 使用场景：UART串口总线解码时，每解码出一帧数据即创建一个UartStruct实例       │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * UART串口解码数据结构。
     * 存储单帧UART解码结果，包括帧起止位置（像素坐标）、数据类型（校验+停止位状态）、
     * 解码数据内容、显示颜色等。DataType字段的bit3标识9bit模式，bit0~2标识校验和停止位状态。
     */
    public class UartStruct implements ISerialBusCSV {
        //region DataType常量定义 —— 定义UART帧数据类型标识
        /** bit3=1时表示9bit数据的第9位，值0x08；bit3=0时表示9bit数据的前8位 */
        public static final int DataType_3=0x08; // 9bit数据第9位标识，bit[3]=1
        /**
         * 校验正确，无停止位，值0x02。
         * DataType低3位=010b，表示校验通过但缺少停止位（帧异常）。
         */
        public static final int DataType_CRCSucc_NoStop=0x02; // 校验正确+无停止位
        /**
         * 校验错误，无停止位，值0x03。
         * DataType低3位=011b，表示校验失败且缺少停止位（帧严重异常）。
         */
        public static final int DataType_CRCFailed_noStop=0x03; // 校验错误+无停止位
        /**
         * 校验正确，有停止位，值0x06。
         * DataType低3位=110b，表示正常帧，校验通过且有停止位。
         */
        public static final int DataType_CRCSucc_Stop=0x06; // 校验正确+有停止位（正常帧）
        /**
         * 校验错误，有停止位，值0x07。
         * DataType低3位=111b，表示校验失败但有停止位。
         */
        public static final int DataType_CRCFailed_Stop=0x07; // 校验错误+有停止位
        //endregion

        /**
         * 是否有开始帧标识。
         * true表示当前帧为UART通信的起始帧，用于帧序列边界识别。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧，true=起始帧，false=非起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 乘以HorizontalAxis.timesPrePix可转换为实际时间值。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素），用于定位波形上的帧起始位置

        /**
         * 帧结束X轴位置，单位：像素。
         * 与BeginX配合确定帧在波形上的时间跨度。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素），用于定位波形上的帧结束位置

        /**
         * 数据类型标识字节。
         * bit[3]：9bit数据标识，1=第9位，0=前8位
         * bit[2:0]：校验与停止位状态
         *   010 = 校验正确，无停止位
         *   011 = 校验错误，无停止位
         *   110 = 校验正确，有停止位
         *   111 = 校验错误，有停止位
         * 取值范围：0x00 ~ 0x0F
         */
        public byte DataType; // 数据类型，编码校验状态和9bit模式标识

        /**
         * 解码数据内容，十六进制/二进制/ASCII字符串。
         * 由C层根据编码方式将原始字节转换为对应格式的字符串。
         * 取值范围：非null时为有效数据字符串，如"FF"、"10100101"、"A"
         */
        public String Data; // 解码后的数据字符串，格式由UartSettingStruct.encoding决定

        /**
         * 数据标识ID，用于帧序列中唯一标识数据段。
         * 取值范围：由FPGA解码逻辑分配，通常为递增序号
         */
        public int Id; // 数据段标识ID，用于帧内数据段的唯一标识

        /**
         * 显示颜色值，ARGB格式。
         * 用于在波形叠加层上以不同颜色渲染不同状态的帧数据。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int  DataColor; // 帧数据显示颜色（ARGB），不同校验状态对应不同颜色

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767（short范围）
         */
        public short Reserve; // 预留字段，供未来协议扩展使用
        //endregion


        /**
         * 获取UART数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,Color"
         */
        @Override
        public String toCsvHead() { // 返回UART CSV导出表头
            return "BeginX,EndX,Data,Color"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将UART解码数据转换为CSV数据行。
         * 时间值由像素坐标乘以时间系数后格式化为可读字符串，
         * μs符号替换为ASCII的u以避免CSV编码问题。
         *
         * @return CSV数据行字符串，格式：起始时间,结束时间,数据,颜色十六进制；
         *         若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将UART帧数据转换为CSV行
            if (Data==null)return ""; // 数据为空时返回空字符串，跳过无效帧
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 像素坐标×时间系数→秒值→格式化时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置同理转换为时间字符串
            b=b.replace("μ","u"); // 将μ替换为u，避免CSV文件编码问题
            e=e.replace("μ","u"); // 结束时间同样替换μ符号
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor); // 拼接CSV行：时间,时间,数据,颜色十六进制
        }

        /**
         * 返回UartStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串，格式：UartStruct{isBeginFrame=..., BeginX=..., ...}
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("UartStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加解码数据内容
            sb.append(", Id=").append(Id); // 追加数据标识ID
            sb.append(", DataColor=0x").append(Integer.toHexString(DataColor)); // 追加颜色十六进制值
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                         LinStruct                                        │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：LIN总线解码数据结构                                              │
     * │ 核心职责：存储LIN协议解码后的单帧数据，包含ID校验、数据、同步信号、           │
     * │          唤醒信号及校验和等状态                                            │
     * │ 数据流：  FPGA LIN解码 → C层填充 → LinStruct → UI渲染/CSV导出             │
     * │ 使用场景：LIN总线解码时，每解码出一帧数据即创建一个LinStruct实例             │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * LIN总线解码数据结构。
     * 存储单帧LIN解码结果，包括帧起止位置、数据类型（ID校验/数据/同步/唤醒/校验和）、
     * 解码数据内容及显示颜色。DataType字段的bit3标识校验成功/失败，bit0~2标识数据段类型。
     */
    public class LinStruct implements ISerialBusCSV{
        /** 奇偶校验正确的ID，值0x01；DataType=0001b，bit3=0表示校验正确，低3位=001表示ID段 */
        public static final int ID_ODD_Sucess=0x01; // ID奇偶校验正确
        /** 奇偶校验错误的ID，值0x09；DataType=1001b，bit3=1表示校验错误，低3位=001表示ID段 */
        public static final int ID_ODD_Failed=0x09; // ID奇偶校验错误
        /** 数据且有停止位，值0x02；DataType=0010b，bit3=0表示有停止位，低3位=010表示数据段 */
        public static final int Data_Stop=0x02; // 数据段+有停止位（正常数据）
        /** 数据且无停止位，值0x0A；DataType=1010b，bit3=1表示无停止位，低3位=010表示数据段 */
        public static final int Data_NoStop=0x0A; // 数据段+无停止位（异常数据）
        /** 正确校验和，值0x03；DataType=0011b，bit3=0表示校验正确，低3位=011表示校验和段 */
        public static final int Check_Success=0x03; // 校验和正确
        /** 错误校验和，值0x0B；DataType=1011b，bit3=1表示校验错误，低3位=011表示校验和段 */
        public static final int Check_Failed=0x0B; // 校验和错误
        /** 正确的同步信号，值0x04；DataType=0100b，bit3=0表示正确，低3位=100表示同步段 */
        public static final int SYNC_Success=0x04; // 同步信号正确
        /** 错误的同步信号，值0x0C；DataType=1100b，bit3=1表示错误，低3位=100表示同步段 */
        public static final int SYNC_Failed=0x0C; // 同步信号错误
        /** 唤醒信号，值0x05；DataType=0101b，bit3=0表示正确，低3位=101表示唤醒段 */
        public static final int Wake_Success=0x05; // 唤醒信号正确
        /** 唤醒错误，值0x0D；DataType=1101b，bit3=1表示错误，低3位=101表示唤醒段 */
        public static final int Wake_Failed=0x0D; // 唤醒信号错误
        /** 同步间隔的上升沿，值0x08；DataType=1000b，标识同步间隔段的上升沿 */
        public static final int SYNC_Distance=0x08; // 同步间隔上升沿

        /**
         * 是否有开始帧标识。
         * true表示当前帧为LIN通信的起始帧。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧，true=起始帧，false=非起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * bit[3]：0=正确/有停止位，1=错误/无停止位
         * bit[2:0]：段类型（001=ID, 010=数据, 011=校验和, 100=同步, 101=唤醒）
         * 取值范围：0x00 ~ 0x0F
         */
        public int DataType; // 数据类型，编码段类型和校验状态

        /**
         * 解码数据内容，格式由LinSettingStruct.encoding决定。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data; // 解码后的数据字符串

        /**
         * 显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 帧数据显示颜色（ARGB）

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段，供未来协议扩展使用

        /**
         * 获取LIN数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,DataColor"
         */
        @Override
        public String toCsvHead() { // 返回LIN CSV导出表头
            return "BeginX,EndX,Data,DataColor"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将LIN解码数据转换为CSV数据行。
         * 时间值由像素坐标乘以时间系数后格式化为可读字符串，
         * μs符号替换为ASCII的u以避免CSV编码问题。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将LIN帧数据转换为CSV行
            if (Data==null)return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u，避免CSV编码问题
            e=e.replace("μ","u"); // 结束时间同样替换
            return  b+","+e+","+Data+",0x"+Integer.toHexString(DataColor); // 拼接CSV行
        }

        /**
         * 返回LinStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串，格式：LinStruct{isBeginFrame=..., BeginX=..., ...}
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("LinStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加解码数据内容
            sb.append(", DataColor=").append(Integer.toHexString(DataColor)); // 追加颜色十六进制值
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                         CanStruct                                        │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：CAN总线解码数据结构                                              │
     * │ 核心职责：存储CAN协议解码后的单帧数据，包含标准/扩展ID、DLC、               │
     * │          数据段、CRC、错误帧、过载帧等状态                                  │
     * │ 数据流：  FPGA CAN解码 → C层填充 → CanStruct → UI渲染/CSV导出             │
     * │ 使用场景：CAN/CAN-FD总线解码时，每解码出一帧数据即创建一个CanStruct实例     │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * CAN总线解码数据结构。
     * 存储单帧CAN解码结果，包括帧起止位置、数据类型（标准ID/扩展ID/DLC/DATA/CRC/错误帧等）、
     * 解码数据内容、帧ID及显示颜色。
     */
    public class CanStruct implements ISerialBusCSV{
        /** 标准ID帧，值0x01；DataType=0001b，表示当前段为11位标准标识符 */
        public static final int StandardID=0x01; // 标准帧ID（11位标识符）
        /** 扩展ID帧，值0x06；DataType=0110b，表示当前段为29位扩展标识符 */
        public static final int ExtendId=0x06; // 扩展帧ID（29位标识符）
        /** DLC数据长度码，值0x02；DataType=0010b，表示当前段为数据长度码（4位） */
        public static final int DLC=0x02; // 数据长度码段
        /** DATA数据段，值0x03；DataType=0011b，表示当前段为数据场 */
        public static final int DATA=0x03; // 数据段
        /** CRC校验段，值0x04；DataType=0100b，表示当前段为CRC校验码 */
        public static final int CRC=0x04; // CRC校验段
        /** 错误帧，值0x08；DataType=1000b，表示当前为CAN错误帧 */
        public static final int Error=0x08; // 错误帧
        /** 过载帧，值0x09；DataType=1001b，表示当前为CAN过载帧 */
        public static final int OverLoad=0x09; // 过载帧
        /** 帧结束，有确认（ACK），值0x0A；DataType=1010b，表示帧正常结束且有ACK确认 */
        public static final int ConfirmOver=0x0A; // 帧结束+有ACK确认
        /** 帧结束，无确认（ACK），值0x0E；DataType=1110b，表示帧结束但无ACK确认 */
        public static final int NoConfirmOver=0x0E; // 帧结束+无ACK确认
        /** 问题帧，值0x0B；DataType=1011b，不满足CAN协议规范的问题帧 */
        public static final int Trouble=0x0B; // 问题帧（不满足CAN协议规范）

        /**
         * 是否有开始帧标识。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * 取值范围：StandardID(0x01), DLC(0x02), DATA(0x03), CRC(0x04),
         *          ExtendId(0x06), Error(0x08), OverLoad(0x09),
         *          ConfirmOver(0x0A), Trouble(0x0B), NoConfirmOver(0x0E)
         */
        public int DataType; // 数据类型，标识当前段是ID/DLC/DATA/CRC/错误帧等

        /**
         * 解码数据内容，格式由CanSettingStruct.encoding决定。
         * 默认空字符串""，与UartStruct/LinStruct不同（其他为null）。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data=""; // 解码后的数据字符串，默认空字符串

        /**
         * CAN帧标识ID，用于标识当前数据段所属的CAN帧。
         * 取值范围：由FPGA解码逻辑分配
         */
        public int ID; // CAN帧标识ID，用于帧内数据段关联

        /**
         * 显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 帧数据显示颜色（ARGB）

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段

        /**
         * 获取CAN数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,DataColor"
         */
        @Override
        public String toCsvHead() { // 返回CAN CSV导出表头
            return "BeginX,EndX,Data,DataColor"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将CAN解码数据转换为CSV数据行。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将CAN帧数据转换为CSV行
            if (Data==null) return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u
            e=e.replace("μ","u"); // 结束时间同样替换
            return b+","+e+","+Data+",+0x"+Integer.toHexString(DataColor); // 拼接CSV行（注意颜色前多了一个+号）
        }

        /**
         * 返回CanStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("CanStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加解码数据内容
            sb.append(", ID=").append(ID); // 追加CAN帧标识ID
            sb.append(", DataColor=").append(Integer.toHexString(DataColor)); // 追加颜色十六进制值
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                         SpiStruct                                        │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：SPI总线解码数据结构                                              │
     * │ 核心职责：存储SPI协议解码后的单帧数据，包含总线数据、停止位等状态            │
     * │ 数据流：  FPGA SPI解码 → C层填充 → SpiStruct → UI渲染/CSV导出             │
     * │ 使用场景：SPI总线解码时，每解码出一帧数据即创建一个SpiStruct实例             │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * SPI总线解码数据结构。
     * 存储单帧SPI解码结果，包括帧起止位置、数据类型（总线数据/最后总线数据/停止位）、
     * 解码数据内容及显示颜色。
     */
    public class SpiStruct implements ISerialBusCSV{
        /**
         * 停止位标识，值0x08；bit[3]=1时表示停止位，此时bit[2:0]无效。
         * 用于标识SPI传输的结束边界。
         */
        public static final int DataType_3bit_Stop=0x08; // 停止位，bit3=1

        /**
         * 总线数据，值0x01；DataType=01b，表示普通数据传输段。
         */
        public static final int DataType_2bit_BusData=0x01; // 总线数据段

        /**
         * 最后一个总线数据，值0x03；DataType=11b，表示当前帧的最后一个数据段。
         */
        public static final int DataType_2bit_LastBusData=0x03; // 最后一个总线数据段

        /**
         * 是否有开始帧标识。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * bit[3]=1：停止位（bit[2:0]无效）
         * bit[1:0]=01：总线数据
         * bit[1:0]=11：最后一个总线数据
         * 取值范围：0x01, 0x03, 0x08
         */
        public int DataType; // 数据类型，标识总线数据/最后数据/停止位

        /**
         * 解码数据内容，格式由SpiSettingStruct.encoding决定。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data; // 解码后的数据字符串

        /**
         * 显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 帧数据显示颜色（ARGB）

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段

        /**
         * 获取SPI数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,DataColor"
         */
        @Override
        public String toCsvHead() { // 返回SPI CSV导出表头
            return "BeginX,EndX,Data,DataColor"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将SPI解码数据转换为CSV数据行。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将SPI帧数据转换为CSV行
            if (Data==null) return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u
            e=e.replace("μ","u"); // 结束时间同样替换
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor); // 拼接CSV行
        }

        /**
         * 返回SpiStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("SpiStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加解码数据内容
            sb.append(", DataColor=").append(DataColor); // 追加颜色值（十进制）
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                         I2cStruct                                        │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：I2C总线解码数据结构                                              │
     * │ 核心职责：存储I2C协议解码后的单帧数据，包含读写地址、应答状态、              │
     * │          数据内容及停止位等状态                                            │
     * │ 数据流：  FPGA I2C解码 → C层填充 → I2cStruct → UI渲染/CSV导出             │
     * │ 使用场景：I2C总线解码时，每解码出一帧数据即创建一个I2cStruct实例             │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * I2C总线解码数据结构。
     * 存储单帧I2C解码结果，包括帧起止位置、数据类型（读/写/数据/应答/停止位）、
     * 解码数据内容、简写形式及显示颜色。DataType字段的bit3标识停止位，
     * bit2标识应答/无应答，bit0~1标识读/写/数据。
     */
    public class I2cStruct implements ISerialBusCSV{
        /**
         * 停止位标识，值0x08；bit[3]=1时表示停止位，bit[2:0]无效。
         * 标识I2C通信的STOP条件。
         */
        public static final int DataType_3bit_Stop=0x08; // 停止位，bit3=1

        /**
         * 应答（ACK），值0x04；bit[2]=1时表示从机应答，DataType=100b。
         */
        public static final int DataType_2bit_Response=0x04; // 应答ACK，bit2=1

        /**
         * 无应答（NACK），值0x00；bit[2]=0时表示从机无应答，DataType=000b。
         */
        public static final int DataType_2Bit_NoResponse=0x00; // 无应答NACK，bit2=0

        /**
         * 写操作，值0x01；bit[1:0]=01b，表示当前段为写地址或写数据。
         */
        public static final int DataType_1bit_Write=0x01; // 写操作，bit[1:0]=01

        /**
         * 读操作，值0x02；bit[1:0]=10b，表示当前段为读地址或读数据。
         */
        public static final int DataType_1bit_Read=0x02; // 读操作，bit[1:0]=10

        /**
         * 数据段，值0x03；bit[1:0]=11b，表示当前段为数据传输。
         */
        public static final int DataType_1bit_Data=0x03; // 数据段，bit[1:0]=11


        /**
         * 是否有开始帧标识。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * bit[3]：1=停止位
         * bit[2]：1=应答ACK，0=无应答NACK
         * bit[1:0]：01=写，10=读，11=数据
         * 取值范围：0x00 ~ 0x0F
         */
        public int DataType; // 数据类型，编码读写/数据/应答/停止位状态

        /**
         * 解码数据完整内容，格式由I2cSettingStruct.encoding决定。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data; // 解码后的完整数据字符串

        /**
         * 数据简写形式，用于UI列表页等空间有限场景的紧凑显示。
         * 取值范围：非null时为简写数据字符串，如地址+R/W标记
         */
        public String ShortData; // 数据简写形式，用于紧凑显示

        /**
         * 显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 帧数据显示颜色（ARGB）

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段

        /**
         * 获取I2C数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,DataColor"
         */
        @Override
        public String toCsvHead() { // 返回I2C CSV导出表头
            return "BeginX,EndX,Data,DataColor"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将I2C解码数据转换为CSV数据行。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将I2C帧数据转换为CSV行
            if (Data==null) return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u
            e=e.replace("μ","u"); // 结束时间同样替换
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor); // 拼接CSV行
        }

        /**
         * 返回I2cStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("I2cStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加完整数据内容
            sb.append(", ShortData='").append(ShortData).append('\''); // 追加简写数据内容
            sb.append(", DataColor=").append(DataColor); // 追加颜色值（十进制）
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      MilSTD1553bStruct                                   │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：MIL-STD-1553B总线解码数据结构                                    │
     * │ 核心职责：存储MIL-STD-1553B协议解码后的单帧数据，包含远程终端地址、          │
     * │          指令字、数据字、奇校验结果及曼切斯特码错误等状态                    │
     * │ 数据流：  FPGA 1553B解码 → C层填充 → MilSTD1553bStruct → UI渲染/CSV导出   │
     * │ 使用场景：MIL-STD-1553B总线解码时，每解码出一帧数据即创建一个实例           │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * MIL-STD-1553B总线解码数据结构。
     * 存储单帧1553B解码结果，包括帧起止位置、数据类型（远程终端地址/指令字/数据字/帧结束/错误）、
     * 解码数据内容及显示颜色。1553B协议采用曼切斯特编码，支持指令字、数据字和状态字三种字类型。
     */
    public class MilSTD1553bStruct implements ISerialBusCSV{
        /** 远程终端地址，值0x01；DataType=0001b，表示当前段为5位远程终端地址（RT Address） */
        public static final int DataType_RemoteAddr=0x01; // 远程终端地址段
        /** 指令/状态字第9~14位（6bit），值0x02；DataType=0010b，子地址/方式码字段 */
        public static final int DataType_Command6bit=0x02; // 指令字9-14位（子地址/方式码）
        /** 指令/状态字第15~19位（5bit），值0x03；DataType=0011b，字计数/方式码字段 */
        public static final int DataType_Command5bit=0x03; // 指令字15-19位（字计数/方式码）
        /** 数据字1，值0x04；DataType=0100b，表示第一个数据字（16bit） */
        public static final int DataType_Data1=0x04; // 数据字1
        /** 数据字2，值0x05；DataType=0101b，表示第二个数据字（16bit） */
        public static final int DataType_Data2=0x05; // 数据字2
        /** 帧结束，奇校验正确，值0x0F；DataType=1111b，表示帧正常结束且奇校验通过 */
        public static final int DataType_OverSuccess=0x0F; // 帧结束+奇校验正确
        /** 帧结束，奇校验错误，值0x07；DataType=0111b，表示帧结束但奇校验失败 */
        public static final int DataType_OverFailed=0x07; // 帧结束+奇校验错误
        /** 曼切斯特码错误，值0x06；DataType=0110b，表示检测到曼切斯特编码违规 */
        public static final int DataType_ManchesterCodeFailed=0x06; // 曼切斯特码错误

        /**
         * 是否有开始帧标识。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * 取值范围：0x01~0x07, 0x0F
         */
        public int DataType; // 数据类型，标识地址/指令字/数据字/帧结束/错误

        /**
         * 解码数据内容，格式由MilSID1553bSettingStruct.encoding决定。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data; // 解码后的数据字符串

        /**
         * 数据标识ID，用于帧序列中唯一标识数据段。
         * 取值范围：由FPGA解码逻辑分配
         */
        public int Id; // 数据段标识ID

        /**
         * 显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 帧数据显示颜色（ARGB）

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段

        /**
         * 获取MIL-STD-1553B数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,Data,DataColor"
         */
        @Override
        public String toCsvHead() { // 返回1553B CSV导出表头
            return "BeginX,EndX,Data,DataColor"; // 表头列：起始时间、结束时间、数据、颜色
        }

        /**
         * 将MIL-STD-1553B解码数据转换为CSV数据行。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将1553B帧数据转换为CSV行
            if (Data==null) return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u
            e=e.replace("μ","u"); // 结束时间同样替换
            return b+","+e+","+","+Data+",0x"+Integer.toHexString(DataColor); // 拼接CSV行（注意多了一个逗号，为预留列位）
        }

        /**
         * 返回MilSTD1553bStruct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("MilSTD1553bStruct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Data='").append(Data).append('\''); // 追加解码数据内容
            sb.append(", Id=").append(Id); // 追加数据标识ID
            sb.append(", DataColor=").append(DataColor); // 追加颜色值（十进制）
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                       Arinc429Struct                                     │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：ARINC429总线解码数据结构                                         │
     * │ 核心职责：存储ARINC429协议解码后的单帧数据，包含Label、SDI、SSM、           │
     * │          数据内容及校验状态                                                │
     * │ 数据流：  FPGA ARINC429解码 → C层填充 → Arinc429Struct → UI渲染/CSV导出  │
     * │ 使用场景：ARINC429总线解码时，每解码出一帧数据即创建一个Arinc429Struct实例  │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * ARINC429总线解码数据结构。
     * 存储单帧ARINC429解码结果，包括帧起止位置、数据类型（Label/数据/错误）、
     * SDI（源/目标标识）、SSM（符号/状态矩阵）、数据内容及各部分独立的显示颜色。
     * ARINC429为航空电子标准总线协议，32位字长，包含Label(8bit)、SDI(2bit)、
     * Data(19bit)、SSM(2bit)和奇校验位(1bit)。
     */
    public class Arinc429Struct implements ISerialBusCSV{
        /**
         * 显示模式1：SDI值 + DATA值 + SSM值，值0x02。
         * 完整显示ARINC429字的所有字段。
         */
        public static final int Arinc429Type_1=0x02; // 显示模式1：SDI+DATA+SSM全部显示

        /**
         * 显示模式2：DATA值 + SSM值，值0x01。
         * 不显示SDI字段，仅显示数据和状态。
         */
        public static final int Arinc429Type_2=0x01; // 显示模式2：DATA+SSM（不含SDI）

        /**
         * 显示模式3：仅DATA值，值0x00。
         * 最简显示模式，仅显示数据字段。
         */
        public static final int Arinc429Type_3=0x00; // 显示模式3：仅DATA

        /** LABEL字段，值0x01；DataType=0001b，表示当前段为8位Label标识符 */
        public static final int DataType_Label=0x01; // Label段
        /** 数据1，值0x02；DataType=0010b，表示第一个数据段 */
        public static final int DataType_Data1=0x02; // 数据段1
        /** 数据2，值0x03；DataType=0011b，表示第二个数据段 */
        public static final int DataType_Data2=0x03; // 数据段2
        /** 数据3，值0x04；DataType=0100b，表示第三个数据段 */
        public static final int DataType_Data3=0x04; // 数据段3
        /** 错误帧，值0x05；DataType=0101b，表示检测到ARINC429协议错误 */
        public static final int DataType_Error=0x05; // 错误帧

        /**
         * 是否有开始帧标识。
         * 取值范围：true（起始帧）/ false（非起始帧）
         */
        public boolean isBeginFrame; // 是否为起始帧

        /**
         * 帧起始X轴位置，单位：像素。
         * 取值范围：0 ~ 屏幕宽度像素值
         */
        public int BeginX; // 帧起始X坐标（像素）

        /**
         * 帧结束X轴位置，单位：像素。
         * 取值范围：BeginX ~ 屏幕宽度像素值
         */
        public int EndX; // 帧结束X坐标（像素）

        /**
         * 数据类型标识。
         * 取值范围：0x01~0x05
         */
        public int DataType; // 数据类型，标识Label/数据段/错误帧

        /**
         * 数据ID，用于帧序列中唯一标识数据段或数据组合。
         * 取值范围：由FPGA解码逻辑分配
         */
        public int Id; // 数据标识ID，用于帧内数据段关联

        /**
         * SDI（源/目标标识）字符串，2位二进制值。
         * 标识数据的源设备或目标设备。
         * 取值范围："00", "01", "10", "11"
         */
        public String SDI; // SDI源/目标标识字符串

        /**
         * SSM（符号/状态矩阵）字符串，2位二进制值。
         * 标识数据的符号和状态（正常/故障/测试等）。
         * 取值范围："00"(正常), "01"(无计算数据), "10"(功能测试), "11"(故障)
         */
        public String SSM; // SSM符号/状态矩阵字符串

        /**
         * 解码数据内容，19位数据字段的字符串表示。
         * 取值范围：非null时为有效数据字符串
         */
        public String Data; // 解码后的数据字符串

        /**
         * SDI显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int SDIColor; // SDI字段显示颜色（ARGB）

        /**
         * SSM显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int SSMColor; // SSM字段显示颜色（ARGB）

        /**
         * 数据显示颜色值，ARGB格式。
         * 取值范围：0x00000000 ~ 0xFFFFFFFF（ARGB）
         */
        public int DataColor; // 数据字段显示颜色（ARGB）

        /**
         * 数据帧结束标志，标识当前ARINC429字的所有段是否已接收完毕。
         * 取值范围：true（帧数据接收完毕）/ false（帧数据尚未接收完毕）
         */
        private boolean DataFrameEnd=false; // 帧结束标志，默认false

        /**
         * 判断ARINC429数据帧是否已接收完毕。
         *
         * @return true表示帧数据全部接收完毕，false表示尚有段未接收
         */
        public boolean isDataFrameEnd() { // 获取帧结束标志
            return DataFrameEnd; // 返回帧结束状态
        }

        /**
         * 设置ARINC429数据帧结束标志。
         *
         * @param dataFrameEnd true表示帧数据全部接收完毕，false表示尚有段未接收
         */
        public void setDataFrameEnd(boolean dataFrameEnd) { // 设置帧结束标志
            DataFrameEnd = dataFrameEnd; // 更新帧结束状态
        }

        /**
         * 预留字段，当前未使用。
         * 取值范围：0 ~ 32767
         */
        public short Reserve; // 预留字段

        /**
         * 获取ARINC429数据CSV表头。
         *
         * @return CSV表头字符串"BeginX,EndX,SDI,Data,SSM,DataColor"
         */
        @Override
        public  String toCsvHead() { // 返回ARINC429 CSV导出表头
            return "BeginX,EndX,SDI,Data,SSM,DataColor"; // 表头列：起始时间、结束时间、SDI、数据、SSM、颜色
        }

        /**
         * 将ARINC429解码数据转换为CSV数据行。
         *
         * @return CSV数据行字符串；若Data为null则返回空字符串""
         */
        @Override
        public String toCSV() { // 将ARINC429帧数据转换为CSV行
            if (Data==null) return ""; // 数据为空时返回空字符串
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix()); // 起始位置→时间字符串
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix()); // 结束位置→时间字符串
            b=b.replace("μ","u"); // 替换μ为u
            e=e.replace("μ","u"); // 结束时间同样替换
            return b+","+e+","+SDI+","+Data+","+SSM+",0x"+Integer.toHexString(DataColor); // 拼接CSV行：时间,时间,SDI,数据,SSM,颜色
        }

        /**
         * 返回Arinc429Struct的字符串表示，用于调试日志输出。
         *
         * @return 包含所有字段的字符串
         */
        @Override
        public String toString() { // 调试用toString方法
            final StringBuilder sb = new StringBuilder("Arinc429Struct{"); // 构建字符串，以类名开头
            sb.append("isBeginFrame=").append(isBeginFrame); // 追加起始帧标识
            sb.append(", BeginX=").append(BeginX); // 追加帧起始X坐标
            sb.append(", EndX=").append(EndX); // 追加帧结束X坐标
            sb.append(", DataType=").append(DataType); // 追加数据类型
            sb.append(", Id=").append(Id); // 追加数据标识ID
            sb.append(", SDI='").append(SDI).append('\''); // 追加SDI字段
            sb.append(", SSM='").append(SSM).append('\''); // 追加SSM字段
            sb.append(", Data='").append(Data).append('\''); // 追加数据内容
            sb.append(", SDIColor=").append(SDIColor); // 追加SDI颜色值
            sb.append(", SSMColor=").append(SSMColor); // 追加SSM颜色值
            sb.append(", DataColor=").append(DataColor); // 追加数据颜色值
            sb.append(", DataFrameEnd=").append(DataFrameEnd); // 追加帧结束标志
            sb.append(", Reserve=").append(Reserve); // 追加预留字段
            sb.append('}'); // 结束大括号
            return sb.toString(); // 返回完整字符串
        }
    }


    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      UartSettingStruct                                   │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：UART串口配置参数结构                                            │
     * │ 核心职责：存储UART解码的用户配置参数，包括数据位长度、使能开关、编码方式     │
     * │ 数据流：  UI设置界面 → UartSettingStruct → C层/FPGA解码参数               │
     * │ 使用场景：用户在UART解码设置界面修改参数后，参数存入此结构供解码器使用       │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * UART串口配置参数结构。
     * 存储用户在界面上设置的UART解码参数，包括数据位长度（7/8/9位）、
     * 使能开关（是否启用UART解码）和编码方式（Hex/Binary/ASCII）。
     */
    public class UartSettingStruct{
        /**
         * UART数据位长度，由界面传入。
         * 取值范围：7, 8, 9（默认8）
         */
        public int uartLength = 8; // 数据位长度，默认8位

        //public long time = 40000000l; // 时间参数（已弃用），原由界面传入，tem=时间/50格

        /**
         * UART解码使能开关，由界面传入。
         * 取值范围：true（启用UART解码）/ false（禁用，默认false）
         */
        public  boolean checked=false; // 解码使能开关，默认关闭

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Hex(0x16), Binary(0x02), ASCII(0xAC)，默认Hex
         */
        public  int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

        /**
         * 设置UART数据位长度。
         *
         * @param uartLength 数据位长度，取值：7, 8, 9
         */
        public void setUartLength(int uartLength) { // 设置数据位长度
            this.uartLength = uartLength; // 更新数据位长度
        }

        /**
         * 设置UART解码使能开关。
         *
         * @param checked true启用UART解码，false禁用
         */
         public void setChecked(boolean checked) { // 设置使能开关
            this.checked = checked; // 更新使能状态
        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         *
         * @param encoding 编码方式索引，0=Hex，1=Binary，2=ASCII
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Hex;break; // 索引0 → 十六进制编码
                case 1:this.encoding=ICharacterEncoding.Binary;break; // 索引1 → 二进制编码
                case 2:this.encoding=ICharacterEncoding.ASCII;break; // 索引2 → ASCII编码
            }

        }

        /**
         * 获取UART数据位长度。
         *
         * @return 数据位长度，取值：7, 8, 9
         */
        public int getUartLength() { // 获取数据位长度
            return uartLength; // 返回当前数据位长度
        }

        /**
         * 获取UART解码使能状态。
         *
         * @return true表示已启用UART解码，false表示已禁用
         */
        public boolean isChecked() { // 获取使能状态
            return checked; // 返回当前使能状态
        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Hex/Binary/ASCII
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      LinSettingStruct                                    │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：LIN总线配置参数结构                                             │
     * │ 核心职责：存储LIN解码的用户配置参数，包括编码方式                           │
     * │ 数据流：  UI设置界面 → LinSettingStruct → C层/FPGA解码参数                │
     * │ 使用场景：用户在LIN解码设置界面修改编码方式后，参数存入此结构供解码器使用    │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * LIN总线配置参数结构。
     * 存储用户在界面上设置的LIN解码参数，目前仅包含编码方式。
     */
    public class LinSettingStruct{
        //long time = 100000000l; // 时间参数（已弃用）

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Hex(0x16), Binary(0x02), ASCII(0xAC)，默认Hex
         */
        int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

        //public void setTime(long time) { // 设置时间参数（已弃用）
        //    this.time = time; // 更新时间参数
       // }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         *
         * @param encoding 编码方式索引，0=Hex，1=Binary，2=ASCII
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Hex;break; // 索引0 → 十六进制编码
                case 1:this.encoding=ICharacterEncoding.Binary;break; // 索引1 → 二进制编码
                case 2:this.encoding=ICharacterEncoding.ASCII;break; // 索引2 → ASCII编码
            }
        }

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Hex/Binary/ASCII
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      CanSettingStruct                                    │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：CAN总线配置参数结构                                             │
     * │ 核心职责：存储CAN解码的用户配置参数，包括编码方式                           │
     * │ 数据流：  UI设置界面 → CanSettingStruct → C层/FPGA解码参数                │
     * │ 使用场景：用户在CAN解码设置界面修改编码方式后，参数存入此结构供解码器使用    │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * CAN总线配置参数结构。
     * 存储用户在界面上设置的CAN解码参数，目前仅包含编码方式。
     */
    public class CanSettingStruct{
//        long time = 1000000l; // 时间转像素参数（已弃用）

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Hex(0x16), Binary(0x02), ASCII(0xAC)，默认Hex
         */
        int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

//        public void setTime(long time) { // 设置时间参数（已弃用）
//            this.time = time; // 更新时间参数
//        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         *
         * @param encoding 编码方式索引，0=Hex，1=Binary，2=ASCII
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Hex;break; // 索引0 → 十六进制编码
                case 1:this.encoding=ICharacterEncoding.Binary;break; // 索引1 → 二进制编码
                case 2:this.encoding=ICharacterEncoding.ASCII;break; // 索引2 → ASCII编码
            }
        }

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Hex/Binary/ASCII
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      SpiSettingStruct                                    │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：SPI总线配置参数结构                                             │
     * │ 核心职责：存储SPI解码的用户配置参数，包括编码方式和数据位长度               │
     * │ 数据流：  UI设置界面 → SpiSettingStruct → C层/FPGA解码参数                │
     * │ 使用场景：用户在SPI解码设置界面修改编码方式和位长后，参数存入此结构         │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * SPI总线配置参数结构。
     * 存储用户在界面上设置的SPI解码参数，包括编码方式和数据位长度（4/8/16/24/32位）。
     */
    public class SpiSettingStruct{
        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Hex(0x16), Binary(0x02), ASCII(0xAC)，默认Hex
         */
        int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

//        long time = 400000l; // 时间转像素参数（已弃用）

        /**
         * SPI数据位长度，决定每次传输的数据字长。
         * 取值范围：4, 8, 16, 24, 32（默认8）
         */
        int dataBit = 8; // 数据位长度，默认8位

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Hex/Binary/ASCII
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         *
         * @param encoding 编码方式索引，0=Hex，1=Binary，2=ASCII
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Hex;break; // 索引0 → 十六进制编码
                case 1:this.encoding=ICharacterEncoding.Binary;break; // 索引1 → 二进制编码
                case 2:this.encoding=ICharacterEncoding.ASCII;break; // 索引2 → ASCII编码
            }
        }

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }

//        public void setTime(long time) { // 设置时间参数（已弃用）
//            this.time = time; // 更新时间参数
//        }

        /**
         * 获取SPI数据位长度。
         *
         * @return 数据位长度，取值：4, 8, 16, 24, 32
         */
        public int getDataBit() { // 获取数据位长度
            return dataBit; // 返回当前数据位长度
        }

        /**
         * 设置SPI数据位长度，将界面索引值转换为实际位长。
         *
         * @param dataBit 位长索引，0=4位，1=8位，2=16位，3=24位，4=32位
         */
        public void setDataBit(int dataBit) { // 设置数据位长度（界面索引→实际位长）
            switch (dataBit){ // 根据界面传入的索引值选择位长
                case 0:this.dataBit = 4;  break; // 索引0 → 4位数据字长
                case 1:this.dataBit = 8; break; // 索引1 → 8位数据字长
                case 2:this.dataBit = 16; break; // 索引2 → 16位数据字长
                case 3:this.dataBit = 24; break; // 索引3 → 24位数据字长
                case 4:this.dataBit = 32; break; // 索引4 → 32位数据字长
            }

        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                      I2cSettingStruct                                    │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：I2C总线配置参数结构                                             │
     * │ 核心职责：存储I2C解码的用户配置参数，包括编码方式                           │
     * │ 数据流：  UI设置界面 → I2cSettingStruct → C层/FPGA解码参数                │
     * │ 使用场景：用户在I2C解码设置界面修改编码方式后，参数存入此结构供解码器使用    │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * I2C总线配置参数结构。
     * 存储用户在界面上设置的I2C解码参数，目前仅包含编码方式。
     */
    public class I2cSettingStruct{
//        long time = 4000000l; // 时间转像素参数（已弃用）

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Hex(0x16), Binary(0x02), ASCII(0xAC)，默认Hex
         */
        int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }
//
//        public void setTime(long time) { // 设置时间参数（已弃用）
//            this.time = time; // 更新时间参数
//        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Hex/Binary/ASCII
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         *
         * @param encoding 编码方式索引，0=Hex，1=Binary，2=ASCII
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Hex;break; // 索引0 → 十六进制编码
                case 1:this.encoding=ICharacterEncoding.Binary;break; // 索引1 → 二进制编码
                case 2:this.encoding=ICharacterEncoding.ASCII;break; // 索引2 → ASCII编码
            }
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                   MilSID1553bSettingStruct                               │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：MIL-STD-1553B总线配置参数结构                                   │
     * │ 核心职责：存储1553B解码的用户配置参数，包括编码方式                         │
     * │ 数据流：  UI设置界面 → MilSID1553bSettingStruct → C层/FPGA解码参数        │
     * │ 使用场景：用户在1553B解码设置界面修改编码方式后，参数存入此结构             │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * MIL-STD-1553B总线配置参数结构。
     * 存储用户在界面上设置的1553B解码参数。
     * 注意：1553B仅支持Binary和Hex两种编码方式（不支持ASCII），
     * 且默认编码为Binary（与其他总线默认Hex不同）。
     */
    public class MilSID1553bSettingStruct{
//        long time = 2000000l; // 时间转像素参数（已弃用）

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Binary(0x02), Hex(0x16)，默认Binary
         * 注意：1553B不支持ASCII编码，仅支持Binary和Hex
         */
        int encoding=ICharacterEncoding.Binary; // 编码方式，默认二进制（1553B特有默认值）

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }
//
//        public void setTime(long time) { // 设置时间参数（已弃用）
//            this.time = time; // 更新时间参数
//        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Binary/Hex
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         * 注意：1553B仅支持Binary和Hex两种编码，索引映射与其他总线不同。
         *
         * @param encoding 编码方式索引，0=Binary，1=Hex
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Binary;break; // 索引0 → 二进制编码（1553B默认）
                case 1:this.encoding=ICharacterEncoding.Hex;break; // 索引1 → 十六进制编码
            }
        }
    }

    /**
     * ┌──────────────────────────────────────────────────────────────────────────┐
     * │                     Arinc429SettingStruct                                │
     * ├──────────────────────────────────────────────────────────────────────────┤
     * │ 模块定位：ARINC429总线配置参数结构                                        │
     * │ 核心职责：存储ARINC429解码的用户配置参数，包括编码方式                      │
     * │ 数据流：  UI设置界面 → Arinc429SettingStruct → C层/FPGA解码参数           │
     * │ 使用场景：用户在ARINC429解码设置界面修改编码方式后，参数存入此结构          │
     * └──────────────────────────────────────────────────────────────────────────┘
     *
     * ARINC429总线配置参数结构。
     * 存储用户在界面上设置的ARINC429解码参数。
     * 注意：ARINC429仅支持Binary和Hex两种编码方式（不支持ASCII），
     * 且默认编码为Hex，索引映射与1553B相反。
     */
    public class Arinc429SettingStruct{
//        long time = 100000000l; // 时间转像素参数（已弃用）

        /**
         * 数据编码方式，由界面传入。
         * 取值范围：ICharacterEncoding.Binary(0x02), Hex(0x16)，默认Hex
         * 注意：ARINC429不支持ASCII编码，仅支持Binary和Hex
         */
        int encoding=ICharacterEncoding.Hex; // 编码方式，默认十六进制

//        public long getTime() { // 获取时间参数（已弃用）
//            return time; // 返回时间参数
//        }
//
//        public void setTime(long time) { // 设置时间参数（已弃用）
//            this.time = time; // 更新时间参数
//        }

        /**
         * 获取数据编码方式。
         *
         * @return 编码方式常量，取值：ICharacterEncoding.Binary/Hex
         */
        public int getEncoding() { // 获取编码方式
            return encoding; // 返回当前编码方式常量
        }

        /**
         * 设置数据编码方式，将界面索引值转换为ICharacterEncoding常量。
         * 注意：ARINC429仅支持Binary和Hex两种编码，索引映射与1553B相反。
         *
         * @param encoding 编码方式索引，0=Binary，1=Hex
         */
        public void setEncoding(int encoding) { // 设置编码方式（界面索引→编码常量）
            switch (encoding){ // 根据界面传入的索引值选择编码方式
                case 0:this.encoding=ICharacterEncoding.Binary;break; // 索引0 → 二进制编码
                case 1:this.encoding=ICharacterEncoding.Hex;break; // 索引1 → 十六进制编码
            }
        }
    }

}
