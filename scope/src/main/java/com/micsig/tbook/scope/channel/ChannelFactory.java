package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块



import com.micsig.tbook.hardware.Hardware;  // 导入Hardware类：硬件操作接口
import com.micsig.tbook.hardware.HardwareProduct;  // 导入HardwareProduct类：硬件产品型号常量
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.math.MathWave;  // 导入MathWave类：数学波形
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入VerticalAxis类：垂直轴管理

import java.util.ArrayList;  // 导入ArrayList类：动态数组
import java.util.List;  // 导入List接口：列表接口
import java.util.Objects;  // 导入Objects类：对象工具类
import java.util.function.Consumer;  // 导入Consumer接口：消费者函数式接口

/**
 * 通道工厂类 - 通道实例管理与层级控制
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：基础设施层 - 通道工厂</li>
 *   <li>设计模式：单例模式 + 工厂模式 + 迭代器模式</li>
 *   <li>职责类型：通道实例创建、通道类型判断、层级管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>创建和管理所有通道实例（模拟/数学/参考/串口）</li>
 *   <li>提供通道类型判断方法</li>
 *   <li>管理通道显示层级（Z-Order）</li>
 *   <li>提供通道遍历接口（forEach系列方法）</li>
 * </ul>
 * 
 * <p><b>通道类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   通道索引分配                                                            │
 * │                                                                          │
 * │   模拟通道（Channel）：                                                   │
 * │   ├── CH1 = 0        通道1                                               │
 * │   ├── CH2 = 1        通道2                                               │
 * │   ├── CH3 = 2        通道3                                               │
 * │   ├── CH4 = 3        通道4                                               │
 * │   ├── CH5 = 4        通道5                                               │
 * │   ├── CH6 = 5        通道6                                               │
 * │   ├── CH7 = 6        通道7                                               │
 * │   ├── CH8 = 7        通道8                                               │
 * │   └── CH_MAX = 8     模拟通道上限                                        │
 * │                                                                          │
 * │   数学通道（MathChannel）：                                               │
 * │   ├── MATH1 = 8      数学通道1                                          │
 * │   ├── MATH2 = 9      数学通道2                                          │
 * │   ├── ...                                                               │
 * │   ├── MATH8 = 15     数学通道8                                          │
 * │   └── MATH_MAX = 16  数学通道上限                                        │
 * │                                                                          │
 * │   参考通道（RefChannel）：                                                │
 * │   ├── REF1 = 16      参考通道1                                          │
 * │   ├── REF2 = 17      参考通道2                                          │
 * │   ├── ...                                                               │
 * │   ├── REF8 = 23      参考通道8                                          │
 * │   └── REF_MAX = 24   参考通道上限                                        │
 * │                                                                          │
 * │   串口通道（SerialChannel）：                                             │
 * │   ├── S1 = 24        串口通道1                                          │
 * │   ├── S2 = 25        串口通道2                                          │
 * │   ├── S3 = 26        串口通道3                                          │
 * │   ├── S4 = 27        串口通道4                                          │
 * │   └── SERIAL_MAX = 28 串口通道上限                                       │
 * │                                                                          │
 * │   总通道数：CHANNEL_CNT = 28                                             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>通道层级管理：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   通道层级（Z-Order）管理                                                 │
 * │                                                                          │
 * │   channelList（按Z-Order排序，索引0为最顶层）                             │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │  [0]     CH1         zOrder=28  (最顶层，当前激活)               │   │
 * │   │  [1]     MATH1       zOrder=27                                  │   │
 * │   │  [2]     CH2         zOrder=26                                  │   │
 * │   │  [3]     REF1        zOrder=25                                  │   │
 * │   │  ...                                                              │   │
 * │   │  [27]    S4          zOrder=1   (最底层)                        │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   层级操作：                                                              │
 * │   toTopLayer(channel)    将通道移到最顶层                               │
 * │   toBottomLayer(channel) 将通道移到最底层                               │
 * │                                                                          │
 * │   使用场景：                                                              │
 * │   - 打开通道时，自动移到顶层并激活                                       │
 * │   - 关闭通道时，自动移到底层                                             │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>通道遍历接口：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   forEach遍历方法                                                         │
 * │                                                                          │
 * │   forEachCh(consumer)     遍历所有模拟通道                               │
 * │   forEachMath(consumer)   遍历所有数学通道                               │
 * │   forEachRef(consumer)    遍历所有参考通道                               │
 * │   forEachSerial(consumer) 遍历所有串口通道                               │
 * │                                                                          │
 * │   使用示例：                                                              │
 * │   ChannelFactory.forEachCh(ch -> {                                      │
 * │       if(ch.isOpen()) {                                                  │
 * │           ch.setPos(0);                                                  │
 * │       }                                                                  │
 * │   });                                                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Hardware（硬件接口，获取通道数量）</li>
 *   <li>依赖：Channel、MathChannel、RefChannel、SerialChannel（通道实现类）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see Channel 模拟通道
 * @see MathChannel 数学通道
 * @see RefChannel 参考通道
 * @see SerialChannel 串口通道
 */
