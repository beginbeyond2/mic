package com.micsig.tbook.tbookscope.main.mainbottom;

/**
 * @auother Liwb
 * @description:
 * @data:2022-2-14 9:31
 */
public class MainBottomMsgQuick {
    private boolean[] enable;

    public boolean[] getEnable() {
        return enable;
    }

    public void setEnable(int index, boolean enable) {
        this.enable[index] = enable;
    }

    public void setEnable(boolean[] enable) {
        this.enable = enable;
    }
}
