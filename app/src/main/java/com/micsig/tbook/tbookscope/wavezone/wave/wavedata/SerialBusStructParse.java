package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwb on 2017/10/18.
 * 串型解码的解析，及绘图
 */

public class SerialBusStructParse {
    private static final String TAG = "SerialBusStructParse";
    private static final double Cycle=4e4;
    //region 单例
    public static class SerialBusStructParseHolder {
        public static final SerialBusStructParse instance = new SerialBusStructParse();
    }

    public static SerialBusStructParse getInstance() {
        return SerialBusStructParseHolder.instance;
    }
    //endregion

    //region 变量

    private long timeToPix;
    private int startX, endX;
    private int encoding;
    private int bits;
    private boolean checked;

    private Rect rectTitleLen = new Rect();
    private PorterDuffXfermode desoutMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    //endregion

    //region 解析数据列表
    private List<SerialBusStruct.UartStruct> listUart;
    private List<SerialBusStruct.LinStruct> listLin;
    private List<SerialBusStruct.CanStruct> listCan;
    private List<SerialBusStruct.SpiStruct> listSpi;
    private List<SerialBusStruct.I2cStruct> listI2c;
    private List<SerialBusStruct.Arinc429Struct> list429;
    private List<SerialBusStruct.MilSTD1553bStruct> list1553b;

    public List<SerialBusStruct.UartStruct> getListUart(){return listUart;}
    public List<SerialBusStruct.LinStruct>  getListLin(){return listLin;}
    public List<SerialBusStruct.CanStruct>  getListCan(){return listCan;}
    public List<SerialBusStruct.SpiStruct>  getListSpi(){return listSpi;}
    public List<SerialBusStruct.I2cStruct>  getListI2c(){return listI2c;}
    public List<SerialBusStruct.Arinc429Struct> getList429(){return list429;}
    public List<SerialBusStruct.MilSTD1553bStruct> getList1553b(){return list1553b;}
    //endregion


