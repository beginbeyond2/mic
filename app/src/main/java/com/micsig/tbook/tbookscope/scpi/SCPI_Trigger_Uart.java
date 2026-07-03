package com.micsig.tbook.tbookscope.scpi; // 定义SCPI触发UART模块的包路径

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发UART配置对象
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于十六进制字符串转整型

/**
 * +--------------------------------------------------------------------------+
 * |                       SCPI_Trigger_Uart                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: SCPI协议 - 触发子系统 - UART总线触发命令处理类                      |
 * | 核心职责: 解析并执行与UART触发相关的SCPI命令(设置/查询触发源、类型、关系、数据、电平) |
 * | 架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command单例访问底层触发配置 |
 * | 数据流向: SCPI命令字符串 → SCPIParam参数解析 → 本类静态方法 → Command中间件 → 底层触发引擎 |
 * | 依赖关系: Command(获取触发UART配置), SCPIParam(命令参数), ToolsSCPI(格式化工具), Tools(十六进制转换) |
 * | 使用场景: 远程控制/自动化测试时，通过SCPI协议设置示波器UART总线触发的各项参数    |
 * +--------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */
public class SCPI_Trigger_Uart {

//     new SCPICommandStruct(":TRIGger:UART:SOURce","SCPI_Trigger_Uart","Source"),//设置UART触发的触发源
//            new SCPICommandStruct(":TRIGger:UART:SOURce?","SCPI_Trigger_Uart","SourceQ"),//查询UART触发的触发源
//            new SCPICommandStruct(":TRIGger:UART:TYPE","SCPI_Trigger_Uart","Type"),//设置UART触发的触发条件
//            new SCPICommandStruct(":TRIGger:UART:TYPE?","SCPI_Trigger_Uart","TypeQ"),//查询UART触发的触发条件
//            new SCPICommandStruct(":TRIGger:UART:RELation","SCPI_Trigger_Uart","Relation"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发关系
//            new SCPICommandStruct(":TRIGger:UART:RELation?","SCPI_Trigger_Uart","RelationQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发关系
//            new SCPICommandStruct(":TRIGger:UART:DATA","SCPI_Trigger_Uart","Data"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发数据。
//            new SCPICommandStruct(":TRIGger:UART:DATA?","SCPI_Trigger_Uart","DataQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发数据。
//            new SCPICommandStruct(":TRIGger:UART:LEVel","SCPI_Trigger_Uart","Level"),//设置UART触发时的触发电平
//            new SCPICommandStruct(":TRIGger:UART:LEVel?","SCPI_Trigger_Uart","LevelQ"),//查询UART触发时的触发电平

    /**
     * 设置UART触发的触发源（包含通道和源选择）
     * @param param SCPI命令参数对象，iParam1为通道索引，iParam2为触发源枚举值
     */
    public static void Source(SCPIParam param){
        Command.get().getTrigger_uart().setSource(param.iParam1, param.iParam2, true); // 调用底层设置UART触发源，iParam1=通道索引，iParam2=源类型，true=立即生效
    }

    /**
     * 查询UART触发的触发源
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return 触发源的整数值字符串
     */
    public static String SourceQ(SCPIParam param){
        int i=Command.get().getTrigger_uart().getSource(param.iParam1); // 从底层获取指定通道的UART触发源值
        return String.valueOf(i); // 将整数值转换为字符串返回
    }

    /**
     * 设置UART触发的触发条件类型
     * 需要先获取当前的condition和number，保持其他参数不变，仅修改type
     * @param param SCPI命令参数对象，iParam1为通道索引，iParam2为触发类型枚举值
     */
    public static void Type(SCPIParam param){
        int condition=Command.get().getTrigger_uart().getCondition(param.iParam1) ; // 获取当前通道的触发条件（保持不变）
        int number=Command.get().getTrigger_uart().getNumber(param.iParam1); // 获取当前通道的触发数据值（保持不变）
        Command.get().getTrigger_uart().setType(param.iParam1, param.iParam2,condition,number,true ); // 设置触发类型，同时保留原有的condition和number，true=立即生效
    }

    /**
     * 查询UART触发的触发条件类型
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return 触发条件类型的字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_uart().getType(param.iParam1); // 从底层获取指定通道的UART触发类型整数值
        return  ToolsSCPI.getuartTriggerType(i); // 将整数类型枚举转换为SCPI标准字符串
    }

    /**
     * 设置UART触发的触发关系（等于/不等于/大于/小于等）
     * 需要先获取当前的type和number，保持其他参数不变，仅修改condition
     * @param param SCPI命令参数对象，iParam1为通道索引，iParam2为关系类型枚举值
     */
    public static void Relation(SCPIParam param){
        int type=Command.get().getTrigger_uart().getType(param.iParam1); // 获取当前通道的触发类型（保持不变）
        int number=Command.get().getTrigger_uart().getNumber(param.iParam1); // 获取当前通道的触发数据值（保持不变）
        Command.get().getTrigger_uart().setType(param.iParam1, type, param.iParam2, number,true); // 设置触发关系，同时保留原有的type和number，true=立即生效
    }

    /**
     * 查询UART触发的触发关系
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return 触发关系的字符串表示
     */
    public static String RelationQ(SCPIParam param){
        int i= Command.get().getTrigger_uart().getCondition(param.iParam1); // 从底层获取指定通道的触发条件（关系）整数值
        return ToolsSCPI.getSerialTriggerCondition(i); // 将整数条件枚举转换为SCPI标准字符串
    }

    /**
     * 设置UART触发的触发数据值
     * 需要先获取当前的type和condition，仅修改number（数据值）
     * @param param SCPI命令参数对象，iParam1为通道索引，sParam1为数据值的十六进制字符串
     */
    public static void Data(SCPIParam param){
        int type=Command.get().getTrigger_uart().getType(param.iParam1); // 获取当前通道的触发类型（保持不变）
        int condition=Command.get().getTrigger_uart().getCondition(param.iParam1); // 获取当前通道的触发条件（保持不变）
        int data= Tools.HexStringToInt(param.sParam1); // 将十六进制字符串转换为整型数据值

        Command.get().getTrigger_uart().setType(param.iParam1, type,condition, data, true); // 设置触发数据，同时保留原有的type和condition，true=立即生效
    }

    /**
     * 查询UART触发的触发数据值
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return 触发数据的十六进制字符串
     */
    public static String DataQ(SCPIParam param){
        int i=Command.get().getTrigger_uart().getNumber(param.iParam1); // 从底层获取指定通道的触发数据整数值
        return Integer.toHexString(i); // 将整数值转换为十六进制字符串返回
    }

    /**
     * 设置UART触发时的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引，dParam1为电平值（伏特）
     */
    public static void Level(SCPIParam param){
        Command.get().getTrigger_uart().setLevel(param.iParam1, param.dParam1, true); // 调用底层设置触发电平，iParam1=通道索引，dParam1=电平值，true=立即生效
    }

    /**
     * 查询UART触发时的触发电平
     * @param param SCPI命令参数对象，iParam1为通道索引
     * @return 触发电平的字符串表示
     */
    public static String LevelQ(SCPIParam param){
        double i=Command.get().getTrigger_uart().getLevel(param.iParam1); // 从底层获取指定通道的触发电平值
        return String.valueOf(i); // 将double值转换为字符串返回
    }

}
