package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           串行总线文字显示接口                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                │
 * │   串行总线数据列表显示模块的核心接口定义，为各种串行协议的适配器提供统一的常量定义  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                │
 * │   1. 定义串行通道类型常量（S1/S2/S3/S4）                                    │
 * │   2. 定义各种串行协议数据显示的最大字符数限制                                │
 * │   3. 为不同协议的列表适配器提供统一的显示参数                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                │
 * │   作为常量定义接口，被所有串行协议适配器类实现或引用：                         │
 * │   - MainAdapterCenterSerialsWordCan（CAN总线适配器）                       │
 * │   - MainAdapterCenterSerialsWordI2c（I2C总线适配器）                        │
 * │   - MainAdapterCenterSerialsWordLin（LIN总线适配器）                        │
 * │   - MainAdapterCenterSerialsWordSpi（SPI总线适配器）                        │
 * │   - MainAdapterCenterSerialsWordM429（ARINC429总线适配器）                  │
 * │   - MainAdapterCenterSerialsWordM1553b（MIL-STD-1553B总线适配器）           │
 * │   - MainAdapterCenterSerialsWordUart（UART总线适配器）                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                │
 * │   TChan（通道枚举）→ ISerialsWord（常量定义）→ 各协议适配器（使用常量）         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                │
 * │   依赖：TChan（通道类型枚举类）                                             │
 * │   被依赖：所有串行协议适配器类                                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用场景】                                                                │
 * │   在RecyclerView列表中显示串行总线解码数据时，用于控制每行显示的字符数，        │
 * │   确保数据显示不溢出、换行显示合理                                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public interface ISerialsWord {
    /**
     * 串行通道类型常量：S1通道
     */
    public static final int TYPE_S1 = TChan.S1; // S1通道类型常量，对应TChan.S1枚举值

    /**
     * 串行通道类型常量：S2通道
     */
    public static final int TYPE_S2 = TChan.S2; // S2通道类型常量，对应TChan.S2枚举值

    /**
     * 串行通道类型常量：S3通道
     */
    public static final int TYPE_S3 = TChan.S3; // S3通道类型常量，对应TChan.S3枚举值

    /**
     * 串行通道类型常量：S4通道
     */
    public static final int TYPE_S4 = TChan.S4; // S4通道类型常量，对应TChan.S4枚举值

    /**
     * 串行通道类型常量：S1+S2+S3+S4组合（所有串行通道）
     */
    public static final int TYPE_S12 = TChan.S1 + TChan.S2 + TChan.S3 + TChan.S4; // 所有串行通道的组合值

    /**
     * lin的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_LIN = 42; // LIN协议每行最多显示42个字符，避免数据溢出

    /**
     * can的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_CAN = 48; // CAN协议每行最多显示48个字符，适配CAN帧数据长度

    /**
     * spi的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_SPI = 130; // SPI协议每行最多显示130个字符，SPI传输数据量较大

    /**
     * i2c的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_I2C = 42; // I2C协议每行最多显示42个字符，适配I2C数据格式

    /**
     * 429的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_429 = 35; // ARINC429协议每行最多显示35个字符，适配429字格式

    /**
     * 1553b的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_1553B = 35; // MIL-STD-1553B协议每行最多显示35个字符，适配1553B字格式
}