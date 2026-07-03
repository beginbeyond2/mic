package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

import java.io.Serializable;

/*
 * +--------------------------------------------------------------------------+
 * |                         串口总线配置消息体                                 |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— 串口配置全局消息数据载体           |
 * | 核心职责: 封装串口通道(S1~S4)的开关状态、协议类型及协议详情，             |
 * |          作为RxBus消息在各模块间传递                                       |
 * | 架构设计: 可序列化数据类，由 RightLayoutSerials 构建并通过 RxBus 广播，    |
 * |          消费方包括电平设置页面等模块                                      |
 * | 数据流向: RightLayoutSerials → RightMsgSerials → RxBus → 消费方          |
 * | 依赖关系: RxBooleanWithSelect, RightBeanSelect, ISerialsDetails           |
 * | 使用场景: 用户切换串口开关、选择协议类型、修改协议参数时，                |
 * |          构建此消息并通过RxBus通知相关模块更新                             |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/15.
 */

/**
 * 串口总线配置消息体
 * <p>
 * 封装串口通道(S1~S4)的使能开关、协议类型(UART/CAN/LIN/SPI/I2C/M429/M1553B)
 * 及对应协议的详细参数，作为RxBus事件消息在模块间传递。
 * </p>
 */
public class RightMsgSerials implements Serializable {

    /** 串口通道S1编号常量 */
    public static final int SERIALS_S1 = 1; // 串口通道S1编号
    /** 串口通道S2编号常量 */
    public static final int SERIALS_S2 = 2; // 串口通道S2编号
    /** 串口通道S3编号常量 */
    public static final int SERIALS_S3 = 3; // 串口通道S3编号
    /** 串口通道S4编号常量 */
    public static final int SERIALS_S4 = 4; // 串口通道S4编号

    /** S1通道开关状态 */
    private RxBooleanWithSelect serials1Check, serials2Check, serials3Check, serials4Check; // S1~S4通道开关状态
    /** 当前串口通道编号(1~4) */
    private int serialsNumber; // 当前串口通道编号
    /** 当前选中的串口协议类型 */
    private RightBeanSelect serialsType; // 当前选中的串口协议类型
    /** 当前协议的详细参数(多态引用) */
    private ISerialsDetails serialsDetails; // 当前协议的详细参数(多态引用)
    /** 本消息是否打开电平页面并设置 */
    private boolean openLevel = true;//本消息是否打开电平页面,并设置
    /** 标识此消息是否由EventBus事件触发 */
    private boolean isFromEventBus = false; // 标识此消息是否由EventBus事件触发

    /**
     * 判断此消息是否由EventBus事件触发
     *
     * @return true=来自EventBus事件，false=来自用户UI操作
     */
    public boolean isFromEventBus() {
        return isFromEventBus; // 返回EventBus来源标识
    }

    /**
     * 设置消息的EventBus来源标识
     *
     * @param fromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus; // 设置EventBus来源标识
    }

    /**
     * 根据串口编号获取对应通道的开关状态
     *
     * @param serialsNumber 串口编号(1~4)
     * @return 对应通道的开关状态封装对象
     */
    public RxBooleanWithSelect getSerialsCheck(int serialsNumber) {
        switch (serialsNumber) { // 根据串口编号分发
            case 2:
                return getSerials2Check(); // 返回S2开关状态
            case 3:
                return getSerials3Check(); // 返回S3开关状态
            case 4:
                return getSerials4Check(); // 返回S4开关状态
            case 1:
            default:
                return getSerials1Check(); // 默认返回S1开关状态
        }
    }

    /**
     * 获取S1通道开关状态
     *
     * @return S1通道开关状态封装对象
     */
    public RxBooleanWithSelect getSerials1Check() {
        return serials1Check; // 返回S1开关状态
    }

    /**
     * 获取S2通道开关状态
     *
     * @return S2通道开关状态封装对象
     */
    public RxBooleanWithSelect getSerials2Check() {
        return serials2Check; // 返回S2开关状态
    }

    /**
     * 获取S3通道开关状态
     *
     * @return S3通道开关状态封装对象
     */
    public RxBooleanWithSelect getSerials3Check() {
        return serials3Check; // 返回S3开关状态
    }

    /**
     * 获取S4通道开关状态
     *
     * @return S4通道开关状态封装对象
     */
    public RxBooleanWithSelect getSerials4Check() {
        return serials4Check; // 返回S4开关状态
    }


    /**
     * 根据串口编号设置对应通道的开关状态
     *
     * @param serialsNumber 串口编号(1~4)
     * @param state         开关状态，true=开启，false=关闭
     */
    public void setSerialsCheck(int serialsNumber, boolean state) {
        switch (serialsNumber) { // 根据串口编号分发
            case 2:
                setSerials2Check(state); // 设置S2开关状态
                break;
            case 3:
                setSerials3Check(state); // 设置S3开关状态
                break;
            case 4:
                setSerials4Check(state); // 设置S4开关状态
                break;
            case 1:
            default:
                setSerials1Check(state); // 默认设置S1开关状态
                break;
        }
    }