public class ChannelFactory {

    /** 日志标签 */
    private static final String TAG = "ChannelFactory";  // 日志输出标签
    
    /** 单例实例：使用volatile保证多线程可见性 */
    private static volatile ChannelFactory instance = null;  // 单例实例引用

    /**
     * 获取单例实例
     * 
     * <p>使用双重检查锁定模式确保线程安全。
     * 
     * @return ChannelFactory单例实例
     */
    public static ChannelFactory getInstance() {

        if (instance == null) {  // 第一次检查
            synchronized (ChannelFactory.class) {  // 同步锁
                if (instance == null) {  // 第二次检查
                    initChFactory();  // 初始化通道工厂配置
                    instance = new ChannelFactory();  // 创建实例
                }
            }
        }
        return instance;  // 返回实例
    }

    /**
     * 初始化通道工厂配置
     * 
     * <p>根据硬件配置初始化通道数量。
     */
    public static void initChFactory(){
        chNums = Hardware.getInstance().getChNum();  // 从硬件获取模拟通道数量

        mathChNums = MATH_CNT;  // 数学通道数量
        refChNums = REF_CNT;  // 参考通道数量
        serialChNums = SERIAL_CNT;  // 串口通道数量

        maxChIdx = chNums + CH1;  // 计算模拟通道最大索引
        maxMathIdx = mathChNums + MATH1;  // 计算数学通道最大索引
        maxRefIdx = refChNums + REF1;  // 计算参考通道最大索引
        maxSerialIdx = serialChNums + S1;  // 计算串口通道最大索引
    }

    // ==================== 模拟通道索引常量 ====================

    /** 通道1索引 */
    public static final int CH1 = 0;  // 通道1
    /** 通道2索引 */
    public static final int CH2 = CH1 + 1;  // 通道2
    /** 通道3索引 */
    public static final int CH3 = CH2 + 1;  // 通道3
    /** 通道4索引 */
    public static final int CH4 = CH3 + 1;  // 通道4
    /** 通道5索引 */
    public static final int CH5 = CH4 + 1;  // 通道5
    /** 通道6索引 */
    public static final int CH6 = CH5 + 1;  // 通道6
    /** 通道7索引 */
    public static final int CH7 = CH6 + 1;  // 通道7
    /** 通道8索引 */
    public static final int CH8 = CH7 + 1;  // 通道8

    /** 模拟通道上限索引 */
    public static final int CH_MAX = CH8 + 1;  // 模拟通道上限
    /** 模拟通道数量 */
    public static final int CH_CNT = CH_MAX - CH1;  // 模拟通道数量


    // ==================== 数学通道索引常量 ====================

    /** 数学通道1索引 */
    public static final int MATH1 = CH_MAX;  // 数学通道1
    /** 数学通道2索引 */
    public static final int MATH2 = MATH1 + 1;  // 数学通道2
    /** 数学通道3索引 */
    public static final int MATH3 = MATH2 + 1;  // 数学通道3
    /** 数学通道4索引 */
    public static final int MATH4 = MATH3 + 1;  // 数学通道4
    /** 数学通道5索引 */
    public static final int MATH5 = MATH4 + 1;  // 数学通道5
    /** 数学通道6索引 */
    public static final int MATH6 = MATH5 + 1;  // 数学通道6
    /** 数学通道7索引 */
    public static final int MATH7 = MATH6 + 1;  // 数学通道7
    /** 数学通道8索引 */
    public static final int MATH8 = MATH7 + 1;  // 数学通道8

    /** 数学通道上限索引 */
    public static final int MATH_MAX = MATH8 + 1;  // 数学通道上限
    /** 数学通道数量 */
    public static final int MATH_CNT = MATH_MAX - MATH1;  // 数学通道数量

    // ==================== 参考通道索引常量 ====================

