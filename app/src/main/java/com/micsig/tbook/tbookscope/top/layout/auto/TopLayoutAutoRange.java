package com.micsig.tbook.tbookscope.top.layout.auto;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Auto.Auto;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.MSwitchBox;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAutoRange extends Fragment {
    private Context context;
    private MSwitchBox rgRange, rgVertical, rgHorizontal, rgLevel;

    private TopMsgAutoRange autoDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_autorange, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        autoDetail = new TopMsgAutoRange();
        autoDetail.setRange(rgRange.isState());
        autoDetail.setVertical(rgVertical.isState());
        autoDetail.setHorizontal(rgHorizontal.isState());
        autoDetail.setLevel(rgLevel.isState());
    }

    private void initView(View view) {
        rgRange = (MSwitchBox) view.findViewById(R.id.rangeDetail);
        rgRange.setOnToggleStateChangedListener(onToggleStateChangedListener);
        rgVertical = (MSwitchBox) view.findViewById(R.id.verticalDetail);
        rgVertical.setOnToggleStateChangedListener(onToggleStateChangedListener);
        rgHorizontal = (MSwitchBox) view.findViewById(R.id.horizontalDetail);
        rgHorizontal.setOnToggleStateChangedListener(onToggleStateChangedListener);
        rgLevel = (MSwitchBox) view.findViewById(R.id.levelDetail);
        rgLevel.setOnToggleStateChangedListener(onToggleStateChangedListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void setCache() {
        if (Tools.isEnableAutoRange()) {
            int range = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE);
            int vertical = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL);
            int horizontal = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL);
            int level = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL);

            this.rgRange.setState(range == 0);
            this.rgVertical.setState(vertical == 0);
            this.rgHorizontal.setState(horizontal == 0);
            this.rgLevel.setState(level == 0);

            rgVertical.setEnabled(range == 0);
            rgHorizontal.setEnabled(range == 0);
            rgLevel.setEnabled(range == 0);

            Command.get().getAuto().range(range == 0, false);
            Command.get().getAuto().rangeVertical(vertical == 0, false);
            Command.get().getAuto().rangeHorizoncal(horizontal == 0, false);
            Command.get().getAuto().rangeLevel(level == 0, false);

            Auto.getInstance().setAutoRangeEnable(range == 0);
            Auto.getInstance().setAutoVerticalEnable(vertical == 0);
            Auto.getInstance().setAutoHorizontalEnable(horizontal == 0);
            Auto.getInstance().setAutoLevleEnable(level == 0);

            autoDetail.setRange(this.rgRange.isState());
            autoDetail.setVertical(this.rgVertical.isState());
            autoDetail.setHorizontal(this.rgHorizontal.isState());
            autoDetail.setLevel(this.rgLevel.isState());
            sendMsg(false);
        } else {
            rgRange.setState(false);
            rgRange.setEnabled(false);
            rgVertical.setEnabled(false);
            rgHorizontal.setEnabled(false);
            rgLevel.setEnabled(false);
        }
    }

    public TopMsgAutoRange getAutoDetail() {
        return autoDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAutoRange, true);
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            boolean isSerials = ChannelFactory.isChOpen(ChannelFactory.S1)
                    || ChannelFactory.isChOpen(ChannelFactory.S2)
                    || ChannelFactory.isChOpen(ChannelFactory.S3)
                    || ChannelFactory.isChOpen(ChannelFactory.S4);
            if (!isSerials) {
                rgRange.setEnabled(true);
                rgVertical.setEnabled(true);
                rgHorizontal.setEnabled(true);
                rgLevel.setEnabled(true);
                if (Tools.isEnableAutoRange()) {
                    int range = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE);
                    int vertical = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL);
                    int horizontal = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL);
                    int level = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL);

                    rgRange.setState(range == 0);
                    rgVertical.setState(vertical == 0);
                    rgHorizontal.setState(horizontal == 0);
                    rgLevel.setState(level == 0);

                    rgVertical.setEnabled(range == 0);
                    rgHorizontal.setEnabled(range == 0);
                    rgLevel.setEnabled(range == 0);

                    Command.get().getAuto().range(range == 0, false);
                    Command.get().getAuto().rangeVertical(vertical == 0, false);
                    Command.get().getAuto().rangeHorizoncal(horizontal == 0, false);
                    Command.get().getAuto().rangeLevel(level == 0, false);

                    Auto.getInstance().setAutoRangeEnable(range == 0);
                    Auto.getInstance().setAutoVerticalEnable(vertical == 0);
                    Auto.getInstance().setAutoHorizontalEnable(horizontal == 0);
                    Auto.getInstance().setAutoLevleEnable(level == 0);

                    autoDetail.setRange(rgRange.isState());
                    autoDetail.setVertical(rgVertical.isState());
                    autoDetail.setHorizontal(rgHorizontal.isState());
                    autoDetail.setLevel(rgLevel.isState());
                    sendMsg(false);
                }
            } else {
                rgRange.setState(false);
                rgRange.setEnabled(false);
                rgVertical.setEnabled(false);
                rgHorizontal.setEnabled(false);
                rgLevel.setEnabled(false);
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            if (Tools.isEnableAutoRange()) {
                switch (commandMsgToUI.getFlag()) {
                    case CommandMsgToUI.FLAG_AUTO_RANGE:
                        int range = Integer.parseInt(commandMsgToUI.getParam());
                        if (rgRange.isState() != (range == 0)) {
                            rgRange.setState(range == 0);
                            onCheckChanged(rgRange, rgRange.isState(), false);
                        }
                        break;
                    case CommandMsgToUI.FLAG_AUTO_RANGEVERTICAL:
                        int rangeVer = Integer.parseInt(commandMsgToUI.getParam());
                        if (rgVertical.isState() != (rangeVer == 0)) {
                            rgVertical.setState(rangeVer == 0);
                            onCheckChanged(rgVertical, rgVertical.isState(), false);
                        }
                        break;
                    case CommandMsgToUI.FLAG_AUTO_RANGEHORIZONTAL:
                        int rangeHor = Integer.parseInt(commandMsgToUI.getParam());
                        if (rgHorizontal.isState() != (rangeHor == 0)) {
                            rgHorizontal.setState(rangeHor == 0);
                            onCheckChanged(rgHorizontal, rgHorizontal.isState(), false);
                        }
                        break;
                    case CommandMsgToUI.FLAG_AUTO_RANGELEVEL:
                        int rangeLevel = Integer.parseInt(commandMsgToUI.getParam());
                        if (rgLevel.isState() != (rangeLevel == 0)) {
                            rgLevel.setState(rangeLevel == 0);
                            onCheckChanged(rgLevel, rgLevel.isState(), false);
                        }
                        break;
                }
            }
        }
    };

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            onCheckChanged(view, state, false);
        }
    };

    private void onCheckChanged(MSwitchBox view, boolean state, boolean isFromEventBus) {
        if (Tools.isEnableAutoRange()) {
            if (view.getId() == R.id.rangeDetail) {
                Command.get().getAuto().range(state, false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE, String.valueOf(state ? 0 : 1));
                if (!isFromEventBus) {
                    Auto.getInstance().setAutoRangeEnable(state);
                }
                rgVertical.setEnabled(state);
                rgHorizontal.setEnabled(state);
                rgLevel.setEnabled(state);
                autoDetail.setRange(state);
                sendMsg(isFromEventBus);
                RxBus.getInstance().post(RxEnum.TOPLAYOUT_AUTO_CHANGED,state);
            } else if (view.getId() == R.id.verticalDetail) {
                Command.get().getAuto().rangeVertical(state, false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL, String.valueOf(state ? 0 : 1));
                if (!isFromEventBus) {
                    Auto.getInstance().setAutoVerticalEnable(state);
                }
                autoDetail.setVertical(state);
                sendMsg(isFromEventBus);
            } else if (view.getId() == R.id.horizontalDetail) {
                Command.get().getAuto().rangeHorizoncal(state, false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL, String.valueOf(state ? 0 : 1));
                if (!isFromEventBus) {
                    Auto.getInstance().setAutoHorizontalEnable(state);
                }
                autoDetail.setHorizontal(state);
                sendMsg(isFromEventBus);
            } else if (view.getId() == R.id.levelDetail) {
                Command.get().getAuto().rangeLevel(state, false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL, String.valueOf(state ? 0 : 1));
                if (!isFromEventBus) {
                    Auto.getInstance().setAutoLevleEnable(state);
                }
                autoDetail.setLevel(state);
                sendMsg(isFromEventBus);
            }
        }
    }
}
