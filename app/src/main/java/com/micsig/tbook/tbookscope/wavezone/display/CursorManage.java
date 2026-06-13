package com.micsig.tbook.tbookscope.wavezone.display;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_XY;
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.svg.SvgManager;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by liwb on 2017/11/6.
 */

public class CursorManage implements IWorkMode, ICursorManage, IWaveControl {
    private static final String TAG = "CursorManage";

    private CursorManage_XY cursorManage_xy;
    private CursorManage_YT cursorManage_yt;

    private int VerIndex = 0;
    private int HorIndex = 0;
    private boolean enableCursorTrance=true;
    //光标改变，是否来自时基的改变
    private boolean CursorTrace =false;
    //多光标移动时，改变位置，但是不改变X1，X2的间距。
    private boolean CursorTraceMultiMove=false;
    //scpi操作的通道
    private int scpiChanIdx =-1;

    private boolean isLabelNotFollowCursor = false;
    //region 单例创建
    private static class CursorManage_Holder {
        private static final CursorManage instance = new CursorManage();
    }

    public static final CursorManage getInstance() {
        return CursorManage_Holder.instance;
    }
    //endregion


    //region  图片
    //通道，类型，状态
    public static final int Row1 = 0;
    public static final int Row2 = 1;
    public static final int Col1 = 2;
    public static final int Col2 = 3;
    public static final int Select = 0;
    public static final int NoSelect = 1;
    private Bitmap[][] resBmp = new Bitmap[4][2];

    private void initResBmp() {
        resBmp[Col1][Select] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorX1Path,
                SvgNodeInfo.CursorSelectColor,
                SvgNodeInfo.CURSOR_X_WIDTH,
                SvgNodeInfo.CURSOR_X_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,
                SvgNodeInfo.CursorPolygonSelectColors
        );
        resBmp[Col1][NoSelect] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorX1Path,
                SvgNodeInfo.CursorNoSelectColor,
                SvgNodeInfo.CURSOR_X_WIDTH,
                SvgNodeInfo.CURSOR_X_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,
                SvgNodeInfo.CursorPolygonNoSelectColors
        );
        resBmp[Col2][Select] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorX2Path,
                SvgNodeInfo.CursorSelectColor,
                SvgNodeInfo.CURSOR_X_WIDTH,
                SvgNodeInfo.CURSOR_X_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,
                SvgNodeInfo.CursorPolygonSelectColors
        );
        resBmp[Col2][NoSelect] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorX2Path,
                SvgNodeInfo.CursorNoSelectColor,
                SvgNodeInfo.CURSOR_X_WIDTH,
                SvgNodeInfo.CURSOR_X_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,
                SvgNodeInfo.CursorPolygonNoSelectColors
        );

        resBmp[Row1][Select] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorY1Path,
                SvgNodeInfo.CursorSelectColor,
                SvgNodeInfo.CURSOR_Y_WIDTH,
                SvgNodeInfo.CURSOR_Y_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,
                SvgNodeInfo.CursorPolygonSelectColors
        );
        resBmp[Row1][NoSelect] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorY1Path,
                SvgNodeInfo.CursorNoSelectColor,
                SvgNodeInfo.CURSOR_Y_WIDTH,
                SvgNodeInfo.CURSOR_Y_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,
                SvgNodeInfo.CursorPolygonNoSelectColors
        );

        resBmp[Row2][Select] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorY2Path,
                SvgNodeInfo.CursorSelectColor,
                SvgNodeInfo.CURSOR_Y_WIDTH,
                SvgNodeInfo.CURSOR_Y_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,
                SvgNodeInfo.CursorPolygonSelectColors
        );
        resBmp[Row2][NoSelect] = SvgManager.createCursorSvg(
                SvgNodeInfo.CursorY2Path,
                SvgNodeInfo.CursorNoSelectColor,
                SvgNodeInfo.CURSOR_Y_WIDTH,
                SvgNodeInfo.CURSOR_Y_HEIGHT,
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,
                SvgNodeInfo.CursorPolygonNoSelectColors
        );
    }
    //endregion

    public CursorManage() {
        initResBmp();
        cursorManage_xy = new CursorManage_XY(resBmp);
        cursorManage_yt = new CursorManage_YT(resBmp);
        cursorManage_xy.setOnMovingWaveEvent(onXYMovingWave);
        cursorManage_xy.setOnSelectChangeEvent(onXYSelectChange);
        cursorManage_yt.setOnMovingWaveEvent(onYTMovingWave);
        cursorManage_yt.setOnSelectChangeEvent(onYTSelectChange);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);

        EventFactory.addEventObserver(EventFactory.EVENT_MEASURE_RANGE,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,OnChannelVscaleUserChange);
    }
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {

            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_MEASURE_RANGE){
                if(MeasureService.isCursorRang()) {
                    CursorManage.getInstance().setColVisible(true);
                }
                if(!MeasureManage.getInstance().isCursorTValueTrace()) {
                    MeasureService.setMeasureRange((int) cursorManage_yt.getCol1Position(),
                            (int) cursorManage_yt.getCol2Position());
                }
            }

        }
    };

    public void init() {
    }

    public boolean isCursorTrace() {
        return CursorTrace;
    }

    public void setCursorTrace(boolean cursorTrace) {
        if (enableCursorTrance==false) return;
        if (WorkModeManage.getInstance().isXyMode()) return;
        this.CursorTrace = cursorTrace;
//        Log.d("Tag.Debug", String.format("CursorManage.setTimebaseChange: %s",timebaseChange ));
//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public boolean isCursorTraceMultiMove() {
        return CursorTraceMultiMove;
    }

    public void setCursorTraceMultiMove(boolean cursorTraceMultiMove) {
        CursorTraceMultiMove = cursorTraceMultiMove;
    }

    public boolean isEnableCursorTrance() {
        return enableCursorTrance;
    }
    public boolean isLabelNotFollowCursor() {
        return isLabelNotFollowCursor;
    }

    public void setEnableCursorTrance(boolean enableCursorTrance) {
        this.enableCursorTrance = enableCursorTrance;
    }

    public void setLabelNotFollowCursor(boolean isLabelFixed) {
        this.isLabelNotFollowCursor = isLabelFixed;
        MeasureService.forceMeasureRefresh();
        cursorManage_yt.refresh();
        cursorManage_xy.refresh();
    }
    public int getScpiChanIdx() {
        return scpiChanIdx;
    }

    public void setScpiChanIdx(int scpiChanIdx) {
        this.scpiChanIdx = scpiChanIdx;
    }

    private EventUIObserver OnChannelVscaleUserChange=new EventUIObserver() {
        @Override
        public void update(Object data) {
            CursorManage.getInstance().setCursorTrace(true);
            setXYData();
            CursorManage.getInstance().setCursorTrace(false);
        }
    };
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + rightMsgMath.getMathChannelNumber());
//            if (mathType == CacheUtil.MATHTYPE_FFT || mathType == CacheUtil.MATHTYPE_AXB) {
                setXYData();
