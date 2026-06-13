package com.micsig.tbook.tbookscope.wavezone.display;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.util.Loggers;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;

/**
 * Created by liwb on 2017/6/19.
 * zoom区的遮罩层+波形
 */

public class WaveMaskLayer_YTZoom {

    //region  单例
    private static class WaveMaskLayer_YTZoom_Holder {
        private static final WaveMaskLayer_YTZoom instance = new WaveMaskLayer_YTZoom();
    }

    public static final WaveMaskLayer_YTZoom getInstance() {
        return WaveMaskLayer_YTZoom_Holder.instance;
    }
    //endregion

    //region 事件接口
      public interface OnZoomEventListener {
        /**
         * @param offset 遮罩层坐标X移动量
         * */
        public void onLayerXChangeEvent(int offset);

        /**
         *
         * @param layerWidth 遮罩层宽度
         */
      //  public void onLayerWidthChangeEvent(int layerWidth);

        /**
         *
         * @param layerTimebaseX 遮罩层时基坐标X
         */
      //  public void onLayerTimeBaseXChangeEvent(int layerTimebaseX);
    }
    //endregion

    //region 属性  layerX zoom窗口中心像素位置  layerWidth zoom窗口的宽  layerTimebaseX 主窗口触发位置
    int layerX = 800, layerWidth = 200 ,layerTimebaseX=800;
    private OnZoomEventListener onZoomEventListenerListener =null;

    public OnZoomEventListener getOnZoomEventListenerListener() {
        return onZoomEventListenerListener;
    }

    public void setOnZoomEventListenerListener(OnZoomEventListener onZoomEventListenerListener) {
        this.onZoomEventListenerListener = onZoomEventListenerListener;
    }

    public void setLayerX(long layerX) {
        if(layerX < -10)
            this.layerX = -10;
        else if(layerX > ScopeBase.getWidth()+10)
            this.layerX = ScopeBase.getWidth()+10;
        else
            this.layerX = (int)layerX;
    }
    public void layerX_move(int offset){
        this.layerX = layerX + offset;
        draw();
        if (onZoomEventListenerListener !=null){
            onZoomEventListenerListener.onLayerXChangeEvent(offset);
        }
    }

    public void setLayerWidth(int layerWidth) {
        if(layerWidth < 2)
            this.layerWidth = 2;
        else
            this.layerWidth = layerWidth;
    }

    public void setLayerTimebaseX(long layerTimebaseX) {
        if(layerTimebaseX < -10)
            this.layerTimebaseX = -10;
        else if(layerTimebaseX > ScopeBase.getWidth()+10)
            this.layerTimebaseX = ScopeBase.getWidth()+10;
        else
            this.layerTimebaseX = (int)layerTimebaseX;
    }

//endregion


    private Bitmap bitmapLayer,oldBitmapLayer;
    private Paint paint;
    private Canvas mCanvas;
    private boolean isChanageBitmap = false;
    private ICanvasGL canvasGL;
    private int zoomH = 1040 / 4;
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
//    private Bitmap bitmapWave;
    private boolean isChanageWave = false;
    private Bitmap[] resBmp;

    public WaveMaskLayer_YTZoom() {
        resBmp = new Bitmap[3];
        resBmp[0] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap();
        resBmp[1] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap();
        resBmp[2] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap();

        paint = new Paint();
        reInitBmp();
        draw();
    }

    public void draw() {
        synchronized (bitmapLayer) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            paint.setColor(Color.argb(90, 60, 128, 128));
            mCanvas.drawRect(new Rect(0, 0, bitmapLayer.getWidth(), bitmapLayer.getHeight()), paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            paint.setColor(Color.argb(0, 0, 0, 0));

            mCanvas.drawRect(new Rect(layerX-layerWidth/2, 0, layerX + layerWidth/2, bitmapLayer.getHeight()), paint);

            if(layerTimebaseX < 0)
                mCanvas.drawBitmap(resBmp[1],0,0,null);
            else if(layerTimebaseX > ScopeBase.getWidth())
                mCanvas.drawBitmap(resBmp[2],ScopeBase.getWidth()-resBmp[2].getWidth(),0,null);
            else
                mCanvas.drawBitmap(resBmp[0],layerTimebaseX-resBmp[0].getWidth()/2,0,null);
            isChanageBitmap = true;
            onRefresh();
        }
    }


//    public void setBitmapWave(int[] points) {
//        synchronized (bitmapWave) {
//            bitmapWave.setPixels(points, 0, bitmapWave.getWidth(), 0, 0, bitmapWave.getWidth(), bitmapWave.getHeight());
//            isChanageWave = true;
//        }
//    }

    public void draw(ICanvasGL canvas) {
//        synchronized (bitmapWave) {
//            //canvas.drawBitmap(bitmapWave, 0, 0, isChanageWave);
//            isChanageWave = false;
//        }
        synchronized (bitmapLayer) {
            canvasGL = canvas;
            if(isChanageBitmap){
                canvas.invalidateTextureContent(bitmapLayer,oldBitmapLayer);
                oldBitmapLayer = null;
            }
            Loggers.d("limh", "zoom maskLayer height= " + bitmapLayer.getHeight());
            canvas.drawBitmap(bitmapLayer, 0, 0);
            isChanageBitmap = false;
        }


    }

    public void changeZoomH(int zoomH) {
        this.zoomH = zoomH;
        reInitBmp();
        draw();
    }

    private void reInitBmp() {
        oldBitmapLayer = bitmapLayer;
        bitmapLayer = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), zoomH, Bitmap.Config.ARGB_8888);
//        bitmapWave = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), zoomH, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmapLayer);
    }


}
