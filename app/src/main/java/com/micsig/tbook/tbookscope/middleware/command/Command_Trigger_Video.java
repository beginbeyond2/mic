package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 基于RxJava的全局事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举定义

/*
 * +=============================================================================+
 * |                     Command_Trigger_Video - 视频触发命令模型                        |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，视频触发器的参数存储与UI同步                      |
 * | 核心职责 : 管理视频触发器的全部可配置参数（触发源、极性、视频标准、同步类型A/B、       |
 * |            信号频率A/B、行号），参数变更时通过RxBus通知UI层刷新                      |
 * | 架构设计 : 纯数据模型 + 观察者通知，每个setter内部判断值是否变化，变化则封装           |
 * |            CommandMsgToUI并通过RxBus.post()广播                                    |
 * | 数据流向 : SCPI解析层 → 本类setter → RxBus → UI层监听回调                            |
 * | 依赖关系 : CommandMsgToUI(消息封装)、RxBus/RxEnum(事件总线)                          |
 * | 使用场景 : 用户通过面板或远程SCPI指令设置视频触发参数时，由SCPI分发器调用对应方法       |
 * +=============================================================================+
 */

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

    private int source;  // 触发源通道索引
    /**
     * const char * trig_vid_pol[] = {
     * "POSitive",
     * "NEGative",
     * NULL
     * };
     */
    private int polarity;  // 极性：0=正极性, 1=负极性
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
    private int standard;  // 视频标准：0=PAL, 1=SECAM, 2=NTSC, 3=720P, 4=1080I, 5=1080P
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
    private int amode;  // A类同步类型（PAL/SECAM/NTSC/1080I）：0=奇数场, 1=偶数场, 2=所有场, 3=所有行, 4=指定行
    /**
     * const char * trig_vid_bmod[] = {
     * "ALLField",
     * "ALLLine",
     * "LINE",
     * NULL
     * };
     */
    private int bmode;  // B类同步类型（720P/1080P）：0=所有场, 1=所有行, 2=指定行
    /**
     * const char * trig_vid_afr[] = {
     * "60Hz",
     * "50Hz",
     * NULL
     * };
     */
    private int afrequence;  // A类信号频率（720P/1080I）：0=60Hz, 1=50Hz
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
    private int bfrequence;  // B类信号频率（1080P）：0=60Hz, 1=50Hz, 2=30Hz, 3=25Hz, 4=24Hz
    /**
     * 当mode为line时的行号
     */
    private int line;  // 指定触发行号

    /**
     * 设置视频触发的触发源
     *
     * @param source      触发源值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Source(int source, boolean isUpdateUI) {
        if (this.source == source) return;  // 值未变化则直接返回
        this.source = source;  // 更新触发源值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_SOURCE);  // 设置消息标志为视频触发源变更
            msgToUI.setParam(String.valueOf(source));  // 设置消息参数为触发源值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询视频触发的触发源
     *
     * @return 触发源值
     */
    public int SourceQ() {
        return source;  // 返回当前触发源值
    }

    /**
     * 设置视频触发的极性
     *
     * @param polar       极性值（0=正极性, 1=负极性）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Polarity(int polar, boolean isUpdateUI) {
        if (this.polarity == polar) return;  // 值未变化则直接返回
        this.polarity = polar;  // 更新极性值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_POLAR);  // 设置消息标志为视频极性变更
            msgToUI.setParam(String.valueOf(polar));  // 设置消息参数为极性值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询视频触发的极性
     *
     * @return 极性值
     */
    public int PolarityQ() {
        return polarity;  // 返回当前极性值
    }

    /**
     * 设置视频触发时的视频标准
     *
     * @param standard    视频标准值（0=PAL, 1=SECAM, 2=NTSC, 3=720P, 4=1080I, 5=1080P）
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Standard(int standard, boolean isUpdateUI) {
        if (this.standard == standard) return;  // 值未变化则直接返回
        this.standard = standard;  // 更新视频标准值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_STANDARD);  // 设置消息标志为视频标准变更
            msgToUI.setParam(String.valueOf(standard));  // 设置消息参数为视频标准值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询视频触发时的视频标准
     *
     * @return 视频标准值
     */
    public int StandardQ() {
        return standard;  // 返回当前视频标准值
    }

    /**
     * 设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
     *
     * @param amode       A类同步类型值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Amode(int amode, boolean isUpdateUI) {
//        if (this.amode == amode) return;
        this.amode = amode;  // 更新A类同步类型值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_AMODE);  // 设置消息标志为视频A类同步类型变更
            msgToUI.setParam(String.valueOf(amode));  // 设置消息参数为A类同步类型值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
     *
     * @return A类同步类型值
     */
    public int AmodeQ() {
        return amode;  // 返回当前A类同步类型值
    }

    /**
     * 设置触发标准为720P、1080P时视频触发的同步类型
     *
     * @param bmode       B类同步类型值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Bmode(int bmode, boolean isUpdateUI) {
//        if (this.bmode == bmode) return;
        this.bmode = bmode;  // 更新B类同步类型值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_BMODE);  // 设置消息标志为视频B类同步类型变更
            msgToUI.setParam(String.valueOf(bmode));  // 设置消息参数为B类同步类型值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询触发标准为720P、1080P时视频触发的同步类型
     *
     * @return B类同步类型值
     */
    public int BmodeQ() {
        return bmode;  // 返回当前B类同步类型值
    }

    /**
     * 设置触发标准为720P、1080I时视频触发的信号频率
     *
     * @param afrequence  A类信号频率值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Afrequence(int afrequence, boolean isUpdateUI) {
//        if (this.afrequence == afrequence) return;
        this.afrequence = afrequence;  // 更新A类信号频率值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_AFREQUENCE);  // 设置消息标志为视频A类频率变更
            msgToUI.setParam(String.valueOf(afrequence));  // 设置消息参数为A类信号频率值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询触发标准为720P、1080I时视频触发的信号频率
     *
     * @return A类信号频率值
     */
    public int AfrequenceQ() {
        return afrequence;  // 返回当前A类信号频率值
    }

    /**
     * 设置触发标准为1080P时视频触发的信号频率
     *
     * @param bfrequence  B类信号频率值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Bfrequence(int bfrequence, boolean isUpdateUI) {
//        if (this.bfrequence == bfrequence) return;
        this.bfrequence = bfrequence;  // 更新B类信号频率值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_BFREQUENCE);  // 设置消息标志为视频B类频率变更
            msgToUI.setParam(String.valueOf(bfrequence));  // 设置消息参数为B类信号频率值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询触发标准为1080P时视频触发的信号频率
     *
     * @return B类信号频率值
     */
    public int BfrequenceQ() {
        return bfrequence;  // 返回当前B类信号频率值
    }

    /**
     * 设置视频触发的行号，根据视频标准校验范围
     *
     * @param line        行号值
     * @param isUpdateUI  是否通知UI刷新
     */
    public void Line(int line, boolean isUpdateUI) {
//        if (this.line == line) return;
        int max=100;  // 默认最大行号
        int min=1;  // 最小行号
        switch (standard){  // 根据视频标准确定最大行号
            case 0:max=625;break;  // PAL标准最大625行
            case 1:max=625;break;  // SECAM标准最大625行
            case 2:max=525;break;  // NTSC标准最大525行
            case 3:max=750;break;  // 720P标准最大750行
            case 4:max=1125;break;  // 1080I标准最大1125行
            case 5:max=1125;break;  // 1080P标准最大1125行
        }
        if (line<min) line=min;  // 低于下限则钳位到1
        if (line>max) line=max;  // 超过上限则钳位到最大行号
        this.line = line;  // 更新行号值
        if (isUpdateUI) {  // 需要通知UI刷新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();  // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERVIDEO_LINE);  // 设置消息标志为视频行号变更
            msgToUI.setParam(String.valueOf(line));  // 设置消息参数为行号值
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);  // 广播消息到UI层
        }
    }

    /**
     * 查询视频触发的行号
     *
     * @return 行号值
     */
    public int LineQ() {
        return line;  // 返回当前行号值
    }
}
