package com.micsig.tbook.tbookscope.structdata;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.CheckBox;

import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterSegmented;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysSimulateClick;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.ui.main.AnimationView;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2017/12/11.
 * 外部按键 ,界面处理部分
 */

public class ExternalKeysUI {

    //region 单例�?
    private static class ExternalKeysUIHolder {
        public static final ExternalKeysUI instance = new ExternalKeysUI();
    }

    public static ExternalKeysUI getInstance() {
        return ExternalKeysUI.ExternalKeysUIHolder.instance;
    }
    //endregion

    private ExternalKeysSimulateClick click = new ExternalKeysSimulateClick();
    private MainViewGroup mainViewGroup;
    private MainLayoutCenterChannel channelWindow;
    private MainLayoutCenterSegmented segmentWindow;
    private CheckBox channelBtn;
    private AnimationView focusControl, focusControlBack;
    private View zoomUpLayout;
//    private TextureView textureView;

    public void setMainViewGroup(MainViewGroup mainViewGroup) {
        this.mainViewGroup = mainViewGroup;
        focusControl = (AnimationView) mainViewGroup.findViewById(R.id.focusControl);
        focusControlBack = (AnimationView) mainViewGroup.findViewById(R.id.focusControlBack);
    }

    public boolean isDialogsShow() {
        return mainViewGroup.isDialogsShow();
    }

    public void setFocusViewVisible(boolean visible) {
        if (visible && (mainViewGroup.isSlipShow() || mainViewGroup.isDialogsShow()
                || mainViewGroup.getChannelsLayout().getVisibility()==View.VISIBLE
                || mainViewGroup.getCenterSegmentedLayout().getVisibility()==View.VISIBLE
                || mainViewGroup.getSerialWorkLayout().getVisibility()==View.VISIBLE)
                ) {
            focusControl.setVisibility(View.VISIBLE);
        } else {
            focusControl.setVisibility(View.GONE);
            focusControlBack.setVisibility(View.GONE);
        }
    }

    public void setFocusControlBackVisible(boolean visible) {
        if (visible) {
            focusControlBack.setVisibility(View.VISIBLE);
        } else {
            focusControlBack.setVisibility(View.GONE);
        }
    }

    public void setVisibleSelectView() {
        click.setImitateClick(false);
    }

    public boolean isVisibleSelectView() {
        return click.isImitateClick();
    }

    public void onDrag_downMove(boolean rightWard) {
        checkChannelWindow(710, 300);
        if (rightWard) {
            click.drag_downMove(710, 300, 710, 301);
        } else {
            click.drag_downMove(710, 300, 710, 299);
        }
    }

    public boolean isVisibleFocusControl() {
        return focusControl.getVisibility() == View.VISIBLE;
    }

    public boolean isVisibleFocusControlBack() {
        return focusControlBack.getVisibility() == View.VISIBLE;
    }

    public void showBackStateFocus(int backState) {
        Rect rect = null;
        if (backState == ExternalKeysProtocol.BACKSTATE_CURSOR) {
            focusControlBack.setVisibility(View.VISIBLE);
            rect = getCurCursorRect();
        } else if (backState == ExternalKeysProtocol.BACKSTATE_CHLIST) {
//            if (focusControlBack.getVisibility() == View.VISIBLE) {
//                focusControlBack.setVisibility(View.GONE);
//            } else {
//                focusControlBack.setVisibility(View.VISIBLE);
//            }
            focusControlBack.setVisibility(View.VISIBLE);
            rect = getChListRect();
            rect.set(rect.left-2,rect.top-2,rect.right+2,rect.bottom+2);
        }else if (backState==ExternalKeysProtocol.BACKSTATE_SEGMENT){
//            if (focusControlBack.getVisibility()==View.VISIBLE){
//                focusControlBack.setVisibility(View.GONE);
//            }else {
//                focusControlBack.setVisibility(View.VISIBLE);
//            }

            focusControlBack.setVisibility(View.VISIBLE);
            rect=getSegmentRect();
            rect.set(rect.left-2,rect.top-2,rect.right+2,rect.bottom+2);
        }
        else if (backState == ExternalKeysProtocol.BACKSTATE_ZOOMUP) {
            focusControlBack.setVisibility(View.VISIBLE);
            rect = getZoomUpRect();
        } else {
            focusControlBack.setVisibility(View.GONE);
        }
        if (rect != null) {
            focusControlBack.setX(rect.left);
            focusControlBack.setY(rect.top);
            ViewGroup.LayoutParams layoutParams = focusControlBack.getLayoutParams();
            layoutParams.width = rect.width();
            layoutParams.height = rect.height();
            focusControlBack.setLayoutParams(layoutParams);
            focusControlBack.bringToFront();
        }
    }

