package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber; // 数字键盘工具类所在包

import com.micsig.tbook.ui.util.StrUtil; // 字符串工具类

import java.math.BigInteger; // 大整数类，用于进制转换

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          KeyBoardNumberUtil                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：数字键盘工具类，提供进制转换、空格格式化、按键属性判断等核心功能    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                 │
 * │   1. 进制转换：十六进制↔二进制、任意进制→十进制                              │
 * │   2. 空格格式化：根据进制类型按不同间隔插入空格（二进制4位、十六进制2位）     │
 * │   3. 位数补齐：用指定字符补足不足位数，或截取指定位数                         │
 * │   4. 按键属性判断：根据当前进制模式判断各按键是否可用                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                 │
 * │   实现 IDigits 和 IKeyBoardNumber 两个接口，统一常量定义和索引常量           │
 * │   全部为静态方法，作为无状态工具类供 TopDialogNumberKeyBoard 调用             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流：                                                                   │
 * │   TopDialogNumberKeyBoard → KeyBoardNumberUtil（格式化/转换/判断）           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖：IDigits（进制常量）、IKeyBoardNumber（按键索引常量）、StrUtil、BigInteger│
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：数字键盘输入时的实时格式化、进制转换、按键启用/禁用判断             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

public class KeyBoardNumberUtil implements IDigits, IKeyBoardNumber { // 实现进制常量接口和按键索引接口
    /**
     * 删除空格
     * @param s 包含空格的字符串
     * @return 去除所有空格后的字符串
     */
    public static String clearSpace(String s) { // 删除字符串中所有空格
        return s.replace(" ", ""); // 将空格替换为空字符串
    }

    /**
     * 去除前面的0
     * @param s 可能包含前导零的字符串
     * @return 去除前导零后的字符串
     */
    public static String clearPreZero(String s) { // 去除字符串前导零
        s = clearSpace(s); // 先去除空格
        String str = ""; // 存储去除前导零后的结果
        for (int i = 0; i < s.length(); i++) { // 遍历每个字符
            if (!"0".equals(s.substring(i, i + 1))) { // 找到第一个非零字符
                str = s.substring(i); // 从该字符开始截取剩余部分
                break; // 跳出循环
            }
        }
        return str; // 返回去除前导零后的字符串
    }

    /**
     * 重新计算空格
     * @param s 需要格式化的字符串
     * @param digits 当前进制类型
     * @return 按进制间隔插入空格后的字符串
     */
    public static String reCalculateSpace(String s, int digits) { // 根据进制类型重新计算空格位置
        int interval = getInterval(digits); // 获取当前进制的空格间隔
        String s1 = s.replace(" ", ""); // 先去除所有现有空格
        String s2 = ""; // 存储格式化后的结果
        for (int i = s1.length() - 1; i >= 0; i--) { // 从末尾向前遍历
            s2 = s1.charAt(i) + s2; // 将字符拼接到结果前面
            if ((s1.length() - i) % interval == 0 && i != s1.length() - 1) { // 每隔interval个字符且不是最后一个字符时
                s2 = " " + s2; // 在前面插入空格
            }
        }
        return s2; // 返回格式化后的字符串
    }

    /**
     * 得到当前的空格之间的间隔
     * @param digits 当前进制类型
     * @return 空格间隔数（二进制4位、十六进制2位、其他100即不分组）
     */
    public static int getInterval(int digits) { // 获取当前进制对应的空格间隔
        int interval = 100; // 默认间隔为100，即不分组
        if (digits == DIGITS_2 || digits == DIGITS_2X) { // 二进制模式
            interval = 4; // 每4位一组
        } else if (digits == DIGITS_16 || digits == DIGITS_16X) { // 十六进制模式
            interval = 2; // 每2位一组
        }
        return interval; // 返回间隔数
    }

    /**
     * 用c补足不足的位数,或者从后截取bits位
     * @param s 原始字符串
     * @param c 补位字符
     * @param bits 目标位数
     * @return 补齐或截取后的字符串
     */
    public static String toBits(String s, String c, int bits) { // 用指定字符补足位数或截取到位数
        if (s.length() < bits) { // 如果当前长度不足目标位数
            int num = bits - s.length(); // 计算需要补齐的位数
            for (int i = 0; i < num; i++) { // 循环补齐
                s = c + s; // 在前面添加补位字符
            }
        } else { // 如果当前长度超过目标位数
            s = s.substring(s.length() - bits, s.length()); // 从末尾截取目标位数
        }
        return s; // 返回处理后的字符串
    }

    /**
     * 用0补足不足的位数,或者从后截取bits位
     * @param s 原始字符串
     * @param bits 目标位数
     * @return 用0补齐或截取后的字符串
     */
    public static String toBits(String s, int bits) { // 用0补足位数或截取到位数
        return toBits(s, "0", bits); // 调用通用方法，补位字符为"0"
    }

