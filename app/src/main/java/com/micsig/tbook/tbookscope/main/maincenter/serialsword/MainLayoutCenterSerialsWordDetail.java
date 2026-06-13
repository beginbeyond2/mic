package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.functions.Consumer;


public class MainLayoutCenterSerialsWordDetail extends Fragment {
    private MainLayoutCenterSerialsWordTip tipLayout;
    private MainLayoutCenterSerialsWordUart uartLayout;
    private MainLayoutCenterSerialsWordLin linLayout;
    private MainLayoutCenterSerialsWordCan canLayout;
    private MainLayoutCenterSerialsWordSpi spiLayout;
    private MainLayoutCenterSerialsWordI2c i2cLayout;
    private MainLayoutCenterSerialsWordM429 m429Layout;
    private MainLayoutCenterSerialsWordM1553b m1553bLayout;
    private Fragment visibleLayout;

    private String[] tags = {"serialsWordTipLayout", "serialsWordUartLayout"
            , "serialsWordLinLayout", "serialsWordCanLayout", "serialsWordSpiLayout"
            , "serialsWordI2cLayout", "serialsWordM429Layout", "serialsWordM1553bLayout"};
    private Fragment[] fragments = new Fragment[8];

    private int chType = ISerialsWord.TYPE_S1;
    private String title;
    private HashMap<String, Boolean> checkMap = new HashMap<>();
    private HashMap<String, Integer> indexMap = new HashMap<>();

    public void setChType(int chType) {
        this.chType = chType;
    }

