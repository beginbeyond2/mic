package com.micsig.tbook.tbookscope.middleware.command;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.CalibrateService;
import com.micsig.tbook.scope.Calibrate.FactorCalibrate;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.List;

public class Command_Calibrate {
//    new SCPICommandStruct(":CALibrate:DATE?","SCPI_Calibrate","DateQ"),//查询上次校准时间
//    new SCPICommandStruct(":CALibrate:STARt","SCPI_Calibrate","Start"),//开始校准
//    new SCPICommandStruct(":CALibrate:QUIT","SCPI_Calibrate","Quit"),//退出校准，校准完成后的操作
//    new SCPICommandStruct(":CALibrate:STOP","SCPI_Calibrate","Stop"),//停止校准，强制停止
//    new SCPICommandStruct(":CALibrate:RESult?","SCPI_Calibrate","ResultQ"),//查询校准结果
//    new SCPICommandStruct(":CALibrate:ZERopoint","SCPI_Calibrate","ZeroPoint"),//零点校准
//    new SCPICommandStruct(":CALibrate:ZERopoint?","SCPI_Calibrate","ZeroPointQ"),//查询零点校准状态
//    new SCPICommandStruct(":CALibrate:CHDF","SCPI_Calibrate","Chdf"),//通道差异校准
//    new SCPICommandStruct(":CALibrate:CHDF?","SCPI_Calibrate","ChdfQ"),//查询通道差异校准状态
//    new SCPICommandStruct(":CALibrate:ADPHa","SCPI_Calibrate","Adpha"),//AD相位校准
//    new SCPICommandStruct(":CALibrate:ADPHa?","SCPI_Calibrate","AdphaQ"),//查询AD相位校准状态
//    new SCPICommandStruct(":CALibrate:ADGain","SCPI_Calibrate","AdGain"),//AD增益校准
//    new SCPICommandStruct(":CALibrate:ADGain?","SCPI_Calibrate","AdGinQ"),//查询AD增益校准状态
//    new SCPICommandStruct(":CALibrate:OFFSet","SCPI_Calibrate","Offset"),//偏移量校准
//    new SCPICommandStruct(":CALibrate:OFFSet?","SCPI_Calibrate","OffsetQ"),//查询偏移量校准状态
//    new SCPICommandStruct(":CALibrate:CHGain","SCPI_Calibrate","ChGain"),//通道增益校准
//    new SCPICommandStruct(":CALibrate:CHGain?","SCPI_Calibrate","ChGainQ"),//查询通道增益校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint","SCPI_Calibrate","Trigger_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint?","SCPI_Calibrate","Trigger_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint","SCPI_Calibrate","Trigger_AC_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint?","SCPI_Calibrate","Trigger_AC_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient","SCPI_Calibrate","Trigger_Coefficient"),//触发系数校准
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient?","SCPI_Calibrate","Trigger_CoefficientQ"),//查询触发系数校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise","SCPI_Calibrate","Trigger_Precise"),//精准触发校准
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise?","SCPI_Calibrate","Trigger_PreciseQ"),//查询精准触发校准状态
//    new SCPICommandStruct(":CALibrate:DATE:LENGth?","SCPI_Calibrate","Date_LengthQ"),//查询校准数据长度
//    new SCPICommandStruct(":CALibrate:DATE:GET","SCPI_Calibrate","Date_Get"),//获取校准数据
//    new SCPICommandStruct(":CALibrate:FILE:RESet?","SCPI_Calibrate","File_ResetQ"),//获取校准数据

    public String DateQ(boolean isUpdateUI) {
        return CabteRegister.getInstance().getCabteTime();
    }

    public String Start(boolean isUpdateUI) {
        Logger.i("scpi", "start");
        //onCalBegin();
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SELFADJUST);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
        return ToolsSCPI.getOKAY();
    }

    public String Quit(boolean isUpdateUI) {
        Logger.i("scpi", "Quit");
        //onCalEnd();
        return ToolsSCPI.getOKAY();
    }

    public String Stop(boolean isUpdateUI) {
        Logger.i("scpi", "stop");
        return onCalStop();
    }

    public String ResultQ(boolean isUpdateUI) {
           if ((FactorCalibrate.getInstance().isCalibrating())){
               return "caling";
           }
           List s = CalibrateService.getInstance().getCalibrate().getResultString();
           StringBuilder sb = new StringBuilder();
           for (int i = 0; i < s.size(); i++) {
               sb.append(s.get(i));
           }
           Logger.i("scpi", "ResultQ  :" + sb.toString());
           return sb.toString();
    }

    /**
     * 零点校准
     */
    public void ZeroPoint(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin(CalibrateService.ZERO_CALIBRATE);
    }

    /**
     * 零点校准结果
     *
     * @param isUpdateUI
     * @return
     */
    public String ZeroPointQ(boolean isUpdateUI) {
        boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.ZERO_CALIBRATE);
        return ToolsSCPI.getSuccState(b);
    }

    /**
     * 通道差异
     *
     * @param isUpdateUI
     */
    public void Chdf(boolean isUpdateUI) {
//        FactorCalibrate.getInstance().begin(CalibrateService.CHDIFF_CALIBRATE);
    }

    public String ChdfQ(boolean isUpdateUI) {
        boolean b = false;
        return ToolsSCPI.getSuccState(b);
    }

    public void Adpha(double param1, boolean isUpdateUI) {

    }

    public void AdphaQ(boolean isUpdateUI) {

    }

    public void AdGain(boolean isUpdateUI) {
        //FactorCalibrate.getInstance().begin(CalibrateService.ADGAIN_CALIBRATE);
    }

    public String AdGinQ(boolean isUpdateUI) {
        boolean b = false;//FactorCalibrate.getInstance().getCalResult(CalibrateService.ADGAIN_CALIBRATE);
        return ToolsSCPI.getSuccState(b);
    }

    public void Offset(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin(CalibrateService.CHCOEF_CALIBRATE);
    }

    public String OffsetQ(boolean isUpdateUI) {
        boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCOEF_CALIBRATE);
        return ToolsSCPI.getSuccState(b);
    }
    public void ExChGain(double chIdx,double amp,double resType,double stdAmp,boolean isUpdateUI){

        double[] param = {chIdx,amp,resType,stdAmp};
        FactorCalibrate.getInstance().begin(CalibrateService.CHGAIN_CALIBRATE_EX, param);
    }

    public String ExChGainQ(boolean isUpdateUI) {
        String result;
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHGAIN_CALIBRATE_EX);
            result=ToolsSCPI.getSuccState(b);
        }
        return result;
    }

    public void ChGain(double chIndex, double amp, double chmode,double idx,boolean isUpdateUI) {
        double[] param = {chIndex, amp,chmode,idx};
        FactorCalibrate.getInstance().begin(CalibrateService.CHGAIN_CALIBRATE, param);

    }

    public String ChGainQ(boolean isUpdateUI) {
        String result;
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHGAIN_CALIBRATE);
            result=ToolsSCPI.getSuccState(b);
        }
        return result;
    }

    public String ChValQ(double ch,boolean isUpdateUI) {
        return ToolsSCPI.getDouble(CabteRegister.getInstance().getNewInputV((int)Math.round(ch)));
    }
    public void ChSetVal(double ch,double amp,boolean isUpdateUI) {
        Log.d("xxxx", "ChSetVal() called with: ch = [" + ch + "], amp = [" + amp + "], isUpdateUI = [" + isUpdateUI + "]");
        CabteRegister.getInstance().setChAmp((int)Math.round(ch),amp);
    }
    public void ChCofit(double chIndex, double gearIndex, double amp,double resistanceType,boolean isUpdateUI){
        double[] param = {chIndex, gearIndex,amp,resistanceType};
        FactorCalibrate.getInstance().begin(CalibrateService.CHCOEF_CALIBRATE, param);
    }

    public String ChCofitQ(boolean isUpdateUI) {
        String result;
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCOEF_CALIBRATE);
            result=ToolsSCPI.getSuccState(b);
        }
        return result;
    }

    public void CapVal(int chIdx, double v,int val, boolean isUpdateUI) {

        CabteRegister cabteRegister = CabteRegister.getInstance();

        if(ChannelFactory.isDynamicCh(chIdx)){

            cabteRegister.setChCapacitanceHigh(chIdx,cabteRegister.getRatioIdx(Channel.RESISTANCE_1M,v),val);

            cabteRegister.saveFactoryCalibrateParam();
            Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);
        }

    }

    public String CapValQ(int chIdx,double v,boolean isUpdateUI) {
        int val = -1;
        CabteRegister cabteRegister = CabteRegister.getInstance();
        if(ChannelFactory.isDynamicCh(chIdx)){
            val = cabteRegister.getChCapacitanceHigh(chIdx,cabteRegister.getRatioIdx(Channel.RESISTANCE_1M,v));;
        }
        return ToolsSCPI.getInt(val);
    }
    public void ChCap(double chIndex, double gearIndex, boolean isUpdateUI) {
        double[] param = {chIndex, gearIndex};

        FactorCalibrate.getInstance().begin(CalibrateService.CHCAP_CALIBRATE, param);


    }

    public String ChCapQ(boolean isUpdateUI) {
        String result="";
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {
            boolean b = false;

                b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCAP_CALIBRATE);

            result=ToolsSCPI.getSuccState(b);
        }
        return result;
    }
    public void UpCal( boolean isUpdateUI) {

        FactorCalibrate.getInstance().begin_upPard();

    }

    public String UpCalQ(boolean isUpdateUI) {

        String result;
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {
            CabteRegister cabteRegister = CabteRegister.getInstance();

            boolean b = (cabteRegister.isTopCalibration());
            result=ToolsSCPI.getSuccState(b);
        }

        return result;
    }
    public void DownCal(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin_downPard();
    }

    public String DownCalQ(boolean isUpdateUI) {

        String result;
        if (FactorCalibrate.getInstance().isCalibrating()) {
            result= "caling";
        } else {

            boolean b = false;
            result=ToolsSCPI.getSuccState(b);
        }
        return result;
    }
    public void Trigger_ZeroPoint(boolean isUpdateUI) {

    }

    public void Trigger_ZeroPointQ(boolean isUpdateUI) {

    }

    public void Trigger_AC_ZeroPoint(boolean isUpdateUI) {

    }

    public void Trigger_AC_ZeroPointQ(boolean isUpdateUI) {

    }

    public void Trigger_Coefficient(boolean isUpdateUI) {

    }

    public void Trigger_CoefficientQ(boolean isUpdateUI) {

    }

    public void Trigger_Precise(boolean isUpdateUI) {

    }

    public void Trigger_PreciseQ(boolean isUpdateUI) {

    }

    public void Date_LengthQ(boolean isUpdateUI) {

    }

    public void Date_Get(boolean isUpdateUI) {

    }

    public boolean DatFile_ResetQ(boolean isUpdateUI) {

        CabteRegister.getInstance().rstDefaultVal();
        return true;
    }


    //校准开始
    private void onCalBegin() {
        Logger.i("scpi", "oncalbegin!");
        int cnt = GlobalVar.get().getChannelsCount();
        //关闭数学
        TChan.foreachMath(mathChan ->{
            Command.get().getMath().Display(TChan.toFpgaChNo(mathChan), false, true);
        });
        //关闭ref
        TChan.foreachRef(refChan -> {
            Command.get().getReference().Display(TChan.toFpgaChNo(refChan), false, true);
        });
        //关闭ZOOM
        Command.get().getDisplay().Zoom(false, true);
        //关闭高刷新
        Command.get().getDisplay().High(false, true);
        //打开所有通道
        //所有通道位置归中心0
        for (int i = 0; i < cnt; i++) {
            Command.get().getChannel().Display(i, true, true);
            Command.get().getChannel().setPosition(i, GlobalVar.get().getMainWave().y/2);
            WaveManage.get().setPositionY(i + 1, ((int) GlobalVar.get().getMainWave().y/2));
        }
//        if (是在运行状态)
        if (Scope.getInstance().isRun()) {
//        {
//            if (是AUTO){
            if (Scope.getInstance().isAuto()) {
//                关掉AUTO
                Scope.getInstance().Auto(false);
                Command.get().getFunctionMenu().Auto(true);
            }
//            }
//            if (是单序列){
            if (Scope.getInstance().isSingle()) {
//                关掉单序列
                Scope.getInstance().setSingle(false);
                Command.get().getFunctionMenu().Single(true);
//            }
            }
//        }
        } else {
//        else
//        {
//            运行
            ExternalKeysCommand.get().clickRunStop();
//        }
        }


        //锁屏
        Command.get().getMenu().Lock(true,true);
    }

    private void onCalEnd() {
        int cnt = GlobalVar.get().getChannelsCount();
        //解除屏幕锁
        Command.get().getMenu().Unlock(true);
        //所有通道位置归中心0
        for (int i = 0; i < cnt; i++) {
            Command.get().getChannel().setPosition(i, GlobalVar.get().getMainWave().y/2);
            WaveManage.get().setPositionY(i + 1, ((int) GlobalVar.get().getMainWave().y/2));
        }

        //刷新右上解的状态

    }

    private String onCalStop() {
        //设置校准标记结束
        FactorCalibrate.getInstance().forceEnd();
        return "OKAY";
    }


}
