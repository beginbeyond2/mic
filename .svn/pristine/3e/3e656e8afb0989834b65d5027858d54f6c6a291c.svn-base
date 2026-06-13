package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxIntWithSelect;

/**
 * Created by limh on 2024/8/6.
 */

public class TopMsgDisplayFftInfo implements IDisplayDetail {
    private RxIntWithSelect fftInfoIndex;
    private RxBooleanWithSelect showFftInfo;

    public RxIntWithSelect getFftInfoIndex() {
        return fftInfoIndex;
    }

    public void setFftInfoIndex(int fftInfoIndex) {
        if (this.fftInfoIndex == null) {
            this.fftInfoIndex = new RxIntWithSelect(fftInfoIndex);
        } else {
            this.fftInfoIndex.setValue(fftInfoIndex);
            setAllUnSelect();
            this.fftInfoIndex.setRxMsgSelect(true);
        }
    }


    public RxBooleanWithSelect isShowFftInfo() {
        return showFftInfo;
    }

    public void setShowFftInfo(boolean showFftInfo) {
        if (this.showFftInfo == null) {
            this.showFftInfo = new RxBooleanWithSelect(showFftInfo);
        } else {
            this.showFftInfo.setValue(showFftInfo);
            setAllUnSelect();
            this.showFftInfo.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
//        showFftInfo.setRxMsgSelect(false);
        fftInfoIndex.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgDisplayFftInfo{" +
//                "showFftInfo=" + showFftInfo +
                ", fftInfoIndex=" + fftInfoIndex +
                '}';
    }
}
