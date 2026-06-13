package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Scope;
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

public class MHO68v1_ZeroCalibrateEx extends Calibrate {
    public MHO68v1_ZeroCalibrateEx(int calibrateType) {
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
    private int dwIdx = 0;
    private int dangwei = VerticalAxis.DANG_1mV;
    private int resistanceType = Channel.RESISTANCE_1M;

    private int errcode;
    private final String TAG = "ZeroEx";
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

    int chMode = 0;

    //校准下一档初始化
    private void rstCalculate(){
        for(int i=0; i<ChannelFactory.CH_CNT; i++) {
            best_value[i] = Float.MAX_VALUE;
            bakCoef[i] = 0;
            state[i] = 0;
            tryNum[i] = 0;
            tryChiShu[i] = 0;
            finished[i] = !channel[i].isOpen();

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
            channel[i].setVScaleId(VerticalAxis.DANG_1V);
        }
    }

    int pgaVal = 0x200;

    private void setPGAVal(int val){
        fpgaCommand.cmdDevice(300);
        fpgaCommand.setPgaVal(val);
        int [] res = {val,val,val,val,val,val,val,val};
        channelHardw.set_ch_AD8370Gain(res);
        fpgaCommand.cmdDevice(300);
    }


    @Override
    public void calibratePrepare() {
        chMode = 0;
        resistanceType = Channel.RESISTANCE_1M;
        dwIdx = HW.RATIO_DANG_1;

        pgaVal =  get10008PGA(true) ;

        dangwei = CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx);

        for(int i=0; i<channelNums; i++) {
            channel[i].setPos(0); //通道纵向位置归零
            channel[i].setVScaleId(dangwei);
            channel[i].setResistanceType(resistanceType);
            channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);
            TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
            triggerEdge.getTriggerLevel(i).setPos(-ScopeBase.getHeight()/3); //特意不让触发
            channel[i].setZero(0);
        }

        ms_sleep(200);
        pgaTs = 0;
        checkParamEx(true);
        errcode = 0;
        sMax = 0;
        resultString.add("<<<<<<<<<< ZeroCalibrate start ......");
        Log.i(TAG1, "<<<<<<<<<< ZeroCalibrate start ......");
    }

    @Override
    public boolean checkParam() {
        checkParamEx(false);
        return super.checkParam();
    }

    private long pgaTs = 0;
    public boolean checkParamEx(boolean b){
        boolean bChange = false;
        long ts = SystemClock.elapsedRealtime();
        for(int i=0;i<channelNums;i++){
            if(channel[i].getResistanceType() != resistanceType){
                Log.e(TAG,"ch:" + i + ",resistanceType:" + resistanceType + ",chResistanceType:" + channel[i].getResistanceType());
            }
        }
        Scope scope = Scope.getInstance();
        int cnt = scope.getChannelSampOnCnt();
        switch (chMode){
            case 0:
                if(cnt != 8){
                    Log.e(TAG,"chMode:" + chMode + ",cnt:" +cnt);
                }
                break;
            case 1:
                if(cnt != 4){
                    Log.e(TAG,"chMode:" + chMode + ",cnt:" +cnt);
                }
                break;
            case 2:
            case 3:
                if(cnt != 2){
                    Log.e(TAG,"chMode:" + chMode + ",cnt:" +cnt);
                }
                break;
        }

        if(((ts - pgaTs) > (60*1000)) || b){
            if(pgaTs != 0) {
                Log.e(TAG, "ts - pgaTs:" + (ts - pgaTs));
            }
            for(int i=0;i<2;i++){
                for(int j=0;j<4;j++){
                    fpgaCommand.writeAD_gain(i, 0,j, 0x800);
                }
            }
            setPGAVal(pgaVal);
            updateSync();
            for(int i=0;i<channelNums;i++){
                channel[i].setPos(0);
            }
            rstCalculate();
            pgaTs = ts;
            bChange = true;
        }
        return bChange;
    }

    int sMax = 0;

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
        updateSync();
        for(int i=0; i<channelNums; i++) {
            if(channel[i].isOpen()) {
                float volDA_perGrid = (float) cabteRegister.vol_ChannelCoef_defaultEx(i,CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx),pgaVal);

                float averageVol=average[i];
                //状态机控制流程
                switch (state[i]) {
                    case 0: {
                        int fV = 2;
                        if(Math.abs(averageVol) <  fV){
                            if (Math.abs(averageVol) < Math.abs(best_value[i])) {
                                //记录更佳值
                                best_value[i] = averageVol;
                                bakCoef[i] = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);
                                Log.d(TAG, "ch:" + i + ",averageVol:" + averageVol + ",coef:" + bakCoef[i]);

                            } else {
                                //没有更佳化，说明可能到达极限，进入下一步
                                state[i]++;
                            }
                        }
                        tryNum[i] = 0;
                        tryChiShu[i] = 0;
                        finished[i] = false;
                    }
                        break;
                    case 1:
                        if (Math.abs(averageVol) < Math.abs(best_value[i])) {
                            //找到更佳值，说明没有到达极限，回到第1步
                            best_value[i] = averageVol;
                            bakCoef[i] = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);
                            Log.d(TAG,"ch:" + i +",averageVol:" + averageVol + ",coef:" + bakCoef[i]);
                            state[i] = 0;
                            finished[i] = false;
                        } else {
                            int chi = 2;
                            if (++tryNum[i] > chi) {
                                if(!finished[i]) {
                                    tryChiShu[i]++;
                                    if(Math.abs(best_value[i]) < 1 || tryChiShu[i] > chi) {
                                        finished[i] = true;
                                    }
                                    else {
                                        tryNum[i] = 0; //再来一次
                                    }
                                }
                            }
                        }
                        break;
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
                float vv = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);
                //计算出新的零点

                if(channel[i].getResistanceType() == Channel.RESISTANCE_1M
                        && dwIdx != HW.RATIO_DANG_1){
                    zero = vv - zero;
                }else{
                    zero = vv + zero;
                }


                //DA芯片嵌位处理
                if(zero < 0) {
                    zero = 32768;
                }
                else if(zero > 65535) {
                    zero = 32768;
                }
                cabteRegister.setChannelZero(i, dwIdx, pgaVal & 0xFF, zero);


