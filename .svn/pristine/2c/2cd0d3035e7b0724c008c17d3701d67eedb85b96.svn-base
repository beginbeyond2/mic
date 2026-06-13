package com.micsig.tbook.hardware;

import android.hardware.SpiPort;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhuzh on 2018/3/9.
 */

public abstract class SpiDev {
    private SpiPort mSpiPort=null;

    protected int addr = 0;

    private int speed = 50000000;

    public SpiDev() {

    }


    public boolean isFSpiDev(){
        return false;
    }

    public abstract void  onInitDev(SpiPort spiPort);

    public abstract String getDevName();

    public void setAddr(int addr){
        this.addr = addr;
    }

    public int getAddrNBytes(){
        return 0;
    }
    protected void initDev(SpiPort spiPort,int mode,int bits,int speed,int lsbfirs,int size){
        mSpiPort = spiPort;
        if(mSpiPort != null) {
            try {
                mSpiPort.setLsb(lsbfirs);
                mSpiPort.setSpeed(speed);
                mSpiPort.setMode(mode);
                mSpiPort.setBits(bits);
                if (size > 0)
                    mSpiPort.setBufSize(size);
                this.speed = speed;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSpeed(int speed){
        if(mSpiPort != null) {
            try {
                mSpiPort.setSpeed(speed);
                this.speed = speed;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getSpeed(){
        return this.speed;
    }

    public int read(ByteBuffer wbyteBuffer,int length,ByteBuffer byteBuffer){
        int r = 0;
        if(mSpiPort != null) {
            try {
                r = mSpiPort.read(wbyteBuffer, length, byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public void write(ByteBuffer byteBuffer ,int length){
        if(mSpiPort != null) {
            try {
                mSpiPort.write(byteBuffer, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
