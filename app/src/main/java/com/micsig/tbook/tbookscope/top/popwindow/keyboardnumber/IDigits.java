package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber; // 包声明：数字键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：数字键盘 - 进制/输入类型常量接口                          │
 * │ 核心职责：定义数字键盘支持的进制和输入类型常量                       │
 * │ 架构设计：纯常量接口，由TopDialogNumberKeyBoard和KeyBoardNumberUtil │
 * │          实现使用                                                  │
 * │ 数据流向：无数据流，仅提供进制常量                                  │
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：setDecimalData时指定输入类型，控制可用按键和格式化规则     │
 * └──────────────────────────────────────────────────────────────────┘
 */
public interface IDigits { // 进制/输入类型常量接口
    public static final int DIGITS_BAUDRATE = 1;//输入类型是波特率类 // 波特率输入类型
    public static final int DIGITS_FLOAT = 20;//输入类型是float类 // 浮点数输入类型
    public static final int DIGITS_10 = 10;//输入是10进制 // 十进制输入类型
    public static final int DIGITS_16 = 16;//输入是16进制 // 十六进制输入类型
    public static final int DIGITS_16X = 17;//输入是16进制和x // 十六进制带0x前缀输入类型
    public static final int DIGITS_8 = 8;//输入是8进制 // 八进制输入类型
    public static final int DIGITS_4 = 4;//输入是4进制 // 四进制输入类型
    public static final int DIGITS_2 = 2;//输入是2进制 // 二进制输入类型
    public static final int DIGITS_2X = 3;//输入是2进制和x // 二进制带0x前缀输入类型
    public static final int DIGITS_0_8 = 9;//输入是0到9 // 0-9数字输入类型
}
