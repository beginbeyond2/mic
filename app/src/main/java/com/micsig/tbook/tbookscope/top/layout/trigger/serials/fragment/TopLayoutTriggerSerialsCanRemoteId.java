package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.CanBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRemoteId;
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

public class TopLayoutTriggerSerialsCanRemoteId extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit canRemoteIdEdit;
    private SerialsDetailCanRemoteId msgCanRemoteId;

    @Override
    protected void initView(View view) {
        canRemoteIdEdit = (TopViewEdit) view.findViewById(R.id.canRemoteIdEdit);
        canRemoteIdEdit.setOnClickEditListener(onClickEditListener);
        msgCanRemoteId = new SerialsDetailCanRemoteId();
        msgCanRemoteId.setCanRemoteIdEdit(DIGITS_16, canRemoteIdEdit.getText());
        msgCanRemoteId.setCanRemoteIdEditTitle(canRemoteIdEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_canremoteid;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String remoteId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + getSerialsNumber());
        msgCanRemoteId.setCanRemoteIdEdit(DIGITS_16, remoteId);
        return msgCanRemoteId;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_CAN) {
            if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 1) {
                //当且仅当当前can列表选中的是该项时，才向外发送消息
                sendMsg(msgCanRemoteId, rightMsgSerials.isFromEventBus());
            }
        }
    }

    @Override
    protected void setCache() {
        String remoteId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + getSerialsNumber());
        canRemoteIdEdit.setText(remoteId);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
        canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID, toD(remoteId, 16));

        msgCanRemoteId.setCanRemoteIdEdit(DIGITS_16, remoteId);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 1) {
            sendMsg(msgCanRemoteId, false);
        }
    }

    public void setCommandData(int id, boolean isFromEventBus) {
        String sId = SerialsUtils.getHexBinFromLong(id, 8, DIGITS_16);
        if (!canRemoteIdEdit.getText().equals(sId)) {
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
            case R.id.canRemoteIdEdit:
                //该为16进制,8位限制
                dialogKeyBoard.setDecimalData(8, DIGITS_16, onDismissListener);
                break;
        }
    }

    TopDialogNumberKeyBoard.OnDismissListener onDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String result, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        //Logger.i("onTextListener:1:" + result);
        int val = (int) toDLong(result, IDigits.DIGITS_16);
        result = SerialsUtils.getHexBinFromInt(val, 8, IDigits.DIGITS_16);
        //Logger.i("onTextListener:2:" + result);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + getSerialsNumber(), result);
        canRemoteIdEdit.setEdit(result);
        msgCanRemoteId.setCanRemoteIdEdit(DIGITS_16, result);
        sendMsg(msgCanRemoteId, isFromEventBus);
        Command.get().getTrigger_can().setType(cmdCh, 1, toD(result, IDigits.DIGITS_16), 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
            canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID, toD(result, IDigits.DIGITS_16));
        }
    }
}
