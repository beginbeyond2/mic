package com.micsig.tbook.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.micsig.tbook.ui.main.MainBeanTopRight;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

/**
 * Created by liwb on 2018/2/1.
 */

public class MTriggerStateBar extends GLTextureView {
    public static final String TAG = MTriggerStateBar.class.getSimpleName();
    private Context context;
    private int width, height;
    private int margin;
    private ArrayList<MainBeanTopRight> showList = new ArrayList<>();
    private Bitmap bmp;
    private Bitmap rectangleBmp;
    private Canvas canvas;
    private Paint paint;
    private TextPaint tPaint;
    private RectF clipRect = new RectF();
    private int backColor = Color.RED;
    private OnClickListener onClickListener;
    private boolean isChanageBitmap = false;
    private final Object lock = new Object();
    private ObjectAnimator animator;
    private ObjectAnimator resetAnimator;

    public interface OnClickListener {
        void onClick(MTriggerStateBar view, MainBeanTopRight item);
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public MTriggerStateBar(Context context) {
        this(context, null);
    }

    public MTriggerStateBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        margin = 3;
//        width = 450;
        width = 88;
        height = 68;
        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setTextSize(18);
        paint.setAntiAlias(false);
        paint.setColor(getResources().getColor(R.color.color_divider_mainwave));

        tPaint = new TextPaint();
        tPaint.setTextSize(18);
        tPaint.setAntiAlias(true);
        tPaint.setColor(paint.getColor());
        tPaint.setTextAlign(Paint.Align.LEFT);


//        bmp = Bitmap.createBitmap(440, 60, Bitmap.Config.ARGB_8888);
        bmp = Bitmap.createBitmap(223, 100, Bitmap.Config.ARGB_8888);
//        rectangleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.right_rectangle);
        rectangleBmp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.ic_rectangle_bg_trigger_state_bar);
        rectangleBmp = BitmapUtil.scaleBitmap(rectangleBmp, bmp.getWidth(), bmp.getHeight());
        canvas = new Canvas(bmp);
        //clipRect.set(2, 3, bmp.getWidth() - 1, bmp.getHeight() - 1);
        clipRect.set(0, 0, bmp.getWidth(), bmp.getHeight());
        paint.setStyle(Paint.Style.FILL);
        backColor = context.getResources().getColor(R.color.color_Backcolor_MainMenu2);

        animator = ObjectAnimator.ofFloat(this, "translationY", 0f, -32f);
        animator.setDuration(6000);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        resetAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f);
    }

    public void setData(ArrayList<MainBeanTopRight> list) {
        synchronized (lock) {
            showList = list;
            draw();
            requestRender();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
//        int result = 450;
        int result = 220;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
//        int result = 60;
        int result = 92;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        return result;
    }


    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    private Rect rect = new Rect();

    private int getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }


    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        if (showList.size() == 0) return;
        synchronized (lock) {
            drawBackColor(canvas);

            if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null);
            canvas.drawBitmap(bmp, 4, 3);
            isChanageBitmap = false;

        }
    }

    private void drawBackColor(ICanvasGL canvas) {
        canvas.clearBuffer(backColor);
    }

    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    private Tuple<Integer, Boolean> GetShowListCount() {
        int count = 0;
        boolean isNumber = false;
        for (int i = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;
            if (showList.get(i).isShowNumber()) {
                isNumber = true;
            }
            count++;
        }
        Tuple<Integer, Boolean> tuple = new Tuple<>(count, isNumber);
        return tuple;
    }

    private void draw() {
        Tuple<Integer, Boolean> tuple = GetShowListCount();
        this.paint.setColor(backColor);
        this.canvas.drawRect(clipRect, paint);
        if (tuple.getSecond()) {
            //是数字
            if (tuple.getFirst() > 4) {
                int showNum = drawMore3Num(tuple.getFirst());
                if (showNum > 4) {
                    startAnimator();
                } else {
                    stopAnimator();
                }
            } else {
                drawLess3Num(tuple.getFirst());
                stopAnimator();
            }
//            int showNum = drawMore3Num(tuple.getFirst());
//            if (showNum > 4) {
//                startAnimator();
//            } else {
//                stopAnimator();
//            }
        } else {
            //是普通字符，即串型解码指符
            drawString();
            stopAnimator();
        }
        isChanageBitmap = true;
    }


    private void drawLess3Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textHeight = 0;
        int textX = 0;
        int textY = 2;
        int lineBottomY = 0;
        int lineTopY = -14;

        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;
            String number = "";
            j++;
            int startHeight = textHeight;
            int listCount = listSize + 1;
            if (listCount >= 4) listCount = 4;  //逻辑触发中，只显示3个

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
                case TChan.Ch8 + 1: number = " ●";break;
            }
            startHeight = (int) (height / listCount * (j - 0.5));
