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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsUart1Data extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit uart1DataEdit;
    private TopViewRadioGroup uart1DataCondition;
    private SerialsDetailUart1Data msgUart1Data;

    @Override
    protected void initView(View view) {
        uart1DataCondition = (TopViewRadioGroup) view.findViewById(R.id.uart1DataCondition);
        uart1DataEdit = (TopViewEdit) view.findViewById(R.id.uart1DataEdit);
        uart1DataCondition.setOnListener(onCheckChangedListener);
        uart1DataEdit.setOnClickEditListener(onClickEditListener);
        msgUart1Data = new SerialsDetailUart1Data();
        msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected());
        msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText());
        msgUart1Data.setUart1DataConditionTitle(uart1DataCondition.getHead());
        msgUart1Data.setUart1DataEditTitle(uart1DataEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_uart1data;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber());
        if (!edit.equals(msgUart1Data.getUart1DataEdit().getValue())) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
            uart1DataEdit.setText(edit);
            msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText());
        }
        if (msgUart1Data.getUart1DataCondition().getIndex() != condition) {
            uart1DataCondition.setSelectedIndex(condition);
            msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected());
        }
        return msgUart1Data;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) {
            RightMsgSerialsUart detailsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails();
            bits = detailsUart.getIntBits();
            digits = detailsUart.getIntDigits(getContext());
            if (uart1DataEdit != null) {
                String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber());
                uart1DataEdit.setText(edit);
                msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 4) {
                    //当且仅当当前uart列表选中的是该项时，才向外发送消息
                    sendMsg(msgUart1Data, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
        uart1DataCondition.setSelectedIndex(condition);
        uart1DataEdit.setText(edit);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
        uartBus.setTriggerRelation(getConditionValue(condition));
        uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, toD(edit, digits));

        msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected());
        msgUart1Data.setUart1DataEdit(digits, edit);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_UART
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 4) {
            sendMsg(msgUart1Data, false);
        }
    }

    public void setCommandData(int condition, int data, boolean isFromEventBus) {
        String sData = SerialsUtils.getHexBinFromLong(data, digits == DIGITS_16 ? 2 : 9, digits);
        if (!uart1DataEdit.getText().equals(sData)) {
            onTextListener(sData, isFromEventBus);
        }
        if (uart1DataCondition.getSelected().getIndex() != condition) {
            uart1DataCondition.setSelectedIndex(condition);
            onCheckListener(uart1DataCondition, uart1DataCondition.getSelected(), isFromEventBus);
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
            case R.id.uart1DataEdit:
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
        if (view.getId() == R.id.uart1DataCondition) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex()));
            msgUart1Data.setUart1DataCondition(item);
            sendMsg(msgUart1Data, isFromEventBus);
            Command.get().getTrigger_uart().setType(cmdCh, 4, item.getIndex(), toD(uart1DataEdit.getText(), digits), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if (serialChannel == null) return;
                UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
                uartBus.setTriggerRelation(getConditionValue(item.getIndex()));
            }
        }
    }

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh= TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber(), text);
        uart1DataEdit.setEdit(text);
        msgUart1Data.setUart1DataEdit(digits, text);
        sendMsg(msgUart1Data, isFromEventBus);
        Command.get().getTrigger_uart().setType(cmdCh, 4, uart1DataCondition.getSelected().getIndex(), toD(text, digits), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
            uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, toD(text, digits));
        }
    }
}