    /**
     * 用0补足不足的位数,保证是偶数位
     * @param s 原始字符串
     * @return 偶数位长度的字符串
     */
    public static String toEvenNumberLength(String s) { // 保证字符串长度为偶数
        if (s.length() % 2 == 0) { // 如果已经是偶数长度
            return s; // 直接返回
        } else { // 如果是奇数长度
            return "0" + s; // 在前面补一个0使其变为偶数长度
        }
    }

    /**
     * 十六进制转二进制
     * @param a 十六进制字符串
     * @return 二进制字符串
     */
    public static String HToB(String a) { // 十六进制转二进制
        String s = clearPreZero(a); // 去除前导零
        if (StrUtil.isEmpty(s)) return "0"; // 空字符串返回"0"
        return Integer.toBinaryString(Integer.valueOf(s, 16)); // 将十六进制解析为整数后转为二进制字符串
    }

    /**
     * 二进制转十六进制
     * @param a 二进制字符串
     * @return 十六进制字符串（大写）
     */
    public static String BToH(String a) { // 二进制转十六进制
        String s = clearPreZero(a); // 去除前导零
        if (StrUtil.isEmpty(s)) return "0"; // 空字符串返回"0"
        return Integer.toHexString(Integer.valueOf(s, 2)).toUpperCase(); // 将二进制解析为整数后转为十六进制大写字符串
    }

    /**
     * 任意进制数转为十进制数
     * @param a 源进制字符串
     * @param b 源进制基数
     * @return 十进制字符串
     */
    public static String toD(String a, int b) { // 任意进制转十进制（int范围）
        String s = clearPreZero(a); // 去除前导零
        if (StrUtil.isEmpty(s)) return "0"; // 空字符串返回"0"
        int aLong = new BigInteger(s, b).intValue(); // 用BigInteger解析指定进制后转为int
        return String.valueOf(aLong).toUpperCase(); // 转为字符串大写形式
    }

    /**
     * 任意进制数转为十进制数
     * @param a 源进制字符串
     * @param b 源进制基数
     * @return 十进制字符串（长整型范围）
     */
    public static String toDLong(String a, int b) { // 任意进制转十进制（long范围）
        String s = clearPreZero(a); // 去除前导零
        if (StrUtil.isEmpty(s)) return "0"; // 空字符串返回"0"
        Long aLong = new BigInteger(s, b).longValue(); // 用BigInteger解析指定进制后转为long
        return String.valueOf(aLong).toUpperCase(); // 转为字符串大写形式
    }

    //region 查询数字键盘上当前位置的按键的属性

    /**
     * 判断按键索引是否为数字键
     * @param index 按键索引
     * @return 是否为数字键
     */
    public static boolean isNumber(int index) { // 判断是否为数字键（排除功能键）
        return index != INDEX_BS && index != INDEX_KBS && index != INDEX_MBS // 不是波特率单位键
                && index != INDEX_ZUO && index != INDEX_ENTER && index != INDEX_POINT; // 不是退格、确认、小数点键
    }

    /**
     * 判断按键索引是否为确认键
     * @param index 按键索引
     * @return 是否为确认键
     */
    public static boolean isEnter(int index) { // 判断是否为确认键
        return index == INDEX_ENTER; // 索引等于确认键索引
    }

    /**
     * 判断按键索引是否为小数点键
     * @param index 按键索引
     * @return 是否为小数点键
     */
    public static boolean isPoint(int index) { // 判断是否为小数点键
        return index == INDEX_POINT; // 索引等于小数点键索引
    }

    /**
     * 判断按键索引是否为退格键
     * @param index 按键索引
     * @return 是否为退格键
     */
    public static boolean isDelete(int index) { // 判断是否为退格键
        return index == INDEX_ZUO; // 索引等于退格键索引
    }

    /**
     * 判断按键索引是否为b/s单位键
     * @param index 按键索引
     * @return 是否为b/s单位键
     */
    public static boolean isBs(int index) { // 判断是否为b/s单位键
        return index == INDEX_BS; // 索引等于b/s键索引
    }

    /**
     * 判断按键索引是否为kb/s单位键
     * @param index 按键索引
     * @return 是否为kb/s单位键
     */
    public static boolean isKbs(int index) { // 判断是否为kb/s单位键
        return index == INDEX_KBS; // 索引等于kb/s键索引
    }

    /**
     * 判断按键索引是否为Mb/s单位键
     * @param index 按键索引
     * @return 是否为Mb/s单位键
     */
    public static boolean isMbs(int index) { // 判断是否为Mb/s单位键
        return index == INDEX_MBS; // 索引等于Mb/s键索引
    }

    /**
     * 判断按键索引是否为二进制有效数字键（0、1）
     * @param index 按键索引
     * @return 是否为二进制数字键
     */
    private static boolean isDigits2(int index) { // 判断是否为二进制数字键
        return index == INDEX_0 || index == INDEX_1; // 只有0和1
    }

    /**
     * 判断按键索引是否为二进制扩展键（0、1、X）
     * @param index 按键索引
     * @return 是否为二进制扩展键
     */
    private static boolean isDigits2x(int index) { // 判断是否为二进制扩展键（含X）
        return isDigits2(index) || index == INDEX_X; // 二进制数字键或X键
    }

