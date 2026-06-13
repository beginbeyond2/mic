package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.Trigger.TriggerLevel;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgLevel;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.command.Command_Bus;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgLevel;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsCan;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsI2c;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsLin;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM1553b;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM429;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.TriggerValueVoltageLine;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.RecoveryManage;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayCommon;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerLogic;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.trigger.DiscreetVoltageLine;
import com.micsig.tbook.tbookscope.wavezone.trigger.DiscreetVoltageLineInfoBean;
import com.micsig.tbook.tbookscope.wavezone.trigger.ITriggerLine;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerVoltageLine;
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage;
import com.micsig.tbook.tbookscope.wavezone.wave.MsgWaveToLevel;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MTriggerLevel;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by liwb on 2017/5/16.
 */

public class MainHolderTriggerLevel extends RecyclerView.ViewHolder {
    private static final String TAG = "MainHolderTriggerLevel";

    public static final String LEVEL_TRIGGER_EDGE = "TRIGGER_EDGE";
    public static final String LEVEL_TRIGGER_PULSEWIDTH = "TRIGGER_PULSEWIDTH";
    public static final String LEVEL_TRIGGER_LOGIC = "TRIGGER_LOGIC";
    public static final String LEVEL_TRIGGER_NEDGE = "TRIGGER_NEDGE";
    public static final String LEVEL_TRIGGER_RUNT = "TRIGGER_RUNT ";
    public static final String LEVEL_TRIGGER_SLOPE = "TRIGGER_SLOPE";
    public static final String LEVEL_TRIGGER_TIMEOUT = "TRIGGER_TIMEOUT";
    public static final String LEVEL_TRIGGER_VIDEO = "TRIGGER_VIDEO";
    public static final String LEVEL_VALUE_UART = "VALUE_UART";
    public static final String LEVEL_VALUE_LIN = "VALUE_LIN";
    public static final String LEVEL_VALUE_CAN = "VALUE_CAN";
    public static final String LEVEL_VALUE_SPI = "VALUE_SPI";
    public static final String LEVEL_VALUE_I2C = "VALUE_I2C";
    public static final String LEVEL_VALUE_M429 = "VALUE_M429";
    public static final String LEVEL_VALUE_M1553B = "VALUE_M1553B";
    private Context context;
    private MTriggerLevel mTriggerLevel;
    private MainMsgTriggerLevel msgTriggerLevel;
    private TopMsgTriggerLogic msgTriggerLogic;
    private boolean visibleTopRight = true;
    private boolean visibleTriggerVideo = true;
    private boolean visibleYT = true;
    private boolean visibleSerialTxt = true;

    private String curLevel = LEVEL_TRIGGER_EDGE;
    private String curTriggerLevel;
    private String curValue1Level = LEVEL_VALUE_UART, curValue2Level = LEVEL_VALUE_UART, curValue3Level = LEVEL_VALUE_UART, curValue4Level = LEVEL_VALUE_UART;
    private final String[] SerialType=new String[]{
            LEVEL_VALUE_UART,LEVEL_VALUE_LIN,LEVEL_VALUE_CAN,LEVEL_VALUE_SPI,LEVEL_VALUE_I2C,LEVEL_VALUE_M429,LEVEL_VALUE_M1553B
    };
    private int curCh = TChan.Ch1;//1- 8
    private int spiCurCh[] = new int[2];
    private int i2cCurCh[] = new int[2];
    private int serialsNumber = 1;
    private MainViewGroup mainViewGroup;

    public MainHolderTriggerLevel(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        msgTriggerLevel = new MainMsgTriggerLevel();
        mainViewGroup = (MainViewGroup) itemView;
        initView(itemView);
        initControl();
    }

    private void initView(View itemView) {
        mTriggerLevel = (MTriggerLevel) itemView.findViewById(R.id.triggerLevel);
        mTriggerLevel.setChannelCount(GlobalVar.get().getChannelsCount());
        mTriggerLevel.bringToFront();
        mTriggerLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    msgTrigger.setFromEventBus(false);
                    consumerTrigger.accept(msgTrigger);
                    setTriggerLevelActive(true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        mTriggerLevel.setOnOpenCloseListener(onOpenCloseListener);
        mTriggerLevel.setOnMouseMoveListener(onMouseMoveListener);
    }

    private void initControl() {
        TriggerValueVoltageLine.getInstance().setTriggerVoltageLineCallback(triggerVoltageLineCallback);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerDisplay);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTrigger);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_SERIALSDETAIL).subscribe(consumerMainRightSerialsDetail);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_LEVEL).subscribe(consumerExternalKeysLevel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_LEVEL).subscribe(consumerRightLevel);
        RxBus.getInstance().getObservable(RxEnum.WAVE_TO_LEVEL).subscribe(consumerWaveToLevel);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);

        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, this::OnChanActiveChange);

        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_LEVEL, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_LEVEL, eventUIObserver);


    }


    private void sendMsgTriggerLevel(boolean isOnlyModifyNumber, boolean isFromEventBus) {
        if (StrUtil.isEmpty(curLevel)) return;
        msgTriggerLevel.setCurLevel(curLevel);
        msgTriggerLevel.setCurCh(curCh);
        msgTriggerLevel.setOnlyModifyNumber(isOnlyModifyNumber);
        msgTriggerLevel.setFromEventBus(isFromEventBus);
        RxBus.getInstance().post(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL, new MainMsgTriggerLevel(msgTriggerLevel));
    }

    private void changeTriggerVoltage(String curLevel, boolean isFromEventBus) {
        if (LEVEL_TRIGGER_LOGIC.equals(curLevel)) {
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.changeTriggerVoltage: %s", Arrays.toString(mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state())));
            mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch1, getModeWorkLogicState(msgTriggerLogic.getCh1().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch2, getModeWorkLogicState(msgTriggerLogic.getCh2().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch3, getModeWorkLogicState(msgTriggerLogic.getCh3().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch4, getModeWorkLogicState(msgTriggerLogic.getCh4().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch5, getModeWorkLogicState(msgTriggerLogic.getCh5().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch6, getModeWorkLogicState(msgTriggerLogic.getCh6().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch7, getModeWorkLogicState(msgTriggerLogic.getCh7().getIndex()));
            mTriggerLevel.setTriggerLevel_Mode_Work_Logic_state(TChan.Ch8, getModeWorkLogicState(msgTriggerLogic.getCh8().getIndex()));

            TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
            line.setShowMode(ITriggerLine.ShowMode_Three);
            line.setTriggerVoltageLine_logic_state(mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state());
            line.setVisibleLine(false);
            curCh = line.getChannelId();
//            long ch1 = WaveManage.get().getPositionY(TChan.Ch1) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH1);
//            line.setOtherY(TChan.Ch1, ch1);
//            long ch2 = WaveManage.get().getPositionY(TChan.Ch2) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH2);
//            line.setOtherY(TChan.Ch2, ch2);
//            long ch3 = WaveManage.get().getPositionY(TChan.Ch3) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH3);
//            line.setOtherY(TChan.Ch3, ch3);
//            long ch4 = WaveManage.get().getPositionY(TChan.Ch4) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH4);
//            line.setOtherY(TChan.Ch4, ch4);
            TChan.foreachChan((chan) -> {
                double chY = WaveManage.get().getPositionY(chan) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + chan);
                line.setOtherY(chan, chY);
            });

            if (curCh == TChan.Ch1 && msgTriggerLogic.getCh1().getIndex() != 2) {
                curCh = TChan.Ch1;
                mTriggerLevel.setCurrCh(TChan.Ch1);
                line.setCurrYIndex(TChan.Ch1);
            } else if (curCh == TChan.Ch2 && msgTriggerLogic.getCh2().getIndex() != 2) {
                curCh = TChan.Ch2;
                mTriggerLevel.setCurrCh(TChan.Ch2);
                line.setCurrYIndex(TChan.Ch2);
            } else if (curCh == TChan.Ch3 && msgTriggerLogic.getCh3().getIndex() != 2) {
                curCh = TChan.Ch3;
                mTriggerLevel.setCurrCh(TChan.Ch3);
                line.setCurrYIndex(TChan.Ch3);
            } else if (curCh == TChan.Ch4 && msgTriggerLogic.getCh4().getIndex() != 2) {
                curCh = TChan.Ch4;
                mTriggerLevel.setCurrCh(TChan.Ch4);
                line.setCurrYIndex(TChan.Ch4);
            } else if (curCh == TChan.Ch5 && msgTriggerLogic.getCh5().getIndex() != 2) {
                curCh = TChan.Ch5;
                mTriggerLevel.setCurrCh(TChan.Ch5);
                line.setCurrYIndex(TChan.Ch5);
            } else if (curCh == TChan.Ch6 && msgTriggerLogic.getCh6().getIndex() != 2) {
                curCh = TChan.Ch6;
                mTriggerLevel.setCurrCh(TChan.Ch6);
            } else if (curCh == TChan.Ch7 && msgTriggerLogic.getCh7().getIndex() != 2) {
                curCh = TChan.Ch7;
                mTriggerLevel.setCurrCh(TChan.Ch7);
                line.setCurrYIndex(TChan.Ch7);
            } else if (curCh == TChan.Ch8 && msgTriggerLogic.getCh8().getIndex() != 2) {
                curCh = TChan.Ch8;
                mTriggerLevel.setCurrCh(TChan.Ch8);
                line.setCurrYIndex(TChan.Ch8);
            } else {
                if (msgTriggerLogic.getCh1().getIndex() != 2) {
                    curCh = TChan.Ch1;
                    mTriggerLevel.setCurrCh(TChan.Ch1);
                    line.setCurrYIndex(TChan.Ch1);
                } else if (msgTriggerLogic.getCh2().getIndex() != 2) {
                    curCh = TChan.Ch2;
                    mTriggerLevel.setCurrCh(TChan.Ch2);
                    line.setCurrYIndex(TChan.Ch2);
                } else if (msgTriggerLogic.getCh3().getIndex() != 2) {
                    curCh = TChan.Ch3;
                    mTriggerLevel.setCurrCh(TChan.Ch3);
                    line.setCurrYIndex(TChan.Ch3);
                } else if (msgTriggerLogic.getCh4().getIndex() != 2) {
                    curCh = TChan.Ch4;
                    mTriggerLevel.setCurrCh(TChan.Ch4);
                    line.setCurrYIndex(TChan.Ch4);
                } else if (msgTriggerLogic.getCh5().getIndex() != 2) {
                    curCh = TChan.Ch5;
                    mTriggerLevel.setCurrCh(TChan.Ch5);
                    line.setCurrYIndex(TChan.Ch5);
                } else if (msgTriggerLogic.getCh6().getIndex() != 2) {
                    curCh = TChan.Ch6;
                    mTriggerLevel.setCurrCh(TChan.Ch6);
                    line.setCurrYIndex(TChan.Ch6);
                } else if (msgTriggerLogic.getCh7().getIndex() != 2) {
                    curCh = TChan.Ch7;
                    mTriggerLevel.setCurrCh(TChan.Ch7);
                    line.setCurrYIndex(TChan.Ch7);
                } else if (msgTriggerLogic.getCh8().getIndex() != 2) {
                    curCh = TChan.Ch8;
                    mTriggerLevel.setCurrCh(TChan.Ch8);
                    line.setCurrYIndex(TChan.Ch8);
                } else {
                    visibleTriggerVideo = false;
                    setTriggerLevelVisible(View.GONE);
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).setShowState(false);
                }
            }
            line.setText(Tools.getChannelLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal));
            Command.get().getTrigger_logic().Level(TChan.Ch1 - TChan.Ch1, getDoubleV(TChan.Ch1), false);
            Command.get().getTrigger_logic().Level(TChan.Ch2 - TChan.Ch1, getDoubleV(TChan.Ch2), false);
            Command.get().getTrigger_logic().Level(TChan.Ch3 - TChan.Ch1, getDoubleV(TChan.Ch3), false);
            Command.get().getTrigger_logic().Level(TChan.Ch4 - TChan.Ch1, getDoubleV(TChan.Ch4), false);
            Command.get().getTrigger_logic().Level(TChan.Ch5 - TChan.Ch1, getDoubleV(TChan.Ch5), false);
            Command.get().getTrigger_logic().Level(TChan.Ch6 - TChan.Ch1, getDoubleV(TChan.Ch6), false);
            Command.get().getTrigger_logic().Level(TChan.Ch7 - TChan.Ch1, getDoubleV(TChan.Ch7), false);
            Command.get().getTrigger_logic().Level(TChan.Ch8 - TChan.Ch1, getDoubleV(TChan.Ch8), false);

            sendMsgTriggerLevel(false, isFromEventBus);
        } else if (LEVEL_TRIGGER_RUNT.equals(curLevel)) {
            mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_HighLow);
            mTriggerLevel.setCurrCh(curCh);

            TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
            line.setChannelId(curCh);
            line.setCurrYIndex(ITriggerLine.VoltageLine_High);
            double high = getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh);
            line.setCurrY(high);
            line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
            double low = getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh);
            line.setCurrY(low);
            line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
            line.setShowMode(ITriggerLine.ShowMode_Two);
            line.setVisibleLine(false);
            line.setText(Tools.getChannelLevel(curCh,
                    line.getCurrYIndex() == ITriggerLine.VoltageLine_High ? Tools.LevelType_High : Tools.LevelType_Normal,
                    Tools.LevelMode_Normal));
            Command.get().getTrigger_dwart().HLevel(getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Normal), false);
            Command.get().getTrigger_dwart().LLevel(getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal), false);
            sendMsgTriggerLevel(false, isFromEventBus);
        } else if (LEVEL_TRIGGER_SLOPE.equals(curLevel)) {
            mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_HighLow);
            mTriggerLevel.setCurrCh(curCh);

            TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
            line.setChannelId(curCh);
            line.setCurrYIndex(ITriggerLine.VoltageLine_High);
            double high = getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh);
            line.setCurrY(high);
            line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
            double low = getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh);
            line.setCurrY(low);
            line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
            line.setShowMode(ITriggerLine.ShowMode_Two);
            line.setVisibleLine(false);
            line.setText(Tools.getChannelLevel(curCh,
                    line.getCurrYIndex() == ITriggerLine.VoltageLine_High ? Tools.LevelType_High : Tools.LevelType_Normal
                    , Tools.LevelMode_Normal));
            Command.get().getTrigger_slope().HLevel(getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Normal), false);
            Command.get().getTrigger_slope().LLevel(getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal), false);
            sendMsgTriggerLevel(false, isFromEventBus);
        } else {
            mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Normal);
            mTriggerLevel.setCurrCh(curCh);
            TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
            line.setChannelId(curCh);
            line.setCurrYIndex(curCh);
            if(curCh == TChan.Ch8 + 1) { //外部触发
                line.setCurrY(getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh));
            } else {
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh));

            }
            line.setShowMode(ITriggerLine.ShowMode_One);
            line.setVisibleLine(false);
            line.setText(Tools.getChannelLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal));
            switch (curLevel) {
                case LEVEL_TRIGGER_LOGIC:
                    Command.get().getTrigger_logic().Level(TChan.toFpgaChNo(curCh), getDoubleV(curCh), false);
                    break;
                case LEVEL_TRIGGER_EDGE:
                    Command.get().getTrigger_edge().Level(getDoubleV(curCh), false);
                    break;
                case LEVEL_TRIGGER_NEDGE:
                    Command.get().getTrigger_nedge().Level(getDoubleV(curCh), false);
                    break;
                case LEVEL_TRIGGER_PULSEWIDTH:
                    Command.get().getTrigger_pulse().Level(getDoubleV(curCh), false);
                    break;
                case LEVEL_TRIGGER_TIMEOUT:
                    Command.get().getTrigger_timeout().Level(getDoubleV(curCh), false);
                    break;
            }
            sendMsgTriggerLevel(false, isFromEventBus);
        }
    }


    /**
     * 刷S1或S2的�?�道显示
     * 功能同 installDiscreetLineParamToUI
     */
    private void refreshDiscreetVoltageLineInfo(int serialsNumber, String curLevel, boolean isFromEventBus) {
        String key;
        if (serialsNumber == 1) {
            key = VoltageLineManage.VoltageLineType_Value1;
            CacheUtil.get().setValueLevelSerials(CacheUtil.S1);
        } else if (serialsNumber == 2) {
            key = VoltageLineManage.VoltageLineType_Value2;
            CacheUtil.get().setValueLevelSerials(CacheUtil.S2);
        } else if (serialsNumber == 3) {
            key = VoltageLineManage.VoltageLineType_Value3;
            CacheUtil.get().setValueLevelSerials(CacheUtil.S3);
        } else {
            key = VoltageLineManage.VoltageLineType_Value4;
            CacheUtil.get().setValueLevelSerials(CacheUtil.S4);
        }
        if (curLevel.equals(LEVEL_VALUE_M429)) {
            DiscreetVoltageLine line = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(key);
            line.setChannelId(curCh);
            line.setCurrYIndex(ITriggerLine.VoltageLine_High);
            double high = getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh);
            line.setOtherY(ITriggerLine.VoltageLine_High, high);
            line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
            double low = getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL + curCh);
            line.setOtherY(ITriggerLine.VoltageLine_Low, low);
            line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
            line.setShowMode(ITriggerLine.ShowMode_Two);
            line.setVisibleLine(false);
            {
                double lowY = line.getOtherY(ITriggerLine.VoltageLine_Low);
                double highY = line.getOtherY(ITriggerLine.VoltageLine_High);
                Map<String, ITriggerLine> map = VoltageLineManage.getInstance().getMapVoltageLine();
                map.forEach((k, v) -> {
                    if (v instanceof DiscreetVoltageLine) {
                        DiscreetVoltageLine otherValueLine = (DiscreetVoltageLine) v;
                        if (v.getNameId() != line.getNameId()) {
                            if (otherValueLine.getShowMode() != ITriggerLine.ShowMode_Two) {
                                if (otherValueLine.getOtherY(line.getChannelId()) != lowY) {
                                    otherValueLine.setOtherY(line.getChannelId(), lowY);
                                }
                            } else {
                                if (otherValueLine.getOtherY(ITriggerLine.VoltageLine_High) != highY
                                        && otherValueLine.getChannelId() == line.getChannelId()) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_High, highY);
                                }
                                if (otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != lowY
                                        && otherValueLine.getChannelId() == line.getChannelId()) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, lowY);
                                }
                            }
                        }
                    }
                });
            }

            line.setText(Tools.getChannelLevel(curCh,
                    line.getCurrYIndex() == ITriggerLine.VoltageLine_High ? Tools.LevelType_High : Tools.LevelType_Normal,
                    Tools.LevelMode_Bus));
            Command.get().getTrigger_m429().setLevelHigh(serialsNumber - 1, curCh, getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Bus), false);
            Command.get().getTrigger_m429().setLevelLow(serialsNumber - 1, curCh, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
            sendMsgTriggerLevel(false, isFromEventBus);
        } else {
            DiscreetVoltageLine line = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(key);
            line.setChannelId(curCh);
            line.setCurrYIndex(curCh);
            line.setTriggerVoltageLine_logic_state(mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state());
            line.setShowMode(ITriggerLine.ShowMode_One);
            line.setVisibleLine(false);
            line.setOtherY(TChan.Ch1, WaveManage.get().getPositionY(TChan.Ch1) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch1));
            line.setOtherY(TChan.Ch2, WaveManage.get().getPositionY(TChan.Ch2) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch2));
            line.setOtherY(TChan.Ch3, WaveManage.get().getPositionY(TChan.Ch3) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch3));
            line.setOtherY(TChan.Ch4, WaveManage.get().getPositionY(TChan.Ch4) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch4));
            line.setOtherY(TChan.Ch5, WaveManage.get().getPositionY(TChan.Ch5) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch5));
            line.setOtherY(TChan.Ch6, WaveManage.get().getPositionY(TChan.Ch6) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch6));
            line.setOtherY(TChan.Ch7, WaveManage.get().getPositionY(TChan.Ch7) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch7));
            line.setOtherY(TChan.Ch8, WaveManage.get().getPositionY(TChan.Ch8) - getLevelCache(CacheUtil.VALUE_CHANNEL + TChan.Ch8));
            {
                Map<String, ITriggerLine> map = VoltageLineManage.getInstance().getMapVoltageLine();
                map.forEach((k, v) -> {
                    if (v instanceof DiscreetVoltageLine) {
                        DiscreetVoltageLine otherValueLine = (DiscreetVoltageLine) v;
                        if(v.getNameId() != line.getNameId()) {
                            if (otherValueLine.getShowMode() != ITriggerLine.ShowMode_Two) {
                                if (otherValueLine.getOtherY(TChan.Ch1) != line.getOtherY(TChan.Ch1)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch1] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch1, line.getOtherY(TChan.Ch1));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch2) != line.getOtherY(TChan.Ch2)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch2] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch2, line.getOtherY(TChan.Ch2));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch3) != line.getOtherY(TChan.Ch3)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch3] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch3, line.getOtherY(TChan.Ch3));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch4) != line.getOtherY(TChan.Ch4)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch4] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch4, line.getOtherY(TChan.Ch4));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch5) != line.getOtherY(TChan.Ch5)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch5] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch5, line.getOtherY(TChan.Ch5));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch6) != line.getOtherY(TChan.Ch6)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch6] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch6, line.getOtherY(TChan.Ch6));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch7) != line.getOtherY(TChan.Ch7)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch7] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch7, line.getOtherY(TChan.Ch7));
                                }
                                if (otherValueLine.getOtherY(TChan.Ch8) != line.getOtherY(TChan.Ch8)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch8] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(TChan.Ch8, line.getOtherY(TChan.Ch8));
                                }
                            } else {
                                if (otherValueLine.getChannelId() == TChan.Ch1
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch1)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch1] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch1));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch2
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch2)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch2] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch2));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch3
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch3)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch3] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch3));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch4
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch4)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch4] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch4));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch5
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch5)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch5] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch5));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch6
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch6)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch6] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch6));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch7
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch7)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch7] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch7));
                                }
                                if (otherValueLine.getChannelId() == TChan.Ch8
                                        && otherValueLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(TChan.Ch8)
                                        && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[TChan.Ch8] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                    otherValueLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(TChan.Ch8));
                                }
                            }

                        }
                    }
                });
            }

            line.setChannelId(curCh);
            line.setCurrYIndex(curCh);
            mTriggerLevel.setCurrCh(curCh);
            line.setText(Tools.getChannelLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus));
            switch (curLevel) {
                case LEVEL_VALUE_SPI:
                    Command.get().getBus_spi().setLevelClock(serialsNumber - 1, getDoubleV(spiClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    Command.get().getBus_spi().setLevelData(serialsNumber - 1, getDoubleV(spiDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    Command.get().getTrigger_spi().LevelCLK(serialsNumber - 1, getDoubleV(spiClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    Command.get().getTrigger_spi().LevelData(serialsNumber - 1, getDoubleV(spiDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    if (spiCsEnable) {
                        Command.get().getBus_spi().setLevelCs(serialsNumber - 1, getDoubleV(spiCsIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                        Command.get().getTrigger_spi().LevelCS(serialsNumber - 1, getDoubleV(spiCsIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    }
                    break;
                case LEVEL_VALUE_I2C:
                    Command.get().getTrigger_iic().setLevelData(serialsNumber - 1, getDoubleV(i2cDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    Command.get().getTrigger_iic().setLevelClock(serialsNumber - 1, getDoubleV(i2cClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    break;
                case LEVEL_VALUE_UART:
                    Command.get().getTrigger_uart().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    break;
                case LEVEL_VALUE_LIN:
                    Command.get().getTrigger_lin().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    break;
                case LEVEL_VALUE_CAN:
                    Command.get().getTrigger_can().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    break;
                case LEVEL_VALUE_M1553B:
                    Command.get().getTrigger_m1553B().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    break;
            }
            sendMsgTriggerLevel(false, isFromEventBus);
        }
        VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();

        String showState;
        showState = (voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value1)).getShowState() ? VoltageLineManage.VoltageLineType_Value1 : "";
        showState += (voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value2)).getShowState() ? VoltageLineManage.VoltageLineType_Value2 : "";
        showState += (voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value3)).getShowState() ? VoltageLineManage.VoltageLineType_Value3 : "";
        showState += (voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value4)).getShowState() ? VoltageLineManage.VoltageLineType_Value4 : "";
        VoltageLineManage.getInstance().setDiscreetVoltageShowState(showState, serialsNumber);

        CacheUtil.get().setValueLevelSerials(0);
    }

    private double getLevelCache(String key) {
        return Tools.getLevelCache(key);
    }

    private static double getYTLevelCache(String key) {
        return Tools.getYTLevelCache(key);
    }

    private void putLevelCache(String key, double value) {
        Tools.putLevelCache(key, value);
    }

    private int getModeWorkLogicState(int channelStateIndex) {
        switch (channelStateIndex) {
            case 0:
                return MTriggerLevel.TriggerLevel_Mode_Work_Logic_High;
            case 1:
                return MTriggerLevel.TriggerLevel_Mode_Work_Logic_Low;
            default:
                return MTriggerLevel.TriggerLevel_Mode_work_Logic_None;
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            //初始化临时电平，使之与正式电平相同
            for (int i = 1; i <= 8; i++) {
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + i, getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + i));
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + i, getLevelCache(CacheUtil.TRIGGER_CHANNEL + i));
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + i, getLevelCache(CacheUtil.VALUE_CHANNEL_H + i));
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + i, getLevelCache(CacheUtil.VALUE_CHANNEL + i));
            }

            initVoltageLineState(CacheUtil.S1);
            initVoltageLineState(CacheUtil.S2);
            initVoltageLineState(CacheUtil.S3);
            initVoltageLineState(CacheUtil.S4);

            String showState = "";
            String key = "";
            int sNo = 1;
            showState = (VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1)).getShowState() ? VoltageLineManage.VoltageLineType_Value1 : "";
            showState += (VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2)).getShowState() ? VoltageLineManage.VoltageLineType_Value2 : "";
            showState += (VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3)).getShowState() ? VoltageLineManage.VoltageLineType_Value3 : "";
            showState += (VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4)).getShowState() ? VoltageLineManage.VoltageLineType_Value4 : "";

            if (showState.contains(VoltageLineManage.VoltageLineType_Value1)) {
                key = VoltageLineManage.VoltageLineType_Value1;
                sNo = 1;
            } else if (showState.contains(VoltageLineManage.VoltageLineType_Value2)) {
                key = VoltageLineManage.VoltageLineType_Value2;
                sNo = 2;
            } else if (showState.contains(VoltageLineManage.VoltageLineType_Value3)) {
                key = VoltageLineManage.VoltageLineType_Value3;
                sNo = 3;
            } else if (showState.contains(VoltageLineManage.VoltageLineType_Value4)) {
                key = VoltageLineManage.VoltageLineType_Value4;
                sNo = 4;
            }
            VoltageLineManage.getInstance().setDiscreetVoltageShowState(showState, sNo);
            VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();


        }
    };

    private void initVoltageLineState(int serialsNumber) {
        DiscreetVoltageLine voltageLine;
//        if (serialsNumber == CacheUtil.S1) {
//            voltageLine = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
//        } else {
//            voltageLine = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
//        }
        voltageLine= (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(serialsNumber);

        int busType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber);
        switch (busType) {
            case CacheUtil.UART: {
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + serialsNumber);
                final int chNo = TChan.toUiChNo(ch);
//                for (int i = 0; i < 4; i++) {
//                    if (i == ch) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chNo) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
            case CacheUtil.LIN: {
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + serialsNumber);
                final int chNo = TChan.toUiChNo(ch);
//                for (int i = 0; i < 4; i++) {
//                    if (i == ch) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chNo) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
            case CacheUtil.CAN: {
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + serialsNumber);
                final int chNo = TChan.toUiChNo(ch);
//                for (int i = 0; i < 4; i++) {
//                    if (i == ch) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chNo) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
            case CacheUtil.SPI: {
                int chClk = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber);
                int chData = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber);
                int chCs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber);
                boolean csSwitch = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber);
                final int chClkNo = TChan.toUiChNo(chClk);
                final int chDataNo = TChan.toUiChNo(chData);
                final int chCsNo = TChan.toUiChNo(chCs);
