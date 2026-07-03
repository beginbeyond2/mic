package com.micsig.tbook.tbookscope.middleware.mq;

import com.alibaba.fastjson2.annotation.JSONField;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.OperateOrder;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChOpenClose;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║              MQChanSelectorManage — 通道选择管理器                       ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：middleware层 · 消息队列(MQ)子系统 · 通道选择管理               ║
 * ║ 核心职责：管理通道的激活/开关/排序状态，响应EventBus事件，                ║
 * ║          通过RxBus广播通道状态变更消息到UI层                              ║
 * ║ 架构设计：观察者模式 + 消息队列模式，                                     ║
 * ║          监听EventFactory的通道开/关/激活事件，                           ║
 * ║          封装MsgChOpenClose/MsgChActiveChange消息通过RxBus广播           ║
 * ║ 数据流向：EventFactory事件 → eventUIObserver/eventOnlyChannelActive →    ║
 * ║          MsgChOpenClose/MsgChActiveChange → RxBus.post() → UI层订阅     ║
 * ║ 依赖关系：ChannelFactory（通道工厂，查询通道状态）、                       ║
 * ║          OperateOrder（通道排序管理）、IChan（通道标识接口）、             ║
 * ║          RxBus/RxBusRegister（消息总线注册与发送）、                       ║
 * ║          EventFactory/EventBase（EventBus事件源）、                       ║
 * ║          MsgChOpenClose/MsgChActiveChange（MQ消息子类）                   ║
 * ║ 使用场景：用户在UI上切换活动通道、打开/关闭通道时，                       ║
 * ║          通过此管理器同步通道状态并通知UI更新显示                          ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * @auother Liwb
 * @description: 通道选择管理器，管理通道激活/开关/排序，响应EventBus并通过RxBus广播
 * @data:2024-2-29 9:08
 */
public class MQChanSelectorManage {

    /** 当前活动通道标识，默认CH_NULL表示无活动通道 */ // 活动通道字段
    private IChan activityChannel = IChan.CH_NULL; // 当前活动（选中）的通道标识

    /** 上一次活动的通道标识，用于记录切换前的通道 */ // 上次活动通道字段
    private IChan lastActiveObject = IChan.CH_NULL; // 上一次活动的通道标识，用于追溯

    /** 通道对象映射表（IChan → IChannel），不参与JSON序列化 */ // 通道映射表字段
    @JSONField(serialize = false) // FastJSON序列化时忽略此字段
    private HashMap<IChan,Object> map=new HashMap<>(); // IChan→IChannel映射，key为通道标识，value为通道对象

    /** 通道开关状态数组，channelShow[chanIndex] = true表示该通道已打开 */ // 通道开关状态数组
    public boolean[] channelShow ; // 通道显示状态数组，索引为通道值，值为是否打开

    /** 活动通道排序列表，按使用优先级排列，最近的通道排在前面 */ // 活动通道排序列表
    public List<IChan> activityChOrder=new ArrayList<>(); // 活动通道排序，最近的排前面

    /** 通道排序操作管理器，提供toFirst/toEnd/getFirst等排序操作 */ // 排序操作管理器
    private OperateOrder operateChOrder; // 通道排序操作封装，管理activityChOrder的排序逻辑

    /** 全部通道标识数组，不参与JSON序列化 */ // 全部通道数组字段
    @JSONField(serialize = false) // FastJSON序列化时忽略此字段
    private IChan[] Chs = new IChan[]{ // 所有可能的通道标识数组，包含模拟/数学/参考/串行通道
            IChan.CH1, IChan.CH2, IChan.CH3, IChan.CH4, IChan.CH5, IChan.CH6, IChan.CH7, IChan.CH8, // 模拟通道CH1~CH8
            IChan.Math1, IChan.Math2, IChan.Math3, IChan.Math4, IChan.Math5, IChan.Math6, IChan.Math7, IChan.Math8, // 数学通道Math1~Math8
            IChan.R1, IChan.R2, IChan.R3, IChan.R4, IChan.R5, IChan.R6, IChan.R7, IChan.R8, // 参考波形通道R1~R8
            IChan.S1, IChan.S2, IChan.S3, IChan.S4 // 串行总线解码通道S1~S4
    };

