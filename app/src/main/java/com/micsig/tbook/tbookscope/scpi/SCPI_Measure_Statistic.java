package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 测量统计子模块                                 |
 * | 核心职责：处理SCPI协议中:MEASure:STATistic和:MEASure:SETTing相关命令          |
 * | 架构设计：静态方法类，委托Command中间件的Measure_statistic和Measure_setting   |
 * | 数据流向：SCPIParam → 本类静态方法 → Command → Measure_statistic/setting    |
 * | 依赖关系：SCPIParam、Command、ToolsSCPI                                    |
 * | 使用场景：远程查询/设置测量统计功能（均值/最大/最小/标准差）及测量阈值设置    |
 * +---------------------------------------------------------------------------+
 *
 * @auother Liwb
 * @description: 测量统计与阈值设置SCPI命令
 * @data:2022-12-1 11:32
 */
public class SCPI_Measure_Statistic {
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure_Statistic","Display"),//打开或关闭统计功能
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure_Statistic","DisplayQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure_Statistic","Reset"),//重新统计
//     new SCPICommandStruct(":MEASure:STATistic:MEAN","SCPI_Measure_Statistic","Mean"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN?","SCPI_Measure_Statistic","MeanQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MAX","SCPI_Measure_Statistic","Max"),//打开或关闭最大值
//     new SCPICommandStruct(":MEASure:STATistic:MAX?","SCPI_Measure_Statistic","MaxQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MIN","SCPI_Measure_Statistic","Min"),//打开或关闭最小值
//     new SCPICommandStruct(":MEASure:STATistic:MIN?","SCPI_Measure_Statistic","MinQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:DEV","SCPI_Measure_Statistic","Dev"),//打开或关闭delta
//     new SCPICommandStruct(":MEASure:STATistic:DEV?","SCPI_Measure_Statistic","DevQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:COUNt","SCPI_Measure_Statistic","count"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt?","SCPI_Measure_Statistic","countQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:VIEW?","SCPI_Measure_Statistic","ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN:VIEW?","SCPI_Measure_Statistic","Mean_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MAX:VIEW?","SCPI_Measure_Statistic","Max_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:MIN:VIEW?","SCPI_Measure_Statistic","Min_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:DEV:VIEW?","SCPI_Measure_Statistic","Dev_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt:VIEW?","SCPI_Measure_Statistic","Count_ViewQ"), //询问统计羡慕的所有数值
//     new SCPICommandStruct(":MEASure:STATistic:CURRent:VIEW?","SCPI_Measure_Statistic","Current_ViewQ"), //询问统计羡慕的所有数值

//            new SCPICommandStruct(":MEASure:SETTing:INDicator","SCPI_Measure_Statistic","Indicator"), //
//            new SCPICommandStruct(":MEASure:SETTing:INDicator?","SCPI_Measure_Statistic","IndicatorQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:RANGe","SCPI_Measure_Statistic","Range"), //
//            new SCPICommandStruct(":MEASure:SETTing:RANGe?","SCPI_Measure_Statistic","RangeQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:ThReshold","SCPI_Measure_Statistic","Threshold"), //
//            new SCPICommandStruct(":MEASure:SETTing:ThReshold?","SCPI_Measure_Statistic","ThresholdQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:HIGH","SCPI_Measure_Statistic","High"), //
//            new SCPICommandStruct(":MEASure:SETTing:HIGH?","SCPI_Measure_Statistic","HighQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:MID","SCPI_Measure_Statistic","Mid"), //
//            new SCPICommandStruct(":MEASure:SETTing:MID?","SCPI_Measure_Statistic","MidQ"), //
//            new SCPICommandStruct(":MEASure:SETTing:LOW","SCPI_Measure_Statistic","Low"), //
//            new SCPICommandStruct(":MEASure:SETTing:LOW?","SCPI_Measure_Statistic","LowQ"), //

