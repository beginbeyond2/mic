package com.micsig.tbook.scope.Data;

import android.util.Log;

import com.micsig.base.Utils;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zhuzh on 2018-4-11.
 */

public abstract class DataBuffer extends BaseDirectBuffer implements IDataBuffer {


    private int ref = 0;
    private long mTimestamp = 0;

    public DataBuffer(int size){
        super(size);
    }

    @Override
    public int obtain() {
        ref++;
        return ref;
    }

    @Override
    public int recycle() {
        if(ref>0)
            ref--;
        return ref;
    }

    @Override
    public int getRef() {
        return ref;
    }

    @Override
    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }
    @Override
    public ByteBuffer getByteBuffer(){
        return getDirectBuffer();
    }

    @Override
    public boolean save(String pathName) {
        File file = new File(pathName);
        if(file.exists()) {
            file.delete();
        }
        if(Utils.isDiskAvaiable(file, 10*1024*1024)) {
            FileDescriptor fd = null;
            FileOutputStream os = null;
            FileChannel fileChannel = null;
            try {
                if (file.createNewFile()) {

                    ByteBuffer byteBuffer = getDirectBuffer();
                    os = new FileOutputStream(file);
                    fd = os.getFD();
                    byteBuffer.clear();
                    fileChannel = os.getChannel();
                    fileChannel.write(byteBuffer);
                    byteBuffer.clear();
                    os.flush();
                    fd.sync();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileChannel != null ){
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(os != null){
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Memory.Sync();

            }
            return true;
        }

        return false;
    }

    @Override
    public boolean save(FileChannel fileChannel) {
        if(fileChannel != null) {
            try {
                ByteBuffer byteBuffer = getDirectBuffer();
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected abstract boolean verification();
    @Override
    public boolean load(String pathName) {
        File file = new File(pathName);
        ByteBuffer byteBuffer = getDirectBuffer();
        if(file.exists() && file.length() > 0 && file.length() <= byteBuffer.capacity()){
            try {

                FileInputStream is = new FileInputStream(file);
                FileChannel fileChannel = is.getChannel();
                byteBuffer.clear();
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                fileChannel.close();
                is.close();
                return verification();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean load(FileChannel fileChannel) {
        ByteBuffer byteBuffer = getDirectBuffer();
        if(fileChannel != null){
            try {
                byteBuffer.clear();
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                return verification();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
