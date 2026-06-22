package com.micsig.tbook.ui.util;

import com.micsig.base.DoubleUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;


/**
 * 示波器核心工具类
 * 
 * <p>提供示波器应用中时间、频率、单位转换的核心功能。</p>
 * 
 * <p>主要功能模块：</p>
 * <ul>
 *   <li>时间转换：毫秒/秒转换、时间字符串解析、时间单位自动选择</li>
 *   <li>频率转换：Hz/kHz/MHz/GHz转换</li>
 *   <li>单位转换：p/n/μ/m/k/M/G/T等单位转换</li>
 *   <li>数值格式化：保留有效数字、去除末尾零</li>
 *   <li>波特率转换：b/s/kb/s/Mb/s转换</li>
 * </ul>
 * 
 * <p>支持的时间单位：</p>
 * <ul>
 *   <li>fs（飞秒）：10⁻¹⁵秒</li>
 *   <li>ps（皮秒）：10⁻¹²秒</li>
 *   <li>ns（纳秒）：10⁻⁹秒</li>
 *   <li>μs（微秒）：10⁻⁶秒</li>
 *   <li>ms（毫秒）：10⁻³秒</li>
 *   <li>s（秒）：基本单位</li>
 *   <li>ks（千秒）：10³秒</li>
 * </ul>
 * 
 * <p>支持的频率单位：</p>
 * <ul>
 *   <li>Hz（赫兹）：基本单位</li>
 *   <li>kHz（千赫）：10³Hz</li>
 *   <li>MHz（兆赫）：10⁶Hz</li>
 *   <li>GHz（吉赫）：10⁹Hz</li>
 * </ul>
 * 
 * @author yangj
 * @version 1.0
 * @since 2018/1/19
 */
public class TBookUtil {

    /** 一位小数格式化器 */
    private static DecimalFormat df1 = new DecimalFormat("#.0");
    
    /** 三位小数格式化器 */
    private static DecimalFormat df3 = new DecimalFormat("#.000");

    /**
     * 私有构造函数
     * 
     * <p>工具类不允许实例化。</p>
     */
    private TBookUtil() {

    }

    /**
     * 根据时间字符串换算成毫秒
     * 
     * <p>支持的时间格式：</p>
     * <ul>
     *   <li>"100ms" -> 100</li>
     *   <li>"1s" -> 1000</li>
     * </ul>
     *
     * @param time 时间字符串，以ms或s结尾
     * @return 毫秒数；如果格式不匹配返回0
     */
    public static int getMsFromTime(String time) {
        if (time.endsWith("ms")) {
            // 毫秒单位，直接解析
            String tmp = time.replace("ms", "");
            return (int) Double.parseDouble(tmp);
        } else if (time.endsWith("s")) {
            // 秒单位，乘以1000转换为毫秒
            String tmp = time.replace("s", "");
            return (int) (Double.parseDouble(tmp) * 1000);
        }
        return 0;
    }

    /**
     * 根据毫秒数换算成时间字符串
     * 
     * <p>自动选择合适的单位：</p>
     * <ul>
     *   <li>大于等于1000ms：转换为秒（如"1s"）</li>
     *   <li>小于1000ms：保持毫秒（如"500ms"）</li>
     * </ul>
     *
     * @param time 毫秒数
     * @return 时间字符串；如果时间无效返回null
     */
    public static String getTimeFromMs(int time) {
        if (time >= 1000) {
            // 转换为秒
            time = time / 1000;
            return time + "s";
        } else if (time < 1000) {
            // 保持毫秒
            return time + "ms";
        }
        return null;
    }

    /**
     * 根据时间字符串换算成double类型的秒
     * 
     * <p>支持的时间单位：fs、ps、ns、μs/us、ms、ks、s</p>
     *
     * @param time 时间字符串，如"100ms"、"1s"、"500ns"
     * @return 秒为单位的double值；如果字符串为空返回0
     */
    public static double getSFromTime(String time) {
        // 空值检查
        if (StrUtil.isEmpty(time)) {
            return 0;
        }
        
        // 根据单位后缀进行转换
        if (time.endsWith("fs")){
            // 飞秒：10^-15秒
            return Double.parseDouble(time.replace("fs", "")) * 0.001 * 0.001 * 0.001*0.001;
        }else if (time.endsWith("ns")) {
            // 纳秒：10^-9秒
            return Double.parseDouble(time.replace("ns", "")) * 0.001 * 0.001 * 0.001;
        } else if (time.endsWith("μs")) {
            // 微秒（希腊字母μ）：10^-6秒
            return Double.parseDouble(time.replace("μs", "")) * 0.001 * 0.001;
        }
        // 2023-1-30 us to μs（兼容拉丁字母u）
        else if (time.endsWith("us")) {
            return Double.parseDouble(time.replace("us", "")) * 0.001 * 0.001;
        }
        else if (time.endsWith("ms")) {
            // 毫秒：10^-3秒
            return Double.parseDouble(time.replace("ms", "")) * 0.001;
        } else if (time.endsWith("ps")) {
            // 皮秒：10^-12秒
            return Double.parseDouble(time.replace("ps", "")) * 1e-12;
        } else if (time.endsWith("ks")) {
            // 千秒：10^3秒
            return Double.parseDouble(time.replace("ks", "")) * 1000;
        } else if (time.endsWith("s")) {
            // 秒：基本单位
            return Double.parseDouble(time.replace("s", ""));
        }
        return 0;
    }

    /**
     * 根据double类型的秒换算成时间字符串
     * 
     * <p>自动选择合适的单位：ks、s、ms、μs、ns</p>
     * <p>保留一位小数。</p>
     *
     * @param time 秒为单位的double值
     * @return 时间字符串，如"1.5ms"、"2.0s"；如果时间接近0返回"0s"
     */
    public static String getTimeFromS(double time) {
        // 处理负数
        boolean isNegative=false;
        if (time<0){
            isNegative=true;
            time=Math.abs(time);
        }
        
        // 根据数值大小选择合适的单位
        if (time >= 1000) {
            // 千秒级
            String s= getDMinus0(Double.parseDouble(df1.format(time / 1000))) + "ks";
            return isNegative?"-"+s:s;
        } else if (time >= 1) {
            // 秒级
            String s= getDMinus0(Double.parseDouble(df1.format(time))) + "s";
            return isNegative?"-"+s:s;
        } else if (time >= 0.001) {
            // 毫秒级
            String s=getDMinus0(Double.parseDouble(df1.format(time * 1000))) + "ms";
            return isNegative?"-"+s:s;
        } else if (time >= 0.001 * 0.001) {
            // 微秒级
            String s= getDMinus0(Double.parseDouble(df1.format(time * 1000 * 1000))) + "μs";
            return isNegative?"-"+s:s;
        } else if (time >= 0.001 * 0.001 * 0.001) {
            // 纳秒级
            String s= getDMinus0(Double.parseDouble(df1.format(time * 1000 * 1000 * 1000))) + "ns";
            return isNegative?"-"+s:s;
        } else {
            return "0s";
        }
    }

