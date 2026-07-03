package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径为主界面中心区域消息通信模块

import android.graphics.Point; // 导入Point类,用于存储坐标点信息(x,y)

import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类,用于读取系统配置参数

/**
 * Created by yangj on 2017/6/30.
 */

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         MainCenterMsgChannels                                ║
 * ║                         中央通道消息数据传输类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║【模块定位】                                                                   ║
 * ║  位于main.maincenter包中,作为示波器主界面中央区域通道选择模块的消息载体      ║
 * ║  用于在不同组件之间传递通道选择状态和相关信息                                ║
 * ║【核心职责】                                                                   ║
 * ║  1. 封装当前选中通道号(chNO)                                                  ║
 * ║  2. 标识消息来源(EventBus或本地)                                              ║
 * ║  3. 标识消息发送者身份(self标识)                                              ║
 * ║  4. 存储通道在屏幕上的位置信息                                                ║
 * ║  5. 提供通道总数信息                                                          ║
 * ║【架构设计】                                                                   ║
 * ║  采用消息载体模式(Message Bean Pattern)                                       ║
 * ║  - 数据封装: 所有状态字段使用private修饰,通过getter/setter访问               ║
 * ║  - 来源标识: isFromEventBus区分消息来源避免循环处理                          ║
 * ║  - 身份标识: self标识防止自身消息重复处理                                     ║
 * ║【数据流向】                                                                   ║
 * ║  通道选择UI → MainCenterMsgChannels → RxBus → 其他模块                       ║
 * ║  其他模块 → RxBus → MainCenterMsgChannels → 通道选择UI(更新状态)             ║
 * ║【依赖关系】                                                                   ║
 * ║  使用方: MainLayoutCenterChannel(通道选择布局)                               ║
 * ║  通信依赖: RxBus(消息总线)                                                   ║
 * ║  数据依赖: CacheUtil(读取通道常量定义)                                        ║
 * ║【使用场景】                                                                   ║
 * ║  当用户在主界面点击切换通道时,创建此消息对象并通过RxBus发送                   ║
 * ║  其他模块接收此消息后更新UI状态或执行相应业务逻辑                             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainCenterMsgChannels {
    public static final int CH_NULL = -1; // 定义空通道常量,表示没有选中任何通道

//    public static final int CH1 = CacheUtil.CH1; // 通道1常量定义(已注释)
//    public static final int CH2 = CacheUtil.CH2; // 通道2常量定义(已注释)
//    public static final int CH3 = CacheUtil.CH3; // 通道3常量定义(已注释)
//    public static final int CH4 = CacheUtil.CH4; // 通道4常量定义(已注释)
//    public static final int CH5 = CacheUtil.CH5; // 通道5常量定义(已注释)
//    public static final int CH6 = CacheUtil.CH6; // 通道6常量定义(已注释)
//    public static final int CH7 = CacheUtil.CH7; // 通道7常量定义(已注释)
//    public static final int CH8 = CacheUtil.CH8; // 通道8常量定义(已注释)
//    public static final int MATH = CacheUtil.MATH; // 数学运算通道常量定义(已注释)
//    public static final int REF1 = CacheUtil.REF1; // 参考通道1常量定义(已注释)
//    public static final int REF2 = CacheUtil.REF2; // 参考通道2常量定义(已注释)
//    public static final int REF3 = CacheUtil.REF3; // 参考通道3常量定义(已注释)
//    public static final int REF4 = CacheUtil.REF4; // 参考通道4常量定义(已注释)
//    public static final int S1 = CacheUtil.REF4 + 1; // 串行解码通道1常量定义(已注释)
//    public static final int S2 = CacheUtil.REF4 + 2; // 串行解码通道2常量定义(已注释)

    /**
     * 从1到9是当前通道号,CH_NULL表示没有当前选中项
     * 通道号范围说明:
     * - 1-8: CH1-CH8物理通道
     * - 9-16: MATH1-MATH8数学运算通道
     * - 17-24: REF1-REF8参考通道
     * - 25-28: S1-S4串行解码通道
     */
    private int chNO; // 当前选中的通道号索引
    private boolean isFromEventBus = false; // 标识消息是否来自EventBus事件总线,用于避免循环处理
    private Point position = new Point(); // 通道在屏幕上的位置坐标(x,y),用于触摸事件定位
    private int chCount; // 当前系统中可用的通道总数
    /**
     * 此消息发送者是否是channelsLayout本身
     * 用于标识消息来源,防止自身发送的消息被自身重复处理
     */
    private boolean self = false; // 自身标识,当消息由通道选择布局自己发送时为true

    /**
     * 完整构造方法
     * 创建消息对象并初始化所有标识字段
     * @param chNO 通道号(1-28)
     * @param self 是否为自身发送的消息
     * @param isFromEventBus 是否来自EventBus
     */
    public MainCenterMsgChannels(int chNO, boolean self, boolean isFromEventBus) {
        this.self = self; // 设置自身标识,区分消息发送者
        this.chNO = chNO; // 设置当前选中通道号
        this.isFromEventBus = isFromEventBus; // 设置消息来源标识
    }

    /**
     * 获取消息来源标识
     * @return true表示来自EventBus,false表示来自本地
     */
    public boolean isFromEventBus() {
        return isFromEventBus; // 返回EventBus来源标识
    }

    /**
     * 设置消息来源标识
     * @param fromEventBus true表示来自EventBus,false表示来自本地
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus; // 更新EventBus来源标识
    }

    /**
     * 简化构造方法
     * 仅设置通道号,其他字段使用默认值
     * @param chNO 通道号(1-28)
     */
    public MainCenterMsgChannels(int chNO) {
        this.chNO = chNO; // 仅设置通道号,其他标识默认为false
    }

    /**
     * 获取自身标识
     * @return true表示消息由channelsLayout自身发送
     */
    public boolean isSelf() {
        return self; // 返回自身标识
    }

    /**
     * 设置自身标识
     * @param self true表示消息由channelsLayout自身发送
     */
    public void setSelf(boolean self) {
        this.self = self; // 更新自身标识
    }

    /**
     * 获取当前选中通道号
     * @return 通道号索引(1-28),或CH_NULL(-1)表示无选中
     */
    public int getChNO() {
        return chNO; // 返回通道号
    }

    /**
     * 设置当前选中通道号
     * @param chNO 通道号索引(1-28)
     */
    public void setChNO(int chNO) {
        this.chNO = chNO; // 更新通道号
    }

    /**
     * 获取通道位置坐标
     * @return Point对象,包含x和y坐标
     */
    public Point getPosition() {
        return position; // 返回位置坐标对象
    }

    /**
     * 设置通道位置坐标
     * @param position Point对象,包含x和y坐标
     */
    public void setPosition(Point position) {
        this.position = position; // 更新位置坐标
    }

    /**
     * 获取可用通道总数
     * @return 通道数量
     */
    public int getChCount() {
        return chCount; // 返回通道总数
    }

    /**
     * 设置可用通道总数
     * @param chCount 通道数量
     */
    public void setChCount(int chCount) {
        this.chCount = chCount; // 更新通道总数
    }
}