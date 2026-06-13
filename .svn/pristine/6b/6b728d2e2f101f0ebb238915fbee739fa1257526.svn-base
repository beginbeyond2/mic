package com.micsig.tbook.tbookscope.middleware.command;


import android.os.Build;
import android.os.SharedMemory;
import android.util.Log;

import com.micsig.tbook.scope.Data.Waveform;
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Waveform {

//     new SCPICommandStruct(":WAVeform:BEGin","SCPI_Waveform","Begin"),//启动波形的读取
//     new SCPICommandStruct(":WAVeform:DATA?","SCPI_Waveform","DataQ"),//读取波形数据
//     new SCPICommandStruct(":WAVeform:END","SCPI_Waveform","End"),//停止波形的读取
//     new SCPICommandStruct(":WAVeform:MODE","SCPI_Waveform","Mode"),//设置波形的读取模式
//     new SCPICommandStruct(":WAVeform:MODE?","SCPI_Waveform","ModeQ"),//查询波形的读取模式
//     new SCPICommandStruct(":WAVeform:PREamble?","SCPI_Waveform","PreambleQ"),//查询全部的波形参数
//     new SCPICommandStruct(":WAVeform:RESet","SCPI_Waveform","Reset"),//复位波形的读取
//     new SCPICommandStruct(":WAVeform:SOURce","SCPI_Waveform","Source"),//设置波形读取的通道源
//     new SCPICommandStruct(":WAVeform:SOURce?","SCPI_Waveform","SourceQ"),//查询波形读取的通道源
//     new SCPICommandStruct(":WAVeform:STARt","SCPI_Waveform","Start"),//设置内存中波形被读取的起始位置
//     new SCPICommandStruct(":WAVeform:STARt?","SCPI_Waveform","StartQ"),//查询内存中波形被读取的起始位置
//     new SCPICommandStruct(":WAVeform:STATus?","SCPI_Waveform","StatusQ"),//查询当前的波形读取状态
//     new SCPICommandStruct(":WAVeform:STOP","SCPI_Waveform","Stop"),//设置内存中波形被读取的停止位置
//     new SCPICommandStruct(":WAVeform:STOP?","SCPI_Waveform","StopQ"),//查询内存中波形被读取的停止位置
//     new SCPICommandStruct(":WAVeform:XINCrement?","SCPI_Waveform","XincrementQ"),//查询指定源x方向上相邻两点的时间差
//     new SCPICommandStruct(":WAVeform:XORigin?","SCPI_Waveform","XoriginQ"),//查询指定源x方向从触发点到参考时间基准的时间
//     new SCPICommandStruct(":WAVeform:XREFerence?","SCPI_Waveform","XreferenceQ"),//查询指定源x方向上数据点的参考时间基准
//     new SCPICommandStruct(":WAVeform:YINCrement?","SCPI_Waveform","YincrementQ"),//查询指定源y方向上相邻两点的时间差
//     new SCPICommandStruct(":WAVeform:YORigin?","SCPI_Waveform","YoriginQ"),//查询指定源y方向从触发点到参考时间基准的时间
//     new SCPICommandStruct(":WAVeform:YREFerence?","SCPI_Waveform","YReferenceQ"),//查询指定源y方向上数据点的参考时间基准

    private int mode;
    private int source;
    private int start;
    private int stop;
    private int format;


    /**
     * 读取波形数据
     */
    public StringBuilder DataQ() {

        return Waveform.getInstance().readWave();
    }
    public StringBuilder DataHexQ(){
        return ReadAllHexWave();
    }
    public StringBuilder DataBinQ(){
        return ReadALLBinWave();
    }
    public StringBuilder DataASCIIQ(){
        return readALLAsciiWave();
    }

    private StringBuilder ReadAllHexWave(){
//        long begin1=new Date().getTime();
        StringBuilder sb=new StringBuilder();
        int len= Waveform.getInstance().getModeNormalAllWaveByGpu();
        sb.append("#9");
        sb.append(String.format("%09d",len));
        long begin2=new Date().getTime();
        ByteBuffer bb= Waveform.getInstance().getWaveBak();
        SCPICommandDeal.getInstance().writeShareMem(sb,len,bb,4);
//        Log.d("Tag.Debug", String.format("Command_Waveform.ReadAllWave Total:%d ,write SHM: %d",(new Date().getTime()-begin1),(new Date().getTime()-begin2) ));
        sb.setLength(0);
        return sb;
    }

    private StringBuilder ReadALLBinWave(){
        SharedMemory shm= SCPICommandDeal.getInstance().getSharedMem();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Waveform.getInstance().writeBinToShm(shm);
        }
        StringBuilder sb=new StringBuilder(0);
        return sb;
    }

    private StringBuilder readALLAsciiWave(){
//        Log.d("Tag.Debug", String.format("Command_Waveform.readALLAsciiWave: " ));
        int len=Waveform.getInstance().getModeNormalAllAsciiWaveByGpu();

        StringBuilder sb=new StringBuilder();
        sb.append("#9");
        sb.append(String.format("%09d",len));
        //long begin2=new Date().getTime();
        ByteBuffer bb= Waveform.getInstance().getWaveBak();
//        Log.d("Tag.Debug", String.format("Command_Waveform.readALLAsciiWave: writeSHM complete!" ));
        SCPICommandDeal.getInstance().writeShareMem(sb,len,bb,16);

        sb.setLength(0);
        return sb;
    }

    /**
     * 设置波形的读取模式
     */
    public void Mode(int mode, boolean isUpdateUI) {
        this.mode = mode;
        Waveform.getInstance().setMode(mode);
    }

    /**
     * 查询波形的读取模式
     */
    public int ModeQ() {
        return mode;
    }

    /**
     * 查询全部的波形参数
     * <format>,<type>,<points>,<count>,<xincrement>,<xorigin>,<xreference>,<yincrement>,<yorigin>,<yreference>
     */
    public String PreambleQ() {
        StringBuilder sb=new StringBuilder();
        sb.append(this.format);
        sb.append(",");
        sb.append(this.mode);
        sb.append(",");
//        sb.append(Waveform.getInstance().getPoints());
//        sb.append(",");
        sb.append(Waveform.getInstance().getAverage());
        sb.append(",");
        sb.append(XincrementQ());
        sb.append(",");
        sb.append(XoriginQ());
        sb.append(",");
        sb.append(XreferenceQ());
        sb.append(",");
        sb.append(YincrementQ());
        sb.append(",");
        sb.append(YOriginQ());
        sb.append(",");
        sb.append(YReferenceQ());
        return sb.toString();
    }

    /**
     * 设置波形读取的通道源
     */
    public void Source(int source, boolean isUpdateUI) {
        this.source = source;
        Waveform.getInstance().setSource(source);

    }

    /**
     * 查询波形读取的通道源
     */
    public int SourceQ() {
        return Waveform.getInstance().getSource();
    }

    public void Format(int format,boolean isUpdateUI){
        this.format=format;
        Waveform.getInstance().setFormat(format);
    }
    public int FormatQ(){
        return format;
    }

    /**
     * 设置内存中波形被读取的起始位置
     */
    public void Start(int index, boolean isUpdateUI) {
        this.start = index;
        Waveform.getInstance().setStartPos(index);
    }

    /**
     * 查询内存中波形被读取的起始位置
     */
    public int StartQ() {
        return start;
    }

    /**
     * 查询当前的波形读取状态
     *
     * @return 查询返返回“IDLE”或“READ”
     */
    public int StatusQ() {
        return 0;
    }

    /**
     * 设置内存中波形被读取的停止位置
     */
    public void Stop(int stop, boolean isUpdateUI) {
        this.stop = stop;
        Waveform.getInstance().setStopPos(stop);
    }

    /**
     * 查询内存中波形被读取的停止位置
     */
    public int StopQ() {
        return stop;
    }

    /**
     * 查询指定源x方向上相邻两点的时间差
     *
     * @return 查询以科学计数形式返回时间差值
     */
    public double XincrementQ() {
        return Waveform.getInstance().getXincrement();
    }

    /**
     * 查询指定源x方向从触发点到参考时间基准的时间
     */
    public double XoriginQ() {
        return Waveform.getInstance().getXorigin();
    }

    /**
     * 查询指定源x方向上数据点的参考时间基准
     */
    public double XreferenceQ() {
        return Waveform.getInstance().getXReference();
    }

    /**
     * 查询指定源y方向上相邻两点的时间差
     */
    public double YincrementQ() {
//        return ChannelFactory.getDynamicChannel(this.source).getVerticalPerPix();
        return Waveform.getInstance().getYincrement();
    }

    /**
     * 查询指定源y方向从触发点到参考时间基准的时间
     */
    public double YOriginQ() {
//        Channel ch = ChannelFactory.getDynamicChannel(this.source);
////        double d= ch.getVScaleIdVal(ch.getVScaleId())*ch.getProbeRate();
////        Logger.i(Command.TAG,"d:"+d+",per pix value:"+ch.getVerticalPerPix());
//        int iCh=CacheUtil.CH1;
//        switch (this.source){
//            case ChannelFactory.CH1:iCh=CacheUtil.CH1;break;
//            case ChannelFactory.CH2:iCh=CacheUtil.CH2;break;
//            case ChannelFactory.CH3:iCh=CacheUtil.CH3;break;
//            case ChannelFactory.CH4:iCh=CacheUtil.CH4;break;
//        }
//        int posPix=CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_CH_Y_POSITION_YT + iCh);
//        double d= ch.getVerticalPerPix()*posPix;
//        return d;
        return Waveform.getInstance().getYOrigin();
    }

    /**
     * 查询指定源y方向上数据点的参考时间基准
     */
    public double YReferenceQ() {
        return Waveform.getInstance().getYReference();
    }


}
