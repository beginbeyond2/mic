package com.micsig.tbook.tbookscope.services.ExternalKeys.client;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

/**
 * Created by liwb on 2017/12/6.
 * 测试模拟点击
 */

public class ExternalKeysSimulateClick {
    private boolean imitateClick = false;

    public void setImitateClick(boolean imitateClick) {
        this.imitateClick = imitateClick;
    }

    public boolean isImitateClick() {
        return imitateClick;
    }

    public static void click() {
        int x = 101;
        int y = 101;

        String[] order = {
                "input",
                "tap",
                "" + x,
                "" + y
        };
        try {
            new ProcessBuilder(order).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void click(View view, float x, float y) {

        long downTime = SystemClock.uptimeMillis();

        //装疯
        MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;

        //卖傻
        MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);

//        view.onTouchEvent(downEvent);
//        view.onTouchEvent(upEvent);
//        view.dispatchTouchEvent(downEvent);
//        view.dispatchTouchEvent(upEvent);
        view.dispatchGenericMotionEvent(downEvent);
        view.dispatchGenericMotionEvent(upEvent);

        downEvent.recycle();
        upEvent.recycle();
    }


    /**
     * 使用这个方法要加权限
     * <uses-permission android:name="android.permission.INJECT_EVENTS"/>
     */
    Instrumentation inst = new Instrumentation();

    public void click(float x, float y, INodeControlOperation operation) {
        RClick r = new RClick(x, y, operation);
        Thread thread = new Thread(r);
        thread.start();
    }

    public void drag(float x, float y, float x1, float y1) {
        RDrag rd = new RDrag(x, y, x1, y1);
        Thread thread = new Thread(rd);
        thread.start();
    }

    public void drag_downMove(float x, float y, float x1, float y1) {
        RDrag_DownMove rd = new RDrag_DownMove(x, y, x1, y1);
        Thread thread = new Thread(rd);
        thread.start();
    }

    public void drag_up(float x, float y, float x1, float y1) {
        RDrag_Up rd = new RDrag_Up(x, y, x1, y1);
        Thread thread = new Thread(rd);
        thread.start();
    }

    class RDrag implements Runnable {
        private float x, y;
        private float x1, y1;

        public RDrag(float x, float y, float x1, float y1) {
            this.x = x;
            this.y = y;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0));
            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x1, y1, 0));
            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 0));
        }
    }

    class RDrag_DownMove implements Runnable {
        private float x, y;
        private float x1, y1;

        public RDrag_DownMove(float x, float y, float x1, float y1) {
            this.x = x;
            this.y = y;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 1));
            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x1, y1, 1));
            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 1));
        }
    }

    class RDrag_Up implements Runnable {
        private float x, y;
        private float x1, y1;

        public RDrag_Up(float x, float y, float x1, float y1) {
            this.x = x;
            this.y = y;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
//            inst.sendPointerSync(MotionEvent.obtain(downTime,eventTime, MotionEvent.ACTION_DOWN, x, y, 0));
//            eventTime = SystemClock.uptimeMillis();
//            inst.sendPointerSync(MotionEvent.obtain(downTime,eventTime, MotionEvent.ACTION_MOVE, x1, y1, 0));
//            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 0));
        }
    }


    class RClick implements Runnable {
        private float x, y;
        private INodeControlOperation operation;

        public RClick(float x, float y, INodeControlOperation operation) {
            this.x = x;
            this.y = y;
            this.operation = operation;
        }

        @Override
        public void run() {
            imitateClick = false;
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 1));
            imitateClick = true;
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 1));
            imitateClick = true;
            if (operation != null) operation.simulateMotionEventActionUp();
        }
    }


    public interface INodeControlOperation {
        void simulateMotionEventActionUp();
    }
}
