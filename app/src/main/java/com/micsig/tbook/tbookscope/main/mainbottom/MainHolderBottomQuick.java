package com.micsig.tbook.tbookscope.main.mainbottom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Display.DisplayAction;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.dialog.MainDialogMenuHalf;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterMenu;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterSegmented;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand;
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.MainLayoutCenterSerialsWord;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgSerialsDetail;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.command.Command_Display;
import com.micsig.tbook.tbookscope.middleware.command.Command_Trigger;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.bean.YTZoomMsgDisplay;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                MainHolderBottomQuick - 底部快捷操作栏组件                        │
 * * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                    │
 * │   主界面底部快捷操作栏的视图持有者,提供截屏、Zoom、全测量、串口文本等快捷功能        │
 * │                                                                                │
 * │ 【核心职责】                                                                    │
 * │   1. 管理底部快捷按钮(截屏/Zoom/录像/全测量/串口文本/中心菜单)                    │
 * │   2. 处理快捷按钮的点击事件和状态切换                                            │
 * │   3. 控制Zoom模式的进入和退出                                                   │
 * │   4. 管理串口文本模式的切换和显示                                               │
 * │   5. 处理快速保存和自动保存任务                                                 │
 * │   6. 响应工作模式变化更新按钮可用状态                                            │
 * │   7. 同步顶部和左侧按钮的状态                                                   │
 * │                                                                                │
 * │ 【架构设计】                                                                    │
 * │   继承RecyclerView.ViewHolder作为列表项视图持有者                               │
 * │   通过RxBus订阅多个消息源实现响应式更新                                          │
 * │   使用EventFactory观察时基、采样率等底层事件                                    │
 * │   内置多个Consumer处理不同类型的消息                                            │
 * │   采用Handler处理延时消息和定时任务                                              │
 * │                                                                                │
 * │ 【数据流向】                                                                    │
 * │   用户点击 → MainHolderBottomQuick → Command → 底层硬件                        │
 * │   底层事件 → EventFactory → MainHolderBottomQuick → UI更新                    │
 * │   RxBus消息 → Consumer → 状态更新 → 按钮状态同步                               │
 * │   工作模式变化 → WorkModeManage → 按钮可用性更新                                │
 * │                                                                                │
 * │ 【依赖关系】                                                                    │
 * │   被依赖:MainViewGroup                                                          │
 * │   依赖:Command(命令系统)、RxBus(消息总线)、WorkModeManage(工作模式管理)         │
 * │         Scope(示波器核心)、CacheUtil(缓存工具)、MeasureManage(测量管理)         │
 * │         CursorManage(光标管理)、PlaySound(音效)、ScreenControls(屏幕控制)      │
 * │                                                                                │
 * │ 【使用场景】                                                                    │
 * │   1. 用户点击截屏按钮进行屏幕截图                                               │
 * │   2. 用户点击Zoom按钮进入/退出Zoom模式                                          │
 * │   3. 用户点击全测量按钮开启/关闭全测量显示                                       │
 * │   4. 用户点击串口文本按钮切换到文本显示模式                                     │
 * │   5. 自动保存任务启动时隐藏保存按钮                                             │
 * │   6. XY模式下禁用Zoom和全测量功能                                               │
 * │   7. 滚屏模式或慢时基时禁用Zoom功能                                             │
 * │   8. FFT模式时显示FFT时基档位                                                   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/5/10.
 */

public class MainHolderBottomQuick extends RecyclerView.ViewHolder {
    private static final String TAG = "MainHolderBottomQuick";

    private static final int SCREEN_CAPTURE_REQUESTCODE = 31;
    private static final int SCREEN_RECORDER_REQUESTCODE = 32;
    private static final int SCREEN_CAPTURE_REQUESTCODE_X = 33;
    private Context context;
    private Button bottScreen;
    private Button btnHome;
    private CheckBox bottZoom;
    private CheckBox bottVideo;
    private CheckBox bottAllMeasure;
    private CheckBox bottSerialBusTxt;
    private CheckBox bottCenterMenu;
    private Button btnCenterSegment;

    private Button leftScreen,mainTopScreen;
    private CheckBox leftZoom,mainTopZoom;
    private Button leftMenuHalf;
    private Button btnZero;

    private MainLayoutCenterMenu layoutCenterMenu;
    private MainLayoutCenterSerialsWord layoutSerialsWord;
    private MainLayoutCenterSegmented layoutCenterSegmented;
    private RelativeLayout recorderTimeLayout;
    private TextView tvPoint, tvMinute, tvColon, tvSecond;
    MainActivity mainActivity;
    private ViewGroup mainHolderBottomQuick;

