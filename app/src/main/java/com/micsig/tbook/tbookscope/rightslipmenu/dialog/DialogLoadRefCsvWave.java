package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.functions.Consumer;

public class DialogLoadRefCsvWave extends ConstraintLayout {

    private static final String TAG = "DialogLoadRefCsvWave";
    private final Context context;
    private ConstraintLayout rootViewGroup;
    private OnDismissListener dismissListener;
    private LinearLayout linearLayout;
    private TopViewChannelMultipleChoice finalRefChoice, channelChoice, mathChoice, refChoice;
    private final HashMap<Integer, Boolean> refOpenMap = new HashMap<>();//当前Ref通道的打开状态
    private final List<CheckBox> allContentCheckBox = new ArrayList<>();
    private final ConcurrentHashMap<Integer, Integer> channelToRef = new ConcurrentHashMap<>();// ref channel
    private View finalRefChoiceView, channelChoiceView, mathChoiceView, refChoiceView;

    public interface OnDismissListener {
        void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef);
    }

    public DialogLoadRefCsvWave(Context context) {
        this(context, null);
    }

    public DialogLoadRefCsvWave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogLoadRefCsvWave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initControl() {
        //EventFactory.addEventObserver(EventFactory.EVENT_LOADCSV_RUN, eventLoadCsvObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
    }

    public void setData(List<Integer> channelInCsv, OnDismissListener dismissListener) {
        channelToRef.clear();
        this.dismissListener = dismissListener;
        updateFinalRefChoiceView();
        updateCsvChoiceState(channelInCsv);
        show();
    }

    public void initView() {
        initNameToChan();
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_load_ref_csv, this);
        linearLayout = rootViewGroup.findViewById(R.id.ll_content);
        Button btnOk = findViewById(R.id.btnOK);
        btnOk.setOnClickListener(onClickListener);
        View outView = rootViewGroup.findViewById(R.id.outView);
        setContentView();
        outView.setOnClickListener(onClickListener);
    }

    private void setContentView() {
        finalRefChoice = new TopViewChannelMultipleChoice(context, false, onTopItemOnlyClickListener);
        channelChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);
        mathChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);
        refChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);

        finalRefChoice.setData(R.array.popArrayRef, R.array.popArrayRefColor, 110, 90, 2);
        channelChoice.setData(R.array.channelsNameEight, R.array.popArrayChannelEightColor, 110, 70, 0);
        mathChoice.setData(R.array.popArrayMath, R.array.popArrayMathColor, 110, 70, 1);
        refChoice.setData(R.array.popArrayRef, R.array.popArrayRefColor, 110, 70, 2);

        finalRefChoiceView = finalRefChoice.getInflate();
        channelChoiceView = channelChoice.getInflate();
        mathChoiceView = mathChoice.getInflate();
        refChoiceView = refChoice.getInflate();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.START;
        layoutParams.setMarginStart(10);

        finalRefChoiceView.setLayoutParams(layoutParams);
        channelChoiceView.setLayoutParams(layoutParams);
        mathChoiceView.setLayoutParams(layoutParams);
        refChoiceView.setLayoutParams(layoutParams);

        linearLayout.addView(finalRefChoiceView, 0);
        linearLayout.addView(channelChoiceView, 2);
        linearLayout.addView(mathChoiceView, 3);
        linearLayout.addView(refChoiceView, 4);

        allContentCheckBox.clear();
        for (int i = 0; i < channelChoice.getCheckBoxs().getChildCount(); i++) {
            allContentCheckBox.add((CheckBox) channelChoice.getCheckBoxs().getChildAt(i));
        }

        for (int i = 0; i < mathChoice.getCheckBoxs().getChildCount(); i++) {
            allContentCheckBox.add((CheckBox) mathChoice.getCheckBoxs().getChildAt(i));
        }

        for (int i = 0; i < refChoice.getCheckBoxs().getChildCount(); i++) {
            allContentCheckBox.add((CheckBox) refChoice.getCheckBoxs().getChildAt(i));
        }
    }

    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.outView:
                hide();
                break;
            case R.id.btnOK:
                sendChToRef();
                hide();
                break;
        }
    };

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_LOAF_REF_CSV);
        Tools.PrintControlsLocation(TAG, rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        //clear All
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {
            CheckBox refCheckBox = (CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i);
            refCheckBox.setChecked(false);
            refCheckBox.setText(refCheckBox.getText().toString().split("\\(")[0]);
        }
        for (CheckBox checkBox : allContentCheckBox) {
            checkBox.setChecked(false);
        }
        TChan.foreachRef(refChan -> {
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + refChan, String.valueOf(0));
        });
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_LOAF_REF_CSV);
    }

    private void sendChToRef() {
        if (channelToRef == null || dismissListener == null) return;
        dismissListener.onDismiss(channelToRef);
    }

    private HashMap<Integer, Boolean> updateRefChannelState() {//更新当前Ref的显示状态
        refOpenMap.clear();
        TChan.foreachRef(refChan -> {
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);
            refOpenMap.put(refChan, refAddByUser);
        });
        return refOpenMap;
    }

    private void updateFinalRefChoiceView() {
        updateRefChannelState();
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {
            ((CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i)).setChecked(refOpenMap.get(i + TChan.R1));
            ((CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i)).setTextColor(TChan.getChannelColor(context, TChan.toRefTChan(TChan.toUiChNo(i))));
        }
    }

    private void updateCsvChoiceState(List<Integer> channelInCsv) {
        for (CheckBox checkBox : allContentCheckBox) {
            checkBox.setEnabled(false);
            checkBox.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewRightViewDisable));
        }
        for (Integer index : channelInCsv) {
            allContentCheckBox.get(index).setEnabled(true);
            allContentCheckBox.get(index).setTextColor(TChan.getChannelColor(context, TChan.toUiChNo(index)));
        }
    }

    private void updateTopCheckState(CheckBox bottomCheckBox) {
        String bottomText = bottomCheckBox.getText().toString();
        int chan = getChannelByName(bottomText);
        boolean canAdd = false;
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {
            CheckBox refCheckBox = (CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i);
            String refText = refCheckBox.getText().toString();
            int refChan = getChannelByName(refText.split("\\(")[0]);
            if (bottomCheckBox.isChecked()) {
                if (refCheckBox.isChecked()) continue;
                canAdd = true;
                refCheckBox.setChecked(true);
                refCheckBox.setTag(chan);
                refCheckBox.setText(String.format("%s(%s)", refText, bottomText));
            } else {
                if (!(refText.contains("(")
                        && refText.split("\\(")[1].replace(")", "")
                        .equals(bottomText))) continue;
                refCheckBox.setChecked(false);
                refCheckBox.setTag(null);
                canAdd = true;
                channelToRef.remove(refChan);
                refCheckBox.setText(refText.split("\\(")[0]);
            }
            Object tag = refCheckBox.getTag();
            if (tag == null) continue;
            if (tag instanceof Integer) {
                int chanTag = (Integer) tag;
                channelToRef.put(refChan, chanTag);
            }
            break;
        }
        if (!canAdd) {
            bottomCheckBox.setChecked(false);
            DToast.get().show(context.getResources().getString(R.string.all_ref_checked));
        }
        finalRefChoice.updateChild();
    }


    private void updateBottomCheckState(CheckBox topCheckBox) {
        int refChan = getChannelByName(topCheckBox.getText().toString().split("\\(")[0]);
        if (!topCheckBox.isChecked()) {
            topCheckBox.setText(topCheckBox.getText().toString().split("\\(")[0]);
            if (!channelToRef.containsKey(refChan)) return;
            int channel = channelToRef.get(refChan);//map里ref对应的channel
            channelToRef.remove(refChan);
            String chName = getNameById(channel);
            for (CheckBox checkBox1 : allContentCheckBox) {
                if (!checkBox1.getText().toString().equals(chName)) continue;
                checkBox1.setChecked(false);
                break;
            }
        } else {
            boolean canAdd = false;
            for (CheckBox checkBox1 : allContentCheckBox) {
                if (checkBox1.isChecked() || !checkBox1.isEnabled()) continue;
                canAdd = true;
                checkBox1.setChecked(true);
                int channel = getChannelByName(checkBox1.getText().toString());
                topCheckBox.setTag(channel);
                channelToRef.put(refChan, channel);
                topCheckBox.setText(String.format("%s(%s)", topCheckBox.getText(), checkBox1.getText()));
                break;
            }
            if (!canAdd) {
                topCheckBox.setChecked(false);
                DToast.get().show(context.getResources().getString(R.string.dialog_no_ref_data_checked));
            }
        }
    }


    private TopViewChannelMultipleChoice.onItemOnlyClickListener onBottomItemOnlyClickListener = new TopViewChannelMultipleChoice.onItemOnlyClickListener() {

        @Override
        public void onlyClick(CheckBox checkBox) {
            //底部checkbox点击引起的变化
            updateTopCheckState(checkBox);
        }
    };

    private TopViewChannelMultipleChoice.onItemOnlyClickListener onTopItemOnlyClickListener = new TopViewChannelMultipleChoice.onItemOnlyClickListener() {

        @Override
        public void onlyClick(CheckBox checkBox) {
            //顶部checkbox点击引起的变化
            updateBottomCheckState(checkBox);
        }
    };


    private HashMap<String, Integer> mapChanName;

    private void initNameToChan() {
        if (mapChanName == null) {
            mapChanName = new HashMap<>();
            mapChanName.put("CH1", ChannelFactory.CH1);
            mapChanName.put("CH2", ChannelFactory.CH2);
            mapChanName.put("CH3", ChannelFactory.CH3);
            mapChanName.put("CH4", ChannelFactory.CH4);
            mapChanName.put("CH5", ChannelFactory.CH5);
            mapChanName.put("CH6", ChannelFactory.CH6);
            mapChanName.put("CH7", ChannelFactory.CH7);
            mapChanName.put("CH8", ChannelFactory.CH8);

            mapChanName.put("M1", ChannelFactory.MATH1);
            mapChanName.put("M2", ChannelFactory.MATH2);
            mapChanName.put("M3", ChannelFactory.MATH3);
            mapChanName.put("M4", ChannelFactory.MATH4);
            mapChanName.put("M5", ChannelFactory.MATH5);
            mapChanName.put("M6", ChannelFactory.MATH6);
            mapChanName.put("M7", ChannelFactory.MATH7);
            mapChanName.put("M8", ChannelFactory.MATH8);

            mapChanName.put("R1", ChannelFactory.REF1);
            mapChanName.put("R2", ChannelFactory.REF2);
            mapChanName.put("R3", ChannelFactory.REF3);
            mapChanName.put("R4", ChannelFactory.REF4);
            mapChanName.put("R5", ChannelFactory.REF5);
            mapChanName.put("R6", ChannelFactory.REF6);
            mapChanName.put("R7", ChannelFactory.REF7);
            mapChanName.put("R8", ChannelFactory.REF8);

            mapChanName.put("S1", ChannelFactory.S1);
            mapChanName.put("S2", ChannelFactory.S2);
            mapChanName.put("S3", ChannelFactory.S3);
            mapChanName.put("S4", ChannelFactory.S4);
        }
    }

    public int getChannelByName(String chName) {
        if (mapChanName == null) initNameToChan();
        return mapChanName.getOrDefault(chName, -1);
    }

    public String getNameById(int chan) {
        if (mapChanName == null) initNameToChan();
        String chName = "NULL";
        for (Map.Entry<String, Integer> entry : mapChanName.entrySet()) {
            if (entry.getValue().equals(chan)) {
                return entry.getKey();
            }
        }
        return chName;
    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            finalRefChoice.setChannelColorForDialogCSv(chIndex, colorStr);
//            channelChoice.setChannelColor(chIndex, colorStr);
            mathChoice.setChannelColorForDialogCSv(chIndex, colorStr);
            refChoice.setChannelColorForDialogCSv(chIndex, colorStr);
        }
    };

}
