package com.micsig.tbook.tbookscope.middleware.command;

import android.util.Log;

import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.measure.MeasureStaticsBean;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * @auother Liwb
 * @description:
 * @data:2022-12-1 11:41
 */
public class Command_Measure_Statistic {

//     new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure_Statistic","Display"),//打开或关闭统计功能
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure_Statistic","DisplayQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure_Statistic","Reset"),//重新统计
//     new SCPICommandStruct(":MEASure:STATistic:MEAN","SCPI_Measure_Statistic","Mean"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN?","SCPI_Measure_Statistic","MeanQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MAX","SCPI_Measure_Statistic","Max"),//打开或关闭最大值
//     new SCPICommandStruct(":MEASure:STATistic:MAX?","SCPI_Measure_Statistic","MaxQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MIN","SCPI_Measure_Statistic","Min"),//打开或关闭最小值
//     new SCPICommandStruct(":MEASure:STATistic:MIN?","SCPI_Measure_Statistic","MinQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:DEV","SCPI_Measure_Statistic","Dev"),//打开或关闭delta
//     new SCPICommandStruct(":MEASure:STATistic:DEV?","SCPI_Measure_Statistic","DevQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:COUNt","SCPI_Measure_Statistic","count"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt?","SCPI_Measure_Statistic","countQ"),//查询打开状态


    private boolean display;
    private boolean mean;
    private boolean max;
    private boolean min;
    private boolean dev;
    private boolean count;

    public void Display(boolean bDisplay, boolean isUpdateUI) {
        Log.d(Command.TAG, "Display: "+bDisplay);
        this.display = bDisplay;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Display);
            String param = String.valueOf(bDisplay);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean DisplayQ() {
        return this.display;
    }

    public void Reset() {
        CommandMsgToUI msgToUI = Command.get().getMsgToUI();
        msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Reset);
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
    }

    public void Mean(boolean bMean, boolean isUpdateUI) {
        this.mean = bMean;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Mean);
            String param = String.valueOf(bMean);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean MeanQ() {
        return this.mean;
    }

    public void Max(boolean bMax, boolean isUpdateUI) {
        this.max = bMax;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Max);
            String param = String.valueOf(bMax);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean MaxQ() {
        return this.max;
    }

    public void Min(boolean bMin, boolean isUpdateUI) {
        this.min = bMin;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Min);
            String param = String.valueOf(bMin);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean MinQ() {
        return this.min;
    }

    public void Dev(boolean bDev, boolean isUpdateUI) {
        this.dev = bDev;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Dev);
            String param = String.valueOf(bDev);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean DevQ() {
        return this.dev;
    }

    public void Count(boolean bCount, boolean isUpdateUI) {
        this.count = bCount;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Count);
            String param = String.valueOf(bCount);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean CountQ() {
        return this.count;
    }


    public String ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "ALL");
    }

    public String Mean_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Mean");
    }

    public String Max_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Max");
    }

    public String Min_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Min");
    }

    public String Dev_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Dev");
    }

    public String Count_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Count");
    }

    public String Current_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Current");
    }

    private String query_viewQ(int chIndex, int itemIndex, String result_item) {
        for (int i = 0; i < MeasureManage.getInstance().getMeasureItem().getMeasureList().size(); i++) {
            MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().getMeasureItem().getMeasureList().get(i);
            int iWaveCh = item.getChannelId();
            if (TChan.isRef(iWaveCh)) continue;
            int measureId = item.getMeasureId();
            if (measureId != itemIndex || iWaveCh != chIndex) continue; //不是指定的测量项目和通道，继续查下一个

            String measureName = item.getMeasureName();
            Measure measure = getHardwareMeasure(iWaveCh - 1);
            MeasureStaticsBean bean = measure.getMeasureStatics(measureId + 16);
            if(bean!=null && bean.getNums()>0) {
                switch (result_item) {
                    case "ALL": {
                        StringBuilder sb = new StringBuilder();
                        sb.append(bean.getVal());
                        sb.append(",");
                        sb.append(bean.getAverageVal());
                        sb.append(",");
                        sb.append(bean.getMaxVal());
                        sb.append(",");
                        sb.append(bean.getMinVal());
                        sb.append(",");
                        sb.append(bean.getMqdVal());
                        sb.append(",");
                        sb.append(bean.getNums());
                        return sb.toString();
                    }
                    case "Mean":
                        return String.valueOf(bean.getAverageVal());
                    case "Max":
                        return String.valueOf(bean.getMaxVal());
                    case "Min":
                        return String.valueOf(bean.getMinVal());
                    case "Dev":
                        return String.valueOf(bean.getMqdVal());
                    case "Count":
                        return String.valueOf(bean.getNums());
                    case "Current":
                        return String.valueOf(bean.getVal());

                }
            }else {
                switch (result_item) {
                    case "ALL": {
                        StringBuilder sb = new StringBuilder();
                        sb.append("NONE");
                        sb.append(",");
                        sb.append("NONE");
                        sb.append(",");
                        sb.append("NONE");
                        sb.append(",");
                        sb.append("NONE");
                        sb.append(",");
                        sb.append("NONE");
                        sb.append(",");
                        sb.append("NONE");
                        return sb.toString();
                    }
                    case "Mean":
                        return String.valueOf("NONE");
                    case "Max":
                        return String.valueOf("NONE");
                    case "Min":
                        return String.valueOf("NONE");
                    case "Dev":
                        return String.valueOf("NONE");
                    case "Count":
                        return String.valueOf("NONE");
                    case "Current":
                        return String.valueOf("NONE");

                }
            }
        }
        return "NONE";
    }

    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;
        if (ChannelFactory.isDynamicCh(chId)) {
            baseChannel = ChannelFactory.getDynamicChannel(chId);
        } else if (ChannelFactory.isMathCh(chId)) {
            baseChannel = ChannelFactory.getMathChannel(chId);
        } else if (ChannelFactory.isRefCh(chId)) {
            baseChannel = ChannelFactory.getRefChannel(chId);
        }
        if (baseChannel != null) {
            return baseChannel.getMeasure();
        }
        return null;
    }
}
