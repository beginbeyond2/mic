package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.I2CBus;
import com.micsig.tbook.scope.Bus.IBus;
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
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/4.
 */

public class RightLayoutSerialsI2c extends RelativeLayout {
    private Context context;
    private RightViewSelect vSda, vScl;

    private RightMsgSerialsI2c msgDetailsI2c;
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;
    private int serialsNumber;

    private I2CBus i2cBus;

    public RightLayoutSerialsI2c(Context context) {
        this(context, null);
    }

    public RightLayoutSerialsI2c(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutSerialsI2c(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_i2c, this);
        vSda = (RightViewSelect) findViewById(R.id.sda);
        vScl = (RightViewSelect) findViewById(R.id.scl);

        String[] channels = GlobalVar.get().getChannelsName();
        vSda.setArray(channels);
        vScl.setArray(channels);

        vSda.setOnItemClickListener(onItemClickListener);
        vScl.setOnItemClickListener(onItemClickListener);

        initData();
    }

    private void initData() {
        msgDetailsI2c = new RightMsgSerialsI2c();
        msgDetailsI2c.setSda(vSda.getSelectItem());
        vScl.setSelectIndex(1);
        msgDetailsI2c.setScl(vScl.getSelectItem());
    }

    void setCache() {
        int sda = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber);
        int scl = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber);
        vSda.setSelectIndex(sda);
        vScl.setSelectIndex(scl);

        Command.get().getTrigger_iic().setSource(serialsNumber - 1, sda, false);
        Command.get().getTrigger_iic().setClock(serialsNumber - 1, scl, false);

        Command.get().getBus_iic().SDA(serialsNumber-1,sda,false);
        Command.get().getBus_iic().SCL(serialsNumber-1,scl,false);

        i2cBus.setSdaChIdx(sda);
        i2cBus.setSclChIdx(scl);

        msgDetailsI2c.setSda(vSda.getSelectItem());
        msgDetailsI2c.setScl(vScl.getSelectItem());
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.I2C) {
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
        int tChan= TChan.toSerialTChan(serialsNumber);
        int fpgaChan=TChan.toFpgaChNo(tChan);
        setControlColorByChIdx(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
    }
    private void setControlColorByChIdx(int chIdx) {
        vSda.setControlColorByChIdx(chIdx);
        vScl.setControlColorByChIdx(chIdx);
    }
    public RightMsgSerialsI2c getMsgDetailsI2c() {
        return msgDetailsI2c;
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsI2c.this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber)));
            if (serialChannel == null) return;
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsI2c, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERIIC_SOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && vSda.getSelectIndex() != Integer.parseInt(params[1])) {
                        vSda.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vSda.getId(), vSda.getSelectItem(), false);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERIIC_CLOCK: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    if (Integer.parseInt(params[0]) == serialsNumber - 1
                            && vScl.getSelectIndex() != Integer.parseInt(params[1])) {
                        vScl.setSelectIndex(Integer.parseInt(params[1]));
                        onCheckChanged(vScl.getId(), vScl.getSelectItem(), false);
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_IIC_SDA:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    int ch=Integer.parseInt(params[1]);
                    if (sNum != serialsNumber - 1) return;
                    vSda.setSelectIndex(ch);
                    onCheckChanged(vSda.getId(),vSda.getSelectItem(),false);
                }break;
                case CommandMsgToUI.FLAG_Bus_IIC_SCL:{
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int sNum=Integer.parseInt(params[0]);
                    int ch=Integer.parseInt(params[1]);
                    if (sNum != serialsNumber - 1) return;
                    vScl.setSelectIndex(ch);
                    onCheckChanged(vScl.getId(),vScl.getSelectItem(),false);
                }break;
            }
        }
    };

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (i2cBus == null) return;
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM
                    && i2cBus.equals(eventBase.getData())) {
                if (i2cBus.getSdaChIdx() != vSda.getSelectIndex()) {
                    vSda.setSelectIndex(i2cBus.getSdaChIdx());
                    onCheckChanged(vSda.getId(), vSda.getSelectItem(), true);
                }
                if (i2cBus.getSclChIdx() != vScl.getSelectIndex()) {
                    vScl.setSelectIndex(i2cBus.getSclChIdx());
                    onCheckChanged(vScl.getId(), vScl.getSelectItem(), true);
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
        if (viewId == vSda.getId()) {
            if (!isFromEventBus) {
                i2cBus.setSdaChIdx(item.getIndex());
            }
            msgDetailsI2c.setSda(item);
            if (item.getIndex() == vScl.getSelectIndex()) {
                int index = vScl.getSelectIndex();
                vScl.setSelectIndex(index == vScl.getSelectCount() - 1 ? 0 : index + 1);
                if (!isFromEventBus) {
                    i2cBus.setSclChIdx(vScl.getSelectIndex());
                }
                msgDetailsI2c.setScl(vScl.getSelectItem());
                msgDetailsI2c.setSda(item);
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber, String.valueOf(vSda.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber, String.valueOf(vScl.getSelectIndex()));
            sendMsg(isFromEventBus);

            Command.get().getTrigger_iic().setSource(serialsNumber - 1, vSda.getSelectIndex(), false);
            Command.get().getBus_iic().SDA(serialsNumber-1,vSda.getSelectIndex(),false);
            Command.get().getTrigger_iic().setClock(serialsNumber - 1, vScl.getSelectIndex(), false);
            Command.get().getBus_iic().SCL(serialsNumber-1,vScl.getSelectIndex(),false);
        } else if (viewId == vScl.getId()) {
            if (!isFromEventBus) {
                i2cBus.setSclChIdx(item.getIndex());
            }
            msgDetailsI2c.setScl(item);
            if (item.getIndex() == vSda.getSelectIndex()) {
                int index = vSda.getSelectIndex();
                vSda.setSelectIndex(index == vSda.getSelectCount() - 1 ? 0 : index + 1);
                if (!isFromEventBus) {
                    i2cBus.setSdaChIdx(vSda.getSelectIndex());
                }
                msgDetailsI2c.setSda(vSda.getSelectItem());
                msgDetailsI2c.setScl(item);
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber, String.valueOf(vSda.getSelectIndex()));
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber, String.valueOf(vScl.getSelectIndex()));
            sendMsg(isFromEventBus);

            Command.get().getTrigger_iic().setSource(serialsNumber - 1, vSda.getSelectIndex(), false);
            Command.get().getBus_iic().SDA(serialsNumber-1,vSda.getSelectIndex(),false);
            Command.get().getTrigger_iic().setClock(serialsNumber - 1, vScl.getSelectIndex(), false);
            Command.get().getBus_iic().SCL(serialsNumber-1,vScl.getSelectIndex(),false);
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }
}
