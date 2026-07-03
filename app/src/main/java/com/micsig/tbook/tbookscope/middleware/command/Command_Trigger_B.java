package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Trigger_B                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: B触发命令处理模块（当前为空实现）                                  |
 * | 核心职责: 处理SCPI B触发相关指令，包括触发源、边沿、耦合、序列类型、触发电平  |
 * |          的设置与查询（所有方法当前为空壳，仅返回默认值0）                    |
 * | 架构设计: 命令模式，作为Command子模块                                        |
 * | 数据流向: SCPI指令 → 本类(空实现)                                           |
 * | 依赖关系: 无外部依赖                                                        |
 * | 使用场景: B触发功能预留接口，待后续实现                                     |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Trigger_B {
//     new SCPICommandStruct(":TRIGger:B:SOURce","SCPI_Trigger_B","Source"),//选择B触发的触发源
//            new SCPICommandStruct(":TRIGger:B:SOURce?","SCPI_Trigger_B","SourceQ"),//查询B触发的触发源
//            new SCPICommandStruct(":TRIGger:B:EDGE","SCPI_Trigger_B","Edge"),//设置B触发的触发斜率
//            new SCPICommandStruct(":TRIGger:B:EDGE?","SCPI_Trigger_B","EdgeQ"),//查询B触发的触发斜率
//            new SCPICommandStruct(":TRIGger:B:COUPle","SCPI_Trigger_B","Couple"),//选择B触发耦合方式
//            new SCPICommandStruct(":TRIGger:B:COUPle?","SCPI_Trigger_B","CoupleQ"),//查询B触发耦合方式
//            new SCPICommandStruct(":TRIGger:B:SEQuence","SCPI_Trigger_B","Sequence"),//设置B触发的触发类型（B在A后触发时间/事件）
//            new SCPICommandStruct(":TRIGger:B:SEQuence?","SCPI_Trigger_B","SequenceQ"),//查询B触发的触发类型
//            new SCPICommandStruct(":TRIGger:B:LEVel","SCPI_Trigger_B","Level"),//设置B触发时的触发电平
//            new SCPICommandStruct(":TRIGger:B:LEVel?","SCPI_Trigger_B","LevelQ"),//查询B触发时的触发电平

    /**
     * 设置B触发的触发源（空实现）
     *
     * @param index       触发源索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Source(int index,boolean isUpdateUI){}
    /**
     * 查询B触发的触发源（空实现）
     *
     * @return 默认返回0
     */
    public  int SourceQ(){return 0;}
    /**
     * 设置B触发的边沿类型（空实现）
     *
     * @param index       边沿索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Edge(int index,boolean isUpdateUI){}
    /**
     * 查询B触发的边沿类型（空实现）
     *
     * @return 默认返回0
     */
    public  int EdgeQ(){return 0;}
    /**
     * 设置B触发的耦合方式（空实现）
     *
     * @param index       耦合方式索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Couple(int index,boolean isUpdateUI){}
    /**
     * 查询B触发的耦合方式（空实现）
     *
     * @return 默认返回0
     */
    public  int CoupleQ(){return 0;}
    /**
     * 设置B触发的序列类型（空实现）
     *
     * @param index       序列类型索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Sequence(int index,boolean isUpdateUI){}
    /**
     * 查询B触发的序列类型（空实现）
     *
     * @return 默认返回0
     */
    public  int SequenceQ(){return 0;}
    /**
     * 设置B触发的触发电平（空实现）
     *
     * @param index       电平索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Level(int index,boolean isUpdateUI){}
    /**
     * 查询B触发的触发电平（空实现）
     *
     * @return 默认返回0
     */
    public  int LevelQ(){return 0;}

}
