package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.ScreenUtil;

import io.reactivex.rxjava3.functions.Consumer;


public class MainLayoutCenterSerialsWord extends RelativeLayout {
    private Context context;
    private MainLayoutCenterSerialsWordDetail s1Layout, s2Layout, s3Layout, s4Layout, s12Layout;//s12Layout代表所有总线，名字暂时不做调整
    private MainLayoutCenterSerialsWordDetail visibleLayout;
    private RadioButton serialsTitleS1, serialsTitleS2, serialsTitleS3, serialsTitleS4, serialsTitleS12;
    private RadioGroup radioGroup;

    private String[] tags = {"serialsWordS1", "serialsWordS2", "serialsWordS3", "serialsWordS4", "serialsWordS12"};
    private Fragment[] fragments = new Fragment[5];

    public MainLayoutCenterSerialsWord(Context context) {
        this(context, null, 0);
    }

    public MainLayoutCenterSerialsWord(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterSerialsWord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
//        initLayout();
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu);
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_SERIALSWORD).subscribe(consumerExternalKeysSerialsWord);
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);

        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maincenter_serialsword, this);

        radioGroup = (RadioGroup) findViewById(R.id.serialsWordTitleGroup);
        serialsTitleS1 = (RadioButton) findViewById(R.id.serialsWordTitleS1);
        serialsTitleS2 = (RadioButton) findViewById(R.id.serialsWordTitleS2);
        serialsTitleS3 = (RadioButton) findViewById(R.id.serialsWordTitleS3);
        serialsTitleS4 = (RadioButton) findViewById(R.id.serialsWordTitleS4);
        serialsTitleS12 = (RadioButton) findViewById(R.id.serialsWordTitleS12);
        serialsTitleS1.setOnClickListener(onClickListener);
        serialsTitleS2.setOnClickListener(onClickListener);
        serialsTitleS3.setOnClickListener(onClickListener);
        serialsTitleS4.setOnClickListener(onClickListener);
        serialsTitleS12.setOnClickListener(onClickListener);
    }

    public void setSavedInstanceState(Bundle savedInstanceState) {
        initLayout(savedInstanceState);
    }

    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag(tags[i]);
            }
        }
        s1Layout = fragments[0] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[0];
        s1Layout.setChType(ISerialsWord.TYPE_S1);
        s2Layout = fragments[1] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[1];
        s2Layout.setChType(ISerialsWord.TYPE_S2);
        s3Layout = fragments[2] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[2];
        s3Layout.setChType(ISerialsWord.TYPE_S3);
        s4Layout = fragments[3] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[3];
        s4Layout.setChType(ISerialsWord.TYPE_S4);
        s12Layout = fragments[4] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[4];
        s12Layout.setChType(ISerialsWord.TYPE_S12);
        if (savedInstanceState == null) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .add(R.id.serialsWordDetailLayout, s1Layout, tags[0])
                    .add(R.id.serialsWordDetailLayout, s2Layout, tags[1])
                    .add(R.id.serialsWordDetailLayout, s3Layout, tags[2])
                    .add(R.id.serialsWordDetailLayout, s4Layout, tags[3])
                    .add(R.id.serialsWordDetailLayout, s12Layout, tags[4])
                    .hide(s2Layout)
                    .hide(s3Layout)
                    .hide(s4Layout)
                    .hide(s12Layout)
                    .commitAllowingStateLoss();
        }
        visibleLayout = s1Layout;
    }

    private void setCache() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        int s1Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
        int s2Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
        int s3Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
        int s4Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
        String s1title = getResources().getString(R.string.serialsWordTitleS1);
        String s2title = getResources().getString(R.string.serialsWordTitleS2);
        String s3title = getResources().getString(R.string.serialsWordTitleS3);
        String s4title = getResources().getString(R.string.serialsWordTitleS4);
        if (s1Check) {
            switch (s1Index) {
                case RightLayoutSerials.SERIALS_UART:
                    s1title = "S1:UART";
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s1title = "S1:LIN";
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s1title = "S1:CAN";
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s1title = "S1:SPI";
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s1title = "S1:I2C";
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s1title = "S1:429";
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s1title = "S1:1553B";
                    break;
            }
        }
        if (s2Check) {
            switch (s2Index) {
                case RightLayoutSerials.SERIALS_UART:
                    s2title = "S2:UART";
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s2title = "S2:LIN";
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s2title = "S2:CAN";
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s2title = "S2:SPI";
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s2title = "S2:I2C";
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s2title = "S2:429";
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s2title = "S2:1553B";
                    break;
            }
        }
        if (s3Check) {
            switch (s3Index) {
                case RightLayoutSerials.SERIALS_UART:
                    s3title = "S3:UART";
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s3title = "S3:LIN";
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s3title = "S3:CAN";
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s3title = "S3:SPI";
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s3title = "S3:I2C";
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s3title = "S3:429";
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s3title = "S3:1553B";
                    break;
            }
        }
        if (s4Check) {
            switch (s4Index) {
                case RightLayoutSerials.SERIALS_UART:
                    s4title = "S4:UART";
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s4title = "S4:LIN";
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s4title = "S4:CAN";
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s4title = "S4:SPI";
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s4title = "S4:I2C";
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s4title = "S4:429";
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s4title = "S4:1553B";
                    break;
            }
        }
        serialsTitleS1.setText(s1title);
        s1Layout.setTitle(s1title);
        serialsTitleS2.setText(s2title);
        s2Layout.setTitle(s2title);
        serialsTitleS3.setText(s3title);
        s3Layout.setTitle(s3title);
        serialsTitleS4.setText(s4title);
        s4Layout.setTitle(s4title);
        setSerialsS12Title();
        int tab = CacheUtil.get().getInt(CacheUtil.SERIAL_TXT_CURRTAB);
        if (tab == ISerialsWord.TYPE_S1) {
            radioGroup.check(serialsTitleS1.getId());
            onClickTitle(serialsTitleS1.getId());
        } else if (tab == ISerialsWord.TYPE_S2) {
            radioGroup.check(serialsTitleS2.getId());
            onClickTitle(serialsTitleS2.getId());
        } else if (tab == ISerialsWord.TYPE_S3) {
            radioGroup.check(serialsTitleS3.getId());
            onClickTitle(serialsTitleS3.getId());
        } else if (tab == ISerialsWord.TYPE_S4) {
            radioGroup.check(serialsTitleS4.getId());
            onClickTitle(serialsTitleS4.getId());
        } else if (tab == ISerialsWord.TYPE_S12) {
            radioGroup.check(serialsTitleS12.getId());
            onClickTitle(serialsTitleS12.getId());
        } else {
            radioGroup.check(serialsTitleS1.getId());
            onClickTitle(serialsTitleS1.getId());
        }
        setTabSelect();
    }

    public int getShowTitle(){
        int r=-1;
        if (serialsTitleS1.isChecked()){
            r=0;
        }
        if (serialsTitleS2.isChecked()){
            r=1;
        }
        if (serialsTitleS3.isChecked()){
            r=2;
        }
        if (serialsTitleS4.isChecked()){
            r=3;
        }
        if (serialsTitleS12.isChecked()){
            r=4;
        }
        return r;

    }

    private Consumer<Integer> consumerExternalKeysSerialsWord = new Consumer<Integer>() {
        @Override
        public void accept(Integer msgSerialsWord) throws Exception {
            if (visibleLayout == s1Layout) {
                s1Layout.setScrollMove(msgSerialsWord);
            } else if (visibleLayout == s2Layout) {
                s2Layout.setScrollMove(msgSerialsWord);
            } else if (visibleLayout == s3Layout) {
                s3Layout.setScrollMove(msgSerialsWord);
            } else if (visibleLayout == s4Layout) {
                s4Layout.setScrollMove(msgSerialsWord);
            } else if (visibleLayout == s12Layout) {
                s12Layout.setScrollMove(msgSerialsWord);
            }
        }
    };

    private static final int MSG_RUN = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RUN:
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {
                        s1Layout.setRunStop(true);
                        s2Layout.setRunStop(true);
                        s3Layout.setRunStop(true);
                        s4Layout.setRunStop(true);
                        s12Layout.setRunStop(true);
                    }
                    handler.sendEmptyMessageDelayed(MSG_RUN, 250);
                    break;
            }
        }
    };

    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {
            boolean run = (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN);
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {
                s1Layout.setRunStop(run);
                s2Layout.setRunStop(run);
                s3Layout.setRunStop(run);
                s4Layout.setRunStop(run);
                s12Layout.setRunStop(run);
            }
            if (handler.hasMessages(MSG_RUN)) {
                handler.removeMessages(MSG_RUN);
            }
            if (run) {
                handler.sendEmptyMessage(MSG_RUN);
            }
        }
    };

    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {
                s1Layout.setRunStop(run);
                s2Layout.setRunStop(run);
                s3Layout.setRunStop(run);
                s4Layout.setRunStop(run);
                s12Layout.setRunStop(run);
            }
            if (handler.hasMessages(MSG_RUN)) {
                handler.removeMessages(MSG_RUN);
            }
            if (run) {
                handler.sendEmptyMessage(MSG_RUN);
            }
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            setCache();

            //获取通道是否打开
            boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
            //通道没打开不显示对应的tab title
            serialsTitleS1.setVisibility(s1Open ? View.VISIBLE : View.GONE);
            serialsTitleS2.setVisibility(s2Open ? View.VISIBLE : View.GONE);
            serialsTitleS3.setVisibility(s3Open ? View.VISIBLE : View.GONE);
            serialsTitleS4.setVisibility(s4Open ? View.VISIBLE : View.GONE);
            setTabSelect();
        }
    };

    private void setTabSelect() {//通道打开关闭 顺序选中最前面的tab
        int tab = CacheUtil.get().getInt(CacheUtil.SERIAL_TXT_CURRTAB);
        boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        int openNum = 0;
        if (s1Open) openNum++;
        if (s2Open) openNum++;
        if (s3Open) openNum++;
        if (s4Open) openNum++;
        if ((tab == ISerialsWord.TYPE_S1 && s1Open)
                || (tab == ISerialsWord.TYPE_S2 && s2Open)
                || (tab == ISerialsWord.TYPE_S3 && s3Open)
                || (tab == ISerialsWord.TYPE_S4 && s4Open)
                || (tab == ISerialsWord.TYPE_S12 && openNum >= 2 && serialsTitleS12.getVisibility() == View.VISIBLE))
        {
            //选中的跟当前显示的匹配，不做处理
        } else if (tab == ISerialsWord.TYPE_S12 && !(s1Open || s2Open || s3Open || s4Open)) {
          //s1/s2/s3/s4都没有打开，选中s12的时候
            radioGroup.check(serialsTitleS12.getId());
            onClickTitle(serialsTitleS12.getId());
        } else {//第一个位置tab更新为选中，优先级S1 > S2 > S3 > S4 > S1&S2&S3&S4
            if (s1Open) {
                radioGroup.check(serialsTitleS1.getId());
                onClickTitle(serialsTitleS1.getId());
            } else {
                if (s2Open) {
                    radioGroup.check(serialsTitleS2.getId());
                    onClickTitle(serialsTitleS2.getId());
                } else {
                    if (s3Open) {
                        radioGroup.check(serialsTitleS3.getId());
                        onClickTitle(serialsTitleS3.getId());
                    } else {
                        if (s4Open) {
                            radioGroup.check(serialsTitleS4.getId());
                            onClickTitle(serialsTitleS4.getId());
                        } else {
                            if (serialsTitleS12.getVisibility() == View.VISIBLE) {
                                radioGroup.check(serialsTitleS12.getId());
                                onClickTitle(serialsTitleS12.getId());
                            } else {
                                radioGroup.check(serialsTitleS1.getId());
                                onClickTitle(serialsTitleS1.getId());
                            }
                        }
                    }
                }
            }
        }

        //更新文本页面对应的旋钮坐标
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_SERIALS_DETAIL_VISIBLE, true);
    }

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            setCache();

            //获取通道是否打开
            boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
            //通道没打开不显示对应的tab title
            serialsTitleS1.setVisibility(s1Open ? View.VISIBLE : View.GONE);
            serialsTitleS2.setVisibility(s2Open ? View.VISIBLE : View.GONE);
            serialsTitleS3.setVisibility(s3Open ? View.VISIBLE : View.GONE);
            serialsTitleS4.setVisibility(s4Open ? View.VISIBLE : View.GONE);
            setTabSelect();
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            ScreenUtil.getViewLocation(v);
            onClickTitle(v.getId());
        }
    };

    private void onClickTitle(int viewId) {
        ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .hide(s1Layout)
                .hide(s2Layout)
                .hide(s3Layout)
                .hide(s4Layout)
                .hide(s12Layout)
                .commitAllowingStateLoss();
        if (viewId == R.id.serialsWordTitleS1) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .show(s1Layout).commitAllowingStateLoss();
            CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_CURRTAB, String.valueOf(ISerialsWord.TYPE_S1));
            visibleLayout = s1Layout;
        } else if (viewId == R.id.serialsWordTitleS2) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .show(s2Layout).commitAllowingStateLoss();
            CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_CURRTAB, String.valueOf(ISerialsWord.TYPE_S2));
            visibleLayout = s2Layout;
        } else if (viewId == R.id.serialsWordTitleS3) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .show(s3Layout).commitAllowingStateLoss();
            CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_CURRTAB, String.valueOf(ISerialsWord.TYPE_S3));
            visibleLayout = s3Layout;
        } else if (viewId == R.id.serialsWordTitleS4) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .show(s4Layout).commitAllowingStateLoss();
            CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_CURRTAB, String.valueOf(ISerialsWord.TYPE_S4));
            visibleLayout = s4Layout;
        } else if (viewId == R.id.serialsWordTitleS12) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .show(s12Layout).commitAllowingStateLoss();
            CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_CURRTAB, String.valueOf(ISerialsWord.TYPE_S12));
            visibleLayout = s12Layout;
        }
    }

    private Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) {
                //这里判断组合状态  S1&S2&S3&S4
                setSerialsS12Title();
            }
        }
    };

    private void setSerialsS12Title() {
        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);

        StringBuilder s12Title = new StringBuilder();
        if (s1Select) {
            s12Title.append("S1");
        }
        if (s2Select) {
            s12Title.append("&S2");
        }
        if (s3Select) {
            s12Title.append("&S3");
        }
        if (s4Select) {
            s12Title.append("&S4");
        }
        String finalTitle = s12Title.toString();
        if (finalTitle.startsWith("&")) {
            finalTitle = finalTitle.replaceFirst("&", "");
        }
        serialsTitleS12.setText(finalTitle);
        s12Layout.setTitle(finalTitle);
        //有组合才显示
        serialsTitleS12.setVisibility(finalTitle.contains("&") ? View.VISIBLE : View.GONE);
        setTabSelect();
    }

}
