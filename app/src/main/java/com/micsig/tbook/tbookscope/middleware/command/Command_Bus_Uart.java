package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:40
 */
/*
 * +=============================================================================+
 * |                       Command_Bus_Uart 类                                    |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   UART串口总线命令模块，管理UART总线的RX接收通道、空闲电平、波特率、           |
 * |   校验位、自定义波特率、数据位宽、显示模式等参数                              |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的UART RX接收通道                                        |
 * |   2. 管理每个总线槽位的UART空闲电平                                           |
 * |   3. 管理每个总线槽位的UART标准波特率（含范围校验1200~8000000）              |
 * |   4. 管理每个总线槽位的UART校验位类型                                         |
 * |   5. 管理每个总线槽位的UART自定义波特率（含范围校验1200~8000000）            |
 * |   6. 管理每个总线槽位的UART数据位宽                                           |
 * |   7. 管理每个总线槽位的UART显示模式                                           |
 * |   8. 设置前校验总线类型是否为UART，参数索引是否合法                           |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+UI同步）和query方法|
 * |   波特率相关方法包含范围校验（minBaund~maxBaund）。                           |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Rx/IdLevel/BaudRate/Check/UserBaud/Width/Display → 校验+保存    |
 * |           → RxBus → UI更新                                                   |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置UART总线的RX接收通道                                               |
 * |   2. 设置UART总线的空闲电平                                                 |
 * |   3. 设置UART总线的标准波特率                                               |
 * |   4. 设置UART总线的校验位类型                                               |
 * |   5. 设置UART总线的自定义波特率                                             |
 * |   6. 设置UART总线的数据位宽                                                 |
 * |   7. 设置UART总线的显示模式                                                 |
 * |   8. SCPI远程控制查询UART参数                                               |
 * +=============================================================================+
 */
public class Command_Bus_Uart {
//    new SCPICommandStruct(":BUS#:UART:RX","SCPI_Bus_Uart","Rx"),             // SCPI设置UART接收通道
//            new SCPICommandStruct(":BUS#:UART:RX?","SCPI_Bus_Uart","RxQ"),   // SCPI查询UART接收通道
//            new SCPICommandStruct(":BUS#:UART:IDLElvl","SCPI_Bus_Uart","IdLevel"),       // SCPI设置UART空闲电平
//            new SCPICommandStruct(":BUS#:UART:IDLElvl?","SCPI_Bus_Uart","IdLevelQ"),     // SCPI查询UART空闲电平
//            new SCPICommandStruct(":BUS#:UART:BAUDrate","SCPI_Bus_Uart","BaudRate"),     // SCPI设置UART波特率
//            new SCPICommandStruct(":BUS#:UART:BAUDrate?","SCPI_Bus_Uart","BaudRateQ"),   // SCPI查询UART波特率
//            new SCPICommandStruct(":BUS#:UART:CHECK","SCPI_Bus_Uart","Check"),           // SCPI设置UART校验位
//            new SCPICommandStruct(":BUS#:UART:CHECK?","SCPI_Bus_Uart","CheckQ"),         // SCPI查询UART校验位
//            new SCPICommandStruct(":BUS#:UART:USERbaud","SCPI_Bus_Uart","UserBaud"),     // SCPI设置UART自定义波特率
//            new SCPICommandStruct(":BUS#:UART:USERbaud?","SCPI_Bus_Uart","UserBaudQ"),   // SCPI查询UART自定义波特率
//            new SCPICommandStruct(":BUS#:UART:WIDTh","SCPI_Bus_Uart","Width"),           // SCPI设置UART数据位宽
//            new SCPICommandStruct(":BUS#:UART:WIDTh?","SCPI_Bus_Uart","WidthQ"),         // SCPI查询UART数据位宽

    /** RX接收通道索引数组，每个总线槽位对应一个接收通道 */
    private final int[] rx = new int[ChannelFactory.SERIAL_CNT]; // RX接收通道索引
    /** 空闲电平数组，定义UART空闲时的电平状态 */
    private final int[] idLevel = new int[ChannelFactory.SERIAL_CNT]; // 空闲电平
    /** 标准波特率数组，索引对应预设波特率选项 */
    private final int[] baudRate = new int[ChannelFactory.SERIAL_CNT]; // 标准波特率
    /** 校验位数组，定义UART校验类型（无校验/奇校验/偶校验等） */
    private final int[] check = new int[ChannelFactory.SERIAL_CNT]; // 校验位类型
    /** 自定义波特率值数组，用于非标准波特率设置 */
    private final int[] userBand = new int[ChannelFactory.SERIAL_CNT]; // 自定义波特率值
    /** 数据位宽数组，UART数据帧的位宽（如8位/9位等） */
    private final int[] width = new int[ChannelFactory.SERIAL_CNT]; // 数据位宽
    /** 显示模式数组，定义UART数据的显示格式 */
    private final int[] display = new int[ChannelFactory.SERIAL_CNT]; // 显示模式

    /** 波特率最小值，1200bps */
    private final int minBaund=1200; // 波特率下限
    /** 波特率最大值，8000000bps */
    private final int maxBaund=8000000; // 波特率上限


