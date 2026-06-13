package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.scope.Calibrate.ProbeCalibrate;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutChannel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2023-11-8 14:54
 */
public class DialogProbeInterface extends ConstraintLayout {
    private Context context;
    private ViewGroup rootView;
    public DialogProbeInterface(@NonNull Context context) {
        this(context,null);
    }

    public DialogProbeInterface(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DialogProbeInterface(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initView();
    }



    private View divider3,divider4;
    private TextView tvModelName,tvSN,lblCal,lblBandWidth;
    private Button btnCal;
    private RightViewSelect bandwidthSelect;
    private RightViewSelect probeType;
    private Channel channel;
    private  int ProbeType_Mode;
    private List<Bean> listBandWidth=new ArrayList<>();
    private void initView() {
        setClickable(true);
        rootView= (ViewGroup) inflate(context, R.layout.dialog_probe_interface,this);
        rootView.findViewById(R.id.outView).setOnTouchListener((v,e) -> {
//            Log.d("Tag.Debug", String.format("onClick: DialogProbeInterface" ));
            hide();
            return false;
        });

        tvModelName=rootView.findViewById(R.id.modelName);
        tvSN=rootView.findViewById(R.id.SN);
        btnCal=rootView.findViewById(R.id.btnCal);
        lblCal=rootView.findViewById(R.id.lblCal);
        divider3=rootView.findViewById(R.id.divider3);
        bandwidthSelect=rootView.findViewById(R.id.bandWidth);
        probeType=rootView.findViewById(R.id.ProbeType);
        divider4=rootView.findViewById(R.id.divider4);
        lblBandWidth=rootView.findViewById(R.id.lblBandWidth);

        btnCal.setOnClickListener(this::OnBtnCalClick);
        bandwidthSelect.setOnItemClickListener(onItemClickListener);
        probeType.setOnItemClickListener(onItemClickListener);


        listBandWidth.add(new Bean("500M",(long)500E6));
        listBandWidth.add(new Bean("400M",(long)400E6));
        listBandWidth.add(new Bean("300M",(long)300E6));
        listBandWidth.add(new Bean("200M",(long)200E6));
        listBandWidth.add(new Bean("150M",(long)150E6));
        listBandWidth.add(new Bean("100M",(long)100E6));
        listBandWidth.add(new Bean("50M",(long)50E6));
        listBandWidth.add(new Bean("30M",(long)30E6));
        listBandWidth.add(new Bean("5M",(long)5E6));


        hide();

    }

    class Bean{
        private String showTxt;
        private long value;
        public Bean(String showTxt,long value){
            this.showTxt=showTxt;
            this.value=value;
        }
    }

    public boolean show(Channel channel,int probeType_Mode){
        this.ProbeType_Mode=probeType_Mode;
        this.channel=channel;
        bandwidthSelect.setControlColorByChIdx(channel.getChId());
        probeType.setControlColorByChIdx(channel.getChId());

        BaseProbe baseProbe = channel.getProbe();
        boolean isProbeInterface= channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());
        if (baseProbe!=null && isProbeInterface){
            refreshUI(baseProbe);
            btnCal.setVisibility(VISIBLE);
            lblCal.setVisibility(VISIBLE);
            divider3.setVisibility(VISIBLE);
            switch (probeType_Mode){
                case RightLayoutChannel.ProbeType_MSP:{
                    refreshBandWidthUI((long) 500E6);
                    bandwidthSelect.setVisibility(GONE);
                    divider4.setVisibility(GONE);
                    lblBandWidth.setVisibility(GONE);
                }break;
                case RightLayoutChannel.ProbeType_MRCP:{
                    divider4.setVisibility(VISIBLE);
                    lblBandWidth.setVisibility(VISIBLE);
                    bandwidthSelect.setVisibility(VISIBLE);
                    refreshBandWidthUI(baseProbe.getBandWidth());
                    if (baseProbe.isScopeAdjust()==false){
                        btnCal.setVisibility(GONE);
                        lblCal.setVisibility(GONE);
                        divider3.setVisibility(GONE);
                    }
                }break;
                case RightLayoutChannel.ProbeType_MOIP:
                case RightLayoutChannel.probeType_MDP:
                default:{
                    divider4.setVisibility(VISIBLE);
                    lblBandWidth.setVisibility(VISIBLE);
                    bandwidthSelect.setVisibility(VISIBLE);

                    refreshBandWidthUI(baseProbe.getBandWidth());
                }break;
            }
            String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+(channel.getChId()+1) ) ;
            bandwidthSelect.setSelectText(fb);

            setVisibility(VISIBLE);
            this.post(()->{
                RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_PROBE_INTERFACE);
                Tools.PrintControlsLocation("dialogProbeInterface",rootView);
            });

