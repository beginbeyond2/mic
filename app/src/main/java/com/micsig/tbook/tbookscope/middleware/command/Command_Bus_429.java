package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:15
 */
/*
 * +=============================================================================+
 * |                          Command_Bus_429 类                                  |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   ARINC429串行总线命令模块，管理429总线的通道源、数据格式、显示模式和波特率     |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的429通道源                                             |
 * |   2. 管理每个总线槽位的429数据格式                                           |
 * |   3. 管理每个总线槽位的429显示模式                                           |
 * |   4. 管理每个总线槽位的429波特率                                             |
 * |   5. 设置前校验总线类型是否为429，参数索引是否合法                            |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+UI同步）和query方法|
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Source/Format/Display/BaudRate → 校验+保存 → RxBus → UI更新     |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置429总线解码通道源                                                   |
 * |   2. 设置429总线数据格式（标签/扩展等）                                      |
 * |   3. 设置429总线波特率                                                       |
 * +=============================================================================+
 */
public class Command_Bus_429 {
//    new SCPICommandStruct(":BUS#:429:SOURce","SCPI_Bus_429","Source"),
//            new SCPICommandStruct(":BUS#:429:SOURce?","SCPI_Bus_429","SourceQ"),
//            new SCPICommandStruct(":BUS#:429:FORMat","SCPI_Bus_429","Format"),
//            new SCPICommandStruct(":BUS#:429:FORMat?","SCPI_Bus_429","FormatQ"),
//            new SCPICommandStruct(":BUS#:429:DISPlay","SCPI_Bus_429","Display"),
//            new SCPICommandStruct(":BUS#:429:DISPlay?","SCPI_Bus_429","DisplayQ"),
//            new SCPICommandStruct(":BUS#:429:BANDrate","SCPI_Bus_429","BandRate"),
//            new SCPICommandStruct(":BUS#:429:BANDrate?","SCPI_Bus_429","BandRateQ"),


    private final int[] ch = new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的429通道源
    private final int[] format = new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的429数据格式
    private final int[] display = new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的429显示模式
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的429波特率

    /**
     * 设置429总线通道源
     * @param s_num 总线槽位索引
     * @param source 通道源索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Source(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return; // 校验总线类型必须是429
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            this.ch[s_num]=source; // 保存429通道源
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Channel); // 设置429通道标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;通道源
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询429总线通道源
     * @param s_num 总线槽位索引
     * @return 通道源索引
     */
    public  int SourceQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.ch[s_num]; // 返回指定槽位的通道源
        }
        return this.ch[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置429总线数据格式
     * @param s_num 总线槽位索引
     * @param format 数据格式索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Format(int s_num,int format,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return; // 校验总线类型必须是429
        boolean b= ToolsSCPI.isCorrect(s_num,this.format); // 校验参数索引合法性
        if (b){ // 索引合法
            this.format[s_num]=format; // 保存429数据格式
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Format); // 设置429格式标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(format); // 拼接参数：总线号;格式
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询429总线数据格式
     * @param s_num 总线槽位索引
     * @return 数据格式索引
     */
    public  int FormatQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.format); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.format[s_num]; // 返回指定槽位的数据格式
        }
        return this.format[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置429总线显示模式
     * @param s_num 总线槽位索引
     * @param display 显示模式索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Display(int s_num,int display ,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return; // 校验总线类型必须是429
        boolean b= ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引合法性
        if (b){ // 索引合法
            this.display[s_num]=display; // 保存429显示模式
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_display); // 设置429显示标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display); // 拼接参数：总线号;显示模式
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询429总线显示模式
     * @param s_num 总线槽位索引
     * @return 显示模式索引
     */
    public  int DisplayQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.display[s_num]; // 返回指定槽位的显示模式
        }
        return this.display[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置429总线波特率
     * @param s_num 总线槽位索引
     * @param baudRate 波特率索引
     * @param isUpdateUI 是否更新UI
     */
    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_429)return; // 校验总线类型必须是429
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引合法性
        if (b){ // 索引合法
            this.baudRate[s_num]=baudRate; // 保存429波特率
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_429_Baudrate); // 设置429波特率标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate); // 拼接参数：总线号;波特率
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询429总线波特率
     * @param s_num 总线槽位索引
     * @return 波特率索引
     */
    public  int BaudRateQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.baudRate[s_num]; // 返回指定槽位的波特率
        }
        return this.baudRate[0]; // 索引不合法时返回默认槽位0的值
    }

}