    /**
     * 设置指定总线槽位的UART RX接收通道
     *
     * @param s_num      总线槽位索引
     * @param rx         RX接收通道索引
     * @param isUpdateUI 是否同步更新UI
     */
    public  void Rx(int s_num,int rx,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART，不是则直接返回
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.rx[s_num]=rx; // 保存RX接收通道索引
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Rx); // 设置消息标志：UART接收通道
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(rx); // 拼接参数：槽位索引+接收通道
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART RX接收通道
     *
     * @param s_num 总线槽位索引
     * @return RX接收通道索引，索引非法时返回第0个槽位的值
     */
    public  int RxQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.rx[s_num]; // 返回指定槽位的RX通道索引
        }
        return this.rx[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART空闲电平
     *
     * @param s_num      总线槽位索引
     * @param idLevel    空闲电平值
     * @param isUpdateUI 是否同步更新UI
     */
    public  void IdLevel(int s_num,int idLevel,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        boolean b= ToolsSCPI.isCorrect(s_num,this.idLevel); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.idLevel[s_num]=idLevel; // 保存空闲电平值
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_IdLevel); // 设置消息标志：UART空闲电平
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(idLevel); // 拼接参数：槽位索引+空闲电平
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART空闲电平
     *
     * @param s_num 总线槽位索引
     * @return 空闲电平值，索引非法时返回第0个槽位的值
     */
    public  int IdLevelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.rx); // 校验参数索引是否合法（注意：此处用rx数组校验，疑似应为idLevel）
        if (b){ // 参数索引合法
            return this.idLevel[s_num]; // 返回指定槽位的空闲电平值
        }
        return this.idLevel[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART标准波特率
     * <p>波特率范围会被钳制在1200~8000000之间</p>
     *
     * @param s_num      总线槽位索引
     * @param baudRate   标准波特率值（bps）
     * @param isUpdateUI 是否同步更新UI
     */
    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        if (baudRate<minBaund) baudRate= minBaund; // 波特率低于下限时钳制到最小值1200
        if (baudRate>maxBaund) baudRate=maxBaund; // 波特率高于上限时钳制到最大值8000000

        boolean b=ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.baudRate[s_num]=baudRate; // 保存标准波特率值
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_BaudRate); // 设置消息标志：UART波特率
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate); // 拼接参数：槽位索引+波特率
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART标准波特率
     *
     * @param s_num 总线槽位索引
     * @return 标准波特率值（bps），索引非法时返回第0个槽位的值
     */
    public  int BaudRateQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.baudRate[s_num]; // 返回指定槽位的标准波特率
        }
        return this.baudRate[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART校验位类型
     *
     * @param s_num      总线槽位索引
     * @param check      校验位类型（如0=无校验，1=奇校验，2=偶校验等）
     * @param isUpdateUI 是否同步更新UI
     */
    public  void Check(int s_num,int check,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        boolean b=ToolsSCPI.isCorrect(s_num,this.check); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.check[s_num]=check; // 保存校验位类型
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Check); // 设置消息标志：UART校验位
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(check); // 拼接参数：槽位索引+校验位类型
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART校验位类型
     *
     * @param s_num 总线槽位索引
     * @return 校验位类型值，索引非法时返回第0个槽位的值
     */
    public int CheckQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.check); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.check[s_num]; // 返回指定槽位的校验位类型
        }
        return this.check[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART自定义波特率
     * <p>自定义波特率范围会被钳制在1200~8000000之间</p>
     *
     * @param s_num      总线槽位索引
     * @param userBaud   自定义波特率值（bps）
     * @param isUpdateUI 是否同步更新UI
     */
    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        if (userBaud<minBaund) userBaud= minBaund; // 自定义波特率低于下限时钳制到最小值1200
        if (userBaud>maxBaund) userBaud=maxBaund; // 自定义波特率高于上限时钳制到最大值8000000

        boolean b=ToolsSCPI.isCorrect(s_num,this.userBand); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.userBand[s_num]=userBaud; // 保存自定义波特率值
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_UserBaud); // 设置消息标志：UART自定义波特率
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(userBaud); // 拼接参数：槽位索引+自定义波特率
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART自定义波特率
     *
     * @param s_num 总线槽位索引
     * @return 自定义波特率值（bps），索引非法时返回第0个槽位的值
     */
    public  int UserBaudQ(int s_num){
        boolean b=ToolsSCPI.isCorrect(s_num,this.userBand); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.userBand[s_num]; // 返回指定槽位的自定义波特率值
        }
        return this.userBand[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART数据位宽
     *
     * @param s_num      总线槽位索引
     * @param width      数据位宽值（如8位/9位等）
     * @param isUpdateUI 是否同步更新UI
     */
    public  void Width(int s_num,int width,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        boolean b=ToolsSCPI.isCorrect(s_num,this.width); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.width[s_num]=width; // 保存数据位宽值
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Width); // 设置消息标志：UART数据位宽
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(width); // 拼接参数：槽位索引+数据位宽
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询指定总线槽位的UART数据位宽
     *
     * @param s_num 总线槽位索引
     * @return 数据位宽值，索引非法时返回第0个槽位的值
     */
    public  int WidthQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.width); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.width[s_num]; // 返回指定槽位的数据位宽
        }
        return this.width[0]; // 索引非法时返回第0个槽位的默认值
    }

    /**
     * 设置指定总线槽位的UART显示模式
     *
     * @param s_num      总线槽位索引
     * @param display    显示模式值
     * @param isUpdateUI 是否同步更新UI
     */
    public void Display(int s_num,int display,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_UART)return; // 校验总线类型是否为UART
        boolean b=ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            this.display[s_num]=display; // 保存显示模式值
            if (isUpdateUI){ // 需要同步更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Uart_Display); // 设置消息标志：UART显示模式
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display); // 拼接参数：槽位索引+显示模式
                msgToUI.setParam(param); // 设置消息参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }

    }

    /**
     * 查询指定总线槽位的UART显示模式
     *
     * @param s_num 总线槽位索引
     * @return 显示模式值，索引非法时返回第0个槽位的值
     */
    public int DisplayQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.display); // 校验参数索引是否合法
        if (b){ // 参数索引合法
            return this.display[s_num]; // 返回指定槽位的显示模式
        }
        return this.display[0]; // 索引非法时返回第0个槽位的默认值
    }

}
