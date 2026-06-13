package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Calibrate.MHO68v2.HW_MHO68V2;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.Arrays;

/**
 * Created by xuj on 2018/7/18.
 * 通道增益校准，每次校准1个通道，或4个通道同时校准，但每次只能校准1个档位
 * 输入校准信号标准：
 *   1. 30Hz交流方波，占空比50%；
 *   2. 方波高电压scale*4(V)；;
 *   3. 方波低电压-scale*4(V)；
 */

public class MHO68v1_ChGainCalibrate extends Calibrate {
    public MHO68v1_ChGainCalibrate(int calibrateType) {
        super(calibrateType);
        delaySet(2);
    }

    private int errcode;
    private final String TAG = "ChGain";
    private final String TAG1=TAG_PRI+":"+TAG;
    private volatile int ch=0;
    private volatile int chIdx = 0;

    private int vPos = 100;//校准方波的基准幅度值应为8*50个像素
    private int vMax = 100*5;
    private volatile int step;
    private int meatCnt;
    private volatile double srcAmp;
    private volatile double vScaleVal;
    private volatile int chMode;


    private volatile int idx;


    private double average[] = new double[ChannelFactory.CH_CNT];
    private double min[] = new double[ChannelFactory.CH_CNT];
    private double max[] = new double[ChannelFactory.CH_CNT];

    private double[] best_value = new double[ChannelFactory.CH_CNT];
    private short[] bakCoef = new short[ChannelFactory.CH_CNT];
    private int[] state = new int[ChannelFactory.CH_CNT];
    private int[] tryNum = new int[ChannelFactory.CH_CNT];
    private int[] tryChiShu = new int[ChannelFactory.CH_CNT];
    private int[] trypga = new int[ChannelFactory.CH_CNT];
    private boolean[] finished = new boolean[ChannelFactory.CH_CNT];

    private boolean[] ch_en = new boolean[ChannelFactory.CH_CNT];

    private ChannelHardw channelHardw;
    private HW_MHO68V1 hw;

    @Override
    public String getTAG() {
        return TAG1;
    }

    @Override
    public int getErrcode() {
        //=1：校验错误
        return errcode;
    }

