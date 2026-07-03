package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - TXT Mix显示详情数据模型                         ║
 * ║  核心职责: 封装Display菜单下"TXT Mix"子页面的串口文本组合选择状态              ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayTxtMix → TopMsgDisplayTxtMix → TopMsgDisplay ║
 * ║  依赖关系: 无外部依赖，仅使用基本类型                                       ║
 * ║  使用场景: 用户在Display-TXT Mix页面选择S1~S4串口文本组合时携带数据           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by limh on 2024/8/8.
 */

public class TopMsgDisplayTxtMix implements IDisplayDetail { // TXT Mix显示详情数据模型，实现IDisplayDetail接口

    private boolean s1Select, s2Select, s3Select, s4Select; // S1~S4四个串口通道的文本组合选择状态

    /**
     * 判断S1串口是否被选中
     * @return true表示选中，false表示未选中
     */
    public boolean isS1Select() { // 获取S1选择状态
        return s1Select; // 返回S1选中状态
    }

    /**
     * 设置S1串口的选择状态
     * @param s1Select true表示选中，false表示未选中
     */
    public void setS1Select(boolean s1Select) { // 设置S1选择状态
        this.s1Select = s1Select; // 赋值S1选中状态
    }

    /**
     * 判断S2串口是否被选中
     * @return true表示选中，false表示未选中
     */
    public boolean isS2Select() { // 获取S2选择状态
        return s2Select; // 返回S2选中状态
    }

    /**
     * 设置S2串口的选择状态
     * @param s2Select true表示选中，false表示未选中
     */
    public void setS2Select(boolean s2Select) { // 设置S2选择状态
        this.s2Select = s2Select; // 赋值S2选中状态
    }

    /**
     * 判断S3串口是否被选中
     * @return true表示选中，false表示未选中
     */
    public boolean isS3Select() { // 获取S3选择状态
        return s3Select; // 返回S3选中状态
    }

    /**
     * 设置S3串口的选择状态
     * @param s3Select true表示选中，false表示未选中
     */
    public void setS3Select(boolean s3Select) { // 设置S3选择状态
        this.s3Select = s3Select; // 赋值S3选中状态
    }

    /**
     * 判断S4串口是否被选中
     * @return true表示选中，false表示未选中
     */
    public boolean isS4Select() { // 获取S4选择状态
        return s4Select; // 返回S4选中状态
    }

    /**
     * 设置S4串口的选择状态
     * @param s4Select true表示选中，false表示未选中
     */
    public void setS4Select(boolean s4Select) { // 设置S4选择状态
        this.s4Select = s4Select; // 赋值S4选中状态
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含S1~S4选择状态的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayTxtMix{" + // 类名前缀
                "s1Select=" + s1Select + // S1选择状态
                ", s2Select=" + s2Select + // S2选择状态
                ", s3Select=" + s3Select + // S3选择状态
                ", s4Select=" + s4Select + // S4选择状态
                '}'; // 类名后缀
    }
}
