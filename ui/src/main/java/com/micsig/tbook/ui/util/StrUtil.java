package com.micsig.tbook.ui.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * <p>提供全面的字符串处理功能，包括：</p>
 * <ul>
 *   <li>语言检测：判断当前系统语言（中文/英文）</li>
 *   <li>空值处理：判断空值、转换空值</li>
 *   <li>长度计算：计算字符串长度（支持中文字符）</li>
 *   <li>格式验证：手机号、邮箱、数字、字母等格式验证</li>
 *   <li>字符串截取：按字节长度截取字符串</li>
 *   <li>类型转换：输入流转字符串、IP地址转换</li>
 *   <li>集合操作：字符串与List互转、数组合并</li>
 * </ul>
 * 
 * <p>中文字符处理说明：</p>
 * <p>本类中中文字符的判断使用Unicode范围[\u0391-\uFFE5]，包含希腊字母和CJK统一表意文字。</p>
 * 
 * @author ZhangDI
 * @version 1.0
 * @since 2024
 */
public class StrUtil {

    /**
     * 判断当前系统语言是否是简体中文
     * 
     * <p>通过检查Locale判断系统语言是否为zh_cn（简体中文）。</p>
     *
     * @return true表示当前系统语言是简体中文，false表示其他语言
     * 
     * @see #getLanguage() 获取当前系统语言代码
     */
    public static boolean isLangCn() {
        return getLanguage().equals("zh_cn");
    }

    /**
     * 获取当前系统语言代码
     * 
     * <p>返回格式为"语言_国家"的小写形式，如"zh_cn"、"en_us"。</p>
     *
     * @return 语言代码字符串，格式为"语言_国家"
     */
    public static String getLanguage() {
        // 获取系统默认Locale
        Locale l = Locale.getDefault();
        // 返回"语言_国家"格式的小写字符串
        return l.getLanguage().toLowerCase() +"_"+ l.getCountry().toLowerCase();
    }

    /**
     * 判断当前系统语言是否是英文
     * 
     * <p>通过检查Locale的toString()是否包含"en"来判断。</p>
     *
     * @return true表示当前系统语言是英文，false表示其他语言
     */
    public static boolean isLangEn() {
        return Locale.getDefault().toString().contains("en");
    }

    /**
     * 将资源ID转换为字符串
     * 
     * <p>通过Context获取资源ID对应的字符串资源。</p>
     *
     * @param context 上下文对象
     * @param str     字符串资源ID
     * @return 资源ID对应的字符串
     */
    public static String intToString(Context context, int str) {
        return context.getString(str);
    }

    /**
     * 将null转换为空字符串
     * 
     * <p>如果字符串为null或"null"（忽略大小写和空格），则转换为空字符串。</p>
     * <p>返回的字符串会去除首尾空格。</p>
     *
     * @param str 指定的字符串
     * @return 处理后的字符串，不会为null
     */
    public static String parseEmpty(String str) {
        // 检查是否为null或字符串"null"
        if (str == null || "null".equals(str.trim())) {
            str = "";
        }
        // 返回去除首尾空格的字符串
        return str.trim();
    }

    /**
     * 判断字符串是否为null或空值
     * 
     * <p>以下情况返回true：</p>
     * <ul>
     *   <li>字符串为null</li>
     *   <li>字符串trim后长度为0</li>
     *   <li>字符串为"null"</li>
     * </ul>
     *
     * @param str 指定的字符串
     * @return true表示为空，false表示不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0 || "null".equals(str);
    }

    /**
     * 获取字符串中中文字符的长度
     * 
     * <p>遍历字符串，统计中文字符的数量，每个中文字符计为2个长度单位。</p>
     * <p>中文字符的Unicode范围：[\u0391-\uFFE5]。</p>
     *
     * @param str 指定的字符串
     * @return 中文字符的长度（每个中文算2个字符）
     */
    public static int chineseLength(String str) {
        int valueLength = 0; // 中文字符长度累计
        String chinese = "[\u0391-\uFFE5]"; // 中文字符正则表达式
        
        // 如果字符串不为空
        if (!isEmpty(str)) {
            // 遍历字符串的每个字符
            for (int i = 0; i < str.length(); i++) {
                // 获取单个字符
                String temp = str.substring(i, i + 1);
                // 判断是否为中文字符
                if (temp.matches(chinese)) {
                    valueLength += 2; // 中文字符长度为2
                }
            }
        }
        return valueLength;
    }

