package com.micsig.tbook.scope.Calibrate.MHO68v1;

import android.util.Log;

import com.micsig.base.DoubleUtil;
import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HW_MHO68V1 extends HW {
    private static final String TAG = "HW_MHO68V1";

    public HW_MHO68V1(int fpgaNums){
        super(fpgaNums);
        addItemAll(Arrays.asList("零点校准","精细增益校准(需要信号)","标准增益校准(需要信号)","偏移量校准(需要信号)","电容校准(需要信号)"));
    }

    @Override
    public void setChPga() {
        Channel channel;
        final CabteRegister cabteRegister = CabteRegister.getInstance();
        final int [] vol = new int[ChannelFactory.CH_CNT];
        final int [] result = {0,0,0,0,0};

        int maxIdx = ChannelFactory.getMaxChIdx();

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        if(fpgaCommand.isCalibrate()
                && (fpgaCommand.isZeroCalibrate() || fpgaCommand.isChGainCalibrate())){
            for(int i=ChannelFactory.CH1;i<maxIdx;i++){
                vol[i] = fpgaCommand.getPgaVal();
            }
        }else{
            for(int i=ChannelFactory.CH1;i<maxIdx;i++) {
                channel = ChannelFactory.getDynamicChannel(i);
                if(channel != null) {
                    double vScaleVal = channel.getVScaleVal() / channel.getProbeRate();
                    cabteRegister.calc_pga_fs_gain(channel.getChId(),
                            vScaleVal,
                            result);
                    vol[i] = result[0] & 0xFFFF;
                }
            }
        }

        setChPgaGain(vol);
    }

    @Override
    public void setAdGain(int fpgaIdx) {
        boolean[] sle = new boolean[ChannelFactory.CH_CNT];
        Scope scope = Scope.getInstance();
        int cnt = scope.getChannelSampOnCnt(scope.isRun(true),sle);

        int [] result = {0,0,0,0,0};
        CabteRegister pCabReg = CabteRegister.getInstance();
        FPGACommand fpgaCommand = FPGACommand.getInstance();
        Channel channel;
        float [] gain_bc = {1,1,1,1};

        int adcMaxChNums = HwConfig.getInstance().getAdcMaxChNums();
        int beginIdx = FPGACommand.beginChIdx(fpgaIdx);
        int engIdx = FPGACommand.endChIdx(fpgaIdx);

        int [] ch2ad = {3,0,2,1};
        if(fpgaCommand.isCalibrate() && fpgaCommand.isChGainCalibrate()){
            return;
        }
        switch (cnt){
            case 2:
                for(int i=beginIdx;i<engIdx;i++){
                    if(sle[i]){
                        channel = ChannelFactory.getDynamicChannel(i);
                        if(channel != null) {
                            pCabReg.calc_pga_fs_gain(channel.getChId(),
                                    channel.getVScaleVal() / channel.getProbeRate()
                                    , result);
                            for(int j=0;j<4;j++){
                                fpgaCommand.writeAD_gain(fpgaIdx,0,ch2ad[j],result[j+1] & 0xFFFF);
                                fpgaCommand.writeAD_offset(fpgaIdx,0,j,(result[j+1]>>>16) & 0xFFFF);
                            }
                        }
                    }
                }
                break;
            case 4:
                for(int i=beginIdx;i<engIdx;i++){
                    if(sle[i]){
                        channel = ChannelFactory.getDynamicChannel(i);
                        if(channel != null) {
                            pCabReg.calc_pga_fs_gain(channel.getChId(),
                                    channel.getVScaleVal() / channel.getProbeRate()
                                    , result);
                            int tmpidx = 0;
                            switch (i){
                                case ChannelFactory.CH1:
                                case ChannelFactory.CH5:
                                    tmpidx = 0;
                                    break;
                                case ChannelFactory.CH2:
                                case ChannelFactory.CH6:
                                    tmpidx = 2;
                                    break;
                                default:
                                    break;

                            }
                            for(int j=0;j<2;j++){
                                fpgaCommand.writeAD_gain(fpgaIdx,0,j + tmpidx,result[j+1] & 0xFFFF);
                                fpgaCommand.writeAD_offset(fpgaIdx,0,j + tmpidx,(result[j+1]>>>16) & 0xFFFF);
                            }
                        }
                    }
                }
                break;
            case 8:
            default:
                for(int i=beginIdx;i<engIdx;i++){
                    channel = ChannelFactory.getDynamicChannel(i);
                    if(channel != null) {
                        pCabReg.calc_pga_fs_gain(channel.getChId(),
                                channel.getVScaleVal() / channel.getProbeRate(),
                                result);

                        fpgaCommand.writeAD_gain(fpgaIdx,0,ch2ad[i%adcMaxChNums],result[1] & 0xFFFF);
                        fpgaCommand.writeAD_offset(fpgaIdx,0,ch2ad[i%adcMaxChNums],(result[1] >>> 16) & 0xFFFF);
                    }
                }
                break;
        }

        fpgaCommand.sendFpga_gain_bc(fpgaIdx,gain_bc);
        fpgaCommand.cmdDevice(fpgaIdx,50);
    }


    @Override
    public void changeChVolScale() {

        long marsk= SHIFTREG_INIT_VAL_MHO10008;
        Scope scope = Scope.getInstance();

        Channel channel;
        int dang = 0;
        int idx = 0;
        int chMax = scope.getChNum() + ChannelFactory.CH1;
        long temp=ChVolScaleShiftRegister();
        int  pdVal= 0;
        int resistanceType;
        for(int ch=ChannelFactory.CH1;ch<chMax;ch++){
            idx = ChannelFactory.CH8 - ch;
            channel = ChannelFactory.getDynamicChannel(ch);
            resistanceType = channel.getResistanceType();
            dang = CabteRegister.getRatioIdx(resistanceType,channel.getVScaleVal()/channel.getProbeRate());

            if(resistanceType== Channel.RESISTANCE_50){
                pdVal |= 1 << ch;
                pdVal |= (1 << ch) << 16;

                if(dang == HW.RATIO_DANG_1){
                    marsk &= ~(1L << (idx * 8 + 7));
                }else{
                    marsk &= ~(1L << (idx * 8 + 6));
                    temp &= ~(1L << (idx * 8 + 2));
                }
            }else{

                if(dang == HW.RATIO_DANG_1) {
                    pdVal |= (1 << ch) << 16;
                }else{
                    pdVal |= 1 << ch;
                }

                switch (dang){
                    default:
                    case HW.RATIO_DANG_1:
                        marsk &= ~(1L << (idx * 8 + 7));
                        temp &= ~(1L << (idx * 8 + 3));
                        break;
                    case HW.RATIO_DANG_2:
                        marsk &= ~(1L << (idx * 8 + 6));
                        temp &= ~(1L << (idx * 8 + 5));
                        temp &= ~(1L << (idx * 8 + 3));
                        temp &= ~(1L << (idx * 8 + 2));
                        break;
                    case HW.RATIO_DANG_3:
                        marsk &= ~(1L << (idx * 8 + 6));
                        temp &= ~(1L << (idx * 8 + 3));
                        temp &= ~(1L << (idx * 8 + 2));
                        break;
                    case HW.RATIO_DANG_4:
                        marsk &= ~(1L << (idx * 8 + 6));
                        temp &= ~(1L << (idx * 8 + 4));
                        temp &= ~(1L << (idx * 8 + 3));
                        temp &= ~(1L << (idx * 8 + 2));
                        break;
                }
            }


        }


        sendShiftRegister( (temp & marsk) );
        usleep(3000);
        sendShiftRegister(temp);
        FPGACommand.getInstance().SendChPD(pdVal);
    }


    private long ChVolScaleShiftRegister(){
        long iTmpValue = SHIFTREG_INIT_VAL_MHO10008;

        Scope scope = Scope.getInstance();
        int chMax = scope.getChNum() + ChannelFactory.CH1;
        int idx = 0;
        for(int i=ChannelFactory.CH1;i<chMax;i++){
            idx =  ChannelFactory.CH8 - i;
            Channel channel = ChannelFactory.getDynamicChannel(i);
            if(!scope.isChannelInSample(i)
                    || channel.getCoupleType() == Channel.COUPLE_TYPE_AC){

                iTmpValue &= ~(1L << (idx * 8 + 0));
            }
            //if(scope.isChannelInSample(i))
            {
                //这里需要常开MHO8
                iTmpValue &= ~(1L << (idx * 8 + 1));
            }

        }
        return iTmpValue;
    }

    //校准-----------------------------------------------------------------------------------------

    public static final int MHO68V1_RATIO_DANG_CNT = RATIO_DANG_4 + 1;

    public static final int CALIBRATION_TOP_ZERO = 0;
    public static final int CALIBRATION_CENTER_CH1GAIN_1 = CALIBRATION_TOP_ZERO + 1;
    public static final int CALIBRATION_CENTER_CH2GAIN_1 = CALIBRATION_CENTER_CH1GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH3GAIN_1 = CALIBRATION_CENTER_CH2GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH4GAIN_1 = CALIBRATION_CENTER_CH3GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH5GAIN_1 = CALIBRATION_CENTER_CH4GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH6GAIN_1 = CALIBRATION_CENTER_CH5GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH7GAIN_1 = CALIBRATION_CENTER_CH6GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH8GAIN_1 = CALIBRATION_CENTER_CH7GAIN_1 + 21;


    public static final int CALIBRATION_CENTER_CH1GAIN_2 = CALIBRATION_CENTER_CH8GAIN_1 + 21;
    public static final int CALIBRATION_CENTER_CH2GAIN_2 = CALIBRATION_CENTER_CH1GAIN_2 + VerticalAxis.DANG_CNT * 2;
    public static final int CALIBRATION_CENTER_CH3GAIN_2 = CALIBRATION_CENTER_CH2GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH4GAIN_2 = CALIBRATION_CENTER_CH3GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH5GAIN_2 = CALIBRATION_CENTER_CH4GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH6GAIN_2 = CALIBRATION_CENTER_CH5GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH7GAIN_2 = CALIBRATION_CENTER_CH6GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CALIBRATION_CENTER_CH8GAIN_2 = CALIBRATION_CENTER_CH7GAIN_2 + VerticalAxis.DANG_CNT * 2;;
    public static final int CHOFFSET_CALIBRATE_CH1 = CALIBRATION_CENTER_CH8GAIN_2 + VerticalAxis.DANG_CNT * 2;
    public static final int CHOFFSET_CALIBRATE_CH2 = CHOFFSET_CALIBRATE_CH1 + 12;
    public static final int CHOFFSET_CALIBRATE_CH3 = CHOFFSET_CALIBRATE_CH2 + 12;
    public static final int CHOFFSET_CALIBRATE_CH4 = CHOFFSET_CALIBRATE_CH3 + 12;
    public static final int CHOFFSET_CALIBRATE_CH5 = CHOFFSET_CALIBRATE_CH4 + 12;
    public static final int CHOFFSET_CALIBRATE_CH6 = CHOFFSET_CALIBRATE_CH5 + 12;
    public static final int CHOFFSET_CALIBRATE_CH7 = CHOFFSET_CALIBRATE_CH6 + 12;
    public static final int CHOFFSET_CALIBRATE_CH8 = CHOFFSET_CALIBRATE_CH7 + 12;
    public static final int CHCAP_CALIBRATE_CH1 = CHOFFSET_CALIBRATE_CH8 + 8;
    public static final int CHCAP_CALIBRATE_CH2 = CHCAP_CALIBRATE_CH1 + 4;
    public static final int CHCAP_CALIBRATE_CH3 = CHCAP_CALIBRATE_CH2 + 4;
    public static final int CHCAP_CALIBRATE_CH4 = CHCAP_CALIBRATE_CH3 + 4;

    public static final int CHCAP_CALIBRATE_CH5 = CHCAP_CALIBRATE_CH4 + 4;
    public static final int CHCAP_CALIBRATE_CH6 = CHCAP_CALIBRATE_CH5 + 4;
    public static final int CHCAP_CALIBRATE_CH7 = CHCAP_CALIBRATE_CH6 + 4;
    public static final int CHCAP_CALIBRATE_CH8 = CHCAP_CALIBRATE_CH7 + 4;
    public static final int CALIBRATE_STATE_MAX = CHCAP_CALIBRATE_CH8 + 4;
    public static final int FLASH_MAX = 100 * 1024;

    private static final int MAX_COFIT_SIZE = 30 * 1024;


    byte [] calibartionState = new byte[CALIBRATE_STATE_MAX/8 + 1];

    @Override
    protected int getMaxCofitSize() {
        return MAX_COFIT_SIZE;
    }

    @Override
    protected int getUseFlashMax() {
        return getCalibrateStateAddr() +  CALIBRATE_STATE_MAX/8 + 1;
    }

    @Override
    protected int getFlashMax() {
        return FLASH_MAX;
    }

    @Override
    public void clearCalibrationState() {
        Arrays.fill(calibartionState, (byte) 0);
        write(getCalibrateStateAddr(),calibartionState);
    }

    @Override
    public void saveCalibrationState() {
        write(getCalibrateStateAddr(),calibartionState);
    }

    @Override
    public void setCalibrationState(int idx, boolean bState) {
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){
            int i = idx / 8;
            idx = idx % 8;
            if(bState){
                calibartionState[i] |= (1 << idx);
            }else{
                calibartionState[i] &= ~(1 << idx);
            }
        }
    }
    @Override
    public boolean isCalibrationState(int idx){
        if(idx >=CALIBRATION_TOP_ZERO && idx < CALIBRATE_STATE_MAX){
            int i = idx / 8;
            idx = idx % 8;
            return (calibartionState[i] & (1 << idx)) != 0;
        }
        return false;
    }
    @Override
    public void loadCalibrationState(){
        read(getCalibrateStateAddr(),calibartionState);
    }

    @Override
    public boolean isTopCalibration(){
        return isCalibrationState(CALIBRATION_TOP_ZERO);
    }
    @Override
    public boolean getCalibrationItemState(int idx,StringBuilder sb){

        return false;
    }
    @Override
    protected int ByteBuffer2cofit(ByteBuffer byteBuffer, int idx) {
        idx = getFloatCoef3(coefChannel, byteBuffer, idx);
        idx = getFloatCoef3(chZero, byteBuffer, idx);
        idx = getFloatCoef1(ch_pga_stepdb,byteBuffer,idx);

        idx = getFloatCoef3(ad_fs_a, byteBuffer, idx);
        idx = getFloatCoef4(ad_fs_a_dual, byteBuffer, idx);
        idx = getFloatCoef4(ad_fs_a_single, byteBuffer, idx);
        idx = getFloatCoef2(ad_fs_d,byteBuffer,idx);
        idx = getFloatCoef3(ad_fs_d_dual,byteBuffer,idx);
        idx = getFloatCoef3(ad_fs_d_single,byteBuffer,idx);

        idx = getIntCoef1(chCapacitanceHigh,byteBuffer,idx);

        idx = getShortCoef1(adOffset,byteBuffer,idx);
        idx = getShortCoef2(adOffset_dual,byteBuffer,idx);
        idx = getShortCoef2(adOffset_single,byteBuffer,idx);

        idx = getFloatCoef3(coefChannel_50, byteBuffer, idx);
        idx = getFloatCoef3(chZero_50, byteBuffer, idx);
        idx = getFloatCoef3(chZero_single_50, byteBuffer, idx);
        idx = getFloatCoef3(chZero_dual_50, byteBuffer, idx);
        idx = getFloatCoef3(chZero_single, byteBuffer, idx);
        idx = getFloatCoef3(chZero_dual, byteBuffer, idx);
        if(getVer() >= CODE_VER_3){
            idx = getShortCoef3(chGain,byteBuffer,idx);
            idx = getShortCoef4(chGain_dual,byteBuffer,idx);
            idx = getShortCoef4(chGain_single,byteBuffer,idx);
        }
        //这个需要放在结尾

        cabteTime = "";
        return idx;
    }


    @Override
    protected int cofit2ByteBuffer(ByteBuffer byteBuffer, int idx,boolean bSave) {

        idx = setFloatCoef3(coefChannel, byteBuffer, idx);
        idx = setFloatCoef3(chZero, byteBuffer, idx);
        idx = setFloatCoef1(ch_pga_stepdb,byteBuffer,idx);
        idx = setFloatCoef3(ad_fs_a, byteBuffer, idx);
        idx = setFloatCoef4(ad_fs_a_dual, byteBuffer, idx);
        idx = setFloatCoef4(ad_fs_a_single, byteBuffer, idx);
        idx = setFloatCoef2(ad_fs_d,byteBuffer,idx);
        idx = setFloatCoef3(ad_fs_d_dual,byteBuffer,idx);
        idx = setFloatCoef3(ad_fs_d_single,byteBuffer,idx);
        idx = setIntCoef1(chCapacitanceHigh,byteBuffer,idx);
        idx = setShortCoef1(adOffset,byteBuffer,idx);
        idx = setShortCoef2(adOffset_dual,byteBuffer,idx);
        idx = setShortCoef2(adOffset_single,byteBuffer,idx);
        idx = setFloatCoef3(coefChannel_50, byteBuffer, idx);
        idx = setFloatCoef3(chZero_50, byteBuffer, idx);
        idx = setFloatCoef3(chZero_single_50, byteBuffer, idx);
        idx = setFloatCoef3(chZero_dual_50, byteBuffer, idx);
        idx = setFloatCoef3(chZero_single, byteBuffer, idx);
        idx = setFloatCoef3(chZero_dual, byteBuffer, idx);

        if(getVer() >= CODE_VER_3){
            idx = setShortCoef3(chGain,byteBuffer,idx);
            idx = setShortCoef4(chGain_dual,byteBuffer,idx);
            idx = setShortCoef4(chGain_single,byteBuffer,idx);
        }

        return idx;
    }

    private float [] ch_pga_stepdb = new float[ChannelFactory.CH_CNT];
    private float [][][]coefChannel = new float[ChannelFactory.CH_CNT][4][2];//偏移量系数

    private float [][][]chZero = new float[ChannelFactory.CH_CNT][4][33];//通道零点


    private int [] chCapacitanceHigh = new int[ChannelFactory.CH_CNT * 4];


    // 0: 1M 1,1: 1M 16,2: 1M 64,3: 1M 256,4: 50 2, 5: 50 20

    //4通道 adc fs a1,a2
    private float [][][] ad_fs_a = new float[ChannelFactory.CH_CNT][6][2];

    //双通道 adc fs a1,a2
    private float [][][][] ad_fs_a_dual = new float[ChannelFactory.CH_CNT][6][2][2];

    private float [][][][] ad_fs_a_single = new float[ChannelFactory.CH_CNT][6][2][4];


    private float [][] ad_fs_d = new float[ChannelFactory.CH_CNT][6];

    private float [][][] ad_fs_d_dual = new float[ChannelFactory.CH_CNT][6][2];
    private float [][][] ad_fs_d_single = new float[ChannelFactory.CH_CNT][6][4];

    //通道，挡位，模式，4个ADC

    private short[][][] chGain = new short[ChannelFactory.CH_CNT][VerticalAxis.DANG_CNT][2];
    private short[][][][] chGain_dual = new short[ChannelFactory.CH_CNT][VerticalAxis.DANG_CNT][2][2];
    private short[][][][] chGain_single = new short[ChannelFactory.CH_CNT][VerticalAxis.DANG_CNT][2][4];


    private short [] adOffset = new short[ChannelFactory.CH_CNT];

    private short [][] adOffset_dual = new short[ChannelFactory.CH_CNT][2];

    private short [][] adOffset_single = new short[ChannelFactory.CH_CNT][4];

    private float [][][]coefChannel_50 = new float[ChannelFactory.CH_CNT][2][2];//偏移量系数

    private float [][][]chZero_50 = new float[ChannelFactory.CH_CNT][2][33];//通道零点

    private float [][][]chZero_single = new float[4][4][33];//通道零点
    private float [][][]chZero_single_50 = new float[4][2][33];//通道零点

    private float [][][]chZero_dual = new float[4][4][33];//通道零点
    private float [][][]chZero_dual_50 = new float[4][2][33];//通道零点

    private static final float DA_CH_OFFSET_ZERO = 32768f;
    public static double [] GAIN_INPUT_AMP ={
            0.0174,0.087,0.437,
            //直通
            0.0097,0.0104,0.0111,
            //9.28
            0.14,0.15,0.16,
            //21.82
            0.563,0.602,0.641,
            //91.51
            2.347,2.51,2.673,
            //50 1.35
            0.0243,0.0261,0.0279,
            //50 11.24
            0.287,0.308,0.33
    };
    public static int [] GAIN_PGA_CODE = {
            0x202,0x210,0x21E,
            //1
            0x212,0x212,0x212,
            //9.28
            0x215,0x215,0x215,
            //21.82
            0x21A,0x21A,0x21A,
            //91.51
            0x21A,0x21A,0x21A,
            //50 1.35
            0x21A,0x21A,0x21A,
            //50 11.24
            0x219,0x219,0x219,

    };

    public static int [] GAIN_ADFS_CODE = {
            0x800,0x800,0x800,
            //1
            684,2048,3415,
            //9.28
            705,2048,3410,
            //21.82
            737,2048,3365,
            //91.51
            746,2048,3383,
            //50 1.35
            658,2048,3457,
            //50 11.24
            656,2048,3487,
    };

    public static int [] GAIN_DB = {8,5,0,0,0,1};


    public static double [] RATIO_DANG_VAL = {0.054,0.55,1.24,5};
    public static double [] RATIO_DANG_VAL_50 = {0.054,0.7};
    @Override
    public int getRatioDangCnt() {
        return MHO68V1_RATIO_DANG_CNT;
    }

    @Override
    protected void defaultVal() {
        defaultVal_ChGain();
        defaultVal_AdFullscale();
        defaultVal_coefChannel();
        defaultVal_chZero();
        defaultVal_ChCapacitance();
        defaultVal_AdGain();
        defaultVal_AdOffset();
    }
    public void defaultVal_ChGain(){
        for(int i=0;i<chGain.length;i++){
            for (int j=0;j<chGain[i].length;j++){
                for(int k=0;k<chGain[i][j].length;k++) {
                    chGain[i][j][k] = 0x800;
                    Arrays.fill(chGain_dual[i][j][k] , (short) 0x800);
                    Arrays.fill(chGain_single[i][j][k] , (short) 0x800);
                }
            }
        }
    }
    public void defaultVal_AdFullscale(){
        Arrays.fill(ch_pga_stepdb, 1.0f);

        for(int i=0;i<ad_fs_d.length;i++){
            for(int k=0;k<6;k++){
                ad_fs_d[i][k] = (float) GAIN_INPUT_AMP[(k + 1) * 3 + 1];
            }
        }
        for(int i=0;i<ad_fs_d_dual.length;i++){
            for(int j=0;j<2;j++) {
                for(int k=0;k<6;k++){
                    ad_fs_d_dual[i][k][j] = (float) GAIN_INPUT_AMP[(k + 1) * 3 + 1];
                }
            }
        }
        for(int i=0;i<ad_fs_d_single.length;i++){
            for(int j=0;j<4;j++) {
                for(int k=0;k<6;k++){
                    ad_fs_d_single[i][k][j] = (float) GAIN_INPUT_AMP[(k + 1) * 3 + 1];
                }
            }
        }

        for(int i=0;i<ad_fs_a.length;i++){
            for(int k=0;k<6;k++){
                int idx = (k + 1) * 3;
                ad_fs_a[i][k][0] = (float) ((GAIN_ADFS_CODE[idx + 1] - GAIN_ADFS_CODE[idx]) / (GAIN_INPUT_AMP[ idx + 1] - GAIN_INPUT_AMP[idx]));
                ad_fs_a[i][k][1] = (float) ((GAIN_ADFS_CODE[idx + 2] - GAIN_ADFS_CODE[idx + 1]) / (GAIN_INPUT_AMP[ idx + 2] - GAIN_INPUT_AMP[idx + 1]));
            }
        }

        for(int i=0;i<ad_fs_a_dual.length;i++){
            for(int k=0;k<2;k++) {
                ad_fs_a_dual[i][0][0][k] = ad_fs_a[i][0][0];
                ad_fs_a_dual[i][0][1][k] = ad_fs_a[i][0][1];
                ad_fs_a_dual[i][1][0][k] = ad_fs_a[i][1][0];
                ad_fs_a_dual[i][1][1][k] = ad_fs_a[i][1][1];
                ad_fs_a_dual[i][2][0][k] = ad_fs_a[i][2][0];
                ad_fs_a_dual[i][2][1][k] = ad_fs_a[i][2][1];
                ad_fs_a_dual[i][3][0][k] = ad_fs_a[i][3][0];
                ad_fs_a_dual[i][3][1][k] = ad_fs_a[i][3][1];
                ad_fs_a_dual[i][4][0][k] = ad_fs_a[i][4][0];
                ad_fs_a_dual[i][4][1][k] = ad_fs_a[i][4][1];
                ad_fs_a_dual[i][5][0][k] = ad_fs_a[i][5][0];
                ad_fs_a_dual[i][5][1][k] = ad_fs_a[i][5][1];
            }
        }
        for(int i=0;i<ad_fs_a_single.length;i++){
            for(int k=0;k<4;k++) {
                ad_fs_a_single[i][0][0][k] = ad_fs_a[i][0][0];
                ad_fs_a_single[i][0][1][k] = ad_fs_a[i][0][1];
                ad_fs_a_single[i][1][0][k] = ad_fs_a[i][1][0];
                ad_fs_a_single[i][1][1][k] = ad_fs_a[i][1][1];
                ad_fs_a_single[i][2][0][k] = ad_fs_a[i][2][0];
                ad_fs_a_single[i][2][1][k] = ad_fs_a[i][2][1];
                ad_fs_a_single[i][3][0][k] = ad_fs_a[i][3][0];
                ad_fs_a_single[i][3][1][k] = ad_fs_a[i][3][1];
                ad_fs_a_single[i][4][0][k] = ad_fs_a[i][4][0];
                ad_fs_a_single[i][4][1][k] = ad_fs_a[i][4][1];
                ad_fs_a_single[i][5][0][k] = ad_fs_a[i][5][0];
                ad_fs_a_single[i][5][1][k] = ad_fs_a[i][5][1];
            }
        }

    }
    private void defaultVal_coefChannel(){

        for(int i=0;i<coefChannel.length;i++){
            for(int j=0;j<coefChannel[i].length;j++){
                coefChannel[i][j][1] = coefChannel[i][j][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_1M,j);
            }
            for(int j=0;j<coefChannel_50[i].length;j++){
                coefChannel_50[i][j][1] = coefChannel_50[i][j][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_50,j);
            }
        }

    }

    private void defaultVal_chZero(){

        for(int j=0; j<chZero.length; j++) {
            for(int i = 0; i< chZero[j][0].length; i++){
                chZero[j][0][i] = 32097.29279119318f;
                chZero[j][1][i] = 33072.76100852273f;
                chZero[j][2][i] = 33073.15861742424f;
                chZero[j][3][i] = 33072.755208333336f;
                chZero_50[j][0][i] = 30698.228160511364f;
                chZero_50[j][1][i] = 33092.930279356064f;
            }
        }
        for(int j=0; j<chZero_single.length; j++) {
            for(int i = 0; i< chZero_single[j][0].length; i++){

                chZero_dual[j][0][i] = 32080.794625946968f;
                chZero_dual[j][1][i] = 33052.20276988636f;
                chZero_dual[j][2][i] = 33052.14299242424f;
                chZero_dual[j][3][i] = 33051.75497159091f;

                chZero_dual_50[j][0][i] = 30685.549479166668f;
                chZero_dual_50[j][1][i] = 33071.687618371216f;

                chZero_single[j][0][i] = 32098.86310369318f;
                chZero_single[j][1][i] = 33073.44365530303f;
                chZero_single[j][2][i] = 33073.67151988636f;
                chZero_single[j][3][i] = 33073.41299715909f;

                chZero_single_50[j][0][i] = 30698.618903882576f;
                chZero_single_50[j][1][i] = 33093.517755681816f;


            }
        }
    }

    private void defaultVal_ChCapacitance(){
        int val = 0xFFFF;
        val /= 2;
        Arrays.fill(chCapacitanceHigh, val);
    }

    private void defaultVal_AdGain(){
        Arrays.fill(ch_pga_stepdb,1.0f);
    }
    public void defaultVal_coefChannel(int ch,int dang,int resistanceType){

        if(resistanceType == Channel.RESISTANCE_1M) {
            coefChannel[ch][dang][1] = coefChannel[ch][dang][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_1M, dang);
        }else{
            coefChannel_50[ch][dang][1] = coefChannel_50[ch][dang][0] = (float) vol_ChannelCoef_default(Channel.RESISTANCE_50, dang);
        }
    }
    public void defaultVal_AdOffset(){
        Arrays.fill(adOffset,(short) 0x100);
        for (short []ints : adOffset_dual) {
            Arrays.fill(ints, (short) 0x100);
        }
        for (short []ints : adOffset_single) {
            Arrays.fill(ints, (short) 0x100);
        }
    }

    private double getLMH6401(int pgaval){
        return Math.pow (10,(26 - pgaval)/20.0);
    }


    public double vol_ChannelCoef_defaultEx(int chIdx,int dangwei,int pgaVal){
//        Log.d(TAG, "vol_ChannelCoef_defaultEx() called with: chIdx = [" + chIdx + "], dangwei = [" + dangwei + "], pgaVal = [" + (pgaVal & 0xFF) + "]");
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();

        pgaVal &= 0xFF;
        double v = 1;
        double r = 0.5;
        int dang = getRatioIdx(resistanceType,VerticalAxis.getScaleIdValById(dangwei));
        if(resistanceType == Channel.RESISTANCE_50){
            if(dang == RATIO_DANG_1){
                r = 0.762924442 * 10.16 * 100.0 / 122;
                v = 0.962;
            }else{
                r = 0.473784195 * 10.16 * 100.0 / 122;
                v = 1.02;
            }
        }else {
            if (dang == RATIO_DANG_1) {
                r = 0.564381082 * 10.16 * 100.0 / 122;
                v = 0.96387;
            } else {
                r = 0.473784195 * 10.16 * 100.0 / 122;
                v = 1.02;
            }
        }
        r = r * getLMH6401(pgaVal)  * (100.0 / 120);
        return (v * 65536) /( 2.5 * 2 * r * 1023);
    }

    public double vol_ChannelCoef_default(int resistanceType,int dang){
        double [] vv = {1,9.28,21.82,91.51};
        if(resistanceType == Channel.RESISTANCE_1M){
            return (VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,dang)) * 65536.0 / 500.0 /vv[dang]);
        }else{
            vv[0] = 0.9554/1.35;
            vv[1] = 0.9674/11.24;
            return (VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,dang))* 65536.0 * vv[dang] / 500.0);
        }
    }
    public int getRatioIdx(int resistanceType,double v){
        int i = 0;
        double [] ratioDangVal = resistanceType == 0 ? RATIO_DANG_VAL : RATIO_DANG_VAL_50;

        for(i=0;i<ratioDangVal.length;i++){
            if(v <= ratioDangVal[i]){
                break;
            }
        }
        if(i >= ratioDangVal.length){
            i = ratioDangVal.length - 1;
        }
        return i;
    }

    @Override
    public int getRatioIdx2Dang(int resistanceType,int idx){

        switch (idx){
            default:
            case RATIO_DANG_1:
                return  VerticalAxis.DANG_50mV;
            case RATIO_DANG_2:
                return  VerticalAxis.DANG_200mV;
            case RATIO_DANG_3:
                return  VerticalAxis.DANG_1V;
            case RATIO_DANG_4:
                return  VerticalAxis.DANG_5V;
        }

    }
    @Override
    public double getVerticalRange(int resistanceType,int dang){
        double vRange = 2.5;
        switch (dang) {
            case RATIO_DANG_1:
                vRange = 2.0;
                break;
            case RATIO_DANG_2:
            case RATIO_DANG_3:
                vRange = 20.0;
                break;
            default:
            case RATIO_DANG_4:
                vRange = 180.0;
                break;
        }
        return vRange;
    }

    @Override
    public float getChannelCoef(int chIdx,int dang,int idx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel.getResistanceType() == Channel.RESISTANCE_50){
            return coefChannel_50[chIdx][dang][idx];
        }else{
            return coefChannel[chIdx][dang][idx];
        }
    }
    @Override
    public void setChannelCoef(int chIdx,int dang,int idx,float val){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        if(channel.getResistanceType() == Channel.RESISTANCE_50){
            coefChannel_50[chIdx][dang][idx] = val;
        }else{
            coefChannel[chIdx][dang][idx] = val;
        }
    }

    @Override
    public void setChannelZero(int chIdx,int dwIdx,int pga,float val){

        Scope scope = Scope.getInstance();
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int cnt = scope.getChannelSampOnCnt()/4;
        int resistanceType = channel.getResistanceType();
        switch (cnt){
            case 0:
                chIdx = chIdx%4 + 2 * (chIdx/4);
                if(resistanceType == Channel.RESISTANCE_50){
                    chZero_single_50[chIdx][dwIdx][pga] = val;
                }else {
                    chZero_single[chIdx][dwIdx][pga] = val;
                }
                break;
            case 1:
                chIdx = chIdx%4 + 2 * (chIdx/4);
                if(resistanceType == Channel.RESISTANCE_50){
                    chZero_dual_50[chIdx][dwIdx][pga] = val;
                }else {
                    chZero_dual[chIdx][dwIdx][pga] = val;
                }
                break;
            default:
            case 2:
                if(resistanceType == Channel.RESISTANCE_50){
                    chZero_50[chIdx][dwIdx][pga] = val;
                }else {
                    chZero[chIdx][dwIdx][pga] = val;
                }
                break;
        }
    }

    @Override
    public float getChannelZero(int chIdx,int dwIdx,int pga) {

        Scope scope = Scope.getInstance();

        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int cnt = scope.getChannelSampOnCnt()/4;
        int resistanceType = channel.getResistanceType();

        switch (cnt){
            case 0: {
                if (chIdx % 4 < 2) {
                    chIdx = chIdx % 4 + 2 * (chIdx/4);
                    if (resistanceType == Channel.RESISTANCE_50) {
                        return chZero_single_50[chIdx][dwIdx][pga];
                    } else {
                        return chZero_single[chIdx][dwIdx][pga];
                    }
                } else {
                    return DA_CH_OFFSET_ZERO;
                }
            }
            case 1:
                if(chIdx % 4 < 2) {
                    chIdx = chIdx % 4 + 2 * (chIdx/4);
                    if (resistanceType == Channel.RESISTANCE_50) {
                        return chZero_dual_50[chIdx][dwIdx][pga];
                    } else {
                        return chZero_dual[chIdx][dwIdx][pga];
                    }
                }else{
                    return DA_CH_OFFSET_ZERO;
                }
            default:
            case 2:
                if(resistanceType == Channel.RESISTANCE_50){
                    return chZero_50[chIdx][dwIdx][pga];
                }else {
                    return chZero[chIdx][dwIdx][pga];
                }
        }
    }

    @Override
    public int getChCapacitanceHigh(int chIdx,int dang){
        return chCapacitanceHigh[chIdx * MHO68V1_RATIO_DANG_CNT + dang];
    }

    @Override
    public void setChCapacitanceHigh(int chIdx,int dang ,int val){
        chCapacitanceHigh[chIdx * MHO68V1_RATIO_DANG_CNT + dang] = val;
    }


    @Override
    public double calc_coefChannel(int chIdx,double scaleVal,int idx){
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        int refScaleId = getRatioIdx(resistanceType,scaleVal);
        double v = VerticalAxis.getScaleIdValById(getRatioIdx2Dang(resistanceType,refScaleId));

        if(resistanceType == Channel.RESISTANCE_50) {
            v = (coefChannel_50[chIdx][refScaleId][idx] * scaleVal / v);
        }else {
            v = (coefChannel[chIdx][refScaleId][idx] * scaleVal / v);
        }
        return v;
    }

    @Override
    public void calc_pga_fs_gain(int chIdx,double scaleVal,int []result){
        Scope scope = Scope.getInstance();
        int cnt = scope.getChannelSampOnCnt()/4;
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);
        int resistanceType = channel.getResistanceType();
        calc_pga_gain(cnt,chIdx,resistanceType,scaleVal, result);
        if(getVer() >= CODE_VER_3){

            int scaleId = VerticalAxis.DANG_NONE;
            for(int i=VerticalAxis.getMinGear();i<=VerticalAxis.getMaxGear();i++){
                if(DoubleUtil.FuzzyCompare(scaleVal,VerticalAxis.getScaleIdValById(i))){
                    scaleId = i;
                    break;
                }
            }
            //补丁
            if(resistanceType == Channel.RESISTANCE_50 && channel.isEnable_50O_700mV()){
                if(DoubleUtil.FuzzyCompare(scaleVal,0.7)){
                    scaleId = VerticalAxis.DANG_1V;
                }
            }

            if(VerticalAxis.isValidScaleIdExt(scaleId)){
                int [] result_bak = new int[result.length];
                calc_pga_gain(2,chIdx,resistanceType,scaleVal, result_bak);
                result[0] = result_bak[0];
                int N = 4 - (cnt * 2);
                if(N == 0) N = 1;
                for(int i=0;i<N;i++){
                    int val = getChGain(chIdx,resistanceType,scaleId,cnt,i);
                    result[1 + i] = (result[1 + i] & 0xFFFF0000) | (val & 0xFFFF);
                }
            }
        }
    }
    @Override
    public void calc_pga_gain(int cnt,int chIdx,int resistanceType,double scaleVal,int []result){

        scaleVal = VerticalAxis.clampMin(scaleVal,resistanceType);

        int dang = getRatioIdx(resistanceType,scaleVal);
        double xs = 1.0;
        if(resistanceType == Channel.RESISTANCE_50){
            dang += RATIO_DANG_4 + 1;
        }


        int N = 4 - (cnt * 2);
        if(N == 0) N = 1;
        double y = 0;

        for(int i=0;i<N;i++){
            y += 20 * Math.log10(getAdFsD1(chIdx,dang,cnt,i)/scaleVal)/ch_pga_stepdb[chIdx];
        }
        y /= N;

        int p = GAIN_DB[dang];
        y += p;
        int m = (int)Math.round(y);

        if(m  > 26) m  = 26;
        else if(m < -6) m = -6;
        result[0] = 26 - m + 0x200;

//            Log.d(TAG,"ch:" + chIdx + ",scaleVal:" + scaleVal
//                    + ",pga:" + Integer.toHexString(result[0])
//                    + ",resistanceType:" + resistanceType
//                    + ",dang:" + dang + ",pga_setpdb:" + ch_pga_stepdb[chIdx]
//            );


        N = 4 - (cnt * 2);
        if(N == 0) N = 1;
        for(int i=0;i<N;i++){
            double db = ch_pga_stepdb[chIdx] * xs;
            y = scaleVal * Math.pow(10,(m - p)/db/20) - getAdFsD1(chIdx,dang,cnt,i);
//                y = scaleVal * Math.pow(10,(m - p)/20.0) - getAdFsD1(chIdx,dang,cnt,i);
            result[1 + i] = calcAdfs(y,getAdFsA(chIdx,dang,cnt,i,y < 0 ? 0 : 1)) | getAdOffset(chIdx,cnt,i) << 16;
//                Log.d(TAG,"i:" + i + "," + (result[1+i] & 0xFFFF) + ",d1:" + getAdFsD1(chIdx,dang,cnt,i));
        }
    }
    private int calcAdfs(double y,double a){
        int m = (int)(Math.round(2048 + y * a));
        if(m > 0xFFF){
            m = 0xFFF;
        }else if(m < 0){
            m = 0;
        }
        return m;
    }
    public void setADFsD1(int chIdx,int idx,int chMode,int adcIdx,float val){
        switch (chMode){
            case 0:
                ad_fs_d_single[chIdx][idx][adcIdx] = val;
                break;
            case 1:
                ad_fs_d_dual[chIdx][idx][adcIdx] = val;
                break;
            case 2:
                ad_fs_d[chIdx][idx] = val;
                break;
        }

    }

    public float getAdFsD1(int chIdx,int idx,int chMode,int adcIdx){
        switch (chMode){
            case 0:
                return ad_fs_d_single[chIdx][idx][adcIdx];
            case 1:
                return ad_fs_d_dual[chIdx][idx][adcIdx];
            case 2:
            default:
                return ad_fs_d[chIdx][idx];
        }
    }


    public void setAdFsA(int chIdx,int Idx,int chMode,int adIdx,int pn,float val){
        switch (chMode){
            case 0:
                ad_fs_a_single[chIdx][Idx][pn][adIdx] = val;
                break;
            case 1:
                ad_fs_a_dual[chIdx][Idx][pn][adIdx] = val;
                break;
            case 2:
                ad_fs_a[chIdx][Idx][pn] = val;
                break;
        }
    }

    public float getAdFsA(int chIdx,int Idx,int chMode,int adIdx,int pn){
        switch (chMode){
            case 0:
                return  ad_fs_a_single[chIdx][Idx][pn][adIdx];
            case 1:
                return  ad_fs_a_dual[chIdx][Idx][pn][adIdx];
            default:
            case 2:
                return  ad_fs_a[chIdx][Idx][pn];
        }
    }

    public void setAdOffset(int chIdx,int chMode,int adIdx,int val){
        switch (chMode){
            case 0:
                adOffset_single[chIdx][adIdx] = (short) val;
                break;
            case 1:
                adOffset_dual[chIdx][adIdx] = (short) val;
                break;
            case 2:
                adOffset[chIdx] = (short) val;
                break;
        }
    }
    public int getAdOffset(int chIdx,int chMode,int adIdx){
        switch (chMode){
            case 0:
                return adOffset_single[chIdx][adIdx];
            case 1:
                return adOffset_dual[chIdx][adIdx];
            default:
            case 2:
                return adOffset[chIdx];
        }
    }
    public void setChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx,int val){

        switch (chMode){
            case 0:
                chGain_single[chIdx][vIdx][resistanceType][adIdx] = (short) val;
                break;
            case 1:
                chGain_dual[chIdx][vIdx][resistanceType][adIdx] = (short) val;
                break;
            case 2:
                chGain[chIdx][vIdx][resistanceType] = (short) val;
                break;
        }
    }

    public int getChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx){
        switch (chMode){
            case 0:
                return chGain_single[chIdx][vIdx][resistanceType][adIdx];
            case 1:
                return chGain_dual[chIdx][vIdx][resistanceType][adIdx];
            default:
            case 2:
                return chGain[chIdx][vIdx][resistanceType];
        }
    }

    @Override
    public void calcGain(int flag){

        int idx = (flag >>> 24) & 0xFF;
        int chMode = (flag >>> 16) & 0xF;
        flag = flag & 0xFFFF;

        int k = (idx + 1) * 3;

        for(int i=ChannelFactory.CH1;i<ChannelFactory.CH_CNT;i++){

            if((flag & (1<<i)) != 0) {

                switch (chMode){
                    case 0://单通道

                        for(int j=0;j<4;j++) {
                            setAdFsA(i,idx,chMode,j,0,
                                    (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, j) - getGainAdFsD (i, chMode, 0, j))));
                            setAdFsA(i,idx,chMode,j,1,
                                    (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, j) - getGainAdFsD (i, chMode, 1, j))));
                            setADFsD1(i,idx,chMode, j, (float)getGainAdFsD (i, chMode, 1, j));
                        }

                        break;
                    case 1://双通道
                        for(int j=0;j<2;j++) {
                            setAdFsA(i,idx,chMode,j,0,
                                    (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, j) - getGainAdFsD (i, chMode, 0, j))));
                            setAdFsA(i,idx,chMode,j,1,
                                    (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, j) - getGainAdFsD (i, chMode, 1, j))));

                            setADFsD1(i,idx,chMode, j, (float)getGainAdFsD (i, chMode, 1, j));
                        }
                        break;
                    case 2://四通道
                        setAdFsA(i,idx,chMode,0,0,
                                (float) ((GAIN_ADFS_CODE[k+1] - GAIN_ADFS_CODE[k])/(getGainAdFsD (i, chMode, 1, 0) - getGainAdFsD (i, chMode, 0, 0))));
                        setAdFsA(i,idx,chMode,0,1,
                                (float) ((GAIN_ADFS_CODE[k+2] - GAIN_ADFS_CODE[k+1])/(getGainAdFsD (i, chMode, 2, 0) - getGainAdFsD (i, chMode, 1, 0))));


                        setADFsD1(i,idx,chMode, 0, (float)getGainAdFsD (i, chMode, 1, 0));
                        break;
                }
            }
        }
