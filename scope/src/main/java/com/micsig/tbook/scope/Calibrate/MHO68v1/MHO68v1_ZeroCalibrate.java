package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
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

public class MHO68v1_ZeroCalibrate extends Calibrate {
    public MHO68v1_ZeroCalibrate(int calibrateType) {
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

    private double vScaleVal = 0;


    int dwIdx = 0;

    private int errcode;
    private final String TAG = "Zero";
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

    //校准下一档初始化
    private void rstCalculate(){
        for(int i=0; i<channelNums; i++) {
            best_value[i] = Float.MAX_VALUE;
            bakCoef[i] = 0;
            state[i] = 0;
            tryNum[i] = 0;
            tryChiShu[i] = 0;
            finished[i] = !channel[i].isOpen();

        }
    }

    int ch = -1;
    int resistanceType = Channel.RESISTANCE_1M;
    int chMode = 2;

    @Override
    public void setParam(Object vol) {
        ch = -1;
        if(vol instanceof double[]) {
            double[] param=(double[])vol;
            if(param.length == 4){
                int ix = (int)(param[0] + 0.1);
                if(ChannelFactory.isDynamicCh(ix)) {
                    ch = ix;
                    vScaleVal = param[1];
                    resistanceType = (int)(param[2] + 0.1);
                    chMode = (int)(param[3] + 0.1);
                }
            }
        }
    }

    @Override
    public void iniCalibrateReg(){
        //可以不用复位零点，在目前的零点基础上进行校准

    }



    int pgaVal = 4;

    private void setPGAVal(int val){
        fpgaCommand.cmdDevice(300);
        int [] res = {val,val,val,val,val,val,val,val};

        channelHardw.set_ch_AD8370Gain(res);
        fpgaCommand.setPgaVal(val);
        fpgaCommand.cmdDevice(300);
    }


    @Override
    public void calibratePrepare() {

        if(!ChannelFactory.isDynamicCh(ch)){
            errcode = 101;
            return;
        }
        dwIdx = CabteRegister.getRatioIdx(resistanceType,vScaleVal);
        TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
        triggerEdge.setTriggerSource(ch);
        ms_sleep(200);
        ChannelFactory.forEachCh((channel)->{
            if(channel.getChId() != ch) {
                ChannelFactory.chClose(channel.getChId());
            }
        });

        channel[ch].setPos(0); //通道纵向位置归零
        channel[ch].setResistanceType(resistanceType);
        channel[ch].setVScaleId(CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx));

        switch (chMode){
            case 0:
                break;
            case 1:
                ChannelFactory.chOpen(ChannelFactory.CH1);
                ChannelFactory.chOpen(ChannelFactory.CH2);
                break;
            case 2:
                ChannelFactory.chOpen(ChannelFactory.CH4);
                break;
        }


        ms_sleep(200);
        int []result={0,0,0,0,0};
        cabteRegister.calc_pga_fs_gain(ch,vScaleVal,result);
        pgaVal = result[0];
        pgaTs = 0;
        checkParam();
        errcode = 0;
        sMax = 0;
        resultString.add("<<<<<<<<<< ZeroCalibrate start ......");
        Log.i(TAG1, "<<<<<<<<<< ZeroCalibrate start ......");
    }
    private long pgaTs = 0;
    public boolean checkParam(){
        long ts = SystemClock.elapsedRealtime();
        if(ts - pgaTs > 60*1000){
            if(pgaTs != 0) {
                Log.e(TAG, "ts - pgaTs:" + (ts - pgaTs));
            }

            for(int i=0;i<2;i++){
                for(int j=0;j<4;j++){
                    fpgaCommand.writeAD_gain(i,0, j, 0x800);
                }
            }

            setPGAVal(pgaVal);
            updateSync();
            for(int i=0;i<channelNums;i++){
                channel[i].setPos(0);
            }
            rstCalculate();
            pgaTs = ts;
            return true;
        }
        return false;
    }

    int sMax = 0;