    private MainBottomMsgQuick msgQuick;
    private boolean isAutoSaveStarting = false;

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private String screenShotAddMsg = "";

    public MainHolderBottomQuick(View itemView, Bundle savedInstanceState) {
        super(itemView);
        this.context = itemView.getContext();
        initView(itemView, savedInstanceState);
        initControl();
    }

    private void initView(View itemView, Bundle savedInstanceState) {
        mainHolderBottomQuick = (ConstraintLayout) itemView.findViewById(R.id.mBottomBar_Quick);
        bottScreen = (Button) itemView.findViewById(R.id.mButton_screen);
        bottZoom = (CheckBox) itemView.findViewById(R.id.mButton_zoom);
        bottVideo = (CheckBox) itemView.findViewById(R.id.mButton_video);
        bottAllMeasure = (CheckBox) itemView.findViewById(R.id.mButton_fullMeasure);
        bottSerialBusTxt = (CheckBox) itemView.findViewById(R.id.mButton_serialbus);
        bottCenterMenu = (CheckBox) itemView.findViewById(R.id.mButton_centerMenu);
        btnCenterSegment = (Button) itemView.findViewById(R.id.mButton_segment);
        btnHome=(Button)itemView.findViewById(R.id.mButton_home);
        btnZero=itemView.findViewById(R.id.mButton_zero);

        mainTopScreen=itemView.findViewById(R.id.mainTopScreen);
        mainTopZoom=itemView.findViewById(R.id.mainTopZoom);

        leftScreen = (Button) itemView.findViewById(R.id.btnLeftScreen);
        leftZoom = (CheckBox) itemView.findViewById(R.id.btnLeftZoom);
        leftMenuHalf = (Button) itemView.findViewById(R.id.btnLeftMenuHalf);

        layoutCenterMenu = (MainLayoutCenterMenu) itemView.findViewById(R.id.mainLayoutCenterMenu);
        layoutCenterSegmented = (MainLayoutCenterSegmented) itemView.findViewById(R.id.mainLayoutCenterSegmented);
        layoutSerialsWord = (MainLayoutCenterSerialsWord) itemView.findViewById(R.id.layoutSerialsWord);
        layoutSerialsWord.setSavedInstanceState(savedInstanceState);
        recorderTimeLayout = (RelativeLayout) itemView.findViewById(R.id.recorderTime);
        tvPoint = (TextView) itemView.findViewById(R.id.point);
        tvMinute = (TextView) itemView.findViewById(R.id.minute);
        tvColon = (TextView) itemView.findViewById(R.id.colon);
        tvSecond = (TextView) itemView.findViewById(R.id.second);

        bottScreen.setOnClickListener(onClickListener);
        bottZoom.setOnClickListener(onClickListener);
        bottVideo.setOnClickListener(onClickListener);
        bottAllMeasure.setOnClickListener(onClickListener);
        bottSerialBusTxt.setOnClickListener(onClickListener);
        bottCenterMenu.setOnClickListener(onClickListener);
        btnCenterSegment.setOnClickListener(onClickListener);
        btnHome.setOnClickListener(onClickListener);
        btnZero.setOnClickListener(onClickListener);

        mainTopScreen.setOnClickListener(onClickListener);
        mainTopZoom.setOnClickListener(onClickListener);
        leftScreen.setOnClickListener(onClickListener);
        leftZoom.setOnClickListener(onClickListener);
        leftMenuHalf.setOnClickListener(onClickListener);


        bottVideo.setVisibility(View.GONE);

        msgQuick = new MainBottomMsgQuick();
        msgQuick.setEnable(new boolean[]{true, true, true, true, true});
    }


    private void initControl() {
        //RxBus.getInstance().getObservable(RxEnum.ACTIVITY_ACTIVITYRESULT).subscribe(consumerActivityResult);
        //RxBus.getInstance().getObservable(RxEnum.ACTIVITY_ACTIVITYONSTOP).subscribe(consumerActivityOnStop);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_DISPLAY_YTZOOM).subscribe(consumerYTZoom);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MCUTOARM).subscribe(consumerMcuToArm);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.TOP_USER_SELFADJUST).subscribe(consumerUserSelfAdjust);
        RxBus.getInstance().getObservable(RxEnum.ROLL_RUN_ZOOM).subscribe(consumerRollRunZoom);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenuAuto);
        RxBus.getInstance().getObservable(RxEnum.SCREENSHOT_MSG).subscribe(consumerScreenShotMsg);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegment);
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED_BTNSEGMENT_ENABLE).subscribe(consumerCenterSegmentedDialogShowing);
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlipToOther);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_SERIALSDETAIL).subscribe(consumerMainRightSerialsDetail);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.MainLeft_To_Menu_Command).subscribe(consumerToMenuLeftCommand);
//        RxBus.getInstance().getObservable(RxEnum.DIALOG_CLOSE).subscribe(consumerDialogClose);
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState);

        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_ZOOM, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_FPGA_STATUS,eventUIObserver);
    }



    private void setCache() {
        boolean b = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ALLMEASURE);