    /** 参考通道1索引 */
    public static final int REF1 = MATH_MAX;  // 参考通道1
    /** 参考通道2索引 */
    public static final int REF2 = REF1 + 1;  // 参考通道2
    /** 参考通道3索引 */
    public static final int REF3 = REF2 + 1;  // 参考通道3
    /** 参考通道4索引 */
    public static final int REF4 = REF3 + 1;  // 参考通道4
    /** 参考通道5索引 */
    public static final int REF5 = REF4 + 1;  // 参考通道5
    /** 参考通道6索引 */
    public static final int REF6 = REF5 + 1;  // 参考通道6
    /** 参考通道7索引 */
    public static final int REF7 = REF6 + 1;  // 参考通道7
    /** 参考通道8索引 */
    public static final int REF8 = REF7 + 1;  // 参考通道8
    /** 参考通道上限索引 */
    public static final int REF_MAX = REF8 + 1;  // 参考通道上限
    /** 参考通道数量 */
    public static final int REF_CNT = REF_MAX - REF1;  // 参考通道数量

    // ==================== 串口通道索引常量 ====================

    /** 串口通道1索引 */
    public static final int S1 = REF_MAX;  // 串口通道1
    /** 串口通道2索引 */
    public static final int S2 = S1 + 1;  // 串口通道2
    /** 串口通道3索引 */
    public static final int S3 = S2 + 1;  // 串口通道3
    /** 串口通道4索引 */
    public static final int S4 = S3 + 1;  // 串口通道4
    /** 串口通道上限索引 */
    private static final int SERIAL_MAX = S4 + 1;  // 串口通道上限
    /** 串口通道数量 */
    public static final int SERIAL_CNT = SERIAL_MAX - S1;  // 串口通道数量
    /** 总通道数量 */
    public static final int CHANNEL_CNT = CH_CNT + MATH_CNT + REF_CNT + SERIAL_CNT;  // 总通道数量



    /** 通道列表：按Z-Order排序，索引0为最顶层 */
    private final List<IChannel> channelList = new ArrayList<>();  // 通道列表
    /** 通道数组：按索引直接访问 */
    private final IChannel [] channels = new IChannel[CHANNEL_CNT];  // 通道数组


    /** 参考通道颜色数组 */
    private static final int [] refColor = new int[REF_CNT];  // 参考通道颜色

    /** 数学通道颜色数组 */
    private static final int [] mathColor = new int[MATH_CNT];  // 数学通道颜色


    /** 模拟通道数量（实际硬件支持的数量） */
    private static int chNums = CH_CNT;  // 模拟通道数量
    /** 数学通道数量 */
    private static int mathChNums = MATH_CNT;  // 数学通道数量
    /** 参考通道数量 */
    private static int refChNums = REF_CNT;  // 参考通道数量

    /** 串口通道数量 */
    private static int serialChNums = SERIAL_CNT;  // 串口通道数量


    /** 模拟通道最大索引 */
    private static int maxChIdx = CH_MAX;  // 模拟通道最大索引
    /** 数学通道最大索引 */
    private static int maxMathIdx = MATH_MAX;  // 数学通道最大索引
    /** 参考通道最大索引 */
    private static int maxRefIdx = REF_MAX;  // 参考通道最大索引
    /** 串口通道最大索引 */
    private static int maxSerialIdx = SERIAL_MAX;  // 串口通道最大索引


    /**
     * 获取模拟通道最大索引
     * 
     * @return 最大索引值
     */
    public static int getMaxChIdx(){
        return maxChIdx;  // 返回模拟通道最大索引
    }

    /**
     * 获取数学通道最大索引
     * 
     * @return 最大索引值
     */
    public static int getMaxMathIdx(){
        return maxMathIdx;  // 返回数学通道最大索引
    }

    /**
     * 获取参考通道最大索引
     * 
     * @return 最大索引值
     */
    public static int getMaxRefIdx(){
        return maxRefIdx;  // 返回参考通道最大索引
    }

    /**
     * 获取串口通道最大索引
     * 
     * @return 最大索引值
     */
    public static int getMaxSerialIdx(){
        return maxSerialIdx;  // 返回串口通道最大索引
    }

    /**
     * 获取模拟通道数量
     * 
     * @return 通道数量
     */
    public static int getChNums(){
        return chNums;  // 返回模拟通道数量
    }

    /**
     * 获取数学通道数量
     * 
     * @return 通道数量
     */
    public static int getMathChNums(){
        return mathChNums;  // 返回数学通道数量
    }

    /**
     * 获取参考通道数量
     * 
     * @return 通道数量
     */
    public static int getRefChNums(){
        return refChNums;  // 返回参考通道数量
    }

    /**
     * 获取串口通道数量
     * 
     * @return 通道数量
     */
    public static int getSerialChNums(){
        return serialChNums;  // 返回串口通道数量
    }

    // ==================== 通道遍历方法 ====================

