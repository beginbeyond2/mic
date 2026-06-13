package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.util.Log;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Data;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsArinc429Data extends TopLayoutTriggerSerialsBaseDetail {
    private int format;

    private SerialsDetailArinc429Data msgArinc429Data;
    private TopViewEdit arinc429DataData;

    @Override
    protected void initView(View view) {
        arinc429DataData = (TopViewEdit) view.findViewById(R.id.arinc429DataData);
        arinc429DataData.setOnClickEditListener(onClickEditListener);
        msgArinc429Data = new SerialsDetailArinc429Data();
        msgArinc429Data.setArinc429DataData(digits, arinc429DataData.getText());
        msgArinc429Data.setArinc429DataDataTitle(arinc429DataData.getHead());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429data;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber());
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber());
        data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        msgArinc429Data.setArinc429DataData(display == 0 ? DIGITS_2 : DIGITS_16, data);
        return msgArinc429Data;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (serialsNumber == rightMsgSerials.getSerialsNumber()
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
            format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + getSerialsNumber());
            if (arinc429DataData != null) {
                String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber());
                data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
                arinc429DataData.setText(data);
                msgArinc429Data.setArinc429DataData(digits, arinc429DataData.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 4) {
                    //当且仅当当前429列表选中的是该项时，才向外发送消息
                    sendMsg(msgArinc429Data, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber());
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + getSerialsNumber());
        data = reCalcSpace(HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        arinc429DataData.setText(data);

        int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_DATA, toD(data, digits));

        msgArinc429Data.setArinc429DataData(display == 0 ? DIGITS_2 : DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber == CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber == CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber == CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber == CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 4) {
            sendMsg(msgArinc429Data, false);
        }
    }

    public void setCommandData(int data, boolean isFromEventBus) {
        String sData = SerialsUtils.getHexBinFromLong(data, SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
        if (!arinc429DataData.getText().equals(sData)) {
            onTextListener(sData, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429DataData:
                //该为2进制时，根据格式依次为，23、21、19位限制//16进制时，根据格式依次为6、6、5位
                dialogKeyBoard.setDecimalData(SerialsUtils.getBitFor429Data(getSerialsNumber()), digits, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber(), text);
        arinc429DataData.setEdit(text);
        msgArinc429Data.setArinc429DataData(digits, text);
        sendMsg(msgArinc429Data, isFromEventBus);
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber);
        Command.get().getTrigger_m429().setType(cmdCh, 4, 0, 0, toD(text, digits), 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
            if(serialChannel == null) return;
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
            a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_DATA, toD(text, digits));
        }
    }
}
