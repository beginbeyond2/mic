package com.micsig.tbook.tbookscope.wavezone.display;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;

import java.util.Arrays;

/**
 * Created by liwb on 2017/6/16.
 */

public class WaveGrid_YTZoom implements IGrid {

    //region 单例创建
//    private static class WaveGrid_YTZoom_Holder {
//        private static final WaveGrid_YTZoom instance = new WaveGrid_YTZoom();
//    }
//
//    public static final WaveGrid_YTZoom get() {
//        return WaveGrid_YTZoom_Holder.instance;
//    }
    //endregion

    //region 属性
    /** 起始亮度 */
    private final int BaseBright=40;
    private int GridAttr_Enum = WaveGridManage.GridAttr_CrossLine | WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_ALLPoint | WaveGridManage.GridAttr_Frame;
    private int GridLine_Bright = Color.rgb(0x80, 0x80, 0x80);
    private static int zoomHeight = 1040 / 4;//默认

    @Override
    public void refresh() {
        drawGrid();
    }

    @Override
    public void setHeightDiv(int heightDiv) {
        zoomHeight = (heightDiv * ScopeBase.getVerticalGridCnt()) / 4;
        reInitBmp();
        drawGrid();
    }

    public int getGridLine_Attr() {
        return GridAttr_Enum;
    }

    public void setGridLine_Attr(int gridLine_Attr) {
        GridAttr_Enum = gridLine_Attr;
        drawGrid();
    }

    public int getGridLine_Bright() {
//        return GridLine_Bright * 2 * 100 / 255 - BaseBright;
        return (int)((GridLine_Bright-BaseBright)/180.0f);
    }

    public void setGridLine_Bright(int gridLine_Bright) {
//        Logger.i(Command.TAG,"i:"+gridLine_Bright);
        //int c=gridLine_Bright%256;
        int c=(int)(BaseBright+(180*gridLine_Bright*1.0/100));
//        int c = (gridLine_Bright + BaseBright) * 255 / 100 / 2;
        GridLine_Bright = Color.rgb(c, c, c);
        drawGrid();
    }
    //endregion

    private Bitmap bmp;
    private Bitmap bmpOld;
    private Canvas mCanvas;
    private Paint paint;
    private boolean isChanageBitmap = false;
    private ICanvasGL canvasGL;
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
    public WaveGrid_YTZoom() {
        paint = new Paint();
        reInitBmp();
        drawGrid();
    }


    public void draw(ICanvasGL canvas) {
        synchronized (bmp) {
            canvasGL = canvas;
            canvasGL.setSize(canvas.getWidth(), zoomHeight);
            if(isChanageBitmap) {
                canvas.invalidateTextureContent(bmp, bmpOld);
                bmpOld = null;
            }
            RectF src = new RectF(0, 0, 1800, zoomHeight);
            RectF des = new RectF(0, 0, 1800, zoomHeight);
            canvas.drawBitmap(bmp, src, des);
            //canvas.drawBitmap(bmp,new RectF(0,0,bmp.getWidth(),bmp.getHeight()),new RectF(0,0,bmp.getWidth(),bmp.getHeight()),isChanageBitmap);
            isChanageBitmap = false;

        }
    }

    private void drawGrid() {
        synchronized (bmp) {

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));


            if ((GridAttr_Enum & WaveGridManage.GridAttr_CrossPoint) == WaveGridManage.GridAttr_CrossPoint) {
                drawCrossPoint(mCanvas);
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_ALLPoint) == WaveGridManage.GridAttr_ALLPoint) {
//                drawAllPoint(mCanvas);
                drawAllPoint_line(mCanvas);
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_CrossLine) == WaveGridManage.GridAttr_CrossLine) {
                drawCrossLine(mCanvas);
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_Frame) == WaveGridManage.GridAttr_Frame) {
                drawFrame(mCanvas);
            }
            isChanageBitmap = true;
        }
    }

    private void drawCrossLine(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());

        paint.setColor(WaveGrid_YT.getCrossLine(GridLine_Bright));
        paint.setStrokeWidth(1);
        //横线
        float[] rowPixs = new float[zoomWidth / 2 * 2];
        Arrays.fill(rowPixs,Float.MAX_VALUE);
        for (int i = 0; i < zoomWidth; i += 2) {
            rowPixs[i] = i;
            rowPixs[i + 1] = (float) zoomHeight / 2;
        }
        canvas.drawPoints(rowPixs, paint);
        //纵线
        float[] colPixs = new float[(zoomHeight + zoomHeight / 5 * 2) * 2];
        Arrays.fill(colPixs,Float.MAX_VALUE);
        int index = 0;
        for (int i = 0; i < zoomHeight; i += 2) {
            if (i % 5 == 0) {
                colPixs[index] = (float)zoomWidth / 2 - 2;
                index++;
                colPixs[index] = i;
                index++;

                colPixs[index] = (float)zoomWidth / 2 + 2;
                index++;
                colPixs[index] = i;
                index++;
            }
            colPixs[index] = (float)zoomWidth / 2;
            index++;
            colPixs[index] = i;
            index++;
        }
        canvas.drawPoints(colPixs, paint);

    }

    // 画小格点
    private void drawCrossPoint(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        paint.setColor(GridLine_Bright);
        paint.setStrokeWidth(1);

        for (int i = 0; i < zoomWidth; i += 30) {
            float[] colPixs = new float[100/10 * 2];
            Arrays.fill(colPixs,Float.MAX_VALUE);
            int index = 0;
            for (int j = 0; j < zoomHeight; j += zoomHeight / 10) {   //  150/10=15
                if(index >= colPixs.length) continue;
                colPixs[index] = i;
                colPixs[index + 1] = j;
                index += 2;
            }
            canvas.drawPoints(colPixs, paint);
        }

    }

    // 画大格线
    private void drawAllPoint(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());

        paint.setColor(GridLine_Bright);
        paint.setStrokeWidth(1);

        for (int i = 0; i <= zoomWidth; i += 150) {
            float[] colPixs = new float[zoomHeight / 2 * 2];
            Arrays.fill(colPixs,Float.MAX_VALUE);
            for (int j = 0; j < zoomHeight; j += 2) {
                colPixs[j] = i;
                colPixs[j + 1] = j;
            }
            canvas.drawPoints(colPixs, paint);
        }
    }
    private void drawAllPoint_line(Canvas canvas){
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);
        paint.setPathEffect(dashPathEffect);
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        for (int y = zoomHeight / 2; y < canvas.getHeight(); y += zoomHeight / 2) {
            canvas.drawLine(0,y,canvas.getWidth(),y,paint);
        }
        for (int x = 150; x < canvas.getWidth(); x += 150) {
            canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
        }
        paint.setPathEffect(null);
    }

    private void drawFrame(Canvas canvas) {
        paint.setColor(0xFF008080);
        paint.setStrokeWidth(1);
        canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);
        canvas.drawLine(0, 0, 0, canvas.getHeight(), paint);
        canvas.drawLine(0, canvas.getHeight() - 1, canvas.getWidth(), canvas.getHeight() - 1, paint);
        canvas.drawLine(canvas.getWidth() - 1, 0, canvas.getWidth() - 1, canvas.getHeight(), paint);
    }

    private void reInitBmp() {
        bmpOld = bmp;
        bmp = Bitmap.createBitmap(1800, zoomHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bmp);
    }



}
