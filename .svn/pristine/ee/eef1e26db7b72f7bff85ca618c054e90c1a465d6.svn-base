package com.micsig.tbook.tbookscope.top.layout.measure;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogMeasureStatics;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutMeasureStatics extends Fragment {
    private Context context;

    private Button btnReset;
    private MSwitchBox sbStaticsAll;
    private MSwitchBox sbStaticsMean;
    private MSwitchBox sbStaticsMax;
    private MSwitchBox sbStaticsMin;
    private MSwitchBox sbStaticsDelta;
    private MSwitchBox sbStaticsCount;
    private DialogMeasureStatics dialogMeasurestatics;
    private TopMsgMeasureStatics msg=new TopMsgMeasureStatics();
    private int measureCount = 0;//选中的测量项
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_measure_statics, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        btnReset = (Button) view.findViewById(R.id.reset);
        sbStaticsAll = (MSwitchBox) view.findViewById(R.id.measureStaticsAllDetail);
        sbStaticsMean = (MSwitchBox) view.findViewById(R.id.measureStaticsMeanDetail);
        sbStaticsMax = (MSwitchBox) view.findViewById(R.id.measureStaticsMaxDetail);
        sbStaticsMin = (MSwitchBox) view.findViewById(R.id.measureStaticsMinDetail);
        sbStaticsDelta = (MSwitchBox) view.findViewById(R.id.measureStaticsDeltaDetail);
        sbStaticsCount = (MSwitchBox) view.findViewById(R.id.measureStaticsCountDetail);



        sbStaticsAll.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbStaticsMean.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbStaticsMax.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbStaticsMin.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbStaticsDelta.setOnToggleStateChangedListener(onToggleStateChangedListener);
        sbStaticsCount.setOnToggleStateChangedListener(onToggleStateChangedListener);

        btnReset.setOnClickListener((view1)->{
            PlaySound.getInstance().playButton();
            MeasureManage.getInstance().measureStaticReset();
        });

        dialogMeasurestatics =(DialogMeasureStatics) ((MainActivity)this.context).findViewById(R.id.dialogMeasureStatics);
    }

    private void initControl(){
//        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT).subscribe(consumerCommandMeasureOpenToUI);
    }

    private void setCache(){
        boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
        boolean mean=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN);
        boolean max=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX);
        boolean min=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN);
        boolean delta=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA);
        boolean count=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT);
        sbStaticsAll.setState(all);
        sbStaticsMean.setState(mean);
        sbStaticsMax.setState(max);
        sbStaticsMin.setState(min);
        sbStaticsDelta.setState(delta);
        sbStaticsCount.setState(count);

        if (all==false){
            sbStaticsMean.setEnabled(false);
            sbStaticsMax.setEnabled(false);
            sbStaticsMin.setEnabled(false);
            sbStaticsDelta.setEnabled(false);
            sbStaticsCount.setEnabled(false);
        }
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMean,mean);
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMax,max);
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMin,min);
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsDelta,delta);
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsCount,count);
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsAll,all);

        Command.get().getMeasure_statistic().Display(all,false);
        Command.get().getMeasure_statistic().Mean(mean,false);
        Command.get().getMeasure_statistic().Max(max,false);
        Command.get().getMeasure_statistic().Min(min,false);
        Command.get().getMeasure_statistic().Dev(delta,false);
        Command.get().getMeasure_statistic().Count(count,false);

        sendMsg();
    }

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            onStateChanged(view, state, false);
        }
    };

    private void showPosition(){
        new Handler().postDelayed(()->{
            Logger.i(ExternalKeysProtocol.Debug,"all:"+ ScreenUtil.getViewLocation(sbStaticsAll).toString());
            Logger.i(ExternalKeysProtocol.Debug, "Mean:"+ScreenUtil.getViewLocation(sbStaticsMean).toString());
            Logger.i(ExternalKeysProtocol.Debug, "max:"+ScreenUtil.getViewLocation(sbStaticsMax).toString());
            Logger.i(ExternalKeysProtocol.Debug, "min:"+ScreenUtil.getViewLocation(sbStaticsMin).toString());
            Logger.i(ExternalKeysProtocol.Debug, "delta:"+ScreenUtil.getViewLocation(sbStaticsDelta).toString());
            Logger.i(ExternalKeysProtocol.Debug, "count:"+ScreenUtil.getViewLocation(sbStaticsCount).toString());
            Logger.i(ExternalKeysProtocol.Debug,"btn:"+ScreenUtil.getViewLocation(btnReset).toString());
        },2000);
    }
    private void showDialog(){
        boolean isShowMean=sbStaticsMean.isState();
        boolean isShowMax=sbStaticsMax.isState();
        boolean isShowMin=sbStaticsMin.isState();
        boolean isShowDelta=sbStaticsDelta.isState();
        boolean isShowCount=sbStaticsCount.isState();
        dialogMeasurestatics.show(isShowMean,isShowMax,isShowMin,isShowDelta,isShowCount, measureCount, ScopeBase.getNewHeight());
    }
    private void setDetaileEnable(boolean enable){
        sbStaticsMean.setEnabled(enable);
        sbStaticsMax.setEnabled(enable);
        sbStaticsMin.setEnabled(enable);
        sbStaticsDelta.setEnabled(enable);
        sbStaticsCount.setEnabled(enable);
    }
    private void sendMsg(){
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE_STATICS,msg);
    }
    private void onStateChanged(MSwitchBox view, boolean state, boolean isFromEventBus){
        if (view.getId() == sbStaticsAll.getId()){
            MeasureManage.getInstance().measureStaticReset();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL,String.valueOf(state));
            Command.get().getMeasure_statistic().Display(state,false);
            if (state){
                showDialog();
                setDetaileEnable(true);
            }else {
                dialogMeasurestatics.hide();
                setDetaileEnable(false);
            }

        }else if (view.getId()==sbStaticsMean.getId()){
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (b==false) return;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN,String.valueOf(state));
            Command.get().getMeasure_statistic().Mean(state,false);
            showDialog();
        }else if (view.getId()==sbStaticsMax.getId()){
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (b==false) return;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX,String.valueOf(state));
            Command.get().getMeasure_statistic().Max(state,false);
            showDialog();
        }else if (view.getId()==sbStaticsMin.getId()){
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (b==false) return;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN,String.valueOf(state));
            Command.get().getMeasure_statistic().Min(state,false);
            showDialog();
        }else if (view.getId()==sbStaticsDelta.getId()){
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (b==false) return;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA,String.valueOf(state));
            Command.get().getMeasure_statistic().Dev(state,false);
            showDialog();
        }else if (view.getId()==sbStaticsCount.getId()){
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (b==false) return;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT,String.valueOf(state));
            Command.get().getMeasure_statistic().Count(state,false);
            showDialog();
        }
        sendMsg();

    }

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

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };
    private Consumer<LoadCache> consumerLoadCacheEx = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            new Handler().postDelayed(()->setCache(),1000);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {

            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_Measure_STAT_Display:{
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsAll.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsAll,b);
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Reset:{
                    MeasureManage.getInstance().measureStaticReset();
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Mean:{
                    if (sbStaticsMean.isEnabled()==false) return;
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsMean.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMean,b);
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Max:{
                    if (sbStaticsMax.isEnabled()==false) return;
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsMax.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMax,b);
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Min:{
                    if (sbStaticsMin.isEnabled()==false) return;
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsMin.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMin,b);
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Dev:{
                    if (sbStaticsDelta.isEnabled()==false) return;
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsDelta.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsDelta,b);
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Count:{
                    if (sbStaticsCount.isEnabled()==false) return;
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam());
                    sbStaticsCount.setState(b);
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsCount,b);
                }break;
            }
        }
    };

    private Consumer<String> consumerCommandMeasureOpenToUI = new Consumer<String>() {
        @Override
        public void accept(String str) throws Exception {
            if(StrUtil.isEmpty(str)) return;
            Logger.e("TopLayoutMeasureStatics", "measureStr= " + str);
            measureCount = Integer.parseInt(str.split(CommandMsgToUI.PARAM_SPLIT)[1]);
            if (sbStaticsAll.isState()) {
                showDialog();
            }
        }
    };



    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public IMeasureDetail getMeasureDetail() {
        return null;
    }
}
