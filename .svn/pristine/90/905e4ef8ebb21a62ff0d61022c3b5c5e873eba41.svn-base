package com.micsig.tbook.ui.top.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.Arrays;

/**
 * Created by yangj on 2017/4/21.
 */

public class TopViewEdit extends LinearLayout {
    private Context context;
    private String headText;
    private String editHintText;
    private int editWidth;
    private int headWidth;
    private int headTextSize;
    private int headMarginBottom;
    private boolean editCenter;
    private boolean orientationV; //是否垂直显示
    private boolean headViewLeft;//headView是否靠左显示
    private TextView headView;
    private TextView editView;
    private OnClickEditListener onClickEditListener;

    public interface OnClickEditListener {
        void onClickEdit(TopViewEdit v, String text);
    }

    public void setOnClickEditListener(OnClickEditListener onClickEditListener) {
        this.onClickEditListener = onClickEditListener;
    }

    public TopViewEdit(Context context) {
        this(context, null);
    }

    public TopViewEdit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_editwithhead, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewEdit);
        orientationV = ta.getBoolean(R.styleable.TopViewEdit_orientationV, false);
        headText = ta.getString(R.styleable.TopViewEdit_head);
        editHintText = ta.getString(R.styleable.TopViewEdit_editHint);
        editCenter = ta.getBoolean(R.styleable.TopViewEdit_editCenter, false);
        headViewLeft = ta.getBoolean(R.styleable.TopViewEdit_headViewGravityLeft, true);
        editWidth = ta.getDimensionPixelSize(R.styleable.TopViewEdit_editWidth, 350);
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewEdit_headWidth, 120);
        headTextSize=ta.getDimensionPixelSize(R.styleable.TopViewEdit_headTextSize,20);
        headMarginBottom = ta.getDimensionPixelSize(R.styleable.TopViewEdit_headMarginBottom, 10);
        ta.recycle();
        setOrientation(orientationV ? VERTICAL : HORIZONTAL);
        setGravity(orientationV ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL);
        headView = (TextView) findViewById(R.id.headView);
        headView.setTextSize(TypedValue.COMPLEX_UNIT_PX,headTextSize);
        if (headViewLeft) {
            headView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        } else {
            headView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        }
        editView = (TextView) findViewById(R.id.editView);
        editView .setTextSize(TypedValue.COMPLEX_UNIT_PX,headTextSize);
        editView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] ints = new int[2];
                v.getLocationOnScreen(ints);
                Logger.i("TopViewEdit:" + Arrays.toString(ints) + "\t" + v.getWidth() + "\t" + v.getHeight());
                if (onClickEditListener != null) {
                    onClickEditListener.onClickEdit(TopViewEdit.this, editView.getText().toString());
                }
            }
        });
        setData();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editView.setEnabled(enabled);
    }

    private void setData() {
        if (!StrUtil.isEmpty(headText)) {
            headView.setText(headText);
        }
        if (!StrUtil.isEmpty(editHintText)) {
            editView.setText(editHintText);
        }
        ViewGroup.LayoutParams editParams = editView.getLayoutParams();
        editParams.width = editWidth;
        editView.setLayoutParams(editParams);
        if (editCenter) {
            editView.setGravity(Gravity.CENTER);
        }
        LinearLayout.LayoutParams headParams = (LinearLayout.LayoutParams) headView.getLayoutParams();
        headParams.width = !StrUtil.isEmpty(headText) ? headWidth : 0;
        if (orientationV) {
            headParams.setMargins(0, 0, 0, headMarginBottom);
        }
        headView.setLayoutParams(headParams);
        if (orientationV) {
            headView.setGravity(Gravity.CENTER);
        }
    }

    public void setData(String headText, String editHintText) {
        this.headText = headText;
        this.editHintText = editHintText;
        setData();
    }

    public String getHead() {
        return headText;
    }

    public void setEdit(String editText) {
        setText(editText);
    }

    public String getText() {
        return editView.getText().toString().trim();
    }

    public void setText(String text) {
        editView.setText(text);
    }

    public void setEditColor(String color) {
        editView.setBackgroundColor(Color.parseColor(color));
    }

}
