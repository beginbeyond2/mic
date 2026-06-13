package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.UartBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBaudRate;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
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

public class RightLayoutSerialsUart extends LinearLayout {
    private Context context;
    private RightViewSelect vRx, vIdle, vCheck, vBits, vDisplay, vBaudRate;
    private RightViewUserDefineEdit tvUserDefine;
    private DialogBaudRate dialogBaudRate;
    private TopDialogNumberKeyBoard dialogKeyBoard;
    private RightMsgSerialsUart msgDetailsUart;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private UartBus uartBus;

    public RightLayoutSerialsUart(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsUart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsUart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_uart, this);
        setOrientation(VERTICAL);
        vRx = (RightViewSelect) findViewById(R.id.rx);
        vIdle = (RightViewSelect) findViewById(R.id.idle);
        vCheck = (RightViewSelect) findViewById(R.id.check);
        vBits = (RightViewSelect) findViewById(R.id.bits);
        vDisplay = (RightViewSelect) findViewById(R.id.display);
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate);
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsUart);

        vRx.setArray(GlobalVar.get().getChannelsName());

        vRx.setOnItemClickListener(onItemClickListener);
        vIdle.setOnItemClickListener(onItemClickListener);
        vCheck.setOnItemClickListener(onItemClickListener);
        vBits.setOnItemClickListener(onItemClickListener);
        vDisplay.setOnItemClickListener(onItemClickListener);
        vBaudRate.setOnItemClickListener(onItemClickListener);
        tvUserDefine.setOnEditClickListener(onEditClickListener);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        initData();
    }

    private void initData() {
        msgDetailsUart = new RightMsgSerialsUart();
        msgDetailsUart.setRx(vRx.getSelectItem());
        msgDetailsUart.setIdleLevel(vIdle.getSelectItem());
        msgDetailsUart.setCheck(vCheck.getSelectItem());
        msgDetailsUart.setBits(vBits.getSelectItem());
        msgDetailsUart.setDisplay(vDisplay.getSelectItem());
        msgDetailsUart.setBaudRateDefine(tvUserDefine.getText().toString().trim());
        msgDetailsUart.setBaudRate(vBaudRate.getSelectItem());
    }

    public void setCache() {
        int rx = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + serialsNumber);
        int idle = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_IDLE + serialsNumber);
        int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + serialsNumber);
        int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + serialsNumber);
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber);
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber);
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber);

        vRx.setSelectIndex(rx);
        vIdle.setSelectIndex(idle);
        vCheck.setSelectIndex(check);
        vBits.setSelectIndex(bits);
        vDisplay.setSelectIndex(display);
        if (!StrUtil.isEmpty(userDefine)) {
            tvUserDefine.setText(userDefine);
            vBaudRate.clearSelect();
        } else {
            tvUserDefine.setText("");
            vBaudRate.setSelectIndex(baudRate);
        }

        Command.get().getTrigger_uart().setSource(serialsNumber - 1, rx, false);
        Command.get().getTrigger_uart().setIdle(serialsNumber - 1, idle, false);
        Command.get().getTrigger_uart().setCheck(serialsNumber - 1, check, false);
        Command.get().getTrigger_uart().setBits(serialsNumber - 1, TBookUtil.getIntFromBaudRate(String.valueOf(bits)), false);
        Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, getBaudRateValueToScope(), false);
        Command.get().getTrigger_uart().setDisplay(serialsNumber - 1, display, false);

        Command.get().getBus_uart().Rx(serialsNumber-1,rx,false);
        Command.get().getBus_uart().IdLevel(serialsNumber-1,idle,false);
        Command.get().getBus_uart().Width(serialsNumber-1,bits,false);
        if (!StrUtil.isEmpty(userDefine)) {
            Command.get().getBus_uart().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(userDefine), false);
            Command.get().getBus_uart().BaudRate(serialsNumber - 1, -1, false);
        } else {
            Command.get().getBus_uart().BaudRate(serialsNumber - 1, baudRate, false);
            Command.get().getBus_uart().UserBaud(serialsNumber - 1, -1, false);
        }

        Command.get().getBus_uart().Display(serialsNumber-1,display,false);

        uartBus.setRxChIdx(rx);
        uartBus.setIdleLevel(idle);
        uartBus.setVerify(check == 2 ? UartBus.UART_EVEN_VERIFY : check);
        uartBus.setBits(Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", "")));
        uartBus.setBaudRate(getBaudRateValueToScope());
        uartBus.setDisplayFormat(getDisplayToScope(display));

//        int chNo = serialsNumber == RightMsgSerials.SERIALS_S1 ? IWave.S1 : IWave.S2;
        int chNo= TChan.toSerialTChan(serialsNumber);
        SerialBusManage.getInstance().getSerialBus(chNo).setUartChecked(check == 0);
        SerialBusManage.getInstance().getSerialBus(chNo).setUartBits(bits + 5);
        SerialBusManage.getInstance().getSerialBus(chNo).setUartEncoding(display);

        msgDetailsUart.setRx(vRx.getSelectItem());
        msgDetailsUart.setIdleLevel(vIdle.getSelectItem());
        msgDetailsUart.setCheck(vCheck.getSelectItem());
        msgDetailsUart.setBits(vBits.getSelectItem());
        msgDetailsUart.setDisplay(vDisplay.getSelectItem());
        msgDetailsUart.setBaudRate(vBaudRate.getSelectItem());
        msgDetailsUart.setBaudRateDefine(tvUserDefine.getText());
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.UART) {
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

    private int getDisplayToScope(int displayIndex) {
        if (displayIndex == 0) {
            return IBus.DISPLAY_HEX_DISPLAY;
        } else if (displayIndex == 1) {
            return IBus.DISPLAY_BIN_DISPLAY;
        } else {
            return IBus.DISPLAY_ASC_DISPLAY;
        }
    }

    private int getDisplayFromScope(int displayIndex) {
        if (displayIndex == IBus.DISPLAY_HEX_DISPLAY) {
            return 0;
        } else if (displayIndex == IBus.DISPLAY_BIN_DISPLAY) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
        int tChan=TChan.toSerialTChan(serialsNumber);
        int fpgaCh=TChan.toFpgaChNo(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh);
        setControlColorByChIdx(tChan);
        if (serialChannel == null) return;
        uartBus = (UartBus) serialChannel.getBus(IBus.UART);
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
    private void setControlColorByChIdx(int chIdx) {
        vRx.setControlColorByChIdx(chIdx);
        vIdle.setControlColorByChIdx(chIdx);
        vCheck.setControlColorByChIdx(chIdx);
        vBits.setControlColorByChIdx(chIdx);
        vDisplay.setControlColorByChIdx(chIdx);
        vBaudRate.setControlColorByChIdx(chIdx);
    }

    public RightMsgSerialsUart getMsgDetailsUart() {
        return msgDetailsUart;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsUart.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsUart, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERUART_SOURCE: {
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vRx.getSelectIndex()) {
                        vRx.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vRx.getId(), vRx.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_IDLE: {
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vIdle.getSelectIndex()) {
                        vIdle.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_CHECK: {
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vCheck.getSelectIndex()) {
                        vCheck.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_BITS: {
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vBits.getSelectIndex()) {
                        vBits.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_BAUDRATE: {
//                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
//                    String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1]));
//                    if (Integer.parseInt(params[0]) == serialsNumber - 1
//                            && !tvUserDefine.getText().toString().equals(baudRate)) {
//                        onTextChanged(tvUserDefine.getId(), baudRate, false);
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
                case CommandMsgToUI.FLAG_TRIGGERUART_DISPLAY: {
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vDisplay.getSelectIndex()) {
                        vDisplay.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_Bus_Uart_Rx:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int rx = Integer.parseInt(params[1]);
                    if (sNum == serialsNumber - 1) {
                        vRx.setSelectIndex(rx);
                        onCheckChanged(vRx.getId(), vRx.getSelectItem(), false);
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Uart_IdLevel:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int idle = Integer.parseInt(params[1]);
                    if (sNum==serialsNumber-1) {
                        vIdle.setSelectIndex(idle);
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false);
                    }
                }break;

                case CommandMsgToUI.FLAG_Bus_Uart_BaudRate: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int rateIndex = Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vBaudRate.setSelectIndex(rateIndex);
                    tvUserDefine.setText("");
                    onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), false);
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Uart_UserBaud: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    float rate = Integer.parseInt(params[1]) / 1000.0f;
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vBaudRate.clearSelect();
                    onTextChanged(tvUserDefine.getId(), rate + "kb/s", false);
                }
                break;

                case CommandMsgToUI.FLAG_Bus_Uart_Check:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int check = Integer.parseInt(params[1]);
                    if (sNum==serialsNumber-1) {
                        vCheck.setSelectIndex(check);
                        onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), false);
                    }
                }break;

                case CommandMsgToUI.FLAG_Bus_Uart_Width:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int width = Integer.parseInt(params[1]);
                    if (sNum==serialsNumber-1) {
                        vBits.setSelectIndex(width);
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false);
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Uart_Display:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum = Integer.parseInt(params[0]);
                    int display = Integer.parseInt(params[1]);
                    if (sNum==serialsNumber-1) {
                        vDisplay.setSelectIndex(display);
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false);
                    }
                }break;

            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (uartBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && uartBus.equals(eventBase.getData())) {
                if (uartBus.getRxChIdx() != vRx.getSelectItem().getIndex()) {
                    vRx.setSelectIndex(uartBus.getRxChIdx());
                    onCheckChanged(vRx.getId(), vRx.getSelectItem(), true);
                }
                if (uartBus.getIdleLevel() != vIdle.getSelectItem().getIndex()) {
                    vIdle.setSelectIndex(uartBus.getIdleLevel());
                    onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), true);
                }
                int verify = uartBus.getVerify();
                verify = verify == UartBus.UART_EVEN_VERIFY ? 2 : verify;
                if (verify != vCheck.getSelectItem().getIndex()) {
                    vCheck.setSelectIndex(verify);
                    onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), true);
                }
                String bit = String.valueOf(uartBus.getBits()) + "bit";
                if (!vBits.getSelectItem().getText().equals(bit)) {
                    if (vBits.setSelectText(bit)) {
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), true);
                    }
                }
                String baudRate = TBookUtil.getBaudRateFromInt(uartBus.getBaudRate());
                if (!vBaudRate.getSelectItem().getText().equals(baudRate) || !tvUserDefine.getText().toString().equals(baudRate)) {
                    if (vBaudRate.setSelectText(baudRate)) {
                        tvUserDefine.setText("");
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                    } else {
                        vBaudRate.clearSelect();
                        tvUserDefine.setText(baudRate);
                        vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1);
                        onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true);
                    }
                }
                int display = getDisplayFromScope(uartBus.getDisplayFormat());
                if (vDisplay.getSelectItem().getIndex() != display) {
                    vDisplay.setSelectIndex(display);
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true);
                }
            }
        }
    };


    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton();
            String s = tvUserDefine.getText();
            if (dialogKeyBoard == null)
                dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
            if (!StrUtil.isEmpty(s)) {
                String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
                double preDouble = Double.parseDouble(number);
                String preBs = s.replace(number, "");
                dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onDismissListener);
            } else {
                dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS, 1200, 8 * 1000 * 1000, onDismissListener);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvUserDefine.getId(), result, false);
        }
    };

    private void debugSerial(){
        int tChan=TChan.S1;
        int fpgaChan=TChan.toFpgaChNo(tChan);
        SerialChannel sNum= ChannelFactory.getSerialChannel(fpgaChan);
        if (sNum == null) return;
        boolean isOpen=sNum.isOpen();
        int busType= sNum.getBusType();
        switch (busType){
            case IBus.UART:{
                UartBus bus= (UartBus) sNum.getBus(busType);
                int ch= bus.getRxChIdx();
                int level=bus.getIdleLevel();
                int check=bus.getVerify();
                int bits=bus.getBits();
                int baudrate=bus.getBaudRate();
                int display=bus.getDisplayFormat();
                int triggerType= bus.getTriggerType();
                Channel chan= ChannelFactory.getDynamicChannel(ch);
                if (chan==null) return;
                double discreetS1 = chan.getBusPrimaryLevel();
                double discreetS2 = chan.getBusSecondaryLevel();
//                Log.d(Tag.Debug,
//                        String.format("RightLayoutSerials.debugSerial: isOpen:%b ,type:%d, rx:%d,level:%d,check:%d,bits:%d,baudrate:%d,display:%d,triggerType:%d, level1:%f,level2:%f ",
//                                isOpen,busType,ch,level,check,bits,baudrate,display,triggerType,discreetS1,discreetS2));
            }break;
        }
        int trigType= TriggerFactory.getTriggerType();
        //Log.d(Tag.Debug, String.format("RightLayoutSerialsUart.debugSerial: %d",trigType ));


    }
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            onCheckChanged(viewId, item, false);
            debugSerial();
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vRx.getId()) {
            if (!isFromEventBus) {
                uartBus.setRxChIdx(item.getIndex());
            }
            Command.get().getTrigger_uart().setSource(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_uart().Rx(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsUart.setRx(vRx.getSelectItem());
            sendMsg(isFromEventBus);
        } else if (viewId == vIdle.getId()) {
            if (!isFromEventBus) {
                uartBus.setIdleLevel(item.getIndex());
            }
            Command.get().getTrigger_uart().setIdle(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_uart().IdLevel(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_IDLE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsUart.setIdleLevel(vIdle.getSelectItem());
            sendMsg(isFromEventBus);
        } else if (viewId == vCheck.getId()) {
            if (!isFromEventBus) {
                if (item.getIndex() == 2) {
                    uartBus.setVerify(UartBus.UART_EVEN_VERIFY);
                } else {
                    uartBus.setVerify(item.getIndex());
                }
            }
            Command.get().getTrigger_uart().setCheck(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_uart().Check(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + serialsNumber, String.valueOf(item.getIndex()));
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartChecked(item.getIndex() == 0);
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartChecked(item.getIndex() == 0);
//            }
            int ch=TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(ch).setUartChecked(item.getIndex() == 0);

            //SerialBusStructParse.get().uartSettingStruct.setChecked( item.getIndex()==0);
            msgDetailsUart.setCheck(vCheck.getSelectItem());
            sendMsg(isFromEventBus);
        } else if (viewId == vBits.getId()) {
            if (!isFromEventBus) {
                uartBus.setBits(Integer.parseInt(item.getText().replace("bit", "")));
            }
            Command.get().getTrigger_uart().setBits(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_uart().Width(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + serialsNumber, String.valueOf(item.getIndex()));
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartBits(item.getIndex() + 5);
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartBits(item.getIndex() + 5);
//            }
            int ch=TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(ch).setUartBits(item.getIndex() + 5);
            //SerialBusStructParse.get().uartSettingStruct.setUartLength(item.getIndex()+5);
            msgDetailsUart.setBits(vBits.getSelectItem());
            sendMsg(isFromEventBus);
        } else if (viewId == vDisplay.getId()) {
            if (!isFromEventBus) {
                uartBus.setDisplayFormat(getDisplayToScope(item.getIndex()));
            }
            Command.get().getTrigger_uart().setDisplay(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_uart().Display(serialsNumber-1,item.getIndex(),false);

            int preDigits;
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber) == 1) {
                preDigits = IDigits.DIGITS_2;
            } else {
                preDigits = IDigits.DIGITS_16;
            }
            setTopSerialsUartData(preDigits, item.getIndex() == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber, String.valueOf(item.getIndex()));
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartEncoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartEncoding(item.getIndex());
//            }
            int ch=TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(ch).setUartEncoding(item.getIndex());
            //SerialBusStructParse.get().uartSettingStruct.setEncoding(item.getIndex());
            msgDetailsUart.setDisplay(vDisplay.getSelectItem());
            sendMsg(isFromEventBus);
        }  else if (viewId == vBaudRate.getId()) {
            Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_uart().BaudRate(serialsNumber-1,item.getIndex(),false);
            Command.get().getBus_uart().UserBaud(serialsNumber-1,-1,false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber, String.valueOf(item.getIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber, "");
            tvUserDefine.setText("");
            if (!isFromEventBus) {
                int baudRate = getBaudRateValueToScope();
                uartBus.setBaudRate(baudRate);
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
            }
            msgDetailsUart.setBaudRate(item);
            sendMsg(isFromEventBus);
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_UART);
//            }
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void setTopSerialsUartData(int preDigits, int nextDigits) {
        int bits = Integer.parseInt(vBits.getSelectItem().getText().replace("bit", ""));
        String key;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + serialsNumber;
            } else if (i == 1) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + serialsNumber;
            } else if (i == 2) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + serialsNumber;
            } else {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + serialsNumber;
            }
            String edit = CacheUtil.get().getString(key);
            edit = SerialsUtils.HexBin(edit, preDigits, nextDigits);
            edit = SerialsUtils.reCalcSpace(edit, nextDigits == IDigits.DIGITS_16 ? 2 : bits, nextDigits);
            CacheUtil.get().putMap(key, edit);
        }
    }

    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) {
//            tvUserDefine.setText(result);
//            if (!isFromEventBus) {
//                int baudRate = TBookUtil.getIntFromBaudRate(result);
//                uartBus.setBaudRate(baudRate);
//                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
//            }
//            Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
//            Command.get().getBus_uart().BaudRate(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false);
//            Command.get().getBus_uart().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false);
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber, result);
//            msgDetailsUart.setBaudRate(result);
//            sendMsg(isFromEventBus);
//            SerialBusManage.getInstance().clearSerialBusTxtBuffer();

            String unit = "";
            if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_MBS)) {
                unit = TopDialogNumberKeyBoard.KEYBOARD_MBS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "");
            } else if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_KBS)) {
                unit = TopDialogNumberKeyBoard.KEYBOARD_KBS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "");
            } else {
                unit = TopDialogNumberKeyBoard.KEYBOARD_BS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
            }

            if (result.length() > 4 && result.substring(0, 4).contains(".")) {
                result = result.substring(0, 5);
            } else if (result.length() > 4) {
                result = result.substring(0, 4);
            }
            result = result + unit;
            vBaudRate.clearSelect();
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) {
                if (!isFromEventBus) {
                    int baudRate = TBookUtil.getIntFromBaudRate(result);
                    uartBus.setBaudRate(baudRate);
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
                }
                Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
                Command.get().getBus_uart().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
                Command.get().getBus_uart().BaudRate(serialsNumber - 1, -1, false);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber, result);
                tvUserDefine.setText(result);
                msgDetailsUart.setBaudRateDefine(result);
                sendMsg(isFromEventBus);
                SerialBusManage.getInstance().clearSerialBusTxtBuffer();
            }
        }
    }
}