//                for (int i = 0; i < 4; i++) {
//                    if (i == chClk || i == chData || (csSwitch && i == chCs)) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chClkNo || i == chDataNo || (csSwitch && i == chCsNo)) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
            case CacheUtil.I2C: {
                int chSda = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber);
                int chScl = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber);
                final int chSdaNo = TChan.toUiChNo(chSda);
                final int chSclNo = TChan.toUiChNo(chScl);
//                for (int i = 0; i < 4; i++) {
//                    if (i == chSda || i == chScl) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chSdaNo || i == chSclNo) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });

            }
            break;
            case CacheUtil.M429: {
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber);
                final int chNo = TChan.toUiChNo(ch);
//                for (int i = 0; i < 4; i++) {
//                    if (i == 0 || i == 1) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == 0 || i == 1) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
            case CacheUtil.M1553B: {
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + serialsNumber);
                final int chNo = TChan.toUiChNo(ch);
//                for (int i = 0; i < 4; i++) {
//                    if (i == ch) {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
//                    } else {
//                        voltageLine.setTriggerVoltageLine_logic_state(i + IWave.Ch1, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
//                    }
//                }
                TChan.foreachChan((i) -> {
                    if (i == chNo) {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight);
                    } else {
                        voltageLine.setTriggerVoltageLine_logic_state(i, DiscreetVoltageLine.TriggerVoltageLine_Logic_None);
                    }
                });
            }
            break;
        }
    }

    private int spiClockIndex = 1;
    private int spiDataIndex = 2;
    private int spiCsIndex = 3;
    private boolean spiCsEnable = true;
    private int i2cDataIndex = 1;
    private int i2cClockIndex = 2;

    private Consumer<TopMsgDisplay> consumerDisplay = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayCommon) {
                TopMsgDisplayCommon displayCommon = (TopMsgDisplayCommon) topMsgDisplay.getDisplayDetail();
                if (displayCommon.getTimeBase().isRxMsgSelect()) {
                    if (displayCommon.getTimeBase().getIndex() == 0) {//YT模式
                        visibleYT = true;
                        setTriggerLevelVisible(View.VISIBLE);
                    } else {//XY模式
                        visibleYT = false;
                        setTriggerLevelVisible(View.GONE);
                    }
                }
            }
        }
    };

    private TopMsgTrigger msgTrigger;
    private MainRightMsgSerialsDetail msgSerials1Detail;
    private MainRightMsgSerialsDetail msgSerials2Detail,msgSerials3Detail,msgSerials4Detail;

    /**
     * 用户通过外部按键旋转旋钮造成触发电平改变则为true
     */
    private boolean changeTriggerPos = false;

    private void SetValue_trigCHisOne(boolean isOneLevel) {
        visibleTriggerVideo = true;
        setTriggerLevelVisible(View.VISIBLE);
        VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).setShowState(true);
        curCh = msgTrigger.getTriggerDetail().getTriggerSource().getIndex() + 1;

        double level = getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh);
        if (!msgTrigger.isFromEventBus()) {
            Trigger trigger = TriggerFactory.getInstance().getTrigger();
            if (!isOneLevel) {//双电平
                double leveH = getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh);
                if (level > leveH) {
                    //if(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index() == 1) //双电平时的高电平
                    leveH = level;
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh, leveH);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + curCh, leveH);
                }
                int src = trigger.getTriggerSource();
                TriggerLevel tLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src);
                if (tLevel == null) return;
                trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src).setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh), changeTriggerPos);
                trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, src).setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh), changeTriggerPos);
                changeTriggerPos = false;
            } else {
                TriggerLevel tLevel = trigger.getTriggerLevel();
                if(tLevel != null) {
                    tLevel.setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh), changeTriggerPos);
                } else {//外部触发
                    if (curCh == TChan.Ch8 + 1) {
                        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
                        triggerCommon.setExtTriggerLevle(Tools.getExtTriggerValCache(CacheUtil.TRIGGER_CHANNEL + curCh));
                    }
                }
                changeTriggerPos = false;
            }
        }
        changeTriggerVoltage(curLevel, msgTrigger.isFromEventBus());
    }

    private Consumer<TopMsgTrigger> consumerTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(TopMsgTrigger topMsgTrigger) throws Exception {
            //region Trigger
            if (topMsgTrigger == null) return;
//            Logger.d("consumerTrigger1:" + topMsgTrigger);
            if (topMsgTrigger.getTriggerTitle().getIndex() != TopLayoutTrigger.DETAIL_COMMON) {
                msgTrigger = (TopMsgTrigger) topMsgTrigger.clone();
            }
//            Logger.d("consumerTrigger2:" + msgTrigger);
            if(msgTrigger == null || msgTrigger.getTriggerTitle() == null) return;
            int index = msgTrigger.getTriggerTitle().getIndex();
            mTriggerLevel.setButtonChColor(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE) + TChan.Ch1);
            mTriggerLevel.setCurrentTriggerIndex(topMsgTrigger.getTriggerTitle().getIndex());
            switch (index) {
                case TopLayoutTrigger.DETAIL_COMMON:
                    visibleTriggerVideo = true;
                    setTriggerLevelVisible(View.VISIBLE);
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).setShowState(true);
                    break;
                case TopLayoutTrigger.DETAIL_EDGE:
                    curLevel = LEVEL_TRIGGER_EDGE;
                    curTriggerLevel = LEVEL_TRIGGER_EDGE;
                    SetValue_trigCHisOne(true);
                    break;
                case TopLayoutTrigger.DETAIL_PULSEWIDTH:
                    curLevel = LEVEL_TRIGGER_PULSEWIDTH;
                    curTriggerLevel = LEVEL_TRIGGER_PULSEWIDTH;
                    SetValue_trigCHisOne(true);
                    break;
                case TopLayoutTrigger.DETAIL_NEDGE:
                    curLevel = LEVEL_TRIGGER_NEDGE;
                    curTriggerLevel = LEVEL_TRIGGER_NEDGE;
                    SetValue_trigCHisOne(true);
                    break;
                case TopLayoutTrigger.DETAIL_TIMEOUT:
                    curLevel = LEVEL_TRIGGER_TIMEOUT;
                    curTriggerLevel = LEVEL_TRIGGER_TIMEOUT;
                    SetValue_trigCHisOne(true);
                    break;
                case TopLayoutTrigger.DETAIL_RUNT:           //欠幅
                    curLevel = LEVEL_TRIGGER_RUNT;
                    curTriggerLevel = LEVEL_TRIGGER_RUNT;
                    SetValue_trigCHisOne(false);
                    break;
                case TopLayoutTrigger.DETAIL_SLOPE:          //斜率
                    curLevel = LEVEL_TRIGGER_SLOPE;
                    curTriggerLevel = LEVEL_TRIGGER_SLOPE;
                    SetValue_trigCHisOne(false);
                    break;
                case TopLayoutTrigger.DETAIL_LOGIC: {
                    curLevel = LEVEL_TRIGGER_LOGIC;
                    curTriggerLevel = LEVEL_TRIGGER_LOGIC;
                    visibleTriggerVideo = true;
                    setTriggerLevelVisible(View.VISIBLE);
                    TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                    line.setShowState(true);
                    curCh = line.getChannelId();
                    msgTriggerLogic = (TopMsgTriggerLogic) msgTrigger.getTriggerDetail();
                    if (!msgTrigger.isFromEventBus()) {
                        Trigger trigger = TriggerFactory.getInstance().getTrigger();
                        for (int i = 0; i < trigger.getTriggerSourceCnt(); i++) {
                            int src = trigger.getTriggerSource(i);
                            int src_ui = TChan.toUiChNo(src);
                            trigger.getTriggerLevel(src).setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + src_ui));
                        }
                    }
                    changeTriggerVoltage(curLevel, msgTrigger.isFromEventBus());
                    break;
                }
                case TopLayoutTrigger.DETAIL_S1:
                case TopLayoutTrigger.DETAIL_S2:
                case TopLayoutTrigger.DETAIL_S3:
                case TopLayoutTrigger.DETAIL_S4:
                {
                    visibleTriggerVideo = false;
                    setTriggerLevelVisible(View.GONE);
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).setShowState(false);
                    if (!msgTrigger.isFromEventBus()) {
                        int seri_idx = TChan.S1;
                        if (index == TopLayoutTrigger.DETAIL_S4) {
                            seri_idx = TChan.S4;
                        } else if (index == TopLayoutTrigger.DETAIL_S3) {
                            seri_idx = TChan.S3;
                        } else if (index == TopLayoutTrigger.DETAIL_S2) {
                            seri_idx = TChan.S2;
                        } else {
                            seri_idx = TChan.S1;
                        }
                        changeSerialTrigVol_serials(seri_idx);
                    }
                    break;
                }
                case TopLayoutTrigger.DETAIL_VIDEO:
                    curLevel = LEVEL_TRIGGER_VIDEO;
                    curTriggerLevel = LEVEL_TRIGGER_VIDEO;
                    visibleTriggerVideo = false;
                    setTriggerLevelVisible(View.GONE);
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).setShowState(false);
                    break;
                default:
                    break;
            }
            TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
            if (triggerValueVoltageLine.isTriggerlevelActive()) {
                switch (index) {
                    case TopLayoutTrigger.DETAIL_S1:
                    case TopLayoutTrigger.DETAIL_S2:
                    case TopLayoutTrigger.DETAIL_S3:
                    case TopLayoutTrigger.DETAIL_S4:
                    case TopLayoutTrigger.DETAIL_VIDEO:
                        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)
                                || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                                || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)
                                || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)) {
                            setTriggerLevelActive(false);
                        }
                        break;
                }


            }
            //endregion
        }
    };

    private Consumer<RightMsgLevel> consumerRightLevel = new Consumer<RightMsgLevel>() {
        @Override
        public void accept(@NonNull RightMsgLevel msgLevel) throws Exception {
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept RightMsgLevel: %s", msgLevel));
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            if (triggerIndex == TopLayoutTrigger.DETAIL_LOGIC) {
                int selectChannel = msgLevel.getBottomSelect();
                if (selectChannel == 0 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH1) != 2) {
                    selectChannel = 0;
                } else if (selectChannel == 1 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH2) != 2) {
                    selectChannel = 1;
                } else if (selectChannel == 2 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH3) != 2) {
                    selectChannel = 2;
                } else if (selectChannel == 3 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH4) != 2) {
                    selectChannel = 3;
                } else if (selectChannel == 4 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH5) != 2) {
                    selectChannel = 4;
                } else if (selectChannel == 5 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH6) != 2) {
                    selectChannel = 5;
                } else if (selectChannel == 6 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH7) != 2) {
                    selectChannel = 6;
                } else if (selectChannel == 7 && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH8) != 2) {
                    selectChannel = 7;
                } else {
                    selectChannel = -1;
                }
                if (selectChannel >= 0 && curLevel.equals(LEVEL_TRIGGER_LOGIC)) {
                    if (curCh == selectChannel + TChan.Ch1) {
                        return;
                    }
                    curCh = selectChannel + TChan.Ch1;
                    mTriggerLevel.setCurrCh(curCh);
                    TriggerVoltageLine line = (TriggerVoltageLine) VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                    line.setChannelId(curCh);
                    line.setCurrY(curCh);
                    changeTriggerVoltage(curLevel, false);
                }
            }
        }
    };

    private void setWaveZoneSlideDirectionAndLastObjToTriggerLevel() {
        TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
        if (triggerValueVoltageLine.isTriggerlevelActive()) {
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerLevel);
        } else {
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.ValueLevel);
        }

        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
    }

    private Consumer<MainRightMsgSerialsDetail> consumerMainRightSerialsDetail = new Consumer<MainRightMsgSerialsDetail>() {
        @Override
        public void accept(@NonNull MainRightMsgSerialsDetail msgSerialsDetail) {

            //region Serials
            RightMsgSerials msgSerials = msgSerialsDetail.getRightMsgSerials();
            if (msgSerials == null) return;
            boolean isAddByUser =   CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + msgSerials.getSerialsNumber());
            if (!isAddByUser) return;
            if (!msgSerials.isOpenLevel()) {
                msgSerials.setOpenLevel(true);
                return;
            }
            serialsNumber = msgSerials.getSerialsNumber();
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept MainRightSerialsDetail: %s", msgSerials));
            if (serialsNumber == 1) {
                mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_SERIALS1);
                MainHolderTriggerLevel.this.msgSerials1Detail = msgSerialsDetail;
                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)) {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1).setShowState(false);
                } else {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1).setShowState(true);
                }
            } else if (serialsNumber == 2) {
                mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_SERIALS2);
                MainHolderTriggerLevel.this.msgSerials2Detail = msgSerialsDetail;
                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)) {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2).setShowState(false);
                } else {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2).setShowState(true);
                }
            } else if (serialsNumber == 3) {
                mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_SERIALS3);
                MainHolderTriggerLevel.this.msgSerials3Detail = msgSerialsDetail;
                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)) {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3).setShowState(false);
                } else {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3).setShowState(true);
                }
            } else if (serialsNumber == 4) {
                mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_SERIALS4);
                MainHolderTriggerLevel.this.msgSerials4Detail = msgSerialsDetail;
                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)) {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4).setShowState(false);
                } else {
                    VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4).setShowState(true);
                }
            }


