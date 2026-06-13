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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Sdi;
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

public class TopLayoutTriggerSerialsArinc429Sdi extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailArinc429Sdi msgArinc429Sdi;
    private TopViewEdit arinc429SdiLabel;

    @Override
    protected void initView(View view) {
        arinc429SdiLabel = (TopViewEdit) view.findViewById(R.id.arinc429SdiLabel);
        arinc429SdiLabel.setOnClickEditListener(onClickEditListener);
        msgArinc429Sdi = new SerialsDetailArinc429Sdi();
        msgArinc429Sdi.setArinc429SdiLabel(IDigits.DIGITS_2, arinc429SdiLabel.getText());
        msgArinc429Sdi.setArinc429SdiLabelTitle(arinc429SdiLabel.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429sdi;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SDI + getSerialsNumber());
        msgArinc429Sdi.setArinc429SdiLabel(IDigits.DIGITS_2, sdi);
        return msgArinc429Sdi;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 3) {
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429Sdi, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SDI + getSerialsNumber());
        arinc429SdiLabel.setText(sdi);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_SDI, toD(sdi, IDigits.DIGITS_2));

        msgArinc429Sdi.setArinc429SdiLabel(IDigits.DIGITS_2, sdi);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 3) {
            sendMsg(msgArinc429Sdi, false);
        }
    }

    public void setCommandData(int sdi, boolean isFromEventBus) {
        String sSdi = SerialsUtils.getHexBinFromLong(sdi, 2, DIGITS_2);
        if (!arinc429SdiLabel.getText().equals(sSdi)) {
            onTextListener(sSdi, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429SdiLabel:
                //该为2进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_2, onSdiListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onSdiListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(result, false);
        }
    };

    private void onTextListener(String text, boolean isFromEventBus) {
        int cmdCh= TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SDI + getSerialsNumber(), text);
        arinc429SdiLabel.setEdit(text);
        msgArinc429Sdi.setArinc429SdiLabel(IDigits.DIGITS_2, text);
        sendMsg(msgArinc429Sdi, isFromEventBus);
        Command.get().getTrigger_m429().setType(cmdCh, 3, 0, toD(text, 2), 0, 0, false);

        if (!isFromEventBus) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
            if(serialChannel == null) return;
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
            a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_SDI, toD(text, IDigits.DIGITS_2));
        }
    }
}
