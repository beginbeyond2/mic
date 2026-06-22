package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包

import android.annotation.SuppressLint; // Android注解工具，抑制lint警告
import android.graphics.Color; // Android颜色常量类，用于帧状态着色

import com.micsig.base.Logger; // MHO基础日志工具类
import com.micsig.tbook.scope.Scope; // 示波器运行状态管理类，判断是否运行中
import com.micsig.tbook.tbookscope.R; // 资源ID引用类
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类，含字节转十六进制字符串等
import com.micsig.tbook.tbookscope.util.App; // 应用全局上下文工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，读取用户配置（如429格式、LIN类型）
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage; // 串口总线管理类
import com.micsig.tbook.ui.MScopePublicConst; // 示波器公共常量类
import com.micsig.tbook.ui.wavezone.TChan; // 通道定义类，含S1-S4串口通道常量

import java.nio.ByteBuffer; // NIO字节缓冲区，FPGA数据载体
import java.util.concurrent.ExecutorService; // 线程池服务接口
import java.util.concurrent.Executors; // 线程池工厂类
import java.util.concurrent.LinkedBlockingQueue; // 线程安全的阻塞队列

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    SerialBusTxtStructParse                                  ║
 * ║                    串口总线文本解码解析器                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║【模块定位】wavedata包核心解析器，位于FPGA数据采集与UI显示之间的中间层           ║
 * ║【核心职责】从FPGA的ByteBuffer中解析7种串口总线协议的二进制数据为文本结构体      ║
 * ║【架构设计】Holder单例模式 + ExecutorService线程池多通道并行解析               ║
 * ║【数据流  】FPGA→ByteBuffer→LinkedBlockingQueue→线程池解析→SerialTxtBuffer  ║
 * ║【依赖关系】                                                                ║
 * ║   ├─ SerialTxtBuffer：解析结果缓冲区管理器                                  ║
 * ║   ├─ SerialBusTxtStruct：7种总线协议文本结构体定义                           ║
 * ║   ├─ SerialBusStruct：总线类型常量定义                                      ║
 * ║   ├─ TChan：通道定义（S1-S4）                                              ║
 * ║   ├─ ICharacterEncoding：编码方式常量（Hex/Bin/Dec/Oct/ASCII）              ║
 * ║   ├─ CacheUtil：用户配置缓存（429格式、LIN类型等）                           ║
 * ║   └─ Scope：示波器运行状态                                                  ║
 * ║【使用场景】                                                                ║
 * ║   1. FPGA数据到达后，调用toParseByRunable()启动线程池异步解析               ║
 * ║   2. 切换协议/停止运行时，调用InterruptedParse()中断当前解析任务             ║
 * ║   3. 支持7种总线：UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SerialBusTxtStructParse {
    /** 日志标签，用于Logcat过滤，值："SerialBusTxtStructParse" */
    private static final String TAG = "SerialBusTxtStructParse";

    //region 单例模式 —— 静态内部类实现，保证线程安全的懒加载
    /**
     * 单例持有者类，利用JVM类加载机制保证线程安全
     * 在SerialBusTxtStructParseHolder被首次引用时才创建实例
     */
    public static class SerialBusTxtStructParseHolder {
        /** 单例实例，JVM保证只初始化一次，值：SerialBusTxtStructParse唯一实例 */
        public static final SerialBusTxtStructParse instance = new SerialBusTxtStructParse();
    }

    /**
     * 获取单例实例
     * @return SerialBusTxtStructParse全局唯一实例
     */
    public static SerialBusTxtStructParse getInstance() {
        return SerialBusTxtStructParseHolder.instance; // 通过静态内部类获取单例
    }

    /**
     * 构造函数，初始化每个串口通道的线程池和解析Runnable
     * 遍历S1-S4所有串口通道，为每个通道创建独立的单线程池和ToParseByRunable
     */
    public SerialBusTxtStructParse(){
        TChan.foreachSerial((ch)->{ // 遍历所有串口通道（S1-S4）
            int chIdx=ch-TChan.S1; // 计算通道索引：0=S1, 1=S2, 2=S3, 3=S4，取值范围[0, MaxSerial-1]
             fixedThreadPool[chIdx]=Executors.newFixedThreadPool(1); // 为每个通道创建单线程池，保证该通道解析顺序性
            toParseByRunables[chIdx]=new ToParseByRunable(); // 为每个通道创建独立的解析Runnable
        });
    }
    //endregion

    /** FPGA时间戳，用于时间计算，取值范围[0, 599999999]（99m59s999ms990us） */
    private long time;
    /** 每个通道的编码方式，取值范围：ICharacterEncoding常量（Hex/Bin/Dec/Oct/ASCII） */
    private int[] encodings = new int[4]; // 数组索引对应S1-S4通道
    /** 每个通道的数据位宽，取值范围[5, 9]（UART为5-9位，SPI为4-32位） */
    private int[] bits = new int[4]; // 数组索引对应S1-S4通道
    /** 每个通道是否启用校验，true=启用奇偶校验，false=不启用 */
    private boolean[] checkeds = new boolean[4]; // 数组索引对应S1-S4通道

    /** 每个通道的固定大小线程池，数组长度=TChan.MaxSerial，每个池只有1个线程 */
    final ExecutorService[] fixedThreadPool=new ExecutorService[TChan.MaxSerial]; // 索引0=S1, 1=S2, 2=S3, 3=S4
    /** 每个通道的解析Runnable实例，数组长度=TChan.MaxSerial */
    private ToParseByRunable[] toParseByRunables=new ToParseByRunable[TChan.MaxSerial]; // 索引0=S1, 1=S2, 2=S3, 3=S4

    /** 每个通道是否正在解析中，true=该通道正在解析，false=空闲 */
    private boolean[] parsing=new boolean[TChan.MaxSerial]; // 索引0=S1, 1=S2, 2=S3, 3=S4

    //region 线程解析控制

    /**
     * 获取指定通道或全局的解析状态
     * @param ch_SerialBus_TXTS1 通道常量（如TChan.S1），若非串口通道则查询全局是否有任一通道在解析
     * @return true=正在解析中，false=空闲
     */
    public boolean getParsing(int ch_SerialBus_TXTS1) {
        int idx=(ch_SerialBus_TXTS1-TChan.S1); // 计算通道索引，取值范围[0, MaxSerial-1]
        if (TChan.isSerial(ch_SerialBus_TXTS1)){ // 判断是否为有效的串口通道
            return parsing[idx]; // 返回指定通道的解析状态
        }else { // 非串口通道参数，查询全局状态
            for(int i=0;i<parsing.length;i++){ // 遍历所有通道
                if (parsing[i]){ // 任一通道正在解析
                    return true; // 返回true
                }
            }
            return false; // 所有通道均空闲
        }
    }

    /**
     * 中断所有通道的解析任务
     * 遍历所有串口通道，调用每个通道的ToParseByRunable.Interrupted()设置中断标志
     * @param ch_SerialBus_TXTS1 通道常量（当前实现中未使用，统一中断所有通道）
     */
    public void InterruptedParse(int ch_SerialBus_TXTS1) {
        TChan.foreachSerial((ch)->{ // 遍历所有串口通道
            int chIdx=ch-TChan.S1; // 计算通道索引
            toParseByRunables[chIdx].Interrupted(); // 设置该通道的中断标志
        });
    }

    /**
     * 解析线程的Runnable实现类
     * 从LinkedBlockingQueue中逐个取出ByteBuffer进行协议解析
     * 支持通过interrupte标志中断解析，中断后跳过剩余数据包
     */
    class ToParseByRunable implements Runnable {
        /** 待解析的ByteBuffer队列，由外部通过setBuffers设置 */
        private LinkedBlockingQueue<ByteBuffer> buffers = null; // null表示未初始化

        /**
         * 设置待解析的数据缓冲区队列
         * @param buffers 来自SerialTxtBuffer的ByteBuffer阻塞队列
         */
        public void setBuffers(LinkedBlockingQueue<ByteBuffer> buffers) {
            this.buffers = buffers; // 保存队列引用
        }

        /** 通道名称字符串，如"S1"、"S2"等，用于线程命名和日志 */
        private String Ch; // 取值范围：SerialBusManage定义的通道名称
        /** 串口文本缓冲区管理器，解析结果写入此对象 */
        private SerialTxtBuffer serialTxtBuffer;
        /** 总线协议类型，取值范围：SerialBusStruct.SerialBusType_*常量（1-7） */
        private int serialBusType;
        /** 编码方式，取值范围：ICharacterEncoding常量 */
        private int encoding;
        /** 数据位宽，取值范围[5, 32]，具体取决于协议 */
        private int bits;
        /** 是否启用校验，true=启用奇偶校验，false=不启用 */
        private boolean checked;

        /** 中断标志，true=要求中断解析，false=正常解析中 */
        private boolean interrupte = false; // 初始为false，由Interrupted()设为true

        /**
         * 设置中断标志，通知解析线程停止处理新数据
         * 已取出的数据包会被跳过（count计数但不解析）
         */
        public void Interrupted() {
            interrupte = true; // 设置中断标志
        }

        /**
         * 设置解析参数
         * @param Ch 通道名称（如"S1"）
         * @param serialTxtBuffer 文本缓冲区管理器
         * @param serialBusType 总线协议类型（1=UART, 2=LIN, 3=CAN, 4=SPI, 5=I2C, 6=429, 7=1553B）
         * @param encoding 编码方式（Hex/Bin/Dec/Oct/ASCII）
         * @param bits 数据位宽
         * @param checked 是否启用校验
         */
        public void setParam(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, int encoding, int bits, boolean checked) {
            this.Ch = Ch; // 保存通道名称
            this.serialTxtBuffer = serialTxtBuffer; // 保存缓冲区引用
            this.serialBusType = serialBusType; // 保存总线类型
            this.encoding = encoding; // 保存编码方式
            this.bits = bits; // 保存位宽
            this.checked = checked; // 保存校验标志
        }

        /**
         * 线程执行体：从队列中逐个取出ByteBuffer进行解析
         * 解析流程：
         * 1. 设置线程名称便于调试
         * 2. 标记该通道为"解析中"
         * 3. 循环取出ByteBuffer，调用toParse()解析
         * 4. 若收到中断信号，跳过剩余数据包
         * 5. 示波器停止运行时立即退出
         * 6. 解析完成后标记该通道为"空闲"
         */
        @Override
        public void run() {
            int count = 0; // 被中断跳过的数据包计数，取值范围[0, +∞)
            interrupte = false; // 重置中断标志，准备新一轮解析
            Thread.currentThread().setName("serial_txt_parse_" + this.Ch); // 设置线程名，格式如"serial_txt_parse_S1"
            int idx= TChan.findChanByValue(this.Ch) - TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]

            parsing[idx]=true; // 标记该通道为解析中
            while (buffers != null && !buffers.isEmpty()) { // 队列非空时循环
                if (Scope.getInstance().isRun()==false) break; // 示波器停止运行，退出解析
                bytes = buffers.poll(); // 从队列头部取出一个ByteBuffer，无数据时返回null
                if (interrupte == false) { // 未收到中断信号
                    if (bytes == null) break; // 队列为空，退出循环
                    toParse(Ch, serialTxtBuffer, serialBusType, bytes, encoding, bits, checked); // 调用核心解析方法
                } else { // 已收到中断信号
                    count++; // 跳过计数+1
                }
            }
            parsing[idx]=false; // 标记该通道为空闲
        }
    }

    /**
     * 通过线程池异步执行解析任务
     * 将解析参数设置到对应通道的ToParseByRunable，并提交到该通道的线程池执行
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param serialBusType 总线协议类型（1-7）
     * @param encoding 编码方式
     * @param bits 数据位宽
     * @param checked 是否启用校验
     */
    public void toParseByRunable(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, int encoding, int bits, boolean checked) {
        int chIdx= TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        toParseByRunables[chIdx].setBuffers(serialTxtBuffer.getBuffer()); // 设置待解析的ByteBuffer队列
        toParseByRunables[chIdx].setParam(Ch, serialTxtBuffer, serialBusType, encoding, bits, checked); // 设置解析参数
        fixedThreadPool[chIdx].execute(toParseByRunables[chIdx]); // 提交到该通道的线程池执行
    }
    //endregion


    /**
     * 核心协议解析分发方法
     * 根据总线类型分发到对应的getXxxStruct解析方法
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器，解析结果写入此对象
     * @param serialBusType 总线协议类型（1=UART, 2=LIN, 3=CAN, 4=SPI, 5=I2C, 6=429, 7=1553B）
     * @param bytes FPGA原始二进制数据缓冲区
     * @param encoding 编码方式（Hex/Bin/Dec/Oct/ASCII）
     * @param bits 数据位宽
     * @param checked 是否启用校验
     */
    public void toParse(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, final ByteBuffer bytes, int encoding, int bits, boolean checked) {
        int chIdx= TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        this.encodings[chIdx]=encoding; // 保存该通道的编码方式
        this.bits[chIdx]=bits; // 保存该通道的数据位宽
        this.checkeds[chIdx]=checked; // 保存该通道的校验标志

        switch (serialBusType) { // 根据总线类型分发解析
            case SerialBusStruct.SerialBusType_UART: { // UART总线（类型值=1）
                getUartStruct(Ch, serialTxtBuffer, bytes); // 解析UART协议
            }
            break;
            case SerialBusStruct.SerialBusType_LIN: { // LIN总线（类型值=2）
                getLinStruct(Ch, serialTxtBuffer, bytes); // 解析LIN协议
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: { // CAN总线（类型值=3）
                getCanStruct(Ch, serialTxtBuffer, bytes); // 解析CAN协议
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: { // SPI总线（类型值=4）
                getSpiStruct(Ch, serialTxtBuffer, bytes); // 解析SPI协议
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: { // I2C总线（类型值=5）
                getI2cStruct(Ch, serialTxtBuffer, bytes); // 解析I2C协议
            }
            break;
            case SerialBusStruct.SerialBusType_429: { // ARINC429总线（类型值=6）
                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT // 从缓存读取429显示格式
                        + TChan.toSerialNumber(Ch)); // 拼接通道号作为缓存key
                get429Struct(Ch, format, serialTxtBuffer, bytes); // 解析ARINC429协议，需额外传入显示格式
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: { // MIL-STD-1553B总线（类型值=7）
                get1553bStruct(Ch, serialTxtBuffer, bytes); // 解析1553B协议
            }
            break;
        }
    }

    /**
     * 解析UART协议二进制数据为文本结构体
     * UART数据格式：每2字节为一组，低字节为数据，高字节为类型标志
     * 高字节bit7=1表示时间帧，bit[2:0]表示校验+停止位状态
     * 当位宽>8时，需要两次数据拼接（奇偶校验位占1位）
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void getUartStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int UartType = SerialBusStruct.SerialBusType_UART; // UART类型常量=1
        final int NoStopColor = Color.YELLOW; // 无停止位颜色：黄色，表示异常
        final int StopSuccessColor = App.get().getResources().getColor(R.color.textColor); // 停止位成功颜色：白色（正常）
        final int StopFailedColor = Color.RED; // 停止位失败颜色：红色，表示校验错误

        boolean checked ; // 是否启用奇偶校验，由界面配置传入
        int encoding; // 编码方式，由界面配置传入
        int uartLength; // UART数据位宽，取值范围[5, 9]，由界面配置传入
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        checked=this.checkeds[chIdx]; // 获取该通道的校验标志
        encoding=this.encodings[chIdx]; // 获取该通道的编码方式
        uartLength=this.bits[chIdx]; // 获取该通道的位宽

        SerialBusTxtStruct.UartStruct uart = null; // 当前正在构建的UART帧结构体
        for (int i = 0; i < bytes.limit(); i += 2) { // 每2字节为一组遍历（低字节=数据，高字节=标志）
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                //时间帧，UART不记录时间（与LIN/CAN等不同）
            } else { // 非时间帧，为数据帧
                if (uart == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getUartCurrSize() > 0) { // 缓冲区中已有历史帧
                        uart = serialTxtBuffer.getLastUartNode(); // 获取最后一帧，尝试续写
                        if (uart.isFlagFrameEnd()) { // 最后一帧已结束，不能续写
                            uart = serialTxtBuffer.getStruct(UartType); // 创建新的UART帧结构体
                            uart.Ch = Ch; // 设置通道标识
                        }
                    } else { // 缓冲区为空
                        uart = serialTxtBuffer.getStruct(UartType); // 创建新的UART帧结构体
                        uart.Ch = Ch; // 设置通道标识
                    }
                } else if (uart.isFlagFrameEnd()) { // 当前帧已结束
                    uart = serialTxtBuffer.getStruct(UartType); // 创建新的UART帧结构体
                    uart.Ch = Ch; // 设置通道标识
                }
                // 解析类型标志的低3位，确定校验+停止位状态
                switch (bytes.get(i + 1) & 0x7) { // 取bit[2:0]
                    case SerialBusTxtStruct.UartStruct.CHECK_OK_NoStop: { // 校验通过，无停止位
                        uart.Color = NoStopColor; // 设置黄色（异常）
                        serialTxtBuffer.addUartErrorData(); // 错误帧计数+1
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_Error_NoStop: { // 校验失败，无停止位
                        uart.Color = StopFailedColor; // 设置红色（错误）
                        serialTxtBuffer.addUartErrorData(); // 错误帧计数+1
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_OK_Stop: { // 校验通过，有停止位（正常）
                        uart.Color = StopSuccessColor; // 设置白色（正常）
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_Error_Stop: { // 校验失败，有停止位
                        uart.Color = StopFailedColor; // 设置红色（错误）
                        serialTxtBuffer.addUartErrorData(); // 错误帧计数+1
                    }
                    break;
                }

                if (uartLength <= 8) { // 位宽≤8位，单字节即可表示数据
                    uart.Data = bytes.get(i) & 0x000000FF; // 取低8位数据，掩码确保无符号
                    uart.setFlagFrameEnd(true); // 标记帧结束
                    serialTxtBuffer.put(UartType, uart); // 写入缓冲区
                    serialTxtBuffer.addUartTotalData(); // 总帧数+1
                } else { // 位宽>8位（9位），需要两次数据拼接
                    if (checked == false) { // 未启用奇偶校验，不进行拼接
                        uart.Data = bytes.get(i) & 0x000000FF; // 取低8位数据
                        uart.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(UartType, uart); // 写入缓冲区
                        serialTxtBuffer.addUartTotalData(); // 总帧数+1
                    } else { // 启用奇偶校验，需要两次拼接
                        if ((bytes.get(i + 1) & 0x08) == 0x08) { // bit3=1，为9位数据的高位部分
                            uart.Data = 0x000000FF & bytes.get(i); // 保存低8位数据
                        } else { // bit3=0，为9位数据的低位部分，与之前的高位拼接
                            uart.Data |= (0x00000100 & ((bytes.get(i)) << 8)); // 将当前字节左移8位拼接到Data的第9位
                            uart.setFlagFrameEnd(true); // 拼接完成，标记帧结束
                            serialTxtBuffer.put(UartType, uart); // 写入缓冲区
                            serialTxtBuffer.addUartTotalData(); // 总帧数+1
                        }
                    }
                }
            }
            // 更新最后一帧引用（用于续写判断）
            if (uart != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastUartNode() != null && !serialTxtBuffer.getLastUartNode().equals(uart)) { // 遍历到最后一个数据且当前帧与最后一帧不同
                serialTxtBuffer.setLastUartNode(uart); // 更新最后一帧引用
            }
        }
    }

    /**
     * 调试辅助方法：比较前后两个UART帧数据是否连续递增
     * 用于验证FPGA数据传输的正确性
     * @param befor 前一帧UART结构体，null时返回false
     * @param curr 当前帧UART结构体
     * @return true=数据连续，false=数据不连续
     */
    private boolean isdebug(SerialBusTxtStruct.UartStruct befor, SerialBusTxtStruct.UartStruct curr) {
        if (befor == null) return false; // 前帧为空，无法比较
        if (befor.Data == curr.Data) return true; // 数据相同，视为连续
        String bef = ""; // 前帧十六进制字符串
        if (befor.Data == 0xDD) { // 特殊值0xDD
            bef = "00000000" + Integer.toHexString(0); // 补零对齐
        } else {
            bef = "00000000" + Integer.toHexString(befor.Data + 17); // 前帧+17后转十六进制（测试偏移量）
        }
        bef = bef.substring(bef.length() - 2, bef.length()); // 取最后2位
        String cur = "00000000" + Integer.toHexString(curr.Data); // 当前帧转十六进制
        cur = cur.substring(cur.length() - 2, cur.length()); // 取最后2位
        if (cur.equals(bef)) { // 比较前后帧
            return true; // 匹配
        }
        Logger.i(TAG, "befor:0x" + bef + "cur:0x" + cur); // 不匹配时记录日志
        return false; // 不连续
    }

    /**
     * 解析LIN协议二进制数据为文本结构体
     * LIN数据格式：每2字节为一组，高字节bit7=1为时间帧，bit[3:0]为数据类型
     * 数据类型包括：ID校验通过、数据、校验和通过、ID校验失败、数据错误、校验和失败
     * 帧结束时检查触发条件标志
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void getLinStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_LIN; // LIN类型常量=2
        SerialBusTxtStruct.LinStruct lin = null; // 当前正在构建的LIN帧结构体
        for (int i = 0; i < bytes.limit(); i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间
            } else { // 非时间帧，为数据帧
                if (lin == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getLinCurrSize() > 0) { // 缓冲区中已有历史帧
                        lin = serialTxtBuffer.getLastLinNode(); // 获取最后一帧，尝试续写
                        if (lin.isFlagFrameEnd()) { // 最后一帧已结束
                            lin = serialTxtBuffer.getStruct(Type); // 创建新的LIN帧结构体
                            lin.Ch = Ch; // 设置通道标识
                            lin.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        }
                    } else { // 缓冲区为空
                        lin = serialTxtBuffer.getStruct(Type); // 创建新的LIN帧结构体
                        lin.Ch = Ch; // 设置通道标识
                        lin.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    }
                } else if (lin.isFlagFrameEnd()) { // 当前帧已结束
                    lin = serialTxtBuffer.getStruct(Type); // 创建新的LIN帧结构体
                    lin.Ch = Ch; // 设置通道标识
                    lin.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                }
                // 从缓存读取LIN协议版本类型（LIN1.x/LIN2.x）
                int serialsChan = TChan.findChanByValue(lin.Ch); // 获取通道整型值
                if (TChan.isSerial(serialsChan)) { // 确认是串口通道
                    lin.linType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + TChan.toSerialNumber(serialsChan)); // 读取LIN类型配置
                }

                // 解析数据类型，取高字节低4位
                int type = bytes.get(i + 1) & 0x0F; // 取bit[3:0]
                switch (type) {
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckYes: { // ID校验通过
                        lin.Id = bytes.get(i) & 0x000000FF; // 保存LIN ID，取值范围[0, 255]
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_DataYes: { // 数据校验通过
                        lin.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData(); // 追加数据字节（十六进制+空格）
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckSumYes: { // 校验和通过
                        lin.Check = bytes.get(i) & 0x000000FF; // 保存校验和，取值范围[0, 255]
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckNo: { // ID校验失败
                        lin.Id = bytes.get(i) & 0x000000FF; // 保存LIN ID
                        lin.Error = "Par"; // 标记奇偶校验错误
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_DataNo: { // 数据错误（停止位错误）
                        lin.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData(); // 仍追加数据
                        lin.Error = "STOP"; // 标记停止位错误
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckSumNo: { // 校验和失败
                        lin.Check = bytes.get(i) & 0x000000FF; // 保存校验和
                        lin.Error = "Chec"; // 标记校验和错误
                    }
                    break;
                }


                // 如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束标志
                    // bit5=1表示不符合触发条件
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) { // bit5=1
                        lin.Trigger = true; // 不符合触发条件
                    } else { // bit5=0
                        lin.Trigger = false; // 符合触发条件
                    }
                    // 防重复写入：仅当帧与缓冲区最后一帧不同，或缓冲区为空，或同一帧未结束时才写入
                    if ((serialTxtBuffer.getLinCurrSize() > 0 && !serialTxtBuffer.getLastLinNode().equals(lin)) // 与最后一帧不同
                            || (serialTxtBuffer.getLinCurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLastLinNode().equals(lin) && !lin.isFlagFrameEnd())) { // 同一帧但未结束
                        lin.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, lin); // 写入缓冲区

                    }
                }
            }

            // 更新最后一帧引用
            if (lin != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastLinNode() != null && !serialTxtBuffer.getLastLinNode().equals(lin)) { // 遍历到最后一个数据且帧不同
                serialTxtBuffer.setLastLinNode(lin); // 更新最后一帧引用
            }

        }
    }

    /**
     * 解析CAN协议二进制数据为文本结构体
     * CAN数据格式：每2字节为一组，高字节bit7=1为时间帧，bit[3:0]为数据类型
     * 支持标准ID（11位）和扩展ID（29位），DLC支持CAN FD格式（>8字节）
     * CRC宽度根据低2位自动判断（16/20/24位）
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void getCanStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_CAN; // CAN类型常量=3
        SerialBusTxtStruct.CanStruct can = null; // 当前正在构建的CAN帧结构体
        int specTimes = 0; // 本帧spec错误次数，取值范围[0, +∞)，用于判断帧错误
        int stdIdtimes = 0; // 标准ID接收次数计数，取值范围[0, 4]，用于拼接多字节ID
        for (int i = 0; i <= bytes.limit() - 2; i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间
            } else { // 非时间帧，为数据帧
                if (can == null) { // 当前无正在构建的帧
                    can = serialTxtBuffer.getLastCanNode(); // 获取最后一帧
                    if (can!=null) { // 最后一帧存在
                        if (can.isFlagFrameEnd()) { // 最后一帧已结束
                            can = serialTxtBuffer.getStruct(Type); // 创建新的CAN帧结构体
                            can.Ch = Ch; // 设置通道标识
                            can.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                            stdIdtimes = 0; // 重置ID接收计数
                        }else { // 最后一帧未结束，续写
                            stdIdtimes=can.stdIdtimes; // 恢复ID接收计数
                        }
                    } else { // 无历史帧
                        can = serialTxtBuffer.getStruct(Type); // 创建新的CAN帧结构体
                        can.Ch = Ch; // 设置通道标识
                        can.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        stdIdtimes = 0; // 初始化ID接收计数
                    }
                } else if (can.isFlagFrameEnd()) { // 当前帧已结束
                    can = serialTxtBuffer.getStruct(Type); // 创建新的CAN帧结构体
                    can.Ch = Ch; // 设置通道标识
                    can.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    stdIdtimes = 0; // 重置ID接收计数
                }

                // 解析数据类型
                int type = bytes.get(i + 1) & 0x0F; // 取bit[3:0]
                switch (type) {
                    case SerialBusTxtStruct.CanStruct.Can_30_stdId: { // 标准ID
                        stdIdtimes++; // ID接收计数+1
                        can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_STDID; // 标记为标准ID类型
                        if (stdIdtimes == 1) { // 第一次接收ID
                            can.ID = bytes.get(i) & 0x000000FF; // 保存ID低8位
                        } else { // 第二次接收ID（标准ID为11位，需2次拼接）
                            stdIdtimes++; // 计数再+1（实际stdIdtimes=2时进入此分支，变为3）
                            can.ID = ((can.ID << 3) | (0x00000007 & bytes.get(i))); // 左移3位拼接高3位
                        }
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_DLC: { // 数据长度码
                        int dlc = bytes.get(i) & 0x000000F; // 取低4位DLC，取值范围[0, 15]
                        if(dlc > 8) { // CAN FD格式，DLC>8需查表转换
                            switch (dlc){
                                case 9: dlc = 12;break; // DLC=9 → 12字节
                                case 10: dlc = 16;break; // DLC=10 → 16字节
                                case 11: dlc = 20;break; // DLC=11 → 20字节
                                case 12: dlc = 24;break; // DLC=12 → 24字节
                                case 13: dlc = 32;break; // DLC=13 → 32字节
                                case 14: dlc = 48;break; // DLC=14 → 48字节
                                case 15: dlc = 64;break; // DLC=15 → 64字节
                                default: // 其他值
                                    dlc = 8; // 默认8字节
                                    break;
                            }
                        }
                        can.DLC = dlc; // 保存DLC值
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_DATA: { // 数据字段
                        can.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData(); // 追加数据字节（十六进制+空格）
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_CRC: { // CRC校验码
                        if (can.CRC > 0) // 已有CRC高位数据
                            can.CRC = (can.CRC << 8) | (0xFF & bytes.get(i)); // 拼接CRC低字节
                        else // 首次接收CRC
                            can.CRC = bytes.get(i) & 0xFF; // 保存CRC首字节
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_Error: { // 错误帧
                        can.ErrorEnum = bytes.get(i) & SerialBusTxtStruct.CanStruct.Can_Error_MASK; // 取错误类型掩码
                        specTimes++; // spec错误计数+1
                        serialTxtBuffer.addCanSpaceFrame(); // CAN空帧/错误帧计数+1
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_extId: { // 扩展ID
                        can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_EXTID; // 标记为扩展ID类型
                        stdIdtimes++; // ID接收计数+1
                        switch (stdIdtimes) { // 根据接收次数拼接29位扩展ID
                            case 2: // 第2次接收
                            case 3: // 第3次接收
                                can.ID = (can.ID << 8 | (0x000000FF & bytes.get(i))); // 左移8位拼接
                                break;
                            case 4: // 第4次接收（最后5位）
                                can.ID = (can.ID << 5 | (0x0000001F & bytes.get(i))); // 左移5位拼接低5位
                                break;
                        }
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_Overload: { // 过载帧
                        // 过载帧暂不处理
                    }
                    break;
                    default: { // 其他类型（帧结束相关）
                        if ((type >> 3 & 0x01) == 0x01) { // bit3=1，帧结束标志
                            if ((type >> 0 & 0x01) == 0x01) { // bit0=1，有确认（ACK）
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Confirm; // 帧结束-有确认
                            } else { // bit0=0，无确认
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_NoConfirm; // 帧结束-无确认
                            }
                            if ((type >> 1 & 0x01) == 0x01) { // bit1=1，远程帧
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Data; // 帧结束-远程帧
                                if (can.TypeEnum == SerialBusTxtStruct.CanStruct.Type_STDID) { // 标准ID
                                    can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_STDREMOTEID; // 改为标准远程ID
                                } else if (can.TypeEnum == SerialBusTxtStruct.CanStruct.Type_EXTID) { // 扩展ID
                                    can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_EXTREMOTEID; // 改为扩展远程ID
                                }
                            } else { // bit1=0，数据帧
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Remote; // 帧结束-数据帧
                            }
                        }
                    }
                    break;
                }


                // 如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束
                    // bit5=1表示不符合触发条件
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) { // bit5=1
                        can.Trigger = true; // 不符合触发条件
                    } else { // bit5=0
                        can.Trigger = false; // 符合触发条件
                    }
                    // 空帧过滤：ID/DLC/Data/CRC全为0时跳过
                    if (can.ID == 0 && can.DLC == 0 && can.Data.equals("") && can.CRC == 0) // 全空帧
                        continue; // 跳过此帧
                    // 防重复写入
                    if ((serialTxtBuffer.getCanCurrSize() > 0 && !serialTxtBuffer.getLastCanNode().equals(can)) // 与最后一帧不同
                            || (serialTxtBuffer.getCanCurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLastCanNode().equals(can) && !can.isFlagFrameEnd())) { // 同一帧但未结束
                        // 根据CRC低2位判断CRC宽度并右移截取有效位
                        switch (can.CRC & 0x03) // 取CRC最低2位
                        {
                            case 0: // CRC-16
                                can.CRC >>=9; // 右移9位取有效16位
                                can.crc_w = 16; // CRC宽度=16位
                                break;
                            case 1: // CRC-20
                                can.CRC >>=7; // 右移7位取有效20位
                                can.crc_w = 20; // CRC宽度=20位
                                break;
                            case 2: // CRC-24
                                can.CRC >>=3; // 右移3位取有效24位
                                can.crc_w = 24; // CRC宽度=24位
                                break;
                        }
                        can.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, can); // 写入缓冲区
                    }
                    if (specTimes > 0) { // 本帧有spec错误
                        serialTxtBuffer.addCanErrorFrame(); // CAN错误帧计数+1
                        specTimes = 0; // 重置spec错误计数
                    }
                    serialTxtBuffer.addCanTotalFrame(); // CAN总帧数+1
                }

            }

            // 更新最后一帧引用
            if (can != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastCanNode() != null && !serialTxtBuffer.getLastCanNode().equals(can)) { // 遍历到最后一个数据且帧不同
                can.stdIdtimes=stdIdtimes; // 保存当前ID接收计数到帧结构体
                serialTxtBuffer.setLastCanNode(can); // 更新最后一帧引用
            }
        }
    }

    /**
     * 解析SPI协议二进制数据为文本结构体
     * SPI数据格式：每2字节为一组，高字节bit7=1为时间帧
     * bit[1:0]标识数据类型：01=数据，11=多字节拼接的最后一个数据
     * bit6=1为帧结束标志，bit5=1为触发条件标志
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void getSpiStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int bits; // SPI数据位宽
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        bits=this.bits[chIdx]; // 获取该通道的位宽

        final int Type = SerialBusStruct.SerialBusType_SPI; // SPI类型常量=4
        SerialBusTxtStruct.SpiStruct spi = null; // 当前正在构建的SPI帧结构体


        for (int i = 0; i < bytes.limit(); i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间
            } else { // 非时间帧，为数据帧
                if (spi == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getSpiCurrSize() > 0) { // 缓冲区中已有历史帧
                        spi = serialTxtBuffer.getLastSpiNode(); // 获取最后一帧，尝试续写
                        if (spi.isFlagFrameEnd()) { // 最后一帧已结束
                            spi = serialTxtBuffer.getStruct(Type); // 创建新的SPI帧结构体
                            spi.Ch = Ch; // 设置通道标识
                            spi.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        }
                    } else { // 缓冲区为空
                        spi = serialTxtBuffer.getStruct(Type); // 创建新的SPI帧结构体
                        spi.Ch = Ch; // 设置通道标识
                        spi.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    }
                } else if (spi.isFlagFrameEnd()) { // 当前帧已结束
                    spi = serialTxtBuffer.getStruct(Type); // 创建新的SPI帧结构体
                    spi.Ch = Ch; // 设置通道标识
                    spi.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                }


                // 解析SPI数据类型掩码
                int mask = bytes.get(i + 1) & SerialBusTxtStruct.SpiStruct.SPI_10_MULDataEnd; // 取bit[1:0]
                if (mask == SerialBusTxtStruct.SpiStruct.SPI_10_Data) { // 01=中间数据
                    spi.appendData(Tools.ByteToHexString(bytes.get(i), bits)).updateData(); // 追加数据（无空格）
                } else if (mask == SerialBusTxtStruct.SpiStruct.SPI_10_MULDataEnd) { // 11=多字节最后一个数据
                    spi.appendData(Tools.ByteToHexString(bytes.get(i), bits)).appendData(" ").updateData(); // 追加数据+空格（分隔不同字）
                }

                // 帧结束处理
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束
                    spi.Trigger = ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition); // bit5=触发条件
                    // 防重复写入
                    if ((serialTxtBuffer.getSpiCurrSize() > 0 && !serialTxtBuffer.getLastSpiNode().equals(spi)) // 与最后一帧不同
                            || (serialTxtBuffer.getSpiCurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLastSpiNode().equals(spi) && !spi.isFlagFrameEnd())) { // 同一帧但未结束
                        spi.setFlagFrameEnd(true); // 标记帧结束
                        spi.setFlagGroupDataEnd(true); // 标记组数据结束
                        if (serialTxtBuffer.getLastSpiNode() != null && !serialTxtBuffer.getLastSpiNode().isFlagGroupDataEnd()) { // 上一帧组数据未结束
                            spi.Ch = SerialBusTxtStruct.SpiStruct.FLAGShow_GroupData; // 标记为组数据续行显示
                        }
                        serialTxtBuffer.put(Type, spi); // 写入缓冲区
                    }
                }

            }
            // 更新最后一帧引用
            if (spi != null && i == bytes.limit() - 2 && (serialTxtBuffer.getLastSpiNode() != null) && !serialTxtBuffer.getLastSpiNode().equals(spi)) { // 遍历到最后一个数据且帧不同
                serialTxtBuffer.setLastSpiNode(spi); // 更新最后一帧引用
            }
        }

    }

    /**
     * 解析I2C协议二进制数据为文本结构体
     * I2C数据格式：每2字节为一组，高字节bit7=1为时间帧
     * bit3=1为帧结束，bit2区分重启/应答，bit[1:0]区分写地址/读地址/数据
     * I2C并非每帧都有帧结束标志（无停止位时FPGA不发送帧结束信号）
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void getI2cStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_I2C; // I2C类型常量=5
        final int Ack = Color.RED; // 无应答（NACK）显示红色
        SerialBusTxtStruct.I2cStruct i2c = null; // 当前正在构建的I2C帧结构体
        for (int i = 0; i <= bytes.limit() - 2; i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间
            } else { // 非时间帧，为数据帧
                if (i2c == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getI2cCurrSize() > 0) { // 缓冲区中已有历史帧
                        i2c = serialTxtBuffer.getLastI2cNode(); // 获取最后一帧，尝试续写
                        if (i2c.isFlagFrameEnd()) { // 最后一帧已结束
                            i2c = serialTxtBuffer.getStruct(Type); // 创建新的I2C帧结构体
                            i2c.Ch = Ch; // 设置通道标识
                            i2c.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        }
                    } else { // 缓冲区为空
                        i2c = serialTxtBuffer.getStruct(Type); // 创建新的I2C帧结构体
                        i2c.Ch = Ch; // 设置通道标识
                        i2c.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    }
                } else if (i2c.isFlagFrameEnd()) { // 当前帧已结束
                    i2c = serialTxtBuffer.getStruct(Type); // 创建新的I2C帧结构体
                    i2c.Ch = Ch; // 设置通道标识
                    i2c.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                }

                // 判断是否为帧结束
                if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_3_FrameEnd) == SerialBusTxtStruct.I2cStruct.I2C_3_FrameEnd) { // bit3=1，帧结束
                    if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_2_FrameEnd_Reboot) == SerialBusTxtStruct.I2cStruct.I2C_2_FrameEnd_Reboot) { // bit2=1，重启
                        i2c.Reboot = true; // 标记为重启帧
                        i2c.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, i2c); // 写入缓冲区
                    } else { // bit2=0，非重启
                        i2c.Reboot = false; // 标记为非重启帧
                    }
                } else { // 非帧结束，解析地址/数据
                    if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_2_Other_respond) == SerialBusTxtStruct.I2cStruct.I2C_2_Other_respond) { // bit2=1，有应答
                        i2c.Confirm = true; // 标记有应答（ACK）
                    } else { // bit2=0，无应答
                        i2c.Confirm = false; // 标记无应答（NACK）
                    }
                    int mask = bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_10_Other_Mask; // 取bit[1:0]
                    if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Write) { // 01=写地址
                        i2c.Addr = "W" + Tools.ByteToHexString(bytes.get(i)); // 格式如"W3F"
                    } else if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Read) { // 10=读地址
                        i2c.Addr = "R" + Tools.ByteToHexString(bytes.get(i)); // 格式如"R3F"
                    } else if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Data) { // 11=数据
                        i2c.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData(); // 追加数据字节+空格
                    }
                }

                // 帧结束处理
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) { // bit5=1
                        i2c.Trigger = true; // 不符合触发条件
                    } else { // bit5=0
                        i2c.Trigger = false; // 符合触发条件
                    }
                    // 防重复写入
                    if ((serialTxtBuffer.getI2cCurrSize() > 0 && !serialTxtBuffer.getLastI2cNode().equals(i2c)) // 与最后一帧不同
                            || (serialTxtBuffer.getI2cCurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLastI2cNode().equals(i2c) && !i2c.isFlagFrameEnd())) { // 同一帧但未结束
                        i2c.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, i2c); // 写入缓冲区
                    }
                }

            }
            // 更新最后一帧引用
            if (i2c != null && i == bytes.limit() - 2 && (serialTxtBuffer.getLastI2cNode() != null) && !serialTxtBuffer.getLastI2cNode().equals(i2c)) { // 遍历到最后一个数据且帧不同
                serialTxtBuffer.setLastI2cNode(i2c); // 更新最后一帧引用
            }
        }


    }

    /**
     * 解析ARINC429协议二进制数据为文本结构体
     * ARINC429数据格式：每2字节为一组，高字节bit7=1为时间帧
     * 数据类型包括：Label、Data1、Data2、校验成功、校验失败、帧错误
     * 支持3种显示格式：Label+DATA、Label+DATA+SSM、Label+DATA+SSM+SDI
     * 32位数据由3个字节拼接：{data3[6:0], data2[7:0], data1[7:0]}
     *
     * @param Ch 通道名称（如"S1"）
     * @param format 显示格式（Arinc429_Format_LabelDATA / Arinc429_Format_LDSSM / Arinc429_Format_LDSSMSDI）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void get429Struct(String Ch, int format, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int encode; // 编码方式
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        encode=this.encodings[chIdx]; // 获取该通道的编码方式

        final int Type = SerialBusStruct.SerialBusType_429; // ARINC429类型常量=6
        SerialBusTxtStruct.Arinc429Struct arinc429 = null; // 当前正在构建的ARINC429帧结构体
        for (int i = 0; i <= bytes.limit() - 2; i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间

            } else { // 非时间帧，为数据帧
                if (arinc429 == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getArinc429CurrSize() > 0) { // 缓冲区中已有历史帧
                        arinc429 = serialTxtBuffer.getLast429Node(); // 获取最后一帧，尝试续写
                        if (arinc429.isFlagFrameEnd()) { // 最后一帧已结束
                            arinc429 = serialTxtBuffer.getStruct(Type); // 创建新的429帧结构体
                            arinc429.Ch = Ch; // 设置通道标识
                            arinc429.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        }
                    } else { // 缓冲区为空
                        arinc429 = serialTxtBuffer.getStruct(Type); // 创建新的429帧结构体
                        arinc429.Ch = Ch; // 设置通道标识
                        arinc429.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    }
                } else if (arinc429.isFlagFrameEnd()) { // 当前帧已结束
                    arinc429 = serialTxtBuffer.getStruct(Type); // 创建新的429帧结构体
                    arinc429.Ch = Ch; // 设置通道标识
                    arinc429.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                }


                // 协议解析，取高字节低4位
                int type = bytes.get(i + 1) & 0x0F; // 取bit[3:0]
                switch (type) {
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Label: { // Label字段
                        arinc429.Label = bytes.get(i) & 0x00FF; // 保存Label，取值范围[0, 255]
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Data1: { // 数据字节1（最低字节）
                        arinc429.setData1(bytes.get(i) & 0x00FF); // 保存Data1
                        arinc429.Times++; // 数据接收计数+1
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Data2: { // 数据字节2（中间字节）
                        arinc429.setData2(bytes.get(i) & 0x00FF); // 保存Data2
                        arinc429.Times++; // 数据接收计数+1
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckSuccess: { // 校验成功+数据字节3
                        arinc429.setData3(bytes.get(i) & 0x00FF); // 保存Data3（最高字节）
                        arinc429.Times++; // 数据接收计数+1
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckError: { // 校验失败+数据字节3
                        arinc429.setData3(bytes.get(i) & 0x00FF); // 保存Data3
                        arinc429.Error = "Par"; // 标记奇偶校验错误
                        arinc429.setErrorNo(type); // 保存错误类型编号
                        arinc429.Times++; // 数据接收计数+1
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError: { // 帧错误
                        arinc429.Error = "Frm"; // 标记帧错误
                        arinc429.setErrorNo(type); // 保存错误类型编号
                    }
                    break;
                }


                // 如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) { // bit5=1
                        arinc429.Trigger = true; // 不符合触发条件
                    } else { // bit5=0
                        arinc429.Trigger = false; // 符合触发条件
                    }
                    // 防重复写入
                    if ((serialTxtBuffer.getArinc429CurrSize() > 0 &&
                            !serialTxtBuffer.getLast429Node().equals(arinc429)) // 与最后一帧不同
                            || (serialTxtBuffer.getArinc429CurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLast429Node().equals(arinc429) && !arinc429.isFlagFrameEnd())) { // 同一帧但未结束
                        // 根据显示格式处理数据
                        switch (format) {
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LabelDATA: { // 格式1：Label+DATA（不含SSM/SDI）
                                if (arinc429.Times == 3) { // 3个数据字节全部接收完成
                                    // 拼接32位数据：{data3[6:0], data2, data1}
                                    arinc429.appendData(getEncoding(encode, (byte) (0x7F & arinc429.getData3()), 2 * 4)).appendData(" "); // data3取低7位，转2位十六进制
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" "); // data2转2位十六进制
                                    arinc429.appendData(getEncoding(encode, (byte) arinc429.getData1(), 2 * 4)).appendData(" "); // data1转2位十六进制

                                    arinc429.updateData(); // 提交数据更新
                                } else { // 数据不完整
                                    arinc429.Data = ""; // 清空数据
                                }
                                arinc429.SDI = "XX"; // SDI未知，显示"XX"
                                arinc429.SSM = "XX"; // SSM未知，显示"XX"

                            }
                            break;
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LDSSM: { // 格式2：Label+DATA+SSM
                                int tem = (arinc429.getData3() >> 5) & 0x03; // 从data3提取SSM（bit[8:9]），取值范围[0, 3]
                                arinc429.SSM = getEncoding(ICharacterEncoding.Binary, tem, 2); // SSM转2位二进制
                                arinc429.SDI = "XX"; // SDI未知
                                if (arinc429.Times == 3) { // 3个数据字节全部接收完成
                                    // 拼接数据：{data3[4:0], data2, data1}（去掉SSM的2位）
                                    arinc429.appendData(getEncoding(encode, (byte) (0x1F & arinc429.getData3()), 2 * 4)).appendData(" "); // data3取低5位
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" "); // data2
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData1()), 2 * 4)).appendData(" "); // data1
                                    arinc429.updateData(); // 提交数据更新
                                } else { // 数据不完整
                                    arinc429.Data = ""; // 清空数据
                                    arinc429.SSM = ""; // 清空SSM
                                }

                            }
                            break;
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LDSSMSDI: { // 格式3：Label+DATA+SSM+SDI
                                int tem = arinc429.getData1() & 0x03; // 从data1提取SDI（bit[1:0]），取值范围[0, 3]
                                arinc429.SDI = getEncoding(ICharacterEncoding.Binary, (tem), 2); // SDI转2位二进制
                                tem = (arinc429.getData3() >> 5) & 0x03; // 从data3提取SSM（bit[8:9]），取值范围[0, 3]
                                arinc429.SSM = getEncoding(ICharacterEncoding.Binary, (tem), 2); // SSM转2位二进制
                                if (arinc429.Times == 3) { // 3个数据字节全部接收完成
                                    // 拼接数据：{data3[4:0], data2[7:0], data1[7:2]}（去掉SSM 2位和SDI 2位）
                                    int tran = ((arinc429.getData3() & 0x1f) << 16 | (arinc429.getData2() & 0xFF) << 8 | (arinc429.getData1() & 0xFF)) >> 2; // 右移2位去掉SDI
                                    arinc429.setData1(tran & 0xFF); // 重新设置data1
                                    arinc429.setData2((tran >> 8) & 0xFF); // 重新设置data2
                                    arinc429.setData3((tran >> 16) & 0xFF); // 重新设置data3

                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData3()), 2 * 4)).appendData(" "); // data3
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" "); // data2
                                    arinc429.appendData(getEncoding(encode, (byte) ((arinc429.getData1())), 2 * 4)).appendData(" "); // data1
                                    arinc429.updateData(); // 提交数据更新
                                } else { // 数据不完整
                                    arinc429.Data = ""; // 清空数据
                                    arinc429.SSM = ""; // 清空SSM
                                }
                                if (arinc429.Times <= 0) { // 未接收到数据
                                    arinc429.SDI = ""; // 清空SDI
                                }
                            }
                            break;
                        }
                        serialTxtBuffer.addArinc429TotalFrame(); // ARINC429总帧数+1
                        if ((arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError) || // 帧错误
                                (arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckError)) { // 校验错误
                            serialTxtBuffer.addArinc429ErrorFrame(); // ARINC429错误帧数+1
                        }
                        if (arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError && !arinc429.Trigger) // 帧错误且符合触发条件
                            arinc429.Data = ""; // 清空数据显示
                        arinc429.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, arinc429); // 写入缓冲区
                    }
                }
            }

            // 更新最后一帧引用
            if (arinc429 != null && i == bytes.limit() - 2 && serialTxtBuffer.getLast429Node() != null && !serialTxtBuffer.getLast429Node().equals(arinc429)) { // 遍历到最后一个数据且帧不同
                serialTxtBuffer.setLast429Node(arinc429); // 更新最后一帧引用
            }

        }

    }

    /**
     * 解析MIL-STD-1553B协议二进制数据为文本结构体
     * 1553B数据格式：每2字节为一组，高字节bit7=1为时间帧
     * 数据类型包括：远程地址、命令/状态字（2字节拼接）、数据字（2字节拼接）
     * 错误类型：曼彻斯特编码错误、校验错误
     *
     * @param Ch 通道名称（如"S1"）
     * @param serialTxtBuffer 文本缓冲区管理器
     * @param bytes FPGA原始二进制数据
     */
    private void get1553bStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int encoding; // 编码方式
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1; // 计算通道索引，取值范围[0, MaxSerial-1]
        encoding=this.encodings[chIdx]; // 获取该通道的编码方式

        final int Type = SerialBusStruct.SerialBusType_1553B; // 1553B类型常量=7
        SerialBusTxtStruct.MilSTD1553bStruct m1553b = null; // 当前正在构建的1553B帧结构体
        for (int i = 0; i <= bytes.limit() - 2; i += 2) { // 每2字节为一组遍历
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) { // bit7=1，为时间帧
                getCurrTotalTime(serialTxtBuffer, bytes, i); // 计算并更新累积时间
            } else { // 非时间帧，为数据帧
                if (m1553b == null) { // 当前无正在构建的帧
                    if (serialTxtBuffer.getMilstd1553bCurrSize() > 0) { // 缓冲区中已有历史帧
                        m1553b = serialTxtBuffer.getLast1553bNode(); // 获取最后一帧，尝试续写
                        if (m1553b.isFlagFrameEnd()) { // 最后一帧已结束
                            m1553b = serialTxtBuffer.getStruct(Type); // 创建新的1553B帧结构体
                            m1553b.Ch = Ch; // 设置通道标识
                            m1553b.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                        }
                    } else { // 缓冲区为空
                        m1553b = serialTxtBuffer.getStruct(Type); // 创建新的1553B帧结构体
                        m1553b.Ch = Ch; // 设置通道标识
                        m1553b.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                    }
                } else if (m1553b.isFlagFrameEnd()) { // 当前帧已结束
                    m1553b = serialTxtBuffer.getStruct(Type); // 创建新的1553B帧结构体
                    m1553b.Ch = Ch; // 设置通道标识
                    m1553b.CurTime = serialTxtBuffer.getTotalTime(); // 设置当前时间戳
                }

                // 解析数据类型
                int type = bytes.get(i + 1) & 0x0F; // 取bit[3:0]
                switch (type) {
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_RemoteAddr: { // 远程地址
                        m1553b.RAddr = getEncoding(encoding, bytes.get(i), 2 * 4); // 按编码方式转换远程地址
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_CommandStateByte6: { // 命令/状态字高6位
                        m1553b.setTemData(bytes.get(i) & 0x0000003F); // 保存低6位临时数据
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_CommandStateByte5: { // 命令/状态字低5位，与高6位拼接
                        m1553b.setTemData(((m1553b.getTemData() << 5) & 0xFFFFFFE0) | (bytes.get(i) & 0x0000001F)); // 拼接为11位命令/状态字
                        // 将命令/状态字按编码方式转为两个字节显示
                        m1553b.appendData(getEncoding(encoding, (m1553b.getTemData() >> 8), 2 * 4)); // 高字节
                        m1553b.appendData(" "); // 空格分隔
                        m1553b.appendData(getEncoding(encoding, (m1553b.getTemData() & 0xFF), 2 * 4)); // 低字节
                        m1553b.appendData(" ").updateData(); // 空格+提交更新

                        m1553b.Type = "C/S"; // 标记为命令/状态字类型
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_Data1: { // 数据字高字节
                        m1553b.setTemData(bytes.get(i) & 0x000000FF); // 保存数据字高字节
                        m1553b.appendData(getEncoding(encoding, bytes.get(i), 2 * 4)); // 按编码方式转换
                        m1553b.appendData(" "); // 空格分隔
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_Data2: { // 数据字低字节
                        m1553b.appendData(getEncoding(encoding, bytes.get(i), 2 * 4)); // 按编码方式转换
                        m1553b.appendData(" ").updateData(); // 空格+提交更新
                        m1553b.Type = "Data"; // 标记为数据字类型

                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_ManchesterEncodingError: { // 曼彻斯特编码错误
                        m1553b.Error = "M-ch"; // 标记曼彻斯特编码错误
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_FrameEnd_CheckError: { // 帧结束+校验错误
                        m1553b.Error = "Par"; // 标记奇偶校验错误
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_FrameEnd_CheckSuccess: { // 帧结束+校验成功
                        // 校验成功，无需设置错误标记
                    }
                    break;
                }

                // 如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) { // bit6=1，帧结束
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) { // bit5=1
                        m1553b.Trigger = true; // 不符合触发条件
                    } else { // bit5=0
                        m1553b.Trigger = false; // 符合触发条件
                    }
                    // 防重复写入
                    if ((serialTxtBuffer.getMilstd1553bCurrSize() > 0 &&
                            !serialTxtBuffer.getLast1553bNode().equals(m1553b)) // 与最后一帧不同
                            || (serialTxtBuffer.getMilstd1553bCurrSize() == 0) // 缓冲区为空
                            || (serialTxtBuffer.getLast1553bNode().equals(m1553b) && !m1553b.isFlagFrameEnd())) { // 同一帧但未结束
                        m1553b.setFlagFrameEnd(true); // 标记帧结束
                        serialTxtBuffer.put(Type, m1553b); // 写入缓冲区
                    }
                }

            }

            // 更新最后一帧引用
            if (m1553b != null && i == bytes.limit() - 2 && serialTxtBuffer.getLast1553bNode() != null && !serialTxtBuffer.getLast1553bNode().equals(m1553b)) { // 遍历到最后一个数据且帧不同
                serialTxtBuffer.setLast1553bNode(m1553b); // 更新最后一帧引用
            }
        }
    }

    /**
     * 计算当前累积总时间
     * FPGA时间戳为15位无符号整数（0-32767），会周期性溢出回绕
     * 本方法处理回绕问题，将FPGA相对时间转换为绝对累积时间
     * 最大时间值599999999对应99分59秒999毫秒990微秒
     *
     * @param serialTxtBuffer 文本缓冲区，用于读写上次时间戳和总时间
     * @param bytes FPGA原始数据缓冲区
     * @param i 当前数据在缓冲区中的偏移量
     * @return 当前累积总时间，取值范围[0, 599999999]
     */
    private long getCurrTotalTime(SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes, int i) {
        /// 599999999代表99m59s999ms990us，即最大可显示时间
        final long MaxTime = 599999999; // 最大时间值

        long currTotalTime = 0; // 计算后的当前累积时间
        int armCurrTime = (((bytes.get(i + 1) & 0x7F) << 8) | (bytes.get(i) & 0xFF)) & 0x7FFF; // 提取15位FPGA时间戳，取值范围[0, 32767]
        long totalTime = serialTxtBuffer.getTotalTime(); // 获取之前累积的总时间
        // 处理FPGA时间戳回绕：当前时间+32768减去上次时间的余数，再取模32768，加上之前的总时间
        currTotalTime = (((32768 + armCurrTime) - (totalTime % 0x7FFF)) % 0x7FFF) + totalTime; // 累积时间计算公式
        serialTxtBuffer.setArmLastTime(armCurrTime); // 保存当前FPGA时间戳
        if (currTotalTime >= MaxTime) currTotalTime = currTotalTime % MaxTime; // 超过最大值时取模回绕
        serialTxtBuffer.setTotalTime(currTotalTime); // 保存新的累积总时间
        return currTotalTime; // 返回当前累积时间
    }

    /**
     * 调试工具方法：将ByteBuffer全部内容转为十六进制字符串
     * @param bytes 待转换的ByteBuffer
     * @return 十六进制字符串，格式如"3F 2A 1B "
     */
    public static String getDebugBytesToString(ByteBuffer bytes) {
        StringBuilder sb = new StringBuilder(); // 构建字符串
        for (int i = 0; i < bytes.limit(); i++) { // 遍历所有字节
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" "); // 每个字节转十六进制+空格
        }
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 调试工具方法：将ByteBuffer指定范围内容转为十六进制字符串
     * @param bytes 待转换的ByteBuffer
     * @param start 起始偏移量，取值范围[0, bytes.limit()-1]
     * @param len 转换长度，取值范围[1, bytes.limit()-start]
     * @return 十六进制字符串
     */
    public static String getDebugBytesToString(ByteBuffer bytes, int start, int len) {
        StringBuilder sb = new StringBuilder(); // 构建字符串
        for (int i = start; i < start + len; i++) { // 遍历指定范围
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" "); // 每个字节转十六进制+空格
        }
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 调试工具方法：从指定索引开始将ByteBuffer剩余内容转为十六进制字符串
     * @param index 起始索引，取值范围[0, bytes.limit()-1]
     * @param bytes 待转换的ByteBuffer
     * @return 十六进制字符串
     */
    public static String getDebugBytesToString(int index, ByteBuffer bytes) {
        StringBuilder sb = new StringBuilder(); // 构建字符串
        for (int i = index; i < bytes.limit(); i++) { // 从index开始遍历
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" "); // 每个字节转十六进制+空格
        }
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 获取扩展ASCII字符
     * 扩展ASCII范围为0xA1-0xFF，0xAD为软连字符返回空字符
     * @param id 字符编码值，取值范围[0, 255]
     * @return 对应的字符，0xAD或不在扩展范围内返回'\0'
     */
    static char getExtASCII(int id) {
        char val = '\0'; // 默认返回空字符
        if (id >= 0xA1 && id <= 0xFF) { // 扩展ASCII范围
            switch (id) {
                case 0xAD: // 软连字符
                    val = '\0'; // 返回空字符
                    break;
                default: // 其他扩展ASCII字符
                    val = (char) id; // 直接转换
                    break;
            }
        }
        return val; // 返回字符
    }

    /**
     * 通用编码转换方法：将整数值按指定编码方式转换为字符串
     * 支持5种编码：ASCII、二进制、十进制、十六进制、八进制
     * 输出字符串截取后width个字符，确保对齐
     *
     * @param Coding 编码方式，取值范围：ICharacterEncoding常量（ASCII/Binary/Decimal/Hex/Octal）
     * @param id 待转换的整数值
     * @param bitWidth 数据位宽，用于计算输出字符宽度，取值范围[1, 64]
     * @return 编码后的字符串
     */
    public static String getEncoding(int Coding, int id, int bitWidth) {
        String s = ""; // 转换结果字符串
        int width = 1; // 输出字符宽度，ASCII默认1
        Long data = id | (1L << 63); // 将最高位置1确保toBinaryString/toHexString等不丢失前导零
        switch (Coding) { // 根据编码方式转换
            case ICharacterEncoding.ASCII: { // ASCII编码
                char s1 = ((id >= 32) && (id <= 126)) ? (char) id : '\0'; // 可打印ASCII范围[32, 126]
                if (id >= 0xA1) { // 扩展ASCII范围
                    s1 = getExtASCII(id); // 获取扩展ASCII字符
                }
                if (s1 == '\0') { // 不可打印字符
                    s = "..........."; // 显示为点号占位
                    width = 3; // 宽度3
                } else { // 可打印字符
                    s = String.valueOf(s1); // 转为字符串
                    width = 1; // 宽度1
                }
            }
            break;
            case ICharacterEncoding.Binary: { // 二进制编码
                s = Long.toBinaryString(data); // 转为二进制字符串
                width = bitWidth; // 宽度=位宽
            }
            break;
            case ICharacterEncoding.Decimal: { // 十进制编码
                s = Long.toString(data); // 转为十进制字符串
                width = bitWidth; // 宽度=位宽
            }
            break;
            case ICharacterEncoding.Hex: { // 十六进制编码
                s = Long.toHexString(data); // 转为十六进制字符串
                width = (int) Math.ceil(bitWidth / 4.0f); // 宽度=位宽/4向上取整（每4位=1个十六进制字符）
            }
            break;
            case ICharacterEncoding.Octal: { // 八进制编码
                s = Long.toOctalString(data); // 转为八进制字符串
                width = (int) Math.ceil(bitWidth / 4.0f); // 宽度=位宽/4向上取整
            }
            break;
        }
        if (Coding == ICharacterEncoding.ASCII) { // ASCII编码
            return s.substring(s.length() - width, s.length()); // 截取后width个字符
        } else { // 其他编码
            // 取字串s的后width个字符输出，并转大写
            return s.toUpperCase().substring(s.length() - width, s.length()); // 截取后width个字符并转大写
        }
    }

    /**
     * 测试入口方法，用于验证ByteBuffer的put操作
     * 非生产代码，仅用于开发调试
     * @param arg 命令行参数（未使用）
     */
    @SuppressLint("DefaultLocale") // 抑制DefaultLocale lint警告
    public static void main(String[] arg) {
        ByteBuffer bb1 = ByteBuffer.allocate(9); // 分配9字节缓冲区
        ByteBuffer bb2 = ByteBuffer.allocate(6); // 分配6字节缓冲区
        bb1.put((byte) 1); // 写入字节1
        bb1.put((byte) 2); // 写入字节2
        bb1.put((byte) 3); // 写入字节3
        bb2.put((byte) 4); // 写入字节4
        bb2.put((byte) 5); // 写入字节5
        bb2.put((byte) 6); // 写入字节6

        bb1.position(0); // 重置position到0
        bb1.limit(3); // 设置limit为3
        System.out.println("bb1.value(0):" + bb1.get(0)); // 打印bb1第0个字节
        bb2.position(0); // 重置position到0
        bb2.limit(3); // 设置limit为3
        bb1.put(bb2); // 将bb2写入bb1
        System.out.println("bb1.value(1):" + bb1.get(1)); // 打印bb1第1个字节（验证put操作）
    }
}
