package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.tbook.scope.Auto.Auto;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.probe.ProbeFactory;
import com.micsig.tbook.scope.vertical.VerticalAxis;

public class MHO68v1_ProbeDACalibrate extends Calibrate {
    public MHO68v1_ProbeDACalibrate(int calibrateType) {
        super(calibrateType);

    }

    private int errcode;
    private final String TAG = "ChProbeDA";
    private final String TAG1=TAG_PRI+":"+TAG;

    private volatile int ch=0;
    private volatile int dangwei=VerticalAxis.DANG_50mV;

    private volatile int meatCnt=0;
    private volatile int step;
    private volatile double average[][] = {
            new double[ChannelFactory.CH_CNT],
            new double[ChannelFactory.CH_CNT]
    };
    private volatile double[] minVal = {
            Double.MAX_VALUE, Double.MAX_VALUE,
            Double.MAX_VALUE, Double.MAX_VALUE,
            Double.MAX_VALUE, Double.MAX_VALUE,
            Double.MAX_VALUE, Double.MAX_VALUE
    };
    private volatile int[] dcpVal = new int[ChannelFactory.CH_CNT];
    private volatile int[] minNum = new int[ChannelFactory.CH_CNT];;
    @Override
    public String getTAG() {
        return TAG1;
    }

    @Override
    public int getErrcode() {
        //=1：校验错误
        return errcode;
    }
    @Override
    public void setErrcode(int err){
        errcode  = err;
    }
    //校准下一档初始化
    private void rstCalculate(){
        for(int j=0; j< 2;j++) {
            for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
                average[j][i] =0;
                minVal[i] = Double.MAX_VALUE;
                dcpVal[i] = 0;
                minNum[i] = 0;
            }
        }
    }
    private int triggerType;
    private int triggerMode;
    private long triggerHoldOffTime;
    private int triggerCouple;
    private int triggerEdge;
    private int triggerSource;
    private int sampleType;
    private int memDepthItem;
    private int drawType;

    private int displayMode;

    private boolean bZoom;

    private int timeScaleId;

    private long timePos;

    private int persistAdjustTime;
    private int persistType;
    private int horRef;
    @Override
    public void iniCalibrateReg(){
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        triggerType = TriggerFactory.getTriggerType();
        triggerMode = triggerFactory.getTriggerCommon().getTriggerMode();
        triggerHoldOffTime = triggerFactory.getTriggerCommon().getTriggerHoldOffTime();
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        triggerCouple = triggerEdge.getTriggerCouple();
        this.triggerEdge = triggerEdge.getTriggerEdge();
        triggerSource = triggerEdge.getTriggerSource();
        sampleType = Sample.getInstance().getSampleType();
        memDepthItem = MemDepthFactory.getMemDepth().getMemDepthItem();
        Display display = Display.getInstance();
        drawType = display.getDrawType();
        displayMode = display.getDisplayMode();
        bZoom = display.isZoom();
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
        timeScaleId = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);
        timePos = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD);
        persistAdjustTime = display.getPersistAdjustTime();
        persistType = display.getPersistType();
        horRef = display.getHorRef();
    }

    @Override
    public void initCalibrateConfig(){
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        //修改为边沿触发
        if(TriggerFactory.getTriggerType() != Trigger.TRIG_TYPE_EDGE){
            triggerFactory.setTriggerType(Trigger.TRIG_TYPE_EDGE);
        }
        //采用自动触发
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_AUTO);
        //触发抑制时间调小
        triggerFactory.getTriggerCommon().setTriggerHoldOffTime(200/4);
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        //触发耦合：直流
        triggerEdge.setTriggerCouple(TriggerEdge.COUPLING_DIRECT);
        //调整每个通道的触发位置到0

        //上升沿触发
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        //触发源设置为通道1
        triggerEdge.setTriggerSource(0);
        //退出自动
        Auto.getInstance().setAuto(false);
        //设置采样模式为正常模式
        Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);
        //设置存储深度强制类型为14M
        memDepthSetBak = MemDepthFactory.getMemDepthSet();
        MemDepthFactory.forceMemDepth(MemDepthFactory.getDefaultMemDepth());
        //存储深度大小设置为140k
        MemDepthFactory.getMemDepth().setMemDepthItem(4);
        //波形显示为线模式
        Display.getInstance().setDrawType(Display.DRAWTYPE_LINE);
        //YT模式
        Display.getInstance().setDisplayMode(Display.DISPLAY_YT);
        //退出Zoom
        Display.getInstance().setZoom(false);
        //时基
        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_2mS);
        //触发时刻
        HorizontalAxis.getInstance().setTimePosOfView(0);
        //余辉时间500ms
        Display.getInstance().setPersistAdjustTime(200);
        //余辉模式：自动
        Display.getInstance().setPersistType(Display.PERSIST_TYPE_AUTO);
        //以屏幕中心进行x轴缩放
        Display.getInstance().setHorRef(Display.HORREF_CENTER);
