package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.cardemulation.HostNfcFService;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathDualWave;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.functions.Consumer;

public class DialogSetChannelInfo extends ConstraintLayout {

    private static final String TAG = "DialogSetChannelInfo";
    private final Context context;
    private ConstraintLayout rootViewGroup;
    private OnDismissListener dismissListener;
    private TopViewEdit verScale, verPosition, horScale, horPosition;
    private TextView chTitle;
    private int chIdx;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;

    public interface OnDismissListener {
        void onDismiss(int chIdx, HashMap<Integer, String> infoMap);
    }

    public DialogSetChannelInfo(Context context) {
        this(context, null);
    }

    public DialogSetChannelInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogSetChannelInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initControl() {

    }

    public void initView() {
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_set_channel_info, this);
//        View outView = rootViewGroup.findViewById(R.id.outView);
//        outView.setOnClickListener(onClickListener);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });

        verScale = findViewById(R.id.ver_scale);
        verPosition = findViewById(R.id.ver_position);
        horScale = findViewById(R.id.hor_scale);
        horPosition = findViewById(R.id.hor_position);
        chTitle = findViewById(R.id.channel_title);

        verScale.setOnClickEditListener(onClickEditListener);
        verPosition.setOnClickEditListener(onClickEditListener);
        horScale.setOnClickEditListener(onClickEditListener);
        horPosition.setOnClickEditListener(onClickEditListener);
    }

