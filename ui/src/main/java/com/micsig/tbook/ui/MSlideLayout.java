package com.micsig.tbook.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsoluteLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2022-2-9 19:47
 */
public class MSlideLayout extends AbsoluteLayout {
    public static final String TAG=MSlideLayout.class.getSimpleName();

    //滑动范围无效大小,即滑动200 测认为是无效，继续显示当前界面
    public static final int MinSliderRange=80;
    //滑动方向
    public static final int SliderDir_None = 0x00;
    public static final int SliderDir_LeftToRight = 0x01;
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight;
    public static final int SliderDir_TopToBottom = 0x02;
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom;
    //滑动方向最小识别大小
    public static final int SlipDirectionSize_X=20;
    public static final int SlipDirectionSize_Y=20;
    //最小滑动距离
    public static final int SLIDE_DISTANCE_MIN =25;

    private Context context;
    private List<View> items=new ArrayList<>();
    private int downX,downY,moveX,moveY;
    private VelocityTracker vTracker = null;
    private float velocityY;
    private int pointerId;
    private int maxVelocity;
    private int slipDir=SliderDir_None;
    //region 属性
    public interface OnTabChanged{
        void onTabChanged(boolean channel);
    }

    private OnTabChanged onTabChanged;

    private int ShowIndex=0;

    private boolean noMove;

    public boolean isNoMove() {
        return noMove;
    }

    public void setNoMove(boolean noMove) {
        this.noMove = noMove;
    }

    public OnTabChanged getOnTabChanged() {
        return onTabChanged;
    }

    public void setOnTabChanged(OnTabChanged onTabChanged) {
        this.onTabChanged = onTabChanged;
    }

    //endregion

    public MSlideLayout(Context context) {
        this(context,null);
    }

    public MSlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MSlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (items.size()<=0) {
            items.clear();
            for (int i = 0; i < this.getChildCount(); i++) {
                if (this.getChildAt(i).getVisibility() == VISIBLE) {
                    items.add(this.getChildAt(i));
                }
            }
        }
        super.onLayout(changed, l, t, r, b);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (noMove) {
                    break;
                }
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                slipDir=SliderDir_None;
                break;
            case MotionEvent.ACTION_MOVE:
                if (noMove) {
                    break;
                }
                moveX = (int) ev.getX();
                moveY = (int) ev.getY();
//                Logger.i(FPGACommand.TAG,"offsetY:"+Math.abs(moveY - downY));
                if (/*Math.abs(moveX - downX) > SLIDE_DISTANCE_MIN  ||*/ Math.abs(moveY - downY) > SLIDE_DISTANCE_MIN ) {
                    return true;//如果是滑动，就自己处理，也就是交给onTouchEvent；其他情况就传下去
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (vTracker == null) {
            vTracker = VelocityTracker.obtain();
        } else {
            vTracker.clear();
        }
        vTracker.addMovement(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                velocityY = 0;
                downX = (int) event.getX();
                downY = (int) event.getY();
                slipDir=SliderDir_None;
                break;
            case MotionEvent.ACTION_MOVE:
                if (noMove) {
                    break;
                }
//                Logger.i(FPGACommand.TAG,"slipDir:"+slipDir);
                vTracker.computeCurrentVelocity(1000, maxVelocity);
                float vY = vTracker.getYVelocity(pointerId);
                if (vY != 0) {
                    velocityY = vY;
                }
                moveX = (int) event.getX();
                moveY = (int) event.getY();
                if (slipDir==SliderDir_None) {
                    slipDir = getSlipDirection(downX, downY, moveX, moveY);
//                    Logger.i(FPGACommand.TAG,"slipDir:"+slipDir+",downX:"+downX+",downY:"+downY+",moveX:"+moveX+",moveY:"+moveY);
                }
                if (slipDir == SliderDir_TopToBottom) {
                    View CurView=getCurView();
                    View PreView=getPreView();
                    if (CurView==PreView)break;
                    CurView.setVisibility(VISIBLE);
                    PreView.setVisibility(VISIBLE);
                    int offsetY=Math.abs(moveY-downY);
                    CurView.setAlpha((CurView.getHeight()-offsetY)*1.0f/(CurView.getHeight()-200));
                    CurView.setY(offsetY);
                    PreView.setY(offsetY-PreView.getHeight());
                }
                if (slipDir == SliderDir_BottomToTop){
                    View CurView=getCurView();
                    View NextView=getNextView();
                    if (CurView==NextView)break;
                    CurView.setVisibility(VISIBLE);
                    NextView.setVisibility(VISIBLE);
                    int offsetY=Math.abs(moveY-downY);
                    CurView.setAlpha((CurView.getHeight()-offsetY)*1.0f/(CurView.getHeight()-200));
                    CurView.setY(0-offsetY);
                    NextView.setY(NextView.getHeight()-offsetY);
                }
                break;

            case MotionEvent.ACTION_UP: {
                View CurView = getCurView();
                View PreView = getPreView();
                View NextView = getNextView();
                if (CurView == NextView) break;

                //再计算一下滑动方向
                moveX = (int) event.getX();
                moveY = (int) event.getY();
                int upSlipDir= getSlipDirection(downX, downY, moveX, moveY);
                int range=Math.abs(moveY-downY);
                if(event.isFromSource(InputDevice.SOURCE_MOUSE)){
                    velocityY = moveY - downY;
                    velocityY *= 1.5;
                }
//                Logger.i(FPGACommand.TAG,"velocity:"+velocityY+",slipDir:"+slipDir);
                if (/*slipDir == SliderDir_TopToBottom && slipDir==upSlipDir*/upSlipDir==SliderDir_TopToBottom) {
                    if (velocityY > MinSliderRange) {
                        ShowIndexLayout(PreView);
                    } else {
                        ShowIndexLayout(CurView);
                    }
                } else if (/*slipDir == SliderDir_BottomToTop && slipDir==upSlipDir*/upSlipDir==SliderDir_BottomToTop) {
                    if (velocityY < 0 - MinSliderRange) {
                        ShowIndexLayout(NextView);
                    } else {
                        ShowIndexLayout(CurView);
                    }
                }else {
                    ShowIndexLayout(CurView);
                }
                slipDir=SliderDir_None;
            }break;
            case MotionEvent.ACTION_CANCEL:{
                View CurView=getCurView();
                ShowIndexLayout(CurView);
                slipDir=SliderDir_None;
            }break;
        }

        return true;
    }



