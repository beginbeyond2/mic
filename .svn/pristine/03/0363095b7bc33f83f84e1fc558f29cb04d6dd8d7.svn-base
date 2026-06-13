package com.micsig.tbook.tbookscope.wavezone.measure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasureCommon;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by liwb on 2017/5/8.
 * 测量管理
 */

public class MeasureManage implements IWorkMode {

//region  单例

    private static class MeasureManageHolder {
        private static final MeasureManage instance = new MeasureManage();
    }

    public static final MeasureManage getInstance() {
        return MeasureManageHolder.instance;
    }

    //endregion

    //region 属性
    private MeasureItem measureItem;

    public MeasureItem getMeasureItem() {
        return measureItem;
    }

    private FrequencyMeterMeasure frequencyMeterMeasure;

    public FrequencyMeterMeasure getFrequencyMeterMeasure() {
        return frequencyMeterMeasure;
    }

    private AllMeasure allMeasure;

    public AllMeasure getAllMeasure() {
        return allMeasure;
    }

    private CursorMeasureManage cursorMeasureManage;
    private CursorMeasure cursorMeasure;

    public CursorMeasure getCursorMeasure() {
        return cursorMeasure;
    }


    private List<FFTMeasure> fftMeasureList = new ArrayList<>();

    public FFTMeasure getFftMeasure(int mathNumber) {
        return fftMeasureList.get(mathNumber - 1);
    }

//    private FFTMeasure fftMeasure;
//
//    public FFTMeasure getFftMeasure() {
//        return fftMeasure;
//    }

    private SegmentMeasure segmentMeasure;

    public SegmentMeasure getSegmentMeasure() {
        return segmentMeasure;
    }

    private MeasureIndication measureIndication;

    public MeasureIndication getMeasureIndication(){
        return measureIndication;
    }
    private Context context=App.get().getApplicationContext();
    //endregion

    public MeasureManage() {
        measureItem = new MeasureItem();
        frequencyMeterMeasure = new FrequencyMeterMeasure();
        allMeasure = new AllMeasure();
        cursorMeasure = new CursorMeasure();
        cursorMeasureManage = new CursorMeasureManage();
        TChan.foreachMath(mathChan -> {
            fftMeasureList.add(TChan.toMathNumber(mathChan) - 1, new FFTMeasure(TChan.toMathNumber(mathChan)));
        });
        segmentMeasure = new SegmentMeasure();
        measureIndication = new MeasureIndication();


        measureItem.setMeasureItemListene(new MeasureItemListener() {
            @Override
            public void onClick(int chIdx, int measure) {
                updateARM(chIdx);
                measureIndication.setMeasureIndication(chIdx-1,measure + Measure.MeasureType.MEASURE_FIRST);
            }

            private void updateARM(int chIdx){
                int channelIdx=chIdx-1;
                int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS);
                String high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam(channelIdx,thresholdsIdx));
                String middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(channelIdx,thresholdsIdx));
                String low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(channelIdx,thresholdsIdx));
                BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(channelIdx);
                if(channel != null){
                    if(thresholdsIdx == 1){
                        channel.setAbsLower(TBookUtil.getDoubleFromM(low));
                        channel.setAbsUpper(TBookUtil.getDoubleFromM(high));
                        channel.setAbsMiddle(TBookUtil.getDoubleFromM(middle));
                    }else{
                        channel.setLower(Integer.parseInt(low));
                        channel.setUpper(Integer.parseInt(high));
                        channel.setMiddle(Integer.parseInt(middle));
                    }
                    channel.setAbsEnable(thresholdsIdx == 1);
                }
                MeasureService.forceMeasureRefresh();
            }
            private String getSaveThresholdsParam(int chIdx,int thresholdsIdx){
                return ""+thresholdsIdx+chIdx;
            }
        });

        measureItem.setMeasureRowChangeListener(new MeasureRowChangeListener() {
            @Override
            public void measureRowChange(int rowCount) {
                RxBus.getInstance().post(RxEnum.MQ_MSG_MEASURE_ROW_COUNT, rowCount);
            }
        });
    }

    public void init() {
    }

    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;
        if (ChannelFactory.isDynamicCh(chId)) {
            baseChannel = ChannelFactory.getDynamicChannel(chId);
        } else if (ChannelFactory.isMathCh(chId)) {
            baseChannel = ChannelFactory.getMathChannel(chId);
        } else if (ChannelFactory.isRefCh(chId)) {
            baseChannel = ChannelFactory.getRefChannel(chId);
        }
        if (baseChannel != null) {
            return baseChannel.getMeasure();
        }
        return null;
    }

    public void measureStaticReset() {
        for (int i = 0; i < MeasureManage.getInstance().getMeasureItem().getValidMeasureList().size(); i++) {
            MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().getMeasureItem().getValidMeasureList().get(i);
            int iWaveCh = item.getChannelId();
            int measureId = item.getMeasureId();
            String measureName = item.getMeasureName();
            Measure measure = getHardwareMeasure(iWaveCh - 1);
            measure.getMeasureStatics().reset();
        }
    }


    public void draw(Canvas canvas) {
        measureItem.draw(canvas);
        frequencyMeterMeasure.draw(canvas);
        allMeasure.draw(canvas);
//        cursorMeasureManage.draw();
//        cursorMeasure.draw(canvas);
    }

    public void draw(ICanvasGL canvas) {
        measureIndication.draw(canvas);
        measureItem.draw(canvas);
        frequencyMeterMeasure.draw(canvas);
        allMeasure.draw(canvas);
//        cursorMeasure.draw(canvas);
        cursorMeasureManage.draw(canvas);
//        fftMeasure.draw(canvas);
        fftMeasureList.forEach(fftMeasure -> fftMeasure.draw(canvas));
//        segmentMeasure.draw(canvas);
    }

    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        //if (workMode == mWorkMode) return;
        this.mWorkMode = workMode;
        measureItem.switchWorkMode(workMode);
        measureIndication.switchWorkMode(workMode);
        frequencyMeterMeasure.switchWorkMode(workMode);
        allMeasure.switchWorkMode(workMode);
        cursorMeasureManage.switchWorkMode(workMode);
