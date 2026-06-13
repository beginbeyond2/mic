package com.micsig.base.filter;

import android.text.InputFilter;

public class FilterFactory {

    public static final String BAUDRATE_REGEX = "^\\d*\\.?\\d*$";//波特率
    public static final String BINARY_REGEX  = "^[01 ]*$";//二进制
    public static final String BINARY_X_REGEX  = "^[01Xx ]*$";//二进制 和 大写X
    public static final String QUATERNARY_REGEX  = "^[0-3 ]*$";//四进制
    public static final String OCTAL_REGEX = "^[0-7 ]*$";//八进制
    public static final String DECIMAL_REGEX = "^[0-9 ]*$";//十进制
    public static final String HEX_REGEX = "^[0-9A-Fa-f ]*$";//十六进制
    public static final String HEX_X_REGEX = "^[0-9A-Fa-fXx ]*$";//十六进制 和 大写X
    public static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";


    public static InputFilter getFilter(String regex) {
        return new CommonInputFilter(regex);
    }


}
