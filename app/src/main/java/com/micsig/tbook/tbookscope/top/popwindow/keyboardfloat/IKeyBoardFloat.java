package com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat; // 包声明：浮点键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：浮点键盘 - 按键索引常量接口（精简版）                      │
 * │ 核心职责：定义浮点键盘中各按键的位置索引常量                         │
 * │ 架构设计：纯常量接口，由TopDialogFloatKeyBoard实现                  │
 * │ 数据流向：无数据流，仅提供索引常量                                  │
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：TopDialogFloatKeyBoard中通过索引访问键盘按钮              │
 * └──────────────────────────────────────────────────────────────────┘
 */
public interface IKeyBoardFloat { // 浮点键盘按键索引常量接口
    public static final int INDEX_7 = 0; // 数字7按键索引
    public static final int INDEX_8 = 1; // 数字8按键索引
    public static final int INDEX_9 = 2; // 数字9按键索引
    public static final int INDEX_p = 3; // p（皮）单位按键索引
    public static final int INDEX_negative = 4; // 负号按键索引
    public static final int INDEX_4 = 5; // 数字4按键索引
    public static final int INDEX_5 = 6; // 数字5按键索引
    public static final int INDEX_6 = 7; // 数字6按键索引
    public static final int INDEX_n = 8; // n（纳）单位按键索引
    public static final int INDEX_positive = 9; // 正号按键索引
    public static final int INDEX_1 = 10; // 数字1按键索引
    public static final int INDEX_2 = 11; // 数字2按键索引
    public static final int INDEX_3 = 12; // 数字3按键索引
    public static final int INDEX_u = 13; // u（微）单位按键索引
    public static final int INDEX_del = 14; // 删除按键索引
    public static final int INDEX_0 = 15; // 数字0按键索引
    public static final int INDEX_point = 16; // 小数点按键索引
    public static final int INDEX_k = 17; // k（千）单位按键索引
    public static final int INDEX_m = 18; // m（毫）单位按键索引
    public static final int INDEX_enter = 19; // 确认按键索引
}
