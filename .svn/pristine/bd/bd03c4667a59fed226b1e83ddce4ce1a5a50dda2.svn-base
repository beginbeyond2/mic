package com.micsig.tbook.tbookscope.top.popwindow;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.auto.TopLayoutAuto;
import com.micsig.tbook.tbookscope.top.layout.cursor.TopLayoutCursor;
import com.micsig.tbook.tbookscope.top.layout.display.TopLayoutDisplay;
import com.micsig.tbook.tbookscope.top.layout.factoryCalibration.TopLayoutFactoryCalibration;
import com.micsig.tbook.tbookscope.top.layout.frequencymeter.TopLayoutFrequencyMeter;
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasure;
import com.micsig.tbook.tbookscope.top.layout.sample.TopLayoutSample;
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSave;
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSaveStore;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.userset.TopLayoutUserset;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/5.
 */
public class TopLayoutPopWindow extends RelativeLayout {
    public static final int DETAIL_MEASURE = 0;
    public static final int DETAIL_SAVE = 1;

    public static final int DETAIL_CURSOR=2;
    public static final int DETAIL_SAMPLE = 3;
    public static final int DETAIL_DISPLAY = 4;
    public static final int DETAIL_TRIGGER = 5;
    public static final int DETAIL_AUTO = 6;
    public static final int DETAIL_USERSET = 7;
    public static final int DETAIL_FREQUENCYMETER = 8;
    public static final int DETAIL_FACTORYCALIBRATION = 9;

    private Context context;
    private TopViewTitleWithScroll titleWithHead;
    private RelativeLayout detailLayout;
    private View topSlipBoundary;
    private TopLayoutMeasure layoutMeasure;                     //测量
    private TopLayoutSave layoutSave;                           //保存
    private TopLayoutCursor layoutCursor;                       //光标
    private TopLayoutSample layoutSample;                       //采样
    private TopLayoutDisplay layoutDisplay;                     //显示
    private TopLayoutTrigger layoutTrigger;                     //触发
    private TopLayoutAuto layoutAuto;                           //自动
    private TopLayoutFrequencyMeter layoutFrequencyMeter;       //频率计
    private TopLayoutUserset layoutUserset;                     //用户设置
    private TopLayoutFactoryCalibration layoutFactoryCalibration;//工厂校准

    private TopMsgPopWindow msgPopWindow;

    private Fragment[] fragments = new Fragment[10];
    private String[] tags = {"Measure", "Save","Cursor", "Sample", "Display", "Trigger"
            , "Auto",  "Userset","FrequencyMeter", "FactoryCalibration"};

    public TopLayoutPopWindow(Context context) {
        this(context, null);
    }

