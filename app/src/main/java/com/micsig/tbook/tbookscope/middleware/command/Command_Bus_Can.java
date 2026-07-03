package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供SERIAL_CNT常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:07
 */
/*
 * +=============================================================================+
 * |                          Command_Bus_Can 类                                  |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   CAN串行总线命令模块，管理CAN/CAN-FD总线的通道、信号类型、波特率、           |
 * |   采样点、ISO标准等参数                                                       |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理每个总线槽位的CAN通道和信号类型                                      |
 * |   2. 管理CAN标准波特率和自定义波特率（含范围校验）                            |
 * |   3. 管理CAN采样点百分比                                                     |
 * |   4. 管理CAN-FD模式的波特率、自定义波特率、采样点                             |
 * |   5. 管理CAN ISO协议标准选择                                                 |
 * |   6. 设置前校验总线类型是否为CAN，参数索引是否合法                            |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含类型校验+参数校验+范围校验+UI同步）  |
 * |   和query方法                                                                 |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Channel/Signal/BaudRate/... → 校验+保存 → RxBus → UI更新         |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取总线类型和消息载体                                          |
 * |   - ChannelFactory: 提供SERIAL_CNT常量                                       |
 * |   - ToolsSCPI: 参数索引合法性校验                                            |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 设置CAN总线通道和信号类型                                               |
 * |   2. 设置CAN/CAN-FD波特率                                                   |
 * |   3. 设置CAN采样点和ISO标准                                                  |
 * +=============================================================================+
 */
