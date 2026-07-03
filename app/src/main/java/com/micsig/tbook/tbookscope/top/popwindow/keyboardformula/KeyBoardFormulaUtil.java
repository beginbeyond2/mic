package com.micsig.tbook.tbookscope.top.popwindow.keyboardformula; // 公式键盘工具类所在包

import com.micsig.base.Logger; // 日志工具类
import com.micsig.tbook.tbookscope.R; // 资源类
import com.micsig.tbook.tbookscope.util.App; // 应用工具类
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类

import java.util.ArrayList; // 动态数组类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                         KeyBoardFormulaUtil                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：公式键盘工具类，提供表达式转换、解析、按键可见性判断等核心功能      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                 │
 * │   1. 表达式转换：将显示用公式（×÷πtb）转为示波器可识别格式（*/P/数值）       │
 * │   2. 表达式解析：将显示文本拆分为token列表（getSelectionListFromShowText）   │
 * │   3. 按键可见性：根据当前光标位置和上下文判断各按键是否可用                  │
 * │   4. 时基处理：将时基字符串转为数值用于表达式计算                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                 │
 * │   实现IKeyBoardFormula接口，统一按键索引常量                                │
 * │   全部为静态方法，作为无状态工具类供TopDialogFormulaKeyBoard调用              │
 * │   使用字符串数组（searchList/keyList）映射显示文本与按键索引                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流：                                                                   │
 * │   TopDialogFormulaKeyBoard → KeyBoardFormulaUtil（解析/转换/可见性判断）     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖：IKeyBoardFormula、StrUtil、TBookUtil、App、R资源数组                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：公式键盘输入时的实时解析、确认时的表达式转换、按键启用/禁用判断     │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class KeyBoardFormulaUtil implements IKeyBoardFormula { // 实现公式键盘按键索引接口
    private static final String TAG = "KeyBoardFormulaUtil"; // 日志标签


    /**
     * 将显示用公式字符串转换为示波器可识别的表达式格式
     * @param exprString 显示用公式字符串（含×÷πtb等显示符号）
     * @param centerTimeBase 中心时基字符串
     * @return 示波器可识别的表达式字符串
     */
    public static String amFormulaToScope(String exprString, String centerTimeBase) { // 将显示格式公式转为示波器格式
        return exprString // 返回转换后的表达式
                .replace("×", "*") // 乘号×替换为*
                .replace("÷", "/") // 除号÷替换为/
                .replace("π", "P") // 圆周率π替换为P
                .replace(App.get().getResources().getString(R.string.key_formula_tb), handleTimeBase(centerTimeBase)); // 时基符号替换为数值
    }

    private static String[] searchList; // 搜索列表，用于从显示文本中解析token
    private static String[] keyList; // 按键显示文本列表，用于索引查找

    /**
     * 获取搜索列表（懒加载）
     * @return 搜索字符串数组
     */
    public static String[] getSearchList() { // 获取搜索列表
        if (searchList == null) { // 如果尚未初始化
            searchList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_search_list); // 从资源加载
        }
        return searchList; // 返回搜索列表
    }

    /**
     * 获取按键显示文本列表（懒加载）
     * @return 按键显示文本数组
     */
    public static String[] getKeyList() { // 获取按键显示文本列表
        if (keyList == null) { // 如果尚未初始化
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list); // 从资源加载
        }
        return keyList; // 返回按键显示文本列表
    }

    /**
     * 从显示文本中解析出token列表
     * @param string 显示文本字符串
     * @return token列表，每个元素对应一个按键的显示文本
     */
    public static ArrayList<String> getSelectionListFromShowText(String string) { // 从显示文本解析token列表
        if (searchList == null) { // 如果搜索列表未初始化
            searchList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_search_list); // 从资源加载
        }
        ArrayList<String> list = new ArrayList<>(); // 存储解析出的token列表

        boolean startWithSearchItem = true;//这个变量是为了处理 键盘乱输入时避免下面死循环问题。 // 防止乱输入导致死循环的标志
        while (!StrUtil.isEmpty(string) && startWithSearchItem) { // 当字符串非空且以已知token开头时循环
            boolean nowHasSearchItem = false; // 当前是否匹配到搜索项
            for (int i = 0; i < searchList.length; i++) { // 遍历搜索列表
                if (string.startsWith(searchList[i])) { // 如果字符串以搜索项开头
                    nowHasSearchItem = true; // 标记匹配成功
                    break; // 跳出循环
                }
            }
            startWithSearchItem = nowHasSearchItem; // 更新标志

            for (int i = 0; i < searchList.length; i++) { // 遍历搜索列表
                if (string.startsWith(searchList[i])) { // 如果字符串以搜索项开头
                    list.add(searchList[i]); // 将匹配的token添加到列表
                    string = string.substring(searchList[i].length()); // 截去已匹配的部分
                    break; // 跳出内层循环
                }
            }
            Logger.d(TAG, "getSelectionListFromShowText() called with: string = [" + string + "]"); // 打印调试日志
        }
        Logger.d(TAG, "getSelectionListFromShowText() called with: string = [" + string + "], return :" + list); // 打印结果日志
        return list; // 返回token列表
    }

    /**
     * 根据显示文本查找对应的按键索引
     * @param s 按键显示文本
     * @return 按键索引，未找到返回INDEX_null
     */
    public static int getIndex(String s) { // 根据显示文本查找按键索引
        if (StrUtil.isEmpty(s)) { // 如果字符串为空
            return INDEX_null; // 返回无效索引
        }
        if (keyList == null) { // 如果按键列表未初始化
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list); // 从资源加载
        }
        for (int i = 0; i < keyList.length; i++) { // 遍历按键列表
            if (keyList[i].equals(s)) { // 如果找到匹配项
                return i; // 返回索引
            }
        }
        return INDEX_null; // 未找到返回无效索引
    }

    /**
     * 根据当前光标位置的按键索引和上下文状态，计算各按键的可见性列表
     * @param keyIndex                当前键位的序号
     * @param eAddSubAfterBracketLeft +-之后的左括号的可点击性（在不在e±之后不一样）
     * @param numberAfterPoint        数字之后的小数点的可点击性（是否已包含小数点）
     * @param numberAfterE            数字之后的E的可点击性（是否已包含E）
     * @param afterBracketRight       后括号的可点击性（是否比前括号多）
     * @param zeroAfterZero           0之后的0的可点击性（是否是纯0数字串）
     * @return 各按键的可见性布尔数组，true=可见/可点击
     */
    public static boolean[] getVisibleListFromCurSelection(int keyIndex, boolean eAddSubAfterBracketLeft,
                                                           boolean numberAfterPoint, boolean numberAfterE,
                                                           boolean afterBracketRight, boolean zeroAfterZero) { // 根据当前按键和上下文计算可见性
        Logger.d(TAG, "getVisibleListFromCurSelection() called with: keyIndex = [" + keyIndex + "], eAddSubAfterBracketLeft = [" + eAddSubAfterBracketLeft + "], numberAfterPoint = [" + numberAfterPoint + "], numberAfterE = [" + numberAfterE + "], afterBracketRight = [" + afterBracketRight + "], zeroAfterZero = [" + zeroAfterZero + "]"); // 打印参数日志
        if (keyList == null) { // 如果按键列表未初始化
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list); // 从资源加载
        }
        boolean[] visibleList = new boolean[keyList.length]; // 创建可见性数组
        for (int i = 0; i < visibleList.length; i++) { // 遍历所有按键
            visibleList[i] = true; // 默认全部可见
        }
        switch (keyIndex) { // 根据当前按键索引判断
            case INDEX_null: { // 初始状态（空表达式或光标在开头）
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_add: // 加号后
            case INDEX_sub: // 减号后
            case INDEX_mul: // 乘号后
            case INDEX_div: { // 除号后
                if (keyIndex == INDEX_add || keyIndex == INDEX_sub) { // 加号或减号后
                    visibleList[INDEX_bracket_left] = eAddSubAfterBracketLeft; // 左括号取决于是否在E±之后
                    visibleList[INDEX_sqrt] = eAddSubAfterBracketLeft; // 平方根取决于是否在E±之后
                    visibleList[INDEX_abs] = eAddSubAfterBracketLeft; // 绝对值取决于是否在E±之后
                    visibleList[INDEX_deg] = eAddSubAfterBracketLeft; // 角度转弧度取决于是否在E±之后
                    visibleList[INDEX_rad] = eAddSubAfterBracketLeft; // 弧度转角度取决于是否在E±之后
                    visibleList[INDEX_exp] = eAddSubAfterBracketLeft; // 指数函数取决于是否在E±之后
                    visibleList[INDEX_diff] = eAddSubAfterBracketLeft; // 微分取决于是否在E±之后
                    visibleList[INDEX_ln] = eAddSubAfterBracketLeft; // 自然对数取决于是否在E±之后
                    visibleList[INDEX_sine] = eAddSubAfterBracketLeft; // 正弦取决于是否在E±之后
                    visibleList[INDEX_cos] = eAddSubAfterBracketLeft; // 余弦取决于是否在E±之后
                    visibleList[INDEX_tan] = eAddSubAfterBracketLeft; // 正切取决于是否在E±之后
                    visibleList[INDEX_intg] = eAddSubAfterBracketLeft; // 积分取决于是否在E±之后
                    visibleList[INDEX_lg] = eAddSubAfterBracketLeft; // 常用对数取决于是否在E±之后
                    visibleList[INDEX_arcsin] = eAddSubAfterBracketLeft; // 反正弦取决于是否在E±之后
                    visibleList[INDEX_arccos] = eAddSubAfterBracketLeft; // 反余弦取决于是否在E±之后
                    visibleList[INDEX_arctan] = eAddSubAfterBracketLeft; // 反正切取决于是否在E±之后
                    visibleList[INDEX_ch1] = eAddSubAfterBracketLeft; // 通道1取决于是否在E±之后
                    visibleList[INDEX_ch2] = eAddSubAfterBracketLeft; // 通道2取决于是否在E±之后
                    visibleList[INDEX_ch3] = eAddSubAfterBracketLeft; // 通道3取决于是否在E±之后
                    visibleList[INDEX_ch4] = eAddSubAfterBracketLeft; // 通道4取决于是否在E±之后
                    visibleList[INDEX_ch5] = eAddSubAfterBracketLeft; // 通道5取决于是否在E±之后
                    visibleList[INDEX_ch6] = eAddSubAfterBracketLeft; // 通道6取决于是否在E±之后
                    visibleList[INDEX_ch7] = eAddSubAfterBracketLeft; // 通道7取决于是否在E±之后
                    visibleList[INDEX_ch8] = eAddSubAfterBracketLeft; // 通道8取决于是否在E±之后
                    visibleList[INDEX_var1] = eAddSubAfterBracketLeft; // 变量1取决于是否在E±之后
                    visibleList[INDEX_var2] = eAddSubAfterBracketLeft; // 变量2取决于是否在E±之后
                    visibleList[INDEX_time_base] = eAddSubAfterBracketLeft; // 时基取决于是否在E±之后
                    visibleList[INDEX_pi] = eAddSubAfterBracketLeft; // 圆周率取决于是否在E±之后
                }
                visibleList[INDEX_add] = false; // 加号不可用
                visibleList[INDEX_sub] = false; // 减号不可用
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_less: // 小于号后
            case INDEX_greater: // 大于号后
            case INDEX_less_equal: // 小于等于号后
            case INDEX_greater_equal: // 大于等于号后
            case INDEX_equal: // 等于号后
            case INDEX_not_equal: // 不等于号后
            case INDEX_and: // 逻辑与后
            case INDEX_or: { // 逻辑或后
                visibleList[INDEX_add] = false; // 加号不可用
                visibleList[INDEX_sub] = false; // 减号不可用
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_sqrt: // 平方根后
            case INDEX_abs: // 绝对值后
            case INDEX_deg: // 角度转弧度后
            case INDEX_rad: // 弧度转角度后
            case INDEX_exp: // 指数函数后
            case INDEX_diff: // 微分后
            case INDEX_ln: // 自然对数后
            case INDEX_sine: // 正弦后
            case INDEX_cos: // 余弦后
            case INDEX_tan: // 正切后
            case INDEX_intg: // 积分后
            case INDEX_lg: // 常用对数后
            case INDEX_arcsin: // 反正弦后
            case INDEX_arccos: // 反余弦后
            case INDEX_arctan: // 反正切后
            case INDEX_no: // 逻辑非后
            case INDEX_bracket_left: { // 左括号后
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_bracket_right: { // 右括号后
                visibleList[INDEX_bracket_left] = false; // 左括号不可用
                visibleList[INDEX_bracket_right] = afterBracketRight; // 右括号取决于是否有未匹配的左括号
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_sqrt] = false; // 平方根不可用
                visibleList[INDEX_abs] = false; // 绝对值不可用
                visibleList[INDEX_deg] = false; // 角度转弧度不可用
                visibleList[INDEX_rad] = false; // 弧度转角度不可用
                visibleList[INDEX_exp] = false; // 指数函数不可用
                visibleList[INDEX_diff] = false; // 微分不可用
                visibleList[INDEX_ln] = false; // 自然对数不可用
                visibleList[INDEX_sine] = false; // 正弦不可用
                visibleList[INDEX_cos] = false; // 余弦不可用
                visibleList[INDEX_tan] = false; // 正切不可用
                visibleList[INDEX_intg] = false; // 积分不可用
                visibleList[INDEX_lg] = false; // 常用对数不可用
                visibleList[INDEX_arcsin] = false; // 反正弦不可用
                visibleList[INDEX_arccos] = false; // 反余弦不可用
                visibleList[INDEX_arctan] = false; // 反正切不可用
                visibleList[INDEX_ch1] = false; // 通道1不可用
                visibleList[INDEX_ch2] = false; // 通道2不可用
                visibleList[INDEX_ch3] = false; // 通道3不可用
                visibleList[INDEX_ch4] = false; // 通道4不可用
                visibleList[INDEX_ch5] = false; // 通道5不可用
                visibleList[INDEX_ch6] = false; // 通道6不可用
                visibleList[INDEX_ch7] = false; // 通道7不可用
                visibleList[INDEX_ch8] = false; // 通道8不可用
                visibleList[INDEX_pi] = false; // 圆周率不可用
                visibleList[INDEX_time_base] = false; // 时基不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_var1] = false; // 变量1不可用
                visibleList[INDEX_var2] = false; // 变量2不可用
                visibleList[INDEX_0] = false; // 数字0不可用
                visibleList[INDEX_1] = false; // 数字1不可用
                visibleList[INDEX_2] = false; // 数字2不可用
                visibleList[INDEX_3] = false; // 数字3不可用
                visibleList[INDEX_4] = false; // 数字4不可用
                visibleList[INDEX_5] = false; // 数字5不可用
                visibleList[INDEX_6] = false; // 数字6不可用
                visibleList[INDEX_7] = false; // 数字7不可用
                visibleList[INDEX_8] = false; // 数字8不可用
                visibleList[INDEX_9] = false; // 数字9不可用
                visibleList[INDEX_point] = false; // 小数点不可用
            }
            break;
            case INDEX_ch1: // 通道1后
            case INDEX_ch2: // 通道2后
            case INDEX_ch3: // 通道3后
            case INDEX_ch4: // 通道4后
            case INDEX_ch5: // 通道5后
            case INDEX_ch6: // 通道6后
            case INDEX_ch7: // 通道7后
            case INDEX_ch8: // 通道8后
            case INDEX_time_base: // 时基后
            case INDEX_pi: // 圆周率后
            case INDEX_var1: // 变量1后
            case INDEX_var2: // 变量2后
            case INDEX_m: // 毫(m)后
            case INDEX_T: // 太(T)后
            case INDEX_u: // 微(μ)后
            case INDEX_G: // 吉(G)后
            case INDEX_n: // 纳(n)后
            case INDEX_M: // 兆(M)后
            case INDEX_f: // 飞(f)后
            case INDEX_p: // 皮(p)后
            case INDEX_k: { // 千(k)后
                visibleList[INDEX_bracket_right] = afterBracketRight; // 右括号取决于是否有未匹配的左括号
                visibleList[INDEX_sqrt] = false; // 平方根不可用
                visibleList[INDEX_abs] = false; // 绝对值不可用
                visibleList[INDEX_deg] = false; // 角度转弧度不可用
                visibleList[INDEX_rad] = false; // 弧度转角度不可用
                visibleList[INDEX_exp] = false; // 指数函数不可用
                visibleList[INDEX_diff] = false; // 微分不可用
                visibleList[INDEX_ln] = false; // 自然对数不可用
                visibleList[INDEX_sine] = false; // 正弦不可用
                visibleList[INDEX_cos] = false; // 余弦不可用
                visibleList[INDEX_tan] = false; // 正切不可用
                visibleList[INDEX_intg] = false; // 积分不可用
                visibleList[INDEX_lg] = false; // 常用对数不可用
                visibleList[INDEX_arcsin] = false; // 反正弦不可用
                visibleList[INDEX_arccos] = false; // 反余弦不可用
                visibleList[INDEX_arctan] = false; // 反正切不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_bracket_left] = false; // 左括号不可用
                visibleList[INDEX_ch1] = false; // 通道1不可用
                visibleList[INDEX_ch2] = false; // 通道2不可用
                visibleList[INDEX_ch3] = false; // 通道3不可用
                visibleList[INDEX_ch4] = false; // 通道4不可用
                visibleList[INDEX_ch5] = false; // 通道5不可用
                visibleList[INDEX_ch6] = false; // 通道6不可用
                visibleList[INDEX_ch7] = false; // 通道7不可用
                visibleList[INDEX_ch8] = false; // 通道8不可用
                visibleList[INDEX_time_base] = false; // 时基不可用
                visibleList[INDEX_pi] = false; // 圆周率不可用
                visibleList[INDEX_var1] = false; // 变量1不可用
                visibleList[INDEX_var2] = false; // 变量2不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_0] = false; // 数字0不可用
                visibleList[INDEX_1] = false; // 数字1不可用
                visibleList[INDEX_2] = false; // 数字2不可用
                visibleList[INDEX_3] = false; // 数字3不可用
                visibleList[INDEX_4] = false; // 数字4不可用
                visibleList[INDEX_5] = false; // 数字5不可用
                visibleList[INDEX_6] = false; // 数字6不可用
                visibleList[INDEX_7] = false; // 数字7不可用
                visibleList[INDEX_8] = false; // 数字8不可用
                visibleList[INDEX_9] = false; // 数字9不可用
                visibleList[INDEX_point] = false; // 小数点不可用
            }
            break;
            case INDEX_E: { // 科学计数法E后
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_sqrt] = false; // 平方根不可用
                visibleList[INDEX_abs] = false; // 绝对值不可用
                visibleList[INDEX_deg] = false; // 角度转弧度不可用
                visibleList[INDEX_rad] = false; // 弧度转角度不可用
                visibleList[INDEX_exp] = false; // 指数函数不可用
                visibleList[INDEX_diff] = false; // 微分不可用
                visibleList[INDEX_ln] = false; // 自然对数不可用
                visibleList[INDEX_sine] = false; // 正弦不可用
                visibleList[INDEX_cos] = false; // 余弦不可用
                visibleList[INDEX_tan] = false; // 正切不可用
                visibleList[INDEX_intg] = false; // 积分不可用
                visibleList[INDEX_lg] = false; // 常用对数不可用
                visibleList[INDEX_arcsin] = false; // 反正弦不可用
                visibleList[INDEX_arccos] = false; // 反余弦不可用
                visibleList[INDEX_arctan] = false; // 反正切不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_bracket_left] = false; // 左括号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_ch1] = false; // 通道1不可用
                visibleList[INDEX_ch2] = false; // 通道2不可用
                visibleList[INDEX_ch3] = false; // 通道3不可用
                visibleList[INDEX_ch4] = false; // 通道4不可用
                visibleList[INDEX_ch5] = false; // 通道5不可用
                visibleList[INDEX_ch6] = false; // 通道6不可用
                visibleList[INDEX_ch7] = false; // 通道7不可用
                visibleList[INDEX_ch8] = false; // 通道8不可用
                visibleList[INDEX_time_base] = false; // 时基不可用
                visibleList[INDEX_pi] = false; // 圆周率不可用
                visibleList[INDEX_var1] = false; // 变量1不可用
                visibleList[INDEX_var2] = false; // 变量2不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_0: // 数字0后
            case INDEX_1: // 数字1后
            case INDEX_2: // 数字2后
            case INDEX_3: // 数字3后
            case INDEX_4: // 数字4后
            case INDEX_5: // 数字5后
            case INDEX_6: // 数字6后
            case INDEX_7: // 数字7后
            case INDEX_8: // 数字8后
            case INDEX_9: { // 数字9后
                visibleList[INDEX_sqrt] = false; // 平方根不可用
                visibleList[INDEX_abs] = false; // 绝对值不可用
                visibleList[INDEX_deg] = false; // 角度转弧度不可用
                visibleList[INDEX_rad] = false; // 弧度转角度不可用
                visibleList[INDEX_exp] = false; // 指数函数不可用
                visibleList[INDEX_diff] = false; // 微分不可用
                visibleList[INDEX_ln] = false; // 自然对数不可用
                visibleList[INDEX_sine] = false; // 正弦不可用
                visibleList[INDEX_cos] = false; // 余弦不可用
                visibleList[INDEX_tan] = false; // 正切不可用
                visibleList[INDEX_intg] = false; // 积分不可用
                visibleList[INDEX_lg] = false; // 常用对数不可用
                visibleList[INDEX_arcsin] = false; // 反正弦不可用
                visibleList[INDEX_arccos] = false; // 反余弦不可用
                visibleList[INDEX_arctan] = false; // 反正切不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_bracket_left] = false; // 左括号不可用
                visibleList[INDEX_bracket_right] = afterBracketRight; // 右括号取决于是否有未匹配的左括号
                visibleList[INDEX_ch1] = false; // 通道1不可用
                visibleList[INDEX_ch2] = false; // 通道2不可用
                visibleList[INDEX_ch3] = false; // 通道3不可用
                visibleList[INDEX_ch4] = false; // 通道4不可用
                visibleList[INDEX_ch5] = false; // 通道5不可用
                visibleList[INDEX_ch6] = false; // 通道6不可用
                visibleList[INDEX_ch7] = false; // 通道7不可用
                visibleList[INDEX_ch8] = false; // 通道8不可用
                visibleList[INDEX_time_base] = false; // 时基不可用
                visibleList[INDEX_pi] = false; // 圆周率不可用
                visibleList[INDEX_var1] = false; // 变量1不可用
                visibleList[INDEX_var2] = false; // 变量2不可用
                visibleList[INDEX_E] = numberAfterE; // 科学计数法E取决于当前数字段是否已包含E
                visibleList[INDEX_point] = numberAfterPoint; // 小数点取决于当前数字段是否已包含小数点
                visibleList[INDEX_0] = zeroAfterZero; // 数字0取决于是否是纯0数字串
            }
            break;
            case INDEX_point: { // 小数点后
                visibleList[INDEX_add] = false; // 加号不可用
                visibleList[INDEX_sub] = false; // 减号不可用
                visibleList[INDEX_mul] = false; // 乘号不可用
                visibleList[INDEX_div] = false; // 除号不可用
                visibleList[INDEX_bracket_left] = false; // 左括号不可用
                visibleList[INDEX_less] = false; // 小于号不可用
                visibleList[INDEX_greater] = false; // 大于号不可用
                visibleList[INDEX_less_equal] = false; // 小于等于号不可用
                visibleList[INDEX_greater_equal] = false; // 大于等于号不可用
                visibleList[INDEX_bracket_right] = false; // 右括号不可用
                visibleList[INDEX_equal] = false; // 等于号不可用
                visibleList[INDEX_not_equal] = false; // 不等于号不可用
                visibleList[INDEX_and] = false; // 逻辑与不可用
                visibleList[INDEX_or] = false; // 逻辑或不可用
                visibleList[INDEX_no] = false; // 逻辑非不可用
                visibleList[INDEX_sqrt] = false; // 平方根不可用
                visibleList[INDEX_abs] = false; // 绝对值不可用
                visibleList[INDEX_deg] = false; // 角度转弧度不可用
                visibleList[INDEX_rad] = false; // 弧度转角度不可用
                visibleList[INDEX_exp] = false; // 指数函数不可用
                visibleList[INDEX_diff] = false; // 微分不可用
                visibleList[INDEX_ln] = false; // 自然对数不可用
                visibleList[INDEX_sine] = false; // 正弦不可用
                visibleList[INDEX_cos] = false; // 余弦不可用
                visibleList[INDEX_tan] = false; // 正切不可用
                visibleList[INDEX_intg] = false; // 积分不可用
                visibleList[INDEX_lg] = false; // 常用对数不可用
                visibleList[INDEX_arcsin] = false; // 反正弦不可用
                visibleList[INDEX_arccos] = false; // 反余弦不可用
                visibleList[INDEX_arctan] = false; // 反正切不可用
                visibleList[INDEX_ch1] = false; // 通道1不可用
                visibleList[INDEX_ch2] = false; // 通道2不可用
                visibleList[INDEX_ch3] = false; // 通道3不可用
                visibleList[INDEX_ch4] = false; // 通道4不可用
                visibleList[INDEX_pi] = false; // 圆周率不可用
                visibleList[INDEX_ch5] = false; // 通道5不可用
                visibleList[INDEX_ch6] = false; // 通道6不可用
                visibleList[INDEX_ch7] = false; // 通道7不可用
                visibleList[INDEX_ch8] = false; // 通道8不可用
                visibleList[INDEX_time_base] = false; // 时基不可用
                visibleList[INDEX_E] = false; // 科学计数法E不可用
                visibleList[INDEX_var1] = false; // 变量1不可用
                visibleList[INDEX_var2] = false; // 变量2不可用
                visibleList[INDEX_m] = false; // 毫(m)不可用
                visibleList[INDEX_T] = false; // 太(T)不可用
                visibleList[INDEX_u] = false; // 微(μ)不可用
                visibleList[INDEX_G] = false; // 吉(G)不可用
                visibleList[INDEX_n] = false; // 纳(n)不可用
                visibleList[INDEX_M] = false; // 兆(M)不可用
                visibleList[INDEX_point] = false; // 小数点不可用
                visibleList[INDEX_f] = false; // 飞(f)不可用
                visibleList[INDEX_p] = false; // 皮(p)不可用
                visibleList[INDEX_k] = false; // 千(k)不可用
            }
            break;
            case INDEX_enter: // 确认键后
            case INDEX_del: // 删除键后
                break; // 不需要额外处理
        }
        visibleList[INDEX_null1] = false; // 占位键始终不可用
        return visibleList; // 返回可见性列表
    }

    /**
     * 判断按键索引是否为数字键
     * @param index 按键索引
     * @return 是否为数字键（0-9）
     */
    public static boolean isNumber(int index) { // 判断是否为数字键
        return index == INDEX_0 || index == INDEX_1 || index == INDEX_2 // 0、1、2
                || index == INDEX_3 || index == INDEX_4 || index == INDEX_5 // 3、4、5
                || index == INDEX_6 || index == INDEX_7 || index == INDEX_8 // 6、7、8
                || index == INDEX_9; // 9
    }

    /**
     * 处理时基字符串，将其转换为数值字符串
     * @param timeBase 时基字符串（可能含换行符）
     * @return 时基数值字符串
     */
    public static String handleTimeBase(String timeBase) { // 处理时基字符串
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase; // 如果含换行符则取第二行
        if (timeBase.isEmpty()) { // 如果为空
            timeBase = "1"; // 默认值为1
        } else { // 非空
            timeBase = (TBookUtil.getSFromTime(timeBase.replace(" ", "")) + "").replaceAll(" ", ""); // 将时基转为秒数值字符串
        }
        return timeBase; // 返回时基数值字符串
    }
}
