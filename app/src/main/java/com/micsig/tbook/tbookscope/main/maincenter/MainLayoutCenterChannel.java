package com.micsig.tbook.tbookscope.main.maincenter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.MiddleMain;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ControlBean;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MRadioGroup;
import com.micsig.tbook.ui.bean.RadioButtonBean;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by yangj on 2017/5/10.
 * 名子叫refresh开始的，里面都不能有消息发送，只更新界面UI
 *
 */

public class MainLayoutCenterChannel extends RelativeLayout {

//    public static final int CH1 = MainCenterMsgChannels.CH1;
//    public static final int CH2 = MainCenterMsgChannels.CH2;
//    public static final int CH3 = MainCenterMsgChannels.CH3;
//    public static final int CH4 = MainCenterMsgChannels.CH4;
//    public static final int MATH = MainCenterMsgChannels.MATH;
//    public static final int REF1 = MainCenterMsgChannels.REF1;
//    public static final int REF2 = MainCenterMsgChannels.REF2;
//    public static final int REF3 = MainCenterMsgChannels.REF3;
//    public static final int REF4 = MainCenterMsgChannels.REF4;

    private Context context;

    private MRadioGroup mRadioGroup;
//    private RadioGroup group;
//    private RadioButton rbCh1, rbCh2, rbCh3, rbCh4, rbMath, rbR1, rbR2, rbR3, rbR4, rbS1, rbS2;
    /**
     * 每一个分别代表一个通道的显现性，通道顺序是：
     * Ch1--Ch8
     * Math1--Math8
     * R1--R8
     * S1--S4
     */
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false
    };

    private ViewGroup rootViewGroup;

    public MainLayoutCenterChannel(Context context) {
        this(context, null);
    }

    public MainLayoutCenterChannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainLayoutCenterChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        channelShow=new boolean[IChan.RefActive.getValue()];
        Arrays.fill(channelShow,false);

        rootViewGroup= (ViewGroup) View.inflate(context, R.layout.layout_maincenter_channel, this);

        mRadioGroup=findViewById(R.id.chanGroup);
        String[] arrays= context.getResources().getStringArray(R.array.popArrayAllChannel);

        int[] arraysColor= new int[28];
        System.arraycopy(SvgNodeInfo.getColorsIntForCenterChView(), 0, arraysColor, 0, 28);
//                context.getResources().getIntArray(R.array.popArrayAllChanColor);

        mRadioGroup.initData(null,arrays,arraysColor,this::onIndexChange);


//        group = (RadioGroup) findViewById(R.id.groupCenterChannel);
//        rbCh1 = (RadioButton) findViewById(R.id.channelsCh1);
//        rbCh2 = (RadioButton) findViewById(R.id.channelsCh2);
//        rbCh3 = (RadioButton) findViewById(R.id.channelsCh3);
//        rbCh4 = (RadioButton) findViewById(R.id.channelsCh4);
//        rbMath = (RadioButton) findViewById(R.id.channelsMath);
//        rbR1 = (RadioButton) findViewById(R.id.channelsR1);
//        rbR2 = (RadioButton) findViewById(R.id.channelsR2);
//        rbR3 = (RadioButton) findViewById(R.id.channelsR3);
//        rbR4 = (RadioButton) findViewById(R.id.channelsR4);
//        rbS1 = (RadioButton) findViewById(R.id.channelsS1);
//        rbS2 = (RadioButton) findViewById(R.id.channelsS2);
        setBackgroundResource(R.drawable.shape_frame_bg_black);
        setClickable(true);
        initControl();
