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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart0Data;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsUart0Data extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewRadioGroup uart0DataCondition;
    private TopViewEdit uart0DataEdit;
    private SerialsDetailUart0Data msgUart0Data;

    @Override
    protected void initView(View view) {
        uart0DataCondition = (TopViewRadioGroup) view.findViewById(R.id.uart0DataCondition);
        uart0DataEdit = (TopViewEdit) view.findViewById(R.id.uart0DataEdit);
        uart0DataCondition.setOnListener(onCheckChangedListener);
        uart0DataEdit.setOnClickEditListener(onClickEditListener);
        msgUart0Data = new SerialsDetailUart0Data();
        msgUart0Data.setUart0DataCondition(uart0DataCondition.getSelected());
        msgUart0Data.setUart0DataEdit(digits, uart0DataEdit.getText());
        msgUart0Data.setUart0DataConditionTitle(uart0DataCondition.getHead());
        msgUart0Data.setUart0DataEditTitle(uart0DataEdit.getHead());

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_uart0data;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber());
        if (!edit.equals(msgUart0Data.getUart0DataEdit().getValue())) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
            uart0DataEdit.setText(edit);
            msgUart0Data.setUart0DataEdit(digits, uart0DataEdit.getText());
        }
        if (msgUart0Data.getUart0DataCondition().getIndex() != condition) {
            uart0DataCondition.setSelectedIndex(condition);
            msgUart0Data.setUart0DataCondition(uart0DataCondition.getSelected());
        }
        return msgUart0Data;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) {
            RightMsgSerialsUart detailsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails();
            bits = detailsUart.getIntBits();
            digits = detailsUart.getIntDigits(getContext());
            if (uart0DataEdit != null) {
                String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber());
                uart0DataEdit.setText(edit);
                msgUart0Data.setUart0DataEdit(digits, uart0DataEdit.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 3) {
                    //当且仅当当前uart列表选中的是该项时，才向外发送消息
                    sendMsg(msgUart0Data, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
        uart0DataCondition.setSelectedIndex(condition);
        uart0DataEdit.setText(edit);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
        uartBus.setTriggerRelation(getConditionValue(condition));
        uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA0, toD(edit, digits));

        msgUart0Data.setUart0DataCondition(uart0DataCondition.getSelected());
        msgUart0Data.setUart0DataEdit(digits, edit);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_UART
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 3) {
            sendMsg(msgUart0Data, false);
        }
    }

    public void setCommandData(int condition, int data, boolean isFromEventBus) {
        String sData = SerialsUtils.getHexBinFromLong(data, digits == DIGITS_16 ? 2 : 9, digits);
        if (!uart0DataEdit.getText().equals(sData)) {
            onTextListener(sData, isFromEventBus);
        }
        if (uart0DataCondition.getSelected().getIndex() != condition) {
            uart0DataCondition.setSelectedIndex(condition);
            onCheckListener(uart0DataCondition, uart0DataCondition.getSelected(), isFromEventBus);
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
            case R.id.uart0DataEdit:
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
        if (view.getId() == R.id.uart0DataCondition) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex()));
            msgUart0Data.setUart0DataCondition(item);
            sendMsg(msgUart0Data, isFromEventBus);
            Command.get().getTrigger_uart().setType(cmdCh, 3, item.getIndex(), toD(uart0DataEdit.getText(), digits), false);

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
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber(), text);
        uart0DataEdit.setEdit(text);
        msgUart0Data.setUart0DataEdit(digits, text);
        sendMsg(msgUart0Data, isFromEventBus);
        Command.get().getTrigger_uart().setType(cmdCh, 3, uart0DataCondition.getSelected().getIndex(), toD(text, digits), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
            uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA0, toD(text, digits));
        }
    }
}