//            if (mTriggerLevel.getTriggerLevel_Mode_Show() == MTriggerLevel.TriggerLevel_Mode_Show_Button) {
                if (msgSerials.getSerialsType() == null) return;
//                Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept serials button: "));
                if (!msgSerialsDetail.isUnOpenTriggerLevel()) {
                    visibleTriggerVideo = true;
                    setTriggerLevelVisible(View.VISIBLE);
                    boolean isCal = CacheUtil.get().getBoolean(CacheUtil.SAVE_TEMP_IS_CALIBRATION);
                    if (!isCal) {
                        mTriggerLevel.Animation_ButtonToDrag(MTriggerLevel.getOpenType(serialsNumber) );
//                        if (mTriggerLevel.getTriggerLevel_Mode_Show() != MTriggerLevel.TriggerLevel_Mode_Show_Button) {
//                            msgTrigger.setFromEventBus(false);
//                            consumerTrigger.accept(msgTrigger);
//                            setTriggerLevelActive(true);
//                        }
                    }
                }

                if (!msgSerialsDetail.isFromEventBus()) {
                    CacheUtil.get().setValueLevelSerials(serialsNumber);
                    SerialChannel sCh= ChannelFactory.getSerialChannel( TChan.toSerialTChan(serialsNumber) );
                     if(sCh!=null) {
                         sCh.setPos(getLevelCache(CacheUtil.VALUE_CHANNEL + curCh));
                     }
                    CacheUtil.get().setValueLevelSerials(0);
                    setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
                }

                installParamToUI(msgSerials);

//                refreshDiscreetVoltageLineInfo(serialsNumber, curLevel, msgSerialsDetail.isFromEventBus());
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), serialsNumber);
                CacheUtil.get().setValueLevelSerials(0);
                VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
//            }
            //endregion
        }

    };


    private int indexOf(String sType){
        int index= Tools.indexOf(SerialType,s->s.equals(sType));
        return index;
    }
    /**
     * 将参数显示到界面，主要包括两个方面，MTrigger 和 DiscreetVoltageLine
     */
    private void installParamToUI(RightMsgSerials msgSerials) {
        installMTriggerParamToUI(msgSerials);
        installDiscreetLineParamToUI(serialsNumber,msgSerials.getSerialsType().getIndex());
    }

    private void installMTriggerParamToUI(RightMsgSerials msgSerials){
        switch (msgSerials.getSerialsType().getIndex()) {
            case RightLayoutSerials.SERIALS_UART: {
                curLevel = LEVEL_VALUE_UART;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_UART;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_UART;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_UART;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_UART;
                }
                RightMsgSerialsUart uart = (RightMsgSerialsUart) msgSerials.getSerialsDetails();
                curCh = uart.getRx().getIndex() + 1;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
            }
            break;
            case RightLayoutSerials.SERIALS_LIN: {
                curLevel = LEVEL_VALUE_LIN;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_LIN;
                } else if (serialsNumber == 2){
                    curValue2Level = LEVEL_VALUE_LIN;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_LIN;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_LIN;
                }
                RightMsgSerialsLin lin = (RightMsgSerialsLin) msgSerials.getSerialsDetails();
                curCh = lin.getSource().getIndex() + 1;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
            }
            break;
            case RightLayoutSerials.SERIALS_CAN: {
                curLevel = LEVEL_VALUE_CAN;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_CAN;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_CAN;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_CAN;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_CAN;
                }
                RightMsgSerialsCan can = (RightMsgSerialsCan) msgSerials.getSerialsDetails();
                curCh = can.getSource().getIndex() + 1;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
            }
            break;
            case RightLayoutSerials.SERIALS_SPI: {
                curLevel = LEVEL_VALUE_SPI;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_SPI;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_SPI;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_SPI;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_SPI;
                }
                RightMsgSerialsSpi spi = (RightMsgSerialsSpi) msgSerials.getSerialsDetails();
                spiClockIndex = spi.getClk().getIndex() + 1;
                spiDataIndex = spi.getData().getIndex() + 1;
                spiCsIndex = spi.getCs().getIndex() + 1;
                spiCsEnable = spi.getCsSwitch().isValue();
                int minCh = Math.min(spiClockIndex, spiDataIndex);
                if (spiCsEnable) {
                    minCh = Math.min(minCh, spiCsIndex);
                }
                curCh = minCh;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                if (spiCsEnable)
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(spiClockIndex, spiDataIndex, spiCsIndex);
                else
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(spiClockIndex, spiDataIndex);
            }
            break;
            case RightLayoutSerials.SERIALS_I2C: {
                curLevel = LEVEL_VALUE_I2C;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_I2C;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_I2C;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_I2C;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_I2C;
                }
                RightMsgSerialsI2c i2c = (RightMsgSerialsI2c) msgSerials.getSerialsDetails();
                i2cDataIndex = i2c.getSda().getIndex() + 1;
                i2cClockIndex = i2c.getScl().getIndex() + 1;
                curCh = Math.min(i2cClockIndex, i2cDataIndex);
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(i2cDataIndex, i2cClockIndex);
            }
            break;
            case RightLayoutSerials.SERIALS_M429: {
                curLevel = LEVEL_VALUE_M429;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_M429;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_M429;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_M429;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_M429;
                }
                RightMsgSerialsM429 m429 = (RightMsgSerialsM429) msgSerials.getSerialsDetails();
                curCh = m429.getSource().getIndex() + 1;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_HighLow);
                mTriggerLevel.setCurrCh(curCh);
            }
            break;
            case RightLayoutSerials.SERIALS_M1553B: {
                curLevel = LEVEL_VALUE_M1553B;
                if (serialsNumber == 1) {
                    curValue1Level = LEVEL_VALUE_M1553B;
                } else if (serialsNumber == 2) {
                    curValue2Level = LEVEL_VALUE_M1553B;
                } else if (serialsNumber == 3) {
                    curValue3Level = LEVEL_VALUE_M1553B;
                } else if (serialsNumber == 4) {
                    curValue4Level = LEVEL_VALUE_M1553B;
                }
                RightMsgSerialsM1553b m1553b = (RightMsgSerialsM1553b) msgSerials.getSerialsDetails();
                curCh = m1553b.getSource().getIndex() + 1;
                mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
            }
            break;
        }
    }
    private void installDiscreetLineParamToUI(int serialsNumber,int serialsType){
        switch (serialsType) {
            case RightLayoutSerials.SERIALS_M429: {
                DiscreetVoltageLine line = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(serialsNumber);
                line.setChannelId(curCh);
                line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                double high = getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh);
                line.setOtherY(ITriggerLine.VoltageLine_High, high);
                line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                double low = getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL + curCh);
                line.setOtherY(ITriggerLine.VoltageLine_Low, low);
                line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
                line.setShowMode(ITriggerLine.ShowMode_Two);
                line.setVisibleLine(false);
                {
                    double lowY = line.getOtherY(ITriggerLine.VoltageLine_Low);
                    double highY = line.getOtherY(ITriggerLine.VoltageLine_High);

                    VoltageLineManage.getInstance().foreachDiscreetLine((otherLine)->{
                        if (otherLine.getShowMode() != ITriggerLine.ShowMode_Two) {
                            if (otherLine.getOtherY(line.getChannelId()) != lowY) {
                                otherLine.setOtherY(line.getChannelId(), lowY);
                            }
                        } else {
                            if (otherLine.getOtherY(ITriggerLine.VoltageLine_High) != highY
                                    && otherLine.getChannelId() == line.getChannelId()) {
                                otherLine.setOtherY(ITriggerLine.VoltageLine_High, highY);
                            }
                            if (otherLine.getOtherY(ITriggerLine.VoltageLine_Low) != lowY
                                    && otherLine.getChannelId() == line.getChannelId()) {
                                otherLine.setOtherY(ITriggerLine.VoltageLine_Low, lowY);
                            }
                        }
                    }, (ln) -> ln != line);

                }

                line.setText(Tools.getChannelLevel(curCh,
                        line.getCurrYIndex() == ITriggerLine.VoltageLine_High ? Tools.LevelType_High : Tools.LevelType_Normal,
                        Tools.LevelMode_Bus));

            }
            break;
            default: {
                DiscreetVoltageLine line = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(serialsNumber);
                line.setChannelId(curCh);
                line.setCurrYIndex(curCh);
                line.setTriggerVoltageLine_logic_state(mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state());
                line.setShowMode(ITriggerLine.ShowMode_One);
                line.setVisibleLine(false);
                TChan.foreachChan((chan) -> {
                    line.setOtherY(chan, WaveManage.get().getPositionY(chan) - getLevelCache(CacheUtil.VALUE_CHANNEL + chan));
                });

                VoltageLineManage.getInstance().foreachDiscreetLine((otherLine) -> {
                    if (otherLine.getShowMode() != ITriggerLine.ShowMode_Two){
                        TChan.foreachChan((chan)->{
                            if (otherLine.getOtherY(chan) != line.getOtherY(chan)
                                    && line.getTriggerVoltageLine_logic_state()[chan] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                otherLine.setOtherY(chan, line.getOtherY(chan));
                            }
                        });

                    }else {
                        TChan.foreachChan((chan)->{
                            if (otherLine.getChannelId() == chan
                                    && otherLine.getOtherY(ITriggerLine.VoltageLine_Low) != line.getOtherY(chan)
                                    && line.getTriggerVoltageLine_logic_state()[chan] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                                otherLine.setOtherY(ITriggerLine.VoltageLine_Low, line.getOtherY(chan));
                            }
                        });

                    }
                }, (ln) -> ln != line);
                line.setChannelId(curCh);
                line.setCurrYIndex(curCh);
                mTriggerLevel.setCurrCh(curCh);
                line.setText(Tools.getChannelLevel(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus));

            }
            break;
        }
    }

    private TriggerValueVoltageLine.ITriggerVoltageLineCallback triggerVoltageLineCallback = new TriggerValueVoltageLine.ITriggerVoltageLineCallback() {
        @Override
        public int OnTriggerLevelNums() {
            return mTriggerLevel.getChangeChannelCount();
        }

        @Override
        public int OnValueLevelNums() {
            if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)) {
                return 0;
            }
            return VoltageLineManage.getInstance().getDiscreetVoltageLineInS1S2Count();
        }


        @Override
        public void switchTriggerValueLevel() {
            TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
            if (triggerValueVoltageLine.isTriggerlevelActive()) {
                triggerValueVoltageLine.setTriggerLevelActive(false);
            } else {
                triggerValueVoltageLine.setTriggerLevelActive(true);
            }
            if (OnTriggerLevelNums() == 0) {
                triggerValueVoltageLine.setTriggerLevelActive(false);
            }
            if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)
                    && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)  ) {
                triggerValueVoltageLine.setTriggerLevelActive(true);
            }
            setTriggerLevelActive(triggerValueVoltageLine.isTriggerlevelActive());
        }
    };

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum = RxBusRegister.parseMqEnum(obj);
        if (mqEnum != MQEnum.CH_ACTIVE) return;
        IChan chan = ((MsgChActiveChange) obj).getChan();
        if (chan != IChan.CH_NULL) {
            TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
            if (!triggerValueVoltageLine.isTriggerlevelActive()) {
                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)
                        && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                        && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)
                        && !CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)
                ) {
                    setTriggerLevelActive(true);
                }
            }

            //解码活动通道改变时，更新触发电平显示
            int achan = ChannelFactory.getChActivate();
            if (TChan.isSerial(TChan.toUiChNo(achan))) {
                switch (achan) {
                    case ChannelFactory.S1:
                        curLevel = curValue1Level;
                        break;
                    case ChannelFactory.S2:
                        curLevel = curValue2Level;
                        break;
                    case ChannelFactory.S3:
                        curLevel = curValue3Level;
                        break;
                    case ChannelFactory.S4:
                        curLevel = curValue4Level;
                        break;
                }
                CacheUtil.get().setValueLevelSerials(TChan.toSerialNumber(TChan.toUiChNo(achan)));
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), TChan.toSerialNumber(TChan.toUiChNo(achan)));
                changeSerialTrigVol_serials(TChan.toUiChNo(achan));
                CacheUtil.get().setValueLevelSerials(0);
                setTriggerLevelActive(false);
                VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
            } else {
                if (mainViewGroup != null && mainViewGroup.isTriggerLevelShow()) {
                    setTriggerLevelActive(true);
                }
            }
        }
    }


    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            visibleSerialTxt = !aBoolean;
            setTriggerLevelVisible(!aBoolean ? View.VISIBLE : View.GONE);
        }
    };

    private void setTriggerLevelActive(boolean bActive) {
        TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
        VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
        triggerValueVoltageLine.setTriggerLevelActive(bActive);
        if (triggerValueVoltageLine.isTriggerlevelActive()) {
            voltageLineManage.setActive(VoltageLineManage.VoltageLineType_Trigger);
        } else {
            voltageLineManage.setActive(VoltageLineManage.VoltageLineType_Value1);
        }

        setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
    }


    private void switchTriggerLevelUp() {
        TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();


        DiscreetVoltageLineInfoBean lineInfoBean;
        VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();

        if (triggerValueVoltageLine.isTriggerlevelActive()) {
            setTriggerLevel();
            mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_TRIGGER);
            mTriggerLevel.setChangeUpChannel();
        } else {
            lineInfoBean = voltageLineManage.setPreDiscreetVoltageLineInS1S2();
            curCh = lineInfoBean.ChannelId;
            setValueLevel();
        }
        setTriggerLevelActive(triggerValueVoltageLine.isTriggerlevelActive());
    }

    private void switchTriggerLevelDown() {
        TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();


        DiscreetVoltageLineInfoBean lineInfoBean;
        VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();

        if (triggerValueVoltageLine.isTriggerlevelActive()) {
            setTriggerLevel();
            mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_TRIGGER);
            mTriggerLevel.setChangeDownChannel();
        } else {
            lineInfoBean = voltageLineManage.setNextDiscreetVoltageLineInS1S2();
            curCh = lineInfoBean.ChannelId;
            setValueLevel();
        }
        setTriggerLevelActive(triggerValueVoltageLine.isTriggerlevelActive());
    }

    private void setTriggerLevel() {
        if (!curLevel.equals(curTriggerLevel)) {
            try {
                msgTrigger.setFromEventBus(false);
                consumerTrigger.accept(msgTrigger);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setValueLevel() {
        DiscreetVoltageLineInfoBean bean = VoltageLineManage.getInstance().getCurrDisCreetVoltageLineInfo();
        if (bean == null) return;
        switch (bean.VoltageLineName) {
            case VoltageLineManage.VoltageLineType_Value1:
                curLevel = curValue1Level;
                break;
            case VoltageLineManage.VoltageLineType_Value2:
                curLevel = curValue2Level;
                break;
            case VoltageLineManage.VoltageLineType_Value3:
                curLevel = curValue3Level;
                break;
            case VoltageLineManage.VoltageLineType_Value4:
                curLevel = curValue4Level;
                break;
        }

//            if (!curLevel.equals(curValue1Level) && !curLevel.equals(curValue2Level)) {
//                curLevel = curValue1Level;
//                MainRightMsgSerialsDetail detail = curLevel.equals(curValue1Level) ? msgSerials1Detail : msgSerials2Detail;
//                detail.setFromEventBus(false);
//                detail.setUnOpenTriggerLevel(true);
//                try {
//                    consumerMainRightSerialsDetail.accept(detail);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
    }

    private Consumer<ExternalKeysMsgLevel> consumerExternalKeysLevel = new Consumer<ExternalKeysMsgLevel>() {
        @Override
        public void accept(ExternalKeysMsgLevel msgLevel) throws Exception {
            int type = msgLevel.getLevelType();
//            double count = msgLevel.getCount() * ScopeBase.getToUICoff();
//            double count = msgLevel.getCount() * ScopeBase.getToUICoff();
            double count = msgLevel.getCount();
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            Log.d(TAG, "type:" + type + ",count:" + count + ",triggerIndex:" + triggerIndex);
            switch (type) {
                case ExternalKeysMsgLevel.TYPE_TRIGGER_MOVECENTER:
                case ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEUP:
                case ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEDOMN: {
                    if (triggerIndex == TopLayoutTrigger.DETAIL_S1
                            || triggerIndex == TopLayoutTrigger.DETAIL_S2
                            || triggerIndex == TopLayoutTrigger.DETAIL_S3
                            || triggerIndex == TopLayoutTrigger.DETAIL_S4
                    ) {
                        return;
                    }
                    VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
                    if (handler.hasMessages(MSG_VALUE1LEVEL_CLOSE)) {
                        handler.removeMessages(MSG_VALUE1LEVEL_CLOSE);
                        String bakKey = voltageLineManage.getKey();
                        ITriggerLine line = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
                        if (line != null && line.getVisibleLine()) {
                            line.setVisibleLine(false);
                        }
                        voltageLineManage.setKey(bakKey);
                    }
                    if (handler.hasMessages(MSG_VALUE2LEVEL_CLOSE)) {
                        handler.removeMessages(MSG_VALUE2LEVEL_CLOSE);
                        String bakKey = voltageLineManage.getKey();
                        ITriggerLine line = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
                        if (line != null && line.getVisibleLine()) {
                            line.setVisibleLine(false);
                        }
                        voltageLineManage.setKey(bakKey);
                    }
                    if (handler.hasMessages(MSG_VALUE3LEVEL_CLOSE)) {
                        handler.removeMessages(MSG_VALUE3LEVEL_CLOSE);
                        String bakKey = voltageLineManage.getKey();
                        ITriggerLine line = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value3);
                        if (line != null && line.getVisibleLine()) {
                            line.setVisibleLine(false);
                        }
                        voltageLineManage.setKey(bakKey);
                    }
                    if (handler.hasMessages(MSG_VALUE4LEVEL_CLOSE)) {
                        handler.removeMessages(MSG_VALUE4LEVEL_CLOSE);
                        String bakKey = voltageLineManage.getKey();
                        ITriggerLine line = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Value4);
                        if (line != null && line.getVisibleLine()) {
                            line.setVisibleLine(false);
                        }
                        voltageLineManage.setKey(bakKey);
                    }
                    changeTriggerPos = true;
                    setTriggerLevel();
                    if (mTriggerLevel.getVisibility() != View.VISIBLE) {
                        return;
                    }
                    mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_TRIGGER);
                    if (type == ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEUP) {
                        mTriggerLevel.setChannelMoveChannel(count);
                    } else if (type == ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEDOMN) {
                        mTriggerLevel.setChannelMoveChannel(count * -1);
                    } else {
                        ITriggerLine triggerLine = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(curCh));
                        if (channel != null) {
                            double pos = (ScopeBase.getNewHeight() * 1.0 / 2 - channel.getPosUI()) - channel.getHalfWaveAmpInPix();
                            pos = Scope.getInstance().convertPos(pos);
                            mTriggerLevel.setChannelMoveChannel(triggerLine.getCurrY() - pos);
                        } else {//外部触发归中间位置，屏幕像素500位置
                            mTriggerLevel.setChannelMoveChannel(triggerLine.getCurrY() - Scope.getInstance().convertPos(ScopeBase.getNewHeight() / 2));
                        }
                    }
                    if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                        handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                    }
                    handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    break;
                }
                case ExternalKeysMsgLevel.TYPE_TRIGGER_SOURCEUP:
//                    if (triggerIndex == TopLayoutTrigger.DETAIL_S1 || triggerIndex == TopLayoutTrigger.DETAIL_S2) {
//                        return;
//                    }
//                    setTriggerLevel();
//                    mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_TRIGGER);
//                    mTriggerLevel.setChangeUpChannel();
                    switchTriggerLevelUp();
                    break;
                case ExternalKeysMsgLevel.TYPE_TRIGGER_SOURCEDOWN:
//                    if (triggerIndex == TopLayoutTrigger.DETAIL_S1 || triggerIndex == TopLayoutTrigger.DETAIL_S2) {
//                        return;
//                    }
//                    setTriggerLevel();
//                    mTriggerLevel.setOpenType(MTriggerLevel.OPENTYPE_TRIGGER);
//                    mTriggerLevel.setChangeDownChannel();
                    switchTriggerLevelDown();
                    break;
                case ExternalKeysMsgLevel.TYPE_VALUE_MOVECENTER:
                case ExternalKeysMsgLevel.TYPE_VALUE_MOVEUP:
                case ExternalKeysMsgLevel.TYPE_VALUE_MOVEDOMN: {

                    VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
                    if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                        handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        String bakKey = voltageLineManage.getKey();
                        ITriggerLine line = voltageLineManage.getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                        if (line != null && line.getVisibleLine()) {
                            line.setVisibleLine(false);
                        }
                        voltageLineManage.setKey(bakKey);
                    }

                    setValueLevel();
                    DiscreetVoltageLineInfoBean lineInfoBean1 = VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
                    if (lineInfoBean1==null) return; //应该不为空的，但是偶尔会为空。打开总线，但是界面上看不到阈值电平。
//                    if (lineInfoBean1.ShowMode != ITriggerLine.ShowMode_Two) {
                    curCh = lineInfoBean1.ChannelId;
//                    }

                    DiscreetVoltageLineInfoBean bean = VoltageLineManage.getInstance().getCurrDisCreetVoltageLineInfo();

                    if (bean == null) return;
                    int openType, msgValue;
                    switch (bean.VoltageLineName) {
                        case VoltageLineManage.VoltageLineType_Value2:
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            msgValue = MSG_VALUE2LEVEL_CLOSE;
                            break;
                        case VoltageLineManage.VoltageLineType_Value3:
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            msgValue = MSG_VALUE3LEVEL_CLOSE;
                            break;
                        case VoltageLineManage.VoltageLineType_Value4:
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            msgValue = MSG_VALUE4LEVEL_CLOSE;
                            break;
                        default:
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            msgValue = MSG_VALUE1LEVEL_CLOSE;
                            break;
                    }
//                    int openType = bean.VoltageLineName.equals(VoltageLineManage.VoltageLineType_Value1) ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2;
                    ITriggerLine valueLine = VoltageLineManage.getInstance().getVoltageLine(bean.VoltageLineName);
                    if (type == ExternalKeysMsgLevel.TYPE_VALUE_MOVEUP) {
                        onMouseMoveListener.onMouseMove(mTriggerLevel, count, bean.ChannelId, openType, true);
                    } else if (type == ExternalKeysMsgLevel.TYPE_VALUE_MOVEDOMN) {
                        onMouseMoveListener.onMouseMove(mTriggerLevel, count * -1, bean.ChannelId, openType, true);
                    } else {
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(curCh));
                        double pos = (ScopeBase.getNewHeight() / 2 - channel.getPosUI()) -  ScopeBase.changeAccuracy(channel.getHalfWaveAmpInPix() * ScopeBase.getToUICoff());
                        pos = Scope.getInstance().convertPos(pos);
                        onMouseMoveListener.onMouseMove(mTriggerLevel, valueLine.getCurrY() - pos, bean.ChannelId, openType, true);
                    }
                    if (handler.hasMessages(msgValue)) {
                        handler.removeMessages(msgValue);
                    }
                    handler.sendEmptyMessageDelayed(msgValue, 800);

                    setS1S2S3S4IconSync(openType);
                    break;
                }
                case ExternalKeysMsgLevel.TYPE_VALUE_SOURCEUP:
                    DiscreetVoltageLineInfoBean lineInfoBean1 = VoltageLineManage.getInstance()
                            .setPreDiscreetVoltageLineInS1S2();
