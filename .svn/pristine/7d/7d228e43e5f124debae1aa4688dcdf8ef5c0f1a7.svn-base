package com.micsig.tbook.tbookscope.wavezone.display;

import android.content.Context;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;

public class WaveMaskLayer_YTZoomAction {
    private static final String TAG = "WaveMaskLayer_YTZoomAction";
    private Context context;
    WaveMaskLayer_YTZoom waveMaskLayer_ytZoom = null;

    public WaveMaskLayer_YTZoomAction(Context context) {
        this.context = context;
        waveMaskLayer_ytZoom = WaveMaskLayer_YTZoom.getInstance();
        waveMaskLayer_ytZoom.setOnZoomEventListenerListener(onZoomEventListener);
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_ZOOM_ENTER, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_POS, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_UI_SAMPLE_GRAPH, eventUIObserver);
    }

    private void setExternalKeyBackStateFocus(){
        ExternalKeysProtocol.BackStateUpdate(ExternalKeysProtocol.BACKSTATE_ZOOMUP);
    }
    private WaveMaskLayer_YTZoom.OnZoomEventListener onZoomEventListener = new WaveMaskLayer_YTZoom.OnZoomEventListener() {
        @Override
        public void onLayerXChangeEvent(int offset) {
            CacheUtil.get().setLastObjectIsCursor(false);
            setExternalKeyBackStateFocus();
            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
            long lx = horizontalAxis.getTimePose(HorizontalAxis.WPI_STANDARD, offset);
            horizontalAxis.setTimePosOfView(horizontalAxis.getTimePosOfView() + lx);
            horizontalAxis.correctTimePose_poseMove();
            long pix = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
            TriggerTimebase.getInstance().setX_disFromEventBus(pix);
        }
    };


    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            boolean updata = false;
            if (!Scope.getInstance().isZoom())
                return;

            if (((EventBase) data).getId() == EventFactory.EVENT_DISPLAY_ZOOM_ENTER) {
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();

                waveMaskLayer_ytZoom.setLayerTimebaseX(ScopeBase.getWidth() / 2 - horizontalAxis.getTimePoseOfGrid(HorizontalAxis.WPI_STANDARD));

                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE);
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx);
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx;
                waveMaskLayer_ytZoom.setLayerX(lx);

                //计算宽度
                double dx = Scope.getInstance().screenNum_zoom();
                int width = (int) (ScopeBase.getWidth() / dx);
                waveMaskLayer_ytZoom.setLayerWidth(width);

                updata = true;
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_TIME_SCALE) {
                double dx = Scope.getInstance().screenNum_zoom();
                int width = (int) (ScopeBase.getWidth() / dx);
                waveMaskLayer_ytZoom.setLayerWidth(width);
                updata = true;
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_TIME_POS) {
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE);
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx);
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx;
                waveMaskLayer_ytZoom.setLayerX(lx);
                updata = true;
            }
            if (((EventBase) data).getId() == EventFactory.EVENT_UI_SAMPLE_GRAPH) {
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();


                long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL);
                long tempTomePos = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD);
                if (tempTomePos != ScopeBase.getWidth() / 2 - l) {
                    horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, ScopeBase.getWidth() / 2 - l);
                }

                long layerTimebaseX = ScopeBase.getWidth() / 2 - horizontalAxis.getTimePoseOfGrid(HorizontalAxis.WPI_STANDARD);
                if (waveMaskLayer_ytZoom.layerTimebaseX != layerTimebaseX) {
                    waveMaskLayer_ytZoom.setLayerTimebaseX(layerTimebaseX);
                }

                //计算放大窗口的中心位置距离触发位置的时间
                long lx = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD) - horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE);
                //把这个时间换算为像素
                lx = horizontalAxis.getTimePoseOfGrid(horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)), lx);
                //计算位置
                lx = ScopeBase.getWidth() / 2 - lx;
                waveMaskLayer_ytZoom.setLayerX(lx);

                double dx = Scope.getInstance().screenNum_zoom();
                int width = (int) (ScopeBase.getWidth() / dx);
                if (width != waveMaskLayer_ytZoom.layerWidth) {
                    waveMaskLayer_ytZoom.setLayerWidth(width);
                }

                updata = true;
            }
            if (updata)
                waveMaskLayer_ytZoom.draw();
        }
    };
}
