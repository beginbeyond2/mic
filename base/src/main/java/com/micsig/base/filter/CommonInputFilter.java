package com.micsig.base.filter;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class CommonInputFilter implements InputFilter {

    private Pattern pattern;

    public CommonInputFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // 构造输入后的新字符串
        String newString = dest.subSequence(0, dstart) + source.subSequence(start, end).toString() + dest.subSequence(dend, dest.length());
        // 如果新字符串为空或符合正则，允许输入
        if (newString.isEmpty() || pattern.matcher(newString).matches()) {
            return null; // 允许输入
        }
        return ""; // 拒绝输入
    }
}
