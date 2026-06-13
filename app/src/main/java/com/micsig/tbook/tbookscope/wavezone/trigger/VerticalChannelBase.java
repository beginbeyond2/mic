package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2017/11/6.
 * 垂直通道基类
 */

public class VerticalChannelBase implements IWave {
    private static final String TAG = "VerticalChannelBase";
//    private static final Bitmap resBmp = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap();
//    private static final Bitmap resBmpLeft = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap();
//    private static final Bitmap resBmpRight = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap();

    private Bitmap[] resBmp;
    private final int resBmpCenter = 0;
    private final int resBmpLeft = 1;
    private final int resBmpRight = 2;
    private final int resBmpNoCenter = 3;
    private final int resBmpNoLeft = 4;
    private final int resBmpNoRight = 5;

    private IWave.OnMovingWaveEvent onMovingWaveEvent;
    private IWave.OnSelectChangeEvent onSelectChangeEvent;

    //region 属性
    private boolean selected;
    private boolean visible = true;
    long x;
    double y;
    private int LineNameID = TChan.TriggerTime;

    public VerticalChannelBase() {
    }

    public void setLineNameID(int lineNameID) {
        this.LineNameID = lineNameID;
    }

    public int getLineNameID() {
        return LineNameID;
    }

    public long getX() {
        return x;
    }

    public void setOffsetX(int offsetX) {
        setX(this.x - offsetX);
    }

    public void setX(long x) {
        this.x = x;
        draw();
        if (onMovingWaveEvent != null) {
            onMovingWaveEvent.OnMovingWave(this, x, y, false, false);
        }
    }

    public void setXFromEventBus(long x) {
        this.x = x;
        draw();
        if (onMovingWaveEvent != null) {
            onMovingWaveEvent.OnMovingWave(this, x, y, false, true);
        }
    }

    public void update() {
        draw();
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public int getColor() {
        return Color.WHITE;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        draw();
        if (selected) {
            if (onSelectChangeEvent != null) onSelectChangeEvent.OnSelectChange(this, true);
        }
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    public void setOnMovingWaveEvent(IWave.OnMovingWaveEvent onMovingWaveEvent) {
        this.onMovingWaveEvent = onMovingWaveEvent;
    }

    public void setOnSelectChangeEvent(IWave.OnSelectChangeEvent onSelectChangeEvent) {
        this.onSelectChangeEvent = onSelectChangeEvent;
    }
    //endregion

    private Bitmap bmp;
    private Canvas mCanvas;
    private Paint paint;
    protected boolean isChanageBitmap = false;
    private ICanvasGL canvasGL;
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
    public void init(Bitmap[] resBmp) {
        this.resBmp = resBmp;
        bmp = Bitmap.createBitmap(resBmp[resBmpLeft].getWidth() + 10, resBmp[resBmpCenter].getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bmp);
        paint = new Paint();
        x = GlobalVar.get().getMainWave().x / 2;
        y = 0;
        draw();
    }


    @Override
    public void moveLine(int offsetX, int offsetY) {

    }

    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmp[resBmpLeft].getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, null);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth(), 0, null);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp[resBmpCenter].getWidth() / 2, 0, null);
//            }
//
//        }
    }

    public void draw(ICanvasGL canvas) {
        if (!visible) return;
        synchronized (bmp) {
            canvasGL = canvas;
            if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
            if (this.x < 0) {
                canvas.drawBitmap(bmp, 0, 0);
            } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth(),
                        0);
            } else {
                canvas.drawBitmap(bmp, (int) x - resBmp[resBmpCenter].getWidth() / 2, 0);
            }
            isChanageBitmap = false;
        }
    }

    @Override
    public void resultIniRect() {

    }

    @Override
    public void setLineNameId(int nameId) {
        this.LineNameID = nameId;
    }


    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    private void draw() {
        synchronized (bmp) {
            paint.setXfermode(clearMode);
            mCanvas.drawPaint(paint);
            paint.setXfermode(srcMode);
            if (this.selected == true) {
                if (this.x < 0) {
                    mCanvas.drawBitmap(resBmp[resBmpLeft], 0, 0, paint);
                } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    mCanvas.drawBitmap(resBmp[resBmpRight], 0, 0, paint);
                } else {
                    mCanvas.drawBitmap(resBmp[resBmpCenter], 0, 0, paint);
                }
            } else {
                if (this.x < 0) {
                    mCanvas.drawBitmap(resBmp[resBmpNoLeft], 0, 0, paint);
                } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    mCanvas.drawBitmap(resBmp[resBmpNoRight], 0, 0, paint);
                } else {
                    mCanvas.drawBitmap(resBmp[resBmpNoCenter], 0, 0, paint);
                }
            }
            isChanageBitmap = true;
            onRefresh();
        }

    }

    @Override
    public void movePix(double px) {
        setX(Math.round(x + px));
    }

    private Rect rect = new Rect();

    public boolean selectCursor(int x, int y) {
        boolean selected;
        //rect.set(0, this.y - resBmp[resBmpCenter].getHeight() / 2, bmp.getWidth(), bmp.getHeight() + this.y - resBmp[resBmpCenter].getHeight() / 2);
        rect.set(((int) this.x) - resBmp[resBmpLeft].getWidth() / 2, 0, bmp.getWidth() + ((int) this.x) - resBmp[resBmpLeft].getWidth() / 2, bmp.getHeight());
        selected = rect.contains(x, y);
        return selected;
    }

}
