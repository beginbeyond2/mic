package com.micsig.tbook.tbookscope.scpi; // 包声明：SCPI触发器命令处理模块

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入Command中间件，用于获取触发器配置对象
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于数据类型转换

/**
 * Created by liwb on 2018/1/12.
 *
 * +=============================================================================================================+
 * |                                           SCPI_Trigger_IIC                                                  |
 * +=============================================================================================================+
 * | 模块定位 : SCPI命令处理器 —— I2C总线触发模块                                                               |
 * | 核心职责 : 解析并执行I2C总线触发相关的SCPI命令（设置/查询触发源、触发类型、                                   |
 * |            触发地址、触发关系、触发数据1/数据2、时钟电平/数据电平）                                           |
 * | 架构设计 : 静态方法类，每个方法对应一条SCPI命令；设置命令读取当前值后仅修改目标字段，                           |
 * |            查询命令读取配置并通过ToolsSCPI/Integer.toHexString转换为SCPI响应字符串                           |
 * | 数据流向 : SCPI解析器 → SCPIParam参数 → 本类静态方法 → Command中间件 → 底层硬件配置                         |
 * | 依赖关系 : Command（触发器配置读写）、ToolsSCPI（枚举值→字符串转换）、                                       |
 * |            Tools（十六进制字符串→整数转换）                                                                 |
 * | 使用场景 : 仪器作为I2C总线分析设备时，用户通过SCPI命令配置I2C协议触发条件                                    |
 * +=============================================================================================================+
 */

public class SCPI_Trigger_IIC {
//     new SCPICommandStruct(":TRIGger:IIC:SOURce","SCPI_Trigger_IIC","Source"),//设置IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:SOURce?","SCPI_Trigger_IIC","SourceQ"),//查询IIC触发的触发源
//            new SCPICommandStruct(":TRIGger:IIC:TYPE","SCPI_Trigger_IIC","Type"),//设置IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:TYPE?","SCPI_Trigger_IIC","TypeQ"),//查询IIC触发的触发类型
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess","SCPI_Trigger_IIC","Address"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，设置IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:ADDRess?","SCPI_Trigger_IIC","AddressQ"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，查询IIC总线触发的触发地址
//            new SCPICommandStruct(":TRIGger:IIC:RELation","SCPI_Trigger_IIC","Relation"),//当IIC触发条件为RDATa时，设置IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:RELation?","SCPI_Trigger_IIC","RelationQ"),//当IIC触发条件为RDATa时，查询IIC总线触发的触发关系
//            new SCPICommandStruct(":TRIGger:IIC:DATA","SCPI_Trigger_IIC","Data"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:DATA?","SCPI_Trigger_IIC","DataQ"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据
//            new SCPICommandStruct(":TRIGger:IIC:LEVel","SCPI_Trigger_IIC","Level"),//设置IIC触发时的触发电平
//            new SCPICommandStruct(":TRIGger:IIC:LEVel?","SCPI_Trigger_IIC","LevelQ"),//查询IIC触发时的触发电平

    /**
     * 设置IIC触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void Source(SCPIParam param){} // 设置IIC触发的触发源（当前为空实现）

    /**
     * 查询IIC触发的触发源。
     * @param param SCPI命令参数，iParam1为通道索引
     */
    public static void SourceQ(SCPIParam param){} // 查询IIC触发的触发源（当前为空实现）