    /**
     * 遍历所有模拟通道
     * 
     * @param action 对每个通道执行的操作
     */
    public static void forEachCh(Consumer<Channel> action){
        Objects.requireNonNull(action);  // 检查action非空
        Channel channel;  // 通道临时变量
        for(int i=ChannelFactory.CH1;i<maxChIdx;i++){  // 遍历模拟通道
            channel = ChannelFactory.getDynamicChannel(i);  // 获取通道
            if(channel != null) {  // 检查通道是否有效
                action.accept(channel);  // 执行操作
            }
        }
    }

    /**
     * 遍历所有数学通道
     * 
     * @param action 对每个通道执行的操作
     */
    public static void forEachMath(Consumer<MathChannel> action){
        Objects.requireNonNull(action);  // 检查action非空
        MathChannel channel;  // 通道临时变量
        for(int i=ChannelFactory.MATH1;i<maxMathIdx;i++){  // 遍历数学通道
            channel = ChannelFactory.getMathChannel(i);  // 获取通道
            if(channel != null) {  // 检查通道是否有效
                action.accept(channel);  // 执行操作
            }
        }
    }

    /**
     * 遍历所有参考通道
     * 
     * @param action 对每个通道执行的操作
     */
    public static void forEachRef(Consumer<RefChannel> action){
        Objects.requireNonNull(action);  // 检查action非空
        RefChannel channel;  // 通道临时变量
        for(int i=ChannelFactory.REF1;i<maxRefIdx;i++){  // 遍历参考通道
            channel = ChannelFactory.getRefChannel(i);  // 获取通道
            if(channel != null) {  // 检查通道是否有效
                action.accept(channel);  // 执行操作
            }
        }
    }

    /**
     * 遍历所有串口通道
     * 
     * @param action 对每个通道执行的操作
     */
    public static void forEachSerial(Consumer<SerialChannel> action){
        Objects.requireNonNull(action);  // 检查action非空
        SerialChannel channel;  // 通道临时变量
        for(int i=ChannelFactory.S1;i<maxSerialIdx;i++){  // 遍历串口通道
            channel = ChannelFactory.getSerialChannel(i);  // 获取通道
            if(channel != null) {  // 检查通道是否有效
                action.accept(channel);  // 执行操作
            }
        }
    }


    /**
     * 私有构造方法：初始化通道工厂
     */
    private ChannelFactory() {
        init1();  // 初始化通道实例
    }

    /**
     * 初始化通道实例
     * 
     * <p>创建所有通道实例并设置Z-Order。
     */
    private void init1() {
        for(int i=CH1;i<maxChIdx;i++){  // 创建模拟通道
            channels[i] = new Channel(i);  // 创建通道实例
            channelList.add(channels[i]);  // 添加到列表
        }
        for(int i=MATH1;i<maxMathIdx;i++){  // 创建数学通道
            channels[i] = new MathChannel(i);  // 创建通道实例
            channelList.add(channels[i]);  // 添加到列表
        }
        for(int i=REF1;i<maxRefIdx;i++){  // 创建参考通道
            channels[i] = new RefChannel(i);  // 创建通道实例
            channelList.add(channels[i]);  // 添加到列表
        }
        for(int i=S1;i<maxSerialIdx;i++){  // 创建串口通道
            channels[i] = new SerialChannel(i);  // 创建通道实例
            channelList.add(channels[i]);  // 添加到列表
        }
        int cnt = channelList.size();  // 获取通道总数
        for(int i=0;i<cnt;i++){  // 设置Z-Order
            IChannel channel = channelList.get(i);  // 获取通道
            channel.setzOrder(cnt- i);  // 设置Z-Order（越大越靠前）
        }
    }

    /**
     * 初始化参考通道颜色
     * 
     * @param refColor 颜色数组
     */
    public static void initRef(int [] refColor){
        System.arraycopy(refColor, 0, ChannelFactory.refColor, 0, ChannelFactory.refColor.length);  // 复制颜色数组
    }

    /**
     * 初始化数学通道颜色
     * 
     * @param mathColor 颜色数组
     */
    public static void initMath(int [] mathColor){
        System.arraycopy(mathColor, 0, ChannelFactory.mathColor, 0, ChannelFactory.mathColor.length);  // 复制颜色数组
    }

    /**
     * 改变通道颜色
     * 
     * @param chIndex 通道索引（1-based）
     */
    public void changeChannelColor(int chIndex) {
        int tempIndex = chIndex - 1;  // 转换为0-based索引
        IChannel channel = getChannel(tempIndex);  // 获取通道
        if (channel == null) return;  // 检查通道是否有效
        if (ChannelFactory.isRefCh(tempIndex)) {  // 检查是否为参考通道
            ((RefChannel) channel).setForegroundColor(refColor[tempIndex - REF1]);  // 设置参考通道颜色
        }
        if (ChannelFactory.isMathCh(tempIndex)) {  // 检查是否为数学通道
            ((MathChannel) channel).setForegroundColor(mathColor[tempIndex - MATH1]);  // 设置数学通道颜色
        }
    }

