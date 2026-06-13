package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import java.nio.ByteBuffer;

public class SerialImageBuffer {
    private final Object lock=new Object();
    private String key;
    private ByteBuffer bytes;
    private long timeToPix;
    private int startX;
    private int endX;
    private boolean deal;
    /** 正在处理 */
    private boolean doing;
    public SerialImageBuffer(String key,ByteBuffer bytes,long timeToPix,int startX,int endX){
        this.key=key;
        this.bytes=bytes;
        this.timeToPix=timeToPix;
        this.startX=startX;
        this.endX=endX;
        this.deal=true;
        this.doing=false;
    }

    public String getKey() {
        return key;
    }

    public void setTimeToPix(long timeToPix) {
        this.timeToPix = timeToPix;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public void setBytes(ByteBuffer bytes){
        this.bytes=bytes;
    }
    public ByteBuffer getBytes() {
        return bytes;
    }

    public long getTimeToPix() {
        return timeToPix;
    }

    public int getStartX() {
        return startX;
    }

    public int getEndX() {
        return endX;
    }

    /**
     * 是否已经处理过数据
     * @return true:已处理  false:未处理
     */
    public boolean isDeal() {
        return this.deal;
    }

    /**
     * 设置处理状态：设置成已处理
     *
     */
    public void setDeal(boolean deal) {
//        synchronized (lock) {
            this.deal = deal;
//        }
    }

    public boolean isDoing() {
        synchronized (lock) {
            return doing;
        }
    }

    public void setDoing(boolean doing) {
        synchronized (lock) {
            this.doing = doing;
        }
    }

    public Object Lock(){
        return lock;
    }

    @Override
    public String toString() {
        return
            " key:"+ key+
           "  bytes:"+ bytes+
           "  timeToPix:"+timeToPix+
           "  startX:"+startX+
           "  endX:"+endX+
           "  deal:"+deal;
    }
}
