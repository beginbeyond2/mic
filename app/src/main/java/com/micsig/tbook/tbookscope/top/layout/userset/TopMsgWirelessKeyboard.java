package com.micsig.tbook.tbookscope.top.layout.userset;

public class TopMsgWirelessKeyboard {
    private long sn;
    private int batterylevel;

    private long wirelessBatteryHeartbeat;
    public TopMsgWirelessKeyboard(long sn, int level, long wirelessBatteryHeartbeat){
        this.sn = sn;
        this.batterylevel = level;
        this.wirelessBatteryHeartbeat = wirelessBatteryHeartbeat;
    }

    public long getSn() {
        return sn;
    }

    public int getBatterylevel() {
        return batterylevel;
    }
}
