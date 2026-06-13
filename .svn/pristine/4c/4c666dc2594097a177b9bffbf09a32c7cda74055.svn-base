package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

/**
 * Created by yangj on 2017/12/1.
 */

class KeyBoardTextItem {
    public interface SpecialKey {
        String DELETE = "delete";
        String PLACEHOLDER = "placeholder";
        String ENTER = "Enter";
        String UPPER = "upper";
        String HIDE = "hide";
        String NUMBER = "123";
        String SPACE = "space";
        String SYMBOL = "!@#";
        String LANG_ENG = "Eng";
        String ENTER_CN = "确认";
        String HIDE_CN = "隐藏";
        String LANG_CN = "中";

        int INDEX_DELETE = 10;
        int INDEX_PLACEHOLDER = 11;
        int INDEX_ENTER = 21;
        int INDEX_UPPER = 22;
        int INDEX_HIDE = 33;
        int INDEX_NUMBER = 34;
        int INDEX_SPACE = 35;
        int INDEX_SYMBOL = 36;
        int INDEX_LANG = 37;
    }

    private int index;
    private String word;//文字
    private int size;//大小

    public KeyBoardTextItem(int index, String word, int size) {
        this.index = index;
        this.word = word;
        this.size = size;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "KeyBoardTextItem{" +
                "" + index +
                ", '" + word + '\'' +
                ", " + size +
                '}';
    }
}
