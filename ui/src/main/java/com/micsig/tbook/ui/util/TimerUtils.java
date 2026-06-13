package com.micsig.tbook.ui.util;

/**
 * @auother Liwb
 * @description: 定时器，用法类似与C#中的timer
 * @data:2022-2-23 13:10
 */
public class TimerUtils {
    private static final String TAG = TimerUtils.class.getSimpleName();


    public interface TimeOutEvent {
        public void onTimeOut();
    }


    private Thread thread;
    private volatile long beginTime;
    private boolean isRun = false;

    //region 属性
    private int intervalMs = 1000;
    private boolean execOne = true;
    private TimeOutEvent event = null;

    public int getIntervalMs() {
        return intervalMs;
    }

    /**
     * 间隔时间执行
     *
     * @param intervalMs
     */
    public void setIntervalMs(int intervalMs) {
        this.intervalMs = intervalMs;
    }

    public boolean isExecOne() {
        return execOne;
    }

    /**
     * 是否直接一次就结束
     *
     * @param execOne
     */
    public void setExecOne(boolean execOne) {
        this.execOne = execOne;
    }

    public TimeOutEvent getEvent() {
        return event;
    }

    /**
     * 事件
     *
     * @param event
     */
    public void setEvent(TimeOutEvent event) {
        this.event = event;
    }
    //endregion

    public TimerUtils(TimeOutEvent event) {
        this.event = event;
    }

    class DoWork implements Runnable {
        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    long curTime = System.currentTimeMillis();
                    if (curTime - beginTime >= intervalMs) {
                        beginTime = curTime;
                        if (event != null) {
                            event.onTimeOut();
                        }
                        if (execOne == true) {
                            return;
                        }
                    }

                    Thread.sleep(10);
                }
            } catch (Exception e) {
                return;
            }
        }
    }


    public void start() {
        try {
            isRun = true;
            beginTime = System.currentTimeMillis();
            if (thread == null || thread.isAlive() == false) {
                thread = new Thread(new DoWork());
            }

            thread.start();
        } catch (Exception e) {
            return;
        }
    }

    public void stop() {
        isRun = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }


}