    /**
     * 根据时间字符串转换成以ps为单位的long类型
     * 
     * <p>支持的时间单位：ps、ns、μs/us、ms、ks、s</p>
     *
     * @param time 时间字符串
     * @return 皮秒为单位的long值
     */
    public static long getPsFromTime(String time) {
        if (time.endsWith("ps")) {
            // 皮秒：基本单位
            return (long) (Double.parseDouble(time.replace("ps", "")));
        } else if (time.endsWith("ns")) {
            // 纳秒转皮秒：乘以1000
            return (long) (Double.parseDouble(time.replace("ns", "")) * 1000L);
        } else if (time.endsWith("μs")) {
            // 微秒转皮秒：乘以10^6
            return (long) (Double.parseDouble(time.replace("μs", "")) * 1000L * 1000L);
        }
        // 2023-1-30 us to μs（兼容处理）
        else if (time.endsWith("us")) {
            return (long) (Double.parseDouble(time.replace("us", "")) * 1000L * 1000L);
        }
        else if (time.endsWith("ms")) {
            // 毫秒转皮秒：乘以10^9
            return (long) (Double.parseDouble(time.replace("ms", "")) * 1000L * 1000L * 1000L);
        } else if (time.endsWith("ks")) {
            // 千秒转皮秒：乘以10^15
            return (long) (Double.parseDouble(time.replace("ks", "")) * 1000L * 1000L * 1000L * 1000L * 1000L);
        } else if (time.endsWith("s")) {
            // 秒转皮秒：乘以10^12
            return (long) (Double.parseDouble(time.replace("s", "")) * 1000L * 1000L * 1000L * 1000L);
        }
        return 0;
    }

    /**
     * 根据时间字符串转换成以ns为单位的long类型
     * 
     * <p>支持的时间单位：ps、ns、μs/us、ms、s、ks</p>
     * <p>使用四舍五入处理小数部分。</p>
     *
     * @param time 时间字符串
     * @return 纳秒为单位的long值
     */
    public static long get_nsFromTime(String time) {
        if (time.endsWith("ps")) {
            // 皮秒转纳秒：除以1000（加0.5用于四舍五入）
            return (long) (Double.parseDouble(time.replace("ps", "")) * 0.001 + 0.5);
        } else if (time.endsWith("ns")) {
            // 纳秒：基本单位
            return (long) (Double.parseDouble(time.replace("ns", "")) + 0.5);
        } else if (time.endsWith("μs")) {
            // 微秒转纳秒：乘以1000
            return (long) (Double.parseDouble(time.replace("μs", "")) * 1e3 + 0.5);
        } //2023-1-30 us to μs（兼容处理）
        else if (time.endsWith("us")) {
            return (long) (Double.parseDouble(time.replace("us", "")) * 1e3 + 0.5);
        }
        else if (time.endsWith("ms")) {
            // 毫秒转纳秒：乘以10^6
            return (long) (Double.parseDouble(time.replace("ms", "")) * 1e6 + 0.5);
        } else if (time.endsWith("s")) {
            // 秒转纳秒：乘以10^9
            return (long) (Double.parseDouble(time.replace("s", "")) * 1e9 + 0.5);
        } else if (time.endsWith("ks")) {
            // 千秒转纳秒：乘以10^12
            return (long) (Double.parseDouble(time.replace("ks", "")) * 1e12 + 0.5);
        }
        return 0;
    }

    // ==================== 时间单位常量（以0.1ps为单位） ====================
    
    /** 1皮秒对应的0.1ps单位值 */
    private static final long L_1PS = 10L;
    
    /** 1纳秒对应的0.1ps单位值 */
    private static final long L_1NS = 10L * 1000L;
    
    /** 1微秒对应的0.1ps单位值 */
    private static final long L_1US = 10L * 1000L * 1000L;
    
    /** 1毫秒对应的0.1ps单位值 */
    private static final long L_1MS = 10L * 1000L * 1000L * 1000L;
    
    /** 1秒对应的0.1ps单位值 */
    private static final long L_1S = 10L * 1000L * 1000L * 1000L * 1000L;
    
    /** 1千秒对应的0.1ps单位值 */
    private static final long L_1KS = 10L * 1000L * 1000L * 1000L * 1000L * 1000L;

    /**
     * 从0.1ps为单位的long类型换成保留4位有效数字的时间字符串
     * 
     * <p>自动选择合适的时间单位：fs、ps、ns、μs、ms、s、ks</p>
     * <p>保留最多4位有效数字。</p>
     *
     * @param fs100 0.1ps为单位的时间数字
     * @return 格式化的时间字符串，如"1.234 ns"、"500.0 ps"
     */
    public static String getSFrom100Fs(long fs100) {
        if (fs100 == 0) {
            return "0 ps";
        } else if (fs100 < 0) {
            // 处理负数
            return "-" + getSFrom100Fs(fs100 * -1);
        } else if (fs100 < L_1PS) {
            // 飞秒级
            return fs100 + "00 fs";
        } else if (fs100 < L_1NS) {
            // 皮秒级
            String letter = fs100 % 10 == 0 ? "" : ("." + fs100 % 10);
            return fs100 / 10 + letter + " ps";
        } else if (fs100 < L_1NS * 1000) {
            // 纳秒级
            return func(fs100, L_1NS, " ns");
        } else if (fs100 < L_1US * 1000L) {
            // 微秒级
            return func(fs100, L_1US, " μs");
        } else if (fs100 < L_1MS * 1000L) {
            // 毫秒级
            return func(fs100, L_1MS, " ms");
        } else if (fs100 < L_1S * 1000L) {
            // 秒级
            return func(fs100, L_1S, " s");
        } else {
            // 千秒级
            return func(fs100, L_1KS, " ks");
        }
    }

    /**
     * 根据long类型的数，获得浮点类型的保留4位有效数字的数
     * 
     * <p>内部辅助方法，用于格式化时间值。</p>
     *
     * @param fs100 原始数据（0.1ps为单位）
     * @param lUnit 整数小数的边界（单位换算因子）
     * @param sUnit 结果显示的单位
     * @return 格式化后的时间字符串
     */
    private static String func(long fs100, long lUnit, String sUnit) {
        if (fs100 < lUnit * 10) {
            // 处理1.x到9.x范围的数值
            long letter = fs100 % lUnit; // 完整的小数部分的数
            long line = lUnit / (10000 / 10); // 舍弃的小数位的边界
            letter = letter / line + (letter % line >= (line / 2) ? 1 : 0); // 保留三位之后的小数部分的数
            String sLetter;
            if (letter == 0) {
                sLetter = "";
            } else if (letter < 10) {
                sLetter = ".00" + letter;
            } else if (letter < 100) {
                sLetter = ".0" + (letter % 10 == 0 ? letter / 10 : letter);
            } else if (letter < 1000) {
                sLetter = "." + (letter % 100 == 0 ? letter / 100 : letter % 10 == 0 ? letter / 10 : letter);
            } else {
                fs100 += lUnit;
                sLetter = "";
            }
            return fs100 / lUnit + sLetter + sUnit;
        } else if (fs100 < lUnit * 100) {
            // 处理10.x到99.x范围的数值
            long letter = fs100 % lUnit; // 完整的小数部分的数
            long line = lUnit / (10000 / 100); // 舍弃的小数位的边界
            letter = letter / line + (letter % line >= (line / 2) ? 1 : 0); // 保留两位之后的小数部分的数
            String sLetter;
            if (letter == 0) {
                sLetter = "";
            } else if (letter < 10) {
                sLetter = ".0" + letter;
            } else if (letter < 100) {
                sLetter = "." + (letter % 10 == 0 ? letter / 10 : letter);
            } else {
                fs100 += lUnit;
                sLetter = "";
            }
            return fs100 / lUnit + sLetter + sUnit;
        } else if (fs100 < lUnit * 1000) {
            // 处理100.x到999.x范围的数值
            long letter = fs100 % lUnit; // 完整的小数部分的数
            long line = lUnit / (10000 / 1000); // 舍弃的小数位的边界
            letter = letter / line + (letter % line >= (line / 2) ? 1 : 0); // 保留一位之后的小数部分的数
            String sLetter;
            if (letter == 0) {
                sLetter = "";
            } else if (letter < 10) {
                sLetter = "." + letter;
            } else {
                fs100 += lUnit;
                sLetter = "";
            }
            return fs100 / lUnit + sLetter + sUnit;
        } else {
            return "";
        }
    }

