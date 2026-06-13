package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * Created by zhuzh on 2018-6-29.
 * 零点校准
 *
 */

public class MHO68v1_ADOffsetCalibrate extends Calibrate {
    public MHO68v1_ADOffsetCalibrate(int calibrateType) {
        super(calibrateType);
    }
    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    private float []best_value=new float[ChannelFactory.CH_CNT];
    //private int []maxVol=new int[4];
    private float []bakCoef=new float[ChannelFactory.CH_CNT];
    private int []state=new int[ChannelFactory.CH_CNT];

    private int tryNum[]=new int[ChannelFactory.CH_CNT];
    private int tryChiShu[]=new int[ChannelFactory.CH_CNT];
    private boolean finished[]=new boolean[ChannelFactory.CH_CNT];

    private int adOffset[] = new int[ChannelFactory.CH_CNT];

    private int dwIdx = HW.RATIO_DANG_1;
    private int dangwei = VerticalAxis.DANG_10mV;



    private int errcode;
    private final String TAG = "ADOffset";
    private final String TAG1=TAG_PRI+":"+TAG;
    ChannelHardw channelHardw = ChannelHardw.getInstance();
    FPGACommand fpgaCommand = FPGACommand.getInstance();

    @Override
    public String getTAG() {
        return TAG1;
    }

    @Override
    public int getErrcode() {
        //=1：校验错误
        return errcode;
    }

    int step = 0;
    int chMode = 0;

    int step1 = 0;

    int [] refIdx = {0,0,0,0};

    //校准下一档初始化
    private void rstCalculate(){
        for(int i=0; i<ChannelFactory.CH_CNT; i++) {
            best_value[i] = Float.MAX_VALUE;
            bakCoef[i] = 0;
            state[i] = 0;
            tryNum[i] = 0;
            tryChiShu[i] = 0;
            adOffset[i] = 0x100;
            if(channel[i].isOpen())
                finished[i] = false;
            else
                finished[i] = true;

        }
    }

    @Override
    public void iniCalibrateReg(){
        //可以不用复位零点，在目前的零点基础上进行校准
    }

    @Override
    protected void restoreCalibrateConfig() {
        super.restoreCalibrateConfig();
        for(int i=0;i<channelNums;i++){
            channel[i].setVScaleId(VerticalAxis.DANG_10mV);
        }
    }

    int pgaVal =  0x200 ;

    private void setPGAVal(int val){
        fpgaCommand.cmdDevice(300);
        int [] res = {val,val,val,val,val,val,val,val};
        channelHardw.set_ch_AD8370Gain(res);
        fpgaCommand.setPgaVal(val);
        fpgaCommand.cmdDevice(300);
    }


    @Override
    public void calibratePrepare() {
        step = 0;
        chMode = 0;
        dwIdx = HW.RATIO_DANG_2;
        pgaVal = 0x217;
        dangwei = CabteRegister.getRatioIdx2Dang(0,dwIdx);
        TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
        triggerEdge.setTriggerSource(ChannelFactory.CH1);
        ms_sleep(100);
        for(int i=0; i<channelNums; i++) {
            channel[i].setPos(0); //通道纵向位置归零
            channel[i].setVScaleId(dangwei);
            triggerEdge.getTriggerLevel(i).setPos(-ScopeBase.getHeight()/3); //特意不让触发
            channel[i].setZero(0);
        }
        ms_sleep(200);

        checkParamEx(true);
        errcode = 0;
        sMax = 0;
        resultString.add("<<<<<<<<<< ZeroCalibrate start ......");
        Log.i(TAG1, "<<<<<<<<<< ZeroCalibrate start ......");
        delaySet(5);
    }

    @Override
    public boolean checkParam() {
        checkParamEx(false);
        return super.checkParam();
    }

