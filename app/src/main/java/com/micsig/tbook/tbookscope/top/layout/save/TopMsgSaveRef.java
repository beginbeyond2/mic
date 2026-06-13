package com.micsig.tbook.tbookscope.top.layout.save;

public class TopMsgSaveRef {
    private int saveToRefId;
    private int fromIdChannelFactory;

    public int getSaveToRefId() {
        return saveToRefId;
    }

    public void setSaveToRefId(int saveToRefId) {
        this.saveToRefId = saveToRefId;
    }

    public int getFromIdChannelFactory() {
        return fromIdChannelFactory;
    }

    public void setFromIdChannelFactory(int fromIdChannelFactory) {
        this.fromIdChannelFactory = fromIdChannelFactory;
    }

    @Override
    public String toString() {
        return "TopMsgSaveRef{" +
                "saveToRefId=" + saveToRefId +
                ", fromIdChannelFactory=" + fromIdChannelFactory +
                '}';
    }
}
