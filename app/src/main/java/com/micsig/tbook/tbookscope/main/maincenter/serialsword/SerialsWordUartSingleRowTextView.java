package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.graphics.Canvas;
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
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

public class SerialsWordUartSingleRowTextView extends View {
    private static final String TAGG = "SerialsWordUartSingleRowTextView";

    public static final String TYPE_AXCII = "0";
    public static final String TYPE_BIN = "00000000";
    public static final String TYPE_HEX_NINE = "0F0";
    public static final String TYPE_HEX_OTHER = "FF";

    private Context context;
    private Paint paint;
    private int padding = 10;
    private ArrayList<SerialBusTxtStruct.UartStruct> list = new ArrayList<>();
    private String showType = TYPE_HEX_OTHER;
    private int chType = ISerialsWord.TYPE_S1;
    private int s1Color, s2Color;
    private int bits, check;

    public SerialsWordUartSingleRowTextView(Context context) {
        this(context, null);
    }

    public SerialsWordUartSingleRowTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordUartSingleRowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        paint = new Paint();
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        s1Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S1);
        s2Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S2);
    }

    public void setList(int bits, int check, String showType, int chType, ArrayList<SerialBusTxtStruct.UartStruct> list) {
        this.bits = bits;
        this.check = check;
        this.showType = showType;
        this.chType = chType;
        this.list = list;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (list != null) {
            int height = getMeasuredHeight();
            int textLeftWidth = showType.equals(TYPE_HEX_OTHER) || showType.equals(TYPE_HEX_NINE) ? 16 : showType.equals(TYPE_BIN) ? 95 : 10;
            int textLeftHeight = 11;
            int textRightWidth = 10;
            int leftListWidth;
            if (showType.equals(TYPE_HEX_OTHER)) {
                leftListWidth = (int) getResources().getDimension(R.dimen.uart_hex_bitOther_width);
            } else if (showType.equals(TYPE_HEX_NINE)) {
                leftListWidth = (int) getResources().getDimension(R.dimen.uart_hex_bit9_width);
            } else if (showType.equals(TYPE_BIN)) {
                leftListWidth = (int) getResources().getDimension(R.dimen.uart_bin_width);
            } else {
                leftListWidth = GlobalVar.get().getMainWave().x;
            }
            for (int i = 0; i < list.size(); i++) {
                int xLeft = i * (textLeftWidth + (TYPE_AXCII.equals(showType) ? 10
                        : TYPE_HEX_NINE.equals(showType) ? 25
                        : TYPE_BIN.equals(showType) ? 20 : 15)) + padding;
                String s;
                int bitWidth = this.check == 0 ? this.bits : this.bits - 1;
                bitWidth += 5;
                if (TYPE_BIN.equals(showType)) {
//                    s = Integer.toBinaryString(list.get(i).Data & 0xFF);
//                    s = String.format("%08d", Integer.valueOf(s));
                    s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Binary, list.get(i).Data, bitWidth);
                } else if (TYPE_HEX_OTHER.equals(showType)) {
//                    s = Integer.toHexString(list.get(i).Data & 0xFF).toUpperCase();
//                    s = s.length() < 2 ? "0" + s : s;
                    s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(i).Data, bitWidth);
                } else if (TYPE_HEX_NINE.equals(showType)) {
//                    s = Integer.toHexString(list.get(i).Data & 0xFFF).toUpperCase();
//                    s = s.length() < 2 ? "00" + s : (s.length() < 3 ? "0" + s : s);
                    s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(i).Data, 9);
                } else {
                    s = Tools.getASCIIFromInt(list.get(i).Data, " ");
                }
                if (chType != ISerialsWord.TYPE_S12) {
                    paint.setColor(list.get(i).Color);
                    canvas.drawText(s, xLeft, height / 2 + textLeftHeight / 2, paint);
                } else {
                    int color= TChan.getChannelColor(context, list.get(i).Ch);
                    paint.setColor(color);
                    paint.setAlpha(200);
                    Rect textRect = Tools.getTextRect(s, paint);
                    canvas.drawRect(xLeft, 2, xLeft + textRect.width() + 4, height - 2, paint);
                    paint.setColor(list.get(i).Color);
                    paint.setAlpha(255);
                    canvas.drawText(s, xLeft, height / 2 + textLeftHeight / 2, paint);
                }
                if (!TYPE_AXCII.equals(showType)) {
                    int xRight = i * (textRightWidth + 10) + padding + leftListWidth;

                    if (chType != ISerialsWord.TYPE_S12) {
                        paint.setColor(list.get(i).Color);
                        canvas.drawText(Tools.getASCIIFromInt(list.get(i).Data, "."), xRight, height / 2 + textLeftHeight / 2, paint);
                    } else {
                        int color= TChan.getChannelColor(context, list.get(i).Ch);
                        paint.setColor(color);
                        paint.setAlpha(200);
                        canvas.drawRect(xRight - 2, 2, xRight + 15, height - 2, paint);
                        paint.setColor(list.get(i).Color);
                        paint.setAlpha(255);
                        canvas.drawText(Tools.getASCIIFromInt(list.get(i).Data, "."), xRight, height / 2 + textLeftHeight / 2, paint);
                    }
                }
            }
        }
    }
}
