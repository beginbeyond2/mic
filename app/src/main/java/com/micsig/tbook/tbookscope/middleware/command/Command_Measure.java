package com.micsig.tbook.tbookscope.middleware.command;


import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Auto.FreqCounter;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.SCPIParam;
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Measure {
    private static final String TAG="Command_Measure";

   public class MeasureInfo {
        public static final int FLAG_ClearAll=0x01;

        public int flag=0;

        public int no = -1;
        public int measureType;
        /** 通道号从1开始 */
        public int measureChannel;
        public double measureValue;
        public boolean isValid;
        //延时  相位使用
        public int param1;
        public int param2;
        public int param3;

        public double dparam1;

        public int getNo(){
            return no;
        }
        public MeasureInfo(){}
        public MeasureInfo(int measureType, int measureChannel, int measureValue) {
            this.measureType = measureType;
            this.measureChannel = measureChannel;
            this.measureValue = measureValue;
        }

       @Override
       public String toString() {
           return  "measureType:"+this.measureType
                   +",measureChannel:"+this.measureChannel
                   +",measureValue:"+this.measureValue
                   +",param1:"+this.param1
                   +",flag:"+flag;
       }
   }

    private List<MeasureInfo> listMeasure = new ArrayList<>();

    //测量命令 MEAS
//            new SCPICommandStruct(":MEASure:PERiod?","SCPI_Measure","PeriodQ"),//查询指定通道波形的周期测量值
//            new SCPICommandStruct(":MEASure:FREQuency?","SCPI_Measure","FreQuencyQ"),//查询指定通道波形的频率测量值
//            new SCPICommandStruct(":MEASure:RISetime?","SCPI_Measure","RiseTimeQ"),//查询指定通道波形的上升时间测量值
//            new SCPICommandStruct(":MEASure:FALLtime?","SCPI_Measure","FallTimeQ"),//查询指定通道波形的下降时间测量值
//            new SCPICommandStruct(":MEASure:DELay?","SCPI_Measure","DelayQ"),//查询通道间延迟测量的结果
//            new SCPICommandStruct(":MEASure:PDUTy?","SCPI_Measure","PDutyQ"),//查询指定通道波形的正占空比测量值
//            new SCPICommandStruct(":MEASure:NDUTy?","SCPI_Measure","NDutyQ"),//查询指定通道波形的负占空比测量值
//            new SCPICommandStruct(":MEASure:PWIDth?","SCPI_Measure","PWidthQ"),//查询指定通道波形的正脉宽测量值
//            new SCPICommandStruct(":MEASure:NWIDth?","SCPI_Measure","NWidthQ"),//查询指定通道波形的负脉宽测量值
//            new SCPICommandStruct(":MEASure:BURStwidth?","SCPI_Measure","BurstWidthQ"),//查询指定通道波形的突发脉冲宽度测量值
//            new SCPICommandStruct(":MEASure:ROV?","SCPI_Measure","RovQ"),//查询指定通道波形的正向超调测量值
//            new SCPICommandStruct(":MEASure:FOV?","SCPI_Measure","FovQ"),//查询指定通道波形的负向超调测量值
//            new SCPICommandStruct(":MEASure:PHASe?","SCPI_Measure","PhaseQ"),//查询指定通道间相位差测量的结果
//            new SCPICommandStruct(":MEASure:PKPK?","SCPI_Measure","PkpkQ"),//查询指定通道波形的峰峰值
//            new SCPICommandStruct(":MEASure:AMP?","SCPI_Measure","AmpQ"),//查询指定通道波形的幅度测量值
//            new SCPICommandStruct(":MEASure:HIGH?","SCPI_Measure","HighQ"),//查询指定通道波形的高值
//            new SCPICommandStruct(":MEASure:LOW?","SCPI_Measure","LowQ"),//查询指定通道波形的低值
//            new SCPICommandStruct(":MEASure:MAX?","SCPI_Measure","MaxQ"),//查询指定通道波形的最大值
//            new SCPICommandStruct(":MEASure:MIN?","SCPI_Measure","MinQ"),//查询指定通道波形的最小值
//            new SCPICommandStruct(":MEASure:RMS?","SCPI_Measure","RmsQ"),//查询指定通道波形的均方根值
//            new SCPICommandStruct(":MEASure:CRMS?","SCPI_Measure","CrmsQ"),//查询指定通道波形的周期均方根值
//            new SCPICommandStruct(":MEASure:MEAN?","SCPI_Measure","MeanQ"),//查询指定通道波形的平均值
//            new SCPICommandStruct(":MEASure:CMEan?","SCPI_Measure","CMeanQ"),//查询指定通道波形的周期平均值
//            new SCPICommandStruct(":MEASure:COLVal?","SCPI_Measure","ColValQ"),


 /*   const char * meas_all[] = {
        "PERiod",//0
                "FREQ",
                "RISetime",
                "FALLtime",
                "DELay",
                "PDUTy",//5
                "NDUTy",
                "PWIDth",
                "NWIDth",
                "BURStwidth",
                "ROVershoot",//10
                "FOVershoot",
                "PHASe",
                "PKPK",
                "AMPlitude",
                "HIGH",//15
                "LOW",
                "MAX",
                "MIN",
                "RMS",
                "CRMS",//20
                "MEAN",
                "CMEan",
                "COLVal",
                NULL
    };
const char * delay_edge[] = {
        "FRISe",
                "FFALl",
                "LRISe",
                "LFALl",
                NULL
    };*/


    boolean isChange = false;
    public void changeMeasurePos() {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getValidMeasureList();
        List<MeasureInfo> tempList = new ArrayList<>();
        for (MeasureManage.MeasureItemStruct itemStruct : list) {
            for (MeasureInfo info : listMeasure) {
                if (itemStruct.getChannelId() == info.measureChannel + 1 && itemStruct.getMeasureId() == info.measureType) {
                    tempList.add(info);
                    break;
                }
            }
        }
        listMeasure.clear();
        listMeasure.addAll(tempList);
        isChange = true;
    }

    private String  query(int ch,int measureType){
//        Logger.i(TAG,"listMeasure.size:"+listMeasure.size()+ " ch:"+ch+" type:"+measureType);
//        for(int i=0;i<listMeasure.size();i++){
//            MeasureInfo info=listMeasure.get(i);
//            Logger.i(TAG,"measureinfo.type:"+info.measureType +"  channel:"+info.measureChannel);
//        }
//        ch++;
//        Logger.i("command","chIndex:"+ch);
        for(int i=0;i<listMeasure.size();i++){
            MeasureInfo info=listMeasure.get(i);
            if (ch==info.measureChannel && measureType==info.measureType){
                if (info.isValid) {
                    return ToolsSCPI.getDouble(info.measureValue);
                }else{
                    return "--";
                }
            }
        }
        return "--";
    }

    /**
     *  如： measure:RMS? ch1
     */

    /**
     * 查询指定通道波形的周期测量值
     */
    public String PeriodQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Period);
    }

    /**
     * 查询指定通道波形的频率测量值
     */
    public String FreQuencyQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_Freq);
    }

    /**
     * 查询指定通道波形的上升时间测量值
     */
    public String RiseTimeQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_RiseTime);
    }

    /**
     * 查询指定通道波形的下降时间测量值
     */
    public String FallTimeQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_FallTime);
    }

    /**
     * 查询通道间延迟测量的结果
     */
    public String DelayQ(int chIndex, int chIndex2) {
        return query(chIndex,MeasureManage.IMeasure.MeasureId_Delay);
    }

    /**
     * 查询指定通道波形的正占空比测量值
     */
    public String PDutyQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_DutyAdd);
    }

    /**
     * 查询指定通道波形的负占空比测量值
     */
    public String NDutyQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_DutySub);
    }

    /**
     * 查询指定通道波形的正脉宽测量值
     */
    public String PWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_WidthAdd);
    }

    /**
     * 查询指定通道波形的负脉宽测量值
     */
    public String NWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_WidthSub);
    }

    /**
     * 查询指定通道波形的突发脉冲宽度测量值
     */
    public String BurstWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_BurstW);
    }

    /**
     * 查询指定通道波形的正向超调测量值
     */
    public String RovQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_ROV);
    }

    /**
     * 查询指定通道波形的负向超调测量值
     */
    public String FovQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_FOV);
    }

    /**
     * 查询指定通道间相位差测量的结果
     */
    public String PhaseQ(int chIndex, int chIndex2) {
        return query(chIndex,MeasureManage.IMeasure.MeasureId_Phase);
    }

    /**
     * 查询指定通道波形的峰峰值
     */
    public String PkpkQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_PKPK);
    }

    /**
     * 查询指定通道波形的幅度测量值
     */
    public String AmpQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Amp);
    }

    /**
     * 查询指定通道波形的高值
     */
    public String HighQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_High);
    }

    /**
     * 查询指定通道波形的低值
     */
    public String LowQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Low);
    }

    /**
     * 查询指定通道波形的最大值
     */
    public String MaxQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Max);
    }

    /**
     * 查询指定通道波形的最小值
     */
    public String MinQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Min);
    }

    /**
     * 查询指定通道波形的均方根值
     */
    public String RmsQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_RMS);
    }

    /**
     * 查询指定通道波形的周期均方根值
     */
    public String CrmsQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_CRMS);
    }

    /**
     * 查询指定通道波形的平均值
     */
    public String MeanQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Mean);
    }

    /**
     * 查询指定通道波形的周期平均值
     */
    public String CMeanQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_CMean);
    }

    public String ACRMSQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_ACRMS);
    }
    public String PRATEQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_PostitiveRate);
    }
    public String NRATEQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_NegativeRate);
    }
    /**
     * 查询指定列上的值 ：列单位为像素
     */
    public String ColValQ(int chIndex) {
        return query(chIndex, MeasureManage.IMeasure.MeasureId_ColValQ);
    }

    public String TValueQ(int chindex){
        return query(chindex, MeasureManage.IMeasure.MeasureId_TVALUE);
    }