//            }
        }
    };

    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Exception {
           setXYData();
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            double ytY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1);
            double ytY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2);
            int ytX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1);
            int ytX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2);
            double xyY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_1);
            double xyY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_2);
            int xyX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_1);
            int xyX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_2);
            ytY1 = ScopeBase.changeAccuracy(ytY1 * ScopeBase.getToUICoff());
            ytY2 = ScopeBase.changeAccuracy(ytY2 * ScopeBase.getToUICoff());

//            Logger.i(TAG, "consumerLoadCache() ==>"+" LoadCacheUtilCmd"
//                    +" ytY1:"+ytY1+" ytY2:"+ytY2+" ytX1:"+ytX1+" ytX2:"+ytX2
//                    +" xyY1:"+xyY1+" xyY2:"+xyY2+" xyX1:"+xyX1+" xyX2:"+xyX2
//                    +" isZoom:"+CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM));
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)) {
                ytY1 = ytY1 * GlobalVar.get().toYTCoef();
                ytY2 = ytY2 * GlobalVar.get().toYTCoef();
            }

            cursorManage_yt.setCursor(TChan.Cursor_row_1, ytY1);
            cursorManage_yt.setCursor(TChan.Cursor_row_2, ytY2);
            cursorManage_yt.setCursor(TChan.Cursor_col_1, ytX1);
            cursorManage_yt.setCursor(TChan.Cursor_col_2, ytX2);
            cursorManage_xy.setCursor(TChan.Cursor_row_1, xyY1);
            cursorManage_xy.setCursor(TChan.Cursor_row_2, xyY2);
            cursorManage_xy.setCursor(TChan.Cursor_col_1, xyX1);
            cursorManage_xy.setCursor(TChan.Cursor_col_2, xyX2);
        }
    };

    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            cursorManage_yt.refresh();
            cursorManage_xy.refresh();
            if (WaveManage.get().getCurCh() == TChan.Ch1) {
                if (workModeBean.getNextWorkMode() == IWorkMode.WorkMode_XY) {
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Ch1);
                } else {
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Ch1);
                }
            }
        }
    };

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.switchWorkMode(workMode);
                break;
            case WorkMode_XY:
                cursorManage_xy.switchWorkMode(workMode);
                break;
        }
    }

    public void draw(ICanvasGL canvas) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.draw(canvas);
                break;
            case WorkMode_XY:
                cursorManage_xy.draw(canvas);
                break;
        }
    }

    public int getVerIndex() {
        return VerIndex;
    }

    public int getHorIndex() {
        return HorIndex;
    }

    /**
     * 垂直方向的两条线中，切换三种状态的下一种状态<p/>
     * 三种状态：选中1，选中2，选中1和2
     */
    public void nextVerState() {
        VerIndex += 1;
        if (VerIndex > 3) VerIndex = 1;
        if (VerIndex == 1) {
            setSelectCursor(TChan.Cursor_col_1);
        } else if (VerIndex == 2) {
            setSelectCursor(TChan.Cursor_col_2);
        } else if (VerIndex == 3) {
            setCursorTracking(TChan.Cursor_col_3);
        }
    }

    /**
     * 水平方向的两条线中，切换三种状态的下一种状态<p/>
     * 三种状态：选中1，选中2，选中1和2
     */
    public void nextHorState() {
        HorIndex += 1;
        if (HorIndex > 3) HorIndex = 1;
        if (HorIndex == 1) {
            setSelectCursor(TChan.Cursor_row_1);
        } else if (HorIndex == 2) {
            setSelectCursor(TChan.Cursor_row_2);
        } else if (HorIndex == 3) {
            setCursorTracking(TChan.Cursor_row_3);
        }
    }

    /**
     * 设置水平方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public void setHorCursorSelected(int iWaveCursor) {
        if (TChan.Cursor_row_1 == iWaveCursor) {
            HorIndex = 1;
            setSelectCursor(TChan.Cursor_row_1);
        } else if (TChan.Cursor_row_2 == iWaveCursor) {
            HorIndex = 2;
            setSelectCursor(TChan.Cursor_row_2);
        } else if (TChan.Cursor_row_3 == iWaveCursor) {
            HorIndex = 3;
            setSelectCursor(TChan.Cursor_row_3);
        } else if (TChan.Cursor_row_4 == iWaveCursor) {
            setSelectCursor(TChan.Cursor_row_4);
        }
    }

    /**
     * 获得水平方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public int getHorCursorSelected() {
        if (HorIndex == 1) {
            return TChan.Cursor_row_1;
        } else if (HorIndex == 2) {
            return TChan.Cursor_row_2;
        } else if (HorIndex==3){
            return TChan.Cursor_row_3;
        }else {
            return TChan.Cursor_row_4;
        }
    }

    /**
     * 设置垂直方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public void setVerCursorSelected(int iWaveCursor) {
        if (TChan.Cursor_col_1 == iWaveCursor) {
            VerIndex = 1;
            setSelectCursor(TChan.Cursor_col_1);
        } else if (TChan.Cursor_col_2 == iWaveCursor) {
            VerIndex = 2;
            setSelectCursor(TChan.Cursor_col_2);
        } else if (TChan.Cursor_col_3 == iWaveCursor) {
            VerIndex = 3;
            setSelectCursor(TChan.Cursor_col_3);
        } else if (TChan.Cursor_col_4 == iWaveCursor) {
            setSelectCursor(TChan.Cursor_col_4);
        }
    }

    /**
     * 获得垂直方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public int getVerCursorSelected() {
        if (VerIndex == 1) {
            return TChan.Cursor_col_1;
        } else if (VerIndex == 2) {
            return TChan.Cursor_col_2;
        } else if (VerIndex==3){
            return TChan.Cursor_col_3;
        }else {
            return TChan.Cursor_col_4;
        }
    }

    /**
     * 根据当前状态移动垂直方向光标
     *
     * @param isRight 向右
     * @param count
     */
    public void moveVerCursor(boolean isRight, int count) {
        if (VerIndex == 0) VerIndex = 1;
        if (VerIndex == 1) {
            setSelectCursor(TChan.Cursor_col_1);
        } else if (VerIndex == 2) {
            setSelectCursor(TChan.Cursor_col_2);
        } else if (VerIndex == 3) {
            setCursorTracking(TChan.Cursor_col_3);
        }
        if (isRight) {
            addPixMove(count);
        } else {
            addPixMove(count * -1);
        }
    }

    /**
     * 根据当前状态移动水平方向光标
     *
     * @param isRight 向右
     * @param count
     */
    public void moveHorCursor(boolean isRight, int count) {
//        Logger.i(TAG,"isRight:"+isRight+" count:"+count+" HorIndex:"+HorIndex);
        if (HorIndex == 0) HorIndex = 1;
        if (HorIndex == 1) {
            setSelectCursor(TChan.Cursor_row_1);
        } else if (HorIndex == 2) {
            setSelectCursor(TChan.Cursor_row_2);
        } else if (HorIndex == 3) {
            setCursorTracking(TChan.Cursor_row_3);
        }
        if (isRight) {
            addPixMove(count);
        } else {
            addPixMove(count * -1);
        }
    }

    public void moveZoomVerCursor(boolean isRight, int count) {
        if (VerIndex == 0) VerIndex = 1;
        if (VerIndex == 1) {
            setSelectCursor(TChan.Cursor_col_1);
        } else if (VerIndex == 2) {
            setSelectCursor(TChan.Cursor_col_2);
        } else if (VerIndex == 3) {
            setCursorTracking(TChan.Cursor_col_3);
        }
        if (isRight) {
            zoomPixMove(count);
        } else {
            zoomPixMove(count * -1);
        }
    }

    public void moveZoomHorCursor(boolean isRight, int count) {
        if (HorIndex == 0) HorIndex = 1;
        if (HorIndex == 1) {
            setSelectCursor(TChan.Cursor_row_1);
        } else if (HorIndex == 2) {
            setSelectCursor(TChan.Cursor_row_2);
        } else if (HorIndex == 3) {
            setCursorTracking(TChan.Cursor_row_3);
        }
        if (isRight) {
            zoomPixMove(count);
        } else {
            zoomPixMove(count * -1);
        }
    }

    private IWave.OnSelectChangeEvent onXYSelectChange = new IWave.OnSelectChangeEvent() {
        @Override
        public void OnSelectChange(IWave iWave, boolean isSelect) {

        }
    };

    private IWave.OnSelectChangeEvent onYTSelectChange = new IWave.OnSelectChangeEvent() {
        @Override
        public void OnSelectChange(IWave iWave, boolean isSelect) {

        }
    };

    private IWave.OnMovingWaveEvent onXYMovingWave = new IWave.OnMovingWaveEvent() {
        @Override
        public void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus) {
           setXYData();
        }
    };

    private IWave.OnMovingWaveEvent onYTMovingWave = new IWave.OnMovingWaveEvent() {
        @Override
        public void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus) {
            setXYData();
        }
    };

    private void setXYData(){
        if(getRowVisible() || getColVisible()) {
            if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) {
                cursorManage_xy.setData();
            } else {
                cursorManage_yt.setData();
            }
        }
    }
    public void curChannelMove() {

        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) {
            int chIdx = WaveManage.get().getCurCh();
            if (chIdx == TChan.Ch1) {
                setWaveZoneSlideDirectionAndLastObjCol(TChan.Ch1);
            } else if (chIdx == TChan.Ch2) {
                setWaveZoneSlideDirectionAndLastObjRow(TChan.Ch2);
            }
        }
       setXYData();

    }

    public void timeBaseMove() {
        setXYData();
    }

    public void initXY() {
        setXYData();
    }

