package com.micsig.tbook.scope.Data;


import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhuzh on 2018-3-27.
 */

public class DataFactory implements IDataListener {
    private static final String TAG = "DataFactory";
    private static volatile DataFactory instance = null;


    public static DataFactory getInstance() {
        if (instance == null) {
            synchronized (DataFactory.class) {
                if (instance == null ) {
                    instance = new DataFactory();

                }
            }
        }
        return instance;
    }
    private SyncHeader getSyncHeader(int idx){
        return SyncHeader.getInstance(idx);
    }
    private DataFactory(){
        WAVE_SIZE = CH_WAVE_SIZE * 4;//ChannelFactory.getChNums();
        SERIAL_SIZE = CH_SERIAL_SIZE * 2;//ChannelFactory.getSerialChNums();
    }

    public static final int SYNC_HEADER_SIZE = 1800 * 4;

    public static final int BITMAP_SIZE = 1800 * 1000 * 4;
    private static final int CH_WAVE_SIZE = 400 * 2 * 1024;
    private static  int WAVE_SIZE = CH_WAVE_SIZE * 4;
    private static final int CH_SERIAL_SIZE = 64 * 1024;
    private static  int SERIAL_SIZE = CH_SERIAL_SIZE * 2;

    @Override
    public boolean onRecv(int idx,ByteBuffer byteBuffer, int length) {
        boolean bDraw = false;
        boolean bValid = false;
        SyncHeader syncHeader = getSyncHeader(idx);
        byteBuffer.order(ByteOrder.nativeOrder());
        syncHeader.setByteBuffer(byteBuffer);
        syncHeader.setIdx(SYNC_HEADER_SIZE);
        bValid = syncHeader.isValid();

//        Logger.d(TAG,"idx:" + idx + ",sync:" + SyncHeader.getSyncHeader() + ",isSegmentTimestamp:" + syncHeader.isSegmentTimestamp() + ",frameNums:" + syncHeader.getSegmentFrameNums());
//        Logger.d(TAG,"idx:" + idx + ",bValid:" + bValid + ",header:" + syncHeader.getHeader() + ",sample:" + syncHeader.isSample() + ",last: " + syncHeader.isLastSample());
//        Logger.d(TAG,"idx:" + idx + ",syncHeader.isBitmapValid():" + syncHeader.isBitmapValid());
//        Log.d(TAG,"idx:" + idx + ",bValid:" + bValid + ",isOnlyWave:" + syncHeader.isOnlyWave());
//        Log.d(TAG,"bValid:" + bValid + ",istxt:" + syncHeader.isSerialTxtValid());

        if(bValid){
            Scope scope = Scope.getInstance();
            boolean []ChSample = new boolean[ChannelFactory.CH_CNT];
            int cnt = scope.getChannelSampOnCnt(scope.isRun(true),ChSample);

            if(syncHeader.isSegmentTimestamp()){
                if(syncHeader.isValid()) {
                    recvSegmentTimestamp(syncHeader,byteBuffer,cnt);
                }
            }else if(syncHeader.isOnlyWave()){
                recvOnlyWave(syncHeader,byteBuffer);
            }else {
                if(syncHeader.isSerialTxtValid()){
                    recvSerialTxt(syncHeader,byteBuffer,cnt);
                }else {
                    if (syncHeader.isSerialValid()) {
                        recvSerial(syncHeader,byteBuffer,cnt);
                    }
                    syncHeader.setIdx(syncHeader.getIdx() + SERIAL_SIZE);
                    if (syncHeader.isWaveValid()) {
                         recvWave(syncHeader,byteBuffer,cnt,ChSample);
                    }
                }
                if(syncHeader.isValid()) {
                    EventFactory.sendEvent(new EventBase(EventFactory.EVENT_SAMPLE_VALID,
                            new boolean[]{
                                    syncHeader.isSample() || syncHeader.isLastSample(),
                                    syncHeader.getFpgaIdx() == 0,
                                    syncHeader.isSample()
                            }));
                }
            }
        }

        return bDraw;
    }
    private void calcMinMax(SyncHeader syncHeader,Channel channel,boolean [] bArray,boolean bMHO,boolean bRun){

        int cnt = syncHeader.getChNum();
        int idx = 0;
        int chIdx = channel.getChId();
//        Log.d(TAG,"cnt:" + cnt + ",idx:" + idx + ",chIdx:"  + chIdx
//                + ",fpgaIdx:" + syncHeader.getFpgaIdx());
        int beginIdx = FPGACommand.beginChIdx(syncHeader.getFpgaIdx());
        int endIdx = FPGACommand.endChIdx(syncHeader.getFpgaIdx());
        switch (cnt)
        {
            case 1:
                idx = 0;
                break;
            case 2:
            {
                for (int i = beginIdx; i < endIdx; i++) {
                    if (bArray[i]) {
                        if (i == chIdx) {
                            break;
                        }
                        idx++;
                    }
                }
            }
            break;
            default:
            case 4:
                idx = chIdx-beginIdx;
                break;
        }

        int maxVal,minVal;

        if (bMHO) {
            maxVal = syncHeader.getChMaxVal16Bit(idx);
            minVal = syncHeader.getChMinVal16Bit(idx);
            channel.setMaxMinVal16Bit(maxVal, minVal,bRun);
        } else {
            maxVal = syncHeader.getChMaxVal(idx);
            minVal = syncHeader.getChMinVal(idx);
            channel.setMaxMinVal(maxVal, minVal,bRun);
        }

        //Logger.d("zhuzh","cnt:" + cnt + ",chidx:" + channel.getChId() + ",idx:" + idx +",maxVal:" + maxVal + ",minVal:" + minVal);

    }


