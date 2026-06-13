package com.micsig.tbook.tbookscope.main.maintop;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.base.OEM;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2022-1-11 15:25
 */
public class MainTopLayoutRightButton extends ConstraintLayout {
    private static final String TAG=MainTopLayoutRightButton.class.getSimpleName();

    private Context context;
    private Button checkRunStop, checkAuto, checkSeq;
    private ImageView ivLogo;
    private TextView txRunStop;

    private int colorGreen= getResources().getColor(R.color.color_maintopleft_state_run);
    private int colorBlue= getResources().getColor(R.color.main_text_color);

    public MainTopLayoutRightButton(Context context) {
        this(context, null);
    }

    public MainTopLayoutRightButton(Context context, AttributeSet attrs) {
        this(context,attrs, 0);
    }

    public MainTopLayoutRightButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
        initControl();
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View view= View.inflate(context, R.layout.layout_maintop_right_button, this);
        ivLogo = (ImageView) view.findViewById(R.id.logo);
        checkRunStop=view.findViewById(R.id.RunStop);
        checkAuto =view.findViewById(R.id.Auto);
        checkSeq =view.findViewById(R.id.SEQ);
        txRunStop = view.findViewById(R.id.tx_runstop);


        checkRunStop.setOnClickListener(Onclick);
        checkAuto.setOnClickListener(Onclick);
        checkSeq.setOnClickListener(Onclick);

        Bitmap bitmap = OEM.getLogo();
        if (bitmap != null) {
            ivLogo.setImageBitmap(bitmap);
        }

        ivLogo.setOnClickListener(this::onImageLogoClickEvent);
    }

    private OnClickListener Onclick=new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.RunStop:
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandRunStop));break;
                case R.id.Auto:
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandAuto));break;
                case R.id.SEQ:
                    checkSeq.setTextColor(colorGreen);
                    sendMsg(new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandSEQ));break;
            }
        }
    };

    private void onImageLogoClickEvent(View view) {
        MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandReturnHome);
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command);
    }

    private void sendMsg(MainMsgCenterMenuCommand command){
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command);
    }


    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_AUTO_CHANGED).subscribe(consumerAutoChanged);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_START, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_SINGLE,eventUIObserver);
    }

    /**
     * 是否点击在三个按钮上
     * @param event
     *
     */
    public boolean containsRect(MotionEvent event){
//        if (MotionEvent.ACTION_DOWN==event.getAction()){
//            int x=(int)event.getRawX();
//            int y=(int)event.getRawY();
//            Rect r1=new Rect();
//            Rect r2=new Rect();
//            Rect r3=new Rect();
//            Rect r4=new Rect();
//            Rect r5=new Rect();
//            checkSeq.getGlobalVisibleRect(r1);
//            checkAuto.getGlobalVisibleRect(r2);
//            checkRunStop.getGlobalVisibleRect(r3);
//            checkZoom.getGlobalVisibleRect(r4);
//            if (r1.contains(x,y) ||r2.contains(x,y) || r3.contains(x,y) || r4.contains(x,y)|| r5.contains(x,y) ){
//                return true;
//            }else {
//                return false;
//            }
//        }else {
//            return false;
//        }
        return false;
    }

    private void setCache(){
        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        boolean seq = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_SEQ);
        boolean auto = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_AUTO);

        seq = false;
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_SEQ, "false");
        auto = false;
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_AUTO, "false");

        if (seq) {
            this.checkSeq.setTextColor(colorGreen);
        } else {
            this.checkSeq.setTextColor(colorBlue);
        }
//        if (auto) {
//            this.checkAuto.setTextColor(colorGreen);
//        } else {
//            this.checkAuto.setTextColor(colorBlue);
//        }
        this.checkAuto.setTextColor(colorBlue);

        boolean autoRange= (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE))==0?true:false;
        if (autoRange){
            checkAuto.setText(getResources().getString(R.string.mainCenterMenuAutoRange));
        }else {
            checkAuto.setText(getResources().getString(R.string.mainCenterMenuAuto));
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache o) throws Exception {
            setCache();
        }
    };
    private Consumer consumerLoadCacheEx=new Consumer() {
        @Override
        public void accept(Object o) throws Exception {

        }
    };

    private void setBtnVisible(){
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
        boolean isXy=WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_XY;

        if (isXy || isSerialsTxt){
            checkAuto.setVisibility(View.GONE);
            checkSeq.setVisibility(View.GONE);
        }else {
            checkAuto.setVisibility(View.VISIBLE);
            checkSeq.setVisibility(View.VISIBLE);
        }

        if (!isSerialsTxt){
            checkSeq.setVisibility(VISIBLE);
        }

    }

    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            setBtnVisible();
        }
    };
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            setBtnVisible();
        }
    };
    private Consumer<CommandMsgToUI> consumerCommandToUI=new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()){
                case CommandMsgToUI.FLAG_MENU_SINGLE:{
                    checkSeq.setTextColor(colorGreen);
                }break;
                case CommandMsgToUI.FLAG_MENU_STOP:
                case CommandMsgToUI.FLAG_MENU_RUN: {
                    checkSeq.setTextColor(colorBlue);
                    break;
                }
            }
        }
    };

    private Consumer<Boolean> consumerAutoChanged=new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean){
                checkAuto.setText(getResources().getString(R.string.mainCenterMenuAutoRange));
            }else {
                checkAuto.setText(getResources().getString(R.string.mainCenterMenuAuto));
            }
        }
    };

    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {
            Logger.i(TAG,"state:"+mainLeftMsgMenuRunStop.getRunState());
            if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN) {
                Logger.i(TAG,"scope state Run:"+ Scope.getInstance().isAuto());
//                if (Scope.getInstance().isAuto()){
//                    return;
//                }
                checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateRun));
                checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));

                txRunStop.setText(getResources().getString(R.string.mainTopLeftStateRun));
                txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_run));

            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.WAIT) {
//                if (Scope.getInstance().isAuto()) {
//                    return;
//                }

                Scope scope = Scope.getInstance();
                if(!scope.isTouchFine()){
                    if (scope.isRun()) {
                        checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateWait));
                        checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait));

                        txRunStop.setText(getResources().getString(R.string.mainTopLeftStateWait));
                        txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_wait));
                        checkSeq.setTextColor(scope.isSingle()?colorGreen:colorBlue);

                    } else {
                        checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop));
                        checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));

                        txRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop));
                        txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
                        checkSeq.setTextColor(colorBlue);
                    }
                }
            } else if (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.STOP) {
                checkRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop));
                checkRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));

                txRunStop.setText(getResources().getString(R.string.mainTopLeftStateStop));
                txRunStop.setTextColor(getResources().getColor(R.color.color_maintopleft_state_stop));
                checkSeq.setTextColor(colorBlue);
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            Scope scope = Scope.getInstance();
            if (eventBase.getId() == EventFactory.EVENT_AUTO_START) {
                    checkAuto.setTextColor(colorGreen);
                    checkSeq.setTextColor(colorBlue);
            } else if (eventBase.getId() == EventFactory.EVENT_AUTO_STOP) {
                    Logger.i(TAG,"AUTO STOP!");
                    checkAuto.setTextColor(colorBlue);
                    checkSeq.setTextColor(colorBlue);
            } else if (eventBase.getId() == EventFactory.EVENT_SCOPE_STATE) {

            } else if (eventBase.getId() == EventFactory.EVENT_SCOPE_SINGLE){
                checkSeq.setTextColor(scope.isSingle()? colorGreen:colorBlue);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (containsRect(event)){
//            return false;
//        }
        return true;
    }
}
