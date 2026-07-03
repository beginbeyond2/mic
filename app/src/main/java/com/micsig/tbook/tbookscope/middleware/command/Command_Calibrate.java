package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import android.util.Log; // Android日志工具

import com.micsig.base.Logger; // 应用日志工具
import com.micsig.tbook.scope.Calibrate.CabteRegister; // 校准寄存器，读写校准参数
import com.micsig.tbook.scope.Calibrate.CalibrateService; // 校准服务，提供校准类型常量和结果查询
import com.micsig.tbook.scope.Calibrate.FactorCalibrate; // 校准因子管理器，执行校准流程
import com.micsig.tbook.scope.Sample.Sample; // 采样管理器，设置采样模式
import com.micsig.tbook.scope.Scope; // 示波器核心状态管理
import com.micsig.tbook.scope.channel.Channel; // 通道常量（如阻抗类型）
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，判断通道类型
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.ToolsSCPI; // SCPI工具类
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand; // 外部按键命令，模拟硬件按键
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 波形管理器，设置通道位置
import com.micsig.tbook.ui.wavezone.TChan; // 通道UI工具类，遍历数学/参考通道

import java.util.List; // 列表集合

/*
 * +=============================================================================+
 * |                       Command_Calibrate 类                                   |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   校准命令模块，管理示波器的零点校准、通道差异校准、AD相位校准、AD增益校准、   |
 * |   偏移量校准、通道增益校准、电容校准、上下行校准、触发相关校准等全流程         |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 提供校准启动/退出/停止/结果查询的SCPI入口                               |
 * |   2. 提供各类校准项的执行入口（零点/偏移/增益/电容/触发等）                   |
 * |   3. 提供各类校准项的结果查询                                                 |
 * |   4. 校准开始前自动关闭数学通道/参考通道/ZOOM/高刷新，打开所有通道归中心     |
 * |   5. 校准结束后解除锁屏，通道归中心                                           |
 * |   6. 强制停止校准时设置校准标记结束                                           |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单命令类，每个公共方法对应一个SCPI命令。校准执行委托给FactorCalibrate，   |
 * |   参数存储委托给CabteRegister。私有方法onCalBegin/onCalEnd/onCalStop管理流程。 |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   SCPI/UI → 校准方法 → FactorCalibrate.begin() → 底层硬件                   |
 * |            → CabteRegister → 校准参数读写                                    |
 * |            → RxBus → UI更新（自校准启动通知）                                |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - FactorCalibrate: 校准执行器，begin()启动校准，getCalResult()获取结果      |
 * |   - CabteRegister: 校准寄存器，读写校准参数和时间                            |
 * |   - CalibrateService: 校准服务，提供校准类型常量和结果字符串                  |
 * |   - Command: 获取各子命令模块（通道/数学/参考/显示/菜单/功能菜单等）         |
 * |   - Scope: 示波器状态（运行/停止/AUTO/单序列）                               |
 * |   - ChannelFactory: 判断是否为动态通道                                       |
 * |   - GlobalVar: 全局变量（通道数/波形尺寸）                                   |
 * |   - WaveManage: 设置通道垂直位置                                             |
 * |   - TChan: 遍历数学/参考通道                                                 |
 * |   - ExternalKeysCommand: 模拟运行/停止按键                                  |
 * |   - RxBus: 事件总线                                                          |
 * |   - ToolsSCPI: SCPI响应格式化                                                |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. SCPI远程控制执行校准流程                                                |
 * |   2. UI界面触发自校准功能                                                    |
 * |   3. 查询各校准项的执行结果                                                  |
 * |   4. 查询/重置校准寄存器数据                                                 |
 * +=============================================================================+
 */
