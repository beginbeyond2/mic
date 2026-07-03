package com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat; // 包声明：浮点键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：全功能浮点键盘 - 按键索引常量接口                          │
 * │ 核心职责：定义全功能浮点键盘中各按键的位置索引常量（含f/T/G/M单位）  │
 * │ 架构设计：纯常量接口，由TopDialogFullFloatKeyBoard实现              │
 * │ 数据流向：无数据流，仅提供索引常量                                  │
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：TopDialogFullFloatKeyBoard中通过索引访问键盘按钮          │
 * └──────────────────────────────────────────────────────────────────┘
 */
public interface IFullKeyBoardFloat { // 全功能浮点键盘按键索引常量接口
    public static final int INDEX_7 = 0; // 数字7按键索引
    public static final int INDEX_8 = 1; // 数字8按键索引
    public static final int INDEX_9 = 2; // 数字9按键索引
    public static final int INDEX_p = 3; // p（皮）单位按键索引
    public static final int INDEX_k = 4; // k（千）单位按键索引
    public static final int INDEX_negative = 5; // 负号按键索引

    public static final int INDEX_4 = 6; // 数字4按键索引
    public static final int INDEX_5 = 7; // 数字5按键索引
    public static final int INDEX_6 = 8; // 数字6按键索引
    public static final int INDEX_n = 9; // n（纳）单位按键索引
    public static final int INDEX_M = 10; // M（兆）单位按键索引
    public static final int INDEX_positive = 11; // 正号按键索引

    public static final int INDEX_1 = 12; // 数字1按键索引
    public static final int INDEX_2 = 13; // 数字2按键索引
    public static final int INDEX_3 = 14; // 数字3按键索引
    public static final int INDEX_u = 15; // u（微）单位按键索引
    public static final int INDEX_G = 16; // G（吉）单位按键索引
    public static final int INDEX_del = 17; // 删除按键索引

    public static final int INDEX_0 = 18; // 数字0按键索引
    public static final int INDEX_point = 19; // 小数点按键索引
    public static final int INDEX_f = 20; // f（飞）单位按键索引
    public static final int INDEX_m = 21; // m（毫）单位按键索引
    public static final int INDEX_T = 22; // T（太）单位按键索引
    public static final int INDEX_enter = 23; // 确认按键索引
}
