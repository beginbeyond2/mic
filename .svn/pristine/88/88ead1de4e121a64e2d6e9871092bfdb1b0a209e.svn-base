package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.TimerUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


public class DialogMeasureStatics extends ConstraintLayout {
    private Context context;
    private TimerUtils timer;

    public DialogMeasureStatics(@NonNull Context context) {
        this(context, null);
    }

    public DialogMeasureStatics(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogMeasureStatics(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        timer = new TimerUtils(new TimeEvent());
        timer.setExecOne(false);
        timer.setIntervalMs(100);

        initControl();
    }

    private View mainView;
    private List<DialogMeasureStaticsItem> listTxtView = new ArrayList<>();
    private LinearLayout listView;
    private DialogMeasureStaticsItem title;
    private TextView line1,line2,line3,line4,line5,line6,line_title;
    private List<TextView> lineList=new ArrayList<>();
    private void initView() {
        View inflate = inflate(context, R.layout.dialog_measure_statics, this);
        mainView = inflate;
        listView = inflate.findViewById(R.id.listView);
        title = inflate.findViewById(R.id.title);

        for (int i = 0; i < GlobalVar.get().getMeasureItemCount(); i++) {
            DialogMeasureStaticsItem item = new DialogMeasureStaticsItem(context);
            item.OnTxtClickEvent = this::OnClickListener;
            listView.addView(item);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();
            params.width = 164;
            item.setLayoutParams(params);
            listTxtView.add(item);
        }

        title.setTitle();
        title.setTitleHide();

        line1=inflate.findViewById(R.id.row1);
        line2=inflate.findViewById(R.id.row2);
        line3=inflate.findViewById(R.id.row3);
        line4=inflate.findViewById(R.id.row4);
        line5=inflate.findViewById(R.id.row5);
        line6=inflate.findViewById(R.id.row6);
        line_title=inflate.findViewById(R.id.line_title);
        lineList.add(line1);
        lineList.add(line2);
        lineList.add(line3);
        lineList.add(line4);
        lineList.add(line5);
        lineList.add(line6);

    }

    private void OnClickListener(View view) {

        if (isMeasureItemClickEnable() == false) return;
        int idx = Tools.indexOf(listTxtView, (v) -> v.equals(view));
        if (idx < 0) return;
        MeasureManage.getInstance().getMeasureItem().setSelectItem(idx);

    }

    private boolean isMeasureItemClickEnable() {
        return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_BEGIN, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_END, eventUIObserver);
    }

    class TimeEvent implements TimerUtils.TimeOutEvent {
        @Override
        public void onTimeOut() {
            ((MainActivity) (DialogMeasureStatics.this.context)).runOnUiThread(() -> {
                startRefresh();
            });
        }
    }

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            if (workModeBean.getNextWorkMode() == IWorkMode.WorkMode_XY) {
//                setVisibility(INVISIBLE);
                setShowDialog(false);
            } else {
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);
                }
            }
        }
    };
    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
