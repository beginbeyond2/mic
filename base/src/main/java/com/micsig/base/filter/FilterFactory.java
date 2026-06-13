package com.micsig.base.filter; // 定义过滤器子包

import android.text.InputFilter; // 导入输入过滤器接口

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                          FilterFactory 过滤器工厂                            │
 * │                            输入过滤器创建与管理                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 输入过滤器工厂                                      │
 * │   提供预定义正则表达式的输入过滤器创建功能                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 定义常用输入验证的正则表达式常量                                        │
 * │   2. 提供工厂方法创建输入过滤器                                             │
 * │   3. 支持多种进制输入验证                                                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   静态工厂模式设计                                                         │
 * │   预定义常用正则表达式常量                                                  │
 * │   通过getFactory方法创建CommonInputFilter实例                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   正则表达式常量 → getFilter() → CommonInputFilter实例                      │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: CommonInputFilter (通用输入过滤器)                                   │
 * │   依赖: android.text.InputFilter (输入过滤器接口)                            │
 * │   被依赖: 各输入框组件、参数设置界面                                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 创建十六进制输入过滤器                                                │
 * │   InputFilter hexFilter = FilterFactory.getFilter(FilterFactory.HEX_REGEX);│
 * │   editText.setFilters(new InputFilter[]{hexFilter});                       │
 * │   // 创建波特率输入过滤器                                                  │
 * │   InputFilter baudFilter = FilterFactory.getFilter(FilterFactory.BAUDRATE_REGEX);│
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Micsig R&D Team
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class FilterFactory { // 过滤器工厂类

    // ==================== 正则表达式常量定义 ====================
    
    /**
     * 波特率输入正则表达式
     * 匹配：数字和小数点的组合（如 9600, 115200.5）
     */
    public static final String BAUDRATE_REGEX = "^\\d*\\.?\\d*$"; // 波特率：允许数字和小数点
    
    /**
     * 二进制输入正则表达式
     * 匹配：0、1和空格的组合（如 1010 1100）
     */
    public static final String BINARY_REGEX  = "^[01 ]*$"; // 二进制：只允许0、1和空格
    
    /**
     * 二进制（含X）输入正则表达式
     * 匹配：0、1、X、x和空格的组合（用于掩码设置）
     */
    public static final String BINARY_X_REGEX  = "^[01Xx ]*$"; // 二进制和X：允许0、1、X、x和空格
    
    /**
     * 四进制输入正则表达式
     * 匹配：0-3和空格的组合
     */
    public static final String QUATERNARY_REGEX  = "^[0-3 ]*$"; // 四进制：只允许0-3和空格
    
    /**
     * 八进制输入正则表达式
     * 匹配：0-7和空格的组合
     */
    public static final String OCTAL_REGEX = "^[0-7 ]*$"; // 八进制：只允许0-7和空格
    
    /**
     * 十进制输入正则表达式
     * 匹配：0-9和空格的组合
     */
    public static final String DECIMAL_REGEX = "^[0-9 ]*$"; // 十进制：只允许0-9和空格
    
    /**
     * 十六进制输入正则表达式
     * 匹配：0-9、A-F、a-f和空格的组合
     */
    public static final String HEX_REGEX = "^[0-9A-Fa-f ]*$"; // 十六进制：允许0-9、A-F、a-f和空格
    
    /**
     * 十六进制（含X）输入正则表达式
     * 匹配：0-9、A-F、a-f、X、x和空格的组合（用于掩码设置）
     */
    public static final String HEX_X_REGEX = "^[0-9A-Fa-fXx ]*$"; // 十六进制和X：允许十六进制字符和X
    
    /**
     * 中文字符正则表达式
     * 匹配：Unicode中文字符范围
     */
    public static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]"; // 中文字符：匹配Unicode中文范围

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取输入过滤器                                                          │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   根据正则表达式创建输入过滤器实例                                        │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param regex 正则表达式字符串，可使用预定义常量                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return CommonInputFilter实例                                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   // 使用预定义常量                                                     │
     * │   InputFilter hexFilter = FilterFactory.getFilter(FilterFactory.HEX_REGEX);│
     * │   // 使用自定义正则                                                    │
     * │   InputFilter customFilter = FilterFactory.getFilter("^[A-Z]*$");      │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static InputFilter getFilter(String regex) { // 工厂方法：创建过滤器
        return new CommonInputFilter(regex); // 创建并返回CommonInputFilter实例
    }

}
