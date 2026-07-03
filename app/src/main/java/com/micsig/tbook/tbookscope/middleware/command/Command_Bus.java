package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import android.util.Log; // Android日志工具

import com.micsig.base.Logger; // 自定义日志工具
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，提供通道数量常量
import com.micsig.tbook.scope.channel.SerialChannel; // 串行总线通道类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBus; // 串行总线波形类
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage; // 串行总线管理器
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStruct; // 串行总线数据结构
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct; // 串行总线文本数据结构
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道编号工具类

import java.util.Iterator; // 迭代器
import java.util.List; // 列表
import java.util.concurrent.LinkedBlockingQueue; // 链表阻塞队列
/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 14:39
 */
/*
 * +=============================================================================+
 * |                            Command_Bus 类                                    |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   串行总线通用命令模块，管理总线的显示、类型、模式、触发电平等通用属性，       |
 * |   并根据总线类型分发到具体总线子模块（UART/LIN/CAN/SPI/IIC/429/1553B）        |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理总线类型（UART/LIN/CAN/SPI/IIC/429/1553B）的设置与查询              |
 * |   2. 管理总线显示开关（委托给Command_Channel）                                |
 * |   3. 管理总线模式（委托给Command_Trigger的串行总线类型）                      |
 * |   4. 管理总线触发电平（根据总线类型分发到对应触发模块）                       |
 * |   5. 提供总线数据查询（文本/图像两种格式）                                    |
 * |   6. 判断通道是否被其他总线占用                                               |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   总线命令的门面类，内部根据总线类型做分发，不同总线的触发电平设置和查询       |
 * |   分别委托给对应的触发命令模块                                                |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Command_Bus方法 → 根据type分发到子模块 → RxBus → UI更新          |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取各子命令模块和消息载体                                       |
 * |   - SerialBusManage: 获取总线实例和类型                                       |
 * |   - ChannelFactory: 提供SERIAL_CNT/CH_CNT常量                                |
 * |   - Command_Bus_Uart/Lin/Can/Spi/Iic/429/1553B: 具体总线子模块               |
 * |   - Command_Trigger_*: 各触发子模块                                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 切换总线类型（如从UART切换到CAN）                                       |
 * |   2. 调整总线触发电平                                                        |
 * |   3. 查询总线解码数据（SCPI远程查询）                                        |
 * +=============================================================================+
 */
public class Command_Bus {
//            new SCPICommandStruct(":BUS#:DISPlay","SCPI_Bus","Display"),
//            new SCPICommandStruct(":BUS#:DISPlay?","SCPI_Bus","DisplayQ"), //
//            new SCPICommandStruct(":BUS#:TYPE","SCPI_Bus","Type"),
//            new SCPICommandStruct(":BUS#:TYPE?","SCPI_Bus","TypeQ"),
//            new SCPICommandStruct(":BUS#:MODE","SCPI_Bus","Mode"),
//            new SCPICommandStruct(":BUS#:MODE?","SCPI_Bus","ModeQ"),
//            new SCPICommandStruct(":BUS#:LEVel","SCPI_Bus","Level"),
//            new SCPICommandStruct(":BUS#:LEVel?","SCPI_Bus","LevelQ"),
//            new SCPICommandStruct(":BUS#:HLEVel","SCPI_Bus","HLevel"),
//            new SCPICommandStruct(":BUS#:HLEVel?","SCPI_Bus","HLevelQ"),
//            new SCPICommandStruct(":BUS#:LLEVel","SCPI_Bus","LLevel"),
//            new SCPICommandStruct(":BUS#:LLEVel?","SCPI_Bus","LLevelQ"),

    public static final int  Type_UART=0; // 总线类型：UART
    public static final int  Type_LIN=1; // 总线类型：LIN
    public static final int  Type_CAN=2; // 总线类型：CAN
    public static final int  Type_SPI=3; // 总线类型：SPI
    public static final int  Type_IIC=4; // 总线类型：IIC/I2C
    public static final int  Type_429=5; // 总线类型：ARINC429
    public static final int  Type_1553B=6; // 总线类型：MIL-STD-1553B

