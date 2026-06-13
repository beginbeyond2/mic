package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by liwb on 2017/5/17.
 * 预值电平
 */

public class DiscreetVoltageLine implements ITriggerLine {
    private static final String TAG = "DiscreetVoltageLine";


    //region 初始化资源
    private static final int discreet_front_l = 0;
    private static final int discreet_front_l_down = 1;
    private static final int discreet_front_l_up = 2;
    private static final int discreet_front_normal = 3;
    private static final int discreet_front_normal_down = 4;
    private static final int discreet_front_normal_up = 5;
    private static final int discreet_l = 6;
    private static final int discreet_l_down = 7;
    private static final int discreet_l_up = 8;
    private static final int discreet_normal = 9;
    private static final int discreet_normal_down = 10;
    private static final int discreet_normal_up = 11;
    //4个通道，加12种状态
    private Bitmap resBmp[][] = new Bitmap[TChan.MaxLogicChan + 1][12];

    private void initRes() {
        resBmp[TChan.Ch1][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l)).getBitmap();
        resBmp[TChan.Ch1][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l_down)).getBitmap();
        resBmp[TChan.Ch1][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l_up)).getBitmap();
        resBmp[TChan.Ch1][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal)).getBitmap();
        resBmp[TChan.Ch1][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal_down)).getBitmap();
        resBmp[TChan.Ch1][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal_up)).getBitmap();
        resBmp[TChan.Ch1][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l)).getBitmap();
        resBmp[TChan.Ch1][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l_down)).getBitmap();
        resBmp[TChan.Ch1][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l_up)).getBitmap();
        resBmp[TChan.Ch1][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal)).getBitmap();
        resBmp[TChan.Ch1][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal_down)).getBitmap();
        resBmp[TChan.Ch1][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal_up)).getBitmap();

        resBmp[TChan.Ch2][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l)).getBitmap();
        resBmp[TChan.Ch2][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l_down)).getBitmap();
        resBmp[TChan.Ch2][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l_up)).getBitmap();
        resBmp[TChan.Ch2][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal)).getBitmap();
        resBmp[TChan.Ch2][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal_down)).getBitmap();
        resBmp[TChan.Ch2][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal_up)).getBitmap();
        resBmp[TChan.Ch2][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l)).getBitmap();
        resBmp[TChan.Ch2][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l_down)).getBitmap();
        resBmp[TChan.Ch2][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l_up)).getBitmap();
        resBmp[TChan.Ch2][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal)).getBitmap();
        resBmp[TChan.Ch2][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal_down)).getBitmap();
        resBmp[TChan.Ch2][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal_up)).getBitmap();

        resBmp[TChan.Ch3][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l)).getBitmap();
        resBmp[TChan.Ch3][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l_down)).getBitmap();
        resBmp[TChan.Ch3][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l_up)).getBitmap();
        resBmp[TChan.Ch3][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal)).getBitmap();
        resBmp[TChan.Ch3][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal_down)).getBitmap();
        resBmp[TChan.Ch3][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal_up)).getBitmap();
        resBmp[TChan.Ch3][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l)).getBitmap();
        resBmp[TChan.Ch3][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l_down)).getBitmap();
        resBmp[TChan.Ch3][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l_up)).getBitmap();
        resBmp[TChan.Ch3][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal)).getBitmap();
        resBmp[TChan.Ch3][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal_down)).getBitmap();
        resBmp[TChan.Ch3][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal_up)).getBitmap();

        resBmp[TChan.Ch4][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l)).getBitmap();
        resBmp[TChan.Ch4][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l_down)).getBitmap();
        resBmp[TChan.Ch4][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l_up)).getBitmap();
        resBmp[TChan.Ch4][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal)).getBitmap();
        resBmp[TChan.Ch4][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal_down)).getBitmap();
        resBmp[TChan.Ch4][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal_up)).getBitmap();
        resBmp[TChan.Ch4][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l)).getBitmap();
        resBmp[TChan.Ch4][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l_down)).getBitmap();
        resBmp[TChan.Ch4][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l_up)).getBitmap();
        resBmp[TChan.Ch4][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal)).getBitmap();
        resBmp[TChan.Ch4][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal_down)).getBitmap();
        resBmp[TChan.Ch4][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal_up)).getBitmap();

        resBmp[TChan.Ch5][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l);
        resBmp[TChan.Ch5][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l_down);
        resBmp[TChan.Ch5][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l_up);
        resBmp[TChan.Ch5][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal);
        resBmp[TChan.Ch5][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal_down);
        resBmp[TChan.Ch5][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal_up);
        resBmp[TChan.Ch5][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch5_l);
        resBmp[TChan.Ch5][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_l_down);
        resBmp[TChan.Ch5][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_l_up);
        resBmp[TChan.Ch5][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal);
        resBmp[TChan.Ch5][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal_down);
        resBmp[TChan.Ch5][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal_up);

        resBmp[TChan.Ch6][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l);
        resBmp[TChan.Ch6][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l_down);
        resBmp[TChan.Ch6][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l_up);
        resBmp[TChan.Ch6][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal);
        resBmp[TChan.Ch6][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal_down);
        resBmp[TChan.Ch6][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal_up);
        resBmp[TChan.Ch6][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch6_l);
        resBmp[TChan.Ch6][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_l_down);
        resBmp[TChan.Ch6][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_l_up);
        resBmp[TChan.Ch6][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal);
        resBmp[TChan.Ch6][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal_down);
        resBmp[TChan.Ch6][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal_up);

        resBmp[TChan.Ch7][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l);
        resBmp[TChan.Ch7][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l_down);
        resBmp[TChan.Ch7][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l_up);
        resBmp[TChan.Ch7][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal);
        resBmp[TChan.Ch7][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal_down);
        resBmp[TChan.Ch7][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal_up);
        resBmp[TChan.Ch7][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch7_l);
        resBmp[TChan.Ch7][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_l_down);
        resBmp[TChan.Ch7][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_l_up);
        resBmp[TChan.Ch7][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal);
        resBmp[TChan.Ch7][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal_down);
        resBmp[TChan.Ch7][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal_up);

        resBmp[TChan.Ch8][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l);
        resBmp[TChan.Ch8][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l_down);
        resBmp[TChan.Ch8][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l_up);
        resBmp[TChan.Ch8][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal);
        resBmp[TChan.Ch8][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal_down);
        resBmp[TChan.Ch8][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal_up);
        resBmp[TChan.Ch8][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch8_l);
        resBmp[TChan.Ch8][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_l_down);
        resBmp[TChan.Ch8][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_l_up);
        resBmp[TChan.Ch8][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal);
        resBmp[TChan.Ch8][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal_down);
        resBmp[TChan.Ch8][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal_up);

    }
    //endregion


    //region 属性
    private String VoltageLineType;
    private int showMode = ITriggerLine.ShowMode_One;
    /**
     * 电平线的显现性
     */
    private boolean visibleLine = false;
    /**
     * 整个控件的显现性
     */
    private boolean visible = true;
    /**
     * 当前显示电平的通道号
     */
    private int channelId = TChan.Ch1;
    /**
     * 当前显示的所有电平的显示位置合集
     */
    private final double[] currYPos = new double[TChan.MaxLogicChan + 1];//保存的是屏幕高度1000对应的值
    /**
     * 当前显示电平的序列号（429模式下为0,1高,2低，其他模式下为0,1,2,3,4）
     */
    private int currYIndex;
    /**
     * 电平线周围显示的数据
     */
    private String text = "";
    /**
     * 一般情况下，为false，表示当前操作的电平显示实心图标其他为空心；为true时，表示当前的图标也为空心
     */
    private boolean curYNull = false;

    private boolean isShowState = true;

    private final ArrayList<DiscreetVoltageLineInfoBean> listShowChannelInfo = new ArrayList<>();

    private final Context context = App.get().getApplicationContext();
    //endregion

    //region
    /**
     * 电平图标
     */
    private final Bitmap[] bmp = new Bitmap[TChan.MaxLogicChan + 1];

    private final Bitmap[] oldBmp = new Bitmap[TChan.MaxLogicChan + 1];

    private final Canvas[] mCanvas = new Canvas[TChan.MaxLogicChan + 1];
    private final Paint paint;
    /**
     * 电平线
     */
    private Bitmap bmpLine,oldBmpLine;
    private Canvas mCanvasLine;
    private boolean isChangeBitmap = false;
    private ICanvasGL canvasGL;

    public void onRefresh() {
        if (canvasGL != null) {
            canvasGL.onRefreshTexture();
        }
    }
    //endregion


    public static final int TriggerVoltageLine_Logic_Hight = 0x01;
    public static final int TriggerVoltageLine_Logic_Low = 0x02;
    /**
     * 该预值电平没有开打，不进行绘制，不进行统计
     */
    public static final int TriggerVoltageLine_Logic_None = 0x03;
    /**
     * 不进行绘制，在统计范围内。 有些预值电平与其它通道预值电平相同，则不进行绘制
     */
    public static final int TriggerVoltageLine_NoDraw = 0x04;

    private final int[] TriggerVoltageLine_logic_state = new int[TChan.MaxLogicChan + 1];

    public void setTriggerVoltageLine_logic_state(int[] logic_state) {
        if (TriggerVoltageLine_logic_state.length != logic_state.length) return;
        for (int i = 0; i < logic_state.length; i++) {
            TriggerVoltageLine_logic_state[i] = logic_state[i];
        }
        //Logger.i(TAG,Arrays.toString(TriggerVoltageLine_logic_state)+",serialNo:"+this.VoltageLineType);
        draw();
    }

    public void setTriggerVoltageLine_logic_state(int chId, int state) {
        TriggerVoltageLine_logic_state[chId] = state;
        //Logger.i(TAG,"arrays:"+ Arrays.toString(TriggerVoltageLine_logic_state)+",serialNo:"+this.VoltageLineType);
        draw();
    }

    public void setTriggerVoltageLine_logic_states(int... chId) {
        Arrays.fill(TriggerVoltageLine_logic_state, TriggerVoltageLine_Logic_None);
        for (int i = 0; i < chId.length; i++) {
            TriggerVoltageLine_logic_state[chId[i]] = TriggerVoltageLine_Logic_Hight;
        }
    }
    public int[] getTriggerVoltageLine_logic_state(){
        return TriggerVoltageLine_logic_state;
    }

    public DiscreetVoltageLine(String VoltageLineType) {
        this.VoltageLineType = VoltageLineType;
        initRes();
        initParam();
        reInitBmp();
        paint = new Paint();
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(1);

        draw();
    }

    private void initParam() {
        Arrays.fill(currYPos, GlobalVar.get().getMainWave().y / 2);
    }

    //region ITriggerLine
    public int getNameId() {
        if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value1)) {
            return TChan.S1;
        } else if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value2)) {
            return TChan.S2;
        } else if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value3)) {
            return TChan.S3;
        } else {
            return TChan.S4;
        }
    }

    public String getName() {
        return this.VoltageLineType;
    }


    private volatile boolean bActive = false;

    private int getDiscreet_l() {
        return bActive ? discreet_front_l : discreet_l;
    }

    private int getDiscreet_l_down() {
        return bActive ? discreet_front_l_down : discreet_l_down;
    }

    private int getDiscreet_l_up() {
        return bActive ? discreet_front_l_up : discreet_l_up;
    }

    private int getDiscreet_normal() {
        return bActive ? discreet_front_normal : discreet_normal;
    }

    private int getDiscreet_normal_down() {
        return bActive ? discreet_front_normal_down : discreet_normal_down;
    }

    private int getDiscreet_normal_up() {
        return bActive ? discreet_front_normal_up : discreet_normal_up;
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
        return channelId;
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
    public void setVisibleLine(boolean visibleLine) {
        if (this.visibleLine != visibleLine) {
            this.visibleLine = visibleLine;
            draw();
        }
    }

    @Override
    public boolean getVisibleLine() {
        return this.visibleLine;
    }

    @Override
    public void setShowMode(int showMode) {
        if (this.showMode != showMode) {
            this.showMode = showMode;
            draw();
        }
    }

    @Override
    public int getShowMode() {
        return this.showMode;
    }

    @Override
    public int getCurrYIndex() {
        return currYIndex;
    }

    /***
     * 设置当前序号,当设为0时，并不会真正的设为序列号号0，而是使全部图标变成空突变
     * 为0的时候没有赋值，所以可以保留原来的选择。
     * @param currYIndex {@link IWave IWave.Ch1 - IWave.Ch4}
     */
    @Override
    public void setCurrYIndex(int currYIndex) {
        Logger.i(TAG, "VoltageLineType:" + VoltageLineType + "," + "index:" + currYIndex + ",currYindex:" + this.currYIndex + ",curYNull:" + curYNull);

        if (currYIndex == 0) {

            curYNull = true;
            draw();
        } else {
            curYNull = false;
            this.currYIndex = currYIndex;
            draw();

        }
    }

    @Override
    public boolean setOffsetY(double offsetY) {
        if (!visible) {
//            return false;
            return setOffsetYHide(offsetY);
        }
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOffsetY() called with: offsetY = [" + offsetY + "]");
        boolean change = false;
        if (offsetY != 0) {
            double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY);
            if (temY < 0) {
                currYPos[currYIndex] = 0;
                change = true;
            } else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                currYPos[currYIndex] =  GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
                change = true;
            } else {
                currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff();
                change = false;
            }
            draw();
        }
        return change;
    }

    private boolean setOffsetYHide(double offsetY) {
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOffsetYHide() called with: offsetY = [" + offsetY + "]");
        boolean change = false;
        if (offsetY != 0) {
            double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY);
            if (temY < 0) {
                currYPos[currYIndex] = 0;
                change = true;
            } else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
                change = true;
            } else {
                currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff();
            }
        }
        return change;
    }

    @Override
    public boolean setCurrY(double currY) {//屏幕实际位置
        if (!visible) return false;
        Logger.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setCurrY() called with: ch = [" + currYIndex + "], y = [" + currY + "]," + VoltageLineType + "," + channelId);

        boolean change = false;
        if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) != currY) {
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
        }
        return change;
    }

    @Override
    public boolean setOtherY(int ch, double y) {//y屏幕实际位置
        if (!visible) {
//            return false;
            return setOtherYHide(ch, y);
        }
        Logger.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOtherY() called with: ch = [" + ch + "], y = [" + y + "]," + VoltageLineType + "," + channelId);

        boolean change = false;
        if (ScopeBase.changeAccuracy(this.currYPos[ch] * ScopeBase.getToUICoff()) != y) {
            if (y < 0) {
                this.currYPos[ch] = 0;
                change = true;
            } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                this.currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
                change = true;
            } else {
                this.currYPos[ch] = y * ScopeBase.getToFPGACoff();
                change = false;
            }
            draw();
        }
        return change;
    }

    private boolean setOtherYHide(int ch, double y) {
        boolean change = false;
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOtherYHide() called with: ch = [" + ch + "], y = [" + y + "]");
        if (ScopeBase.changeAccuracy(this.currYPos[ch] * ScopeBase.getToUICoff()) != y) {
            if (y < 0) {
                this.currYPos[ch] = 0;
                change = true;
            } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                this.currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();
                change = true;
            } else {
                this.currYPos[ch] = y * ScopeBase.getToFPGACoff();
            }
        }
        return change;
    }

    @Override
    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            draw();
        }
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
        double[] temp = new double[TChan.MaxLogicChan + 1];
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
                    currYPos[i] = currYPos[i] * GlobalVar.get().toYTCoef();
                }
                break;
            case IWorkMode.WorkMode_YTZOOM:
                for (int i = 0; i < currYPos.length; i++) {
                    currYPos[i] = currYPos[i] * GlobalVar.get().toZoomCoef();
                }
                break;

        }
        draw();
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
//                //case ShowMode_Three:drawThree(canvas);break;
//            }
//        }
    }

    @Override
    public void draw(ICanvasGL canvas) {
        if (!isShowState) return;
        if (!visible) return;
        synchronized (bmp) {
            canvasGL = canvas;
            if (isChangeBitmap) {
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
                //case ShowMode_Three:drawThree(canvas);break;
            }
            isChangeBitmap = false;
        }
    }