    /**
     * 获得四位有效数字的表示方法
     * 
     * <p>根据double类型的标准值，自动选择合适的单位前缀。</p>
     * <p>支持的单位前缀：f、p、n、μ、m、（无）、k、M、G、T、P</p>
     *
     * @param d 标准值
     * @return 格式化的字符串，如"1.234 k"、"500.0 m"
     */
    public static String getFourFromD(double d) {
        if (d < 0) {
            // 处理负数
            return "-" + getFourFromD(d * -1);
        } else if (d == 0) {
            return "0 ";
        } else if (d < 1) {
            // 小于1的数值，使用小单位前缀
            double l = d * 1e13;
            if (l < 10) {
                // 0.1ps级
                return (l * 100) + " f";
            } else if (l < 10 * 100) {
                // 1p级、10p级
                return l / 10 + "." + l % 10 + " p";
            } else if (l < 10 * 1000) {
                // 100p级
                return l / 10 + "." + l % 10 + " p";
            } else if (l < 10 * 1000 * 10) {
                // 1n级
                return getDFourFromD(((l / 100) * 0.01)) + " n";
            } else if (l < 10 * 1000 * 100) {
                // 10n级
                return getDFourFromD(((l / 1000) * 0.1)) + " n";
            } else if (l < 10 * 1000 * 1000) {
                // 100n级
                String d4FromD = getDFourFromD((l / 1000 / 10));
                if ("1000".equals(d4FromD)) {
                    return "1 μ";
                }
                return d4FromD + " n";
            } else if (l < 10 * 1000 * 1000 * 10) {
                // 1u级
                return getDFourFromD(((l / 1000 / 100) * 0.01)) + " μ";
            } else if (l < 10 * 1000 * 1000 * 100) {
                // 10u级
                return getDFourFromD(((l / 1000 / 1000) * 0.1)) + " μ";
            } else if (l < 10L * 1000L * 1000L * 1000L) {
                // 100u级
                String d4FromD = getDFourFromD((l / 1000 / 1000 / 10));
                if ("1000".equals(d4FromD)) {
                    return "1 m";
                }
                return d4FromD + " μ";
            } else if (l < 10L * 1000L * 1000L * 1000L * 10L) {
                // 1m级
                return getDFourFromD(((l / 1000 / 1000 / 100) * 0.01)) + " m";
            } else if (l < 10L * 1000L * 1000L * 1000L * 100L) {
                // 10m级
                return getDFourFromD(((l / 1000 / 1000 / 1000) * 0.1)) + " m";
            } else {
                // 100m级
                String d4FromD = getDFourFromD((l / 1000 / 1000 / 1000 / 10));
                if ("1000".equals(getDFourFromD((l / 1000 / 1000 / 1000 / 10)))) {
                    return "1 ";
                }
                return d4FromD + " m";
            }
        } else {
            // 大于等于1的数值，使用大单位前缀
            if (d < 1000) {
                String d4FromD = getDFourFromD(d);
                if ("1000".equals(d4FromD)) {
                    return "1 k";
                }
                return d4FromD+" ";
            } else if (d < 1000 * 1000) {
                String d4FromD = getDFourFromD(d / 1000);
                if ("1000".equals(d4FromD)) {
                    return "1 M";
                }
                return d4FromD + " k";
            } else if (d < 1000 * 1000 * 1000) {
                String d4FromD = getDFourFromD(d / 1000 / 1000);
                if ("1000".equals(d4FromD)) {
                    return "1 G";
                }
                return d4FromD + " M";
            } else if (d < 1000L * 1000L * 1000L * 1000L) {
                String d4FromD = getDFourFromD(d / 1000 / 1000 / 1000);
                if ("1000".equals(d4FromD)) {
                    return "1 T";
                }
                return d4FromD + " G";
            } else if (d < 1000L * 1000L * 1000L * 1000L * 1000L) {
                String d4FromD = getDFourFromD(d / 1000 / 1000 / 1000 / 1000);
                if ("1000".equals(d4FromD)) {
                    return "1 P";
                }
                return d4FromD + " T";
            } else {
                return getDFourFromD(d / 1000 / 1000 / 1000 / 1000 / 1000) + " P";
            }
        }
    }

    /** 大单位前缀数组：无、k、M、G、T、P */
    public final static String[] unit1 = {"", "k", "M", "G", "T", "P"};

    /**
     * 获得四位有效数字的表示方法（大单位版本）
     * 
     * <p>递归处理大于等于1的数值，自动选择k、M、G、T、P等单位。</p>
     *
     * @param d       数值
     * @param place   当前单位索引
     * @param isRound 是否进行四舍五入
     * @return 格式化的字符串
     */
    private static String getFourFromD_1(double d, int place, boolean isRound) {
        String str1 = "";
        if (d >= 1000) {
            // 数值大于等于1000，升级单位
            if (place < unit1.length - 1) {
                place++;
                d /= 1000;
                return getFourFromD_1(d, place, isRound);
            } else {
                // 已达最大单位，直接取整数部分
                str1 = String.valueOf(d);
                int ix = str1.indexOf(".");
                if (ix > 0)
                    str1 = str1.substring(0, ix);
            }
        } else {
            String str2 = String.valueOf(d);
            int ix = str2.indexOf(".");
            if (ix < 0) {
                // 没有小数点，补齐小数位
                str1 = str2;
                if (str1.length() == 1) str1 = str1 + ".000";
                else if (str1.length() == 2) str1 = str1 + ".00";
                else if (str1.length() == 3) str1 = str1 + ".0";
            } else {
                if (isRound) {
                    // 四舍五入处理
                    switch (ix) {
                        default:
                        case 1:
                            d += 0.0005;
                            break;
                        case 2:
                            d += 0.005;
                            break;
                        case 3:
                            d += 0.05;
                            break;
                    }
                    return getFourFromD_1(d, place, false);
                } else {
                    if (str2.length() >= 5)
                        str1 = str2.substring(0, 4 + 1);
                    else{
                        str1 = str2;
                    }
                    if (str1.length() == 3) str1 = str1 + "00";
                    if (str1.length() == 4) str1 = str1 + "0";
                }
            }
        }

        return str1 +" "+ unit1[place];
    }

    /** 小单位前缀数组：无、m、μ、n、p、f */
    public final static String[] unit2 = {"", "m", "μ", "n", "p", "f"};

