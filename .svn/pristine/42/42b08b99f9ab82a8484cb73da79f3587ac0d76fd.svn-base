package com.micsig.tbook.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgManager;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by liwb on 2017/4/5
 * 这个控件有两种状态，一个是按钮状态，一个是拖动状态的选择。.
 */
public class MTriggerLevel extends View {
    private String TAG = "MTriggerLevel";

    //region 全局变量

    //显示模式
    public static final int TriggerLevel_Mode_Show_Button = 0x01;
    public static final int TriggerLevel_Mode_Show_Drag = 0x02;
    public static final int TriggerLevel_Mode_Show_Changing = 0x03;

    @IntDef({TriggerLevel_Mode_Show_Button, TriggerLevel_Mode_Show_Drag, TriggerLevel_Mode_Show_Changing})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TriggerLevel_Mode_Show {
    }

    //工作模式
    public static final int TriggerLevel_Mode_Work_Normal = 0x01;
    public static final int TriggerLevel_Mode_Work_HighLow = 0x02;
    public static final int TriggerLevel_Mode_Work_Logic = 0x03;

    @IntDef({TriggerLevel_Mode_Work_Normal, TriggerLevel_Mode_Work_HighLow, TriggerLevel_Mode_Work_Logic})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TriggerLevel_Mode_Work {
    }

    public static final int TriggerLevel_Mode_Work_Logic_High = 0x01;
    public static final int TriggerLevel_Mode_Work_Logic_Low = 0x02;
    public static final int TriggerLevel_Mode_work_Logic_None = 0x03;
    //endregion

    /**
     * 按钮高度
     */
    private static final int BUTTONHEIGHT = 106;
    /**
     * 滑动高度
     */
    private static final int DRAGHEIGHT = 1040;
    private static int waveHeight = 1040;
    /**
     * 中心位置
     */
//    private static final int CenterY = (DRAGHEIGHT - BUTTONHEIGHT) / 2-46;
    private static float CenterY = (DRAGHEIGHT - BUTTONHEIGHT) / 2.0f;
    private int currentTriggerIndex = 1; //当前触发类型，只有时边沿触发时才显示 外部


    //region 接口
    public interface OnMouseMoveListener {
        /**
         * @param view
         * @param deltaY     正数表示向上移动
         * @param Ch         1~4
         * @param openType   打开方式：OPENTYPE_TRIGGER, OPENTYPE_SERIALS1, OPENTYPE_SERIALS2
         * @param isFromUser 是否是来自用户的设置
         */
        void onMouseMove(View view, double deltaY, int Ch, @OpenType int openType, boolean isFromUser);

        void onUpClick(View view, int Ch, @OpenType int openType);

        void onDownClick(View view, int Ch, @OpenType int openType);

        void onMouseMoveComplete(View view, @OpenType int openType);
    }

    //打开时的种类
    public static final int OPENTYPE_TRIGGER = 0;
    public static final int OPENTYPE_SERIALS1 = 1;
    public static final int OPENTYPE_SERIALS2 = 2;
    public static final int OPENTYPE_SERIALS3 = 3;
    public static final int OPENTYPE_SERIALS4 = 4;

    public static boolean isOpenSerial(int type){
        if (type>=OPENTYPE_SERIALS1 && type<=OPENTYPE_SERIALS4){
            return true;
        }
        return false;
    }

    @IntDef({OPENTYPE_TRIGGER, OPENTYPE_SERIALS1, OPENTYPE_SERIALS2,OPENTYPE_SERIALS3,OPENTYPE_SERIALS4})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpenType {
    }

    public interface OnOpenCloseListener {
        void onOpen(@OpenType int type);

        void onClose(@OpenType int type);
    }
    //endregion

    //region attrs属性
    private Rect upRect, downRect;
    private int[] TriggerLevel_Mode_Work_Logic_state = new int[TChan.MaxLogicChan+1];
    private int channelCount = 4;
    private Context context;
    private OnOpenCloseListener onOpenCloseListener;
    @OpenType
    private int openType = OPENTYPE_TRIGGER;

    public void setOpenType(@OpenType int openType) {
        this.openType = openType;
    }

