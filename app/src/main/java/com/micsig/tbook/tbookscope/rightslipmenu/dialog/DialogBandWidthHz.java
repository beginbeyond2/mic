package com.micsig.tbook.tbookscope.rightslipmenu.dialog;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.top.view.frequency.TopUtilBandWidthHz;
import com.micsig.tbook.ui.top.view.frequency.TopViewBandWidthHzLarge;
import com.micsig.tbook.ui.top.view.frequency.TopViewBandWidthHzSmall;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2022-3-8 10:07
 */
public class DialogBandWidthHz extends AbsoluteLayout {
    private Context context;
    private TextView head, show;
    private TopViewBandWidthHzLarge topViewBandWidthHzLarge;
    private TopViewBandWidthHzSmall TopViewBandWidthHzSmall;
    private OnDismissListener onDismissListener;

    private ViewGroup rootViewGroup;

    private long timeMin = TopUtilBandWidthHz.DEFAULT_MIN_TIME;
    private long timeMax = TopUtilBandWidthHz.DEFAULT_MAX_TIME;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public DialogBandWidthHz(Context context) {
        this(context, null);
    }

    public DialogBandWidthHz(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogBandWidthHz(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    //[50, 342]	608	170
    private void initView() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_bandwidthhz, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listener();
                hide();
                return false;
            }
        });
        head = (TextView) rootViewGroup.findViewById(R.id.head);
        show = (TextView) rootViewGroup.findViewById(R.id.show);
        topViewBandWidthHzLarge = (TopViewBandWidthHzLarge) rootViewGroup.findViewById(R.id.scaleLargeView);
        topViewBandWidthHzLarge.setOnRulerChangedListener(onLargeRulerChangedListener);
        TopViewBandWidthHzSmall = (TopViewBandWidthHzSmall) rootViewGroup.findViewById(R.id.scaleSmallView);
        TopViewBandWidthHzSmall.setOnRulerChangedListener(onSmallRulerChangedListener);

        hide();
        RxBus.getInstance().getObservable(RxEnum.DIALOG_SCALE_CHANGED).subscribe(consumerScaleChanged);
    }

    public void listener() {
        if (onDismissListener != null) {
            long ns = TBookUtil.getPsFromTime(TopUtilBandWidthHz.getSFromHz(show.getText().toString())) / 1000;
            onDismissListener.onDismiss(TopUtilBandWidthHz.getHzFromS(TBookUtil.getTime3FromPs(ns * 1000 * 10)));
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BANDWIDTHHZ);
        Tools.PrintControlsLocation("DialogBandWidthHz",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BANDWIDTHHZ);
    }

    public void setValue(String headString, String hzValue, long hzMin, long hzMax, OnDismissListener onDismissListener) {
        long timeMin = hzMin * 10;
        long timeMax = hzMax * 10;
//        this.timeMin = Math.min(timeMin, TopUtilBandWidthHz.TIME_US2NS);
//        this.timeMax = Math.max(timeMax, TopUtilBandWidthHz.TIME_S2NS);
        this.timeMin = timeMin;
        this.timeMax = timeMax;
        long ns = TopUtilBandWidthHz.getNSFromValue(TopUtilBandWidthHz.getSFromHz(hzValue));
        ns = ns < timeMin ? timeMin : ns;
        ns = ns > timeMax ? timeMax : ns;
        head.setText(headString);
        setValue(ns, onDismissListener);
    }

    /**
     * @param ns 单位ns
     *           1us = 1000ns
     *           1ms = 1000 * 1000ns
     *           1s  = 1000 * 1000 * 1000ns
     */
    private void setValue(long ns, OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        TopUtilBandWidthHz.ScaleValue scaleValue = new TopUtilBandWidthHz().createScaleValue();
        TopUtilBandWidthHz.getValueFromNS(ns, scaleValue);
        show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(scaleValue.value) + scaleValue.itemUnit));
        topViewBandWidthHzLarge.setTimeRange(timeMin, timeMax);
        TopViewBandWidthHzSmall.setTimeRange(timeMin, timeMax);
        topViewBandWidthHzLarge.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue);
        TopViewBandWidthHzSmall.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue / 100);
        show();
    }

    private Consumer<Integer> consumerScaleChanged = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) {
                case TopUtilBandWidthHz.ACTION_SCALE_FINISH:
                    listener();
                    hide();
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_LARGE_LEFT:
                    topViewBandWidthHzLarge.moveLeftOneStep();
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_LARGE_RIGHT:
                    topViewBandWidthHzLarge.moveRightOneStep();
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_SMALL_LEFT:
                    TopViewBandWidthHzSmall.moveLeftOneStep();
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_SMALL_RIGHT:
                    TopViewBandWidthHzSmall.moveRightOneStep();
                    break;
            }
        }
    };

    private TopViewBandWidthHzLarge.OnRulerChangedListener onLargeRulerChangedListener = new TopViewBandWidthHzLarge.OnRulerChangedListener() {
        @Override
        public void rulerChanged(double value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilBandWidthHz.UNIT_NS.equals(unit)) {
                value = Math.max(value, timeMin);
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 10 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / TopUtilBandWidthHz.TIME_US2NS);
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > 10 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 100 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / (10 * TopUtilBandWidthHz.TIME_US2NS));
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > 100 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 1000 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / (100 * TopUtilBandWidthHz.TIME_US2NS));
            }

            if (TopUtilBandWidthHz.UNIT_S.equals(unit)) {
                value = Math.min(value, 1.0 * timeMax / TopUtilBandWidthHz.TIME_S2NS);
            }
            show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(value) + unit));
            TopViewBandWidthHzSmall.setValue(value, unit, item / 100);
            listener();
        }
    };

    private TopViewBandWidthHzSmall.OnRulerChangedListener onSmallRulerChangedListener = new TopViewBandWidthHzSmall.OnRulerChangedListener() {
        @Override
        public void rulerChanged(String value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilBandWidthHz.UNIT_NS.equals(unit)) {
                value = String.valueOf(Math.max(Double.parseDouble(value), timeMin));
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 10 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / TopUtilBandWidthHz.TIME_US2NS));
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > 10 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 100 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / (10 * TopUtilBandWidthHz.TIME_US2NS)));
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)
                    && timeMin > 100 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 1000 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / (100 * TopUtilBandWidthHz.TIME_US2NS)));
            }
            if (TopUtilBandWidthHz.UNIT_S.equals(unit)) {
                value = String.valueOf(Math.min(Double.parseDouble(value), 1.0 * timeMax / TopUtilBandWidthHz.TIME_S2NS));
            }
            show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(Double.parseDouble(value)) + unit));
            topViewBandWidthHzLarge.setValue(Double.parseDouble(value), unit, item * 100);
            listener();
        }
    };

    public boolean contains(MotionEvent event){
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            int x=(int)event.getRawX();
            int y=(int)event.getRawY();
            Rect rect= Tools.getViewRect(this);
            if (rect.contains(x,y)){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}
