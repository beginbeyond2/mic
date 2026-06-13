package com.micsig.tbook.tbookscope.rightslipmenu.dialog;



/**
 * Created by yangj on 2017/5/3.
 */

public class DialogRefRecallBean {
    private int index;
    private String title;
    private String time;
    private boolean select;
    private String pathFile;
    public long lastModifyTime;

    public DialogRefRecallBean(int index, String title, String time,String pathFile,long lastModifyTime, boolean select) {
        this.index = index;
        this.title = title;
        this.time = time;
        this.pathFile=pathFile;
        this.select = select;
        this.lastModifyTime=lastModifyTime;
    }
   public   DialogRefRecallBean(){}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
}
