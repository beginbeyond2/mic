package com.micsig.tbook.tbookscope.wavezone.display;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwb on 2017/5/5.
 * 光标管理
 */


public class CursorManage_YT implements IWorkMode {
    private static final String TAG = "CursorManage_YT";
    //光标选择范围
    private static final int CURSOR_OFFSET_SELECT = 20;
    private static final int alpha = 0xFF;

    private List<Cursor_impIWave> curList = new ArrayList<Cursor_impIWave>();
    private Bitmap[][] resBmp;
    private CursorLabel cursorLabel;
    private Context context=App.get().getApplicationContext();

    public CursorManage_YT(Bitmap[][] resBmp) {

        this.resBmp = resBmp;
        double ytY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1);
        double ytY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2);
        int ytX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1);
        int ytX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2);
        ytY1 = ScopeBase.changeAccuracy(ytY1 * ScopeBase.getToUICoff());
        ytY2 = ScopeBase.changeAccuracy(ytY2 * ScopeBase.getToUICoff());
//        Logger.i(TAG, "CursorManage_YT() ==>"
//                +" ytY1:"+ytY1+" ytY2:"+ytY2+" ytX1:"+ytX1+" ytX2:"+ytX2);

        Cursor_impIWave cur = new Cursor_impIWave(this.resBmp, TChan.Cursor_row_1, IWorkMode.WorkMode_YT,true);
        cur.setLineNameId(TChan.Cursor_row_1);
        cur.setColor(Color.rgb(80, 80, 80));
        cur.setSelected(false);
        cur.setX(0);
        cur.setY(ytY1);
        cur.setVisible(true);
        curList.add(cur);

        cur = new Cursor_impIWave(this.resBmp, TChan.Cursor_row_2, IWorkMode.WorkMode_YT,true);
        cur.setLineNameId(TChan.Cursor_row_2);
        cur.setColor(Color.rgb(80, 80, 80));
        cur.setSelected(true);
        cur.setX(0);
        cur.setY(ytY2);
        cur.setVisible(true);
        curList.add(cur);

        cur = new Cursor_impIWave(this.resBmp, TChan.Cursor_col_1, IWorkMode.WorkMode_YT,true);
        cur.setLineNameId(TChan.Cursor_col_1);
        cur.setColor(Color.rgb(80, 80, 80));
        cur.setSelected(false);
        cur.setX(ytX1);
        cur.setY(0);
        cur.setVisible(true);
        curList.add(cur);

        cur = new Cursor_impIWave(this.resBmp, TChan.Cursor_col_2, IWorkMode.WorkMode_YT,true);
        cur.setLineNameId(TChan.Cursor_col_2);
        cur.setColor(Color.rgb(80, 80, 80));
        cur.setSelected(false);
        cur.setX(ytX2);
        cur.setY(0);
        cur.setVisible(true);
        curList.add(cur);

        CancelAllHightShow();
        cursorLabel=new CursorLabel(curList);
        cursorLabel.initMeasure();
    }

    //region 单例创建
