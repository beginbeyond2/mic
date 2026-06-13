package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.ui.util.svg.SvgManager;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by liwb on 2017/5/12.
 */

public class TriggerVoltageLine implements ITriggerLine {
    private static final String TAG = "TriggerVoltageLine";
    //资源图片
    /***
     * 第一维表示通道号，第二维表示图片类型，第一维的[0]不使用。从1表示与通道号一致
     */
    private Bitmap[][] resBmp = new Bitmap[TChan.MaxLogicChan + 2][6];
    private static final int trigger_ch = 0;
    private static final int trigger_ch_current = 1;
    private static final int trigger_ch_down = 2;
    private static final int trigger_ch_down_current = 3;
    private static final int trigger_ch_up = 4;
    private static final int trigger_ch_up_current = 5;

    //高低
//    public static final int  VoltageLine_High=1;
//    public static final int  VoltageLine_Low=2;

    public static final int TriggerVoltageLine_Logic_Hight = 0x01;
    public static final int TriggerVoltageLine_Logic_Low = 0x02;
    public static final int TriggerVoltageLine_Logic_None = 0x03;

    //region  图片
    private void initResBmp() {
        resBmp[TChan.Ch1][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch1][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch1][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch1][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch1][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch1][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch2][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch2][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch2][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch2][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch2][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch2][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch3][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch3][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch3][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch3][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch3][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch3][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch4][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch4][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch4][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch4][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch4][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch4][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch5][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch5][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch5][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch5][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch5][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch5][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch6][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch6][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch6][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch6][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch6][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch6][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch7][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch7][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch7][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch7][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch7][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch7][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch8][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch8][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch8][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);

        resBmp[TChan.Ch8 + 1][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8 + 1][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch8 + 1][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8 + 1][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
        resBmp[TChan.Ch8 + 1][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT);
        resBmp[TChan.Ch8 + 1][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT);
    }
    //endregion


    //region 属性
    private boolean bActive = true;
    private final String VoltageLineType;
    private int channelId = TChan.Ch1;
    private double offsetY;
    private int showMode = ITriggerLine.ShowMode_One;
    private boolean visibleLine = false;
    private boolean visible = true;

    private final Context context = App.get().getApplicationContext();
    private final ArrayList<DiscreetVoltageLineInfoBean> listShowChannelInfo = new ArrayList<>();

    private int[] TriggerVoltageLine_logic_state = new int[TChan.MaxLogicChan + 1];

    public void setTriggerVoltageLine_logic_state(int[] logic_state) {
        this.TriggerVoltageLine_logic_state = logic_state;
        draw();
    }

    /**
     * 当前显示的所有电平的显示位置合集， 保存的是1000对应的值
     */
    private double[] currYPos = new double[TChan.MaxLogicChan + 2];
    private int currYIndex = TChan.Ch1;
    private String text = "";

    private boolean isShowState = true;

    //endregion

    private Bitmap[] bmp = new Bitmap[TChan.MaxLogicChan + 2];
    private Bitmap[] oldBmp = new Bitmap[TChan.MaxLogicChan + 2];

    private Canvas[] mCanvas = new Canvas[TChan.MaxLogicChan + 2];
    private Bitmap bmpLine,oldBmpLine;
    private Canvas mCanvasLine;
    private final Paint paint;
    private boolean isChanageBitmap = false;
    private ICanvasGL canvasGL;

    public void onRefresh() {
        if (canvasGL != null) {
            canvasGL.onRefreshTexture();
        }
    }

    public TriggerVoltageLine(String VoltageLineType) {
        this.VoltageLineType = VoltageLineType;
        initResBmp();
        initPram();
        reInitBmp();
        paint = new Paint();
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(1);
        draw();
    }

    private void initPram() {
        Arrays.fill(currYPos, GlobalVar.get().getMainWave().y / 2);
    }

    //region  ITriggerLine接口处理
    public int getNameId() {
        return TChan.TriggerLevel;
    }

    public String getName() {
        return this.VoltageLineType;
    }


    private int getTrigger_ch1() {
        return bActive ? trigger_ch_current : trigger_ch;
    }

    private int getTrigger_ch1_down() {
        return bActive ? trigger_ch_down_current : trigger_ch_down;
    }

    private int getTrigger_ch1_up() {
        return bActive ? trigger_ch_up_current : trigger_ch_up;
    }

    @Override
    public void setActive(boolean bActive) {
        this.bActive = bActive;
        draw();
    }

    @Override
    public boolean isActive() {
        return bActive;
    }

    @Override
    public void setChannelId(int channelId) {
        if (this.channelId == channelId) return;
        this.channelId = channelId;
        draw();
    }

    @Override
    public int getChannelId() {
        return this.channelId;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public boolean setOffsetY(double offsetY) {
        if (!visible) {
//            return false;
            return setOffsetYHide(offsetY);
        }
        boolean change;
        double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY);
        if (temY < 0) {
            currYPos[currYIndex] = 0;
            change = true;
        } else if (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) < temY) {
            currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
            change = true;
        } else {
            this.offsetY = offsetY;
            currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff();
            change = false;
        }
        draw();
        return change;
    }

