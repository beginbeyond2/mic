package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import android.annotation.SuppressLint;
import android.graphics.Color;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.MScopePublicConst;
import com.micsig.tbook.ui.wavezone.TChan;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialBusTxtStructParse {
    private static final String TAG = "SerialBusTxtStructParse";

    //region 单例
    public static class SerialBusTxtStructParseHolder {
        public static final SerialBusTxtStructParse instance = new SerialBusTxtStructParse();
    }

    public static SerialBusTxtStructParse getInstance() {
        return SerialBusTxtStructParseHolder.instance;
    }
    public SerialBusTxtStructParse(){
        TChan.foreachSerial((ch)->{
            int chIdx=ch-TChan.S1;
             fixedThreadPool[chIdx]=Executors.newFixedThreadPool(1);
            toParseByRunables[chIdx]=new ToParseByRunable();
        });
    }
    //endregion

    private long time;
//    private int encoding1;
//    private int encoding2;
//    private int bits1;
//    private int bits2;
//    private boolean checked;
    private int[] encodings = new int[4];
    private int[] bits = new int[4];
    private boolean[] checkeds = new boolean[4];

//    final ExecutorService fixedThreadPoolS1 = Executors.newFixedThreadPool(1);
//    final ExecutorService fixedThreadPoolS2 = Executors.newFixedThreadPool(1);
//    private ToParseByRunable toParseByRunableS1 = new ToParseByRunable();
//    private ToParseByRunable toParseByRunableS2 = new ToParseByRunable();

    final ExecutorService[] fixedThreadPool=new ExecutorService[TChan.MaxSerial];
    private ToParseByRunable[] toParseByRunables=new ToParseByRunable[TChan.MaxSerial];

    //正在解析的状态
//    private boolean parsingS1 = false;
//    private boolean parsingS2 = false;
    private boolean[] parsing=new boolean[TChan.MaxSerial];

    //region 线程解析1

    public boolean getParsing(int ch_SerialBus_TXTS1) {
//        if (ch_SerialBus_TXTS1 == SerialBusManage.SerialBus_TXTS1) {
//            return this.parsingS1;
//        } else if (ch_SerialBus_TXTS1 == SerialBusManage.SerialBus_TXTS2) {
//            return this.parsingS2;
//        } else {
//            return this.parsingS1 || this.parsingS2;
//        }
        int idx=(ch_SerialBus_TXTS1-TChan.S1);
        if (TChan.isSerial(ch_SerialBus_TXTS1)){
            return parsing[idx];
        }else {
            for(int i=0;i<parsing.length;i++){
                if (parsing[i]){
                    return true;
                }
            }
            return false;
        }
    }

    public void InterruptedParse(int ch_SerialBus_TXTS1) {
//        toParseByRunableS2.Interrupted();
//        toParseByRunableS1.Interrupted();
        TChan.foreachSerial((ch)->{
            int chIdx=ch-TChan.S1;
            toParseByRunables[chIdx].Interrupted();
        });

//        if (ch_SerialBus_TXTS1 == SerialBusManage.SerialBus_TXTS1) {
//            toParseByRunableS1.Interrupted();
//        } else if (ch_SerialBus_TXTS1 == SerialBusManage.SerialBus_TXTS2) {
//            toParseByRunableS2.Interrupted();
//        }else{
//            toParseByRunableS2.Interrupted();
//            toParseByRunableS1.Interrupted();
//        }
    }

    class ToParseByRunable implements Runnable {
        private LinkedBlockingQueue<ByteBuffer> buffers = null;

        public void setBuffers(LinkedBlockingQueue<ByteBuffer> buffers) {
            this.buffers = buffers;
        }

        private String Ch;
        private SerialTxtBuffer serialTxtBuffer;
        private int serialBusType;
        private ByteBuffer bytes;
        private int encoding;
        private int bits;
        private boolean checked;

        private boolean interrupte = false;

        public void Interrupted() {
            interrupte = true;
        }

        public void setParam(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, int encoding, int bits, boolean checked) {
            this.Ch = Ch;
            this.serialTxtBuffer = serialTxtBuffer;
            this.serialBusType = serialBusType;
            this.encoding = encoding;
            this.bits = bits;
            this.checked = checked;
        }

        @Override
        public void run() {
            int count = 0;
            interrupte = false;
            Thread.currentThread().setName("serial_txt_parse_" + this.Ch);
//            if (this.Ch.equals(SerialBusManage.SerialBus_STRINGSHOWS1)) parsingS1 = true;
//            else parsingS2 = true;
            int idx= TChan.findChanByValue(this.Ch) - TChan.S1;

            parsing[idx]=true;
            while (buffers != null && !buffers.isEmpty()) {
                if (Scope.getInstance().isRun()==false) break;
                bytes = buffers.poll();
                if (interrupte == false) {
                    if (bytes == null) break;
                    toParse(Ch, serialTxtBuffer, serialBusType, bytes, encoding, bits, checked);
                } else {
                    count++;
                }
            }
//            Logger.i(TAG,"chNo:"+this.Ch+"  线程停止后未处理的数据包:"+count);
//            if (this.Ch.equals(SerialBusManage.SerialBus_STRINGSHOWS1)) parsingS1 = false;
//            else parsingS2 = false;
            parsing[idx]=false;
        }
    }

    public void toParseByRunable(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, int encoding, int bits, boolean checked) {
//        if (Ch.equals(SerialBusManage.SerialBus_STRINGSHOWS1)) {
//            toParseByRunableS1.setBuffers(serialTxtBuffer.getBuffer());
//            toParseByRunableS1.setParam(Ch, serialTxtBuffer, serialBusType, encoding, bits, checked);
//            fixedThreadPoolS1.execute(toParseByRunableS1);
//        } else {
//            toParseByRunableS2.setBuffers(serialTxtBuffer.getBuffer());
//            toParseByRunableS2.setParam(Ch, serialTxtBuffer, serialBusType, encoding, bits, checked);
//            fixedThreadPoolS2.execute(toParseByRunableS2);
//        }
        int chIdx= TChan.findChanByValue(Ch)-TChan.S1;
        toParseByRunables[chIdx].setBuffers(serialTxtBuffer.getBuffer());
        toParseByRunables[chIdx].setParam(Ch, serialTxtBuffer, serialBusType, encoding, bits, checked);
        fixedThreadPool[chIdx].execute(toParseByRunables[chIdx]);
    }
    //endregion


    public void toParse(String Ch, SerialTxtBuffer serialTxtBuffer, int serialBusType, final ByteBuffer bytes, int encoding, int bits, boolean checked) {
//        if (Ch.equals("S1")) {
//            this.encoding1 = encoding;
//            this.bits1 = bits;
//        } else {
//            this.encoding2 = encoding;
//            this.bits2 = bits;
//        }
//        this.checked = checked;
        int chIdx= TChan.findChanByValue(Ch)-TChan.S1;
        this.encodings[chIdx]=encoding;
        this.bits[chIdx]=bits;
        this.checkeds[chIdx]=checked;

        switch (serialBusType) {
            case SerialBusStruct.SerialBusType_UART: {
                getUartStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_LIN: {
                getLinStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: {
                getCanStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: {
                getSpiStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: {
                getI2cStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_429: {
                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT
                        + TChan.toSerialNumber(Ch));
                get429Struct(Ch, format, serialTxtBuffer, bytes);
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: {
                get1553bStruct(Ch, serialTxtBuffer, bytes);
            }
            break;
        }
    }

    private void getUartStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int UartType = SerialBusStruct.SerialBusType_UART;
        final int NoStopColor = Color.YELLOW;
        final int StopSuccessColor = App.get().getResources().getColor(R.color.textColor); //Color.WHITE;
        final int StopFailedColor = Color.RED;

        boolean checked ;//false;     //也是界面传进来的。
        int encoding;
        int uartLength; //8;       //位的长度，由界面传过来的。
//        if (Ch.equals("S1")) {
//            encoding = this.encoding1; //ICharacterEncoding.Hex;  //也是界面传进来的
//            uartLength = this.bits1;
//        } else {
//            encoding = this.encoding2; //ICharacterEncoding.Hex;  //也是界面传进来的
//            uartLength = this.bits2;
//        }
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1;
        checked=this.checkeds[chIdx];
        encoding=this.encodings[chIdx];
        uartLength=this.bits[chIdx];

        SerialBusTxtStruct.UartStruct uart = null;
        for (int i = 0; i < bytes.limit(); i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                //时间
//                uart=serialTxtBuffer.getStruct(UartType);
            } else {
                if (uart == null) {
                    if (serialTxtBuffer.getUartCurrSize() > 0) {
                        uart = serialTxtBuffer.getLastUartNode();
                        if (uart.isFlagFrameEnd()) {
                            uart = serialTxtBuffer.getStruct(UartType);
                            uart.Ch = Ch;
                        }
                    } else {
                        uart = serialTxtBuffer.getStruct(UartType);
                        uart.Ch = Ch;
                    }
                } else if (uart.isFlagFrameEnd()) {
                    uart = serialTxtBuffer.getStruct(UartType);
                    uart.Ch = Ch;
                }
                //类型
                switch (bytes.get(i + 1) & 0x7) {
                    case SerialBusTxtStruct.UartStruct.CHECK_OK_NoStop: {
                        uart.Color = NoStopColor;
                        serialTxtBuffer.addUartErrorData();
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_Error_NoStop: {
                        uart.Color = StopFailedColor;
                        serialTxtBuffer.addUartErrorData();
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_OK_Stop: {
                        uart.Color = StopSuccessColor;
                    }
                    break;
                    case SerialBusTxtStruct.UartStruct.CHECK_Error_Stop: {
                        uart.Color = StopFailedColor;
                        serialTxtBuffer.addUartErrorData();
                    }
                    break;
                }

                if (uartLength <= 8) {
                    uart.Data = bytes.get(i) & 0x000000FF;
                    //region 测试 中间出错数据
//                    if  (serialTxtBuffer.getUartCurrSize()>0 && isdebug(serialTxtBuffer.getLastUartNode(),uart)==false){
//                            Logger.i(TAG,"i:"+i+" get(i):"+bytes.get(i));
//                            StringBuilder s= new StringBuilder();
//                            for(int j = 0; j < bytes.limit(); j ++){
//                                s.append("0x").append(Integer.toHexString(bytes.get(j))).append(", ");
//                            }
//                            Logger.e(TAG,"出错数据："+s.toString());
//
//                    }
                    //endregion
                    uart.setFlagFrameEnd(true);
                    serialTxtBuffer.put(UartType, uart);
                    serialTxtBuffer.addUartTotalData();
                } else {
                    //奇偶，不进行拼接
                    if (checked == false) {
                        uart.Data = bytes.get(i) & 0x000000FF;
                        uart.setFlagFrameEnd(true);
                        serialTxtBuffer.put(UartType, uart);
                        serialTxtBuffer.addUartTotalData();
                    } else {
                        if ((bytes.get(i + 1) & 0x08) == 0x08) {
                            uart.Data = 0x000000FF & bytes.get(i);
//                            Logger.i(TAG,"1 data:"+Integer.toHexString(bytes.get(i)) +" Data:"+Integer.toHexString(uart.Data));
                        } else {
                            uart.Data |= (0x00000100 & ((bytes.get(i)) << 8));
//                            Logger.i(TAG,"2 data:"+Integer.toHexString(bytes.get(i))+" Data:"+ Integer.toHexString(uart.Data));
                            uart.setFlagFrameEnd(true);
                            serialTxtBuffer.put(UartType, uart);
                            serialTxtBuffer.addUartTotalData();
                        }
                    }
                }
            }
            if (uart != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastUartNode() != null && !serialTxtBuffer.getLastUartNode().equals(uart)) {
                serialTxtBuffer.setLastUartNode(uart);
            }
        }
    }

    private boolean isdebug(SerialBusTxtStruct.UartStruct befor, SerialBusTxtStruct.UartStruct curr) {
        if (befor == null) return false;
        if (befor.Data == curr.Data) return true;
//        Logger.i(TAG," bef: "+befor.Data+"  cur:"+curr.Data);
        String bef = "";
        if (befor.Data == 0xDD) {
            bef = "00000000" + Integer.toHexString(0);
        } else {
            bef = "00000000" + Integer.toHexString(befor.Data + 17);
        }
        bef = bef.substring(bef.length() - 2, bef.length());
        String cur = "00000000" + Integer.toHexString(curr.Data);
        cur = cur.substring(cur.length() - 2, cur.length());
        if (cur.equals(bef)) {
            return true;
        }
        Logger.i(TAG, "befor:0x" + bef + "cur:0x" + cur);
        return false;
    }

    private void getLinStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_LIN;
        SerialBusTxtStruct.LinStruct lin = null;
        for (int i = 0; i < bytes.limit(); i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                getCurrTotalTime(serialTxtBuffer, bytes, i);
            } else {
                if (lin == null) {
                    if (serialTxtBuffer.getLinCurrSize() > 0) {
                        lin = serialTxtBuffer.getLastLinNode();
                        if (lin.isFlagFrameEnd()) {
                            lin = serialTxtBuffer.getStruct(Type);
                            lin.Ch = Ch;
                            lin.CurTime = serialTxtBuffer.getTotalTime();
                        }
                    } else {
                        lin = serialTxtBuffer.getStruct(Type);
                        lin.Ch = Ch;
                        lin.CurTime = serialTxtBuffer.getTotalTime();
                    }
                } else if (lin.isFlagFrameEnd()) {
                    lin = serialTxtBuffer.getStruct(Type);
                    lin.Ch = Ch;
                    lin.CurTime = serialTxtBuffer.getTotalTime();
                }
                int serialsChan = TChan.findChanByValue(lin.Ch);
                if (TChan.isSerial(serialsChan)) {
                    lin.linType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + TChan.toSerialNumber(serialsChan));
                }

                //解析数据
                int type = bytes.get(i + 1) & 0x0F;
                switch (type) {
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckYes: {
                        lin.Id = bytes.get(i) & 0x000000FF;
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_DataYes: {
                        lin.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData();
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckSumYes: {
                        lin.Check = bytes.get(i) & 0x000000FF;
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckNo: {
                        lin.Id = bytes.get(i) & 0x000000FF;
                        lin.Error = "Par";
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_DataNo: {
                        lin.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData();
                        lin.Error = "STOP";
                    }
                    break;
                    case SerialBusTxtStruct.LinStruct.Lin_30_CheckSumNo: {
                        lin.Check = bytes.get(i) & 0x000000FF;
                        lin.Error = "Chec";
                    }
                    break;
                }


                //如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) {
                        //本次总线通信不符合触发条件
                        lin.Trigger = true;
                    } else { //本次总线通信符合触发条件
                        lin.Trigger = false;
                    }
                    if ((/*lin != null &&*/ serialTxtBuffer.getLinCurrSize() > 0 && !serialTxtBuffer.getLastLinNode().equals(lin))
                            || (/*lin != null &&*/ serialTxtBuffer.getLinCurrSize() == 0)
                            || (serialTxtBuffer.getLastLinNode().equals(lin) && !lin.isFlagFrameEnd())) {
                        lin.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, lin);

                    }
                }
            }

            if (lin != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastLinNode() != null && !serialTxtBuffer.getLastLinNode().equals(lin)) {
                serialTxtBuffer.setLastLinNode(lin);
            }

        }
    }

    private void getCanStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_CAN;
        SerialBusTxtStruct.CanStruct can = null;
        //spec错误次数
        int specTimes = 0;
        //标准id次数
        int stdIdtimes = 0;
        for (int i = 0; i <= bytes.limit() - 2; i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                //时间
                getCurrTotalTime(serialTxtBuffer, bytes, i);
            } else {
                if (can == null) {
                    can = serialTxtBuffer.getLastCanNode();
                    if (can!=null) {
                        if (can.isFlagFrameEnd()) {
                            can = serialTxtBuffer.getStruct(Type);
                            can.Ch = Ch;
                            can.CurTime = serialTxtBuffer.getTotalTime();
                            stdIdtimes = 0;
                        }else {
                            stdIdtimes=can.stdIdtimes;
                        }
                    } else {
                        can = serialTxtBuffer.getStruct(Type);
                        can.Ch = Ch;
                        can.CurTime = serialTxtBuffer.getTotalTime();
                        stdIdtimes = 0;
                    }
                } else if (can.isFlagFrameEnd()) {
                    can = serialTxtBuffer.getStruct(Type);
                    can.Ch = Ch;
                    can.CurTime = serialTxtBuffer.getTotalTime();
                    stdIdtimes = 0;
                }

                //解析数据
                int type = bytes.get(i + 1) & 0x0F;
                switch (type) {
                    case SerialBusTxtStruct.CanStruct.Can_30_stdId: {
                        stdIdtimes++;
                        can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_STDID;
                        if (stdIdtimes == 1) {
                            //第一次发送过来的id,不管是标准Id还是扩展ID都进行保存
                            can.ID = bytes.get(i) & 0x000000FF;
                        } else {
                            //第二次id 一定是标准ID,3个字节有效
                            stdIdtimes++;
                            can.ID = ((can.ID << 3) | (0x00000007 & bytes.get(i)));
                        }
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_DLC: {
                        int dlc = bytes.get(i) & 0x000000F;
                        if(dlc > 8) {
                            switch (dlc){
                                case 9: dlc = 12;break;
                                case 10: dlc = 16;break;
                                case 11: dlc = 20;break;
                                case 12: dlc = 24;break;
                                case 13: dlc = 32;break;
                                case 14: dlc = 48;break;
                                case 15: dlc = 64;break;
                                default:
                                    dlc = 8;
                                    break;
                            }
                        }
                        can.DLC = dlc;
                        //can.DLC = bytes.get(i) & 0x000000FF;
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_DATA: {
                        can.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData();
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_CRC: {
                        if (can.CRC > 0)
                            can.CRC = (can.CRC << 8) | (0xFF & bytes.get(i));
                        else
                            can.CRC = bytes.get(i) & 0xFF;
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_Error: {
                        can.ErrorEnum = bytes.get(i) & SerialBusTxtStruct.CanStruct.Can_Error_MASK;
                        specTimes++;
                        serialTxtBuffer.addCanSpaceFrame();
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_extId: {
                        can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_EXTID;
                        stdIdtimes++;
                        switch (stdIdtimes) {
                            case 2:
                            case 3:
                                can.ID = (can.ID << 8 | (0x000000FF & bytes.get(i)));
                                break;
                            case 4:
                                can.ID = (can.ID << 5 | (0x0000001F & bytes.get(i)));
                                break;
                        }
                    }
                    break;
                    case SerialBusTxtStruct.CanStruct.Can_30_Overload: {

                    }
                    break;
                    default: {
                        //帧结束
                        if ((type >> 3 & 0x01) == 0x01) {
                            if ((type >> 0 & 0x01) == 0x01) {
                                //帧结束，有确认
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Confirm;
                            } else {
                                //帧结束，无确认；（当无确认时，会在之前先给出“错误”）
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_NoConfirm;
                            }
                            if ((type >> 1 & 0x01) == 0x01) {
                                //帧结束，远程帧
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Data;
                                if (can.TypeEnum == SerialBusTxtStruct.CanStruct.Type_STDID) {
                                    can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_STDREMOTEID;
                                } else if (can.TypeEnum == SerialBusTxtStruct.CanStruct.Type_EXTID) {
                                    can.TypeEnum = SerialBusTxtStruct.CanStruct.Type_EXTREMOTEID;
                                }
                            } else {
                                //帧结束，数据帧
                                can.FrameEndEnum = SerialBusTxtStruct.CanStruct.Can_FrameEnd_Remote;
                            }
//                            //帧结束
//                            can.setFlagFrameEnd(true);
//                            serialTxtBuffer.put(Type, can);
//                            if (specTimes > 0) {
//                                serialTxtBuffer.addCanErrorFrame();
//                                specTimes = 0;
//                            }
//                            serialTxtBuffer.addCanTotalFrame();
                        }
                    }
                    break;
                }


                //如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) {
                        //本次总线通信不符合触发条件
                        can.Trigger = true;
                    } else { //本次总线通信符合触发条件
                        can.Trigger = false;
                    }
                    if (can.ID == 0 && can.DLC == 0 && can.Data.equals("") && can.CRC == 0)
                        continue;
                    if ((/*can != null &&*/ serialTxtBuffer.getCanCurrSize() > 0 && !serialTxtBuffer.getLastCanNode().equals(can))
                            || (/*can != null &&*/ serialTxtBuffer.getCanCurrSize() == 0)
                            || (serialTxtBuffer.getLastCanNode().equals(can) && !can.isFlagFrameEnd())) {
                        switch (can.CRC & 0x03)
                        {
                            case 0:
                                can.CRC >>=9;
                                can.crc_w = 16;
                                break;
                            case 1:
                                can.CRC >>=7;
                                can.crc_w = 20;
                                break;
                            case 2:
                                can.CRC >>=3;
                                can.crc_w = 24;
                                break;
                        }
                        can.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, can);
//                        if (can.ID!=0x12EFABCD && can.Trigger==true) {
//                            Logger.e(Command.TAG,"index:"+i+",bytes:"+ Arrays.toString(bytes.array()));
//                            Logger.e(Command.TAG, "can frame type:" + Integer.toHexString(type) + ",Frame:" + can.toString());
//                        }else{
//                            Logger.i(Command.TAG, "index:"+i+",Can frame type:" + Integer.toHexString(type) + ",Frame:" + can.toString());
//                        }
//                        if (i>=10230){
//                            byte[] bs=new byte[bytes.limit()-i];
//                            System.arraycopy(bytes.array(),i,bs,0,bs.length);
//                            Logger.e(Command.TAG,"10230 bytes:"+Arrays.toString(bs));
//                        }
                    }
                    if (specTimes > 0) {
                        serialTxtBuffer.addCanErrorFrame();
                        specTimes = 0;
                    }
                    serialTxtBuffer.addCanTotalFrame();
                }

            }

            if (can != null && i == bytes.limit() - 2 && serialTxtBuffer.getLastCanNode() != null && !serialTxtBuffer.getLastCanNode().equals(can)) {
                can.stdIdtimes=stdIdtimes;
                serialTxtBuffer.setLastCanNode(can);
//                Logger.i(Command.TAG,"stdIdTimes:"+stdIdtimes+",index:"+i+",count:"+bytes.limit()+ ",can Frame:"+can.toString());
            }
        }
    }

    /**
     * [2]:
     * =“1”此数据符合触发条件；arm对符合触发条件的数据应该以不同的颜色显示。
     * [1:0]:
     * 01：表示总线数据；
     * 11：表示总线数据，且是多字节拼接的最后一个数据；
     *
     * @param serialTxtBuffer
     * @param bytes
     */
    private void getSpiStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int bits;
//        if (Ch.equals("S1")) {
//            bits = this.bits1;
//        } else {
//            bits = this.bits2;
//        }
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1;
        bits=this.bits[chIdx];

        final int Type = SerialBusStruct.SerialBusType_SPI;
        SerialBusTxtStruct.SpiStruct spi = null;


        for (int i = 0; i < bytes.limit(); i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                //时间
                //将时间记录到数据中 uart中不需要记录时间
                getCurrTotalTime(serialTxtBuffer, bytes, i);
//                if (serialTxtBuffer.getSpiCurrSize() > 0) {
////                    SerialBusTxtStruct.SpiStruct temSpi=serialTxtBuffer.getLastSpiNode();
//                    if (serialTxtBuffer.getArmLastTime()<=armCurrTime){
//                        currTotalTime =serialTxtBuffer.getTotalTime()+(  armCurrTime-serialTxtBuffer.getArmLastTime());
//                    }else {
//                        currTotalTime=serialTxtBuffer.getTotalTime()+ armCurrTime;
//                    }
//                }else {
//                    if (serialTxtBuffer.getTotalTime()<=0)  currTotalTime=armCurrTime;
//                    else {currTotalTime=serialTxtBuffer.getTotalTime()+armCurrTime; }
//                }
//
////                Logger.i(TAG,"armcurrTime:"+armCurrTime+" - lastArmTime"+serialTxtBuffer.getArmLastTime()+ "+  totalTime:"+serialTxtBuffer.getTotalTime()+"= currTotalTime:"+currTotalTime);

//                currTotalTime = (((32768 + armCurrTime) - (totalTime % 0x7FFF)) % 0x7FFF) + totalTime;
//                Logger.i(TAG,"currTotalTime:"+currTotalTime+" armcurrtime:"+armCurrTime);
            } else {
                if (spi == null) {
                    if (serialTxtBuffer.getSpiCurrSize() > 0) {
                        spi = serialTxtBuffer.getLastSpiNode();
                        if (spi.isFlagFrameEnd()) {
                            spi = serialTxtBuffer.getStruct(Type);
                            spi.Ch = Ch;
                            spi.CurTime = serialTxtBuffer.getTotalTime();
                        }
                    } else {
                        spi = serialTxtBuffer.getStruct(Type);
                        spi.Ch = Ch;
                        spi.CurTime = serialTxtBuffer.getTotalTime();
                    }
                } else if (spi.isFlagFrameEnd()) {
                    spi = serialTxtBuffer.getStruct(Type);
                    spi.Ch = Ch;
                    spi.CurTime = serialTxtBuffer.getTotalTime();
                }


                //  spi.Trigger = (bytes.get(i + 1) & SerialBusTxtStruct.SpiStruct.SPI_2_OkTriggerCondition) == SerialBusTxtStruct.SpiStruct.SPI_2_OkTriggerCondition;
                int mask = bytes.get(i + 1) & SerialBusTxtStruct.SpiStruct.SPI_10_MULDataEnd;
//                Logger.i(TAG,"mask:"+Integer.toHexString(mask));
                if (mask == SerialBusTxtStruct.SpiStruct.SPI_10_Data) {
                    spi.appendData(Tools.ByteToHexString(bytes.get(i), bits)).updateData();
                } else if (mask == SerialBusTxtStruct.SpiStruct.SPI_10_MULDataEnd) {
                    spi.appendData(Tools.ByteToHexString(bytes.get(i), bits)).appendData(" ").updateData();
                }
//                if (spi.Data.length() > 40 * 3) {//屏幕显示范围够就可以
//                    spi.setFlagFrameEnd(true);
//                    if (serialTxtBuffer.getLastSpiNode() != null && !serialTxtBuffer.getLastSpiNode().isFlagGroupDataEnd()) {
//                        spi.Ch = SerialBusTxtStruct.SpiStruct.FLAGShow_GroupData;
//                    }
////                    if (serialTxtBuffer.getLastSpiNode()!=null) Logger.i(TAG,"换行帧："+serialTxtBuffer.getLastSpiNode().toString());
//                    serialTxtBuffer.put(Type, spi);
//                }

                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    spi.Trigger = ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition);
                    if ((serialTxtBuffer.getSpiCurrSize() > 0 && !serialTxtBuffer.getLastSpiNode().equals(spi))
                            || (serialTxtBuffer.getSpiCurrSize() == 0)
                            || (serialTxtBuffer.getLastSpiNode().equals(spi) && !spi.isFlagFrameEnd())) {
                        spi.setFlagFrameEnd(true);
                        spi.setFlagGroupDataEnd(true);
                        if (serialTxtBuffer.getLastSpiNode() != null && !serialTxtBuffer.getLastSpiNode().isFlagGroupDataEnd()) {
                            spi.Ch = SerialBusTxtStruct.SpiStruct.FLAGShow_GroupData;
                        }
                        serialTxtBuffer.put(Type, spi);
//                        Logger.e(TAG,"put frameEnd "+spi.toString());
                    }
                }

            }
            if (spi != null && i == bytes.limit() - 2 && (serialTxtBuffer.getLastSpiNode() != null) && !serialTxtBuffer.getLastSpiNode().equals(spi)) {
                serialTxtBuffer.setLastSpiNode(spi);
            }
        }

    }

    /**
     * [3]=1时，表示帧结束，[2:0]:
     * [1]：“1”表示这帧是重新启动；“0”表示无重启动；
     * [3]=0时，[2:0]:
     * [2]：表示应答信息：“1”表示应答，“0”表示无应答；
     * [1:0]：“01”写地址，“10”读地址，“11”表示数据；
     * 并非每帧都会接收到帧结束标志，当这帧没有停止位，就开始下一帧的启动时，fpga就不会发出“帧结束”信号
     *
     * @param serialTxtBuffer
     * @param bytes
     */
    private void getI2cStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        final int Type = SerialBusStruct.SerialBusType_I2C;
        //I2C：Ack列文字显示红色
        final int Ack = Color.RED;
        //I2C：触发显示Yes，不触发显示空；重启显示Yes，非重启显示空；无确认显示X，有确认显示空
        SerialBusTxtStruct.I2cStruct i2c = null;
        for (int i = 0; i <= bytes.limit() - 2; i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                //时间
                getCurrTotalTime(serialTxtBuffer, bytes, i);
            } else {
                if (i2c == null) {
                    if (serialTxtBuffer.getI2cCurrSize() > 0) {
                        i2c = serialTxtBuffer.getLastI2cNode();
                        if (i2c.isFlagFrameEnd()) {
                            i2c = serialTxtBuffer.getStruct(Type);
                            i2c.Ch = Ch;
                            i2c.CurTime = serialTxtBuffer.getTotalTime();
                        }
                    } else {
                        i2c = serialTxtBuffer.getStruct(Type);
                        i2c.Ch = Ch;
                        i2c.CurTime = serialTxtBuffer.getTotalTime();
                    }
                } else if (i2c.isFlagFrameEnd()) {
                    i2c = serialTxtBuffer.getStruct(Type);
                    i2c.Ch = Ch;
                    i2c.CurTime = serialTxtBuffer.getTotalTime();
                }

                if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_3_FrameEnd) == SerialBusTxtStruct.I2cStruct.I2C_3_FrameEnd) {
                    if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_2_FrameEnd_Reboot) == SerialBusTxtStruct.I2cStruct.I2C_2_FrameEnd_Reboot) {
                        i2c.Reboot = true;
                        i2c.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, i2c);
                    } else {
                        i2c.Reboot = false;
                    }
                } else {
                    if ((bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_2_Other_respond) == SerialBusTxtStruct.I2cStruct.I2C_2_Other_respond) {
                        i2c.Confirm = true;
                    } else {
                        i2c.Confirm = false;
                    }
                    int mask = bytes.get(i + 1) & SerialBusTxtStruct.I2cStruct.I2C_10_Other_Mask;
                    if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Write) {
//                        i2c.Addr = "W" + String.format("%02x", bytes.get(i)).toUpperCase();
                        i2c.Addr = "W" + Tools.ByteToHexString(bytes.get(i));
                    } else if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Read) {
//                        i2c.Addr = "R" + String.format("%02x", bytes.get(i)).toUpperCase();
                        i2c.Addr = "R" + Tools.ByteToHexString(bytes.get(i));
                    } else if (mask == SerialBusTxtStruct.I2cStruct.I2C_10_Other_Data) {
//                        i2c.Data += String.format("%02x", bytes.get(i)).toUpperCase() + " ";
                        i2c.appendData(Tools.ByteToHexString(bytes.get(i))).appendData(" ").updateData();
                    }
                }

                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) {
                        //本次总线通信不符合触发条件
                        i2c.Trigger = true;
                    } else {
                        //本次总线通信符合触发条件
                        i2c.Trigger = false;
                    }
                    if ((/*i2c != null &&*/ serialTxtBuffer.getI2cCurrSize() > 0 && !serialTxtBuffer.getLastI2cNode().equals(i2c))
                            || (/*i2c != null &&*/ serialTxtBuffer.getI2cCurrSize() == 0)
                            || (serialTxtBuffer.getLastI2cNode().equals(i2c) && !i2c.isFlagFrameEnd())) {
                        i2c.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, i2c);
                    }
                }

            }
            if (i2c != null && i == bytes.limit() - 2 && (serialTxtBuffer.getLastI2cNode() != null) && !serialTxtBuffer.getLastI2cNode().equals(i2c)) {
                serialTxtBuffer.setLastI2cNode(i2c);
            }
        }


    }

    private void get429Struct(String Ch, int format, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int encode;
//        if (Ch.equals("S1")) {
//            encode = this.encoding1;
//        } else {
//            encode = this.encoding2;
//        }
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1;
        encode=this.encodings[chIdx];

        final int Type = SerialBusStruct.SerialBusType_429;
        SerialBusTxtStruct.Arinc429Struct arinc429 = null;
        for (int i = 0; i <= bytes.limit() - 2; i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                getCurrTotalTime(serialTxtBuffer, bytes, i);

            } else {
                if (arinc429 == null) {
                    if (serialTxtBuffer.getArinc429CurrSize() > 0) {
                        arinc429 = serialTxtBuffer.getLast429Node();
                        if (arinc429.isFlagFrameEnd()) {
                            arinc429 = serialTxtBuffer.getStruct(Type);
                            arinc429.Ch = Ch;
                            arinc429.CurTime = serialTxtBuffer.getTotalTime();
                        }
                    } else {
                        arinc429 = serialTxtBuffer.getStruct(Type);
                        arinc429.Ch = Ch;
                        arinc429.CurTime = serialTxtBuffer.getTotalTime();
                    }
                } else if (arinc429.isFlagFrameEnd()) {
                    arinc429 = serialTxtBuffer.getStruct(Type);
                    arinc429.Ch = Ch;
                    arinc429.CurTime = serialTxtBuffer.getTotalTime();
                }


                //协议解析
                int type = bytes.get(i + 1) & 0x0F;
                switch (type) {
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Label: {
                        arinc429.Label = bytes.get(i) & 0x00FF;
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Data1: {
                        arinc429.setData1(bytes.get(i) & 0x00FF);
                        arinc429.Times++;
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_Data2: {
                        arinc429.setData2(bytes.get(i) & 0x00FF);
                        arinc429.Times++;
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckSuccess: {
                        arinc429.setData3(bytes.get(i) & 0x00FF);
                        arinc429.Times++;
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckError: {
                        arinc429.setData3(bytes.get(i) & 0x00FF);
                        arinc429.Error = "Par";
                        arinc429.setErrorNo(type);
                        arinc429.Times++;
                    }
                    break;
                    case SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError: {
                        arinc429.Error = "Frm";
                        arinc429.setErrorNo(type);
                    }
                    break;
                }


                //如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) {
                        //本次总线通信不符合触发条件
                        arinc429.Trigger = true;
                    } else { //本次总线通信符合触发条件
                        arinc429.Trigger = false;
                    }
                    if ((/*arinc429 != null &&*/ serialTxtBuffer.getArinc429CurrSize() > 0 &&
                            !serialTxtBuffer.getLast429Node().equals(arinc429))
                            || (/*arinc429 != null && */serialTxtBuffer.getArinc429CurrSize() == 0)
                            || (serialTxtBuffer.getLast429Node().equals(arinc429) && !arinc429.isFlagFrameEnd())) {
                        switch (format) {
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LabelDATA: {
//                            arinc429.Data = String.format("%02x", (0x7F & arinc429.getData3())) + " ";
//                            arinc429.Data += String.format("%02x", arinc429.getData2()) + " ";
//                            arinc429.Data += String.format("%02x", arinc429.getData1()) + " ";
//                                arinc429.appendData(Tools.ByteToHexString((byte) (0x7F & arinc429.getData3()))).appendData(" ");
//                                arinc429.appendData(Tools.ByteToHexString((byte) (arinc429.getData2()))).appendData(" ");
//                                arinc429.appendData(Tools.ByteToHexString((byte) arinc429.getData1())).appendData(" ");
                                if (arinc429.Times == 3) {
                                    //{data3[6:0],data2,data1}
                                    arinc429.appendData(getEncoding(encode, (byte) (0x7F & arinc429.getData3()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) arinc429.getData1(), 2 * 4)).appendData(" ");

                                    arinc429.updateData();
                                } else {
                                    arinc429.Data = "";
                                }
                                arinc429.SDI = "XX";
                                arinc429.SSM = "XX";

                            }
                            break;
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LDSSM: {
                                int tem = (arinc429.getData3() >> 5) & 0x03;
                                arinc429.SSM = getEncoding(ICharacterEncoding.Binary, tem, 2);
                                arinc429.SDI = "XX";
                                if (arinc429.Times == 3) {
                                    //{data3[4:0],data2[7:0],data1[7:0]}
                                    arinc429.appendData(getEncoding(encode, (byte) (0x1F & arinc429.getData3()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData1()), 2 * 4)).appendData(" ");
                                    arinc429.updateData();
                                } else {
                                    arinc429.Data = "";
                                    arinc429.SSM = "";
                                }

                            }
                            break;
                            case SerialBusTxtStruct.Arinc429Struct.Arinc429_Format_LDSSMSDI: {
                                int tem = arinc429.getData1() & 0x03;
                                arinc429.SDI = getEncoding(ICharacterEncoding.Binary, (tem), 2);
                                tem = (arinc429.getData3() >> 5) & 0x03;
                                arinc429.SSM = getEncoding(ICharacterEncoding.Binary, (tem), 2);
                                if (arinc429.Times == 3) {
                                    //{data3[4:0],data2[7:0],data1[7:2]}
                                    int tran = ((arinc429.getData3() & 0x1f) << 16 | (arinc429.getData2() & 0xFF) << 8 | (arinc429.getData1() & 0xFF)) >> 2;
                                    arinc429.setData1(tran & 0xFF);
                                    arinc429.setData2((tran >> 8) & 0xFF);
                                    arinc429.setData3((tran >> 16) & 0xFF);

                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData3()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) (arinc429.getData2()), 2 * 4)).appendData(" ");
                                    arinc429.appendData(getEncoding(encode, (byte) ((arinc429.getData1())), 2 * 4)).appendData(" ");
                                    arinc429.updateData();
                                } else {
                                    arinc429.Data = "";
                                    arinc429.SSM = "";
                                }
                                if (arinc429.Times <= 0) {
                                    arinc429.SDI = "";
                                }


                            }
                            break;
                        }
                        serialTxtBuffer.addArinc429TotalFrame();
                        if ((arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError) ||
                                (arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_CheckError)) {
                            serialTxtBuffer.addArinc429ErrorFrame();
                        }
                        if (arinc429.getErrorNo() == SerialBusTxtStruct.Arinc429Struct.Arinc429_FrameError && !arinc429.Trigger)
                            arinc429.Data = "";
                        arinc429.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, arinc429);
                    }
                }
            }

            if (arinc429 != null && i == bytes.limit() - 2 && serialTxtBuffer.getLast429Node() != null && !serialTxtBuffer.getLast429Node().equals(arinc429)) {
                serialTxtBuffer.setLast429Node(arinc429);
            }

        }

    }

    private void get1553bStruct(String Ch, SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes) {
        int encoding;
//        if (Ch.equals("S1")) {
//            encoding = encoding1;
//        } else {
//            encoding = encoding2;
//        }
        int chIdx=TChan.findChanByValue(Ch)-TChan.S1;
        encoding=this.encodings[chIdx];

        final int Type = SerialBusStruct.SerialBusType_1553B;
        SerialBusTxtStruct.MilSTD1553bStruct m1553b = null;
        for (int i = 0; i <= bytes.limit() - 2; i += 2) {
            if ((bytes.get(i + 1) & SerialBusTxtStruct.Serial_7_TimeKey) == SerialBusTxtStruct.Serial_7_TimeKey) {
                getCurrTotalTime(serialTxtBuffer, bytes, i);
            } else {
                if (m1553b == null) {
                    if (serialTxtBuffer.getMilstd1553bCurrSize() > 0) {
                        m1553b = serialTxtBuffer.getLast1553bNode();
                        if (m1553b.isFlagFrameEnd()) {
                            m1553b = serialTxtBuffer.getStruct(Type);
                            m1553b.Ch = Ch;
                            m1553b.CurTime = serialTxtBuffer.getTotalTime();
                        }
                    } else {
                        m1553b = serialTxtBuffer.getStruct(Type);
                        m1553b.Ch = Ch;
                        m1553b.CurTime = serialTxtBuffer.getTotalTime();
                    }
                } else if (m1553b.isFlagFrameEnd()) {
                    m1553b = serialTxtBuffer.getStruct(Type);
                    m1553b.Ch = Ch;
                    m1553b.CurTime = serialTxtBuffer.getTotalTime();
                }

                //解析数据
                int type = bytes.get(i + 1) & 0x0F;
                switch (type) {
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_RemoteAddr: {
//                        m1553b.RAddr =Integer.toHexString(bytes.get(i) & 0x000000FF).toUpperCase();
//                        m1553b.RAddr = Tools.ByteToHexString(bytes.get(i));
                        m1553b.RAddr = getEncoding(encoding, bytes.get(i), 2 * 4);
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_CommandStateByte6: {
                        m1553b.setTemData(bytes.get(i) & 0x0000003F);
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_CommandStateByte5: {
                        m1553b.setTemData(((m1553b.getTemData() << 5) & 0xFFFFFFE0) | (bytes.get(i) & 0x0000001F));
//                        m1553b.Data=Tools.ByteToHexString((byte)(m1553b.getTemData()>>8))+" ";
//                        m1553b.Data +=Tools.ByteToHexString((byte) (m1553b.getTemData() & 0xFF))+" ";
                        m1553b.appendData(getEncoding(encoding, (m1553b.getTemData() >> 8), 2 * 4));
                        m1553b.appendData(" ");
                        m1553b.appendData(getEncoding(encoding, (m1553b.getTemData() & 0xFF), 2 * 4));
                        m1553b.appendData(" ").updateData();

                        m1553b.Type = "C/S";
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_Data1: {
                        m1553b.setTemData(bytes.get(i) & 0x000000FF);
//                        m1553b.Data=Tools.ByteToHexString(bytes.get(i))+" ";
                        m1553b.appendData(getEncoding(encoding, bytes.get(i), 2 * 4));
                        m1553b.appendData(" ");
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_Data2: {
//                        m1553b.Data += Tools.ByteToHexString(bytes.get(i))+" ";
                        m1553b.appendData(getEncoding(encoding, bytes.get(i), 2 * 4));
                        m1553b.appendData(" ").updateData();
                        m1553b.Type = "Data";

                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_ManchesterEncodingError: {
                        m1553b.Error = "M-ch";
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_FrameEnd_CheckError: {
                        m1553b.Error = "Par";
                    }
                    break;
                    case SerialBusTxtStruct.MilSTD1553bStruct.MilSTD1553b_FrameEnd_CheckSuccess: {
//                        m1553b.Error = type;
                    }
                    break;
                }

                //如果是结束帧，就进行保存数据
                if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_6_keyEnd) == SerialBusTxtStruct.Serial_6_keyEnd)) {
                    //表示帧结束 第五位有意义
                    if (((bytes.get(i + 1) & SerialBusTxtStruct.Serial_5_okTriggerCondition) == SerialBusTxtStruct.Serial_5_okTriggerCondition)) {
                        //本次总线通信不符合触发条件
                        m1553b.Trigger = true;
                    } else { //本次总线通信符合触发条件
                        m1553b.Trigger = false;
                    }
                    if ((/*m1553b != null &&*/ serialTxtBuffer.getMilstd1553bCurrSize() > 0 &&
                            !serialTxtBuffer.getLast1553bNode().equals(m1553b))
                            || (/*m1553b != null &&*/ serialTxtBuffer.getMilstd1553bCurrSize() == 0)
                            || (serialTxtBuffer.getLast1553bNode().equals(m1553b) && !m1553b.isFlagFrameEnd())) {
                        m1553b.setFlagFrameEnd(true);
                        serialTxtBuffer.put(Type, m1553b);
//                        Logger.i(TAG,"m1553b:"+m1553b.toString());
                    }
                }

            }

            if (m1553b != null && i == bytes.limit() - 2 && serialTxtBuffer.getLast1553bNode() != null && !serialTxtBuffer.getLast1553bNode().equals(m1553b)) {
                serialTxtBuffer.setLast1553bNode(m1553b);
            }
        }
    }

    private long getCurrTotalTime(SerialTxtBuffer serialTxtBuffer, ByteBuffer bytes, int i) {
        /// 599999999代表99m59s999ms990us
        final long MaxTime = 599999999;

        long currTotalTime = 0;
        int armCurrTime = (((bytes.get(i + 1) & 0x7F) << 8) | (bytes.get(i) & 0xFF)) & 0x7FFF;
        long totalTime = serialTxtBuffer.getTotalTime();
        currTotalTime = (((32768 + armCurrTime) - (totalTime % 0x7FFF)) % 0x7FFF) + totalTime;
        serialTxtBuffer.setArmLastTime(armCurrTime);
        if (currTotalTime >= MaxTime) currTotalTime = currTotalTime % MaxTime;
        serialTxtBuffer.setTotalTime(currTotalTime);
        return currTotalTime;
    }

    public static String getDebugBytesToString(ByteBuffer bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.limit(); i++) {
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" ");
        }
        return sb.toString();
    }

    public static String getDebugBytesToString(ByteBuffer bytes, int start, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + len; i++) {
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" ");
        }
        return sb.toString();
    }

    public static String getDebugBytesToString(int index, ByteBuffer bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < bytes.limit(); i++) {
            sb.append(Tools.ByteToHexString(bytes.get(i))).append(" ");
        }
        return sb.toString();
    }

    static char getExtASCII(int id) {
        char val = '\0';
        if (id >= 0xA1 && id <= 0xFF) {
            switch (id) {
                case 0xAD:
                    val = '\0';
                    break;
                default:
                    val = (char) id;
                    break;
            }
        }
        return val;
    }

    public static String getEncoding(int Coding, int id, int bitWidth) {
        String s = "";
        int width = 1;
        Long data = id | (1L << 63);
        switch (Coding) {
            case ICharacterEncoding.ASCII: {
//                char s1 = ((id >= 48 && id <= 57) || (id >= 65 && id <= 90) || (id >= 97 && id <= 122)) ? (char) id : '\0';
                char s1 = ((id >= 32) && (id <= 126)) ? (char) id : '\0';
                if (id >= 0xA1) {
                    s1 = getExtASCII(id);
                }
                if (s1 == '\0') {
                    s = "...........";
                    width = 3;
                } else {
                    s = String.valueOf(s1);
                    width = 1;
                }
            }
            break;
            case ICharacterEncoding.Binary: {
                s = Long.toBinaryString(data);
                width = bitWidth;
            }
            break;
            case ICharacterEncoding.Decimal: {
                s = Long.toString(data);
                width = bitWidth;
            }
            break;
            case ICharacterEncoding.Hex: {
                s = Long.toHexString(data);
                width = (int) Math.ceil(bitWidth / 4.0f);
            }
            break;
            case ICharacterEncoding.Octal: {
                s = Long.toOctalString(data);
                width = (int) Math.ceil(bitWidth / 4.0f);
            }
            break;
        }
        if (Coding == ICharacterEncoding.ASCII) {
            return s.substring(s.length() - width, s.length());
        } else {
            //取字串s的后width个字符输出
            return s.toUpperCase().substring(s.length() - width, s.length());
        }
    }

    @SuppressLint("DefaultLocale")
    public static void main(String[] arg) {
        ByteBuffer bb1 = ByteBuffer.allocate(9);
        ByteBuffer bb2 = ByteBuffer.allocate(6);
        bb1.put((byte) 1);
        bb1.put((byte) 2);
        bb1.put((byte) 3);
        bb2.put((byte) 4);
        bb2.put((byte) 5);
        bb2.put((byte) 6);

        bb1.position(0);
        bb1.limit(3);
        System.out.println("bb1.value(0):" + bb1.get(0));
        bb2.position(0);
        bb2.limit(3);
        bb1.put(bb2);
        System.out.println("bb1.value(1):" + bb1.get(1));
    }
}

