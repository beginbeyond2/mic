package com.micsig.tbook.ui.util;

/**
 * Created by yangj on 2017/9/13.
 */

public class DelayedSend {
    private boolean loop = true;
    private boolean send = false;
    private Thread thread;

    public DelayedSend(final DelayedSendTo delayedSendTo) {
        this(100, delayedSendTo);
    }

    public DelayedSend(final int delay, final DelayedSendTo delayedSendTo) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("DelaySend");
                while (loop) {
                    if (send) {
                        send = false;
                        delayedSendTo.delayedSendMessage();
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void delayedSend() {
        send = true;
    }

    public void closeThread() {
        loop = false;
    }

    public interface DelayedSendTo {
        /**
         * 此方法工作在非UI线程
         */
        void delayedSendMessage();
    }
}
