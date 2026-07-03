package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键通道消息实体类                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 通道选择消息封装                                     │
 * 【核心职责】                                                                  │
 *   1. 定义示波器所有可用通道类型常量（物理通道CH1-8、数学运算MATH1-8、参考波形REF1-8、存储S1-4）
 *   2. 封装当前选中的通道索引                                                      │
 *   3. 作为消息载体在按键事件与通道管理模块之间传递数据                              │
 * 【架构设计】                                                                  │
 *   常量定义类 + POJO类，采用静态常量定义通道类型，实例字段存储当前选中通道         │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → 通道控制层 → 通道切换执行                  │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、通道控制层                         │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键切换通道时，创建此消息对象并分发，携带目标通道索引           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExternalKeysMsgChannel {
    /** 物理通道1索引常量 */ // 定义物理通道1的索引值
    public static final int CH1 = 1; // 物理通道1索引常量

    /** 物理通道2索引常量 */ // 定义物理通道2的索引值
    public static final int CH2 = 2; // 物理通道2索引常量

    /** 物理通道3索引常量 */ // 定义物理通道3的索引值
    public static final int CH3 = 3; // 物理通道3索引常量

    /** 物理通道4索引常量 */ // 定义物理通道4的索引值
    public static final int CH4 = 4; // 物理通道4索引常量

    /** 物理通道5索引常量 */ // 定义物理通道5的索引值
    public static final int CH5 = 5; // 物理通道5索引常量

    /** 物理通道6索引常量 */ // 定义物理通道6的索引值
    public static final int CH6 = 6; // 物理通道6索引常量

    /** 物理通道7索引常量 */ // 定义物理通道7的索引值
    public static final int CH7 = 7; // 物理通道7索引常量

    /** 物理通道8索引常量 */ // 定义物理通道8的索引值
    public static final int CH8 = 8; // 物理通道8索引常量

    /** 数学运算通道1索引常量 */ // 定义数学运算通道1的索引值
    public static final int MATH1 = 9; // 数学运算通道1索引常量

    /** 数学运算通道2索引常量 */ // 定义数学运算通道2的索引值
    public static final int MATH2 = 10; // 数学运算通道2索引常量

    /** 数学运算通道3索引常量 */ // 定义数学运算通道3的索引值
    public static final int MATH3 = 11; // 数学运算通道3索引常量

    /** 数学运算通道4索引常量 */ // 定义数学运算通道4的索引值
    public static final int MATH4 = 12; // 数学运算通道4索引常量

    /** 数学运算通道5索引常量 */ // 定义数学运算通道5的索引值
    public static final int MATH5 = 13; // 数学运算通道5索引常量

    /** 数学运算通道6索引常量 */ // 定义数学运算通道6的索引值
    public static final int MATH6 = 14; // 数学运算通道6索引常量

    /** 数学运算通道7索引常量 */ // 定义数学运算通道7的索引值
    public static final int MATH7 = 15; // 数学运算通道7索引常量

    /** 数学运算通道8索引常量 */ // 定义数学运算通道8的索引值
    public static final int MATH8 = 16; // 数学运算通道8索引常量

    /** 参考波形通道1索引常量 */ // 定义参考波形通道1的索引值
    public static final int REF1 = 17; // 参考波形通道1索引常量

    /** 参考波形通道2索引常量 */ // 定义参考波形通道2的索引值
    public static final int REF2 = 18; // 参考波形通道2索引常量

    /** 参考波形通道3索引常量 */ // 定义参考波形通道3的索引值
    public static final int REF3 = 19; // 参考波形通道3索引常量

    /** 参考波形通道4索引常量 */ // 定义参考波形通道4的索引值
    public static final int REF4 = 20; // 参考波形通道4索引常量

    /** 参考波形通道5索引常量 */ // 定义参考波形通道5的索引值
    public static final int REF5 = 21; // 参考波形通道5索引常量

    /** 参考波形通道6索引常量 */ // 定义参考波形通道6的索引值
    public static final int REF6 = 22; // 参考波形通道6索引常量

    /** 参考波形通道7索引常量 */ // 定义参考波形通道7的索引值
    public static final int REF7 = 23; // 参考波形通道7索引常量

    /** 参考波形通道8索引常量 */ // 定义参考波形通道8的索引值
    public static final int REF8 = 24; // 参考波形通道8索引常量

    /** 存储通道S1索引常量 */ // 定义存储通道S1的索引值
    public static final int S1 = 25; // 存储通道S1索引常量

    /** 存储通道S2索引常量 */ // 定义存储通道S2的索引值
    public static final int S2 = 26; // 存储通道S2索引常量

    /** 存储通道S3索引常量 */ // 定义存储通道S3的索引值
    public static final int S3 = 27; // 存储通道S3索引常量

    /** 存储通道S4索引常量 */ // 定义存储通道S4的索引值
    public static final int S4 = 28; // 存储通道S4索引常量

    /** 当前选中的通道索引 */ // 当前选中通道的索引值
    private int chIndex; // 通道索引字段

    /**
     * 构造函数：创建外部按键通道消息
     *
     * @param chIndex 选中的通道索引（使用CH1-8、MATH1-8、REF1-8、S1-4常量）
     */
    public ExternalKeysMsgChannel(int chIndex) {
        this.chIndex = chIndex; // 初始化通道索引
    }

    /**
     * 获取当前选中的通道索引
     *
     * @return 通道索引值（对应CH1-8、MATH1-8、REF1-8、S1-4常量）
     */
    public int getChIndex() {
        return chIndex; // 返回通道索引
    }

    /**
     * 设置当前选中的通道索引
     *
     * @param chIndex 通道索引值（使用CH1-8、MATH1-8、REF1-8、S1-4常量）
     */
    public void setChIndex(int chIndex) {
        this.chIndex = chIndex; // 更新通道索引
    }

    /**
     * 获取消息对象的字符串表示
     *
     * @return 包含通道索引的字符串格式
     */
    @Override
    public String toString() {
        return "ExternalKeysMsgChannel{" + // 返回类名前缀
                "chIndex=" + chIndex + // 拼接通道索引值
                '}'; // 添加闭合括号
    }
}