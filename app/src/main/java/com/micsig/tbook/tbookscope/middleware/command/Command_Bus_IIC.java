package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:11
 */
/*
 * +=============================================================================+
 * |                          Command_Bus_IIC 类                                  |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   IIC/I2C串行总线命令模块，管理IIC总线的SDA数据线和SCL时钟线通道分配           |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的IIC SDA数据线通道                                     |
 * |   2. 管理每个总线槽位的IIC SCL时钟线通道                                     |
 * |   3. 设置前校验总线类型是否为IIC，参数索引是否合法                            |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+UI同步）和query方法|
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → SDA/SCL → 校验+保存 → CommandMsgToUI → RxBus → UI更新           |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置IIC总线SDA数据线对应的物理通道                                      |
 * |   2. 设置IIC总线SCL时钟线对应的物理通道                                      |
 * +=============================================================================+
 */
public class Command_Bus_IIC {
//            new SCPICommandStruct(":BUS#:IIC:SDA","SCPI_Bus_IIC","SDA"),
//            new SCPICommandStruct(":BUS#:IIC:SDA?","SCPI_Bus_IIC","SDAQ"),
//            new SCPICommandStruct(":BUS#:IIC:SCL","SCPI_Bus_IIC","SCL"),
//            new SCPICommandStruct(":BUS#:IIC:SCL?","SCPI_Bus_IIC","SCLQ"),

    private final int[] sda=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的SDA数据线通道
    private final int[] scl=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的SCL时钟线通道

    /**
     * 设置IIC总线SDA数据线通道
     * @param s_num 总线槽位索引
     * @param source SDA通道索引
     * @param isUpdateUI 是否更新UI
     */
    public  void SDA(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_IIC)return; // 校验总线类型必须是IIC
        boolean b= ToolsSCPI.isCorrect(s_num,this.sda); // 校验参数索引合法性
        if (b){ // 索引合法
            this.sda[s_num]=source; // 保存SDA通道
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_IIC_SDA); // 设置IIC SDA标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;SDA通道
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询IIC总线SDA数据线通道
     * @param s_num 总线槽位索引
     * @return SDA通道索引
     */
    public  int SDAQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.sda); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.sda[s_num]; // 返回指定槽位的SDA通道
        }
        return this.sda[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置IIC总线SCL时钟线通道
     * @param s_num 总线槽位索引
     * @param source SCL通道索引
     * @param isUpdateUI 是否更新UI
     */
    public  void SCL(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_IIC)return; // 校验总线类型必须是IIC
        boolean b= ToolsSCPI.isCorrect(s_num,this.scl); // 校验参数索引合法性
        if (b){ // 索引合法
            this.scl[s_num]=source; // 保存SCL通道
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_IIC_SCL); // 设置IIC SCL标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;SCL通道
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询IIC总线SCL时钟线通道
     * @param s_num 总线槽位索引
     * @return SCL通道索引
     */
    public  int SCLQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.scl); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.scl[s_num]; // 返回指定槽位的SCL通道
        }
        return this.scl[0]; // 索引不合法时返回默认槽位0的值
    }

}