//    private static class Cursor_Holder {
//        private static final CursorManage_YT instance = new CursorManage_YT();
//    }
//
//    public static final CursorManage_YT get() {
//        return Cursor_Holder.instance;
//    }

    public void draw(Canvas canvas) {
        for (Cursor_impIWave c : curList) {
            if (c.getVisible()) c.draw(canvas);
        }
        cursorLabel.drawMeasure();
    }

    public void draw(ICanvasGL canvas) {
        for (Cursor_impIWave c : curList) {
            if (c.getVisible()) c.draw(canvas);
        }
        cursorLabel.drawMeasure(canvas);
    }

    //endregion


    public void changeHeight(int height) {
        cursorLabel.changeHeight(height);
        for (Cursor_impIWave c : curList) {
            c.refresh();
        }
    }


    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        if (this.mWorkMode == workMode) return;
        this.mWorkMode = workMode;
        for (Cursor_impIWave c : curList) {
            c.switchWorkMode(workMode);
        }
        cursorLabel.refreshLabelPos();
    }

    //endregion


    //region 设置事件接口
    public void setData(){
        cursorLabel.setData();
    }
    public void setRowMeasureVisible(boolean b){
        cursorLabel.setRowMeasureVisible(b);
    }
    public void setColMeasureVisible(boolean b){
        cursorLabel.setColMeasureVisible(b);
    }
    public void MoveLabel(int offsetX,int offsetY){
        cursorLabel.MoveLabel(offsetX,offsetY);
    }
    public boolean getRowVisible() {
        for (Cursor_impIWave c : curList) {
            if (TChan.isCursorRow2(c.getCursorType()))
                return c.getVisible();
        }
        return false;
    }

    public boolean getCursorVisible(int cursorId){
        switch (cursorId){
            case TChan.Cursor_row_1:{
                return curList.get(0).getVisible();
            }
            case TChan.Cursor_row_2:{
                return curList.get(1).getVisible();
            }
            case TChan.Cursor_col_1:{
                return curList.get(2).getVisible();
            }
            case TChan.Cursor_col_2:{
                return curList.get(3).getVisible();
            }
        }
        return false;
    }
    public boolean getColVisible() {
        for (Cursor_impIWave c : curList) {
            if (TChan.isCursorCol2(c.getCursorType()))
                return c.getVisible();
        }
        return false;
    }

    public double getRow1MainUIPosition() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
            return curList.get(0).getY() * GlobalVar.get().toYTCoef();
        } else {
            return curList.get(0).getY();
        }
    }

    public double getRow2MainUIPosition() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
            return curList.get(1).getY() * GlobalVar.get().toYTCoef();
        } else {
            return curList.get(1).getY();
        }
    }

    public double getRow1Position() {
        return curList.get(0).getY();
    }

    public double getRow2Position() {
        return curList.get(1).getY();
    }

    public long getCol1Position() {
        return curList.get(2).getX();
    }

    public long getCol2Position() {
        return curList.get(3).getX();
    }

    /**
     * 是否行光标选中
     *
     * @return
     */
    public boolean isRowSelect() {
        return curList.get(0).isSelected() || curList.get(1).isSelected();
    }


    /***
     * 设置横向光标线是否显示
     * @param visible 是否显示光标线
     */
    public void setRowVisible(int workMode, boolean visible) {
        for (Cursor_impIWave c : curList) {
            if (TChan.isCursorRow2(c.getCursorType()))
                c.setVisible(visible);
        }
        MeasureManage.getInstance().setRowVisible(workMode, visible);
    }

    /***
     * 设置纵向光标线是否显示
     * @param visible
     */
    public void setColVisible(int workMode, boolean visible) {
        for (Cursor_impIWave c : curList) {
            if (TChan.isCursorCol2(c.getCursorType()))
                c.setVisible(visible);
        }
        cursorLabel.refreshLabelPos();
        MeasureManage.getInstance().setColVisible(workMode, visible);
    }

    /***
     * 设置光标切换事件。
     * @param onSelectChangeEvent
     */
    public void setOnSelectChangeEvent(IWave.OnSelectChangeEvent onSelectChangeEvent) {
        for (Cursor_impIWave c : curList) {
            c.setOnSelectChangeEvent(onSelectChangeEvent);
        }
    }

    /***
     * 设置光标移动事件
     * @param onMovingWaveEvent
     */
    public void setOnMovingWaveEvent(IWave.OnMovingWaveEvent onMovingWaveEvent) {
        for (Cursor_impIWave c : curList) {
            c.setOnMovingWaveEvent(onMovingWaveEvent);
        }
    }

    public void refresh() {
        for (Cursor_impIWave c : curList) {
            c.refresh();
        }
    }

    /***
     * 设置光标颜色
     * @param color
     */
    private void setCursorColor(int color) {
        for (Cursor_impIWave c : curList) {
            int cc = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
            c.setColor(cc);
//            c.setColor(color);
        }
    }

    public void setCursorChannelColor(int ChNo) {
        setCursorColor(TChan.getChannelColor(context,ChNo));
//        switch (ChNo) {
//            case IWave.Ch1:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch1));
//                break;
//            case IWave.Ch2:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch2));
//                break;
//            case IWave.Ch3:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch3));
//                break;
//            case IWave.Ch4:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch4));
//                break;
//            case IWave.Math1:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Math));
//                break;
//            case IWave.R1:
//            case IWave.R2:
//            case IWave.R3:
//            case IWave.R4:
//                setCursorColor(App.get().getResources().getColor(R.color.color_R1));
//                break;
//            case IWave.S1:
//                setCursorColor(App.get().getResources().getColor(R.color.color_S1));
//                break;
//            case IWave.S2:
//                setCursorColor(App.get().getResources().getColor(R.color.color_S2));
//                break;
//            default:
//                setCursorColor(Color.GRAY);
//                break;
//        }

    }

    /***
     * 加一个像素
     */
    public void addPixMove() {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                c.movePix(1);
            }
        }
    }

    /**
     * 加减num个像素
     *
     * @param num 为正增加，为负减少
     */
    public void addPixMove(int num) {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                c.movePix(num);
            }
        }
    }

    /**
     * 如果是正数，则两条光标线向中间聚拢，如果是负数，则向两边扩散...
     */
    public void zoomPixMove(int num) {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                if (c.getCursorType() == TChan.Cursor_col_1 || c.getCursorType() == TChan.Cursor_row_1) {
                    c.movePix(num);
                }
                if (c.getCursorType() == TChan.Cursor_col_2 || c.getCursorType() == TChan.Cursor_row_2) {
                    c.movePix(num * -1);
                }
            }
        }
    }

    /***
     *减一个像素
     */
    public void subPixMove() {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                c.movePix(-1);
            }
        }
    }

    public void initCursorX() {
        for (Cursor_impIWave c : curList) {
            if (c.getCursorType() == TChan.Cursor_col_1) {
                c.setX(GlobalVar.get().getMainWave().x / 4);
            }
            if (c.getCursorType() == TChan.Cursor_col_2) {
                c.setX(GlobalVar.get().getMainWave().x / 4 * 3);
            }
        }
    }

    public void initCursorY() {
        for (Cursor_impIWave c : curList) {
            if (c.getCursorType() == TChan.Cursor_row_1) {
                c.setY(GlobalVar.get().getWaveZoneHeight_Pix(mWorkMode) * 1.0f / 4);
            }
            if (c.getCursorType() == TChan.Cursor_row_2) {
                c.setY(GlobalVar.get().getWaveZoneHeight_Pix(mWorkMode) * 1.0f / 4 * 3);
            }
        }
    }
