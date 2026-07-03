package com.micsig.tbook.tbookscope.scpi; // 定义SCPI触发超时模块的包路径

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发超时配置对象

/**
 * +--------------------------------------------------------------------------+
 * |                       SCPI_Trigger_Timeout                               |
 * +--------------------------------------------------------------------------+
 * | 模块定位: SCPI协议 - 触发子系统 - 超时(Timeout)触发命令处理类                 |
 * | 核心职责: 解析并执行与超时触发相关的SCPI命令(设置/查询触发源、极性、超时时间、电平) |
 * | 架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command单例访问底层触发配置 |
 * | 数据流向: SCPI命令字符串 → SCPIParam参数解析 → 本类静态方法 → Command中间件 → 底层触发引擎 |
 * | 依赖关系: Command(获取触发超时配置), SCPIParam(命令参数), ToolsSCPI(格式化工具) |
 * | 使用场景: 远程控制/自动化测试时，通过SCPI协议设置示波器超时触发的各项参数        |
 * +--------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */
public class SCPI_Trigger_Timeout {
//      new SCPICommandStruct(":TRIGger:TIMeout:SOURce","SCPI_Trigger_Timeout","Source"),//设置超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:SOURce?","SCPI_Trigger_Timeout","SourceQ"),//查询超时触发的触发源
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity","SCPI_Trigger_Timeout","Polarity"),//设置超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:POLarity?","SCPI_Trigger_Timeout","PolarityQ"),//查询超时触发极性
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME","SCPI_Trigger_Timeout","Time"),//设置超时触发的超时时间
//            new SCPICommandStruct(":TRIGger:TIMeout:TIME?","SCPI_Trigger_Timeout","TimeQ"),//查询超时触发的超时时间

    /**
     * 设置超时触发的触发源
     * @param param SCPI命令参数对象，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_timeout().Source(param.iParam1, true); // 调用底层设置超时触发源，iParam1=通道索引，true=立即生效
    }

    /**
     * 查询超时触发的触发源
     * @param param SCPI命令参数对象
     * @return 触发源通道的字符串表示（如CH1、CH2）
     */
    public static String SourceQ(SCPIParam param) {
        int i=Command.get().getTrigger_timeout().SourceQ(); // 从底层获取超时触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI标准通道字符串
    }

    /**
     * 设置超时触发的极性（正极性/负极性）
     * @param param SCPI命令参数对象，iParam1为极性类型枚举值
     */
    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_timeout().Polarity(param.iParam1, true); // 调用底层设置超时触发极性，iParam1=极性类型，true=立即生效
    }

    /**
     * 查询超时触发的极性
     * @param param SCPI命令参数对象
     * @return 极性类型的字符串表示
     */
    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_timeout().PolarityQ(); // 从底层获取超时触发极性整数值
        return ToolsSCPI.getTriggerTimeoutPolarity(i); // 将整数极性枚举转换为SCPI标准字符串
    }

    /**
     * 设置超时触发的超时时间
     * @param param SCPI命令参数对象，dParam1为超时时间值（秒）
     */
    public static void Time(SCPIParam param) {
        Command.get().getTrigger_timeout().Time(param.dParam1, true); // 调用底层设置超时时间，dParam1=时间值，true=立即生效
    }

    /**
     * 查询超时触发的超时时间
     * @param param SCPI命令参数对象
     * @return 超时时间的字符串表示
     */
    public static String TimeQ(SCPIParam param) {
        double d=Command.get().getTrigger_timeout().TimeQ(); // 从底层获取超时时间值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

    /**
     * 设置超时触发的触发电平
     * @param param SCPI命令参数对象，dParam1为电平值（伏特）
     */
    public static void Level(SCPIParam param){
        Command.get().getTrigger_timeout().Level(param.dParam1, true); // 调用底层设置触发电平，dParam1=电平值，true=立即生效
    }

    /**
     * 查询超时触发的触发电平
     * @param param SCPI命令参数对象
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_timeout().LevelQ(); // 从底层获取触发电平值
        return ToolsSCPI.getDouble(d); // 将double值格式化为SCPI标准字符串
    }

}
