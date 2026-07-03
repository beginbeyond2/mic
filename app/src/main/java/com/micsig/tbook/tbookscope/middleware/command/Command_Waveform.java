package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import android.os.Build; // Android版本构建信息
import android.os.SharedMemory; // 共享内存（用于SCPI数据传输）
import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.Data.Waveform; // 波形数据管理
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal; // SCPI命令处理

import java.nio.ByteBuffer; // 字节缓冲区
import java.util.Date; // 日期工具

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Waveform                                     |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器波形数据命令处理模块                                          |
 * | 核心职责: 处理SCPI波形数据相关指令，包括波形数据读取（Hex/Bin/ASCII格式）、    |
 * |          读取模式设置/查询、前导参数查询、通道源设置/查询、                    |
 * |          起止位置设置/查询、X/Y方向参数查询等                                 |
 * | 架构设计: 命令模式，作为Command子模块，通过Waveform单例操作波形数据           |
 * |          大数据量传输通过SharedMemory共享内存实现（SCPI标准）                  |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → Waveform单例 → 共享内存              |
 * | 依赖关系: Command, Waveform, SCPICommandDeal, SharedMemory                   |
 * | 使用场景: 远程读取波形数据、查询波形参数、设置波形读取模式时使用              |
 * +-----------------------------------------------------------------------------+
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

    private int mode; // 波形读取模式
    private int source; // 波形读取通道源
    private int start; // 波形读取起始位置
    private int stop; // 波形读取停止位置
    private int format; // 波形数据格式


    /**
     * 读取波形数据（默认格式）
     *
     * @return 波形数据字符串
     */
    public StringBuilder DataQ() {

        return Waveform.getInstance().readWave(); // 通过Waveform单例读取波形数据
    }

    /**
     * 读取波形数据（Hex十六进制格式）
     *
     * @return 波形数据字符串（数据已写入共享内存）
     */
    public StringBuilder DataHexQ(){
        return ReadAllHexWave(); // 调用Hex格式读取方法
    }

    /**
     * 读取波形数据（Bin二进制格式）
     *
     * @return 波形数据字符串（数据已写入共享内存）
     */
    public StringBuilder DataBinQ(){
        return ReadALLBinWave(); // 调用Bin格式读取方法
    }

    /**
     * 读取波形数据（ASCII文本格式）
     *
     * @return 波形数据字符串（数据已写入共享内存）
     */
    public StringBuilder DataASCIIQ(){
        return readALLAsciiWave(); // 调用ASCII格式读取方法
    }

    /**
     * 以Hex十六进制格式读取全部波形数据，写入共享内存
     * 数据格式：#9 + 9位长度 + 波形数据字节
     *
     * @return 空StringBuilder（数据已通过共享内存传输）
     */
    private StringBuilder ReadAllHexWave(){
//        long begin1=new Date().getTime();
        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        int len= Waveform.getInstance().getModeNormalAllWaveByGpu(); // 获取GPU处理后的波形数据长度
        sb.append("#9"); // 追加IEEE 488.2定界符（9位长度）
        sb.append(String.format("%09d",len)); // 追加9位数据长度
        long begin2=new Date().getTime(); // 记录共享内存写入开始时间
        ByteBuffer bb= Waveform.getInstance().getWaveBak(); // 获取波形数据字节缓冲区
        SCPICommandDeal.getInstance().writeShareMem(sb,len,bb,4); // 将波形数据写入共享内存（4字节对齐）
//        Log.d("Tag.Debug", String.format("Command_Waveform.ReadAllWave Total:%d ,write SHM: %d",(new Date().getTime()-begin1),(new Date().getTime()-begin2) ));
        sb.setLength(0); // 清空字符串构建器
        return sb; // 返回空StringBuilder
    }

    /**
     * 以Bin二进制格式读取全部波形数据，写入共享内存
     * 需要Android 8.1+（API 27+）支持
     *
     * @return 空StringBuilder（数据已通过共享内存传输）
     */
    private StringBuilder ReadALLBinWave(){
        SharedMemory shm= SCPICommandDeal.getInstance().getSharedMem(); // 获取共享内存实例
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) { // Android 8.1+支持SharedMemory
            Waveform.getInstance().writeBinToShm(shm); // 将二进制波形数据写入共享内存
        }
        StringBuilder sb=new StringBuilder(0); // 创建空字符串构建器
        return sb; // 返回空StringBuilder
    }

    /**
     * 以ASCII文本格式读取全部波形数据，写入共享内存
     * 数据格式：#9 + 9位长度 + 波形数据字符
     *
     * @return 空StringBuilder（数据已通过共享内存传输）
     */
    private StringBuilder readALLAsciiWave(){
//        Log.d("Tag.Debug", String.format("Command_Waveform.readALLAsciiWave: " ));
        int len=Waveform.getInstance().getModeNormalAllAsciiWaveByGpu(); // 获取ASCII格式的波形数据长度

        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        sb.append("#9"); // 追加IEEE 488.2定界符
        sb.append(String.format("%09d",len)); // 追加9位数据长度
        //long begin2=new Date().getTime();
        ByteBuffer bb= Waveform.getInstance().getWaveBak(); // 获取波形数据字节缓冲区
//        Log.d("Tag.Debug", String.format("Command_Waveform.readALLAsciiWave: writeSHM complete!" ));
        SCPICommandDeal.getInstance().writeShareMem(sb,len,bb,16); // 将ASCII波形数据写入共享内存（16字节对齐）

        sb.setLength(0); // 清空字符串构建器
        return sb; // 返回空StringBuilder
    }

    /**
     * 设置波形的读取模式
     *
     * @param mode        读取模式
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Mode(int mode, boolean isUpdateUI) {
        this.mode = mode; // 保存读取模式
        Waveform.getInstance().setMode(mode); // 设置Waveform读取模式
    }

    /**
     * 查询波形的读取模式
     *
     * @return 读取模式
     */
    public int ModeQ() {
        return mode; // 返回读取模式
    }

    /**
     * 查询全部的波形前导参数
     * 返回格式: &lt;format&gt;,&lt;type&gt;,&lt;points&gt;,&lt;count&gt;,
     *           &lt;xincrement&gt;,&lt;xorigin&gt;,&lt;xreference&gt;,
     *           &lt;yincrement&gt;,&lt;yorigin&gt;,&lt;yreference&gt;
     *
     * @return 逗号分隔的前导参数字符串
     */
    public String PreambleQ() {
        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        sb.append(this.format); // 追加数据格式
        sb.append(","); // 追加分隔符
        sb.append(this.mode); // 追加读取模式
        sb.append(","); // 追加分隔符
//        sb.append(Waveform.getInstance().getPoints());
//        sb.append(",");
        sb.append(Waveform.getInstance().getAverage()); // 追加平均次数
        sb.append(","); // 追加分隔符
        sb.append(XincrementQ()); // 追加X方向时间增量
        sb.append(","); // 追加分隔符
        sb.append(XoriginQ()); // 追加X方向原点时间
        sb.append(","); // 追加分隔符
        sb.append(XreferenceQ()); // 追加X方向参考点
        sb.append(","); // 追加分隔符
        sb.append(YincrementQ()); // 追加Y方向增量
        sb.append(","); // 追加分隔符
        sb.append(YOriginQ()); // 追加Y方向原点
        sb.append(","); // 追加分隔符
        sb.append(YReferenceQ()); // 追加Y方向参考点
        return sb.toString(); // 返回前导参数字符串
    }

    /**
     * 设置波形读取的通道源
     *
     * @param source      通道源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Source(int source, boolean isUpdateUI) {
        this.source = source; // 保存通道源
        Waveform.getInstance().setSource(source); // 设置Waveform通道源

    }

    /**
     * 查询波形读取的通道源
     *
     * @return 通道源索引
     */
    public int SourceQ() {
        return Waveform.getInstance().getSource(); // 从Waveform单例查询通道源
    }

    /**
     * 设置波形数据格式
     *
     * @param format      数据格式
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Format(int format,boolean isUpdateUI){
        this.format=format; // 保存数据格式
        Waveform.getInstance().setFormat(format); // 设置Waveform数据格式
    }

    /**
     * 查询波形数据格式
     *
     * @return 数据格式
     */
    public int FormatQ(){
        return format; // 返回数据格式
    }

    /**
     * 设置内存中波形被读取的起始位置
     *
     * @param index       起始位置索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Start(int index, boolean isUpdateUI) {
        this.start = index; // 保存起始位置
        Waveform.getInstance().setStartPos(index); // 设置Waveform读取起始位置
    }

    /**
     * 查询内存中波形被读取的起始位置
     *
     * @return 起始位置索引
     */
    public int StartQ() {
        return start; // 返回起始位置
    }

    /**
     * 查询当前的波形读取状态
     *
     * @return 读取状态（0:IDLE，当前固定返回0）
     */
    public int StatusQ() {
        return 0; // 固定返回0（IDLE状态）
    }

    /**
     * 设置内存中波形被读取的停止位置
     *
     * @param stop        停止位置索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Stop(int stop, boolean isUpdateUI) {
        this.stop = stop; // 保存停止位置
        Waveform.getInstance().setStopPos(stop); // 设置Waveform读取停止位置
    }

    /**
     * 查询内存中波形被读取的停止位置
     *
     * @return 停止位置索引
     */
    public int StopQ() {
        return stop; // 返回停止位置
    }

    /**
     * 查询指定源X方向上相邻两点的时间差（时间增量）
     *
     * @return X方向时间增量（科学计数法）
     */
    public double XincrementQ() {
        return Waveform.getInstance().getXincrement(); // 从Waveform单例查询X方向时间增量
    }

    /**
     * 查询指定源X方向从触发点到参考时间基准的时间（X原点）
     *
     * @return X方向原点时间
     */
    public double XoriginQ() {
        return Waveform.getInstance().getXorigin(); // 从Waveform单例查询X原点
    }

    /**
     * 查询指定源X方向上数据点的参考时间基准（X参考点）
     *
     * @return X方向参考点索引
     */
    public double XreferenceQ() {
        return Waveform.getInstance().getXReference(); // 从Waveform单例查询X参考点
    }

    /**
     * 查询指定源Y方向上相邻两点的电压差（电压增量）
     *
     * @return Y方向电压增量
     */
    public double YincrementQ() {
//        return ChannelFactory.getDynamicChannel(this.source).getVerticalPerPix();
        return Waveform.getInstance().getYincrement(); // 从Waveform单例查询Y方向增量
    }

    /**
     * 查询指定源Y方向从触发点到参考时间基准的偏移（Y原点）
     *
     * @return Y方向偏移电压值
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
        return Waveform.getInstance().getYOrigin(); // 从Waveform单例查询Y原点
    }

    /**
     * 查询指定源Y方向上数据点的参考基准（Y参考点）
     *
     * @return Y方向参考点
     */
    public double YReferenceQ() {
        return Waveform.getInstance().getYReference(); // 从Waveform单例查询Y参考点
    }


}