    /**
     * 获取顶层通道
     * 
     * @return 顶层通道实例
     */
    public IChannel getChannel() {
        return channelList.get(0);  // 返回列表第一个元素（最顶层）
    }

    /**
     * 获取顶层参考通道
     * 
     * @return 顶层参考通道实例，没有返回null
     */
    public IChannel getTopRefChannel() {
        for(int i=0; i<channelList.size(); i++) {  // 遍历通道列表
            if(isRefCh(channelList.get(i).getChId())) {  // 检查是否为参考通道
                return channelList.get(i);  // 返回第一个参考通道
            }
        }
        return null;  // 没有参考通道返回null
    }

    /**
     * 获取指定索引的通道
     * 
     * @param idx 通道索引
     * @return 通道实例，无效索引返回null
     */
    public IChannel getChannel(int idx) {
        if(isValidCh(idx)){  // 检查索引是否有效
            return channels[idx];  // 返回通道实例
        }
        return null;  // 无效索引返回null
    }

    /**
     * 获取任意波形通道
     * 
     * <p>返回第一个非串口通道。
     * 
     * @return 通道实例
     */
    public IChannel getWaveChannel() {
        for(IChannel c:channelList){  // 遍历通道列表
            if(c.getChId() < S1){  // 检查是否为非串口通道
                return c;  // 返回通道
            }
        }
        return null;  // 没有找到返回null
    }

    /**
     * 设置参考通道激活状态
     * 
     * @param refIdx 参考通道索引
     */
    public static void setRefActive(int refIdx) {
        forEachRef((refChannel -> {  // 遍历参考通道
            int color;  // 颜色临时变量
            int chIdx = refChannel.getChId();  // 获取通道ID
            color = refColor[chIdx - REF1];  // 获取颜色
            refChannel.setForegroundColor(color);  // 设置前景色
        }));

    }

    /**
     * 设置数学通道激活状态
     * 
     * @param mathIdx 数学通道索引
     */
    public static void setMathActive(int mathIdx){
        forEachMath((mathChannel -> {  // 遍历数学通道
            int color;  // 颜色临时变量
            int chIdx;  // 通道ID
            chIdx = mathChannel.getChId();  // 获取通道ID
            color =  mathColor[chIdx - MATH1];  // 获取颜色
            mathChannel.setForegroundColor(color);  // 设置前景色
        }));
    }

    // ==================== 通道类型判断方法 ====================

    /**
     * 检查通道索引是否有效
     * 
     * @param idx 通道索引
     * @return true表示有效，false表示无效
     */
    public static boolean isValidCh(int idx) {
        return (idx >= CH1 && idx < SERIAL_MAX);  // 检查是否在有效范围内
    }

    /**
     * 检查是否为模拟通道
     * 
     * @param idx 通道索引
     * @return true表示是模拟通道，false表示不是
     */
    public static boolean isDynamicCh(int idx) {
        return (idx >= CH1 && idx < maxChIdx);  // 检查是否在模拟通道范围内
    }

    /**
     * 检查是否为模拟通道或数学通道
     * 
     * @param idx 通道索引
     * @return true表示是，false表示不是
     */
    public static boolean isDynamic_or_math_Ch(int idx) {
        return isDynamicCh(idx) || isMathCh(idx);  // 检查是否为模拟或数学通道
    }

    /**
     * 检查是否为串口通道
     * 
     * @param idx 通道索引
     * @return true表示是串口通道，false表示不是
     */
    public static boolean isSerialCh(int idx) {
        return (idx >= S1 && idx < maxSerialIdx);  // 检查是否在串口通道范围内
    }

    /**
     * 检查是否为数学通道
     * 
     * @param idx 通道索引
     * @return true表示是数学通道，false表示不是
     */
    public static boolean isMathCh(int idx) {
        return (idx >= MATH1 && idx < maxMathIdx);  // 检查是否在数学通道范围内
    }

    /**
     * 检查是否为参考通道
     * 
     * @param idx 通道索引
     * @return true表示是参考通道，false表示不是
     */
    public static boolean isRefCh(int idx) {
        return (idx >= REF1 && idx < maxRefIdx);  // 检查是否在参考通道范围内
    }

    /**
     * 检查是否为FFT数学通道
     * 
     * @param idx 通道索引
     * @return true表示是FFT通道，false表示不是
     */
    public static boolean isMath_FFT_Ch(int idx) {
        return isMathCh(idx) && getMathChannel(idx).getMathType() == MathWave.MATH_FFTWAVE;  // 检查是否为FFT类型
    }

