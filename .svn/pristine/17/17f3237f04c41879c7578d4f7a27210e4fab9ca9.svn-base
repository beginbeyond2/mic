package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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

public class DialogOkCancel extends RelativeLayout {
    private static final String TAG = "DialogOkCancel";

    private Context context;
    private TextView tvPrompt;
    private Object okData, cancelData;
    private OnOkCancelClickListener onOkCancelClickListener;
    private View clickView;

    private boolean exitToHome;
    private ViewGroup rootViewGroup;

    public interface OnOkCancelClickListener {
        /**
         * @param data dialog 点击确定后，需要传递的消息
         */
        void onOkClick(View v, Object data);

        /**
         * @param data dialog 点击取消后，需要传递的消息
         */
        void onCancelClick(View v, Object data);

        void onDialogClose(View v);
    }

    public DialogOkCancel(Context context) {
        this(context, null);
    }

    public DialogOkCancel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogOkCancel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        rootViewGroup= (ViewGroup) inflate(context, R.layout.dialog_okcancel, this);

        tvPrompt = (TextView) findViewById(R.id.txtPrompt);
        findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();
                Logger.i(TAG, "BtnOK");//[210, 302]	69	38
                hide();
                onOkCancelClickListener.onOkClick(clickView, okData);
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();
                Logger.i(TAG, "BtnCancel");//[521, 302]	69	38
                hide();
                onOkCancelClickListener.onCancelClick(clickView, cancelData);
            }
        });

    }

    //region 公共

    public boolean isDialogExitToHome() {
        return exitToHome;
    }

    /**
     * @param clickView
     * @param msgResId          dialog 显示的消息的资源id
     * @param okData            dialog 点击确定后，需要传递的消息
     * @param cancelData        dialog 点击取消后，需要传递的消息
     * @param onOkCancelClickListener dialog 点击确定后，需要执行的动作
     */
    public void setData(View clickView, int msgResId, Object okData, Object cancelData, OnOkCancelClickListener onOkCancelClickListener) {
        exitToHome = okData != null && okData instanceof MainMsgExitToHome;
        tvPrompt.setText(msgResId);
        this.clickView = clickView;
        this.okData = okData;
        this.cancelData = cancelData;
        this.onOkCancelClickListener = onOkCancelClickListener;
        show();
    }
    //endregion

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_OKCANCEL);
        Tools.PrintControlsLocation("DialogOkCancel",rootViewGroup);
    }

    public void hide() {
        onOkCancelClickListener.onDialogClose(clickView);
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_OKCANCEL);
    }
    public boolean isShow(){
        return getVisibility()==VISIBLE;
    }

    public void pressOK(){
        findViewById(R.id.btnOK).performClick();
    }
    public void PressCancel(){
        findViewById(R.id.btnCancel).performClick();
    }
}
