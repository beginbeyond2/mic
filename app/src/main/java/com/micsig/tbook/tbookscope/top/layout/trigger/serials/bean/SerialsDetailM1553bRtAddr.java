package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailM1553bRtAddr（MIL-STD-1553B远程终端地址触发详情）       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailM1553bRtAddr.java                       ║
 * ║ 核心职责: 封装MIL-STD-1553B远程终端地址触发条件的详情数据                        ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有1个DataBean字段                       ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: 1553B总线远程终端地址(RT Address)触发时传递地址值                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailM1553bRtAddr implements ISerialsDetail { // MIL-STD-1553B远程终端地址触发详情，实现ISerialsDetail
    private DataBean m1553bRtAddrRtAddr; // 1553B远程终端地址值
    private String m1553bRtAddrRtAddrTitle; // 远程终端地址字段标题

    /**
     * 获取远程终端地址字段标题
     * @return 标题字符串
     */
    public String getM1553bRtAddrRtAddrTitle() { // 获取远程终端地址标题
        return m1553bRtAddrRtAddrTitle; // 返回标题
    }

    /**
     * 设置远程终端地址字段标题
     * @param m1553bRtAddrRtAddrTitle 标题字符串
     */
    public void setM1553bRtAddrRtAddrTitle(String m1553bRtAddrRtAddrTitle) { // 设置远程终端地址标题
        this.m1553bRtAddrRtAddrTitle = m1553bRtAddrRtAddrTitle; // 赋值标题
    }

    /**
     * 获取1553B远程终端地址值
     * @return DataBean 远程终端地址对象
     */
    public DataBean getM1553bRtAddrRtAddr() { // 获取远程终端地址值
        return m1553bRtAddrRtAddr; // 返回DataBean
    }

    /**
     * 设置1553B远程终端地址值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setM1553bRtAddrRtAddr(int digits, String value) { // 设置远程终端地址值
        if (this.m1553bRtAddrRtAddr == null) { // 如果DataBean为空
            this.m1553bRtAddrRtAddr = new DataBean(); // 懒初始化创建DataBean
        }
        this.m1553bRtAddrRtAddr.setDigits(digits); // 设置进制
        this.m1553bRtAddrRtAddr.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailM1553bRtAddr{" + // 开始构建字符串
                "m1553bRtAddrRtAddr='" + m1553bRtAddrRtAddr + '\'' + // 拼接远程终端地址字段
                '}'; // 结束字符串构建
    }
}