    /** 通道开关消息，用于RxBus广播通道打开/关闭事件 */ // 通道开关消息字段
    private MsgChOpenClose msgChOpenClose= RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,MQEnum.CH_OPEN); // 预创建通道开关消息，路由类型为MQ_CHANNEL_ACTIVE_CHANGE

    /** 通道激活变更消息，用于RxBus广播活动通道切换事件 */ // 通道激活消息字段
    private MsgChActiveChange msgActive=RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,MQEnum.CH_ACTIVE); // 预创建通道激活消息，路由类型为MQ_CHANNEL_ACTIVE_CHANGE


    /**
     * 构造方法，初始化通道状态并注册EventBus观察者
     * <p>1. 初始化channelShow数组（全部false）</p>
     * <p>2. 遍历所有通道，建立IChan→IChannel映射</p>
     * <p>3. 创建OperateOrder排序管理器</p>
     * <p>4. 注册EventBus通道开/关/激活事件监听</p>
     */
    public MQChanSelectorManage(){ // 构造方法，初始化通道管理器
        channelShow=new boolean[Chs.length]; // 创建与通道数量等长的boolean数组
        Arrays.fill(channelShow,false); // 全部填充为false（初始状态所有通道关闭）

        for(int i=0;i< Chs.length;i++){ // 遍历所有通道标识
            IChan ch=Chs[i]; // 获取当前通道标识
            activityChOrder.add(ch); // 将通道加入活动排序列表
            if(ChannelFactory.isDynamicCh(ch.getValue())){ // 判断是否为模拟通道（CH1~CH8）
                map.put(ch, ChannelFactory.getDynamicChannel(ch.getValue())); // 从工厂获取模拟通道对象并放入映射
            }else if(ChannelFactory.isMathCh(ch.getValue())){ // 判断是否为数学通道（Math1~Math8）
                map.put(ch,ChannelFactory.getMathChannel(ch.getValue())); // 从工厂获取数学通道对象并放入映射
            }else if(ChannelFactory.isRefCh(ch.getValue())){ // 判断是否为参考波形通道（R1~R8）
                map.put(ch,ChannelFactory.getRefChannel(ch.getValue())); // 从工厂获取参考通道对象并放入映射
            }else if(ChannelFactory.isSerialCh(ch.getValue())){ // 判断是否为串行总线通道（S1~S4）
                map.put(ch,ChannelFactory.getSerialChannel(ch.getValue())); // 从工厂获取串行通道对象并放入映射
            }
        }
        operateChOrder =new OperateOrder(activityChOrder); // 创建排序操作管理器，以活动通道列表为底层数据

        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this::eventUIObserver); // 注册通道打开事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE, this::eventUIObserver); // 注册通道关闭事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE, this::eventOnlyChannelActive); // 注册通道激活事件观察者
    }

    /**
     * 通道激活事件的EventBus回调方法
     * <p>仅处理EVENT_CHANNEL_ACTIVE事件，根据事件数据设置活动通道</p>
     * <p>若事件无数据则自动选择第一个打开的通道</p>
     * @param observable Observable事件源（未使用）
     * @param obj EventBase事件对象，包含事件ID和通道数据
     */
    private void eventOnlyChannelActive(Observable observable, Object obj) { // EventBus通道激活事件回调
        EventBase eventBase = (EventBase) obj; // 将事件对象强转为EventBase
        if(eventBase.getId() == EventFactory.EVENT_CHANNEL_ACTIVE) { // 确认事件ID为通道激活事件
            IChan activeChan = null; // 声明活动通道变量
            if (eventBase.getData() != null) { // 事件数据不为空
                activeChan = IChan.toIChan((int) eventBase.getData()); // 从事件数据中解析通道标识
            } else { // 事件数据为空
//            activeChan = (IChan) operateChOrder.getFirst(); // 原始逻辑：取排序列表第一个（已注释）
                activeChan = getFirstOpen(); // 改用获取第一个打开的通道
            }
            setActivityChannel(activeChan,true); // 设置活动通道，isFromEventBus=true表示来自EventBus
        }
    }

    /**
     * 获取第一个已打开的通道
     * <p>遍历operateChOrder排序列表，返回第一个处于打开状态的通道</p>
     * @return 第一个打开的通道，若无则返回CH_NULL
     */
    private IChan getFirstOpen() { // 获取第一个已打开的通道
        IChan temp = IChan.CH_NULL; // 初始化为空通道
        for (int i = 0; i < operateChOrder.getList().size(); i++) { // 遍历排序列表
            IChan chan = (IChan) operateChOrder.getItem(i); // 获取指定位置的通道
            if (chan != null && ChannelFactory.isChOpen(chan.getValue())) { // 通道非空且处于打开状态
                temp = chan; // 记录此通道
                break; // 找到第一个即跳出循环
            }
        }
        return temp; // 返回第一个打开的通道，或CH_NULL
    }

    /**
     * 通道开/关事件的EventBus回调方法
     * <p>处理EVENT_CHANNEL_OPEN和EVENT_CHANNEL_CLOSE事件</p>
     * <p>通道打开时将其移至排序列表首位，关闭时移至末位</p>
     * <p>封装MsgChOpenClose消息通过RxBus广播</p>
     * @param observable Observable事件源（未使用）
     * @param o EventBase事件对象，包含事件ID和通道数据
     */
    private void eventUIObserver(Observable observable, Object o) { // EventBus通道开/关事件回调
        // TODO: 2024-2-29 未完成，加载的时候不处理相关辑逻
//        if (SaveRecovery.getIns().hasLoading()) return; // 加载中时不处理（已注释）

        int evId = ((EventBase) o).getId(); // 获取事件ID
        IChan curChan = IChan.toIChan((int) ((EventBase) o).getData()); // 从事件数据解析通道标识
//        if (operateChOrder.getFirst()==curChan) return; // 已注释：如果排首位则跳过
        refreshChannelShow(); // 刷新通道开关状态数组
        if (evId==EventFactory.EVENT_CHANNEL_OPEN){ // 事件为通道打开
            operateChOrder.toFirst(curChan); // 将该通道移至排序列表首位（最近使用）
            msgChOpenClose.setMqType(MQEnum.CH_OPEN); // 设置消息类型为通道打开
        }else { // 事件为通道关闭
            operateChOrder.toEnd(curChan); // 将该通道移至排序列表末位
            msgChOpenClose.setMqType(MQEnum.CH_CLOSE); // 设置消息类型为通道关闭
        }
        msgChOpenClose.setChan(curChan); // 设置消息关联的通道标识
        RxBus.getInstance().post(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,msgChOpenClose); // 通过RxBus广播通道开关消息

//        IChan activeChan = (IChan) operateChOrder.getFirst(); // 已注释：获取排序列表首位的活动通道
//        setActivityChannel(activeChan); // 已注释：设置活动通道
    }

    /**
     * 仅设置示波器参数（激活当前活动通道），不触发RxBus消息
     * <p>用于初始化或恢复时仅设置FPGA参数而不通知UI</p>
     */
    public void onlaySetScopeParam(){ // 仅设置示波器参数，不触发消息广播
        ChannelFactory.chActivate(this.activityChannel.getValue()); // 调用工厂激活当前活动通道的FPGA参数
    }


    /**
     * 设置活动通道（不来自EventBus）
     * <p>重载方法，默认isFromEventBus=false</p>
     * @param chan 要激活的通道标识
     */
    public void setActivityChannel(IChan chan){ // 设置活动通道（非EventBus来源）
        setActivityChannel(chan,false); // 委托给带isFromEventBus参数的重载方法
    }

    /**
     * 设置活动通道
     * <p>1. 校验通道合法性（非CH_NULL且已打开）</p>
     * <p>2. 若非EventBus来源则先激活通道FPGA参数</p>
     * <p>3. 若活动通道确实发生变更则更新状态并广播消息</p>
     * @param chan 要激活的通道标识
     * @param isFromEventBus 是否来自EventBus事件
     */
    public void setActivityChannel(IChan chan,boolean isFromEventBus) { // 设置活动通道（完整参数版）

        /*boolean b =*/
        if (chan != IChan.CH_NULL && !ChannelFactory.isChOpen(chan.getValue())) return; // 通道非空但未打开则直接返回（不切换到未打开的通道）

        if(!isFromEventBus){ // 非EventBus来源时需要手动激活通道
            if (!ChannelFactory.isChActivate(chan.getValue())) { // 通道当前未被激活
                ChannelFactory.chActivate(chan.getValue()); // 调用工厂激活该通道的FPGA参数
            }
        }

        if(activityChannel != chan) { // 活动通道确实发生变更
            activityChannel = chan; // 更新当前活动通道标识
            setLastActiveObject(chan); // 记录为最后活动的通道
            operateChOrder.toFirst(chan); // 将该通道移至排序列表首位
            msgActive.setChan(chan); // 设置激活消息的关联通道
            msgActive.isFromEventBus = isFromEventBus; // 设置消息来源标志
            RxBus.getInstance().post(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, msgActive); // 通过RxBus广播通道激活变更消息
        }
    }

    /**
     * 通道使能开关（打开/关闭通道）
     * <p>此方法尚未完全替换，仅调用ChannelFactory.chEnable</p>
     * @param chan 通道标识
     * @param isOpen true=打开通道，false=关闭通道
     */
    public void chEnable(IChan chan,boolean isOpen){ // 通道使能开关
        ChannelFactory.chEnable(chan.getValue(),isOpen); // 调用工厂方法设置通道使能状态
    }

    /**
     * 获取当前活动通道标识
     * @return 当前活动通道的IChan标识
     */
    public IChan getActivityChannel() { // 获取当前活动通道
        return activityChannel; // 返回activityChannel字段
    }

    /**
     * 设置最后活动的通道标识
     * @param chan 通道标识
     */
    public void setLastActiveObject(IChan chan) { // 设置最后活动的通道
        lastActiveObject = chan; // 赋值lastActiveObject字段
    }


    /**
     * 获取最后活动的通道标识
     * @return 上一次活动的通道IChan标识
     */
    public IChan getLastActiveObject() { // 获取最后活动的通道
        return lastActiveObject; // 返回lastActiveObject字段
    }

    /**
     * 刷新通道开关状态数组
     * <p>遍历IChan→IChannel映射表，查询每个通道的isOpen状态并更新到channelShow数组</p>
     * @return 更新后的通道开关状态数组
     */
    public boolean[] refreshChannelShow() { // 刷新通道开关状态数组
        for (Map.Entry e : map.entrySet()) { // 遍历映射表的所有键值对
            channelShow[((IChan) e.getKey()).getValue()] = ((IChannel) e.getValue()).isOpen(); // 通过通道值作为索引，设置该通道的打开状态
        }
        return channelShow; // 返回更新后的状态数组
    }

    /**
     * 判断指定通道是否已打开
     * @param chan 通道标识
     * @return true=通道已打开，false=通道已关闭
     */
    public boolean hasChannelOpen(IChan chan){ // 判断指定通道是否已打开
        return  ((IChannel)map.get(chan)).isOpen(); // 从映射表获取通道对象并查询isOpen状态
    }

}
