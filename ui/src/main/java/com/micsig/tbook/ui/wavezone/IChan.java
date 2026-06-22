package com.micsig.tbook.ui.wavezone;



/**
 * 通道标识枚举类
 * 
 * <p>定义示波器系统中所有通道类型的枚举常量，包括模拟通道、数学通道、
 * 参考通道、串行通道和光标等。每个通道都有唯一的值、颜色和名称。</p>
 * 
 * <p>通道类型说明：</p>
 * <ul>
 *     <li>CH1-CH8：模拟输入通道，对应物理输入端口</li>
 *     <li>Math1-8：数学运算通道，用于波形运算结果</li>
 *     <li>R1-8：参考通道，用于存储和显示参考波形</li>
 *     <li>S1-4：串行通道，用于串行协议分析</li>
 *     <li>Cursor：光标通道，包括行光标、列光标和多光标</li>
 *     <li>Trigger：触发相关通道，包括触发电平和触发时间</li>
 * </ul>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>提供通道的唯一标识值（value）</li>
 *     <li>定义通道的默认颜色（color）</li>
 *     <li>提供通道的显示名称（name）</li>
 *     <li>提供通道类型判断的工具方法</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * IChan channel = IChan.CH1;
 * int channelValue = channel.getValue();  // 获取通道值
 * int channelColor = channel.getColor();  // 获取通道颜色
 * String channelName = channel.getName(); // 获取通道名称
 * 
 * // 判断通道类型
 * if (IChan.isCh1ToCh8(channel)) {
 *     // 处理模拟通道
 * }
 * }</pre>
 * 
 * @author Liwb
 * @version 1.0
 * @since 2023-8-10
 * @see TChan 通道常量定义类
 * @see IWave 波形绘制接口
 */
public enum IChan {
    // ==================== 空通道 ====================
    /** 空通道标识，用于表示无效或未定义的通道 */
    CH_NULL(-1, 0xFF202020, "NULL"),
    
    // ==================== 模拟通道 CH1-CH8 ====================
    /** 模拟通道1 - 黄色 */
    CH1(0, 0xFFFFE700, "CH1"),
    /** 模拟通道2 - 青色 */
    CH2(1, 0xFF00E4FF, "CH2"),
    /** 模拟通道3 - 品红色 */
    CH3(2, 0xFFFF0031, "CH3"),
    /** 模拟通道4 - 黄绿色 */
    CH4(3, 0xFFB6FF00, "CH4"),
    /** 模拟通道5 - 橙色 */
    CH5(4, 0xFFFF9000, "CH5"),
    /** 模拟通道6 - 蓝色 */
    CH6(5, 0xFF0037FF, "CH6"),
    /** 模拟通道7 - 粉色 */
    CH7(6, 0xFFFF0086, "CH7"),
    /** 模拟通道8 - 青绿色 */
    CH8(7, 0xFF00FF91, "CH8"),

    // ==================== 数学通道 Math1-8 ====================
    /** 数学运算通道1 - 品红色 */
    Math1(8, 0xFFFF00FF, "Math1"),
    /** 数学运算通道2 - 品红色 */
    Math2(9, 0xFFFF00FF, "Math2"),
    /** 数学运算通道3 - 品红色 */
    Math3(10, 0xFFFF00FF, "Math3"),
    /** 数学运算通道4 - 品红色 */
    Math4(11, 0xFFFF00FF, "Math4"),
    /** 数学运算通道5 - 品红色 */
    Math5(12, 0xFFFF00FF, "Math5"),
    /** 数学运算通道6 - 品红色 */
    Math6(13, 0xFFFF00FF, "Math6"),
    /** 数学运算通道7 - 品红色 */
    Math7(14, 0xFFFF00FF, "Math7"),
    /** 数学运算通道8 - 品红色 */
    Math8(15, 0xFFFF00FF, "Math8"),

