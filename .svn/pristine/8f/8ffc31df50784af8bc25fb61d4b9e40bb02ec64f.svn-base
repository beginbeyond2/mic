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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Ssm;
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

public class TopLayoutTriggerSerialsArinc429Ssm extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit arinc429SsmLabel;
    private SerialsDetailArinc429Ssm msgArinc429Ssm;

    @Override
    protected void initView(View view) {
        arinc429SsmLabel = (TopViewEdit) view.findViewById(R.id.arinc429SsmLabel);
        arinc429SsmLabel.setOnClickEditListener(onClickEditListener);
        msgArinc429Ssm = new SerialsDetailArinc429Ssm();
        msgArinc429Ssm.setArinc429SsmLabel(IDigits.DIGITS_2, arinc429SsmLabel.getText());
        msgArinc429Ssm.setArinc429SsmLabelTitle(arinc429SsmLabel.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429ssm;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SSM + getSerialsNumber());
        msgArinc429Ssm.setArinc429SsmLabel(IDigits.DIGITS_2, ssm);
        return msgArinc429Ssm;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 5) {
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429Ssm, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SSM + getSerialsNumber());
        arinc429SsmLabel.setText(ssm);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_SSM, toD(ssm, IDigits.DIGITS_2));

        msgArinc429Ssm.setArinc429SsmLabel(IDigits.DIGITS_2, ssm);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 5) {
            sendMsg(msgArinc429Ssm, false);
        }
    }

    public void setCommandData(int ssm, boolean isFromEventBus) {
        String sSsm = SerialsUtils.getHexBinFromLong(ssm, 2, DIGITS_2);
        if (!arinc429SsmLabel.getText().equals(sSsm)) {
            onTextListener(sSsm, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429SsmLabel:
                //该为2进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_2, onSsmListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onSsmListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SSM + getSerialsNumber(), text);
        arinc429SsmLabel.setEdit(text);
        msgArinc429Ssm.setArinc429SsmLabel(IDigits.DIGITS_2, text);
        sendMsg(msgArinc429Ssm, isFromEventBus);
        Command.get().getTrigger_m429().setType(cmdCh, 5, 0, 0, 0, toD(text, IDigits.DIGITS_2), false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
            a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_SSM, toD(text, IDigits.DIGITS_2));
        }
    }
}