//        boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
        boolean isSerialBusTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        boolean isMenu = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_MENU);
        int menuX = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_MENU_X);
        int menuY = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_MENU_Y);

        bottAllMeasure.setChecked(b);
        clickAllMeasure();

        //zoom的初始化改在了mainActivity中所有cache结束之后，splash结束之前
//        zoom.setChecked(isZoom);
//        onClickChange(zoom,false);

        bottSerialBusTxt.setChecked(isSerialBusTxt);
        Command.get().getTrigger().SerialBus_Type(bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
        //Command.get().getBus().Mode(0,bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
        Command.get().getBus().Mode(1,bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
        clickSerialBusTxt(false);
//            if (isZoom) WorkModeManage.getInstance().setmWorkMode(IWorkMode.WorkMode_YTZOOM);
        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderBottomQuick, true);
        //
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1) {
            bottZoom.setEnabled(false);
            mainTopZoom.setEnabled(false);
            bottAllMeasure.setEnabled(false);
            bottSerialBusTxt.setEnabled(false);
            btnZero.setEnabled(false);
        }
        String textAllMeasure = (String) context.getResources().getText(R.string.menu_bottom_qiuck_all_measure);
        String textScreen = (String) context.getResources().getText(R.string.menu_bottom_qiuck_screen);
        String textSerialsTxt = (String) context.getResources().getText(R.string.menu_bottom_qiuck_serials_txt);
        String textZoom = (String) context.getResources().getText(R.string.menu_bottom_qiuck_zoom);

