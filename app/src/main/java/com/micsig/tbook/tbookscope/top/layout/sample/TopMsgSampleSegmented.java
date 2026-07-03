// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopMsgSampleSegmented.java
//  核心职责：分段存储子页面的消息数据类，携带分段开关/数量/自定义/显示/拟合/排序等状态
//  架构设计：消息数据类，作为RxBus消息载体，传递分段存储所有配置状态
//  数据流向：TopLayoutSampleSegmented → RxBus → TopLayoutSample（标题栏更新）
//  依赖关系：依赖RxBooleanWithSelect、RxStringWithSelect带选中标志的Bean、TopBeanChannel通道Bean
//  使用场景：分段存储页面发送状态消息时，作为消息体通过RxBus传递
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中标志的布尔Bean
import com.micsig.tbook.ui.bean.RxStringWithSelect; // 导入带选中标志的字符串Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean

/**
 * 分段存储消息类 - 携带分段存储的所有配置状态
 */
public class TopMsgSampleSegmented { // 分段存储消息类
    /** 是否来自EventBus事件 */
    private boolean isFromEventBus; // 事件来源标志
    /** 分段存储开关状态（带选中标志） */
    private RxBooleanWithSelect state; // 分段开关状态
    /** 分段数量选择数据 */
    private TopBeanChannel number; // 分段数量Bean
    /** 自定义分段数量（带选中标志） */
    private RxStringWithSelect userDefine; // 自定义数量
    /** 分段显示模式选择数据 */
    private TopBeanChannel display; // 显示模式Bean
    /** 拟合起始帧（带选中标志） */
    private RxStringWithSelect start; // 拟合起始帧
    /** 拟合结束帧（带选中标志） */
    private RxStringWithSelect end; // 拟合结束帧
    /** 分段排序选择数据 */
    private TopBeanChannel order; // 排序Bean

    /**
     * 判断消息是否来自EventBus
     * @return true表示来自EventBus，false表示来自用户操作
     */
    public boolean isFromEventBus() { // 判断是否来自EventBus
        return isFromEventBus; // 返回事件来源标志
    }

    /**
     * 设置消息来源标志
     * @param fromEventBus true表示来自EventBus，false表示来自用户操作
     */
    public void setFromEventBus(boolean fromEventBus) { // 设置事件来源标志
        isFromEventBus = fromEventBus; // 保存事件来源标志
    }

    /**
     * 获取分段存储开关状态
     * @return 带选中标志的布尔Bean
     */
    public RxBooleanWithSelect getState() { // 获取分段开关状态
        return state; // 返回分段开关状态
    }

