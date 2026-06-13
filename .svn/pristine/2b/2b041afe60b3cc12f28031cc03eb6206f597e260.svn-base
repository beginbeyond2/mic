package com.micsig.tbook.tbookscope.middleware.command;

import android.util.Log;

import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liwb on 2018/1/17.
 */

public class Command_Channel {
//有plus指令的没有实现完全。

    //    //通道命令 CHAN 1.0协议
//    new SCPICommandStruct(":CHANnel:DISPlay","SCPI_Channel","Display"),//通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:DISPlay?","SCPI_Channel","DisplayQ"),//查询通道的打开或关闭
//    new SCPICommandStruct(":CHANnel:INVerse","SCPI_Channel","inverse"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVerse?","SCPI_Channel","InverseQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert","SCPI_Channel","Invert"),//打开或关闭通道的反相显示
//    new SCPICommandStruct(":CHANnel:INVert?","SCPI_Channel","InvertQ"),//查询通道的反相显示
//    new SCPICommandStruct(":CHANnel:BAND","SCPI_Channel","Band"),//设置通道的带宽限制
//    new SCPICommandStruct(":CHANnel:BAND?","SCPI_Channel","BandQ"),//查询通道的带宽限制
//    new SCPICommandStruct(":CHANnel:PRTY","SCPI_Channel","prty"),//设置通道的探针类型
//    new SCPICommandStruct(":CHANnel:PRTY?","SCPI_Channel","PrtyQ"),//查询通道的探针类型
//    new SCPICommandStruct(":CHANnel:PROBe","SCPI_Channel","Probe"),//设置探头的衰减比
//    new SCPICommandStruct(":CHANnel:PROBe?","SCPI_Channel","ProbeQ"),//查询探头的衰减比
//    new SCPICommandStruct(":CHANnel:COUPle","SCPI_Channel","Couple"),//设置通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:COUPle?","SCPI_Channel","CoupleQ"),//查询通道输入耦合方式
//    new SCPICommandStruct(":CHANnel:INPutres","SCPI_Channel","Inputres"),//设置通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:INPutres?","SCPI_Channel","InputresQ"),//查询通道的输入阻抗
//    new SCPICommandStruct(":CHANnel:EXTent","SCPI_Channel","extent"),//设置指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:PLUS:EXTent","SCPI_Channel","Plus_Extent"),
//    new SCPICommandStruct(":CHANnel:EXTent?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
//    new SCPICommandStruct(":CHANnel:POSition","SCPI_Channel","Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:PLUS:POSition","SCPI_Channel","Plus_Position"),//设置指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:POSition?","SCPI_Channel","PositionQ"),//查询指定通道波形显示的垂直偏移
//    new SCPICommandStruct(":CHANnel:VERNier","SCPI_Channel","Vernier"),//打开或关闭指定通道的垂直档位微调功能
//    new SCPICommandStruct(":CHANnel:VERNier?","SCPI_Channel","VernierQ"),//查询指定通道的垂直档位微调功能的打开或关闭
//    new SCPICommandStruct(":CHANnel:VREF","SCPI_Channel","Vref"),//设置垂直展开基准
//    new SCPICommandStruct(":CHANnel:VREF?","SCPI_Channel","VrefQ"),//查询垂直展开基准
//    new SCPICommandStruct(":CHANnel:CURRent","SCPI_Channel","Current"),//设置垂直展开基准
//    new SCPICommandStruct(":CHANnel:CURRent?","SCPI_Channel","CurrentQ"),//查询垂直展开基准

