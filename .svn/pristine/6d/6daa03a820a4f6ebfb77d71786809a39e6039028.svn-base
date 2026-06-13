package com.micsig.tbook.tbookscope.main.maincenter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Action.UiMessage;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.fpga.FPGA_CHAZHI_COEF;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.probe.ProbeFactory;
import com.micsig.tbook.scope.surface.XDmaDevManage;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;


public class MainLayoutCenterTestJZ extends RelativeLayout {

    private static final String TAG = "MainLayoutCenterTest";



    private Context context;

    TextView tvadfsA,tvadfsB,tvadfsC,tvadfsD,tvPGA;
    SeekBar adfsSeekBarA,adfsSeekBarB,adfsSeekBarC,adfsSeekBarD,pgaseekBarA;

    CheckBox cbAdDebug;



    private RadioButton rbCh1, rbCh2, rbCh3, rbCh4,rbCh5, rbCh6, rbCh7, rbCh8;
    private RadioButton rbAdGain,rbAdOffset;


    SharedPreferences sharedPreferences;

    Button btnSave,btnLoad;

    public MainLayoutCenterTestJZ(Context context) {
        this(context, null);
    }

    public MainLayoutCenterTestJZ(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterTestJZ(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.argb(0.5f, 0.f, 0.f, 0.f)); // 设置背景颜色
        shape.setStroke(2, Color.GRAY); // 设置边框宽度和颜色
        shape.setCornerRadius(4); // 设置圆角大小

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            shape.setPadding(1, 1, 1, 1);
        }

        setBackground(shape);

        sharedPreferences = context.getSharedPreferences("MainLayoutCenterTestJZ",MODE_PRIVATE);
    }



    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maincenter_test_jz, this);




        btnSave = findViewById(R.id.btnSavejz);
        btnLoad = findViewById(R.id.btnLoadjz);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //CabteRegister.getInstance().saveUserCalibrateParam();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                SeekBar [] sb = {adfsSeekBarA,adfsSeekBarB,adfsSeekBarC,adfsSeekBarD};
                String pre_key = rbAdOffset.isChecked() ? "adOffset" : "adGain";
                for (int i = 0; i < sb.length; i++) {
                    int val = sb[i].getProgress();
                    editor.putInt(pre_key + i, val);
                }
                editor.apply();
            }
        });

        btnLoad.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar [] sb = {adfsSeekBarA,adfsSeekBarB,adfsSeekBarC,adfsSeekBarD};
                String pre_key = rbAdOffset.isChecked() ? "adOffset" : "adGain";
                for (int i = 0; i < sb.length; i++) {
                    int val = sharedPreferences.getInt(pre_key+i,-1);
                    if(val >= 0){
                        sb[i].setProgress(val);
                    }
                }
            }
        });


        rbCh1 = (RadioButton) findViewById(R.id.testChannelsCh1jz);
        rbCh2 = (RadioButton) findViewById(R.id.testChannelsCh2jz);
        rbCh3 = (RadioButton) findViewById(R.id.testChannelsCh3jz);
        rbCh4 = (RadioButton) findViewById(R.id.testChannelsCh4jz);
        rbCh5 = (RadioButton) findViewById(R.id.testChannelsCh5jz);
        rbCh6 = (RadioButton) findViewById(R.id.testChannelsCh6jz);
        rbCh7 = (RadioButton) findViewById(R.id.testChannelsCh7jz);
        rbCh8 = (RadioButton) findViewById(R.id.testChannelsCh8jz);

        rbAdGain = (RadioButton) findViewById(R.id.adGainjz);
        rbAdOffset = (RadioButton) findViewById(R.id.adOffsetjz);

        cbAdDebug = findViewById(R.id.adDebug);

        rbCh1.setOnClickListener(onClickListener);
        rbCh2.setOnClickListener(onClickListener);
        rbCh3.setOnClickListener(onClickListener);
        rbCh4.setOnClickListener(onClickListener);
        rbCh5.setOnClickListener(onClickListener);
        rbCh6.setOnClickListener(onClickListener);
        rbCh7.setOnClickListener(onClickListener);
        rbCh8.setOnClickListener(onClickListener);

        rbAdGain.setOnClickListener(onClickListener);
        rbAdOffset.setOnClickListener(onClickListener);



        tvadfsA = findViewById(R.id.tvadfsA);
        tvadfsB = findViewById(R.id.tvadfsB);
        tvadfsC = findViewById(R.id.tvadfsC);
        tvadfsD = findViewById(R.id.tvadfsD);
        tvPGA = findViewById(R.id.tvPGA);
        adfsSeekBarA = findViewById(R.id.adfsseekBarA);
        adfsSeekBarB = findViewById(R.id.adfsseekBarB);
        adfsSeekBarC = findViewById(R.id.adfsseekBarC);
        adfsSeekBarD = findViewById(R.id.adfsseekBarD);
        pgaseekBarA = findViewById(R.id.pgaseekBarA);

        adfsSeekBarA.setOnSeekBarChangeListener(onSeekBarChangeListener);
        adfsSeekBarB.setOnSeekBarChangeListener(onSeekBarChangeListener);
        adfsSeekBarC.setOnSeekBarChangeListener(onSeekBarChangeListener);
        adfsSeekBarD.setOnSeekBarChangeListener(onSeekBarChangeListener);
        pgaseekBarA.setOnSeekBarChangeListener(onSeekBarChangeListener);



