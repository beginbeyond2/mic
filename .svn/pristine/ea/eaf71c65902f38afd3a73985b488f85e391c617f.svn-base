package com.micsig.tbook.tbookscope.middleware.command;


import android.util.Log;

import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

import java.util.HashMap;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Sample {
//    //采样命令 SAMP
//            new SCPICommandStruct(":ACQuire:TYPE","SCPI_Sample","Type(){}
//            new SCPICommandStruct(":ACQuire:TYPE?","SCPI_Sample","TypeQ(){}
//            new SCPICommandStruct(":ACQuire:MEAN","SCPI_Sample","Mean(){}
//            new SCPICommandStruct(":ACQuire:MEAN?","SCPI_Sample","MeanQ(){}
//            new SCPICommandStruct(":ACQuire:ENVelop","SCPI_Sample","Envelop(){}
//            new SCPICommandStruct(":ACQuire:ENVelop?","SCPI_Sample","EnvelopQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented","SCPI_Sample","SegMented(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:NO?","SCPI_Sample","SegmentedNoQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:QTY","SCPI_Sample","SegmentedQTY(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:QTY?","SCPI_Sample","SegmentedQTYQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:DISType","SCPI_Sample","SegmentedDisplayType(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:DISType?","SCPI_Sample","SegmentedDisplayTypeQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:ORDer","SCPI_Sample","SegmentedOrder(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:ORDer?","SCPI_Sample","SegmentedOrderQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY","SCPI_Sample","SegmentedPlay(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:STOP","SCPI_Sample","SegmentedStop(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA1","SCPI_Sample","SegmentedFra1(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA1?","SCPI_Sample","SegmentedFra1Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA2","SCPI_Sample","SegmentedFra2(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA2?","SCPI_Sample","SegmentedFra2Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA3","SCPI_Sample","SegmentedFra3(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA3?","SCPI_Sample","SegmentedFra3Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED","SCPI_Sample","SegmentedPlaySpeed(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED?","SCPI_Sample","SegMentedPlaySpeedQ(){}
//            new SCPICommandStruct(":ACQuire:SRATe?","SCPI_Sample","SrateQ(){}
//            new SCPICommandStruct(":ACQuire:MDEPth","SCPI_Sample","Mdepth"),//设置当前存储尝试
//            new SCPICommandStruct(":ACQuire:MDEPth?","SCPI_Sample","MdepthQ(){}

    /**
     * const char * samp_type[] = {
     * "NORMal",
     * "MEAN",
     * "ENVelop",
     * "PEAK",
     * NULL
     * };
     */
    private int type;
    /**
     * const char * samp_mean_num[] = {
     * "2","4", "8", "16", "32", "64", "128", "256"
     * , NULL
     * };
     */
    private int meanIndex;
    /**
     * const char * samp_env_num[] = {
     * "2","4", "8", "16", "32", "64", "128", "256", "inf"
     * , NULL
     * };
     */
    private int envelopIndex;
    /**
     * 是否开启分段存储
     */
    private boolean isOpenSegMent;

    /**
     * 分段存储总段数
     */
    private int segmentedCount;

    /**
     * true:单帧  false:拟合
     */
    private int segmentedDisplayType;
    /**
     * true:顺序  false:倒序
     */
    private int segmentedOrder;
    private int frame1;
    private int frame2;
    private int frame3;
    private int playSpeed;

    /**
     * 设置采样方式
     */
    public void Type(int type, boolean isUpdateUI) {
//        if (this.type == type) return;
        this.type = type;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_TYPE);
            msgToUI.setParam(String.valueOf(type));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询采样方式
     * /**
     * const char * samp_type[] = {
     * "NORMal",
     * "PEAK",
     * "MEAN",
     * "ENVelop",
     * NULL
     * };
     */
    public int TypeQ() {
        return type;
    }

    /**
     * 设置平均采样次数。所设置的值为2的整数倍数。
     */
    public void Mean(int meanIndex, boolean isUpdateUI) {
//        if (this.meanIndex == meanIndex) return;
        this.meanIndex = meanIndex;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_MEAN);
            msgToUI.setParam(String.valueOf(meanIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询平均采样次数
     */
    public int MeanQ() {
        return meanIndex;
    }

    /**
     * 设置包络采样次数。所设置的值为2的整数倍数或无穷
     */
    public void Envelop(int envelopIndex, boolean isUpdateUI) {
        if (this.envelopIndex == envelopIndex) return;
        this.envelopIndex = envelopIndex;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_ENVELOP);
            msgToUI.setParam(String.valueOf(envelopIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询包络采样次数
     */
    public int EnvelopQ() {
        return envelopIndex;
    }

    /**
     * 设置分段存储的段数
     */
    public void SegMented(boolean isOpen, boolean isUpdateUI) {
        this.isOpenSegMent=isOpen;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_IsOpenSegMent);
            msgToUI.setParam(String.valueOf(isOpenSegMent));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean SegMentedQ(){
        return this.isOpenSegMent;
    }
    //查询已存在段数
     public int SegmentedNoQ(){
         SegmentSample segmentSample = SegmentSample.getInstance();
         int count=0;
         if (Scope.getInstance().isRun()) {
             int nums = segmentSample.getSegmentNums();
             if (nums > segmentSample.getMaxSegmentNums())
                 nums = segmentSample.getMaxSegmentNums();
//             strTxt = "" + segmentSample.getSegmentFrames() + "/" + nums;
             count=segmentSample.getSegmentFrames();
         } else {
             Scope scope = Scope.getInstance();
             int nums = scope.getSegmentFrameNums();
             int val = (scope.getSegmentFrameNo() + 1);
             if (SegmentSample.getInstance().getSegmentDisplayType()==SegmentSample.SEGMENT_DISPLAY_FITTING){
                 val = scope.getSegmentFrameNo() ;
             }
             if (val>nums) val=nums;
             if (nums <= 0) val = 0;
//             strTxt = "" + val + "/" + nums;
             count=nums;
         }
         return count;
     }
    //设置段数
     public void SegmentedQTY(int count,boolean isUpdateUI){
        this.segmentedCount=count;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedQTY);
            msgToUI.setParam(String.valueOf(count));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }

     }
    //查询段数
     public int SegmentedQTYQ(){
        return this.segmentedCount;
     }
    public int SegmentedMaxQ(){
        return SegmentSample.getInstance().getMaxSegmentNums();
    }
    //设置显示类型
    public void SegmentedDisplayType(int displayIndex,boolean isUpdateUI){
        this.segmentedDisplayType=displayIndex;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedDisplayType);
            msgToUI.setParam(String.valueOf(displayIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    //查询显示类型
    public int SegmentedDisplayTypeQ(){
        return this.segmentedDisplayType;
    }

    public void SegmentedOrder(int orderIndex,boolean isUpdateUI){
        this.segmentedOrder=orderIndex;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedOrder);
            msgToUI.setParam(String.valueOf(orderIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int SegmentedOrderQ(){
        return this.segmentedOrder;
    }
    public void SegmentedPlay(boolean isUpdateUI){
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedPlay);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public void SegmentedStop(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedStop);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public void SegmentedFra1(int frame,boolean isUpdateUI){
        int max= Scope.getInstance().getSegmentFrameNums();
        int min=1;
        if (frame<min) frame=min;
        if (frame>max) frame=max;
        this.frame1=frame;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra1);
            msgToUI.setParam(String.valueOf(frame));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int SegmentedFra1Q(){
        return this.frame1;
    }
    public void SegmentedFra2(int frame,boolean isUpdateUI){
        this.frame2=frame;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra2);
            msgToUI.setParam(String.valueOf(frame));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int SegmentedFra2Q(){
        return this.frame2;
    }
    public void SegmentedFra3(int frame,boolean isUpdateUI){
        this.frame3=frame;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra3);
            msgToUI.setParam(String.valueOf(frame));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int SegmentedFra3Q(){
        return this.frame3;
    }
    public void SegmentedPlaySpeed(int speed,boolean isUpdateUI){
        this.playSpeed=speed;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedPlaySpeed);
            msgToUI.setParam(String.valueOf(speed));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int SegmentedPlaySpeedQ(){
        return this.playSpeed;
    }

    /**
     * 查询当前的采样率
     */
    public double SrateQ() {
        double dx=0;
        IChannel channel = ChannelFactory.getInstance().getWaveChannel();
        if (channel != null) {
            dx = channel.getSampleRate2display();
        }
        return dx;
    }

    private int mDepth=0;
    private HashMap<Integer,Integer> mapDepth=new HashMap();
    public  void Mdepth(String  memdepth,boolean isUpdateUI){
        Log.d("xxxxxx", "Mdepth() called with: memdepth = [" + memdepth + "], isUpdateUI = [" + isUpdateUI + "]");

        int index = MemDepthFactory.getMemDepth().memDepth2menuIdx(memdepth.trim());
        if(index >= 0){
            Mdepth(index,isUpdateUI);
        }
    }
    public  void Mdepth(int  index,boolean isUpdateUI){
//        if (index >= 6) index -= 5;
        if (mapDepth.size()==0){
            mapDepth.put(0,0);
            mapDepth.put(1,1);
            mapDepth.put(2,2);
            mapDepth.put(3,3);
            mapDepth.put(4,4);
            mapDepth.put(5,5);
            mapDepth.put(6,1);
            mapDepth.put(7,2);
            mapDepth.put(8,3);
            mapDepth.put(9,4);
            mapDepth.put(10,5);
            mapDepth.put(11,1);
            mapDepth.put(12,2);
            mapDepth.put(13,3);
            mapDepth.put(14,4);
            mapDepth.put(15,5);
        }
        index=mapDepth.get(index);
        this.mDepth=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_LENGTH);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    /**
     * 查询示波器当前存储深度
     */
    public int MdepthQ() {
       if (mDepth==0){
           return mDepth;
       }else {
           return MemDepthFactory.getSampleMemDepth();
       }
    }
}
