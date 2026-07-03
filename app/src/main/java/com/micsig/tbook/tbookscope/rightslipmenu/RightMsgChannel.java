package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          RightMsgChannel                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 模拟通道消息封装类                                    ║
 * ║ 核心职责：封装单个模拟通道的所有参数状态，用于UI与业务层通信                      ║
 * ║ 架构设计：纯数据消息Bean，配合RightLayoutChannel使用，通过RxBus事件总线传递     ║
 * ║ 数据流向：RightLayoutChannel → RxBus → 订阅方                               ║
 * ║ 依赖关系：RxBooleanWithSelect, RxStringWithSelect, RightBeanSelect          ║
 * ║ 使用场景：通道参数（耦合/探头/带宽/阻抗/标签/延迟/微调等）变更时传递消息         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/12.
 * 模拟通道消息封装类
 * <p>
 * 封装单个模拟通道的所有参数状态，包括：
 * - channelNumber：通道编号（1-8）
 * - chCheck：通道开关
 * - invert：波形反相
 * - couple：耦合方式（DC/AC/GND）
 * - probeType：探头类型
 * - probeMultiple：探头倍率
 * - bandWidth：带宽限制
 * - imped：输入阻抗
 * - label：通道标签
 * - delay：通道延迟
 * - fineExtent：微调幅度
 * - fineSwitch：微调开关
 * </p>
 * <p>每个setter方法在非首次调用时，会先清除所有字段的选中标记，
 * 再将当前变更字段标记为选中，以便接收方识别哪个属性发生了变化。</p>
 */
public class RightMsgChannel {

    /**
     * 通道编号，值范围1-8
     * 对应 TChan.Ch1 ~ TChan.Ch8
     */
    private int channelNumber;
    /** 通道开关状态（带选中标记） */
    private RxBooleanWithSelect chCheck;
    /** 波形反相开关（带选中标记） */
    private RxBooleanWithSelect invert;
    /** 耦合方式选择项 */
    private RightBeanSelect couple;
    /** 探头类型选择项 */
    private RightBeanSelect probeType;
    /** 探头倍率文本（带选中标记） */
    private RxStringWithSelect probeMultiple;
    /** 带宽限制选择项 */
    private RightBeanSelect bandWidth;
    /** 带宽编辑值文本（带选中标记） */
    private RxStringWithSelect bandWidthEdit;
    /** 输入阻抗选择项 */
    private RightBeanSelect imped;
    /** 通道标签文本（带选中标记） */
    private RxStringWithSelect label;
    /** 通道延迟文本（带选中标记） */
    private RxStringWithSelect delay;

    /** 微调幅度文本（带选中标记） */
    private RxStringWithSelect fineExtent;

    /** 微调开关状态（带选中标记） */
    private RxBooleanWithSelect fineSwitch;
    /** 探头倍率变化比，改变前的值除以改变后的值 */
    private double probeMulScale;//改变比，改变前的值除以改变后的值

    /** 是否来自EventBus事件 */
    private boolean isFromEventBus;
    /** 是否为向上点击（垂直档位调节方向） */
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
     * 判断是否来自EventBus事件
     * @return true=来自事件总线, false=来自用户操作
     */
    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    /**
     * 设置是否来自EventBus事件
     * @param fromEventBus 是否来自事件总线
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    /**
     * 获取通道编号
     * @return 通道编号（1-8）
     */
    public int getChannelNumber() {
        return channelNumber;
    }

    /**
     * 设置通道编号
     * @param channelNumber 通道编号
     */
    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    /**
     * 获取通道开关状态
     * @return 带选中标记的开关状态
     */
    public RxBooleanWithSelect getChCheck() {
        return chCheck;
    }

    /**
     * 设置通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param chCheck 开关状态值
     */
    public void setChCheck(boolean chCheck) {
        if (this.chCheck == null) {                                                    // 首次创建对象
            this.chCheck = new RxBooleanWithSelect(chCheck);
        } else {                                                                       // 非首次，更新值并标记选中
            this.chCheck.setValue(chCheck);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.chCheck.setRxMsgSelect(true);                                         // 标记当前字段为选中
        }
    }

    /**
     * 获取波形反相开关状态
     * @return 带选中标记的反相状态
     */
    public RxBooleanWithSelect getInvert() {
        return invert;
    }

    /**
     * 设置波形反相开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param invert 反相状态值
     */
    public void setInvert(boolean invert) {
        if (this.invert == null) {                                                     // 首次创建对象
            this.invert = new RxBooleanWithSelect(invert);
        } else {                                                                       // 非首次，更新值并标记选中
            this.invert.setValue(invert);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.invert.setRxMsgSelect(true);                                          // 标记当前字段为选中
        }
    }

