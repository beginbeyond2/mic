package com.micsig.tbook.tbookscope.top.layout.display;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by limh on 2024/8/8.
 */

public class TopLayoutDisplayTxtMix extends Fragment {
    private Context context;
    private CheckBox checkS1, checkS2, checkS3, checkS4;
    private OnDetailSendMsgListener onDetailSendMsgListener;
    private ViewGroup rootView;
    private TopMsgDisplayTxtMix displayDetail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_displaytxtmix, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initView(View view) {
        rootView = (ViewGroup) view;
        checkS1 = (CheckBox) view.findViewById(R.id.check_s1);
        checkS2 = (CheckBox) view.findViewById(R.id.check_s2);
        checkS3 = (CheckBox) view.findViewById(R.id.check_s3);
        checkS4 = (CheckBox) view.findViewById(R.id.check_s4);

        checkS1.setOnClickListener(onClickListener);
        checkS2.setOnClickListener(onClickListener);
        checkS3.setOnClickListener(onClickListener);
        checkS4.setOnClickListener(onClickListener);
    }

    private void initData() {
        displayDetail = new TopMsgDisplayTxtMix();
        displayDetail.setS1Select(checkS1.isChecked());
        displayDetail.setS2Select(checkS2.isChecked());
        displayDetail.setS3Select(checkS3.isChecked());
        displayDetail.setS4Select(checkS4.isChecked());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
    }


    private void setCache() {
        //是否选择了Txt组合
        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);
        checkS1.setChecked(s1Select);
        checkS2.setChecked(s2Select);
        checkS3.setChecked(s3Select);
        checkS4.setChecked(s4Select);

        //通道是否打开，未打开时置灰
        boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        //全部可点击
        checkS1.setEnabled(true);
        checkS2.setEnabled(true);
        checkS3.setEnabled(true);
        checkS4.setEnabled(true);
        //更新数据
        displayDetail.setS1Select(s1Select);
        displayDetail.setS2Select(s2Select);
        displayDetail.setS3Select(s3Select);
        displayDetail.setS4Select(s4Select);
        sendMsg(false);
    }

    private void sendMsg(boolean isFromEventBus) {
        Tools.PrintControlsLocation("TopLayoutDisplayTxtMix", rootView);
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    public TopMsgDisplayTxtMix getDisplayDetail() {
        return displayDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.check_s1:
                displayDetail.setS1Select(checkS1.isChecked());
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1, String.valueOf(checkS1.isChecked()));
                break;
            case R.id.check_s2:
                displayDetail.setS2Select(checkS2.isChecked());
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2, String.valueOf(checkS2.isChecked()));
                break;
            case R.id.check_s3:
                displayDetail.setS3Select(checkS3.isChecked());
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3, String.valueOf(checkS3.isChecked()));
                break;
            case R.id.check_s4:
                displayDetail.setS4Select(checkS4.isChecked());
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4, String.valueOf(checkS4.isChecked()));
                break;
        }
        sendMsg(false);
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            setCache();
        }
    };

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            setCache();
        }
    };

}
