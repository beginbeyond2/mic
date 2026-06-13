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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData;
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

public class TopLayoutTriggerSerialsI2cRomData extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewRadioGroup i2cRomDataCondition;
    private TopViewEdit i2cRomDataData;
    private SerialsDetailI2cRomData msgI2cRomData;

    @Override
    protected void initView(View view) {
        i2cRomDataCondition = (TopViewRadioGroup) view.findViewById(R.id.i2cRomDataCondition);
        i2cRomDataData = (TopViewEdit) view.findViewById(R.id.i2cRomDataData);
        i2cRomDataCondition.setOnListener(onCheckChangedListener);
        i2cRomDataData.setOnClickEditListener(onClickEditListener);
        msgI2cRomData = new SerialsDetailI2cRomData();
        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected());
        msgI2cRomData.setI2cRomDataData(DIGITS_16, i2cRomDataData.getText());
        msgI2cRomData.setI2cRomDataConditionTitle(i2cRomDataCondition.getHead());
        msgI2cRomData.setI2cRomDataDataTitle(i2cRomDataData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_i2cromdata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber());
        i2cRomDataCondition.setSelectedIndex(condition);
        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected());
        msgI2cRomData.setI2cRomDataData(DIGITS_16, data);
        return msgI2cRomData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 7) {
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cRomData, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber());
        i2cRomDataCondition.setSelectedIndex(condition);
        i2cRomDataData.setText(data);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
        i2cBus.setTriggerRelation(getConditionValue(condition));
        i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, toD(data, IDigits.DIGITS_16));

        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected());
        msgI2cRomData.setI2cRomDataData(DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 7) {
            sendMsg(msgI2cRomData, false);
        }
    }

    public void setCommandData(int data1, int condition, boolean isFromEventBus) {
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16);
        if (!i2cRomDataData.getText().equals(sData1)) {
            onTextListener(sData1, isFromEventBus);
        }
        if (i2cRomDataCondition.getSelected().getIndex() != condition) {
            i2cRomDataCondition.setSelectedIndex(condition);
            onCheckListener(i2cRomDataCondition, i2cRomDataCondition.getSelected(), isFromEventBus);
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
            case R.id.i2cRomDataData:
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

    private void onTextListener(String result, boolean isFromEventBus) {
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber(), result);
        i2cRomDataData.setEdit(result);
        msgI2cRomData.setI2cRomDataData(DIGITS_16, result);
        sendMsg(msgI2cRomData, isFromEventBus);
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        Command.get().getTrigger_iic().setType(cmdCh, 7, 0, toD(result, IDigits.DIGITS_16), 0, i2cRomDataCondition.getSelected().getIndex(), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
            i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, toD(result, IDigits.DIGITS_16));
        }
    }

    private void onCheckListener(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == R.id.i2cRomDataCondition) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex()));
            msgI2cRomData.setI2cRomDataCondition(item);
            sendMsg(msgI2cRomData, isFromEventBus);
            int cmdCh=TChan.toFpgaChNo(serialsNumber);
            int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
            Command.get().getTrigger_iic().setType(cmdCh, 7, 0, Integer.parseInt(i2cRomDataData.getText(),16), 0, i2cRomDataCondition.getSelected().getIndex(), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                i2cBus.setTriggerRelation(getConditionValue(item.getIndex()));
            }
        }
    }
}
