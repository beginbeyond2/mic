package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 水平轴（时基档位列表）
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath; // 数学运算水平轴
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef; // 参考通道水平轴
import com.micsig.tbook.tbookscope.main.maincenter.TimeBaseScale; // 时基刻度
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义

import java.util.ArrayList; // 动态数组
import java.util.List; // 列表接口

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Timebase                                     |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器时基命令处理模块                                              |
 * | 核心职责: 处理SCPI时基相关指令，包括水平时基档位设置/查询、时基显示模式       |
 * |          (YT/ROLL/XY)设置/查询、水平偏移设置/查询、缩放比例设置/查询、       |
 * |          时基档位列表查询等                                                   |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * |          时基档位设置时需根据当前激活通道（普通/REF/MATH）获取对应的           |
 * |          水平轴列表进行档位校正                                               |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, ChannelFactory,            |
 * |          HorizontalAxis, HorizontalAxisRef, HorizontalAxisMath, Tools        |
 * | 使用场景: 远程控制水平时基参数、查询时基状态时使用                           |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Timebase {
//     new SCPICommandStruct(":TIMebase:EXTent","SCPI_Timebase","Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:PLUS:EXTent","SCPI_Timebase","Plus_Extent"),//设置水平时基档位
//            new SCPICommandStruct(":TIMebase:EXTent?","SCPI_Timebase","ExtentQ"),//查询水平时基档位
//            new SCPICommandStruct(":TIMebase:MODE","SCPI_Timebase","Mode"),//设置屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:MODE?","SCPI_Timebase","ModeQ"),//查询屏幕时基显示方式
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay","SCPI_Timebase","Roll_Display"),//滚屏设置
//            new SCPICommandStruct(":TIMebase:ROLL:DISPlay?","SCPI_Timebase","Roll_DisplayQ"),//滚屏查询
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay","SCPI_Timebase","XY1_Display"),//打开或关闭通道1和通道2的XY模式显示
//            new SCPICommandStruct(":TIMebase:XY1:DISPlay?","SCPI_Timebase","XY1_DisplayQ"),//查询通道1和通道2的XY模式显示
//    //timerbase:offset 协议1.1 换名为 timerbase:position 2016.12.8
//            new SCPICommandStruct(":TIMebase:POSition","SCPI_Timebase","Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet","SCPI_Timebase","Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:OFFSet","SCPI_Timebase","Plus_Offset"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:PLUS:POSition","SCPI_Timebase","Plus_Position"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:POSition?","SCPI_Timebase","PositionQ"),//设置波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:OFFSet?","SCPI_Timebase","OffsetQ"),//查询波形显示的水平偏移
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle","SCPI_Timebase","Scale"),
//            new SCPICommandStruct(":TIMebase:ZOOm:SCAle?","SCPI_Timebase","ScaleQ"),

    private double extent; // 水平时基档位值
    /**
     * const char * tim_mode[] = {
     * "YT",
     * "ROLL",
     * "XY",
     * NULL
     * };
     */
    private int mode; // 时基显示模式（0:YT, 1:ROLL, 2:XY）
    private int xY1_Display; // XY模式显示状态
    private double position; // 水平偏移位置
    private double offset; // 水平偏移量（与position等价）
    private double scale; // 缩放比例

    /**
     * 设置水平时基档位，自动校正到最近的合法档位值
     *
     * @param chIndex     通道索引
     * @param extent      时基档位值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Extent(int chIndex, double extent, boolean isUpdateUI) {


        List<Double> list = null; // 水平轴档位列表
        int activatIdx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        if(ChannelFactory.isRefCh(activatIdx)){ // 当前激活通道为REF参考通道
            RefChannel refChannel = (RefChannel) ChannelFactory.getRefChannel(activatIdx); // 获取参考通道实例
            if(refChannel != null) { // 参考通道不为空
                HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef(); // 获取REF水平轴
                list = horizontalAxisRef.getxAxis(); // 获取REF水平轴档位列表
            }
        }else if (ChannelFactory.isMath_FFT_Ch(activatIdx)) { // 当前激活通道为MATH/FFT通道
            HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(activatIdx).getHorizontalAxisMathFFT(); // 获取MATH水平轴
            list = horizontalAxisMath.getxAxis(); // 获取MATH水平轴档位列表
        } else { // 普通通道
            list = HorizontalAxis.getInstance().getxAxis(); // 获取标准水平轴档位列表
        }
        if(list != null) { // 档位列表有效
            int n = list.size() - 1; // 列表最大索引
            int i = 0; // 遍历索引
            for (i = 0; i < n; i++) { // 遍历档位列表
                if (extent > (list.get(i) + list.get(i + 1)) / 2) { // 找到最接近的档位区间
                    break; // 跳出循环
                }
            }
            extent = list.get(i); // 校正为列表中的合法档位值
        }
        if (this.extent == extent) return; // 值未变化则直接返回
//        List<Double> axis = HorizontalAxis.getInstance().getxAxis();
//        boolean flag=false;
//        for(int i=0;i<axis.size();i++){
//           if (Math.abs(extent-axis.get(i))<1E-15 || 0==(extent-axis.get(i)) ){
//              flag=true;
//              break;
//           }
//        }
//        if (!flag) return;
        this.extent = extent; // 保存时基档位
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_EXTENT); // 设置消息标志为时基档位
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接通道和档位参数
            msgToUI.setParam(param); // 设置消息参数
//            CursorManage.getInstance().setCursorTrace(true);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
//            CursorManage.setCursorByTimebaseTrace();
//            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    /**
     * 递增/递减设置水平时基档位（空实现）
     *
     * @param index       1为增加一个档位，-1为减少一个档位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Extent(int index, boolean isUpdateUI) {
        if (index == 1) { // 增加一个档位
            //增加一个档位
        } else if (index == -1) { // 减少一个档位
            //减少一个档位
        }
        if (isUpdateUI) { // 判断是否需要更新UI
            // 暂无UI更新逻辑
        }
    }

    /**
     * 查询水平时基档位
     *
     * @return 时基档位值
     */
    public double ExtentQ() {
        return extent; // 返回时基档位
    }

    /**
     * 设置屏幕时基显示方式
     *
     * @param mode        时基模式（0:YT, 1:Roll, 2:XY）
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Mode(int mode, boolean isUpdateUI) {
//        if (this.mode == mode) return;
        this.mode = mode; // 保存时基模式
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_MODE); // 设置消息标志为时基模式
            msgToUI.setParam(String.valueOf(mode)); // 设置模式参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询屏幕时基显示方式
     *
     * @return 时基模式（0:YT, 1:Roll, 2:XY）
     */
    public int ModeQ() {
        return mode; // 返回时基模式
    }

    private boolean isRoll; // 滚屏模式标志

    /**
     * 设置滚屏显示模式
     *
     * @param isRoll      是否开启滚屏模式
     * @param isUpdateUI  是否同步更新UI界面
     */
    public  void Roll_Display(boolean isRoll,boolean isUpdateUI)
    {
            this.isRoll=isRoll; // 保存滚屏模式
            if (isUpdateUI){ // 判断是否需要更新UI
                CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
                msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_ROLL); // 设置消息标志为滚屏模式
                msgToUI.setParam(String.valueOf(isRoll)); // 设置滚屏参数
                RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
            }
    }

    /**
     * 查询滚屏显示模式
     *
     * @return 是否开启滚屏模式
     */
    public boolean Roll_DisplayQ(){
            return this.isRoll; // 返回滚屏模式
    }

    /**
     * 打开或关闭通道1和通道2的XY模式显示（空实现）
     *
     * @param index       XY模式开关索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void XY1_Display(int index, boolean isUpdateUI) {
    }

    /**
     * 查询通道1和通道2的XY模式显示（空实现）
     *
     * @return 默认返回0
     */
    public int XY1_DisplayQ() {
        return 0; // 空实现，返回0
    }

    /**
     * 设置波形显示的水平偏移（Position方式）
     *
     * @param position    水平偏移值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Position(double position, boolean isUpdateUI) {
//        if (this.position == position) return;
        this.position = position; // 保存水平偏移
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_POSITION); // 设置消息标志为水平偏移
            msgToUI.setParam(String.valueOf(position)); // 设置偏移参数
//            CursorManage.getInstance().setCursorTrace(true);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
//            CursorManage.setCursorByTimebaseTrace();
//            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    /**
     * 设置波形显示的水平偏移（Offset方式，委托给Position）
     *
     * @param offset      水平偏移值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Offset(double offset, boolean isUpdateUI) {
        Position(offset,isUpdateUI); // 委托给Position方法处理
//        if (this.offset == offset) return;
//        this.offset = offset;
//        if (isUpdateUI) {
//        }
    }

    /**
     * 递增/递减设置水平偏移（Offset方式，委托给Plus_Position）
     *
     * @param index       1为加一个单位，-1为减一个单位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Offset(int index, boolean isUpdateUI) {
        Plus_Position(index,isUpdateUI); // 委托给Plus_Position方法处理
//        if (index == 1) {
//
//        } else if (index == -1) {
//
//        }
//        if (isUpdateUI) {
//        }
    }

    /**
     * 递增/递减设置水平偏移（Position方式，空实现）
     *
     * @param index       1为加一个单位，-1为减一个单位
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Position(int index, boolean isUpdateUI) {
        if (index == 1) { // 加一个单位
            // 递增逻辑待实现
        } else if (index == -1) { // 减一个单位
            // 递减逻辑待实现
        }
        if (isUpdateUI) { // 判断是否需要更新UI
            // 暂无UI更新逻辑
        }
    }

    /**
     * 查询波形显示的水平偏移（Position方式）
     *
     * @return 水平偏移值
     */
    public double PositionQ() {
        return position; // 返回水平偏移
    }

    /**
     * 查询波形显示的水平偏移（Offset方式，委托给PositionQ）
     *
     * @return 水平偏移值
     */
    public double OffsetQ() {
        return PositionQ(); // 委托给PositionQ返回偏移值
        //return offset;
    }

    /**
     * 设置缩放比例（Zoom模式），根据当前时基档位限制缩放范围
     *
     * @param chIndex     通道索引
     * @param d           缩放比例值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Scale(int chIndex, double d, boolean isUpdateUI) {
        List<Double> axis = HorizontalAxis.getInstance().getxAxis(); // 获取标准水平轴档位列表
        List<Double> newAxis=new ArrayList<>(); // 创建合法缩放档位列表
        for(int i=0;i<axis.size();i++){ // 遍历档位列表
            if ( ((extent-axis.get(i))>1E-15 || Double.compare(extent,axis.get(i))==0) && Double.compare(extent,d)>=0 ){ // 筛选<=当前时基且<=目标值的档位
                newAxis.add(axis.get(i)); // 添加到合法列表
            }
        }

        //最大值的左边界
        if(newAxis.size()==0){ // 没有合法缩放档位
            d=extent; // 使用当前时基作为缩放值
        }
        //最小值的右边界
        if(newAxis.size()>0 && Double.compare(d, newAxis.get(newAxis.size()-1))<0){ // 目标值小于最小合法档位
            d=newAxis.get(newAxis.size()-1); // 限制为最小合法档位
        }
        double finalD = d; // 最终缩放值（用于lambda表达式）
        int index= Tools.indexOf(axis, t->Double.compare(finalD,t)==0); // 查找缩放值在列表中的索引
//        Logger.i(Command.TAG,"d:"+d+",extent:"+extent+",index:"+index);
//        Logger.i(Command.TAG, "list:"+Arrays.toString(newAxis.toArray()));
        if (index==-1)return; // 缩放值不在合法列表中，直接返回

        this.scale = d; // 保存缩放比例
        if (isUpdateUI){ // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_TIMEBASE_ZOOM_SCALE); // 设置消息标志为缩放比例
            String param = String.valueOf(chIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(scale); // 拼接通道和缩放参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询缩放比例
     *
     * @return 缩放比例值
     */
    public double ScaleQ(){
        return scale; // 返回缩放比例
    }

    /**
     * 查询时基档位列表，以逗号分隔的字符串返回
     *
     * @return 时基档位列表字符串，如 "1E-9,2E-9,5E-9,..."
     */
    public String ListQ(){
        List<Double> axis= HorizontalAxis.getInstance().getxAxis(); // 获取标准水平轴档位列表
        StringBuilder sb=new StringBuilder(); // 创建字符串构建器
        for(int i=0;i<axis.size();i++){ // 遍历档位列表
            sb.append(axis.get(i).toString()); // 追加档位值字符串
            if (i!= axis.size()-1) { // 非最后一个元素
                sb.append(","); // 追加逗号分隔符
            }
        }
//        Log.d("Tag.Debug", String.format("Command_Timebase.ListQ: %s ,len:%s",axis.size(),sb.toString().length() ));

        return sb.toString(); // 返回档位列表字符串
    }

}
