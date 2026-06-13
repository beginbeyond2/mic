package com.micsig.tbook.tbookscope.top.layout.auto;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxIntWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAutoSet implements IAutoDetail {
    private RxBooleanWithSelect openChannel;
    private TopBeanChannel triggerSource;
    private TopBeanChannel levelSelect;
    private RxIntWithSelect levelDetail;//最大值99999

    public RxBooleanWithSelect getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(boolean openChannel) {
        if (this.openChannel == null) {
            this.openChannel = new RxBooleanWithSelect(openChannel);
        } else {
            this.openChannel.setValue(openChannel);
            setAllUnSelect();
            this.openChannel.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(TopBeanChannel triggerSource) {
        if (this.triggerSource == null) {
            this.triggerSource = triggerSource;
        } else {
            this.triggerSource = triggerSource;
            setAllUnSelect();
            this.triggerSource.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getLevelSelect() {
        return levelSelect;
    }

    public void setLevelSelect(TopBeanChannel levelSelect) {
        if (this.levelSelect == null) {
            this.levelSelect = levelSelect;
        } else {
            this.levelSelect = levelSelect;
            setAllUnSelect();
            this.levelSelect.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getLevelDetail() {
        return levelDetail;
    }

    public void setLevelDetail(int levelDetail) {
        if (this.levelDetail == null) {
            this.levelDetail = new RxIntWithSelect(levelDetail);
        } else {
            this.levelDetail.setValue(levelDetail);
            setAllUnSelect();
            this.levelDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        openChannel.setRxMsgSelect(false);
        triggerSource.setRxMsgSelect(false);
        levelSelect.setRxMsgSelect(false);
        levelDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgAutoSet{" +
                "openChannel=" + openChannel +
                ", triggerSource=" + triggerSource +
                ", levelSelect=" + levelSelect +
                ", levelDetail='" + levelDetail + '\'' +
                '}';
    }
}
