package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

import android.content.Context; // Context
import android.os.Bundle; // Bundle
import android.view.LayoutInflater; // LayoutInflater
import android.view.View; // View
import android.view.ViewGroup; // ViewGroup
import android.widget.LinearLayout; // LinearLayout

import androidx.annotation.Nullable; // Nullable
import androidx.fragment.app.Fragment; // Fragment
import androidx.recyclerview.widget.GridLayoutManager; // GridLayoutManager
import androidx.recyclerview.widget.RecyclerView; // RecyclerView

import com.micsig.tbook.scope.Bus.ARINC429Bus; // ARINC429Bus
import com.micsig.tbook.scope.Bus.CanBus; // CanBus
import com.micsig.tbook.scope.Bus.I2CBus; // I2CBus
import com.micsig.tbook.scope.Bus.IBus; // IBus
import com.micsig.tbook.scope.Bus.LinBus; // LinBus
import com.micsig.tbook.scope.Bus.MILSTD1553BBus; // MILSTD1553BBus
import com.micsig.tbook.scope.Bus.SpiBus; // SpiBus
import com.micsig.tbook.scope.Bus.UartBus; // UartBus
import com.micsig.tbook.scope.Event.EventBase; // EventBase
import com.micsig.tbook.scope.Event.EventFactory; // EventFactory
import com.micsig.tbook.scope.Event.EventUIObserver; // EventUIObserver
import com.micsig.tbook.scope.channel.ChannelFactory; // ChannelFactory
import com.micsig.tbook.scope.channel.SerialChannel; // SerialChannel
import com.micsig.tbook.tbookscope.LoadCache; // LoadCache
import com.micsig.tbook.tbookscope.R; // R
import com.micsig.tbook.tbookscope.middleware.command.Command; // Command
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // CommandMsgToUI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxBus
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxEnum
import com.micsig.tbook.tbookscope.tools.PlaySound; // PlaySound
import com.micsig.tbook.tbookscope.tools.Tools; // Tools
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // OnDetailSendMsgListener
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.DataBean; // DataBean
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // ISerialsDetail
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Data; // SerialsDetailArinc429Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label; // SerialsDetailArinc429Label
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelData; // SerialsDetailArinc429LabelData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi; // SerialsDetailArinc429LabelSdi
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSsm; // SerialsDetailArinc429LabelSsm
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Sdi; // SerialsDetailArinc429Sdi
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Ssm; // SerialsDetailArinc429Ssm
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanDataId; // SerialsDetailCanDataId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData; // SerialsDetailCanIdData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId; // SerialsDetailCanRdId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRemoteId; // SerialsDetailCanRemoteId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2c10WriteFrame; // SerialsDetailI2c10WriteFrame
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame1; // SerialsDetailI2cFrame1
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2; // SerialsDetailI2cFrame2
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cNoAckInAdr; // SerialsDetailI2cNoAckInAdr
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData; // SerialsDetailI2cRomData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId; // SerialsDetailLinFrameId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData; // SerialsDetailLinIdData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bCsWord; // SerialsDetailM1553bCsWord
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord; // SerialsDetailM1553bDataWord
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bRtAddr; // SerialsDetailM1553bRtAddr
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData; // SerialsDetailSpiData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart0Data; // SerialsDetailUart0Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data; // SerialsDetailUart1Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartData; // SerialsDetailUartData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUartxData; // SerialsDetailUartxData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Data; // TopLayoutTriggerSerialsArinc429Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Label; // TopLayoutTriggerSerialsArinc429Label
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelData; // TopLayoutTriggerSerialsArinc429LabelData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelSdi; // TopLayoutTriggerSerialsArinc429LabelSdi
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429LabelSsm; // TopLayoutTriggerSerialsArinc429LabelSsm
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Sdi; // TopLayoutTriggerSerialsArinc429Sdi
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsArinc429Ssm; // TopLayoutTriggerSerialsArinc429Ssm
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsBaseDetail; // TopLayoutTriggerSerialsBaseDetail
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanDataId; // TopLayoutTriggerSerialsCanDataId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanIdData; // TopLayoutTriggerSerialsCanIdData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanRdId; // TopLayoutTriggerSerialsCanRdId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsCanRemoteId; // TopLayoutTriggerSerialsCanRemoteId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2c10WriteFrame; // TopLayoutTriggerSerialsI2c10WriteFrame
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cFrame1; // TopLayoutTriggerSerialsI2cFrame1
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cFrame2; // TopLayoutTriggerSerialsI2cFrame2
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cNoAckInAdr; // TopLayoutTriggerSerialsI2cNoAckInAdr
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsI2cRomData; // TopLayoutTriggerSerialsI2cRomData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsLinFrameId; // TopLayoutTriggerSerialsLinFrameId
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsLinIdData; // TopLayoutTriggerSerialsLinIdData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bCsWord; // TopLayoutTriggerSerialsM1553bCsWord
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bDataWord; // TopLayoutTriggerSerialsM1553bDataWord
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsM1553bRtAddr; // TopLayoutTriggerSerialsM1553bRtAddr
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsSpiData; // TopLayoutTriggerSerialsSpiData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUart0Data; // TopLayoutTriggerSerialsUart0Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUart1Data; // TopLayoutTriggerSerialsUart1Data
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUartData; // TopLayoutTriggerSerialsUartData
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment.TopLayoutTriggerSerialsUartxData; // TopLayoutTriggerSerialsUartxData
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // IDigits
import com.micsig.tbook.tbookscope.util.CacheUtil; // CacheUtil
import com.micsig.tbook.ui.wavezone.TChan; // TChan

import java.util.ArrayList; // ArrayList
import java.util.List; // List

import io.reactivex.rxjava3.annotations.NonNull; // NonNull
import io.reactivex.rxjava3.functions.Consumer; // Consumer


/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            TopLayoutTriggerSerials（串行触发主布局Fragment）                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/TopLayoutTriggerSerials.java                               ║
 * ║ 核心职责: 管理串行协议选择列表和详情子Fragment的切换                              ║
 * ║ 架构设计: Fragment + 子Fragment架构，左侧协议列表+右侧详情面板                    ║
 * ║ 数据流向: 协议列表选择 → 详情Fragment → Bean → Command → 底层硬件               ║
 * ║ 依赖关系: 聚合SerialsAdapter/TopMsgTriggerSerials/26种详情Fragment              ║
 * ║ 使用场景: 示波器串行触发功能的主入口界面                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by Administrator on 2017/4/10.
 */
public class TopLayoutTriggerSerials extends Fragment implements SerialsDetailFlag { // 代码块开始
    private Context context; // context
    private LinearLayout serialsDetail; // serialsDetail
    private View divider; // divider
    private List<Serials> list = new ArrayList<>(); // ArrayList<>()
    private int serialsNumber; // serialsNumber
    private SerialsAdapter adapter; // adapter

    private TopMsgTriggerSerials triggerDetail; // triggerDetail
    private OnDetailSendMsgListener onDetailSendMsgListener; // onDetailSendMsgListener

    private TopLayoutTriggerSerialsBaseDetail fragments[] = new TopLayoutTriggerSerialsBaseDetail[26]; // TopLayoutTriggerSerialsBaseDetail[26]
    private String fragmentTag[] = { // {
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
    }; // 执行语句

