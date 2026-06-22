package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包，包含协议解析、数据结构定义和缓存管理

import com.micsig.base.Logger; // 日志工具类，用于调试输出
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂类，用于DCL单例的同步锁对象
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 基于RxJava的全局事件总线，用于组件间解耦通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // Rx事件枚举，定义所有事件类型常量
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay; // 顶部显示消息载体，封装显示详情
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix; // 文本混合显示详情，携带S1-S4选中状态
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，持久化存储UI参数

import java.util.HashMap; // 哈希表，用于存储通道选中状态映射

import io.reactivex.rxjava3.functions.Consumer; // RxJava3消费者接口，用于订阅RxBus事件

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                         SerialsTxtMixUtils                                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 串口文本混合显示工具类                                │
 * │ 核心职责：管理S1-S4串口通道的文本列表混合显示选中状态，提供状态查询接口       │
 * │ 架构设计：DCL单例模式 + RxBus事件驱动，通过监听UI顶部菜单的显示设置变化，    │
 * │          实时更新各通道的选中状态，同时从CacheUtil持久化缓存中恢复状态        │
 * │ 数据流：  UI(TopLayoutDisplayTxtMix) → RxBus(TOPLAYOUT_DISPLAY事件)         │
 * │          → consumerTopSlipTitle(更新serialsCheckMap)                        │
 * │          → getSerialsCheckMap()(供文本列表过滤读取)                          │
 * │ 依赖关系：依赖RxBus(事件总线)、RxEnum(事件类型)、TopMsgDisplayTxtMix(消息体) │
 * │          CacheUtil(持久化缓存)、ChannelFactory(同步锁)                      │
 * │          被串口文本列表渲染组件调用，用于过滤混合显示的通道数据               │
 * │ 使用场景：用户在"显示→TXT Mix"菜单中勾选S1-S4通道的CheckBox时，            │
 * │          该工具类捕获选中状态变化，供文本列表组件查询哪些通道需要混合显示     │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * 串口文本混合工具类。DCL单例模式，管理S1-S4四个串口通道在文本列表混合显示
 * 模式下的选中状态。通过RxBus监听UI顶部菜单的显示设置变化事件，实时更新
 * 各通道的选中状态映射表；同时从CacheUtil持久化缓存中读取和校验状态，
 * 确保应用重启后状态一致。
 *
 * @author limh
 * @since 2024/8/9
 */
public class SerialsTxtMixUtils {

    /** 串口通道选中状态映射表，键为通道标识("S1"/"S2"/"S3"/"S4")，
     *  值为该通道是否被选中参与文本混合显示（true=选中，false=未选中） */
    private static final HashMap<String, Boolean> serialsCheckMap = new HashMap<>(); // 通道选中状态表，静态全局共享，取值：{S1→true/false, S2→true/false, S3→true/false, S4→true/false}

    /** 单例实例引用，volatile修饰保证多线程下的可见性 */
    private static volatile SerialsTxtMixUtils instance = null; // 单例实例，volatile保证线程可见性，初始null

    /** S1通道的映射键名 */
    private static final String S1Key = "S1"; // S1通道键名，取值："S1"

    /** S2通道的映射键名 */
    private static final String S2Key = "S2"; // S2通道键名，取值："S2"

    /** S3通道的映射键名 */
    private static final String S3Key = "S3"; // S3通道键名，取值："S3"

    /** S4通道的映射键名 */
    private static final String S4Key = "S4"; // S4通道键名，取值："S4"

    /**
     * 获取串口通道选中状态映射表。
     *
     * 功能：先调用checkAndUpdateMap()从CacheUtil持久化缓存中校验并更新状态，
     *       然后返回serialsCheckMap引用，供调用方查询各通道的选中状态。
     *
     * @return 通道选中状态映射表，键为"S1"/"S2"/"S3"/"S4"，
     *         值为Boolean（true=选中参与混合显示，false=未选中）
     *
     * 业务意义：文本列表渲染组件在绘制混合显示列表时，通过此方法获取
     *           各通道的选中状态，只显示被选中通道的解码文本数据。
     */
    public HashMap<String, Boolean> getSerialsCheckMap() { // 获取通道选中状态映射表
        checkAndUpdateMap(); // 从CacheUtil校验并更新状态，确保与持久化缓存一致
        return serialsCheckMap; // 返回状态映射表引用
    }

    /**
     * 获取SerialsTxtMixUtils的单例实例（DCL双重检查锁定模式）。
     *
     * 功能：通过双重检查锁定确保多线程环境下只创建一个实例。
     *       首次创建实例时，先调用initControl()注册RxBus事件监听，
     *       再创建实例。同步锁使用ChannelFactory.class作为锁对象。
     *
     * 注意：同步锁使用ChannelFactory.class而非本类class，
     *       这是一种跨类锁策略，确保与ChannelFactory的初始化互斥。
     *
     * @return SerialsTxtMixUtils单例实例
     */
    public static SerialsTxtMixUtils getInstance() { // 获取单例实例
        if (instance == null) { // 第一次检查：实例是否为空，避免不必要的同步
            synchronized (ChannelFactory.class) { // 使用ChannelFactory.class作为同步锁，跨类互斥
                if (instance == null) { // 第二次检查：锁内再次判断，防止多线程重复创建
                    initControl(); // 初始化事件监听，注册RxBus订阅
                    instance = new SerialsTxtMixUtils(); // 创建单例实例
                }
            }
        }
        return instance; // 返回单例实例
    }


