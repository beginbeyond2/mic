package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import io.reactivex.rxjava3.functions.Consumer;


public class TopLayoutUsersetCapture extends Fragment {
    private Context context;
    private TopViewRadioGroup tvTimestamp;
    private TopViewRadioGroup tvScreenInvert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetcapture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initView(View view) {
        tvTimestamp = (TopViewRadioGroup) view.findViewById(R.id.timestamp);
        tvTimestamp.setData(R.string.timestamp, R.array.timestamp, onCheckChangedListener);
        tvScreenInvert = (TopViewRadioGroup) view.findViewById(R.id.screenInvert);
        tvScreenInvert.setData(R.string.screenInvert, R.array.screenInvert, onCheckChangedListener);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private void setCache() {
        boolean timestamp = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP);
        tvTimestamp.setSelectedIndex(timestamp ? 0 : 1);
        boolean screenInvert = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT);
        tvScreenInvert.setSelectedIndex(screenInvert ? 0 : 1);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            if (view.getId() == R.id.timestamp) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP, String.valueOf(item.getIndex() == 0));
            } else if (view.getId() == R.id.screenInvert) {
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT, String.valueOf(item.getIndex() == 0));
            }
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {

        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }
    };
}
