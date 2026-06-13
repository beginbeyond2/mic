package com.micsig.tbook.ui.top.view.selectHorList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.ui.R;

import java.util.ArrayList;
import java.util.List;

public class EHorizontalSelectedView extends View {
    private static final String TAG = "EHorizontalSelectedView";
    private Context mContext;
    private Paint mOtherPaint;
    private Paint mSelectPaint;
    private Paint bgPaint;
    private List<String> data = new ArrayList<>();
    /**
     * 可见数
     */
    private int seeSize;
    private Rect mRect = new Rect();
    /**
     * 选中位置
     */
    private int selectIndex = 0;
    private float downX;
    private boolean isClick = false;
    private int mItemWidth;
    private float mOffset;
    private int selectColor;
    private int otherColor;
    private int selectBackground;
    private int otherBackground;
    private float otherTextSize;
    private float selectTextSize;

    public EHorizontalSelectedView(Context context) {
        this(context, null);
    }

    public EHorizontalSelectedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EHorizontalSelectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttrs(attrs);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.EHorizontalSelectedView);
        otherTextSize = typedArray.getDimension(R.styleable.EHorizontalSelectedView_otherTextSize, 20);
        selectTextSize = typedArray.getDimension(R.styleable.EHorizontalSelectedView_selectTextSize, 20);
        seeSize = typedArray.getInteger(R.styleable.EHorizontalSelectedView_seeSize, 1);
        otherColor = typedArray.getColor(R.styleable.EHorizontalSelectedView_otherColor, Color.RED);
        selectColor = typedArray.getColor(R.styleable.EHorizontalSelectedView_selectColor, Color.YELLOW);
        otherBackground = R.color.bg_topmenu_color;
        selectBackground = R.color.color_Backcolor_MainMenu2;
        typedArray.recycle();
    }

    private void initPaint() {
        mOtherPaint = new Paint();
        mOtherPaint.setAntiAlias(true);
        mOtherPaint.setTextSize(otherTextSize);
        mOtherPaint.setColor(otherColor);

        mSelectPaint = new Paint();
        mSelectPaint.setAntiAlias(true);
        mSelectPaint.setColor(selectColor);
        mSelectPaint.setTextSize(selectTextSize);

        bgPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取宽度
        int width = getWidth();
        int height = getHeight();
        // 获取每个条目的大小
        if (seeSize == 0) {
            return;
        }
        mItemWidth = width / seeSize;
        int tmp = 0;
        for (String datum : data) {
            mOtherPaint.getTextBounds(datum, 0, datum.length(), mRect);
            int textWidth = mRect.width();
            if (textWidth > tmp) {
                tmp = textWidth;
            }
        }
        // 修正文字过大导致长度bug
        mItemWidth = Math.max(mItemWidth, tmp);
        seeSize = width / mItemWidth;
        // | dfadf |  dsafa | afasdf |
        // 得到选中的条目
        // 画出第一个

        bgPaint.setColor(getResources().getColor(otherBackground));
        canvas.drawRect(0, 0, mItemWidth * (seeSize - 1) / 2, height, bgPaint);
        bgPaint.setColor(getResources().getColor(selectBackground));
        canvas.drawRect(mItemWidth * (seeSize - 1) / 2, 0, mItemWidth * (seeSize + 1) / 2, height, bgPaint);
        bgPaint.setColor(getResources().getColor(otherBackground));
        canvas.drawRect(mItemWidth * (seeSize + 1) / 2, 0, width, height, bgPaint);

        for (int j = 0; j < data.size(); j++) {
            String datum = data.get(j);
            mOtherPaint.getTextBounds(datum, 0, datum.length(), mRect);
            int textWidth = mRect.width();
            int textHeight = mRect.height();
            if (j != selectIndex) {
                // 画其他的
                if (j < selectIndex) {
                    int a = selectIndex - j;
                    canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 - a * mItemWidth + mOffset, height / 2 + textHeight / 2, mOtherPaint);
                } else {
                    int a = j - selectIndex;
                    canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 + a * mItemWidth + mOffset, height / 2 + textHeight / 2, mOtherPaint);
                }
            } else {
                canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 + mOffset, height / 2 + textHeight / 2, mSelectPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float scrollX = event.getX();
                mOffset = scrollX - downX;
                if (Math.abs(mOffset) > 10) {
                    isClick = false;
                }
                // 向右滑动
                if (scrollX > downX) {
                    // 如果滑动距离大于一个条目的大小，则减1
                    if (scrollX - downX >= mItemWidth / 2) {
                        if (selectIndex > 0) {
                            mOffset = mOffset - mItemWidth;
                            selectIndex = selectIndex - 1;
                            downX = downX + mItemWidth;
                            if (mOnRollingListener != null) {
                                mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                            }
                        }
                    }
                } else {
                    //向左滑动大于一个条目的大小,则加1
                    if (downX - scrollX >= mItemWidth / 2) {
                        if (selectIndex < data.size() - 1) {
                            mOffset = mOffset + mItemWidth;
                            selectIndex = selectIndex + 1;
                            downX = downX - mItemWidth;
                            if (mOnRollingListener != null) {
                                mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                            }
                        }
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if (isClick) {
                    float moveX = event.getX() - getMeasuredWidth() / 2;
                    int moveCount = 0;
                    if (Math.abs(moveX) < mItemWidth / 2) {
                        break;
                    } else if (moveX > 0) {
                        //moveCount为正数
                        moveCount = (int) ((moveX - mItemWidth / 2) / mItemWidth + 1);
                        if (selectIndex + moveCount > data.size() - 1) {
                            selectIndex = data.size() - 1;
                        } else {
                            selectIndex = selectIndex + moveCount;
                        }
                    } else if (moveX < 0) {
                        //moveCount为负数
                        moveCount = (int) ((-1 * moveX - mItemWidth / 2) / mItemWidth + 1) * -1;
                        if (selectIndex + moveCount < 0) {
                            selectIndex = 0;
                        } else {
                            selectIndex = selectIndex + moveCount;
                        }
                    }
                    if (mOnRollingListener != null) {
                        mOnRollingListener.onClick(selectIndex, data.get(selectIndex));
                    }
                }
                //抬起手指时，偏移量归零，相当于回弹。
                mOffset = 0;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        if (selectIndex > data.size()) {
            selectIndex = data.size() - 1;
        }
        this.selectIndex = selectIndex;
        invalidate();
    }

    public void setSelectTextColor(int color) {
        this.selectColor = color;
        invalidate();
    }

    public int getSelectColor() {
        return selectColor;
    }

    public void setOtherTextColor(int color) {
        this.otherColor = color;
        invalidate();
    }

    public String getSelectText() {
        return data.get(selectIndex);
    }

    public void setData(List<String> data) {
        this.data = data;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        invalidate();
    }

    public void setSeeSize(int seeSize) {
        this.seeSize = seeSize;
        invalidate();
    }

    private OnRollingListener mOnRollingListener;

    public void setOnRollingListener(OnRollingListener onRollingListener) {
        mOnRollingListener = onRollingListener;
    }

    public interface OnRollingListener {
        /**
         * 滚动监听
         *
         * @param position 角标
         * @param s        滚动的文字
         */
        void onRolling(int position, String s);

        void onClick(int position, String s);
    }

    public void setOtherTextSize(float otherTextSize) {
        this.otherTextSize = otherTextSize;
    }

    public void setSelectTextSize(float selectTextSize) {
        this.selectTextSize = selectTextSize;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.i(TAG, "keyCode= " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mOnRollingListener != null) {
                    if (selectIndex >= 1) {
                        selectIndex = selectIndex - 1;
                        mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                        invalidate();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (selectIndex < data.size() - 1) {
                    selectIndex = selectIndex + 1;
                    mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                    invalidate();
                }
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                if (getParent().getParent() instanceof TopViewSelectHorListToList) {
                    ((TopViewSelectHorListToList) getParent().getParent()).hide();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
