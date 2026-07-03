package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI频率计命令处理

/**
 * Created by yangj on 2018/1/19.
 * 频率计
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                            Command_Frequency                                 |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 频率计(Frequency)命令处理层                      |
 * | 核心职责：管理频率计的通道源设置与查询                                       |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          仅维护一个index成员变量表示频率计通道源                             |
 * | 数据流向：SCPI指令 → Command_Frequency → index(状态存储)                    |
 * | 依赖关系：Command(单例入口)                                                 |
 * | 使用场景：远程SCPI控制中设置/查询频率计通道源                               |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Frequency {
    private int index; // 频率计通道源索引

    /**
     * 设置频率计通道源
     * @param index 通道源索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void setFrequency(int index, boolean isUpdateUI) {
        if (this.index == index) return; // 通道源未变则直接返回
        this.index = index; // 更新频率计通道源
        if (isUpdateUI) { // 需要通知UI更新
        }
    }

    /**
     * 查询频率计通道源
     * @return 通道源索引
     */
    public int getFrequency() {
        return index; // 返回频率计通道源索引
    }
}