    // ==================== 参考通道 R1-8 ====================
    /** 参考通道1 - 紫色 */
    R1(16, 0xFF807FFF, "R1"),
    /** 参考通道2 - 紫色 */
    R2(17, 0xFF807FFF, "R2"),
    /** 参考通道3 - 紫色 */
    R3(18, 0xFF807FFF, "R3"),
    /** 参考通道4 - 紫色 */
    R4(19, 0xFF807FFF, "R4"),
    /** 参考通道5 - 紫色 */
    R5(20, 0xFF807FFF, "R5"),
    /** 参考通道6 - 紫色 */
    R6(21, 0xFF807FFF, "R6"),
    /** 参考通道7 - 紫色 */
    R7(22, 0xFF807FFF, "R7"),
    /** 参考通道8 - 紫色 */
    R8(23, 0xFF807FFF, "R8"),

    // ==================== 串行通道 S1-4 ====================
    /** 串行通道1 - 棕色 */
    S1(24, 0xFF806040, "S1"),
    /** 串行通道2 - 浅青色 */
    S2(25, 0xFFA4DBDD, "S2"),
    /** 串行通道3 - 深蓝色 */
    S3(26, 0xFF406080, "S3"),
    /** 串行通道4 - 深绿色 */
    S4(27, 0xFF408040, "S4"),
    
    // ==================== 特殊通道 ====================
    /** 参考激活通道 - 紫色 */
    RefActive(28, 0xFF807FFF, "RefActive"),
    /** 串行通道组合S1S2 - 深灰色 */
    S1S2(29, 0xFF202020, "S1S2"),
    /** FFT数学通道 - 红色 */
    Math_FFT(30, 0xFFFF0000, "Math"),

    // ==================== 光标通道 ====================
    /** 列光标1 - 深灰色 */
    Cursor_col_1(0x41, 0xFF202020, "Col1"),
    /** 列光标2 - 深灰色 */
    Cursor_col_2(0x42, 0xFF202020, "Col2"),
    /** 列光标3（联动） - 深灰色 */
    Cursor_col_3(0x43, 0xFF202020, "Col3"),
    /** 列光标4（联动，可选中） - 深灰色 */
    Cursor_col_4(0x44, 0xFF202020, "Col4"),
    /** 行光标1 - 深灰色 */
    Cursor_row_1(0x51, 0xFF202020, "Row1"),
    /** 行光标2 - 深灰色 */
    Cursor_row_2(0x52, 0xFF202020, "Row2"),
    /** 行光标3（联动） - 深灰色 */
    Cursor_row_3(0x53, 0xFF202020, "Row3"),
    /** 行光标4（联动，可选中） - 深灰色 */
    Cursor_row_4(0x54, 0xFF202020, "Row4"),

    // ==================== 触发通道 ====================
    /** 触发时间通道 - 深灰色 */
    TriggerTime(0x60, 0xFF202020, "TriggerTime"),
    /** 触发电平通道 - 深灰色 */
    TriggerLevel(0x61, 0xFF202020, "TriggerLevel"),
    /** 数值电平通道 - 深灰色 */
    ValueLevel(0x62,0xFF202020,"ValueLevel"),
    
    // ==================== 多光标通道 ====================
    /** 多光标列1 - 巧克力色 */
    MultiCursor_col1(0x70, 0xFFD2691E, "Multi Col1"),
    /** 多光标列2 - 巧克力色 */
    MultiCursor_col2(0x71, 0xFFD2691E, "Multi Col2");

    // ==================== 枚举成员变量 ====================
    /** 通道的唯一标识值 */
    private final int value;
    /** 通道的颜色值（ARGB格式） */
    private final int color;
    /** 通道的显示名称 */
    private final String name;

    // ==================== 构造函数 ====================
    /**
     * 枚举构造函数
     * 
     * @param ch 通道的唯一标识值
     * @param color 通道的颜色值（ARGB格式）
     * @param name 通道的显示名称
     */
    private IChan(int ch, int color, String name) {
        this.value = ch;
        this.color = color;
        this.name = name;
    }

    // ==================== Getter方法 ====================
    /**
     * 获取通道的唯一标识值
     * 
     * @return 通道值
     */
    public  int getValue() {
        return value;
    }