    /**
     * 打开或关闭测量统计功能
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Display(SCPIParam param){
        Command.get().getMeasure_statistic().Display(param.bParam1, true); // 委托Command中间件设置统计开关，true表示通知UI刷新
    }

    /**
     * 查询测量统计功能的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String DisplayQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().DisplayQ(); // 从Command中间件获取统计开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 清除历史统计数据并重新统计
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void Reset(SCPIParam param){
        Command.get().getMeasure_statistic().Reset(); // 委托Command中间件重置统计数据
    }

    /**
     * 打开或关闭统计均值显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Mean(SCPIParam param){
        Command.get().getMeasure_statistic().Mean(param.bParam1, true); // 委托Command中间件设置均值开关，true表示通知UI刷新
    }

    /**
     * 查询统计均值显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String MeanQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MeanQ(); // 从Command中间件获取均值开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭统计最大值显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Max(SCPIParam param){
        Command.get().getMeasure_statistic().Max(param.bParam1, true); // 委托Command中间件设置最大值开关，true表示通知UI刷新
    }

    /**
     * 查询统计最大值显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String MaxQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MaxQ(); // 从Command中间件获取最大值开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭统计最小值显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Min(SCPIParam param){
        Command.get().getMeasure_statistic().Min(param.bParam1, true); // 委托Command中间件设置最小值开关，true表示通知UI刷新
    }

    /**
     * 查询统计最小值显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String MinQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().MinQ(); // 从Command中间件获取最小值开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭统计标准差(Dev)显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Dev(SCPIParam param){
        Command.get().getMeasure_statistic().Dev(param.bParam1, true); // 委托Command中间件设置标准差开关，true表示通知UI刷新
    }

    /**
     * 查询统计标准差(Dev)显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String DevQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().DevQ(); // 从Command中间件获取标准差开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 打开或关闭统计计数(Count)显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Count(SCPIParam param){
        Command.get().getMeasure_statistic().Count(param.bParam1, true); // 委托Command中间件设置计数开关，true表示通知UI刷新
    }

    /**
     * 查询统计计数(Count)显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String CountQ(SCPIParam param){
        boolean b=Command.get().getMeasure_statistic().CountQ(); // 从Command中间件获取计数开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 查询指定测量项的所有统计数值
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 统计数值字符串
     */
    public static String ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询全部统计视图
    }

    /**
     * 查询指定测量项的统计均值视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 均值统计视图字符串
     */
    public static String Mean_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Mean_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询均值视图
    }

    /**
     * 查询指定测量项的统计最大值视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 最大值统计视图字符串
     */
    public static String Max_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Max_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询最大值视图
    }

    /**
     * 查询指定测量项的统计最小值视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 最小值统计视图字符串
     */
    public static String Min_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Min_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询最小值视图
    }

    /**
     * 查询指定测量项的统计标准差(Dev)视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 标准差统计视图字符串
     */
    public static String Dev_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Dev_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询标准差视图
    }

    /**
     * 查询指定测量项的统计计数(Count)视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 计数统计视图字符串
     */
    public static String Count_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Count_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询计数视图
    }

    /**
     * 查询指定测量项的当前值(Current)视图
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为通道索引
     * @return 当前值统计视图字符串
     */
    public static String Current_ViewQ(SCPIParam param){
        return Command.get().getMeasure_statistic().Current_ViewQ(param.iParam1, param.iParam2); // 委托Command中间件查询当前值视图
    }


    /**
     * 设置测量阈值指示器(Indicator)开关
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Indicator(SCPIParam param){
        Command.get().getMeasure_setting().Indicator(param.bParam1, true); // 委托Command中间件设置指示器开关，true表示通知UI刷新
    }

    /**
     * 设置测量范围模式
     * @param param SCPI参数封装，iParam1为范围模式索引
     */
    public static void Range(SCPIParam param){
        Command.get().getMeasure_setting().Range(param.iParam1, true); // 委托Command中间件设置测量范围，true表示通知UI刷新
    }

    /**
     * 设置测量阈值模式
     * @param param SCPI参数封装，iParam1为阈值模式索引
     */
    public static void Threshold(SCPIParam param){
        Command.get().getMeasure_setting().Threshold(param.iParam1,true); // 委托Command中间件设置阈值模式，true表示通知UI刷新
    }

    /**
     * 设置测量高阈值
     * @param param SCPI参数封装，sParam1为阈值数值字符串
     */
    public static void High(SCPIParam param){
        Command.get().getMeasure_setting().High(param.sParam1, true); // 委托Command中间件设置高阈值，true表示通知UI刷新
    }

    /**
     * 设置测量中阈值
     * @param param SCPI参数封装，sParam1为阈值数值字符串
     */
    public static void Mid(SCPIParam param){
        Command.get().getMeasure_setting().Mid(param.sParam1, true); // 委托Command中间件设置中阈值，true表示通知UI刷新
    }

    /**
     * 设置测量低阈值
     * @param param SCPI参数封装，sParam1为阈值数值字符串
     */
    public static void Low(SCPIParam param){
        Command.get().getMeasure_setting().Low(param.sParam1, true); // 委托Command中间件设置低阈值，true表示通知UI刷新
    }


    /**
     * 查询测量阈值指示器(Indicator)开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String IndicatorQ(SCPIParam param){
        boolean b=Command.get().getMeasure_setting().IndicatorQ(); // 从Command中间件获取指示器开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 查询测量范围模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 范围模式名称字符串
     */
    public static String RangeQ(SCPIParam param){
        int index=Command.get().getMeasure_setting().RangeQ(); // 从Command中间件获取测量范围索引
        return ToolsSCPI.getMeasureSettingRange(index); // 将索引转换为范围模式名称字符串
    }

    /**
     * 查询测量阈值模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 阈值模式名称字符串
     */
    public static String ThresholdQ(SCPIParam param){
        int index=Command.get().getMeasure_setting().ThresholdQ(); // 从Command中间件获取阈值模式索引
        return ToolsSCPI.getMeasureSettingThreshold(index); // 将索引转换为阈值模式名称字符串
    }

    /**
     * 查询测量高阈值
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 高阈值字符串
     */
    public static String HighQ(SCPIParam param){
        return Command.get().getMeasure_setting().HighQ(); // 从Command中间件获取高阈值
    }

    /**
     * 查询测量中阈值
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 中阈值字符串
     */
    public static String MidQ(SCPIParam param){
        return Command.get().getMeasure_setting().MidQ(); // 从Command中间件获取中阈值
    }

    /**
     * 查询测量低阈值
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 低阈值字符串
     */
    public static String LowQ(SCPIParam param){
        return Command.get().getMeasure_setting().LowQ(); // 从Command中间件获取低阈值
    }

}
