package com.micsig.tbook.tbookscope.tools;

import android.content.Context;

import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Display.DisplayXYService;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.Trigger.TriggerLevel;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.probe.ProbeUtils;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * @auother Liwb
 * @description:
 * @data:2024-3-29 13:54
 */
public class RecoveryManage {
    //region  单例
    private static RecoveryManage ins = null;

    public static RecoveryManage getIns() {
        if (ins == null) {
            ins = new RecoveryManage();
        }
        return ins;
    }
    //endregion

    private  boolean Loading=false;
    private Context context;

    public  RecoveryManage(){
        RxBus.getInstance().dealObservable(RxEnum.COMPLETE,this::OnComplete);
    }

    private void OnComplete(Object obj) {
        Scope.getInstance().enableCommand(true);
        Loading=false;
    }

    public boolean isLoading(){
        return Loading;
    }

    public void init(Context context){
        this.context=context;
    }

    public void loadParam(String pathFile) throws InterruptedException {
        Loading =true;
        Scope.getInstance().enableCommand(false);
        ((MainActivity) context).preMainLoadCahceProcess();

        if (!SaveManage.getInstance().loadUserSet(pathFile, CacheUtil.get().getCacheMap())) {
            //配置载入失败则清空配置载入默认配置值
            CacheUtil.get().clearCacheMap();
            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS);
            horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0);
            DToast.get().show(R.string.saveRecoveryFileIsNotExist);
        }

//        ((MainActivity) context).updateMainLoadCaheProcess();
//        ((MainActivity) context).postMainLoadCacheProcess();
        loadFpgaParam();
        RxBus.getInstance().post(RxEnum.MAIN_LOAD_CACHE,new LoadCache());
        RxBus.getInstance().post(RxEnum.COMPLETE,new Object());
    }
    private void loadFpgaParam(){
        loadScopeState();
        loadWorkMode();
        loadChannel();
        loadMath();
        loadSerials();
    }

    private void loadScopeState() {
        Scope mScope=Scope.getInstance();
        boolean bRun = mScope.isRun();
        mScope.setRun(true);
        CacheUtil.get().checkCacheParam();
        mScope.setRun(bRun);
    }

    private void loadWorkMode(){
        int ytIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE);
        if (ytIdx==0){
            Display.getInstance().setDisplayMode(Display.DISPLAY_YT);
            boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            Scope.getInstance().setZoom(isZoom);

            boolean isRoll= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL)==0;
            if (isRoll==false){
                long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL);
                HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - l);
            }
        }else if (ytIdx==1){
            Display.getInstance().setDisplayMode(Display.DISPLAY_XY);
        }
    }
    private void loadChannel() {
        TChan.foreachChan((uiCh)->{
            int fpgaCh=TChan.toFpgaChNo(uiCh);
            Channel chan= ChannelFactory.getDynamicChannel(fpgaCh);

            boolean isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE+uiCh);
            boolean isInvert=CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT+uiCh);
            int coupleIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE+uiCh);
            int probeIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE+uiCh);
            String probeMul=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE+uiCh);
            String probeMulDef=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE+uiCh);
            int bandwidthIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH+uiCh);
            String bandwidthH=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT+uiCh);
            String bandwidthL=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT+uiCh);