    /**
     * 获取通道的颜色值
     * 
     * @return 颜色值（ARGB格式）
     */
    public int getColor() {
        return color;
    }

    /**
     * 获取通道的显示名称
     * 
     * @return 显示名称
     */
    public String getName() {
        return name;
    }

    // ==================== 静态变量和方法 ====================
    /** 枚举值数组缓存，用于提高查找性能 */
    public static IChan[] values;

    // 静态初始化块，初始化枚举值数组
    static {
        values = values();
    }

    // ==================== 通道类型判断方法 ====================
    /**
     * 判断是否为模拟通道CH1-CH8
     * 
     * @param chan 待判断的通道
     * @return 如果是CH1-CH8返回true，否则返回false
     */
    public static boolean isCh1ToCh8(IChan chan) {
        return (chan.getValue()>=IChan.CH1.getValue() &&  chan.getValue()<= IChan.CH8.getValue());
    }

    /**
     * 判断是否为参考通道R1-R8
     * 
     * @param chan 待判断的通道
     * @return 如果是R1-R8返回true，否则返回false
     */
    public static boolean isR1ToR8(IChan chan) {
        return (chan.getValue() >= IChan.R1.getValue() && chan.getValue() <= IChan.R8.getValue());
    }

    /**
     * 判断是否为CH1到Math8范围内的通道
     * 
     * @param chan 待判断的通道
     * @return 如果在CH1到Math8范围内返回true，否则返回false
     */
    public static boolean isCh1ToMath(IChan chan) {
        return (chan.getValue() >= IChan.CH1.getValue() && chan.getValue() <= IChan.Math8.getValue());
    }

    /**
     * 判断是否为数学通道或参考通道
     * 
     * @param chan 待判断的通道
     * @return 如果是Math1-8或R1-8返回true，否则返回false
     */
    public static boolean isMathOrRef(IChan chan) {
        return (chan.getValue()>= IChan.Math1.getValue() && chan.getValue()<=IChan.R8.getValue());
    }

    /**
     * 判断是否为Math1到S4范围内的通道
     * 
     * @param chan 待判断的通道
     * @return 如果在Math1到S4范围内返回true，否则返回false
     */
    public static boolean isMathToS4(IChan chan) {
        return (chan.getValue() >= IChan.Math1.getValue() && chan.getValue() <= IChan.S4.getValue());
    }

    /**
     * 判断是否为CH1到RefActive范围内的通道
     * 
     * @param chan 待判断的通道
     * @return 如果在CH1到RefActive范围内返回true，否则返回false
     */
    public static boolean isCh1ToRefActive(IChan chan) {
        return (chan.getValue() >= IChan.CH1.getValue() && chan.getValue() <= IChan.RefActive.getValue());
    }

    /**
     * 判断是否为串行通道S1-S4
     * 
     * @param chan 待判断的通道
     * @return 如果是S1-S4返回true，否则返回false
     */
    public static boolean isSerialNo(IChan chan) {
        return (chan.getValue()>= IChan.S1.getValue() && chan.getValue()<=IChan.S4.getValue());
    }

    /**
     * 判断是否为CH1到S4范围内的通道
     * 
     * @param chan 待判断的通道
     * @return 如果在CH1到S4范围内返回true，否则返回false
     */
    public static boolean isCh1ToS4(IChan chan) {
        return chan.getValue()>=IChan.CH1.getValue() && chan.getValue()<=IChan.S4.getValue();
    }

    /**
     * 判断是否为参考通道
     * 
     * @param chan 待判断的通道
     * @return 如果是R1-R8返回true，否则返回false
     */
    public static boolean isRef(IChan chan) {
        return chan.getValue() >= IChan.R1.getValue() && chan.getValue() <= IChan.R8.getValue();
    }