//        bottAllMeasure.setTextSize(getTextSize(textAllMeasure, bottAllMeasure.getMeasuredWidth()));
//        bottScreen.setTextSize(getTextSize(textScreen, bottScreen.getMeasuredWidth()));
//        bottSerialBusTxt.setTextSize(getTextSize(textSerialsTxt, bottSerialBusTxt.getMeasuredWidth()));
//        bottZoom.setTextSize(getTextSize(textZoom, bottZoom.getMeasuredWidth()));

        bottCenterMenu.setChecked(isMenu);
        clickCenterMenu();
        layoutCenterMenu.setLocation(menuX, menuY);
        setSerialBusTxtEnable();
        sendMsg();


        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        if(!runStop){
            int state = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE);
            btnCenterSegment.setEnabled(state==0);
        }else{
            btnCenterSegment.setEnabled(false);
        }
    }

    private void sendMsg() {
//        msgQuick.setEnable(0, bottScreen.isEnabled());
//        msgQuick.setEnable(1, bottZoom.isEnabled());
        msgQuick.setEnable(0, bottAllMeasure.isEnabled());
        msgQuick.setEnable(1, bottSerialBusTxt.isEnabled());
        msgQuick.setEnable(2,btnCenterSegment.isEnabled());
        msgQuick.setEnable(3,btnZero.isEnabled());
        msgQuick.setEnable(4,btnHome.isEnabled());
        RxBus.getInstance().post(RxEnum.MAINBOTTOM_QUICKENABLE, msgQuick);
    }

    public int getTextSize(String text, int textWidth) {
        Paint paint = new Paint();
        int textSize;
        for (textSize = 12; textSize >= 2; textSize--) {
            paint.setTextSize(textSize);
            Rect textRect = Tools.getTextRect(text, paint);
            if (textRect.width() < textWidth) {
                break;
            }
        }
        return textSize;
    }

    private void screenShot() {
        View view = mainActivity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bp = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth(),
                view.getMeasuredHeight());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();


    }

    /**
     * 截屏
     *
     * @param screenShotAddMsg 截屏成功时，除了截屏的信息外还需要显示的信息
     */
    private void screenShot(final String screenShotAddMsg) {

        String str = "";
        if(screenShotAddMsg.isEmpty()) {
            this.bottScreen.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.screenShot();
                }
            }, 200);
        }

        this.bottScreen.postDelayed(new Runnable() {
            @Override
            public void run() {

//                    String screenShotSuccess = App.get().getResources().getString(com.micsig.tbook.ui.R.string.screenShotSuccess);
//
//                    if (!StrUtil.isEmpty(screenShotAddMsg)) {
//                        screenShotSuccess = "\n" + screenShotSuccess.substring(0, screenShotSuccess.length() - 1) + "!";
//                        DToast.get().show(screenShotAddMsg + screenShotSuccess);
//                    } else {
//                        screenShotSuccess += "\n" + str;
//                    }


                if (!StrUtil.isEmpty(screenShotAddMsg)) {
                    DToast.get().show(screenShotAddMsg);
                }
                Logger.d(TAG, screenShotAddMsg);
            }
        }, 800);
    }

    final Object mScreenshotLock = new Object();
    ServiceConnection mScreenshotConnection = null;

    private void takeScreenshot(final Runnable runnable) {
        synchronized (mScreenshotLock) {
            if (mScreenshotConnection != null) {
                return;
            }
            //初始化要绑定的服务，从这里可以看出要绑定的服务是SystemUI里的TakeScreenshotService
            ComponentName cn = new ComponentName("com.android.systemui",
                    "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 2);
                        msg.arg1 = 0;
                        msg.arg2 = 0;
                        if (CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP)) {
                            msg.arg1 = 1;
                        }
                        if (CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT)) {
                            msg.arg2 = 1;
                        }
                        final ServiceConnection myConn = this;
                        Handler h = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        context.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.d(TAG, "onServiceDisconnected");
                }
            };
            //绑定Service
            if (context.bindService(
                    intent, conn, Context.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                //设置超时机制，若超时就解除绑定
                //postDelayed(mScreenshotTimeout, 10000);
            } else {
                Logger.d(TAG, "bindService error");
            }
        }
    }


    private String getCurTime(String pattern) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    private void clickAllMeasure() {
        if (bottAllMeasure.isChecked()) {
            int ch = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
            MeasureManage.getInstance().getAllMeasure().setChannelId(ch + 1);
            MeasureManage.getInstance().getAllMeasure().setVisible(true);
            MeasureService.forceMeasureRefresh();
        } else {
            MeasureManage.getInstance().getAllMeasure().setVisible(false);
        }
        RxBus.getInstance().post(RxEnum.BOTTOMLAYOUT_ALLMEASURE, bottAllMeasure.isChecked());
    }

    private void clickSerialBusTxt(boolean isFromEventBus) {
        boolean show;
        SerialBusManage.getInstance().clearSerialBusTxtBuffer();
        if(!isFromEventBus){
            Scope.getInstance().setRun(true);
        }

        if (Command.get().getTrigger().SerialBus_TypeQ() == Command_Trigger.SerialBusType_TXT) {
            if (!isFromEventBus) {
                Scope.getInstance().setType(Scope.TYPE_TXT);
            }
            show = true;
        } else if (Command.get().getTrigger().SerialBus_TypeQ() == Command_Trigger.SerialBusType_IMG) {
//            SerialBusManage.getInstance().getSerialTxtBuffer(ISerialsWord.TYPE_S1).clearAll();
//            SerialBusManage.getInstance().getSerialTxtBuffer(ISerialsWord.TYPE_S2).clearAll();
//            SerialBusManage.getInstance().clearSerialBusTxtBuffer();
            if (!isFromEventBus) {
                Scope.getInstance().setType(Scope.TYPE_NORMAL);
            }
            show = false;
        } else {
            if (!isFromEventBus) {
                Scope.getInstance().setType(Scope.TYPE_NONE);
            }
            show = false;
        }
        layoutSerialsWord.setVisibility(show ? View.VISIBLE : View.GONE);
        bottZoom.setEnabled(!show);
        mainTopZoom.setEnabled(!show);
        bottAllMeasure.setEnabled(!show);
        btnZero.setEnabled(!show && !isAutoSaveStarting);
        RxBus.getInstance().post(RxEnum.CENTER_SERIALSWORD_VISIBLE, show);
        sendMsg();
    }

    private void clickCenterMenu() {
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_MENU, String.valueOf(bottCenterMenu.isChecked()));
        if (bottCenterMenu.isChecked()) {
            layoutCenterMenu.setVisibility(View.VISIBLE);
        } else {
            layoutCenterMenu.setVisibility(View.GONE);
        }
    }
    private void clickCenterSegment() {
        Logger.i(TAG,"ClickCenterSegment!");
        if (layoutCenterSegmented.isBtnSegmentEnable()) {
            if (layoutCenterSegmented.getVisibility() == View.VISIBLE) {
                layoutCenterSegmented.setVisibility(View.GONE);
            } else {
                layoutCenterSegmented.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setSerialBusTxtEnable(boolean bFPGA){
        boolean b= CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        if (b && bFPGA) {
            bottSerialBusTxt.setEnabled(true);
            sendMsg();
            return;
        }
        boolean isYT = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 0;
        boolean isNoSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 1;
        boolean isOpenS1= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean isOpenS2= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean isOpenS3= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean isOpenS4= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);


        boolean bb = (bFPGA && isYT && isNoSegment && ((isOpenS1 || isOpenS2 || isOpenS3 || isOpenS4)) && !isAutoSaveStarting);
        bottSerialBusTxt.setEnabled(bb);
        sendMsg();

    }
    private void setSerialBusTxtEnable() {
        boolean ScopeEnable= ScopeConfig.getConfig().isBusEnable();
        boolean isYT = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 0;
        boolean isNoSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 1;
        boolean isOpenS1= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean isOpenS2= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean isOpenS3= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean isOpenS4= CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);

        boolean b= CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);

        Logger.i(Command.TAG,"scopeEnable:"+ScopeEnable+",b:"+b+ ",yt,isNosegment,s1,s2:"+isYT+","+isNoSegment+","+isOpenS1+","+isOpenS2);
        if (ScopeEnable) {
            bottSerialBusTxt.setEnabled(isYT && isNoSegment && (isOpenS1 || isOpenS2 || isOpenS3 || isOpenS4) && !isAutoSaveStarting);
            if (b) {
                bottSerialBusTxt.setEnabled(true);
            }
        }else{
            bottSerialBusTxt.setEnabled(false);
        }
        sendMsg();
    }

    private Consumer<YTZoomMsgDisplay> consumerYTZoom = new Consumer<YTZoomMsgDisplay>() {
        @Override
        public void accept(YTZoomMsgDisplay ytZoomMsgDisplay) throws Exception {
            if (bottZoom.isChecked() != ytZoomMsgDisplay.isDisplay()) {
                bottZoom.setChecked(ytZoomMsgDisplay.isDisplay());
                Scope.getInstance().setZoomFlags(
                        ytZoomMsgDisplay.isReloadLargeTimeScale() ?
                                DisplayAction.FLAGS_RELOAD_LARGE_TIME_SCALE : DisplayAction.FLAGS_NONE_LARGE_TIME_SCALE);
                onClickChange(bottZoom, ytZoomMsgDisplay.isPlaySound(), false);
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    /**
     * 设置全部测量颜色
     * @param obj
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);
        if (mqEnum!=MQEnum.CH_ACTIVE)return;
        IChan chan=((MsgChActiveChange)obj).getChan();
        setAdjustZeroEnable();

        if (chan!=IChan.CH_NULL) {
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(chan.getValue()));
        } else {
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_WILL_NULL, String.valueOf(CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT)));
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(chan.getValue()));
        }
        if (MeasureManage.getInstance().getAllMeasure().isVisible()) {
            MeasureManage.getInstance().getAllMeasure().setChannelId(chan.getValue()+1);
            RxBus.getInstance().post(RxEnum.BOTTOMLAYOUT_ALLMEASURE, bottAllMeasure.isChecked());
        }
        //TODO 切换为xy模式的时候，由于math、ref通道需要关闭，则需改变当前通道，但此时状态为xy模式，则该设置没生效
        WaveManage.get().setSelectCursor(chan.getValue()+1);

        Command.get().getCursor().Source(chan.getValue(), false);
        Command.get().getChannel().setCurrActiveChannel(chan.getValue(), false);
    }



    private Consumer<Integer> consumerMcuToArm = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (integer == ExternalKeysCommand.MCUTOARM_SCREENCAPTURE) {
                onClickListener.onClick(bottScreen);
            } else if (integer == ExternalKeysCommand.MCUTOARM_ZOOM) {
                bottZoom.setChecked(!bottZoom.isChecked());
                onClickChange(bottZoom, true, false);
            }
        }
    };

    private void setAdjustZeroEnable(){
        btnZero.setEnabled(false);
        btnHome.setEnabled(!isAutoSaveStarting);
        if (ChannelFactory.getChActivate()>=ChannelFactory.CH1
                && ChannelFactory.getChActivate()<ChannelFactory.MATH1
                && !isAutoSaveStarting){
            btnZero.setEnabled(true);
        }
    }

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            switch (workModeBean.getNextWorkMode()) {
                case IWorkMode.WorkMode_YT:
                    if (bottZoom.isChecked()) {
                        bottZoom.setChecked(false);
                        onClickChange(bottZoom, true, workModeBean.isFromEventBus());
                    }
                    bottZoom.setEnabled(true);
                    mainTopZoom.setEnabled(true);
                    bottAllMeasure.setEnabled(true);
//                    btnZero.setEnabled(true);
                    setAdjustZeroEnable();
                    bottSerialBusTxt.setEnabled(ScopeConfig.getConfig().isBusEnable());
                    clickAllMeasure();
                    break;
                case IWorkMode.WorkMode_YTZOOM:
                    bottZoom.setEnabled(true);
                    mainTopZoom.setEnabled(true);
                    bottAllMeasure.setEnabled(true);
//                    btnZero.setEnabled(true);
                    setAdjustZeroEnable();
                    bottSerialBusTxt.setEnabled(ScopeConfig.getConfig().isBusEnable());
                    clickAllMeasure();
                    break;
                case IWorkMode.WorkMode_XY:
                    if (bottZoom.isChecked()) {
                        bottZoom.setChecked(false);
                        onClickChange(bottZoom, true, workModeBean.isFromEventBus());
                    }
                    bottZoom.setEnabled(false);
                    mainTopZoom.setEnabled(false);
                    bottAllMeasure.setEnabled(false);
                    bottSerialBusTxt.setEnabled(false);
                    btnZero.setEnabled(false);
                    MeasureManage.getInstance().getAllMeasure().setVisible(false);
                    break;
            }
            sendMsg();
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_DISPLAY_ZOOM:
                    bottZoom.setChecked(Boolean.parseBoolean(commandMsgToUI.getParam()));
                    onClickChange(bottZoom, true, false);
                    break;
                case CommandMsgToUI.FLAG_STOTAGE_CAPTURE:
                    //screen.performClick();
                    //onClickListener.onClick(screen);
                    screenShot("");
                    break;
                case CommandMsgToUI.FLAG_STOTAGE_RECORD:
                    bottVideo.setChecked(Integer.parseInt(commandMsgToUI.getParam()) == 0);
                    onClickListener.onClick(bottVideo);
                    break;
                case CommandMsgToUI.FLAG_Bus_Mode:
                case CommandMsgToUI.FLAG_TRIGGER_SERIALBUS_TYPE:
                    boolean b=Integer.parseInt(commandMsgToUI.getParam())==Command_Trigger.SerialBusType_TXT;
                    bottSerialBusTxt.setChecked(b);
                    onClickListener.onClick(bottSerialBusTxt);
                    break;
                case CommandMsgToUI.FLAG_MEASURE_ALL_DISPLAY:
                    bottAllMeasure.setChecked(Boolean.parseBoolean(commandMsgToUI.getParam()));
                    onClickListener.onClick(bottAllMeasure);
                    break;

            }
        }
    };



    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            if (bottZoom.isChecked() && !mainTopMsgRightGone.isVisible() && Scope.getInstance().isRun()) {
                //运行状态调节到慢时基和滚屏档，则退出zoom
                bottZoom.setChecked(false);
                onClickChange(bottZoom, true, false);
            }
            if (!mainTopMsgRightGone.isVisible() && Scope.getInstance().isRun()) {
                bottZoom.setEnabled(false);
                mainTopZoom.setEnabled(false);
            } else {
                if (bottSerialBusTxt.isChecked() || WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_XY){
                    bottZoom.setEnabled(false);
                    mainTopZoom.setEnabled(false);
                    btnZero.setEnabled(false);
                }else {
                    bottZoom.setEnabled(true);
                    mainTopZoom.setEnabled(true);
//                    btnZero.setEnabled(true);
                }
                sendMsg();
            }
        }
    };

    private Consumer<Integer> consumerUserSelfAdjust = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (bottAllMeasure.isChecked()) {
                bottAllMeasure.setChecked(false);
                onClickListener.onClick(bottAllMeasure);
            }
        }
    };

    private Consumer<Boolean> consumerRollRunZoom = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (!aBoolean/* && bottZoom.isChecked()*/) {
                bottZoom.setChecked(false);
                onClickChange(bottZoom, true, false);
            }
        }
    };

    private Consumer<Boolean> consumerMainLeftMenuAuto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean && bottZoom.isChecked()) {
                bottZoom.setChecked(false);
                onClickChange(bottZoom, true, true);
            }
        }
    };

    private Consumer<String> consumerScreenShotMsg = new Consumer<String>() {
        @Override
        public void accept(String s) throws Exception {
            screenShot(s);
        }
    };

