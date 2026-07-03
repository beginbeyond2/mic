package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI数学AXB运算命令处理

import com.micsig.base.Logger; // 日志工具
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.MathChannel; // 数学通道
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-25 14:44
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                           Command_Math_AXB                                   |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 数学运算AX+B子命令处理层                         |
 * | 核心职责：管理AX+B线性运算的信源、系数A/B、单位、垂直档位和偏移             |
 * | 架构设计：属于Command_Math的子模块，由Command单例统一调度；                  |
 * |          A/B系数输入支持工程记数法（如1k, 3.3m），带合法性校验；             |
 * |          单位字符串限制最大3个字符                                           |
 * | 数据流向：SCPI指令 → Command_Math_AXB → 成员变量(状态存储)                 |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、ChannelFactory(通道工厂)、RxBus(事件总线)、     |
 * |           CacheUtil(缓存)、TBookUtil(数值转换)、Tools(输入校验)              |
 * | 使用场景：远程SCPI控制中设置/查询AX+B运算参数                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Math_AXB {
//            new SCPICommandStruct(":MATH:AX+B:SOURce","SCPI_Math_AXB","Source"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:SOURce?","SCPI_Math_AXB","SourceQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:A","SCPI_Math_AXB","A"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:A?","SCPI_Math_AXB","AQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B","SCPI_Math_AXB","B"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B?","SCPI_Math_AXB","BQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT","SCPI_Math_AXB","Unit"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT?","SCPI_Math_AXB","UnitQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:EXTent","SCPI_Math_AXB","Extent"),//设置AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:EXTent?","SCPI_Math_AXB","ExtentQ"),//查询AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:OFFSet","SCPI_Math_AXB","Offset"),//设置AXB运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:AX+B:OFFSet?","SCPI_Math_AXB","OffsetQ"),//查询AXB运算结果的垂直偏移

    private int ch; // AXB运算信源通道索引
    private double extent; // AXB运算结果垂直档位
    private double offset; // AXB运算结果垂直偏移
    private String a=""; // 系数A字符串（支持工程记数法）
    private String b=""; // 系数B字符串（支持工程记数法）
    private String unit=""; // 用户自定义单位字符串

    /**
     * 设置AXB运算的信源通道
     * 对应SCPI指令: :MATH:AX+B:SOURce
     * @param mathIndex 数学通道索引
     * @param ch 信源通道索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Source(int mathIndex, int ch, boolean isUpdateUI) {
        this.ch=ch; // 更新信源通道
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Source); // 设置消息标志为AXB信源
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(ch); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }

    }

    /**
     * 查询AXB运算的信源通道
     * 对应SCPI指令: :MATH:AX+B:SOURce?
     * @return 信源通道索引
     */
    public  int SourceQ(){
        return this.ch; // 返回信源通道索引
    }

    /** 工程记数法单位后缀列表，用于A/B系数输入校验 */
    private static final String ABunit[]={"k","p","n","u","m","0","1","2","3","4","5","6","7","8","9"}; // 工程记数法后缀

    /**
     * 设置AXB运算的系数A
     * 对应SCPI指令: :MATH:AX+B:A
     * @param mathIndex 数学通道索引
     * @param a 系数A字符串（支持工程记数法，如1k, 3.3m）
     * @param isUpdateUI 是否通知UI更新
     */
    public  void A(int mathIndex, String a,boolean isUpdateUI){
        //判断输入合法性
        a=a.trim(); // 去除首尾空白
        boolean isNum=false; // 数值部分是否合法
        int index=-1; // 后缀在ABunit中的索引
        if (a.length()<=1){ // 长度为0或1，仅数字
            isNum = Tools.isNumeric(a); // 判断是否为数字
            index=0; // 后缀索引设为0（无后缀）
        }else { // 长度大于1
            String lastWord = a.substring(a.length() - 1); // 取最后一个字符
            index = Tools.indexOf(ABunit, s -> lastWord.equals(s)); // 查找后缀在ABunit中的索引
            if (Tools.isNumeric(lastWord)){ // 最后一个字符是数字
                isNum=Tools.isNumeric(a); // 整个字符串必须全部为数字
            }else { // 最后一个字符是后缀
                String num = a.substring(0, a.length() - 1); // 取数值部分
                isNum = Tools.isNumeric(num); // 判断数值部分是否为数字
            }

        }
//        Logger.i(Command.TAG,"lastword:"+lastWord+",num:"+num);
//        Logger.i(Command.TAG,"b:"+b+",index:"+index);
        if (isNum==false || index==-1) return; // 输入不合法则直接返回

        //超范围处理
        double d= TBookUtil.getDoubleFromM(a); // 将工程记数法字符串转为double值
        a = TBookUtil.getValue(d); // 将double值转为规范化的工程记数字符串
        this.a=a; // 更新系数A
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_A); // 设置消息标志为AXB系数A
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(a); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询AXB运算的系数A
     * 对应SCPI指令: :MATH:AX+B:A?
     * @return 系数A字符串
     */
    public  String AQ(){
        return a; // 返回系数A
    }

    /**
     * 设置AXB运算的系数B
     * 对应SCPI指令: :MATH:AX+B:B
     * @param mathIndex 数学通道索引
     * @param b 系数B字符串（支持工程记数法）
     * @param isUpdateUI 是否通知UI更新
     */
    public void B(int mathIndex, String b, boolean isUpdateUI) {
        b=b.trim(); // 去除首尾空白
        boolean isNum=false; // 数值部分是否合法
        int index=-1; // 后缀在ABunit中的索引
        if (b.length()<=1){ // 长度为0或1
            isNum = Tools.isNumeric(b); // 判断是否为数字
            index=0; // 后缀索引设为0
        }else { // 长度大于1
            String lastWord = b.substring(b.length() - 1); // 取最后一个字符
            index = Tools.indexOf(ABunit, s -> lastWord.equals(s)); // 查找后缀索引
            if (Tools.isNumeric(lastWord)){ // 最后一个字符是数字
                isNum=Tools.isNumeric(b); // 整个字符串必须全部为数字
            }else { // 最后一个字符是后缀
                String num = b.substring(0, b.length() - 1); // 取数值部分
                isNum = Tools.isNumeric(num); // 判断数值部分是否为数字
            }
        }
        if (isNum==false || index==-1) return; // 输入不合法则直接返回
        double d= TBookUtil.getDoubleFromM(b); // 将工程记数法字符串转为double值
        b = TBookUtil.getValue(d); // 将double值转为规范化的工程记数字符串
        this.b=b; // 更新系数B
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_B); // 设置消息标志为AXB系数B
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(b); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询AXB运算的系数B
     * 对应SCPI指令: :MATH:AX+B:B?
     * @return 系数B字符串
     */
    public  String BQ(){
        return this.b; // 返回系数B
    }

    /**
     * 设置AXB运算的自定义单位
     * 对应SCPI指令: :MATH:AX+B:UNIT
     * @param mathIndex 数学通道索引
     * @param unit 单位字符串（最多3个字符）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Unit(int mathIndex, String unit, boolean isUpdateUI) {
        unit=unit.trim(); // 去除首尾空白
        if (unit.length()>3) unit=unit.substring(0,3); // 超过3个字符则截断
        this.unit=unit; // 更新单位
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_UNIT); // 设置消息标志为AXB单位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(unit); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询AXB运算的自定义单位
     * 对应SCPI指令: :MATH:AX+B:UNIT?
     * @return 单位字符串
     */
    public  String UnitQ(){
        return this.unit; // 返回单位
    }


    /**
     * 设置AXB运算结果的垂直档位
     * 对应SCPI指令: :MATH:AX+B:EXTent
     * @param mathIndex 数学通道索引
     * @param ext 垂直档位值
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Extent(int mathIndex, double ext,boolean isUpdateUI){
//        if (this.extent==ext) return;
        this.extent=ext; // 更新垂直档位
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Extent); // 设置消息标志为AXB垂直档位
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询AXB运算结果的垂直档位
     * 对应SCPI指令: :MATH:AX+B:EXTent?
     * @return 垂直档位值
     */
    public  double ExtentQ(){
        return this.extent; // 返回垂直档位
    }

    /**
     * 设置AXB运算结果的垂直偏移
     * 对应SCPI指令: :MATH:AX+B:OFFSet
     * @param mathIndex 数学通道索引
     * @param offset 垂直偏移值
     * @param isUpdateUI 是否通知UI更新
     */
    public  void Offset(int mathIndex, double offset,boolean isUpdateUI){
//        if (this.offset==offset) return;
        this.offset=offset; // 更新垂直偏移
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Offset); // 设置消息标志为AXB垂直偏移
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(this.offset); // 拼接参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询AXB运算结果的垂直偏移
     * 对应SCPI指令: :MATH:AX+B:OFFSet?
     * @param mathIndex 数学通道索引
     * @return 垂直偏移值（含单位字符串）
     */
    public  String OffsetQ(int mathIndex){
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex); // 获取数学通道对象
        double pos; // 垂直位置像素值
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
//                break;
//            case CacheUtil.MATHTYPE_FFT:
//                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE) == 0) {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
//                } else {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
//                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.toUiChNo(mathIndex)); // 从缓存获取AXB垂直位置
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos; // 像素位置转换为偏移值：屏幕中心 - 缓存位置
        String unit = ChannelFactory.getProbeType(mathIndex); // 获取通道探头类型对应的单位
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit; // 像素偏移转物理量并拼接单位
    }
}