//    private void setXYData() {
//        if (WorkModeManage.getInstance().getmWorkMode() == WorkMode_YT
//                || WorkModeManage.getInstance().getmWorkMode() == WorkMode_YTZOOM) {
//
//            int curCh= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE)+1;
//            if (curCh==IWave.S1){ //自由选择模式
//                curCh = WaveManage.get().getCurCh();
//            }
//            if (curCh == -1) return;
//            long curChY = WaveManage.get().getPositionY(curCh);
//            long curTimeX = 0;
//            double timeEvery = 0;
//            if ((curCh >= IWave.Ch1 && curCh <= IWave.Math) || (curCh >= IWave.S1 && curCh <= IWave.S2) ) {
//                if (ChannelFactory.isMath_FFT_Ch(curCh - IWave.Ch1 + ChannelFactory.CH1)) {
//                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel().getHorizontalAxisMathFFT();
//                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();
//                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();
//                } else {
//                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
//                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
//                }
//            } else if (curCh >= IWave.R1 && curCh <= IWave.R4) {
//                RefChannel refChannel = ChannelFactory.getRefChannel(curCh - IWave.Ch1 + ChannelFactory.CH1);
//                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();
//                timeEvery = refChannel.getRefTimePerPix();
//            }
//
//            double chEvery = 0;
//            if (curCh >= IWave.Ch1 && curCh <= IWave.Ch4) {
//                chEvery = ChannelFactory.getDynamicChannel(curCh - 1).getVerticalPerPix();
//            } else if (curCh == IWave.Math) {
//                chEvery = ChannelFactory.getMathChannel().getVerticalPerPix();
//            } else if (curCh >= IWave.R1 && curCh <= IWave.R4) {
//                chEvery = ChannelFactory.getRefChannel(curCh - 1).getVerticalPerPix();
//            }
//
//            MeasureService.setMeasureRange((int)cursorManage_yt.getCol1Position(),
//                        (int)cursorManage_yt.getCol2Position());
//
//            double x1 = (cursorManage_yt.getCol1Position() - curTimeX) * timeEvery;
//            double x2 = (cursorManage_yt.getCol2Position() - curTimeX) * timeEvery;
//            double y1 = (curChY - cursorManage_yt.getRow1Position()) * chEvery;
//            double y2 = (curChY - cursorManage_yt.getRow2Position()) * chEvery;
//
//            double deltaY = Math.abs(y1 - y2);
//            double deltaX = Math.abs(x1 - x2);
//            String yUnit = ChannelFactory.getProbeType(curCh - IWave.Ch1 + ChannelFactory.CH1);
//            String xUnit;
//            if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate())) {
//                xUnit = "Hz";
//            } else {
//                xUnit = "s";
//            }
////            Logger.d(TAG,"curCh:" + curCh);
//            if((curCh >= IWave.S1 && curCh <= IWave.S2)) {
//                MeasureManage.getInstance().getCursorMeasure().setParam(
//                        null                            //单位：V(A)
//                        , null                         //单位：V(A)
//                        , null                      //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit)                         //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s"))//单位：Hz
//                        , null                              //单位：V(A)/s
//                );
//            }else{
//                MeasureManage.getInstance().getCursorMeasure().setParam(
//                        String.valueOf(TBookUtil.getFourFromD_(y1) + yUnit)                              //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(y2) + yUnit)                            //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaY) + yUnit)                        //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit)                         //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s"))//单位：Hz
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + yUnit + "/" + xUnit)//单位：V(A)/s
//                );
//            }
//            Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 1.0 / deltaX, deltaY * 1.0 / deltaX);
//        } else if (WorkModeManage.getInstance().getmWorkMode() == WorkMode_XY) {
//            long curChX = WaveManage.get().getPositionY(IWave.Ch1);
//            long curChY = WaveManage.get().getPositionY(IWave.Ch2);
//            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();
//            double chEveryY = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();
//            double x1 = (cursorManage_xy.getCol1Position() - curChX) * chEveryX;
//            double x2 = (cursorManage_xy.getCol2Position() - curChX) * chEveryX;
//            double y1 = (curChY - cursorManage_xy.getRow1Position()) * chEveryY;
//            double y2 = (curChY - cursorManage_xy.getRow2Position()) * chEveryY;
//            double deltaY = Math.abs(y1 - y2);
//            double deltaX = Math.abs(x1 - x2);
//            String unitX = ChannelFactory.getProbeType(ChannelFactory.CH1);
//            String unitY = ChannelFactory.getProbeType(ChannelFactory.CH2);
//            MeasureManage.getInstance().getCursorMeasure().setParam(
//                    String.valueOf(TBookUtil.getFourFromD_(y1) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(y2) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(deltaY) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(x1) + unitX)
//                    , String.valueOf(TBookUtil.getFourFromD_(x2) + unitX)
//                    , String.valueOf(TBookUtil.getFourFromD_(deltaX) + unitX)
//                    , null, null
//            );
//            Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 0, 0);
//        }
//    }

    //region  光标数据

    public boolean getRowVisible() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getRowVisible();
            case WorkMode_XY:
                return cursorManage_xy.getRowVisible();
        }
        return false;
    }

    public boolean getColVisible() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getColVisible();
            case WorkMode_XY:
                return cursorManage_xy.getColVisible();
        }
        return false;
    }

    public double getRow1Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getRow1Position();
            case WorkMode_XY:
                return cursorManage_xy.getRow1Position();
        }
        return -100;
    }

    public double getRow2Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getRow2Position();
            case WorkMode_XY:
                return cursorManage_xy.getRow2Position();
        }
        return -100;
    }

    public long getCol1Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getCol1Position();
            case WorkMode_XY:
                return cursorManage_xy.getCol1Position();
        }
        return -100;
    }

    public  long getCol2Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getCol2Position();
            case WorkMode_XY:
                return cursorManage_xy.getCol2Position();
        }
        return -100;
    }
    //endregion


    //region interface ICursorManage

    private void setWaveZoneSlideDirectionAndLastObjRow(int cursorRowObj) {
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, cursorRowObj);
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
    }

    private void setWaveZoneSlideDirectionAndLastObjCol(int cursorColObj) {
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, cursorColObj);
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);
    }

    private void setWaveZoneSlideDirectionAndLastObjToWaveChannel() {
        int chIdx = WaveManage.get().getCurCh();
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);
    }

    private void setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone() {
        int chIdx = WaveManage.get().getCurCh();
        if (ChannelFactory.isDynamicCh(TChan.toFpgaChNo(chIdx))) {
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);
            if (TChan.Ch1 == chIdx || TChan.Ch3 == chIdx) {
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);
            } else if (TChan.Ch2 == chIdx || TChan.Ch4 == chIdx) {
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);
            }
        }
    }

    @Override
    public void setRowVisible(boolean visible) {
        setRowVisible(WorkModeManage.getInstance().getmWorkMode(), visible);
    }


    @Override
    public void setColVisible(boolean visible) {
        setColVisible(WorkModeManage.getInstance().getmWorkMode(), visible);
    }

    public void setRowVisible(int workMode, boolean visible) {
        RxBus.getInstance().post(RxEnum.CURSOR_CHANGE_VISIBLE, new MsgCursorVisible(workMode != WorkMode_XY, false, visible));
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setRowVisible(workMode, visible);
                cursorManage_yt.setRowMeasureVisible(visible);
                if (visible) {
                    this.HorIndex = 1;
                    cursorManage_yt.setSelectCursor(TChan.Cursor_row_1);
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1);
                } else {
                    if (getColVisible() && (getCurrSelectCursor() == TChan.Cursor_col_1 || getCurrSelectCursor() == TChan.Cursor_col_2 || getCurrSelectCursor()== TChan.Cursor_col_3)) {
                        this.VerIndex = 1;
                        cursorManage_yt.setSelectCursor(TChan.Cursor_col_1);
                        setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1);
                    } else {
                        setWaveZoneSlideDirectionAndLastObjToWaveChannel();
                    }
                }
                break;
            case WorkMode_XY:
                cursorManage_xy.setRowVisible(workMode, visible);
                cursorManage_xy.setRowMeasureVisible(visible);
                if (visible) {
                    this.HorIndex = 1;
                    cursorManage_xy.setSelectCursor(TChan.Cursor_row_1);
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1);
                } else {
                    if (getColVisible() && (getCurrSelectCursor() == TChan.Cursor_col_1 || getCurrSelectCursor() == TChan.Cursor_col_2 || getCurrSelectCursor()== TChan.Cursor_col_3)) {
                        this.VerIndex = 1;
                        cursorManage_xy.setSelectCursor(TChan.Cursor_col_1);
                        setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1);
                    } else {
                        setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone();
                    }
                }
                break;
        }
    }

    public void setColVisible(int workMode, boolean visible) {
        RxBus.getInstance().post(RxEnum.CURSOR_CHANGE_VISIBLE, new MsgCursorVisible(workMode != WorkMode_XY, true, visible));
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setColVisible(workMode, visible);
                cursorManage_yt.setColMeasureVisible(visible);
                if (visible) {
                    this.VerIndex = 1;
                    cursorManage_yt.setSelectCursor(TChan.Cursor_col_1);
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1);
                } else {
                    if (getRowVisible() && ((getCurrSelectCursor() == TChan.Cursor_row_1 || getCurrSelectCursor() == TChan.Cursor_row_2))) {
                        this.HorIndex = 1;
                        cursorManage_yt.setSelectCursor(TChan.Cursor_row_1);
                        setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1);
                    } else {
                        setWaveZoneSlideDirectionAndLastObjToWaveChannel();
                    }
                }
                break;
            case WorkMode_XY:
                cursorManage_xy.setColVisible(workMode, visible);
                cursorManage_xy.setColMeasureVisible(visible);
                if (visible) {
                    this.VerIndex = 1;
                    cursorManage_xy.setSelectCursor(TChan.Cursor_col_1);
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1);
                } else {
                    if (getRowVisible() && ((getCurrSelectCursor() == TChan.Cursor_row_1 || getCurrSelectCursor() == TChan.Cursor_row_2))) {
                        this.HorIndex = 1;
                        cursorManage_xy.setSelectCursor(TChan.Cursor_row_1);
                        setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1);
                    } else {
                        setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone();
                    }
                }
                break;
        }
        setXYData();
    }

    @Override
    public int selectCursor(int x, double y) {
        int i = 0;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                i = cursorManage_yt.selectCursor(x, y);
                break;
            case WorkMode_XY:
                i = cursorManage_xy.selectCursor(x, y);
                break;
        }
        return i;
    }

    public void MoveLabel(int offsetX,int offsetY){
        if (WorkModeManage.getInstance().getmWorkMode()!=WorkMode_XY){
            cursorManage_yt.MoveLabel(offsetX,offsetY);
        }else{
            cursorManage_xy.MoveLabel(offsetX,offsetY);
        }
    }

    public int selectCursor(int x,int y,int cursorId){
        int i = -1;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                i = cursorManage_yt.selectCursor(x, y,cursorId);
                break;
            case WorkMode_XY:
                i = cursorManage_xy.selectCursor(x, y,cursorId);
                break;
        }
        return i;
    }

    public void setSelectHighColor(int index) {
        cursorManage_yt.setSelectHighColor(index);
        cursorManage_xy.setSelectHighColor(index);
    }

    public int getMultiCursorSelect(int x,int y){
        if (selectCursor(x,y)!=-1) {
            switch (WorkModeManage.getInstance().getmWorkMode()) {
                case WorkMode_YT:
                case WorkMode_YTZOOM:
                    return cursorManage_yt.getMultiCursorSelect(x, y);
                case WorkMode_XY:
                    return cursorManage_xy.getMultiSelectCursor(x, y);

            }
        }
        return -1;
    }
    @Override
    public void setSelectCursor(int index) {
        if (index== TChan.Cursor_all){
            switch (WorkModeManage.getInstance().getmWorkMode()){
                case WorkMode_YT:
                case WorkMode_YTZOOM:
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2,TChan.Cursor_row_1,TChan.Cursor_row_2);

                    break;
                case WorkMode_XY:
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2,TChan.Cursor_row_1, TChan.Cursor_row_2);

                    break;
            }
            return;
        }
        if (index == TChan.Cursor_col_4 || index == TChan.Cursor_row_4 || index==TChan.Cursor_col_3 || index== TChan.Cursor_row_3) {
            switch (WorkModeManage.getInstance().getmWorkMode()) {
                case WorkMode_YT:
                case WorkMode_YTZOOM:
                    if (index == TChan.Cursor_col_4 || index==TChan.Cursor_col_3) {
                        cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2);
                    }
                    if (index==TChan.Cursor_row_4 || index==TChan.Cursor_row_3)
                    {
                        cursorManage_yt.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2);
                    }
                    break;
                case WorkMode_XY:
                    if (index == TChan.Cursor_col_4 || index==TChan.Cursor_col_3) {
                        cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2);
                    } else {
                        cursorManage_xy.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2);
                    }
                    break;
            }
            return;
        }
        switch (index) {
            case TChan.Cursor_col_1:
                VerIndex = 1;
                break;
            case TChan.Cursor_col_2:
                VerIndex = 2;
                break;
            case TChan.Cursor_row_1:
                HorIndex = 1;
                break;
            case TChan.Cursor_row_2:
                HorIndex = 2;
                break;
            default:
                break;
        }
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setSelectCursor(index);
                break;
            case WorkMode_XY:
                cursorManage_xy.setSelectCursor(index);
                break;
        }
    }

    @Override
    public void moveSelectCursor(int offsetX, double offsetY) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.moveSelectCursor(offsetX, offsetY);
                break;
            case WorkMode_XY:
                cursorManage_xy.moveSelectCursor(offsetX, offsetY);
                break;
        }
    }

    @Override
    public void moveMultiSelectCursor(int offsetX, int offsetY) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.moveMultiSelectCursor(offsetX, offsetY);
                break;
            case WorkMode_XY:
                cursorManage_xy.moveMultiSelectCursor(offsetX, offsetY);
                break;
        }
    }
    public void moveMultiSelectCursor(int offsetX, int offsetY,int cursorIdx) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false,cursorIdx) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                if (CursorManage.getInstance().isEnableCursorTrance()) {
//                setCursorTrace(true);
                    setCursorTraceMultiMove(true);
                    if (offsetX != 0 && cursorIdx == TChan.Cursor_col_3 || cursorIdx == TChan.Cursor_col_4) {
                        CursorManage.setCursorByTimebaseTrace(offsetX);
                    }
                    if (offsetY != 0 && cursorIdx == TChan.Cursor_row_3 || cursorIdx == TChan.Cursor_row_4) {
                        CursorManage.setCursorByScaleTrace(-offsetY);
                    }
                    setCursorTrace(false);
                    setCursorTraceMultiMove(false);
                }else {
                    cursorManage_yt.moveMultiSelectCursor(offsetX, offsetY);
                }
                break;
            case WorkMode_XY:
                cursorManage_xy.moveMultiSelectCursor(offsetX, offsetY);
                break;
        }
    }


    public static boolean isAutoSwitchChannel(){
        return ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT == CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
    }

    @Override
    public void setCursorChannelColor(int ChNo) {
        ChNo=TChan.Cursor_col_1;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setCursorChannelColor(ChNo);
                cursorManage_yt.setData();

                break;
            case WorkMode_XY:
                cursorManage_yt.setCursorChannelColor(ChNo);
                cursorManage_xy.setCursorChannelColor(-1);
                cursorManage_xy.setData();

                break;
        }
    }

    /**
     * 是否选择自动光标
     * @return
     */
    public boolean isAutoCursor(){
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
        return sourceIdx==24;
    }
    public int getCursorChan(){
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
        return sourceIdx;
    }
    public boolean isSelectChanEqCursorChan() {
        boolean b = (TChan.toFpgaChNo(WaveManage.get().getCurCh())) == getCursorChan();
//        Log.d("Tag.Debug", String.format("CursorManage.isSelectChanEqCursorChan: curChan:%s ,curorChan:%s ,b:%s",WaveManage.get().getCurCh(),getSelectChan(),b ));
        return b;
    }

    public boolean isScpiChanEqCursorChan(){
        if (getScpiChanIdx()==-1) return false;
        boolean b= getScpiChanIdx()==getCursorChan();
//        Log.d("Tag.Debug", String.format("CursorManage.isScpiChanEqCursorChan: %s,%s",getScpiChanIdx(),getCursorChan() ));

        return b;
    }


    private  boolean isCursorLogic(){
        int chIdx= getCursorChan();
        return isLogicChan(chIdx);
    }
    private boolean isCurLogic(){
        int chIdx= TChan.toFpgaChNo(WaveManage.get().getCurCh());
        return isLogicChan(chIdx);
    }
    private boolean isLogicChan(int chIdx)
    {
        boolean b1=(chIdx>=ChannelFactory.CH1 && chIdx<=ChannelFactory.CH8);
        boolean b2=(chIdx>=ChannelFactory.MATH1 && chIdx<=ChannelFactory.MATH8 && ChannelFactory.getMathChannel(chIdx).getMathType()!=MathWave.MATH_FFTWAVE);
        return b1 || b2;
    }
    private boolean isSerialChan(){
        return (TChan.isSerial(WaveManage.get().getCurCh()));
    }

    public boolean isFFT(int tChan)
    {
        if (TChan.isRef(tChan) ){
            RefChannel ref= ChannelFactory.getRefChannel(TChan.toFpgaChNo(tChan));
            if (ref.getRefType()==2){
                return true;
            }
        }else if (TChan.isMath(tChan)){
            MathChannel math=ChannelFactory.getMathChannel(TChan.toFpgaChNo(tChan));
            if (math.getMathType()== MathFFTWave.MATH_FFTWAVE){
                return true;
            }
        }
        return false;
    }
    /**
     * 垂直光标是否跟踪
     * 1。自动，都移动
     * 2。不自动，锁定FFT ，当前FFT，都移动
     * 3。不自动，锁定非FFT，当前非FFT，都移动
     * 4.当前是串形通道，都移动
     *
     *
     * @return true:跟踪  false:不跟踪
     */
    public boolean isColCursorTrace()
    {
        boolean bTrace1= isAutoCursor();
        boolean bTrace2= isAutoCursor()==false && isCursorLogic() && isCurLogic();
        boolean bTrace3= isAutoCursor()==false && isSelectChanEqCursorChan();
        boolean bTrace4= isSerialChan();
        //Log.d("Tag.Debug", String.format("CursorManage.isColCursorTrace: %s,%s,%s,%s",bTrace1,bTrace2,bTrace3,bTrace4 ));
        return  bTrace1 || bTrace2 || bTrace3 || bTrace4;
    }
    public boolean isRowCursorTrace()
    {
        boolean bTrace1= CursorManage.getInstance().isAutoCursor() ;
        boolean bTrace2= CursorManage.getInstance().isSelectChanEqCursorChan();
        boolean bTrace3 = CursorManage.getInstance().isScpiChanEqCursorChan();
//        Log.d("Tag.Debug", String.format("CursorManage.isRowCursorTrace: bTrace1:%s,bTrace2:%s,bTrace3:%s",bTrace1,bTrace2,bTrace3 ));
        return bTrace1 || bTrace2 || bTrace3;
    }

    @Override
    public void moveFinish() {
        cursorManage_yt.CancelAllHightShow();
        cursorManage_xy.CancelAllHightShow();
    }
    //endregion

    //region interface IWaveControl

    @Override
    public void addPixMove() {
        if (isExecMoveForTracking(1, 1, false) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.addPixMove();
                break;
            case WorkMode_XY:
                cursorManage_xy.addPixMove();
                break;
        }
    }

    /**
     * 加减num个像素
     *
     * @param num 为正增加，为负减少
     */
    public void addPixMove(int num) {
        if (isExecMoveForTracking(num, num, false) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.addPixMove(num);
                break;
            case WorkMode_XY:
                cursorManage_xy.addPixMove(num);
                break;
        }
    }

    /**
     * 如果是正数，则两条光标线向中间聚拢，如果是负数，则向两边扩散...
     */
    public void zoomPixMove(int num) {
        if (isExecMoveForTracking(num, num, true) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.zoomPixMove(num);
                break;
            case WorkMode_XY:
                cursorManage_xy.zoomPixMove(num);
                break;
        }
    }

    @Override
    public void subPixMove() {
        if (isExecMoveForTracking(-1, -1, false) == false) return;
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.subPixMove();
                break;
            case WorkMode_XY:
                cursorManage_xy.subPixMove();
                break;
        }
    }

    /**
     * 跟踪光标是否执行移动
     *
     * @param offsetX X偏移位置 右偏移为正  左偏移为负
     * @param offsetY Y偏移位置  上偏移为负，下偏移为负
     * @param isZoom  是否是缩放操作
     * @return 默认返回不执行，到边界
     */
    private boolean isExecMoveForTracking(int offsetX, double offsetY, boolean isZoom) {
        boolean b = false;
//        Logger.i(TAG,"==========isRowSelect:"+isRowSelect()+" VerIndex:"+VerIndex+" HorIndex:"+HorIndex+"==========");
        int cursor2Reverse = isZoom ? -1 : 1;
        if (isRowSelect() && HorIndex == 3) {
            int minRow = 0;
            int maxRow = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
            double row1 = getRow1Position();
            double row2 = getRow2Position();
//            Logger.i(TAG,"minRow:"+minRow+" maxRow:"+maxRow+" row1:"+row1+" row2:"+row2+" offsetX:"+offsetY);
            if ((row1 + offsetY < maxRow && row2 + offsetY * cursor2Reverse < maxRow)
                    && (row1 + offsetY >= minRow && row2 + offsetY * cursor2Reverse >= minRow)) {
                b = true;
            }
        } else if (!isRowSelect() && VerIndex == 3) {
            int minCol = 0;
            int maxCol = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
            long col1 = getCol1Position();
            long col2 = getCol2Position();
//            Logger.i(TAG,"minCol:"+minCol+" maxCol:"+maxCol+" col1:"+col1+" col2:"+col2+" offsetY:"+offsetX);
            if ((col1 + offsetX < maxCol && col2 + offsetX * cursor2Reverse < maxCol)
                    && (col1 + offsetX >= minCol && col2 + offsetX * cursor2Reverse >= minCol)) {
                b = true;
            }
        } else {
            b = true;
        }
        return b;
    }
    private boolean isExecMoveForTracking(int offsetX, int offsetY, boolean isZoom,int cursorIdx) {
        boolean b = false;
//        Logger.i(TAG,"==========isRowSelect:"+isRowSelect()+" VerIndex:"+VerIndex+" HorIndex:"+HorIndex+"==========");
        int cursor2Reverse = isZoom ? -1 : 1;
        if (isRowSelect() && (HorIndex == 3 || cursorIdx==TChan.Cursor_row_3 || cursorIdx==TChan.Cursor_row_4)) {
            int minRow = 0;
            int maxRow = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
            double row1 = getRow1Position();
            double row2 = getRow2Position();
//            Logger.i(TAG,"minRow:"+minRow+" maxRow:"+maxRow+" row1:"+row1+" row2:"+row2+" offsetX:"+offsetY);
            if ((row1 + offsetY < maxRow && row2 + offsetY * cursor2Reverse < maxRow)
                    && (row1 + offsetY >= minRow && row2 + offsetY * cursor2Reverse >= minRow)) {
                b = true;
            }
        } else if (!isRowSelect() && (VerIndex == 3 || cursorIdx==TChan.Cursor_col_3 || cursorIdx==TChan.Cursor_col_4) )  {
            int minCol = 0;
            int maxCol = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
            long col1 = getCol1Position();
            long col2 = getCol2Position();
//            Logger.i(TAG,"minCol:"+minCol+" maxCol:"+maxCol+" col1:"+col1+" col2:"+col2+" offsetY:"+offsetX);
            if ((col1 + offsetX < maxCol && col2 + offsetX * cursor2Reverse < maxCol)
                    && (col1 + offsetX >= minCol && col2 + offsetX * cursor2Reverse >= minCol)) {
                b = true;
            }
        } else {
            b = true;
        }
        return b;
    }

    private boolean isRowSelect() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.isRowSelect();
            case WorkMode_XY:
                return cursorManage_xy.isRowSelect();
        }
        return false;
    }

    @Override
    public void initCursorX() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.initCursorX();
                break;
            case WorkMode_XY:
                cursorManage_xy.initCursorX();
                break;
        }
    }

    @Override
    public void initCursorY() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.initCursorY();
                break;
            case WorkMode_XY:
                cursorManage_xy.initCursorY();
                break;
        }
    }

    @Override
    public void setCursor(int cursorType, double position) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setCursor(cursorType, position);
                break;
            case WorkMode_XY:
                cursorManage_xy.setCursor(cursorType, position);
                break;
        }
    }
    public void setCursorOffsetPos(int cursorType,int offset){
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.setCursorOffsetPos(cursorType, offset);
                break;
            case WorkMode_XY:
                cursorManage_xy.setCursorOffsetPos(cursorType, offset);
                break;
        }
    }

    @Override
    public double getCursor(int cursorType) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorManage_yt.getCursor(cursorType);
                break;
            case WorkMode_XY:
                cursorManage_xy.getCursor(cursorType);
                break;
        }
        return 0;
    }

    @Override
    public void setCursorTracking(int CurrChNo) {
        if (CurrChNo == TChan.Cursor_col_3) {
            VerIndex = 3;
            switch (WorkModeManage.getInstance().getmWorkMode()) {
                case WorkMode_YT:
                case WorkMode_YTZOOM:
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2);
                    break;
                case WorkMode_XY:
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2);
                    break;
            }
        } else if (CurrChNo == TChan.Cursor_row_3) {
            HorIndex = 3;
            switch (WorkModeManage.getInstance().getmWorkMode()) {
                case WorkMode_YT:
                case WorkMode_YTZOOM:
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2);
                    break;
                case WorkMode_XY:
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2);
                    break;
            }
        }
    }

    @Override
    public int getCurrSelectCursor() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                return cursorManage_yt.getCurrSelectCursor();
            case WorkMode_XY:
                return cursorManage_xy.getCurrSelectCursor();

        }
        return 0;
    }

    public void changeHeight(int height) {
//        cursorManage_xy.changeHeight(height);
        cursorManage_yt.changeHeight(height);
//        CursorManage.getInstance().initCursorY();
    }

    //endregion

    //光标跟踪之时基，触发时刻
    public static void setCursorByTimebaseTrace(){
        try {
            if (CursorManage.getInstance().getColVisible()==false) return;
            if (getInstance().enableCursorTrance==false) return;
            if (WorkModeManage.getInstance().isXyMode()) return;
            if (CursorManage.getInstance().isColCursorTrace()==false) return;
            //if (CursorManage.getInstance().isAutoCursor()==false &&  CursorManage.getInstance().isSelectChanEqCursorChan()==false) return;
            double x1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1));
            double x2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2));
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())){
                x1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);
                x2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);
            }
            long pix1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x1);
            long pix2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x2);


            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: x1:%s, x2:%s  ,pix1:%s,pix2:%s ,curCh:%s",x1,x2,pix1,pix2,WaveManage.get().getCurCh() ));

            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, (int) pix1);
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, (int) pix2);

