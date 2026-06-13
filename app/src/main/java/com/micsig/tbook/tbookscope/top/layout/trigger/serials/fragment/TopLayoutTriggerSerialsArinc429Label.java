package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.tbook.scope.Bus.ARINC429Bus;
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

public class TopLayoutTriggerSerialsArinc429Label extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit arinc429LabelLabel;
    private SerialsDetailArinc429Label msgArinc429Label;

    @Override
    protected void initView(View view) {
        arinc429LabelLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelLabel);
        arinc429LabelLabel.setOnClickEditListener(onClickEditListener);
        msgArinc429Label = new SerialsDetailArinc429Label();
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, arinc429LabelLabel.getText());
        msgArinc429Label.setArinc429LabelLabelTitle(arinc429LabelLabel.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429label;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber());
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, label);
        return msgArinc429Label;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 2) {
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429Label, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber());
        arinc429LabelLabel.setText(label);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, toD(label, IDigits.DIGITS_8));

        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, label);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 2) {
            sendMsg(msgArinc429Label, false);
        }
    }

    public void setCommandData(int label, boolean isFromEventBus) {
        String sLabel = SerialsUtils.getHexBinFromLong(label, 3, DIGITS_8);
        if (!arinc429LabelLabel.getText().equals(sLabel)) {
            onTextListener(sLabel, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429LabelLabel:
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber(), text);
        arinc429LabelLabel.setEdit(text);
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, text);
        sendMsg(msgArinc429Label, isFromEventBus);
        Command.get().getTrigger_m429().setType(cmdCh, 2, toD(text, 8), 0, 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
            a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, toD(text, IDigits.DIGITS_8));
        }
    }
}
