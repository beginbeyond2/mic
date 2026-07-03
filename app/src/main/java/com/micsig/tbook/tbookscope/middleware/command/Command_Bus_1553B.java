package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:13
 */
/*
 * +=============================================================================+
 * |                         Command_Bus_1553B 类                                 |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   MIL-STD-1553B串行总线命令模块，管理1553B总线的通道选择和显示模式参数         |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的1553B通道选择                                         |
 * |   2. 管理每个总线槽位的1553B显示模式                                         |
 * |   3. 设置前校验总线类型是否为1553B，参数索引是否合法                          |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+UI同步）和query方法|
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Channel/Display → 校验+保存 → CommandMsgToUI → RxBus → UI更新   |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置1553B总线解码通道                                                   |
 * |   2. 设置1553B总线显示模式                                                   |
 * +=============================================================================+
 */
public class Command_Bus_1553B {
//            new SCPICommandStruct(":BUS#:1553B:CHANnel","SCPI_Bus_1553B","Channel"),
//            new SCPICommandStruct(":BUS#:1553B:CHANnel?","SCPI_Bus_1553B","ChannelQ"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay","SCPI_Bus_1553B","Display"),
//            new SCPICommandStruct(":BUS#:1553B:DISPlay?","SCPI_Bus_1553B","DisplayQ"),

    private final int[] ch=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的1553B通道选择
    private final int[] display=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的1553B显示模式

    /**
     * 设置1553B总线通道
     * @param s_num 总线槽位索引
     * @param source 通道索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_1553B)return; // 校验总线类型必须是1553B
        boolean b = ToolsSCPI.isCorrect(s_num, this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            this.ch[s_num]=source; // 保存1553B通道选择
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_1553B_Channel); // 设置1553B通道标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;通道
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询1553B总线通道
     * @param s_num 总线槽位索引
     * @return 通道索引
     */
    public  int ChannelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.ch[s_num]; // 返回指定槽位的通道
        }
        return this.ch[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置1553B总线显示模式
     * @param s_num 总线槽位索引
     * @param display 显示模式索引
     * @param isUpdateUI 是否更新UI
     */
    public void Display(int s_num,int display,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_1553B)return; // 校验总线类型必须是1553B
        boolean b= ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引合法性
        if (b){ // 索引合法
            this.display[s_num]=display; // 保存1553B显示模式
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_1553B_Display); // 设置1553B显示标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display); // 拼接参数：总线号;显示模式
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询1553B总线显示模式
     * @param s_num 总线槽位索引
     * @return 显示模式索引
     */
    public int DisplayQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.display[s_num]; // 返回指定槽位的显示模式
        }
        return this.display[0]; // 索引不合法时返回默认槽位0的值
    }

}
