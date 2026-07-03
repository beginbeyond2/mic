package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.Sample.MemDepthFactory; // 存储深度工厂
import com.micsig.tbook.scope.Sample.SegmentSample; // 分段采样管理
import com.micsig.tbook.scope.Scope; // 示波器核心作用域
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.IChannel; // 通道接口
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义

import java.util.HashMap; // 哈希映射

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                           Command_Sample                                    |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器采样命令处理模块                                             |
 * | 核心职责: 处理SCPI采样相关指令，包括采样方式设置/查询、平均/包络采样次数、    |
 * |          分段存储开关/段数/显示类型/顺序/播放/帧操作、采样率/存储深度查询等   |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层;                        |
 * |          查询时从Scope/SegmentSample/ChannelFactory/MemDepthFactory读取数据   |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, Scope, SegmentSample,      |
 * |           ChannelFactory, MemDepthFactory                                    |
 * | 使用场景: 远程控制采样参数、查询采样状态时使用                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Sample {
//    //采样命令 SAMP
//            new SCPICommandStruct(":ACQuire:TYPE","SCPI_Sample","Type(){}
//            new SCPICommandStruct(":ACQuire:TYPE?","SCPI_Sample","TypeQ(){}
//            new SCPICommandStruct(":ACQuire:MEAN","SCPI_Sample","Mean(){}
//            new SCPICommandStruct(":ACQuire:MEAN?","SCPI_Sample","MeanQ(){}
//            new SCPICommandStruct(":ACQuire:ENVelop","SCPI_Sample","Envelop(){}
//            new SCPICommandStruct(":ACQuire:ENVelop?","SCPI_Sample","EnvelopQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented","SCPI_Sample","SegMented(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:NO?","SCPI_Sample","SegmentedNoQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:QTY","SCPI_Sample","SegmentedQTY(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:QTY?","SCPI_Sample","SegmentedQTYQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:DISType","SCPI_Sample","SegmentedDisplayType(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:DISType?","SCPI_Sample","SegmentedDisplayTypeQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:ORDer","SCPI_Sample","SegmentedOrder(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:ORDer?","SCPI_Sample","SegmentedOrderQ(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY","SCPI_Sample","SegmentedPlay(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:STOP","SCPI_Sample","SegmentedStop(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA1","SCPI_Sample","SegmentedFra1(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA1?","SCPI_Sample","SegmentedFra1Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA2","SCPI_Sample","SegmentedFra2(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA2?","SCPI_Sample","SegmentedFra2Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA3","SCPI_Sample","SegmentedFra3(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:FRA3?","SCPI_Sample","SegmentedFra3Q(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED","SCPI_Sample","SegmentedPlaySpeed(){}
//            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED?","SCPI_Sample","SegMentedPlaySpeedQ(){}
//            new SCPICommandStruct(":ACQuire:SRATe?","SCPI_Sample","SrateQ(){}
//            new SCPICommandStruct(":ACQuire:MDEPth","SCPI_Sample","Mdepth"),//设置当前存储尝试
//            new SCPICommandStruct(":ACQuire:MDEPth?","SCPI_Sample","MdepthQ(){}

    /**
     * const char * samp_type[] = {
     * "NORMal",
     * "MEAN",
     * "ENVelop",
     * "PEAK",
     * NULL
     * };
     */
    private int type; // 采样方式索引（NORMal/MEAN/ENVelop/PEAK）
    /**
     * const char * samp_mean_num[] = {
     * "2","4", "8", "16", "32", "64", "128", "256"
     * , NULL
     * };
     */
    private int meanIndex; // 平均采样次数索引
    /**
     * const char * samp_env_num[] = {
     * "2","4", "8", "16", "32", "64", "128", "256", "inf"
     * , NULL
     * };
     */
    private int envelopIndex; // 包络采样次数索引
    /**
     * 是否开启分段存储
     */
    private boolean isOpenSegMent; // 分段存储是否开启

    /**
     * 分段存储总段数
     */
    private int segmentedCount; // 分段存储总段数

    /**
     * true:单帧  false:拟合
     */
    private int segmentedDisplayType; // 分段显示类型（单帧/拟合）
    /**
     * true:顺序  false:倒序
     */
    private int segmentedOrder; // 分段显示顺序（顺序/倒序）
    private int frame1; // 帧1索引
    private int frame2; // 帧2索引
    private int frame3; // 帧3索引
    private int playSpeed; // 播放速度

    /**
     * 设置采样方式
     *
     * @param type        采样方式索引（0:NORMal, 1:MEAN, 2:ENVelop, 3:PEAK）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Type(int type, boolean isUpdateUI) {
//        if (this.type == type) return;
        this.type = type; // 保存采样方式
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_TYPE); // 设置消息标志为采样方式
            msgToUI.setParam(String.valueOf(type)); // 设置采样方式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询采样方式
     * /**
     * const char * samp_type[] = {
     * "NORMal",
     * "PEAK",
     * "MEAN",
     * "ENVelop",
     * NULL
     * };
     *
     * @return 采样方式索引
     */
    public int TypeQ() {
        return type; // 返回采样方式索引
    }

    /**
     * 设置平均采样次数。所设置的值为2的整数倍数。
     *
     * @param meanIndex   平均采样次数索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Mean(int meanIndex, boolean isUpdateUI) {
//        if (this.meanIndex == meanIndex) return;
        this.meanIndex = meanIndex; // 保存平均采样次数索引
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_MEAN); // 设置消息标志为平均采样
            msgToUI.setParam(String.valueOf(meanIndex)); // 设置平均采样次数参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询平均采样次数
     *
     * @return 平均采样次数索引
     */
    public int MeanQ() {
        return meanIndex; // 返回平均采样次数索引
    }

    /**
     * 设置包络采样次数。所设置的值为2的整数倍数或无穷
     *
     * @param envelopIndex 包络采样次数索引
     * @param isUpdateUI   是否同步更新UI界面
     */
    public void Envelop(int envelopIndex, boolean isUpdateUI) {
        if (this.envelopIndex == envelopIndex) return; // 值未变化则直接返回
        this.envelopIndex = envelopIndex; // 保存包络采样次数索引
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_ENVELOP); // 设置消息标志为包络采样
            msgToUI.setParam(String.valueOf(envelopIndex)); // 设置包络采样次数参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询包络采样次数
     *
     * @return 包络采样次数索引
     */
    public int EnvelopQ() {
        return envelopIndex; // 返回包络采样次数索引
    }

    /**
     * 设置分段存储的开关
     *
     * @param isOpen      是否开启分段存储
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegMented(boolean isOpen, boolean isUpdateUI) {
        this.isOpenSegMent=isOpen; // 保存分段存储开关状态
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_IsOpenSegMent); // 设置消息标志为分段存储开关
            msgToUI.setParam(String.valueOf(isOpenSegMent)); // 设置开关状态参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段存储是否开启
     *
     * @return 分段存储是否开启
     */
    public boolean SegMentedQ(){
        return this.isOpenSegMent; // 返回分段存储开关状态
    }

    /**
     * 查询已存在的分段存储段数
     *
     * @return 已存储的段数
     */
    //查询已存在段数
     public int SegmentedNoQ(){
         SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段采样单例
         int count=0; // 段数计数
         if (Scope.getInstance().isRun()) { // 示波器正在运行
             int nums = segmentSample.getSegmentNums(); // 获取最大段数
             if (nums > segmentSample.getMaxSegmentNums()) // 超过最大段数
                 nums = segmentSample.getMaxSegmentNums(); // 限制为最大段数
//             strTxt = "" + segmentSample.getSegmentFrames() + "/" + nums;
             count=segmentSample.getSegmentFrames(); // 获取已采集帧数
         } else { // 示波器已停止
             Scope scope = Scope.getInstance(); // 获取Scope单例
             int nums = scope.getSegmentFrameNums(); // 获取总分段帧数
             int val = (scope.getSegmentFrameNo() + 1); // 当前帧号+1
             if (SegmentSample.getInstance().getSegmentDisplayType()==SegmentSample.SEGMENT_DISPLAY_FITTING){ // 拟合显示模式
                 val = scope.getSegmentFrameNo() ; // 当前帧号（拟合模式不加1）
             }
             if (val>nums) val=nums; // 超过总帧数则限制
             if (nums <= 0) val = 0; // 总帧数为0则当前帧号也为0
//             strTxt = "" + val + "/" + nums;
             count=nums; // 返回总帧数
         }
         return count; // 返回段数
     }

    /**
     * 设置分段存储的段数
     *
     * @param count       段数
     * @param isUpdateUI  是否同步更新UI界面
     */
    //设置段数
     public void SegmentedQTY(int count,boolean isUpdateUI){
        this.segmentedCount=count; // 保存段数
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedQTY); // 设置消息标志为分段段数
            msgToUI.setParam(String.valueOf(count)); // 设置段数参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }

     }

    /**
     * 查询分段存储的段数
     *
     * @return 段数
     */
    //查询段数
     public int SegmentedQTYQ(){
        return this.segmentedCount; // 返回段数
     }

     /**
      * 查询分段存储的最大段数
      *
      * @return 最大段数
      */
    public int SegmentedMaxQ(){
        return SegmentSample.getInstance().getMaxSegmentNums(); // 返回最大段数
    }

    /**
     * 设置分段显示类型
     *
     * @param displayIndex 显示类型索引
     * @param isUpdateUI   是否同步更新UI界面
     */
    //设置显示类型
    public void SegmentedDisplayType(int displayIndex,boolean isUpdateUI){
        this.segmentedDisplayType=displayIndex; // 保存显示类型索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedDisplayType); // 设置消息标志为分段显示类型
            msgToUI.setParam(String.valueOf(displayIndex)); // 设置显示类型参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段显示类型
     *
     * @return 显示类型索引
     */
    //查询显示类型
    public int SegmentedDisplayTypeQ(){
        return this.segmentedDisplayType; // 返回显示类型索引
    }

    /**
     * 设置分段显示顺序
     *
     * @param orderIndex  顺序索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedOrder(int orderIndex,boolean isUpdateUI){
        this.segmentedOrder=orderIndex; // 保存显示顺序索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedOrder); // 设置消息标志为分段显示顺序
            msgToUI.setParam(String.valueOf(orderIndex)); // 设置顺序参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段显示顺序
     *
     * @return 顺序索引
     */
    public int SegmentedOrderQ(){
        return this.segmentedOrder; // 返回显示顺序索引
    }

    /**
     * 开始分段存储播放
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedPlay(boolean isUpdateUI){
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedPlay); // 设置消息标志为分段播放
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 停止分段存储播放
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedStop(boolean isUpdateUI){
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedStop); // 设置消息标志为分段停止
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 设置分段帧1的索引
     *
     * @param frame       帧索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedFra1(int frame,boolean isUpdateUI){
        int max= Scope.getInstance().getSegmentFrameNums(); // 获取最大帧数
        int min=1; // 最小帧号
        if (frame<min) frame=min; // 低于最小值则限制为1
        if (frame>max) frame=max; // 超过最大值则限制为最大帧数
        this.frame1=frame; // 保存帧1索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra1); // 设置消息标志为帧1
            msgToUI.setParam(String.valueOf(frame)); // 设置帧索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段帧1的索引
     *
     * @return 帧1索引
     */
    public int SegmentedFra1Q(){
        return this.frame1; // 返回帧1索引
    }

    /**
     * 设置分段帧2的索引
     *
     * @param frame       帧索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedFra2(int frame,boolean isUpdateUI){
        this.frame2=frame; // 保存帧2索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra2); // 设置消息标志为帧2
            msgToUI.setParam(String.valueOf(frame)); // 设置帧索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段帧2的索引
     *
     * @return 帧2索引
     */
    public int SegmentedFra2Q(){
        return this.frame2; // 返回帧2索引
    }

    /**
     * 设置分段帧3的索引
     *
     * @param frame       帧索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedFra3(int frame,boolean isUpdateUI){
        this.frame3=frame; // 保存帧3索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedFra3); // 设置消息标志为帧3
            msgToUI.setParam(String.valueOf(frame)); // 设置帧索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段帧3的索引
     *
     * @return 帧3索引
     */
    public int SegmentedFra3Q(){
        return this.frame3; // 返回帧3索引
    }

    /**
     * 设置分段播放速度
     *
     * @param speed       播放速度索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void SegmentedPlaySpeed(int speed,boolean isUpdateUI){
        this.playSpeed=speed; // 保存播放速度
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_SAMPLE_SegmentedPlaySpeed); // 设置消息标志为分段播放速度
            msgToUI.setParam(String.valueOf(speed)); // 设置速度参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询分段播放速度
     *
     * @return 播放速度索引
     */
    public int SegmentedPlaySpeedQ(){
        return this.playSpeed; // 返回播放速度
    }

    /**
     * 查询当前的采样率
     *
     * @return 采样率（Hz）
     */
    public double SrateQ() {
        double dx=0; // 采样率初始值
        IChannel channel = ChannelFactory.getInstance().getWaveChannel(); // 获取波形通道
        if (channel != null) { // 通道存在
            dx = channel.getSampleRate2display(); // 获取显示用采样率
        }
        return dx; // 返回采样率
    }

    private int mDepth=0; // 存储深度索引
    private HashMap<Integer,Integer> mapDepth=new HashMap(); // 存储深度映射表

    /**
     * 通过字符串设置存储深度
     *
     * @param memdepth    存储深度字符串
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Mdepth(String  memdepth,boolean isUpdateUI){
        Log.d("xxxxxx", "Mdepth() called with: memdepth = [" + memdepth + "], isUpdateUI = [" + isUpdateUI + "]"); // 调试日志

        int index = MemDepthFactory.getMemDepth().memDepth2menuIdx(memdepth.trim()); // 将存储深度字符串转为菜单索引
        if(index >= 0){ // 索引有效
            Mdepth(index,isUpdateUI); // 调用整型索引方法设置存储深度
        }
    }

    /**
     * 通过索引设置存储深度
     *
     * @param index       存储深度菜单索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Mdepth(int  index,boolean isUpdateUI){
//        if (index >= 6) index -= 5;
        if (mapDepth.size()==0){ // 映射表为空则初始化
            mapDepth.put(0,0); // 索引0→0
            mapDepth.put(1,1); // 索引1→1
            mapDepth.put(2,2); // 索引2→2
            mapDepth.put(3,3); // 索引3→3
            mapDepth.put(4,4); // 索引4→4
            mapDepth.put(5,5); // 索引5→5
            mapDepth.put(6,1); // 索引6→1（映射）
            mapDepth.put(7,2); // 索引7→2（映射）
            mapDepth.put(8,3); // 索引8→3（映射）
            mapDepth.put(9,4); // 索引9→4（映射）
            mapDepth.put(10,5); // 索引10→5（映射）
            mapDepth.put(11,1); // 索引11→1（映射）
            mapDepth.put(12,2); // 索引12→2（映射）
            mapDepth.put(13,3); // 索引13→3（映射）
            mapDepth.put(14,4); // 索引14→4（映射）
            mapDepth.put(15,5); // 索引15→5（映射）
        }
        index=mapDepth.get(index); // 通过映射表转换索引
        this.mDepth=index; // 保存存储深度索引
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_LENGTH); // 设置消息标志为存储深度
            msgToUI.setParam(String.valueOf(index)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询示波器当前存储深度
     *
     * @return 存储深度索引
     */
    public int MdepthQ() {
       if (mDepth==0){ // 索引为0
           return mDepth; // 返回0
       }else { // 索引非0
           return MemDepthFactory.getSampleMemDepth(); // 从工厂获取实际存储深度
       }
    }
}
