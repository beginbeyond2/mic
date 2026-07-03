package com.micsig.tbook.tbookscope.main.mainbottom;

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                     MainBottomMsgQuick - 底部快捷栏消息类                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   主界面底部快捷栏的消息数据载体，用于传递快捷按钮的启用状态                         │
 * │                                                                              │
 * │ 【核心职责】                                                                   │
 * │   1. 存储底部快捷栏各按钮的启用状态数组                                          │
 * │   2. 提供状态数组的getter和setter方法                                          │
 * │   3. 支持单个按钮状态的设置和整体状态的设置                                       │
 * │                                                                              │
 * │ 【架构设计】                                                                   │
 * │   简单数据模型类，采用boolean数组存储多按钮状态                                   │
 * │   作为RxBus消息传递的数据载体，被MainHolderBottom等类使用                        │
 * │                                                                              │
 * │ 【数据流向】                                                                   │
 * │   MainHolderBottom → MainBottomMsgQuick → RxBus → 各订阅组件                   │
 * │   用于同步底部快捷栏按钮的可用状态                                               │
 * │                                                                              │
 * │ 【依赖关系】                                                                   │
 * │   被依赖：MainHolderBottom、MainHolderBottomQuick                             │
 * │   依赖：无                                                                    │
 * │                                                                              │
 * │ 【使用场景】                                                                   │
 * │   1. 工作模式切换时更新按钮可用状态                                              │
 * │   2. 串口文本模式切换时同步按钮状态                                              │
 * │   3. 自动保存任务状态改变时更新按钮状态                                          │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @auother Liwb
 * @description: 底部快捷栏按钮启用状态消息类
 * @data:2022-2-14 9:31
 */
public class MainBottomMsgQuick {
    
    /** 底部快捷栏各按钮的启用状态数组，索引对应各按钮位置 */ // 底部快捷按钮启用状态数组
    private boolean[] enable;

    /**
     * 获取底部快捷栏按钮的启用状态数组
     * 
     * @return boolean[] 按钮启用状态数组，true表示可用，false表示不可用
     */
    public boolean[] getEnable() {
        return enable; // 返回启用状态数组
    }

    /**
     * 设置指定索引位置的按钮启用状态
     * 
     * @param index   按钮索引位置，对应数组下标
     * @param enable  启用状态，true表示可用，false表示不可用
     */
    public void setEnable(int index, boolean enable) {
        this.enable[index] = enable; // 设置指定索引的按钮状态
    }

    /**
     * 设置整个按钮启用状态数组
     * 
     * @param enable 按钮启用状态数组，包含所有按钮的状态
     */
    public void setEnable(boolean[] enable) {
        this.enable = enable; // 设置整个状态数组
    }
}