    public void setTitle(String title) {
        this.title = title;
        if (uartLayout != null) {
            uartLayout.setTitle(title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initLayout(savedInstanceState);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerShowLayout);
    }

    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]);
            }
        }

        tipLayout = fragments[0] == null ? new MainLayoutCenterSerialsWordTip() : (MainLayoutCenterSerialsWordTip) fragments[0];
        tipLayout.setChType(chType);
        uartLayout = fragments[1] == null ? new MainLayoutCenterSerialsWordUart() : (MainLayoutCenterSerialsWordUart) fragments[1];
        uartLayout.setChType(chType);
        linLayout = fragments[2] == null ? new MainLayoutCenterSerialsWordLin() : (MainLayoutCenterSerialsWordLin) fragments[2];
        linLayout.setChType(chType);
        canLayout = fragments[3] == null ? new MainLayoutCenterSerialsWordCan() : (MainLayoutCenterSerialsWordCan) fragments[3];
        canLayout.setChType(chType);
        spiLayout = fragments[4] == null ? new MainLayoutCenterSerialsWordSpi() : (MainLayoutCenterSerialsWordSpi) fragments[4];
        spiLayout.setChType(chType);
        i2cLayout = fragments[5] == null ? new MainLayoutCenterSerialsWordI2c() : (MainLayoutCenterSerialsWordI2c) fragments[5];
        i2cLayout.setChType(chType);
        m429Layout = fragments[6] == null ? new MainLayoutCenterSerialsWordM429() : (MainLayoutCenterSerialsWordM429) fragments[6];
        m429Layout.setChType(chType);
        m1553bLayout = fragments[7] == null ? new MainLayoutCenterSerialsWordM1553b() : (MainLayoutCenterSerialsWordM1553b) fragments[7];
        m1553bLayout.setChType(chType);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.fragmentLayout, tipLayout, tags[0])
                    .add(R.id.fragmentLayout, uartLayout, tags[1])
                    .add(R.id.fragmentLayout, linLayout, tags[2])
                    .add(R.id.fragmentLayout, canLayout, tags[3])
                    .add(R.id.fragmentLayout, spiLayout, tags[4])
                    .add(R.id.fragmentLayout, i2cLayout, tags[5])
                    .add(R.id.fragmentLayout, m429Layout, tags[6])
                    .add(R.id.fragmentLayout, m1553bLayout, tags[7])
                    .hide(uartLayout)
                    .hide(linLayout)
                    .hide(canLayout)
                    .hide(spiLayout)
                    .hide(i2cLayout)
                    .hide(m429Layout)
                    .hide(m1553bLayout)
                    .commitAllowingStateLoss();
        }
        visibleLayout = tipLayout;
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
        checkMap.put("S1", s1Check);
        checkMap.put("S2", s2Check);
        checkMap.put("S3", s3Check);
        checkMap.put("S4", s4Check);
        indexMap.put("S1", s1Index);
        indexMap.put("S2", s2Index);
        indexMap.put("S3", s3Index);
        indexMap.put("S4", s4Index);
        boolean check = false;
        int index = 0;
        if (chType == ISerialsWord.TYPE_S1) {
            check = s1Check;
            index = s1Index;
        } else if (chType == ISerialsWord.TYPE_S2) {
            check = s2Check;
            index = s2Index;
        } else if (chType == ISerialsWord.TYPE_S3) {
            check = s3Check;
            index = s3Index;
        } else if (chType == ISerialsWord.TYPE_S4) {
            check = s4Check;
            index = s4Index;
        } else if (chType == ISerialsWord.TYPE_S12) {
            check = getS12Check();
            index = getS12Index();
            SerialBusManage.getInstance().getSerialTxtBuffer(ISerialsWord.TYPE_S12)
                    .setOpenS1S2(check, index + 1);
            if ( isSelectAndOpen() && check==false){
                tipLayout.setTip(R.string.serialsWordUartTip);
            }else {
                tipLayout.setTip(R.string.serialsWordTip);
            }
        }

        getChildFragmentManager().beginTransaction()
                .hide(tipLayout)
                .hide(uartLayout)
                .hide(linLayout)
                .hide(canLayout)
                .hide(spiLayout)
                .hide(i2cLayout)
                .hide(m429Layout)
                .hide(m1553bLayout)
                .commitAllowingStateLoss();
        if (check) {
            switch (index) {
                case RightLayoutSerials.SERIALS_UART:
                    getChildFragmentManager().beginTransaction().show(uartLayout).commitAllowingStateLoss();
                    visibleLayout = uartLayout;
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    getChildFragmentManager().beginTransaction().show(linLayout).commitAllowingStateLoss();
                    visibleLayout = linLayout;
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    getChildFragmentManager().beginTransaction().show(canLayout).commitAllowingStateLoss();
                    visibleLayout = canLayout;
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    getChildFragmentManager().beginTransaction().show(spiLayout).commitAllowingStateLoss();
                    visibleLayout = spiLayout;
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    getChildFragmentManager().beginTransaction().show(i2cLayout).commitAllowingStateLoss();
                    visibleLayout = i2cLayout;
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    getChildFragmentManager().beginTransaction().show(m429Layout).commitAllowingStateLoss();
                    visibleLayout = m429Layout;
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    getChildFragmentManager().beginTransaction().show(m1553bLayout).commitAllowingStateLoss();
                    visibleLayout = m1553bLayout;
                    break;
                case -1:
                    getChildFragmentManager().beginTransaction().show(tipLayout).commitAllowingStateLoss();
                    visibleLayout = tipLayout;
                    break;
            }
        } else {
            getChildFragmentManager().beginTransaction().show(tipLayout).commitAllowingStateLoss();
            visibleLayout = tipLayout;
        }
    }

    private boolean isSelectAndOpen(){
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);
        boolean[] select = {s1Select, s2Select, s3Select, s4Select};
        boolean[] check = {s1Check, s2Check, s3Check, s4Check};

        for(int i=0;i<select.length;i++){
            if (select[i] && check[i]==false){
                return false;
            }
        }
        return true;
    }
    private boolean getS12Check() {
        String[] titles = title.split("&");
        boolean finalCheck = true;
        boolean finalIndex = true;
        String preKey = "";
        String nowKey = "";
        for (String s : titles) {
            if (checkMap.containsKey(s)) {
                finalCheck = finalCheck && Boolean.TRUE.equals(checkMap.get(s));
            }
            if (indexMap.containsKey(s)) {
                preKey = nowKey;
                nowKey = s;
                if(preKey.isEmpty()) continue;
                finalIndex = finalIndex && (Objects.equals(indexMap.get(preKey), indexMap.get(nowKey)));
            }
        }
        return finalCheck && finalIndex;
    }

    private int getS12Index() {
        String[] titles = title.split("&");
        int finalIndex = -1;
        for (String s : titles) {
            if (indexMap.containsKey(s) && indexMap.get(s) != null) {
                finalIndex = indexMap.get(s);
                break;

            }
        }
        return finalIndex;
    }

    public void setScrollMove(int moveCount) {
        if (visibleLayout == uartLayout) {
            uartLayout.setScrollMove(moveCount);
        } else if (visibleLayout == linLayout) {
            linLayout.setScrollMove(moveCount);
        } else if (visibleLayout == canLayout) {
            canLayout.setScrollMove(moveCount);
        } else if (visibleLayout == spiLayout) {
            spiLayout.setScrollMove(moveCount);
        } else if (visibleLayout == i2cLayout) {
            i2cLayout.setScrollMove(moveCount);
        } else if (visibleLayout == m429Layout) {
            m429Layout.setScrollMove(moveCount);
        } else if (visibleLayout == m1553bLayout) {
            m1553bLayout.setScrollMove(moveCount);
        }
    }

    public void setRunStop(boolean run) {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        int s1Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
        int s2Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
        int s3Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
        int s4Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
        boolean check = false;
        int index = 0;
        if (chType == ISerialsWord.TYPE_S1) {
            check = s1Check;
            index = s1Index;
        } else if (chType == ISerialsWord.TYPE_S2) {
            check = s2Check;
            index = s2Index;
        } else if (chType == ISerialsWord.TYPE_S3) {
            check = s3Check;
            index = s3Index;
        } else if (chType == ISerialsWord.TYPE_S4) {
            check = s4Check;
            index = s4Index;
        } else if (chType == ISerialsWord.TYPE_S12) {
            check = getS12Check();
            index = getS12Index();
        }
//        Logger.i("MainlayoutCenterSerialsWordDetail:setRunStop" + "chType:" + chType + " check:" + check+" index:"+index);
        if (check) {
            switch (index) {
                case RightLayoutSerials.SERIALS_UART:
                    uartLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    linLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    canLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    spiLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    i2cLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    m429Layout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    m1553bLayout.setRunStop(run);
                    break;
            }
        }
    }

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
        }
    };

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            if ((rightMsgSerials.isSerials1() && chType == ISerialsWord.TYPE_S1)
                    || (rightMsgSerials.isSerials2() && chType == ISerialsWord.TYPE_S2)
                    || (rightMsgSerials.isSerials3() && chType == ISerialsWord.TYPE_S3)
                    || (rightMsgSerials.isSerials4() && chType == ISerialsWord.TYPE_S4)
                    || chType == ISerialsWord.TYPE_S12) {
                setCache();
            }
        }
    };

    private Consumer<TopMsgDisplay> consumerShowLayout = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) {
                setCache();
            }
        }
    };
}
