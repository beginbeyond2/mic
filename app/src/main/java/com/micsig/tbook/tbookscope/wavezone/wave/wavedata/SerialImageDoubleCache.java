package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;


import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class SerialImageDoubleCache {
    private static final String TAG="SerialImageDoubleCache";

    private static final int MaxCache=1024*1024;
    private static final String Cache1="Cache1";
    private static final String Cache2="Cache2";
//    private HashMap<String,SerialImageBuffer> cacheS1=new HashMap<>();
//    private HashMap<String,SerialImageBuffer> cacheS2=new HashMap<>();
//    private SerialImageBuffer lastCacheS1;
//    private SerialImageBuffer lastCacheS2;
    private HashMap<String,SerialImageBuffer>[] cache=new HashMap[TChan.MaxSerial];
    private SerialImageBuffer[] lastCache=new SerialImageBuffer[TChan.MaxSerial];

    private static final Object syncLock=new Object();
    private static SerialImageDoubleCache instance;

    public static SerialImageDoubleCache getInstance() {
        if (instance==null) {
            synchronized (syncLock){
                if (instance==null){
                    instance=new SerialImageDoubleCache();
                }
            }
        }
        return instance;
    }

    public SerialImageDoubleCache(){
        TChan.foreachSerial((ch)->{
            int idx=ch-TChan.S1;
            cache[idx]=new HashMap<>();
            SerialImageBuffer buffer=new SerialImageBuffer(Cache1,ByteBuffer.allocate(MaxCache),0,0,0);
            cache[idx].put(Cache1,buffer);
            buffer=new SerialImageBuffer(Cache2,ByteBuffer.allocate(MaxCache),0,0,0);
            cache[idx].put(Cache2,buffer);
        });

//        SerialImageBuffer buffer=new SerialImageBuffer(Cache1,ByteBuffer.allocate(MaxCache),0,0,0);
//        cacheS1.put(Cache1,buffer);
//        buffer=new SerialImageBuffer(Cache2,ByteBuffer.allocate(MaxCache),0,0,0);
//        cacheS1.put(Cache2,buffer);
//
//        buffer=new SerialImageBuffer(Cache1,ByteBuffer.allocate(MaxCache),0,0,0);
//        cacheS2.put(Cache1,buffer);
//        buffer=new SerialImageBuffer(Cache2,ByteBuffer.allocate(MaxCache),0,0,0);
//        cacheS2.put(Cache2,buffer);
    }

    public SerialImageBuffer getLastCache(int tChan) {
//        if (iwaveCh== TChan.S1){
//            return lastCacheS1;
//        }
//        else {
//            return lastCacheS2;
//        }

        if (TChan.isSerial(tChan)) {
            int idx = tChan - TChan.S1;
            return lastCache[idx];
        }
        return lastCache[0];
    }

    public HashMap<String, SerialImageBuffer> getCache(int tChan){
//        if (iwaveCh== TChan.S1){
//            return cacheS1;
//        }
//        else { return  cacheS2;}

        if (TChan.isSerial(tChan)){
            int idx=tChan-TChan.S1;
            return cache[idx];
        }
        return cache[0];
    }
    //返回已经解析显示完的缓冲区
    public SerialImageBuffer getCacheShowed(int tChan){
//        if (iwaveCh== TChan.S1){
//            if (!cacheS1.get(Cache1).isDoing()){
//                return cacheS1.get(Cache1);
//            }
//            else{
//                return cacheS1.get(Cache2);
//            }
//        }
//        else {
//            if (!cacheS2.get(Cache1).isDoing()){
//                return cacheS2.get(Cache1);
//            }
//            else{
//                return cacheS2.get(Cache2);
//            }
//
//        }
        if (TChan.isSerial(tChan)){
            int idx=tChan-TChan.S1;
            if (!cache[idx].get(Cache1).isDoing()){
                return cache[idx].get(Cache1);
            }else {
                return cache[idx].get(Cache2);
            }
        }else {
            if (!cache[0].get(Cache1).isDoing()){
                return cache[0].get(Cache1);
            }else {
                return cache[0].get(Cache2);
            }
        }

    }

    public SerialImageDoubleCache put(int tChan,String key, SerialImageBuffer serialImageBuffer){
//        if (iwaveCh== TChan.S1){
//            cacheS1.put(key,serialImageBuffer);
//        }
//        else {
//            cacheS2.put(key,serialImageBuffer);
//        }
//        return this;


        int idx=tChan-TChan.S1;
        cache[idx].put(key,serialImageBuffer);
        return this;
    }
    //添加
    public SerialImageDoubleCache put(int tChan, SerialImageBuffer serialImageBuffer){
//        if (iwaveCh== TChan.S1){
////            if (cacheS1.get(Cache1).isDeal()) cacheS1.put(Cache1,serialImageBuffer);
////            else cacheS1.put(Cache2,serialImageBuffer);
//            cacheS1.put(serialImageBuffer.getKey(),serialImageBuffer);
//            lastCacheS1=serialImageBuffer;
//        }
//        else {
////            if (cacheS2.get(Cache1).isDeal()) cacheS2.put(Cache1,serialImageBuffer);
////            else cacheS2.put(Cache2,serialImageBuffer);
//            cacheS2.put(serialImageBuffer.getKey(),serialImageBuffer);
//            lastCacheS2=serialImageBuffer;
//        }

        int idx=tChan-TChan.S1;
        cache[idx].put(serialImageBuffer.getKey(),serialImageBuffer);
        lastCache[idx]=serialImageBuffer;
        return this;
    }
}
