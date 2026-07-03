package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║          SerialsDetailCanRemoteId（CAN远程帧ID触发详情）                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailCanRemoteId.java                       ║
 * ║ 核心职责: 封装CAN远程帧ID触发条件的详情数据                                      ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: CAN远程帧ID触发时传递ID值                                           ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailCanRemoteId implements ISerialsDetail { // CAN远程帧ID触发详情，实现ISerialsDetail
    private DataBean canRemoteIdEdit; // CAN远程帧ID值
    private String canRemoteIdEditTitle; // ID字段标题

    /**
     * 获取ID字段标题
     * @return 标题字符串
     */
    public String getCanRemoteIdEditTitle() { // 获取ID标题
        return canRemoteIdEditTitle; // 返回标题
    }

    /**
     * 设置ID字段标题
     * @param canRemoteIdEditTitle 标题字符串
     */
    public void setCanRemoteIdEditTitle(String canRemoteIdEditTitle) { // 设置ID标题
        this.canRemoteIdEditTitle = canRemoteIdEditTitle; // 赋值标题
    }

    /**
     * 获取CAN远程帧ID值
     * @return DataBean ID对象
     */
    public DataBean getCanRemoteIdEdit() { // 获取ID值
        return canRemoteIdEdit; // 返回DataBean
    }

    /**
     * 设置CAN远程帧ID值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setCanRemoteIdEdit(int digits, String value) { // 设置ID值
        if (this.canRemoteIdEdit == null) { // 如果DataBean为空
            this.canRemoteIdEdit = new DataBean(); // 懒初始化创建DataBean
        }
        this.canRemoteIdEdit.setDigits(digits); // 设置进制
        this.canRemoteIdEdit.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailCanRemoteId{" + // 开始构建字符串
                "canRemoteIdEdit='" + canRemoteIdEdit + '\'' + // 拼接ID字段
                '}'; // 结束字符串构建
    }
}