    @Override
    public boolean onCalibrate() {
        //等待硬件操作完成
        if(!isFinishedAction())
            return false;
        delaySet(0);
        if(!(ChannelFactory.isDynamicCh(ch) && ch <  channelNums)){
            Log.e(TAG1,"ch:" +ch);
            errcode = 101;
            return true;
        }
        checkParam();

        float[] average ={0,0,0,0,0,0,0,0};
        long sum;
        WaveData waveData = null;
        int N;
        HwConfig hwConfig = HwConfig.getInstance();
        //获取每个通道的波形平均值
        for(int i=0;i<channelNums;i++) {
            if (channel[i].isOpen()) {
                waveData = (WaveData) getWave(i);
                if (waveData == null || (N = waveData.getWaveLength()) < 10)
                    return false;
                sum = MathNative.calcSum(waveData.getByteBuffer());
                average[i] = (float) hwConfig.getWavFactor() * sum / N;
                if(ch == i){
                    int max1 = MathNative.calcMax(waveData.getByteBuffer());
                    int min1 = MathNative.calcMin(waveData.getByteBuffer());
                    int ss = Math.abs(max1 - min1);
                    if(ss > sMax){
                        sMax = ss;
                    }
                    if(ss > 300){
                        Log.i(TAG1, "ch:" + i + ",max="+max1+", min="+min1);
                        Log.i(TAG1, "ch"+(i+1)+"有外接信号输入，校准结束！");
//                        errcode = 105;
//                        String str1 = "ZeroCalibrate error: ch"+(i+1)+"可能通道探头没有拔出，或者从外部输入了信号";
//                        Log.e(TAG1, str1);
//                        resultString.add(str1);
//                        return true;
                    }
                }
            }
        }



        {
            updateSync();
            if(channel[ch].isOpen()) {
                float averageVol=average[ch];
                //状态机控制流程
                switch (state[ch]) {
                    case 0:
                        int fV = 2;
                        if(Math.abs(averageVol) <  fV){
                            if (Math.abs(averageVol) < Math.abs(best_value[ch])) {
                                //记录更佳值
                                best_value[ch] = averageVol;
                                bakCoef[ch] = cabteRegister.getChannelZero(ch, dwIdx, pgaVal & 0xFF);
                                Log.d(TAG, "ch:" + ch + ",averageVol:" + averageVol + ",coef:" + bakCoef[ch]);

                            } else {
                                //没有更佳化，说明可能到达极限，进入下一步
                                state[ch]++;
                            }
                        }
                        tryNum[ch] = 0;
                        tryChiShu[ch] = 0;
                        finished[ch] = false;
                        break;
                    case 1:
                        if (Math.abs(averageVol) < Math.abs(best_value[ch])) {
                            //找到更佳值，说明没有到达极限，回到第1步
                            best_value[ch] = averageVol;
                            bakCoef[ch] = cabteRegister.getChannelZero(ch,dwIdx,pgaVal & 0xFF);
                            Log.d(TAG,"ch:" + ch +",averageVol:" + averageVol + ",coef:" + bakCoef[ch]);
                            state[ch] = 0;
                            finished[ch] = false;

                        } else {
                            int chi = 10;

                            if (++tryNum[ch] > chi) {
                                if(!finished[ch]) {
                                    tryChiShu[ch]++;
                                    if(Math.abs(best_value[ch]) < 0.5 || tryChiShu[ch] > chi) {
                                        finished[ch] = true;
                                    }
                                    else {
                                        tryNum[ch] = 0; //再来一次
                                    }
                                }
                            }
                        }
                }
                //调整零点
                //通过数据均值计算波形距离理论零点像素数，从而改变步进细度提高校准精度
                float volDA_perGrid = (float) cabteRegister.vol_ChannelCoef_defaultEx(ch,CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx),pgaVal);
                float fx1 = Math.abs(averageVol);//误差多少像素
                float zero = averageVol*volDA_perGrid; //误差的DA值


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
                float vv = cabteRegister.getChannelZero(ch,dwIdx,pgaVal & 0xFF);
                //计算出新的零点

                if(channel[ch].getResistanceType() == Channel.RESISTANCE_1M
                        && dwIdx != HW_MHO68V1.RATIO_DANG_1){
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

                cabteRegister.setChannelZero(ch,dwIdx,pgaVal &0xFF,zero);
                //调节硬件
                channel[ch].setPos(0); //通道纵向位置归零
            }
        }

        boolean bFinished = true;

        if(!finished[ch]){
            bFinished = false;
        }


        if(bFinished){
            //本档位校准完成
            boolean ok=true;

            {
                if (channel[ch].isOpen()) {

                    cabteRegister.setChannelZero(ch,dwIdx,pgaVal &0xFF,bakCoef[ch]);
                    String str1 = "档位="+pgaVal+", ch"+(ch+1)
                            +"最佳: 平均值="+best_value[ch]
                            +", 零点值="+bakCoef[ch];
                    Log.i(TAG1, str1);
                    resultString.add(str1);
                    //校准结果判断
                    int wucha=2;

                    if(Math.abs(best_value[ch]) > wucha) {
                        errcode = 103;
                        str1 = "ZeroCalibrate error: ch"+(ch+1)+"平均值超标，大于"+wucha +"," + best_value[ch] + "," + VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV) + "V";
                        Log.e(TAG1, str1);
                        resultString.add(str1);
                        ok = false;
                    }
                }
            }
            if(ok) {
                //全部校准结束
                if(!cabteRegister.verifyChZeroCoef())
                    errcode = 104;
                Log.d(TAG,"sMax:" + sMax);
                return true;
            }
        }
        else {
            return false;
        }
        return false;
    }


}