    public int getOpenType() {
        return openType;
    }

    public OnOpenCloseListener getOnOpenCloseListener() {
        return onOpenCloseListener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener onOpenCloseListener) {
        this.onOpenCloseListener = onOpenCloseListener;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    //设置切换状态
    public void setTriggerLevel_Mode_Work_Logic_state(int chId, int channelState) {
        TriggerLevel_Mode_Work_Logic_state[chId] = channelState;
        invalidate();
    }

    public void setTriggerLevel_Mode_Work_Logic_states(int... chId) {
        for (int i = 0; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
            TriggerLevel_Mode_Work_Logic_state[i] = TriggerLevel_Mode_work_Logic_None;
        }
        for (int i = 0; i < chId.length; i++) {
            TriggerLevel_Mode_Work_Logic_state[chId[i]] = TriggerLevel_Mode_Work_Logic_High;
        }
        invalidate();
    }

    public int[] getTriggerLevel_Mode_Work_Logic_state() {
        return TriggerLevel_Mode_Work_Logic_state;
    }

    //高低电平时的状态，高还是低
    private boolean TriggerLevel_Mode_Work_HighLow_State;

    /**
     * @return true高电平
     */
    public boolean isTriggerLevel_Mode_Work_HighLow_State() {
        return TriggerLevel_Mode_Work_HighLow_State;
    }

    /**
     * @return 1:高电平;2:低电平
     */
    public int isTriggerLevel_Mode_Work_HighLow_Index() {
        return TriggerLevel_Mode_Work_HighLow_State ? 1 : 2;
    }

    /***
     * 设置高低电平时的选择
     */
    public void setTriggerLevel_Mode_Work_HighLow_State(boolean triggerLevel_Mode_Work_HighLow_State) {
        TriggerLevel_Mode_Work_HighLow_State = triggerLevel_Mode_Work_HighLow_State;
        invalidate();
    }

    public int getCurrCh() {
        return CurrCh;
    }

    public void setCurrCh(int currCh) {
        if (currCh % (channelCount + 2) == 0) currCh = channelCount;
        CurrCh = (currCh % (channelCount + 2));
        invalidate();
    }

    private int CurrCh = TChan.Ch1;
    private int buttonChColor = TChan.Ch1;

    public void setButtonChColor(int buttonChColor) {
        this.buttonChColor = buttonChColor;
        invalidate();
    }

    @TriggerLevel_Mode_Show
    public int getTriggerLevel_Mode_Show() {
        return TriggerLevel_Mode_Show;
    }

    public void setTriggerLevel_Mode_Show(@TriggerLevel_Mode_Show int triggerLevel_Mode_Show) {
        TriggerLevel_Mode_Show = triggerLevel_Mode_Show;
        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            //this.layout(0,228,this.getWidth(),228+BUTTONHEIGHT);

            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = BUTTONHEIGHT;
            this.setLayoutParams(lp);
            setY(CenterY);
        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
            //this.layout(0,0,this.getWidth(),DRAGHEIGHT);
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = DRAGHEIGHT;
            this.setLayoutParams(lp);
            setY(40);

        }
        invalidate();
    }

    //显示模式
    @TriggerLevel_Mode_Show
    private int TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Button;

    public void setTriggerLevel_Mode_Work(@TriggerLevel_Mode_Work int triggerLevel_Mode_Work) {
        TriggerLevel_Mode_Work = triggerLevel_Mode_Work;
        invalidate();
    }

    //返回当前工作模式
    public int getTriggerLevel_Mode_Work() {
        return TriggerLevel_Mode_Work;
    }

    //工作模式
    @TriggerLevel_Mode_Work
    private int TriggerLevel_Mode_Work = TriggerLevel_Mode_Work_Normal;


    private View.OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnMouseMoveListener(OnMouseMoveListener onMouseMoveListener) {
        this.onMouseMoveListener = onMouseMoveListener;
    }

    //拖动通道的事件
    private OnMouseMoveListener onMouseMoveListener = null;

