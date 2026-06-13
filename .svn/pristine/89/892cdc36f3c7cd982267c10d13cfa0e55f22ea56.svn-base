package com.micsig.tbook.tbookscope.main.maincenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.MButton_CheckBox;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.main.MainLayoutSegmentedFit;
import com.micsig.tbook.ui.main.MainViewSegmentedSingleLarge;
import com.micsig.tbook.ui.main.MainViewSegmentedSingleSmall;
import com.micsig.tbook.ui.util.TBookUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/10.
 */

public class MainLayoutCenterSegmented extends RelativeLayout {
    private static final String TAG = "MainLayoutCenterSegmented";
    public static final int TYPE_SINGLE_SMALL = 0;
    public static final int TYPE_SINGLE_LARGE = 1;
    public static final int TYPE_FIT = 2;

    private static final int SLIDE_SLOP = 50;
    private static final Integer[] SPEEDS ={1, 2, 4, 8};
    private static final int[] SIGNED_SPEEDS = new int[]{-8, -4, -2, -1, 1, 2, 4, 8};
    private int curSpeed = SPEEDS[0];

    private Context context;

    private TextView tvDisplayTitle;
    private MSwitchBox btnDisplay;
    private RelativeLayout layoutSingle;
    private TextView tvSinglePlaySpeed;
    private Button btnSinglePlay;
    private TextView tvSingleFirstTime;
    private RelativeLayout layoutSingleSmall;
    private MainViewSegmentedSingleSmall viewSingleSmall;
    private MainViewSegmentedSingleLarge viewSingleLarge;
    private TextView tvSingleFrameCount;
    private MButton_CheckBox btnSingleSlip;
    private RelativeLayout layoutFit;
    private TextView tvFitFirstTime;
    private MainLayoutSegmentedFit layoutFitStart;
    private MainLayoutSegmentedFit layoutFitEnd;
    private TextView tvFitFrameCount;

    private List<SegmentedSingleBean> frameList = new ArrayList<>();

    private TopDialogNumberKeyBoard dialogNumberKeyBoard;

    private MainMsgCenterSegmented msgPreCenterSegmented;
    private MainMsgCenterSegmented msgCenterSegmented;
    private ViewGroup rootViewGroup;
    /**
     * 此属性不是本dialog界面看上去的显现性的显示隐藏，而是下拉菜单按键的可点击性....
     */
    private boolean btnSegmentEnable = false;

    public MainLayoutCenterSegmented(Context context) {
        this(context, null);
    }

    public MainLayoutCenterSegmented(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterSegmented(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootViewGroup= (ViewGroup) View.inflate(context, R.layout.layout_maincenter_segmented, this);
//        setBackgroundResource(R.drawable.bg_center_segment_half);
        setBackgroundResource(R.drawable.shape_frame_bg_black);
        setClickable(true);

        tvDisplayTitle = (TextView) findViewById(R.id.dialogSegmentedDisplayTitle);
        btnDisplay = (MSwitchBox) findViewById(R.id.dialogSegmentedDisplayDetail);
        layoutSingle = (RelativeLayout) findViewById(R.id.dialogSegmentedSingleLayout);
        tvSinglePlaySpeed = (TextView) findViewById(R.id.singlePlaySpeed);
        btnSinglePlay = (Button) findViewById(R.id.singlePlayBtn);
        tvSingleFirstTime = (TextView) findViewById(R.id.dialogSingleFirstTime);
        layoutSingleSmall = (RelativeLayout) findViewById(R.id.dialogSingleSmallLayout);
        viewSingleSmall = (MainViewSegmentedSingleSmall) findViewById(R.id.dialogSingleSmallView);
        viewSingleLarge = (MainViewSegmentedSingleLarge) findViewById(R.id.dialogSingleLargeView);
        tvSingleFrameCount = (TextView) findViewById(R.id.dialogSingleFrameCount);
        btnSingleSlip = (MButton_CheckBox) findViewById(R.id.dialogSingleSlip);
        layoutFit = (RelativeLayout) findViewById(R.id.dialogSegmentedFitLayout);
        tvFitFirstTime = (TextView) findViewById(R.id.dialogFitFirstTime);
        layoutFitStart = (MainLayoutSegmentedFit) findViewById(R.id.dialogFitStartLayout);
        layoutFitEnd = (MainLayoutSegmentedFit) findViewById(R.id.dialogFitEndLayout);
        tvFitFrameCount = (TextView) findViewById(R.id.dialogFitFrameCount);

        btnDisplay.setOnToggleStateChangedListener(onToggleStateChangedListener);
        btnSinglePlay.setOnTouchListener(onTouchListener);
        viewSingleSmall.setOnEvents(onSmallEventListener);
        viewSingleLarge.setOnEvents(onLargeEventListener);
        btnSingleSlip.setOnClickListener(onCheckChangedListener);
        layoutFitStart.setOnClickListener(onClickListener);
        layoutFitEnd.setOnClickListener(onClickListener);

        initData();
        initControl();
    }

    private void initData() {
        msgCenterSegmented = new MainMsgCenterSegmented();
        msgCenterSegmented.setDisplay(btnDisplay.isState());
        msgCenterSegmented.setPlaying(false);
        msgCenterSegmented.setPlaySpeed(1);
        msgCenterSegmented.setCount(Integer.parseInt(tvSingleFrameCount.getText().toString()));
        msgCenterSegmented.setSingleOrder(true);
        msgCenterSegmented.setSingleLarge(false);
        msgCenterSegmented.setCurSingleFrame(viewSingleSmall.getCurBean());
        msgCenterSegmented.setFitStart(layoutFitStart.getBean());
        msgCenterSegmented.setFitEnd(layoutFitEnd.getBean());
        msgPreCenterSegmented = new MainMsgCenterSegmented();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegmented);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMenuRunStop);
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlipToOther);
        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_TIMESTAMP, eventUIObserver);
    }

    private void setCache() {
        String[] displayArray = getResources().getStringArray(R.array.sampleSegmentedDisplay);
        tvDisplayTitle.setText(displayArray[1]);

        int x = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_SEGMENTED_X);
        int y = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_SEGMENTED_Y);

        int display = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY);
        int start = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START);
        int end = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END);
        int order = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_ORDER);
        int speed = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_SAMPLE_SEGMENTED_PLAY_SPEED);
        initFrames();
        setDisplayCheck(display == 1, false);

        int speedIndex=Tools.indexOf(SPEEDS,s->s==speed);
        Command.get().getSample().SegmentedFra1(start,false);
        Command.get().getSample().SegmentedPlaySpeed(speedIndex,false);

