package com.micsig.tbook.tbookscope.scpi; // 示波器SCPI命令解析包

import com.micsig.tbook.scope.Sample.MemDepthFactory; // 导入存储深度工厂，获取存储深度选项和当前值
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件

import java.util.Arrays; // 导入数组工具，用于存储深度列表格式化

/**
 * +---------------------------------------------------------------------------+
 * | 模块定位：示波器SCPI命令层 - 采样控制子模块                                 |
 * | 核心职责：处理SCPI协议中:SAMPle和:ACQuire相关命令的设置与查询                 |
 * | 架构设计：静态方法类，委托Command中间件的Sample接口和MemDepthFactory执行操作  |
 * | 数据流向：SCPIParam → 本类静态方法 → Command.get().getSample()/MemDepthFactory|
 * | 依赖关系：SCPIParam、Command、MemDepthFactory、ToolsSCPI                   |
 * | 使用场景：远程设置/查询采样方式、平均次数、存储深度、分段存储参数等            |
 * +---------------------------------------------------------------------------+
 *
 * Created by liwb on 2018/1/12.
 */

public class SCPI_Sample {
//    //采样命令 SAMP
//            new SCPICommandStruct(":SAMPle:TYPE","SCPI_Sample","Type"),//设置采样方式
//            new SCPICommandStruct(":SAMPle:TYPE?","SCPI_Sample","TypeQ"),//查询采样方式
//            new SCPICommandStruct(":SAMPle:MEAN","SCPI_Sample","Mean"),//设置平均采样次数。所设置的值为2的整数倍数。
//            new SCPICommandStruct(":SAMPle:MEAN?","SCPI_Sample","MeanQ"),//查询平均采样次数
//            new SCPICommandStruct(":SAMPle:ENVelop","SCPI_Sample","Envelop"),//设置包络采样次数。所设置的值为2的整数倍数或无穷
//            new SCPICommandStruct(":SAMPle:ENVelop?","SCPI_Sample","EnvelopQ"),//查询包络采样次数
//            new SCPICommandStruct(":SAMPle:SEGMented","SCPI_Sample","SegMented"),//设置分段存储的段数
//            new SCPICommandStruct(":SAMPle:SRATe?","SCPI_Sample","SrateQ"),//查询当前的采样率
//            new SCPICommandStruct(":ACQuire:MDEPth","SCPI_Sample","Mdepth"),//设置当前存储尝试
//            new SCPICommandStruct(":SAMPle:MDEPth?","SCPI_Sample","MdepthQ"),//查询示波器当前存储深度

    /**
     * 设置采样方式（普通/平均/包络/分段）
     * @param param SCPI参数封装，iParam1为采样方式索引
     */
    public static void Type(SCPIParam param) {
        Command.get().getSample().Type(param.iParam1, true); // 委托Command中间件设置采样方式，true表示通知UI刷新
    }

    /**
     * 查询当前采样方式
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 采样方式名称字符串，如"NORMal"
     */
    public static String TypeQ(SCPIParam param) {
        int i= Command.get().getSample().TypeQ(); // 从Command中间件获取采样方式索引
        return ToolsSCPI.getSampleType(i); // 将索引转换为采样方式名称字符串
    }

    /**
     * 设置平均采样次数
     * @param param SCPI参数封装，iParam1为平均次数索引
     */
    public static void Mean(SCPIParam param) {
        Command.get().getSample().Mean(param.iParam1, true); // 委托Command中间件设置平均次数，true表示通知UI刷新
    }

    /**
     * 查询平均采样次数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 平均次数名称字符串
     */
    public static String MeanQ(SCPIParam param) {
        int i= Command.get().getSample().MeanQ(); // 从Command中间件获取平均次数索引
        return ToolsSCPI.getSampleEnvelop(i); // 将索引转换为平均次数名称字符串
    }

    /**
     * 设置包络采样次数
     * @param param SCPI参数封装，iParam1为包络次数索引
     */
    public static void Envelop(SCPIParam param) {
        Command.get().getSample().Envelop(param.iParam1, true); // 委托Command中间件设置包络次数，true表示通知UI刷新
    }

    /**
     * 查询包络采样次数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 包络次数名称字符串
     */
    public static String EnvelopQ(SCPIParam param) {
        int i= Command.get().getSample().EnvelopQ(); // 从Command中间件获取包络次数索引
        return ToolsSCPI.getSampleEnvelop(i); // 将索引转换为包络次数名称字符串
    }

