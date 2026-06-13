package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.util.CacheUtil;

public class SerialsUtils {

    /**
     * 将带空格的二进制十六进制转换，转换之后依然带空格
     */
    public static String HexBin(String text, int preDigits, int digits) {
        if (preDigits == IDigits.DIGITS_2 && digits == IDigits.DIGITS_16) {
            return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.BToH(text.replace(" ", "")), IDigits.DIGITS_16).trim();
        } else if (preDigits == IDigits.DIGITS_16 && digits == IDigits.DIGITS_2) {
            return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.HToB(text.replace(" ", "")), IDigits.DIGITS_2).trim();
        } else {
            return text;
        }
    }

    /**
     * 将目标数字去空格，补位数，重新计算空格
     */
    public static String reCalcSpace(String s, int bits, int digits) {
        return KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(s.replace(" ", ""), bits), digits).trim();
    }

    /**
     * //该为2进制时，根据格式依次为，23、21、19位限制//16进制时，根据格式依次为6、6、5位
     */
    public static int getBitFor429Data(int serialsNumber) {
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber);
        int digit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber);
        if (digit == 0) {
            if (format == 0) {
                return 23;
            } else if (format == 1) {
                return 21;
            } else {
                return 19;
            }
        } else {
            if (format == 0 || format == 1) {
                return 6;
            } else {
                return 5;
            }
        }
    }

    public static int getBitInBinFor429Data(int serialsNumber) {
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber);
        if (format == 0) {
            return 23;
        } else if (format == 1) {
            return 21;
        } else {
            return 19;
        }
    }

    /**
     * 将任意进制带空格数字转换为10进制int
     *
     * @param text   数字
     * @param digits 进制
     * @return
     */
    public static int toD(String text, int digits) {
        text = text.replace(" ", "");
        return Integer.parseInt(KeyBoardNumberUtil.toD(text, digits));
    }

    public static long toDLong(String text, int digits) {
        text = text.replace(" ", "");
        return Long.parseLong(KeyBoardNumberUtil.toDLong(text, digits));
    }

    public static int getCanDlcFromShow(String text) {
        if ("12".equals(text)) {
            return 9;
        } else if ("16".equals(text)) {
            return 10;
        } else if ("20".equals(text)) {
            return 11;
        } else if ("24".equals(text)) {
            return 12;
        } else if ("32".equals(text)) {
            return 13;
        } else if ("48".equals(text)) {
            return 14;
        } else if ("64".equals(text)) {
            return 15;
        } else {
            return Integer.parseInt(text);
        }
    }

    public static int getCanDlcFromScope(int text) {
        if (9 == text) {
            return 12;
        } else if (10 == text) {
            return 16;
        } else if (11 == text) {
            return 20;
        } else if (12 == text) {
            return 24;
        } else if (13 == text) {
            return 32;
        } else if (14 == text) {
            return 48;
        } else if (15 == text) {
            return 64;
        } else {
            return text;
        }
    }

    public static int getConditionValueToEventBus(int indexCondition) {
        switch (indexCondition) {
            case 0:
                return IBus.TRIGGER_RELATION_LESS_THAN;
            case 1:
                return IBus.TRIGGER_RELATION_MORE_THAN;
            case 2:
                return IBus.TRIGGER_RELATION_EQUAL;
            case 3:
                return IBus.TRIGGER_RELATION_NOT_EQUAL;
            default:
                return IBus.TRIGGER_RELATION_LESS_THAN;
        }
    }

    public static int getConditionValueFromEventBus(int indexCondition) {
        switch (indexCondition) {
            case IBus.TRIGGER_RELATION_LESS_THAN:
                return 0;
            case IBus.TRIGGER_RELATION_MORE_THAN:
                return 1;
            case IBus.TRIGGER_RELATION_EQUAL:
                return 2;
            case IBus.TRIGGER_RELATION_NOT_EQUAL:
                return 3;
            default:
                return 0;
        }
    }

    public static String getSpiMask(String data) {
        data = data.replace(" ", "");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            if ('X' == data.charAt(i)) {
                result.append('0');
            } else {
                result.append('1');
            }
        }
        return result.toString();
    }

    public static String getSpiData(String data) {
        data = data.replace(" ", "");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            if ('1' == data.charAt(i)) {
                result.append('1');
            } else {
                result.append('0');
            }
        }
        return result.toString();
    }

    /**
     * Text 11 00 XX
     * mask	11 11 00
     * data	11 00 00
     */
    public static String getSpiText(int mask, int data) {
        String sMask = Integer.toBinaryString(mask);
        String sData = Integer.toBinaryString(data);
        String result = "";
        while (sMask.length() < sData.length()) {
            sMask = "0" + sMask;
        }
        while (sData.length() < sMask.length()) {
            sData = "0" + sData;
        }
        for (int i = 0; i < sMask.length(); i++) {
            char cMask = sMask.charAt(i);
            char cdata = sData.charAt(i);
            if (cMask == '0') {
                result += "X";
            } else if (cdata == '1') {
                result += "1";
            } else {
                result += "0";
            }
        }
        return result;
    }

    /**
     * 将long类型的数据转换成十六进制、二进制类型的带空格的数据
     *
     * @param data   long类型的原始数据
     * @param bits   最后数字的显示位数
     * @param digits 最后数字的显示进制数
     */
    public static String getHexBinFromLong(long data, int bits, int digits) {
        String sData;
        if (digits == IDigits.DIGITS_16) {
            sData = Long.toHexString(data);
        } else if (digits == IDigits.DIGITS_2) {
            sData = Long.toBinaryString(data);
        } else if (digits == IDigits.DIGITS_8) {
            sData = Long.toOctalString(data);
        } else {
            sData = String.valueOf(data);
        }
        sData = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(
                sData.replace(" ", ""), bits), digits).trim().toUpperCase();
        return sData;
    }

    /**
     * 将int类型的数据转换成十六进制、二进制类型的带空格的数据
     *
     * @param data   int类型的原始数据
     * @param bits   最后数字的显示位数
     * @param digits 最后数字的显示进制数
     */
    public static String getHexBinFromInt(int data, int bits, int digits) {
        String sData;
        if (digits == IDigits.DIGITS_16) {
            sData = Integer.toHexString(data);
        } else if (digits == IDigits.DIGITS_2) {
            sData = Integer.toBinaryString(data);
        } else if (digits == IDigits.DIGITS_8) {
            sData = Integer.toOctalString(data);
        } else {
            sData = String.valueOf(data);
        }
        sData = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(
                sData.replace(" ", ""), bits), digits).trim();
        return sData.toUpperCase();
    }
}
