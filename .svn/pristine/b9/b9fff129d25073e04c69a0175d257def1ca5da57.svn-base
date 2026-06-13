package com.micsig.tbook.scope.Data;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Created by zhuzh on 2018-10-25.
 */

public class SaveBin extends XAction{
    private final static String TAG = "SaveBin";
    private static class SaveBinHolder {
        public static final SaveBin instance = new SaveBin();
    }

    public static SaveBin getInstance() {
        return SaveBin.SaveBinHolder.instance;
    }

    public final static int MAX_GET_VAL  = (1024*1024);
    private final static int BIN_BUFFER_MAX = MAX_GET_VAL*2;


    private SaveBin(){
        super();
        binBuffer = ByteBuffer.allocateDirect(BIN_BUFFER_MAX);
        binBuffer.order(ByteOrder.nativeOrder());
    }
    private volatile boolean bSaveFile = false;
    private volatile boolean bAllSegments = false;
    private Channel channel;
    private String pathName;
    private FileOutputStream os;
    private FileChannel fileChannel;
    private ByteBuffer binBuffer;
    private volatile boolean bSaveBin = false;

    private volatile int segmentLen = 0;
    private volatile int segmentIdx = 0;
    private volatile int segmentSpace = 0;
    private volatile long waveLen = 0;
    private volatile long wLen = 0;
    private volatile int status = 0; //取数要求status：[1:0] 00b停止取数 01b首次取数 10b末次取数 11b连续取数
    private volatile long nums = 0;

    public interface ISaveBinListener{
        void onProgress(int val);
    }

    private ISaveBinListener saveBinListener = null;
    public void setSaveBinListener(ISaveBinListener saveBinListener){
        this.saveBinListener = saveBinListener;
    }
    public ISaveBinListener getSaveBinListener(){
        return this.saveBinListener;
    }


    public long estimateStorage(){
        long useSize = calcWaveLen();
        if(SegmentSample.getInstance().isSegmentEnable()){
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
            useSize = (long) scopeFrozen.getSegmentFrameNums() * scopeFrozen.getMemDepth();
        }
        return useSize * 2 + 10*1024*1024;
    }
    private void calcSegmentSpace(){
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
        long zun =  scopeFrozen.getMemDepth();
        if(zun <= SegmentSample.SEGMENT_16M){
            int val = 1024;
            while( val < zun){
                val *= 2;
            }
            segmentSpace = val;
        }else{
            long k = 1;
            while ((k * SegmentSample.SEGMENT_16M) < zun){
                k++;
            }
            segmentSpace = (int) (k * SegmentSample.SEGMENT_16M);
        }
        Logger.d(TAG,"segmentSpace:" + segmentSpace);
    }
    public int getChIdx(){
        if(channel != null) {
            return channel.getChId();
        }else{
            return  0;
        }
    }

    public int getStatus(){
        return status;
    }

    public long getNums(){
        return nums;
    }
    public int getSegmentIdx(){return segmentIdx;}
    public int getSegmentLen(){return segmentLen;}
    public int getSegmentspace(){return segmentSpace;}

