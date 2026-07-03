package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI数学基础运算命令处理

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
 * @auother Liwb
 * @description:
 * @data:2022-3-24 10:09
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                           Command_Math_Base                                  |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 数学运算基础(Base)子命令处理层                   |
 * | 核心职责：管理基础双通道运算（加/减/乘/除）的信源1/2、垂直档位/偏移、       |
 * |          运算符类型；XY模式下不允许设置档位和偏移                            |
 * | 架构设计：属于Command_Math的子模块，由Command单例统一调度；                  |
 * |          信源设置同步到MathDualWave对象；                                    |
 * |          运算符类型由modeIndex区分（+、-、×、÷等）                          |
 * | 数据流向：SCPI指令 → Command_Math_Base → 成员变量(状态存储)                 |
 * |                            → MathDualWave(双通道运算)                       |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、ChannelFactory(通道工厂)、RxBus(事件总线)、     |
 * |           Command_Display(时基模式判断)、CacheUtil(缓存)                     |
 * | 使用场景：远程SCPI控制中设置/查询基础运算参数                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Math_Base {

    //            new SCPICommandStruct(":MATH:BASE:S1","SCPI_Math_BASE","S1"),//选择加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S1?","SCPI_Math_BASE","S1Q"),//查询加法运算的信源1
//            new SCPICommandStruct(":MATH:BASE:S2","SCPI_Math_BASE","S2"),//选择加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:S2?","SCPI_Math_BASE","S2Q"),//查询加法运算的信源2
//            new SCPICommandStruct(":MATH:BASE:EXTent","SCPI_Math_BASE","Extent"),//设置加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:EXTent?","SCPI_Math_BASE","ExtentQ"),//查询加法运算结果的垂直档位
//            new SCPICommandStruct(":MATH:BASE:OFFSet","SCPI_Math_BASE","Offset"),//设置加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OFFSet?","SCPI_Math_BASE","OffsetQ"),//查询加法运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:BASE:OPERator","SCPI_Math_BASE","Operator"),//设置运算符
//            new SCPICommandStruct(":MATH:BASE:OPERator?","SCPI_Math_BASE","OperatorQ"),//查询运算符

    private int s1; // 信源1通道索引
    private int s2; // 信源2通道索引
    private double extent; // 运算结果垂直档位
    private double offset; // 运算结果垂直偏移
    private int modeIndex; // 运算符类型索引

    /**
     * 选择加法运算的信源1
     * 对应SCPI指令: :MATH:BASE:S1
     * @param mathIndex 数学通道索引
     * @param index 信源1通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void S1(int mathIndex, int index, boolean isUpdateUI) {
        if (s1 == index) return; // 信源1未变则直接返回
        this.s1 = index; // 更新信源1
        ChannelFactory.getMathChannel(mathIndex).getMathDualWave().setSource1(index); // 同步到双通道运算对象
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_S1); // 设置消息标志为信源1
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询加法运算的信源1
     * 对应SCPI指令: :MATH:BASE:S1?
     * @return 信源1通道索引
     */
    public int S1Q() {
        return this.s1; // 返回信源1索引
    }

    /**
     * 选择加法运算的信源2
     * 对应SCPI指令: :MATH:BASE:S2
     * @param mathIndex 数学通道索引
     * @param index 信源2通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void S2(int mathIndex, int index, boolean isUpdateUI) {
        if (s2 == index) return; // 信源2未变则直接返回
        this.s2 = index; // 更新信源2
        ChannelFactory.getMathChannel(mathIndex).getMathDualWave().setSource2(index); // 同步到双通道运算对象
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_S2); // 设置消息标志为信源2
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询加法运算的信源2
     * 对应SCPI指令: :MATH:BASE:S2?
     * @return 信源2通道索引
     */
    public int S2Q() {
        return this.s2; // 返回信源2索引
    }


    /**
     * 设置加法运算结果的垂直档位
     * 对应SCPI指令: :MATH:BASE:EXTent
     * @param mathIndex 数学通道索引
     * @param extent 垂直档位值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_XY) return; // XY模式下不允许设置
//        if (this.extent == extent) return;
        this.extent = extent; // 更新垂直档位
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_EXTENT); // 设置消息标志为基础垂直档位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询加法运算结果的垂直档位
     * 对应SCPI指令: :MATH:BASE:EXTent?
     * @return 垂直档位值
     */
    public double ExtentQ() {
        return this.extent; // 返回垂直档位
    }


    /**
     * 设置加法运算结果的垂直偏移
     * 对应SCPI指令: :MATH:BASE:OFFSet
     * @param mathIndex 数学通道索引
     * @param offset 垂直偏移值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Offset(int mathIndex, double offset, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ()==Command_Display.RoutineTimeBaseMode_XY)return; // XY模式下不允许设置
        this.offset = offset; // 更新垂直偏移
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_OFFSET); // 设置消息标志为基础垂直偏移
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }



    /**
     * 查询加法运算结果的垂直偏移
     * 对应SCPI指令: :MATH:BASE:OFFSet?
     * @param mathIndex 数学通道索引
     * @return 垂直偏移值（含单位字符串）
     */
    public String OffsetQ(int mathIndex) {
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex); // 获取数学通道对象
        double pos; // 垂直位置像素值
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + TChan.toUiChNo(mathIndex)); // 从缓存获取双通道运算垂直位置
//                break;
//            case CacheUtil.MATHTYPE_FFT:
//                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE) == 0) {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
//                } else {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
//                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION);
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 像素位置转换为偏移值：屏幕中心 - 缓存位置
        String unit = ChannelFactory.getProbeType(mathIndex); // 获取通道探头类型对应的单位
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit; // 像素偏移转物理量并拼接单位
    }

    /**
     * 设置运算符类型
     * 对应SCPI指令: :MATH:BASE:OPERator
     * @param mathIndex 数学通道索引
     * @param modeIndex 运算符类型索引（+、-、×、÷等）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Operator(int mathIndex, int modeIndex, boolean isUpdateUI) {
        if (this.modeIndex == modeIndex) return; // 运算符未变则直接返回
        this.modeIndex = modeIndex; // 更新运算符类型
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_BASE_OPERATOR); // 设置消息标志为运算符
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(modeIndex); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询运算符类型
     * 对应SCPI指令: :MATH:BASE:OPERator?
     * @return 运算符类型索引
     */
    public int OperatorQ(){
        return this.modeIndex; // 返回运算符类型索引
    }

}
