package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.UartBus;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartxData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsUartxData extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewRadioGroup uartxDataCondition;
    private TopViewEdit uartxDataEdit;
    private SerialsDetailUartxData msgUartxData;

    @Override
    protected void initView(View view) {
        uartxDataCondition = (TopViewRadioGroup) view.findViewById(R.id.uartxDataCondition);
        uartxDataEdit = (TopViewEdit) view.findViewById(R.id.uartxDataEdit);
        uartxDataCondition.setOnListener(onCheckChangedListener);
        uartxDataEdit.setOnClickEditListener(onClickEditListener);
        msgUartxData = new SerialsDetailUartxData();
        msgUartxData.setUartxDataCondition(uartxDataCondition.getSelected());
        msgUartxData.setUartxDataEdit(digits, uartxDataEdit.getText());
        msgUartxData.setUartxDataConditionTitle(uartxDataCondition.getHead());
        msgUartxData.setUartxDataEditTitle(uartxDataEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_uartxdata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber());
        if (!edit.equals(msgUartxData.getUartxDataEdit().getValue())) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
            uartxDataEdit.setText(edit);
            msgUartxData.setUartxDataEdit(digits, uartxDataEdit.getText());
        }
        if (msgUartxData.getUartxDataCondition().getIndex() != condition) {
            uartxDataCondition.setSelectedIndex(condition);
            msgUartxData.setUartxDataCondition(uartxDataCondition.getSelected());
        }
        return msgUartxData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) {
            RightMsgSerialsUart detailsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails();
            bits = detailsUart.getIntBits();
            digits = detailsUart.getIntDigits(getContext());
            if (uartxDataEdit != null) {
                String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber());
                uartxDataEdit.setText(edit);
                msgUartxData.setUartxDataEdit(digits, uartxDataEdit.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 5) {
                    //当且仅当当前uart列表选中的是该项时，才向外发送消息
                    sendMsg(msgUartxData, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
        uartxDataCondition.setSelectedIndex(condition);
        uartxDataEdit.setText(edit);


        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
        uartBus.setTriggerRelation(getConditionValue(condition));
        uartBus.setTriggerData(UartBus.UART_TRIGGER_DATAx, toD(edit, digits));

        msgUartxData.setUartxDataCondition(uartxDataCondition.getSelected());
        msgUartxData.setUartxDataEdit(digits, edit);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_UART
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 5) {
            sendMsg(msgUartxData, false);
        }
    }

    public void setCommandData(int condition, int data, boolean isFromEventBus) {
        String sData = SerialsUtils.getHexBinFromLong(data, digits == DIGITS_16 ? 2 : 9, digits);
        if (!uartxDataEdit.getText().equals(sData)) {
            onTextListener(sData, isFromEventBus);
        }
        if (uartxDataCondition.getSelected().getIndex() != condition) {
            uartxDataCondition.setSelectedIndex(condition);
            onCheckListener(uartxDataCondition, uartxDataCondition.getSelected(), isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {
        onCheckListener(view, item, false);
    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.uartxDataEdit:
                //该为16进制，两位限制//2进制9位
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 2 : 9, digits, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onCheckListener(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == R.id.uartxDataCondition) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex()));
            msgUartxData.setUartxDataCondition(item);
            sendMsg(msgUartxData, isFromEventBus);
            Command.get().getTrigger_uart().setType(cmdCh, 5, item.getIndex(), toD(uartxDataEdit.getText(), digits), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if (serialChannel == null) return;
                UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
                uartBus.setTriggerRelation(getConditionValue(item.getIndex()));
            }
        }
    }

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber(), text);
        uartxDataEdit.setEdit(text);
        msgUartxData.setUartxDataEdit(digits, text);
        sendMsg(msgUartxData, isFromEventBus);
        Command.get().getTrigger_uart().setType(cmdCh, 5, uartxDataCondition.getSelected().getIndex(), toD(text, digits), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
            uartBus.setTriggerData(UartBus.UART_TRIGGER_DATAx, toD(text, digits));
        }
    }
}
