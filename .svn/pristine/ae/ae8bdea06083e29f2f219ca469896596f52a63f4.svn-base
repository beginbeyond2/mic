package com.micsig.tbook.tbookscope.top.popwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.micsig.base.DoubleUtil;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.measure.MeasureBean;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/4/25.
 */

public class TopDialogMeasureTValue extends AbsoluteLayout {
    private Context context;
    private RadioButton head;
    private TopViewRadioGroup rValueCursor;
    private TopViewEdit tvalue_voltage,tvalue_edge_occurence;

    protected TopDialogNumberKeyBoard dialogKeyBoard;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    private MeasureBean measureBean;
    private OnSureListener onSureListener;
    private ViewGroup rootViewGroup;

    public interface OnSureListener {
        void onSure(MeasureBean measureBean, double vol, int edgeOccurence, int cursorIndex);
    }

    public TopDialogMeasureTValue(Context context) {
        this(context, null);
    }

    public TopDialogMeasureTValue(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogMeasureTValue(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_measuretvalue, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });
        head = (RadioButton) rootViewGroup.findViewById(R.id.head);
        TextView sure = (TextView) rootViewGroup.findViewById(R.id.sure);
        sure.setOnClickListener(onClickListener);
        tvalue_voltage = rootViewGroup.findViewById(R.id.tvalue_voltage);
        tvalue_edge_occurence = rootViewGroup.findViewById(R.id.tvalue_edge_occurence);
        rValueCursor = findViewById(R.id.tvalue_cursor);
        rValueCursor.setData(R.string.tValueCursor, R.array.tValueCursor, onCheckChangedListener);
        tvalue_voltage.setOnClickEditListener(onClickEditListener);
        tvalue_edge_occurence.setOnClickEditListener(onClickEditListener);

        hide();
    }

    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if(dialogFloatKeyBoard == null) {
                dialogFloatKeyBoard =  ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
            }
            if (v.getId() == tvalue_voltage.getId()) {
                dialogFloatKeyBoard.setFloatData_Offset(tvalue_voltage.getText().replace("A", "")
                                .replace("V", "").replace(" ", ""),
                        true, tvalue_voltage, new TopDialogFloatKeyBoard.OnDismissListener() {
                    @Override
                    public void onDismiss(View fromView, String show) {
                        PlaySound.getInstance().playButton();
                        tvalue_voltage.setText(show);
                    }
                });

            } else if (v.getId() == tvalue_edge_occurence.getId()) {
                dialogFloatKeyBoard.setNumberData(tvalue_edge_occurence.getText().trim(), true, 7, tvalue_edge_occurence, new TopDialogFloatKeyBoard.OnDismissListener() {
                    @Override
                    public void onDismiss(View fromView, String result) {
                        PlaySound.getInstance().playButton();
                        tvalue_edge_occurence.setText(result);
                    }
                });
            }
        }
    };

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASURE_TVALUE);
        Tools.PrintControlsLocation("TopDialogMeasureTValue",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASURE_TVALUE);
    }

    public int getExKeysPosition() {
        //return otherChView.getSelected() == null ? otherMathView.getSelected().getIndex() + channelCount : otherChView.getSelected().getIndex();
        return 0;
    }

    public void setCache(double vol, int edgeOccurence, int cursorIndex) {
        tvalue_voltage.setText(String.valueOf(vol));
        tvalue_edge_occurence.setText(String.valueOf(edgeOccurence));
        rValueCursor.setSelectedIndex(cursorIndex);
    }

    public void setData(String selfChannel, MeasureBean measureBean, OnSureListener onSureListener) {
        this.measureBean = measureBean;
        this.onSureListener = onSureListener;
        head.setText(selfChannel);
        show();
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hide();
            ScreenUtil.getViewLocation(v);
            if (onSureListener != null) {
                onSureListener.onSure(measureBean,
                        TBookUtil.getDoubleFromM(tvalue_voltage.getText()),
                        (int) TBookUtil.getDoubleFromM(tvalue_edge_occurence.getText()),
                        rValueCursor.getSelected().getIndex()
                );
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

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item, false);
        }
    };


    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFormHardware) {
        if (view.getId() == rValueCursor.getId()) {
            rValueCursor.setSelectedIndex(item.getIndex());
        }
    }


}
