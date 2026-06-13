package com.micsig.tbook.tbookscope.top.layout.measure;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.display.MsgCursorVisible;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.MMainMenuChannel;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.channel.TopViewChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutMeasureSetting extends Fragment {

    private static final String TAG = "TopLayoutMeasureSetting";
    private Context context;
    private MMainMenuChannel viewChannel;
    private MSwitchBox checkIndicator;
    private TopMsgMeasureSetting msgSetting;
    private TopViewRadioGroup radioRange, radioThreshold;
    private TextView txtHigh,txtMiddle,txtLow,lblHigh,lblMiddle,lblLow;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    private Button btnReset;
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false
    };
//    private List<Boolean> channelShowList = new ArrayList<>(
//            Collections.nCopies(
//                    ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT +
//                            ChannelFactory.REF_CNT + ChannelFactory.SERIAL_CNT, Boolean.FALSE)
//    );
    
    private ViewGroup rootView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.rootView=container;
        return inflater.inflate(R.layout.layout_measure_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControls();
    }

    private void initView(View view) {
        dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);

        checkIndicator=view.findViewById(R.id.indicator);
        radioRange =view.findViewById(R.id.range);
        radioThreshold =view.findViewById(R.id.Threshold);
        txtHigh=view.findViewById(R.id.txtHigh);
        txtMiddle=view.findViewById(R.id.txtMiddle);
        txtLow=view.findViewById(R.id.txtLow);
        lblHigh=view.findViewById(R.id.lblHigh);
        lblMiddle=view.findViewById(R.id.lblMiddle);
        lblLow=view.findViewById(R.id.lblLow);
        btnReset=view.findViewById(R.id.btnReset);

        checkIndicator.setOnToggleStateChangedListener(this::onToggleStateChanged);
        radioRange.setData(R.string.measureSettingRange,R.array.measureSettingRange,onCheckChangedListener);
        radioThreshold.setData(R.string.measureSettingThresholds,R.array.measureSettingThresholds,onCheckChangedListener);
        txtHigh.setOnClickListener(this::OnClickListener);
        txtMiddle.setOnClickListener(this::OnClickListener);
        txtLow.setOnClickListener(this::OnClickListener);
        btnReset.setOnClickListener(this::OnClickListener);
        initChannelView(view);

        msgSetting=new TopMsgMeasureSetting();

    }
    private void initChannelView(View view){
        viewChannel = (MMainMenuChannel) view.findViewById(R.id.chanMeasureSetting);
        viewChannel.setChangeListener(onChannelItemClickListener, null);

    }
    private void initControls(){
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.CURSOR_CHANGE_VISIBLE).subscribe(consumerCursorChangeVisible);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
        RxBus.getInstance().getObservable(RxEnum.MSG_TVALUE_ENABLE).subscribe(consumerTValue);

    }

    private void sendMsg(){
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE_SETTING,msgSetting);
    }
    private void onToggleStateChanged(MSwitchBox view, boolean state){
        PlaySound.getInstance().playButton();
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR,String.valueOf(state));

        MeasureManage.getInstance().getMeasureIndication().setEnable(state);
        MeasureManage.getInstance().getMeasureItem().setSelectEnable(state);
        Tools.PrintControlsLocation("Setting",rootView);
