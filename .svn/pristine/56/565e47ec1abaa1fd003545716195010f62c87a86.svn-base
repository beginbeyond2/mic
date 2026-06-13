package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Bus.LinBus;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

public class SerialBusTxtStruct {
    //region
    public static class SerialBusTxtStructHolder {
        public static final SerialBusTxtStruct instance = new SerialBusTxtStruct();
    }

    public static SerialBusTxtStruct getInstance() {
        return SerialBusTxtStructHolder.instance;
    }
    //endregion

    //region interface
    public interface ISerialBusTxtCSV {
        //        void frameEnd();
//        String getCsv();
        String toCSVHead();
        String toCSV();
        String getCh();
    }
    //endregion

    //region 名称定义
    public static final int SerialBusType_UART = 1;
    public static final int SerialBusType_LIN = 2;
    public static final int SerialBusType_CAN = 3;
    public static final int SerialBusType_SPI = 4;
    public static final int SerialBusType_I2C = 5;
    public static final int SerialBusType_429 = 6;
    public static final int SerialBusType_1553B = 7;
    //endregion

    /** 第7位 是0时，为数据类型 */
    //public static final int Serial_7_DataType=0;
    /**
     * 第7位 是1时，数据类型和数据值组合在一起共15bit表示时间戳。fpga有两种情况会给出时间戳，一种情况是帧起始；另一种情况是长时间没有码字发生
     */
    public static final int Serial_7_TimeKey = 0x80;
    /**
     * 第6位 是1时，表示帧结束了，此时bit[5]有意义
     */
    public static final int Serial_6_keyEnd = 0x40;
    /** 本次总线通信不符合触发条件 */
    //public static final int Serial_5_errorTriggerCondition=0;
    /**
     * 本次总线通信符合触发条件
     */
    public static final int Serial_5_okTriggerCondition = 0x20;

    public static String toCSVTitleUart() {
        return "Ch,Data,Color";
    }

    public static String toCSVTitleLin() {
        return "Ch,CurTime,Id,Data,Error,Check,Trigger";
    }

    public static String toCSVTitleCan() {
        return "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum";
    }

    public static String toCSVTitleSpi() {
        return "Ch,CurrTime,Data,Trigger";
    }

    public static String toCSVTitleI2c() {
        return "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot";
    }

    public static String toCSVTitleM1553b() {
        return "Ch,CurTime,Type,RAddr,Data,Trigger,Error";
    }

    public static String toCSVTitleArinc492() {
        return "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger";
    }

    /**
     * Uart串型文本结构
     */
    public class UartStruct implements ISerialBusTxtCSV {
        /**
         * “010”校验正确，无停止位
         */
        public static final int CHECK_OK_NoStop = 0x02;
        /**
         * =“011”校验错误，无停止位；
         */
        public static final int CHECK_Error_NoStop = 0x03;
        /**
         * =“110” 校验正确，有停止位；
         */
        public static final int CHECK_OK_Stop = 0x06;
        /**
         * =“111” 校验错误，有停止位；
         */
        public static final int CHECK_Error_Stop = 0x07;


        public String Ch = TChan.getChannelName(TChan.S1);
        public int Color;
        public int Data;
        public String csv = null;
        //region 属性
        private boolean FlagFrameEnd=false;
        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }
        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }
        //endregion

        public UartStruct() {
        }

        public UartStruct clean() {
            Ch = TChan.getChannelName(TChan.S1);
            Color = 0xFFFFFF;
            Data = 0;
            FlagFrameEnd=false;
            return this;
        }

        @Override
        public String toString() {
            return "Ch" + Ch +
                    "Data:" + Integer.toHexString(Data) +
                    "Color:0x" + Integer.toHexString(Color);
        }


        //        @Override