//        fftMeasure.switchWorkMode(workMode);
        fftMeasureList.forEach(fftMeasure -> fftMeasure.switchWorkMode(workMode));
//        segmentMeasure.switchWorkMode(workMode);
    }

    //endregion

    public void setCursorChannelColor(int ChNo) {
        cursorMeasureManage.setCursorChannelColor(ChNo);
    }
    public void setAllMeasureChannelColor(int ChNo) {
        allMeasure.setChannelId(ChNo);
    }
    public void setRowVisible(boolean rowVisible) {
        cursorMeasureManage.setRowVisible(rowVisible);
    }


    public void setColVisible(boolean colVisible) {
        cursorMeasureManage.setColVisible(colVisible);
    }

    public void setRowVisible(int workMode, boolean rowVisible) {
        cursorMeasureManage.setRowVisible(workMode, rowVisible);
    }

    public void setColVisible(int workMode, boolean colVisible) {
        cursorMeasureManage.setColVisible(workMode, colVisible);
    }

    /****
     * 绘制接口
     *
     */
    public interface IMeasure {
        //region 常量
        public static final int MeasureId_Period = Measure.MeasureType.MEASURE_PERIOD - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Freq = Measure.MeasureType.MEASURE_FREQ - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_RiseTime = Measure.MeasureType.MEASURE_RISETIME - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_FallTime = Measure.MeasureType.MEASURE_FALLTIME - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Delay = Measure.MeasureType.MEASURE_DELAY - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_DutyAdd = Measure.MeasureType.MEASURE_POSITIVE_DUTY - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_DutySub = Measure.MeasureType.MEASURE_NEGATIVE_DUTY - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_WidthAdd = Measure.MeasureType.MEASURE_POSITIVE_PULSE_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_WidthSub = Measure.MeasureType.MEASURE_NEGATIVE_PULSE_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_BurstW = Measure.MeasureType.MEASURE_BURST_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_ROV = Measure.MeasureType.MEASURE_POSITIVE_OVERSHOOT - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_FOV = Measure.MeasureType.MEASURE_NEGATIVE_OVERSHOOT - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Phase = Measure.MeasureType.MEASURE_PHASE - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_PKPK = Measure.MeasureType.MEASURE_PK_PK - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Amp = Measure.MeasureType.MEASURE_AMPLITUDE - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_High = Measure.MeasureType.MEASURE_HIGH - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Low = Measure.MeasureType.MEASURE_LOW - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Max = Measure.MeasureType.MEASURE_MAX - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Min = Measure.MeasureType.MEASURE_MIN - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_RMS = Measure.MeasureType.MEASURE_RMS - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_CRMS = Measure.MeasureType.MEASURE_CRMS - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_Mean = Measure.MeasureType.MEASURE_MEAN - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_CMean = Measure.MeasureType.MEASURE_CMEAN - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_ACRMS = Measure.MeasureType.MEASURE_AC_RMS - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_PostitiveRate = Measure.MeasureType.MEASURE_POSITIVE_RATE - Measure.MeasureType.MEASURE_FIRST;
        public static final int MeasureId_NegativeRate = Measure.MeasureType.MEASURE_NEGATIVE_RATE - Measure.MeasureType.MEASURE_FIRST;

        public static final int MeasureId_TVALUE = Measure.MeasureType.MEASURE_TVALUE - Measure.MeasureType.MEASURE_FIRST;

        public static final int MeasureId_ColValQ = Measure.MeasureType.MEASURE_COLV - Measure.MeasureType.MEASURE_FIRST;

        public static final int MeasureId_Cursor_X1 = Measure.MeasureType.MEASURE_CURSOR_X1 - Measure.MeasureType.MEASURE_FIRST;

        public static final int MeasureId_Cursor_X2 = Measure.MeasureType.MEASURE_CURSOR_X2 - Measure.MeasureType.MEASURE_FIRST;



        //endregion

        void draw(Canvas canvas);

        void draw(ICanvasGL canvas);
    }

    /***
     * 光标参数结构
     */
    public class CursorMeasureStruct {
        public String row1;
        public String row2;
        public String deltaRow;

        public String col1;
        public String col2;
        public String deltaCol;
        public String deltaTCol;

        public String S;
    }

    /***
     * 光标测量类 移到CurosrMeasureBase中，分出YT和XY模式
     */
    public class CursorMeasure implements IMeasure, IWorkMode {

        //region  属性
        private boolean rowCursorVisible = true;
        private boolean colCursorVisible = true;
        private int channelId = 1;

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
            draw();
        }

        public boolean isRowCursorVisible() {
            return rowCursorVisible;
        }

        public void setRowCursorVisible(boolean rowCursorVisible) {
            this.rowCursorVisible = rowCursorVisible;
            draw();
        }

        public boolean isColCursorVisible() {
            return colCursorVisible;
        }

        public void setColCursorVisible(boolean colCursorVisible) {
            this.colCursorVisible = colCursorVisible;
            draw();
        }

        //endregion
        private CursorMeasureStruct param = null;

        private int showX = 550, showY = 50;
        private Paint p;
        private Bitmap bmp;
        private Canvas mCanvas;
        private boolean isChanageBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        public CursorMeasure() {
            param = new CursorMeasureStruct();
            bmp = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setTextSize(20);
            p.setAntiAlias(true);
            draw();
        }

        //region IWorkMode 接口
        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureCursorPosition(workMode);
            showX = p.x;
            showY = p.y;
        }


        //endregion

        @Override
        public void draw(Canvas canvas) {
            if (colCursorVisible || rowCursorVisible) {
                synchronized (bmp) {
                    canvas.drawBitmap(bmp, showX, showY, null);
                }
            }
        }

        @Override
        public void draw(ICanvasGL canvas) {
            if (colCursorVisible || rowCursorVisible) {
                synchronized (bmp) {
                    canvasGL = canvas;
                    if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
                    canvas.drawBitmap(bmp, showX, showY);
                    isChanageBitmap = false;
                }
            }
        }

        //region 显示
        private void draw() {
            synchronized (bmp) {
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mCanvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                if (TChan.isRef(channelId)) {
                    p.setColor(TChan.getChannelColor(context,TChan.RefActive));
                } else {
                    p.setColor(TChan.getChannelColor(context,channelId));
                }
                if (rowCursorVisible && colCursorVisible) {
                    int y = drawRow();
                    y = drawCol(y);
                    drawS(y);

                } else if (rowCursorVisible) {
                    drawRow();
                } else if (colCursorVisible) {
                    drawCol(0);
                }
                isChanageBitmap = true;
                onRefresh();
            }
        }

        private int drawRow() {
            int x = 0, y = 0;
            String text = "Y1:" + String.valueOf(param.row1);
            x = 0;
            y = getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            text = "Y2:" + String.valueOf(param.row2);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            text = "ΔY:" + String.valueOf(param.deltaRow);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            return y;
        }

        private int drawCol(int y) {
            int x;
            String text = "X1:" + String.valueOf(param.col1);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            text = "X2:" + String.valueOf(param.col2);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            text = "ΔX:" + String.valueOf(param.deltaCol);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            text = "1/ΔX:" + String.valueOf(param.deltaTCol);
            x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);

            return y;
        }

        private void drawS(int y) {
            String text = "S:" + String.valueOf(param.S);
            int x = 0;
            y = y + getTextHeight(text) + 5;
            mCanvas.drawText(text, x, y, p);
        }

        private int getTextHeight(String text) {
            Rect rect = new Rect();
            p.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width();
            int h = rect.height();
            return h;
        }
        //endregion

        public void setParam(String row1, String row2, String deltaRow, String col1, String col2, String deltaCol,
                             String deltaTCol, String S) {
            cursorMeasureManage.setParam(row1, row2, deltaRow, col1, col2, deltaCol, deltaTCol, S);
        }
    }

    public class MeasureIndication implements IMeasure, IWorkMode{

        public static final int MEASURE_INDICATION_LEFT     = 0;
        public static final int MEASURE_INDICATION_TOP      = 1;
        public static final int MEASURE_INDICATION_RIGHT    = 2;
        public static final int MEASURE_INDICATION_BOTTOM   = 3;
        public static final int MEASURE_INDICATION_MAX = 4;
        private int [] pos = new int[MEASURE_INDICATION_MAX];
        private Bitmap [] bmp = new Bitmap[MEASURE_INDICATION_MAX];

        private Bitmap [] oldBmp = new Bitmap[MEASURE_INDICATION_MAX];
        private Canvas mCanvas;
        private boolean [] visiable = new boolean[MEASURE_INDICATION_MAX];

        private boolean bEnable = false;
        private boolean isChanageBitmap = false;

        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int chIdx = -1;
        private int measureId = -1;

        public synchronized void setMeasureIndication(int chIdx,int measureId){
            this.chIdx = chIdx;
            this.measureId = measureId;
            MeasureService.forceMeasureRefresh();
        }
        public synchronized int getChIdx(){
            return chIdx;
        }
        public synchronized int getMeasureId(){
            return measureId;
        }
        public boolean isVisiable(){
            return visiable[MEASURE_INDICATION_LEFT]
                    || visiable[MEASURE_INDICATION_TOP]
                    || visiable[MEASURE_INDICATION_RIGHT]
                    || visiable[MEASURE_INDICATION_BOTTOM];
        }
        public synchronized void setVisiable(boolean bVisiable){

            visiable[MEASURE_INDICATION_LEFT]
                    = visiable[MEASURE_INDICATION_TOP]
                    = visiable[MEASURE_INDICATION_RIGHT]
                    = visiable[MEASURE_INDICATION_BOTTOM] = bVisiable;
            onRefresh();
        }
        public synchronized void setVisiable(int idx,boolean bVisiable){

            visiable[idx] = bVisiable;
            onRefresh();
        }

        public synchronized void setPos(int idx, int pos) {
            pos = (int) Math.round(pos * ScopeBase.getToFPGACoff());
            this.pos[idx] = pos;
        }

        public synchronized boolean isEnable() {
            return bEnable;
        }

        public synchronized void setEnable(boolean bEnable) {
            this.bEnable = bEnable;
            onRefresh();
        }

        public MeasureIndication(){
            initLine();
            isChanageBitmap = true;
            onRefresh();
        }

        private void initLine() {
            int m = WorkModeManage.getInstance().getmWorkMode();
            int sw = GlobalVar.get().getWaveZoneWidth_Pix(m);
            int sh = GlobalVar.get().getWaveZoneHeight_Pix(m);
            System.arraycopy(bmp,0,oldBmp,0, bmp.length);
            bmp[MEASURE_INDICATION_LEFT] = Bitmap.createBitmap(3, sh, Bitmap.Config.ARGB_8888); //left
            bmp[MEASURE_INDICATION_TOP] = Bitmap.createBitmap(sw, 3, Bitmap.Config.ARGB_8888); // top
            bmp[MEASURE_INDICATION_RIGHT] = Bitmap.createBitmap(3, sh, Bitmap.Config.ARGB_8888);   //right
            bmp[MEASURE_INDICATION_BOTTOM] = Bitmap.createBitmap(sw, 3, Bitmap.Config.ARGB_8888); //bottom
            drawLine( bmp[MEASURE_INDICATION_LEFT],1,0,1,sh - 1);
            drawLine( bmp[MEASURE_INDICATION_TOP],0,1,sw - 1, 1);
            drawLine( bmp[MEASURE_INDICATION_RIGHT],1,0,1,sh - 1);
            drawLine( bmp[MEASURE_INDICATION_BOTTOM],0,1,sw - 1, 1);
        }

        public void changeLineHeight() {
            initLine();
        }


        private void drawLine(Bitmap bmp,int x1,int y1,int x2,int y2){
            Paint p = new Paint();

            Canvas canvas = new Canvas(bmp);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(p);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            p.setStrokeWidth(1);
            p.setColor(Color.LTGRAY);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{20, 10}, 0);//设置虚线模式： 实现20 空白10 重复
            p.setPathEffect(dashPathEffect);
            canvas.drawLine(x1,y1,x2,y2,p);
        }

        @Override
        public void switchWorkMode(int workMode) {
            initLine();
        }

        @Override
        public void draw(Canvas canvas) {

        }

        @Override
        public void draw(ICanvasGL canvas) {

            synchronized (this) {
                canvasGL = canvas;
                if ( isEnable() && isVisiable()) {
                    if (isChanageBitmap) {
                        for (int i = 0; i < bmp.length; i++) {
                            canvas.invalidateTextureContent(bmp[i],oldBmp[i]);
                        }
                    }

                    if(visiable[MEASURE_INDICATION_LEFT]) {
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_LEFT], pos[MEASURE_INDICATION_LEFT] - 1, 0);
                    }
                    if(visiable[MEASURE_INDICATION_TOP]) {
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_TOP], 0, (int) Math.round(ScopeBase.changeAccuracy(pos[MEASURE_INDICATION_TOP] * ScopeBase.getToUICoff())) - 1);
                    }
                    if(visiable[MEASURE_INDICATION_RIGHT]) {
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_RIGHT], pos[MEASURE_INDICATION_RIGHT] - 1, 0);
                    }
                    if(visiable[MEASURE_INDICATION_BOTTOM]) {
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_BOTTOM], 0, (int) Math.round(ScopeBase.changeAccuracy(pos[MEASURE_INDICATION_BOTTOM] * ScopeBase.getToUICoff())) - 1);
                    }

                }
                isChanageBitmap = false;

            }
        }
    }

    /***
     * 所有测量
     */
    public class AllMeasure implements IMeasure, IWorkMode {

        //region 属性
        private boolean visible = false;

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            draw();
        }
        //endregion

        private List<MeasureItemStruct> measureList = new ArrayList<>();
        private Bitmap bmp;
        private Canvas mCanvas;
        private Paint p;
        private TextPaint textPaint;
        private boolean isChanageBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int showX = 0, showY = 415;
        private int ItemInterval = 140;

        public AllMeasure() {
            bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 200, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setTextSize(20);
            p.setAntiAlias(true);
            p.setStrokeWidth(1);

            textPaint=new TextPaint();
            textPaint.setTextSize(20);
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.BLACK);
            textPaint.setStrokeWidth(4);
            textPaint.setStyle(Paint.Style.STROKE);

            int[] color = new int[]{
                    Color.argb(20, 0, 160, 160),
                    Color.argb(100, 0, 255, 255),
                    Color.argb(20, 0, 160, 160)};
            linearGradient = new LinearGradient(0, 0, bmp.getWidth(), 0, color, null, Shader.TileMode.CLAMP);
            iniParam();
            Point p = GlobalVar.get().getMeasureAllPosition(WorkModeManage.getInstance().getmWorkMode());
            showX = p.x;
            showY = p.y;
            draw();
            ItemInterval = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) / 5;
        }

        //region IWorkMode接口

        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            updateShowXY();
        }

        //endregion

        private void updateShowXY() {
            Point p = GlobalVar.get().getMeasureAllPosition(WorkModeManage.getInstance().getmWorkMode());
            int rowCount = MeasureManage.getInstance().getMeasureItem().getMeasureRowCount();
            if (p == null) return;
            showX = p.x;
            switch (rowCount) {
                case 0:
                    showY = 890;
                    break;
                case 1:
                    showY = 860;
                    break;
                case 2:
                    showY = 830;
                    break;
                case 3:
                    showY = 800;
                    break;
                case 4:
                    showY = 770;
                    break;
                default:
                    showY = p.y;
                    break;
            }
            if(WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
                showY = Math.round((showY - 35) * GlobalVar.get().toZoomCoef()) - 10;
            }
            boolean isShowMeasureStatic = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            if (isShowMeasureStatic) {
//                    boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
                boolean mean = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN);
                boolean max = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX);
                boolean min = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN);
                boolean delta = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA);
                boolean count = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT);
                int staticNum = 0;
                if (mean) staticNum++;
                if (max) staticNum++;
                if (min) staticNum++;
                if (delta) staticNum++;
                if (count) staticNum++;
                showY = showY - 35 * Math.max(0, staticNum - (rowCount - 2));
                if (rowCount == 0) {
                    showY += 55;
                }
            }
        }

        @Override
        public void draw(Canvas canvas) {
            synchronized (bmp) {
                if (isVisible() == true) {
                    updateShowXY();
                    canvas.drawBitmap(bmp, showX, showY, null);
                }
            }
        }

        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {
                canvasGL = canvas;
                if (isVisible() == true) {
                    if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
                    updateShowXY();
                    canvas.drawBitmap(bmp, showX, showY);
                }
                isChanageBitmap = false;
            }
        }

        private volatile int chIdx = 0;

        //region  对外接口
        public void setChannelId(int channelId) {
            for (MeasureItemStruct c : measureList) {
                c.channelId = channelId;
            }
            draw();
            chIdx = channelId;
        }

        public int getChIdx() {
            return chIdx;
        }

        /**
         * TODO
         * 延时、相位没有AllMeasure值
         */
        public void setMeasureData(List<String> list) {
            if (list == null || measureList.size() != list.size()) {
                return;
            }
            for (int i = 0; i < measureList.size(); i++) {
                measureList.get(i).data = list.get(i);
            }
            draw();
        }
        //endregion

        //region 显示接口
        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        private LinearGradient linearGradient;

        private void draw() {
            synchronized (bmp) {
                p.setXfermode(clearMode);
                mCanvas.drawPaint(p);
                p.setXfermode(srcMode);
                p.setShader(linearGradient);
                p.setAntiAlias(false);
                mCanvas.drawLine(0, 0, bmp.getWidth(), 0, p);
                mCanvas.drawLine(0, 114, bmp.getWidth(), 114, p);
                p.setShader(null);
                p.setAntiAlias(true);
                for (int i = 0; i < measureList.size(); i++) {
                    MeasureItemStruct c = measureList.get(i);
//                    if (TChan.isRef(c.getChannelId())) {
//                        p.setColor(TChan.getChannelColor(context,TChan.RefActive));
//                    } else {
                        p.setColor(TChan.getChannelColor(context, c.channelId));
//                    }
                    String text = c.measureName + ":";
                    if (TChan.isSerial(c.getChannelId())) {
                        text += "----";
                    } else {
                        text += c.data;
                    }
                    int x, y, h;
//                    h = getTextHeight(text);
//                    x = i * ItemInterval % GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
                    x = i % 5 * 340;
//                    y = i * 140 / GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) * (bmp.getHeight() / 7) + h;
                    y = i / 5 * 22 + 21;
                    mCanvas.drawText(text, x + 100, y, textPaint);
                    mCanvas.drawText(text, x + 100, y, p);
                }
                isChanageBitmap = true;
                onRefresh();
            }
        }

        private Rect rect = new Rect();

        private int getTextHeight(String text) {

            p.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width();
            int h = rect.height();
            return h;
        }
        //endregion

        //region 私有
        private void iniParam() {
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Period, getMeasureIdToName(IMeasure.MeasureId_Period), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Freq, getMeasureIdToName(IMeasure.MeasureId_Freq), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_RiseTime, getMeasureIdToName(IMeasure.MeasureId_RiseTime), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_FallTime, getMeasureIdToName(IMeasure.MeasureId_FallTime), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_DutyAdd, getMeasureIdToName(IMeasure.MeasureId_DutyAdd), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_DutySub, getMeasureIdToName(IMeasure.MeasureId_DutySub), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_WidthAdd, getMeasureIdToName(IMeasure.MeasureId_WidthAdd), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_WidthSub, getMeasureIdToName(IMeasure.MeasureId_WidthSub), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_BurstW, getMeasureIdToName(IMeasure.MeasureId_BurstW), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_ROV, getMeasureIdToName(IMeasure.MeasureId_ROV), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_FOV, getMeasureIdToName(IMeasure.MeasureId_FOV), TopLayoutMeasureCommon.MEASURE_DATA_INIT));

            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_PKPK, getMeasureIdToName(IMeasure.MeasureId_PKPK), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Amp, getMeasureIdToName(IMeasure.MeasureId_Amp), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_High, getMeasureIdToName(IMeasure.MeasureId_High), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Low, getMeasureIdToName(IMeasure.MeasureId_Low), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Max, getMeasureIdToName(IMeasure.MeasureId_Max), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Min, getMeasureIdToName(IMeasure.MeasureId_Min), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_RMS, getMeasureIdToName(IMeasure.MeasureId_RMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_CRMS, getMeasureIdToName(IMeasure.MeasureId_CRMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Mean, getMeasureIdToName(IMeasure.MeasureId_Mean), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_CMean, getMeasureIdToName(IMeasure.MeasureId_CMean), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_ACRMS, getMeasureIdToName(IMeasure.MeasureId_ACRMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_PostitiveRate, getMeasureIdToName(IMeasure.MeasureId_PostitiveRate), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_NegativeRate, getMeasureIdToName(IMeasure.MeasureId_NegativeRate), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
//            addMeasureItem(new MeasureItemStruct(TChan.Ch1, IMeasure.MeasureId_TVALUE, getMeasureIdToName(IMeasure.MeasureId_TVALUE), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
        }

        private boolean addMeasureItem(MeasureItemStruct item) {
            measureList.add(item);
            return true;
        }

        private String getMeasureIdToName(int MeasureId) {
            String[] s = App.get().getResources().getStringArray(R.array.measures);
            return s[MeasureId];

        }
        //endregion
    }


    /***
     *
     * 频率计 测量显示
     *
     */
    public class FrequencyMeterMeasure implements IMeasure, IWorkMode {

        //region 属性
        private int channelId = 1;
        //        private float data = 0;
        private String data = "";
        private boolean visible = false;

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
            draw();
        }

        public void setData(String data) {
            this.data = data;
            draw();
        }

        public String getData() {
            return data;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            draw();
        }

        public boolean getVisible() {
            return visible;
        }
        //endregion

        private Bitmap bmp;
        private Canvas mCanvas;
        private Paint p;
        private boolean isChangeBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int showX = 30, showY = 30;

        public FrequencyMeterMeasure() {
            bmp = Bitmap.createBitmap(200, 50, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setTextSize(20);
            p.setAntiAlias(true);
            switchWorkMode(WorkModeManage.getInstance().getmWorkMode());
            draw();
        }

        //region IWorkMode接口

        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureFrequencyMeterPosition(workMode);
            if (p == null) {
                showX = 1000;
                showY = 0;
                return;
            }
            showX = p.x;
            showY = p.y;


        }


        //endregion


        @Override
        public void draw(Canvas canvas) {
            synchronized (bmp) {
                if (visible) canvas.drawBitmap(bmp, showX, showY, null);
            }
        }

        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {
                canvasGL = canvas;
                if (visible) {
                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null);
                    canvas.drawBitmap(bmp, showX, showY);
                }
                isChangeBitmap = false;
            }
        }

        //region 显示
        public void draw() {
            synchronized (bmp) {
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mCanvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                if (TChan.isRef(channelId)) {
                    p.setColor(TChan.getChannelColor(context,TChan.RefActive));
                } else {
                    p.setColor(TChan.getChannelColor(context,channelId));
                }
                String channelName = TChan.getChannelName(channelId);
                if (Objects.equals(channelName, "")) return;
                String text;
                if (TextUtils.isEmpty(data)) {
                    text = channelName + ": " + "---.--- Hz";
                } else
                    text = channelName + ": " + data; /*String.valueOf(data) + "Hz"*/
                ;

                int x, y, h;
                h = getTextHeight(text);
                x = 0;
                y = h;
                mCanvas.drawText(text, x, y, p);

                isChangeBitmap = true;
                onRefresh();

            }
        }

        private int getTextHeight(String text) {
            Rect rect = new Rect();
            p.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width();
            int h = rect.height();
            return h;
        }
        //endregion

        //region 界面接口

        //endregion

    }

    public interface MeasureItemListener{
        void onClick(int chIdx,int measure);
    }

    public interface MeasureRowChangeListener {
        void measureRowChange(int rowCount);
    }

    public static String getEnclosedNumber(int number){
        if(number >= 1 && number <= 20){
            number += 0x245F;
        }else if(number >= 21 && number <= 35){
            number += 0x323C;
        }else if(number >= 36 && number <= 40){
            number += 0x328D;
        }else{
            return "";
        }
        return new String(Character.toChars(number));
    }

    /***
     * 测量显示类
     */
    public class MeasureItem implements IMeasure, IWorkMode {

        private List<MeasureItemStruct> measureList = new ArrayList<>();

        public List<MeasureItemStruct> getMeasureList() {
            return measureList;
        }

        public List<MeasureItemStruct> getValidMeasureList() {
            List<MeasureItemStruct> tempList = new ArrayList<>();
            for (MeasureItemStruct itemStruct : measureList) {
                if (itemStruct.getChannelId() > 0) {
                    tempList.add(itemStruct);
                }
            }
            return tempList;
        }

        public int getMeasureRowCount() {
            int perRowCount = GlobalVar.get().getMeasureItemPerRowCount();//每行数量
            int measureCount = measureList.size();
            return measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
        }

        private Bitmap bmp;
        private Canvas mCanvas;
        private Paint p;
        private boolean isChanageBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int showX = 0, showY = 620;
        private int ItemInterval = 140;
        public Consumer<MeasureItemStruct> OnClickEvent=this::onClickEvent;
        public Consumer<Boolean> OnRefresh;

        private MeasureItemListener itemListener;
        private MeasureRowChangeListener rowChangeListener;
        private boolean SelectEnable=false;
        private boolean visible=false;
        public void setMeasureItemListene(MeasureItemListener listener){
            this.itemListener = listener;
        }
        public void setMeasureRowChangeListener(MeasureRowChangeListener listener) {
            this.rowChangeListener = listener;
        }

        public MeasureItem() {
            bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 100, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setTextSize(20);
            p.setAntiAlias(true);

            Point p = GlobalVar.get().getMeasureItemPosition(WorkModeManage.getInstance().getmWorkMode());
            showX = p.x;
            showY = p.y;
            ItemInterval = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) / 5;
        }

        private void onClickEvent(MeasureItemStruct c){

            if(itemListener != null){
                itemListener.onClick(c.channelId,c.measureId);
            }
        }
        //region IWorkMode接口

        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureItemPosition(workMode);
            if (p == null) {
                showX = 1000;
                showY = 0;
                return;
            }
            showX = p.x;
            showY = p.y;
        }

        //endregion


        //region 显示接口
        @Override
        public void draw(Canvas canvas) {
            if (isShow()) return;
            synchronized (bmp) {
                canvas.drawBitmap(bmp, showX, showY, null);
            }
        }

        @Override
        public void draw(ICanvasGL canvas) {
            if (isShow()) return;
            if (visible==false) return;
            synchronized (bmp) {
                canvasGL = canvas;
                if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
                canvas.drawBitmap(bmp, showX, showY);
                isChanageBitmap = false;
            }
        }

        private boolean isShow() {
            boolean isShowMeasureStatic = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            return isShowMeasureStatic;
        }
        private boolean isMeasureItemClickEnable(){
            return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR);
        }

        public void setSelectEnable(boolean state){
            this.SelectEnable=state;
            draw();
        }
        public boolean isSelectEnable(){
            return this.SelectEnable;
        }
        public boolean isExistClickSelected(){
            for(int i=0;i<measureList.size();i++){
                MeasureItemStruct c= measureList.get(i);
                if (c.isSelected && c.isClickSelected){
                    return true;
                }
            }
            return false;
        }
        public void CancelAllSelected(){
            for(int i=0;i<measureList.size();i++){
                MeasureItemStruct c= measureList.get(i);
                c.isSelected=false;
            }
        }



        public void dealCursorItem(MotionEvent event){

            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                     int x=(int)event.getX();
                     int y= (int)event.getY()-showY;

                     MeasureItemStruct tem=null;
                     boolean isChangeSelect=false;
                     for(int i=0;i<measureList.size();i++){
                         MeasureItemStruct c= measureList.get(i);
                         if (c.isSelected) tem=c;
                         if (c.rect.contains(x,y) && isMeasureItemClickEnable()){
                             c.isSelected=true;
                             c.isClickSelected=true;
                             isChangeSelect=true;
                             if ((OnClickEvent!=null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
                                 OnClickEvent.accept(c);
                             }
                         }else {
                             c.isSelected=false;
                         }
                     }
                     if (isChangeSelect==false && tem!=null){
                         tem.isSelected=true;
                     }else {
                         draw();
                     }

                    break;
            }
        }
        public boolean setSelectItem(int idx){
            if (idx<0) return false;

            MeasureItemStruct tem=null;
            boolean isChangeSelect=false;
            boolean isSelectItem=false;
            boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
            List<MeasureItemStruct> validMeasureList = null;
            if (isShowDialog) {
                validMeasureList = getValidMeasureList();
            } else {
                validMeasureList = measureList;
            }
            for (int i = 0; i < validMeasureList.size(); i++) {
                MeasureItemStruct c = validMeasureList.get(i);
                if (c.isSelected) tem=c;
                if (idx==i && isMeasureItemClickEnable()){
                    c.isSelected=true;
                    c.isClickSelected=true;
                    isChangeSelect=true;
                    if ((OnClickEvent!=null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
                        OnClickEvent.accept(c);
                        isSelectItem=true;
                    }
                }else {
                    c.isSelected=false;
                    c.isClickSelected=false;
                }
                if (isChangeSelect==false && tem!=null){
                    tem.isClickSelected=true;
                }else {
                    draw();
                }

            }

           return isSelectItem;
        }

        public MeasureItemStruct getSelectItem() {
            List<MeasureItemStruct> validMeasureList = getValidMeasureList();
            if (validMeasureList == null || validMeasureList.size() == 0) return null;
            for (int i = 0; i < validMeasureList.size(); i++) {
                MeasureItemStruct c = validMeasureList.get(i);
                if (c.isSelected) {
                    return c;
                }
            }

            return validMeasureList.get(validMeasureList.size() - 1);
        }



        private void draw() {
            synchronized (bmp) {
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mCanvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                if (measureList.size() > 0) {
                    int h = getTextHeight(measureList.get(0).measureName) + 3;
                    int x, y;
                    for (int i = 0; i < measureList.size(); i++) {
                        MeasureItemStruct c = measureList.get(i);

                        String text = c.measureName + ":" + c.data;

//                        x = i * ItemInterval % GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
                        x = i % 5 * 340+100;
//                        y = i * ItemInterval / GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) * (bmp.getHeight() / 5) + h;
                        y = i / 5 * 30 + 18;
                        if (c.isSelected && SelectEnable){
                            text="["+text+"]";
                            p.setColor(Color.WHITE);
                            int w=  getTextWidth(text);
                            mCanvas.drawLine(x,y+5,x+w,y+5,p);
                        }
                        if (TChan.isRef(c.getChannelId())) {
                            p.setColor(TChan.getChannelColor(context,TChan.RefActive));
                        } else {
                            p.setColor(TChan.getChannelColor(context, c.channelId));
                        }
                        mCanvas.drawText(text, x , y, p);
                        c.rect.set(x,y-18,x+200, y-18+30);
                    }
                }
                isChanageBitmap = true;
                onRefresh();
                if (OnRefresh!=null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        OnRefresh.accept(isChanageBitmap);
                    }
                }
//                for(int i=0;i<measureList.size();i++){
//                    Log.d("Tag.Debug", String.format("draw: %s",measureList.get(i).toString() ));
//                }
            }
        }


        private int getTextHeight(String text) {
            Rect rect = new Rect();
            p.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width();
            int h = rect.height();
            return h;
        }
        private int getTextWidth(String text) {
            Rect rect = new Rect();
            p.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width();
            int h = rect.height();
            return w;
        }
        //endregion

        //region 最外层接口

        /***
         * 添加测量项目
         * @param item
         * @return -1不成功
         */
        public boolean addMeasureItem(MeasureItemStruct item) {
            if (getValidMeasureList().size() >= GlobalVar.get().getMeasureItemCount()) return false;
            if (isExist(item)) {

                if (!isExistClickSelected()){
                    CancelAllSelected();
                    item.isSelected=true;
                }

                boolean addInEmptyPos = false;
                ListIterator<MeasureItemStruct> iterator = measureList.listIterator();
                while (iterator.hasNext()) {
                    MeasureItemStruct itemStruct = iterator.next();
                    if (itemStruct.getChannelId() < 0) {
                        iterator.remove();
                        iterator.add(item);
                        addInEmptyPos = true;
                        break;
                    }
                }
                if(!addInEmptyPos) {
                    measureList.add(item);
                }
                clearRowEmpty();
                draw();
                return true;
            }
            return false;
        }


        /**
         * 指定位置添加空占位item
         * @param position
         * @param item
         * @return
         */
        public boolean addEmptyMeasureItem(int position, MeasureItemStruct item) {
            if (position >= measureList.size()) {
                measureList.add(item);
            } else {
                measureList.add(position, item);
            }

            if (measureList.size() > GlobalVar.get().getMeasureItemCount()) {
                measureList.remove(position);
                return false;
            }
//            clearRowEmpty();
            draw();
            return true;
        }


        public MeasureItemStruct getMeasureItemStruct(int channelId, int measureId) {
            MeasureItemStruct measureItemStruct = null;
            for (MeasureItemStruct itemStruct : measureList) {
                if (itemStruct.getChannelId() == channelId && itemStruct.getMeasureId() == measureId) {
                    measureItemStruct = itemStruct;
                    break;
                }
            }
            return measureItemStruct;
        }



        public boolean updateMeasureItem(MeasureItemStruct item) {
            if (!isExist(item)) {
                for (MeasureItemStruct itemStruct : measureList) {
                    if (itemStruct.channelId == item.channelId && itemStruct.measureId == item.measureId) {
                        itemStruct.data = item.data;
                    }
                }
                draw();
                return true;
            }
            return false;
        }

        /***
         * 删除测量项目
         * @param index
         * @return
         */
        public boolean delMeasureItem(int index) {
            if (measureList.size() > index) {
                if (measureList.get(index).isSelected && measureList.size()>=2){
                    measureList.remove(index);
                    MeasureItemStruct m= measureList.get(measureList.size()-1);
                    m.isSelected=true;
                }else {
                  measureList.remove(index);
                }
                clearRowEmpty();
                draw();
                return true;
            }
            return false;
        }

        public boolean delAllMeasureItem() {
            for (int i = measureList.size() - 1; i >= 0; i--) {
                measureList.remove(i);
            }
            clearRowEmpty();
            draw();
            return true;
        }

        public boolean delMeasureItem(int channelId, int measureId) {
            for (int i = 0; i < measureList.size(); i++) {
                MeasureItemStruct c = measureList.get(i);
                if (c.channelId == channelId && c.measureId == measureId) {
//                    measureList.remove(i);
                    MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(-1,-1, -1, "", "");
                    measureList.set(i, item);
                    clearRowEmpty();
                    setLastValidItemSelect(c);
                    draw();
                    return true;
                }
            }
            return false;
        }

        private void setLastValidItemSelect(MeasureItemStruct c) {
            if (!c.isSelected || measureList.size() <= 0) return;
            for (int i = measureList.size() - 1; i >= 0; i--) {
                MeasureItemStruct item = measureList.get(i);
                if (item.getChannelId() < 0) continue;
                item.isSelected = true;
                break;
            }
        }

        /***
         * 是否存在
         * @return true不存在
         */
        private boolean isExist(MeasureItemStruct item) {
            for (MeasureItemStruct c : measureList) {
                if (c.channelId == item.channelId && c.measureId == item.measureId)
                    return false;
            }
            return true;
        }

        /**
         * 若一行都为空，则删除此行空数据
         */
        public void clearRowEmpty() {
            List<MeasureItemStruct> newList = new ArrayList<>();
            int perRowCount = GlobalVar.get().getMeasureItemPerRowCount();//每行数量
            int measureCount = measureList.size();
            int measureRowCount = measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
            for (int i = 0; i < measureCount; i += perRowCount) {
                int end = Math.min(i + perRowCount, measureCount);
                boolean isEmptyRow = true;

                for (int j = i; j < end; j++) {
                    if (measureList.get(j).getChannelId() >= 0) {
                        isEmptyRow = false;
                        break;
                    }
                }
                if (!isEmptyRow) {
                    newList.addAll(measureList.subList(i, end));
                }
            }
            measureList.clear();
            measureList.addAll(newList);
            measureCount = measureList.size();
            measureRowCount = measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
            rowChangeListener.measureRowChange(measureRowCount);
            Command.get().getMeasure().changeMeasurePos();
        }

        // endregion
    }

    /***
     * 测量结构
     */
    public class MeasureItemStruct {
        int no;
        /**
         * 1 - 9
         */
        int channelId;
        int measureId;
        String measureName;
        /*float或者‘----’ */
        String data;
        /**
         * 在当前图片中显示的位置
         */
        Rect rect=new Rect();
        boolean isSelected=false;
        /** 是否是点击选择 */
        boolean isClickSelected=false;
        public MeasureItemStruct(int no,int channelId, int measureId, String measureName, String data) {
            this.no = no;
            this.channelId = channelId;
            this.measureId = measureId;
            this.measureName = measureName;
            this.data = data;
        }

        public int getNo(){return no;}
        public int getChannelId() {
            return channelId;
        }

        public int getMeasureId() {
            return measureId;
        }

        public String getMeasureName() {
            return measureName;
        }
        public boolean isSelected(){
            return isSelected;
        }
        public boolean isClickSelected(){
            return isClickSelected;
        }

        public String getData() {
            return data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MeasureItemStruct{");
            sb.append("channelId=").append(channelId);
            sb.append(", measureId=").append(measureId);
            sb.append(", measureName='").append(measureName).append('\'');
            sb.append(", data='").append(data).append('\'');
            sb.append(", rect=").append(rect);
            sb.append('}');
            return sb.toString();
        }
    }


    public static class FFTMeasure implements IMeasure, IWorkMode {

        //region 属性
        private String dataA = "---";
        private String dataV = "---";
        private String dataFre = "---";
        private String labelA = "";
        private String labelV = "";
        private String labelFre = "";
        private boolean visible = false;
        private int mathNumber;

        public void setVisible(boolean visible) {
            this.visible = visible;
            draw();
        }

        public void setColor(int color) {
            p.setColor(color);
        }

        public boolean isVisible() {
            return visible;
        }

        public void setLabel(String labelDC, String labelAmp, String labelFre) {
            this.labelA = labelDC;
            this.labelV = labelAmp;
            this.labelFre = labelFre;
            draw();
        }

        public void setData(String dataDC, String dataAmp, String dataFre) {
            this.dataA = dataDC;
            this.dataV = dataAmp;
            this.dataFre = dataFre;
            draw();
        }
        //endregion


        private final Bitmap bmp;
        private Canvas mCanvas;
        private Paint p;
        private boolean isChangeBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int showX = 30, showY = 55;

        public FFTMeasure(int mathNumber) {
            bmp = Bitmap.createBitmap(150, 100, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setColor(App.get().getResources().getColor(R.color.color_Math) /*Color.RED*/);
            p.setTextSize(20);
            p.setAntiAlias(true);
            switchWorkMode(WorkModeManage.getInstance().getmWorkMode());
            this.mathNumber = mathNumber;
            draw();
        }

        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        private void draw() {
            p.setXfermode(clearMode);
            mCanvas.drawPaint(p);
            p.setXfermode(srcMode);

            int x = 0, y = 22;
            mCanvas.drawText("M" + this.mathNumber, x, y, p);
            y += 22;
            mCanvas.drawText(labelA + ": " + dataA, x, y, p);
            y += 22;
            mCanvas.drawText(labelFre + ": " + dataFre, x, y, p);
            y += 22;
            mCanvas.drawText(labelV + ": " + dataV, x, y, p);

            isChangeBitmap = true;
            onRefresh();
        }

        //region IworkMode
        @Override
        public void switchWorkMode(int workMode) {
            Point pp = GlobalVar.get().getMeasureFFTPosition(workMode);
            if (pp == null) {
                showX = 10000;
                showY = -100;
                return;
            }
            showX = pp.x;
            showY = pp.y;
        }
        //endregion

        //region IMeasure

        @Override
        public void draw(Canvas canvas) {

        }

        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {
                canvasGL = canvas;
                if (visible) {
                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null);
                    canvas.drawBitmap(bmp, showX, showY);
                }
                isChangeBitmap = false;
            }
        }

        //endregion


    }

    public class SegmentMeasure implements IMeasure, IWorkMode {
        private String text = "--/--";
        private boolean visible = false;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
            draw();
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            draw();
        }

        private final Bitmap bmp;
        private Canvas mCanvas;
        private Paint p;
        private boolean isChangeBitmap = false;
        private ICanvasGL canvasGL;
        public void onRefresh(){
            if(canvasGL != null){
                canvasGL.onRefreshTexture();
            }
        }
        private int showX = 30, showY = 5;

        public SegmentMeasure() {
            bmp = Bitmap.createBitmap(150, 50, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bmp);
            p = new Paint();
            p.setColor(App.get().getResources().getColor(R.color.color_Math) /*Color.RED*/);
            p.setTextSize(20);
            p.setAntiAlias(true);
            draw();
        }

        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        private void draw() {
            p.setXfermode(clearMode);
            mCanvas.drawPaint(p);
            p.setXfermode(srcMode);

            int x = 0, y = 18;
            mCanvas.drawText(text, x, y, p);

            isChangeBitmap = true;
            onRefresh();
        }

        //region IworkMode
        @Override
        public void switchWorkMode(int workMode) {
            Point pp = GlobalVar.get().getMeasureSegmentPosition(workMode);
            if (pp == null) {
                showX = 10000;
                showY = -100;
                return;
            }
            showX = pp.x;
            showY = pp.y;
        }
        //endregion

        //region IMeasure

        @Override
        public void draw(Canvas canvas) {

        }

        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {
                canvasGL = canvas;
                if (visible) {

                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null);
                    canvas.drawBitmap(bmp, showX, showY);
                }
                isChangeBitmap = false;
            }
        }
    }

    public boolean isCursorTValueTrace() {
        boolean isTValueTrace = false;
        int X1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1);
        int X2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2);
//        Log.d("asdf","X1:" + X1 + ",X2:" +X2);
        if (X1 >= 0 || X2 >= 0) {
            isTValueTrace = true;
        }
        return isTValueTrace;
    }

}
