package com.micsig.tbook.tbookscope.wavezone.display;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.text.TextPaint;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.DoubleUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;
import com.micsig.tbook.tbookscope.main.maincenter.MainCenterMsgChannels;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.trigger.MainWaveMsgTriggerTimeBase;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.Arrays;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2023-8-15 14:02
 */
public class RulerManage implements IWorkMode {

    private static final String TAG = "RulerManage";
    private static RulerManage ins=null;
    public static RulerManage getIns(){
        if (ins==null){
            ins=new RulerManage();
        }
        return ins;
    }


    public RulerManage(){
        initView();
        initControl();
    }
    public void init(){

    }

    private Bitmap rowBmp[]=new Bitmap[11];//竖向刻度
    private Bitmap colBmp[]=new Bitmap[12];//横向刻度
    private Paint paint;
    private TextPaint textPaint;
    private Canvas mCanvas;
    private Xfermode modeClear=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private Xfermode modeSrc=new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private  volatile boolean isDraw=false;
    private ICanvasGL canvasGL;
	private Context context=App.get().getApplicationContext();
    private boolean bShow=true;
    public void setShow(boolean bShow){
        this.bShow=bShow;
    }
    public boolean isShow(){
        return bShow;
    }

    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }

    private int perHorGridPx = GlobalVar.get().getMainWave().x / 12, perVerGridPx = GlobalVar.get().getMainWave().y / 10;
    private void initView(){
        paint=new Paint();
        paint.setTextSize(16);
        paint.setAntiAlias(true);
//        paint.setTypeface(Typeface.SERIF);
        textPaint=new TextPaint();
        textPaint.setTextSize(16);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeWidth(4);

        mCanvas=new Canvas();
        for(int i=0;i<rowBmp.length;i++){
            rowBmp[i]=Bitmap.createBitmap(100,20, Bitmap.Config.ARGB_8888);
        }
        for(int i=0;i<colBmp.length;i++){
            colBmp[i]=Bitmap.createBitmap(100,20, Bitmap.Config.ARGB_8888);
        }
    }
    private void initControl(){
//        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_SELECT).subscribe(consumerCenterChannelsSelect);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);
        RxBus.getInstance().getObservable(RxEnum. MAINWAVE_TRIGGERTIMEBASE).subscribe(consumerTriggerTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
        EventFactory.addEventObserver(EventFactory.EVENT_UI_SAMPLE_GRAPH, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_POS, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_VPOS, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_REF_VPOS, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_FFT_SCALE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_EVENT, eventUIObserver);
//        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE,eventUIObserver);
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);
    }




    private synchronized void drawRowBmp(){
        synchronized (this) {
            String[] s = getRowContent();
            for (int i = 0; i < rowBmp.length; i++) {//竖向
                paint.setXfermode(modeClear);
                mCanvas.setBitmap(rowBmp[i]);
                mCanvas.drawPaint(paint);
                paint.setXfermode(modeSrc);

                Rect rect = Tools.getTextRect(s[i], paint);
                mCanvas.drawText(s[i], rowBmp[i].getWidth() - rect.width(), 16, textPaint);
                mCanvas.drawText(s[i], rowBmp[i].getWidth() - rect.width(), 16, paint);
            }
            isDraw = true;
        }
        onRefresh();
    }
    private String[] getRowContent(){

        String[] strings = new String[rowBmp.length];
        Arrays.fill(strings, "");
        int curCh = ChannelFactory.getChActivate();
        if (ChannelFactory.isChOpen(curCh) == false) {
            return strings;
        }
        double interval, zeroVal;
        String unit = "";
        if (ChannelFactory.isDynamicCh(curCh)) {
            Channel channel = ChannelFactory.getDynamicChannel(curCh);
            if (channel == null) return strings;
            interval = channel.getVScaleVal();
            unit = ChannelFactory.getProbeType(curCh);
            zeroVal = channel.getVScaleVal() * channel.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());
        } else if (ChannelFactory.isMathCh(curCh)) {
            MathChannel math = ChannelFactory.getMathChannel(curCh);
            if (math == null) return strings;
            interval = math.getVScaleVal();
            unit = ChannelFactory.getProbeType(curCh);
            zeroVal = math.getVScaleIdVal() * math.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());
        } else if (ChannelFactory.isRefCh(curCh)) {
            RefChannel ref = ChannelFactory.getRefChannel(curCh);
            if (ref == null) return strings;
            interval = ref.getVScaleVal();
            unit = ChannelFactory.getProbeType(curCh);
            zeroVal = ref.getVScaleVal() * ref.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());
        } else {
            return strings;
        }

        double v = interval / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());
        for (int i = 0; i < strings.length; i++) {
            double val = zeroVal * (-1) + interval * (5 - i);
            if (Math.abs(val) < v) {
                val = 0;
            }
            strings[i] = TBookUtil.getFourFromD_Trim0(val) + unit;
        }
        paint.setColor(TChan.getChannelColor(context,TChan.toUiChNo(curCh)));
        return strings;
    }

    private synchronized void drawColBmp(){
        synchronized (this) {
            String[] s = getColContent();
            int color = App.get().getApplicationContext().getResources().getColor(R.color.main_text_color);
            paint.setColor(color);
            for (int i = 0; i < colBmp.length; i++) {//横向
                paint.setXfermode(modeClear);
                mCanvas.setBitmap(colBmp[i]);
                mCanvas.drawPaint(paint);
                paint.setXfermode(modeSrc);

                Rect rect = Tools.getTextRect(s[i], paint);
                if (i == 0) {
                    mCanvas.drawText(s[i], 0, 16, textPaint);
                    mCanvas.drawText(s[i], 0, 16, paint);
                } else {
                    mCanvas.drawText(s[i], (colBmp[i].getWidth() - rect.width()) / 2, 16, textPaint);
                    mCanvas.drawText(s[i], (colBmp[i].getWidth() - rect.width()) / 2, 16, paint);
                }
            }
            isDraw = true;
        }
        onRefresh();
    }
    private String[] getColContent(){
        String[] strings=new String[colBmp.length];
        Arrays.fill(strings,"");

        Scope scope = Scope.getInstance();
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
        double interval = horizontalAxis.getTimeScaleIdVal() * HorizontalAxis.TIME_FS_100;
        double centerPos = interval * horizontalAxis.getTimePoseOfViewPix() / ScopeBase.getHorizonPerGridPixels();
//        if (scope.isInScrollMode()) {
//            centerPos =  interval * ScopeBase.getHorizonGridCnt() / 2;
//        }
//        Logger.d("RulerManage", "centerPos channel= " + centerPos + " ,timePos= " + horizontalAxis.getTimePoseOfViewPix());

        int chActivate=ChannelFactory.getChActivate();
        if (ChannelFactory.isMath_FFT_Ch(chActivate)) {
            MathChannel mathChannel = ChannelFactory.getMathChannel(chActivate);
            interval= mathChannel.getHorizontalAxisMathFFT().fftXScaleIdVal();
            long pos= mathChannel.getHorizontalAxisMathFFT().getXPosOfView();
            centerPos = interval * pos / ScopeBase.getHorizonPerGridPixels();

            for (int i = 0; i < strings.length; i++) {
                long s = DoubleUtil.floor(centerPos + interval * (i - 6));
                if (s<0) {
                    strings[i] = "";
                }else {
                    strings[i] = TBookUtil.getHz3FromHz(s);
                }
            }
        } else if (ChannelFactory.isRefCh(chActivate)) {
            if (ChannelFactory.getRefChannel(chActivate).getRefType() == 2) {
                //getreftype()= 2 = fft
                int id = ChannelFactory.getRefChannel(chActivate).getRefTimeScaleId();
                //档位
                interval = ChannelFactory.getRefChannel(chActivate).getHorizontalAxisRef().fftTimeScaleIdVal(id);
                //位置
                long pos = Math.abs(ChannelFactory.getRefChannel(chActivate).getRefMovPix());
                //中心点位置
                centerPos = interval * pos / ScopeBase.getHorizonPerGridPixels();

                for (int i = 0; i < strings.length; i++) {
                    long s = DoubleUtil.floor(centerPos + interval * (i - 6));
                    if (s < 0) {
                        strings[i] = "";
                    } else {
                        strings[i] = TBookUtil.getHz3FromHz(s);
                    }
                }
            } else {
                RefChannel refChannel = (RefChannel) ChannelFactory.getInstance().getTopRefChannel();
                interval = TBookUtil.getDoubleFromM(handleTimeBase(MainHolderBottom.getCenterTimeBase()).replace("s", "")) * HorizontalAxis.TIME_FS_100;
                long timePos = refChannel.getTimePoseOfViewPix();
                centerPos = interval * timePos / ScopeBase.getHorizonPerGridPixels();
//                if (scope.isInScrollMode()) {
//                    centerPos = interval * ScopeBase.getHorizonGridCnt() / 2;
//                }
                for (int i = 0; i < strings.length; i++) {
                    long s = DoubleUtil.floor(centerPos + interval * (i - 6));
                    strings[i] = TBookUtil.getSFrom100Fs(s);
                }
            }
        }
        else {
            for (int i = 0; i < strings.length; i++) {
                long s = DoubleUtil.floor(centerPos + interval * (i - 6));
                strings[i] = TBookUtil.getSFrom100Fs(s);
            }
        }
        return strings;
    }


    public synchronized void draw(ICanvasGL canvas){
        synchronized (this) {
            if (this.bShow==false) return;
            //Log.d("RulerManage", "1 draw() called with: canvas = [" + canvas + "]");
            canvasGL = canvas;
            if (isDraw) {
                for (int i = 0; i < rowBmp.length; i++)
                    canvas.invalidateTextureContent(rowBmp[i],null);
                for (int i = 0; i < colBmp.length; i++) {
                    canvas.invalidateTextureContent(colBmp[i],null);
                }
                isDraw = false;
            }
            for (int i = 0; i < rowBmp.length; i++) {
                if (i == 0) {
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, 0);
                } else if (i == rowBmp.length - 1) {
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, perVerGridPx * i - rowBmp[i].getHeight());
                } else {
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, perVerGridPx * i - rowBmp[i].getHeight() / 2);
                }
            }
            for (int i = 0; i < colBmp.length; i++) {
                if (i == 0) {
                    canvas.drawBitmap(colBmp[i], 0, perVerGridPx * 10 - colBmp[i].getHeight());
                } else {
                    canvas.drawBitmap(colBmp[i], perHorGridPx * i - colBmp[i].getWidth() / 2, perVerGridPx * 10 - colBmp[i].getHeight());
                }
            }
            //Log.d("RulerManage", "2 draw() called with: canvas = [" + canvas + "]");
        }

    }


    private Consumer<MainCenterMsgChannels> consumerCenterChannelsSelect = new Consumer<MainCenterMsgChannels>() {
        @Override
        public void accept(MainCenterMsgChannels msgChannels) throws Exception {
            drawRowBmp();
            drawColBmp();
//            int curCh=msgChannels.getChNO();
//            if (curCh<IWave.Ch1 || curCh> IWave.R4) return;
//            if (curCh>=IWave.Ch1 && curCh<=IWave.Ch4){
//                drawRowBmp();
//                drawColBmp();
//            }else if(curCh== IWave.Math){
//                drawRowBmp();
//            }else {
//                drawRowBmp();
//            }
        }
    };
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            drawRowBmp();
        }
    };
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            drawColBmp();
        }
    };
    private Consumer<MainWaveMsgTriggerTimeBase> consumerTriggerTimeBase=new Consumer<MainWaveMsgTriggerTimeBase>() {
        @Override
        public void accept(MainWaveMsgTriggerTimeBase mainWaveMsgTriggerTimeBase) throws Exception {
            drawColBmp();
        }
    };

    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);
        if (mqEnum==MQEnum.CH_ACTIVE){
            drawRowBmp();
            drawColBmp();
        }
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_UI_SAMPLE_GRAPH
                    || eventBase.getId()==EventFactory.EVENT_MATH_FFT_SCALE

