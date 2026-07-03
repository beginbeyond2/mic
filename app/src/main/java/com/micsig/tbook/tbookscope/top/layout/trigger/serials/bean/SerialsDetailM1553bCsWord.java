package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailM1553bCsWord（MIL-STD-1553B指令/状态字触发详情）        ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailM1553bCsWord.java                       ║
 * ║ 核心职责: 封装MIL-STD-1553B指令/状态字触发条件的详情数据                         ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: 1553B总线指令字(Command Word)或状态字(Status Word)触发时传递字值       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailM1553bCsWord implements ISerialsDetail { // MIL-STD-1553B指令/状态字触发详情，实现ISerialsDetail
    private DataBean m1553bCsWordCsWord; // 1553B指令/状态字值
    private String m1553bCsWordCsWordTitle; // 指令/状态字字段标题

    /**
     * 获取指令/状态字字段标题
     * @return 标题字符串
     */
    public String getM1553bCsWordCsWordTitle() { // 获取指令/状态字标题
        return m1553bCsWordCsWordTitle; // 返回标题
    }

    /**
     * 设置指令/状态字字段标题
     * @param m1553bCsWordCsWordTitle 标题字符串
     */
    public void setM1553bCsWordCsWordTitle(String m1553bCsWordCsWordTitle) { // 设置指令/状态字标题
        this.m1553bCsWordCsWordTitle = m1553bCsWordCsWordTitle; // 赋值标题
    }

    /**
     * 获取1553B指令/状态字值
     * @return DataBean 指令/状态字对象
     */
    public DataBean getM1553bCsWordCsWord() { // 获取指令/状态字值
        return m1553bCsWordCsWord; // 返回DataBean
    }

    /**
     * 设置1553B指令/状态字值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setM1553bCsWordCsWord(int digits, String value) { // 设置指令/状态字值
        if (this.m1553bCsWordCsWord == null) { // 如果DataBean为空
            this.m1553bCsWordCsWord = new DataBean(); // 懒初始化创建DataBean
        }
        this.m1553bCsWordCsWord.setDigits(digits); // 设置进制
        this.m1553bCsWordCsWord.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailM1553bCsWord{" + // 开始构建字符串
                "m1553bCsWordCsWord='" + m1553bCsWordCsWord + '\'' + // 拼接指令/状态字字段
                '}'; // 结束字符串构建
    }
}
