package com.micsig.tbook.tbookscope.main;

/**
 * 外部按键发送触发电平、阈值电平数值改变时的消息实体类
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键电平消息实体类                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 触发电平/阈值电平调节消息封装                          │
 * 【核心职责】                                                                  │
 *   1. 定义电平操作类型常量（触发移动、触发源切换、数值移动、数值源切换）            │
 *   2. 封装电平调节相关参数（电平类型、调节步数）                                   │
 *   3. 作为消息载体在按键事件与电平控制模块之间传递数据                             │
 * 【架构设计】                                                                  │
 *   常量定义类 + POJO类，采用静态常量定义操作类型，实例字段存储电平调节参数         │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → 电平控制层 → 电平调节执行                  │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、电平控制层                         │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键调节触发电平或阈值电平时，创建此消息对象并分发                 │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExternalKeysMsgLevel {
    /** 电平操作类型：触发电平向上移动 */ // 定义触发电平上移常量
    public static final int TYPE_TRIGGER_MOVEUP = 1; // 触发电平向上移动

    /** 电平操作类型：触发电平向下移动 */ // 定义触发电平下移常量
    public static final int TYPE_TRIGGER_MOVEDOMN = 2; // 触发电平向下移动

    /** 电平操作类型：触发电平移动到中心 */ // 定义触发电平居中常量
    public static final int TYPE_TRIGGER_MOVECENTER = 3; // 触发电平移动到中心

    /** 电平操作类型：触发源向上切换 */ // 定义触发源上切常量
    public static final int TYPE_TRIGGER_SOURCEUP = 4; // 触发源向上切换

    /** 电平操作类型：触发源向下切换 */ // 定义触发源下切常量
    public static final int TYPE_TRIGGER_SOURCEDOWN = 5; // 触发源向下切换

    /** 电平操作类型：数值向上移动 */ // 定义数值上移常量
    public static final int TYPE_VALUE_MOVEUP = 6; // 数值向上移动

    /** 电平操作类型：数值向下移动 */ // 定义数值下移常量
    public static final int TYPE_VALUE_MOVEDOMN = 7; // 数值向下移动

    /** 电平操作类型：数值移动到中心 */ // 定义数值居中常量
    public static final int TYPE_VALUE_MOVECENTER = 8; // 数值移动到中心

    /** 电平操作类型：数值源向上切换 */ // 定义数值源上切常量
    public static final int TYPE_VALUE_SOURCEUP = 9; // 数值源向上切换

    /** 电平操作类型：数值源向下切换 */ // 定义数值源下切常量
    public static final int TYPE_VALUE_SOURCEDOWN = 10; // 数值源向下切换

    /** 电平操作类型（TYPE_TRIGGER_*或TYPE_VALUE_*） */ // 电平操作类型字段
    private int levelType; // 电平操作类型

    /** 调节步数/计数 */ // 调节步数字段
    private int count; // 调节步数计数

    /**
     * 构造函数：创建外部按键电平消息
     *
     * @param levelType 电平操作类型（使用TYPE_TRIGGER_*或TYPE_VALUE_*常量）
     * @param count     调节步数（单次调节的步进数量）
     */
    public ExternalKeysMsgLevel(int levelType, int count) {
        this.levelType = levelType; // 初始化电平操作类型
        this.count = count; // 初始化调节步数
    }

    /**
     * 获取电平操作类型
     *
     * @return 电平操作类型（TYPE_TRIGGER_*或TYPE_VALUE_*常量）
     */
    public int getLevelType() {
        return levelType; // 返回电平操作类型
    }

    /**
     * 设置电平操作类型
     *
     * @param levelType 电平操作类型（使用TYPE_TRIGGER_*或TYPE_VALUE_*常量）
     */
    public void setLevelType(int levelType) {
        this.levelType = levelType; // 更新电平操作类型
    }

    /**
     * 获取调节步数
     *
     * @return 调节步数（单次调节的步进数量）
     */
    public int getCount() {
        return count; // 返回调节步数
    }

    /**
     * 设置调节步数
     *
     * @param count 调节步数（单次调节的步进数量）
     */
    public void setCount(int count) {
        this.count = count; // 更新调节步数
    }
}