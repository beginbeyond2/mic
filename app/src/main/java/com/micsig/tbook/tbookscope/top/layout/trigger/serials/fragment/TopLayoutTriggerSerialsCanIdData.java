package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.view.View;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Bus.CanBus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsCanIdData extends TopLayoutTriggerSerialsBaseDetail {
    private SerialsDetailCanIdData msgCanIdData;
    private TopViewEdit canIdDataId;
    private TopViewEdit canIdDataDlc;
    private TopViewEdit canIdDataData;

    @Override
    protected void initView(View view) {
        canIdDataId = (TopViewEdit) view.findViewById(R.id.canIdDataId);
        canIdDataDlc = (TopViewEdit) view.findViewById(R.id.canIdDataDlc);
        canIdDataData = (TopViewEdit) view.findViewById(R.id.canIdDataData);
        canIdDataId.setOnClickEditListener(onClickEditListener);
        canIdDataDlc.setOnClickEditListener(onClickEditListener);
        canIdDataData.setOnClickEditListener(onClickEditListener);
        msgCanIdData = new SerialsDetailCanIdData();
        msgCanIdData.setCanIdDataId(DIGITS_16, canIdDataId.getText());
        msgCanIdData.setCanIdDataDlc(DIGITS_10, canIdDataDlc.getText());
        msgCanIdData.setCanIdDataData(DIGITS_16, canIdDataData.getText());
        msgCanIdData.setCanIdDataIdTitle(canIdDataId.getHead());
        msgCanIdData.setCanIdDataDlcTitle(canIdDataDlc.getHead());
        msgCanIdData.setCanIdDataDataTitle(canIdDataData.getHead());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_triggerserials_caniddata;
    }

    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) {
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber());
        String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber());
        msgCanIdData.setCanIdDataId(DIGITS_16, id);
        msgCanIdData.setCanIdDataDlc(DIGITS_10, dlc);
        msgCanIdData.setCanIdDataData(DIGITS_16, data);
        return msgCanIdData;
    }

    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) {
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 4) {
            //当且仅当当前can列表选中的是该项时，才向外发送消息
            sendMsg(msgCanIdData, rightMsgSerials.isFromEventBus());
        }
    }

    @Override
    protected void setCache() {
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber());
        String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber());
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber());
        canIdDataId.setText(id);
        canIdDataDlc.setText(dlc);
        canIdDataData.setText(data);

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        if(serialChannel == null) return;
        CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
        canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, toD(id, 16));
        canBus.setDlc(SerialsUtils.getCanDlcFromShow(dlc));
        canBus.setData(toDLong(data, 16));

        int type = Command.get().getTrigger_can().getType(getSerialsNumber() - 1);
        if (type == 4) {
            Command.get().getTrigger_can().setType(getSerialsNumber() - 1, type, Tools.HexStringToInt(id), Integer.parseInt(dlc), toDLong(data, 16), false);
        }

        msgCanIdData.setCanIdDataId(DIGITS_16, id);
        msgCanIdData.setCanIdDataDlc(DIGITS_10, dlc);
        msgCanIdData.setCanIdDataData(DIGITS_16, data);
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3)
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4))
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 4) {
            sendMsg(msgCanIdData, false);
        }
    }

    public void setCommandData(int id, int dlc, long data, boolean isFromEventBus) {
        String sId = SerialsUtils.getHexBinFromLong(id, 8, DIGITS_16);
        dlc = Math.min(64, dlc);
        dlc = Math.max(0, dlc);
        String sDlc = String.valueOf(dlc);
        int bits = dlc >= 8 ? 8 : dlc;
        String sData = SerialsUtils.getHexBinFromLong(data, bits * 2, DIGITS_16);
        if (!canIdDataId.getText().equals(sId)) {
            onTextListener(canIdDataId, sId, isFromEventBus);
        }
        if (!canIdDataDlc.getText().equals(sDlc)) {
            onTextListener(canIdDataDlc, sDlc, isFromEventBus);
        }
        if (!canIdDataData.getText().equals(sData)) {
            onTextListener(canIdDataData, sData, isFromEventBus);
        }
    }

    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) {

    }

    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.canIdDataId:
                //该为16进制,8位限制
                dialogKeyBoard.setDecimalData(8, DIGITS_16, onIdListener);
                break;
            case R.id.canIdDataDlc:
