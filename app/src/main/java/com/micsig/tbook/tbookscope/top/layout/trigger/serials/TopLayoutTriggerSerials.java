package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.scope.Bus.ARINC429Bus;
import com.micsig.tbook.scope.Bus.CanBus;
import com.micsig.tbook.scope.Bus.I2CBus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;
import com.micsig.tbook.scope.Bus.SpiBus;
import com.micsig.tbook.scope.Bus.UartBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.DataBean;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSsm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Sdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Ssm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanDataId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRemoteId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2c10WriteFrame;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame1;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cNoAckInAdr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bCsWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bRtAddr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart0Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartxData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Label;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelSdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelSsm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Sdi;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Ssm;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsBaseDetail;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanDataId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanRdId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanRemoteId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2c10WriteFrame;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cFrame1;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cFrame2;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cNoAckInAdr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cRomData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsLinFrameId;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsLinIdData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bCsWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bDataWord;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bRtAddr;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsSpiData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUart0Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUart1Data;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUartData;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUartxData;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerSerials extends Fragment implements SerialsDetailFlag {
    private Context context;
    private LinearLayout serialsDetail;
    private View divider;
    private List<Serials> list = new ArrayList<>();
    private int serialsNumber;
    private SerialsAdapter adapter;

    private TopMsgTriggerSerials triggerDetail;
    private OnDetailSendMsgListener onDetailSendMsgListener;

    private TopLayoutTriggerSerialsBaseDetail fragments[] = new TopLayoutTriggerSerialsBaseDetail[26];
    private String fragmentTag[] = {
            "viewUartData",
            "viewUart0Data",
            "viewUart1Data",
            "viewUartxData",
            "viewLinFrameId",
            "viewLinIdData",
            "viewCanRemoteId",
            "viewCanDataId",
            "viewCanRDId",
            "viewCanIdData",
            "viewSpiData",
            "viewI2cNoAckInAdr",
            "viewI2cFrame1",
            "viewI2cFrame2",
            "viewI2cRomData",
            "viewI2c10WriteFrame",
            "viewArinc429Label",
            "viewArinc429Sdi",
            "viewArinc429Data",
            "viewArinc429Ssm",
            "viewArinc429LabelSdi",
            "viewArinc429LabelData",
            "viewArinc429LabelSsm",
            "viewM1553bCSWord",
            "viewM1553bRTAddr",
            "viewM1553bDataWord"
    };

    private TopLayoutTriggerSerialsBaseDetail viewUartData;
    private TopLayoutTriggerSerialsBaseDetail viewUart0Data;
    private TopLayoutTriggerSerialsBaseDetail viewUart1Data;
    private TopLayoutTriggerSerialsBaseDetail viewUartxData;
    private TopLayoutTriggerSerialsBaseDetail viewLinFrameId;
    private TopLayoutTriggerSerialsBaseDetail viewLinIdData;
    private TopLayoutTriggerSerialsBaseDetail viewCanRemoteId;
    private TopLayoutTriggerSerialsBaseDetail viewCanDataId;
    private TopLayoutTriggerSerialsBaseDetail viewCanRDId;
    private TopLayoutTriggerSerialsBaseDetail viewCanIdData;
    private TopLayoutTriggerSerialsBaseDetail viewSpiData;
    private TopLayoutTriggerSerialsBaseDetail viewI2cNoAckInAdr;
    private TopLayoutTriggerSerialsBaseDetail viewI2cFrame1;
    private TopLayoutTriggerSerialsBaseDetail viewI2cFrame2;
    private TopLayoutTriggerSerialsBaseDetail viewI2cRomData;
    private TopLayoutTriggerSerialsBaseDetail viewI2c10WriteFrame;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Label;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Sdi;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Data;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Ssm;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelSdi;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelData;
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelSsm;
    private TopLayoutTriggerSerialsBaseDetail viewM1553bCSWord;
    private TopLayoutTriggerSerialsBaseDetail viewM1553bRTAddr;
    private TopLayoutTriggerSerialsBaseDetail viewM1553bDataWord;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_triggerserials, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initDetailView(savedInstanceState);
        initControl();
    }

    RecyclerView listView;

    private void initView(View view) {
        listView = (RecyclerView) view.findViewById(R.id.list);
        serialsDetail = (LinearLayout) view.findViewById(R.id.serialsDetail);
        divider = view.findViewById(R.id.divider);

        String[] titles = getResources().getStringArray(R.array.triggerSerialsUART);
        triggerDetail = new TopMsgTriggerSerials();
        triggerDetail.setSerials(new Serials(titles[0], 0, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber(), NULL));
        triggerDetail.getSerials().setRxMsgSelect(false);
        triggerDetail.setSerialsDetail(null);
        divider.setVisibility(View.GONE);

        listView.setLayoutManager(new GridLayoutManager(context, 8));
        adapter = new SerialsAdapter(context, list);
        adapter.setOnItemClickListener(onSerialsItemClickListener);
        listView.setAdapter(adapter);
    }

    private ArrayList<Serials> uart, lin, can, spi, i2c, m429, m1553b;

    public void initList(ArrayList<Serials> uart, ArrayList<Serials> lin, ArrayList<Serials> can, ArrayList<Serials> spi, ArrayList<Serials> i2c, ArrayList<Serials> m429, ArrayList<Serials> m1553b, int serialsNumber) {
        this.uart = uart;
        this.lin = lin;
        this.can = can;
        this.spi = spi;
        this.i2c = i2c;
        this.m429 = m429;
        this.m1553b = m1553b;
        this.serialsNumber = serialsNumber;
    }

    public void setInitCache() {
        setCache();
        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerSerials, true);
    }

    private int getSerialsNumber() {
        return serialsNumber;
    }

    private void initDetailView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (int i = 0; i < fragmentTag.length; i++) {
                TopLayoutTriggerSerialsBaseDetail fragment = (TopLayoutTriggerSerialsBaseDetail) getChildFragmentManager().findFragmentByTag(fragmentTag[i]);
                if (fragment != null) {
                    fragments[i] = fragment;
                }
            }
        }

        viewUartData = fragments[0] == null ? new TopLayoutTriggerSerialsUartData() : fragments[0];
        viewUart0Data = fragments[1] == null ? new TopLayoutTriggerSerialsUart0Data() : fragments[1];
        viewUart1Data = fragments[2] == null ? new TopLayoutTriggerSerialsUart1Data() : fragments[2];
        viewUartxData = fragments[3] == null ? new TopLayoutTriggerSerialsUartxData() : fragments[3];
        viewLinFrameId = fragments[4] == null ? new TopLayoutTriggerSerialsLinFrameId() : fragments[4];
        viewLinIdData = fragments[5] == null ? new TopLayoutTriggerSerialsLinIdData() : fragments[5];
        viewCanRemoteId = fragments[6] == null ? new TopLayoutTriggerSerialsCanRemoteId() : fragments[6];
        viewCanDataId = fragments[7] == null ? new TopLayoutTriggerSerialsCanDataId() : fragments[7];
        viewCanRDId = fragments[8] == null ? new TopLayoutTriggerSerialsCanRdId() : fragments[8];
        viewCanIdData = fragments[9] == null ? new TopLayoutTriggerSerialsCanIdData() : fragments[9];
        viewSpiData = fragments[10] == null ? new TopLayoutTriggerSerialsSpiData() : fragments[10];
        viewI2cNoAckInAdr = fragments[11] == null ? new TopLayoutTriggerSerialsI2cNoAckInAdr() : fragments[11];
        viewI2cFrame1 = fragments[12] == null ? new TopLayoutTriggerSerialsI2cFrame1() : fragments[12];
        viewI2cFrame2 = fragments[13] == null ? new TopLayoutTriggerSerialsI2cFrame2() : fragments[13];
        viewI2cRomData = fragments[14] == null ? new TopLayoutTriggerSerialsI2cRomData() : fragments[14];
        viewI2c10WriteFrame = fragments[15] == null ? new TopLayoutTriggerSerialsI2c10WriteFrame() : fragments[15];
        viewArinc429Label = fragments[16] == null ? new TopLayoutTriggerSerialsArinc429Label() : fragments[16];
        viewArinc429Sdi = fragments[17] == null ? new TopLayoutTriggerSerialsArinc429Sdi() : fragments[17];
        viewArinc429Data = fragments[18] == null ? new TopLayoutTriggerSerialsArinc429Data() : fragments[18];
        viewArinc429Ssm = fragments[19] == null ? new TopLayoutTriggerSerialsArinc429Ssm() : fragments[19];
        viewArinc429LabelSdi = fragments[20] == null ? new TopLayoutTriggerSerialsArinc429LabelSdi() : fragments[20];
        viewArinc429LabelData = fragments[21] == null ? new TopLayoutTriggerSerialsArinc429LabelData() : fragments[21];
        viewArinc429LabelSsm = fragments[22] == null ? new TopLayoutTriggerSerialsArinc429LabelSsm() : fragments[22];
        viewM1553bCSWord = fragments[23] == null ? new TopLayoutTriggerSerialsM1553bCsWord() : fragments[23];
        viewM1553bRTAddr = fragments[24] == null ? new TopLayoutTriggerSerialsM1553bRtAddr() : fragments[24];
        viewM1553bDataWord = fragments[25] == null ? new TopLayoutTriggerSerialsM1553bDataWord() : fragments[25];

        viewUartData.setSerialsNumber(serialsNumber);
        viewUart0Data.setSerialsNumber(serialsNumber);
        viewUart1Data.setSerialsNumber(serialsNumber);
        viewUartxData.setSerialsNumber(serialsNumber);
        viewLinFrameId.setSerialsNumber(serialsNumber);
        viewLinIdData.setSerialsNumber(serialsNumber);
        viewCanRemoteId.setSerialsNumber(serialsNumber);
        viewCanDataId.setSerialsNumber(serialsNumber);
        viewCanRDId.setSerialsNumber(serialsNumber);
        viewCanIdData.setSerialsNumber(serialsNumber);
        viewSpiData.setSerialsNumber(serialsNumber);
        viewI2cNoAckInAdr.setSerialsNumber(serialsNumber);
        viewI2cFrame1.setSerialsNumber(serialsNumber);
        viewI2cFrame2.setSerialsNumber(serialsNumber);
        viewI2cRomData.setSerialsNumber(serialsNumber);
        viewI2c10WriteFrame.setSerialsNumber(serialsNumber);
        viewArinc429Label.setSerialsNumber(serialsNumber);
        viewArinc429Sdi.setSerialsNumber(serialsNumber);
        viewArinc429Data.setSerialsNumber(serialsNumber);
        viewArinc429Ssm.setSerialsNumber(serialsNumber);
        viewArinc429LabelSdi.setSerialsNumber(serialsNumber);
        viewArinc429LabelData.setSerialsNumber(serialsNumber);
        viewArinc429LabelSsm.setSerialsNumber(serialsNumber);
        viewM1553bCSWord.setSerialsNumber(serialsNumber);
        viewM1553bRTAddr.setSerialsNumber(serialsNumber);
        viewM1553bDataWord.setSerialsNumber(serialsNumber);

        viewUartData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewUart0Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewUart1Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewUartxData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewLinFrameId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewLinIdData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewCanRemoteId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewCanDataId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewCanRDId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewCanIdData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewSpiData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewI2cNoAckInAdr.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewI2cFrame1.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewI2cFrame2.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewI2cRomData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewI2c10WriteFrame.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429Label.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429Sdi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429Ssm.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429LabelSdi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429LabelData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewArinc429LabelSsm.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewM1553bCSWord.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewM1553bRTAddr.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);
        viewM1553bDataWord.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.serialsDetail, viewUartData, fragmentTag[0])
                    .add(R.id.serialsDetail, viewUart0Data, fragmentTag[1])
                    .add(R.id.serialsDetail, viewUart1Data, fragmentTag[2])
                    .add(R.id.serialsDetail, viewUartxData, fragmentTag[3])
                    .add(R.id.serialsDetail, viewLinFrameId, fragmentTag[4])
                    .add(R.id.serialsDetail, viewLinIdData, fragmentTag[5])
                    .add(R.id.serialsDetail, viewCanRemoteId, fragmentTag[6])
                    .add(R.id.serialsDetail, viewCanDataId, fragmentTag[7])
                    .add(R.id.serialsDetail, viewCanRDId, fragmentTag[8])
                    .add(R.id.serialsDetail, viewCanIdData, fragmentTag[9])
                    .add(R.id.serialsDetail, viewSpiData, fragmentTag[10])
                    .add(R.id.serialsDetail, viewI2cNoAckInAdr, fragmentTag[11])
                    .add(R.id.serialsDetail, viewI2cFrame1, fragmentTag[12])
                    .add(R.id.serialsDetail, viewI2cFrame2, fragmentTag[13])
                    .add(R.id.serialsDetail, viewI2cRomData, fragmentTag[14])
                    .add(R.id.serialsDetail, viewI2c10WriteFrame, fragmentTag[15])
                    .add(R.id.serialsDetail, viewArinc429Label, fragmentTag[16])
                    .add(R.id.serialsDetail, viewArinc429Sdi, fragmentTag[17])
                    .add(R.id.serialsDetail, viewArinc429Data, fragmentTag[18])
                    .add(R.id.serialsDetail, viewArinc429Ssm, fragmentTag[19])
                    .add(R.id.serialsDetail, viewArinc429LabelSdi, fragmentTag[20])
                    .add(R.id.serialsDetail, viewArinc429LabelData, fragmentTag[21])
                    .add(R.id.serialsDetail, viewArinc429LabelSsm, fragmentTag[22])
                    .add(R.id.serialsDetail, viewM1553bCSWord, fragmentTag[23])
                    .add(R.id.serialsDetail, viewM1553bRTAddr, fragmentTag[24])
                    .add(R.id.serialsDetail, viewM1553bDataWord, fragmentTag[25])
                    .hide(viewUartData)
                    .hide(viewUart0Data)
                    .hide(viewUart1Data)
                    .hide(viewUartxData)
                    .hide(viewLinFrameId)
                    .hide(viewLinIdData)
                    .hide(viewCanRemoteId)
                    .hide(viewCanDataId)
                    .hide(viewCanRDId)
                    .hide(viewCanIdData)
                    .hide(viewSpiData)
                    .hide(viewI2cNoAckInAdr)
                    .hide(viewI2cFrame1)
                    .hide(viewI2cFrame2)
                    .hide(viewI2cRomData)
                    .hide(viewI2c10WriteFrame)
                    .hide(viewArinc429Label)
                    .hide(viewArinc429Sdi)
                    .hide(viewArinc429Data)
                    .hide(viewArinc429Ssm)
                    .hide(viewArinc429LabelSdi)
                    .hide(viewArinc429LabelData)
                    .hide(viewArinc429LabelSsm)
                    .hide(viewM1553bCSWord)
                    .hide(viewM1553bRTAddr)
                    .hide(viewM1553bDataWord)
                    .commitAllowingStateLoss();
        }
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    private void setCache() {
        int serials = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber());
        list.clear();
        int title;
        switch (serials) {
            case UART:
                list.addAll(uart);
                break;
            case LIN:
                list.addAll(lin);
                break;
            case CAN:
                list.addAll(can);
                break;
            case SPI:
                list.addAll(spi);
                break;
            case I2C:
                list.addAll(i2c);
                break;
            case ARINC429:
                list.addAll(m429);
                break;
            case M1553B:
                list.addAll(m1553b);
                break;
            default:
                list.addAll(uart);
                break;
        }
        title = CacheUtil.get().getInt(list.get(0).getCacheListKey());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSelected(i == title);
            if (i == title) {
                showDetail(list.get(i).getDetailFlag());
                setTriggerType(list, i);
                triggerDetail.setSerials(list.get(i));
                triggerDetail.getSerials().setRxMsgSelect(false);
                if (list.get(i).getDetailFlag() == NULL) {
                    triggerDetail.setSerialsDetail(null);
                    divider.setVisibility(View.GONE);
                } else {
                    triggerDetail.setSerialsDetail(getSerialsDetail(list.get(i).getDetailFlag()));
                    divider.setVisibility(View.GONE);
                }
                sendMsg(false);
            }
        }
        adapter.notifyDataSetChanged();


        int selectType= Tools.indexOf(list, Serials::isSelected);
        setCommand(serials,selectType);

    }



    private void setCommand(int serialType,int selectType){
        switch (serialType) {
            case UART: {
                int condition = Command.get().getTrigger_uart().getCondition(getSerialsNumber() - 1);
                int num = Command.get().getTrigger_uart().getNumber(getSerialsNumber() - 1);
                Command.get().getTrigger_uart().setType(getSerialsNumber() - 1, selectType, condition, num, false);
            }break;
            case LIN: {
                int id = Command.get().getTrigger_lin().getId(getSerialsNumber() - 1);
                long data = Command.get().getTrigger_lin().getData(getSerialsNumber() - 1);
                Command.get().getTrigger_lin().setType(getSerialsNumber() - 1, selectType, id, data, false);
            }break;
            case CAN: {
                int id = Command.get().getTrigger_can().getId(getSerialsNumber() - 1);
                int dlc=Command.get().getTrigger_can().getDlc(getSerialsNumber()-1);
                long data=Command.get().getTrigger_can().getData(getSerialsNumber()-1);
                Command.get().getTrigger_can().setType(getSerialsNumber() - 1, selectType, id, dlc, data, false);
            }break;
            case SPI: {
                Command.get().getTrigger_spi().Type(getSerialsNumber() - 1, selectType, false);
            }break;
            case I2C: {
                int addr=Command.get().getTrigger_iic().getAddr(getSerialsNumber()-1);
                int data1=Command.get().getTrigger_iic().getData1(getSerialsNumber()-1);
                int data2=Command.get().getTrigger_iic().getData2(getSerialsNumber()-1);
                int condition=Command.get().getTrigger_iic().getCondition(getSerialsNumber()-1);
                Command.get().getTrigger_iic().setType(getSerialsNumber() - 1, serialType, addr, data1, data2, condition, false);
            }break;
            case ARINC429: {
                int label=Command.get().getTrigger_m429().getLabel(getSerialsNumber()-1);
                int sdi=Command.get().getTrigger_m429().getSdi(getSerialsNumber()-1);
                long data=Command.get().getTrigger_m429().getData(getSerialsNumber()-1);
                int ssm= Command.get().getTrigger_m429().getSsm(getSerialsNumber()-1);
                Command.get().getTrigger_m429().setType(getSerialsNumber() - 1, serialType, label, sdi, data, ssm, false);
            }break;
            case M1553B: {
                int cs=Command.get().getTrigger_m1553B().getCsWord(getSerialsNumber()-1);
                int rt=Command.get().getTrigger_m1553B().getRtAddr(getSerialsNumber()-1);
                int data=Command.get().getTrigger_m1553B().getDataWord(getSerialsNumber()-1);
                Command.get().getTrigger_m1553B().setType(getSerialsNumber() - 1, serialType, cs, rt, data, false);
            }break;
//            default: {
//                int condition = Command.get().getTrigger_uart().getCondition(getSerialsNumber() - 1);
//                int num = Command.get().getTrigger_uart().getNumber(getSerialsNumber() - 1);
//                Command.get().getTrigger_uart().setType(getSerialsNumber() - 1, selectType, condition, num, false);
//            }break;
        }
    }

    public void setList(List<Serials> serialsList, int select, boolean isFromEventBus) {
        if (serialsList != null && serialsList.size() > 0) {
            list.clear();
            list.addAll(serialsList);
            adapter.notifyDataSetChanged();
            showDetail(list.get(select).getDetailFlag());
            CacheUtil.get().putMap(list.get(select).getCacheListKey(), String.valueOf(select));

            int serials = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber());
            int selectType= Tools.indexOf(list,s->s.isSelected());
            setCommand(serials,selectType);

            if (!isFromEventBus) {
                setTriggerType(list, select);
            }
            if (list.get(select).getDetailFlag() == NULL) {
                triggerDetail.setSerialsDetail(null);
                divider.setVisibility(View.GONE);
            } else {
                triggerDetail.setSerialsDetail(getSerialsDetail(list.get(select).getDetailFlag()));
                divider.setVisibility(View.GONE);
            }
            triggerDetail.setSerials(list.get(select));
            triggerDetail.getSerials().setRxMsgSelect(false);
            sendMsg(isFromEventBus);
        }
    }

    private void showDetail(int detailFlag) {
        detailLayoutSetGone();
        TopLayoutTriggerSerialsBaseDetail baseDetail = getFragmentForFlag(detailFlag);
        if (baseDetail != null) {
            getChildFragmentManager().beginTransaction().show(baseDetail).commitAllowingStateLoss();
        }
    }

    private TopLayoutTriggerSerialsBaseDetail getFragmentForFlag(int detailFlag) {
        TopLayoutTriggerSerialsBaseDetail baseDetail = null;
        switch (detailFlag) {
            case UART_DATA:
                baseDetail = viewUartData;
                break;
            case UART_0DATA:
                baseDetail = viewUart0Data;
                break;
            case UART_1DATA:
                baseDetail = viewUart1Data;
                break;
            case UART_XDATA:
                baseDetail = viewUartxData;
                break;
            case LIN_FRAMEID:
                baseDetail = viewLinFrameId;
                break;
            case LIN_IDDATA:
                baseDetail = viewLinIdData;
                break;
            case CAN_REMOTEID:
                baseDetail = viewCanRemoteId;
                break;
            case CAN_DATAID:
                baseDetail = viewCanDataId;
                break;
            case CAN_RDID:
                baseDetail = viewCanRDId;
                break;
            case CAN_IDDATA:
                baseDetail = viewCanIdData;
                break;
            case SPI_DATA:
                baseDetail = viewSpiData;
                break;
            case I2C_NOACKINADR:
                baseDetail = viewI2cNoAckInAdr;
                break;
            case I2C_FRAME1:
                baseDetail = viewI2cFrame1;
                break;
            case I2C_FRAME2:
                baseDetail = viewI2cFrame2;
                break;
            case I2C_ROMDATA:
                baseDetail = viewI2cRomData;
                break;
            case I2C_10WRITEFRAME:
                baseDetail = viewI2c10WriteFrame;
                break;
            case ARINC429_LABEL:
                baseDetail = viewArinc429Label;
                break;
            case ARINC429_SDI:
                baseDetail = viewArinc429Sdi;
                break;
            case ARINC429_DATA:
                baseDetail = viewArinc429Data;
                break;
            case ARINC429_SSM:
                baseDetail = viewArinc429Ssm;
                break;
            case ARINC429_LABELSDI:
                baseDetail = viewArinc429LabelSdi;
                break;
            case ARINC429_LABELDATA:
                baseDetail = viewArinc429LabelData;
                break;
            case ARINC429_LABELSSM:
                baseDetail = viewArinc429LabelSsm;
                break;
            case M1553B_CSWORD:
                baseDetail = viewM1553bCSWord;
                break;
            case M1553B_RTADDR:
                baseDetail = viewM1553bRTAddr;
                break;
            case M1553B_DATAWORD:
                baseDetail = viewM1553bDataWord;
                break;
            case NULL:
            default:
                break;
        }
        return baseDetail;
    }

    private void detailLayoutSetGone() {
        getChildFragmentManager().beginTransaction()
                .hide(viewUartData)
                .hide(viewUart0Data)
                .hide(viewUart1Data)
                .hide(viewUartxData)
                .hide(viewLinFrameId)
                .hide(viewLinIdData)
                .hide(viewCanRemoteId)
                .hide(viewCanDataId)
                .hide(viewCanRDId)
                .hide(viewCanIdData)
                .hide(viewSpiData)
                .hide(viewI2cNoAckInAdr)
                .hide(viewI2cFrame1)
                .hide(viewI2cFrame2)
                .hide(viewI2cRomData)
                .hide(viewI2c10WriteFrame)
                .hide(viewArinc429Label)
                .hide(viewArinc429Sdi)
                .hide(viewArinc429Data)
                .hide(viewArinc429Ssm)
                .hide(viewArinc429LabelSdi)
                .hide(viewArinc429LabelData)
                .hide(viewArinc429LabelSsm)
                .hide(viewM1553bCSWord)
                .hide(viewM1553bRTAddr)
                .hide(viewM1553bDataWord)
                .commitAllowingStateLoss();
    }

    public TopMsgTriggerSerials getTriggerDetail() {
        return triggerDetail;
    }

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener;
    }

    private void sendMsg(boolean isFromEventBus) {
        if (onDetailSendMsgListener != null) {
            onDetailSendMsgListener.onClick(this, isFromEventBus);
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            //由于本页面的初始化需要从TopLayoutTrigger页面获取参数之后才能开始初始化，所以移到本页面别的方法中去了...
        }
    };

    //region CommandToUI
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            int cmdCh=TChan.toFpgaChNo(serialsNumber);

            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_TRIGGERUART_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(uart.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int condition = Integer.parseInt(params[2]);
                        int number = Integer.parseInt(params[3]);
                        if (!list.get(type).isEnabled()) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 2:
                                ((TopLayoutTriggerSerialsUartData) viewUartData).setCommandData(condition, number, false);
                                break;
                            case 3:
                                ((TopLayoutTriggerSerialsUart0Data) viewUart0Data).setCommandData(condition, number, false);
                                break;
                            case 4:
                                ((TopLayoutTriggerSerialsUart1Data) viewUart1Data).setCommandData(condition, number, false);
                                break;
                            case 5:
                                ((TopLayoutTriggerSerialsUartxData) viewUartxData).setCommandData(condition, number, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(lin.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int id = Integer.parseInt(params[2]);
                        long data = Long.parseLong(params[3]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 1:
                                ((TopLayoutTriggerSerialsLinFrameId) viewLinFrameId).setCommandData(id, false);
                                break;
                            case 2:
                                ((TopLayoutTriggerSerialsLinIdData) viewLinIdData).setCommandData(id, data, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(can.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int id = Integer.parseInt(params[2]);
                        int dic = Integer.parseInt(params[3]);
                        long data = Long.parseLong(params[4]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 1:
                                ((TopLayoutTriggerSerialsCanRemoteId) viewCanRemoteId).setCommandData(id, false);
                                break;
                            case 2:
                                ((TopLayoutTriggerSerialsCanDataId) viewCanDataId).setCommandData(id, false);
                                break;
                            case 3:
                                ((TopLayoutTriggerSerialsCanRdId) viewCanRDId).setCommandData(id, false);
                                break;
                            case 4:
                                ((TopLayoutTriggerSerialsCanIdData) viewCanIdData).setCommandData(id, dic, data, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(spi.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int mask = Integer.parseInt(params[2]);
                        int data = Integer.parseInt(params[3]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 1:
                                String spiText = SerialsUtils.getSpiText(mask, data);
                                ((TopLayoutTriggerSerialsSpiData) viewSpiData).setCommandData(spiText, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERIIC_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(i2c.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int addr = Integer.parseInt(params[2]);
                        int data1 = Integer.parseInt(params[3]);
                        int data2 = Integer.parseInt(params[4]);
                        int condition = Integer.parseInt(params[5]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 4:
                                ((TopLayoutTriggerSerialsI2cNoAckInAdr) viewI2cNoAckInAdr).setCommandData(addr, false);
                                break;
                            case 5:
                                ((TopLayoutTriggerSerialsI2cFrame1) viewI2cFrame1).setCommandData(addr, data1, false);
                                break;
                            case 6:
                                ((TopLayoutTriggerSerialsI2cFrame2) viewI2cFrame2).setCommandData(addr, data1, data2, false);
                                break;
                            case 7:
                                ((TopLayoutTriggerSerialsI2cRomData) viewI2cRomData).setCommandData(data1, condition, false);
                                break;
                            case 8:
                                ((TopLayoutTriggerSerialsI2c10WriteFrame) viewI2c10WriteFrame).setCommandData(addr, data1, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(m429.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int label = Integer.parseInt(params[2]);
                        int sdi = Integer.parseInt(params[3]);
                        int data = Integer.parseInt(params[4]);
                        int ssm = Integer.parseInt(params[5]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 2:
                                ((TopLayoutTriggerSerialsArinc429Label) viewArinc429Label).setCommandData(label, false);
                                break;
                            case 3:
                                ((TopLayoutTriggerSerialsArinc429Sdi) viewArinc429Sdi).setCommandData(sdi, false);
                                break;
                            case 4:
                                ((TopLayoutTriggerSerialsArinc429Data) viewArinc429Data).setCommandData(data, false);
                                break;
                            case 5:
                                ((TopLayoutTriggerSerialsArinc429Ssm) viewArinc429Ssm).setCommandData(ssm, false);
                                break;
                            case 6:
                                ((TopLayoutTriggerSerialsArinc429LabelSdi) viewArinc429LabelSdi).setCommandData(label, sdi, false);
                                break;
                            case 7:
                                ((TopLayoutTriggerSerialsArinc429LabelData) viewArinc429LabelData).setCommandData(label, data, false);
                                break;
                            case 8:
                                ((TopLayoutTriggerSerialsArinc429LabelSsm) viewArinc429LabelSsm).setCommandData(label, ssm, false);
                                break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM1553B_TYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int serialsIndex = Integer.parseInt(params[0]);
                    if (serialsIndex ==cmdCh && list.get(0).getName().equals(m1553b.get(0).getName())) {
                        int type = Integer.parseInt(params[1]);
                        int csWord = Integer.parseInt(params[2]);
                        int rtAddr = Integer.parseInt(params[3]);
                        int dataWord = Integer.parseInt(params[4]);
                        if (list.get(type).isEnabled()==false) return;
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setSelected(type == i);
                        }
                        adapter.notifyDataSetChanged();
                        setSerialsItemClick(list, list.get(type), false);
                        switch (type) {
                            case 2:
                                ((TopLayoutTriggerSerialsM1553bCsWord) viewM1553bCSWord).setCommandData(csWord, false);
                                break;
                            case 3:
                                ((TopLayoutTriggerSerialsM1553bRtAddr) viewM1553bRTAddr).setCommandData(rtAddr, false);
                                break;
                            case 5:
                                ((TopLayoutTriggerSerialsM1553bDataWord) viewM1553bDataWord).setCommandData(dataWord, false);
                                break;
                        }
                    }
                    break;
                }
            }
        }
    };
    //endregion

    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_BUS_PARAM) {
                int select = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isSelected()) {
                        select = i;
                        break;
                    }
                }
                int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber);
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
                if(serialChannel == null) return;
                int clickable = -1;
                if (list.get(0).getName().equals(uart.get(0).getName())) {
                    UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
                    if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_START_BIT && select != 0) {
                        clickable = 0;
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_STOP_BIT && select != 1) {
                        clickable = 1;
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA) {
                        if (select != 2) {
                            clickable = 2;
                        }
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation());
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA);
                        ((TopLayoutTriggerSerialsUartData) viewUartData).setCommandData(condition, number, true);
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA0) {
                        if (select != 3) {
                            clickable = 3;
                        }
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation());
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA0);
                        ((TopLayoutTriggerSerialsUart0Data) viewUart0Data).setCommandData(condition, number, true);
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA1) {
                        if (select != 4) {
                            clickable = 4;
                        }
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation());
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA1);
                        ((TopLayoutTriggerSerialsUart1Data) viewUart1Data).setCommandData(condition, number, true);
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATAx) {
                        if (select != 5) {
                            clickable = 5;
                        }
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation());
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATAx);
                        ((TopLayoutTriggerSerialsUartxData) viewUartxData).setCommandData(condition, number, true);
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_OOD_EVEN_BIT_ERROR && select != 6) {
                        clickable = 6;
                    }
                } else if (list.get(0).getName().equals(lin.get(0).getName())) {
                    LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
                    int id = 0;
                    switch (linBus.getTriggerType()) {
                        case LinBus.LIN_TRIGGER_SYNC_RISING_EDGE:
                            if (select != 0) {
                                clickable = 0;
                            }
                            break;
                        case LinBus.LIN_TRIGGER_FRAME_ID:
                            if (select != 1) {
                                clickable = 1;
                            }
                            id = linBus.getFrameId(LinBus.LIN_TRIGGER_FRAME_ID);
                            ((TopLayoutTriggerSerialsLinFrameId) viewLinFrameId).setCommandData(id, true);
                            break;
                        case LinBus.LIN_TRIGGER_ID_AND_DATA:
                            if (select != 2) {
                                clickable = 2;
                            }
                            id = linBus.getFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA);
                            long data1 = linBus.getData();
                            ((TopLayoutTriggerSerialsLinIdData) viewLinIdData).setCommandData(id, data1, true);
                            break;
                        case LinBus.LIN_TRIGGER_PARITY_ERROR:
                            if (select != 3) {
                                clickable = 3;
                            }
                            break;
                        case LinBus.LIN_TRIGGER_CHECKSUM_ERROR:
                            if (select != 4) {
                                clickable = 4;
                            }
                            break;
                    }
                } else if (list.get(0).getName().equals(can.get(0).getName())) {
                    CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
                    if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_FRAME_START && select != 0) {
                        clickable = 0;
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_REMOTE_FRAME_ID) {
                        if (select != 1) {
                            clickable = 1;
                        }
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID);
                        ((TopLayoutTriggerSerialsCanRemoteId) viewCanRemoteId).setCommandData(id, true);
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_DATA_FRAME_ID) {
                        if (select != 2) {
                            clickable = 2;
                        }
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_DATA_FRAME_ID);
                        ((TopLayoutTriggerSerialsCanDataId) viewCanDataId).setCommandData(id, true);
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_REMOTE_DATA_ID) {
                        if (select != 3) {
                            clickable = 3;
                        }
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID);
                        ((TopLayoutTriggerSerialsCanRdId) viewCanRDId).setCommandData(id, true);
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ID_AND_DATA) {
                        if (select != 4) {
                            clickable = 4;
                        }
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA);
                        int dlc = canBus.getDlc();
                        long data1 = canBus.getData();
                        ((TopLayoutTriggerSerialsCanIdData) viewCanIdData).setCommandData(id, SerialsUtils.getCanDlcFromScope(dlc), data1, true);
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_WRONG_FRAME && select != 5) {
                        clickable = 5;
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ALL_ERROR && select != 6) {
                        clickable = 6;
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ACK_ERROR && select != 7) {
                        clickable = 7;
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_OVERLOAD_FRAME && select != 8) {
                        clickable = 8;
                    }
                } else if (list.get(0).getName().equals(spi.get(0).getName())) {
                    SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI);
                    if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_CS && select != 0) {
                        clickable = 0;
                    } else if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_DATA) {
                        if (select != 1) {
                            clickable = 1;
                        }
                        String spiText = SerialsUtils.getSpiText(spiBus.getTriggerMask(), spiBus.getTriggerData());
                        ((TopLayoutTriggerSerialsSpiData) viewSpiData).setCommandData(spiText, true);
                    } else if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_X_DATA && select != 2) {
                        clickable = 2;
                    }
                } else if (list.get(0).getName().equals(i2c.get(0).getName())) {
                    I2CBus i2CBus = (I2CBus) serialChannel.getBus(IBus.I2C);
                    if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_START_CONDITION && select != 0) {
                        clickable = 0;
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_STOP_CONDITION && select != 1) {
                        clickable = 1;
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_ACK_LOST && select != 2) {
                        clickable = 2;
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_RESTART && select != 3) {
                        clickable = 3;
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK) {
                        if (select != 4) {
                            clickable = 4;
                        }
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK);
                        ((TopLayoutTriggerSerialsI2cNoAckInAdr) viewI2cNoAckInAdr).setCommandData(addr, true);
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_FRAME1) {
                        if (select != 5) {
                            clickable = 5;
                        }
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_FRAME1);
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_FRAME1);
                        ((TopLayoutTriggerSerialsI2cFrame1) viewI2cFrame1).setCommandData(addr, data1, true);
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_FRAME2) {
                        if (select != 6) {
                            clickable = 6;
                        }
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_FRAME2);
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_FRAME2);
                        int data2 = i2CBus.getTriggerData2();
                        ((TopLayoutTriggerSerialsI2cFrame2) viewI2cFrame2).setCommandData(addr, data1, data2, true);
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_EEPROM_READ_DATA) {
                        if (select != 7) {
                            clickable = 7;
                        }
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA);
                        int condition = SerialsUtils.getConditionValueFromEventBus(i2CBus.getTriggerRelation());
                        ((TopLayoutTriggerSerialsI2cRomData) viewI2cRomData).setCommandData(data1, condition, true);
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_WRITE_FRAME) {
                        if (select != 8) {
                            clickable = 8;
                        }
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_WRITE_FRAME);
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME);
                        ((TopLayoutTriggerSerialsI2c10WriteFrame) viewI2c10WriteFrame).setCommandData(addr, data1, true);
                    }
                } else if (list.get(0).getName().equals(m429.get(0).getName())) {
                    ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
                    if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_BEGIN && select != 0) {
                        clickable = 0;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_END && select != 1) {
                        clickable = 1;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL) {
                        if (select != 2) {
                            clickable = 2;
                        }
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL);
                        ((TopLayoutTriggerSerialsArinc429Label) viewArinc429Label).setCommandData(label, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_SDI) {
                        if (select != 3) {
                            clickable = 3;
                        }
                        int sdi = a429Bus.getSdi(ARINC429Bus.ARINC429_TRIGGER_SDI);
                        ((TopLayoutTriggerSerialsArinc429Sdi) viewArinc429Sdi).setCommandData(sdi, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_DATA) {
                        if (select != 4) {
                            clickable = 4;
                        }
                        int data1 = a429Bus.getData(ARINC429Bus.ARINC429_TRIGGER_DATA);
                        ((TopLayoutTriggerSerialsArinc429Data) viewArinc429Data).setCommandData(data1, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_SSM) {
                        if (select != 5) {
                            clickable = 5;
                        }
                        int ssm = a429Bus.getSSM(ARINC429Bus.ARINC429_TRIGGER_SSM);
                        ((TopLayoutTriggerSerialsArinc429Ssm) viewArinc429Ssm).setCommandData(ssm, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI) {
                        if (select != 6) {
                            clickable = 6;
                        }
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI);
                        int sdi = a429Bus.getSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI);
                        ((TopLayoutTriggerSerialsArinc429LabelSdi) viewArinc429LabelSdi).setCommandData(label, sdi, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA) {
                        if (select != 7) {
                            clickable = 7;
                        }
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA);
                        int data1 = a429Bus.getData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA);
                        ((TopLayoutTriggerSerialsArinc429LabelData) viewArinc429LabelData).setCommandData(label, data1, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM) {
                        if (select != 8) {
                            clickable = 8;
                        }
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM);
                        int ssm = a429Bus.getSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM);
                        ((TopLayoutTriggerSerialsArinc429LabelSsm) viewArinc429LabelSsm).setCommandData(label, ssm, true);
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_ERROR && select != 9) {
                        clickable = 9;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_INTERVAL && select != 10) {
                        clickable = 10;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_VERIFY_ERROR && select != 11) {
                        clickable = 11;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_ERROR && select != 12) {
                        clickable = 12;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_0 && select != 13) {
                        clickable = 13;
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_1 && select != 14) {
                        clickable = 14;
                    }
                } else if (list.get(0).getName().equals(m1553b.get(0).getName())) {
                    MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
                    if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC && select != 0) {
                        clickable = 0;
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD_SYNC && select != 1) {
                        clickable = 1;
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD) {
                        if (select != 2) {
                            clickable = 2;
                        }
                        int csWord = m1553bBus.getCmdStatus();
                        ((TopLayoutTriggerSerialsM1553bCsWord) viewM1553bCSWord).setCommandData(csWord, true);
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS) {
                        if (select != 3) {
                            clickable = 3;
                        }
                        int rtAddr = m1553bBus.getAddr();
                        ((TopLayoutTriggerSerialsM1553bRtAddr) viewM1553bRTAddr).setCommandData(rtAddr, true);
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_MANCHESTER_ERROR && select != 4) {
                        clickable = 4;
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD) {
                        if (select != 5) {
                            clickable = 5;
                        }
                        int dataWord = m1553bBus.getData();
                        ((TopLayoutTriggerSerialsM1553bDataWord) viewM1553bDataWord).setCommandData(dataWord, true);
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_ODD_PARITY_ERROR && select != 6) {
                        clickable = 6;
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_ALL_ERROR && select != 7) {
                        clickable = 7;
                    }
                }
                if (clickable != -1) {
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setSelected(i == clickable);
                    }
                    adapter.notifyDataSetChanged();
                    setSerialsItemClick(list, list.get(clickable), true);
                }
            }
        }
    };

    //region onSerialsItemClickListener
    private SerialsAdapter.OnSerialsItemClickListener onSerialsItemClickListener = new SerialsAdapter.OnSerialsItemClickListener() {
        @Override
        public void itemClick(List<Serials> serialsList, Serials serials) {
            PlaySound.getInstance().playButton();
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true));
            setSerialsItemClick(serialsList, serials, false);
        }
    };
    //endregion

    private void setSerialsItemClick(List<Serials> serialsList, Serials serials, boolean isFromEventBus) {
        int cmdCh=TChan.toFpgaChNo(serialsNumber);

        if (serials.isEnabled()) {
            for (int i = 0; i < serialsList.size(); i++) {
                if (serialsList.get(i).getId() == serials.getId()) {
                    CacheUtil.get().putMap(serials.getCacheListKey(), String.valueOf(i));
                    showDetail(serialsList.get(i).getDetailFlag());
                    if (serialsList.get(i).getDetailFlag() == NULL) {
                        divider.setVisibility(View.GONE);
                        triggerDetail.setSerialsDetail(null);
                        if (serialsList.get(0).getName().equals(uart.get(0).getName())) {
                            Command.get().getTrigger_uart().setType(cmdCh, i, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(lin.get(0).getName())) {
                            Command.get().getTrigger_lin().setType(cmdCh, i, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(can.get(0).getName())) {
                            Command.get().getTrigger_can().setType(cmdCh, i, 0, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(spi.get(0).getName())) {
                            Command.get().getTrigger_spi().Type(cmdCh,i,false);
                            Command.get().getBus_spi().setType(cmdCh, i, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(i2c.get(0).getName())) {
                            Command.get().getTrigger_iic().setType(cmdCh, i, 0, 0, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(m429.get(0).getName())) {
                            Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, 0, 0, false);
                        } else if (serialsList.get(0).getName().equals(m1553b.get(0).getName())) {
                            Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, 0, 0, false);
                        }
                    } else {
                        divider.setVisibility(View.GONE);
                        triggerDetail.setSerialsDetail(getSerialsDetail(serialsList.get(i).getDetailFlag()));
                        int uartDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                        int linDigits = IDigits.DIGITS_16;
                        int canDigits = IDigits.DIGITS_16;
                        int i2cDigits = IDigits.DIGITS_16;
                        int m429LabelDigits = IDigits.DIGITS_8;
                        int m429SdiDigits = IDigits.DIGITS_2;
                        int m429DataDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                        int m429SsmDigits = IDigits.DIGITS_2;
                        int m1553bDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                        switch (serialsList.get(i).getDetailFlag()) {
                            case UART_DATA: {
                                SerialsDetailUartData serialsDetail = (SerialsDetailUartData) triggerDetail.getSerialsDetail();
                                int condition = serialsDetail.getUartDataCondition().getIndex();
                                DataBean data = serialsDetail.getUartDataEdit();
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false);
                                break;
                            }
                            case UART_0DATA: {
                                SerialsDetailUart0Data serialsDetail = (SerialsDetailUart0Data) triggerDetail.getSerialsDetail();
                                int condition = serialsDetail.getUart0DataCondition().getIndex();
                                DataBean data = serialsDetail.getUart0DataEdit();
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false);
                                break;
                            }
                            case UART_1DATA: {
                                SerialsDetailUart1Data serialsDetail = (SerialsDetailUart1Data) triggerDetail.getSerialsDetail();
                                int condition = serialsDetail.getUart1DataCondition().getIndex();
                                DataBean data = serialsDetail.getUart1DataEdit();
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false);
                                break;
                            }
                            case UART_XDATA: {
                                SerialsDetailUartxData serialsDetail = (SerialsDetailUartxData) triggerDetail.getSerialsDetail();
                                int condition = serialsDetail.getUartxDataCondition().getIndex();
                                DataBean data = serialsDetail.getUartxDataEdit();
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false);
                                break;
                            }
                            case LIN_FRAMEID: {
                                SerialsDetailLinFrameId serialsDetail = (SerialsDetailLinFrameId) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getLinFrameIdEditEdit();
                                Command.get().getTrigger_lin().setType(cmdCh, i, SerialsUtils.toD(id.getValue(), linDigits), 0, false);
                                break;
                            }
                            case LIN_IDDATA: {
                                SerialsDetailLinIdData serialsDetail = (SerialsDetailLinIdData) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getLinIdDataId();
                                DataBean data = serialsDetail.getLinIdDataData();
                                if (id.getValue().contains("X")) {
                                    Command.get().getTrigger_lin().setType(cmdCh, i, getMatchFid(id.getValue(), data.getValue()), SerialsUtils.toDLong(data.getValue(), IDigits.DIGITS_16), false);
                                } else {
                                    Command.get().getTrigger_lin().setType(cmdCh, i, SerialsUtils.toD(id.getValue(), linDigits), SerialsUtils.toDLong(data.getValue(), IDigits.DIGITS_16), false);
                                }
                                break;
                            }
                            case CAN_REMOTEID: {
                                SerialsDetailCanRemoteId serialsDetail = (SerialsDetailCanRemoteId) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getCanRemoteIdEdit();
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false);
                                break;
                            }
                            case CAN_DATAID: {
                                SerialsDetailCanDataId serialsDetail = (SerialsDetailCanDataId) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getCanDataIdEdit();
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false);
                                break;
                            }
                            case CAN_RDID: {
                                SerialsDetailCanRdId serialsDetail = (SerialsDetailCanRdId) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getCanRdIdEdit();
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false);
                                break;
                            }
                            case CAN_IDDATA: {
                                SerialsDetailCanIdData serialsDetail = (SerialsDetailCanIdData) triggerDetail.getSerialsDetail();
                                DataBean id = serialsDetail.getCanIdDataId();
                                DataBean dlc = serialsDetail.getCanIdDataDlc();
                                DataBean data = serialsDetail.getCanIdDataData();
                                int iDlc=Integer.parseInt(dlc.getValue());
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits)
                                        ,iDlc , SerialsUtils.toDLong(data.getValue(), canDigits), false);
                                break;
                            }
                            case SPI_DATA: {
                                SerialsDetailSpiData serialsDetail = (SerialsDetailSpiData) triggerDetail.getSerialsDetail();
                                DataBean data = serialsDetail.getSpiDataData();
                                long triggerMask = SerialsUtils.toDLong(SerialsUtils.getSpiMask(data.getValue()), IDigits.DIGITS_2);
                                long triggerData = SerialsUtils.toDLong(SerialsUtils.getSpiData(data.getValue()), IDigits.DIGITS_2);

                                Command.get().getTrigger_spi().Type(cmdCh,i,false);
                                Command.get().getTrigger_spi().Data(cmdCh,data.getValue(),false);
                                Command.get().getBus_spi().setType(cmdCh, i, (int) triggerMask, (int) triggerData, false);
                                break;
                            }
                            case I2C_NOACKINADR: {
                                SerialsDetailI2cNoAckInAdr serialsDetail = (SerialsDetailI2cNoAckInAdr) triggerDetail.getSerialsDetail();
                                DataBean addr = serialsDetail.getI2cNoAckInAdrData();
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), 0, 0, 0, false);
                                break;
                            }
                            case I2C_FRAME1: {
                                SerialsDetailI2cFrame1 serialsDetail = (SerialsDetailI2cFrame1) triggerDetail.getSerialsDetail();
                                DataBean addr = serialsDetail.getI2cFrame1Addr();
                                DataBean data = serialsDetail.getI2cFrame1Data();
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), SerialsUtils.toD(data.getValue(), i2cDigits), 0, 0, false);
                                break;
                            }
                            case I2C_FRAME2: {
                                SerialsDetailI2cFrame2 serialsDetail = (SerialsDetailI2cFrame2) triggerDetail.getSerialsDetail();
                                DataBean addr = serialsDetail.getI2cFrame2Addr();
                                DataBean data = serialsDetail.getI2cFrame2Data1();
                                DataBean data2 = serialsDetail.getI2cFrame2Data2();
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits)
                                        , SerialsUtils.toD(data.getValue(), i2cDigits), SerialsUtils.toD(data2.getValue(), i2cDigits), 0, false);
                                break;
                            }
                            case I2C_ROMDATA: {
                                SerialsDetailI2cRomData serialsDetail = (SerialsDetailI2cRomData) triggerDetail.getSerialsDetail();
                                int condition = serialsDetail.getI2cRomDataCondition().getIndex();
                                DataBean data = serialsDetail.getI2cRomDataData();
                                Command.get().getTrigger_iic().setType(cmdCh, i, 0, SerialsUtils.toD(data.getValue(), i2cDigits), 0, condition, false);
                                break;
                            }
                            case I2C_10WRITEFRAME: {
                                SerialsDetailI2c10WriteFrame serialsDetail = (SerialsDetailI2c10WriteFrame) triggerDetail.getSerialsDetail();
                                DataBean addr = serialsDetail.getI2c10WriteFrameAddr();
                                DataBean data = serialsDetail.getI2c10WriteFrameData();
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), SerialsUtils.toD(data.getValue(), i2cDigits), 0, 0, false);
                                break;
                            }
                            case ARINC429_LABEL: {
                                SerialsDetailArinc429Label serialsDetail = (SerialsDetailArinc429Label) triggerDetail.getSerialsDetail();
                                DataBean label = serialsDetail.getArinc429LabelLabel();
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits), 0, 0, 0, false);
                                break;
                            }
                            case ARINC429_SDI: {
                                SerialsDetailArinc429Sdi serialsDetail = (SerialsDetailArinc429Sdi) triggerDetail.getSerialsDetail();
                                DataBean sdi = serialsDetail.getArinc429SdiLabel();
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, SerialsUtils.toD(sdi.getValue(), m429SdiDigits), 0, 0, false);
                                break;
                            }
                            case ARINC429_DATA: {
                                SerialsDetailArinc429Data serialsDetail = (SerialsDetailArinc429Data) triggerDetail.getSerialsDetail();
                                DataBean data = serialsDetail.getArinc429DataData();
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, SerialsUtils.toD(data.getValue(), m429DataDigits), 0, false);
                                break;
                            }
                            case ARINC429_SSM: {
                                SerialsDetailArinc429Ssm serialsDetail = (SerialsDetailArinc429Ssm) triggerDetail.getSerialsDetail();
                                DataBean ssm = serialsDetail.getArinc429SsmLabel();
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, 0, SerialsUtils.toD(ssm.getValue(), m429SsmDigits), false);
                                break;
                            }
                            case ARINC429_LABELSDI: {
                                SerialsDetailArinc429LabelSdi serialsDetail = (SerialsDetailArinc429LabelSdi) triggerDetail.getSerialsDetail();
                                DataBean label = serialsDetail.getArinc429LabelSdiLabel();
                                DataBean sdi = serialsDetail.getArinc429LabelSdiSdi();
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits)
                                        , SerialsUtils.toD(sdi.getValue(), m429SdiDigits), 0, 0, false);
                                break;
                            }
                            case ARINC429_LABELDATA: {
                                SerialsDetailArinc429LabelData serialsDetail = (SerialsDetailArinc429LabelData) triggerDetail.getSerialsDetail();
                                DataBean label = serialsDetail.getArinc429LabelDataLabel();
                                DataBean data = serialsDetail.getArinc429LabelDataData();
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits)
                                        , 0, SerialsUtils.toD(data.getValue(), m429DataDigits), 0, false);
                                break;
                            }
                            case ARINC429_LABELSSM: {
                                SerialsDetailArinc429LabelSsm serialsDetail = (SerialsDetailArinc429LabelSsm) triggerDetail.getSerialsDetail();
                                DataBean label = serialsDetail.getArinc429LabelSsmLabel();
                                DataBean ssm = serialsDetail.getArinc429LabelSsmSsm();
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits)
                                        , 0, 0, SerialsUtils.toD(ssm.getValue(), m429SsmDigits), false);
                                break;
                            }
                            case M1553B_CSWORD: {
                                SerialsDetailM1553bCsWord serialsDetail = (SerialsDetailM1553bCsWord) triggerDetail.getSerialsDetail();
                                DataBean csWord = serialsDetail.getM1553bCsWordCsWord();
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, SerialsUtils.toD(csWord.getValue(), m1553bDigits), 0, 0, false);
                                break;
                            }
                            case M1553B_RTADDR: {
                                SerialsDetailM1553bRtAddr serialsDetail = (SerialsDetailM1553bRtAddr) triggerDetail.getSerialsDetail();
                                DataBean rtAddr = serialsDetail.getM1553bRtAddrRtAddr();
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, SerialsUtils.toD(rtAddr.getValue(), m1553bDigits), 0, false);
                                break;
                            }
                            case M1553B_DATAWORD: {
                                SerialsDetailM1553bDataWord serialsDetail = (SerialsDetailM1553bDataWord) triggerDetail.getSerialsDetail();
                                DataBean dataWord = serialsDetail.getM1553bDataWordData();
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, 0, SerialsUtils.toD(dataWord.getValue(), m1553bDigits), false);
                                break;
                            }
                        }
                    }
                    triggerDetail.setSerials(serials);
                    sendMsg(isFromEventBus);

                    if (!isFromEventBus) {
                        setTriggerType(serialsList, i);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 设置底层数据
     *
     * @param curSerialsList 当前type列表
     * @param index          当前选中项序号
     */
    private void setTriggerType(List<Serials> curSerialsList, int index) {
        int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        if (curSerialsList.get(0).getName().equals(uart.get(0).getName())) {
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART);
            switch (index) {
                case 0:
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_START_BIT);
                    break;
                case 1:
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_STOP_BIT);
                    break;
                case 2: {
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA);
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + getSerialsNumber());
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition));
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA, SerialsUtils.toD(edit, digits));
                }
                break;
                case 3: {
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA0);
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + getSerialsNumber());
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition));
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA0, SerialsUtils.toD(edit, digits));
                }
                break;
                case 4: {
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA1);
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber());
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition));
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, SerialsUtils.toD(edit, digits));
                }
                break;
                case 5: {
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATAx);
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + getSerialsNumber());
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition));
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATAx, SerialsUtils.toD(edit, digits));
                }
                break;
                case 6:
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_OOD_EVEN_BIT_ERROR);
                    break;
            }
        } else if (curSerialsList.get(0).getName().equals(lin.get(0).getName())) {
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);
            switch (index) {
                case 0:
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_SYNC_RISING_EDGE);
                    break;
                case 1:
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_FRAME_ID);
                    String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber());
                    linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, SerialsUtils.toD(linFrameIdEdit, IDigits.DIGITS_16));
                    break;
                case 2:
                    String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber());
                    if (dataID.contains("X")) {//代表单独数据触发，设置触发方式
                        linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA);
                    } else {
                        linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA);
                    }
                    String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber());
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, SerialsUtils.toD(id, IDigits.DIGITS_16));
                    linBus.setData(SerialsUtils.toDLong(data, IDigits.DIGITS_16));
                    break;
                case 3:
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_PARITY_ERROR);
                    break;
                case 4:
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_CHECKSUM_ERROR);
                    break;
            }
        } else if (curSerialsList.get(0).getName().equals(can.get(0).getName())) {
            CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN);
            switch (index) {
                case 0:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_FRAME_START);
                    break;
                case 1:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID);
                    String remoteId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + getSerialsNumber());
                    canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID, SerialsUtils.toD(remoteId, IDigits.DIGITS_16));
                    break;
                case 2:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_DATA_FRAME_ID);
                    String dataId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + getSerialsNumber());
                    canBus.setFrameId(CanBus.CAN_TRIGGER_DATA_FRAME_ID, SerialsUtils.toD(dataId, IDigits.DIGITS_16));
                    break;
                case 3:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_REMOTE_DATA_ID);
                    String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber());
                    canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, SerialsUtils.toD(rdId, IDigits.DIGITS_16));
                    break;
                case 4:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ID_AND_DATA);
                    String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber());
                    String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber());
                    canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, SerialsUtils.toD(id, IDigits.DIGITS_16));
                    canBus.setDlc(SerialsUtils.getCanDlcFromShow(dlc));
                    canBus.setData(SerialsUtils.toDLong(data, IDigits.DIGITS_16));
                    break;
                case 5:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_WRONG_FRAME);
                    break;
                case 6:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ALL_ERROR);
                    break;
                case 7:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ACK_ERROR);
                    break;
                case 8:
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_OVERLOAD_FRAME);
                    break;
            }
        } else if (curSerialsList.get(0).getName().equals(spi.get(0).getName())) {
            SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI);
            switch (index) {
                case 0:
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_CS);
                    break;
                case 1:
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_DATA);
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber());
                    spiBus.setTriggerMask((int) SerialsUtils.toDLong(SerialsUtils.getSpiMask(data), IDigits.DIGITS_2));
                    spiBus.setTriggerData((int) SerialsUtils.toDLong(SerialsUtils.getSpiData(data), IDigits.DIGITS_2));
                    break;
                case 2:
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_X_DATA);
                    break;
            }
        } else if (curSerialsList.get(0).getName().equals(i2c.get(0).getName())) {
            I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
            switch (index) {
                case 0:
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_START_CONDITION);
                    break;
                case 1:
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_STOP_CONDITION);
                    break;
                case 2:
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_ACK_LOST);
                    break;
                case 3:
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_RESTART);
                    break;
                case 4: {
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK);
                    String noAckInAdr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + getSerialsNumber());
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK, SerialsUtils.toD(noAckInAdr, IDigits.DIGITS_16));
                }
                break;
                case 5: {
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_FRAME1);
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + getSerialsNumber());
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME1, SerialsUtils.toD(addr, IDigits.DIGITS_16));
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME1, SerialsUtils.toD(data, IDigits.DIGITS_16));
                }
                break;
                case 6: {
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_FRAME2);
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber());
                    String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber());
                    String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber());
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, SerialsUtils.toD(addr, IDigits.DIGITS_16));
                    i2cBus.setTriggerData1(SerialsUtils.toD(data1, IDigits.DIGITS_16));
                    i2cBus.setTriggerData2(SerialsUtils.toD(data2, IDigits.DIGITS_16));
                }
                break;
                case 7: {
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA);
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber());
                    i2cBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition));
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, SerialsUtils.toD(data, IDigits.DIGITS_16));
                }
                break;
                case 8: {
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_WRITE_FRAME);
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + getSerialsNumber());
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_WRITE_FRAME, SerialsUtils.toD(addr, IDigits.DIGITS_16));
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME, SerialsUtils.toD(data, IDigits.DIGITS_16));
                }
                break;
            }
        } else if (curSerialsList.get(0).getName().equals(m429.get(0).getName())) {
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429);
            switch (index) {
                case 0:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_BEGIN);
                    break;
                case 1:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_END);
                    break;
                case 2: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL);
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber());
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, SerialsUtils.toD(label, IDigits.DIGITS_8));
                }
                break;
                case 3: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_SDI);
                    String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SDI + getSerialsNumber());
                    a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_SDI, SerialsUtils.toD(sdi, IDigits.DIGITS_2));
                }
                break;
                case 4: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_DATA);
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    data = SerialsUtils.reCalcSpace(SerialsUtils.HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
                    a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_DATA, SerialsUtils.toD(data, digits));
                }
                break;
                case 5: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_SSM);
                    String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SSM + getSerialsNumber());
                    a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_SSM, SerialsUtils.toD(ssm, IDigits.DIGITS_2));
                }
                break;
                case 6: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI);
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber());
                    String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber());
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, SerialsUtils.toD(label, IDigits.DIGITS_8));
                    a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, SerialsUtils.toD(sdi, IDigits.DIGITS_2));
                }
                break;
                case 7: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA);
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + getSerialsNumber());
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    data = SerialsUtils.reCalcSpace(SerialsUtils.HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits);
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, SerialsUtils.toD(label, IDigits.DIGITS_8));
                    a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, SerialsUtils.toD(data, digits));
                }
                break;
                case 8: {
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM);
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + getSerialsNumber());
                    String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + getSerialsNumber());
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, SerialsUtils.toD(label, IDigits.DIGITS_8));
                    a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, SerialsUtils.toD(ssm, IDigits.DIGITS_2));
                }
                break;
                case 9:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_ERROR);
                    break;
                case 10:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_INTERVAL);
                    break;
                case 11:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_VERIFY_ERROR);
                    break;
                case 12:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_ERROR);
                    break;
                case 13:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_0);
                    break;
                case 14:
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_1);
                    break;
            }
        } else if (curSerialsList.get(0).getName().equals(m1553b.get(0).getName())) {
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B);
            switch (index) {
                case 0:
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC);
                    break;
                case 1:
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD_SYNC);
                    break;
                case 2: {
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD);
                    String csWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    m1553bBus.setCmdStatus(SerialsUtils.toD(csWord, digits));
                }
                break;
                case 3: {
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS);
                    String rtAddr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    m1553bBus.setAddr(SerialsUtils.toD(rtAddr, digits));
                }
                break;
                case 4:
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_MANCHESTER_ERROR);
                    break;
                case 5: {
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD);
                    String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber());
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                    m1553bBus.setData(SerialsUtils.toD(dataWord, digits));
                }
                break;
                case 6:
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_ODD_PARITY_ERROR);
                    break;
                case 7:
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_ALL_ERROR);
                    break;
            }
        }
    }

    private ISerialsDetail getSerialsDetail(int detailFlag) {
        TopLayoutTriggerSerialsBaseDetail layout = getFragmentForFlag(detailFlag);
        return layout.getSerialsDetail(detailFlag);
    }

    private TopLayoutTriggerSerialsBaseDetail.OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener = new TopLayoutTriggerSerialsBaseDetail.OnSerialsDetailSendMsgListener() {
        @Override
        public void onClick(Fragment detail, ISerialsDetail serialsDetail, boolean isFromEventBus) {
            if (adapter.getSelected() != null) {
                triggerDetail.setSerials(adapter.getSelected());
                triggerDetail.getSerials().setRxMsgSelect(false);
                triggerDetail.setSerialsDetail(serialsDetail);
                sendMsg(isFromEventBus);
            }
        }
    };

    private int getMatchFid(String id, String data) {
        int dataLength = data.replace(" ", "").trim().length();
        if (id.contains("X")) {
            if (dataLength <= 4) {
                id = "1F";
            } else if (dataLength <= 8) {
                id = "2F";
            } else {
                id = "3F";
            }
        }
        return SerialsUtils.toD(id, 16);
    }

}
