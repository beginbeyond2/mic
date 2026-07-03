package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import android.util.Log; // 导入Android日志工具

import com.micsig.tbook.hardware.Hardware; // 导入硬件抽象层，用于读取温度传感器
import com.micsig.tbook.scope.Scope; // 导入示波器核心类，获取FPGA温度
import com.micsig.tbook.scope.ScopeMessage; // 导入示波器消息类，获取FPGA物理状态
import com.micsig.tbook.scope.fpga.FPGA_Status; // 导入FPGA状态类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 生产/系统管理子模块                             |
 * | 核心职责：处理SCPI协议中固件烧写、产品信息查询、系统控制等私有命令            |
 * | 架构设计：静态方法类，委托Command中间件的Production接口和硬件层执行操作        |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getProduction()/Hardware  |
 * | 依赖关系：SCPIParam、Command、Hardware、Scope、ScopeMessage、ToolsSCPI      |
 * | 使用场景：产线生产测试、固件烧写、设备信息读取、系统关机/重启/待机控制        |
 * +---------------------------------------------------------------------------+
 */
public class SCPI_Production {
//            new SCPICommandStruct(":ware","SCPI_Production","Ware"), //固件烧写
//            new SCPICommandStruct(":application","SCPI_Production","Application"), //MCU、APP烧写
//            new SCPICommandStruct(":SYS:WRIT?","SCPI_Production","WriteQ"), //产品信息写入查询
//            new SCPICommandStruct(":SYS:SN?","SCPI_Production","SNQ"), //查询SN
//            new SCPICommandStruct(":PRIVate:UUID?","SCPI_Production","UUIDQ"), //查询唯一识别码
//            new SCPICommandStruct(":PRIVate:HWVersion?","SCPI_Production","HWVersionQ"), //查询硬件版本
//            new SCPICommandStruct(":PRIVate:SERiaino?","SCPI_Production","SeriaiNoQ"), //查询SN
//            new SCPICommandStruct(":PRIVate:STRingcode","SCPI_Production","StringCode"), //串码写入
//            new SCPICommandStruct(":PRIVate:DISPlay:SERiaino","SCPI_Production","DisplaySeriaiNo"), //写入外部SN号
//            new SCPICommandStruct(":PRIVate:MACHinetype","SCPI_Production","MachineType"), //设置设备型号
//            new SCPICommandStruct("PRIVate:STAR","SCPI_Production","Star"),// 开启私有
//            new SCPICommandStruct("PRIVate:STAT?","SCPI_PRoduction","StarQ"),//查询状态
//            new SCPICommandStruct("PRIVate:STOP","SCPI_Production","Stop"), //关闭私有
//            new SCPICommandStruct("PRIVate:SETTing:CLEar","SCPI_Production","SettingClear"),//清楚设置
//
//            new SCPICommandStruct("INTeface:TIME","SCPI_Production","Time"), //设置系统时间
//            new SCPICommandStruct("INTeface:CLEAn","SCPI_Production","Clean"),//恢复系统设置

    /**
     * 启动固件烧写流程
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Ware(SCPIParam param){
        Command.get().getProduction().Ware(true); // 委托Command中间件启动固件烧写，true表示通知UI刷新
    }

    /**
     * 启动MCU/APP烧写流程
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Application(SCPIParam param){
        Command.get().getProduction().Application(true); // 委托Command中间件启动MCU/APP烧写，true表示通知UI刷新
    }

    /**
     * 写入产品信息并查询结果
     * @param param SCPI参数封装，sParam1~sParam5为产品信息字段
     * @return 写入结果字符串，"1"成功/"0"失败
     */
    public static String WriteQ(SCPIParam param){
        boolean b = Command.get().getProduction().WriteQ(param.sParam1, param.sParam2, param.sParam3, param.sParam4, param.sParam5, true); // 委托Command中间件写入产品信息
        return ToolsSCPI.getSuccState(b); // 将布尔结果转换为成功/失败字符串
    }

    /**
     * 查询系统ID
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 系统ID字符串
     */
    public static String SysIdQ(SCPIParam param){
        return Command.get().getProduction().SysId(true); // 委托Command中间件查询系统ID
    }

    /**
     * 查询产品序列号(SN)
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 序列号字符串
     */
    public static String SNQ(SCPIParam param){
        return Command.get().getProduction().SNQ(true); // 委托Command中间件查询SN
    }

    /**
     * 写入生产日期
     * @param param SCPI参数封装，sParam1为日期字符串
     * @return 写入结果字符串
     */
    public static String WDate(SCPIParam param){ return Command.get().getProduction().WDate(param.sParam1,true); } // 委托Command中间件写入生产日期

    /**
     * 查询设备唯一识别码(UUID)
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return UUID字符串
     */
    public static String UUIDQ(SCPIParam param){
        return Command.get().getProduction().UUIDQ(true); // 委托Command中间件查询UUID
    }

    /**
     * 查询硬件版本号
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 硬件版本字符串
     */
    public static String HWVersionQ(SCPIParam param){
        return Command.get().getProduction().HWVersionQ(true); // 委托Command中间件查询硬件版本
    }

    /**
     * 查询外部序列号
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 外部序列号字符串
     */
    public static String SeriaiNoQ(SCPIParam param){
        return Command.get().getProduction().SerialiNoQ(true); // 委托Command中间件查询外部序列号
    }

