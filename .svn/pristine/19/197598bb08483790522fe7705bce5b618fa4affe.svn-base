package com.micsig.tbook.tbookscope.top.popwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.measure.MeasureBean;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/4/25.
 */

public class TopDialogMeasureDelay extends AbsoluteLayout {
    private Context context;
    private RadioButton head;
    private TopViewRadioGroup selfPosView, otherChView, otherMathView, otherPosView;
    private MeasureBean measureBean;
    private OnSureListener onSureListener;

    private ViewGroup rootViewGroup;
    private final int channelCount = GlobalVar.get().getChannelsCount();
    public interface OnSureListener {
        void onSure(MeasureBean measureBean, String selfChannel, int selfPosition, int otherChannel, int otherPosition);
    }

    public TopDialogMeasureDelay(Context context) {
        this(context, null);
    }

    public TopDialogMeasureDelay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogMeasureDelay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_measuredelay, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });

        head = (RadioButton) rootViewGroup.findViewById(R.id.head);
        TextView sure = (Button) rootViewGroup.findViewById(R.id.sure);
        selfPosView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.selfPosition);
        selfPosView.setData(null, context.getResources().getStringArray(R.array.measureDelayPosition), onCheckChangedListener);
        otherChView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherChannel);
        otherMathView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherMath);
        String[] channel1 = GlobalVar.get().getChannelsName();
        String[] channel2 = context.getResources().getStringArray(R.array.measureDelayToChannel);
        otherChView.setData(null, channel1, onCheckChangedListener);
        otherMathView.setData(null, channel2, onCheckChangedListener);
        otherPosView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherPosition);
        otherPosView.setData(null, context.getResources().getStringArray(R.array.measureDelayPosition), onCheckChangedListener);
        sure.setOnClickListener(onClickListener);

        hide();
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASUREDELAY);
        Tools.PrintControlsLocation("TopDialogMeasureDelay",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASUREDELAY);
    }

    public int getExKeysPosition() {
        return selfPosView.getSelected().getIndex();
    }

    public void setCache(int selfPosCache, int otherChCache, int otherPosCache) {
        selfPosView.setSelectedIndex(selfPosCache);
        if (otherChCache < channelCount) {
            otherChView.setSelectedIndex(otherChCache);
            otherMathView.clearCheck();
        } else {
            otherChView.clearCheck();
            otherMathView.setSelectedIndex(otherChCache - channelCount);
        }
        otherPosView.setSelectedIndex(otherPosCache);
    }

    public void setData(String selfChannel, MeasureBean measureBean, OnSureListener onSureListener) {
        this.measureBean = measureBean;
        this.onSureListener = onSureListener;
        head.setText(selfChannel);
        show();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hide();
            ScreenUtil.getViewLocation(v);
            if (onSureListener != null) {
                int otherChannel = otherChView.getSelected() == null ? otherMathView.getSelected().getIndex() + channelCount : otherChView.getSelected().getIndex();
                onSureListener.onSure(measureBean, head.getText().toString(), selfPosView.getSelected().getIndex(), otherChannel, otherPosView.getSelected().getIndex());
            }
        }
    };


    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            switch (view.getId()) {
                case R.id.selfPosition:
                    break;
                case R.id.otherChannel:
                    otherMathView.clearCheck();
                    break;
                case R.id.otherMath:
                    otherChView.clearCheck();
                    break;
                case R.id.otherPosition:
                    break;
            }
        }
    };
}