    private long pgaTs = 0;
    public boolean checkParamEx(boolean bAdConfig){
        long ts = SystemClock.elapsedRealtime();
        if(ts - pgaTs > 3 * 60 * 1000 || bAdConfig){
            if(pgaTs != 0) {
                Log.e(TAG, "ts - pgaTs:" + (ts - pgaTs));
            }

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for(int k=0;k<2;k++) {
                        fpgaCommand.writeAD_gain(i, j, k,0xA000);
                    }
                }
            }
            if(step == 1) {
                switch (chMode) {
                    case 0://1-5 单通道
                        for (int i = 0; i < 4; i++) {
                            cabteRegister.setAdOffset(ChannelFactory.CH1, 0, i, 0x100);
                            cabteRegister.setAdOffset(ChannelFactory.CH5, 0, i, 0x100);
                        }
                        fpgaCommand.setADCChannel(new boolean[]{true, false, false, false, true, false, false, false});
                        break;
                    case 1:// 双通道
                        for (int i = 0; i < 2; i++) {
                            cabteRegister.setAdOffset(ChannelFactory.CH1, 1, i, 0x100);
                            cabteRegister.setAdOffset(ChannelFactory.CH2, 1, i, 0x100);
                            cabteRegister.setAdOffset(ChannelFactory.CH5, 1, i, 0x100);
                            cabteRegister.setAdOffset(ChannelFactory.CH6, 1, i, 0x100);
                        }
                        fpgaCommand.setADCChannel(new boolean[]{true, true, false, false, true, true, false, false});
                        break;
                    case 2://单通道 2，6
                        for (int i = 0; i < 4; i++) {
                            cabteRegister.setAdOffset(ChannelFactory.CH2, 0, i, 0x100);
                            cabteRegister.setAdOffset(ChannelFactory.CH6, 0, i, 0x100);
                        }
                        fpgaCommand.setADCChannel(new boolean[]{false, true, false, false, false, true, false, false});
                        break;
                }
            }
            setPGAVal(pgaVal);
            for(int i=0;i<channelNums;i++){
                channel[i].setPos(0);
            }
            rstCalculate();
            pgaTs = ts;
            ms_sleep(100);
            delaySet(5);
            return true;
        }
        return false;
    }

    int sMax = 0;


    public float calculateAverage(float[] array,int offset,int len) {

        float sum = 0;
        int endIdx = offset + len;
        for(int i=offset;i<endIdx;i++ ){
            sum += array[i];
        }
        return sum / len;
    }

    public int calcRefIdx(float[] array,int offset,int len){
        int endIdx = offset + len;
        int idx = offset;
        float v = calculateAverage(array,offset,len);
        float min = Math.abs(array[offset] - v);
        float m;
        for(int i = offset + 1;i < endIdx;i++){
            m = Math.abs(array[i] - v);
            if( m < min){
                min = m;
                idx = i;
            }
        }
        return idx;
    }

    @Override
    public boolean onCalibrate() {
        //等待硬件操作完成
        if(!isFinishedAction())
            return false;

        delaySet(0);

        if(checkParamEx(false)){
            return false;
        }

        float[] average ={0,0,0,0,0,0,0,0};
        long sum;
        WaveData waveData = null;
        int N;

        HwConfig hwConfig = HwConfig.getInstance();
        //获取每个通道的波形平均值
        for(int i=0; i<channelNums; i++){
            if(channel[i].isOpen()) {
                waveData = (WaveData) getWave(i);
                if(waveData == null || (N = waveData.getWaveLength()) < 10)
                    return false;
                sum = MathNative.calcSum(waveData.getByteBuffer());

                average[i] = (float) (hwConfig.getWavFactor() * sum / N);

                if(step == 1) {
                    if (Math.abs(average[i]) > 100) {
                        Log.e(TAG, "ch:" + i + ",average:" + average[i] + ",N:" + N);
                        return false;
                    }
                }



                int max1 = MathNative.calcMax(waveData.getByteBuffer());
                int min1 = MathNative.calcMin(waveData.getByteBuffer());
                int ss = Math.abs(max1 - min1);
                if(ss > sMax){
                    sMax = ss;
                }
                if(ss * hwConfig.getWavFactor() > 300){
                    Log.i(TAG1, "ch:" + i + ",max="+max1+", min="+min1);
                    Log.i(TAG1, "ch"+(i+1)+"有外接信号输入，校准结束！");

//                    errcode = 105;
//                    String str1 = "ZeroCalibrate error: ch"+(i+1)+"可能通道探头没有拔出，或者从外部输入了信号";
//                    Log.e(TAG1, str1);
//                    resultString.add(str1);
//                    return true;
                }

            }
        }
        if(step == 1){
            switch (chMode){
                case 2: // 2-6 单通道
                case 0: // 1-5 单通道
                {
                    if(step1 == 0){
                        for (int i = 0; i < 2; i++) {
                            refIdx[i] = calcRefIdx(average,i * 4, 4);
                        }
                        step1 = 1;
                    }else {
                        int idx = chMode == 2 ? 1 : 0;
                        int[] ch2ad = {3, 0, 2, 1};
                        int adcIdx;
                        for (int i = 0; i < channelNums; i++) {
                            adcIdx = ch2ad[i%4];
                            float v = Math.abs(average[i] - average[refIdx[i / 4]]);
                            int vv = cabteRegister.getADOffset((i / 4) * 4 + idx,0,adcIdx);
                            Log.d(TAG,"chMode:" + chMode + ",ch: " + i  + ",v:" + v + ",offset:" + vv + "," + ((i / 4) * 4 + idx));

                            if (v > 0.5) {
                                tryNum[i] = 0;
                                if (average[i] < average[refIdx[i / 4]]) {
                                    vv++;
                                } else {
                                    vv--;
                                }
                                if(vv < 0){
                                    vv = 0;
                                }else if(vv > 0x1FF){
                                    vv = 0x1FF;
                                }
                                //fpgaCommand.writeAD_offset(i / 4, adcIdx, vv);
                                cabteRegister.setAdOffset((i/4) * 4 + idx,0,adcIdx,vv);

                            }else{
                                if(best_value[i] > v){
                                    best_value[i] = v;
                                    adOffset[i] = vv;
                                }
                                tryNum[i]++;
                                if(tryNum[i] > 3){
                                    finished[i] = true;
                                }
                            }
                        }
                        boolean bR = true;
                        for(boolean r:finished){
                            if (!r) {
                                bR = false;
                                break;
                            }
                        }
                        if(bR){
                            for(int i=0;i<channelNums;i++){
                                int vv = adOffset[i];
                                Log.d(TAG,"i:" + i + ",vv:" + vv);
                                cabteRegister.setAdOffset((i/4) * 4 + idx,0,ch2ad[i%4],vv);
                            }
                        }
                        fpgaSync();
                    }
                }
                break;
                case 1: // 12 - 56 //双通道
                {
                    int []ar={0,1,0,1,2,3,2,3};
                    if(step1 == 0){
                        refIdx[0] = calcRefIdx(new float[]{average[2],average[0]},0,2); //c d
                        refIdx[0] = refIdx[0] == 0 ? 2 : 0;
                        refIdx[1] = calcRefIdx(new float[]{average[1],average[3]},0,2); //a b
                        refIdx[1] = refIdx[1] == 0 ? 1 : 3;
                        refIdx[2] = calcRefIdx(new float[]{average[6],average[4]},0,2);
                        refIdx[2] = refIdx[2] == 0 ? 6 : 4;
                        refIdx[3] = calcRefIdx(new float[]{average[5],average[7]},0,2);
                        refIdx[3] = refIdx[3] == 0 ? 5 : 7;

                        step1 = 1;
                    }else {
                        int[] ch2ad = {3, 0, 2, 1};
                        int[] ar1 = {1,0,0,1};

                        for (int i = 0; i < channelNums; i++) {
                            float v = Math.abs(average[i] - average[refIdx[ar[i]]]);

                            int vv = cabteRegister.getADOffset((i/4) * 4 + ((i + 1) % 2),1,ar1[i%4]);

                            Log.d(TAG,"chMode:" + chMode + ",ch: " + i  + ",v:" + v + ",offset:" + vv + "," + ((i/4) * 4 + ((i + 1) % 2)));


                            if (v > 0.5) {
                                tryNum[i] = 0;
                                if (average[i] < average[refIdx[ar[i]]]) {
                                    vv++;
                                } else {
                                    vv--;
                                }
                                int rIdx = -1;
                                if(vv < 0){
                                    rIdx = refIdx[ar[i]];
                                    vv = 0;
                                }else if(vv > 0x1FF){
                                    rIdx = refIdx[ar[i]];
                                    vv = 0x1FF;
                                }

                                if(rIdx >= 0){
                                    int rvv = cabteRegister.getADOffset((rIdx/4) * 4 + ((rIdx + 1) % 2),1,ar1[rIdx%4]);
                                    if(vv == 0){
                                        rvv++;
                                    }else{
                                        rvv--;
                                    }
                                    //fpgaCommand.writeAD_offset(rIdx / 4, ch2ad[rIdx%4], rvv);
                                    cabteRegister.setAdOffset((rIdx/4) * 4 + ((rIdx + 1) % 2),1,ar1[rIdx%4],rvv);
                                }

                                //fpgaCommand.writeAD_offset(i / 4, ch2ad[i%4], vv);
                                cabteRegister.setAdOffset((i/4) * 4 + ((i + 1) % 2),1,ar1[i%4],vv);
                            }else{
                                if(best_value[i] > v){
                                    best_value[i] = v;
                                    adOffset[i] = vv;
                                }
                                tryNum[i]++;
                                if(tryNum[i] > 3){
                                    finished[i] = true;
                                }
                            }
                        }
                        boolean bR = true;
                        for(boolean r:finished){
                            if (!r) {
                                bR = false;
                                break;
                            }
                        }
                        if(bR){
                            for(int i=0;i<channelNums;i++){
                                int vv = adOffset[i];
                                cabteRegister.setAdOffset((i/4) * 4 + ((i + 1) % 2),1,ar1[i%4],vv);
                            }
                        }
                        fpgaSync();
                    }
                }
                break;
            }
        }else {
            updateSync();
            for (int i = 0; i < channelNums; i++) {
                if (channel[i].isOpen()) {
                    float volDA_perGrid = (float) cabteRegister.vol_ChannelCoef_defaultEx(i, CabteRegister.getRatioIdx2Dang(channel[i].getResistanceType(),dwIdx), pgaVal);

                    float averageVol = average[i];
                    //状态机控制流程
                    switch (state[i]) {
                        case 0: {
                            int fV = 2;

                            if (Math.abs(averageVol) < Math.abs(best_value[i])) {
                                //记录更佳值
                                best_value[i] = averageVol;
                                bakCoef[i] = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);
                                Log.d(TAG, "ch:" + i + ",averageVol:" + averageVol + ",coef:" + bakCoef[i]);

                            } else if (Math.abs(averageVol) > fV) {
                                //离结果差太多，继续


                            } else {
                                //没有更佳化，说明可能到达极限，进入下一步
                                state[i]++;
                                tryNum[i] = 0;
                            }
                        }
                        break;
                        case 1:
                            if (Math.abs(averageVol) < Math.abs(best_value[i])) {
                                //找到更佳值，说明没有到达极限，回到第1步
                                best_value[i] = averageVol;
                                bakCoef[i] = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);
                                Log.d(TAG, "ch:" + i + ",averageVol:" + averageVol + ",coef:" + bakCoef[i]);
                                state[i] = 0;
                                finished[i] = false;

                            } else {
                                int chi = 4;

                                if (++tryNum[i] > chi) {
                                    if (!finished[i]) {
                                        tryChiShu[i]++;
                                        if (Math.abs(best_value[i]) < 1 || tryChiShu[i] > 1) {
                                            finished[i] = true;

                                        } else {
                                            tryNum[i] = 0; //再来一次

                                        }
                                    }
                                }
                            }
                    }


                    float fx1 = Math.abs(averageVol);//误差多少像素
                    float zero = averageVol*volDA_perGrid; //误差的DA值
                    if(zero > 300){
                        zero = 300;
                    }

                    if(fx1 < 2){ //小于2个像素，则每次调节到四分之一
                        zero /= 4;
                    }else if(fx1 < 5) //小于5个像素，则每次调节二分之一
                        zero /= 2;

                    //当调节值小于0.2像素时，每次固定调节0.1像素
                    if(Math.abs(zero) < Math.abs(volDA_perGrid/5))
                    {
                        zero = Math.abs(volDA_perGrid/10);
                        if(fx1 < 0) zero = -zero;
                        //这里zero值可能小于1，但是不用担心。虽然DA的精度为1，但是零点还是采用浮点数。
                        //在小档位时，可能需要移动好几个像素DA值才会修改1。
                        //处理方法是，FPGA增加偏移量补偿寄存器
                    }
                    float vv = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);
                    //计算出新的零点

                    if(channel[i].getResistanceType() == Channel.RESISTANCE_1M
                        && dwIdx != HW.RATIO_DANG_1){
                        zero = vv - zero;
                    }else{
                        zero = vv + zero;
                    }


                    //DA芯片嵌位处理
                    if (zero < 0) {
                        zero = 32768;
                    } else if (zero > 65535) {
                        zero = 32768;
                    }
                    cabteRegister.setChannelZero(i, dwIdx, pgaVal & 0xFF, zero);


                    Log.d(TAG, "ch:" + i + ",dwIdx:" + dwIdx + ",pga:" + Integer.toHexString(pgaVal)
                            + ",old zero:" + vv + ",zero:" + zero + ",averageVol:" + averageVol + ",volDA_perGrid:" + volDA_perGrid + ",best:" + best_value[i]);

                    //调节硬件
                    channel[i].setPos(0); //通道纵向位置归零
                }
            }
        }

        boolean bFinished = true;
        for(int i=0;i<channelNums;i++){
            if(!finished[i]){
                bFinished = false;
                break;
            }
        }

        if(bFinished){
            //本档位校准完成
            boolean ok=true;
            if(step == 0) {
                for (int i = 0; i < channelNums; i++) {
                    if (channel[i].isOpen()) {

                        cabteRegister.setChannelZero(i, dwIdx, pgaVal & 0xFF, bakCoef[i]);
                        String str1 = "档位=" + pgaVal + ", ch" + (i + 1)
                                + "最佳: 平均值=" + best_value[i]
                                + ", 零点值=" + bakCoef[i];
                        Log.i(TAG1, str1);
                        resultString.add(str1);
                        //校准结果判断
                        int wucha = 5;

                        if (Math.abs(best_value[i]) > wucha) {
                            errcode = 103;
                            str1 = "ZeroCalibrate error: ch" + (i + 1) + "平均值超标，大于" + wucha + "," + best_value[i] + "," + VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV) + "V";
                            Log.e(TAG1, str1);
                            resultString.add(str1);
                            ok = false;
                        }
                    }
                }
            }
            if(ok) {
                if(DoStateMachine()) {
                    //全部校准结束
                    if (!cabteRegister.verifyChZeroCoef())
                        errcode = 104;

                    Log.d(TAG, "sMax:" + sMax);
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }



    private boolean DoStateMachine() {

        if(step == 0) {
            switch (chMode) {
                case 0://8通道
                    ChannelFactory.chClose(ChannelFactory.CH3);
                    ChannelFactory.chClose(ChannelFactory.CH4);
                    ChannelFactory.chClose(ChannelFactory.CH7);
                    ChannelFactory.chClose(ChannelFactory.CH8);
                    ms_sleep(200);
                    chMode++;
                    break;
                case 1://4通道
                    ChannelFactory.chClose(ChannelFactory.CH2);
                    ChannelFactory.chClose(ChannelFactory.CH6);
                    ms_sleep(200);
                    chMode++;
                    break;
                case 2://2通道 1-5
                {
                    ChannelFactory.chOpen(ChannelFactory.CH2);
                    ChannelFactory.chOpen(ChannelFactory.CH6);
                    ChannelFactory.chClose(ChannelFactory.CH1);
                    ChannelFactory.chClose(ChannelFactory.CH5);
                    ms_sleep(200);
                    TriggerEdge triggerEdge = (TriggerEdge) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
                    triggerEdge.setTriggerSource(ChannelFactory.CH2);
                    ms_sleep(200);
                    triggerEdge.setTriggerSource(ChannelFactory.CH2);
                    chMode++;
                }
                    break;
                case 3://2通道 2-6
                {
                    step = 1;
                    chMode = 0;
                    step1 = 0;
                    rstCalculate();
                    for (int i = ChannelFactory.CH1; i < ChannelFactory.getMaxChIdx(); i++) {
                        ChannelFactory.chOpen(i);
                    }
                    ms_sleep(200);
                    TriggerEdge triggerEdge = (TriggerEdge) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
                    triggerEdge.setTriggerSource(ChannelFactory.CH1);
                    ms_sleep(200);
                    fpgaCommand.setADCChannel(new boolean[]{true, false, false, false, true, false, false, false});
                }
                break;
            }

            dangwei = CabteRegister.getRatioIdx2Dang(0,dwIdx);
            for (int i = 0; i < channelNums; i++) {
                channel[i].setVScaleId(dangwei);
            }
            ms_sleep(100);
            //校准下一个档位
            Log.i(TAG1, "开始校准档位"+pgaVal+"-------------------------------");

        }else{
            switch (chMode){
                case 0:
                    chMode++;
                    step1 = 0;
                    fpgaCommand.setADCChannel(new boolean[]{true,true,false,false,true,true,false,false});
                    break;
                case 1:
                {
                    chMode++;
                    step1 = 0;
                    fpgaCommand.setADCChannel(new boolean[]{false,true,false,false,false,true,false,false});
                    TriggerEdge triggerEdge = (TriggerEdge) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
                    triggerEdge.setTriggerSource(ChannelFactory.CH2);
                }
                break;
                case 2:
                    return true;
            }
            delaySet(4);
        }
        checkParamEx(true);
        pgaTs = 0;
        return false;
    }
}
