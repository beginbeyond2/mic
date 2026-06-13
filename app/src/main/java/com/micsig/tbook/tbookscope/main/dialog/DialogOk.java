package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * Created by liwb on 2018/8/7.
 */

public class DialogOk extends RelativeLayout {
    private static final String TAG = "DialogOk";

    private Context context;
    private TextView tvPrompt;
    private Button btnOk;
    private Object data;
    private OnOkClickListener onOkClickListener;

    private boolean exitToHome;
    private ViewGroup rootViewGroup;

    public interface OnOkClickListener {
        /**
         * @param data dialog 点击确定后，需要传递的消息
         */
        void onClick(View v, Object data);
    }

    public DialogOk(Context context) {
        this(context, null);
    }

    public DialogOk(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogOk(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        rootViewGroup= (ViewGroup) inflate(context, R.layout.dialog_ok, this);

        tvPrompt = (TextView) findViewById(R.id.txtPrompt);
        btnOk = findViewById(R.id.btnOK);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();
                Logger.i(TAG, "BtnOK");//[360, 300]	80, 40
                hide();
                if (onOkClickListener != null) {
                    onOkClickListener.onClick(DialogOk.this, data);
                }
            }
        });

    }

    //region 公共

    public Button getBtnOk() {
        return btnOk;
    }

    public boolean isDialogExitToHome() {
        return exitToHome;
    }

    /**
     * @param msgResId          dialog 显示的消息的资源id
     * @param data              dialog 点击确定后，需要传递的消息
     * @param onOkClickListener dialog 点击确定后，需要执行的动作
     */
    public void setData(int msgResId, Object data, OnOkClickListener onOkClickListener) {
        exitToHome = data != null && data instanceof MainMsgExitToHome;
        tvPrompt.setText(msgResId);
        this.data = data;
        this.onOkClickListener = onOkClickListener;
        show();
    }

    public void setData(String msg, Object data, OnOkClickListener onOkClickListener) {
        exitToHome = data != null && data instanceof MainMsgExitToHome;
        tvPrompt.setText(msg);
        this.data = data;
        this.onOkClickListener = onOkClickListener;
        show();
    }
    //endregion

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_OK);
        Tools.PrintControlsLocation("DialogOk",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_OK);
    }

    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    public String getText() {
        return tvPrompt.getText().toString();
    }
}