//     new SCPICommandStruct(":MEASure:AREa?","SCPI_Measure","AreaQ"),//查询指定通道波形的面积
//            new SCPICommandStruct(":MEASure:CARea?","SCPI_Measure","CareaQ"),//查询指定通道波形的周期面积
//            new SCPICommandStruct(":MEASure:CLEar","SCPI_Measure","Clear"),//清除打开的测量项中的任一项或所有项
//            new SCPICommandStruct(":MEASure:OPEN","SCPI_Measure","Open"),//打开测量项
//            new SCPICommandStruct(":MEASure:CLOSe","SCPI_Measure","Close"),//关闭测量项
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure","Statistic_Display"),//打开或关闭统计功能
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure","Statistic_DisplayQ"),//查询统计功能打开或关闭
//            new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure","Statistic_Reset"),//清楚历史统计数据并重新统计
//            new SCPICommandStruct(":MEASure:ADISplay","SCPI_Measure","Adislay"),//打开或关闭全部测量
//            new SCPICommandStruct(":MEASure:ADISplay?","SCPI_Measure","AdisplayQ"),//查询全部测量打开或关闭
//            new SCPICommandStruct(":MEASure:SCOPe","SCPI_Measure","Scope"),//设置测量范围
//            new SCPICommandStruct(":MEASure:SCOPe?","SCPI_Measure","ScopeQ"),//查询测量范围
//            new SCPICommandStruct(":MEASure:COUNter:SOURce","SCPI_Measure","Source"),//设置源
//            new SCPICommandStruct(":MEASure:COUNter:SOURce?","SCPI_Measure","SourceQ"),//设置源
//           new SCPICommandStruct(":MEASure:COUNter:MODE","SCPI_Measure","Counter_Mode"),
//            new SCPICommandStruct(":MEASure:COUNter:MODE?","SCPI_Measure","Counter_ModeQ"),
//            new SCPICommandStruct(":MEASure:COUNter:VALue?","SCPI_Measure","Counter_ValueQ"),//查询频率计结果
//            new SCPICommandStruct(":MEASure:ITEM","SCPI_Measure","Item"),//设置信源
//            new SCPICommandStruct(":MEASure:ITEM?","SCPI_Measure","ItemQ"),//设置信源


    /**
     * 查询指定通道波形的面积
     */
    public double AreaQ() {
        return 0;
    }

    /**
     * 查询指定通道波形的周期面积
     */
    public double CareaQ() {
        return 0;
    }

    public void setListMeasureNo(int channel,int measureIndex,int no){
        Log.d(TAG, "setListMeasureNo() called with: channel = [" + channel + "], measureIndex = [" + measureIndex + "], no = [" + no + "]");
        for (MeasureInfo info : listMeasure) {
            if (info.measureChannel + 1 == channel
                    && info.measureType == measureIndex) {
                info.no = no;
                break;
            }
        }
    }

    public void setListMeasureValue(int channel, int measureIndex, double value, boolean isValid) {
        for (MeasureInfo info : listMeasure) {
            if (info.measureChannel + 1 == channel && info.measureType == measureIndex) {
                int index = listMeasure.indexOf(info);
                info.measureValue = value;
                info.isValid = isValid;
                listMeasure.set(index, info);
                break;
            }
        }
    }
    public void setListMeasureValueEx(int ch,int type,double value, boolean isValid){
        int index=-1;
        for(int i=0;i<listMeasure.size();i++){
            MeasureInfo info=listMeasure.get(i);
            if (info.measureType==type && info.measureChannel==ch){
                index=i;
                break;
            }
        }
        if (index==-1)return;
        MeasureInfo info=listMeasure.get(index);
        info.measureValue= value;
        info.isValid=isValid;
        listMeasure.set(index,info);

    }

    public void ClearAllItem(boolean isUpdateUI){
        listMeasure.clear();
        postMeasureCountChange(false + CommandMsgToUI.PARAM_SPLIT + listMeasure.size());
        if (isUpdateUI){
            MeasureInfo info=new MeasureInfo();
            info.flag= MeasureInfo.FLAG_ClearAll;
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,info);
        }
    }

    /**
     * 清除打开的测量项中的任一项或所有项
     */
    public void Clear(int index, boolean isUpdateUI) {
        if (isChange && !isUpdateUI) {
            isChange = false;
            return;
        }
        if (index>= listMeasure.size()){ return;}
        MeasureInfo info=listMeasure.get(index);
        listMeasure.remove(info);
        postMeasureCountChange(false + CommandMsgToUI.PARAM_SPLIT + listMeasure.size());
        if (isUpdateUI){
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,info);
        }
    }


    private void postMeasureCountChange(String str) {
        RxBus.getInstance().post(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT, str);
    }




    private boolean isExistMeasureInfo(int measureType, int measureChannel, int param3, int param4, int param5) {
        return isExistMeasureInfo(measureType,measureChannel,param3,param4,param5,0);
    }
    private boolean isExistMeasureInfo(int measureType, int measureChannel, int param3, int param4, int param5,double param6){
        for (int i=0;i<listMeasure.size();i++){
            MeasureInfo info= listMeasure.get(i);
            switch (measureType){
                case 4:
                    if (info.measureType==measureType && info.measureChannel==measureChannel && info.param1==param3 && info.param2==param4 && info.param3==param5){
                        return true;
                    }
                    break;
                case 12:
                case MeasureManage.IMeasure.MeasureId_ColValQ:
                    if (info.measureType==measureType && info.measureChannel==measureChannel && info.param1==param3){
                        return true;
                    }
                    break;
                case MeasureManage.IMeasure.MeasureId_TVALUE: {
                    if (info.measureType==measureType
                            && info.measureChannel==measureChannel
                            && info.param1 == param3 &&
                            info.dparam1 == param6){
                        return true;
                    }
                }
                break;
                default:
                    if (info.measureType==measureType && info.measureChannel==measureChannel){
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
    /**
     * 打开测量项
     * 如：measure:open RMS,ch1
     */
    public boolean Open(int measureType, int measureChannel, int param3, int param4, int param5, boolean isUpdateUI){
        return Open(-1,measureType, measureChannel, param3, param4, param5,0, isUpdateUI);
    }
    public boolean Open(int no,int measureType, int measureChannel, int param3, int param4, int param5,double param6, boolean isUpdateUI) {

        //Log.d("Tag.Debug", String.format("Command_Measure.Open: [measureType:%d, measureChannel:%d]",measureType,measureChannel ));
        boolean flag = false;
        MeasureInfo info=null;
        if (isExistMeasureInfo(measureType, measureChannel, param3, param4, param5)) return true;
        switch (measureType) {
            case 4: { //延时
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {       break;        }
                info = new MeasureInfo(measureType, measureChannel, 0);
                info.param1 = param3;
                info.param2 = param4;
                info.param3 = param5;
                listMeasure.add(info);
                flag = true;
            }
            break;
            case 12: { //相位
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                }
                info = new MeasureInfo(measureType, measureChannel, 0);
                info.param1 = param3;
                listMeasure.add(info);
                flag = true;
            }
            break;
            case MeasureManage.IMeasure.MeasureId_ColValQ: { //colvalQ
//                Logger.i(TAG,"open colvalQ type:"+measureType+ " ,chan:"+measureChannel+" ,param3:"+param3);
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                }
                info = new MeasureInfo(measureType, measureChannel, 0);
                info.param1 = param3;
                listMeasure.add(info);
                flag = true;
//                Logger.i(TAG,"add");
            }
            break;
            case MeasureManage.IMeasure.MeasureId_TVALUE:{
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                }
                MeasureInfo xinfo = null;
                info = null;
                for (int i=0;i<listMeasure.size();i++){
                    xinfo= listMeasure.get(i);
                    if(xinfo.measureType == measureType
                        && xinfo.measureChannel == measureChannel){
                        info = xinfo;
                        break;
                    }
                }
                if (info == null) {
                    info = new MeasureInfo(measureType, measureChannel, 0);
                    info.param1 = param3;
                    info.param2 = param4;
                    info.dparam1 = param6;
                    listMeasure.add(info);
                } else {
                    info.isValid = false;
                    info.param1 = param3;
                    info.param2 = param4;
                    info.dparam1 = param6;
                }

                flag = true;
            }
            break;

            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) { break; }
                info = new MeasureInfo(measureType, measureChannel, 0);
                listMeasure.add(info);
                flag = true;
                break;
        }
        if(info != null && no >= 0  && no < GlobalVar.get().getMeasureItemCount()) {
            info.no = no;
        }
//        Logger.i(TAG,"isUpdateUI:"+isUpdateUI+ ",flag:"+flag);
        if (isUpdateUI && flag) {
            Logger.i(TAG,"info:"+info.toString());

            RxBus.getInstance().post(RxEnum.COMMANDMEASUREOPEN_TO_UI,info);
        }

        if(flag) {
            postMeasureCountChange(true + CommandMsgToUI.PARAM_SPLIT + listMeasure.size());
        }
        return flag;
    }

    /**
     * 关闭测量项
     */
    public void Close(int type, int Ch, boolean isUpdateUI) {
        MeasureInfo temInfo=null;
        boolean flag=false;
        for (int i=0;i<listMeasure.size();i++){
            MeasureInfo info=listMeasure.get(i);
            //Logger.i(Command.TAG,"info:"+info.toString()+";    CH:"+Ch+",type:"+type);
            if (info.measureType==type && (info.measureChannel)== Ch){
                temInfo=info;
                listMeasure.remove(info);
                flag=true;
                break;
            }
        }
        if(flag) {

        }
        //Logger.i(Command.TAG,"flag:"+flag);
        if (isUpdateUI && flag) {
            //Logger.i(Command.TAG,"clear flag:"+temInfo.flag);
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,temInfo);
        }
    }

    /**
     * 打开或关闭统计功能
     */
    public void Statistic_Display(int index, boolean isUpdateUI) {
    }

    /**
     * 查询统计功能打开或关闭
     */
    public double Statistic_DisplayQ() {
        return 0;
    }

    /**
     * 清楚历史统计数据并重新统计
     */
    public void Statistic_Reset(int index, boolean isUpdateUI) {
    }


    private boolean AllMeasureDisplay=false;
    /**
     * 打开或关闭全部测量
     */
    public void Adislay(boolean allMeasureDisplay, boolean isUpdateUI) {
        this.AllMeasureDisplay=allMeasureDisplay;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_ALL_DISPLAY);
            msgToUI.setParam(String.valueOf(allMeasureDisplay));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }

    }

    /**
     * 查询全部测量打开或关闭
     */
    public boolean AdisplayQ() {
        return this.AllMeasureDisplay;
    }

    /**
     * 设置测量范围
     */
    public void Scope(int index, boolean isUpdateUI) {
    }

    /**
     * 查询测量范围
     */
    public double ScopeQ() {
        return 0;
    }


    private int countSource;
    /**
     * 计数器设置源
     */
    public void Counter_Source(int index, boolean isUpdateUI) {
        this.countSource=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_COUNT_SOURCE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置源
     */
    public int Counter_SourceQ() {
        if (FreqCounter.getInstance().isFreqCounterEnable()){
            return TChan.toUiChNo(FreqCounter.getInstance().getChIdx());
        }
        return 0;
    }

    private int countMode;
    public  void Counter_Mode(int index,boolean isUpdateUI){
        this.countMode=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_COUNT_MODE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  int Counter_ModeQ(){
        return countMode;
    }

    /**
     * 查询频率计结果
     */
    public String Counter_ValueQ() {
        String hz="";
        if (FreqCounter.getInstance().IsVaild()) {
            hz = TBookUtil.getHzFromHz(FreqCounter.getInstance().getFreqVal());
        };
        return hz;
    }

    /**
     * 设置信源
     */
    public void Item(int index, boolean isUpdateUI) {
    }

    /**
     * 设置信源
     */
    public double ItemQ() {
        return 0;
    }

    public void factoryCalibration(){
        if(MeasureManage.getInstance().getMeasureItem().getMeasureRowCount() != 1){
            ClearAllItem(true);
            ChannelFactory.forEachCh(channel -> {
                Open(MeasureManage.IMeasure.MeasureId_Amp, channel.getChId(), 0, 0, 0, true);
            });
        }
    }

    public String ListQ() {
        StringBuilder stringBuilder = new StringBuilder();
        listMeasure.stream().filter(measureInfo -> measureInfo.getNo() >= 0)
                .sorted(Comparator.comparing(MeasureInfo::getNo))
                .forEach( m-> {
                    if(stringBuilder.length() > 0) stringBuilder.append(",");
                    stringBuilder.append(m.getNo() + 1);
                });
        if(stringBuilder.length() > 0) {
            return stringBuilder.toString();
        }else {
            return "--";
        }
    }

final static int [] MEASURE_ID_ARRAY ={
        MeasureManage.IMeasure.MeasureId_Period,
        MeasureManage.IMeasure.MeasureId_Freq,
        MeasureManage.IMeasure.MeasureId_RiseTime,
        MeasureManage.IMeasure.MeasureId_FallTime,
        MeasureManage.IMeasure.MeasureId_DutyAdd,
        MeasureManage.IMeasure.MeasureId_DutySub,
        MeasureManage.IMeasure.MeasureId_WidthAdd,
        MeasureManage.IMeasure.MeasureId_WidthSub,
        MeasureManage.IMeasure.MeasureId_BurstW,
        MeasureManage.IMeasure.MeasureId_ROV,
        MeasureManage.IMeasure.MeasureId_FOV,
        MeasureManage.IMeasure.MeasureId_PKPK,
        MeasureManage.IMeasure.MeasureId_Amp,
        MeasureManage.IMeasure.MeasureId_High,
        MeasureManage.IMeasure.MeasureId_Low,
        MeasureManage.IMeasure.MeasureId_Max,
        MeasureManage.IMeasure.MeasureId_Min,
        MeasureManage.IMeasure.MeasureId_RMS,
        MeasureManage.IMeasure.MeasureId_CRMS,
        MeasureManage.IMeasure.MeasureId_Mean,
        MeasureManage.IMeasure.MeasureId_CMean,
        MeasureManage.IMeasure.MeasureId_ACRMS,
        MeasureManage.IMeasure.MeasureId_PostitiveRate,
        MeasureManage.IMeasure.MeasureId_NegativeRate,
};
    public void AddNew(int measureNo){
        if(measureNo>=0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() < GlobalVar.get().getMeasureItemCount()) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if (!opt.isPresent()) {
                for(int i=ChannelFactory.CH1;i<ChannelFactory.REF_MAX;i++){
                    if(ChannelFactory.isChOpen(i)){
                        for(int idx:MEASURE_ID_ARRAY){
                            int finalI = i;
                            boolean exists = listMeasure.stream().anyMatch(measureInfo -> measureInfo.measureType == idx
                                    && measureInfo.measureChannel == finalI);
                            if(!exists){
                                Open(measureNo,idx,i,0,0,0,0,true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void Delete(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            opt.ifPresent(measureInfo -> Close(measureInfo.measureType, measureInfo.measureChannel, true));
        }
    }

    public void XType(SCPIParam param){
        Open(param.iParam1,param.iParam2,param.iParam3,param.iParam4,param.iParam5,param.iParam6,param.dParam1,true);
    }

    final static String [] meas_all = {
        "PERiod",//0
        "FREQ",
        "RISetime",
        "FALLtime",
        "DELay",
        "PDUTy",//5
        "NDUTy",
        "PWIDth",
        "NWIDth",
        "BURStw",
        "ROVershoot",//10
        "FOVershoot",
        "PHASe",
        "PKPK",
        "AMPlitude",
        "HIGH",//15
        "LOW",
        "MAX",
        "MIN",
        "RMS",
        "CRMS",//20
        "MEAN",
        "CMEan",
        "ACRMs",
        "PRATe",
        "NRATe",//25
        "TVALue"
    };
    public String XTypeQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.measureType >= 0
                        && measureInfo.measureType < meas_all.length){
                    return meas_all[measureInfo.measureType];
                }
            }
        }
        return "--";
    }

    public void XSOURce1(int measureNo,int s){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){
                Open(measureNo,measureInfo.measureType,s,
                        measureInfo.param1,measureInfo.param2,measureInfo.param2,0,true);
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Phase){
                Open(measureNo,measureInfo.measureType,s,
                        measureInfo.param1,measureInfo.param2,0,0,true);
            }
        }
    }
    public String XSOURce1Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                return ToolsSCPI.getChAll(measureInfo.measureChannel);
            }
        }
        return "--";
    }

    public void XSOURce2(int measureNo,int s){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        s,measureInfo.param2,measureInfo.param2,0,true);
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Phase){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        s,measureInfo.param2,0,0,true);
            }
        }
    }
    public String XSOURce2Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                switch (measureInfo.measureType){
                    case MeasureManage.IMeasure.MeasureId_Delay:
                    case MeasureManage.IMeasure.MeasureId_Phase:
                        return ToolsSCPI.getChAll(measureInfo.param1);
                }
            }
        }
        return "--";
    }

    public String XVALueQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.isValid){
                    return ToolsSCPI.getDouble(measureInfo.measureValue);
                }
            }
        }
        return "--";
    }

    public String XUnitQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                switch (measureInfo.measureType) {
                    case MeasureManage.IMeasure.MeasureId_Freq:
                        return "Hz";
                    case MeasureManage.IMeasure.MeasureId_DutyAdd:
                    case MeasureManage.IMeasure.MeasureId_DutySub:
                    case MeasureManage.IMeasure.MeasureId_ROV:
                    case MeasureManage.IMeasure.MeasureId_FOV:
                        //%不显示m、k、M等前缀，保留2位小数
                        return "%";
                    case MeasureManage.IMeasure.MeasureId_Phase:
                        return "°";
                    case MeasureManage.IMeasure.MeasureId_Period:
                    case MeasureManage.IMeasure.MeasureId_RiseTime:
                    case MeasureManage.IMeasure.MeasureId_FallTime:
                    case MeasureManage.IMeasure.MeasureId_Delay:
                    case MeasureManage.IMeasure.MeasureId_WidthAdd:
                    case MeasureManage.IMeasure.MeasureId_WidthSub:
                    case MeasureManage.IMeasure.MeasureId_BurstW:
                    case MeasureManage.IMeasure.MeasureId_TVALUE:
                        return "s";
                    case MeasureManage.IMeasure.MeasureId_PKPK:
                    case MeasureManage.IMeasure.MeasureId_Amp:
                    case MeasureManage.IMeasure.MeasureId_High:
                    case MeasureManage.IMeasure.MeasureId_Low:
                    case MeasureManage.IMeasure.MeasureId_Max:
                    case MeasureManage.IMeasure.MeasureId_Min:
                    case MeasureManage.IMeasure.MeasureId_RMS:
                    case MeasureManage.IMeasure.MeasureId_CRMS:
                    case MeasureManage.IMeasure.MeasureId_Mean:
                    case MeasureManage.IMeasure.MeasureId_CMean:
                    case MeasureManage.IMeasure.MeasureId_ACRMS:
                        return ChannelFactory.getProbeType(measureInfo.measureChannel);
                    case MeasureManage.IMeasure.MeasureId_PostitiveRate:
                    case MeasureManage.IMeasure.MeasureId_NegativeRate:
                        return ChannelFactory.getProbeType(measureInfo.measureChannel) + "/s";
                }
            }
        }
        return "--";
    }
    public String XValidQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                return ToolsSCPI.getOpenState(measureInfo.isValid);
            }
        }
        return "--";
    }
    public void XEdge1(int measureNo,int e){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        measureInfo.param1,e,measureInfo.param3,0,true);
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        e,measureInfo.param2,0,measureInfo.dparam1,true);
            }
        }
    }
    final static String [] delay_edge = {
        "FRISe","FFALl","LRISe","LFALl"
    };
    public String XEdge1Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay
                    && measureInfo.param2 >= 0
                        && measureInfo.param2 < delay_edge.length){
                    return delay_edge[measureInfo.param2];
                }
            }
        }
        return "--";
    }
    public void XEdge2(int measureNo,int e){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,measureInfo.param2,e,0,true);
            }
        }
    }
    public String XEdge2Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay
                        && measureInfo.param3 >= 0
                        && measureInfo.param3 < delay_edge.length){
                    return delay_edge[measureInfo.param3];
                }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){
                    return ToolsSCPI.getInt(measureInfo.param1);
                }
            }
        }
        return "--";
    }
    public void XCURSor(int measureNo,int x){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,x,0,measureInfo.dparam1,true);
            }
        }
    }
    final static String [] meas_cursor={
        "NONE","X1","X2"
    };
    public String XCURSorQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount()
                && listMeasure.size() > 0) {
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE
                        && measureInfo.param2 >= 0
                        && measureInfo.param2 < meas_cursor.length){
                    return meas_cursor[measureInfo.param2];
                }
            }
        }
        return "--";
    }
    public void XVVLue(int measureNo,double val){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
        if(opt.isPresent()){
            MeasureInfo measureInfo = opt.get();
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,measureInfo.param2,0,val,true);
            }
        }
    }
    public String XVVLueQ(int measureNo){

            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst();
            if(opt.isPresent()){
                MeasureInfo measureInfo = opt.get();
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){
                    return ToolsSCPI.getDouble(measureInfo.dparam1);
                }
            }

        return "--";
    }
}
