package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber; // 包声明：数字键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：数字键盘 - 按键索引常量接口                              │
 * │ 核心职责：定义数字键盘中各按键的位置索引常量                         │
 * │ 架构设计：纯常量接口，由TopDialogNumberKeyBoard和KeyBoardNumberUtil │
 * │          实现使用                                                  │
 * │ 数据流向：无数据流，仅提供索引常量                                  │
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：TopDialogNumberKeyBoard中通过索引访问键盘按钮             │
 * └──────────────────────────────────────────────────────────────────┘
 */
public interface IKeyBoardNumber { // 数字键盘按键索引常量接口
    public static final int INDEX_7 = 0; // 数字7按键索引
    public static final int INDEX_8 = 1; // 数字8按键索引
    public static final int INDEX_9 = 2; // 数字9按键索引
    public static final int INDEX_BS = 3; // b/s单位按键索引
    public static final int INDEX_KBS = 4; // kb/s单位按键索引
    public static final int INDEX_MBS = 5; // Mb/s单位按键索引
    public static final int INDEX_4 = 6; // 数字4按键索引
    public static final int INDEX_5 = 7; // 数字5按键索引
    public static final int INDEX_6 = 8; // 数字6按键索引
    public static final int INDEX_A = 9; // 十六进制A按键索引
    public static final int INDEX_B = 10; // 十六进制B按键索引
    public static final int INDEX_ZUO = 11; // 删除（左箭头）按键索引
    public static final int INDEX_1 = 12; // 数字1按键索引
    public static final int INDEX_2 = 13; // 数字2按键索引
    public static final int INDEX_3 = 14; // 数字3按键索引
    public static final int INDEX_C = 15; // 十六进制C按键索引
    public static final int INDEX_D = 16; // 十六进制D按键索引
    public static final int INDEX_POINT = 17; // 小数点按键索引
    public static final int INDEX_0 = 18; // 数字0按键索引
    public static final int INDEX_X = 19; // 十六进制x前缀按键索引
    public static final int INDEX_E = 20; // 十六进制E按键索引
    public static final int INDEX_F = 21; // 十六进制F按键索引
    public static final int INDEX_ENTER = 22; // 确认按键索引

}
