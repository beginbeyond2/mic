package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:55
 */
/*
 * +=============================================================================+
 * |                          Command_Bus_Lin 类                                  |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   LIN串行总线命令模块，管理LIN总线的通道、空闲电平、波特率、自定义波特率、     |
 * |   LIN协议类型等参数                                                           |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的LIN通道和协议类型                                      |
 * |   2. 管理每个总线槽位的LIN空闲电平                                            |
 * |   3. 管理每个总线槽位的LIN标准波特率索引和自定义波特率（含范围校验）          |
 * |   4. 设置前校验总线类型是否为LIN，参数索引是否合法                            |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+UI同步）和query方法|
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → LinType/Channel/IdLevel/BaudRate/UserBaud → 校验+保存 → RxBus    |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置LIN总线协议类型                                                     |
 * |   2. 设置LIN总线通道和波特率                                                 |
 * +=============================================================================+
 */
public class Command_Bus_Lin {
//     new SCPICommandStruct(":BUS#:LIN:CHANnel","SCPI_Bus_Lin","Channel"),
//            new SCPICommandStruct(":BUS#:LIN:CHANnel?","SCPI_Bus_Lin","ChannelQ"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl","SCPI_Bus_Lin","IdLevel"),
//            new SCPICommandStruct(":BUS#:LIN:IDLElvl?","SCPI_Bus_Lin","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate","SCPI_Bus_Lin","BaudRate"),
//            new SCPICommandStruct(":BUS#:LIN:BAUDrate?","SCPI_Bus_Lin","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud","SCPI_Bus_Lin","UserBaud"),
//            new SCPICommandStruct(":BUS#:LIN:USERbaud?","SCPI_Bus_Lin","UserBaudQ"),

    private final int[] src=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的LIN通道源
    private final int[] idLevel=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的LIN空闲电平
    private final int[] baudRate=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的LIN标准波特率索引
    private final int[] userBaud=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的LIN自定义波特率值
    private final int[] linType=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的LIN协议类型

    private final int minBaud=2400; // LIN自定义波特率最小值
    private final int maxBaud=625000; // LIN自定义波特率最大值

    /**
     * 设置LIN总线协议类型
     * @param s_num 总线槽位索引
     * @param linType LIN协议类型索引
     * @param isUpdateUI 是否更新UI
     */
    public void LinType(int s_num,int linType,boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return; // 校验总线类型必须是LIN
        boolean b = ToolsSCPI.isCorrect(s_num, this.linType); // 校验参数索引合法性
        if (b){ // 索引合法
            this.linType[s_num]=linType; // 保存LIN协议类型
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_TYPE); // 设置LIN类型标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(linType); // 拼接参数：总线号;LIN类型
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }

    }

    /**
     * 查询LIN总线协议类型
     * @param s_num 总线槽位索引
     * @return LIN协议类型索引
     */
    public int LinTypeQ(int s_num) {
        boolean b = ToolsSCPI.isCorrect(s_num, this.linType); // 校验参数索引合法性
        if (b) { // 索引合法
            return this.linType[s_num]; // 返回指定槽位的LIN协议类型
        }
        return this.linType[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置LIN总线通道
     * @param s_num 总线槽位索引
     * @param source 通道索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return; // 校验总线类型必须是LIN
        boolean b= ToolsSCPI.isCorrect(s_num,this.src); // 校验参数索引合法性
        if (b){ // 索引合法
            this.src[s_num]=source; // 保存LIN通道源
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_Channel); // 设置LIN通道标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;通道
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询LIN总线通道
     * @param s_num 总线槽位索引
     * @return 通道索引
     */
    public  int ChannelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.src); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.src[s_num]; // 返回指定槽位的LIN通道
        }
        return this.src[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置LIN总线空闲电平
     * @param s_num 总线槽位索引
     * @param id_level 空闲电平索引
     * @param isUpdateUI 是否更新UI
     */
    public  void IdLevel(int s_num,int id_level,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return; // 校验总线类型必须是LIN
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel); // 校验参数索引合法性
        if (b){ // 索引合法
            this.idLevel[s_num]=id_level; // 保存LIN空闲电平
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_IdLevel); // 设置LIN空闲电平标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(id_level); // 拼接参数：总线号;空闲电平
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询LIN总线空闲电平
     * @param s_num 总线槽位索引
     * @return 空闲电平索引
     */
    public  int IdLevelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.idLevel[s_num]; // 返回指定槽位的空闲电平
        }
        return this.idLevel[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置LIN总线标准波特率
     * @param s_num 总线槽位索引
     * @param baudRateIndex 波特率索引
     * @param isUpdateUI 是否更新UI
     */
    public  void BaudRate(int s_num,int baudRateIndex,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return; // 校验总线类型必须是LIN
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引合法性
        if (b){ // 索引合法
            this.baudRate[s_num]=baudRateIndex; // 保存LIN标准波特率索引
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_BaudRate); // 设置LIN波特率标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRateIndex); // 拼接参数：总线号;波特率索引
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询LIN总线标准波特率
     * @param s_num 总线槽位索引
     * @return 波特率索引
     */
    public  int BaudRateQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.baudRate[s_num]; // 返回指定槽位的波特率索引
        }
        return this.baudRate[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置LIN总线自定义波特率
     * @param s_num 总线槽位索引
     * @param userBaud 自定义波特率值（-1表示不设置）
     * @param isUpdateUI 是否更新UI
     */
    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_LIN)return; // 校验总线类型必须是LIN
        if (userBaud!=-1) { // 非-1时进行范围校验
            if (userBaud < minBaud) userBaud = minBaud; // 低于最小值则限制
            if (userBaud > maxBaud) userBaud = maxBaud; // 超过最大值则限制
        }
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud); // 校验参数索引合法性
        if (b){ // 索引合法
            this.userBaud[s_num]=userBaud; // 保存LIN自定义波特率
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Lin_Userbaud); // 设置LIN自定义波特率标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(userBaud); // 拼接参数：总线号;自定义波特率
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询LIN总线自定义波特率
     * @param s_num 总线槽位索引
     * @return 自定义波特率值
     */
    public  int UserBaudQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.userBaud[s_num]; // 返回指定槽位的自定义波特率
        }
        return this.userBaud[0]; // 索引不合法时返回默认槽位0的值
    }

}