//                    || eventBase.getId() == EventFactory.EVENT_TIME_SCALE ||
//                    eventBase.getId() == EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH
            ) {
                drawColBmp();
            }else if (eventBase.getId()==EventFactory.EVENT_CHANNEL_POS ||
                    eventBase.getId()==EventFactory.EVENT_MATH_VPOS ||
                    eventBase.getId()==EventFactory.EVENT_REF_VPOS ||
                    eventBase.getId()==EventFactory.EVENT_CHANNEL_VSCALE ||
                    eventBase.getId()==EventFactory.EVENT_CHANNEL_VSCALE_USER
                    || eventBase.getId()==EventFactory.EVENT_PROBE_EVENT
            ){
                drawRowBmp();
            }
//            else if (eventBase.getId()==EventFactory.EVENT_CHANNEL_ACTIVE){
//                drawRowBmp();
//            }
        }
    };


    @Override
    public void switchWorkMode(int workMode) {
        perHorGridPx = GlobalVar.get().getWaveZoneWidth_Pix(workMode) / 12;
        perVerGridPx = GlobalVar.get().getWaveZoneHeight_Pix(workMode) / 10;
    }

    private static String handleTimeBase(String timeBase) {
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase;
        if (timeBase.isEmpty()) {
            timeBase = "1";
        } else {
            timeBase = (TBookUtil.getSFromTime(timeBase.replace(" ", "")) + "").replaceAll(" ", "");
        }
        return timeBase;
    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Exception {
//            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
//            String[] info = colorInfo.split(";");
//            int chIndex = Integer.parseInt(info[0]);
//            String colorStr = info[1];
            drawRowBmp();
        }
    };

}
