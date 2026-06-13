package com.micsig.tbook.tbookscope.top.layout.cursor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.MMainMenuChannel;
import com.micsig.tbook.ui.top.view.channel.TopViewChannel;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2023-9-6 9:01
 */
public class TopLayoutCursorCommon extends Fragment {

    private static final String TAG = "TopLayoutCursorCommon";
    private Context context;
    private MMainMenuChannel viewChannel;

    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //default
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            true
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_cursor_common, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControls();
    }
    private void initView(View view){
        initChannelView(view);
    }
    private void initChannelView(View view){
        viewChannel =  view.findViewById(R.id.chanCursorCommon);
        viewChannel.setData(R.array.popCursorArrayChannel, R.array.popCursorArrayChannelColor);
        viewChannel.setChangeListener(onChannelItemClickListener, null);
    }

    private void initControls(){
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
    }


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true);
        }
    };
    private void setCache(){
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);
        viewChannel.setChecked(sourceIdx);
        setSource(sourceIdx);
    }

    private void setSource(int sourceIndex){
        int ch = sourceIndex;
        if (sourceIndex == 24) {
            ch = ChannelFactory.getChActivate();
            if (!ChannelFactory.isChOpen(ch)) {
                ch = -1;
            }
        }
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(sourceIndex));
        CursorManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch));
        MeasureManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch));
    }

    private TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() {
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) {
            PlaySound.getInstance().playButton();
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(checkedIndex));
            setSource(checkedIndex);
        }
    };
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception {
            TChan.foreachChan(chan -> {
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan);
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue();
            });
            setChannelShow();
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            TChan.foreachMath(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            TChan.foreachRef(chan -> {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
            });
            setChannelShow();
        }
    };

    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception {
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber());
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser;
            setChannelShow();
        }
    };

    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow,false);

        setSource(viewChannel.getSelectedIndex());
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL);
    }


    public IMeasureDetail getCursorDetail() {
        return null;
    }
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }


    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            viewChannel.setChannelColor(chIndex, colorStr);
        }
    };
}