//                    if (lineInfoBean1.ShowMode != ITriggerLine.ShowMode_Two) {
                    curCh = lineInfoBean1.ChannelId;
//                    }
                    setValueLevel();
                    break;
                case ExternalKeysMsgLevel.TYPE_VALUE_SOURCEDOWN:
                    DiscreetVoltageLineInfoBean lineInfoBean2 = VoltageLineManage.getInstance()
                            .setNextDiscreetVoltageLineInS1S2();
//                    if (lineInfoBean2.ShowMode != ITriggerLine.ShowMode_Two) {
                    curCh = lineInfoBean2.ChannelId;
//                    }
                    setValueLevel();
                    break;
            }
        }


    };
    //endregion

    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Exception {
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept RightChannel: %s", rightMsgChannel));
            if (rightMsgChannel.getInvert().isRxMsgSelect()) {//如果当前点击的是反相按钮
                int channelNumber = rightMsgChannel.getChannelNumber();
                ITriggerLine triggerLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                Trigger trigger = TriggerFactory.getInstance().getTrigger();
                if (triggerLine.getChannelId() == channelNumber && triggerLine.getShowMode() == ITriggerLine.ShowMode_Two) {
                    double levelH = getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + channelNumber);
                    double levelL = getLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber, levelH * -1);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + channelNumber, levelL * -1);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + channelNumber, levelH * -1);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + channelNumber, levelL * -1);

                    if (!rightMsgChannel.isFromEventBus()) {
                        trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, channelNumber).setPos(
                                getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + channelNumber), true);
                        trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, channelNumber).setPos(
                                getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber), true);
                    }
                } else {
                    double level = getLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber, level * -1);
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + channelNumber, level * -1);

                    if (!rightMsgChannel.isFromEventBus()) {
                        if ((TChan.toFpgaChNo(channelNumber)) == TriggerFactory.getInstance().getTrigger().getTriggerSource()) {
//                            Log.d(TAG, "accept rec: " + channelNumber + ",trigger source:" + TriggerFactory.getInstance().getTrigger().getTriggerSource());
                            trigger.getTriggerLevel()
                                    .setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + channelNumber), true);
                        }
                    }
                }

                ITriggerLine value1Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
                ITriggerLine value2Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
                ITriggerLine value3Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3);
                ITriggerLine value4Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4);
                if ((value1Line.getChannelId() == channelNumber && value1Line.getShowMode() == ITriggerLine.ShowMode_Two)
                        || (value2Line.getChannelId() == channelNumber && value2Line.getShowMode() == ITriggerLine.ShowMode_Two)
                        || (value3Line.getChannelId() == channelNumber && value3Line.getShowMode() == ITriggerLine.ShowMode_Two)
                        || (value4Line.getChannelId() == channelNumber && value4Line.getShowMode() == ITriggerLine.ShowMode_Two)) {
                    double levelH = getLevelCache(CacheUtil.VALUE_CHANNEL_H + channelNumber);
                    double levelL = getLevelCache(CacheUtil.VALUE_CHANNEL + channelNumber);
                    putLevelCache(CacheUtil.VALUE_CHANNEL + channelNumber, levelH * -1);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_H + channelNumber, levelL * -1);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + channelNumber, levelH * -1);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + channelNumber, levelL * -1);

                    if (!rightMsgChannel.isFromEventBus()) {
                        changeSerialTrigVol_serials(channelNumber);
                    }
                } else {
                    double level = getLevelCache(CacheUtil.VALUE_CHANNEL + channelNumber);
                    putLevelCache(CacheUtil.VALUE_CHANNEL + channelNumber, level * -1);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + channelNumber, level * -1);

                    if (!rightMsgChannel.isFromEventBus()) {
                        changeSerialTrigVol_serials(channelNumber);
                    }
                }

                String tmpCurLevel = curLevel;
                if (mTriggerLevel.getVisibility() == View.VISIBLE) {
                    curLevel = curTriggerLevel;
                    changeTriggerVoltage(curTriggerLevel, rightMsgChannel.isFromEventBus());
                }
                Thread.sleep(100);
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)) {
                    curLevel = curValue1Level;
//                    refreshDiscreetVoltageLineInfo(1, curValue1Level, rightMsgChannel.isFromEventBus());
//                    VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), 1);
                    installDiscreetLineParamToUI(1,indexOf(curValue1Level));
                }
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)) {
                    curLevel = curValue2Level;
//                    refreshDiscreetVoltageLineInfo(2, curValue2Level, rightMsgChannel.isFromEventBus());
//                    VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), 2);
                    installDiscreetLineParamToUI(2,indexOf(curValue2Level));
                }
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3)) {
                    curLevel=curValue3Level;
//                    VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), 3);
                    installDiscreetLineParamToUI(3,indexOf(curValue3Level));
                }
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)) {
                    curLevel=curValue4Level;
//                    VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), 4);
                    installDiscreetLineParamToUI(4,indexOf(curValue4Level));
                }

                curLevel = tmpCurLevel;
            }
        }
    };


    public static void changeSerialTrigVol_channel(int chIdx) { //这里chIdx=IWave.ch1~ch4
        int src = TChan.toFpgaChNo(chIdx);
        Channel channel = ChannelFactory.getDynamicChannel(src);
        if (channel != null) {
            double vol1 = getYTLevelCache(CacheUtil.VALUE_CHANNEL + chIdx);

            //if (vol1 != channel.getBusPrimaryLevel()) //bug	0010182
            {
                if (isValueLevelHighAndLow(chIdx)) {
                    channel.setBusSecondaryLevel(vol1);
                } else {
                    channel.setBusPrimaryLevel(vol1);
                }
            }
            double vol2 = getYTLevelCache(CacheUtil.VALUE_CHANNEL_H + chIdx);

            //if (vol2 != channel.getBusSecondaryLevel()) //bug	0010182
            {
                if (isValueLevelHighAndLow(chIdx)) {
                    channel.setBusPrimaryLevel(vol2);
                } else {
                    channel.setBusSecondaryLevel(vol2);
                }
            }
        }
    }

    /**
     * @param chIdx 这里chIdx=IWave.ch1~ch4
     */
    private boolean isTriggerLevelHighAndLow(int chIdx) {
        return (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_RUNT
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE) == chIdx)
                || (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_SLOPE
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE) == chIdx);
    }

    /**
     * @param uiChan chIdx=IWave.ch1~ch4
     */
    private static boolean isValueLevelHighAndLow(int uiChan) {
//        return (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1) == RightLayoutSerials.SERIALS_M429
//                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S1) == chIdx - 1) ||
//                (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2) == RightLayoutSerials.SERIALS_M429
//                        && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S2) == chIdx - 1)
//                || (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3) == RightLayoutSerials.SERIALS_M429
//                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S3) == chIdx - 1)
//                || (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4) == RightLayoutSerials.SERIALS_M429
//                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S4) == chIdx - 1);
        return RecoveryManage.isValueLevelHighAndLow(uiChan);
    }

    private void changeSerialTrigVol_serials(int seri_idx) { //这里seri_idx=IWave.S1或IWave.S2
        int trigType = Trigger.TRIG_TYPE_SERIAL1;
        if (seri_idx == TChan.S1) {
            trigType = Trigger.TRIG_TYPE_SERIAL1;
        } else if (seri_idx == TChan.S2) {
            trigType = Trigger.TRIG_TYPE_SERIAL2;
        } else if (seri_idx == TChan.S3) {
            trigType = Trigger.TRIG_TYPE_SERIAL3;
        } else if (seri_idx == TChan.S4) {
            trigType = Trigger.TRIG_TYPE_SERIAL4;
        }
        Trigger trigger = TriggerFactory.getTriggerObj(trigType);
        for (int i = 0; i < trigger.getTriggerSourceCnt(); i++) {
            int src = trigger.getTriggerSource(i);
            changeSerialTrigVol_channel(TChan.toUiChNo(src));
        }
    }

    private Consumer<MsgWaveToLevel> consumerWaveToLevel = new Consumer<MsgWaveToLevel>() {
        @Override
        public void accept(MsgWaveToLevel msgWaveToLevel) throws Exception {
            int ch = msgWaveToLevel.getCurCh();
            String tempLevel = curLevel;
            int tempCh = curCh;
            if (msgWaveToLevel.getLevelType() == MsgWaveToLevel.LEVELTYPE_TRIGGER) {
                if (!StrUtil.isEmpty(curTriggerLevel)) {
                    curLevel = curTriggerLevel;
                    curCh = ch;
                    sendMsgTriggerLevel(true, false);
                }
            } else if (msgWaveToLevel.getLevelType() == MsgWaveToLevel.LEVELTYPE_VALUE1) {
                if (!StrUtil.isEmpty(curValue1Level)) {
                    curLevel = curValue1Level;
                    curCh = ch;
                    sendMsgTriggerLevel(true, false);
                }
            } else if (msgWaveToLevel.getLevelType() == MsgWaveToLevel.LEVELTYPE_VALUE2) {
                if (!StrUtil.isEmpty(curValue2Level)) {
                    curLevel = curValue2Level;
                    curCh = ch;
                    sendMsgTriggerLevel(true, false);
                }
            } else if (msgWaveToLevel.getLevelType() == MsgWaveToLevel.LEVELTYPE_VALUE3){
                if (!StrUtil.isEmpty(curValue3Level)) {
                    curLevel = curValue3Level;
                    curCh = ch;
                    sendMsgTriggerLevel(true, false);
                }
            }else if (msgWaveToLevel.getLevelType() == MsgWaveToLevel.LEVELTYPE_VALUE4){
                if (!StrUtil.isEmpty(curValue4Level)) {
                    curLevel = curValue4Level;
                    curCh = ch;
                    sendMsgTriggerLevel(true, false);
                }
            }
            curLevel = tempLevel;
            curCh = tempCh;
        }
    };

    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Throwable {
            //当设置通道scale的时候，需要对应改变触发电平的位置。改变位置和临时值的大小，所以只需要设置界面显示与cache
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept MainRightMsgChannels: %s", msgChannels));

            if (msgChannels.isChangeChState()) return;
            if (msgChannels.isFromEventBus()) return;

            int ch = TChan.Ch1;//scale改变的通道
            double changeScale = 0;//scale改变比例，改变前除以改变后
            if (msgChannels.isCh1ScaleChange()) {
                ch = TChan.Ch1;
                changeScale = msgChannels.getCh1Scale();
            } else if (msgChannels.isCh2ScaleChange()) {
                ch = TChan.Ch2;
                changeScale = msgChannels.getCh2Scale();
            } else if (msgChannels.isCh3ScaleChange()) {
                ch = TChan.Ch3;
                changeScale = msgChannels.getCh3Scale();
            } else if (msgChannels.isCh4ScaleChange()) {
                ch = TChan.Ch4;
                changeScale = msgChannels.getCh4Scale();
            } else if (msgChannels.isCh5ScaleChange()) {
                ch = TChan.Ch5;
                changeScale = msgChannels.getCh5Scale();
            } else if (msgChannels.isCh6ScaleChange()) {
                ch = TChan.Ch6;
                changeScale = msgChannels.getCh6Scale();
            } else if (msgChannels.isCh7ScaleChange()) {
                ch = TChan.Ch7;
                changeScale = msgChannels.getCh7Scale();
            } else if (msgChannels.isCh8ScaleChange()) {
                ch = TChan.Ch8;
                changeScale = msgChannels.getCh8Scale();
            }

            String cacheKeyTempH, cacheKeyTemp, cacheKeyH, cacheKey;
            String curTempLevel = curLevel;
            {
                ITriggerLine iTriggerLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                cacheKeyTempH = CacheUtil.TRIGGER_CHANNEL_TEMP_H;
                cacheKeyTemp = CacheUtil.TRIGGER_CHANNEL_TEMP;
                cacheKeyH = CacheUtil.TRIGGER_CHANNEL_H;
                cacheKey = CacheUtil.TRIGGER_CHANNEL;

                if (iTriggerLine.getShowMode() == ITriggerLine.ShowMode_Two) {
                    double cacheHighNew = changeScale * getLevelCache(cacheKeyTempH + ch);
                    boolean changeHigh = iTriggerLine.setOtherY(ITriggerLine.VoltageLine_High,
                            WaveManage.get().getPositionY(ch) - cacheHighNew);
                    double cacheLowNew = changeScale * getLevelCache(cacheKeyTemp + ch);
                    boolean changeLow = iTriggerLine.setOtherY(ITriggerLine.VoltageLine_Low,
                            WaveManage.get().getPositionY(ch) - cacheLowNew);
                    putLevelCache(cacheKeyTempH + ch, cacheHighNew);
                    putLevelCache(cacheKeyTemp + ch, cacheLowNew);
                    if (!changeHigh) {
                        putLevelCache(cacheKeyH + ch, cacheHighNew);
                    } else {
                        putLevelCache(cacheKeyH + ch,
                                WaveManage.get().getPositionY(ch) - iTriggerLine.getOtherY(ITriggerLine.VoltageLine_High));
                    }
                    if (!changeLow) {
                        putLevelCache(cacheKey + ch, cacheLowNew);
                    } else {
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - iTriggerLine.getOtherY(ITriggerLine.VoltageLine_Low));
                    }
                } else {
                    int verticalMode = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch)).getVerticalMode();
                    double cacheNew = changeScale * getLevelCache(cacheKeyTemp + ch);
                    boolean change = iTriggerLine.setOtherY(ch, WaveManage.get().getPositionY(ch) - cacheNew);
                    putLevelCache(cacheKeyTemp + ch, cacheNew);
                    if (!change || verticalMode == Channel.VERTICAL_MODE_SCREEN_CENTER) {
                        putLevelCache(cacheKey + ch, cacheNew);
                    } else {
                        putLevelCache(cacheKey + ch, WaveManage.get().getPositionY(ch) - iTriggerLine.getOtherY(ch));
                    }
                }
                curLevel = curTriggerLevel;
                msgTrigger.setFromEventBus(msgChannels.isFromEventBus());
                consumerTrigger.accept(msgTrigger);
                sendMsgTriggerLevel(false, msgChannels.isFromEventBus());
            }
            {
                ITriggerLine valueLine1 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
                ITriggerLine valueLine2 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
                ITriggerLine valueLine3 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3);
                ITriggerLine valueLine4 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4);

                cacheKeyTempH = CacheUtil.VALUE_CHANNEL_TEMP_H;
                cacheKeyTemp = CacheUtil.VALUE_CHANNEL_TEMP;
                cacheKeyH = CacheUtil.VALUE_CHANNEL_H;
                cacheKey = CacheUtil.VALUE_CHANNEL;

                double cacheHighNew;
                double cacheLowNew;
                if (CacheUtil.get().isValueInit(cacheKey + ch)) {
                    cacheHighNew = getLevelCache(cacheKeyH + ch);
                    cacheLowNew = getLevelCache(cacheKey + ch);
                } else {
                    cacheHighNew = changeScale * getLevelCache(cacheKeyTempH + ch);
                    cacheLowNew = changeScale * getLevelCache(cacheKeyTemp + ch);
                }
                boolean changeHigh = false;
                boolean changeLow = false;
                if (valueLine1.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if(valueLine1.getChannelId() == ch) {
                        changeHigh = valueLine1.setOtherY(ITriggerLine.VoltageLine_High,
                                WaveManage.get().getPositionY(ch) - cacheHighNew);
                        changeLow = valueLine1.setOtherY(ITriggerLine.VoltageLine_Low,
                                WaveManage.get().getPositionY(ch) - cacheLowNew);
                    }
                } else {
                    changeLow = valueLine1.setOtherY(ch, WaveManage.get().getPositionY(ch) - cacheLowNew);
                }
                if (valueLine2.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if(valueLine2.getChannelId() == ch) {
                        changeHigh = valueLine2.setOtherY(ITriggerLine.VoltageLine_High,
                                WaveManage.get().getPositionY(ch) - cacheHighNew);
                        changeLow = valueLine2.setOtherY(ITriggerLine.VoltageLine_Low,
                                WaveManage.get().getPositionY(ch) - cacheLowNew);
                    }
                } else {
                    changeLow = valueLine2.setOtherY(ch, WaveManage.get().getPositionY(ch) - cacheLowNew);
                }
                if (valueLine3.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if(valueLine3.getChannelId() == ch) {
                        changeHigh = valueLine3.setOtherY(ITriggerLine.VoltageLine_High,
                                WaveManage.get().getPositionY(ch) - cacheHighNew);
                        changeLow = valueLine3.setOtherY(ITriggerLine.VoltageLine_Low,
                                WaveManage.get().getPositionY(ch) - cacheLowNew);
                    }
                } else {
                    changeLow = valueLine3.setOtherY(ch, WaveManage.get().getPositionY(ch) - cacheLowNew);
                }
                if (valueLine4.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if(valueLine4.getChannelId() == ch) {
                        changeHigh = valueLine4.setOtherY(ITriggerLine.VoltageLine_High,
                                WaveManage.get().getPositionY(ch) - cacheHighNew);
                        changeLow = valueLine4.setOtherY(ITriggerLine.VoltageLine_Low,
                                WaveManage.get().getPositionY(ch) - cacheLowNew);
                    }
                } else {
                    changeLow = valueLine4.setOtherY(ch, WaveManage.get().getPositionY(ch) - cacheLowNew);
                }

                putLevelCache(cacheKeyTempH + ch, cacheHighNew);
                putLevelCache(cacheKeyTemp + ch, cacheLowNew);
                if (!changeHigh) {
                    putLevelCache(cacheKeyH + ch, cacheHighNew);
                } else {
                    if (valueLine1.getShowMode() == ITriggerLine.ShowMode_Two) {
                        putLevelCache(cacheKeyH + ch,
                                WaveManage.get().getPositionY(ch) - valueLine1.getOtherY(ITriggerLine.VoltageLine_High));
                    } else if (valueLine2.getShowMode() == ITriggerLine.ShowMode_Two) {
                        putLevelCache(cacheKeyH + ch,
                                WaveManage.get().getPositionY(ch) - valueLine2.getOtherY(ITriggerLine.VoltageLine_High));
                    }else if (valueLine3.getShowMode() == ITriggerLine.ShowMode_Two){
                        putLevelCache(cacheKeyH + ch,
                                WaveManage.get().getPositionY(ch) - valueLine3.getOtherY(ITriggerLine.VoltageLine_High));
                    }else if (valueLine4.getShowMode() == ITriggerLine.ShowMode_Two){
                        putLevelCache(cacheKeyH + ch,
                                WaveManage.get().getPositionY(ch) - valueLine4.getOtherY(ITriggerLine.VoltageLine_High));
                    }
                }
                if (!changeLow) {
                    putLevelCache(cacheKey + ch, cacheLowNew);
                } else {
                    if (valueLine1.getShowMode() == ITriggerLine.ShowMode_Two) {
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - valueLine1.getOtherY(ITriggerLine.VoltageLine_Low));
                    } else if (valueLine2.getShowMode() == ITriggerLine.ShowMode_Two) {
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - valueLine2.getOtherY(ITriggerLine.VoltageLine_Low));
                    }else if (valueLine3.getShowMode() == ITriggerLine.ShowMode_Two){
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - valueLine3.getOtherY(ITriggerLine.VoltageLine_Low));
                    } else if (valueLine4.getShowMode() == ITriggerLine.ShowMode_Two){
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - valueLine4.getOtherY(ITriggerLine.VoltageLine_Low));
                    } else {
                        putLevelCache(cacheKey + ch,
                                WaveManage.get().getPositionY(ch) - valueLine2.getOtherY(ch));
                    }
                }
                curLevel = curValue1Level;
                sendMsgTriggerLevel(false, msgChannels.isFromEventBus());
                curLevel = curValue2Level;
                sendMsgTriggerLevel(false, msgChannels.isFromEventBus());
                curLevel =curValue3Level;
                sendMsgTriggerLevel(false, msgChannels.isFromEventBus());
                curLevel =curValue4Level;
                sendMsgTriggerLevel(false, msgChannels.isFromEventBus());
            }

            curLevel = curTempLevel;

            if (!msgChannels.isFromEventBus()) {
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
                    || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4)) {
                    changeSerialTrigVol_channel(ch);
                }
            }
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
//            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept MainRightMsgOthers: %s", mainRightMsgOthers));
            boolean s1 = mainRightMsgOthers.getS1().isValue();
            boolean s2 = mainRightMsgOthers.getS2().isValue();
            boolean s3 = mainRightMsgOthers.getS3().isValue();
            boolean s4 = mainRightMsgOthers.getS4().isValue();
            ITriggerLine value1Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
            ITriggerLine value2Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
            ITriggerLine value3Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3);
            ITriggerLine value4Line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4);
            if (value1Line.getShowState() == s1 && value2Line.getShowState() == s2
                    && value3Line.getShowState() == s3 && value4Line.getShowState() == s4) {
//                Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.accept: [mainRightMsgOthers] return" ));
                return;
            }
            value1Line.setShowState(s1);
            value2Line.setShowState(s2);
            value3Line.setShowState(s3);
            value4Line.setShowState(s4);

            if(mainRightMsgOthers.getS1().isRxMsgSelect()){
                serialsNumber = 1;
            }else if(mainRightMsgOthers.getS2().isRxMsgSelect()){
                serialsNumber = 2;
            }else if(mainRightMsgOthers.getS3().isRxMsgSelect()){
                serialsNumber = 3;
            }else if(mainRightMsgOthers.getS4().isRxMsgSelect()){
                serialsNumber = 4;
            }

            if (msgSerials1Detail != null && s1) {
                curLevel = curValue1Level;
                CacheUtil.get().setValueLevelSerials(CacheUtil.S1);
                RightMsgSerials msgSerials = msgSerials1Detail.getRightMsgSerials();
//                setParam(msgSerials);
//                refreshDiscreetVoltageLineInfo(msgSerials.getSerialsNumber(), curValue1Level, false);
                installParamToUI(msgSerials);
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), msgSerials.getSerialsNumber());
                //打开串行通道需要修改阈值电平
                changeSerialTrigVol_serials(TChan.S1);
                CacheUtil.get().setValueLevelSerials(0);

            }
            if (msgSerials2Detail != null && s2) {
                curLevel = curValue2Level;
                CacheUtil.get().setValueLevelSerials(CacheUtil.S2);
                RightMsgSerials msgSerials = msgSerials2Detail.getRightMsgSerials();
//                setParam(msgSerials);
//                refreshDiscreetVoltageLineInfo(msgSerials.getSerialsNumber(), curValue2Level, false);
                installParamToUI(msgSerials);
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), msgSerials.getSerialsNumber());
                //打开串行通道需要修改阈值电平
                changeSerialTrigVol_serials(TChan.S2);
                CacheUtil.get().setValueLevelSerials(0);
            }
            if (msgSerials3Detail != null && s3) {
                curLevel = curValue3Level;
                CacheUtil.get().setValueLevelSerials(CacheUtil.S3);
                RightMsgSerials msgSerials = msgSerials3Detail.getRightMsgSerials();
//                setParam(msgSerials);
//                refreshDiscreetVoltageLineInfo(msgSerials.getSerialsNumber(), curValue2Level, false);
                installParamToUI(msgSerials);
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), msgSerials.getSerialsNumber());
                //打开串行通道需要修改阈值电平
                changeSerialTrigVol_serials(TChan.S3);
                CacheUtil.get().setValueLevelSerials(0);
            }
            if (msgSerials4Detail != null && s4) {
                curLevel = curValue4Level;
                CacheUtil.get().setValueLevelSerials(CacheUtil.S4);
                RightMsgSerials msgSerials = msgSerials4Detail.getRightMsgSerials();
//                setParam(msgSerials);
//                refreshDiscreetVoltageLineInfo(msgSerials.getSerialsNumber(), curValue2Level, false);
                installParamToUI(msgSerials);
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(getShowState(), msgSerials.getSerialsNumber());
                //打开串行通道需要修改阈值电平
                changeSerialTrigVol_serials(TChan.S4);
                CacheUtil.get().setValueLevelSerials(0);
            }

            if (!s1 && !s2 && !s3 && !s4) {
                mTriggerLevel.setCurrCh(mTriggerLevel.getCurrCh());
                setTriggerLevelActive(true);
            } else {
                setTriggerLevelActive(false);
                VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
            }
        }

        private void setParam(RightMsgSerials msgSerials) {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(ChannelFactory.toFactorySerial(msgSerials.getSerialsNumber()));
            if (serialChannel == null) return;
            serialChannel.setPos(getLevelCache(CacheUtil.VALUE_CHANNEL + curCh));
            switch (msgSerials.getSerialsType().getIndex()) {
                case RightLayoutSerials.SERIALS_UART: {
                    curLevel = LEVEL_VALUE_UART;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_UART;
                    } else if (msgSerials.getSerialsNumber() == 2) {
                        curValue2Level = LEVEL_VALUE_UART;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_UART;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_UART;
                    }
                    RightMsgSerialsUart uart = (RightMsgSerialsUart) msgSerials.getSerialsDetails();
                    curCh = uart.getRx().getIndex() + 1;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
                }
                break;
                case RightLayoutSerials.SERIALS_LIN: {
                    curLevel = LEVEL_VALUE_LIN;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_LIN;
                    } else if (msgSerials.getSerialsNumber() == 2){
                        curValue2Level = LEVEL_VALUE_LIN;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_LIN;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_LIN;
                    }
                    RightMsgSerialsLin lin = (RightMsgSerialsLin) msgSerials.getSerialsDetails();
                    curCh = lin.getSource().getIndex() + 1;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
                }
                break;
                case RightLayoutSerials.SERIALS_CAN: {
                    curLevel = LEVEL_VALUE_CAN;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_CAN;
                    } else if (msgSerials.getSerialsNumber() == 2) {
                        curValue2Level = LEVEL_VALUE_CAN;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_CAN;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_CAN;
                    }
                    RightMsgSerialsCan can = (RightMsgSerialsCan) msgSerials.getSerialsDetails();
                    curCh = can.getSource().getIndex() + 1;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
                }
                break;
                case RightLayoutSerials.SERIALS_SPI: {
                    curLevel = LEVEL_VALUE_SPI;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_SPI;
                    } else if (msgSerials.getSerialsNumber() == 2){
                        curValue2Level = LEVEL_VALUE_SPI;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_SPI;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_SPI;
                    }
                    RightMsgSerialsSpi spi = (RightMsgSerialsSpi) msgSerials.getSerialsDetails();
                    spiClockIndex = spi.getClk().getIndex() + 1;
                    spiDataIndex = spi.getData().getIndex() + 1;
                    spiCsIndex = spi.getCs().getIndex() + 1;
                    spiCsEnable = spi.getCsSwitch().isValue();
                    curCh = spiClockIndex;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    if (spiCsEnable == true)
                        mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh, spiDataIndex, spiCsIndex);
                    else
                        mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh, spiDataIndex);
                }
                break;
                case RightLayoutSerials.SERIALS_I2C: {
                    curLevel = LEVEL_VALUE_I2C;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_I2C;
                    } else if (msgSerials.getSerialsNumber() == 2) {
                        curValue2Level = LEVEL_VALUE_I2C;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_I2C;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_I2C;
                    }
                    RightMsgSerialsI2c i2c = (RightMsgSerialsI2c) msgSerials.getSerialsDetails();
                    i2cDataIndex = i2c.getSda().getIndex() + 1;
                    i2cClockIndex = i2c.getScl().getIndex() + 1;
                    curCh = i2cDataIndex;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh, i2cClockIndex);
                }
                break;
                case RightLayoutSerials.SERIALS_M429: {
                    curLevel = LEVEL_VALUE_M429;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_M429;
                    } else if (msgSerials.getSerialsNumber() == 2) {
                        curValue2Level = LEVEL_VALUE_M429;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_M429;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_M429;
                    }
                    RightMsgSerialsM429 m429 = (RightMsgSerialsM429) msgSerials.getSerialsDetails();
                    curCh = m429.getSource().getIndex() + 1;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_HighLow);
                    mTriggerLevel.setCurrCh(curCh);
                }
                break;
                case RightLayoutSerials.SERIALS_M1553B: {
                    curLevel = LEVEL_VALUE_M1553B;
                    if (msgSerials.getSerialsNumber() == 1) {
                        curValue1Level = LEVEL_VALUE_M1553B;
                    } else if (msgSerials.getSerialsNumber() == 2) {
                        curValue2Level = LEVEL_VALUE_M1553B;
                    } else if (msgSerials.getSerialsNumber() == 3) {
                        curValue3Level = LEVEL_VALUE_M1553B;
                    } else if (msgSerials.getSerialsNumber() == 4) {
                        curValue4Level = LEVEL_VALUE_M1553B;
                    }
                    RightMsgSerialsM1553b m1553b = (RightMsgSerialsM1553b) msgSerials.getSerialsDetails();
                    curCh = m1553b.getSource().getIndex() + 1;
                    mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
                    mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
                }
                break;
            }
        }
    };

    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            if (mainTopMsgRightGone == null) return;
            visibleTopRight = mainTopMsgRightGone.isVisible();
            setTriggerLevelVisible(View.VISIBLE);
        }
    };

    private void setTriggerLevelVisible(int visible) {
        if (visibleTopRight && visibleTriggerVideo && visibleYT && visibleSerialTxt && visible == View.VISIBLE) {
            mTriggerLevel.setVisibility(View.VISIBLE);
        } else {
            mTriggerLevel.setVisibility(View.GONE);
        }
    }

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                case IWorkMode.WorkMode_YTZOOM:
                    visibleYT = true;
                    updateLevelValue();
                    break;
                case IWorkMode.WorkMode_XY:
                    visibleYT = false;
                    break;
            }
            MainHolderTriggerLevel.this.setTriggerLevelVisible(visibleYT ? View.VISIBLE : View.GONE);
        }
    };

    //更新触发电平值，使SCPI查询到的是最新值
    private void updateLevelValue() {
        ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine(MTriggerLevel.OPENTYPE_TRIGGER);
        onMouseMoveForFunc(line);
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {

            //region Command
            CacheUtil.get().setValueLevelSerials(serialsNumber);
            int chTriggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);
            int curTriggerIndex = TChan.toUiChNo(chTriggerIndex);
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_HLEVEL: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                    if (curLevel.equals(LEVEL_TRIGGER_SLOPE) && line.getCurrYIndex() == ITriggerLine.VoltageLine_High) {
                        double deltaY = getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curTriggerIndex));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSLOPE_LLEVEL: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                    if (curLevel.equals(LEVEL_TRIGGER_SLOPE) && line.getCurrYIndex() == ITriggerLine.VoltageLine_Low) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERRUNT_HLEVEL: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                    if (curLevel.equals(LEVEL_TRIGGER_RUNT) && line.getCurrYIndex() == ITriggerLine.VoltageLine_High) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERRUNT_LLEVEL: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                    if (curLevel.equals(LEVEL_TRIGGER_RUNT) && line.getCurrYIndex() == ITriggerLine.VoltageLine_Low) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLOGIC_LEVEL: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