public class Command_Calibrate {
//    new SCPICommandStruct(":CALibrate:DATE?","SCPI_Calibrate","DateQ"),//查询上次校准时间
//    new SCPICommandStruct(":CALibrate:STARt","SCPI_Calibrate","Start"),//开始校准
//    new SCPICommandStruct(":CALibrate:QUIT","SCPI_Calibrate","Quit"),//退出校准，校准完成后的操作
//    new SCPICommandStruct(":CALibrate:STOP","SCPI_Calibrate","Stop"),//停止校准，强制停止
//    new SCPICommandStruct(":CALibrate:RESult?","SCPI_Calibrate","ResultQ"),//查询校准结果
//    new SCPICommandStruct(":CALibrate:ZERopoint","SCPI_Calibrate","ZeroPoint"),//零点校准
//    new SCPICommandStruct(":CALibrate:ZERopoint?","SCPI_Calibrate","ZeroPointQ"),//查询零点校准状态
//    new SCPICommandStruct(":CALibrate:CHDF","SCPI_Calibrate","Chdf"),//通道差异校准
//    new SCPICommandStruct(":CALibrate:CHDF?","SCPI_Calibrate","ChdfQ"),//查询通道差异校准状态
//    new SCPICommandStruct(":CALibrate:ADPHa","SCPI_Calibrate","Adpha"),//AD相位校准
//    new SCPICommandStruct(":CALibrate:ADPHa?","SCPI_Calibrate","AdphaQ"),//查询AD相位校准状态
//    new SCPICommandStruct(":CALibrate:ADGain","SCPI_Calibrate","AdGain"),//AD增益校准
//    new SCPICommandStruct(":CALibrate:ADGain?","SCPI_Calibrate","AdGinQ"),//查询AD增益校准状态
//    new SCPICommandStruct(":CALibrate:OFFSet","SCPI_Calibrate","Offset"),//偏移量校准
//    new SCPICommandStruct(":CALibrate:OFFSet?","SCPI_Calibrate","OffsetQ"),//查询偏移量校准状态
//    new SCPICommandStruct(":CALibrate:CHGain","SCPI_Calibrate","ChGain"),//通道增益校准
//    new SCPICommandStruct(":CALibrate:CHGain?","SCPI_Calibrate","ChGainQ"),//查询通道增益校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint","SCPI_Calibrate","Trigger_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint?","SCPI_Calibrate","Trigger_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint","SCPI_Calibrate","Trigger_AC_ZeroPoint"),//触发触发零点校准
//    new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint?","SCPI_Calibrate","Trigger_AC_ZeroPointQ"),//查询触发零点校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient","SCPI_Calibrate","Trigger_Coefficient"),//触发系数校准
//    new SCPICommandStruct(":CALibrate:TRIGger:COEFficient?","SCPI_Calibrate","Trigger_CoefficientQ"),//查询触发系数校准状态
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise","SCPI_Calibrate","Trigger_Precise"),//精准触发校准
//    new SCPICommandStruct(":CALibrate:TRIGger:PRECise?","SCPI_Calibrate","Trigger_PreciseQ"),//查询精准触发校准状态
//    new SCPICommandStruct(":CALibrate:DATE:LENGth?","SCPI_Calibrate","Date_LengthQ"),//查询校准数据长度
//    new SCPICommandStruct(":CALibrate:DATE:GET","SCPI_Calibrate","Date_Get"),//获取校准数据
//    new SCPICommandStruct(":CALibrate:FILE:RESet?","SCPI_Calibrate","File_ResetQ"),//获取校准数据

    /**
     * 查询上次校准时间
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 上次校准时间字符串
     */
    public String DateQ(boolean isUpdateUI) {
        return CabteRegister.getInstance().getCabteTime(); // 从校准寄存器获取上次校准时间
    }