//            throw new Exception();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /** delta标答移动 */
    public static void setCursorByTimebaseTrace(int offsetX){
        try {
            if (CursorManage.getInstance().getColVisible()==false) return;
            if (getInstance().enableCursorTrance==false) return;
            if (WorkModeManage.getInstance().isXyMode()) return;
            if (CursorManage.getInstance().isColCursorTrace()==false) return;
            //if (CursorManage.getInstance().isAutoCursor()==false &&  CursorManage.getInstance().isSelectChanEqCursorChan()==false) return;
            double x1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1));
            double x2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2));
            double deltaX=Math.abs(x1-x2);
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())){
                x1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);
                x2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);
                deltaX=Math.abs(x1-x2);
            }

            long curTimeX = 0;
            double timeEvery = 0;
            int chIdx= TChan.toFpgaChNo(WaveManage.get().getCurCh());
            if (ChannelFactory.isDynamicCh(chIdx)
                    || ChannelFactory.isMathCh(chIdx)
                    || ChannelFactory.isSerialCh(chIdx) ) {
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) {
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT();
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();
                } else {
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
                }
            } else if (ChannelFactory.isRefCh(chIdx)) {
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();
                timeEvery = refChannel.getRefTimePerPix();
            }

            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: offsetX:%s, chEvery:%s ,x1:%s ,x2:%s",offsetX,timeEvery,x1,x2 ));
            x1= (x1-offsetX*timeEvery);
            x2=(x2-offsetX*timeEvery);

            long pix1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x1);
            long pix2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x2);


            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: offsetX:%s,pix1:%s,pix2:%s",offsetX,pix1,pix2 ));
            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: x1:%s,x2:%s ,delta:%s",x1,x2,deltaX ));
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())){
                if ((Math.abs(pix1 - pix2) * timeEvery) <= deltaX || !GlobalVar.get().isContainMainWaveX((int)pix1,(int)pix2) ) {
                    //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: save" ));
                    CursorManage.getInstance().setCursorTrace(true);
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ, String.valueOf(x1));
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ, String.valueOf(x2));
                }
            }else {
                if ((Math.abs(pix1 - pix2) * timeEvery) <= deltaX || !GlobalVar.get().isContainMainWaveX((int)pix1,(int)pix2)) {
                    CursorManage.getInstance().setCursorTrace(true);
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1, String.valueOf(x1));
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2, String.valueOf(x2));
                }
            }

            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, (int) pix1);
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, (int) pix2);
            CursorManage.getInstance().setCursorTrace(false);

