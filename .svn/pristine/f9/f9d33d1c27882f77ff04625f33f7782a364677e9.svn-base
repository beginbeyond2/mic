package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.graphics.Bitmap;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by liwb on 2017/5/18.
 * 触发时刻
 */

public class TriggerTimebase extends VerticalChannelBase {
    private static final String TAG = "TriggerTimebase";

//    private static final Bitmap resBmp = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap();
//    private static final Bitmap resBmpLeft = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap();
//    private static final Bitmap resBmpRight = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap();

    private static Bitmap resBmp[];
    private MainWaveMsgTriggerTimeBase mainWaveMsgTriggerTimeBase = new MainWaveMsgTriggerTimeBase(x);

    //region 单例
    public static class TriggerTimebaseHolder {
        public static final TriggerTimebase instance = new TriggerTimebase();
    }

    public static TriggerTimebase getInstance() {
        return TriggerTimebaseHolder.instance;
    }
    //endregion


    public TriggerTimebase() {
        super();
        resBmp = new Bitmap[6];
        resBmp[0] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time);
        resBmp[1] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time_l);
        resBmp[2] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(), R.drawable.svg_trigger_time_r);

        resBmp[3] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time);
        resBmp[4] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time_l);
        resBmp[5] = BitmapUtil.getBitmapFromDrawable(App.get().getApplicationContext(),R.drawable.svg_trigger_time_r);

        init(resBmp);
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_POS, eventUIObserver);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerMainTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPCENTER_TEXT_GONE).subscribe(consumerMainTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
    }

    public void init() {
    }

    public void setCache() {
        setX_dis(getCacheForTimeBasePosition());
    }

    //x轴移动50%功能
    public void rstX_50percentt() {
        CursorManage.getInstance().setCursorTrace(true);

        int ch_idx = ChannelFactory.getChActivate();
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        if(ChannelFactory.isRefCh(ch_idx) && temp != 0){ //独立情况
            setX(ScopeBase.getWidth() / 2 - ChannelFactory.getRefChannel(ch_idx).getTimePoseOfViewPix_original_nowScale());
        }else{
            setX(ScopeBase.getWidth() / 2);
        }

        CursorManage.setCursorByTimebaseTrace();
        CursorManage.getInstance().setCursorTrace(false);
    }

    /**
     * 带回算的设置timeBase的位置...
     */
    @Override
    public void setX(long x) {
        long pix = x;
        int ch_idx = ChannelFactory.getChActivate();
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        if (ChannelFactory.isRefCh(ch_idx)) {
            RefChannel refChannel = ChannelFactory.getRefChannel(ch_idx);
            if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) {
                refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x);
                refChannel.correctTimePose();//某些情况下需要纠正
                pix = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();//因为可能被纠正，所以回算
            } else {
                if (temp ==0 ) { //跟随
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x);
                    HorizontalAxis.getInstance().correctTimePose_poseMove(); //某些情况下需要纠正
                    pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); //因为可能被纠正，所以回算
                } else {
                    refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x);
                    refChannel.correctTimePose();//某些情况下需要纠正
                    pix = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();//因为可能被纠正，所以回算
                }
            }
        } else {
            if (ChannelFactory.isMath_FFT_Ch(ch_idx)) {
                //数学FFT
                HorizontalAxisMath horizontalAxisFFT = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT();
                horizontalAxisFFT.setXPosOfView(ScopeBase.getWidth() / 2 - x);
                horizontalAxisFFT.correctXPose();
                pix = ScopeBase.getWidth() / 2 - horizontalAxisFFT.getXPosOfView();
            } else {
                HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x);
                HorizontalAxis.getInstance().correctTimePose_poseMove(); //某些情况下需要纠正
                pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); //因为可能被纠正，所以回算
            }
        }

        setX_dis(pix);
    }

    /**
     * 不带回算的设置timeBase的位置...
     */
    public void setX_dis(long x) {

        int ch_idx = ChannelFactory.getChActivate();
        int temp = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
        RefChannel refChannel = ChannelFactory.getRefChannel(ch_idx);
        if(ChannelFactory.isRefCh(ch_idx)){
            if (temp == 0 && refChannel.getRefType() != WaveData.FFT_WAVE) {//跟随
                if (HorizontalAxis.getInstance().getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) {
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x);
                }
                for (int i = ChannelFactory.REF1; i <= ChannelFactory.REF8; i++) {
                    refChannel = ChannelFactory.getRefChannel(i);
                    if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE && refChannel.getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) {
                        refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x);
                        putCacheForTimeBasePosition(x, i);
                    }
                }
            } else {
                if (refChannel != null && refChannel.getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) {
                    refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x);
                }
            }
        }else{
            if(ChannelFactory.isMath_FFT_Ch(ch_idx)){
                //数学FFT
                HorizontalAxisMath horizontalAxisFFT = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT();
                if (horizontalAxisFFT.getXPosOfView() != ScopeBase.getWidth() / 2 - x) {
                    horizontalAxisFFT.setXPosOfView(ScopeBase.getWidth() / 2 - x);
                }
            } else {
                if (HorizontalAxis.getInstance().getTimePoseOfViewPix() != ScopeBase.getWidth() / 2 - x) {
                    HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - x);
                }
                if (temp == 0) {//跟随
                    for (int i = ChannelFactory.REF1; i <= ChannelFactory.REF8; i++) {
                        refChannel = ChannelFactory.getRefChannel(i);
                        if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE) {
                            refChannel.setXPosOfViewPix(ScopeBase.getWidth() / 2 - x);
                            putCacheForTimeBasePosition(x, i);
                        }
                    }
                }
            }
        }

        super.setX(x);

        //if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YT) // zoom 为啥不保存
        {
            putCacheForTimeBasePosition(x);
        }
        mainWaveMsgTriggerTimeBase.setX(x);
        RxBus.getInstance().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, mainWaveMsgTriggerTimeBase);
        CursorManage.getInstance().timeBaseMove();
    }

    public void setX_disFromEventBus(long x) {
        setX_dis(x);
        //   if (!getVisible())
        //       return;
//        super.setXFromEventBus(x);
        //if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YT)  // zoom 为啥不保存
//        {
//            putCacheForTimeBasePosition(x);
//        }
//        mainWaveMsgTriggerTimeBase.setX(x);
//        RxBus.getInstance().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, mainWaveMsgTriggerTimeBase);
//        CursorManage.getInstance().timeBaseMove();
    }

    public void drawActiveTrigTime(boolean isFromEventBus) {
        long x = getCacheForTimeBasePosition();
        CursorManage.getInstance().timeBaseMove();
        if (!getVisible())
            return;
        if (isFromEventBus) {
            setXFromEventBus(x);
        } else {
            super.setX(x);
        }
    }

    private void putCacheForTimeBasePosition(long x) {
        putCacheForTimeBasePosition(x, ChannelFactory.getChActivate());
    }

    public void putCacheForTimeBasePosition(long x, int chIDX) {
        //Logger.d(TAG, "putCacheForTimeBasePosition() called with: x = [" + x + "], chIDX = [" + chIDX + "]");
        if(ChannelFactory.isDynamicCh(chIDX) || ChannelFactory.isSerialCh(chIDX)){
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x));
        }else if(ChannelFactory.isMathCh(chIDX)){
            if (ChannelFactory.getMathChannel(chIDX).getMathType() != MathWave.MATH_FFTWAVE) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x));
            } else {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_FFTMOD, String.valueOf(x));
            }
        }else if(ChannelFactory.isRefCh(chIDX)){
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.toUiChNo(chIDX), String.valueOf(x));
            int refTimeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
            if (refTimeBaseIndex == 0) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL, String.valueOf(x));
            }
        }
    }

