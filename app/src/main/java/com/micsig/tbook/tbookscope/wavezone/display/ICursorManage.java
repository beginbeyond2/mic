package com.micsig.tbook.tbookscope.wavezone.display;

/**
 * Created by liwb on 2017/11/6.
 */

public interface ICursorManage {
    public void setRowVisible(boolean visible);
    public void setColVisible(boolean visible);

    public void setCursorChannelColor(int ChNo);
    public int selectCursor(int x, double y);
    public void setSelectCursor(int index);
    public void moveSelectCursor(int x, double y);
    public void moveMultiSelectCursor(int x, int y);
    public void moveFinish();

    public void setCursorTracking(int ChNo);
    public int getCurrSelectCursor();
}
