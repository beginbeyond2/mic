package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                            RightMsgRef                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 4通道Ref消息封装类（旧版）                            ║
 * ║ 核心职责：封装4个参考通道的开关状态和回调路径，用于UI与业务层通信               ║
 * ║ 架构设计：纯数据消息Bean，配合RightLayoutRef(4通道版)使用                      ║
 * ║ 数据流向：RightLayoutRef → RxBus → 订阅方                                   ║
 * ║ 依赖关系：RxBooleanWithSelect, RxStringWithSelect                           ║
 * ║ 使用场景：4通道Ref模式下，传递参考通道的开关和回调路径变更消息                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/12.
 * 4通道参考通道消息封装类（旧版，仅支持R1-R4）
 * <p>
 * 封装4个参考通道的开关状态和回调路径：
 * - r1Checked/r2Checked/r3Checked/r4Checked：各通道开关状态
 * - r1Recall/r2Recall/r3Recall/r4Recall：各通道回调路径
 * </p>
 * <p>注意：此类为4通道旧版，8通道版本请使用 {@link RightMsgRefForEight}</p>
 */
public class RightMsgRef {
    /** R1通道开关状态（带选中标记） */
    private RxBooleanWithSelect refChecked;
    /** R1通道开关状态（带选中标记） */
    private RxBooleanWithSelect r1Checked;
    /** R1通道回调路径（带选中标记） */
    private RxStringWithSelect r1Recall;
    /** R2通道开关状态（带选中标记） */
    private RxBooleanWithSelect r2Checked;
    /** R2通道回调路径（带选中标记） */
    private RxStringWithSelect r2Recall;
    /** R3通道开关状态（带选中标记） */
    private RxBooleanWithSelect r3Checked;
    /** R3通道回调路径（带选中标记） */
    private RxStringWithSelect r3Recall;
    /** R4通道开关状态（带选中标记） */
    private RxBooleanWithSelect r4Checked;
    /** R4通道回调路径（带选中标记） */
    private RxStringWithSelect r4Recall;
    /** 参考通道编号，默认1 */
    private int refChannelNumber = 1;

    /**
     * 获取参考通道编号
     * @return 参考通道编号
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
     * 获取参考通道总开关状态
     * @return 带选中标记的开关状态
     */
    public RxBooleanWithSelect getRefChecked() {
        return refChecked;
    }

    /**
     * 设置参考通道总开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param refChecked 开关状态值
     */
    public void setRefChecked(boolean refChecked) {
        if (this.refChecked == null) {                                                 // 首次创建对象
            this.refChecked = new RxBooleanWithSelect(refChecked);
        } else {                                                                       // 非首次，更新值并标记选中
            this.refChecked.setValue(refChecked);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.refChecked.setRxMsgSelect(true);                                      // 标记当前字段为选中
        }
    }

    /**
     * 获取R1通道开关状态
     * @return 带选中标记的R1开关状态
     */
    public RxBooleanWithSelect getR1Checked() {
        return r1Checked;
    }

