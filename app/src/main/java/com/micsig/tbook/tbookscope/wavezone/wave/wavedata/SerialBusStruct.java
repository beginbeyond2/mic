package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

//import android.annotation.IntDef;


import androidx.annotation.IntDef;

import com.micsig.smart.Property;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.ui.util.TBookUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by liwb on 2017/10/17.
 */

public class SerialBusStruct {

    //region
    public static class SerialBusStructHolder {
        public static final SerialBusStruct instance = new SerialBusStruct();
    }

    public static SerialBusStruct getInstance() {
        return SerialBusStructHolder.instance;
    }
    //endregion

    //region 名称定义
    //原定义
//    public static final int SerialBusType_UART=1;
//    public static final int SerialBusType_LIN=2;
//    public static final int SerialBusType_CAN=3;
//    public static final int SerialBusType_SPI=4;
//    public static final int SerialBusType_I2C=5;
//    public static final int SerialBusType_429=6;
//    public static final int SerialBusType_1553B=7;
    @IntDef({SerialBusType_UART,SerialBusType_LIN,SerialBusType_CAN,SerialBusType_SPI,SerialBusType_I2C,SerialBusType_429,SerialBusType_1553B})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SerialBusType{}
    //同property
    public static final int SerialBusType_UART= Property.BUS_UART;
    public static final int SerialBusType_LIN=Property.BUS_LIN;
    public static final int SerialBusType_CAN=Property.BUS_CAN;
    public static final int SerialBusType_SPI=Property.BUS_SPI;
    public static final int SerialBusType_I2C=Property.BUS_I2C;
    public static final int SerialBusType_429=Property.BUS_429;
    public static final int SerialBusType_1553B=Property.BUS_1553B;
    //endregion

    /**
     * 起始数据 0x00;
     */
    public static final int DataType_BeginData=0x00;


    public interface ISerialBusCSV {
        String toCsvHead();
        String toCSV();
    }

    /**
     * UART串口结构
     */
    public class UartStruct implements ISerialBusCSV {
        //region  结构
        /** 1”9bit数据的前8位，/** 0”9bit数据的第九位*/
        public static final int DataType_3=0x08;
        /**
         * 校验正确，无停止位
         */
        public static final int DataType_CRCSucc_NoStop=0x02;
        /**
         * 校验错误，无停止位
         */
        public static final int DataType_CRCFailed_noStop=0x03;
        /**
         * 校验正确，有停止位
         */
        public static final int DataType_CRCSucc_Stop=0x06;
        /**
         * 校验错误，有停止位
         */
        public static final int DataType_CRCFailed_Stop=0x07;

        /**
         * 是否是有开始帧
         */
        public boolean isBeginFrame;
        /**
         * x轴位置，单位：时间
         */
        public int BeginX;
        public int EndX;
        /**
         * 数据类型：
         * 【0-2】： 010  校验正确，无停止位
         *           011  校验错误，无停止位
         *           110  校验正确，有停止位
         *           111  校验错误，有停止位
         *  【3】： 1  9bit数据的前8位， 0 9bit数据的第九位
         */
        public byte DataType;
        /**
         * 数据 ,协议上是byte 由于可能拼数据，所以使用short
         */
        public String Data;
        public int Id;
        /** 显示颜色*/
        public int  DataColor;
        /**
         * 预留
         */
        public short Reserve;
        //endregion


        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,Color";
        }

        @Override
        public String toCSV() {
            if (Data==null)return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UartStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", Id=").append(Id);
            sb.append(", DataColor=0x").append(Integer.toHexString(DataColor));
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * LinStruct
     */
    public class LinStruct implements ISerialBusCSV{
        /** 0001”奇偶校验正确的ID*/
        public static final int ID_ODD_Sucess=0x01;
        /** 1001”奇偶校验错误的ID*/
        public static final int ID_ODD_Failed=0x09;
         /** 0010”数据且有停止位*/
        public static final int Data_Stop=0x02;
        /** 1010”数据且无停止位*/
        public static final int Data_NoStop=0x0A;
        /** 0011”正确校验和*/
        public static final int Check_Success=0x03;
        /** 1011”错误校验和*/
         public static final int Check_Failed=0x0B;
         /** 0100”正确的同步信号*/
        public static final int SYNC_Success=0x04;
        /** 1100”错误的同步信号*/
        public static final int SYNC_Failed=0x0C;
        /** 0101”唤醒信号*/
        public static final int Wake_Success=0x05;
        /** 1101”唤醒错误*/
        public static final int Wake_Failed=0x0D;
        /** 1000”同步间隔的上升沿*/
        public static final int SYNC_Distance=0x08;

        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /**
         *数据
         */