    public void toParse(int Ch, int titleLen, int serialBusType, Canvas canvas, Paint paint, final ByteBuffer bytes, long timeToPix, int encoding, int bits, boolean checked, int startX, int endX) {
        if(bytes.limit() == bytes.capacity()) return;//Worktile #2816 尝试规避ByteBuffer转化为串口数据时java.lang.IndexOutofBoundsException异常。
        this.timeToPix = timeToPix;
        this.encoding = encoding;
        this.bits = bits;
        this.checked = checked;
        this.startX = startX;
        this.endX = endX;
        switch (serialBusType) {
            case SerialBusStruct.SerialBusType_UART: {
//              Tools.beginTime();
                List<SerialBusStruct.UartStruct> list = getUartStruct(bytes);
                listUart=list;
//              Logger.i(TAG,String.format("解析耗时： %d ms",Tools.endTime()));
//              Tools.beginTime();
                if (encoding == ICharacterEncoding.ASCII) {
                    paint.setTypeface(Typeface.SERIF);
                }
                    drawParseUartData(titleLen, canvas, paint, list);
                paint.setTypeface(Typeface.MONOSPACE);
//               Logger.i(TAG,String.format("绘制耗时：%d ms",Tools.endTime()));

            }
            break;
            case SerialBusStruct.SerialBusType_LIN: {
                paint.setTypeface(Typeface.MONOSPACE);
                List<SerialBusStruct.LinStruct> list = getLinStruct(bytes);
                listLin=list;
                drawParseLinData(titleLen, canvas, paint, list);
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: {
//                bytes.clear();
//                byte[] bytess=new byte[]{0x00,0x00 ,0x00,0x00,(byte)0xD8,0x00,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,(byte)0x5D ,0x01,0x20 ,0x04 ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,(byte)0xF5 ,0x06 ,0x48 ,0x09 ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,0x79 ,0x06 ,0x08 ,0x0D ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,0x14 ,0x06 ,0x60 ,0x0F ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,0x14 ,0x00 ,(byte)0xC8 ,0x10 ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,0x01 ,0x02 ,(byte)0xA8 ,0x12 ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,(byte)0xC1 ,0x03 ,(byte)0xE0 ,0x16 ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,(byte)0xC5 ,0x04 ,(byte)0xA0 ,(byte)0x1A ,0x00 ,0x00 ,
//                                         0x00 ,0x00 ,0x64 ,0x04 ,(byte)0xE8 ,(byte)0x1D ,0x00 ,0x00};
//                bytes.put(bytess);
                paint.setTypeface(Typeface.MONOSPACE);
                List<SerialBusStruct.CanStruct> list = getCanStruct(bytes);
                listCan=list;
                drawParseCanData(titleLen, canvas, paint, list);
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: {
                paint.setTypeface(Typeface.MONOSPACE);
                List<SerialBusStruct.SpiStruct> list = getSpiStruct(bytes);
                listSpi=list;
                drawParseSpiData(titleLen, canvas, paint, list);
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: {
                paint.setTypeface(Typeface.MONOSPACE);
                List<SerialBusStruct.I2cStruct> list = getI2cStruct(bytes);
                listI2c=list;
                drawParseI2cData(titleLen, canvas, paint, list);
            }
            break;
            case SerialBusStruct.SerialBusType_429: {
                paint.setTypeface(Typeface.MONOSPACE);
//                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT
//                        + (Ch.equals("S1") ? CacheUtil.S1 : CacheUtil.S2));
                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + TChan.toSerialNumber(Ch));
                List<SerialBusStruct.Arinc429Struct> list = getArinc429Struct(bytes, format);
                list429=list;
                drawParseArinc429Data(titleLen, canvas, paint, list);
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: {
                paint.setTypeface(Typeface.MONOSPACE);
                List<SerialBusStruct.MilSTD1553bStruct> list = getMilSTD1553bStruct(bytes);
                list1553b=list;
                drawParseMilSTD1553bData(titleLen, canvas, paint, list);
            }
            break;
        }
    }


    //region UART绘制

    /**
     * 解析Uart数据结构
     *
     * @param bytes
     * @return
     */
    private List<SerialBusStruct.UartStruct> getUartStruct(ByteBuffer bytes) {
        final int NoStopColor = Color.YELLOW;
        final int StopSuccessColor = App.get().getResources().getColor(R.color.textColor);// Color.rgb(128,128,128); //Color.WHITE;
        final int StopFailedColor = Color.RED;

        int uartLength = bits; //8;       //位的长度，由界面传过来的。
        long tmp = this.timeToPix; //40000000l;    //也是界面传进来的。 tem= 时间/50格
        boolean checked = this.checked;//false;     //也是界面传进来的。
        int encoding = this.encoding; //ICharacterEncoding.Hex;  //也是界面传进来的
        int temData = 0;
        List<SerialBusStruct.UartStruct> list = new ArrayList<>();
        SerialBusStruct.UartStruct uartStruct = null;
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                uartStruct = SerialBusStruct.getInstance().new UartStruct();
                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                uartStruct.BeginX = uartStruct.EndX;
                uartStruct.Data = "?";
                uartStruct.isBeginFrame=true;
                list.add(uartStruct);
                continue;
            }
            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) {
//                if (uartStruct != null && uartStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    uartStruct.EndX = uartStruct.BeginX;
//                    uartStruct.Data = "?";
//                    list.add(uartStruct);
//                }
                uartStruct = SerialBusStruct.getInstance().new UartStruct();
                uartStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                uartStruct.BeginX = (int) (uartStruct.BeginX * Cycle / tmp);
                uartStruct.DataType = bytes.get(i + 3);
                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                uartStruct.DataColor = StopFailedColor;
                uartStruct.isBeginFrame=true;
                continue;
            }
            switch (bytes.get(i + 3) & 0x07) {
                case SerialBusStruct.UartStruct.DataType_CRCFailed_noStop:
                case SerialBusStruct.UartStruct.DataType_CRCSucc_NoStop: {
                    if (uartStruct == null) {
                        uartStruct = SerialBusStruct.getInstance().new UartStruct();
                        uartStruct.BeginX = 0;
                        uartStruct.isBeginFrame=false;
                    }
                    uartStruct.DataType=(byte)(bytes.get(i + 3) & 0x0F);

                    if (uartLength <= 8) {
//                        temData=bytes.get(i+2) & mask;
                        uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                        uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                        int len = checked ? uartLength : uartLength - 1;
                        uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, len);
                        uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                        uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor;
                        list.add(uartStruct);
                    } else {
                        //奇偶校验，不进行拼成9bit
                        if (checked != true) {
//                            temData=bytes.get(i+2) & mask;
                            uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                            uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                            uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                            uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, uartLength > 8 ? 8 : uartLength); //String.format("%02x", bytes.get(i + 2)).toUpperCase();
//                            uartStruct.Data=getEncoding(encoding,temData,uartLength>8?8:uartLength);
                            uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                            uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor;
                            ;
                            list.add(uartStruct);
                        } else {
                            if ((uartStruct.DataType & SerialBusStruct.UartStruct.DataType_3) == SerialBusStruct.UartStruct.DataType_3) {
                                //拼字节
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                                uartStruct.Id = 0x000000FF & bytes.get(i + 2);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor;
                                ;

                            } else {
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                                uartStruct.Id |= 0x00000100 & (bytes.get(i + 2) << 8);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor;
                                ;

                                uartStruct.Data = getEncoding(encoding, uartStruct.Id, uartLength); //String.format("%03x", uartStruct.Id).toUpperCase();
                                if (uartStruct.BeginX < 0) {
                                    uartStruct.Data = "?";
                                }
                                list.add(uartStruct);
                            }
                        }
                    }
                }
                break;
                case SerialBusStruct.UartStruct.DataType_CRCFailed_Stop:
                case SerialBusStruct.UartStruct.DataType_CRCSucc_Stop: {
                    //有停止位，不进行拼字节，停止位就是第9位。
                    if (uartStruct == null) {
                        uartStruct = SerialBusStruct.getInstance().new UartStruct();
                        uartStruct.BeginX = 0;
                        uartStruct.isBeginFrame=false;
                    }
                    uartStruct.DataType=(byte)(bytes.get(i + 3) & 0x07);

                    if (uartLength <= 8) {
                        uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                        uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                        int len = checked ? uartLength : uartLength - 1;
                        uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, len); //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                        uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                        if ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) {
                            uartStruct.DataColor = StopFailedColor;
                        } else {
                            uartStruct.DataColor = StopSuccessColor;
                        }
                        list.add(uartStruct);
                    } else {

                        //奇偶校验，不拼字节
                        if (checked != true) {
                            uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                            uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                            uartStruct.DataType =(byte)( bytes.get(i + 3) & 0x07);
                            uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, uartLength > 8 ? 8 : uartLength);
                            uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                            if ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) {
                                uartStruct.DataColor = StopFailedColor;
                            } else {
                                uartStruct.DataColor = StopSuccessColor;
                            }
                            list.add(uartStruct);

                        } else {
                            if ((uartStruct.DataType & SerialBusStruct.UartStruct.DataType_3) == SerialBusStruct.UartStruct.DataType_3) {
                                //拼字节
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                                uartStruct.Id = 0x000000FF & bytes.get(i + 2);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) ? StopFailedColor : StopSuccessColor;
                                //String.format("%03x", uartStruct.Id).toUpperCase();

                            } else {
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp);
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07);
                                uartStruct.Id |= 0x00000100 & (bytes.get(i + 2) << 8);
                                //uartStruct.Id = 0x000000FF & bytes.get(i + 2);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) ? StopFailedColor : StopSuccessColor;
                                ;
                                uartStruct.Data = getEncoding(encoding, uartStruct.Id, uartLength);
                                if (uartStruct.BeginX < 0) {
                                    uartStruct.Data = "?";
                                }
                                list.add(uartStruct);
                            }
                        }
                    }
                }
                break;

            }

        }

        //将最后一个添加上
        if (list.size() > 0 && uartStruct != null)
            if (uartStruct.hashCode() != list.get(list.size() - 1).hashCode()) {
                uartStruct.Data = "?";
//                uartStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                uartStruct.EndX = (this.endX - this.startX);
                list.add(uartStruct);
            }

        return list;
    }

    /**
     * 绘制解析数据
     *
     * @param canvas
     * @param list
     */
    private  void drawParseUartData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.UartStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
            if (i != 0 && list.get(i).BeginX == list.get(i - 1).BeginX) {
                continue;
            }
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }


    //endregion

    //region Lin绘制
    private List<SerialBusStruct.LinStruct> getLinStruct(ByteBuffer bytes) {
        long tmp = this.timeToPix; //100000000l;
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的
        int bitWidth = 2 * 4;

        List<SerialBusStruct.LinStruct> list = new ArrayList<>();
        SerialBusStruct.LinStruct linStruct = null;

        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                linStruct = SerialBusStruct.getInstance().new LinStruct();
                linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                linStruct.BeginX = linStruct.EndX;
                linStruct.Data = "?";
                linStruct.isBeginFrame=true;
                list.add(linStruct);
                continue;
            }
            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) {
//                if (linStruct != null && linStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    linStruct.EndX = linStruct.BeginX;
//                    linStruct.Data = "?";
//                    list.add(linStruct);
//                }
                linStruct = SerialBusStruct.getInstance().new LinStruct();
                linStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                linStruct.BeginX = (int) (linStruct.BeginX * Cycle / tmp);
                linStruct.DataType = bytes.get(i + 3);
                linStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                linStruct.DataColor = Color.RED;
                linStruct.isBeginFrame=true;
                continue;
            }
            switch (bytes.get(i + 3) & 0x0F) {

                case SerialBusStruct.LinStruct.ID_ODD_Sucess:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth);  //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.YELLOW;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.ID_ODD_Failed:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Data_Stop:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor =App.get().getResources().getColor(R.color.textColor); //Color.WHITE;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Data_NoStop:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) &0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Check_Success:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.GREEN; //Color.rgb(250, 128, 10);
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Check_Failed:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.SYNC_Success:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }

                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = "SYNC";
                    linStruct.DataColor = Color.YELLOW;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.SYNC_Failed:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = "SYNC";
                    linStruct.DataColor = Color.RED;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Wake_Success:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = "WAKE";
                    linStruct.DataColor = Color.BLUE;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.Wake_Failed:
                    if (linStruct == null) {
                        linStruct = SerialBusStruct.getInstance().new LinStruct();
                        linStruct.BeginX = 0;
                        linStruct.isBeginFrame=false;
                    }
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp);
                    linStruct.DataType = bytes.get(i + 3) & 0x0F;
                    linStruct.Data = "WUP";
                    linStruct.DataColor = Color.RED;
                    list.add(linStruct);
                    break;
                case SerialBusStruct.LinStruct.SYNC_Distance:
//                    Logger.i(TAG,"同步间隔上升沿 bytes:"+SerialBusTxtStructParse.getDebugBytesToString(i,bytes));
                    if (linStruct!=null && linStruct.DataType==SerialBusStruct.DataType_BeginData){
                        linStruct.Data="SYNC_Distance";
                    }
                    break;

            }
        }

        //将最后一个添加上
        if (list.size() > 0 && linStruct != null)
            if (linStruct.hashCode() != list.get(list.size() - 1).hashCode() && !"SYNC_Distance".equals(linStruct.Data)) {
                linStruct.Data = "?";
                linStruct.EndX = this.endX - this.startX;
                list.add(linStruct);
//                Logger.i(TAG,"type:"+linStruct.DataType+"  data:"+linStruct.Data);
            }

        return list;
    }

    /**
     * 画lin图
     *
     * @param canvas
     * @param paint
     * @param list
     */
    private void drawParseLinData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.LinStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