//        //通道设置
//        for(int i=0; i<channelNums; i++) {
//            //全带宽
//            channel[i].setBandWidthType(Channel.BANDWIDTH_TYPE_FULL,20e6);
//
//            channel[i].setInvert(false); //关闭反相
//            channel[i].setCoupleType(Channel.COUPLE_TYPE_DC); //DC耦合
//            //如果有输入阻抗控制，则设置为1M输入阻抗
//            channel[i].setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);
//
//            //100mV档位
//            channel[i].setVScaleId(VerticalAxis.DANG_100mV);
//            channel[i].setFineScale(1.0); //精细电压档位为1.0
//            channel[i].setProbeType(VerticalAxis.PROBE_TYPE_VOL); //类型为电压
//            channel[i].setProbeRate(10); //10X探头
//            channel[i].setPos(0); //通道纵向位置归零
//
//        }
//        //打开所有通道
//        for(int i=0; i<channelNums; i++) {
//            ChannelFactory.chOpen(i);
//
//        }
//        //关闭数学通道
//        ChannelFactory.chClose(ChannelFactory.MATH_CH);
//        //关闭串口通道
//        ChannelFactory.chClose(ChannelFactory.S1);
//        ChannelFactory.chClose(ChannelFactory.S2);
    }
    /**
     * 设置校准参数
     * @param vol
     * vol为int[];
     * 第1个值为通道：0~3,-1；（-1表示所有通道）
     * 第2个值为档位；
     */
    @Override
    public void setParam(Object vol) {
        ch = ChannelFactory.CH1;
        dangwei = VerticalAxis.DANG_50mV;

        if(vol instanceof double[]) {
            double[] param=(double[])vol;
            if(param.length >= 2){
                int ix =(int)(param[0] + 0.1);
                if(param[0] < 0){
                    ix = -1;
                }
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {
                    ch = ix;
                    ix =(int)(param[1] + 0.1);
                    if(VerticalAxis.isValidScaleId(ix)) {
                        dangwei = ix;
                    }
                }
            }
        }
    }

    int []param=new int[2];
    @Override
    public Object getParam() {
        param[0] = ch;
        param[1] = dangwei;
        return param;
    }

    long startTime = 0;


    int delay = 0;
    double offsetVal = 0;

    @Override
    protected void restoreCalibrateConfig() {

        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        triggerFactory.setTriggerType(triggerType);

        triggerFactory.getTriggerCommon().setTriggerMode(triggerMode);
        triggerFactory.getTriggerCommon().setTriggerHoldOffTime(triggerHoldOffTime);
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        triggerEdge.setTriggerCouple(triggerCouple);
        triggerEdge.setTriggerEdge(this.triggerEdge);
        triggerEdge.setTriggerSource(triggerSource);
        Sample.getInstance().setSampleType(sampleType);
        MemDepthFactory.getMemDepth().setMemDepthItem(memDepthItem);
        Display display = Display.getInstance();
        display.setDrawType(drawType);
        display.setDisplayMode(displayMode);
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD,timeScaleId);
        horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD,timePos);
        display.setPersistAdjustTime(persistAdjustTime);
        display.setPersistType(persistType);
        display.setHorRef(horRef);
        Scope.getInstance().setZoom(bZoom);
    }
    @Override
    public void endCalibrate(){
        super.endCalibrate();

        channel[ch].setDelay(delay);
        channel[ch].setChOffsetVal(offsetVal);
    }
    @Override
    public void calibratePrepare() {

        delay = 0;
        offsetVal = 0;
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();

        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_20uS);
        horizontalAxis.setTimePoseOfViewPix(ScopeBase.getWidth()/2-ScopeBase.getHorizonPerGridPixels()/2);
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        ms_sleep(500);
        if(ChannelFactory.isDynamicCh(ch)){
            triggerEdge.setTriggerSource(ch);
        }else{
            triggerEdge.setTriggerSource(0);
            ch = -1;
        }
        horizontalAxis.setTimePoseOfViewPix(ScopeBase.getWidth()/2-ScopeBase.getHorizonPerGridPixels()/2);

        ms_sleep(500);
        if(ch < 0){
            for(int i=0; i<channelNums; i++){
                refVal[i] = CENTER_DA_VAL;
                channel[i].setProbeDaVal(MIN_DA_VAL + refVal[ch]);
                channel[i].setVScaleId(dangwei);
                channel[i].setPos(-ScopeBase.getVerticalPerGridPixels() * 2);
                channel[i].setChOffsetVal(0);
                for(int j=VerticalAxis.DANG_MIN;j<=VerticalAxis.DANG_MAX;j++){
                    channel[i].setZero(j,0);
                }
            }
        }else{
            refVal[ch] = CENTER_DA_VAL;


            channel[ch].setBandWidthType(Channel.BANDWIDTH_TYPE_FULL,Channel.getMaxBandWidth());

            channel[ch].setInvert(false); //关闭反相
            channel[ch].setCoupleType(Channel.COUPLE_TYPE_DC); //DC耦合
            //如果有输入阻抗控制，则设置为1M输入阻抗
            channel[ch].setVerticalMode(Channel.VERTICAL_MODE_CH_ZERO);

            channel[ch].setFineScale(1.0); //精细电压档位为1.0
            channel[ch].setProbeType(VerticalAxis.PROBE_TYPE_VOL); //类型为电压
            channel[ch].setProbeRate(10); //10X探头
            channel[ch].setProbeDaVal(MIN_DA_VAL + refVal[ch]);
            channel[ch].setVScaleId(dangwei);
//            for(int i=0;i<channelNums;i++)
            {
                channel[ch].setPos(-ScopeBase.getVerticalPerGridPixels() * 2);
            }
            offsetVal = channel[ch].getChOffsetVal();
            delay = channel[ch].getDelay();
            channel[ch].setDelay(0);
            channel[ch].setChOffsetVal(0);
            for(int j=VerticalAxis.DANG_MIN;j<=VerticalAxis.DANG_MAX;j++){
                channel[ch].setZero(j,0);
            }
        }
        horizontalAxis.setTimePoseOfViewPix(ScopeBase.getWidth()/2-ScopeBase.getHorizonPerGridPixels()/2);
        ms_sleep(500);
        triggerEdge.getTriggerLevel().setPos(ScopeBase.getVerticalPerGridPixels() * 2);
        rstCalculate();
        meatCnt = 0;
        step = 0;
        errcode = 0;
        resultString.add("<<<<<<<<<< ProbeDaCalibrate start ......" + "ch:" + ch + ",dangwei:" + dangwei);
        Log.i(TAG1,"<<<<<<<<<< ProbeDaCalibrate start ......" + "ch:" + ch + ",dangwei:" + dangwei);
        startTime = SystemClock.elapsedRealtime();
    }
    public boolean checkParam(){
        boolean bChange = false;
        int chIdx = ch;
        if(ch == -1){
            chIdx = 0;
        }
        if(ChannelFactory.isDynamicCh(chIdx)){
            TriggerFactory triggerFactory=TriggerFactory.getInstance();

            TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
            if(!triggerEdge.isTriggerSource(chIdx)){
                triggerEdge.setTriggerSource(chIdx);
                bChange = true;
            }

            if(triggerEdge.getTriggerLevel().getPos() != (ScopeBase.getVerticalPerGridPixels() * 2)){
                triggerEdge.getTriggerLevel().setPos(ScopeBase.getVerticalPerGridPixels() * 2);
                bChange = true;
            }
        }
        if(bChange){
            for(int i=0; i<channelNums; i++) {
                refVal[i] = CENTER_DA_VAL;
                if (ch == i || ch == -1) {
                    BaseProbe probe = channel[i].getProbe();
                    if (probe != null && probe.isDa()) {
                        probe.setDaValue(MIN_DA_VAL + CENTER_DA_VAL);
                    }
                }
            }
            rstCalculate();
            delaySet(2);
        }
        return bChange;
    }
    public static final int MIN_DA_VAL = 28839;
    public static final int MAX_DA_VAL = 65535;
    public static final int DEFAULT_DA_VAL = 39321;
    public static final int CENTER_DA_VAL = (MAX_DA_VAL - MIN_DA_VAL) / 2;
    private volatile int [] refVal = {CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL,CENTER_DA_VAL};
    private static final int STD_VAL = ScopeBase.getVerticalPerGridPixels() * 4;
    @Override
    public boolean onCalibrate() {
        //等待硬件操作完成

        if (!isFinishedAction())
            return false;
        if(step == 0){
            delaySet(2);
            step++;
            return false;
        }
        if(step == 1){
            delaySet(0);
            step++;
            meatCnt = 0;
            return false;
        }
        if(meatCnt == 0){
            delaySet(0);
        }
        HwConfig hwConfig = HwConfig.getInstance();
        WaveData waveData;
        int N;
        int gridCnt = ScopeBase.getHorizonGridCnt();
        int xx = ScopeBase.getHorizonPerGridPixels();
        //获取每个通道的波形平均值
        for (int i = 0; i < channelNums; i++) {
            if(ch == i || ch == -1) {
                waveData = (WaveData) getWave(i);
                if (waveData == null || (N = waveData.getWaveLength()) < 10)
                    return false;
                int len1 = N / gridCnt;
                int len2 = len1 / xx;
                N = len1;
                average[0][i] += (double) MathNative.calcSum(waveData.getByteBuffer(), N + len2 * 4, len2) / len2;
                average[1][i] += (double) MathNative.calcSum(waveData.getByteBuffer(), N + len1 * 10, len2) / len2;
            }
        }
        meatCnt++;
//        Log.i(TAG,"step:"+step + ",channelNums:" + channelNums + ",ch:" + ch);

        if(meatCnt >= 2) {
            for (int i = 0; i < channelNums; i++) {
                average[0][i] /= meatCnt;
                average[1][i] /= meatCnt;
                average[0][i] *= hwConfig.getWavFactor();
                average[1][i] *= hwConfig.getWavFactor();
                if(ch == i || ch == -1){
                    if(Math.abs(average[1][i] - STD_VAL)/STD_VAL > 0.25){
                        errcode = 705;
                        return true;
                    }
                }
            }
        }else {
            return false;
        }
        meatCnt = 0;
        if(step == 2){
            boolean bFaild = false;
            double TVAL = 0.1;
            boolean[] bOk = new boolean[channelNums];
            for (int i = 0; i < channelNums; i++) {
                if (ch == i || ch == -1) {
                    BaseProbe probe = channel[i].getProbe();
                    if(probe != null && probe.isDa()){
                        double val = average[1][i] - average[0][i];
                        if(Math.abs(val) < minVal[i]){
                            minVal[i] = Math.abs(val);
                            dcpVal[i] = probe.getDaValue();
                            minNum[i] = 0;
                        }else{
                            minNum[i]++;
                        }

                        refVal[i] /= 2;
                        if(refVal[i] == 0){
                            refVal[i] = 1;
                        }
                        if(minNum[i] > 6){
                            probe.setDaValue(dcpVal[i]);
                            ProbeFactory.getInstance().setProbeDa(probe.getChIdx(),probe.getSN(),probe.getDaValue());

                            if(Math.abs(val) > 1){
                                bFaild = true;
                            }else {
                                bOk[i] = true;
                            }
                        }else if(Math.abs(val) > TVAL) {
                            int xVal = probe.getDaValue();
                            if (val > TVAL) {
                                xVal += refVal[i];
                                if(xVal > MAX_DA_VAL){
                                    xVal = MAX_DA_VAL;
                                    if(probe.getDaValue() == MAX_DA_VAL){
                                        bFaild = true;
                                        xVal = dcpVal[i];
                                    }
                                }
                            } else if (val < -TVAL) {
                                xVal -= refVal[i];
                                if(xVal < MIN_DA_VAL){
                                    xVal = MIN_DA_VAL;
                                    if(probe.getDaValue() == MIN_DA_VAL){
                                        bFaild = true;
                                        xVal = dcpVal[i];
                                    }
                                }
                            }
                            Log.d(TAG, "xVal:" + xVal);
                            probe.setDaValue(xVal);
                            ProbeFactory.getInstance().setProbeDa(probe.getChIdx(),probe.getSN(),probe.getDaValue());
                        }
                        else{
                            bOk[i] = true;
                        }
                    }else{
                        bOk[i] = true;
                    }
                }
                average[0][i] = 0;
                average[1][i] = 0;
            }
            boolean bx = false;
            if(ch == -1){
                int i =0;
                for(;i<channelNums;i++){
                    if(!bOk[i]){
                       break;
                    }
                }
                bx = (i == channelNums);
            }else{
                bx = bOk[ch];
            }
            if(bx){
                step++;
            }
            delaySet(4);

            if(bFaild){
                errcode = 703;
                return true;
            }
        }
        if((SystemClock.elapsedRealtime() - startTime) > 60 * 1000){
            errcode = 1001;
            return true;
        }

        if (step != 3) {

            return false;
        }

        resultString.add("chCapCalibrate Calibrate end >>>>>>>>>>>");
        return true;
    }
}
