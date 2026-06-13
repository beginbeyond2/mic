package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.ui.util.ScreenUtil;

public class MainLayoutCenterSerialsWordTip extends Fragment {
    private TextView tvTip;
    private Button btnClear;

    private int type = ISerialsWord.TYPE_S1;

    public void setChType(int type) {
        this.type = type;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_tip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tvTip = (TextView) view.findViewById(R.id.tip);
        btnClear  = (Button) view.findViewById(R.id.clear);
        btnClear.setOnClickListener(onClickListener);
        btnClear.setVisibility(App.IsDebug() ? View.VISIBLE : View.GONE);
    }

    public void setTip(String tip) {
        tvTip.setText(tip);
    }
    public void setTip(int resId){tvTip.setText(resId);}
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
        }
    };
}
