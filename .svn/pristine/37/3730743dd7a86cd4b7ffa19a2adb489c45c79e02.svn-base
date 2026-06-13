package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.bean.RxIntWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayGraticule implements IDisplayDetail {
    private TopBeanChannel displayMode;
    private RxIntWithSelect intensity;

    public TopBeanChannel getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(TopBeanChannel displayMode) {
        if (this.displayMode == null) {
            this.displayMode = displayMode;
        } else {
            this.displayMode = displayMode;
            setAllUnSelect();
            this.displayMode.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        if (this.intensity == null) {
            this.intensity = new RxIntWithSelect(intensity);
        } else {
            this.intensity.setValue(intensity);
            setAllUnSelect();
            this.intensity.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        displayMode.setRxMsgSelect(false);
        intensity.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgDisplayGraticule{" +
                "displayMode=" + displayMode +
                ", intensity=" + intensity +
                '}';
    }
}