//    private Consumer<Integer> consumerDialogClose = new Consumer<Integer>() {
//        @Override
//        public void accept(Integer integer) throws Exception {
//            if (integer == MainViewGroup.DIALOG_CENTERTIMEBASE) {
//                centerMenu.setChecked(false);
//                clickCenterMenu();
//            }
//        }
//    };

    private Consumer<TopMsgSampleSegmented> consumerSampleSegment = new Consumer<TopMsgSampleSegmented>() {
        @Override
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception {
            setSerialBusTxtEnable();
        }
    };

    private Consumer<Boolean> consumerCenterSegmentedDialogShowing = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            Logger.i(TAG,"segment enable:"+aBoolean);
            itemView.post(() ->{
                if (aBoolean) {
                    btnCenterSegment.setEnabled(true);
                } else {
                    btnCenterSegment.setEnabled(false);
                }
                sendMsg();
            });
        }
    };
    private Consumer<MainMsgSlip> consumerMainSlipToOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
//            if (mainMsgSlip.isOpen() && btnCenterSegment.isEnabled()) {
//                btnCenterSegment.setChecked(true);
//            }else {
//                btnCenterSegment.setChecked(false);
//            }
        }
    };
    private Consumer<MainRightMsgSerialsDetail> consumerMainRightSerialsDetail = new Consumer<MainRightMsgSerialsDetail>() {
        @Override
        public void accept(@NonNull MainRightMsgSerialsDetail msgSerialsDetail) throws Exception {
            setSerialBusTxtEnable();
        }
    };
    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            setSerialBusTxtEnable();
        }
    };
    private Consumer<MainMsgCenterMenuCommand> consumerToMenuLeftCommand=new Consumer<MainMsgCenterMenuCommand>() {
        @Override
        public void accept(MainMsgCenterMenuCommand mainMsgCenterMenuCommand) throws Exception {
            switch (mainMsgCenterMenuCommand.getCommand()){
                case MainMsgCenterMenuCommand.CommandSerialText:{
                    bottSerialBusTxt.setChecked(false);
                    onClickListener.onClick(bottSerialBusTxt);
                }break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == bottScreen.getId() || v.getId() == leftScreen.getId() || mainTopScreen.getId()==v.getId()) {
                ((MainViewGroup) MainHolderBottomQuick.this.itemView).hideSlip(MainViewGroup.BOTTOMSLIP);
            }

            Tools.PrintControlsLocation("MainHolderQuick",mainHolderBottomQuick);
            onClickChange(v, true, false);
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_DISPLAY_ZOOM) {
                if (bottZoom.isChecked() != Scope.getInstance().isZoom()) {
                    bottZoom.setChecked(Scope.getInstance().isZoom());
                    onClickChange(bottZoom, true, true);
                }
            }else if(eventBase.getId() == EventFactory.EVENT_FPGA_STATUS) {
                boolean b = (boolean) eventBase.getData();
//                bottSerialBusTxt.setEnabled(b);
                setSerialBusTxtEnable(b);
                App.RefreshFwVersion();
            }
        }
    };

    private void onClickChange(View v, boolean isPlaySound, boolean isFromEventBus) {
        MainViewGroup mainViewGroup = (MainViewGroup) MainHolderBottomQuick.this.itemView;
        int id = v.getId();
        if (id == leftZoom.getId()) {
            bottZoom.setChecked(leftZoom.isChecked());
            mainTopZoom.setChecked(leftZoom.isChecked());
            id = bottZoom.getId();
        }
        if (id==mainTopZoom.getId()){
            bottZoom.setChecked(mainTopZoom.isChecked());
            leftZoom.setChecked(mainTopZoom.isChecked());
            id = bottZoom.getId();
        }
        if (id == bottScreen.getId() || id == leftScreen.getId() || id==mainTopScreen.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            screenShot("");
        }else if (id==btnHome.getId()){
            MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandReturnHome);
            RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command);
        }
        else if (id == bottZoom.getId() ) {
            CursorManage.getInstance().setCursorTrace(true);
            //滚屏、运行、慢时基状态不能进入zoom模式
            boolean checked = bottZoom.isChecked();
            leftZoom.setChecked(checked);
            mainTopZoom.setChecked(checked);
            if (bottZoom.isChecked() && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL) == 0
                    && Scope.getInstance().isRun()
                    && Scope.getInstance().isInScrollMode()/*Tools.isSlowTimeBase()*/) {
                bottZoom.setChecked(false);
                leftZoom.setChecked(false);
                mainTopZoom.setChecked(false);
                RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, new YTZoomMsgDisplay(bottZoom.isChecked()));
                Command.get().getDisplay().Zoom(bottZoom.isChecked(), false);
                return;
            } else {
                if (isPlaySound) {
                    PlaySound.getInstance().playButton();
                }
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM, String.valueOf(bottZoom.isChecked()));
            if (bottZoom.isChecked() && Command.get().getDisplay().getRoutineTimeBaseModeQ() != Command_Display.RoutineTimeBaseMode_YT) {
                Command.get().getDisplay().setRoutineTimeBaseMode(Command_Display.RoutineTimeBaseMode_YT, true);
            }
            Command.get().getDisplay().Zoom(bottZoom.isChecked(), false);