    private void recvSegmentTimestamp(SyncHeader syncHeader,ByteBuffer byteBuffer,int cnt){

        Scope scope = Scope.getInstance();
        int frameNums = syncHeader.getSegmentFrameNums();
        Logger.d(TAG,"frameNums:" + frameNums
                + ",getSegmentFrameNums:" + scope.getSegmentFrameNums()
                + ",sampleState:" + Sample.getInstance().getSampleState());

        scope.clearSegmentTimestamp();

        Log.d(TAG, "recvSegmentTimestamp(),cnt:" + cnt  +",chNum:" + syncHeader.getChNum());
        if (cnt / HwConfig.getInstance().getFpgaNums() == syncHeader.getChNum()) {
            if(frameNums > SegmentSample.SEGMENT_MAX){
                frameNums = SegmentSample.SEGMENT_MAX;
            }
            //int frameNo = syncHeader.getSegmentFrameNo();
            int frameNo = frameNums -1;
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
            scopeFrozen.setSegmentFrameNums(frameNums);
            SegmentSample.getInstance().setSegmentFrames(frameNums);
            int idx = 0;
            if(frameNo < frameNums - 1){
                idx = frameNo + 1;
            }
            int j = syncHeader.getIdx();
            for(int f=0;f<frameNums;f++) {
                scope.addSegmentTimestamp(byteBuffer.getLong(j + idx * 8) * 8L);
                if(++idx >= frameNums){
                    idx = 0;
                }
            }
            scopeFrozen.calcFrameNo(frameNums);
            if(frameNums > 0) {

                EventFactory.sendEvent(EventFactory.EVENT_SEGMENT_TIMESTAMP);
            }
        }
    }
    ExecutorService executorService = Executors.newFixedThreadPool(ChannelFactory.CH_CNT);
    private class WaveTask implements Runnable{

        private Channel channel;
        private int cnt;
        boolean [] ChSample;
        ByteBuffer byteBuffer;
        int waveLength;
        int Idx;
        SyncHeader syncHeader;

