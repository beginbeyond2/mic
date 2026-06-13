package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;

public class TopViewNumberPicker extends View {
    private Context context;
    private Paint paint;
    /**
     * 当前展示的列表index，-1表示不展示
     */
    private int isExpand = -1;
    private int itemWidth, itemHeight;
    private int spaceWidth;
    private int dividerColor, bgColor, textColor;
    private String s1, s2;
    private Rect showRect, expandRect;
    private OnNumberPickerListener onNumberPickerListener;
    private int margin=9;

    public interface OnNumberPickerListener {
        void onChangedShow(String s1, String s2);
    }

    public TopViewNumberPicker(Context context) {
        this(context, null);
    }

    public TopViewNumberPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        s1 = "-5.6789";
        s2 = "-1";
        margin = 8;
        itemWidth = 64 + margin;
        itemHeight = 64 + margin;
        spaceWidth = 16;

        dividerColor = context.getResources().getColor(R.color.scaleDivider);
        bgColor = context.getResources().getColor(R.color.bgDialog);
        textColor = context.getResources().getColor(R.color.textColorNewRightViewEnable);
        paint = new Paint();
        paint.setTextSize(22);
        paint.setAntiAlias(true);
        showRect = new Rect(0, 0, 0, 0);
        expandRect = new Rect(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChangeShow) {
            changeShow();
        }
        String s = (s1 + s2).replace(".", "");
        Logger.d("onDraw1:" + s);
        //-9.9999和-9两组数，包含正负号，不包含小数点...
        //i==0,6是正负号，i==1包含小数点
        int textWidth = getTextWidth("0");
        int textHeight = getTextHeight("0");
        paint.setColor(dividerColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        showRect = new Rect(1, itemHeight * 5, itemWidth * 8 + spaceWidth, itemHeight * 6);
        canvas.drawRect(showRect.left, showRect.top, showRect.left + itemWidth * 6 , showRect.bottom, paint);
        canvas.drawRect(showRect.right - itemWidth * 2, showRect.top, showRect.right, showRect.bottom, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        canvas.drawRect(showRect.left, showRect.top, showRect.left + itemWidth * 6 , showRect.bottom, paint);
        canvas.drawRect(showRect.right - itemWidth * 2, showRect.top, showRect.right , showRect.bottom, paint);
        for (int i = 0; i < 8; i++) {
            String text = String.valueOf(s.charAt(i));
            int x;
            int y = itemHeight * 6 - itemHeight / 2 + textHeight / 2;
            if (i >= 6) {
                x = itemWidth * i + itemWidth / 2 - textWidth / 2 + spaceWidth;
            } else {
                x = itemWidth * i + itemWidth / 2 - textWidth / 2;
            }
            paint.setColor(textColor);
            canvas.drawText(text, x, y, paint);
            if (i == 1) {
                canvas.drawText(".", x + textWidth + 5, y, paint);
            }
            if (isExpand == i) {
                if (i == 0 || i == 6) {
                    paint.setColor(dividerColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    if (i >= 6) {
                        expandRect = new Rect(itemWidth * i + spaceWidth, itemHeight * 4, itemWidth * (i + 1) + spaceWidth, itemHeight * 6);
                    } else {
                        expandRect = new Rect(itemWidth * i, itemHeight * 4, itemWidth * (i + 1), itemHeight * 6);
                    }
                    canvas.drawRect(expandRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(bgColor);
                    if (i >= 6) {
                        canvas.drawRect(expandRect.left, expandRect.top, expandRect.right, expandRect.bottom, paint);
                    } else {
                        canvas.drawRect(expandRect.left + 1, expandRect.top, expandRect.right, expandRect.bottom, paint);
                    }
                    drawSelectBorder(canvas,i);
                    paint.setColor(textColor);
                    String symbol = text.equals("-") ? "+" : "-";
                    String[] symbols = {symbol, text};
                    for (int j = 0; j < 50; j++) {
                        int expandY = y + itemHeight * (-25 + j) + moveY;
                        if (expandY >= itemHeight * 4 + 10 && expandY <= itemHeight * 6) {
                            canvas.drawText(symbols[j % 2], x, expandY, paint);
                        }
                    }
                } else {
                    paint.setColor(dividerColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    if (i >= 6) {
                        expandRect = new Rect(itemWidth * i + spaceWidth, 0, itemWidth * (i + 1) + spaceWidth, itemHeight * 10 - 1);
                    } else {
                        expandRect = new Rect(itemWidth * i, 0, itemWidth * (i + 1), itemHeight * 10 - 1);
                    }
                    canvas.drawRect(expandRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(bgColor);
                    if (i >= 6) {
                        canvas.drawRect(expandRect.left, expandRect.top + 1, expandRect.right , expandRect.bottom, paint);
                    } else {
                        canvas.drawRect(expandRect.left, expandRect.top + 1, expandRect.right, expandRect.bottom, paint);
                    }
                    drawSelectBorder(canvas,i);
                    paint.setColor(textColor);
                    int number = Integer.valueOf(text);
                    for (int j = 0; j < 50; j++) {
                        int expandY = y + itemHeight * (-25 + j) + moveY;
                        if (expandY >= 0 && expandY <= itemHeight * 10) {
                            canvas.drawText(String.valueOf((number + 35 + j) % 10), x, expandY, paint);
                        }
                    }
                }

            }
        }
    }

    private void drawSelectBorder(Canvas canvas,int selectedIndex){
        Rect itemRect;

        if (selectedIndex >= 6) {
            int x=itemWidth * selectedIndex + spaceWidth+margin;
            int y=itemHeight*5+margin;
            itemRect=new Rect(x,y,x+itemWidth-margin*2,y+itemHeight-margin*2);
        } else {
            int x=itemWidth * selectedIndex+margin;
            int y=itemHeight*5+margin;
            itemRect=new Rect(x,y,x+itemWidth-margin*2,y+itemHeight-margin*2);
        }

        paint.setColor(Color.BLACK);
        canvas.drawRect(itemRect,paint);

    }

    public void setData(String s1, String s2, OnNumberPickerListener onNumberPickerListener) {
        if (s1.charAt(0) != '+' && s1.charAt(0) != '-') {
            s1 = "+" + s1;
        }
        if (s2.charAt(0) != '+' && s2.charAt(0) != '-') {
            s2 = "+" + s2;
        }
        this.isExpand = -1;
        this.s1 = s1;
        this.s2 = s2;
        this.onNumberPickerListener = onNumberPickerListener;
        invalidate();
    }

    public void openExpand(int index) {
        this.isExpand = index;
        handler.sendEmptyMessage(MSG_EXPAND_DELAY);
        invalidate();
    }

    public void addOne(int index, boolean isAdd) {
        if (isExpand != index) {
            openExpand(index);
        }
        handler.sendEmptyMessage(MSG_EXPAND_DELAY);
        moveY = isAdd ? -itemHeight : itemHeight;
        isChangeShow = true;
        invalidate();
    }

    /**
     * 当前的点是不是在显示范围内...
     */
    private boolean isContain(int x, int y) {
        if (showRect.contains(x, y)) {
            return true;
        }
        if (isExpand != -1 && expandRect.contains(x, y)) {
            return true;
        }
        return false;
    }

    private void changeShow() {
        if (isExpand != -1) {
            switch (isExpand) {
                case 0: {
                    int change = moveY / itemHeight % 2;
                    if (change == 1) {
                        if (s1.substring(0, 1).equals("+")) {
                            s1 = "-" + s1.substring(1);
                        } else if (s1.substring(0, 1).equals("-")) {
                            s1 = "+" + s1.substring(1);
                        }
                    }
                }
                break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5: {
                    int change = moveY / itemHeight;
                    String s11 = s1;
                    s1 = s1.replace(".", "");
                    int number = Integer.valueOf(s1.substring(1, isExpand + 1)) - change;
                    number += 100000;
                    String numberStr = String.valueOf(number);
                    if (numberStr.length() >= isExpand) {
                        numberStr = numberStr.substring(numberStr.length() - isExpand);
                    } else {
                        for (int i = 0; i < isExpand - numberStr.length(); i++) {
                            numberStr = "0" + numberStr;
                        }
                    }
                    s1 = s1.substring(0, 1) + numberStr + (isExpand == 5 ? "" : s1.substring(isExpand + 1));
                    s1 = s1.substring(0, 2) + "." + s1.substring(2);
                    Logger.d("changeShow:" + s11 + "," + s1 + "," + isExpand);
                }
                break;
                case 6: {
                    int change = moveY / itemHeight % 2;
                    if (change == 1) {
                        if (s2.substring(0, 1).equals("+")) {
                            s2 = "-" + s2.substring(1);
                        } else if (s2.substring(0, 1).equals("-")) {
                            s2 = "+" + s2.substring(1);
                        }
                    }
                }
                break;
                case 7: {
                    int change = moveY / itemHeight % 10;
                    int number = Integer.valueOf(s2.substring(1)) - change;
                    number = (number + 30) % 10;
                    s2 = s2.substring(0, 1) + number;
                }
                break;
            }
        }
        if (onNumberPickerListener != null) {
            onNumberPickerListener.onChangedShow(
                    s1.replace("+", ""), s2.replace("+", ""));
        }
        Logger.d("onDraw2:" + (s1 + s2).replace(".", "") + ",moveY:" + moveY);
        isChangeShow = false;
        moveY = 0;
    }

    int startX, startY;
    int moveY;
    boolean isChangeShow = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isContain((int) event.getX(), (int) event.getY())) {
                    return super.onTouchEvent(event);
                }
                moveY = 0;
                startX = (int) event.getX();
                startY = (int) event.getY();
                if (expandRect.contains(startX, startY)) {
                    handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                }
                if (showRect.contains(startX, startY)) {
                    if (startX <= itemWidth * 6) {
                        isExpand = startX / itemWidth;
                    } else if (startX >= itemWidth * 6 + spaceWidth) {
                        isExpand = (startX - spaceWidth) / itemWidth;
                    }
                    invalidate();
                }
                Logger.d("MotionEvent.ACTION_DOWN:" + moveY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (expandRect.contains(startX, startY)) {
                    int curY = (int) event.getY();
                    moveY = curY - startY;
                    handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                    invalidate();
                }
                Logger.d("MotionEvent.ACTION_MOVE:" + moveY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (moveY == 0) {
                    if (expandRect.contains(startX, startY)) {
                        moveY = -(((int) event.getY()) / itemHeight - 5) * itemHeight;
                    }
                } else if (moveY > 0) {
                    moveY = (moveY + itemHeight / 2) / itemHeight * itemHeight;
                } else {
                    moveY = (moveY - itemHeight / 2) / itemHeight * itemHeight;
                }
                Logger.d("MotionEvent.ACTION_UP:" + moveY);
                isChangeShow = true;
                invalidate();
                handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                break;
        }
        return true;
    }

    private static final int MSG_EXPAND_DELAY = 0x146;
    private static final int MSG_EXPAND_HIDE = 0x147;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_EXPAND_DELAY) {
                if (handler.hasMessages(MSG_EXPAND_HIDE)) {
                    handler.removeMessages(MSG_EXPAND_HIDE);
                }
                handler.sendEmptyMessageDelayed(MSG_EXPAND_HIDE, 3 * 1000);
            } else if (msg.what == MSG_EXPAND_HIDE) {
                isExpand = -1;
                invalidate();
            }
        }
    };

    private Rect rect = new Rect();

    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    private int getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }
}