    private boolean setOffsetYHide(double offsetY) {
        boolean change = false;
        double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY);
        if (temY < 0) {
            currYPos[currYIndex] = 0;
            change = true;
        } else if (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) < temY) {
            currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
            change = true;
        } else {
            this.offsetY = offsetY;
            currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff();
        }
        return change;
    }

    @Override
    public boolean setCurrY(double currY) {
        if (!visible) return false;
        boolean change;
        if (currY < 0) {
            this.currYPos[currYIndex] = 0;
            change = true;
        } else if (currY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
            this.currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
            change = true;
        } else {
            this.currYPos[currYIndex] = currY * ScopeBase.getToFPGACoff();
            change = false;
        }
        draw();
        return change;
    }

    @Override
    public boolean setOtherY(int ch, double y) {
        if (!visible) {
//            return false;
            return setOtherYHide(ch, y);
        }
        boolean change;
        if (y < 0) {
            currYPos[ch] = 0;
            change = true;
        } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
            currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();;
            change = true;
        } else {
            this.currYPos[ch] = y * ScopeBase.getToFPGACoff();
            change = false;
        }
        draw();
        return change;
    }

    private boolean setOtherYHide(int ch, double y) {
        boolean change = false;
        if (y < 0) {
            currYPos[ch] = 0;
            change = true;
        } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
            currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
            change = true;
        } else {
            this.currYPos[ch] = y * ScopeBase.getToFPGACoff();
        }
        return change;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        draw();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public double getCurrY() {
        return ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff());
    }

    @Override
    public double getOtherY(int ch) {
        return ScopeBase.changeAccuracy(currYPos[ch] * ScopeBase.getToUICoff());
    }

    @Override
    public double[] getCurrYAll() {
        double[] temp = new double[TChan.MaxLogicChan + 2];
        for (int i = 0; i < currYPos.length; i++) {
            temp[i] = ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff());
        }
        return temp;
    }

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {
            case IWorkMode.WorkMode_YT:
                for (int i = 0; i < currYPos.length; i++) {
                    this.currYPos[i] = this.currYPos[i] * GlobalVar.get().toYTCoef();
                }
                break;
            case IWorkMode.WorkMode_YTZOOM:
                for (int i = 0; i < currYPos.length; i++) {
                    this.currYPos[i] = this.currYPos[i] * GlobalVar.get().toZoomCoef();
                }
                break;
        }
        //  Logger.i(TAG,"switchworkMode draw() currYPos:"+this.currYPos[currYIndex]+" ch:"+currYIndex);
        draw();
    }

    @Override
    public void setShowState(boolean show) {
        if (isShowState != show) {
            isShowState = show;
            draw();
        }
    }

    @Override
    public boolean getShowState() {
        return isShowState;
    }

    @Override
    public ArrayList<DiscreetVoltageLineInfoBean> getShowChannelInfo() {
        listShowChannelInfo.clear();
        if (showMode == ITriggerLine.ShowMode_One) {
            DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
            bean.ChannelId = channelId;
            bean.ShowMode = showMode;
            bean.VoltageLineChannelIndex = VoltageLine_Normal;
            bean.VoltageLineName = VoltageLineType;
            listShowChannelInfo.add(bean);
        } else if (showMode == ITriggerLine.ShowMode_Two) {
            DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
            bean.ChannelId = channelId;
            bean.ShowMode = showMode;
            bean.VoltageLineName = VoltageLineType;
            bean.VoltageLineChannelIndex = VoltageLine_High;
            listShowChannelInfo.add(bean);

            bean = new DiscreetVoltageLineInfoBean();
            bean.ChannelId = channelId;
            bean.ShowMode = showMode;
            bean.VoltageLineName = VoltageLineType;
            bean.VoltageLineChannelIndex = VoltageLine_Low;
            listShowChannelInfo.add(bean);
        } else if (showMode == ITriggerLine.ShowMode_Three) {
//            for (int i = 1; i < 5; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
//                bean.ChannelId = i;
//                bean.ShowMode = showMode;
//                bean.VoltageLineName = VoltageLineType;
//                bean.VoltageLineChannelIndex = VoltageLine_Normal;
//                listShowChannelInfo.add(bean);
//            }
            TChan.foreachChan((i) -> {
                        DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
                        bean.ChannelId = i;
                        bean.ShowMode = showMode;
                        bean.VoltageLineName = VoltageLineType;
                        bean.VoltageLineChannelIndex = VoltageLine_Normal;
                        listShowChannelInfo.add(bean);
                    }, (chan) ->
                            TriggerVoltageLine_logic_state[chan] == TriggerVoltageLine_Logic_None
            );

        }
        return listShowChannelInfo;
    }

    @Override
    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            canvas.drawBitmap(bmpLine, 0, currYPos[currYIndex], null);
