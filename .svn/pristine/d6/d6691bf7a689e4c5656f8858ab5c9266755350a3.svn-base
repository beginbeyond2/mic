package com.micsig.tbook.tbookscope.scpi;

import android.util.Log;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
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

    public static void Save(SCPIParam param) {
        Command.get().getStorage().Save(param.iParam1, param.iParam2, true);
    }

    public static void Load(SCPIParam param) {
        Command.get().getStorage().Load(param.iParam1, param.sParam1, param.bParam1, true);
    }

    public static void Capture(SCPIParam param) {
        Command.get().getStorage().Capture(true);
    }

    public static void Capture_Time(SCPIParam param) {
        Command.get().getStorage().Capture_Time(param.bParam1,true);
    }
    public static String Capture_TimeQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_TimeQ();
        return ToolsSCPI.getOpenState(b);
    }
    public static void Capture_Incolor(SCPIParam param) {
        Command.get().getStorage().Capture_Incolor(param.bParam1,true);
    }
    public static String Capture_IncolorQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_IncolorQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Capture_Thumbnail(SCPIParam param) {
        Command.get().getStorage().Capture_Thumbnail(param.bParam1,true);
    }
    public static String Capture_ThumbnailQ(SCPIParam param) {
        boolean b=Command.get().getStorage().Capture_ThumbnailQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Capture_Start(SCPIParam param) {
        Command.get().getStorage().Capture_Start(true);
    }

    public static void Depth(SCPIParam param) {
    }

    public static void DepthQ(SCPIParam param) {
    }

    public static void ConSave(SCPIParam param) {
        Command.get().getStorage().ConSave(param.sParam1.trim(), true);
    }

    public static void ConSave_start(SCPIParam param){
        Command.get().getStorage().ConSave_start(true);
    }
    public static void ConLoad(SCPIParam param) {
        Command.get().getStorage().ConLoad(param.sParam1.trim(), true);
    }

    public static void Record(SCPIParam param) {
        Command.get().getStorage().Record(param.iParam1, true);
    }

    public static void RecordQ(SCPIParam param) {
    }

    public static void Play(SCPIParam param) {
    }

    public static void PlayQ(SCPIParam param) {
    }

    public static void Play_Speed(SCPIParam param) {
    }

    public static void Play_SpeedQ(SCPIParam param) {
    }

    public static void Play_Back(SCPIParam param) {
    }

    public static void Play_backQ(SCPIParam param) {
    }

    public static void Save_Source(SCPIParam param){
        Command.get().getStorage().Save_Source(param.iParam1, true);
    }
    public static String Save_SourceQ(SCPIParam param){
        int i=Command.get().getStorage().Save_SourceQ();
        return ToolsSCPI.getChAll(i);
    }
    public static void Save_Location(SCPIParam param){
        Command.get().getStorage().Save_Location(param.iParam1, true);
    }
    public static String Save_LocationQ(SCPIParam param){
        int i=Command.get().getStorage().Save_LocationQ();
        return ToolsSCPI.getLocal(i);
    }
    public static void Save_Type(SCPIParam param){
        Command.get().getStorage().Save_Type(param.iParam1, true);
    }
    public static String Save_TypeQ(SCPIParam param){
        int i=Command.get().getStorage().Save_TypeQ();
        return ToolsSCPI.getSaveType(i);
    }
    public static void Save_Filename(SCPIParam param){
        Command.get().getStorage().Save_Filename(param.sParam1, true);
    }
    public static String Save_FilenameQ(SCPIParam param){
        return Command.get().getStorage().Save_FilenameQ();
    }
    public static void Save_Start(SCPIParam param){
        Command.get().getStorage().Save_Start();
    }

    public static void Save_ALLSegments(SCPIParam param){
        Command.get().getStorage().Save_ALLSegments(param.bParam1, true);
    }

    public static String Save_DataType(SCPIParam param){
        String [] strArray = {param.sParam1,param.sParam2, param.sParam3,param.sParam4,param.sParam5};
        boolean b=Command.get().getStorage().Save_DataType(strArray);
        return "";
    }

    public static String Save_DataStatusQ(SCPIParam param){
        boolean b = Command.get().getStorage().Save_DataStatusQ();
        return !b ? "ing" : ToolsSCPI.getSuccState(b);
    }

    public static StringBuilder Save_DataCSVQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataCSVQ());
    }

    public static StringBuilder Save_DataPNGQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataPNGQ());
    }

    public static StringBuilder Save_DataMSSQ(SCPIParam param){
        return new StringBuilder(Command.get().getStorage().Save_DataMSSQ());
    }

}
