package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import android.os.Build; // Android版本构建信息
import android.os.Environment; // Android环境变量
import android.os.SharedMemory; // 共享内存（用于SCPI数据传输）
import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.Data.AutoSave; // 自动保存数据
import com.micsig.tbook.scope.Data.SingleSaveData; // 单次保存数据管理
import com.micsig.tbook.scope.Data.Waveform; // 波形数据
import com.micsig.tbook.scope.Scope; // 示波器核心实例
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.scpi.SCPICommandDeal; // SCPI命令处理
import com.micsig.tbook.tbookscope.scpi.SCPIParam; // SCPI参数
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 屏幕控制（锁屏/进度条）
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.App; // 应用上下文
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具

import java.util.Arrays; // 数组工具
import java.util.HashMap; // 哈希映射
import java.util.concurrent.atomic.AtomicBoolean; // 原子布尔（线程安全）
import java.util.concurrent.atomic.AtomicInteger; // 原子整数（线程安全）

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Storage                                      |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器存储命令处理模块                                              |
 * | 核心职责: 处理SCPI存储相关指令，包括波形保存/载入REF/截图/存储深度/设置保存    |
 * |          恢复/录制/回放/数据类型保存（CSV/PNG/MSS）等                         |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * |          内含SingleSaveData.ISaveDataListener实现，处理保存过程的屏幕锁定     |
 * |          与进度回调                                                           |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * |          保存数据: 本类 → SingleSaveData → ISaveDataListener → 屏幕锁定/解锁 |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, SingleSaveData,            |
 * |          ScreenControls, Scope, CacheUtil, ToolsSCPI                         |
 * | 使用场景: 远程控制存储操作、查询存储状态、保存波形数据到文件时使用             |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Storage {
//     new SCPICommandStruct(":STORage:SAVE","SCPI_Storage","Save"),//存储指定通道的波形到指定位置
//            new SCPICommandStruct(":STORage:LOAD","SCPI_Storage","Load"),//载入ref
//            new SCPICommandStruct(":STORage:CAPTure","SCPI_Storage","Capture"),//屏幕截图
//            new SCPICommandStruct(":STORage:DEPTh","SCPI_Storage","Depth"),//设置示波器存储深度
//            new SCPICommandStruct(":STORage:DEPTh?","SCPI_Storage","DepthQ"),//查询示波器存储深度
//            new SCPICommandStruct(":STORage:CONSave","SCPI_Storage","ConSave"),//存储示波器设置
//            new SCPICommandStruct(":STORage:CONLoad","SCPI_Storage","ConLoad"),//调用示波器设置
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

    private int depth; // 存储深度
    /**
     * const char * stor_vedioState[] = {
     * "RECOrd",
     * "STOP",
     * NULL
     * };
     */
    private int record; // 录制状态（0:STOP, 1:RECORD）
    private int play; // 回放状态
    private int play_Speed; // 回放快进选项
    private int play_back; // 回放后退选项
    private int ch; // 保存通道索引
    private int location; // 保存位置（本地/USB）
    private int saveType; // 保存类型
    private String fileName; // 保存文件名
    private boolean isAllSegments; // 是否保存全部分段
    private boolean isTime; // 截图是否包含时间戳
    private boolean isIncolor; // 截图是否彩色
    private boolean isThumbnail; // 截图是否含缩略图

    /**
     * 存储指定通道的波形到指定位置
     *
     * @param channel     通道索引 {CH1|CH2|CH3|CH4|MATH}
     * @param type        存储位置 {LOCal|UDISk}
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Save(int channel, int type, boolean isUpdateUI) {
        this.ch = channel; // 保存通道索引
        this.location = type; // 保存存储位置
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE); // 设置消息标志为存储保存
            String params = String.valueOf(channel) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type); // 拼接通道和位置参数
            msgToUI.setParam(params); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 载入参考通道(REF)波形文件
     *
     * @param source      参考通道索引 {REF1|REF2|REF3|REF4}
     * @param fileName    波形文件名
     * @param isOpen      是否打开载入
     * @param isUpdateUI  是否同步更新UI界面
     */
    //private String[] list={ "CH1","CH2","CH3","CH4","MATH","REF1","REF2","REF3","REF4","OFF"};
    private String[] list = {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8"}; // 参考通道名称列表

    public void Load(int source, String fileName, boolean isOpen, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Android 7.0+支持stream操作
                if (Arrays.stream(list).filter(s->s.equals(ToolsSCPI.getChAll(source).trim())).findAny().isPresent() ){ // 检查源是否为REF通道
                    CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
                    msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_LOAD); // 设置消息标志为载入REF
                    String params = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + // 拼接源索引
                            String.valueOf(fileName.trim())+ CommandMsgToUI.PARAM_SPLIT+ // 拼接文件名
                            String.valueOf(isOpen); // 拼接是否打开
                    msgToUI.setParam(params); // 设置消息参数
                    RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
                }
            }
        }
    }

    /**
     * 屏幕截图操作
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Capture(boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE); // 设置消息标志为截图
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }


    /**
     * 设置截图是否包含时间戳
     *
     * @param isTime      是否包含时间戳
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Capture_Time(boolean isTime,boolean isUpdateUI) {
        this.isTime=isTime; // 保存时间戳设置
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_TIME); // 设置消息标志为截图时间戳
            msgToUI.setParam(String.valueOf(isTime)); // 设置时间戳参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询截图是否包含时间戳
     *
     * @return 是否包含时间戳
     */
    public  boolean Capture_TimeQ() {
        return isTime; // 返回时间戳设置
    }

    /**
     * 设置截图是否为彩色
     *
     * @param isIncolor   是否彩色
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Capture_Incolor(boolean isIncolor,boolean isUpdateUI) {
        this.isIncolor=isIncolor; // 保存彩色设置
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_INCOLOR); // 设置消息标志为截图彩色
            msgToUI.setParam(String.valueOf(isIncolor)); // 设置彩色参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询截图是否为彩色
     *
     * @return 是否彩色
     */
    public  boolean Capture_IncolorQ() {
        return isIncolor; // 返回彩色设置
    }

    /**
     * 设置截图是否包含缩略图
     *
     * @param isThumbnail 是否包含缩略图
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Capture_Thumbnail(boolean isThumbnail, boolean isUpdateUI) {
        this.isThumbnail = isThumbnail; // 保存缩略图设置
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE_THUMBNAIL); // 设置消息标志为截图缩略图
            msgToUI.setParam(String.valueOf(isThumbnail)); // 设置缩略图参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询截图是否包含缩略图
     *
     * @return 是否包含缩略图
     */
    public boolean Capture_ThumbnailQ() {
        return isThumbnail; // 返回缩略图设置
    }

    /**
     * 开始截图操作（与Capture等效）
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Capture_Start(boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CAPTURE); // 设置消息标志为截图
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }


    /**
     * 设置示波器存储深度
     *
     * @param depth       存储深度值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Depth(int depth, boolean isUpdateUI) {
        if (this.depth == depth) return; // 值未变化则直接返回
        this.depth = depth; // 保存存储深度
        if (isUpdateUI) { // 判断是否需要更新UI
            // 存储深度变更暂无UI更新逻辑
        }
    }

    /**
     * 查询示波器存储深度
     *
     * @return 存储深度值
     */
    public int DepthQ() {
        return depth; // 返回存储深度
    }

    /**
     * 存储示波器设置到指定文件
     *
     * @param fileName    设置文件名
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void ConSave(String fileName, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONSAVE); // 设置消息标志为保存设置
            msgToUI.setParam(fileName.trim()); // 设置文件名参数（去除首尾空格）
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 开始保存设置操作
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void ConSave_start( boolean isUpdateUI){
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONSAVE_START); // 设置消息标志为开始保存设置
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 调用（载入）示波器设置
     *
     * @param fileName    设置文件名
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void ConLoad(String fileName, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_CONLOAD); // 设置消息标志为载入设置
            msgToUI.setParam(fileName); // 设置文件名参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 设置示波器录制功能的打开与关闭
     *
     * @param record      录制状态（0:STOP, 1:RECORD）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Record(int record, boolean isUpdateUI) {
        if (this.record == record) return; // 值未变化则直接返回
        this.record = record; // 保存录制状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_RECORD); // 设置消息标志为录制状态
            msgToUI.setParam(String.valueOf(record)); // 设置录制状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询示波器录制功能的打开与关闭
     *
     * @return 录制状态
     */
    public int RecordQ() {
        return record; // 返回录制状态
    }

    /**
     * 设置示波器回放功能的打开和关闭（空实现）
     *
     * @param index       回放状态索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Play(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放功能的打开和关闭（空实现）
     *
     * @return 默认返回0
     */
    public int PlayQ() {
        return 0; // 空实现，返回0
    }

    /**
     * 设置示波器回放快进选项（空实现）
     *
     * @param index       快进选项索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Play_Speed(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放快进选项（空实现）
     *
     * @return 默认返回0
     */
    public int Play_SpeedQ() {
        return 0; // 空实现，返回0
    }

    /**
     * 设置示波器回放后退选项（空实现）
     *
     * @param index       后退选项索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Play_Back(int index, boolean isUpdateUI) {
    }

    /**
     * 查询示波器回放后退选项（空实现）
     *
     * @return 默认返回0
     */
    public int Play_backQ() {
        return 0; // 空实现，返回0
    }

    /**
     * 设置保存的通道源
     *
     * @param ch          通道索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Save_Source(int ch, boolean isUpdateUI) {
        this.ch = ch; // 保存通道索引
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_SOURCE); // 设置消息标志为保存源
            msgToUI.setParam(String.valueOf(ch)); // 设置通道参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询保存的通道源
     *
     * @return 通道索引
     */
    public int Save_SourceQ() {
        return this.ch; // 返回保存通道
    }

    /**
     * 设置保存位置（本地/USB）
     *
     * @param location    位置索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Save_Location(int location, boolean isUpdateUI) {
        this.location=location; // 保存存储位置
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_LOCATION); // 设置消息标志为保存位置
            msgToUI.setParam(String.valueOf(location)); // 设置位置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询保存位置
     *
     * @return 位置索引
     */
    public int Save_LocationQ() {
        return location; // 返回保存位置
    }

    /**
     * 设置保存类型
     *
     * @param saveType    保存类型索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Save_Type(int saveType, boolean isUpdateUI) {
        this.saveType=saveType; // 保存类型
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_TYPE); // 设置消息标志为保存类型
            msgToUI.setParam(String.valueOf(saveType)); // 设置类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询保存类型
     *
     * @return 保存类型索引
     */
    public int Save_TypeQ() {
        return saveType; // 返回保存类型
    }

    /**
     * 设置保存文件名
     *
     * @param fileName    文件名
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Save_Filename(String fileName, boolean isUpdateUI) {
        this.fileName=fileName.trim(); // 保存文件名（去除首尾空格）
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_FILENAME); // 设置消息标志为保存文件名
            msgToUI.setParam(String.valueOf(fileName.trim())); // 设置文件名参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询保存文件名
     *
     * @return 文件名
     */
    public String Save_FilenameQ() {
        return fileName; // 返回保存文件名
    }

    /**
     * 开始执行保存操作（无isUpdateUI参数，始终通知UI）
     */
    public void Save_Start() {
        CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
        msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_START); // 设置消息标志为开始保存
        msgToUI.setParam(String.valueOf(record)); // 设置录制状态参数
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
    }

    /**
     * 设置是否保存全部分段数据
     *
     * @param isOpen      是否保存全部分段
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Save_ALLSegments(boolean isOpen,boolean isUpdateUI){
        this.isAllSegments=isOpen; // 保存全部分段标志
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_STOTAGE_SAVE_ALLSEGMENTS); // 设置消息标志为全部分段
            msgToUI.setParam(String.valueOf(isOpen)); // 设置是否全部分段参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询是否保存全部分段数据
     *
     * @return 是否保存全部分段
     */
    public  boolean Save_ALLSegmentsQ(){
        return isAllSegments; // 返回全部分段标志
    }

    /**
     * SingleSaveData的回调监听器实现，处理保存过程中的屏幕锁定/解锁和进度更新。
     * 在保存开始时暂停示波器运行，保存结束后恢复运行状态。
     */
    SingleSaveData.ISaveDataListener saveDataListener = new SingleSaveData.ISaveDataListener() {
        private ScreenControls screenControls = null; // 屏幕控制实例
        private int lockFlag = 0; // 屏幕锁定标志位

        private AtomicBoolean atomicInteger = new AtomicBoolean(false); // 保存前示波器是否在运行（原子布尔，线程安全）

        /**
         * 保存开始回调：暂停示波器运行
         */
        @Override
        public void onBegin() {
            if(screenControls == null){ // 屏幕控制未初始化
                screenControls = ScreenControls.getInstance(); // 获取屏幕控制单例
            }
            screenControls.onUI(()->{ // 在UI线程执行
                Scope scope = Scope.getInstance(); // 获取示波器核心实例
                boolean b = scope.isRun(); // 查询当前是否运行中
                if(b){ // 如果正在运行
                    Command.get().getFunctionMenu().Stop(true); // 停止示波器运行
                }
                atomicInteger.set(b); // 记录保存前运行状态
            });

        }

        /**
         * 保存结束回调：解锁屏幕，恢复示波器运行
         */
        @Override
        public void onEnd() {
            if(screenControls.isLockScreen(lockFlag)) { // 如果屏幕被锁定
                screenControls.unLockScreen(lockFlag); // 解锁屏幕
            }
            lockFlag = 0; // 重置锁定标志
            screenControls.onUI(()->{ // 在UI线程执行
                if(atomicInteger.get()){ // 如果保存前示波器在运行
                    Command.get().getFunctionMenu().Run(true); // 恢复运行
                }
                atomicInteger.set(false); // 重置运行状态记录
            });
        }

        /**
         * 保存前回调：锁定屏幕（可选锁定进度条）
         *
         * @param bProgress 是否显示进度条
         */
        @Override
        public void onSaveBefore(boolean bProgress) {
            lockFlag = ScreenControls.LOCK_SCREEN; // 设置屏幕锁定标志
            if(bProgress){ // 需要显示进度条
                lockFlag |= ScreenControls.LOCK_PROGRESS; // 追加进度条锁定标志
            }
            if(!screenControls.isLockScreen(lockFlag)) { // 屏幕未被锁定
                screenControls.lockScreen(lockFlag); // 锁定屏幕
            }
        }

        /**
         * 保存进度回调：更新进度条值
         *
         * @param val 进度值
         */
        @Override
        public void onProgress(int val) {
            screenControls.onUI(()-> { // 在UI线程执行
                screenControls.setProgressValue(val); // 更新进度条值
            });
        }

        /**
         * 保存后回调：解锁进度条或屏幕
         *
         * @param bProgress 是否解锁进度条
         */
        @Override
        public void onSaveAfter(boolean bProgress) {
            int flag = lockFlag; // 取当前锁定标志
            if(bProgress){ // 需要解锁进度条
                flag = ScreenControls.LOCK_PROGRESS; // 设置进度条解锁标志
                lockFlag &= ~flag; // 从锁定标志中移除进度条位
            }
            if(screenControls.isLockScreen(flag)) { // 对应标志位已锁定
                screenControls.unLockScreen(flag); // 解锁对应标志位
            }
        }

        /**
         * 截图回调：触发自动保存截图
         *
         * @param filePath 文件路径
         * @param fileName 文件名
         */
        @Override
        public void onPicture(String filePath, String fileName) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath); // 缓存截图路径
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,fileName); // 缓存截图文件名
            RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true); // 发送自动保存截图事件
        }

        /**
         * 获取当前缓存数据回调：用于保存设置时获取当前配置
         *
         * @return 包含默认保存名和其他保存名的缓存映射
         */
        @Override
        public HashMap<String, HashMap<String, String>> onCurCache() {
            HashMap<String, HashMap<String, String>> map = new HashMap<>(); // 创建外层映射
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap()); // 放入默认保存名的缓存
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap()); // 放入其他保存名的缓存
            return map; // 返回缓存映射
        }
    };

    /**
     * 按指定数据类型保存波形数据（CSV/PNG/MSS）
     *
     * @param strArray 数据类型字符串数组，如 {"csv","png","mss"}
     * @return 保存是否成功启动
     */
    public boolean Save_DataType(String [] strArray){
        SingleSaveData singleSaveData = SingleSaveData.getInstance(); // 获取单次保存数据单例
        boolean b = false; // 保存启动结果，默认失败
        if(!singleSaveData.isRun()) { // 当前没有保存任务在运行
            singleSaveData.setAutoSaveListener(saveDataListener); // 设置保存回调监听器
            int type = 0; // 保存类型标志位
            for(String s:strArray){ // 遍历数据类型数组
                if(s.equalsIgnoreCase("csv")){ // CSV格式
                    type |= SingleSaveData.AUTO_SAVE_CSV; // 追加CSV类型标志
                }else if(s.equalsIgnoreCase("png")){ // PNG图片格式
                    type |= SingleSaveData.AUTO_SAVE_IMAGE; // 追加图片类型标志
                }else if(s.equalsIgnoreCase("mss")){ // MSS会话格式
                    type |= SingleSaveData.AUTO_SAVE_SESSION; // 追加会话类型标志
                }
            }
            if(type != 0) { // 有有效的保存类型
                singleSaveData.setSaveType(type); // 设置保存类型
                String path = Tools.getSaveDataPath(); // 获取保存数据路径
                singleSaveData.setFilePath(path); // 设置文件保存路径
                singleSaveData.start(); // 启动保存任务
                b = true; // 标记启动成功
            }
        }
        return b; // 返回保存启动结果
    }

    /**
     * 查询保存数据状态（是否空闲可保存）
     *
     * @return true表示空闲（不在运行），false表示正在保存
     */
    public boolean Save_DataStatusQ(){
        SingleSaveData singleSaveData = SingleSaveData.getInstance(); // 获取单次保存数据单例
        return !singleSaveData.isRun(); // 返回取反的运行状态（空闲=true）
    }


    /**
     * 查询CSV格式波形数据，写入共享内存
     *
     * @return 空字符串（数据已写入共享内存）
     */
    public String Save_DataCSVQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem(); // 获取共享内存实例
        SingleSaveData.getInstance().writeCsvToShm(shm); // 将CSV数据写入共享内存
        return ""; // 返回空字符串
    }

    /**
     * 查询PNG格式截图数据，写入共享内存
     *
     * @return 空字符串（数据已写入共享内存）
     */
    public String Save_DataPNGQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem(); // 获取共享内存实例
        SingleSaveData.getInstance().writePngToShm(shm); // 将PNG数据写入共享内存
        return ""; // 返回空字符串
    }

    /**
     * 查询MSS格式会话数据，写入共享内存
     *
     * @return 空字符串（数据已写入共享内存）
     */
    public String Save_DataMSSQ(){
        SharedMemory shm = SCPICommandDeal.getInstance().getSharedMem(); // 获取共享内存实例
        SingleSaveData.getInstance().writeMssToShm(shm); // 将MSS数据写入共享内存
        return ""; // 返回空字符串
    }
}