    /**
     * 设置IIC触发的触发类型。
     * 读取当前地址、数据1、数据2、条件，仅更新触发类型，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的触发类型值
     */
    public static void Type(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前IIC触发类型
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1); // 获取当前触发地址
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1); // 获取当前触发数据1
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1); // 获取当前触发数据2
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        Command.get().getTrigger_iic().setType(param.iParam1, param.iParam2, addr,data1,data2,condition,true); // 设置新的触发类型，保持其余参数不变，并通知硬件
    }

    /**
     * 查询IIC触发的触发类型。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发类型的SCPI字符串表示
     */
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前IIC触发类型
        return ToolsSCPI.getIICTriggerType(i); // 将触发类型枚举值转换为SCPI字符串
    }

    /**
     * 设置IIC触发的触发地址。
     * 读取当前触发类型、数据1、数据2、条件，仅更新地址值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为地址的十六进制字符串
     */
    public static void Address(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前触发类型
        //int addr=Command.get().getTrigger_iic().getAddr(param.iParam1);
        int addr= Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数地址值
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1); // 获取当前触发数据1
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1); // 获取当前触发数据2
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr, data1,data2,condition,true); // 设置新的地址值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询IIC触发的触发地址。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发地址的十六进制字符串
     */
    public static String AddressQ(SCPIParam param){
        int i=Command.get().getTrigger_iic().getAddr(param.iParam1); // 获取当前触发地址
        return Integer.toHexString(i); // 将地址值转换为十六进制字符串返回
    }

    /**
     * 设置IIC触发的触发关系（数据比较条件）。
     * 读取当前触发类型、地址、数据1、数据2，仅更新条件值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，iParam2为新的条件值
     */
    public static void Relation(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前触发类型
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1); // 获取当前触发地址
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1); // 获取当前触发数据1
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1); // 获取当前触发数据2
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr,data1,data2, param.iParam2, true); // 设置新的条件值（替换原条件），并通知硬件
    }

    /**
     * 查询IIC触发的触发关系（数据比较条件）。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发关系的SCPI字符串表示
     */
    public static String RelationQ(SCPIParam param){
        int i= Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        return ToolsSCPI.getSerialTriggerCondition(i); // 将条件枚举值转换为SCPI字符串
    }

    /**
     * 设置IIC触发的触发数据1。
     * 读取当前触发类型、地址、数据2、条件，仅更新数据1值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为数据的十六进制字符串
     */
    public static void Data1(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前触发类型
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1); // 获取当前触发地址
        //int data1=Command.get().getTrigger_iic().getData1(param.iParam1);
        int data=Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数数据1值
        int data2=Command.get().getTrigger_iic().getData2(param.iParam1); // 获取当前触发数据2
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr, data, data2,condition,true); // 设置新的数据1值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询IIC触发的触发数据1。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发数据1的十六进制字符串
     */
    public static String Data1Q(SCPIParam param){
        int i=Command.get().getTrigger_iic().getData1(param.iParam1); // 获取当前触发数据1
        return Integer.toHexString(i); // 将数据1值转换为十六进制字符串返回
    }

    /**
     * 设置IIC触发的触发数据2。
     * 读取当前触发类型、地址、数据1、条件，仅更新数据2值，其余参数保持不变。
     * @param param SCPI命令参数，iParam1为通道索引，sParam1为数据的十六进制字符串
     */
    public static void Data2(SCPIParam param){
        int type=Command.get().getTrigger_iic().getType(param.iParam1); // 获取当前触发类型
        int addr=Command.get().getTrigger_iic().getAddr(param.iParam1); // 获取当前触发地址
        int data1=Command.get().getTrigger_iic().getData1(param.iParam1); // 获取当前触发数据1
        //int data2=Command.get().getTrigger_iic().getData2(param.iParam1);
        int data2=Tools.HexStringToInt(param.sParam1); // 将十六进制字符串参数转换为整数数据2值
        int condition=Command.get().getTrigger_iic().getCondition(param.iParam1); // 获取当前触发关系条件
        Command.get().getTrigger_iic().setType(param.iParam1, type, addr,data1, data2, condition,true); // 设置新的数据2值，保持其余参数不变，并通知硬件
    }

    /**
     * 查询IIC触发的触发数据2。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 触发数据2的十六进制字符串
     */
    public static String Data2Q(SCPIParam param){
        int i=Command.get().getTrigger_iic().getData2(param.iParam1); // 获取当前触发数据2
        return Integer.toHexString(i); // 将数据2值转换为十六进制字符串返回
    }

    /**
     * 设置IIC触发的时钟线触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为时钟线电平值
     */
    public static void LevelClock(SCPIParam param){
        Command.get().getTrigger_iic().setLevelClock(param.iParam1, param.dParam1, true); // 设置IIC时钟线触发电平，并通知硬件
    }

    /**
     * 查询IIC触发的时钟线触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 时钟线触发电平的字符串表示
     */
    public static String LevelClockQ(SCPIParam param){
        double d=Command.get().getTrigger_iic().getLevelClock(param.iParam1); // 获取当前时钟线触发电平
        return String.valueOf(d); // 将时钟线触发电平转换为字符串返回
    }

    /**
     * 设置IIC触发的数据线触发电平。
     * @param param SCPI命令参数，iParam1为通道索引，dParam1为数据线电平值
     */
    public static void LevelData(SCPIParam param){
        Command.get().getTrigger_iic().setLevelData(param.iParam1, param.dParam1, true); // 设置IIC数据线触发电平，并通知硬件
    }

    /**
     * 查询IIC触发的数据线触发电平。
     * @param param SCPI命令参数，iParam1为通道索引
     * @return 数据线触发电平的字符串表示
     */
    public static String LevelDataQ(SCPIParam param){
        double d=Command.get().getTrigger_iic().getLevelData(param.iParam1); // 获取当前数据线触发电平
        return String.valueOf(d); // 将数据线触发电平转换为字符串返回
    }
}
