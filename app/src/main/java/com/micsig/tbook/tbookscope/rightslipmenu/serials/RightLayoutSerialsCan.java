package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.smart.Property;
import com.micsig.tbook.scope.Bus.CanBus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.ExKeysMsgRightCanPercent;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsCan extends RelativeLayout {
    private Context context;
    private RightViewSelect vSource, vSignal, vBaudRate, vFDBaudRate;
    private RightViewUserDefineEdit tvUserDefine, tvFDUserDefine;
    private TopDialogNumberKeyBoard dialogKeyBoard;
    private TextView tvBaudRatePercent, tvFdPercent;
    private RightViewSelect vISO;

    private RightMsgSerialsCan msgDetailsCan;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private CanBus canBus;

    private ViewGroup rootView;

    public RightLayoutSerialsCan(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsCan(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsCan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_serials_can, this);
        vSource = (RightViewSelect) findViewById(R.id.source);
        vSignal = (RightViewSelect) findViewById(R.id.signal);
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate);
        vFDBaudRate = (RightViewSelect) findViewById(R.id.fdBaudRate);
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsCan);
        tvFDUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsCanFD);
        tvBaudRatePercent = (TextView) findViewById(R.id.baudRatePercent);
        tvFdPercent = (TextView) findViewById(R.id.fdPercent);
        vISO = (RightViewSelect) findViewById(R.id.iSO);

        vSource.setArray(GlobalVar.get().getChannelsName());

        vSource.setOnItemClickListener(onItemClickListener);
        vSignal.setOnItemClickListener(onItemClickListener);
        vBaudRate.setOnItemClickListener(onItemClickListener);
        vFDBaudRate.setOnItemClickListener(onItemClickListener);
        tvUserDefine.setOnEditClickListener(onEditClickListener);
        tvFDUserDefine.setOnEditClickListener(onEditClickListener);
        tvBaudRatePercent.setOnClickListener(onClickListener);
        tvFdPercent.setOnClickListener(onClickListener);
        vISO.setOnItemClickListener(onItemClickListener);

