package com.micsig.tbook.scope.Data;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by zhuzh on 2018-5-28.
 */

public class BaseDirectBuffer {
    private ByteBuffer byteBuffer;
    public BaseDirectBuffer(int capacity){
        byteBuffer = ByteBuffer.allocateDirect(capacity);
        byteBuffer.order(ByteOrder.nativeOrder());
    }
    protected byte getByteVal(int idx){return byteBuffer.get(idx);}
    protected short getShortVal(int idx){return byteBuffer.getShort(idx);}
    protected int getIntVal(int idx){
        return byteBuffer.getInt(idx);
    }
    protected long getLongVal(int idx){
        return byteBuffer.getLong(idx);
    }
    protected double getFloatVal(int idx){
        return byteBuffer.getFloat(idx);
    }

    protected double getDoubleVal(int idx){
        return byteBuffer.getDouble(idx);
    }
    protected void setVal(int idx,short val){
        byteBuffer.putShort(idx,val);
    }
    protected void setVal(int idx,byte val){
        byteBuffer.put(idx,val);
    }
    protected void setVal(int idx,int val){
        byteBuffer.putInt(idx,val);
    }
    protected void setVal(int idx,long val){
        byteBuffer.putLong(idx,val);
    }
    protected void setVal(int idx,float val){
        byteBuffer.putFloat(idx,val);
    }
    protected void setVal(int idx,double val){
        byteBuffer.putDouble(idx,val);
    }
    protected void setVal(int idx,byte [] bytes){
        for(int i=0;i<bytes.length;i++){
            byteBuffer.put(idx + i,bytes[i]);
        }
    }
    protected byte[] getBytesVal(int idx,int len){
        byte [] bytes = new byte[len];
        for(int i=0;i<len;i++){
            bytes[i] = byteBuffer.get(i+idx);
        }
        return bytes;
    }
    public ByteBuffer getDirectBuffer(){
        return byteBuffer;
    }

    public void clear() {
        byteBuffer.clear();
    }
}