//                    Logger.i(Command.TAG,"name:"+curLevel+",curY:"+(line.getCurrYIndex()-1)+",param0:"+params[0]);
                    line.setCurrYIndex(Integer.parseInt(params[0]) + 1);
                    curCh = Integer.parseInt(params[0]) + 1;
                    if (curLevel.equals(LEVEL_TRIGGER_LOGIC) && line.getCurrYIndex() - 1 == Integer.parseInt(params[0])) {
                        double deltaY =  (getPxFromV(curCh, Double.parseDouble(params[1]))
                                - (getCurCh() - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curCh, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGEREDGE_LEVEL: {
                    if (curLevel.equals(LEVEL_TRIGGER_EDGE)) {
                        double deltaY=0;
                        if (curTriggerIndex==9){
                            ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine(MTriggerLevel.OPENTYPE_TRIGGER);
                            deltaY=(getPxFromVAux(Double.valueOf(commandMsgToUI.getParam())))
                                    - line.getCurrY();
                        } else {
                            double temPix = getPxFromV(curTriggerIndex, Double.valueOf(commandMsgToUI.getParam()));
                            if (Scope.getInstance().isZoom()) {
                                temPix += getReviseVal(curTriggerIndex, Double.valueOf(commandMsgToUI.getParam()));
                            }
                            deltaY = (temPix
                                    - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        }
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERNEDGE_LEVEL: {
                    if (curLevel.equals(LEVEL_TRIGGER_NEDGE)) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERPULSE_LEVEL: {
                    if (curLevel.equals(LEVEL_TRIGGER_PULSEWIDTH)) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERTIMEOUT_LEVEL: {
                    if (curLevel.equals(LEVEL_TRIGGER_TIMEOUT)) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(commandMsgToUI.getParam()))
                                - (getChPos(curTriggerIndex) - Tools.getLevelCache(CacheUtil.TRIGGER_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, MTriggerLevel.OPENTYPE_TRIGGER, true);
                        if (handler.hasMessages(MSG_TRIGGERLEVEL_CLOSE)) {
                            handler.removeMessages(MSG_TRIGGERLEVEL_CLOSE);
                        }
                        handler.sendEmptyMessageDelayed(MSG_TRIGGERLEVEL_CLOSE, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_LEVELHIGH: {
                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE) + TChan.Ch1;
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_M429;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_M429;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_M429;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_M429;
                            break;
                    }
                    if (line.getCurrYIndex() != ITriggerLine.VoltageLine_High) {
                        line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                        mTriggerLevel.setChangeDownChannel();
                        onMouseMoveListener.onDownClick(mTriggerLevel, curTriggerIndex, openType);
                    }
                    line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                    line.setChannelId(ch + 1);
                    curCh = ch + 1;
                    curLevel = LEVEL_VALUE_M429;
                    if (curLevel.equals(LEVEL_VALUE_M429) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == ITriggerLine.VoltageLine_High) {
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL_H + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_LEVELLOW: {
//                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE)+IWave.Ch1;
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_M429;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_M429;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_M429;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_M429;
                            break;
                    }
                    if (line.getCurrYIndex() != ITriggerLine.VoltageLine_Low) {
                        line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                        mTriggerLevel.setChangeDownChannel();
                        onMouseMoveListener.onDownClick(mTriggerLevel, curTriggerIndex, openType);
                    }
                    line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                    line.setChannelId(ch + 1);
                    curCh = ch + 1;
                    curLevel = LEVEL_VALUE_M429;
                    if (curLevel.equals(LEVEL_VALUE_M429) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == ITriggerLine.VoltageLine_Low) {

                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);
                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                        }

                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCLOCK: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);

                    if (mTriggerLevel.getCurrCh() != spiClockIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    if (mTriggerLevel.getCurrCh() != spiClockIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    line.setCurrYIndex(spiClockIndex);
                    curLevel = LEVEL_VALUE_SPI;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_SPI;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_SPI;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_SPI;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_SPI;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_SPI) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == spiClockIndex) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(spiClockIndex, Double.parseDouble(params[1]))
                                - (getChPos(spiClockIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + spiClockIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, spiClockIndex, openType, true);
                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_LEVELDATA: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);

                    if (mTriggerLevel.getCurrCh() != spiDataIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    if (mTriggerLevel.getCurrCh() != spiDataIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    line.setCurrYIndex(spiDataIndex);
                    curLevel = LEVEL_VALUE_SPI;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_SPI;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_SPI;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_SPI;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_SPI;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_SPI) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == spiDataIndex) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(spiDataIndex, Double.parseDouble(params[1]))
                                - (getChPos(spiDataIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + spiDataIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, spiDataIndex,openType, true);
                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_LEVELCS: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    // spi cs预值电平是否打开
                    boolean csEnable = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber);
                    int csCh = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber);
//                    Logger.i(Command.TAG,"csEnable:"+csEnable+",csCh:"+csCh+",ch:"+ch);
                    if (csEnable == false) return;
                    if (csCh != ch) return;

                    if (mTriggerLevel.getCurrCh() != spiCsIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    if (mTriggerLevel.getCurrCh() != spiCsIndex) {
                        mTriggerLevel.setChangeDownChannel();
                    }
                    line.setCurrYIndex(spiCsIndex);
                    curLevel = LEVEL_VALUE_SPI;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_SPI;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_SPI;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_SPI;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_SPI;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_SPI) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == spiCsIndex) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(spiCsIndex, Double.parseDouble(params[1]))
                                - (getChPos(spiCsIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + spiCsIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, spiCsIndex, openType, true);
                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERIIC_LEVELDATA: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);

                    if (mTriggerLevel.getCurrCh() != i2cDataIndex) {
                        mTriggerLevel.setChangeDownChannel();
                        line.setCurrYIndex(i2cDataIndex);
//                        onMouseMoveListener.onDownClick(mTriggerLevel, i2cDataIndex, serialsNumber == 1 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                    }
                    curLevel = LEVEL_VALUE_I2C;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_I2C;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_I2C;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_I2C;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_I2C;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_I2C) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == i2cDataIndex) {

                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(i2cDataIndex, Double.parseDouble(params[1]))
                                - (getChPos(i2cDataIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + i2cDataIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, i2cDataIndex, openType, true);
                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERIIC_LEVELCLOCK: {
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);

                    if (mTriggerLevel.getCurrCh() != i2cClockIndex) {
                        mTriggerLevel.setChangeDownChannel();
                        line.setCurrYIndex(i2cClockIndex);
//                        onMouseMoveListener.onDownClick(mTriggerLevel, i2cClockIndex, serialsNumber == 1 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                    }
                    curLevel = LEVEL_VALUE_I2C;

                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_I2C;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_I2C;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_I2C;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_I2C;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_I2C) && serialNo == serialsNumber - 1 && line.getCurrYIndex() == i2cClockIndex) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(i2cClockIndex, Double.parseDouble(params[1]))
                                - (getChPos(i2cClockIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + i2cClockIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, i2cClockIndex, openType, true);

                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_LEVEL: {
//                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX)+ IWave.Ch1;
                    ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine();
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    curTriggerIndex = TChan.toUiChNo(ch);
                    serialsNumber = serialNo + 1;
                    //Logger.i(Command.TAG,"-------------------------------------------------");
                    //Logger.i(Command.TAG,"curTriggerIndex:"+curTriggerIndex+",ch:"+ch);
                    curLevel = LEVEL_VALUE_UART;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_UART;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_UART;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_UART;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_UART;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_UART) && serialNo == serialsNumber - 1) {
                        //Logger.d(Command.TAG,CacheUtil.VALUE_INIT + serialsNumber + 0 + curTriggerIndex + ":" + CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serialsNumber + 0 + curTriggerIndex));
                        curCh = curTriggerIndex;
//                        mTriggerLevel.setOpenType(serialsNumber==1?MTriggerLevel.OPENTYPE_SERIALS1:MTriggerLevel.OPENTYPE_SERIALS2);
//                        mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
//                        mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
//                        VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

//                        Logger.i(Command.TAG,"key:"+(CacheUtil.VALUE_CHANNEL + curTriggerIndex));
//                        Logger.i(Command.TAG,"curCh:"+curCh);
//                        Logger.i(Command.TAG,"绝对阈值位置(px):"+(int) (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))));
//                        Logger.i(Command.TAG,"通道位置(px):"+(getChPos(curTriggerIndex)));
//                        Logger.i(Command.TAG,"当前阈值位置(px)："+getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex));
                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);

                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_LEVEL: {
//                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE)+ IWave.Ch1;
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    curLevel = LEVEL_VALUE_LIN;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_LIN;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_LIN;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_LIN;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_LIN;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_LIN) && Integer.parseInt(params[0]) == serialsNumber - 1) {
                        curCh = curTriggerIndex;
//                        mTriggerLevel.setOpenType(serialsNumber==1?MTriggerLevel.OPENTYPE_SERIALS1:MTriggerLevel.OPENTYPE_SERIALS2);
//                        mTriggerLevel.setTriggerLevel_Mode_Work(MTriggerLevel.TriggerLevel_Mode_Work_Logic);
//                        mTriggerLevel.setTriggerLevel_Mode_Work_Logic_states(curCh);
//                        VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);

                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_LEVEL: {
//                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE)+ IWave.Ch1;
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    curLevel = LEVEL_VALUE_CAN;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_CAN;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_CAN;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_CAN;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_CAN;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_CAN) && serialNo == serialsNumber - 1) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);

                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }

                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM1553B_LEVEL: {
//                    curTriggerIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE)+ IWave.Ch1;
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialNo = (Integer.parseInt(params[0]));
                    int ch = Integer.parseInt(params[2]);
                    serialsNumber = serialNo + 1;
                    curTriggerIndex = TChan.toUiChNo(ch);
                    curLevel = LEVEL_VALUE_M1553B;
                    int msgWhat;
                    int openType;
                    switch (serialsNumber) {
                        case 2:
                            msgWhat = MSG_VALUE2LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS2;
                            curValue2Level = LEVEL_VALUE_M1553B;
                            break;
                        case 3:
                            msgWhat = MSG_VALUE3LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS3;
                            curValue3Level = LEVEL_VALUE_M1553B;
                            break;
                        case 4:
                            msgWhat = MSG_VALUE4LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS4;
                            curValue4Level = LEVEL_VALUE_M1553B;
                            break;
                        default:
                            msgWhat = MSG_VALUE1LEVEL_CLOSE;
                            openType = MTriggerLevel.OPENTYPE_SERIALS1;
                            curValue1Level = LEVEL_VALUE_M1553B;
                            break;
                    }
                    if (curLevel.equals(LEVEL_VALUE_M1553B) && serialNo == serialsNumber - 1) {
                        curCh = curTriggerIndex;
                        CacheUtil.get().setValueLevelSerials(serialsNumber);
                        switchSerialId(serialNo);

                        double deltaY = (getPxFromV(curTriggerIndex, Double.parseDouble(params[1]))
                                - (getChPos(curTriggerIndex) - getLevelCache(CacheUtil.VALUE_CHANNEL + curTriggerIndex)));
                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex, openType, true);
//                        onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
//                                , MTriggerLevel.getOpenType(serialsNumber), true);

                        if (Command_Bus.isChannelExistOtherSerialNum(serialNo, ch)) {
                            onMouseMoveListener.onMouseMove(mTriggerLevel, -deltaY, curTriggerIndex
                                    , serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2, true);
                            onMouseMoveListener.onMouseMoveComplete(null, serialsNumber == 2 ? MTriggerLevel.OPENTYPE_SERIALS1 : MTriggerLevel.OPENTYPE_SERIALS2);
                        }
                        if (handler.hasMessages(msgWhat)) {
                            handler.removeMessages(msgWhat);
                        }
                        handler.sendEmptyMessageDelayed(msgWhat, 800);
                    }
                    break;
                }
            }
            CacheUtil.get().setValueLevelSerials(0);
            //endregion
        }
    };

    private String getShowState() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        String showState;
        showState = s1Check ? VoltageLineManage.VoltageLineType_Value1 : "";
        showState += s2Check ? VoltageLineManage.VoltageLineType_Value2 : "";
        showState += s3Check ? VoltageLineManage.VoltageLineType_Value3 : "";
        showState += s4Check ? VoltageLineManage.VoltageLineType_Value4 : "";
        return showState;
    }

    private void switchSerialId(int scpi_serialNo) {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);

        if (s1Check || s2Check || s3Check || s4Check) {
            int type = scpi_serialNo + MTriggerLevel.OPENTYPE_SERIALS1;

            boolean s1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean s2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean s3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean s4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);