    /**
     * 获得四位有效数字的表示方法（小单位版本）
     * 
     * <p>递归处理小于1的数值，自动选择m、μ、n、p、f等单位。</p>
     *
     * @param d       数值
     * @param place   当前单位索引
     * @param isRound 是否进行四舍五入
     * @return 格式化的字符串
     */
    private static String getFourFromD_2(double d, int place, boolean isRound) {
        String str1 = "";
        if (d < 1) {
            // 数值小于1，降级单位
            if (place < unit2.length - 1) {
                place++;
                d *= 1000;
                return getFourFromD_2(d, place, isRound);
            } else {
                // 已达最小单位
                str1 = String.valueOf(d);
                int ix = str1.indexOf(".");
                if (ix > 0) {
                    if (str1.length() > 4)
                        str1 = str1.substring(0, 3 + 1);
                    str1 = getDMinus0(str1);
                    if (str1.equals("0"))
                        place = 0;
                } else {
                    str1 = "0";
                    place = 0;
                }
            }
        } else if (d >= 1000) {
            // 数值大于等于1000，升级单位
            place--;
            d /= 1000;
            return getFourFromD_2(d, place, isRound);
        } else {
            String str2 = String.valueOf(d);
            int ix = str2.indexOf(".");
            if (ix < 0) {
                // 没有小数点，补齐小数位
                str1 = str2;
                if (str1.length() == 1) str1 = str1 + ".000";
                else if (str1.length() == 2) str1 = str1 + ".00";
                else if (str1.length() == 3) str1 = str1 + ".0";
            } else {
                if (isRound) {
                    // 四舍五入处理
                    switch (ix) {
                        default:
                        case 1:
                            d += 0.0005;
                            break;
                        case 2:
                            d += 0.005;
                            break;
                        case 3:
                            d += 0.05;
                            break;
                    }
                    return getFourFromD_2(d, place, false);
                } else {
                    if (str2.length() >= 5)
                        str1 = str2.substring(0, 4 + 1);
                    else{
                        str1 = str2;
                    }
                    if (str1.length() == 3) str1 = str1 + "00";
                    if (str1.length() == 4) str1 = str1 + "0";
                }
            }
        }

        return str1 +" "+ unit2[place];
    }

    /**
     * 获得四位有效数字的表示方法（仅使用大单位）
     * 
     * <p>不使用小单位前缀（m、μ、n、p、f），只使用大单位前缀（k、M、G、T、P）。</p>
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getFourFromD_NoSmallUnit(double d) {
        if (d < 0)
            return "-" + getFourFromD_(-d);
        else
            return getFourFromD_1(d, 0, true);
    }

    /**
     * 获得保留4位数字的double类型数据
     * 
     * <p>自动选择合适的单位前缀。</p>
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getFourFromD_(double d) {
        if (d < 0)
            return "-" + getFourFromD_(-d);
        if (d < 1)
            return getFourFromD_2(d, 0, true);
        else
            return getFourFromD_1(d, 0, true);
    }

    /**
     * 获得保留4位数字的double类型数据，去除末尾的0
     * 
     * <p>如果小数点后以0结尾，则简化之。</p>
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getFourFromD_Trim0(double d) {
        String s = getFourFromD_(d);
        String unit = "";
        
        // 提取单位前缀
        for (int i = 0; i < unit1.length; i++) {
            if (!StrUtil.isEmpty(unit1[i]) && s.endsWith(unit1[i])) {
                unit = unit1[i];
                s = s.substring(0, s.length() - 1);
                break;
            }
        }
        if (StrUtil.isEmpty(unit)) {
            for (int i = 0; i < unit2.length; i++) {
                if (!StrUtil.isEmpty(unit2[i]) && s.endsWith(unit2[i])) {
                    unit = unit2[i];
                    s = s.substring(0, s.length() - 1);
                    break;
                }
            }
        }
        
        // 去除末尾的0
        s=s.replace(" ","");
        if (s.contains(".")) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s +" "+ unit;
    }

    /**
     * 精确到两位小数，即使小数位是00也要显示
     * 
     * <p>不进行单位扩展，用于百分比的显示。</p>
     *
     * @param d 数值
     * @return 格式化的字符串，如"12.34 "
     */
    public static String getPoint2FromD_noscale(double d) {
        if (d < 0)
            return "-" + getPoint2FromD_noscale(-d);
        else {
            d += 0.005; // 四舍五入
            String str = String.valueOf(d);
            int idx = str.indexOf(".");
            if (idx < 0)
                return str+" ";
            else if (idx + 3 >= str.length())
                return str+" ";
            else
                return str.substring(0, idx + 3)+" ";
        }
    }

    /**
     * 获得三位有效数字的表示方法（大单位版本）
     * 
     * <p>内部辅助方法。</p>
     */
    private static String getThreeFromD_1(double d, int place, boolean isrund) {
        String str1 = "";
        if (d >= 1000) {
            if (place < unit1.length - 1) {
                place++;
                d /= 1000;
                return getThreeFromD_1(d, place, isrund);
            } else {
                str1 = String.valueOf(d);
                int ix = str1.indexOf(".");
                if (ix > 0)
                    str1 = str1.substring(0, ix);
            }
        } else {
            String str2 = String.valueOf(d);
            int ix = str2.indexOf(".");
            if (ix < 0) {
                str1 = str2;
            } else {
                if (isrund) {
                    switch (ix) {
                        default:
                        case 1:
                            d += 0.005;
                            break;
                        case 2:
                            d += 0.05;
                            break;
                        case 3:
                            d += 0.5;
                            break;
                    }
                    return getThreeFromD_1(d, place, false);
                } else {
                    if (str2.length() >= 4)
                        str1 = str2.substring(0, 3 + 1);
                    else{
                        str1 = str2;
                    }
                    str1 = getDMinus0(str1);
                }
            }
        }

        return str1 + unit1[place];
    }

    /**
     * 获得三位有效数字的表示方法（小单位版本）
     * 
     * <p>内部辅助方法。</p>
     */
    private static String getThreeFromD_2(double d, int place, boolean isrund) {
        String str1 = "";
        if (d < 1) {
            if (place < unit2.length - 1) {
                place++;
                d *= 1000;
                return getThreeFromD_2(d, place, isrund);
            } else {
                str1 = String.valueOf(d);
                int ix = str1.indexOf(".");
                if (ix > 0) {
                    if (str1.length() > 4)
                        str1 = str1.substring(0, 3 + 1);
                    str1 = getDMinus0(str1);
                    if (str1.equals("0"))
                        place = 0;
                } else {
                    str1 = "0";
                    place = 0;
                }
            }
        } else if (d >= 1000) {
            place--;
            d /= 1000;
            return getThreeFromD_2(d, place, isrund);
        } else {
            String str2 = String.valueOf(d);
            int ix = str2.indexOf(".");
            if (ix < 0) {
                str1 = str2;
            } else {
                if (isrund) {
                    switch (ix) {
                        default:
                        case 1:
                            d += 0.005;
                            break;
                        case 2:
                            d += 0.05;
                            break;
                        case 3:
                            d += 0.5;
                            break;
                    }
                    return getThreeFromD_2(d, place, false);
                } else {
                    if (str2.length() >= 4)
                        str1 = str2.substring(0, 3 + 1);
                    else{
                        str1 = str2;
                    }
                    str1 = getDMinus0(str1);
                }
            }
        }

        return str1 + unit2[place];
    }

