package com.micsig.tbook.ui.wavezone;

import android.content.Context;
import android.graphics.Color;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 通道工具类（Tools Channel）
 * 
 * <p>提供示波器通道相关的常量定义和工具方法。该类定义了所有通道类型的常量标识，
 * 并提供通道号转换、类型判断、遍历等工具方法。</p>
 * 
 * <p>通道类型说明：</p>
 * <ul>
 *     <li>Ch1-Ch8：模拟输入通道（值：1-8）</li>
 *     <li>Math1-Math8：数学运算通道（值：9-16）</li>
 *     <li>R1-R8：参考通道（值：17-24）</li>
 *     <li>S1-S4：串行通道（值：25-28）</li>
 *     <li>Cursor：光标通道（值：0x41-0x56）</li>
 *     <li>Trigger：触发相关通道（值：0x60-0x62）</li>
 * </ul>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>定义通道常量：提供所有通道类型的常量标识</li>
 *     <li>通道号转换：在UI通道号和FPGA通道号之间转换</li>
 *     <li>类型判断：判断通道号所属的通道类型</li>
 *     <li>遍历操作：提供通道遍历的工具方法</li>
 *     <li>颜色管理：获取通道对应的颜色</li>
 *     <li>名称管理：获取通道的显示名称</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 通道号转换
 * int fpgaChNo = TChan.toFpgaChNo(TChan.Ch1);
 * int uiChNo = TChan.toUiChNo(fpgaChNo);
 * 
 * // 类型判断
 * if (TChan.isMath(channelId)) {
 *     // 处理数学通道
 * }
 * 
 * // 遍历所有通道
 * TChan.foreachAllChan(chNo -> {
 *     String name = TChan.getChannelName(chNo);
 *     int color = TChan.getChannelColor(context, chNo);
 * });
 * }</pre>
 * 
 * @author Liwb
 * @version 1.0
 * @since 2024-3-20
 * @see IChan 通道标识枚举
 * @see ChannelFactory 通道工厂类
 * @see SvgNodeInfo SVG节点信息类
 */
public class TChan {
    // ==================== 通道常量定义 ====================
    
    // ---------- 空通道 ----------
    /** 空通道标识 */
    public static final int NULL= 0;
    
    // ---------- 模拟通道 Ch1-Ch8 ----------
    /** 模拟通道1 */
    public static final int Ch1 = 1;
    /** 模拟通道2 */
    public static final int Ch2 = 2;
    /** 模拟通道3 */
    public static final int Ch3 = 3;
    /** 模拟通道4 */
    public static final int Ch4 = 4;
    /** 模拟通道5 */
    public static final int Ch5 = 5;
    /** 模拟通道6 */
    public static final int Ch6 = 6;
    /** 模拟通道7 */
    public static final int Ch7 = 7;
    /** 模拟通道8 */
    public static final int Ch8 = 8;

    // ---------- 数学通道 Math1-Math8 ----------
    /** 数学运算通道1 */
    public static final int Math1 = 9;
    /** 数学运算通道2 */
    public static final int Math2 = 10;
    /** 数学运算通道3 */
    public static final int Math3 = 11;
    /** 数学运算通道4 */
    public static final int Math4 = 12;
    /** 数学运算通道5 */
    public static final int Math5 = 13;
    /** 数学运算通道6 */
    public static final int Math6 = 14;
    /** 数学运算通道7 */
    public static final int Math7 = 15;
    /** 数学运算通道8 */
    public static final int Math8 = 16;

    // ---------- 参考通道 R1-R8 ----------
    /** 参考通道1 */
    public static final int R1 = 17;
    /** 参考通道2 */
    public static final int R2 = 18;
    /** 参考通道3 */
    public static final int R3 = 19;
    /** 参考通道4 */
    public static final int R4 = 20;
    /** 参考通道5 */
    public static final int R5 = 21;
    /** 参考通道6 */
    public static final int R6 = 22;
    /** 参考通道7 */
    public static final int R7 = 23;
    /** 参考通道8 */
    public static final int R8 = 24;

