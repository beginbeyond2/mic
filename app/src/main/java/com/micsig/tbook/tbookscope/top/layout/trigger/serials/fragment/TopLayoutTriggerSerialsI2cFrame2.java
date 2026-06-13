package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.I2CBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsI2cFrame2 extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailI2cFrame2 msgI2cFrame2;
    private TopViewEdit i2cFrame2Addr;
    private TopViewEdit i2cFrame2Data1;
    private TopViewEdit i2cFrame2Data2;

    @Override
    protected void initView(View view) {
        i2cFrame2Addr = (TopViewEdit) view.findViewById(R.id.i2cFrame2Addr);
        i2cFrame2Data1 = (TopViewEdit) view.findViewById(R.id.i2cFrame2Data1);
        i2cFrame2Data2 = (TopViewEdit) view.findViewById(R.id.i2cFrame2Data2);
        i2cFrame2Addr.setOnClickEditListener(onClickEditListener);
        i2cFrame2Data1.setOnClickEditListener(onClickEditListener);
        i2cFrame2Data2.setOnClickEditListener(onClickEditListener);
        msgI2cFrame2 = new SerialsDetailI2cFrame2();
        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, i2cFrame2Addr.getText());
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, i2cFrame2Data1.getText());
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, i2cFrame2Data2.getText());
        msgI2cFrame2.setI2cFrame2AddrTitle(i2cFrame2Addr.getHead());
        msgI2cFrame2.setI2cFrame2Data1Title(i2cFrame2Data1.getHead());
        msgI2cFrame2.setI2cFrame2Data2Title(i2cFrame2Data2.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_i2cframe2;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber());
        String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber());
        String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber());
        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, addr);
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, data1);
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, data2);
        return msgI2cFrame2;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 6) {
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cFrame2, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber());
        String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber());
        String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber());
        i2cFrame2Addr.setText(addr);
        i2cFrame2Data1.setText(data1);
        i2cFrame2Data2.setText(data2);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
        i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, toD(addr, IDigits.DIGITS_16));
        i2cBus.setTriggerData1(toD(data1, IDigits.DIGITS_16));
        i2cBus.setTriggerData2(toD(data2, IDigits.DIGITS_16));

        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, addr);
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, data1);
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, data2);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 6) {
            sendMsg(msgI2cFrame2, false);
        }
    }

    public void setCommandData(int addr, int data1, int data2, boolean isFromEventBus) {
        String sAddr = SerialsUtils.getHexBinFromLong(addr, 2, DIGITS_16);
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16);
        String sData2 = SerialsUtils.getHexBinFromLong(data2, 2, DIGITS_16);
        if (!i2cFrame2Addr.getText().equals(sAddr)) {
            onTextListener(i2cFrame2Addr, sAddr, isFromEventBus);
        }
        if (!i2cFrame2Data1.getText().equals(sData1)) {
            onTextListener(i2cFrame2Data1, sData1, isFromEventBus);
        }
        if (!i2cFrame2Data2.getText().equals(sData2)) {
            onTextListener(i2cFrame2Data2, sData2, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.i2cFrame2Addr:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onAddrListener);
                break;
            case R.id.i2cFrame2Data1:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onData1Listener);
                break;
            case R.id.i2cFrame2Data2:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onData2Listener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onAddrListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2cFrame2Addr, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onData1Listener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2cFrame2Data1, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onData2Listener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2cFrame2Data2, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String result, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == i2cFrame2Addr.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber(), result);
            i2cFrame2Addr.setEdit(result);
            msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, result);
            sendMsg(msgI2cFrame2, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, toD(result, IDigits.DIGITS_16));
            }
        } else if (view.getId() == i2cFrame2Data1.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber(), result);
            i2cFrame2Data1.setEdit(result);
            msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, result);
            sendMsg(msgI2cFrame2, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME2, toD(result, IDigits.DIGITS_16));
            }
        } else if (view.getId() == i2cFrame2Data2.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber(), result);
            i2cFrame2Data2.setEdit(result);
            msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, result);
            sendMsg(msgI2cFrame2, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerData2(toD(result, IDigits.DIGITS_16));
            }
        }
    }
}