//        tvFitFirstTime.setText();
//        tvFitFrameCount.setText();
        layoutFitStart.setBean(getFrameBean(start));
        layoutFitEnd.setBean(getFrameBean(end));

//        tvSingleFirstTime.setText();
//        tvSingleFrameCount.setText();


        setSingleSmallOrLarge(false);
        curSpeed = speed;
        tvSinglePlaySpeed.setText("X" + speed);
        setPlaySpeed(getPlaySpeed(curSpeed));

        msgCenterSegmented.setDisplay(btnDisplay.isState());
        msgCenterSegmented.setPlaying(false);
        msgCenterSegmented.setPlaySpeed(speed);
        msgCenterSegmented.setSingleOrder(order == 0);
        msgCenterSegmented.setCurSingleFrame(viewSingleSmall.getCurBean());
        msgCenterSegmented.setFitStart(layoutFitStart.getBean());
        msgCenterSegmented.setFitEnd(layoutFitEnd.getBean());

//        btnSegmentEnable = false;
        setPlay(false);

        boolean bxx = false;

        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        if(!runStop) {
            int state = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE);
            if(state == 0){
                show();
                bxx = true;
            }
        }
        if(!bxx){
            setVisibility(GONE);
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE, String.valueOf(false), true);
        }
        setLocation(x, y);

        setCurFrame(start-1);
    }

    private List<SegmentedSingleBean> initFrames() {
        frameList.clear();
        for (int i = 0; i < 2; i++) {
            SegmentedSingleBean bean = new SegmentedSingleBean(i + 1, String.valueOf(System.currentTimeMillis()));
            frameList.add(bean);
        }
        viewSingleSmall.setList(frameList);
        viewSingleLarge.setList(frameList);
        return frameList;
    }

    private List<SegmentedSingleBean> setFrames(List<SegmentedSingleBean> allFrame) {
        frameList.clear();
        frameList.addAll(allFrame);
        viewSingleSmall.setList(frameList);
        viewSingleLarge.setList(frameList);
        layoutFitStart.setBean(getFrameBean(layoutFitStart.getBean().getFrameId()));
        layoutFitEnd.setBean(getFrameBean(layoutFitEnd.getBean().getFrameId()));

        return frameList;
    }

    private void setDisplayCheck(boolean check, boolean isSendMsg) {
        Command.get().getSample().SegMented(check,false);
        btnDisplay.setState(check);
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY, String.valueOf(check ? 1 : 0));
//        String[] displays = getResources().getStringArray(R.array.sampleSegmentedDisplay);
//        String gang = "/";
//        String s = displays[0] + gang + displays[1];
//        SpannableString spannableString = new SpannableString(s);
//        if (check) {//单帧
//            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, displays[0].length()
//                    , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        } else {//拟合
//            spannableString.setSpan(new StyleSpan(Typeface.BOLD), (displays[0] + gang).length(), s.length()
//                    , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        btnDisplay.setText(spannableString);
        if (!check) {
            layoutSingle.setVisibility(VISIBLE);
            layoutFit.setVisibility(GONE);
            setSingleSmallOrLarge(false);
        } else {
            layoutSingle.setVisibility(GONE);
            layoutFit.setVisibility(VISIBLE);
        }

        msgCenterSegmented.setDisplay(btnDisplay.isState());
        if (isSendMsg) {
            sendMsg();
        }