//        initRadioButton();
//        group.clearCheck();
//        rbCh1.setOnClickListener(onClickListener);
//        rbCh2.setOnClickListener(onClickListener);
//        rbCh3.setOnClickListener(onClickListener);
//        rbCh4.setOnClickListener(onClickListener);
//        rbMath.setOnClickListener(onClickListener);
//        rbR1.setOnClickListener(onClickListener);
//        rbR2.setOnClickListener(onClickListener);
//        rbR3.setOnClickListener(onClickListener);
//        rbR4.setOnClickListener(onClickListener);
//        rbS1.setOnClickListener(onClickListener);
//        rbS2.setOnClickListener(onClickListener);
    }
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_RECOVERY_SELECT).subscribe(consumerRecoverySelect);

        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::onChanStateChange);
    }

    private void onChanStateChange(Object obj) {
        MQEnum type = RxBusRegister.parseMqEnum(obj);
        if (type == MQEnum.CH_ACTIVE) {
            channelShow = MiddleMain.getIns().getChanSelectorManage().refreshChannelShow();
            IChan chan = MiddleMain.getIns().getChanSelectorManage().getActivityChannel();
//            Log.d(Tag.Debug, String.format("MainLayoutCenterChannel.onChanStateChange: %s", chan));
            if (chan != IChan.CH_NULL) {
                if (!(IChan.isCh1ToMath(chan))) {
                    TriggerTimebase.getInstance().drawActiveTrigTime(false);
                }
                refreshRadioGroupCheck(chan.getValue());
                CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(chan.getValue()));
            }

        } else if (type == MQEnum.CH_OPEN || type == MQEnum.CH_CLOSE) {
            channelShow = MiddleMain.getIns().getChanSelectorManage().refreshChannelShow();
            refreshMRadioGroupLayout();
            refreshRadioGroup(false);
        }
    }

    private void onIndexChange(RadioButtonBean radioButtonBean) {
        setExternalKeyBackStateFocus();
//        setChannels(false);
        IChan chan=IChan.toIChan(radioButtonBean.getIndex());
        MiddleMain.getIns().getChanSelectorManage().setActivityChannel(chan);

    }

    public List<ControlBean> getChanRect(){
        return getChanRect(rootViewGroup);
    }
    private List<ControlBean> getChanRect(ViewGroup viewGroup){
        List<ControlBean> list=new ArrayList<>();
        int viewCount=viewGroup.getChildCount();
        for(int i=0;i<viewCount;i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof RecyclerView) {
                int count= ((RecyclerView)view).getAdapter().getItemCount();
                for(int j=0;j<count;j++){
                    View v= ((RecyclerView)view).getLayoutManager().findViewByPosition(j);
                    if (v==null) {
                        list.add(new ControlBean("",false,new Rect(0,0,0,0)));
                        continue;
                    }
                    RadioButton r= v.findViewById(R.id.textView);
                    boolean visible=true;
                    if (r.getWidth()==0 || r.getHeight()==0) {
                        visible=false;
                    }
                    String name=r.getText().toString();
                    Rect rect= Tools.getViewRect(r);
                    list.add(new ControlBean(name,visible,rect));
                }
            }else if (view instanceof ViewGroup){
                list.addAll(getChanRect((ViewGroup) view));
            }
        }

//        Log.d("Tag.Debug", String.format("-------------" ));
//        Log.d("Tag.Debug", String.format("MainLayoutCenterChannel.getChanRect: %d",list.size() ));
//        for(int i=0;i<list.size();i++){
//            Log.d("Tag.Debug", String.format("MainLayoutCenterChannel.getChanRect: %s",list.get(i).toString() ));
//        }
        return list;
    }
