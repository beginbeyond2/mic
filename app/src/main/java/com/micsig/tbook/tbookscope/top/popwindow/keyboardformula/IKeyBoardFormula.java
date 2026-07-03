package com.micsig.tbook.tbookscope.top.popwindow.keyboardformula; // 公式键盘接口所在包

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          IKeyBoardFormula                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：公式键盘按键索引常量接口，定义66个按键的位置索引                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                 │
 * │   统一定义公式键盘中所有按键的索引常量，供KeyBoardFormulaUtil和               │
 * │   TopDialogFormulaKeyBoard引用，实现按键索引与布局位置的映射                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                 │
 * │   纯常量接口，无方法定义，仅包含public static final int常量                  │
 * │   被KeyBoardFormulaUtil和TopDialogFormulaKeyBoard实现（implements）          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流：                                                                   │
 * │   布局XML中按钮tag → 索引常量 → 工具类/对话框中的按键识别和可见性判断        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖：无外部依赖                                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：公式键盘中识别按键类型、控制按键可见性、处理按键点击事件             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public interface IKeyBoardFormula { // 公式键盘按键索引常量接口
    public static final int INDEX_null = -1; // 无效索引，表示空或初始状态

    public static final int INDEX_add = 0; // 加号键索引
    public static final int INDEX_sub = 1; // 减号键索引
    public static final int INDEX_mul = 2; // 乘号键索引
    public static final int INDEX_div = 3; // 除号键索引
    public static final int INDEX_bracket_left = 4; // 左括号键索引

    public static final int INDEX_less = 5; // 小于号键索引
    public static final int INDEX_greater = 6; // 大于号键索引
    public static final int INDEX_less_equal = 7; // 小于等于号键索引
    public static final int INDEX_greater_equal = 8; // 大于等于号键索引
    public static final int INDEX_bracket_right = 9; // 右括号键索引

    public static final int INDEX_equal = 10; // 等于号键索引
    public static final int INDEX_not_equal = 11; // 不等于号键索引
    public static final int INDEX_and = 12; // 逻辑与键索引
    public static final int INDEX_or = 13; // 逻辑或键索引
    public static final int INDEX_no = 14; // 逻辑非键索引

    public static final int INDEX_sqrt = 15; // 平方根键索引
    public static final int INDEX_abs = 16; // 绝对值键索引
    public static final int INDEX_deg = 17; // 角度转弧度键索引
    public static final int INDEX_rad = 18; // 弧度转角度键索引
    public static final int INDEX_exp = 19; // 指数函数键索引

    public static final int INDEX_diff = 20; // 微分键索引
    public static final int INDEX_ln = 21; // 自然对数键索引
    public static final int INDEX_sine = 22; // 正弦函数键索引
    public static final int INDEX_cos = 23; // 余弦函数键索引
    public static final int INDEX_tan = 24; // 正切函数键索引

    public static final int INDEX_intg = 25; // 积分键索引
    public static final int INDEX_lg = 26; // 常用对数键索引
    public static final int INDEX_arcsin = 27; // 反正弦键索引
    public static final int INDEX_arccos = 28; // 反余弦键索引
    public static final int INDEX_arctan = 29; // 反正切键索引

    public static final int INDEX_ch1 = 30; // 通道1键索引
    public static final int INDEX_ch2 = 31; // 通道2键索引
    public static final int INDEX_ch3 = 32; // 通道3键索引
    public static final int INDEX_ch4 = 33; // 通道4键索引
    public static final int INDEX_null1 = 34; // 占位键索引（无功能）
    public static final int INDEX_pi = 35; // 圆周率π键索引

    public static final int INDEX_ch5 = 36; // 通道5键索引
    public static final int INDEX_ch6 = 37; // 通道6键索引
    public static final int INDEX_ch7 = 38; // 通道7键索引
    public static final int INDEX_ch8 = 39; // 通道8键索引
    public static final int INDEX_time_base = 40; // 时基键索引
    public static final int INDEX_E = 41; // 科学计数法E键索引

    public static final int INDEX_7 = 42; // 数字7键索引
    public static final int INDEX_8 = 43; // 数字8键索引
    public static final int INDEX_9 = 44; // 数字9键索引
    public static final int INDEX_m = 45; // 毫(m)单位键索引
    public static final int INDEX_T = 46; // 太(T)单位键索引
    public static final int INDEX_var1 = 47; // 变量1键索引

    public static final int INDEX_4 = 48; // 数字4键索引
    public static final int INDEX_5 = 49; // 数字5键索引
    public static final int INDEX_6 = 50; // 数字6键索引
    public static final int INDEX_u = 51; // 微(μ)单位键索引
    public static final int INDEX_G = 52; // 吉(G)单位键索引
    public static final int INDEX_var2 = 53; // 变量2键索引

    public static final int INDEX_1 = 54; // 数字1键索引
    public static final int INDEX_2 = 55; // 数字2键索引
    public static final int INDEX_3 = 56; // 数字3键索引
    public static final int INDEX_n = 57; // 纳(n)单位键索引
    public static final int INDEX_M = 58; // 兆(M)单位键索引
    public static final int INDEX_enter = 59; // 确认键索引

    public static final int INDEX_point = 60; // 小数点键索引
    public static final int INDEX_0 = 61; // 数字0键索引
    public static final int INDEX_f = 62; // 飞(f)单位键索引
    public static final int INDEX_p = 63; // 皮(p)单位键索引
    public static final int INDEX_k = 64; // 千(k)单位键索引
    public static final int INDEX_del = 65; // 删除键索引


}
