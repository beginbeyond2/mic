package com.micsig.tbook.tbookscope.scpi;

/**
 * Created by liwb on 2018/8/9.
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |               ToolsSCPI - SCPI枚举值与索引映射工具类                     |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系中枚举字符串与整型索引的双向映射工具层                  |   *
 *   |  核心职责: 维护所有SCPI协议枚举值的字符串数组，提供索引→字符串的getter方法       |   *
 *   |  架构设计: 纯静态工具类，每个枚举类型对应一个static final String[]数组和一个     |   *
 *   |           getXxx(int index)静态getter方法，通过数组下标直接映射              |   *
 *   |  数据流向: SCPI_xxx命令处理类的查询方法 → 本类getter → 返回SCPI协议规定的枚举字符串|   *
 *   |  依赖关系: 被所有SCPI_xxx命令处理类（SCPI_Auto/SCPI_Bus_xxx/SCPI_Trigger等）引用|   *
 *   |  使用场景: 查询类SCPI命令需要将设备层返回的整型索引转换为SCPI协议规定的枚举字符串   |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class ToolsSCPI {

    /**
     * 检查整型索引是否在int数组的有效范围内。
     * @param index 待检查的索引值
     * @param src   目标int数组
     * @return true=索引有效(0 ≤ index < length)，false=越界
     */
    public static boolean isCorrect(int index, int[] src) {
        return index >= 0 && index < src.length; // 判断索引是否在[0, length)范围内
    }

    /**
     * 检查整型索引是否在double数组的有效范围内。
     * @param index 待检查的索引值
     * @param src   目标double数组
     * @return true=索引有效，false=越界
     */
    public static boolean isCorrect(int index, double[] src) {
        return index >= 0 && index < src.length; // 判断索引是否在[0, length)范围内
    }

    /**
     * 检查整型索引是否在long数组的有效范围内。
     * @param index 待检查的索引值
     * @param src   目标long数组
     * @return true=索引有效，false=越界
     */
    public static boolean isCorrect(int index, long[] src) {
        return index >= 0 && index < src.length; // 判断索引是否在[0, length)范围内
    }

    /**
     * 检查整型索引是否在boolean数组的有效范围内。
     * @param index 待检查的索引值
     * @param src   目标boolean数组
     * @return true=索引有效，false=越界
     */
    public static boolean isCorrect(int index, boolean[] src) {
        return index >= 0 && index < src.length; // 判断索引是否在[0, length)范围内
    }

    /**
     * 获取SCPI命令执行成功的标准响应字符串"OKAY"。
     * @return 固定字符串"OKAY"
     */
    public static String getOKAY(){return "OKAY";} // 返回SCPI标准成功响应

    /**
     * 将布尔值转换为成功/失败状态字符串。
     * @param b 布尔状态值
     * @return "successful"或"failed"
     */
    public static String getSuccState(boolean b){return b?"successful":"failed";} // 布尔→成功/失败字符串

    /**
     * 将布尔开关状态转换为整数字符串"1"或"0"。
     * @param b 布尔开关状态
     * @return "1"(开启)或"0"(关闭)
     */
    public static String getOpenStateToInt(boolean b){return b?"1":"0";} // 布尔→"1"/"0"整数字符串

