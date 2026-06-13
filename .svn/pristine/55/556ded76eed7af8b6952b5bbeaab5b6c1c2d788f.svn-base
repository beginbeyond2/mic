package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.SpiBus;
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
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsSpi extends LinearLayout {
    private Context context;
    private RightViewSelect vClk, vData, vCs, vBits;
    private RightViewSelect rgClkCheck, rgDataCheck, rgCsCheck;
    private MSwitchBox csSwitch;

    private RightMsgSerialsSpi msgDetailsSpi;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private SpiBus spiBus;

    public RightLayoutSerialsSpi(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsSpi(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsSpi(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_spi, this);
        setOrientation(VERTICAL);
        vClk = (RightViewSelect) findViewById(R.id.clk);
        vData = (RightViewSelect) findViewById(R.id.data);
        vCs = (RightViewSelect) findViewById(R.id.cs);
        vBits = (RightViewSelect) findViewById(R.id.bit);
        rgClkCheck = (RightViewSelect) findViewById(R.id.clkCheck);
        rgDataCheck = (RightViewSelect) findViewById(R.id.dataCheck);
        rgCsCheck = (RightViewSelect) findViewById(R.id.csCheck);
        csSwitch = (MSwitchBox) findViewById(R.id.csSwith);
        RelativeLayout csLayout = (RelativeLayout) findViewById(R.id.csLayout);

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            csLayout.setVisibility(GONE);
        } else {
            csLayout.setVisibility(VISIBLE);
        }
        String[] channels = GlobalVar.get().getChannelsName();
        vClk.setArray(channels);
        vData.setArray(channels);
        vCs.setArray(channels);

        vClk.setOnItemClickListener(onItemClickListener);
        vData.setOnItemClickListener(onItemClickListener);
        vCs.setOnItemClickListener(onItemClickListener);
        vBits.setOnItemClickListener(onItemClickListener);
        rgClkCheck.setOnItemClickListener(onItemClickListener);
        rgDataCheck.setOnItemClickListener(onItemClickListener);
        rgCsCheck.setOnItemClickListener(onItemClickListener);
        csSwitch.setOnToggleStateChangedListener(onToggleStateChangedListener);

        initData();
    }

    private void initData() {
        msgDetailsSpi = new RightMsgSerialsSpi();
        msgDetailsSpi.setClk(vClk.getSelectItem());
        vData.setSelectIndex(1);
        msgDetailsSpi.setData(vData.getSelectItem());
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            vCs.setSelectIndex(0);
        } else {
            vCs.setSelectIndex(2);
        }
        msgDetailsSpi.setCs(vCs.getSelectItem());
        msgDetailsSpi.setBit(vBits.getSelectItem());
        msgDetailsSpi.setClkLow(false);
        msgDetailsSpi.setDataLow(false);
        msgDetailsSpi.setCsLow(false);
        msgDetailsSpi.setCsSwitch(false);
    }

    void setCache() {
        int clk = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber);
        int data = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber);
        int cs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber);
        int bit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_BIT + serialsNumber);
        boolean clkCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLKCHECK + serialsNumber);
        boolean dataCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATACHECK + serialsNumber);
        boolean csCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSCHECK + serialsNumber);
        boolean csSwitch = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber);
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2 && cs >= 2) {
            cs = 0;
        }
        vClk.setSelectIndex(clk);
        vData.setSelectIndex(data);
        vCs.setSelectIndex(cs);
        vBits.setSelectIndex(bit);
        setRadioGroupCheck(rgClkCheck, clkCheck);
        setRadioGroupCheck(rgDataCheck, dataCheck);
        setRadioGroupCheck(rgCsCheck, csCheck);
        this.csSwitch.setState(csSwitch);
        vCs.setEnabled(csSwitch);
        rgCsCheck.setEnabled(csSwitch);

        Command.get().getBus_spi().setClock(serialsNumber - 1, clk, false);
        Command.get().getBus_spi().setClockSwitch(serialsNumber - 1, clkCheck ? 1 : 0, false);
        Command.get().getBus_spi().setData(serialsNumber - 1, data, false);
        Command.get().getBus_spi().setDataSwitch(serialsNumber - 1, dataCheck ? 1 : 0, false);
        Command.get().getBus_spi().setCs(serialsNumber - 1, cs, false);
        Command.get().getBus_spi().setCsSwitch(serialsNumber - 1, csCheck ? 1 : 0, false);
        Command.get().getBus_spi().setCsEnable(serialsNumber - 1, csSwitch , false);
        Command.get().getBus_spi().setBits(serialsNumber - 1,
                Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", "")), false);



        spiBus.setClkChIdx(clk);
        spiBus.setClkSample(clkCheck ? SpiBus.SPI_CLK_FALL_EDGE : SpiBus.SPI_CLK_RISE_EDGE);
        spiBus.setDataChIdx(data);
        spiBus.setDataIdleLevel(dataCheck ? SpiBus.IDLE_LEVEL_LOW : SpiBus.IDLE_LEVEL_HIGH);
        spiBus.setCsChIdx(cs);
        spiBus.setCsIdleLevel(csCheck ? SpiBus.IDLE_LEVEL_LOW : SpiBus.IDLE_LEVEL_HIGH);
        spiBus.setCsValid(csSwitch);
        spiBus.setBits(Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", "")));

        int ch=TChan.toSerialTChan(serialsNumber);
        SerialBusManage.getInstance().getSerialBus(ch).setSpiBits(bit);

        msgDetailsSpi.setClk(this.vClk.getSelectItem());
        msgDetailsSpi.setData(this.vData.getSelectItem());
        msgDetailsSpi.setCs(this.vCs.getSelectItem());
        msgDetailsSpi.setBit(this.vBits.getSelectItem());
        msgDetailsSpi.setClkLow(clkCheck);
        msgDetailsSpi.setDataLow(dataCheck);
        msgDetailsSpi.setCsLow(csCheck);
        msgDetailsSpi.setCsSwitch(csSwitch);
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.SPI) {
            sendMsg(false);
        }
    }

    private void setRadioGroupCheck(RightViewSelect viewSelect, boolean check) {
        int number = check ? 1 : 0;
        viewSelect.setSelectIndex(number);
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
        spiBus = (SpiBus) serialChannel.getBus(IBus.SPI);
    }
    private void setControlColorByChIdx(int chIdx) {
        vClk.setControlColorByChIdx(chIdx);
        vData.setControlColorByChIdx(chIdx);
        vCs.setControlColorByChIdx(chIdx);
        vBits.setControlColorByChIdx(chIdx);
        rgClkCheck.setControlColorByChIdx(chIdx);
        rgDataCheck.setControlColorByChIdx(chIdx);
        rgCsCheck.setControlColorByChIdx(chIdx);
    }

    public RightMsgSerialsSpi getMsgDetailsSpi() {
        return msgDetailsSpi;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsSpi.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsSpi, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERSPI_CLOCK: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& Integer.parseInt(params[1]) != vClk.getSelectIndex()*/) {
                        vClk.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vClk.getId(), vClk.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CLOCKSWITCH: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int clkIndex = Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& clkIndex != rgClkCheck.getSelectIndex()*/) {
                        rgClkCheck.setSelectIndex(clkIndex);
                        onCheckChanged(rgClkCheck.getId(), rgClkCheck.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_DATA: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& Integer.parseInt(params[1]) != vData.getSelectIndex()*/) {
                        vData.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vData.getId(), vData.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_DATASWITCH: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int dataIndex = Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& dataIndex != rgDataCheck.getSelectIndex()*/) {
                        rgDataCheck.setSelectIndex(dataIndex);
                        onCheckChanged(rgDataCheck.getId(), rgDataCheck.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CS: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& Integer.parseInt(params[1]) != vCs.getSelectIndex()*/) {
                        vCs.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vCs.getId(), vCs.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CSWITCH: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int csIndex = Integer.parseInt(params[1]);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                           /* && csIndex != rgCsCheck.getSelectIndex()*/) {
                        rgCsCheck.setSelectIndex(csIndex);
                        onCheckChanged(rgCsCheck.getId(), rgCsCheck.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CSENABLE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    boolean b=Boolean.parseBoolean(params[1]);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) {
                        csSwitch.setState(b);
                        onToggleStateChangedListener.onToggleStateChanged(csSwitch, csSwitch.isState());
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_BITS: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            /*&& Integer.parseInt(params[1]) != vBits.getSelectIndex()*/) {
                        vBits.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false);
                    }
                    break;
                }
            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (spiBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && spiBus.equals(eventBase.getData())) {
                if (spiBus.getClkChIdx() != vClk.getSelectIndex()) {
                    vClk.setSelectIndex(spiBus.getClkChIdx());
                    onCheckChanged(vClk.getId(), vClk.getSelectItem(), true);
                }
                boolean clkCheck = spiBus.getClkSample() == SpiBus.SPI_CLK_FALL_EDGE;
                if (rgClkCheck.getSelectIndex() != spiBus.getClkSample()) {
                    setRadioGroupCheck(rgClkCheck, clkCheck);
                    onCheckChanged(rgClkCheck.getId(), rgClkCheck.getSelectItem(), true);
                }
                if (spiBus.getDataChIdx() != vData.getSelectIndex()) {
                    vData.setSelectIndex(spiBus.getDataChIdx());
                    onCheckChanged(vData.getId(), vData.getSelectItem(), true);
                }
                boolean dataCheck = spiBus.getDataIdleLevel() == SpiBus.IDLE_LEVEL_LOW;
                if (rgDataCheck.getSelectIndex() != spiBus.getDataIdleLevel()) {
                    setRadioGroupCheck(rgDataCheck, dataCheck);
                    onCheckChanged(rgDataCheck.getId(), rgDataCheck.getSelectItem(), true);
                }
                if (spiBus.getCsChIdx() != vCs.getSelectIndex()) {
                    vCs.setSelectIndex(spiBus.getCsChIdx());
                    onCheckChanged(vCs.getId(), vCs.getSelectItem(), true);
                }
                boolean csCheck = spiBus.getCsIdleLevel() == SpiBus.IDLE_LEVEL_LOW;
                if (rgCsCheck.getSelectIndex() != spiBus.getCsIdleLevel()) {
                    setRadioGroupCheck(rgCsCheck, csCheck);
                    onCheckChanged(rgCsCheck.getId(), rgCsCheck.getSelectItem(), true);
                }
                if (csSwitch.isState() != spiBus.isCsValid()) {
                    csSwitch.setState(spiBus.isCsValid());
                    onSwitchChanged(csSwitch.getId(), csSwitch.isState(), true);
                }
                String bits = spiBus.getBits() + "bit";
                if (!vBits.getSelectItem().getText().equals(bits)) {
                    if (vBits.setSelectText(bits)) {
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), true);
                    }
                }
            }
        }
    };

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            onSwitchChanged(csSwitch.getId(), state, false);
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
        if (viewId == vClk.getId()) {
            if (!isFromEventBus) {
                spiBus.setClkChIdx(item.getIndex());
            }

            msgDetailsSpi.setClk(item);
            if (vData.getSelectIndex() == item.getIndex()) {
                int index = vData.getSelectIndex();
                vData.setSelectIndex(index == vData.getSelectCount() - 1 ? 0 : index + 1);
                if (!isFromEventBus) {
                    spiBus.setDataChIdx(vData.getSelectIndex());
                }
            }
            if (csSwitch.isState()) {
                if (vCs.getSelectIndex() == item.getIndex()) {
                    int index = vCs.getSelectIndex();
                    int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1;
                    if (index2 != vData.getSelectIndex()) {
                        vCs.setSelectIndex(index2);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    } else {
                        int index3 = ((index2 == vCs.getSelectCount() - 1) ? 0 : index2 + 1);
                        vCs.setSelectIndex(index3);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    }
                } else {
                    if (vCs.getSelectIndex() == vData.getSelectIndex()) {
                        int index = vCs.getSelectIndex();
                        int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1;
                        vCs.setSelectIndex(index2);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex()));
            msgDetailsSpi.setData(vData.getSelectItem());
            msgDetailsSpi.setCs(vCs.getSelectItem());
            msgDetailsSpi.setClk(vClk.getSelectItem());
            sendMsg(isFromEventBus);
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false);
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false);
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false);
        } else if (viewId == vData.getId()) {
            if (!isFromEventBus) {
                spiBus.setDataChIdx(item.getIndex());
            }

            msgDetailsSpi.setData(item);
            if (vClk.getSelectIndex() == item.getIndex()) {
                int index = vClk.getSelectIndex();
                vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1);
                if (!isFromEventBus) {
                    spiBus.setClkChIdx(vClk.getSelectIndex());
                }
            }
            if (csSwitch.isState()) {
                if (vCs.getSelectIndex() == item.getIndex()) {
                    int index = vCs.getSelectIndex();
                    int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1;
                    if (index2 != vClk.getSelectIndex()) {
                        vCs.setSelectIndex(index2);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    } else {
                        int index3 = ((index2 == vCs.getSelectCount() - 1) ? 0 : index2 + 1);
                        vCs.setSelectIndex(index3);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    }
                } else {
                    if (vCs.getSelectIndex() == vClk.getSelectIndex()) {
                        int index = vCs.getSelectIndex();
                        int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1;
                        vCs.setSelectIndex(index2);
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex());
                        }
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex()));
            msgDetailsSpi.setClk(vClk.getSelectItem());
            msgDetailsSpi.setCs(vCs.getSelectItem());
            msgDetailsSpi.setData(vData.getSelectItem());
            sendMsg(isFromEventBus);
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false);
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false);
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false);
        } else if (viewId == vCs.getId()) {
            if (!isFromEventBus) {
                spiBus.setCsChIdx(item.getIndex());
            }

            msgDetailsSpi.setCs(item);
            if (vClk.getSelectIndex() == item.getIndex()) {
                int index = vClk.getSelectIndex();
                vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1);
                if (!isFromEventBus) {
                    spiBus.setClkChIdx(vClk.getSelectIndex());
                }
            }
            if (vData.getSelectIndex() == item.getIndex()) {
                int index = vData.getSelectIndex();
                int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1;
                if (index2 != vClk.getSelectIndex()) {
                    vData.setSelectIndex(index2);
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex());
                    }
                } else {
                    int index3 = ((index2 == vData.getSelectCount() - 1) ? 0 : index2 + 1);
                    vData.setSelectIndex(index3);
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex());
                    }
                }
            } else {
                if (vData.getSelectIndex() == vClk.getSelectIndex()) {
                    int index = vData.getSelectIndex();
                    int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1;
                    vData.setSelectIndex(index2);
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex());
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex()));
            msgDetailsSpi.setClk(vClk.getSelectItem());
            msgDetailsSpi.setData(vData.getSelectItem());
            msgDetailsSpi.setCs(vCs.getSelectItem());
            sendMsg(isFromEventBus);
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false);
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false);
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false);
        } else if (viewId == vBits.getId()) {
            if (!isFromEventBus) {
                spiBus.setBits(Integer.parseInt(item.getText().replace("bit", "")));
            }
            Command.get().getBus_spi().setBits(serialsNumber - 1, item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_BIT + serialsNumber, String.valueOf(item.getIndex()));
            msgDetailsSpi.setBit(item);
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setSpiBits(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setSpiBits(item.getIndex());
//            }
            int ch= TChan.toSerialTChan(serialsNumber);
            SerialBusManage.getInstance().getSerialBus(ch).setSpiBits(item.getIndex());

            sendMsg(isFromEventBus);
        } else if (viewId == rgClkCheck.getId()) {
            int index = rgClkCheck.getSelectIndex();
            if (!isFromEventBus) {
                spiBus.setClkSample(index);
            }
            Command.get().getBus_spi().setClockSwitch(serialsNumber - 1, index, false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLKCHECK + serialsNumber, String.valueOf(index == 1));
            msgDetailsSpi.setClkLow(index == 1);
            sendMsg(isFromEventBus);
        } else if (viewId == rgDataCheck.getId()) {
            int index = rgDataCheck.getSelectIndex();
            if (!isFromEventBus) {
                spiBus.setDataIdleLevel(index);
            }
            Command.get().getBus_spi().setDataSwitch(serialsNumber - 1, index, false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATACHECK + serialsNumber, String.valueOf(index == 1));
            msgDetailsSpi.setDataLow(index == 1);
            sendMsg(isFromEventBus);
        } else if (viewId == rgCsCheck.getId()) {
            int index = rgCsCheck.getSelectIndex();
            if (!isFromEventBus) {
                spiBus.setCsIdleLevel(index);
            }
            Command.get().getBus_spi().setCsSwitch(serialsNumber - 1, index, false);
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSCHECK + serialsNumber, String.valueOf(index == 1));
            msgDetailsSpi.setCsLow(index == 1);
            sendMsg(isFromEventBus);
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    private void onSwitchChanged(int viewId, boolean state, boolean isFromEventBus) {
        if (viewId == csSwitch.getId()) {
            msgDetailsSpi.setCsSwitch(state);
            vCs.setEnabled(state);
            rgCsCheck.setEnabled(state);
            if (state) {
                int select = vCs.getSelectIndex();
                if (vClk.getSelectIndex() == select) {
                    int index = vClk.getSelectIndex();
                    vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1);
                }
                if (vData.getSelectIndex() == select) {
                    int index = vData.getSelectIndex();
                    int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1;
                    if (index2 != vClk.getSelectIndex()) {
                        vData.setSelectIndex(index2);
                    } else {
                        int index3 = ((index2 == vData.getSelectCount() - 1) ? 0 : index2 + 1);
                        vData.setSelectIndex(index3);
                    }
                } else {
                    if (vData.getSelectIndex() == vClk.getSelectIndex()) {
                        int index = vData.getSelectIndex();
                        int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1;
                        vData.setSelectIndex(index2);
                    }
                }
                msgDetailsSpi.setClk(vClk.getSelectItem());
                msgDetailsSpi.setData(vData.getSelectItem());
                msgDetailsSpi.setCsSwitch(state);
            }
            if (!isFromEventBus) {
                spiBus.setCsValid(state);
            }
            Command.get().getBus_spi().setCsEnable(serialsNumber - 1, state , false);
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false);
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false);
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false);

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber, String.valueOf(state));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex()));
            sendMsg(isFromEventBus);

            SerialBusManage.getInstance().clearSerialBusTxtBuffer();
        }
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}
