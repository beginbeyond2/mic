package com.micsig.tbook.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MTimeBaseSelector extends View {
    private String TAG = "MTimeBaseSelector";

    private Context context;
    private List<MRadioButton> list = new ArrayList<>();
    private Paint paint = new Paint();
    private Rect rectText = new Rect();
    private OnClickEvent onClickEvent;
    private int currItemIndex = 0;
    private final int backGroudLineColor = Color.parseColor("#012c4e");//Color.rgb(0x02, 0x69, 0x98);

    public interface OnClickEvent {
        void onTouchDown(int index, String value);

        void onTouchUp(int index, String value);

        void onItemChange(int index, String value);

    }

    public MTimeBaseSelector(Context context) {
        this(context, null);
    }

    public MTimeBaseSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTimeBaseSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(20);
        paint.setAntiAlias(true);
        this.setBackgroundColor(getResources().getColor(R.color.color_Division_Line_595959));
//        this.setBackgroundColor(backGroudLineColor);
    }

    public void initDataUI(List<String> list) {
        this.list.clear();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(new MRadioButton(false, list.get(i), i));
        }
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = 1802;//实际1099
        params.height = (int) (Math.ceil(1.0 * list.size() / MRadioButton.LENGTH) * MRadioButton.HEIGHT + Math.floor(1.0 * list.size() / MRadioButton.LENGTH) + 2);

//        params.y= (600-params.height-64);
//        params.x=1;

//        Logger.i(TAG,"height:"+params.height+" y:"+params.y+"  size:"+list.size());
        this.setLayoutParams(params);
        invalidate();
    }

    public void setSelectItems(String s) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).mText.equals(s)) {
                setSelectIndex(i);
                currItemIndex = i;
                return;
            }
        }
    }

    private MTimeBaseSelector setSelectIndex(int index) {
        if (index >= 0 && index < list.size()) {
            clearSelect();
            list.get(index).setChecked(true);
            currItemIndex = index;
            invalidate();
        }
        return this;
    }

    private void clearSelect() {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setChecked(false);
        }
    }

    private int getSelectIndex(MotionEvent event) {
        int index = -1;
        int x = (int) event.getX();
        int y = (int) event.getY();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(x, y)) {
                return i;
            }
        }
        return index;
    }


    public void setOnClickEvent(OnClickEvent onClickEvent) {
        this.onClickEvent = onClickEvent;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int index = getSelectIndex(event);
                if (index < 0) break;
                setSelectIndex(index);
                String value = list.get(index).mText;
                if (onClickEvent != null) onClickEvent.onTouchDown(index, value);

            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int index = getSelectIndex(event);
                if (index < 0) break;
                String value = list.get(index).mText;
                if (onClickEvent != null && index != currItemIndex)
                    onClickEvent.onItemChange(index, value);
                setSelectIndex(index);
            }
            break;
            case MotionEvent.ACTION_UP: {
                int index = getSelectIndex(event);
                if (index < 0) break;
                setSelectIndex(index);
                String value = list.get(index).mText;
                if (onClickEvent != null) onClickEvent.onTouchUp(index, value);
            }
            break;
            case MotionEvent.ACTION_CANCEL: {
                clearSelect();
            }
            break;
        }

        return true;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {

        float x = 0, y = -1 * MRadioButton.HEIGHT;
        for (int i = 0; i < list.size(); i++) {
            if (i % MRadioButton.LENGTH == 0) {
                y += MRadioButton.HEIGHT + 1;
                x = 1;
            }

            if (currItemIndex == i) {
                paint.setColor(list.get(i).selectBackColor);
                int reviseX = x == 1 ? -1 : 0;
                canvas.drawRect(x, y, x + MRadioButton.WIDTH + reviseX, y + MRadioButton.HEIGHT, paint);
                paint.setStyle(Paint.Style.STROKE);

                paint.setColor(list.get(i).selectBorderColor);
                //paint.setShader(list.get(i).getSelectShader(x+MRadioButton.WIDTH/2,y+MRadioButton.HEIGHT/2));
            } else {
                paint.setColor(list.get(i).backColor);
            }
            int reviseX = x == 1 ? -1 : 0;
            canvas.drawRect(x, y, x + MRadioButton.WIDTH + reviseX, y + MRadioButton.HEIGHT, paint);
            list.get(i).setRadioButtonRect(x, y);
            if (currItemIndex == i) {
                paint.setColor(list.get(i).selectTextColor);
            } else {
                paint.setColor(list.get(i).textColor);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(null);
            paint.getTextBounds(list.get(i).mText, 0, list.get(i).mText.length(), rectText);
            canvas.drawText(list.get(i).mText, x + (MRadioButton.WIDTH - rectText.width()) / 2, (MRadioButton.HEIGHT - rectText.height()) / 2 + rectText.height() + y, paint);

            x += MRadioButton.WIDTH + 1 + reviseX;
        }
        if (x < getWidth() && list.size() > 0) {
            paint.setColor(list.get(0).backColor);
            //canvas.drawRect(x, y, getWidth()-1, y + MRadioButton.HEIGHT, paint);
            for (; x < getWidth(); ) {
                float newX = (x + MRadioButton.WIDTH);
                canvas.drawRect(x, y, newX, y + MRadioButton.HEIGHT, paint);
                x = newX + 1;
            }
        }

        paint.setColor(list.get(0).backColor);
        canvas.drawRect(this.getWidth() - 2, 0, this.getWidth(), this.getHeight(), paint);

    }


    class MRadioButton {
        public final static int LENGTH = 12;
        public final static float WIDTH = ((1803 - (LENGTH + 1)) / LENGTH); // (总宽度-10个坚线)/9个按钮
        public final static int HEIGHT = 80;

        //        public  int backColor=R.color.color_Backcolor_MainMenu3;
        public int backColor = Color.rgb(0, 0, 0x0C);     //red:9  green:0x14  blue:0x26
        public int selectBorderColor = Color.parseColor("#02608d");//Color.rgb(0x2, 0x69, 0x98);   //red:255 green 0,blue:0
        public int textColor = Color.parseColor("#026998");//Color.rgb(0x02, 0x69, 0x98);     //red:255  green:255  blue:255
        public int selectTextColor = Color.WHITE;//Color.rgb(0x03, 0x07, 0x12);   //red:255  green:255  blue:255
        public int selectBackColor = Color.BLACK;

        private boolean mIsUiChecked;
        private String mText;
        private int mIndex;
        private RectF radioButtonRect = new RectF();


        MRadioButton(boolean isUiChecked, String text, int index) {
            mIsUiChecked = isUiChecked;
            mText = text;
            mIndex = index;
            textColor = getResources().getColor(R.color.textColor);
            backColor = getResources().getColor(R.color.bg_main_outside);
            selectTextColor = getResources().getColor(R.color.textColor);
            selectBorderColor = getResources().getColor(R.color.bgNewRightViewSelect);
            selectBackColor = getResources().getColor(R.color.color_Backcolor_MainMenu2);
        }

        public void setRadioButtonRect(float x, float y) {
            radioButtonRect.set(x, y, x + MRadioButton.WIDTH, y + MRadioButton.HEIGHT);
        }

        public void setChecked(boolean checked) {
            this.mIsUiChecked = checked;
        }

        public boolean contains(int x, int y) {
            return radioButtonRect.contains(x, y);
        }

        public Shader getSelectShader(float x, float y) {
            Shader selectShader = new RadialGradient(x, y, WIDTH / 3 * 2,
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, selectBackColor}, null, Shader.TileMode.REPEAT);
            return selectShader;
        }
    }

}
