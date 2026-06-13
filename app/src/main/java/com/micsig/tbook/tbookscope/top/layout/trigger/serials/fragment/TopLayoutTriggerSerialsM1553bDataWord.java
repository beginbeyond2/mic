package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsM1553bDataWord extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailM1553bDataWord msgM1553bDataWord;
    private TopViewEdit m1553bDataWordData;

    @Override
    protected void initView(View view) {
        m1553bDataWordData = (TopViewEdit) view.findViewById(R.id.m1553bDataWordData);
        m1553bDataWordData.setOnClickEditListener(onClickEditListener);
        msgM1553bDataWord = new SerialsDetailM1553bDataWord();
        msgM1553bDataWord.setM1553bDataWordData(digits, m1553bDataWordData.getText());
        msgM1553bDataWord.setM1553bDataWordDataTitle(m1553bDataWordData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_m1553bdataword;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber());
        int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        msgM1553bDataWord.setM1553bDataWordData(digits, dataWord);
        return msgM1553bDataWord;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (serialsNumber == rightMsgSerials.getSerialsNumber()
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M1553B) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
            if (m1553bDataWordData != null) {
                String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber());
                m1553bDataWordData.setText(dataWord);
                msgM1553bDataWord.setM1553bDataWordData(digits, m1553bDataWordData.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 5) {
                    //当且仅当当前1554b列表选中的是该项时，才向外发送消息
                    sendMsg(msgM1553bDataWord, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        m1553bDataWordData.setText(dataWord);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
        m1553bBus.setData(toD(dataWord, digits));

        msgM1553bDataWord.setM1553bDataWordData(digits, dataWord);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M1553B
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 5) {
            sendMsg(msgM1553bDataWord, false);
        }
    }

    public void setCommandData(int dataWord, boolean isFromEventBus) {
        String sDataWord = SerialsUtils.getHexBinFromLong(dataWord, digits == DIGITS_16 ? 4 : 16, digits);
        if (!m1553bDataWordData.getText().equals(sDataWord)) {
            onTextListener(sDataWord, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.m1553bDataWordData:
                //该为2进制,16位限制//16进制，4位
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 4 : 16, digits, onDataListener);
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
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber(), text);
        m1553bDataWordData.setEdit(text);
        msgM1553bDataWord.setM1553bDataWordData(digits, text);
        sendMsg(msgM1553bDataWord, isFromEventBus);
        Command.get().getTrigger_m1553B().setType(cmdCh, 5, 0, 0, toD(text, digits), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
            m1553bBus.setData(toD(text, digits));
        }
    }
}
