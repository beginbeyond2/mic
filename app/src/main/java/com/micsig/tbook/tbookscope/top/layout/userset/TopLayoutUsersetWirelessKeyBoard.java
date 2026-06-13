package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.BuildConfig;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysOnBindService;
import com.micsig.tbook.tbookscope.tools.PlaySound;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutUsersetWirelessKeyBoard extends Fragment {

    private Context context;
    private Button btnWireless;
    private TextView tvWireless;

    private TextView tvWirelessBattery;

    private DialogOk dialogOk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetwirelesskeyboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.WIRELESS_KEYBOARD_STAT).subscribe(consumerWirelessKeyboard);
    }

    private void initView(View view) {
        btnWireless = (Button) view.findViewById(R.id.btnWireless);
        tvWireless = (TextView) view.findViewById(R.id.txtWirelessSn);
        tvWirelessBattery = (TextView) view.findViewById(R.id.txtWirelessBattery);
        dialogOk = (DialogOk) getActivity().findViewById(R.id.dialogOk);
        btnWireless.setOnClickListener(onClickListener);
        displaysn();
    }
    int wirelessBatteryLevel = 0;
    boolean bFirst = false;
    boolean bDialog = false;

    int showBatteryLevel = 0;

    private void displaysn(){

        if(ExternalKeysOnBindService.wirelessId !=0 ){
            tvWireless.setText(String.format("%010d", ExternalKeysOnBindService.wirelessId));

            if((SystemClock.elapsedRealtime() - ExternalKeysOnBindService.wirelessBatteryHeartbeat) < 3000){
                if(!bFirst){
                    wirelessBatteryLevel = 100;
                    showBatteryLevel = 100;
                    bFirst = true;
                }
                int level = ExternalKeysOnBindService.wirelessBattery & 0xFF;
                int s = (ExternalKeysOnBindService.wirelessBattery >> 8) & 0xFF;


                if(level > 100) level = 100;
                else if(level < 0) level = 0;

                if(BuildConfig.DEBUG){
                    int vol = (ExternalKeysOnBindService.wirelessBattery >>>16) & 0xFFFF;
                    tvWirelessBattery.setText("" + level + "%" + " :" + vol);
                }else{
                    tvWirelessBattery.setText("" + level + "%");
                }
                if(s != 0){
                    level = 20;
                }

                if(level < 10){
                    if(showBatteryLevel >= 15){
                        dialogOk.setData(R.string.msgKeyBoardBatteryLow, null, null);
                        bDialog = true;
                        showBatteryLevel = 10;
                    }
                }else if(level < 20){
                    if(showBatteryLevel >= 25){
                        dialogOk.setData(R.string.msgKeyBoardBatteryLow, null, null);
                        bDialog = true;
                        showBatteryLevel = 15;
                    }
                }else {
                    if(showBatteryLevel < 20){
                        if(dialogOk.isShow()){
                            dialogOk.hide();
                        }
                        bDialog = false;
                    }
                }
                wirelessBatteryLevel = level;
            }else{
                bFirst = false;
                tvWirelessBattery.setText("");
                if(bDialog){
                    if(dialogOk.isShow()){
                        dialogOk.hide();
                    }
                    bDialog = false;
                }
            }
        }else{
            bFirst = false;
            tvWireless.setText("");
            tvWirelessBattery.setText("");
            if(bDialog){
                if(dialogOk.isShow()){
                    dialogOk.hide();
                }
                bDialog = false;
            }
        }
        tvWirelessBattery.postDelayed(new Runnable() {
            @Override
            public void run() {
                displaysn();
            }
        },1000);
    }

    private Consumer<TopMsgWirelessKeyboard> consumerWirelessKeyboard = new Consumer<TopMsgWirelessKeyboard>() {
        @Override
        public void accept(TopMsgWirelessKeyboard wirelessKeyboard) throws Exception {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.unbindWirelessId();
        }
    };
}