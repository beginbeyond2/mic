package com.micsig.tbook.tbookscope.wavezone.measure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasureCommon;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2017/11/6.
 */
public class CurSorMeasureBase implements MeasureManage.IMeasure, IWorkMode {

    /***
     * 光标参数结构
     */
    public class CursorMeasureStruct {
        public String row1;
        public String row2;
        public String detaRow;

        public String col1;
        public String col2;
        public String detaCol;
        public String detaTCol;

        public String S;
    }


    //region  属性
    protected boolean rowCursorVisible = true;
    protected boolean colCursorVisible = true;
    private int channelId = 1;
    private Context context=App.get().getApplicationContext();

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
        if (TChan.isCh1ToS4(channelId)==false) {
            setParam("---", "---", "---", "---",
                    "---", "---", "---", "---");
        } else {
            draw();
        }
    }

    public boolean isRowCursorVisible() {
        return rowCursorVisible;
    }

    public void setRowCursorVisible(boolean rowCursorVisible) {
        this.rowCursorVisible = rowCursorVisible;
        draw();
    }

    public boolean isColCursorVisible() {
        return colCursorVisible;
    }

    public void setColCursorVisible(boolean colCursorVisible) {
        this.colCursorVisible = colCursorVisible;
        draw();
    }

    //endregion
    private CursorMeasureStruct param = null;

    private int showX = GlobalVar.get().getMeasureCursorPosition(WorkMode_YT).x;
    private int showY = GlobalVar.get().getMeasureCursorPosition(WorkMode_YT).y;  //GlobalVar.getMeasureCursorPosition();
    private int padding = 13;
    protected Paint p;
    protected Bitmap bmp;
    protected Canvas mCanvas;
    protected boolean isChanageBitmap = false;
    private ICanvasGL canvasGL;
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
    public CurSorMeasureBase() {
        param = new CursorMeasureStruct();
        bmp = Bitmap.createBitmap(200, 220, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bmp);
        p = new Paint();
        p.setTextSize(20);
        p.setAntiAlias(true);
        draw();
    }

    //region IWorkMode 接口
    @Override
    public void switchWorkMode(@IWorkMode.WorkMode int workMode) {
        Point p = GlobalVar.get().getMeasureCursorPosition(workMode);
        showX = p.x;
        showY = p.y;
    }


    //endregion

    @Override
    public void draw(Canvas canvas) {
        if (colCursorVisible || rowCursorVisible) {
            synchronized (bmp) {
                canvas.drawBitmap(bmp, showX, showY, null);
            }
        }
    }

    @Override
    public void draw(ICanvasGL canvas) {

//        if (colCursorVisible || rowCursorVisible) {
//            synchronized (bmp) {
//                if (isChanageBitmap) canvas.invalidateTextureContent(bmp);
//                canvas.drawBitmap(bmp, showX, showY);
//                isChanageBitmap = false;
//            }
//        }
    }

    //region 显示
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    protected void draw() {
        synchronized (bmp) {
            p.setXfermode(clearMode);
            mCanvas.drawPaint(p);
            p.setXfermode(srcMode);
            int textAllHeight = 0;
            int textAllWidth = 0;
            String row1s = "Y1:" + (param.row1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row1);
            String row2s = "Y2:" + (param.row2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row2);
            String detaRows = "ΔY:" + (param.detaRow == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaRow);
            String col1s = "X1:" + (param.col1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col1);
            String col2s = "X2:" + (param.col2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col2);
            String detaCols = "ΔX:" + (param.detaCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaCol);
            String detaTCols = "1/ΔX:" + (param.detaTCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaTCol);
            String ss = "S:" + (param.S == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.S);
            if (rowCursorVisible && colCursorVisible) {
                textAllWidth = Math.max(textAllWidth, getTextWidth(row1s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(row2s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaRows));
                textAllWidth = Math.max(textAllWidth, getTextWidth(col1s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(col2s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaCols));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaTCols));
                textAllWidth = Math.max(textAllWidth, getTextWidth(ss));
                textAllHeight = (getTextHeight() + 5) * 8;
            } else if (rowCursorVisible) {
                textAllWidth = Math.max(textAllWidth, getTextWidth(row1s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(row2s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaRows));
                textAllHeight = (getTextHeight() + 5) * 3;
            } else if (colCursorVisible) {
                textAllWidth = Math.max(textAllWidth, getTextWidth(col1s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(col2s));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaCols));
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaTCols));
                textAllHeight = (getTextHeight() + 5) * 4;
            }
            if (textAllHeight != 0 && textAllWidth != 0) {
                if (TChan.isRef(channelId)) {
                    p.setColor(TChan.getChannelColor(context, TChan.RefActive));
                } else {
                    p.setColor(TChan.getChannelColor(context, channelId));
                }
                p.setAlpha(255);
                p.setStyle(Paint.Style.STROKE);
                textAllHeight += (padding * 2);
                textAllWidth += (padding * 2);
                mCanvas.drawRoundRect(1, 1, textAllWidth - 1, textAllHeight - 1, 6f, 6f, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.BLACK);
                p.setAlpha(204);
                mCanvas.drawRoundRect(1, 1, textAllWidth - 1, textAllHeight - 1, 6f, 6f, p);
            }

            p.setAlpha(255);
            p.setColor(App.get().getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewEnable));
            if (rowCursorVisible && colCursorVisible) {
                int y = drawRow();
                y = drawCol(y);
                y = drawDeltaX(y);
                drawS(y);
            } else if (rowCursorVisible) {
                drawRow();
            } else if (colCursorVisible) {
                int y = drawCol(padding - 5);
                drawDeltaX(y);
            }

            isChanageBitmap = true;
            onRefresh();
        }
    }

    protected int drawRow() {
        int x = padding, y = padding;
        String text = "Y1:" + (param.row1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row1);
        y = y + getTextHeight();
        mCanvas.drawText(text, x, y, p);

        text = "Y2:" + (param.row2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row2);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);

        text = "ΔY:" + (param.detaRow == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaRow);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);

        return y;
    }

    protected int drawCol(int y) {
        int x = padding;
        String text = "X1:" + (param.col1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col1);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);

        text = "X2:" + (param.col2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col2);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);

        text = "ΔX:" + (param.detaCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaCol);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);

        return y;
    }

    protected int drawDeltaX(int y) {
        int x = padding;
        String text = "1/ΔX:" + (param.detaTCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaTCol);
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);
        return y;
    }

    protected void drawS(int y) {
        String text = "S:" + (param.S == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.S);
        int x = padding;
        y = y + getTextHeight() + 5;
        mCanvas.drawText(text, x, y, p);
    }

    private Rect rect = new Rect();

    private int getTextHeight() {
        String text="123456789mV";
        p.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }

    public int getTextWidth(String text) {
        p.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }
    //endregion

    public void setParam(String row1, String row2, String detaRow, String col1, String col2, String detaCol,
                         String detaTCol, String S) {
        param.row1 = row1;
        param.row2 = row2;
        param.detaRow = detaRow;
        param.col1 = col1;
        param.col2 = col2;
        param.detaCol = detaCol;
        param.detaTCol = detaTCol;
        param.S = S;
        draw();
    }
}