//    public long setXFromCurCh() {
//        long position = getCacheForTimeBasePosition();
//        setX(position);
//        return position;
//    }

    private long getCacheForTimeBasePosition() {
        long l;
        int curIdx = ChannelFactory.getChActivate();
        if(ChannelFactory.isRefCh(curIdx)){
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.toUiChNo(curIdx));
        }else if(ChannelFactory.isMath_FFT_Ch(curIdx)){
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_FFTMOD);
        }else {
            l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL);
        }

        Logger.d(TAG, "getCacheForTimeBasePosition() called ch:" + ChannelFactory.getChActivate() + "," + l);
        return l;
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_TIME_POS) {
                long pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
//                Logger.i(TAG, "eventUIObserver() ==>"
//                        +" pix:"+pix+" - "+getCacheForTimeBasePosition());
                if (pix != getCacheForTimeBasePosition()) {
                    setX_disFromEventBus(pix);
                }
            }
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            update();
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<MainTopMsgRightGone> consumerMainTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone msgTopRightGone) throws Exception {
            boolean preVisible = getVisible();
//            setVisible(msgTopRightGone.isVisible());
//            if (!preVisible && msgTopRightGone.isVisible()) {
//                setX_dis(getX());
//            }else if(preVisible && !msgTopRightGone.isVisible()){
//                Scope scope = Scope.getInstance();
//                if(scope.isRun() && scope.isInScrollMode()){
//                    setX(ScopeBase.getWidth() / 2);
//                }
//            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TIMEBASE_POSITION: {
                    double d = Double.valueOf(commandMsgToUI.getParam());
                    long x = HorizontalAxis.getInstance().SCPIQueryPixInScreenFromTImePosVal(d);
                    CursorManage.getInstance().setCursorTrace(true);
                    setX(GlobalVar.get().getMainWave().x / 2 - x);
                    CursorManage.setCursorByTimebaseTrace();
                    CursorManage.getInstance().setCursorTrace(false);
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFT_Position:{
                    String[] param= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int ch_idx=Integer.parseInt(param[0]);
                    double d = Double.parseDouble(param[1]);
                    int activeCh=ChannelFactory.getChActivate();
                    ChannelFactory.chActivate(ch_idx);
                    long x = ChannelFactory.getMathChannel(ch_idx).getHorizontalAxisMathFFT().SCPIQueryPixInScreenFromTImePosVal(d);
//                    Logger.i(Command.TAG,"x:"+x+",d:"+d);
                    setX(GlobalVar.get().getMainWave().x / 2 - x);
                    ChannelFactory.chActivate(activeCh);
                }break;
                case CommandMsgToUI.FLAG_REF_Timebase_Position:{
                    String[] param= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int ch_idx=Integer.parseInt(param[0]);
                    double d = Double.valueOf(param[1]);

                    int activeCh=ChannelFactory.getChActivate();
                    ChannelFactory.chActivate(ch_idx);
                    ChannelFactory.setRefActive(ch_idx);

//                    Logger.i(Command.TAG,"perPix:"+ ChannelFactory.getRefChannel(ch_idx).getRefTimePerPix());
//                    Logger.i(Command.TAG,"scaleVal:"+ChannelFactory.getRefChannel(ch_idx).getRefTimeScaleVal());
                    long x=(long) (d/ChannelFactory.getRefChannel(ch_idx).getRefTimePerPix());

//                    long x = HorizontalAxis.getInstance().SCPIQueryPixInScreenFromTImePosVal(d);
                    setX(GlobalVar.get().getMainWave().x / 2 - x);
                    ChannelFactory.chActivate(activeCh);

                }break;
            }
        }
    };

    public void setOffsetX(int offsetX) {
        CursorManage.getInstance().setCursorTrace(true);

        if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate()) && (getX()-offsetX)>GlobalVar.get().getMainWave().x/2){
            offsetX=(int)(getX()-GlobalVar.get().getMainWave().x/2);
        }

        putCacheForTimeBasePosition(this.x - offsetX);
        super.setOffsetX(offsetX);
        CursorManage.setCursorByTimebaseTrace();
        CursorManage.getInstance().setCursorTrace(false);
    }
    @Override
    public void movePix(double px) {
        CursorManage.getInstance().setCursorTrace(true);
        if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate()) && (getX()+px)>GlobalVar.get().getMainWave().x/2){
            px=(int)(GlobalVar.get().getMainWave().x/2-getX());
        }
        super.movePix(px);
        CursorManage.setCursorByTimebaseTrace();
        CursorManage.getInstance().setCursorTrace(false);
    }
    //    //region 属性