//            String probBandwidth= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+uiCh);
            int verBase= CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE+uiCh);
            String labelIdx=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL+uiCh);
            String labelDef=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE+uiCh);
            String delay= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY+uiCh);
            String offset =CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_OFFSET+uiCh);
            boolean fine=CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE+uiCh);
            String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+uiCh);

            chan.setOpen(isOpen);
            chan.setInvert(isInvert);
            chan.setCoupleType(coupleIdx);
            chan.setProbeType(probeIdx);
            chan.setProbeRate(TBookUtil.getDoubleFromX(probeMul));
            switch (bandwidthIdx){
                case Channel.BANDWIDTH_TYPE_FULL:{
                    chan.setBandWidthType(bandwidthIdx,Channel.getMaxBandWidth());
                }break;
                case Channel.BANDWIDTH_TYPE_200M:{
                    chan.setBandWidthType(bandwidthIdx,200*1e6);
                }break;
                case Channel.BANDWIDTH_TYPE_300M:{
                    chan.setBandWidthType(bandwidthIdx,300*1e6);
                }break;
                case Channel.BANDWIDTH_TYPE_HIGHPASS:{
                    chan.setBandWidthType(bandwidthIdx, TBookUtil.getMHzFromHz(bandwidthH)*1e6);
                }break;
                case Channel.BANDWIDTH_TYPE_LOWPASS:{
                    chan.setBandWidthType(bandwidthIdx,TBookUtil.getMHzFromHz(bandwidthL)*1e6);
                }break;
                case Channel.BANDWIDTH_TYPE_20M:{
                    chan.setBandWidthType(bandwidthIdx,20*1e6);
                }break;
            }
            chan.setVerticalMode(verBase);
            chan.setChOffsetVal(TBookUtil.getDoubleFromM(offset.replace("A","").replace("V","").replace(" ","")));
            chan.setDelay((int)(TBookUtil.getDoubleFromM(delay.replace("s", "")) * 1e12 + 0.1));
            chan.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));

            //位置
            double pos=0;
            if (isYt()){
                int h = (isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2;
                pos = h - Tools.getChannelPositionUI(uiCh);
                chan.setPos(pos);
            }else if (isXy()){
                if (uiCh==TChan.Ch1){
                    pos= CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch1);
                    DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) Math.round(pos));
                }else if (uiCh==TChan.Ch2){
                    pos=CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch2);
                    DisplayXYService.getInstance().setY(ScopeBase.getXYWidth() / 2 - (int) Math.round(pos));
                }
            }

            //档位
            int vScaleId=CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID +uiCh);
            chan.setVScaleId(vScaleId);
            int val = Scope.vSpanOfView(chan.getResistanceType(),chan.getVScaleVal() / chan.getProbeRate());
            chan.setVRange(-val, val);

        },(ui)->ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ui))==null);


        //触发电平
        Trigger trigger = TriggerFactory.getInstance().getTrigger();
        int src=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
        int trigType= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (trigType== TopLayoutTrigger.DETAIL_SLOPE || trigType==TopLayoutTrigger.DETAIL_RUNT){
            TriggerLevel triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, src);
            triggerLevel.setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + src),true);
            triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src);
            triggerLevel.setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + src), true);
        }else {
            double vol = Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + src);
            TriggerLevel triggerLevel = TriggerFactory.getInstance().getTrigger().getTriggerLevel(src);
            triggerLevel.setPos(vol, true);
        }
    }
    private void loadMath(){
        TChan.foreachMath((uiMath)->{
            int fpgaCh=TChan.toFpgaChNo(uiMath);
            MathChannel math= ChannelFactory.getMathChannel(fpgaCh);


        });
    }
    private void loadSerials(){
        TChan.foreachSerial((uiS)->{
            //正常的属性设置

            //阈值电平
            int discreetType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + uiS);
            if (discreetType == RightLayoutSerials.SERIALS_M429) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            } else if (discreetType == RightLayoutSerials.SERIALS_CAN) {
                int srcCLK = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + TChan.toSerialNumber(uiS));
                int srcData = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + TChan.toSerialNumber(uiS));
                boolean csEnable = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + TChan.toSerialNumber(uiS));
                if (csEnable) {
                    int srcCs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + TChan.toSerialNumber(uiS));
                    setDiscreetPos(srcCs);
                }
                setDiscreetPos(srcCLK);
                setDiscreetPos(srcData);

            } else if (discreetType == RightLayoutSerials.SERIALS_I2C) {
                int srcData = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + TChan.toSerialNumber(uiS));
                int srcCLK = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + TChan.toSerialNumber(uiS));
                setDiscreetPos(srcData);
                setDiscreetPos(srcCLK);
            } else if (discreetType == RightLayoutSerials.SERIALS_UART) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            } else if (discreetType == RightLayoutSerials.SERIALS_LIN) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            } else if (discreetType == RightLayoutSerials.SERIALS_CAN) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            } else if (discreetType == RightLayoutSerials.SERIALS_M429) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            } else if (discreetType == RightLayoutSerials.SERIALS_M1553B) {
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + TChan.toSerialNumber(uiS));
                setDiscreetPos(src);
            }

        });
    }
    private void setDiscreetPos(int fpgaChan){
        int chIdx=TChan.toUiChNo(fpgaChan);
        Channel channel = ChannelFactory.getDynamicChannel(fpgaChan);
        if (channel != null) {
            double vol1 = Tools.getYTLevelCache(CacheUtil.VALUE_CHANNEL + chIdx);
            {
                if (isValueLevelHighAndLow(chIdx)) {
                    channel.setBusSecondaryLevel(vol1);
                } else {
                    channel.setBusPrimaryLevel(vol1);
                }
            }

            double vol2 = Tools.getYTLevelCache(CacheUtil.VALUE_CHANNEL_H + chIdx);
            {
                if (isValueLevelHighAndLow(chIdx)) {
                    channel.setBusPrimaryLevel(vol2);
                } else {
                    channel.setBusSecondaryLevel(vol2);
                }
            }
        }
    }
    public static boolean isValueLevelHighAndLow(int uiChan) {
        boolean b= TChan.foreachSerialResult((uiCh)->{
                    int type=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + TChan.toSerialNumber(uiCh));
                    int src= CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiCh));
                    boolean b1=type==RightLayoutSerials.SERIALS_M429 && src==TChan.toFpgaChNo(uiChan);
                    return b1;
                });
        return b;
    }

    private boolean isYt(){
        return CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE)==0;
    }
    private boolean isXy(){
        return CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE)==1;
    }
    private boolean isZoom(){
        boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
        return isZoom;
    }
    private boolean isRoll(){
        boolean isRoll= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL)==0;
        return isRoll;
    }

}
