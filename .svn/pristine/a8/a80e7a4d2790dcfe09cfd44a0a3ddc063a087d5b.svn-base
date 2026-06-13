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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame1;
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

public class TopLayoutTriggerSerialsI2cFrame1 extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit i2cFrame1Addr;
    private TopViewEdit i2cFrame1Data;
    private SerialsDetailI2cFrame1 msgI2cFrame1;

    @Override
    protected void initView(View view) {
        i2cFrame1Addr = (TopViewEdit) view.findViewById(R.id.i2cFrame1Addr);
        i2cFrame1Data = (TopViewEdit) view.findViewById(R.id.i2cFrame1Data);
        i2cFrame1Addr.setOnClickEditListener(onClickEditListener);
        i2cFrame1Data.setOnClickEditListener(onClickEditListener);
        msgI2cFrame1 = new SerialsDetailI2cFrame1();
        msgI2cFrame1.setI2cFrame1Addr(DIGITS_16, i2cFrame1Addr.getText());
        msgI2cFrame1.setI2cFrame1Data(DIGITS_16, i2cFrame1Data.getText());
        msgI2cFrame1.setI2cFrame1AddrTitle(i2cFrame1Addr.getHead());
        msgI2cFrame1.setI2cFrame1DataTitle(i2cFrame1Data.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_i2cframe1;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + getSerialsNumber());
        msgI2cFrame1.setI2cFrame1Addr(DIGITS_16, addr);
        msgI2cFrame1.setI2cFrame1Data(DIGITS_16, data);
        return msgI2cFrame1;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 5) {
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cFrame1, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + getSerialsNumber());
        i2cFrame1Addr.setText(addr);
        i2cFrame1Data.setText(data);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
        i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME1, toD(addr, DIGITS_16));
        i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME1, toD(data, DIGITS_16));

        msgI2cFrame1.setI2cFrame1Addr(DIGITS_16, addr);
        msgI2cFrame1.setI2cFrame1Data(DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 5) {
            sendMsg(msgI2cFrame1, false);
        }
    }

    public void setCommandData(int addr, int data1, boolean isFromEventBus) {
        String sAddr = SerialsUtils.getHexBinFromLong(addr, 2, DIGITS_16);
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16);
        if (!i2cFrame1Addr.getText().equals(sAddr)) {
            onTextListener(i2cFrame1Addr, sAddr, isFromEventBus);
        }
        if (!i2cFrame1Data.getText().equals(sData1)) {
            onTextListener(i2cFrame1Data, sData1, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.i2cFrame1Addr:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onAddrListener);
                break;
            case R.id.i2cFrame1Data:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onAddrListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2cFrame1Addr, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2cFrame1Data, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String result, boolean isFromEventBus) {
        int cmdCh= TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == i2cFrame1Addr.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + getSerialsNumber(), result);
            i2cFrame1Addr.setEdit(result);
            msgI2cFrame1.setI2cFrame1Addr(DIGITS_16, result);
            sendMsg(msgI2cFrame1, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 5, toD(i2cFrame1Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame1Data.getText(), IDigits.DIGITS_16), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME1, toD(result, IDigits.DIGITS_16));
            }
        } else if (view.getId() == i2cFrame1Data.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + getSerialsNumber(), result);
            i2cFrame1Data.setEdit(result);
            msgI2cFrame1.setI2cFrame1Data(DIGITS_16, result);
            sendMsg(msgI2cFrame1, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 5, toD(i2cFrame1Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame1Data.getText(), IDigits.DIGITS_16), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME1, toD(result, IDigits.DIGITS_16));
            }
        }
    }
}
