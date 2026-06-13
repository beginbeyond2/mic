package com.micsig.tbook.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

;

/**
 * @auother Liwb
 * @description:
 * @data:2020/5/27 9:21
 */
public class MainViewSegmentedSingleSmall extends GLTextureView {
    private static final String TAG = MainViewSegmentedSingleSmall.class.getSimpleName();

    private static final int SHOWCOUNT = 9;
    private static final int SHOWCOUNTHALF = (SHOWCOUNT - 1) / 2;

    private static final int BaseSpeed = 2;
    public static final int PLAYSPEEP_1X = (int) (1 * BaseSpeed);
    public static final int PLAYSPEEP_2X = (int) (2 * BaseSpeed);
    public static final int PLAYSPEEP_4X = (int) (4 * BaseSpeed);
    public static final int PLAYSPEEP_8X = (int) (8 * BaseSpeed);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAYSPEEP_1X, PLAYSPEEP_2X, PLAYSPEEP_4X, PLAYSPEEP_8X})
    @interface PLAYSPEED {
    }

    private boolean isPlay = false;
    private boolean isPlayOption = false;
    private boolean isPlayOrder = false;
    private int isVisible = View.GONE;
    private int curPlaySpeed = PLAYSPEEP_1X;
    private Paint paint;
    private int width, height;
    private int padding;
    private int curFrame;
    private int offsetX;
    private int textColor;
    private int kuangColor,noSelectKuangColor;
    private Bitmap bitmap;
    private Canvas canvas;
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private boolean isRedraw = false;

    public void onRefresh(){
        requestRender();
    }
    private OnEvents onEvents;
    private List<SegmentedSingleBean> list = new ArrayList<>();


    public OnEvents getOnEvents() {
        return onEvents;
    }

    public void setOnEvents(OnEvents onEvents) {
        this.onEvents = onEvents;
    }

    public interface OnEvents {
        void onClick(int curFrame);

        void onCurrFrameChange(int currFrame);

        void onOrderChange(boolean order);

        void onVisibleChange(int visibility);
    }

    public MainViewSegmentedSingleSmall(Context context) {
        this(context, null);
    }

    public MainViewSegmentedSingleSmall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainViewSegmentedSingleSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        width = 160;
        height = 60;
        padding = 5;
        curFrame = 125678;
        textColor = getResources().getColor(R.color.textColorNewTopViewEnable);
        kuangColor = getResources().getColor(R.color.textColorCenterSegment);
        noSelectKuangColor=getResources().getColor(R.color.frame_color);
        paint = new Paint();
        paint.setAntiAlias(true);
        bitmap = Bitmap.createBitmap(width * SHOWCOUNT, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        isVisible = visibility;
        if (onEvents != null) onEvents.onVisibleChange(visibility);
        onRefresh();
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas) {

        if (isVisible == View.GONE) return;
//        Logger.i(TAG,"OnGlDraw Small!");
        if (isPlay && isPlayOption == false) {
            playDraw(canvas);
        } else {
            stopDraw(canvas);
        }

    }

    private int startX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPlay || isPlayOption) {
            playTouchEvent(event);
        } else {
            stopTouchEvent(event);
        }
        return true;
    }

    private void stopDraw(ICanvasGL canvas) {
        synchronized (bitmap) {
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            canvas.drawBitmap(bitmap, 0, 0);
            isRedraw = false;
        }
    }

    private synchronized void draw(int offsetX) {
        if (list == null || list.size() == 0) return;
        synchronized (bitmap) {
            paint.setXfermode(clearMode);
            this.canvas.drawPaint(paint);
            paint.setXfermode(srcMode);
            for (int i = 0; i < SHOWCOUNT; i++) {
                int offset = (i - SHOWCOUNTHALF) * width + offsetX;
                int idx = curFrame + (i - SHOWCOUNTHALF);
                while (idx < 0 && list.size() != 0) {
                    idx = list.size() + idx;
                }
                while (idx >= list.size() && list.size() != 0) {
                    idx = idx - list.size();
                }
                String curText = String.valueOf(list.get(idx).getFrameId());
                String curTime = list.get(idx).getTimeMs();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(noSelectKuangColor);
                this.canvas.drawRect(offset, 0, width + offset , height, paint);
                paint.setColor(Color.BLACK);
                this.canvas.drawRect(offset+1, 0+1, width + offset-2 , height-2, paint);
                paint.setColor(textColor);
                paint.setTextSize(24);
                paint.setStyle(Paint.Style.FILL);
                this.canvas.drawText(curText, width / 2 - getTextWidth(curText) / 2 + offset, height / 2 - 5, paint);
                paint.setTextSize(20);
                this.canvas.drawText(curTime, width / 2 - getTextWidth(curTime) / 2 + offset, height - padding - 5, paint);
            }
            isRedraw = true;
            onRefresh();
        }
    }

    private void onStopDraw() {
        draw(this.offsetX);
    }

    private void stopTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = (int) (event.getX() - startX);
                onStopDraw();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                offsetX = (int) (event.getX() - startX);
                if (offsetX == 0) {
                    if (onEvents != null) {
                        onEvents.onClick(curFrame);
                    }
                } else {
                    int offsetFrame;
                    if (offsetX > 0) {
                        if (offsetX % width > offsetX / 2) {
                            offsetFrame = offsetX / width + 1;
                        } else {
                            offsetFrame = offsetX / width;
                        }
                    } else {
                        offsetX *= -1;
                        if (offsetX % width > offsetX / 2) {
                            offsetFrame = offsetX / width + 1;
                        } else {
                            offsetFrame = offsetX / width;
                        }
                        offsetX *= -1;
                        offsetFrame *= -1;
                    }
                    offsetX = 0;
                    curFrame -= offsetFrame;
                    curFrame = checkCurFrame(curFrame);
                    if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                    onStopDraw();
                }
                break;
        }
    }

    private int playOffset = 0;

    private long playts = 0;
    private void playDraw(ICanvasGL canvas) {

        int offset = (int) (width * curPlaySpeed / 60);

        long ts = SystemClock.elapsedRealtime();
        if((ts - playts) < 15){
            offset = 0;
        }else {
            playts = ts;
        }

        playOffset += offset;
        if (isPlayOrder) {
            draw(-playOffset);
        } else {
            draw(playOffset);
        }
        synchronized (bitmap) {
            if (isRedraw) canvas.invalidateTextureContent(bitmap,null);
            canvas.drawBitmap(bitmap, 0, 0);
            isRedraw = false;
        }
//        Logger.i(TAG,"playOffset:"+playOffset+"，curFrame:"+curFrame);
        if (playOffset >= width) {
            playOffset -= width;
            if (isPlayOrder) {
                curFrame++;
                curFrame = checkCurFrame(curFrame);
            } else {
                curFrame--;
                curFrame = checkCurFrame(curFrame);
            }
            if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
        }
    }

    private void playTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                startX = (int) event.getX();
                isPlayOption = true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                offsetX = (int) (event.getX() - startX);
                onStopDraw();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                offsetX = (int) (event.getX() - startX);
                setPlayOrder(offsetX > 0 ? false : true);
                if (onEvents != null) onEvents.onOrderChange(offsetX > 0 ? false : true);
                int offsetFrame;
                if (offsetX > 0) {
                    if (offsetX % width > offsetX / 2) {
                        offsetFrame = offsetX / width + 1;
                    } else {
                        offsetFrame = offsetX / width;
                    }
                } else {
                    offsetX *= -1;
                    if (offsetX % width > offsetX / 2) {
                        offsetFrame = offsetX / width + 1;
                    } else {
                        offsetFrame = offsetX / width;
                    }
                    offsetX *= -1;
                    offsetFrame *= -1;
                }
                offsetX = 0;
                curFrame -= offsetFrame;
                curFrame = checkCurFrame(curFrame);
                if (onEvents != null) onEvents.onCurrFrameChange(curFrame);
                onStopDraw();
                isPlayOption = false;


            }
            break;
        }
    }


    private Rect rect = new Rect();

    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    private int checkCurFrame(int curFrame) {
        if (curFrame >= list.size()) {
            curFrame = curFrame % list.size();
        } else if (curFrame < 0) {
            curFrame = list.size() + curFrame;
        }
        return curFrame;
    }

    public int getCurFrame() {
        return curFrame;
    }

    public SegmentedSingleBean getCurBean() {
        if (list != null && curFrame < list.size()) {
            return list.get(curFrame);
        }
        return null;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 10) {
                draw(0);
                return;
            }
            int x = ((int) msg.obj) * msg.what;
