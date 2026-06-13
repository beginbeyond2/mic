package com.micsig.tbook.scope.Data;

/**
 * Created by zhuzh on 2018-5-25.
 */

public class SingleBuffer extends BufferQueue {
    @Override
    public void add(IDataBuffer dataBuffer){
        synchronized (this){
            if(useQueue.size() < 1) {
                useQueue.add(dataBuffer);
            }
        }
    }
    //使用者获取一个最新数据
    @Override
    public IDataBuffer obtain(){
        IDataBuffer dataBuffer = null;
        synchronized (this) {
            int nums = useQueue.size();
            if (nums > 0) {
                dataBuffer = useQueue.get(0);
                dataBuffer.obtain();
            }
        }
        return dataBuffer;
    }
    //使用者归还一个使用后数据
    public void recycle(IDataBuffer dataBuffer){

        synchronized (this) {
            dataBuffer.recycle();
        }

    }
    //获得一个空闲buffer
    public IDataBuffer dequeue(){
        IDataBuffer dataBuffer = null;
        synchronized (this){
            int nums = useQueue.size();
            if(nums > 0){
                dataBuffer = useQueue.get(0);
            }
        }
        return dataBuffer;
    }

    //放入一个数据buffer
    public void enqueue(IDataBuffer dataBuffer){
        synchronized (this){
            dataBuffer.setTimestamp(getTimestamp());
        }
    }


}
