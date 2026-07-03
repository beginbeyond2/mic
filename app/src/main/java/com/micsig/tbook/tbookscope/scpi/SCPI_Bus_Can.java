package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-30 15:07
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                  SCPI_Bus_Can - CAN总线SCPI命令处理                      |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中BUS#:CAN子系统的Java层命令处理类                   |   *
 *   |  核心职责: 处理CAN总线的通道、信号、波特率、采样点、FD及ISO协议等相关SCPI命令    |   *
 *   |  架构设计: 纯静态方法类，每个方法对应一条SCPI命令，通过Command中间件下发到设备层  |   *
 *   |  数据流向: SCPICommandDeal反射调用 → 本类静态方法 → Command中间件 → 设备层    |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、Command(中间件)、ToolsSCPI(结果格式化)        |   *
 *   |  使用场景: 上位机配置CAN/CAN-FD总线解码参数（通道/信号/波特率/采样点/ISO）时调用  |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPI_Bus_Can {
//            new SCPICommandStruct(":BUS#:CAN:CHANnel","SCPI_Bus_Can","Channel"),
//            new SCPICommandStruct(":BUS#:CAN:CHANnel?","SCPI_Bus_Can","ChannelQ"),
//            new SCPICommandStruct(":BUS#:CAN:SIGNal","SCPI_Bus_Can","Signal"),
//            new SCPICommandStruct(":BUS#:CAN:SIGNal?","SCPI_Bus_Can","signalQ"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate","SCPI_Bus_Can","BaudRate"),
//            new SCPICommandStruct(":BUS#:CAN:BAUDrate?","SCPI_Bus_Can","BaudRateQ"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud","SCPI_Bus_Can","UserBaud"),
//            new SCPICommandStruct(":BUS#:CAN:USERbaud?","SCPI_Bus_Can","UserBaudQ"),

//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint","SCPI_Bus_Can","SAMPlepoint"),
//            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint?","SCPI_Bus_Can","SAMPlepointQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate","SCPI_Bus_Can","FDBAudrate"),
//            new SCPICommandStruct(":BUS#:CAN:FDBAudrate?","SCPI_Bus_Can","FDBAudrateQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud","SCPI_Bus_Can","FDUSerbaud"),
//            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud?","SCPI_Bus_Can","FDUSerbaudQ"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint","SCPI_Bus_Can","FDSAmplepoint"),
//            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint?","SCPI_Bus_Can","FDSAmplepointQ"),

    /**
     * 设置CAN总线的通道源。
     * 对应SCPI命令: :BUS#:CAN:CHANnel
     * @param param SCPI参数，iParam1为总线编号，iParam2为通道索引
     */
    public static void Channel(SCPIParam param){
        Command.get().getBus_can().Channel(param.iParam1, param.iParam2,true); // 通过Command中间件设置CAN通道源
    }

    /**
     * 查询CAN总线的通道源。
     * 对应SCPI命令: :BUS#:CAN:CHANnel?
     * @param param SCPI参数，iParam1为总线编号
     * @return 通道名称字符串(如"CH1"、"CH2")
     */
    public static String ChannelQ(SCPIParam param){
        int i=Command.get().getBus_can().ChannelQ(param.iParam1); // 查询CAN通道源索引
        return ToolsSCPI.getCh(i); // 将通道索引转换为通道名称字符串返回
    }

    /**
     * 设置CAN总线的信号类型（CAN_H/CAN_L/H_L/L_H/Rx/Tx）。
     * 对应SCPI命令: :BUS#:CAN:SIGNal
     * @param param SCPI参数，iParam1为总线编号，iParam2为信号类型索引
     */
    public static void Signal(SCPIParam param){
        Command.get().getBus_can().Signal(param.iParam1, param.iParam2,true); // 通过Command中间件设置CAN信号类型
    }

    /**
     * 查询CAN总线的信号类型。
     * 对应SCPI命令: :BUS#:CAN:SIGNal?
     * @param param SCPI参数，iParam1为总线编号
     * @return 信号类型名称字符串(如"CAN_H"、"Rx"等)
     */
    public static String SignalQ(SCPIParam param){
        int i=Command.get().getBus_can().SignalQ(param.iParam1); // 查询CAN信号类型索引
        return ToolsSCPI.getCanSignal(i); // 将信号类型索引转换为名称字符串返回
    }

    /**
     * 设置CAN总线的波特率。
     * 对应SCPI命令: :BUS#:CAN:BAUDrate
     * @param param SCPI参数，iParam1为总线编号，iParam2为波特率索引
     */
    public static void BaudRate(SCPIParam param){
        Command.get().getBus_can().BaudRate(param.iParam1, param.iParam2, true); // 通过Command中间件设置CAN波特率
    }

    /**
     * 查询CAN总线的波特率。如果为自定义波特率(索引-1)，则转查自定义波特率。
     * 对应SCPI命令: :BUS#:CAN:BAUDrate?
     * @param param SCPI参数，iParam1为总线编号
     * @return 波特率名称字符串或自定义波特率数值字符串
     */
    public static String BaudRateQ(SCPIParam param){
        int i=Command.get().getBus_can().BaudRateQ(param.iParam1); // 查询CAN波特率索引
        if (i==-1) return UserBaudQ(param); // 索引为-1表示自定义波特率，转查自定义值
        return ToolsSCPI.getCanBaudRate(i); // 将波特率索引转换为名称字符串返回
    }

    /**
     * 设置CAN总线的自定义波特率。
     * 对应SCPI命令: :BUS#:CAN:USERbaud
     * @param param SCPI参数，iParam1为总线编号，iParam2为自定义波特率值
     */
    public static void UserBaud(SCPIParam param){
        Command.get().getBus_can().UserBaud(param.iParam1, param.iParam2, true); // 通过Command中间件设置CAN自定义波特率
    }

    /**
     * 查询CAN总线的自定义波特率。如果未设置(索引-1)，则转查标准波特率。
     * 对应SCPI命令: :BUS#:CAN:USERbaud?
     * @param param SCPI参数，iParam1为总线编号
     * @return 自定义波特率数值字符串或标准波特率名称字符串
     */
    public static String UserBaudQ(SCPIParam param){
        int i=Command.get().getBus_can().UserBaudQ(param.iParam1); // 查询CAN自定义波特率值
        if (i==-1){
            return BaudRateQ(param); // 索引为-1表示未设置自定义值，转查标准波特率
        }
        return i+""; // 将自定义波特率数值转换为字符串返回
    }

    /**
     * 设置CAN总线的采样点位置。
     * 对应SCPI命令: :BUS#:CAN:SAMPlepoint
     * @param param SCPI参数，iParam1为总线编号，dParam1为采样点百分比值
     */
  public static void   SAMPlepoint(SCPIParam param){
        Command.get().getBus_can().SAMPlepoint(param.iParam1, param.dParam1, true); // 通过Command中间件设置CAN采样点
  }

    /**
     * 查询CAN总线的采样点位置。
     * 对应SCPI命令: :BUS#:CAN:SAMPlepoint?
     * @param param SCPI参数，iParam1为总线编号
     * @return 采样点百分比字符串
     */
  public static String   SAMPlepointQ(SCPIParam param){
        double i= Command.get().getBus_can().SAMPlepointQ(param.iParam1); // 查询CAN采样点值
        return String.valueOf(i); // 将采样点double值转换为字符串返回
  }

    /**
     * 设置CAN-FD的数据相波特率。
     * 对应SCPI命令: :BUS#:CAN:FDBAudrate
     * @param param SCPI参数，iParam1为总线编号，iParam2为FD波特率索引
     */
  public static void   FDBAudrate(SCPIParam param){
        Command.get().getBus_can().FDBAudrate(param.iParam1, param.iParam2, true); // 通过Command中间件设置CAN-FD数据相波特率
  }

    /**
     * 查询CAN-FD的数据相波特率。如果为自定义(索引-1)，则转查自定义FD波特率。
     * 对应SCPI命令: :BUS#:CAN:FDBAudrate?
     * @param param SCPI参数，iParam1为总线编号
     * @return FD波特率名称字符串或自定义FD波特率数值字符串
     */
  public static String   FDBAudrateQ(SCPIParam param){
      int i = Command.get().getBus_can().FDBAudrateQ(param.iParam1); // 查询CAN-FD数据相波特率索引
      if (i==-1){
          return FDUSerbaudQ(param); // 索引为-1表示自定义，转查自定义FD波特率
      }
      return ToolsSCPI.getCanFDBaudrate(i); // 将FD波特率索引转换为名称字符串返回
  }

    /**
     * 设置CAN-FD的自定义数据相波特率。
     * 对应SCPI命令: :BUS#:CAN:FDUSerbaud
     * @param param SCPI参数，iParam1为总线编号，iParam2为自定义FD波特率值
     */
  public static void   FDUSerbaud(SCPIParam param){
        Command.get().getBus_can().FDUSerbaud(param.iParam1, param.iParam2, true); // 通过Command中间件设置CAN-FD自定义数据相波特率
  }

    /**
     * 查询CAN-FD的自定义数据相波特率。如果未设置(索引-1)，则转查标准FD波特率。
     * 对应SCPI命令: :BUS#:CAN:FDUSerbaud?
     * @param param SCPI参数，iParam1为总线编号
     * @return 自定义FD波特率数值字符串或标准FD波特率名称字符串
     */
  public static String   FDUSerbaudQ(SCPIParam param){
        int i=Command.get().getBus_can().FDUSerbaudQ(param.iParam1); // 查询CAN-FD自定义数据相波特率值
        if (i==-1){
            return FDBAudrateQ(param); // 索引为-1表示未设置自定义值，转查标准FD波特率
        }
        return i+""; // 将自定义FD波特率数值转换为字符串返回
  }

    /**
     * 设置CAN-FD的采样点位置。
     * 对应SCPI命令: :BUS#:CAN:FDSAmplepoint
     * @param param SCPI参数，iParam1为总线编号，dParam1为FD采样点百分比值
     */
  public static void   FDSAmplepoint(SCPIParam param){
        Command.get().getBus_can().FDSAmplepoint(param.iParam1, param.dParam1, true); // 通过Command中间件设置CAN-FD采样点
  }

    /**
     * 查询CAN-FD的采样点位置。
     * 对应SCPI命令: :BUS#:CAN:FDSAmplepoint?
     * @param param SCPI参数，iParam1为总线编号
     * @return FD采样点百分比字符串
     */
  public static String   FDSAmplepointQ(SCPIParam param){
      double i = Command.get().getBus_can().FDSAmplepointQ(param.iParam1); // 查询CAN-FD采样点值
      return String.valueOf(i); // 将FD采样点double值转换为字符串返回
  }

    /**
     * 设置CAN总线的ISO协议模式。
     * 对应SCPI命令: :BUS#:CAN:ISO
     * @param param SCPI参数，iParam1为总线编号，iParam2为ISO模式索引
     */
    public static void ISO(SCPIParam param){
        Command.get().getBus_can().ISO(param.iParam1, param.iParam2, true); // 通过Command中间件设置CAN的ISO协议
    }

    /**
     * 查询CAN总线的ISO协议模式。
     * 对应SCPI命令: :BUS#:CAN:ISO?
     * @param param SCPI参数，iParam1为总线编号
     * @return ISO模式名称字符串(如"ISO"、"NON")
     */
    public static String ISOQ(SCPIParam param){
        int i=Command.get().getBus_can().ISOQ(param.iParam1); // 查询CAN的ISO协议模式索引
        return ToolsSCPI.getCanIso(i); // 将ISO模式索引转换为名称字符串返回
    }

}
