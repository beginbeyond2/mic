package com.micsig.tbook.tbookscope.services.ExternalKeys.client; // 外部按键客户端模块包

/**
 * +----------------------------------------------------------------------+
 * |                    触发电平/值电平线切换管理器                         |
 * +----------------------------------------------------------------------+
 * | 模块定位：ExternalKeys客户端侧，管理示波器触发电平线和值电平线的       |
 * |           激活状态和切换逻辑                                          |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 维护触发电平线(TriggerLevel)的激活状态                          |
 * |   2. 提供触发电平/值电平切换的回调机制                                |
 * |   3. 单例模式，全局唯一管理触发电平线状态                              |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   单例模式 + 回调接口                                                 |
 * |   通过ITriggerVoltageLineCallback接口通知外部切换触发电平/值电平       |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   外部按键 → setTriggerLevelActive / switchTriggerValueLevel → 回调   |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - ITriggerVoltageLineCallback (切换回调接口)                        |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   物理旋钮旋转时，切换触发电平线和值电平线的激活状态                  |
 * +----------------------------------------------------------------------+
 */
public class TriggerValueVoltageLine {
    private boolean triggerLevelActive = true; // 触发电平线激活状态，默认激活
    private static TriggerValueVoltageLine triggerVoltageLine = null; // 单例实例

    /**
     * 获取单例实例
     * 使用懒加载方式创建，线程不安全（单线程环境下使用）
     *
     * @return TriggerValueVoltageLine单例实例
     */
    public static TriggerValueVoltageLine getInstance(){
        if(triggerVoltageLine == null){ // 实例为空
            triggerVoltageLine = new TriggerValueVoltageLine(); // 创建新实例
        }
        return triggerVoltageLine; // 返回单例实例
    }

    /**
     * +----------------------------------------------------------------------+
     * | 触发/值电平线切换回调接口                                             |
     * +----------------------------------------------------------------------+
     * | 由外部实现，用于获取触发/值电平线的数量并执行切换操作                  |
     * +----------------------------------------------------------------------+
     */
    public interface ITriggerVoltageLineCallback{
        /**
         * 获取触发电平线数量
         *
         * @return 触发电平线数量
         */
        int OnTriggerLevelNums();

        /**
         * 获取值电平线数量
         *
         * @return 值电平线数量
         */
        int OnValueLevelNums();

        /**
         * 执行触发电平/值电平切换操作
         */
        void switchTriggerValueLevel();
    }

    private ITriggerVoltageLineCallback triggerVoltageLineCallback = null; // 回调接口引用

    /**
     * 私有构造函数，防止外部实例化
     */
    private TriggerValueVoltageLine(){

    }

    /**
     * 设置触发电平/值电平切换回调
     *
     * @param triggerVoltageLineCallback 回调接口实现
     */
    public void setTriggerVoltageLineCallback(ITriggerVoltageLineCallback triggerVoltageLineCallback){
        this.triggerVoltageLineCallback = triggerVoltageLineCallback; // 保存回调引用
    }

    /**
     * 设置触发电平线激活状态
     *
     * @param triggerLevelActive true=激活触发电平线，false=禁用
     */
    public void setTriggerLevelActive(boolean triggerLevelActive){
        this.triggerLevelActive = triggerLevelActive; // 设置激活状态
    }

    /**
     * 获取触发电平线激活状态
     *
     * @return true=触发电平线激活，false=禁用
     */
    public boolean isTriggerlevelActive(){
        return triggerLevelActive; // 返回激活状态
    }

    /**
     * 切换触发电平/值电平
     * 通过回调接口通知外部执行切换操作
     */
    public void switchTriggerValueLevel(){
        if(triggerVoltageLineCallback != null){ // 回调不为空
            triggerVoltageLineCallback.switchTriggerValueLevel(); // 调用回调执行切换
        }
    }
}
