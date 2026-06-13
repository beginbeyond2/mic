package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Bus.ARINC429Bus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsM429 extends RelativeLayout {
    private Context context;
    private RightViewSelect vSource, vFormat, vDisplay, vBaudRate;

    private RightMsgSerialsM429 msgDetailsM429;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private ARINC429Bus a429Bus;

    public RightLayoutSerialsM429(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsM429(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsM429(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_m429, this);
        vSource = (RightViewSelect) findViewById(R.id.source);
        vFormat = (RightViewSelect) findViewById(R.id.format);
        vDisplay = (RightViewSelect) findViewById(R.id.display);
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate);

        vSource.setArray(GlobalVar.get().getChannelsName());

        vSource.setOnItemClickListener(onItemClickListener);
        vFormat.setOnItemClickListener(onItemClickListener);
        vDisplay.setOnItemClickListener(onItemClickListener);
        vBaudRate.setOnItemClickListener(onItemClickListener);

        initData();
    }

    private void initData() {
        msgDetailsM429 = new RightMsgSerialsM429();
        msgDetailsM429.setSource(vSource.getSelectItem());
        msgDetailsM429.setFormat(vFormat.getSelectItem());
        msgDetailsM429.setDisplay(vDisplay.getSelectItem());
        msgDetailsM429.setBaudRate(vBaudRate.getSelectItem());
    }

    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber);
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber);
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber);
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_BAUDRATE + serialsNumber);
        vSource.setSelectIndex(source);
        vFormat.setSelectIndex(format);
        vDisplay.setSelectIndex(display);
        vBaudRate.setSelectIndex(baudRate);

        Command.get().getTrigger_m429().setSource(serialsNumber - 1, source, false);
        Command.get().getTrigger_m429().setFormat(serialsNumber - 1, format, false);
        Command.get().getTrigger_m429().setDisplay(serialsNumber - 1, display, false);
        Command.get().getTrigger_m429().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()), false);

        Command.get().getBus_429().Source(serialsNumber-1,source,false);
        Command.get().getBus_429().Format(serialsNumber-1,format,false);
        Command.get().getBus_429().Display(serialsNumber-1,display,false);
        Command.get().getBus_429().BaudRate(serialsNumber-1,baudRate,false);

        a429Bus.setSrcChIdx(source);
        a429Bus.setFormat(getFormatValueToScope(format));
        a429Bus.setDisplayFormat(display);
        a429Bus.setBaudRate(TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()));

