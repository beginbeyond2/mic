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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi;
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

public class TopLayoutTriggerSerialsArinc429LabelSdi extends TopLayoutTriggerSerialsBaseDetail {
    private TopViewEdit arinc429LabelSdiLabel;
    private TopViewEdit arinc429LabelSdiSdi;
    private SerialsDetailArinc429LabelSdi msgArinc429LabelSdi;

    @Override
    protected void initView(View view) {
        arinc429LabelSdiLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelSdiLabel);
        arinc429LabelSdiSdi = (TopViewEdit) view.findViewById(R.id.arinc429LabelSdiSdi);
        arinc429LabelSdiLabel.setOnClickEditListener(onClickEditListener);
        arinc429LabelSdiSdi.setOnClickEditListener(onClickEditListener);
        msgArinc429LabelSdi = new SerialsDetailArinc429LabelSdi();
        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, arinc429LabelSdiLabel.getText());
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, arinc429LabelSdiSdi.getText());
        msgArinc429LabelSdi.setArinc429LabelSdiLabelTitle(arinc429LabelSdiLabel.getHead());
        msgArinc429LabelSdi.setArinc429LabelSdiSdiTitle(arinc429LabelSdiSdi.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_arinc429labelsdi;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber());
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber());
        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, label);
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, sdi);
        return msgArinc429LabelSdi;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 6) {
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429LabelSdi, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber());
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber());
        arinc429LabelSdiLabel.setText(label);
        arinc429LabelSdiSdi.setText(sdi);

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
        a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(sdi, IDigits.DIGITS_2));
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(label, IDigits.DIGITS_8));

        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, label);
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, sdi);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 6) {
            sendMsg(msgArinc429LabelSdi, false);
        }
    }

    public void setCommandData(int label, int sdi, boolean isFromEventBus) {
        String sLabel = SerialsUtils.getHexBinFromLong(label, 3, DIGITS_8);
        String sSdi = SerialsUtils.getHexBinFromLong(sdi, 2, DIGITS_2);
        if (!arinc429LabelSdiLabel.getText().equals(sLabel)) {
            onTextListener(arinc429LabelSdiLabel, sLabel, isFromEventBus);
        }
        if (!arinc429LabelSdiSdi.getText().equals(sSdi)) {
            onTextListener(arinc429LabelSdiSdi, sSdi, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.arinc429LabelSdiLabel:
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener);
                break;
            case R.id.arinc429LabelSdiSdi:
                //该为2进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_2, onSdiListener);
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelSdiLabel, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onSdiListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(arinc429LabelSdiSdi, result, false);

        }
    };

    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);

        if (view.getId() == arinc429LabelSdiLabel.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber(), text);
            arinc429LabelSdiLabel.setEdit(text);
            msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, text);
            sendMsg(msgArinc429LabelSdi, isFromEventBus);
            Command.get().getTrigger_m429().setType(serialsNumber, 6, toD(arinc429LabelSdiLabel.getText(), IDigits.DIGITS_8), toD(arinc429LabelSdiSdi.getText(), 2), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(text, 8));
            }
        } else if (view.getId() == arinc429LabelSdiSdi.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber(), text);
            arinc429LabelSdiSdi.setEdit(text);
            msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, text);
            sendMsg(msgArinc429LabelSdi, isFromEventBus);
            Command.get().getTrigger_m429().setType(cmdCh, 6, toD(arinc429LabelSdiLabel.getText(), IDigits.DIGITS_8), toD(arinc429LabelSdiSdi.getText(), 2), 0, 0, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(text, IDigits.DIGITS_2));
            }
        }
    }
}
