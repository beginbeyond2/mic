package com.micsig.tbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwb on 2017/4/11.
 */

public class MButton_RadioGroup extends ViewGroup {

    private View view;
    private List<MButton_CheckBox> list = new ArrayList<MButton_CheckBox>();

    //region 属性
    private OnClickListener onClickListener = null;

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    //endregion
    public MButton_RadioGroup(Context context) {
        this(context, null);
    }

    public MButton_RadioGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MButton_RadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton_RadioGroup, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MButton_RadioGroup_uiLayout) {

                int id = a.getResourceId(attr, -1);
                if (id == -1) throw new RuntimeException("资源没有被找到，请设置布局");
                view = inflate(context, id, this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                this.setLayoutParams(lp);
            }
        }

        init(this.view);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 记录总高度
        int mTotalHeight = 0;
        // 遍历所有子视图
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);

            // 获取在onMeasure中计算的视图尺寸
            int mLeft = childView.getLeft();
            int mTop = childView.getTop();
            int measureHeight = childView.getMeasuredHeight();
            int measuredWidth = childView.getMeasuredWidth();
//            childView.layout(l, mTotalHeight, measuredWidth, mTotalHeight
//                    + measureHeight);
            childView.layout(mLeft, mTop, measuredWidth, measureHeight);
            mTotalHeight += measureHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        if (childCount == 0) {
            setMeasuredDimension(0, 0);
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            width = childCount * getChildAt(0).getMeasuredWidth();
            height = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(width, height);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = childCount * getChildAt(0).getMeasuredWidth();
            setMeasuredDimension(width, height);
        } else {
            height = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //遍历按钮 到记录
    private void init(View v) {
        ViewGroup viewGroup = (ViewGroup) v;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof MButton_CheckBox) {
                list.add((MButton_CheckBox) view);
                ((MButton_CheckBox) view).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dealOnClick(v);
                    }
                });
                continue;
            }
            if (view instanceof ViewGroup) {
                init(view);
            }
        }
    }

    private void dealOnClick(View v) {
        MButton_CheckBox check = (MButton_CheckBox) v;
        if (!((MButton_CheckBox) v).isChecked()) {
            check.setChecked(true);
            return;
        }
        for (MButton_CheckBox m : list) {
            if (m != v) {
                m.setChecked(false);
            }
        }
        if (onClickListener != null) onClickListener.onClick(v);
    }
}