//            switch (this.showMode) {
//                case ShowMode_One:
//                    drawOne(canvas);
//                    break;
//                case ShowMode_Two:
//                    drawTwo(canvas);
//                    break;
//                case ShowMode_Three:
//                    drawThree(canvas);
//                    break;
//            }
//        }
    }

    @Override
    public void draw(ICanvasGL canvas) {
        if (!isShowState) return;
        if (!visible) return;
        synchronized (bmp) {
            canvasGL = canvas;
            // if (isChanageBitmap) Logger.i(TAG,"draw ICanvas!");
            if (isChanageBitmap) {
                canvas.invalidateTextureContent(bmpLine,oldBmpLine);
                oldBmpLine = null;
            }

            canvas.drawBitmap(bmpLine, 0, (int) Math.round(ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff())));
            switch (this.showMode) {
                case ShowMode_One:
                    drawOne(canvas);
                    break;
                case ShowMode_Two:
                    drawTwo(canvas);
                    break;
                case ShowMode_Three:
                    drawThree(canvas);
                    break;
            }
            isChanageBitmap = false;
        }
    }

    private void drawOne(ICanvasGL canvas) {

        //上下限的限制
        if (isChanageBitmap) {
            canvas.invalidateTextureContent(bmp[currYIndex],oldBmp[currYIndex]);
            oldBmp[currYIndex] = null;
        }
        if (ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[1][trigger_ch_up_current].getHeight()) {
            int temY = resBmp[1][0].getHeight();
            temY = temY - (bmp[currYIndex].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[currYIndex], 0, temY);
        } else if (ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][trigger_ch_down_current].getHeight()) {
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
            temY = temY - (bmp[currYIndex].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[currYIndex], 0, temY);
        } else {
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff())) - (bmp[currYIndex].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[currYIndex], 0, temY);
        }
    }

    private void drawTwo(ICanvasGL canvas) {
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
        if (currYPos[1] > currYPos[2]) {
            if (currYIndex == 1) {
                currYPos[2] = currYPos[1];
            } else {
                currYPos[1] = currYPos[2];
            }
        }


        for (int i = 1; i < 3; i++) {
            if (isChanageBitmap){
                canvas.invalidateTextureContent(bmp[i],oldBmp[i]);
                oldBmp[i] = null;
            }
            if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                temY = temY - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            } else {
                int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            }
        }

    }

    private void drawThree(ICanvasGL canvas) {
//        for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//            if (isChanageBitmap) canvas.invalidateTextureContent(bmp[i]);
//            if (currYPos[i] < resBmp[i][getTrigger_ch1_up()].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            }
//        }
        TChan.foreachChan((i) -> {
            if (isChanageBitmap){
                canvas.invalidateTextureContent(bmp[i],oldBmp[i]);
                oldBmp[i] = null;
            }
            if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                temY = temY - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            } else {
                int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2;
                canvas.drawBitmap(bmp[i], 0, temY);
            }
        });


        int i = currYIndex;
        if (isChanageBitmap){
            canvas.invalidateTextureContent(bmp[i],oldBmp[i]);
            oldBmp[i] = null;
        }
        if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) {
            int temY = resBmp[1][0].getHeight();
            temY = temY - (bmp[i].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[i], 0, temY);
        } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) {
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
            temY = temY - (bmp[i].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[i], 0, temY);
        } else {
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2;
            canvas.drawBitmap(bmp[i], 0, temY);
        }


    }


    @Override
    public int getCurrYIndex() {
        return currYIndex;
    }

    /***
     * 设置当前序号
     * @param currYIndex {@link IWave IWave.Ch1 - IWave.Ch4}
     *                   或者{@link ITriggerLine ITriggerLine.VoltageLine_High - ITriggerLine.VoltageLine_Low}
     */
    @Override
    public void setCurrYIndex(int currYIndex) {
        this.currYIndex = currYIndex;
        draw();
    }

    @Override
    public void setShowMode(int showMode) {
        this.showMode = showMode;

        draw();
    }

    @Override
    public int getShowMode() {
        return this.showMode;
    }

    @Override
    public void setVisibleLine(boolean visibleLine) {
        if (visibleLine != this.visibleLine) {
            this.visibleLine = visibleLine;
            draw();
        }
    }

    @Override
    public boolean getVisibleLine() {
        return this.visibleLine;
    }

    @Override
    public void refresh() {
        reInitBmp();
        draw();
    }

    private
    @IWorkMode.WorkMode
    int workMode = IWorkMode.WorkMode_None;
    private void reInitBmp(){
        if(workMode != WorkModeManage.getInstance().getmWorkMode()){
            workMode = WorkModeManage.getInstance().getmWorkMode();
            TChan.foreachChan((chan) -> {
                oldBmp[chan] = bmp[chan];
                bmp[chan] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888);
                mCanvas[chan] = new Canvas(bmp[chan]);
            });

            //For Trigger Input
            oldBmp[TChan.Ch8 + 1] = bmp[TChan.Ch8 + 1];
            bmp[TChan.Ch8 + 1] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888);
            mCanvas[TChan.Ch8 + 1] = new Canvas(bmp[TChan.Ch8 + 1]);
            oldBmpLine = bmpLine;
            bmpLine = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 1, Bitmap.Config.ARGB_8888);
            mCanvasLine = new Canvas(bmpLine);
        }

    }

