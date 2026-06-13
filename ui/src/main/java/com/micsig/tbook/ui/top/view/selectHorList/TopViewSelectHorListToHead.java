package com.micsig.tbook.ui.top.view.selectHorList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by Administrator on 2017/4/5.
 */
public class TopViewSelectHorListToHead extends LinearLayout {
    private Context context;
    private String head;
    private String show;
    private TextView tvShow;
    private View.OnClickListener onClickListener;

    public TopViewSelectHorListToHead(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public TopViewSelectHorListToHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        View.inflate(context, R.layout.view_selecthorizontallistwithhead, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setData(String head, String show, View.OnClickListener listener) {
        this.head = head;
        this.show = show;
        this.onClickListener = listener;
        updateView();
    }

    public void setData(int headResId, int showResId, View.OnClickListener listener) {
        this.head = context.getString(headResId);
        this.show = context.getString(showResId);
        this.onClickListener = listener;
        updateView();
    }

    private void updateView() {
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvShow = (TextView) findViewById(R.id.show);
        tvTitle.setText(head);
        tvShow.setText(show);
        tvShow.setOnClickListener(onClickListener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tvShow.setEnabled(enabled);
    }

    public void setText(String showString) {
        if (StrUtil.isEmpty(showString)) {
            showString = "---";
        }
        tvShow.setText(showString);
    }

    public String getText() {
        if ("---".equals(tvShow.getText().toString())) {
            return "";
        }
        return tvShow.getText().toString();
    }

    public TextView getTvShow() {
        return tvShow;
    }
}
