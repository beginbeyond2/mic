package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailLinFrameId（LIN帧ID触发详情）                        ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailLinFrameId.java                         ║
 * ║ 核心职责: 封装LIN帧ID触发条件的详情数据                                        ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: LIN总线帧ID触发时传递标识符值                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailLinFrameId implements ISerialsDetail { // LIN帧ID触发详情，实现ISerialsDetail
    private DataBean linFrameIdEditEdit; // LIN帧ID编辑值
    private String linFrameIdEditEditTitle; // 帧ID字段标题

    /**
     * 获取帧ID字段标题
     * @return 标题字符串
     */
    public String getLinFrameIdEditEditTitle() { // 获取帧ID标题
        return linFrameIdEditEditTitle; // 返回标题
    }

    /**
     * 设置帧ID字段标题
     * @param linFrameIdEditEditTitle 标题字符串
     */
    public void setLinFrameIdEditEditTitle(String linFrameIdEditEditTitle) { // 设置帧ID标题
        this.linFrameIdEditEditTitle = linFrameIdEditEditTitle; // 赋值标题
    }

    /**
     * 获取LIN帧ID编辑值
     * @return DataBean 帧ID对象
     */
    public DataBean getLinFrameIdEditEdit() { // 获取帧ID值
        return linFrameIdEditEdit; // 返回DataBean
    }

    /**
     * 设置LIN帧ID编辑值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setLinFrameIdEditEdit(int digits, String value) { // 设置帧ID值
        if (this.linFrameIdEditEdit == null) { // 如果DataBean为空
            this.linFrameIdEditEdit = new DataBean(); // 懒初始化创建DataBean
        }
        this.linFrameIdEditEdit.setDigits(digits); // 设置进制
        this.linFrameIdEditEdit.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailLinFrameId{" + // 开始构建字符串
                "linFrameIdEditEdit='" + linFrameIdEditEdit + '\'' + // 拼接帧ID字段
                '}'; // 结束字符串构建
    }
}
