package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
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
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsLin extends RelativeLayout {
    private Context context;
    private RightViewSelect vSource, vLinType, vIdle, vBaudRate;
    private RightViewUserDefineEdit tvUserDefine;
    private TopDialogNumberKeyBoard dialogKeyBoard;

    private RightMsgSerialsLin msgDetailsLin;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    public int serialsNumber;
    private ViewGroup rootView;

    LinBus linBus;

    public RightLayoutSerialsLin(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsLin(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsLin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        rootView=(ViewGroup) View.inflate(context, R.layout.layout_right_serials_lin, this);

        vSource = (RightViewSelect) findViewById(R.id.source);
        vLinType = (RightViewSelect) findViewById(R.id.linType);
        vIdle = (RightViewSelect) findViewById(R.id.idle);
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate);
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsLin);

        vSource.setArray(GlobalVar.get().getChannelsName());

        vSource.setOnItemClickListener(onItemClickListener);
        vLinType.setOnItemClickListener(onItemClickListener);
        vIdle.setOnItemClickListener(onItemClickListener);
        vBaudRate.setOnItemClickListener(onItemClickListener);
        tvUserDefine.setOnEditClickListener(onEditClickListener);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);

        initData();
    }

    private void initData() {
        msgDetailsLin = new RightMsgSerialsLin();
        msgDetailsLin.setSource(vSource.getSelectItem());
        msgDetailsLin.setLinType(vLinType.getSelectItem());
        msgDetailsLin.setIdleLevel(vIdle.getSelectItem());
        msgDetailsLin.setBaudRate(vBaudRate.getSelectItem());
        msgDetailsLin.setBaudRateDefine("");
    }

    public void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + serialsNumber);
        int linType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + serialsNumber);
        int idle = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_IDLE + serialsNumber);
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_BAUDRATE + serialsNumber);
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber);
        vSource.setSelectIndex(source);
        vLinType.setSelectIndex(linType);
        vIdle.setSelectIndex(idle);
        if (!StrUtil.isEmpty(userDefine)) {
            tvUserDefine.setText(userDefine);
            vBaudRate.clearSelect();
        } else {
            tvUserDefine.setText("");
            vBaudRate.setSelectIndex(baudRate);
        }

        Command.get().getTrigger_lin().setSource(serialsNumber - 1, source, false);
        Command.get().getTrigger_lin().setLinType(serialsNumber - 1, source, false);
        Command.get().getTrigger_lin().setIdle(serialsNumber - 1, idle, false);
        Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, getBaudRateValueToScope(), false);

        Command.get().getBus_lin().Channel(serialsNumber-1,source,false);
        Command.get().getBus_lin().LinType(serialsNumber - 1, linType, false);
        Command.get().getBus_lin().IdLevel(serialsNumber-1,idle,false);
        if (!StrUtil.isEmpty(userDefine)){
            Command.get().getBus_lin().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(userDefine),false);
            Command.get().getBus_lin().BaudRate(serialsNumber-1,-1,false);
        }else {
            Command.get().getBus_lin().BaudRate(serialsNumber-1,baudRate,false);
            Command.get().getBus_lin().UserBaud(serialsNumber-1,-1,false);
        }

        linBus.setSrcChIdx(source);
        linBus.setLinType(linType);
        linBus.setIdleLevel(idle);
        linBus.setBaudRate(getBaudRateValueToScope());

        msgDetailsLin.setSource(vSource.getSelectItem());
        msgDetailsLin.setLinType(vLinType.getSelectItem());
        msgDetailsLin.setIdleLevel(vIdle.getSelectItem());
        msgDetailsLin.setBaudRate(vBaudRate.getSelectItem());
        msgDetailsLin.setBaudRateDefine(tvUserDefine.getText());
//        if (baudRate == vBaudRate.getSelectCount() - 1) {
//            tvUserDefine.setVisibility(VISIBLE);
//            vBaudRate.clearSelect();
//            RightBeanSelect bean = msgDetailsLin.getBaudRate();
//            msgDetailsLin.setBaudRate(new RightBeanSelect(bean.getIndex(), userDefine, bean.isCheck()));
//        } else {
//            tvUserDefine.setVisibility(GONE);
//        }
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.LIN) {
            sendMsg(false);
        }
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    private int getBaudRateValueToScope() {
        if (StrUtil.isEmpty(tvUserDefine.getText())) {
            return TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText());
        } else {
            return TBookUtil.getIntFromBaudRate(tvUserDefine.getText().toString());
        }
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
        int tChan= TChan.toSerialTChan(serialsNumber);
        int fpgaChan=TChan.toFpgaChNo(tChan);
        setControlColorByChIdx(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        linBus = (LinBus) serialChannel.getBus(IBus.LIN);

    }
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx);
        vLinType.setControlColorByChIdx(chIdx);
        vIdle.setControlColorByChIdx(chIdx);
        vBaudRate.setControlColorByChIdx(chIdx);
    }

    public RightMsgSerialsLin getMsgDetailsLin() {
        return msgDetailsLin;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsLin.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsLin, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERLIN_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vSource.getSelectIndex()) {
                        vSource.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vLinType.getSelectIndex()) {
                        vLinType.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_IDLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vIdle.getSelectIndex()) {
                        vIdle.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_BAUDRATE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) {
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1]));
                        if ((vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && !vBaudRate.getSelectItem().getText().equals(baudRate))
                                || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().toString().equals(baudRate))) {
                            if (vBaudRate.setSelectText(baudRate)) {
//                                tvUserDefine.setVisibility(GONE);
                                tvUserDefine.setText("");
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                            } else {
//                                tvUserDefine.setVisibility(VISIBLE);
                                vBaudRate.clearSelect();
                                tvUserDefine.setText(baudRate);
                                vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1);
                                onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true);
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_Lin_BaudRate:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    int rateIndex=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return;
                    vBaudRate.setSelectIndex(rateIndex);
                    tvUserDefine.setText("");
                    onCheckChanged(vBaudRate.getId(),vBaudRate.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_Userbaud:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    float rate=Integer.parseInt(params[1])/1000.0f;
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return;
                    vBaudRate.clearSelect();
                    onTextChanged(tvUserDefine.getId(),rate+"kb/s",false);

                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_Channel:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    int ch=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return;
                    vSource.setSelectIndex(ch);
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false);

                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