//        setBackgroundResource(R.drawable.bg_rg_channels);
        setClickable(true);
        updateView();
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_RESISTANCETYPE,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE,eventUIObserver);

    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            btnSave.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            },100);
        }
    };
    private void updateView(){
        Channel channel = ChannelFactory.getDynamicChannel(getChIdx());
        if(channel != null) {
            TextView [] tv = {tvadfsA,tvadfsB,tvadfsC,tvadfsD};
            SeekBar [] sb = {adfsSeekBarA,adfsSeekBarB,adfsSeekBarC,adfsSeekBarD};

            String[] tt = {"0x8C","0x8A","0x94","0x92"};
            if(rbAdGain.isChecked()){
                tt[0] = "0x32";
                tt[1] = "0x30";
            }
            for(int i=0;i<tv.length;i++){

                int val = rbAdGain.isChecked() ? 0xA000 : 0;


                sb[i].setMin(0);
                sb[i].setMax(rbAdGain.isChecked()? 0xFFFF : 0x7FF);
                sb[i].setProgress(val);
                tv[i].setText(tt[i] + ":" + Integer.toHexString(val & 0xFFFF));
            }
        }
    }
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //if(fromUser)
            {
                Scope scope = Scope.getInstance();
                int cnt = scope.getChannelSampOnCnt()/4;
                Channel channel = ChannelFactory.getDynamicChannel(getChIdx());
                CabteRegister cabteRegister = CabteRegister.getInstance();
                int adIdx = -1;
                int chIdx = channel.getChId();
                switch (seekBar.getId()){
                    default:
                    case R.id.adfsseekBarA:
                        adIdx = 0;
                        break;
                    case R.id.adfsseekBarB:
                        adIdx = 1;
                        break;
                    case R.id.adfsseekBarC:
                        adIdx = 2;
                        break;
                    case R.id.adfsseekBarD:
                        adIdx = 3;
                        break;
                    case  R.id.pgaseekBarA:
                        break;
                }

                if(adIdx >= 0) {
                    String[] tt = {"0x8C","0x8A","0x94","0x92"};
                    if (rbAdGain.isChecked()) {
                        tt[0] = "0x32";
                        tt[1] = "0x30";
                        FPGACommand.getInstance().writeAD_gain(chIdx/4, chIdx % 4 / 2, adIdx,progress & 0xFFFF);
                    } else {
                        FPGACommand.getInstance().writeAD_offset(chIdx/4, chIdx % 4 / 2, adIdx,progress & 0x7FF);
                    }

                    TextView[] tv = {tvadfsA, tvadfsB, tvadfsC, tvadfsD};
                    SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD};

                    tv[adIdx].setText(tt[adIdx] + ":" + Integer.toHexString(sb[adIdx].getProgress() & 0xFFFF));

                }else{
                    FPGACommand fpgaCommand = FPGACommand.getInstance();
                    int pgaVal = progress|0x200;
                    fpgaCommand.setDebugPGA(pgaVal);
                    FPGACommand.getInstance().SendAD8370Data(0,new int[] {pgaVal,pgaVal,pgaVal,pgaVal});
                    FPGACommand.getInstance().SendAD8370Data(1,new int[] {pgaVal,pgaVal,pgaVal,pgaVal});
                    tvPGA.setText("P:" + Integer.toHexString(progress));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if(v.getId()  == rbAdGain.getId()
                    || v.getId() == rbAdOffset.getId()){
                SeekBar [] sb = {adfsSeekBarA,adfsSeekBarB,adfsSeekBarC,adfsSeekBarD};
                for (SeekBar seekBar : sb) {
                    seekBar.setMin(0);
                    seekBar.setMax(rbAdGain.isChecked() ? 0xFFFF : 0x7FF);
                }
            }
//            Channel channel = ChannelFactory.getDynamicChannel(getChIdx());
//            channel.setVScaleId(channel.getVScaleId(),true);
//            updateView();
        }
    };


    private int getChIdx(){
        RadioButton [] radioButtons = {rbCh1,rbCh2,rbCh3,rbCh4,rbCh5,rbCh6,rbCh7,rbCh8};
        int chidx = 0;
        for(int i = 0 ;i < radioButtons.length;i++){
            if(radioButtons[i].isChecked()){
                chidx = i;
                break;
            }
        }
        return chidx;
    }

    float downX, downY;
    float moveX, moveY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                left = (int) this.getX();
                top = (int) this.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = ev.getRawX();
                moveY = ev.getRawY();
                if (Math.abs(moveX - downX) > 5 || Math.abs(moveY - downY) > 5) {
                    return true;//如果是滑动，就自己处理，也就是交给onTouchEvent；其他情况就传下去
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    int left, top;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getRawX();
                moveY = event.getRawY();

                Rect screen = GlobalVar.get().getScreen();
                float tmpX = getX() + (moveX - downX);
                if (tmpX < screen.left) {
                    tmpX = screen.left;
                }
                if (tmpX + getWidth() > screen.right) {
                    tmpX = screen.right - getWidth();
                }

                float tmpY = getY() + (moveY - downY);
                if (tmpY < screen.top) {
                    tmpY = screen.top;
                }
                if (tmpY + getHeight() > screen.bottom) {
                    tmpY = screen.bottom - getHeight();
                }
                this.setX(tmpX);
                this.setY(tmpY);
                downX = moveX;
                downY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                if (left != this.getX() || top != this.getY()) {
                }
                break;
        }
        return true;
    }

    public void setLocation(int x, int y) {
        setX(x);
        setY(y);
    }

    private Rect r = new Rect();

    public boolean containsPoint(int x, int y) {
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        return x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE;
    }

    public boolean containsDownPoint(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        boolean b = x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE && event.getAction() == MotionEvent.ACTION_DOWN;
        return b;
    }
}