    private int[] type=new int[ChannelFactory.SERIAL_CNT]; // 每个总线槽位的类型索引
    private double[][] level=new double[ChannelFactory.SERIAL_CNT][ChannelFactory.CH_CNT]; // 每个总线槽位每通道的触发电平值


    /**
     * 设置总线显示开关，委托给通道命令模块
     * @param ch 通道索引
     * @param isOpen true=显示，false=隐藏
     * @param isUpdateUI 是否更新UI
     */
    public void Display(int ch,boolean isOpen,boolean isUpdateUI){
        Command.get().getChannel().Display(ch,isOpen,isUpdateUI); // 委托给通道模块控制显示
    }

    /**
     * 查询总线显示状态
     * @param ch 通道索引
     * @return 显示状态字符串（ON/OFF）
     */
    public String DisplayQ(int ch){
      boolean  b= Command.get().getChannel().DisplayQ(ch); // 查询通道显示状态
      return ToolsSCPI.getOpenState(b); // 将布尔值转为SCPI标准ON/OFF字符串
    }

    /**
     * 设置总线类型
     * @param ch 总线槽位索引
     * @param type 总线类型（Type_UART等）
     * @param isUpdateUI 是否更新UI
     */
    public  void Type(int ch,int type,boolean isUpdateUI){
        this.type[ch]=type; // 保存总线类型
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Type); // 设置总线类型标志
            String param = String.valueOf(ch) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(type); // 拼接参数：总线号;类型
            msgToUI.setParam(param); // 设置参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询总线类型
     * @param ch 总线槽位索引
     * @return 总线类型索引
     */
    public  int TypeQ(int ch){
        return type[ch]; // 返回指定总线槽位的类型
    }

    /**
     * 设置总线模式（委托给触发模块的串行总线类型设置）
     * @param sNum 总线槽位索引
     * @param mode 模式值
     * @param isUpdateUI 是否更新UI
     */
    public  void Mode(int sNum,int mode,boolean isUpdateUI){
//        this.mode[sNum]=mode;
//        if (isUpdateUI){
//            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
//            msgToUI.setFlag(CommandMsgToUI.FLAG_Bus_Mode);
//            String param = String.valueOf(mode);
//            msgToUI.setParam(param);
//            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
//        }
        Command.get().getTrigger().SerialBus_Type(mode,isUpdateUI); // 委托给触发模块设置串行总线类型
    }

    /**
     * 查询总线模式
     * @param sNum 总线槽位索引
     * @return 模式值
     */
    public int ModeQ(int sNum){
//        return mode[sNum];
        return Command.get().getTrigger().SerialBus_TypeQ(); // 委托给触发模块查询串行总线类型
    }

    /**
     * 设置总线触发电平
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @param level 电平值
     * @param isUpdateUI 是否更新UI
     */
    public  void Level(int sNum,int ch,double level,boolean isUpdateUI){
        if (sNum != 0 && sNum != 1) return; // 总线槽位只能是0或1，否则直接返回
        if (!isCorrect(sNum, ch)) return; // 校验通道与总线类型是否匹配

        this.level[sNum][ch]=level; // 保存触发电平值
        if (isUpdateUI) { // 判断是否需要更新UI
            post(sNum,ch,level); // 发送UI更新消息
        }
    }

    /**
     * 查询总线触发电平
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @return 触发电平值
     */
    public  double LevelQ(int sNum,int ch){
        return getLevel(sNum,ch); // 委托给getLevel获取实际电平值
    }

