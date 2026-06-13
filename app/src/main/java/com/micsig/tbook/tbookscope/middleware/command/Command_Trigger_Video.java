package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Video {
//     new SCPICommandStruct(":TRIGger:VIDeo:SOURce","SCPI_Trigger_Video","Source"),//设置视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:SOURce?","SCPI_Trigger_Video","SourceQ"),//查询视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity","SCPI_Trigger_Video","Polarity"),//设置视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity?","SCPI_Trigger_Video","PolarityQ"),//查询视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard","SCPI_Trigger_Video","Standard"),//设置视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard?","SCPI_Trigger_Video","StandardQ"),//查询视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe","SCPI_Trigger_Video","Amode"),//设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe?","SCPI_Trigger_Video","AmodeQ"),//查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe","SCPI_Trigger_Video","Bmode"),//设置触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe?","SCPI_Trigger_Video","BmodeQ"),//查询触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence","SCPI_Trigger_Video","Afrequence"),//设置触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence?","SCPI_Trigger_Video","AfrequenceQ"),//查询触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence","SCPI_Trigger_Video","Bfrequence"),//设置触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence?","SCPI_Trigger_Video","BfrequenceQ"),//查询触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE","SCPI_Trigger_Video","Line"),
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE?","SCPI_Trigger_Video","LineQ"),

    private int source;
    /**
     * const char * trig_vid_pol[] = {
     * "POSitive",
     * "NEGative",
     * NULL
     * };
     */
    private int polarity;
    /**
     * const char * trig_vid_stan[] = {
     * "PAL",
     * "SECam",
     * "NTSC",
     * "720P",
     * "1080I",
     * "1080P",
     * NULL
     * };
     */
    private int standard;
    /**
     * const char * trig_vid_amod[] = {
     * "ODDField",
     * "EVENfied",
     * "ALLField",
     * "ALLLine",
     * "LINE",
     * NULL
     * };
     */
    private int amode;
    /**
     * const char * trig_vid_bmod[] = {
     * "ALLField",
     * "ALLLine",
     * "LINE",
     * NULL
     * };
     */
    private int bmode;
    /**
     * const char * trig_vid_afr[] = {
     * "60Hz",
     * "50Hz",
     * NULL
     * };
     */
    private int afrequence;
    /**
     * const char * trig_vid_bfr[] = {
     * "60Hz",
     * "50Hz",
     * "30Hz",
     * "25Hz",
     * "24Hz",
     * NULL
     * };
     */
    private int bfrequence;
    /**
     * 当mode为line时的行号
     */
    private int line;

    /**
     * 设置视频触发的触发源
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;
        this.source = source;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_SOURCE);
            msgToUI.setParam(String.valueOf(source));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询视频触发的触发源
     */
    public int SourceQ() {
        return source;
    }

    /**
     * 设置视频触发的极性
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;
        this.polarity = polar;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_POLAR);
            msgToUI.setParam(String.valueOf(polar));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询视频触发的极性
     */
    public int PolarityQ() {
        return polarity;
    }

    /**
     * 设置视频触发时的视频标准
     */
    public void Standard(int standard, boolean isUpdateUI) {
        if (this.standard == standard) return;
        this.standard = standard;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_STANDARD);
            msgToUI.setParam(String.valueOf(standard));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询视频触发时的视频标准
     */
    public int StandardQ() {
        return standard;
    }

    /**
     * 设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
     */
    public void Amode(int amode, boolean isUpdateUI) {
//        if (this.amode == amode) return;
        this.amode = amode;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_AMODE);
            msgToUI.setParam(String.valueOf(amode));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
     */
    public int AmodeQ() {
        return amode;
    }

    /**
     * 设置触发标准为720P、1080P时视频触发的同步类型
     */
    public void Bmode(int bmode, boolean isUpdateUI) {
//        if (this.bmode == bmode) return;
        this.bmode = bmode;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_BMODE);
            msgToUI.setParam(String.valueOf(bmode));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询触发标准为720P、1080P时视频触发的同步类型
     */
    public int BmodeQ() {
        return bmode;
    }

    /**
     * 设置触发标准为720P、1080I时视频触发的信号频率
     */
    public void Afrequence(int afrequence, boolean isUpdateUI) {
//        if (this.afrequence == afrequence) return;
        this.afrequence = afrequence;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_AFREQUENCE);
            msgToUI.setParam(String.valueOf(afrequence));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询触发标准为720P、1080I时视频触发的信号频率
     */
    public int AfrequenceQ() {
        return afrequence;
    }

    /**
     * 设置触发标准为1080P时视频触发的信号频率
     */
    public void Bfrequence(int bfrequence, boolean isUpdateUI) {
//        if (this.bfrequence == bfrequence) return;
        this.bfrequence = bfrequence;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_BFREQUENCE);
            msgToUI.setParam(String.valueOf(bfrequence));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询触发标准为1080P时视频触发的信号频率
     */
    public int BfrequenceQ() {
        return bfrequence;
    }

    public void Line(int line, boolean isUpdateUI) {
//        if (this.line == line) return;
        int max=100;
        int min=1;
        switch (standard){
            case 0:max=625;break;
            case 1:max=625;break;
            case 2:max=525;break;
            case 3:max=750;break;
            case 4:max=1125;break;
            case 5:max=1125;break;
        }
        if (line<min) line=min;
        if (line>max) line=max;
        this.line = line;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_LINE);
            msgToUI.setParam(String.valueOf(line));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int LineQ() {
        return line;
    }
}
