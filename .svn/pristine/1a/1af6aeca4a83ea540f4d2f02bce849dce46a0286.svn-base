package com.micsig.tbook.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatRadioButton;

/**
 * @auother Liwb
 * @description: 点击抖动，也算点击
 * @data:2022-2-12 9:13
 */
public class MRadioButton extends AppCompatRadioButton {
    private static final String TAG=MRadioButton.class.getSimpleName();

    private static final int MinClickRangePx=10;

    private int oldX,oldY;

    public MRadioButton(Context context) {
        this(context,null,android.R.attr.textViewStyle);
    }

    public MRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs,android.R.attr.textViewStyle);
    }

    public MRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:{
                oldX=(int)event.getRawX();
                oldY=(int)event.getRawY();
            }break;
            case MotionEvent.ACTION_UP:{
                int offsetX=Math.abs((int)event.getRawX()-oldX);
                int offsetY=Math.abs((int)event.getRawY()-oldY);
                //Logger.i(TAG,"offsetX:"+offsetX+",offsetY:"+offsetY);
                if (offsetX<MinClickRangePx && offsetY<MinClickRangePx && isEnabled()){
                    //Logger.i(TAG,"Click Name:"+getText());
                    super.performClick();
                }
                return true;
            }
        }

        return super.onTouchEvent(event);
    }
}