    /**
     * 设置S1通道开关状态
     *
     * @param serials1Check S1开关状态，true=开启，false=关闭
     */
    public void setSerials1Check(boolean serials1Check) {
        if (this.serials1Check == null) { // 首次赋值
            this.serials1Check = new RxBooleanWithSelect(serials1Check); // 创建新对象
        } else { // 非首次赋值
            this.serials1Check.setValue(serials1Check); // 更新值
            this.serials1Check.setRxMsgSelect(true); // 标记为RxMsg选中
        }
    }

    /**
     * 设置S2通道开关状态
     *
     * @param serials2Check S2开关状态，true=开启，false=关闭
     */
    public void setSerials2Check(boolean serials2Check) {
        if (this.serials2Check == null) { // 首次赋值
            this.serials2Check = new RxBooleanWithSelect(serials2Check); // 创建新对象
        } else { // 非首次赋值
            this.serials2Check.setValue(serials2Check); // 更新值
            this.serials2Check.setRxMsgSelect(true); // 标记为RxMsg选中
        }
    }

    /**
     * 设置S3通道开关状态
     *
     * @param serials3Check S3开关状态，true=开启，false=关闭
     */
    public void setSerials3Check(boolean serials3Check) {
        if (this.serials3Check == null) { // 首次赋值
            this.serials3Check = new RxBooleanWithSelect(serials3Check); // 创建新对象
        } else { // 非首次赋值
            this.serials3Check.setValue(serials3Check); // 更新值
            this.serials3Check.setRxMsgSelect(true); // 标记为RxMsg选中
        }
    }

    /**
     * 设置S4通道开关状态
     *
     * @param serials4Check S4开关状态，true=开启，false=关闭
     */
    public void setSerials4Check(boolean serials4Check) {
        if (this.serials4Check == null) { // 首次赋值
            this.serials4Check = new RxBooleanWithSelect(serials4Check); // 创建新对象
        } else { // 非首次赋值
            this.serials4Check.setValue(serials4Check); // 更新值
            this.serials4Check.setRxMsgSelect(true); // 标记为RxMsg选中
        }
    }

    /**
     * 判断是否需要打开电平页面
     *
     * @return true=打开电平页面，false=不打开
     */
    public boolean isOpenLevel() {
        return openLevel; // 返回电平页面打开标识
    }

    /**
     * 设置是否打开电平页面
     *
     * @param openLevel true=打开电平页面，false=不打开
     */
    public void setOpenLevel(boolean openLevel) {
        this.openLevel = openLevel; // 设置电平页面打开标识
    }

    /**
     * 获取当前串口通道编号
     *
     * @return 串口编号(1~4)
     */
    public int getSerialsNumber() {
        return serialsNumber; // 返回串口编号
    }

    /**
     * 设置当前串口通道编号
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber; // 设置串口编号
    }

    /**
     * 获取当前选中的串口协议类型
     *
     * @return 协议类型选择项
     */
    public RightBeanSelect getSerialsType() {
        return serialsType; // 返回协议类型
    }

    /**
     * 设置当前选中的串口协议类型
     *
     * @param serialsType 协议类型选择项
     */
    public void setSerialsType(RightBeanSelect serialsType) {
        if (this.serialsType == null) { // 首次赋值
            this.serialsType = serialsType; // 直接赋值
        } else { // 非首次赋值
            this.serialsType = serialsType; // 更新值
            this.serialsType.setRxMsgSelect(true); // 标记为RxMsg选中
        }
    }

    /**
     * 获取当前协议的详细参数
     *
     * @return 协议详情对象（多态引用）
     */
    public ISerialsDetails getSerialsDetails() {
        return serialsDetails; // 返回协议详情
    }

    /**
     * 设置当前协议的详细参数
     *
     * @param serialsDetails 协议详情对象
     */
    public void setSerialsDetails(ISerialsDetails serialsDetails) {
        this.serialsDetails = serialsDetails; // 设置协议详情
    }

    /**
     * 判断当前是否为S1通道
     *
     * @return true=S1通道
     */
    public boolean isSerials1() {
        return serialsNumber == SERIALS_S1; // 判断是否为S1
    }

    /**
     * 判断当前是否为S2通道
     *
     * @return true=S2通道
     */
    public boolean isSerials2() {
        return serialsNumber == SERIALS_S2; // 判断是否为S2
    }

    /**
     * 判断当前是否为S3通道
     *
     * @return true=S3通道
     */
    public boolean isSerials3() {
        return serialsNumber == SERIALS_S3; // 判断是否为S3
    }

    /**
     * 判断当前是否为S4通道
     *
     * @return true=S4通道
     */
    public boolean isSerials4() {
        return serialsNumber == SERIALS_S4; // 判断是否为S4
    }

    /**
     * 返回消息体的字符串表示，用于调试
     *
     * @return 包含串口编号、协议类型和详情的字符串
     */
    @Override
    public String toString() {
        return "RightMsgSerials{" + // 构建字符串
                "serialsNumber=" + serialsNumber + // 串口编号
                ", serialsType=" + serialsType + // 协议类型
                ", serialsDetails=" + serialsDetails + // 协议详情
                '}';
    }
}
