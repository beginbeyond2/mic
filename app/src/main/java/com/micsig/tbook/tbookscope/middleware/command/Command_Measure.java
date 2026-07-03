package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI测量命令处理


import android.util.Log; // Android日志工具

import com.micsig.base.Logger; // 日志工具
import com.micsig.tbook.scope.Auto.FreqCounter; // 频率计数器
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.SCPIParam; // SCPI参数封装
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 测量管理器
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

import java.util.ArrayList; // 动态数组
import java.util.Comparator; // 比较器
import java.util.List; // 列表接口
import java.util.Optional; // Optional容器
import java.util.stream.Collectors; // Stream收集器

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                           Command_Measure                                    |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 测量子系统命令处理层                              |
 * | 核心职责：管理示波器自动测量功能的全部SCPI指令，包括：                        |
 * |          1. 测量值查询：周期/频率/上升时间/下降时间/延迟/占空比/脉宽/         |
 * |             峰峰值/幅度/高值/低值/最大值/最小值/RMS/CRMS/均值等20+项         |
 * |          2. 测量项管理：打开/关闭/清除/全部测量/统计/计数器                   |
 * |          3. 扩展测量命令：XType/XSOURce/XVALue/XUnit/XValid/XEdge/XCURSor   |
 * | 架构设计：属于Command的子模块，由Command单例统一调度；                       |
 * |          内部维护MeasureInfo列表跟踪所有已打开的测量项；                     |
 * |          延时(4)/相位(12)/ColValQ/TVALUE等特殊测量项需要额外参数；           |
 * |          查询类方法通过统一query()方法在listMeasure中查找结果                |
 * | 数据流向：SCPI指令 → Command_Measure → MeasureInfo列表(状态存储)            |
 * |                            → RxBus → UI层(测量项开关/值更新)                |
 * | 依赖关系：Command(单例入口)、MeasureManage(测量管理)、ChannelFactory(通道)  |
 * |           RxBus(事件总线)、FreqCounter(频率计数器)、ToolsSCPI(格式化)       |
 * | 使用场景：远程SCPI控制中查询测量值、打开/关闭测量项、管理测量统计           |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Measure {
    private static final String TAG="Command_Measure"; // 日志标签

    /**
     * 测量项信息内部类，存储单个测量项的类型、通道、值、有效性等
     */
   public class MeasureInfo {
        public static final int FLAG_ClearAll=0x01; // 清除全部测量项标志

        public int flag=0; // 操作标志位

        public int no = -1; // 测量项序号（从0开始），-1表示未分配
        public int measureType; // 测量类型ID，对应MeasureManage.IMeasure.MeasureId_*
        /** 通道号从1开始 */
        public int measureChannel; // 测量通道号（从0开始的内部索引）
        public double measureValue; // 测量值
        public boolean isValid; // 测量值是否有效
        //延时  相位使用
        public int param1; // 扩展参数1：延时第二通道/相位第二通道/ColValQ列位置/TVALUE边沿类型
        public int param2; // 扩展参数2：延时边沿1/相位边沿/TVALUE光标类型
        public int param3; // 扩展参数3：延时边沿2

        public double dparam1; // 双精度扩展参数：TVALUE的变量值

        /**
         * 获取测量项序号
         * @return 序号值
         */
        public int getNo(){
            return no; // 返回序号
        }
        /**
         * 默认构造函数
         */
        public MeasureInfo(){}
        /**
         * 带参数构造函数
         * @param measureType 测量类型
         * @param measureChannel 测量通道
         * @param measureValue 测量值
         */
        public MeasureInfo(int measureType, int measureChannel, int measureValue) {
            this.measureType = measureType; // 设置测量类型
            this.measureChannel = measureChannel; // 设置测量通道
            this.measureValue = measureValue; // 设置测量值
        }

       /**
        * 返回MeasureInfo的字符串表示，用于调试日志
        * @return 格式化字符串
        */
       @Override
       public String toString() {
           return  "measureType:"+this.measureType // 测量类型
                   +",measureChannel:"+this.measureChannel // 测量通道
                   +",measureValue:"+this.measureValue // 测量值
                   +",param1:"+this.param1 // 扩展参数1
                   +",flag:"+flag; // 操作标志
       }
   }

    private List<MeasureInfo> listMeasure = new ArrayList<>(); // 已打开的测量项列表

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
//            new SCPICommandStruct(":MEASure:COLVal?","SCPI_Measure","ColValQ"),


 /*   const char * meas_all[] = {
        "PERiod",//0
                "FREQ",
                "RISetime",
                "FALLtime",
                "DELay",
                "PDUTy",//5
                "NDUTy",
                "PWIDth",
                "NWIDth",
                "BURStwidth",
                "ROVershoot",//10
                "FOVershoot",
                "PHASe",
                "PKPK",
                "AMPlitude",
                "HIGH",//15
                "LOW",
                "MAX",
                "MIN",
                "RMS",
                "CRMS",//20
                "MEAN",
                "CMEan",
                "COLVal",
                NULL
    };
const char * delay_edge[] = {
        "FRISe",
                "FFALl",
                "LRISe",
                "LFALl",
                NULL
    };*/


    boolean isChange = false; // 测量项位置是否发生变更标志
    /**
     * 根据UI层测量项有效列表重新排列测量项顺序
     * 将listMeasure与UI层有效测量列表进行匹配，保留匹配项并按UI顺序排列
     */
    public void changeMeasurePos() {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getValidMeasureList(); // 获取UI层有效测量列表
        List<MeasureInfo> tempList = new ArrayList<>(); // 临时列表存储匹配的测量项
        for (MeasureManage.MeasureItemStruct itemStruct : list) { // 遍历UI层测量项
            for (MeasureInfo info : listMeasure) { // 遍历当前测量项列表
                if (itemStruct.getChannelId() == info.measureChannel + 1 && itemStruct.getMeasureId() == info.measureType) { // 通道和类型都匹配（注意通道号差1）
                    tempList.add(info); // 添加到临时列表
                    break; // 找到匹配项后跳出内层循环
                }
            }
        }
        listMeasure.clear(); // 清空当前列表
        listMeasure.addAll(tempList); // 按UI层顺序重新添加
        isChange = true; // 标记位置已变更
    }

    /**
     * 统一测量值查询方法
     * 在listMeasure中查找指定通道和类型的测量项，返回格式化的测量值或"--"
     * @param ch 通道索引（从0开始）
     * @param measureType 测量类型ID
     * @return 格式化的测量值字符串，无效时返回"--"
     */
    private String  query(int ch,int measureType){
//        Logger.i(TAG,"listMeasure.size:"+listMeasure.size()+ " ch:"+ch+" type:"+measureType);
//        for(int i=0;i<listMeasure.size();i++){
//            MeasureInfo info=listMeasure.get(i);
//            Logger.i(TAG,"measureinfo.type:"+info.measureType +"  channel:"+info.measureChannel);
//        }
//        ch++;
//        Logger.i("command","chIndex:"+ch);
        for(int i=0;i<listMeasure.size();i++){ // 遍历所有已打开的测量项
            MeasureInfo info=listMeasure.get(i); // 获取当前测量项
            if (ch==info.measureChannel && measureType==info.measureType){ // 通道和类型都匹配
                if (info.isValid) { // 测量值有效
                    return ToolsSCPI.getDouble(info.measureValue); // 返回格式化的测量值
                }else{ // 测量值无效
                    return "--"; // 返回无效标记
                }
            }
        }
        return "--"; // 未找到匹配的测量项，返回无效标记
    }

    /**
     *  如： measure:RMS? ch1
     */

    /**
     * 查询指定通道波形的周期测量值
     * 对应SCPI指令: :MEASure:PERiod?
     * @param ch 通道索引
     * @return 周期测量值字符串
     */
    public String PeriodQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Period); // 查询周期
    }

    /**
     * 查询指定通道波形的频率测量值
     * 对应SCPI指令: :MEASure:FREQuency?
     * @param ch 通道索引
     * @return 频率测量值字符串
     */
    public String FreQuencyQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_Freq); // 查询频率
    }

    /**
     * 查询指定通道波形的上升时间测量值
     * 对应SCPI指令: :MEASure:RISetime?
     * @param ch 通道索引
     * @return 上升时间测量值字符串
     */
    public String RiseTimeQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_RiseTime); // 查询上升时间
    }

    /**
     * 查询指定通道波形的下降时间测量值
     * 对应SCPI指令: :MEASure:FALLtime?
     * @param ch 通道索引
     * @return 下降时间测量值字符串
     */
    public String FallTimeQ(int ch) {
        return query(ch,MeasureManage.IMeasure.MeasureId_FallTime); // 查询下降时间
    }

    /**
     * 查询通道间延迟测量的结果
     * 对应SCPI指令: :MEASure:DELay?
     * @param chIndex 第一通道索引
     * @param chIndex2 第二通道索引（未使用，延时参数通过param1存储）
     * @return 延时测量值字符串
     */
    public String DelayQ(int chIndex, int chIndex2) {
        return query(chIndex,MeasureManage.IMeasure.MeasureId_Delay); // 查询延时
    }

    /**
     * 查询指定通道波形的正占空比测量值
     * 对应SCPI指令: :MEASure:PDUTy?
     * @param ch 通道索引
     * @return 正占空比测量值字符串
     */
    public String PDutyQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_DutyAdd); // 查询正占空比
    }

    /**
     * 查询指定通道波形的负占空比测量值
     * 对应SCPI指令: :MEASure:NDUTy?
     * @param ch 通道索引
     * @return 负占空比测量值字符串
     */
    public String NDutyQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_DutySub); // 查询负占空比
    }

    /**
     * 查询指定通道波形的正脉宽测量值
     * 对应SCPI指令: :MEASure:PWIDth?
     * @param ch 通道索引
     * @return 正脉宽测量值字符串
     */
    public String PWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_WidthAdd); // 查询正脉宽
    }

    /**
     * 查询指定通道波形的负脉宽测量值
     * 对应SCPI指令: :MEASure:NWIDth?
     * @param ch 通道索引
     * @return 负脉宽测量值字符串
     */
    public String NWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_WidthSub); // 查询负脉宽
    }

    /**
     * 查询指定通道波形的突发脉冲宽度测量值
     * 对应SCPI指令: :MEASure:BURStwidth?
     * @param ch 通道索引
     * @return 突发脉冲宽度测量值字符串
     */
    public String BurstWidthQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_BurstW); // 查询突发脉冲宽度
    }

    /**
     * 查询指定通道波形的正向超调测量值
     * 对应SCPI指令: :MEASure:ROV?
     * @param ch 通道索引
     * @return 正向超调测量值字符串
     */
    public String RovQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_ROV); // 查询正向超调
    }

    /**
     * 查询指定通道波形的负向超调测量值
     * 对应SCPI指令: :MEASure:FOV?
     * @param ch 通道索引
     * @return 负向超调测量值字符串
     */
    public String FovQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_FOV); // 查询负向超调
    }

    /**
     * 查询指定通道间相位差测量的结果
     * 对应SCPI指令: :MEASure:PHASe?
     * @param chIndex 第一通道索引
     * @param chIndex2 第二通道索引（未使用）
     * @return 相位差测量值字符串
     */
    public String PhaseQ(int chIndex, int chIndex2) {
        return query(chIndex,MeasureManage.IMeasure.MeasureId_Phase); // 查询相位差
    }

    /**
     * 查询指定通道波形的峰峰值
     * 对应SCPI指令: :MEASure:PKPK?
     * @param ch 通道索引
     * @return 峰峰值测量值字符串
     */
    public String PkpkQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_PKPK); // 查询峰峰值
    }

    /**
     * 查询指定通道波形的幅度测量值
     * 对应SCPI指令: :MEASure:AMP?
     * @param ch 通道索引
     * @return 幅度测量值字符串
     */
    public String AmpQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Amp); // 查询幅度
    }

    /**
     * 查询指定通道波形的高值
     * 对应SCPI指令: :MEASure:HIGH?
     * @param ch 通道索引
     * @return 高值测量值字符串
     */
    public String HighQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_High); // 查询高值
    }

    /**
     * 查询指定通道波形的低值
     * 对应SCPI指令: :MEASure:LOW?
     * @param ch 通道索引
     * @return 低值测量值字符串
     */
    public String LowQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Low); // 查询低值
    }

    /**
     * 查询指定通道波形的最大值
     * 对应SCPI指令: :MEASure:MAX?
     * @param ch 通道索引
     * @return 最大值测量值字符串
     */
    public String MaxQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Max); // 查询最大值
    }

    /**
     * 查询指定通道波形的最小值
     * 对应SCPI指令: :MEASure:MIN?
     * @param ch 通道索引
     * @return 最小值测量值字符串
     */
    public String MinQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Min); // 查询最小值
    }

    /**
     * 查询指定通道波形的均方根值
     * 对应SCPI指令: :MEASure:RMS?
     * @param ch 通道索引
     * @return RMS测量值字符串
     */
    public String RmsQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_RMS); // 查询均方根值
    }

    /**
     * 查询指定通道波形的周期均方根值
     * 对应SCPI指令: :MEASure:CRMS?
     * @param ch 通道索引
     * @return CRMS测量值字符串
     */
    public String CrmsQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_CRMS); // 查询周期均方根值
    }

    /**
     * 查询指定通道波形的平均值
     * 对应SCPI指令: :MEASure:MEAN?
     * @param ch 通道索引
     * @return 平均值测量值字符串
     */
    public String MeanQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_Mean); // 查询平均值
    }

    /**
     * 查询指定通道波形的周期平均值
     * 对应SCPI指令: :MEASure:CMEan?
     * @param ch 通道索引
     * @return 周期平均值测量值字符串
     */
    public String CMeanQ(int ch) {
        return query(ch, MeasureManage.IMeasure.MeasureId_CMean); // 查询周期平均值
    }

    /**
     * 查询指定通道波形的交流均方根值(AC RMS)
     * @param ch 通道索引
     * @return ACRMS测量值字符串
     */
    public String ACRMSQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_ACRMS); // 查询交流均方根值
    }

    /**
     * 查询指定通道波形的正沿速率
     * @param ch 通道索引
     * @return 正沿速率测量值字符串
     */
    public String PRATEQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_PostitiveRate); // 查询正沿速率
    }

    /**
     * 查询指定通道波形的负沿速率
     * @param ch 通道索引
     * @return 负沿速率测量值字符串
     */
    public String NRATEQ(int ch){
        return query(ch,MeasureManage.IMeasure.MeasureId_NegativeRate); // 查询负沿速率
    }

    /**
     * 查询指定列上的值 ：列单位为像素
     * @param chIndex 通道索引
     * @return 列值测量值字符串
     */
    public String ColValQ(int chIndex) {
        return query(chIndex, MeasureManage.IMeasure.MeasureId_ColValQ); // 查询列值
    }

    /**
     * 查询指定通道的T值（时间值）
     * @param chindex 通道索引
     * @return T值测量值字符串
     */
    public String TValueQ(int chindex){
        return query(chindex, MeasureManage.IMeasure.MeasureId_TVALUE); // 查询T值
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
//            new SCPICommandStruct(":MEASure:COUNter:SOURce","SCPI_Measure","Source"),//设置源
//            new SCPICommandStruct(":MEASure:COUNter:SOURce?","SCPI_Measure","SourceQ"),//设置源
//           new SCPICommandStruct(":MEASure:COUNter:MODE","SCPI_Measure","Counter_Mode"),
//            new SCPICommandStruct(":MEASure:COUNter:MODE?","SCPI_Measure","Counter_ModeQ"),
//            new SCPICommandStruct(":MEASure:COUNter:VALue?","SCPI_Measure","Counter_ValueQ"),//查询频率计结果
//            new SCPICommandStruct(":MEASure:ITEM","SCPI_Measure","Item"),//设置信源
//            new SCPICommandStruct(":MEASure:ITEM?","SCPI_Measure","ItemQ"),//设置信源


    /**
     * 查询指定通道波形的面积（空实现）
     * @return 固定返回0
     */
    public double AreaQ() {
        return 0; // 未实现，返回0
    }

    /**
     * 查询指定通道波形的周期面积（空实现）
     * @return 固定返回0
     */
    public double CareaQ() {
        return 0; // 未实现，返回0
    }

    /**
     * 设置指定测量项的序号
     * @param channel 通道号（从1开始）
     * @param measureIndex 测量类型ID
     * @param no 序号值
     */
    public void setListMeasureNo(int channel,int measureIndex,int no){
        Log.d(TAG, "setListMeasureNo() called with: channel = [" + channel + "], measureIndex = [" + measureIndex + "], no = [" + no + "]"); // 打印调试日志
        for (MeasureInfo info : listMeasure) { // 遍历所有测量项
            if (info.measureChannel + 1 == channel // 通道号匹配（内部从0开始，外部从1开始）
                    && info.measureType == measureIndex) { // 测量类型匹配
                info.no = no; // 设置序号
                break; // 找到后退出循环
            }
        }
    }

    /**
     * 设置指定测量项的值和有效性
     * @param channel 通道号（从1开始）
     * @param measureIndex 测量类型ID
     * @param value 测量值
     * @param isValid 值是否有效
     */
    public void setListMeasureValue(int channel, int measureIndex, double value, boolean isValid) {
        for (MeasureInfo info : listMeasure) { // 遍历所有测量项
            if (info.measureChannel + 1 == channel && info.measureType == measureIndex) { // 通道和类型匹配
                int index = listMeasure.indexOf(info); // 获取测量项在列表中的索引
                info.measureValue = value; // 更新测量值
                info.isValid = isValid; // 更新有效性标志
                listMeasure.set(index, info); // 替换列表中的测量项（触发列表更新）
                break; // 找到后退出循环
            }
        }
    }

    /**
     * 设置指定测量项的值和有效性（扩展版本，通道号从0开始）
     * @param ch 通道索引（从0开始）
     * @param type 测量类型ID
     * @param value 测量值
     * @param isValid 值是否有效
     */
    public void setListMeasureValueEx(int ch,int type,double value, boolean isValid){
        int index=-1; // 初始化索引为-1（未找到）
        for(int i=0;i<listMeasure.size();i++){ // 遍历所有测量项
            MeasureInfo info=listMeasure.get(i); // 获取当前测量项
            if (info.measureType==type && info.measureChannel==ch){ // 类型和通道都匹配
                index=i; // 记录找到的索引
                break; // 退出循环
            }
        }
        if (index==-1)return; // 未找到则直接返回
        MeasureInfo info=listMeasure.get(index); // 获取目标测量项
        info.measureValue= value; // 更新测量值
        info.isValid=isValid; // 更新有效性标志
        listMeasure.set(index,info); // 替换列表中的测量项

    }

    /**
     * 清除所有测量项
     * @param isUpdateUI 是否通知UI更新
     */
    public void ClearAllItem(boolean isUpdateUI){
        listMeasure.clear(); // 清空测量项列表
        postMeasureCountChange(false + CommandMsgToUI.PARAM_SPLIT + listMeasure.size()); // 通知测量项数量变更
        if (isUpdateUI){ // 需要通知UI更新
            MeasureInfo info=new MeasureInfo(); // 创建测量项信息（用作清除标志载体）
            info.flag= MeasureInfo.FLAG_ClearAll; // 设置清除全部标志
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,info); // 发送测量关闭事件到UI
        }
    }

    /**
     * 清除打开的测量项中的任一项或所有项
     * 对应SCPI指令: :MEASure:CLEar
     * @param index 要清除的测量项在列表中的索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Clear(int index, boolean isUpdateUI) {
        if (isChange && !isUpdateUI) { // 如果位置已变更且非UI更新调用
            isChange = false; // 重置变更标志
            return; // 直接返回（避免位置变更后误删）
        }
        if (index>= listMeasure.size()){ return;} // 索引越界则返回
        MeasureInfo info=listMeasure.get(index); // 获取要清除的测量项
        listMeasure.remove(info); // 从列表中移除
        postMeasureCountChange(false + CommandMsgToUI.PARAM_SPLIT + listMeasure.size()); // 通知测量项数量变更
        if (isUpdateUI){ // 需要通知UI更新
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,info); // 发送测量关闭事件到UI
        }
    }


    /**
     * 发送测量项数量变更通知
     * @param str 格式为"isAdd+分隔符+数量"的字符串
     */
    private void postMeasureCountChange(String str) {
        RxBus.getInstance().post(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT, str); // 发送测量项数量变更消息
    }




    /**
     * 检查指定测量项是否已存在于列表中（5参数版本，默认param6=0）
     * @param measureType 测量类型
     * @param measureChannel 测量通道
     * @param param3 扩展参数1
     * @param param4 扩展参数2
     * @param param5 扩展参数3
     * @return 是否存在
     */
    private boolean isExistMeasureInfo(int measureType, int measureChannel, int param3, int param4, int param5) {
        return isExistMeasureInfo(measureType,measureChannel,param3,param4,param5,0); // 委托给6参数版本
    }

    /**
     * 检查指定测量项是否已存在于列表中（6参数完整版本）
     * 对于延时(4)/相位(12)/ColValQ/TVALUE等特殊类型需要匹配额外参数
     * @param measureType 测量类型
     * @param measureChannel 测量通道
     * @param param3 扩展参数1
     * @param param4 扩展参数2
     * @param param5 扩展参数3
     * @param param6 双精度扩展参数
     * @return 是否存在
     */
    private boolean isExistMeasureInfo(int measureType, int measureChannel, int param3, int param4, int param5,double param6){
        for (int i=0;i<listMeasure.size();i++){ // 遍历所有测量项
            MeasureInfo info= listMeasure.get(i); // 获取当前测量项
            switch (measureType){ // 根据测量类型决定匹配条件
                case 4: // 延时测量：需要匹配通道、param1、param2、param3
                    if (info.measureType==measureType && info.measureChannel==measureChannel && info.param1==param3 && info.param2==param4 && info.param3==param5){
                        return true; // 完全匹配，已存在
                    }
                    break;
                case 12: // 相位测量
                case MeasureManage.IMeasure.MeasureId_ColValQ: // ColValQ测量：需要匹配通道和param1
                    if (info.measureType==measureType && info.measureChannel==measureChannel && info.param1==param3){
                        return true; // 匹配，已存在
                    }
                    break;
                case MeasureManage.IMeasure.MeasureId_TVALUE: { // TVALUE测量：需要匹配通道、param1和dparam1
                    if (info.measureType==measureType
                            && info.measureChannel==measureChannel
                            && info.param1 == param3 &&
                            info.dparam1 == param6){
                        return true; // 完全匹配，已存在
                    }
                }
                break;
                default: // 其他测量类型：只需匹配通道和类型
                    if (info.measureType==measureType && info.measureChannel==measureChannel){
                        return true; // 匹配，已存在
                    }
                    break;
            }
        }
        return false; // 未找到匹配项
    }

    /**
     * 打开测量项（5参数版本，默认no=-1, param6=0）
     * 如：measure:open RMS,ch1
     * @param measureType 测量类型
     * @param measureChannel 测量通道
     * @param param3 扩展参数1
     * @param param4 扩展参数2
     * @param param5 扩展参数3
     * @param isUpdateUI 是否通知UI更新
     * @return 是否成功打开
     */
    public boolean Open(int measureType, int measureChannel, int param3, int param4, int param5, boolean isUpdateUI){
        return Open(-1,measureType, measureChannel, param3, param4, param5,0, isUpdateUI); // 委托给7参数版本，no=-1表示未指定
    }

    /**
     * 打开测量项（7参数完整版本）
     * 根据测量类型创建MeasureInfo并添加到列表，不同类型需要不同的扩展参数
     * @param no 测量项序号，-1表示未指定
     * @param measureType 测量类型
     * @param measureChannel 测量通道
     * @param param3 扩展参数1
     * @param param4 扩展参数2
     * @param param5 扩展参数3
     * @param param6 双精度扩展参数
     * @param isUpdateUI 是否通知UI更新
     * @return 是否成功打开
     */
    public boolean Open(int no,int measureType, int measureChannel, int param3, int param4, int param5,double param6, boolean isUpdateUI) {

        //Log.d("Tag.Debug", String.format("Command_Measure.Open: [measureType:%d, measureChannel:%d]",measureType,measureChannel ));
        boolean flag = false; // 是否成功添加标志
        MeasureInfo info=null; // 新建的测量项信息
        if (isExistMeasureInfo(measureType, measureChannel, param3, param4, param5)) return true; // 已存在则直接返回成功
        switch (measureType) { // 根据测量类型创建不同的MeasureInfo
            case 4: { // 延时测量：需要3个额外参数（第二通道、边沿1、边沿2）
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {       break;        } // 超出最大测量项数则不添加
                info = new MeasureInfo(measureType, measureChannel, 0); // 创建延时测量项
                info.param1 = param3; // 设置第二通道
                info.param2 = param4; // 设置边沿1
                info.param3 = param5; // 设置边沿2
                listMeasure.add(info); // 添加到列表
                flag = true; // 标记成功
            }
            break;
            case 12: { // 相位测量：需要1个额外参数（第二通道）
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                } // 超出最大测量项数则不添加
                info = new MeasureInfo(measureType, measureChannel, 0); // 创建相位测量项
                info.param1 = param3; // 设置第二通道
                listMeasure.add(info); // 添加到列表
                flag = true; // 标记成功
            }
            break;
            case MeasureManage.IMeasure.MeasureId_ColValQ: { // ColValQ测量：需要1个额外参数（列位置）
//                Logger.i(TAG,"open colvalQ type:"+measureType+ " ,chan:"+measureChannel+" ,param3:"+param3);
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                } // 超出最大测量项数则不添加
                info = new MeasureInfo(measureType, measureChannel, 0); // 创建ColValQ测量项
                info.param1 = param3; // 设置列位置
                listMeasure.add(info); // 添加到列表
                flag = true; // 标记成功
//                Logger.i(TAG,"add");
            }
            break;
            case MeasureManage.IMeasure.MeasureId_TVALUE:{ // TVALUE测量：需要额外参数和双精度变量值
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) {                    break;                } // 超出最大测量项数则不添加
                MeasureInfo xinfo = null; // 已存在的TVALUE测量项
                info = null; // 新建的TVALUE测量项
                for (int i=0;i<listMeasure.size();i++){ // 遍历查找是否已有同通道的TVALUE
                    xinfo= listMeasure.get(i); // 获取当前测量项
                    if(xinfo.measureType == measureType
                        && xinfo.measureChannel == measureChannel){ // 同类型同通道
                        info = xinfo; // 使用已有的测量项
                        break; // 退出查找
                    }
                }
                if (info == null) { // 没有已有的TVALUE测量项
                    info = new MeasureInfo(measureType, measureChannel, 0); // 创建新的TVALUE测量项
                    info.param1 = param3; // 设置边沿类型
                    info.param2 = param4; // 设置光标类型
                    info.dparam1 = param6; // 设置变量值
                    listMeasure.add(info); // 添加到列表
                } else { // 已有同通道的TVALUE，更新参数
                    info.isValid = false; // 重置有效性
                    info.param1 = param3; // 更新边沿类型
                    info.param2 = param4; // 更新光标类型
                    info.dparam1 = param6; // 更新变量值
                }

                flag = true; // 标记成功
            }
            break;

            case 0: // 周期
            case 1: // 频率
            case 2: // 上升时间
            case 3: // 下降时间
            case 5: // 正占空比
            case 6: // 负占空比
            case 7: // 正脉宽
            case 8: // 负脉宽
            case 9: // 突发脉冲宽度
            case 10: // 正向超调
            case 11: // 负向超调
            case 13: // 峰峰值
            case 14: // 幅度
            case 15: // 高值
            case 16: // 低值
            case 17: // 最大值
            case 18: // 最小值
            case 19: // RMS
            case 20: // CRMS
            case 21: // 均值
            case 22: // 周期均值
            case 23: // ACRMS
            case 24: // 正沿速率
            case 25: // 负沿速率
                if (listMeasure.size() > GlobalVar.get().getMeasureItemCount()) { break; } // 超出最大测量项数则不添加
                info = new MeasureInfo(measureType, measureChannel, 0); // 创建普通测量项（无需额外参数）
                listMeasure.add(info); // 添加到列表
                flag = true; // 标记成功
                break;
        }
        if(info != null && no >= 0  && no < GlobalVar.get().getMeasureItemCount()) { // 测量项创建成功且指定了有效序号
            info.no = no; // 设置序号
        }