    //动画效果的处理      先动画，最后变为实际效果
    public void setAnimation_ViewHeight(int animation_ViewHeight) {
        Animation_ViewHeight = animation_ViewHeight;

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) this.getLayoutParams();
        lp.height = Animation_ViewHeight;
        this.setLayoutParams(lp);
        post(() -> {
            CenterY = (DRAGHEIGHT - this.getHeight()) / 2.0f;
            setY(CenterY);
        });
        invalidate();
    }

    private int Animation_ViewHeight;
    //endregion

    //region 私有变量
    private Bitmap[][] bmp = new Bitmap[TChan.MaxLogicChan + 2][2];
    private Bitmap ch1_down, ch1_up, ch2_down, ch2_up, ch3_down, ch3_up, ch4_down, ch4_up,
            ch5_down, ch5_up, ch6_down, ch6_up, ch7_down, ch7_up, ch8_down, ch8_up, out_down, out_up, levelSlider;
    private Bitmap triglevel_ch1, triglevel_ch2, triglevel_ch3, triglevel_ch4;
    private Rect rectTrigLevel;
    //按钮模式的高度
    private int buttonHeight;
    private Paint mPaint;
    private int lastX, lastY, oldX, oldY;
    //按下的状态
    private boolean downState = false;


    //endregion

    public MTriggerLevel(Context context) {
        this(context, null);
    }

    public MTriggerLevel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTriggerLevel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;

        ch1_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch1), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch1_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch1), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch2_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch2), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch2_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch2), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch3_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch3), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch3_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch3), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch4_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch4), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch4_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch4), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        ch5_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch5), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch5_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch5), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch6_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch6), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch6_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch6), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch7_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch7), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch7_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch7), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch8_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch8), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch8_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch8), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        out_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.NULL), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        out_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.NULL), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        Bitmap temp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.trigger_level_slider);
        levelSlider = BitmapUtil.scaleBitmap(temp, temp.getWidth(), DRAGHEIGHT);

//        triglevel_ch1 = BitmapFactory.decodeResource(getResources(), R.drawable.triglevel_ch1_1);
//        triglevel_ch2 = BitmapFactory.decodeResource(getResources(), R.drawable.triglevel_ch2_1);
//        triglevel_ch3 = BitmapFactory.decodeResource(getResources(), R.drawable.triglevel_ch3_1);
//        triglevel_ch4 = BitmapFactory.decodeResource(getResources(), R.drawable.triglevel_ch4_1);
        triglevel_ch1 = readSvgBmp(context,R.drawable.svg_main_trigger_level);// BitmapFactory.decodeResource(getResources(), R.drawable.svg_main_trigger_level);
        triglevel_ch2 = readSvgBmp(context,R.drawable.svg_main_trigger_level);// BitmapFactory.decodeResource(getResources(), R.drawable.svg_main_trigger_level);
        triglevel_ch3 = readSvgBmp(context,R.drawable.svg_main_trigger_level);// BitmapFactory.decodeResource(getResources(), R.drawable.svg_main_trigger_level);
        triglevel_ch4 = readSvgBmp(context,R.drawable.svg_main_trigger_level);// BitmapFactory.decodeResource(getResources(), R.drawable.svg_main_trigger_level);

        rectTrigLevel = new Rect(0, 0, this.triglevel_ch1.getWidth(), triglevel_ch1.getHeight());

        bmp[1][0] = ch1_down;
        bmp[2][0] = ch2_down;
        bmp[3][0] = ch3_down;
        bmp[4][0] = ch4_down;
        bmp[5][0] = ch5_down;
        bmp[6][0] = ch6_down;
        bmp[7][0] = ch7_down;
        bmp[8][0] = ch8_down;
        bmp[9][0] = out_down;

        bmp[1][1] = ch1_up;
        bmp[2][1] = ch2_up;
        bmp[3][1] = ch3_up;
        bmp[4][1] = ch4_up;
        bmp[5][1] = ch5_up;
        bmp[6][1] = ch6_up;
        bmp[7][1] = ch7_up;
        bmp[8][1] = ch8_up;
        bmp[9][1] = out_up;


        buttonHeight = BUTTONHEIGHT;
        mPaint = new Paint();