    private long calcWaveLen() {
        long w = ScopeBase.getWidth();
        ScopeFrozen frozen = ScopeFrozen.getInstance();
        waveLen = frozen.getMemDepth();
        long addr = frozen.getVaildAddr();
        if (frozen.isRool()) {
            waveLen = ((w - addr) * waveLen / w);
        } else if (frozen.isSlowScale()) {
            waveLen = ((addr + 1) * waveLen / w);
        }
        return waveLen;
    }
    private boolean saveHeader(){

        ScopeFrozen frozen = ScopeFrozen.getInstance();
        WaveData waveData  = (WaveData) channel.obtain();
       if(waveData != null){
           ByteBuffer headerBuffer = ByteBuffer.allocateDirect(WaveData.WAVE_HEADER_SIZE);
           headerBuffer.order(ByteOrder.nativeOrder());
           Memory.Memcpy(headerBuffer,waveData.getByteBuffer(),WaveData.WAVE_HEADER_SIZE);

           //16bit
           headerBuffer.putInt(12, 2);

           headerBuffer.putDouble(28,frozen.getSampFre());
           if(frozen.isRool()){
               headerBuffer.putLong(36,0);
           }

           segmentLen = frozen.getMemDepth();
           Logger.d(TAG,"segmentLen:" + segmentLen);

           headerBuffer.putInt(140,frozen.isSegmentEnable() ? 1: 0);
           int frameNums = 1;
           if(bAllSegments){
               frameNums = frozen.getSegmentFrameNums();
               Log.d(TAG,"frameNums:" + frameNums);
               if(frameNums > 0) {
                   waveLen += (long) (frameNums - 1) * segmentLen;
               }
           }
           headerBuffer.putInt(148,frameNums);
           headerBuffer.putInt(152,segmentLen);

           if ((int) waveLen < 0) {
               headerBuffer.putInt(8, 0xFFFFFFFF);
           } else {
               headerBuffer.putInt(8, (int) waveLen);
           }
           headerBuffer.putLong(210, waveLen);
           headerBuffer.put(218, (byte) Scope.getInstance().getChNum());

           headerBuffer.clear();
           headerBuffer.limit(WaveData.WAVE_HEADER_SIZE);

           if(bSaveFile) {
               try {
                   fileChannel.write(headerBuffer);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
           channel.recycle(waveData);
           return true;
       }
       return false;
    }


    private long getFileUseSzie(){
        long useSize = waveLen;

        if(bAllSegments){
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
            useSize = (long) scopeFrozen.getSegmentFrameNums() * scopeFrozen.getMemDepth();
        }
        return useSize * 2 + 10*1024*1024;
    }

    private boolean save(){
        bSaveBin = false;
        status = 1;
        wLen = 0;
        if(!Scope.getInstance().isRun()
                && ScopeFrozen.getInstance().isValid())
        {
            waveLen = calcWaveLen();

            if(waveLen > 0) {
                calcSegmentSpace();
                if(bSaveFile) {
                    File file = new File(pathName);
                    if (file.exists()) {
                        file.delete();
                    }

                    if (Utils.isDiskAvaiable(file, getFileUseSzie())) {

                        boolean bFaild = false;
                        try {
                            if (file.createNewFile()) {
                                os = new FileOutputStream(file);
                                fileChannel = os.getChannel();
                                if (saveHeader()) {
                                    sendEvent();
                                    sendCommand();
                                    bSaveBin = true;
                                } else {
                                    bFaild = true;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            if(bFaild) {
                                try {
                                    fileChannel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try{
                                    os.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                file.delete();
                            }
                        }
                    }
                }else{
                    if (saveHeader()) {
                        sendCommand();
                        bSaveBin = true;
                    } else {

                    }
                }

            }
        }
        return bSaveBin;
    }


    public void save(Channel channel,String pathName){
        save(channel,pathName,false);
    }
    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void save(Channel channel, String pathName,boolean bAllSegments,boolean bLast){
        this.channel = channel;
        this.pathName = pathName;
        this.bAllSegments = bAllSegments;
        this.bSaveFile = true;
        forceStop = false;
        Sample sample = Sample.getInstance();
        int sampleState = sample.getSampleState();
        if(sampleState != Sample.SAMPLE_STOP){
            sample.frozenSample();
            ms_sleep(1200);
        }

        if(this.save()){
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(forceStop && bSaveFile){
                    File file = new File(pathName);
                    if(file.exists()){
                        file.delete();
                    }
                    sendErr();
                }
                bSaveBin = forceStop = false;
            }

        }else{
            sendErr();
        }
        if(sampleState != Sample.SAMPLE_STOP ){
            sample.frozenSample(Sample.SAMPLE_RUN);
        }else{
            if(bLast) {
                SegmentSample segmentSample = SegmentSample.getInstance();
                segmentSample.setSegmentDisplayType(segmentSample.getSegmentDisplayType());
            }
        }
    }

    public void save(Channel channel, String pathName,boolean bAllSegments){
        save(channel,pathName,bAllSegments,true);
    }

    private void sendCommand(){
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
        nums = 0;
        segmentIdx = scopeFrozen.getSegmentFrameNo();

        if(wLen < waveLen){
            if(wLen > 0) status = 3;
            nums = waveLen - wLen;
            if(nums < MAX_GET_VAL) {
                if(wLen > 0) status = 2;
            }
            else {
                nums = MAX_GET_VAL;
            }

        }else{
            status = 0;
        }
        if(bAllSegments){
            long n = 1;
            if(segmentSpace < MAX_GET_VAL) {
                n = nums/segmentLen;
                if((nums % segmentLen) > 0){
                    n++;
                }
                while ((n * segmentSpace) > MAX_GET_VAL) {
                    n--;
                }
            }
            nums = n * segmentSpace;
            segmentIdx = (int) wLen / segmentLen;
            if(wLen < waveLen) {
                if ((wLen % segmentLen) == 0) {
                    status = 1;
                }
            }
        }
        sendFpgaMsg(FPGAMessage.FPGA_CMD_GET_DATA);
    }

    public void sendErr(){
        if(saveBinListener != null){
            saveBinListener.onProgress(-1);
        }else{
            EventBase eventBase = new EventBase(EventFactory.EVENT_SAVEBIN_RUN);
            eventBase.setData(-1);
            sendEvent(eventBase);
        }
    }

    public void sendEvent(){
        int x = (int) ( wLen * 100 / waveLen);
        if(saveBinListener != null){
            saveBinListener.onProgress(x);
        }else{
            if(bSaveBin) {
                EventBase eventBase = new EventBase(EventFactory.EVENT_SAVEBIN_RUN);
                eventBase.setData(x);
                sendEvent(eventBase);
            }
        }
    }


    public void save(ByteBuffer byteBuffer,int pos,int length){

        if(bSaveBin) {
            binBuffer.clear();

            int len = convert16(byteBuffer, pos, length, channel.getPlaceVal());

            binBuffer.limit(len * 2);
            wLen += len;
            if(bSaveFile) {
                try {
                    int temp = fileChannel.write(binBuffer);
//                    Logger.d(TAG, "writeBinLen= " + temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendCommand();
                if (wLen < waveLen) {
                    sendEvent();
                } else {

                    try {
                        os.flush();
                        os.getFD().sync();
                        fileChannel.close();
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            fileChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            os.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Memory.Sync();
                    sendEvent();
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }else{
                synchronized (this){
                    this.notifyAll();
                }
            }
            if(wLen >= waveLen) {
                bSaveBin = false;
                os = null;
                fileChannel = null;
            }
        }
    }

    private int convert16(ByteBuffer byteBuffer, int pos, int length, int val) {

        ScopeFrozen frozen = ScopeFrozen.getInstance();
        if(length > 0 && length * 2 <= BIN_BUFFER_MAX) {

            length = 0;
            if(segmentSpace > MAX_GET_VAL){
                int n = (int) (wLen / segmentLen);
                int len = (int) ((n + 1) * segmentLen - wLen);
                if(len > MAX_GET_VAL){
                    len = MAX_GET_VAL;
                }
                Memory.Memcpy(binBuffer,0,byteBuffer,pos,len * 2);
                length = len;
            }else{
                long len = waveLen - wLen;
                int m = MAX_GET_VAL/segmentSpace;
                long n = len/segmentLen;

                if( (len % segmentLen) > 0 ){
                    n += 1;
                }

                if(n > m){
                    n = m;
                }
                int x = 0;
                for(int i=0;i<n;i++)
                {
                    Memory.Memcpy(binBuffer,x*2,byteBuffer,pos,segmentLen * 2);
                    pos += segmentSpace*2;
                    x += segmentLen;
                }
                length = x;
            }
            return length;
        }
        return 0;
    }


    private volatile boolean forceStop = false;

    public synchronized void stop(){
        Log.d(TAG, "stop() called");
        forceStop = true;
        this.notifyAll();

    }


    //---------------------------------
    public boolean save(Channel channel, boolean bAllSegments){
        this.channel = channel;
        this.bAllSegments = bAllSegments;
        this.bSaveFile = false;
        if(this.save()){
            synchronized (this){
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                if(forceStop && bSaveFile){
                    File file = new File(pathName);
                    if(file.exists()){
                        file.delete();
                    }
                }
                bSaveBin = forceStop = false;
            }
            return true;
        }
        return false;
    }

    public ByteBuffer getBinBuffer(){
        return binBuffer;
    }

    public boolean nexSegment(){
        return nexSegment(wLen);
    }
    public boolean nexSegment(long pos){
        wLen = pos;
        this.sendCommand();
        synchronized (this){
            try {
                this.wait(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
                return false;
            }
            if(forceStop && bSaveFile){
                File file = new File(pathName);
                if(file.exists()){
                    file.delete();
                }
            }
            bSaveBin = forceStop = false;
        }
        return true;
    }
}
