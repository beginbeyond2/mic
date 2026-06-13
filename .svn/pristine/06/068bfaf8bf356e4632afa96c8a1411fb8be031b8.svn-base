package com.micsig.tbook.tbookscope.main;

/**
 * 外部按键发送触发电平、阈值电平数值改变时的消息实体类
 */
public class ExternalKeysMsgLevel {
    public static final int TYPE_TRIGGER_MOVEUP = 1;
    public static final int TYPE_TRIGGER_MOVEDOMN = 2;
    public static final int TYPE_TRIGGER_MOVECENTER = 3;
    public static final int TYPE_TRIGGER_SOURCEUP = 4;
    public static final int TYPE_TRIGGER_SOURCEDOWN = 5;
    public static final int TYPE_VALUE_MOVEUP = 6;
    public static final int TYPE_VALUE_MOVEDOMN = 7;
    public static final int TYPE_VALUE_MOVECENTER = 8;
    public static final int TYPE_VALUE_SOURCEUP = 9;
    public static final int TYPE_VALUE_SOURCEDOWN = 10;

    private int levelType;
    private int count;

    public ExternalKeysMsgLevel(int levelType, int count) {
        this.levelType = levelType;
        this.count = count;
    }

    public int getLevelType() {
        return levelType;
    }

    public void setLevelType(int levelType) {
        this.levelType = levelType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
