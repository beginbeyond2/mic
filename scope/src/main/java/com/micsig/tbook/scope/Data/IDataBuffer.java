package com.micsig.tbook.scope.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zhuzh on 2018-4-10.
 */

public interface IDataBuffer {
    ByteBuffer getByteBuffer();
    int obtain();
    int recycle();
    int getRef();
    void setTimestamp(long timestamp);
    long getTimestamp();
    int write(ByteBuffer byteBuffer,int pos,int length);
    int convert16to32(ByteBuffer byteBuffer,int pos,int length,int val);
    int read(ByteBuffer byteBuffer);
    boolean save(String pathName);
    boolean saveCSV(String pathName);
    boolean load(String pathName);
    boolean save(FileChannel fileChannel);
    boolean load(FileChannel fileChannel);

}
