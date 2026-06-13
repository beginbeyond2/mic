package com.micsig.tbook.tbookscope.wavezone.measure;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;

/**
 * Created by liwb on 2017/11/6.
 */

public class CursorMeasureManage implements IWorkMode {
    private CursorMeasure_XY cursorMeasure_xy;
    private CursorMeasure_YT cursorMeasure_yt;

    public CursorMeasureManage() {
        cursorMeasure_xy = new CursorMeasure_XY();
        cursorMeasure_yt = new CursorMeasure_YT();
    }

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.switchWorkMode(workMode);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.switchWorkMode(workMode);
                break;
        }
    }

    public void draw(ICanvasGL canvas) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.draw(canvas);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.draw(canvas);
                break;
        }
    }

    public void setCursorChannelColor(int ChNo) {
        cursorMeasure_yt.setChannelId(ChNo);
        cursorMeasure_xy.setChannelId(ChNo);
    }

    public void setRowVisible(boolean rowVisible) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.setRowCursorVisible(rowVisible);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.setRowCursorVisible(rowVisible);
                break;
        }
    }

    public void setColVisible(boolean colVisible) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.setColCursorVisible(colVisible);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.setColCursorVisible(colVisible);
                break;
        }
    }

    public void setRowVisible(int workMode, boolean rowVisible) {
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.setRowCursorVisible(false);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.setRowCursorVisible(rowVisible);
                break;
        }
    }

    public void setColVisible(int workMode, boolean colVisible) {
        switch (workMode) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.setColCursorVisible(false);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.setColCursorVisible(colVisible);
                break;
        }
    }

    public void setParam(String row1, String row2, String detaRow, String col1, String col2, String detaCol,
                         String detaTCol, String S) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {
            case WorkMode_YT:
            case WorkMode_YTZOOM:
                cursorMeasure_yt.setParam(row1, row2, detaRow, col1, col2, detaCol, detaTCol, S);
                break;
            case WorkMode_XY:
                cursorMeasure_xy.setParam(row1, row2, detaRow, col1, col2, detaCol, detaTCol, S);
                break;
        }
    }
}