//        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setAntiAlias(true);
        upRect = new Rect();
        downRect = new Rect();

    }


    public static Bitmap readSvgBmp(Context context, int drawableId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

    }

    public static Rect getTextRect(String text, Paint paint) {
        Rect rectText = new Rect();
        paint.getTextBounds(text, 0, text.length(), rectText);
        return rectText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            if (this.getHeight() <= this.buttonHeight) {
                mPaint.setColor(Color.rgb(0xAD,0xBD,0xCC));
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextSize(24);
                Rect rect = getTextRect("Level", mPaint);
                switch (buttonChColor) {
                    case TChan.Ch1:
                        canvas.drawBitmap(triglevel_ch1, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch2:
                        canvas.drawBitmap(triglevel_ch2, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch3:
                        canvas.drawBitmap(triglevel_ch3, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch4:
                    case TChan.Ch5:
                    case TChan.Ch6:
                    case TChan.Ch7:
                    case TChan.Ch8:
                    case TChan.Ch8 + 1:
                        canvas.drawBitmap(triglevel_ch4, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;

                }
            } else { //正在变化时，该显示的状态 从button变为drag模式
                //画背景
                switchDrawModeShowDrag(canvas);
            }

        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
            autoDragToButton();
            //Logger.i();("height:"+getHeight()+"parentHeight:"+((View)this.getParent()).getHeight());

            if (this.getHeight() >= ((View) this.getParent()).getHeight()) {
                //画背景
                switchDrawModeShowDrag(canvas);
            } else {//变化的时，显示的状态 Drag变为Button
                switchDrawModeShowDrag(canvas);
            }
        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Changing) {
            switchDrawModeShowDrag(canvas);
        }

    }

    private int getPreCh(int currCh) {
        int i = currCh;
        do {
            i--;
            if (i == 0) i = TriggerLevel_Mode_Work_Logic_state.length - 1;
            if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None)
                return i;
        } while (i != currCh);
        return currCh;
    }

    private int getNextCh(int currCh) {
        int i = currCh;
        do {
            i++;
            if (i == TriggerLevel_Mode_Work_Logic_state.length) i = 1;
            if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None)
                return i;
        } while (i != currCh);
        return currCh;
    }

    private void switchDrawModeShowDrag(Canvas canvas) {
        int paddingEnd = getPaddingEnd();
//        mPaint.setColor(getResources().getColor(R.color.color_Backcolor_MainMenu3));
//        mPaint.setStyle(Paint.Style.FILL);
//        canvas.drawRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), 0, 0, mPaint);
//
//        mPaint.setColor(getResources().getColor(R.color.color_Backcolor_MainMenu2));
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setStrokeWidth(1);
//        canvas.drawRoundRect(new RectF(1, 1, this.getWidth() - 1 - paddingEnd, this.getHeight() - 1), 0, 0, mPaint);
//
//        mPaint.setColor(getResources().getColor(R.color.color_divider_mainwave));
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(1);
//        canvas.drawRoundRect(new RectF(1, 1, this.getWidth() - 1 - paddingEnd, this.getHeight() - 1), 0, 0, mPaint);
        Rect src = new Rect(0, 0, levelSlider.getWidth(), levelSlider.getHeight());
        Rect dst = new Rect(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(levelSlider, src, dst, mPaint);

        if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal || TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            switch (CurrCh) {
                case TChan.Ch1:
                    if (getCurrentTriggerIndex() == 1) {
                        if (channelCount == 2) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        } else if (channelCount == 4) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        } else if (channelCount == 8) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        }
                    } else {
                        if (channelCount == 2) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch2_up, ch2_down);
                        } else if (channelCount == 4) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch4_up, ch2_down);
                        } else if (channelCount == 8) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch8_up, ch2_down);
                        }
                    }
                    break;
                case TChan.Ch2:
                    if (channelCount == 2) {
                        drawModeShowDrag(canvas, ch2_up, ch2_down, ch1_up, ch1_down);
                    } else{
                        drawModeShowDrag(canvas, ch2_up, ch2_down, ch1_up, ch3_down);
                    }
                    break;
                case TChan.Ch3:
                    drawModeShowDrag(canvas, ch3_up, ch3_down, ch2_up, ch4_down);
                    break;
                case TChan.Ch4:
                    if (channelCount==4) {
                        drawModeShowDrag(canvas, ch4_up, ch4_down, ch3_up, ch1_down);
                    }else {
                        drawModeShowDrag(canvas, ch4_up, ch4_down, ch3_up, ch5_down);
                    }
                    break;
                case TChan.Ch5:
                    drawModeShowDrag(canvas, ch5_up, ch5_down, ch4_up, ch6_down);
                    break;
                case TChan.Ch6:
                    drawModeShowDrag(canvas, ch6_up, ch6_down, ch5_up, ch7_down);
                    break;
                case TChan.Ch7:
                    drawModeShowDrag(canvas, ch7_up, ch7_down, ch6_up, ch8_down);
                    break;
                case TChan.Ch8:
                    if(getCurrentTriggerIndex() == 1) {
                        drawModeShowDrag(canvas, ch8_up, ch8_down, ch7_up, out_down);
                    } else {
                        drawModeShowDrag(canvas, ch8_up, ch8_down, ch7_up, ch1_down);
                    }
                    break;
                case TChan.Ch8 + 1:
                    if (getCurrentTriggerIndex() == 1) {
                        drawModeShowDrag(canvas, out_up, out_down, ch8_up, ch1_down);
                    }
                    break;
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            switch (CurrCh) {
                case TChan.Ch1:
                    drawModeShowDrag(canvas, ch1_up, ch1_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch2:
                    drawModeShowDrag(canvas, ch2_up, ch2_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch3:
                    drawModeShowDrag(canvas, ch3_up, ch3_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch4:
                    drawModeShowDrag(canvas, ch4_up, ch4_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch5:
                    drawModeShowDrag(canvas, ch5_up, ch5_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch6:
                    drawModeShowDrag(canvas, ch6_up, ch6_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch7:
                    drawModeShowDrag(canvas, ch7_up, ch7_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch8:
                    drawModeShowDrag(canvas, ch8_up, ch8_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
            }
        }
    }

    private void drawModeShowDrag(Canvas canvas, Bitmap curr_up, Bitmap curr_down, Bitmap up, Bitmap down) {
        int paddingEnd = getPaddingEnd();
        Rect src = new Rect(0, 0, curr_up.getWidth(), curr_up.getHeight());
        Rect des = new Rect(0, this.getHeight() - curr_up.getHeight(), curr_up.getWidth(), this.getHeight());
        int leftBmp = (this.getWidth() - ch1_down.getWidth() - getPaddingEnd()) / 2;
        int leftTxt = (this.getWidth() - 20 - paddingEnd) / 2;
        if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            canvas.drawBitmap(up, leftBmp, leftBmp, null);
            canvas.drawBitmap(down, leftBmp, des.top - leftBmp, null);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            //切换高或低
            mPaint.setStyle(Paint.Style.FILL);
            if (TriggerLevel_Mode_Work_HighLow_State) {
                canvas.drawBitmap(curr_up, leftBmp, leftBmp, null);
                mPaint.setColor(TChan.getChannelColor(context, CurrCh));
                canvas.drawText("L", leftTxt, leftBmp + 60, mPaint);
                canvas.drawBitmap(curr_down, leftBmp, des.top - leftBmp, null);
                canvas.drawText("L", leftTxt, this.getHeight() - 40 - leftBmp, mPaint);
            } else {
                canvas.drawBitmap(curr_up, leftBmp, leftBmp, null);
                mPaint.setColor(TChan.getChannelColor(context, CurrCh));
                canvas.drawText("H", leftTxt, leftBmp + 60, mPaint);
                canvas.drawBitmap(curr_down, leftBmp, des.top - leftBmp, null);
                canvas.drawText("H", leftTxt, this.getHeight() - 40 - leftBmp, mPaint);
            }

        } else {  //logic模式
            canvas.drawBitmap(up, leftBmp, leftBmp, null);
            canvas.drawBitmap(down, leftBmp, des.top - leftBmp, null);
        }
    }

    private boolean moveLeft = false;
    private boolean moveUpDown = false;
    private MoveLevelMenuListener onMoveLevelMenuListener;

    public void setOnMoveLevelMenuListener(MoveLevelMenuListener onMoveLevelMenuListener) {
        this.onMoveLevelMenuListener = onMoveLevelMenuListener;
    }

    public interface MoveLevelMenuListener {
        void onMoving(MTriggerLevel triggerLevel, int moveX);

        void onMoveEnd(MTriggerLevel triggerLevel);

        void onLevelVisible(MTriggerLevel triggerLevel, boolean visible);
    }

    //滑动方向
    public static final int SliderDir_None = 0x00;
    public static final int SliderDir_LeftToRight = 0x01;
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight;
    public static final int SliderDir_TopToBottom = 0x02;
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom;
    private int slipDir = SliderDir_None;

    public boolean dealTouchEvent(MotionEvent event) {
        int[] location = new int[2];
        getLocationOnScreen(location);
        autoDragToButton();
        upRect.set(location[0], location[1], location[0] + BUTTONHEIGHT, location[1] + BUTTONHEIGHT);
        downRect.set(location[0], location[1] + this.getHeight() - BUTTONHEIGHT, location[0] + BUTTONHEIGHT, location[0] + this.getHeight());

//        Logger.i(TAG,"control left:"+location[0] +" control top:"+location[1]);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                downState = true;
                oldX = lastX = (int) event.getRawX();
                oldY = lastY = (int) event.getRawY();
                moveLeft = false;
                moveUpDown = false;
                slipDir = SliderDir_None;
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY())
                        && TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button
                ) {
                    //setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Drag);
                    Animation_ButtonToDrag(OPENTYPE_TRIGGER);
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                    }
                    break;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (slipDir == SliderDir_None) {
                    if (!moveUpDown && Math.abs(oldY - event.getRawY()) >= 20) {
                        moveUpDown = true;
                        slipDir = SliderDir_TopToBottom;
                    } else if (!moveLeft && !moveUpDown && oldX - event.getRawX() >= 20) {
                        moveLeft = true;
                        slipDir = SliderDir_RightToLeft;
                    }
                }
                if (slipDir == SliderDir_RightToLeft && openType == OPENTYPE_TRIGGER) {
                    if (onMoveLevelMenuListener != null) {
                        onMoveLevelMenuListener.onMoving(MTriggerLevel.this, (int) (oldX - event.getRawX()));
                    }
                } else if (slipDir == SliderDir_TopToBottom) {
                    if (this.TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
                        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && upRect.contains(oldX, oldY) &&
                                upRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            break;
                        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && downRect.contains(oldX, oldY) &&
                                downRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            break;
                        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) { //不在Drag区域不能引起位置移动
                            Rect rect = getViewRect(this);
                            if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                break;
                            }
                        }
                        //这里滑动的值，要带出去
                        int tempY = lastY - (int) event.getRawY();
                        setChannelMoveChannel(TBookUtil.isFine() ? (tempY / TBookUtil.getNumFine()) : tempY);
                        if (TBookUtil.isFine()) {
                            if (Math.abs(tempY) > TBookUtil.getNumFine()) {
                                lastY = (int) event.getRawY();
                            }
                        } else {
                            lastY = (int) event.getRawY();
                        }
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                downState = false;
                if (moveLeft && openType == OPENTYPE_TRIGGER) {
                    if (onMoveLevelMenuListener != null) {
                        onMoveLevelMenuListener.onMoveEnd(MTriggerLevel.this);
                    }
                }
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY()) &&
                        TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
                    break;
                }
                if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && upRect.contains(oldX, oldY) &&
                        upRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    setChangeUpChannel();
                    break;
                } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && downRect.contains(oldX, oldY) &&
                        downRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    setChangeDownChannel();
                    break;
                } else {
                    //
                    if (onMouseMoveListener != null)
                        onMouseMoveListener.onMouseMoveComplete(this, openType);
                }
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                lastX = oldX = (int) event.getRawX();
                lastY = oldY = (int) event.getRawY();
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                int id = event.getActionIndex();
                if (id == 0) {
                    int left = (int) (event.getRawX() - event.getX(0));
                    int top = (int) (event.getRawY() - event.getY(0));
                    lastX = oldX = (int) event.getX(1) + left;
                    lastY = oldY = (int) event.getY(1) + top;
                } else {
                    lastX = oldX = (int) event.getRawX();
                    lastY = oldY = (int) event.getRawY();
                }
            }
            break;
        }

        return true;
    }

    /**
     * @param deltaY 向上滑是正数
     */
    public void setChannelMoveChannel(double deltaY) {
        if (onMouseMoveListener != null) {
            onMouseMoveListener.onMouseMove(this, deltaY, CurrCh, openType, true);
        }
    }

    public boolean isFirstChannel() {
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            return CurrCh == TChan.Ch1;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {

            return TriggerLevel_Mode_Work_HighLow_State;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {

            boolean bFirst = true;
            for (int i = CurrCh - 1; i >= 0; i--) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    bFirst = false;
                    break;
                }
            }
            return bFirst;
        }
        return false;
    }

    public boolean isLastChannel(int chnums) {

        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if (chnums == 2) {
                return CurrCh == TChan.Ch2;
            } else if (chnums == 4) {
                return CurrCh == TChan.Ch4;
            } else {
                return CurrCh == TChan.Ch8;
            }

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {

            return !TriggerLevel_Mode_Work_HighLow_State;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {

            boolean bLast = true;
            for (int i = CurrCh + 1; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    bLast = false;
                    break;
                }
            }
            return bLast;
        }
        return false;
    }

    public int getChangeChannelCount() {
        int nums = 4;
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            nums = 4;
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            nums = 2;
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            nums = 0;
            for (int i = 1; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    nums++;
                }
            }
        }
        return nums;
    }

    public void setChangeDownChannel() {

        Logger.i("down Click");
        int finalCount = channelCount + 1;
        if(getCurrentTriggerIndex() == 1) {
            finalCount = channelCount + 2;
        }
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if ((getCurrCh() + 1) % (finalCount) == 0) {
                setCurrCh(TChan.Ch1);
            } else {
                setCurrCh(Math.abs(getCurrCh() + 1));
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            setTriggerLevel_Mode_Work_HighLow_State(!TriggerLevel_Mode_Work_HighLow_State);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            setCurrCh(getNextCh(CurrCh));
        }

        if (onMouseMoveListener != null) {
            onMouseMoveListener.onDownClick(this, CurrCh, openType);
        }
    }

    public void setChangeUpChannel() {

        Logger.i("up Click");
        int finalCount = channelCount + 1;
        if(getCurrentTriggerIndex() == 1) {
            finalCount = channelCount + 2;
        }
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if ((getCurrCh() - 1) % (finalCount) == 0) {
                setCurrCh(finalCount - 1);//四通道机器为IWave.Ch4，双通道机器为IWave.Ch2
            } else {
                setCurrCh(Math.abs(getCurrCh() - 1));
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            setTriggerLevel_Mode_Work_HighLow_State(!TriggerLevel_Mode_Work_HighLow_State);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            setCurrCh(getPreCh(CurrCh));
        }

        if (onMouseMoveListener != null) {
            onMouseMoveListener.onUpClick(this, CurrCh, openType);
        }
    }

    private int[] location = new int[2];
    private Rect controlRect = new Rect();

    private Rect getViewInScreen() {
        this.getLocationOnScreen(location);
        controlRect.set(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]);
        return controlRect;
        //return new Rect(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]);
    }

    private static final int MSG_AUTO_DRAGTOBTN = 0x666;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTO_DRAGTOBTN:
                    if (getTriggerLevel_Mode_Show() == TriggerLevel_Mode_Show_Drag) {
                        Animation_DragToButton();
                    }
                    break;
            }
        }
    };

    public void autoDragToButton() {
        if (handler.hasMessages(MSG_AUTO_DRAGTOBTN)) {
            handler.removeMessages(MSG_AUTO_DRAGTOBTN);
        }
        handler.sendEmptyMessageDelayed(MSG_AUTO_DRAGTOBTN, 5000);
    }

    private ObjectAnimator oaButtonToDrag;
    public void Animation_ButtonToDrag(@OpenType final int type) {
        openType = type;
//        if (TriggerLevel_Mode_Show != TriggerLevel_Mode_Show_Button) {
//            //这里不走动画，只更新效果
//            TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Changing;
//            setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Drag);
//            if (onOpenCloseListener != null) {
//                onOpenCloseListener.onOpen(type);
//            }
//            return;
//        }
        TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Changing;
        oaButtonToDrag = ObjectAnimator.ofInt(this, "Animation_ViewHeight", BUTTONHEIGHT, DRAGHEIGHT);
        oaButtonToDrag.setDuration(150);
        oaButtonToDrag.start();
        oaButtonToDrag.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int cVal = (int) animation.getAnimatedValue();
                if (cVal >= DRAGHEIGHT) {
                    setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Drag);
                    if (onOpenCloseListener != null) {
                        onOpenCloseListener.onOpen(type);
                    }
                    return;
                }
                setAnimation_ViewHeight(cVal);
                autoDragToButton();
            }
        });
    }

    private ObjectAnimator oaDragToButton;
    public void Animation_DragToButton() {
        if (TriggerLevel_Mode_Show != TriggerLevel_Mode_Show_Drag) return;
        TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Changing;
        if (handler.hasMessages(MSG_AUTO_DRAGTOBTN)) {
            handler.removeMessages(MSG_AUTO_DRAGTOBTN);
        }
        oaDragToButton = ObjectAnimator.ofInt(this, "Animation_ViewHeight", DRAGHEIGHT, BUTTONHEIGHT);
        oaDragToButton.setDuration(150);
        oaDragToButton.start();
        oaDragToButton.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int cVal = (int) animation.getAnimatedValue();
                if (cVal <= BUTTONHEIGHT) {
                    setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Button);
                    if (onOpenCloseListener != null) {
                        onOpenCloseListener.onClose(openType);
                    }
                    return;
                }
                setAnimation_ViewHeight(cVal);
            }
        });
        if (onMoveLevelMenuListener != null) {
            onMoveLevelMenuListener.onLevelVisible(MTriggerLevel.this, false);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (onMoveLevelMenuListener != null) {
            onMoveLevelMenuListener.onLevelVisible(MTriggerLevel.this, visibility == VISIBLE);
        }
    }

    public boolean hasPoint(int x, int y) {
        return getVisibility() == VISIBLE && getViewInScreen().contains(x, y);
    }

    public static boolean isSerialType(int type) {
        return (type >= OPENTYPE_SERIALS1) && (type <= OPENTYPE_SERIALS4);
    }

    public static @OpenType
    int getOpenType(int sNo) {
        return sNo;
    }

    private Rect getViewRect(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        Rect rect = new Rect(x, y, x + v.getWidth(), y + v.getHeight());
//        Log.d("Debug", String.format("getViewRect: %s", rect.toString() ));
        return rect;
    }

    public int getCurrentTriggerIndex() {
        return currentTriggerIndex;
    }

    public void setCurrentTriggerIndex(int currentTriggerIndex) {
        this.currentTriggerIndex = currentTriggerIndex;
    }

    public void setCenterY(int height) {
        waveHeight = height;
        CenterY = (height - BUTTONHEIGHT) / 2.0f;
        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            if (oaButtonToDrag != null) {
                oaButtonToDrag.setIntValues(BUTTONHEIGHT, DRAGHEIGHT);
            }
            if (oaDragToButton != null) {
                oaDragToButton.setIntValues(BUTTONHEIGHT, DRAGHEIGHT);
            }
            Bitmap temp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.trigger_level_slider);
            levelSlider = Bitmap.createScaledBitmap(levelSlider, levelSlider.getWidth(), DRAGHEIGHT, true);
            setY(CenterY);
            invalidate();
        } else {
            autoDragToButton();
        }
    }

}


