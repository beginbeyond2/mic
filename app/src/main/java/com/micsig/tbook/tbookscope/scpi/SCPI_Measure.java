package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import android.os.SystemClock; // 导入Android系统时钟，用于测量耗时
import android.util.Log; // 导入Android日志工具

import com.micsig.base.Logger; // 导入应用日志工具
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量，获取测量项数量等
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理模块

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 自动测量子模块                                 |
 * | 核心职责：处理SCPI协议中:MEASure相关命令的设置与查询                         |
 * | 架构设计：静态方法类，委托Command中间件的Measure接口执行实际操作              |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getMeasure() → 测量结果  |
 * | 依赖关系：SCPIParam、Command、GlobalVar、MeasureManage、ToolsSCPI           |
 * | 使用场景：远程查询波形参数(频率/幅值/时间等)、开关测量项、配置测量设置       |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Measure {
    private static final String TAG="SCPI_Measure"; // 日志标签，用于日志输出标识
    //测量命令 MEAS
//            new SCPICommandStruct(":MEASure:PERiod?","SCPI_Measure","PeriodQ"),//查询指定通道波形的周期测量值
//            new SCPICommandStruct(":MEASure:FREQuency?","SCPI_Measure","FreQuencyQ"),//查询指定通道波形的频率测量值
//            new SCPICommandStruct(":MEASure:RISetime?","SCPI_Measure","RiseTimeQ"),//查询指定通道波形的上升时间测量值
//            new SCPICommandStruct(":MEASure:FALLtime?","SCPI_Measure","FallTimeQ"),//查询指定通道波形的下降时间测量值
//            new SCPICommandStruct(":MEASure:DELay?","SCPI_Measure","DelayQ"),//查询通道间延迟测量的结果
//            new SCPICommandStruct(":MEASure:PDUTy?","SCPI_Measure","PDutyQ"),//查询指定通道波形的正占空比测量值
//            new SCPICommandStruct(":MEASure:NDUTy?","SCPI_Measure","NDutyQ"),//查询指定通道波形的负占空比测量值
//            new SCPICommandStruct(":MEASure:PWIDth?","SCPI_Measure","PWidthQ"),//查询指定通道波形的正脉宽测量值
//            new SCPICommandStruct(":MEASure:NWIDth?","SCPI_Measure","NWidthQ"),//查询指定通道波形的负脉宽测量值
//            new SCPICommandStruct(":MEASure:BURStwidth?","SCPI_Measure","BurstWidthQ"),//查询指定通道波形的突发脉冲宽度测量值
//            new SCPICommandStruct(":MEASure:ROV?","SCPI_Measure","RovQ"),//查询指定通道波形的正向超调测量值
//            new SCPICommandStruct(":MEASure:FOV?","SCPI_Measure","FovQ"),//查询指定通道波形的负向超调测量值
//            new SCPICommandStruct(":MEASure:PHASe?","SCPI_Measure","PhaseQ"),//查询指定通道间相位差测量的结果
//            new SCPICommandStruct(":MEASure:PKPK?","SCPI_Measure","PkpkQ"),//查询指定通道波形的峰峰值
//            new SCPICommandStruct(":MEASure:AMP?","SCPI_Measure","AmpQ"),//查询指定通道波形的幅度测量值
//            new SCPICommandStruct(":MEASure:HIGH?","SCPI_Measure","HighQ"),//查询指定通道波形的高值
//            new SCPICommandStruct(":MEASure:LOW?","SCPI_Measure","LowQ"),//查询指定通道波形的低值
//            new SCPICommandStruct(":MEASure:MAX?","SCPI_Measure","MaxQ"),//查询指定通道波形的最大值
//            new SCPICommandStruct(":MEASure:MIN?","SCPI_Measure","MinQ"),//查询指定通道波形的最小值
//            new SCPICommandStruct(":MEASure:RMS?","SCPI_Measure","RmsQ"),//查询指定通道波形的均方根值
//            new SCPICommandStruct(":MEASure:CRMS?","SCPI_Measure","CrmsQ"),//查询指定通道波形的周期均方根值
//            new SCPICommandStruct(":MEASure:MEAN?","SCPI_Measure","MeanQ"),//查询指定通道波形的平均值
//            new SCPICommandStruct(":MEASure:CMEan?","SCPI_Measure","CMeanQ"),//查询指定通道波形的周期平均值
//            new SCPICommandStruct(":MEASure:ACRMs?","SCPI_Measure","ACRMs"),//
//            new SCPICommandStruct(":MEASure:PRATe?","SCPI_Measure","PRATe"),//
//            new SCPICommandStruct(":MEASure:NRATe?","SCPI_Measure","NRATe"),//
//            new SCPICommandStruct(":MEASure:COLVal?","SCPI_Measure","ColValQ"),

    /**
     * 查询指定通道波形的周期测量值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 周期测量值的字符串表示
     */
    public static String PeriodQ(SCPIParam param){
         return Command.get().getMeasure().PeriodQ(param.iParam1); // 委托Command中间件查询周期
    }

    /**
     * 查询指定通道波形的频率测量值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 频率测量值的字符串表示
     */
    public static String FreQuencyQ(SCPIParam param){
         return Command.get().getMeasure().FreQuencyQ(param.iParam1); // 委托Command中间件查询频率
    }

    /**
     * 查询指定通道波形的上升时间测量值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 上升时间测量值的字符串表示
     */
    public static String RiseTimeQ(SCPIParam param){
        return Command.get().getMeasure().RiseTimeQ(param.iParam1); // 委托Command中间件查询上升时间

    }

    /**
     * 查询指定通道波形的下降时间测量值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 下降时间测量值的字符串表示
     */
    public static String FallTimeQ(SCPIParam param){
        return Command.get().getMeasure().FallTimeQ(param.iParam1); // 委托Command中间件查询下降时间
    }

    /**
     * 查询两通道间延迟测量结果
     * @param param SCPI参数封装，iParam1/iParam2为两个通道索引
     * @return 延迟测量值的字符串表示
     */
    public static String DelayQ(SCPIParam param){
        return Command.get().getMeasure().DelayQ(param.iParam1,param.iParam2); // 委托Command中间件查询两通道间延迟
    }

    /**
     * 查询指定通道波形的正占空比
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 正占空比测量值的字符串表示
     */
    public static String PDutyQ(SCPIParam param){
        return Command.get().getMeasure().PDutyQ(param.iParam1); // 委托Command中间件查询正占空比
    }

    /**
     * 查询指定通道波形的负占空比
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 负占空比测量值的字符串表示
     */
    public static String NDutyQ(SCPIParam param){
        return Command.get().getMeasure().NDutyQ(param.iParam1); // 委托Command中间件查询负占空比
    }

    /**
     * 查询指定通道波形的正脉宽
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 正脉宽测量值的字符串表示
     */
    public static String PWidthQ(SCPIParam param){
        return Command.get().getMeasure().PWidthQ(param.iParam1); // 委托Command中间件查询正脉宽
    }

    /**
     * 查询指定通道波形的负脉宽
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 负脉宽测量值的字符串表示
     */
    public static String NWidthQ(SCPIParam param){
        return Command.get().getMeasure().NWidthQ(param.iParam1); // 委托Command中间件查询负脉宽
    }

    /**
     * 查询指定通道波形的突发脉冲宽度
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 突发脉冲宽度测量值的字符串表示
     */
    public static String BurstWidthQ(SCPIParam param){
        return Command.get().getMeasure().BurstWidthQ(param.iParam1); // 委托Command中间件查询突发脉冲宽度
    }

    /**
     * 查询指定通道波形的正向超调
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 正向超调测量值的字符串表示
     */
    public static String RovQ(SCPIParam param){
        return Command.get().getMeasure().RovQ(param.iParam1); // 委托Command中间件查询正向超调
    }

    /**
     * 查询指定通道波形的负向超调
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 负向超调测量值的字符串表示
     */
    public static String FovQ(SCPIParam param){
        return Command.get().getMeasure().FovQ(param.iParam1); // 委托Command中间件查询负向超调
    }

    /**
     * 查询两通道间相位差测量结果
     * @param param SCPI参数封装，iParam1/iParam2为两个通道索引
     * @return 相位差测量值的字符串表示
     */
    public static String PhaseQ(SCPIParam param){
        return Command.get().getMeasure().PhaseQ(param.iParam1,param.iParam2); // 委托Command中间件查询相位差
    }

    /**
     * 查询指定通道波形的峰峰值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 峰峰值测量值的字符串表示
     */
    public static String PkpkQ(SCPIParam param){
        return Command.get().getMeasure().PkpkQ(param.iParam1); // 委托Command中间件查询峰峰值
    }

    /**
     * 查询指定通道波形的幅度
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 幅度测量值的字符串表示
     */
    public static String AmpQ(SCPIParam param){
        return Command.get().getMeasure().AmpQ(param.iParam1); // 委托Command中间件查询幅度
    }

    /**
     * 查询指定通道波形的高值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 高值测量值的字符串表示
     */
    public static String HighQ(SCPIParam param){
        return Command.get().getMeasure().HighQ(param.iParam1); // 委托Command中间件查询高值
    }

    /**
     * 查询指定通道波形的低值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 低值测量值的字符串表示
     */
    public static String LowQ(SCPIParam param){
        return Command.get().getMeasure().LowQ(param.iParam1); // 委托Command中间件查询低值
    }

    /**
     * 查询指定通道波形的最大值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 最大值测量值的字符串表示
     */
    public static String MaxQ(SCPIParam param){
        return Command.get().getMeasure().MaxQ(param.iParam1); // 委托Command中间件查询最大值
    }

    /**
     * 查询指定通道波形的最小值
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 最小值测量值的字符串表示
     */
    public static String MinQ(SCPIParam param){
        return Command.get().getMeasure().MinQ(param.iParam1); // 委托Command中间件查询最小值
    }

    /**
     * 查询指定通道波形的均方根值(RMS)
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 均方根值的字符串表示
     */
    public static String RmsQ(SCPIParam param){
        return Command.get().getMeasure().RmsQ(param.iParam1); // 委托Command中间件查询均方根值

    }

    /**
     * 查询指定通道波形的周期均方根值(CRMS)
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 周期均方根值的字符串表示
     */
    public static String CrmsQ(SCPIParam param){
        return Command.get().getMeasure().CrmsQ(param.iParam1); // 委托Command中间件查询周期均方根值
    }

    /**
     * 查询指定通道波形的平均值(Mean)
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 平均值的字符串表示
     */
    public static String MeanQ(SCPIParam param){
        return Command.get().getMeasure().MeanQ(param.iParam1); // 委托Command中间件查询平均值

    }

    /**
     * 查询指定通道波形的周期平均值(CMean)
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 周期平均值的字符串表示
     */
    public static String CMeanQ(SCPIParam param){
        return Command.get().getMeasure().CMeanQ(param.iParam1); // 委托Command中间件查询周期平均值
    }

    /**
     * 查询指定通道波形的交流均方根值(AC RMS)
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 交流均方根值的字符串表示
     */
    public static String ACRMSQ(SCPIParam param){
        return Command.get().getMeasure().ACRMSQ(param.iParam1); // 委托Command中间件查询交流均方根值
    }

    /**
     * 查询指定通道波形的正边沿速率
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 正边沿速率的字符串表示
     */
    public static String PRATEQ(SCPIParam param){
        return Command.get().getMeasure().PRATEQ(param.iParam1); // 委托Command中间件查询正边沿速率
    }

    /**
     * 查询指定通道波形的负边沿速率
     * @param param SCPI参数封装，iParam1为通道索引
     * @return 负边沿速率的字符串表示
     */
    public static String NRATEQ(SCPIParam param){
        return Command.get().getMeasure().NRATEQ(param.iParam1); // 委托Command中间件查询负边沿速率
    }

    /**
     * 查询指定测量项的所有通道值
     * @param param SCPI参数封装，iParam1为测量项索引
     * @return 多通道测量值的字符串表示
     */
    public static String ColValQ(SCPIParam param){
        return Command.get().getMeasure().ColValQ(param.iParam1); // 委托Command中间件查询测量项所有通道值
    }

//     new SCPICommandStruct(":MEASure:AREa?","SCPI_Measure","AreaQ"),//查询指定通道波形的面积
//            new SCPICommandStruct(":MEASure:CARea?","SCPI_Measure","CareaQ"),//查询指定通道波形的周期面积
//            new SCPICommandStruct(":MEASure:CLEar","SCPI_Measure","Clear"),//清除打开的测量项中的任一项或所有项
//            new SCPICommandStruct(":MEASure:OPEN","SCPI_Measure","Open"),//打开测量项
//            new SCPICommandStruct(":MEASure:CLOSe","SCPI_Measure","Close"),//关闭测量项
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure","Statistic_Display"),//打开或关闭统计功能
//            new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure","Statistic_DisplayQ"),//查询统计功能打开或关闭
//            new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure","Statistic_Reset"),//清楚历史统计数据并重新统计
//            new SCPICommandStruct(":MEASure:ADISplay","SCPI_Measure","Adislay"),//打开或关闭全部测量
//            new SCPICommandStruct(":MEASure:ADISplay?","SCPI_Measure","AdisplayQ"),//查询全部测量打开或关闭
//            new SCPICommandStruct(":MEASure:SCOPe","SCPI_Measure","Scope"),//设置测量范围
//            new SCPICommandStruct(":MEASure:SCOPe?","SCPI_Measure","ScopeQ"),//查询测量范围
//            new SCPICommandStruct(":MEASure:COUNter:SOURce","SCPI_Measure","Counter_Source"),//设置源
//            new SCPICommandStruct(":MEASure:COUNter:SOURce?","SCPI_Measure","Counter_SourceQ"),//设置源
//           new SCPICommandStruct(":MEASure:COUNter:MODE","SCPI_Measure","Counter_Mode"),
//            new SCPICommandStruct(":MEASure:COUNter:MODE?","SCPI_Measure","Counter_ModeQ"),
//            new SCPICommandStruct(":MEASure:COUNter:VALue?","SCPI_Measure","Counter_ValueQ"),//查询频率计结果
//            new SCPICommandStruct(":MEASure:ITEM","SCPI_Measure","Item"),//设置信源
//            new SCPICommandStruct(":MEASure:ITEM?","SCPI_Measure","ItemQ"),//设置信源

    /**
     * 查询指定通道波形的面积（占位实现，暂无操作）
     * @param param SCPI参数封装
     */
    public static void AreaQ(SCPIParam param){}

    /**
     * 查询指定通道波形的周期面积（占位实现，暂无操作）
     * @param param SCPI参数封装
     */
    public static void CareaQ(SCPIParam param){}

    /**
     * 清除测量项。若iParam1等于测量项总数则清除所有，否则清除指定项
     * @param param SCPI参数封装，iParam1为测量项索引（等于总数时清除全部）
     */
    public static void Clear(SCPIParam param){
        if (param.iParam1 == GlobalVar.get().getMeasureItemCount()) { // 判断是否请求清除所有测量项
            Command.get().getMeasure().ClearAllItem(true); // 清除全部测量项，true表示通知UI刷新
        } else {
            Command.get().getMeasure().Clear(param.iParam1, true); // 清除指定测量项，true表示通知UI刷新
        }
    }

    /**
     * 打开（添加）一个测量项
     * @param param SCPI参数封装，包含测量项类型、通道等多个参数
     */
    public static void Open(SCPIParam param){
        Command.get().getMeasure().Open(-1,param.iParam1,param.iParam2,param.iParam3,param.iParam4,param.iParam5,param.dParam1,true); // 委托Command中间件打开测量项，-1表示自动分配索引
    }

    /**
     * 关闭（移除）一个测量项
     * @param param SCPI参数封装，iParam1为测量项索引，iParam2为参数
     */
    public static void Close(SCPIParam param){
        Command.get().getMeasure().Close(param.iParam1,param.iParam2,true); // 委托Command中间件关闭测量项，true表示通知UI刷新
    }

    /**
     * 打开或关闭统计功能（占位实现，委托给Statistic子模块）
     * @param param SCPI参数封装
     */
    public static void Statistic_Display(SCPIParam param){}

    /**
     * 查询统计功能开关状态（占位实现）
     * @param param SCPI参数封装
     */
    public static void Statistic_DisplayQ(SCPIParam param){}

    /**
     * 清除历史统计数据并重新统计（占位实现）
     * @param param SCPI参数封装
     */
    public static void Statistic_Reset(SCPIParam param){}

    /**
     * 打开或关闭全部测量显示
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void Adislay(SCPIParam param){
        Command.get().getMeasure().Adislay(param.bParam1,true); // 委托Command中间件设置全部测量开关，true表示通知UI刷新
    }

    /**
     * 查询全部测量显示的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String AdisplayQ(SCPIParam param){
        boolean b= Command.get().getMeasure().AdisplayQ(); // 从Command中间件获取全部测量开关状态
        return ToolsSCPI.getOpenState(b); // 将布尔值转换为"ON"/"OFF"字符串
    }

    /**
     * 设置测量范围（占位实现）
     * @param param SCPI参数封装
     */
    public static void Scope(SCPIParam param){}

    /**
     * 查询测量范围（占位实现）
     * @param param SCPI参数封装
     */
    public static void ScopeQ(SCPIParam param){}

    /**
     * 设置频率计的信源通道
     * @param param SCPI参数封装，iParam1为通道索引
     */
    public static void Counter_Source(SCPIParam param){
        Command.get().getMeasure().Counter_Source(param.iParam1, true); // 委托Command中间件设置频率计信源，true表示通知UI刷新
    }

    /**
     * 查询频率计的信源通道
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 通道名称字符串
     */
    public static String Counter_SourceQ(SCPIParam param){
         int i= Command.get().getMeasure().Counter_SourceQ(); // 从Command中间件获取频率计信源索引
         return ToolsSCPI.getCounterCh(i); // 将索引转换为频率计通道名称字符串
    }

    /**
     * 打开时间值(TValue)测量项
     * @param param SCPI参数封装，iParam1/iParam2将被重组为测量项参数
     */
    public static void TVALue(SCPIParam param){
        param.iParam3 = param.iParam2; // 将原iParam2移至iParam3位置
        param.iParam2 = param.iParam1; // 将原iParam1移至iParam2位置
        param.iParam1 = MeasureManage.IMeasure.MeasureId_TVALUE; // 设置iParam1为TValue测量类型ID
        Open(param); // 调用Open方法添加TValue测量项
    }

    /**
     * 查询时间值(TValue)测量结果
     * @param param SCPI参数封装，iParam1为测量项索引
     * @return TValue测量值的字符串表示
     */
    public static String  TVALueQ(SCPIParam param){
        return Command.get().getMeasure().TValueQ(param.iParam1); // 委托Command中间件查询TValue测量值
    }

    /**
     * 设置频率计的模式
     * @param param SCPI参数封装，iParam1为模式索引
     */
    public static void Counter_Mode(SCPIParam param){
            Command.get().getMeasure().Counter_Mode(param.iParam1, true); // 委托Command中间件设置频率计模式，true表示通知UI刷新
    }

    /**
     * 查询频率计的模式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 频率计模式名称字符串
     */
    public static String Counter_ModeQ(SCPIParam param){
        int i=Command.get().getMeasure().Counter_ModeQ(); // 从Command中间件获取频率计模式索引
        return ToolsSCPI.getCountMode(i); // 将模式索引转换为名称字符串
    }

    /**
     * 查询频率计的测量值
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 频率计测量值的字符串表示
     */
    public static String Counter_ValueQ(SCPIParam param){
        return Command.get().getMeasure().Counter_ValueQ(); // 委托Command中间件查询频率计测量值

    }

    private static String ss; // 用于Item方法测试的静态字符串缓存

    /**
     * 设置测量信源（性能测试方法，生成大量十六进制字符串用于性能验证）
     * @param param SCPI参数封装
     */
    public static void Item(SCPIParam param){
        long c= SystemClock.elapsedRealtime(); // 记录开始时间（毫秒）

        int max=1024/2*256; //256k // 计算循环次数：512*256=131072
        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        for (int i=0;i<max;i++){ // 循环生成十六进制字符串
            sb.append( Integer.toHexString(i & 0xFF)); // 取低8位转为十六进制追加
        }
        ss=sb.toString(); // 将结果保存到静态变量
        c= SystemClock.elapsedRealtime()-c; // 计算耗时
        Logger.i(Command.TAG,"create complete:"+c); // 输出耗时日志
    }

    /**
     * 查询测量信源信息（返回格式化的IEEE488.2任意块数据头）
     * @param param SCPI参数封装
     * @return 格式化的数据头字符串
     */
    public static String ItemQ(SCPIParam param){
//        Item(param);
//        Logger.i(Command.TAG,"len:"+ss.length());
        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        sb.append("#9000000000#"); // 附加IEEE488.2任意块数据头占位符
        sb.replace(3,11,String.format("%08X",0x7FFFFFFF)); // 将占位符替换为最大数据长度(0x7FFFFFFF)的8位十六进制
        return sb.toString(); // 返回格式化数据头字符串
    }

    /**
     * 查询所有测量项列表
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 测量项列表字符串
     */
    public static String ListQ(SCPIParam param) {
        return Command.get().getMeasure().ListQ(); // 委托Command中间件查询测量项列表
    }


    /**
     * 新增一个测量项（索引从0开始，参数从1开始需减1）
     * @param param SCPI参数封装，iParam1为1-based索引
     */
    public static void AddNew(SCPIParam param){
        Command.get().getMeasure().AddNew(param.iParam1 - 1); // 委托Command中间件新增测量项，iParam1-1转为0-based索引
    }

    /**
     * 删除一个测量项（索引从0开始，参数从1开始需减1）
     * @param param SCPI参数封装，iParam1为1-based索引
     */
    public static void Delete(SCPIParam param){
        Command.get().getMeasure().Delete(param.iParam1 - 1); // 委托Command中间件删除测量项，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X测量类型
     * @param param SCPI参数封装，包含测量项索引和类型参数
     */
    public static void XType(SCPIParam param){
        Command.get().getMeasure().XType(param); // 委托Command中间件设置X测量类型
    }

    /**
     * 查询自定义测量项的X测量类型
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X测量类型名称字符串
     */
    public static String XTypeQ(SCPIParam param){
        return Command.get().getMeasure().XTypeQ(param.iParam1 - 1); // 查询X测量类型，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X信源1
     * @param param SCPI参数封装，iParam1为1-based索引，iParam2为信源类型
     */
    public static void XSOURce1(SCPIParam param){
        Command.get().getMeasure().XSOURce1(param.iParam1-1,param.iParam2); // 委托Command中间件设置X信源1，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X信源1
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X信源1名称字符串
     */
    public static String XSOURce1Q(SCPIParam param){
        return Command.get().getMeasure().XSOURce1Q(param.iParam1-1); // 查询X信源1，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X信源2
     * @param param SCPI参数封装，iParam1为1-based索引，iParam2为信源类型
     */
    public static void XSOURce2(SCPIParam param){
        Command.get().getMeasure().XSOURce2(param.iParam1-1,param.iParam2); // 委托Command中间件设置X信源2，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X信源2
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X信源2名称字符串
     */
    public static String XSOURce2Q(SCPIParam param){
        return Command.get().getMeasure().XSOURce2Q(param.iParam1-1); // 查询X信源2，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X测量值
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X测量值字符串
     */
    public static String XVALueQ(SCPIParam param){
        return Command.get().getMeasure().XVALueQ(param.iParam1-1); // 查询X测量值，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X单位
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X单位字符串
     */
    public static String XUnitQ(SCPIParam param){
        return Command.get().getMeasure().XUnitQ(param.iParam1-1); // 查询X单位，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X有效状态
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X有效状态字符串
     */
    public static String XValidQ(SCPIParam param){
        return Command.get().getMeasure().XValidQ(param.iParam1-1); // 查询X有效状态，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X边沿1
     * @param param SCPI参数封装，iParam1为1-based索引，iParam2为边沿类型
     */
    public static void XEdge1(SCPIParam param){
        Command.get().getMeasure().XEdge1(param.iParam1-1, param.iParam2); // 委托Command中间件设置X边沿1，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X边沿1
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X边沿1名称字符串
     */
    public static String XEdge1Q(SCPIParam param){
        return Command.get().getMeasure().XEdge1Q(param.iParam1-1); // 查询X边沿1，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X边沿2
     * @param param SCPI参数封装，iParam1为1-based索引，iParam2为边沿类型
     */
    public static void XEdge2(SCPIParam param){
        Command.get().getMeasure().XEdge2(param.iParam1-1, param.iParam2); // 委托Command中间件设置X边沿2，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X边沿2
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X边沿2名称字符串
     */
    public static String XEdge2Q(SCPIParam param){
        return Command.get().getMeasure().XEdge2Q(param.iParam1-1); // 查询X边沿2，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X光标
     * @param param SCPI参数封装，iParam1为1-based索引，iParam2为光标参数
     */
    public static void XCURSor(SCPIParam param){
        Command.get().getMeasure().XCURSor(param.iParam1-1,param.iParam2); // 委托Command中间件设置X光标，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X光标
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X光标值字符串
     */
    public static String XCURSorQ(SCPIParam param){
        return Command.get().getMeasure().XCURSorQ(param.iParam1-1); // 查询X光标，iParam1-1转为0-based索引
    }

    /**
     * 设置自定义测量项的X光标值
     * @param param SCPI参数封装，iParam1为1-based索引，dParam1为光标位置值
     */
    public static void XVVLue(SCPIParam param){
        Command.get().getMeasure().XVVLue(param.iParam1-1,param.dParam1); // 委托Command中间件设置X光标值，iParam1-1转为0-based索引
    }

    /**
     * 查询自定义测量项的X光标值
     * @param param SCPI参数封装，iParam1为1-based索引
     * @return X光标值字符串
     */
    public static String XVVLueQ(SCPIParam param){
        return Command.get().getMeasure().XVVLueQ(param.iParam1-1); // 查询X光标值，iParam1-1转为0-based索引
    }
}