    //校准下一档初始化
    private void rstCalculate(){
        for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
            best_value[i] = Float.MAX_VALUE;
            state[i] = 0;
            average[i] = 0;
            max[i] = -Double.MAX_VALUE;
            min[i] = Double.MAX_VALUE;
            tryNum[i] = 0;
            tryChiShu[i] = 0;
            trypga[i] = 0;
        }
        meatCnt = 0;
    }

    @Override
    public void iniCalibrateReg(){
        channelHardw = ChannelHardw.getInstance();
        hw = (HW_MHO68V1) cabteRegister.getHw();
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
        if(vol instanceof double[]) {
            double[] param=(double[])vol;
            if(param.length >= 3){
                int ix =(int)(param[0] + 0.1);
                if(param[0] < 0){
                    ix = -1;
                }
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {
                    ch = ix;
                    vScaleVal = param[1];
                    srcAmp = vScaleVal * 8;
                    chMode = (int)(param[2] + 0.1);
                    idx = (int)(param[3] + 0.1);
                    Logger.d(TAG,"ch:" + ch
                            + ",vScaleVal:" + vScaleVal
                            + ",srcAmp:" + srcAmp
                            + ",chMode:" + chMode
                            + ",idx:" + idx
                    );
                    if(idx < 0 || idx >= HW_MHO68V1.GAIN_PGA_CODE.length){
                        idx = 0;
                    }
                    if(idx < 3){
                        chMode = 3;
                    }

                    return;

                }
            }
        }
        ch = ChannelFactory.CH1;
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_10mV);
    }

    int []param = new int[2];
    @Override
    public Object getParam() {
        param[0] = ch;
        param[1] = idx;
        return param;
    }
    private void setPGAVal(int val){
        FPGACommand fpgaCommand = FPGACommand.getInstance();
        fpgaCommand.cmdDevice(300);
        int [] res ={val,val,val,val,val,val,val,val};
        channelHardw.set_ch_AD8370Gain(res);
        fpgaCommand.setPgaVal(val);
        fpgaCommand.cmdDevice(300);
    }

    volatile int ratioIdx = 0;

    int adfs = 0;
    int pgaVal = 0;
    int resistanceType = Channel.RESISTANCE_1M;
    @Override
    public void calibratePrepare() {

        cabteRegister.setNewInputV(-1);
        for(int i=0; i<finished.length; i++) {
            finished[i] = true;
            ch_en[i] = false;
        }

        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_100uS);
        FPGACommand.getInstance().setADdiffGainCalib(true);
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        triggerEdge.setTriggerCouple(TriggerEdge.COUPLING_NOISERS);
        Logger.d(TAG,"idx:" + idx + ",vScaleVal:" + vScaleVal);


        ratioIdx = HW_MHO68V1.RATIO_DANG_2;
        resistanceType = Channel.RESISTANCE_1M;

        pgaVal = HW_MHO68V1.GAIN_PGA_CODE[idx];
        adfs = HW_MHO68V1.GAIN_ADFS_CODE[idx];

        if(idx < 3){
            ratioIdx = HW_MHO68V1.RATIO_DANG_2;
        }else{
            ratioIdx = HW_MHO68V1.RATIO_DANG_1 + idx / 3 - 1;
            if(idx >= 15){
                resistanceType = Channel.RESISTANCE_50;
                ratioIdx = (idx - 15) / 3;
            }
        }
        double x = 1;
        Log.d(TAG,"ch:" + ch + ",idx:" + idx + ",pga:" + pgaVal + ",ratioIdx:" + ratioIdx);
        cabteRegister.rst_coefChannel(ch,ratioIdx,resistanceType);
        ms_sleep(100);
        int triSrc = Math.max(ch,0);
        if(ch < 0) {
            triggerEdge.setTriggerSource(triSrc);
            x = channel[0].getADVerticalPerPix()/channel[0].getProbeRate();
            for(int k = 0;k<channelNums;k++){
                finished[k] = false;
                ch_en[k] = true;
            }
            chIdx = 0;

        } else {

            if(chMode == 2){
                switch (ch){
                    case 0:
                    case 4:
                        triSrc = ch + 1;
                        break;
                    case 1:
                    case 5:
                        triSrc = ch - 1;
                        break;
                }
            }

            triggerEdge.setTriggerSource(triSrc);
            x = channel[ch].getADVerticalPerPix()/channel[ch].getProbeRate();
            finished[ch] = false;
            ch_en[ch] = true;
            chIdx = ch;
        }
        ms_sleep(200);
        for(int i=0; i<channelNums; i++) {
            channel[i].setProbeRate(1);
            channel[i].setVScaleId(CabteRegister.getRatioIdx2Dang(resistanceType,ratioIdx));
            channel[i].setResistanceType(resistanceType);
            x = channel[i].getADVerticalPerPix()/channel[i].getProbeRate();
            channel[i].setPos(-(int)(srcAmp / (2 * x)));
        }
        step = 0;
        chflag = 0;
        int levelPos = -channel[triSrc].getYPos();
        triggerEdge.getTriggerLevel().setPos(levelPos);
        setPgaAndAdfs();

        for(int i=0;i<3;i++){
            ms_sleep(1100);
            checkParam();
        }
        ms_sleep(150);
        setPgaAndAdfs();
        rstCalculate();
        fpgaSync();
        delaySet(2);
        errcode = 0;
        resultString.add("chGainCalibrate start");
        Log.i(TAG1,"chGainCalibrate start");

    }
    private void setPgaAndAdfs(){
        if(idx >= 3) {
            switchChannel();
        }
        setPGAVal(pgaVal);
        FPGACommand fpgaCommand = FPGACommand.getInstance();
        for(int i=0;i<channelNums;i++){
            fpgaCommand.writeAD_gain(i/4,0,i%4, adfs);
        }
        fpgaCommand.ADCalibrate();
    }

    @Override
    public boolean checkParam() {
        boolean bChange = false;
        int chidx = Math.max(ch,0);
        if(ChannelFactory.isDynamicCh(chidx)) {
            for(int i=0;i<channelNums;i++){
                if(channel[i].getResistanceType() != resistanceType){
                    bChange = true;
                    break;
                }
            }
            if(bChange){
                updateSync();
                for(int i=0;i<channelNums;i++){
                    if(channel[i].getResistanceType() != resistanceType){
                        channel[i].setResistanceType(resistanceType);
                    }
                }
            }

            int triSrc = ch;
            if(chMode == 2){
                switch (ch){
                    case 0:
                    case 4:
                        triSrc = ch + 1;
                        break;
                    case 1:
                    case 5:
                        triSrc = ch - 1;
                        break;
                }
            }
            TriggerFactory triggerFactory = TriggerFactory.getInstance();

            TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
            if (!triggerEdge.isTriggerSource(triSrc)) {
                triggerEdge.setTriggerSource(triSrc);
                bChange = true;
            }

            double levelPos = -channel[triSrc].getPos();
            if (triggerEdge.getTriggerLevel().getPos() != levelPos) {
                triggerEdge.getTriggerLevel().setPos(levelPos);
                bChange = true;
            }
        }
        if(bChange){
            ms_sleep(100);
            setPgaAndAdfs();
            rstCalculate();
            fpgaSync();
        }
        return bChange;
    }


    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    private boolean isCheckSig(double v){
        double vv = ScopeBase.getVerticalPerGridPixels() * 8;
        return (Math.abs(v - vv) / vv) < 0.05;
    }
    private volatile int chflag = 0;
    @Override
    public boolean onCalibrate() {
        //等待硬件操作完成

        if (!isFinishedAction())
            return false;

        delaySet(0);
        if(idx < 0){
            setErrcode(3005);
            return true;
        }
        if(checkParam()){
            return false;
        }
        double sum1,sum2,m1,m2;
        WaveData waveData;
        HwConfig hwConfig = HwConfig.getInstance();
        int N;

        //获取每个通道的波形平均值
        for (int i = 0; i < channelNums; i++) {

            waveData = (WaveData) getWave(i);
            if (waveData == null || (N = waveData.getWaveLength()) < 10)
                return false;

            int n = N / ScopeBase.getHorizonGridCnt();
            int len1 = n * (ScopeBase.getHorizonGridCnt()/2 - 2);
            sum1 = MathNative.calcSum(waveData.getByteBuffer(), n, len1);
            sum2 = MathNative.calcSum(waveData.getByteBuffer(), n * (ScopeBase.getHorizonGridCnt()/2 + 1), len1);
            m1 = MathNative.calcMax(waveData.getByteBuffer(),n * (ScopeBase.getHorizonGridCnt()/2 + 1), len1) ;
            m2 = MathNative.calcMin(waveData.getByteBuffer(),n,len1) ;
            average[i] += Math.abs(sum2/len1 - sum1/len1);

            double chPos = channel[i].getPos();
            int h = ScopeBase.getHeight()/2;
            if(chPos < -h){
                chPos = -h;
            }else if(chPos > h){
                chPos = h;
            }

            min[i] = m2 * hwConfig.getWavFactor()  + chPos;
            max[i] = m1 * hwConfig.getWavFactor()  + chPos;

        }
        boolean bPosChange = false;
        for(int i=0;i<channelNums;i++){
            if((!ch_en[i])){
                continue;
            }
            int h = ScopeBase.getHeight()/2;
            double chPos = channel[i].getPos();

            int v = ScopeBase.getVerticalPerGridPixels()/4;
            double coef = cabteRegister.vol_ChannelCoef_defaultEx(i,CabteRegister.getRatioIdx2Dang(resistanceType,ratioIdx),pgaVal);
            double coef1 = cabteRegister.calc_coefChannel(i,channel[i].getVScaleVal()/channel[i].getProbeRate(),0);

            double minVal = min[i];
            double maxVal = max[i];
            if(idx >= 3){
                switch (chMode){
                    case 3://4通道
                        minVal = min[i];
                        maxVal = max[i];
                        break;
                    case 0://单通道1-5
                    case 1://单通道2-6
                        if(i % 4 < 2) {
                            int n = (i / 4) * 4;
                            minVal = min[n];
                            maxVal = max[n];
                            for (int j = 0; j < 4; j++,n++) {
                                if(min[n] < minVal ){
                                    minVal = min[n];
                                }
                                if(max[n] > maxVal){
                                    maxVal = max[n];
                                }
                            }
                        }
                        break;
                    case 2://双通道12,56
                        if(i % 4 < 2){

                            int n = 0;
                            if (i % 2 == 0) { // 1,5
                                n = (i / 4) * 4 ;
                                minVal = Math.min(min[n + 1],min[n + 3]);
                                maxVal = Math.max(max[n + 1],max[n + 3]);

                            } else { // 2 , 6
                                n = (i / 4) * 4;
                                minVal = Math.min(min[n + 2],min[n + 0]);
                                maxVal = Math.max(max[n + 2],max[n + 0]);
                            }

                        }
                        break;
                }
            }else{
                minVal = min[i];
                maxVal = max[i];
            }

            if(minVal < -h){
                chPos += Math.max((h - maxVal) / 2,v) * coef / coef1;
                bPosChange = true;
            }else if(maxVal > h){
                chPos -= Math.max((h + minVal) / 2,v) * coef / coef1;
                bPosChange = true;
            }
            if(bPosChange){
                channel[i].setPos(chPos);
            }
        }

        if(bPosChange){
            ms_sleep(100);
            if(!checkParam()) {
                setPgaAndAdfs();
                rstCalculate();
                updateSync();
            }
            return false;
        }

        if(++meatCnt < 5) {
            return false;
        }

        for(int i=0; i<channelNums; i++){
            average[i] = average[i] / meatCnt;
            average[i] *= hwConfig.getWavFactor();
            Log.d(TAG,"ch:" + i + ",average:" + average[i] + ",ch_en:" + ch_en[i]);
        }

        meatCnt = 0;

        double [] newAmp = new double[ChannelFactory.CH_CNT];
        Arrays.fill(newAmp,0);
        int ampCnt = 0;
        boolean bCheckSig = true;

        for(int i=0; i<channelNums; i++) {
            //状态机控制流程
            if((!ch_en[i])){
                continue;
            }
            chflag |= (1 << i);
            if(idx < 3){
                switch(idx){
                    case 0:
//                        CabteRegister.gain_pga_stepdb_a1[i] = average[i];
                        hw.setGainPgaA1(i,average[i]);
                        break;
                    case 1:
//                        CabteRegister.gain_pga_stepdb_a2[i] = average[i];
                        hw.setGainPgaA2(i,average[i]);
                        break;
                    case 2:
//                        CabteRegister.gain_pga_stepdb_a3[i] = average[i];
                        hw.setGainPgaA3(i,average[i]);
                        break;
                }
            }else{

                chflag |= (idx/3 - 1) << 24;
                switch (chMode){
                    case 3://4通道
                        bCheckSig = bCheckSig && isCheckSig(average[i]);
                        ampCnt++;
                        newAmp[i] = srcAmp * 1024 /10.23/average[i];
                        hw.setGainAdFsD(i,2,idx % 3,0,newAmp[i]);
                        chflag |= 2 << 16;
                        break;
                    case 0://单通道1-5
                    case 1://单通道2-6
                        if(i % 4 < 2) {
                            int n = 0;
                            for (int j = 0; j < 4; j++) {
                                n = (i / 4) * 4 + j;
                                ampCnt++;
                                bCheckSig = bCheckSig && isCheckSig(average[n]);
                                newAmp[n] = srcAmp * 1024 / 10.23 / average[n];
                                hw.setGainAdFsD(i, 0, idx % 3, j, newAmp[n]);
                            }
                            chflag |= 0 << 16;
                        }
                        break;
                    case 2://双通道12,56
                        if(i % 4 < 2){

                            int n = 0;
                            if (i % 2 == 0) { // 1,5

                                n = (i / 4) * 4 + 1;
                                ampCnt++;
                                bCheckSig = bCheckSig && isCheckSig(average[n]);
                                newAmp[n] = srcAmp * 1024 / 10.23 / average[n];
                                hw.setGainAdFsD(i, 1, idx % 3, 0, newAmp[n]);//A

                                n = (i / 4) * 4 + 3;
                                ampCnt++;
                                bCheckSig = bCheckSig && isCheckSig(average[n]);
                                newAmp[n] = srcAmp * 1024 / 10.23 / average[n];
                                hw.setGainAdFsD(i, 1, idx % 3, 1, newAmp[n]);//B
                            } else { // 2 , 6
                                n = (i / 4) * 4 + 2;
                                ampCnt++;
                                bCheckSig = bCheckSig && isCheckSig(average[n]);
                                newAmp[n] = srcAmp * 1024 / 10.23 / average[n];
                                hw.setGainAdFsD(i, 1, idx % 3, 0, newAmp[n]);//C
                                n = (i / 4) * 4 + 0;
                                ampCnt++;
                                bCheckSig = bCheckSig && isCheckSig(average[n]);
                                newAmp[n] = srcAmp * 1024 / 10.23 / average[n];
                                hw.setGainAdFsD(i, 1, idx % 3, 1, newAmp[n]);//D
                            }
                            chflag |= 1 << 16;
                        }
                        break;
                }
            }
        }
        for(int i=0;i<channelNums;i++){
            average[i] = 0;
        }
        if(idx >= 3){
            double s = 0;
            for (double v: newAmp) {
                s += v;
            }
            cabteRegister.setNewInputV(s/ampCnt);

            cabteRegister.calcGain(chflag);
            chflag = 0;
            if(!bCheckSig){
                setErrcode(3007);
            }
        }else if(idx == 2){
            cabteRegister.calcPgaStep(chflag);
            chflag = 0;
        }

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        fpgaCommand.setADdiffGainCalib(false);
        rstCalculate();
        return true;

    }

    private void switchChannel(){
        FPGACommand fpgaCommand = FPGACommand.getInstance();
        boolean [] bChEnable = new boolean[ChannelFactory.CH_CNT];
        Arrays.fill(bChEnable,false);
        switch (chMode){
            case 3://4通道
                Arrays.fill(bChEnable,true);
                fpgaCommand.setADCChannel(bChEnable);
                break;
            case 0://单通道1-5
                bChEnable[0] = bChEnable[4] = true;
                fpgaCommand.setADCChannel(bChEnable);
                if(ch < 0){
                    Arrays.fill(ch_en,false);
                    ch_en[0] = ch_en[4] = true;
                }
                break;
            case 1://单通道2-6
                bChEnable[1] = bChEnable[5] = true;
                fpgaCommand.setADCChannel(bChEnable);
                if(ch < 0){
                    Arrays.fill(ch_en,false);
                    ch_en[1] = ch_en[5] = true;
                }
                break;
            case 2://双通道12,56
                bChEnable[0] = bChEnable[4] = true;
                bChEnable[1] = bChEnable[5] = true;
                fpgaCommand.setADCChannel(bChEnable);
                if(ch < 0){
                    Arrays.fill(ch_en,false);
                    ch_en[0] = ch_en[4] = true;
                    ch_en[1] = ch_en[5] = true;
                }
                break;
        }
    }

}
