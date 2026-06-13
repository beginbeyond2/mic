package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

import android.os.Parcel;
import android.os.Parcelable;

import com.micsig.tbook.ui.bean.RxMsgSelect;

/**
 * Created by yangj on 2017/4/27.
 */

public class Serials extends RxMsgSelect implements Parcelable{
    private int id;
    private String name;
    private boolean enabled = true;
    private boolean selected = false;
    private int detailFlag;
    private String cacheListKey;

    protected Serials(Parcel in) {
        id = in.readInt();
        name = in.readString();
        enabled = in.readByte() != 0;
        selected = in.readByte() != 0;
        detailFlag = in.readInt();
        cacheListKey = in.readString();
    }

    public static final Creator<Serials> CREATOR = new Creator<Serials>() {
        @Override
        public Serials createFromParcel(Parcel in) {
            return new Serials(in);
        }

        @Override
        public Serials[] newArray(int size) {
            return new Serials[size];
        }
    };

    public int getDetailFlag() {
        return detailFlag;
    }

    public void setDetailFlag(int detailFlag) {
        this.detailFlag = detailFlag;
    }

    public String getCacheListKey() {
        return cacheListKey;
    }

    public void setCacheListKey(String cacheListKey) {
        this.cacheListKey = cacheListKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Serials(String name, int id, String cacheListKey, int detailFlag) {
        this.name = name;
        this.id = id;
        this.cacheListKey = cacheListKey;
        this.detailFlag = detailFlag;
    }

    @Override
    public String toString() {
        return "Serials{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", selected=" + selected +
                ", rxMsgSelect=" + rxMsgSelect +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeInt(detailFlag);
        dest.writeString(cacheListKey);
    }
}