    /**
     * 初始化事件监听控制。
     *
     * 功能：订阅RxBus的TOPLAYOUT_DISPLAY事件，当UI顶部菜单的显示设置
     *       发生变化时，consumerTopSlipTitle回调会被触发，更新serialsCheckMap。
     *
     * 业务意义：建立UI事件驱动的状态更新通道，确保用户在顶部菜单中
     *           修改TXT Mix选项后，状态能实时同步到本工具类。
     */
    private static void initControl() { // 初始化RxBus事件监听
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle); // 订阅顶部显示设置变化事件，绑定消费者回调
    }


    /**
     * RxBus事件消费者，处理顶部显示设置变化事件。
     *
     * 功能：当收到TOPLAYOUT_DISPLAY事件时，检查消息体是否为TopMsgDisplayTxtMix类型，
     *       如果是则提取S1-S4四个通道的选中状态，更新到serialsCheckMap中。
     *
     * 业务意义：用户在"显示→TXT Mix"菜单中勾选/取消勾选通道CheckBox时，
     *           TopLayoutDisplayTxtMix通过RxBus发送TopMsgDisplay消息，
     *           本消费者接收消息并更新内部状态，实现UI与数据的实时同步。
     */
    private static final Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() { // 顶部显示事件消费者
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception { // 处理接收到的显示消息
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) { // 消息非空且详情为文本混合类型
                //这里判断组合状态  S1&S2&S3&S4
                TopMsgDisplayTxtMix topMsgDisplayTxtMix = (TopMsgDisplayTxtMix) topMsgDisplay.getDisplayDetail(); // 强制转换为文本混合详情
                boolean s1Check = topMsgDisplayTxtMix.isS1Select(); // 获取S1通道选中状态，取值：true(选中)/false(未选中)
                boolean s2Check = topMsgDisplayTxtMix.isS2Select(); // 获取S2通道选中状态，取值：true(选中)/false(未选中)
                boolean s3Check = topMsgDisplayTxtMix.isS3Select(); // 获取S3通道选中状态，取值：true(选中)/false(未选中)
                boolean s4Check = topMsgDisplayTxtMix.isS4Select(); // 获取S4通道选中状态，取值：true(选中)/false(未选中)
                serialsCheckMap.put(S1Key, s1Check); // 更新S1通道选中状态到映射表
                serialsCheckMap.put(S2Key, s2Check); // 更新S2通道选中状态到映射表
                serialsCheckMap.put(S3Key, s3Check); // 更新S3通道选中状态到映射表
                serialsCheckMap.put(S4Key, s4Check); // 更新S4通道选中状态到映射表
                Logger.d("limh SerialsTxtMixUtils", "serialsCheckMap:" + serialsCheckMap.toString()); // 输出调试日志，打印当前所有通道选中状态
            }
        }
    };

    /**
     * 从CacheUtil持久化缓存中校验并更新serialsCheckMap。
     *
     * 功能：逐个读取S1-S4通道在CacheUtil中的持久化选中状态，
     *       与serialsCheckMap中的当前值比较，如果不一致则更新。
     *       这确保了在RxBus事件未到达时（如应用重启、页面切换后首次查询），
     *       状态映射表也能反映最新的持久化值。
     *
     * 业务意义：CacheUtil通过putMap在用户操作CheckBox时持久化选中状态，
     *           此方法在每次getSerialsCheckMap()调用时执行校验，
     *           保证即使RxBus事件丢失，状态也能从持久化缓存中恢复。
     */
    //判断并更新map
    private void checkAndUpdateMap() { // 从持久化缓存校验并更新选中状态
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1); // 从缓存读取S1选中状态，取值：true/false
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2); // 从缓存读取S2选中状态，取值：true/false
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3); // 从缓存读取S3选中状态，取值：true/false
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4); // 从缓存读取S4选中状态，取值：true/false
        if (Boolean.TRUE.equals(serialsCheckMap.get(S1Key)) != s1Check) { // S1状态不一致（注意：map中null需用Boolean.TRUE.equals安全比较）
            serialsCheckMap.put(S1Key, s1Check); // 更新S1选中状态为缓存值
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S2Key)) != s2Check) { // S2状态不一致
            serialsCheckMap.put(S2Key, s2Check); // 更新S2选中状态为缓存值
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S3Key)) != s3Check) { // S3状态不一致
            serialsCheckMap.put(S3Key, s3Check); // 更新S3选中状态为缓存值
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S4Key)) != s4Check) { // S4状态不一致
            serialsCheckMap.put(S4Key, s4Check); // 更新S4选中状态为缓存值
        }
    }

}
