package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsLinFrameId extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit linFrameIdEditEdit;
    private SerialsDetailLinFrameId msgLinFrameId;

    @Override
    protected void initView(View view) {
        linFrameIdEditEdit = (TopViewEdit) view.findViewById(R.id.linFrameIdEditEdit);
        linFrameIdEditEdit.setOnClickEditListener(onClickEditListener);
        msgLinFrameId = new SerialsDetailLinFrameId();
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEditEdit.getText());
        msgLinFrameId.setLinFrameIdEditEditTitle(linFrameIdEditEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_linframeid;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber());
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEdit);
        return msgLinFrameId;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 1) {
            //当且仅当当前lin列表选中的是该项时，才向外发送消息
            sendMsg(msgLinFrameId, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber());
        linFrameIdEditEdit.setText(linFrameIdEdit);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
        linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, toD(linFrameIdEdit, 16));

        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEdit);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 1) {
            sendMsg(msgLinFrameId, false);
        }
    }

    public void setCommandData(int id, boolean isFromEventBus) {
        String sId = SerialsUtils.getHexBinFromLong(id, 2, DIGITS_16);
        if (!linFrameIdEditEdit.getText().equals(sId)) {
            onTextListener(sId, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.linFrameIdEditEdit:
                //该为16进制,两位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onIdListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onIdListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        text = (text.charAt(0) - '0') > 3 ? "3F" : text;//此结果最大值为3F
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber(), text);
        linFrameIdEditEdit.setEdit(text);
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, text);
        sendMsg(msgLinFrameId, isFromEventBus);
        Command.get().getTrigger_lin().setType(cmdCh, 1, toD(text, 16), 0, false);
        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
            linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, toD(text, 16));
        }
    }
}