//            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }

    //endregion

    //region Can绘制
    private List<SerialBusStruct.CanStruct> getCanStruct(ByteBuffer bytes) {
        final int IDcolor = Color.YELLOW;
        final int DLCcolor = Color.GREEN;
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE;
        final int CrcColor = Color.GREEN;
        final int ErrorColor = Color.RED;
        final int OverLoadColor = Color.BLUE;
        final int ProblemColor = Color.RED;

        long tmp = this.timeToPix; //1000000l;  //时间转像素
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的
        int bitWidth = 8;

        List<SerialBusStruct.CanStruct> list = new ArrayList<>();
        int idShowUpTimes = 0;  //id出现的次数
        SerialBusStruct.CanStruct canStruct = null;
//        Logger.i(TAG,"timeToPix:"+timeToPix);
//Logger.i(TAG,"bytes:"+SerialBusTxtStructParse.getDebugBytesToString(bytes));
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                canStruct = SerialBusStruct.getInstance().new CanStruct();
                canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                canStruct.BeginX = canStruct.EndX;
                canStruct.Data = "?";
                canStruct.isBeginFrame=true;
                list.add(canStruct);
                continue;
            }
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){
//                if (canStruct!=null && canStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    canStruct.EndX=canStruct.BeginX;
//                    canStruct.Data="?";
//                    list.add(canStruct);
//                }
                canStruct = SerialBusStruct.getInstance().new CanStruct();
                canStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                canStruct.BeginX = (int) (canStruct.BeginX * Cycle / tmp);
                canStruct.DataType = bytes.get(i + 3) ;
                canStruct.DataColor = ProblemColor;
                canStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0));
                canStruct.isBeginFrame=true;
                idShowUpTimes = 0;
                continue;
            }
            switch (bytes.get(i + 3) & 0x0F) {
                case SerialBusStruct.CanStruct.StandardID: {
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.Data = "?";
                        canStruct.DataColor = IDcolor;
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        canStruct.ID = 0x000000FF & bytes.get(i + 2);
                        canStruct.isBeginFrame=false;
                        idShowUpTimes = 1;
                        continue;
                    }
                    //标准ID最多出现2次
                    if ((canStruct.DataType & 0x0F) == SerialBusStruct.DataType_BeginData) {
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        canStruct.ID = 0x000000FF & bytes.get(i + 2);
                        idShowUpTimes++;
                    } else {
                        canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        if (!canStruct.Data.equals("?") && canStruct.isBeginFrame)
                            canStruct.Data = getEncoding(encoding, canStruct.ID << 3 | (0x07 & bytes.get(i + 2)), 3 * 4);
                        canStruct.DataColor = IDcolor;
                        idShowUpTimes = 0;
                        list.add(canStruct);
//                        debugCan(bytes,i,"StandardID",canStruct,list);
                    }
                }
                break;
                case SerialBusStruct.CanStruct.ExtendId: {
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.Data = "?";
                        canStruct.DataColor = IDcolor;
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        canStruct.ID = 0x000000FF & bytes.get(i + 2);
                        canStruct.isBeginFrame=false;
                        idShowUpTimes = 1;
                        continue;
                    }
                    switch (idShowUpTimes) {
                        case 0:
                            //这个进不来，因为第一帧是标准帧
                            break;
                        case 1:
                            canStruct.ID = (canStruct.ID << 8 | (bytes.get(i + 2) & 0x000000FF));
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                            canStruct.DataType = bytes.get(i + 3) & 0x0F;
                            canStruct.DataColor = IDcolor;
                            idShowUpTimes++;
                            break;
                        case 2:
                            canStruct.ID = (canStruct.ID << 8 | (bytes.get(i + 2)& 0x000000FF));
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                            canStruct.DataType = bytes.get(i + 3) & 0x0F;
                            canStruct.DataColor = IDcolor;
                            idShowUpTimes++;
                            break;
                        case 3:
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                            if (!canStruct.Data.equals("?") && canStruct.isBeginFrame)
                                canStruct.Data = getEncoding(encoding, (((canStruct.ID << 5) & 0xFFFFFFE0) | (bytes.get(i + 2) & 0x1F)), 8 * 4);
                            canStruct.DataType = bytes.get(i + 3) & 0x0F;
                            canStruct.DataColor = IDcolor;
                            idShowUpTimes = 0;
                            list.add(canStruct);
//                            debugCan(bytes,i,"ExtendId",canStruct,list);
                            break;
                    }
                }
                break;
                case SerialBusStruct.CanStruct.DLC: {
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.Data="?";
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
//                    if (!canStruct.Data.equals("?") && canStruct.isBeginFrame)
                    int dlc = bytes.get(i + 2) & 0xF;
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
//                    canStruct.Data = getEncoding(encoding, dlc, 2 * 4);
                    canStruct.Data = "" + dlc;
                    canStruct.DataType = bytes.get(i + 3) & 0x0F;
                    canStruct.DataColor = DLCcolor;
                    list.add(canStruct);
//                    debugCan(bytes,i,"DLC",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.DATA: {
                    canStruct = SerialBusStruct.getInstance().new CanStruct();
                    if (list.size() > 0) {
                        canStruct.BeginX = list.get(list.size() - 1).EndX;
                    } else {
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                    canStruct.DataType = bytes.get(i + 3) & 0x0F;
                    canStruct.DataColor = DataColor;
                    canStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);  //Integer.toHexString(bytes.get(i + 2));
                    list.add(canStruct);
//                    debugCan(bytes,i,"Data",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.CRC: {
                    if (canStruct == null || list.size() == 0) {
                        //第一次接收数据为CRC
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        canStruct.ID = 0xFF & bytes.get(i + 2);
                        canStruct.DataColor = ErrorColor;
                        canStruct.isBeginFrame=false;
                    } else if (list.size() > 0) {
                        //正常的数据接收。
                        if ((canStruct.DataType & 0x0F) == SerialBusStruct.CanStruct.CRC) {
                            canStruct.DataType = bytes.get(i + 3) & 0x0F;
                            canStruct.DataColor = CrcColor;
                            canStruct.ID = canStruct.ID << 8 | (0xFF & bytes.get(i + 2));
                        } else {
                            canStruct = SerialBusStruct.getInstance().new CanStruct();
                            canStruct.BeginX = list.get(list.size() - 1).EndX;
                            canStruct.DataType = bytes.get(i + 3) & 0x0F;
                            canStruct.ID = 0xFF & bytes.get(i + 2);
                            canStruct.DataColor = CrcColor;
                        }
                    } else {
                        //第一次接收是CRC的，第二次还是CRC的接收进行拼字节
                        canStruct.DataType = bytes.get(i + 3) & 0x0F;
                        canStruct.DataColor = CrcColor;
                        canStruct.ID = canStruct.ID << 8 | (0xFF & bytes.get(i + 2));
                    }


                }
                break;
                case SerialBusStruct.CanStruct.ConfirmOver: {
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);

                    canStruct.DataColor = CrcColor;
                    if((canStruct.ID & 0x04) != 0){
                        canStruct.DataColor = ErrorColor;
                    }

                    int tmw = 16;
                    switch (canStruct.ID & 0x03){
                        case 0:
                            canStruct.ID >>= 9;
                            tmw = 16;
                            break;
                        case 1:
                            canStruct.ID >>= 7;
                            tmw = 20;
                            break;
                        case 2:
                            tmw = 24;
                            canStruct.ID >>= 3;
                            break;
                    }
                    canStruct.Data = getEncoding(encoding, canStruct.ID, tmw) + "[A]"; //Integer.toHexString(canStruct.ID << 7 | (0x7F & bytes.get(i + 2))).toUpperCase() + "[~A]";
                    //canStruct.Data="CRC[~A]";

                    list.add(canStruct);
