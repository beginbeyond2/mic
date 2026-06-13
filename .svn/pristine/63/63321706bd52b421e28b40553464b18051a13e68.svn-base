package com.micsig.tbook.tbookscope.top.layout.trigger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM429;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.Serials;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsDetailFlag;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.TopLayoutTriggerSerials;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitle;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTrigger extends Fragment implements SerialsDetailFlag, TopViewTitleWithScroll.IOnScrollViewSliderListener {
    private static final String TAG = "TopLayoutTrigger";
    public static final int DETAIL_COMMON = 0;
    public static final int DETAIL_EDGE = 1;
    public static final int DETAIL_PULSEWIDTH = 2;
    public static final int DETAIL_LOGIC = 3;
    public static final int DETAIL_NEDGE = 4;
    public static final int DETAIL_RUNT = 5;
    public static final int DETAIL_SLOPE = 6;
    public static final int DETAIL_TIMEOUT = 7;
    public static final int DETAIL_VIDEO = 8;

    public static final int DETAIL_S1 = 9;
    public static final int DETAIL_S2 = 10;
    public static final int DETAIL_S3 = 11;
    public static final int DETAIL_S4 = 12;

    private Context context;
    private RelativeLayout triggerDetail;
    private TopViewTitleWithScroll triggerTitle;
    private TopLayoutTriggerCommon triggerCommonLayout;         //常规
    private TopLayoutTriggerEdge triggerEdgeLayout;             //边沿
    private TopLayoutTriggerPulsewidth triggerPulsewidthLayout; //脉宽
    private TopLayoutTriggerLogic triggerLogicLayout;           //逻辑
    private TopLayoutTriggerNEdge triggerNEdgeLayout;           //N边沿
    private TopLayoutTriggerRunt triggerRuntLayout;             //欠幅
    private TopLayoutTriggerSlope triggerSlopeLayout;           //斜率
    private TopLayoutTriggerTimeout triggerTimeoutLayout;       //超时
    private TopLayoutTriggerVideo triggerVideoLayout;           //视频
    private TopLayoutTriggerSerials triggerS1Layout;            //S1
    private TopLayoutTriggerSerials triggerS2Layout;            //S2
    private TopLayoutTriggerSerials triggerS3Layout;            //S3
    private TopLayoutTriggerSerials triggerS4Layout;            //S4

    private TopMsgTrigger msgTrigger;

    private ArrayList<Serials> uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1;
    private ArrayList<Serials> uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2;
    private ArrayList<Serials> uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3;
    private ArrayList<Serials> uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4;

    private String[] tags = {"triggerCommon", "triggerEdge", "triggerPulsewidth"
            , "triggerLogic", "triggerNEdge", "triggerRunt", "triggerSlope", "triggerTimeout"
            , "triggerVideo", "triggerS1", "triggerS2", "triggerS3", "triggerS4"};
    private Fragment[] fragments = new Fragment[13];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_trigger, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view, savedInstanceState);
        initControl();
    }

    private void initView(View view, Bundle savedInstanceState) {
        triggerTitle = (TopViewTitleWithScroll) view.findViewById(R.id.triggerTitle);
        triggerDetail = (RelativeLayout) view.findViewById(R.id.triggerDetail);
        triggerTitle.setData(R.array.trigger, onCheckChangedTitleListener, onItemClickListener);
        triggerTitle.setOnScrollViewSliderListener(this);

        initSerialsList();
        initLayout(savedInstanceState);
    }

    //region interface TopViewTitleWithScroll.IOnScrollViewSliderListener
    @Override
    public void onSliderStop() {

    }
    //endregion

    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < tags.length; i++) {
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]);
            }
        }

        triggerCommonLayout = fragments[0] == null ? new TopLayoutTriggerCommon() : (TopLayoutTriggerCommon) fragments[0];
        triggerEdgeLayout = fragments[1] == null ? new TopLayoutTriggerEdge() : (TopLayoutTriggerEdge) fragments[1];
        triggerPulsewidthLayout = fragments[2] == null ? new TopLayoutTriggerPulsewidth() : (TopLayoutTriggerPulsewidth) fragments[2];
        triggerLogicLayout = fragments[3] == null ? new TopLayoutTriggerLogic() : (TopLayoutTriggerLogic) fragments[3];
        triggerNEdgeLayout = fragments[4] == null ? new TopLayoutTriggerNEdge() : (TopLayoutTriggerNEdge) fragments[4];
        triggerRuntLayout = fragments[5] == null ? new TopLayoutTriggerRunt() : (TopLayoutTriggerRunt) fragments[5];
        triggerSlopeLayout = fragments[6] == null ? new TopLayoutTriggerSlope() : (TopLayoutTriggerSlope) fragments[6];
        triggerTimeoutLayout = fragments[7] == null ? new TopLayoutTriggerTimeout() : (TopLayoutTriggerTimeout) fragments[7];
        triggerVideoLayout = fragments[8] == null ? new TopLayoutTriggerVideo() : (TopLayoutTriggerVideo) fragments[8];
        triggerS1Layout = fragments[9] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[9];
        triggerS2Layout = fragments[10] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[10];
        triggerS3Layout = fragments[11] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[11];
        triggerS4Layout = fragments[12] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[12];

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.triggerDetail, triggerCommonLayout, tags[0])
                    .add(R.id.triggerDetail, triggerEdgeLayout, tags[1])
                    .add(R.id.triggerDetail, triggerPulsewidthLayout, tags[2])
                    .add(R.id.triggerDetail, triggerLogicLayout, tags[3])
                    .add(R.id.triggerDetail, triggerNEdgeLayout, tags[4])
                    .add(R.id.triggerDetail, triggerRuntLayout, tags[5])
                    .add(R.id.triggerDetail, triggerSlopeLayout, tags[6])
                    .add(R.id.triggerDetail, triggerTimeoutLayout, tags[7])
                    .add(R.id.triggerDetail, triggerVideoLayout, tags[8])
                    .add(R.id.triggerDetail, triggerS1Layout, tags[9])
                    .add(R.id.triggerDetail, triggerS2Layout, tags[10])
                    .add(R.id.triggerDetail, triggerS3Layout, tags[11])
                    .add(R.id.triggerDetail, triggerS4Layout, tags[12])
                    .hide(triggerEdgeLayout)
                    .hide(triggerPulsewidthLayout)
                    .hide(triggerLogicLayout)
                    .hide(triggerNEdgeLayout)
                    .hide(triggerRuntLayout)
                    .hide(triggerSlopeLayout)
                    .hide(triggerTimeoutLayout)
                    .hide(triggerVideoLayout)
                    .hide(triggerS1Layout)
                    .hide(triggerS2Layout)
                    .hide(triggerS3Layout)
                    .hide(triggerS4Layout)
                    .commitAllowingStateLoss();
        }

        triggerS1Layout.initList(uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1, CacheUtil.S1);
        triggerS2Layout.initList(uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2, CacheUtil.S2);
        triggerS3Layout.initList(uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3, CacheUtil.S3);
        triggerS4Layout.initList(uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4, CacheUtil.S4);

        triggerCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerEdgeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerPulsewidthLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerLogicLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerNEdgeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerRuntLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerSlopeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerTimeoutLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerVideoLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerS1Layout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerS2Layout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerS3Layout.setOnDetailSendMsgListener(onDetailSendMsgListener);
        triggerS4Layout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgTrigger = new TopMsgTrigger();
        msgTrigger.setTriggerTitle(triggerTitle.getSelected());
        msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail());
        msgTrigger.setFromEventBus(false);
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS_FOLLOW).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_TYPE, eventUIObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState);
    }

    private void initSerialsList() {
        int[] uartsDetailFlag = {NULL, NULL, UART_DATA, UART_0DATA, UART_1DATA
                , UART_XDATA, NULL};
        int[] linsDetailFlag = new int[]{NULL, LIN_FRAMEID, LIN_IDDATA, NULL, NULL};
        int[] cansDetailFlag = new int[]{NULL, CAN_REMOTEID, CAN_DATAID, CAN_RDID, CAN_IDDATA
                , NULL, NULL, NULL, NULL};
        int[] spisDetailFlag = new int[]{NULL, SPI_DATA, NULL};
        int[] i2csDetailFlag = new int[]{NULL, NULL, NULL, NULL, I2C_NOACKINADR
                , I2C_FRAME1, I2C_FRAME2, I2C_ROMDATA, I2C_10WRITEFRAME};
        int[] m429sDetailFlag = new int[]{NULL, NULL, ARINC429_LABEL, ARINC429_SDI, ARINC429_DATA
                , ARINC429_SSM, ARINC429_LABELSDI, ARINC429_LABELDATA, ARINC429_LABELSSM, NULL
                , NULL, NULL, NULL, NULL, NULL};
        int[] m1553bDetailFlag = new int[]{NULL, NULL, M1553B_CSWORD, M1553B_RTADDR, NULL
                , M1553B_DATAWORD, NULL, NULL};

        uart1 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S1);
        lin1 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S1);
        can1 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S1);
        spi1 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S1);
        i2c1 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S1);
        arinc4291 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S1);
        m1553b1 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S1);

        uart2 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S2);
        lin2 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S2);
        can2 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S2);
        spi2 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S2);
        i2c2 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S2);
        arinc4292 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S2);
        m1553b2 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S2);

        uart3 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S3);
        lin3 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S3);
        can3 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S3);
        spi3 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S3);
        i2c3 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S3);
        arinc4293 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S3);
        m1553b3 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S3);

        uart4 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S4);
        lin4 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S4);
        can4 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S4);
        spi4 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S4);
        i2c4 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S4);
        arinc4294 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S4);
        m1553b4 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S4);

    }

    private ArrayList<Serials> getSerialsListFromStrings(int arrayResId, int[] detailFlags, String cacheListKey) {
        String[] strings = App.get().getResources().getStringArray(arrayResId);
        ArrayList<Serials> list = new ArrayList<>();
        for (int i = 0; i < strings.length; i++) {
            Serials serials = new Serials(strings[i], i, cacheListKey, detailFlags[i]);
            serials.setSelected(i == CacheUtil.get().getInt(cacheListKey));
            list.add(serials);
        }

        if (arrayResId == R.array.triggerSerialsUART && list.size() >= 7) {
            int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + cacheListKey.replace(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART, ""));
            int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + cacheListKey.replace(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART, ""));
            list.get(2).setEnabled(bits != 4);
            list.get(3).setEnabled(bits == 4);
            list.get(4).setEnabled(bits == 4);
            list.get(5).setEnabled(bits == 4);
            list.get(6).setEnabled(check != 0);
        }
        return list;
    }

    private void setCache() {
        initSerialsList();
        triggerS1Layout.initList(uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1, CacheUtil.S1);
        triggerS2Layout.initList(uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2, CacheUtil.S2);
        triggerS3Layout.initList(uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3, CacheUtil.S3);
        triggerS4Layout.initList(uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4, CacheUtil.S4);
        triggerS1Layout.setInitCache();
        triggerS2Layout.setInitCache();
        triggerS3Layout.setInitCache();
        triggerS4Layout.setInitCache();

        int serials1 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
        int serials2 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
        int serials3 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
        int serials4 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
        String[] serialsStr = context.getResources().getStringArray(R.array.triggerSerialsTitle);
        triggerTitle.updateItemText(DETAIL_S1, ("S1 " + serialsStr[serials1]).toUpperCase());
        triggerTitle.updateItemText(DETAIL_S2, ("S2 " + serialsStr[serials2]).toUpperCase());
        triggerTitle.updateItemText(DETAIL_S3, ("S3 " + serialsStr[serials3]).toUpperCase());
        triggerTitle.updateItemText(DETAIL_S4, ("S4 " + serialsStr[serials4]).toUpperCase());
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);

        handler.sendEmptyMessageDelayed(MSG, 200);
        triggerTitle.setCacheSelect(triggerIndex);
        onCheckChanged(triggerTitle.getSelected(), false);
        Command.get().getTrigger().Type(triggerIndex,false);

        TriggerFactory.getInstance().setTriggerType(TopMatchTrigger.triggerTypeViewToScope(triggerIndex));

        if (rightMsgSerials1 != null) {
            try {
                consumerRightSerials.accept(rightMsgSerials1);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (rightMsgSerials2 != null) {
            try {
                consumerRightSerials.accept(rightMsgSerials2);
            }  catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (rightMsgSerials3 != null) {
            try {
                consumerRightSerials.accept(rightMsgSerials3);
            }  catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (rightMsgSerials4 != null) {
            try {
                consumerRightSerials.accept(rightMsgSerials4);
            }  catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static final int MSG = 164;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG) {
                int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
                triggerTitle.moveOnlyScroll(triggerIndex);
            }
        }
    };

    /**
     * Serials word 显现消失的时候，设置trigger标签下的所有标签的可点击性
     */
    public void setSerialsWordVisible(boolean serialsWordVisible) {
        triggerTitle.setEnable(DETAIL_COMMON, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_EDGE, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_PULSEWIDTH, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_LOGIC, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_NEDGE, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_RUNT, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_SLOPE, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_TIMEOUT, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_VIDEO, !serialsWordVisible);
        triggerTitle.setEnable(DETAIL_S1, true);
        triggerTitle.setEnable(DETAIL_S2, true);
        triggerTitle.setEnable(DETAIL_S3, true);
        triggerTitle.setEnable(DETAIL_S4, true);

//        if (serialsWordVisible && triggerTitle.getSelected().getIndex() != DETAIL_S1 && triggerTitle.getSelected().getIndex() != DETAIL_S2) {
//            triggerTitle.moveOnlyScroll(DETAIL_S1);
//            triggerTitle.setSelected(DETAIL_S1);
//            onCheckChanged(triggerTitle.getSelected(), false);
//        }
    }

    public void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_TRIGGER, msgTrigger);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTrigger, true);
        }
    };

    private RightMsgSerials rightMsgSerials1;
    private RightMsgSerials rightMsgSerials2;
    private RightMsgSerials rightMsgSerials3;
    private RightMsgSerials rightMsgSerials4;

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(@NonNull RightMsgSerials rightMsgSerials) throws Exception {
            if (rightMsgSerials.isSerials1()) {
                TopLayoutTrigger.this.rightMsgSerials1 = rightMsgSerials;
            } else if (rightMsgSerials.isSerials2()) {
                TopLayoutTrigger.this.rightMsgSerials2 = rightMsgSerials;
            } else if (rightMsgSerials.isSerials3()) {
                TopLayoutTrigger.this.rightMsgSerials3 = rightMsgSerials;
            } else if (rightMsgSerials.isSerials4()) {
                TopLayoutTrigger.this.rightMsgSerials4 = rightMsgSerials;
            }
            int serialsType = rightMsgSerials.getSerialsType().getIndex();
            int detailSerialsIndex;
            String namePre;
            TopLayoutTriggerSerials triggerSerials;
            List<Serials> uart, lin, can, spi, i2c, arinc429, m1553b;
            if (rightMsgSerials.isSerials1()) {
                detailSerialsIndex = DETAIL_S1;
                namePre = "s1 ";
                triggerSerials = triggerS1Layout;
                uart = uart1;
                lin = lin1;
                can = can1;
                spi = spi1;
                i2c = i2c1;
                arinc429 = arinc4291;
                m1553b = m1553b1;
            } else if (rightMsgSerials.isSerials2()) {
                detailSerialsIndex = DETAIL_S2;
                namePre = "s2 ";
                triggerSerials = triggerS2Layout;
                uart = uart2;
                lin = lin2;
                can = can2;
                spi = spi2;
                i2c = i2c2;
                arinc429 = arinc4292;
                m1553b = m1553b2;
            } else if (rightMsgSerials.isSerials3()) {
                detailSerialsIndex = DETAIL_S3;
                namePre = "s3 ";
                triggerSerials = triggerS3Layout;
                uart = uart3;
                lin = lin3;
                can = can3;
                spi = spi3;
                i2c = i2c3;
                arinc429 = arinc4293;
                m1553b = m1553b3;
            } else {
                detailSerialsIndex = DETAIL_S4;
                namePre = "s4 ";
                triggerSerials = triggerS4Layout;
                uart = uart4;
                lin = lin4;
                can = can4;
                spi = spi4;
                i2c = i2c4;
                arinc429 = arinc4294;
                m1553b = m1553b4;
            }
            String[] serials = context.getResources().getStringArray(R.array.triggerSerialsTitle);
            triggerTitle.updateItemText(detailSerialsIndex, (namePre + serials[serialsType]).toUpperCase());
            switch (serialsType) {
                case UART:
                    RightMsgSerialsUart serialsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails();
                    uart.get(2).setEnabled(serialsUart.getBits().getIndex() != 4);
                    uart.get(3).setEnabled(serialsUart.getBits().getIndex() == 4);
                    uart.get(4).setEnabled(serialsUart.getBits().getIndex() == 4);
                    uart.get(5).setEnabled(serialsUart.getBits().getIndex() == 4);
                    uart.get(6).setEnabled(serialsUart.getCheck().getIndex() != 0);
                    for (int i = 0; i < uart.size(); i++) {
                        if (uart.get(i).isSelected() && !uart.get(i).isEnabled()) {
                            uart.get(0).setSelected(true);
                            uart.get(i).setSelected(false);
                            break;
                        }
                    }
                    int i1;
                    for (i1 = 0; i1 < uart.size(); i1++) {
                        if (uart.get(i1).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(uart, i1, rightMsgSerials.isFromEventBus());
                    break;
                case LIN:
                    int i2;
                    for (i2 = 0; i2 < lin.size(); i2++) {
                        if (lin.get(i2).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(lin, i2, rightMsgSerials.isFromEventBus());
                    break;
                case CAN:
                    int i3;
                    for (i3 = 0; i3 < can.size(); i3++) {
                        if (can.get(i3).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(can, i3, rightMsgSerials.isFromEventBus());
                    break;
                case SPI:
                    RightMsgSerialsSpi serialsSpi = (RightMsgSerialsSpi) rightMsgSerials.getSerialsDetails();
                    spi.get(0).setEnabled(serialsSpi.getCsSwitch().isValue());
                    if (spi.get(0).isSelected() && !spi.get(0).isEnabled()) {
                        spi.get(0).setSelected(false);
                        spi.get(1).setSelected(true);
                    }
                    int i4;
                    for (i4 = 0; i4 < spi.size(); i4++) {
                        if (spi.get(i4).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(spi, i4, rightMsgSerials.isFromEventBus());
                    break;
                case I2C:
                    int i5;
                    for (i5 = 0; i5 < i2c.size(); i5++) {
                        if (i2c.get(i5).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(i2c, i5, rightMsgSerials.isFromEventBus());
                    break;
                case ARINC429:
                    RightMsgSerialsM429 serialsM429 = (RightMsgSerialsM429) rightMsgSerials.getSerialsDetails();
                    switch (serialsM429.getFormat().getIndex()) {
                        case 0:
                            arinc429.get(3).setEnabled(false);
                            arinc429.get(5).setEnabled(false);
                            arinc429.get(6).setEnabled(false);
                            arinc429.get(8).setEnabled(false);
                            break;
                        case 1:
                            arinc429.get(3).setEnabled(false);
                            arinc429.get(5).setEnabled(true);
                            arinc429.get(6).setEnabled(false);
                            arinc429.get(8).setEnabled(true);
                            break;
                        default:
                            arinc429.get(3).setEnabled(true);
                            arinc429.get(5).setEnabled(true);
                            arinc429.get(6).setEnabled(true);
                            arinc429.get(8).setEnabled(true);
                            break;
                    }
                    for (int i = 0; i < arinc429.size(); i++) {
                        if (arinc429.get(i).isSelected() && !arinc429.get(i).isEnabled()) {
                            arinc429.get(0).setSelected(true);
                            arinc429.get(i).setSelected(false);
                            break;
                        }
                    }
                    int i6;
                    for (i6 = 0; i6 < arinc429.size(); i6++) {
                        if (arinc429.get(i6).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(arinc429, i6, rightMsgSerials.isFromEventBus());
                    break;
                case M1553B:
                    int i7;
                    for (i7 = 0; i7 < m1553b.size(); i7++) {
                        if (m1553b.get(i7).isSelected()) {
                            break;
                        }
                    }
                    triggerSerials.setList(m1553b, i7, rightMsgSerials.isFromEventBus());
                    break;
            }
        }
    };

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGER_TYPE: {
                    if (triggerTitle.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) {
                        triggerTitle.setSelected(Integer.parseInt(commandMsgToUI.getParam()));
                        onCheckChanged(triggerTitle.getSelected(), false);
                    }
                    break;
                }

            }
        }
    };

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true));
        }
    };

    public void setTriggerIdx(int triggerIdx){
        triggerTitle.setSelected(triggerIdx);
        onCheckChanged(triggerTitle.getSelected(), false);
    }
    public int getTriggerIdx(){
        return triggerTitle.getSelected().getIndex();
    }

    private void onCheckChanged(TopAllBeanTitle item, boolean isFromEventBus) {

        Command.get().getTrigger().Type(item.getIndex(), false);
        if (item.getIndex() != DETAIL_COMMON) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER, String.valueOf(item.getIndex()));
        }
        if (!isFromEventBus) {
            TriggerFactory.getInstance().setTriggerType(TopMatchTrigger.triggerTypeViewToScope(item.getIndex()));
        }
        getChildFragmentManager().beginTransaction()
                .hide(triggerCommonLayout)
                .hide(triggerEdgeLayout)
                .hide(triggerPulsewidthLayout)
                .hide(triggerLogicLayout)
                .hide(triggerNEdgeLayout)
                .hide(triggerRuntLayout)
                .hide(triggerSlopeLayout)
                .hide(triggerTimeoutLayout)
                .hide(triggerVideoLayout)
                .hide(triggerS1Layout)
                .hide(triggerS2Layout)
                .hide(triggerS3Layout)
                .hide(triggerS4Layout)
                .commitAllowingStateLoss();
        switch (item.getIndex()) {
            case DETAIL_COMMON:             //常规
                getChildFragmentManager().beginTransaction()
                        .show(triggerCommonLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_EDGE:             //边沿
                getChildFragmentManager().beginTransaction()
                        .show(triggerEdgeLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerEdgeLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_PULSEWIDTH:             //脉宽
                getChildFragmentManager().beginTransaction()
                        .show(triggerPulsewidthLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerPulsewidthLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_LOGIC:             //逻辑
                getChildFragmentManager().beginTransaction()
                        .show(triggerLogicLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerLogicLayout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_NEDGE:             //N边沿
                getChildFragmentManager().beginTransaction()
                        .show(triggerNEdgeLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerNEdgeLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_RUNT:             //欠幅
                getChildFragmentManager().beginTransaction()
                        .show(triggerRuntLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerRuntLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_SLOPE:             //斜率
                getChildFragmentManager().beginTransaction()
                        .show(triggerSlopeLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerSlopeLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_TIMEOUT:             //超时
                getChildFragmentManager().beginTransaction()
                        .show(triggerTimeoutLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerTimeoutLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_VIDEO:             //视频
                getChildFragmentManager().beginTransaction()
                        .show(triggerVideoLayout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerVideoLayout.getMsgTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_S1:             //S1 Serials
                getChildFragmentManager().beginTransaction()
                        .show(triggerS1Layout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerS1Layout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_S2:            //S2 Serials
                getChildFragmentManager().beginTransaction()
                        .show(triggerS2Layout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerS2Layout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_S3:
                getChildFragmentManager().beginTransaction()
                        .show(triggerS3Layout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerS3Layout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
            case DETAIL_S4:
                getChildFragmentManager().beginTransaction()
                        .show(triggerS4Layout).commitAllowingStateLoss();
                msgTrigger.setTriggerTitle(item);
                msgTrigger.setTriggerDetail(triggerS4Layout.getTriggerDetail());
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
                break;
        }
    }

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            onCheckChanged(item, false);
        }
    };

    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() {
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) {
            if (fragment.equals(triggerCommonLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_EDGE)
                    || (fragment.equals(triggerEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_EDGE));
                msgTrigger.setTriggerDetail(triggerEdgeLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerPulsewidthLayout) && triggerTitle.getSelected().getIndex() == DETAIL_PULSEWIDTH)
                    || (fragment.equals(triggerPulsewidthLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_PULSEWIDTH));
                msgTrigger.setTriggerDetail(triggerPulsewidthLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerLogicLayout) && triggerTitle.getSelected().getIndex() == DETAIL_LOGIC)
                    || (fragment.equals(triggerLogicLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_LOGIC));
                msgTrigger.setTriggerDetail(triggerLogicLayout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerNEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_NEDGE)
                    || (fragment.equals(triggerNEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_NEDGE));
                msgTrigger.setTriggerDetail(triggerNEdgeLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerRuntLayout) && triggerTitle.getSelected().getIndex() == DETAIL_RUNT)
                    || (fragment.equals(triggerRuntLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_RUNT));
                msgTrigger.setTriggerDetail(triggerRuntLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerSlopeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_SLOPE)
                    || (fragment.equals(triggerSlopeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_SLOPE));
                msgTrigger.setTriggerDetail(triggerSlopeLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if ((fragment.equals(triggerTimeoutLayout) && triggerTitle.getSelected().getIndex() == DETAIL_TIMEOUT)
                    || (fragment.equals(triggerTimeoutLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) {
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_TIMEOUT));
                msgTrigger.setTriggerDetail(triggerTimeoutLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(triggerVideoLayout) && triggerTitle.getSelected().getIndex() == DETAIL_VIDEO) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerVideoLayout.getMsgTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(triggerS1Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S1) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerS1Layout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(triggerS2Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S2) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerS2Layout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(triggerS3Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S3) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerS3Layout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            } else if (fragment.equals(triggerS4Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S4) {
                msgTrigger.setTriggerTitle(triggerTitle.getSelected());
                msgTrigger.setTriggerDetail(triggerS4Layout.getTriggerDetail());
                msgTrigger.getTriggerTitle().setRxMsgSelect(false);
                msgTrigger.setFromEventBus(isFromEventBus);
                sendMsg();
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_TYPE) {
                int triggerType = TopMatchTrigger.triggerTypeScopeToView(TriggerFactory.getTriggerType());
                if (triggerTitle.getSelected().getIndex() != triggerType) {
                    triggerTitle.moveOnlyScroll(0);
                    triggerTitle.setSelected(triggerType);
                    onCheckChanged(triggerTitle.getSelected(), true);
                }
            }
        }
    };


    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            if (aBoolean && getTriggerIdx() != DETAIL_EDGE) {
                setTriggerIdx(DETAIL_EDGE);
            }
        }
    };

}
