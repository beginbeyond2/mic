package com.micsig.tbook.tbookscope.main.mainbottom;


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                   MainTopMsgRightGone - 顶部右侧隐藏消息类                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   主界面顶部右侧区域显示状态的消息数据载体，用于控制右侧区域的可见性                 │
 * │                                                                              │
 * │ 【核心职责】                                                                   │
 * │   1. 存储顶部右侧区域的可见性状态                                              │
 * │   2. 提供可见性的getter和setter方法                                            │
 * │   3. 作为消息传递的数据载体控制界面显示                                         │
 * │                                                                              │
 * │ 【架构设计】                                                                   │
 * │   简单数据模型类，采用单一boolean字段存储可见性状态                              │
 * │   默认状态为可见(visible=true)                                                │
 * │                                                                              │
 * │ 【数据流向】                                                                   │
 * │   MainHolderBottom → MainTopMsgRightGone → RxBus → 顶部右侧组件                │
 * │   用于控制顶部右侧区域在特定条件下的显示隐藏                                     │
 * │                                                                              │
 * │ 【依赖关系】                                                                   │
 * │   被依赖：MainHolderBottom、MainHolderBottomQuick                             │
 * │   依赖：无                                                                    │
 * │                                                                              │
 * │ 【使用场景】                                                                   │
 * │   1. 滚屏模式或慢时基时隐藏右侧区域                                             │
 * │   2. Zoom模式退出时控制右侧显示                                                │
 * │   3. 运行状态变化时同步右侧区域可见性                                           │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * Created by yangj on 2017/7/31.
 */

public class MainTopMsgRightGone {
    
    /** 顶部右侧区域的可见性状态，true表示可见，false表示隐藏 */ // 可见性状态标识
    private boolean visible;

    /**
     * 默认构造函数，初始化为可见状态
     */
    public MainTopMsgRightGone() {
        this.visible = true; // 初始化为可见状态
    }

    /**
     * 判断顶部右侧区域是否可见
     * 
     * @return boolean 可见性状态，true表示可见，false表示隐藏
     */
    public boolean isVisible() {
        return visible; // 返回可见性状态
    }

    /**
     * 设置顶部右侧区域的可见性状态
     * 
     * @param visible 可见性状态，true表示可见，false表示隐藏
     */
    public void setVisible(boolean visible) {
        this.visible = visible; // 设置可见性状态
    }

    /**
     * 获取对象的字符串表示，用于调试和日志输出
     * 
     * @return String 包含可见性状态的字符串描述
     */
    @Override
    public String toString() {
        return "MainTopMsgRightGone{" + // 构建toString字符串起始
                "visible=" + visible + // 添加可见性状态信息
                '}'; // 结束toString字符串
    }
}