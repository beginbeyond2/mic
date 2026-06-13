package com.micsig.tbook.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.R;


public class MainLayoutSegmentedFit extends LinearLayout {
    private Context context;
    private LinearLayout layoutFit;
    private TextView tvFrame;
    private TextView tvTime;

    public MainLayoutSegmentedFit(Context context) {
        this(context, null);
    }

    public MainLayoutSegmentedFit(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutSegmentedFit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        View inflate = inflate(context, R.layout.view_segmentedfit, this);
        layoutFit = (LinearLayout) inflate.findViewById(R.id.dialogFitLayout);
        tvFrame = (TextView) inflate.findViewById(R.id.dialogFitFrame);
        tvTime = (TextView) inflate.findViewById(R.id.dialogFitTime);
        layoutFit.setBackgroundResource(R.drawable.shape_frame_bg_black);
    }

    public void setBean(SegmentedSingleBean bean) {
        tvFrame.setText(String.valueOf(bean.getFrameId()));
        tvTime.setText(String.valueOf(bean.getTimeMs()));
    }

    public SegmentedSingleBean getBean() {
        return new SegmentedSingleBean(
                Integer.valueOf(tvFrame.getText().toString()), tvTime.getText().toString());
    }
}
