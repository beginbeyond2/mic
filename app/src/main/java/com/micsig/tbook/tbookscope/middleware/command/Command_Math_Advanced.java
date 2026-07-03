package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI数学高级运算命令处理


import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.MathChannel; // 数学通道
import com.micsig.tbook.scope.math.MathExprError; // 数学表达式错误
import com.micsig.tbook.scope.math.MathExprWave; // 数学表达式波形
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom; // 底部栏Holder
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.KeyBoardFormulaUtil; // 公式键盘工具
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

import java.text.DecimalFormat; // 十进制格式化

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Math_Advanced                                |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 数学运算高级(Advanced)子命令处理层               |
 * | 核心职责：管理高级数学运算的表达式、变量var1/var2、垂直档位/偏移、          |
 * |          自定义单位；表达式长度限制37字符，变量范围±9.9999E+9              |
 * | 架构设计：属于Command_Math的子模块，由Command单例统一调度；                  |
 * |          表达式输入前进行合法性校验（MathExprWave.isExprValid）；            |
 * |          变量使用科学记数法格式化（0.0000E0）                                |
 * | 数据流向：SCPI指令 → Command_Math_Advanced → 成员变量(状态存储)             |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、ChannelFactory(通道工厂)、MathExprWave(表达式校验)|
 * |           RxBus(事件总线)、CacheUtil(缓存)、KeyBoardFormulaUtil(公式转换)    |
 * | 使用场景：远程SCPI控制中设置/查询高级运算表达式和参数                       |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Math_Advanced {
//new SCPICommandStruct(":MATH:ADVanced:EXPRession", "SCPI_Math_Advanced","Expression"),
//            new SCPICommandStruct(":MATH:ADVanced:EXPRession?","SCPI_Math_Advanced","ExpressionQ"),
//            new SCPICommandStruct(":MATH:ADVanced:VAR1","SCPI_Math_Advanced","Var1"),//设置高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR1?","SCPI_Math_Advanced","Var1Q"),//查询高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR2","SCPI_Math_Advanced","Var2"),//设置高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:VAR2?","SCPI_Math_Advanced","Var2Q"),//查询高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:EXTent","SCPI_Math_Advanced","Extent"),//设置高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:EXTent?","SCPI_Math_Advanced","ExtentQ"),//查询高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet","SCPI_Math_Advanced","Offset"),//设置高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet?","SCPI_Math_Advanced","OffsetQ"),//查询高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:UNIT", "SCPI_Math_Advanced","Unit"),
//            new SCPICommandStruct(":MATH:ADVanced:UNIT?", "SCPI_Math_Advanced","UnitQ"),


    private String express=""; // 高级运算表达式字符串
    private double var1; // 变量1的值
    private double var2; // 变量2的值
    private double extent; // 运算结果垂直档位
    private double offset; // 运算结果垂直偏移
    private String unit; // 用户自定义单位
    private double minVar=-9.9999E+9; // 变量最小值
    private double maxVar=9.9999E+9; // 变量最大值

    private DecimalFormat decimalFormat = new DecimalFormat("0.0000E0"); // 科学记数法格式化器

    /**
     * 设置高级运算的表达式
     * 对应SCPI指令: :MATH:ADVanced:EXPRession
     * @param mathIndex 数学通道索引
     * @param express 表达式字符串
     * @param isUpdateUI 是否通知UI更新
     */
    public void Expression(int mathIndex, String express, boolean isUpdateUI) {
//        if (this.express.equals(express)) return;
        express = express.replace("CH","ch"); // 将大写CH替换为小写ch
        express=express.replace("*","×").replace("/","÷").replace("pi","π"); // 替换运算符和常量为显示符号
        MathExprError mathExprError = MathExprWave.isExprValid( // 校验表达式合法性
                KeyBoardFormulaUtil.amFormulaToScope(express.trim(), MainHolderBottom.getCenterTimeBase())); // 将AM公式转为内部格式后校验
        if (!mathExprError.isSuccess() || express.length()>37) return; // 表达式不合法或超长则直接返回

        this.express=express.trim(); // 更新表达式（去除首尾空白）
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Express); // 设置消息标志为高级表达式
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(express.trim()); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算的表达式
     * 对应SCPI指令: :MATH:ADVanced:EXPRession?
     * @return 表达式字符串
     */
    public  String ExpressionQ(){return express;} // 返回表达式

    /**
     * 校验科学记数法字符串的指数部分是否合法（指数最多1位数字）
     * @param s 科学记数法字符串
     * @return 合法返回true
     */
    private boolean isCorrect(String s){
        if(s.contains("E")) { // 包含E（科学记数法）
            String[] param = s.split("E"); // 按E分割

            if (param[1].replace("+", "").replace("-", "").length() > 1) { // 指数部分去除正负号后长度>1
                return false; // 指数超过1位，不合法
            }
        }
        return true; // 合法
    }

    /**
     * 设置高级运算表达式中的变量1
     * 对应SCPI指令: :MATH:ADVanced:VAR1
     * @param mathIndex 数学通道索引
     * @param var1 变量1的值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Var1(int mathIndex, double var1, boolean isUpdateUI) {
        if (var1<minVar) var1=minVar; // 低于最小值则钳位
        if (var1>maxVar) var1=maxVar; // 超过最大值则钳位
        String s= decimalFormat.format(var1); // 格式化为科学记数法
        Log.d("zhuzh","s:" + s + "var1:" + var1); // 调试日志
        if (isCorrect(s)==false) return; // 格式化结果不合法则直接返回
        this.var1=var1; // 更新变量1
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Var1); // 设置消息标志为变量1
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(s); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算表达式中的变量1
     * 对应SCPI指令: :MATH:ADVanced:VAR1?
     * @return 变量1的值
     */
    public  double Var1Q(){return var1;} // 返回变量1

    /**
     * 设置高级运算表达式中的变量2
     * 对应SCPI指令: :MATH:ADVanced:VAR2
     * @param mathIndex 数学通道索引
     * @param var2 变量2的值
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Var2(int mathIndex, double var2,boolean isUpdateUI){
        if (var2<minVar) var2=minVar; // 低于最小值则钳位
        if (var2>maxVar) var2=maxVar; // 超过最大值则钳位
        String s= decimalFormat.format(var2); // 格式化为科学记数法
        if (isCorrect(s)==false) return; // 格式化结果不合法则直接返回
        this.var2=var2; // 更新变量2
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Var2); // 设置消息标志为变量2
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(s); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算表达式中的变量2
     * 对应SCPI指令: :MATH:ADVanced:VAR2?
     * @return 变量2的值
     */
    public  double Var2Q(){return var2;} // 返回变量2

    /**
     * 设置高级运算结果的垂直档位
     * 对应SCPI指令: :MATH:ADVanced:EXTent
     * @param mathIndex 数学通道索引
     * @param extent 垂直档位值
     * @param isUpdateUI 是否通知UI更新
     */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI)
    {
//        if (this.extent==extent) return;
        this.extent=extent; // 更新垂直档位
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Extent); // 设置消息标志为高级垂直档位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算结果的垂直档位
     * 对应SCPI指令: :MATH:ADVanced:EXTent?
     * @return 垂直档位值
     */
    public  double ExtentQ(){return extent;} // 返回垂直档位

    /**
     * 设置高级运算结果的垂直偏移
     * 对应SCPI指令: :MATH:ADVanced:OFFSet
     * @param mathIndex 数学通道索引
     * @param offset 垂直偏移值
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Offset(int mathIndex, double offset,boolean isUpdateUI){
//        if (this.offset==offset) return;
        this.offset=offset; // 更新垂直偏移
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Offset); // 设置消息标志为高级垂直偏移
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算结果的垂直偏移
     * 对应SCPI指令: :MATH:ADVanced:OFFSet?
     * @param mathIndex 数学通道索引
     * @return 垂直偏移值（含单位字符串）
     */
    public String OffsetQ(int mathIndex) {
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex); // 获取数学通道对象
        double pos; // 垂直位置像素值
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//        pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
//                break;
//            case CacheUtil.MATHTYPE_FFT:
//                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE) == 0) {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
//                } else {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
//                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION);
//                break;
//            default:
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + TChan.toUiChNo(mathIndex)); // 从缓存获取高级运算垂直位置
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 像素位置转换为偏移值：屏幕中心 - 缓存位置
        String unit = ChannelFactory.getProbeType(mathIndex); // 获取通道探头类型对应的单位
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit; // 像素偏移转物理量并拼接单位
    }

    /**
     * 设置高级运算的自定义单位
     * 对应SCPI指令: :MATH:ADVanced:UNIT
     * @param mathIndex 数学通道索引
     * @param unit 单位字符串（最多3个字符）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Unit(int mathIndex, String unit,boolean isUpdateUI){
        unit= unit.trim(); // 去除首尾空白
        if (unit.length()>3) unit= unit.substring(0,3); // 超过3个字符则截断
        this.unit=unit; // 更新单位
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Unit); // 设置消息标志为高级单位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(unit.trim()); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询高级运算的自定义单位
     * 对应SCPI指令: :MATH:ADVanced:UNIT?
     * @return 单位字符串
     */
    public String UnitQ(){
        return unit; // 返回单位
    }

}