//            throw new Exception();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void setCursorByScaleTrace(){
        try{
//            Log.d("Tag.Debug", String.format("CursorManage.setCursorByScaleTrace: begin" ));
            if (CursorManage.getInstance().getRowVisible()==false) return;
            if (getInstance().enableCursorTrance==false) return;
            if (CursorManage.getInstance().isRowCursorTrace()==false)  return;

            if (WorkModeManage.getInstance().isXyMode()) return;
            if (TChan.isSerial(WaveManage.get().getCurCh())) return;

            int chIdx=TChan.toFpgaChNo(WaveManage.get().getCurCh());
            if (CursorManage.getInstance().isScpiChanEqCursorChan()){
                chIdx=CursorManage.getInstance().getScpiChanIdx();
                CursorManage.getInstance().setScpiChanIdx(-1);
            }

            double y1 =Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1));
            double y2 =Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2));
            double pix1=Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/,y1);
            double pix2=Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/,y2);

            CursorManage.getInstance().setCursor(TChan.Cursor_row_1,(int)pix1);
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2,(int)pix2);
            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByScaleTrace: %s,%s",pix1,pix2 ));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setCursorByScaleTrace(int offsetY){
        try{
            if (CursorManage.getInstance().getRowVisible()==false) return;
            if (getInstance().enableCursorTrance==false) return;
//            if (CursorManage.getInstance().isRowCursorTrace()==false)  return;

            if (WorkModeManage.getInstance().isXyMode()) return;
            if (TChan.isSerial(WaveManage.get().getCurCh())) return;

            int chIdx=TChan.toFpgaChNo(WaveManage.get().getCurCh());
            if (isAutoSwitchChannel()==false){
                chIdx=CursorManage.getInstance().getCursorChan();
            }
            if (CursorManage.getInstance().isScpiChanEqCursorChan()){
                chIdx=CursorManage.getInstance().getScpiChanIdx();
                CursorManage.getInstance().setScpiChanIdx(-1);
            }

            double y1 =Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1));
            double y2 =Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2));
            double deltaY=Math.abs(y1-y2);

            double chEvery=0;
            if (ChannelFactory.isDynamicCh(chIdx)) {
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isMathCh(chIdx)) {
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isRefCh(chIdx)) {
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix();
            }
            y1= (y1-offsetY*chEvery);
            y2=(y2-offsetY*chEvery);

            double pix1=Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/,y1);
            double pix2=Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/,y2);

            if ((Math.abs(pix1-pix2)*chEvery)<=deltaY || !GlobalVar.get().isContainMainWaveY((int)pix1,(int)pix2)){
                CursorManage.getInstance().setCursorTrace(true);
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1,String.valueOf(y1));
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2,String.valueOf(y2));
            }
            CursorManage.getInstance().setCursor(TChan.Cursor_row_1,(int)pix1);
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2,(int)pix2);
            CursorManage.getInstance().setCursorTrace(false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            cursorManage_xy.refresh();
            cursorManage_yt.refresh();
        }
    };
}
