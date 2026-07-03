package com.micsig.tbook.tbookscope.middleware; // 中间件层包声明

import com.micsig.tbook.tbookscope.middleware.mq.MQChanSelectorManage; // 导入MQ通道选择器管理类

import java.util.function.Consumer; // 导入Consumer函数式接口

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 11:21
 */
/*
 * +=============================================================================+
 * |                          MiddleMain - 中间件主入口                          |
 * +=============================================================================+
 * | 模块定位: 中间件层(Middleware)的核心单例入口类                                |
 * | 核心职责: 作为中间件层的全局访问点，持有并管理MiddleMainBean配置对象            |
 * | 架构设计: 采用饿汉式单例模式，确保全局只有一个MiddleMain实例                   |
 * | 数据流向: 外部调用者 → MiddleMain(单例) → MiddleMainBean → MQChanSelectorManage |
 * | 依赖关系: MiddleMainBean(数据持有), MQChanSelectorManage(MQ通道管理)           |
 * | 使用场景: 全局获取中间件配置、MQ通道选择器管理器等中间件层资源的统一入口         |
 * +=============================================================================+
 */
public class MiddleMain {
    private static final MiddleMain ourInstance = new MiddleMain(); // 饿汉式单例：类加载时即创建唯一实例

    /**
     * 获取MiddleMain的单例实例。
     *
     * @return MiddleMain的全局唯一实例
     */
    public static MiddleMain getIns() {
        return ourInstance; // 返回饿汉式单例实例
    }

    private MiddleMainBean bean=new MiddleMainBean(); // 持有中间件配置Bean对象，初始化时创建默认实例

    /**
     * 私有构造函数，防止外部通过new创建实例，保证单例模式。
     */
    private MiddleMain() {

    }

    private Consumer<Integer> a; // 预留的整数类型Consumer回调（暂未使用）

    /**
     * 获取MQ通道选择器管理器。
     *
     * @return MQChanSelectorManage MQ通道选择器管理器实例
     */
    public MQChanSelectorManage getChanSelectorManage(){

        return bean.getChanSelectorManage(); // 委托给MiddleMainBean获取通道选择器管理器
    }

    /**
     * 获取当前持有的MiddleMainBean配置对象。
     *
     * @return MiddleMainBean 中间件配置Bean对象
     */
    public MiddleMainBean getBean() {
        return bean; // 返回当前持有的配置Bean
    }

    /**
     * 设置MiddleMainBean配置对象，用于替换或更新中间件配置。
     *
     * @param bean 新的MiddleMainBean配置对象
     */
    public void setBean(MiddleMainBean bean) {
        this.bean = bean; // 将配置Bean引用指向新传入的对象
    }
}
