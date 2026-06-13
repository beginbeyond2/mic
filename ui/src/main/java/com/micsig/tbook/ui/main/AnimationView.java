package com.micsig.tbook.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

/**
 * Created by liwb on 2017/12/14.
 * 动画View 用在外部按键动画显示框
 */

public class AnimationView extends View {

    //region 属性
    private float aX;
    private float aY;
    private float aWidth;
    private float aHeight;

    public float getaX() {
        return aX;
    }

    public void setAX(float aX) {
        this.aX = aX;
        setX(aX);
    }

    public float getaY() {
        return aY;
    }

    public void setAY(float aY) {
        this.aY = aY;
        setY(aY);
    }

    public float getaWidth() {
        return aWidth;
    }

    public void setAWidth(float aWidth) {
        this.aWidth = aWidth;
        ViewGroup.LayoutParams layoutParams=this.getLayoutParams();
        layoutParams.width=(int)aWidth;
        this.setLayoutParams(layoutParams);
    }

    public float getaHeight() {
        return aHeight;
    }

    public void setAHeight(float aHeight) {
        this.aHeight = aHeight;
        ViewGroup.LayoutParams layoutParams=this.getLayoutParams();
        layoutParams.height=(int) aHeight;
        this.setLayoutParams(layoutParams);
    }

    //endregion

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



}
