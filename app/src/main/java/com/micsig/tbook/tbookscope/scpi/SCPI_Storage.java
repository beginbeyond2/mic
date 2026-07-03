package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import android.util.Log; // 导入Android日志工具

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 存储/截图/录制子模块                            |
 * | 核心职责：处理SCPI协议中:STORage相关命令（波形存储/载入/截图/录制/回放等）      |
 * | 架构设计：静态方法类，委托Command中间件的Storage接口执行实际操作               |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getStorage()             |
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程保存/载入波形、截图、录制回放、设置存储参数                     |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Storage {
//     new SCPICommandStruct(":STORage:SAVE","SCPI_Storage","Save"),//存储指定通道的波形到指定位置
//            new SCPICommandStruct(":STORage:LOAD","SCPI_Storage","Load"),//载入ref
//            new SCPICommandStruct(":STORage:CAPTure","SCPI_Storage","Capture"),//屏幕截图
//    new SCPICommandStruct(":STORage:CAPTure:TIME","SCPI_Storage","Capture_Time"),//屏幕截图
//            new SCPICommandStruct(":STORage:CAPTure:TIME?","SCPI_Storage","Capture_TimeQ"),//屏幕截图
//            new SCPICommandStruct(":STORage:CAPTure:INCOlor","SCPI_Storage","Capture_Incolor"),//屏幕截图
//            new SCPICommandStruct(":STORage:CAPTure:INCOlorQ","SCPI_Storage","Capture_IncolorQ"),//屏幕截图
//            new SCPICommandStruct(":STORage:CAPTure:STARt","SCPI_Storage","Capture_Start"),//屏幕截图
//            new SCPICommandStruct(":STORage:DEPTh","SCPI_Storage","Depth"),//设置示波器存储深度
//            new SCPICommandStruct(":STORage:DEPTh?","SCPI_Storage","DepthQ"),//查询示波器存储深度
//            new SCPICommandStruct(":STORage:CONSave:FILename","SCPI_Storage","ConSave"),//存储示波器设置
//            new SCPICommandStruct(":STORage:CONSave:STARt","SCPI_Storage","ConSave_start"),//存储示波器设置
//            new SCPICommandStruct(":STORage:CONLoad:FILename","SCPI_Storage","ConLoad"),//调用示波器设置
//            new SCPICommandStruct(":STORage:RECord","SCPI_Storage","Record"),//设置示波器录制功能的打开与关闭
//            new SCPICommandStruct(":STORage:RECord?","SCPI_Storage","RecordQ"),//查询示波器录制功能的打开与关闭
//            new SCPICommandStruct(":STORage:PLAY","SCPI_Storage","Play"),//设置示波器回放功能的打开和关闭
//            new SCPICommandStruct(":STORage:PLAY?","SCPI_Storage","PlayQ"),//查询示波器回放功能的打开和关闭
//            new SCPICommandStruct(":STORage:PLAY:SPEed","SCPI_Storage","Play_Speed"),//设置示波器回放快进选项
//            new SCPICommandStruct(":STORage:PLAY:SPEed?","SCPI_Storage","Play_SpeedQ"),//查询示波器回放快进选项
//            new SCPICommandStruct(":STORage:PLAY:BACK","SCPI_Storage","Play_Back"),//设置示波器回放后退选项
//            new SCPICommandStruct(":STORage:PLAY:BACK?","SCPI_Storage","Play_backQ"),//查询示波器回放后退选项
//            new SCPICommandStruct(":STORage:SAVE:SOURce", "SCPI_Storage","Save_Source"),
//            new SCPICommandStruct(":STORage:SAVE:SOURce?", "SCPI_Storage","Save_SourceQ"),
//            new SCPICommandStruct(":STORage:SAVE:LOCAtion", "SCPI_Storage","Save_Location"),
//            new SCPICommandStruct(":STORage:SAVE:LOCAtion?", "SCPI_Storage","Save_LocationQ"),
//            new SCPICommandStruct(":STORage:SAVE:TYPE", "SCPI_Storage","Save_Type"),
//            new SCPICommandStruct(":STORage:SAVE:TYPE?", "SCPI_Storage","Save_TypeQ"),
//            new SCPICommandStruct(":STORage:SAVE:FILename","SCPI_Storage","Save_Filename"),
//            new SCPICommandStruct(":STORage:SAVE:FILename?", "SCPI_Storage","Save_FilenameQ"),
//            new SCPICommandStruct(":STORage:SAVE:START", "SCPI_Storage","Save_Start"),
//            new SCPICommandStruct(":STORage:SAVE:ALLSegments", "SCPI_Storage","Save_ALLSegments"),
//            new SCPICommandStruct(":STORage:SAVE:ALLSegments?", "SCPI_Storage","Save_ALLSegmentsQ"),

    /**
     * 保存波形到指定位置
     * @param param SCPI参数封装，iParam1为信源，iParam2为位置
     */
    public static void Save(SCPIParam param) {
        Command.get().getStorage().Save(param.iParam1, param.iParam2, true); // 委托Command中间件保存波形，true表示通知UI刷新
    }

    /**
     * 载入参考波形
     * @param param SCPI参数封装，iParam1为通道索引，sParam1为文件名，bParam1为参数
     */
    public static void Load(SCPIParam param) {
        Command.get().getStorage().Load(param.iParam1, param.sParam1, param.bParam1, true); // 委托Command中间件载入波形，true表示通知UI刷新
    }

    /**
     * 执行屏幕截图
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Capture(SCPIParam param) {
        Command.get().getStorage().Capture(true); // 委托Command中间件执行截图，true表示通知UI刷新
    }

    /**
     * 设置截图是否添加时间戳
     * @param param SCPI参数封装，bParam1为true添加/false不添加
     */
    public static void Capture_Time(SCPIParam param) {
        Command.get().getStorage().Capture_Time(param.bParam1,true); // 委托Command中间件设置截图时间戳，true表示通知UI刷新
    }

    /**
     * 查询截图时间戳设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String Capture_TimeQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_TimeQ(); // 从Command中间件获取时间戳设置
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置截图是否使用彩色
     * @param param SCPI参数封装，bParam1为true彩色/false灰度
     */
    public static void Capture_Incolor(SCPIParam param) {
        Command.get().getStorage().Capture_Incolor(param.bParam1,true); // 委托Command中间件设置截图颜色模式，true表示通知UI刷新
    }

    /**
     * 查询截图颜色模式设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String Capture_IncolorQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_IncolorQ(); // 从Command中间件获取颜色模式设置
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置截图是否保存缩略图
     * @param param SCPI参数封装，bParam1为true保存/false不保存
     */
    public static void Capture_Thumbnail(SCPIParam param) {
        Command.get().getStorage().Capture_Thumbnail(param.bParam1,true); // 委托Command中间件设置缩略图选项，true表示通知UI刷新
    }

    /**
     * 查询截图缩略图设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String Capture_ThumbnailQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_ThumbnailQ(); // 从Command中间件获取缩略图设置
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 立即执行截图
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Capture_Start(SCPIParam param) {
        Command.get().getStorage().Capture_Start(true); // 委托Command中间件立即截图，true表示通知UI刷新
    }

    /**
     * 设置存储深度（占位实现）
     * @param param SCPI参数封装
     */
    public static void Depth(SCPIParam param) {
    }

    /**
     * 查询存储深度（占位实现）
     * @param param SCPI参数封装
     */
    public static void DepthQ(SCPIParam param) {
    }

    /**
     * 保存示波器设置到指定文件
     * @param param SCPI参数封装，sParam1为文件名
     */
    public static void ConSave(SCPIParam param) {
        Command.get().getStorage().ConSave(param.sParam1.trim(), true); // 委托Command中间件保存设置，trim去除首尾空格
    }

    /**
     * 开始保存示波器设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void ConSave_start(SCPIParam param){
        Command.get().getStorage().ConSave_start(true); // 委托Command中间件开始保存设置，true表示通知UI刷新
    }

    /**
     * 载入指定文件的示波器设置
     * @param param SCPI参数封装，sParam1为文件名
     */
    public static void ConLoad(SCPIParam param) {
        Command.get().getStorage().ConLoad(param.sParam1.trim(), true); // 委托Command中间件载入设置，trim去除首尾空格
    }

    /**
     * 设置录制功能的开关
     * @param param SCPI参数封装，iParam1为开关索引
     */
    public static void Record(SCPIParam param) {
        Command.get().getStorage().Record(param.iParam1, true); // 委托Command中间件设置录制开关，true表示通知UI刷新
    }

    /**
     * 查询录制功能的开关状态（占位实现）
     * @param param SCPI参数封装
     */
    public static void RecordQ(SCPIParam param) {
    }

    /**
     * 设置回放功能的开关（占位实现）
     * @param param SCPI参数封装
     */
    public static void Play(SCPIParam param) {
    }

    /**
     * 查询回放功能的开关状态（占位实现）
     * @param param SCPI参数封装
     */
    public static void PlayQ(SCPIParam param) {
    }

    /**
     * 设置回放快进选项（占位实现）
     * @param param SCPI参数封装
     */
    public static void Play_Speed(SCPIParam param) {
    }

    /**
     * 查询回放快进选项（占位实现）
     * @param param SCPI参数封装
     */
    public static void Play_SpeedQ(SCPIParam param) {
    }

    /**
     * 设置回放后退选项（占位实现）
     * @param param SCPI参数封装
     */
    public static void Play_Back(SCPIParam param) {
    }

    /**
     * 查询回放后退选项（占位实现）
     * @param param SCPI参数封装
     */
    public static void Play_backQ(SCPIParam param) {
    }

    /**
     * 设置保存波形的信源通道
     * @param param SCPI参数封装，iParam1为信源索引
     */
    public static void Save_Source(SCPIParam param){
        Command.get().getStorage().Save_Source(param.iParam1, true); // 委托Command中间件设置保存信源，true表示通知UI刷新
    }

    /**
     * 查询保存波形的信源通道
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 信源名称字符串
     */
    public static String Save_SourceQ(SCPIParam param){
        int i=Command.get().getStorage().Save_SourceQ(); // 从Command中间件获取保存信源索引
        return ToolsSCPI.getChAll(i); // 将索引转换为信源名称字符串（含ALL选项）
    }

    /**
     * 设置保存位置（内部/USB等）
     * @param param SCPI参数封装，iParam1为位置索引
     */
    public static void Save_Location(SCPIParam param){
        Command.get().getStorage().Save_Location(param.iParam1, true); // 委托Command中间件设置保存位置，true表示通知UI刷新
    }

    /**
     * 查询保存位置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 位置名称字符串
     */
    public static String Save_LocationQ(SCPIParam param){
        int i=Command.get().getStorage().Save_LocationQ(); // 从Command中间件获取保存位置索引
        return ToolsSCPI.getLocal(i); // 将索引转换为位置名称字符串
    }

    /**
     * 设置保存文件类型（波形/设置/图片等）
     * @param param SCPI参数封装，iParam1为类型索引
     */
    public static void Save_Type(SCPIParam param){
        Command.get().getStorage().Save_Type(param.iParam1, true); // 委托Command中间件设置保存类型，true表示通知UI刷新
    }

    /**
     * 查询保存文件类型
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 文件类型名称字符串
     */
    public static String Save_TypeQ(SCPIParam param){
        int i=Command.get().getStorage().Save_TypeQ(); // 从Command中间件获取保存类型索引
        return ToolsSCPI.getSaveType(i); // 将索引转换为文件类型名称字符串
    }

    /**
     * 设置保存文件名
     * @param param SCPI参数封装，sParam1为文件名
     */
    public static void Save_Filename(SCPIParam param){
        Command.get().getStorage().Save_Filename(param.sParam1, true); // 委托Command中间件设置文件名，true表示通知UI刷新
    }

    /**
     * 查询保存文件名
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 文件名字符串
     */
    public static String Save_FilenameQ(SCPIParam param){
        return Command.get().getStorage().Save_FilenameQ(); // 从Command中间件获取当前文件名
    }

    /**
     * 开始执行保存操作
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Save_Start(SCPIParam param){
        Command.get().getStorage().Save_Start(); // 委托Command中间件开始保存
    }

    /**
     * 设置是否保存全部分段数据
     * @param param SCPI参数封装，bParam1为true保存全部/false保存当前段
     */
    public static void Save_ALLSegments(SCPIParam param){
        Command.get().getStorage().Save_ALLSegments(param.bParam1, true); // 委托Command中间件设置全段保存，true表示通知UI刷新
    }

    /**
     * 设置保存数据类型（多参数组合）
     * @param param SCPI参数封装，sParam1~sParam5为数据类型字段
     * @return 空字符串
     */
    public static String Save_DataType(SCPIParam param){
        String [] strArray = {param.sParam1,param.sParam2, param.sParam3,param.sParam4,param.sParam5}; // 组装数据类型参数数组
        boolean b=Command.get().getStorage().Save_DataType(strArray); // 委托Command中间件设置保存数据类型
        return ""; // 返回空字符串
    }

    /**
     * 查询保存数据的状态（进行中/已完成）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ing"表示进行中，"1"表示成功
     */
    public static String Save_DataStatusQ(SCPIParam param){
        boolean b = Command.get().getStorage().Save_DataStatusQ(); // 从Command中间件获取保存完成状态
        return !b ? "ing" : ToolsSCPI.getSuccState(b); // 未完成返回"ing"，完成返回成功状态
    }

    /**
     * 查询保存数据的CSV格式内容
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return CSV格式数据的StringBuilder
     */
    public static StringBuilder Save_DataCSVQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataCSVQ()); // 从Command中间件获取CSV数据并包装为StringBuilder
    }

    /**
     * 查询保存数据的PNG格式内容
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return PNG格式数据的StringBuilder
     */
    public static StringBuilder Save_DataPNGQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataPNGQ()); // 从Command中间件获取PNG数据并包装为StringBuilder
    }

    /**
     * 查询保存数据的MSS格式内容
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return MSS格式数据的StringBuilder
     */
    public static StringBuilder Save_DataMSSQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataMSSQ()); // 从Command中间件获取MSS数据并包装为StringBuilder
    }

}