//        Logger.d(MainLayoutCenterSegmented.TAG + "check:" + check);
//        Screen.getViewLocation(this);
//        Screen.getViewLocation(btnDisplay);
//        if (check) {
//            Logger.d(MainLayoutCenterSegmented.TAG + "check:true");
//            Screen.getViewLocation(layoutFitStart);
//            Screen.getViewLocation(layoutFitEnd);
//            Logger.d(MainLayoutCenterSegmented.TAG + "check:false");
//            Screen.getViewLocation(btnSinglePlay);
//            Screen.getViewLocation(viewSingleSmall);
//            Screen.getViewLocation(viewSingleLarge);
//            Screen.getViewLocation(btnSingleSlip);
//        } else {
//            Logger.d(MainLayoutCenterSegmented.TAG + "check:false");
//            Screen.getViewLocation(btnSinglePlay);
//            Screen.getViewLocation(viewSingleSmall);
//            Screen.getViewLocation(viewSingleLarge);
//            Screen.getViewLocation(btnSingleSlip);
//            Logger.d(MainLayoutCenterSegmented.TAG + "check:true");
//            Screen.getViewLocation(layoutFitStart);
//            Screen.getViewLocation(layoutFitEnd);
//        }
    }

    private void setSingleSmallOrLarge(boolean isSendMsg) {
        boolean isChecked = btnSingleSlip.isChecked();
        if (isChecked) {
            layoutSingleSmall.setVisibility(GONE);
            viewSingleSmall.setVisibility(GONE);
            viewSingleLarge.setVisibility(VISIBLE);

        } else {
            layoutSingleSmall.setVisibility(VISIBLE);
            viewSingleLarge.setVisibility(GONE);
            viewSingleSmall.setVisibility(VISIBLE);
        }
        msgCenterSegmented.setSingleLarge(isChecked);
        if (isSendMsg) {
            sendMsg();
        }
    }

    /**
     * 计算出来bitmap贴图的位置
     */
    public void getViewLocationOnScreen(int[] outLocation){
        Rect rect= Tools.getViewRect(this);
        Rect rectView;
        if (btnSingleSlip.isChecked()){
            rectView=Tools.getViewRect(viewSingleLarge);
        }else {
            rectView=Tools.getViewRect(viewSingleSmall);
        }
        outLocation[0]=rectView.left;
        outLocation[1]=rectView.top;
    }
    public Bitmap getBitmap(){
        boolean isChecked = btnSingleSlip.isChecked();
        Bitmap bitmap;
        if (isChecked) {
            bitmap = Bitmap.createBitmap(viewSingleLarge.getWidth()+2, viewSingleLarge.getHeight()+2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            Bitmap bp = viewSingleLarge.getBitmap();

            canvas.drawBitmap(bp,0, 0,null);
            bp.recycle();
        } else {
            bitmap = Bitmap.createBitmap(viewSingleSmall.getWidth()+2, viewSingleSmall.getHeight()+2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            Bitmap bp = viewSingleSmall.getBitmap();
            canvas.drawBitmap(bp,0, 0,null);
            bp.recycle();
        }
        return bitmap;
    }

    public int getType() {
        if (!btnDisplay.isState()) {//单帧
            if (layoutSingleSmall.getVisibility() == VISIBLE) {
                return TYPE_SINGLE_SMALL;
            } else {
                return TYPE_SINGLE_LARGE;
            }
        } else {//拟合
            return TYPE_FIT;
        }
    }

    public void moveCurFrame(int count) {
        if (!btnDisplay.isState()) {//单帧
            if (!isPlay) {
                int frame = curFrame + count;
                while (frame < 0) {
                    frame += frameList.size();
                }
                while (frame > frameList.size() - 1) {
                    frame -= frameList.size();
                }
                setCurFrame(frame);
            }
        }
    }

    private void setSegmentedParam() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        Scope scope = Scope.getInstance();
        List<SegmentedSingleBean> allFrame = scope.getAllFrameTimestamp();
        int curFrame = scope.getSegmentFrameNums() -1;
        long beginTime = SegmentSample.getInstance().getBeginSampleTime();
        Date date = new Date(beginTime);
        tvSingleFirstTime.setText(simpleDateFormat.format(date));
        tvFitFirstTime.setText(simpleDateFormat.format(date));
        tvSingleFrameCount.setText(String.valueOf(allFrame.size()));
        tvFitFrameCount.setText(String.valueOf(allFrame.size()));
        if (allFrame.size() == 0) {
            initFrames();
        } else {
            setFrames(allFrame);
        }
        setCurFrame(curFrame);
//        Logger.i(Command.TAG,"setSegmentedParam curFrame:"+curFrame+",getCurFrame:"+scope.getSegmentFrameNo());
    }

    public void sendMsg() {

//        Logger.d(TAG, "accept sendMsg() called MAINCENTER_SEGMENTED :" + msgCenterSegmented);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED, msgCenterSegmented);
            }
        }, 100);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedFra1:{
                     if (getType()!=TYPE_FIT){
                         int i=Integer.parseInt(commandMsgToUI.getParam());
                         if (i>frameList.size()) return;
                         setCurFrame(i-1);
                     }
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedPlay:{
                    if (getType()!=TYPE_FIT){
                        setPlay(true);
                    }
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedStop:{
                    if (getType()!=TYPE_FIT){
                        setPlay(false);
                    }
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedPlaySpeed:{
                    int i=Integer.parseInt(commandMsgToUI.getParam());
                    //之所以减一，是因为下面循环执行过程中加1了
                    if (i-1<0)i=SPEEDS.length-1;
                    else i=i-1;
                    curSpeed=SPEEDS[i];
                    setPlaySpeed(getPlaySpeed(curSpeed));
                    handler.sendEmptyMessage(MSG_PLAY_SPEED_CYCLE);
                    handler.sendEmptyMessage(MSG_PLAY_SPEED_END);
                }break;
            }
        }
    };

    private Consumer<TopMsgSampleSegmented> consumerSampleSegmented = new Consumer<TopMsgSampleSegmented>() {
        @Override
        public void accept(TopMsgSampleSegmented msgSegmented) throws Exception {
            if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 1) {
                hide();
                return;
            }
            boolean changed = false;
            SegmentSample segment = SegmentSample.getInstance();
            int maxSegmentNums = Scope.getInstance().getSegmentFrameNums();
            if (maxSegmentNums < 1) maxSegmentNums = 1;
            int start = segment.getFittingBegingFrame();
            start++;
            if (start > maxSegmentNums) start = maxSegmentNums;
            if (layoutFitStart.getBean().getFrameId() != start) {
                changed = true;
                layoutFitStart.setBean(getFrameBean(start));
                msgCenterSegmented.setFitStart(layoutFitStart.getBean());
            }
            int end = segment.getFittingEndFrame();
            end++;
            if (end > maxSegmentNums) end = maxSegmentNums;
            if (layoutFitEnd.getBean().getFrameId() != end) {
                changed = true;
                layoutFitEnd.setBean(getFrameBean(end));
                msgCenterSegmented.setFitEnd(layoutFitEnd.getBean());
            }
            boolean order = msgSegmented.getOrder().getIndex() == 0;
            if (viewSingleSmall.isPlayOrder() != order || viewSingleLarge.isPlayOrder() != order) {
                changed = true;
                setPlayOrder(order);
                msgCenterSegmented.setSingleOrder(msgSegmented.getOrder().getIndex() == 0);
            }
            boolean check = segment.getSegmentDisplayType() == 1;
            if (btnDisplay.isState() != check) {
                changed = true;
                setDisplayCheck(check, false);
                msgCenterSegmented.setDisplay(segment.getSegmentDisplayType() == 1);
            }
            if (changed) {
                sendMsg();
            }
        }
    };

    private Consumer<MainLeftMsgMenuRunStop> consumerMenuRunStop = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop msgMenuRunStop) throws Exception {
            if (msgMenuRunStop.getRunState() != MainLeftMsgMenuRunStop.STOP) {
                ((Activity) context).runOnUiThread(()->{
                    hide();
                });
            }
        }
    };

    private Consumer<MainMsgSlip> consumerMainSlipToOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            if (mainMsgSlip.isOpen()) {
                setVisibility(GONE);
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_SEGMENT_TIMESTAMP) {
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1) {
                    return;
                }
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {
                    return;
                }
