package com.micsig.tbook.tbookscope.top.layout.cursor; // 光标模块消息类所在包

import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail; // 导入测量详情接口，用于光标详情数据传递
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题数据Bean，用于光标标题信息传递

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                      TopMsgCursor                                   │
 * │                  光标模块消息数据封装类                               │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：top.layout.cursor → 光标子系统的消息传递载体               │
 * │ 核心职责：封装光标标题、光标详情、EventBus来源标志，供RxBus传递      │
 * │ 架构设计：标准POJO消息类，由TopLayoutCursor构建并通过RxBus分发       │
 * │ 数据流向：TopLayoutCursor → RxBus → 主界面光标管理器                 │
 * │ 依赖关系：TopAllBeanTitle(标题数据)、IMeasureDetail(详情数据)        │
 * │ 使用场景：光标模式切换、光标参数变更时，携带数据通知订阅者            │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/5/16.
 */

public class TopMsgCursor { // 光标消息数据封装类
    private TopAllBeanTitle cursorTitle; // 光标标题数据，包含当前选中的标题项索引和文本
    private IMeasureDetail cursorDetail; // 光标详情数据，用于传递光标测量详情
    private boolean isFromEventBus; // 是否来自EventBus远程事件的标志

    /**
     * 获取是否来自EventBus远程事件
     * @return true表示来自EventBus，false表示本地UI操作
     */
    public boolean isFromEventBus() { // 判断消息是否来自EventBus远程事件
        return isFromEventBus; // 返回EventBus来源标志
    } // isFromEventBus方法结束

    /**
     * 设置EventBus来源标志
     * @param fromEventBus 是否来自EventBus
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置EventBus来源标志
        isFromEventBus = fromEventBus; // 赋值EventBus来源标志
    } // setFromEventBus方法结束

    /**
     * 获取光标标题数据
     * @return 当前光标标题Bean
     */
    public TopAllBeanTitle getCursorTitle() { // 获取光标标题数据
        return cursorTitle; // 返回光标标题Bean
    } // getCursorTitle方法结束

    /**
     * 设置光标标题数据，非首次设置时标记为Rx消息选中状态
     * @param cursorTitle 光标标题Bean
     */
    public void setCursorTitle(TopAllBeanTitle cursorTitle) { // 设置光标标题数据
        if (this.cursorTitle == null) { // 如果当前标题为空（首次设置）
            this.cursorTitle = cursorTitle; // 直接赋值标题
        } else { // 非首次设置
            this.cursorTitle = cursorTitle; // 赋值新标题
            this.cursorTitle.setRxMsgSelect(true); // 标记为Rx消息选中状态，通知订阅者标题已变更
        } // if-else结束
    } // setCursorTitle方法结束

    /**
     * 获取光标详情数据
     * @return 光标详情接口实例
     */
    public IMeasureDetail getCursorDetail() { // 获取光标详情数据
        return cursorDetail; // 返回光标详情接口实例
    } // getCursorDetail方法结束

    /**
     * 设置光标详情数据
     * @param cursorDetail 光标详情接口实例
     */
    public void setCursorDetail(IMeasureDetail cursorDetail) { // 设置光标详情数据
        this.cursorDetail = cursorDetail; // 赋值光标详情
    } // setCursorDetail方法结束

    /**
     * 返回消息对象的字符串表示，用于调试日志
     * @return 包含cursorTitle和cursorDetail的字符串
     */
    @Override
    public String toString() { // 重写toString方法，用于调试输出
        return "TopMsgMeasure{" + // 返回消息对象字符串表示
                "cursorTitle=" + cursorTitle + // 拼接标题信息
                ", cursorDetail=" + cursorDetail + // 拼接详情信息
                '}'; // 字符串结束
    } // toString方法结束
} // TopMsgCursor类结束
