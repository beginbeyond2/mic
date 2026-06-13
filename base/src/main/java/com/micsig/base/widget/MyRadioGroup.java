package com.micsig.base.widget;

/**
 * @Description: 扩展RadioGroup，可设置最大行数，多了换列
 * @Author: limh
 * @CreateDate: 2024/3/22 9:04
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import com.micsig.base.R;

public class MyRadioGroup extends RadioGroup {
    private static final String TAG = "MyRadioGroup";
    private int maxRows = 1;//默认1，即横向显示
    private Context context;

    public MyRadioGroup(Context context) {
        this(context, null);
    }

    public MyRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyRadioGroup);
        maxRows = ta.getInt(R.styleable.MyRadioGroup_maxRows, Integer.MAX_VALUE);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //调用ViewGroup的方法，测量子view
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int currentRow = 1;//当前行

        int maxLineWidth = 0;//最大列宽
        int maxLineHeight = 0;//最大行高

        int totalWidth = 0;//累积宽
        int totalHeight = 0;//累积高
        int perHeight = 0;//每列的高

        int count = getChildCount();
        //假设 widthMode和heightMode都是AT_MOST
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
//            if(child.getVisibility() != View.VISIBLE) continue;
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();

            int deltaX = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;//当前宽
            int deltaY = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;//当前高

            maxLineWidth = Math.max(maxLineWidth, deltaX);//更新最大列宽
            maxLineHeight = Math.max(maxLineHeight, deltaY);//更新最大行高
            if (currentRow > maxRows) { //换列
                currentRow = 1; //重置行数
                totalWidth += deltaX;//累加宽度
                perHeight = deltaY;
            } else {
                currentRow++; //当前行数
                perHeight +=  deltaY;
                totalHeight = Math.max(totalHeight, perHeight);
                totalWidth = Math.max(totalWidth, deltaX);
            }
        }

        //加上当前容器的padding值
        totalWidth += getPaddingLeft() + getPaddingRight();
        totalHeight += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(totalWidth, totalHeight);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        //pre为前面所有的child的相加后的位置
        int preLeft = getPaddingLeft();
        int preTop = getPaddingTop();

        int maxHeight = 0;//记录每一行的最高值
        int maxWidth = 0;//记录每一列的最宽值

        int currentRow = 1;//当前行

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            int currentWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
            int currentHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
            if (currentRow > maxRows) { //换列
                currentRow = 1; //重置行数
                preTop = getPaddingTop();//重置preTop
                preLeft = preLeft + maxWidth;//选择child的width最大的作为设置
                maxWidth = getChildAt(i).getMeasuredWidth() + params.leftMargin + params.rightMargin;
            } else {
                currentRow++;
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + params.leftMargin + params.rightMargin);
            }

            //left坐标
            int left = preLeft + params.leftMargin;
            //top坐标
            int top = preTop + params.topMargin;
            int right = left + child.getMeasuredWidth();
            int bottom = top + child.getMeasuredHeight();

            //为子view布局
            child.layout(left, top, right, bottom);
            //计算布局结束后，preLeft的值
//            preLeft += params.leftMargin + child.getMeasuredWidth() + params.rightMargin;
            preTop += params.topMargin + child.getMeasuredHeight() + params.bottomMargin;
        }


    }

//    @Override
//    public void addView(View child) {
//        if (child instanceof RadioButton) {
//            radioButtons.add((RadioButton) child);
//        } else {
//            throw new IllegalArgumentException("Child views must be instances of RadioButton");
//        }
//        requestLayout();
//    }
//
//    @Override
//    public void removeView(View view) {
//        if (view instanceof RadioButton) {
//            radioButtons.remove(view);
//        }
//        super.removeView(view);
//        requestLayout();
//    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        requestLayout();
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setChildBackGround(Drawable drawableTop, Drawable drawableMiddle, Drawable drawableBottom) {
//        int childCount = getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View child = getChildAt(i);
//            if (1 % maxRows == 0) {
//                child.setBackgroundDrawable(drawableTop);
//            } else if (1 % maxRows == maxRows - 1 || i == childCount - 1) {
//                child.setBackgroundDrawable(drawableBottom);
//            } else {
//                child.setBackgroundDrawable(drawableMiddle);
//            }
//        }
//        requestLayout();
    }
}