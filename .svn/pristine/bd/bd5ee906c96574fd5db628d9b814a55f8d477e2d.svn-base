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
import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;

import java.util.Arrays;

/**
 * Created by liwb on 2017/11/2.
 * XY的网络有个特点，它是始终不会变的。
 * 他与YT网络是一样的，都有那几个5个属性
 * 要按理说应该搞个接口，不过就这两个，手写一下算了。
 */

public class WaveGrid_XY implements IWorkMode, IGrid {

    //region 属性
    /** 起始亮度 */
    private final int BaseBright=40;
    private int GridAttr_Enum = GridAttr_CrossLine | GridAttr_CrossPoint | GridAttr_ALLPoint | GridAttr_Frame;
    private int GridLine_Bright = Color.argb(0x80, 0x80, 0x80, 0x80);


    @Override
    public void refresh() {
        drawGrid();
    }

    @Override
    public void setHeightDiv(int heightDiv) {

    }

    @Override
    public int getGridLine_Attr() {
        return GridAttr_Enum;
    }

    @Override
    public void setGridLine_Attr(int gridLine_Attr) {
        GridAttr_Enum = gridLine_Attr;
        drawGrid();
    }


    @Override
    public int getGridLine_Bright() {
//        return GridLine_Bright * 2 * 100 / 255 - BaseBright;
        return (int)((GridLine_Bright-BaseBright)/180.0f);
    }

    /**
     * 亮度范围50-230之间
     * @param gridLine_Bright
     */
    @Override
    public void setGridLine_Bright(int gridLine_Bright) {
        //int c=gridLine_Bright%256;
//        int c = (gridLine_Bright + BaseBright) * 255 / 100 / 2;
        int c=(int)(BaseBright+(180*gridLine_Bright*1.0/100));
        GridLine_Bright = Color.rgb(c, c, c);
        drawGrid();
    }
    //endregion

    private Bitmap bmp;
    private Canvas mCanvas;
    private Paint paint;
    /**
     * 图像是否改变，即图像像素是否改变
     */
    private boolean isChanageBitmap = false;

    private ICanvasGL canvasGL;
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }

    public WaveGrid_XY() {
        //temp 宽高应该在一个静态类中，直接获取到。
        bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY),
                GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bmp);
        paint = new Paint();
        drawGrid();

    }

    //region interface IworkMode
    @Override
    public void switchWorkMode(@WorkMode int workMode) {

    }
    //endregion

    //region 单例创建