//            String key = type == MTriggerLevel.OPENTYPE_SERIALS1 ? VoltageLineManage.VoltageLineType_Value1 : VoltageLineManage.VoltageLineType_Value2;

            //显示S1S2的图标
            String showState;
            showState = s1 ? VoltageLineManage.VoltageLineType_Value1 : "";
            showState += s2 ? VoltageLineManage.VoltageLineType_Value2 : "";
            showState += s3 ? VoltageLineManage.VoltageLineType_Value3 : "";
            showState += s4 ? VoltageLineManage.VoltageLineType_Value4 : "";
            VoltageLineManage.getInstance().setDiscreetVoltageShowState(showState, type);

//        if (type == MTriggerLevel.OPENTYPE_SERIALS1 || type == MTriggerLevel.OPENTYPE_SERIALS2) {
//            setS1S2IconSync(type);
//        }
            VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
        }
    }


    /**
     * 电压档转像素
     *
     * @param d
     * @return
     */
    private double getPxFromV(int iWaveCh, double d) {
        Channel ch = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(iWaveCh));
        double perD = 0;
        double posPix = 0;
        if (ch != null) {


//            perD = ch.getVerticalPerPix(ch.getVScaleId());
            perD = ch.getADVerticalPerPix();
            double chPos = WaveManage.get().getPositionY(iWaveCh);
            posPix = chPos - (d / perD);
                            Logger.i("command","chpos:"+chPos+"  trigPix:"+posPix+" 位置："+(d/perD));
        }
        return posPix;
    }

    private double getReviseVal(int iWaveCh, double d) { //0011326 zoom下的补偿值，配合getPxFromV使用
        Channel ch = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(iWaveCh));
        double perD = 0;
        double posPix = 0;
        if (ch != null) {
            perD = ch.getADVerticalPerPix();
            posPix = d / perD * (1 - GlobalVar.get().toZoomCoef());
        }
        return posPix;
    }

    private double getPxFromVAux(double d)
    {
        double val = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_500mV) ;
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();
        if(Scope.getInstance().isZoom()){
            h = ScopeBase.getZoomVerticalPerGridPixels() * ScopeBase.getToUICoff();
        }
        double perD=val/h;
        double posPix=0;
        double chPos=GlobalVar.get().getMainWave().y;

        posPix = chPos - (d / perD);
        return posPix;
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
//            if (RecoveryManage.getIns().isLoading()) return;

            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_TRIGGER_LEVEL) {
                Trigger trigger = TriggerFactory.getInstance().getTrigger();
                ITriggerLine triggerLine = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                int tmpCh = curCh;
                String tmpLevel = curLevel;
                curLevel = curTriggerLevel;
                TChan.foreachChan((i) -> {
                    curCh = i;
                    if (isTriggerLevelHighAndLow(i)) {
                        TriggerLevel triggerHigh = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, TChan.toFpgaChNo(curCh));
                        TriggerLevel triggerLow = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, TChan.toFpgaChNo(curCh));
                        if (triggerHigh == null || triggerLow == null) return;
                        double posH = triggerHigh.getPosUI();
                        double posL = triggerLow.getPosUI();
                        boolean change = false;
                        if (triggerLine.getOtherY(ITriggerLine.VoltageLine_High) != WaveManage.get().getPositionY(curCh) - posH) {
                            change = true;
                            triggerLine.setOtherY(ITriggerLine.VoltageLine_High, WaveManage.get().getPositionY(curCh) - posH);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh, posH);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + curCh, posH);
                        }
                        if (triggerLine.getOtherY(ITriggerLine.VoltageLine_Low) != WaveManage.get().getPositionY(curCh) - posL) {
                            change = true;
                            triggerLine.setOtherY(ITriggerLine.VoltageLine_Low, WaveManage.get().getPositionY(curCh) - posL);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, posL);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, posL);
                        }
                        if (change) {
                            sendMsgTriggerLevel(false, true);
                        }
                    } else {
                        TriggerLevel triggerNormal = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_NORMAL, TChan.toFpgaChNo(curCh));
                        if (triggerNormal == null) return;
                        double pos = triggerNormal.getPosUI();
                        if (triggerLine.getOtherY(curCh) != WaveManage.get().getPositionY(curCh) - pos) {
                            triggerLine.setOtherY(curCh, WaveManage.get().getPositionY(curCh) - pos);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, pos);
                            putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, pos);
                            sendMsgTriggerLevel(false, true);
                        }
                    }
                });
                curCh = tmpCh;
                curLevel = tmpLevel;
                triggerLine.setCurrYIndex(curCh);
                mTriggerLevel.setCurrCh(curCh);
                sendMsgTriggerLevel(false, true);
            } else if (eventBase.getId() == EventFactory.EVENT_BUS_LEVEL) {
//                ITriggerLine valueLine1 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
//                ITriggerLine valueLine2 = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
                int tmpCh = curCh;
                String tmpLevel = curLevel;
//                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1)) {
//                    curLevel = curValue1Level;
//                    if (valueLine1.getShowMode() == ITriggerLine.ShowMode_Two) {
//                        //1-4
//                        int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S1) + TChan.Ch1;
//                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
//                        int posH = Tools.YT2Zoom(channel.getBusPrimaryLevel());
//                        int posL = Tools.YT2Zoom(channel.getBusSecondaryLevel());
//                        boolean change = false;
//                        if (valueLine1.getOtherY(ITriggerLine.VoltageLine_High) != WaveManage.get().getPositionY(ch) - posH) {
//                            change = true;
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_H + ch, posH);
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + ch, posH);
//                            valueLine1.setOtherY(ITriggerLine.VoltageLine_High, WaveManage.get().getPositionY(ch) - posH);
//                        }
//                        if (valueLine1.getOtherY(ITriggerLine.VoltageLine_Low) != WaveManage.get().getPositionY(ch) - posL) {
//                            change = true;
//                            putLevelCache(CacheUtil.VALUE_CHANNEL + ch, posL);
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + ch, posL);
//                            valueLine1.setOtherY(ITriggerLine.VoltageLine_Low, WaveManage.get().getPositionY(ch) - posL);
//                        }
//                        if (change) {
//                            curCh = ch;
//                            sendMsgTriggerLevel(false, true);
//                        }
//                    } else {
//                        TChan.foreachChan((i) -> {
//                            Channel channel = ChannelFactory.getDynamicChannel(i - 1);
//                            int pos;
//                            if (!isValueLevelHighAndLow(i)) {
//                                pos = Tools.YT2Zoom(channel.getBusPrimaryLevel());
//                            } else {
//                                pos = Tools.YT2Zoom(channel.getBusSecondaryLevel());
//                            }
//                            if (valueLine1.getOtherY(i) != WaveManage.get().getPositionY(i) - pos) {
//                                putLevelCache(CacheUtil.VALUE_CHANNEL + i, pos);
//                                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + i, pos);
//                                valueLine1.setOtherY(i, WaveManage.get().getPositionY(i) - pos);
//                            }
//                        });
//
//                        sendMsgTriggerLevel(false, true);
//                    }
//                }
//
//                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)) {
//                    curLevel = curValue2Level;
//                    if (valueLine2.getShowMode() == ITriggerLine.ShowMode_Two) {
//                        //1-4
//                        int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + CacheUtil.S2) + TChan.Ch1;
//                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
//                        int posH = Tools.YT2Zoom(channel.getBusPrimaryLevel());
//                        int posL = Tools.YT2Zoom(channel.getBusSecondaryLevel());
//                        boolean change = false;
//                        if (valueLine2.getOtherY(ITriggerLine.VoltageLine_High) != WaveManage.get().getPositionY(ch) - posH) {
//                            change = true;
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_H + ch, posH);
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + ch, posH);
//                            valueLine2.setOtherY(ITriggerLine.VoltageLine_High, WaveManage.get().getPositionY(ch) - posH);
//                        }
//                        if (valueLine2.getOtherY(ITriggerLine.VoltageLine_Low) != WaveManage.get().getPositionY(ch) - posL) {
//                            change = true;
//                            putLevelCache(CacheUtil.VALUE_CHANNEL + ch, posL);
//                            putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + ch, posL);
//                            valueLine2.setOtherY(ITriggerLine.VoltageLine_Low, WaveManage.get().getPositionY(ch) - posL);
//                        }
//                        if (change) {
//                            curCh = ch;
//                            sendMsgTriggerLevel(false, true);
//                        }
//                    } else {
//                        TChan.foreachChan((i) -> {
//                            Channel channel = ChannelFactory.getDynamicChannel(i - 1);
//                            int pos;
//                            if (!isValueLevelHighAndLow(i)) {
//                                pos = Tools.YT2Zoom(channel.getBusPrimaryLevel());
//                            } else {
//                                pos = Tools.YT2Zoom(channel.getBusSecondaryLevel());
//                            }
//                            if (valueLine2.getOtherY(i) != WaveManage.get().getPositionY(i) - pos) {
//                                putLevelCache(CacheUtil.VALUE_CHANNEL + i, pos);
//                                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + i, pos);
//                                valueLine2.setOtherY(i, WaveManage.get().getPositionY(i) - pos);
//                            }
//                        });
//                        sendMsgTriggerLevel(false, true);
//                    }
//                }

                for(int i=CacheUtil.S1;i<=CacheUtil.S4;i++){
                    eventUpdateDiscreetLine(i);
                }


                curCh = tmpCh;
                curLevel = tmpLevel;
            }
        }
    };

    private void eventUpdateDiscreetLine(int serialsNumber){
        ITriggerLine valueLine=VoltageLineManage.getInstance().getVoltageLine(serialsNumber);
        boolean isOpen=false;
        if (serialsNumber==CacheUtil.S1){
            isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        }else if (serialsNumber==CacheUtil.S2){
            isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        }else if (serialsNumber==CacheUtil.S3){
            isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        }else if (serialsNumber==CacheUtil.S4){
            isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        }

        if (isOpen) {
            curLevel = curValue1Level;
            if (serialsNumber== CacheUtil.S1){
                curLevel=curValue1Level;
            }else if (serialsNumber==CacheUtil.S2){
                curLevel=curValue2Level;
            }else if (serialsNumber==CacheUtil.S3){
                curLevel=curValue3Level;
            }else if (serialsNumber==CacheUtil.S4){
                curLevel=curValue4Level;
            }

            if (valueLine.getShowMode() == ITriggerLine.ShowMode_Two) {
                //1-4
                int ch = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber) + TChan.Ch1;
                Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
                double posH = Tools.YT2Zoom(channel.getBusPrimaryLevel());
                double posL = Tools.YT2Zoom(channel.getBusSecondaryLevel());
                boolean change = false;
                if (valueLine.getOtherY(ITriggerLine.VoltageLine_High) != WaveManage.get().getPositionY(ch) - posH) {
                    change = true;
                    putLevelCache(CacheUtil.VALUE_CHANNEL_H + ch, posH);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + ch, posH);
                    valueLine.setOtherY(ITriggerLine.VoltageLine_High, WaveManage.get().getPositionY(ch) - posH);
                }
                if (valueLine.getOtherY(ITriggerLine.VoltageLine_Low) != WaveManage.get().getPositionY(ch) - posL) {
                    change = true;
                    putLevelCache(CacheUtil.VALUE_CHANNEL + ch, posL);
                    putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + ch, posL);
                    valueLine.setOtherY(ITriggerLine.VoltageLine_Low, WaveManage.get().getPositionY(ch) - posL);
                }
                if (change) {
                    curCh = ch;
                    sendMsgTriggerLevel(false, true);
                }
            } else {
                TChan.foreachChan((i) -> {
                    Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(i));
                    if (channel == null) return;
                    double pos;
                    if (!isValueLevelHighAndLow(i)) {
                        pos = Tools.YT2Zoom(channel.getBusPrimaryLevel());
                    } else {
                        pos = Tools.YT2Zoom(channel.getBusSecondaryLevel());
                    }
                    if (valueLine.getOtherY(i) != WaveManage.get().getPositionY(i) - pos) {
                        putLevelCache(CacheUtil.VALUE_CHANNEL + i, pos);
                        putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + i, pos);
                        valueLine.setOtherY(i, WaveManage.get().getPositionY(i) - pos);
                    }
                });
                sendMsgTriggerLevel(false, true);
            }
        }

    }

    private double getCurCh() {
        return getChPos(curCh);
    }

    private double getChPos(int chIndex) {
        return WaveManage.get().getPositionYButWorkModeXY(chIndex);
    }

    private MTriggerLevel.OnMouseMoveListener onMouseMoveListener = new MTriggerLevel.OnMouseMoveListener() {
        @Override
        public void onMouseMove(View view, double deltaY, int ch, int openType, boolean isFromUser) {
            ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine(openType);

            if (isFromUser) {
                int serials=openType;
//                if (openType == MTriggerLevel.OPENTYPE_SERIALS1) {
//                    serials = CacheUtil.S1;
//                } else if (openType == MTriggerLevel.OPENTYPE_SERIALS2) {
//                    serials = CacheUtil.S2;
//                } else {
//                    serials = 0;
//                }

                if (serials != 0) {
                    int busType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serials);
                    if (CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serials + busType + ch)) {
                        CacheUtil.get().putMap(CacheUtil.VALUE_INIT + serials + busType + ch, String.valueOf(false));
                    }
                    CacheUtil.get().putMap(CacheUtil.LASTSET_SERIALS, String.valueOf(serials));
                    CacheUtil.get().setValueLevelSerials(serials);
                }
            }