//    int x, y;
//    private int LineNameID = IWave.TriggerTime;
//
//    public int getLineNameID() {
//        return LineNameID;
//    }
//
//    public int getX() {
//        return x;
//    }
//
//
//    public void setX(int x) {
//        this.x = x;
//        RxBus.get().post(RxEnum.MAINWAVE_TRIGGERTIMEBASE, new MainWaveMsgTriggerTimeBase(x));
//        draw();
//    }
//
//    public void initX() {
//        setX(GlobalVar.get().getMainWave().x / 2);
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public void setY(int y) {
//        this.y = y;
//    }
//    //endregion
//
//    private static Bitmap bmp;
//    private Canvas mCanvas;
//    private Paint paint;
//    private boolean isChanageBitmap = false;
//
//
//    public TriggerTimebase() {
//        bmp = Bitmap.createBitmap(resBmpLeft.getWidth(), resBmpLeft.getHeight(), Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(bmp);
//        paint = new Paint();
//        x = GlobalVar.get().getMainWave().x / 2;
//        y = 0;
//        draw();
//    }
//
//    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmpLeft.getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, null);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth(), 0, null);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp.getWidth() / 2, 0, null);
//            }
//
//        }
//    }
//
//    public void draw(ICanvasGL canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmpLeft.getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, isChanageBitmap);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth(), 0, isChanageBitmap);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp.getWidth() / 2, 0, isChanageBitmap);
//            }
//            isChanageBitmap = false;
//        }
//    }
//
//    private void draw() {
//        synchronized (bmp) {
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            mCanvas.drawPaint(paint);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
//            if (this.x <= resBmpLeft.getWidth()) {
//                mCanvas.drawBitmap(resBmpLeft, 0, 0, paint);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) - resBmpRight.getWidth()) {
//                mCanvas.drawBitmap(resBmpRight, 0, 0, paint);
//            } else {
//                mCanvas.drawBitmap(resBmp, 0, 0, paint);
//            }
//
//            isChanageBitmap = true;
//        }
//
//    }
//
//    public void movePix() {
//        setX(x + 1);
//    }
//
//    public void subPixMove() {
//        setX(x - 1);
//    }

}
