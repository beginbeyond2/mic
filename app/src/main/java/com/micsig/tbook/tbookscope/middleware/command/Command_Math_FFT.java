package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI数学FFT运算命令处理


import com.micsig.base.Logger; // 日志工具
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.MathChannel; // 数学通道
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                           Command_Math_FFT                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 数学运算FFT子命令处理层                          |
 * | 核心职责：管理FFT（快速傅里叶变换）运算的信源、窗函数、显示类型、            |
 * |          垂直档位/偏移、水平档位/位置；XY模式下不允许设置垂直档位和偏移      |
 * | 架构设计：属于Command_Math的子模块，由Command单例统一调度；                  |
 * |          FFT运算需要数学通道处于显示状态且模式为FFT时才允许设置水平参数；    |
 * |          OffsetQ查询时根据FFT类型(RMS/DB)从不同缓存获取垂直位置            |
 * | 数据流向：SCPI指令 → Command_Math_FFT → 成员变量(状态存储)                  |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、ChannelFactory(通道工厂)、RxBus(事件总线)、     |
 * |           Command_Display(时基模式判断)、CacheUtil(缓存)、GlobalVar(全局变量) |
 * | 使用场景：远程SCPI控制中设置/查询FFT运算参数                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Math_FFT {
    //FFT
//            new SCPICommandStruct(":MATH:FFT:SOURce","SCPI_Math_FFT","Source"),//选择FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:SOURce?","SCPI_Math_FFT","SourceQ"),//查询FFT运算的信源
//            new SCPICommandStruct(":MATH:FFT:WINDow","SCPI_Math_FFT","Window"),//选择FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:WINDow?","SCPI_Math_FFT","WindowQ"),//查询FFT运算的窗函数
//            new SCPICommandStruct(":MATH:FFT:TYPE","SCPI_Math_FFT","Type"),//选择FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:TYPE?","SCPI_Math_FFT","TypeQ"),//查询FFT波形的显示方式
//            new SCPICommandStruct(":MATH:FFT:EXTent","SCPI_Math_FFT","Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:PLUS:EXTent","SCPI_Math_FFT","Plus_Extent"),//设置FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:EXTent?","SCPI_Math_FFT","ExtentQ"),//查询FFT运算结果的垂直档位
//            new SCPICommandStruct(":MATH:FFT:OFFSet","SCPI_Math_FFT","Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:PLUS:OFFSet","SCPI_Math_FFT","Plus_Offset"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:OFFSet?","SCPI_Math_FFT","OffsetQ"),//查询FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HSCAle","SCPI_Math_FFT","HsCale"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HSCAle?","SCPI_Math_FFT","HsCaleQ"),//查询FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HPOSition","SCPI_Math_FFT","Position"),//设置FFT运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:FFT:HPOSition?","SCPI_Math_FFT","PositionQ"),//查询FFT运算结果的垂直偏移

    private int source; // FFT信源通道索引
    private int window; // FFT窗函数类型索引
    private int type; // FFT显示方式类型索引（0=RMS, 1=DB）
    private double extent; // FFT运算结果垂直档位
    private double offset; // FFT运算结果垂直偏移
    private double hsCale; // FFT水平档位（Hz/div）
    private double position; // FFT水平位置（像素偏移）

    /**
     * 选择FFT运算的信源
     * 对应SCPI指令: :MATH:FFT:SOURce
     * @param mathIndex 数学通道索引
     * @param index 信源通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Source(int mathIndex, int index, boolean isUpdateUI) {
        if (source == index) return; // 信源未变则直接返回
        this.source = index; // 更新信源索引
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTSOURCE); // 设置消息标志为FFT信源
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT运算的信源
     * 对应SCPI指令: :MATH:FFT:SOURce?
     * @return 信源通道索引
     */
    public int SourceQ() {
        return this.source; // 返回信源索引
    }

    /**
     * 选择FFT运算的窗函数
     * 对应SCPI指令: :MATH:FFT:WINDow
     * @param mathIndex 数学通道索引
     * @param index 窗函数类型索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Window(int mathIndex, int index, boolean isUpdateUI) {
        if (window == index) return; // 窗函数未变则直接返回
        this.window = index; // 更新窗函数索引
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTWINDOW); // 设置消息标志为FFT窗函数
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT运算的窗函数
     * 对应SCPI指令: :MATH:FFT:WINDow?
     * @return 窗函数类型索引
     */
    public int WindowQ() {
        return this.window; // 返回窗函数索引
    }

    /**
     * 选择FFT波形的显示方式
     * 对应SCPI指令: :MATH:FFT:TYPE
     * @param mathIndex 数学通道索引
     * @param index 显示方式类型索引（0=RMS, 1=DB）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Type(int mathIndex, int index, boolean isUpdateUI) {
        if (type == index) return; // 显示方式未变则直接返回
        this.type = index; // 更新显示方式索引
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFTTYPE); // 设置消息标志为FFT类型
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT波形的显示方式
     * 对应SCPI指令: :MATH:FFT:TYPE?
     * @return 显示方式类型索引
     */
    public int TypeQ() {
        return this.type; // 返回显示方式索引
    }

    /**
     * 设置FFT运算结果的垂直档位
     * 对应SCPI指令: :MATH:FFT:EXTent
     * @param mathIndex 数学通道索引
     * @param extent 垂直档位值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY)
            return; // XY模式下不允许设置垂直档位
//        if (this.extent == extent) return;
        this.extent = extent; // 更新垂直档位
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Extent); // 设置消息标志为FFT垂直档位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * TODO 数学多通道
     * 设置FFT运算结果的垂直档位（增量模式）
     * 对应SCPI指令: :MATH:FFT:PLUS:EXTent
     * @param index 数学通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Extent(int index, boolean isUpdateUI) {
        double tem = ExtentQ(); // 获取当前垂直档位
//        tem+=getExtent(index);
        Extent(index, tem, isUpdateUI); // 以当前档位值重新设置（预留增量逻辑）
    }

    /**
     * 查询FFT运算结果的垂直档位
     * 对应SCPI指令: :MATH:FFT:EXTent?
     * @return 垂直档位值
     */
    public double ExtentQ() {
        return this.extent; // 返回垂直档位
    }

    /**
     * 设置FFT运算结果的垂直偏移
     * 对应SCPI指令: :MATH:FFT:OFFSet
     * @param mathIndex 数学通道索引
     * @param offset 垂直偏移值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Offset(int mathIndex, double offset, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY)
            return; // XY模式下不允许设置垂直偏移
//        if (this.offset == offset) return;
        this.offset = offset; // 更新垂直偏移
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Offset); // 设置消息标志为FFT垂直偏移
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 设置FFT运算结果的垂直偏移（增量模式）
     * 对应SCPI指令: :MATH:FFT:PLUS:OFFSet
     * @param index 数学通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Offset(int index, boolean isUpdateUI) {
        // 空实现，预留接口
    }

    /**
     * 查询FFT运算结果的垂直偏移
     * 对应SCPI指令: :MATH:FFT:OFFSet?
     * 根据FFT类型(RMS/DB)从不同缓存获取垂直位置，转换为物理量偏移值
     * @param source 信源通道号
     * @return 垂直偏移值（含单位字符串）
     */
    public String OffsetQ(int source) {

        int mathChan = TChan.toMathChan(source); // 将信源转换为数学通道号
        MathChannel channel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan)); // 获取数学通道对象
        double pos; // 垂直位置像素值
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//        pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
//                break;
//            case CacheUtil.MATHTYPE_FFT:
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID) == 0) { // FFT类型为RMS
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION); // 从缓存获取RMS垂直位置
                } else { // FFT类型为DB
                    pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION); // 从缓存获取DB垂直位置
                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION);
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 像素位置转换为偏移值：屏幕中心 - 缓存位置
        String unit = ChannelFactory.getProbeType(ChannelFactory.MATH1); // 获取数学通道探头类型对应的单位
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit; // 像素偏移转物理量并拼接单位
    }

    /**
     * 设置FFT运算的水平档位
     * 对应SCPI指令: :MATH:FFT:HSCAle
     * 仅当数学通道显示且模式为FFT时才允许设置，水平档位范围1~100000000
     * @param mathIndex 数学通道索引
     * @param hsCale 水平档位值（Hz/div）
     * @param isUpdateUI 是否通知UI更新
     */
    public void HsCale(int mathIndex, double hsCale,boolean isUpdateUI){
        if (!Command.get().getMath().DisplayQ() || Command.get().getMath().ModeQ()!= CacheUtil.MATHTYPE_FFT) return; // 数学通道未显示或非FFT模式则返回
        int min=1; // 水平档位最小值
        int max=100000000; // 水平档位最大值
        if (hsCale<min) hsCale=min; // 限制最小值
        if (hsCale>max) hsCale=max; // 限制最大值

        this.hsCale=hsCale; // 更新水平档位
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_HsCal); // 设置消息标志为FFT水平档位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(hsCale); // 拼接参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件（注意：此处未调用setParam，参数直接设置到msgToUI）
        }
    }

    /**
     * 查询FFT运算的水平档位
     * 对应SCPI指令: :MATH:FFT:HSCAle?
     * @return 水平档位值（Hz/div）
     */
    public double HsCaleQ(){
        return this.hsCale; // 返回水平档位
    }

    /**
     * 设置FFT运算的水平位置
     * 对应SCPI指令: :MATH:FFT:HPOSition
     * 仅当数学通道显示且模式为FFT时才允许设置
     * @param mathIndex 数学通道索引
     * @param position 水平位置值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Position(int mathIndex, double position, boolean isUpdateUI) {
        if (!Command.get().getMath().DisplayQ() || Command.get().getMath().ModeQ()!= CacheUtil.MATHTYPE_FFT) return; // 数学通道未显示或非FFT模式则返回
        this.position=position; // 更新水平位置
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_FFT_Position); // 设置消息标志为FFT水平位置
//            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_POSITION);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(position); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询FFT运算的水平位置
     * 对应SCPI指令: :MATH:FFT:HPOSition?
     * @return 水平位置值
     */
    public double PositionQ(){
        return this.position; // 返回水平位置
    }

}
