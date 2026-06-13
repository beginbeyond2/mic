package com.micsig.tbook.scope.Data;

import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class SaveMultiChannelWave {

    private String pathName;
    public SaveMultiChannelWave(String pathName){
        this.pathName = pathName;
    }

    public static int getChannelNum(String pathName){
        int chNum = 0;
        File file = new File(pathName);
        if(file.exists()){
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.nativeOrder());
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                FileChannel fileChannel = is.getChannel();
                fileChannel.read(byteBuffer,WaveData.VERSION_IDX);
                int ver = byteBuffer.getInt(0);byteBuffer.clear();
                fileChannel.read(byteBuffer,WaveData.CH_NUM_IDX);
                int n = byteBuffer.getInt(0);byteBuffer.clear();
                fileChannel.close();
                if(ver == WaveData.VERSION_3){
                    chNum = n;
                }else if(ver == WaveData.VERSION){
                    chNum = 1;
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return chNum;
    }
    public static boolean isMultiChannel(String pathName){
        return getChannelNum(pathName) > 1;
    }

    private long getFileUseSzie(){

        return (long) ChannelFactory.getDynamicChannelOpenCount() * WaveData.BUFFER_SIZE;
    }
    public boolean save(){
        File file = new File(pathName);
        if (file.exists()) {
            file.delete();
        }
        if (Utils.isDiskAvaiable(file, getFileUseSzie())) {

            try {
                if (file.createNewFile()) {
                    int idx = 0;
                    Channel channel = null;
                    FileOutputStream os = new FileOutputStream(file);
                    FileChannel fileChannel = os.getChannel();
                    int n = ChannelFactory.getDynamicChannelOpenCount();
                    int maxIdx = ChannelFactory.getMaxChIdx();
                    for(int i=ChannelFactory.CH1;i<maxIdx;i++){
                        if(ChannelFactory.isChOpen(i)){
                            channel = ChannelFactory.getDynamicChannel(i);
                            if(channel != null){
                                WaveData waveData  = (WaveData) channel.obtain();
                                if(waveData != null) {
                                    int ver = waveData.getVersion();
                                    waveData.setVersion(WaveData.VERSION_3);
                                    waveData.setChNum(n);
                                    waveData.setIdx(idx);
                                    fileChannel.position((long) idx * WaveData.BUFFER_SIZE);
                                    waveData.save(fileChannel);
                                    waveData.setChNum(1);
                                    waveData.setIdx(0);
                                    waveData.setVersion(ver);
                                    waveData.recycle();
                                    idx++;
                                }
                            }
                        }
                    }
                    os.flush();
                    fileChannel.close();
                    os.close();
                    Memory.Sync();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean load(int [] arrayChIdx){
        File file = new File(pathName);
        if(file.exists()){
            FileInputStream is = null;
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.nativeOrder());
            try {
                is = new FileInputStream(file);
                FileChannel fileChannel = is.getChannel();
                fileChannel.read(byteBuffer,WaveData.CH_NUM_IDX);
                int n = byteBuffer.getInt(0);
                n = Math.min(n,arrayChIdx.length);
                RefChannel refChannel;
                for(int i=0;i<n;i++){
                    refChannel = ChannelFactory.getRefChannel(arrayChIdx[i]);
                    if(refChannel!= null){
                        fileChannel.position((long) i * WaveData.BUFFER_SIZE);
                        refChannel.loadWave(fileChannel);
                    }
                }
                fileChannel.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }


}