    /**
     * 设置分段存储开关状态
     * @param state 开关状态
     */
    public void setState(boolean state) { // 设置分段开关状态
        if (this.state == null) { // 如果当前状态为空（首次设置）
            this.state = new RxBooleanWithSelect(state); // 创建新的带选中标志的布尔
        } else { // 如果当前状态已有值
            this.state.setValue(state); // 更新状态值
            setAllUnSelect(); // 清除所有选中状态
            this.state.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取分段数量选择数据
     * @return 分段数量Bean
     */
    public TopBeanChannel getNumber() { // 获取分段数量
        return number; // 返回分段数量Bean
    }

    /**
     * 设置分段数量选择数据
     * @param number 分段数量Bean
     */
    public void setNumber(TopBeanChannel number) { // 设置分段数量
        if (this.number == null) { // 如果当前数量为空（首次设置）
            this.number = number; // 直接赋值
        } else { // 如果当前数量已有值
            this.number = number; // 更新数量
            setAllUnSelect(); // 清除所有选中状态
            this.number.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取自定义分段数量
     * @return 带选中标志的字符串Bean
     */
    public RxStringWithSelect getUserDefine() { // 获取自定义数量
        return userDefine; // 返回自定义数量
    }

    /**
     * 设置自定义分段数量
     * @param userDefine 自定义数量字符串
     */
    public void setUserDefine(String userDefine) { // 设置自定义数量
        if (this.userDefine == null) { // 如果当前值为空（首次设置）
            this.userDefine = new RxStringWithSelect(userDefine); // 创建新的带选中标志的字符串
        } else { // 如果当前值已有值
            this.userDefine.setValue(userDefine); // 更新值
            setAllUnSelect(); // 清除所有选中状态
            this.userDefine.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取分段显示模式选择数据
     * @return 显示模式Bean
     */
    public TopBeanChannel getDisplay() { // 获取显示模式
        return display; // 返回显示模式Bean
    }

    /**
     * 设置分段显示模式选择数据
     * @param display 显示模式Bean
     */
    public void setDisplay(TopBeanChannel display) { // 设置显示模式
        if (this.display == null) { // 如果当前显示为空（首次设置）
            this.display = display; // 直接赋值
        } else { // 如果当前显示已有值
            this.display = display; // 更新显示
            setAllUnSelect(); // 清除所有选中状态
            this.display.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取拟合起始帧
     * @return 带选中标志的字符串Bean
     */
    public RxStringWithSelect getStart() { // 获取拟合起始帧
        return start; // 返回拟合起始帧
    }

    /**
     * 设置拟合起始帧
     * @param start 起始帧字符串
     */
    public void setStart(String start) { // 设置拟合起始帧
        if (this.start == null) { // 如果当前值为空（首次设置）
            this.start = new RxStringWithSelect(start); // 创建新的带选中标志的字符串
        } else { // 如果当前值已有值
            this.start.setValue(start); // 更新值
            setAllUnSelect(); // 清除所有选中状态
            this.start.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取拟合结束帧
     * @return 带选中标志的字符串Bean
     */
    public RxStringWithSelect getEnd() { // 获取拟合结束帧
        return end; // 返回拟合结束帧
    }

    /**
     * 设置拟合结束帧
     * @param end 结束帧字符串
     */
    public void setEnd(String end) { // 设置拟合结束帧
        if (this.end == null) { // 如果当前值为空（首次设置）
            this.end = new RxStringWithSelect(end); // 创建新的带选中标志的字符串
        } else { // 如果当前值已有值
            this.end.setValue(end); // 更新值
            setAllUnSelect(); // 清除所有选中状态
            this.end.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 获取分段排序选择数据
     * @return 排序Bean
     */
    public TopBeanChannel getOrder() { // 获取排序
        return order; // 返回排序Bean
    }

    /**
     * 设置分段排序选择数据
     * @param order 排序Bean
     */
    public void setOrder(TopBeanChannel order) { // 设置排序
        if (this.order == null) { // 如果当前排序为空（首次设置）
            this.order = order; // 直接赋值
        } else { // 如果当前排序已有值
            this.order = order; // 更新排序
            setAllUnSelect(); // 清除所有选中状态
            this.order.setRxMsgSelect(true); // 标记当前项为选中
        }
    }

    /**
     * 清除所有字段的选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态
        state.setRxMsgSelect(false); // 取消开关选中
        number.setRxMsgSelect(false); // 取消数量选中
        userDefine.setRxMsgSelect(false); // 取消自定义选中
        display.setRxMsgSelect(false); // 取消显示选中
        start.setRxMsgSelect(false); // 取消起始帧选中
        end.setRxMsgSelect(false); // 取消结束帧选中
        order.setRxMsgSelect(false); // 取消排序选中
    }

    /**
     * 返回消息的字符串表示
     * @return 包含所有字段的字符串
     */
    @Override
    public String toString() { // 转为字符串
        return "TopMsgSampleSegmented{" + // 返回消息字符串
                "state=" + state + // 包含开关状态
                ", number=" + number + // 包含数量
                ", userDefine=" + userDefine + // 包含自定义数量
                ", display=" + display + // 包含显示模式
                ", start=" + start + // 包含起始帧
                ", end=" + end + // 包含结束帧
                ", order=" + order + // 包含排序
                '}'; // 结束
    }
}
