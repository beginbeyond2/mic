package com.micsig.tbook.ui.top.view.selectHorList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/4/5.
 */
public class TopViewSelectHorListToList extends AbsoluteLayout {
    private Context context;
    private View view;
    private LinearLayout showLayout;
    //    private TopViewHorizontalPicker picker;
    private EHorizontalSelectedView horSelectedView;
    private int topViewSelectHorizontalListToHeadId;
    private String[] array;
    private OnDialogChangedListener onDialogChangedListener;

    public interface OnDialogChangedListener {
        void checkChanged(int headViewId, TopBeanHorizontal item);

        void onShow();

        void onHide();
    }

    public TopViewSelectHorListToList(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public TopViewSelectHorListToList(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    //1, 183, 700, 34
    private void initView() {
        view = inflate(context, R.layout.view_selecthorizontallistwithlist, this);
        showLayout = (LinearLayout) view.findViewById(R.id.showLayoutHorList);
        view.findViewById(R.id.outView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        setVisibility(GONE);
    }

    public void setData(int topViewSelectHorizontalListToHeadId, int arrayResId, OnDialogChangedListener onDialogChangedListener) {
        setData(topViewSelectHorizontalListToHeadId, context.getResources().getStringArray(arrayResId), onDialogChangedListener);
    }

    public void setData(int topViewSelectHorizontalListToHeadId, String[] array, OnDialogChangedListener onDialogChangedListener) {
        this.topViewSelectHorizontalListToHeadId = topViewSelectHorizontalListToHeadId;
        this.array = array;
        this.onDialogChangedListener = onDialogChangedListener;
        updateView();
        if (getVisibility() == VISIBLE) {
            hide();
        }
    }

    private void updateView() {
//        picker = (TopViewHorizontalPicker) view.findViewById(R.id.picker);
//        CharSequence[] charSequences = array;
//        picker.setSelectedItem(0);
//        picker.setValues(charSequences);
//        picker.setOnItemSelectedListener(onItemSelected);

        horSelectedView = (EHorizontalSelectedView) findViewById(R.id.hsv);
        horSelectedView.setData(new ArrayList<>(Arrays.asList(array)));
        horSelectedView.setSelectIndex(0);
        horSelectedView.setSeeSize(7);
        horSelectedView.setOnRollingListener(onItemSelected);
    }

    public void show(int layoutParamsY) {
        AbsoluteLayout.LayoutParams layoutParams = (LayoutParams) showLayout.getLayoutParams();
        layoutParams.y = layoutParamsY;
        showLayout.setLayoutParams(layoutParams);
        setVisibility(VISIBLE);
        if (onDialogChangedListener != null) {
            onDialogChangedListener.onShow();
        }
    }

    public void hide() {
        setVisibility(GONE);
        if (onDialogChangedListener != null) {
            onDialogChangedListener.onHide();
        }
    }

    public TopBeanHorizontal getSelected() {
        int index = horSelectedView.getSelectIndex();
        return new TopBeanHorizontal(index, array[index]);
    }

    public void moveLeftOneStep() {
        int index = horSelectedView.getSelectIndex();
        if (index != 0) {
            setSelected(index - 1);
        }
    }

    public void moveRightOneStep() {
        int index = horSelectedView.getSelectIndex();
        if (index != array.length - 1) {
            setSelected(index + 1);
        }
    }

    public void setSelected(int index) {
        horSelectedView.setSelectIndex(index);
    }

    /**
     * 根据所设置的时间ms数来获得具体bean类
     */
    public TopBeanHorizontal getBean(int ms) {
        int index = 0;
        if (ms < TBookUtil.getMsFromTime(array[0])) {
            index = 0;
        } else if (ms > TBookUtil.getMsFromTime(array[array.length - 1])) {
            index = array.length - 1;
        } else {
            for (int i = 0; i < array.length - 2; i++) {
                if (ms < TBookUtil.getMsFromTime(array[i + 1])) {
                    index = i;
                    break;
                }
            }
        }
        return new TopBeanHorizontal(index, array[index]);
    }

    /**
     * 根据所设置的时间ms数来设置具体数值
     */
    public void setSelectedMS(int ms) {
        if (ms < TBookUtil.getMsFromTime(array[0])) {
            setSelected(0);
        } else if (ms > TBookUtil.getMsFromTime(array[array.length - 1])) {
            setSelected(array.length - 1);
        } else {
            for (int i = 0; i < array.length - 2; i++) {
                if (ms < TBookUtil.getMsFromTime(array[i + 1])) {
                    setSelected(i);
                    break;
                }
            }
        }
    }

//    private TopViewHorizontalPicker.OnItemSelected onItemSelected = new TopViewHorizontalPicker.OnItemSelected() {
//        @Override
//        public void onItemSelected(int index) {
//            if (onDialogChangedListener != null) {
//                onDialogChangedListener.checkChanged(topViewSelectHorizontalListToHeadId, new TopBeanHorizontal(index, array[index]));
//            }
//        }
//    };

    private EHorizontalSelectedView.OnRollingListener onItemSelected = new EHorizontalSelectedView.OnRollingListener() {
        @Override
        public void onRolling(int position, String s) {
            Logger.i("移动到的是 :  " + s + " ,position= " + position);
            if (onDialogChangedListener != null) {
                onDialogChangedListener.checkChanged(topViewSelectHorizontalListToHeadId, new TopBeanHorizontal(position, s));
            }
        }

        @Override
        public void onClick(int position, String s) {
            Logger.i("点击到的是 :  " + s + " ,position= " + position);
            if (onDialogChangedListener != null) {
                onDialogChangedListener.checkChanged(topViewSelectHorizontalListToHeadId, new TopBeanHorizontal(position, s));
            }
        }
    };
}
