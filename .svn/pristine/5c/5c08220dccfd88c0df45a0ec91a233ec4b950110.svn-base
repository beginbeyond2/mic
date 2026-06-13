package com.micsig.tbook.tbookscope.top.popwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.top.view.scale.TopUtilScale;
import com.micsig.tbook.ui.top.view.scale.TopViewScaleLarge;
import com.micsig.tbook.ui.top.view.scale.TopViewScaleSmall;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopDialogScale extends AbsoluteLayout {
    private Context context;
    private TextView head, show;
    private TopViewScaleLarge topViewScaleLarge;
    private TopViewScaleSmall topViewScaleSmall;
    private OnDismissListener onDismissListener;

    private ViewGroup rootViewGroup;
    private long timeMin = TopUtilScale.DEFAULT_MIN_TIME;
    private long timeMax = TopUtilScale.DEFAULT_MAX_TIME;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public TopDialogScale(Context context) {
        this(context, null);
    }

    public TopDialogScale(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogScale(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    //[50, 342]	608	170
    private void initView() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_scale, this);

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v)->{
            listener();
            hide();
        });
        head = (TextView) rootViewGroup.findViewById(R.id.head);
        show = (TextView) rootViewGroup.findViewById(R.id.show);
        topViewScaleLarge = (TopViewScaleLarge) rootViewGroup.findViewById(R.id.scaleLargeView);
        topViewScaleLarge.setOnRulerChangedListener(onLargeRulerChangedListener);
        topViewScaleSmall = (TopViewScaleSmall) rootViewGroup.findViewById(R.id.scaleSmallView);
        topViewScaleSmall.setOnRulerChangedListener(onSmallRulerChangedListener);

        hide();
        RxBus.getInstance().getObservable(RxEnum.DIALOG_SCALE_CHANGED).subscribe(consumerScaleChanged);
    }

    public void listener() {
        if (onDismissListener != null) {
            int minInterval = GlobalVar.get().getTimeMinInterval();
            long ns = TBookUtil.getPsFromTime(show.getText().toString()) / 1000;
            ns = ns - ns % minInterval;
            onDismissListener.onDismiss(TBookUtil.getTime3FromPs(ns * 1000 * 10));
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TOPSCALE);
        Tools.PrintControlsLocation("TopDialogScale",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TOPSCALE);
    }

    public void setValue(String headString, String value, long timeMin, long timeMax, OnDismissListener onDismissListener) {
        this.timeMin = Math.min(timeMin, TopUtilScale.TIME_US2NS);
        this.timeMax = Math.max(timeMax, TopUtilScale.TIME_S2NS);
        long ns = TopUtilScale.getNSFromValue(value);
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
        TopUtilScale.ScaleValue scaleValue = new TopUtilScale().createScaleValue();
        TopUtilScale.getValueFromNS(ns, scaleValue);
        show.setText(TBookUtil.getD3FromD(scaleValue.value) + scaleValue.itemUnit);
        topViewScaleLarge.setTimeRange(timeMin, timeMax);
        topViewScaleSmall.setTimeRange(timeMin, timeMax);
        topViewScaleLarge.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue);
        topViewScaleSmall.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue / 100);
        show();
    }

    private Consumer<Integer> consumerScaleChanged = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) {
                case TopUtilScale.ACTION_SCALE_FINISH:
                    listener();
                    hide();
                    break;
                case TopUtilScale.ACTION_SCALE_LARGE_LEFT:
                    topViewScaleLarge.moveLeftOneStep();
                    break;
                case TopUtilScale.ACTION_SCALE_LARGE_RIGHT:
                    topViewScaleLarge.moveRightOneStep();
                    break;
                case TopUtilScale.ACTION_SCALE_SMALL_LEFT:
                    topViewScaleSmall.moveLeftOneStep();
                    break;
                case TopUtilScale.ACTION_SCALE_SMALL_RIGHT:
                    topViewScaleSmall.moveRightOneStep();
                    break;
            }
        }
    };

    private TopViewScaleLarge.OnRulerChangedListener onLargeRulerChangedListener = new TopViewScaleLarge.OnRulerChangedListener() {
        @Override
        public void rulerChanged(double value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilScale.UNIT_NS.equals(unit)) {
                value = Math.max(value, timeMin);
            }
            if (TopUtilScale.UNIT_S.equals(unit)) {
                value = Math.min(value, timeMax / TopUtilScale.TIME_S2NS);
            }
            show.setText(TBookUtil.getD3FromD(value) + unit);
            topViewScaleSmall.setValue(value, unit, item / 100);
            listener();
        }
    };

    private TopViewScaleSmall.OnRulerChangedListener onSmallRulerChangedListener = new TopViewScaleSmall.OnRulerChangedListener() {
        @Override
        public void rulerChanged(String value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilScale.UNIT_NS.equals(unit)) {
                value = String.valueOf(Math.max(Double.parseDouble(value), timeMin));
            }
            if (TopUtilScale.UNIT_S.equals(unit)) {
                value = String.valueOf(Math.min(Double.parseDouble(value), timeMax / TopUtilScale.TIME_S2NS));
            }
            show.setText(TBookUtil.getD3FromD(Double.parseDouble(value)) + unit);
            topViewScaleLarge.setValue(Double.parseDouble(value), unit, item * 100);
            listener();
        }
    };
}
