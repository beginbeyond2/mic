package com.micsig.tbook.tbookscope.main.mainbottom;



/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │               MainBottomMsgQuickHighRefresh - 高刷新率消息类                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   底部快捷栏高刷新率状态的消息数据载体，用于标识是否需要高刷新显示                   │
 * │                                                                              │
 * │ 【核心职责】                                                                   │
 * │   1. 存储高刷新率显示状态标识                                                   │
 * │   2. 提供刷新状态的getter和setter方法                                          │
 * │   3. 作为消息传递的数据载体                                                    │
 * │                                                                              │
 * │ 【架构设计】                                                                   │
 * │   简单数据模型类，采用单一boolean字段存储刷新状态                                │
 * │   作为消息传递载体，用于界面刷新控制                                             │
 * │                                                                              │
 * │ 【数据流向】                                                                   │
 * │   数据源 → MainBottomMsgQuickHighRefresh → RxBus → 界面订阅者                  │
 * │   控制界面是否采用高刷新率模式显示                                               │
 * │                                                                              │
 * │ 【依赖关系】                                                                   │
 * │   被依赖：界面刷新相关组件                                                      │
 * │   依赖：无                                                                    │
 * │                                                                              │
 * │ 【使用场景】                                                                   │
 * │   1. 高速信号采集时启用高刷新显示                                               │
 * │   2. 实时波形更新控制                                                         │
 * │   3. 界面性能优化调节                                                         │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * Created by yangj on 2017/8/21.
 */

public class MainBottomMsgQuickHighRefresh  {
    
    /** 高刷新率显示状态标识，true表示需要高刷新，false表示正常刷新 */ // 高刷新率状态标识
    private boolean refresh;

    /**
     * 构造函数，初始化高刷新率状态
     * 
     * @param refresh 初始刷新状态，true表示启用高刷新率，false表示正常刷新率
     */
    public MainBottomMsgQuickHighRefresh(boolean refresh) {
        this.refresh = refresh; // 初始化刷新状态
    }

    /**
     * 判断是否需要高刷新率显示
     * 
     * @return boolean 高刷新状态，true表示需要高刷新，false表示正常刷新
     */
    public boolean isRefresh() {
        return refresh; // 返回刷新状态
    }

    /**
     * 设置高刷新率显示状态
     * 
     * @param refresh 刷新状态，true表示启用高刷新率，false表示正常刷新率
     */
    public void setRefresh(boolean refresh) {
        this.refresh = refresh; // 设置刷新状态
    }
}