//                Log.d(TAG,"ch:" + i + ",dwIdx:" + dwIdx + ",pga:" + Integer.toHexString(pgaVal)
//                        + ",old zero:" + vv + ",zero:" + zero + ",averageVol:" +averageVol + ",volDA_perGrid:" + volDA_perGrid + ",best:" + best_value[i]);

                //调节硬件
                channel[i].setPos(0); //通道纵向位置归零
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
            for(int i=0; i<channelNums; i++) {
                if (channel[i].isOpen()) {

                    cabteRegister.setChannelZero(i,dwIdx,pgaVal &0xFF,bakCoef[i]);
                    String str1 = "档位="+pgaVal+", ch"+(i+1)
                            +"最佳: 平均值="+best_value[i]
                            +", 零点值="+bakCoef[i];
                    Log.i(TAG1, str1);
                    resultString.add(str1);
                    //校准结果判断
                    int wucha=5;

//                    if(Math.abs(best_value[i]) > wucha) {
//                        errcode = 103;
//                        str1 = "ZeroCalibrate error: ch"+(i+1)+"平均值超标，大于"+wucha +"," + best_value[i] + "," + VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV) + "V";
//                        Log.e(TAG1, str1);
//                        resultString.add(str1);
//                        ok = false;
//                    }
                }
            }
            if(ok) {
                if(DoStateMachine()) {
                    //全部校准结束
                    if(!cabteRegister.verifyChZeroCoef())
                        errcode = 104;

                    Log.d(TAG,"sMax:" + sMax);
                    return true;
                }
                else
                    return false;
            }
            else {
                return true;
            }
        }
        else
            return false;
    }


    private int get10008PGA(boolean bFirst){

        if(bFirst){
            pgaVal = 0x200;
        }else{
            pgaVal++;
        }
        return pgaVal;
    }
    private boolean DoStateMachine() {
        int val = get10008PGA(false);
        if((val & 0xFF) > 0x20){
            dwIdx++;
            if(dwIdx >= 2 && resistanceType == Channel.RESISTANCE_50) {
                switch (chMode){
                    case 0://8通道
                        ChannelFactory.chClose(ChannelFactory.CH3);
                        ChannelFactory.chClose(ChannelFactory.CH4);
                        ChannelFactory.chClose(ChannelFactory.CH7);
                        ChannelFactory.chClose(ChannelFactory.CH8);
                        ms_sleep(300);
                        dwIdx = 0;
                        resistanceType = Channel.RESISTANCE_1M;
                        chMode++;
                        break;
                    case 1://4通道
                        ChannelFactory.chClose(ChannelFactory.CH2);
                        ChannelFactory.chClose(ChannelFactory.CH6);
                        ms_sleep(300);
                        chMode++;
                        dwIdx = 0;
                        resistanceType = Channel.RESISTANCE_1M;
                        break;
                    case 2://2通道 1-5
                        ChannelFactory.chOpen(ChannelFactory.CH2);
                        ChannelFactory.chOpen(ChannelFactory.CH6);
                        ChannelFactory.chClose(ChannelFactory.CH1);
                        ChannelFactory.chClose(ChannelFactory.CH5);
                        ms_sleep(300);
                        TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
                        triggerEdge.setTriggerSource(ChannelFactory.CH2);
                        ms_sleep(300);
                        triggerEdge.setTriggerSource(ChannelFactory.CH2);
                        chMode++;
                        dwIdx = 0;
                        resistanceType = Channel.RESISTANCE_1M;
                        break;
                    case 3://2通道 2-6
                        return true;
                }
            }else if (dwIdx >= 4 && resistanceType == Channel.RESISTANCE_1M) {
                dwIdx = 0;
                resistanceType = Channel.RESISTANCE_50;
            }
            dangwei = CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx);
            for (int i = 0; i < channelNums; i++) {
                channel[i].setVScaleId(dangwei);
                channel[i].setResistanceType(resistanceType);
                channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);
            }
            val = get10008PGA(true);
        }

        pgaTs = 0;
        checkParamEx(true);
        //校准下一个档位
        Log.i(TAG1, "开始校准档位"+val+"-------------------------------");
        return false;
    }
}
