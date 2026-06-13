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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bCsWord;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsM1553bCsWord extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailM1553bCsWord msgM1553bCsWord;
    private TopViewEdit m1553bCsWordCsWord;

    @Override
    protected void initView(View view) {
        m1553bCsWordCsWord = (TopViewEdit) view.findViewById(R.id.m1553bCsWordCsWord);
        m1553bCsWordCsWord.setOnClickEditListener(onClickEditListener);
        msgM1553bCsWord = new SerialsDetailM1553bCsWord();
        msgM1553bCsWord.setM1553bCsWordCsWord(digits, m1553bCsWordCsWord.getHead());
        msgM1553bCsWord.setM1553bCsWordCsWordTitle(m1553bCsWordCsWord.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_m1553bcsword;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String csWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber());
        int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        msgM1553bCsWord.setM1553bCsWordCsWord(digits, csWord);
        return msgM1553bCsWord;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (serialsNumber == rightMsgSerials.getSerialsNumber()
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M1553B) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
            if (m1553bCsWordCsWord != null) {
                String csWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber());
                m1553bCsWordCsWord.setText(csWord);
                msgM1553bCsWord.setM1553bCsWordCsWord(digits, m1553bCsWordCsWord.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 2) {
                    //当且仅当当前1554b列表选中的是该项时，才向外发送消息
                    sendMsg(msgM1553bCsWord, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String csWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        m1553bCsWordCsWord.setText(csWord);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
        m1553bBus.setCmdStatus(toD(csWord, digits));

        msgM1553bCsWord.setM1553bCsWordCsWord(digits, csWord);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M1553B
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 2) {
            sendMsg(msgM1553bCsWord, false);
        }
    }

    public void setCommandData(int csWord, boolean isFromEventBus) {
        String sCsWord = SerialsUtils.getHexBinFromLong(csWord, digits == DIGITS_16 ? 4 : 16, digits);
        if (!m1553bCsWordCsWord.getText().equals(sCsWord)) {
            onTextListener(sCsWord, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.m1553bCsWordCsWord:
                //该为2进制,16位限制//16进制，4位
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 4 : 16, digits, onCsWordListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onCsWordListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber(), text);
        m1553bCsWordCsWord.setEdit(text);
        msgM1553bCsWord.setM1553bCsWordCsWord(digits, text);
        sendMsg(msgM1553bCsWord, isFromEventBus);
        Command.get().getTrigger_m1553B().setType(cmdCh, 2, toD(text, digits), 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
            m1553bBus.setCmdStatus(toD(text, digits));
        }
    }
}
