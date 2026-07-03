package com.micsig.tbook.tbookscope.main.mainright; // 定义包路径，属于主界面右侧功能模块

/**
 * +=============================================================================================+
 * |  模块定位：示波器主界面右侧面板 - 触发电平消息数据类                                         |
 * +=============================================================================================+
 * |  核心职责：                                                                                  |
 * |  1. 封装触发电平调节相关的消息数据                                                           |
 * |  2. 记录当前触发电平值和触发通道号                                                           |
 * |  3. 标识消息来源（EventBus或UI操作）                                                         |
 * |  4. 控制触发源修改的开关状态                                                                 |
 * +=============================================================================================+
 * |  架构设计：                                                                                  |
 * |  - 数据传输对象（DTO）模式                                                                   |
 * |  - 消息封装类，用于模块间数据传递                                                            |
 * |  - 支持拷贝构造，便于消息复制和转发                                                          |
 * +=============================================================================================+
 * |  数据流向：                                                                                  |
 * |  输入：触发电平值（curLevel）+ 触发通道号（curCh） + 来源标识 + 修改范围标识                 |
 * |  流向：UI组件 → EventBus → 业务处理模块 → 硬件控制层                                        |
 * +=============================================================================================+
 * |  依赖关系：                                                                                  |
 * |  - 无外部依赖，纯数据封装类                                                                  |
 * |  - 被MainHolderTriggerLevel等UI组件使用                                                     |
 * |  - 通过EventBus在模块间传递                                                                 |
 * +=============================================================================================+
 * |  使用场景：                                                                                  |
 * |  1. 触发电平调节时封装消息数据                                                               |
 * |  2. 通道切换时传递触发电平信息                                                               |
 * |  3. EventBus消息传递的载体对象                                                               |
 * +=============================================================================================+
 * Created by yangj on 2017/8/28.
 */

public class MainMsgTriggerLevel { // 触发电平消息数据类定义
    /**
     * 该消息是否是来自于eventBus的发送
     */
    private boolean isFromEventBus = false;
    /**
     * 该消息一般情况下是同时发送给触发电平值、阈值电平值、触发源的修改，
     * 但是当移动通道时，不做触发源的修改
     */
    private boolean isOnlyModifyNumber = false;
    private String curLevel;
    private int curCh;

    public MainMsgTriggerLevel() {
    }

    public MainMsgTriggerLevel(MainMsgTriggerLevel msgTriggerLevel) {
        this.curLevel = msgTriggerLevel.curLevel;
        this.curCh = msgTriggerLevel.curCh;
        this.isOnlyModifyNumber = msgTriggerLevel.isOnlyModifyNumber;
        this.isFromEventBus = msgTriggerLevel.isFromEventBus;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public boolean isOnlyModifyNumber() {
        return isOnlyModifyNumber;
    }

    public void setOnlyModifyNumber(boolean onlyModifyNumber) {
        isOnlyModifyNumber = onlyModifyNumber;
    }

    public String getCurLevel() {
        return curLevel;
    }

    public void setCurLevel(String curLevel) {
        this.curLevel = curLevel;
    }

    public int getCurCh() {
        return curCh;
    }

    public void setCurCh(int curCh) {
        this.curCh = curCh;
    }

    @Override
    public String toString() {
        return "MainMsgTriggerLevel{" +
                "isFromEventBus=" + isFromEventBus +
                ", isOnlyModifyNumber=" + isOnlyModifyNumber +
                ", curLevel='" + curLevel + '\'' +
                ", curCh=" + curCh +
                '}';
    }
}
