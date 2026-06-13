package com.micsig.tbook.tbookscope.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.MProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liwb on 2018/5/24.
 * <pre class="prettyprint">
 *  ScreenControls:是一个屏幕控制类，控制触摸屏是否锁屏，外部按键是否锁死等一些操作。
 * </pre>
 */

public class ScreenControls {
    private static final String TAG="ScreenControls";

    //region  单例
    private static class ScreenControlsHolder {
        public static final ScreenControls instance = new ScreenControls();
    }

    public static ScreenControls getInstance() {
        return ScreenControls.ScreenControlsHolder.instance;
    }
    //endregion

    public Handler handler;
    public final int MSG_LOCKSCREEN = 0xAF001;
    public final int MSG_LOCKSTATEHIDE = 0xAF002;

    private FrameLayout MaskLayerLayout;
    private MainViewGroup mainViewGroup;

    private boolean isExternalKey = false;
    private Context context;

    private MProgress progress;
    private View progressView;
    private View lockState;

    private TextView tipsSelfAdjust;
    private TextView tipsFactoryAdjust;

//    private TextView tipsAutoZeroing;

    public interface IScreenLockListener{
        void onLockScreen(boolean bLockScreen);
    }
    private IScreenLockListener screenLockListener;

    @SuppressLint("ClickableViewAccessibility")
    public void init(MainViewGroup mainViewGroup, Context context,IScreenLockListener screenLockListener) {
        this.context = context;
        this.mainViewGroup = mainViewGroup;
        this.screenLockListener = screenLockListener;
        MaskLayerLayout = (FrameLayout) mainViewGroup.findViewById(R.id.MaskLayerLayout);
        View view = View.inflate(this.context, R.layout.layout_main_lock_progress, this.MaskLayerLayout);
        lockState=view.findViewById(R.id.lock_screentClickState);
        progressView = view.findViewById(R.id.progressView);
        progress = (MProgress) view.findViewById(R.id.progress);
        view.setVisibility(View.GONE);
        view.requestLayout();


        tipsSelfAdjust = (TextView) ((MainActivity) context).findViewById(R.id.tipsSelfAdjust);

        tipsFactoryAdjust = (TextView) ((MainActivity) context).findViewById(R.id.tipsFactoryAdjust);

//        tipsAutoZeroing = (TextView) ((MainActivity) context).findViewById(R.id.briefChannelAutoZeroing);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_LOCKSCREEN) {
                    processLockScreen();
                }else if(msg.what == MSG_LOCKSTATEHIDE){
                    lockState.setVisibility(View.GONE);
                }
            }
        };
    }

    public void setMaskLayerLayoutOnTouchListener(MotionEvent ev){
        if (isLockScreen() && isExternalKey==false && ev.getMetaState()!=1){
            switch (ev.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    lockState.setVisibility(View.VISIBLE);
                    Message msg=new Message();
                    msg.what=MSG_LOCKSTATEHIDE;
                    handler.sendMessageDelayed(msg,1000);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    lockState.setVisibility(View.GONE);
                    break;
            }
        }
    }
    public void onUI(Runnable runnable){
        handler.post(runnable);
    }


    private volatile int lockflag = 0;

    public static final int LOCK_SCREEN = (1<<0);

    public static final int LOCK_PROGRESS = (1<<1);

    public static final int LOCK_SELF_ADJUST = (1<<2);
    public static final int LOCK_FACTORY_ADJUST = (1<<3);
    public static final int LOCK_KEY = (1<<4);

    public static final int LOCK_PROBE = (1<<5);

    public static final int LOCK_LOADCSV = (1<<16);

    public static final int LOCK_LOADCSV_MASK = 0xFF << 16;


    public ScreenControls setProgressValue(int value) {
        if (progress != null) progress.setProgress(value);
        return this;
    }

    private void processLockScreen(){
        Log.d(TAG,"lockflag:" + Integer.toHexString(lockflag));
        int v = (lockflag & LOCK_PROGRESS) != 0?View.VISIBLE:View.GONE;
        if((lockflag & LOCK_LOADCSV_MASK) != 0){
            v = View.VISIBLE;
        }

        progressView.setVisibility(v);
        progressView.requestLayout();
        if((lockflag & LOCK_PROGRESS) == 0){
//            setProgressValue(0);
        }
//        tipsSelfAdjust.setVisibility((lockflag & LOCK_SELF_ADJUST) != 0 ? View.VISIBLE : View.GONE);
        tipsFactoryAdjust.setVisibility((lockflag & LOCK_FACTORY_ADJUST) != 0 ? View.VISIBLE : View.GONE);
//        tipsAutoZeroing.setVisibility((lockflag & LOCK_PROBE) != 0 ? View.VISIBLE : View.GONE);
        this.MaskLayerLayout.setVisibility(lockflag != 0 ? View.VISIBLE:View.GONE);
    }

    Map<Integer,Integer> refcsvmap = new HashMap<>();

    public synchronized void csvupdate(int flag,int val){
        if(lockflag != 0
                && ((flag & LOCK_LOADCSV_MASK) != 0) ){
            refcsvmap.put(flag,val);
            AtomicInteger s = new AtomicInteger();
            refcsvmap.forEach((key,value)->{
                s.addAndGet(value);
            });
            progress.post(()->{
                setProgressValue(20 + s.get()/refcsvmap.size() * 80 / 100);
            });
        }
    }
    /***
     * 锁屏幕
     */
    public ScreenControls lockScreen(int flag) {
//        Log.d(TAG, "lockScreen() called with: flag = [" + Integer.toHexString(flag) + "]" + "," + Integer.toHexString(lockflag));
        synchronized (this) {
            lockflag |= flag;
        }

        refcsvmap.clear();
        for(int i=16;i<24;i++){
            if((lockflag & (1<<16)) != 0){
                refcsvmap.put(flag,0);
            }
        }


        handler.sendEmptyMessage(MSG_LOCKSCREEN);
        if(screenLockListener != null){
            screenLockListener.onLockScreen(lockflag != 0);
        }
        return this;
    }

    public ScreenControls unLockScreen(int flag) {
//        Log.d(TAG, "unLockScreen() called with: flag = [" + Integer.toHexString(flag) + "]");
        synchronized (this) {
            lockflag &= ~flag;
        }

        handler.sendEmptyMessage(MSG_LOCKSCREEN);
        if(screenLockListener != null){
            screenLockListener.onLockScreen(lockflag != 0);
        }
        return this;
    }



    public boolean isLockScreen(int flag){
        return (lockflag & flag) == flag;
    }

    public boolean isLockScreen() {
        return lockflag != 0;
    }

    public boolean isExternalKey() {
        return (lockflag & (~LOCK_KEY)) != 0 ;
    }
}
