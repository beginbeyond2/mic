package com.micsig.tbook.tbookscope.scpi;

import com.micsig.base.Logger;

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                 ATest_SCPI - SCPI命令测试辅助类                          |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令系统的测试与调试辅助模块                                 |   *
 *   |  核心职责: 维护一组SCPI命令测试用例，支持遍历、前进、后退获取测试SCPI命令字符串   |   *
 *   |  架构设计: 单例模式，内部维护ScpiBean数组和当前索引，提供顺序/逆序遍历接口       |   *
 *   |  数据流向: 外部调用getSCPICurrent/Next/Prev → 组装SCPI命令字符串 → 返回      |   *
 *   |  依赖关系: 依赖Logger(日志输出)                                           |   *
 *   |  使用场景: 开发调试阶段，用于批量遍历测试SCPI命令是否被设备正确响应              |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class ATest_SCPI {

    /** 单例实例 */
    private static ATest_SCPI aTestScpi; // 单例引用
    /** SCPI命令测试数组，包含所有待测试的SCPI命令及参数 */
    static ScpiBean[] scpis = null; // SCPI命令测试数组

    /**
     * 获取ATest_SCPI的单例实例。
     * 使用懒加载方式，首次调用时创建实例。
     * @return ATest_SCPI单例实例
     */
    public static ATest_SCPI get() {
        if (aTestScpi == null) { // 判断单例是否已创建
            aTestScpi = new ATest_SCPI(); // 首次调用时创建单例实例
        }
        return aTestScpi; // 返回单例实例
    }

    /**
     * 私有构造函数，初始化SCPI命令测试数组。
     * 数组中每条命令对应一个ScpiBean(function, params)，
     * 大量注释掉的命令保留供参考，仅保留部分活跃命令用于测试。
     */
    private ATest_SCPI() {
        if (scpis == null) { // 判断测试数组是否已初始化
            //region scpis初始化
            scpis = new ScpiBean[]{
//                    new ScpiBean(":AUTO", ""),
//                    new ScpiBean(":RUN", ""),
//                    new ScpiBean(":STOP", ""),
//                    new ScpiBean(":SINGle", ""),
//                    new ScpiBean(":MULTiple", ""),
//                    new ScpiBean(":BEEP", ""),
////                    new ScpiBean(":CALibrate:DATE?", ""),
//                    new ScpiBean(":CALibrate:STARt", ""),
//                    new ScpiBean(":CALibrate:QUIT", ""),
//                    new ScpiBean(":CALibrate:STOP", ""),
////                    new ScpiBean(":CALibrate:RESult?", ""),
//                    new ScpiBean(":CALibrate:ZERopoint", ""),
////                    new ScpiBean(":CALibrate:ZERopoint?", ""),
//                    new ScpiBean(":CALibrate:CHdf", ""),
////                    new ScpiBean(":CALibrate:CHdf?", ""),
//                    new ScpiBean(":CALibrate:ADPHa", ""),
////                    new ScpiBean(":CALibrate:ADPHa?", ""),
//                    new ScpiBean(":CALibrate:ADGain", ""),
////                    new ScpiBean(":CALibrate:ADGain?", ""),
//                    new ScpiBean(":CALibrate:OFFSet", ""),
////                    new ScpiBean(":CALibrate:OFFSet?", ""),
//                    new ScpiBean(":CALibrate:CHGain", ""),
////                    new ScpiBean(":CALibrate:CHGain?", ""),
//                    new ScpiBean(":CALibrate:TRIGger:ZERopoint", ""),
////                    new ScpiBean(":CALibrate:TRIGger:ZERopoint?", ""),
//                    new ScpiBean(":CALibrate:TRIGger:AC:ZERopoint", ""),
////                    new ScpiBean(":CALibrate:TRIGger:AC:ZERopoint?", ""),
//                    new ScpiBean(":CALibrate:TRIGger:COEFficient", ""),
////                    new ScpiBean(":CALibrate:TRIGger:COEFficient?", ""),
//                    new ScpiBean(":CALibrate:TRIGger:PRECise", ""),
////                    new ScpiBean(":CALibrate:TRIGger:PRECise?", ""),
////                    new ScpiBean(":CALibrate:DATE:LENGth?", ""),
//                    new ScpiBean(":CALibrate:DATE:GET", ""),
////                    new ScpiBean(":CALibrate:FILE:RESet?", ""),
//                    new ScpiBean(":WAVeform:BEGin", ""),
////                    new ScpiBean(":WAVeform:DATA?", ""),
//                    new ScpiBean(":WAVeform:END", ""),
//                    new ScpiBean(":WAVeform:MODE", ""),
////                    new ScpiBean(":WAVeform:MODE?", ""),
////                    new ScpiBean(":WAVeform:PREamble?", ""),
//                    new ScpiBean(":WAVeform:RESet", ""),
//                    new ScpiBean(":WAVeform:SOURce", ""),
////                    new ScpiBean(":WAVeform:SOURce?", ""),
//                    new ScpiBean(":WAVeform:STARt", ""),
////                    new ScpiBean(":WAVeform:STARt?", ""),
////                    new ScpiBean(":WAVeform:STATus?", ""),
//                    new ScpiBean(":WAVeform:STOP", ""),
////                    new ScpiBean(":WAVeform:STOP?", ""),
////                    new ScpiBean(":WAVeform:XINCrement?", ""),
////                    new ScpiBean(":WAVeform:XORigin?", ""),
////                    new ScpiBean(":WAVeform:XREFerence?", ""),
////                    new ScpiBean(":WAVeform:YINCrement?", ""),
////                    new ScpiBean(":WAVeform:YORigin?", ""),
////                    new ScpiBean(":WAVeform:YREFerence?", ""),
//                    new ScpiBean(":MENU:AUTO", ""),
//                    new ScpiBean(":MENU:RUN", ""),
//                    new ScpiBean(":MENU:STOP", ""),
//                    new ScpiBean(":MENU:SINGle", ""),
//                    new ScpiBean(":MENU:MULTiple", ""),
//                    new ScpiBean(":MENU:BEEP", ""),
//                    new ScpiBean(":MENU:HALF:CHANnel", ""),
//                    new ScpiBean(":MENU:HALF:TRIGpos", ""),
//                    new ScpiBean(":MENU:HALF:XCURsor", ""),
//                    new ScpiBean(":MENU:HALF:YCURsor", ""),
//                    new ScpiBean(":MENU:HALF:LEVel", ""),
//                    new ScpiBean(":MENU:HOMepage", ""),
//                    new ScpiBean(":MENU:RETurn", ""),
//                    new ScpiBean(":MENU:LOCK", ""),
//                    new ScpiBean(":MENU:UNLock", ""),
//                    new ScpiBean(":MENU:COUNter", ""),
////                    new ScpiBean(":MENU:COUNter?", ""),
//                    new ScpiBean(":MENU:RESet", ""),
//                    new ScpiBean(":MENU:MEASure", ""),
//                    new ScpiBean(":MENU:TRIGger", ""),
                    new ScpiBean(":SAMPle:TYPE", "MEAN"),//NORMal|MEAN|PEAK|ENVelop // 设置采样类型为平均
//                    new ScpiBean(":SAMPle:TYPE?", ""),
                    new ScpiBean(":SAMPle:MEAN", "256"),//type=MEAN时，取值=2|4|8|16|32|64|128|256 // 设置平均次数为256
//                    new ScpiBean(":SAMPle:MEAN?", ""),
                    new ScpiBean(":SAMPle:ENVelop", "INF"),//type=PEAK时，取值=2|4|8|16|32|64|128|256|inf // 设置包络次数为无穷
//                    new ScpiBean(":SAMPle:ENVelop?", ""),
//                    new ScpiBean(":SAMPle:SEGMented", ""),
////                    new ScpiBean(":SAMPle:SRATe?", ""),
////                    new ScpiBean(":SAMPle:MDEPth?", ""),
                    new ScpiBean(":CHANnel:DISPlay", "CH2,OFF"), // 关闭CH2通道显示
//                    new ScpiBean(":CHANnel:DISPlay?", ""),
                    new ScpiBean(":CHANnel:INVerse", "CH2,ON"), // 开启CH2反相
//                    new ScpiBean(":CHANnel:INVerse?", ""),
                    new ScpiBean(":CHANnel:INVert", "CH2,ON"), // 开启CH2反转
//                    new ScpiBean(":CHANnel:INVert?", ""),
                    new ScpiBean(":CHANnel:BAND", "CH2,HIGH,59.5"), // 设置CH2带宽限制
//                    new ScpiBean(":CHANnel:BAND?", ""),
                    new ScpiBean(":CHANnel:PRTY", "CH2,VOL"), // 设置CH2探针类型为电压
//                    new ScpiBean(":CHANnel:PRTY?", ""),
                    new ScpiBean(":CHANnel:PROBe", "CH2,0.02"), // 设置CH2探头衰减比
//                    new ScpiBean(":CHANnel:PROBe?", ""),
                    new ScpiBean(":CHANnel:COUPle", "CH2,GND"), // 设置CH2耦合方式为GND
//                    new ScpiBean(":CHANnel:COUPle?", ""),
//                    new ScpiBean(":CHANnel:INPutres", "CH2,MEGA"),
//                    new ScpiBean(":CHANnel:INPutres?", ""),
                    new ScpiBean(":CHANnel:EXTent", "CH2,0.005"), // 设置CH2垂直档位
                    new ScpiBean(":CHANnel:PLUS:EXTent", "CH2,1"), // 设置CH2微调垂直档位
//                    new ScpiBean(":CHANnel:EXTent?", ""),
                    new ScpiBean(":CHANnel:POSition", "CH2,350"), // 设置CH2垂直偏移
                    new ScpiBean(":CHANnel:PLUS:POSition", "CH2,-1"), // 设置CH2微调垂直偏移
//                    new ScpiBean(":CHANnel:POSition?", ""),
//                    new ScpiBean(":CHANnel:VERNier", "CH1,ON"),
//                    new ScpiBean(":CHANnel:VERNier?", ""),
                    new ScpiBean(":CHANnel2:DISPlay", "OFF"), // 关闭通道2显示
                    new ScpiBean(":CHANnel2:INVerse", "OFF"), // 关闭通道2反相
                    new ScpiBean(":CHANnel2:INVert", "OFF"), // 关闭通道2反转
                    new ScpiBean(":CHANnel2:PRTY", "VOL"), // 设置通道2探针类型为电压
                    new ScpiBean(":CHANnel2:PROBe", "0.02"), // 设置通道2探头衰减比
                    new ScpiBean(":CHANnel2:COUPle", "GND"), // 设置通道2耦合方式为GND
                    new ScpiBean(":CHANnel2:SCALe", "0.005"), // 设置通道2垂直档位
                    new ScpiBean(":CHANnel2:POSition", "250"), // 设置通道2垂直偏移
                    new ScpiBean(":CHANnel2:VERNier", "OFF"), // 关闭通道2微调
//                    new ScpiBean(":CHANnel2:PC", ""),
//                    new ScpiBean(":CHANnel2:INPutres", ""),
//                    new ScpiBean(":CHANnel2:DISPlay?", ""),
//                    new ScpiBean(":CHANnel2:INVerse?", ""),
//                    new ScpiBean(":CHANnel2:INVert?", ""),
//                    new ScpiBean(":CHANnel2:PRTY?", ""),
//                    new ScpiBean(":CHANnel2:PROBe?", ""),
//                    new ScpiBean(":CHANnel2:COUPle?", ""),
//                    new ScpiBean(":CHANnel2:SCALe?", ""),
//                    new ScpiBean(":CHANnel2:POSition?", ""),
//                    new ScpiBean(":CHANnel2:VERNier?", ""),
//                    new ScpiBean(":CHANnel2:PC?", ""),
//                    new ScpiBean(":CHANnel2:INPutres?", ""),
                    new ScpiBean(":MATH:DISPlay", "OFF"), // 关闭数学运算显示
//                    new ScpiBean(":MATH:DISPlay?", ""),
                    new ScpiBean(":MATH:MODE", "FFT"),//ADD|SUB|MUL|DIV|FFT // 设置数学运算模式为FFT
//                    new ScpiBean(":MATH:MODE?", ""),
                    new ScpiBean(":MATH:ADD:S1", "CH2"), // 设置加法运算信源1为CH2
//                    new ScpiBean(":MATH:ADD:S1?", ""),
                    new ScpiBean(":MATH:ADD:S2", "CH3"), // 设置加法运算信源2为CH3
//                    new ScpiBean(":MATH:ADD:S2?", ""),
//                    new ScpiBean(":MATH:ADD:EXTent", "0.002"),//0.002|0.005|0.01|0.02|0.05|0.1|0.2|0.5|1|2|5
//                    new ScpiBean(":MATH:ADD:PLUS:EXTent", "1"),
//                    new ScpiBean(":MATH:ADD:EXTent?", ""),
//                    new ScpiBean(":MATH:ADD:OFFSet", "21.3"),
//                    new ScpiBean(":MATH:ADD:PLUS:OFFSet", "-1"),
//                    new ScpiBean(":MATH:ADD:OFFSet?", ""),
                    new ScpiBean(":MATH:SUB:S1", "CH4"), // 设置减法运算信源1为CH4
//                    new ScpiBean(":MATH:SUB:S1?", ""),
                    new ScpiBean(":MATH:SUB:S2", "CH1"), // 设置减法运算信源2为CH1
//                    new ScpiBean(":MATH:SUB:S2?", ""),
//                    new ScpiBean(":MATH:SUB:EXTent", "0.01"),
//                    new ScpiBean(":MATH:SUB:PLUS:EXTent", "1"),
//                    new ScpiBean(":MATH:SUB:EXTent?", ""),
//                    new ScpiBean(":MATH:SUB:OFFSet", "56"),
//                    new ScpiBean(":MATH:SUB:PLUS:OFFSet", "-1"),
//                    new ScpiBean(":MATH:SUB:OFFSet?", ""),
                    new ScpiBean(":MATH:MUL:S1", "CH1"), // 设置乘法运算信源1为CH1
//                    new ScpiBean(":MATH:MUL:S1?", ""),
                    new ScpiBean(":MATH:MUL:S2", "CH2"), // 设置乘法运算信源2为CH2
//                    new ScpiBean(":MATH:MUL:S2?", ""),
//                    new ScpiBean(":MATH:MUL:EXTent", "0.1"),
//                    new ScpiBean(":MATH:MUL:PLUS:EXTent", "-1"),
//                    new ScpiBean(":MATH:MUL:EXTent?", ""),
//                    new ScpiBean(":MATH:MUL:OFFSet", "100"),
//                    new ScpiBean(":MATH:MUL:PLUS:OFFSet", "1"),
//                    new ScpiBean(":MATH:MUL:OFFSet?", ""),
                    new ScpiBean(":MATH:DIV:S1", "CH3"), // 设置除法运算信源1为CH3
//                    new ScpiBean(":MATH:DIV:S1?", ""),
                    new ScpiBean(":MATH:DIV:S2", "CH4"), // 设置除法运算信源2为CH4
//                    new ScpiBean(":MATH:DIV:S2?", ""),
//                    new ScpiBean(":MATH:DIV:EXTent", "2"),
//                    new ScpiBean(":MATH:DIV:PLUS:EXTent", "-1"),
//                    new ScpiBean(":MATH:DIV:EXTent?", ""),
//                    new ScpiBean(":MATH:DIV:OFFSet", "200"),
//                    new ScpiBean(":MATH:DIV:PLUS:OFFSet", "1"),
//                    new ScpiBean(":MATH:DIV:OFFSet?", ""),
                    new ScpiBean(":MATH:FFT:SOURce", "CH2"), // 设置FFT运算信源为CH2
//                    new ScpiBean(":MATH:FFT:SOURce?", ""),
                    new ScpiBean(":MATH:FFT:WINDow", "HAMMing"),//RECTangle|HAMMing|BLACkman|HANNing // 设置FFT窗函数为汉明窗
//                    new ScpiBean(":MATH:FFT:WINDow?", ""),
                    new ScpiBean(":MATH:FFT:TYPE", "DB"),//LINE|DB // 设置FFT显示方式为dB
//                    new ScpiBean(":MATH:FFT:TYPE?", ""),
//                    new ScpiBean(":MATH:FFT:EXTent", "0.002"),
//                    new ScpiBean(":MATH:FFT:PLUS:EXTent", "1"),
//                    new ScpiBean(":MATH:FFT:EXTent?", ""),
//                    new ScpiBean(":MATH:FFT:OFFSet", "111"),
//                    new ScpiBean(":MATH:FFT:PLUS:OFFSet", "1"),
//                    new ScpiBean(":MATH:FFT:OFFSet?", ""),
//                    new ScpiBean(":MATH:LOGic:S1", ""),
////                    new ScpiBean(":MATH:LOGic:S1?", ""),
//                    new ScpiBean(":MATH:LOGic:S2", ""),
////                    new ScpiBean(":MATH:LOGic:S2?", ""),
//                    new ScpiBean(":MATH:LOGic:OPERator", ""),
////                    new ScpiBean(":MATH:LOGic:OPERator?", ""),
//                    new ScpiBean(":MATH:LOGic:EXTent", ""),
////                    new ScpiBean(":MATH:LOGic:EXTent?", ""),
//                    new ScpiBean(":MATH:LOGic:OFFSet", ""),
////                    new ScpiBean(":MATH:LOGic:OFFSet?", ""),
//                    new ScpiBean(":MATH:FILTer:SOURce", ""),
////                    new ScpiBean(":MATH:FILTer:SOURce?", ""),
//                    new ScpiBean(":MATH:FILTer:FORMat", ""),
////                    new ScpiBean(":MATH:FILTer:FORMat?", ""),
//                    new ScpiBean(":MATH:FILTer:BANDwidth", ""),
////                    new ScpiBean(":MATH:FILTer:BANDwidth?", ""),
//                    new ScpiBean(":MATH:FILTer:EXTent", ""),
////                    new ScpiBean(":MATH:FILTer:EXTent?", ""),
//                    new ScpiBean(":MATH:FILTer:OFFSet", ""),
////                    new ScpiBean(":MATH:FILTer:OFFSet?", ""),
//                    new ScpiBean(":MATH:ADVanced:EXPRession", ""),
////                    new ScpiBean(":MATH:ADVanced:EXPRession?", ""),
//                    new ScpiBean(":MATH:ADVanced:VAR1", ""),
////                    new ScpiBean(":MATH:ADVanced:VAR1?", ""),
//                    new ScpiBean(":MATH:ADVanced:VAR2", ""),
////                    new ScpiBean(":MATH:ADVanced:VAR2?", ""),
//                    new ScpiBean(":MATH:ADVanced:EXTent", ""),
////                    new ScpiBean(":MATH:ADVanced:EXTent?", ""),
//                    new ScpiBean(":MATH:ADVanced:OFFSet", ""),
////                    new ScpiBean(":MATH:ADVanced:OFFSet?", ""),
                    new ScpiBean(":CURSor:HORizontal", "ON"), // 开启水平光标
//                    new ScpiBean(":CURSor:HORizontal?", ""),
                    new ScpiBean(":CURSor:VERTical", "ON"), // 开启垂直光标
//                    new ScpiBean(":CURSor:VERTical?", ""),
                    new ScpiBean(":CURSor:CX1", "100"), // 设置垂直光标A位置
                    new ScpiBean(":CURSor:PLUS:CXA", "1"), // 微调垂直光标A位置
//                    new ScpiBean(":CURSor:CX1?", ""),
                    new ScpiBean(":CURSor:CX2", "400"), // 设置垂直光标B位置
                    new ScpiBean(":CURSor:PLUS:CXB", "-1"), // 微调垂直光标B位置
//                    new ScpiBean(":CURSor:CX2?", ""),
                    new ScpiBean(":CURSor:CY1", "110"), // 设置水平光标A位置
                    new ScpiBean(":CURSor:PLUS:CYA", "-1"), // 微调水平光标A位置
//                    new ScpiBean(":CURSor:CY1?", ""),
                    new ScpiBean(":CURSor:CY2", "250"), // 设置水平光标B位置
                    new ScpiBean(":CURSor:PLUS:CYB", "1"), // 微调水平光标B位置
//                    new ScpiBean(":CURSor:CY2?", ""),
//                    new ScpiBean(":CURSor:X1Value?", ""),
//                    new ScpiBean(":CURSor:X2Value?", ""),
//                    new ScpiBean(":CURSor:Y1Value?", ""),
//                    new ScpiBean(":CURSor:Y2Value?", ""),
//                    new ScpiBean(":CURSor:XDELta?", ""),
//                    new ScpiBean(":CURSor:YDELta?", ""),
//                    new ScpiBean(":CURSor:RATio?", ""),
                    new ScpiBean(":CURSor:SOURce", "CH2"),//CH1|CH2|CH3|CH4| REF1| REF2| REF3| REF4|MATH // 设置光标测量源为CH2
//                    new ScpiBean(":CURSor:SOURce?", ""),
//                    new ScpiBean(":CURSor:FREQ?", ""),
                    new ScpiBean(":DISPlay:WAVeform", "DOTS"),//VECTors|DOTS // 设置波形显示方式为点状
//                    new ScpiBean(":DISPlay:WAVeform?", ""),
                    new ScpiBean(":DISPlay:BRIGhtness", "38"),//0至100 // 设置波形亮度为38
//                    new ScpiBean(":DISPlay:BRIGhtness?", ""),
                    new ScpiBean(":DISPlay:GRATicule", "GRID"),//FULL|GRID|RETical|FRAMe // 设置网格类型为GRID
//                    new ScpiBean(":DISPlay:GRATicule?", ""),
                    new ScpiBean(":DISPlay:INTensity", "50"),//整型，0至100 // 设置网格亮度为50
//                    new ScpiBean(":DISPlay:INTensity?", ""),
                    new ScpiBean(":DISPlay:PERSist:MODE", "NORMal"),//AUTO|NORMal|INFinite|none // 设置余辉模式为普通
//                    new ScpiBean(":DISPlay:PERSist:MODE?", ""),
                    new ScpiBean(":DISPlay:PERSist:ADJust", "500"),//单位ms，100,200,300,400,500,600,700,800,900,1000,2000,3000,4000,5000,6000,7000，8000，9000，10000 // 设置余辉时间为500ms
//                    new ScpiBean(":DISPlay:PERSist:ADJust?", ""),
                    new ScpiBean(":DISPlay:PERSist:CLEar", ""), // 清除余辉显示
                    new ScpiBean(":DISPlay:HIGH", "ON"), // 开启高刷新
//                    new ScpiBean(":DISPlay:HIGH?", ""),
                    new ScpiBean(":DISPlay:HORRef", "CENTER"),//CENTer|TRIGpos // 设置水平参考中心为中心
//                    new ScpiBean(":DISPlay:HORRef?", ""),
//                    new ScpiBean(":DISPlay:ZOOM", ""),//ON
//                    new ScpiBean(":DISPlay:ZOOM?", ""),
////                    new ScpiBean(":MEASure:PERiod?", ""),
////                    new ScpiBean(":MEASure:FREQuency?", ""),
////                    new ScpiBean(":MEASure:RISetime?", ""),
////                    new ScpiBean(":MEASure:FALLtime?", ""),
////                    new ScpiBean(":MEASure:DELay?", ""),
////                    new ScpiBean(":MEASure:PDUTy?", ""),
////                    new ScpiBean(":MEASure:NDUTy?", ""),
////                    new ScpiBean(":MEASure:PWIDth?", ""),
////                    new ScpiBean(":MEASure:NWIDth?", ""),
////                    new ScpiBean(":MEASure:BURStwidth?", ""),
////                    new ScpiBean(":MEASure:ROV?", ""),
////                    new ScpiBean(":MEASure:FOV?", ""),
////                    new ScpiBean(":MEASure:PHASe?", ""),
////                    new ScpiBean(":MEASure:PKPK?", ""),
////                    new ScpiBean(":MEASure:AMP?", ""),
////                    new ScpiBean(":MEASure:HIGH?", ""),
////                    new ScpiBean(":MEASure:LOW?", ""),
////                    new ScpiBean(":MEASure:MAX?", ""),
////                    new ScpiBean(":MEASure:MIN?", ""),
////                    new ScpiBean(":MEASure:RMS?", ""),
////                    new ScpiBean(":MEASure:CRMS?", ""),
////                    new ScpiBean(":MEASure:MEAN?", ""),
////                    new ScpiBean(":MEASure:CMEan?", ""),
////                    new ScpiBean(":MEASure:COLVal?", ""),
////                    new ScpiBean(":MEASure:AREa?", ""),
////                    new ScpiBean(":MEASure:CARea?", ""),
//                    new ScpiBean(":MEASure:CLEar", ""),//ITEM1|ITEM2|ITEM3|ITEM4|ITEM5|ALL
//                    new ScpiBean(":MEASure:OPEN", ""),
//                    new ScpiBean(":MEASure:CLOSe", ""),
//                    new ScpiBean(":MEASure:STATistic:DISPlay", ""),
////                    new ScpiBean(":MEASure:STATistic:DISPlay?", ""),
//                    new ScpiBean(":MEASure:STATistic:RESet", ""),
//                    new ScpiBean(":MEASure:ADISplay", ""),
////                    new ScpiBean(":MEASure:ADISplay?", ""),
//                    new ScpiBean(":MEASure:SCOPe", ""),
////                    new ScpiBean(":MEASure:SCOPe?", ""),
//                    new ScpiBean(":MEASure:SOURce", ""),
////                    new ScpiBean(":MEASure:SOURce?", ""),
////                    new ScpiBean(":MEASure:COUNter:VALue?", ""),
//                    new ScpiBean(":MEASure:ITEM", ""),
////                    new ScpiBean(":MEASure:ITEM?", ""),
                    new ScpiBean(":TRIGger:TYPE", "LOGIc"),//EDGE|PULSe|LOGIc|DWARtRunt|B|SLOPe|TIMEout|NEDGe|SETUp|VIDEo|UART|LIN|SPI|CAN|I2C|1553B|429 // 设置触发类型为逻辑触发
//                    new ScpiBean(":TRIGger:TYPE?", ""),
                    new ScpiBean(":TRIGger:HOLDoff", "0.000002"),//200ns至10s // 设置触发释抑时间为2μs
//                    new ScpiBean(":TRIGger:HOLDoff?", ""),
                    new ScpiBean(":TRIGger:MODE", "AUTO"),//AUTO|NORMal // 设置触发模式为自动
//                    new ScpiBean(":TRIGger:MODE?", ""),
//                    new ScpiBean(":TRIGger:STATus?", ""),//返回"RUN"、"WAIT"、"AUTO"、"STOP"
                    new ScpiBean(":TRIGger:EDGE:SOURce", "CH2"),//CH1|CH2|CH3|CH4 // 设置边沿触发源为CH2
//                    new ScpiBean(":TRIGger:EDGE:SOURce?", ""),
                    new ScpiBean(":TRIGger:EDGE:SLOPe", "FALL"),//RISE|FALL|DUAL // 设置边沿触发斜率为下降沿
//                    new ScpiBean(":TRIGger:EDGE:SLOPe?", ""),
                    new ScpiBean(":TRIGger:EDGE:LEVel", "220"), // 设置边沿触发电平为220
                    new ScpiBean(":TRIGger:EDGE:PLUS:LEVel", "1"), // 微调边沿触发电平
//                    new ScpiBean(":TRIGger:EDGE:LEVel?", ""),
                    new ScpiBean(":TRIGger:EDGE:COUPle", "AC"),//DC|AC|HFRej|LFRej|Noiserej // 设置边沿触发耦合为AC
//                    new ScpiBean(":TRIGger:EDGE:COUPle?", ""),
                    new ScpiBean(":TRIGger:PULSe:SOURce", "CH2"),//CH1|CH2|CH3|CH4 // 设置脉宽触发源为CH2
//                    new ScpiBean(":TRIGger:PULSe:SOURce?", ""),
                    new ScpiBean(":TRIGger:PULSe:POLarity", "NEGative"),//POSitive|NEGative // 设置脉宽触发极性为负
//                    new ScpiBean(":TRIGger:PULSe:POLarity?", ""),
                    new ScpiBean(":TRIGger:PULSe:WIDTh", "0.00000004"),//40ns至10s // 设置脉宽触发宽度为40ns
//                    new ScpiBean(":TRIGger:PULSe:WIDTh?", ""),
                    new ScpiBean(":TRIGger:PULSe:CONDition", "LESS"),//GREat|LESS|EQUal|UNEQual // 设置脉宽触发条件为小于
//                    new ScpiBean(":TRIGger:PULSe:CONDition?", ""),
                    new ScpiBean(":TRIGger:PULSe:LEVel", "330"), // 设置脉宽触发电平
                    new ScpiBean(":TRIGger:PULSe:PLUS:LEVel", "-1"), // 微调脉宽触发电平
//                    new ScpiBean(":TRIGger:PULSe:LEVel?", ""),
                    new ScpiBean(":TRIGger:LOGic:STATus", "CH2,LOW"),//CH1|CH2|CH3|CH4,HIGH|LOW|NONE // 设置逻辑触发CH2为低电平
//                    new ScpiBean(":TRIGger:LOGic:STATus?", ""),
                    new ScpiBean(":TRIGger:LOGic:FUNCtion", "OR"),//"AND"、"OR"、"NAND"或"NOR" // 设置逻辑触发函数为OR
//                    new ScpiBean(":TRIGger:LOGic:FUNCtion?", ""),
                    new ScpiBean(":TRIGger:LOGic:CONDition", "LESS"),//GREat|LESS|EQUal|UNEQual|TRUE|FALSe // 设置逻辑触发条件
//                    new ScpiBean(":TRIGger:LOGic:CONDition?", ""),
                    new ScpiBean(":TRIGger:LOGic:TIME", "0.0000002"),//200ns至10s // 设置逻辑触发时间
//                    new ScpiBean(":TRIGger:LOGic:TIME?", ""),
                    new ScpiBean(":TRIGger:LOGic:LEVel", "CH2,234"),//CH1|CH2|CH3|CH4,实型 // 设置逻辑触发CH2电平
                    new ScpiBean(":TRIGger:LOGic:PLUS:LEVel", "CH2,1"), // 微调逻辑触发电平
//                    new ScpiBean(":TRIGger:LOGic:LEVel?", ""),
//                    new ScpiBean(":TRIGger:B:SOURce", ""),
////                    new ScpiBean(":TRIGger:B:SOURce?", ""),
//                    new ScpiBean(":TRIGger:B:EDGE", ""),
////                    new ScpiBean(":TRIGger:B:EDGE?", ""),
//                    new ScpiBean(":TRIGger:B:COUPle", ""),
////                    new ScpiBean(":TRIGger:B:COUPle?", ""),
//                    new ScpiBean(":TRIGger:B:SEQuence", ""),
////                    new ScpiBean(":TRIGger:B:SEQuence?", ""),
//                    new ScpiBean(":TRIGger:B:LEVel", ""),
////                    new ScpiBean(":TRIGger:B:LEVel?", ""),
                    new ScpiBean(":TRIGger:RUNT:SOURce", "CH2"), // 设置矮脉宽触发源
//                    new ScpiBean(":TRIGger:RUNT:SOURce?", ""),
                    new ScpiBean(":TRIGger:RUNT:POLarity", "NEGAtive"),//POSItive|NEGAtive // 设置矮脉宽触发极性
//                    new ScpiBean(":TRIGger:RUNT:POLarity?", ""),
                    new ScpiBean(":TRIGger:RUNT:CONDition", "LESS"),//GREAt|LESS|BETWeen|NONE // 设置矮脉宽触发条件
//                    new ScpiBean(":TRIGger:RUNT:CONDition?", ""),
                    new ScpiBean(":TRIGger:RUNT:HTIMe", "0.1"),//8ns至10s // 设置矮脉宽触发时间上限
//                    new ScpiBean(":TRIGger:RUNT:HTIMe?", ""),
                    new ScpiBean(":TRIGger:RUNT:LTIMe", "0.0001"),//8ns至10s // 设置矮脉宽触发时间下限
//                    new ScpiBean(":TRIGger:RUNT:LTIMe?", ""),
                    new ScpiBean(":TRIGger:RUNT:BTIMe", "0.02,0.0002"),//8ns至10s // 设置矮脉宽触发时间区间
//                    new ScpiBean(":TRIGger:RUNT:BTIMe?", ""),
                    new ScpiBean(":TRIGger:RUNT:HLEVel", "400"), // 设置矮脉宽触发高电平
                    new ScpiBean(":TRIGger:RUNT:PLUS:HLEVel", "-1"), // 微调矮脉宽触发高电平
//                    new ScpiBean(":TRIGger:RUNT:HLEVel?", ""),
                    new ScpiBean(":TRIGger:RUNT:LLEVel", "50"), // 设置矮脉宽触发低电平
                    new ScpiBean(":TRIGger:RUNT:PLUS:LLEVel", "1"), // 微调矮脉宽触发低电平
//                    new ScpiBean(":TRIGger:RUNT:LLEVel?", ""),
                    new ScpiBean(":TRIGger:DWARt:SOURce", "CH2"), // 设置DWARt触发源
//                    new ScpiBean(":TRIGger:DWARt:SOURce?", ""),
                    new ScpiBean(":TRIGger:DWARt:POLarity", "NEGAtive"),//POSItive|NEGAtive // 设置DWARt触发极性
//                    new ScpiBean(":TRIGger:DWARt:POLarity?", ""),
                    new ScpiBean(":TRIGger:DWARt:CONDition", "LESS"),//GREAt|LESS|BETWeen|NONE // 设置DWARt触发条件
//                    new ScpiBean(":TRIGger:DWARt:CONDition?", ""),
                    new ScpiBean(":TRIGger:DWARt:HTIMe", "0.1"),//8ns至10s // 设置DWARt触发时间上限
//                    new ScpiBean(":TRIGger:DWARt:HTIMe?", ""),
                    new ScpiBean(":TRIGger:DWARt:LTIMe", "0.0001"),//8ns至10s // 设置DWARt触发时间下限
//                    new ScpiBean(":TRIGger:DWARt:LTIMe?", ""),
                    new ScpiBean(":TRIGger:DWARt:BTIMe", "0.02,0.0002"),//8ns至10s // 设置DWARt触发时间区间
//                    new ScpiBean(":TRIGger:DWARt:BTIMe?", ""),
                    new ScpiBean(":TRIGger:DWARt:HLEVel", "50"), // 设置DWARt触发高电平
                    new ScpiBean(":TRIGger:DWARt:PLUS:HLEVel", "-1"), // 微调DWARt触发高电平
//                    new ScpiBean(":TRIGger:DWARt:HLEVel?", ""),
                    new ScpiBean(":TRIGger:DWARt:LLEVel", "400"), // 设置DWARt触发低电平
                    new ScpiBean(":TRIGger:DWARt:PLUS:LLEVel", "1"), // 微调DWARt触发低电平
//                    new ScpiBean(":TRIGger:DWARt:LLEVel?", ""),
                    new ScpiBean(":TRIGger:SLOPe:SOURce", "CH2"), // 设置斜率触发源
//                    new ScpiBean(":TRIGger:SLOPe:SOURce?", ""),
                    new ScpiBean(":TRIGger:SLOPe:EDGE", "FALL"),//RISE|FALL|EITHer // 设置斜率触发边沿
//                    new ScpiBean(":TRIGger:SLOPe:EDGE?", ""),
                    new ScpiBean(":TRIGger:SLOPe:CONDition", "LESS"),//GREat|LESS|BETWeen // 设置斜率触发条件
//                    new ScpiBean(":TRIGger:SLOPe:CONDition?", ""),
                    new ScpiBean(":TRIGger:SLOPe:HTIMe", "0.1"),//8ns至10s // 设置斜率触发时间上限
//                    new ScpiBean(":TRIGger:SLOPe:HTIMe?", ""),
                    new ScpiBean(":TRIGger:SLOPe:LTIMe", "0.0001"),//8ns至10s // 设置斜率触发时间下限
//                    new ScpiBean(":TRIGger:SLOPe:LTIMe?", ""),
                    new ScpiBean(":TRIGger:SLOPe:BTIMe", "0.02,0.0002"),//8ns至10s // 设置斜率触发时间区间
//                    new ScpiBean(":TRIGger:SLOPe:BTIMe?", ""),
                    new ScpiBean(":TRIGger:SLOPe:HLEVel", "140"), // 设置斜率触发高电平
                    new ScpiBean(":TRIGger:SLOPe:PLUS:HLEVel", "-1"), // 微调斜率触发高电平
//                    new ScpiBean(":TRIGger:SLOPe:HLEVel?", ""),
                    new ScpiBean(":TRIGger:SLOPe:LLEVel", "250"), // 设置斜率触发低电平
                    new ScpiBean(":TRIGger:SLOPe:PLUS:LLEVel", "1"), // 微调斜率触发低电平
//                    new ScpiBean(":TRIGger:SLOPe:LLEVel?", ""),
                    new ScpiBean(":TRIGger:TIMeout:SOURce", "CH2"), // 设置超时触发源
//                    new ScpiBean(":TRIGger:TIMeout:SOURce?", ""),
                    new ScpiBean(":TRIGger:TIMeout:POLarity", "NEGative"),//POSitive|NEGative|EITHer // 设置超时触发极性
//                    new ScpiBean(":TRIGger:TIMeout:POLarity?", ""),
                    new ScpiBean(":TRIGger:TIMeout:TIME", "0.005"),//8ns至10s // 设置超时触发时间
//                    new ScpiBean(":TRIGger:TIMeout:TIME?", ""),
                    new ScpiBean(":TRIGger:NEDGe:SOURce", "CH2"), // 设置第N边沿触发源
//                    new ScpiBean(":TRIGger:NEDGe:SOURce?", ""),
                    new ScpiBean(":TRIGger:NEDGe:SLOPe", "FALL"),//RISE|FALL // 设置第N边沿触发斜率
//                    new ScpiBean(":TRIGger:NEDGe:SLOPe?", ""),
                    new ScpiBean(":TRIGger:NEDGe:IDLE", "0.03"),//8ns至10s // 设置第N边沿触发空闲时间
//                    new ScpiBean(":TRIGger:NEDGe:IDLE?", ""),
                    new ScpiBean(":TRIGger:NEDGe:EDGE", "33593"),//1至65535 // 设置第N边沿触发的N值
//                    new ScpiBean(":TRIGger:NEDGe:EDGE?", ""),
                    new ScpiBean(":TRIGger:NEDGe:LEVel", "333"), // 设置第N边沿触发电平
                    new ScpiBean(":TRIGger:NEDGe:PLUS:LEVel", "1"), // 微调第N边沿触发电平
//                    new ScpiBean(":TRIGger:NEDGe:LEVel?", ""),
//                    new ScpiBean(":TRIGger:SETup:CLOCk", ""),
////                    new ScpiBean(":TRIGger:SETup:CLOCk?", ""),
//                    new ScpiBean(":TRIGger:SETup:DATA", ""),
////                    new ScpiBean(":TRIGger:SETup:DATA?", ""),
//                    new ScpiBean(":TRIGger:SETup:CEDGe", ""),
////                    new ScpiBean(":TRIGger:SETup:CEDGe?", ""),
//                    new ScpiBean(":TRIGger:SETup:STIMe", ""),
////                    new ScpiBean(":TRIGger:SETup:STIMe?", ""),
//                    new ScpiBean(":TRIGger:SETup:HTIMe", ""),
////                    new ScpiBean(":TRIGger:SETup:HTIMe?", ""),
//                    new ScpiBean(":TRIGger:SETup:CLEVel", ""),
////                    new ScpiBean(":TRIGger:SETup:CLEVel?", ""),
//                    new ScpiBean(":TRIGger:SETup:DLEVel", ""),
////                    new ScpiBean(":TRIGger:SETup:DLEVel?", ""),
                    new ScpiBean(":TRIGger:VIDeo:SOURce", "CH2"), // 设置视频触发源
//                    new ScpiBean(":TRIGger:VIDeo:SOURce?", ""),
                    new ScpiBean(":TRIGger:VIDeo:POLarity", "NEGAtive"),//POSItive|NEGAtive // 设置视频触发极性
//                    new ScpiBean(":TRIGger:VIDeo:POLarity?", ""),
                    new ScpiBean(":TRIGger:VIDeo:STANdard", "1080I"),//PAL|SECAm|NTSC|720P|1080I|1080P // 设置视频触发标准
//                    new ScpiBean(":TRIGger:VIDeo:STANdard?", ""),
                    new ScpiBean(":TRIGger:VIDeo:AMODe", "EVENfield"),//触发标准为PAL、SECAm、NTSC、1080I时的同步类型ODDField|EVENfield|ALLField|ALLLine|LINE // 设置视频触发同步类型
//                    new ScpiBean(":TRIGger:VIDeo:AMODe?", ""),
                    new ScpiBean(":TRIGger:VIDeo:BMODe", "ALINe"),//触发标准为720P、1080P时的同步类型ALLField|ALLLine|LINE // 设置视频触发同步类型
//                    new ScpiBean(":TRIGger:VIDeo:BMODe?", ""),
                    new ScpiBean(":TRIGger:VIDeo:AFRequence", "50Hz"),//触发标准为720P、1080I时的信号频率60Hz|50Hz // 设置视频触发信号频率
//                    new ScpiBean(":TRIGger:VIDeo:AFRequence?", ""),
                    new ScpiBean(":TRIGger:VIDeo:BFRequence", "25Hz"),//触发标准为1080P时的信号频率60Hz|50Hz|30Hz|25Hz|24Hz // 设置视频触发信号频率
//                    new ScpiBean(":TRIGger:VIDeo:BFRequence?", ""),
//                    new ScpiBean(":TRIGger:UART:SOURce", ""),//S1|S2
////                    new ScpiBean(":TRIGger:UART:SOURce?", ""),
//                    new ScpiBean(":TRIGger:UART:TYPE", ""),
////                    new ScpiBean(":TRIGger:UART:TYPE?", ""),
//                    new ScpiBean(":TRIGger:UART:RELation", ""),
////                    new ScpiBean(":TRIGger:UART:RELation?", ""),
//                    new ScpiBean(":TRIGger:UART:DATA", ""),
////                    new ScpiBean(":TRIGger:UART:DATA?", ""),
//                    new ScpiBean(":TRIGger:UART:LEVel", ""),
////                    new ScpiBean(":TRIGger:UART:LEVel?", ""),
//                    new ScpiBean(":TRIGger:LIN:SOURce", ""),
////                    new ScpiBean(":TRIGger:LIN:SOURce?", ""),
//                    new ScpiBean(":TRIGger:LIN:TYPE", ""),
////                    new ScpiBean(":TRIGger:LIN:TYPE?", ""),
//                    new ScpiBean(":TRIGger:LIN:ID", ""),
////                    new ScpiBean(":TRIGger:LIN:ID?", ""),
//                    new ScpiBean(":TRIGger:LIN:DATA", ""),
////                    new ScpiBean(":TRIGger:LIN:DATA?", ""),
//                    new ScpiBean(":TRIGger:LIN:LEVel", ""),
////                    new ScpiBean(":TRIGger:LIN:LEVel?", ""),
//                    new ScpiBean(":TRIGger:CAN:SOURce", ""),
////                    new ScpiBean(":TRIGger:CAN:SOURce?", ""),
//                    new ScpiBean(":TRIGger:CAN:TYPE", ""),
////                    new ScpiBean(":TRIGger:CAN:TYPE?", ""),
//                    new ScpiBean(":TRIGger:CAN:ID", ""),
////                    new ScpiBean(":TRIGger:CAN:ID?", ""),
//                    new ScpiBean(":TRIGger:CAN:DLC", ""),
////                    new ScpiBean(":TRIGger:CAN:DLC?", ""),
//                    new ScpiBean(":TRIGger:CAN:DATA", ""),
////                    new ScpiBean(":TRIGger:CAN:DATA?", ""),
//                    new ScpiBean(":TRIGger:CAN:LEVel", ""),
////                    new ScpiBean(":TRIGger:CAN:LEVel?", ""),
//                    new ScpiBean(":TRIGger:SPI:DATA", ""),
////                    new ScpiBean(":TRIGger:SPI:DATA?", ""),
//                    new ScpiBean(":TRIGger:SPI:SOURce", ""),
////                    new ScpiBean(":TRIGger:SPI:SOURce?", ""),
//                    new ScpiBean(":TRIGger:SPI:LEVel", ""),
////                    new ScpiBean(":TRIGger:SPI:LEVel?", ""),
//                    new ScpiBean(":TRIGger:IIC:SOURce", ""),
////                    new ScpiBean(":TRIGger:IIC:SOURce?", ""),
//                    new ScpiBean(":TRIGger:IIC:TYPE", ""),
////                    new ScpiBean(":TRIGger:IIC:TYPE?", ""),
//                    new ScpiBean(":TRIGger:IIC:ADDRess", ""),
////                    new ScpiBean(":TRIGger:IIC:ADDRess?", ""),
//                    new ScpiBean(":TRIGger:IIC:RELation", ""),
////                    new ScpiBean(":TRIGger:IIC:RELation?", ""),
//                    new ScpiBean(":TRIGger:IIC:DATA", ""),
////                    new ScpiBean(":TRIGger:IIC:DATA?", ""),
//                    new ScpiBean(":TRIGger:IIC:LEVel", ""),
////                    new ScpiBean(":TRIGger:IIC:LEVel?", ""),
//                    new ScpiBean(":TIMebase:EXTent", ""),
//                    new ScpiBean(":TIMebase:PLUS:EXTent", ""),
////                    new ScpiBean(":TIMebase:EXTent?", ""),
//                    new ScpiBean(":TIMebase:MODE", ""),
////                    new ScpiBean(":TIMebase:MODE?", ""),
//                    new ScpiBean(":TIMebase:XY1:DISPlay", ""),
////                    new ScpiBean(":TIMebase:XY1:DISPlay?", ""),
//                    new ScpiBean(":TIMebase:POSition", ""),
//                    new ScpiBean(":TIMebase:OFFSet", ""),
//                    new ScpiBean(":TIMebase:PLUS:OFFSet", ""),
//                    new ScpiBean(":TIMebase:PLUS:POSition", ""),
////                    new ScpiBean(":TIMebase:POSition?", ""),
////                    new ScpiBean(":TIMebase:OFFSet?", ""),
                    new ScpiBean(":STORage:SAVE", "CH2,UDISk"),//CH1|CH2|CH3|CH4|MATH,LOCal|UDISk // 保存CH2波形到U盘
                    new ScpiBean(":STORage:LOAD", "REF2,ON"),//REF1| REF2| REF3| REF4 // 载入REF2参考波形
                    new ScpiBean(":STORage:CAPTure", ""), // 屏幕截图
//                    new ScpiBean(":STORage:DEPTh", ""),
//                    new ScpiBean(":STORage:DEPTh?", ""),
//                    new ScpiBean(":STORage:CONSave", ""),
//                    new ScpiBean(":STORage:CONLoad", ""),
                    new ScpiBean(":STORage:RECord", "STOP"),//RECOrd|STOP // 停止录制
//                    new ScpiBean(":STORage:RECord?", ""),
//                    new ScpiBean(":STORage:PLAY", "STOP"),//RECOrd|STOP
////                    new ScpiBean(":STORage:PLAY?", ""),
//                    new ScpiBean(":STORage:PLAY:SPEed", ""),//2X|4X|8X|16X|32X|64X
////                    new ScpiBean(":STORage:PLAY:SPEed?", ""),
//                    new ScpiBean(":STORage:PLAY:BACK", ""),//2X|4X|8X|16X|32X|64X
////                    new ScpiBean(":STORage:PLAY:BACK?", ""),
//                    new ScpiBean(":MASK:SOURce", ""),
////                    new ScpiBean(":MASK:SOURce?", ""),
//                    new ScpiBean(":MASK:RANGe", ""),
////                    new ScpiBean(":MASK:RANGe?", ""),
//                    new ScpiBean(":MASK:STATistic", ""),
////                    new ScpiBean(":MASK:STATistic?", ""),
//                    new ScpiBean(":MASK:RESet", ""),
//                    new ScpiBean(":MASK:SOOutput", ""),
////                    new ScpiBean(":MASK:SOOutput?", ""),
//                    new ScpiBean(":MASK:AUXout", ""),
////                    new ScpiBean(":MASK:AUXout?", ""),
//                    new ScpiBean(":MASK:ENABle", ""),
////                    new ScpiBean(":MASK:ENABle?", ""),
//                    new ScpiBean(":MASK:OPERate", ""),
////                    new ScpiBean(":MASK:OPERate?", ""),
//                    new ScpiBean(":MASK:X", ""),
////                    new ScpiBean(":MASK:X?", ""),
//                    new ScpiBean(":MASK:Y", ""),
////                    new ScpiBean(":MASK:Y?", ""),
                    new ScpiBean(":REFerence:DISPlay", "OFF"), // 关闭参考波形显示
//                    new ScpiBean(":REFerence:DISPlay?", ""),
                    new ScpiBean(":REFerence:ENABle", "REF2,OFF"),//REF1| REF2| REF3| REF4 // 关闭REF2参考通道
//                    new ScpiBean(":REFerence:ENABle?", ""),
//                    new ScpiBean(":REFerence:HSCale", "REF2,1"),//REF1| REF2| REF3| REF4,1ns~1ks
//                    new ScpiBean(":REFerence:PLUS:HSCale", ""),
//                    new ScpiBean(":REFerence:HSCale?", ""),
//                    new ScpiBean(":REFerence:VSCale", "REF2,2"),//REF1| REF2| REF3| REF4,5mV~5V
//                    new ScpiBean(":REFerence:PLUS:VSCale", ""),
//                    new ScpiBean(":REFerence:VSCale?", ""),
//                    new ScpiBean(":REFerence:CURRent", ""),
//                    new ScpiBean(":REFerence:PLUS:HOFFset", ""),
//                    new ScpiBean(":REFerence:PLUS:VOFFset", ""),
//                    new ScpiBean(":REFerence:POSition", ""),
//                    new ScpiBean(":REFerence:TIMebase:POSition", ""),
//                    new ScpiBean(":REFerence:PLUS:TIMebase:POSition", ""),
//                    new ScpiBean(":REFerence:PLUS:POSition", ""),
                    new ScpiBean(":AUTO:SET:CHANnel", "ON"), // 设置自动打开通道
//                    new ScpiBean(":AUTO:SET:CHANnel?", ""),
                    new ScpiBean(":AUTO:SET:LEVEl", "0.01"),//0.001V~99V // 设置门限电平为0.01V
//                    new ScpiBean(":AUTO:SET:LEVEl?", ""),
                    new ScpiBean(":AUTO:SET:SOURce", "MAX"),//CURrent|MAX // 设置触发源为最大值
//                    new ScpiBean(":AUTO:SET:SOURce?", ""),
                    new ScpiBean(":AUTO:RANge", "ON"), // 开启自动量程
//                    new ScpiBean(":AUTO:RANge?", ""),
                    new ScpiBean(":AUTO:RANge:VERtical", "ON"), // 开启自动垂直量程
//                    new ScpiBean(":AUTO:RANge:VERtical?", ""),
                    new ScpiBean(":AUTO:RANge:HORizoncal", "ON"), // 开启自动水平量程
//                    new ScpiBean(":AUTO:RANge:HORizoncal?", ""),
                    new ScpiBean(":AUTO:RANge:LEVEl", "ON"), // 开启自动量程电平
//                    new ScpiBean(":AUTO:RANge:LEVEl?", "")
            };
            //endregion
        }
    }

    /** 当前遍历索引，指向scpis数组中的当前位置 */
    private int i = 0; // 当前测试命令索引

    /**
     * 获取当前索引位置的SCPI命令字符串。
     * 以"命令 参数\r\n"格式返回，同时输出日志。
     * @return 当前SCPI命令字符串
     */
    public String getSCPICurrent() {
        Logger.i("TestSCPI:" + i + " :" + scpis[i].function + " " + scpis[i].params); // 记录当前测试命令日志
        return scpis[i].function + " " + scpis[i].params + "\r\n"; // 拼接SCPI命令字符串并添加回车换行
    }

    /**
     * 获取下一条SCPI命令字符串（索引前进1）。
     * 到达末尾后循环回到索引0。
     * @return 下一条SCPI命令字符串
     */
    public String getSCPINext() {
        if (i >= scpis.length - 1) { // 判断是否已到达数组末尾
            i = 0; // 循环回到数组起始位置
        } else {
            i++; // 索引前进1
        }
        Logger.i("TestSCPI:" + i + " :" + scpis[i].function + " " + scpis[i].params); // 记录下一条测试命令日志
        return scpis[i].function + " " + scpis[i].params + "\r\n"; // 拼接SCPI命令字符串并添加回车换行
    }

    /**
     * 获取上一条SCPI命令字符串（索引后退1）。
     * 到达起始后循环回到末尾索引。
     * @return 上一条SCPI命令字符串
     */
    public String getSCPIPrev() {
        if (i <= 0) { // 判断是否已在数组起始位置
            i = scpis.length - 1; // 循环回到数组末尾位置
        } else {
            i--; // 索引后退1
        }
        Logger.i("TestSCPI:" + i + " :" + scpis[i].function + " " + scpis[i].params); // 记录上一条测试命令日志
        return scpis[i].function + " " + scpis[i].params + "\r\n"; // 拼接SCPI命令字符串并添加回车换行
    }

    /**
     * SCPI命令Bean，封装一条SCPI命令的function(命令头)和params(参数)。
     */
    class ScpiBean {
        /** SCPI命令头，如":TRIGger:TYPE"、":CHANnel:DISPlay" */
        String function; // SCPI命令头
        /** SCPI命令参数，如"EDGE"、"CH2,OFF" */
        String params; // SCPI命令参数

        /**
         * 构造ScpiBean。
         * @param function SCPI命令头
         * @param params SCPI命令参数
         */
        ScpiBean(String function, String params) {
            this.function = function; // 赋值命令头
            this.params = params; // 赋值命令参数
        }
    }
}