//        if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//            SerialBusManage.getInstance().getSerialBus(TChan.S1).set429Encoding(display);
//        } else {
//            SerialBusManage.getInstance().getSerialBus(TChan.S2).set429Encoding(display);
//        }
        int chIdx=TChan.toSerialTChan(serialsNumber);
        SerialBusManage.getInstance().getSerialBus(chIdx).set429Encoding(display);

        msgDetailsM429.setSource(vSource.getSelectItem());
        msgDetailsM429.setFormat(vFormat.getSelectItem());
        msgDetailsM429.setDisplay(vDisplay.getSelectItem());
        msgDetailsM429.setBaudRate(vBaudRate.getSelectItem());
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.M429) {
            sendMsg(false);
        }
    }

    private int getFormatValueToScope(int format) {
        if (format == 0) {
            return ARINC429Bus.ARINC429_LABEL_DATA;
        } else if (format == 1) {
            return ARINC429Bus.ARINC429_LABEL_DATA_SSM;
        } else {
            return ARINC429Bus.ARINC429_LABEL_SDI_DATA_SSM;
        }
    }

    private int getFormatValueFromScope(int format) {
        if (format == ARINC429Bus.ARINC429_LABEL_DATA) {
            return 0;
        } else if (format == ARINC429Bus.ARINC429_LABEL_DATA_SSM) {
            return 1;
        } else {
            return 2;
        }
    }


    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
        int tChan=TChan.toSerialTChan(serialsNumber);
        int fpgaChan=TChan.toFpgaChNo(tChan);
        setControlColorByChIdx(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
    }
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx);
        vFormat.setControlColorByChIdx(chIdx);
        vDisplay.setControlColorByChIdx(chIdx);
        vBaudRate.setControlColorByChIdx(chIdx);
    }
    public RightMsgSerialsM429 getMsgDetailsM429() {
        return msgDetailsM429;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsM429.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsM429, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERM429_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) {
                        vSource.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_FORMAT: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) {
                        vFormat.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vFormat.getId(), vFormat.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_DISPLAY: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) {
                        vDisplay.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_BAUDRATE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) {
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1]));
                        if (!vBaudRate.getSelectItem().getText().equals(baudRate)) {
                            if (vBaudRate.setSelectText(baudRate)) {
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_429_Channel:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String ch=params[0];
                    int source=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vSource.setSelectIndex(source);
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_429_Format:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String ch=params[0];
                    int format=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vFormat.setSelectIndex(format);
                    onCheckChanged(vFormat.getId(),vFormat.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_429_display:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String ch=params[0];
                    int display=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vDisplay.setSelectIndex(display);
                    onCheckChanged(vDisplay.getId(),vDisplay.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_429_Baudrate:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String ch=params[0];
                    int baudrate=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vBaudRate.setSelectIndex(baudrate);
                    onCheckChanged(vBaudRate.getId(),vBaudRate.getSelectItem(),false);
                }break;

            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (a429Bus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && a429Bus.equals(eventBase.getData())) {
                if (a429Bus.getSrcChIdx() != vSource.getSelectIndex()) {
                    vSource.setSelectIndex(a429Bus.getSrcChIdx());
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true);
                }
                int format = getFormatValueFromScope(a429Bus.getFormat());
                if (format != vFormat.getSelectIndex()) {
                    vFormat.setSelectIndex(format);
                    onCheckChanged(vFormat.getId(), vFormat.getSelectItem(), true);
                }
                if (a429Bus.getDisplayFormat() != vDisplay.getSelectIndex()) {
                    vDisplay.setSelectIndex(a429Bus.getDisplayFormat());
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true);
                }
                String baudRate = TBookUtil.getBaudRateFromInt(a429Bus.getBaudRate());
                if (!vBaudRate.getSelectItem().getText().equals(baudRate)) {
                    if (vBaudRate.setSelectText(baudRate)) {
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true);
                    }
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
                a429Bus.setSrcChIdx(vSource.getSelectIndex());
            }
            Command.get().getTrigger_m429().setSource(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_429().Source(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsM429.setSource(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vFormat.getId()) {
            if (!isFromEventBus) {
                a429Bus.setFormat(getFormatValueToScope(vFormat.getSelectIndex()));
            }
            Command.get().getTrigger_m429().setFormat(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_429().Format(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsM429.setFormat(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vDisplay.getId()) {
            if (!isFromEventBus) {
                a429Bus.setDisplayFormat(vDisplay.getSelectIndex());
            }
            Command.get().getTrigger_m429().setDisplay(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_429().Display(serialsNumber-1,item.getIndex(),false);
            int preDigits;
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber) == 0) {
                preDigits = IDigits.DIGITS_2;
            } else {
                preDigits = IDigits.DIGITS_16;
            }
            setTopSerialsM429Data(preDigits, item.getIndex() == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsM429.setDisplay(item);
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).set429Encoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).set429Encoding(item.getIndex());
//            }
            int chIdx=TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(chIdx).set429Encoding(item.getIndex());

            sendMsg(isFromEventBus);
        } else if (viewId == vBaudRate.getId()) {
            if (!isFromEventBus) {
                int baudRate = TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText());
                a429Bus.setBaudRate(baudRate);
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
            }



            Command.get().getTrigger_m429().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_429().BaudRate(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_BAUDRATE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsM429.setBaudRate(item);
            sendMsg(isFromEventBus);
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void setTopSerialsM429Data(int preDigits, int nextDigits) {
        String key;
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + serialsNumber;
            } else {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + serialsNumber;
            }
            String s = "setTopSerialsM429Data:";
            String data = CacheUtil.get().getString(key);
            s += data;
            data = SerialsUtils.HexBin(data, preDigits, nextDigits);
            s += ("," + data);
            data = SerialsUtils.reCalcSpace(data, nextDigits == IDigits.DIGITS_2 ? 23 : 6, nextDigits);
            s += ("," + data);
            CacheUtil.get().putMap(key, data);
            Logger.d("setTopSerialsM429Data:"+s);
        }
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}