//        if (state==false){
//            MeasureManage.getInstance().getMeasureItem().cancelAllSelected();
//        }
    }
    private TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() {
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) {
            PlaySound.getInstance().playButton();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT, String.valueOf(checkedIndex));
            updateTextView(checkedIndex);

        }
    };

    public void updateTextView(int checkedIndex){
        checkedIndex = TChan.toUiChNo(checkedIndex);
        txtHigh.setText(readTxtHigh());
        txtMiddle.setText(readTxtMiddle());
        txtLow.setText(readTxtLow());
        lblHigh.setTextColor(TChan.getChannelColor(context,checkedIndex));
        lblMiddle.setTextColor(TChan.getChannelColor(context,checkedIndex));
        lblLow.setTextColor(TChan.getChannelColor(context,checkedIndex));
        radioThreshold.setPromptTxtColor(TChan.getChannelColor(context,checkedIndex));

        int thresholdIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        //#2811 启动示波器之后测量指示中的设置值不对，传递参数有误导致获取对应的channel的buffer不对。
        checkedIndex = TChan.toFpgaChNo(checkedIndex);
        updateParamToDevice(checkedIndex, thresholdIdx);
    }

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
    private Consumer<Boolean> consumerTValue = new Consumer<Boolean>() {
        @Override
        public void accept(@androidx.annotation.NonNull Boolean update) throws Exception {
            Logger.i(TAG, "isTVtrace= " + MeasureManage.getInstance().isCursorTValueTrace());
            radioRange.setEnabled(!MeasureManage.getInstance().isCursorTValueTrace());
        }
    };
    private void onCheckChanged(TopViewRadioGroup view,TopBeanChannel item,boolean isFormHardware){
        if (view.getId()==radioRange.getId()){

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_RANGE,String.valueOf(item.getIndex()));
            MeasureService.setCursorRang(item.getIndex() == 1);
            MeasureService.forceMeasureRefresh();
            Command.get().getMeasure_setting().Range(item.getIndex(),false);


        }else if (view.getId()==radioThreshold.getId()){
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS,String.valueOf(item.getIndex()));
            txtHigh.setText(readTxtHigh());
            txtMiddle.setText(readTxtMiddle());
            txtLow.setText(readTxtLow());

            int chIdx = viewChannel.getSelectChannel();
            updateParamToDevice(chIdx,item.getIndex());
        }
        sendMsg();
    }

    private void updateParamToDevice(int chIdx,int thresholdIdx){
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx);
        if(channel != null){
            if(thresholdIdx == 1){
                channel.setAbsLower(TBookUtil.getDoubleFromM(getLow()));
                channel.setAbsUpper(TBookUtil.getDoubleFromM(getHigh()));
                channel.setAbsMiddle(TBookUtil.getDoubleFromM(getMiddle()));
            }else{
                channel.setLower(Integer.parseInt(getLow()));
                channel.setUpper(Integer.parseInt(getHigh()));
                channel.setMiddle(Integer.parseInt(getMiddle()));
            }
            channel.setAbsEnable(thresholdIdx == 1);

        }
        MeasureService.forceMeasureRefresh();
    }
    private void reset(){
        TChan.foreachAllChan((chan)->{
            int ch=TChan.toFpgaChNo(chan);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH + "0" + ch, String.valueOf(90));
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + ch, String.valueOf(50));
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW + "0" + ch, String.valueOf(10));

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH + "1" + ch, String.valueOf(1));
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + ch, String.valueOf(0));
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW + "1" + ch, String.valueOf(-1));
        });
        radioThreshold.setSelectedIndex(0);
        onCheckChanged(radioThreshold,new TopBeanChannel(0,""),false);
        updateTxtChanged(txtHigh.getId(),txtHigh.getText().toString());
        updateTxtChanged(txtMiddle.getId(),txtMiddle.getText().toString());
        updateTxtChanged(txtLow.getId(),txtLow.getText().toString());
        //control deal
        for(int chIdx=ChannelFactory.CH1;chIdx<ChannelFactory.S1;chIdx++) {
            BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx);
            if (channel != null) {
                channel.setLower(Integer.parseInt(getLow(chIdx)));
                channel.setUpper(Integer.parseInt(getHigh(chIdx)));
                channel.setMiddle(Integer.parseInt(getMiddle(chIdx)));
            }
        }
        MeasureService.forceMeasureRefresh();
    }

    private String getHigh(){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH +getSaveThresholdsParam());
    }
    private String getMiddle(){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam());
    }

    private String getLow(){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam());
    }
    private String getHigh(int chIdx){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH +getSaveThresholdsParam(chIdx));
    }
    private String getMiddle(int chIdx){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(chIdx));
    }
    private String getLow(int chIdx){
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(chIdx));
    }


    private String readTxtHigh(){
        return getHigh()+getUnit();
    }
    private String readTxtMiddle(){
        return getMiddle()+getUnit();
    }
    private String readTxtLow(){
        return getLow()+getUnit();
    }

    private void OnClickListener(View v){
        if (v.getId()==txtHigh.getId()){
            openDialog(txtHigh);
        }else if (v.getId()==txtMiddle.getId()){
            openDialog(txtMiddle);
        }else if (v.getId()==txtLow.getId()){
            openDialog(txtLow);
        }else if (v.getId()==btnReset.getId()){
            PlaySound.getInstance().playButton();
            reset();
        }
    }
    private void openDialog(TextView txtView){
        int thresholdsIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        String txt= txtView.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
        if (thresholdsIdx==0){
            dialogFloatKeyBoard.setFloatData_OnlyNum(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() {
                @Override
                public void onDismiss(View fromView, String show) {
                    PlaySound.getInstance().playButton();
                    int val =(int) TBookUtil.getDoubleFromM(show);
                    val= getVerify(txtView,val);
                    show = TBookUtil.getPercent(Math.abs(val));
                    if (show.equals("")) {
                        show = "0 ";
                    }
                    updateTxtChanged(txtView.getId(),show);
                }
            });

        }else if (thresholdsIdx==1){
            boolean bShow = false;
            int chIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT);
            if(ChannelFactory.isMath_FFT_Ch(chIdx)){
                MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);
                if(mathChannel != null){
                    if(mathChannel.getMathFFTWave().getFFTType()== MathFFTWave.FFT_TYPE_DB){
                        bShow = true;
                        dialogFloatKeyBoard.setFloatData_NoUnit(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(View fromView, String show) {
                                PlaySound.getInstance().playButton();
                                double val = TBookUtil.getDoubleFromM(show);
                                show = TBookUtil.getD3FromD(Math.abs(val));
                                if (val<0) show="-"+show;
                                if (show.equals("")) {
                                    show = "0 ";
                                }
                                show= String.valueOf((int)Double.parseDouble(show));
                                show= getVerify(txtView,show);
                                updateTxtChanged(txtView.getId(), show);
                            }
                        });
                    }
                }
            }
            if(!bShow){
                dialogFloatKeyBoard.setFloatData(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() {
                    @Override
                    public void onDismiss(View fromView, String show) {
                        PlaySound.getInstance().playButton();
                        double val = TBookUtil.getDoubleFromM(show);
                        show = TBookUtil.getMFromDouble(Math.abs(val));
                        if (val < 0) show = "-" + show;
                        if (show.equals("")) {
                            show = "0 ";
                        }
                        show= getVerify(txtView,show);
                        updateTxtChanged(txtView.getId(), show);
                    }
                });
            }
        }

    }

    private int getVerify(TextView txtView,int val){
        if (txtView.getId()==txtHigh.getId()){
            int low=Integer.parseInt( txtLow.getText().toString().replace("%",""));
            if (val<=low){
                val=low+1;
            }
            int mid=Integer.parseInt(txtMiddle.getText().toString().replace("%",""));
            if (mid>val){
                mid=val;
                txtMiddle.setText(mid+getUnit());
            }
            return val;
        }else if (txtView.getId()==txtLow.getId()){
            int high=Integer.parseInt( txtHigh.getText().toString().replace("%",""));
            if (val>=high){
                val= high-1;
            }
            int mid=Integer.parseInt(txtMiddle.getText().toString().replace("%",""));
            if (mid<val){
                mid=val;
                txtMiddle.setText(mid+getUnit());
            }
            return val;
        }else {
            int high=Integer.parseInt( txtHigh.getText().toString().replace("%",""));
            int low=Integer.parseInt( txtLow.getText().toString().replace("%",""));
            if (val>high){
                val=high;
            }else if (val<low){
                val=low;
            }
            return val;
        }
    }

    private String getVerify(TextView txtView,String val){
        String txt= val.replaceAll("(?:A|V|%|dB|\\s)","");
        double value = TBookUtil.getDoubleFromM(txt);
        if(txtView.getId()==txtHigh.getId()){
            String td=txtLow.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            double low=TBookUtil.getDoubleFromM(td);
            if (value<=low){
                val=TBookUtil.addMinUnit(td,1);
            }
            value = TBookUtil.getDoubleFromM(val);
            String mid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            double middle=TBookUtil.getDoubleFromM(mid);
            if (middle>value){
                middle=value;
                String s = TBookUtil.getD3FromD_zf(middle);
                txtMiddle.setText(s+getUnit());
            }

            return val;
        }else if (txtView.getId()==txtLow.getId()){
            String td=txtHigh.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            double high=TBookUtil.getDoubleFromM(td);
            if (value>=high){
                val=TBookUtil.subMinUnit(td,1);
            }

            value = TBookUtil.getDoubleFromM(val);
            String mid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            double middle=TBookUtil.getDoubleFromM(mid);
            if (middle<value){
                middle=value;
                String s = TBookUtil.getD3FromD_zf(middle);
                txtMiddle.setText(s+getUnit());
            }
            return val;
        }else {
            String th=txtHigh.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            String tl=txtLow.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
            double high=TBookUtil.getDoubleFromM(th);
            double low=TBookUtil.getDoubleFromM(tl);
            double mid=TBookUtil.getDoubleFromM(val);
            if (mid>high){
                val=th;
            }else if (mid<low){
                val=tl;
            }

            return val;
        }
    }
    private void updateTxtChanged(int id,String result){
        if (StrUtil.isEmpty(result)) {
            result = "0";
        }
        result= result.replaceAll("(?:A|V|%|dB|\\s)","");

        int chIdx = viewChannel.getSelectChannel();
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx);
        if (id==txtHigh.getId()) {
            txtHigh.setText(result + getUnit());
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam() , result);

            if(channel != null){
                if(channel.isAbsEnable()){
                    channel.setAbsUpper(TBookUtil.getDoubleFromM(result));
                }else {
                    channel.setUpper(Integer.parseInt(result));
                }
            }
        }else if (id==txtMiddle.getId()){
            txtMiddle.setText(result + getUnit());
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam() , result);
            if(channel != null){
                if(channel.isAbsEnable()){
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(result));

                }else {
                    channel.setMiddle(Integer.parseInt(result));
                }
            }
        }else if (id==txtLow.getId()){
            txtLow.setText(result + getUnit());
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(), result);
            if(channel != null){
                if(channel.isAbsEnable()){
                    channel.setAbsLower(TBookUtil.getDoubleFromM(result));
                }else {
                    channel.setLower(Integer.parseInt(result));
                }
            }
        }

        //if middle value changed,reset middle
        String mid= CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam());
        String txtMid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)","");
        if (mid.equals(txtMid)==false){
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam() , txtMid);
            if(channel != null){
                if(channel.isAbsEnable()){
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(txtMid));
                }else {
                    channel.setMiddle(Integer.parseInt(txtMid));
                }
            }
        }
        MeasureService.forceMeasureRefresh();
    }

    private String getSaveThresholdsParam(){
        int chIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT);
        int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        return ""+thresholdsIdx+chIdx;
    }
    private String getSaveThresholdsParam(int chIdx){
        int thresholdsIdx=0;
        return ""+thresholdsIdx+chIdx;
    }
    private String getSaveThresholdsParam(int chIdx,int thresholdsIdx){

        return ""+thresholdsIdx+chIdx;
    }

    private String getUnit(){
        int thresholdsIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        if (thresholdsIdx==0){
            return "%";
        }else {
            int chIdx= viewChannel.getSelectChannel();
            String yUnit = ChannelFactory.getProbeType(chIdx);
            return yUnit;
        }

    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true);
        }
    };
    @SuppressLint("SetTextI18n")
    private void setCache(){
        boolean indicator= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR);
        int rangeIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_RANGE);
        int channelIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT);
        int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        String high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam());
        String middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam());
        String low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam());

        checkIndicator.setState(indicator);
        radioRange.setSelectedIndex(rangeIdx);
        viewChannel.setChecked(channelIdx);
        radioThreshold.setSelectedIndex(thresholdsIdx);
        txtHigh.setText(high+getUnit());
        txtMiddle.setText(middle+getUnit());
        txtLow.setText(low+getUnit());

        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1);
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2);
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3);
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4);
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5);
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6);
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7);
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8);

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
        } else {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
            channelShow[4] = ch5;
            channelShow[5] = ch6;
            channelShow[6] = ch7;
            channelShow[7] = ch8;
        }
        TChan.foreachCh1ToR8(chan -> {
            if (TChan.isMath(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            }
            if (TChan.isRef(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            }
        });
        setParam();
        setChannelShow();
        MeasureManage.getInstance().getMeasureIndication().setEnable(indicator);
        MeasureManage.getInstance().getMeasureItem().setSelectEnable(indicator);
        MeasureService.setCursorRang(rangeIdx == 1);

        //init selected
        boolean b= isMeasureItemClickEnable();
        MeasureManage.MeasureItemStruct item=MeasureManage.getInstance().getMeasureItem().getSelectItem();
        if (item==null || b==false){
            return;
        }
        channelIdx=item.getChannelId()-1;
        high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam(channelIdx,thresholdsIdx));
        middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(channelIdx,thresholdsIdx));
        low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(channelIdx,thresholdsIdx));
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(channelIdx);
        if(channel != null){
            if(thresholdsIdx == 1){
                channel.setAbsLower(TBookUtil.getDoubleFromM(low));
                channel.setAbsUpper(TBookUtil.getDoubleFromM(high));
                channel.setAbsMiddle(TBookUtil.getDoubleFromM(middle));
            }else{
                channel.setLower(Integer.parseInt(low));
                channel.setUpper(Integer.parseInt(high));
                channel.setMiddle(Integer.parseInt(middle));
            }
            channel.setAbsEnable(thresholdsIdx == 1);
        }
        MeasureService.forceMeasureRefresh();

        Command.get().getMeasure_setting().Range(rangeIdx,false);
    }

    private void setParam() {
        int thresholdIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL);
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        for (int i = 0; i < channelList.size(); i++) {
            int chIdx = Integer.parseInt(channelList.get(i)) - 1;
            BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx);
            if (channel != null) {
                if (thresholdIdx == 1) {
                    channel.setAbsLower(TBookUtil.getDoubleFromM(getLow(chIdx)));
                    channel.setAbsUpper(TBookUtil.getDoubleFromM(getHigh(chIdx)));
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(getMiddle(chIdx)));
                } else {
                    channel.setLower(Integer.parseInt(getLow(chIdx)));
                    channel.setUpper(Integer.parseInt(getHigh(chIdx)));
                    channel.setMiddle(Integer.parseInt(getMiddle(chIdx)));
                }
                channel.setAbsEnable(thresholdIdx == 1);
            }
        }
        MeasureService.forceMeasureRefresh();
    }

    private boolean isMeasureItemClickEnable(){
        return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR);
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {

            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_Measure_Setting_Range:{
                    int range=Integer.parseInt(commandMsgToUI.getParam());
                    radioRange.setSelectedIndex(range);
                    onCheckChangedListener.onClick(radioRange,radioRange.getSelected());
                }break;

            }
        }
    };
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception {
            TChan.foreachChan(chan -> {
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan);
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue();
            });
            setChannelShow();
            txtHigh.setText(readTxtHigh());
            txtMiddle.setText(readTxtMiddle());
            txtLow.setText(readTxtLow());
        }
    };
    private Consumer<RightMsgMath> consumerRightMath=new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            txtHigh.setText(readTxtHigh());
            txtMiddle.setText(readTxtMiddle());
            txtLow.setText(readTxtLow());
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            TChan.foreachMath(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            TChan.foreachRef(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            setChannelShow();
            txtHigh.setText(readTxtHigh());
            txtMiddle.setText(readTxtMiddle());
            txtLow.setText(readTxtLow());
        }
    };

    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception {
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber());
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser;
            setChannelShow();
            txtHigh.setText(readTxtHigh());
            txtMiddle.setText(readTxtMiddle());
            txtLow.setText(readTxtLow());
        }
    };

    private  Consumer<MsgCursorVisible> consumerCursorChangeVisible = new Consumer<MsgCursorVisible>() {
        @Override
        public void accept(MsgCursorVisible msgCursorVisible) throws Exception {
            if (msgCursorVisible.isVisible()==false && msgCursorVisible.isShu()){
                radioRange.setSelectedIndex(0);
                onCheckChanged(radioRange,new TopBeanChannel(0,""),false);
            }
        }
    };


    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow,true);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT, String.valueOf(viewChannel.getSelectedIndex()));
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL);
        updateTextView(viewChannel.getSelectChannel());
    }

    public IMeasureDetail getMeasureDetail() {
        return null;
    }
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            viewChannel.setChannelColor(chIndex, colorStr);
        }
    };


}
