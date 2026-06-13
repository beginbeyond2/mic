package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber;

import com.micsig.tbook.ui.util.StrUtil;

import java.math.BigInteger;

/**
 * Created by yangj on 2017/5/19.
 */

public class KeyBoardNumberUtil implements IDigits, IKeyBoardNumber {
    /**
     * 删除空格
     */
    public static String clearSpace(String s) {
        return s.replace(" ", "");
    }

    /**
     * 去除前面的0
     */
    public static String clearPreZero(String s) {
        s = clearSpace(s);
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            if (!"0".equals(s.substring(i, i + 1))) {
                str = s.substring(i);
                break;
            }
        }
        return str;
    }

    /**
     * 重新计算空格
     */
    public static String reCalculateSpace(String s, int digits) {
        int interval = getInterval(digits);
        String s1 = s.replace(" ", "");
        String s2 = "";
        for (int i = s1.length() - 1; i >= 0; i--) {
            s2 = s1.charAt(i) + s2;
            if ((s1.length() - i) % interval == 0 && i != s1.length() - 1) {
                s2 = " " + s2;
            }
        }
        return s2;
    }

    /**
     * 得到当前的空格之间的间隔
     */
    public static int getInterval(int digits) {
        int interval = 100;
        if (digits == DIGITS_2 || digits == DIGITS_2X) {
            interval = 4;
        } else if (digits == DIGITS_16 || digits == DIGITS_16X) {
            interval = 2;
        }
        return interval;
    }

    /**
     * 用c补足不足的位数,或者从后截取bits位
     */
    public static String toBits(String s, String c, int bits) {
        if (s.length() < bits) {
            int num = bits - s.length();
            for (int i = 0; i < num; i++) {
                s = c + s;
            }
        } else {
            s = s.substring(s.length() - bits, s.length());
        }
        return s;
    }

    /**
     * 用0补足不足的位数,或者从后截取bits位
     */
    public static String toBits(String s, int bits) {
        return toBits(s, "0", bits);
    }

    /**
     * 用0补足不足的位数,保证是偶数位
     */
    public static String toEvenNumberLength(String s) {
        if (s.length() % 2 == 0) {
            return s;
        } else {
            return "0" + s;
        }
    }

    /**
     * 十六进制转二进制
     */
    public static String HToB(String a) {
        String s = clearPreZero(a);
        if (StrUtil.isEmpty(s)) return "0";
        return Integer.toBinaryString(Integer.valueOf(s, 16));
    }

    /**
     * 二进制转十六进制
     */
    public static String BToH(String a) {
        String s = clearPreZero(a);
        if (StrUtil.isEmpty(s)) return "0";
        return Integer.toHexString(Integer.valueOf(s, 2)).toUpperCase();
    }

    /**
     * 任意进制数转为十进制数
     */
    public static String toD(String a, int b) {
        String s = clearPreZero(a);
        if (StrUtil.isEmpty(s)) return "0";
        int aLong = new BigInteger(s, b).intValue();
        return String.valueOf(aLong).toUpperCase();
    }

    /**
     * 任意进制数转为十进制数
     */
    public static String toDLong(String a, int b) {
        String s = clearPreZero(a);
        if (StrUtil.isEmpty(s)) return "0";
        Long aLong = new BigInteger(s, b).longValue();
        return String.valueOf(aLong).toUpperCase();
    }

    //region 查询数字键盘上当前位置的按键的属性

    public static boolean isNumber(int index) {
        return index != INDEX_BS && index != INDEX_KBS && index != INDEX_MBS
                && index != INDEX_ZUO && index != INDEX_ENTER && index != INDEX_POINT;
    }

    public static boolean isEnter(int index) {
        return index == INDEX_ENTER;
    }

    public static boolean isPoint(int index) {
        return index == INDEX_POINT;
    }

    public static boolean isDelete(int index) {
        return index == INDEX_ZUO;
    }

    public static boolean isBs(int index) {
        return index == INDEX_BS;
    }

    public static boolean isKbs(int index) {
        return index == INDEX_KBS;
    }

    public static boolean isMbs(int index) {
        return index == INDEX_MBS;
    }

    private static boolean isDigits2(int index) {
        return index == INDEX_0 || index == INDEX_1;
    }

    private static boolean isDigits2x(int index) {
        return isDigits2(index) || index == INDEX_X;
    }

    private static boolean isDigits4(int index) {
        return isDigits2(index) || index == INDEX_2 || index == INDEX_3;
    }

    private static boolean isDigits8(int index) {
        return isDigits4(index) || index == INDEX_4 || index == INDEX_5
                || index == INDEX_6 || index == INDEX_7;
    }

    private static boolean isDigits10(int index) {
        return isDigits8(index) || index == INDEX_8 || index == INDEX_9;
    }

    private static boolean isDigits16(int index) {
        return isDigits10(index) || index == INDEX_A || index == INDEX_B
                || index == INDEX_C || index == INDEX_D
                || index == INDEX_E || index == INDEX_F;
    }

    private static boolean isDigits16x(int index) {
        return isDigits16(index) || index == INDEX_X;
    }

    private static boolean isDigitsBs(int index) {
        return index == INDEX_BS || index == INDEX_KBS || index == INDEX_MBS
                || isDigitsFloat(index);
    }

    private static boolean isDigits0_8(int index) {
        return isDigits8(index) || index == INDEX_8;
    }

    private static boolean isDigitsFloat(int index) {
        return index != INDEX_X && index != INDEX_A
                && index != INDEX_B && index != INDEX_C
                && index != INDEX_D && index != INDEX_E
                && index != INDEX_F && index != INDEX_BS
                && index != INDEX_KBS && index != INDEX_MBS;
    }

    public static boolean isEnabled(int digits, int index) {
        if (!(index == INDEX_ENTER || index == INDEX_ZUO)) {
            switch (digits) {
                case IDigits.DIGITS_2:
                    return isDigits2(index);
                case IDigits.DIGITS_2X:
                    return isDigits2x(index);
                case IDigits.DIGITS_4:
                    return isDigits4(index);
                case IDigits.DIGITS_8:
                    return isDigits8(index);
                case IDigits.DIGITS_10:
                    return isDigits10(index);
                case IDigits.DIGITS_16:
                    return isDigits16(index);
                case IDigits.DIGITS_16X:
                    return isDigits16x(index);
                case IDigits.DIGITS_BAUDRATE:
                    return isDigitsBs(index);
                case IDigits.DIGITS_0_8:
                    return isDigits0_8(index);
                case IDigits.DIGITS_FLOAT:
                    return isDigitsFloat(index);
            }
        }
        return true;
    }
    //endregion
}
