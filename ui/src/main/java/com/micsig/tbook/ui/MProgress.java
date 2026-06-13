package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;

/**
 * Created by liwb on 2018/5/25.
 */

public class MProgress extends View {
   private String TAG="MProgress";

   private Context context;
   private Bitmap bitmapPlate,bitmapPlateActive;
   private Canvas cacheCanvas;
   private int bitmapWidth,bitmapHeight;
   private Paint backgroundPaint;
   private  Paint progressPaint;
   private  int progress;
   private Rect srcRect,desRect;

    /**
     * 进度条的宽度
     */
    private int view_width;
    /**
     * 画布的宽度
     */
    private int view_base_width;
    /**
     * 控件的宽度
     */
    private int view_edge_width;

   //渐变色开始
    private static final int DEFAULT_START_COLOR = Color.parseColor("#34DAB5");
    //渐变色结束
    private static final int DEFAULT_END_COLOR = Color.parseColor("#27A5FE");


    public MProgress(Context context) {
        super(context);
        initView(context);
    }

    public MProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }



    private void initView(Context context) {
        this.context = context;
        bitmapPlate = BitmapFactory.decodeResource(context.getResources(), R.drawable.sbtn_slider_plate);
        bitmapWidth = bitmapPlate.getWidth();
        bitmapHeight = bitmapPlate.getHeight();

        bitmapPlateActive = BitmapFactory.decodeResource(context.getResources(), R.drawable.sbtn_slider_plate_active);
        bitmapWidth = bitmapPlateActive.getWidth();
        bitmapHeight = bitmapPlateActive.getHeight();

        backgroundPaint = new Paint();
        backgroundPaint.setStrokeWidth(bitmapWidth);
        backgroundPaint.setColor(Color.parseColor("#cccccc"));
        backgroundPaint.setDither(true);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setTextSize(17);

        progressPaint = new Paint();
        progressPaint.setStrokeWidth(bitmapWidth);
        progressPaint.setDither(true);
        progressPaint.setAntiAlias(true);
        srcRect=new Rect(0,0,bitmapPlate.getWidth(),bitmapPlate.getHeight());
        desRect=new Rect(0,0,bitmapPlate.getWidth(),bitmapPlate.getHeight());
    }

    public void setProgress(int progress) {
        this.progress = progress;
         view_width = bitmapPlate.getWidth() * progress / 100;
         desRect.set(0,0,view_width,bitmapPlate.getHeight());
        invalidate();
//        Logger.i(TAG, "setProgress:" + progress);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawColor(0x00000000);
        canvas.drawBitmap(bitmapPlate,srcRect,srcRect,progressPaint);
        canvas.drawBitmap(bitmapPlateActive,desRect,desRect,progressPaint);
        canvas.drawText(progress+"%",200,17,backgroundPaint);
        canvas.restore();
//        Logger.i(TAG,"progress:"+progress);
    }

}
