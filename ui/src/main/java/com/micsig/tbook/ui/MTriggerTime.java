package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.GLPaint;
import com.chillingvan.canvasgl.glview.texture.GLTextureView;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.micsig.base.Logger;
import com.micsig.tbook.ui.util.BitmapUtil;

/**
 * Created by liwb on 2018/1/29.
 * 触发时刻控件
 */

public class MTriggerTime extends GLTextureView {
    private Context context;
    private int width, height;
    private GLPaint paint;
    private Bitmap iconTBitMap, contentBitmap;
    //    private Bitmap bitmap_arrowLeft, bitmap_arrowRight;
    private Bitmap bitmapContent;
    private Bitmap bitmap_tip;
    private Paint mPaint;
    private Canvas contentCanvas;
    private String content1 = "110Mpts";  //显示存储深度
    private String content2 = "Normal";  //显示采样类型
    private String content3 = "500Msa/s";  //显示采样率
    private String content4 = "190/1000";  //显示分段存储
    private String content5 = "0ps";  //显示触发时刻的数字
    private boolean segmentVisible = false; //分段存储是否显示
    private boolean memoryDepthVisible = true; //存储深度是否可显
    private boolean sampleRateVisible = true;  //采样频率是否可显

//    private int contentSpace;//content 数据间隔
//    private int linWidth;//线长度
    private int spaceWidth;//两侧空白的宽度
    private int passagewayColor;//线的颜色
    private int passagewayY;//线的y坐标
    private float bracketMultiple;//括住部分占整个控件的几分之一
    private float normalBracketMultiple;//正常时候括号占整个控件的几分之一
    private int bracketWidth;//括号的宽度
    private int bracketColor;//括号的颜色
    private float iconTPercent;//T位于整个控件的百分比位置
    private boolean iconTVisible;//T的可见性
    private boolean contentVisible;//下半部内容可见性
    private boolean canMove;//是否可以移动
    private int tipWidth;//尖的宽度
    private float tipPercent;//尖位于整个控件的百分比位置
    private int contentLayoutY;//文本区域显示Y坐标
    private Rect ContentRect = new Rect();  //文本的大小,自动测量出来的


    private int bitmap_arrowY = 0;  // 箭头绘制高度
    private int backColor = Color.BLACK; //

    public MTriggerTime(Context context) {
        this(context, null);
    }

    public MTriggerTime(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        paint = new GLPaint();
        passagewayColor = Color.GRAY;
        bracketMultiple = 1;
        normalBracketMultiple = 1;
        bracketWidth = 5;
        bracketColor = 0xFF5D6273;
        iconTPercent = 50;
        tipWidth = 12;
        tipPercent = 0.5f;
//        spaceWidth = 306;//(width - (linWidth - 2)) / 2
        spaceWidth = 11;
        iconTVisible = true;
        contentLayoutY = 15;
        contentVisible = true;
        bitmap_arrowY = 4;
//        linWidth = 320;
//        contentSpace = 15;


//        contentBitmap = Bitmap.createBitmap(910, 35, Bitmap.Config.ARGB_8888);
        contentBitmap = Bitmap.createBitmap(320, 68, Bitmap.Config.ARGB_8888);
//        Bitmap midRectangleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mid_rectangle);
        contentCanvas = new Canvas(contentBitmap);
//        bitmap_arrowLeft = BitmapFactory.decodeResource(getResources(), R.drawable.time_direction_l);
//        bitmap_arrowRight = BitmapFactory.decodeResource(getResources(), R.drawable.time_direction_r);
        bitmapContent = BitmapUtil.getBitmapFromDrawable(context, R.drawable.ic_rectangle_trigger_time_bottom);
        bitmapContent = BitmapUtil.scaleBitmap(bitmapContent, contentBitmap.getWidth(), contentBitmap.getHeight());
        bitmap_tip = BitmapFactory.decodeResource(getResources(), R.drawable.ic_tip);
        tipWidth = bitmap_tip.getWidth();
        Bitmap tBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mini_time_trigger);
        backColor = context.getResources().getColor(R.color.color_Backcolor_MainMenu1);
//        Matrix tipMatrix = new Matrix();
//        tipMatrix.postScale(tipWidth / bitmap_tip.getWidth(), (height / 2 - 2) / bitmap_tip.getHeight());
//        bitmap_tip = Bitmap.createBitmap(tBitmap, 0, 0, bitmap_tip.getWidth(), bitmap_tip.getHeight(), tipMatrix, true);

        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f);
        iconTBitMap = Bitmap.createBitmap(tBitmap, 0, 0, tBitmap.getWidth(), tBitmap.getHeight(), matrix, false);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setARGB(255, 0x25, 0x6F, 0x71); //0x256F71
        mPaint.setStrokeWidth(1);