    //1.1 协议
//    new SCPICommandStruct(":CHANnel#:DISPlay","SCPI_Channel","Display"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse","SCPI_Channel","inverse"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert","SCPI_Channel","Invert"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY","SCPI_Channel","prty"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe","SCPI_Channel","Probe"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle","SCPI_Channel","Couple"),  //设置通道输入耦合方式
//     1.1 新增       new SCPICommandStruct(":CHANnel#:SCALe","SCPI_Channel","Scale"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition","SCPI_Channel","Position"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier","SCPI_Channel","Vernier"), //打开或关闭指定通道的垂直档位微调功能
//     1.1 新增       new SCPICommandStruct(":CHANnel#:PC","SCPI_Channel","pc"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres","SCPI_Channel","Inputres"),   //设置阻抗
//
//            new SCPICommandStruct(":CHANnel#:DISPlay?","SCPI_Channel","DisplayQ"), //通道打开或关闭
//            new SCPICommandStruct(":CHANnel#:INVerse?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:INVert?","SCPI_Channel","InvertQ"), //打开或关闭通道的反相显示
//            new SCPICommandStruct(":CHANnel#:PRTY?","SCPI_Channel","PrtyQ"), //设置通道的探针类型
//            new SCPICommandStruct(":CHANnel#:PROBe?","SCPI_Channel","ProbeQ"),  //设置探头的衰减比
//            new SCPICommandStruct(":CHANnel#:COUPle?","SCPI_Channel","CoupleQ"),  //设置通道输入耦合方式
//      1.1新增      new SCPICommandStruct(":CHANnel#:SCALe?","SCPI_Channel","ScaleQ"),  //设置通道波形显示的垂直档位
//            new SCPICommandStruct(":CHANnel#:POSition?","SCPI_Channel","PositionQ"),  //设置通道波形显示的垂直偏移
//            new SCPICommandStruct(":CHANnel#:VERNier?","SCPI_Channel","VernierQ"), //打开或关闭指定通道的垂直档位微调功能
//       1.1新增     new SCPICommandStruct(":CHANnel#:PC?","SCPI_Channel","PCQ"),   //获取通道波形到上位机
//            new SCPICommandStruct(":CHANnel#:INPutres?","SCPI_Channel","InputresQ"),   //查询阻抗状态


   public class ChannelAttribute {
        /**
         * 显示
         */
        boolean display = false;
        /**
         * 反向
         */
        boolean inverse = false;
        /**
         * 通道的探针类型
         */
        int prty = 0;
        /**
         * 衰减比
         */
        double prob = 0;
        /**
         * 耦合方式
         */
        int coup = 0;
        /**
         * 设置通道波形显示的垂直档位 又名：EXT
         */
        double extent = 0;
        /**
         * 通道波形显示的垂直偏移
         */
        double pos = 0;
        /**
         * 打开或关闭指定通道的垂直档位微调功能
         */
        boolean Vern = false;
        /**
         * 获得通道波形到上位机
         */
        int pc = 0;
        /**
         * 阻抗
         */
        int Inp = 0;
        /** 带宽 */
        /**
         * const char * chan_band[] = {
         * "FULL"
         * , "200M"
         * , "20M"
         * , "HIGH"
         * , "LOW"
         * , NULL
         * };
         */
        int band_param2;
        /**
         * 高通低通 具体数据
         */
        double band_param3;
       /**
        * 垂直展开基准
        */
       int Vref=0;
       /**
        * 通道标签
        */
       String label;
       double delay;
       double offset;
       boolean vernier;

       @Override
       public String toString() {
           final StringBuilder sb = new StringBuilder("ChannelAttribute{");
           sb.append("display=").append(display);
           sb.append(", inverse=").append(inverse);
           sb.append(", prty=").append(prty);
           sb.append(", prob=").append(prob);
           sb.append(", coup=").append(coup);
           sb.append(", extent=").append(extent);
           sb.append(", pos=").append(pos);
           sb.append(", Vern=").append(Vern);
           sb.append(", pc=").append(pc);
           sb.append(", Inp=").append(Inp);
           sb.append(", band_param2=").append(band_param2);
           sb.append(", band_param3=").append(band_param3);
           sb.append(", Vref=").append(Vref);
           sb.append(", label='").append(label).append('\'');
           sb.append('}');
           return sb.toString();
       }
   }

    private List<ChannelAttribute> chDisplayList = new ArrayList<>();
    private int currActiveChannel = TChan.Ch1;
    private int currActiveObject = TChan.Ch1;

    public Command_Channel() {
        TChan.foreachAllChan((chNo)->{
            ChannelAttribute chAttr = new ChannelAttribute();
            chDisplayList.add(chAttr);
        });
//        for (int i = 0; i <= IWave.S2; i++) {
//            ChannelAttribute chAttr = new ChannelAttribute();
//            chDisplayList.add(chAttr);
//        }


    }

