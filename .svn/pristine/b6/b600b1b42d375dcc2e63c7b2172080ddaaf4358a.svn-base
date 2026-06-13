package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Trigger_Lin {
//      new SCPICommandStruct(":TRIGger:LIN:SOURce","SCPI_Trigger_Lin","Source"),//设置LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:SOURce?","SCPI_Trigger_Lin","SourceQ"),//查询LIN触发的触发源
//            new SCPICommandStruct(":TRIGger:LIN:TYPE","SCPI_Trigger_Lin","Type"),//设置LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:TYPE?","SCPI_Trigger_Lin","TypeQ"),//查询LIN触发的触发条件
//            new SCPICommandStruct(":TRIGger:LIN:ID","SCPI_Trigger_Lin","Id"),//当LIN总线触发条件为FID或IDATa时，设置LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:ID?","SCPI_Trigger_Lin","IdQ"),//当LIN总线触发条件为FID或IDATa时，查询LIN触发的触发ID值
//            new SCPICommandStruct(":TRIGger:LIN:DATA","SCPI_Trigger_Lin","Data"),//当LIN总线触发条件为IDATa时，设置LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:DATA?","SCPI_Trigger_Lin","DataQ"),//当LIN总线触发条件为IDATa时，查询LIN触发的触发数据
//            new SCPICommandStruct(":TRIGger:LIN:LEVel","SCPI_Trigger_Lin","Level"),//设置LIN触发时的触发电平
//            new SCPICommandStruct(":TRIGger:LIN:LEVel?","SCPI_Trigger_Lin","LevelQ"),//查询LIN触发时的触发电平

    public static void Source(SCPIParam param){}
    public static void SourceQ(SCPIParam param){}

    public static void Type(SCPIParam param){
        int id=Command.get().getTrigger_lin().getId(param.iParam1);
        long data=Command.get().getTrigger_lin().getData(param.iParam1);
        Command.get().getTrigger_lin().setType(param.iParam1, param.iParam2, id,data,true);
    }
    public static String TypeQ(SCPIParam param){
        int i= Command.get().getTrigger_lin().getType(param.iParam1);
        return ToolsSCPI.getLinTriggerType(i);
    }
    public static void Id(SCPIParam param){
//        int type=Command.get().getTrigger_lin().getType(param.iParam1);
        int type=Command.get().getBus_lin().LinTypeQ(param.iParam1);
        long data=Command.get().getTrigger_lin().getData(param.iParam1);
        int id= Tools.HexStringToInt(param.sParam1);
        Command.get().getTrigger_lin().setType(param.iParam1, type, id, data,true);
    }
    public static String IdQ(SCPIParam param){
        int id= Command.get().getTrigger_lin().getId(param.iParam1);
        return Integer.toHexString(id);
    }
    public static void Data(SCPIParam param){
//        int type=Command.get().getTrigger_lin().getType(param.iParam1);
        int type=Command.get().getBus_lin().LinTypeQ(param.iParam1);
        int id=Command.get().getTrigger_lin().getId(param.iParam1);
        int frameLen=2*2;
        if (id>=0 && id<32){
            frameLen=2*2;
        }else if (id>=32 && id<48){
            frameLen=4*2;
        }else if (id>=48 && id<64){
            frameLen=8*2;
        }
        String frame= param.sParam1.trim();
        int start=frame.length()-frameLen<0?0:frame.length()-frameLen;
        frame=frame.substring(start);
        long data= SerialsUtils.toDLong(frame,16);
        Command.get().getTrigger_lin().setType(param.iParam1, type,id, data, true);

    }
    public static String DataQ(SCPIParam param){
        long data=Command.get().getTrigger_lin().getData(param.iParam1);
        return Long.toHexString(data);
    }
    public static void Level(SCPIParam param){
        Command.get().getTrigger_lin().setLevel(param.iParam1, param.dParam1, true);
    }
    public static String LevelQ(SCPIParam param){
        double d=Command.get().getTrigger_lin().getLevel(param.iParam1);
        return String.valueOf(d);
    }

}
