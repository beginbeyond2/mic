package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;

public class SerialsWordM1553bSingleScreenTextView extends View {
    private Context context;
    private Paint paint;
    private DashPathEffect dashPathEffect;
    private int width;
    private int rowHeight;
    private ArrayList<SerialBusTxtStruct.MilSTD1553bStruct> list = new ArrayList<>();
    private int showLine;
    private boolean showMs = false;
    private int offset = 0;

    private int chWidth;
    private int timeWidth;
    private int typeWidth;
    private int rAddrWidth;
    private int dataWidth;
    private int triggerWidth;
    private int errorWidth;

    public SerialsWordM1553bSingleScreenTextView(Context context) {
        this(context, null);
    }

    public SerialsWordM1553bSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordM1553bSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        paint = new Paint();
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        width = GlobalVar.get().getMainWave().x;
        rowHeight = (int) context.getResources().getDimension(R.dimen.formHeightDetail);
        offset = -2;

        chWidth = (int) getResources().getDimension(R.dimen.s1553b_ch_width);
        timeWidth = (int) getResources().getDimension(R.dimen.s1553b_time_width);
        typeWidth = (int) getResources().getDimension(R.dimen.s1553b_type_width);
        rAddrWidth = (int) getResources().getDimension(R.dimen.s1553b_raddr_width);
        dataWidth = (int) getResources().getDimension(R.dimen.s1553b_data_width);
        triggerWidth = (int) getResources().getDimension(R.dimen.s1553b_trigger_width);
        errorWidth = (int) getResources().getDimension(R.dimen.s1553b_error_width);
    }

    public void setData(ArrayList<SerialBusTxtStruct.MilSTD1553bStruct> list, int showLine, boolean showMs) {
        this.list = list;
        this.showLine = showLine;
        this.showMs = showMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0, drawLine = 0; i < list.size() && i < showLine; i++) {
            SerialBusTxtStruct.MilSTD1553bStruct bean = list.get(i);
            String ch = bean.Ch;
            String time = TBookUtil.getStringFrom10us(bean.CurTime);
            String type = bean.Type;
            String rAdr = bean.RAddr;
            String data = bean.Data.trim();
            String trigger = bean.Trigger ? "Yes" : "";
            String error = String.valueOf(bean.Error);
            Rect chRect = Tools.getTextRect("S1", paint);
            Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);
            Rect typeRect = Tools.getTextRect(type, paint);
            Rect rAdrRect = Tools.getTextRect(rAdr, paint);
            Rect dataRect = Tools.getTextRect(data, paint);
            Rect triggerRect = Tools.getTextRect(trigger, paint);
            Rect errorRect = Tools.getTextRect(error, paint);
            int y = drawLine * rowHeight + rowHeight / 2 + chRect.height() / 2 + offset;
            paint.setColor(getResources().getColor(R.color.textColor));
            canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);
            canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);
            canvas.drawText(type, chWidth + timeWidth + typeWidth / 2 - typeRect.width() / 2, y, paint);
            canvas.drawText(rAdr, chWidth + timeWidth + typeWidth + rAddrWidth / 2 - rAdrRect.width() / 2, y, paint);

            int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_1553B + 1;
            for (int j = 0; j < curDrawLine; j++) {
                if (j != curDrawLine - 1) {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_1553B
                            , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_1553B)
                            , chWidth + timeWidth + typeWidth + rAddrWidth + 15, y + j * rowHeight, paint);
                } else {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_1553B)
                            , chWidth + timeWidth + typeWidth + rAddrWidth + 15, y + j * rowHeight, paint);
                    drawLine(canvas, y + j * rowHeight + 6);//最后一行数据下面画点线
                }
                drawLine++;
            }

            canvas.drawText(trigger, chWidth + timeWidth + typeWidth + rAddrWidth + dataWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);
            paint.setColor(Color.RED);
            canvas.drawText(error, chWidth + timeWidth + typeWidth + rAddrWidth + dataWidth + triggerWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);
        }
    }

    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }
}