//    @SuppressLint("NonConstantResourceId")
//    private final View.OnClickListener onClickListener = v -> {
//        switch (v.getId()) {
//            case R.id.outView:
//                hide();
//                break;
//        }
//    };

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_SET_CHANNEL_INFO);
        Tools.PrintControlsLocation(TAG, rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        clearContentInfo();
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_SET_CHANNEL_INFO);
    }


    public void setData(int chIdx, OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
        this.chIdx = chIdx;
        setContentInfo(chIdx);
        show();
    }

    private void clearContentInfo() {
        chTitle.setText("");
        verScale.setText("");
        verPosition.setText("");
        horScale.setText("");
        horPosition.setText("");
    }

    private void setContentInfo(int chIdx) {
        String chName = ChannelFactory.getChannelName(chIdx);
        chTitle.setTextColor(SvgNodeInfo.getAllBaseColorInt(TChan.toUiChNo(chIdx)));
        chTitle.setText(chName);

        verScale.setVisibility(View.VISIBLE);
        verPosition.setVisibility(View.VISIBLE);
        horScale.setVisibility(View.VISIBLE);
        horPosition.setVisibility(View.VISIBLE);

        if (ChannelFactory.isDynamicCh(chIdx)) { //模拟通道
            setChInfo(chIdx);
        } else if (ChannelFactory.isRefCh(chIdx)) {//参考通道
            setRefInfo(chIdx);
        } else if (ChannelFactory.isMathCh(chIdx)) {//数学通道
            serMathInfo(chIdx);
        }
    }

    private void serMathInfo(int chIdx) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);
        if (mathChannel == null || !mathChannel.isOpen()) return;
        //垂直档位
        String unit = ChannelFactory.getProbeType(chIdx);
        double vScaleVal = mathChannel.getVScaleVal();
        verScale.setText(TBookUtil.getMFromDouble(vScaleVal) + unit);

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos;
        String number = TBookUtil.getFourFromD_Trim0(pos * mathChannel.getVerticalPerPix());
        verPosition.setText(number + unit);

        //水平档位
        String timeBaseScale;
        if (ChannelFactory.isMath_FFT_Ch(chIdx)) {
            timeBaseScale = TBookUtil.getMFromDouble(mathChannel.getHorizontalAxisMathFFT().fftXScaleIdVal()) + "Hz";
        } else {
            timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);
        }
        horScale.setText(timeBaseScale);


        //水平位置
        long timePos = HorizontalAxis.getInstance().getTimePosOfView();
        if (ChannelFactory.isMath_FFT_Ch(chIdx)) {
            horPosition.setVisibility(View.GONE);
        } else {
            horPosition.setText(TBookUtil.getSFrom100Fs(timePos));
        }
    }

    private void setChInfo(int chIdx) {
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if (channel == null || !channel.isOpen()) return;

        //垂直档位
        double chxVal = channel.getVScaleVal();
        String unit = Tools.getChanProbeTypeUnit(channel) == 0 ? "V" : "A";
        verScale.setText(TBookUtil.getMFromDouble(chxVal) + unit);

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos;
        String number = TBookUtil.getFourFromD_Trim0(pos * channel.getVerticalPerPix());
        verPosition.setText(number + unit);

        //水平档位
        String timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);
        horScale.setText(timeBaseScale);

        //水平位置
        long timePos = HorizontalAxis.getInstance().getTimePosOfView();
        horPosition.setText(TBookUtil.getSFrom100Fs(timePos));
    }


    private void setRefInfo(int chIdx) {
        RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
        if (refChannel == null || !refChannel.isOpen()) return;

        //垂直档位
        double extent = refChannel.getVScaleIdVal();
        String unit = ChannelFactory.getProbeType(chIdx);
        verScale.setText(TBookUtil.getMFromDouble(extent) + unit);

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos;
        String number = TBookUtil.getFourFromD_Trim0(pos * refChannel.getVerticalPerPix());
        verPosition.setText(number + unit);

        //水平档位
        String timeBase = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.toUiChNo(chIdx));
        horScale.setText(timeBase);

        //水平位置
        int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        String timePosStr = "";
        if (refChannel.getRefType() != WaveData.FFT_WAVE) {
            long timePos;
            if (followCh == 0) { //跟随模拟通道
                timePos = HorizontalAxis.getInstance().getTimePosOfView();
            } else {
                timePos = refChannel.getTimePosOfView();
            }
            timePosStr = TBookUtil.getSFrom100Fs(timePos);
        } else {
            horPosition.setVisibility(View.GONE);
        }
        horPosition.setText(timePosStr);
    }


    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            if (dialogFloatKeyBoard == null) {
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
            }
            if (v.getId() == verScale.getId()) {
                setChVScale(verScale);
            } else if (v.getId() == verPosition.getId()) {
                setChVPosition(verPosition);
            } else if (v.getId() == horScale.getId()) {
                setChHScale(horScale);
            } else if (v.getId() == horPosition.getId()) {
                setChHPosition(horPosition);
            }
        }
    };

    //水平位置
    private void setChHPosition(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();
        String txt = topViewEdit.getText().replace("s", "").replace(" ", "");
        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh hposition= " + show);
                PlaySound.getInstance().playButton();
                topViewEdit.setText(show + "s");

                double val = TBookUtil.getDoubleFromM(show);
                Command.get().getTimebase().Position(val, true);
            }
        });
    }

    //水平档位
    private void setChHScale(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();
        String txt = topViewEdit.getText().replace("s", "").replace(" ", "");
        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh hScale= " + show);
                PlaySound.getInstance().playButton();
                topViewEdit.setText(show + "s");

                double val = TBookUtil.getDoubleFromM(show);
                Command.get().getTimebase().Extent(-1, val, true);
            }
        });
    }

    //垂直位置
    private void setChVPosition(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();

        String txt;
        if (ChannelFactory.isMathCh(chIdx)) {
            String unit = ChannelFactory.getProbeType(chIdx);
            txt = topViewEdit.getText().replace(unit, "").replace(" ", "");
        } else {
            txt = topViewEdit.getText().replace("V", "").replace("A", "").replace(" ", "");
        }

        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh vposition= " + show);
                PlaySound.getInstance().playButton();
                String unit = ChannelFactory.getProbeType(chIdx);
                topViewEdit.setText(show + unit);

                double val = TBookUtil.getDoubleFromM(show);
                if (ChannelFactory.isDynamicCh(chIdx)) {
                    Command.get().getChannel().Position(chIdx, val, true);
                } else if (ChannelFactory.isRefCh(chIdx)) {
                    Command.get().getReference().Position(chIdx, val, true);
                } else if (ChannelFactory.isMathCh(chIdx)) {
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                    switch (mathType) {
                        case CacheUtil.MATHTYPE_DW:
                            Command.get().getMath_base().Offset(chIdx, val, true);
                            break;
                        case CacheUtil.MATHTYPE_FFT:
                            Command.get().getMath_fft().Offset(chIdx, val, true);
                            break;
                        case CacheUtil.MATHTYPE_AXB:
                            Command.get().getMath_axb().Offset(chIdx, val, true);
                            break;
                        case CacheUtil.MATHTYPE_AM:
                            Command.get().getMath_advanced().Offset(chIdx, val, true);
                            break;
                    }
                }
            }
        });

    }

    //垂直档位
    private void setChVScale(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();

        String txt;
        if (ChannelFactory.isMathCh(chIdx)) {
            String unit = ChannelFactory.getProbeType(chIdx);
            txt = topViewEdit.getText().replace(unit, "").replace(" ", "");
        } else {
            txt = topViewEdit.getText().replace("V", "").replace("A", "").replace(" ", "");
        }
        dialogFloatKeyBoard.setFloatData_Extent(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh vscale= " + show);
                PlaySound.getInstance().playButton();
//                Channel channel = ChannelFactory.getDynamicChannel(chIdx);
//                String unit = channel.getProbeType() == VerticalAxis.PROBE_TYPE_VOL ? "V" : "A";
                String unit = ChannelFactory.getProbeType(chIdx);
                topViewEdit.setText(show + unit);
                double d = TBookUtil.getDoubleFromM(show);

                if (ChannelFactory.isDynamicCh(chIdx)){
                    Command.get().getChannel().Extent(chIdx, d, true);
                } else if (ChannelFactory.isRefCh(chIdx)) {
                    Command.get().getReference().Vscale(chIdx, d, true);
                } else if (ChannelFactory.isMathCh(chIdx)) {
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                    switch (mathType) {
                        case CacheUtil.MATHTYPE_DW:
                            Command.get().getMath_base().Extent(chIdx, d, true);
                            break;
                        case CacheUtil.MATHTYPE_FFT:
                            Command.get().getMath_fft().Extent(chIdx, d, true);
                            break;
                        case CacheUtil.MATHTYPE_AXB:
                            Command.get().getMath_axb().Extent(chIdx, d, true);
                            break;
                        case CacheUtil.MATHTYPE_AM:
                            Command.get().getMath_advanced().Extent(chIdx, d, true);
                            break;
                    }
                }
            }
        });
    }


    /**
     * 返回有效档位值
     */
    private double getVerticalRange(double input, Channel channel) {
        double min = 0;
        double max = 0;

        min = VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear()) * channel.getProbeRate();
        max = VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear());
        if (channel.getResistanceType() == Channel.RESISTANCE_50) {
            max = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1V);
        }
        max *= channel.getProbeRate();

        if (input < min) {
            input = min;
        }
        if (input > max) {
            input = max;
        }
        return input;
    }

}
