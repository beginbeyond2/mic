package com.micsig.tbook.ui.wavezone;



/**
 * @auother Liwb
 * @description:
 * @data:2023-8-10 15:33
 */
public enum IChan {
    CH_NULL(-1, 0xFF202020, "NULL"),
    /**
     * value :0
     */
    CH1(0, 0xFFFFE700, "CH1"),
    CH2(1, 0xFF00E4FF, "CH2"),
    CH3(2, 0xFFFF0031, "CH3"),
    CH4(3, 0xFFB6FF00, "CH4"),
    CH5(4, 0xFFFF9000, "CH5"),
    CH6(5, 0xFF0037FF, "CH6"),
    CH7(6, 0xFFFF0086, "CH7"),
    CH8(7, 0xFF00FF91, "CH8"),

    Math1(8, 0xFFFF00FF, "Math1"),
    Math2(9, 0xFFFF00FF, "Math2"),
    Math3(10, 0xFFFF00FF, "Math3"),
    Math4(11, 0xFFFF00FF, "Math4"),
    Math5(12, 0xFFFF00FF, "Math5"),
    Math6(13, 0xFFFF00FF, "Math6"),
    Math7(14, 0xFFFF00FF, "Math7"),
    Math8(15, 0xFFFF00FF, "Math8"),

    R1(16, 0xFF807FFF, "R1"),
    R2(17, 0xFF807FFF, "R2"),
    R3(18, 0xFF807FFF, "R3"),
    R4(19, 0xFF807FFF, "R4"),
    R5(20, 0xFF807FFF, "R5"),
    R6(21, 0xFF807FFF, "R6"),
    R7(22, 0xFF807FFF, "R7"),
    R8(23, 0xFF807FFF, "R8"),

    S1(24, 0xFF806040, "S1"),
    S2(25, 0xFFA4DBDD, "S2"),
    S3(26, 0xFF406080, "S3"),
    S4(27, 0xFF408040, "S4"),
    RefActive(28, 0xFF807FFF, "RefActive"),
    S1S2(29, 0xFF202020, "S1S2"),

    Math_FFT(30, 0xFFFF0000, "Math"),

    /**
     * rowCursor
     */
    Cursor_col_1(0x41, 0xFF202020, "Col1"),//行
    Cursor_col_2(0x42, 0xFF202020, "Col2"),
    /**
     * link rowCursor 联动
     */
    Cursor_col_3(0x43, 0xFF202020, "Col3"),//行联动，点击不选中
    Cursor_col_4(0x44, 0xFF202020, "Col4"),//行联动，点击选中

    /**
     * colCursor
     */
    Cursor_row_1(0x51, 0xFF202020, "Row1"),//列
    Cursor_row_2(0x52, 0xFF202020, "Row2"),
    /**
     * link colCursor 联动
     */
    Cursor_row_3(0x53, 0xFF202020, "Row3"),//列联动，点击不选中
    Cursor_row_4(0x54, 0xFF202020, "Row4"),//列联动，点击选中

    TriggerTime(0x60, 0xFF202020, "TriggerTime"),
    TriggerLevel(0x61, 0xFF202020, "TriggerLevel"),
    ValueLevel(0x62,0xFF202020,"ValueLevel"),
    /**
     * multiCursor 多光标
     */
    MultiCursor_col1(0x70, 0xFFD2691E, "Multi Col1"), //第一条线
    MultiCursor_col2(0x71, 0xFFD2691E, "Multi Col2"); //第二条线

    private final int value;
    private final int color;
    private final String name;

    private IChan(int ch, int color, String name) {
        this.value = ch;
        this.color = color;
        this.name = name;
    }

    public  int getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public static IChan[] values;

    static {
        values = values();
    }

    public static boolean isCh1ToCh8(IChan chan) {
        return (chan.getValue()>=IChan.CH1.getValue() &&  chan.getValue()<= IChan.CH8.getValue());
    }

    public static boolean isR1ToR8(IChan chan) {
        return (chan.getValue() >= IChan.R1.getValue() && chan.getValue() <= IChan.R8.getValue());
    }

    public static boolean isCh1ToMath(IChan chan) {
        return (chan.getValue() >= IChan.CH1.getValue() && chan.getValue() <= IChan.Math8.getValue());
    }

    public static boolean isMathOrRef(IChan chan) {
        return (chan.getValue()>= IChan.Math1.getValue() && chan.getValue()<=IChan.R8.getValue());
    }

    public static boolean isMathToS4(IChan chan) {
        return (chan.getValue() >= IChan.Math1.getValue() && chan.getValue() <= IChan.S4.getValue());
    }

    public static boolean isCh1ToRefActive(IChan chan) {
        return (chan.getValue() >= IChan.CH1.getValue() && chan.getValue() <= IChan.RefActive.getValue());
    }

    public static boolean isSerialNo(IChan chan) {
        return (chan.getValue()>= IChan.S1.getValue() && chan.getValue()<=IChan.S4.getValue());
    }

    public static boolean isCh1ToS4(IChan chan) {
        return chan.getValue()>=IChan.CH1.getValue() && chan.getValue()<=IChan.S4.getValue();
    }

    public static boolean isRef(IChan chan) {
        return chan.getValue() >= IChan.R1.getValue() && chan.getValue() <= IChan.R8.getValue();
    }

    /**
     * 4 cursor + 2 link cursor
     *
     * @param chan
     * @return
     */
    public static boolean isCursor_base6(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2 || chan == IChan.Cursor_row_3
                || chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2 || chan == IChan.Cursor_col_3);
        return b;
    }

    public static boolean isCursor_base4(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2
                || chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2);
        return b;
    }

    public static boolean isCursor_link2(IChan chan) {
        boolean b = (chan == IChan.Cursor_col_3 || chan == IChan.Cursor_row_3);
        return b;
    }

    public static boolean isMulCursor(IChan chan) {
        boolean b = (chan == IChan.MultiCursor_col1 || chan == IChan.MultiCursor_col2);
        return b;
    }

    public static boolean isCursor_row(IChan chan) {
        boolean b = (chan == IChan.Cursor_row_1 || chan == IChan.Cursor_row_2);
        return b;
    }

    public static boolean isCursor_col(IChan chan) {
        boolean b = (chan == IChan.Cursor_col_1 || chan == IChan.Cursor_col_2);
        return b;
    }

    public static IChan toIChan(int no) {
        for (IChan chan : values) {
            if (chan.getValue() == no) {
                return chan;
            }
        }
        return IChan.CH_NULL;
    }

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