public class Command_Bus_Can {
//            new SCPICommandStruct(":BUS#:CAN:CHANnel","SCPI_Bus_Can","Channel"),
//            new SCPICommandStruct(":BUS#:CAN:CHANnel?","SCPI_Bus_Can","ChannelQ"),
//            new SCPICommandStruct(":BUS#:CAN:IDLElvl","SCPI_Bus_Can","IdLevel"),
//            new SCPICommandStruct(":BUS#:CAN:IDLElvl?","SCPI_Bus_Can","IdLevelQ"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate","SCPI_Bus_Can","BaudRate"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate?","SCPI_Bus_Can","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud","SCPI_Bus_Can","UserBaud"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud?","SCPI_Bus_Can","UserBaudQ"),
//
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint","SCPI_Bus_Can","SAMPlepoint"),
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint?","SCPI_Bus_Can","SAMPlepointQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate","SCPI_Bus_Can","FDBAudrate"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate?","SCPI_Bus_Can","FDBAudrateQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud","SCPI_Bus_Can","FDUSerbaud"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud?","SCPI_Bus_Can","FDUSerbaudQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint","SCPI_Bus_Can","FDSAmplepoint"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint?","SCPI_Bus_Can","FDSAmplepointQ"),

    private final int[] ch =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN通道
    private final int[] signal =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN信号类型
    private final int[] baudRate =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN标准波特率索引
    private final int[] userBaud =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN自定义波特率值
    private final double[] samplePoint =new double[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN采样点百分比
    private final int[] FDBaudrate =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN-FD波特率索引
    private final int[] FDUserBaudrate =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN-FD自定义波特率值
    private final double[] FDSamplePoint =new double[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN-FD采样点百分比
    private final int[] iso =new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的CAN ISO标准索引

    private final int minBaud=10000; // CAN自定义波特率最小值
    private final int maxBaud=1000000; // CAN自定义波特率最大值

    private final int FDminBaud=1000000; // CAN-FD自定义波特率最小值
    private final int FDmaxBaud=12000000; // CAN-FD自定义波特率最大值

    /**
     * 设置CAN总线通道
     * @param s_num 总线槽位索引
     * @param source 通道索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Channel(int s_num,int source,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            this.ch[s_num]=source; // 保存CAN通道
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_Channel); // 设置CAN通道标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(source); // 拼接参数：总线号;通道
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询CAN总线通道
     * @param s_num 总线槽位索引
     * @return 通道索引
     */
    public  int ChannelQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.ch[s_num]; // 返回指定槽位的CAN通道
        }
        return this.ch[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置CAN总线信号类型
     * @param s_num 总线槽位索引
     * @param signal 信号类型索引
     * @param isUpdateUI 是否更新UI
     */
    public  void Signal(int s_num,int signal,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        boolean b= ToolsSCPI.isCorrect(s_num,this.ch); // 校验参数索引合法性
        if (b){ // 索引合法
            this.signal[s_num]=signal; // 保存CAN信号类型
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_Signal); // 设置CAN信号标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(signal); // 拼接参数：总线号;信号类型
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询CAN总线信号类型
     * @param s_num 总线槽位索引
     * @return 信号类型索引
     */
    public  int SignalQ(int s_num){
        boolean b= ToolsSCPI.isCorrect(s_num,this.signal); // 校验参数索引合法性
        if (b){ // 索引合法
            return this.signal[s_num]; // 返回指定槽位的信号类型
        }
        return this.signal[0]; // 索引不合法时返回默认槽位0的值
    }

    /**
     * 设置CAN总线标准波特率
     * @param s_num 总线槽位索引
     * @param baudRate 波特率索引
     * @param isUpdateUI 是否更新UI
     */
    public  void BaudRate(int s_num,int baudRate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        boolean b= ToolsSCPI.isCorrect(s_num,this.baudRate); // 校验参数索引合法性
        if (b){ // 索引合法
            this.baudRate[s_num]=baudRate; // 保存CAN标准波特率索引
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_BaudRate); // 设置CAN波特率标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudRate); // 拼接参数：总线号;波特率
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询CAN总线标准波特率
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
     * 设置CAN总线自定义波特率
     * @param s_num 总线槽位索引
     * @param userBaud 自定义波特率值（-1表示不设置）
     * @param isUpdateUI 是否更新UI
     */
    public  void UserBaud(int s_num,int userBaud,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(s_num)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        if (userBaud!=-1){ // 非-1时进行范围校验
            if (userBaud<minBaud) userBaud=minBaud; // 低于最小值则限制为最小值
            if (userBaud>maxBaud) userBaud=maxBaud; // 超过最大值则限制为最大值
        }
        boolean b= ToolsSCPI.isCorrect(s_num,this.userBaud); // 校验参数索引合法性
        if (b){ // 索引合法
            this.userBaud[s_num]=userBaud; // 保存CAN自定义波特率
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
                msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_UserBaud); // 设置CAN自定义波特率标志
                String param = String.valueOf(s_num) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(userBaud); // 拼接参数：总线号;自定义波特率
                msgToUI.setParam(param); // 设置参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
            }
        }
    }

    /**
     * 查询CAN总线自定义波特率
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


    /**
     * 设置CAN总线采样点百分比
     * @param sNum 总线槽位索引
     * @param percent 采样点百分比
     * @param isUpdateUI 是否更新UI
     */
    public  void   SAMPlepoint(int sNum,double percent,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        if (percent<1) percent=1; // 低于1%则限制为1%
        if (percent>=100) percent=99.9; // 超过100%则限制为99.9%
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return; // 索引不合法直接返回
        this.samplePoint[sNum]=percent; // 保存CAN采样点百分比
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_SamplePoint); // 设置CAN采样点标志
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(percent); // 拼接参数：总线号;采样点
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询CAN总线采样点百分比
     * @param sNum 总线槽位索引
     * @return 采样点百分比
     */
    public  double   SAMPlepointQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return this.samplePoint[0]; // 索引不合法时返回默认槽位0的值
        return this.samplePoint[sNum]; // 返回指定槽位的采样点百分比
    }

    /**
     * 设置CAN-FD波特率索引
     * @param sNum 总线槽位索引
     * @param baudrateIndex 波特率索引
     * @param isUpdateUI 是否更新UI
     */
    public  void   FDBAudrate(int sNum,int baudrateIndex,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return; // 索引不合法直接返回
        this.FDBaudrate[sNum]=baudrateIndex; // 保存CAN-FD波特率索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDBaudrate); // 设置CAN-FD波特率标志
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudrateIndex); // 拼接参数：总线号;FD波特率索引
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询CAN-FD波特率索引
     * @param sNum 总线槽位索引
     * @return 波特率索引
     */
    public  int   FDBAudrateQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return this.FDBaudrate[0]; // 索引不合法时返回默认槽位0的值
        return this.FDBaudrate[sNum]; // 返回指定槽位的FD波特率索引
    }

    /**
     * 设置CAN-FD自定义波特率
     * @param sNum 总线槽位索引
     * @param baudrate 自定义波特率值（-1表示不设置）
     * @param isUpdateUI 是否更新UI
     */
    public  void   FDUSerbaud(int sNum,int baudrate,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        if (baudrate!=-1){ // 非-1时进行范围校验
            if (baudrate<FDminBaud) baudrate=FDminBaud; // 低于FD最小值则限制
            if (baudrate>FDmaxBaud) baudrate=FDmaxBaud; // 超过FD最大值则限制
        }

        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return; // 索引不合法直接返回
        this.FDUserBaudrate[sNum]=baudrate; // 保存CAN-FD自定义波特率
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDUserBaud); // 设置CAN-FD自定义波特率标志
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(baudrate); // 拼接参数：总线号;FD自定义波特率
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询CAN-FD自定义波特率
     * @param sNum 总线槽位索引
     * @return 自定义波特率值
     */
    public int FDUSerbaudQ(int sNum) {
        if (!ToolsSCPI.isCorrect(sNum, this.userBaud)) { // 校验参数索引合法性
            return this.FDUserBaudrate[0]; // 索引不合法时返回默认槽位0的值
        } else {
            return this.FDUserBaudrate[sNum]; // 返回指定槽位的FD自定义波特率
        }
    }

    /**
     * 设置CAN-FD采样点百分比
     * @param sNum 总线槽位索引
     * @param percent 采样点百分比
     * @param isUpdateUI 是否更新UI
     */
    public void FDSAmplepoint(int sNum, double percent, boolean isUpdateUI) {
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        if (percent<1) percent=1; // 低于1%则限制为1%
        if (percent>=100) percent=99.9; // 超过100%则限制为99.9%
        if (!ToolsSCPI.isCorrect(sNum,this.userBaud)) return; // 校验参数索引合法性
        if (!ToolsSCPI.isCorrect(sNum,this.FDSamplePoint)) return; // 校验FD采样点数组索引合法性
        this.FDSamplePoint[sNum] = percent; // 保存CAN-FD采样点百分比
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_FDSamplePoint); // 设置CAN-FD采样点标志
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(percent); // 拼接参数：总线号;FD采样点
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询CAN-FD采样点百分比
     * @param sNum 总线槽位索引
     * @return 采样点百分比
     */
    public  double   FDSAmplepointQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.userBaud); // 校验参数索引合法性
        if (b==false) return this.FDSamplePoint[0]; // 索引不合法时返回默认槽位0的值
        return this.FDSamplePoint[sNum]; // 返回指定槽位的FD采样点百分比
    }

    /**
     * 设置CAN总线ISO标准
     * @param sNum 总线槽位索引
     * @param index ISO标准索引
     * @param isUpdateUI 是否更新UI
     */
    public void ISO(int sNum,int index,boolean isUpdateUI){
        if (Command.get().getBus().TypeQ(sNum)!=Command_Bus.Type_CAN)return; // 校验总线类型必须是CAN
        boolean b= ToolsSCPI.isCorrect(sNum,this.iso); // 校验参数索引合法性
        if (b==false) return; // 索引不合法直接返回
        this.iso[sNum]=index; // 保存CAN ISO标准索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Can_ISO); // 设置CAN ISO标志
            String param = String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(index); // 拼接参数：总线号;ISO索引
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询CAN总线ISO标准
     * @param sNum 总线槽位索引
     * @return ISO标准索引
     */
    public int ISOQ(int sNum){
        boolean b= ToolsSCPI.isCorrect(sNum,this.iso); // 校验参数索引合法性
        if (b==false) return this.iso[0]; // 索引不合法时返回默认槽位0的值
        return this.iso[sNum]; // 返回指定槽位的ISO标准索引
    }


}