    // ==================== 通道操作方法 ====================

    /**
     * 打开通道
     * 
     * <p>将通道移到顶层并激活。
     * 
     * @param idx 通道索引
     */
    public void open(int idx) {
        IChannel channel = getChannel(idx);  // 获取通道
        if(channel != null){  // 检查通道是否有效
            if(!channel.isOpen()){  // 检查通道是否已打开
                toTopLayer(channel);  // 移到顶层
                channel.open();  // 打开通道
                channel.activate();  // 激活通道
            }
        }
    }

    /**
     * 激活通道
     * 
     * <p>将已打开的通道移到顶层并激活。
     * 
     * @param idx 通道索引
     */
    public void activate(int idx) {
        IChannel channel = getChannel(idx);  // 获取通道
        if(channel != null){  // 检查通道是否有效
            if(channel.isOpen()){  // 检查通道是否已打开
                toTopLayer(channel);  // 移到顶层
                channel.activate();  // 激活通道
            }
        }
    }

    /**
     * 关闭通道
     * 
     * <p>将通道移到底层并关闭。
     * 
     * @param idx 通道索引
     */
    public void close(int idx) {
        IChannel channel = getChannel(idx);  // 获取通道
        if (channel != null) {  // 检查通道是否有效
            if(!channel.isOpen()) return;  // 已关闭则直接返回
            toBottomLayer(channel);  // 移到底层
            channel.close();  // 关闭通道
            boolean bActive = false;  // 激活标志
            if(channelList.get(0).isOpen() ){  // 检查顶层通道是否打开
                if(!isSerialCh(channelList.get(0).getChId())) {  // 检查是否为非串口通道
                    channelList.get(0).activate();  // 激活顶层通道
                    bActive = true;  // 设置激活标志
                }
            } else {
                toTopLayer(channel);  // 将关闭的通道移到顶层
            }
            if(!bActive){  // 如果没有激活任何通道
                EventFactory.sendEvent(EventFactory.EVENT_CHANNEL_ACTIVE);  // 发送激活事件
            }
        }
    }

    /**
     * 检查通道是否打开
     * 
     * @param idx 通道索引
     * @return true表示打开，false表示关闭
     */
    public boolean isOpen(int idx) {
        IChannel channel = getChannel(idx);  // 获取通道
        boolean b = false;  // 结果变量
        if (channel != null) {  // 检查通道是否有效
            b = channel.isOpen();  // 获取打开状态
        }
        return b;  // 返回结果
    }

    /**
     * 更新所有通道的Z-Order
     */
    private void zOrder() {
        int cnt = channelList.size();  // 获取通道总数
        for (int i = 0; i < cnt; i++) {  // 遍历通道列表
            channelList.get(i).setzOrder(cnt-i);  // 设置Z-Order
        }
    }

    /**
     * 调整通道层级
     * 
     * @param channel 通道实例
     * @param toTop true表示移到顶层，false表示移到底层
     */
    private synchronized void toZorder(IChannel channel,boolean toTop){
        channelList.remove(channel);  // 从列表中移除
        if(toTop) {  // 移到顶层
            channelList.add(0, channel);  // 添加到列表头部
        }else {  // 移到底层
            channelList.add(channel);  // 添加到列表尾部
        }
        zOrder();  // 更新Z-Order
    }

    /**
     * 将通道移到顶层
     * 
     * @param channel 通道实例
     */
    private void toTopLayer(IChannel channel) {
        toZorder(channel,true);  // 移到顶层
    }

    /**
     * 将通道移到底层
     * 
     * @param channel 通道实例
     */
    private void toBottomLayer(IChannel channel) {
        toZorder(channel,false);  // 移到底层
    }

    /**
     * 检查通道是否激活
     * 
     * @param chIdx 通道索引
     * @return true表示激活，false表示未激活
     */
    public boolean isActive(int chIdx){
        return getValidChannel().getChId() == chIdx;  // 检查是否为当前激活通道
    }



    /**
     * 获取指定索引的通道（静态方法）
     * 
     * @param idx 通道索引
     * @return 通道实例
     */
    public static IChannel getValidChannel(int idx) {
        return getInstance().getChannel(idx);  // 返回通道实例
    }

    /**
     * 获取当前激活通道
     * 
     * @return 激活通道实例
     */
    public static IChannel getValidChannel() {
        return getInstance().getChannel();  // 返回顶层通道
    }