    // ---------- 串行通道 S1-S4 ----------
    /** 串行通道1 */
    public static final int S1 = 25;
    /** 串行通道2 */
    public static final int S2 = 26;
    /** 串行通道3 */
    public static final int S3 = 27;
    /** 串行通道4 */
    public static final int S4 = 28;

    // ---------- 特殊通道 ----------
    /** 参考激活通道 */
    public static final int RefActive = 29;
    /** 数学激活通道 */
    public static final int MathActive = 30;

    // ---------- 光标通道 ----------
    /** 列光标1 */
    public static final int Cursor_col_1 = 0x41;
    /** 列光标2 */
    public static final int Cursor_col_2 = 0x42;
    /** 列光标3（联动，点击不选中） */
    public static final int Cursor_col_3 = 0x43;
    /** 列光标4（联动，点击选中） */
    public static final int Cursor_col_4 = 0x44;

    /** 行光标1 */
    public static final int Cursor_row_1 = 0x51;
    /** 行光标2 */
    public static final int Cursor_row_2 = 0x52;
    /** 行光标3（联动，点击不选中） */
    public static final int Cursor_row_3 = 0x53;
    /** 行光标4（联动，点击选中） */
    public static final int Cursor_row_4 = 0x54;
    /** 所有光标标识 */
    public static final int Cursor_all = 0x55;

    /** 单矩形光标 */
    public static final int SingleRect = 0x56;
    
    // ---------- 触发通道 ----------
    /** 触发时间通道 */
    public static final int TriggerTime = 0x60;
    /** 触发电平通道 */
    public static final int TriggerLevel = 0x61;
    /** 数值电平通道 */
    public static final int ValueLevel = 0x62;

    // ---------- 最大值常量 ----------
    /** 最大通道号 */
    public static final int MaxChan=S4;
    /** 最大串行通道数 */
    public static final int MaxSerial=4;
    /** 最大逻辑通道号 */
    public static final int MaxLogicChan =Ch8;
    /** 自动通道标识 */
    public static final int AUTO=R8;

    // ==================== 通道号转换方法 ====================
    
    /**
     * 将UI通道号转换为FPGA通道号
     * 
     * <p>UI通道号和FPGA通道号的编号体系不同，需要进行转换。
     * UI通道号从1开始，FPGA通道号从0开始，因此转换时需要减1。</p>
     * 
     * <p>转换公式：FPGA通道号 = UI通道号 - Ch1 + ChannelFactory.CH1</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int fpgaChNo = TChan.toFpgaChNo(TChan.Ch1);  // 返回 0
     * int fpgaChNo2 = TChan.toFpgaChNo(TChan.Ch8); // 返回 7
     * }</pre>
     * 
     * @param uiChNo UI通道号（1-28），对应TChan.Ch1到TChan.S4
     * @return FPGA通道号（0-27），用于与底层硬件通信
     * @see ChannelFactory#CH1 FPGA通道1常量
     */
    public static int toFpgaChNo(int uiChNo){
        return uiChNo-TChan.Ch1+ ChannelFactory.CH1;
    }

    /**
     * 将FPGA通道号转换为UI通道号
     * 
     * <p>与{@link #toFpgaChNo(int)}相反，将底层FPGA通道号转换为UI层使用的通道号。
     * FPGA通道号从0开始，UI通道号从1开始，因此转换时需要加1。</p>
     * 
     * <p>转换公式：UI通道号 = FPGA通道号 + Ch1</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int uiChNo = TChan.toUiChNo(0);  // 返回 TChan.Ch1 (1)
     * int uiChNo2 = TChan.toUiChNo(7); // 返回 TChan.Ch8 (8)
     * }</pre>
     * 
     * @param fpgaChNo FPGA通道号（0-27），来自底层硬件
     * @return UI通道号（1-28），用于UI显示和业务逻辑
     */
    public static int toUiChNo(int fpgaChNo){
        return fpgaChNo+TChan.Ch1;
    }

