package com.micsig.tbook.tbookscope.top.layout.sample;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.IMemDepth;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutSampleDepth extends Fragment {
    private Context context;
    private TopViewRadioGroup viewDepth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_sample_depth, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        viewDepth = (TopViewRadioGroup) view.findViewById(R.id.depth);
        viewDepth.setData(R.string.sampleDepth, R.array.sampleDepth, onCheckChangedListener);
    }

    private void initData() {
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_MEM_DEPTH, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_FORCE_MEM_DEPTH, eventUIObserver);
    }

    private void setCache() {
        refreshMemDepth(false);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleDepth, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_USERSET_LENGTH:
                    if (viewDepth.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        viewDepth.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam()));
                        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(viewDepth.getSelected().getIndex()));
                        MemDepthFactory.getMemDepth().setMemDepthItem(viewDepth.getSelected().getIndex());
                    }
                    break;
            }
        }
    };

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            Command.get().getUserset().setLength(item.getIndex(), false);
            Command.get().getSample().Mdepth(item.getIndex(),false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(item.getIndex()));
            MemDepthFactory.getMemDepth().setMemDepthItem(item.getIndex());
            refreshMemDepth(true);

        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_MEM_DEPTH
                    || ((EventBase) data).getId() == EventFactory.EVENT_FORCE_MEM_DEPTH) {
                refreshMemDepth(true);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(MemDepthFactory.getMemDepth().getMemDepthItem()));
            }
        }
    };

    private void refreshMemDepth(boolean isFromEventBus) {
        IMemDepth memDepth = MemDepthFactory.getMemDepth();
        List<String> memDepthList = memDepth.getMemDepthItemName();
        String[] memDepths = new String[memDepthList.size()];
        memDepthList.toArray(memDepths);
        if (!Arrays.equals(memDepths, viewDepth.getArray())) {
            if (memDepths[0].equalsIgnoreCase("Auto")) {
                String[] strings = context.getResources().getStringArray(R.array.sampleDepth);
                memDepths[0] = strings[0];
            }
            viewDepth.setData(App.get().getString(R.string.sampleDepth), memDepths, onCheckChangedListener);
        }
        int depth;
        if (!isFromEventBus) {
            depth = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH);
        } else {
            depth = memDepth.getMemDepthItem();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth));
        }
        if (depth >= memDepthList.size()) {
            depth = 0;
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth));
        }
        viewDepth.setSelectedIndex(depth);
        if (!isFromEventBus) {
            if (memDepth.getMemDepthItem() != viewDepth.getSelected().getIndex()) {
                memDepth.setMemDepthItem(viewDepth.getSelected().getIndex());
            }
        }
        Command.get().getUserset().setLength(viewDepth.getSelected().getIndex(), false);
        Command.get().getSample().Mdepth(viewDepth.getSelected().getIndex(),false);

        if (CacheUtil.get().isLoadParamComplete()) {
            TopMsgSampleDepth msgSampleDepth = new TopMsgSampleDepth(isFromEventBus);
            msgSampleDepth.setDepth(viewDepth.getSelected().getIndex());
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLEDEPTH, msgSampleDepth);
        }
    }
}