        public WaveTask(Channel channel,boolean [] ChSample,int cnt,
                        ByteBuffer byteBuffer,int idx,int waveLength,SyncHeader syncHeader){
            this.channel = channel;
            this.ChSample = ChSample;
            this.cnt = cnt;
            this.byteBuffer = byteBuffer;
            this.Idx = idx;
            this.waveLength = waveLength;
            this.syncHeader = syncHeader;
        }
        @Override
        public void run() {
            Scope scope = Scope.getInstance();
            boolean bMHO = true;
            WaveData waveData  = (WaveData) channel.dequeue();

            if (waveData != null) {
                SerializationChannel(syncHeader,channel, waveData,cnt);
                //if(syncHeader.isSample() || syncHeader.isLastSample())
                {
                    calcMinMax(syncHeader,channel,ChSample,bMHO,syncHeader.isSample() || syncHeader.isLastSample());
                }
                int wavelen = waveLength;
                if(syncHeader.isSample()){
                    waveData.setPlaceVal(channel.getPlaceVal());
                    int w = ScopeBase.getWidth();
                    int colAddr = syncHeader.getRollColAddr();
                    if(syncHeader.isRoll()) {
                        wavelen = (w - colAddr) * waveLength/w;
                        waveData.setStartX(colAddr);
                    } else if(syncHeader.isSlowScale()){
                        if(scope.isZoom()){
                            wavelen = waveLength;
                        } else {
                            waveData.setEndX(colAddr);
                            wavelen =  (colAddr + 1) * waveLength / w;
                        }
                    }
                }

                waveData.convert16to32(byteBuffer, Idx , wavelen * 2,waveData.getPlaceVal());
                waveData.setSyncHeader(syncHeader.getHeader());
                channel.enqueue(waveData);
                channel.setWaveValid(true);
            }
        }
    }
    private void recvWave(SyncHeader syncHeader,ByteBuffer byteBuffer,int cnt,boolean [] ChSample){
        Channel channel;
        int waveLength = syncHeader.getWaveLength();
        boolean bWave = false;

        int beginIdx = FPGACommand.beginChIdx(syncHeader.getFpgaIdx());
        int endIdx = FPGACommand.endChIdx(syncHeader.getFpgaIdx());


        List<Future<?>> listFutures = new ArrayList<>();
        for(int i=beginIdx;i<endIdx;i++){
            if(ChSample[i]){
                channel = ChannelFactory.getDynamicChannel(i);
                if(channel != null && (channel.isNeedWave()
                        || !syncHeader.isSample()
                        || SyncHeader.isCalibrate()
                        || syncHeader.isLastSample()
                )) {
                    listFutures.add(executorService.submit(new WaveTask(channel,ChSample,cnt,
                            byteBuffer,syncHeader.getIdx(),waveLength,syncHeader)));
                    bWave = true;
                }
                syncHeader.setIdx(syncHeader.getIdx()+CH_WAVE_SIZE);
            }
        }
        if(bWave){
            for(Future<?> f:listFutures){
                try {
                    f.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(bWave) {
            EventFactory.sendEvent(EventFactory.EVENT_CH_WAVE_UPDATE,true);
        }
    }
    private void recvOnlyWave(SyncHeader syncHeader,ByteBuffer byteBuffer){

        SaveBin.getInstance().save(byteBuffer,syncHeader.getIdx(),SaveBin.MAX_GET_VAL );
    }
    private void recvSerial(SyncHeader syncHeader,ByteBuffer byteBuffer,int cnt){
        IDataBuffer dataBuffer;
        SerialChannel channel;
        int length = 0;
        boolean bSerial = false;
        int l = syncHeader.getIdx();
        int beginIdx = FPGACommand.beginSerialIdx(syncHeader.getFpgaIdx());
        int endIdx = FPGACommand.endSerialIdx(syncHeader.getFpgaIdx());
        for(int i=beginIdx;i<endIdx;i++){
            length = syncHeader.getSerialLength(i-beginIdx);
            channel = ChannelFactory.getSerialChannel(i);

            if(channel != null
                    && channel.isOpen()) {
                if(IBus.isBusEnable(channel.getBusType())) {
                    dataBuffer = channel.dequeue();
                    if (dataBuffer != null) {
                        SerializationChannel(syncHeader,channel, dataBuffer,cnt);
                        dataBuffer.write(byteBuffer, l+ (CH_SERIAL_SIZE * (i-beginIdx)), length);
                        channel.enqueue(dataBuffer);
                        bSerial = true;
                    }
                }
            }

        }
        if(bSerial) {
            EventFactory.sendEvent(EventFactory.EVENT_SERIAL_UPDATE);
        }
    }
    private void recvSerialTxt(SyncHeader syncHeader,ByteBuffer byteBuffer,int cnt){
        IDataBuffer dataBuffer;
        SerialChannel channel;
        int length = 0;

        int beginIdx = FPGACommand.beginSerialIdx(syncHeader.getFpgaIdx());
        int endIdx = FPGACommand.endSerialIdx(syncHeader.getFpgaIdx());
        for (int i = beginIdx; i < endIdx; i++) {
            length = syncHeader.getSerialTxtLength(i - beginIdx);
            channel = ChannelFactory.getSerialChannel(i);
            if (length > 0) {
                if (channel != null
                        && channel.isOpen() && IBus.isBusEnable(channel.getBusType())) {
                    dataBuffer = channel.dequeue();
                    int l = syncHeader.getIdx();
                    if (dataBuffer != null) {
                        SerializationChannel(syncHeader, channel, dataBuffer, cnt);
                        dataBuffer.write(byteBuffer, l, length * 2);
                        channel.enqueue(dataBuffer);
                        EventBase e = new EventBase(EventFactory.EVENT_SERIAL_TXT_UPDATE, channel);
                        EventFactory.sendEvent(e);
                    }
                    if ((length & 0x01) == 0x01) {
                        length += 1;
                    }
                    syncHeader.setIdx(l + length * 2);
                }
            }
        }
    }


    private static int BUFFER_MAX_NUMS = 4;
    public static IBufferQueue allocateBufferQueue(boolean singleBuffer){

        BufferQueue bufferQueue = null;
        int nums = BUFFER_MAX_NUMS;
        if(singleBuffer){
            bufferQueue = new SingleBuffer();
            nums = 1;
        }else{
            bufferQueue = new BufferQueue();
        }
        for(int i=0;i<nums;i++) {
            bufferQueue.add(new WaveData());
        }
        return bufferQueue;
    }

    public static IBufferQueue allocateBufferSerial(){
        BufferQueue bufferQueue = new BufferQueue();
        for(int i=0;i<BUFFER_MAX_NUMS;i++){
            bufferQueue.add(new SerialData());
        }
        return bufferQueue;
    }

    private static void SerializationDynamicCh(Channel channel, WaveData waveData,int chCnt){
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
        Scope scope = Scope.getInstance();
        waveData.setWaveType(WaveData.DYNAMIC_WAVE);
        waveData.setLabel(channel.getLabel());
        waveData.setTimeScaleVal(horizontalAxis.getTimeScaleIdVal());
        waveData.setChIdx(channel.getChId());
        waveData.setStartX(horizontalAxis.getStartX());
        waveData.setEndX(horizontalAxis.getEndX());
        MathChannel mathChannel = ChannelFactory.getMathChannel(ChannelFactory.MATH1);
        waveData.setSampRate(mathChannel.getSampleRate());
        waveData.setSampRate2display(mathChannel.getSampleRate2display());
        waveData.setAdPix(channel.getAdPix());
        waveData.setXPos(horizontalAxis.getTimePosOfView());
        waveData.setMemDepthSet(MemDepthFactory.getMemDepthSet());
        waveData.setMemDepthItemIdx(MemDepthFactory.getMemDepth().getMemDepthItem());
        waveData.setBinType(scope.isSegmentEnable() ? WaveData.SEGMENT_BIN : WaveData.WAVE_BIN);

        waveData.setSegmentNums(1);
        waveData.setSegmentLen((int)scope.zunMemDepth(chCnt));
        waveData.setVerticalPerGridPixels(ScopeBase.getVerticalPerGridPixels());
        double wavFactor = HwConfig.getInstance().getWavFactor();
        if(scope.isRun(true)){
            waveData.setPlaceVal(channel.getPlaceVal());
            waveData.setProbeRate(channel.getProbeRate());
            waveData.setProbeType(channel.getProbeType());
            waveData.setProbeStr(channel.getProbeStr());
            waveData.setVScaleVal(channel.getVScaleVal());
            waveData.setVerticalPerPix(waveData.getVScaleVal() / ScopeBase.getVerticalPerGridPixels() * wavFactor * channel.getYFactor());
            waveData.setYPosition(channel.getPos());
            waveData.setRoll(scope.isInScrollMode());

        }else{
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();
            waveData.setPlaceVal(scopeFrozen.getPlaceVal(channel.getChId()));
            VerticalAxis verticalAxis = scopeFrozen.getChVertical(channel.getChId());
            waveData.setProbeRate(verticalAxis.getProbeRate());
            waveData.setProbeType(verticalAxis.getProbeType());
            waveData.setProbeStr(verticalAxis.getProbeStr());
            waveData.setVScaleVal(verticalAxis.getScaleVal());
            wavFactor *= scopeFrozen.getYFactor(channel.getChId());
            waveData.setVerticalPerPix(wavFactor * verticalAxis.getScaleVal() / ScopeBase.getVerticalPerGridPixels());
            waveData.setYPosition(scopeFrozen.getChPos(channel.getChId()));
            waveData.setRoll(scopeFrozen.isRool());
        }

        //waveData.setTotalTime();
    }



    private static void SerialzationSerialCh(SyncHeader syncHeader,SerialChannel channel,SerialData serialData){
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
        serialData.setTimePrePix(horizontalAxis.getTimesPrePixExt());
        serialData.setStartX(horizontalAxis.getStartX());
        serialData.setEndX(horizontalAxis.getEndX());
        serialData.setChIdx(channel.getChId());
        serialData.setBusType(channel.getBusType());
        serialData.setSerialType(Scope.getInstance().isSerialText()?SerialData.SERIAL_TXT:SerialData.SERIAL_GRAPHICAL);
        if(!syncHeader.isSerialTxtValid()) {
            serialData.setLastData(false);
        }else{
            serialData.setLastData(syncHeader.isSerialTxtLast());
        }
    }

    public static void SerializationChannel(SyncHeader syncHeader,IChannel ch, IDataBuffer dataBuffer,int chCnt){
        if(ChannelFactory.isDynamicCh(ch.getChId())){
            SerializationDynamicCh((Channel) ch,(WaveData) dataBuffer,chCnt);
        }else if(ChannelFactory.isSerialCh(ch.getChId())){
            SerialzationSerialCh(syncHeader,(SerialChannel)ch,(SerialData) dataBuffer);
        }
    }

    public static String getDebugBytesToString(ByteBuffer bytes){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<bytes.limit();i++){
            sb.append(Integer.toHexString(bytes.get(i))).append(" ");
        }
        return sb.toString();
    }

}
