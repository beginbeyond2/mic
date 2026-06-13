package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsLinIdData extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit linIdDataId;
    private TopViewEdit linIdDataData;
    private SerialsDetailLinIdData msgLinIdData;

    @Override
    protected void initView(View view) {
        linIdDataId = (TopViewEdit) view.findViewById(R.id.linIdDataId);
        linIdDataData = (TopViewEdit) view.findViewById(R.id.linIdDataData);
        linIdDataId.setOnClickEditListener(onClickEditListener);
        linIdDataData.setOnClickEditListener(onClickEditListener);
        msgLinIdData = new SerialsDetailLinIdData();
        msgLinIdData.setLinIdDataId(DIGITS_16X, linIdDataId.getText());
        msgLinIdData.setLinIdDataData(DIGITS_16, linIdDataData.getText());
        msgLinIdData.setLinIdDataIdTitle(linIdDataId.getHead());
        msgLinIdData.setLinIdDataDataTitle(linIdDataData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_liniddata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber());
        String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber());//帧ID有没有输入X
        if(dataID.contains("X")){
            msgLinIdData.setLinIdDataId(DIGITS_16X, dataID);
        } else {
            msgLinIdData.setLinIdDataId(DIGITS_16X, id);
        }
        msgLinIdData.setLinIdDataData(DIGITS_16, data);
        return msgLinIdData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 2) {
            //当且仅当当前lin列表选中的是该项时，才向外发送消息
            sendMsg(msgLinIdData, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber());
        String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber());
        if (dataID.contains("X")) {
            linIdDataId.setText(dataID);
        } else {
            linIdDataId.setText(id);
        }
        linIdDataData.setText(data);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
        linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(id, data));
        linBus.setData(toDLong(data, 16));

        msgLinIdData.setLinIdDataId(DIGITS_16X, id);
        msgLinIdData.setLinIdDataData(DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 2) {
            sendMsg(msgLinIdData, false);
        }
    }

    public void setCommandData(int id, long data, boolean isFromEventBus) {
        String sId = SerialsUtils.getHexBinFromLong(id, 2, DIGITS_16);
        String sData = SerialsUtils.getHexBinFromLong(data, 16, DIGITS_16);
        if (!linIdDataId.getText().equals(sId)) {
            onTextListener(linIdDataId, sId, sId, isFromEventBus);
        }
        if (!linIdDataData.getText().equals(sData)) {
            onTextListener(linIdDataData, sData, sData, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.linIdDataId:
                //该为16进制,两位限制
                dialogKeyBoard.setOriginalDecimalData(2, DIGITS_16X, onIdListener);
                break;
            case R.id.linIdDataData:
                //该为16进制,四位限制
                dialogKeyBoard.setOriginalDecimalData(getBitFromId(linIdDataId.getText()), DIGITS_16, onDataListener);
                break;
        }
    }

    private int getBitFromId(String id) {
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        int bit = 4;
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return bit;
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
        if (linBus.getLinType() == LinBus.LIN_TYPE_1_3) {
            if (id.charAt(0) - '0' == 0 || id.charAt(0) - '0' == 1) {
                bit = 4;
            } else if (id.charAt(0) - '0' == 2) {
                bit = 8;
            } else if (id.charAt(0) - '0' == 3 || id.contains("X")) {
                bit = 16;
            }
        } else {
            bit = 16;
        }
        return bit;
    }

    private int getMatchFid(String id, String data) {
        int dataLength = data.replace(" ", "").trim().length();
        if (id.contains("X")) {
            if (dataLength <= 4) {
                id = "1F";
            } else if (dataLength <= 8) {
                id = "2F";
            } else {
                id = "3F";
            }
        }
        return toD(id, 16);
    }

    TopDialogNumberKeyBoard.OnOriginalDismissListener onIdListener = new TopDialogNumberKeyBoard.OnOriginalDismissListener() {
        @Override
        public void onOriginalDismiss(String result, String originalResult) {
            onTextListener(linIdDataId, result, originalResult, false);
        }
    };

    TopDialogNumberKeyBoard.OnOriginalDismissListener onDataListener = new TopDialogNumberKeyBoard.OnOriginalDismissListener() {
        @Override
        public void onOriginalDismiss(String result, String originalResult) {
            onTextListener(linIdDataData, result, originalResult, false);
        }
    };

    private void onTextListener(TopViewEdit view, String text, String originalResult, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        int type = 2;// 2代表帧ID和数据触发，3代表单独数据触发
        if (view.getId() == linIdDataId.getId()) {
            int bit = 4;
            if (text.contains("X")) {
                text = "X";
                bit = 16;//帧ID设置为X时候，数据可设置8个字节
                type =3;
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber(), text);
            } else {
                text = (text.charAt(0) - '0') > 3 ? "3F" : text;//此结果最大值为3F
                bit = getBitFromId(text);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber(), "");
            }
            linIdDataId.setEdit(text);
            String data = linIdDataData.getText().replace(" ", "");
            if (data.length() < bit) {
                data = KeyBoardNumberUtil.reCalculateSpace(
                        linIdDataData.getText().replace(" ", ""),
                        KeyBoardNumberUtil.DIGITS_16).trim();
            } else {
                data = KeyBoardNumberUtil.reCalculateSpace(
                        KeyBoardNumberUtil.toBits(
                                linIdDataData.getText().replace(" ", "")
                                , bit), KeyBoardNumberUtil.DIGITS_16).trim();
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber(), Long.toHexString(getMatchFid(text, data)).toUpperCase());
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber(), data);
            linIdDataData.setEdit(data);
            msgLinIdData.setLinIdDataId(DIGITS_16, text);
            msgLinIdData.setLinIdDataData(DIGITS_16, data);
            sendMsg(msgLinIdData, isFromEventBus);
            //这里需要根据 data length匹配id
            Command.get().getTrigger_lin().setType(cmdCh, type, getMatchFid(linIdDataId.getText(), data), toDLong(linIdDataData.getText(), IDigits.DIGITS_16), false);
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
            if (!isFromEventBus) {
                if (type == 3) {
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA);
                    linBus.setFrameId(LinBus.LIN_TRIGGER_DATA, getMatchFid(text, data));
                } else {
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA);
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(text, data));
                }
                linBus.setData(toDLong(linIdDataData.getText(), IDigits.DIGITS_16));
                linBus.setDataLength(data.replace(" ", "").length());
            }
        } else if (view.getId() == linIdDataData.getId()) {
            String linIdD = linIdDataId.getText();
            if (linIdD.contains("X")) {
                type = 3;
            }
            if (originalResult.replace(" ", "").length() < 4) {//最少2个字节
                originalResult = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(
                        originalResult.replace(" ", "")
                        , 4), IDigits.DIGITS_16);
            }
            linIdDataData.setEdit(originalResult);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber(), originalResult);
            msgLinIdData.setLinIdDataData(DIGITS_16, originalResult);
            sendMsg(msgLinIdData, isFromEventBus);
            Command.get().getTrigger_lin().setType(cmdCh, type, getMatchFid(linIdD, originalResult), toDLong(linIdDataData.getText(), IDigits.DIGITS_16), false);
            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if (serialChannel == null) return;
                LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
                linBus.setData(toDLong(linIdDataData.getText(), IDigits.DIGITS_16));
                if (type == 3) {
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA);
                    linBus.setFrameId(LinBus.LIN_TRIGGER_DATA, getMatchFid(linIdD, linIdDataData.getText()));
                } else {
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA);
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(linIdD, linIdDataData.getText()));
                }
                linBus.setDataLength(originalResult.replace(" ", "").length());
            }
        }
    }
}