    private Rect getChListRect() {
        if (channelWindow == null) {
            channelWindow = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels);
        }
        return Tools.getViewRect(channelWindow);
    }
    private Rect getSegmentRect(){
        if (segmentWindow==null){
            segmentWindow=(MainLayoutCenterSegmented) mainViewGroup.findViewById(R.id.mainLayoutCenterSegmented);
        }
        return Tools.getViewRect(segmentWindow);
    }

    private Rect getZoomUpRect() {
        if (zoomUpLayout == null) {
            zoomUpLayout = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zoom);
        }
        return Tools.getViewRect(zoomUpLayout);
    }

    private Rect getCurCursorRect() {
//        if (textureView == null) {
//            textureView = (TextureView) mainViewGroup.findViewById(R.id.textureView);
//        }
        if (zoomUpLayout == null) {
            zoomUpLayout = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zoom);
        }
        int zoomHeight = Tools.getViewRect(zoomUpLayout).height();
//        Rect textureRect = Tools.getViewRect(textureView);
        int topHeight= mainViewGroup.findViewById(R.id.topstatus).getHeight();
        int leftBarWidth=0;
        int rectRight= GlobalVar.get().getMainWave().x+leftBarWidth;
        int rectBottom=GlobalVar.get().getMainWave().y+topHeight;
        Rect textureRect = new Rect(leftBarWidth, topHeight, rectRight, rectBottom);
        boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
        int selectCursor = CursorManage.getInstance().getCurrSelectCursor();
        int xStart = textureRect.left;
        int yStart = textureRect.top;
        int x1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1);
        int x2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2);
        double y1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1);
        double y2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2);
        y1 = ScopeBase.changeAccuracy(y1 * ScopeBase.getToUICoff());
        y2 = ScopeBase.changeAccuracy(y2 * ScopeBase.getToUICoff());
        int halfWidth = 8;
        if (selectCursor == TChan.Cursor_col_1) {//竖直方向...
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top;
            return new Rect(x1 + xStart - halfWidth, top, x1 + xStart + halfWidth, textureRect.bottom);
        } else if (selectCursor == TChan.Cursor_col_2) {//竖直方向...
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top;
            return new Rect(x2 + xStart - halfWidth, top, x2 + xStart + halfWidth, textureRect.bottom);
        } else if (selectCursor == TChan.Cursor_col_3) {//竖直方向...
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top;
            return new Rect(x1 + xStart - halfWidth, top, x2 + xStart + halfWidth, textureRect.bottom);
        } else if (selectCursor == TChan.Cursor_row_1) {//水平方向...
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y1) : textureRect.top + y1);
            return new Rect(xStart, top - halfWidth, textureRect.right, top + halfWidth);
        } else if (selectCursor == TChan.Cursor_row_2) {//水平方向...
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y2) : textureRect.top + y2);
            return new Rect(xStart, top - halfWidth, textureRect.right, top + halfWidth);
        } else if (selectCursor == TChan.Cursor_row_3) {//水平方向...
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y1) : textureRect.top + y1);
            int bottom = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y2) : textureRect.top + y2);
            return new Rect(xStart, top - halfWidth, textureRect.right, bottom + halfWidth);
        }
        return new Rect(0, 0, 0, 0);
    }

    /**
     * 模拟点击，如果被channelWindow覆盖，则先隐藏之
     */
    public void onClick(int x, int y) {
        click(x, y);
    }

    /**
     * 模拟点击，但是不隐藏channelWindow
     */
    public void onClickForChannelWindow(int x, int y) {
        click.click(x, y, null);
    }


    private void click(int x, int y) {
        checkChannelWindow(x, y);
        click.click(x, y, null);
    }

    private void checkChannelWindow(int x, int y) {
        if (channelWindow == null || channelBtn == null) {
            channelWindow = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels);
            channelBtn = (CheckBox) mainViewGroup.findViewById(R.id.current);
        }
        if (channelWindow.getVisibility() == View.VISIBLE && channelWindow.containsPoint(x, y)) {
            channelWindow.setVisibility(View.GONE);
            channelBtn.setChecked(false);
        }
    }
}