//    public void initCursorY() {
//        for (Cursor_impIWave c : curList) {
//            if (c.getCursorType() == IWave.Cursor_row_1) {
//                c.setY(GlobalVar.get().getMainWave().y / 4);
//            }
//            if (c.getCursorType() == IWave.Cursor_row_2) {
//                c.setY(GlobalVar.get().getMainWave().y / 4 * 3);
//            }
//        }
//    }

    public void setCursor(int cursorType, double position) {
        for (Cursor_impIWave c : curList) {
            if (c.getCursorType() == cursorType) {
                if (TChan.isCursorCol2(cursorType)) {
                    c.setX(Math.round(position));
                } else if (TChan.isCursorRow2(cursorType)) {
                    c.setY(position);
                }
            }
        }
    }
    public void setCursorOffsetPos(int cursorType,int offset){
        for (Cursor_impIWave c : curList) {
            if (c.getCursorType() == cursorType) {
                if (TChan.isCursorCol2(cursorType)) {
                    c.movePix(offset);
                } else if (TChan.isCursorRow2(cursorType)) {
                    c.movePix(offset);
                }
            }
        }
    }

    public double getCursor(int cursorType) {
        for (Cursor_impIWave c : curList) {
            if (c.getCursorType() == cursorType) {
                if (TChan.isCursorCol2(cursorType)) {
                    return c.getX();
                } else if (TChan.isCursorRow2(cursorType)) {
                    return c.getY();
                }
            }
        }
        return 0;
    }

    //endregion


    //region  桌面操作接口
    public void CancelAllHightShow() {
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            int color = c.getColor();
            int cc = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
            c.setColor(cc);
        }
    }

    public void setSelectHighColor(int index) {
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (c.getLineNameID() == index
                    || (index==TChan.Cursor_col_4 && (TChan.isCursorCol2(c.getLineNameID())))
                    || (index==TChan.Cursor_row_4 && (TChan.isCursorRow2(c.getLineNameID())))
            ) {
                int color = c.getColor();
                int cc = Color.argb(0xFF, Color.red(color), Color.green(color), Color.blue(color));
                c.setColor(cc);
            }
        }
    }

    /***
     * 返回选择的光标线
     * @param x 按下的x坐标
     * @param y 按下的Y坐标
     * @return 返回-1 没有选中，大于等于0则选中一个。
     */
    public int selectCursor(int x, double y) {
        int finalSelect = cursorLabel.selectMeasure(x, (int) Math.round(y));
        if (finalSelect >= 0) return finalSelect;
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (!c.getVisible()) continue;
            if (TChan.isCursorCol2(c.getCursorType())) {
                if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) {
                    finalSelect = c.getCursorType();
                }
            } else if (TChan.isCursorRow2(c.getCursorType())) {
                if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) {
                    finalSelect = c.getCursorType();
                }
            }
        }
        return finalSelect;
    }
    public int selectCursor(int x, int y,int cursorId) {
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (!c.getVisible()) continue;
            if (cursorId==TChan.Cursor_all){
                if (TChan.isCursorCol2(c.getCursorType())) {
                    if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) return c.getCursorType();
                } else if (TChan.isCursorRow2(c.getCursorType())) {
                    if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) return c.getCursorType();
                }
            }else if (cursorId==TChan.Cursor_row_3 || cursorId==TChan.Cursor_row_4){
                if (TChan.isCursorRow2(c.getCursorType())) {
                    if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) return c.getCursorType();
                }
            }else if (cursorId==TChan.Cursor_col_3 || cursorId==TChan.Cursor_col_4){
                if (TChan.isCursorCol2(c.getCursorType())) {
                    if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) return c.getCursorType();
                }
            }
        }
        return -1;
    }


    /***
     * 设置选择的光标
     * @param index 光标的序号
     */
    public void setSelectCursor(int index) {
//        Logger.i(TAG, "setSelectCursor:0x"+Integer.toHexString(index));
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (c.getCursorType() == index) {
                c.setSelected(true);
            } else {
                c.setSelected(false);
            }
        }
    }

    /***
     * 多选择光标
     * @param index1
     * @param index2
     */
    public void setMultiSelectCursor(int index1, int index2) {
//        Logger.i(TAG, "setMultiSelectCursor: [1]0x"+Integer.toHexString(index1)+" - [2]0x"+Integer.toHexString(index2));
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (c.getCursorType() == index1 || c.getCursorType() == index2) {
                c.setSelected(true);
            } else {
                c.setSelected(false);
            }
        }
    }
    public void setMultiSelectCursor(int index1, int index2,int index3,int index4) {
//        Logger.i(TAG, "setMultiSelectCursor: [1]0x"+Integer.toHexString(index1)+" - [2]0x"+Integer.toHexString(index2));
        for (int i = 0; i < curList.size(); i++) {
            Cursor_impIWave c = curList.get(i);
            if (c.getCursorType() == index1 || c.getCursorType() == index2 || c.getCursorType()==index3 || c.getCursorType()==index4) {
                c.setSelected(true);
            } else {
                c.setSelected(false);
            }
        }
    }

    /***
     * 移动光标
     * @param x
     * @param y
     */
    public void moveSelectCursor(int x, double y) {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                if (TChan.isCursorRow2(c.getCursorType())) {
                    c.setY(c.getY() - y);
                } else if (TChan.isCursorCol2(c.getCursorType())) {
                    c.setX(c.getX() - x);
                }
            }
        }
    }

    /***
     * 多光标线移动  相对坐标
     * @param x
     * @param y
     */
    public void moveMultiSelectCursor(int x, int y) {
        for (Cursor_impIWave c : curList) {
            if (c.isSelected() == true) {
                if (TChan.isCursorRow2(c.getCursorType())) {
                    c.setY(c.getY() - y);  //c.setY(y);
                }
                if (TChan.isCursorCol2(c.getCursorType())) {
                    c.setX(c.getX() - x); //c.setX(x);
                }
            }
        }
    }

    public int getMultiCursorSelect(int x,int y){
        if (getRowVisible() && getColVisible()){
            if (curList.get(0).isSelected() && curList.get(1).isSelected()
            && curList.get(2).isSelected() && curList.get(3).isSelected()){
                return TChan.Cursor_all;
            }else if (curList.get(0).isSelected() && curList.get(1).isSelected()){
                return TChan.Cursor_row_4;
            }else if (curList.get(2).isSelected() && curList.get(3).isSelected()){
                return TChan.Cursor_col_4;
            }

        }else if (getRowVisible()){
            if (curList.get(0).isSelected() && curList.get(1).isSelected()){
                return TChan.Cursor_row_4;
            }
        }else if (getColVisible()){
            if (curList.get(2).isSelected() && curList.get(3).isSelected()){
                return TChan.Cursor_col_4;
            }
        }
        return -1;
    }
    public int getCurrSelectCursor() {
        int select = 0;
        if (getRowVisible() && getColVisible()) {

            if (curList.get(0).isSelected() && curList.get(1).isSelected()){
                select=TChan.Cursor_row_3;
            }else if (curList.get(2).isSelected() && curList.get(3).isSelected()){
                select=TChan.Cursor_col_3;
            }else{
                for (Cursor_impIWave c : curList) {
                    if (c.isSelected()) {
                        select = c.getLineNameID();
                        break;
                    }
                }
            }
            if (select == 0) {
                select = TChan.Cursor_row_1;
                setSelectCursor(select);
            }
        } else if (getRowVisible()) {
            if (curList.get(0).isSelected() && curList.get(1).isSelected()){
                select= TChan.Cursor_row_3;
            }else if (curList.get(0).isSelected()) select = TChan.Cursor_row_1;
            else if (curList.get(1).isSelected()) select = TChan.Cursor_row_2;
            else {
                select = TChan.Cursor_row_1;
                setSelectCursor(select);
            }
        } else if (getColVisible()) {
            if (curList.get(2).isSelected() && curList.get(3).isSelected()){
                select=TChan.Cursor_col_3;
            }else if (curList.get(2).isSelected()) {
                select = TChan.Cursor_col_1;
            } else if (curList.get(3).isSelected()) {
                select = TChan.Cursor_col_2;
            } else {
                select = TChan.Cursor_col_1;
                setSelectCursor(select);
            }
        } else {
            select = 0;
        }
        return select;
    }
    //endregion

}
