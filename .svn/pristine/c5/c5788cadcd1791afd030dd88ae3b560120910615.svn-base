package com.micsig.tbook.tbookscope.main.maincenter;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.HwConfig;
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


public class MainLayoutCenterTest extends RelativeLayout {

    private static final String TAG = "MainLayoutCenterTest";



    private Context context;
    TextView tvDaSetp,tvRoolSetp;
    TextView tvDcp1,tvPga,tvDa;
    SeekBar seekBar,roolSeekBar;

    Button btnDcp1Add,btnDcp1Sub;

    Button btnDaAdd,btnDaSub;

    Button btnPgaAdd,btnPgaSub;
    Button btnSwitchCoef;
    private RadioGroup group;
    private RadioButton rbCh1, rbCh2, rbCh3, rbCh4,rbCh5, rbCh6, rbCh7, rbCh8;
    private TextView cmiref_ab,cmiref_cd;
    private SeekBar cmiref_ab_seekBar,cmiref_cd_seekBar;

    Button btnFpgaStop,btnFpgaStart;


    Button btnAdjz,btnSave;

    public MainLayoutCenterTest(Context context) {
        this(context, null);
    }

    public MainLayoutCenterTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }



    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maincenter_test, this);

        cmiref_ab = findViewById(R.id.cmiref_ab);
        cmiref_cd = findViewById(R.id.cmiref_cd);

        cmiref_ab_seekBar = findViewById(R.id.cmiref_ab_seekBar);
        cmiref_cd_seekBar = findViewById(R.id.cmiref_cd_seekBar);

        cmiref_ab_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    FPGACommand.getInstance().SendADData(0,0,
                            0x8000|0x0905,progress & 0x1F);
                }
                cmiref_ab.setText("ab:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cmiref_cd_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    FPGACommand.getInstance().SendADData(0,0,
                            0x8000|0x0906,progress & 0x1F);
                }
                cmiref_cd.setText("cd:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tvDcp1 = findViewById(R.id.tvdcp1);

        tvPga = findViewById(R.id.tvpga);
        tvDa = findViewById(R.id.tvpda);
        tvDaSetp = findViewById(R.id.tvadstep);
        tvRoolSetp = findViewById(R.id.tvroolstep);

        btnDcp1Add = findViewById(R.id.dcp1add);
        btnDcp1Sub = findViewById(R.id.dcp1sub);

        btnDaAdd = findViewById(R.id.daadd);
        btnDaSub = findViewById(R.id.dasub);

        btnPgaAdd = findViewById(R.id.pgaadd);
        btnPgaSub = findViewById(R.id.pgasub);

        btnSwitchCoef = findViewById(R.id.switch_btn);

        btnFpgaStart = findViewById(R.id.fpga_start);
        btnFpgaStop = findViewById(R.id.fpga_stop);



        btnAdjz = findViewById(R.id.ad_jz);
        btnAdjz.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ScopeMessage.getInstance().setADCalibrate();
            }
        });
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CabteRegister.getInstance().saveUserCalibrateParam();
            }
        });

        group = (RadioGroup) findViewById(R.id.testChannels);
        rbCh1 = (RadioButton) findViewById(R.id.testChannelsCh1);
        rbCh2 = (RadioButton) findViewById(R.id.testChannelsCh2);
        rbCh3 = (RadioButton) findViewById(R.id.testChannelsCh3);
        rbCh4 = (RadioButton) findViewById(R.id.testChannelsCh4);
        rbCh5 = (RadioButton) findViewById(R.id.testChannelsCh5);
        rbCh6 = (RadioButton) findViewById(R.id.testChannelsCh6);
        rbCh7 = (RadioButton) findViewById(R.id.testChannelsCh7);
        rbCh8 = (RadioButton) findViewById(R.id.testChannelsCh8);



        seekBar = (SeekBar)findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDaSetp.setText("DA步进:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        roolSeekBar = findViewById(R.id.roolseekBar);
        roolSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRoolSetp.setText("滚屏帧数:" + progress);
                if(fromUser){
                    Sample.getInstance().setRoolFrameRate(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        roolSeekBar.setProgress(Sample.getInstance().getRoolFrameRate());






        setBackgroundResource(R.drawable.bg_rg_channels);
        setClickable(true);

        btnDcp1Add.setOnClickListener(onClickListener);
        btnDcp1Sub.setOnClickListener(onClickListener);

        btnDaAdd.setOnClickListener(onClickListener);
        btnDaSub.setOnClickListener(onClickListener);
        btnPgaAdd.setOnClickListener(onClickListener);
        btnPgaSub.setOnClickListener(onClickListener);

        rbCh1.setOnClickListener(onClickListener);
        rbCh2.setOnClickListener(onClickListener);
        rbCh3.setOnClickListener(onClickListener);
        rbCh4.setOnClickListener(onClickListener);
        rbCh5.setOnClickListener(onClickListener);
        rbCh6.setOnClickListener(onClickListener);
        rbCh7.setOnClickListener(onClickListener);
        rbCh8.setOnClickListener(onClickListener);

        btnSwitchCoef.setOnClickListener(onClickListener);


        btnFpgaStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        XDmaDevManage.getInstance().resume();
                        ScopeMessage.runResume();
                        Scope.getInstance().AdReset();
                        Scope.getInstance().setRun(true);
                        btnFpgaStart.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"  FPGA start OK !!!  ",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });
        btnFpgaStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Scope.getInstance().setRun(false);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        XDmaDevManage.getInstance().suspend();

                        btnFpgaStop.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"  FPGA sotp OK !!!  ",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });


        updateView();
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,eventUIObserver);
    }

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
//            Logger.d(TAG,"ch" + eventBase.getId());
            if(eventBase.getId() == EventFactory.EVENT_CHANNEL_VSCALE_USER
                || eventBase.getId() == EventFactory.EVENT_CHANNEL_VSCALE){
                updateView();
            }
        }
    };
    private void updateView(){
        CabteRegister cabteRegister = CabteRegister.getInstance();
        Channel channel = ChannelFactory.getDynamicChannel(getChIdx());
        if(channel != null) {

            int i = channel.getChId();
            int mask =  0xFFFF;

            int dwIdx = CabteRegister.getRatioIdx(channel.getResistanceType(),channel.getVScaleVal() / channel.getProbeRate());

            tvDcp1.setText("DCP1:" + Integer.toHexString(cabteRegister.getChCapacitanceHigh(i,dwIdx) & mask));

            BaseProbe baseProbe = channel.getProbe();
            if (baseProbe != null && baseProbe.isDa()) {
                tvDa.setText("DA:" + baseProbe.getDaValue());
            } else {
                tvDa.setText("不支持的探头");
            }
            int pgaVal = 0;
            int vScaleId = channel.getVScaleId();

            if(channel.getResistanceType() == Channel.RESISTANCE_50) {
                pgaVal = HwConfig.pga_mho_8_50O_v2[vScaleId];
            }else{
                pgaVal = HwConfig.pga_mho68_v2[vScaleId];
            }



            tvPga.setText("PGA:" + Integer.toHexString(pgaVal & 0xFF));
        }
    }
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            CabteRegister cabteRegister = CabteRegister.getInstance();
            BaseProbe baseProbe;
            Channel channel = ChannelFactory.getDynamicChannel(getChIdx());
            int vScaleId = channel.getVScaleId();
            int i = channel.getChId();
            int dwIdx = CabteRegister.getRatioIdx(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());
            boolean bSave = true;
            int vvv = 0;
            switch (v.getId()) {
                case R.id.dcp1add:
                    vvv = cabteRegister.getChCapacitanceHigh(i,dwIdx);
                    vvv += seekBar.getProgress();
                    cabteRegister.setChCapacitanceHigh(i,dwIdx,vvv);
                    bSave = false;
                    break;
                case R.id.dcp1sub:
                    vvv = cabteRegister.getChCapacitanceHigh(i,dwIdx);
                    vvv -= seekBar.getProgress();
                    cabteRegister.setChCapacitanceHigh(i,dwIdx,vvv);
                    bSave = false;
                    break;

                case R.id.daadd:

                    baseProbe = channel.getProbe();
                    if(baseProbe != null && baseProbe.isDa()){
                        int val = baseProbe.getDaValue() + seekBar.getProgress();
                        if(val > 0xFFFF) val = 0xFFFF;
                        baseProbe.setDaValue(val);
                        ProbeFactory.getInstance().setProbeDa(channel.getChId(),baseProbe.getSN(),val);
                    }
                    bSave = false;
                    break;
                case R.id.dasub:
                    baseProbe = channel.getProbe();
                    if(baseProbe != null && baseProbe.isDa()){
                        int val = baseProbe.getDaValue() - seekBar.getProgress();
                        if(val < 0) val = 0;
                        baseProbe.setDaValue(val);
                        ProbeFactory.getInstance().setProbeDa(channel.getChId(),baseProbe.getSN(),val);
                    }
                    bSave = false;
                    break;
                case R.id.switch_btn:
                    FPGA_CHAZHI_COEF.COEF_INDEX = FPGA_CHAZHI_COEF.COEF_INDEX == 0 ? 1 : 0;
                    Scope.getInstance().setRun(true);
                    bSave = false;
                    break;
                case R.id.pgaadd:

                        if (channel.getResistanceType() == Channel.RESISTANCE_50) {
                            HwConfig.pga_mho_8_50O_v2[vScaleId]++;
                            if (HwConfig.pga_mho_8_50O_v2[vScaleId] > 0x220) {
                                HwConfig.pga_mho_8_50O_v2[vScaleId] = 0x200;
                            }
                        } else {
                            HwConfig.pga_mho68_v2[vScaleId]++;
                            if (HwConfig.pga_mho68_v2[vScaleId] > 0x220) {
                                HwConfig.pga_mho68_v2[vScaleId] = 0x200;
                            }
                        }

                    break;
                case R.id.pgasub:

                        if (channel.getResistanceType() == Channel.RESISTANCE_50) {
                            HwConfig.pga_mho_8_50O_v2[vScaleId]--;
                            if (HwConfig.pga_mho_8_50O_v2[vScaleId] < 0x200) {
                                HwConfig.pga_mho_8_50O_v2[vScaleId] = 0x220;
                            }
                        } else {
                            HwConfig.pga_mho68_v2[vScaleId]--;
                            if (HwConfig.pga_mho68_v2[vScaleId] < 0x200) {
                                HwConfig.pga_mho68_v2[vScaleId] = 0x220;
                            }
                        }


                    break;

            }
            channel.setVScaleId(channel.getVScaleId(),true);
            updateView();
            if(bSave) {
                //cabteRegister.saveUserCalibrateParam();
            }
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