//                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL) == 0) {
//                    Logger.d(TAG,"3");
//                    return;
//                }
                Scope scope = Scope.getInstance();
                if (scope.isInScrollMode() || scope.isInXYMode()) {
                    return;
                }

                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP)) {
                    return;
                }

                if (SegmentSample.getInstance().isSegmentEnable() && scope.getSegmentFrameNums() > 0) {
                    Logger.d(TAG, "eventUIObserver visible");
                    ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                    show();
                }

            }
        }
    };

    public void changePlaySpeed(boolean positive) {
        int tmpSpeed = isOrder ? curSpeed : (curSpeed * -1);
        if (positive) {
            for (int i = 0; i < SIGNED_SPEEDS.length; i++) {
                if (tmpSpeed == SIGNED_SPEEDS[i]) {
                    if (i == SIGNED_SPEEDS.length - 1) {
                        tmpSpeed = SIGNED_SPEEDS[i];
                        return;
                    } else {
                        tmpSpeed = SIGNED_SPEEDS[i + 1];
                    }
                    break;
                }
            }
        } else {
            for (int i = SIGNED_SPEEDS.length - 1; i >= 0; i--) {
                if (tmpSpeed == SIGNED_SPEEDS[i]) {
                    if (i == 0) {
                        tmpSpeed = SIGNED_SPEEDS[i];
                        return;
                    } else {
                        tmpSpeed = SIGNED_SPEEDS[i - 1];
                    }
                    break;
                }
            }
        }

        curSpeed = Math.abs(tmpSpeed);
        setPlayOrder(tmpSpeed > 0);
        tvSinglePlaySpeed.setText("X" + curSpeed);
        setPlaySpeed(getPlaySpeed(curSpeed));
        CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SAMPLE_SEGMENTED_PLAY_SPEED, String.valueOf(curSpeed));
        msgCenterSegmented.setPlaySpeed(curSpeed);
        msgCenterSegmented.setSingleOrder(isOrder);
        sendMsg();
    }

    public boolean isPlay() {
        return isPlay;
    }

    public boolean isBtnSegmentEnable() {
        return btnSegmentEnable;
    }

    public void show() {
        btnSegmentEnable = true;
        setVisibility(VISIBLE);
        setSegmentedParam();
        RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_BTNSEGMENT_ENABLE, true);
        Tools.PrintControlsLocation(TAG, rootViewGroup);
        int x = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_SEGMENTED_X);
        int y = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_SEGMENTED_Y);
        setLocation(x, y);
    }

    public void hide() {
        btnSegmentEnable = false;
        setPlay(false);
        initDialogNumberKeyBoard();
        if (dialogNumberKeyBoard.getVisibility() == VISIBLE && getVisibility() == VISIBLE) {
            dialogNumberKeyBoard.hide();
        }
        setVisibility(GONE);

        RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_BTNSEGMENT_ENABLE, false);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean cacheVisible = visibility == VISIBLE;
        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE) != cacheVisible) {
            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE, String.valueOf(cacheVisible));
            RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_VISIBLE, cacheVisible);
        }
    }

    private void initDialogNumberKeyBoard() {
        if (dialogNumberKeyBoard == null) {
            dialogNumberKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
        }
    }

    private void setExternalKeyBackStateFocus(){
        ExternalKeysProtocol.BackStateUpdate(ExternalKeysProtocol.BACKSTATE_SEGMENT);
    }
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            if (view.getId() == btnDisplay.getId()) {
                setDisplayCheck(state, true);
            }
        }
    };

    private OnClickListener onCheckChangedListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            if (v.getId() == btnSingleSlip.getId()) {
                setSingleSmallOrLarge(true);
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            if (v.getId() == layoutFitStart.getId()) {
                initDialogNumberKeyBoard();
                dialogNumberKeyBoard.setDecimalData(String.valueOf(layoutFitStart.getBean().getFrameId())
                        , 8, IDigits.DIGITS_10, new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                if (Integer.parseInt(result) > frameList.size()) {
                                    result = String.valueOf(frameList.size());
                                } else if (Integer.parseInt(result) <= 0) {
                                    result = String.valueOf(1);
                                }
                                Command.get().getSample().SegmentedFra2(Integer.parseInt(result),false);
                                layoutFitStart.setBean(getFrameBean(Integer.parseInt(result)));
                                msgCenterSegmented.setFitStart(layoutFitStart.getBean());
                                sendMsg();
                            }
                        });
            } else if (v.getId() == layoutFitEnd.getId()) {
                initDialogNumberKeyBoard();
                dialogNumberKeyBoard.setDecimalData(String.valueOf(layoutFitEnd.getBean().getFrameId())
                        , 8, IDigits.DIGITS_10, new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result);
                                if (Integer.parseInt(result) > frameList.size()) {
                                    result = String.valueOf(frameList.size());
                                } else if (Integer.parseInt(result) <= 0) {
                                    result = String.valueOf(1);
                                }
                                Command.get().getSample().SegmentedFra3(Integer.parseInt(result),false);
                                SegmentedSingleBean frameBean = getFrameBean(Integer.parseInt(result));
                                layoutFitEnd.setBean(frameBean);
                                msgCenterSegmented.setFitEnd(layoutFitEnd.getBean());
                                sendMsg();
                            }
                        });
            }
        }
    };

    private SegmentedSingleBean getFrameBean(int frameId) {
//        Logger.d("frameList size:" + frameList.size());
//        Logger.d("frameList:" + frameList);
//        Logger.d("frameId:"+frameId);
        if (frameId > 0) {
            if (frameList.size() >0 && frameList.size() >= frameId) {
                return frameList.get(frameId - 1);
            } else {
                return new SegmentedSingleBean(frameId , TBookUtil.getMsFromNs(System.currentTimeMillis()));
            }
        } else {
            return new SegmentedSingleBean(1, TBookUtil.getMsFromNs(System.currentTimeMillis()));
        }
    }

    private int curFrame;//0 - (length-1)...
    private boolean isOrder;
    private boolean isPlay;
    private int playSpeed = MainViewSegmentedSingleLarge.PLAYSPEEP_1X;

    private void setCurFrame(int curFrame) {
//        Logger.i(Command.TAG,"curFrame:"+curFrame);
        Command.get().getSample().SegmentedFra1(curFrame+1,false);
        this.curFrame = curFrame;
        Scope.getInstance().setSegmentFrameNo(curFrame);
        Logger.i(Command.TAG,"setFrame:"+curFrame+",getFrame:"+Scope.getInstance().getSegmentFrameNo());
        if (viewSingleLarge.getVisibility() == View.VISIBLE && viewSingleLarge.getCurFrame() != curFrame) {
            viewSingleLarge.setCurFrame(curFrame);
        } else if (viewSingleSmall.getVisibility() == View.VISIBLE && viewSingleSmall.getCurFrame() != curFrame) {
            viewSingleSmall.setCurFrame(curFrame);
        }
    }

    private void setPlayOrder(boolean isOrder) {
        this.isOrder = isOrder;
        if (viewSingleLarge.getVisibility() == View.VISIBLE && viewSingleLarge.isPlayOrder() != isOrder) {
            viewSingleLarge.setPlayOrder(isOrder);
        } else if (viewSingleSmall.getVisibility() == View.VISIBLE && viewSingleSmall.isPlayOrder() != isOrder) {
            viewSingleSmall.setPlayOrder(isOrder);
        }
    }

    private void setPlay(boolean isPlay) {
        this.isPlay = isPlay;
        btnSinglePlay.setBackgroundResource(isPlay ? R.drawable.play_down : R.drawable.play_normal);
        if (viewSingleLarge.getVisibility() == View.VISIBLE) {
            viewSingleLarge.setPlay(isPlay);
        } else if (viewSingleSmall.getVisibility() == View.VISIBLE) {
            viewSingleSmall.setPlay(isPlay);
        }
    }

    private void setPlaySpeed(int playSpeed) {
        this.playSpeed = playSpeed;
        if (viewSingleLarge.getVisibility() == View.VISIBLE) {
            viewSingleLarge.setPlaySpeed(playSpeed);
        } else if (viewSingleSmall.getVisibility() == View.VISIBLE) {
            viewSingleSmall.setPlaySpeed(playSpeed);
        }
    }

    private MainViewSegmentedSingleSmall.OnEvents onSmallEventListener = new MainViewSegmentedSingleSmall.OnEvents() {
        @Override
        public void onClick(int curFrame) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            initDialogNumberKeyBoard();
            dialogNumberKeyBoard.setDecimalData(String.valueOf(curFrame)
                    , 8, IDigits.DIGITS_10, new TopDialogNumberKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            result = TBookUtil.getNumRemovePreZero(result);
                            if (Integer.parseInt(result) > frameList.size()) {
                                result = String.valueOf(frameList.size());
                            } else if (Integer.parseInt(result) <= 0) {
                                result = String.valueOf(1);
                            }
                            setCurFrame(Integer.parseInt(result)-1 );
                            msgCenterSegmented.setCurSingleFrame(viewSingleSmall.getCurBean());
                            sendMsg();
                        }
                    });
        }

        @Override
        public void onCurrFrameChange(int currFrame) {
            Command.get().getSample().SegmentedFra1(currFrame+1,false);
            MainLayoutCenterSegmented.this.curFrame = currFrame;
            Scope.getInstance().setSegmentFrameNo(currFrame);
            Logger.i(Command.TAG,"setFrame:"+curFrame+",getFrame:"+Scope.getInstance().getSegmentFrameNo());
            msgCenterSegmented.setCurSingleFrame(viewSingleSmall.getCurBean());
//            ((MainActivity) context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
            sendMsg();
//                }
//            });
        }

        @Override
        public void onOrderChange(boolean order) {
            Logger.d(TAG, "accept small onOrderChange() called with: order = [" + order + "]");
            setPlayOrder(order);
            msgCenterSegmented.setSingleOrder(order);
            sendMsg();
        }

        @Override
        public void onVisibleChange(int visibility) {
            if (visibility == View.GONE) {
                viewSingleSmall.setPlay(false);
            } else if (visibility == View.VISIBLE) {
                viewSingleSmall.setCurFrame(curFrame);
                viewSingleSmall.setPlaySpeed(playSpeed);
                viewSingleSmall.setPlayOrder(isOrder);
                viewSingleSmall.setPlay(isPlay);
            }
            boolean visible = visibility == View.VISIBLE;
            if (msgCenterSegmented.isSingleLarge().isValue() == visible) {
                msgCenterSegmented.setSingleLarge(!visible);
                sendMsg();
            }
        }
    };

    private MainViewSegmentedSingleLarge.OnEvents onLargeEventListener = new MainViewSegmentedSingleLarge.OnEvents() {
        @Override
        public void onClick(int curFrame) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            initDialogNumberKeyBoard();
            dialogNumberKeyBoard.setDecimalData(String.valueOf(curFrame)
                    , 8, IDigits.DIGITS_10, new TopDialogNumberKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            result = TBookUtil.getNumRemovePreZero(result);
                            if (Integer.parseInt(result) > frameList.size()) {
                                result = String.valueOf(frameList.size());
                            } else if (Integer.parseInt(result) <= 0) {
                                result = String.valueOf(1);
                            }
                            setCurFrame(Integer.parseInt(result) -1);
                            msgCenterSegmented.setCurSingleFrame(viewSingleLarge.getCurBean());
                            sendMsg();
                        }
                    });
        }

        @Override
        public void onCurrFrameChange(int currFrame) {
            Command.get().getSample().SegmentedFra1(currFrame+1,false);
            MainLayoutCenterSegmented.this.curFrame = currFrame;
            Scope.getInstance().setSegmentFrameNo(currFrame);
            Logger.i(Command.TAG,"setFrame:"+curFrame+",getFrame:"+Scope.getInstance().getSegmentFrameNo());
//            Logger.i(Command.TAG,"curFrame:"+currFrame+",getcurframe:"+ Scope.getInstance().getSegmentFrameNo());
            msgCenterSegmented.setCurSingleFrame(viewSingleLarge.getCurBean());
            sendMsg();
        }

        @Override
        public void onOrderChange(boolean order) {
            Logger.d(TAG, "accept large onOrderChange() called with: order = [" + order + "]");
            setPlayOrder(order);
            msgCenterSegmented.setSingleOrder(order);
            sendMsg();
        }

        @Override
        public void onVisibleChange(int visibility) {
            if (visibility == View.GONE) {
                viewSingleLarge.setPlay(false);
            } else if (visibility == View.VISIBLE) {
                viewSingleLarge.setCurFrame(curFrame);
                viewSingleLarge.setPlayOrder(isOrder);
                viewSingleLarge.setPlaySpeed(playSpeed);
                viewSingleLarge.setPlay(isPlay);
            }
            boolean visible = visibility == View.VISIBLE;
            if (msgCenterSegmented.isSingleLarge().isValue() != visible) {
                msgCenterSegmented.setSingleLarge(visible);
                sendMsg();
            }
        }
    };

    private OnTouchListener onTouchListener = new OnTouchListener() {
        private int mLastMotionX, mLastMotionY;
        private boolean isMoved = false;
        private boolean isLongClick = false;


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = x;
                    mLastMotionY = y;
                    isMoved = false;
                    isLongClick = false;
                    /*
                     * 将mLongPressRunnable放进任务队列中，到达设定时间后开始执行
                     * 这里的长按时间采用系统标准长按时间
                     */
                    postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isMoved) break;
                    int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
                    if (Math.abs(mLastMotionX - x) > touchSlop
                            || Math.abs(mLastMotionY - y) > touchSlop) {
                        //移动超过阈值，则表示移动了
                        isMoved = true;
                        removeCallbacks(mLongPressRunnable);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //释放了
                    removeCallbacks(mLongPressRunnable);
                    handler.sendEmptyMessage(MSG_PLAY_SPEED_END);
                    if (!isMoved && !isLongClick) {
                        onCheckChanged(!isPlay);
                    }
                    break;
            }
            return true;
        }

        private void onCheckChanged(boolean isChecked) {
            PlaySound.getInstance().playButton();
            setExternalKeyBackStateFocus();
            setPlay(isChecked);
            msgCenterSegmented.setPlaying(isChecked);
            sendMsg();
        }

        //长按的runnable
        private Runnable mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
