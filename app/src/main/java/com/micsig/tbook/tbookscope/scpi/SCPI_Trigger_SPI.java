package com.micsig.tbook.tbookscope.scpi; // 定义SCPI触发SPI模块的包路径

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发SPI配置对象

/**
 * +--------------------------------------------------------------------------+
 * |                        SCPI_Trigger_SPI                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: SCPI协议 - 触发子系统 - SPI总线触发命令处理类                      |
 * | 核心职责: 解析并执行与SPI触发相关的SCPI命令(设置/查询触发源、类型、数据、电平)  |
 * | 架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command单例访问底层触发配置 |
 * | 数据流向: SCPI命令字符串 → SCPIParam参数解析 → 本类静态方法 → Command中间件 → 底层触发引擎 |
 * | 依赖关系: Command(获取触发SPI配置), SCPIParam(命令参数), ToolsSCPI(格式化工具) |
 * | 使用场景: 远程控制/自动化测试时，通过SCPI协议设置示波器SPI总线触发的各项参数      |
 * +--------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */
public class SCPI_Trigger_SPI {
//      new SCPICommandStruct(":TRIGger:SPI:DATA","SCPI_Trigger_SPI","Data"),//设置SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:DATA?","SCPI_Trigger_SPI","DataQ"),//查询SPI触发下的数据值
//            new SCPICommandStruct(":TRIGger:SPI:SOURce","SCPI_Trigger_SPI","Source"),//设置SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:SOURce?","SCPI_Trigger_SPI","SourceQ"),//查询SPI触发的触发源
//            new SCPICommandStruct(":TRIGger:SPI:LEVel","SCPI_Trigger_SPI","Level"),//设置SPI触发时的触发电平
//            new SCPICommandStruct(":TRIGger:SPI:LEVel?","SCPI_Trigger_SPI","LevelQ"),//查询SPI触发时的触发电平

    /**
     * 设置SPI触发的触发源（当前为空实现，预留接口）
     * @param param SCPI命令参数对象，包含触发源信息
     */
    public static void Source(SCPIParam param){} // 空实现，预留SPI触发源设置接口

    /**
     * 查询SPI触发的触发源（当前为空实现，预留接口）
     * @param param SCPI命令参数对象
     */
    public static void SourceQ(SCPIParam param){} // 空实现，预留SPI触发源查询接口

    /**
     * 设置SPI触发的触发条件类型
     * @param param SCPI命令参数对象，iParam1为通道索引，iParam2为触发类型枚举值
     */
    public static void Type(SCPIParam param){
        Command.get().getTrigger_spi().Type(param.iParam1, param.iParam2, true); // 调用底层命令设置SPI触发类型，iParam1=通道索引，iParam2=触发类型，true=立即生效
    }

    /**
     * 查询SPI触发的触发条件类型
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return SPI触发类型的字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i=Command.get().getTrigger_spi().TypeQ(param.iParam1); // 从底层获取SPI触发类型整数值，iParam1=通道索引
        return ToolsSCPI.getSpiTriggerType(i); // 将整数类型枚举转换为SCPI标准字符串
    }

    /**
     * 设置SPI触发的数据值
     * @param param SCPI命令参数对象，iParam1为通道索引，sParam1为数据值的十六进制字符串
     */
    public static void Data(SCPIParam param){
        Command.get().getTrigger_spi().Data(param.iParam1, param.sParam1.toUpperCase(), true); // 设置SPI触发数据，将字符串转大写后传入，true=立即生效
    }

    /**
     * 查询SPI触发的数据值
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return SPI触发数据的十六进制字符串
     */
    public static String DataQ(SCPIParam param){
        String s=Command.get().getTrigger_spi().DataQ(param.iParam1); // 从底层获取SPI触发数据字符串
        return s; // 返回数据值字符串
    }



    /**
     * 设置SPI触发时CLK（时钟）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引，dParam1为电平值
     */
    public static void LevelCLK(SCPIParam param){
        Command.get().getTrigger_spi().LevelCLK(param.iParam1, param.dParam1, true); // 设置CLK线触发电平，dParam1=电平值，true=立即生效
    }

    /**
     * 查询SPI触发时CLK（时钟）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return CLK线触发电平的字符串表示
     */
    public static String LevelCLKQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelCLKQ(param.iParam1); // 从底层获取CLK线触发电平值
        return String.valueOf(d); // 将double值转换为字符串返回
    }

    /**
     * 设置SPI触发时DATA（数据）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引，dParam1为电平值
     */
    public static void LevelData(SCPIParam param){
        Command.get().getTrigger_spi().LevelData(param.iParam1, param.dParam1, true); // 设置DATA线触发电平，dParam1=电平值，true=立即生效
    }

    /**
     * 查询SPI触发时DATA（数据）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return DATA线触发电平的字符串表示
     */
    public static String LevelDataQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelDataQ(param.iParam1); // 从底层获取DATA线触发电平值
        return String.valueOf(d); // 将double值转换为字符串返回
    }

    /**
     * 设置SPI触发时CS（片选）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引，dParam1为电平值
     */
    public static void LevelCS(SCPIParam param){
        Command.get().getTrigger_spi().LevelCS(param.iParam1, param.dParam1, true); // 设置CS线触发电平，dParam1=电平值，true=立即生效
    }

    /**
     * 查询SPI触发时CS（片选）线的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return CS线触发电平的字符串表示
     */
    public static String LevelCSQ(SCPIParam param){
        double d=Command.get().getTrigger_spi().LevelCSQ(param.iParam1); // 从底层获取CS线触发电平值
        return String.valueOf(d); // 将double值转换为字符串返回
    }

}