    /**
     * 获取模拟通道实例
     * 
     * @param idx 通道索引（0~CH_MAX）
     * @return 模拟通道实例，无效索引返回null
     */
    public static Channel getDynamicChannel(int idx) {
        if (isDynamicCh(idx))  // 检查是否为模拟通道
            return (Channel) getValidChannel(idx);  // 返回模拟通道
        return null;  // 无效索引返回null
    }

    /**
     * 获取串口通道实例
     * 
     * @param idx 通道索引
     * @return 串口通道实例，无效索引返回null
     */
    public static SerialChannel getSerialChannel(int idx) {
        if (isSerialCh(idx))  // 检查是否为串口通道
            return (SerialChannel) getValidChannel(idx);  // 返回串口通道
        return null;  // 无效索引返回null
    }

    /**
     * 获取打开的模拟通道数量
     * 
     * @return 打开的通道数量
     */
    public static int getDynamicChannelOpenCount() {
        int nums = 0;  // 计数器
        for (int i = CH1; i < maxChIdx; i++) {  // 遍历模拟通道
            if (getValidChannel(i).isOpen()) {  // 检查是否打开
                nums++;  // 计数增加
            }
        }
        return nums;  // 返回数量
    }



    /**
     * 获取数学通道实例
     * 
     * @param idx 通道索引
     * @return 数学通道实例，无效索引返回null
     */
    public static MathChannel getMathChannel(int idx) {
        if(isMathCh(idx)) {  // 检查是否为数学通道
            return (MathChannel) getValidChannel(idx);  // 返回数学通道
        }
        return null;  // 无效索引返回null
    }

    /**
     * 获取参考通道实例
     * 
     * @param idx 通道索引
     * @return 参考通道实例，无效索引返回null
     */
    public static RefChannel getRefChannel(int idx) {
        if (isRefCh(idx)) {  // 检查是否为参考通道
            return (RefChannel) getValidChannel(idx);  // 返回参考通道
        }
        return null;  // 无效索引返回null
    }

    /**
     * 打开通道（静态方法）
     * 
     * @param idx 通道索引
     */
    public static void chOpen(int idx) {
        getInstance().open(idx);  // 调用实例方法
    }

    /**
     * 检查通道是否打开（静态方法）
     * 
     * @param idx 通道索引
     * @return true表示打开，false表示关闭
     */
    public static boolean isChOpen(int idx) {
        return getInstance().isOpen(idx);  // 返回打开状态
    }



    /**
     * 获取当前激活通道ID
     * 
     * @return 激活通道索引
     */
    public static int getChActivate() {
        return getValidChannel().getChId();  // 返回激活通道ID
    }

    /**
     * 检查指定通道是否激活
     * 
     * @param chIdx 通道索引
     * @return true表示激活，false表示未激活
     */
    public static boolean isChActivate(int chIdx){
        return getValidChannel().getChId() == chIdx;  // 检查是否为激活通道
    }

    /**
     * 激活指定通道
     * 
     * @param chIdx 通道索引
     */
    public static void chActivate(int chIdx){
        if(!isChActivate(chIdx)) {  // 检查是否已激活
            getInstance().activate(chIdx);  // 激活通道
        }
    }

    /**
     * 关闭通道（静态方法）
     * 
     * @param idx 通道索引
     */
    public static void chClose(int idx) {
        getInstance().close(idx);  // 调用实例方法
    }

    /**
     * 启用/禁用通道
     * 
     * @param idx 通道索引
     * @param isOpen true表示打开，false表示关闭
     */
    public static void chEnable(int idx, boolean isOpen) {
        if (isOpen) {  // 打开通道
            chOpen(idx);  // 打开
        } else {  // 关闭通道
            chClose(idx);  // 关闭
        }
    }

    /**
     * 获取探头类型字符串
     * 
     * @param ch 通道索引
     * @return 探头类型字符串
     */
    public static String getProbeType(int ch){
        if(ch < CH1 || ch >= REF_MAX)  // 检查索引范围
            return "";  // 无效索引返回空字符串

        int prbType = VerticalAxis.PROBE_TYPE_VOL;  // 默认电压探头
        if(isMathCh(ch)){  // 数学通道
            prbType = getMathChannel(ch).getProbeType();  // 获取数学通道探头类型
        }
        else if(isRefCh(ch)){  // 参考通道
            prbType = getRefChannel(ch).getProbeType();  // 获取参考通道探头类型
        }
        else if(isDynamicCh(ch)){  // 模拟通道
            prbType = getDynamicChannel(ch).getProbeType();  // 获取模拟通道探头类型
        }
        if(prbType == VerticalAxis.PROBE_TYPE_CUSTOM){  // 自定义探头
            return getProbeString(ch,VerticalAxis.PROBE_TYPE_CUSTOM);  // 返回自定义探头字符串
        }
        return getProbeString(prbType);  // 返回标准探头字符串
    }

