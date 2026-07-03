package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        RightMsgRefForEight                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 8通道Ref消息封装类                                   ║
 * ║ 核心职责：封装单个参考通道的开关状态和标签信息，用于UI与业务层通信                ║
 * ║ 架构设计：纯数据消息Bean，配合RightLayoutRef使用，通过RxBus事件总线传递         ║
 * ║ 数据流向：RightLayoutRef → RxBus → 订阅方                                   ║
 * ║ 依赖关系：RxBooleanWithSelect, RxStringWithSelect, TChan                    ║
 * ║ 使用场景：8通道Ref模式下，传递参考通道的开关和标签变更消息                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 8通道Ref消息封装类
 * <p>
 * 封装单个参考通道的核心属性：
 * - refChecked：参考通道开关状态
 * - label：参考通道标签文本
 * - refChannelNumber：参考通道编号（R1-R8）
 * - isUpClick：垂直档位调节方向标识
 * </p>
 * <p>每次属性变更时，会先调用setAllUnSelect()清除所有字段的选中标记，
 * 再将当前变更字段标记为选中，以便接收方识别哪个属性发生了变化。</p>
 */
public class RightMsgRefForEight {
    /** 参考通道开关状态（带选中标记） */
    private RxBooleanWithSelect refChecked;
    /** 参考通道标签文本（带选中标记） */
    private RxStringWithSelect label;

    /** 参考通道编号，默认R1 */
    private int refChannelNumber = TChan.R1;//默认R1

    /** 垂直档位调节方向标识（true=向上调节, false=向下调节） */
    private boolean isUpClick;

    /**
     * 判断是否为向上点击调节
     * @return true=向上调节, false=向下调节
     */
    public boolean isUpClick() {
        return isUpClick;
    }

    /**
     * 设置垂直档位调节方向
     * @param upClick true=向上调节, false=向下调节
     */
    public void setUpClick(boolean upClick) {
        isUpClick = upClick;
    }

    /**
     * 获取参考通道编号
     * @return 参考通道编号（TChan.R1-R8）
     */
    public int getRefChannelNumber() {
        return refChannelNumber;
    }

    /**
     * 设置参考通道编号
     * @param refChannelNumber 参考通道编号
     */
    public void setRefChannelNumber(int refChannelNumber) {
        this.refChannelNumber = refChannelNumber;
    }

    /**
     * 获取参考通道开关状态
     * @return 带选中标记的开关状态
     */
    public RxBooleanWithSelect getRefChecked() {
        return refChecked;
    }

    /**
     * 设置参考通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param refChecked 开关状态值
     */
    public void setRefChecked(boolean refChecked) {
        if (this.refChecked == null) {                                               // 首次创建对象
            this.refChecked = new RxBooleanWithSelect(refChecked);
        } else {                                                                     // 非首次，更新值并标记选中
            this.refChecked.setValue(refChecked);
            setAllUnSelect();                                                        // 先清除所有选中标记
            this.refChecked.setRxMsgSelect(true);                                    // 标记当前字段为选中
        }
    }

    /**
     * 获取参考通道标签文本
     * @return 带选中标记的标签文本
     */
    public RxStringWithSelect getLabel() {
        return label;
    }

    /**
     * 设置参考通道标签文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param label 标签文本值
     */
    public void setLabel(String label) {
        if (this.label == null) {                                                    // 首次创建对象
            this.label = new RxStringWithSelect(label);
        } else {                                                                     // 非首次，更新值并标记选中
            this.label.setValue(label);
            setAllUnSelect();                                                        // 先清除所有选中标记
            this.label.setRxMsgSelect(true);                                         // 标记当前字段为选中
        }
    }


    /**
     * 清除所有字段的选中标记
     * <p>在每次属性变更前调用，确保只有变更的字段被标记为选中</p>
     */
    private void setAllUnSelect() {
        refChecked.setRxMsgSelect(false);                                            // 清除开关状态选中标记
        label.setRxMsgSelect(false);                                                 // 清除标签选中标记
    }

    /**
     * 返回对象的字符串表示
     * @return 包含通道编号、开关状态和标签的字符串
     */
    @Override
    public String toString() {
        return "RightMsgRef{" +
                "refChannelNumber=" + refChannelNumber +
                ", refChecked=" + refChecked +
                ", refLabel=" + label +
                '}';
    }
}