    private TopLayoutTriggerSerialsBaseDetail viewUartData; // viewUartData
    private TopLayoutTriggerSerialsBaseDetail viewUart0Data; // viewUart0Data
    private TopLayoutTriggerSerialsBaseDetail viewUart1Data; // viewUart1Data
    private TopLayoutTriggerSerialsBaseDetail viewUartxData; // viewUartxData
    private TopLayoutTriggerSerialsBaseDetail viewLinFrameId; // viewLinFrameId
    private TopLayoutTriggerSerialsBaseDetail viewLinIdData; // viewLinIdData
    private TopLayoutTriggerSerialsBaseDetail viewCanRemoteId; // viewCanRemoteId
    private TopLayoutTriggerSerialsBaseDetail viewCanDataId; // viewCanDataId
    private TopLayoutTriggerSerialsBaseDetail viewCanRDId; // viewCanRDId
    private TopLayoutTriggerSerialsBaseDetail viewCanIdData; // viewCanIdData
    private TopLayoutTriggerSerialsBaseDetail viewSpiData; // viewSpiData
    private TopLayoutTriggerSerialsBaseDetail viewI2cNoAckInAdr; // viewI2cNoAckInAdr
    private TopLayoutTriggerSerialsBaseDetail viewI2cFrame1; // viewI2cFrame1
    private TopLayoutTriggerSerialsBaseDetail viewI2cFrame2; // viewI2cFrame2
    private TopLayoutTriggerSerialsBaseDetail viewI2cRomData; // viewI2cRomData
    private TopLayoutTriggerSerialsBaseDetail viewI2c10WriteFrame; // viewI2c10WriteFrame
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Label; // viewArinc429Label
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Sdi; // viewArinc429Sdi
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Data; // viewArinc429Data
    private TopLayoutTriggerSerialsBaseDetail viewArinc429Ssm; // viewArinc429Ssm
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelSdi; // viewArinc429LabelSdi
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelData; // viewArinc429LabelData
    private TopLayoutTriggerSerialsBaseDetail viewArinc429LabelSsm; // viewArinc429LabelSsm
    private TopLayoutTriggerSerialsBaseDetail viewM1553bCSWord; // viewM1553bCSWord
    private TopLayoutTriggerSerialsBaseDetail viewM1553bRTAddr; // viewM1553bRTAddr
    private TopLayoutTriggerSerialsBaseDetail viewM1553bDataWord; // viewM1553bDataWord

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // onCreateView方法
        return inflater.inflate(R.layout.layout_triggerserials, container, false); // 返回值
    } // 代码块结束

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // onViewCreated方法
        this.context = getActivity(); // 赋值
        initView(view); // 执行语句
        initDetailView(savedInstanceState); // 执行语句
        initControl(); // 执行语句
    } // 代码块结束

    RecyclerView listView; // 执行语句

    private void initView(View view) { // initView方法
        listView = (RecyclerView) view.findViewById(R.id.list); // 调用findViewById
        serialsDetail = (LinearLayout) view.findViewById(R.id.serialsDetail); // 调用findViewById
        divider = view.findViewById(R.id.divider); // 调用findViewById

        String[] titles = getResources().getStringArray(R.array.triggerSerialsUART); // 调用getStringArray
        triggerDetail = new TopMsgTriggerSerials(); // 赋值
        triggerDetail.setSerials(new Serials(titles[0], 0, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber(), NULL)); // 调用setSerials
        triggerDetail.getSerials().setRxMsgSelect(false); // 调用getSerials
        triggerDetail.setSerialsDetail(null); // 调用setSerialsDetail
        divider.setVisibility(View.GONE); // 调用setVisibility

        listView.setLayoutManager(new GridLayoutManager(context, 8)); // 调用setLayoutManager
        adapter = new SerialsAdapter(context, list); // 赋值
        adapter.setOnItemClickListener(onSerialsItemClickListener); // 调用setOnItemClickListener
        listView.setAdapter(adapter); // 调用setAdapter
    } // 代码块结束

    private ArrayList<Serials> uart, lin, can, spi, i2c, m429, m1553b; // m1553b

    public void initList(ArrayList<Serials> uart, ArrayList<Serials> lin, ArrayList<Serials> can, ArrayList<Serials> spi, ArrayList<Serials> i2c, ArrayList<Serials> m429, ArrayList<Serials> m1553b, int serialsNumber) { // initList方法
        this.uart = uart; // 赋值
        this.lin = lin; // 赋值
        this.can = can; // 赋值
        this.spi = spi; // 赋值
        this.i2c = i2c; // 赋值
        this.m429 = m429; // 赋值
        this.m1553b = m1553b; // 赋值
        this.serialsNumber = serialsNumber; // 赋值
    } // 代码块结束

    public void setInitCache() { // setInitCache方法
        setCache(); // 执行语句
        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerSerials, true); // 调用get
    } // 代码块结束

    private int getSerialsNumber() { // getSerialsNumber方法
        return serialsNumber; // 返回值
    } // 代码块结束

    private void initDetailView(Bundle savedInstanceState) { // initDetailView方法
        if (savedInstanceState != null) { // 条件判断
            for (int i = 0; i < fragmentTag.length; i++) { // 循环
                TopLayoutTriggerSerialsBaseDetail fragment = (TopLayoutTriggerSerialsBaseDetail) getChildFragmentManager().findFragmentByTag(fragmentTag[i]); // 调用findFragmentByTag
                if (fragment != null) { // 条件判断
                    fragments[i] = fragment; // 赋值
                } // 代码块结束
            } // 代码块结束
        } // 代码块结束

        viewUartData = fragments[0] == null ? new TopLayoutTriggerSerialsUartData() : fragments[0]; // 赋值
        viewUart0Data = fragments[1] == null ? new TopLayoutTriggerSerialsUart0Data() : fragments[1]; // 赋值
        viewUart1Data = fragments[2] == null ? new TopLayoutTriggerSerialsUart1Data() : fragments[2]; // 赋值
        viewUartxData = fragments[3] == null ? new TopLayoutTriggerSerialsUartxData() : fragments[3]; // 赋值
        viewLinFrameId = fragments[4] == null ? new TopLayoutTriggerSerialsLinFrameId() : fragments[4]; // 赋值
        viewLinIdData = fragments[5] == null ? new TopLayoutTriggerSerialsLinIdData() : fragments[5]; // 赋值
        viewCanRemoteId = fragments[6] == null ? new TopLayoutTriggerSerialsCanRemoteId() : fragments[6]; // 赋值
        viewCanDataId = fragments[7] == null ? new TopLayoutTriggerSerialsCanDataId() : fragments[7]; // 赋值
        viewCanRDId = fragments[8] == null ? new TopLayoutTriggerSerialsCanRdId() : fragments[8]; // 赋值
        viewCanIdData = fragments[9] == null ? new TopLayoutTriggerSerialsCanIdData() : fragments[9]; // 赋值
        viewSpiData = fragments[10] == null ? new TopLayoutTriggerSerialsSpiData() : fragments[10]; // 赋值
        viewI2cNoAckInAdr = fragments[11] == null ? new TopLayoutTriggerSerialsI2cNoAckInAdr() : fragments[11]; // 赋值
        viewI2cFrame1 = fragments[12] == null ? new TopLayoutTriggerSerialsI2cFrame1() : fragments[12]; // 赋值
        viewI2cFrame2 = fragments[13] == null ? new TopLayoutTriggerSerialsI2cFrame2() : fragments[13]; // 赋值
        viewI2cRomData = fragments[14] == null ? new TopLayoutTriggerSerialsI2cRomData() : fragments[14]; // 赋值
        viewI2c10WriteFrame = fragments[15] == null ? new TopLayoutTriggerSerialsI2c10WriteFrame() : fragments[15]; // 赋值
        viewArinc429Label = fragments[16] == null ? new TopLayoutTriggerSerialsArinc429Label() : fragments[16]; // 赋值
        viewArinc429Sdi = fragments[17] == null ? new TopLayoutTriggerSerialsArinc429Sdi() : fragments[17]; // 赋值
        viewArinc429Data = fragments[18] == null ? new TopLayoutTriggerSerialsArinc429Data() : fragments[18]; // 赋值
        viewArinc429Ssm = fragments[19] == null ? new TopLayoutTriggerSerialsArinc429Ssm() : fragments[19]; // 赋值
        viewArinc429LabelSdi = fragments[20] == null ? new TopLayoutTriggerSerialsArinc429LabelSdi() : fragments[20]; // 赋值
        viewArinc429LabelData = fragments[21] == null ? new TopLayoutTriggerSerialsArinc429LabelData() : fragments[21]; // 赋值
        viewArinc429LabelSsm = fragments[22] == null ? new TopLayoutTriggerSerialsArinc429LabelSsm() : fragments[22]; // 赋值
        viewM1553bCSWord = fragments[23] == null ? new TopLayoutTriggerSerialsM1553bCsWord() : fragments[23]; // 赋值
        viewM1553bRTAddr = fragments[24] == null ? new TopLayoutTriggerSerialsM1553bRtAddr() : fragments[24]; // 赋值
        viewM1553bDataWord = fragments[25] == null ? new TopLayoutTriggerSerialsM1553bDataWord() : fragments[25]; // 赋值

        viewUartData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewUart0Data.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewUart1Data.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewUartxData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewLinFrameId.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewLinIdData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewCanRemoteId.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewCanDataId.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewCanRDId.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewCanIdData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewSpiData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewI2cNoAckInAdr.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewI2cFrame1.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewI2cFrame2.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewI2cRomData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewI2c10WriteFrame.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429Label.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429Sdi.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429Data.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429Ssm.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429LabelSdi.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429LabelData.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewArinc429LabelSsm.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewM1553bCSWord.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewM1553bRTAddr.setSerialsNumber(serialsNumber); // 调用setSerialsNumber
        viewM1553bDataWord.setSerialsNumber(serialsNumber); // 调用setSerialsNumber

        viewUartData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewUart0Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewUart1Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewUartxData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewLinFrameId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewLinIdData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewCanRemoteId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewCanDataId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewCanRDId.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewCanIdData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewSpiData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewI2cNoAckInAdr.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewI2cFrame1.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewI2cFrame2.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewI2cRomData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewI2c10WriteFrame.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429Label.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429Sdi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429Data.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429Ssm.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429LabelSdi.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429LabelData.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewArinc429LabelSsm.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewM1553bCSWord.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewM1553bRTAddr.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener
        viewM1553bDataWord.setOnSerialsDetailSendMsgListener(onSerialsDetailSendMsgListener); // 调用setOnSerialsDetailSendMsgListener

        if (savedInstanceState == null) { // 条件判断
            getChildFragmentManager().beginTransaction() // 调用beginTransaction
                    .add(R.id.serialsDetail, viewUartData, fragmentTag[0]) // 调用add
                    .add(R.id.serialsDetail, viewUart0Data, fragmentTag[1]) // 调用add
                    .add(R.id.serialsDetail, viewUart1Data, fragmentTag[2]) // 调用add
                    .add(R.id.serialsDetail, viewUartxData, fragmentTag[3]) // 调用add
                    .add(R.id.serialsDetail, viewLinFrameId, fragmentTag[4]) // 调用add
                    .add(R.id.serialsDetail, viewLinIdData, fragmentTag[5]) // 调用add
                    .add(R.id.serialsDetail, viewCanRemoteId, fragmentTag[6]) // 调用add
                    .add(R.id.serialsDetail, viewCanDataId, fragmentTag[7]) // 调用add
                    .add(R.id.serialsDetail, viewCanRDId, fragmentTag[8]) // 调用add
                    .add(R.id.serialsDetail, viewCanIdData, fragmentTag[9]) // 调用add
                    .add(R.id.serialsDetail, viewSpiData, fragmentTag[10]) // 调用add
                    .add(R.id.serialsDetail, viewI2cNoAckInAdr, fragmentTag[11]) // 调用add
                    .add(R.id.serialsDetail, viewI2cFrame1, fragmentTag[12]) // 调用add
                    .add(R.id.serialsDetail, viewI2cFrame2, fragmentTag[13]) // 调用add
                    .add(R.id.serialsDetail, viewI2cRomData, fragmentTag[14]) // 调用add
                    .add(R.id.serialsDetail, viewI2c10WriteFrame, fragmentTag[15]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429Label, fragmentTag[16]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429Sdi, fragmentTag[17]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429Data, fragmentTag[18]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429Ssm, fragmentTag[19]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429LabelSdi, fragmentTag[20]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429LabelData, fragmentTag[21]) // 调用add
                    .add(R.id.serialsDetail, viewArinc429LabelSsm, fragmentTag[22]) // 调用add
                    .add(R.id.serialsDetail, viewM1553bCSWord, fragmentTag[23]) // 调用add
                    .add(R.id.serialsDetail, viewM1553bRTAddr, fragmentTag[24]) // 调用add
                    .add(R.id.serialsDetail, viewM1553bDataWord, fragmentTag[25]) // 调用add
                    .hide(viewUartData) // 调用hide
                    .hide(viewUart0Data) // 调用hide
                    .hide(viewUart1Data) // 调用hide
                    .hide(viewUartxData) // 调用hide
                    .hide(viewLinFrameId) // 调用hide
                    .hide(viewLinIdData) // 调用hide
                    .hide(viewCanRemoteId) // 调用hide
                    .hide(viewCanDataId) // 调用hide
                    .hide(viewCanRDId) // 调用hide
                    .hide(viewCanIdData) // 调用hide
                    .hide(viewSpiData) // 调用hide
                    .hide(viewI2cNoAckInAdr) // 调用hide
                    .hide(viewI2cFrame1) // 调用hide
                    .hide(viewI2cFrame2) // 调用hide
                    .hide(viewI2cRomData) // 调用hide
                    .hide(viewI2c10WriteFrame) // 调用hide
                    .hide(viewArinc429Label) // 调用hide
                    .hide(viewArinc429Sdi) // 调用hide
                    .hide(viewArinc429Data) // 调用hide
                    .hide(viewArinc429Ssm) // 调用hide
                    .hide(viewArinc429LabelSdi) // 调用hide
                    .hide(viewArinc429LabelData) // 调用hide
                    .hide(viewArinc429LabelSsm) // 调用hide
                    .hide(viewM1553bCSWord) // 调用hide
                    .hide(viewM1553bRTAddr) // 调用hide
                    .hide(viewM1553bDataWord) // 调用hide
                    .commitAllowingStateLoss(); // 调用commitAllowingStateLoss
        } // 代码块结束
    } // 代码块结束

    private void initControl() { // initControl方法
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 调用getInstance
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 调用getInstance
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam); // 调用addEventObserver
    } // 代码块结束

    private void setCache() { // setCache方法
        int serials = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()); // 调用get
        list.clear(); // 调用clear
        int title; // 执行语句
        switch (serials) { // switch分支
            case UART: // case分支
                list.addAll(uart); // 调用addAll
                break; // 跳出分支
            case LIN: // case分支
                list.addAll(lin); // 调用addAll
                break; // 跳出分支
            case CAN: // case分支
                list.addAll(can); // 调用addAll
                break; // 跳出分支
            case SPI: // case分支
                list.addAll(spi); // 调用addAll
                break; // 跳出分支
            case I2C: // case分支
                list.addAll(i2c); // 调用addAll
                break; // 跳出分支
            case ARINC429: // case分支
                list.addAll(m429); // 调用addAll
                break; // 跳出分支
            case M1553B: // case分支
                list.addAll(m1553b); // 调用addAll
                break; // 跳出分支
            default:
                list.addAll(uart); // 调用addAll
                break; // 跳出分支
        } // 代码块结束
        title = CacheUtil.get().getInt(list.get(0).getCacheListKey()); // 调用get
        for (int i = 0; i < list.size(); i++) { // 循环
            list.get(i).setSelected(i == title); // 调用get
            if (i == title) { // 条件判断
                showDetail(list.get(i).getDetailFlag()); // 调用get
                setTriggerType(list, i); // 执行语句
                triggerDetail.setSerials(list.get(i)); // 调用setSerials
                triggerDetail.getSerials().setRxMsgSelect(false); // 调用getSerials
                if (list.get(i).getDetailFlag() == NULL) { // 条件判断
                    triggerDetail.setSerialsDetail(null); // 调用setSerialsDetail
                    divider.setVisibility(View.GONE); // 调用setVisibility
                } else { // 否则分支
                    triggerDetail.setSerialsDetail(getSerialsDetail(list.get(i).getDetailFlag())); // 调用setSerialsDetail
                    divider.setVisibility(View.GONE); // 调用setVisibility
                } // 代码块结束
                sendMsg(false); // 执行语句
            } // 代码块结束
        } // 代码块结束
        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged


        int selectType= Tools.indexOf(list, Serials::isSelected); // 调用indexOf
        setCommand(serials,selectType); // 执行语句

    } // 代码块结束



    private void setCommand(int serialType,int selectType){ // setCommand方法
        switch (serialType) { // switch分支
            case UART: { // case分支
                int condition = Command.get().getTrigger_uart().getCondition(getSerialsNumber() - 1); // 调用get
                int num = Command.get().getTrigger_uart().getNumber(getSerialsNumber() - 1); // 调用get
                Command.get().getTrigger_uart().setType(getSerialsNumber() - 1, selectType, condition, num, false); // 调用get
            }break; // 执行语句
            case LIN: { // case分支
                int id = Command.get().getTrigger_lin().getId(getSerialsNumber() - 1); // 调用get
                long data = Command.get().getTrigger_lin().getData(getSerialsNumber() - 1); // 调用get
                Command.get().getTrigger_lin().setType(getSerialsNumber() - 1, selectType, id, data, false); // 调用get
            }break; // 执行语句
            case CAN: { // case分支
                int id = Command.get().getTrigger_can().getId(getSerialsNumber() - 1); // 调用get
                int dlc=Command.get().getTrigger_can().getDlc(getSerialsNumber()-1); // 调用get
                long data=Command.get().getTrigger_can().getData(getSerialsNumber()-1); // 调用get
                Command.get().getTrigger_can().setType(getSerialsNumber() - 1, selectType, id, dlc, data, false); // 调用get
            }break; // 执行语句
            case SPI: { // case分支
                Command.get().getTrigger_spi().Type(getSerialsNumber() - 1, selectType, false); // 调用get
            }break; // 执行语句
            case I2C: { // case分支
                int addr=Command.get().getTrigger_iic().getAddr(getSerialsNumber()-1); // 调用get
                int data1=Command.get().getTrigger_iic().getData1(getSerialsNumber()-1); // 调用get
                int data2=Command.get().getTrigger_iic().getData2(getSerialsNumber()-1); // 调用get
                int condition=Command.get().getTrigger_iic().getCondition(getSerialsNumber()-1); // 调用get
                Command.get().getTrigger_iic().setType(getSerialsNumber() - 1, serialType, addr, data1, data2, condition, false); // 调用get
            }break; // 执行语句
            case ARINC429: { // case分支
                int label=Command.get().getTrigger_m429().getLabel(getSerialsNumber()-1); // 调用get
                int sdi=Command.get().getTrigger_m429().getSdi(getSerialsNumber()-1); // 调用get
                long data=Command.get().getTrigger_m429().getData(getSerialsNumber()-1); // 调用get
                int ssm= Command.get().getTrigger_m429().getSsm(getSerialsNumber()-1); // 调用get
                Command.get().getTrigger_m429().setType(getSerialsNumber() - 1, serialType, label, sdi, data, ssm, false); // 调用get
            }break; // 执行语句
            case M1553B: { // case分支
                int cs=Command.get().getTrigger_m1553B().getCsWord(getSerialsNumber()-1); // 调用get
                int rt=Command.get().getTrigger_m1553B().getRtAddr(getSerialsNumber()-1); // 调用get
                int data=Command.get().getTrigger_m1553B().getDataWord(getSerialsNumber()-1); // 调用get
                Command.get().getTrigger_m1553B().setType(getSerialsNumber() - 1, serialType, cs, rt, data, false); // 调用get
            }break; // 执行语句
//            default: {
//                int condition = Command.get().getTrigger_uart().getCondition(getSerialsNumber() - 1);
//                int num = Command.get().getTrigger_uart().getNumber(getSerialsNumber() - 1);
//                Command.get().getTrigger_uart().setType(getSerialsNumber() - 1, selectType, condition, num, false);
//            }break;
        } // 代码块结束
    } // 代码块结束

    public void setList(List<Serials> serialsList, int select, boolean isFromEventBus) { // setList方法
        if (serialsList != null && serialsList.size() > 0) { // 条件判断
            list.clear(); // 调用clear
            list.addAll(serialsList); // 调用addAll
            adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
            showDetail(list.get(select).getDetailFlag()); // 调用get
            CacheUtil.get().putMap(list.get(select).getCacheListKey(), String.valueOf(select)); // 调用get

            int serials = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()); // 调用get
            int selectType= Tools.indexOf(list,s->s.isSelected()); // 调用indexOf
            setCommand(serials,selectType); // 执行语句

            if (!isFromEventBus) { // 条件判断
                setTriggerType(list, select); // 执行语句
            } // 代码块结束
            if (list.get(select).getDetailFlag() == NULL) { // 条件判断
                triggerDetail.setSerialsDetail(null); // 调用setSerialsDetail
                divider.setVisibility(View.GONE); // 调用setVisibility
            } else { // 否则分支
                triggerDetail.setSerialsDetail(getSerialsDetail(list.get(select).getDetailFlag())); // 调用setSerialsDetail
                divider.setVisibility(View.GONE); // 调用setVisibility
            } // 代码块结束
            triggerDetail.setSerials(list.get(select)); // 调用setSerials
            triggerDetail.getSerials().setRxMsgSelect(false); // 调用getSerials
            sendMsg(isFromEventBus); // 执行语句
        } // 代码块结束
    } // 代码块结束

    private void showDetail(int detailFlag) { // showDetail方法
        detailLayoutSetGone(); // 执行语句
        TopLayoutTriggerSerialsBaseDetail baseDetail = getFragmentForFlag(detailFlag); // 赋值
        if (baseDetail != null) { // 条件判断
            getChildFragmentManager().beginTransaction().show(baseDetail).commitAllowingStateLoss(); // 调用beginTransaction
        } // 代码块结束
    } // 代码块结束

    private TopLayoutTriggerSerialsBaseDetail getFragmentForFlag(int detailFlag) { // getFragmentForFlag方法
        TopLayoutTriggerSerialsBaseDetail baseDetail = null; // 赋值
        switch (detailFlag) { // switch分支
            case UART_DATA: // case分支
                baseDetail = viewUartData; // 赋值
                break; // 跳出分支
            case UART_0DATA: // case分支
                baseDetail = viewUart0Data; // 赋值
                break; // 跳出分支
            case UART_1DATA: // case分支
                baseDetail = viewUart1Data; // 赋值
                break; // 跳出分支
            case UART_XDATA: // case分支
                baseDetail = viewUartxData; // 赋值
                break; // 跳出分支
            case LIN_FRAMEID: // case分支
                baseDetail = viewLinFrameId; // 赋值
                break; // 跳出分支
            case LIN_IDDATA: // case分支
                baseDetail = viewLinIdData; // 赋值
                break; // 跳出分支
            case CAN_REMOTEID: // case分支
                baseDetail = viewCanRemoteId; // 赋值
                break; // 跳出分支
            case CAN_DATAID: // case分支
                baseDetail = viewCanDataId; // 赋值
                break; // 跳出分支
            case CAN_RDID: // case分支
                baseDetail = viewCanRDId; // 赋值
                break; // 跳出分支
            case CAN_IDDATA: // case分支
                baseDetail = viewCanIdData; // 赋值
                break; // 跳出分支
            case SPI_DATA: // case分支
                baseDetail = viewSpiData; // 赋值
                break; // 跳出分支
            case I2C_NOACKINADR: // case分支
                baseDetail = viewI2cNoAckInAdr; // 赋值
                break; // 跳出分支
            case I2C_FRAME1: // case分支
                baseDetail = viewI2cFrame1; // 赋值
                break; // 跳出分支
            case I2C_FRAME2: // case分支
                baseDetail = viewI2cFrame2; // 赋值
                break; // 跳出分支
            case I2C_ROMDATA: // case分支
                baseDetail = viewI2cRomData; // 赋值
                break; // 跳出分支
            case I2C_10WRITEFRAME: // case分支
                baseDetail = viewI2c10WriteFrame; // 赋值
                break; // 跳出分支
            case ARINC429_LABEL: // case分支
                baseDetail = viewArinc429Label; // 赋值
                break; // 跳出分支
            case ARINC429_SDI: // case分支
                baseDetail = viewArinc429Sdi; // 赋值
                break; // 跳出分支
            case ARINC429_DATA: // case分支
                baseDetail = viewArinc429Data; // 赋值
                break; // 跳出分支
            case ARINC429_SSM: // case分支
                baseDetail = viewArinc429Ssm; // 赋值
                break; // 跳出分支
            case ARINC429_LABELSDI: // case分支
                baseDetail = viewArinc429LabelSdi; // 赋值
                break; // 跳出分支
            case ARINC429_LABELDATA: // case分支
                baseDetail = viewArinc429LabelData; // 赋值
                break; // 跳出分支
            case ARINC429_LABELSSM: // case分支
                baseDetail = viewArinc429LabelSsm; // 赋值
                break; // 跳出分支
            case M1553B_CSWORD: // case分支
                baseDetail = viewM1553bCSWord; // 赋值
                break; // 跳出分支
            case M1553B_RTADDR: // case分支
                baseDetail = viewM1553bRTAddr; // 赋值
                break; // 跳出分支
            case M1553B_DATAWORD: // case分支
                baseDetail = viewM1553bDataWord; // 赋值
                break; // 跳出分支
            case NULL: // case分支
            default:
                break; // 跳出分支
        } // 代码块结束
        return baseDetail; // 返回值
    } // 代码块结束

    private void detailLayoutSetGone() { // detailLayoutSetGone方法
        getChildFragmentManager().beginTransaction() // 调用beginTransaction
                .hide(viewUartData) // 调用hide
                .hide(viewUart0Data) // 调用hide
                .hide(viewUart1Data) // 调用hide
                .hide(viewUartxData) // 调用hide
                .hide(viewLinFrameId) // 调用hide
                .hide(viewLinIdData) // 调用hide
                .hide(viewCanRemoteId) // 调用hide
                .hide(viewCanDataId) // 调用hide
                .hide(viewCanRDId) // 调用hide
                .hide(viewCanIdData) // 调用hide
                .hide(viewSpiData) // 调用hide
                .hide(viewI2cNoAckInAdr) // 调用hide
                .hide(viewI2cFrame1) // 调用hide
                .hide(viewI2cFrame2) // 调用hide
                .hide(viewI2cRomData) // 调用hide
                .hide(viewI2c10WriteFrame) // 调用hide
                .hide(viewArinc429Label) // 调用hide
                .hide(viewArinc429Sdi) // 调用hide
                .hide(viewArinc429Data) // 调用hide
                .hide(viewArinc429Ssm) // 调用hide
                .hide(viewArinc429LabelSdi) // 调用hide
                .hide(viewArinc429LabelData) // 调用hide
                .hide(viewArinc429LabelSsm) // 调用hide
                .hide(viewM1553bCSWord) // 调用hide
                .hide(viewM1553bRTAddr) // 调用hide
                .hide(viewM1553bDataWord) // 调用hide
                .commitAllowingStateLoss(); // 调用commitAllowingStateLoss
    } // 代码块结束

    public TopMsgTriggerSerials getTriggerDetail() { // getTriggerDetail方法
        return triggerDetail; // 返回值
    } // 代码块结束

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // setOnDetailSendMsgListener方法
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值
    } // 代码块结束

    private void sendMsg(boolean isFromEventBus) { // sendMsg方法
        if (onDetailSendMsgListener != null) { // 条件判断
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用onClick
        } // 代码块结束
    } // 代码块结束

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // accept方法
            //由于本页面的初始化需要从TopLayoutTrigger页面获取参数之后才能开始初始化，所以移到本页面别的方法中去了...
        } // 代码块结束
    }; // 执行语句

    //region CommandToUI
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // accept方法
            int cmdCh=TChan.toFpgaChNo(serialsNumber); // 调用toFpgaChNo

            switch (commandMsgToUI.getFlag()) { // switch分支
                case CommandMsgToUI.FLAG_TRIGGERUART_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(uart.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int condition = Integer.parseInt(params[2]); // 调用parseInt
                        int number = Integer.parseInt(params[3]); // 调用parseInt
                        if (!list.get(type).isEnabled()) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 2: // case分支
                                ((TopLayoutTriggerSerialsUartData) viewUartData).setCommandData(condition, number, false); // 调用setCommandData
                                break; // 跳出分支
                            case 3: // case分支
                                ((TopLayoutTriggerSerialsUart0Data) viewUart0Data).setCommandData(condition, number, false); // 调用setCommandData
                                break; // 跳出分支
                            case 4: // case分支
                                ((TopLayoutTriggerSerialsUart1Data) viewUart1Data).setCommandData(condition, number, false); // 调用setCommandData
                                break; // 跳出分支
                            case 5: // case分支
                                ((TopLayoutTriggerSerialsUartxData) viewUartxData).setCommandData(condition, number, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERLIN_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(lin.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int id = Integer.parseInt(params[2]); // 调用parseInt
                        long data = Long.parseLong(params[3]); // 调用parseLong
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 1: // case分支
                                ((TopLayoutTriggerSerialsLinFrameId) viewLinFrameId).setCommandData(id, false); // 调用setCommandData
                                break; // 跳出分支
                            case 2: // case分支
                                ((TopLayoutTriggerSerialsLinIdData) viewLinIdData).setCommandData(id, data, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERCAN_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(can.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int id = Integer.parseInt(params[2]); // 调用parseInt
                        int dic = Integer.parseInt(params[3]); // 调用parseInt
                        long data = Long.parseLong(params[4]); // 调用parseLong
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 1: // case分支
                                ((TopLayoutTriggerSerialsCanRemoteId) viewCanRemoteId).setCommandData(id, false); // 调用setCommandData
                                break; // 跳出分支
                            case 2: // case分支
                                ((TopLayoutTriggerSerialsCanDataId) viewCanDataId).setCommandData(id, false); // 调用setCommandData
                                break; // 跳出分支
                            case 3: // case分支
                                ((TopLayoutTriggerSerialsCanRdId) viewCanRDId).setCommandData(id, false); // 调用setCommandData
                                break; // 跳出分支
                            case 4: // case分支
                                ((TopLayoutTriggerSerialsCanIdData) viewCanIdData).setCommandData(id, dic, data, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERSPI_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(spi.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int mask = Integer.parseInt(params[2]); // 调用parseInt
                        int data = Integer.parseInt(params[3]); // 调用parseInt
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 1: // case分支
                                String spiText = SerialsUtils.getSpiText(mask, data); // 调用getSpiText
                                ((TopLayoutTriggerSerialsSpiData) viewSpiData).setCommandData(spiText, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERIIC_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(i2c.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int addr = Integer.parseInt(params[2]); // 调用parseInt
                        int data1 = Integer.parseInt(params[3]); // 调用parseInt
                        int data2 = Integer.parseInt(params[4]); // 调用parseInt
                        int condition = Integer.parseInt(params[5]); // 调用parseInt
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 4: // case分支
                                ((TopLayoutTriggerSerialsI2cNoAckInAdr) viewI2cNoAckInAdr).setCommandData(addr, false); // 调用setCommandData
                                break; // 跳出分支
                            case 5: // case分支
                                ((TopLayoutTriggerSerialsI2cFrame1) viewI2cFrame1).setCommandData(addr, data1, false); // 调用setCommandData
                                break; // 跳出分支
                            case 6: // case分支
                                ((TopLayoutTriggerSerialsI2cFrame2) viewI2cFrame2).setCommandData(addr, data1, data2, false); // 调用setCommandData
                                break; // 跳出分支
                            case 7: // case分支
                                ((TopLayoutTriggerSerialsI2cRomData) viewI2cRomData).setCommandData(data1, condition, false); // 调用setCommandData
                                break; // 跳出分支
                            case 8: // case分支
                                ((TopLayoutTriggerSerialsI2c10WriteFrame) viewI2c10WriteFrame).setCommandData(addr, data1, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERM429_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex == cmdCh && list.get(0).getName().equals(m429.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int label = Integer.parseInt(params[2]); // 调用parseInt
                        int sdi = Integer.parseInt(params[3]); // 调用parseInt
                        int data = Integer.parseInt(params[4]); // 调用parseInt
                        int ssm = Integer.parseInt(params[5]); // 调用parseInt
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 2: // case分支
                                ((TopLayoutTriggerSerialsArinc429Label) viewArinc429Label).setCommandData(label, false); // 调用setCommandData
                                break; // 跳出分支
                            case 3: // case分支
                                ((TopLayoutTriggerSerialsArinc429Sdi) viewArinc429Sdi).setCommandData(sdi, false); // 调用setCommandData
                                break; // 跳出分支
                            case 4: // case分支
                                ((TopLayoutTriggerSerialsArinc429Data) viewArinc429Data).setCommandData(data, false); // 调用setCommandData
                                break; // 跳出分支
                            case 5: // case分支
                                ((TopLayoutTriggerSerialsArinc429Ssm) viewArinc429Ssm).setCommandData(ssm, false); // 调用setCommandData
                                break; // 跳出分支
                            case 6: // case分支
                                ((TopLayoutTriggerSerialsArinc429LabelSdi) viewArinc429LabelSdi).setCommandData(label, sdi, false); // 调用setCommandData
                                break; // 跳出分支
                            case 7: // case分支
                                ((TopLayoutTriggerSerialsArinc429LabelData) viewArinc429LabelData).setCommandData(label, data, false); // 调用setCommandData
                                break; // 跳出分支
                            case 8: // case分支
                                ((TopLayoutTriggerSerialsArinc429LabelSsm) viewArinc429LabelSsm).setCommandData(label, ssm, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
                case CommandMsgToUI.FLAG_TRIGGERM1553B_TYPE: { // case分支
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 调用getParam
                    int serialsIndex = Integer.parseInt(params[0]); // 调用parseInt
                    if (serialsIndex ==cmdCh && list.get(0).getName().equals(m1553b.get(0).getName())) { // 条件判断
                        int type = Integer.parseInt(params[1]); // 调用parseInt
                        int csWord = Integer.parseInt(params[2]); // 调用parseInt
                        int rtAddr = Integer.parseInt(params[3]); // 调用parseInt
                        int dataWord = Integer.parseInt(params[4]); // 调用parseInt
                        if (list.get(type).isEnabled()==false) return; // 条件判断
                        for (int i = 0; i < list.size(); i++) { // 循环
                            list.get(i).setSelected(type == i); // 调用get
                        } // 代码块结束
                        adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                        setSerialsItemClick(list, list.get(type), false); // 调用get
                        switch (type) { // switch分支
                            case 2: // case分支
                                ((TopLayoutTriggerSerialsM1553bCsWord) viewM1553bCSWord).setCommandData(csWord, false); // 调用setCommandData
                                break; // 跳出分支
                            case 3: // case分支
                                ((TopLayoutTriggerSerialsM1553bRtAddr) viewM1553bRTAddr).setCommandData(rtAddr, false); // 调用setCommandData
                                break; // 跳出分支
                            case 5: // case分支
                                ((TopLayoutTriggerSerialsM1553bDataWord) viewM1553bDataWord).setCommandData(dataWord, false); // 调用setCommandData
                                break; // 跳出分支
                        } // 代码块结束
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
            } // 代码块结束
        } // 代码块结束
    }; // 执行语句
    //endregion

    private EventUIObserver eventBusParam = new EventUIObserver() { // {
        @Override
        public void update(Object data) { // update方法
            if (((EventBase) data).getId() == EventFactory.EVENT_BUS_PARAM) { // 条件判断
                int select = 0; // 赋值
                for (int i = 0; i < list.size(); i++) { // 循环
                    if (list.get(i).isSelected()) { // 条件判断
                        select = i; // 赋值
                        break; // 跳出分支
                    } // 代码块结束
                } // 代码块结束
                int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber); // 调用toFpgaBySerialNumber
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan); // 调用getSerialChannel
                if(serialChannel == null) return; // 条件判断
                int clickable = -1; // 赋值
                if (list.get(0).getName().equals(uart.get(0).getName())) { // 条件判断
                    UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 调用getBus
                    if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_START_BIT && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_STOP_BIT && select != 1) { // 调用getTriggerType
                        clickable = 1; // 赋值
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA) { // 调用getTriggerType
                        if (select != 2) { // 条件判断
                            clickable = 2; // 赋值
                        } // 代码块结束
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation()); // 调用getConditionValueFromEventBus
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA); // 调用getTriggerData
                        ((TopLayoutTriggerSerialsUartData) viewUartData).setCommandData(condition, number, true); // 调用setCommandData
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA0) { // 调用getTriggerType
                        if (select != 3) { // 条件判断
                            clickable = 3; // 赋值
                        } // 代码块结束
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation()); // 调用getConditionValueFromEventBus
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA0); // 调用getTriggerData
                        ((TopLayoutTriggerSerialsUart0Data) viewUart0Data).setCommandData(condition, number, true); // 调用setCommandData
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATA1) { // 调用getTriggerType
                        if (select != 4) { // 条件判断
                            clickable = 4; // 赋值
                        } // 代码块结束
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation()); // 调用getConditionValueFromEventBus
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATA1); // 调用getTriggerData
                        ((TopLayoutTriggerSerialsUart1Data) viewUart1Data).setCommandData(condition, number, true); // 调用setCommandData
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_DATAx) { // 调用getTriggerType
                        if (select != 5) { // 条件判断
                            clickable = 5; // 赋值
                        } // 代码块结束
                        int condition = SerialsUtils.getConditionValueFromEventBus(uartBus.getTriggerRelation()); // 调用getConditionValueFromEventBus
                        int number = uartBus.getTriggerData(UartBus.UART_TRIGGER_DATAx); // 调用getTriggerData
                        ((TopLayoutTriggerSerialsUartxData) viewUartxData).setCommandData(condition, number, true); // 调用setCommandData
                    } else if (uartBus.getTriggerType() == UartBus.UART_TRIGGER_OOD_EVEN_BIT_ERROR && select != 6) { // 调用getTriggerType
                        clickable = 6; // 赋值
                    } // 代码块结束
                } else if (list.get(0).getName().equals(lin.get(0).getName())) { // 调用get
                    LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 调用getBus
                    int id = 0; // 赋值
                    switch (linBus.getTriggerType()) { // switch分支
                        case LinBus.LIN_TRIGGER_SYNC_RISING_EDGE: // case分支
                            if (select != 0) { // 条件判断
                                clickable = 0; // 赋值
                            } // 代码块结束
                            break; // 跳出分支
                        case LinBus.LIN_TRIGGER_FRAME_ID: // case分支
                            if (select != 1) { // 条件判断
                                clickable = 1; // 赋值
                            } // 代码块结束
                            id = linBus.getFrameId(LinBus.LIN_TRIGGER_FRAME_ID); // 调用getFrameId
                            ((TopLayoutTriggerSerialsLinFrameId) viewLinFrameId).setCommandData(id, true); // 调用setCommandData
                            break; // 跳出分支
                        case LinBus.LIN_TRIGGER_ID_AND_DATA: // case分支
                            if (select != 2) { // 条件判断
                                clickable = 2; // 赋值
                            } // 代码块结束
                            id = linBus.getFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA); // 调用getFrameId
                            long data1 = linBus.getData(); // 调用getData
                            ((TopLayoutTriggerSerialsLinIdData) viewLinIdData).setCommandData(id, data1, true); // 调用setCommandData
                            break; // 跳出分支
                        case LinBus.LIN_TRIGGER_PARITY_ERROR: // case分支
                            if (select != 3) { // 条件判断
                                clickable = 3; // 赋值
                            } // 代码块结束
                            break; // 跳出分支
                        case LinBus.LIN_TRIGGER_CHECKSUM_ERROR: // case分支
                            if (select != 4) { // 条件判断
                                clickable = 4; // 赋值
                            } // 代码块结束
                            break; // 跳出分支
                    } // 代码块结束
                } else if (list.get(0).getName().equals(can.get(0).getName())) { // 调用get
                    CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 调用getBus
                    if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_FRAME_START && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_REMOTE_FRAME_ID) { // 调用getTriggerType
                        if (select != 1) { // 条件判断
                            clickable = 1; // 赋值
                        } // 代码块结束
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID); // 调用getFrameId
                        ((TopLayoutTriggerSerialsCanRemoteId) viewCanRemoteId).setCommandData(id, true); // 调用setCommandData
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_DATA_FRAME_ID) { // 调用getTriggerType
                        if (select != 2) { // 条件判断
                            clickable = 2; // 赋值
                        } // 代码块结束
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_DATA_FRAME_ID); // 调用getFrameId
                        ((TopLayoutTriggerSerialsCanDataId) viewCanDataId).setCommandData(id, true); // 调用setCommandData
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_REMOTE_DATA_ID) { // 调用getTriggerType
                        if (select != 3) { // 条件判断
                            clickable = 3; // 赋值
                        } // 代码块结束
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID); // 调用getFrameId
                        ((TopLayoutTriggerSerialsCanRdId) viewCanRDId).setCommandData(id, true); // 调用setCommandData
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ID_AND_DATA) { // 调用getTriggerType
                        if (select != 4) { // 条件判断
                            clickable = 4; // 赋值
                        } // 代码块结束
                        int id = canBus.getFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA); // 调用getFrameId
                        int dlc = canBus.getDlc(); // 调用getDlc
                        long data1 = canBus.getData(); // 调用getData
                        ((TopLayoutTriggerSerialsCanIdData) viewCanIdData).setCommandData(id, SerialsUtils.getCanDlcFromScope(dlc), data1, true); // 调用setCommandData
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_WRONG_FRAME && select != 5) { // 调用getTriggerType
                        clickable = 5; // 赋值
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ALL_ERROR && select != 6) { // 调用getTriggerType
                        clickable = 6; // 赋值
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_ACK_ERROR && select != 7) { // 调用getTriggerType
                        clickable = 7; // 赋值
                    } else if (canBus.getTriggerType() == CanBus.CAN_TRIGGER_OVERLOAD_FRAME && select != 8) { // 调用getTriggerType
                        clickable = 8; // 赋值
                    } // 代码块结束
                } else if (list.get(0).getName().equals(spi.get(0).getName())) { // 调用get
                    SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI); // 调用getBus
                    if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_CS && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_DATA) { // 调用getTriggerType
                        if (select != 1) { // 条件判断
                            clickable = 1; // 赋值
                        } // 代码块结束
                        String spiText = SerialsUtils.getSpiText(spiBus.getTriggerMask(), spiBus.getTriggerData()); // 调用getSpiText
                        ((TopLayoutTriggerSerialsSpiData) viewSpiData).setCommandData(spiText, true); // 调用setCommandData
                    } else if (spiBus.getTriggerType() == SpiBus.SPI_TRIGGER_FRAME_X_DATA && select != 2) { // 调用getTriggerType
                        clickable = 2; // 赋值
                    } // 代码块结束
                } else if (list.get(0).getName().equals(i2c.get(0).getName())) { // 调用get
                    I2CBus i2CBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 调用getBus
                    if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_START_CONDITION && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_STOP_CONDITION && select != 1) { // 调用getTriggerType
                        clickable = 1; // 赋值
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_ACK_LOST && select != 2) { // 调用getTriggerType
                        clickable = 2; // 赋值
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_RESTART && select != 3) { // 调用getTriggerType
                        clickable = 3; // 赋值
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK) { // 调用getTriggerType
                        if (select != 4) { // 条件判断
                            clickable = 4; // 赋值
                        } // 代码块结束
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK); // 调用getTriggerAddr
                        ((TopLayoutTriggerSerialsI2cNoAckInAdr) viewI2cNoAckInAdr).setCommandData(addr, true); // 调用setCommandData
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_FRAME1) { // 调用getTriggerType
                        if (select != 5) { // 条件判断
                            clickable = 5; // 赋值
                        } // 代码块结束
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_FRAME1); // 调用getTriggerAddr
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_FRAME1); // 调用getTriggerData1
                        ((TopLayoutTriggerSerialsI2cFrame1) viewI2cFrame1).setCommandData(addr, data1, true); // 调用setCommandData
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_FRAME2) { // 调用getTriggerType
                        if (select != 6) { // 条件判断
                            clickable = 6; // 赋值
                        } // 代码块结束
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_FRAME2); // 调用getTriggerAddr
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_FRAME2); // 调用getTriggerData1
                        int data2 = i2CBus.getTriggerData2(); // 调用getTriggerData2
                        ((TopLayoutTriggerSerialsI2cFrame2) viewI2cFrame2).setCommandData(addr, data1, data2, true); // 调用setCommandData
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_EEPROM_READ_DATA) { // 调用getTriggerType
                        if (select != 7) { // 条件判断
                            clickable = 7; // 赋值
                        } // 代码块结束
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA); // 调用getTriggerData1
                        int condition = SerialsUtils.getConditionValueFromEventBus(i2CBus.getTriggerRelation()); // 调用getConditionValueFromEventBus
                        ((TopLayoutTriggerSerialsI2cRomData) viewI2cRomData).setCommandData(data1, condition, true); // 调用setCommandData
                    } else if (i2CBus.getTriggerType() == I2CBus.I2C_TRIGGER_WRITE_FRAME) { // 调用getTriggerType
                        if (select != 8) { // 条件判断
                            clickable = 8; // 赋值
                        } // 代码块结束
                        int addr = i2CBus.getTriggerAddr(I2CBus.I2C_TRIGGER_WRITE_FRAME); // 调用getTriggerAddr
                        int data1 = i2CBus.getTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME); // 调用getTriggerData1
                        ((TopLayoutTriggerSerialsI2c10WriteFrame) viewI2c10WriteFrame).setCommandData(addr, data1, true); // 调用setCommandData
                    } // 代码块结束
                } else if (list.get(0).getName().equals(m429.get(0).getName())) { // 调用get
                    ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 调用getBus
                    if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_BEGIN && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_END && select != 1) { // 调用getTriggerType
                        clickable = 1; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL) { // 调用getTriggerType
                        if (select != 2) { // 条件判断
                            clickable = 2; // 赋值
                        } // 代码块结束
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL); // 调用getLabel
                        ((TopLayoutTriggerSerialsArinc429Label) viewArinc429Label).setCommandData(label, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_SDI) { // 调用getTriggerType
                        if (select != 3) { // 条件判断
                            clickable = 3; // 赋值
                        } // 代码块结束
                        int sdi = a429Bus.getSdi(ARINC429Bus.ARINC429_TRIGGER_SDI); // 调用getSdi
                        ((TopLayoutTriggerSerialsArinc429Sdi) viewArinc429Sdi).setCommandData(sdi, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_DATA) { // 调用getTriggerType
                        if (select != 4) { // 条件判断
                            clickable = 4; // 赋值
                        } // 代码块结束
                        int data1 = a429Bus.getData(ARINC429Bus.ARINC429_TRIGGER_DATA); // 调用getData
                        ((TopLayoutTriggerSerialsArinc429Data) viewArinc429Data).setCommandData(data1, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_SSM) { // 调用getTriggerType
                        if (select != 5) { // 条件判断
                            clickable = 5; // 赋值
                        } // 代码块结束
                        int ssm = a429Bus.getSSM(ARINC429Bus.ARINC429_TRIGGER_SSM); // 调用getSSM
                        ((TopLayoutTriggerSerialsArinc429Ssm) viewArinc429Ssm).setCommandData(ssm, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI) { // 调用getTriggerType
                        if (select != 6) { // 条件判断
                            clickable = 6; // 赋值
                        } // 代码块结束
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI); // 调用getLabel
                        int sdi = a429Bus.getSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI); // 调用getSdi
                        ((TopLayoutTriggerSerialsArinc429LabelSdi) viewArinc429LabelSdi).setCommandData(label, sdi, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA) { // 调用getTriggerType
                        if (select != 7) { // 条件判断
                            clickable = 7; // 赋值
                        } // 代码块结束
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA); // 调用getLabel
                        int data1 = a429Bus.getData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA); // 调用getData
                        ((TopLayoutTriggerSerialsArinc429LabelData) viewArinc429LabelData).setCommandData(label, data1, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM) { // 调用getTriggerType
                        if (select != 8) { // 条件判断
                            clickable = 8; // 赋值
                        } // 代码块结束
                        int label = a429Bus.getLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM); // 调用getLabel
                        int ssm = a429Bus.getSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM); // 调用getSSM
                        ((TopLayoutTriggerSerialsArinc429LabelSsm) viewArinc429LabelSsm).setCommandData(label, ssm, true); // 调用setCommandData
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_ERROR && select != 9) { // 调用getTriggerType
                        clickable = 9; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_WORD_INTERVAL && select != 10) { // 调用getTriggerType
                        clickable = 10; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_VERIFY_ERROR && select != 11) { // 调用getTriggerType
                        clickable = 11; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_ERROR && select != 12) { // 调用getTriggerType
                        clickable = 12; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_0 && select != 13) { // 调用getTriggerType
                        clickable = 13; // 赋值
                    } else if (a429Bus.getTriggerType() == ARINC429Bus.ARINC429_TRIGGER_ALL_1 && select != 14) { // 调用getTriggerType
                        clickable = 14; // 赋值
                    } // 代码块结束
                } else if (list.get(0).getName().equals(m1553b.get(0).getName())) { // 调用get
                    MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B); // 调用getBus
                    if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC && select != 0) { // 条件判断
                        clickable = 0; // 赋值
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD_SYNC && select != 1) { // 调用getTriggerType
                        clickable = 1; // 赋值
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD) { // 调用getTriggerType
                        if (select != 2) { // 条件判断
                            clickable = 2; // 赋值
                        } // 代码块结束
                        int csWord = m1553bBus.getCmdStatus(); // 调用getCmdStatus
                        ((TopLayoutTriggerSerialsM1553bCsWord) viewM1553bCSWord).setCommandData(csWord, true); // 调用setCommandData
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS) { // 调用getTriggerType
                        if (select != 3) { // 条件判断
                            clickable = 3; // 赋值
                        } // 代码块结束
                        int rtAddr = m1553bBus.getAddr(); // 调用getAddr
                        ((TopLayoutTriggerSerialsM1553bRtAddr) viewM1553bRTAddr).setCommandData(rtAddr, true); // 调用setCommandData
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_MANCHESTER_ERROR && select != 4) { // 调用getTriggerType
                        clickable = 4; // 赋值
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD) { // 调用getTriggerType
                        if (select != 5) { // 条件判断
                            clickable = 5; // 赋值
                        } // 代码块结束
                        int dataWord = m1553bBus.getData(); // 调用getData
                        ((TopLayoutTriggerSerialsM1553bDataWord) viewM1553bDataWord).setCommandData(dataWord, true); // 调用setCommandData
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_ODD_PARITY_ERROR && select != 6) { // 调用getTriggerType
                        clickable = 6; // 赋值
                    } else if (m1553bBus.getTriggerType() == MILSTD1553BBus.MILSTD1553B_TRIGGER_ALL_ERROR && select != 7) { // 调用getTriggerType
                        clickable = 7; // 赋值
                    } // 代码块结束
                } // 代码块结束
                if (clickable != -1) { // 条件判断
                    for (int i = 0; i < list.size(); i++) { // 循环
                        list.get(i).setSelected(i == clickable); // 调用get
                    } // 代码块结束
                    adapter.notifyDataSetChanged(); // 调用notifyDataSetChanged
                    setSerialsItemClick(list, list.get(clickable), true); // 调用get
                } // 代码块结束
            } // 代码块结束
        } // 代码块结束
    }; // 执行语句

    //region onSerialsItemClickListener
    private SerialsAdapter.OnSerialsItemClickListener onSerialsItemClickListener = new SerialsAdapter.OnSerialsItemClickListener() { // {
        @Override
        public void itemClick(List<Serials> serialsList, Serials serials) { // itemClick方法
            PlaySound.getInstance().playButton(); // 调用getInstance
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true)); // 调用get
            setSerialsItemClick(serialsList, serials, false); // 执行语句
        } // 代码块结束
    }; // 执行语句
    //endregion

    private void setSerialsItemClick(List<Serials> serialsList, Serials serials, boolean isFromEventBus) { // setSerialsItemClick方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 调用toFpgaChNo

        if (serials.isEnabled()) { // 条件判断
            for (int i = 0; i < serialsList.size(); i++) { // 循环
                if (serialsList.get(i).getId() == serials.getId()) { // 条件判断
                    CacheUtil.get().putMap(serials.getCacheListKey(), String.valueOf(i)); // 调用get
                    showDetail(serialsList.get(i).getDetailFlag()); // 调用get
                    if (serialsList.get(i).getDetailFlag() == NULL) { // 条件判断
                        divider.setVisibility(View.GONE); // 调用setVisibility
                        triggerDetail.setSerialsDetail(null); // 调用setSerialsDetail
                        if (serialsList.get(0).getName().equals(uart.get(0).getName())) { // 条件判断
                            Command.get().getTrigger_uart().setType(cmdCh, i, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(lin.get(0).getName())) { // 调用get
                            Command.get().getTrigger_lin().setType(cmdCh, i, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(can.get(0).getName())) { // 调用get
                            Command.get().getTrigger_can().setType(cmdCh, i, 0, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(spi.get(0).getName())) { // 调用get
                            Command.get().getTrigger_spi().Type(cmdCh,i,false); // 调用get
                            Command.get().getBus_spi().setType(cmdCh, i, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(i2c.get(0).getName())) { // 调用get
                            Command.get().getTrigger_iic().setType(cmdCh, i, 0, 0, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(m429.get(0).getName())) { // 调用get
                            Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, 0, 0, false); // 调用get
                        } else if (serialsList.get(0).getName().equals(m1553b.get(0).getName())) { // 调用get
                            Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, 0, 0, false); // 调用get
                        } // 代码块结束
                    } else { // 否则分支
                        divider.setVisibility(View.GONE); // 调用setVisibility
                        triggerDetail.setSerialsDetail(getSerialsDetail(serialsList.get(i).getDetailFlag())); // 调用setSerialsDetail
                        int uartDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                        int linDigits = IDigits.DIGITS_16; // 赋值
                        int canDigits = IDigits.DIGITS_16; // 赋值
                        int i2cDigits = IDigits.DIGITS_16; // 赋值
                        int m429LabelDigits = IDigits.DIGITS_8; // 赋值
                        int m429SdiDigits = IDigits.DIGITS_2; // 赋值
                        int m429DataDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                        int m429SsmDigits = IDigits.DIGITS_2; // 赋值
                        int m1553bDigits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                        switch (serialsList.get(i).getDetailFlag()) { // switch分支
                            case UART_DATA: { // case分支
                                SerialsDetailUartData serialsDetail = (SerialsDetailUartData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                int condition = serialsDetail.getUartDataCondition().getIndex(); // 调用getUartDataCondition
                                DataBean data = serialsDetail.getUartDataEdit(); // 调用getUartDataEdit
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case UART_0DATA: { // case分支
                                SerialsDetailUart0Data serialsDetail = (SerialsDetailUart0Data) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                int condition = serialsDetail.getUart0DataCondition().getIndex(); // 调用getUart0DataCondition
                                DataBean data = serialsDetail.getUart0DataEdit(); // 调用getUart0DataEdit
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case UART_1DATA: { // case分支
                                SerialsDetailUart1Data serialsDetail = (SerialsDetailUart1Data) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                int condition = serialsDetail.getUart1DataCondition().getIndex(); // 调用getUart1DataCondition
                                DataBean data = serialsDetail.getUart1DataEdit(); // 调用getUart1DataEdit
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case UART_XDATA: { // case分支
                                SerialsDetailUartxData serialsDetail = (SerialsDetailUartxData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                int condition = serialsDetail.getUartxDataCondition().getIndex(); // 调用getUartxDataCondition
                                DataBean data = serialsDetail.getUartxDataEdit(); // 调用getUartxDataEdit
                                Command.get().getTrigger_uart().setType(cmdCh, i, condition, SerialsUtils.toD(data.getValue(), uartDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case LIN_FRAMEID: { // case分支
                                SerialsDetailLinFrameId serialsDetail = (SerialsDetailLinFrameId) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getLinFrameIdEditEdit(); // 调用getLinFrameIdEditEdit
                                Command.get().getTrigger_lin().setType(cmdCh, i, SerialsUtils.toD(id.getValue(), linDigits), 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case LIN_IDDATA: { // case分支
                                SerialsDetailLinIdData serialsDetail = (SerialsDetailLinIdData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getLinIdDataId(); // 调用getLinIdDataId
                                DataBean data = serialsDetail.getLinIdDataData(); // 调用getLinIdDataData
                                if (id.getValue().contains("X")) { // 条件判断
                                    Command.get().getTrigger_lin().setType(cmdCh, i, getMatchFid(id.getValue(), data.getValue()), SerialsUtils.toDLong(data.getValue(), IDigits.DIGITS_16), false); // 调用get
                                } else { // 否则分支
                                    Command.get().getTrigger_lin().setType(cmdCh, i, SerialsUtils.toD(id.getValue(), linDigits), SerialsUtils.toDLong(data.getValue(), IDigits.DIGITS_16), false); // 调用get
                                } // 代码块结束
                                break; // 跳出分支
                            } // 代码块结束
                            case CAN_REMOTEID: { // case分支
                                SerialsDetailCanRemoteId serialsDetail = (SerialsDetailCanRemoteId) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getCanRemoteIdEdit(); // 调用getCanRemoteIdEdit
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case CAN_DATAID: { // case分支
                                SerialsDetailCanDataId serialsDetail = (SerialsDetailCanDataId) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getCanDataIdEdit(); // 调用getCanDataIdEdit
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case CAN_RDID: { // case分支
                                SerialsDetailCanRdId serialsDetail = (SerialsDetailCanRdId) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getCanRdIdEdit(); // 调用getCanRdIdEdit
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case CAN_IDDATA: { // case分支
                                SerialsDetailCanIdData serialsDetail = (SerialsDetailCanIdData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean id = serialsDetail.getCanIdDataId(); // 调用getCanIdDataId
                                DataBean dlc = serialsDetail.getCanIdDataDlc(); // 调用getCanIdDataDlc
                                DataBean data = serialsDetail.getCanIdDataData(); // 调用getCanIdDataData
                                int iDlc=Integer.parseInt(dlc.getValue()); // 调用parseInt
                                Command.get().getTrigger_can().setType(cmdCh, i, (int) SerialsUtils.toDLong(id.getValue(), canDigits) // 调用get
                                        ,iDlc , SerialsUtils.toDLong(data.getValue(), canDigits), false); // 调用toDLong
                                break; // 跳出分支
                            } // 代码块结束
                            case SPI_DATA: { // case分支
                                SerialsDetailSpiData serialsDetail = (SerialsDetailSpiData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean data = serialsDetail.getSpiDataData(); // 调用getSpiDataData
                                long triggerMask = SerialsUtils.toDLong(SerialsUtils.getSpiMask(data.getValue()), IDigits.DIGITS_2); // 调用toDLong
                                long triggerData = SerialsUtils.toDLong(SerialsUtils.getSpiData(data.getValue()), IDigits.DIGITS_2); // 调用toDLong

                                Command.get().getTrigger_spi().Type(cmdCh,i,false); // 调用get
                                Command.get().getTrigger_spi().Data(cmdCh,data.getValue(),false); // 调用get
                                Command.get().getBus_spi().setType(cmdCh, i, (int) triggerMask, (int) triggerData, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case I2C_NOACKINADR: { // case分支
                                SerialsDetailI2cNoAckInAdr serialsDetail = (SerialsDetailI2cNoAckInAdr) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean addr = serialsDetail.getI2cNoAckInAdrData(); // 调用getI2cNoAckInAdrData
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), 0, 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case I2C_FRAME1: { // case分支
                                SerialsDetailI2cFrame1 serialsDetail = (SerialsDetailI2cFrame1) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean addr = serialsDetail.getI2cFrame1Addr(); // 调用getI2cFrame1Addr
                                DataBean data = serialsDetail.getI2cFrame1Data(); // 调用getI2cFrame1Data
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), SerialsUtils.toD(data.getValue(), i2cDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case I2C_FRAME2: { // case分支
                                SerialsDetailI2cFrame2 serialsDetail = (SerialsDetailI2cFrame2) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean addr = serialsDetail.getI2cFrame2Addr(); // 调用getI2cFrame2Addr
                                DataBean data = serialsDetail.getI2cFrame2Data1(); // 调用getI2cFrame2Data1
                                DataBean data2 = serialsDetail.getI2cFrame2Data2(); // 调用getI2cFrame2Data2
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits) // 调用get
                                        , SerialsUtils.toD(data.getValue(), i2cDigits), SerialsUtils.toD(data2.getValue(), i2cDigits), 0, false); // 调用toD
                                break; // 跳出分支
                            } // 代码块结束
                            case I2C_ROMDATA: { // case分支
                                SerialsDetailI2cRomData serialsDetail = (SerialsDetailI2cRomData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                int condition = serialsDetail.getI2cRomDataCondition().getIndex(); // 调用getI2cRomDataCondition
                                DataBean data = serialsDetail.getI2cRomDataData(); // 调用getI2cRomDataData
                                Command.get().getTrigger_iic().setType(cmdCh, i, 0, SerialsUtils.toD(data.getValue(), i2cDigits), 0, condition, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case I2C_10WRITEFRAME: { // case分支
                                SerialsDetailI2c10WriteFrame serialsDetail = (SerialsDetailI2c10WriteFrame) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean addr = serialsDetail.getI2c10WriteFrameAddr(); // 调用getI2c10WriteFrameAddr
                                DataBean data = serialsDetail.getI2c10WriteFrameData(); // 调用getI2c10WriteFrameData
                                Command.get().getTrigger_iic().setType(cmdCh, i, SerialsUtils.toD(addr.getValue(), i2cDigits), SerialsUtils.toD(data.getValue(), i2cDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_LABEL: { // case分支
                                SerialsDetailArinc429Label serialsDetail = (SerialsDetailArinc429Label) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean label = serialsDetail.getArinc429LabelLabel(); // 调用getArinc429LabelLabel
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits), 0, 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_SDI: { // case分支
                                SerialsDetailArinc429Sdi serialsDetail = (SerialsDetailArinc429Sdi) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean sdi = serialsDetail.getArinc429SdiLabel(); // 调用getArinc429SdiLabel
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, SerialsUtils.toD(sdi.getValue(), m429SdiDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_DATA: { // case分支
                                SerialsDetailArinc429Data serialsDetail = (SerialsDetailArinc429Data) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean data = serialsDetail.getArinc429DataData(); // 调用getArinc429DataData
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, SerialsUtils.toD(data.getValue(), m429DataDigits), 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_SSM: { // case分支
                                SerialsDetailArinc429Ssm serialsDetail = (SerialsDetailArinc429Ssm) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean ssm = serialsDetail.getArinc429SsmLabel(); // 调用getArinc429SsmLabel
                                Command.get().getTrigger_m429().setType(cmdCh, i, 0, 0, 0, SerialsUtils.toD(ssm.getValue(), m429SsmDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_LABELSDI: { // case分支
                                SerialsDetailArinc429LabelSdi serialsDetail = (SerialsDetailArinc429LabelSdi) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean label = serialsDetail.getArinc429LabelSdiLabel(); // 调用getArinc429LabelSdiLabel
                                DataBean sdi = serialsDetail.getArinc429LabelSdiSdi(); // 调用getArinc429LabelSdiSdi
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits) // 调用get
                                        , SerialsUtils.toD(sdi.getValue(), m429SdiDigits), 0, 0, false); // 调用toD
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_LABELDATA: { // case分支
                                SerialsDetailArinc429LabelData serialsDetail = (SerialsDetailArinc429LabelData) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean label = serialsDetail.getArinc429LabelDataLabel(); // 调用getArinc429LabelDataLabel
                                DataBean data = serialsDetail.getArinc429LabelDataData(); // 调用getArinc429LabelDataData
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits) // 调用get
                                        , 0, SerialsUtils.toD(data.getValue(), m429DataDigits), 0, false); // 调用toD
                                break; // 跳出分支
                            } // 代码块结束
                            case ARINC429_LABELSSM: { // case分支
                                SerialsDetailArinc429LabelSsm serialsDetail = (SerialsDetailArinc429LabelSsm) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean label = serialsDetail.getArinc429LabelSsmLabel(); // 调用getArinc429LabelSsmLabel
                                DataBean ssm = serialsDetail.getArinc429LabelSsmSsm(); // 调用getArinc429LabelSsmSsm
                                Command.get().getTrigger_m429().setType(cmdCh, i, SerialsUtils.toD(label.getValue(), m429LabelDigits) // 调用get
                                        , 0, 0, SerialsUtils.toD(ssm.getValue(), m429SsmDigits), false); // 调用toD
                                break; // 跳出分支
                            } // 代码块结束
                            case M1553B_CSWORD: { // case分支
                                SerialsDetailM1553bCsWord serialsDetail = (SerialsDetailM1553bCsWord) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean csWord = serialsDetail.getM1553bCsWordCsWord(); // 调用getM1553bCsWordCsWord
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, SerialsUtils.toD(csWord.getValue(), m1553bDigits), 0, 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case M1553B_RTADDR: { // case分支
                                SerialsDetailM1553bRtAddr serialsDetail = (SerialsDetailM1553bRtAddr) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean rtAddr = serialsDetail.getM1553bRtAddrRtAddr(); // 调用getM1553bRtAddrRtAddr
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, SerialsUtils.toD(rtAddr.getValue(), m1553bDigits), 0, false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                            case M1553B_DATAWORD: { // case分支
                                SerialsDetailM1553bDataWord serialsDetail = (SerialsDetailM1553bDataWord) triggerDetail.getSerialsDetail(); // 调用getSerialsDetail
                                DataBean dataWord = serialsDetail.getM1553bDataWordData(); // 调用getM1553bDataWordData
                                Command.get().getTrigger_m1553B().setType(cmdCh, i, 0, 0, SerialsUtils.toD(dataWord.getValue(), m1553bDigits), false); // 调用get
                                break; // 跳出分支
                            } // 代码块结束
                        } // 代码块结束
                    } // 代码块结束
                    triggerDetail.setSerials(serials); // 调用setSerials
                    sendMsg(isFromEventBus); // 执行语句

                    if (!isFromEventBus) { // 条件判断
                        setTriggerType(serialsList, i); // 执行语句
                    } // 代码块结束
                    break; // 跳出分支
                } // 代码块结束
            } // 代码块结束
        } // 代码块结束
    } // 代码块结束

    /**
     * 设置底层数据
     *
     * @param curSerialsList 当前type列表
     * @param index          当前选中项序号
     */
    private void setTriggerType(List<Serials> curSerialsList, int index) { // setTriggerType方法
        int fpgaChan=TChan.toFpgaBySerialNumber(serialsNumber); // 调用toFpgaBySerialNumber
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan); // 调用getSerialChannel
        if (serialChannel == null) return; // 条件判断
        if (curSerialsList.get(0).getName().equals(uart.get(0).getName())) { // 条件判断
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_START_BIT); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_STOP_BIT); // 调用setTriggerType
                    break; // 跳出分支
                case 2: { // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA); // 调用setTriggerType
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + getSerialsNumber()); // 调用get
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition)); // 调用setTriggerRelation
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA, SerialsUtils.toD(edit, digits)); // 调用setTriggerData
                } // 代码块结束
                break; // 跳出分支
                case 3: { // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA0); // 调用setTriggerType
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + getSerialsNumber()); // 调用get
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition)); // 调用setTriggerRelation
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA0, SerialsUtils.toD(edit, digits)); // 调用setTriggerData
                } // 代码块结束
                break; // 跳出分支
                case 4: { // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATA1); // 调用setTriggerType
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber()); // 调用get
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition)); // 调用setTriggerRelation
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, SerialsUtils.toD(edit, digits)); // 调用setTriggerData
                } // 代码块结束
                break; // 跳出分支
                case 5: { // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_DATAx); // 调用setTriggerType
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + getSerialsNumber()); // 调用get
                    String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    uartBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition)); // 调用setTriggerRelation
                    uartBus.setTriggerData(UartBus.UART_TRIGGER_DATAx, SerialsUtils.toD(edit, digits)); // 调用setTriggerData
                } // 代码块结束
                break; // 跳出分支
                case 6: // case分支
                    uartBus.setTriggerType(UartBus.UART_TRIGGER_OOD_EVEN_BIT_ERROR); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(lin.get(0).getName())) { // 调用get
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_SYNC_RISING_EDGE); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_FRAME_ID); // 调用setTriggerType
                    String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber()); // 调用get
                    linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, SerialsUtils.toD(linFrameIdEdit, IDigits.DIGITS_16)); // 调用setFrameId
                    break; // 跳出分支
                case 2: // case分支
                    String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber()); // 调用get
                    if (dataID.contains("X")) {//代表单独数据触发，设置触发方式 // 条件判断
                        linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA); // 调用setTriggerType
                    } else { // 否则分支
                        linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA); // 调用setTriggerType
                    } // 代码块结束
                    String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber()); // 调用get
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, SerialsUtils.toD(id, IDigits.DIGITS_16)); // 调用setFrameId
                    linBus.setData(SerialsUtils.toDLong(data, IDigits.DIGITS_16)); // 调用setData
                    break; // 跳出分支
                case 3: // case分支
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_PARITY_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 4: // case分支
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_CHECKSUM_ERROR); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(can.get(0).getName())) { // 调用get
            CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_FRAME_START); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID); // 调用setTriggerType
                    String remoteId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + getSerialsNumber()); // 调用get
                    canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_FRAME_ID, SerialsUtils.toD(remoteId, IDigits.DIGITS_16)); // 调用setFrameId
                    break; // 跳出分支
                case 2: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_DATA_FRAME_ID); // 调用setTriggerType
                    String dataId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + getSerialsNumber()); // 调用get
                    canBus.setFrameId(CanBus.CAN_TRIGGER_DATA_FRAME_ID, SerialsUtils.toD(dataId, IDigits.DIGITS_16)); // 调用setFrameId
                    break; // 跳出分支
                case 3: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_REMOTE_DATA_ID); // 调用setTriggerType
                    String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber()); // 调用get
                    canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, SerialsUtils.toD(rdId, IDigits.DIGITS_16)); // 调用setFrameId
                    break; // 跳出分支
                case 4: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ID_AND_DATA); // 调用setTriggerType
                    String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber()); // 调用get
                    String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber()); // 调用get
                    canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, SerialsUtils.toD(id, IDigits.DIGITS_16)); // 调用setFrameId
                    canBus.setDlc(SerialsUtils.getCanDlcFromShow(dlc)); // 调用setDlc
                    canBus.setData(SerialsUtils.toDLong(data, IDigits.DIGITS_16)); // 调用setData
                    break; // 跳出分支
                case 5: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_WRONG_FRAME); // 调用setTriggerType
                    break; // 跳出分支
                case 6: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ALL_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 7: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_ACK_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 8: // case分支
                    canBus.setTriggerType(CanBus.CAN_TRIGGER_OVERLOAD_FRAME); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(spi.get(0).getName())) { // 调用get
            SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_CS); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_DATA); // 调用setTriggerType
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber()); // 调用get
                    spiBus.setTriggerMask((int) SerialsUtils.toDLong(SerialsUtils.getSpiMask(data), IDigits.DIGITS_2)); // 调用setTriggerMask
                    spiBus.setTriggerData((int) SerialsUtils.toDLong(SerialsUtils.getSpiData(data), IDigits.DIGITS_2)); // 调用setTriggerData
                    break; // 跳出分支
                case 2: // case分支
                    spiBus.setTriggerType(SpiBus.SPI_TRIGGER_FRAME_X_DATA); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(i2c.get(0).getName())) { // 调用get
            I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_START_CONDITION); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_STOP_CONDITION); // 调用setTriggerType
                    break; // 跳出分支
                case 2: // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_ACK_LOST); // 调用setTriggerType
                    break; // 跳出分支
                case 3: // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_RESTART); // 调用setTriggerType
                    break; // 跳出分支
                case 4: { // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK); // 调用setTriggerType
                    String noAckInAdr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + getSerialsNumber()); // 调用get
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_ADDRESS_NO_ACK, SerialsUtils.toD(noAckInAdr, IDigits.DIGITS_16)); // 调用setTriggerAddrs
                } // 代码块结束
                break; // 跳出分支
                case 5: { // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_FRAME1); // 调用setTriggerType
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + getSerialsNumber()); // 调用get
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME1, SerialsUtils.toD(addr, IDigits.DIGITS_16)); // 调用setTriggerAddrs
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME1, SerialsUtils.toD(data, IDigits.DIGITS_16)); // 调用setTriggerData1
                } // 代码块结束
                break; // 跳出分支
                case 6: { // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_FRAME2); // 调用setTriggerType
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber()); // 调用get
                    String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber()); // 调用get
                    String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber()); // 调用get
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, SerialsUtils.toD(addr, IDigits.DIGITS_16)); // 调用setTriggerAddrs
                    i2cBus.setTriggerData1(SerialsUtils.toD(data1, IDigits.DIGITS_16)); // 调用setTriggerData1
                    i2cBus.setTriggerData2(SerialsUtils.toD(data2, IDigits.DIGITS_16)); // 调用setTriggerData2
                } // 代码块结束
                break; // 跳出分支
                case 7: { // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA); // 调用setTriggerType
                    int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber()); // 调用get
                    i2cBus.setTriggerRelation(SerialsUtils.getConditionValueToEventBus(condition)); // 调用setTriggerRelation
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, SerialsUtils.toD(data, IDigits.DIGITS_16)); // 调用setTriggerData1
                } // 代码块结束
                break; // 跳出分支
                case 8: { // case分支
                    i2cBus.setTriggerType(I2CBus.I2C_TRIGGER_WRITE_FRAME); // 调用setTriggerType
                    String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + getSerialsNumber()); // 调用get
                    i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_WRITE_FRAME, SerialsUtils.toD(addr, IDigits.DIGITS_16)); // 调用setTriggerAddrs
                    i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_WRITE_FRAME, SerialsUtils.toD(data, IDigits.DIGITS_16)); // 调用setTriggerData1
                } // 代码块结束
                break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(m429.get(0).getName())) { // 调用get
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_BEGIN); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_END); // 调用setTriggerType
                    break; // 跳出分支
                case 2: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL); // 调用setTriggerType
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber()); // 调用get
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, SerialsUtils.toD(label, IDigits.DIGITS_8)); // 调用setLabel
                } // 代码块结束
                break; // 跳出分支
                case 3: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_SDI); // 调用setTriggerType
                    String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SDI + getSerialsNumber()); // 调用get
                    a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_SDI, SerialsUtils.toD(sdi, IDigits.DIGITS_2)); // 调用setSdi
                } // 代码块结束
                break; // 跳出分支
                case 4: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_DATA); // 调用setTriggerType
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    data = SerialsUtils.reCalcSpace(SerialsUtils.HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits); // 调用reCalcSpace
                    a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_DATA, SerialsUtils.toD(data, digits)); // 调用setData
                } // 代码块结束
                break; // 跳出分支
                case 5: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_SSM); // 调用setTriggerType
                    String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_SSM + getSerialsNumber()); // 调用get
                    a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_SSM, SerialsUtils.toD(ssm, IDigits.DIGITS_2)); // 调用setSSM
                } // 代码块结束
                break; // 跳出分支
                case 6: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI); // 调用setTriggerType
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber()); // 调用get
                    String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber()); // 调用get
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, SerialsUtils.toD(label, IDigits.DIGITS_8)); // 调用setLabel
                    a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, SerialsUtils.toD(sdi, IDigits.DIGITS_2)); // 调用setSdi
                } // 代码块结束
                break; // 跳出分支
                case 7: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA); // 调用setTriggerType
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + getSerialsNumber()); // 调用get
                    String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    data = SerialsUtils.reCalcSpace(SerialsUtils.HexBin(data, digits, digits), SerialsUtils.getBitFor429Data(getSerialsNumber()), digits); // 调用reCalcSpace
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, SerialsUtils.toD(label, IDigits.DIGITS_8)); // 调用setLabel
                    a429Bus.setData(ARINC429Bus.ARINC429_TRIGGER_LABEL_DATA, SerialsUtils.toD(data, digits)); // 调用setData
                } // 代码块结束
                break; // 跳出分支
                case 8: { // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM); // 调用setTriggerType
                    String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + getSerialsNumber()); // 调用get
                    String ssm = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + getSerialsNumber()); // 调用get
                    a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, SerialsUtils.toD(label, IDigits.DIGITS_8)); // 调用setLabel
                    a429Bus.setSSM(ARINC429Bus.ARINC429_TRIGGER_LABEL_SSM, SerialsUtils.toD(ssm, IDigits.DIGITS_2)); // 调用setSSM
                } // 代码块结束
                break; // 跳出分支
                case 9: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 10: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_WORD_INTERVAL); // 调用setTriggerType
                    break; // 跳出分支
                case 11: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_VERIFY_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 12: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 13: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_0); // 调用setTriggerType
                    break; // 跳出分支
                case 14: // case分支
                    a429Bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_ALL_1); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } else if (curSerialsList.get(0).getName().equals(m1553b.get(0).getName())) { // 调用get
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B); // 调用getBus
            switch (index) { // switch分支
                case 0: // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_SYNC); // 调用setTriggerType
                    break; // 跳出分支
                case 1: // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD_SYNC); // 调用setTriggerType
                    break; // 跳出分支
                case 2: { // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_COMMAND_STATUS_WORD); // 调用setTriggerType
                    String csWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    m1553bBus.setCmdStatus(SerialsUtils.toD(csWord, digits)); // 调用setCmdStatus
                } // 代码块结束
                break; // 跳出分支
                case 3: { // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_RT_ADDRESS); // 调用setTriggerType
                    String rtAddr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    m1553bBus.setAddr(SerialsUtils.toD(rtAddr, digits)); // 调用setAddr
                } // 代码块结束
                break; // 跳出分支
                case 4: // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_MANCHESTER_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 5: { // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_DATA_WORD); // 调用setTriggerType
                    String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber()); // 调用get
                    int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16; // 调用get
                    m1553bBus.setData(SerialsUtils.toD(dataWord, digits)); // 调用setData
                } // 代码块结束
                break; // 跳出分支
                case 6: // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_ODD_PARITY_ERROR); // 调用setTriggerType
                    break; // 跳出分支
                case 7: // case分支
                    m1553bBus.setTriggerType(MILSTD1553BBus.MILSTD1553B_TRIGGER_ALL_ERROR); // 调用setTriggerType
                    break; // 跳出分支
            } // 代码块结束
        } // 代码块结束
    } // 代码块结束

    private ISerialsDetail getSerialsDetail(int detailFlag) { // getSerialsDetail方法
        TopLayoutTriggerSerialsBaseDetail layout = getFragmentForFlag(detailFlag); // 赋值
        return layout.getSerialsDetail(detailFlag); // 返回值
    } // 代码块结束

    private TopLayoutTriggerSerialsBaseDetail.OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener = new TopLayoutTriggerSerialsBaseDetail.OnSerialsDetailSendMsgListener() { // {
        @Override
        public void onClick(Fragment detail, ISerialsDetail serialsDetail, boolean isFromEventBus) { // onClick方法
            if (adapter.getSelected() != null) { // 条件判断
                triggerDetail.setSerials(adapter.getSelected()); // 调用setSerials
                triggerDetail.getSerials().setRxMsgSelect(false); // 调用getSerials
                triggerDetail.setSerialsDetail(serialsDetail); // 调用setSerialsDetail
                sendMsg(isFromEventBus); // 执行语句
            } // 代码块结束
        } // 代码块结束
    }; // 执行语句

    private int getMatchFid(String id, String data) { // getMatchFid方法
        int dataLength = data.replace(" ", "").trim().length(); // 调用replace
        if (id.contains("X")) { // 条件判断
            if (dataLength <= 4) { // 条件判断
                id = "1F"; // 赋值
            } else if (dataLength <= 8) { // 赋值
                id = "2F"; // 赋值
            } else { // 否则分支
                id = "3F"; // 赋值
            } // 代码块结束
        } // 代码块结束
        return SerialsUtils.toD(id, 16); // 返回值
    } // 代码块结束

} // 代码块结束