//    private static class WaveGrid_XY_Holder {
//        private static final WaveGrid_XY instance = new WaveGrid_XY();
//    }
//
//    public static final WaveGrid_XY getInstance() {
//        return WaveGrid_XY_Holder.instance;
//    }
    //endregion

    @Override
    public void draw(ICanvasGL canvas) {
        synchronized (bmp) {
            canvasGL = canvas;
            //canvas.drawBitmap(bmp,0,0,isChanageBitmap);
            if(isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
            canvas.drawBitmap(bmp, new RectF(0, 0, bmp.getWidth(), bmp.getHeight()), new RectF(0, 0, bmp.getWidth(), bmp.getHeight()));
            isChanageBitmap = false;
        }
    }

    private int widthBigDiv = 100;
    private int heightBigDiv = 100;
    private int widthSmallDiv = widthBigDiv/5;
    private int heightSmallDiv = heightBigDiv/5;

    private void drawGrid() {
        synchronized (bmp) {

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));


            if ((GridAttr_Enum & GridAttr_CrossPoint) == GridAttr_CrossPoint) {
                drawCrossPoint(mCanvas);
            }
            if ((GridAttr_Enum & GridAttr_ALLPoint) == GridAttr_ALLPoint) {
//                drawAllPoint(mCanvas);
                drawALLLine(mCanvas);
            }
            if ((GridAttr_Enum & GridAttr_CrossLine) == GridAttr_CrossLine) {
                drawCrossLine(mCanvas);
            }
            if ((GridAttr_Enum & GridAttr_Frame) == GridAttr_Frame) {
                drawFrame(mCanvas);
            }

            isChanageBitmap = true;
            onRefresh();
        }
    }

    //region 私有函数
    private void drawFrame(Canvas canvas) {
        paint.setColor(0xFF008080);
        paint.setStrokeWidth(1);
        canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);
        canvas.drawLine(0, 0, 0, canvas.getHeight(), paint);
        // FIXME: 2024/7/8  WorkTile:#2260 效果上看XY界面右下两个边比左上宽，故作此修改。
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), paint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), paint);

    }

    //十字线
    private void drawCrossLine(Canvas canvas) {
        int rowPoint = canvas.getWidth() / 2;
        int rowpoint1 = 8 * 4 * 2; //18格 1条4条线，一条线2个点
        int rowpoint2 = 8 * 4;   //18格 1格4个点

        int colPoint = canvas.getHeight();
        int colPoint1 = 8 * 4 * 2;
        int colPoint2 = 8 * 4;

        float[] rowPts = new float[(rowPoint + rowpoint1 + rowpoint2) * 2];
        float[] colPts = new float[(colPoint + colPoint1 + colPoint2) * 2];
        Arrays.fill(rowPts,Float.MAX_VALUE);
        Arrays.fill(colPts,Float.MAX_VALUE);
        paint.setColor(WaveGrid_YT.getCrossLine(GridLine_Bright));
        paint.setStrokeWidth(1);
        //画横线
        int index = 0;
        for (int x = 0; x < canvas.getWidth(); x += 2) {
            //被50整除画4个点，被5整除画2个点。
            if (x % widthBigDiv == 0 && x % widthSmallDiv == 0) {
                rowPts[index] = x;
                index = index + 1;
                rowPts[index] = canvas.getHeight() / 2 - 2;
                index += 1;

                rowPts[index] = x;
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 - 4;
                index += 1;

                rowPts[index] = x;
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 2;
                index += 1;

                rowPts[index] = x;
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 4;
                index += 1;

            } else if (x % widthSmallDiv == 0) {
                rowPts[index] = x;
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 - 2;
                index += 1;

                rowPts[index] = x;
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 2;
                index += 1;
            }
            rowPts[index] = x;
            index = index + 1;
            rowPts[index] = canvas.getHeight() / 2;
            index = index + 1;
        }
        canvas.drawPoints(rowPts, paint);

        //画坚线
        index = 0;
        for (int y = 0; y < canvas.getHeight(); y += 2) {
            //被50整除画4个点，被5整除画2个点。
            if (y % heightBigDiv == 0 && y % heightSmallDiv == 0) {
                colPts[index] = canvas.getWidth() / 2 - 2;
                index = index + 1;
                colPts[index] = y;
                index += 1;

                colPts[index] = canvas.getWidth() / 2 - 4;
                index += 1;
                colPts[index] = y;
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 2;
                index += 1;
                colPts[index] = y;
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 4;
                index += 1;
                colPts[index] = y;
                index += 1;

            } else if (y % heightSmallDiv == 0) {
                colPts[index] = canvas.getWidth() / 2 - 2;
                index += 1;
                colPts[index] = y;
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 2;
                index += 1;
                colPts[index] = y;
                index += 1;
            }
            colPts[index] = canvas.getWidth() / 2;
            index = index + 1;
            colPts[index] = y;
            index = index + 1;
        }
        canvas.drawPoints(colPts, paint);

    }

    //十字点
    private void drawCrossPoint(Canvas canvas) {
        float[] pts = new float[canvas.getWidth() / widthBigDiv * canvas.getHeight() / heightBigDiv * 2];
        Arrays.fill(pts,Float.MAX_VALUE);
        paint.setColor(GridLine_Bright);
        paint.setStrokeWidth(1);

        int index = 0;
        for (int y = 0; y < canvas.getHeight(); y += heightBigDiv) {
            for (int x = 0; x < canvas.getWidth(); x += widthBigDiv) {
                pts[index] = x;
                index += 1;
                pts[index] = y;
                index += 1;
            }
        }
        canvas.drawPoints(pts, paint);

    }

    //画所有点
    private void drawAllPoint(Canvas canvas) {
        int rowPoint = canvas.getWidth() / widthSmallDiv * 2;
        int colPoint = canvas.getHeight() / heightSmallDiv * 2;
        float[] pts = new float[rowPoint * colPoint];
        Arrays.fill(pts,Float.MAX_VALUE);
        paint.setColor(GridLine_Bright);
        paint.setStrokeWidth(1);

        int index = 0;
        for (int y = 0; y < canvas.getHeight(); y += heightSmallDiv)
            for (int x = 0; x < canvas.getWidth(); x += widthSmallDiv) {
                if (x % widthBigDiv == 0) {
                    pts[index] = x;
                    index += 1;
                    pts[index] = y;
                    index += 1;
                }
                if (y % heightBigDiv == 0) {
                    pts[index] = x;
                    index += 1;
                    pts[index] = y;
                    index += 1;
                }
            }

        canvas.drawPoints(pts, paint);
    }

    private void drawALLLine(Canvas canvas){
        for(int y=heightBigDiv;y<canvas.getHeight();y+=heightBigDiv){
            canvas.drawLine(0,y,canvas.getWidth(),y,paint);
        }
        for(int x=widthBigDiv;x<canvas.getWidth();x+=widthBigDiv){
            canvas.drawLine(x,0,x,canvas.getHeight(),paint);
        }
    }
    //endregion
}