//          为了省空间，不是显示number 进行顶格显示，下同
            if (!number.equalsIgnoreCase("")) {
                startHeight += textY + j * 4;
                numWidth = getTextWidth(number);
            } else {
                startHeight += textY + 10;
            }
            drawText(number, textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(), textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(textWidth, startHeight + lineTopY, w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(textWidth, lineBottomY + startHeight+3, w + textWidth, lineBottomY + startHeight+3, paint);
            }
            textHeight = getTextHeight(item.getText()) + margin + startHeight;
        }
    }

    private void draw4Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textHeight = 0;
        int textX = 0;
        int textY = -2;
        int lineBottomY = 0;
        int lineTopY = -14;

        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;
            String number = "";
            j++;
            int listCount = 3;

            int startHeight = textHeight;
            int startWidth = 0;

            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));
            if (j>2){
                startWidth=105;
            }
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
            }
            startHeight = height / listCount * (j>2?j-2:j);
            //为了省空间，不是显示number 进行顶格显示，下同
            if (!number.equalsIgnoreCase("")) {
                startHeight += textY + (j>2?j-2:j) * 4;
                numWidth = getTextWidth(number);
            } else {
                startHeight += textY + 10;
            }
            drawText(number,startWidth+ textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(),startWidth+ textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(startWidth+textWidth, startHeight + lineTopY, startWidth+w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(startWidth+textWidth, lineBottomY + startHeight+3, startWidth+w + textWidth, lineBottomY + startHeight+3, paint);
            }
            textHeight = getTextHeight(item.getText()) + margin + startHeight;

        }

    }

    private int drawMore3Num(int listSize) {
        int numWidth = 0;
        int textWidth = 0;
        int textX = 0;
        int lineBottomY = 0;
        int lineTopY = -14;
        int showNum = 0;
        for (int i = 0, j = 0; i < showList.size(); i++) {
            if (!showList.get(i).isVisible()) continue;
            String number = "";
            j++;
            int startHeight;
            int startWidth;
            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));
            switch (item.getChannel()) {
                case TChan.Ch1: number = " ①";break;
                case TChan.Ch2: number = " ②";break;
                case TChan.Ch3: number = " ③";break;
                case TChan.Ch4: number = " ④";break;
                case TChan.Ch5: number = " ⑤";break;
                case TChan.Ch6: number = " ⑥";break;
                case TChan.Ch7: number = " ⑦";break;
                case TChan.Ch8: number = " ⑧";break;
                case TChan.Ch8 + 1: number = " ●";break;
            }
            if ((j % 2) == 1) {
                startWidth = 0;
            } else {
                startWidth = 105;
            }
            startHeight = 23 + 23 * ((j - 1) / 2);
//            startWidth = ((j - 1) % 4) * 110;
//            startHeight = 23 + 31 * ((j - 1) / 4);

            //为了省空间，不是显示number 进行顶格显示，下同
            if (!number.equalsIgnoreCase("")) {
                numWidth = getTextWidth(number);
            }
            drawText(number, startWidth + textX, startHeight, paint);
            if (!number.equalsIgnoreCase("")) {
                textWidth = numWidth + 10;
            }
            drawText(item.getText(), startWidth + textWidth, startHeight, paint);

            int w = getTextWidth(item.getText());

            if (item.getLine() == MainBeanTopRight.LINE_TOP) {
                this.canvas.drawLine(startWidth + textWidth, startHeight + lineTopY, startWidth + w + textWidth, startHeight + lineTopY, paint);
            } else if (item.getLine() == MainBeanTopRight.LINE_BOTTOM) {
                this.canvas.drawLine(startWidth + textWidth, lineBottomY + startHeight + 3, startWidth + w + textWidth, lineBottomY + startHeight + 3, paint);
            }
//            textHeight = getTextHeight(item.getText()) + margin + startHeight;
            showNum = j;

        }
        return showNum;
    }

    private void drawString() {
//        int startHeight = 21;
        int startHeight = 18;
        for (int i = 0; i < showList.size(); i++){
            if (!showList.get(i).isVisible()) continue;
            MainBeanTopRight item = showList.get(i);
            paint.setColor(getResources().getColor(item.getColorResId()));
            drawText(item.getText(),0,startHeight,paint);
        }
    }

    private void drawText(String text, int x, int y, Paint p) {
        tPaint.setColor(p.getColor());
        text = text.replaceAll("\n", "  ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder b = StaticLayout.Builder.obtain(text, 0, text.length(), tPaint, bmp.getWidth()-2);
            b.setAlignment(Layout.Alignment.ALIGN_NORMAL);
            StaticLayout sl = b.build();
            canvas.save();
            canvas.translate(x, y - 18);
            sl.draw(canvas);
            canvas.restore();
        } else {
            this.canvas.drawText(text, x, y, p);
        }

    }

    private void startAnimator() {
        animator.start();
    }

    private void stopAnimator() {
        if (animator.isRunning()) {
            animator.cancel();
        }
        resetAnimator.start();
    }


    @Override
    protected int getRenderMode() {
        return GLThread.RENDERMODE_WHEN_DIRTY;
    }
}
