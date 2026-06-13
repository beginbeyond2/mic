package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
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

public class MHO68v1_ChGainCalibrateEx extends Calibrate {
    public MHO68v1_ChGainCalibrateEx(int calibrateType) {
        super(calibrateType);
        delaySet(2);
    }

    private int errcode;
    private final String TAG = "ChGainEx";
    private final String TAG1=TAG_PRI+":"+TAG;
    private volatile int ch=0;
    private volatile int chIdx = 0;


    private volatile int step;
    private int meatCnt;
    private volatile double stdAmp;
    private volatile double vScaleVal;
    private volatile int resistanceType = Channel.RESISTANCE_1M;

    private volatile int dwIdx = VerticalAxis.DANG_NONE;

    private volatile int chMode = 0;

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
            finished[i] = false;
        }
        meatCnt = 0;
    }

    @Override
    public void iniCalibrateReg(){
        channelHardw = ChannelHardw.getInstance();
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

                    resistanceType = (int)(param[2] + 0.1);

                    stdAmp = param[3];

                    if(resistanceType == 50) resistanceType = Channel.RESISTANCE_50;
                    else if(resistanceType == 1000000) resistanceType = Channel.RESISTANCE_1M;


                    dwIdx = VerticalAxis.getScaleIdByValue(vScaleVal);
                    Logger.d(TAG,"ch:" + ch
                            + ",vScaleVal:" + vScaleVal
                            + ",srcAmp:" + stdAmp
                            + ",resistanceType:" + resistanceType
                    );
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
        param[1] = resistanceType * VerticalAxis.DANG_CNT + dwIdx;
        return param;
    }


    @Override
    public void calibratePrepare() {

        for(int i=0; i<finished.length; i++) {
            finished[i] = false;
            ch_en[i] = ch < 0 || ch == i;
        }
        chIdx = Math.max(ch,0);
        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_100uS);
        FPGACommand.getInstance().setADdiffGainCalib(true);
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        triggerEdge.setTriggerCouple(TriggerEdge.COUPLING_NOISERS);
        triggerEdge.setTriggerSource(chIdx);

        Logger.d(TAG,"resistanceType:" + resistanceType + ",vScaleVal:" + vScaleVal);

        ms_sleep(200);

        boolean bEnable_50O_700mV = (resistanceType == Channel.RESISTANCE_50) && (dwIdx > VerticalAxis.DANG_500mV);
        for(int i=0; i<channelNums; i++) {
            channel[i].set50O_700mV(bEnable_50O_700mV);
            channel[i].setProbeRate(1);
            channel[i].setResistanceType(resistanceType);
            channel[i].setVScaleId(dwIdx);
            channel[i].setPos((int) -Math.round(stdAmp / (2 * channel[i].getADVerticalPerPix())));
        }

        chMode = 3;
        step = 0;
        int levelPos = -channel[chIdx].getYPos();
        triggerEdge.getTriggerLevel().setPos(levelPos);

        for(int i=0;i<3;i++){
            ms_sleep(1000);
            checkParam();
        }

        switchChannel();
        rstCalculate();
        fpgaSync();
        delaySet(2);
        errcode = 0;
        resultString.add("chGainCalibrate start");
        Log.i(TAG1,"chGainCalibrate start");

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
                        triSrc = chIdx + 1;
                        break;
                    case 1:
                    case 5:
                        triSrc = chIdx - 1;
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
            switchChannel();
            rstCalculate();
            fpgaSync();
        }
        return bChange;
    }


    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }


    @Override
    public boolean onCalibrate() {
        //等待硬件操作完成

        if (!isFinishedAction())
            return false;

        delaySet(0);

        if(checkParam()){
            return false;
        }
        double sum1,sum2;
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
            average[i] += Math.abs(sum2/len1 - sum1/len1);
        }

        if(++meatCnt < 5) {
            return false;
        }

        for(int i=0; i<channelNums; i++){
            average[i] = average[i] / meatCnt;
            average[i] *= hwConfig.getWavFactor() * channel[i].getYFactor();
        }

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        boolean bOK = true;
        int [] ch2ad = {3,0,2,1};
        for(int i=0; i<channelNums; i++) {
            //状态机控制流程
            if((!ch_en[i])){
                continue;
            }
            double thresholdV = channel[i].getYFactor() * 0.7;
            switch (chMode){
                case 3://4通道
                {
                    int m = cabteRegister.getChGain(i, resistanceType, dwIdx, 2, 0);
                    double kk = stdAmp / channel[i].getADVerticalPerPix();
                    double k = Math.abs(average[i] - kk);
                    Log.d(TAG,"ch:" + i + ",average:" + average[i] + ",kk:" + kk + ",m:" + m);
                    if (k < thresholdV) {
                        if(k < best_value[i]) {
                            best_value[i] = average[i];
                            bakCoef[i] = (short) m;
                        }
                        tryNum[i]++;
                        if(tryNum[i] > 3) {
                            finished[i] = true;
                        }
                    }else{
                        tryNum[i] = 0;
                        k /= channel[i].getYFactor();
                        int s = (int)((k < 1 ? 1 : k < 2 ? Math.round(k*3) : Math.round(k*5)) + 0.1);
                        if(average[i] > kk){
                            m += s;
                        }else{
                            m -= s;
                        }
                        m &= 0xFFF;
                        cabteRegister.setChGain(i, resistanceType, dwIdx, 2, 0, m);
                        fpgaCommand.writeAD_gain(i/4,0,ch2ad[i%4],m);
                    }
                    bOK = bOK && finished[i];
                }
                    break;
                case 0://单通道1-5
                case 1://单通道2-6
                    if(i % 4 < 2) {
                        int n = 0;
                        for (int j = 0; j < 4; j++) {
                            n = (i / 4) * 4 + j;
                            int m = cabteRegister.getChGain(i, resistanceType, dwIdx, 0, j);
                            double kk = stdAmp / channel[i].getADVerticalPerPix();
                            double k = Math.abs(average[n] - kk);
                            Log.d(TAG,"ch:" + i + ",n:" + n + ",average:" + average[n] + ",kk:" + kk + ",m:" + m);
                            if (k < thresholdV) {
                                if(k < best_value[n]) {
                                    best_value[n] = average[n];
                                    bakCoef[n] = (short) m;
                                }
                                tryNum[n]++;
                                if(tryNum[n] > 3) {
                                    finished[n] = true;
                                }
                            }else{

                                tryNum[n] = 0;
                                k /= channel[i].getYFactor();
                                int s = (int)((k < 1 ? 1 : k < 2 ? Math.round(k*3) : Math.round(k*5)) + 0.1);
                                if(average[n] > kk){
                                    m+=s;
                                }else{
                                    m-=s;
                                }
                                m &= 0xFFF;
                                cabteRegister.setChGain(i, resistanceType, dwIdx, 0, j, m);
                                fpgaCommand.writeAD_gain(i/4,0,ch2ad[j],m);
                            }
                            bOK = bOK && finished[n];
                        }
                    }
                    break;
                case 2://双通道12,56
                    if(i % 4 < 2){
                        int [] nn = {0,0};
                        int tmpidx = 0;
                        if (i % 2 == 0) { // 1,5
                            nn[0] = (i / 4) * 4 + 1;
                            nn[1] = (i / 4) * 4 + 3;
                        } else { // 2 , 6
                            nn[0] = (i / 4) * 4 + 2;
                            nn[1] = (i / 4) * 4 + 0;
                            tmpidx = 2;
                        }
                        double kk = stdAmp / channel[i].getADVerticalPerPix();

                        for(int j=0;j<2;j++){
                            int n = nn[j];
                            int m = cabteRegister.getChGain(i, resistanceType, dwIdx, 1, j);
                            double k = Math.abs(average[n] - kk);
                            Log.d(TAG,"ch:" + i + ",n:" + n + ",average:" + average[n] + ",kk:" + kk + ",m:" + m);
                            if (k < thresholdV) {
                                if(k < best_value[n]) {
                                    best_value[n] = average[n];
                                    bakCoef[n] = (short) m;
                                }
                                tryNum[n]++;
                                if(tryNum[n] > 3) {
                                    finished[n] = true;
                                }
                            }else{
                                tryNum[n] = 0;
                                k /= channel[i].getYFactor();
                                int s = (int)((k < 1 ? 1 : k < 2 ? Math.round(k*3) : Math.round(k*5)) + 0.1);
                                if(average[n] > kk){
                                    m+=s;
                                }else{
                                    m-=s;
                                }
                                m &= 0xFFF;
                                cabteRegister.setChGain(i, resistanceType, dwIdx, 1, j, m);
                                fpgaCommand.writeAD_gain(i/4,0,tmpidx + j,m);
                            }
                            bOK = bOK && finished[n];
                        }
                    }
                    break;
            }
        }
        if(bOK){

            for(int i=0;i<channelNums;i++) {
                if (!ch_en[i]) {
                    continue;
                }
                switch (chMode) {
                    case 3://4通道
                        cabteRegister.setChGain(i, resistanceType, dwIdx, 2, 0, bakCoef[i]);
                        break;
                    case 0://单通道1-5
                    case 1://单通道2-6
                        if (i % 4 < 2) {
                            int n = 0;
                            for (int j = 0; j < 4; j++) {
                                n = (i / 4) * 4 + j;
                                cabteRegister.setChGain(i, resistanceType, dwIdx, 0, j, bakCoef[n]);
                            }
                        }
                        break;
                    case 2://双通道12,56
                        if (i % 4 < 2) {
                            int[] nn = {0, 0};
                            if (i % 2 == 0) { // 1,5
                                nn[0] = (i / 4) * 4 + 1;
                                nn[1] = (i / 4) * 4 + 3;
                            } else { // 2 , 6
                                nn[0] = (i / 4) * 4 + 2;
                                nn[1] = (i / 4) * 4 + 0;
                            }
                            for (int j = 0; j < 2; j++) {
                                cabteRegister.setChGain(i, resistanceType, dwIdx, 1, j, bakCoef[nn[j]]);
                            }
                        }
                        break;
                }
            }

            bOK = false;
            switch (chMode) {
                case 3://4通道
                    if(chIdx % 4  < 2){
                        chMode = 2;
                    }else{
                        bOK = true;
                    }
                    break;
                case 0://单通道1-5
                case 1://单通道2-6
                    bOK = true;
                    break;
                case 2://双通道12,56
                    if(chIdx % 2 == 0){
                        chMode = 0;
                    }else{
                        chMode = 1;
                    }
                    break;
            }
            if(!bOK){
                switchChannel();
                rstCalculate();
                fpgaSync();
            }
        }else{
            meatCnt = 0;
            for(int i=0;i<channelNums;i++){
                average[i] = 0;
            }
            fpgaSync();
            return false;
        }
        if(!bOK){
            return false;
        }
        fpgaCommand.setADdiffGainCalib(false);
        rstCalculate();
        cabteRegister.setVer(HW.CODE_VER);
        return true;

    }

    private void switchChannel(){
        int triSrc = ch;
        if(chMode == 2){
            switch (ch){
                case 0:
                case 4:
                    triSrc = chIdx + 1;
                    break;
                case 1:
                case 5:
                    triSrc = chIdx - 1;
                    break;
            }
        }
        TriggerFactory triggerFactory = TriggerFactory.getInstance();

        TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        if (!triggerEdge.isTriggerSource(triSrc)) {
            updateSync();
            triggerEdge.setTriggerSource(triSrc);
        }

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        boolean [] bChEnable = new boolean[ChannelFactory.CH_CNT];
        Arrays.fill(bChEnable,false);
        int [] ch2ad = {3,0,2,1};
        switch (chMode){
            case 3://4通道
                Arrays.fill(bChEnable,true);
                fpgaCommand.setADCChannel(bChEnable);
                for(int i=0;i<ch_en.length;i++) {
                    if(ch_en[i]) {
                        cabteRegister.setChGain(i, resistanceType, dwIdx, 2, 0, 0x800);
                        fpgaCommand.writeAD_gain(i/4,0,ch2ad[i%4],0x800);
                    }
                }
                break;
            case 0://单通道1-5
                bChEnable[0] = bChEnable[4] = true;
                fpgaCommand.setADCChannel(bChEnable);
                if(ch < 0){
                    Arrays.fill(ch_en,false);
                    ch_en[0] = ch_en[4] = true;
                }
                for(int i=0;i<ch_en.length;i++) {
                    if (ch_en[i]) {
                        for(int j=0;j<4;j++){
                            cabteRegister.setChGain(i, resistanceType, dwIdx, 0, j, 0x800);
                            fpgaCommand.writeAD_gain(i/4,0,ch2ad[j],0x800);
                        }
                    }
                }

                break;
            case 1://单通道2-6
                bChEnable[1] = bChEnable[5] = true;
                fpgaCommand.setADCChannel(bChEnable);
                if(ch < 0){
                    Arrays.fill(ch_en,false);
                    ch_en[1] = ch_en[5] = true;
                }
                for(int i=0;i<ch_en.length;i++) {
                    if (ch_en[i]) {
                        for(int j=0;j<4;j++){
                            cabteRegister.setChGain(i, resistanceType, dwIdx, 0, j, 0x800);
                            fpgaCommand.writeAD_gain(i/4,0,ch2ad[j],0x800);
                        }
                    }
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
                for(int i=0;i<ch_en.length;i++) {
                    if (ch_en[i]) {
                        int tmpidx = (i % 2 == 0) ? 0 : 2;
                        for(int j=0;j<2;j++) {
                            cabteRegister.setChGain(i, resistanceType, dwIdx, 1, j, 0x800);
                            fpgaCommand.writeAD_gain(i / 4, 0,tmpidx + j, 0x800);
                        }
                    }
                }
                break;
        }
    }

}
