package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

/**
 * Created by yangj on 2017/12/29.
 */

public class KeyBoardCandidatesItem {
    private int index;
    private String text;
    private boolean select;

    public KeyBoardCandidatesItem(int index, String text, boolean select) {
        this.index = index;
        this.text = text;
        this.select = select;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public String toString() {
        return "KeyBoardCandidatesItem{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", select=" + select +
                '}';
    }
}
