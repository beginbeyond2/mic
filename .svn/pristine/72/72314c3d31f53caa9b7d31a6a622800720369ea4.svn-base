package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Menu {
//      new SCPICommandStruct(":MENU:AUTO","SCPI_Menu","Auto"),//自动
//       new SCPICommandStruct(":MENU:RUN","SCPI_Menu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
//       new SCPICommandStruct(":MENU:STOP","SCPI_Menu","Stop"),//使示波器停止运行，数据采集停止
//       new SCPICommandStruct(":MENU:SINGle","SCPI_Menu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
//       new SCPICommandStruct(":MENU:MULTiple","SCPI_Menu","Multiple"),//将示波器设置为连续触发方式
//       new SCPICommandStruct(":MENU:BEEP","SCPI_Menu","Beep"),//设置示波器的蜂鸣状态
//       new SCPICommandStruct(":MENU:HALF:CHANnel","SCPI_Menu","Half_Channel"),//将通道位置设置为垂直零点位置（波形显示区垂直中心）
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
//       new SCPICommandStruct(":MENU:MEASure","SCPI_Menu","MeasureBean"),//打开测量菜单
//       new SCPICommandStruct(":MENU:TRIGger","SCPI_Menu","Trigger"),//打开触发菜单
//       new SCPICommandStruct(":MENU:CHANnel","SCPI_Menu","Channel"),//打开通道
//       new SCPICommandStruct(":MENU:CHANnel?","SCPI_Menu","ChannelQ"),//打开通道
//       new SCPICommandStruct(":MENU:QUICk","SCPI_Menu","Quick"),//打开通道
//       new SCPICommandStruct(":MENU:QUICk?","SCPI_Menu","QuickQ"),//打开通道
//       new SCPICommandStruct(":MENU:MAIN","SCPI_Menu","Main"),//打开通道
//       new SCPICommandStruct(":MENU:MAIN?","SCPI_Menu","MainQ"),//打开通道

    public static void Auto(SCPIParam param) {
        System.out.println(":MENU:AUTO");
        Command.get().getMenu().Auto(param.bParam1, true);
    }
    public static String AutoQ(SCPIParam param) {
        boolean b= Command.get().getMenu().AutoQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Run(SCPIParam param) {
        Command.get().getFunctionMenu().Run(true);
    }

    public static void Stop(SCPIParam param) {
        Command.get().getFunctionMenu().Stop(true);
    }

    public static void Single(SCPIParam param) {
        Command.get().getFunctionMenu().Single(true);
    }

    public static void Multiple(SCPIParam param) {
        Command.get().getFunctionMenu().Multiple(true);
    }

    public static void Beep(SCPIParam param) {
        Command.get().getFunctionMenu().Beep(true);
    }

    public static void Half_Channel(SCPIParam param) {
        Command.get().getMenu().Half_Channel(param.iParam1,true);
    }

    public static void TrigPos(SCPIParam param) {
        Command.get().getMenu().TrigPos(param.iParam1,true);
    }

    public static void Xcursor(SCPIParam param) {
        Command.get().getMenu().Xcursor(true);
    }

    public static void Ycursor(SCPIParam param) {
        Command.get().getMenu().Ycursor(true);
    }

    public static void Level(SCPIParam param) {
        Command.get().getMenu().Level(param.iParam1,true);
    }

    public static void HomePage(SCPIParam param) {

        Command.get().getMenu().HomePage(true);
    }

    public static void Return(SCPIParam param) {
        Command.get().getMenu().Return(true);
    }

    public static void Lock(SCPIParam param) {
        Command.get().getMenu().Lock(param.bParam1,true);
    }
    public static String LockQ(SCPIParam param){
        boolean b= Command.get().getMenu().LockQ(true);
        return ToolsSCPI.getOpenState(b);
    }

    public static void Unlock(SCPIParam param) {
        Command.get().getMenu().Unlock(true);
    }

    public static void Counter(SCPIParam param) {
        Command.get().getMenu().Counter(param.iParam1,true);
    }

    public static void CounterQ(SCPIParam param) {
        Command.get().getMenu().CounterQ();
    }

    public static void Reset(SCPIParam param) {
        Command.get().getMenu().Reset(true);
    }

    public static void Measure(SCPIParam param) {
        Command.get().getMenu().Measure(true);
    }

    public static void Trigger(SCPIParam param) {
        Command.get().getMenu().Trigger(true);
    }

    public static void Channel(SCPIParam param){
        Command.get().getMenu().Channel(param.iParam1, param.bParam1, true);
    }
    public static String ChannelQ(SCPIParam param){
        boolean b=Command.get().getMenu().ChannelQ(param.iParam1);
        return ToolsSCPI.getOpenState(b);
    }
    public static void Quick(SCPIParam param){
        Command.get().getMenu().Quick(param.bParam1, true);
    }
    public static String QuickQ(SCPIParam param){
        boolean b=Command.get().getMenu().QuickQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Main(SCPIParam param){
        Command.get().getMenu().Main(param.bParam1, true);
    }
    public static String MainQ(SCPIParam param){
        boolean b=Command.get().getMenu().MainQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Aux_trigger(SCPIParam param){
        Command.get().getMenu().aux_trigger(param.iParam1,true);
    }
    public static String Aux_triggerQ(SCPIParam param){
       int i= Command.get().getMenu().aux_triggerQ();
       return ToolsSCPI.getAux(i);
    }
    public static void Aux_clock(SCPIParam param){
        Command.get().getMenu().aux_clock(param.iParam1,true);
    }
    public static String Aux_clockQ(SCPIParam param){
        int i= Command.get().getMenu().aux_clockQ();
        return ToolsSCPI.getAux(i);
    }
    public static void Aux_Inputres(SCPIParam param){
        Command.get().getMenu().aux_inputres(param.iParam1,true);
    }
    public static String Aux_InputresQ(SCPIParam param){
        int idx= Command.get().getMenu().aux_inputresQ();
        return ToolsSCPI.getInputres(idx);
    }

}