    /**
     * 判断是否为基本光标（4个光标 + 2个联动光标）
     * 
     * <p>包括：Cursor_row_1, Cursor_row_2, Cursor_row_3,
     * Cursor_col_1, Cursor_col_2, Cursor_col_3</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是基本光标返回true，否则返回false
     */
    public static boolean isCursor_base6(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2 || chan == IChan.Cursor_row_3
                || chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2 || chan == IChan.Cursor_col_3);
        return b;
    }

    /**
     * 判断是否为基本光标（4个光标）
     * 
     * <p>包括：Cursor_row_1, Cursor_row_2, Cursor_col_1, Cursor_col_2</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是基本光标返回true，否则返回false
     */
    public static boolean isCursor_base4(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2
                || chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2);
        return b;
    }

    /**
     * 判断是否为联动光标（2个）
     * 
     * <p>包括：Cursor_col_3, Cursor_row_3</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是联动光标返回true，否则返回false
     */
    public static boolean isCursor_link2(IChan chan) {
        boolean b = (chan == IChan.Cursor_col_3 || chan == IChan.Cursor_row_3);
        return b;
    }

    /**
     * 判断是否为多光标
     * 
     * <p>包括：MultiCursor_col1, MultiCursor_col2</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是多光标返回true，否则返回false
     */
    public static boolean isMulCursor(IChan chan) {
        boolean b = (chan == IChan.MultiCursor_col1 || chan == IChan.MultiCursor_col2);
        return b;
    }

    /**
     * 判断是否为行光标
     * 
     * <p>包括：Cursor_row_1, Cursor_row_2</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是行光标返回true，否则返回false
     */
    public static boolean isCursor_row(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2);
        return b;
    }

    /**
     * 判断是否为列光标
     * 
     * <p>包括：Cursor_col_1, Cursor_col_2</p>
     * 
     * @param chan 待判断的通道
     * @return 如果是列光标返回true，否则返回false
     */
    public static boolean isCursor_col(IChan chan) {
        boolean b = (chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2);
        return b;
    }

    // ==================== 工具方法 ====================
    /**
     * 根据通道值获取对应的枚举实例
     * 
     * @param no 通道值
     * @return 对应的枚举实例，如果未找到则返回CH_NULL
     */
    public static IChan toIChan(int no) {
        for (IChan chan : values) {
            if (chan.getValue() == no) {
                return chan;
            }
        }
        return IChan.CH_NULL;
    }

    /**
     * 判断两个通道的值是否相等
     * 
     * @param ch1 第一个通道
     * @param ch2 第二个通道
     * @return 如果值相等返回true，否则返回false
     */
    public static boolean equalsValue(IChan ch1, IChan ch2) {
        return ch1.getValue() == ch2.getValue();
    }

//    public static IChan getMaxDynamicCh() {
//        return IChan.toIChan(GlobalVar.get().getChannelsCount() - 1);
//    }
//
//    public static IChan getMaxDynamicCh(IChan chan) {
//        return IChan.toIChan(chan.getValue() % GlobalVar.get().getChannelsCount());
//    }

    /**
     * 获取通道的显示文本
     * 
     * <p>根据通道类型返回不同的显示文本：</p>
     * <ul>
     *     <li>CH1-CH8：返回通道编号（1-8）</li>
     *     <li>Math1-8：返回"Math"</li>
     *     <li>R1-8：返回"Ref"</li>
     *     <li>S1-S4：返回对应的串行通道名称</li>
     * </ul>
     * 
     * @param chan 通道枚举
     * @return 显示文本，如果通道类型不匹配则返回空字符串
     */
    public static String getChShowTxt(IChan chan) {
        switch (chan) {
            case CH1:
            case CH2:
            case CH3:
            case CH4:
            case CH5:
            case CH6:
            case CH7:
            case CH8:
                return String.valueOf(chan.getValue() + 1);
            case Math1:
            case Math2:
            case Math3:
            case Math4:
            case Math5:
            case Math6:
            case Math7:
            case Math8:
                return "Math";
            case R1:
            case R2:
            case R3:
            case R4:
            case R5:
            case R6:
            case R7:
            case R8:
                return "Ref";
            case S1:
                return "S1";
            case S2:
                return "S2";
            case S3:
                return "S3";
            case S4:
                return "S4";
        }
        return "";
    }


}