    /**
     * 开始校准流程
     * <p>发送自校准UI通知，当前未调用onCalBegin()做前置环境准备</p>
     *
     * @param isUpdateUI 是否同步更新UI
     * @return SCPI OKAY响应
     */
    public String Start(boolean isUpdateUI) {
        Logger.i("scpi", "start"); // 记录SCPI校准启动日志
        //onCalBegin(); // 校准前置环境准备（已注释，当前未启用）
        if (isUpdateUI) { // 需要同步更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SELFADJUST); // 设置消息标志：自校准
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
        return ToolsSCPI.getOKAY(); // 返回SCPI OKAY响应
    }

    /**
     * 退出校准（校准完成后的操作）
     * <p>当前未调用onCalEnd()做后置环境恢复</p>
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return SCPI OKAY响应
     */
    public String Quit(boolean isUpdateUI) {
        Logger.i("scpi", "Quit"); // 记录SCPI校准退出日志
        //onCalEnd(); // 校准后置环境恢复（已注释，当前未启用）
        return ToolsSCPI.getOKAY(); // 返回SCPI OKAY响应
    }

    /**
     * 强制停止校准
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 停止结果字符串
     */
    public String Stop(boolean isUpdateUI) {
        Logger.i("scpi", "stop"); // 记录SCPI校准停止日志
        return onCalStop(); // 调用私有方法强制停止校准
    }

    /**
     * 查询校准结果
     * <p>如果正在校准中返回"caling"，否则返回各校准项结果的拼接字符串</p>
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String ResultQ(boolean isUpdateUI) {
           if ((FactorCalibrate.getInstance().isCalibrating())){ // 检查是否正在校准中
               return "caling"; // 正在校准中，返回"caling"
           }
           List s = CalibrateService.getInstance().getCalibrate().getResultString(); // 获取校准结果字符串列表
           StringBuilder sb = new StringBuilder(); // 创建字符串构建器
           for (int i = 0; i < s.size(); i++) { // 遍历所有结果字符串
               sb.append(s.get(i)); // 拼接每个结果字符串
           }
           Logger.i("scpi", "ResultQ  :" + sb.toString()); // 记录校准结果日志
           return sb.toString(); // 返回拼接后的校准结果
    }

    /**
     * 零点校准
     */
    public void ZeroPoint(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin(CalibrateService.ZERO_CALIBRATE); // 启动零点校准
    }

    /**
     * 零点校准结果
     *
     * @param isUpdateUI
     * @return
     */
    public String ZeroPointQ(boolean isUpdateUI) {
        boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.ZERO_CALIBRATE); // 获取零点校准结果
        return ToolsSCPI.getSuccState(b); // 将布尔结果转为SCPI成功/失败字符串
    }

    /**
     * 通道差异
     *
     * @param isUpdateUI
     */
    public void Chdf(boolean isUpdateUI) {
//        FactorCalibrate.getInstance().begin(CalibrateService.CHDIFF_CALIBRATE); // 通道差异校准（已注释，未启用）
    }

    /**
     * 查询通道差异校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，当前固定返回失败
     */
    public String ChdfQ(boolean isUpdateUI) {
        boolean b = false; // 通道差异校准结果，当前固定为false（功能未启用）
        return ToolsSCPI.getSuccState(b); // 返回失败状态
    }

    /**
     * AD相位校准（空实现，预留接口）
     *
     * @param param1      参数1（未使用）
     * @param isUpdateUI  是否同步更新UI（未使用）
     */
    public void Adpha(double param1, boolean isUpdateUI) {

    }

    /**
     * 查询AD相位校准结果（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void AdphaQ(boolean isUpdateUI) {

    }

    /**
     * AD增益校准（当前已注释实现，未启用）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void AdGain(boolean isUpdateUI) {
        //FactorCalibrate.getInstance().begin(CalibrateService.ADGAIN_CALIBRATE); // AD增益校准（已注释）
    }

    /**
     * 查询AD增益校准结果
     * <p>当前固定返回失败（功能未启用）</p>
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，当前固定返回失败
     */
    public String AdGinQ(boolean isUpdateUI) {
        boolean b = false;//FactorCalibrate.getInstance().getCalResult(CalibrateService.ADGAIN_CALIBRATE); // AD增益校准结果（已注释，固定false）
        return ToolsSCPI.getSuccState(b); // 返回失败状态
    }