    /**
     * 设置分段存储功能的开关
     * @param param SCPI参数封装，bParam1为true打开/false关闭
     */
    public static void SegMented(SCPIParam param) {
        Command.get().getSample().SegMented(param.bParam1, true); // 委托Command中间件设置分段存储开关，true表示通知UI刷新
    }

    /**
     * 查询分段存储功能的开关状态
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "ON"或"OFF"
     */
    public static String SegMentedQ(SCPIParam param){
        boolean i=Command.get().getSample().SegMentedQ(); // 从Command中间件获取分段存储开关状态
        return ToolsSCPI.getOpenState(i); // 将布尔值转换为"ON"/"OFF"字符串
    }

    //查询已存在段数
    /**
     * 查询已采集的分段存储段数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 已采集段数的字符串表示
     */
    public static String SegmentedNoQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedNoQ(); // 从Command中间件获取已采集段数
        return String.valueOf(i); // 将段数转换为字符串
    }

    //设置段数
    /**
     * 设置分段存储的最大段数
     * @param param SCPI参数封装，iParam1为段数
     */
    public static void SegmentedQTY(SCPIParam param){
        Command.get().getSample().SegmentedQTY(param.iParam1,true); // 委托Command中间件设置段数，true表示通知UI刷新
    }

    //查询段数
    /**
     * 查询分段存储的最大段数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 最大段数的字符串表示
     */
    public static String SegmentedQTYQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedQTYQ(); // 从Command中间件获取最大段数
        return String.valueOf(i); // 将段数转换为字符串
    }

    /**
     * 查询分段存储是否支持10000段（固定返回OFF）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "OFF"
     */
    public static String SegmentedIs10000Q(SCPIParam param){
        return ToolsSCPI.getOpenState(false); // 固定返回OFF，表示不支持10000段
    }

    /**
     * 查询分段存储的最大段数限制
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 最大段数限制的字符串表示
     */
    public static String SegmentedMaxQ(SCPIParam param){
        int max=Command.get().getSample().SegmentedMaxQ(); // 从Command中间件获取最大段数限制
        return String.valueOf(max); // 将最大值转换为字符串
    }

    //设置显示类型
    /**
     * 设置分段存储的显示类型（如缩略图/列表等）
     * @param param SCPI参数封装，iParam1为显示类型索引
     */
    public static void SegmentedDisplayType(SCPIParam param){
        Command.get().getSample().SegmentedDisplayType(param.iParam1,true); // 委托Command中间件设置显示类型，true表示通知UI刷新
    }

    //查询显示类型
    /**
     * 查询分段存储的显示类型
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 显示类型名称字符串
     */
    public static String SegmentedDisplayTypeQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedDisplayTypeQ(); // 从Command中间件获取显示类型索引
        return ToolsSCPI.getSegmentDisplayType(i); // 将索引转换为显示类型名称字符串
    }

    /**
     * 设置分段存储的排列顺序
     * @param param SCPI参数封装，iParam1为排列顺序索引
     */
    public static void SegmentedOrder(SCPIParam param){
        Command.get().getSample().SegmentedOrder(param.iParam1,true); // 委托Command中间件设置排列顺序，true表示通知UI刷新
    }

    /**
     * 查询分段存储的排列顺序
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 排列顺序名称字符串
     */
    public static String SegmentedOrderQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedOrderQ(); // 从Command中间件获取排列顺序索引
        return ToolsSCPI.getSegmentOrder(i); // 将索引转换为排列顺序名称字符串
    }

    /**
     * 开始分段存储回放播放
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void SegmentedPlay(SCPIParam param){
        Command.get().getSample().SegmentedPlay(true); // 委托Command中间件开始分段播放，true表示通知UI刷新
    }

    /**
     * 停止分段存储回放播放
     * @param param SCPI参数封装（本方法未使用参数内容）
     */
    public static void SegmentedStop(SCPIParam param){
        Command.get().getSample().SegmentedStop(true); // 委托Command中间件停止分段播放，true表示通知UI刷新
    }

    /**
     * 设置分段存储FRA1参数
     * @param param SCPI参数封装，iParam1为FRA1值
     */
    public static void SegmentedFra1(SCPIParam param){
        Command.get().getSample().SegmentedFra1(param.iParam1,true); // 委托Command中间件设置FRA1，true表示通知UI刷新
    }

    /**
     * 查询分段存储FRA1参数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return FRA1值的字符串表示
     */
    public static String SegmentedFra1Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra1Q(); // 从Command中间件获取FRA1值
        return String.valueOf(i); // 将FRA1转换为字符串
    }

    /**
     * 设置分段存储FRA2参数
     * @param param SCPI参数封装，iParam1为FRA2值
     */
    public static void SegmentedFra2(SCPIParam param){
        Command.get().getSample().SegmentedFra2(param.iParam1,true); // 委托Command中间件设置FRA2，true表示通知UI刷新
    }

    /**
     * 查询分段存储FRA2参数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return FRA2值的字符串表示
     */
    public static String SegmentedFra2Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra2Q(); // 从Command中间件获取FRA2值
        return String.valueOf(i); // 将FRA2转换为字符串
    }

    /**
     * 设置分段存储FRA3参数
     * @param param SCPI参数封装，iParam1为FRA3值
     */
    public static void SegmentedFra3(SCPIParam param){
        Command.get().getSample().SegmentedFra3(param.iParam1,true); // 委托Command中间件设置FRA3，true表示通知UI刷新
    }

    /**
     * 查询分段存储FRA3参数
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return FRA3值的字符串表示
     */
    public static String SegmentedFra3Q(SCPIParam param){
        int i=Command.get().getSample().SegmentedFra3Q(); // 从Command中间件获取FRA3值
        return String.valueOf(i); // 将FRA3转换为字符串
    }

    /**
     * 设置分段存储播放速度
     * @param param SCPI参数封装，iParam1为播放速度索引
     */
    public static void SegmentedPlaySpeed(SCPIParam param){
        Command.get().getSample().SegmentedPlaySpeed(param.iParam1,true); // 委托Command中间件设置播放速度，true表示通知UI刷新
    }

    /**
     * 查询分段存储播放速度
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 播放速度名称字符串
     */
    public static String SegmentedPlaySpeedQ(SCPIParam param){
        int i=Command.get().getSample().SegmentedPlaySpeedQ(); // 从Command中间件获取播放速度索引
        return ToolsSCPI.getSegmentDisplaySpeed(i); // 将索引转换为播放速度名称字符串
    }


    /**
     * 查询当前采样率
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 采样率的字符串表示
     */
    public static String SrateQ(SCPIParam param) {
        double d=Command.get().getSample().SrateQ(); // 从Command中间件获取当前采样率
        return String.valueOf(d); // 将采样率转换为字符串
    }

    /**
     * 设置存储深度（通过名称选择）
     * @param param SCPI参数封装，sParam1为存储深度名称
     */
    public static void MdepthSelect(SCPIParam param){
        Command.get().getSample().Mdepth(param.sParam1, true); // 委托Command中间件设置存储深度，true表示通知UI刷新
    }

    /**
     * 查询当前存储深度选择（AUTO或具体值）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return "AUTO"或存储深度值的字符串
     */
    public static String MdepthSelectQ(SCPIParam param) {
        int v = Command.get().getSample().MdepthQ(); // 从Command中间件获取存储深度值
        if (v==0){ // 值为0表示自动模式
            return "AUTO"; // 返回AUTO标识
        }else {
            return String.valueOf(v); // 返回具体存储深度值
        }
    }

    /**
     * 设置存储深度（通过索引选择，与MdepthSelect相同实现）
     * @param param SCPI参数封装，sParam1为存储深度名称
     */
    public static void MdepthSelectIndex(SCPIParam param){
        Command.get().getSample().Mdepth(param.sParam1, true); // 委托Command中间件设置存储深度，true表示通知UI刷新
    }

    /**
     * 查询当前实际存储深度（采样点数）
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 实际存储深度的字符串表示
     */
    public static String MdepthQ(SCPIParam param) {
        int i= MemDepthFactory.getSampleMemDepth(); // 从MemDepthFactory获取实际采样存储深度
        return String.valueOf(i); // 将存储深度转换为字符串
    }

    /**
     * 查询可选存储深度列表
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 存储深度选项的逗号分隔字符串
     */
    public static String RangeQ(SCPIParam param){
        String s= Arrays.toString( MemDepthFactory.getMemDepth().getMemDepthItemName().toArray()); // 获取存储深度选项列表并转为字符串
        return s.replace("[","").replace("]",""); // 去除数组格式的方括号
    }

    /**
     * 查询默认存储深度的初始化名称
     * @param param SCPI参数封装（本方法未使用参数内容）
     * @return 默认存储深度名称字符串
     */
    public static String InitQ(SCPIParam param){
        String s = MemDepthFactory.getMemDepthObj(MemDepthFactory.getDefaultMemDepth()).getMemDepthInitName(); // 获取默认存储深度的初始化名称
        return s; // 返回初始化名称
    }
}