//endregion

    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    private void draw() {
        synchronized (bmp) {
            paint.setXfermode(clearMode);
            mCanvas[currYIndex].drawPaint(paint);
            mCanvasLine.drawPaint(paint);
            paint.setXfermode(srcMode);
            switch (showMode) {
                case ITriggerLine.ShowMode_One:
                    drawOne();
                    break;
                case ITriggerLine.ShowMode_Two:
                    drawTwo();
                    break;
                case ITriggerLine.ShowMode_Three:
                    drawThree();
                    break;
            }
            isChanageBitmap = true;
            onRefresh();
        }
    }


    private void drawOne() {
        if (this.visibleLine) {
            //显示
            if(channelId == TChan.Ch8 + 1) {
                paint.setColor(context.getResources().getColor(R.color.colorChCommon));
            } else {
                paint.setColor(TChan.getChannelColor(context, channelId));
            }
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint);
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[channelId][0].getHeight();
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()],
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()],
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(),
                        temY, paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0);
            } else {
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()],
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(),
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
            }

        } else {
            //不显示
            if(channelId == TChan.Ch8 + 1) {
                paint.setColor(context.getResources().getColor(R.color.colorChCommon));
            } else {
                paint.setColor(TChan.getChannelColor(context, channelId));
            }
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[channelId][0].getHeight();
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(),
                        temY, paint);
            } else {
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(),
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint);
            }

        }
    }

    private void drawTwo() {
        if (this.visibleLine) {
            paint.setColor(TChan.getChannelColor(context, channelId));
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint);

            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(),
                        temY, paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0);
            } else {
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(),
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint);
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
            }

            //如果高，就绘制低
            int temIndex;
            if (currYIndex == VoltageLine_High) {
                temIndex = VoltageLine_Low;
            } else {
                temIndex = VoltageLine_High;
            }
            paint.setXfermode(clearMode);
            mCanvas[temIndex].drawPaint(paint);
            paint.setXfermode(srcMode);

            if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][trigger_ch_up].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_up].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
                //drawText(35);
            } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getWidth(),
                        temY, paint);
                //drawText(0);
            } else {
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch].getWidth(),
                        (bmp[temIndex].getHeight() - resBmp[channelId][trigger_ch].getHeight()) / 2, paint);
                //drawText(35);
            }


        } else {
            //不显示线条
            paint.setColor(TChan.getChannelColor(context, channelId));
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(),
                        temY, paint);
            } else {
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(),
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint);
            }
            //如果不是当前的，就显示小图标
            int temIndex;
            if (currYIndex == VoltageLine_High) {
                temIndex = VoltageLine_Low;
            } else {
                temIndex = VoltageLine_High;
            }
            paint.setXfermode(clearMode);
            mCanvas[temIndex].drawPaint(paint);
            paint.setXfermode(srcMode);
            if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][trigger_ch_up].getHeight()) {
                int temY = resBmp[1][0].getHeight();
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_up].getWidth(),
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);

            } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getHeight()) {
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                temY = bmp[channelId].getHeight() - resBmp[channelId][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getWidth(),
                        temY, paint);

            } else {
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch].getWidth(),
                        (bmp[temIndex].getHeight() - resBmp[channelId][trigger_ch].getHeight()) / 2, paint);

            }

        }
    }

    private void drawThree() {
        if (this.visibleLine) {
            paint.setColor(TChan.getChannelColor(context, channelId));
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint);

            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
                paint.setXfermode(clearMode);
                mCanvas[i].drawPaint(paint);
                paint.setXfermode(srcMode);
                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
                if (i == currYIndex) {
                    if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
                        int temY = resBmp[1][0].getHeight();
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                    } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
                                temY, paint);
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0);
                    } else {
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                    }
                } else {
                    if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][trigger_ch_up].getHeight()) {
                        int temY = resBmp[1][0].getHeight();
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
                    } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
                                temY, paint);
                    } else {
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);

                    }
                }
            }