    /**
     * 设置通道状态
     *
     * @param chIndex
     * @param isOpen
     * @param isUpdateUI
     */
    public void Display(int chIndex, boolean isOpen, boolean isUpdateUI) {
        if (!ChannelFactory.isDynamicCh(chIndex)) return;
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
//        Logger.i(Command.TAG,"chIndex:"+chIndex+",isOpen:"+isOpen+",scpi disPlay:"+chAttr.display);
        if (chAttr.display == isOpen) return;
        chAttr.display = isOpen;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DISPLAY);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen);
            msgToUI.setParam(param);
            msgToUI.setObject(chDisplayList);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean DisplayQ(int chIndex) {
        return chDisplayList.get(chIndex).display;
    }

    /**
     * 返回采样通道个数
     */
//    public int getSampleCount() {
//        int n = 0;
//        for (int i = IWave.Ch1; i <= IWave.Ch4; i++) {
//            if (chDisplayList.get(i).display) n++;
//        }
//        return n;
//    }

    /**
     * 设置通道反向显示
     *
     * @param chIndex
     * @param isOpen
     * @param isUpdateUI
     */
    public void Inverse(int chIndex, boolean isOpen, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.inverse == isOpen) return;
        chAttr.inverse = isOpen;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_INVERSE);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean InverseQ(int chIndex) {
        return chDisplayList.get(chIndex).inverse;
    }

    public void Band(int chIndex, int bandIndex, double bandDetail, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
//        if (chAttr.band_param2 == bandIndex && chAttr.band_param3 == bandDetail) return;
        int min=30;
        long max=(long) (Channel.getMaxBandWidth());
//        Logger.i(Command.TAG,"max:"+max+",channel:"+chIndex+",band:"+bandIndex);
        if ( (bandIndex==3 || bandIndex==4) ){
            if (bandDetail<min) bandDetail=min;
            if (bandDetail>max) bandDetail=max;
        }

        chAttr.band_param2 = bandIndex;
        chAttr.band_param3 = bandDetail;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_BANDWIDTH);
            String param = String.valueOf(chIndex)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bandIndex)
                    + CommandMsgToUI.PARAM_SPLIT + String.valueOf(bandDetail);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 返回的与通道无关，默认返回ch1的值
     */
    public int BandQ(int chIndex) {
        return chDisplayList.get(chIndex).band_param2;
    }
    public double BandWidthValueQ(int chIdx){
        return ChannelFactory.getDynamicChannel(chIdx).getBandWidth();
    }
    public void Prty(int chIndex, int prty, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.prty == prty) return;
        chAttr.prty = prty;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_PROBETYPE);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(prty);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int PrtyQ(int chIndex) {
        return chDisplayList.get(chIndex).prty;
    }


    public void Probe(int chIndex, double probe, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.prob == probe) return;
        chAttr.prob = probe;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_PROBEMULTIPLE);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(probe);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public double ProbeQ(int chIndex) {
        return chDisplayList.get(chIndex).prob;
    }

    public void Couple(int chIndex, int couple, boolean isUpdateUI) {
        int impedance = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_IMPED + TChan.toUiChNo(chIndex));
        if(impedance == 1 && couple == 1) return;
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.coup == couple) return;
        chAttr.coup = couple;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_COUPLE);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(couple);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int CoupleQ(int chIndex) {
        return chDisplayList.get(chIndex).coup;
    }

    /**
     * 输入阻抗
     */
    public void Inputres(int chIndex,int inp, boolean isUpdateUI) {
        int chCouple = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE + TChan.toUiChNo(chIndex));
        if (chCouple == 1 && inp == 1) return;
//        Logger.i(Command.TAG,"ch:"+chIndex+",inp:"+inp);
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.Inp == inp) return;
        chAttr.Inp = inp;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_INPUTRES);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(inp);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 返回输入阻抗 目前不支持
     */
    public int InputresQ(int chIndex) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        return chAttr.Inp;
    }

    private double[] arrayExtent;

    /**
     * 设置波形的垂直档位
     */
    public void Extent(int chIndex, double extent, boolean isUpdateUI) {

        if (chIndex < 0 || chIndex > 7) return;
        if (arrayExtent == null) {
            String[] arrayStr = App.get().getResources().getStringArray(R.array.channelProbeEveryDouble);
            arrayExtent = new double[arrayStr.length];
            for (int i = 0; i < arrayStr.length; i++) {
                arrayExtent[i] = Double.parseDouble(arrayStr[i]);
            }
        }
        String s=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + (TChan.toUiChNo(chIndex)));
        double multiple = TBookUtil.getDoubleFromX(s);
//        extent = DoubleUtil.divide(extent, multiple,5);


