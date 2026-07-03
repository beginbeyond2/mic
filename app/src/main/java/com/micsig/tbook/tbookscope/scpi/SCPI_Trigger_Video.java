package com.micsig.tbook.tbookscope.scpi; // 定义SCPI触发视频模块的包路径

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发视频配置对象

/**
 * +--------------------------------------------------------------------------+
 * |                       SCPI_Trigger_Video                                 |
 * +--------------------------------------------------------------------------+
 * | 模块定位: SCPI协议 - 触发子系统 - 视频(Video)触发命令处理类                    |
 * | 核心职责: 解析并执行与视频触发相关的SCPI命令(设置/查询触发源、极性、标准、同步模式、频率、行号) |
 * | 架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command单例访问底层触发配置 |
 * | 数据流向: SCPI命令字符串 → SCPIParam参数解析 → 本类静态方法 → Command中间件 → 底层触发引擎 |
 * | 依赖关系: Command(获取触发视频配置), SCPIParam(命令参数), ToolsSCPI(格式化工具) |
 * | 使用场景: 远程控制/自动化测试时，通过SCPI协议设置示波器视频触发的各项参数        |
 * +--------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */
public class SCPI_Trigger_Video {
//     new SCPICommandStruct(":TRIGger:VIDeo:SOURce","SCPI_Trigger_Video","Source"),//设置视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:SOURce?","SCPI_Trigger_Video","SourceQ"),//查询视频触发的触发源
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity","SCPI_Trigger_Video","Polarity"),//设置视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:POLarity?","SCPI_Trigger_Video","PolarityQ"),//查询视频触发的极性
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard","SCPI_Trigger_Video","Standard"),//设置视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:STANdard?","SCPI_Trigger_Video","StandardQ"),//查询视频触发时的视频标准
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe","SCPI_Trigger_Video","Amode"),//设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AMODe?","SCPI_Trigger_Video","AmodeQ"),//查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe","SCPI_Trigger_Video","Bmode"),//设置触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:BMODe?","SCPI_Trigger_Video","BmodeQ"),//查询触发标准为720P、1080P时视频触发的同步类型
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence","SCPI_Trigger_Video","Afrequence"),//设置触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:AFRequence?","SCPI_Trigger_Video","AfrequenceQ"),//查询触发标准为720P、1080I时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence","SCPI_Trigger_Video","Bfrequence"),//设置触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence?","SCPI_Trigger_Video","BfrequenceQ"),//查询触发标准为1080P时视频触发的信号频率
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE","SCPI_Trigger_Video","Line"),
//            new SCPICommandStruct(":TRIGger:VIDeo:LINE?","SCPI_Trigger_Video","LineQ"),

    /**
     * 设置视频触发的触发源
     * @param param SCPI命令参数对象，iParam1为触发源通道索引
     */
    public static void Source(SCPIParam param) {
        Command.get().getTrigger_video().Source(param.iParam1, true); // 调用底层设置视频触发源，iParam1=通道索引，true=立即生效
    }

    /**
     * 查询视频触发的触发源
     * @param param SCPI命令参数对象
     * @return 触发源通道的字符串表示（如CH1、CH2）
     */
    public static String SourceQ(SCPIParam param) {
        int i= Command.get().getTrigger_video().SourceQ(); // 从底层获取视频触发源通道索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为SCPI标准通道字符串
    }

    /**
     * 设置视频触发的极性（正极性/负极性）
     * @param param SCPI命令参数对象，iParam1为极性类型枚举值
     */
    public static void Polarity(SCPIParam param) {
        Command.get().getTrigger_video().Polarity(param.iParam1, true); // 调用底层设置视频触发极性，iParam1=极性类型，true=立即生效
    }

    /**
     * 查询视频触发的极性
     * @param param SCPI命令参数对象
     * @return 极性类型的字符串表示
     */
    public static String PolarityQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().PolarityQ(); // 从底层获取视频触发极性整数值
        return ToolsSCPI.getTriggerVideoPolarity(i); // 将整数极性枚举转换为SCPI标准字符串
    }

    /**
     * 设置视频触发时的视频标准（PAL/NTSC/SECAM/720P/1080I/1080P等）
     * @param param SCPI命令参数对象，iParam1为视频标准枚举值
     */
    public static void Standard(SCPIParam param) {
        Command.get().getTrigger_video().Standard(param.iParam1, true); // 调用底层设置视频标准，iParam1=标准类型，true=立即生效
    }

    /**
     * 查询视频触发时的视频标准
     * @param param SCPI命令参数对象
     * @return 视频标准的字符串表示
     */
    public static String StandardQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().StandardQ(); // 从底层获取视频标准整数值
        return ToolsSCPI.getTriggerVideoStandard(i); // 将整数标准枚举转换为SCPI标准字符串
    }

    /**
     * 设置触发标准为PAL/SECAM/NTSC/1080I时视频触发的同步类型
     * @param param SCPI命令参数对象，iParam1为同步模式枚举值
     */
    public static void Amode(SCPIParam param) {
        Command.get().getTrigger_video().Amode(param.iParam1, true); // 调用底层设置A模式同步类型，iParam1=同步模式，true=立即生效
    }

    /**
     * 查询触发标准对应的同步模式
     * 根据当前视频标准判断：720P(3)/1080P(5)使用Bmode，其他使用Amode
     * @param param SCPI命令参数对象
     * @return 同步模式的字符串表示
     */
    public static String AmodeQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().AmodeQ(); // 从底层获取同步模式整数值
        int standard=Command.get().getTrigger_video().StandardQ(); // 从底层获取当前视频标准整数值
        if ( standard==3 || standard==5){ // 判断是否为720P(3)或1080P(5)标准
            return ToolsSCPI.getTriggerVideoBmode(i); // 720P/1080P标准使用Bmode格式化
        }else {
            return ToolsSCPI.getTriggerVideoAmode(i); // 其他标准(PAL/SECAM/NTSC/1080I)使用Amode格式化
        }
    }

    /**
     * 设置触发标准为720P/1080P时视频触发的同步类型
     * @param param SCPI命令参数对象，iParam1为同步模式枚举值
     */
    public static void Bmode(SCPIParam param) {
        Command.get().getTrigger_video().Bmode(param.iParam1, true); // 调用底层设置B模式同步类型，iParam1=同步模式，true=立即生效
    }

    /**
     * 查询触发标准为720P/1080P时视频触发的同步类型
     * @param param SCPI命令参数对象
     * @return 同步模式的字符串表示
     */
    public static String BmodeQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().BmodeQ(); // 从底层获取B模式同步类型整数值
        return ToolsSCPI.getTriggerVideoBmode(i); // 将整数同步模式枚举转换为SCPI标准字符串
    }

    /**
     * 设置视频触发的信号频率（适用于720P/1080I标准）
     * @param param SCPI命令参数对象，iParam1为频率枚举值
     */
    public static void Afrequence(SCPIParam param) {
        Command.get().getTrigger_video().Afrequence(param.iParam1, true); // 调用底层设置A频率，iParam1=频率类型，true=立即生效
    }

    /**
     * 查询视频触发的信号频率
     * @param param SCPI命令参数对象
     * @return 信号频率的字符串表示
     */
    public static String AfrequenceQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().AfrequenceQ(); // 从底层获取信号频率整数值
        return ToolsSCPI.getTriggerVideoBfrequence(i); // 将整数频率枚举转换为SCPI标准字符串（复用Bfrequence格式化）
    }

    /**
     * 设置视频触发的信号频率（适用于1080P标准）
     * @param param SCPI命令参数对象，iParam1为频率枚举值
     */
    public static void Bfrequence(SCPIParam param) {
        Command.get().getTrigger_video().Bfrequence(param.iParam1, true); // 调用底层设置B频率，iParam1=频率类型，true=立即生效
    }

    /**
     * 查询视频触发的信号频率（1080P标准）
     * @param param SCPI命令参数对象
     * @return 信号频率的字符串表示
     */
    public static String BfrequenceQ(SCPIParam param) {
        int i=Command.get().getTrigger_video().BfrequenceQ(); // 从底层获取B频率整数值
        return ToolsSCPI.getTriggerVideoBfrequence(i); // 将整数频率枚举转换为SCPI标准字符串
    }

    /**
     * 设置视频触发时的指定行号
     * @param param SCPI命令参数对象，iParam1为行号值
     */
    public static void Line(SCPIParam param){
        Command.get().getTrigger_video().Line(param.iParam1, true); // 调用底层设置触发行号，iParam1=行号，true=立即生效
    }

    /**
     * 查询视频触发时的指定行号
     * @param param SCPI命令参数对象
     * @return 行号的字符串表示
     */
    public static String LineQ(SCPIParam param){
        int i=Command.get().getTrigger_video().LineQ(); // 从底层获取触发行号整数值
        return ToolsSCPI.getInt(i); // 将整数值格式化为SCPI标准字符串
    }
}