    /**
     * 获得三位有效数字的表示方法
     * 
     * <p>自动选择合适的单位前缀。</p>
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getThreeFromD_(double d) {
        if (d < 0)
            return "-" + getThreeFromD_(-d);
        if (d < 1)
            return getThreeFromD_2(d, 0, true);
        else
            return getThreeFromD_1(d, 0, true);
    }

    /**
     * 根据long类型的单位是0.1ps的时间换算成保留三位有效数字的时间字符串
     * 
     * <p>自动选择合适的时间单位：fs、ps、ns、μs、ms、s</p>
     *
     * @param ps 0.1ps为单位的时间值
     * @return 格式化的时间字符串
     */
    public static String getTime3FromPs(long ps) {
        if (ps < 0) {
            return "-" + getTime3FromPs(ps * -1);
        } else if (ps == 0) {
            return "0ps";
        } else if (ps < 10) {
            // 0.1ps级，转换为飞秒
            return (ps * 100) + "fs";
        } else if (ps < 10 * 100) {
            // 1ps级、10ps级
            return getD3FromD(ps * 0.1) + "ps";
        } else if (ps < 10 * 1000) {
            // 100ps级
            return getD3FromD(ps / 10) + "ps";
        } else if (ps < 10 * 1000 * 10) {
            // 1ns级
            return getD3FromD(((ps / 100) * 0.01)) + "ns";
        } else if (ps < 10 * 1000 * 100) {
            // 10ns级
            return getD3FromD(((ps / 1000) * 0.1)) + "ns";
        } else if (ps < 10 * 1000 * 1000) {
            // 100ns级
            return getD3FromD((ps / 1000 / 10)) + "ns";
        } else if (ps < 10 * 1000 * 1000 * 10) {
            // 1μs级
            return getD3FromD(((ps / 1000 / 100) * 0.01)) + "μs";
        } else if (ps < 10 * 1000 * 1000 * 100) {
            // 10μs级
            return getD3FromD(((ps / 1000 / 1000) * 0.1)) + "μs";
        } else if (ps < 10L * 1000L * 1000L * 1000L) {
            // 100μs级
            return getD3FromD((ps / 1000 / 1000 / 10)) + "μs";
        } else if (ps < 10L * 1000L * 1000L * 1000L * 10L) {
            // 1ms级
            return getD3FromD(((ps / 1000 / 1000 / 100) * 0.01)) + "ms";
        } else if (ps < 10L * 1000L * 1000L * 1000L * 100L) {
            // 10ms级
            return getD3FromD(((ps / 1000 / 1000 / 1000) * 0.1)) + "ms";
        } else if (ps < 10L * 1000L * 1000L * 1000L * 1000L) {
            // 100ms级
            return getD3FromD((ps / 1000 / 1000 / 1000 / 10)) + "ms";
        } else if (ps < 10L * 1000L * 1000L * 1000L * 1000L * 10L) {
            // s级
            return getD3FromD(((ps / 1000 / 1000 / 1000 / 100) * 0.01)) + "s";
        } else {
            // 10s级
            return getD3FromD(((ps / 1000 / 1000 / 1000 / 1000) * 0.1)) + "s";
        }
    }

    /**
     * long类型的ns转换成ms形式的字符串
     * 
     * <p>格式：35.2365ms</p>
     *
     * @param ns 纳秒值
     * @return 格式化的毫秒字符串
     */
    public static String getMsFromNs(long ns) {
        String string = String.valueOf(ns);
        // 补齐到10位
        while (string.length() < 10) {
            string = "0" + string;
        }
        Long a = Long.valueOf(string.substring(0, 4));
        Long b = Long.valueOf(string.substring(4, 8));
        String value = Long.valueOf(string.substring(0, 4)) + "." + string.substring(4, 8);
        return value + "ms";
    }

    /**
     * 保留4位有效数字
     * 
     * <p>内部辅助方法，用于格式化数值。</p>
     *
     * @param d 数值（应为10000以内）
     * @return 格式化的字符串
     */
    private static String getDFourFromD(double d) {
        if (d >= 10000 || d < 1) {
            return String.valueOf(getDMinus0((int) d));
        }
        String s = String.valueOf(d);
        if (!s.contains(".") || s.length() <= 5) {
            return getDMinus0(Double.parseDouble(s));
        }
        boolean b = Integer.parseInt(s.substring(5, 6)) >= 5;
        s = s.substring(0, 5);
        int pointIndex = s.indexOf(".");
        int integer = Integer.valueOf(s.replace(".", ""));
        if (b) integer++;
        return getDMinus0(StrUtil.getStringAddChar(String.valueOf(integer), String.valueOf(integer).length() - (4 - pointIndex), '.'));
    }

    /**
     * 把double类型的数据保留三位有效数字
     *
     * @param d 数据为1000以内
     * @return 格式化的字符串
     */
    public static String getD3FromD(double d) {
        if (d >= 1000) return String.valueOf((int) d);
        String s = String.valueOf(d);
        if (s.length() > 3) {
            s = s.substring(0, 4);
        }
        s = getDMinus0(Double.parseDouble(s));
        if (s.contains(".") || s.length() <= 3) {
            return s;
        } else {
            return s.substring(0, 3);
        }
    }

    /**
     * 把double类型的数据保留三位有效数字（包括正负值）
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getD3FromD_zf(double d){
        String s= getMFromDouble(Math.abs(d));
        if (d < 0) s = "-" + s;
        return s;
    }

    /**
     * 对三位有效数字进行四舍五入
     *
     * @param value 数值字符串
     * @return 格式化的字符串
     */
    public static String getD3Round(String value){
        double d=Double.parseDouble(value);
        if (d >= 1000) return String.valueOf((int) d);
        else if (d>=100){
            DecimalFormat df=new DecimalFormat("###");
            return (df.format(d));
        }else if (d>=10){
            DecimalFormat df=new DecimalFormat("##.#");
            return (df.format(d));
        }else {
            DecimalFormat df=new DecimalFormat("#.##");
            return (df.format(d));
        }
    }

    /**
     * 把double类型的数据保留四位有效数字
     *
     * @param d 数据为10000以内
     * @return 格式化的字符串
     */
    public static String getD4FromD(double d) {
        if (d >= 10000) return String.valueOf((int) d);
        String s = String.valueOf(d);
        if (s.length() > 4) {
            s = s.substring(0, 5);
        }
        s = getDMinus0(Double.parseDouble(s));
        if (s.contains(".") || s.length() <= 4) {
            return s;
        } else {
            return s.substring(0, 4);
        }
    }

    /**
     * 把double类型的数据保留六位有效数字
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getD6FromD(double d) {
        String s = String.valueOf(d);
        if (s.contains(".")) {
            if (s.length() > 7) {
                s = s.substring(0, 7);
            } else {
                while (s.length() < 7) {
                    s = s + "0";
                }
            }
        } else {
            if (s.length() > 6) {
                s = s.substring(0, 6);
            } else {
                s = s + ".";
                while (s.length() < 7) {
                    s = s + "0";
                }
            }
        }
        return s;
    }

    /**
     * 去掉double数据小数点后末尾的0
     *
     * @param d 数值
     * @return 去除末尾0后的字符串
     */
    public static String getDMinus0(double d) {
        return getDMinus0(String.valueOf(d));
    }

