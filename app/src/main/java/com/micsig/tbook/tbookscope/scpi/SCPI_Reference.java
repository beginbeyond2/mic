package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂，用于判断和获取参考通道
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类，查询垂直档位等
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具，读取参考通道水平档位缓存
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道号转换工具

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 参考波形(REF)子模块                             |
 * | 核心职责：处理SCPI协议中:REFerence相关命令的设置与查询                        |
 * | 架构设计：静态方法类，委托Command中间件的Reference接口和ChannelFactory执行操作 |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getReference()/ChannelFactory|
 * | 依赖关系：SCPIParam、Command、ChannelFactory、RefChannel、CacheUtil、TChan   |
 * | 使用场景：远程控制参考波形的打开/关闭/档位/偏移/位置/采样率查询等             |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Reference {
//     new SCPICommandStruct(":REFerence:DISPlay","SCPI_Reference","Display"),//打开或关闭REF功能
//            new SCPICommandStruct(":REFerence:DISPlay?","SCPI_Reference","DisplayQ"),//查询REF功能打开或关闭
//            new SCPICommandStruct(":REFerence:ENABle","SCPI_Reference","Enable"),//打开或关闭指定的参考通道
//            new SCPICommandStruct(":REFerence:ENABle?","SCPI_Reference","EnableQ"),//查询指定的参考通道打开或关闭
//            new SCPICommandStruct(":REFerence:HSCale","SCPI_Reference","Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:PLUS:HSCale","SCPI_Reference","Plus_Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:HSCale?","SCPI_Reference","HscaleQ"),//查询参考通道的水平档位
//            new SCPICommandStruct(":REFerence:VSCale","SCPI_Reference","Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:PLUS:VSCale","SCPI_Reference","Plus_Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:VSCale?","SCPI_Reference","VscaleQ"),//查询参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:CURRent","SCPI_Reference","Current"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:HOFFset","SCPI_Reference","Plus_Hoffset"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:VOFFset","SCPI_Reference","Plus_Voffset"),//选择当前参考通道
//    //1.1新添加 2016.12.8
//            new SCPICommandStruct(":REFerence:POSition","SCPI_Reference","Position"),//设置垂直偏移
//            new SCPICommandStruct(":REFerence:POSition?","SCPI_Reference","PositionQ"),//查询垂直偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition","SCPI_Reference","Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition?","SCPI_Reference","Timebase_PositionQ"),//查询水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:TIMebase:POSition","SCPI_Reference","Plus_Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:POSition","SCPI_Reference","Plus_position"),//设置垂直偏移
//            new SCPICommandStruct(":REF:SRATe?","SCPI_Reference","REF_SRateQ"),//查询采样率
//            new SCPICommandStruct(":REF:MDEPth?","SCPI_Reference","REF_MDepthQ"),//查询存储深度

    /**
     * 打开或关闭REF功能
     * @param param SCPI参数封装，iParam1为参考通道索引，bParam1为true打开/false关闭
     */
    public static void Display(SCPIParam param) {
        Command.get().getReference().Display(param.iParam1, param.bParam1, true); // 委托Command中间件设置REF功能开关，true表示通知UI刷新
    }

    /**
     * 查询REF功能的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String DisplayQ(SCPIParam param) {
        boolean b=Command.get().getReference().DisplayQ(); // 从Command中间件获取REF功能开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭指定的参考通道
     * @param param SCPI参数封装，iParam1为通道索引，bParam1为true打开/false关闭
     */
    public static void Enable(SCPIParam param) {
        Command.get().getReference().Enable(param.iParam1, param.bParam1, true); // 委托Command中间件设置参考通道开关，true表示通知UI刷新
    }

    /**
     * 查询指定参考通道的开关状态
     * @param param SCPI参数封装，iParam1为通道索引
     * @return "ON"或"OFF"
     */
    public static String EnableQ(SCPIParam param) {
        boolean b=Command.get().getReference().EnableQ(param.iParam1); // 从Command中间件获取参考通道开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置参考通道的水平档位
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为档位值
     */
    public static void Hscale(SCPIParam param) {
        Command.get().getReference().Hscale(param.iParam1, param.dParam1, true); // 委托Command中间件设置水平档位，true表示通知UI刷新
    }

    /**
     * 设置参考通道的水平档位（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为增量方向
     */
    public static void Plus_Hscale(SCPIParam param) {
        Command.get().getReference().Plus_Hscale(param.iParam1, param.iParam2, true); // 委托Command中间件增加水平档位，true表示通知UI刷新
    }

    /**
     * 查询参考通道的水平档位（从缓存读取）
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 水平档位值的字符串表示
     */
    public static String HscaleQ(SCPIParam param) {
//        double d=Command.get().getReference().HscaleQ(param.iParam1);
//        return ToolsSCPI.getDouble(d);
        return CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.toUiChNo(param.iParam1)); // 从缓存读取参考通道水平档位
    }

    /**
     * 设置参考通道的垂直档位
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为档位值
     */
    public static void Vscale(SCPIParam param) {
        Command.get().getReference().Vscale(param.iParam1, param.dParam1, true); // 委托Command中间件设置垂直档位，true表示通知UI刷新
    }

    /**
     * 设置参考通道的垂直档位（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为增量方向
     */
    public static void Plus_Vscale(SCPIParam param) {
        Command.get().getReference().Plus_Vscale(param.iParam1, param.iParam2, true); // 委托Command中间件增加垂直档位，true表示通知UI刷新
    }

    /**
     * 查询参考通道的垂直档位（从RefChannel实例读取）
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 垂直档位值的字符串表示，无效通道返回空字符串
     */
    public static String VscaleQ(SCPIParam param) {
//        double d=Command.get().getReference().VscaleQ(param.iParam1);
//        return ToolsSCPI.getDouble(d);
        if (ChannelFactory.isRefCh(param.iParam1)) { // 判断是否为有效的参考通道
            RefChannel ref = ChannelFactory.getRefChannel(param.iParam1); // 获取参考通道实例
            return String.valueOf(ref.getVScaleVal()); // 返回参考通道垂直档位值
        }
        return ""; // 无效通道返回空字符串
    }

    /**
     * 选择当前参考通道
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Current(SCPIParam param) {
        Command.get().getReference().Current(param.iParam1, true); // 委托Command中间件选择当前参考通道，true表示通知UI刷新
    }

    /**
     * 设置参考通道的水平偏移（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为偏移增量
     */
    public static void Plus_Hoffset(SCPIParam param) {
        Command.get().getReference().Plus_Hoffset(param.iParam1, param.iParam2, true); // 委托Command中间件增加水平偏移，true表示通知UI刷新
    }

    /**
     * 设置参考通道的垂直偏移（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为偏移增量
     */
    public static void Plus_Voffset(SCPIParam param) {
        Command.get().getReference().Plus_Voffset(param.iParam1, param.iParam2, true); // 委托Command中间件增加垂直偏移，true表示通知UI刷新
    }

    /**
     * 设置参考通道的垂直位置
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为位置值
     */
    public static void Position(SCPIParam param) {
        Command.get().getReference().Position(param.iParam1, param.dParam1, true); // 委托Command中间件设置垂直位置，true表示通知UI刷新
    }

    /**
     * 查询参考通道的垂直位置
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 垂直位置值的字符串表示
     */
    public static String PositionQ(SCPIParam param){
        double d= Command.get().getReference().PositionQ(param.iParam1, true); // 从Command中间件获取垂直位置
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置参考通道的水平位置（时基偏移）
     * @param param SCPI参数封装，iParam1为通道索引，dParam1为位置值
     */
    public static void Timebase_Position(SCPIParam param) {
        Command.get().getReference().Timebase_Position(param.iParam1, param.dParam1, true); // 委托Command中间件设置水平位置，true表示通知UI刷新
    }

    /**
     * 查询参考通道的水平位置（时基偏移）
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 水平位置值的字符串表示
     */
    public static String Timebase_PositionQ(SCPIParam param){
        double d= Command.get().getReference().Timebase_PositionQ(param.iParam1, true); // 从Command中间件获取水平位置
        return ToolsSCPI.getDouble(d); // 将double值格式化为字符串
    }

    /**
     * 设置参考通道的水平位置（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为增量方向
     */
    public static void Plus_Timebase_Position(SCPIParam param) {
        Command.get().getReference().Plus_Timebase_Position(param.iParam1, param.iParam2, true); // 委托Command中间件增加水平位置，true表示通知UI刷新
    }

    /**
     * 设置参考通道的垂直位置（Plus增量方式）
     * @param param SCPI参数封装，iParam1为通道索引，iParam2为增量方向
     */
    public static void Plus_position(SCPIParam param) {
        Command.get().getReference().Plus_position(param.iParam1, param.iParam2, true); // 委托Command中间件增加垂直位置，true表示通知UI刷新
    }

    /**
     * 查询参考通道的采样率
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 采样率字符串
     */
    public static String REF_SRateQ(SCPIParam param){
        return Command.get().getReference().REF_SRateQ(param.iParam1); // 委托Command中间件查询参考通道采样率

    }

    /**
     * 查询参考通道的存储深度
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 存储深度字符串
     */
    public static String REF_MDepthQ(SCPIParam param){
        return Command.get().getReference().REF_MDepthQ(param.iParam1); // 委托Command中间件查询参考通道存储深度

    }

    /**
     * 选择当前参考通道（委托给Channel命令）
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Curr_ref(SCPIParam param){
        Command.get().getChannel().Current(param.iParam1, true); // 委托Command中间件设置当前通道，true表示通知UI刷新
    }

}
