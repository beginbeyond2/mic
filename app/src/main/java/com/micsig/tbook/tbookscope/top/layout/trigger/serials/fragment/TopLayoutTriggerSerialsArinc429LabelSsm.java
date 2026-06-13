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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSsm;
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

public class TopLayoutTriggerSerialsArinc429LabelSsm extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit arinc429LabelSsmLabel;
    private TopViewEdit arinc429LabelSsmSsm;
    private SerialsDetailArinc429LabelSsm msgArinc429LabelSsm;

    @Override
    protected void initView(View view) {
        arinc429LabelSsmLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelSsmLabel);
        arinc429LabelSsmSsm = (TopViewEdit) view.findViewById(R.id.arinc429LabelSsmSsm);
        arinc429LabelSsmLabel.setOnClickEditListener(onClickEditListener);
        arinc429LabelSsmSsm.setOnClickEditListener(onClickEditListener);
        msgArinc429LabelSsm = new SerialsDetailArinc429LabelSsm();
        msgArinc429LabelSsm.setArinc429LabelSsmLabel(IDigits.DIGITS_8, arinc429LabelSsmLabel.getText());
        msgArinc429LabelSsm.setArinc429LabelSsmSsm(IDigits.DIGITS_2, arinc429LabelSsmSsm.getText());
        msgArinc429LabelSsm.setArinc429LabelSsmLabelTitle(arinc429LabelSsmLabel.getHead());
        msgArinc429LabelSsm.setArinc429LabelSsmSsmTitle(arinc429LabelSsmSsm.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429labelssm;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + getSerialsNumber());
        String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + getSerialsNumber());
        msgArinc429LabelSsm.setArinc429LabelSsmLabel(IDigits.DIGITS_8, label);
        msgArinc429LabelSsm.setArinc429LabelSsmSsm(IDigits.DIGITS_2, ssm);
        return msgArinc429LabelSsm;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 8) {
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429LabelSsm, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + getSerialsNumber());
        String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + getSerialsNumber());
        arinc429LabelSsmLabel.setText(label);
        arinc429LabelSsmSsm.setText(ssm);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, toD(ssm, IDigits.DIGITS_2));
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, toD(label, IDigits.DIGITS_8));

        msgArinc429LabelSsm.setArinc429LabelSsmLabel(IDigits.DIGITS_8, label);
        msgArinc429LabelSsm.setArinc429LabelSsmSsm(IDigits.DIGITS_2, ssm);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 8) {
            sendMsg(msgArinc429LabelSsm, false);
        }
    }

    public void setCommandData(int label, int ssm, boolean isFromEventBus) {
        String sLabel = SerialsUtils.getHexBinFromLong(label, 3, DIGITS_8);
        String sSsm = SerialsUtils.getHexBinFromLong(ssm, 2, DIGITS_2);
        if (!arinc429LabelSsmLabel.getText().equals(sLabel)) {
            onTextListener(arinc429LabelSsmLabel, sLabel, isFromEventBus);
        }
        if (!arinc429LabelSsmSsm.getText().equals(sSsm)) {
            onTextListener(arinc429LabelSsmSsm, sSsm, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429LabelSsmLabel:
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener);
                break;
            case R.id.arinc429LabelSsmSsm:
                //该为2进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_2, onSsmListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelSsmLabel, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onSsmListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelSsmSsm, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == arinc429LabelSsmLabel.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + getSerialsNumber(), text);
            arinc429LabelSsmLabel.setEdit(text);
            msgArinc429LabelSsm.setArinc429LabelSsmLabel(IDigits.DIGITS_8, text);
            sendMsg(msgArinc429LabelSsm, isFromEventBus);
            Command.get().getTrigger_m429().setType(cmdCh, 8, toD(text, IDigits.DIGITS_8), 0, 0, toD(arinc429LabelSsmSsm.getText(), IDigits.DIGITS_2), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, toD(text, IDigits.DIGITS_8));
            }
        } else if (view.getId() == arinc429LabelSsmSsm.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + getSerialsNumber(), text);
            arinc429LabelSsmSsm.setEdit(text);
            msgArinc429LabelSsm.setArinc429LabelSsmSsm(IDigits.DIGITS_2, text);
            sendMsg(msgArinc429LabelSsm, isFromEventBus);
            Command.get().getTrigger_m429().setType(cmdCh, 8, toD(text, IDigits.DIGITS_8), 0, 0, toD(arinc429LabelSsmSsm.getText(), IDigits.DIGITS_2), false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, toD(text, IDigits.DIGITS_2));
            }
        }
    }
}