//            Logger.i(TAG, "x:" + x);
            draw(x);
            Message msg1 = handler.obtainMessage();
            msg1.what = msg.what + 1;
            msg1.obj = msg.obj;
            handler.sendMessageDelayed(msg1, 20);
        }
    };

    public void setCurFrame(int curFrame) {
        if (curFrame == this.curFrame) {
            return;
        }
        curFrame = checkCurFrame(curFrame);
        int offsetCount = 5 * width;
        if (Math.abs(curFrame - this.curFrame) < 5) {
            offsetCount = Math.abs(this.curFrame - curFrame) * width;
        }
        int step = this.curFrame > curFrame ? offsetCount / 10 : -offsetCount / 10;
        this.curFrame = curFrame;

        Message msg = Message.obtain();
        msg.obj = step;
        msg.what = 1;
        handler.sendMessage(msg);


//        onStopDraw();
    }

    //region 属性
    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        if (isPlay != play) {
            isPlay = play;
            if (!isPlay) {
                draw(0);
//               if  (playOffset>=width/2){
//                   curFrame--;
//               }else {
//                   curFrame++;
//               }
            }

        }
        onRefresh();
    }

    public boolean isPlayOrder() {
        return isPlayOrder;
    }

    public void setPlayOrder(boolean order) {
        isPlayOrder = order;
    }

    /**
     * 设置播放速度
     *
     * @param playSpeed PLAYSPEEP_1X,PLAYSPEED_2X,PLAYSPEED_4X,PLAYSPEED_8X
     */
    public void setPlaySpeed(@PLAYSPEED int playSpeed) {
        curPlaySpeed = playSpeed;
    }

    public synchronized void setList(List<SegmentedSingleBean> list) {
        this.list.clear();
        this.list.addAll(list);
        if (curFrame >= list.size()) curFrame = 0;
        if (list.size() < SHOWCOUNT) {
            List<SegmentedSingleBean> list1 = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                list1.add(list.get(i));
            }
            int t = ((int) Math.ceil(SHOWCOUNT / (float) list.size()));
            for (int i = 0; i < t; i++) {
                this.list.addAll(list1);
            }
        }
        onStopDraw();
    }

    @Override
    public String toString() {
        return "MainViewSegmentedSingleSmall{" +
                "isPlay=" + isPlay +
                ", isPlayOption=" + isPlayOption +
                ", isPlayOrder=" + isPlayOrder +
                ", curPlaySpeed=" + curPlaySpeed +
                ", width=" + width +
                ", height=" + height +
                ", curFrame=" + curFrame +
                '}';
    }

    //endregion
}
