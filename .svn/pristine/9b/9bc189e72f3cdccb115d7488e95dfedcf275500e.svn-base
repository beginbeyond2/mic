package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialTxtBuffer {
    private static final String TAG = "SerialTxtBuffer";

    private static final Object lock = new Object();
    /**
     * 是否打开S1 S2功能
     */
    private static boolean openS1AndS2 = false;

    //region 缓冲区链表与方法
    private LinkedBlockingQueue<ByteBuffer> buffer = new LinkedBlockingQueue<>();

    public void putBytesToQueue(ByteBuffer bytes) throws InterruptedException {
        ByteBuffer oBytes = ByteBuffer.allocate(bytes.limit());
        bytes.position(0);
        oBytes.put(bytes);
        buffer.add(oBytes);
//           Logger.i(TAG," bytes:"+SerialBusTxtStructParse.getDebugBytesToString(oBytes));
    }

    public LinkedBlockingQueue<ByteBuffer> getBuffer() {
        return buffer;
    }
    //endregion

    //region 文本解析属�??

    //uart总共接收数据
    private long uartTotalData = 0;
    //uart接收错误数据
    private long uartErrorData = 0;

    /**
     * can 总帧
     */
    private long canTotalFrame = 0;
    /**
     * can 空帧
     */
    private long canSpaceFrame = 0;
    /**
     * can 错误�?
     */
    private long canErrorFrame = 0;

    /**
     * arinc429 总帧
     */
    private long arinc429TotalFrame = 0;
    /**
     * arinc429 错误�?
     */
    private long arinc429ErrorFrame = 0;

    //�?后一次时间帧
    private int armLastTime = 0;
    private long totalTime = 0;

    private SerialBusTxtStruct.UartStruct lastUartNode = null;
    private SerialBusTxtStruct.Arinc429Struct last429Node = null;
    private SerialBusTxtStruct.CanStruct lastCanNode = null;
    private SerialBusTxtStruct.SpiStruct lastSpiNode = null;
    private SerialBusTxtStruct.I2cStruct lastI2cNode = null;
    private SerialBusTxtStruct.LinStruct lastLinNode = null;
    private SerialBusTxtStruct.MilSTD1553bStruct last1553bNode = null;

    private LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bListScreen = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListScreen = null;

    private LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bListTotal = null;
    private LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListTotal = null;

    private static LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartS1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429S1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canS1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cS1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linS1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bS1S2ListScreen = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiS1S2ListScreen = null;

    private static LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartS1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429S1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canS1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cS1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linS1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> milstd1553bS1S2ListTotal = null;
    private static LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiS1S2ListTotal = null;

    private static int BUFFER_SIZE = 4096;

    private int uartBufferScreenSize = 2814, arinc429BufferScreenSize = 20, canBufferScreenSize = 20, i2cBufferScreenSize = 20,
            lineBufferScreenSize = 20, milstd1553bBufferScreenSize = 20, spiBufferScreenSize = 20;
    private int uartCurrScreenSize = 0, arinc429CurrScreenSize = 0, canCurrScreenSize = 0, i2cCurrScreenSize = 0,
            linCurrScreenSize = 0, milstd1553bCurrScreenSize = 0, spiCurrScreenSize = 0;
    private int uartBufferSize = BUFFER_SIZE * 40, arinc429BufferSize = BUFFER_SIZE * 40, canBufferSize = BUFFER_SIZE * 40, i2cBufferSize = BUFFER_SIZE * 40,
            lineBufferSize = BUFFER_SIZE * 40, milstd1553bBufferSize = BUFFER_SIZE * 40, spiBufferSize = BUFFER_SIZE * 40;
    private int uartCurrSize = 0, arinc429CurrSize = 0, canCurrSize = 0, i2cCurrSize = 0,
            linCurrSize = 0, milstd1553bCurrSize = 0, spiCurrSize = 0;

    private static int uartS1S2CurrScreenSize = 0, arinc429S1S2CurrScreenSize = 0, canS1S2CurrScreenSize = 0, i2cS1S2CurrScreenSize = 0, linS1S2CurrScreenSize = 0,
            milstd1553bS1S2CurrScreenSize = 0, spiS1S2CurrScreenSize = 0;
    private static int uartS1S2CurrSize = 0, arinc429S1S2CurrSize = 0, canS1S2CurrSize = 0, i2cS1S2CurrSize = 0, linS1S2CurrSize = 0,
            milstd1553bS1S2CurrSize = 0, spiS1S2CurrSize = 0;
    //endregion

    public SerialTxtBuffer() {
        this.uartBufferSize = BUFFER_SIZE * 40;
        this.arinc429BufferSize = BUFFER_SIZE * 40;
        this.canBufferSize = BUFFER_SIZE * 40;
        this.i2cBufferSize = BUFFER_SIZE * 40;
        this.lineBufferSize = BUFFER_SIZE * 40;
        this.milstd1553bBufferSize = BUFFER_SIZE * 40;
        this.spiBufferSize = BUFFER_SIZE * 40;

        uartListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>());
        arinc429ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>());
        canListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>());
        i2cListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>());
        linListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>());
        milstd1553bListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>());
        spiListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>());

        uartListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>());
        arinc429ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>());
        canListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>());
        i2cListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>());
        linListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>());
        milstd1553bListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>());
        spiListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>());

        uartS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>());
        arinc429S1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>());
        canS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>());
        i2cS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>());
        linS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>());
        milstd1553bS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>());
        spiS1S2ListScreen = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>());

        uartS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>());
        arinc429S1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>());
        milstd1553bS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>());
        canS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>());
        i2cS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>());
        linS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>());
        spiS1S2ListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>());
    }

    public void setOpenS1S2(boolean isOpen, int serialbusStruct_SerialBusType) {
        synchronized (lock) {
            if (openS1AndS2 == isOpen) return;
            openS1AndS2 = isOpen;
            if (!openS1AndS2) {
                if (uartS1S2ListTotal != null) uartS1S2ListTotal.clear();
                if (arinc429S1S2ListTotal != null) arinc429S1S2ListTotal.clear();
                if (canS1S2ListTotal != null) canS1S2ListTotal.clear();
                if (i2cS1S2ListTotal != null) i2cS1S2ListTotal.clear();
                if (linS1S2ListTotal != null) linS1S2ListTotal.clear();
                if (milstd1553bS1S2ListTotal != null) milstd1553bS1S2ListTotal.clear();
                if (spiS1S2ListTotal != null) spiS1S2ListTotal.clear();

                if (uartS1S2ListScreen != null) uartS1S2ListScreen.clear();
                if (arinc429S1S2ListScreen != null) arinc429S1S2ListScreen.clear();
                if (canS1S2ListScreen != null) canS1S2ListScreen.clear();
                if (i2cS1S2ListScreen != null) i2cS1S2ListScreen.clear();
                if (linS1S2ListScreen != null) linS1S2ListScreen.clear();
                if (milstd1553bS1S2ListScreen != null) milstd1553bS1S2ListScreen.clear();
                if (spiS1S2ListScreen != null) spiS1S2ListScreen.clear();

                uartS1S2CurrScreenSize = 0;
                arinc429S1S2CurrScreenSize = 0;
                canS1S2CurrScreenSize = 0;
                i2cS1S2CurrScreenSize = 0;
                linS1S2CurrScreenSize = 0;
                milstd1553bS1S2CurrScreenSize = 0;
                spiS1S2CurrScreenSize = 0;

                uartS1S2CurrSize = 0;
                arinc429S1S2CurrSize = 0;
                canS1S2CurrSize = 0;
                i2cS1S2CurrSize = 0;
                linS1S2CurrSize = 0;
                milstd1553bS1S2CurrSize = 0;
                spiS1S2CurrSize = 0;
            }
        }
    }

    public SerialTxtBuffer(int uartBufferSize, int arinc429BufferSize, int canBufferSize,
                           int i2cBufferSize, int lineBufferSize, int milstd1553bBufferSize, int spiBufferSize) {
//        if (uartBufferSize < 256 * 10) uartBufferSize = 256 * 10;
//        if (arinc429BufferSize < 500) arinc429BufferSize = 500;
//        if (canBufferSize < 500) canBufferSize = 500;
//        if (i2cBufferSize < 500) i2cBufferSize = 500;
//        if (lineBufferSize < 500) lineBufferSize = 500;
//        if (milstd1553bBufferSize < 500) milstd1553bBufferSize = 500;
//        if (spiBufferSize < 500) spiBufferSize = 500;
//        this.uartBufferSize = uartBufferSize;
//        this.arinc429BufferSize = arinc429BufferSize;
//        this.canBufferSize = canBufferSize;
//        this.i2cBufferSize = i2cBufferSize;
//        this.lineBufferSize = lineBufferSize;
//        this.milstd1553bBufferSize = milstd1553bBufferSize;
//        this.spiBufferSize = spiBufferSize;
//
//        uartListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.UartStruct>());
//        arinc429ListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct>());
//        canListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.CanStruct>());
//        i2cListTotal = (new LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct>());
//        linListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.LinStruct>());
//        milstd1553bListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct>());
//        spiListTotal =  (new LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct>());
    }

    public boolean isSerialsSelected(SerialBusTxtStruct.ISerialBusTxtCSV o) {
        String chName = o.getCh();
        HashMap<String, Boolean> map = SerialsTxtMixUtils.getInstance().getSerialsCheckMap();
//        Logger.d("limh", "getSerialsCheckMap= " + map.toString());
        //选中的组合才塞数据
        return !map.isEmpty() && map.containsKey(chName) && Boolean.TRUE.equals(map.get(chName));
    }

    public void put(int serialBusType, Object o) {
        switch (serialBusType) {
            case SerialBusStruct.SerialBusType_UART: {
                lastUartNode = (SerialBusTxtStruct.UartStruct) o;
                synchronized (lock) {
                    while (uartCurrSize >= uartBufferSize) {
                        uartCurrSize--;
                        uartListTotal.poll();
                    }
                    uartCurrSize++;
                    uartListTotal.add((SerialBusTxtStruct.UartStruct) o);


                    while (uartCurrScreenSize >= uartBufferScreenSize) {
                        uartCurrScreenSize--;
                        uartListScreen.poll();
                    }
                    uartCurrScreenSize++;
                    uartListScreen.add((SerialBusTxtStruct.UartStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.UartStruct) o)) {
                    synchronized (lock) {
                        while (uartS1S2CurrSize >= uartBufferSize * 2) {
                            uartS1S2CurrSize--;
                            uartS1S2ListTotal.poll();
                        }
                        uartS1S2CurrSize++;
                        uartS1S2ListTotal.add((SerialBusTxtStruct.UartStruct) o);
                    }
                    synchronized (lock) {
                        while (uartS1S2CurrScreenSize >= uartBufferScreenSize * 2) {
                            uartS1S2CurrScreenSize--;
                            uartS1S2ListScreen.poll();
                        }
                        uartS1S2CurrScreenSize++;
                        uartS1S2ListScreen.add((SerialBusTxtStruct.UartStruct) o);
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_429: {
                last429Node = (SerialBusTxtStruct.Arinc429Struct) o;
                synchronized (lock) {
                    while (arinc429CurrSize >= arinc429BufferSize) {
                        arinc429CurrSize--;
                        arinc429ListTotal.poll();
                    }
                    arinc429CurrSize++;
                    arinc429ListTotal.add((SerialBusTxtStruct.Arinc429Struct) o);

                    while (arinc429CurrScreenSize >= arinc429BufferScreenSize) {
                        arinc429CurrScreenSize--;
                        arinc429ListScreen.poll();
                    }
                    arinc429CurrScreenSize++;
                    arinc429ListScreen.add((SerialBusTxtStruct.Arinc429Struct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.Arinc429Struct) o)) {
                    synchronized (lock) {
                        while (arinc429S1S2CurrSize >= arinc429BufferSize * 2) {
                            arinc429S1S2CurrSize--;
                            arinc429S1S2ListTotal.poll();
                        }
                        arinc429S1S2CurrSize++;
                        arinc429S1S2ListTotal.add((SerialBusTxtStruct.Arinc429Struct) o);
                    }
                    synchronized (lock) {
                        while (arinc429S1S2CurrScreenSize >= arinc429BufferScreenSize * 3) {
                            arinc429S1S2CurrScreenSize--;
                            arinc429S1S2ListScreen.poll();
                        }
                        arinc429S1S2CurrScreenSize++;
                        arinc429S1S2ListScreen.add((SerialBusTxtStruct.Arinc429Struct) o);
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: {
                synchronized (lock) {
                last1553bNode=(SerialBusTxtStruct.MilSTD1553bStruct) o;
                while (milstd1553bCurrSize >= milstd1553bBufferSize){milstd1553bCurrSize--;     milstd1553bListTotal.poll();}
                milstd1553bCurrSize++;
//                Logger.i(TAG,"put 1553b count:"+milstd1553bCurrSize);
                milstd1553bListTotal.add((SerialBusTxtStruct.MilSTD1553bStruct) o);

                while (milstd1553bCurrScreenSize >= milstd1553bBufferScreenSize) { milstd1553bCurrScreenSize--;   milstd1553bListScreen.poll();              }
                milstd1553bCurrScreenSize++;
                milstd1553bListScreen.add((SerialBusTxtStruct.MilSTD1553bStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.MilSTD1553bStruct) o)) {
                    synchronized (lock) {
                        while (milstd1553bS1S2CurrSize >= milstd1553bBufferSize * 2) {
                            milstd1553bS1S2CurrSize--;
                            milstd1553bS1S2ListTotal.poll();
                        }
                        milstd1553bS1S2CurrSize++;
                        milstd1553bS1S2ListTotal.add((SerialBusTxtStruct.MilSTD1553bStruct) o);
                    }
                    synchronized (lock) {
                        while (milstd1553bS1S2CurrScreenSize >= milstd1553bBufferScreenSize * 3) {
                            milstd1553bS1S2CurrScreenSize--;
                            milstd1553bS1S2ListScreen.poll();
                        }
                        milstd1553bS1S2CurrScreenSize++;
                        milstd1553bS1S2ListScreen.add((SerialBusTxtStruct.MilSTD1553bStruct) o);
//                        Logger.i(TAG,"1553b screen:"+milstd1553bS1S2CurrScreenSize +"size:"+milstd1553bS1S2ListScreen.size());
                    }
//                    Logger.i(TAG,"put Ch:"+((SerialBusTxtStruct.MilSTD1553bStruct)o).Ch+" size:"+milstd1553bS1S2ListTotal.size());
                }
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: {
                lastCanNode = (SerialBusTxtStruct.CanStruct) o;
                synchronized (lock) {
                    while (canCurrSize >= canBufferSize) {
                        canCurrSize--;
                        canListTotal.poll();
                    }
                    canCurrSize++;
                    canListTotal.add((SerialBusTxtStruct.CanStruct) o);

                    while (canCurrScreenSize >= canBufferScreenSize) {
                        canCurrScreenSize--;
                        canListScreen.poll();
                    }
                    canCurrScreenSize++;
                    canListScreen.add((SerialBusTxtStruct.CanStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.CanStruct) o)) {
                    synchronized (lock) {
                        while (canS1S2CurrSize >= canBufferSize * 2) {
                            canS1S2CurrSize--;
                            canS1S2ListTotal.poll();
                        }
                        canS1S2CurrSize++;
                        canS1S2ListTotal.add((SerialBusTxtStruct.CanStruct) o);
                    }
                    synchronized (lock) {
                        while (canS1S2CurrScreenSize >= canBufferScreenSize * 3) {
                            canS1S2CurrScreenSize--;
                            canS1S2ListScreen.poll();
                        }
                        canS1S2CurrScreenSize++;
                        canS1S2ListScreen.add((SerialBusTxtStruct.CanStruct) o);
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: {
                lastI2cNode = ((SerialBusTxtStruct.I2cStruct) o);
                synchronized (lock) {
                    while (i2cCurrSize >= i2cBufferSize) {
                        i2cCurrSize--;
                        i2cListTotal.poll();
                    }
                    i2cCurrSize++;
                    i2cListTotal.add((SerialBusTxtStruct.I2cStruct) o);

                    while (i2cCurrScreenSize >= i2cBufferScreenSize) {
                        i2cCurrScreenSize--;
                        i2cListScreen.poll();
                    }
                    i2cCurrScreenSize++;
                    i2cListScreen.add((SerialBusTxtStruct.I2cStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.I2cStruct) o)) {
                    synchronized (lock) {
                        while (i2cS1S2CurrSize >= i2cBufferSize * 2) {
                            i2cS1S2CurrSize--;
                            i2cS1S2ListTotal.poll();
                        }
                        i2cS1S2CurrSize++;
                        i2cS1S2ListTotal.add((SerialBusTxtStruct.I2cStruct) o);
                    }
                    synchronized (lock) {
                        while (i2cS1S2CurrScreenSize >= i2cBufferScreenSize * 3) {
                            i2cS1S2CurrScreenSize--;
                            i2cS1S2ListScreen.poll();
                        }
                        i2cS1S2CurrScreenSize++;
                        i2cS1S2ListScreen.add((SerialBusTxtStruct.I2cStruct) o);
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_LIN: {
                lastLinNode = (SerialBusTxtStruct.LinStruct) o;
                synchronized (lock) {
                    while (linCurrSize >= lineBufferSize) {
                        linCurrSize--;
                        linListTotal.poll();
                    }
                    linCurrSize++;
                    linListTotal.add((SerialBusTxtStruct.LinStruct) o);

                    while (linCurrScreenSize >= lineBufferScreenSize) {
                        linCurrScreenSize--;
                        linListScreen.poll();
                    }
                    linCurrScreenSize++;
                    linListScreen.add((SerialBusTxtStruct.LinStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.LinStruct) o)) {
                    synchronized (lock) {
                        while (linS1S2CurrSize >= lineBufferSize * 2) {
                            linS1S2CurrSize--;
                            linS1S2ListTotal.poll();
                        }
                        linS1S2CurrSize++;
                        linS1S2ListTotal.add((SerialBusTxtStruct.LinStruct) o);
                    }
                    synchronized (lock) {
                        while (linS1S2CurrScreenSize >= lineBufferScreenSize * 3) {
                            linS1S2CurrScreenSize--;
                            linS1S2ListScreen.poll();
                        }
                        linS1S2CurrScreenSize++;
                        linS1S2ListScreen.add((SerialBusTxtStruct.LinStruct) o);
                    }
                }
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: {
                lastSpiNode = (SerialBusTxtStruct.SpiStruct) o;
                synchronized (lock) {
                    while (spiCurrSize >= spiBufferSize) {
                        spiCurrSize--;
                        spiListTotal.poll();
                    }
                    spiCurrSize++;
                    spiListTotal.add((SerialBusTxtStruct.SpiStruct) o);

                    while (spiCurrScreenSize >= spiBufferScreenSize) {
                        spiCurrScreenSize--;
                        spiListScreen.poll();
                    }
                    spiCurrScreenSize++;
                    spiListScreen.add((SerialBusTxtStruct.SpiStruct) o);
                }
                if (openS1AndS2 && isSerialsSelected((SerialBusTxtStruct.SpiStruct) o)) {
                    synchronized (lock) {
                        while (spiS1S2CurrSize >= spiBufferSize * 2) {
                            spiS1S2CurrSize--;
                            spiS1S2ListTotal.poll();
                        }
                        spiS1S2CurrSize++;
                        spiS1S2ListTotal.add((SerialBusTxtStruct.SpiStruct) o);
                    }
                    synchronized (lock) {
                        while (spiS1S2CurrScreenSize >= spiBufferScreenSize * 3) {
                            spiS1S2CurrScreenSize--;
                            spiS1S2ListScreen.poll();
                        }
                        spiS1S2CurrScreenSize++;
                        spiS1S2ListScreen.add((SerialBusTxtStruct.SpiStruct) o);
                    }
                }
            }
            break;
        }
    }

    public    <T> T getStruct(int serialBusType){
        synchronized (lock) {
            switch (serialBusType) {
                case SerialBusStruct.SerialBusType_UART: {
                    if (uartCurrSize >= uartBufferSize) {
                        uartCurrSize--;
//                        Object o = (uartListTotal.poll()).clean();
//                        return (T) (o);
                        uartListTotal.poll();
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new UartStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new UartStruct());
                }
                case SerialBusStruct.SerialBusType_LIN: {
                    if (linCurrSize >= lineBufferSize) {
                        linCurrSize--;
                        (linListTotal).poll();
//                        Object o = ((SerialBusTxtStruct.LinStruct) (linListTotal).poll()).clean();
//                        return (T) o;
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new LinStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new LinStruct());
                }
                case SerialBusStruct.SerialBusType_CAN: {
                    if (canCurrSize >= canBufferSize) {
                        canCurrSize--;
                        (canListTotal).poll();
//                        Object o = ((SerialBusTxtStruct.CanStruct) (canListTotal).poll()).clean();
//                        return (T) o;
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new CanStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new CanStruct());
                }
                case SerialBusStruct.SerialBusType_SPI: {
                    if (spiCurrSize >= spiBufferSize) {
                        spiCurrSize--;
                        (spiListTotal).poll();
//                        return (T) ((SerialBusTxtStruct.SpiStruct) (spiListTotal).poll()).clean();
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new SpiStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new SpiStruct());
                }
                case SerialBusStruct.SerialBusType_I2C: {
                    if (i2cCurrSize >= i2cBufferSize) {
                        i2cCurrSize--;
                        (i2cListTotal).poll();
//                        return (T) ((SerialBusTxtStruct.I2cStruct) (i2cListTotal).poll()).clean();
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new I2cStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new I2cStruct());
                }
                case SerialBusStruct.SerialBusType_429: {
                    if (arinc429CurrSize >= arinc429BufferSize) {
                        arinc429CurrSize--;
                         (arinc429ListTotal).poll();
//                        return (T) ((SerialBusTxtStruct.Arinc429Struct) (arinc429ListTotal).poll()).clean();
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new Arinc429Struct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new Arinc429Struct());
                }
                case SerialBusStruct.SerialBusType_1553B: {
                    if (milstd1553bCurrSize >= milstd1553bBufferSize) {
                        milstd1553bCurrSize--;
                        (milstd1553bListTotal).poll();
//                        return (T) (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct());
//                        return (T) ((SerialBusTxtStruct.MilSTD1553bStruct) (milstd1553bListTotal).poll()).clean();
                    }
//                    else {
//                        return (T) (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct());
//                    }
                    return (T) (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct());
                }
            }
        }
        return null;
    }


    public void clearAll() {
        synchronized (lock) {
            buffer.clear();

            uartCurrScreenSize = 0;
            arinc429CurrScreenSize = 0;
            canCurrScreenSize = 0;
            i2cCurrScreenSize = 0;
            linCurrScreenSize = 0;
            milstd1553bCurrScreenSize = 0;
            spiCurrScreenSize = 0;
            uartCurrSize = 0;
            arinc429CurrSize = 0;
            canCurrSize = 0;
            i2cCurrSize = 0;
            linCurrSize = 0;
            milstd1553bCurrSize = 0;
            spiCurrSize = 0;
            uartS1S2CurrSize = 0;
            arinc429S1S2CurrSize = 0;
            canS1S2CurrSize = 0;
            i2cS1S2CurrSize = 0;
            linS1S2CurrSize = 0;
            milstd1553bS1S2CurrSize = 0;
            spiS1S2CurrSize = 0;

            uartS1S2CurrScreenSize = 0;
            arinc429S1S2CurrScreenSize = 0;
            canS1S2CurrScreenSize = 0;
            i2cS1S2CurrScreenSize = 0;
            linS1S2CurrScreenSize = 0;
            milstd1553bS1S2CurrScreenSize = 0;
            spiS1S2CurrScreenSize = 0;

            uartListScreen.clear();
            arinc429ListScreen.clear();
            canListScreen.clear();
            i2cListScreen.clear();
            linListScreen.clear();
            milstd1553bListScreen.clear();
            spiListScreen.clear();

            uartListTotal.clear();
            arinc429ListTotal.clear();
            canListTotal.clear();
            i2cListTotal.clear();
            linListTotal.clear();
            milstd1553bListTotal.clear();
            spiListTotal.clear();

            if (uartS1S2ListTotal != null) uartS1S2ListTotal.clear();
            if (arinc429S1S2ListTotal != null) arinc429S1S2ListTotal.clear();
            if (canS1S2ListTotal != null) canS1S2ListTotal.clear();
            if (i2cS1S2ListTotal != null) i2cS1S2ListTotal.clear();
            if (linS1S2ListTotal != null) linS1S2ListTotal.clear();
            if (milstd1553bS1S2ListTotal != null) milstd1553bS1S2ListTotal.clear();
            if (spiS1S2ListTotal != null) spiS1S2ListTotal.clear();

            if (uartS1S2ListScreen != null) uartS1S2ListScreen.clear();
            if (arinc429S1S2ListScreen != null) arinc429S1S2ListScreen.clear();
            if (canS1S2ListScreen != null) canS1S2ListScreen.clear();
            if (i2cS1S2ListScreen != null) i2cS1S2ListScreen.clear();
            if (linS1S2ListScreen != null) linS1S2ListScreen.clear();
            if (milstd1553bS1S2ListScreen != null) milstd1553bS1S2ListScreen.clear();
            if (spiS1S2ListScreen != null) spiS1S2ListScreen.clear();

            uartTotalData = 0;
            uartErrorData = 0;

            canTotalFrame = 0;
            canSpaceFrame = 0;
            canErrorFrame = 0;

            arinc429TotalFrame = 0;
            arinc429ErrorFrame = 0;

            armLastTime = 0;
            totalTime = 0;
        }
    }

    //region uart attribute
    public long getUartTotalData() {
        return uartTotalData;
    }

    public  void addUartTotalData() {
        synchronized (lock) {
            this.uartTotalData++;
        }
    }

    public long getUartErrorData() {
        return uartErrorData;
    }

    public  void addUartErrorData() {
        synchronized (lock) {
            this.uartErrorData++;
        }
    }

    public  void addCanTotalFrame() {
        synchronized (lock) {
            this.canTotalFrame++;
        }
    }

    public  void addCanSpaceFrame() {
        synchronized (lock) {
            this.canSpaceFrame++;
        }
    }

    public  void addCanErrorFrame() {
        synchronized (lock) {
            this.canErrorFrame++;
        }
    }

    public long getCanTotalFrame() {
        return canTotalFrame;
    }

    public long getCanSpaceFrame() {
        return canSpaceFrame;
    }

    public long getCanErrorFrame() {
        return canErrorFrame;
    }

    public  void addArinc429TotalFrame() {
        synchronized (lock) {
            this.arinc429TotalFrame++;
        }
    }

    public  void addArinc429ErrorFrame() {
        synchronized (lock) {
            this.arinc429ErrorFrame++;
        }
    }

    public long getArinc429TotalFrame() {
        return arinc429TotalFrame;
    }

    public long getArinc429ErrorFrame() {
        return arinc429ErrorFrame;
    }

    public boolean isOpenS1AndS2() {
        return openS1AndS2;
    }

    public int getArmLastTime() {
        return armLastTime;
    }

    public void setArmLastTime(int armLastTime) {
        this.armLastTime = armLastTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public SerialBusTxtStruct.UartStruct getLastUartNode() {
        return lastUartNode;
    }

    public SerialBusTxtStruct.Arinc429Struct getLast429Node() {
        return last429Node;
    }

    public SerialBusTxtStruct.CanStruct getLastCanNode() {
        return lastCanNode;
    }

    public SerialBusTxtStruct.SpiStruct getLastSpiNode() {
        return lastSpiNode;
    }

    public SerialBusTxtStruct.I2cStruct getLastI2cNode() {
        return lastI2cNode;
    }

    public SerialBusTxtStruct.LinStruct getLastLinNode() {
        return lastLinNode;
    }

    public SerialBusTxtStruct.MilSTD1553bStruct getLast1553bNode() {
        return last1553bNode;
    }

    public void setLastUartNode(SerialBusTxtStruct.UartStruct lastUartNode) {
        this.lastUartNode = lastUartNode;
    }

    public void setLast429Node(SerialBusTxtStruct.Arinc429Struct last429Node) {
        this.last429Node = last429Node;
    }

    public void setLastCanNode(SerialBusTxtStruct.CanStruct lastCanNode) {
        this.lastCanNode = lastCanNode;
    }

    public void setLastSpiNode(SerialBusTxtStruct.SpiStruct lastSpiNode) {
        this.lastSpiNode = lastSpiNode;
    }

    public void setLastI2cNode(SerialBusTxtStruct.I2cStruct lastI2cNode) {
        this.lastI2cNode = lastI2cNode;
    }

    public void setLastLinNode(SerialBusTxtStruct.LinStruct lastLinNode) {
        if (!(this.lastLinNode.equals(lastLinNode)))
            this.lastLinNode = lastLinNode;
    }

    public void setLast1553bNode(SerialBusTxtStruct.MilSTD1553bStruct last1553bNode) {
        this.last1553bNode = last1553bNode;
    }
    //endregion

    //region getList方法


    public static int getUartS1S2CurrScreenSize() {
        return uartS1S2CurrScreenSize;
    }

    public static int getArinc429S1S2CurrScreenSize() {
        return arinc429S1S2CurrScreenSize;
    }

    public static int getCanS1S2CurrScreenSize() {
        return canS1S2CurrScreenSize;
    }

    public static int getI2cS1S2CurrScreenSize() {
        return i2cS1S2CurrScreenSize;
    }

    public static int getLinS1S2CurrScreenSize() {
        return linS1S2CurrScreenSize;
    }

    public static int getMilstd1553bS1S2CurrScreenSize() {
        return milstd1553bS1S2CurrScreenSize;
    }

    public static int getSpiS1S2CurrScreenSize() {
        return spiS1S2CurrScreenSize;
    }

    public static int getUartS1S2CurrSize() {
        return uartS1S2CurrSize;
    }

    public static int getArinc429S1S2CurrSize() {
        return arinc429S1S2CurrSize;
    }

    public static int getCanS1S2CurrSize() {
        return canS1S2CurrSize;
    }

    public static int getI2cS1S2CurrSize() {
        return i2cS1S2CurrSize;
    }

    public static int getLinS1S2CurrSize() {
        return linS1S2CurrSize;
    }

    public static int getMilstd1553bS1S2CurrSize() {
        return milstd1553bS1S2CurrSize;
    }

    public static int getSpiS1S2CurrSize() {
        return spiS1S2CurrSize;
    }

    public int getUartCurrScreenSize() {
        return uartCurrScreenSize;
    }

    public int getArinc429CurrScreenSize() {
        return arinc429CurrScreenSize;
    }

    public int getCanCurrScreenSize() {
        return canCurrScreenSize;
    }

    public int getI2cCurrScreenSize() {
        return i2cCurrScreenSize;
    }

    public int getLinCurrScreenSize() {
        return linCurrScreenSize;
    }

    public int getMilstd1553bCurrScreenSize() {
        return milstd1553bCurrScreenSize;
    }

    public int getSpiCurrScreenSize() {
        return spiCurrScreenSize;
    }

    public int getUartCurrSize() {
        return uartCurrSize;
    }

    public int getArinc429CurrSize() {
        return arinc429CurrSize;
    }

    public int getCanCurrSize() {
        return canCurrSize;
    }

    public int getI2cCurrSize() {
        return i2cCurrSize;
    }

    public int getLinCurrSize() {
        return linCurrSize;
    }

    public int getMilstd1553bCurrSize() {
        return milstd1553bCurrSize;
    }

    public int getSpiCurrSize() {
        return spiCurrSize;
    }

    public SerialBusTxtStruct.UartStruct getUartLastElement() {
        SerialBusTxtStruct.UartStruct uart = null;
        for (Iterator iter = uartListTotal.iterator(); iter.hasNext(); ) {
            uart = (SerialBusTxtStruct.UartStruct) iter.next();
        }
        if (uart == null) {
            uart = (SerialBusTxtStruct.getInstance().new UartStruct());
        }
        return uart;
    }

    public SerialBusTxtStruct.Arinc429Struct getArinc429LastElement() {
        SerialBusTxtStruct.Arinc429Struct a429 = null;
        for (Iterator iter = arinc429ListTotal.iterator(); iter.hasNext(); ) {
            a429 = (SerialBusTxtStruct.Arinc429Struct) iter.next();
        }
        if (a429 == null) {
            a429 = (SerialBusTxtStruct.getInstance().new Arinc429Struct());
        }
        return a429;
    }

    public SerialBusTxtStruct.CanStruct getCanLastElement() {
        SerialBusTxtStruct.CanStruct can = null;
        for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) {
            can = (SerialBusTxtStruct.CanStruct) iter.next();
        }
        if (can == null) {
            can = (SerialBusTxtStruct.getInstance().new CanStruct());
        }
        return can;
    }

    public SerialBusTxtStruct.I2cStruct getI2cLastElement() {
        SerialBusTxtStruct.I2cStruct i2c = null;
        for (Iterator iter = i2cListTotal.iterator(); iter.hasNext(); ) {
            i2c = (SerialBusTxtStruct.I2cStruct) iter.next();
        }
        if (i2c == null) {
            i2c = (SerialBusTxtStruct.getInstance().new I2cStruct());
        }
        return i2c;
    }

    public SerialBusTxtStruct.LinStruct getLinLastElement() {
        SerialBusTxtStruct.LinStruct lin = null;
        for (Iterator iter = linListTotal.iterator(); iter.hasNext(); ) {
            lin = (SerialBusTxtStruct.LinStruct) iter.next();
        }
        if (lin == null) {
            lin = (SerialBusTxtStruct.getInstance().new LinStruct());
        }
        return lin;
    }

    public SerialBusTxtStruct.MilSTD1553bStruct getM1553bLastElement() {
        SerialBusTxtStruct.MilSTD1553bStruct m1553b = null;
        for (Iterator iter = milstd1553bListTotal.iterator(); iter.hasNext(); ) {
            m1553b = (SerialBusTxtStruct.MilSTD1553bStruct) iter.next();
        }
        if (m1553b == null) {
            m1553b = (SerialBusTxtStruct.getInstance().new MilSTD1553bStruct());
        }
        return m1553b;
    }

    public SerialBusTxtStruct.SpiStruct getSpiLastElement() {
        SerialBusTxtStruct.SpiStruct spi = null;
        for (Iterator iter = spiListTotal.iterator(); iter.hasNext(); ) {
            spi = (SerialBusTxtStruct.SpiStruct) iter.next();
        }
        if (spi == null) {
            spi = (SerialBusTxtStruct.getInstance().new SpiStruct());
        }
        return spi;
    }


    //endregion

    //region getQueue方法


    public LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> getUartS1S2ListScreen() {
        return uartS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> getArinc429S1S2ListScreen() {
        return arinc429S1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> getCanS1S2ListScreen() {
        return canS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> getI2cS1S2ListScreen() {
        return i2cS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> getLinS1S2ListScreen() {
        return linS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> getMilstd1553bS1S2ListScreen() {
        return milstd1553bS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> getSpiS1S2ListScreen() {
        return spiS1S2ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> getUartListScreen() {
        return uartListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> getArinc429ListScreen() {
        return arinc429ListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> getCanListScreen() {
        return canListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> getI2cListScreen() {
        return i2cListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> getLinListScreen() {
        return linListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> getMilstd1553bListScreen() {
        return milstd1553bListScreen;
    }

    public LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> getSpiListScreen() {
        return spiListScreen;
    }

    public LinkedBlockingQueue getUartQueueTotal() {
        return uartListTotal;
    }

    public LinkedBlockingQueue getArinc429QueueTotal() {
        return arinc429ListTotal;
    }

    public LinkedBlockingQueue getCanQueueTotal() {
        return canListTotal;
    }

    public LinkedBlockingQueue getI2cQueueTotal() {
        return i2cListTotal;
    }

    public LinkedBlockingQueue getLinQueueTotal() {
        return linListTotal;
    }

    public LinkedBlockingQueue getMilstd1553bQueueTotal() {
        return milstd1553bListTotal;
    }

    public LinkedBlockingQueue getSpiQueueTotal() {
        return spiListTotal;
    }

    public LinkedBlockingQueue getUartS1S2QueueTotal() {
        return uartS1S2ListTotal;
    }

    public LinkedBlockingQueue getArinc429S1S2QueueTotal() {
        return arinc429S1S2ListTotal;
    }

    public LinkedBlockingQueue getCanS1S2QueueTotal() {
        return canS1S2ListTotal;
    }

    public LinkedBlockingQueue getI2cS1S2QueueTotal() {
        return i2cS1S2ListTotal;
    }

    public LinkedBlockingQueue getLinS1S2QueueTotal() {
        return linS1S2ListTotal;
    }

    public LinkedBlockingQueue getMilstd1553bS1S2QueueTotal() {
        return milstd1553bS1S2ListTotal;
    }

    public LinkedBlockingQueue getSpiS1S2QueueTotal() {
        return spiS1S2ListTotal;
    }
    //endregion

    //region 测试
    private LinkedBlockingQueue queue = new LinkedBlockingQueue();

    //endregion
}