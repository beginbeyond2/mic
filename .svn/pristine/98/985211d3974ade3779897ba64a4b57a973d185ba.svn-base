package com.micsig.tbook.scope.Data;

import android.hardware.OtherManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.mem.Memory;
import com.micsig.tbook.scope.surface.DeviceFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveRecoverySession {
    public static final String TAG = "SaveRecoverySession";
    public static final String MSS_REF_TAG = UUID.randomUUID().toString();
    private static SaveRecoverySession instance;

    public static synchronized SaveRecoverySession getInstance(){
        if(instance == null){
            instance = new SaveRecoverySession();
        }
        return instance;
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SaveRecoverySession(){
    }

    private static final Gson gson = new Gson();
    private static final Type mapType = new TypeToken<HashMap<String,HashMap<String,String>>>(){}.getType();
    private static byte[] toJsonBytes(HashMap<String,HashMap<String,String>> map){
        String json = gson.toJson(map,mapType);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    private static  HashMap<String,HashMap<String,String>> fromJsonBytes(byte[] jsonBytes){
        String json = new String(jsonBytes,StandardCharsets.UTF_8);
        return gson.fromJson(json,mapType);
    }
    private static final int MAGICNUM = 0x20250805;
    private static final int VERSION = 0;

    private static final int MSS_AA55 = 0xAA550000;
    private static final int MSS_PRODUCT = 1;
    private static final int MSS_SN = 2;
    private static final int MSS_SYSID = 3;

    private static final int MSS_CONFIG = 4;
    private static final int MSS_REF_NUMS = 5;
    private static final int MSS_REF_WAVE = 6;

    private static final int MSS_SCOPE = 7;

    private static final int MSS_TYPE_MASK = 0xFF;

    //16bit + 8bit + 8bit
    //16bit 0xAA55
    //8bit  refid
    //8bit msstype ,java < 100, JNI C++ >= 100
    private static boolean isMssValid(int mss,int t){
        return ((mss & MSS_TYPE_MASK) == t) && ((mss & 0xFFFF0000) == MSS_AA55);
    }
    public long estimateMaxStorage(){
        long s = SegmentSample.SEGMENT_LENGTH
                * Short.BYTES;
        s *= 1.2;
        return s;
    }
    public long estimateStorage(){
        Scope scope = Scope.getInstance();
        if(!scope.isRun()) {
            SegmentSample segmentSample = SegmentSample.getInstance();
            long frameNums = 1;
            if(segmentSample.isSegmentEnable()){
                frameNums = scope.getSegmentFrameNums();
            }
            if(frameNums < 1){
                frameNums = 1;
            }
            long s = frameNums
                    * SegmentSample.getSegmentSpace(scope.zunMemDepth())
                    * scope.getChannelSampOnCnt(false)
                    * Short.BYTES;
            s *= 1.2;
            return s;
        }
        return -1;
    }
    public int getSaveRecoveryProgress(){
        if(getStatus() == S_INIT) {
            return DeviceFactory.allocDevice().getSaveRecoveryProgress();
        }
        return 100;
    }
    private boolean storeRef(RefChannel refChannel,RandomAccessFile raf){
        IDataBuffer dataBuffer = refChannel.obtain();
        ByteBuffer byteBuffer = dataBuffer.getByteBuffer();
        boolean ret = false;
        try {
            int idx = refChannel.getChId() - ChannelFactory.REF1;
            ret = writeByteBuffer(((idx&0xFF) << 8) | MSS_REF_WAVE,byteBuffer,raf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refChannel.recycle(dataBuffer);
        return ret;
    }
    private boolean writeByteBuffer(int mssType,ByteBuffer byteBuffer,RandomAccessFile raf) throws IOException{
        byteBuffer.clear();
        int l = byteBuffer.capacity();
        raf.writeInt(mssType | MSS_AA55);
        raf.writeLong(Long.reverseBytes(l));
        return raf.getChannel().write(byteBuffer)==l;
    }
    private void writeString(int mssType,String str,RandomAccessFile raf) throws IOException {
        byte[] bytes =  str.getBytes(StandardCharsets.UTF_8);
        writeBytes(mssType,bytes,raf);
    }
    private void writeBytes(int mssType,byte [] bytes,RandomAccessFile raf) throws IOException {
        raf.writeInt(mssType | MSS_AA55);
        raf.writeLong(Long.reverseBytes(bytes.length));
        raf.write(bytes);
    }
    private void writeInt(int mssType,int val,RandomAccessFile raf) throws IOException{
        raf.writeInt(mssType | MSS_AA55);
        raf.writeLong(Long.reverseBytes(Integer.BYTES));
        raf.writeInt(val);
    }
    int status = 0;
    public static final int S_INIT = 1;
    public static final int S_SUCESS = 2;
    public static final int S_FAIL = 3;
    public synchronized boolean isDone(){
        return status > S_INIT;
    }


    public static final int ERR_NONE = 0;
    public static final int ERR_PRODUCT = -1000;
    public static final int ERR_VERSION = -1001;
    public static final int ERR_IO      = -1002;

    private int errCode = ERR_NONE;

    public synchronized int getErrCode(){
        return errCode;
    }
    public synchronized void setErrCode(int errCode){
        this.errCode = errCode;
    }

    private synchronized void setStatus(int s){
        Log.d(TAG, "setStatus() called with: s = [" + s + "]");
        status = s;
    }
    public synchronized int getStatus(){
        return status;
    }
    private AtomicBoolean forceStop = new AtomicBoolean(false);

    public void stop(){
        forceStop.set(true);
        DeviceFactory.allocDevice().stop();
        Log.d(TAG, "stop() called");
    }

    public synchronized void store(HashMap<String,HashMap<String,String>> map,String pathName){
        setStatus(S_INIT);
        setErrCode(ERR_NONE);
        forceStop.set(false);
        executorService.execute(() -> {
            boolean ret = false;
            File file = new File(pathName);
            if(file.exists()){
                file.delete();
            }
//            if (!file.exists())
            {
                try {
                    ret = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (ret) {

                    try (RandomAccessFile raf = new RandomAccessFile(pathName, "rw")) {

                        Scope scope = Scope.getInstance();
                        raf.writeInt(MAGICNUM);
                        raf.writeInt(VERSION);
                        writeString(MSS_PRODUCT,scope.getProduct(), raf);
                        writeString(MSS_SN,scope.getSn(), raf);
                        writeString(MSS_SYSID,OtherManager.getString("ro.product.id"), raf);
                        writeBytes(MSS_CONFIG,toJsonBytes(map),raf);
                        ArrayList<RefChannel> list = new ArrayList<>();
                        for (int i = ChannelFactory.REF1; i < ChannelFactory.getMaxRefIdx(); i++) {
                            RefChannel refChannel = ChannelFactory.getRefChannel(i);
                            if (refChannel != null
                                    && refChannel.isOpen()) {
                                list.add(refChannel);
                            }
                        }
                        writeInt(MSS_REF_NUMS,list.size(),raf);
                        boolean bref = true;
                        for (RefChannel refChannel : list) {
                            if (!storeRef(refChannel, raf)) {
                                Logger.e(TAG,"ref storeRef");
                                bref = false;
                            }
                        }
                        if(bref) {

                            writeString(MSS_SCOPE,ScopeFrozen.getInstance().toJson(),raf);

                            ret = DeviceFactory.allocDevice().store(raf);
                            if(!ret){
                                if(forceStop.get()){
                                    if(file.exists()){
                                        file.delete();
                                    }
                                }
                                setErrCode(ERR_IO);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(ret){
                        Memory.Sync();
                    }
                }
            }
            setStatus(ret ? S_SUCESS : S_FAIL);
        });
    }
    private String readString(int [] mss,RandomAccessFile raf) throws IOException {
        byte [] bytes = readBytes(mss,raf);
        if(bytes != null && bytes.length > 0){
            return new String(bytes,StandardCharsets.UTF_8);
        }
        return "";
    }
    private byte[] readBytes(int [] mss ,RandomAccessFile raf) throws IOException{
        mss[0] = raf.readInt();
        mss[1] = (int) Long.reverseBytes(raf.readLong());
        byte [] bytes = new byte[mss[1]];
        if(raf.read(bytes) == bytes.length){
            return bytes;
        }
        return null;
    }
    private int readInt(int [] mss,RandomAccessFile raf) throws IOException{
        mss[0] = raf.readInt();
        mss[1] = (int) Long.reverseBytes(raf.readLong());
        return raf.readInt();
    }


    public void restore(HashMap<String,HashMap<String,String>> map,String pathName){
        setStatus(S_INIT);
        setErrCode(ERR_NONE);
        forceStop.set(false);
        executorService.execute(() -> {
            boolean ret = false;
            File file = new File(pathName);
            if (file.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(pathName, "r")) {
                    Scope scope = Scope.getInstance();
                    if (raf.readInt() == MAGICNUM
                            && raf.readInt() == VERSION) {
                        do {
                            int[] mss = {0, 0};
                            String product = readString(mss, raf);
                            if (!isMssValid(mss[0], MSS_PRODUCT)) break;
                            String sn = readString(mss, raf);
                            if (!isMssValid(mss[0], MSS_SN)) break;
                            String sysid = readString(mss, raf);
                            if (!isMssValid(mss[0], MSS_SYSID)) break;
                            Logger.d(TAG, "From mss file product:" + product + " ,sn:" + sn + " ,sysid:" + sysid + " ,pathName= " + pathName);
                            Logger.d(TAG, "From device product:" + scope.getProduct() + " ,sn:" + scope.getSn() + " ,sysid:" + OtherManager.getString("ro.product.id"));
                            if (product.equals(scope.getProduct())
                                    && sysid.equals(OtherManager.getString("ro.product.id"))) {

                                byte[] bytes = readBytes(mss, raf);
                                if (bytes != null
                                        && bytes.length == mss[1]
                                        && isMssValid(mss[0],MSS_CONFIG)) {
                                    HashMap<String, HashMap<String, String>> map1 = fromJsonBytes(bytes);
                                    for (Map.Entry<String, HashMap<String, String>> entry : map1.entrySet()) {
                                        map.put(entry.getKey(), new HashMap<>(entry.getValue()));
                                    }
                                    int n = readInt(mss, raf);
                                    if (!isMssValid(mss[0], MSS_REF_NUMS)) break;
                                    for (int i = 0; i < n; i++) {
                                        int id = raf.readInt();
                                        if (isMssValid(id,MSS_REF_WAVE)) {
                                            id = ((id >> 8) & 0xFF) + ChannelFactory.REF1;
                                            int len = (int)Long.reverseBytes(raf.readLong());
                                            RefChannel refChannel = ChannelFactory.getRefChannel(id);
                                            if (refChannel != null) {
                                                IDataBuffer dataBuffer = refChannel.obtain();
                                                ByteBuffer byteBuffer = dataBuffer.getByteBuffer();
                                                byteBuffer.clear();

                                                if (len <= byteBuffer.capacity()) {
                                                    byteBuffer.limit(len);
                                                    int xn = raf.getChannel().read(byteBuffer);
                                                    refChannel.loadWave();
                                                }
                                                refChannel.recycle(dataBuffer);
                                            }else{
                                                byte [] bytes1 = new byte[len];
                                                raf.readFully(bytes1);
                                            }
                                        }
                                    }
                                    String s = readString(mss, raf);
                                    if (!isMssValid(mss[0], MSS_SCOPE)) {
                                        Log.d(TAG,"mss:" + mss[0]);
                                        break;
                                    }
                                    ScopeFrozen.getInstance().fromJson(s);
                                    ret = DeviceFactory.allocDevice().restore(raf);
                                    if(!ret){
                                        if(forceStop.get()){
                                            if(file.exists()){
                                                file.delete();
                                            }
                                        }
                                        setErrCode(ERR_IO);
                                    }
                                }
                            } else {
                                setErrCode(ERR_PRODUCT);
                            }
                        }while (false);
                    }else{
                        setErrCode(ERR_VERSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setStatus(ret ? S_SUCESS : S_FAIL);
        });
    }
}
