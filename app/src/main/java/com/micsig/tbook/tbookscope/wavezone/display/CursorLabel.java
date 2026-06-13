package com.micsig.tbook.tbookscope.wavezone.display;

import static com.micsig.tbook.scope.measure.Measure.MeasureType.MEASURE_CURSOR_X1;
import static com.micsig.tbook.scope.measure.Measure.MeasureType.MEASURE_CURSOR_X2;
import static com.micsig.tbook.tbookscope.wavezone.IWorkMode.WorkMode_YTZOOM;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.measure.MeasureService;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.CurSorMeasureBase;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auother Liwb
 * @description:
 * @data:2023-11-17 9:19
 */
public class CursorLabel {
    private static final String SPACE = " ";
    //region 绘制出 测量参数显示
    private static final int X1 = 0;
    private static final int X2 = 1;
    private static final int XDelta = 2;
    private static final int Y1 = 3;
    private static final int Y2 = 4;
    private static final int YDelta = 5;
    private static final int XFre = 6;
    private static final int S = 7;
    private static final int XArrow = 8;
    private static final int YArrow = 9;

    private static final int X1CursorInteraction = 10;

    private static final int X2CursorInteraction = 11;

    private static final int VoltgaeDelta = 12;

    private static final int SingleBmp = 13;

    private static final int VoltageDivideXDelta = 14;


    private static final boolean EnableMeasure = true;
    private Bitmap[] bmp = new Bitmap[6];

    private Bitmap[] smallBmps = new Bitmap[6];

    private Bitmap[] bigBmps = new Bitmap[6];

    private Bitmap[] tvalBmp = new Bitmap[6];
    private Bitmap rowBmp, colBmp, oldColBmp, singleBmp, smallSingleBmp, middleSingleBmp,tvalueBmp;
    private Canvas canvas;
    private Paint paint;
    private TextPaint textPaint;
    private HashMap<Integer, String> dataMap = new HashMap();
    private HashMap<Integer, Rect> rectMap = new HashMap<>();
    private List<Cursor_impIWave> cursorList;
    private boolean isChangeBmp = false;
    private ICanvasGL canvasGL;

    public void onRefresh() {
        if (canvasGL != null) {
            canvasGL.onRefreshTexture();
        }
    }

    private boolean colVisible = false;
    private boolean rowVisible = false;

    private int labelX, labelY, singleLabelX, singleLabelY;
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private Bitmap[] resCursorBmp = new Bitmap[30];

    private Context context = App.get().getApplicationContext();

    private double xDeltaCalculate = 0.0;

    private String xDeltaUnit = "";

    private void initResCursorBmp() {

        resCursorBmp[0] = Tools.readNineBmp(R.drawable.cursor_ch1_lr);
        resCursorBmp[1] = Tools.readNineBmp(R.drawable.cursor_ch2_lr);
        resCursorBmp[2] = Tools.readNineBmp(R.drawable.cursor_ch3_lr);
        resCursorBmp[3] = Tools.readNineBmp(R.drawable.cursor_ch4_lr);
        resCursorBmp[4] = Tools.readNineBmp(R.drawable.cursor_ch5_lr);
        resCursorBmp[5] = Tools.readNineBmp(R.drawable.cursor_ch6_lr);
        resCursorBmp[6] = Tools.readNineBmp(R.drawable.cursor_ch7_lr);
        resCursorBmp[7] = Tools.readNineBmp(R.drawable.cursor_ch8_lr);
        resCursorBmp[8] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[9] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[10] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[11] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[12] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[13] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[14] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[15] = Tools.readNineBmp(R.drawable.cursor_m_lr);
        resCursorBmp[16] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[17] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[18] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[19] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[20] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[21] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[22] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[23] = Tools.readNineBmp(R.drawable.cursor_r_lr);
        resCursorBmp[24] = Tools.readNineBmp(R.drawable.cursor_s1_lr);
        resCursorBmp[25] = Tools.readNineBmp(R.drawable.cursor_s2_lr);
        resCursorBmp[26] = Tools.readNineBmp(R.drawable.cursor_s3_lr);
        resCursorBmp[27] = Tools.readNineBmp(R.drawable.cursor_s4_lr);
        resCursorBmp[28] = Tools.readNineBmp(R.drawable.cursor_chx_lr);
        resCursorBmp[29] = Tools.readNineBmp(R.drawable.cursor_xy_lr);
    }

    public CursorLabel(List<Cursor_impIWave> curList) {
        this.cursorList = curList;
    }

