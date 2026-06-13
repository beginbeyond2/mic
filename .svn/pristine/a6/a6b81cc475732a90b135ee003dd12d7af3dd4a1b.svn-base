package com.micsig.tbook.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.DeviceManager;
import android.hardware.SpiManager;
import android.hardware.SpiPort;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.IntDef;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class SpiDevManager {

    private  SpiManager mSpiManager;
    private  Context mContext;

    @IntDef({ SPI_DEV_FPGA_BOOT,SPI_DEV_FPGA1_CMD,SPI_DEV_FPGA2_CMD,SPI_DEV_CLK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpiDevType {}


    public final static int SPI_DEV_FPGA_BOOT = 0;
    public final static int SPI_DEV_FPGA1_CMD = 1;
    public final static int SPI_DEV_FPGA2_CMD = 2;
    public final static int SPI_DEV_CLK = 3;
    public final static int SPI_DEV_MAX = 4;

    private final static int SPI_CPHA = 0x01;
    private final static int SPI_CPOL = 0x02;

    private final static int SPI_MODE_0 = 		(0|0);
    private final static int SPI_MODE_1	=	(0|SPI_CPHA);
    private final static int SPI_MODE_2	=	(SPI_CPOL|0);
    private final static int SPI_MODE_3	=	(SPI_CPOL|SPI_CPHA);


    private SpiDev mSpiDev[] = new SpiDev[SPI_DEV_MAX];

    private static volatile SpiDevManager instance = null;

    public static SpiDevManager getInstance(){
        return instance;
    }
    public static SpiDevManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SpiDevManager.class) {
                if (instance == null && context != null) {
                    instance = new SpiDevManager(context);
                }
            }
        }
        return instance;
    }

    @SuppressLint("WrongConstant")
    private SpiDevManager(Context context){
        mContext = context;
        mSpiManager = (SpiManager)mContext.getSystemService(HwServiceName.SPI_SERVICE);

        init();
    }

    private void init(){
        SpiPort spiPort = null;
        if(HardwareProduct.isFSpiBoot()){
            mSpiDev[SPI_DEV_FPGA_BOOT] = new FPGA_BOOT_FSpiDev(mContext);
        }else {
            mSpiDev[SPI_DEV_FPGA_BOOT] = new FPGA_BOOT_SpiDev();
        }
        mSpiDev[SPI_DEV_FPGA1_CMD] = new FPGA_CMD_SpiDev();

        mSpiDev[SPI_DEV_FPGA2_CMD] = new FPGA2_CMD_SpiDev();
        mSpiDev[SPI_DEV_CLK] = new NullSpiDev();

        for(int i=0;i<SPI_DEV_MAX;i++){
            if(mSpiDev[i] != null
                    && !mSpiDev[i].isFSpiDev()) {
                try {
                    Log.d("zhuzh","spi dev name:" + mSpiDev[i].getDevName());
                    String devName = mSpiDev[i].getDevName();
                    if(devName != null && devName.length() > 0) {
                        spiPort = mSpiManager.openSpiPort(devName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(spiPort != null) {
                    mSpiDev[i].onInitDev(spiPort);
                }
            }
        }
    }

    public SpiDev getSpiDev(@SpiDevType int idx ){

        if(idx>=0 && idx <SPI_DEV_MAX) {
            return mSpiDev[idx];
        }
        return null;
    }


    private class NullSpiDev extends SpiDev{
        @Override
        public void onInitDev(SpiPort spiPort) {

        }

        @Override
        public String getDevName() {
            return null;
        }

    }

    private class CLK_SpiDev extends SpiDev{


        @Override
        public void onInitDev(SpiPort spiPort) {
            initDev(spiPort,SPI_MODE_0,8,20*1000*1000,0,0);
        }

        @Override
        public String getDevName() {
            return "/dev/spidev3.0";
        }
    }

    private class FPGA_BOOT_FSpiDev extends SpiDev{
        ParcelFileDescriptor parcelFileDescriptor;
        FileOutputStream fileOutputStream;
        FileChannel fileChannel;


        public FPGA_BOOT_FSpiDev(Context context) {
            super();
            DeviceManager deviceManager = (DeviceManager)context.getSystemService("device");
            try {
                parcelFileDescriptor = deviceManager.openDevicePort(getDevName());
                if(parcelFileDescriptor != null) {
                    fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                    fileChannel = fileOutputStream.getChannel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isFSpiDev() {
            return true;
        }

        @Override
        public void onInitDev(SpiPort spiPort) {

        }

        @Override
        public String getDevName() {
            return "/dev/fspidev5.0";
        }



        @Override
        public int read(ByteBuffer wbyteBuffer, int length, ByteBuffer byteBuffer) {
            return 0;
        }

        @Override
        public void write(ByteBuffer byteBuffer, int length) {

            if(fileChannel != null){
                byteBuffer.put(0,(byte) ((addr >>> 16) & 0xFF));
                byteBuffer.put(1,(byte) ((addr >>> 8) & 0xFF));
                byteBuffer.put(2,(byte) ((addr) & 0xFF));
                byteBuffer.position(0);
                byteBuffer.limit(length);

                try {
                    fileChannel.write(byteBuffer);
                    fileOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getAddrNBytes() {
            return 3;
        }
    }

    private class FPGA_BOOT_SpiDev extends SpiDev{

        @Override
        public void onInitDev(SpiPort spiPort) {
            initDev(spiPort,SPI_MODE_0,8,50*1000*1000,0,0);
        }

        @Override
        public String getDevName() {
            if(HardwareProduct.isMHO28V1()){
                return "/dev/spidev0.2";
            }else {
                return "/dev/spidev0.0";
            }
        }
    }

    private class FPGA_CMD_SpiDev extends SpiDev {

        @Override
        public void onInitDev(SpiPort spiPort) {
            initDev(spiPort,SPI_MODE_3,8,10*1000*1000,0,0);
        }

        @Override
        public String getDevName() {
            return "/dev/spidev0.0";
        }
    }

    private class FPGA2_CMD_SpiDev extends SpiDev {

        @Override
        public void onInitDev(SpiPort spiPort) {
            initDev(spiPort,SPI_MODE_3,8,10*1000*1000,0,0);
        }

        @Override
        public String getDevName() {
            return "/dev/spidev0.1";
        }
    }

}
