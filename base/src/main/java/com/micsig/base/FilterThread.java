package com.micsig.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by zhuzh on 2018-4-13.
 */

public class FilterThread {

    private static final int MSG_RUN = 0x1001;
    private static final int MSG_TIMER_RUN = 0x1002;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Runnable mRunable;
    private volatile int mDelayMillis = 0;
    private volatile boolean mDelayRun = false;

    private boolean bBeforeRun = true;


    public FilterThread(String threadName,Runnable runnable,int delayMillis){
        mDelayMillis = delayMillis;
        mRunable = runnable;
        mHandlerThread = new HandlerThread(threadName);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                FilterThread.this.handleMessage(msg);
            }
        };
    }
    public FilterThread(String threadName){
        this(threadName,null,0);
    }
    public boolean isSelfThread(){
        return (Thread.currentThread() == mHandlerThread);
    }
    protected void handleMessage(Message msg){
        switch (msg.what){
            case MSG_RUN:
                msgRun();
                break;
            case MSG_TIMER_RUN:
                timerRun();
                break;
        }
    }
    private void timerRun(){

        boolean bRun = false;
        boolean br = true;
        synchronized (this){
            bRun = mDelayRun;
            mDelayRun = false;
            br = bBeforeRun;
            if(br) {
                if (!mHandler.hasMessages(MSG_TIMER_RUN)) {
                    mHandler.sendEmptyMessageDelayed(MSG_TIMER_RUN, mDelayMillis);
                }
            }
        }
        if(bRun) {
            if (mRunable != null) {
                mRunable.run();
            }
        }
        if(!br){
            if (!mHandler.hasMessages(MSG_TIMER_RUN)) {
                mHandler.sendEmptyMessageDelayed(MSG_TIMER_RUN, mDelayMillis);
            }
        }
    }

    public void setBeforeRun(boolean bBeforeRun) {
        synchronized (this) {
            this.bBeforeRun = bBeforeRun;
        }
    }

    private void msgRun(){
        if(mRunable != null){
            mRunable.run();
        }
    }

    public void setRunnable(Runnable runnable){
        mRunable = runnable;
    }

    public long getDelayMillis(){
        synchronized (this){
            return mDelayMillis;
        }
    }
    public void setDelayMillis(int delayMillis){
        synchronized (this){
            mDelayMillis = delayMillis;
            if(mHandler.hasMessages(MSG_TIMER_RUN)) {
                mHandler.removeMessages(MSG_TIMER_RUN);
            }
            mHandler.sendEmptyMessageDelayed(MSG_TIMER_RUN,mDelayMillis);
        }
    }

    public void run(){
        synchronized (this){
            if(mDelayMillis > 1){
                mDelayRun = true;
            }else{
                if(!mHandler.hasMessages(MSG_RUN)){
                    mHandler.sendEmptyMessage(MSG_RUN);
                }
            }
        }
    }
    public void run(Runnable runnable){
        mHandler.post(runnable);
    }
}