//                    debugCan(bytes,i,"ConfirmOver",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.NoConfirmOver: {
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                    canStruct.DataColor = CrcColor;
                    if((canStruct.ID & 0x04) != 0){
                        canStruct.DataColor = ErrorColor;
                    }
                    int tmw = 16;
                    switch (canStruct.ID & 0x03){
                        case 0:
                            canStruct.ID >>= 9;
                            tmw = 16;
                            break;
                        case 1:
                            canStruct.ID >>= 7;
                            tmw = 20;
                            break;
                        case 2:
                            tmw = 24;
                            canStruct.ID >>= 3;
                            break;
                    }
                    canStruct.Data = getEncoding(encoding, canStruct.ID, tmw) + "[~A]"; //Integer.toHexString(canStruct.ID << 7 | (0x7F & bytes.get(i + 2))).toUpperCase() + "[A]";
                    //canStruct.Data="CRC";
//                    canStruct.DataColor = CrcColor;
                    list.add(canStruct);
//                    debugCan(bytes,i,"NoConfirmOver",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.Error: {
                    if (i - 8 + 3 < 0) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        if (list.size()<=0) canStruct.isBeginFrame=false;
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) == SerialBusStruct.DataType_BeginData) {
                        //上一个是开始帧，只进行赋值就可以了。
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        if (list.size() > 0) {
                            canStruct.BeginX = list.get(list.size() - 1).EndX;
                        } else {canStruct.BeginX = 0;canStruct.isBeginFrame=false; }
                    }
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                    canStruct.DataColor = ErrorColor;
                    canStruct.DataType = bytes.get(i + 3) & 0x0F;
                    canStruct.Data = "Err";
                    list.add(canStruct);
                    idShowUpTimes = 0;
//                    debugCan(bytes,i,"Error",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.OverLoad: {
                    if (i - 8 + 3 < 0) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        if (list.size()<=0) canStruct.isBeginFrame=false;
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) == SerialBusStruct.DataType_BeginData) {
                        //上一个是开始帧，只进行赋值就可以了。
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        if (list.size() > 0) {
                            canStruct.BeginX = list.get(list.size() - 1).EndX;
//                            if (list.get(list.size()-1).DataType==SerialBusStruct.CanStruct.CRC || list.get(list.size()-1).DataType==0){
//                                canStruct.BeginX=list.get(list.size()-1).BeginX;
//                            }
                        } else{ canStruct.BeginX = 0; canStruct.isBeginFrame=false;}
                    }
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                    canStruct.DataColor = OverLoadColor;
                    canStruct.DataType = bytes.get(i + 3) & 0x0F;
                    canStruct.Data = "OverLoad";
                    list.add(canStruct);
                    idShowUpTimes = 0;
//                    debugCan(bytes,i,"OverLoad",canStruct,list);
                }
                break;
                case SerialBusStruct.CanStruct.Trouble: {
                    if (i - 8 + 3 < 0) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        if (list.size()<=0) canStruct.isBeginFrame=false;
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) != SerialBusStruct.DataType_BeginData) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        if (list.size() > 0) {
                            canStruct.BeginX = list.get(list.size() - 1).EndX;
//                            if (list.get(list.size()-1).DataType==SerialBusStruct.CanStruct.CRC || list.get(list.size()-1).DataType==0){
//                                canStruct.BeginX=list.get(list.size()-1).BeginX;
//                            }
                        } else{ canStruct.BeginX = 0; canStruct.isBeginFrame=false;}
                    }
                    if (canStruct == null) {
                        canStruct = SerialBusStruct.getInstance().new CanStruct();
                        canStruct.BeginX = 0;
                        canStruct.isBeginFrame=false;
                    }

                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp);
                    canStruct.DataColor = ErrorColor;
                    canStruct.DataType = bytes.get(i + 3) & 0x0F;
                    canStruct.Data = "Err";
                    list.add(canStruct);
                    idShowUpTimes = 0;
//                    debugCan(bytes,i,"Trouble",canStruct,list);
                }
                break;
            }


        }

        //将最后一个添加上
        if (list.size() > 0 && canStruct != null)
            if (canStruct.hashCode() != list.get(list.size() - 1).hashCode()) {
                canStruct.Data = "?";
//                canStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                canStruct.EndX = this.endX - this.startX;
                list.add(canStruct);
            }
        return list;
    }

    private void debugCan(ByteBuffer bytes, int index, String tag, SerialBusStruct.CanStruct canStruct, List<SerialBusStruct.CanStruct> list) {
        if (canStruct.EndX - canStruct.BeginX > 10) {
            Logger.i(TAG, tag + "  index:" + index + "  canStruct:" + canStruct.toString());
            Logger.e(TAG, "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(index, bytes));
        }
    }
    private void debugSpi(ByteBuffer bytes,int index,String tag,SerialBusStruct.SpiStruct spiStruct){
        if (spiStruct.EndX - spiStruct.BeginX > 10) {
            Logger.i(TAG, tag + "  index:" + index + "  spiStruct:" + spiStruct.toString());
            Logger.e(TAG, "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(index, bytes));
        }
    }

    private void drawParseCanData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.CanStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }

    // endregion

    //region SPI 绘制
    private List<SerialBusStruct.SpiStruct> getSpiStruct(ByteBuffer bytes) {
        final int CodeColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE;
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的


        long tmp = this.timeToPix;//400000l;  //时间转像素
        int dataBit = this.bits; //8;
        int dataBit_showTimes = 1;  //字节数据显示的次数。
        List<SerialBusStruct.SpiStruct> list = new ArrayList<>();
        SerialBusStruct.SpiStruct spiStruct = null;
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                spiStruct = SerialBusStruct.getInstance().new SpiStruct();
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp);
                spiStruct.BeginX = spiStruct.EndX;
                spiStruct.Data = "?";
                spiStruct.isBeginFrame=true;
                list.add(spiStruct);
//                debugSpi(bytes,i,TAG,spiStruct);
                continue;
            }

            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){
//                if (spiStruct!=null && spiStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    spiStruct.EndX=spiStruct.BeginX;
//                    spiStruct.Data="?";
//                    list.add(spiStruct);
//                }
                spiStruct = SerialBusStruct.getInstance().new SpiStruct();
                spiStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                spiStruct.BeginX = (int) (spiStruct.BeginX * Cycle / tmp);
                spiStruct.Data = "";
                spiStruct.DataColor = CodeColor;
                spiStruct.DataType=SerialBusStruct.DataType_BeginData;
                spiStruct.isBeginFrame=true;
                continue;
            }

            if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_3bit_Stop) == SerialBusStruct.SpiStruct.DataType_3bit_Stop) {
                if (spiStruct == null) {
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct();
                    spiStruct.Data = "";
                    spiStruct.BeginX = 0;
                    spiStruct.isBeginFrame=false;
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp);
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop;
                spiStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit);  //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                spiStruct.DataColor = CodeColor;
                list.add(spiStruct);