    public void initMeasure() {
        initResCursorBmp();
        for (int i = 0; i < bmp.length; i++) {
            if (i == XDelta) {
                bmp[i] = Bitmap.createBitmap(190, 80, Bitmap.Config.ARGB_8888);
                bigBmps[i] = Bitmap.createBitmap(190, 85, Bitmap.Config.ARGB_8888);
                smallBmps[i] = Bitmap.createBitmap(190, 75, Bitmap.Config.ARGB_8888);
                tvalBmp[i] = Bitmap.createBitmap(190, 50, Bitmap.Config.ARGB_8888);
            } else if (i == YDelta) {
                bmp[i] = Bitmap.createBitmap(135, 40, Bitmap.Config.ARGB_8888);
                bigBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);
                smallBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);
                tvalBmp[i] = Bitmap.createBitmap(190, 30, Bitmap.Config.ARGB_8888);
            } else {
                bmp[i] = Bitmap.createBitmap(135, 40, Bitmap.Config.ARGB_8888);
                bigBmps[i] = Bitmap.createBitmap(135, 43, Bitmap.Config.ARGB_8888);
                if(i == Y1 || i == Y2){
                    bigBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);
                }
                smallBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);
                tvalBmp[i] = Bitmap.createBitmap(190, 30, Bitmap.Config.ARGB_8888);
            }
        }
        rowBmp = Bitmap.createBitmap(1800, 16, Bitmap.Config.ARGB_8888);
        colBmp = Bitmap.createBitmap(64, 1000, Bitmap.Config.ARGB_8888);
        singleBmp = Bitmap.createBitmap(185, 80, Bitmap.Config.ARGB_8888);
        //只显示y的情况下用
        smallSingleBmp = Bitmap.createBitmap(200, 62, Bitmap.Config.ARGB_8888);
        //只显示x的情况下，以及xy都显示的时候用
        middleSingleBmp = Bitmap.createBitmap(200, 153, Bitmap.Config.ARGB_8888);

        tvalueBmp = Bitmap.createBitmap(200, 82, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        paint = new Paint();
        paint.setTextSize(18);
        paint.setAntiAlias(true);

        textPaint = new TextPaint();
        textPaint.setTextSize(18);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(4);

        dataMap.put(X1, "---");
        dataMap.put(X2, "---");
        dataMap.put(XDelta, "---");
        dataMap.put(Y1, "---");
        dataMap.put(Y2, "---");
        dataMap.put(YDelta, "---");
        dataMap.put(XFre, "---");
        dataMap.put(X1CursorInteraction, "---");
        dataMap.put(X2CursorInteraction, "---");
        dataMap.put(VoltageDivideXDelta, "---");

        dataMap.put(VoltgaeDelta, "---");

        rectMap.put(X1, new Rect());
        rectMap.put(X2, new Rect());
        rectMap.put(XDelta, new Rect());
        rectMap.put(Y1, new Rect());
        rectMap.put(Y2, new Rect());
        rectMap.put(YDelta, new Rect());
        rectMap.put(XFre, new Rect());
        rectMap.put(XArrow, new Rect());
        rectMap.put(YArrow, new Rect());
        rectMap.put(SingleBmp, new Rect());
        EventFactory.addEventObserver(EventFactory.EVENT_CH_MEASURE_UPDATE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_MEASURE_UPDATE, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_REF_MEASURE_UPDATE, eventUIObserver);
        labelX = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X);
        labelY = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y);
        singleLabelX = 1600;
        singleLabelY = 0;
    }

    public void changeHeight(int height) {
        oldColBmp = colBmp;
        colBmp = Bitmap.createBitmap(64, height, Bitmap.Config.ARGB_8888);
        for (Cursor_impIWave c : cursorList) {
            if (TChan.isCursorRow4(c.getCursorType())) {
                c.changeCursorH(height);
            }
        }
        drawMeasure();
    }

    public synchronized void drawMeasure() {
        if (!colVisible && !rowVisible) return;
        int chan = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);

        boolean bTValueTrace = MeasureManage.getInstance().isCursorTValueTrace();

        int txtColor = App.get().getResources().getColor(R.color.color_Text_white);
        if (chan == TChan.AUTO) { //自由选择模式
            chan = WaveManage.get().getCurCh() - 1;
        }
        if (chan < 0) chan = resCursorBmp.length - 2; //cursor_chx_lr
        //如果yt模式下进行绘制
        String ellipsizedVoltageDivideXDelta = TextUtils.ellipsize(
                dataMap.get(VoltageDivideXDelta),
                textPaint,
                175,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(VoltageDivideXDelta,ellipsizedVoltageDivideXDelta);
        if (WorkModeManage.getInstance().isYTMode() || WorkModeManage.getInstance().getmWorkMode()==WorkMode_YTZOOM) {
            //如果是标签跟随状态，分为好几个框分别绘制
            if (!CursorManage.getInstance().isLabelNotFollowCursor()) {
                //在跟随光标的情况下，对宽度进行限制
                updateStringWidth();
                for (int i = 0; i < bmp.length; i++) {
                    if (colVisible && rowVisible ) {
                        bmp[i] = smallBmps[i];
                    } else {
                        if(bTValueTrace){
                            bmp[i] = tvalBmp[i];
                        }else {
                            bmp[i] = bigBmps[i];
                        }
                    }
                    canvas.setBitmap(bmp[i]);
                    paint.setXfermode(clearMode);
                    canvas.drawPaint(paint);
                    paint.setXfermode(srcMode);
                    int color = TChan.getChannelColor(context, chan + 1);
                    Paint strokePaint = new Paint();
                    strokePaint.setStyle(Paint.Style.STROKE);
                    strokePaint.setStrokeWidth(1);
                    strokePaint.setColor(color);
                    Paint fillPaint = new Paint();
                    fillPaint.setStyle(Paint.Style.FILL);
                    fillPaint.setColor(Color.argb(180,18,18,18));
                    Rect rect = new Rect(1, 1, bmp[i].getWidth()-1, bmp[i].getHeight()-1);
                    canvas.drawRect(rect,fillPaint);
                    canvas.drawRoundRect(1, 1, bmp[i].getWidth()-1f, bmp[i].getHeight()-1f, 3, 3, strokePaint);
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(txtColor);
                    if (i == XDelta) {
                        int y = 19;
                        if (colVisible == true && rowVisible == true) {
                            y = 21;
                        }

                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);
                        Tools.drawText(canvas, dataMap.get(XFre), 5, y * 2, bmp[i].getWidth(), paint, textPaint);
                        if (colVisible == true && rowVisible == false ) {
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(VoltgaeDelta), 5, y * 3, bmp[i].getWidth(), paint, textPaint);
                                Tools.drawText(canvas, dataMap.get(VoltageDivideXDelta), 5, y * 4, bmp[i].getWidth(), paint, textPaint);
                            }
                        } else if (colVisible && rowVisible) {
                            Tools.drawText(canvas, dataMap.get(S), 5, y * 3, bmp[i].getWidth(), paint, textPaint);
                        }

                    } else if (i == X1) {
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        if (colVisible == true && rowVisible == false) {
                            y = 18;
                        }
                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);
                        if (colVisible == true && rowVisible == false) {
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(X1CursorInteraction), 22, y * 2, bmp[i].getWidth(), paint, textPaint);
                            }
                        }
                    } else if (i == X2) {
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        if (colVisible == true && rowVisible == false) {
                            y = 18;
                        }
                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);
                        if (colVisible == true && rowVisible == false) {
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(X2CursorInteraction), 22, y * 2, bmp[i].getWidth(), paint, textPaint);
                            }
                        }
                    } else {
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);
                    }
                }
            } else {
                if (colVisible) {

                    if(bTValueTrace && !rowVisible){
                        singleBmp = tvalueBmp;
                    }else{
                        singleBmp = middleSingleBmp;
                    }
                } else if (rowVisible) {
                    singleBmp = smallSingleBmp;
                }
                canvas.setBitmap(singleBmp);
                paint.setXfermode(clearMode);
                canvas.drawPaint(paint);
                paint.setXfermode(srcMode);
                int color = TChan.getChannelColor(context, chan + 1);
                Paint strokePaint = new Paint();
                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(1);
                strokePaint.setColor(color);
                Paint fillPaint = new Paint();
                fillPaint.setStyle(Paint.Style.FILL);
                fillPaint.setColor(Color.argb(180,18,18,18));
                Rect rect = new Rect(1, 1, singleBmp.getWidth()-1, singleBmp.getHeight()-1);
                canvas.drawRect(rect,fillPaint);
                canvas.drawRoundRect(1, 1, singleBmp.getWidth()-1f, singleBmp.getHeight()-1f, 5, 5, strokePaint);

//                canvas.drawRoundRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), 6f, 6f, strokePaint);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(txtColor);
                if (colVisible && !rowVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        if (i == X1) {
                            int x = 6;
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * 1, singleBmp.getWidth(), paint, textPaint);
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(X1CursorInteraction), 22, y * 2, singleBmp.getWidth(), paint, textPaint);
                            }
                        } else if (i == X2) {
                            int x = 6;
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (bTValueTrace ? 2 : 3), singleBmp.getWidth(), paint, textPaint);
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(X2CursorInteraction), 22, y * 4, singleBmp.getWidth(), paint, textPaint);
                            }
                        } else if (i == XDelta) {
                            int y = 18;
                            int x = 6;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (bTValueTrace ? 3 : 5), bmp[i].getWidth(), paint, textPaint);
                            Tools.drawText(canvas, dataMap.get(XFre), x, y * (bTValueTrace ? 4 : 6), bmp[i].getWidth(), paint, textPaint);
                            if(!bTValueTrace) {
                                Tools.drawText(canvas, dataMap.get(VoltgaeDelta), x, y * 7, bmp[i].getWidth(), paint, textPaint);
                                Tools.drawText(canvas, dataMap.get(VoltageDivideXDelta), x, y * 8, bmp[i].getWidth(), paint, textPaint);
                            }
                        }
                    }
                } else if (rowVisible && !colVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        if (i == XDelta || i == X1 || i == X2) {
                            continue;
                        } else {
                            int y = 18;
                            int x = 6;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);
                        }
                    }
                } else if (colVisible && rowVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        int x = 6;
                        if (i == X1) {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                        } else if (i == X2) {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                        } else if (i == XDelta) {
                            int y = 18;
                            if (dataMap.get(XFre).isEmpty()) {
                                y = 25;
                            }
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                            Tools.drawText(canvas, dataMap.get(XFre), x, y * (i + 5), singleBmp.getWidth(), paint, textPaint);
                            Tools.drawText(canvas, dataMap.get(S), x, y * (i + 6), singleBmp.getWidth(), paint, textPaint);
                        } else {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);
                        }
                    }
                }
            }
        } else if (WorkModeManage.getInstance().isXyMode()) {
            updateStringWidth();
            if (!CursorManage.getInstance().isLabelNotFollowCursor()) {
                for (int i = 0; i < bmp.length; i++) {
                    bmp[i] = smallBmps[i];
                    if (i == XDelta) {
                        bmp[i] = Bitmap.createBitmap(140, 25, Bitmap.Config.ARGB_8888);
                    }
                    canvas.setBitmap(bmp[i]);
                    paint.setXfermode(clearMode);
                    canvas.drawPaint(paint);
                    paint.setXfermode(srcMode);
                    int color = Color.LTGRAY;
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1.5f);
                    canvas.drawRoundRect(0, 0, bmp[i].getWidth(), bmp[i].getHeight(), 6f, 6f, paint);
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(txtColor);
                    if (i == XDelta) {
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);
                    } else if (i == X1) {
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);
                    } else if (i == X2) {
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);
                    } else {
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);
                    }
                }
            } else {
                if (colVisible && rowVisible) {
                    singleBmp = Bitmap.createBitmap(120, 115, Bitmap.Config.ARGB_8888);
                } else if (rowVisible || colVisible) {
                    singleBmp = Bitmap.createBitmap(120, 60, Bitmap.Config.ARGB_8888);
                }

                canvas.setBitmap(singleBmp);
                paint.setXfermode(clearMode);
                paint.setAntiAlias(true);
                canvas.drawPaint(paint);
                paint.setXfermode(srcMode);
                int color = Color.LTGRAY;
                paint.setColor(color);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f);
