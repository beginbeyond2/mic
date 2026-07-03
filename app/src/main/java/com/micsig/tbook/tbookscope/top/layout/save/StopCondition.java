package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - 自动保存停止条件                             ║
 * ║  核心职责: 定义自动保存任务的停止条件（按时间/按帧数/无限制）            ║
 * ║  架构设计: 值对象模式 + Builder构建器模式，不可变对象                   ║
 * ║  数据流向: AutoSaveTaskCondition → StopCondition → AutoSaveTaskManager║
 * ║  依赖关系: 被 AutoSaveTaskCondition 持有和使用                         ║
 * ║  使用场景: 配置自动保存任务的终止条件，如指定时间点或保存帧数后停止       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * 自动保存停止条件类
 * <p>封装自动保存任务的停止条件，支持三种类型：无限制、按时间、按帧数</p>
 */
public class StopCondition { // 停止条件封装类

    /**
     * 停止条件类型枚举
     * <p>定义自动保存任务的三种停止条件类型</p>
     */
    enum StopConditionType { // 停止条件类型枚举
        NONE(0), TIME(1), AFTER_N_FRAME(2); // 无限制(0)、按时间(1)、按帧数(2)

        /** 类型编码 */
        private final int code; // 类型编码字段

        /**
         * 枚举构造方法
         * @param code 类型编码值
         */
        StopConditionType(int code) { // 枚举构造方法
            this.code = code; // 赋值类型编码
        }

        /**
         * 根据编码值获取对应的停止条件类型
         * @param code 类型编码值
         * @return 对应的StopConditionType，未匹配则返回null
         */
        public static StopConditionType fromCode(int code) { // 根据编码获取枚举实例
            for (StopConditionType stopConditionType : values()) { // 遍历所有枚举值
                if (stopConditionType.code == code) { // 比较编码值
                    return stopConditionType; // 返回匹配的枚举实例
                }
            }
            return null; // 未找到匹配则返回null
        }
    }

    /** 停止条件类型 */
    private StopConditionType type; // 停止条件类型

    /** 停止条件值（时间字符串或帧数字符串） */
    private String value; // 停止条件值

    /**
     * 停止条件构造方法
     * @param type 停止条件类型
     * @param value 停止条件值
     */
    StopCondition(StopConditionType type, String value) { // 构造方法
        this.type = type; // 赋值类型
        this.value = value; // 赋值条件值
    }


    /**
     * 停止条件Builder，用于构建StopCondition实例
     */
    public static final class StopConditionBuilder{ // 停止条件构建器
        /** 停止条件类型 */
        private StopConditionType type; // 停止条件类型

        /** 停止条件值 */
        private String value; // 停止条件值


        /**
         * 构建StopCondition实例
         * @return 构建完成的StopCondition对象
         */
        public StopCondition build(){ // 构建StopCondition实例
            return new StopCondition(type,value); // 使用type和value创建实例
        }

        /**
         * 设置停止条件类型
         * @param terminateCondition 停止条件类型
         * @return Builder自身，支持链式调用
         */
        public StopConditionBuilder setType(StopConditionType terminateCondition){ // 设置停止条件类型
            this.type = terminateCondition; // 赋值类型
            return this; // 返回Builder自身
        }

        /**
         * 设置停止条件值
         * @param value 停止条件值
         * @return Builder自身，支持链式调用
         */
        public StopConditionBuilder setValue(String value){ // 设置停止条件值
            this.value = value; // 赋值条件值
            return this; // 返回Builder自身
        }


    }

    /**
     * 获取停止条件类型
     * @return 停止条件类型枚举
     */
    public StopConditionType getType() { // 获取停止条件类型
        return type; // 返回类型
    }

    /**
     * 获取停止条件值
     * @return 停止条件值字符串
     */
    public String getValue() { // 获取停止条件值
        return value; // 返回条件值
    }
}
