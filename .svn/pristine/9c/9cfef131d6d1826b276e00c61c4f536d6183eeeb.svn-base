package com.micsig.tbook.tbookscope.top.layout.userset;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * @auother limh
 * @description: 参考波形时基调节
 * @data:2025-10-20 14:40
 */
public class TopLayoutUserSetRefTimeBase extends Fragment {

    private static final String TAG = "TopLayoutUserSetRefTimeBase";
    private Context context;
    private ViewGroup rootViewGroup;
    private TopViewRadioGroup groupTimeBase;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetreftimebase, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        rootViewGroup = (ViewGroup) view;
        groupTimeBase = view.findViewById(R.id.ref_timebase);
        groupTimeBase.setOnListener(onCheckChangedListener);
    }

    private void initControl() {
//        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onSelectChanged(view, item, false);
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {
        }
    };


    private void setCache() {
        groupTimeBase.postDelayed(()->{
            int timeBaseIndex = CacheUtil.get().isRefTimebase() ? 0 : 1;//CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
            groupTimeBase.setSelectedIndex(timeBaseIndex);
            onCheckChangedListener.onClick(groupTimeBase, groupTimeBase.getSelected());
        },800);
    }

    @SuppressLint("NonConstantResourceId")
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutUserSetRefTimeBase", rootViewGroup);
        switch (view.getId()) {
            case R.id.ref_timebase:
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE) == item.getIndex()) return;
                HorizontalAxis.getInstance().setScaleFollowingCh(item.getIndex() == 0);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE, String.valueOf(item.getIndex()));
                RxBus.getInstance().post(RxEnum.MQ_MSG_USER_SET_TIMEBASE, item.getIndex());
                break;
            default:
                break;
        }
    }

    private final Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

/*    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {//TODO 待SCPI指令接入
                default:
                    break;
            }
        }
    };*/

    void ms_sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