//    private void initRadioButton() {
//        for (int i = 0; i < group.getChildCount(); i++) {
//            group.getChildAt(i).setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PlaySound.getInstance().playButton();
//                }
//            });
//        }
//    }


    private void refreshRadioGroup(boolean isFromEventBus) {
        boolean changedSelect = false;
        int j = 0;//此次改变之后的可点击的通道个数
//        Log.d("Tag.Debug", String.format("MainLayoutCenterChannel.setChannels: %s", Arrays.toString(channelShow) ));
//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        for (int i = 0; i < channelShow.length; i++) {
            if (channelShow[i]) {
//                group.getChildAt(i).setVisibility(VISIBLE);
                mRadioGroup.setItemVisible(i,View.VISIBLE);
                j++;
            } else {
                mRadioGroup.setItemVisible(i,View.GONE);
                if (mRadioGroup.getChecked(i)){
                    changedSelect=true;
                }
//                group.getChildAt(i).setVisibility(GONE);
//                if (((RadioButton) group.getChildAt(i)).isChecked()) {
//                    changedSelect = true;
//                }
            }
        }

        if (changedSelect || isAllUnCheck()) {
            if (checkHistoryList.size() != 0) {
                refreshRadioGroupCheck(checkHistoryList.get(checkHistoryList.size()-1));
//                groupCheck(checkHistoryList.get(checkHistoryList.size() - 1));
            } else {
                int idx= mRadioGroup.getFirstVisibleIdx();
                refreshRadioGroupCheck(idx);
//                for (int i = 0; i < group.getChildCount(); i++) {
//                    if (group.getChildAt(i).getVisibility() == VISIBLE) {
//                        groupCheck(group.getChildAt(i).getId());
//                        break;
//                    }
//                }
            }
        }
//        Logger.i("MainLayoutCenterChannel,setChannels() jCnt:" + j);
        setDialogVisible(visibility);
        if (j == 0) {
            if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
                CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(MainCenterMsgChannels.CH_NULL));
            }
//            RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(MainCenterMsgChannels.CH_NULL, true, isFromEventBus));
        } else {
            int channelSelectIndex = getChannelSelectIndex();

            if (channelSelectIndex >= 0) {
//                RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(channelSelectIndex + 1, true, isFromEventBus));
                ChannelFactory.setRefActive(channelSelectIndex);
            }
        }
        //TODO 切换为xy模式的时候，如果math、ref通道打开，则需要关闭，但此时状态为xy模式，则该设置没生效
        TChan.foreachAllChan((chan)->{
            if (TChan.isRef(chan)) {//0011030
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean addByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && addByUser;
            }
            WaveManage.get().setVisible(chan, channelShow[TChan.toFpgaChNo(chan)]);
        });
//        WaveManage.get().setVisible(IWave.Ch1, channelShow[IWave.Ch1-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.Ch2, channelShow[IWave.Ch2-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.Ch3, channelShow[IWave.Ch3-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.Ch4, channelShow[IWave.Ch4-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.Math1, channelShow[IWave.Math1-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.R1, channelShow[IWave.R1-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.R2, channelShow[IWave.R2-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.R3, channelShow[IWave.R3-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.R4, channelShow[IWave.R4-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.S1, channelShow[IWave.S1-IWave.Ch1]);
//        WaveManage.get().setVisible(IWave.S2, channelShow[IWave.S2-IWave.Ch1]);
    }

    private boolean isAllUnCheck() {
          return   mRadioGroup.isAllUnCheck();
//        for (int i = 0; i < group.getChildCount(); i++) {
//            if (((RadioButton) group.getChildAt(i)).isChecked()) {
//                return false;
//            }
//        }
//        return true;
    }

    private void refreshMRadioGroupLayout(){
        int showCount=getCurChannelShowCount();
        if (showCount<mRadioGroup.getInitSpanSize() && showCount>=2){
            mRadioGroup.setSpanSize(showCount);
        }else{
            mRadioGroup.setSpanSize(mRadioGroup.getInitSpanSize());
        }
    }

    private void setCache() {
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1);
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2);
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3);
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4);
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5);
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6);
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7);
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8);
        int channelsSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
        } else {
            channelShow[0] = ch1;
            channelShow[1] = ch2;
            channelShow[2] = ch3;
            channelShow[3] = ch4;
            channelShow[4] = ch5;
            channelShow[5] = ch6;
            channelShow[6] = ch7;
            channelShow[7] = ch8;
        }

        TChan.foreachCh1ToR8(chan -> {
            if (TChan.isMath(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
                boolean addByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && addByUser;
            }
            if (TChan.isRef(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
                boolean addByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && addByUser;
            }
            if (TChan.isSerial(chan)) {
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + chan);
                boolean addByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(chan));
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && addByUser;
            }
        });

        checkHistoryList.clear();
        setChannelSelectIndex(channelsSelect,false);
        refreshRadioGroup(false);
    }

    private ArrayList<Integer> checkHistoryList = new ArrayList<>();


    //刷新，不能有发送事件的东西，有发送事件就会循环
    private void refreshRadioGroupCheck(int index){
        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
            if (CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY) != index) {
                CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(index));
            }
        }
        mRadioGroup.setSelectIndex(index);
        if (checkHistoryList.contains(index)) {
            groupRemove((Integer) index);
        }
        checkHistoryList.add(index);

    }