//                performLongClick();    // 执行长按事件（如果有定义的话）
                isLongClick = true;
                if (CacheUtil.get().isLoadParamComplete()) {
                    handler.sendEmptyMessage(MSG_PLAY_SPEED_START);
                }
            }
        };
    };

    public static final int MSG_PLAY_SPEED_START = 0xb1;
    public static final int MSG_PLAY_SPEED_END = 0xb2;
    public static final int MSG_PLAY_SPEED_CYCLE = 0xb3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY_SPEED_START:
                    if (handler.hasMessages(MSG_PLAY_SPEED_CYCLE)) {
                        handler.removeMessages(MSG_PLAY_SPEED_CYCLE);
                    }
                    handler.sendEmptyMessage(MSG_PLAY_SPEED_CYCLE);
                    break;
                case MSG_PLAY_SPEED_END:
                    if (handler.hasMessages(MSG_PLAY_SPEED_CYCLE)) {
                        handler.removeMessages(MSG_PLAY_SPEED_CYCLE);
                    }
                    break;
                case MSG_PLAY_SPEED_CYCLE:
                    for (int i = 0; i < SPEEDS.length; i++) {
                        if (Math.abs(curSpeed) == SPEEDS[i]) {
                            if (i == SPEEDS.length - 1) {
                                curSpeed = SPEEDS[0];
                            } else {
                                curSpeed = SPEEDS[i + 1];
                            }
                            tvSinglePlaySpeed.setText("X" + curSpeed);
                            setPlaySpeed(getPlaySpeed(curSpeed));
                            int index= Tools.indexOf(SPEEDS,s->s==curSpeed);
                            Command.get().getSample().SegmentedPlaySpeed(index,false);
                            CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SAMPLE_SEGMENTED_PLAY_SPEED, String.valueOf(curSpeed));
                            msgCenterSegmented.setPlaySpeed(curSpeed);
                            sendMsg();
                            break;
                        }
                    }
                    handler.sendEmptyMessageDelayed(MSG_PLAY_SPEED_CYCLE, 1000);
                    break;
            }
        }
    };

    private int getPlaySpeed(int curSpeed) {
        if (curSpeed == SPEEDS[0]) {
            return MainViewSegmentedSingleSmall.PLAYSPEEP_1X;
        } else if (curSpeed == SPEEDS[1]) {
            return MainViewSegmentedSingleSmall.PLAYSPEEP_2X;
        } else if (curSpeed == SPEEDS[2]) {
            return MainViewSegmentedSingleSmall.PLAYSPEEP_4X;
        } else if (curSpeed == SPEEDS[3]) {
            return MainViewSegmentedSingleSmall.PLAYSPEEP_8X;
        } else {
            return MainViewSegmentedSingleSmall.PLAYSPEEP_1X;
        }
    }

    float downX, downY;
    float moveX, moveY;

    int left, top;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getRawX();
                moveY = event.getRawY();

                Rect screen = GlobalVar.get().getScreen();
                float tmpX = getX() + (moveX - downX);
                if (tmpX < screen.left) {
                    tmpX = screen.left;
                }
                if (tmpX + getWidth() > screen.right) {
                    tmpX = screen.right - getWidth();
                }

                float tmpY = getY() + (moveY - downY);
                if (tmpY < screen.top) {
                    tmpY = screen.top;
                }
                if (tmpY + getHeight() > screen.bottom) {
                    tmpY = screen.bottom - getHeight();
                }
                this.setX(tmpX);
                this.setY(tmpY);
                downX = moveX;
                downY = moveY;
                RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_MOVE, 0);
                break;
            case MotionEvent.ACTION_UP:
                setExternalKeyBackStateFocus();
                if (left != this.getX() || top != this.getY()) {
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SEGMENTED_X, String.valueOf((int) this.getX()));
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_SEGMENTED_Y, String.valueOf((int) this.getY()));
                }
                break;
        }
        return true;
    }

    public void setLocation(int x, int y) {
        setX(x);
        setY(y);
        RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_MOVE, 0);
    }

    private Rect r = new Rect();

    public boolean containsPoint(int x, int y) {
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        return x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE;
    }

    public boolean containsDownPoint(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        boolean b = x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE && event.getAction() == MotionEvent.ACTION_DOWN;
        return b;
    }
}
