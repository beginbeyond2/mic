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
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.ICharacterEncoding;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;

public class SerialsWordM429SingleScreenTextView extends View {
    private Context context;
    private Paint paint;
    private DashPathEffect dashPathEffect;
    private int width;
    private int rowHeight;
    private ArrayList<SerialBusTxtStruct.Arinc429Struct> list = new ArrayList<>();
    private int showLine;
    private boolean showMs = false;
    private int offset = 0;

    private int chWidth;
    private int timeWidth;
    private int labelWidth;
    private int sdiWidth;
    private int dataWidth;
    private int ssmWidth;
    private int errorWidth;
    private int triggerWidth;

    public SerialsWordM429SingleScreenTextView(Context context) {
        this(context, null);
    }

    public SerialsWordM429SingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordM429SingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);
        paint = new Paint();
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        width = GlobalVar.get().getMainWave().x;
        rowHeight = (int) context.getResources().getDimension(R.dimen.formHeightDetail);
        offset = -2;

        chWidth = (int) getResources().getDimension(R.dimen.arin429_ch_width);
        timeWidth = (int) getResources().getDimension(R.dimen.arin429_time_width);
        labelWidth = (int) getResources().getDimension(R.dimen.arin429_label_width);
        sdiWidth = (int) getResources().getDimension(R.dimen.arin429_sdi_width);
        dataWidth = (int) getResources().getDimension(R.dimen.arin429_data_width);
        ssmWidth = (int) getResources().getDimension(R.dimen.arin429_ssm_width);
        errorWidth = (int) getResources().getDimension(R.dimen.arin429_error_width);
        triggerWidth = (int) getResources().getDimension(R.dimen.arin429_trigger_width);
    }

    public void setData(ArrayList<SerialBusTxtStruct.Arinc429Struct> list, int showLine, boolean showMs) {
        this.list = list;
        this.showLine = showLine;
        this.showMs = showMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0, drawLine = 0; i < list.size() && i < showLine; i++) {
            SerialBusTxtStruct.Arinc429Struct bean = list.get(i);
            String ch = bean.Ch;
            String time = TBookUtil.getStringFrom10us(bean.CurTime);
//          String label = String.valueOf(bean.Label);
            String label = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Octal, bean.Label, 3 * 4);
            String sdi = bean.SDI;
            String data = bean.Data.trim();
            String ssm = bean.SSM;
            String error = String.valueOf(bean.Error);
            String trigger = bean.Trigger ? "Yes" : "";
            Rect chRect = Tools.getTextRect("S1", paint);
            Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);
            Rect labelRect = Tools.getTextRect(label, paint);
            Rect sdiRect = Tools.getTextRect(sdi, paint);
            Rect dataRect = Tools.getTextRect(data, paint);
            Rect ssmRect = Tools.getTextRect(ssm, paint);
            Rect errorRect = Tools.getTextRect(error, paint);
            Rect triggerRect = Tools.getTextRect(trigger, paint);
            int y = drawLine * rowHeight + rowHeight / 2 + chRect.height() / 2 + offset;
            paint.setColor(getResources().getColor(R.color.textColor));
            canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);
            canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);
            canvas.drawText(label, chWidth + timeWidth + labelWidth / 2 - labelRect.width() / 2, y, paint);
            canvas.drawText(sdi, chWidth + timeWidth + labelWidth + sdiWidth / 2 - sdiRect.width() / 2, y, paint);

            int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_429 + 1;
            for (int j = 0; j < curDrawLine; j++) {
                if (j != curDrawLine - 1) {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_429
                            , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_429)
                            , chWidth + timeWidth + labelWidth + sdiWidth + 15, y + j * rowHeight, paint);
                } else {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_429)
                            , chWidth + timeWidth + labelWidth + sdiWidth + 15, y + j * rowHeight, paint);
                    drawLine(canvas, y + j * rowHeight + 6);//最后一行数据下面画点线
                }
                drawLine++;
            }

            canvas.drawText(ssm, chWidth + timeWidth + labelWidth + sdiWidth + dataWidth + ssmWidth / 2 - ssmRect.width() / 2, y, paint);
            canvas.drawText(trigger, chWidth + timeWidth + labelWidth + sdiWidth + dataWidth + ssmWidth + errorWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);
            paint.setColor(Color.RED);
            canvas.drawText(error, chWidth + timeWidth + labelWidth + sdiWidth + dataWidth + ssmWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);
        }
    }

    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }
}