//                debugSpi(bytes,i,TAG,spiStruct);
            } else if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_2bit_LastBusData) == SerialBusStruct.SpiStruct.DataType_2bit_LastBusData) {
                if (spiStruct == null) {
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct();
                    spiStruct.Data = "";
                    spiStruct.BeginX = 0;
                    spiStruct.isBeginFrame=false;
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp);
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop;
                if (dataBit == 4)
                    spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit);
                else spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 8);
                spiStruct.DataColor = CodeColor;
                list.add(spiStruct);
//                debugSpi(bytes,i,TAG,spiStruct);
                dataBit_showTimes = 1;
//                Logger.i(TAG,"11合成包："+spiStruct.toString());
            } else if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_2bit_BusData) == SerialBusStruct.SpiStruct.DataType_2bit_BusData) {
                if (spiStruct == null) {
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct();
                    spiStruct.Data = "";
                    spiStruct.BeginX = 0;
                    spiStruct.isBeginFrame=false;
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp);
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop;
                if (dataBit == 4)
                    spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit);
                else spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 8);
                spiStruct.DataColor = CodeColor;
//                Logger.i(TAG,"dataBit:"+dataBit+"    01数据包:"+spiStruct.toString());
            }

        }

        //将最后一个添加上
        if (list.size() > 0 && spiStruct != null)
            if (spiStruct.hashCode() != list.get(list.size() - 1).hashCode()) {
                spiStruct.Data = "?";
//                spiStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                spiStruct.EndX = this.endX - this.startX;
                list.add(spiStruct);
            }
        return list;
    }

    private void drawParseSpiData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.SpiStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);

    }
    //endregion

    //region I2C 绘制
    private List<SerialBusStruct.I2cStruct> getI2cStruct(ByteBuffer bytes) {
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE;
        final int WriteColor = Color.YELLOW;
        final int ReadColor = Color.GREEN;

        long tmp = timeToPix;// 4000000l;  //时间转像素
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的
//        Logger.i("TAG", "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(bytes));
        List<SerialBusStruct.I2cStruct> list = new ArrayList<>();
        SerialBusStruct.I2cStruct i2cStruct = null;
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                i2cStruct = SerialBusStruct.getInstance().new I2cStruct();
                i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp);
                i2cStruct.BeginX = i2cStruct.EndX;
                i2cStruct.Data = "?";
                i2cStruct.isBeginFrame=true;
                list.add(i2cStruct);