//        contentCanvas.drawBitmap(bitmap_arrowLeft, 1, bitmap_arrowY, null);
//        contentCanvas.drawBitmap(bitmap_arrowRight, contentBitmap.getWidth() - bitmap_arrowRight.getWidth() + 1, bitmap_arrowY, null);
//        contentCanvas.drawBitmap(midRectangleBitmap, bitmap_arrowLeft.getWidth() + 2, 2, null);

//        contentCanvas.drawBitmap(bitmapContent, 0, 0, null);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(20);
        mPaint.setAntiAlias(true);
//        clipRect.set(13, 3, contentBitmap.getWidth() - 12, contentBitmap.getHeight());
    }

    //region 对外接口
    public MTriggerTime setIconTPercent(float iconTPercent) {
//        if (!canMove) return this;
        this.iconTPercent = iconTPercent;
        return this;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    /**
     * 下半部是否可见
     *
     * @param contentVisible
     * @return
     */
    public MTriggerTime setContentVisible(boolean contentVisible) {
//        if (!canMove) return this;
        this.contentVisible = contentVisible;
        return this;
    }

    public MTriggerTime setTipPercent(float tipPercent) {
//        if (!canMove) return this;
        this.tipPercent = tipPercent;
        return this;
    }

    public MTriggerTime setMultiple(float bracketMultiple) {
//        if (!canMove) return this;
        this.bracketMultiple = bracketMultiple;
        return this;
    }

    public MTriggerTime setNormalMultiple(float normalBracketMultiple) {
//        if (!canMove) return this;
        this.normalBracketMultiple = normalBracketMultiple;
        return this;
    }

    public MTriggerTime setPassagewayColor(int passagewayColor) {
        this.passagewayColor = passagewayColor;
        return this;
    }

    public MTriggerTime setMemoryDepth(String memoryDepth) {
        this.content1 = memoryDepth;
        return this;
    }

    public MTriggerTime setSampleType(String sampleType) {
        this.content2 = sampleType;
        return this;
    }

    public MTriggerTime setSampleRate(String sample) {
        this.content3 = sample;
        return this;
    }

    public MTriggerTime setSegmentedBuffer(String segmentedBuffer) {
        this.content4 = segmentedBuffer;
        return this;
    }

    public String getSegmentedBuffer() {
        return this.content4;
    }

    public boolean isSampleRateVisible() {
        return sampleRateVisible;
    }

    public void setSampleRateVisible(boolean sampleRateVisible) {
        this.sampleRateVisible = sampleRateVisible;
    }

    public boolean isMemoryDepthVisible() {
        return memoryDepthVisible;
    }

    public void setMemoryDepthVisible(boolean memoryDepthVisible) {
        this.memoryDepthVisible = memoryDepthVisible;
    }

    public boolean isSegmentVisible() {
        return segmentVisible;
    }

    /**
     * 分段存储是否显示。
     * 分段存储显示时，与时刻左右分散显示\n
     * 分段存储不显示时，时刻居中显示
     *
     * @param segmentVisible true:显示 false:不显示
     */
    public void setSegmentVisible(boolean segmentVisible) {
        this.segmentVisible = segmentVisible;
    }

    public MTriggerTime setTrigger(String string) {
//        if (!canMove) return this;
        this.content5 = string;
        return this;
    }

    public String getTrigger() {
        return this.content5;
    }

    public void updateUI() {
        requestRender();
    }
    //endregion

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
//        int result = 910;
        int result = 256;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        width = result;
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
//        int result = 30;
        int result = 15;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
        }
        //height = result - contentBitmap.getHeight();
        height = result;
        passagewayY = height / 2;
        return result;
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas) {
/*        if (canMove) {
            drawBackColor(canvas);
            drawContent(canvas);
            drawPic(canvas);
            drawIconT(canvas);
            drawLine(canvas);
        } else {
            drawBackColor(canvas);
            drawContent(canvas);
            drawPic(canvas);
            drawLine(canvas);
        }*/
        if (canMove) {
            drawBackColor(canvas);
            drawLine(canvas);
            drawPic(canvas);
            drawIconT(canvas);
            drawContent(canvas);
        } else {
            drawBackColor(canvas);
            drawLine(canvas);
            drawPic(canvas);
            drawContent(canvas);
        }
    }

    private void drawBackColor(ICanvasGL canvas) {
        canvas.clearBuffer(backColor);
    }

    private void drawLine(ICanvasGL canvas) {
        paint.setColor(passagewayColor);
//        canvas.drawLine((width - linWidth) / 2 + 11, 10, (width + linWidth) / 2 - 11, 10, paint);
        canvas.drawLine(spaceWidth + 1, height / 2, width - spaceWidth, height / 2, paint);
    }