    /**
     * 判断按键索引是否为四进制数字键（0-3）
     * @param index 按键索引
     * @return 是否为四进制数字键
     */
    private static boolean isDigits4(int index) { // 判断是否为四进制数字键
        return isDigits2(index) || index == INDEX_2 || index == INDEX_3; // 0、1、2、3
    }

    /**
     * 判断按键索引是否为八进制数字键（0-7）
     * @param index 按键索引
     * @return 是否为八进制数字键
     */
    private static boolean isDigits8(int index) { // 判断是否为八进制数字键
        return isDigits4(index) || index == INDEX_4 || index == INDEX_5 // 0-5
                || index == INDEX_6 || index == INDEX_7; // 6、7
    }

    /**
     * 判断按键索引是否为十进制数字键（0-9）
     * @param index 按键索引
     * @return 是否为十进制数字键
     */
    private static boolean isDigits10(int index) { // 判断是否为十进制数字键
        return isDigits8(index) || index == INDEX_8 || index == INDEX_9; // 0-9
    }

    /**
     * 判断按键索引是否为十六进制数字键（0-9、A-F）
     * @param index 按键索引
     * @return 是否为十六进制数字键
     */
    private static boolean isDigits16(int index) { // 判断是否为十六进制数字键
        return isDigits10(index) || index == INDEX_A || index == INDEX_B // 0-9、A、B
                || index == INDEX_C || index == INDEX_D // C、D
                || index == INDEX_E || index == INDEX_F; // E、F
    }

    /**
     * 判断按键索引是否为十六进制扩展键（0-9、A-F、X）
     * @param index 按键索引
     * @return 是否为十六进制扩展键
     */
    private static boolean isDigits16x(int index) { // 判断是否为十六进制扩展键（含X）
        return isDigits16(index) || index == INDEX_X; // 十六进制数字键或X键
    }

    /**
     * 判断按键索引是否为波特率模式有效键（单位键+浮点数字键）
     * @param index 按键索引
     * @return 是否为波特率模式有效键
     */
    private static boolean isDigitsBs(int index) { // 判断是否为波特率模式有效键
        return index == INDEX_BS || index == INDEX_KBS || index == INDEX_MBS // 波特率单位键
                || isDigitsFloat(index); // 或浮点数字键
    }

    /**
     * 判断按键索引是否为0-8数字键
     * @param index 按键索引
     * @return 是否为0-8数字键
     */
    private static boolean isDigits0_8(int index) { // 判断是否为0-8数字键
        return isDigits8(index) || index == INDEX_8; // 八进制数字键或8键
    }

    /**
     * 判断按键索引是否为浮点模式有效键（0-9、小数点，排除A-F和单位键）
     * @param index 按键索引
     * @return 是否为浮点模式有效键
     */
    private static boolean isDigitsFloat(int index) { // 判断是否为浮点模式有效键
        return index != INDEX_X && index != INDEX_A // 排除X、A
                && index != INDEX_B && index != INDEX_C // 排除B、C
                && index != INDEX_D && index != INDEX_E // 排除D、E
                && index != INDEX_F && index != INDEX_BS // 排除F、b/s
                && index != INDEX_KBS && index != INDEX_MBS; // 排除kb/s、Mb/s
    }

    /**
     * 根据进制类型判断按键是否可用
     * @param digits 当前进制类型
     * @param index 按键索引
     * @return 按键是否可用
     */
    public static boolean isEnabled(int digits, int index) { // 根据进制类型判断按键是否启用
        if (!(index == INDEX_ENTER || index == INDEX_ZUO)) { // 确认键和退格键始终可用，不需要判断
            switch (digits) { // 根据进制类型判断
                case IDigits.DIGITS_2: // 二进制
                    return isDigits2(index); // 只有0、1可用
                case IDigits.DIGITS_2X: // 二进制扩展
                    return isDigits2x(index); // 0、1、X可用
                case IDigits.DIGITS_4: // 四进制
                    return isDigits4(index); // 0-3可用
                case IDigits.DIGITS_8: // 八进制
                    return isDigits8(index); // 0-7可用
                case IDigits.DIGITS_10: // 十进制
                    return isDigits10(index); // 0-9可用
                case IDigits.DIGITS_16: // 十六进制
                    return isDigits16(index); // 0-9、A-F可用
                case IDigits.DIGITS_16X: // 十六进制扩展
                    return isDigits16x(index); // 0-9、A-F、X可用
                case IDigits.DIGITS_BAUDRATE: // 波特率模式
                    return isDigitsBs(index); // 单位键+浮点数字键可用
                case IDigits.DIGITS_0_8: // 0-8模式
                    return isDigits0_8(index); // 0-8可用
                case IDigits.DIGITS_FLOAT: // 浮点模式
                    return isDigitsFloat(index); // 浮点数字键可用
            }
        }
        return true; // 确认键和退格键始终可用
    }
    //endregion
}
