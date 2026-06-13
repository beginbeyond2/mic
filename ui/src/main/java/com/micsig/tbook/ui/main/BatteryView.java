package com.micsig.tbook.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.BitmapUtil;

/**
 * Created by yangj on 2017/11/22.
 */

public class BatteryView extends View {
    private Bitmap bitmap;
    private Bitmap bitmapEmpty;
    private Bitmap bitmapCharge;
    private Bitmap bitmapSupply;
    private int level;//0~100
    private int color;
    private Paint paint;
    private int fullBattery;
    private int curBattery;
    private boolean bCharge = false;

    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmapEmpty = BitmapUtil.getBitmapFromDrawable(context, R.drawable.state_battery_empty);
        bitmapCharge = BitmapUtil.getBitmapFromDrawable(context, R.drawable.state_battery_charge);
        bitmapSupply=BitmapFactory.decodeResource(getResources(),R.drawable.battery_supply);

        bitmap = bitmapEmpty;
        level = 40;
        color = 0xFF808080;
        paint = new Paint();
        paint.setColor(color);
        fullBattery = bitmap.getHeight() - 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == bitmapEmpty) {
            for (int i = 0; i < curBattery; i++) {
                canvas.drawBitmap(bitmapSupply, 1, bitmapEmpty.getHeight() - 2 - i, null);
            }
        }
        canvas.drawBitmap(bitmap, 0, 0, null);

        //canvas.drawRect(2, 3 + fullBattery - curBattery, bitmap.getWidth() - 2, bitmap.getHeight() - 1, paint);
        //canvas.drawRect(2 + fullBattery - curBattery, 2, bitmap.getWidth() - 4, bitmap.getHeight() - 1, paint);
//        canvas.drawRect(2,2+fullBattery-curBattery,bitmap.getWidth() - 4, bitmap.getHeight() - 1, paint);
//        canvas.drawBitmap(bitmap, 0, 0, null);


//        if(!bCharge){
//            Paint textPaint = new Paint();
//            textPaint.setColor(Color.RED);
//            textPaint.setTextAlign(Paint.Align.CENTER);
//            textPaint.setTextSize(9);
//            Rect rect = new Rect();
//            this.getLocalVisibleRect(rect);
//
//            Paint.FontMetrics fontMetrics=textPaint.getFontMetrics();
//            float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
//            float baseline=rect.centerY() + distance;
//
//            canvas.rotate(-90,rect.centerX(),rect.centerY());
//
//            canvas.drawText("" + level + "", (float) rect.centerX()-1.5f, (float) baseline+0.5f, textPaint);
//            canvas.rotate(90,rect.centerX(),rect.centerY());
//        }

    }

    public void setLevel(int level) {
        this.level = level;
        curBattery = (int) (fullBattery * level * 1.0 / 100);
        invalidate();
    }

    public int getLevel() {
        return level;
    }

    public void setIcon(boolean charge) {
        bCharge = charge;
        if (charge) {
            bitmap = bitmapCharge;
        } else {
            bitmap = bitmapEmpty;
        }
        invalidate();
    }

    /**
     * 充电时的页面自动更新，这儿只负责显示，重复的操作在上一层handler中实现
     */
    public void selfUpdate() {
        curBattery++;
        if (curBattery > fullBattery) {
            curBattery = (int) (fullBattery * level * 1.0 / 100);
        }
        invalidate();
    }
}