        public String Data;
        /**
         *数据颜色
         */
        public int DataColor;
        /**
         * 预留
         */
        public short Reserve;

        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null)return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return  b+","+e+","+Data+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("LinStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", DataColor=").append(Integer.toHexString(DataColor));
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }

    public class CanStruct implements ISerialBusCSV{
        /** 0001”：标准ID */
        public static final int StandardID=0x01;
        /** 0110”：扩展ID*/
        public static final int ExtendId=0x06;
        /** 0010”：DLC*/
        public static final int DLC=0x02;
        /** 0011”：DATA*/
        public static final int DATA=0x03;
        /** 0100”：CRC*/
        public static final int CRC=0x04;
        /** 1000”：错误帧*/
        public static final int Error=0x08;
        /** 1001”：过载帧*/
        public static final int OverLoad=0x09;
        /** 1010”：帧结束，有确认*/
        public static final int ConfirmOver=0x0A;
        /** 1110”：帧结束，无确认*/
        public static final int NoConfirmOver=0x0E;
        /** 1011”：问题帧*///不满足can协议的问题帧
        public static final int Trouble=0x0B;

        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /**
         *数据
         */
        public String Data="";
        public int ID;
        /**
         *数据颜色
         */
        public int DataColor;
        /**
         * 预留
         */
        public short Reserve;

        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null) return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+Data+",+0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CanStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", ID=").append(ID);
            sb.append(", DataColor=").append(Integer.toHexString(DataColor));
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }

    public class SpiStruct implements ISerialBusCSV{
        /**
         * 第三位为1时，停止位,[2:0]无效
          */
        public static final int DataType_3bit_Stop=0x08;
        /**
         * 总线数据
         */
        public static final int DataType_2bit_BusData=0x01;
        /**
         * 最后一个总线数据
         */
        public static final int DataType_2bit_LastBusData=0x03;

        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /**
         *数据
         */
        public String Data;
        /**
         *数据颜色
         */
        public int DataColor;
        /**
         * 预留
         */
        public short Reserve;

        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null) return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SpiStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", DataColor=").append(DataColor);
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }


    public class I2cStruct implements ISerialBusCSV{
        /**
         * 第三位为1时，停止位,[2:0]无效
         */
        public static final int DataType_3bit_Stop=0x08;
        /**
         * 第2位为1时，表示应答 100b
         */
        public static final int DataType_2bit_Response=0x04;
        /**
         * 第2位为0时，表示无应答
         */
        public static final int DataType_2Bit_NoResponse=0x00;

        /**
         * 第0~1位为01b时，表示写
         */
        public static final int DataType_1bit_Write=0x01;
        /**
         * 第0~1位为：10b时，表示读
         */
        public static final int DataType_1bit_Read=0x02;
        /**
         * 第0~1位为：11b时，表示数据
         */
        public static final int DataType_1bit_Data=0x03;


        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /**
         *数据
         */
        public String Data;
        /** 简写形式 */
        public String ShortData;
        /**
         *数据颜色
         */
        public int DataColor;
        /**
         * 预留
         */
        public short Reserve;

        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null) return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+Data+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("I2cStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", ShortData='").append(ShortData).append('\'');
            sb.append(", DataColor=").append(DataColor);
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }

    public class MilSTD1553bStruct implements ISerialBusCSV{
        /**0001 远程终端地址*/
        public static final int DataType_RemoteAddr=0x01;
        /**0010  指令/状态字9-14位（6bit）*/
        public static final int DataType_Command6bit=0x02;
        /**0011  指令/状态字15-19位 （5bit）*/
        public static final int DataType_Command5bit=0x03;
        /**0100  数据字1*/
        public static final int DataType_Data1=0x04;
        /**0101  数据字2*/
        public static final int DataType_Data2=0x05;
        /**1111  帧结束，奇校验正确*/
        public static final int DataType_OverSuccess=0x0F;
        /**0111  帧结束，奇校验错误*/
        public static final int DataType_OverFailed=0x07;
        /**0110  曼切斯特码错误*/
        public static final int DataType_ManchesterCodeFailed=0x06;

        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /**
         *数据
         */
        public String Data;
        public int Id;
        /**
         *数据颜色
         */
        public int DataColor;
        /**
         * 预留
         */
        public short Reserve;

        @Override
        public String toCsvHead() {
            return "BeginX,EndX,Data,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null) return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+","+Data+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MilSTD1553bStruct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", Id=").append(Id);
            sb.append(", DataColor=").append(DataColor);
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }

    public class Arinc429Struct implements ISerialBusCSV{
        /**SDI值  DATA值  SSM值 */
        public static final int Arinc429Type_1=0x02;
        /** DATA值  SSM值 */
        public static final int Arinc429Type_2=0x01;
        /** DATA值 */
        public static final int Arinc429Type_3=0x00;

        /**0001  LABEL*/
        public static final int DataType_Label=0x01;
        /**0010  数据1*/
        public static final int DataType_Data1=0x02;
        /**0011  数据2*/
        public static final int DataType_Data2=0x03;
        /**0100  数据3*/
        public static final int DataType_Data3=0x04;
        /**0101  错误帧*/
        public static final int DataType_Error=0x05;

        /**
         * 是否有开始帧
         */
        public boolean isBeginFrame;
        /**
         * X开始位置
         */
        public int BeginX;
        /**
         * 结束位置
         */
        public int EndX;
        /**
         * 数据类型
         */
        public int DataType;
        /** 数据ID 或为数据组合用*/
        public int Id;
        /**
         *数据SDI
         */
        public String SDI;
        /** 数据SSM */
        public String SSM;
        /** Data */
        public String Data;
        /** SDI显示颜色 */
        public int SDIColor;
        /** SSM显示颜色 */
        public int SSMColor;
        /**
         *数据颜色
         */
        public int DataColor;
        private boolean DataFrameEnd=false;

        public boolean isDataFrameEnd() {
            return DataFrameEnd;
        }

        public void setDataFrameEnd(boolean dataFrameEnd) {
            DataFrameEnd = dataFrameEnd;
        }

        /**
         * 预留
         */
        public short Reserve;

        @Override
        public  String toCsvHead() {
            return "BeginX,EndX,SDI,Data,SSM,DataColor";
        }

        @Override
        public String toCSV() {
            if (Data==null) return "";
            String b= TBookUtil.getTimeFromS( BeginX* HorizontalAxis.getInstance().getTimesPrePix());
            String e =TBookUtil.getTimeFromS( EndX*HorizontalAxis.getInstance().getTimesPrePix());
            b=b.replace("μ","u");
            e=e.replace("μ","u");
            return b+","+e+","+SDI+","+Data+","+SSM+",0x"+Integer.toHexString(DataColor);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Arinc429Struct{");
            sb.append("isBeginFrame=").append(isBeginFrame);
            sb.append(", BeginX=").append(BeginX);
            sb.append(", EndX=").append(EndX);
            sb.append(", DataType=").append(DataType);
            sb.append(", Id=").append(Id);
            sb.append(", SDI='").append(SDI).append('\'');
            sb.append(", SSM='").append(SSM).append('\'');
            sb.append(", Data='").append(Data).append('\'');
            sb.append(", SDIColor=").append(SDIColor);
            sb.append(", SSMColor=").append(SSMColor);
            sb.append(", DataColor=").append(DataColor);
            sb.append(", DataFrameEnd=").append(DataFrameEnd);
            sb.append(", Reserve=").append(Reserve);
            sb.append('}');
            return sb.toString();
        }
    }


    /***
     * 串口配置参数
     */
    public class UartSettingStruct{
        public int uartLength = 8;       //位的长度，由界面传过来的。
        //public long time = 40000000l;    //也是界面传进来的。 tem= 时间/50格
        public  boolean checked=false;     //也是界面传进来的。
        public  int encoding=ICharacterEncoding.Hex;  //也是界面传进来的
        public void setUartLength(int uartLength) {
            this.uartLength = uartLength;
        }

         public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Hex;break;
                case 1:this.encoding=ICharacterEncoding.Binary;break;
                case 2:this.encoding=ICharacterEncoding.ASCII;break;
            }

        }

        public int getUartLength() {
            return uartLength;
        }

        public boolean isChecked() {
            return checked;
        }

        public int getEncoding() {
            return encoding;
        }
    }

    public class LinSettingStruct{
        //long time = 100000000l;
        int encoding=ICharacterEncoding.Hex;  //也是界面传进来的

        //public void setTime(long time) {
        //    this.time = time;
       // }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Hex;break;
                case 1:this.encoding=ICharacterEncoding.Binary;break;
                case 2:this.encoding=ICharacterEncoding.ASCII;break;
            }
        }

