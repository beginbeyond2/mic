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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsUartData extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewRadioGroup uartDataCondition;
    private TopViewEdit uartDataEdit;
    private SerialsDetailUartData msgUartData;

    @Override
    protected void initView(View view) {
        uartDataCondition = (TopViewRadioGroup) view.findViewById(R.id.uartDataCondition);
        uartDataEdit = (TopViewEdit) view.findViewById(R.id.uartDataEdit);
        uartDataCondition.setOnListener(onCheckChangedListener);
        uartDataEdit.setOnClickEditListener(onClickEditListener);
        msgUartData = new SerialsDetailUartData();
        msgUartData.setUartDataCondition(uartDataCondition.getSelected());
        msgUartData.setUartDataConditionTitle(uartDataCondition.getHead());
        msgUartData.setUartDataEdit(digits, uartDataEdit.getText());
        msgUartData.setUartDataEditTitle(uartDataEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_uartdata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber());
        if (!edit.equals(msgUartData.getUartDataEdit().getValue())) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
            uartDataEdit.setText(edit);
            msgUartData.setUartDataEdit(digits, uartDataEdit.getText());
        }
        if (msgUartData.getUartDataCondition().getIndex() != condition) {
            uartDataCondition.setSelectedIndex(condition);
            msgUartData.setUartDataCondition(uartDataCondition.getSelected());
        }
        return msgUartData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) {
            RightMsgSerialsUart detailsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails();
            bits = detailsUart.getIntBits();
            digits = detailsUart.getIntDigits(getContext());
            if (uartDataEdit != null) {
                String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber());
//                digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
                uartDataEdit.setText(edit);
                msgUartData.setUartDataEdit(digits, uartDataEdit.getText());
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber(), uartDataEdit.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 2) {
                    //当且仅当当前uart列表选中的是该项时，才向外发送消息
                    sendMsg(msgUartData, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + getSerialsNumber());
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16;
        uartDataCondition.setOnListener(null);
        uartDataCondition.setSelectedIndex(condition);
        uartDataCondition.setOnListener(onCheckChangedListener);
        uartDataEdit.setText(edit);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
        uartBus.setTriggerRelation(getConditionValue(condition));
        uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA, toD(edit, digits));

        msgUartData.setUartDataCondition(uartDataCondition.getSelected());
        msgUartData.setUartDataEdit(digits, edit);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (
                ((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber == CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber == CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber == CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber == CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_UART
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 2) {
            sendMsg(msgUartData, false);
        }
    }

    public void setCommandData(int conditionIndex, int data, boolean isFromEventBus) {
        String sData = SerialsUtils.getHexBinFromLong(data, digits == DIGITS_16 ? 2 : bits, digits);
        if (!uartDataEdit.getText().equals(sData)) {
            onTextListener(sData, isFromEventBus);
        }
        if (uartDataCondition.getSelected().getIndex() != conditionIndex) {
            uartDataCondition.setSelectedIndex(conditionIndex);
            onCheckListener(uartDataCondition, uartDataCondition.getSelected(), isFromEventBus);
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
            case R.id.uartDataEdit:
                //该为16进制，两位限制//2进制，右侧决定位数
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 2 : bits, digits, onDataListener);
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
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber(), text);
        uartDataEdit.setEdit(text);
        msgUartData.setUartDataEdit(digits, text);
        sendMsg(msgUartData, isFromEventBus);
        Command.get().getTrigger_uart().setType(cmdCh, 2, uartDataCondition.getSelected().getIndex(), toD(text, digits), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
            uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA, toD(text, digits));
        }
    }

    private void onCheckListener(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == R.id.uartDataCondition) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex()));
            msgUartData.setUartDataCondition(item);
            sendMsg(msgUartData, isFromEventBus);
            Command.get().getTrigger_uart().setType(cmdCh, 2, item.getIndex(), toD(uartDataEdit.getText(), digits), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if (serialChannel == null) return;
                UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
                uartBus.setTriggerRelation(getConditionValue(item.getIndex()));
            }
        }
    }


}
