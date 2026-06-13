package com.micsig.tbook.scope.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by zhuzh on 2018-4-10.
 */

public class BufferQueue implements IBufferQueue{
    private static final String TAG = "BufferQueue";
    protected List<IDataBuffer> idleQueue = new ArrayList<IDataBuffer>();
    protected List<IDataBuffer> useQueue = new ArrayList<IDataBuffer>();


    public BufferQueue(){

    }

    private volatile long mTimestamp = 0;
    protected long getTimestamp(){
        long timestamp;
        mTimestamp = (mTimestamp + 1) & 0x7FFFFFFFFFFFFFFFL;
        timestamp = mTimestamp;
        return timestamp;
    }
    public void add(IDataBuffer dataBuffer){
        synchronized (this){
            idleQueue.add(dataBuffer);
        }
    }
    //使用者获取一个最新数据
    public IDataBuffer obtain(){
        IDataBuffer dataBuffer = null;
        synchronized (this) {
            int nums = useQueue.size();
            if (nums > 0) {
                dataBuffer = useQueue.get(nums - 1);
                dataBuffer.obtain();
            }
        }
        return dataBuffer;
    }
    //使用者归还一个使用后数据
    public void recycle(IDataBuffer dataBuffer){
        boolean idle = false;
        synchronized (this) {
            if (dataBuffer.recycle() == 0
                    && dataBuffer.getTimestamp() != mTimestamp) {
                if(useQueue.size() >= 1) {
                    useQueue.remove(dataBuffer);
                    idle = true;
                }
            }
            if (idle) {
                dataBuffer.setTimestamp(0);
                idleQueue.add(dataBuffer);
            }

        }

    }
    //获得一个空闲buffer
    public IDataBuffer dequeue(){
        IDataBuffer dataBuffer = null;
        synchronized (this){
            int nums = idleQueue.size();
            if(nums>0){
                dataBuffer = idleQueue.remove(0);
            }else{
                try{
                    throw new IndexOutOfBoundsException();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return dataBuffer;
    }

    //放入一个数据buffer
    public void enqueue(IDataBuffer dataBuffer){
        synchronized (this){
            dataBuffer.setTimestamp(getTimestamp());
            useQueue.add(dataBuffer);
            if(useQueue.size() > 1) {
                recycle();
            }
        }
    }



    private void recycle(){

        Iterator<IDataBuffer> iter = useQueue.iterator();
        IDataBuffer dataBuffer;
        while (iter.hasNext()) {
            dataBuffer = iter.next();
            if (dataBuffer.getRef() == 0
                    && dataBuffer.getTimestamp() != mTimestamp) {
                iter.remove();
                idleQueue.add(dataBuffer);
            }
        }
    }

}
