package com.micsig.tbook.tbookscope.top.layout.measure;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

public class TopLayoutMeasureCounter extends Fragment {
    private Context context;

    private TopViewRadioGroup measureCounterType;
    private TopViewRadioGroup measureCounterSource;
    private TextView reset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_measure_counter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
    }

    private void initView(View view) {
        measureCounterType = (TopViewRadioGroup) view.findViewById(R.id.measureCounterType);
        measureCounterSource = (TopViewRadioGroup) view.findViewById(R.id.measureCounterSource);
        reset = (TextView) view.findViewById(R.id.reset);
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public IMeasureDetail getMeasureDetail() {
        return null;
    }
}