    /**
     * 写入串码
     * @param param SCPI参数封装，sParam1为串码字符串
     * @return 写入结果字符串，"1"成功/"0"失败
     */
    public static String StringCode(SCPIParam param){
        boolean b= Command.get().getProduction().StringCode(param.sParam1,true); // 委托Command中间件写入串码
        return ToolsSCPI.getSuccState(b); // 将布尔结果转换为成功/失败字符串
    }

    /**
     * 写入外部显示序列号
     * @param param SCPI参数封装，sParam1为序列号字符串
     * @return 写入结果字符串，"1"成功/"0"失败
     */
    public static String DisplaySeriaiNo(SCPIParam param){
         boolean b= Command.get().getProduction().DisplaySeriaiNo(param.sParam1,true); // 委托Command中间件写入显示序列号
         return ToolsSCPI.getSuccState(b); // 将布尔结果转换为成功/失败字符串
    }

    /**
     * 设置设备型号
     * @param param SCPI参数封装，sParam1为型号字符串
     * @return 写入结果字符串，"1"成功/"0"失败
     */
    public static String MachineType(SCPIParam param){
         boolean b= Command.get().getProduction().MachineType(param.sParam1, true); // 委托Command中间件设置设备型号
         return ToolsSCPI.getSuccState(b); // 将布尔结果转换为成功/失败字符串
    }

    /**
     * 开启私有命令模式
     * @param param SCPI参数封装，sParam1为密码/令牌字符串
     */
    public static void Star(SCPIParam param){
        Command.get().getProduction().Star(param.sParam1,true); // 委托Command中间件开启私有模式，true表示通知UI刷新
    }

    /**
     * 查询私有命令模式的开启状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"开启/"0"关闭
     */
    public static String StarQ(SCPIParam param){
        boolean b= Command.get().getProduction().StarQ(true); // 从Command中间件获取私有模式状态
        return ToolsSCPI.getOpenStateToInt(b); // 将布尔值转换为整数状态字符串
    }

    /**
     * 关闭私有命令模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Stop(SCPIParam param){
        Command.get().getProduction().Stop(true); // 委托Command中间件关闭私有模式，true表示通知UI刷新
    }

    /**
     * 设置带宽限制
     * @param param SCPI参数封装，iParam1为带宽索引
     */
    public static void BandWidth(SCPIParam param){
        Command.get().getProduction().BandWidth(param.iParam1,true); // 委托Command中间件设置带宽，true表示通知UI刷新
    }

    /**
     * 清除所有用户设置，恢复默认
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String SettingClear(SCPIParam param){
        Command.get().getProduction().SettingClear(true); // 委托Command中间件清除设置，true表示通知UI刷新
        return ToolsSCPI.getSuccState(true); // 返回成功状态
   }

    /**
     * 设置系统时间
     * @param param SCPI参数封装，sParam1~sParam5为年/月/日/时/分/秒
     */
    public static void Time(SCPIParam param){
        Command.get().getProduction().Time(param.sParam1,param.sParam2,param.sParam3,param.sParam4,param.sParam5,true); // 委托Command中间件设置系统时间
    }

    /**
     * 恢复系统出厂设置
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Clean(SCPIParam param){

        Command.get().getProduction().Clean(true); // 委托Command中间件恢复出厂设置，true表示通知UI刷新
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 系统关机
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Shutdown(SCPIParam param){
        Command.get().getProduction().Shutdown(); // 委托Command中间件执行关机
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 系统重启
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Restart(SCPIParam param){
        Command.get().getProduction().Restart(); // 委托Command中间件执行重启
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 系统待机
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Standby(SCPIParam param){
        Command.get().getProduction().Standby(); // 委托Command中间件执行待机
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 系统唤醒
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Wakeup(SCPIParam param){
        Command.get().getProduction().Wakeup(); // 委托Command中间件执行唤醒
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 锁定系统（禁止操作）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Lock(SCPIParam param){
        Command.get().getProduction().Lock(); // 委托Command中间件执行系统锁定
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 解锁系统
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "1"表示成功
     */
    public static String Unlock(SCPIParam param){
        Command.get().getProduction().Unlock(); // 委托Command中间件执行系统解锁
        return ToolsSCPI.getSuccState(true); // 返回成功状态
    }

    /**
     * 查询系统温度（主板温度和CPU温度）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "主板温度,CPU温度"格式字符串
     */
    public static String SysTemperatureQ(SCPIParam param){
        Hardware hw = Hardware.getInstance(); // 获取硬件单例实例

        return hw.getTemperature() + "," + hw.getCpuTemperature(); // 拼接主板温度和CPU温度返回
    }

    /**
     * 查询FPGA温度
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "FPGA温度1,FPGA温度2"格式字符串
     */
    public static String FpgaTemperatureQ(SCPIParam param){
        return Scope.fpgaTemperature1+","+Scope.fpgaTemperature2; // 拼接两片FPGA的温度返回
    }

    /**
     * 查询FPGA物理层状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return FPGA状态值的字符串表示
     */
    public static String SysFpgaStatusQ(SCPIParam param){
        int v = ScopeMessage.getInstance().getFpgaPhyStatus(); // 从ScopeMessage获取FPGA物理层状态值
        return String.valueOf(v); // 将状态值转换为字符串返回
    }

}
