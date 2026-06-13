package com.micsig.base.filter;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.util.regex.Pattern;

public class UnitInputFilter implements InputFilter {

    // 正则表达式：可选负号、数字（整数或小数）、后接单个指定字符
    private static final String REGEX = "^(\\d+\\.?\\d*|\\.\\d+)$";
    private Pattern mPattern = Pattern.compile(REGEX);
    public UnitInputFilter(String units){
        if(units == null
                || units.isEmpty()){
            mPattern = Pattern.compile("^-?($|(\\d+\\.?\\d*|\\.\\d+))$");
        }else {
            mPattern = Pattern.compile("^-?($|(\\d+\\.?\\d*|\\.\\d+))[" + units + "]?$");
        }
    }
    public UnitInputFilter(){

    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        // 构造输入后的新字符串
        String newString = dest.subSequence(0, dstart) + source.subSequence(start, end).toString() + dest.subSequence(dend, dest.length());
        // 如果新字符串为空或符合正则，允许输入
        if (newString.isEmpty() || mPattern.matcher(newString).matches()) {
            return null; // 允许输入
        }
        return ""; // 拒绝输入
    }
}