//                setVisibility(INVISIBLE);
                setShowDialog(false);
            } else {
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);
                }
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_BEGIN) {
//                setVisibility(INVISIBLE);
                setShowDialog(false);
            } else if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_END) {
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);
                }
            }
        }
    };


    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;
        if (ChannelFactory.isDynamicCh(chId)) {
            baseChannel = ChannelFactory.getDynamicChannel(chId);
        } else if (ChannelFactory.isMathCh(chId)) {
            baseChannel = ChannelFactory.getMathChannel(chId);
        } else if (ChannelFactory.isRefCh(chId)) {
            baseChannel = ChannelFactory.getRefChannel(chId);
        }
        if (baseChannel != null) {
            return baseChannel.getMeasure();
        }
        return null;
    }

    public void open() {
        timer.start();
    }

    public void close() {
        timer.stop();
    }

    private void clearTextContent() {
        for (int i = 0; i < listTxtView.size(); i++) {
            listTxtView.get(i).clearTxt();
        }
    }

    private String addUnit(int ch, int measureType, float val) {
        switch (measureType) {
            case MeasureManage.IMeasure.MeasureId_Freq:
                return TBookUtil.getFourFromD_(val) + "Hz";
            case MeasureManage.IMeasure.MeasureId_DutyAdd:
            case MeasureManage.IMeasure.MeasureId_DutySub:
            case MeasureManage.IMeasure.MeasureId_ROV:
            case MeasureManage.IMeasure.MeasureId_FOV:
                //%不显示m、k、M等前缀，保留2位小数
                return TBookUtil.getPoint2FromD_noscale(val * 100) + "%";
            case MeasureManage.IMeasure.MeasureId_Phase:
                String d = TBookUtil.getFourFromD_(val);
                if ("-0f".equals(d) || "0f".equals(d)) {
                    d = "0";
                }
                return d + "°";
            case MeasureManage.IMeasure.MeasureId_Period:
            case MeasureManage.IMeasure.MeasureId_RiseTime:
            case MeasureManage.IMeasure.MeasureId_FallTime:
            case MeasureManage.IMeasure.MeasureId_Delay:
            case MeasureManage.IMeasure.MeasureId_WidthAdd:
            case MeasureManage.IMeasure.MeasureId_WidthSub:
            case MeasureManage.IMeasure.MeasureId_BurstW:
            case MeasureManage.IMeasure.MeasureId_TVALUE:
                return TBookUtil.getFourFromD_(val) + "s";
            case MeasureManage.IMeasure.MeasureId_PKPK:
            case MeasureManage.IMeasure.MeasureId_Amp:
            case MeasureManage.IMeasure.MeasureId_High:
            case MeasureManage.IMeasure.MeasureId_Low:
            case MeasureManage.IMeasure.MeasureId_Max:
            case MeasureManage.IMeasure.MeasureId_Min:
            case MeasureManage.IMeasure.MeasureId_RMS:
            case MeasureManage.IMeasure.MeasureId_CRMS:
            case MeasureManage.IMeasure.MeasureId_Mean:
            case MeasureManage.IMeasure.MeasureId_CMean:
            case MeasureManage.IMeasure.MeasureId_ACRMS:
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch);
            case MeasureManage.IMeasure.MeasureId_PostitiveRate:
            case MeasureManage.IMeasure.MeasureId_NegativeRate:
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch) + "/s";
        }
        return "";
    }

    private void startRefresh() {
        updateView();
    }

    private void updateView() {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getValidMeasureList();
        title.UpdateParamVisible();
        for (int i = 0; i < listTxtView.size(); i++) {
            DialogMeasureStaticsItem txt = listTxtView.get(i);
            if (i >= list.size()) {
                txt.clearTxt();
                txt.setBackground(null);
                txt.setVisibility(GONE);
                continue;
            }
            txt.setVisibility(VISIBLE);
            txt.UpdateParamVisible();
            MeasureManage.MeasureItemStruct bean = list.get(i);
            txt.UpdateView(bean,isMeasureItemClickEnable());
        }


        if (list.size()>0){
            line_title.setVisibility(VISIBLE);
        }else {
            line_title.setVisibility(GONE);
        }
    }

    public void show(boolean isShowMean, boolean isShowMax, boolean isShowMin, boolean isShowDelta, boolean isShowCount, int measureCount, int height) {
        updateView();
        int sum = 1;
        if (isShowMean) sum++;
        if (isShowMax) sum++;
        if (isShowMin) sum++;
        if (isShowDelta) sum++;
        if (isShowCount) sum++;

        for(int i=0;i<lineList.size();i++){
            if (sum>i){
                lineList.get(i).setVisibility(VISIBLE);
                int color=i%2==0? Color.TRANSPARENT:Color.rgb(54,59,64);
                lineList.get(i).setBackgroundColor(color);
            }else {
                lineList.get(i).setVisibility(GONE);
            }

        }

        close();
        open();
        //Log.d("Tag.Debug", String.format("DialogMeasureStatics.show: %d",getHeight() ));

        // 40是标题高
        int titleHeight = measureCount == 0 ? 2 : 40;
        int offSet = 1;//位置变化的补偿值
//        if (height == 1040) {
//            offSet = 1;
//        } else if (height == 1000) {
//            offSet = 2;
//        }
        this.setY(1040 - titleHeight - sum * 34 - offSet);
        setShowDialog(true);
    }

    public void hide() {
        close();
        setShowDialog(false);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASURE_STATICS);
    }

    private void setShowDialog(boolean b){
        boolean visible = WorkModeManage.getInstance().isXyMode();
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        boolean result= b && (visible==false && isSerialsTxt==false);
        setVisibility(result?View.VISIBLE:View.GONE);
        if (result){
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASURE_STATICS);
        }
    }

}