/*    int content1Len, content2Len, content3Len, content4Len, content5Len;
    private synchronized void drawContent(ICanvasGL canvas) {
        //if (this.contentVisible) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(backColor);
//            contentCanvas.drawRect(0,0,contentCanvas.getWidth(),20,mPaint);
            contentCanvas.drawRect(0,0,contentCanvas.getWidth(),contentCanvas.getHeight(),mPaint);


        //触发时刻
        mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
        content5Len = ContentRect.width();
        mPaint.setColor(getResources().getColor(R.color.textColor));
//                contentCanvas.drawText(content5, contentBitmap.getWidth() - ContentRect.width() - 10, contentBitmap.getHeight()-4, mPaint);
        contentCanvas.drawText(content5, (width + linWidth) / 2, height / 2 + 5, mPaint);//时间

        //分段存储
        if (segmentVisible) {
            mPaint.getTextBounds(content4, 0, content4.length(), ContentRect);
            content4Len = ContentRect.width();
            mPaint.setColor(getResources().getColor(R.color.textColor));
//                contentCanvas.drawText(content4, 10, contentBitmap.getHeight()-2, mPaint);
            contentCanvas.drawText(content4, (width + linWidth) / 2 + content5Len + contentSpace - 1, height / 2 + 5, mPaint);
        }

        //采样类型
        mPaint.getTextBounds(content2, 0, content2.length(), ContentRect);
        content2Len = ContentRect.width();
        mPaint.setColor(getResources().getColor(R.color.textColor));
//            contentCanvas.drawText(content2, (contentBitmap.getWidth() - ContentRect.width()) / 2 - 3, 20, mPaint);
        int startX = segmentVisible ? (width + linWidth) / 2 + content5Len + contentSpace + content4Len + contentSpace - 1 : (width + linWidth) / 2 + content5Len + contentSpace;
        contentCanvas.drawText(content2, startX, height / 2 + 5, mPaint);

        //采样率
        if (sampleRateVisible) {
            mPaint.getTextBounds(content3, 0, content3.length(), ContentRect);
            content3Len = ContentRect.width();
            mPaint.setColor(getResources().getColor(R.color.textColor));
//                contentCanvas.drawText(content3, contentBitmap.getWidth() - ContentRect.width() - 10, 20, mPaint);
            contentCanvas.drawText(content3, (width - linWidth) / 2 - content3Len, height / 2 + 5, mPaint);
        }

        //存储深度
        if (memoryDepthVisible) {
            mPaint.getTextBounds(content1, 0, content1.length(), ContentRect);
            content1Len = ContentRect.width();
            mPaint.setColor(getResources().getColor(R.color.textColor));
//                contentCanvas.drawText(content1, 10, 20, mPaint);
            int startXX = sampleRateVisible ? (width - linWidth) / 2 - content3Len - 12 - content1Len : (width - linWidth) / 2 - content1Len;
            contentCanvas.drawText(content1, startXX, height / 2 + 5, mPaint);
        }

//            if (canMove){
//                if (segmentVisible) {
//                    // 与时刻左右分散显示
//                    mPaint.getTextBounds(content4, 0, content4.length(), ContentRect);
//                    mPaint.setColor(getResources().getColor(R.color.textColor));
//                    contentCanvas.drawText(content4, 10,
//                            contentBitmap.getHeight()-2, mPaint);
//
//                    mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
//                    mPaint.setColor(getResources().getColor(R.color.textColor));
//                    contentCanvas.drawText(content5, contentBitmap.getWidth() - ContentRect.width() - 10,
//                            contentBitmap.getHeight()-4, mPaint);
//                }else {
//                    //分段存储不显示时，时刻居中显示
//                    mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
//                    mPaint.setColor(getResources().getColor(R.color.textColor));
//                    contentCanvas.drawText(content5, ((width-ContentRect.width())/2),
//                            contentBitmap.getHeight()-4, mPaint);
//                }
//            }
            canvas.invalidateTextureContent(contentBitmap);
            canvas.drawBitmap(contentBitmap, 0, 0);
        //}
    }

    private void drawPic(ICanvasGL canvas) {
        float tipPercent = this.tipPercent;
        float bracketMultiple = this.bracketMultiple * this.normalBracketMultiple;
        if (!iconTVisible) {
            tipPercent = 0.5f;
            bracketMultiple = 1;
        }
        int barWidth = width - spaceWidth * 2; //298
        //绘制左括号
        float tipLeftWidth = barWidth * tipPercent;
        int lWidth = (int) (tipLeftWidth - barWidth / 2 / bracketMultiple + spaceWidth + 1);
        if (lWidth >= spaceWidth - bracketWidth) {
            paint.setColor(bracketColor);
            canvas.drawLine(lWidth, 10 - bracketWidth, lWidth + bracketWidth - 1, 10 - bracketWidth, paint);//上横线
            canvas.drawLine(lWidth - 1, 10+ bracketWidth, lWidth + bracketWidth - 1, 10 + bracketWidth, paint);//下横线
            canvas.drawLine(lWidth, 10 - bracketWidth, lWidth, 10 + bracketWidth, paint);//竖线
        }
        //绘制右括号
        int rWidth = (int) (tipLeftWidth + barWidth / 2 / bracketMultiple + spaceWidth);
        if (rWidth <= width - spaceWidth + bracketWidth) {
//        if (lWidth <= spaceWidth * 2 + barWidth + bracketWidth) {
            paint.setColor(bracketColor);
            canvas.drawLine(rWidth, 10 - bracketWidth, rWidth - bracketWidth, 10 - bracketWidth, paint);//上横线
            canvas.drawLine(rWidth, 10 + bracketWidth, rWidth - bracketWidth, 10 + bracketWidth, paint);//下横线
            canvas.drawLine(rWidth, 10 - bracketWidth, rWidth, 10 + bracketWidth, paint);//竖线
        }

        int place = (int) tipLeftWidth - tipWidth / 2 + spaceWidth;
        if (place >= tipWidth && place <= spaceWidth * 2 + barWidth + tipWidth) {
            canvas.drawBitmap(bitmap_tip, place - 1, (20 - bitmap_tip.getHeight()) / 2);
        }

//        if (iconTVisible) {
//            float tWidth = spaceWidth + barWidth * iconTPercent - iconTBitMap.getWidth() / 2;
//            if (tWidth < 0)
//                tWidth = 0;
//            else if (tWidth > width - spaceWidth - iconTBitMap.getWidth() / 2)
//                tWidth = width - spaceWidth - iconTBitMap.getWidth() / 2;
//            canvas.drawBitmap(iconTBitMap, (int) tWidth, 0);
//        }
    }

    private void drawIconT(ICanvasGL canvas) {
        if (iconTVisible) {
            int barWidth = width - spaceWidth * 2;
            float tWidth = spaceWidth + barWidth * iconTPercent - iconTBitMap.getWidth() / 2;
//            Logger.e("limh", "tWidth= " + tWidth);
            if (tWidth < spaceWidth - iconTBitMap.getWidth() / 2) {
                tWidth = spaceWidth - iconTBitMap.getWidth() / 2;
            } else if (tWidth > width - spaceWidth - iconTBitMap.getWidth() / 2) {
                tWidth = width - spaceWidth - iconTBitMap.getWidth() / 2;
            }
//            Logger.e("limh", "tWidth1111= " + tWidth);
            canvas.drawBitmap(iconTBitMap, (int) tWidth-1, (20-iconTBitMap.getHeight())/2);
        }
    }*/

    public Rect getTriggerRect() {
        return triggerRect;
    }


    private final Rect triggerRect = new Rect();
    private synchronized void drawContent(ICanvasGL canvas) {
        //if (this.contentVisible) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(backColor);
        contentCanvas.drawRect(0,0,contentCanvas.getWidth(),28,mPaint);
        contentCanvas.drawRect(0,48,contentCanvas.getWidth(),contentCanvas.getHeight(),mPaint);

        if (memoryDepthVisible==true) {
            mPaint.getTextBounds(content1, 0, content1.length(), ContentRect);
            mPaint.setColor(getResources().getColor(R.color.textColor));
            contentCanvas.drawText(content1, 10,
                    20, mPaint);
        }

        mPaint.getTextBounds(content2, 0, content2.length(), ContentRect);
        mPaint.setColor(getResources().getColor(R.color.textColor));
        contentCanvas.drawText(content2, (contentBitmap.getWidth() - ContentRect.width()) / 2 - 3,
                20, mPaint);

        if (sampleRateVisible==true) {
            mPaint.getTextBounds(content3, 0, content3.length(), ContentRect);
            mPaint.setColor(getResources().getColor(R.color.textColor));
            contentCanvas.drawText(content3, contentBitmap.getWidth() - ContentRect.width() - 10,
                    20, mPaint);
        }

        if (canMove){
            if (segmentVisible) {
                // 与时刻左右分散显示
                mPaint.getTextBounds(content4, 0, content4.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content4, 10,
                        contentBitmap.getHeight()-2, mPaint);

                mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content5, contentBitmap.getWidth() - ContentRect.width() - 10,
                        contentBitmap.getHeight()-4, mPaint);

                triggerRect.left  =  contentBitmap.getWidth() - ContentRect.width() - 10 + ContentRect.left;
                triggerRect.top = contentBitmap.getHeight()-4 + ContentRect.top;
                triggerRect.right = contentBitmap.getWidth() - ContentRect.width() - 10 + ContentRect.right;
                triggerRect.bottom = contentBitmap.getHeight()-4 + ContentRect.bottom;
            }else {
                //分段存储不显示时，时刻居中显示
                mPaint.getTextBounds(content5, 0, content5.length(), ContentRect);
                mPaint.setColor(getResources().getColor(R.color.textColor));
                contentCanvas.drawText(content5, ((width-ContentRect.width())/2),
                        contentBitmap.getHeight()-4, mPaint);

                triggerRect.left = ((width - ContentRect.width()) / 2) + ContentRect.left;
                triggerRect.top = contentBitmap.getHeight() - 4 + ContentRect.top;
                triggerRect.right = ((width - ContentRect.width()) / 2) + ContentRect.right;
                triggerRect.bottom = contentBitmap.getHeight() - 4 + ContentRect.bottom;
            }
        }
        canvas.invalidateTextureContent(contentBitmap,null);
        canvas.drawBitmap(contentBitmap, 0, 0);
        //}
    }

    private void drawPic(ICanvasGL canvas) {
        float tipPercent = this.tipPercent;
        float bracketMultiple = this.bracketMultiple * this.normalBracketMultiple;
        if (!iconTVisible) {
            tipPercent = 0.5f;
            bracketMultiple = 1;
        }
        int barWidth = width - spaceWidth * 2;
        //绘制左括号
        float tipLeftWidth = barWidth * tipPercent;
        int lWidth = (int) (tipLeftWidth - barWidth / 2 / bracketMultiple + spaceWidth + 1);
        if (lWidth >= -bracketWidth) {
            paint.setColor(bracketColor);
            canvas.drawLine(lWidth, height / 2 - bracketWidth, lWidth + bracketWidth - 1, height / 2 - bracketWidth, paint);
            canvas.drawLine(lWidth - 1, height / 2 + bracketWidth, lWidth + bracketWidth - 1, height / 2 + bracketWidth, paint);
            canvas.drawLine(lWidth, height / 2 - bracketWidth, lWidth, height / 2 + bracketWidth, paint);
        }
        //绘制右括号
        int rWidth = (int) (tipLeftWidth + barWidth / 2 / bracketMultiple + spaceWidth);
        if (lWidth <= spaceWidth * 2 + barWidth + bracketWidth) {
            paint.setColor(bracketColor);
            canvas.drawLine(rWidth, height / 2 - bracketWidth, rWidth - bracketWidth, height / 2 - bracketWidth, paint);
            canvas.drawLine(rWidth, height / 2 + bracketWidth, rWidth - bracketWidth, height / 2 + bracketWidth, paint);
            canvas.drawLine(rWidth, height / 2 - bracketWidth, rWidth, height / 2 + bracketWidth, paint);
        }

        int place = (int) tipLeftWidth - tipWidth / 2 + spaceWidth;
        if (place >= tipWidth && place <= spaceWidth * 2 + barWidth + tipWidth) {
            canvas.drawBitmap(bitmap_tip,place-1,(height-bitmap_tip.getHeight())/2);
        }

//        if (iconTVisible) {
//            float tWidth = spaceWidth + barWidth * iconTPercent - iconTBitMap.getWidth() / 2;
//            if (tWidth < 0)
//                tWidth = 0;
//            else if (tWidth > width - spaceWidth - iconTBitMap.getWidth() / 2)
//                tWidth = width - spaceWidth - iconTBitMap.getWidth() / 2;
//            canvas.drawBitmap(iconTBitMap, (int) tWidth, 0);
//        }
    }

    private void drawIconT(ICanvasGL canvas) {
        if (iconTVisible) {
            int barWidth = width - spaceWidth * 2;
            float tWidth = spaceWidth + barWidth * iconTPercent - iconTBitMap.getWidth() / 2;
            if (tWidth < 1) {
                tWidth = 1;
            } else if (tWidth > width - spaceWidth - iconTBitMap.getWidth() / 2) {
                tWidth = width - spaceWidth - iconTBitMap.getWidth() / 2;
            }
            canvas.drawBitmap(iconTBitMap, (int) tWidth-1, (height-iconTBitMap.getHeight())/2);
        }
    }


    @Override
    protected int getRenderMode() {
        return GLThread.RENDERMODE_WHEN_DIRTY;
    }
}