//    public static String getOpenState(boolean b){
//        return b?"1|ON":"0|OFF";
//    }

    /**
     * 将布尔开关状态转换为SCPI协议规定的"1"或"0"字符串。
     * 大多数SCPI查询命令(如通道开关、自动量程等)使用此格式返回。
     * @param b 布尔开关状态
     * @return "1"(开启)或"0"(关闭)
     */
    public static String getOpenState(boolean b){
    return b?"1":"0"; // 布尔→SCPI标准"1"/"0"开关字符串
}

    /**
     * 将double数值转换为字符串，用于浮点型查询结果的SCPI响应。
     * @param d 双精度浮点值
     * @return 数值的字符串表示
     */
    public static String getDouble(double d){
        return String.valueOf(d); // double→字符串
    }

    /**
     * 直接返回字符串本身，用于字符串型查询结果的透传。
     * @param s 原始字符串
     * @return 原样返回
     */
    public static String getString(String s){return s;} // 字符串透传

    /**
     * 将int数值转换为字符串，用于整型查询结果的SCPI响应。
     * @param i 整型值
     * @return 数值的字符串表示
     */
    public static String getInt(int i){return String.valueOf(i);} // int→字符串

    /**
     * 获取未实现SCPI命令的提示字符串。
     * @return 固定字符串"Unrealized SCPI"
     */
    public static String getUnSCPI(){return "Unrealized SCPI";} // 返回未实现命令提示

    /** 模拟通道名称数组: CH1~CH8 */
    private static final String[] Ch = {"CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8"}; // 通道名称枚举
    /**
     * 根据索引获取模拟通道名称(如"CH1"、"CH2")。
     * @param index 通道索引(0=CH1, 1=CH2, ...)
     * @return 通道名称字符串
     */
    public static String getCh(int index){ return Ch[index];} // 索引→通道名称

    /** 全部通道/源名称数组: CH1~CH8, MATH1~MATH8, R1~R8, S1~S4, OFF, EXT */
    private static final String[] chAll = { // 全部可用通道/参考/数学运算/串行总线源名称
            "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8", // 模拟通道CH1~CH8
            "MATH1", "MATH2", "MATH3", "MATH4", "MATH5", "MATH6", "MATH7", "MATH8", // 数学运算通道MATH1~MATH8
            "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", // 参考通道R1~R8
            "S1", "S2", "S3", "S4","OFF","EXT"}; // 串行总线S1~S4、关闭、外部触发源
    /**
     * 根据索引获取全部通道/源名称(含CH/MATH/R/S/OFF/EXT)。
     * @param index 索引值
     * @return 通道/源名称字符串
     */
    public static String getChAll(int index){return chAll[index];} // 索引→全部通道名称

    /** 计数器通道名称数组: CLOSe, CH1~CH8 */
    private static final String[] counter_ch = {"CLOSe", "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8"}; // 计数器通道枚举(含关闭选项)
    /**
     * 根据索引获取计数器通道名称。
     * @param index 索引(0=CLOSe, 1=CH1, ...)
     * @return 通道名称字符串
     */
    public static String getCounterCh(int index){return counter_ch[index];} // 索引→计数器通道名称

    /** 带宽限制枚举数组: FULL/200M/20M/HIGH/LOW/NULL */
    private static final String[] band={"FULL","200M","20M","HIGH","LOW","NULL"}; // 带宽限制模式枚举
    /**
     * 根据索引获取带宽限制模式名称。
     * @param index 索引(0=FULL, 1=200M, 2=20M, ...)
     * @return 带宽限制名称字符串
     */
    public static String getBand(int index){ return band[index];} // 索引→带宽限制名称

    /** 电流/电压优先级枚举数组: VOL/CUR */
    private static final String[] prty={"VOL","CUR"}; // 探头优先级枚举(电压/电流)
    /**
     * 根据索引获取探头优先级名称。
     * @param index 索引(0=VOL, 1=CUR)
     * @return 优先级名称字符串
     */
    public static String getPrty(int index){return prty[index];} // 索引→优先级名称

    /** 探头衰减比枚举数组: 0.001~10000(共22档) */
    private static final String[] probe={"0.001","0.002","0.005","0.01","0.02","0.05", // 探头衰减比枚举(小衰减比)
            "0.1","0.2","0.5","1","2","5","10","20","50","100","200","500", // 探头衰减比枚举(常用衰减比)
            "1000","2000","5000","10000"}; // 探头衰减比枚举(大衰减比)
    /**
     * 根据索引获取探头衰减比字符串。
     * @param index 索引(0=0.001, ..., 9=1, ..., 21=10000)
     * @return 衰减比字符串
     */
    public static  String getProbe(int index){return probe[index];} // 索引→探头衰减比

    /** 耦合方式枚举数组: DC/AC/GND */
    private static final String[] couple={"DC","AC","GND"}; // 耦合方式枚举(直流/交流/接地)
    /**
     * 根据索引获取耦合方式名称。
     * @param index 索引(0=DC, 1=AC, 2=GND)
     * @return 耦合方式名称字符串
     */
    public static String getCouple(int index){ return couple[index];} // 索引→耦合方式名称

    /** 输入阻抗枚举数组: MEGA(1MΩ)/FIFTy(50Ω) */
    private static final String[] inputres={"MEGA","FIFTy"}; // 输入阻抗枚举(1MΩ/50Ω)
    /**
     * 根据索引获取输入阻抗名称。
     * @param index 索引(0=MEGA, 1=FIFTy)
     * @return 输入阻抗名称字符串
     */
    public static String getInputres(int index){return inputres[index];} // 索引→输入阻抗名称

    /** 采样模式枚举数组: NORMal/MEAN/ENVelop/PEAK/HIGHres/SEGMented */
    private static final String[] sampleType={"NORMal","MEAN","ENVelop","PEAK","HIGHres","SEGMented"}; // 采样模式枚举(正常/平均/包络/峰值/高分辨率/分段)
    /**
     * 根据索引获取采样模式名称。
     * @param index 索引(0=NORMal, 1=MEAN, 2=ENVelop, ...)
     * @return 采样模式名称字符串
     */
    public static String getSampleType(int index){return sampleType[index];}; // 索引→采样模式名称

    /** 包络采样数枚举数组: 2/4/8/16/32/64/128/256/inf */
    private static final String[] sampleEnvelop={"2","4","8","16","32","64","128","256","inf"}; // 包络采样数枚举
    /**
     * 根据索引获取包络采样数字符串。
     * @param index 索引
     * @return 包络采样数字符串
     */
    public static String getSampleEnvelop(int index){return sampleEnvelop[index];} // 索引→包络采样数

    /** 数学运算模式枚举数组: BASE/FFT/AX+B/ADVAnced */
    private static final String[] mathMode={"BASE","FFT", " AX+B","ADVAnced"}; // 数学运算模式枚举(基础/FFT/线性/高级)
    /**
     * 根据索引获取数学运算模式名称。
     * @param index 索引(0=BASE, 1=FFT, 2=AX+B, 3=ADVAnced)
     * @return 数学运算模式名称字符串
     */
    public static String getMathMode(int index){return  mathMode[index];} // 索引→数学运算模式名称

    /** 数学运算垂直参考枚举数组: Center/Zero */
    private static final String[] mathVREF={"Center","Zero"}; // 数学运算垂直参考枚举(中心/零点)
    /**
     * 根据索引获取数学运算垂直参考名称。
     * @param index 索引(0=Center, 1=Zero)
     * @return 垂直参考名称字符串
     */
    public static String getMathVRef(int index){return mathVREF[index];} // 索引→垂直参考名称

    /** 数学基础运算符枚举数组: ADD/SUB/MUL/DIV */
    private static final String[] mathBaseOperator={"ADD","SUB","MUL","DIV"}; // 基础运算符枚举(加/减/乘/除)
    /**
     * 根据索引获取数学基础运算符名称。
     * @param index 索引(0=ADD, 1=SUB, 2=MUL, 3=DIV)
     * @return 运算符名称字符串
     */
    public static String getMathBaseOperator(int index){return mathBaseOperator[index];} // 索引→基础运算符名称

    /** FFT窗函数枚举数组: RECTangle/HAMMing/BLACkman/HANNing */
    private static final String[] mathWindow={"RECTangle","HAMMing","BLACkman","HANNing"}; // FFT窗函数枚举(矩形/海明/布莱克曼/汉宁)
    /**
     * 根据索引获取FFT窗函数名称。
     * @param index 索引(0=RECTangle, 1=HAMMing, 2=BLACkman, 3=HANNing)
     * @return 窗函数名称字符串
     */
    public static String getMathWindow(int index){return mathWindow[index];} // 索引→FFT窗函数名称

    /** FFT显示类型枚举数组: LINE/DB */
    private static final String[] mathFftType={"LINE","DB"}; // FFT显示类型枚举(线性/对数)
    /**
     * 根据索引获取FFT显示类型名称。
     * @param index 索引(0=LINE, 1=DB)
     * @return FFT显示类型名称字符串
     */
    public static String getMathFftType(int index){return mathFftType[index];} // 索引→FFT显示类型名称

    /** 波形显示格式枚举数组: DOTS/VECTors */
    private static final String[] displayWaveForm={"DOTS","VECTors"}; // 波形显示格式枚举(点/矢量)
    /**
     * 根据索引获取波形显示格式名称。
     * @param index 索引(0=DOTS, 1=VECTors)
     * @return 波形显示格式名称字符串
     */
    public static String getDisplayWaveForm(int index){ return displayWaveForm[index];} // 索引→波形显示格式名称

    /** 背景样式枚举数组: Dark/Light */
    private static final String[] displayBackground={"Dark","Light"}; // 背景样式枚举(深色/浅色)

    /**
     * 根据索引获取背景样式名称。
     * @param index 索引(0=Dark, 1=Light)
     * @return 背景样式名称字符串
     */
    public static String getDisplayBackground(int index){return displayBackground[index];} // 索引→背景样式名称

    /** 网格类型枚举数组: FULL/GRID/RETical/FRAMe */
    private static final String[] displayGraticule={"FULL","GRID","RETical","FRAMe"}; // 网格类型枚举(全/网格/十字/边框)
    /**
     * 根据索引获取网格类型名称。
     * @param index 索引(0=FULL, 1=GRID, 2=RETical, 3=FRAMe)
     * @return 网格类型名称字符串
     */
    public static String getDisplayGraticule(int index){ return displayGraticule[index];    } // 索引→网格类型名称

    /** 普通波形持续模式枚举数组: none/AUTO/NORMal/INFinite */
    private static final String[] displayPersistMode={"none","AUTO","NORMal","INFinite"}; // 持续模式枚举(无/自动/正常/无限)
    /**
     * 根据索引获取持续模式名称。
     * @param index 索引(0=none, 1=AUTO, 2=NORMal, 3=INFinite)
     * @return 持续模式名称字符串
     */
    public static String getDisplayPersistMode(int index){return displayPersistMode[index];} // 索引→持续模式名称

    /** FFT波形持续模式枚举数组: none/AUTO/NORMal/INFinite */
    private static final String[] displayFftPersistMode={"none","AUTO","NORMal","INFinite"}; // FFT持续模式枚举(同普通持续模式)
    /**
     * 根据索引获取FFT持续模式名称。
     * @param index 索引
     * @return FFT持续模式名称字符串
     */
    public static String getDisplayFftPersistMode(int index){return displayFftPersistMode[index];} // 索引→FFT持续模式名称

    /** 普通波形持续调节时间数组: 100~10000ms(19档) */
    public static final Integer[] displayPersistAdjust={100,200,300,400,500,600,700,800,900,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000}; // 持续调节时间枚举(ms)
    /**
     * 根据索引获取持续调节时间字符串。
     * @param index 索引
     * @return 持续调节时间字符串(单位ms)
     */
    public static String getDisplayPersistAdjust(int index){ return  String.valueOf(displayPersistAdjust[index]);} // 索引→持续调节时间字符串

    /** FFT波形持续调节时间数组: 200/500/1000/2000/5000/10000ms(6档) */
    public static final Integer[] displayFftPersistAdjust = {200, 500, 1000, 2000, 5000, 10000}; // FFT持续调节时间枚举(ms)
    /**
     * 根据索引获取FFT持续调节时间字符串。
     * @param index 索引
     * @return FFT持续调节时间字符串(单位ms)
     */
    public static String getDisplayFftPersistAdjust(int index){ return  String.valueOf(displayFftPersistAdjust[index]);} // 索引→FFT持续调节时间字符串

    /** 水平参考枚举数组: CENTer/TRIGpos */
    private static final String[] displayHorRef={"CENTer","TRIGpos"}; // 水平参考枚举(中心/触发位置)
    /**
     * 根据索引获取水平参考名称。
     * @param index 索引(0=CENTer, 1=TRIGpos)
     * @return 水平参考名称字符串
     */
    public static String getDisplayHorRef(int index){ return displayHorRef[index];} // 索引→水平参考名称

    /** 触发类型枚举数组: COMMon/EDGE/PULSe/LOGic/NEDGe/RUNT/SLOPe/TIMeout/VIDeo/S1~S4 */
    private static final String[] triggerType={"COMMon","EDGE", "PULSe", "LOGic", "NEDGe", "RUNT", "SLOPe", "TIMeout", "VIDeo","S1","S2","S3","S4"}; // 触发类型枚举(通用/边沿/脉冲/逻辑/第N边沿/毛刺/斜率/超时/视频/串行S1~S4)
    /**
     * 根据索引获取触发类型名称。
     * @param index 索引(0=COMMon, 1=EDGE, 2=PULSe, ...)
     * @return 触发类型名称字符串
     */
    public static String getTriggerType(int index){return  triggerType[index];} // 索引→触发类型名称

    /** 触发模式枚举数组: AUTO/NORMal */
    private static final String[] triggerMode={"AUTO","NORMal"}; // 触发模式枚举(自动/正常)
    /**
     * 根据索引获取触发模式名称。
     * @param index 索引(0=AUTO, 1=NORMal)
     * @return 触发模式名称字符串
     */
    public static String getTriggerMode(int index){return  triggerMode[index];} // 索引→触发模式名称

    /** 触发状态枚举数组: STOP/RUN/WAIT/AUTO */
    private static final String[] triggerStatus={"STOP","RUN","WAIT","AUTO"}; // 触发状态枚举(停止/运行/等待/自动)
    /**
     * 根据索引获取触发状态名称。
     * @param index 索引(0=STOP, 1=RUN, 2=WAIT, 3=AUTO)
     * @return 触发状态名称字符串
     */
    public static String getTriggerStatus(int index){return  triggerStatus[index];} // 索引→触发状态名称

    /** 边沿触发斜率枚举数组: RISE/FALL/DUAL */
    private static final String[] triggerEdgeSlope={"RISE","FALL","DUAL"}; // 边沿触发斜率枚举(上升/下降/双边沿)
    /**
     * 根据索引获取边沿触发斜率名称。
     * @param index 索引(0=RISE, 1=FALL, 2=DUAL)
     * @return 斜率名称字符串
     */
    public static String getTriggerEdgeSlope(int index){return triggerEdgeSlope[index];} // 索引→边沿斜率名称

    /** 边沿触发耦合枚举数组: DC/AC/HFRej/LFRej/Noiserej */
    private static final String[] triggerEdgeCouple={"DC","AC","HFRej","LFRej","Noiserej"}; // 边沿触发耦合枚举(直流/交流/高频抑制/低频抑制/噪声抑制)
    /**
     * 根据索引获取边沿触发耦合名称。
     * @param index 索引(0=DC, 1=AC, 2=HFRej, 3=LFRej, 4=Noiserej)
     * @return 耦合名称字符串
     */
    public static String getTriggerEdgeCouple(int index){return triggerEdgeCouple[index];} // 索引→边沿耦合名称

    /** 脉冲触发极性枚举数组: POSitive/NEGative/EITHer */
    private static final String[] triggerPulsePolarity={"POSitive","NEGative","EITHer"}; // 脉冲触发极性枚举(正/负/任一)
    /**
     * 根据索引获取脉冲触发极性名称。
     * @param index 索引(0=POSitive, 1=NEGative, 2=EITHer)
     * @return 极性名称字符串
     */
    public static String getTriggerPulsePolarity(int index){return  triggerPulsePolarity[index];} // 索引→脉冲极性名称

    /** 脉冲触发条件枚举数组: LESS/GREat/EQUal/UNEQual */
    private static final String[] triggerPulseCondition={"LESS","GREat","EQUal","UNEQual"}; // 脉冲触发条件枚举(小于/大于/等于/不等于)
    /**
     * 根据索引获取脉冲触发条件名称。
     * @param index 索引(0=LESS, 1=GREat, 2=EQUal, 3=UNEQual)
     * @return 条件名称字符串
     */
    public static String getTriggerPulseCondition(int index){return triggerPulseCondition[index];} // 索引→脉冲条件名称

    /** 逻辑触发状态枚举数组: HIGH/LOW/NONE */
    private static final String[] triggerLogicStatus={"HIGH","LOW","NONE"}; // 逻辑触发状态枚举(高/低/无关)
    /**
     * 根据索引获取逻辑触发状态名称。
     * @param index 索引(0=HIGH, 1=LOW, 2=NONE)
     * @return 状态名称字符串
     */
    public static String getTriggerLogicStatus(int index){return  triggerLogicStatus[index];} // 索引→逻辑触发状态名称

    /** 逻辑触发函数枚举数组: AND/OR/NAND/NOR */
    private static final String[] triggerLogicFunction={"AND","OR","NAND","NOR"}; // 逻辑触发函数枚举(与/或/与非/或非)
    /**
     * 根据索引获取逻辑触发函数名称。
     * @param index 索引(0=AND, 1=OR, 2=NAND, 3=NOR)
     * @return 逻辑函数名称字符串
     */
    public static String getTriggerLogicFunction(int index){return  triggerLogicFunction[index];} // 索引→逻辑函数名称

    /** 逻辑触发条件枚举数组: LESS/GREat/EQUal/UNEQual/TRUE/FALSe */
    private static final String[] triggerLogicCondition={"LESS","GREat","EQUal","UNEQual","TRUE","FALSe"}; // 逻辑触发条件枚举(小于/大于/等于/不等于/真/假)
    /**
     * 根据索引获取逻辑触发条件名称。
     * @param index 索引(0~5)
     * @return 条件名称字符串
     */
    public static String getTriggerLogicCondition(int index){return  triggerLogicCondition[index];} // 索引→逻辑条件名称

    /** 毛刺(RUNT)触发条件枚举数组: LESS/GREAt/BETWeen/NONE */
    private static final String[] triggerDwartCondition={"LESS","GREAt","BETWeen","NONE"}; // 毛刺触发条件枚举(小于/大于/之间/无)
    /**
     * 根据索引获取毛刺触发条件名称。
     * @param index 索引(0=LESS, 1=GREAt, 2=BETWeen, 3=NONE)
     * @return 条件名称字符串
     */
    public static String getTriggerDwartCondition(int index){return triggerDwartCondition[index];} // 索引→毛刺条件名称

    /** 斜率触发边沿枚举数组: RISE/FALL/EITHer */
    private static final String[] triggerSlopeEdge={"RISE","FALL","EITHer"}; // 斜率触发边沿枚举(上升/下降/任一)
    /**
     * 根据索引获取斜率触发边沿名称。
     * @param index 索引(0=RISE, 1=FALL, 2=EITHer)
     * @return 边沿名称字符串
     */
    public static String getTriggerSlopeEdge(int index){return triggerSlopeEdge[index];} // 索引→斜率边沿名称

    /** 斜率触发条件枚举数组: LESS/GREat/BETWeen */
    private static final String[] triggerSlopeCondition={"LESS","GREat","BETWeen"}; // 斜率触发条件枚举(小于/大于/之间)
    /**
     * 根据索引获取斜率触发条件名称。
     * @param index 索引(0=LESS, 1=GREat, 2=BETWeen)
     * @return 条件名称字符串
     */
    public static String getTriggerSlopeCondition(int index){return triggerSlopeCondition[index];} // 索引→斜率条件名称

    /** 超时触发极性枚举数组: POSitive/NEGative/EITHer */
    private static final String[] triggerTimeoutPolarity={"POSitive","NEGative","EITHer"}; // 超时触发极性枚举(正/负/任一)
    /**
     * 根据索引获取超时触发极性名称。
     * @param index 索引(0=POSitive, 1=NEGative, 2=EITHer)
     * @return 极性名称字符串
     */
    public static String getTriggerTimeoutPolarity(int index){return triggerTimeoutPolarity[index];} // 索引→超时极性名称

    /** 第N边沿触发斜率枚举数组: RISE/FALL/EITHer */
    private static final String[] triggerNedgeSlope={"RISE","FALL","EITHer"}; // 第N边沿触发斜率枚举(上升/下降/任一)
    /**
     * 根据索引获取第N边沿触发斜率名称。
     * @param index 索引(0=RISE, 1=FALL, 2=EITHer)
     * @return 斜率名称字符串
     */
    public static String getTriggerNedgeSlope(int index){return  triggerNedgeSlope[index];} // 索引→第N边沿斜率名称

    /** 上升/下降边沿枚举数组: RISE/FALL(用于SPI等总线边沿选择) */
    private static final String[] triggerRiseFall={"RISE","FALL"}; // 上升/下降边沿枚举
    /**
     * 根据索引获取上升/下降边沿名称(常用于SPI时钟边沿、总线空闲电平选择)。
     * @param idx 索引(0=RISE, 1=FALL)
     * @return 边沿名称字符串
     */
    public static String getTriggerRiseFall(int idx){return triggerRiseFall[idx];} // 索引→上升/下降边沿名称

    /** 视频触发极性枚举数组: POSitive/NEGative */
    private static final String[] triggerVideoPolarity={"POSitive","NEGative"}; // 视频触发极性枚举(正/负)
    /**
     * 根据索引获取视频触发极性名称。
     * @param index 索引(0=POSitive, 1=NEGative)
     * @return 极性名称字符串
     */
    public static String getTriggerVideoPolarity(int index){return triggerVideoPolarity[index];} // 索引→视频极性名称

    /** 视频制式枚举数组: PAL/SECAm/NTSC/720P/1080I/1080P */
    private static final String[] triggerVideoStandard={"PAL","SECAm","NTSC","720P","1080I","1080P"}; // 视频制式枚举(PAL/SECAM/NTSC/720P/1080I/1080P)
    /**
     * 根据索引获取视频制式名称。
     * @param index 索引(0=PAL, 1=SECAm, 2=NTSC, 3=720P, 4=1080I, 5=1080P)
     * @return 制式名称字符串
     */
    public static String getTriggerVideoStandard(int index){return triggerVideoStandard[index];} // 索引→视频制式名称

    /** 视频A模式触发枚举数组: ODDField/EVENfield/ALLField/ALLLine/LINE */
    private static final String[] triggerVideoAmode={"ODDField","EVENfield","ALLField","ALLLine","LINE"}; // 视频A模式枚举(奇场/偶场/全场上/全行/指定行)
    /**
     * 根据索引获取视频A模式名称。
     * @param index 索引
     * @return A模式名称字符串
     */
    public static String getTriggerVideoAmode(int index){return triggerVideoAmode[index];} // 索引→视频A模式名称

    /** 视频B模式触发枚举数组: ALLField/ALLLine/LINE */
    private static final String[] triggerVideoBmode={"ALLField","ALLLine","LINE"}; // 视频B模式枚举(全场上/全行/指定行)
    /**
     * 根据索引获取视频B模式名称。
     * @param index 索引
     * @return B模式名称字符串
     */
    public static String getTriggerVideoBmode(int index){return triggerVideoBmode[index];} // 索引→视频B模式名称

    /** 视频A模式频率枚举数组: 60Hz/50Hz */
    private static final String[] triggerVideoAfrequence={"60Hz","50Hz"}; // 视频A模式频率枚举
    /**
     * 根据索引获取视频A模式频率名称。
     * @param index 索引(0=60Hz, 1=50Hz)
     * @return 频率名称字符串
     */
    public static String getTriggerVideoAfrequence(int index){return triggerVideoAfrequence[index];} // 索引→视频A频率名称

    /** 视频B模式频率枚举数组: 60Hz/50Hz/30Hz/25Hz/24Hz */
    private static final String[] triggerVideoBfrequence={"60Hz","50Hz","30Hz","25Hz","24Hz"}; // 视频B模式频率枚举
    /**
     * 根据索引获取视频B模式频率名称。
     * @param index 索引
     * @return 频率名称字符串
     */
    public static String getTriggerVideoBfrequence(int index){return triggerVideoBfrequence[index];} // 索引→视频B频率名称

    /** 时基模式枚举数组: YT/XY */
    private static final String[] timebaseMode={"YT","XY"}; // 时基模式枚举(Y-T/X-Y)
    /**
     * 根据索引获取时基模式名称。
     * @param index 索引(0=YT, 1=XY)
     * @return 时基模式名称字符串
     */
    public static String getTimebaseMode(int index){return timebaseMode[index];} // 索引→时基模式名称

    /** 存储位置枚举数组: Local/UDisk */
    private static final String[] local={"Local","UDisk"}; // 存储位置枚举(本地/U盘)
    /**
     * 根据索引获取存储位置名称。
     * @param index 索引(0=Local, 1=UDisk)
     * @return 存储位置名称字符串
     */
    public static String getLocal(int index){ return local[index]; } // 索引→存储位置名称

    /** 保存文件类型枚举数组: WAV/CSV/BIN */
    private static final String[] saveType={"WAV","CSV","BIN"}; // 保存文件类型枚举(波形/CSV/二进制)
    /**
     * 根据索引获取保存文件类型名称。
     * @param index 索引(0=WAV, 1=CSV, 2=BIN)
     * @return 文件类型名称字符串
     */
    public static String getSaveType(int index){return saveType[index];} // 索引→保存文件类型名称

    /** 总线类型枚举数组: Uart/LIN/CAN/SPI/I2C/429/1553B */
    private static final String[] busType={"Uart","LIN","CAN","SPI","I2C","429","1553B"}; // 总线类型枚举(UART/LIN/CAN/SPI/I2C/ARINC429/1553B)
    /**
     * 根据索引获取总线类型名称。
     * @param index 索引(0=Uart, 1=LIN, 2=CAN, 3=SPI, 4=I2C, 5=429, 6=1553B)
     * @return 总线类型名称字符串
     */
    public static String getBusType(int index){return busType[index];} // 索引→总线类型名称

    /** 空闲电平枚举数组: high/low */
    private static final String[] idLevel={"high","low"}; // 空闲电平枚举(高/低)
    /**
     * 根据索引获取空闲电平名称(用于UART/SPI等总线空闲电平选择)。
     * @param index 索引(0=high, 1=low)
     * @return 空闲电平名称字符串
     */
    public static String getIdLevel(int index){return idLevel[index];} // 索引→空闲电平名称

    /** LIN波特率枚举数组: 2400/4800/9600/19200 */
    private static final String[] linBaudRate={"2400","4800","9600","19200"}; // LIN波特率枚举
    /**
     * 根据索引获取LIN波特率字符串。
     * @param index 索引
     * @return 波特率字符串
     */
    public static String getLinBaudRate(int index){return  linBaudRate[index];} // 索引→LIN波特率

    /** UART波特率枚举数组: 1200~115200(10档) */
    private static final String[] uartBaudRate={"1200","2400","4800","9600","19200","38400","43000","56000","57600","115200"}; // UART波特率枚举
    /**
     * 根据索引获取UART波特率字符串。
     * @param index 索引
     * @return 波特率字符串
     */
    public static String getUartBaudRate(int index){return uartBaudRate[index];} // 索引→UART波特率

    /** UART校验方式枚举数组: NONE/ODD/EVEN */
    private static final String[] uartCheck={"NONE","ODD","EVEN"}; // UART校验方式枚举(无/奇/偶)
    /**
     * 根据索引获取UART校验方式名称。
     * @param index 索引(0=NONE, 1=ODD, 2=EVEN)
     * @return 校验方式名称字符串
     */
    public static String getUartCheck(int index){return uartCheck[index];} // 索引→UART校验方式名称

    /** UART数据位宽度枚举数组: 5/6/7/8/9 */
    private static final String[] uartWidth={"5","6","7","8","9"}; // UART数据位宽度枚举
    /**
     * 根据索引获取UART数据位宽度字符串。
     * @param index 索引(0=5位, 1=6位, ..., 4=9位)
     * @return 位宽字符串
     */
    public static String getUartWidth(int index){return uartWidth[index];} // 索引→UART数据位宽度

    /** UART显示格式枚举数组: Hex/Bin/ASCII */
    private static final String[] uartDisplay={"Hex","Bin","ASCII"}; // UART显示格式枚举(十六进制/二进制/ASCII)
    /**
     * 根据索引获取UART显示格式名称。
     * @param index 索引(0=Hex, 1=Bin, 2=ASCII)
     * @return 显示格式名称字符串
     */
    public static String getUartDisplay(int index){return uartDisplay[index];} // 索引→UART显示格式名称

    /** CAN信号线枚举数组: CAN_H/CAN_L/H_L/L_H/Rx/Tx */
    private static final String[] canSignal={"CAN_H","CAN_L","H_L","L_H","Rx","Tx"}; // CAN信号线枚举(CAN高/CAN低/差分H_L/差分L_H/接收/发送)
    /**
     * 根据索引获取CAN信号线名称。
     * @param index 索引
     * @return 信号线名称字符串
     */
    public static String getCanSignal(int index){return canSignal[index];} // 索引→CAN信号线名称

    /** CAN标准波特率枚举数组: 100k/500k/1M */
    public static final String[] canBaudRate={"100000","500000","1000000"}; // CAN标准波特率枚举(100K/500K/1M)
    /**
     * 根据索引获取CAN标准波特率字符串。
     * @param index 索引(0=100000, 1=500000, 2=1000000)
     * @return 波特率字符串
     */
    public static String getCanBaudRate(int index){ return canBaudRate[index];} // 索引→CAN标准波特率

    /** CAN FD数据相位波特率枚举数组: NONE/2M/5M */
    public static final String[] CanFDBaudrate={"NONE","2000000","5000000"}; // CAN FD数据相位波特率枚举(无/2M/5M)
    /**
     * 根据索引获取CAN FD数据相位波特率字符串。
     * @param index 索引(0=NONE, 1=2000000, 2=5000000)
     * @return FD波特率字符串
     */
    public static String getCanFDBaudrate(int index){return CanFDBaudrate[index];}; // 索引→CAN FD波特率

    /** CAN DLC(数据长度码)枚举数组: 0~8,12,16,20,24,32,48,64 */
    public static final int[] canDlc={0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64}; // CAN DLC枚举(标准0~8 + FD扩展12~64)
    /**
     * 根据索引获取CAN DLC值。
     * @param index 索引
     * @return DLC整型值
     */
    public static final int getCanDlc(int index){return canDlc[index];} // 索引→CAN DLC值

    /** CAN ISO模式枚举数组: ISO/NON */
    public static final String[] canIso={"ISO","NON"}; // CAN ISO模式枚举(ISO标准/非ISO)
    /**
     * 根据索引获取CAN ISO模式名称。
     * @param index 索引(0=ISO, 1=NON)
     * @return ISO模式名称字符串
     */
    public static final String getCanIso(int index){return canIso[index];} // 索引→CAN ISO模式名称

    /** ARINC429标签格式枚举数组: LABEL+DATA/L+D+SSM/L+SDI+D+SSM */
    private static final String[] art429Format={"LABEL+DATA","L+D+SSM","L+SDI+D+SSM"}; // ARINC429标签格式枚举(标签+数据/标签+数据+SSM/标签+SDI+数据+SSM)
    /**
     * 根据索引获取ARINC429标签格式名称。
     * @param index 索引(0=LABEL+DATA, 1=L+D+SSM, 2=L+SDI+D+SSM)
     * @return 标签格式名称字符串
     */
    public static final String get429Format(int index){ return art429Format[index];} // 索引→ARINC429标签格式名称

    /** ARINC429显示格式枚举数组: Bin/Hex */
    private static final String[] art429Display={"Bin","Hex"}; // ARINC429显示格式枚举(二进制/十六进制)
    /**
     * 根据索引获取ARINC429显示格式名称。
     * @param index 索引(0=Bin, 1=Hex)
     * @return 显示格式名称字符串
     */
    public static final String get429Display(int index){return art429Display[index];} // 索引→ARINC429显示格式名称

    /** ARINC429波特率枚举数组: 12500/100000 */
    private static final String[] art429BaudRate={"12500","100000"}; // ARINC429波特率枚举(低速12.5Kb/高速100Kb)
    /**
     * 根据索引获取ARINC429波特率字符串。
     * @param index 索引(0=12500, 1=100000)
     * @return 波特率字符串
     */
    public static final String get429BaudRate(int index){return art429BaudRate[index];} // 索引→ARINC429波特率

    /** UART串行触发类型枚举数组: STARt/STOP/DATA/0:DATA/1:DATA/X:DATA/PARIty */
    private static final String[] uartTriggerType={"STARt","STOP","DATA","0:DATA","1:DATA","X:DATA","PARIty"}; // UART串行触发类型枚举(起始位/停止位/数据/0数据/1数据/X数据/校验)
    /**
     * 根据索引获取UART串行触发类型名称。
     * @param index 索引
     * @return 触发类型名称字符串
     */
    public static final String getuartTriggerType(int index){return uartTriggerType[index];} // 索引→UART串行触发类型名称

    /** LIN串行触发类型枚举数组: SRISe/FID/IDATa */
    private static final String[] LinTriggerType={"SRISe","FID","IDATa"}; // LIN串行触发类型枚举(同步上升/帧ID/ID+数据)
    /**
     * 根据索引获取LIN串行触发类型名称。
     * @param index 索引(0=SRISe, 1=FID, 2=IDATa)
     * @return 触发类型名称字符串
     */
    public static final String getLinTriggerType(int index){return LinTriggerType[index];} // 索引→LIN串行触发类型名称

    /** CAN串行触发类型枚举数组: FSTArt/RFID/DFID/RDID/IDATa/WRFR/AERRor/ACKError/OVERload */
    private static final String[] CanTriggerType={"FSTArt","RFID","DFID","RDID","IDATa","WRFR","AERRor","ACKError","OVERload"}; // CAN串行触发类型枚举(帧起始/远程帧ID/数据帧ID/远程数据ID/ID+数据/远程帧/主动错误/ACK错误/过载)
    /**
     * 根据索引获取CAN串行触发类型名称。
     * @param index 索引
     * @return 触发类型名称字符串
     */
    public static final String getCanTriggerType(int index){return CanTriggerType[index];} // 索引→CAN串行触发类型名称

    /** SPI串行触发类型枚举数组: CS/DATA/X:DATA */
    private static final String[] SPITriggerType={"CS","DATA","X:DATA"}; // SPI串行触发类型枚举(片选/数据/指定数据)
    /**
     * 根据索引获取SPI串行触发类型名称。
     * @param index 索引(0=CS, 1=DATA, 2=X:DATA)
     * @return 触发类型名称字符串
     */
    public static final String getSpiTriggerType(int index){return SPITriggerType[index];} // 索引→SPI串行触发类型名称

    /** I2C串行触发类型枚举数组: STARt/STOP/ACKLost/RESTart/NACKaddress/FRAM1/FRAM2/RDATa/WRITe10 */
    private static final String[] IICTriggerType={"STARt","STOP","ACKLost","RESTart","NACKaddress","FRAM1","FRAM2","RDATa","WRITe10",}; // I2C串行触发类型枚举(起始/停止/ACK丢失/重复起始/NACK地址/帧1/帧2/读数据/写10位地址)
    /**
     * 根据索引获取I2C串行触发类型名称。
     * @param index 索引
     * @return 触发类型名称字符串
     */
    public static final String getIICTriggerType(int index){return IICTriggerType[index];} // 索引→I2C串行触发类型名称

    /** MIL-STD-1553B串行触发类型枚举数组: CSSYnc/DWSYnc/CSWOrd/RTADdress/MERRor/DWORd/OPERror/AERRor */
    private static final String[] B1553bTriggerType={"CSSYnc","DWSYnc","CSWOrd","RTADdress","MERRor","DWORd","OPERror","AERRor",}; // 1553B串行触发类型枚举(命令字同步/数据字同步/命令字/RT地址/消息错误/数据字/奇偶错误/主动错误)
    /**
     * 根据索引获取1553B串行触发类型名称。
     * @param index 索引
     * @return 触发类型名称字符串
     */
    public static final String getB1553bTriggerType(int index){return B1553bTriggerType[index];} // 索引→1553B串行触发类型名称

    /** ARINC429串行触发类型枚举数组: WBEGin/WEND/LABEl/SDI/DATA/SSM/LSDI/LDATa/LSSM/WERROr/WINTerval/VERRor/AERRor/ALL0/ALL1 */
    private static final String[] Arinc429TriggerType={"WBEGin","WEND","LABEl","SDI","DATA","SSM","LSDI","LDATa","LSSM","WERROr","WINTerval","VERRor","AERRor","ALL0","ALL1"}; // ARINC429串行触发类型枚举(字开始/字结束/标签/SDI/数据/SSM/标签+SDI/标签+数据/标签+SSM/字错误/字间隔/值错误/主动错误/全0/全1)
    /**
     * 根据索引获取ARINC429串行触发类型名称。
     * @param index 索引
     * @return 触发类型名称字符串
     */
    public static final String getArinc429TriggerType(int index){return Arinc429TriggerType[index];} // 索引→ARINC429串行触发类型名称

    /** 串行触发数据条件枚举数组: LESS/GREAt/EQUAl/UNEQual */
    private static final String[] SerialTriggerCondition={"LESS","GREAt","EQUAl","UNEQual"}; // 串行触发数据条件枚举(小于/大于/等于/不等于)
    /**
     * 根据索引获取串行触发数据条件名称。
     * @param index 索引
     * @return 条件名称字符串
     */
    public static final String getSerialTriggerCondition(int index){return SerialTriggerCondition[index];} // 索引→串行触发条件名称

    /** 计数器测量模式枚举数组: FREQuency/PERiod/TOTalize */
    private static final String[] CountMode={"FREQuency","PERiod","TOTalize"}; // 计数器测量模式枚举(频率/周期/累计)
    /**
     * 根据索引获取计数器测量模式名称。
     * @param index 索引(0=FREQuency, 1=PERiod, 2=TOTalize)
     * @return 测量模式名称字符串
     */
    public  static final String getCountMode(int index){return CountMode[index];} // 索引→计数器测量模式名称

    /** 存储深度枚举数组: Auto/AUTO/220M/22M/2.2M/220K/22K/110M/11M/1.1M/110K/11K */
    private static final String[] mDepth={ "Auto", "AUTO","220000000","22000000","2200000","220000","22000","110000000","11000000","1100000","110000","11000",}; // 存储深度枚举(Auto/AUTO/220M~11K各档)
    /**
     * 根据索引获取存储深度名称。
     * @param index 索引
     * @return 存储深度名称字符串
     */
    public static final String getMdepth(int index){return mDepth[index];} // 索引→存储深度名称

    /** 自动量程触发源枚举数组: CURRent/Max */
    private static final String[] mAutoSource={"CURrent","Max"}; // 自动量程触发源枚举(当前/最大)
    /**
     * 根据索引获取自动量程触发源名称。
     * @param index 索引(0=CURrent, 1=Max)
     * @return 触发源名称字符串
     */
    public static final String getAutoSource(int index){return mAutoSource[index];} // 索引→自动量程触发源名称

    /** 串行总线显示模式枚举数组: GRAP/TXT */
    private static final String[] mSerialBusMode={"GRAP","TXT"}; // 串行总线显示模式枚举(图形/文本)
    /**
     * 根据索引获取串行总线显示模式名称。
     * @param index 索引(0=GRAP, 1=TXT)
     * @return 显示模式名称字符串
     */
    public static final String getSerialBusMode(int index){ return mSerialBusMode[index];} // 索引→串行总线显示模式名称

    /** SPI数据位宽度枚举数组: 4/8/16/24/32 */
    private static final String[] mSpiBits={ "4","8","16","24","32",}; // SPI数据位宽度枚举
    /**
     * 根据索引获取SPI数据位宽度字符串。
     * @param index 索引(0=4, 1=8, 2=16, 3=24, 4=32)
     * @return 位宽字符串
     */
    public static final String getSpiBits(int index){return mSpiBits[index];} // 索引→SPI位宽

    /** 分段采样显示类型枚举数组: SINGLe/FIT */
    private static final String[] mSegmentedDisplayType={"SINGLe","FIT"}; // 分段采样显示类型枚举(单段/适配)
    /**
     * 根据索引获取分段采样显示类型名称。
     * @param index 索引(0=SINGLe, 1=FIT)
     * @return 显示类型名称字符串
     */
    public static final String getSegmentDisplayType(int index){return mSegmentedDisplayType[index];} // 索引→分段显示类型名称

    /** 分段采样播放速度枚举数组: 1/2/4/8 */
    private static final String[] mSegmentDisplaySpeed={"1","2","4","8"}; // 分段采样播放速度枚举
    /**
     * 根据索引获取分段采样播放速度字符串。
     * @param index 索引
     * @return 速度字符串
     */
    public static final String getSegmentDisplaySpeed(int index){return mSegmentDisplaySpeed[index];} // 索引→分段播放速度

    /** 分段采样播放顺序枚举数组: ORDer/REORder */
    private static final String[] mSegmentOrder={"ORDer","REORder"}; // 分段采样播放顺序枚举(顺序/倒序)
    /**
     * 根据索引获取分段采样播放顺序名称。
     * @param index 索引(0=ORDer, 1=REORder)
     * @return 播放顺序名称字符串
     */
    public static final String getSegmentOrder(int index){return mSegmentOrder[index];} // 索引→分段播放顺序名称

    /** 波形数据获取模式枚举数组: NORMal/MAXimum/RAW */
    private static final String[] mWaveFormMode={"NORMal","MAXimum","RAW"}; // 波形数据获取模式枚举(正常/最大/原始)
    /**
     * 根据索引获取波形数据获取模式名称。
     * @param index 索引(0=NORMal, 1=MAXimum, 2=RAW)
     * @return 获取模式名称字符串
     */
    public static final String getWaveFormMode(int index){return mWaveFormMode[index];} // 索引→波形获取模式名称

    /** 波形数据格式枚举数组: WORD/BYTE/ASCii */
    private static final String[] mWaveFormFormat={"WORD","BYTE","ASCii "}; // 波形数据格式枚举(字/字节/ASCII)
    /**
     * 根据索引获取波形数据格式名称。
     * @param index 索引(0=WORD, 1=BYTE, 2=ASCii)
     * @return 数据格式名称字符串
     */
    public static final String getWaveFormFormat(int index){return mWaveFormFormat[index];} // 索引→波形数据格式名称

    /** Aux输出/输入模式枚举数组: OUT/IN */
    private static final String[] mAux={"OUT","IN"}; // Aux输入输出模式枚举(输出/输入)
    /**
     * 根据索引获取Aux输入输出模式名称。
     * @param index 索引(0=OUT, 1=IN)
     * @return 模式名称字符串
     */
    public static final String getAux(int index){return mAux[index];} // 索引→Aux模式名称

    /** 测量范围设置枚举数组: SCReen/CURSor */
    private static final String[] mMeasureSettingRange={"SCReen","CURSor"}; // 测量范围枚举(屏幕/光标)
    /**
     * 根据索引获取测量范围设置名称。
     * @param index 索引(0=SCReen, 1=CURSor)
     * @return 范围名称字符串
     */
    public static final String getMeasureSettingRange(int index){return mMeasureSettingRange[index];} // 索引→测量范围名称

    /** 测量阈值模式枚举数组: PERCent/ABSolute */
    private static final String[] mMeasureSettingThreshold={"PERCent","ABSolute"}; // 测量阈值模式枚举(百分比/绝对值)
    /**
     * 根据索引获取测量阈值模式名称。
     * @param index 索引(0=PERCent, 1=ABSolute)
     * @return 阈值模式名称字符串
     */
    public static final String getMeasureSettingThreshold(int index){return mMeasureSettingThreshold[index];} // 索引→测量阈值模式名称

}