//    private void groupCheck(int viewId) {
//        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {
//            int index = 0;
//            for (int i = 0; i < group.getChildCount(); i++) {
//                if (group.getChildAt(i).getId() == viewId) {
//                    index = i;
//                    break;
//                }
//            }
//            if (CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY) != index) {
//                CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(index));
//            }
//        }
//        if (rbCh1.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbCh1");
//        } else if (rbCh2.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbCh2");
//        } else if (rbCh3.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbCh3");
//        } else if (rbCh4.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbCh4");
//        } else if (rbMath.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbMath");
//        } else if (rbR1.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbR1");
//        } else if (rbR2.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbR2");
//        } else if (rbR3.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbR3");
//        } else if (rbR4.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbR4");
//        } else if (rbS1.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbS1");
//        } else if (rbS2.getId() == viewId) {
//            Logger.d(TAG, "groupCheck() called with: view = rbS2");
//        }
//        group.check(viewId);
//        if (checkHistoryList.contains(viewId)) {
//            groupRemove((Integer) viewId);
//        }
//        checkHistoryList.add(viewId);
//    }

    private void groupRemove(Integer viewId) {
        checkHistoryList.remove((Integer) viewId);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainLayoutCenterChannel, true);
        }
    };
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_CURRENT:
                case CommandMsgToUI.FLAG_REF_Current_Channel: {
                    int curChannel = Integer.parseInt(commandMsgToUI.getParam());
                    mRadioGroup.setSelectIndex(curChannel);
                    mRadioGroup.OnClick(curChannel);


                }
                break;

            }
        }
    };
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            if (workModeBean.getNextWorkMode() != IWorkMode.WorkMode_XY) {
                int index = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY);

                if (index != MainCenterMsgChannels.CH_NULL) {
                    new Handler().postDelayed(()->{
//                        setRadioGroupCheck(index);
                        setChannelSelectIndex(index,workModeBean.isFromEventBus());
//                        groupCheck(group.getChildAt(index).getId());
                        refreshRadioGroup(workModeBean.isFromEventBus());
                    },500);
                }
            }

        }
    };


    private Consumer<Integer> consumerRecoverySelect = new Consumer<Integer>() {
        @Override
        public void accept(Integer recoverySelect) throws Throwable {
            if (recoverySelect == null) return;

            new Handler().postDelayed(()->{
                setChannelSelectIndex(recoverySelect,false);
                refreshRadioGroup(false);
            },520);//520 比 consumerWorkModeChange的延迟时间大一点就行
        }
    };


    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() {
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception {
            if (!msgChannels.isChangeChState()
                    || msgChannels.isFromEventBus()) return;
            TChan.foreachChan(chan -> {
                if(msgChannels.getCh(TChan.toFpgaChNo(chan)).isRxMsgSelect()) {
                    setChannelSelectIndex(TChan.toFpgaChNo(chan),msgChannels.isFromEventBus());
                    refreshRadioGroup(msgChannels.isFromEventBus());
                }
            });
//            if (!msgChannels.isChangeChState()) return;
//
//            boolean ch1Select = msgChannels.getCh1().isRxMsgSelect();
//            boolean ch2Select = msgChannels.getCh2().isRxMsgSelect();
//            boolean ch3Select = msgChannels.getCh3().isRxMsgSelect();
//            boolean ch4Select = msgChannels.getCh4().isRxMsgSelect();
////            Log.d("Tag.Debug", String.format("MainLayoutCenterChannel.accept: %b,%b,%b,%b",ch1Select,ch2Select,ch3Select,ch4Select ));
////            Log.d("Tag.Debug", String.format("MainLayoutCenterChannel.accept: %s", Arrays.toString(channelShow) ));
//            if (!msgChannels.isChangeChState()) {
//                if (msgChannels.isFromEventBus()) {
//                    return;
//                }
//                ch1Select = msgChannels.isCh1ScaleChange();
//                ch2Select = msgChannels.isCh2ScaleChange();
//                ch3Select = msgChannels.isCh3ScaleChange();
//                ch4Select = msgChannels.isCh4ScaleChange();
//            } else if (channelShow[0] == msgChannels.getCh1().isValue()
//                    && channelShow[1] == msgChannels.getCh2().isValue()
//                    && channelShow[2] == msgChannels.getCh3().isValue()
//                    && channelShow[3] == msgChannels.getCh4().isValue()
//                    && ((group.getCheckedRadioButtonId() == rbCh1.getId() && ch1Select)
//                    || (group.getCheckedRadioButtonId() == rbCh2.getId() && ch2Select)
//                    || (group.getCheckedRadioButtonId() == rbCh3.getId() && ch3Select)
//                    || (group.getCheckedRadioButtonId() == rbCh4.getId() && ch4Select))) {
//                //当前4个通道的打开状态一样，且当前的处于运行状态的通道和即将设置的通道也一样，就直接返回
//                return;
//            }
//            channelShow[0] = msgChannels.getCh1().isValue();
//            channelShow[1] = msgChannels.getCh2().isValue();
//            channelShow[2] = msgChannels.getCh3().isValue();
//            channelShow[3] = msgChannels.getCh4().isValue();
//            if (ch1Select) {
//                if (msgChannels.getCh1().isValue()) {
//                    groupCheck(rbCh1.getId());
//                } else {
//                    groupRemove(((Integer) rbCh1.getId()));
//                }
//            } else if (ch2Select) {
//                if (msgChannels.getCh2().isValue()) {
//                    groupCheck(rbCh2.getId());
//                } else {
//                    groupRemove(((Integer) rbCh2.getId()));
//                }
//            } else if (ch3Select) {
//                if (msgChannels.getCh3().isValue()) {
//                    groupCheck(rbCh3.getId());
//                } else {
//                    groupRemove(((Integer) rbCh3.getId()));
//                }
//            } else if (ch4Select) {
//                if (msgChannels.getCh4().isValue()) {
//                    groupCheck(rbCh4.getId());
//                } else {
//                    groupRemove(((Integer) rbCh4.getId()));
//                }
//            }
//            setChannels(msgChannels.isFromEventBus());
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
//            boolean math = msgOthers.getMath().isValue();
//            boolean ref = msgOthers.getRef().isValue();
//            boolean s1 = msgOthers.getS1().isValue();
//            boolean s2 = msgOthers.getS2().isValue();
//
//            String closeSave = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE);
//            boolean r1 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_R1_CHECK) || closeSave.contains("1");
//            boolean r2 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_R2_CHECK) || closeSave.contains("2");
//            boolean r3 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_R3_CHECK) || closeSave.contains("3");
//            boolean r4 = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_R4_CHECK) || closeSave.contains("4");
//
//            boolean mathSelect = msgOthers.getMath().isRxMsgSelect();
//            boolean refSelect = msgOthers.getRef().isRxMsgSelect();
//            boolean s1Select = msgOthers.getS1().isRxMsgSelect();
//            boolean s2Select = msgOthers.getS2().isRxMsgSelect();
//
//            if (channelShow[4] == math
//                    && channelShow[5] == (ref && r1)
//                    && channelShow[6] == (ref && r2)
//                    && channelShow[7] == (ref && r3)
//                    && channelShow[8] == (ref && r4)
//                    && channelShow[9] == s1
//                    && channelShow[10] == s2
//                    && ((group.getCheckedRadioButtonId() == rbMath.getId() && mathSelect)
//                    || (group.getCheckedRadioButtonId() == rbS1.getId() && s1Select)
//                    || (group.getCheckedRadioButtonId() == rbS2.getId() && s2Select)
//                    || ((group.getCheckedRadioButtonId() == rbR1.getId()
//                    || group.getCheckedRadioButtonId() == rbR2.getId()
//                    || group.getCheckedRadioButtonId() == rbR3.getId()
//                    || group.getCheckedRadioButtonId() == rbR4.getId()) && refSelect))) {
//                return;
//            }
//            channelShow[4] = math;
//            channelShow[5] = ref && r1;
//            channelShow[6] = ref && r2;
//            channelShow[7] = ref && r3;
//            channelShow[8] = ref && r4;
//            channelShow[9] = s1;
//            channelShow[10] = s2;
//            if (msgOthers.getMath().isRxMsgSelect()) {
//                if (msgOthers.getMath().isValue()) {
//                    groupCheck(rbMath.getId());
//                } else {
//                    groupRemove(((Integer) rbMath.getId()));
//                }
//            } else if (msgOthers.getRef().isRxMsgSelect()) {
//                if (msgOthers.getRef().isValue() && (r1 || r2 || r3 || r4)) {
//                    if (r1) {
//                        groupCheck(rbR1.getId());
//                    } else if (r2) {
//                        groupCheck(rbR2.getId());
//                    } else if (r3) {
//                        groupCheck(rbR3.getId());
//                    } else {
//                        groupCheck(rbR4.getId());
//                    }
//                } else {
//                    groupRemove(((Integer) rbR1.getId()));
//                    groupRemove(((Integer) rbR2.getId()));
//                    groupRemove(((Integer) rbR3.getId()));
//                    groupRemove(((Integer) rbR4.getId()));
//                }
//            } else if (msgOthers.getS1().isRxMsgSelect()) {
//                if (msgOthers.getS1().isValue()) {
//                    groupCheck(rbS1.getId());
//                } else {
//                    groupRemove(((Integer) rbS1.getId()));
//                }
//            } else if (msgOthers.getS2().isRxMsgSelect()) {
//                if (msgOthers.getS2().isValue()) {
//                    groupCheck(rbS2.getId());
//                } else {
//                    groupRemove(((Integer) rbS2.getId()));
//                }
//            }
//            setChannels(false);
        }
    };

    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() {
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception {
//            channelShow[5] = msgRef.getRefChecked().isValue() && msgRef.getR1Checked().isValue();
//            channelShow[6] = msgRef.getRefChecked().isValue() && msgRef.getR2Checked().isValue();
//            channelShow[7] = msgRef.getRefChecked().isValue() && msgRef.getR3Checked().isValue();
//            channelShow[8] = msgRef.getRefChecked().isValue() && msgRef.getR4Checked().isValue();
//            int index = IWave.Ch1;
//            if (msgRef.getR1Checked().isRxMsgSelect()) {
//                index = IWave.R1;
//            } else if (msgRef.getR2Checked().isRxMsgSelect()) {
//                index = IWave.R2;
//            } else if (msgRef.getR3Checked().isRxMsgSelect()) {
//                index = IWave.R3;
//            } else if (msgRef.getR4Checked().isRxMsgSelect()) {
//                index = IWave.R4;
//            } else if (msgRef.getRefChecked().isRxMsgSelect()) {
//                if (msgRef.getR1Checked().isValue()) {
//                    index = IWave.R1;
//                } else if (msgRef.getR2Checked().isValue()) {
//                    index = IWave.R2;
//                } else if (msgRef.getR3Checked().isValue()) {
//                    index = IWave.R3;
//                } else if (msgRef.getR4Checked().isValue()) {
//                    index = IWave.R4;
//                }
//            }
//            if (index == IWave.R1) {
//                if (msgRef.getR1Checked().isValue()) {
//                    groupCheck(rbR1.getId());
//                } else {
//                    groupRemove(((Integer) rbR1.getId()));
//                }
//            } else if (index == IWave.R2) {
//                if (msgRef.getR2Checked().isValue()) {
//                    groupCheck(rbR2.getId());
//                } else {
//                    groupRemove(((Integer) rbR2.getId()));
//                }
//            } else if (index == IWave.R3) {
//                if (msgRef.getR3Checked().isValue()) {
//                    groupCheck(rbR3.getId());
//                } else {
//                    groupRemove(((Integer) rbR3.getId()));
//                }
//            } else if (index == IWave.R4) {
//                if (msgRef.getR4Checked().isValue()) {
//                    groupCheck(rbR4.getId());
//                } else {
//                    groupRemove(((Integer) rbR4.getId()));
//                }
//            } else {
//                groupRemove(((Integer) rbR1.getId()));
//                groupRemove(((Integer) rbR2.getId()));
//                groupRemove(((Integer) rbR3.getId()));
//                groupRemove(((Integer) rbR4.getId()));
//            }
//            setChannels(false);
        }
    };

    private Consumer<MainCenterMsgChannels> consumerMainCenterChannel = new Consumer<MainCenterMsgChannels>() {
        @Override
        public void accept(MainCenterMsgChannels msgChannels) throws Exception {
            if (msgChannels.getChNO() != MainCenterMsgChannels.CH_NULL) {
                int lastCh = ChannelFactory.getChActivate();
                int nowCh = msgChannels.getChNO() - 1;
                if (lastCh == nowCh) return;
                if (!msgChannels.isFromEventBus()) {
                    ChannelFactory.chActivate(nowCh);
                }
                if (!(lastCh <= ChannelFactory.MATH1 && nowCh <= ChannelFactory.MATH1)) {
                    TriggerTimebase.getInstance().drawActiveTrigTime(msgChannels.isFromEventBus());
                }

                if (!msgChannels.isSelf()) {
//                    groupCheck(group.getChildAt(msgChannels.getChNO() - 1).getId());
                    refreshRadioGroupCheck(msgChannels.getChNO()-1);
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(msgChannels.getChNO() - 1));
                }
            }
        }
    };

    private int visibility = GONE;

    public int setDialogVisible(int visibility) {
        int j = 0;
        for (int i = 0; i < channelShow.length; i++) {
            if (channelShow[i]) {
                j++;
            }
        }
        if (j >= 2) {
            if (getVisibility()!=visibility) {
                RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_VISIBLE_LAYOUTTOBTN, visibility);
                Command.get().getMenu().ChannelSelector(visibility == View.VISIBLE, false);
                this.visibility = visibility;
                setVisibility(visibility);
            }
        } else {
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_CHANNELLIST_VISIBLE, String.valueOf(false));
            RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_VISIBLE_LAYOUTTOBTN, GONE);
            Command.get().getMenu().ChannelSelector(visibility == View.VISIBLE, false);
            this.visibility = GONE;
            setVisibility(GONE);
        }
        return this.visibility;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility==View.VISIBLE){
            Tools.PrintControlsLocation(rootViewGroup);
        }
    }

    /**
     * 获得能被切换到的ref的index
     *
     * @return -1为没有这样的ref
     */
    public int getRefCanClick() {
        for (int i = 5; i < channelShow.length; i++) {
            if (channelShow[i]) {
                return i;
            }
        }
        return -1;
    }

    public int getCurChannelShowCount() {
        int j = 0;
        for (int i = 0; i < channelShow.length; i++) {
            if (channelShow[i]) {
                j++;
            }
        }
        return j;
    }

    public boolean[] getChannelShow() {
        return channelShow;
    }

    public int getCurChannelShowIndex() {
        int index= mRadioGroup.getSelectIndex();
        if (index==-1) return 0;
        return index;
//        int j = 0;
//        int index = -1;
//        for (int i = 0; i < channelShow.length; i++) {
//            if (group.getChildAt(i).getId() == group.getCheckedRadioButtonId()) {
//                index = j;
//            }
//            if (channelShow[i]) {
//                j++;
//            }
//        }
//        if (j == 0) {
//            return 0;
//        } else {
//            return index;
//        }
    }

    public int getChannelSelectIndex() {
        int idx = mRadioGroup.getSelectIndex();
        if(idx >= 0 && idx < channelShow.length){
            if(channelShow[idx]){
                return idx;
            }
        }
        return ChannelFactory.getChActivate();

//        int j = 0;
//        int index = -1;
//        for (int i = 0; i < channelShow.length; i++) {
//            if (channelShow[i]) {
//                j++;
//                if (group.getChildAt(i).getId() == group.getCheckedRadioButtonId()) {
//                    index = i;
//                }
//            }
//
//        }
//        if (j == 0) {
//            return -1;
//        } else {
//            return index;
//        }
    }

    public void setChannelSelectIndex(int selectIndex,boolean isFormEventBus) {
        if (selectIndex < 0 || selectIndex >= channelShow.length) return;
        if (channelShow[selectIndex]) {
            refreshRadioGroupCheck(selectIndex);
//            groupCheck(group.getChildAt(selectIndex).getId());
            MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(selectIndex),isFormEventBus);
        }
    }

    public void updateSelect(boolean isFromEventBus) {
        refreshRadioGroup(isFromEventBus);
    }

    private void setExternalKeyBackStateFocus(){
        ExternalKeysProtocol.BackStateUpdate(ExternalKeysProtocol.BACKSTATE_CHLIST);
    }
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
//            setExternalKeyBackStateFocus();
//            switch (v.getId()) {
//                case R.id.channelsCh1:
//                case R.id.channelsCh2:
//                case R.id.channelsCh3:
//                case R.id.channelsCh4:
//                case R.id.channelsMath:
//                case R.id.channelsR1:
//                case R.id.channelsR2:
//                case R.id.channelsR3:
//                case R.id.channelsR4:
//                case R.id.channelsS1:
//                case R.id.channelsS2:
//                    PlaySound.getInstance().playButton();
//                    groupCheck(v.getId());
//                    setChannels(false);
//                    break;
//            }
        }
    };

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
                double v = Math.sqrt(Math.pow(moveX - downX, 2) + Math.pow(moveY - downY, 2));
                double t = ev.getEventTime() - ev.getDownTime();
                if( (t > 100 && v > 20)  || v > 40){
                    return true;
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
                RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_MOVE, 0);
                break;
            case MotionEvent.ACTION_UP:
                if (left != this.getX() || top != this.getY()) {
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_X, String.valueOf((int) this.getX()));
                    CacheUtil.get().putMap(CacheUtil.MAIN_CENTER_CHANNELS_Y, String.valueOf((int) this.getY()));
                }
                break;
        }
        return true;
    }

    public void setLocation(int x, int y) {
        setX(x);
        setY(y);
        RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_MOVE, 0);
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

    public void changeColor() {
        int[] arraysColor= new int[28];
        System.arraycopy(SvgNodeInfo.getColorsIntForCenterChView(), 0, arraysColor, 0, 28);
        mRadioGroup.setColors(arraysColor);

        channelShow = MiddleMain.getIns().getChanSelectorManage().refreshChannelShow();
        refreshMRadioGroupLayout();
        refreshRadioGroup(false);

    }
}
