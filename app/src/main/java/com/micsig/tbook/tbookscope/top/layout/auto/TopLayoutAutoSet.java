package com.micsig.tbook.tbookscope.top.layout.auto;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Auto.Auto;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;
import com.micsig.tbook.ui.util.StrUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAutoSet extends Fragment {
    private static final String TAG = "TopLayoutAutoSet";

    private Context context;
    private MSwitchBox rgOpenChannel;
    private TopViewRadioGroup rgTriggerSource;
    private TopViewRadioGroup rgLevelSelect;
    private TextView tvLevelDetail;
    private TopDialogTextKeyBoard dialogTextKeyBoard;

    private TopMsgAutoSet msgAutoDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_autosetting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initData() {
        msgAutoDetail = new TopMsgAutoSet();
        msgAutoDetail.setOpenChannel(rgOpenChannel.isState());
        msgAutoDetail.setTriggerSource(rgTriggerSource.getSelected());
        msgAutoDetail.setLevelSelect(rgLevelSelect.getSelected());
        msgAutoDetail.setLevelDetail(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL));
    }

    private void initView(View view) {
        rgOpenChannel = (MSwitchBox) view.findViewById(R.id.openChannelDetail);
        rgOpenChannel.setOnToggleStateChangedListener(onToggleStateChangedListener);
        rgTriggerSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource);
        rgTriggerSource.setData(R.string.autoSettingTriggerSource, R.array.autoSettingTriggerSource, onCheckChangeListener);
        rgLevelSelect = (TopViewRadioGroup) view.findViewById(R.id.levelSelect);
        rgLevelSelect.setData(null, getResources().getStringArray(R.array.autoSettingLevelSelect), onCheckChangeListener);
        tvLevelDetail = (TextView) view.findViewById(R.id.levelDetail);
        tvLevelDetail.setOnClickListener(onClickListener);

        dialogTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void setCache() {
        int channel = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_CHANNEL);
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_SOURCE);
        int levelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELSELECT);
        int levelmV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL);

        rgOpenChannel.setState(channel == 0);
        rgTriggerSource.setSelectedIndex(source);
        rgLevelSelect.setSelectedIndex(levelSelect);
        if (levelSelect == 0) {
            tvLevelDetail.setText(getVFromMV());
        } else {
            tvLevelDetail.setText(String.valueOf(levelmV));
        }
        if (channel == 1) {
            rgLevelSelect.setEnabled(false);
            tvLevelDetail.setEnabled(false);
            tvLevelDetail.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable));
//            tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext_unclickable);
        } else {
            rgLevelSelect.setEnabled(true);
            tvLevelDetail.setEnabled(true);
//            tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext);
            tvLevelDetail.setTextColor(getResources().getColor(R.color.textColor));
        }

        Command.get().getAuto().setSource(source, false);
        Command.get().getAuto().setLevel(Double.parseDouble(getVFromMV()), false);
        Command.get().getAuto().setChannel(channel == 0, false);

        Auto.getInstance().setAutoChannelEnable(channel == 0);
        Auto.getInstance().setAutoTriggerSource(source);
        Auto.getInstance().setAutoThresholdLevel(Float.parseFloat(getVFromMV()));

        msgAutoDetail.setOpenChannel(rgOpenChannel.isState());
        msgAutoDetail.setTriggerSource(rgTriggerSource.getSelected());
        msgAutoDetail.setLevelSelect(rgLevelSelect.getSelected());
        msgAutoDetail.setLevelDetail(levelmV);
        sendMsg(false);
    }

    private String getVFromMV() {
        int levelmV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL);

        String levelV = String.valueOf(levelmV / 1000);
        if (levelmV % 1000 != 0) {
            String number = String.valueOf(levelmV % 1000);
            while (number.length() < 3) {
                number = "0" + number;
            }
            levelV = levelV + "." + number;
        }
        return levelV;
    }

    public TopMsgAutoSet getMsgAutoDetail() {
        return msgAutoDetail;
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAutoSet, true);
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_AUTO_CHANNEL:
                    int channel = Integer.parseInt(commandMsgToUI.getParam());
                    if (rgOpenChannel.isState() != (channel == 0)) {
                        rgOpenChannel.setState(channel == 0);
                        onToggleStateChangedListener.onToggleStateChanged(rgOpenChannel, rgOpenChannel.isState());
                    }
                    break;
                case CommandMsgToUI.FLAG_AUTO_LEVEL:
                    int param = (int) (Double.parseDouble(commandMsgToUI.getParam()) * 1000);
                    if (param<1) param=1;
                    if (param>99999) param=99999;
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL,String.valueOf(param));
                    if (rgLevelSelect.getSelected().getIndex() == 0) {
                        tvLevelDetail.setText(getVFromMV());
                    } else {
                        tvLevelDetail.setText(String.valueOf(param));
                    }
                    onTextChanged(tvLevelDetail, tvLevelDetail.getText().toString(), false);
                    break;
                case CommandMsgToUI.FLAG_AUTO_SOURCE:
                    Integer sourceIndex = Integer.valueOf(commandMsgToUI.getParam());
                    if (rgTriggerSource.getSelected().getIndex() != sourceIndex) {
                        rgTriggerSource.setSelectedIndex(sourceIndex);
                        onCheckChanged(rgTriggerSource, rgTriggerSource.getSelected(), false);
                    }
                    break;
            }
        }
    };

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            boolean isFromEventBus = false;
            if (view.getId() == R.id.openChannelDetail) {
                if (!state) {
                    rgLevelSelect.setEnabled(false);
                    tvLevelDetail.setEnabled(false);
                    tvLevelDetail.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable));