//    private void drawOne(Canvas canvas) {
//        for (int i = 1; i < 5; i++) {
//            if (currYPos[i] < resBmp[1][0].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            }
//        }
//    }

    private void drawOne(ICanvasGL canvas) {
//        for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//            if (i == currYIndex) continue;
//            drawToCanvasGL(canvas, i);
//        }
        TChan.foreachChan((i) -> {
                    drawToCanvasGL(canvas, i);
                },
                (chIdx) -> chIdx == currYIndex
        );
        drawToCanvasGL(canvas, currYIndex);
    }

//    private void drawTwo(Canvas canvas) {
//        //高小于低，就跟着跑，反之也是如此
//        //高电平在上，像素小，低电平在下，像素大
//        if (currYPos[1] > currYPos[2]) {
//            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
//            if (currYIndex == 1) currYPos[2] = currYPos[1];
//            else currYPos[1] = currYPos[2];
//        }
//
//
//        for (int i = 1; i < 3; i++) {
//            if (currYPos[i] < resBmp[1][0].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            }
//        }
//
//    }

    /**
     * 模式2的跟随
     */
    private void twoFollow() {
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
        if (currYPos[1] > currYPos[2]) {
            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
            if (currYIndex == 1) {
                currYPos[2] = currYPos[1];
            } else {
                currYPos[1] = currYPos[2];
            }
        }

    }

    private void drawTwo(ICanvasGL canvas) {
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
//        if (currYPos[1] > currYPos[2]) {
//            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
//            if (currYIndex == 1) {
//                currYPos[2] = currYPos[1];
//            } else {
//                currYPos[1] = currYPos[2];
//            }
//        }
        twoFollow();
        for (int i = 1; i < 5; i++) {
            if (i == currYIndex) continue;
            drawToCanvasGL(canvas, i);
        }
        drawToCanvasGL(canvas, currYIndex);

    }

    private void drawToCanvasGL(ICanvasGL canvas, int chId) {
//        Logger.i(TAG,"name:"+VoltageLineType+"  Id:"+chId+" pos:"+currYPos[chId]);
        if (isChangeBitmap) {
            canvas.invalidateTextureContent(bmp[chId], oldBmp[chId]);
            oldBmp[chId] = null;
        }

        if (currYPos[chId] < 0) {
            int temY = resBmp[1][0].getHeight();
            temY = temY - (bmp[chId].getHeight() - 1) / 2;

            canvas.drawBitmap(bmp[chId], 0, temY);
        } else if (ScopeBase.changeAccuracy(currYPos[chId] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
            temY = temY - (bmp[chId].getHeight() - 1) / 2;

            canvas.drawBitmap(bmp[chId], 0, temY);
        } else {
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[chId] * ScopeBase.getToUICoff())) - (bmp[chId].getHeight() - 1) / 2;

            canvas.drawBitmap(bmp[chId], 0, temY);
        }
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
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
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
                    },
                    (chIdx) -> TriggerVoltageLine_logic_state[chIdx] == TriggerVoltageLine_Logic_None
            );
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
        }


        return listShowChannelInfo;
    }

    private @IWorkMode.WorkMode int workMode = IWorkMode.WorkMode_None;

    private void reInitBmp(){
        if(workMode != WorkModeManage.getInstance().getmWorkMode()){
            workMode = WorkModeManage.getInstance().getmWorkMode();
            for (int i = 0; i < bmp.length; i++) {
                oldBmp[i] = bmp[i];
                bmp[i] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888);
                mCanvas[i] = new Canvas(bmp[i]);
            }
            oldBmpLine = bmpLine;
            bmpLine = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 1, Bitmap.Config.ARGB_8888);
            mCanvasLine = new Canvas(bmpLine);
        }

    }

    @Override
    public void refresh() {
        reInitBmp();
        draw();
    }

    //endregion

    //region 私有
    private void draw() {
        synchronized (bmp) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//            for (int i = 0; i < 5; i++) {
//                mCanvas[i].drawPaint(paint);
//                mCanvasLine.drawPaint(paint);
//            }
            TChan.foreachChan((i) -> {
                mCanvas[i].drawPaint(paint);
                mCanvasLine.drawPaint(paint);
            });
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
//            Logger.d(TAG, "showMode:" + showMode);
            switch (showMode) {
                case ITriggerLine.ShowMode_One:
                    drawOne();
                    break;
                case ITriggerLine.ShowMode_Two:
                    twoFollow();
                    drawTwo();
                    break;
                //case ITriggerLine.ShowMode_Three:drawThree();break;
            }
            isChangeBitmap = true;
            onRefresh();
        }
    }

    private void drawOne() {
        if (this.visibleLine) {
            paint.setColor(TChan.getChannelColor(context, channelId));
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint);
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw) continue;
//                if (i == currYIndex && !curYNull) {
//                    if (this.currYPos[currYIndex] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
//                                (float) ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                        drawText(this.currYPos[currYIndex], 35);
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][getDiscreet_l_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
//                                temY, paint);
//                        drawText(this.currYPos[currYIndex], 0);
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
//                                (float) (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
//                        drawText(this.currYPos[currYIndex], 35);
//                    }
//                } else {
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                    mCanvas[i].drawPaint(paint);
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
//
//                    if (this.currYPos[i] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
//                                (float) ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
//                                (float) (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);
//
//                    }
//                }
//            }
            TChan.foreachChan((i) -> {
                        if (i == currYIndex && !curYNull) {
                            if (this.currYPos[currYIndex] < 0) {
                                int temY = resBmp[1][0].getHeight();
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                                temY = bmp[channelId].getHeight() - resBmp[i][getDiscreet_l_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
                                        temY, paint);
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0);
                            } else {
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                            }
                        } else {
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                            mCanvas[i].drawPaint(paint);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                            if (this.currYPos[i] < 0) {
                                int temY = resBmp[1][0].getHeight();
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);
                            } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
                                        temY, paint);
                            } else {
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
                                        (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);

                            }
                        }
                    },
                    (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None || TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw
            );
        } else {
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw) continue;
//                if (i == currYIndex && !curYNull) {
//                    if (this.currYPos[currYIndex] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
//                                temY, paint);
//
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
//                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
//
//                    }
//                } else {
//                    if (this.currYPos[i] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
//                                (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);
//
//                    }
//                }
//            }
            TChan.foreachChan((i) -> {
                        if (i == currYIndex && !curYNull) {
                            if (this.currYPos[currYIndex] < 0) {
                                int temY = resBmp[1][0].getHeight();
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);

                            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
                                        temY, paint);

                            } else {
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
                            }
                        } else {
                            if (this.currYPos[i] < 0) {
                                int temY = resBmp[1][0].getHeight();
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);
                            } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
                                        temY, paint);
                            } else {
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
                                        (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);

                            }
                        }
                    },
                    (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None || TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw
            );

        }
    }

    private void drawTwo() {
        if (this.visibleLine) {
            paint.setColor(TChan.getChannelColor(context, channelId));
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint);

            //绘制当前显示电平的图标
            int front_up, front_down, front;
            if (!curYNull) {
                if (currYIndex == VoltageLine_High) {
                    front_up = getDiscreet_normal_up();
                    front_down = getDiscreet_normal_down();
                    front = getDiscreet_normal();
                } else {
                    front_up = getDiscreet_l_up();
                    front_down = getDiscreet_l_down();
                    front = getDiscreet_l();
                }
            } else {
                if (currYIndex == VoltageLine_High) {
                    front_up = discreet_normal_up;
                    front_down = discreet_normal_down;
                    front = discreet_normal;
                } else {
                    front_up = discreet_l_up;
                    front_down = discreet_l_down;
                    front = discreet_l;
                }
            }

            //绘制
            if (TriggerVoltageLine_logic_state[currYIndex] != TriggerVoltageLine_NoDraw) {

                if (this.currYPos[currYIndex] < 0) {
                    int temY = resBmp[1][0].getHeight();
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_up].getWidth(),
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint);
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_down].getWidth(),
                            temY, paint);
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0);
                } else {
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front].getWidth(),
                            (bmp[currYIndex].getHeight() - resBmp[channelId][front].getHeight()) / 2, paint);
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35);
                }
            }
            //绘制另一个非当前显示电平的图标
            int temIndex;
            int back_up, back_down, back;
            if (currYIndex == VoltageLine_High) {
                temIndex = VoltageLine_Low;
                back_up = discreet_l_up;
                back_down = discreet_l_down;
                back = discreet_l;
            } else {
                temIndex = VoltageLine_High;
                back_up = discreet_normal_up;
                back_down = discreet_normal_down;
                back = discreet_normal;

            }

            //需要绘制
            if (TriggerVoltageLine_logic_state[temIndex] != TriggerVoltageLine_NoDraw) {

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mCanvas[temIndex].drawPaint(paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                if (this.currYPos[temIndex] < 0) {
                    int temY = resBmp[1][0].getHeight();
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_up].getWidth(),
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint);
                    //drawText(35);
                } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_down].getWidth(),
                            temY, paint);
                    //drawText(0);
                } else {
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back].getWidth(),
                            (bmp[temIndex].getHeight() - resBmp[channelId][back].getHeight()) / 2, paint);
                    //drawText(35);
                }
            }

        } else { //
            //不显示线条

            //绘制当前显示电平的图标
            int front_up, front_down, front;
            if (!curYNull) {
                if (currYIndex == VoltageLine_High) {
                    front_up = getDiscreet_normal_up();
                    front_down = getDiscreet_normal_down();
                    front = getDiscreet_normal();
                } else {
                    front_up = getDiscreet_l_up();
                    front_down = getDiscreet_l_down();
                    front = getDiscreet_l();
                }
            } else {
                if (currYIndex == VoltageLine_High) {
                    front_up = discreet_normal_up;
                    front_down = discreet_normal_down;
                    front = discreet_normal;
                } else {
                    front_up = discreet_l_up;
                    front_down = discreet_l_down;
                    front = discreet_l;
                }
            }
            //需要绘制
            if (TriggerVoltageLine_logic_state[currYIndex] != TriggerVoltageLine_NoDraw) {

                paint.setColor(TChan.getChannelColor(context, channelId));
                if (this.currYPos[currYIndex] < 0) {
                    int temY = resBmp[1][0].getHeight();
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_up].getWidth(),
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint);
                } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_down].getWidth(),
                            temY, paint);
                } else {
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front].getWidth(),
                            (bmp[currYIndex].getHeight() - resBmp[channelId][front].getHeight()) / 2, paint);
                }
            }
            //绘制另一个非当前显示电平的图标
            int temIndex;
            int back_up, back_down, back;
            if (currYIndex == VoltageLine_High) {
                temIndex = VoltageLine_Low;
                back_up = discreet_l_up;
                back_down = discreet_l_down;
                back = discreet_l;
            } else {
                temIndex = VoltageLine_High;
                back_up = discreet_normal_up;
                back_down = discreet_normal_down;
                back = discreet_normal;
            }
            //需要绘制
            if (TriggerVoltageLine_logic_state[temIndex] != TriggerVoltageLine_NoDraw) {

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mCanvas[temIndex].drawPaint(paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                if (this.currYPos[temIndex] < 0) {
                    int temY = resBmp[1][0].getHeight();
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_up].getWidth(),
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint);

                } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2;
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_down].getWidth(),
                            temY, paint);

                } else {
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back].getWidth(),
                            (bmp[temIndex].getHeight() - resBmp[channelId][back].getHeight()) / 2, paint);

                }
            }

        }

    }

    /***
     * 绘制文字
     * @param  currY
     * @param drawTextY 绘制的起始Y坐标
     */
    private void drawText(int currY, int drawTextY) {
        if (currY <= 50) {
            drawTextY = 35;
        } else if (currY >= GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - 50) {
            drawTextY = 0;
        } else {
            drawTextY = 35;
        }
        int x;
        Rect rect;
        rect = getTextHeight(text);
        x = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - rect.width() - 5;
        drawTextY = drawTextY + rect.height();
        mCanvas[currYIndex].drawText(text, x, drawTextY, paint);

    }

    private Rect getTextHeight(String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return rect;
    }
    //endregion

}
