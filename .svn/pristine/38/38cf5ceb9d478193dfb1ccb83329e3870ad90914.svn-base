package com.micsig.tbook.scope.Data;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.SharedMemory;
import android.system.ErrnoException;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.mathEx.MathExt;
import com.micsig.tbook.scope.mem.Memory;

import java.nio.ByteBuffer;

public class Waveform {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_MAX = 1;
    public static final int MODE_RAW = 2;

    public static final int FORMAT_WORD = 0;
    public static final int FORMAT_BYTE = 1;
    public static final int FORMAT_ASCII = 2;

    public static final int MAX_LENGTH = 250000;
    public static final int MAX_RAW_LENGTH = 1024 * 1024 * 3;
    private int chIdx = 0;
    private int mode = MODE_NORMAL;
    private int start_pos = 0;
    private int stop_pos = 0;
    private int average = 0;
    private int format = FORMAT_BYTE;


    private WaveData waveData = new WaveData();
    private ByteBuffer waveBak=ByteBuffer.allocateDirect(10*1024*1024);

    private static Waveform waveform = null;
    public static synchronized Waveform getInstance(){

        if(waveform == null){
            waveform = new Waveform();
        }
        return waveform;
    }
    private Waveform(){

    }
    public void setMode(int mode){
        if((mode == MODE_MAX || mode == MODE_RAW) && !Scope.getInstance().isRun()){
            mode = MODE_RAW;
        }else{
            mode = MODE_NORMAL;
        }
        this.mode = mode;
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel != null){
            WaveData w = (WaveData) channel.obtain();
            if(w != null){
                Sample sample = Sample.getInstance();
                Memory.Memcpy(waveData.getDirectBuffer(), 0, w.getDirectBuffer(), 0, WaveData.BUFFER_SIZE);
                if (sample.getSampleType()==Sample.SAMPLE_TYPE_AVERAGE || sample.getSampleType()==Sample.SAMPLE_TYPE_ENVEL) {
                    average = sample.getSampleNum();
                }else{
                    average=1;
                }
                waveData.setRoll(w.isRoll());
                channel.recycle(w);

                if(mode == MODE_RAW){

                    if(!SaveBin.getInstance().save(channel,false)){
                        waveData.setWaveLength(0);
                    }else {
                        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
                        waveData.setSampRate(scopeFrozen.getSampFre());
                        waveData.setWaveLength(scopeFrozen.getMemDepth());
                    }
                }
                this.start_pos = 0;
                this.stop_pos = waveData.getWaveLength() - 1;
            }
        }
    }
    public void setFormat(int format){
        this.format = format;
    }
    public int getFormat(){
        return format;
    }
    public void setSource(int chIdx){
        if(ChannelFactory.isDynamicCh(chIdx)) {
            this.chIdx = chIdx;
        }
    }
    public int getSource(){
        return chIdx;
    }
    public void setStartPos(int pos){
        pos -= 1;
        int len = waveData.getWaveLength();
        if (pos >= 0 && pos < len) {
            this.start_pos = pos;
        }
        if(mode == MODE_RAW && !Scope.getInstance().isRun()){
            SaveBin.getInstance().nexSegment(this.start_pos);
        }
    }
    public void setStopPos(int pos){
        pos -= 1;
        int len = waveData.getWaveLength();
        if(pos >= 0 && pos < len) {
            this.stop_pos = pos;
        }
    }
    public int getStartPos(){
        return this.start_pos;
    }
    public int getStopPos(){
        return this.stop_pos;
    }
    private final static char [] hexChar = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static StringBuilder Hex2String(StringBuilder builder,int val){
        int idx = 0;
        for(int i=0;i<4;i++){
            idx = (val>>(12 - i * 4))&0xF;
            builder.append(hexChar[idx]);
        }
        return builder;
    }
    @SuppressLint("DefaultLocale")
    private StringBuilder getModeNormalWave(){
        int i = 0;
        StringBuilder sb = new StringBuilder(MAX_LENGTH + 64);
        ByteBuffer byteBuffer = waveData.getDirectBuffer();
        int placeVal = waveData.getPlaceVal();
        double vv = waveData.getVerticalPerPix();
        sb.append("#9000000000");
        if(byteBuffer != null){
            int s = this.start_pos * 4 + WaveData.WAVE_HEADER_SIZE;
            int e = this.stop_pos * 4 + WaveData.WAVE_HEADER_SIZE;
            int val = 0;
            while (s <= e){
                val = byteBuffer.getInt(s);
                switch (format){
                    case FORMAT_WORD:
                    case FORMAT_BYTE:
                        Hex2String(sb,val-placeVal);
                        break;
                    case FORMAT_ASCII:{
                        sb.append(String.format("%e",vv * val));
                        sb.append(",");
                    }
                        break;
                }
                s += 4;
                i ++;
                if(sb.length() >= MAX_LENGTH + 11){
                    break;
                }
            }
            this.start_pos += i;
        }
        if(i > 0){
            sb.replace(2,11,String.format("%09d",i));
        }
        return sb;
    }
    @SuppressLint("DefaultLocale")
    private StringBuilder getModeRawWave(){
        int i = 0;
        StringBuilder sb = new StringBuilder(MAX_LENGTH + 64);
        sb.append("#9000000000");
        SaveBin saveBin = SaveBin.getInstance();
        ByteBuffer byteBuffer = saveBin.getBinBuffer();

        int placeVal = waveData.getPlaceVal();
        double vv = waveData.getVerticalPerPix();
        if(byteBuffer != null){
            int val = 0;
            while (this.start_pos <= this.stop_pos){
                if(byteBuffer.position() >= byteBuffer.limit()){
                    if(!saveBin.nexSegment()){
                        break;
                    }
                }
                val = byteBuffer.getShort();

                switch (format){
                    case FORMAT_WORD:
                    case FORMAT_BYTE:
                        Hex2String(sb,val);
                        break;
                    case FORMAT_ASCII:
                        sb.append(String.format("%e",vv * (val + placeVal)));
                        sb.append(",");
                        break;
                }
                this.start_pos++;
                i += 1;
                if(sb.length() >= MAX_LENGTH + 11){
                    break;
                }
            }
        }
        if(i > 0){
            sb.replace(2,11,String.format("%09d",i));
        }
        return sb;
    }
    public StringBuilder readWave(){
        if(mode == MODE_NORMAL){
            return getModeNormalWave();
        }else{
            return getModeRawWave();
        }
    }
    public int getModeNormalAllWaveByGpu(){
        final int DOT_LENGTH=4;
        if (start_pos>=waveData.getWaveLength()*DOT_LENGTH || this.mode==MODE_RAW) return 0;
        ByteBuffer byteBuffer = waveData.getDirectBuffer();
        int placeVal = waveData.getPlaceVal();
        double vv = waveData.getVerticalPerPix();
        int len=waveData.getWaveLength();

        int end=len*DOT_LENGTH+WaveData.WAVE_HEADER_SIZE;
        start_pos=waveBak.capacity();

        byteBuffer.position(WaveData.WAVE_HEADER_SIZE);
        byteBuffer.limit(WaveData.WAVE_HEADER_SIZE+len*4);

        MathExt.IntToHex(byteBuffer.asIntBuffer(),waveBak ,placeVal,WaveData.WAVE_HEADER_SIZE, len);
        waveBak.position(0);
        waveBak.limit(len*DOT_LENGTH);
        return len;
    }
    // ASCII 一个点16个字符
    public int getModeNormalAllAsciiWaveByGpu(){
        final int DOT_LENGTH=16;
        if (start_pos>=waveData.getWaveLength()*DOT_LENGTH || this.mode==MODE_RAW) return 0;
        ByteBuffer byteBuffer = waveData.getDirectBuffer();
        int placeVal = waveData.getPlaceVal();
        double vv = waveData.getVerticalPerPix();
        int len=waveData.getWaveLength();

        int end=len*DOT_LENGTH+WaveData.WAVE_HEADER_SIZE;
        start_pos=waveBak.capacity();

        byteBuffer.position(WaveData.WAVE_HEADER_SIZE);
        byteBuffer.limit(WaveData.WAVE_HEADER_SIZE+len*4);

        MathExt.DoubleToASCII(byteBuffer.asIntBuffer(),waveBak ,vv,WaveData.WAVE_HEADER_SIZE, len);

        waveBak.position(0);
        waveBak.limit(len*DOT_LENGTH);
        return len;
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void writeBinToShm(SharedMemory shm){

        ByteBuffer srcBuf = waveData.getDirectBuffer();
        int placeVal = waveData.getPlaceVal();
        double vv = waveData.getVerticalPerPix();

        int len=waveData.getWaveLength();
        if (start_pos>=waveData.getWaveLength()*4 || this.mode==MODE_RAW)
            len=0;

        try{
            ByteBuffer desBuf=shm.mapReadWrite();
            int sum=len*4+13;
            String head=String.format("#9%09d",len);
            String end="\r\n";
            srcBuf.position(WaveData.WAVE_HEADER_SIZE);
            srcBuf.limit(WaveData.WAVE_HEADER_SIZE+len*4);

            desBuf.putInt(sum);
            desBuf.put(head.getBytes());
            desBuf.put(srcBuf);
            desBuf.put(end.getBytes());
            SharedMemory.unmap(desBuf);

            start_pos=waveBak.capacity();
        }catch (ErrnoException e) {
            e.printStackTrace();
        }
    }
    public ByteBuffer getWaveBak(){
        return waveBak;
    }


    public int getPoints(){
        return waveData.getWaveLength();
    }

    public int getAverage(){
        return average;
    }

    public double getXincrement(){
        return 1.0 / waveData.getSampRate();
    }

    public double getXorigin(){
        if(waveData.isRoll()){
            return 0;
        }else {
            return (double) waveData.getXPos() / HorizontalAxis.Sto100FS(1);
        }
    }

    public double getYincrement(){
        return waveData.getVerticalPerPix();
    }

    public double getYOrigin() {//像素值 * 每像素对应幅值 = 实际幅值
        return waveData.getYPosition() * waveData.getVerticalPerPix();
    }
    public double getXReference(){
        return 0;
    }

    public double getYReference(){
        return waveData.getPlaceVal();
    }

}