//            int mainWaveY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
            int mainWaveY = GlobalVar.get().getMainWave().y;
            if (line.getCurrY() < 0 && deltaY < 0) {
                deltaY = line.getCurrY() + deltaY;
            } else if (line.getCurrY() > mainWaveY && deltaY > 0) {
                deltaY = line.getCurrY() - mainWaveY + deltaY;
            }
            line.setChannelId(ch);
            line.setOffsetY(deltaY);
            line.setVisibleLine(true);
            onMouseMoveForFunc(line);

            if (line.getShowMode() == ITriggerLine.ShowMode_Two) {
                //int[] currYAll = line.getCurrYAll();
                line.setText(Tools.getChannelLevel(ch,
                        line.getCurrYIndex() == ITriggerLine.VoltageLine_High ? Tools.LevelType_High : Tools.LevelMode_Normal,
                        openType == MTriggerLevel.OPENTYPE_TRIGGER ? Tools.LevelMode_Normal : Tools.LevelMode_Bus));
            } else {
                line.setText(Tools.getChannelLevel(ch, Tools.LevelMode_Normal,
                        openType == MTriggerLevel.OPENTYPE_TRIGGER ? Tools.LevelMode_Normal : Tools.LevelMode_Bus));
            }

            setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
            CacheUtil.get().setValueLevelSerials(0);
        }

        @Override
        public void onUpClick(View view, int Ch, int openType) {
            PlaySound.getInstance().playButton();
            VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
            ITriggerLine line = voltageLineManage.getVoltageLine(openType);
            line.setChannelId(Ch);
            line.setVisibleLine(false);
            switch (mTriggerLevel.getTriggerLevel_Mode_Work()) {
                case MTriggerLevel.TriggerLevel_Mode_Work_Normal:
                    break;
                case MTriggerLevel.TriggerLevel_Mode_Work_HighLow:
                    line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
                    break;
                case MTriggerLevel.TriggerLevel_Mode_Work_Logic:
                    line.setCurrYIndex(Ch);
                    break;
            }
            if (openType == MTriggerLevel.OPENTYPE_SERIALS1) {
                CacheUtil.get().setValueLevelSerials(CacheUtil.S1);
            } else if (openType == MTriggerLevel.OPENTYPE_SERIALS2) {
                CacheUtil.get().setValueLevelSerials(CacheUtil.S2);
            }else if (openType == MTriggerLevel.OPENTYPE_SERIALS3){
                CacheUtil.get().setValueLevelSerials(CacheUtil.S3);
            }else if (openType == MTriggerLevel.OPENTYPE_SERIALS4){
                CacheUtil.get().setValueLevelSerials(CacheUtil.S4);
            }
            changeCh(line, Ch);
            setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
            CacheUtil.get().setValueLevelSerials(0);

        }

        @Override
        public void onDownClick(View view, int Ch, int openType) {
            PlaySound.getInstance().playButton();
            VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
            ITriggerLine line = voltageLineManage.getVoltageLine(openType);
            line.setChannelId(Ch);
            line.setVisibleLine(false);
            switch (mTriggerLevel.getTriggerLevel_Mode_Work()) {
                case MTriggerLevel.TriggerLevel_Mode_Work_Normal:
                    break;
                case MTriggerLevel.TriggerLevel_Mode_Work_HighLow:
                    line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
                    break;
                case MTriggerLevel.TriggerLevel_Mode_Work_Logic:
                    line.setCurrYIndex(Ch);
                    break;
            }
            if (openType == MTriggerLevel.OPENTYPE_SERIALS1) {
                CacheUtil.get().setValueLevelSerials(CacheUtil.S1);
            } else if (openType == MTriggerLevel.OPENTYPE_SERIALS2) {
                CacheUtil.get().setValueLevelSerials(CacheUtil.S2);
            }else if (openType == MTriggerLevel.OPENTYPE_SERIALS3){
                CacheUtil.get().setValueLevelSerials(CacheUtil.S3);
            }else if (openType == MTriggerLevel.OPENTYPE_SERIALS4){
                CacheUtil.get().setValueLevelSerials(CacheUtil.S4);
            }
            changeCh(line, Ch);
            setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
            CacheUtil.get().setValueLevelSerials(0);

        }

        @Override
        public void onMouseMoveComplete(View view, int openType) {
            ITriggerLine line = VoltageLineManage.getInstance().getVoltageLine(openType);
            if (line != null) {
                line.setVisibleLine(false);
            }
//            setWaveZoneSlideDirectionAndLastObjToTriggerLevel();
        }
    };

    /**
     * 当滑动triggerLevel数据改变时，�?要Cache保存、Command发�?��?�msg发�?�等做的操作
     */
    private void onMouseMoveForFunc(ITriggerLine line) {
        if (line instanceof TriggerVoltageLine) {
            if (LEVEL_TRIGGER_SLOPE.equals(curLevel)) {
                double[] currYs = line.getCurrYAll();
                double leveH = getCurCh() - currYs[ITriggerLine.VoltageLine_High];
                double leveL = getCurCh() - currYs[ITriggerLine.VoltageLine_Low];
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh, leveH);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, leveL);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + curCh,leveH);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, leveL);
                //修改底层触发电平值
                Trigger trigger = TriggerFactory.getInstance().getTrigger();
                int src = trigger.getTriggerSource();
                TriggerLevel triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, src);
                if (leveH != triggerLevel.getPosUI()) {
                    triggerLevel.setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh), true);
                }
                triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src);
                if (leveL != triggerLevel.getPosUI()) {
                    triggerLevel.setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh), true);
                }
                Command.get().getTrigger_slope().HLevel(getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Normal), false);
                Command.get().getTrigger_slope().LLevel(getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal), false);
            } else if (LEVEL_TRIGGER_RUNT.equals(curLevel)) {
                double[] currYs = line.getCurrYAll();
                double leveH = getCurCh() - currYs[ITriggerLine.VoltageLine_High];
                double leveL = getCurCh() - currYs[ITriggerLine.VoltageLine_Low];
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh, leveH);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, leveL);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP_H + curCh, leveH);
                putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, leveL);
                //修改底层触发电平值
                Trigger trigger = TriggerFactory.getInstance().getTrigger();
                int src = trigger.getTriggerSource();
                TriggerLevel triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, src);
                if (leveH != triggerLevel.getPosUI())
                    triggerLevel.setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh), true);
                triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src);
                if (leveL != triggerLevel.getPosUI())
                    triggerLevel.setPos(getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh), true);
                Command.get().getTrigger_dwart().HLevel(getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Normal), false);
                Command.get().getTrigger_dwart().LLevel(getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Normal), false);
            } else {
                if (TChan.isChan(curCh)) {
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, ScopeBase.changeAccuracy(getCurCh() - line.getCurrY()));
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, ScopeBase.changeAccuracy(getCurCh() - line.getCurrY()));
                    //修改底层触发电平值
                    TriggerLevel triggerLevel = TriggerFactory.getInstance().getTrigger().getTriggerLevel(TChan.toFpgaChNo(curCh));
                    //long vol = WaveManage.get().getPositionY(line.getChannelId()) - line.getCurrY();
                    double vol = getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh);
                    if (triggerLevel != null && vol != triggerLevel.getPosUI())
                        triggerLevel.setPos(vol, true);
                    switch (curLevel) {
                        case LEVEL_TRIGGER_LOGIC:
                            Command.get().getTrigger_logic().Level(TChan.toFpgaChNo(curCh), getDoubleV(curCh), false);
                            break;
                        case LEVEL_TRIGGER_EDGE:
                            Command.get().getTrigger_edge().Level(getDoubleV(curCh), false);
                            break;
                        case LEVEL_TRIGGER_NEDGE:
                            Command.get().getTrigger_nedge().Level(getDoubleV(curCh), false);
                            break;
                        case LEVEL_TRIGGER_PULSEWIDTH:
                            Command.get().getTrigger_pulse().Level(getDoubleV(curCh), false);
                            break;
                        case LEVEL_TRIGGER_TIMEOUT:
                            Command.get().getTrigger_timeout().Level(getDoubleV(curCh), false);
                            break;
                    }
                } else {
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, line.getCurrY());
                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, line.getCurrY());