    /**
     * 获取耦合方式选择项
     * @return 耦合方式选择项
     */
    public RightBeanSelect getCouple() {
        return couple;
    }

    /**
     * 设置耦合方式选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param couple 耦合方式选择项
     */
    public void setCouple(RightBeanSelect couple) {
        if (this.couple == null) {                                                     // 首次赋值
            this.couple = couple;
        } else {                                                                       // 非首次，更新值并标记选中
            this.couple = couple;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.couple.setRxMsgSelect(true);                                          // 标记当前字段为选中
        }
    }

    /**
     * 获取探头类型选择项
     * @return 探头类型选择项
     */
    public RightBeanSelect getProbeType() {
        return probeType;
    }

    /**
     * 设置探头类型选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param probeType 探头类型选择项
     */
    public void setProbeType(RightBeanSelect probeType) {
        if (this.probeType == null) {                                                  // 首次赋值
            this.probeType = probeType;
        } else {                                                                       // 非首次，更新值并标记选中
            this.probeType = probeType;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.probeType.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取探头倍率文本
     * @return 带选中标记的探头倍率
     */
    public RxStringWithSelect getProbeMultiple() {
        return probeMultiple;
    }

    /**
     * 设置探头倍率文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param probeMultiple 探头倍率文本
     */
    public void setProbeMultiple(String probeMultiple) {
        if (this.probeMultiple == null) {                                              // 首次创建对象
            this.probeMultiple = new RxStringWithSelect(probeMultiple);
        } else {                                                                       // 非首次，更新值并标记选中
            this.probeMultiple.setValue(probeMultiple);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.probeMultiple.setRxMsgSelect(true);                                   // 标记当前字段为选中
        }
    }

    /**
     * 获取探头倍率变化比
     * @return 变化比值（改变前/改变后）
     */
    public double getProbeMulScale() {
        return probeMulScale;
    }

    /**
     * 设置探头倍率变化比
     * @param probeMulScale 变化比值
     */
    public void setProbeMulScale(double probeMulScale) {
        this.probeMulScale = probeMulScale;
    }

    /**
     * 获取带宽限制选择项
     * @return 带宽限制选择项
     */
    public RightBeanSelect getBandWidth() {
        return bandWidth;
    }

    /**
     * 设置带宽限制选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param bandWidth 带宽限制选择项
     */
    public void setBandWidth(RightBeanSelect bandWidth) {
        if (this.bandWidth == null) {                                                  // 首次赋值
            this.bandWidth = bandWidth;
        } else {                                                                       // 非首次，更新值并标记选中
            this.bandWidth = bandWidth;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.bandWidth.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取带宽编辑值文本
     * @return 带选中标记的带宽编辑值
     */
    public RxStringWithSelect getBandWidthEdit() {
        return bandWidthEdit;
    }

    /**
     * 设置带宽编辑值文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param bandWidthEdit 带宽编辑值文本
     */
    public void setBandWidthEdit(String bandWidthEdit) {
        if (this.bandWidthEdit == null) {                                              // 首次创建对象
            this.bandWidthEdit = new RxStringWithSelect(bandWidthEdit);
        } else {                                                                       // 非首次，更新值并标记选中
            this.bandWidthEdit.setValue(bandWidthEdit);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.bandWidthEdit.setRxMsgSelect(true);                                   // 标记当前字段为选中
        }
    }

    /**
     * 获取输入阻抗选择项
     * @return 输入阻抗选择项
     */
    public RightBeanSelect getImped() {
        return imped;
    }

    /**
     * 设置输入阻抗选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param Imped 输入阻抗选择项
     */
    public void setImped(RightBeanSelect Imped) {
        if (this.imped == null) {                                                      // 首次赋值
            this.imped = Imped;
        } else {                                                                       // 非首次，更新值并标记选中
            this.imped = Imped;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.imped.setRxMsgSelect(true);                                           // 标记当前字段为选中
        }
    }

    /**
     * 获取通道标签文本
     * @return 带选中标记的标签文本
     */
    public RxStringWithSelect getLabel() {
        return label;
    }

    /**
     * 设置通道标签文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param label 标签文本
     */
    public void setLabel(String label) {
        if (this.label == null) {                                                      // 首次创建对象
            this.label = new RxStringWithSelect(label);
        } else {                                                                       // 非首次，更新值并标记选中
            this.label.setValue(label);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.label.setRxMsgSelect(true);                                           // 标记当前字段为选中
        }
    }

    /**
     * 获取通道延迟文本
     * @return 带选中标记的延迟文本
     */
    public RxStringWithSelect getDelay() {
        return delay;
    }

    /**
     * 设置通道延迟文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param delay 延迟文本
     */
    public void setDelay(String delay) {
        if (this.delay == null) {                                                      // 首次创建对象
            this.delay = new RxStringWithSelect(delay);
        } else {                                                                       // 非首次，更新值并标记选中
            this.delay.setValue(delay);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.delay.setRxMsgSelect(true);                                           // 标记当前字段为选中
        }
    }

    /**
     * 获取微调幅度文本
     * @return 带选中标记的微调幅度
     */
    public RxStringWithSelect getFineExtent(){return fineExtent;}

    /**
     * 设置微调幅度文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param fineExtent 微调幅度文本
     */
    public void setFineExtent(String fineExtent){
        if (this.fineExtent==null){                                                    // 首次创建对象
            this.fineExtent=new RxStringWithSelect(fineExtent);
        }else {                                                                        // 非首次，更新值并标记选中
            this.fineExtent.setValue(fineExtent);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.fineExtent.setRxMsgSelect(true);                                      // 标记当前字段为选中
        }
    }

    /**
     * 获取微调开关状态
     * @return 带选中标记的微调开关
     */
    public RxBooleanWithSelect getFineSwitch(){return fineSwitch;}

    /**
     * 设置微调开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param fineSwitch 微调开关状态值
     */
    public void setFineSwitch(boolean fineSwitch){
        if (this.fineSwitch==null){                                                    // 首次创建对象
            this.fineSwitch=new RxBooleanWithSelect(fineSwitch);
        }else {                                                                        // 非首次，更新值并标记选中
            this.fineSwitch.setValue(fineSwitch);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.fineSwitch.setRxMsgSelect(true);                                      // 标记当前字段为选中
        }
    }

    /**
     * 清除所有字段的选中标记
     * <p>在每次属性变更前调用，确保只有变更的字段被标记为选中</p>
     */
    private void setAllUnSelect() {
        invert.setRxMsgSelect(false);                                                  // 清除反相选中标记
        couple.setRxMsgSelect(false);                                                  // 清除耦合选中标记
        probeType.setRxMsgSelect(false);                                               // 清除探头类型选中标记
        probeMultiple.setRxMsgSelect(false);                                            // 清除探头倍率选中标记
        bandWidth.setRxMsgSelect(false);                                                // 清除带宽选中标记
        bandWidthEdit.setRxMsgSelect(false);                                            // 清除带宽编辑选中标记
        imped.setRxMsgSelect(false);                                                   // 清除阻抗选中标记
        label.setRxMsgSelect(false);                                                   // 清除标签选中标记
        delay.setRxMsgSelect(false);                                                   // 清除延迟选中标记
        fineExtent.setRxMsgSelect(false);                                               // 清除微调幅度选中标记
        fineSwitch.setRxMsgSelect(false);                                               // 清除微调开关选中标记

    }

    /**
     * 返回对象的字符串表示
     * @return 包含所有通道参数的字符串
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RightMsgChannel{");                // 构建字符串
        sb.append("channelNumber=").append(channelNumber);                              // 通道编号
        sb.append(", chCheck=").append(chCheck);                                        // 开关状态
        sb.append(", invert=").append(invert);                                          // 反相状态
        sb.append(", couple=").append(couple);                                          // 耦合方式
        sb.append(", probeType=").append(probeType);                                    // 探头类型
        sb.append(", probeMultiple=").append(probeMultiple);                            // 探头倍率
        sb.append(", bandWidth=").append(bandWidth);                                    // 带宽限制
        sb.append(", bandWidthEdit=").append(bandWidthEdit);                            // 带宽编辑值
        sb.append(", imped=").append(imped);                                            // 输入阻抗
        sb.append(", label=").append(label);                                            // 通道标签
        sb.append(", delay=").append(delay);                                            // 通道延迟
        sb.append(", fineExtent=").append(fineExtent);                                  // 微调幅度
        sb.append(", fineSwitch=").append(fineSwitch);                                  // 微调开关
        sb.append(", probeMulScale=").append(probeMulScale);                            // 探头倍率变化比
        sb.append(", isFromEventBus=").append(isFromEventBus);                          // 事件来源标识
        sb.append(", isUpClick=").append(isUpClick);                                    // 调节方向标识
        sb.append('}');                                                                 // 结束括号
        return sb.toString();                                                           // 返回字符串
    }
}