    /**
     * 将串行通道序号转换为TChan通道号
     * 
     * <p>串行通道序号从1开始，转换为TChan定义的通道常量。
     * 转换公式：TChan通道号 = 串行序号 + R8</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int tChan = TChan.toSerialTChan(1);  // 返回 TChan.S1 (25)
     * int tChan2 = TChan.toSerialTChan(4); // 返回 TChan.S4 (28)
     * }</pre>
     * 
     * @param serialsNumber 串行通道序号（1-4）
     * @return TChan通道号（S1-S4，即25-28）
     */
    public static int toSerialTChan(int serialsNumber) {
        return serialsNumber + TChan.R8;
    }

    /**
     * 将扩展通道号转换为SCPI通道索引
     * 
     * <p>SCPI协议中，通道8使用扩展编号29表示。
     * 此方法将扩展编号29转换回通道8。</p>
     * 
     * @param index 原始通道索引或扩展索引（29表示CH8）
     * @return SCPI通道索引，29转换为Ch8，其他保持不变
     */
    public static int ExtChToChIdxOfScpi(int index){
        if (index==29){
            return TChan.Ch8;
        }
        return index;
    }
    
    /**
     * 将通道索引转换为SCPI扩展通道号
     * 
     * <p>SCPI协议中，通道8使用扩展编号29表示。
     * 此方法将通道8转换为扩展编号29。</p>
     * 
     * @param index 通道索引
     * @return SCPI扩展通道号，Ch8转换为29，其他保持不变
     */
    public static int ChToExtChOfScpi(int index)
    {
        if (index==TChan.Ch8){
            return 29;
        }
        return index;
    }
    
    /**
     * 将数学通道序号转换为TChan通道号
     * 
     * <p>数学通道序号从1开始，转换为TChan定义的通道常量。
     * 转换公式：TChan通道号 = 数学序号 + Ch8</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int tChan = TChan.toMathChan(1);  // 返回 TChan.Math1 (9)
     * int tChan2 = TChan.toMathChan(8); // 返回 TChan.Math8 (16)
     * }</pre>
     * 
     * @param mathIndex 数学通道序号（1-8）
     * @return TChan通道号（Math1-Math8，即9-16）
     */
    public static int toMathChan(int mathIndex) {
        return mathIndex + TChan.Ch8;
    }

    /**
     * 将TChan数学通道号转换为数学通道序号
     * 
     * <p>将TChan.Math1~TChan.Math8转换为1-8的序号。
     * 如果输入不是数学通道，返回默认值1。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int mathNo = TChan.toMathNumber(TChan.Math1);  // 返回 1
     * int mathNo2 = TChan.toMathNumber(TChan.Math8); // 返回 8
     * int mathNo3 = TChan.toMathNumber(TChan.Ch1);   // 返回 1（非数学通道）
     * }</pre>
     * 
     * @param TChanMathNo TChan数学通道号（Math1-Math8，即9-16）
     * @return 数学通道序号（1-8），非数学通道返回1
     */
    public static int toMathNumber(int TChanMathNo) {
        if (TChan.isMath(TChanMathNo)) {
            return TChanMathNo - TChan.Math1 + 1;
        }
        return 1;
    }

    /**
     * 将参考通道序号转换为TChan通道号
     * 
     * <p>参考通道序号从1开始，转换为TChan定义的通道常量。
     * 转换公式：TChan通道号 = 参考序号 + Math8</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int tChan = TChan.toRefTChan(1);  // 返回 TChan.R1 (17)
     * int tChan2 = TChan.toRefTChan(8); // 返回 TChan.R8 (24)
     * }</pre>
     * 
     * @param refNumber 参考通道序号（1-8）
     * @return TChan通道号（R1-R8，即17-24）
     */
    public static int toRefTChan(int refNumber) {
        return refNumber + TChan.Math8;
    }

    /**
     * 将TChan参考通道号转换为参考通道序号
     * 
     * <p>将TChan.R1~TChan.R8转换为1-8的序号。
     * 如果输入不是参考通道，返回默认值1。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int refNo = TChan.toRefNumber(TChan.R1);  // 返回 1
     * int refNo2 = TChan.toRefNumber(TChan.R8); // 返回 8
     * int refNo3 = TChan.toRefNumber(TChan.Ch1); // 返回 1（非参考通道）
     * }</pre>
     * 
     * @param TChanRefNo TChan参考通道号（R1-R8，即17-24）
     * @return 参考通道序号（1-8），非参考通道返回1
     */
    public static int toRefNumber(int TChanRefNo) {
        if (TChan.isRef(TChanRefNo)) {
            return TChanRefNo - TChan.R1 + 1;
        }
        return 1;
    }