            return true;
        }
        return false;

    }
    public void hide(){
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_PROBE_INTERFACE);
    }

    public ViewGroup getRootView(){
        return rootView;
    }
    public int getListBandWidthCount(){
        return bandwidthSelect.getSelectCount();
    }
    private void refreshUI(BaseProbe baseProbe){
        tvModelName.setText("Model:"+baseProbe.getProbeName());
        tvSN.setText("SN:"+baseProbe.getSN());


        BaseProbe bp= channel.getProbe();
        if (bp!=null) {
            setProbeTypeEnable(bp.isSupportProbeRateCtrl());
            if (bp.isSupportProbeRateCtrl()) {
                boolean isAuto = bp.isAutoRateCtrl();
                probeType.setSelectIndex(isAuto ? 1 : 0);
            }
        }
    }
    private void refreshBandWidthUI(long maxBandWidth){
        List<String> list=new ArrayList<>();
        for(int i=0;i<listBandWidth.size();i++){
            if (listBandWidth.get(i).value <= (maxBandWidth)){
                list.add(listBandWidth.get(i).showTxt+"Hz");
            }
        }
        if (list.size() == 0) {
            bandwidthSelect.setVisibility(GONE);
            return;
        } else {
            bandwidthSelect.setVisibility(VISIBLE);
        }
        bandwidthSelect.setArray(list.toArray(new String[0]));
        bandwidthSelect.clearSelect();

        String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+(channel.getChId()+1));
        if (StrUtil.isEmpty(fb)) {
            if (list.size() > 0) {
                bandwidthSelect.setSelectIndex(0);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH + (channel.getChId() + 1), list.get(0));
            }
        }else {
            bandwidthSelect.setSelectText(fb);
        }
    }

    private void setProbeTypeEnable(boolean b){
        probeType.setEnabled(b);
        probeType.setEnabled(false);
    }


    private void OnBtnCalClick(View view) {
        switch (this.ProbeType_Mode){
            case RightLayoutChannel.probeType_MDP:{
                calMDP700();
            }break;
            case RightLayoutChannel.ProbeType_MSP:{
                calMSP500();
            }break;
            case RightLayoutChannel.ProbeType_MOIP:{
                claMOIP();
            }break;
        }

    }

    private void claMOIP() {
        BaseProbe probe = channel.getProbe();
        probe.autoGain();
        lockScreen();
    }

    private void calMDP700(){
        BaseProbe probe = channel.getProbe();
        probe.autoZero();
        lockScreen();
    }

    private  void calMSP500(){
        double[] param = {channel.getChId(), VerticalAxis.DANG_50mV};
        BaseProbe baseProbe = channel.getProbe();
        if(baseProbe != null && baseProbe.isDa()) {
            ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();
            if (!probeCalibrate.isCalibrate()) {
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                if (WorkModeManage.getInstance().isYTMode()==false) {
                    WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);
                }
                ProbeCalibrate.getInstance().begin(param);
                lockScreen();
            }
        }
    }

    private void lockScreen() {
        if(channel != null) {
            ScreenControls screenControls = ScreenControls.getInstance();
            screenControls.lockScreen(ScreenControls.LOCK_PROBE << channel.getChId());
        }
    }

    private RightViewSelect.OnItemClickListener onItemClickListener =new RightViewSelect.OnItemClickListener() {
        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            DialogProbeInterface.this.onItemClick(viewId, item, false);
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {

        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };
    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus){
        if (viewId==bandwidthSelect.getId()) {
            String fb = bandwidthSelect.getSelectItem().getText().toString();
//        double hz = TBookUtil.getMHzFromHz(fb);
            int bandWidthType = Channel.BANDWIDTH_TYPE_LOWPASS;
            channel.setBandWidthType(bandWidthType, (TBookUtil.getMHzFromHz(fb) * 1000 * 1000));
            RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_CHANNEL_BANDWIDTH, fb+","+channel.getChId());
            hide();
        }else if (viewId==probeType.getId()){
            BaseProbe bp=channel.getProbe();
            if (item.getIndex()==0){
                if (bp!=null) {
                    bp.setAutoRateCtrl(false);
                }
            }else {
                if (bp!=null) {
                    bp.setAutoRateCtrl(true);
                }
            }
        }
    }
}
