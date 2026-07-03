package com.micsig.tbook.tbookscope.middleware; // 中间件层包声明

import com.micsig.tbook.tbookscope.middleware.mq.MQChanSelectorManage; // 导入MQ通道选择器管理类

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 11:28
 */
/*
 * +=============================================================================+
 * |                       MiddleMainBean - 中间件配置Bean                       |
 * +=============================================================================+
 * | 模块定位: 中间件层(Middleware)的配置数据持有类                                |
 * | 核心职责: 封装中间件运行所需的配置数据，主要持有MQ通道选择器管理器              |
 * | 架构设计: 采用JavaBean模式，提供getter/setter供外部读写配置                   |
 * | 数据流向: MiddleMain(单例) → MiddleMainBean → MQChanSelectorManage          |
 * | 依赖关系: MQChanSelectorManage(MQ通道选择器管理)                              |
 * | 使用场景: 由MiddleMain持有，作为中间件层配置的集中管理对象                     |
 * +=============================================================================+
 */
public class MiddleMainBean {
    private MQChanSelectorManage chanSelectorManage=new MQChanSelectorManage(); // MQ通道选择器管理器，初始化时创建默认实例

    /**
     * 默认构造函数，创建MiddleMainBean实例。
     */
    public MiddleMainBean(){}

    /**
     * 获取MQ通道选择器管理器。
     *
     * @return MQChanSelectorManage 当前持有的MQ通道选择器管理器实例
     */
    public MQChanSelectorManage getChanSelectorManage() {
        return chanSelectorManage; // 返回MQ通道选择器管理器实例
    }

    /**
     * 设置MQ通道选择器管理器，用于替换或更新通道选择策略。
     *
     * @param chanSelectorManage 新的MQ通道选择器管理器实例
     */
    public void setChanSelectorManage(MQChanSelectorManage chanSelectorManage) {
        this.chanSelectorManage = chanSelectorManage; // 将通道选择器管理器引用指向新传入的对象
    }

}
