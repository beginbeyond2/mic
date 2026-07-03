package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 菜单/功能控制子模块                             |
 * | 核心职责：处理SCPI协议中:MENU相关命令（运行/停止/自动/锁定/恢复出厂等）        |
 * | 架构设计：静态方法类，委托Command中间件的Menu和FunctionMenu接口执行实际操作   |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getMenu()/getFunctionMenu()|
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程控制示波器运行状态、屏幕锁定、恢复出厂、通道开关、辅助接口设置  |
 * +---------------------------------------------------------------------------+
 *
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

    /**
     * 执行自动设置功能
     * @param param SCPI参数封装，bParam1为自动设置开关
     */
    public static void Auto(SCPIParam param) {
        System.out.println(":MENU:AUTO"); // 调试输出自动设置命令
        Command.get().getMenu().Auto(param.bParam1, true); // 委托Command中间件执行自动设置，true表示通知UI刷新
    }

    /**
     * 查询自动设置功能的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String AutoQ(SCPIParam param) {
        boolean b= Command.get().getMenu().AutoQ(); // 从Command中间件获取自动设置状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 使示波器开始运行采集数据
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Run(SCPIParam param) {
        Command.get().getFunctionMenu().Run(true); // 委托Command中间件启动运行，true表示通知UI刷新
    }

    /**
     * 使示波器停止运行，数据采集停止
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Stop(SCPIParam param) {
        Command.get().getFunctionMenu().Stop(true); // 委托Command中间件停止运行，true表示通知UI刷新
    }

    /**
     * 将示波器设置为单次(Single)触发模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Single(SCPIParam param) {
        Command.get().getFunctionMenu().Single(true); // 委托Command中间件设置单次触发，true表示通知UI刷新
    }

    /**
     * 将示波器设置为连续(Multiple)触发模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Multiple(SCPIParam param) {
        Command.get().getFunctionMenu().Multiple(true); // 委托Command中间件设置连续触发，true表示通知UI刷新
    }

    /**
     * 切换蜂鸣器开关
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Beep(SCPIParam param) {
        Command.get().getFunctionMenu().Beep(true); // 委托Command中间件切换蜂鸣器，true表示通知UI刷新
    }

    /**
     * 将指定通道位置归零到垂直中心
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Half_Channel(SCPIParam param) {
        Command.get().getMenu().Half_Channel(param.iParam1,true); // 委托Command中间件归零通道位置，true表示通知UI刷新
    }

    /**
     * 设置触发位置到屏幕水平中心
     * @param param SCPI参数封装，iParam1为参数
     */
    public static void TrigPos(SCPIParam param) {
        Command.get().getMenu().TrigPos(param.iParam1,true); // 委托Command中间件设置触发位置居中，true表示通知UI刷新
    }

    /**
     * 设置垂直光标到50%位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Xcursor(SCPIParam param) {
        Command.get().getMenu().Xcursor(true); // 委托Command中间件设置垂直光标居中，true表示通知UI刷新
    }

    /**
     * 设置水平光标到50%位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Ycursor(SCPIParam param) {
        Command.get().getMenu().Ycursor(true); // 委托Command中间件设置水平光标居中，true表示通知UI刷新
    }

    /**
     * 将触发电平设置为触发信号幅值的中间位置
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Level(SCPIParam param) {
        Command.get().getMenu().Level(param.iParam1,true); // 委托Command中间件设置触发电平居中，true表示通知UI刷新
    }

    /**
     * 返回示波器主界面
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void HomePage(SCPIParam param) {

        Command.get().getMenu().HomePage(true); // 委托Command中间件返回主界面，true表示通知UI刷新
    }

    /**
     * 退出当前菜单/程序，返回上一级
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Return(SCPIParam param) {
        Command.get().getMenu().Return(true); // 委托Command中间件执行返回操作，true表示通知UI刷新
    }

    /**
     * 锁定示波器屏幕（禁止触控操作）
     * @param param SCPI参数封装，bParam1为true锁定/false解锁
     */
    public static void Lock(SCPIParam param) {
        Command.get().getMenu().Lock(param.bParam1,true); // 委托Command中间件设置屏幕锁定，true表示通知UI刷新
    }

    /**
     * 查询屏幕锁定状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String LockQ(SCPIParam param){
        boolean b= Command.get().getMenu().LockQ(true); // 从Command中间件获取屏幕锁定状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 解锁示波器屏幕
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Unlock(SCPIParam param) {
        Command.get().getMenu().Unlock(true); // 委托Command中间件解锁屏幕，true表示通知UI刷新
    }

    /**
     * 设置频率计的开关状态
     * @param param SCPI参数封装，iParam1为开关状态索引
     */
    public static void Counter(SCPIParam param) {
        Command.get().getMenu().Counter(param.iParam1,true); // 委托Command中间件设置频率计开关，true表示通知UI刷新
    }

    /**
     * 查询频率计的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void CounterQ(SCPIParam param) {
        Command.get().getMenu().CounterQ(); // 委托Command中间件查询频率计状态
    }

    /**
     * 恢复出厂设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Reset(SCPIParam param) {
        Command.get().getMenu().Reset(true); // 委托Command中间件执行恢复出厂设置，true表示通知UI刷新
    }

    /**
     * 打开测量菜单
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Measure(SCPIParam param) {
        Command.get().getMenu().Measure(true); // 委托Command中间件打开测量菜单，true表示通知UI刷新
    }

    /**
     * 打开触发菜单
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Trigger(SCPIParam param) {
        Command.get().getMenu().Trigger(true); // 委托Command中间件打开触发菜单，true表示通知UI刷新
    }

    /**
     * 打开或关闭指定通道
     * @param param SCPI参数封装，iParam1为通道索引，bParam1为true打开/false关闭
     */
    public static void Channel(SCPIParam param){
        Command.get().getMenu().Channel(param.iParam1, param.bParam1, true); // 委托Command中间件设置通道开关，true表示通知UI刷新
    }

    /**
     * 查询指定通道的开关状态
     * @param param SCPI参数封装，iParam1为通道索引
     * @return "ON"或"OFF"
     */
    public static String ChannelQ(SCPIParam param){
        boolean b=Command.get().getMenu().ChannelQ(param.iParam1); // 从Command中间件获取通道开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭快速菜单
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Quick(SCPIParam param){
        Command.get().getMenu().Quick(param.bParam1, true); // 委托Command中间件设置快速菜单开关，true表示通知UI刷新
    }

    /**
     * 查询快速菜单的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String QuickQ(SCPIParam param){
        boolean b=Command.get().getMenu().QuickQ(); // 从Command中间件获取快速菜单状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭主菜单
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Main(SCPIParam param){
        Command.get().getMenu().Main(param.bParam1, true); // 委托Command中间件设置主菜单开关，true表示通知UI刷新
    }

    /**
     * 查询主菜单的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String MainQ(SCPIParam param){
        boolean b=Command.get().getMenu().MainQ(); // 从Command中间件获取主菜单状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置辅助触发源
     * @param param SCPI参数封装，iParam1为触发源索引
     */
    public static void Aux_trigger(SCPIParam param){
        Command.get().getMenu().aux_trigger(param.iParam1,true); // 委托Command中间件设置辅助触发源，true表示通知UI刷新
    }

    /**
     * 查询辅助触发源
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 辅助触发源名称字符串
     */
    public static String Aux_triggerQ(SCPIParam param){
       int i= Command.get().getMenu().aux_triggerQ(); // 从Command中间件获取辅助触发源索引
       return ToolsSCPI.getAux(i); // 将索引转换为辅助源名称字符串
    }

    /**
     * 设置辅助时钟源
     * @param param SCPI参数封装，iParam1为时钟源索引
     */
    public static void Aux_clock(SCPIParam param){
        Command.get().getMenu().aux_clock(param.iParam1,true); // 委托Command中间件设置辅助时钟源，true表示通知UI刷新
    }

    /**
     * 查询辅助时钟源
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 辅助时钟源名称字符串
     */
    public static String Aux_clockQ(SCPIParam param){
        int i= Command.get().getMenu().aux_clockQ(); // 从Command中间件获取辅助时钟源索引
        return ToolsSCPI.getAux(i); // 将索引转换为辅助源名称字符串
    }

    /**
     * 设置辅助输入阻抗
     * @param param SCPI参数封装，iParam1为阻抗索引
     */
    public static void Aux_Inputres(SCPIParam param){
        Command.get().getMenu().aux_inputres(param.iParam1,true); // 委托Command中间件设置输入阻抗，true表示通知UI刷新
    }

    /**
     * 查询辅助输入阻抗
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 输入阻抗名称字符串
     */
    public static String Aux_InputresQ(SCPIParam param){
        int idx= Command.get().getMenu().aux_inputresQ(); // 从Command中间件获取输入阻抗索引
        return ToolsSCPI.getInputres(idx); // 将索引转换为阻抗名称字符串
    }

}
