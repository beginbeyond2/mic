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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cNoAckInAdr;
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

public class TopLayoutTriggerSerialsI2cNoAckInAdr extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit i2cNoAckInAdrData;
    private SerialsDetailI2cNoAckInAdr msgI2cNoAckInAdr;

    @Override
    protected void initView(View view) {
        i2cNoAckInAdrData = (TopViewEdit) view.findViewById(R.id.i2cNoAckInAdrData);
        i2cNoAckInAdrData.setOnClickEditListener(onClickEditListener);
        msgI2cNoAckInAdr = new SerialsDetailI2cNoAckInAdr();
        msgI2cNoAckInAdr.setI2cNoAckInAdrData(DIGITS_16, i2cNoAckInAdrData.getText());
        msgI2cNoAckInAdr.setI2cNoAckInAdrDataTitle(i2cNoAckInAdrData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_i2cnoackinadr;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String noAckInAdr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + getSerialsNumber());
        msgI2cNoAckInAdr.setI2cNoAckInAdrData(DIGITS_16, noAckInAdr);
        return msgI2cNoAckInAdr;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 4) {
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cNoAckInAdr, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String noAckInAdr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + getSerialsNumber());
        i2cNoAckInAdrData.setText(noAckInAdr);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
        i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK, toD(noAckInAdr, IDigits.DIGITS_16));

        msgI2cNoAckInAdr.setI2cNoAckInAdrData(DIGITS_16, noAckInAdr);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 4) {
            sendMsg(msgI2cNoAckInAdr, false);
        }
    }

    public void setCommandData(int addr, boolean isFromEventBus) {
        String sAddr = SerialsUtils.getHexBinFromLong(addr, 2, DIGITS_16);
        if (!i2cNoAckInAdrData.getText().equals(sAddr)) {
            onTextListener(sAddr, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.i2cNoAckInAdrData:
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onDataListener);
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
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + getSerialsNumber(), text);
        i2cNoAckInAdrData.setEdit(text);
        msgI2cNoAckInAdr.setI2cNoAckInAdrData(DIGITS_16, text);
        sendMsg(msgI2cNoAckInAdr, isFromEventBus);
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        Command.get().getTrigger_iic().setType(cmdCh, 4, toD(text, IDigits.DIGITS_16), 0, 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
            i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK, toD(text, IDigits.DIGITS_16));
        }
    }
}
