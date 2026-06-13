package com.micsig.tbook.tbookscope.top.layout.measure;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
/**
 * @auother Liwb
 * @description:
 * @data:2022-2-14 10:26
 */


public class TopMsgFrequencyMeter implements IMeasureDetail {
    private TopBeanChannel frequencyMeter;
    private boolean frequencyEnable;

    public TopBeanChannel getFrequencyMeter() {
        return frequencyMeter;
    }

    public void setFrequencyMeter(TopBeanChannel frequencyMeter) {
        if (this.frequencyMeter == null) {
            this.frequencyMeter = frequencyMeter;
        } else {
            this.frequencyMeter = frequencyMeter;
            this.frequencyMeter.setRxMsgSelect(true);
        }
    }

    public boolean isFrequencyEnable() {
        return frequencyEnable;
    }

    public void setFrequencyEnable(boolean frequencyEnable) {
        this.frequencyEnable = frequencyEnable;
    }
}