    /**
     * 去掉字符串表示的double数据小数点后末尾的0
     *
     * @param s 数值字符串
     * @return 去除末尾0后的字符串
     */
    public static String getDMinus0(String s) {
        if (!s.contains(".")) return s;
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) s = s.replace(".", "");
        return s;
    }

    /**
     * 根据mX、X、kX转换成X为单位的double类型数字
     *
     * @param s 带单位的字符串，如"1mX"、"1X"、"1kX"
     * @return X为单位的double值
     */
    public static double getDoubleFromX(String s) {
        if(s != null) {
            if (s.endsWith("mX")) {
                return Double.parseDouble(s.replace("mX", "")) * 0.001;
            } else if (s.endsWith("kX")) {
                return Double.parseDouble(s.replace("kX", "")) * 1000;
            } else if (s.endsWith("X")) {
                return Double.parseDouble(s.replace("X", ""));
            }
        }
        return 0;
    }

    /**
     * 根据double类型的数字转换成mX、X、kX的单位的字符串
     *
     * @param x X为单位的数值
     * @return 带单位的字符串
     */
    public static String getXFromDouble(double x) {
        String unit = "";
        if (x < 1) {
            x = x * 1000;
            unit = "mX";
        } else if (x >= 1000) {
            x = x / 1000;
            unit = "kX";
        } else {
            unit = "X";
        }
        x += 0.001;
        String s = String.valueOf(x);
        if (s.length() >= 3) {
            String substring = s.substring(0, 3);
            if (substring.contains(".") && s.length() >= 4) {
                substring = s.substring(0, 4);
            }
            if (substring.contains(".")) {
                while (substring.endsWith("0")) {
                    substring = substring.substring(0, substring.length() - 1);
                }
                if (substring.endsWith(".")) {
                    substring = substring.substring(0, substring.length() - 1);
                }
            }
            s = substring;
        }
        return s + unit;
    }

    // ==================== 频率单位常量 ====================
    
    /** 吉赫单位字符串 */
    public static final String UNIT_GHZ = "GHz";
    
    /** 兆赫单位字符串 */
    public static final String UNIT_MHZ = "MHz";
    
    /** 千赫单位字符串 */
    public static final String UNIT_KHZ = "kHz";
    
    /** 赫兹单位字符串 */
    public static final String UNIT_HZ = "Hz";

    /**
     * 根据GHz、MHz、kHz、Hz转换成以MHz为单位的double类数据
     *
     * @param hz 频率字符串，如"1GHz"、"100MHz"、"1kHz"、"1000Hz"
     * @return MHz为单位的double值
     */
    public static double getMHzFromHz(String hz) {
        if (hz.endsWith(UNIT_GHZ)) {
            return Double.parseDouble(hz.replace(UNIT_GHZ, "")) * 1000;
        } else if (hz.endsWith(UNIT_MHZ)) {
            return Double.parseDouble(hz.replace(UNIT_MHZ, ""));
        } else if (hz.endsWith(UNIT_KHZ)) {
            return Double.parseDouble(hz.replace(UNIT_KHZ, "")) * 0.001;
        } else if (hz.endsWith(UNIT_HZ)) {
            return Double.parseDouble(hz.replace(UNIT_HZ, "")) * 1e-6;
        }
        return 0;
    }

    /**
     * 根据double类的MHz数据，转换成以MHz或kHz结尾的字符串
     *
     * @param hz MHz为单位的频率值
     * @return 频率字符串，如"1.000MHz"、"500kHz"
     */
    public static String getHzFromMHz(double hz) {
        if (hz > 1) {
            String tmp = df3.format(hz);
            while (tmp.endsWith("0")) {
                tmp = tmp.substring(0, tmp.length() - 2);
            }
            if (tmp.endsWith(".")) {
                tmp = tmp.substring(0, tmp.length() - 2);
            }
            return tmp + UNIT_MHZ;
        } else {
            int tmp = (int) (hz * 1000);
            return String.valueOf(tmp) + UNIT_KHZ;
        }
    }

    /**
     * 根据double类型的Hz数据，转换成以GHz、MHz、kHz、Hz结尾的字符串
     * 
     * <p>保留6位有效数字。</p>
     *
     * @param hz Hz为单位的频率值
     * @return 频率字符串
     */
    public static String getHzFromHz(double hz) {
        String unit;

        if (hz >= 1000 * 1000 * 1000) {
            hz = hz / (1000 * 1000 * 1000);
            unit = UNIT_GHZ;
        } else if (hz >= 1000 * 1000) {
            hz = hz / (1000 * 1000);
            unit = UNIT_MHZ;
        } else if (hz >= 1000) {
            hz = hz / 1000;
            unit = UNIT_KHZ;
        } else {
            unit = UNIT_HZ;
        }
        return getD6FromD(hz) +" "+ unit;
    }

    /**
     * 根据double类型的Hz转换成保留3位有效数字的频率字符串
     *
     * @param hz Hz为单位的频率值
     * @return 频率字符串
     */
    public static String getHz3FromHz(double hz) {
        String unit;
        if (hz >= 1000 * 1000 * 1000) {
            hz = hz / (1000 * 1000 * 1000);
            unit = UNIT_GHZ;
        } else if (hz >= 1000 * 1000) {
            hz = hz / (1000 * 1000);
            unit = UNIT_MHZ;
        } else if (hz >= 1000) {
            hz = hz / 1000;
            unit = UNIT_KHZ;
        } else {
            unit = UNIT_HZ;
        }
        return getD3FromD(hz) + unit;
    }

    /**
     * 根据频率字符串转换成Hz为单位的int值
     *
     * @param hz 频率字符串，如"1GHz"、"100MHz"
     * @return Hz为单位的int值
     */
    public static int getHzFromHz3(String hz) {
        double d;
        if (hz.endsWith(UNIT_GHZ)) {
            d = Double.valueOf(hz.replace(UNIT_GHZ, ""));
            d *= 1000 * 1000 * 1000;
        } else if (hz.endsWith(UNIT_MHZ)) {
            d = Double.valueOf(hz.replace(UNIT_MHZ, ""));
            d *= 1000 * 1000;
        } else if (hz.endsWith(UNIT_KHZ)) {
            d = Double.valueOf(hz.replace(UNIT_KHZ, ""));
            d *= 1000;
        } else {
            d = Double.valueOf(hz.replace(UNIT_HZ, ""));
        }
        return (int) d;
    }

    /**
     * 把Mb/s、kb/s、b/s类型的波特率转换成int类型的波特率
     *
     * @param bs 波特率字符串
     * @return b/s为单位的int值
     */
    public static int getIntFromBaudRate(String bs) {
        if (bs.endsWith("Mb/s")) {
            return (int) (Double.parseDouble(bs.replace("Mb/s", "")) * 1000 * 1000);
        } else if (bs.endsWith("kb/s")) {
            return (int) (Double.parseDouble(bs.replace("kb/s", "")) * 1000);
        } else if (bs.endsWith("b/s")) {
            return (int) (Double.parseDouble(bs.replace("b/s", "")));
        }
        return 0;
    }

    /**
     * 把int类型的波特率转换成Mb/s、kb/s、b/s类型的字符串
     *
     * @param bs b/s为单位的波特率
     * @return 波特率字符串
     */
    public static String getBaudRateFromInt(int bs) {
        String baudRate = "";
        if (bs >= 1000 * 1000) {
            int k = bs / 1000 % 1000;
            baudRate = bs / (1000 * 1000) + (k == 0 ? "" : ("." + (k < 10 ? ("00" + k) : k < 100 ? ("0" + k) : k)));
            baudRate = getDMinus0(baudRate) + "Mb/s";
        } else if (bs >= 1000) {
            int k = bs % 1000;
            baudRate = bs / 1000 + (k == 0 ? "" : ("." + (k < 10 ? ("00" + k) : k < 100 ? ("0" + k) : k)));
            baudRate = getDMinus0(baudRate) + "kb/s";
        } else if (bs >= 1) {
            baudRate = getDMinus0(bs) + "b/s";
        }
        return baudRate;
    }

    /**
     * 把带单位前缀的字符串转换成double类型（高精度版本）
     * 
     * <p>支持的单位：p、n、μ/u、m、（无）、k、M、G、T</p>
     * <p>使用BigDecimal进行精确计算。</p>
     *
     * @param s 带单位的字符串，如"1k"、"1M"、"1μ"
     * @return double值
     */
    public static double getBigDoubleFromM(String s) {
        if (s==null || s.isEmpty()){
            return 0;
        }else if (s.endsWith("p")) {
            s = s.replace("p", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E-12")).doubleValue();
        } else if (s.endsWith("n")) {
            s = s.replace("n", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E-9")).doubleValue();
        }
        else if (s.endsWith("μ")) {
            s = s.replace("μ", "").replace(" ", "");
            return new BigDecimal(s).multiply(new BigDecimal("1E-6")).doubleValue();
        }
        else if (s.endsWith("u")) {
            s = s.replace("u", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E-6")).doubleValue();
        } else if (s.endsWith("m")) {
            s = s.replace("m", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E-3")).doubleValue();
        } else if (s.endsWith("k")) {
            s = s.replace("k", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E3")).doubleValue();
        } else if (s.endsWith("M")) {
            s = s.replace("M", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E6")).doubleValue();
        } else if (s.endsWith("G")) {
            s = s.replace("G", "").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E9")).doubleValue();
        }else if (s.endsWith("T")){
            s=s.replace("T","").replace(" ","");
            return new BigDecimal(s).multiply(new BigDecimal("1E12")).doubleValue();
        }
        else {
            s = s.replace(" ","");
            if(!s.isEmpty()) {
                return new BigDecimal(s).doubleValue();
            }else{
                try{
                    throw  new IllegalArgumentException();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return 0;
        }
    }

    /**
     * 把带单位前缀的字符串转换成double类型
     * 
     * <p>支持的单位：f、p、n、μ/u、m、（无）、k、M、G、T</p>
     *
     * @param s 带单位的字符串
     * @return double值
     */
    public static double getDoubleFromM(String s) {
        if (s==null || s.isEmpty()){
            return 0;
        } else if (s.endsWith("f")) {
            s = s.replace("f", "").replace(" ", "");
            return Double.valueOf(s) * 0.001 * 0.001 * 0.001 * 0.001 * 0.001;
        } else if (s.endsWith("p")) {
            s = s.replace("p", "").replace(" ","");
            return Double.valueOf(s) * 0.001 * 0.001 * 0.001 * 0.001;
        } else if (s.endsWith("n")) {
            s = s.replace("n", "").replace(" ","");
            return Double.valueOf(s) * 0.001 * 0.001 * 0.001;
        }
        else if (s.endsWith("μ")) {
            s = s.replace("μ", "").replace(" ", "");
            return Double.valueOf(s) * 0.001 * 0.001;
        }
        else if (s.endsWith("u")) {
            s = s.replace("u", "").replace(" ","");
            return Double.valueOf(s) * 0.001 * 0.001;
        } else if (s.endsWith("m")) {
            s = s.replace("m", "").replace(" ","");
            return Double.valueOf(s) * 0.001;
        } else if (s.endsWith("k")) {
            s = s.replace("k", "").replace(" ","");
            return Double.valueOf(s) * 1000;
        } else if (s.endsWith("M")) {
            s = s.replace("M", "").replace(" ","");
            return Double.valueOf(s) * 1000_000d;
        } else if (s.endsWith("G")) {
            s = s.replace("G", "").replace(" ", "");
            return Double.valueOf(s) * 1000_000_000d;
        } else if (s.endsWith("T")) {
            s = s.replace("T", "").replace(" ", "");
            return Double.valueOf(s) * 1000_000_000_000d;
        }
        else {
            s = s.replace(" ","");
            if(!s.isEmpty()) {
                return Double.valueOf(s);
            }else{
                try{
                    throw  new IllegalArgumentException();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return 0;
        }
    }

    /**
     * 把double类型转换成带单位前缀的字符串
     * 
     * <p>自动选择合适的单位：f、p、n、μ、m、（无）、k、M、G、T</p>
     * <p>保留三位有效数字。</p>
     *
     * @param d 数值
     * @return 带单位的字符串
     */
    public static String getMFromDouble(double d) {
        if (d >= 1000 * 1000 * 1000 * 1000L) {
            d = d / (1000 * 1000 * 1000 * 1000L);
            return getDMinus0(getD3FromD(d + 0.0001)) + " T";
        } else if (d >= 1000 * 1000 * 1000) {
            d = d / (1000 * 1000 * 1000);
            return getDMinus0(getD3FromD(d + 0.0001)) + " G";
        } else if (d >= 1000 * 1000) {
            d = d / (1000 * 1000);
            return getDMinus0(getD3FromD(d + 0.0001)) + " M";
        } else if (d >= 1000) {
            d = d / 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " k";
        } else if (d >= 1) {
            return getDMinus0(getD3FromD(d + 0.0001))+" ";
        }else if (d==0){
            return "0 ";
        }
        else if (d + 5e-6 >= 1e-3) {
            d = d * 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " m";
        } else if (d + 5e-9 >= 1e-6) {
            d = d * 1000 * 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " μ";
        } else if (d + 5e-12 >= 1e-9) {
            d = d * 1000 * 1000 * 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " n";
        } else if (d + 5e-15 >= 1e-12) {
            d = d * 1000 * 1000 * 1000 * 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " p";
        } else if (d + 5e-18 >= 1e-15) {
            d = d * 1000 * 1000 * 1000 * 1000 * 1000;
            return getDMinus0(getD3FromD(d + 0.0001)) + " f";
        }
        return "";
    }

    /**
     * 获取百分比字符串
     * 
     * <p>限制范围在1-99之间。</p>
     *
     * @param d 百分比值
     * @return 百分比字符串
     */
    public static String getPercent(double d){
        if (d<1) d=1;
        if (d>99) d=99;
        return String.valueOf ((int)d);
    }

    /**
     * 返回三位有效数字的最小单位
     * 
     * <p>例如：8的最小单位为0.01。</p>
     *
     * @param s 带单位的字符串
     * @return 最小单位值
     */
    public static double getMinD3Unit(String s){
        if (s==null || s==""){
            return 0;
        }else if (s.endsWith("p")) {
            s = s.replace("p", "").replace(" ","");
            return getD3Count(s) * 0.001 * 0.001 * 0.001 * 0.001;
        } else if (s.endsWith("n")) {
            s = s.replace("n", "").replace(" ","");
            return getD3Count(s) * 0.001 * 0.001 * 0.001;
        }
        else if (s.endsWith("μ")) {
            s = s.replace("μ", "").replace(" ", "");
            return getD3Count(s) * 0.001 * 0.001;
        }
        else if (s.endsWith("u")) {
            s = s.replace("u", "").replace(" ","");
            return getD3Count(s) * 0.001 * 0.001;
        } else if (s.endsWith("m")) {
            s = s.replace("m", "").replace(" ","");
            return getD3Count(s) * 0.001;
        } else if (s.endsWith("k")) {
            s = s.replace("k", "").replace(" ","");
            return getD3Count(s) * 1000;
        } else if (s.endsWith("M")) {
            s = s.replace("M", "").replace(" ","");
            return getD3Count(s) * 1000 * 1000;
        } else if (s.endsWith("G")) {
            s = s.replace("G", "").replace(" ","");
            return getD3Count(s) * 1000 * 1000 * 1000;
        }else if (s.endsWith("T")){
            s=s.replace("T","").replace(" ","");
            return getD3Count(s) * 1000 * 1000 * 1000*1000;
        }
        else {
            return getD3Count(s.replace(" ",""));
        }

    }

    /**
     * 根据数值大小返回三位有效数字的最小单位
     *
     * @param s 数值字符串
     * @return 最小单位值（0.01、0.1或1）
     */
    public static double getD3Count(String s){
        double d=Double.parseDouble(s);
        d= Math.abs(d);
        if (d>=100){
            return 1;
        }else if (d>=10){
            return 0.1;
        }else {
            return 0.01;
        }
    }
    
    /**
     * 将单位是10μs的long型数据转换成时间字符串
     * 
     * <p>格式：99m59s567.89（单位为ms）</p>
     *
     * @param _10us 10μs为单位的时间值
     * @return 格式化的时间字符串
     */
    public static String getStringFrom10us(long _10us) {
        String str;
        long ms = _10us / 100;
        if (ms < 1000) {
            str = complete3Bit(ms);
        } else if (ms < 1000 * 60) {
            str = complete2bit(ms / 1000) + "s"
                    + complete3Bit(ms % 1000);
        } else {
            str = complete2bit(ms / (1000 * 60)) + "m"
                    + complete2bit(ms % (1000 * 60) / 1000) + "s"
                    + complete3Bit(ms % 1000);
        }
        return str + "." + complete2bit(_10us % 100);
    }

    /**
     * 不足三位的数字，补齐三位
     *
     * @param l 数值
     * @return 补齐后的字符串
     */
    private static String complete3Bit(long l) {
        if (l < 0 || l >= 1000) return String.valueOf(l);
        return (l < 10 ? "00" : (l < 100 ? "0" : "")) + l;
    }

    /**
     * 不足两位的数字，补齐两位
     *
     * @param l 数值
     * @return 补齐后的字符串
     */
    private static String complete2bit(long l) {
        if (l < 0 || l >= 100) return String.valueOf(l);
        return (l < 10 ? "0" : "") + l;
    }

    /**
     * 去除数字字符串前导零
     *
     * @param s 数字字符串
     * @return 去除前导零后的字符串
     */
    public static String getNumRemovePreZero(String s) {
        while (s.startsWith("0")) {
            if (s.equals("0")) {
                break;
            }
            s = s.substring(1);
        }
        return s;
    }

    // ==================== 精度控制相关 ====================
    
    /** 精度模式标志 */
    private static boolean fine;
    
    /** 精度数值 */
    private static int numFine = 8;

    public static int getNumFine() {
        return numFine;
    }

    public static boolean isFine() {
        return fine;
    }

    public static void setFine(boolean tfine) {
        fine = tfine;
    }

    //region math_ax+b: Out of range processing
    
    /**
     * 获取5位有效数字的字符串
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    private static String get5Bit(double d) {
        String s = String.valueOf(d);
        if (s.length() >= 6) {
            s = s.substring(0, 6);
        }
        if (s.contains(".")) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.replace(".", "");
            }
        }
        return s;
    }
    
    /**
     * 根据数值范围获取格式化字符串
     * 
     * <p>自动选择合适的单位：m、u、n、p。</p>
     *
     * @param d 数值
     * @return 格式化的字符串
     */
    public static String getValue(double d) {
        if (DoubleUtil.compareTo(d, 0d) < 0) {
            return "-" + getValue(DoubleUtil.mul(d, -1d));
        } else if (DoubleUtil.compareTo(d, 0d) == 0) {
            return "0";
        } else if (DoubleUtil.compareTo(d, 1000d) >= 0) {
            return "1k";
        } else if (DoubleUtil.compareTo(d, 1d) >= 0) {
            return get5Bit(d);
        } else if (DoubleUtil.compareTo(d, 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d);
            return get5Bit(d) + "m";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d);
            return get5Bit(d) + "u";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d);
            String s = get5Bit(d);
            if (s.contains(".")) {
                String[] split = s.split("\\.");
                if (split[1].length() > 3) {
                    split[1] = split[1].substring(0, 3);
                    s = split[0] + "." + split[1];
                }
            }
            return s + "n";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d);
            return get5Bit(((int) d)) + "p";
        } else {
            return "1p";
        }
    }

    /**
     * 在数值上增加最小单位
     *
     * @param val   带单位的数值字符串
     * @param count 增加的最小单位数量
     * @return 增加后的数值字符串
     */
    public static String addMinUnit(String val,int count){
        double d=TBookUtil.getDoubleFromM(val);
        double minUnit=TBookUtil.getMinD3Unit(val);
        d+=(minUnit*count)+1e-15;
        if (d>100) d+=1e-3;
        if (d<0){
            d=Math.abs(d);
            return "-"+TBookUtil.getMFromDouble(d);
        }else {
            return TBookUtil.getMFromDouble(d);
        }
    }
    
    /**
     * 在数值上减少最小单位
     *
     * @param val   带单位的数值字符串
     * @param count 减少的最小单位数量
     * @return 减少后的数值字符串
     */
    public static String subMinUnit(String val,int count){
        double d=TBookUtil.getDoubleFromM(val);
        double minUnit=TBookUtil.getMinD3Unit(val);
        d-=(minUnit*count)+5e-15;
        if (d>100) d+=1e-3;
        if (d<0){
            d=Math.abs(d);
            return "-"+TBookUtil.getMFromDouble(d);
        }else {
            return TBookUtil.getMFromDouble(d);
        }
    }
    //endregion

    /** 单位前缀数组（从大到小） */
    private static final String[] UNITS = {"P", "T", "G", "M", "k", "", "m", "μ", "n", "p", "f"};
    
    /** 单位换算因子数组（从大到小） */
    private static final double[] VALUES = {1e15, 1e12, 1e9, 1e6, 1e3, 1, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15};

    /**
     * 使用单位前缀格式化数值
     * 
     * <p>自动选择合适的单位前缀，保留4位有效数字。</p>
     *
     * @param value    数值
     * @param baseUnit 基本单位，如"Hz"、"V"、"A"
     * @return 格式化的字符串，如"1.234 kHz"
     */
    public static String formatWithUnit(double value, String baseUnit) {
        if (value == 0) {
            return "0" + baseUnit;
        }
        double absValue = Math.abs(value);
        int unitIndex = 0;
        
        // 查找合适的单位
        for (int i = 0; i < VALUES.length; i++) {
            if (absValue >= VALUES[i]) {
                unitIndex = i;
                break;
            }
        }
        
        // 转换数值并格式化
        double convertedValue = value / VALUES[unitIndex];
        BigDecimal bd = new BigDecimal(String.valueOf(convertedValue));
        bd = bd.round(new MathContext(4, RoundingMode.HALF_UP));
        return bd.doubleValue() + " " + UNITS[unitIndex] + baseUnit;
    }

}