//        Logger.i(TAG,"isUpdateUI:"+isUpdateUI+ ",flag:"+flag);
        if (isUpdateUI && flag) { // 需要通知UI且成功添加
            Logger.i(TAG,"info:"+info.toString()); // 打印添加的测量项信息

            RxBus.getInstance().post(RxEnum.COMMANDMEASUREOPEN_TO_UI,info); // 发送测量打开事件到UI
        }

        if(flag) { // 成功添加
            postMeasureCountChange(true + CommandMsgToUI.PARAM_SPLIT + listMeasure.size()); // 通知测量项数量增加
        }
        return flag; // 返回是否成功
    }

    /**
     * 关闭测量项
     * 对应SCPI指令: :MEASure:CLOSe
     * @param type 测量类型ID
     * @param Ch 通道号（从0开始）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Close(int type, int Ch, boolean isUpdateUI) {
        MeasureInfo temInfo=null; // 被移除的测量项
        boolean flag=false; // 是否成功移除标志
        for (int i=0;i<listMeasure.size();i++){ // 遍历所有测量项
            MeasureInfo info=listMeasure.get(i); // 获取当前测量项
            //Logger.i(Command.TAG,"info:"+info.toString()+";    CH:"+Ch+",type:"+type);
            if (info.measureType==type && (info.measureChannel)== Ch){ // 类型和通道都匹配
                temInfo=info; // 记录被移除的测量项
                listMeasure.remove(info); // 从列表中移除
                flag=true; // 标记成功移除
                break; // 退出循环
            }
        }
        if(flag) { // 成功移除（此处代码块为空，预留扩展）

        }
        //Logger.i(Command.TAG,"flag:"+flag);
        if (isUpdateUI && flag) { // 需要通知UI且成功移除
            //Logger.i(Command.TAG,"clear flag:"+temInfo.flag);
            RxBus.getInstance().post(RxEnum.COMMANDMEASURECLOSE_TO_UI,temInfo); // 发送测量关闭事件到UI
        }
    }

    /**
     * 打开或关闭统计功能（空实现）
     * 对应SCPI指令: :MEASure:STATistic:DISPlay
     * @param index 统计开关索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Statistic_Display(int index, boolean isUpdateUI) {
        // 空实现，预留接口
    }

    /**
     * 查询统计功能打开或关闭（空实现）
     * 对应SCPI指令: :MEASure:STATistic:DISPlay?
     * @return 固定返回0
     */
    public double Statistic_DisplayQ() {
        return 0; // 未实现，返回0
    }

    /**
     * 清楚历史统计数据并重新统计（空实现）
     * 对应SCPI指令: :MEASure:STATistic:RESet
     * @param index 统计重置索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Statistic_Reset(int index, boolean isUpdateUI) {
        // 空实现，预留接口
    }


    private boolean AllMeasureDisplay=false; // 全部测量显示开关状态
    /**
     * 打开或关闭全部测量
     * 对应SCPI指令: :MEASure:ADISplay
     * @param allMeasureDisplay true=打开全部测量，false=关闭
     * @param isUpdateUI 是否通知UI更新
     */
    public void Adislay(boolean allMeasureDisplay, boolean isUpdateUI) {
        this.AllMeasureDisplay=allMeasureDisplay; // 更新全部测量显示状态
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_ALL_DISPLAY); // 设置消息标志为全部测量显示
            msgToUI.setParam(String.valueOf(allMeasureDisplay)); // 设置参数为开关状态字符串
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }

    }

    /**
     * 查询全部测量打开或关闭
     * 对应SCPI指令: :MEASure:ADISplay?
     * @return true=已打开，false=已关闭
     */
    public boolean AdisplayQ() {
        return this.AllMeasureDisplay; // 返回全部测量显示状态
    }

    /**
     * 设置测量范围（空实现）
     * 对应SCPI指令: :MEASure:SCOPe
     * @param index 测量范围索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Scope(int index, boolean isUpdateUI) {
        // 空实现，预留接口
    }

    /**
     * 查询测量范围（空实现）
     * 对应SCPI指令: :MEASure:SCOPe?
     * @return 固定返回0
     */
    public double ScopeQ() {
        return 0; // 未实现，返回0
    }


    private int countSource; // 计数器信源通道索引
    /**
     * 计数器设置源
     * 对应SCPI指令: :MEASure:COUNter:SOURce
     * @param index 信源通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Counter_Source(int index, boolean isUpdateUI) {
        this.countSource=index; // 更新计数器信源
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_COUNT_SOURCE); // 设置消息标志为计数器信源
            msgToUI.setParam(String.valueOf(index)); // 设置参数为信源索引字符串
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询计数器信源
     * 对应SCPI指令: :MEASure:COUNter:SOURce?
     * @return 信源通道UI编号，未启用时返回0
     */
    public int Counter_SourceQ() {
        if (FreqCounter.getInstance().isFreqCounterEnable()){ // 频率计数器已启用
            return TChan.toUiChNo(FreqCounter.getInstance().getChIdx()); // 返回频率计数器的通道UI编号
        }
        return 0; // 频率计数器未启用，返回0
    }

    private int countMode; // 计数器模式索引

    /**
     * 设置计数器模式
     * 对应SCPI指令: :MEASure:COUNter:MODE
     * @param index 计数器模式索引
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Counter_Mode(int index,boolean isUpdateUI){
        this.countMode=index; // 更新计数器模式
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MEASURE_COUNT_MODE); // 设置消息标志为计数器模式
            msgToUI.setParam(String.valueOf(index)); // 设置参数为模式索引字符串
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询计数器模式
     * 对应SCPI指令: :MEASure:COUNter:MODE?
     * @return 计数器模式索引
     */
    public  int Counter_ModeQ(){
        return countMode; // 返回计数器模式
    }

    /**
     * 查询频率计结果
     * 对应SCPI指令: :MEASure:COUNter:VALue?
     * @return 频率值字符串（带单位），无效时返回空字符串
     */
    public String Counter_ValueQ() {
        String hz=""; // 频率值字符串
        if (FreqCounter.getInstance().IsVaild()) { // 频率计数据有效
            hz = TBookUtil.getHzFromHz(FreqCounter.getInstance().getFreqVal()); // 格式化频率值（带单位）
        };
        return hz; // 返回频率值字符串
    }

    /**
     * 设置信源（空实现）
     * 对应SCPI指令: :MEASure:ITEM
     * @param index 信源索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Item(int index, boolean isUpdateUI) {
        // 空实现，预留接口
    }

    /**
     * 查询信源（空实现）
     * 对应SCPI指令: :MEASure:ITEM?
     * @return 固定返回0
     */
    public double ItemQ() {
        return 0; // 未实现，返回0
    }

    /**
     * 出厂校准：清除所有测量项后为每个打开的通道打开幅度测量
     */
    public void factoryCalibration(){
        if(MeasureManage.getInstance().getMeasureItem().getMeasureRowCount() != 1){ // 当前不止1个测量项
            ClearAllItem(true); // 清除所有测量项
            ChannelFactory.forEachCh(channel -> { // 遍历所有通道
                Open(MeasureManage.IMeasure.MeasureId_Amp, channel.getChId(), 0, 0, 0, true); // 为每个通道打开幅度测量
            });
        }
    }

    /**
     * 查询所有测量项的序号列表
     * 对应SCPI指令: :MEASure:LIST?
     * @return 逗号分隔的序号字符串（从1开始），无测量项时返回"--"
     */
    public String ListQ() {
        StringBuilder stringBuilder = new StringBuilder(); // 构建结果字符串
        listMeasure.stream().filter(measureInfo -> measureInfo.getNo() >= 0) // 过滤出已分配序号的测量项
                .sorted(Comparator.comparing(MeasureInfo::getNo)) // 按序号排序
                .forEach( m-> { // 遍历排序后的测量项
                    if(stringBuilder.length() > 0) stringBuilder.append(","); // 非首个项前加逗号
                    stringBuilder.append(m.getNo() + 1); // 序号从1开始（内部从0开始，加1转换）
                });
        if(stringBuilder.length() > 0) { // 有测量项
            return stringBuilder.toString(); // 返回逗号分隔的序号列表
        }else { // 无测量项
            return "--"; // 返回无效标记
        }
    }

    /** 测量类型ID数组，用于AddNew时按顺序尝试添加 */
    final static int [] MEASURE_ID_ARRAY ={
        MeasureManage.IMeasure.MeasureId_Period, // 周期
        MeasureManage.IMeasure.MeasureId_Freq, // 频率
        MeasureManage.IMeasure.MeasureId_RiseTime, // 上升时间
        MeasureManage.IMeasure.MeasureId_FallTime, // 下降时间
        MeasureManage.IMeasure.MeasureId_DutyAdd, // 正占空比
        MeasureManage.IMeasure.MeasureId_DutySub, // 负占空比
        MeasureManage.IMeasure.MeasureId_WidthAdd, // 正脉宽
        MeasureManage.IMeasure.MeasureId_WidthSub, // 负脉宽
        MeasureManage.IMeasure.MeasureId_BurstW, // 突发脉冲宽度
        MeasureManage.IMeasure.MeasureId_ROV, // 正向超调
        MeasureManage.IMeasure.MeasureId_FOV, // 负向超调
        MeasureManage.IMeasure.MeasureId_PKPK, // 峰峰值
        MeasureManage.IMeasure.MeasureId_Amp, // 幅度
        MeasureManage.IMeasure.MeasureId_High, // 高值
        MeasureManage.IMeasure.MeasureId_Low, // 低值
        MeasureManage.IMeasure.MeasureId_Max, // 最大值
        MeasureManage.IMeasure.MeasureId_Min, // 最小值
        MeasureManage.IMeasure.MeasureId_RMS, // RMS
        MeasureManage.IMeasure.MeasureId_CRMS, // CRMS
        MeasureManage.IMeasure.MeasureId_Mean, // 均值
        MeasureManage.IMeasure.MeasureId_CMean, // 周期均值
        MeasureManage.IMeasure.MeasureId_ACRMS, // ACRMS
        MeasureManage.IMeasure.MeasureId_PostitiveRate, // 正沿速率
        MeasureManage.IMeasure.MeasureId_NegativeRate, // 负沿速率
    };

    /**
     * 在指定序号位置添加新测量项
     * 自动从打开的通道中按MEASURE_ID_ARRAY顺序查找第一个未添加的测量类型
     * @param measureNo 测量项序号（从0开始）
     */
    public void AddNew(int measureNo){
        if(measureNo>=0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() < GlobalVar.get().getMeasureItemCount()) { // 未超出最大数量
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号是否已有测量项
            if (!opt.isPresent()) { // 该序号尚无测量项
                for(int i=ChannelFactory.CH1;i<ChannelFactory.REF_MAX;i++){ // 遍历所有通道
                    if(ChannelFactory.isChOpen(i)){ // 通道已打开
                        for(int idx:MEASURE_ID_ARRAY){ // 按优先级遍历测量类型
                            int finalI = i; // 用于lambda表达式的final变量
                            boolean exists = listMeasure.stream().anyMatch(measureInfo -> measureInfo.measureType == idx
                                    && measureInfo.measureChannel == finalI); // 检查该通道是否已有此类型测量项
                            if(!exists){ // 该通道尚无此类型测量项
                                Open(measureNo,idx,i,0,0,0,0,true); // 打开测量项
                                return; // 添加成功后返回
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除指定序号的测量项
     * @param measureNo 测量项序号（从0开始）
     */
    public void Delete(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            opt.ifPresent(measureInfo -> Close(measureInfo.measureType, measureInfo.measureChannel, true)); // 存在则关闭
        }
    }

    /**
     * 扩展测量命令：按参数打开测量项
     * 对应SCPI指令: :MEASure:XType
     * @param param SCPI参数封装对象，包含测量类型、通道、扩展参数等
     */
    public void XType(SCPIParam param){
        Open(param.iParam1,param.iParam2,param.iParam3,param.iParam4,param.iParam5,param.iParam6,param.dParam1,true); // 按参数打开测量项并通知UI
    }

    /** 测量类型SCPI名称数组，用于XTypeQ查询返回SCPI标准名称 */
    final static String [] meas_all = {
        "PERiod",//0 周期
        "FREQ", // 频率
        "RISetime", // 上升时间
        "FALLtime", // 下降时间
        "DELay", // 延时
        "PDUTy",//5 正占空比
        "NDUTy", // 负占空比
        "PWIDth", // 正脉宽
        "NWIDth", // 负脉宽
        "BURStw", // 突发脉冲宽度
        "ROVershoot",//10 正向超调
        "FOVershoot", // 负向超调
        "PHASe", // 相位
        "PKPK", // 峰峰值
        "AMPlitude", // 幅度
        "HIGH",//15 高值
        "LOW", // 低值
        "MAX", // 最大值
        "MIN", // 最小值
        "RMS", // RMS
        "CRMS",//20 CRMS
        "MEAN", // 均值
        "CMEan", // 周期均值
        "ACRMs", // ACRMS
        "PRATe", // 正沿速率
        "NRATe",//25 负沿速率
        "TVALue" // T值
    };

    /**
     * 查询指定序号测量项的类型名称
     * 对应SCPI指令: :MEASure:XType?
     * @param measureNo 测量项序号（从0开始）
     * @return SCPI标准测量类型名称，无效时返回"--"
     */
    public String XTypeQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.measureType >= 0 // 测量类型有效
                        && measureInfo.measureType < meas_all.length){ // 在名称数组范围内
                    return meas_all[measureInfo.measureType]; // 返回SCPI标准类型名称
                }
            }
        }
        return "--"; // 未找到或无效，返回无效标记
    }

    /**
     * 设置指定测量项的信源1（用于延时/相位测量的第二通道）
     * 对应SCPI指令: :MEASure:XSOURce1
     * @param measureNo 测量项序号
     * @param s 信源1通道索引
     */
    public void XSOURce1(int measureNo,int s){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){ // 延时类型
                Open(measureNo,measureInfo.measureType,s,
                        measureInfo.param1,measureInfo.param2,measureInfo.param2,0,true); // 重新打开，更新信源1
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Phase){ // 相位类型
                Open(measureNo,measureInfo.measureType,s,
                        measureInfo.param1,measureInfo.param2,0,0,true); // 重新打开，更新信源1
            }
        }
    }

    /**
     * 查询指定测量项的信源1
     * 对应SCPI指令: :MEASure:XSOURce1?
     * @param measureNo 测量项序号
     * @return 信源1的SCPI通道名称，无效时返回"--"
     */
    public String XSOURce1Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                return ToolsSCPI.getChAll(measureInfo.measureChannel); // 返回信源1的SCPI通道名称
            }
        }
        return "--"; // 未找到，返回无效标记
    }

    /**
     * 设置指定测量项的信源2（用于延时/相位测量的第二通道的边沿通道）
     * 对应SCPI指令: :MEASure:XSOURce2
     * @param measureNo 测量项序号
     * @param s 信源2通道索引
     */
    public void XSOURce2(int measureNo,int s){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){ // 延时类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        s,measureInfo.param2,measureInfo.param2,0,true); // 重新打开，更新信源2（param1）
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Phase){ // 相位类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        s,measureInfo.param2,0,0,true); // 重新打开，更新信源2（param1）
            }
        }
    }

    /**
     * 查询指定测量项的信源2
     * 对应SCPI指令: :MEASure:XSOURce2?
     * @param measureNo 测量项序号
     * @return 信源2的SCPI通道名称，无效时返回"--"
     */
    public String XSOURce2Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                switch (measureInfo.measureType){ // 根据测量类型处理
                    case MeasureManage.IMeasure.MeasureId_Delay: // 延时
                    case MeasureManage.IMeasure.MeasureId_Phase: // 相位
                        return ToolsSCPI.getChAll(measureInfo.param1); // 返回param1作为信源2的SCPI通道名称
                }
            }
        }
        return "--"; // 未找到或非延时/相位类型，返回无效标记
    }

    /**
     * 查询指定测量项的测量值
     * 对应SCPI指令: :MEASure:XVALue?
     * @param measureNo 测量项序号
     * @return 格式化的测量值字符串，无效时返回"--"
     */
    public String XVALueQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.isValid){ // 测量值有效
                    return ToolsSCPI.getDouble(measureInfo.measureValue); // 返回格式化的测量值
                }
            }
        }
        return "--"; // 未找到或值无效，返回无效标记
    }

    /**
     * 查询指定测量项的单位
     * 对应SCPI指令: :MEASure:XUnit?
     * @param measureNo 测量项序号
     * @return 测量单位字符串，无效时返回"--"
     */
    public String XUnitQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                switch (measureInfo.measureType) { // 根据测量类型返回不同单位
                    case MeasureManage.IMeasure.MeasureId_Freq: // 频率
                        return "Hz"; // 返回赫兹
                    case MeasureManage.IMeasure.MeasureId_DutyAdd: // 正占空比
                    case MeasureManage.IMeasure.MeasureId_DutySub: // 负占空比
                    case MeasureManage.IMeasure.MeasureId_ROV: // 正向超调
                    case MeasureManage.IMeasure.MeasureId_FOV: // 负向超调
                        //%不显示m、k、M等前缀，保留2位小数
                        return "%"; // 返回百分比
                    case MeasureManage.IMeasure.MeasureId_Phase: // 相位
                        return "°"; // 返回角度
                    case MeasureManage.IMeasure.MeasureId_Period: // 周期
                    case MeasureManage.IMeasure.MeasureId_RiseTime: // 上升时间
                    case MeasureManage.IMeasure.MeasureId_FallTime: // 下降时间
                    case MeasureManage.IMeasure.MeasureId_Delay: // 延时
                    case MeasureManage.IMeasure.MeasureId_WidthAdd: // 正脉宽
                    case MeasureManage.IMeasure.MeasureId_WidthSub: // 负脉宽
                    case MeasureManage.IMeasure.MeasureId_BurstW: // 突发脉冲宽度
                    case MeasureManage.IMeasure.MeasureId_TVALUE: // T值
                        return "s"; // 返回秒
                    case MeasureManage.IMeasure.MeasureId_PKPK: // 峰峰值
                    case MeasureManage.IMeasure.MeasureId_Amp: // 幅度
                    case MeasureManage.IMeasure.MeasureId_High: // 高值
                    case MeasureManage.IMeasure.MeasureId_Low: // 低值
                    case MeasureManage.IMeasure.MeasureId_Max: // 最大值
                    case MeasureManage.IMeasure.MeasureId_Min: // 最小值
                    case MeasureManage.IMeasure.MeasureId_RMS: // RMS
                    case MeasureManage.IMeasure.MeasureId_CRMS: // CRMS
                    case MeasureManage.IMeasure.MeasureId_Mean: // 均值
                    case MeasureManage.IMeasure.MeasureId_CMean: // 周期均值
                    case MeasureManage.IMeasure.MeasureId_ACRMS: // ACRMS
                        return ChannelFactory.getProbeType(measureInfo.measureChannel); // 返回通道探头类型对应的电压单位
                    case MeasureManage.IMeasure.MeasureId_PostitiveRate: // 正沿速率
                    case MeasureManage.IMeasure.MeasureId_NegativeRate: // 负沿速率
                        return ChannelFactory.getProbeType(measureInfo.measureChannel) + "/s"; // 返回电压/秒
                }
            }
        }
        return "--"; // 未找到或类型未知，返回无效标记
    }

    /**
     * 查询指定测量项的有效性
     * 对应SCPI指令: :MEASure:XValid?
     * @param measureNo 测量项序号
     * @return "ON"=有效，"OFF"=无效，"--"=未找到
     */
    public String XValidQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                return ToolsSCPI.getOpenState(measureInfo.isValid); // 返回ON/OFF状态字符串
            }
        }
        return "--"; // 未找到，返回无效标记
    }

    /**
     * 设置指定测量项的边沿1
     * 对应SCPI指令: :MEASure:XEdge1
     * 用于延时测量的边沿2(TIndex)或TVALUE测量的边沿类型
     * @param measureNo 测量项序号
     * @param e 边沿索引
     */
    public void XEdge1(int measureNo,int e){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){ // 延时类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        measureInfo.param1,e,measureInfo.param3,0,true); // 重新打开，更新边沿1（param2）
            }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // TVALUE类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,
                        e,measureInfo.param2,0,measureInfo.dparam1,true); // 重新打开，更新边沿类型（param1）
            }
        }
    }

    /** 延时边沿SCPI名称数组：上升沿1/下降沿1/上升沿2/下降沿2 */
    final static String [] delay_edge = {
        "FRISe","FFALl","LRISe","LFALl" // 上升沿1、下降沿1、上升沿2、下降沿2
    };

    /**
     * 查询指定测量项的边沿1
     * 对应SCPI指令: :MEASure:XEdge1?
     * @param measureNo 测量项序号
     * @return 边沿SCPI名称，无效时返回"--"
     */
    public String XEdge1Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay // 延时类型
                    && measureInfo.param2 >= 0 // param2有效
                        && measureInfo.param2 < delay_edge.length){ // param2在数组范围内
                    return delay_edge[measureInfo.param2]; // 返回边沿SCPI名称
                }
            }
        }
        return "--"; // 未找到或无效，返回无效标记
    }

    /**
     * 设置指定测量项的边沿2
     * 对应SCPI指令: :MEASure:XEdge2
     * 用于延时测量的边沿2(param3)更新
     * @param measureNo 测量项序号
     * @param e 边沿索引
     */
    public void XEdge2(int measureNo,int e){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay){ // 延时类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,measureInfo.param2,e,0,true); // 重新打开，更新边沿2（param3）
            }
        }
    }

    /**
     * 查询指定测量项的边沿2
     * 对应SCPI指令: :MEASure:XEdge2?
     * 延时类型返回边沿SCPI名称，TVALUE类型返回param1的整数值
     * @param measureNo 测量项序号
     * @return 边沿SCPI名称或整数值字符串，无效时返回"--"
     */
    public String XEdge2Q(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_Delay // 延时类型
                        && measureInfo.param3 >= 0 // param3有效
                        && measureInfo.param3 < delay_edge.length){ // param3在数组范围内
                    return delay_edge[measureInfo.param3]; // 返回边沿SCPI名称
                }else if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // TVALUE类型
                    return ToolsSCPI.getInt(measureInfo.param1); // 返回param1的整数值
                }
            }
        }
        return "--"; // 未找到或无效，返回无效标记
    }

    /**
     * 设置指定测量项的光标位置
     * 对应SCPI指令: :MEASure:XCURSor
     * 仅用于TVALUE类型，更新光标类型（param2）
     * @param measureNo 测量项序号
     * @param x 光标位置索引（0=NONE, 1=X1, 2=X2）
     */
    public void XCURSor(int measureNo,int x){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // TVALUE类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,x,0,measureInfo.dparam1,true); // 重新打开，更新光标类型（param2）
            }
        }
    }

    /** 光标类型SCPI名称数组 */
    final static String [] meas_cursor={
        "NONE","X1","X2" // 无光标、光标X1、光标X2
    };

    /**
     * 查询指定测量项的光标类型
     * 对应SCPI指令: :MEASure:XCURSor?
     * @param measureNo 测量项序号
     * @return 光标SCPI名称，无效时返回"--"
     */
    public String XCURSorQ(int measureNo){
        if(measureNo >= 0 && measureNo < GlobalVar.get().getMeasureItemCount() // 序号有效
                && listMeasure.size() > 0) { // 列表不为空
            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE // TVALUE类型
                        && measureInfo.param2 >= 0 // param2有效
                        && measureInfo.param2 < meas_cursor.length){ // param2在数组范围内
                    return meas_cursor[measureInfo.param2]; // 返回光标SCPI名称
                }
            }
        }
        return "--"; // 未找到或无效，返回无效标记
    }

    /**
     * 设置指定测量项的变量值
     * 对应SCPI指令: :MEASure:XVVLue
     * 仅用于TVALUE类型，更新变量值（dparam1）
     * @param measureNo 测量项序号
     * @param val 变量值
     */
    public void XVVLue(int measureNo,double val){
        Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
        if(opt.isPresent()){ // 找到测量项
            MeasureInfo measureInfo = opt.get(); // 获取测量项信息
            if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // TVALUE类型
                Open(measureNo,measureInfo.measureType,measureInfo.measureChannel,measureInfo.param1,measureInfo.param2,0,val,true); // 重新打开，更新变量值（dparam1）
            }
        }
    }

    /**
     * 查询指定测量项的变量值
     * 对应SCPI指令: :MEASure:XVVLue?
     * @param measureNo 测量项序号
     * @return 格式化的变量值字符串，无效时返回"--"
     */
    public String XVVLueQ(int measureNo){

            Optional<MeasureInfo> opt = listMeasure.stream().filter(measureInfo -> measureInfo.no == measureNo).findFirst(); // 查找该序号的测量项
            if(opt.isPresent()){ // 找到测量项
                MeasureInfo measureInfo = opt.get(); // 获取测量项信息
                if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // TVALUE类型
                    return ToolsSCPI.getDouble(measureInfo.dparam1); // 返回格式化的变量值
                }
            }

        return "--"; // 未找到或非TVALUE类型，返回无效标记
    }
}