    /**
     * 偏移量校准（通道系数校准）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Offset(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin(CalibrateService.CHCOEF_CALIBRATE); // 启动通道系数校准（偏移量）
    }

    /**
     * 查询偏移量校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串
     */
    public String OffsetQ(boolean isUpdateUI) {
        boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCOEF_CALIBRATE); // 获取通道系数校准结果
        return ToolsSCPI.getSuccState(b); // 返回成功/失败状态
    }

    /**
     * 扩展通道增益校准，带电阻类型和标准幅度参数
     *
     * @param chIdx      通道索引
     * @param amp        幅度值
     * @param resType    电阻类型
     * @param stdAmp     标准幅度
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void ExChGain(double chIdx,double amp,double resType,double stdAmp,boolean isUpdateUI){

        double[] param = {chIdx,amp,resType,stdAmp}; // 组装校准参数数组：通道索引+幅度+电阻类型+标准幅度
        FactorCalibrate.getInstance().begin(CalibrateService.CHGAIN_CALIBRATE_EX, param); // 启动扩展通道增益校准
    }

    /**
     * 查询扩展通道增益校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String ExChGainQ(boolean isUpdateUI) {
        String result; // 声明结果变量
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中，返回"caling"
        } else { // 校准已完成
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHGAIN_CALIBRATE_EX); // 获取扩展通道增益校准结果
            result=ToolsSCPI.getSuccState(b); // 转为成功/失败字符串
        }
        return result; // 返回结果
    }

    /**
     * 通道增益校准
     *
     * @param chIndex    通道索引
     * @param amp        幅度值
     * @param chmode     通道模式
     * @param idx        索引
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void ChGain(double chIndex, double amp, double chmode,double idx,boolean isUpdateUI) {
        double[] param = {chIndex, amp,chmode,idx}; // 组装校准参数数组：通道索引+幅度+通道模式+索引
        FactorCalibrate.getInstance().begin(CalibrateService.CHGAIN_CALIBRATE, param); // 启动通道增益校准

    }

    /**
     * 查询通道增益校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String ChGainQ(boolean isUpdateUI) {
        String result; // 声明结果变量
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中
        } else { // 校准已完成
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHGAIN_CALIBRATE); // 获取通道增益校准结果
            result=ToolsSCPI.getSuccState(b); // 转为成功/失败字符串
        }
        return result; // 返回结果
    }

    /**
     * 查询通道输入电压值
     *
     * @param ch         通道索引
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 通道输入电压的SCPI格式字符串
     */
    public String ChValQ(double ch,boolean isUpdateUI) {
        return ToolsSCPI.getDouble(CabteRegister.getInstance().getNewInputV((int)Math.round(ch))); // 从校准寄存器获取通道输入电压并格式化
    }

    /**
     * 设置通道幅度校准值
     *
     * @param ch         通道索引
     * @param amp        幅度值
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void ChSetVal(double ch,double amp,boolean isUpdateUI) {
        Log.d("xxxx", "ChSetVal() called with: ch = [" + ch + "], amp = [" + amp + "], isUpdateUI = [" + isUpdateUI + "]"); // 调试日志
        CabteRegister.getInstance().setChAmp((int)Math.round(ch),amp); // 设置校准寄存器中的通道幅度值
    }

    /**
     * 通道系数校准（带档位和电阻类型参数）
     *
     * @param chIndex         通道索引
     * @param gearIndex       档位索引
     * @param amp             幅度值
     * @param resistanceType  电阻类型
     * @param isUpdateUI      是否同步更新UI（未使用）
     */
    public void ChCofit(double chIndex, double gearIndex, double amp,double resistanceType,boolean isUpdateUI){
        double[] param = {chIndex, gearIndex,amp,resistanceType}; // 组装校准参数数组：通道索引+档位索引+幅度+电阻类型
        FactorCalibrate.getInstance().begin(CalibrateService.CHCOEF_CALIBRATE, param); // 启动通道系数校准
    }

    /**
     * 查询通道系数校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String ChCofitQ(boolean isUpdateUI) {
        String result; // 声明结果变量
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中
        } else { // 校准已完成
            boolean b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCOEF_CALIBRATE); // 获取通道系数校准结果
            result=ToolsSCPI.getSuccState(b); // 转为成功/失败字符串
        }
        return result; // 返回结果
    }

    /**
     * 设置通道电容校准值（仅对动态通道有效）
     *
     * @param chIdx      通道索引
     * @param v          电压值，用于确定档位索引
     * @param val        电容校准值
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void CapVal(int chIdx, double v,int val, boolean isUpdateUI) {

        CabteRegister cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器实例

        if(ChannelFactory.isDynamicCh(chIdx)){ // 仅对动态通道进行电容校准

            cabteRegister.setChCapacitanceHigh(chIdx,cabteRegister.getRatioIdx(Channel.RESISTANCE_1M,v),val); // 设置高阻抗1MΩ档位的电容校准值

            cabteRegister.saveFactoryCalibrateParam(); // 保存出厂校准参数到寄存器
            Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL); // 设置采样模式为普通模式
        }

    }

    /**
     * 查询通道电容校准值（仅对动态通道有效）
     *
     * @param chIdx      通道索引
     * @param v          电压值，用于确定档位索引
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 电容校准值的SCPI格式字符串，非动态通道返回-1
     */
    public String CapValQ(int chIdx,double v,boolean isUpdateUI) {
        int val = -1; // 默认值-1，表示非动态通道
        CabteRegister cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器实例
        if(ChannelFactory.isDynamicCh(chIdx)){ // 仅对动态通道查询电容校准值
            val = cabteRegister.getChCapacitanceHigh(chIdx,cabteRegister.getRatioIdx(Channel.RESISTANCE_1M,v));; // 获取高阻抗1MΩ档位的电容校准值
        }
        return ToolsSCPI.getInt(val); // 将整数值转为SCPI格式字符串
    }

    /**
     * 通道电容校准
     *
     * @param chIndex    通道索引
     * @param gearIndex  档位索引
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void ChCap(double chIndex, double gearIndex, boolean isUpdateUI) {
        double[] param = {chIndex, gearIndex}; // 组装校准参数数组：通道索引+档位索引

        FactorCalibrate.getInstance().begin(CalibrateService.CHCAP_CALIBRATE, param); // 启动通道电容校准


    }

    /**
     * 查询通道电容校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String ChCapQ(boolean isUpdateUI) {
        String result=""; // 声明结果变量，默认空字符串
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中
        } else { // 校准已完成
            boolean b = false; // 默认失败

                b = FactorCalibrate.getInstance().getCalResult(CalibrateService.CHCAP_CALIBRATE); // 获取通道电容校准结果

            result=ToolsSCPI.getSuccState(b); // 转为成功/失败字符串
        }
        return result; // 返回结果
    }

    /**
     * 上行校准（顶部校准）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void UpCal( boolean isUpdateUI) {

        FactorCalibrate.getInstance().begin_upPard(); // 启动上行校准

    }

    /**
     * 查询上行校准结果
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String UpCalQ(boolean isUpdateUI) {

        String result; // 声明结果变量
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中
        } else { // 校准已完成
            CabteRegister cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器实例

            boolean b = (cabteRegister.isTopCalibration()); // 查询是否已进行顶部校准
            result=ToolsSCPI.getSuccState(b); // 转为成功/失败字符串
        }

        return result; // 返回结果
    }

    /**
     * 下行校准（底部校准）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void DownCal(boolean isUpdateUI) {
        FactorCalibrate.getInstance().begin_downPard(); // 启动下行校准
    }

    /**
     * 查询下行校准结果
     * <p>当前固定返回失败（功能未实现）</p>
     *
     * @param isUpdateUI 是否同步更新UI（本方法未使用）
     * @return 校准结果字符串，"caling"表示正在校准中
     */
    public String DownCalQ(boolean isUpdateUI) {

        String result; // 声明结果变量
        if (FactorCalibrate.getInstance().isCalibrating()) { // 检查是否正在校准中
            result= "caling"; // 正在校准中
        } else { // 校准已完成

            boolean b = false; // 下行校准结果，当前固定为false（功能未实现）
            result=ToolsSCPI.getSuccState(b); // 返回失败状态
        }
        return result; // 返回结果
    }

    /**
     * 触发零点校准（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_ZeroPoint(boolean isUpdateUI) {

    }

    /**
     * 查询触发零点校准结果（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_ZeroPointQ(boolean isUpdateUI) {

    }

    /**
     * 触发AC零点校准（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_AC_ZeroPoint(boolean isUpdateUI) {

    }

    /**
     * 查询触发AC零点校准结果（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_AC_ZeroPointQ(boolean isUpdateUI) {

    }

    /**
     * 触发系数校准（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_Coefficient(boolean isUpdateUI) {

    }

    /**
     * 查询触发系数校准结果（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_CoefficientQ(boolean isUpdateUI) {

    }

    /**
     * 精准触发校准（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_Precise(boolean isUpdateUI) {

    }

    /**
     * 查询精准触发校准结果（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Trigger_PreciseQ(boolean isUpdateUI) {

    }

    /**
     * 查询校准数据长度（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Date_LengthQ(boolean isUpdateUI) {

    }

    /**
     * 获取校准数据（空实现，预留接口）
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     */
    public void Date_Get(boolean isUpdateUI) {

    }

    /**
     * 重置校准数据文件到出厂默认值
     *
     * @param isUpdateUI 是否同步更新UI（未使用）
     * @return 固定返回true
     */
    public boolean DatFile_ResetQ(boolean isUpdateUI) {

        CabteRegister.getInstance().rstDefaultVal(); // 重置校准寄存器到出厂默认值
        return true; // 返回true表示重置成功
    }


    //校准开始
    /**
     * 校准开始前的环境准备
     * <p>1. 关闭所有数学通道和参考通道</p>
     * <p>2. 关闭ZOOM和高刷新模式</p>
     * <p>3. 打开所有通道并归位到中心</p>
     * <p>4. 如果正在运行则确保示波器处于运行状态（关闭AUTO/单序列或启动运行）</p>
     * <p>5. 锁定屏幕防止误操作</p>
     */
    private void onCalBegin() {
        Logger.i("scpi", "oncalbegin!"); // 记录校准开始日志
        int cnt = GlobalVar.get().getChannelsCount(); // 获取通道总数
        //关闭数学
        TChan.foreachMath(mathChan ->{ // 遍历所有数学通道
            Command.get().getMath().Display(TChan.toFpgaChNo(mathChan), false, true); // 关闭数学通道显示
        });
        //关闭ref
        TChan.foreachRef(refChan -> { // 遍历所有参考通道
            Command.get().getReference().Display(TChan.toFpgaChNo(refChan), false, true); // 关闭参考通道显示
        });
        //关闭ZOOM
        Command.get().getDisplay().Zoom(false, true); // 关闭ZOOM缩放模式
        //关闭高刷新
        Command.get().getDisplay().High(false, true); // 关闭高刷新率模式
        //打开所有通道
        //所有通道位置归中心0
        for (int i = 0; i < cnt; i++) { // 遍历所有物理通道
            Command.get().getChannel().Display(i, true, true); // 打开通道显示
            Command.get().getChannel().setPosition(i, GlobalVar.get().getMainWave().y/2); // 设置通道垂直位置到中心
            WaveManage.get().setPositionY(i + 1, ((int) GlobalVar.get().getMainWave().y/2)); // 同步波形管理器的通道位置
        }
//        if (是在运行状态)
        if (Scope.getInstance().isRun()) { // 示波器当前处于运行状态
//        {
//            if (是AUTO){
            if (Scope.getInstance().isAuto()) { // 当前是AUTO模式
//                关掉AUTO
                Scope.getInstance().Auto(false); // 关闭AUTO模式
                Command.get().getFunctionMenu().Auto(true); // 更新UI上的AUTO按钮状态
            }
//            }
//            if (是单序列){
            if (Scope.getInstance().isSingle()) { // 当前是单序列模式
//                关掉单序列
                Scope.getInstance().setSingle(false); // 关闭单序列模式
                Command.get().getFunctionMenu().Single(true); // 更新UI上的单序列按钮状态
//            }
            }
//        }
        } else { // 示波器当前处于停止状态
//        else
//        {
//            运行
            ExternalKeysCommand.get().clickRunStop(); // 模拟按下运行/停止键，启动运行
//        }
        }


        //锁屏
        Command.get().getMenu().Lock(true,true); // 锁定屏幕防止误操作
    }

    /**
     * 校准结束后的环境恢复
     * <p>1. 解除屏幕锁定</p>
     * <p>2. 所有通道位置归中心</p>
     */
    private void onCalEnd() {
        int cnt = GlobalVar.get().getChannelsCount(); // 获取通道总数
        //解除屏幕锁
        Command.get().getMenu().Unlock(true); // 解除屏幕锁定
        //所有通道位置归中心0
        for (int i = 0; i < cnt; i++) { // 遍历所有物理通道
            Command.get().getChannel().setPosition(i, GlobalVar.get().getMainWave().y/2); // 设置通道垂直位置到中心
            WaveManage.get().setPositionY(i + 1, ((int) GlobalVar.get().getMainWave().y/2)); // 同步波形管理器的通道位置
        }

        //刷新右上解的状态

    }

    /**
     * 强制停止校准
     *
     * @return "OKAY"表示停止成功
     */
    private String onCalStop() {
        //设置校准标记结束
        FactorCalibrate.getInstance().forceEnd(); // 强制结束校准流程
        return "OKAY"; // 返回OKAY
    }


}
