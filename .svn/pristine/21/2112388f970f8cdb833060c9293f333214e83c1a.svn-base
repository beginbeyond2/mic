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

public class SerialsWordCanSingleRowTextView extends View {
    private Context context;
    private Paint paint;
    private DashPathEffect dashPathEffect;
    private int width;
    private int height;
    private SerialBusTxtStruct.CanStruct bean;
    private boolean showMs = false;

    private int chWidth;
    private int timeWidth;
    private int idWidth;
    private int typeWidth;
    private int dlcWidth;
    private int dataWidth;
    private int crcWidth;
    private int errorWidth;
    private int triggerWidth;

    public SerialsWordCanSingleRowTextView(Context context) {
        this(context, null);
    }

    public SerialsWordCanSingleRowTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordCanSingleRowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);
        paint = new Paint();
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        width = GlobalVar.get().getMainWave().x;
        height = (int) context.getResources().getDimension(R.dimen.formHeightDetail);

        chWidth = (int) getResources().getDimension(R.dimen.can_ch_width);
        timeWidth = (int) getResources().getDimension(R.dimen.can_time_width);
        idWidth = (int) getResources().getDimension(R.dimen.can_id_width);
        typeWidth = (int) getResources().getDimension(R.dimen.can_type_width);
        dlcWidth = (int) getResources().getDimension(R.dimen.can_dlc_width);
        dataWidth = (int) getResources().getDimension(R.dimen.can_data_width);
        crcWidth = (int) getResources().getDimension(R.dimen.can_crc_width);
        errorWidth = (int) getResources().getDimension(R.dimen.can_error_width);
        triggerWidth = (int) getResources().getDimension(R.dimen.can_trigger_width);
    }

    public void setData(SerialBusTxtStruct.CanStruct bean, boolean showMs) {
        this.bean = bean;
        this.showMs = showMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bean == null) return;

//        Logger.i(Command.TAG,"onDraw:"+bean.toString());

        String ch = bean.Ch;
        String time = TBookUtil.getStringFrom10us(bean.CurTime);
        String id =bean.getId(); //Integer.toHexString(bean.ID).toUpperCase();
        String type = bean.getTypeEnum(); //(bean.TypeEnum== SerialBusTxtStruct.CanStruct.Type_STDID?"SFF":"EFF");
//        String dlc = Integer.toHexString(bean.DLC).toUpperCase();
        String dlc = Integer.toString(bean.DLC);
        String data = bean.Data.trim();
//        String crc = Integer.toHexString(bean.CRC).toUpperCase();
        String crc = String.format("%0" + bean.crc_w/4 + "x", bean.CRC).toUpperCase();
        String error = (bean.getErrorEnum());
        String trigger = bean.Trigger ? "Yes" : "";
        Rect chRect = Tools.getTextRect("S1", paint);
        Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);
        Rect idRect = Tools.getTextRect(id, paint);
        Rect typeRect = Tools.getTextRect(type, paint);
        Rect dlcRect = Tools.getTextRect(dlc, paint);
        Rect dataRect = Tools.getTextRect(data, paint);
        Rect crcRect = Tools.getTextRect(crc, paint);
        Rect errorRect = Tools.getTextRect(error, paint);
        Rect triggerRect = Tools.getTextRect(trigger, paint);
        paint.setColor(getResources().getColor(R.color.textColor));
        int y = height / 2 + chRect.height() / 2;
        canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);
        canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);
        canvas.drawText(id, chWidth + timeWidth + idWidth / 2 - idRect.width() / 2, y, paint);
        canvas.drawText(type, chWidth + timeWidth + idWidth + typeWidth / 2 - typeRect.width() / 2, y, paint);
        canvas.drawText(dlc, chWidth + timeWidth + idWidth + typeWidth + dlcWidth / 2 - dlcRect.width() / 2, y, paint);

        int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_CAN + 1;
        for (int j = 0; j < curDrawLine; j++) {
            if (j != curDrawLine - 1) {
                String temp = data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN
                        , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN);
                String before = temp.substring(0, ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);
                String after = temp.substring(ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);
                canvas.drawText(before + "  " + after,
                        chWidth + timeWidth + idWidth + typeWidth + dlcWidth + 15, y + j * height, paint);
            } else {
                String temp = data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN);
                if (temp.length() > ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2) {
                    String before = temp.substring(0, ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);
                    String after = temp.substring(ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);
                    temp = before + "  " + after;
                }
                canvas.drawText(temp,
                        chWidth + timeWidth + idWidth + typeWidth + dlcWidth + 15, y + j * height, paint);
                drawLine(canvas, y + j * height + 6);//最后一行数据下面画点线
            }
        }

        canvas.drawText(crc, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth / 2 - crcRect.width() / 2, y, paint);
        canvas.drawText(trigger, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth + errorWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);
        paint.setColor(Color.RED);
        canvas.drawText(error, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);
    }

    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }
}
