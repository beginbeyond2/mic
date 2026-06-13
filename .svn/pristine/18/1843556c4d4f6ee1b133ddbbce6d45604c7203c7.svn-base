package com.micsig.tbook.tbookscope.middleware.command;


import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Menu {
//      new SCPICommandStruct(":MENU:AUTO","SCPI_Menu","Auto"),//自动
//       new SCPICommandStruct(":MENU:RUN","SCPI_Menu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
//       new SCPICommandStruct(":MENU:STOP","SCPI_Menu","Stop"),//使示波器停止运行，数据采集停止
//       new SCPICommandStruct(":MENU:SINGle","SCPI_Menu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
//       new SCPICommandStruct(":MENU:MULTiple","SCPI_Menu","Multiple"),//将示波器设置为连续触发方式
//       new SCPICommandStruct(":MENU:BEEP","SCPI_Menu","Beep"),//设置示波器的蜂鸣状态
//       new SCPICommandStruct(":MENU:HALF:CHANnel","SCPI_Menu","Channel"),//将通道位置设置为垂直零点位置（波形显示区垂直中心）
//       new SCPICommandStruct(":MENU:HALF:TRIGpos","SCPI_Menu","TrigPos"),//设置触发位置到屏幕中间
//       new SCPICommandStruct(":MENU:HALF:XCURsor","SCPI_Menu","Xcursor"),//设置通道的垂直光标在50%处
//       new SCPICommandStruct(":MENU:HALF:YCURsor","SCPI_Menu","Ycursor"),//设置通道的水平光标在50%处
//       new SCPICommandStruct(":MENU:HALF:LEVel","SCPI_Menu","Level"),//将触发电平设置为触发信号幅值的中间位置
//       new SCPICommandStruct(":MENU:HOMepage","SCPI_Menu","HomePage"),//设置示波器回到主界面
//       new SCPICommandStruct(":MENU:RETurn","SCPI_Menu","Return"),//设置退出示波器程序，返回主界面
//       new SCPICommandStruct(":MENU:LOCK","SCPI_Menu","Lock"),//锁定示波器屏幕
//       new SCPICommandStruct(":MENU:UNLock","SCPI_Menu","Unlock"),//解锁示波器屏幕
//       new SCPICommandStruct(":MENU:COUNter","SCPI_Menu","Counter"),//频率计的打开与关闭
//       new SCPICommandStruct(":MENU:COUNter?","SCPI_Menu","CounterQ"),//频率计的打开与关闭查询
//       new SCPICommandStruct(":MENU:RESet","SCPI_Menu","Reset"),//恢复出厂设置
//       new SCPICommandStruct(":MENU:MEASure","SCPI_Menu","Measure"),//打开测量菜单
//       new SCPICommandStruct(":MENU:TRIGger","SCPI_Menu","Trigger"),//打开触发菜单

    //频率计源
    private int freSource;
    /** 通道选择条是否显示 */
    private boolean channelSelectorVisible =false;
    private int idxAux_trigger;
    private int idxAux_clock;

    private MainViewGroup mainViewGroup;
    public void initMainViewGroup(MainViewGroup mainViewGroup){
        this.mainViewGroup=mainViewGroup;
    }

    /** 自动 */
    public  void Auto(boolean isRun,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUTO);
            msgToUI.setParam(String.valueOf(isRun));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public boolean AutoQ(){
        return Scope.getInstance().isAuto();
    }

    /** 使示波器开始运行，符合触发条件，开始采集数据 */
    public  void Run(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RUN);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 使示波器停止运行，数据采集停止 */
    public  void Stop(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_STOP);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 将示波器设置为单序列，示波器捕获并显示单次采集 */
    public  void Single(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_SINGLE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 将示波器设置为连续触发方式 */
    public  void Multiple(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MULTIPLE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置示波器的蜂鸣状态 */
    public  void Beep(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_BEEP);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 将通道位置设置为垂直零点位置（波形显示区垂直中心） */
    public  void Half_Channel(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_HALF_CHANNEL);
            msgToUI.setParam(String.valueOf(chIndex+1));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置触发位置到屏幕中间 */
    public  void TrigPos(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_TRIGPOS);
            msgToUI.setParam(String.valueOf(chIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置通道的垂直光标在50%处 */
    public  void Xcursor(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_XCURSOR);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置通道的水平光标在50%处 */
    public  void Ycursor(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_YCURSOR);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 将触发电平设置为触发信号幅值的中间位置 */
    public  void Level(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_LEVEL);
            msgToUI.setParam(String.valueOf(chIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置示波器回到主界面 */
    public  void HomePage(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_HOMEPAGE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 设置退出示波器程序，返回主界面 */
    public  void Return(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RETURN);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 锁定示波器屏幕 */
    public  void Lock(boolean isLock,boolean isUpdateUI){
        if (isLock) {
            ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_KEY);
        }else {
            ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_KEY);
        }
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_LOCK);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean LockQ(boolean isUpdateUI){
        return ScreenControls.getInstance().isLockScreen();
    }

    /** 解锁示波器屏幕 */
    public  void Unlock(boolean isUpdateUI){
        ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_KEY);
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_UNLOCK);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 频率计的打开与关闭 */
    public  void Counter(int chIndex,boolean isUpdateUI){
        int ch=chIndex+1;
        if (ch> TChan.MaxLogicChan) ch=0;
        if (freSource == ch) return;
        this.freSource =ch;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_COUNTER);
            msgToUI.setParam(String.valueOf(ch));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 频率计的打开与关闭查询 */
    public  int CounterQ(){
        return this.freSource;
    }

    /** 恢复出厂设置 */
    public  void Reset(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_FACTORYRESET);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 打开测量菜单 */
    public  void Measure(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MEASURE);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 打开触发菜单 */
    public  void Trigger(boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_TRIGGER);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void Channel(int ch,boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_CHANNEL);
            msgToUI.setParam(String.valueOf(ch)+CommandMsgToUI.PARAM_SPLIT+String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public boolean ChannelQ(int ch){
        int slip=-1;
        switch (ch)
        {
            case TChan.Ch1-1:slip=MainViewGroup.RIGHTSLIP_CH1;break;
            case TChan.Ch2-1:slip=MainViewGroup.RIGHTSLIP_CH2;break;
            case TChan.Ch3-1:slip=MainViewGroup.RIGHTSLIP_CH3;break;
            case TChan.Ch4-1:slip=MainViewGroup.RIGHTSLIP_CH4;break;
            case TChan.Ch5-1:slip=MainViewGroup.RIGHTSLIP_CH5;break;
            case TChan.Ch6-1:slip=MainViewGroup.RIGHTSLIP_CH6;break;
            case TChan.Ch7-1:slip=MainViewGroup.RIGHTSLIP_CH7;break;
            case TChan.Ch8-1:slip=MainViewGroup.RIGHTSLIP_CH8;break;
            case TChan.Math1-1:slip=MainViewGroup.RIGHTSLIP_MATH1;break;
            case TChan.Math2-1:slip=MainViewGroup.RIGHTSLIP_MATH2;break;
            case TChan.Math3-1:slip=MainViewGroup.RIGHTSLIP_MATH3;break;
            case TChan.Math4-1:slip=MainViewGroup.RIGHTSLIP_MATH4;break;
            case TChan.Math5-1:slip=MainViewGroup.RIGHTSLIP_MATH5;break;
            case TChan.Math6-1:slip=MainViewGroup.RIGHTSLIP_MATH6;break;
            case TChan.Math7-1:slip=MainViewGroup.RIGHTSLIP_MATH7;break;
            case TChan.Math8-1:slip=MainViewGroup.RIGHTSLIP_MATH8;break;
            case TChan.R1-1:slip=MainViewGroup.RIGHTSLIP_REF1;break;
            case TChan.R2-1:slip=MainViewGroup.RIGHTSLIP_REF2;break;
            case TChan.R3-1:slip=MainViewGroup.RIGHTSLIP_REF3;break;
            case TChan.R4-1:slip=MainViewGroup.RIGHTSLIP_REF4;break;
            case TChan.R5-1:slip=MainViewGroup.RIGHTSLIP_REF5;break;
            case TChan.R6-1:slip=MainViewGroup.RIGHTSLIP_REF6;break;
            case TChan.R7-1:slip=MainViewGroup.RIGHTSLIP_REF7;break;
            case TChan.R8-1:slip=MainViewGroup.RIGHTSLIP_REF8;break;
            case TChan.S1-1:slip=MainViewGroup.RIGHTSLIP_S1;break;
            case TChan.S2-1:slip=MainViewGroup.RIGHTSLIP_S2;break;
            case TChan.S3-1:slip=MainViewGroup.RIGHTSLIP_S3;break;
            case TChan.S4-1:slip=MainViewGroup.RIGHTSLIP_S4;break;
        }
        if (slip==-1)return false;
        return mainViewGroup.isSlipShow(slip);
    }
    public void Quick(boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_QuickBottom);
            msgToUI.setParam(String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public boolean QuickQ(){
        return mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP);
    }
    public void Main(boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_Main);
            msgToUI.setParam(String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public boolean MainQ(){
        return mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP);
    }


    /** SCPI指令未实现  通道选择条的打开与关闭 */
    public void ChannelSelector(boolean isOpen,boolean isUpdateUI){
         if (channelSelectorVisible !=isOpen){
             channelSelectorVisible =isOpen;
             if (isUpdateUI){
                 CommandMsgToUI msgToUI = Command.get().getMsgToUI();
                 msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_CHANNELSELECTOR);
                 RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
             }
         }
    }

    /** SCPI指令未实现  */
    public boolean ChannelSelectorQ(){
        return channelSelectorVisible;
    }

    public void aux_trigger(int idx,boolean isUpdateUI){
        idxAux_trigger=idx;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_TRIGGER);
            msgToUI.setParam(String.valueOf(idx));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int aux_triggerQ(){
        return idxAux_trigger;
    }
    public void aux_clock(int idx,boolean isUpdateUI){
        idxAux_clock=idx;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_CLOCK);
            msgToUI.setParam(String.valueOf(idx));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int aux_clockQ(){
        return Sample.getInstance().isClkInOut()?1:0;
        //return idxAux_clock;
    }
    public void aux_inputres(int idx,boolean isUpdateUI){
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_INPUTRES);
            msgToUI.setParam(String.valueOf(idx));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public int aux_inputresQ(){
        return TriggerFactory.getInstance().getTriggerCommon().getExtTriggerInputRes();
    }

}
