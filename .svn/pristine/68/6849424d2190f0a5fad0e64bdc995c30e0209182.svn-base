package com.micsig.tbook.tbookscope.top.layout.save;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.ui.top.view.TopViewSave;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

public class TopLayoutSaveTxt extends Fragment {
    private Context context;

    private TopViewRadioGroup saveIn;
    private TopViewSave saveName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_txt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
    }

    private void initView(View view) {
        saveIn = (TopViewRadioGroup) view.findViewById(R.id.saveIn);
        saveName = (TopViewSave) view.findViewById(R.id.saveName);
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public ISaveDetail getSaveDetail() {
        return null;
    }
}
