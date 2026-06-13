package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutUserset extends Fragment {

    public static final int DETAIL_FACTORYRESET = 0;
    public static final int DETAIL_SELFADJUST = 1;
    public static final int DETAIL_WIRELESSKEYBOARD= 2;
    public static final int DETAIL_USERSETAUXOUT = 3;
    public static final int DETAIL_REF_TIMEBASE = 4;

    private Context context;
    private TopViewTitleWithScroll settingTitle;
    private RelativeLayout settingDetail;

    private TopLayoutUsersetFactoryReset layoutFactoryReset;    //出厂设置
    private TopLayoutUsersetSelfAdjust layoutSelfAdjust;        //自动校准
    private TopLayoutUsersetWirelessKeyBoard layoutWireless;    //无线键盘
    private TopLayoutUserSetAuxOut layoutAuxOut;                //触发/时钟输入输出
    private TopLayoutUserSetRefTimeBase layoutRefTimeBase;      //Ref时基调节



    private TopMsgUserset msgUserset;

    private String[] tags = {"FactoryReset", "SelfAdjust", "WirelessKeyboard", "UserSetAuxOut", "RefTimeBase"};
    private Fragment[] fragments = new Fragment[5];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_userset, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view, savedInstanceState);
        initControl();
    }

    private void initView(View view, Bundle savedInstanceState) {
        settingDetail = (RelativeLayout) view.findViewById(R.id.settingDetail);
        settingTitle = (TopViewTitleWithScroll) view.findViewById(R.id.settingTitle);

        String[] array = context.getResources().getStringArray(R.array.setting);
        boolean[] settingVisible = new boolean[array.length];
        settingVisible[0] = true;
        settingVisible[1] = true;
        settingVisible[2] = Build.MODEL.equalsIgnoreCase("ETO");
        settingVisible[3] = true;
        settingVisible[4] = true;
        settingTitle.setData(array, settingVisible, onCheckChangedTitleListener, onItemClickListener);

        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]);
            }
        }

        layoutFactoryReset = fragments[0] == null ? new TopLayoutUsersetFactoryReset() : (TopLayoutUsersetFactoryReset) fragments[0];
        layoutSelfAdjust = fragments[1] == null ? new TopLayoutUsersetSelfAdjust() : (TopLayoutUsersetSelfAdjust) fragments[1];
        layoutWireless = fragments[2] == null ? new TopLayoutUsersetWirelessKeyBoard() : (TopLayoutUsersetWirelessKeyBoard) fragments[2];
        layoutAuxOut = fragments[3] == null ? new TopLayoutUserSetAuxOut() : (TopLayoutUserSetAuxOut) fragments[3];
        layoutRefTimeBase = fragments[4] == null ? new TopLayoutUserSetRefTimeBase() : (TopLayoutUserSetRefTimeBase) fragments[4];
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()

                    .add(R.id.settingDetail, layoutFactoryReset, tags[0])
                    .add(R.id.settingDetail, layoutSelfAdjust, tags[1])
                    .add(R.id.settingDetail, layoutWireless, tags[2])
                    .add(R.id.settingDetail, layoutAuxOut, tags[3])
                    .add(R.id.settingDetail, layoutRefTimeBase, tags[4])
                    .hide(layoutFactoryReset)
                    .hide(layoutSelfAdjust)
                    .hide(layoutWireless)
                    .hide(layoutAuxOut)
                    .hide(layoutRefTimeBase)
                    .commitAllowingStateLoss();
        }

        msgUserset = new TopMsgUserset();
        msgUserset.setUsersetTitle(settingTitle.getSelected());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState);
    }

    private void setCache() {
        int userset = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET);
        settingTitle.setSelected(userset);
        onCheckChangedTitleListener.checkChanged(settingTitle, settingTitle.getSelected());
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_USERSET, msgUserset);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutUserset, true);
        }
    };

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
        }
    };

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET, String.valueOf(item.getIndex()));
            getChildFragmentManager().beginTransaction()
                    .hide(layoutFactoryReset)
                    .hide(layoutSelfAdjust)
                    .hide(layoutWireless)
                    .hide(layoutAuxOut)
                    .hide(layoutRefTimeBase)
                    .commitAllowingStateLoss();
            switch (item.getIndex()) {
                case DETAIL_FACTORYRESET:
                    getChildFragmentManager().beginTransaction()
                            .show(layoutFactoryReset).commitAllowingStateLoss();
                    msgUserset.setUsersetTitle(item);
                    sendMsg();
                    break;
                case DETAIL_SELFADJUST:
                    getChildFragmentManager().beginTransaction()
                            .show(layoutSelfAdjust).commitAllowingStateLoss();
                    msgUserset.setUsersetTitle(item);
                    sendMsg();
                    break;
                case DETAIL_WIRELESSKEYBOARD:
                    getChildFragmentManager().beginTransaction()
                            .show(layoutWireless).commitAllowingStateLoss();
                    msgUserset.setUsersetTitle(item);
                    sendMsg();
                    break;
                case DETAIL_USERSETAUXOUT:
                    getChildFragmentManager().beginTransaction()
                            .show(layoutAuxOut).commitAllowingStateLoss();
                    msgUserset.setUsersetTitle(item);
                    sendMsg();
                    break;
                case DETAIL_REF_TIMEBASE:
                    getChildFragmentManager().beginTransaction()
                            .show(layoutRefTimeBase).commitAllowingStateLoss();
                    msgUserset.setUsersetTitle(item);
                    sendMsg();
                    break;
            }
        }
    };

    public int getUserIdx(){
        return settingTitle.getSelected().getIndex();
    }

    private Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean setState) throws Throwable {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (settingTitle.getSelected().getIndex() == DETAIL_SELFADJUST) {
                        settingTitle.setSelected(DETAIL_FACTORYRESET);
                    }
                    settingTitle.setEnable(DETAIL_SELFADJUST, setState);
                }
            });
        }
    };
}