//                ((MainViewGroup) MainHolderBottomQuick.this.itemView).hideAllDialogSlip();
            if (!isFromEventBus) {
                Scope.getInstance().setZoom(bottZoom.isChecked());
            }
            RxBus.getInstance().post(RxEnum.WAVEZONE_DISPLAY_YTZOOM, new YTZoomMsgDisplay(bottZoom.isChecked()));

            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU,
                    new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_ZOOM,
                            bottZoom.isChecked() ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF));

            if (checked != (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM)) {
                WorkModeManage.getInstance().setWorkMode(checked ? IWorkMode.WorkMode_YTZOOM : IWorkMode.WorkMode_YT, isFromEventBus);
            }
            mainViewGroup.hideAllDialogSlip();
            CursorManage.setCursorByTimebaseTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }else if (id==btnZero.getId()){
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandCalibrationZero));
        }
        else if (id == bottVideo.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            Command.get().getStorage().Record(bottVideo.isChecked() ? 0 : 1, false);

            mainViewGroup.hideAllDialogSlip();
        } else if (id == bottAllMeasure.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            Command.get().getMeasure().Adislay(bottAllMeasure.isChecked(),false);
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ALLMEASURE, String.valueOf(bottAllMeasure.isChecked()));
            clickAllMeasure();
            mainViewGroup.hideAllDialogSlip();
        } else if (id == bottSerialBusTxt.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT, String.valueOf(bottSerialBusTxt.isChecked()));
            Command.get().getTrigger().SerialBus_Type(bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
            //Command.get().getBus().Mode(0,bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
            Command.get().getBus().Mode(1,bottSerialBusTxt.isChecked() ? Command_Trigger.SerialBusType_TXT : Command_Trigger.SerialBusType_IMG, false);
            clickSerialBusTxt(isFromEventBus);
            mainViewGroup.hideAllDialogSlip();
            if (bottSerialBusTxt.isChecked()==false){
                setSerialBusTxtEnable();
                ((MainViewGroup)itemView).getMainMenu().restoreTriggerIdx();
            }else {
                ((MainViewGroup)itemView).getMainMenu().setTriggerSerialBus1();
            }
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(isFromEventBus));
        } else if (id == bottCenterMenu.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            clickCenterMenu();
            mainViewGroup.hideAllDialogSlip();
        } else if (id == leftMenuHalf.getId()) {
            MainDialogMenuHalf dialog = (MainDialogMenuHalf) mainViewGroup.getDialog(MainViewGroup.DIALOG_MENUHALF);
            dialog.show();
        }
        else if (id == btnCenterSegment.getId()) {
            if (isPlaySound) {
                PlaySound.getInstance().playButton();
            }
            clickCenterSegment();
            mainViewGroup.hideAllDialogSlip();
        }
    }

    private void sendMsg(MainMsgCenterMenuCommand command){
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command);
    }

    private static final int MSG_RECORDER_SHOW = 41;
    private static final int MSG_RECORDER_HIDE = 42;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RECORDER_SHOW:
                    int minute = msg.arg1 / 1000 / 60;
                    int second = msg.arg1 / 1000 % 60;
                    recorderTimeLayout.setVisibility(View.VISIBLE);
                    tvPoint.setVisibility(second % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                    tvColon.setVisibility(second % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                    tvMinute.setText(getTwoBitNum(minute));
                    tvSecond.setText(getTwoBitNum(second));
                    break;
                case MSG_RECORDER_HIDE:
                    recorderTimeLayout.setVisibility(View.GONE);
                    break;
            }
        }

        private String getTwoBitNum(int number) {
            String s = String.valueOf(number);
            if (s.length() < 2) {
                s = "0" + s;
            }
            return s;
        }
    };

    private Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean isAutoSaveStop) throws Throwable {
            itemView.post(() -> { //false 时候正在保存
                isAutoSaveStarting = !isAutoSaveStop;
                setAdjustZeroEnable();
                mainTopScreen.setEnabled(isAutoSaveStop);
            });
        }
    };

}
