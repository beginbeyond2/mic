package com.micsig.tbook.tbookscope.wavezone.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage;

/**
 * Created by liwb on 2017/5/3.
 * 目前已不使用。使用WaveZoneDisplay_YT
 */

public class WaveDisplay_YT extends SurfaceView implements SurfaceHolder.Callback {

    //region 私有变量
    private CanvasWaveThread canvasWaveThread;
    private SurfaceHolder sfh;
    private Canvas canvas;
    private boolean ThreadEof_Flag = true;

    //endregion

    public WaveDisplay_YT(Context context) {
        super(context);
        sfh = this.getHolder();
        sfh.addCallback(this);

        canvasWaveThread = new CanvasWaveThread();


        //响应事件
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        canvasWaveThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class CanvasWaveThread extends Thread {
        public CanvasWaveThread() {

        }

        @Override
        public void run() {

            while (ThreadEof_Flag) {
                canvas = sfh.lockCanvas();
                canvas.drawColor(Color.BLACK);
                //canvas.drawBitmap(bmp, 0, 0, new Paint());
                //WaveGridManage.get().drawGrid(canvas);
                //CursorManage_YT.get().draw(canvas);
                MeasureManage.getInstance().draw(canvas);
                VoltageLineManage.getInstance().draw(canvas);
                TriggerTimebase.getInstance().draw(canvas);
                //WaveManage.get().draw(canvas);
                try {
                    if (sfh != null) {
                        sfh.unlockCanvasAndPost(canvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    int index = -1;
    int oldX, oldY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                //用来做偏移值
//                oldX = (int) event.getX();
//                oldY = (int) event.getY();
//                index = CursorManage.getInstance().selectCursor(x, y);
//                if (index >= 0) {
//                    CursorManage.getInstance().setSelectCursor(index);
//                } else if ((index = WaveManage.get().selectCursor(x, y)) >= 0) {
//                    System.out.println("通道号：" + index);
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (index >= 0) CursorManage.getInstance().moveSelectCursor(x, y);
//                else { //暂时  光标没有被选择，就操作触发时刻
//                    int offsetX = oldX - (int) event.getX();
//                    int offsetY = oldY - (int) event.getY();
//                    TriggerTimebase.getInstance().setOffsetX(offsetX);
//                    WaveManage.get().setOffsetY(offsetY);
//                    System.out.println("offsetX:" + offsetX);
//                    oldX = (int) event.getX();
//                    oldY = (int) event.getY();
//                    //TriggerTimebase.get().setX(x);
//
//                }
//                break;
//        }
        return true;
        //return super.onTouchEvent(event);
    }
}