    public TopLayoutPopWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);
    }

    View mainView;


    private void initView() {
        mainView = View.inflate(context, R.layout.layout_toppopwindow, this);

//        setOrientation(VERTICAL);
        detailLayout = (RelativeLayout) findViewById(R.id.topPopDetailPopwindow);
        titleWithHead = (TopViewTitleWithScroll) findViewById(R.id.topPopTitleWithHead);
        topSlipBoundary = findViewById(R.id.topSlipBoundary);

//        initLayout();
        if (GlobalVar.get().isFactoryCalibration()) {
            String[] array = context.getResources().getStringArray(R.array.popArrayTitleCalibration);
//            Log.d("Debug", String.format("initView: %s", Arrays.toString(array) ));
            boolean[] arrayVisible = new boolean[array.length];
            for (int i = 0; i < array.length; i++) {
                if (i != DETAIL_FREQUENCYMETER) {
                    arrayVisible[i] = true;
                } else {
                    arrayVisible[i] = false;
                }
            }
            titleWithHead.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);
        } else {
            String[] array = context.getResources().getStringArray(R.array.popArrayTitle);
//            Log.d("Debug", String.format("initView: %s", Arrays.toString(array) ));
            boolean[] arrayVisible = new boolean[array.length];
            for (int i = 0; i < array.length; i++) {
                if (i != DETAIL_FREQUENCYMETER) {
                    arrayVisible[i] = true;
                } else {
                    arrayVisible[i] = false;
                }
            }
            titleWithHead.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);
        }

        msgPopWindow = new TopMsgPopWindow(DETAIL_MEASURE, true, false);
    }

    public void setSavedInstanceState(Bundle savedInstanceState) {
        initLayout(savedInstanceState);
    }

    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag(tags[i]);
            }
        }

        layoutMeasure = fragments[DETAIL_MEASURE] == null ? new TopLayoutMeasure() : (TopLayoutMeasure) fragments[DETAIL_MEASURE];
        layoutSave = fragments[DETAIL_SAVE] == null ? new TopLayoutSave() : (TopLayoutSave) fragments[DETAIL_SAVE];
        layoutCursor=fragments[DETAIL_CURSOR]==null ?new TopLayoutCursor():(TopLayoutCursor)fragments[DETAIL_CURSOR];
        layoutSample = fragments[DETAIL_SAMPLE] == null ? new TopLayoutSample() : (TopLayoutSample) fragments[DETAIL_SAMPLE];
        layoutDisplay = fragments[DETAIL_DISPLAY] == null ? new TopLayoutDisplay() : (TopLayoutDisplay) fragments[DETAIL_DISPLAY];
        layoutTrigger = fragments[DETAIL_TRIGGER] == null ? new TopLayoutTrigger() : (TopLayoutTrigger) fragments[DETAIL_TRIGGER];
        layoutAuto = fragments[DETAIL_AUTO] == null ? new TopLayoutAuto() : (TopLayoutAuto) fragments[DETAIL_AUTO];
        layoutFrequencyMeter = fragments[DETAIL_FREQUENCYMETER] == null ? new TopLayoutFrequencyMeter() : (TopLayoutFrequencyMeter) fragments[DETAIL_FREQUENCYMETER];
        layoutUserset = fragments[DETAIL_USERSET] == null ? new TopLayoutUserset() : (TopLayoutUserset) fragments[DETAIL_USERSET];
        layoutFactoryCalibration = fragments[DETAIL_FACTORYCALIBRATION] == null ? new TopLayoutFactoryCalibration() : (TopLayoutFactoryCalibration) fragments[DETAIL_FACTORYCALIBRATION];

        if (savedInstanceState == null) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .add(R.id.topPopDetailPopwindow, layoutMeasure, tags[DETAIL_MEASURE])
                    .add(R.id.topPopDetailPopwindow, layoutSave, tags[DETAIL_SAVE])
                    .add(R.id.topPopDetailPopwindow, layoutCursor,tags[DETAIL_CURSOR])
                    .add(R.id.topPopDetailPopwindow, layoutSample, tags[DETAIL_SAMPLE])
                    .add(R.id.topPopDetailPopwindow, layoutDisplay, tags[DETAIL_DISPLAY])
                    .add(R.id.topPopDetailPopwindow, layoutTrigger, tags[DETAIL_TRIGGER])
                    .add(R.id.topPopDetailPopwindow, layoutAuto, tags[DETAIL_AUTO])
                    .add(R.id.topPopDetailPopwindow, layoutFrequencyMeter, tags[DETAIL_FREQUENCYMETER])
                    .add(R.id.topPopDetailPopwindow, layoutUserset, tags[DETAIL_USERSET])
                    .add(R.id.topPopDetailPopwindow, layoutFactoryCalibration, tags[DETAIL_FACTORYCALIBRATION])
                    .hide(layoutSave)
                    .hide(layoutCursor)
                    .hide(layoutSample)
                    .hide(layoutDisplay)
                    .hide(layoutTrigger)
                    .hide(layoutAuto)
                    .hide(layoutFrequencyMeter)
                    .hide(layoutUserset)
                    .hide(layoutFactoryCalibration)
                    .commitAllowingStateLoss();
        }
    }

    private void setCache() {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP);
        titleWithHead.moveOnlyScroll(0);
        titleWithHead.setSelected(index);
        onCheckChange(titleWithHead, titleWithHead.getSelected());
        resetParamsHandle.sendEmptyMessageDelayed(index, 200);
    }

    public void showLayoutMeasure() {
        titleWithHead.setSelected(DETAIL_MEASURE);
        layoutMeasure.showMeasureCommon();
        onCheckChange(titleWithHead, titleWithHead.getSelected());
    }

    public void showLayoutTrigger() {
        titleWithHead.setSelected(DETAIL_TRIGGER);
        onCheckChange(titleWithHead, titleWithHead.getSelected());
    }
    public void showLayoutSample(){
        titleWithHead.setSelected(DETAIL_SAMPLE);
        onCheckChange(titleWithHead,titleWithHead.getSelected());
    }

    public void showLayoutDisplay() {
        titleWithHead.setSelected(DETAIL_DISPLAY);
        onCheckChange(titleWithHead, titleWithHead.getSelected());
    }

    public boolean isShowLayoutMeasureCommon(){
        boolean b= DETAIL_MEASURE == titleWithHead.getSelected().getIndex();
        boolean b1=layoutMeasure.DETAIL_COMMON==layoutMeasure.getMeasureIdx();
        return b && b1;
    }
    public boolean isShowLayoutMeasure() {
        return DETAIL_MEASURE == titleWithHead.getSelected().getIndex();
    }

    public boolean isShowLayoutTrigger() {
        return DETAIL_TRIGGER == titleWithHead.getSelected().getIndex();
    }
    public boolean isShowLayoutSample(){
        return DETAIL_SAMPLE==titleWithHead.getSelected().getIndex();
    }
    public boolean isShowLayoutDisplay() {
        return DETAIL_DISPLAY == titleWithHead.getSelected().getIndex();
    }


    public TopLayoutDisplay getLayoutDisPlay() {
        return layoutDisplay;
    }

    public void setTriggerSerialBus1(){
        int saveIdx= layoutTrigger.getTriggerIdx();
        if (saveIdx == TopLayoutTrigger.DETAIL_S1
                || saveIdx == TopLayoutTrigger.DETAIL_S2
                || saveIdx == TopLayoutTrigger.DETAIL_S3
                || saveIdx == TopLayoutTrigger.DETAIL_S4
        ) {
            return;
        }else {
            saveTriggerAndShowIdx(TopLayoutTrigger.DETAIL_S1);
        }
    }
    private void saveTriggerAndShowIdx(int idx){
        int saveIdx= layoutTrigger.getTriggerIdx();
        CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(false));
        CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_INDEX,String.valueOf(saveIdx));
        layoutTrigger.setTriggerIdx(idx);
    }
    public void restoreTriggerIdx(){
        int idx= CacheUtil.get().getInt(CacheUtil.SAVE_TEMP_TRIGGER_INDEX);
        boolean isOption=CacheUtil.get().getBoolean(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION);
        if (idx<TopLayoutTrigger.DETAIL_COMMON
                || idx>TopLayoutTrigger.DETAIL_S4
                || idx== layoutTrigger.getTriggerIdx()
                || isOption
        ) return;
        layoutTrigger.setTriggerIdx(idx);
    }

    public int getFirstMenuIdx(){
        return titleWithHead.getSelected().getIndex();
    }
    public int getSecondMenuIdx(){
        int idx=getFirstMenuIdx();
        int index=0;
        switch (idx){
            case DETAIL_MEASURE:{
                index= layoutMeasure.getMeasureIdx();
            }break;
            case DETAIL_SAVE:{
                index= layoutSave.getSaveIdx();
            }break;
            case DETAIL_CURSOR:{
                index= layoutCursor.getCursorIdx();
            }break;
            case DETAIL_SAMPLE:{
                index=layoutSample.getSampleIdx();
            }break;
            case DETAIL_DISPLAY:{
                index=layoutDisplay.getDisplayIdx();
            }break;
            case DETAIL_TRIGGER:{
                index=layoutTrigger.getTriggerIdx();
            }break;
            case DETAIL_AUTO:{
                index=layoutAuto.getAutoIdx();
            }break;
            case DETAIL_FREQUENCYMETER:{

            }break;
            case DETAIL_USERSET:{
                index=layoutUserset.getUserIdx();
            }break;
            case DETAIL_FACTORYCALIBRATION:{
                index=0;
            }break;
        }
        return index;
    }


    public boolean isCurYTMode() {
        return msgPopWindow.getYtMode().isValue();
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutPopWindow, true);
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                case IWorkMode.WorkMode_YTZOOM:
                    if (workModeBean.getPreWorkMode() != IWorkMode.WorkMode_XY) {
                        //当不是由xy模式进入yt模式或ytZoom模式的时候，则不向下进行
                        return;
                    }
                    titleWithHead.setEnable(DETAIL_MEASURE, true);
                    titleWithHead.setEnable(DETAIL_SAVE, true);
                    titleWithHead.setEnable(DETAIL_CURSOR,true);
                    titleWithHead.setEnable(DETAIL_SAMPLE, true);
                    titleWithHead.setEnable(DETAIL_DISPLAY, true);
                    titleWithHead.setEnable(DETAIL_TRIGGER, true);
                    titleWithHead.setEnable(DETAIL_AUTO, true);
                    titleWithHead.setEnable(DETAIL_FREQUENCYMETER, true);
                    titleWithHead.setEnable(DETAIL_USERSET, true);
                    if (GlobalVar.get().isFactoryCalibration()) {
                        titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, true);
                    }

                    msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex());
                    msgPopWindow.setYtMode(true);
                    sendMsg();
                    break;
                case IWorkMode.WorkMode_XY:
                    titleWithHead.setEnable(DETAIL_MEASURE, false);
                    titleWithHead.setEnable(DETAIL_SAVE, false);
                    titleWithHead.setEnable(DETAIL_CURSOR,false);
                    titleWithHead.setEnable(DETAIL_SAMPLE, false);
                    titleWithHead.setEnable(DETAIL_DISPLAY, true);
                    titleWithHead.setEnable(DETAIL_TRIGGER, false);
                    titleWithHead.setEnable(DETAIL_AUTO, false);
                    titleWithHead.setEnable(DETAIL_FREQUENCYMETER, false);
                    titleWithHead.setEnable(DETAIL_USERSET, false);
                    if (GlobalVar.get().isFactoryCalibration()) {
                        titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, false);
                    }
                    if (titleWithHead.getSelected().getIndex() != DETAIL_DISPLAY) {
                        titleWithHead.setSelected(DETAIL_DISPLAY);
                        onCheckChange(titleWithHead, titleWithHead.getSelected());
                    }

                    msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex());
                    msgPopWindow.setYtMode(false);
                    sendMsg();
                    break;
            }
        }
    };

    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            titleWithHead.setEnable(DETAIL_MEASURE, !aBoolean);
            titleWithHead.setEnable(DETAIL_SAVE, !aBoolean);
            titleWithHead.setEnable(DETAIL_CURSOR,!aBoolean);
            titleWithHead.setEnable(DETAIL_SAMPLE, !aBoolean);
            titleWithHead.setEnable(DETAIL_DISPLAY, true);
            titleWithHead.setEnable(DETAIL_TRIGGER, true);
            titleWithHead.setEnable(DETAIL_AUTO, !aBoolean);
            titleWithHead.setEnable(DETAIL_FREQUENCYMETER, !aBoolean);
            titleWithHead.setEnable(DETAIL_USERSET, !aBoolean);
            if (GlobalVar.get().isFactoryCalibration()) {
                titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, !aBoolean);
            }
            layoutTrigger.setSerialsWordVisible(aBoolean);
            if (aBoolean) {
                if (titleWithHead.getSelected().getIndex() != DETAIL_TRIGGER) {
                    titleWithHead.setSelected(DETAIL_TRIGGER);
                    onCheckChange(titleWithHead, titleWithHead.getSelected());
                }
            }

            layoutDisplay.setSerialsWordVisible(aBoolean);
            //暂定 固定到Trigger标签位置