//                    tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext_unclickable);
                } else {
                    rgLevelSelect.setEnabled(true);
                    tvLevelDetail.setEnabled(true);
                    tvLevelDetail.setTextColor(getResources().getColor(R.color.textColor));
//                    tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext);
                }

                Command.get().getAuto().setChannel(state, false);
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_CHANNEL, String.valueOf(state ? 0 : 1));
                if (!isFromEventBus) {
                    Auto.getInstance().setAutoChannelEnable(state);
                }
                msgAutoDetail.setOpenChannel(state);
                sendMsg(isFromEventBus);
            }
        }
    };

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangeListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            onCheckChanged(view, item, false);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            int levelMV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL);
            if (rgLevelSelect.getSelected().getIndex() == 0) {
                dialogTextKeyBoard.setDataDouble(Double.parseDouble(getVFromMV()), 2, 3, onDialogDismissListener);
            } else {
                dialogTextKeyBoard.setData(String.valueOf(levelMV), TopDialogTextKeyBoard.INPUT_TYPE_NUMBER_INT, 5, onDialogDismissListener);
            }
        }
    };

    private TopDialogTextKeyBoard.OnDialogDismissListener onDialogDismissListener = new TopDialogTextKeyBoard.OnDialogDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvLevelDetail, result, false);
        }
    };

    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) {
        if (view.getId() == R.id.triggerSource) {
            Command.get().getAuto().setSource(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_SOURCE, String.valueOf(item.getIndex()));
            if (!isFromEventBus) {
                Auto.getInstance().setAutoTriggerSource(item.getIndex());
            }
            msgAutoDetail.setTriggerSource(item);
            sendMsg(isFromEventBus);
        } else if (view.getId() == R.id.levelSelect) {
            //切换时，只是改变了数据的单位，实际数据并没有变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELSELECT, String.valueOf(item.getIndex()));
            int levelMV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL);
            if (item.getIndex() == 0) {
                tvLevelDetail.setText(getVFromMV());
            } else {
                tvLevelDetail.setText(String.valueOf(levelMV));
            }
        }
    }

    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) {
        if (tv.getId() == tvLevelDetail.getId()) {
            if (StrUtil.isEmpty(result)) {
                result = "0";
            }
            double d = Double.parseDouble(result);
            int levelMV;
            if (rgLevelSelect.getSelected().getIndex() == 0) {
                levelMV = (int) (d * 1000);
            } else {
                levelMV = (int) d;
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL, String.valueOf(levelMV));
            Command.get().getAuto().setLevel(Double.parseDouble(getVFromMV()), false);
            if (!isFromEventBus) {
                Auto.getInstance().setAutoThresholdLevel(Float.parseFloat(getVFromMV()));
            }
            if (rgLevelSelect.getSelected().getIndex() == 0) {
                tvLevelDetail.setText(getVFromMV());
            } else {
                tvLevelDetail.setText(String.valueOf(levelMV));
            }
            msgAutoDetail.setLevelDetail(levelMV);
            sendMsg(isFromEventBus);
        }
    }
}
