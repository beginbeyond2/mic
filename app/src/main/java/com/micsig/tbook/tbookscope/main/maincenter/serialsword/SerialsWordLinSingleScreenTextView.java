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

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

public class SerialsWordLinSingleScreenTextView extends View {
    private Context context;
    private Paint paint;
    private DashPathEffect dashPathEffect;
    private int width;
    private int rowHeight;
    private ArrayList<SerialBusTxtStruct.LinStruct> list = new ArrayList<>();
    private int showLine;
    private boolean showMs = false;
    private int offset = 0;

    private int chWidth;
    private int timeWidth;
    private int idWidth;
    private int dataWidth;
    private int crcWidth;
    private int errorWidth;
    private int TriggerWidth;

    public SerialsWordLinSingleScreenTextView(Context context) {
        this(context, null);
    }

    public SerialsWordLinSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordLinSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        chWidth = (int) getResources().getDimension(R.dimen.lin_ch_width);
        timeWidth = (int) getResources().getDimension(R.dimen.lin_time_width);
        idWidth = (int) getResources().getDimension(R.dimen.lin_id_width);
        dataWidth = (int) getResources().getDimension(R.dimen.lin_data_width);
        crcWidth = (int) getResources().getDimension(R.dimen.lin_crc_width);
        errorWidth = (int) getResources().getDimension(R.dimen.lin_error_width);
        TriggerWidth = (int) getResources().getDimension(R.dimen.lin_trigger_width);
    }

    public void setData(ArrayList<SerialBusTxtStruct.LinStruct> list, int showLine, boolean showMs) {
        this.list = list;
        this.showLine = showLine;
        this.showMs = showMs;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0, drawLine = 0; i < list.size() && i < showLine; i++) {
            SerialBusTxtStruct.LinStruct bean = list.get(i);
            String ch = bean.Ch;
            String time = TBookUtil.getStringFrom10us(bean.CurTime);
            String id = Tools.ByteToHexString((byte) bean.Id);
            //FIXME 非LIN1.3时FPGA传过来的data会在末尾多出来校验位数据，这里去掉。
            String data = bean.Data.trim();
            int chan= TChan.findChanByValue(ch);
            if (chan==-1) chan=TChan.S1;
            chan=TChan.toFpgaChNo(chan);
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(chan);
            if (serialChannel != null) {
                LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
                if (linBus != null && linBus.getLinType() != LinBus.LIN_TYPE_1_3) {
                    if (data.length() > 2) {
                        data = data.substring(0, data.length() - 3);
                    }
                }
            }
            String error = String.valueOf(bean.Error);
            String check = Tools.ByteToHexString((byte) bean.Check);
            String trigger = bean.Trigger ? "Yes" : "";
            Rect chRect = Tools.getTextRect("S1", paint);
            Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);
            Rect idRect = Tools.getTextRect(id, paint);
            Rect dataRect = Tools.getTextRect(data, paint);
            Rect errorRect = Tools.getTextRect(error, paint);
            Rect checkRect = Tools.getTextRect(check, paint);
            Rect triggerRect = Tools.getTextRect(trigger, paint);
            int y = drawLine * rowHeight + rowHeight / 2 + chRect.height() / 2 + offset;
            paint.setColor(getResources().getColor(R.color.textColor));
            canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);
            canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);
            canvas.drawText(id, chWidth + timeWidth + idWidth / 2 - idRect.width() / 2, y, paint);

            int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_LIN + 1;
            for (int j = 0; j < curDrawLine; j++) {
                if (j != curDrawLine - 1) {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN
                            , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN)
                            , chWidth + timeWidth + idWidth + 15, y + j * rowHeight, paint);
                } else {
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN)
                            , chWidth + timeWidth + idWidth + 15, y + j * rowHeight, paint);
                    drawLine(canvas, y + j * rowHeight + 6);//最后一行数据下面画点线
                }
                drawLine++;
            }

            canvas.drawText(check, chWidth + timeWidth + idWidth + dataWidth + errorWidth + crcWidth / 2 - checkRect.width() / 2, y, paint);
            canvas.drawText(trigger, chWidth + timeWidth + idWidth + dataWidth + errorWidth + crcWidth + TriggerWidth / 2 - triggerRect.width() / 2, y, paint);
            paint.setColor(Color.RED);
            canvas.drawText(error, chWidth + timeWidth + idWidth + dataWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);
        }
    }


    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);
        canvas.drawLine(0, y, width, y, paint);
        paint.setPathEffect(null);
    }
}
