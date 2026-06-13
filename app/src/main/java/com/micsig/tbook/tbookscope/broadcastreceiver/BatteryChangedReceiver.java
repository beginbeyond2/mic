package com.micsig.tbook.tbookscope.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.main.BatteryView;

import java.util.Calendar;
import java.util.Objects;

/**
 * @auother Liwb
 * @description:
 * @data:2022-4-14 9:32
 */
public class BatteryChangedReceiver extends BroadcastReceiver {
    private static final int MSG_UPDATETIME = 31;//时间显示更新
    private static final int MSG_TIP_DISPLAY_TB = 32;//时基改变提示显示
    private static final int MSG_TIP_DISPLAY_TB_GONE = 33;//时基改变提示消失
    private static final int MSG_BATTERY_CHARGE = 34;//电池进入充电状态
    private static final int MSG_BATTERY_DISCHARGE = 35;//电池进入非充电状态
    private static final int MSG_BATTERY_SELF_UPDATE = 36;//电池电量充电时的自动更新
    private static final int MSG_TIMEBASE = 37;
    private DialogOk dialogOk;
    private Context context;
    private BatteryView tvBattery;
    private TextView tvTime;
    private TimeThread timeThread;
    public void setBatteryControl(BatteryView tvBattery){
        this.tvBattery=tvBattery;
        if(!HardwareProduct.isBattery()){
            tvBattery.setVisibility(View.GONE);
        }
    }
    public void setDialogOk(DialogOk dialogOk){
        this.dialogOk=dialogOk;
    }
    public void setTimeControl(TextView tvTime){
        this.tvTime=tvTime;
        if (timeThread==null){
            timeThread= new TimeThread();
            timeThread.start();
        }
    }


    private class TimeThread extends Thread {
        Message msg;

        public TimeThread() {
            msg = new Message();
        }

        @Override
        public void run() {
            super.run();
            do {
                try {
                    msg = handler.obtainMessage();
                    msg.what = MSG_UPDATETIME;
                    handler.sendMessage(msg);
                    Thread.sleep(1000 * 20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    private  Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATETIME:
                    Calendar calendar = Calendar.getInstance();
                    int hourInt = Tools.is24HourFormat()
                            ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
//                    int hourInt=calendar.get(Calendar.HOUR_OF_DAY);
                    int minInt = calendar.get(Calendar.MINUTE);
                    String hourStr = String.valueOf(hourInt < 10 ? "0" + hourInt : hourInt);
                    String minStr = String.valueOf(minInt < 10 ? "0" + minInt : minInt);
                    tvTime.setText(hourStr + ":" + minStr);
                    break;
                case MSG_TIP_DISPLAY_TB:
//                    tvBriefDisplayTB.setVisibility(View.VISIBLE);
//                    String str;
//                    if (btnCenterTimeBase.getText().toString().contains("\n")) {
//                        str = btnCenterTimeBase.getText().toString().split("\n")[1];
//                    } else {
//                        str = btnCenterTimeBase.getText().toString();
//                    }
//                    tvBriefDisplayTB.setText(str);
//                    if (handler.hasMessages(MSG_TIP_DISPLAY_TB_GONE)) {
//                        handler.removeMessages(MSG_TIP_DISPLAY_TB_GONE);
//                    }
//                    handler.sendEmptyMessageDelayed(MSG_TIP_DISPLAY_TB_GONE, 2000);
                    break;
                case MSG_TIP_DISPLAY_TB_GONE:
//                    tvBriefDisplayTB.setVisibility(View.GONE);
                    break;
                case MSG_BATTERY_CHARGE:
//                    Logger.i(Command.TAG,"dialogOK:"+dialogOk+",tvBattery:"+tvBattery);
                    if (dialogOk==null || tvBattery==null)break;
                    if (dialogOk.isShow() && Objects.equals(dialogOk.getText(), context.getResources().getString(R.string.msgBatteryLow))) {
                        dialogOk.hide();
                    }
                    tvBattery.setLevel(msg.arg1);
                    tvBattery.setIcon(true);
//                    tvBattery.setText(msg.arg1 + "%");
//                    if (handler.hasMessages(MSG_BATTERY_SELF_UPDATE)) {
//                        handler.removeMessages(MSG_BATTERY_SELF_UPDATE);
//                    }
//                    handler.sendEmptyMessageDelayed(MSG_BATTERY_SELF_UPDATE, 800);
                    break;
                case MSG_BATTERY_DISCHARGE:
//                    Logger.i(Command.TAG,"dialogOK:"+dialogOk+",tvBattery:"+tvBattery);
                    if (dialogOk==null || tvBattery==null)break;
                    if (tvBattery.getLevel() >= 20 && msg.arg1 < 20) {
                        dialogOk.setData(R.string.msgBatteryLow, null, null);
                    }
                    tvBattery.setLevel(msg.arg1);
                    tvBattery.setIcon(false);
//                    if (handler.hasMessages(MSG_BATTERY_SELF_UPDATE)) {
//                        handler.removeMessages(MSG_BATTERY_SELF_UPDATE);
//                    }
                    break;
                case MSG_BATTERY_SELF_UPDATE:
//                    tvBattery.selfUpdate();
//                    handler.sendEmptyMessageDelayed(MSG_BATTERY_SELF_UPDATE, 800);
                    break;
                case MSG_TIMEBASE:
//                    if (Scope.getInstance().isZoom()) {
//                        bgTimeBase.setBackgroundResource(R.drawable.ic_rectangle_6_zoom);
//                    } else {
//                        bgTimeBase.setBackgroundResource(R.drawable.ic_rectangle_6);
//                    }
//                    btnLeftTimeBase.setBackgroundResource(R.drawable.ic_timebase_left);
//                    btnRightTimeBase.setBackgroundResource(R.drawable.ic_timebase_right);
                    break;
            }
        }
    };



    @Override
    public void onReceive(Context context, Intent intent) {
        this.context =context;
        String action = intent.getAction();
        if (action == null) return;
        if(!HardwareProduct.isBattery()) return;
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);//电量当前值
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);//电量最大值
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            Message message = new Message();
            message.arg1 = level;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:        //正在充电,2
                    message.what = MSG_BATTERY_CHARGE;
                    handler.sendMessage(message);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:            //充满,5
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:    //没有充电,4
                case BatteryManager.BATTERY_STATUS_DISCHARGING:     //放电,3
                default:
                    message.what = MSG_BATTERY_DISCHARGE;
                    handler.sendMessage(message);
                    break;
            }
            if (status != BatteryManager.BATTERY_STATUS_FULL) {
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                Message message2 = new Message();
                message2.arg1 = level;
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:             //连接的交流变电器
                    case BatteryManager.BATTERY_PLUGGED_USB:            //连接的usb
                        message2.what = MSG_BATTERY_CHARGE;
                        handler.sendMessage(message2);
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:       //连接的无线电源
                    default:
                        message.what = MSG_BATTERY_DISCHARGE;
                        //handler.sendMessage(message);
                        break;
                }
            }
        } else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)) {
            // 表示当前电池电量低
            DToast.get().show(R.string.msgBatteryLow);
        } else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_OKAY)) {
            // 表示当前电池已经从电量低恢复为正常
        }
    }

}
