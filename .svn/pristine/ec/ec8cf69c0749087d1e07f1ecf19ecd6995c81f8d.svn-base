package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayPersist implements IDisplayDetail {
    private TopBeanChannel persist, fftPersist;
    private RxBooleanWithSelect clear, fftClear;
    private TopBeanHorizontal adjust, fftAdjust;

    public TopBeanChannel getPersist() {
        return persist;
    }

    public void setPersist(TopBeanChannel persist) {
        if (this.persist == null) {
            this.persist = persist;
        } else {
            this.persist = persist;
            setAllUnSelect();
            this.persist.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getFftPersist() {
        return fftPersist;
    }

    public void setFftPersist(TopBeanChannel fftPersist) {
        if (this.fftPersist == null) {
            this.fftPersist = fftPersist;
        } else {
            this.fftPersist = fftPersist;
            setAllUnSelect();
            this.fftPersist.setRxMsgSelect(true);
        }
    }


    public RxBooleanWithSelect getClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        if (this.clear == null) {
            this.clear = new RxBooleanWithSelect(clear);
        } else {
            this.clear.setValue(clear);
            setAllUnSelect();
            this.clear.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getFftClear() {
        return fftClear;
    }

    public void setFftClear(boolean fftClear) {
        if (this.fftClear == null) {
            this.fftClear = new RxBooleanWithSelect(fftClear);
        } else {
            this.fftClear.setValue(fftClear);
            setAllUnSelect();
            this.fftClear.setRxMsgSelect(true);
        }
    }

    public TopBeanHorizontal getAdjust() {
        return adjust;
    }

    public void setAdjust(TopBeanHorizontal adjust) {
        if (this.adjust == null) {
            this.adjust = adjust;
        } else {
            this.adjust = adjust;
            setAllUnSelect();
            this.adjust.setRxMsgSelect(true);
        }
    }

    public TopBeanHorizontal getFftAdjust() {
        return fftAdjust;
    }

    public void setFftAdjust(TopBeanHorizontal fftAdjust) {
        if (this.fftAdjust == null) {
            this.fftAdjust = fftAdjust;
        } else {
            this.fftAdjust = fftAdjust;
            setAllUnSelect();
            this.fftAdjust.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        persist.setRxMsgSelect(false);
        fftPersist.setRxMsgSelect(false);
        clear.setRxMsgSelect(false);
        fftClear.setRxMsgSelect(false);
        adjust.setRxMsgSelect(false);
        fftAdjust.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgDisplayPersist{" +
                "persist=" + persist +
                ", clear=" + clear +
                ", adjust=" + adjust +
                ", fftPersist=" + fftPersist +
                ", fftClear=" + fftClear +
                ", fftAdjust=" + fftAdjust +
                '}';
    }
}
