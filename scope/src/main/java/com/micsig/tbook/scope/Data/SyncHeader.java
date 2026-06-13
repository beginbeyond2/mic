package com.micsig.tbook.scope.Data;

import android.util.Log;

import com.micsig.tbook.scope.mem.Memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by zhuzh on 2018-4-11.
 */

public class SyncHeader {
    private static String TAG = "SyncHeader";
    private static volatile SyncHeader[] instance = {null,null};
    public static SyncHeader getInstance(int idx) {
        if (instance[idx] == null) {
            synchronized (SyncHeader.class) {
                if (instance[idx] == null) {
                    instance[idx] = new SyncHeader(idx);
                }
            }
        }
        return instance[idx];
    }
    private static int SYNC_HEADER_SIZE = 64*2;
    private ByteBuffer byteBuffer = null;
    private ShortBuffer shortBuffer = null;
    private  static int fpgaCmdCnt = 0;


    private static boolean bCalibrate = false;
    public static synchronized boolean isCalibrate() {
        return bCalibrate;
    }
    public static synchronized void setCalibrate(boolean bCalibrate){
        SyncHeader.bCalibrate = bCalibrate;
    }

    private int fpgaIdx = 0;
    public int getFpgaIdx(){
        return fpgaIdx;
    }
    private int mIdx = 0;
    public synchronized void setIdx(int idx){
        this.mIdx = idx;
    }
    public synchronized int getIdx(){
        return mIdx;
    }
    private SyncHeader(int idx){
        this.fpgaIdx = idx;
        byteBuffer = ByteBuffer.allocateDirect(SYNC_HEADER_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        shortBuffer = byteBuffer.asShortBuffer();
        reset();
    }

    public void reset(){
        shortBuffer.put(0,(short) 0);
        shortBuffer.put(1,(short) 0);
        shortBuffer.put(2,(short) 0);
    }

    public void setByteBuffer(ByteBuffer byteBuffer){
        reset();
        if( byteBuffer != null
                && byteBuffer.capacity() >= SYNC_HEADER_SIZE) {
            Memory.Memcpy(this.byteBuffer,byteBuffer,SYNC_HEADER_SIZE);
        }
    }

    public static synchronized int upSyncHeader(){
        fpgaCmdCnt = ((fpgaCmdCnt+1) & 0x7FFF);
        if(fpgaCmdCnt == 0){
            fpgaCmdCnt++;
        }
        return fpgaCmdCnt;
    }

    public static synchronized int getSyncHeader(){
        return fpgaCmdCnt;
    }

    public boolean isValid(){
        int h = getSyncHeader();
        int val = getHeader();
        return  h == val;
    }
    public int getHeader(){
        return get(0);
    }


    private short get(int idx){
        return shortBuffer.get(idx);
    }
    private boolean isValid(int idx,int bit){
        return ((get(idx) & (1<<bit)) != 0);
    }

    public boolean isBitmapValid(){
        return isValid(1,0);
    }
    public boolean isWaveValid(){
        return isValid(1,1);
    }
    public boolean isSerialValid(){
        return isValid(1,2);
    }
    public boolean isSerialTxtValid(){
        return isValid(1,3);
    }
    public boolean isTrigger(){
        return isValid(1,6);
    }
    public boolean isSample(){
        return isValid(1,7);
    }
    public boolean isRoll(){
        return isValid(1,8);
    }
    public boolean isSlowScale(){
        return isValid(1,10);
    }
    public boolean isOnlyWave(){
        return isValid(1,12);
    }
    public boolean isSerialTxtLast(){
        return isValid(1,13);
    }
    public boolean isLastSample(){return  isValid(1,13);}
    public boolean isSegmentTimestamp(){return isValid(1,14);}




    public int getChNum(){
        int val = get(1);
        val = (((val>>>9) & 0x01)|((val>>>10)&0x2)) & 0x3;
        switch (val){
            case 0:  val = 2; break;
            default:
            case 1:  val = 1; break;
            case 2:  val = 4; break;
        }
        return val;
    }

    public int getWaveLength(){
        int val_L = get(2);
        int val_H = get(6);
        return ((val_H & 0xFFFF)<<16) | (val_L&0xFFFF);
    }

    public int getRollColAddr(){
        return  ((int)get(4) & 0xFFFF);
    }

    public int getSerialLength(int idx){
        return 8 * (int)get(12+idx);
    }

    public int getSerialTxtLength(int idx){
        int len = get(14 + idx);
        len = (len << 16) | get(12 + idx);

        return len;
    }
    public int getChMaxVal(int chIdx){
        return ((get(8 + chIdx) >> 8) & 0xFF) - 128;
    }
    public int getChMinVal(int chIdx){
        return (get(8 + chIdx) & 0xFF) - 128;
    }

    public int getSegmentFrameNums(){
        int l = get(16);
        int h = get(17);
        return  ((h & 0xFFFF) << 16) | (l &0xFFFF);

    }

    public int getSegmentFrameNo(){
        int l = get(18);
        int h = get(19);
        return  ((h & 0xFFFF) << 16) | (l &0xFFFF);

    }

    public long getSegmentTimestamp(){
        long l1 = get(20);
        long l2 = get(21);
        long l3 = get(22);
        long l4 = get(23);

        return (((l4 & 0xFFFFL) << 48) |((l3 & 0xFFFFL) << 32) | ((l2 & 0xFFFFL) << 16) | (l1 &0xFFFFL)) * 8L;
    }
    public int getFrameNo(){
        int l = get(24);
        int h = get(25);
        return  ((h & 0xFFFF) << 16) | (l &0xFFFF);
    }

    public int getChMaxVal16Bit(int chIdx){
        return get(27 + chIdx * 2 ) - 512;
    }
    public int getChMinVal16Bit(int chIdx){
        return get(26 + chIdx * 2) - 512;
    }

    public int getCmdCnt(){
        return get(34) & 0xFFFF | ((get(35) & 0xFFFF) << 16);
    }
}