    /**
     * 设置总线高电平（仅对ARINC429总线有效）
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @param level 高电平值
     * @param isUpdateUI 是否更新UI
     */
    public void HLevel(int sNum,int ch,double level,boolean isUpdateUI){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1); // 获取总线实例
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType(); // 获取总线类型
        if (type!=SerialBusStruct.SerialBusType_429) return; // 仅429总线支持高电平设置
        Command.get().getTrigger_m429().setLevelHigh(sNum,ch,level,isUpdateUI); // 委托给429触发模块设置高电平
    }

    /**
     * 查询总线高电平
     * @param sNum 总线槽位索引
     * @param ch 通道索引（未使用）
     * @return 高电平值
     */
    public double HLevelQ(int sNum,int ch){
        return Command.get().getTrigger_m429().getLevelHigh(sNum); // 委托给429触发模块查询高电平
    }

    /**
     * 设置总线低电平（仅对ARINC429总线有效）
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @param level 低电平值
     * @param isUpdateUI 是否更新UI
     */
    public void LLevel(int sNum,int ch,double level,boolean isUpdateUI){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1); // 获取总线实例
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType(); // 获取总线类型
        if (type!=SerialBusStruct.SerialBusType_429) return; // 仅429总线支持低电平设置
        Command.get().getTrigger_m429().setLevelLow(sNum,ch,level,isUpdateUI); // 委托给429触发模块设置低电平
    }

    /**
     * 查询总线低电平
     * @param sNum 总线槽位索引
     * @param ch 通道索引（未使用）
     * @return 低电平值
     */
    public double LLevelQ(int sNum,int ch){
        return Command.get().getTrigger_m429().getLevelLow(sNum); // 委托给429触发模块查询低电平
    }

    /**
     * 校验通道与总线类型是否匹配
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @return true=通道属于该总线，false=不属于
     */
    private boolean isCorrect(int sNum,int ch){
        boolean result=false; // 默认不匹配
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1); // 获取总线实例
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType(); // 获取总线类型

        switch (type){ // 根据总线类型判断通道是否属于该总线
            case SerialBusStruct.SerialBusType_UART:{
                int channel= Command.get().getBus_uart().RxQ(sNum); // 查询UART的接收通道
                if (ch==channel){ // 判断传入通道是否与UART接收通道一致
                    result= true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                int channel= Command.get().getBus_lin().ChannelQ(sNum); // 查询LIN的通道
                if (ch==channel){ // 判断传入通道是否与LIN通道一致
                    result= true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                int channel=Command.get().getBus_can().ChannelQ(sNum); // 查询CAN的通道
                if (ch==channel){ // 判断传入通道是否与CAN通道一致
                    result= true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum); // 查询SPI的时钟通道
                int ch_data=Command.get().getBus_spi().getData(sNum); // 查询SPI的数据通道
                int ch_cs= Command.get().getBus_spi().getCs(sNum); // 查询SPI的CS片选通道
                if (ch==ch_clk || ch==ch_data || ch==ch_cs){ // 判断传入通道是否为SPI三个通道之一
                    result= true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum); // 查询IIC的SCL时钟通道
                int cl_data=Command.get().getBus_iic().SDAQ(sNum); // 查询IIC的SDA数据通道
                if (ch==cl_clk || ch==cl_data){ // 判断传入通道是否为IIC两个通道之一
                    result =true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                //int ch_h= Command.get().getBus_429().SourceQ(sNum);
                if (ch==0 || ch==1){ // 429总线使用通道0和1
                    result =true; // 匹配
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                int channel=Command.get().getBus_1553B().ChannelQ(sNum); // 查询1553B的通道
                if (channel==ch){ // 判断传入通道是否与1553B通道一致
                    result=true; // 匹配
                }
            }break;
        }

        return result; // 返回匹配结果
    }

    /**
     * 根据总线类型发送触发电平更新消息到UI
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @param level 电平值
     */
    private void post(int sNum,int ch,double level){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1); // 获取总线实例
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType(); // 获取总线类型
        CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
        msgToUI.setParam(String.valueOf(sNum) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(level)+CommandMsgToUI.PARAM_SPLIT+String.valueOf(ch)); // 设置参数：总线号;电平值;通道号
        switch (type){ // 根据总线类型设置不同的FLAG标志
            case SerialBusStruct.SerialBusType_UART:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERUART_LEVEL); // UART触发电平标志
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERLIN_LEVEL); // LIN触发电平标志
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERCAN_LEVEL); // CAN触发电平标志
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum); // 获取SPI时钟通道
                int ch_data=Command.get().getBus_spi().getData(sNum); // 获取SPI数据通道
                int ch_cs= Command.get().getBus_spi().getCs(sNum); // 获取SPI CS片选通道
                if (ch==ch_clk) { // 传入通道是时钟通道
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK); // SPI时钟电平标志
                }else if (ch==ch_data) { // 传入通道是数据通道
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA); // SPI数据电平标志
                }else if (ch==ch_cs) { // 传入通道是CS片选通道
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS); // SPI CS电平标志
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum); // 获取IIC SCL时钟通道
                int cl_data=Command.get().getBus_iic().SDAQ(sNum); // 获取IIC SDA数据通道
                if (ch==cl_clk) { // 传入通道是SCL时钟通道
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELCLOCK); // IIC时钟电平标志
                }else if (ch==cl_data) { // 传入通道是SDA数据通道
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERIIC_LEVELDATA); // IIC数据电平标志
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                if (ch==0) { // 通道0对应高电平
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELHIGH); // 429高电平标志
                }else if (ch==1) { // 通道1对应低电平
                    msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM429_LEVELLOW); // 429低电平标志
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                msgToUI.setFlag(CommandMsgToUI.FLAG_TRIGGERM1553B_LEVEL); // 1553B触发电平标志
            }break;
        }
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息

    }

    /**
     * 判断指定通道是否被另一个总线槽位占用
     * @param curSNum 当前总线槽位索引
     * @param ch 要检查的通道索引
     * @return true=被另一个总线占用，false=未被占用
     */
    public static boolean isChannelExistOtherSerialNum(int curSNum,int ch){
        int otherSNum=curSNum==0?1:0; // 获取另一个总线槽位索引
        int otherType=Command.get().getBus().TypeQ(otherSNum); // 查询另一个总线的类型
        boolean result=false; // 默认未被占用
        Logger.i(Command.TAG,"otherNum:"+otherSNum+",otherType:"+otherType); // 打印日志

        int otherCh= otherSNum+ TChan.R8; // 计算另一个总线对应的通道编号
        boolean  b= Command.get().getChannel().DisplayQ(otherCh); // 查询另一个总线通道是否显示
        if (!b){ // 如果另一个总线通道未显示
            return b; // 未被占用
        }

        switch (otherType){ // 根据另一个总线的类型检查通道占用情况
            case SerialBusStruct.SerialBusType_UART:{
                 int source =Command.get().getTrigger_uart().getSource(otherSNum); // 查询UART触发源
                 result=ch==source; // 判断通道是否与UART源一致
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                int source=Command.get().getTrigger_lin().getSource(otherSNum); // 查询LIN触发源
                result=ch==source; // 判断通道是否与LIN源一致
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                int source=Command.get().getTrigger_can().getSource(otherSNum); // 查询CAN触发源
                result=ch==source; // 判断通道是否与CAN源一致
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(otherSNum); // 获取SPI时钟通道
                int ch_data=Command.get().getBus_spi().getData(otherSNum); // 获取SPI数据通道
                int ch_cs= Command.get().getBus_spi().getCs(otherSNum); // 获取SPI CS片选通道
                result=(ch==ch_clk || ch==ch_data || ch==ch_cs) ; // 判断通道是否为SPI三通道之一
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(otherSNum); // 获取IIC SCL时钟通道
                int cl_data=Command.get().getBus_iic().SDAQ(otherSNum); // 获取IIC SDA数据通道
                result = (ch== cl_clk || ch==cl_data); // 判断通道是否为IIC两通道之一
            }break;
            case SerialBusStruct.SerialBusType_429:{
                int source= Command.get().getTrigger_m429().getSource(otherSNum); // 查询429触发源
                result=ch==source; // 判断通道是否与429源一致
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                int source =Command.get().getTrigger_m1553B().getSource(otherSNum); // 查询1553B触发源
                result=ch==source; // 判断通道是否与1553B源一致
            }break;
        }
        Logger.i(Command.TAG,"result:"+result); // 打印结果日志
        return result; // 返回占用检查结果
    }

    /**
     * 根据总线类型获取对应触发电平值
     * @param sNum 总线槽位索引
     * @param ch 通道索引
     * @return 触发电平值
     */
    private double getLevel(int sNum,int ch){
        SerialBus serialBus = SerialBusManage.getInstance().getSerialBus(sNum + TChan.S1); // 获取总线实例
        @SerialBusStruct.SerialBusType int type= serialBus.getSerialBusType(); // 获取总线类型
        double level=0; // 默认电平值

        switch (type){ // 根据总线类型从对应触发模块获取电平值
            case SerialBusStruct.SerialBusType_UART:{
               level=Command.get().getTrigger_uart().getLevel(sNum); // 从UART触发模块获取电平
            }break;
            case SerialBusStruct.SerialBusType_LIN:{
                level=Command.get().getTrigger_lin().getLevel(sNum); // 从LIN触发模块获取电平
            }break;
            case SerialBusStruct.SerialBusType_CAN:{
                level=Command.get().getTrigger_can().getLevel(sNum); // 从CAN触发模块获取电平
            }break;
            case SerialBusStruct.SerialBusType_SPI:{
                int ch_clk=Command.get().getBus_spi().getClock(sNum); // 获取SPI时钟通道
                int ch_data=Command.get().getBus_spi().getData(sNum); // 获取SPI数据通道
                int ch_cs= Command.get().getBus_spi().getCs(sNum); // 获取SPI CS片选通道
                if (ch==ch_clk) { // 时钟通道
                    level= Command.get().getTrigger_spi().LevelCLKQ(sNum); // 从SPI触发模块获取时钟电平
                }else if (ch==ch_data) { // 数据通道
                    level=Command.get().getTrigger_spi().LevelDataQ(sNum); // 从SPI触发模块获取数据电平
                }else if (ch==ch_cs) { // CS片选通道
                    level=Command.get().getTrigger_spi().LevelCSQ(sNum); // 从SPI触发模块获取CS电平
                }
            }break;
            case SerialBusStruct.SerialBusType_I2C:{
                int cl_clk=Command.get().getBus_iic().SCLQ(sNum); // 获取IIC SCL时钟通道
                int cl_data=Command.get().getBus_iic().SDAQ(sNum); // 获取IIC SDA数据通道
                if (ch==cl_clk) { // SCL时钟通道
                    Command.get().getTrigger_iic().getLevelClock(sNum); // 从IIC触发模块获取时钟电平
                }else if (ch==cl_data) { // SDA数据通道
                    Command.get().getTrigger_iic().getLevelData(sNum); // 从IIC触发模块获取数据电平
                }
            }break;
            case SerialBusStruct.SerialBusType_429:{
                if (ch==0) { // 通道0对应高电平
                    level= Command.get().getTrigger_m429().getLevelHigh(sNum); // 从429触发模块获取高电平
                }else if (ch==1) { // 通道1对应低电平
                    level= Command.get().getTrigger_m429().getLevelLow(sNum); // 从429触发模块获取低电平
                }
            }break;
            case SerialBusStruct.SerialBusType_1553B:{
                level=Command.get().getTrigger_m1553B().getLevel(sNum); // 从1553B触发模块获取电平
            }break;
        }
        return level; // 返回触发电平值
    }

    /**
     * 查询总线解码数据（SCPI远程查询用）
     * @param sumNo 总线槽位索引
     * @return 总线数据CSV字符串
     */
    public String DataQ(int sumNo){
        return getSerialBusData(sumNo); // 委托获取总线解码数据
    }
    //region  总线数据
    /**
     * 返回总线数据
     * @param chIdx 总线槽位索引
     * @return 总线数据CSV字符串
     */
    private static String getSerialBusData(int chIdx){
        int type= Command.get().getTrigger().SerialBus_TypeQ(); // 查询串行总线触发类型
        if (type== Command_Trigger.SerialBusType_TXT){ // 如果是文本模式
            return getSerialTxtBuffer(chIdx); // 获取文本格式总线数据
        }else { // 如果是图像模式
            return getSerialImgBuffer(TChan.toUiChNo(chIdx)); // 获取图像格式总线数据
        }
    }

    /**
     * 获取文本格式的总线解码数据
     * @param chIdx 总线槽位索引
     * @return CSV格式文本数据字符串
     */
    private static String getSerialTxtBuffer(int chIdx){
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(chIdx); // 获取串行通道实例
        if (serialChannel == null) return "Error"; // 通道为空返回错误
        int type = serialChannel.getBusType(); // 获取总线类型

        LinkedBlockingQueue<SerialBusTxtStruct.ISerialBusTxtCSV> list= SerialBusManage.getInstance().getSerialTxtBufferQueue(TChan.toUiChNo(chIdx),type,true); // 获取文本缓冲队列
        if (list==null) return "Error"; // 队列为空返回错误
        Log.d(Command.TAG, "getSerialTxtBuffer list size: "+list.size()); // 打印队列大小日志
        //返回记录条数
        int resultRecordCount=1000; // 最大返回记录条数

        int i=0; // 遍历计数器
        StringBuilder sb=new StringBuilder(); // 构建CSV字符串
        for (Iterator iter = list.iterator(); iter.hasNext(); ) { // 遍历队列中的每条记录
            SerialBusTxtStruct.ISerialBusTxtCSV uart = (SerialBusTxtStruct.ISerialBusTxtCSV) iter.next(); // 获取当前记录
            i++; // 递增计数器
            if ((list.size()-i)<resultRecordCount) { // 只取最后N条记录
                if (sb.length() == 0) { // 第一条记录时添加CSV头
                    sb.append(uart.toCSVHead() + ";"); // 添加CSV表头
                }
                sb.append(uart.toCSV() + ";"); // 添加CSV数据行
            }
        }
        Log.d(Command.TAG, "getSerialTxtBuffer list size: "+list.size()); // 打印队列大小日志
        Log.d(Command.TAG, "getSerialTxtBuffer length: "+sb.toString().length()); // 打印结果长度日志
        return sb.toString(); // 返回CSV字符串
    }

    /**
     * 图像数据
     * @param IWave_serialNum 波形界面的串行总线编号
     * @return CSV格式图像数据字符串
     */
    private static String getSerialImgBuffer(int IWave_serialNum){
        Class c= SerialBusManage.getInstance().getSerialBusType(IWave_serialNum); // 获取总线类型Class
        Log.d(Command.TAG, "getSerialImgBuffer class name: "+c.getSimpleName()); // 打印类名日志
        List<SerialBusStruct.ISerialBusCSV> l= SerialBusManage.getInstance().getSerialImgBufferQueue(IWave_serialNum, c); // 获取图像缓冲队列
        if (l==null) return "Error"; // 队列为空返回错误
        return toCsv(l); // 转换为CSV字符串
    }

    /**
     * 将总线数据列表转换为CSV字符串
     * @param list 总线数据列表
     * @return CSV格式字符串
     */
    private static <T extends SerialBusStruct.ISerialBusCSV> String toCsv(List<T> list){
        StringBuilder sb=new StringBuilder(); // 构建CSV字符串
        if (list.size()>0){ // 列表不为空时
            sb.append(list.get(0).toCsvHead()+";"); // 添加CSV表头
        }
        for(int i=0;i<list.size();i++){ // 遍历每条记录
            sb.append(list.get(i).toCSV()+";"); // 添加CSV数据行
        }
        return sb.toString(); // 返回CSV字符串
    }
    //endregion

}
