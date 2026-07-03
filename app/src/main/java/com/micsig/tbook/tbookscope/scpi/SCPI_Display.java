package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 命令中间件，用于获取底层显示操作接口

/*
 * +=============================================================================+
 * |  模块定位：SCPI显示控制命令处理层                                             |
 * |  核心职责：将SCPI协议中:DISPlay子系统的命令解析并转发至底层显示中间件             |
 * |  架构设计：纯静态方法类，作为SCPI命令分发器与Command中间件之间的桥接层           |
 * |  数据流向：SCPI解析器 → SCPI_Display → Command.get().getDisplay() → 底层      |
 * |  依赖关系：依赖SCPIParam(参数封装)、Command(命令中间件)、ToolsSCPI(工具类)     |
 * |  使用场景：示波器显示参数控制，包括波形显示方式、背景色、亮度、网格类型、       |
 * |           网格亮度、余辉模式/时间/清除、FFT余辉、高刷新模式、                  |
 * |           水平展开中心、ZOOM开关、CCT(色温)控制等                              |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Display {
//      new SCPICommandStruct(":DISPlay:WAVeform","SCPI_Display","WaveForm"),//设置屏幕中波形的显示方式
//            new SCPICommandStruct(":DISPlay:WAVeform?","SCPI_Display","WaveFormQ"),//查询屏幕中波形的显示方式
//            new SCPICommandStruct(":DISPlay:BRIGhtness","SCPI_Display","Brightness"),//设置屏幕中波形显示的亮度
//            new SCPICommandStruct(":DISPlay:BRIGhtness?","SCPI_Display","BrightnessQ"),//查询屏幕中波形显示的亮度
//            new SCPICommandStruct(":DISPlay:GRATicule","SCPI_Display","Graticule"),//设置屏幕显示的网格类型
//            new SCPICommandStruct(":DISPlay:GRATicule?","SCPI_Display","GraticuleQ"),//查询屏幕显示的网格类型
//            new SCPICommandStruct(":DISPlay:INTensity","SCPI_Display","Intensity"),//设置屏幕中网格显示的亮度
//            new SCPICommandStruct(":DISPlay:INTensity?","SCPI_Display","IntensityQ"),//查询屏幕中网格显示的亮度
//            new SCPICommandStruct(":DISPlay:PERSist:MODE","SCPI_Display","Persist_Mode"),//设置余辉显示模式
//            new SCPICommandStruct(":DISPlay:PERSist:MODE?","SCPI_Display","Persist_ModeQ"),//查询余辉显示模式
//            new SCPICommandStruct(":DISPlay:PERSist:ADJust","SCPI_Display","Persist_Adjust"),//设置余辉普通显示模式下余辉时间
//            new SCPICommandStruct(":DISPlay:PERSist:ADJust?","SCPI_Display","Persist_AdjustQ"),//查询余辉普通显示模式下余辉时间
//            new SCPICommandStruct(":DISPlay:PERSist:CLEar","SCPI_Display","Persist_Clear"),//清除余辉显示
//            new SCPICommandStruct(":DISPlay:HIGH","SCPI_Display","High"),//打开或关闭高刷新
//            new SCPICommandStruct(":DISPlay:HIGH?","SCPI_Display","HighQ"),//查询高刷新打开或关闭
//            new SCPICommandStruct(":DISPlay:HORRef","SCPI_Display","HorRef"),//设置屏幕水平展开中心模式
//            new SCPICommandStruct(":DISPlay:HORRef?","SCPI_Display","HorRefQ"),//查询屏幕水平展开中心模式
//            new SCPICommandStruct(":DISPlay:ZOOM","SCPI_Display","Zoom"),//打开或关闭ZOOM
//            new SCPICommandStruct(":DISPlay:ZOOM?","SCPI_Display","ZoomQ"),//查询ZOOM打开或关闭
//            new SCPICommandStruct(":DISPlay:CCT","SCPI_Display","CCT"),//打开或关闭ZOOM
//            new SCPICommandStruct(":DISPlay:CCT?","SCPI_Display","CCTQ"),//查询ZOOM打开或关闭

    /**
     * 设置屏幕中波形的显示方式（点/线/矢量等）
     * @param param SCPI命令参数，iParam1为波形显示方式
     */
    public static void WaveForm(SCPIParam param) {
        Command.get().getDisplay().WaveForm(param.iParam1, true); // 调用底层接口设置波形显示方式
    }

    /**
     * 查询屏幕中波形的显示方式
     * @param param SCPI命令参数（本命令无参数）
     * @return 波形显示方式字符串
     */
    public static String WaveFormQ(SCPIParam param) {
        int i=Command.get().getDisplay().WaveFormQ(); // 调用底层接口查询波形显示方式
        return ToolsSCPI.getDisplayWaveForm(i); // 将整数类型值转换为波形显示方式字符串
    }

    /**
     * 设置屏幕背景色
     * @param param SCPI命令参数，iParam1为背景色类型
     */
    public static void Background(SCPIParam param){
        Command.get().getDisplay().Background(param.iParam1,true); // 调用底层接口设置背景色
    }

    /**
     * 查询屏幕背景色
     * @param param SCPI命令参数（本命令无参数）
     * @return 背景色字符串
     */
    public static String BackgroundQ(SCPIParam param){
        int i=Command.get().getDisplay().BackgroundQ(); // 调用底层接口查询背景色
        return ToolsSCPI.getDisplayBackground(i); // 将整数类型值转换为背景色字符串
    }

    /**
     * 设置屏幕中波形显示的亮度
     * @param param SCPI命令参数，iParam1为亮度值
     */
    public static void Brightness(SCPIParam param) {
        Command.get().getDisplay().Brightness(param.iParam1, true); // 调用底层接口设置波形亮度
    }

    /**
     * 查询屏幕中波形显示的亮度
     * @param param SCPI命令参数（本命令无参数）
     * @return 亮度值字符串
     */
    public static String BrightnessQ(SCPIParam param) {
        int i=Command.get().getDisplay().BrightnessQ(); // 调用底层接口查询波形亮度
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置屏幕显示的网格类型
     * @param param SCPI命令参数，iParam1为网格类型
     */
    public static void Graticule(SCPIParam param) {
        Command.get().getDisplay().Graticule(param.iParam1, true); // 调用底层接口设置网格类型
    }

    /**
     * 查询屏幕显示的网格类型
     * @param param SCPI命令参数（本命令无参数）
     * @return 网格类型字符串
     */
    public static String GraticuleQ(SCPIParam param) {
        int i=Command.get().getDisplay().GraticuleQ(); // 调用底层接口查询网格类型
        return ToolsSCPI.getDisplayGraticule(i); // 将整数类型值转换为网格类型字符串
    }

    /**
     * 设置屏幕中网格显示的亮度
     * @param param SCPI命令参数，iParam1为网格亮度值
     */
    public static void Intensity(SCPIParam param) {
        Command.get().getDisplay().Intensity(param.iParam1, true); // 调用底层接口设置网格亮度
    }

    /**
     * 查询屏幕中网格显示的亮度
     * @param param SCPI命令参数（本命令无参数）
     * @return 网格亮度值字符串
     */
    public static String IntensityQ(SCPIParam param) {
        int i=Command.get().getDisplay().IntensityQ(); // 调用底层接口查询网格亮度
        return ToolsSCPI.getInt(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置余辉显示模式（关闭/普通/无限）
     * @param param SCPI命令参数，iParam1为余辉模式
     */
    public static void Persist_Mode(SCPIParam param) {
        Command.get().getDisplay().Persist_Mode(param.iParam1, true); // 调用底层接口设置余辉模式
    }

    /**
     * 查询余辉显示模式
     * @param param SCPI命令参数（本命令无参数）
     * @return 余辉模式字符串
     */
    public static String Persist_ModeQ(SCPIParam param) {
        int i= Command.get().getDisplay().Persist_ModeQ(); // 调用底层接口查询余辉模式
        return ToolsSCPI.getDisplayPersistMode(i); // 将整数类型值转换为余辉模式字符串
    }

    /**
     * 设置FFT余辉显示模式
     * @param param SCPI命令参数，iParam1为FFT余辉模式
     */
    public static void FftPersist_Mode(SCPIParam param) {
        Command.get().getDisplay().FftPersist_Mode(param.iParam1, true); // 调用底层接口设置FFT余辉模式
    }

    /**
     * 查询FFT余辉显示模式
     * @param param SCPI命令参数（本命令无参数）
     * @return FFT余辉模式字符串
     */
    public static String FftPersist_ModeQ(SCPIParam param) {
        int i= Command.get().getDisplay().FftPersist_ModeQ(); // 调用底层接口查询FFT余辉模式
        return ToolsSCPI.getDisplayFftPersistMode(i); // 将整数类型值转换为FFT余辉模式字符串
    }

    /**
     * 设置余辉普通显示模式下的余辉时间
     * @param param SCPI命令参数，iParam1为余辉时间值
     */
    public static void Persist_Adjust(SCPIParam param) {
        Command.get().getDisplay().Persist_Adjust(param.iParam1, true); // 调用底层接口设置余辉时间
    }

    /**
     * 查询余辉普通显示模式下的余辉时间
     * @param param SCPI命令参数（本命令无参数）
     * @return 余辉时间字符串
     */
    public static String Persist_AdjustQ(SCPIParam param) {
        int i=Command.get().getDisplay().Persist_AdjustQ(); // 调用底层接口查询余辉时间
//        return ToolsSCPI.getDisplayPersistAdjust(i);
        return String.valueOf(i); // 将余辉时间整数值直接转换为字符串返回
    }

    /**
     * 清除余辉显示
     * @param param SCPI命令参数（本命令无参数）
     */
    public static void Persist_Clear(SCPIParam param) {
        Command.get().getDisplay().Persist_Clear(true); // 调用底层接口清除余辉显示
    }

    /**
     * 打开或关闭高刷新模式
     * @param param SCPI命令参数，bParam1为高刷新开启/关闭标志
     */
    public static void High(SCPIParam param) {
        Command.get().getDisplay().High(param.bParam1, true); // 调用底层接口设置高刷新模式
    }

    /**
     * 查询高刷新模式状态
     * @param param SCPI命令参数（本命令无参数）
     * @return 高刷新状态字符串（ON/OFF）
     */
    public static String HighQ(SCPIParam param) {
        boolean b=Command.get().getDisplay().HighQ(); // 调用底层接口查询高刷新模式状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 设置屏幕水平展开中心模式
     * @param param SCPI命令参数，iParam1为水平展开中心模式
     */
    public static void HorRef(SCPIParam param) {
        Command.get().getDisplay().HorRef(param.iParam1, true); // 调用底层接口设置水平展开中心模式
    }

    /**
     * 查询屏幕水平展开中心模式
     * @param param SCPI命令参数（本命令无参数）
     * @return 水平展开中心模式字符串
     */
    public static String HorRefQ(SCPIParam param) {
        int i=Command.get().getDisplay().HorRefQ(); // 调用底层接口查询水平展开中心模式
        return ToolsSCPI.getDisplayHorRef(i); // 将整数类型值转换为水平展开中心模式字符串
    }

    /**
     * 打开或关闭ZOOM（缩放）模式
     * @param param SCPI命令参数，bParam1为ZOOM开启/关闭标志
     */
    public static void Zoom(SCPIParam param) {
        Command.get().getDisplay().Zoom(param.bParam1, true); // 调用底层接口设置ZOOM开关
    }

    /**
     * 查询ZOOM（缩放）模式状态
     * @param param SCPI命令参数（本命令无参数）
     * @return ZOOM状态字符串（ON/OFF）
     */
    public static String ZoomQ(SCPIParam param) {
        boolean b=Command.get().getDisplay().ZoomQ(); // 调用底层接口查询ZOOM状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

    /**
     * 打开或关闭CCT（色温）控制
     * @param param SCPI命令参数，bParam1为CCT开启/关闭标志
     */
    public static void CCT(SCPIParam param){
        Command.get().getDisplay().Cct(param.bParam1,true); // 调用底层接口设置CCT开关
    }

    /**
     * 查询CCT（色温）控制状态
     * @param param SCPI命令参数（本命令无参数）
     * @return CCT状态字符串（ON/OFF）
     */
    public static String CCTQ(SCPIParam param){
        boolean b=Command.get().getDisplay().CctQ(); // 调用底层接口查询CCT状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为ON/OFF字符串
    }

}
