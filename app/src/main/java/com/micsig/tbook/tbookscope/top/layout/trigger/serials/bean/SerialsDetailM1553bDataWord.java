package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailM1553bDataWord（MIL-STD-1553B数据字触发详情）          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailM1553bDataWord.java                     ║
 * ║ 核心职责: 封装MIL-STD-1553B数据字触发条件的详情数据                             ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: 1553B总线数据字(Data Word)触发时传递字值                             ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailM1553bDataWord implements ISerialsDetail { // MIL-STD-1553B数据字触发详情，实现ISerialsDetail
    private DataBean m1553bDataWordData; // 1553B数据字值
    private String m1553bDataWordDataTitle; // 数据字字段标题

    /**
     * 获取数据字字段标题
     * @return 标题字符串
     */
    public String getM1553bDataWordDataTitle() { // 获取数据字标题
        return m1553bDataWordDataTitle; // 返回标题
    }

    /**
     * 设置数据字字段标题
     * @param m1553bDataWordDataTitle 标题字符串
     */
    public void setM1553bDataWordDataTitle(String m1553bDataWordDataTitle) { // 设置数据字标题
        this.m1553bDataWordDataTitle = m1553bDataWordDataTitle; // 赋值标题
    }

    /**
     * 获取1553B数据字值
     * @return DataBean 数据字对象
     */
    public DataBean getM1553bDataWordData() { // 获取数据字值
        return m1553bDataWordData; // 返回DataBean
    }

    /**
     * 设置1553B数据字值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setM1553bDataWordData(int digits, String value) { // 设置数据字值
        if (this.m1553bDataWordData == null) { // 如果DataBean为空
            this.m1553bDataWordData = new DataBean(); // 懒初始化创建DataBean
        }
        this.m1553bDataWordData.setDigits(digits); // 设置进制
        this.m1553bDataWordData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailM1553bDataWord{" + // 开始构建字符串
                "m1553bDataWordData='" + m1553bDataWordData + '\'' + // 拼接数据字字段
                '}'; // 结束字符串构建
    }
}
