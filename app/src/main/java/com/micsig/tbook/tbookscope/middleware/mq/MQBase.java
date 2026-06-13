package com.micsig.tbook.tbookscope.middleware.mq;

import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 10:52
 */
public abstract class MQBase {
    protected RxEnum rxType;
    protected MQEnum mqType;
    protected IChan chan;
    public boolean isFromEventBus = false;

    public MQBase(){

    }
    public MQBase(MQEnum type, IChan chan){
        this.mqType =type;
        this.chan=chan;
    }

    public MQEnum getMqType() {
        return mqType;
    }

    public RxEnum getRxType() {
        return rxType;
    }

    public void setRxType(RxEnum rxType) {
        this.rxType = rxType;
    }

    public void setMqType(MQEnum mqType) {
        this.mqType = mqType;
    }

    public IChan getChan() {
        return chan;
    }

    public void setChan(IChan chan) {
        this.chan = chan;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MQBase{");
        sb.append("rxType=").append(rxType);
        sb.append(", mqType=").append(mqType);
        sb.append(", chan=").append(chan);
        sb.append('}');
        return sb.toString();
    }
}