    private View getIndexView(int showIndex){
        if (showIndex>=0 && showIndex<items.size()){
            return items.get(showIndex);
        }else{
            return null;
        }
    }
    private View getCurView(){
        return getIndexView(ShowIndex);
    }
    private View getPreView(){
        int preView=ShowIndex-1;
        if (preView<0){
            preView=items.size()-1;
        }
        return getIndexView(preView);
    }
    private View getNextView(){
        int nextView=ShowIndex+1;
        if (nextView>=items.size()){
            nextView=0;
        }
       return getIndexView(nextView);
    }
    public int getCurViewIdx(){
        return ShowIndex;
    }
    /*
    返回滑动方向
     */
    public static int getSlipDirection(int oldX, int oldY, int newX, int newY) {
        int slipDir;
        if (Math.abs(oldY-newY)>SlipDirectionSize_Y && Math.abs(oldY-newY) > Math.abs(oldX-newX)*2 ){
            //说明是上下滑动
            if (oldY - newY > 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {
                slipDir = SliderDir_BottomToTop;
            } else if (oldY - newY < 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {
                slipDir = SliderDir_TopToBottom;
            } else {
                slipDir = SliderDir_None;
            }
        } else {
            //说明是左右滑动
            if (oldX - newX > 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {
                slipDir = SliderDir_RightToLeft;
            } else if (oldX - newX < 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {
                slipDir = SliderDir_LeftToRight;
            } else {
                slipDir = SliderDir_None;
            }
        }

        return slipDir;
    }

    private void hideAllItemLayout(){
        for(int i=0;i<items.size();i++){
            items.get(i).setVisibility(GONE);
            items.get(i).setAlpha(1);
        }
    }

    private void ShowIndexLayout(View view){
        hideAllItemLayout();
        for(int i=0;i<items.size();i++){
            if (items.get(i)==view && ShowIndex!=i){
                ShowIndex=i;
                if (onTabChanged!=null){
                    onTabChanged.onTabChanged(ShowIndex==0?true:false);
                }
                break;
            }
        }
        view.setVisibility(VISIBLE);
        view.setX(0);
        view.setY(0);
    }
    public void ShowIndexLayout(int index){
        if (index < 0 || index >= items.size()) return;
        ShowIndex=index;
        hideAllItemLayout();
        items.get(index).setVisibility(VISIBLE);
        items.get(index).setX(0);
        items.get(index).setY(0);
    }


}