//                    int sNum = Integer.parseInt(params[0]);
                    int linS = Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vLinType.setSelectIndex(linS);
                    onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), false);
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Lin_IdLevel:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    int idle=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return;
                    vIdle.setSelectIndex(idle);
                    onCheckChanged(vIdle.getId(),vIdle.getSelectItem(),false);
                }break;

            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (linBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && linBus.equals(eventBase.getData())) {
                if (linBus.getSrcChIdx() != vSource.getSelectItem().getIndex()) {
                    vSource.setSelectIndex(linBus.getSrcChIdx());
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true);
                }
                if (linBus.getLinType() != vLinType.getSelectItem().getIndex()) {
                    vLinType.setSelectIndex(linBus.getLinType());
                    onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), true);
                }
                if (linBus.getIdleLevel() != vIdle.getSelectItem().getIndex()) {
                    vIdle.setSelectIndex(linBus.getIdleLevel());
                    onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), true);
                }
                String baudRate = TBookUtil.getBaudRateFromInt(linBus.getBaudRate());
                if ((/*vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 &&*/ !vBaudRate.getSelectItem().getText().equals(baudRate))
                        || (/*vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 &&*/ !tvUserDefine.getText().toString().equals(baudRate))) {
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
            }
        }
    };

    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton();
            String s = tvUserDefine.getText();
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
            if (!StrUtil.isEmpty(s)) {
                String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
                double preDouble = Double.parseDouble(number);
                String preBs = s.replace(number, "");
                dialogKeyBoard.setBaudRateData(preDouble, preBs, 2400, 625 * 1000, onDismissListener);
            } else {
                dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS, 2400, 625 * 1000, onDismissListener);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvUserDefine.getId(), result, false);
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
        //  Tools.PrintControlsLocation("linTypeLocation", rootView);
        if (viewId == vSource.getId()) {
            if (!isFromEventBus) {
                linBus.setSrcChIdx(item.getIndex());
            }
            Command.get().getTrigger_lin().setSource(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_lin().Channel(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsLin.setSource(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vLinType.getId()) {
            if (!isFromEventBus) {
                linBus.setLinType(item.getIndex());
            }
            Command.get().getTrigger_lin().setLinType(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_lin().LinType(serialsNumber - 1, item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsLin.setLinType(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vIdle.getId()) {
            if (!isFromEventBus) {
                linBus.setIdleLevel(item.getIndex());
            }
            Command.get().getTrigger_lin().setIdle(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_lin().IdLevel(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_IDLE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsLin.setIdleLevel(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vBaudRate.getId()) {
            Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_lin().BaudRate(serialsNumber-1,item.getIndex(),false);
            Command.get().getBus_lin().UserBaud(serialsNumber-1,-1,false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_BAUDRATE + serialsNumber, String.valueOf(item.getIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber, "");
            tvUserDefine.setText("");
            if (!isFromEventBus) {
                int baudRate = getBaudRateValueToScope();
                linBus.setBaudRate(baudRate);
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
            }
            msgDetailsLin.setBaudRate(item);
            sendMsg(isFromEventBus);
            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_LIN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_LIN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_LIN);
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_LIN);
            }
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) {
            String s = result.replace("kb/s", "");
            if (s.length() > 4 && s.substring(0, 4).contains(".")) {
                result = s.substring(0, 5);
                result += "kb/s";
            } else if (s.length() > 4) {
                result = s.substring(0, 4);
                result += "kb/s";
            }
            vBaudRate.clearSelect();
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) {
                if (!isFromEventBus) {
                    int baudRate = TBookUtil.getIntFromBaudRate(result);
                    linBus.setBaudRate(baudRate);
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
                }
                Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
                Command.get().getBus_lin().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false);
                Command.get().getBus_lin().BaudRate(serialsNumber-1,-1,false);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber, result);
                tvUserDefine.setText(result);
                msgDetailsLin.setBaudRateDefine(result);
                sendMsg(isFromEventBus);
                SerialBusManage.getInstance().clearSerialBusTxtBuffer();
            }
        }
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}