    /**
     * 设置R1通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r1Checked R1开关状态值
     */
    public void setR1Checked(boolean r1Checked) {
        if (this.r1Checked == null) {                                                  // 首次创建对象
            this.r1Checked = new RxBooleanWithSelect(r1Checked);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r1Checked.setValue(r1Checked);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r1Checked.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取R2通道开关状态
     * @return 带选中标记的R2开关状态
     */
    public RxBooleanWithSelect getR2Checked() {
        return r2Checked;
    }

    /**
     * 设置R2通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r2Checked R2开关状态值
     */
    public void setR2Checked(boolean r2Checked) {
        if (this.r2Checked == null) {                                                  // 首次创建对象
            this.r2Checked = new RxBooleanWithSelect(r2Checked);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r2Checked.setValue(r2Checked);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r2Checked.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取R3通道开关状态
     * @return 带选中标记的R3开关状态
     */
    public RxBooleanWithSelect getR3Checked() {
        return r3Checked;
    }

    /**
     * 设置R3通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r3Checked R3开关状态值
     */
    public void setR3Checked(boolean r3Checked) {
        if (this.r3Checked == null) {                                                  // 首次创建对象
            this.r3Checked = new RxBooleanWithSelect(r3Checked);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r3Checked.setValue(r3Checked);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r3Checked.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取R4通道开关状态
     * @return 带选中标记的R4开关状态
     */
    public RxBooleanWithSelect getR4Checked() {
        return r4Checked;
    }

    /**
     * 设置R4通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r4Checked R4开关状态值
     */
    public void setR4Checked(boolean r4Checked) {
        if (this.r4Checked == null) {                                                  // 首次创建对象
            this.r4Checked = new RxBooleanWithSelect(r4Checked);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r4Checked.setValue(r4Checked);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r4Checked.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取R1通道回调路径
     * @return 带选中标记的R1回调路径
     */
    public RxStringWithSelect getR1Recall() {
        return r1Recall;
    }

    /**
     * 设置R1通道回调路径
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r1Recall R1回调路径
     */
    public void setR1Recall(String r1Recall) {
        if (this.r1Recall == null) {                                                   // 首次创建对象
            this.r1Recall = new RxStringWithSelect(r1Recall);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r1Recall.setValue(r1Recall);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r1Recall.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 获取R2通道回调路径
     * @return 带选中标记的R2回调路径
     */
    public RxStringWithSelect getR2Recall() {
        return r2Recall;
    }

    /**
     * 设置R2通道回调路径
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r2Recall R2回调路径
     */
    public void setR2Recall(String r2Recall) {
        if (this.r2Recall == null) {                                                   // 首次创建对象
            this.r2Recall = new RxStringWithSelect(r2Recall);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r2Recall.setValue(r2Recall);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r2Recall.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 获取R3通道回调路径
     * @return 带选中标记的R3回调路径
     */
    public RxStringWithSelect getR3Recall() {
        return r3Recall;
    }

    /**
     * 设置R3通道回调路径
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r3Recall R3回调路径
     */
    public void setR3Recall(String r3Recall) {
        if (this.r3Recall == null) {                                                   // 首次创建对象
            this.r3Recall = new RxStringWithSelect(r3Recall);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r3Recall.setValue(r3Recall);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r3Recall.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 获取R4通道回调路径
     * @return 带选中标记的R4回调路径
     */
    public RxStringWithSelect getR4Recall() {
        return r4Recall;
    }

    /**
     * 设置R4通道回调路径
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param r4Recall R4回调路径
     */
    public void setR4Recall(String r4Recall) {
        if (this.r4Recall == null) {                                                   // 首次创建对象
            this.r4Recall = new RxStringWithSelect(r4Recall);
        } else {                                                                       // 非首次，更新值并标记选中
            this.r4Recall.setValue(r4Recall);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.r4Recall.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 判断所有通道的开关状态是否与给定值一致
     * @param r1 R1目标状态
     * @param r2 R2目标状态
     * @param r3 R3目标状态
     * @param r4 R4目标状态
     * @return true=全部一致, false=存在不一致
     */
    public boolean isAllCheckEquals(boolean r1, boolean r2, boolean r3, boolean r4) {
        return r1 == r1Checked.isValue() && r2 == r2Checked.isValue() && r3 == r3Checked.isValue() && r4 == r4Checked.isValue(); // 逐通道比较
    }

    /**
     * 清除所有字段的选中标记
     * <p>在每次属性变更前调用，确保只有变更的字段被标记为选中</p>
     */
    private void setAllUnSelect() {
        r1Checked.setRxMsgSelect(false);                                               // 清除R1开关选中标记
        r2Checked.setRxMsgSelect(false);                                               // 清除R2开关选中标记
        r3Checked.setRxMsgSelect(false);                                               // 清除R3开关选中标记
        r4Checked.setRxMsgSelect(false);                                               // 清除R4开关选中标记
        r1Recall.setRxMsgSelect(false);                                                // 清除R1回调选中标记
        r2Recall.setRxMsgSelect(false);                                                // 清除R2回调选中标记
        r3Recall.setRxMsgSelect(false);                                                // 清除R3回调选中标记
        r4Recall.setRxMsgSelect(false);                                                // 清除R4回调选中标记
    }

    /**
     * 返回对象的字符串表示
     * @return 包含所有通道状态的字符串
     */
    @Override
    public String toString() {
        return "RightMsgRef{" +
                "r1Checked=" + r1Checked +
                ", r1Recall='" + r1Recall + '\'' +
                ", r2Checked=" + r2Checked +
                ", r2Recall='" + r2Recall + '\'' +
                ", r3Checked=" + r3Checked +
                ", r3Recall='" + r3Recall + '\'' +
                ", r4Checked=" + r4Checked +
                ", r4Recall='" + r4Recall + '\'' +
                '}';
    }
}
