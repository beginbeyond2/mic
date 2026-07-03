package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI显示命令处理

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                            Command_Display                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 显示(Display)命令处理层                          |
 * | 核心职责：解析并执行示波器显示相关的SCPI指令，管理波形显示方式、亮度、       |
 * |          网格、余辉、高刷新、色温、ZOOM、时基模式、滚屏等显示参数            |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          各属性独立存储，设置方法采用"值变更检测→状态更新→UI通知"三段式流程  |
 * | 数据流向：SCPI指令 → Command_Display → 成员变量(状态存储)                  |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、RxBus(事件总线)、ToolsSCPI(余辉校验)            |
 * | 使用场景：远程SCPI控制中设置/查询示波器显示参数                             |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Display {
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
//            new SCPICommandStruct(":DISPlay:HORRef","SCPI_Display","horRef"),//设置屏幕水平展开中心模式
//            new SCPICommandStruct(":DISPlay:HORRef?","SCPI_Display","HorRefQ"),//查询屏幕水平展开中心模式
//            new SCPICommandStruct(":DISPlay:ZOOM","SCPI_Display","Zoom"),//打开或关闭ZOOM
//            new SCPICommandStruct(":DISPlay:ZOOM?","SCPI_Display","ZoomQ"),//查询ZOOM打开或关闭
//            new SCPICommandStruct(":DISPlay:CCT","SCPI_Display","CCT"),//打开或关闭ZOOM
//            new SCPICommandStruct(":DISPlay:CCT?","SCPI_Display","CCTQ"),//查询ZOOM打开或关闭

    //常规   routineTimeBaseRef()    时基参考  （中心，触发位置）
    //常规   routineTimbaseMode()    时基模式  （YT，XY）
    //常规   routineRollingScreen()  滚屏      （开启，关闭）

    public static final int RoutineTimeBaseMode_YT = 0; // YT时基模式常量
    public static final int RoutineTimeBaseMode_XY = 1; // XY时基模式常量

    //时基模式
    private int routineTimebaseMode = 0; // 当前时基模式，默认YT模式
    //滚屏
    private int routineRollingScreen = 0; // 滚屏模式，默认关闭

    private int waveForm; // 波形显示方式（点/线）
    private int brightness; // 波形亮度
    private int background; // 背景色
    private int graticule; // 网格类型
    private int intensity; // 网格亮度
    private int persistMode, fftPersistMode; // 余辉模式、FFT余辉模式
    private int persistAdjust, fftPersistAdjust; // 余辉时间、FFT余辉时间
    private boolean isHigh; // 高刷新开关
    //时基参考
    private int horRef; // 水平展开中心模式
    private boolean cct; // 色温开关
    private boolean isZoom; // ZOOM开关

    /**
     * 设置屏幕中波形的显示方式 点 线
     * 对应SCPI指令: :DISPlay:WAVeform
     * @param index 显示方式索引（0=点, 1=线等）
     * @param isUpdateUI 是否通知UI更新
     */
    public void WaveForm(int index, boolean isUpdateUI) {
        if (waveForm == index) return; // 显示方式未变则直接返回
        this.waveForm = index; // 更新显示方式
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_WAVEFORM); // 设置消息标志为波形显示方式
            msgToUI.setParam(String.valueOf(index)); // 设置显示方式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕中波形的显示方式
     * 对应SCPI指令: :DISPlay:WAVeform?
     * @return 显示方式索引
     */
    public int WaveFormQ() {
        return this.waveForm; // 返回波形显示方式
    }

    /**
     * 设置屏幕中波形显示的亮度
     * 对应SCPI指令: :DISPlay:BRIGhtness
     * @param brightness 亮度值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Brightness(int brightness, boolean isUpdateUI) {
        if (this.brightness == brightness) return; // 亮度未变则直接返回
        this.brightness = brightness; // 更新亮度值
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_BRIGHTNESS); // 设置消息标志为亮度
            msgToUI.setParam(String.valueOf(brightness)); // 设置亮度参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕中波形显示的亮度
     * 对应SCPI指令: :DISPlay:BRIGhtness?
     * @return 亮度值
     */
    public int BrightnessQ() {
        return this.brightness; // 返回亮度值
    }

    /**
     * 设置屏幕背景色
     * 对应SCPI指令: :DISPlay:BACKground
     * @param background 背景色索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Background(int background,boolean isUpdateUI){
        if(this.background == background) return; // 背景色未变则直接返回
        this.background = background; // 更新背景色
        if(isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_BACKGROUND); // 设置消息标志为背景色
            msgToUI.setParam(String.valueOf(background)); // 设置背景色参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕背景色
     * 对应SCPI指令: :DISPlay:BACKground?
     * @return 背景色索引
     */
    public int BackgroundQ(){return this.background;} // 返回背景色

    /**
     * 设置屏幕显示的网格类型
     * 对应SCPI指令: :DISPlay:GRATicule
     * @param graticule 网格类型索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Graticule(int graticule, boolean isUpdateUI) {
        if (this.graticule == graticule) return; // 网格类型未变则直接返回
        this.graticule = graticule; // 更新网格类型
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_GRATICULE); // 设置消息标志为网格类型
            msgToUI.setParam(String.valueOf(graticule)); // 设置网格类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕显示的网格类型
     * 对应SCPI指令: :DISPlay:GRATicule?
     * @return 网格类型索引
     */
    public int GraticuleQ() {
        return this.graticule; // 返回网格类型
    }

    /**
     * 设置屏幕中网格显示的亮度
     * 对应SCPI指令: :DISPlay:INTensity
     * @param intensity 网格亮度值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Intensity(int intensity, boolean isUpdateUI) {
        if (this.intensity == intensity) return; // 亮度未变则直接返回
        this.intensity = intensity; // 更新网格亮度
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_INTENSITY); // 设置消息标志为网格亮度
            msgToUI.setParam(String.valueOf(intensity)); // 设置亮度参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕中网格显示的亮度
     * 对应SCPI指令: :DISPlay:INTensity?
     * @return 网格亮度值
     */
    public int IntensityQ() {
        return this.intensity; // 返回网格亮度
    }

    /**
     * 设置余辉显示模式
     * 对应SCPI指令: :DISPlay:PERSist:MODE
     * @param index 余辉模式索引（0=关闭, 1=无限, 2=普通等）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Persist_Mode(int index, boolean isUpdateUI) {
        if (this.persistMode == index) return; // 模式未变则直接返回
        this.persistMode = index; // 更新余辉模式
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_PERSISTMODE); // 设置消息标志为余辉模式
            msgToUI.setParam(String.valueOf(index)); // 设置模式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询余辉显示模式
     * 对应SCPI指令: :DISPlay:PERSist:MODE?
     * @return 余辉模式索引
     */
    public int Persist_ModeQ() {
        return this.persistMode; // 返回余辉模式
    }

    /**
     * 设置FFT余辉显示模式
     * 对应SCPI指令: :DISPlay:FFTPERSist:MODE
     * @param index FFT余辉模式索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void FftPersist_Mode(int index, boolean isUpdateUI) {
        if (this.fftPersistMode == index) return; // FFT余辉模式未变则直接返回
        this.fftPersistMode = index; // 更新FFT余辉模式
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTMODE); // 设置消息标志为FFT余辉模式
            msgToUI.setParam(String.valueOf(index)); // 设置模式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT余辉显示模式
     * 对应SCPI指令: :DISPlay:FFTPERSist:MODE?
     * @return FFT余辉模式索引
     */
    public int FftPersist_ModeQ() {
        return this.fftPersistMode; // 返回FFT余辉模式
    }

    /**
     * 设置余辉普通显示模式下余辉时间
     * index: 是数值，不是索引
     * 对应SCPI指令: :DISPlay:PERSist:ADJust
     * @param index 余辉时间数值（ms）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Persist_Adjust(int index, boolean isUpdateUI) {
//        if (persistAdjust == index) return;
        if ( Tools.indexOf(ToolsSCPI.displayPersistAdjust, s->index==s)==-1 ) return; // 校验余辉时间值是否合法，不合法则直接返回
        this.persistAdjust = index; // 更新余辉时间
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_PERSISTADJUST); // 设置消息标志为余辉时间
            msgToUI.setParam(String.valueOf(index)); // 设置余辉时间参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询余辉普通显示模式下余辉时间
     * 对应SCPI指令: :DISPlay:PERSist:ADJust?
     * @return 余辉时间值
     */
    public int Persist_AdjustQ() {
        return this.persistAdjust; // 返回余辉时间
    }

    /**
     * 设置FFT余辉普通显示模式下余辉时间
     * index: 是数值，不是索引
     * 对应SCPI指令: :DISPlay:FFTPERSist:ADJust
     * @param index FFT余辉时间数值
     * @param isUpdateUI 是否通知UI更新
     */
    public void FftPersist_Adjust(int index, boolean isUpdateUI) {
        if ( Tools.indexOf(ToolsSCPI.displayFftPersistAdjust, s->index==s)==-1 ) return; // 校验FFT余辉时间值是否合法
        this.fftPersistAdjust = index; // 更新FFT余辉时间
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTADJUST); // 设置消息标志为FFT余辉时间
            msgToUI.setParam(String.valueOf(index)); // 设置余辉时间参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT余辉普通显示模式下余辉时间
     * 对应SCPI指令: :DISPlay:FFTPERSist:ADJust?
     * @return FFT余辉时间值
     */
    public int FftPersist_AdjustQ() {
        return this.fftPersistAdjust; // 返回FFT余辉时间
    }


    /**
     * 清除余辉显示
     * 对应SCPI指令: :DISPlay:PERSist:CLEar
     * @param isUpdateUI 是否通知UI更新
     */
    public void Persist_Clear(boolean isUpdateUI) {
        if (isUpdateUI) {//由于是个btn，不需要修改UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_PERSIST_CLEAR); // 设置消息标志为清除余辉
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 清除FFT余辉显示
     * 对应SCPI指令: :DISPlay:FFTPERSist:CLEar
     * @param isUpdateUI 是否通知UI更新
     */
    public void FftPersist_Clear(boolean isUpdateUI) {
        if (isUpdateUI) {//由于是个btn，不需要修改UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_FFT_PERSIST_CLEAR); // 设置消息标志为清除FFT余辉
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 打开或关闭高刷新
     * 对应SCPI指令: :DISPlay:HIGH
     * @param isHigh 是否开启高刷新
     * @param isUpdateUI 是否通知UI更新
     */
    public void High(boolean isHigh, boolean isUpdateUI) {
//        if (this.isHigh == isHigh) return;
        this.isHigh = isHigh; // 更新高刷新状态
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_HIGHREFRESH); // 设置消息标志为高刷新
            msgToUI.setParam(String.valueOf(isHigh)); // 设置开关参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高刷新打开或关闭
     * 对应SCPI指令: :DISPlay:HIGH?
     * @return 是否开启高刷新
     */
    public boolean HighQ() {
        return this.isHigh; // 返回高刷新状态
    }

    /**
     * 设置屏幕水平展开中心模式
     * 对应SCPI指令: :DISPlay:HORRef
     * @param index 水平展开中心模式索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void HorRef(int index, boolean isUpdateUI) {
//        if (this.horRef == index) return;
        this.horRef = index; // 更新水平展开中心模式
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_HORREF); // 设置消息标志为水平展开中心
            msgToUI.setParam(String.valueOf(index)); // 设置模式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询屏幕水平展开中心模式
     * 对应SCPI指令: :DISPlay:HORRef?
     * @return 水平展开中心模式索引
     */
    public int HorRefQ() {
        return this.horRef; // 返回水平展开中心模式
    }


    /**
     * 设置色温
     * 对应SCPI指令: :DISPlay:CCT
     * @param isCCT 是否开启色温
     * @param isUpdateUI 是否通知UI更新
     */
    public void Cct(boolean isCCT, boolean isUpdateUI) {
//        if (this.cct == index) return;
        this.cct = isCCT; // 更新色温状态
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_CCT); // 设置消息标志为色温
            msgToUI.setParam(String.valueOf(isCCT)); // 设置色温参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询色温
     * 对应SCPI指令: :DISPlay:CCT?
     * @return 是否开启色温
     */
    public boolean CctQ() {
        return this.cct; // 返回色温状态
    }

    /**
     * 打开或关闭ZOOM
     * 对应SCPI指令: :DISPlay:ZOOM
     * @param isZoom 是否开启ZOOM
     * @param isUpdateUI 是否通知UI更新
     */
    public void Zoom(boolean isZoom, boolean isUpdateUI) {
//        if (this.isZoom == isZoom) return;
        this.isZoom = isZoom; // 更新ZOOM状态
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_ZOOM); // 设置消息标志为ZOOM
            msgToUI.setParam(String.valueOf(isZoom)); // 设置开关参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询ZOOM打开或关闭
     * 对应SCPI指令: :DISPlay:ZOOM?
     * @return 是否开启ZOOM
     */
    public boolean ZoomQ() {
        return this.isZoom; // 返回ZOOM状态
    }

    /**
     * 设置时基模式（YT/XY）
     * 常规   routineTimebaseMode()    时基模式  （YT，XY）
     * @param index 时基模式索引（0=YT, 1=XY）
     * @param isUpdateUI 是否通知UI更新
     */
    public void setRoutineTimeBaseMode(int index, boolean isUpdateUI) {
        if (routineTimebaseMode == index) return; // 模式未变则直接返回
        this.routineTimebaseMode = index; // 更新时基模式
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI=Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_DISPLAY_TIMEBASE); // 设置消息标志为时基模式
            msgToUI.setParam(String.valueOf(this.routineTimebaseMode)); // 设置模式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI,msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询时基模式
     * @return 时基模式索引（0=YT, 1=XY）
     */
    public int getRoutineTimeBaseModeQ() {
        return this.routineTimebaseMode; // 返回时基模式
    }

    public static int RollingScreen_true = 0; // 滚屏开启常量
    public static int RollingScreen_false = 1; // 滚屏关闭常量

    /**
     * 设置滚屏模式
     * 常规   routineRollingScreen()  滚屏      （开启，关闭）
     * @param index 滚屏模式索引（0=开启, 1=关闭）
     * @param isUpdateUI 是否通知UI更新
     */
    public void setRoutineRollingScreen(int index, boolean isUpdateUI) {
        if (this.routineRollingScreen == index) return; // 模式未变则直接返回
        this.routineRollingScreen = index; // 更新滚屏模式
        if (isUpdateUI) { // 需要通知UI更新
        }
    }

    /**
     * 查询滚屏模式
     * @return 滚屏模式索引
     */
    public int getRoutineRollingScreen() {
        return this.routineRollingScreen; // 返回滚屏模式
    }

}