//        tvUserDefine.setVisibility(GONE);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        initData();
    }

    private void initData() {
        msgDetailsCan = new RightMsgSerialsCan();
        msgDetailsCan.setSource(vSource.getSelectItem());
        msgDetailsCan.setSignal(vSignal.getSelectItem());
        msgDetailsCan.setBaudRate(vBaudRate.getSelectItem());
        msgDetailsCan.setFDBaudRate(vFDBaudRate.getSelectItem());
        msgDetailsCan.setBaudRateDefine("");
        msgDetailsCan.setFDBaudRateDefine("");
        msgDetailsCan.setISO(vISO.getSelectItem());
    }

    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + serialsNumber);
        int signal = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serialsNumber);
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_BAUDRATE + serialsNumber);
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber);

        int fdBaudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + serialsNumber);
        String fdUserDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber);
        String percent = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_PERCENT + serialsNumber);
        String fdPercent = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDPERCENT + serialsNumber);
        int fdISO = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_ISO + serialsNumber);

        vSource.setSelectIndex(source);
        vSignal.setSelectIndex(signal);
        if (!StrUtil.isEmpty(userDefine)) {
            tvUserDefine.setText(userDefine);
            vBaudRate.clearSelect();
        } else {
            tvUserDefine.setText("");
            vBaudRate.setSelectIndex(baudRate);
        }

        if (!StrUtil.isEmpty(fdUserDefine)) {
            tvFDUserDefine.setText(fdUserDefine);
            vFDBaudRate.clearSelect();
        } else {
            tvFDUserDefine.setText("");
            vFDBaudRate.setSelectIndex(fdBaudRate);
        }
        tvBaudRatePercent.setText(percent + "%");
        tvFdPercent.setText(fdPercent + "%");
        vISO.setSelectIndex(fdISO);

        Command.get().getTrigger_can().setSource(serialsNumber - 1, source, false);
        Command.get().getTrigger_can().setIdle(serialsNumber - 1, signal, false);
        Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, getBaudRateValue(), false);

        Command.get().getBus_can().Channel(serialsNumber - 1, source, false);
        Command.get().getBus_can().Signal(serialsNumber - 1, signal, false);
        Command.get().getBus_can().ISO(serialsNumber - 1, fdISO, false);
        if (!StrUtil.isEmpty(userDefine)) {
            vBaudRate.clearSelect();
            Command.get().getBus_can().BaudRate(serialsNumber - 1, -1, false);
            Command.get().getBus_can().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(userDefine), false);
        } else {
            tvUserDefine.setText("");
            Command.get().getBus_can().BaudRate(serialsNumber - 1, baudRate, false);
            Command.get().getBus_can().UserBaud(serialsNumber - 1, -1, false);
        }
        Command.get().getBus_can().SAMPlepoint(serialsNumber - 1, Double.parseDouble(percent), false);
        Command.get().getBus_can().FDSAmplepoint(serialsNumber - 1, Double.parseDouble(fdPercent), false);
        if (!StrUtil.isEmpty(fdUserDefine)) {
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(fdUserDefine), false);
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false);
        } else {
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false);
            Command.get().getBus_can().FDBAudrate(serialsNumber - 1, fdBaudRate, false);
        }


        canBus.setSrcChIdx(source);
        canBus.setSignal(signal);
        canBus.setBaudRate(getBaudRateValue());
        if (!(ScopeConfig.getConfig().isBusEnable(Property.BUS_CAN_FD) || App.IsDebug())) {
            canBus.setFDBandRate(0);
            tvFDUserDefine.setText("");
            vFDBaudRate.setSelectIndex(0);
            vFDBaudRate.setEnabled(false);
            tvFdPercent.setEnabled(false);
            tvFDUserDefine.setEnabled(false);
            vISO.setEnabled(false);
        } else {
            vFDBaudRate.setEnabled(true);
            tvFdPercent.setEnabled(true);
            tvFDUserDefine.setEnabled(true);
            vISO.setEnabled(true);
            canBus.setFDBandRate(getFDBaudRateValue());
            canBus.setSamplePlace1(Double.parseDouble(percent) * 1.0 / 100);
            canBus.setSamplePlace2(Double.parseDouble(fdPercent) * 1.0 / 100);
            canBus.setISO(fdISO == 0);
        }

        msgDetailsCan.setSource(vSource.getSelectItem());
        msgDetailsCan.setSignal(vSignal.getSelectItem());
        msgDetailsCan.setBaudRate(vBaudRate.getSelectItem());
        msgDetailsCan.setBaudRateDefine(tvUserDefine.getText());
        msgDetailsCan.setFDBaudRate(vFDBaudRate.getSelectItem());
        msgDetailsCan.setFDBaudRateDefine(tvFDUserDefine.getText());
        msgDetailsCan.setISO(vISO.getSelectItem());