//        public long getTime() {
//            return time;
//        }

        public int getEncoding() {
            return encoding;
        }
    }

    public class CanSettingStruct{
//        long time = 1000000l;  //时间转像素
        int encoding=ICharacterEncoding.Hex;  //也是界面传进来的

//        public void setTime(long time) {
//            this.time = time;
//        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Hex;break;
                case 1:this.encoding=ICharacterEncoding.Binary;break;
                case 2:this.encoding=ICharacterEncoding.ASCII;break;
            }
        }

//        public long getTime() {
//            return time;
//        }

        public int getEncoding() {
            return encoding;
        }
    }
    public class SpiSettingStruct{
        int encoding=ICharacterEncoding.Hex;  //也是界面传进来的
//        long time = 400000l;  //时间转像素
        int dataBit = 8;

        public int getEncoding() {
            return encoding;
        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Hex;break;
                case 1:this.encoding=ICharacterEncoding.Binary;break;
                case 2:this.encoding=ICharacterEncoding.ASCII;break;
            }
        }

//        public long getTime() {
//            return time;
//        }

//        public void setTime(long time) {
//            this.time = time;
//        }

        public int getDataBit() {
            return dataBit;
        }

        public void setDataBit(int dataBit) {
            switch (dataBit){
                case 0:this.dataBit = 4;  break;
                case 1:this.dataBit = 8; break;
                case 2:this.dataBit = 16; break;
                case 3:this.dataBit = 24; break;
                case 4:this.dataBit = 32; break;
            }

        }
    }
    public class I2cSettingStruct{
//        long time = 4000000l;  //时间转像素
        int encoding=ICharacterEncoding.Hex;  //也是界面传进来的

//        public long getTime() {
//            return time;
//        }
//
//        public void setTime(long time) {
//            this.time = time;
//        }

        public int getEncoding() {
            return encoding;
        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Hex;break;
                case 1:this.encoding=ICharacterEncoding.Binary;break;
                case 2:this.encoding=ICharacterEncoding.ASCII;break;
            }
        }
    }
    public class MilSID1553bSettingStruct{
//        long time = 2000000l;  //时间转像素
        int encoding=ICharacterEncoding.Binary;  //也是界面传进来的

//        public long getTime() {
//            return time;
//        }
//
//        public void setTime(long time) {
//            this.time = time;
//        }

        public int getEncoding() {
            return encoding;
        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Binary;break;
                case 1:this.encoding=ICharacterEncoding.Hex;break;
            }
        }
    }
    public class Arinc429SettingStruct{
//        long time = 100000000l;  //时间转像素
        int encoding=ICharacterEncoding.Hex;  //也是界面传进来的

//        public long getTime() {
//            return time;
//        }
//
//        public void setTime(long time) {
//            this.time = time;
//        }

        public int getEncoding() {
            return encoding;
        }

        public void setEncoding(int encoding) {
            switch (encoding){
                case 0:this.encoding=ICharacterEncoding.Binary;break;
                case 1:this.encoding=ICharacterEncoding.Hex;break;
            }
        }
    }

}