//            dumpGain();
    }

    @Override
    public void calcPgaSetp(int flag){
        int maxIdx = ChannelFactory.getMaxChIdx();
        for(int i=ChannelFactory.CH1;i<maxIdx;i++) {

            if ((flag & (1 << i)) != 0) {

                double n1 = 20 * Math.log10(GAIN_INPUT_AMP[2]*1000/gain_pga_stepdb_a3[i]);
                double n2 = 20 * Math.log10(GAIN_INPUT_AMP[1]*1000/gain_pga_stepdb_a2[i]);
                double n3 = 20 * Math.log10(GAIN_INPUT_AMP[0]*1000/gain_pga_stepdb_a1[i]);
                //(-4,n1),(10,n2),(24,n3)

                double k = ((n2 + n3) / 2 - (n1 + n2)/2 ) / (GAIN_PGA_CODE[1] - GAIN_PGA_CODE[0]);;
                ch_pga_stepdb[i] = (float)(-k);

//                    Log.d("zhuzh","ch:" + i + ",pgastep:" + ch_pga_stepdb[i]
//                            + ",a1:" + gain_pga_stepdb_a1[i]
//                            + ",a2:" + gain_pga_stepdb_a2[i]
//                            + ",a3:" + gain_pga_stepdb_a3[i]);
            }
        }
    }
    public double [] gain_pga_stepdb_a1 = new double[ChannelFactory.CH_CNT];
    public double [] gain_pga_stepdb_a2 = new double[ChannelFactory.CH_CNT];
    public double [] gain_pga_stepdb_a3 = new double[ChannelFactory.CH_CNT];

    public void setGainPgaA1(int chIdx,double v){
        gain_pga_stepdb_a1[chIdx] = v;
    }
    public void setGainPgaA2(int chIdx,double v){
        gain_pga_stepdb_a2[chIdx] = v;
    }

    public void setGainPgaA3(int chIdx,double v){
        gain_pga_stepdb_a3[chIdx] = v;
    }



    public double [][][][] gain_ad_fs_d = new double[ChannelFactory.CH_CNT][3][3][4];

    public void setGainAdFsD(int chIdx,int chMode,int idx,int adcIdx,double val){
        gain_ad_fs_d[chIdx][chMode][idx][adcIdx] = val;
    }
    public double getGainAdFsD(int chIdx,int chMode,int idx,int adcIdx){
        return gain_ad_fs_d[chIdx][chMode][idx][adcIdx];
    }
}
