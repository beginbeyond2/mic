package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.ARINC429Bus;
import com.micsig.tbook.scope.Bus.IBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsArinc429LabelData extends TopLayoutTriggerSerialsBaseDetail {
    private int format;

    private SerialsDetailArinc429LabelData msgArinc429LabelData;
    private TopViewEdit arinc429LabelDataData;
    private TopViewEdit arinc429LabelDataLabel;

    @Override
    protected void initView(View view) {
        arinc429LabelDataLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelDataLabel);
        arinc429LabelDataData = (TopViewEdit) view.findViewById(R.id.arinc429LabelDataData);
        arinc429LabelDataLabel.setOnClickEditListener(onClickEditListener);
        arinc429LabelDataData.setOnClickEditListener(onClickEditListener);
        msgArinc429LabelData = new SerialsDetailArinc429LabelData();
        msgArinc429LabelData.setArinc429LabelDataLabel(DIGITS_8, arinc429LabelDataLabel.getText());
        msgArinc429LabelData.setArinc429LabelDataData(digits, arinc429LabelDataData.getText());
        msgArinc429LabelData.setArinc429LabelDataLabelTitle(arinc429LabelDataLabel.getHead());
        msgArinc429LabelData.setArinc429LabelDataDataTitle(arinc429LabelDataData.getHead());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429labeldata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        msgArinc429LabelData.setArinc429LabelDataLabel(DIGITS_8, label);
        msgArinc429LabelData.setArinc429LabelDataData(digits, data);
        return msgArinc429LabelData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
            format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + getSerialsNumber());
            if (arinc429LabelDataData != null) {
                String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber());
                data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
                arinc429LabelDataData.setText(data);
                msgArinc429LabelData.setArinc429LabelDataData(digits, arinc429LabelDataData.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 7) {
                    //当且仅当当前429列表选中的是该项时，才向外发送消息
                    sendMsg(msgArinc429LabelData, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + getSerialsNumber());
        data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        arinc429LabelDataLabel.setText(label);
        arinc429LabelDataData.setText(data);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, toD(label, DIGITS_8));
        a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, toD(data, digits));

        msgArinc429LabelData.setArinc429LabelDataLabel(DIGITS_8, label);
        msgArinc429LabelData.setArinc429LabelDataData(digits, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 7) {
            sendMsg(msgArinc429LabelData, false);
        }
    }

    public void setCommandData(int label, int data, boolean isFromEventBus) {
        String sLabel = SerialsUtils.getHexBinFromLong(label, 2, DIGITS_8);
        String sData = SerialsUtils.getHexBinFromLong(data, SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        if (!arinc429LabelDataLabel.getText().equals(sLabel)) {
            onTextListener(arinc429LabelDataLabel, sLabel, isFromEventBus);
        }
        if (!arinc429LabelDataData.getText().equals(sData)) {
            onTextListener(arinc429LabelDataData, sData, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429LabelDataLabel:
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener);
                break;
            case R.id.arinc429LabelDataData:
                //该为2进制时，根据格式依次为，23、21、19位限制//16进制时，根据格式依次为6、6、5位
                dialogKeyBoard.setDecimalData(SerialsUtils.getBitFor429Data(getSerialsNumber()), digits, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelDataLabel, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelDataData, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == arinc429LabelDataLabel.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + getSerialsNumber(), text);
            arinc429LabelDataLabel.setEdit(text);
            msgArinc429LabelData.setArinc429LabelDataLabel(DIGITS_8, text);
            sendMsg(msgArinc429LabelData, isFromEventBus);
            Command.get().getTrigger_m429().setType(cmdCh, 7, toD(arinc429LabelDataLabel.getText(), DIGITS_8), 0, toD(arinc429LabelDataData.getText(), digits), 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, toD(text, DIGITS_8));
            }
        } else if (view.getId() == arinc429LabelDataData.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber(), text);
            arinc429LabelDataData.setEdit(text);
            msgArinc429LabelData.setArinc429LabelDataData(digits, text);
            sendMsg(msgArinc429LabelData, isFromEventBus);
            Command.get().getTrigger_m429().setType(cmdCh, 7, toD(arinc429LabelDataLabel.getText(), DIGITS_8), 0, toD(arinc429LabelDataData.getText(), digits), 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, toD(text, digits));
            }
        }
    }
}
