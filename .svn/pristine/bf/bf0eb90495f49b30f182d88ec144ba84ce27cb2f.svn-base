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

public class SerialsWordUartSingleScreenTextView extends View {
    private static final String TAG = "SerialsWordUartSingleScreenTextView";
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
    private int countOfLine;
    private int showLine;
    private int s1Color, s2Color;
    private int bits;
    private int check;
    private int offset=0;
    private int rowHeight;

    public SerialsWordUartSingleScreenTextView(Context context) {
        this(context, null);
    }

    public SerialsWordUartSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SerialsWordUartSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        paint = new Paint();
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        s1Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S1);
        s2Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S2);
        offset=-2;
        rowHeight = (int) context.getResources().getDimension(R.dimen.formHeightDetail);
    }

    public void setList(int bits, int check, String showType, int chType, int countOfLine, int showLine, ArrayList<SerialBusTxtStruct.UartStruct> list) {
        this.bits = bits;
        this.check = check;
        this.showType = showType;
        this.chType = chType;
        this.countOfLine = countOfLine;
        this.showLine = showLine;
        this.list = list;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (list != null) {
            for (int row = 0; row < showLine; row++) {
                int baseY = row * rowHeight+offset;
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
                for (int i = 0; i < countOfLine; i++) {
                    int xLeft = i * (textLeftWidth + (TYPE_AXCII.equals(showType) ? 10
                            : TYPE_HEX_NINE.equals(showType) ? 25
                            : TYPE_BIN.equals(showType) ? 20 : 15)) + padding;
                    String s;
                    int index = i + row * countOfLine;
                    if (index >= list.size()) break;
                    int bitWidth = this.check == 0 ? this.bits : this.bits - 1;
                    bitWidth += 5;
                    if (TYPE_BIN.equals(showType)) {
//                        s = Integer.toBinaryString(list.get(index).Data & 0xFF);
//                        s = String.format("%08d", Integer.valueOf(s));
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Binary, list.get(index).Data, bitWidth);
                    } else if (TYPE_HEX_OTHER.equals(showType)) {
//                        s = Integer.toHexString(list.get(index).Data & 0xFF).toUpperCase();
//                        s = s.length() < 2 ? "0" + s : s;
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(index).Data, bitWidth);
                    } else if (TYPE_HEX_NINE.equals(showType)) {
//                        s = Integer.toHexString(list.get(index).Data & 0xFFF).toUpperCase();
//                        s = s.length() < 2 ? "00" + s : (s.length() < 3 ? "0" + s : s);
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(index).Data, 9);
                    } else {
                        s = Tools.getASCIIFromInt(list.get(index).Data, " ");
                    }
                    if (chType != ISerialsWord.TYPE_S12) {
                        paint.setColor(list.get(index).Color);
                        canvas.drawText(s, xLeft, baseY + rowHeight / 2 + textLeftHeight / 2, paint);
                    } else {
                        int color=TChan.getChannelColor(context, list.get(index).Ch);
                        paint.setColor(color);
                        paint.setAlpha(200);
                        Rect textRect = Tools.getTextRect(s, paint);
                        canvas.drawRect(xLeft, baseY + 2, xLeft + textRect.width() + 4, baseY + rowHeight - 2, paint);
                        paint.setColor(list.get(index).Color);
                        paint.setAlpha(255);
                        canvas.drawText(s, xLeft, baseY + rowHeight / 2 + textLeftHeight / 2, paint);
                    }
                    if (!TYPE_AXCII.equals(showType)) {
                        int xRight = i * (textRightWidth + 10) + padding + leftListWidth;
                        if (chType != ISerialsWord.TYPE_S12) {
                            paint.setColor(list.get(index).Color);
                            canvas.drawText(Tools.getASCIIFromInt(list.get(index).Data, "."), xRight, baseY + rowHeight / 2 + textLeftHeight / 2, paint);
                        } else {
                            int color= TChan.getChannelColor(context, list.get(index).Ch);
                            paint.setColor(color);
                            paint.setAlpha(200);
                            canvas.drawRect(xRight - 2, baseY + 2, xRight + 15, baseY + rowHeight - 2, paint);
                            paint.setColor(list.get(index).Color);
                            paint.setAlpha(255);
                            canvas.drawText(Tools.getASCIIFromInt(list.get(index).Data, "."), xRight, baseY + rowHeight / 2 + textLeftHeight / 2, paint);
                        }
                    }
                }
            }
        }
    }
}
