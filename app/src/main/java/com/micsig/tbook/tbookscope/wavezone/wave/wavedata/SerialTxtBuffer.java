package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包

import java.nio.ByteBuffer; // NIO字节缓冲区，FPGA数据载体
import java.util.HashMap; // 哈希表，用于通道选中状态映射
import java.util.Iterator; // 迭代器，用于遍历队列获取最后元素
import java.util.concurrent.LinkedBlockingQueue; // 线程安全的阻塞队列，缓冲区核心数据结构

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       SerialTxtBuffer                                       ║
 * ║                       串口文本缓冲区管理器                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║【模块定位】wavedata包缓冲区层，位于解析器与UI显示之间的数据存储层              ║
 * ║【核心职责】为7种总线协议维护屏幕缓冲区和总缓冲区，管理数据生命周期             ║
 * ║【架构设计】双缓冲区架构（Screen+Total）+ S1S2混合缓冲区 + 溢出淘汰策略       ║
 * ║【数据流  】SerialBusTxtStructParse解析→put()→Screen/Total/S1S2队列→UI读取   ║
 * ║【依赖关系】                                                                ║
 * ║   ├─ SerialBusTxtStruct：7种总线协议文本结构体定义                           ║
 * ║   ├─ SerialBusStruct：总线类型常量定义                                      ║
 * ║   ├─ SerialsTxtUtils：S1S2混合通道选中状态管理                               ║
 * ║   └─ TChan：通道定义（S1-S4）                                              ║
 * ║【使用场景】                                                                ║
 * ║   1. 解析器调用put()写入解析后的帧结构体                                    ║
 * ║   2. UI层通过getQueue方法读取缓冲区数据进行显示                             ║
 * ║   3. 停止运行时调用clearAll()清空所有缓冲区                                 ║
 * ║   4. 开启/关闭S1S2混合模式时调用setOpenS1S2()                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SerialTxtBuffer {
    /** 日志标签，用于Logcat过滤，值："SerialTxtBuffer" */
    private static final String TAG = "SerialTxtBuffer";

    /** 全局同步锁，保证多线程下缓冲区操作的原子性 */
    private static final Object lock = new Object();

    /**
     * 是否开启S1S2混合显示模式
     * true=开启混合模式，数据同时写入S1S2混合缓冲区
     * false=关闭混合模式，S1S2混合缓冲区被清空
     */
    private static boolean openS1AndS2 = false;

    //region 缓冲区链表与方法
    /** FPGA原始数据ByteBuffer队列，由putBytesToQueue写入，解析线程从中poll取出 */
    private LinkedBlockingQueue<ByteBuffer> buffer = new LinkedBlockingQueue<>();

    /**
     * 将FPGA原始ByteBuffer数据复制后放入队列
     * 复制原因：原始ByteBuffer可能被FPGA复用，必须深拷贝
     *
     * @param bytes FPGA原始数据缓冲区
     * @throws InterruptedException 队列满时等待被中断
     */
    public void putBytesToQueue(ByteBuffer bytes) throws InterruptedException {
        ByteBuffer oBytes = ByteBuffer.allocate(bytes.limit()); // 分配与原始数据等大的新缓冲区
        bytes.position(0); // 重置原始缓冲区读取位置到0
        oBytes.put(bytes); // 将原始数据复制到新缓冲区
        buffer.add(oBytes); // 将复制后的缓冲区加入队列尾部
    }

    /**
     * 获取FPGA原始数据ByteBuffer队列引用
     * @return ByteBuffer阻塞队列，供解析线程poll取出
     */
    public LinkedBlockingQueue<ByteBuffer> getBuffer() {
        return buffer;
    }
    //endregion

    //region 文本解析属性

    /** UART总接收数据帧数，取值范围[0, Long.MAX_VALUE] */
    private long uartTotalData = 0;
    /** UART接收错误数据帧数，取值范围[0, uartTotalData] */
    private long uartErrorData = 0;

    /**
     * CAN 总帧数，取值范围[0, Long.MAX_VALUE]
     */
    private long canTotalFrame = 0;
    /**
     * CAN 空帧数（spec错误帧），取值范围[0, canTotalFrame]
     */
    private long canSpaceFrame = 0;
    /**
     * CAN 错误帧数，取值范围[0, canTotalFrame]
     */
    private long canErrorFrame = 0;

    /**
     * ARINC429 总帧数，取值范围[0, Long.MAX_VALUE]
     */
    private long arinc429TotalFrame = 0;
    /**
     * ARINC429 错误帧数，取值范围[0, arinc429TotalFrame]
     */
    private long arinc429ErrorFrame = 0;

    /** 上一次FPGA时间戳（15位），取值范围[0, 32767]，用于时间累积计算 */
    private int armLastTime = 0;
    /** 累积总时间，取值范围[0, 599999999]（99m59s999ms990us） */
    private long totalTime = 0;

    /** UART最后一帧引用，用于续写判断（跨ByteBuffer边界时帧未结束可续写） */
    private SerialBusTxtStruct.UartStruct lastUartNode = null;
    /** ARINC429最后一帧引用 */
    private SerialBusTxtStruct.Arinc429Struct last429Node = null;
    /** CAN最后一帧引用 */
    private SerialBusTxtStruct.CanStruct lastCanNode = null;
    /** SPI最后一帧引用 */
    private SerialBusTxtStruct.SpiStruct lastSpiNode = null;
    /** I2C最后一帧引用 */
    private SerialBusTxtStruct.I2cStruct lastI2cNode = null;
    /** LIN最后一帧引用 */
    private SerialBusTxtStruct.LinStruct lastLinNode = null;
    /** MIL-STD-1553B最后一帧引用 */
    private SerialBusTxtStruct.MilSTD1553bStruct last1553bNode = null;

    // ── 屏幕缓冲区：仅保留屏幕可见范围内的最新帧，溢出时淘汰最旧帧 ──
    /** UART屏幕缓冲区，容量=uartBufferScreenSize */
    private LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListScreen = null;
    /** ARINC429屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListScreen = null;
    /** CAN屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListScreen = null;
    /** I2C屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListScreen = null;
    /** LIN屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListScreen = null;
    /** MIL-STD-1553B屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bListScreen = null;
    /** SPI屏幕缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListScreen = null;

    // ── 总缓冲区：保留所有历史帧，溢出时淘汰最旧帧 ──
    /** UART总缓冲区，容量=uartBufferSize */
    private LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = null;
    /** ARINC429总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListTotal = null;
    /** CAN总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = null;
    /** I2C总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = null;
    /** LIN总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = null;
    /** MIL-STD-1553B总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bListTotal = null;
    /** SPI总缓冲区 */
    private LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListTotal = null;

    // ── S1S2混合屏幕缓冲区：当开启S1S2混合模式时，选中通道的数据同时写入 ──
    /** UART S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartS1S2ListScreen = null;
    /** ARINC429 S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429S1S2ListScreen = null;
    /** CAN S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canS1S2ListScreen = null;
    /** I2C S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cS1S2ListScreen = null;
    /** LIN S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linS1S2ListScreen = null;
    /** MIL-STD-1553B S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bS1S2ListScreen = null;
    /** SPI S1S2混合屏幕缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiS1S2ListScreen = null;

    // ── S1S2混合总缓冲区 ──
    /** UART S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartS1S2ListTotal = null;
    /** ARINC429 S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429S1S2ListTotal = null;
    /** CAN S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canS1S2ListTotal = null;
    /** I2C S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cS1S2ListTotal = null;
    /** LIN S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linS1S2ListTotal = null;
    /** MIL-STD-1553B S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bS1S2ListTotal = null;
    /** SPI S1S2混合总缓冲区 */
    private static LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiS1S2ListTotal = null;

    /** 基础缓冲区大小常量，值=4096 */
    private static int BUFFER_SIZE = 4096;

    // ── 屏幕缓冲区容量限制 ──
    /** UART屏幕缓冲区最大容量，值=2814（屏幕可显示的UART帧行数） */
    private int uartBufferScreenSize = 2814, // UART屏幕容量，远大于其他协议（因为UART每字节一帧）
            /** ARINC429屏幕缓冲区最大容量，值=20 */
            arinc429BufferScreenSize = 20,
            /** CAN屏幕缓冲区最大容量，值=20 */
            canBufferScreenSize = 20,
            /** I2C屏幕缓冲区最大容量，值=20 */
            i2cBufferScreenSize = 20,
            /** LIN屏幕缓冲区最大容量，值=20 */
            lineBufferScreenSize = 20,
            /** MIL-STD-1553B屏幕缓冲区最大容量，值=20 */
            milstd1553bBufferScreenSize = 20,
            /** SPI屏幕缓冲区最大容量，值=20 */
            spiBufferScreenSize = 20;

    // ── 屏幕缓冲区当前元素计数 ──
    /** UART屏幕缓冲区当前帧数，取值范围[0, uartBufferScreenSize] */
    private int uartCurrScreenSize = 0,
            /** ARINC429屏幕缓冲区当前帧数 */
            arinc429CurrScreenSize = 0,
            /** CAN屏幕缓冲区当前帧数 */
            canCurrScreenSize = 0,
            /** I2C屏幕缓冲区当前帧数 */
            i2cCurrScreenSize = 0,
            /** LIN屏幕缓冲区当前帧数 */
            linCurrScreenSize = 0,
            /** MIL-STD-1553B屏幕缓冲区当前帧数 */
            milstd1553bCurrScreenSize = 0,
            /** SPI屏幕缓冲区当前帧数 */
            spiCurrScreenSize = 0;

    // ── 总缓冲区容量限制 ──
    /** UART总缓冲区最大容量，值=BUFFER_SIZE*40=163840 */
    private int uartBufferSize = BUFFER_SIZE * 40,
            /** ARINC429总缓冲区最大容量 */
            arinc429BufferSize = BUFFER_SIZE * 40,
            /** CAN总缓冲区最大容量 */
            canBufferSize = BUFFER_SIZE * 40,
            /** I2C总缓冲区最大容量 */
            i2cBufferSize = BUFFER_SIZE * 40,
            /** LIN总缓冲区最大容量 */
            lineBufferSize = BUFFER_SIZE * 40,
            /** MIL-STD-1553B总缓冲区最大容量 */
            milstd1553bBufferSize = BUFFER_SIZE * 40,
            /** SPI总缓冲区最大容量 */
            spiBufferSize = BUFFER_SIZE * 40;

    // ── 总缓冲区当前元素计数 ──
    /** UART总缓冲区当前帧数，取值范围[0, uartBufferSize] */
    private int uartCurrSize = 0,
            /** ARINC429总缓冲区当前帧数 */
            arinc429CurrSize = 0,
            /** CAN总缓冲区当前帧数 */
            canCurrSize = 0,
            /** I2C总缓冲区当前帧数 */
            i2cCurrSize = 0,
            /** LIN总缓冲区当前帧数 */
            linCurrSize = 0,
            /** MIL-STD-1553B总缓冲区当前帧数 */
            milstd1553bCurrSize = 0,
            /** SPI总缓冲区当前帧数 */
            spiCurrSize = 0;

    // ── S1S2混合缓冲区当前元素计数（static，所有实例共享） ──
    /** UART S1S2混合屏幕缓冲区当前帧数 */
    private static int uartS1S2CurrScreenSize = 0,
            /** ARINC429 S1S2混合屏幕缓冲区当前帧数 */
            arinc429S1S2CurrScreenSize = 0,
            /** CAN S1S2混合屏幕缓冲区当前帧数 */
            canS1S2CurrScreenSize = 0,
            /** I2C S1S2混合屏幕缓冲区当前帧数 */
            i2cS1S2CurrScreenSize = 0,
            /** LIN S1S2混合屏幕缓冲区当前帧数 */
            linS1S2CurrScreenSize = 0,
            /** MIL-STD-1553B S1S2混合屏幕缓冲区当前帧数 */
            milstd1553bS1S2CurrScreenSize = 0,
            /** SPI S1S2混合屏幕缓冲区当前帧数 */
            spiS1S2CurrScreenSize = 0;

    /** UART S1S2混合总缓冲区当前帧数 */
    private static int uartS1S2CurrSize = 0,
            /** ARINC429 S1S2混合总缓冲区当前帧数 */
            arinc429S1S2CurrSize = 0,
            /** CAN S1S2混合总缓冲区当前帧数 */
            canS1S2CurrSize = 0,
            /** I2C S1S2混合总缓冲区当前帧数 */
            i2cS1S2CurrSize = 0,
            /** LIN S1S2混合总缓冲区当前帧数 */
            linS1S2CurrSize = 0,
            /** MIL-STD-1553B S1S2混合总缓冲区当前帧数 */
            milstd1553bS1S2CurrSize = 0,
            /** SPI S1S2混合总缓冲区当前帧数 */
            spiS1S2CurrSize = 0;
    //endregion

    /**
     * 默认构造函数，初始化所有缓冲区队列
     * 为7种协议各创建4组队列：Screen、Total、S1S2Screen、S1S2Total
     */
    public SerialTxtBuffer() {
        this.uartBufferSize = BUFFER_SIZE * 40; // UART总缓冲区容量=163840
        this.arinc429BufferSize = BUFFER_SIZE * 40; // ARINC429总缓冲区容量
        this.canBufferSize = BUFFER_SIZE * 40; // CAN总缓冲区容量
        this.i2cBufferSize = BUFFER_SIZE * 40; // I2C总缓冲区容量
        this.lineBufferSize = BUFFER_SIZE * 40; // LIN总缓冲区容量
        this.milstd1553bBufferSize = BUFFER_SIZE * 40; // 1553B总缓冲区容量
        this.spiBufferSize = BUFFER_SIZE * 40; // SPI总缓冲区容量

        // 初始化屏幕缓冲区
        uartListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>()); // UART屏幕队列
        arinc429ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>()); // 429屏幕队列
        canListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>()); // CAN屏幕队列
        i2cListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>()); // I2C屏幕队列
        linListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>()); // LIN屏幕队列
        milstd1553bListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>()); // 1553B屏幕队列
        spiListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>()); // SPI屏幕队列

        // 初始化总缓冲区
        uartListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>()); // UART总队列
        arinc429ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>()); // 429总队列
        canListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>()); // CAN总队列
        i2cListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>()); // I2C总队列
        linListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>()); // LIN总队列
        milstd1553bListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>()); // 1553B总队列
        spiListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>()); // SPI总队列

        // 初始化S1S2混合屏幕缓冲区
        uartS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>()); // UART S1S2屏幕队列
        arinc429S1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>()); // 429 S1S2屏幕队列
        canS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>()); // CAN S1S2屏幕队列
        i2cS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>()); // I2C S1S2屏幕队列
        linS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>()); // LIN S1S2屏幕队列
        milstd1553bS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>()); // 1553B S1S2屏幕队列
        spiS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>()); // SPI S1S2屏幕队列

        // 初始化S1S2混合总缓冲区
        uartS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>()); // UART S1S2总队列
        arinc429S1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>()); // 429 S1S2总队列
        milstd1553bS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>()); // 1553B S1S2总队列
        canS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>()); // CAN S1S2总队列
        i2cS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>()); // I2C S1S2总队列
        linS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>()); // LIN S1S2总队列
        spiS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>()); // SPI S1S2总队列
    }

    /**
     * 设置S1S2混合模式的开启/关闭
     * 关闭时清空所有S1S2混合缓冲区及计数器
     *
     * @param isOpen true=开启混合模式，false=关闭混合模式
     * @param serialbusStruct_SerialBusType 总线类型（当前实现中未使用，统一操作所有协议）
     */
    public void setOpenS1S2(boolean isOpen, int serialbusStruct_SerialBusType) {
        synchronized (lock) { // 加锁保证线程安全
            if (openS1AndS2 == isOpen) return; // 状态未变，直接返回
            openS1AndS2 = isOpen; // 更新状态
            if (!openS1AndS2) { // 关闭混合模式时，清空所有S1S2缓冲区
                // 清空S1S2总缓冲区
                if (uartS1S2ListTotal != null) uartS1S2ListTotal.clear(); // 清空UART S1S2总队列
                if (arinc429S1S2ListTotal != null) arinc429S1S2ListTotal.clear(); // 清空429 S1S2总队列
                if (canS1S2ListTotal != null) canS1S2ListTotal.clear(); // 清空CAN S1S2总队列
                if (i2cS1S2ListTotal != null) i2cS1S2ListTotal.clear(); // 清空I2C S1S2总队列
                if (linS1S2ListTotal != null) linS1S2ListTotal.clear(); // 清空LIN S1S2总队列
                if (milstd1553bS1S2ListTotal != null) milstd1553bS1S2ListTotal.clear(); // 清空1553B S1S2总队列
                if (spiS1S2ListTotal != null) spiS1S2ListTotal.clear(); // 清空SPI S1S2总队列

                // 清空S1S2屏幕缓冲区
                if (uartS1S2ListScreen != null) uartS1S2ListScreen.clear(); // 清空UART S1S2屏幕队列
                if (arinc429S1S2ListScreen != null) arinc429S1S2ListScreen.clear(); // 清空429 S1S2屏幕队列
                if (canS1S2ListScreen != null) canS1S2ListScreen.clear(); // 清空CAN S1S2屏幕队列
                if (i2cS1S2ListScreen != null) i2cS1S2ListScreen.clear(); // 清空I2C S1S2屏幕队列
                if (linS1S2ListScreen != null) linS1S2ListScreen.clear(); // 清空LIN S1S2屏幕队列
                if (milstd1553bS1S2ListScreen != null) milstd1553bS1S2ListScreen.clear(); // 清空1553B S1S2屏幕队列
                if (spiS1S2ListScreen != null) spiS1S2ListScreen.clear(); // 清空SPI S1S2屏幕队列

                // 重置S1S2屏幕计数器
                uartS1S2CurrScreenSize = 0; // UART S1S2屏幕计数归零
                arinc429S1S2CurrScreenSize = 0; // 429 S1S2屏幕计数归零
                canS1S2CurrScreenSize = 0; // CAN S1S2屏幕计数归零
                i2cS1S2CurrScreenSize = 0; // I2C S1S2屏幕计数归零
                linS1S2CurrScreenSize = 0; // LIN S1S2屏幕计数归零
                milstd1553bS1S2CurrScreenSize = 0; // 1553B S1S2屏幕计数归零
                spiS1S2CurrScreenSize = 0; // SPI S1S2屏幕计数归零

                // 重置S1S2总计数器
                uartS1S2CurrSize = 0; // UART S1S2总计数归零
                arinc429S1S2CurrSize = 0; // 429 S1S2总计数归零
                canS1S2CurrSize = 0; // CAN S1S2总计数归零
                i2cS1S2CurrSize = 0; // I2C S1S2总计数归零
                linS1S2CurrSize = 0; // LIN S1S2总计数归零
                milstd1553bS1S2CurrSize = 0; // 1553B S1S2总计数归零
                spiS1S2CurrSize = 0; // SPI S1S2总计数归零
            }
        }
    }

    /**
     * 带参数的构造函数（当前实现为空，保留扩展用）
     * @param uartBufferSize UART缓冲区大小
     * @param arinc429BufferSize ARINC429缓冲区大小
     * @param canBufferSize CAN缓冲区大小
     * @param i2cBufferSize I2C缓冲区大小
     * @param lineBufferSize LIN缓冲区大小
     * @param milstd1553bBufferSize MIL-STD-1553B缓冲区大小
     * @param spiBufferSize SPI缓冲区大小
     */
    public SerialTxtBuffer(int uartBufferSize, int arinc429BufferSize, int canBufferSize,
                           int i2cBufferSize, int lineBufferSize, int milstd1553bBufferSize, int spiBufferSize) {
        // 保留扩展，当前未实现自定义缓冲区大小
    }

    /**
     * 判断指定帧的通道是否在S1S2混合模式中被选中
     * 只有被选中的通道数据才会写入S1S2混合缓冲区
     *
     * @param o 串口总线文本结构体，需实现ISerialBusTxtCSV接口
     * @return true=该通道被选中，数据应写入S1S2混合缓冲区；false=未选中
     */
    public boolean isSerialsSelected(SerialBusTxtStruct.ISerialBusTxtCSV o) {
        String chName = o.getCh(); // 获取帧的通道名称（如"S1"）
        HashMap<String, Boolean> map = SerialsTxtUtils.getInstance().getSerialsCheckMap(); // 获取通道选中状态映射
        // 选中的组合才塞数据
        return !map.isEmpty() && map.containsKey(chName) && Boolean.TRUE.equals(map.get(chName)); // 映射非空且通道被选中
    }

    /**
     * 核心写入方法：将解析后的帧结构体写入对应协议的缓冲区
     * 写入策略：
     * 1. 总缓冲区满时淘汰最旧帧（poll），保证不超过容量限制
     * 2. 屏幕缓冲区满时淘汰最旧帧（poll），保证不超过容量限制
     * 3. 若S1S2混合模式开启且通道被选中，同时写入S1S2混合缓冲区
     *
     * @param serialBusType 总线协议类型（1=UART, 2=LIN, 3=CAN, 4=SPI, 5=I2C, 6=429, 7=1553B）
     * @param o 解析后的帧结构体对象
     */
    public void put(int serialBusType, Object o) {
        switch (serialBusType) { // 根据总线类型分发
            case SerialBusStruct.SerialBusType_UART: { // UART（类型值=1）
                lastUartNode = (SerialBusTxtStruct.UartStruct) o; // 更新最后一帧引用
                synchronized (lock) { // 加锁保证线程安全
                    // 总缓冲区溢出淘汰
                    while (uartCurrSize >= uartBufferSize) { // 总缓冲区满
                        uartCurrSize--; // 计数-1
                        uartListTotal.poll(); // 淘汰最旧帧
                    }
                    uartCurrSize++; // 计数+1
                    uartListTotal.add((SerialBusTxtStruct.UartStruct) o); // 写入总缓冲区

                    // 屏幕缓冲区溢出淘汰
                    while (uartCurrScreenSize >= uartBufferScreenSize) { // 屏幕缓冲区满
                        uartCurrScreenSize--; // 计数-1
                        uartListScreen.poll(); // 淘汰最旧帧
                    }
                    uartCurrScreenSize++; // 计数+1
                    uartListScreen.add((SerialBusTxtStruct.UartStruct) o); // 写入屏幕缓冲区
                }
                // S1S2混合模式处理
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.UartStruct) o)) { // 混合模式开启且通道选中
                    synchronized (lock) { // 加锁
                        while (uartS1S2CurrSize >= uartBufferSize * 2) { // S1S2总缓冲区满（容量=单通道2倍）
                            uartS1S2CurrSize--; // 计数-1
                            uartS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        uartS1S2CurrSize++; // 计数+1
                        uartS1S2ListTotal.add((SerialBusTxtStruct.UartStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (uartS1S2CurrScreenSize >= uartBufferScreenSize * 2) { // S1S2屏幕缓冲区满
                            uartS1S2CurrScreenSize--; // 计数-1
                            uartS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        uartS1S2CurrScreenSize++; // 计数+1
                        uartS1S2ListScreen.add((SerialBusTxtStruct.UartStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_429: { // ARINC429（类型值=6）
                last429Node = (SerialBusTxtStruct.Arinc429Struct) o; // 更新最后一帧引用
                synchronized (lock) { // 加锁
                    while (arinc429CurrSize >= arinc429BufferSize) { // 总缓冲区满
                        arinc429CurrSize--; // 计数-1
                        arinc429ListTotal.poll(); // 淘汰最旧帧
                    }
                    arinc429CurrSize++; // 计数+1
                    arinc429ListTotal.add((SerialBusTxtStruct.Arinc429Struct) o); // 写入总缓冲区

                    while (arinc429CurrScreenSize >= arinc429BufferScreenSize) { // 屏幕缓冲区满
                        arinc429CurrScreenSize--; // 计数-1
                        arinc429ListScreen.poll(); // 淘汰最旧帧
                    }
                    arinc429CurrScreenSize++; // 计数+1
                    arinc429ListScreen.add((SerialBusTxtStruct.Arinc429Struct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.Arinc429Struct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (arinc429S1S2CurrSize >= arinc429BufferSize * 2) { // S1S2总缓冲区满
                            arinc429S1S2CurrSize--; // 计数-1
                            arinc429S1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        arinc429S1S2CurrSize++; // 计数+1
                        arinc429S1S2ListTotal.add((SerialBusTxtStruct.Arinc429Struct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (arinc429S1S2CurrScreenSize >= arinc429BufferScreenSize * 3) { // S1S2屏幕缓冲区满（容量=单通道3倍）
                            arinc429S1S2CurrScreenSize--; // 计数-1
                            arinc429S1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        arinc429S1S2CurrScreenSize++; // 计数+1
                        arinc429S1S2ListScreen.add((SerialBusTxtStruct.Arinc429Struct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: { // MIL-STD-1553B（类型值=7）
                synchronized (lock) { // 加锁
                last1553bNode=(SerialBusTxtStruct.MilSTD1553bStruct) o; // 更新最后一帧引用
                while (milstd1553bCurrSize >= milstd1553bBufferSize){milstd1553bCurrSize--;     milstd1553bListTotal.poll();} // 总缓冲区满时淘汰
                milstd1553bCurrSize++; // 计数+1
                milstd1553bListTotal.add((SerialBusTxtStruct.MilSTD1553bStruct) o); // 写入总缓冲区

                while (milstd1553bCurrScreenSize >= milstd1553bBufferScreenSize) { milstd1553bCurrScreenSize--;   milstd1553bListScreen.poll();              } // 屏幕缓冲区满时淘汰
                milstd1553bCurrScreenSize++; // 计数+1
                milstd1553bListScreen.add((SerialBusTxtStruct.MilSTD1553bStruct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.MilSTD1553bStruct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (milstd1553bS1S2CurrSize >= milstd1553bBufferSize * 2) { // S1S2总缓冲区满
                            milstd1553bS1S2CurrSize--; // 计数-1
                            milstd1553bS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        milstd1553bS1S2CurrSize++; // 计数+1
                        milstd1553bS1S2ListTotal.add((SerialBusTxtStruct.MilSTD1553bStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (milstd1553bS1S2CurrScreenSize >= milstd1553bBufferScreenSize * 3) { // S1S2屏幕缓冲区满
                            milstd1553bS1S2CurrScreenSize--; // 计数-1
                            milstd1553bS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        milstd1553bS1S2CurrScreenSize++; // 计数+1
                        milstd1553bS1S2ListScreen.add((SerialBusTxtStruct.MilSTD1553bStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: { // CAN（类型值=3）
                lastCanNode = (SerialBusTxtStruct.CanStruct) o; // 更新最后一帧引用
                synchronized (lock) { // 加锁
                    while (canCurrSize >= canBufferSize) { // 总缓冲区满
                        canCurrSize--; // 计数-1
                        canListTotal.poll(); // 淘汰最旧帧
                    }
                    canCurrSize++; // 计数+1
                    canListTotal.add((SerialBusTxtStruct.CanStruct) o); // 写入总缓冲区

                    while (canCurrScreenSize >= canBufferScreenSize) { // 屏幕缓冲区满
                        canCurrScreenSize--; // 计数-1
                        canListScreen.poll(); // 淘汰最旧帧
                    }
                    canCurrScreenSize++; // 计数+1
                    canListScreen.add((SerialBusTxtStruct.CanStruct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.CanStruct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (canS1S2CurrSize >= canBufferSize * 2) { // S1S2总缓冲区满
                            canS1S2CurrSize--; // 计数-1
                            canS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        canS1S2CurrSize++; // 计数+1
                        canS1S2ListTotal.add((SerialBusTxtStruct.CanStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (canS1S2CurrScreenSize >= canBufferScreenSize * 3) { // S1S2屏幕缓冲区满
                            canS1S2CurrScreenSize--; // 计数-1
                            canS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        canS1S2CurrScreenSize++; // 计数+1
                        canS1S2ListScreen.add((SerialBusTxtStruct.CanStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: { // I2C（类型值=5）
                lastI2cNode = ((SerialBusTxtStruct.I2cStruct) o); // 更新最后一帧引用
                synchronized (lock) { // 加锁
                    while (i2cCurrSize >= i2cBufferSize) { // 总缓冲区满
                        i2cCurrSize--; // 计数-1
                        i2cListTotal.poll(); // 淘汰最旧帧
                    }
                    i2cCurrSize++; // 计数+1
                    i2cListTotal.add((SerialBusTxtStruct.I2cStruct) o); // 写入总缓冲区

                    while (i2cCurrScreenSize >= i2cBufferScreenSize) { // 屏幕缓冲区满
                        i2cCurrScreenSize--; // 计数-1
                        i2cListScreen.poll(); // 淘汰最旧帧
                    }
                    i2cCurrScreenSize++; // 计数+1
                    i2cListScreen.add((SerialBusTxtStruct.I2cStruct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.I2cStruct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (i2cS1S2CurrSize >= i2cBufferSize * 2) { // S1S2总缓冲区满
                            i2cS1S2CurrSize--; // 计数-1
                            i2cS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        i2cS1S2CurrSize++; // 计数+1
                        i2cS1S2ListTotal.add((SerialBusTxtStruct.I2cStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (i2cS1S2CurrScreenSize >= i2cBufferScreenSize * 3) { // S1S2屏幕缓冲区满
                            i2cS1S2CurrScreenSize--; // 计数-1
                            i2cS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        i2cS1S2CurrScreenSize++; // 计数+1
                        i2cS1S2ListScreen.add((SerialBusTxtStruct.I2cStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_LIN: { // LIN（类型值=2）
                lastLinNode = (SerialBusTxtStruct.LinStruct) o; // 更新最后一帧引用
                synchronized (lock) { // 加锁
                    while (linCurrSize >= lineBufferSize) { // 总缓冲区满
                        linCurrSize--; // 计数-1
                        linListTotal.poll(); // 淘汰最旧帧
                    }
                    linCurrSize++; // 计数+1
                    linListTotal.add((SerialBusTxtStruct.LinStruct) o); // 写入总缓冲区

                    while (linCurrScreenSize >= lineBufferScreenSize) { // 屏幕缓冲区满
                        linCurrScreenSize--; // 计数-1
                        linListScreen.poll(); // 淘汰最旧帧
                    }
                    linCurrScreenSize++; // 计数+1
                    linListScreen.add((SerialBusTxtStruct.LinStruct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.LinStruct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (linS1S2CurrSize >= lineBufferSize * 2) { // S1S2总缓冲区满
                            linS1S2CurrSize--; // 计数-1
                            linS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        linS1S2CurrSize++; // 计数+1
                        linS1S2ListTotal.add((SerialBusTxtStruct.LinStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (linS1S2CurrScreenSize >= lineBufferScreenSize * 3) { // S1S2屏幕缓冲区满
                            linS1S2CurrScreenSize--; // 计数-1
                            linS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        linS1S2CurrScreenSize++; // 计数+1
                        linS1S2ListScreen.add((SerialBusTxtStruct.LinStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: { // SPI（类型值=4）
                lastSpiNode = (SerialBusTxtStruct.SpiStruct) o; // 更新最后一帧引用
                synchronized (lock) { // 加锁
                    while (spiCurrSize >= spiBufferSize) { // 总缓冲区满
                        spiCurrSize--; // 计数-1
                        spiListTotal.poll(); // 淘汰最旧帧
                    }
                    spiCurrSize++; // 计数+1
                    spiListTotal.add((SerialBusTxtStruct.SpiStruct) o); // 写入总缓冲区

                    while (spiCurrScreenSize >= spiBufferScreenSize) { // 屏幕缓冲区满
                        spiCurrScreenSize--; // 计数-1
                        spiListScreen.poll(); // 淘汰最旧帧
                    }
                    spiCurrScreenSize++; // 计数+1
                    spiListScreen.add((SerialBusTxtStruct.SpiStruct) o); // 写入屏幕缓冲区
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.SpiStruct) o)) { // 混合模式
                    synchronized (lock) { // 加锁
                        while (spiS1S2CurrSize >= spiBufferSize * 2) { // S1S2总缓冲区满
                            spiS1S2CurrSize--; // 计数-1
                            spiS1S2ListTotal.poll(); // 淘汰最旧帧
                        }
                        spiS1S2CurrSize++; // 计数+1
                        spiS1S2ListTotal.add((SerialBusTxtStruct.SpiStruct) o); // 写入S1S2总缓冲区
                    }
                    synchronized (lock) { // 加锁
                        while (spiS1S2CurrScreenSize >= spiBufferScreenSize * 3) { // S1S2屏幕缓冲区满
                            spiS1S2CurrScreenSize--; // 计数-1
                            spiS1S2ListScreen.poll(); // 淘汰最旧帧
                        }
                        spiS1S2CurrScreenSize++; // 计数+1
                        spiS1S2ListScreen.add((SerialBusTxtStruct.SpiStruct) o); // 写入S1S2屏幕缓冲区
                    }
                }
            }
            break;
        }
    }

    /**
     * 从对应协议的总缓冲区中获取一个新的结构体实例
     * 当总缓冲区已满时，淘汰最旧帧并返回新实例
     * 注意：当前实现总是创建新实例，不回收旧帧
     *
     * @param <T> 结构体泛型类型
     * @param serialBusType 总线协议类型（1-7）
     * @return 新创建的结构体实例，类型不匹配时返回null
     */
    public    <T> T getStruct(int serialBusType){
        synchronized (lock) { // 加锁保证线程安全
            switch (serialBusType) { // 根据总线类型分发
                case SerialBusStruct.SerialBusType_UART: { // UART
                    if (uartCurrSize >= uartBufferSize) { // 总缓冲区满
                        uartCurrSize--; // 计数-1
                        uartListTotal.poll(); // 淘汰最旧帧（当前未回收利用）
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new UartStruct()); // 创建新的UART结构体
                }
                case SerialBusStruct.SerialBusType_LIN: { // LIN
                    if (linCurrSize >= lineBufferSize) { // 总缓冲区满
                        linCurrSize--; // 计数-1
                        (linListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new LinStruct()); // 创建新的LIN结构体
                }
                case SerialBusStruct.SerialBusType_CAN: { // CAN
                    if (canCurrSize >= canBufferSize) { // 总缓冲区满
                        canCurrSize--; // 计数-1
                        (canListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new CanStruct()); // 创建新的CAN结构体
                }
                case SerialBusStruct.SerialBusType_SPI: { // SPI
                    if (spiCurrSize >= spiBufferSize) { // 总缓冲区满
                        spiCurrSize--; // 计数-1
                        (spiListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new SpiStruct()); // 创建新的SPI结构体
                }
                case SerialBusStruct.SerialBusType_I2C: { // I2C
                    if (i2cCurrSize >= i2cBufferSize) { // 总缓冲区满
                        i2cCurrSize--; // 计数-1
                        (i2cListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new I2cStruct()); // 创建新的I2C结构体
                }
                case SerialBusStruct.SerialBusType_429: { // ARINC429
                    if (arinc429CurrSize >= arinc429BufferSize) { // 总缓冲区满
                        arinc429CurrSize--; // 计数-1
                         (arinc429ListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new Arinc429Struct()); // 创建新的429结构体
                }
                case SerialBusStruct.SerialBusType_1553B: { // MIL-STD-1553B
                    if (milstd1553bCurrSize >= milstd1553bBufferSize) { // 总缓冲区满
                        milstd1553bCurrSize--; // 计数-1
                        (milstd1553bListTotal).poll(); // 淘汰最旧帧
                    }
                    return (T) (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct()); // 创建新的1553B结构体
                }
            }
        }
        return null; // 未知总线类型，返回null
    }


    /**
     * 清空所有缓冲区和计数器
     * 包括：FPGA原始数据队列、屏幕缓冲区、总缓冲区、S1S2混合缓冲区、统计计数、时间戳
     * 在示波器停止运行或切换协议时调用
     */
    public void clearAll() {
        synchronized (lock) { // 加锁保证线程安全
            buffer.clear(); // 清空FPGA原始数据队列

            // 重置屏幕缓冲区计数器
            uartCurrScreenSize = 0; // UART屏幕计数归零
            arinc429CurrScreenSize = 0; // 429屏幕计数归零
            canCurrScreenSize = 0; // CAN屏幕计数归零
            i2cCurrScreenSize = 0; // I2C屏幕计数归零
            linCurrScreenSize = 0; // LIN屏幕计数归零
            milstd1553bCurrScreenSize = 0; // 1553B屏幕计数归零
            spiCurrScreenSize = 0; // SPI屏幕计数归零

            // 重置总缓冲区计数器
            uartCurrSize = 0; // UART总计数归零
            arinc429CurrSize = 0; // 429总计数归零
            canCurrSize = 0; // CAN总计数归零
            i2cCurrSize = 0; // I2C总计数归零
            linCurrSize = 0; // LIN总计数归零
            milstd1553bCurrSize = 0; // 1553B总计数归零
            spiCurrSize = 0; // SPI总计数归零

            // 重置S1S2总缓冲区计数器
            uartS1S2CurrSize = 0; // UART S1S2总计数归零
            arinc429S1S2CurrSize = 0; // 429 S1S2总计数归零
            canS1S2CurrSize = 0; // CAN S1S2总计数归零
            i2cS1S2CurrSize = 0; // I2C S1S2总计数归零
            linS1S2CurrSize = 0; // LIN S1S2总计数归零
            milstd1553bS1S2CurrSize = 0; // 1553B S1S2总计数归零
            spiS1S2CurrSize = 0; // SPI S1S2总计数归零

            // 重置S1S2屏幕缓冲区计数器
            uartS1S2CurrScreenSize = 0; // UART S1S2屏幕计数归零
            arinc429S1S2CurrScreenSize = 0; // 429 S1S2屏幕计数归零
            canS1S2CurrScreenSize = 0; // CAN S1S2屏幕计数归零
            i2cS1S2CurrScreenSize = 0; // I2C S1S2屏幕计数归零
            linS1S2CurrScreenSize = 0; // LIN S1S2屏幕计数归零
            milstd1553bS1S2CurrScreenSize = 0; // 1553B S1S2屏幕计数归零
            spiS1S2CurrScreenSize = 0; // SPI S1S2屏幕计数归零

            // 清空屏幕缓冲区队列
            uartListScreen.clear(); // 清空UART屏幕队列
            arinc429ListScreen.clear(); // 清空429屏幕队列
            canListScreen.clear(); // 清空CAN屏幕队列
            i2cListScreen.clear(); // 清空I2C屏幕队列
            linListScreen.clear(); // 清空LIN屏幕队列
            milstd1553bListScreen.clear(); // 清空1553B屏幕队列
            spiListScreen.clear(); // 清空SPI屏幕队列

            // 清空总缓冲区队列
            uartListTotal.clear(); // 清空UART总队列
            arinc429ListTotal.clear(); // 清空429总队列
            canListTotal.clear(); // 清空CAN总队列
            i2cListTotal.clear(); // 清空I2C总队列
            linListTotal.clear(); // 清空LIN总队列
            milstd1553bListTotal.clear(); // 清空1553B总队列
            spiListTotal.clear(); // 清空SPI总队列

            // 清空S1S2混合总缓冲区
            if (uartS1S2ListTotal != null) uartS1S2ListTotal.clear(); // 清空UART S1S2总队列
            if (arinc429S1S2ListTotal != null) arinc429S1S2ListTotal.clear(); // 清空429 S1S2总队列
            if (canS1S2ListTotal != null) canS1S2ListTotal.clear(); // 清空CAN S1S2总队列
            if (i2cS1S2ListTotal != null) i2cS1S2ListTotal.clear(); // 清空I2C S1S2总队列
            if (linS1S2ListTotal != null) linS1S2ListTotal.clear(); // 清空LIN S1S2总队列
            if (milstd1553bS1S2ListTotal != null) milstd1553bS1S2ListTotal.clear(); // 清空1553B S1S2总队列
            if (spiS1S2ListTotal != null) spiS1S2ListTotal.clear(); // 清空SPI S1S2总队列

            // 清空S1S2混合屏幕缓冲区
            if (uartS1S2ListScreen != null) uartS1S2ListScreen.clear(); // 清空UART S1S2屏幕队列
            if (arinc429S1S2ListScreen != null) arinc429S1S2ListScreen.clear(); // 清空429 S1S2屏幕队列
            if (canS1S2ListScreen != null) canS1S2ListScreen.clear(); // 清空CAN S1S2屏幕队列
            if (i2cS1S2ListScreen != null) i2cS1S2ListScreen.clear(); // 清空I2C S1S2屏幕队列
            if (linS1S2ListScreen != null) linS1S2ListScreen.clear(); // 清空LIN S1S2屏幕队列
            if (milstd1553bS1S2ListScreen != null) milstd1553bS1S2ListScreen.clear(); // 清空1553B S1S2屏幕队列
            if (spiS1S2ListScreen != null) spiS1S2ListScreen.clear(); // 清空SPI S1S2屏幕队列

            // 重置统计计数
            uartTotalData = 0; // UART总帧数归零
            uartErrorData = 0; // UART错误帧数归零

            canTotalFrame = 0; // CAN总帧数归零
            canSpaceFrame = 0; // CAN空帧数归零
            canErrorFrame = 0; // CAN错误帧数归零

            arinc429TotalFrame = 0; // 429总帧数归零
            arinc429ErrorFrame = 0; // 429错误帧数归零

            // 重置时间戳
            armLastTime = 0; // FPGA时间戳归零
            totalTime = 0; // 累积总时间归零
        }
    }

    //region uart attribute
    /**
     * 获取UART总接收帧数
     * @return UART总帧数，取值范围[0, Long.MAX_VALUE]
     */
    public long getUartTotalData() {
        return uartTotalData;
    }

    /**
     * UART总帧数+1（线程安全）
     */
    public  void addUartTotalData() {
        synchronized (lock) { // 加锁
            this.uartTotalData++; // 总帧数+1
        }
    }

    /**
     * 获取UART错误帧数
     * @return UART错误帧数，取值范围[0, uartTotalData]
     */
    public long getUartErrorData() {
        return uartErrorData;
    }

    /**
     * UART错误帧数+1（线程安全）
     */
    public  void addUartErrorData() {
        synchronized (lock) { // 加锁
            this.uartErrorData++; // 错误帧数+1
        }
    }

    /**
     * CAN总帧数+1（线程安全）
     */
    public  void addCanTotalFrame() {
        synchronized (lock) { // 加锁
            this.canTotalFrame++; // 总帧数+1
        }
    }

    /**
     * CAN空帧数+1（线程安全），spec错误时调用
     */
    public  void addCanSpaceFrame() {
        synchronized (lock) { // 加锁
            this.canSpaceFrame++; // 空帧数+1
        }
    }

    /**
     * CAN错误帧数+1（线程安全），帧结束且有spec错误时调用
     */
    public  void addCanErrorFrame() {
        synchronized (lock) { // 加锁
            this.canErrorFrame++; // 错误帧数+1
        }
    }

    /**
     * 获取CAN总帧数
     * @return CAN总帧数，取值范围[0, Long.MAX_VALUE]
     */
    public long getCanTotalFrame() {
        return canTotalFrame;
    }

    /**
     * 获取CAN空帧数
     * @return CAN空帧数，取值范围[0, canTotalFrame]
     */
    public long getCanSpaceFrame() {
        return canSpaceFrame;
    }

    /**
     * 获取CAN错误帧数
     * @return CAN错误帧数，取值范围[0, canTotalFrame]
     */
    public long getCanErrorFrame() {
        return canErrorFrame;
    }

    /**
     * ARINC429总帧数+1（线程安全）
     */
    public  void addArinc429TotalFrame() {
        synchronized (lock) { // 加锁
            this.arinc429TotalFrame++; // 总帧数+1
        }
    }

    /**
     * ARINC429错误帧数+1（线程安全）
     */
    public  void addArinc429ErrorFrame() {
        synchronized (lock) { // 加锁
            this.arinc429ErrorFrame++; // 错误帧数+1
        }
    }

    /**
     * 获取ARINC429总帧数
     * @return ARINC429总帧数，取值范围[0, Long.MAX_VALUE]
     */
    public long getArinc429TotalFrame() {
        return arinc429TotalFrame;
    }

    /**
     * 获取ARINC429错误帧数
     * @return ARINC429错误帧数，取值范围[0, arinc429TotalFrame]
     */
    public long getArinc429ErrorFrame() {
        return arinc429ErrorFrame;
    }

    /**
     * 获取S1S2混合模式是否开启
     * @return true=开启，false=关闭
     */
    public boolean isOpenS1AndS2() {
        return openS1AndS2;
    }

    /**
     * 获取上一次FPGA时间戳
     * @return FPGA时间戳，取值范围[0, 32767]
     */
    public int getArmLastTime() {
        return armLastTime;
    }

    /**
     * 设置上一次FPGA时间戳
     * @param armLastTime FPGA时间戳，取值范围[0, 32767]
     */
    public void setArmLastTime(int armLastTime) {
        this.armLastTime = armLastTime;
    }

    /**
     * 获取累积总时间
     * @return 累积总时间，取值范围[0, 599999999]
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * 设置累积总时间
     * @param totalTime 累积总时间，取值范围[0, 599999999]
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * 获取UART最后一帧引用
     * @return UART最后一帧，可能为null
     */
    public SerialBusTxtStruct.UartStruct getLastUartNode() {
        return lastUartNode;
    }

    /**
     * 获取ARINC429最后一帧引用
     * @return ARINC429最后一帧，可能为null
     */
    public SerialBusTxtStruct.Arinc429Struct getLast429Node() {
        return last429Node;
    }

    /**
     * 获取CAN最后一帧引用
     * @return CAN最后一帧，可能为null
     */
    public SerialBusTxtStruct.CanStruct getLastCanNode() {
        return lastCanNode;
    }

    /**
     * 获取SPI最后一帧引用
     * @return SPI最后一帧，可能为null
     */
    public SerialBusTxtStruct.SpiStruct getLastSpiNode() {
        return lastSpiNode;
    }

    /**
     * 获取I2C最后一帧引用
     * @return I2C最后一帧，可能为null
     */
    public SerialBusTxtStruct.I2cStruct getLastI2cNode() {
        return lastI2cNode;
    }

    /**
     * 获取LIN最后一帧引用
     * @return LIN最后一帧，可能为null
     */
    public SerialBusTxtStruct.LinStruct getLastLinNode() {
        return lastLinNode;
    }

    /**
     * 获取MIL-STD-1553B最后一帧引用
     * @return 1553B最后一帧，可能为null
     */
    public SerialBusTxtStruct.MilSTD1553bStruct getLast1553bNode() {
        return last1553bNode;
    }

    /**
     * 设置UART最后一帧引用
     * @param lastUartNode UART帧结构体
     */
    public void setLastUartNode(SerialBusTxtStruct.UartStruct lastUartNode) {
        this.lastUartNode = lastUartNode;
    }

    /**
     * 设置ARINC429最后一帧引用
     * @param last429Node ARINC429帧结构体
     */
    public void setLast429Node(SerialBusTxtStruct.Arinc429Struct last429Node) {
        this.last429Node = last429Node;
    }

    /**
     * 设置CAN最后一帧引用
     * @param lastCanNode CAN帧结构体
     */
    public void setLastCanNode(SerialBusTxtStruct.CanStruct lastCanNode) {
        this.lastCanNode = lastCanNode;
    }

    /**
     * 设置SPI最后一帧引用
     * @param lastSpiNode SPI帧结构体
     */
    public void setLastSpiNode(SerialBusTxtStruct.SpiStruct lastSpiNode) {
        this.lastSpiNode = lastSpiNode;
    }

    /**
     * 设置I2C最后一帧引用
     * @param lastI2cNode I2C帧结构体
     */
    public void setLastI2cNode(SerialBusTxtStruct.I2cStruct lastI2cNode) {
        this.lastI2cNode = lastI2cNode;
    }

    /**
     * 设置LIN最后一帧引用（防重复：若与当前相同则不更新）
     * @param lastLinNode LIN帧结构体
     */
    public void setLastLinNode(SerialBusTxtStruct.LinStruct lastLinNode) {
        if (!(this.lastLinNode.equals(lastLinNode))) // 防止重复设置同一引用
            this.lastLinNode = lastLinNode;
    }

    /**
     * 设置MIL-STD-1553B最后一帧引用
     * @param last1553bNode 1553B帧结构体
     */
    public void setLast1553bNode(SerialBusTxtStruct.MilSTD1553bStruct last1553bNode) {
        this.last1553bNode = last1553bNode;
    }
    //endregion

    //region getList方法

    /**
     * 获取UART S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数，取值范围[0, uartBufferScreenSize*2]
     */
    public static int getUartS1S2CurrScreenSize() {
        return uartS1S2CurrScreenSize;
    }

    /**
     * 获取ARINC429 S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getArinc429S1S2CurrScreenSize() {
        return arinc429S1S2CurrScreenSize;
    }

    /**
     * 获取CAN S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getCanS1S2CurrScreenSize() {
        return canS1S2CurrScreenSize;
    }

    /**
     * 获取I2C S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getI2cS1S2CurrScreenSize() {
        return i2cS1S2CurrScreenSize;
    }

    /**
     * 获取LIN S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getLinS1S2CurrScreenSize() {
        return linS1S2CurrScreenSize;
    }

    /**
     * 获取MIL-STD-1553B S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getMilstd1553bS1S2CurrScreenSize() {
        return milstd1553bS1S2CurrScreenSize;
    }

    /**
     * 获取SPI S1S2混合屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getSpiS1S2CurrScreenSize() {
        return spiS1S2CurrScreenSize;
    }

    /**
     * 获取UART S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getUartS1S2CurrSize() {
        return uartS1S2CurrSize;
    }

    /**
     * 获取ARINC429 S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getArinc429S1S2CurrSize() {
        return arinc429S1S2CurrSize;
    }

    /**
     * 获取CAN S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getCanS1S2CurrSize() {
        return canS1S2CurrSize;
    }

    /**
     * 获取I2C S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getI2cS1S2CurrSize() {
        return i2cS1S2CurrSize;
    }

    /**
     * 获取LIN S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getLinS1S2CurrSize() {
        return linS1S2CurrSize;
    }

    /**
     * 获取MIL-STD-1553B S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getMilstd1553bS1S2CurrSize() {
        return milstd1553bS1S2CurrSize;
    }

    /**
     * 获取SPI S1S2混合总缓冲区当前帧数
     * @return 当前帧数
     */
    public static int getSpiS1S2CurrSize() {
        return spiS1S2CurrSize;
    }

    /**
     * 获取UART屏幕缓冲区当前帧数
     * @return 当前帧数，取值范围[0, uartBufferScreenSize]
     */
    public int getUartCurrScreenSize() {
        return uartCurrScreenSize;
    }

    /**
     * 获取ARINC429屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getArinc429CurrScreenSize() {
        return arinc429CurrScreenSize;
    }

    /**
     * 获取CAN屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getCanCurrScreenSize() {
        return canCurrScreenSize;
    }

    /**
     * 获取I2C屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getI2cCurrScreenSize() {
        return i2cCurrScreenSize;
    }

    /**
     * 获取LIN屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getLinCurrScreenSize() {
        return linCurrScreenSize;
    }

    /**
     * 获取MIL-STD-1553B屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getMilstd1553bCurrScreenSize() {
        return milstd1553bCurrScreenSize;
    }

    /**
     * 获取SPI屏幕缓冲区当前帧数
     * @return 当前帧数
     */
    public int getSpiCurrScreenSize() {
        return spiCurrScreenSize;
    }

    /**
     * 获取UART总缓冲区当前帧数
     * @return 当前帧数，取值范围[0, uartBufferSize]
     */
    public int getUartCurrSize() {
        return uartCurrSize;
    }

    /**
     * 获取ARINC429总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getArinc429CurrSize() {
        return arinc429CurrSize;
    }

    /**
     * 获取CAN总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getCanCurrSize() {
        return canCurrSize;
    }

    /**
     * 获取I2C总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getI2cCurrSize() {
        return i2cCurrSize;
    }

    /**
     * 获取LIN总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getLinCurrSize() {
        return linCurrSize;
    }

    /**
     * 获取MIL-STD-1553B总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getMilstd1553bCurrSize() {
        return milstd1553bCurrSize;
    }

    /**
     * 获取SPI总缓冲区当前帧数
     * @return 当前帧数
     */
    public int getSpiCurrSize() {
        return spiCurrSize;
    }

    /**
     * 获取UART总缓冲区中的最后一个元素
     * 通过迭代器遍历到队列末尾获取
     * @return UART最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.UartStruct getUartLastElement() {
        SerialBusTxtStruct.UartStruct uart = null; // 最后一帧引用
        for (Iterator iter = uartListTotal.iterator(); iter.hasNext(); ) { // 遍历UART总队列
            uart = (SerialBusTxtStruct.UartStruct) iter.next(); // 逐个取出，最终指向最后一个
        }
        if (uart == null) { // 队列为空
            uart = (SerialBusTxtStruct.getInstance().new UartStruct()); // 返回空结构体
        }
        return uart; // 返回最后一帧
    }

    /**
     * 获取ARINC429总缓冲区中的最后一个元素
     * @return ARINC429最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.Arinc429Struct getArinc429LastElement() {
        SerialBusTxtStruct.Arinc429Struct a429 = null; // 最后一帧引用
        for (Iterator iter = arinc429ListTotal.iterator(); iter.hasNext(); ) { // 遍历429总队列
            a429 = (SerialBusTxtStruct.Arinc429Struct) iter.next(); // 逐个取出
        }
        if (a429 == null) { // 队列为空
            a429 = (SerialBusTxtStruct.getInstance().new Arinc429Struct()); // 返回空结构体
        }
        return a429; // 返回最后一帧
    }

    /**
     * 获取CAN总缓冲区中的最后一个元素
     * @return CAN最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.CanStruct getCanLastElement() {
        SerialBusTxtStruct.CanStruct can = null; // 最后一帧引用
        for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) { // 遍历CAN总队列
            can = (SerialBusTxtStruct.CanStruct) iter.next(); // 逐个取出
        }
        if (can == null) { // 队列为空
            can = (SerialBusTxtStruct.getInstance().new CanStruct()); // 返回空结构体
        }
        return can; // 返回最后一帧
    }

    /**
     * 获取I2C总缓冲区中的最后一个元素
     * @return I2C最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.I2cStruct getI2cLastElement() {
        SerialBusTxtStruct.I2cStruct i2c = null; // 最后一帧引用
        for (Iterator iter = i2cListTotal.iterator(); iter.hasNext(); ) { // 遍历I2C总队列
            i2c = (SerialBusTxtStruct.I2cStruct) iter.next(); // 逐个取出
        }
        if (i2c == null) { // 队列为空
            i2c = (SerialBusTxtStruct.getInstance().new I2cStruct()); // 返回空结构体
        }
        return i2c; // 返回最后一帧
    }

    /**
     * 获取LIN总缓冲区中的最后一个元素
     * @return LIN最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.LinStruct getLinLastElement() {
        SerialBusTxtStruct.LinStruct lin = null; // 最后一帧引用
        for (Iterator iter = linListTotal.iterator(); iter.hasNext(); ) { // 遍历LIN总队列
            lin = (SerialBusTxtStruct.LinStruct) iter.next(); // 逐个取出
        }
        if (lin == null) { // 队列为空
            lin = (SerialBusTxtStruct.getInstance().new LinStruct()); // 返回空结构体
        }
        return lin; // 返回最后一帧
    }

    /**
     * 获取MIL-STD-1553B总缓冲区中的最后一个元素
     * @return 1553B最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.MilSTD1553bStruct getM1553bLastElement() {
        SerialBusTxtStruct.MilSTD1553bStruct m1553b = null; // 最后一帧引用
        for (Iterator iter = milstd1553bListTotal.iterator(); iter.hasNext(); ) { // 遍历1553B总队列
            m1553b = (SerialBusTxtStruct.MilSTD1553bStruct) iter.next(); // 逐个取出
        }
        if (m1553b == null) { // 队列为空
            m1553b = (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct()); // 返回空结构体
        }
        return m1553b; // 返回最后一帧
    }

    /**
     * 获取SPI总缓冲区中的最后一个元素
     * @return SPI最后一帧，队列为空时返回空结构体
     */
    public SerialBusTxtStruct.SpiStruct getSpiLastElement() {
        SerialBusTxtStruct.SpiStruct spi = null; // 最后一帧引用
        for (Iterator iter = spiListTotal.iterator(); iter.hasNext(); ) { // 遍历SPI总队列
            spi = (SerialBusTxtStruct.SpiStruct) iter.next(); // 逐个取出
        }
        if (spi == null) { // 队列为空
            spi = (SerialBusTxtStruct.getInstance().new SpiStruct()); // 返回空结构体
        }
        return spi; // 返回最后一帧
    }


    //endregion

    //region getQueue方法

    /**
     * 获取UART S1S2混合屏幕缓冲区队列
     * @return UART S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> getUartS1S2ListScreen() {
        return uartS1S2ListScreen;
    }

    /**
     * 获取ARINC429 S1S2混合屏幕缓冲区队列
     * @return ARINC429 S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> getArinc429S1S2ListScreen() {
        return arinc429S1S2ListScreen;
    }

    /**
     * 获取CAN S1S2混合屏幕缓冲区队列
     * @return CAN S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> getCanS1S2ListScreen() {
        return canS1S2ListScreen;
    }

    /**
     * 获取I2C S1S2混合屏幕缓冲区队列
     * @return I2C S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> getI2cS1S2ListScreen() {
        return i2cS1S2ListScreen;
    }

    /**
     * 获取LIN S1S2混合屏幕缓冲区队列
     * @return LIN S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> getLinS1S2ListScreen() {
        return linS1S2ListScreen;
    }

    /**
     * 获取MIL-STD-1553B S1S2混合屏幕缓冲区队列
     * @return 1553B S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> getMilstd1553bS1S2ListScreen() {
        return milstd1553bS1S2ListScreen;
    }

    /**
     * 获取SPI S1S2混合屏幕缓冲区队列
     * @return SPI S1S2屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> getSpiS1S2ListScreen() {
        return spiS1S2ListScreen;
    }

    /**
     * 获取UART屏幕缓冲区队列
     * @return UART屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> getUartListScreen() {
        return uartListScreen;
    }

    /**
     * 获取ARINC429屏幕缓冲区队列
     * @return ARINC429屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> getArinc429ListScreen() {
        return arinc429ListScreen;
    }

    /**
     * 获取CAN屏幕缓冲区队列
     * @return CAN屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> getCanListScreen() {
        return canListScreen;
    }

    /**
     * 获取I2C屏幕缓冲区队列
     * @return I2C屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> getI2cListScreen() {
        return i2cListScreen;
    }

    /**
     * 获取LIN屏幕缓冲区队列
     * @return LIN屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> getLinListScreen() {
        return linListScreen;
    }

    /**
     * 获取MIL-STD-1553B屏幕缓冲区队列
     * @return 1553B屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> getMilstd1553bListScreen() {
        return milstd1553bListScreen;
    }

    /**
     * 获取SPI屏幕缓冲区队列
     * @return SPI屏幕队列
     */
    public LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> getSpiListScreen() {
        return spiListScreen;
    }

    /**
     * 获取UART总缓冲区队列
     * @return UART总队列
     */
    public LinkedBlockingQueue getUartQueueTotal() {
        return uartListTotal;
    }

    /**
     * 获取ARINC429总缓冲区队列
     * @return ARINC429总队列
     */
    public LinkedBlockingQueue getArinc429QueueTotal() {
        return arinc429ListTotal;
    }

    /**
     * 获取CAN总缓冲区队列
     * @return CAN总队列
     */
    public LinkedBlockingQueue getCanQueueTotal() {
        return canListTotal;
    }

    /**
     * 获取I2C总缓冲区队列
     * @return I2C总队列
     */
    public LinkedBlockingQueue getI2cQueueTotal() {
        return i2cListTotal;
    }

    /**
     * 获取LIN总缓冲区队列
     * @return LIN总队列
     */
    public LinkedBlockingQueue getLinQueueTotal() {
        return linListTotal;
    }

    /**
     * 获取MIL-STD-1553B总缓冲区队列
     * @return 1553B总队列
     */
    public LinkedBlockingQueue getMilstd1553bQueueTotal() {
        return milstd1553bListTotal;
    }

    /**
     * 获取SPI总缓冲区队列
     * @return SPI总队列
     */
    public LinkedBlockingQueue getSpiQueueTotal() {
        return spiListTotal;
    }

    /**
     * 获取UART S1S2混合总缓冲区队列
     * @return UART S1S2总队列
     */
    public LinkedBlockingQueue getUartS1S2QueueTotal() {
        return uartS1S2ListTotal;
    }

    /**
     * 获取ARINC429 S1S2混合总缓冲区队列
     * @return ARINC429 S1S2总队列
     */
    public LinkedBlockingQueue getArinc429S1S2QueueTotal() {
        return arinc429S1S2ListTotal;
    }

    /**
     * 获取CAN S1S2混合总缓冲区队列
     * @return CAN S1S2总队列
     */
    public LinkedBlockingQueue getCanS1S2QueueTotal() {
        return canS1S2ListTotal;
    }

    /**
     * 获取I2C S1S2混合总缓冲区队列
     * @return I2C S1S2总队列
     */
    public LinkedBlockingQueue getI2cS1S2QueueTotal() {
        return i2cS1S2ListTotal;
    }

    /**
     * 获取LIN S1S2混合总缓冲区队列
     * @return LIN S1S2总队列
     */
    public LinkedBlockingQueue getLinS1S2QueueTotal() {
        return linS1S2ListTotal;
    }

    /**
     * 获取MIL-STD-1553B S1S2混合总缓冲区队列
     * @return 1553B S1S2总队列
     */
    public LinkedBlockingQueue getMilstd1553bS1S2QueueTotal() {
        return milstd1553bS1S2ListTotal;
    }

    /**
     * 获取SPI S1S2混合总缓冲区队列
     * @return SPI S1S2总队列
     */
    public LinkedBlockingQueue getSpiS1S2QueueTotal() {
        return spiS1S2ListTotal;
    }
    //endregion

    //region 测试
    /** 测试用队列（保留，未使用） */
    private LinkedBlockingQueue queue = new LinkedBlockingQueue();

    //endregion
}
