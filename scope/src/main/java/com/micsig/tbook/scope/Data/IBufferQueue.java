package com.micsig.tbook.scope.Data;

/**
 * Created by zhuzh on 2018-4-11.
 */

public interface IBufferQueue {
    IDataBuffer obtain();
    void recycle(IDataBuffer dataBuffer);
    IDataBuffer dequeue();
    void enqueue(IDataBuffer dataBuffer);
}
