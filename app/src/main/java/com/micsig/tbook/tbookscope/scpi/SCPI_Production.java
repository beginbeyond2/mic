package com.micsig.tbook.tbookscope.scpi;

import android.util.Log;

import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.fpga.FPGA_Status;
import com.micsig.tbook.tbookscope.middleware.command.Command;

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

    public static void Ware(SCPIParam param){
        Command.get().getProduction().Ware(true);
    }
    public static void Application(SCPIParam param){
        Command.get().getProduction().Application(true);
    }
    public static String WriteQ(SCPIParam param){
        boolean b = Command.get().getProduction().WriteQ(param.sParam1, param.sParam2, param.sParam3, param.sParam4, param.sParam5, true);
        return ToolsSCPI.getSuccState(b);
    }
    public static String SysIdQ(SCPIParam param){
        return Command.get().getProduction().SysId(true);
    }
    public static String SNQ(SCPIParam param){
        return Command.get().getProduction().SNQ(true);
    }
    public static String WDate(SCPIParam param){ return Command.get().getProduction().WDate(param.sParam1,true); }
    public static String UUIDQ(SCPIParam param){
        return Command.get().getProduction().UUIDQ(true);
    }
    public static String HWVersionQ(SCPIParam param){
        return Command.get().getProduction().HWVersionQ(true);
    }
    public static String SeriaiNoQ(SCPIParam param){
        return Command.get().getProduction().SerialiNoQ(true);
    }
    public static String StringCode(SCPIParam param){
        boolean b= Command.get().getProduction().StringCode(param.sParam1,true);
        return ToolsSCPI.getSuccState(b);
    }
    public static String DisplaySeriaiNo(SCPIParam param){
         boolean b= Command.get().getProduction().DisplaySeriaiNo(param.sParam1,true);
         return ToolsSCPI.getSuccState(b);
    }
    public static String MachineType(SCPIParam param){
         boolean b= Command.get().getProduction().MachineType(param.sParam1, true);
         return ToolsSCPI.getSuccState(b);
    }

    public static void Star(SCPIParam param){
        Command.get().getProduction().Star(param.sParam1,true);
    }
    public static String StarQ(SCPIParam param){
        boolean b= Command.get().getProduction().StarQ(true);
        return ToolsSCPI.getOpenStateToInt(b);
    }
    public static void Stop(SCPIParam param){
        Command.get().getProduction().Stop(true);
    }

    public static void BandWidth(SCPIParam param){
        Command.get().getProduction().BandWidth(param.iParam1,true);
    }
   public static String SettingClear(SCPIParam param){
        Command.get().getProduction().SettingClear(true);
        return ToolsSCPI.getSuccState(true);
   }
    public static void Time(SCPIParam param){
        Command.get().getProduction().Time(param.sParam1,param.sParam2,param.sParam3,param.sParam4,param.sParam5,true);
    }

    public static String Clean(SCPIParam param){

        Command.get().getProduction().Clean(true);
        return ToolsSCPI.getSuccState(true);
    }
    public static String Shutdown(SCPIParam param){
        Command.get().getProduction().Shutdown();
        return ToolsSCPI.getSuccState(true);
    }

    public static String Restart(SCPIParam param){
        Command.get().getProduction().Restart();
        return ToolsSCPI.getSuccState(true);
    }

    public static String Standby(SCPIParam param){
        Command.get().getProduction().Standby();
        return ToolsSCPI.getSuccState(true);
    }

    public static String Wakeup(SCPIParam param){
        Command.get().getProduction().Wakeup();
        return ToolsSCPI.getSuccState(true);
    }

    public static String Lock(SCPIParam param){
        Command.get().getProduction().Lock();
        return ToolsSCPI.getSuccState(true);
    }

    public static String Unlock(SCPIParam param){
        Command.get().getProduction().Unlock();
        return ToolsSCPI.getSuccState(true);
    }
    public static String SysTemperatureQ(SCPIParam param){
        Hardware hw = Hardware.getInstance();

        return hw.getTemperature() + "," + hw.getCpuTemperature();
    }
    public static String FpgaTemperatureQ(SCPIParam param){
        return Scope.fpgaTemperature1+","+Scope.fpgaTemperature2;
    }
    public static String SysFpgaStatusQ(SCPIParam param){
        int v = ScopeMessage.getInstance().getFpgaPhyStatus();
        return String.valueOf(v);
    }

}
