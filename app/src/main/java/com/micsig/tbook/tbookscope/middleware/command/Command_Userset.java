package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;

import java.util.Objects;

/**
 * Created by yangj on 2018/1/19.
 */

public class Command_Userset {
    private int length;
    private String names[] = new String[]{"", "", "", "", "", "", "", "", "", ""};

    public int getLength() {
        return length;
    }

    public void setLength(int length, boolean isUpdateUI) {
        if (this.length == length) return;
        this.length = length;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_LENGTH);
            msgToUI.setParam(String.valueOf(length));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setFactoryReset(boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_FACTORYRESET);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setSelfAdjust(boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SELFADJUST);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setAutoZero(boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_AutoZero);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }


    public String getName(int index) {
        return names[index];
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(int index, String name, boolean isUpdateUI) {
        if (Objects.equals(this.names[index], name)) return;
        this.names[index] = name;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_NAME);
            msgToUI.setParam(String.valueOf(index) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(name));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setSave(int index, boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SAVE);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setSave(String name, boolean isUpdateUI) {
        int index = -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                index = i;
                break;
            }
        }
        setSave(index, isUpdateUI);
    }

    public void setRecovery(int index, boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_RECOVERY);
            msgToUI.setParam(String.valueOf(index));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public void setRecovery(String name, boolean isUpdateUI) {
        int index = -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                index = i;
                break;
            }
        }
        setRecovery(index, isUpdateUI);
    }
}