//            if (aBoolean) {
//                if (titleWithHead.getSelected().getIndex() != DETAIL_DISPLAY) {
//                    titleWithHead.setSelected(DETAIL_DISPLAY);
//                    onCheckChange(titleWithHead, titleWithHead.getSelected());
//                }
//            }

            msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex());
            msgPopWindow.setSerialWord(aBoolean);
            sendMsg();
        }
    };

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPSLIP_TITLE, msgPopWindow);
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
        }
    };

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            onCheckChange(view, item);
        }
    };

    private void onCheckChange(View view, TopAllBeanTitle item) {
        msgPopWindow.setCheckIndex(item.getIndex());
        sendMsg();
        if (item.getIndex() != DETAIL_FACTORYCALIBRATION) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP, String.valueOf(item.getIndex()));
        }
        ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .hide(layoutSave)
                .hide(layoutCursor)
                .hide(layoutSample)
                .hide(layoutDisplay)
                .hide(layoutTrigger)
                .hide(layoutAuto)
                .hide(layoutFrequencyMeter)
                .hide(layoutUserset)
                .hide(layoutMeasure)
                .hide(layoutFactoryCalibration)
                .commitAllowingStateLoss();
        switch (item.getIndex()) {
            case DETAIL_MEASURE:                //测量
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutMeasure).commitAllowingStateLoss();
                break;
            case DETAIL_SAVE:                   //保存
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutSave).commitAllowingStateLoss();
                break;
            case DETAIL_CURSOR:                 //光标
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutCursor).commitAllowingStateLoss();
                break;
            case DETAIL_SAMPLE:                 //采样
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutSample).commitAllowingStateLoss();
                break;
            case DETAIL_DISPLAY:                //显示
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutDisplay).commitAllowingStateLoss();
                break;
            case DETAIL_TRIGGER:                //触发
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutTrigger).commitAllowingStateLoss();
                break;
            case DETAIL_AUTO:                   //自动
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutAuto).commitAllowingStateLoss();
                break;
            case DETAIL_FREQUENCYMETER:         //频率计
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutFrequencyMeter).commitAllowingStateLoss();
                break;
            case DETAIL_USERSET:                //用户设置
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutUserset).commitAllowingStateLoss();
                break;
            case DETAIL_FACTORYCALIBRATION:     //工厂校准
                ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                        .show(layoutFactoryCalibration).commitAllowingStateLoss();
                break;
        }
        resetParamsHandle.sendEmptyMessageDelayed(item.getIndex(), 200);
    }

    android.os.Handler resetParamsHandle = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(layoutParams);
        }
    };

    /**
     * 获得当前TopSlip的有效点击区域
     */
    public Rect getValidRect() {
        int[] boundary = new int[2];
        topSlipBoundary.getLocationOnScreen(boundary);
        int[] topSlip = new int[2];
        getLocationOnScreen(topSlip);
        return new Rect(getLeft(), topSlip[1], getRight(), boundary[1]);
    }

    public TopLayoutTrigger getLayoutTrigger() {
        return layoutTrigger;
    }
}
