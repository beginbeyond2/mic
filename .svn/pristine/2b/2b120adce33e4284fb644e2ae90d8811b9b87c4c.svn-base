package com.micsig.tbook.tbookscope.main.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/6/27.
 */

public class MainDialogMenuHalf extends RelativeLayout {
    private static final String TAG = "MainDialogMenuHalf";

    private Context context;
    private OnDismissListener onDismissListener;

    private Button timeBase, level, cursorH, cursorV;
    private Button ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8;

    private ViewGroup rootViewGroup;
    private Rect rectBtnPer50;

    private final int channelCount = GlobalVar.get().getChannelsCount();
    public void setRectBtnPer50(Rect rectBtnPer50){
        this.rectBtnPer50=rectBtnPer50;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public MainDialogMenuHalf(Context context) {
        this(context, null);
    }

    public MainDialogMenuHalf(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainDialogMenuHalf(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initControl();
    }

    private void initView() {
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_menuhalf_eight, this);
        } else {
            rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_menuhalf_four, this);
        }
        findViewById(R.id.menuhalf_outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (rectBtnPer50!=null && rectBtnPer50.contains((int)event.getX(),(int)event.getY())){
                    return false;
                }
                hide();
                return false;
            }
        });

        timeBase = (Button) rootViewGroup.findViewById(R.id.menuhalf_timeBase);
        level = (Button) rootViewGroup.findViewById(R.id.menuhalf_level);
        cursorH = (Button) rootViewGroup.findViewById(R.id.menuhalf_cursorH);
        cursorV = (Button) rootViewGroup.findViewById(R.id.menuhalf_cursorV);
        ch1 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch1);
        ch2 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch2);
        ch3 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch3);
        ch4 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch4);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            ch5 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch5);
            ch6 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch6);
            ch7 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch7);
            ch8 = (Button) rootViewGroup.findViewById(R.id.menuhalf_ch8);
        }

        timeBase.setOnClickListener(onClickListener);
        level.setOnClickListener(onClickListener);
        cursorH.setOnClickListener(onClickListener);
        cursorV.setOnClickListener(onClickListener);
        ch1.setOnClickListener(onClickListener);
        ch2.setOnClickListener(onClickListener);
        ch3.setOnClickListener(onClickListener);
        ch4.setOnClickListener(onClickListener);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            ch5.setOnClickListener(onClickListener);
            ch6.setOnClickListener(onClickListener);
            ch7.setOnClickListener(onClickListener);
            ch8.setOnClickListener(onClickListener);
        }

        if (channelCount != GlobalVar.CHANNEL_COUNT_8) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2) {
                ch3.setVisibility(View.INVISIBLE);
                ch4.setVisibility(View.INVISIBLE);
            } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
                ch3.setVisibility(View.VISIBLE);
                ch4.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }



    public void show() {
        if (WorkModeManage.getInstance().isXyMode()){
            timeBase.setEnabled(false);
            level.setEnabled(false);
            cursorH.setEnabled(false);
            cursorV.setEnabled(false);
        }else{
            timeBase.setEnabled(true);
            level.setEnabled(true);
            cursorH.setEnabled(true);
            cursorV.setEnabled(true);
        }
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MENUHALF);
        Tools.PrintControlsLocation("MainDialogMenuHalf",rootViewGroup);
    }

    public void hide() {
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MENUHALF);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            Logger.i("MainDialogMenuHalf");
            Screen.getViewLocation(v);
            switch (v.getId()) {
                case R.id.menuhalf_timeBase:
                    TriggerTimebase.getInstance().rstX_50percentt();
                    break;
                case R.id.menuhalf_level:
                    ExternalKeysCommand.get().clickTriggerLevelCenter();
                    break;
                case R.id.menuhalf_cursorH:
                    CursorManage.getInstance().initCursorY();
                    break;
                case R.id.menuhalf_cursorV:
                    CursorManage.getInstance().initCursorX();
                    break;
                case R.id.menuhalf_ch1:
                    WaveManage.get().setCenterChY(TChan.Ch1);
                    break;
                case R.id.menuhalf_ch2:
                    WaveManage.get().setCenterChY(TChan.Ch2);
                    break;
                case R.id.menuhalf_ch3:
                    WaveManage.get().setCenterChY(TChan.Ch3);
                    break;
                case R.id.menuhalf_ch4:
                    WaveManage.get().setCenterChY(TChan.Ch4);
                    break;
                case R.id.menuhalf_ch5:
                    WaveManage.get().setCenterChY(TChan.Ch5);
                    break;
                case R.id.menuhalf_ch6:
                    WaveManage.get().setCenterChY(TChan.Ch6);
                    break;
                case R.id.menuhalf_ch7:
                    WaveManage.get().setCenterChY(TChan.Ch7);
                    break;
                case R.id.menuhalf_ch8:
                    WaveManage.get().setCenterChY(TChan.Ch8);
                    break;
            }
            hide();
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_MENU_HALF_CHANNEL: {
                    //将通道位置设置为垂直零点位置（波形显示区垂直中心)
                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());
                    WaveManage.get().setCenterChY(chIndex);
                }
                break;
                case CommandMsgToUI.FLAG_MENU_TRIGPOS: {
//                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());
                    TriggerTimebase.getInstance().rstX_50percentt();
                }
                break;
                case CommandMsgToUI.FLAG_MENU_XCURSOR: {
                    CursorManage.getInstance().initCursorX();
                }
                break;
                case CommandMsgToUI.FLAG_MENU_YCURSOR: {
                    CursorManage.getInstance().initCursorY();
                }
                break;
                case CommandMsgToUI.FLAG_MENU_LEVEL: {
                    //将触发电平设置为触发信号幅值的中间位置
//                    int chIndex = Integer.parseInt(commandMsgToUI.getParam());
                    ExternalKeysCommand.get().clickTriggerLevelCenter();
                }
                break;

            }
        }
    };
}