//            canvas.drawRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), paint);
                canvas.drawRoundRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), 6f, 6f, paint);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
//            canvas.drawRoundRect(1, 1, singleBmp.getWidth() - 1, singleBmp.getHeight() - 1,8,8,paint);
                paint.setColor(txtColor);
                CurSorMeasureBase curSorMeasureBase = new CurSorMeasureBase();
                if (colVisible && !rowVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        if (i == X1) {
                            int x = 4;
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y, singleBmp.getWidth(), paint, textPaint);
                        } else if (i == X2) {
                            int x = 4;
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * 2, singleBmp.getWidth(), paint, textPaint);
                        } else if (i == XDelta) {
                            int y = 18;
                            int x = 4;
                            Tools.drawText(canvas, dataMap.get(i), x, y * 3, bmp[i].getWidth(), paint, textPaint);
                        }
                    }
                } else if (rowVisible && !colVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        if (i == XDelta || i == X1 || i == X2) {
                            continue;
                        } else {
                            int y = 18;
                            int x = 4;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);
                        }
                    }
                } else if (colVisible && rowVisible) {
                    for (int i = 0; i < bmp.length; i++) {
                        int x = 4;
                        if (i == X1) {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                        } else if (i == X2) {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                        } else if (i == XDelta) {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);
                        } else {
                            int y = 18;
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);
                        }
                    }
                }
            }
        }


        //绘制光标
        int pX1 = (int) getCursor(TChan.Cursor_col_1);
        int pX2 = (int) getCursor(TChan.Cursor_col_2);
        int pY1 = (int) getCursor(TChan.Cursor_row_1);
        int pY2 = (int) getCursor(TChan.Cursor_row_2);
        int distance = Math.abs(pX1 - pX2);
        canvas.setBitmap(rowBmp);
        paint.setXfermode(clearMode);
        canvas.drawPaint(paint);
        paint.setXfermode(srcMode);
        Bitmap bmp = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 100);
        if (bmp != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            canvas.drawBitmap(bmp, Math.min(pX1, pX2), 0, paint);
            putRect(XArrow, Math.min(pX1, pX2), 0, bmp);
        }
        if (bmp != null) bmp.recycle();

        distance = Math.abs(pY1 - pY2);
        canvas.setBitmap(colBmp);
        paint.setXfermode(clearMode);
        canvas.drawPaint(paint);
        paint.setXfermode(srcMode);
        Bitmap bmp1 = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 75);
        if (WorkModeManage.getInstance().isXyMode()) {
            bmp1 = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 75);
        }
        if (bmp1 != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            Matrix m = new Matrix();
            m.postTranslate((float) -bmp1.getWidth() / 2, (float) -bmp1.getHeight() / 2);
            m.postRotate(90);
            m.postTranslate((float) bmp1.getHeight() / 2, (float) bmp1.getWidth() / 2 + Math.min(pY1, pY2));
            canvas.drawBitmap(bmp1, m, paint);
        }
        if (bmp1 != null) bmp1.recycle();
        isChangeBmp = true;
        onRefresh();
    }

    public double getCursor(int cursorType) {
        for (Cursor_impIWave c : cursorList) {
            if (c.getCursorType() == cursorType) {
                if (TChan.isCursorCol2(cursorType)) {
                    return c.getX();
                } else if (TChan.isCursorRow2(cursorType)) {
                    return c.getY();
                }
            }
        }
//        Log.d("Tag.Debug", String.format("getCursor error: %d",0 ));
        return 0;
    }

    private int centerTxtX(Bitmap bmp, String s, Paint paint) {
        int width = Tools.getTextRect(s, paint).width();
        int x = (bmp.getWidth() - width) / 2;
        return x;
    }

    private int centerTxtY(Bitmap bmp, String s, Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float distance = (metrics.bottom - metrics.top) / 2 - metrics.bottom;
        float baseline = distance + bmp.getHeight() / 2;
        return (int) baseline;
    }

    public synchronized void drawMeasure(ICanvasGL canvas) {
        if (EnableMeasure == false) return;
        canvasGL = canvas;
        int interval = 30;
        int x = labelX + 160, y = labelY + 150;
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
        int pX1 = (int) Math.round(getCursor(TChan.Cursor_col_1));
        int pX2 = (int) Math.round(getCursor(TChan.Cursor_col_2));
        int pY1 = (int) Math.round(getCursor(TChan.Cursor_row_1));
        int pY2 = (int) Math.round(getCursor(TChan.Cursor_row_2));
        for (int i = 0; i < bmp.length; i++) {
            if (isChangeBmp) canvas.invalidateTextureContent(bmp[i], null);
        }
        if (isChangeBmp) {
            canvas.invalidateTextureContent(rowBmp, null);
            canvas.invalidateTextureContent(colBmp, oldColBmp);
            canvas.invalidateTextureContent(singleBmp, null);
            oldColBmp = null;
        }

        if (colVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            drawGLXDelta(canvas, y, pX1, pX2, screenWidth, bmp[XDelta]);
            drawGLX1(canvas, y, pX1, pX2, screenWidth, bmp[X1]);
            drawGLX2(canvas, y, pX1, pX2, screenWidth, bmp[X2]);
        } else if (colVisible && CursorManage.getInstance().isLabelNotFollowCursor()) {
            drawGLSingle(canvas, screenHeight, screenWidth, singleBmp);
            drawGLX1(canvas, y, pX1, pX2, screenWidth, bmp[X1]);
        }
        if (rowVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            drawGLY1(canvas, x, pY1, pY2, screenHeight, bmp[Y1]);
            drawGLY2(canvas, x, pY1, pY2, screenHeight, bmp[Y2]);
            drawGLYDelta(canvas, x, pY1, pY2, screenHeight, bmp[YDelta]);
        } else if (rowVisible && CursorManage.getInstance().isLabelNotFollowCursor()) {
            drawGLSingle(canvas, screenHeight, screenWidth, singleBmp);
        }
        putRect(XArrow, 0, labelY + 140, rowBmp);
        putRect(YArrow, labelX + 150, 0, colBmp);
        intersect(screenWidth, screenHeight);
        drawScreen(canvas);
        isChangeBmp = false;
    }

    private void putRect(int key, int x, int y, Bitmap bmp) {
        Rect rect = rectMap.get(key);
        rect.set(x, y, x + bmp.getWidth(), y + bmp.getHeight());
        rectMap.put(key, rect);
    }

    private void putRect(int key, int x, int y) {
        Rect rect = rectMap.get(key);
        int width = rect.width();
        int height = rect.height();
        rect.set(x, y, x + width, y + height);
        rectMap.put(key, rect);
    }

    private void drawGLXDelta(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {
        int xInterval = Math.abs(pX1 - pX2) + 0;
        int x;
        if (xInterval < bmp.getWidth() && pX1 < bmp.getWidth() && pX2 < bmp.getWidth()) {
            //左边delta显示不全情况
            x = Math.min(pX1, pX2);
        } else if (xInterval < bmp.getWidth() && pX1 + bmp.getWidth() > screenWidth && pX2 + bmp.getWidth() > screenWidth) {
            //右边delta显示不全情况
            x = Math.max(pX1, pX2) - bmp.getWidth();
        } else if (xInterval < bmp.getWidth()) {
            //中间delta显示不全情况
            x = CenterBmpX(pX1, pX2, bmp);
        } else {
            //中间可以显示完全
            x = CenterBmpX(pX1, pX2, bmp);
        }
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());

        if(y<0){
            y=0;
        }else if(y+bmp.getHeight() > screenHeight){
            y = screenHeight - bmp.getHeight();
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(XDelta, x, y, bmp);
    }

    private void drawGLYDelta(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {
        int xInterval = Math.abs(pY1 - pY2) + 0;
        int y;
        if (xInterval < bmp.getHeight() && pY1 < bmp.getHeight() && pY2 < bmp.getHeight()) {
            //左边delta显示不全情况
            y = Math.min(pY1, pY2);
        } else if (xInterval < bmp.getHeight() && pY1 + bmp.getHeight() > screenHeight && pY2 + bmp.getHeight() > screenHeight) {
            //右边delta显示不全情况
            y = Math.max(pY1, pY2) - bmp.getHeight();
        } else if (xInterval < bmp.getHeight()) {
            //中间delta显示不全情况
            y = CenterBmpY(pY1, pY2, bmp);
        } else {
            //中间可以显示完全
            y = CenterBmpY(pY1, pY2, bmp);
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(YDelta, x, y, bmp);
    }

    private void drawGLX1(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {
        int x;
        y = labelY;
        int xInterval = Math.abs(pX1 - pX2) + 0;
        if (pX1 < bmp.getWidth() / 2) {
            //左边delta显示不全情况
            x = pX1;
        } else if (pX1 + bmp.getWidth() / 2 > screenWidth) {
            //右边delta显示不全情况
            x = pX1 - bmp.getWidth();
        } else if (xInterval < bmp.getWidth()) {
            //中间delta显示不全情况
            x = pX1 - bmp.getWidth() + xInterval / 2;
            if (pX1 > pX2) {
                x = pX1 - xInterval / 2;
            }
        } else {
            //中间可以显示完全
            x = pX1 - bmp.getWidth() / 2;
        }
//        if (x<0){
//            x=pX1;
//        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(X1, x, y, bmp);
    }

    private void drawGLX2(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {
        int x;
        y = labelY;
        int xInterval = Math.abs(pX1 - pX2) + 0;
        if (pX2 < pX1 + bmp.getWidth() + bmp.getWidth() / 2 && pX1 <= bmp.getWidth() / 2
            /*|| pX1==rectMap.get(X1).left*/) {
            //左边delta显示不全情况
            x = pX2;
            y += 50;
        } else if (pX2 + bmp.getWidth() / 2 > pX1 - bmp.getWidth() && pX1 + bmp.getWidth() / 2 > screenWidth) {
            //右边delta显示不全情况
            x = pX2 - bmp.getWidth();
            y += 50;
        } else if (pX2 + bmp.getWidth() / 2 > screenWidth) {
            x = pX2 - bmp.getWidth();
            y += 50;
        } else if (pX2 < bmp.getWidth() / 2) {
            x = pX2;
            y += 50;
        } else if (xInterval < bmp.getWidth()) {
            //中间delta显示不全情况
            x = pX2 - xInterval / 2;
            if (pX2 < pX1) {
                x = pX2 - bmp.getWidth() + xInterval / 2;
            }
        } else {
            //中间可以显示完全
            x = pX2 - bmp.getWidth() / 2;
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(X2, x, y, bmp);
    }

    private void drawGLY1(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {
        int y;
        x = labelX;
        int xInterval = Math.abs(pY1 - pY2) + 0;
        if (pY1 < bmp.getHeight() / 2) {
            //左边delta显示不全情况
            y = pY1;
        } else if (pY1 + bmp.getHeight() / 2 > screenHeight) {
            //右边delta显示不全情况
            y = pY1 - bmp.getHeight();
        } else if (xInterval < bmp.getHeight()) {
            //中间delta显示不全情况
            y = pY1 - bmp.getHeight() + xInterval / 2;
            if (pY1 > pY2) {
                y = pY1 - xInterval / 2;
            }
        } else {
            //中间可以显示完全
            y = pY1 - bmp.getHeight() / 2;
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(Y1, x, y, bmp);
    }

    private void drawGLY2(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {
        int y;
        x = labelX;
        int xInterval = Math.abs(pY1 - pY2) + 0;
        if (pY2 < pY1 + bmp.getHeight() + bmp.getHeight() / 2 && pY1 <= bmp.getHeight() / 2) {
            //左边delta显示不全情况
//            x = 50;
            y = pY2;
        } else if (pY2 + bmp.getHeight() / 2 > pY1 - bmp.getHeight() && pY1 + bmp.getHeight() / 2 > screenHeight) {
            //右边delta显示不全情况
//            x=50;
            y = pY2 - bmp.getHeight();
        } else if (pY2 < bmp.getHeight() / 2) {
            //左边delta显示不全情况
//            x=50;
            y = pY2;
        } else if (pY2 + bmp.getHeight() / 2 > screenHeight) {
            //右边delta显示不全情况
//            x=50;
            y = pY2 - bmp.getHeight();
        } else if (xInterval < bmp.getHeight()) {
            //中间delta显示不全情况
            y = pY2 - xInterval / 2;
            if (pY2 < pY1) {
                y = pY2 - bmp.getHeight() + xInterval / 2;
            }
        } else {
            //中间可以显示完全
            y = pY2 - bmp.getHeight() / 2;
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(Y2, x, y, bmp);
    }


    private void drawGLSingle(ICanvasGL canvas, int screenHeight, int screenWidth, Bitmap bmp) {

        int x = singleLabelX;
        int y = singleLabelY;
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        if (x < 0) {
            x = 0;
        } else if (x + bmpWidth > screenWidth) {
            x = screenWidth - bmpWidth;
        }
        if (y < 0) {
            y = 0;
        } else if (y + bmpHeight > screenHeight) {
            y = screenHeight - bmpHeight;
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(SingleBmp, x, y, bmp);
    }

    private void intersect(int screenWidth, int screenHeight) {
        if (rowVisible) {
            int py1 = (int) getCursor(TChan.Cursor_row_1);
            int py2 = (int) getCursor(TChan.Cursor_row_2);

            if (Rect.intersects(rectMap.get(Y1), rectMap.get(Y2))) {
                Rect y2 = rectMap.get(Y2);
                Rect yDelta = rectMap.get(YDelta);
                y2.offset(y2.width(), 0);
                yDelta.offset(y2.width(), 0);
                if (y2.right > screenWidth) y2.offset(-y2.width() * 2, 0);
                if (yDelta.right > screenWidth) yDelta.offset(-yDelta.width() * 3, 0);
                rectMap.put(Y2, y2);
                rectMap.put(YDelta, yDelta);

                if (rectMap.get(Y1).top < 0) {
                    putRect(Y1, rectMap.get(Y1).left, py1);
                }
                if (rectMap.get(Y2).top < 0) {
                    putRect(Y2, rectMap.get(Y2).left, py2);
                }

                if (rectMap.get(Y1).bottom > screenHeight) {
                    putRect(Y1, rectMap.get(Y1).left, py1 - bmp[Y1].getHeight());
                }
                if (rectMap.get(Y2).bottom > screenHeight) {
                    putRect(Y2, rectMap.get(Y2).left, py2 - bmp[Y2].getHeight());
                }
            }

        }
        if (colVisible) {
            int pX1 = (int) getCursor(TChan.Cursor_col_1);
            int pX2 = (int) getCursor(TChan.Cursor_col_2);

            if (rectMap.get(X1).left < 0) {
                putRect(X1, pX1, labelY);
            }
            if (rectMap.get(X2).left < 0) {
                putRect(X2, pX2, labelY + 50);
            }

            if (rectMap.get(X1).right > screenWidth) {
                putRect(X1, pX1 - bmp[X1].getWidth(), labelY);
            }
            if (rectMap.get(X2).right > screenWidth) {
                putRect(X2, pX2 - bmp[X2].getWidth(), labelY + 50);
            }

            if (Rect.intersects(rectMap.get(X1), rectMap.get(X2)) && rectMap.get(X1).left == pX1) {
                putRect(X2, pX2, labelY + 50);
            }
            if (Rect.intersects(rectMap.get(X1), rectMap.get(X2)) && rectMap.get(X1).right == pX1) {
                putRect(X2, pX2 - bmp[X2].getWidth(), labelY + 50);
            }
        }
    }

    private void drawScreen(ICanvasGL canvasGL) {
        if (colVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {

            int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());

            Rect x = rectMap.get(X1);
            int y = x.top;
            int hh = rectMap.get(XDelta).height() + 50;
            if ( y> screenHeight - hh) y = screenHeight - hh;

            canvasGL.drawBitmap(bmp[X1], x.left, y);
            x = rectMap.get(X2);
            y = x.top;
            hh = rectMap.get(XDelta).height() + 50;
            if ( y> screenHeight - hh) y = screenHeight - hh;

            canvasGL.drawBitmap(bmp[X2], x.left, y);
            x = rectMap.get(XDelta);
            canvasGL.drawBitmap(bmp[XDelta], x.left, x.top);
        } else if (colVisible && CursorManage.getInstance().isLabelNotFollowCursor() && !rowVisible) {
            Rect x = rectMap.get(SingleBmp);
//            Rect x1 = rectMap.get(X1);
            canvasGL.drawBitmap(singleBmp, x.left, x.top);
//            canvasGL.drawBitmap(bmp[X1],x1.left,x1.top);
        }
        if (rowVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            Rect x = rectMap.get(Y1);
            canvasGL.drawBitmap(bmp[Y1], x.left, x.top);
            x = rectMap.get(Y2);
            canvasGL.drawBitmap(bmp[Y2], x.left, x.top);
            x = rectMap.get(YDelta);
            canvasGL.drawBitmap(bmp[YDelta], x.left, x.top);
        } else if (CursorManage.getInstance().isLabelNotFollowCursor() && rowVisible) {
            Rect x = rectMap.get(SingleBmp);
            canvasGL.drawBitmap(singleBmp, x.left, x.top);
        }
        if (colVisible) {
            Rect x = rectMap.get(XArrow);
            int y = x.top;
            int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
            int hh = rectMap.get(XDelta).height() + 10;

            if ( y> screenHeight - hh) y = screenHeight - hh;

            canvasGL.drawBitmap(rowBmp, x.left, y);
        }
        if (rowVisible) {
            Rect y = rectMap.get(YArrow);
            canvasGL.drawBitmap(colBmp, y.left, y.top);
        }
    }

    private int CenterBmpX(int x1, int x2, Bitmap bmp) {
        int min = Math.min(x1, x2);
        int x = (Math.abs(x1 - x2) - bmp.getWidth()) / 2 + min;
        return x;
    }

    private int CenterBmpY(int y1, int y2, Bitmap bmp) {
        int min = Math.min(y1, y2);
        int y = (Math.abs(y1 - y2) - bmp.getHeight()) / 2 + min;
        return y;
    }

    private int selectedIndex = -1;

    public int selectMeasure(int x, int y) {
        if (EnableMeasure == false) return -1;
        Map.Entry<Integer, Rect> selected = null;
        for (Map.Entry<Integer, Rect> set : rectMap.entrySet()) {
            if (set.getValue().contains(x, y) && (set.getKey() == XDelta || set.getKey() == YDelta)) {
                selected = set;
                break;
            } else if (set.getValue().contains(x, y)) {
                selected = set;
                break;
            }
        }
        if (selected != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {
            switch (selected.getKey()) {
                case XDelta: {
                    selectedIndex = TChan.Cursor_col_4;
                    return TChan.Cursor_col_4;
                }
                case YDelta: {
                    selectedIndex = TChan.Cursor_row_4;
                    return TChan.Cursor_row_4;
                }
                case X1: {
                    selectedIndex = -1;
                    return TChan.Cursor_col_1;
                }
                case X2: {
                    selectedIndex = -1;
                    return TChan.Cursor_col_2;
                }
                case Y1: {
                    selectedIndex = -1;
                    return TChan.Cursor_row_1;
                }
                case Y2: {
                    selectedIndex = -1;
                    return TChan.Cursor_row_2;
                }
            }
        }else if(selected != null && CursorManage.getInstance().isLabelNotFollowCursor()){
            switch (selected.getKey()) {
                case SingleBmp: {
                    selectedIndex = TChan.SingleRect;
                    return TChan.SingleRect;
                }
            }
        }

        return -1;
    }

    public void MoveLabel(int offsetX, int offsetY) {
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
        //Log.d("Tag.Debug", String.format("MoveLabel: [offsetX:%d, offsetY:%d]",screenWidth,screenHeight ));
//        int width=bmp[Y1].getWidth()+bmp[YDelta].getWidth()+40;
//        int height=bmp[X1].getHeight()+bmp[XDelta].getHeight()+110;
        int width = rectMap.get(YDelta).right - rectMap.get(Y1).left;
        int height = rectMap.get(XDelta).bottom - rectMap.get(X1).top;
        //设置标签位置，然后刷新
        if (selectedIndex == TChan.Cursor_col_4) {
            labelY -= offsetY;
            if (labelY < 0) labelY = 0;
            if (labelY > screenHeight - height) labelY = screenHeight - height;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y, String.valueOf(labelY));
        } else if (selectedIndex == TChan.Cursor_row_4) {
            labelX -= offsetX;
            if (labelX < 0) labelX = 0;
            if (labelX > screenWidth - width) labelX = screenWidth - width;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X, String.valueOf(labelX));
        } else if (selectedIndex == TChan.SingleRect) {
            singleLabelX -= offsetX;
            if (singleLabelX < 0) singleLabelX = 0;
            width = singleBmp.getWidth();
            if (singleLabelX > screenWidth - width) singleLabelX = screenWidth - width;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_X, String.valueOf(singleLabelX));
            singleLabelY -= offsetY;
            if (singleLabelY < 0) singleLabelY = 0;
            height = rectMap.get(SingleBmp).bottom - rectMap.get(SingleBmp).top;
            if (singleLabelY > screenHeight - height) singleLabelY = screenHeight - height;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_Y, String.valueOf(singleLabelY));
        }
        drawMeasure();
    }

    public void refreshLabelPos() {
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
        int width = rectMap.get(YDelta).right - rectMap.get(Y1).left;
        int height = rectMap.get(XDelta).bottom - rectMap.get(X1).top;
        //设置标签位置，然后刷新
        if (CursorManage.getInstance().getColVisible()) {
            if (labelY < 0) labelY = 0;
            if (labelY > screenHeight - height) labelY = screenHeight - height;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y, String.valueOf(labelY));
        } else if (CursorManage.getInstance().getRowVisible()) {
            if (labelX < 0) labelX = 0;
            if (labelX > screenWidth - width) labelX = screenWidth - width;
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X, String.valueOf(labelX));
        }
        drawMeasure();
    }

    public void setData() {
        if (!colVisible && !rowVisible) return;
        if (WorkModeManage.getInstance().isXyMode()) {
            calDataXY();
        } else {
            calDataYT();
        }
        drawMeasure();
    }

    private void calDataYT() {
//        if (CursorManage.getInstance().isCursorTrace()) return;
        int curCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
        if (curCh == TChan.AUTO) { //自由选择模式
            curCh = WaveManage.get().getCurCh();
        } else {
            curCh = TChan.toUiChNo(curCh);
        }
        if (curCh == -1 || Scope.getInstance().isUsersetReset()) {
            dataMap.put(X1, "X1:" + SPACE + "---");
            dataMap.put(X2, "X2:" + SPACE + "---");
            dataMap.put(XDelta, "ΔX:" + SPACE + "---");
            dataMap.put(XFre, "1/ΔX:" + SPACE + "---");
            dataMap.put(Y1, "Y1:" + SPACE + "---");
            dataMap.put(Y2, "Y2:" + SPACE + "---");
            dataMap.put(YDelta, "ΔY:" + SPACE + "---");
            dataMap.put(S, "S:" + SPACE + "---");
            dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + "---");
            dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + "---");
            dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + "---");
            dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + "---");
            return;
        }

        double curChY = WaveManage.get().getUIPositionY(curCh);
        long curTimeX = 0;
        double timeEvery = 0;
        int chIdx = TChan.toFpgaChNo(curCh);
        if (ChannelFactory.isDynamicCh(chIdx)
                || ChannelFactory.isMathCh(chIdx)
                || ChannelFactory.isSerialCh(chIdx)) {
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

        double chEvery = 0;
        if (ChannelFactory.isDynamicCh(chIdx)) {
            chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix();
        } else if (ChannelFactory.isMathCh(chIdx)) {
            chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix();
        } else if (ChannelFactory.isRefCh(chIdx)) {
            chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix();
        }
        if(!MeasureManage.getInstance().isCursorTValueTrace()) {
            MeasureService.setMeasureRange((int) CursorManage.getInstance().getCol1Position(),
                    (int) CursorManage.getInstance().getCol2Position());
        }

        double x1 = (CursorManage.getInstance().getCol1Position() - curTimeX) * timeEvery;
        double x2 = (CursorManage.getInstance().getCol2Position() - curTimeX) * timeEvery;
        double y1 = ((curChY - CursorManage.getInstance().getRow1Position()) * chEvery);
        double y2 = ((curChY - CursorManage.getInstance().getRow2Position()) * chEvery);


        if (CursorManage.getInstance().isCursorTrace() == false) {
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT 保存测量值: x1:%s,x2:%s,y1:%s,y2:%s ,ch:%s",x1,x2,y1,y2,WaveManage.get().getCurCh() ));
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            if (CursorManage.getInstance().isFFT(curCh)) {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ, String.valueOf(x1));
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ, String.valueOf(x2));
            } else {
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1, String.valueOf(x1));
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2, String.valueOf(x2));
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1, String.valueOf(y1));
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2, String.valueOf(y2));
        }
        //else  if (CursorManage.getInstance().isAutoCursor() ||  CursorManage.getInstance().isSelectChanEqCursorChan())
        else if (CursorManage.getInstance().isEnableCursorTrance()) {
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: 测量值不变:%s",WaveManage.get().getCurCh() ));
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            if (CursorManage.getInstance().isColCursorTrace()) {
                double px1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1);
                double px2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2);
                if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) {
                    px1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);
                    px2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);
                }
                long posX1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), px1);
                long posX2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), px2);
                x1 = (posX1 - curTimeX) * timeEvery;
                x2 = (posX2 - curTimeX) * timeEvery;
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: px1:%s,px2:%s,posX1:%s,posX2:%s,curTimeX:%s,timeEvery:%s,x1:%s,x2:%s",px1,px2,posX1,posX2,curTimeX,timeEvery,x1,x2 ));
//                try {
//                    throw new Exception();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            if (CursorManage.getInstance().isRowCursorTrace()) {
                int chanIdx = TChan.toFpgaChNo(curCh);
                if (CursorManage.getInstance().isScpiChanEqCursorChan()) {
//                    Log.d("Tag.Debug", String.format("CursorLabel.calDataYT scpi: %s",chanIdx ));
                    chanIdx = CursorManage.getInstance().getScpiChanIdx();
                    curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chanIdx));
                    if (ChannelFactory.isDynamicCh(chanIdx)) {
                        chEvery = ChannelFactory.getDynamicChannel(chanIdx).getVerticalPerPix();
                    } else if (ChannelFactory.isMathCh(chanIdx)) {
                        chEvery = ChannelFactory.getMathChannel(chanIdx).getVerticalPerPix();
                    } else if (ChannelFactory.isRefCh(chanIdx)) {
                        chEvery = ChannelFactory.getRefChannel(chanIdx).getVerticalPerPix();
                    }

                }
                double py1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1);
                double py2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2);
                double posY1 = Tools.ScaleToPix(chanIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, py1);
                double posY2 = Tools.ScaleToPix(chanIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, py2);

                y1 = ( curChY - posY1) * chEvery;
                y2 = ( curChY - posY2) * chEvery;
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: py1:%s ,py2:%s,posY1:%s,posY2:%s",py1,py2,posY1,posY2 ));
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: y1:%s,y2:%s ,chIdx:%s ,curChy:%s,chEvery:%s",y1,y2,chanIdx,curChY,chEvery ));
            }
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:  x1:%s ,posX1:%s ,curTimex:%s ,timeEvery:%s",x1,posX1,curTimeX,timeEvery ));
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:py1:%s,  y1:%s ,posY1:%s ,curChY:%s ,chEvery:%s",py1, y1,posY1,curChY,chEvery ));
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:py2:%s,  y2:%s ,posY2:%s ,curChY:%s ,chEvery:%s",py2, y2,posY2,curChY,chEvery ));
        }


        double deltaY = Math.abs(y1 - y2);
        double deltaX = Math.abs(x1 - x2);
        xDeltaCalculate = deltaX;
        String yUnit = ChannelFactory.getProbeType(TChan.toFpgaChNo(curCh));
        String xUnit;
        if (ChannelFactory.isMath_FFT_Ch(TChan.toFpgaChNo(curCh))
                || (ChannelFactory.isRefCh(TChan.toFpgaChNo(curCh)) && ChannelFactory.getRefChannel(TChan.toFpgaChNo(curCh)).getRefType() == 2)) {
            xUnit = "Hz";
        } else {
            xUnit = "s";
        }
        xDeltaUnit = xUnit;
        if (TChan.isSerial(curCh)) {
            dataMap.put(X1, "X1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit));
            dataMap.put(X2, "X2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit));
            dataMap.put(XDelta, "ΔX" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit));
            dataMap.put(XFre, "1/ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s")));
            dataMap.put(Y1, "Y1:" + SPACE + "---");
            dataMap.put(Y2, "Y2:" + SPACE + "---");
            dataMap.put(YDelta, "ΔY:" + SPACE + "---");
            dataMap.put(S, "ΔV/Δt:" + SPACE + "---");

        } else {
            dataMap.put(X1, "X1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit));
            dataMap.put(X2, "X2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit));
            dataMap.put(XDelta, "ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit));
            dataMap.put(XFre, "1/ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s")));
            dataMap.put(Y1, "Y1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(y1) + yUnit));
            dataMap.put(Y2, "Y2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(y2) + yUnit));
            dataMap.put(YDelta, "ΔY:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaY) + yUnit));
            dataMap.put(S, "ΔV/Δt:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + yUnit + "/" + xUnit));
        }
        Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 1.0 / deltaX, deltaY * 1.0 / deltaX);

        if (colVisible || !rowVisible) {
            int pX1 = (int) getCursor(TChan.Cursor_col_1);
            int pX2 = (int) getCursor(TChan.Cursor_col_2);
            Measure measure = getHardwareMeasure(curCh-1);
            double px1Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1);
            double px2Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2);
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) {
                px1Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);
                px2Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);
            }
            long posX1 = Tools.TimebaseToPix(chIdx,px1Position);
            long posX2 = Tools.TimebaseToPix(chIdx,px2Position);
            if(posX1<0 || posX1 >1799){
                pX1 = -1;
            }
            if(posX2<0 || posX2 >1799){
                pX2 = -1;
            }

            if (ChannelFactory.isChOpen(curCh-1)
                    && measure != null) {
                measure.setMeasureCursor(pX1, pX2);
                measure.MeasureItemEnable(MEASURE_CURSOR_X1, true);
                measure.MeasureItemEnable(MEASURE_CURSOR_X2, true);
            }
        }

    }

    private void calDataXY() {
        double curChX = WaveManage.get().getPositionY(TChan.Ch1);
        double curChY = WaveManage.get().getPositionY(TChan.Ch2);
        double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();
        double chEveryY = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();
        double x1 = (getCursor(TChan.Cursor_col_1) - curChX) * chEveryX;
        double x2 = (getCursor(TChan.Cursor_col_2) - curChX) * chEveryX;
        double y1 = (curChY - getCursor(TChan.Cursor_row_1)) * chEveryY;
        double y2 = (curChY - getCursor(TChan.Cursor_row_2)) * chEveryY;
        double deltaY = Math.abs(y1 - y2);
        double deltaX = Math.abs(x1 - x2);
        String unitX = ChannelFactory.getProbeType(ChannelFactory.CH1);
        String unitY = ChannelFactory.getProbeType(ChannelFactory.CH2);
        dataMap.put(X1, "X1:" + SPACE + (TBookUtil.getFourFromD_(x1) + unitX));
        dataMap.put(X2, "X2:" + SPACE + (TBookUtil.getFourFromD_(x2) + unitX));
        dataMap.put(XDelta, "ΔX:" + SPACE + (TBookUtil.getFourFromD_(deltaX) + unitX));
        dataMap.put(XFre, "");
        dataMap.put(Y1, "Y1:" + SPACE + (TBookUtil.getFourFromD_(y1) + unitY));
        dataMap.put(Y2, "Y2:" + SPACE + (TBookUtil.getFourFromD_(y2) + unitY));
        dataMap.put(YDelta, "ΔY:" + SPACE + (TBookUtil.getFourFromD_(deltaY) + unitY));
        dataMap.put(S, "ΔV/Δt:" + SPACE + TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + unitY + "/" + unitX);
//        dataMap.put(X1CursorInteraction, SPACE+SPACE + SPACE + "---" );
//        dataMap.put(X2CursorInteraction, SPACE+SPACE + SPACE + "---");
//        dataMap.put(VoltgaeDelta,"ΔV:" + SPACE + "---");
//        dataMap.put(VoltageDivideXDelta,"ΔV/Δt:" + SPACE + "---");
        Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 0, 0);
    }

    public void setRowMeasureVisible(boolean b) {
        this.rowVisible = b;
//        drawMeasure();
        setData();

    }

    public void setColMeasureVisible(boolean b) {
        this.colVisible = b;
//        drawMeasure();
        setData();
    }


    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            int id = eventBase.getId();
            if (id== EventFactory.EVENT_CH_MEASURE_UPDATE
                    || id == EventFactory.EVENT_MATH_MEASURE_UPDATE
                    || id == EventFactory.EVENT_REF_MEASURE_UPDATE) {
                int chan = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
                if (chan == TChan.AUTO) { //自由选择模式
                    chan = WaveManage.get().getCurCh();
                } else {
                    chan = TChan.toUiChNo(chan);
                }
                int chIdx = TChan.toFpgaChNo(chan);

                switch(id){
                    case EventFactory.EVENT_CH_MEASURE_UPDATE:
                        if(!ChannelFactory.isDynamicCh(chIdx)){
                            return;
                        }
                        break;
                    case EventFactory.EVENT_MATH_MEASURE_UPDATE:
                        if(!ChannelFactory.isMathCh(chIdx)){
                            return;
                        }
                        break;
                    case EventFactory.EVENT_REF_MEASURE_UPDATE:
                        if(!ChannelFactory.isRefCh(chIdx)){
                            return;
                        }
                        break;
                }

                Measure measure = getHardwareMeasure(chIdx);
                int idx1 = MEASURE_CURSOR_X1;
                int idx2 = MEASURE_CURSOR_X2;
                //形式计算单位用v方便后续函数换算，但实际显示单位是实时获取的，并在计算后添加到计算结果后面
                String realUnitY = ChannelFactory.getProbeType(TChan.toFpgaChNo(chan));
                String unitY = "V";
//                Log.d("TAG", "ch: " + measure.getChIdx() + ",update: "  + measure.isMeasureItemValid(idx1));
                float itemCursorX1Interaction = 0.0F;
                float itemCursorX2Interaction = 0.0F;
                if (ChannelFactory.isChOpen(chan-1) && measure != null) {
                    //如果idx1，idx2有效就分别放数据， 全都有效则更新 v/t数据，如果有一个不有效就失效单个数据以及v/t
                    if(measure.isMeasureItemValid(idx1) || measure.isMeasureItemValid(idx2)) {
                        if(measure.isMeasureItemValid(idx1)){
                            itemCursorX1Interaction = measure.getMeasureItemVal(idx1);
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX1Interaction) + realUnitY);
                            } else {
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX1Interaction) + realUnitY);
                            }
                        }
                        if(measure.isMeasureItemValid(idx2)){
                            itemCursorX2Interaction = measure.getMeasureItemVal(idx2);
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX2Interaction) + realUnitY);
                            } else {
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX2Interaction) + realUnitY);
                            }
                        }
                        float deltaVolatage = Math.abs(itemCursorX2Interaction - itemCursorX1Interaction);
                        dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + TBookUtil.getFourFromD_(Math.abs(itemCursorX2Interaction - itemCursorX1Interaction)) + realUnitY);
                        String voltageTime;
                        if (xDeltaUnit != null) {
                            String voltageUnitDataDetail = TBookUtil.getFourFromD_(Math.abs(itemCursorX2Interaction - itemCursorX1Interaction)) + unitY;
                            String voltageUnitDetail = voltageUnitDataDetail.substring(voltageUnitDataDetail.length() - 2);
                            realUnitY = voltageUnitDataDetail.charAt(voltageUnitDataDetail.length() - 2) + realUnitY;
                            //传递电压，需要固定电压单位，然后for循环转换时间单位
                            if (deltaVolatage != 0) {
                                voltageTime = calculateVoltageOverTime(deltaVolatage, voltageUnitDetail, realUnitY, xDeltaCalculate);
                            } else {
                                voltageTime = "---";
                            }
                            dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + voltageTime);
                        }
                    }
                    if(!measure.isMeasureItemValid(idx1) || !measure.isMeasureItemValid(idx2)){
                        if(!measure.isMeasureItemValid(idx1)){
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + "---");
                            } else {
                                dataMap.put(X1CursorInteraction, "---");
                            }
                        }
                        if(!measure.isMeasureItemValid(idx2)){
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + "---");
                            } else {
                                dataMap.put(X2CursorInteraction, "---");
                            }
                        }
                        dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + "---");
                        dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + "---");
                    }

                }
                drawMeasure();
            }
        }
    };

    /**
     * 计算电压除以时间的值，并返回指定单位的结果
     *
     * @param voltageValue 电压数值
     * @param voltageUnit  电压单位 (V, mV, kV, uV, μV)
     * @param timeValue    时间数值
     * @return 计算结果
     * @throws IllegalArgumentException 如果单位不支持
     */
    public static String calculateVoltageOverTime(
            double voltageValue,
            String voltageUnit,
            String realUnit,
            double timeValue) {

        // 标准化单位字符串（去除空格，转小写用于查找）
        String vUnit = voltageUnit.trim();
        double voltageInVolts = voltageValue;
        double timeInSeconds = timeValue;
        // 计算基础结果 (V/s)
        double resultInVs = voltageInVolts / timeInSeconds;

        double resultVoltage = convertToVoltageUnit(resultInVs, vUnit);
        String result = autoConvert(resultVoltage, realUnit);
        return result;
    }


    /**
     * 将 V/s 转换为指定电压单位的数值（仍以 /s 为时间单位）
     */
    private static double convertToVoltageUnit(double voltsPerSecond, String voltageUnit) {
        switch (voltageUnit) {
            case "V":
                return voltsPerSecond;
            case "mV":
                return voltsPerSecond * 1000;    // 1 V = 1000 mV
            case "kV":
                return voltsPerSecond / 1000;    // 1 kV = 1000 V
            case "MV":
                return voltsPerSecond / 1000000; // 1 MV = 1e6 V
            case "μV":
            case "uV":
                return voltsPerSecond * 1000000; // 1 V = 1e6 μV
            case "nV":
                return voltsPerSecond * 1000000000;
            case "pV":
                return voltsPerSecond * 1000000000000L;
            default:
                return 0;
        }
    }

    public static String autoConvert(double rateValue, String voltageUnit) {
        // 秒的单位数组 + 秒与该单位的换算因子
        String[] timeUnits = {"s", "ms", "μs", "ns", "ps"};
        double[] factors = {1, 1_000, 1_000_000, 1_000_000_000, 1_000_000_000_000L};

        for (int i = 0; i < timeUnits.length; i++) {
            double value = rateValue / factors[i];
            // 判断这个时间单位是否让值更直观
            if (value >= 0.1 && value < 1000) {
                String formatted = formatMaxDigits(value, 4);
                return formatted + voltageUnit + "/" + timeUnits[i];
            }
        }

        // 如果都不合适，返回原速率
        return String.format("%.6f %s/s", rateValue, voltageUnit);
    }

    private static String formatMaxDigits(double num, int maxDigits) {
        String intPart = String.valueOf((long) Math.floor(num));
        int integerLength = intPart.length();

        // 可保留小数位数
        int decimalPlaces = Math.max(0, maxDigits - integerLength);
        return String.format("%." + decimalPlaces + "f", num);
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
    //endregion

    private void updateStringWidth(){

        String ellipsizedVoltageDivideX1 = TextUtils.ellipsize(
                dataMap.get(X1CursorInteraction),
                textPaint,
                100,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(X1CursorInteraction,ellipsizedVoltageDivideX1);

        String ellipsizedVoltageDivideX2 = TextUtils.ellipsize(
                dataMap.get(X2CursorInteraction),
                textPaint,
                100,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(X2CursorInteraction,ellipsizedVoltageDivideX2);

        String ellipsizedY1 = TextUtils.ellipsize(
                dataMap.get(Y1),
                textPaint,
                125,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(Y1,ellipsizedY1);
        String ellipsizedY2 = TextUtils.ellipsize(
                dataMap.get(Y2),
                textPaint,
                125,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(Y2,ellipsizedY2);
        String ellipsizedYDelta = TextUtils.ellipsize(
                dataMap.get(YDelta),
                textPaint,
                125,
                TextUtils.TruncateAt.END
        ).toString();
        dataMap.put(YDelta,ellipsizedYDelta);
    }
}