//                Logger.i("TAG", "过采样处理");
                continue;
            }

            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) {
//                if (i2cStruct != null && i2cStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    i2cStruct.EndX = i2cStruct.BeginX;
//                    i2cStruct.Data = "?";
//                    list.add(i2cStruct);
//                }
                i2cStruct = SerialBusStruct.getInstance().new I2cStruct();
                i2cStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                i2cStruct.BeginX = (int) (i2cStruct.BeginX * Cycle / tmp);
                i2cStruct.DataColor = Color.RED;
                i2cStruct.DataType = SerialBusStruct.DataType_BeginData;
                i2cStruct.isBeginFrame=true;
                continue;
            }
            if ((bytes.get(i+3) & 0x08)==SerialBusStruct.I2cStruct.DataType_3bit_Stop){
                i2cStruct=null;
                continue;
            }
            switch (bytes.get(i + 3) & 0x03) {
                case SerialBusStruct.I2cStruct.DataType_1bit_Write: {
                    if (i2cStruct == null) {
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct();
                        i2cStruct.BeginX = 0;
                        i2cStruct.isBeginFrame=false;
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp);
                    i2cStruct.Data = "W:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // String.format("%02x", bytes.get(i + 2)).toUpperCase();
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) {
                        i2cStruct.Data += "[A]";
                    } else
                        i2cStruct.Data += "[~A]";
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03;
                    i2cStruct.DataColor = WriteColor;
                    list.add(i2cStruct);
                }
                break;
                case SerialBusStruct.I2cStruct.DataType_1bit_Read: {
                    if (i2cStruct == null) {
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct();
                        i2cStruct.BeginX = 0;
                        i2cStruct.isBeginFrame=false;
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp);
                    i2cStruct.Data = "R:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) {
                        i2cStruct.Data += "[A]";
                    } else
                        i2cStruct.Data += "[~A]";
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03;
                    i2cStruct.DataColor = ReadColor;
                    list.add(i2cStruct);
                }
                break;
                case SerialBusStruct.I2cStruct.DataType_1bit_Data: {
                    if (i2cStruct == null) {
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct();
                        i2cStruct.BeginX = 0;
                        i2cStruct.isBeginFrame=false;
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp);
                    i2cStruct.Data = "D:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) {
                        i2cStruct.Data += "[A]";
                    } else
                        i2cStruct.Data += "[~A]";
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03;
                    i2cStruct.DataColor = DataColor;
                    list.add(i2cStruct);
                }
                break;
            }
        }

        //将最后一个添加上
        if (list.size() > 0 && i2cStruct != null)
            if (i2cStruct.hashCode() != list.get(list.size() - 1).hashCode()) {
                i2cStruct.Data = "?";
                i2cStruct.EndX = this.endX - this.startX;
//                Logger.i(TAG," i2c:"+i2cStruct.toString()+"  last i2c:"+list.get(list.size()-1).toString());
                list.add(i2cStruct);
            }
        return list;
    }

    private void drawParseI2cData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.I2cStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).ShortData, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }
    //endregion


    //region 1553B 绘制
    private List<SerialBusStruct.MilSTD1553bStruct> getMilSTD1553bStruct(ByteBuffer bytes) {
        final int RemoteColor = Color.BLUE;
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE;
        final int CommandColor = Color.YELLOW;
        final int ErrorColor = Color.RED;

        long tmp = this.timeToPix;// 2000000l;  //时间转像素
        int encoding = this.encoding;//ICharacterEncoding.Binary;  //也是界面传进来的

        List<SerialBusStruct.MilSTD1553bStruct> list = new ArrayList<>();
        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct = null;
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                milSTD1553bStruct.BeginX = milSTD1553bStruct.EndX;
                milSTD1553bStruct.Data = "?";
                milSTD1553bStruct.isBeginFrame=true;
                list.add(milSTD1553bStruct);
                continue;
            }
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){
//                if (milSTD1553bStruct!=null && milSTD1553bStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    milSTD1553bStruct.EndX=milSTD1553bStruct.BeginX;
//                    milSTD1553bStruct.Data="";
//                    list.add(milSTD1553bStruct);
//                }
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                    milSTD1553bStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    milSTD1553bStruct.BeginX = (int) (milSTD1553bStruct.BeginX * Cycle / tmp);
                    milSTD1553bStruct.DataColor = ErrorColor;
                    milSTD1553bStruct.DataType=bytes.get(i+3);
                    milSTD1553bStruct.isBeginFrame=true;
                continue;
            }
            switch (bytes.get(i + 3) & 0x0F) {

                case SerialBusStruct.MilSTD1553bStruct.DataType_RemoteAddr: {
                    if (milSTD1553bStruct == null) {
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                        milSTD1553bStruct.BeginX = 0;
                        milSTD1553bStruct.isBeginFrame=false;
                    }
                    milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                    milSTD1553bStruct.DataType = bytes.get(i + 3) &0x0F;
                    if (encoding == ICharacterEncoding.Binary) {
                        milSTD1553bStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 5);
                    } else {
                        milSTD1553bStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);
                    }
                    //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                    milSTD1553bStruct.DataColor = RemoteColor;
                    list.add(milSTD1553bStruct);

                    //添加< 为指令6，5做准备,结束为开始
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                    milSTD1553bStruct.BeginX = list.get(list.size() - 1).EndX;
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Command6bit: {
                    //9-14位 为高6位
                    if (milSTD1553bStruct == null) {
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                        milSTD1553bStruct.BeginX = 0;
                        milSTD1553bStruct.isBeginFrame=false;
                    }
                    milSTD1553bStruct.Id = 0x0000003F & ((bytes.get(i + 2) & 0x3F));
                    milSTD1553bStruct.DataColor = CommandColor;
                    milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F;

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Command5bit: {
                    //15-19位 为低5位
                    if (milSTD1553bStruct == null) {
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                        milSTD1553bStruct.BeginX = 0;
                        milSTD1553bStruct.Data = "?";
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                        milSTD1553bStruct.DataColor = CommandColor;
                        milSTD1553bStruct.isBeginFrame=false;
                        list.add(milSTD1553bStruct);
                    } else {
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                        int id = (bytes.get(i + 1) & 0xC0) << 3 | ((bytes.get(i + 2) & 0x07));
                        milSTD1553bStruct.Id = (0xFFFFFFE0 & milSTD1553bStruct.Id << 5) | id;
                        if (encoding == ICharacterEncoding.Binary) {
                            milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 11);
                        } else {
                            milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 3 * 4); //String.format("%03x", milSTD1553bStruct.Id).toUpperCase();
                        }
                        milSTD1553bStruct.DataColor = CommandColor;
                        milSTD1553bStruct.DataType = bytes.get(i + 3) &0x0F;
                        list.add(milSTD1553bStruct);
                    }

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Data1: {
                    if (milSTD1553bStruct == null) {
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                        milSTD1553bStruct.BeginX = 0;
                        milSTD1553bStruct.isBeginFrame=false;
                    }
                    milSTD1553bStruct.Id = 0xFFFFFF00 & ((bytes.get(i + 2)) << 8);
                    milSTD1553bStruct.DataColor = DataColor;
                    milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F;

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Data2: {
                    if (milSTD1553bStruct == null) {
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                        milSTD1553bStruct.BeginX = 0;
                        milSTD1553bStruct.Data = "?";
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                        milSTD1553bStruct.DataColor = DataColor;
                        milSTD1553bStruct.isBeginFrame=false;
                        list.add(milSTD1553bStruct);
                    } else {
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);

                        milSTD1553bStruct.Id |= (bytes.get(i + 2) & 0xFF);
                        milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 4 * 4); //String.format("%04x", milSTD1553bStruct.Id).toUpperCase();
                        milSTD1553bStruct.DataColor = DataColor;
                        milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F;
                        list.add(milSTD1553bStruct);
                    }

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_OverSuccess: {
                    if (list.size() > 0) {
                        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct1 = list.get(list.size() - 1);
                        milSTD1553bStruct1.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct1.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                        list.set(list.size() - 1, milSTD1553bStruct1);
                    }
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_OverFailed: {
                    if (list.size() > 0) {
                        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct1 = list.get(list.size() - 1);
                        milSTD1553bStruct1.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        milSTD1553bStruct1.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                        milSTD1553bStruct1.DataColor = ErrorColor;
                        list.set(list.size() - 1, milSTD1553bStruct1);
                    }
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_ManchesterCodeFailed: {
//                    if (milSTD1553bStruct == null) {
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct();
                    milSTD1553bStruct.BeginX = 0;
//                    }
                    if (list.size() > 0) {
                        milSTD1553bStruct.BeginX = list.get(list.size() - 1).EndX;
                    }else {
                        milSTD1553bStruct.isBeginFrame=false;
                    }
                    milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp);
                    milSTD1553bStruct.DataColor = ErrorColor;
                    milSTD1553bStruct.Data = "MANCH";
                    list.add(milSTD1553bStruct);
                }
                break;
            }
        }

        //将最后一个添加上
        if (list.size() > 0 && milSTD1553bStruct != null)
            if (milSTD1553bStruct.hashCode() != list.get(list.size() - 1).hashCode()) {
                milSTD1553bStruct.Data = "?";
//                milSTD1553bStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                milSTD1553bStruct.EndX = this.endX - this.startX;
                list.add(milSTD1553bStruct);
            }
        return list;
    }

    private void drawParseMilSTD1553bData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.MilSTD1553bStruct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
        for (int i = 0; i < list.size(); i++) {
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame);
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }
    //endregion

    //region Arinc429 绘制
    private List<SerialBusStruct.Arinc429Struct> getArinc429Struct(ByteBuffer bytes, int Arinc429Type) {
        final int LabelColor = Color.YELLOW;
        final int SDIColor = Color.BLUE;
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE;
        final int ErrorColor = Color.RED;
        final int SSMColor = Color.GREEN;

        long tmp = this.timeToPix;// 100000000l;  //时间转像素
        int encoding = this.encoding;//ICharacterEncoding.Hex;  //也是界面传进来的


        List<SerialBusStruct.Arinc429Struct> list = new ArrayList<>();
        SerialBusStruct.Arinc429Struct arinc429Struct = null;
        for (int i = 0; i <= bytes.limit() - 8; i += 8) {
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) {
                arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp);
                arinc429Struct.BeginX = arinc429Struct.EndX;
                arinc429Struct.Data = "?";
                arinc429Struct.isBeginFrame=true;
                list.add(arinc429Struct);
                continue;
            }
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){
//                if (arinc429Struct!=null && arinc429Struct.DataType==SerialBusStruct.DataType_BeginData){
//                    arinc429Struct.EndX=arinc429Struct.BeginX;
//                    arinc429Struct.Data="?";
//                    list.add(arinc429Struct);
//                }
                arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                arinc429Struct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                arinc429Struct.BeginX = (int) (arinc429Struct.BeginX * Cycle / tmp);
                arinc429Struct.DataColor = ErrorColor;
                arinc429Struct.DataType=bytes.get(i+3);
                arinc429Struct.isBeginFrame=true;
                continue;
            }

            switch (bytes.get(i + 3) & 0x0F) {
//                case SerialBusStruct.DataType_BeginData: {
//                    arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
//                    arinc429Struct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
//                    arinc429Struct.BeginX = (int) (arinc429Struct.BeginX * Cycle / tmp);
//                    arinc429Struct.DataColor = ErrorColor;
//                }
//                break;
                case SerialBusStruct.Arinc429Struct.DataType_Label: {
                    if (arinc429Struct == null) {
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                        arinc429Struct.BeginX = 0;
                        arinc429Struct.isBeginFrame=false;
                    }
                    arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp);
                    arinc429Struct.DataColor = LabelColor;
                    arinc429Struct.Data = getEncoding(ICharacterEncoding.Octal, bytes.get(i + 2) & 0x00FF, 3 * 4); //String.format("%o", bytes.get(i + 2));
                    arinc429Struct.DataType = bytes.get(i + 3) &0x0F;
                    list.add(arinc429Struct);

                    //为Data添加对像
                    arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                    arinc429Struct.BeginX = list.get(list.size() - 1).EndX;
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data1: {
                    if (arinc429Struct == null) {
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                        arinc429Struct.BeginX = 0;
                        arinc429Struct.isBeginFrame=false;
                    }
                    arinc429Struct.Id = (0x000000FF & bytes.get(i + 2));
                    arinc429Struct.DataColor = ErrorColor;
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data2: {
                    if (arinc429Struct == null) {
                        continue;
                    }
                    arinc429Struct.Id = arinc429Struct.Id | ((0x000000FF & bytes.get(i + 2)) << 8);
                    arinc429Struct.DataColor = ErrorColor;
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data3: {
                    if (arinc429Struct == null) {
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                        arinc429Struct.BeginX = 0;
                        arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp);
                        arinc429Struct.Data = "?";
                        arinc429Struct.DataColor = DataColor;
                        arinc429Struct.setDataFrameEnd(true);
                        arinc429Struct.isBeginFrame=false;
                        list.add(arinc429Struct);
                    } else {
                        arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                        arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp);
                        arinc429Struct.Id = arinc429Struct.Id | ((0x000000FF & bytes.get(i + 2)) << 16);
                        arinc429Struct.DataColor = DataColor;
                        arinc429Struct.setDataFrameEnd(true);
                        list.add(arinc429Struct);
                        //然后分析数据，赋值
                        //顺序 arinc429Struct.Id=data3+data2+data1
                        switch (Arinc429Type) {
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_1: {
                                String s = Integer.toBinaryString((arinc429Struct.Id & 0x03) | 0xFF0000);
                                arinc429Struct.SDI = s.substring(s.length() - 2, s.length()) + " ";
                                s = Integer.toBinaryString((arinc429Struct.Id & 0x600000) >> 21 | 0xFF0000);
                                arinc429Struct.SSM = s.substring(s.length() - 2, s.length()) + " ";
                                int tem = (arinc429Struct.Id & 0xFC) >> 2;
                                tem |= ((arinc429Struct.Id & 0xFF00) >> 2);
                                tem |= (arinc429Struct.Id & 0x1F0000) >> 2;
                                if (encoding == ICharacterEncoding.Binary) {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 19);
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 5 * 4); //String.format("%06x", tem).toUpperCase() + " ";
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) {
                                    arinc429Struct.SDIColor = ErrorColor;
                                    arinc429Struct.SSMColor = ErrorColor;
                                    arinc429Struct.DataColor = ErrorColor;
                                } else {
                                    arinc429Struct.SDIColor = SDIColor;
                                    arinc429Struct.SSMColor = SSMColor;
                                    arinc429Struct.DataColor = DataColor;
                                }
                            }
                            break;
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_2: {
                                //data3+data2+data1
                                String s = Integer.toBinaryString(((arinc429Struct.Id & 0x600000) >> 21) | 0xFF000000);
                                arinc429Struct.SSM = s.substring(s.length() - 2, s.length()) + " ";
                                int tem = arinc429Struct.Id & 0x1FFFFF;
                                if (encoding == ICharacterEncoding.Binary) {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 21);
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 6 * 4); //String.format("%06x", tem).toUpperCase() + " ";
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) {
                                    arinc429Struct.SDIColor = ErrorColor;
                                    arinc429Struct.SSMColor = ErrorColor;
                                    arinc429Struct.DataColor = ErrorColor;
                                } else {
                                    arinc429Struct.SDIColor = SDIColor;
                                    arinc429Struct.SSMColor = SSMColor;
                                    arinc429Struct.DataColor = DataColor;
                                }
                            }
                            break;
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_3: {
                                //data3+data2+data1
                                int temId = (arinc429Struct.Id & 0x7FFFFF);
                                if (encoding == ICharacterEncoding.Binary) {
                                    arinc429Struct.Data = getEncoding(encoding, temId, 23);
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, temId, 6 * 4); //String.format("%06x", temId).toUpperCase();
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) {
                                    arinc429Struct.SDIColor = ErrorColor;
                                    arinc429Struct.SSMColor = ErrorColor;
                                    arinc429Struct.DataColor = ErrorColor;
                                } else {
                                    arinc429Struct.SDIColor = SDIColor;
                                    arinc429Struct.SSMColor = SSMColor;
                                    arinc429Struct.DataColor = DataColor;
                                }
                            }
                            break;
                        }

                    }
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Error: {
                    if (list.size() > 0 && list.get(list.size() - 1).isDataFrameEnd()) {
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                        arinc429Struct.BeginX = list.get(list.size() - 1).EndX;
                    }
                    if (arinc429Struct == null) {
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
                        arinc429Struct.BeginX = 0;
                        arinc429Struct.isBeginFrame=false;
                    }


                    arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
                    arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp);
                    arinc429Struct.Data = "Err";
                    arinc429Struct.DataColor = ErrorColor;
                    arinc429Struct.DataType = bytes.get(i + 3) &0x0F;
                    list.add(arinc429Struct);

                }
                break;
            }
        }

        //将最后一个添加上
        if (list.size() > 0 && arinc429Struct != null)
            if (arinc429Struct.hashCode() != list.get(list.size() - 1).hashCode()) {
                arinc429Struct.Data = "?";
                arinc429Struct.DataColor = DataColor;
//                arinc429Struct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                arinc429Struct.EndX = this.endX - this.startX;
                list.add(arinc429Struct);
            }
        return list;
    }

    private void drawParseArinc429Data(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.Arinc429Struct> list) {
        paint.setXfermode(srcMode);
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint);
//        Logger.i(Command.TAG,"list size:"+list.size());
        for (int i = 0; i < list.size(); i++) {
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null);
//            }