    /**
     * 获取字符串的长度
     * 
     * <p>计算字符串长度，中文字符计2个长度，其他字符计1个长度。</p>
     * <p>中文字符的Unicode范围：[\u0391-\uFFE5]。</p>
     *
     * @param str 指定的字符串
     * @return 字符串的长度（中文字符计2个）
     */
    public static int strLength(String str) {
        int valueLength = 0; // 总长度累计
        String chinese = "[\u0391-\uFFE5]"; // 中文字符正则表达式
        
        // 如果字符串不为空
        if (!isEmpty(str)) {
            // 遍历字符串的每个字符
            for (int i = 0; i < str.length(); i++) {
                // 获取单个字符
                String temp = str.substring(i, i + 1);
                // 判断是否为中文字符
                if (temp.matches(chinese)) {
                    valueLength += 2; // 中文字符长度为2
                } else {
                    valueLength += 1; // 其他字符长度为1
                }
            }
        }
        return valueLength;
    }

    /**
     * 获取指定长度的字符所在位置
     * 
     * <p>从字符串开头计算，找到达到指定长度时的字符索引位置。</p>
     * <p>长度计算规则：中文字符计2个长度，其他字符计1个长度。</p>
     *
     * @param str  指定的字符串
     * @param maxL 要取到的长度（字符长度，中文字符计2个）
     * @return 达到指定长度时的字符索引位置
     */
    public static int subStringLength(String str, int maxL) {
        int currentIndex = 0; // 当前字符索引
        int valueLength = 0;  // 累计长度
        String chinese = "[\u0391-\uFFE5]"; // 中文字符正则表达式
        
        // 遍历字符串的每个字符
        for (int i = 0; i < str.length(); i++) {
            // 获取单个字符
            String temp = str.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                valueLength += 2; // 中文字符长度为2
            } else {
                valueLength += 1; // 其他字符长度为1
            }
            // 检查是否达到指定长度
            if (valueLength >= maxL) {
                currentIndex = i;
                break;
            }
        }
        return currentIndex;
    }

    /**
     * 验证手机号格式
     * 
     * <p>支持的手机号段：13x、15x（不含154）、17x、18x。</p>
     * <p>手机号必须为11位数字。</p>
     *
     * @param str 指定的手机号码字符串
     * @return true表示是有效的手机号码格式，false表示无效
     */
    public static Boolean isMobileNo(String str) {
        Boolean isMobileNo = false;
        try {
            // 手机号正则表达式：13x、15x（不含154）、17x、18x开头，后跟8位数字
            Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$");
            Matcher m = p.matcher(str);
            isMobileNo = m.matches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMobileNo;
    }

    /**
     * 判断字符串是否只包含字母和数字
     * 
     * <p>字符串必须至少包含一个字符，且所有字符都是字母或数字。</p>
     *
     * @param str 指定的字符串
     * @return true表示只包含字母和数字，false表示包含其他字符
     */
    public static Boolean isNumberLetter(String str) {
        Boolean isNoLetter = false;
        String expr = "^[A-Za-z0-9]+$"; // 字母数字正则表达式
        if (str.matches(expr)) {
            isNoLetter = true;
        }
        return isNoLetter;
    }

    /**
     * 判断字符串是否只包含字母
     * 
     * <p>字符串必须至少包含一个字符，且所有字符都是字母（大小写均可）。</p>
     *
     * @param str 指定的字符串
     * @return true表示只包含字母，false表示包含其他字符
     */
    public static Boolean isLetter(String str) {
        Boolean isNoLetter = false;
        String expr = "^[A-Za-z]+$"; // 字母正则表达式
        if (str.matches(expr)) {
            isNoLetter = true;
        }
        return isNoLetter;
    }

    /**
     * 判断字符串是否只包含数字
     * 
     * <p>字符串必须至少包含一个字符，且所有字符都是数字。</p>
     *
     * @param str 指定的字符串
     * @return true表示只包含数字，false表示包含其他字符
     */
    public static Boolean isNumber(String str) {
        Boolean isNumber = false;
        String expr = "^[0-9]+$"; // 数字正则表达式
        if (str.matches(expr)) {
            isNumber = true;
        }
        return isNumber;
    }

    /**
     * 判断字符串是否是有效的邮箱格式
     * 
     * <p>邮箱格式验证，支持常见的邮箱格式。</p>
     *
     * @param str 指定的字符串
     * @return true表示是有效的邮箱格式，false表示无效
     */
    public static Boolean isEmail(String str) {
        Boolean isEmail = false;
        // 邮箱正则表达式
        String expr = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        if (str.matches(expr)) {
            isEmail = true;
        }
        return isEmail;
    }

    /**
     * 判断字符串是否全部是中文字符
     * 
     * <p>遍历字符串的每个字符，检查是否都在中文字符范围内。</p>
     * <p>中文字符的Unicode范围：[\u0391-\uFFE5]。</p>
     *
     * @param str 指定的字符串
     * @return true表示全部是中文字符，false表示包含非中文字符
     */
    public static Boolean isChinese(String str) {
        Boolean isChinese = true;
        String chinese = "[\u0391-\uFFE5]"; // 中文字符正则表达式
        
        // 如果字符串不为空
        if (!isEmpty(str)) {
            // 遍历字符串的每个字符
            for (int i = 0; i < str.length(); i++) {
                // 获取单个字符
                String temp = str.substring(i, i + 1);
                // 判断是否为中文字符
                if (temp.matches(chinese)) {
                    // 是中文字符，继续检查
                } else {
                    isChinese = false; // 发现非中文字符
                }
            }
        }
        return isChinese;
    }

    /**
     * 判断字符串是否只含有汉字、数字、字母、下划线，且不能以下划线开头和结尾
     * 
     * <p>用于验证用户名等需要特定格式的字符串。</p>
     *
     * @param str 指定的字符串
     * @return true表示符合格式要求，false表示不符合
     */
    public static Boolean isRealName(String str) {
        // 正则表达式：不能以下划线开头和结尾，只能包含汉字、数字、字母、下划线
        String expr = "^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        return str.matches(expr);
    }

    /**
     * 判断字符串是否包含中文字符
     * 
     * <p>遍历字符串的每个字符，检查是否存在中文字符。</p>
     * <p>中文字符的Unicode范围：[\u0391-\uFFE5]。</p>
     *
     * @param str 指定的字符串
     * @return true表示包含中文字符，false表示不包含
     */
    public static Boolean isContainChinese(String str) {
        Boolean isChinese = false;
        String chinese = "[\u0391-\uFFE5]"; // 中文字符正则表达式
        
        // 如果字符串不为空
        if (!isEmpty(str)) {
            // 遍历字符串的每个字符
            for (int i = 0; i < str.length(); i++) {
                // 获取单个字符
                String temp = str.substring(i, i + 1);
                // 判断是否为中文字符
                if (temp.matches(chinese)) {
                    isChinese = true; // 发现中文字符
                }
            }
        }
        return isChinese;
    }

    /**
     * 判断字符串是否包含GB2312编码的中文字符
     * 
     * <p>通过检查字符的字节编码判断是否为GB2312中文字符。</p>
     * <p>GB2312编码范围：第一字节0x81-0xFE，第二字节0x40-0xFE。</p>
     *
     * @param str 要检查的字符串
     * @return true表示包含GB2312中文字符，false表示不包含
     */
    public static boolean vd(String str) {
        // 将字符串转换为字符数组
        char[] chars = str.toCharArray();
        boolean isGB2312 = false;
        
        // 遍历每个字符
        for (int i = 0; i < chars.length; i++) {
            // 获取字符的字节表示
            byte[] bytes = ("" + chars[i]).getBytes();
            
            // 检查是否为双字节字符
            if (bytes.length == 2) {
                int[] ints = new int[2];
                ints[0] = bytes[0] & 0xff; // 第一字节（无符号）
                ints[1] = bytes[1] & 0xff; // 第二字节（无符号）
                
                // 检查是否在GB2312编码范围内
                if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40 && ints[1] <= 0xFE) {
                    isGB2312 = true;
                    break;
                }
            }
        }
        return isGB2312;
    }

    /**
     * 从输入流中读取字符串
     * 
     * <p>使用BufferedReader逐行读取输入流，拼接成完整字符串。</p>
     * <p>读取完成后会自动关闭输入流。</p>
     *
     * @param is 输入流
     * @return 读取的字符串内容
     */
    public static String convertStreamToString(InputStream is) {
        // 创建缓冲读取器
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        
        try {
            // 逐行读取
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            // 删除最后一个多余的换行符
            if (sb.indexOf("\n") != -1 && sb.lastIndexOf("\n") == sb.length() - 1) {
                sb.delete(sb.lastIndexOf("\n"), sb.lastIndexOf("\n") + 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭输入流
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 标准化日期时间格式
     * 
     * <p>将日期时间字符串中的不足两位的数字补0。</p>
     * <p>例如："2012-3-2 12:2:20" 转换为 "2012-03-02 12:02:20"。</p>
     *
     * @param dateTime 预格式的时间字符串，如:2012-3-2 12:2:20
     * @return 格式化好的时间字符串，如:2012-03-02 12:02:20；如果输入为空则返回null
     */
    public static String dateTimeFormat(String dateTime) {
        StringBuilder sb = new StringBuilder();
        try {
            // 检查输入是否为空
            if (isEmpty(dateTime)) {
                return null;
            }
            
            // 分割日期和时间部分
            String[] dateAndTime = dateTime.split(" ");
            
            if (dateAndTime.length > 0) {
                for (String str : dateAndTime) {
                    if (str.indexOf("-") != -1) {
                        // 处理日期部分（用-分隔）
                        String[] date = str.split("-");
                        for (int i = 0; i < date.length; i++) {
                            String str1 = date[i];
                            sb.append(strFormat2(str1)); // 补0
                            if (i < date.length - 1) {
                                sb.append("-");
                            }
                        }
                    } else if (str.indexOf(":") != -1) {
                        // 处理时间部分（用:分隔）
                        sb.append(" ");
                        String[] date = str.split(":");
                        for (int i = 0; i < date.length; i++) {
                            String str1 = date[i];
                            sb.append(strFormat2(str1)); // 补0
                            if (i < date.length - 1) {
                                sb.append(":");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    /**
     * 字符串格式化为至少2位
     * 
     * <p>如果字符串长度不足2位，在前面补"0"。</p>
     * <p>例如："3" 转换为 "03"。</p>
     *
     * @param str 指定的字符串
     * @return 至少2个字符的字符串
     */
    public static String strFormat2(String str) {
        try {
            // 如果长度不足2位，在前面补0
            if (str.length() <= 1) {
                str = "0" + str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 截取字符串到指定字节长度
     * 
     * <p>按字节长度截取字符串，不添加省略符号。</p>
     *
     * @param str    原字符串
     * @param length 指定字节长度
     * @return 截取后的字符串
     * 
     * @see #cutString(String, int, String) 截取字符串（带省略符号）
     */
    public static String cutString(String str, int length) {
        return cutString(str, length, "");
    }

    /**
     * 截取字符串到指定字节长度
     * 
     * <p>按GBK编码计算字节长度，截取字符串。</p>
     * <p>中文字符占2个字节，其他字符占1个字节。</p>
     *
     * @param str    原字符串
     * @param length 指定字节长度
     * @param dot    省略符号（如"..."），可为null
     * @return 截取后的字符串，如果原字符串长度不超过指定长度则返回原字符串
     */
    public static String cutString(String str, int length, String dot) {
        // 计算字符串的字节长度（GBK编码）
        int strBLen = strlen(str, "GBK");
        
        // 如果字节长度不超过指定长度，直接返回
        if (strBLen <= length) {
            return str;
        }
        
        int temp = 0; // 累计字节长度
        StringBuffer sb = new StringBuffer(length);
        char[] ch = str.toCharArray();
        
        // 遍历每个字符
        for (char c : ch) {
            sb.append(c);
            // 计算字节长度（中文字符>256，占2字节）
            if (c > 256) {
                temp += 2;
            } else {
                temp += 1;
            }
            // 检查是否达到指定长度
            if (temp >= length) {
                if (dot != null) {
                    sb.append(dot); // 添加省略符号
                }
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 从第一个指定字符开始截取字符串
     * 
     * <p>查找字符串中第一个出现的指定字符，从该位置偏移offset后截取。</p>
     *
     * @param str1   原字符串
     * @param str2   指定字符
     * @param offset 偏移的索引（从找到的位置开始偏移）
     * @return 截取后的字符串；如果未找到指定字符或偏移后超出范围，返回空字符串
     */
    public static String cutStringFromChar(String str1, String str2, int offset) {
        // 检查原字符串是否为空
        if (isEmpty(str1)) {
            return "";
        }
        
        // 查找指定字符的位置
        int start = str1.indexOf(str2);
        
        if (start != -1) {
            // 检查偏移后是否超出字符串范围
            if (str1.length() > start + offset) {
                return str1.substring(start + offset);
            }
        }
        return "";
    }

    /**
     * 获取字符串的字节长度
     * 
     * <p>按指定字符集计算字符串的字节长度。</p>
     *
     * @param str     字符串
     * @param charset 字符集名称（如"GBK"、"UTF-8"）
     * @return 字节长度；如果字符串为空或发生异常返回0
     */
    public static int strlen(String str, String charset) {
        // 检查字符串是否为空
        if (str == null || str.length() == 0) {
            return 0;
        }
        
        int length = 0;
        try {
            // 按指定字符集计算字节长度
            length = str.getBytes(charset).length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    /**
     * 获取文件大小的描述字符串
     * 
     * <p>将字节数转换为易读的大小描述，自动选择合适的单位（B/K/M/G）。</p>
     * <p>例如：1024 -> "1K"，1048576 -> "1M"。</p>
     *
     * @param size 字节个数
     * @return 大小的描述字符串，如"1K"、"2M"、"3G"
     */
    public static String getSizeDesc(long size) {
        String suffix = "B"; // 默认单位为字节
        
        // 大于等于1024字节，转换为KB
        if (size >= 1024) {
            suffix = "K";
            size = size >> 10; // 除以1024
            // 大于等于1024KB，转换为MB
            if (size >= 1024) {
                suffix = "M";
                size = size >> 10; // 除以1024
                // 大于等于1024MB，转换为GB
                if (size >= 1024) {
                    suffix = "G";
                    size = size >> 10; // 除以1024
                }
            }
        }
        return size + suffix;
    }

    /**
     * IP地址转换为10进制数
     * 
     * <p>将点分十进制IP地址转换为长整型数值。</p>
     * <p>例如："192.168.1.1" -> 3232235777。</p>
     *
     * @param ip 点分十进制IP地址字符串
     * @return IP地址对应的10进制数值
     */
    public static long ip2int(String ip) {
        // 将点号替换为逗号
        ip = ip.replace(".", ",");
        // 分割IP地址的四个部分
        String[] items = ip.split(",");
        // 将四个部分组合成一个32位整数
        return Long.valueOf(items[0]) << 24 | Long.valueOf(items[1]) << 16 | Long.valueOf(items[2]) << 8 | Long.valueOf(items[3]);
    }

    /**
     * 判断字符串是否是有效的颜色值
     * 
     * <p>支持的颜色格式：</p>
     * <ul>
     *   <li>#RGB（3位十六进制）</li>
     *   <li>#RRGGBB（6位十六进制）</li>
     * </ul>
     *
     * @param color 颜色字符串
     * @return true表示是有效的颜色值格式，false表示无效
     */
    public static boolean isColor(String color) {
        if (!isEmpty(color)) {
            // 颜色值正则表达式：#后跟3位或6位十六进制字符
            return color.matches("^#([a-f]|[A-F]|[0-9]){3}(([a-f]|[A-F]|[0-9]){3})?$");
        }
        return false;
    }

    /**
     * 比较两个字符串是否相等
     * 
     * <p>空值处理规则：</p>
     * <ul>
     *   <li>null、"null"、空字符串视为等价</li>
     *   <li>比较时会去除首尾空格</li>
     * </ul>
     *
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return true表示相等，false表示不相等
     */
    public static boolean equals(String s1, String s2) {
        // 处理第一个字符串为空的情况
        if (s1 == null || "null".equalsIgnoreCase(s1.trim()) || "".equals(s1.trim())) {
            return (s2 == null || "null".equalsIgnoreCase(s2.trim()) || "".equals(s2.trim()));
        } 
        // 处理第一个字符串不为空的情况
        else if (s2 != null) {
            return s1.trim().equals((s2.trim()));
        } else {
            return false;
        }
    }

    /**
     * 判断字符串是否是URL
     * 
     * <p>检查字符串是否以"http"或"www"开头。</p>
     *
     * @param url 要检查的字符串
     * @return true表示是URL格式，false表示不是
     */
    public static boolean isUrl(String url) {
        return url.startsWith("http") || url.startsWith("www");
    }

    /**
     * 合并两个字符串数组
     * 
     * <p>将两个字符串数组合并为一个新的数组，s1在前，s2在后。</p>
     *
     * @param s1 第一个字符串数组
     * @param s2 第二个字符串数组
     * @return 合并后的新数组；如果任一数组为null，返回null
     */
    public static String[] add(String[] s1, String[] s2) {
        // 检查两个数组是否都不为空
        if (s1 != null && s2 != null) {
            // 创建新数组
            String[] result = new String[s1.length + s2.length];
            // 复制第一个数组
            for (int i = 0; i < s1.length; i++) {
                result[i] = s1[i];
            }
            // 复制第二个数组
            for (int i = 0; i < s2.length; i++) {
                result[s1.length + i] = s2[i];
            }
            return result;
        }
        return null;
    }

    /**
     * 将字符串按分隔符分割为ArrayList
     * 
     * <p>使用指定的分隔符将字符串分割成多个部分，存入ArrayList。</p>
     *
     * @param s    要分割的字符串
     * @param flag 分隔符
     * @return 分割后的ArrayList；如果字符串为空，返回空列表
     */
    public static ArrayList<String> getListFromString(String s, String flag) {
        ArrayList<String> a = new ArrayList<>();
        // 检查字符串是否为空
        if (!isEmpty(s)) {
            // 使用分隔符分割字符串并添加到列表
            Collections.addAll(a, s.split(flag));
        }
        return a;
    }

    /**
     * 将ArrayList转换为字符串
     * 
     * <p>使用指定的分隔符将列表中的元素连接成一个字符串。</p>
     *
     * @param list 字符串列表
     * @param flag 分隔符
     * @return 连接后的字符串；如果列表为空，返回空字符串
     */
    public static String getStringFromList(ArrayList<String> list, String flag) {
        // 检查列表是否为空
        if (list != null && list.size() > 0) {
            String r = "";
            // 遍历列表元素
            for (String s : list) {
                // 添加分隔符（非第一个元素）
                if (!isEmpty(r)) {
                    r = r + flag;
                }
                r = r + s;
            }
            return r;
        }
        return "";
    }

    /**
     * 在字符串的指定位置插入字符
     * 
     * <p>在字符串的指定索引位置插入一个字符。</p>
     *
     * @param s 原字符串
     * @param i 要插入的位置索引
     * @param c 要插入的字符
     * @return 插入字符后的新字符串
     */
    public static String getStringAddChar(String s, int i, char c) {
        StringBuilder sb = new StringBuilder();
        // 遍历字符串的每个字符
        for (int j = 0; j < s.length(); j++) {
            // 在指定位置插入字符
            if (i == j) {
                sb.append(c);
            }
            sb.append(s.charAt(j));
        }
        return sb.toString().trim();
    }

    /**
     * 获取指定字符串在另一个字符串中出现的次数
     * 
     * <p>统计子字符串在主字符串中出现的次数。</p>
     *
     * @param s 主字符串
     * @param c 要统计的子字符串
     * @return 出现的次数
     */
    public static int getCountFromString(String s, String c) {
        int count = 0;
        // 遍历字符串的每个位置
        for (int i = 0; i < s.length(); i++) {
            // 查找子字符串的位置
            int t = s.indexOf(c, i);
            // 如果找到的位置等于当前位置，计数加1
            if (i == t) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断字符是否是数字字符
     * 
     * <p>检查字符是否在'0'到'9'范围内。</p>
     *
     * @param c 要检查的字符
     * @return true表示是数字字符，false表示不是
     */
    public static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
}
