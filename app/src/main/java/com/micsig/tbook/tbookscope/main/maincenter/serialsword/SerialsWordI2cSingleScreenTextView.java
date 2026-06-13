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

public class SerialsWordI2cSingleScreenTextView extends View {
    private Context context;
    private Paint paint;
    private DashPathEffect dashPathEffect;
    private int width;
    private int rowHeight;
    private ArrayList<SerialBusTxtStruct.I2cStruct> list = new ArrayList<>();
    private int showLine;
    private boolean showMs = false;
    private int offset = 0;

    private int chWidth;
    private int timeWidth;
    private int addrWidth;
    private int dataWidth;
    private int confirmWidth;
    private int triggerWidth;
    private int restartWidth;

    public SerialsWordI2cSingleScreenTextView(Context context) {
        this(context, null);
    }

    public SerialsWordI2cSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordI2cSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        chWidth = (int) getResources().getDimension(R.dimen.i2c_ch_width);
        timeWidth = (int) getResources().getDimension(R.dimen.i2c_time_width);
        addrWidth = (int) getResources().getDimension(R.dimen.i2c_addr_width);
        dataWidth = (int) getResources().getDimension(R.dimen.i2c_data_width);
        confirmWidth = (int) getResources().getDimension(R.dimen.i2c_confirm_width);
        triggerWidth = (int) getResources().getDimension(R.dimen.i2c_trigger_width);
        restartWidth = (int) getResources().getDimension(R.dimen.i2c_restart_width);
    }

    public void setData(ArrayList<SerialBusTxtStruct.I2cStruct> list, int showLine, boolean showMs) {
        this.list = list;
        this.showLine = showLine;
        this.showMs = showMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0, drawLine = 0; i < list.size() && i < showLine; i++) {
            SerialBusTxtStruct.I2cStruct bean = list.get(i);
            String ch = bean.Ch;
            String time = TBookUtil.getStringFrom10us(bean.CurTime);
            String addr = String.valueOf(bean.Addr);
            String data = bean.Data.trim();
            String confirm = bean.Confirm ? "" : "X";
            String trigger = bean.Trigger ? "Yes" : "";
            String restart = String.valueOf(bean.Reboot ? "Yes" : "");
            Rect chRect = Tools.getTextRect("S1", paint);
            Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);
            Rect addrRect = Tools.getTextRect(addr, paint);
            Rect dataRect = Tools.getTextRect(data, paint);
            Rect confirmRect = Tools.getTextRect(confirm, paint);
            Rect triggerRect = Tools.getTextRect(trigger, paint);
            Rect restartRect = Tools.getTextRect(restart, paint);
            int y = drawLine * rowHeight + rowHeight / 2 + chRect.height() / 2 + offset;
            paint.setColor(getResources().getColor(R.color.textColor));
            canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);
            canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);
            canvas.drawText(addr, chWidth + timeWidth + addrWidth / 2 - addrRect.width() / 2, y, paint);

            int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_I2C + 1;
            for (int j = 0; j < curDrawLine; j++) {
                if (j != curDrawLine - 1) {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C
                            , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C)
                            , chWidth + timeWidth + addrWidth + 15, y + j * rowHeight, paint);
                } else {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C)
                            , chWidth + timeWidth + addrWidth + 15, y + j * rowHeight, paint);
                    drawLine(canvas, y + j * rowHeight + 6);//最后一行数据下面画点线
                }
                drawLine++;
            }

            canvas.drawText(trigger, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);
            canvas.drawText(restart, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth + triggerWidth + restartWidth / 2 - restartRect.width() / 2, y, paint);
            paint.setColor(Color.RED);
            canvas.drawText(confirm, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth / 2 - confirmRect.width() / 2, y, paint);
        }
    }


    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }
}
