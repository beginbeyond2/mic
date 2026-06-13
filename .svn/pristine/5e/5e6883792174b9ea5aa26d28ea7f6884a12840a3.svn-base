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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2c10WriteFrame;
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

public class TopLayoutTriggerSerialsI2c10WriteFrame extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit i2c10WriteFrameAddr;
    private TopViewEdit i2c10WriteFrameData;
    private SerialsDetailI2c10WriteFrame msgI2c10WriteFrame;

    @Override
    protected void initView(View view) {
        i2c10WriteFrameAddr = (TopViewEdit) view.findViewById(R.id.i2c10WriteFrameAddr);
        i2c10WriteFrameData = (TopViewEdit) view.findViewById(R.id.i2c10WriteFrameData);
        i2c10WriteFrameAddr.setOnClickEditListener(onClickEditListener);
        i2c10WriteFrameData.setOnClickEditListener(onClickEditListener);
        msgI2c10WriteFrame = new SerialsDetailI2c10WriteFrame();
        msgI2c10WriteFrame.setI2c10WriteFrameAddr(DIGITS_16, i2c10WriteFrameAddr.getText());
        msgI2c10WriteFrame.setI2c10WriteFrameData(DIGITS_16, i2c10WriteFrameData.getText());
        msgI2c10WriteFrame.setI2c10WriteFrameAddrTitle(i2c10WriteFrameAddr.getHead());
        msgI2c10WriteFrame.setI2c10WriteFrameDataTitle(i2c10WriteFrameData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_i2c10writeframe;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + getSerialsNumber());
        msgI2c10WriteFrame.setI2c10WriteFrameAddr(DIGITS_16, addr);
        msgI2c10WriteFrame.setI2c10WriteFrameData(DIGITS_16, data);
        return msgI2c10WriteFrame;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 8) {
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2c10WriteFrame, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + getSerialsNumber());
        i2c10WriteFrameAddr.setText(addr);
        i2c10WriteFrameData.setText(data);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
        i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_WRITE_FRAME, toD(addr, DIGITS_16));
        i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME, toD(data, DIGITS_16));

        msgI2c10WriteFrame.setI2c10WriteFrameAddr(DIGITS_16, addr);
        msgI2c10WriteFrame.setI2c10WriteFrameData(DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 8) {
            sendMsg(msgI2c10WriteFrame, false);
        }
    }

    public void setCommandData(int addr, int data1, boolean isFromEventBus) {
        String sAddr = SerialsUtils.getHexBinFromLong(addr, 3, DIGITS_16);
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16);
        if (!i2c10WriteFrameAddr.getText().equals(sAddr)) {
            onTextListener(i2c10WriteFrameAddr, sAddr, isFromEventBus);
        }
        if (!i2c10WriteFrameData.getText().equals(sData1)) {
            onTextListener(i2c10WriteFrameData, sData1, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.i2c10WriteFrameAddr:
                //该为16进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_16, onAddrListener);
                break;
            case R.id.i2c10WriteFrameData:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onAddrListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2c10WriteFrameAddr, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(i2c10WriteFrameData, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == i2c10WriteFrameAddr.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + getSerialsNumber(), text);
            i2c10WriteFrameAddr.setEdit(text);
            msgI2c10WriteFrame.setI2c10WriteFrameAddr(DIGITS_16, text);
            sendMsg(msgI2c10WriteFrame, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 8, toD(i2c10WriteFrameAddr.getText(), IDigits.DIGITS_16), toD(i2c10WriteFrameData.getText(), IDigits.DIGITS_16), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_WRITE_FRAME, toD(text, IDigits.DIGITS_16));
            }
        } else if (view.getId() == i2c10WriteFrameData.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + getSerialsNumber(), text);
            i2c10WriteFrameData.setEdit(text);
            msgI2c10WriteFrame.setI2c10WriteFrameData(DIGITS_16, text);
            sendMsg(msgI2c10WriteFrame, isFromEventBus);
            Command.get().getTrigger_iic().setType(cmdCh, 8, toD(i2c10WriteFrameAddr.getText(), IDigits.DIGITS_16), toD(i2c10WriteFrameData.getText(), IDigits.DIGITS_16), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME, toD(text, IDigits.DIGITS_16));
            }
        }
    }
}