//                //只能输入0-8这9个数字
//                dialogKeyBoard.setDecimalData(1, DIGITS_0_8, onDlcListener);
                //需要显示0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64,这16个数
                dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_16, true, onDlcListener);
                break;
            case R.id.canIdDataData:
                //该为16进制,12位限制
                int bit = Integer.parseInt(canIdDataDlc.getText());
                if (bit != 0) {
                    bit = Math.min(bit, 8);
                    dialogKeyBoard.setDecimalData(bit * 2, DIGITS_16, onDataListener);
                }
                break;
        }
    }

    private TopDialogNumberKeyBoard.OnDismissListener onIdListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(canIdDataId, result, false);
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDlcListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            //需要显示0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64,这16个数
            Logger.i("onDlcListener Result=" + result);
            try {
                int temp = Integer.parseInt(result);
                temp = findNearResult(temp);
                result = String.valueOf(temp);
            } catch (Exception e) {
                Logger.e("onDlcListener result=" + result + " can not parse int");
                e.printStackTrace();
            } finally {
                onTextListener(canIdDataDlc, result, false);
            }
        }
    };


    private int findNearResult(int temp) {
        ArrayList<Integer> limitList = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 12, 16, 20, 24, 32, 48, 64));
        if (temp >= limitList.get(limitList.size() - 1)) {
            return limitList.get(limitList.size() - 1);
        }
        for (int i = 0; i < limitList.size() - 1; i++) {
            int left = limitList.get(i);
            int right = limitList.get(i + 1);
            if (left <= temp && right > temp) {
                if (Math.abs(left - temp) <= Math.abs(temp - right)) {
                    temp = left;
                } else {
                    temp = right;
                }
                break;
            }
        }
        return temp;
    }

    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextListener(canIdDataData, result, false);
        }
    };

    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) {
        int cmdCh= TChan.toFpgaChNo(serialsNumber);
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber);
        if (view.getId() == canIdDataId.getId()) {
            int val = (int) toDLong(text, IDigits.DIGITS_16);
            text = SerialsUtils.getHexBinFromInt(val, 8, IDigits.DIGITS_16);

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber(), text);
            canIdDataId.setEdit(text);
            msgCanIdData.setCanIdDataId(DIGITS_16, text);
            sendMsg(msgCanIdData, isFromEventBus);
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16);
            int dlc = Integer.parseInt(canIdDataDlc.getText());//toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long data = toDLong(canIdDataData.getText(), IDigits.DIGITS_16);
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, data, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
                canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, toD(text, IDigits.DIGITS_16));
            }
        } else if (view.getId() == canIdDataDlc.getId()) {
            if (StrUtil.isEmpty(text)) return;//没选DLC，保持现有的
            int bit = Integer.parseInt(text);
            bit = Math.min(bit, 8);
            String sData = KeyBoardNumberUtil.reCalculateSpace(
                    KeyBoardNumberUtil.toBits(
                            canIdDataData.getText().replace(" ", "")
                            , bit * 2), KeyBoardNumberUtil.DIGITS_16).trim();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber(), text);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber(), sData);
            canIdDataData.setText(sData);
            canIdDataDlc.setEdit(text);
            msgCanIdData.setCanIdDataData(DIGITS_16, sData);
            msgCanIdData.setCanIdDataDlc(DIGITS_10, text);
            sendMsg(msgCanIdData, isFromEventBus);
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16);
            int dlc = Integer.parseInt(canIdDataDlc.getText());  //toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long lData = toDLong(canIdDataData.getText(), IDigits.DIGITS_16);
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, lData, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
                canBus.setDlc(SerialsUtils.getCanDlcFromShow(text));
                canBus.setData(toDLong(sData, IDigits.DIGITS_16));
            }
        } else if (view.getId() == canIdDataData.getId()) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber(), text);
            canIdDataData.setEdit(text);
            msgCanIdData.setCanIdDataData(DIGITS_16, text);
            sendMsg(msgCanIdData, isFromEventBus);
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16);
            int dlc = Integer.parseInt(canIdDataDlc.getText()); //toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long data = toDLong(canIdDataData.getText(), IDigits.DIGITS_16);
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, data, false);

            if (!isFromEventBus) {
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
                if(serialChannel == null) return;
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
                canBus.setData(toDLong(text, IDigits.DIGITS_16));
            }
        }
    }
}