//            Logger.i(Command.TAG,"index to string:"+list.get(i).toString());
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i));
        }
        paint.setXfermode(desoutMode);
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight());
        canvas.drawRect(rectTitleLen, paint);
    }

    //endregion


    //region 绘制<==>  公有函数

    /**
     * @param canvas 画布
     * @param beginX 开始位置 单位pix
     * @param width  绘制宽度 单位pix
     * @param value  显示值
     */
    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, String value, int textColor,boolean isBeginFrame) {
        final int ShowPixReVise = 2;
        final int ShowTxtReVise = 6;
        int mid = canvas.getHeight() / 2;
        int color = paint.getColor();
        if (value == null) {
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint);
        } else if ("?".equals(value) || !isBeginFrame) {

            Rect rect = Tools.getTextRect(value, paint);
            //int top = (canvas.getHeight() - rect.height()) / 2 + rect.height()-1;
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics= paint.getFontMetrics();
            float fTop=fontMetrics.top;
            float fBottom=fontMetrics.bottom;

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2);
            int left = (width - mid - rect.width()) / 2 + mid;

            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
            } else {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                if (this.startX != beginX ) {
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
                }
                if ((beginX + width - mid) < (this.endX - mid)) {
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
                } else {
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint);
                }
                paint.setColor(textColor);
                canvas.drawText(value, beginX + width / 2, top, paint);
            }
        } else if (width < mid * 2) {
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
        } else if (width >= mid * 2) {
            paint.setXfermode(desoutMode);
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
            canvas.drawRect(rectTitleLen, paint);
            paint.setXfermode(srcMode);
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
            //写字
            Rect rect = Tools.getTextRect(value, paint);
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            int left = (width - mid * 2 - rect.width()) / 2 + mid;
            paint.setColor(textColor);
            if (left < mid / 2) {
                rect = Tools.getTextRect("?", paint);
                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText("?", beginX + width / 2, top, paint);
            } else {
//                canvas.drawText(value, left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint);
            }
        }

        paint.setColor(color);
    }

    /**
     * @param canvas     画布
     * @param beginX     开始位置 单位pix
     * @param width      绘制宽度 单位pix
     * @param value      显示值
     * @param ShortValue 简写显示值
     */
    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, String value, String ShortValue, int textColor,boolean isBeginFrame) {
        final int ShowPixReVise = 2;
        final int ShowTxtReVise = 6;
        int mid = canvas.getHeight() / 2;
        int color = paint.getColor();
        if (value == null) {
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint);
        }
        else if (value.equals("?") || !isBeginFrame) {
            Rect rect = Tools.getTextRect(value, paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics= paint.getFontMetrics();
            float fTop=fontMetrics.top;
            float fBottom=fontMetrics.bottom;

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2);
            int left = (width - mid - rect.width()) / 2 + mid;

            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
            } else {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                if (this.startX != beginX ) {
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
                }
                if ((beginX + width - mid) < (this.endX - mid)) {
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
                } else {
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint);
                }
                paint.setColor(textColor);
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint);
            }
        } else if (width < mid * 2) {
//            Rect rect = Tools.getTextRect("?", paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
//            int left = (width - mid * 2 - rect.width()) / 2 + mid;
//            if (left < mid/2){
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
//                canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            }
//            else {
//                paint.setXfermode(desoutMode);
//                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
//                canvas.drawRect(rectTitleLen, paint);
//                paint.setXfermode(srcMode);
//                // 画<>
//                // 画<
//                canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
//                canvas.drawLine(beginX, mid, beginX + mid, canvas.getHeight() , paint);
//                // 画>
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, canvas.getHeight() , paint);
//                //上下-
//                canvas.drawLine(beginX + mid, 0, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + mid, canvas.getHeight()-1 , beginX + width - mid, canvas.getHeight()-1 , paint);
//
//                rect = Tools.getTextRect("?", paint);
//                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                paint.setColor(textColor);
//                canvas.drawText("?", left + beginX, top, paint);
//
//            }
        } else if (width >= mid * 2) {
            paint.setXfermode(desoutMode);
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
            canvas.drawRect(rectTitleLen, paint);
            paint.setXfermode(srcMode);
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
            //写字
            Rect rect = Tools.getTextRect(value, paint);
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height()-2;
            int left = (width - mid * 2 - rect.width()) / 2 + mid;

            Rect shortRect = Tools.getTextRect(ShortValue, paint);
            int shortTop = (canvas.getHeight() - shortRect.height()) / 2 + shortRect.height();
            int shortLeft = (width - mid * 2 - shortRect.width()) / 2 + mid;

            paint.setColor(textColor);
            if (left > mid / 2) {
//                canvas.drawText(value, left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint);
            } else if (shortLeft > mid / 2) {
                Paint.Align al = paint.getTextAlign();
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(ShortValue, shortLeft + beginX, shortTop, paint);
                paint.setTextAlign(al);
            } else {
                rect = Tools.getTextRect("?", paint);
                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText("?", beginX + width / 2, top, paint);
            }
        }

        paint.setColor(color);
    }

    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, SerialBusStruct.Arinc429Struct arinc429Struct) {
        final int ShowPixReVise = 2;
        final int ShowTxtReVise = 6;

        int mid = canvas.getHeight() / 2;
        int color = paint.getColor();
        if (arinc429Struct == null) {
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint);
        } else if (arinc429Struct.Data.equals("?") /*|| !arinc429Struct.isBeginFrame*/) {

            Rect rect = Tools.getTextRect(arinc429Struct.Data, paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics= paint.getFontMetrics();
            float fTop=fontMetrics.top;
            float fBottom=fontMetrics.bottom;

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2);
            int left = (width - mid - rect.width()) / 2 + mid;
            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
            } else {
                paint.setXfermode(desoutMode);
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
                canvas.drawRect(rectTitleLen, paint);
                paint.setXfermode(srcMode);

                if (this.startX != beginX ) {
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
                }
                if ((beginX + width - mid) < (this.endX - mid)) {
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
                } else {
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint);
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint);
                }
                paint.setColor(arinc429Struct.DataColor);
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText(arinc429Struct.Data, beginX + width / 2, top, paint);
            }
        } else if (width < mid * 2) {
            //画 |  且补上空白的后线
//            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
//            canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            Rect rect = Tools.getTextRect("?", paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
//            int left = (width - mid * 2 - rect.width()) / 2 + mid;
//            if (left < mid/2){
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
//                canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            }
//            else {
//                paint.setXfermode(desoutMode);
//                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
//                canvas.drawRect(rectTitleLen, paint);
//                paint.setXfermode(srcMode);
//                // 画<>
//                // 画<
//                canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
//                canvas.drawLine(beginX, mid, beginX + mid, canvas.getHeight() , paint);
//                // 画>
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, canvas.getHeight() , paint);
//                //上下-
//                canvas.drawLine(beginX + mid, 0, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + mid, canvas.getHeight()-1 , beginX + width - mid, canvas.getHeight()-1 , paint);
//
//                rect = Tools.getTextRect("?", paint);
//                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                paint.setColor(arinc429Struct.DataColor);
//                canvas.drawText("?", left + beginX, top, paint);
//
//            }
        } else if (width >= mid * 2) {
            paint.setXfermode(desoutMode);
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
            canvas.drawRect(rectTitleLen, paint);
            paint.setXfermode(srcMode);
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint);
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint);
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint);
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint);
            //写字
            String value = "";
            int spaceWidth = 10;
            int times = 0;
            if (arinc429Struct.SDI != null) {
                value = arinc429Struct.SDI;
                times++;
            }
            if (arinc429Struct.SSM != null) {
                value += arinc429Struct.SSM;
                times++;
            }
            if (arinc429Struct.Data != null) {
                value += arinc429Struct.Data;
            }

            Rect rect = Tools.getTextRect(value, paint);
            rect.set(rect.left, rect.top, rect.right + spaceWidth * times, rect.bottom);
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            int left = (width - mid * 2 - rect.width()) / 2 + mid;
            //paint.setColor(textColor);

            Paint.Align al = paint.getTextAlign();
            paint.setTextAlign(Paint.Align.LEFT);
            if (left < mid / 2) {
                rect = Tools.getTextRect("?", paint);
                left = (width - mid * 2 - rect.width()) / 2 + mid;
                paint.setColor(arinc429Struct.DataColor);
                canvas.drawText("?", left + beginX, top, paint);
            } else {
                int temX = left + beginX;
                if (arinc429Struct.SDI != null) {
                    value = arinc429Struct.SDI;
                    paint.setColor(arinc429Struct.SDIColor);
                    canvas.drawText(value, temX, top, paint);
                    temX += Tools.getTextRect(value, paint).width() + spaceWidth;
                }
                if (arinc429Struct.Data != null) {
                    value = arinc429Struct.Data;
                    paint.setColor(arinc429Struct.DataColor);
                    canvas.drawText(value, temX, top, paint);
                    temX += Tools.getTextRect(value, paint).width() + spaceWidth;
                }
                if (arinc429Struct.SSM != null) {
                    value = arinc429Struct.SSM;
                    paint.setColor(arinc429Struct.SSMColor);
                    canvas.drawText(value, temX, top, paint);
//                    temX += Tools.getTextRect(value, paint).width() + 10;
                }
            }
            paint.setTextAlign(al);
        }

        paint.setColor(color);
    }




    static char getExtASCII(int id){
        char val = '\0';
        if(id>= 0xA1 && id <= 0xFF){
            switch (id){
                case 0xAD:
                    val = '\0';
                    break;
                default:
                    val = (char)id;
                    break;
            }
        }
        return val;
    }

    /**
     * 整型转换成 进制
     *
     * @param Coding   ICharacterEncoding类型
     * @param id       整型数据
     * @param bitWidth 保留字符串宽度，位宽
     * @return 返回字符串
     */
    private static String getEncoding(int Coding, int id, int bitWidth) {
        String s = "";
        int width = 1;
        Long data = id | (1L << 63);
        switch (Coding) {
            case ICharacterEncoding.ASCII: {
//                char s1 = ((id >= 48 && id <= 57) || (id >= 65 && id <= 90) || (id >= 97 && id <= 122)) ? (char) id : '\0';
                char s1 = ((id >= 32) && (id <= 126)) ? (char) id : '\0';
                if(id >= 0xA1){
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
        if (Coding==ICharacterEncoding.ASCII){
            return s.substring(s.length() - width, s.length());
        }
        else {
            //取字串s的后width个字符输出
            return s.toUpperCase().substring(s.length() - width, s.length());
        }
    }
    //endregion

    public static void main(String[] arg){

    }
}
