package com.micsig.tbook.tbookscope.first;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.micsig.base.Logger;
import com.micsig.base.OEM;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;

/**
 * Created by liwb on 2018/1/26.
 */

public class SplashScreenSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "SplashScreenSurfaceView";
    private SurfaceHolder holder ;
    private Context context;
    private Bitmap bmp;
    private  Thread drawThread;
    /**
     * gif 动画绘制的开始坐标
     */
    private int x=240;
    private int y =180;
    private boolean isRun=true;
    private int dotY=GlobalVar.get().getScreen().height()  -20;
    private Rect bmpSrc=new Rect();
    private Rect ScreenDes=GlobalVar.get().getScreen();

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            initView();
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            paint.setFakeBoldText(true);
            paint.setAntiAlias(true);
            long startTime = SystemClock.elapsedRealtime();


            while (isRun) {
                Canvas canvas = holder.lockCanvas();
                if (canvas==null){
                    try {
                        Thread.sleep(20);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                canvas.drawColor(Color.BLACK);
                if (bmp!=null) {
                    canvas.drawBitmap(bmp, bmpSrc,ScreenDes, paint);
                }
                long count = (SystemClock.elapsedRealtime() - startTime) / 1000;
                String strLoad = "";
                count %= 6;
                for(int i=0;i<=count;i++){
                    strLoad += ".";
                }
                canvas.drawText(strLoad,10,dotY,paint);

                holder.unlockCanvasAndPost(canvas);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public SplashScreenSurfaceView(Context context, AttributeSet attrs){
        super(context,attrs);
        this.context=context;
        holder = getHolder();// 电影的播放器
        holder.addCallback(this);
        drawThread=new Thread(run);
        holder.setFormat(PixelFormat.RGBA_8888);
    }

    private void initView() {
        bmp = OEM.getSplashScreen();
        if(bmp == null){
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            op.inScaled = false;
            int id = R.raw.smart_oscilloscope_mho_10008;
            bmp = BitmapFactory.decodeResource(context.getResources(), id, op);
        }
        if(bmp != null) {
            bmpSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void stop(){
        isRun=false;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
        Logger.d(TAG,"surfaceDestroyed");
    }


}
