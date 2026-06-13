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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bRtAddr;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsM1553bRtAddr extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailM1553bRtAddr msgM1553bRtAddr;
    private TopViewEdit m1553bRtAddrRtAddr;

    @Override
    protected void initView(View view) {
        m1553bRtAddrRtAddr = (TopViewEdit) view.findViewById(R.id.m1553bRtAddrRtAddr);
        m1553bRtAddrRtAddr.setOnClickEditListener(onClickEditListener);
        msgM1553bRtAddr = new SerialsDetailM1553bRtAddr();
        msgM1553bRtAddr.setM1553bRtAddrRtAddr(digits, m1553bRtAddrRtAddr.getText());
        msgM1553bRtAddr.setM1553bRtAddrRtAddrTitle(m1553bRtAddrRtAddr.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_m1553brtaddr;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String rtAddr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber());
        int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        msgM1553bRtAddr.setM1553bRtAddrRtAddr(digits, rtAddr);
        return msgM1553bRtAddr;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (serialsNumber == rightMsgSerials.getSerialsNumber()
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M1553B) {
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
            if (m1553bRtAddrRtAddr != null) {
                String rtAddr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber());
                m1553bRtAddrRtAddr.setText(rtAddr);
                msgM1553bRtAddr.setM1553bRtAddrRtAddr(digits, m1553bRtAddrRtAddr.getText());
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 3) {
                    //当且仅当当前1554b列表选中的是该项时，才向外发送消息
                    sendMsg(msgM1553bRtAddr, rightMsgSerials.isFromEventBus());
                }
            }
        }
    }

    @Override
    protected void setCache() {
        String rtAddr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber());
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16;
        m1553bRtAddrRtAddr.setText(rtAddr);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if (serialChannel == null) return;
        MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
        m1553bBus.setAddr(toD(rtAddr, digits));

        msgM1553bRtAddr.setM1553bRtAddrRtAddr(digits, rtAddr);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M1553B
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 3) {
            sendMsg(msgM1553bRtAddr, false);
        }
    }

    public void setCommandData(int rtAddr, boolean isFromEventBus) {
        String sRtAddr = SerialsUtils.getHexBinFromLong(rtAddr, digits == DIGITS_16 ? 2 : 5, digits);
        if (!m1553bRtAddrRtAddr.getText().equals(sRtAddr)) {
            onTextListener(sRtAddr, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.m1553bRtAddrRtAddr:
                //该为2进制,5位限制//16进制，2位，最大1F
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 2 : 5, digits, onRtAddrListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onRtAddrListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber(), text);
        m1553bRtAddrRtAddr.setEdit(text);
        msgM1553bRtAddr.setM1553bRtAddrRtAddr(digits, text);
        sendMsg(msgM1553bRtAddr, isFromEventBus);
        Command.get().getTrigger_m1553B().setType(cmdCh, 3, 0, toD(text, digits), 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if (serialChannel == null) return;
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
            m1553bBus.setAddr(toD(text, digits));
        }
    }
}
