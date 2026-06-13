package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.SpiBus;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsSpiData extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailSpiData msgSpiData;
    private TopViewEdit spiDataData;

    @Override
    protected void initView(View view) {
        spiDataData = (TopViewEdit) view.findViewById(R.id.spiDataData);
        spiDataData.setOnClickEditListener(onClickEditListener);
        msgSpiData = new SerialsDetailSpiData();
        msgSpiData.setSpiDataData(DIGITS_2X, spiDataData.getText());
        msgSpiData.setSpiDataDataTitle(spiDataData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_spidata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber());
        msgSpiData.setSpiDataData(DIGITS_2X, data);
        return msgSpiData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        int preDigits = 0;
        if (serialsNumber == rightMsgSerials.getSerialsNumber()
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_SPI) {
            RightMsgSerialsSpi detailsSpi = (RightMsgSerialsSpi) rightMsgSerials.getSerialsDetails();
            bits = detailsSpi.getIntBit();
            if (spiDataData != null) {
                String s = spiDataData.getText().replace(" ", "");
                String d = KeyBoardNumberUtil.toBits(s, "X", bits);
                String result = KeyBoardNumberUtil.reCalculateSpace(d, DIGITS_2);
                spiDataData.setText(result);
                int cmdCh= TChan.toFpgaChNo(serialsNumber);
                Command.get().getTrigger_spi().Data(cmdCh, result,false);

                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber(), result);
                msgSpiData.setSpiDataData(DIGITS_2X, spiDataData.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + getSerialsNumber()) == 1) {
                    //当且仅当当前spi列表选中的是该项时，才向外发送消�?
                    sendMsg(msgSpiData, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber());
        spiDataData.setText(data);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI);
        spiBus.setTriggerMask((int) toDLong(getMask(data), IDigits.DIGITS_2));
        spiBus.setTriggerData((int) toDLong(getData(data), IDigits.DIGITS_2));

        int type=Command.get().getTrigger_spi().TypeQ(getSerialsNumber()-1);
        if (type==1){
            Command.get().getTrigger_spi().Data(getSerialsNumber()-1,data,false);
        }

        msgSpiData.setSpiDataData(DIGITS_2X, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_SPI
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + getSerialsNumber()) == 1) {
            sendMsg(msgSpiData, false);
        }
    }

    private String getMask(String data) {
        return SerialsUtils.getSpiMask(data);
    }

    private String getData(String data) {
        return SerialsUtils.getSpiData(data);
    }

    public void setCommandData(String data, boolean isFromEventBus) {
        String s = data.replace("0", "").replace("1", "")
                .replace("X", "").replace("x", "");
        if (!StrUtil.isEmpty(s)) return;
        data = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits(
                data.replace(" ", ""), "X", bits), DIGITS_2X).trim();
        if (!spiDataData.getText().equals(data)) {
            onDataListener(data, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.spiDataData:
                //只能输入0�?1、X，右侧决定位数，可能�?要直接操作edit而不是操作键盘上的text
                dialogKeyBoard.setDecimalData(msgSpiData.getSpiDataData().getValue(), bits, DIGITS_2X, onDataListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onDataListener(result, false);
        }
    };

    private void onDataListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber(), text);
        spiDataData.setEdit(text);
        msgSpiData.setSpiDataData(DIGITS_2X, text);
        sendMsg(msgSpiData, isFromEventBus);
        long mask = toDLong(getMask(text), IDigits.DIGITS_2);
        long data = toDLong(getData(text), IDigits.DIGITS_2);
        Command.get().getBus_spi().setType(cmdCh, 1, (int) mask, (int) data, false);
        Command.get().getTrigger_spi().Type(cmdCh, 1,false);
        Command.get().getTrigger_spi().Data(cmdCh, text,false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI);
            spiBus.setTriggerMask((int) toDLong(getMask(text), IDigits.DIGITS_2));
            spiBus.setTriggerData((int) toDLong(getData(text), IDigits.DIGITS_2));
        }
    }
}