    /**
     * 获取探头字符串
     * 
     * @param ch 通道索引
     * @param probeType 探头类型
     * @return 探头字符串
     */
    public static String getProbeString(int ch,int probeType){
        String probeString="";  // 探头字符串
        if(probeType == VerticalAxis.PROBE_TYPE_CUSTOM){  // 自定义探头
            if(isMathCh(ch)){  // 数学通道
                probeString = getMathChannel(ch).getProbeStr();  // 获取数学通道探头字符串
            }
            else if(isRefCh(ch)){  // 参考通道
                probeString = getRefChannel(ch).getProbeStr();  // 获取参考通道探头字符串
            }
            else if(isDynamicCh(ch)){  // 模拟通道
                probeString = getDynamicChannel(ch).getProbeStr();  // 获取模拟通道探头字符串
            }
        }else {  // 标准探头
            probeString = getProbeType(ch);  // 获取探头类型字符串
        }
        return probeString;  // 返回探头字符串
    }

    /**
     * 根据探头类型获取字符串
     * 
     * @param prbType 探头类型
     * @return 探头类型字符串
     */
    public static String getProbeString(int prbType){
        switch (prbType){  // 根据探头类型返回字符串
            case VerticalAxis.PROBE_TYPE_VOL:       return "V";  // 电压
            case VerticalAxis.PROBE_TYPE_CUR:       return "A";  // 电流
            case VerticalAxis.PROBE_TYPE_VA :       return "V/A";  // 电压/电流
            case VerticalAxis.PROBE_TYPE_AV :       return "A/V";  // 电流/电压
            case VerticalAxis.PROBE_TYPE_W :        return "W";  // 功率
            case VerticalAxis.PROBE_TYPE_DB :       return "dB";  // 分贝
            case VerticalAxis.PROBE_TYPE_AA :       return "AA";  // 电流/电流
            case VerticalAxis.PROBE_TYPE_VV :       return "VV";  // 电压/电压
            case VerticalAxis.PROBE_TYPE_WENHAO:    return "?";  // 未知
            case VerticalAxis.PROBE_TYPE_VTOV:      return "V/V";  // 电压比
            case VerticalAxis.PROBE_TYPE_ATOA:      return "A/A";  // 电流比
            case VerticalAxis.PROBE_TYPE_EMPTY :  // 空类型
            default:                                 return "";  // 默认返回空
        }
    }

    /**
     * 获取通道名称
     * 
     * @param idx 通道索引
     * @return 通道名称
     */
    public static String getChannelName(int idx) {
        IChannel channel = getValidChannel(idx);  // 获取通道
        if(channel != null){  // 检查通道是否有效
            return channel.getName();  // 返回通道名称
        }
        return "";  // 无效通道返回空字符串
    }

    /**
     * 检查是否有数学通道打开
     * 
     * @return true表示有打开的数学通道，false表示没有
     */
    public static boolean isMathEnable(){
        boolean bEnable = false;  // 启用标志
        for (int i=MATH1;i<maxMathIdx;i++){  // 遍历数学通道
            MathChannel mathChannel = ChannelFactory.getMathChannel(i);  // 获取数学通道
            if(mathChannel != null) {  // 检查通道是否有效
                bEnable = mathChannel.isOpen();  // 获取打开状态
                if(bEnable){  // 如果有打开的
                    break;  // 退出循环
                }
            }
        }
        return bEnable;  // 返回结果
    }

    /**
     * 将串口通道编号转换为工厂索引
     * 
     * @param serialChNums 串口通道编号（1~4）
     * @return 工厂索引（ChannelFactory.S1~S4）
     */
    public static int toFactorySerial(int serialChNums) {
        return serialChNums + ChannelFactory.REF8;  // 转换为工厂索引
    }

    /**
     * 获取通道标签
     * 
     * @param chIdx 通道索引
     * @return 标签字符串
     */
    public static String getLabel(int chIdx) {
        String label = "";  // 标签字符串
        IChannel channel = ChannelFactory.getValidChannel(chIdx);  // 获取通道
        if (channel == null) return label;  // 无效通道返回空字符串
        return channel.getLabel();  // 返回标签
    }

    /**
     * 获取通道偏移量
     * 
     * @param idx 通道索引
     * @return 偏移量
     */
    public static double getChannelOffset(int idx) {
        IChannel channel = ChannelFactory.getValidChannel(idx);  // 获取通道
        if (channel != null) {  // 检查通道是否有效
            return channel.getPosUI() * channel.getADVerticalPerPix();  // 计算偏移量
        } else {
            return 0;  // 无效通道返回0
        }
    }

}
