package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

import com.micsig.tbook.tbookscope.top.layout.trigger.ITriggerDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerSerials implements ITriggerDetail {
    private Serials serials;
    private ISerialsDetail serialsDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Serials getSerials() {
        return serials;
    }

    public void setSerials(Serials serials) {
        this.serials = serials;
        this.serials.setRxMsgSelect(true);
    }

    @Override
    public TopBeanChannel getTriggerSource() {
        return null;
    }

    public ISerialsDetail getSerialsDetail() {
        return serialsDetail;
    }

    public void setSerialsDetail(ISerialsDetail serialsDetail) {
        this.serialsDetail = serialsDetail;
    }

    @Override
    public String toString() {
        return "TopMsgTriggerSerials{" +
                "serials=" + serials +
                ", serialsDetail=" + serialsDetail +
                '}';
    }
}
