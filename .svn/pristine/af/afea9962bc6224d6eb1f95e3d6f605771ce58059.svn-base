package com.micsig.tbook.tbookscope.wavezone.measure;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


/**
 * Created by liwb on 2017/11/6.
 */

public class CursorMeasure_XY extends CurSorMeasureBase {

    private PorterDuffXfermode clearMode=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode=new PorterDuffXfermode(PorterDuff.Mode.SRC);
    @Override
    protected void draw() {
        //super.draw();
        synchronized (bmp) {
            p.setXfermode(clearMode);
            mCanvas.drawPaint(p);
            p.setXfermode(srcMode);
            p.setColor(Color.rgb(200,200,200));
            if (rowCursorVisible && colCursorVisible) {
                int y = drawRow();
                y = drawCol(y);
//                y=drawDeltaX(y);
//                drawS(y);

            } else if (rowCursorVisible) {
                drawRow();
            } else if (colCursorVisible) {
                int y=drawCol(0);
//                drawDeltaX(y);
            }
            isChanageBitmap = true;
            onRefresh();
        }
    }


}
