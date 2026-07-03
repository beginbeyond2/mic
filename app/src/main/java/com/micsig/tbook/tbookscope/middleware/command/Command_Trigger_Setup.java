package com.micsig.tbook.tbookscope.middleware.command;  // 命令中间件包，存放各类触发器命令模型

/*
 * +=============================================================================+
 * |                  Command_Trigger_Setup - 建立保持时间触发命令模型（桩实现）           |
 * +=============================================================================+
 * | 模块定位 : middleware.command 子包，建立/保持时间触发器的参数存储与UI同步（桩实现）     |
 * | 核心职责 : 定义建立/保持时间触发器的SCPI接口框架，当前所有方法为空实现（桩）           |
 * | 架构设计 : 空壳模式，方法签名已定义但逻辑未实现，预留后续开发                         |
 * | 数据流向 : 暂无实际数据流向                                                           |
 * | 依赖关系 : 无                                                                        |
 * | 使用场景 : 预留接口，待实现建立/保持时间触发功能时填充具体逻辑                         |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2018/1/12.
 */

public class Command_Trigger_Setup {
//    new SCPICommandStruct(":TRIGger:SETup:CLOCk","SCPI_Trigger_Setup","Clock"),//设置建立保持时间触发的时钟信号源
//            new SCPICommandStruct(":TRIGger:SETup:CLOCk?","SCPI_Trigger_Setup","ClockQ"),//查询建立保持时间触发的时钟信号源
//            new SCPICommandStruct(":TRIGger:SETup:DATA","SCPI_Trigger_Setup","Data"),//设置建立保持时间触发的数据信号源
//            new SCPICommandStruct(":TRIGger:SETup:DATA?","SCPI_Trigger_Setup","DataQ"),//查询建立保持时间触发的数据信号源
//            new SCPICommandStruct(":TRIGger:SETup:CEDGe","SCPI_Trigger_Setup","Cedge"),//设置建立保持时间触发的时钟边沿类型
//            new SCPICommandStruct(":TRIGger:SETup:CEDGe?","SCPI_Trigger_Setup","CedgeQ"),//查询建立保持时间触发的时钟边沿类型
//            new SCPICommandStruct(":TRIGger:SETup:STIMe","SCPI_Trigger_Setup","STime"),//设置建立保持时间触发的建立时间
//            new SCPICommandStruct(":TRIGger:SETup:STIMe?","SCPI_Trigger_Setup","STimeQ"),//查询建立保持时间触发的建立时间
//            new SCPICommandStruct(":TRIGger:SETup:HTIMe","SCPI_Trigger_Setup","HTime"),//设置建立保持时间触发的保持时间
//            new SCPICommandStruct(":TRIGger:SETup:HTIMe?","SCPI_Trigger_Setup","HTimeQ"),//查询建立保持时间触发的保持时间
//            new SCPICommandStruct(":TRIGger:SETup:CLEVel","SCPI_Trigger_Setup","CLevel"),//设置建立保持时间触发的时钟源触发电平
//            new SCPICommandStruct(":TRIGger:SETup:CLEVel?","SCPI_Trigger_Setup","CLevelQ"),//查询建立保持时间触发的时钟源触发电平
//            new SCPICommandStruct(":TRIGger:SETup:DLEVel","SCPI_Trigger_Setup","DLevel"),//设置建立保持时间触发的数据源触发电平
//            new SCPICommandStruct(":TRIGger:SETup:DLEVel?","SCPI_Trigger_Setup","DLevelQ"),//查询建立保持时间触发的数据源触发电平

    /**
     * 设置建立保持时间触发的时钟信号源（桩实现）
     *
     * @param index       时钟信号源索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void Clock(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的时钟信号源（桩实现）
     *
     * @return 时钟信号源索引（默认返回0）
     */
    public  int ClockQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的数据信号源（桩实现）
     *
     * @param index       数据信号源索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void Data(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的数据信号源（桩实现）
     *
     * @return 数据信号源索引（默认返回0）
     */
    public  int DataQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的时钟边沿类型（桩实现）
     *
     * @param index       时钟边沿类型索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void Cedge(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的时钟边沿类型（桩实现）
     *
     * @return 时钟边沿类型索引（默认返回0）
     */
    public  int CedgeQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的建立时间（桩实现）
     *
     * @param index       建立时间索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void STime(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的建立时间（桩实现）
     *
     * @return 建立时间索引（默认返回0）
     */
    public  int STimeQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的保持时间（桩实现）
     *
     * @param index       保持时间索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void HTime(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的保持时间（桩实现）
     *
     * @return 保持时间索引（默认返回0）
     */
    public  int HTimeQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的时钟源触发电平（桩实现）
     *
     * @param index       时钟源触发电平索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void CLevel(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的时钟源触发电平（桩实现）
     *
     * @return 时钟源触发电平索引（默认返回0）
     */
    public  int CLevelQ(){return 0;}  // 桩实现，默认返回0

    /**
     * 设置建立保持时间触发的数据源触发电平（桩实现）
     *
     * @param index       数据源触发电平索引
     * @param isUpdateUI  是否通知UI刷新
     */
    public  void DLevel(int index,boolean isUpdateUI){}  // 桩实现，待后续填充逻辑

    /**
     * 查询建立保持时间触发的数据源触发电平（桩实现）
     *
     * @return 数据源触发电平索引（默认返回0）
     */
    public  int DLevelQ(){return 0;}  // 桩实现，默认返回0

}
