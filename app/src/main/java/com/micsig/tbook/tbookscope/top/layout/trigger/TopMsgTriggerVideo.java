package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.tbookscope.R; // 资源ID //
import com.micsig.tbook.tbookscope.util.App; // 应用工具 //
import com.micsig.tbook.ui.bean.RxStringWithSelect; // RxJava字符串选择Bean //
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 通道配置Bean类 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   视频触发器配置消息数据类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMsgTriggerVideo                                                  ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2017/5/17                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 封装视频触发器的触发源/极性/标准/触发类型/频率/行号参数                    ║
 * ║  2. 实现ITriggerDetail接口，支持克隆操作                                      ║
 * ║  3. 管理RxMsg选中状态，用于右侧面板识别变更参数                               ║
 * ║  4. 判断当前触发类型是否为行触发模式                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系: ITriggerDetail, TopBeanChannel, RxStringWithSelect, App, R        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMsgTriggerVideo implements ITriggerDetail { // 视频触发器配置消息数据类 //
    /** 触发源参数 */ // 触发源 //
    private TopBeanChannel triggerSource; // 触发源 //
    /** 极性参数，如正极性/负极性 */ // 极性 //
    private TopBeanChannel polar; // 极性 //
    /** 视频标准，如PAL/NTSC/SECAM */ // 视频标准 //
    private TopBeanChannel standard; // 视频标准 //
    /** 触发类型，如奇数场/偶数场/行号 */ // 触发类型 //
    private TopBeanChannel trigger; // 触发类型 //
    /** 频率 */ // 频率 //
    private TopBeanChannel frequency; // 频率 //
    /** 行号详情 */ // 行号 //
    private RxStringWithSelect lineDetail; // 行号详情 //

    /**
     * 克隆当前对象，创建深拷贝
     * @return Object 克隆后的对象
     * @throws CloneNotSupportedException 如果不支持克隆
     */
    @Override // 重写clone方法 //
    public Object clone() throws CloneNotSupportedException { // 克隆方法 //
        TopMsgTriggerVideo topMsgTriggerVideo = (TopMsgTriggerVideo) super.clone(); // 浅拷贝当前对象 //
        topMsgTriggerVideo.triggerSource = (TopBeanChannel) topMsgTriggerVideo.triggerSource.clone(); // 深拷贝触发源 //
        topMsgTriggerVideo.polar = (TopBeanChannel) topMsgTriggerVideo.polar.clone(); // 深拷贝极性 //
        topMsgTriggerVideo.standard = (TopBeanChannel) topMsgTriggerVideo.standard.clone(); // 深拷贝视频标准 //
        topMsgTriggerVideo.trigger = (TopBeanChannel) topMsgTriggerVideo.trigger.clone(); // 深拷贝触发类型 //
        topMsgTriggerVideo.frequency = (TopBeanChannel) topMsgTriggerVideo.frequency.clone(); // 深拷贝频率 //
        topMsgTriggerVideo.lineDetail = (RxStringWithSelect) topMsgTriggerVideo.lineDetail.clone(); // 深拷贝行号 //
        return topMsgTriggerVideo; // 返回深拷贝对象 //
    } // 方法结束 //

    /**
     * 获取触发源
     * @return TopBeanChannel 触发源
     */
    public TopBeanChannel getTriggerSource() { // 获取触发源 //
        return triggerSource; // 返回触发源 //
    } // 方法结束 //

    /**
     * 设置触发源
     * @param triggerSource 触发源
     */
    public void setTriggerSource(TopBeanChannel triggerSource) { // 设置触发源 //
        if (this.triggerSource == null) { // 如果触发源为空（首次设置） //
            this.triggerSource = triggerSource; // 直接设置 //
        } else { // 如果触发源已存在 //
            this.triggerSource = triggerSource; // 设置触发源 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.triggerSource.setRxMsgSelect(true); // 标记触发源为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取极性
     * @return TopBeanChannel 极性
     */
    public TopBeanChannel getPolar() { // 获取极性 //
        return polar; // 返回极性 //
    } // 方法结束 //

    /**
     * 设置极性
     * @param polar 极性
     */
    public void setPolar(TopBeanChannel polar) { // 设置极性 //
        if (this.polar == null) { // 如果极性为空（首次设置） //
            this.polar = polar; // 直接设置 //
        } else { // 如果极性已存在 //
            this.polar = polar; // 设置极性 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.polar.setRxMsgSelect(true); // 标记极性为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取视频标准
     * @return TopBeanChannel 视频标准
     */
    public TopBeanChannel getStandard() { // 获取视频标准 //
        return standard; // 返回视频标准 //
    } // 方法结束 //

    /**
     * 设置视频标准
     * @param standard 视频标准
     */
    public void setStandard(TopBeanChannel standard) { // 设置视频标准 //
        if (this.standard == null) { // 如果视频标准为空（首次设置） //
            this.standard = standard; // 直接设置 //
        } else { // 如果视频标准已存在 //
            this.standard = standard; // 设置视频标准 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.standard.setRxMsgSelect(true); // 标记视频标准为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取触发类型
     * @return TopBeanChannel 触发类型
     */
    public TopBeanChannel getTrigger() { // 获取触发类型 //
        return trigger; // 返回触发类型 //
    } // 方法结束 //

    /**
     * 设置触发类型
     * @param trigger 触发类型
     */
    public void setTrigger(TopBeanChannel trigger) { // 设置触发类型 //
        if (this.trigger == null) { // 如果触发类型为空（首次设置） //
            this.trigger = trigger; // 直接设置 //
        } else { // 如果触发类型已存在 //
            this.trigger = trigger; // 设置触发类型 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.trigger.setRxMsgSelect(true); // 标记触发类型为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 判断当前触发类型是否为行触发模式
     * @return boolean true表示是行触发模式
     */
    public boolean isTriggerLine() { // 判断是否为行触发 //
        String[] ss = App.get().getResources().getStringArray(R.array.triggerVideoTriggerMore); // 获取触发类型数组 //
        String s = ss[ss.length - 1]; // 获取最后一个元素（行触发） //
        return s.equals(trigger.getText()); // 比较当前触发类型文本 //
    } // 方法结束 //

    /**
     * 获取频率
     * @return TopBeanChannel 频率
     */
    public TopBeanChannel getFrequency() { // 获取频率 //
        return frequency; // 返回频率 //
    } // 方法结束 //

    /**
     * 设置频率
     * @param frequency 频率
     */
    public void setFrequency(TopBeanChannel frequency) { // 设置频率 //
        if (this.frequency == null) { // 如果频率为空（首次设置） //
            this.frequency = frequency; // 直接设置 //
        } else { // 如果频率已存在 //
            this.frequency = frequency; // 设置频率 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.frequency.setRxMsgSelect(true); // 标记频率为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 获取行号详情
     * @return RxStringWithSelect 行号详情
     */
    public RxStringWithSelect getLineDetail() { // 获取行号 //
        return lineDetail; // 返回行号 //
    } // 方法结束 //

    /**
     * 设置行号详情
     * @param lineDetail 行号字符串
     */
    public void setLineDetail(String lineDetail) { // 设置行号 //
        if (this.lineDetail == null) { // 如果行号为空（首次设置） //
            this.lineDetail = new RxStringWithSelect(lineDetail); // 创建新对象 //
        } else { // 如果行号已存在 //
            this.lineDetail.setValue(lineDetail); // 更新值 //
            setAllUnSelect(); // 清除所有RxMsg选中状态 //
            this.lineDetail.setRxMsgSelect(true); // 标记行号为选中 //
        } // 判断结束 //
    } // 方法结束 //

    /**
     * 清除所有参数的RxMsg选中状态
     */
    private void setAllUnSelect() { // 清除所有选中状态 //
        triggerSource.setRxMsgSelect(false); // 清除触发源选中 //
        polar.setRxMsgSelect(false); // 清除极性选中 //
        standard.setRxMsgSelect(false); // 清除视频标准选中 //
        trigger.setRxMsgSelect(false); // 清除触发类型选中 //
        frequency.setRxMsgSelect(false); // 清除频率选中 //
        lineDetail.setRxMsgSelect(false); // 清除行号选中 //
    } // 方法结束 //

    @Override // 重写toString //
    public String toString() { // 转字符串方法 //
        return "TopMsgTriggerVideo{" + // 返回类名 //
                "triggerSource=" + triggerSource + // 包含触发源 //
                ", polar=" + polar + // 包含极性 //
                ", standard=" + standard + // 包含视频标准 //
                ", trigger=" + trigger + // 包含触发类型 //
                ", frequency=" + frequency + // 包含频率 //
                ", lineDetail='" + lineDetail + '\'' + // 包含行号 //
                '}'; // 结束 //
    } // 方法结束 //
} // 类结束 //
