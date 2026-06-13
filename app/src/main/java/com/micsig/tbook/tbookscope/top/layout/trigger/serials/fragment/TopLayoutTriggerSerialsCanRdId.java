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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId;
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

public class TopLayoutTriggerSerialsCanRdId extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit canRdIdEdit;
    private SerialsDetailCanRdId msgCanRdId;

    @Override
    protected void initView(View view) {
        canRdIdEdit = (TopViewEdit) view.findViewById(R.id.canRdIdEdit);
        canRdIdEdit.setOnClickEditListener(onClickEditListener);
        msgCanRdId = new SerialsDetailCanRdId();
        msgCanRdId.setCanRdIdEdit(DIGITS_16, canRdIdEdit.getText());
        msgCanRdId.setCanRdIdEditTitle(canRdIdEdit.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_canrdid;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber());
        msgCanRdId.setCanRdIdEdit(DIGITS_16, rdId);
        return msgCanRdId;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 3) {
            //当且仅当当前can列表选中的是该项时，才向外发送消息
            sendMsg(msgCanRdId, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber());
        canRdIdEdit.setText(rdId);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
        canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, toD(rdId, 16));

        msgCanRdId.setCanRdIdEdit(DIGITS_16, rdId);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 3) {
            sendMsg(msgCanRdId, false);
        }
    }

    public void setCommandData(int id, boolean isFromEventBus) {
        String sId = SerialsUtils.getHexBinFromLong(id, 8, DIGITS_16);
        if (!canRdIdEdit.getText().equals(sId)) {
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
            case R.id.canRdIdEdit:
                //该为16进制,8位限制
                dialogKeyBoard.setDecimalData(8, DIGITS_16, onIdListener);
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
        int val = (int) toDLong(text, IDigits.DIGITS_16);
        text = SerialsUtils.getHexBinFromInt(val, 8, IDigits.DIGITS_16);

        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber(), text);
        canRdIdEdit.setEdit(text);
        msgCanRdId.setCanRdIdEdit(DIGITS_16, text);
        sendMsg(msgCanRdId, isFromEventBus);
        Command.get().getTrigger_can().setType(cmdCh, 3, toD(text, IDigits.DIGITS_16), 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
            canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, toD(text, IDigits.DIGITS_16));
        }
    }
}