//        if (baudRate == vBaudRate.getSelectCount() - 1) {
//            this.tvUserDefine.setVisibility(VISIBLE);
//            vBaudRate.clearSelect();
//            RightBeanSelect bean = msgDetailsCan.getBaudRate();
//            msgDetailsCan.setBaudRate(new RightBeanSelect(bean.getIndex(), userDefine, bean.isCheck()));
//        } else {
//            this.tvUserDefine.setVisibility(GONE);
//        }
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.CAN) {
            sendMsg(false);
        }
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_RIGHTCAN_PERCENT).subscribe(consumerExKeysRightCanPercent);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    private int getBaudRateValue() {
        if (StrUtil.isEmpty(tvUserDefine.getText())) {
            return TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText());
        } else {
            return TBookUtil.getIntFromBaudRate(tvUserDefine.getText().toString());
        }
    }

    private int getFDBaudRateValue() {
        if (StrUtil.isEmpty(tvFDUserDefine.getText())) {
            return TBookUtil.getIntFromBaudRate(vFDBaudRate.getSelectItem().getText());
        } else {
            return TBookUtil.getIntFromBaudRate(tvFDUserDefine.getText().toString());
        }
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
        int tChan=TChan.toSerialTChan(serialsNumber);
        int fpgaChan=TChan.toFpgaChNo(tChan);
        setControlColorByChIdx(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        canBus = (CanBus) serialChannel.getBus(IBus.CAN);

    }
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx);
        vSignal.setControlColorByChIdx(chIdx);
        vBaudRate.setControlColorByChIdx(chIdx);
        vFDBaudRate.setControlColorByChIdx(chIdx);
        vISO.setControlColorByChIdx(chIdx);
    }

    public RightMsgSerialsCan getMsgDetailsCan() {
        return msgDetailsCan;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsCan.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderLeftMenu, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERCAN_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (serialsNumber - 1 == Integer.parseInt(params[0])
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) {
                        vSource.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_IDLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (serialsNumber - 1 == Integer.parseInt(params[0])
                            && vSignal.getSelectIndex() != Integer.parseInt(params[1])) {
                        vSignal.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_BAUDRATE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (serialsNumber - 1 == Integer.parseInt(params[0])) {
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1]));
                        if (vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && (!vBaudRate.getSelectItem().getText().equals(baudRate))
                                || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().equals(baudRate))) {
                            if (vBaudRate.setSelectText(baudRate)) {
//                                tvUserDefine.setVisibility(GONE);
                                tvUserDefine.setText("");
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                            } else {
//                                tvUserDefine.setVisibility(VISIBLE);
                                vBaudRate.clearSelect();
                                tvUserDefine.setText(baudRate);
                                vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1);
                                onTextChanged(tvUserDefine.getId(), tvUserDefine.getText(), true);
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_Can_Channel: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int ch = Integer.parseInt(params[1]);
                    if ((serialsNumber - 1) == sNum) {
                        vSource.setSelectIndex(ch);
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_Signal: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int signal = Integer.parseInt(params[1]);
                    Logger.i(Command.TAG, "signal:" + signal);
                    if (serialsNumber - 1 == sNum) {
                        vSignal.setSelectIndex(signal);
                        onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_BaudRate: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int baudrate = Integer.parseInt(params[1]);
                    if (sNum == serialsNumber - 1) {
                        tvUserDefine.setText("");
                        vBaudRate.setSelectIndex(baudrate);
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_UserBaud: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    float userbaud = Integer.parseInt(params[1]) / 1000.0f;
                    if (sNum == serialsNumber - 1) {
                        vBaudRate.clearSelect();
                        onTextChanged(tvUserDefine.getId(), userbaud + "kb/s", false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_SamplePoint: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    String percent = (params[1]);
                    if (sNum == serialsNumber - 1) {
                        onTextChanged(tvBaudRatePercent.getId(), percent, false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDBaudrate: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int index = Integer.parseInt(params[1]);
                    if (sNum == serialsNumber - 1) {
                        tvFDUserDefine.setText("");
                        vFDBaudRate.setSelectIndex(index);
                        onItemClickListener.onItemClick(vFDBaudRate.getId(), vFDBaudRate.getSelectItem());
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDUserBaud: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    String unit = "kb/s";
                    float baudrate = Integer.parseInt(params[1]) / 1000.0f;
                    if (baudrate > 1000) {
                        baudrate = baudrate / 1000.0f;
                        unit = "Mb/s";
                    }
                    if (sNum == serialsNumber - 1) {
                        vFDBaudRate.clearSelect();
                        onTextChanged(tvFDUserDefine.getId(), baudrate + unit, false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDSamplePoint: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    String percent = (params[1]);
                    if (sNum == serialsNumber - 1) {
                        onTextChanged(tvFdPercent.getId(), percent, false);
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_ISO: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int iso = Integer.parseInt(params[1]);
                    if (sNum == serialsNumber - 1) {
                        vISO.setSelectIndex(iso);
                        onCheckChanged(vISO.getId(), vISO.getSelectItem(), true);
                    }
                }
                break;
            }
        }
    };

    private Consumer<ExKeysMsgRightCanPercent> consumerExKeysRightCanPercent = new Consumer<ExKeysMsgRightCanPercent>() {
        @Override
        public void accept(ExKeysMsgRightCanPercent msgPercent) throws Exception {
            if (msgPercent.isS1() == (serialsNumber == RightMsgSerials.SERIALS_S1)) {
                TextView textView = msgPercent.isTop() ? tvBaudRatePercent : tvFdPercent;
                double percent = Double.parseDouble(textView.getText().toString().replace("%", ""));
                percent = msgPercent.isAdd() ? percent + msgPercent.getCount() : percent - msgPercent.getCount();
                percent = Math.max(percent, 0);
                percent = Math.min(percent, 99);
                onTextChanged(textView.getId(), String.valueOf(percent), false);
            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (canBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && canBus.equals(eventBase.getData())) {

                if (canBus.getSrcChIdx() != vSource.getSelectIndex()) {
                    vSource.setSelectIndex(canBus.getSrcChIdx());
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true);
                }
                if (canBus.getSignal() != vSignal.getSelectIndex()) {
                    vSignal.setSelectIndex(canBus.getSignal());
                    onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), true);
                }
                String baudRate = TBookUtil.getBaudRateFromInt(canBus.getBaudRate());
                if (vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && (!vBaudRate.getSelectItem().getText().equals(baudRate))
                        || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().toString().equals(baudRate))) {
                    if (vBaudRate.setSelectText(baudRate)) {
//                        tvUserDefine.setVisibility(GONE);
                        tvUserDefine.setText("");
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                    } else {
//                        tvUserDefine.setVisibility(VISIBLE);
                        vBaudRate.clearSelect();
                        tvUserDefine.setText(baudRate);
                        vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1);
                        onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true);
                    }
                }
                if (canBus.isISO() != (vISO.getSelectIndex() == 0)) {
                    vISO.setSelectIndex(canBus.isISO() ? 0 : 1);
                    onCheckChanged(vISO.getId(), vISO.getSelectItem(), true);
                }
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
            ScreenUtil.getViewLocation(v);
            if (v.getId() == tvBaudRatePercent.getId()) {
                dialogKeyBoard.setFloatData(Double.parseDouble(tvBaudRatePercent.getText().toString().replace("%", ""))
                        , 0.0, 99.9
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                if (result.endsWith(".0")) {
                                    result = result.replace(".0", "");
                                }
                                onTextChanged(tvBaudRatePercent.getId(), result, false);
                            }
                        });
            } else if (v.getId() == tvFdPercent.getId()) {
                dialogKeyBoard.setFloatData(Double.parseDouble(tvFdPercent.getText().toString().replace("%", ""))
                        , 0.0, 99.9
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                if (result.endsWith(".0")) {
                                    result = result.replace(".0", "");
                                }
                                onTextChanged(tvFdPercent.getId(), result, false);
                            }
                        });
            }
        }
    };

    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton();
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
            if (view.getId() == tvUserDefine.getId()) {
                String s = tvUserDefine.getText();
                if (!StrUtil.isEmpty(s)) {
                    String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "")
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "")
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
                    double preDouble = Double.parseDouble(number);
                    String preBs = s.replace(number, "");
                    dialogKeyBoard.setBaudRateData(preDouble, preBs
                            , 10 * 1000, 5 * 1000 * 1000
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvUserDefine.getId(), result, false);
                                }
                            });
                } else {
                    dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS
                            , 10 * 1000, 5 * 1000 * 1000
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvUserDefine.getId(), result, false);
                                }
                            });
                }
            } else if (view.getId() == tvFDUserDefine.getId()) {
                String s = tvFDUserDefine.getText();
                if (!StrUtil.isEmpty(s)) {
                    String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "")
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "")
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
                    double preDouble = Double.parseDouble(number);
                    String preBs = s.replace(number, "");
                    dialogKeyBoard.setBaudRateData(preDouble, preBs
                            , 10 * 1000, 12 * 1000 * 1000
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvFDUserDefine.getId(), result, false);
                                }
                            });
                } else {
                    dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS
                            , 10 * 1000, 12 * 1000 * 1000
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvFDUserDefine.getId(), result, false);
                                }
                            });
                }
            }
        }
    };

    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            onCheckChanged(viewId, item, false);
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vSource.getId()) {
            if (!isFromEventBus) {
                canBus.setSrcChIdx(item.getIndex());
            }
            Command.get().getTrigger_can().setSource(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_can().Channel(serialsNumber - 1, item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsCan.setSource(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vSignal.getId()) {
            if (!isFromEventBus) {
                canBus.setSignal(item.getIndex());
            }
            Command.get().getTrigger_can().setIdle(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_can().Signal(serialsNumber - 1, item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsCan.setSignal(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vBaudRate.getId()) {
            Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_can().BaudRate(serialsNumber - 1, item.getIndex(), false);
//            Command.get().getBus_can().BaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_can().UserBaud(serialsNumber - 1, -1, false);

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_BAUDRATE + serialsNumber, String.valueOf(item.getIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber, "");
            tvUserDefine.setText("");
            if (!isFromEventBus) {
                int baudRate = getBaudRateValue();
                canBus.setBaudRate(baudRate);
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
            }
            msgDetailsCan.setBaudRate(item);
            sendMsg(isFromEventBus);
            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_CAN);
            }
        } else if (viewId == vFDBaudRate.getId()) {
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false);
            Command.get().getBus_can().FDBAudrate(serialsNumber - 1, item.getIndex(), false);

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + serialsNumber, String.valueOf(item.getIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber, "");
            tvFDUserDefine.setText("");
            if (!isFromEventBus) {
                int baudRate = getFDBaudRateValue();
                canBus.setFDBandRate(baudRate);
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
            }
            msgDetailsCan.setFDBaudRate(item);
            sendMsg(isFromEventBus);
            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_CAN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_CAN);
            }
        } else if (viewId == vISO.getId()) {
            Command.get().getBus_can().ISO(serialsNumber - 1, item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_ISO + serialsNumber, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                canBus.setISO(item.getIndex() == 0);
            }
            msgDetailsCan.setISO(item);
            sendMsg(isFromEventBus);
        }

        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) {
            String s = result.replace("kb/s", "");
            if (!result.equals(s)) {
                if (s.length() > 4) {
                    if (s.substring(0, 4).contains(".")) {
                        result = s.substring(0, 5);
                    } else {
                        result = s.substring(0, 4);
                    }
                    result += "kb/s";
                }
            } else {
                s = result.replace("Mb/s", "");
                if (s.length() > 4) {
                    if (s.substring(0, 4).contains(".")) {
                        result = s.substring(0, 5);
                    } else {
                        result = s.substring(0, 4);
                    }
                    result += "Mb/s";
                }
            }
            vBaudRate.clearSelect();
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) {
                if (!isFromEventBus) {
                    int baudRate = TBookUtil.getIntFromBaudRate(result);
                    canBus.setBaudRate(baudRate);
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
                }
                Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
                Command.get().getBus_can().BaudRate(serialsNumber - 1, -1, false);
                Command.get().getBus_can().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber, result);
                tvUserDefine.setText(result);
                msgDetailsCan.setBaudRateDefine(result);
                sendMsg(isFromEventBus);
                SerialBusManage.getInstance().clearSerialBusTxtBuffer();
            }
        } else if (viewId == tvFDUserDefine.getId()) {
            String s = result.replace("kb/s", "");
            if (!result.equals(s)) {
                if (s.length() > 4) {
                    if (s.substring(0, 4).contains(".")) {
                        result = s.substring(0, 5);
                    } else {
                        result = s.substring(0, 4);
                    }
                    result += "kb/s";
                }
            } else {
                s = result.replace("Mb/s", "");
                if (s.length() > 4) {
                    if (s.substring(0, 4).contains(".")) {
                        result = s.substring(0, 5);
                    } else {
                        result = s.substring(0, 4);
                    }
                    result += "Mb/s";
                }
            }
            vFDBaudRate.clearSelect();
            if (!result.equalsIgnoreCase(tvFDUserDefine.getText().toString())) {
                if (!isFromEventBus) {
                    int baudRate = TBookUtil.getIntFromBaudRate(result);
                    canBus.setFDBandRate(baudRate);
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
                }

                Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
                Command.get().getBus_can().FDBAudrate(serialsNumber - 1, -1, false);

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber, result);
                tvFDUserDefine.setText(result);
                msgDetailsCan.setFDBaudRateDefine(result);
                sendMsg(isFromEventBus);
                SerialBusManage.getInstance().clearSerialBusTxtBuffer();
            }
        } else if (viewId == tvBaudRatePercent.getId()) {
            if (StrUtil.isEmpty(result)) {
                result = "0";
            }
            result = result.replace("%", "");
            tvBaudRatePercent.setText(result + "%");
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_PERCENT + serialsNumber, result);
            canBus.setSamplePlace1(Double.parseDouble(result) / 100);
            Command.get().getBus_can().SAMPlepoint(serialsNumber - 1, Double.parseDouble(result), false);
        } else if (viewId == tvFdPercent.getId()) {
            if (StrUtil.isEmpty(result)) {
                result = "0";
            }
            result = result.replace("%", "");
            tvFdPercent.setText(result + "%");
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDPERCENT + serialsNumber, result);
            canBus.setSamplePlace2(Double.parseDouble(result) / 100);
            Command.get().getBus_can().FDSAmplepoint(serialsNumber - 1, Double.parseDouble(result), false);
        }
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}