package com.micsig.tbook.scope.Data;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.mem.Memory;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class SerialData extends DataBuffer {

    public static final int SERIAL_GRAPHICAL = 0;
    public static final int SERIAL_TXT = 1;
    private int codeNum = 0;
    private long timePrePix = 1;
    private int startX = 0;
    private int endX = 0;
    private int chIdx = 0;
    private int busType = 0;
    private int serialType = SERIAL_GRAPHICAL;
    private boolean bLastData = false;
    private LongBuffer longBuffer = null;
    private ShortBuffer shortBuffer = null;
    private static final int MAX_BUF_SIZE = 1024 * 1024;
    public SerialData(){
        super(MAX_BUF_SIZE);
        longBuffer = getByteBuffer().asLongBuffer();
        shortBuffer = getByteBuffer().asShortBuffer();
    }

    public int getCodeNum() {
        return codeNum;
    }

    public void setCodeNum(int codeNum) {
        this.codeNum = codeNum;
    }

    public synchronized long getTimePrePix() {
        return timePrePix;
    }

    public synchronized void setTimePrePix(long timePrePix) {
        this.timePrePix = timePrePix;
    }

    public synchronized int getStartX() {
        return startX;
    }

    public synchronized void setStartX(int startX) {
        this.startX = startX;
    }

    public synchronized int getEndX() {
        return endX;
    }

    public synchronized void setEndX(int endX) {
        this.endX = endX;
    }

    public int getChIdx() {
        return chIdx;
    }

    public void setChIdx(int chIdx) {
        this.chIdx = chIdx;
    }

    public int getBusType() {
        return busType;
    }

    public void setBusType(int busType) {
        this.busType = busType;
    }

    public int getSerialType() {
        return serialType;
    }

    public void setSerialType(int serialType) {
        this.serialType = serialType;
    }

    public boolean isLastData() {
        return bLastData;
    }

    public void setLastData(boolean bLastData) {
        this.bLastData = bLastData;
    }

    //获取1个BusCode码字
    public BusCode getBusCode(int idx){
        BusCode busCode = new BusCode(0,0,0);
        getBusCode(idx,busCode);
        return busCode;
    }
    public BusCode getTxtBusCode(int idx){
        short val = shortBuffer.get(idx);
        return new BusCode(0,(int)((val>>>8) & 0xFF),(int)((val) & 0xFF));
    }

    public long getBusCode(int idx,BusCode busCode){
        long val = 0;
        if(serialType == SERIAL_GRAPHICAL) {
            val = longBuffer.get(idx);
            if (busCode != null) {
                busCode.setX(getX(val >>> 32));
                busCode.setType((int) ((val >>> 24) & 0xFF));
                busCode.setData((int) ((val >>> 16) & 0xFF));
            }
        }else if(serialType == SERIAL_TXT){
            val = getTxtBusCode(idx,busCode);
        }
        return val;
    }
    private short getTxtBusCode(int idx,BusCode busCode){
        short val = shortBuffer.get(idx);
        if(busCode != null){
            busCode.setX(0);
            busCode.setType((int)((val>>>8) & 0xFF));
            busCode.setData((int)((val) & 0xFF));
        }
        return val;
    }

    //获得64bit Code
    public long getCode(int idx){
        if(serialType == SERIAL_GRAPHICAL) {
            return longBuffer.get(idx);
        }else if(serialType == SERIAL_TXT){
            return shortBuffer.get(idx);
        }else
            return 0;
    }


    //时间转像素
    public long time2Pix(long x){
        return (long)( 4e4 * x /getTimePrePix());
    }

    //获得x屏幕坐标
    public int getX(long x){
        return (int)(getStartX() + time2Pix(x));
    }

    //test begin
    private void testBus(){
        String TAG = "BUS";
        BusCode busCode;
        int codeNum = getCodeNum();
        Logger.d(TAG,"start busType = "+ getBusType() +",codeNum = " + codeNum);
        for(int i=0;i<codeNum;i++){
            busCode = getBusCode(i);
            Logger.d(TAG,"x = " + busCode.getX() + ",type = " + Integer.toHexString(busCode.getType()) + ",data = "+ Integer.toHexString(busCode.getData()));
        }
        Logger.d(TAG,"end");
    }
    private static long currTotalTime = 0;
    private static long xcode = 0;
    private void testTxtBus(){
        String TAG = "BUS";
        short busCode;
        int codeNum = getCodeNum();
        long currTime = 0;
        //Logger.d(TAG,"start busType = "+ getBusType() +",codeNum = " + codeNum + ",chIdx = " + getChIdx());
        for(int i=0;i<codeNum;i++){
            busCode = getTxtBusCode(i,null);
            if((busCode & 0x8000) != 0){
                busCode &= 0x7FFF;
                //Logger.d(TAG,"busCode = " + busCode + ",xcode = " + xcode);
                currTime = (((32768 + busCode) - (currTotalTime % 0x7FFF)) % 0x7FFF) + currTotalTime;
                if(currTime - currTotalTime > 2000)
                {
                    Logger.d(TAG,"xcode = " + xcode + ",busCode = " + busCode);
                }
                //Logger.d(TAG,"currTotalTime : " + currTotalTime + ",busCode : " + busCode + ",currTime :" + currTime);
                currTotalTime = currTime;
                xcode = busCode;
            }
        }
        //Logger.d(TAG,"end");
    }
    //test end

    @Override
    public int write(ByteBuffer byteBuffer, int pos, int length) {

        if(length > 0 && length < MAX_BUF_SIZE) {
            Memory.Memcpy(getByteBuffer(), 0, byteBuffer, pos, length);

            if (serialType == SERIAL_GRAPHICAL) {
                setCodeNum(length / 8);
            } else if (serialType == SERIAL_TXT) {
                setCodeNum(length / 2);
            }
            getByteBuffer().clear();
            getByteBuffer().limit(length);

//            testBus();
//            testTxtBus();
        }else{
            getByteBuffer().clear();
            getByteBuffer().limit(0);
        }


        return length;
    }

    @Override
    public int convert16to32(ByteBuffer byteBuffer, int pos, int length, int val) {
        return 0;
    }

    @Override
    public int read(ByteBuffer byteBuffer) {

        int lenght = getCodeNum();
        if(serialType == SERIAL_GRAPHICAL)
            lenght *= 8;
        else if(serialType == SERIAL_TXT)
            lenght *= 2;
        else
            lenght = 0;
        if(byteBuffer.capacity() >= lenght) {
            byteBuffer.clear();
            Memory.Memcpy(byteBuffer,  getByteBuffer(),  lenght);
            return lenght;
        }
        return 0;
    }

    @Override
    public boolean saveCSV(String pathName) {
        return false;
    }

    @Override
    protected boolean verification() {
        return false;
    }
    class BusCode{
        //事件
        private int x;
        private int type;
        private int data;

        public BusCode(int x,int type,int data){
            this.x = x;
            this.type = type;
            this.data = data;
        }

        //屏幕x轴坐标
        public int getX() {
            return x;
        }
        //码字类型
        public int getType() {
            return type;
        }
        //码字数据
        public int getData() {
            return data;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
}