//            TChan.foreachChan((i) -> {
//                        paint.setXfermode(clearMode);
//                        mCanvas[i].drawPaint(paint);
//                        paint.setXfermode(srcMode);
////                      if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                        if (i == currYIndex) {
//                            if (this.currYPos[currYIndex] < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
//                                int temY = resBmp[1][0].getHeight();
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
//                                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
//                                drawText(this.currYPos[currYIndex], 35);
//                            } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
//                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                                temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
//                                        temY, paint);
//                                drawText(this.currYPos[currYIndex], 0);
//                            } else {
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
//                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);
//                                drawText(this.currYPos[currYIndex], 35);
//                            }
//                        } else {
//                            if (this.currYPos[i] < resBmp[i][trigger_ch_up].getHeight()) {
//                                int temY = resBmp[1][0].getHeight();
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
//                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                            } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
//                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                                temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
//                                        temY, paint);
//                            } else {
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
//                                        (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);
//
//                            }
//                        }
//                    }, (i) ->
//                            TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None
//            );

        } else {
            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
                paint.setXfermode(clearMode);
                mCanvas[i].drawPaint(paint);
                paint.setXfermode(srcMode);
                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
                if (i == currYIndex) {
                    if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
                        int temY = resBmp[1][0].getHeight();
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);

                    } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
                                temY, paint);

                    } else {
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);

                    }
                } else {
                    if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][trigger_ch_up].getHeight()) {
                        int temY = resBmp[1][0].getHeight();
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
                    } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
                                temY, paint);
                    } else {
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);

                    }
                }
            }
//            TChan.foreachChan((i) -> {
//                paint.setXfermode(clearMode);
//                mCanvas[i].drawPaint(paint);
//                paint.setXfermode(srcMode);
////                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (i == currYIndex) {
//                    if (this.currYPos[currYIndex] < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
//                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
//
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
//                                temY, paint);
//
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
//                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);
//
//                    }
//                } else {
//                    if (this.currYPos[i] < resBmp[i][trigger_ch_up].getHeight()) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
//                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);
//
//                    }
//                }
//            }, (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None);
        }
    }

    /***
     * 绘制文字
     * @param currY
     * @param drawTextY 绘制的起始Y坐标
     */
    private void drawText(int currY, int drawTextY) {
        int temp = 35;
//        if (channelId == TChan.Ch8 + 1) {
//            temp = 70;
//        }
        if (currY <= 50) {
            drawTextY = temp;
        } else if (currY >= GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - 50) {
            drawTextY = 0;
        } else {
            drawTextY = temp;
        }
        int x;
        Rect rect;
        rect = getTextHeight(text);
        x = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - rect.width() - 5;
        drawTextY = drawTextY + rect.height();
        mCanvas[currYIndex].drawText(text, x, drawTextY, paint);
    }

    private Rect rect = new Rect();

    private Rect getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return rect;
    }


}
