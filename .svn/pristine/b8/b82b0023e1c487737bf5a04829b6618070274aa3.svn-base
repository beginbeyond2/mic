package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
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
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsM1553B extends RelativeLayout {
    private Context context;
    private RightViewSelect vSource, vDisplay;

    private RightMsgSerialsM1553b msgDetailsM1553B;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private MILSTD1553BBus m1553bBus;

    public RightLayoutSerialsM1553B(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsM1553B(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsM1553B(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_m1553b, this);
        vSource = (RightViewSelect) findViewById(R.id.source);
        vDisplay = (RightViewSelect) findViewById(R.id.display);

        vSource.setArray(GlobalVar.get().getChannelsName());

        vSource.setOnItemClickListener(onItemClickListener);
        vDisplay.setOnItemClickListener(onItemClickListener);

        initData();
    }

    private void initData() {
        msgDetailsM1553B = new RightMsgSerialsM1553b();
        msgDetailsM1553B.setSource(vSource.getSelectItem());
        msgDetailsM1553B.setDisplay(vDisplay.getSelectItem());
    }

    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + serialsNumber);
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber);
        vSource.setSelectIndex(source);
        vDisplay.setSelectIndex(display);

        Command.get().getTrigger_m1553B().setSource(serialsNumber - 1, source, false);
        Command.get().getTrigger_m1553B().setDisplay(serialsNumber - 1, display, false);

        Command.get().getBus_1553B().Channel(serialsNumber-1,source,false);
        Command.get().getBus_1553B().Display(serialsNumber-1,display,false);

        m1553bBus.setSrcChIdx(source);
        m1553bBus.setDisplayFormat(display);

//        if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//            SerialBusManage.getInstance().getSerialBus(IWave.S1).set1553bEncoding(display);
//        } else {
//            SerialBusManage.getInstance().getSerialBus(IWave.S2).set1553bEncoding(display);
//        }
        int ch= TChan.toSerialTChan(serialsNumber);
        SerialBusManage.getInstance().getSerialBus(ch).set1553bEncoding(display);

        msgDetailsM1553B.setSource(vSource.getSelectItem());
        msgDetailsM1553B.setDisplay(vDisplay.getSelectItem());
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.M1553B) {
            sendMsg(false);
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
        m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
    }
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx);
        vDisplay.setControlColorByChIdx(chIdx);
    }
    public RightMsgSerialsM1553b getMsgDetailsM1553B() {
        return msgDetailsM1553B;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsM1553B.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsM1553B, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERM1553B_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vSource.getSelectIndex()) {
                        vSource.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM1553B_DISPLAY: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && Integer.parseInt(params[1]) != vDisplay.getSelectIndex()) {
                        vDisplay.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_Bus_1553B_Channel:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String sNum=params[0];
                    int ch=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vSource.setSelectIndex(ch);
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_1553B_Display:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    String sNum=params[0];
                    int ch=Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return;
                    vDisplay.setSelectIndex(ch);
                    onCheckChanged(vDisplay.getId(),vDisplay.getSelectItem(),false);
                }break;
            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (m1553bBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && m1553bBus.equals(eventBase.getData())) {
                if (m1553bBus.getSrcChIdx() != vSource.getSelectIndex()) {
                    vSource.setSelectIndex(m1553bBus.getSrcChIdx());
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true);
                }
                if (m1553bBus.getDisplayFormat() != vDisplay.getSelectIndex()) {
                    vDisplay.setSelectIndex(m1553bBus.getDisplayFormat());
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true);
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
                m1553bBus.setSrcChIdx(vSource.getSelectIndex());
            }
            Command.get().getTrigger_m1553B().setSource(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_1553B().Channel(serialsNumber-1,item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsM1553B.setSource(item);
            sendMsg(isFromEventBus);
        } else if (viewId == vDisplay.getId()) {
            if (!isFromEventBus) {
                m1553bBus.setDisplayFormat(vDisplay.getSelectIndex());
            }
            Command.get().getTrigger_m1553B().setDisplay(serialsNumber - 1, item.getIndex(), false);
            Command.get().getBus_1553B().Display(serialsNumber-1,item.getIndex(),false);
            int preDigits;
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber) == 0) {
                preDigits = IDigits.DIGITS_2;
            } else {
                preDigits = IDigits.DIGITS_16;
            }
            setTopSerialsM1553bData(preDigits, item.getIndex() == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber, String.valueOf(item.getIndex()));
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).set1553bEncoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).set1553bEncoding(item.getIndex());
//            }
            int ch=TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(ch).set1553bEncoding(item.getIndex());

            msgDetailsM1553B.setDisplay(item);
            sendMsg(isFromEventBus);
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void setTopSerialsM1553bData(int preDigits, int nextDigits) {
        String key;
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + serialsNumber;
            } else if (i == 1) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + serialsNumber;
            } else {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + serialsNumber;
            }
            String edit = CacheUtil.get().getString(key);
            edit = SerialsUtils.HexBin(edit, preDigits, nextDigits);
            edit = SerialsUtils.reCalcSpace(edit, nextDigits == IDigits.DIGITS_16 ? 4 : 16, nextDigits);
            CacheUtil.get().putMap(key, edit);
        }
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}
