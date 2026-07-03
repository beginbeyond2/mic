package com.micsig.tbook.tbookscope.main;

/**
 * Created by yangj on 2018/8/15.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      外部按键光标消息实体类                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * 【模块定位】                                                                  │
 *   示波器外部按键消息系统 - 光标控制消息封装                                     │
 * 【核心职责】                                                                  │
 *   1. 定义光标操作类型常量（改变、打开、关闭）                                    │
 *   2. 封装光标相关参数（水平/垂直方向、操作类型）                                 │
 *   3. 作为消息载体在按键事件与光标控制模块之间传递数据                             │
 * 【架构设计】                                                                  │
 *   常量定义类 + POJO类，采用静态常量定义操作类型，实例字段存储光标状态             │
 * 【数据流向】                                                                  │
 *   外部按键事件 → 消息封装 → 事件总线 → 光标控制层 → 光标操作执行                  │
 * 【依赖关系】                                                                  │
 *   被依赖：ExternalKeysMsg相关处理类、事件总线、光标控制层                         │
 * 【使用场景】                                                                  │
 *   当用户通过外部按键控制光标时，创建此消息对象并分发，携带光标方向和操作类型       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ExternalKeysMsgCursor {
    /** 光标操作类型：改变一次 */ // 定义光标改变操作类型常量
    public static final int TYPE_CHANGE = 0;//改变一次 // 光标改变一次操作常量

    /** 光标操作类型：状态改为打开 */ // 定义光标打开操作类型常量
    public static final int TYPE_OPEN = 1;//状态改为打开 // 光标打开操作常量

    /** 光标操作类型：状态改为关闭 */ // 定义光标关闭操作类型常量
    public static final int TYPE_CLOSE = 2;//状态改为关闭 // 光标关闭操作常量

    /** 是否为水平光标（true=水平光标, false=垂直光标） */ // 光标方向标识
    private boolean isHor;//是水平光标么 // 是否为水平光标

    /** 光标操作类型（TYPE_CHANGE/TYPE_OPEN/TYPE_CLOSE） */ // 光标操作类型
    private int type; // 光标操作类型字段

    /**
     * 构造函数：创建外部按键光标消息
     *
     * @param isHor 是否为水平光标（true=水平光标, false=垂直光标）
     * @param type  光标操作类型（TYPE_CHANGE=改变一次, TYPE_OPEN=打开, TYPE_CLOSE=关闭）
     */
    public ExternalKeysMsgCursor(boolean isHor, int type) {
        this.isHor = isHor; // 初始化光标方向标识
        this.type = type; // 初始化光标操作类型
    }

    /**
     * 获取是否为水平光标
     *
     * @return true表示水平光标，false表示垂直光标
     */
    public boolean isHor() {
        return isHor; // 返回光标方向标识
    }

    /**
     * 设置是否为水平光标
     *
     * @param hor true表示水平光标，false表示垂直光标
     */
    public void setHor(boolean hor) {
        isHor = hor; // 更新光标方向标识
    }

    /**
     * 获取光标操作类型
     *
     * @return 光标操作类型（TYPE_CHANGE/TYPE_OPEN/TYPE_CLOSE）
     */
    public int getType() {
        return type; // 返回光标操作类型
    }

    /**
     * 设置光标操作类型
     *
     * @param type 光标操作类型（TYPE_CHANGE/TYPE_OPEN/TYPE_CLOSE）
     */
    public void setType(int type) {
        this.type = type; // 更新光标操作类型
    }
}