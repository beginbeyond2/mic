package com.micsig.tbook.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LeftPositionView extends View {
    private static final String TAG = "LeftPositionView";

    public static final int CH1 = 0;
    public static final int CH2 = 1;
    public static final int CH3 = 2;
    public static final int CH4 = 3;
    public static final int CH5 = 4;
    public static final int CH6 = 5;
    public static final int CH7 = 6;
    public static final int CH8 = 7;

    private Context context;
    private Paint paint;
    private int width, height;
    private int textAllHeight;
    private int startPos;
    private boolean visible;

    private ArrayList<LeftPosBean> list = new ArrayList<LeftPosBean>();

    public LeftPositionView(Context context) {
        this(context, null);
    }

    public LeftPositionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftPositionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        width = 72;
        height = 650;
        paint = new Paint();
        paint.setTextSize(16);
        paint.setAntiAlias(true);
        visible = true;

        startPos = 0;
        textAllHeight = 30;
        list.add(new LeftPosBean(0, getResources().getColor(R.color.color_Ch1), 100, 100, "100", "mV"));
        list.add(new LeftPosBean(1, getResources().getColor(R.color.color_Ch2), 200, 200, "1", "V"));
        list.add(new LeftPosBean(2, getResources().getColor(R.color.color_Ch3), 300, 300, "100", "mV"));
        list.add(new LeftPosBean(3, getResources().getColor(R.color.color_Ch4), 400, 400, "10.2", "V"));
        list.add(new LeftPosBean(4, getResources().getColor(R.color.color_Ch5), 500, 500, "100", "mV"));
        list.add(new LeftPosBean(5, getResources().getColor(R.color.color_Ch6), 600, 600, "1", "V"));
        list.add(new LeftPosBean(6, getResources().getColor(R.color.color_Ch7), 700, 700, "100", "mV"));
        list.add(new LeftPosBean(7, getResources().getColor(R.color.color_Ch8), 800, 800, "1", "V"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!visible) {
            return;
        }
        int x;
        int y;

        sort();
        fixPos();
        fixPos();

        for (LeftPosBean bean : list) {
            if (!bean.visible) {
                break;
            }
            x = (width - ScreenUtil.getTextWidth(paint, bean.number+bean.unit))/2;
            y = bean.tmpPos;
            paint.setColor(bean.color);
            canvas.drawText(bean.number+bean.unit, x, y + 5, paint);
//            canvas.drawText(bean.unit, width - ScreenUtil.getTextWidth(paint, bean.unit)
//                    , y + textAllHeight / 2 - 1, paint);
        }
    }

    private void sort() {
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).visible) {
                list.get(i).tmpPos = Integer.MAX_VALUE - i * textAllHeight * 2;
            } else {
                list.get(i).tmpPos = list.get(i).pos;
                if (list.get(i).tmpPos < startPos) {
                    list.get(i).tmpPos = startPos;
                } else if (list.get(i).tmpPos > height) {
                    list.get(i).tmpPos = height;
                }
            }
        }
        Collections.sort(list, new Comparator<LeftPosBean>() {
            @Override
            public int compare(LeftPosBean lhs, LeftPosBean rhs) {
                return lhs.tmpPos - rhs.tmpPos;
            }
        });
    }

    private void fixPos() {
        LeftPosBean bean1 = list.get(0);
        LeftPosBean bean2 = list.get(1);
        LeftPosBean bean3 = list.get(2);
        LeftPosBean bean4 = list.get(3);
        LeftPosBean bean5 = list.get(4);
        LeftPosBean bean6 = list.get(5);
        LeftPosBean bean7 = list.get(6);
        LeftPosBean bean8 = list.get(7);

        int visibleCount = 0;
        visibleCount += (list.get(0).visible ? 1 : 0);
        visibleCount += (list.get(1).visible ? 1 : 0);
        visibleCount += (list.get(2).visible ? 1 : 0);
        visibleCount += (list.get(3).visible ? 1 : 0);
        visibleCount += (list.get(4).visible ? 1 : 0);
        visibleCount += (list.get(5).visible ? 1 : 0);
        visibleCount += (list.get(6).visible ? 1 : 0);
        visibleCount += (list.get(7).visible ? 1 : 0);

        //TODO 8通道
        if (bean2.tmpPos - bean1.tmpPos < textAllHeight) {
            int center12 = (bean1.tmpPos + bean2.tmpPos) / 2;
            if (bean3.tmpPos - center12 < textAllHeight * 3 / 2) {
                int center123 = (bean1.tmpPos + bean2.tmpPos + bean3.tmpPos) / 3;
                if (bean4.tmpPos - center123 < textAllHeight * 2) {
                    int center1234 = (bean1.tmpPos + bean2.tmpPos + bean3.tmpPos + bean4.tmpPos) / 4;
                    bean1.tmpPos = center1234 - textAllHeight * 3 / 2;
                    bean2.tmpPos = center1234 - textAllHeight / 2;
                    bean3.tmpPos = center1234 + textAllHeight / 2;
                    bean4.tmpPos = center1234 + textAllHeight * 3 / 2;
                } else {
                    bean1.tmpPos = center123 - textAllHeight;
                    bean2.tmpPos = center123;
                    bean3.tmpPos = center123 + textAllHeight;
                }
            } else {
                bean1.tmpPos = center12 - textAllHeight / 2;
                bean2.tmpPos = center12 + textAllHeight / 2;
                if (bean4.tmpPos - bean3.tmpPos < textAllHeight) {
                    int center34 = (bean3.tmpPos + bean4.tmpPos) / 2;
                    bean3.tmpPos = center34 - textAllHeight / 2;
                    bean4.tmpPos = center34 + textAllHeight / 2;
                }
            }
        } else if (bean3.tmpPos - bean2.tmpPos < textAllHeight) {
            int center23 = (bean2.tmpPos + bean3.tmpPos) / 2;
            if (bean4.tmpPos - center23 < textAllHeight * 3 / 2) {
                int center234 = (bean2.tmpPos + bean3.tmpPos + bean4.tmpPos) / 3;
                bean2.tmpPos = center234 - textAllHeight;
                bean3.tmpPos = center234;
                bean4.tmpPos = center234 + textAllHeight;
            } else {
                bean2.tmpPos = center23 - textAllHeight / 2;
                bean3.tmpPos = center23 + textAllHeight / 2;
            }
        } else if (bean4.tmpPos - bean3.tmpPos < textAllHeight) {
            int center34 = (bean3.tmpPos + bean4.tmpPos) / 2;
            bean3.tmpPos = center34 - textAllHeight / 2;
            bean4.tmpPos = center34 + textAllHeight / 2;
        }

        if (visibleCount >= 1) {
            bean1.tmpPos = (int) Math.max(bean1.tmpPos, startPos + textAllHeight / 2.0);
            bean1.tmpPos = (int) Math.min(bean1.tmpPos, height - textAllHeight * (visibleCount - 1.0 / 2));
        }
        if (visibleCount >= 2) {
            bean2.tmpPos = (int) Math.max(bean2.tmpPos, startPos + textAllHeight * 3.0 / 2);
            bean2.tmpPos = (int) Math.min(bean2.tmpPos, height - textAllHeight * (visibleCount - 3.0 / 2));
        }
        if (visibleCount >= 3) {
            bean3.tmpPos = (int) Math.max(bean3.tmpPos, startPos + textAllHeight * 5.0 / 2);
            bean3.tmpPos = (int) Math.min(bean3.tmpPos, height - textAllHeight * (visibleCount - 5.0 / 2));
        }
        if (visibleCount == 4) {
            bean4.tmpPos = (int) Math.max(bean4.tmpPos, startPos + textAllHeight * 7.0 / 2);
            bean4.tmpPos = (int) Math.min(bean4.tmpPos, height - textAllHeight * (visibleCount - 7.0 / 2));
        }
    }

    public void setItemVisible(int chIndex, boolean visible) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).chIndex == chIndex) {
                list.get(i).visible = visible;
                break;
            }
        }
        invalidate();
    }

    public void setAllVisible(boolean visible) {
        this.visible = visible;
        invalidate();
    }

    public void setData(int chIndex, double pos, String number, String unit, int startPos) {
        int temp = (int)Math.round(pos);
        this.startPos = startPos;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).chIndex == chIndex) {
                index = i;
            }
        }
        list.get(index).pos = temp;
        list.get(index).tmpPos = temp;
        list.get(index).number = number;
        list.get(index).unit = unit;
        for (int i = 0; i < 2; i++) {
            String[] ss = i == 0 ? TBookUtil.unit1 : TBookUtil.unit2;
            for (String s : ss) {
                if (!StrUtil.isEmpty(s) && list.get(index).number.endsWith(s)) {
                    list.get(index).number = list.get(index).number.replace(s, "");
                    list.get(index).unit = s + list.get(index).unit;
                    break;
                }
            }
        }
        list.get(index).number = fixNumber(list.get(index).number);
        invalidate();
    }

    private String fixNumber(String num) {
        int count = 3;
        if (num.contains("-")) {
            return "-" + fixNumber(num.replace("-", ""));
        }
        if (num.contains(".")) {
            if (num.length() < count + 1) {
                while (num.length() < count + 1) {
                    num = num + "0";
                }
            } else if (num.length() > count + 1) {
                num = num.substring(0, count + 1);
                if (num.endsWith(".")) {
                    num = num.substring(0, num.length() - 1);
                }
            }
        } else if (!num.contains(".")) {
            if (num.length() < count) {
                num = num + ".";
                while (num.length() < count + 1) {
                    num = num + "0";
                }
            }
        }
        return num;
    }

    class LeftPosBean {
        int chIndex;
        int color;
        int pos;//实际的位置
        int tmpPos;//显示的位置
        String number;
        String unit;
        boolean visible;

        public LeftPosBean(int chIndex, int color, int pos, int tmpPos, String number, String unit) {
            this.chIndex = chIndex;
            this.color = color;
            this.pos = pos;
            this.tmpPos = tmpPos;
            this.number = number;
            this.unit = unit;
            this.visible = true;
        }

        @Override
        public String toString() {
            return "LeftPosBean{" +
                    "chIndex=" + chIndex +
                    ", color=" + color +
                    ", pos=" + pos +
                    ", tmpPos=" + tmpPos +
                    ", number='" + number + '\'' +
                    ", unit='" + unit + '\'' +
                    ", visible=" + visible +
                    '}';
        }
    }
}
