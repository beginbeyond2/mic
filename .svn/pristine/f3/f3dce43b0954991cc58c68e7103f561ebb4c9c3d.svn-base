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

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 9:08
 */
public class MQChanSelectorManage {
    private IChan activityChannel = IChan.CH_NULL;
    private IChan lastActiveObject = IChan.CH_NULL;

    @JSONField(serialize = false)
    private HashMap<IChan,Object> map=new HashMap<>();
    public boolean[] channelShow ;
    public List<IChan> activityChOrder=new ArrayList<>();
    private OperateOrder operateChOrder;
    @JSONField(serialize = false)
    private IChan[] Chs = new IChan[]{
            IChan.CH1, IChan.CH2, IChan.CH3, IChan.CH4, IChan.CH5, IChan.CH6, IChan.CH7, IChan.CH8,
            IChan.Math1, IChan.Math2, IChan.Math3, IChan.Math4, IChan.Math5, IChan.Math6, IChan.Math7, IChan.Math8,
            IChan.R1, IChan.R2, IChan.R3, IChan.R4, IChan.R5, IChan.R6, IChan.R7, IChan.R8,
            IChan.S1, IChan.S2, IChan.S3, IChan.S4
    };
    private MsgChOpenClose msgChOpenClose= RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,MQEnum.CH_OPEN);
    private MsgChActiveChange msgActive=RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,MQEnum.CH_ACTIVE);


    public MQChanSelectorManage(){
        channelShow=new boolean[Chs.length];
        Arrays.fill(channelShow,false);

        for(int i=0;i< Chs.length;i++){
            IChan ch=Chs[i];
            activityChOrder.add(ch);
            if(ChannelFactory.isDynamicCh(ch.getValue())){
                map.put(ch, ChannelFactory.getDynamicChannel(ch.getValue()));
            }else if(ChannelFactory.isMathCh(ch.getValue())){
                map.put(ch,ChannelFactory.getMathChannel(ch.getValue()));
            }else if(ChannelFactory.isRefCh(ch.getValue())){
                map.put(ch,ChannelFactory.getRefChannel(ch.getValue()));
            }else if(ChannelFactory.isSerialCh(ch.getValue())){
                map.put(ch,ChannelFactory.getSerialChannel(ch.getValue()));
            }
        }
        operateChOrder =new OperateOrder(activityChOrder);

        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this::eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE, this::eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE, this::eventOnlyChannelActive);
    }

    private void eventOnlyChannelActive(Observable observable, Object obj) {
        EventBase eventBase = (EventBase) obj;
        if(eventBase.getId() == EventFactory.EVENT_CHANNEL_ACTIVE) {
            IChan activeChan = null;
            if (eventBase.getData() != null) {
                activeChan = IChan.toIChan((int) eventBase.getData());
            } else {
//            activeChan = (IChan) operateChOrder.getFirst();
                activeChan = getFirstOpen();
            }
            setActivityChannel(activeChan,true);
        }
    }

    private IChan getFirstOpen() {
        IChan temp = IChan.CH_NULL;
        for (int i = 0; i < operateChOrder.getList().size(); i++) {
            IChan chan = (IChan) operateChOrder.getItem(i);
            if (chan != null && ChannelFactory.isChOpen(chan.getValue())) {
                temp = chan;
                break;
            }
        }
        return temp;
    }

    private void eventUIObserver(Observable observable, Object o) {
        // TODO: 2024-2-29 未完成，加载的时候不处理相关辑逻
//        if (SaveRecovery.getIns().hasLoading()) return;

        int evId = ((EventBase) o).getId();
        IChan curChan = IChan.toIChan((int) ((EventBase) o).getData());
//        if (operateChOrder.getFirst()==curChan) return;
        refreshChannelShow();
        if (evId==EventFactory.EVENT_CHANNEL_OPEN){
            operateChOrder.toFirst(curChan);
            msgChOpenClose.setMqType(MQEnum.CH_OPEN);
        }else {
            operateChOrder.toEnd(curChan);
            msgChOpenClose.setMqType(MQEnum.CH_CLOSE);
        }
        msgChOpenClose.setChan(curChan);
        RxBus.getInstance().post(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,msgChOpenClose);

//        IChan activeChan = (IChan) operateChOrder.getFirst();
//        setActivityChannel(activeChan);
    }

    public void onlaySetScopeParam(){
        ChannelFactory.chActivate(this.activityChannel.getValue());
    }


    public void setActivityChannel(IChan chan){
        setActivityChannel(chan,false);
    }
    public void setActivityChannel(IChan chan,boolean isFromEventBus) {

        /*boolean b =*/
        if (chan != IChan.CH_NULL && !ChannelFactory.isChOpen(chan.getValue())) return;

        if(!isFromEventBus){
            if (!ChannelFactory.isChActivate(chan.getValue())) {
                ChannelFactory.chActivate(chan.getValue());
            }
        }

        if(activityChannel != chan) {
            activityChannel = chan;
            setLastActiveObject(chan);
            operateChOrder.toFirst(chan);
            msgActive.setChan(chan);
            msgActive.isFromEventBus = isFromEventBus;
            RxBus.getInstance().post(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, msgActive);
        }
    }

    /**
     * 这个还没有替换是，里面只调用了，没有其它动作
     * @param chan
     * @param isOpen
     */
    public void chEnable(IChan chan,boolean isOpen){
        ChannelFactory.chEnable(chan.getValue(),isOpen);
    }

    public IChan getActivityChannel() {
        return activityChannel;
    }

    public void setLastActiveObject(IChan chan) {
        lastActiveObject = chan;
    }


    public IChan getLastActiveObject() {
        return lastActiveObject;
    }

    public boolean[] refreshChannelShow() {
        for (Map.Entry e : map.entrySet()) {
            channelShow[((IChan) e.getKey()).getValue()] = ((IChannel) e.getValue()).isOpen();
        }
        return channelShow;
    }
    public boolean hasChannelOpen(IChan chan){
        return  ((IChannel)map.get(chan)).isOpen();
    }

}