//        boolean isContinue = false;
//        for (double anArrayExtent : arrayExtent) {
//            if (DoubleUtil.compareTo(anArrayExtent, extent) == 0) {
//                isContinue = true;
//                break;
//            }
//        }
//        if (!isContinue) {
//            if (DoubleUtil.compareTo(extent, arrayExtent[0]) < 0) {
//                extent = arrayExtent[0];
//                isContinue = true;
//            } else if (DoubleUtil.compareTo(extent, arrayExtent[arrayExtent.length - 1]) > 0) {
//                extent = arrayExtent[arrayExtent.length - 1];
//                isContinue = true;
//            }
//        }
//        if (!isContinue) return;
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
//        if (chAttr.extent == extent) return;
        chAttr.extent = extent;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_EXTENT);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent);
            msgToUI.setParam(param);
            CursorManage.getInstance().setScpiChanIdx(chIndex);
            CursorManage.getInstance().setCursorTrace(true);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    public void Plus_Extent(int chIndex, int extentIndex, boolean isUpdateUI) {
        if (extentIndex != 1 && extentIndex != -1) return;
        if (arrayExtent == null) {
            String[] arrayStr = App.get().getResources().getStringArray(R.array.channelProbeEveryDouble);
            arrayExtent = new double[arrayStr.length];
            for (int i = 0; i < arrayStr.length; i++) {
                arrayExtent[i] = Integer.parseInt(arrayStr[i]);
            }
        }
        int index = 0;
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        for (int i = 0; i < arrayExtent.length; i++) {
            if (Double.doubleToLongBits(arrayExtent[i]) == Double.doubleToLongBits(chAttr.extent)) {
                index = i;
                break;
            }
        }
        if (index == 0 && extentIndex == -1) {
            index = arrayExtent.length - 1;
        } else if (index == arrayExtent.length - 1 && extentIndex == 1) {
            index = 0;
        } else {
            index = index + extentIndex;
        }
        Extent(chIndex, arrayExtent[index], isUpdateUI);
    }

    public double ExtentQ(int chIndex) {
//        return chDisplayList.get(chIndex).extent * ProbeQ(chIndex);

        double chVal = ChannelFactory.getDynamicChannel(chIndex).getVScaleVal();
        return chVal;
    }

    public void setPosition(int chIndex,double posPix){
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        chAttr.pos = posPix;
        chDisplayList.set(chIndex, chAttr);
    }
    /**
     * 设置波形的垂直位置
     * @param chIndex 通道 chindex 0开始
     * @param posV 垂直偏移
     * @param isUpdateUI 是否更新界面
     */
    public void Position(int chIndex, double posV, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        //档位转像素
         Channel ch= ChannelFactory.getDynamicChannel(chIndex);
        double d= 0;//每个像素对应的幅度值
        if (ch != null) {
            d = ch.getVerticalPerPix();
        }
//        Logger.i("Command"," d:"+d+"  vsid:"+ch.getVScaleId());
        double offsetPix = (Tools.isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2.0 - (posV / d);
        if (chAttr.pos == offsetPix) return;
        chAttr.pos = offsetPix;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CursorManage.getInstance().setScpiChanIdx(chIndex);
            CursorManage.getInstance().setCursorTrace(true);
            WaveManage.get().setPositionY(chIndex + 1,offsetPix);
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    public void Plus_Position(int chIndex, int posIndex, boolean isUpdateUI) {
        if (posIndex == 1 || posIndex == -1) {
            Channel ch= ChannelFactory.getDynamicChannel(chIndex);
            double d= 0;
            if (ch != null) {
                d = ch.getVerticalPerPix();
            }
            Position(chIndex, this.PositionQ(chIndex)+posIndex*d,isUpdateUI);
        }
    }

    public double PositionQ(int chIndex) {
        Channel ch = ChannelFactory.getDynamicChannel(chIndex);
        if (ch != null) {
            double d = ch.getVerticalPerPix();
            return (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) / 2 - WaveManage.get().getPositionY(chIndex + 1)) * d;
        }
        return 0;
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     */
    public void Vernier(int index, boolean isUpdateUI) {
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     */
    public boolean VernierQ() {
        return false;
    }


    public void Pc(int chIndex, int pc, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        if (chAttr.pc == pc) return;
        chAttr.pc = pc;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
        }
    }

    public int PCQ(int chIndex) {
        return chDisplayList.get(chIndex).pc;
    }


    public void Label(int chIndex,String label,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        chAttr.label = label;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_LABEL);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(label);
            msgToUI.setParam(param);
            msgToUI.setObject(chDisplayList);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public String LabelQ(int chIndex){
        if (StrUtil.isEmpty(chDisplayList.get(chIndex).label)){
            return "NONE";
        }
        return chDisplayList.get(chIndex).label;
    }

    public void Clear(int chIndex,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        chAttr.label = "";
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_LABEL_CLEAR);
            String param = String.valueOf(chIndex) ;
            msgToUI.setParam(param);
            msgToUI.setObject(chDisplayList);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public String CountQ(){
        return String.valueOf(GlobalVar.get().getChannelsCount());
    }
    /**
     * 设置当前活动通道
     * 包括：CH1，ch2,ch3,ch4,math ,ref1,ref2,ref3,ref4
     */
    public void setCurrActiveChannel(int chIndex, boolean isUpdateUI) {
        if (currActiveChannel == chIndex) return;
        this.currActiveChannel = chIndex;
        if (isUpdateUI) {
        }
    }

    public int getCurrActiveChannel() {
        return this.currActiveChannel;
    }

    /**
     * 设置当前活动对像
     *
     * @param index      ch1,ch2,ch3,ch4,math,ref1,ref2,ref3,ref4,s1,s2,光标1，光标2，光标3，光标4，触发时刻
     * @param isUpdateUI
     */
    public void setCurrActiveObject(int index, boolean isUpdateUI) {
        if (currActiveObject == index) return;
        this.currActiveObject = index;
        if (isUpdateUI) {
        }
    }

    public int getCurrActiveObject() {
        return this.currActiveObject;
    }


    public void Vref(int chIndex,int vref,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        chAttr.Vref = vref;
        chDisplayList.set(chIndex, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_VREF);
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(vref);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int VrefQ(int chIndex){
        ChannelAttribute chAttr = chDisplayList.get(chIndex);
        return chAttr.Vref;

    }

    private int currentActivty;
    public void Current(int chIndex,boolean isUpdateUI){
        this.currentActivty=chIndex;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_CURRENT);
            String param = String.valueOf(chIndex);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int CurrentQ(){
       return TChan.toFpgaChNo( WaveManage.get().getCurCh());
//        return currentActivty;
    }


    public void Delay(int chIdx,double d,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIdx);
        chAttr.delay = d;
        chDisplayList.set(chIdx, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_DELAY);
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public double DelayQ(int chIdx){
        //以ps为单位
        int ps= ChannelFactory.getDynamicChannel(chIdx).getDelay();
        return ps*1e-12;
    }

    public void Offset(int chIdx,double d,boolean isUpdateUI){
        ChannelAttribute chAttr = chDisplayList.get(chIdx);
        chAttr.offset = d;
        chDisplayList.set(chIdx, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_OFFSET);
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(d);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public double OffsetQ(int chIdx){
       return   ChannelFactory.getDynamicChannel(chIdx).getChOffsetVal();
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     */
    public void Vernier(int chIdx,boolean isOpen, boolean isUpdateUI) {
        ChannelAttribute chAttr = chDisplayList.get(chIdx);
        chAttr.vernier = isOpen;
        chDisplayList.set(chIdx, chAttr);
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CHANNEL_VERNIER);
            String param = String.valueOf(chIdx) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isOpen);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 打开或关闭指定通道的垂直档位微调功能 目前不支持
     */
    public boolean VernierQ(int chIdx) {
//        return ChannelFactory.getDynamicChannel(chIdx).isFineExtent();
        ChannelAttribute chAttr = chDisplayList.get(chIdx);
        return chAttr.vernier;
    }


    public String getProbeInfoQ(int chIdx){
        Channel chan= ChannelFactory.getDynamicChannel(chIdx);
        BaseProbe probe= chan.getProbe();
        if (probe==null){
            return "None";
        }else {
//            String b= JSON.toJSONString(probe);
//            return b;
            StringBuilder sb=new StringBuilder();
            sb.append(probe.getProbeName()).append(":");
            sb.append(probe.getSN()).append(":");
            sb.append(probe.getVersion()).append(":");
            sb.append(probe.getProbeX().stream().collect(Collectors.joining(","))).append(":");
            sb.append(probe.getBandWidth()).append(":");
            sb.append(probe.isSupportImped50()).append(":");
            return sb.toString();

        }

    }
}
