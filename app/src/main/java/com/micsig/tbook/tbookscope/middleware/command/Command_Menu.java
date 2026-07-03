package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import com.micsig.tbook.scope.Sample.Sample; // 采样管理
import com.micsig.tbook.scope.Scope; // 示波器核心作用域
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图组
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 屏幕控制工具
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                            Command_Menu                                     |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器菜单功能命令处理模块                                         |
 * | 核心职责: 处理SCPI菜单相关指令，包括自动设置、运行/停止/单次/连续、蜂鸣、     |
 * |          通道归零、触发位置居中、光标居中、电平居中、主页/返回、锁屏/解锁、   |
 * |          频率计、出厂重置、通道开关、快捷菜单、主菜单、通道选择条、辅助功能等 |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, Scope, MainViewGroup,      |
 * |           ScreenControls, TChan, Sample, TriggerFactory                      |
 * | 使用场景: 远程控制示波器菜单功能时使用                                       |
 * +-----------------------------------------------------------------------------+
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
    private int freSource; // 频率计信号源通道索引
    /** 通道选择条是否显示 */
    private boolean channelSelectorVisible =false; // 通道选择条可见性标志
    private int idxAux_trigger; // 辅助触发索引
    private int idxAux_clock; // 辅助时钟索引

    private MainViewGroup mainViewGroup; // 主视图组引用

    /**
     * 初始化主视图组引用
     *
     * @param mainViewGroup 主视图组对象
     */
    public void initMainViewGroup(MainViewGroup mainViewGroup){
        this.mainViewGroup=mainViewGroup; // 保存主视图组引用
    }

    /** 自动 */
    /**
     * 设置自动模式
     *
     * @param isRun       是否运行自动模式
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Auto(boolean isRun,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUTO); // 设置消息标志为自动模式
            msgToUI.setParam(String.valueOf(isRun)); // 设置运行状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询当前是否为自动模式
     *
     * @return 是否处于自动模式
     */
    public boolean AutoQ(){
        return Scope.getInstance().isAuto(); // 从Scope单例查询自动模式状态
    }

    /** 使示波器开始运行，符合触发条件，开始采集数据 */
    /**
     * 启动示波器运行
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Run(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RUN); // 设置消息标志为运行
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 使示波器停止运行，数据采集停止 */
    /**
     * 停止示波器运行
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Stop(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_STOP); // 设置消息标志为停止
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 将示波器设置为单序列，示波器捕获并显示单次采集 */
    /**
     * 设置单次触发模式
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Single(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_SINGLE); // 设置消息标志为单次
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 将示波器设置为连续触发方式 */
    /**
     * 设置连续触发模式
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Multiple(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MULTIPLE); // 设置消息标志为连续触发
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置示波器的蜂鸣状态 */
    /**
     * 设置蜂鸣器状态
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Beep(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_BEEP); // 设置消息标志为蜂鸣
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 将通道位置设置为垂直零点位置（波形显示区垂直中心） */
    /**
     * 将指定通道位置归零到垂直中心
     *
     * @param chIndex     通道索引（0起始）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Half_Channel(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_HALF_CHANNEL); // 设置消息标志为通道归零
            msgToUI.setParam(String.valueOf(chIndex+1)); // 通道索引+1转为1起始后设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置触发位置到屏幕中间 */
    /**
     * 将触发位置设置到屏幕中间
     *
     * @param chIndex     通道索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void TrigPos(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_TRIGPOS); // 设置消息标志为触发位置居中
            msgToUI.setParam(String.valueOf(chIndex)); // 设置通道索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置通道的垂直光标在50%处 */
    /**
     * 将垂直光标设置到50%位置
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Xcursor(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_XCURSOR); // 设置消息标志为垂直光标居中
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置通道的水平光标在50%处 */
    /**
     * 将水平光标设置到50%位置
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Ycursor(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_YCURSOR); // 设置消息标志为水平光标居中
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 将触发电平设置为触发信号幅值的中间位置 */
    /**
     * 将触发电平设置为信号幅值的中间位置
     *
     * @param chIndex     通道索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Level(int chIndex,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_LEVEL); // 设置消息标志为电平居中
            msgToUI.setParam(String.valueOf(chIndex)); // 设置通道索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置示波器回到主界面 */
    /**
     * 返回示波器主界面
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void HomePage(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_HOMEPAGE); // 设置消息标志为主页
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 设置退出示波器程序，返回主界面 */
    /**
     * 退出示波器程序，返回系统主界面
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Return(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_RETURN); // 设置消息标志为返回
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 锁定示波器屏幕 */
    /**
     * 锁定或解锁示波器屏幕
     *
     * @param isLock      是否锁定屏幕
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Lock(boolean isLock,boolean isUpdateUI){
        if (isLock) { // 判断是否锁定
            ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_KEY); // 锁定屏幕
        }else { // 解锁
            ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_KEY); // 解锁屏幕
        }
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_LOCK); // 设置消息标志为锁屏
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询屏幕锁定状态
     *
     * @param isUpdateUI  是否同步更新UI界面
     * @return 屏幕是否锁定
     */
    public boolean LockQ(boolean isUpdateUI){
        return ScreenControls.getInstance().isLockScreen(); // 查询屏幕锁定状态
    }

    /** 解锁示波器屏幕 */
    /**
     * 解锁示波器屏幕
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Unlock(boolean isUpdateUI){
        ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_KEY); // 解锁屏幕
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_UNLOCK); // 设置消息标志为解锁
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 频率计的打开与关闭 */
    /**
     * 设置频率计的信号源通道
     *
     * @param chIndex     通道索引（0起始）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Counter(int chIndex,boolean isUpdateUI){
        int ch=chIndex+1; // 通道索引转为1起始
        if (ch> TChan.MaxLogicChan) ch=0; // 超出最大逻辑通道数则设为0
        if (freSource == ch) return; // 频率计源未变化则直接返回
        this.freSource =ch; // 保存频率计源
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_COUNTER); // 设置消息标志为频率计
            msgToUI.setParam(String.valueOf(ch)); // 设置通道参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 频率计的打开与关闭查询 */
    /**
     * 查询频率计信号源通道
     *
     * @return 频率计源通道索引
     */
    public  int CounterQ(){
        return this.freSource; // 返回频率计源
    }

    /** 恢复出厂设置 */
    /**
     * 恢复出厂设置
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Reset(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_FACTORYRESET); // 设置消息标志为出厂重置
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 打开测量菜单 */
    /**
     * 打开测量菜单
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Measure(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_MEASURE); // 设置消息标志为测量菜单
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /** 打开触发菜单 */
    /**
     * 打开触发菜单
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Trigger(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_TRIGGER); // 设置消息标志为触发菜单
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 设置通道开关状态
     *
     * @param ch          通道索引
     * @param isOpen      是否打开通道
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Channel(int ch,boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_CHANNEL); // 设置消息标志为通道开关
            msgToUI.setParam(String.valueOf(ch)+CommandMsgToUI.PARAM_SPLIT+String.valueOf(isOpen)); // 设置通道索引和开关状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询通道是否显示
     *
     * @param ch  通道索引（0起始）
     * @return 通道是否可见
     */
    public boolean ChannelQ(int ch){
        int slip=-1; // 侧滑面板索引，-1表示无效
        switch (ch) // 根据通道索引映射到对应的侧滑面板
        {
            case TChan.Ch1-1:slip=MainViewGroup.RIGHTSLIP_CH1;break; // CH1通道
            case TChan.Ch2-1:slip=MainViewGroup.RIGHTSLIP_CH2;break; // CH2通道
            case TChan.Ch3-1:slip=MainViewGroup.RIGHTSLIP_CH3;break; // CH3通道
            case TChan.Ch4-1:slip=MainViewGroup.RIGHTSLIP_CH4;break; // CH4通道
            case TChan.Ch5-1:slip=MainViewGroup.RIGHTSLIP_CH5;break; // CH5通道
            case TChan.Ch6-1:slip=MainViewGroup.RIGHTSLIP_CH6;break; // CH6通道
            case TChan.Ch7-1:slip=MainViewGroup.RIGHTSLIP_CH7;break; // CH7通道
            case TChan.Ch8-1:slip=MainViewGroup.RIGHTSLIP_CH8;break; // CH8通道
            case TChan.Math1-1:slip=MainViewGroup.RIGHTSLIP_MATH1;break; // Math1通道
            case TChan.Math2-1:slip=MainViewGroup.RIGHTSLIP_MATH2;break; // Math2通道
            case TChan.Math3-1:slip=MainViewGroup.RIGHTSLIP_MATH3;break; // Math3通道
            case TChan.Math4-1:slip=MainViewGroup.RIGHTSLIP_MATH4;break; // Math4通道
            case TChan.Math5-1:slip=MainViewGroup.RIGHTSLIP_MATH5;break; // Math5通道
            case TChan.Math6-1:slip=MainViewGroup.RIGHTSLIP_MATH6;break; // Math6通道
            case TChan.Math7-1:slip=MainViewGroup.RIGHTSLIP_MATH7;break; // Math7通道
            case TChan.Math8-1:slip=MainViewGroup.RIGHTSLIP_MATH8;break; // Math8通道
            case TChan.R1-1:slip=MainViewGroup.RIGHTSLIP_REF1;break; // REF1通道
            case TChan.R2-1:slip=MainViewGroup.RIGHTSLIP_REF2;break; // REF2通道
            case TChan.R3-1:slip=MainViewGroup.RIGHTSLIP_REF3;break; // REF3通道
            case TChan.R4-1:slip=MainViewGroup.RIGHTSLIP_REF4;break; // REF4通道
            case TChan.R5-1:slip=MainViewGroup.RIGHTSLIP_REF5;break; // REF5通道
            case TChan.R6-1:slip=MainViewGroup.RIGHTSLIP_REF6;break; // REF6通道
            case TChan.R7-1:slip=MainViewGroup.RIGHTSLIP_REF7;break; // REF7通道
            case TChan.R8-1:slip=MainViewGroup.RIGHTSLIP_REF8;break; // REF8通道
            case TChan.S1-1:slip=MainViewGroup.RIGHTSLIP_S1;break; // S1通道
            case TChan.S2-1:slip=MainViewGroup.RIGHTSLIP_S2;break; // S2通道
            case TChan.S3-1:slip=MainViewGroup.RIGHTSLIP_S3;break; // S3通道
            case TChan.S4-1:slip=MainViewGroup.RIGHTSLIP_S4;break; // S4通道
        }
        if (slip==-1)return false; // 无效通道索引，返回false
        return mainViewGroup.isSlipShow(slip); // 查询侧滑面板是否可见
    }

    /**
     * 设置快捷菜单的开关
     *
     * @param isOpen      是否打开快捷菜单
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Quick(boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_QuickBottom); // 设置消息标志为快捷菜单
            msgToUI.setParam(String.valueOf(isOpen)); // 设置开关状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询快捷菜单是否显示
     *
     * @return 快捷菜单是否可见
     */
    public boolean QuickQ(){
        return mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP); // 查询底部侧滑面板可见性
    }

    /**
     * 设置主菜单的开关
     *
     * @param isOpen      是否打开主菜单
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Main(boolean isOpen,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_Main); // 设置消息标志为主菜单
            msgToUI.setParam(String.valueOf(isOpen)); // 设置开关状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询主菜单是否显示
     *
     * @return 主菜单是否可见
     */
    public boolean MainQ(){
        return mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP); // 查询顶部侧滑面板可见性
    }


    /** SCPI指令未实现  通道选择条的打开与关闭 */
    /**
     * 设置通道选择条的显示状态（SCPI指令未实现）
     *
     * @param isOpen      是否打开通道选择条
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void ChannelSelector(boolean isOpen,boolean isUpdateUI){
         if (channelSelectorVisible !=isOpen){ // 状态发生变化才处理
             channelSelectorVisible =isOpen; // 更新通道选择条可见性
             if (isUpdateUI){ // 判断是否需要更新UI
                 CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
                 msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_CHANNELSELECTOR); // 设置消息标志为通道选择条
                 RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
             }
         }
    }

    /** SCPI指令未实现  */
    /**
     * 查询通道选择条是否显示（SCPI指令未实现）
     *
     * @return 通道选择条是否可见
     */
    public boolean ChannelSelectorQ(){
        return channelSelectorVisible; // 返回通道选择条可见性
    }

    /**
     * 设置辅助触发源索引
     *
     * @param idx         触发源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void aux_trigger(int idx,boolean isUpdateUI){
        idxAux_trigger=idx; // 保存辅助触发源索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_TRIGGER); // 设置消息标志为辅助触发
            msgToUI.setParam(String.valueOf(idx)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询辅助触发源索引
     *
     * @return 辅助触发源索引
     */
    public int aux_triggerQ(){
        return idxAux_trigger; // 返回辅助触发源索引
    }

    /**
     * 设置辅助时钟索引
     *
     * @param idx         时钟索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void aux_clock(int idx,boolean isUpdateUI){
        idxAux_clock=idx; // 保存辅助时钟索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_CLOCK); // 设置消息标志为辅助时钟
            msgToUI.setParam(String.valueOf(idx)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询辅助时钟状态
     *
     * @return 1表示时钟输出，0表示时钟输入
     */
    public int aux_clockQ(){
        return Sample.getInstance().isClkInOut()?1:0; // 从Sample单例查询时钟输入输出状态
        //return idxAux_clock;
    }

    /**
     * 设置辅助输入阻抗索引
     *
     * @param idx         输入阻抗索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void aux_inputres(int idx,boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MENU_AUX_INPUTRES); // 设置消息标志为辅助输入阻抗
            msgToUI.setParam(String.valueOf(idx)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询辅助输入阻抗
     *
     * @return 输入阻抗索引
     */
    public int aux_inputresQ(){
        return TriggerFactory.getInstance().getTriggerCommon().getExtTriggerInputRes(); // 从触发器工厂查询外部触发输入阻抗
    }

}
