package com.micsig.tbook.ui;

import android.content.Context;
import android.view.MotionEvent;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.GLView;

public class MSerialTxtView extends GLView {
    public static final int SerialTxtView_UART=1;
    public static final int SerialTxtView_LIN=2;
    public static final int SerialTxtView_CAN=3;
    public static final int SerialTxtView_SPI=4;
    public static final int SerialTxtView_I2C=5;
    public static final int SerialTxtView_429=6;
    public static final int SerialTxtView_1553b=7;

    private int currShowFlag=1;

    public MSerialTxtView(Context context) {
        super(context);
    }

    public void refreshUart(){
        currShowFlag=SerialTxtView_UART;
        requestRender();
    }

    public void refreshLin(){
        currShowFlag=SerialTxtView_LIN;
        requestRender();
    }

    public void refreshCan(){
        currShowFlag=SerialTxtView_CAN;
        requestRender();
    }

    public void refreshSpi(){
        currShowFlag=SerialTxtView_SPI;
        requestRender();
    }
    public void refreshI2c(){
        currShowFlag=SerialTxtView_I2C;
        requestRender();
    }

    public void refresh429(){
        currShowFlag=SerialTxtView_429;
        requestRender();
    }
    public void refresh1553b(){
        currShowFlag=SerialTxtView_1553b;
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas) {

    }
}