//                    putLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh, Tools.transFormTriggerInput(line.getCurrY()));
//                    putLevelCache(CacheUtil.TRIGGER_CHANNEL_TEMP + curCh, Tools.transFormTriggerInput(line.getCurrY()));
                    if (curCh == TChan.Ch8 + 1) {
                        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
                        triggerCommon.setExtTriggerLevle(Tools.getExtTriggerValCache(CacheUtil.TRIGGER_CHANNEL + curCh));
                    }
                }
            }
        } else if (line instanceof DiscreetVoltageLine) {
            double vol1, vol2 = 0;
            if (LEVEL_VALUE_M429.equals(curLevel)) {
                double[] currYs = line.getCurrYAll();
                vol1 = getCurCh() - currYs[ITriggerLine.VoltageLine_High];
                vol2 = getCurCh() - currYs[ITriggerLine.VoltageLine_Low];
                double valueH = CacheUtil.get().getDouble(CacheUtil.VALUE_CHANNEL_H + curCh);
                double valueL = CacheUtil.get().getDouble(CacheUtil.VALUE_CHANNEL + curCh);
                putLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh, vol1);
                putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, vol2);
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP_H + curCh, vol1);
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh, vol2);
                Command.get().getTrigger_m429().setLevelHigh(serialsNumber - 1, curCh, getDoubleV(curCh, Tools.LevelType_High, Tools.LevelMode_Bus), false);
                Command.get().getTrigger_m429().setLevelLow(serialsNumber - 1, curCh, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
            } else if (LEVEL_VALUE_SPI.equals(curLevel)) {
                vol1 = getCurCh() - line.getCurrY();
                putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, vol1);
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh, vol1);

                double[] currYs = line.getCurrYAll();
                Command.get().getBus_spi().setLevelClock(serialsNumber - 1, getDoubleV(spiClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                Command.get().getBus_spi().setLevelData(serialsNumber - 1, getDoubleV(spiDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                Command.get().getTrigger_spi().LevelCLK(serialsNumber - 1, getDoubleV(spiClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                Command.get().getTrigger_spi().LevelData(serialsNumber - 1, getDoubleV(spiDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                if (spiCsEnable) {
                    Command.get().getBus_spi().setLevelCs(serialsNumber - 1, getDoubleV(spiCsIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                    Command.get().getTrigger_spi().LevelCS(serialsNumber - 1, getDoubleV(spiCsIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                }
            } else if (LEVEL_VALUE_I2C.equals(curLevel)) {
                vol1 = getCurCh() - line.getCurrY();
                putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, vol1);
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh, vol1);

                double[] currYs = line.getCurrYAll();
                Command.get().getTrigger_iic().setLevelData(serialsNumber - 1, getDoubleV(i2cDataIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                Command.get().getTrigger_iic().setLevelClock(serialsNumber - 1, getDoubleV(i2cClockIndex, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
            } else {//单阈值电平的总线
                vol1 = getCurCh() - line.getCurrY();
                putLevelCache(CacheUtil.VALUE_CHANNEL + curCh, vol1);
                putLevelCache(CacheUtil.VALUE_CHANNEL_TEMP + curCh, vol1);

                switch (curLevel) {
                    case LEVEL_VALUE_UART:
                        Command.get().getTrigger_uart().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                        break;
                    case LEVEL_VALUE_LIN:
                        Command.get().getTrigger_lin().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                        break;
                    case LEVEL_VALUE_CAN:
                        Command.get().getTrigger_can().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                        break;
                    case LEVEL_VALUE_M1553B:
                        Command.get().getTrigger_m1553B().setLevel(serialsNumber - 1, getDoubleV(curCh, Tools.LevelType_Normal, Tools.LevelMode_Bus), false);
                        break;
                }
            }
            double valueH = CacheUtil.get().getDouble(CacheUtil.VALUE_CHANNEL_H + curCh);
            double valueL = CacheUtil.get().getDouble(CacheUtil.VALUE_CHANNEL + curCh);
            Logger.i("ValueCache12:" + valueH + "\t" + valueL);
            Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.onMouseMoveForFunc curCh: %d,valueH:%s,valueL:%s",curCh,valueH,valueL ));
            //修改底层触发电平值
            changeSerialTrigVol_channel(curCh);
        }
        sendMsgTriggerLevel(false, false);
    }

    /**
     * @param chIndex 1 - 4
     */
    private double getDoubleV(int chIndex) {
        return getDoubleV(chIndex, Tools.LevelType_Normal, Tools.LevelMode_Normal);
    }

    /**
     * @param chIndex 1 - 4
     */
    private double getDoubleV(int chIndex, int levelType, int levelMode) {
        String v = Tools.getChannelLevel(chIndex, levelType, levelMode);
        return TBookUtil.getDoubleFromM(v.replace("V", "").replace("A", ""));
    }

    /**
     * @param ch 改变之后的channel
     */
    private void changeCh(ITriggerLine line, int ch) {
        curCh = ch;
        int leveType = Tools.LevelType_Normal;
        int leveMode = Tools.LevelMode_Normal;
        if (line instanceof TriggerVoltageLine) {
            if (LEVEL_TRIGGER_LOGIC.equals(curLevel)) {
//                line.setCurrYIndex(TChan.Ch1);
//                line.setCurrY(WaveManage.get().getPositionY(TChan.Ch1) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH1));
//                line.setCurrYIndex(TChan.Ch2);
//                line.setCurrY(WaveManage.get().getPositionY(TChan.Ch2) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH2));
//                line.setCurrYIndex(TChan.Ch3);
//                line.setCurrY(WaveManage.get().getPositionY(TChan.Ch3) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH3));
//                line.setCurrYIndex(TChan.Ch4);
//                line.setCurrY(WaveManage.get().getPositionY(TChan.Ch4) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + CacheUtil.CH4));
                TChan.foreachChan((i) -> {
                    line.setCurrYIndex(i);
                    line.setCurrY(WaveManage.get().getPositionY(i) - getLevelCache(CacheUtil.TRIGGER_CHANNEL + i));
                });
                line.setCurrYIndex(curCh);
            } else if (LEVEL_TRIGGER_SLOPE.equals(curLevel)
                    || LEVEL_TRIGGER_RUNT.equals(curLevel)) {
                line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL_H + curCh));
                line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh));
                line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());

            } else {
                line.setCurrYIndex(curCh);
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.TRIGGER_CHANNEL + curCh));
            }
        } else if (line instanceof DiscreetVoltageLine) {
            leveMode = Tools.LevelMode_Bus;
            if (LEVEL_VALUE_M429.equals(curLevel)) {
                line.setCurrYIndex(ITriggerLine.VoltageLine_High);
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL_H + curCh));
                line.setCurrYIndex(ITriggerLine.VoltageLine_Low);
                line.setCurrY(getCurCh() - getLevelCache(CacheUtil.VALUE_CHANNEL + curCh));
                line.setCurrYIndex(mTriggerLevel.isTriggerLevel_Mode_Work_HighLow_Index());
                if (line.getCurrYIndex() == ITriggerLine.VoltageLine_High) {
                    leveType = Tools.LevelType_High;
                }
            } else {
                line.setCurrYIndex(curCh);
//                if (LEVEL_VALUE_SPI.equals(curLevel)) {
//                    spiCurCh[line.getNameId() - IWave.S1] = curCh;
//                } else if (LEVEL_VALUE_I2C.equals(curLevel)) {
//                    i2cCurCh[line.getNameId() - IWave.S1] = curCh;
//                }
            }
        } else {
            Logger.e(TAG, "changeCh");
        }
        line.setText(Tools.getChannelLevel(ch, leveType, leveMode));
        sendMsgTriggerLevel(false, false);
    }

    private MTriggerLevel.OnOpenCloseListener onOpenCloseListener = new MTriggerLevel.OnOpenCloseListener() {
        @Override
        public void onOpen(int type) {
            PlaySound.getInstance().playSlide();
            VoltageLineManage voltageLineManage = VoltageLineManage.getInstance();
            String str = voltageLineManage.getDiscreetVoltageLineCurKey();

            Logger.d(TAG, "str:" + str + ",type:" + type);
//            if (type == MTriggerLevel.OPENTYPE_SERIALS1) {
//                voltageLineManage.setDiscreetVoltageShowState(VoltageLineManage.VoltageLineType_Value1, 1);
//                DiscreetVoltageLineInfoBean bean = voltageLineManage.getCurrDisCreetVoltageLineInfo();
//                Log.d(Tag.Debug, String.format("MainHolderTriggerLevel.onOpen: %s", bean));
//                while (!VoltageLineManage.VoltageLineType_Value1.equals(bean.VoltageLineName)) {
//                    bean = voltageLineManage.setNextDiscreetVoltageLineInS1S2();
//                }
//                curCh = bean.ChannelId;
//                setValueLevel();
//                setTriggerLevelActive(false);
//            } else if (type == MTriggerLevel.OPENTYPE_SERIALS2) {
//                voltageLineManage.setDiscreetVoltageShowState(VoltageLineManage.VoltageLineType_Value2, 2);
//
//                DiscreetVoltageLineInfoBean bean = voltageLineManage.getCurrDisCreetVoltageLineInfo();
//                while (!VoltageLineManage.VoltageLineType_Value2.equals(bean.VoltageLineName)) {
//                    bean = voltageLineManage.setNextDiscreetVoltageLineInS1S2();
//                    Logger.d(TAG, bean.VoltageLineName);
//                }
//                curCh = bean.ChannelId;
//                setValueLevel();
//                setTriggerLevelActive(false);
//            }

            if (MTriggerLevel.isOpenSerial(type)) {
                String SerialKey = voltageLineManage.getSerialKey(type);
                voltageLineManage.setDiscreetVoltageShowState(SerialKey, type);
                DiscreetVoltageLineInfoBean bean = voltageLineManage.getCurrDisCreetVoltageLineInfo();
                while (!SerialKey.equals(bean.VoltageLineName)) {
                    bean = voltageLineManage.setNextDiscreetVoltageLineInS1S2();
                }
                curCh = bean.ChannelId;
                setValueLevel();
                setTriggerLevelActive(false);
            }

        }

        @Override
        public void onClose(int type) {
            PlaySound.getInstance().playSlide();
            Log.d(TAG, "onClose() called with: type = [" + type + "]");
            boolean s1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean s2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean s3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean s4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);

//            VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1).setShowState(s1);
//            VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2).setShowState(s2);
            if (!VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger).getShowState()) {
                setTriggerLevelVisible(View.GONE);
            } else {
                setTriggerLevelVisible(View.VISIBLE);
            }
            int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
            visibleTriggerVideo = (triggerIndex != TopLayoutTrigger.DETAIL_S1
                    && triggerIndex != TopLayoutTrigger.DETAIL_S2
                    && triggerIndex != TopLayoutTrigger.DETAIL_S3
                    && triggerIndex != TopLayoutTrigger.DETAIL_S4
                    && triggerIndex != TopLayoutTrigger.DETAIL_VIDEO);

            if (MTriggerLevel.isSerialType(type)) {
                int sNo = type;
                //显示S1S2的图标
                String showState;
                showState = s1 ? VoltageLineManage.VoltageLineType_Value1 : "";
                showState += s2 ? VoltageLineManage.VoltageLineType_Value2 : "";
                showState += s3 ? VoltageLineManage.VoltageLineType_Value3 : "";
                showState += s4 ? VoltageLineManage.VoltageLineType_Value4 : "";
                VoltageLineManage.getInstance().setDiscreetVoltageShowState(showState, sNo);
            }

            //region S1S2 的通道图标位置同步
//            if (type == MTriggerLevel.OPENTYPE_SERIALS1 || type == MTriggerLevel.OPENTYPE_SERIALS2) {
//                setS1S2IconSync(type);
//
//            }
            VoltageLineManage.getInstance().setCurrDiscreetVoltageLineInS1S2();
            //endregion

        }
    };

    /**
     * S1S2S3S4 的通道图标位置同步
     */
    private void setS1S2S3S4IconSync(int openType) {
        DiscreetVoltageLine curLine = (DiscreetVoltageLine) VoltageLineManage.getInstance().getVoltageLine(openType);
        VoltageLineManage.getInstance().foreachDiscreetLine((otherLine) -> {
            if (LEVEL_VALUE_M429.equals(curLevel)) {
                double[] currYs = curLine.getCurrYAll();
                if (otherLine.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if (otherLine.getChannelId() == curLine.getChannelId()) {
                        otherLine.setOtherY(ITriggerLine.VoltageLine_High, currYs[ITriggerLine.VoltageLine_High]);
                        otherLine.setOtherY(ITriggerLine.VoltageLine_Low, currYs[ITriggerLine.VoltageLine_Low]);
                    }
                } else {
                    otherLine.setOtherY(curLine.getChannelId(), currYs[ITriggerLine.VoltageLine_Low]);
                }
            } else {
                if (otherLine.getShowMode() == ITriggerLine.ShowMode_Two) {
                    if (otherLine.getChannelId() == curCh
                            && mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[otherLine.getChannelId()] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                        otherLine.setOtherY(ITriggerLine.VoltageLine_Low, curLine.getCurrYAll()[curCh]);
                    }
                } else {
                    for (int i = 1; i < TChan.MaxLogicChan+1; i++) {
                        if (mTriggerLevel.getTriggerLevel_Mode_Work_Logic_state()[i] != MTriggerLevel.TriggerLevel_Mode_work_Logic_None) {
                            otherLine.setOtherY(i, curLine.getCurrYAll()[i]);
                        }
                    }
                }
            }
        }, (ln) -> ln != curLine);
    }

    private static final int MSG_TRIGGERLEVEL_CLOSE = 57;
    private static final int MSG_VALUE1LEVEL_CLOSE = 58;
    private static final int MSG_VALUE2LEVEL_CLOSE = 59;
    private static final int MSG_VALUE3LEVEL_CLOSE = 60;
    private static final int MSG_VALUE4LEVEL_CLOSE = 61;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ITriggerLine line = null;
            switch (msg.what) {
                case MSG_TRIGGERLEVEL_CLOSE:
                    line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Trigger);
                    break;
                case MSG_VALUE1LEVEL_CLOSE:
                    line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value1);
                    break;
                case MSG_VALUE2LEVEL_CLOSE:
                    line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value2);
                    break;
                case MSG_VALUE3LEVEL_CLOSE:
                    line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value3);
                    break;
                case MSG_VALUE4LEVEL_CLOSE:
                    line = VoltageLineManage.getInstance().getVoltageLine(VoltageLineManage.VoltageLineType_Value4);
                    break;
            }
            if (line != null) {
                line.setVisibleLine(false);
            }
        }
    };
}