    /**
     * 将串行通道序号转换为FPGA通道号
     * 
     * <p>组合使用{@link #toSerialTChan(int)}和{@link #toFpgaChNo(int)}，
     * 直接将串行序号转换为底层可用的FPGA通道号。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int fpgaNo = TChan.toFpgaBySerialNumber(1);  // 返回 24（S1对应的FPGA通道号）
     * }</pre>
     * 
     * @param serialsNumber 串行通道序号（1-4）
     * @return FPGA通道号（24-27）
     */
    public static int toFpgaBySerialNumber(int serialsNumber){
        int tChan=toSerialTChan(serialsNumber);
        return toFpgaChNo(tChan);
    }
    
    /**
     * 将TChan串行通道号转换为串行通道序号
     * 
     * <p>将TChan.S1~TChan.S4转换为1-4的序号。
     * 如果输入不是串行通道，返回默认值1。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int serialNo = TChan.toSerialNumber(TChan.S1);  // 返回 1
     * int serialNo2 = TChan.toSerialNumber(TChan.S4); // 返回 4
     * }</pre>
     * 
     * @param TChanSNo TChan串行通道号（S1-S4，即25-28）
     * @return 串行通道序号（1-4），非串行通道返回1
     */
    public static int toSerialNumber(int TChanSNo){
        if (TChan.isSerial(TChanSNo)) {
            return TChanSNo - TChan.S1 + 1;
        }
        return 1;
    }
    
    /**
     * 将串行通道名称转换为串行通道序号
     * 
     * <p>先通过通道名称查找对应的TChan通道号，再转换为序号。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int serialNo = TChan.toSerialNumber("S1");  // 返回 1
     * int serialNo2 = TChan.toSerialNumber("S4"); // 返回 4
     * }</pre>
     * 
     * @param SerialName 串行通道名称（"S1"-"S4"）
     * @return 串行通道序号（1-4），未找到返回-1对应的序号
     */
    public static int toSerialNumber(String SerialName){
        int ch= findChanByValue(SerialName);
        return toSerialNumber(ch);
    }

    // ==================== 通道遍历方法 ====================
    