//        public void frameEnd(){
//              csv= toCSV();
//        }
//        @Override
//        public String getCsv(){return csv;}

        @Override
        public String toCSVHead() {
            return "Ch,Data,Color";
        }

        @Override
        public String toCSV() {
            return Ch + "," + Integer.toHexString(Data) + "," + "0x" + Integer.toHexString(Color);
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

    public class LinStruct implements ISerialBusTxtCSV {

        /**
         * [3:0]：    “0001”奇偶校验正确的ID；
         */
        public static final int Lin_30_CheckYes = 0x01;
        /**
         * [3:0]：    “1001”奇偶校验错误的ID；
         */
        public static final int Lin_30_CheckNo = 0x09;
        /**
         * [3:0]：    “0010”数据且有停止位；
         */
        public static final int Lin_30_DataYes = 0x02;
        /**
         * [3:0]：    “1010”数据且无停止位；
         */
        public static final int Lin_30_DataNo = 0x0A;
        /**
         * [3:0]：    “0011”正确校验和；
         */
        public static final int Lin_30_CheckSumYes = 0x03;
        /**
         * [3:0]：    “1011”错误校验和；
         */
        public static final int Lin_30_CheckSumNo = 0x0B;

        public int linType = LinBus.LIN_TYPE_1_3;
        public String Ch = TChan.getChannelName(TChan.S1);
        public long CurTime = 0;
        public int Id = 0;
        public String Data = "";
        public String Error = "";
        public int Check = 0;
        public boolean Trigger = false;
        //region 属性
        private boolean FlagFrameEnd = false;

        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }

        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }

        //endregion
        private StringBuilder sbData = new StringBuilder();

        public LinStruct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            this.Data = sbData.toString();
        }

        public LinStruct clean() {
            Ch = TChan.getChannelName(TChan.S1);
            CurTime = 0;
            Id = 0;
            Data = "";
            Error = "";
            Check = 0;
            Trigger = false;
            sbData.delete(0, sbData.length());
            FlagFrameEnd=false;
            return this;
        }

        @Override
        public String toString() {
            return "Ch:" + Ch +
                    " CurTime:" + CurTime +
                    " Id:" + Id +
                    " Data:" + Data +
                    " Error:" + Error +
                    " Check:" + Check +
                    " Trigger:" + Trigger
                    ;
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurTime,Id,Data,Error,Check,Trigger";
        }

        @Override
        public String toCSV() {
            if (linType != LinBus.LIN_TYPE_1_3) { //FIXME 非LIN1.3时FPGA传过来的data会在末尾多出来校验位数据，这里去掉。
                if (Data.length() > 2) {
                    Data = Data.substring(0, Data.length() - 3);
                }
            }
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Integer.toHexString(Id) + "," + Data + "," + Error + "," + Integer.toHexString(Check) + "," + Trigger;
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

    /**
     * can 串型文本结构
     */
    public class CanStruct implements ISerialBusTxtCSV {
        public static final int Type_STDID = 0x00;
        public static final int Type_EXTID = 0x01;
        public static final int Type_STDREMOTEID=0x02;
        public static final int Type_EXTREMOTEID=0x03;

        /**
         * [3:0]：   “0001”：标准ID；
         */
        public static final int Can_30_stdId = 0x01;
        /**
         * [3:0]：   “0110”：扩展ID；
         */
        public static final int Can_30_extId = 0x06;
        /**
         * [3:0]：   “0010”：DLC；
         */
        public static final int Can_30_DLC = 0x02;
        /**
         * [3:0]：   “0011”：DATA；
         */
        public static final int Can_30_DATA = 0x03;
        /**
         * [3:0]：   “0100”：CRC；
         */
        public static final int Can_30_CRC = 0x04;
        /**
         * [3:0]：   “0101”：错误；
         */
        public static final int Can_30_Error = 0x05;
        /**
         * [3:0]：   “0111”：过载帧；
         */
        public static final int Can_30_Overload = 0x07;
//          /**  [3:0]：   “1XX1”：帧结束，有确认；  */
//          public static final int Can_30_FrameEnd_Confirm=0x08 | 0x01;
//          /**  [3:0]：   “1XX0”：帧结束，无确认；（当无确认时，会在之前先给出“错误”）  */
//          public static final int Can_30_FrameEnd_NoConfirm=0x08;
//          /**  [3:0]：   “1X0X”：帧结束，数据帧；  */
//          public static final int Can_30_
//          /**  [3:0]：   “1X1X”：帧结束，远程帧；  */
//          public static final int Can_30_
//          /**  [3:0]：   “1XXX”：帧结束  */
//          public static final int Can_30_FrameEnd=


        /**
         * [3:0]=“0101”（错误）时，数据段定义：  1---位填充错误；
         */
        public static final int Can_Error_1 = 0x01;
        /**
         * [3:0]=“0101”（错误）时，数据段定义：  2---格式错误；
         */
        public static final int Can_Error_2 = 0x02;
        /**
         * [3:0]=“0101”（错误）时，数据段定义：  3---ACK错误；
         */
        public static final int Can_Error_3 = 0x03;
        /**
         * [3:0]=“0101”（错误）时，数据段定义：  4---CRC错误；
         */
        public static final int Can_Error_4 = 0x04;
        /**
         * [3:0] 掩码
         */
        public static final int Can_Error_MASK = 0x0F;

        /**
         * 帧结束 有确认
         */
        public static final int Can_FrameEnd_Confirm = 0;
        /**
         * 帧结束  无确认
         */
        public static final int Can_FrameEnd_NoConfirm = 1;
        /**
         * 帧结束  数据帧
         */
        public static final int Can_FrameEnd_Data = 2;
        /**
         * 帧结束  远程帧
         */
        public static final int Can_FrameEnd_Remote = 3;


        public String Ch = TChan.getChannelName(TChan.S1);
        /**
         * 时间单位为10us
         */
        public long CurTime = 0;
        public int ID = 0;
        /**
         * 枚举类型：Type_STDID
         *          TYPE_EXTID
         */
        public int TypeEnum = 0;
        public int DLC = 0;
        public String Data = "";
        public int CRC = 0;
        public int ErrorEnum = 0;
        public boolean Trigger = false;
        public int FrameEndEnum = 0;
        public int crc_w = 16;
        /**
         * 标准id次数，记录解析的次数。解决解析多帧拼接问题
         */
        public int stdIdtimes=0;
        //region 属性
        private boolean FlagFrameEnd = false;

        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }

        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }
        //endregion

        private StringBuilder sbData = new StringBuilder();

        public CanStruct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            this.Data = sbData.toString();
        }

        public CanStruct clean() {
            Ch = TChan.getChannelName(TChan.S1);
            /** 时间单位为10us */
            CurTime = 0;
            ID = 0;
            /** 枚举类型：Type_STDID  TYPE_EXTID*/
            TypeEnum = 0;
            DLC = 0;
            Data = "";
            CRC = 0;
            ErrorEnum = 0;
            Trigger = false;
            FrameEndEnum = 0;
            FlagFrameEnd=false;
            stdIdtimes=0;
            sbData.delete(0, sbData.length());
            return this;
        }

        @Override
        public String toString() {
            return "Ch:" + Ch +
                    " CurTime:" + CurTime +
                    " ID:" + ID +
                    " TypeEnum:" + TypeEnum +
                    " DLC:" + DLC +
                    " Data:" + Data +
                    " CRC:" + CRC +
                    " ErrorEnum:" + ErrorEnum +
                    " Trigger:" + Trigger +
                    " FrameEndEnum:" + FrameEndEnum
                    ;
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurTime,Id,TypeEnum,DLC,Data,CRC,Error,Trigger,FrameEndEnum";
        }

        @Override
        public String toCSV() {
            return Ch + "," +
                    TBookUtil.getStringFrom10us(CurTime) + "," +
                    getId() + "," +
                    getTypeEnum() + "," +
                    DLC + "," +
                    Data + "," +
                    Integer.toHexString(CRC) + "," +
                    getErrorEnum() + "," +
                    Trigger + "," +
                    FrameEndEnum;
        }

        @Override
        public String getCh() {
            return Ch;
        }

        public String getErrorEnum(){
            switch (ErrorEnum){
                case Can_Error_1:return "Bit";
                case Can_Error_2:return "Fmt";
                case Can_Error_3:return "Ack";
                case Can_Error_4:return "CRC";
            }
            return "";
        }

        public String getTypeEnum(){
            switch (this.TypeEnum){
                case Type_STDID:return "SFF";
                case Type_STDREMOTEID:return "SRF";
                case Type_EXTID:return "EFF";
                case Type_EXTREMOTEID:return "ERF";
                default:return "";
            }
        }
        public String getId(){
            String id="";
            switch (this.TypeEnum){
                case Type_STDID:
                case Type_STDREMOTEID:
                    //3个字符，11位
                    id= String.format("%03x",ID).toUpperCase();
                    break;
                default:
                    //8个字符，29位
                    id= String.format("%08x",ID).toUpperCase();
                    break;
            }
            return id;
        }
    }

    /**
     * spi 串型文本结构
     */
    public class SpiStruct implements ISerialBusTxtCSV {
        public static final String FLAGShow_GroupData="--";

        /**
         * [2]:  =“1”此数据符合触发条件；
         */
        public static final int SPI_2_OkTriggerCondition = 0x04;
        /**
         * [1:0]:        01：表示总线数据；
         */
        public static final int SPI_10_Data = 0x01;
        /**
         * [1:0]:  11：表示总线数据，且是多字节拼接的最后一个数据；
         */
        public static final int SPI_10_MULDataEnd = 0x03;

        public String Ch = TChan.getChannelName(TChan.S1);
        public long CurTime = 0;
        public String Data = "";
        public boolean Trigger = false;
        //region 属性
        private boolean FlagFrameEnd = false;
        //一组数据是否解析完成
        private boolean FlagGroupDataEnd=false;

        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }

        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }

        public boolean isFlagGroupDataEnd() {
            return FlagGroupDataEnd;
        }

        public void setFlagGroupDataEnd(boolean flagGroupDataEnd) {
            FlagGroupDataEnd = flagGroupDataEnd;
        }

        //endregion
        private StringBuilder sbData = new StringBuilder();

        public SpiStruct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            this.Data = sbData.toString();
        }


        public SpiStruct clean() {
            this.Ch = TChan.getChannelName(TChan.S1);
            CurTime = 0;
            Data = "";
            Trigger = false;
            sbData.delete(0, sbData.length());
            FlagFrameEnd = false;
            FlagGroupDataEnd=false;
            return this;
        }

        @Override
        public String toString() {
            return "Ch:" + Ch +
                    "CurrTime:" + CurTime +
                    "Data:" + Data +
                    "Trigger:" + Trigger+
                    "FlagGroupDataEnd:"+FlagGroupDataEnd
                    ;
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurrTime,Data,Trigger";
        }

        @Override
        public String toCSV() {
            return Ch + "," +
                    TBookUtil.getStringFrom10us(CurTime) + "," +
                    Data + "," +
                    Trigger
                    ;
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

    /**
     * i2c 串型文本结构
     */
    public class I2cStruct implements ISerialBusTxtCSV {
        /**
         * [3]=1时，表示帧结束
         */
        public static final int I2C_3_FrameEnd = 0x08;
        /**
         * [2:0]: [1]：“1”表示这帧是重新启动；“0”表示无重启动
         */
        public static final int I2C_2_FrameEnd_Reboot = 0x02;
        /**
         * [3]=0时，[2:0]:   [2]：表示应答信息：“1”表示应答，“0”表示无应答
         */
        public static final int I2C_2_Other_respond = 0x04;
        /**
         * [3]=0时，[2:0]: [1:0]：“01”写地址，“10”读地址，“11”表示数据；
         */
        public static final int I2C_10_Other_Write = 0x01;
        /**
         * [3]=0时，[2:0]: [1:0]：“01”写地址，“10”读地址，“11”表示数据；
         */
        public static final int I2C_10_Other_Read = 0x02;
        /**
         * [3]=0时，[2:0]: [1:0]：“01”写地址，“10”读地址，“11”表示数据；
         */
        public static final int I2C_10_Other_Data = 0x03;
        public static final int I2C_10_Other_Mask = 0x03;

        public String Ch = TChan.getChannelName(TChan.S1);
        public long CurTime = 0;
        public String Addr = "--";
        public String Data = "";
        /**
         * ACK
         */
        public boolean Confirm = false;
        public boolean Trigger = false;
        public boolean Reboot = false;
        /**
         * ACK颜色
         */
        public int Color = 0xFFFFFFFF;

        private StringBuilder sbData = new StringBuilder();
        //region 属性
        private boolean FlagFrameEnd=false;
        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }
        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }
        //endregion

        public I2cStruct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            this.Data = sbData.toString();
        }

        public I2cStruct clean() {
            Ch = TChan.getChannelName(TChan.S1);
            CurTime = 0;
            Addr = "--";
            Data = "";
            /**ACK */
            Confirm = false;
            Trigger = false;
            Reboot = false;
            sbData.delete(0, sbData.length());
            FlagFrameEnd=false;
            return this;
        }

        @Override
        public String toString() {
            return "Ch:" + Ch +
                    "CurTime:" + CurTime +
                    "Addr:" + Addr +
                    "Data:" + Data +
                    "Confirm:" + Confirm +
                    "Trigger:" + Trigger +
                    "Reboot:" + Reboot
                    ;
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurTime,Addr,Data,Confirm,Trigger,Reboot";
        }

        @Override
        public String toCSV() {
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Addr + "," + Data + "," + Confirm + "," + Trigger + "," + Reboot;
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

    public class MilSTD1553bStruct implements ISerialBusTxtCSV {

        /**
         * [3:0]：      “0001”远程终端地址；
         */
        public static final int MilSTD1553b_RemoteAddr = 0x01;
        /**
         * [3:0]：      “0010”指令/状态字9-14位；（6bit）
         */
        public static final int MilSTD1553b_CommandStateByte6 = 0x02;
        /**
         * [3:0]：      “0011”指令/状态字15-19位；（5bit）
         */
        public static final int MilSTD1553b_CommandStateByte5 = 0x03;
        /**
         * [3:0]：      “0100”数据字1；
         */
        public static final int MilSTD1553b_Data1 = 0x04;
        /**
         * [3:0]：      “0101”数据字2；
         */
        public static final int MilSTD1553b_Data2 = 0x05;
        /**
         * [3:0]：      “1111”帧结束，奇校验正确；
         */
        public static final int MilSTD1553b_FrameEnd_CheckSuccess = 0x0F;
        /**
         * [3:0]：      “0111”帧结束，奇校验错误；
         */
        public static final int MilSTD1553b_FrameEnd_CheckError = 0x07;
        /**
         * [3:0]：      “0110”曼切斯特码错误；
         */
        public static final int MilSTD1553b_ManchesterEncodingError = 0x06;

        public String Ch =TChan.getChannelName(TChan.S1);
        public long CurTime = 0;
        public String Type = "";
        public String RAddr = "N/A";
        public String Data = "";
        public boolean Trigger = false;
        public String Error = "";

        private int temData = 0;
        //region 属性
        private boolean FlagFrameEnd=false;
        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }
        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }
        //endregion
        public void setTemData(int data) {
            this.temData = data;
        }

        public int getTemData() {
            return this.temData;
        }

        private StringBuilder sbData = new StringBuilder();

        public MilSTD1553bStruct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            Data = sbData.toString();
        }

        public MilSTD1553bStruct clean() {
            this.Ch = TChan.getChannelName(TChan.S1);
            this.CurTime = 0;
            this.Type = "";
            this.RAddr = "N/A";
            this.Data = "";
            this.Trigger = false;
            this.Error = "";
            this.temData = 0;
            sbData.delete(0, sbData.length());
            FlagFrameEnd=false;
            return this;
        }

        @Override
        public String toString() {
            return " Ch:" + Ch +
                    " CurTime:" + CurTime +
                    " Type:" + Type +
                    " RAddr:" + RAddr +
                    " Data:" + Data +
                    " Error:" + Trigger +
                    "";
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurTime,Type,RAddr,Data,Trigger,Error";
        }

        @Override
        public String toCSV() {
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Type + "," + RAddr + "," + Data + "," + Trigger+","+Error;
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

    public class Arinc429Struct implements ISerialBusTxtCSV {
        /**
         * Label+Data
         */
        public static final int Arinc429_Format_LabelDATA = 0x00;
        /**
         * Label+D+SSM
         */
        public static final int Arinc429_Format_LDSSM = 0x01;
        /**
         * Label+Data+SSM+SDI
         */
        public static final int Arinc429_Format_LDSSMSDI = 0x02;

        /**
         * [3:0]：      “0001”LABEL；
         */
        public static final int Arinc429_Label = 0x01;
        /**
         * [3:0]：      “0010”数据1；
         */
        public static final int Arinc429_Data1 = 0x02;
        /**
         * [3:0]：      “0011”数据2；
         */
        public static final int Arinc429_Data2 = 0x03;
        /**
         * [3:0]：      “0110”数据3；奇偶校验错误；
         */
        public static final int Arinc429_CheckError = 0x06;
        /**
         * [3:0]：      “0100”数据3；奇偶校验正确；
         */
        public static final int Arinc429_CheckSuccess = 0x04;
        /**
         * [3:0]：      “0101”错误帧；
         */
        public static final int Arinc429_FrameError = 0x05;


        public String Ch = TChan.getChannelName(TChan.S1);
        public long CurTime = 0;
        public int Label = 0;
        public String SDI = "";
        public String Data = "";
        public String SSM = "";
        public String Error = "";
        public boolean Trigger = false;

        public int Times=0;

        //region 属性
        private boolean FlagFrameEnd=false;
        public boolean isFlagFrameEnd() {
            return FlagFrameEnd;
        }
        public void setFlagFrameEnd(boolean flagFrameEnd) {
            FlagFrameEnd = flagFrameEnd;
        }
        //endregion
        private int errorNo;

        public int getErrorNo() {
            return errorNo;
        }

        public void setErrorNo(int errorNo) {
            this.errorNo = errorNo;
        }

        private int data1, data2, data3;

        public void setData1(int data1) {
            this.data1 = data1;
        }

        public void setData2(int data2) {
            this.data2 = data2;
        }

        public void setData3(int data3) {
            this.data3 = data3;
        }

        public int getData1() {
            return this.data1;
        }

        public int getData2() {
            return this.data2;
        }

        public int getData3() {
            return this.data3;
        }

        private StringBuilder sbData = new StringBuilder();

        public Arinc429Struct appendData(String data) {
            sbData.append(data);
            return this;
        }

        public void updateData() {
            this.Data = sbData.toString();
        }

        public Arinc429Struct clean() {
            this.Ch = TChan.getChannelName(TChan.S1);
            this.CurTime = 0;
            this.Label = 0;
            this.SDI = "";
            this.Data = "";
            this.SSM = "";
            this.Error = "";
            this.Trigger = false;

            this.data1 = 0;
            data2 = 0;
            data3 = 0;
            sbData.delete(0, sbData.length());
            FlagFrameEnd=false;
            Times=0;
            return this;
        }

        @Override
        public String toString() {
            return "Ch:" + Ch +
                    " CurTime:" + CurTime +
                    " Label:" + Label +
                    " SDI:" + SDI +
                    " Data:" + Data +
                    " SSM:" + SSM +
                    " Error:" + Error +
                    " Trigger:" + Trigger +
                    "";
        }

        @Override
        public String toCSVHead() {
            return "Ch,CurTime,Label,SDI,Data,SSM,Error,Trigger";
        }

        @Override
        public String toCSV() {
            return Ch + "," + TBookUtil.getStringFrom10us(CurTime) + "," + Integer.toString(Label,8) + "," + SDI + "," + Data + "," + SSM + "," + Error + "," + Trigger;
        }

        @Override
        public String getCh() {
            return Ch;
        }
    }

}
