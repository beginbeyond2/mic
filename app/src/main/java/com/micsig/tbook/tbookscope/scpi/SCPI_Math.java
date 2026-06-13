package com.micsig.tbook.tbookscope.scpi;

import com.micsig.tbook.tbookscope.middleware.command.Command;

/**
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Math {
//    new SCPICommandStruct(":MATH:DISPlay","SCPI_Math","Display"),//打开或关闭数学运算
//            new SCPICommandStruct(":MATH:DISPlay?","SCPI_Math","DisplayQ"),//查询数学运算打开或关闭
//            new SCPICommandStruct(":MATH:MODE","SCPI_Math","Mode"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:MODE?","SCPI_Math","ModeQ"),//查询数学运算类型
//    new SCPICommandStruct(":MATH:VREF","SCPI_Math","VRef"),//选择数学运算类型
//            new SCPICommandStruct(":MATH:VREF?","SCPI_Math","VRefQ"),//查询数学运算类型

    public static void Display(SCPIParam param) {
        Command.get().getMath().Display(param.iParam1, param.bParam1, true);
    }

    public static String DisplayQ(SCPIParam param) {
        boolean b= Command.get().getMath().DisplayQ();
        return ToolsSCPI.getOpenState(b);
    }

    public static void Mode(SCPIParam param) {
        Command.get().getMath().Mode(param.iParam1, param.iParam2, true);
    }

    public static String ModeQ(SCPIParam param) {
        int i= Command.get().getMath().ModeQ();
        return ToolsSCPI.getMathMode(i);
    }

    public static void VRef(SCPIParam param){
        Command.get().getMath().VRef(param.iParam1, param.iParam2, true);
    }
    public static String VRefQ(SCPIParam param)
    {
        int i=Command.get().getMath().VRefQ();
        return ToolsSCPI.getMathVRef(i);
    }

}