    /**
     * 遍历所有通道（Ch1到S4）
     * 
     * <p>对Ch1到MaxChan（S4）范围内的所有通道执行指定操作。
     * 包括模拟通道、数学通道、参考通道和串行通道。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachAllChan(chNo -> {
     *     System.out.println("通道号: " + chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个通道执行的操作，接收通道号作为参数
     */
    public static void foreachAllChan(Consumer<Integer> action){
        for(int i=TChan.Ch1;i<=MaxChan;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历Ch1到R8范围内的通道
     * 
     * <p>对Ch1到R8范围内的通道执行指定操作。
     * 包括模拟通道、数学通道和参考通道，不包括串行通道。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachCh1ToR8(chNo -> {
     *     // 处理模拟、数学、参考通道
     * });
     * }</pre>
     * 
     * @param action 对每个通道执行的操作
     */
    public static void foreachCh1ToR8(Consumer<Integer> action){
        for(int i=TChan.Ch1;i<=TChan.R8;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历Math1到R8范围内的通道
     * 
     * <p>对Math1到R8范围内的通道执行指定操作。
     * 包括数学通道和参考通道。</p>
     * 
     * @param action 对每个通道执行的操作
     */
    public static void foreachMath1ToR8(Consumer<Integer> action){
        for (int i = TChan.Math1; i <= TChan.R8; i++) {
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有模拟通道（Ch1到Ch8）
     * 
     * <p>对Ch1到Ch8范围内的模拟通道执行指定操作。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachChan(chNo -> {
     *     String name = TChan.getChannelName(chNo);
     *     int color = TChan.getChannelColor(context, chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个模拟通道执行的操作
     */
    public static void foreachChan(Consumer<Integer> action){
        for(int i=TChan.Ch1;i<=TChan.Ch8;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有模拟通道，支持条件跳过
     * 
     * <p>对Ch1到Ch8范围内的模拟通道执行指定操作。
     * 当continuePredicate返回true时，跳过当前通道。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * // 跳过禁用的通道
     * TChan.foreachChan(chNo -> {
     *     processChannel(chNo);
     * }, chNo -> !isChannelEnabled(chNo));
     * }</pre>
     * 
     * @param action 对每个通道执行的操作
     * @param continuePredicate 跳过条件，返回true时跳过当前通道
     */
    public static void foreachChan(Consumer<Integer> action, Predicate<Integer> continuePredicate){
        for(int i=TChan.Ch1;i<=TChan.Ch8;i++){
            if (continuePredicate.test(i)){
                continue;
            }
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有数学通道（Math1到Math8）
     * 
     * <p>对Math1到Math8范围内的数学通道执行指定操作。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachMath(chNo -> {
     *     updateMathChannel(chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个数学通道执行的操作
     */
    public static void foreachMath(Consumer<Integer> action){
        for(int i=TChan.Math1;i<=TChan.Math8;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有参考通道（R1到R8）
     * 
     * <p>对R1到R8范围内的参考通道执行指定操作。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachRef(chNo -> {
     *     loadReferenceWaveform(chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个参考通道执行的操作
     */
    public static void foreachRef(Consumer<Integer> action){
        for(int i=TChan.R1;i<=TChan.R8;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有串行通道（S1到S4）
     * 
     * <p>对S1到S4范围内的串行通道执行指定操作。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * TChan.foreachSerial(chNo -> {
     *     configureSerialPort(chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个串行通道执行的操作
     */
    public static void foreachSerial(Consumer<Integer> action){
        for(int i=TChan.S1;i<=TChan.S4;i++){
            action.accept(i);
        }
    }
    
    /**
     * 遍历所有串行通道并返回结果
     * 
     * <p>对S1到S4范围内的串行通道执行指定操作，返回操作结果的逻辑或值。
     * 只要有一个通道的操作返回true，最终结果就为true。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean hasActive = TChan.foreachSerialResult(chNo -> {
     *     return isSerialActive(chNo);
     * });
     * }</pre>
     * 
     * @param action 对每个串行通道执行的操作，返回布尔结果
     * @return 所有通道操作结果的逻辑或值
     */
    public static boolean foreachSerialResult(Function<Integer,Boolean> action){
        boolean result=false;
        for(int i=TChan.S1;i<=TChan.S4;i++){
           boolean b= action.apply(i);
           result = result|| b;
        }
        return result;
    }

    // ==================== 通道类型判断方法 ====================
    
    /**
     * 判断是否为模拟通道
     * 
     * <p>检查通道号是否在Ch1到Ch8范围内。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * if (TChan.isChan(channelId)) {
     *     // 处理模拟通道
     * }
     * }</pre>
     * 
     * @param chNo 通道号
     * @return 如果是模拟通道（1-8）返回true，否则返回false
     */
    public static boolean isChan(int chNo){
        return chNo >= Ch1 && chNo <= Ch8;
    }
    
    /**
     * 判断是否为数学通道
     * 
     * <p>检查通道号是否在Math1到Math8范围内。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * if (TChan.isMath(channelId)) {
     *     // 处理数学通道
     * }
     * }</pre>
     * 
     * @param chNo 通道号
     * @return 如果是数学通道（9-16）返回true，否则返回false
     */
    public static boolean isMath(int chNo){
        return chNo>=Math1 && chNo<=Math8;
    }
    
    /**
     * 判断是否为参考通道
     * 
     * <p>检查通道号是否在R1到R8范围内。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * if (TChan.isRef(channelId)) {
     *     // 处理参考通道
     * }
     * }</pre>
     * 
     * @param chNo 通道号
     * @return 如果是参考通道（17-24）返回true，否则返回false
     */
    public static boolean isRef(int chNo){
        return chNo>=R1 && chNo<=R8;
    }
    
    /**
     * 判断是否为串行通道
     * 
     * <p>检查通道号是否在S1到S4范围内。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * if (TChan.isSerial(channelId)) {
     *     // 处理串行通道
     * }
     * }</pre>
     * 
     * @param chNo 通道号
     * @return 如果是串行通道（25-28）返回true，否则返回false
     */
    public static boolean isSerial(int chNo){
        return chNo>=S1 && chNo<=S4;
    }

    /**
     * 判断是否为Ch1到R8范围内的通道
     * 
     * <p>检查通道号是否在Ch1到R8范围内。
     * 包括模拟通道、数学通道和参考通道。</p>
     * 
     * @param chNo 通道号
     * @return 如果在范围内（1-24）返回true，否则返回false
     */
    public static boolean isCh1ToR8(int chNo){
        return chNo>=Ch1 && chNo<=R8;
    }
    
    /**
     * 判断是否为Ch1到S4范围内的通道
     * 
     * <p>检查通道号是否在Ch1到S4范围内。
     * 包括模拟通道、数学通道、参考通道和串行通道。</p>
     * 
     * @param chNo 通道号
     * @return 如果在范围内（1-28）返回true，否则返回false
     */
    public static boolean isCh1ToS4(int chNo){
        return chNo>=Ch1 && chNo<=S4;
    }
    
    /**
     * 判断是否为Math1到R8范围内的通道
     * 
     * <p>检查通道号是否在Math1到R8范围内。
     * 包括数学通道和参考通道。</p>
     * 
     * @param chNo 通道号
     * @return 如果在范围内（9-24）返回true，否则返回false
     */
    public static boolean isMathToR8(int chNo){
        return chNo>=Math1 && chNo<=R8;
    }

    /**
     * 判断是否为Math1到S4范围内的通道
     * 
     * <p>检查通道号是否在Math1到S4范围内。
     * 包括数学通道、参考通道和串行通道。</p>
     * 
     * @param chNo 通道号
     * @return 如果在范围内（9-28）返回true，否则返回false
     */
    public static boolean isMathToS4(int chNo) {
        return chNo >= Math1 && chNo <= S4;
    }
    
    /**
     * 判断是否为Ch1到Math8范围内的通道
     * 
     * <p>检查通道号是否在Ch1到Math8范围内。
     * 包括模拟通道和数学通道。</p>
     * 
     * @param chNo 通道号
     * @return 如果在范围内（1-16）返回true，否则返回false
     */
    public static boolean isChToMath(int chNo){
        return chNo>=Ch1 && chNo<=Math8;
    }

    /**
     * 判断是否为双行光标（Cursor_row_1或Cursor_row_2）
     * 
     * <p>检查通道号是否为Cursor_row_1或Cursor_row_2。</p>
     * 
     * @param chNo 通道号
     * @return 如果是双行光标返回true，否则返回false
     */
    public static boolean isCursorRow2(int chNo){
        return chNo==Cursor_row_1 || chNo==Cursor_row_2;
    }
    
    /**
     * 判断是否为双列光标（Cursor_col_1或Cursor_col_2）
     * 
     * <p>检查通道号是否为Cursor_col_1或Cursor_col_2。</p>
     * 
     * @param chNo 通道号
     * @return 如果是双列光标返回true，否则返回false
     */
    public static boolean isCursorCol2(int chNo){
        return chNo==Cursor_col_1 || chNo==Cursor_col_2;
    }
    
    /**
     * 判断是否为四行光标
     * 
     * <p>检查通道号是否为Cursor_row_1、Cursor_row_2、Cursor_row_3或Cursor_row_4。</p>
     * 
     * @param chNo 通道号
     * @return 如果是四行光标返回true，否则返回false
     */
    public static boolean isCursorRow4(int chNo){
        return chNo==Cursor_row_1 || chNo==Cursor_row_2 || chNo==Cursor_row_3 || chNo==Cursor_row_4;
    }
    
    /**
     * 判断是否为四列光标
     * 
     * <p>检查通道号是否为Cursor_col_1、Cursor_col_2、Cursor_col_3或Cursor_col_4。</p>
     * 
     * @param chNo 通道号
     * @return 如果是四列光标返回true，否则返回false
     */
    public static boolean isCursorCol4(int chNo){
        return chNo==Cursor_col_1 || chNo==Cursor_col_2 || chNo==Cursor_col_3 || chNo==Cursor_col_4;
    }
    
    /**
     * 判断是否为八光标（包括行列光标和单矩形光标）
     * 
     * <p>检查通道号是否为所有光标类型之一：
     * Cursor_col_1到Cursor_col_4、Cursor_row_1到Cursor_row_4、SingleRect。</p>
     * 
     * @param chNo 通道号
     * @return 如果是八光标之一返回true，否则返回false
     */
    public static boolean isCursor8(int chNo){
        return chNo==Cursor_col_1 || chNo==Cursor_col_2 || chNo==Cursor_col_3 || chNo==Cursor_col_4
                || chNo==Cursor_row_1 || chNo==Cursor_row_2 || chNo==Cursor_row_3 || chNo==Cursor_row_4 || chNo == SingleRect;
    }
    
    /**
     * 判断是否为六光标
     * 
     * <p>检查通道号是否为Cursor_col_1到Cursor_col_3、Cursor_row_1到Cursor_row_3之一。</p>
     * 
     * @param chNo 通道号
     * @return 如果是六光标之一返回true，否则返回false
     */
    public static boolean isCursor6(int chNo){
        return chNo==Cursor_col_1 || chNo==Cursor_col_2 || chNo==Cursor_col_3
                || chNo==Cursor_row_1 || chNo==Cursor_row_2 || chNo==Cursor_row_3 ;
    }
    
    /**
     * 判断是否为四光标（双行双列）
     * 
     * <p>检查通道号是否为Cursor_col_1、Cursor_col_2、Cursor_row_1、Cursor_row_2之一。</p>
     * 
     * @param chNo 通道号
     * @return 如果是四光标之一返回true，否则返回false
     */
    public static boolean isCursor4(int chNo){
        return chNo==Cursor_col_1 || chNo==Cursor_col_2
                || chNo==Cursor_row_1 || chNo==Cursor_row_2 ;
    }

    // ==================== 颜色和名称管理方法 ====================
    
    /**
     * 获取通道对应的颜色值
     * 
     * <p>根据通道ID从{@link SvgNodeInfo}中获取对应的颜色值。
     * 如果通道ID超出范围，返回默认灰色。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int color = TChan.getChannelColor(context, TChan.Ch1);
     * view.setBackgroundColor(color);
     * }</pre>
     * 
     * @param context Android上下文（当前未使用，保留用于未来扩展）
     * @param channelId 通道ID（0-27）
     * @return 通道对应的颜色值（ARGB格式），超出范围返回默认灰色
     * @see SvgNodeInfo#getColorsIntForChannel() 获取通道颜色数组
     */
    public static int getChannelColor(Context context, int channelId) {
        int defaultColor = Color.argb(255, 200, 200, 200);
        if (channelId < 0 || channelId > SvgNodeInfo.getColorsForChannelSize - 1) {
            return defaultColor;
        } else {
            return SvgNodeInfo.getColorsIntForChannel()[channelId];
        }
    }

    /**
     * 根据通道名称获取通道颜色值
     * 
     * <p>先通过通道名称查找对应的通道ID，再获取颜色值。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int color = TChan.getChannelColor(context, "CH1");
     * }</pre>
     * 
     * @param context Android上下文
     * @param chName 通道名称（如"CH1"、"Math1"、"Ref1"等）
     * @return 通道对应的颜色值
     */
    public static int getChannelColor(Context context,String chName){
        int ch= findChanByValue(chName);
        return getChannelColor(context,ch);
    }
    
    /**
     * 根据颜色值查找对应的通道ID
     * 
     * <p>遍历所有通道的颜色映射，查找与指定颜色匹配的通道。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int color = Color.YELLOW;
     * int chId = TChan.getColorToChannel(context, color);
     * }</pre>
     * 
     * @param context Android上下文（当前未使用）
     * @param color 要查找的颜色值
     * @return 匹配的通道ID，未找到返回0
     */
    public static int getColorToChannel(Context context,int color) {
        int ch=0;
        for (Map.Entry entry : SvgNodeInfo.getALLColorMap().entrySet()) {
            if ((Integer) entry.getValue() == color) {
                ch = (Integer) entry.getKey();
                return ch;
            }
        }
        return ch;
    }

    /** 通道名称映射表，用于通道ID和名称之间的转换 */
    private static HashMap<Integer,String> mapChanName;
    
    /**
     * 初始化通道名称映射表
     * 
     * <p>懒加载初始化通道名称映射表，将所有通道ID映射到对应的显示名称。
     * 包括模拟通道（CH1-CH8）、数学通道（Math1-Math8）、参考通道（Ref1-Ref8）和串行通道（S1-S4）。</p>
     */
    private static void tryInitMapChanName(){
        if (mapChanName==null){
            mapChanName=new HashMap<>();
            // 初始化模拟通道名称
            mapChanName.put(TChan.Ch1,"CH1");
            mapChanName.put(TChan.Ch2,"CH2");
            mapChanName.put(TChan.Ch3,"CH3");
            mapChanName.put(TChan.Ch4,"CH4");
            mapChanName.put(TChan.Ch5,"CH5");
            mapChanName.put(TChan.Ch6,"CH6");
            mapChanName.put(TChan.Ch7,"CH7");
            mapChanName.put(TChan.Ch8,"CH8");

            // 初始化数学通道名称
            mapChanName.put(TChan.Math1,"Math1");
            mapChanName.put(TChan.Math2,"Math2");
            mapChanName.put(TChan.Math3,"Math3");
            mapChanName.put(TChan.Math4,"Math4");
            mapChanName.put(TChan.Math5,"Math5");
            mapChanName.put(TChan.Math6,"Math6");
            mapChanName.put(TChan.Math7,"Math7");
            mapChanName.put(TChan.Math8,"Math8");

            // 初始化参考通道名称
            mapChanName.put(TChan.R1,"Ref1");
            mapChanName.put(TChan.R2,"Ref2");
            mapChanName.put(TChan.R3,"Ref3");
            mapChanName.put(TChan.R4,"Ref4");
            mapChanName.put(TChan.R5,"Ref5");
            mapChanName.put(TChan.R6,"Ref6");
            mapChanName.put(TChan.R7,"Ref7");
            mapChanName.put(TChan.R8,"Ref8");

            // 初始化串行通道名称
            mapChanName.put(TChan.S1,"S1");
            mapChanName.put(TChan.S2,"S2");
            mapChanName.put(TChan.S3,"S3");
            mapChanName.put(TChan.S4,"S4");
        }
    }
    
    /**
     * 获取通道的显示名称
     * 
     * <p>根据通道ID获取对应的显示名称字符串。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * String name = TChan.getChannelName(TChan.Ch1);   // 返回 "CH1"
     * String name2 = TChan.getChannelName(TChan.Math1); // 返回 "Math1"
     * String name3 = TChan.getChannelName(999);         // 返回 "NULL"
     * }</pre>
     * 
     * @param chIdx 通道ID
     * @return 通道显示名称，未找到返回"NULL"
     */
    public static String getChannelName(int chIdx){
        tryInitMapChanName();
        return mapChanName.getOrDefault(chIdx,"NULL");
    }
    
    /**
     * 根据通道名称查找通道ID
     * 
     * <p>通过通道名称反向查找对应的通道ID。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * int chId = TChan.findChanByValue("CH1");    // 返回 TChan.Ch1 (1)
     * int chId2 = TChan.findChanByValue("Math1"); // 返回 TChan.Math1 (9)
     * int chId3 = TChan.findChanByValue("Unknown"); // 返回 -1
     * }</pre>
     * 
     * @param chName 通道名称
     * @return 通道ID，未找到返回-1
     */
    public static int findChanByValue(String chName){
        tryInitMapChanName();
        int chan=-1;
        for(Map.Entry<Integer,String> entry:mapChanName.entrySet()){
            if (entry.getValue().equals(chName)){
                return entry.getKey();
            }
        }
        return chan;
    }
}
