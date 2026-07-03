package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.SpiBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                       SPI串口协议配置布局                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— SPI协议参数配置的UI布局           |
 * | 核心职责: 提供SPI总线的CLK/DATA/CS通道选择、位数、各信号极性及           |
 * |          CS使能开关配置界面，处理通道冲突自动调整和外部事件               |
 * | 架构设计: LinearLayout子类，包含clk/data/cs/bits/clkCheck/dataCheck/     |
 * |          csCheck/csSwitch控件，采用观察者模式监听RxBus和EventBus事件      |
 * | 数据流向: 用户UI → RightLayoutSerialsSpi → Command(SCPI) → 硬件         |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsSpi, SpiBus, Command,         |
 * |          ChannelFactory, CacheUtil, RxBus, MSwitchBox                    |
 * | 使用场景: 用户在右侧菜单选择SPI协议后，此布局显示SPI参数配置             |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * SPI串口协议配置布局
 * <p>
 * 提供SPI总线的时钟(CLK)、数据(DATA)、片选(CS)通道选择、
 * 数据位数、各信号低电平有效标志及CS使能开关等参数的配置界面。
 * 当CLK/DATA/CS选择同一通道时自动调整避免冲突。
 * </p>
 */
public class RightLayoutSerialsSpi extends LinearLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** CLK/DATA/CS/位数选择控件 */
    private RightViewSelect vClk, vData, vCs, vBits; // SPI通道和位数选择控件
    /** CLK/DATA/CS极性选择控件 */
    private RightViewSelect rgClkCheck, rgDataCheck, rgCsCheck; // 信号极性选择控件
    /** CS使能开关 */
    private MSwitchBox csSwitch; // CS使能开关

    /** SPI协议详情消息体 */
    private RightMsgSerialsSpi msgDetailsSpi; // SPI协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** SPI总线数据对象 */
    private SpiBus spiBus; // SPI总线数据对象

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsSpi(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsSpi(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsSpi(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_spi, this); // 加载SPI布局
        setOrientation(VERTICAL); // 设置垂直排列
        vClk = (RightViewSelect) findViewById(R.id.clk); // 时钟线选择控件
        vData = (RightViewSelect) findViewById(R.id.data); // 数据线选择控件
        vCs = (RightViewSelect) findViewById(R.id.cs); // 片选线选择控件
        vBits = (RightViewSelect) findViewById(R.id.bit); // 位数选择控件
        rgClkCheck = (RightViewSelect) findViewById(R.id.clkCheck); // 时钟极性选择控件
        rgDataCheck = (RightViewSelect) findViewById(R.id.dataCheck); // 数据极性选择控件
        rgCsCheck = (RightViewSelect) findViewById(R.id.csCheck); // 片选极性选择控件
        csSwitch = (MSwitchBox) findViewById(R.id.csSwith); // CS使能开关控件
        RelativeLayout csLayout = (RelativeLayout) findViewById(R.id.csLayout); // CS布局容器

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 2通道设备
            csLayout.setVisibility(GONE); // 隐藏CS相关控件
        } else { // 4通道设备
            csLayout.setVisibility(VISIBLE); // 显示CS相关控件
        }
        String[] channels = GlobalVar.get().getChannelsName(); // 获取通道名称数组
        vClk.setArray(channels); // 设置CLK通道选项
        vData.setArray(channels); // 设置DATA通道选项
        vCs.setArray(channels); // 设置CS通道选项

        vClk.setOnItemClickListener(onItemClickListener); // 绑定CLK点击监听
        vData.setOnItemClickListener(onItemClickListener); // 绑定DATA点击监听
        vCs.setOnItemClickListener(onItemClickListener); // 绑定CS点击监听
        vBits.setOnItemClickListener(onItemClickListener); // 绑定位数点击监听
        rgClkCheck.setOnItemClickListener(onItemClickListener); // 绑定CLK极性点击监听
        rgDataCheck.setOnItemClickListener(onItemClickListener); // 绑定DATA极性点击监听
        rgCsCheck.setOnItemClickListener(onItemClickListener); // 绑定CS极性点击监听
        csSwitch.setOnToggleStateChangedListener(onToggleStateChangedListener); // 绑定CS开关监听

        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsSpi = new RightMsgSerialsSpi(); // 创建SPI详情消息对象
        msgDetailsSpi.setClk(vClk.getSelectItem()); // 设置当前CLK
        vData.setSelectIndex(1); // DATA默认选择第二个通道
        msgDetailsSpi.setData(vData.getSelectItem()); // 设置当前DATA
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 2通道设备
            vCs.setSelectIndex(0); // CS默认选择第一个通道
        } else { // 4通道设备
            vCs.setSelectIndex(2); // CS默认选择第三个通道
        }
        msgDetailsSpi.setCs(vCs.getSelectItem()); // 设置当前CS
        msgDetailsSpi.setBit(vBits.getSelectItem()); // 设置当前位数
        msgDetailsSpi.setClkLow(false); // CLK默认高电平有效
        msgDetailsSpi.setDataLow(false); // DATA默认高电平有效
        msgDetailsSpi.setCsLow(false); // CS默认高电平有效
        msgDetailsSpi.setCsSwitch(false); // CS默认禁用
    }

    /**
     * 从缓存恢复SPI配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    void setCache() {
        int clk = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber); // 读取缓存CLK通道索引
        int data = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber); // 读取缓存DATA通道索引
        int cs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber); // 读取缓存CS通道索引
        int bit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_BIT + serialsNumber); // 读取缓存位数索引
        boolean clkCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLKCHECK + serialsNumber); // 读取CLK极性
        boolean dataCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATACHECK + serialsNumber); // 读取DATA极性
        boolean csCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSCHECK + serialsNumber); // 读取CS极性
        boolean csSwitch = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber); // 读取CS使能状态
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2 && cs >= 2) { // 2通道设备CS越界修正
            cs = 0; // 重置CS为通道0
        }
        vClk.setSelectIndex(clk); // 设置CLK选中项
        vData.setSelectIndex(data); // 设置DATA选中项
        vCs.setSelectIndex(cs); // 设置CS选中项
        vBits.setSelectIndex(bit); // 设置位数选中项
        setRadioGroupCheck(rgClkCheck, clkCheck); // 设置CLK极性选中项
        setRadioGroupCheck(rgDataCheck, dataCheck); // 设置DATA极性选中项
        setRadioGroupCheck(rgCsCheck, csCheck); // 设置CS极性选中项
        this.csSwitch.setState(csSwitch); // 设置CS开关状态
        vCs.setEnabled(csSwitch); // CS通道选择是否可用取决于开关
        rgCsCheck.setEnabled(csSwitch); // CS极性选择是否可用取决于开关

        Command.get().getBus_spi().setClock(serialsNumber - 1, clk, false); // 下发CLK通道SCPI
        Command.get().getBus_spi().setClockSwitch(serialsNumber - 1, clkCheck ? 1 : 0, false); // 下发CLK极性SCPI
        Command.get().getBus_spi().setData(serialsNumber - 1, data, false); // 下发DATA通道SCPI
        Command.get().getBus_spi().setDataSwitch(serialsNumber - 1, dataCheck ? 1 : 0, false); // 下发DATA极性SCPI
        Command.get().getBus_spi().setCs(serialsNumber - 1, cs, false); // 下发CS通道SCPI
        Command.get().getBus_spi().setCsSwitch(serialsNumber - 1, csCheck ? 1 : 0, false); // 下发CS极性SCPI
        Command.get().getBus_spi().setCsEnable(serialsNumber - 1, csSwitch , false); // 下发CS使能SCPI
        Command.get().getBus_spi().setBits(serialsNumber - 1,
                Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", "")), false); // 下发位数SCPI



        spiBus.setClkChIdx(clk); // 同步SpiBus CLK通道索引
        spiBus.setClkSample(clkCheck ? SpiBus.SPI_CLK_FALL_EDGE : SpiBus.SPI_CLK_RISE_EDGE); // 同步CLK采样边沿
        spiBus.setDataChIdx(data); // 同步SpiBus DATA通道索引
        spiBus.setDataIdleLevel(dataCheck ? SpiBus.IDLE_LEVEL_LOW : SpiBus.IDLE_LEVEL_HIGH); // 同步DATA空闲电平
        spiBus.setCsChIdx(cs); // 同步SpiBus CS通道索引
        spiBus.setCsIdleLevel(csCheck ? SpiBus.IDLE_LEVEL_LOW : SpiBus.IDLE_LEVEL_HIGH); // 同步CS空闲电平
        spiBus.setCsValid(csSwitch); // 同步CS使能状态
        spiBus.setBits(Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", ""))); // 同步位数

        int ch=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
        SerialBusManage.getInstance().getSerialBus(ch).setSpiBits(bit); // 设置串口总线的SPI位数

        msgDetailsSpi.setClk(this.vClk.getSelectItem()); // 同步消息体CLK
        msgDetailsSpi.setData(this.vData.getSelectItem()); // 同步消息体DATA
        msgDetailsSpi.setCs(this.vCs.getSelectItem()); // 同步消息体CS
        msgDetailsSpi.setBit(this.vBits.getSelectItem()); // 同步消息体位数
        msgDetailsSpi.setClkLow(clkCheck); // 同步消息体CLK极性
        msgDetailsSpi.setDataLow(dataCheck); // 同步消息体DATA极性
        msgDetailsSpi.setCsLow(csCheck); // 同步消息体CS极性
        msgDetailsSpi.setCsSwitch(csSwitch); // 同步消息体CS使能
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.SPI) { // 当前串口类型为SPI时
            sendMsg(false); // 发送消息通知父布局
        }
    }

    /**
     * 设置极性选择控件的选中索引
     *
     * @param viewSelect 极性选择控件
     * @param check      true=低电平有效(索引1)，false=高电平有效(索引0)
     */
    private void setRadioGroupCheck(RightViewSelect viewSelect, boolean check) {
        int number = check ? 1 : 0;
        viewSelect.setSelectIndex(number);
    }

    /** 初始化控件，订阅RxBus和EventBus事件 */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    /**
     * 设置串口通道编号，并获取对应的SpiBus对象
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber; // 保存串口编号
        int tChan=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
        int fpgaChan=TChan.toFpgaChNo(tChan); // 转换为FPGA通道编号
        setControlColorByChIdx(tChan); // 根据通道设置控件颜色
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan); // 获取串口通道对象
        if (serialChannel == null) return; // 通道为空则返回
        spiBus = (SpiBus) serialChannel.getBus(IBus.SPI); // 从通道获取SPI总线对象
    }

    /**
     * 根据通道索引设置所有选择控件的主题颜色
     *
     * @param chIdx TChan通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vClk.setControlColorByChIdx(chIdx); // 设置CLK控件颜色
        vData.setControlColorByChIdx(chIdx); // 设置DATA控件颜色
        vCs.setControlColorByChIdx(chIdx); // 设置CS控件颜色
        vBits.setControlColorByChIdx(chIdx); // 设置位数控件颜色
        rgClkCheck.setControlColorByChIdx(chIdx); // 设置CLK极性控件颜色
        rgDataCheck.setControlColorByChIdx(chIdx); // 设置DATA极性控件颜色
        rgCsCheck.setControlColorByChIdx(chIdx); // 设置CS极性控件颜色
    }

    /**
     * 获取SPI协议详情消息体
     *
     * @return SPI详情消息对象
     */
    public RightMsgSerialsSpi getMsgDetailsSpi() {
        return msgDetailsSpi; // 返回SPI详情消息体
    }

    /**
     * 设置详情变更发送消息的监听器
     *
     * @param onSerialsDetailSendMsgListener 消息发送监听器
     */
    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener; // 保存监听器引用
    }

    /**
     * 通过监听器发送消息通知父布局SPI配置已变更
     *
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) { // 监听器非空时
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsSpi.this, isFromEventBus); // 回调通知父布局
        }
    }

    /** RxBus缓存加载消费者，接收到MAIN_LOAD_CACHE事件时从缓存恢复SPI配置 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道为空则跳过
            setCache(); // 从缓存恢复SPI配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsSpi, true); // 标记SPI缓存已加载
        }
    };

    /** RxBus命令回传消费者，接收硬件返回的SPI参数变更通知并同步到UI */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志位分发处理
                case CommandMsgToUI.FLAG_TRIGGERSPI_CLOCK: { // CLK通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& Integer.parseInt(params[1]) != vClk.getSelectIndex()*/) {
                        vClk.setSelectIndex(Integer.parseInt(params[1])); // 更新CLK选中项
                        onCheckChanged(vClk.getId(), vClk.getSelectItem(), false); // 触发冲突调整
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CLOCKSWITCH: { // CLK极性变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int clkIndex = Integer.parseInt(params[1]); // 获取极性索引
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& clkIndex != rgClkCheck.getSelectIndex()*/) {
                        rgClkCheck.setSelectIndex(clkIndex); // 更新CLK极性选中项
                        onCheckChanged(rgClkCheck.getId(), rgClkCheck.getSelectItem(), false); // 触发极性变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_DATA: { // DATA通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& Integer.parseInt(params[1]) != vData.getSelectIndex()*/) {
                        vData.setSelectIndex(Integer.parseInt(params[1])); // 更新DATA选中项
                        onCheckChanged(vData.getId(), vData.getSelectItem(), false); // 触发冲突调整
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_DATASWITCH: { // DATA极性变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int dataIndex = Integer.parseInt(params[1]); // 获取极性索引
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& dataIndex != rgDataCheck.getSelectIndex()*/) {
                        rgDataCheck.setSelectIndex(dataIndex); // 更新DATA极性选中项
                        onCheckChanged(rgDataCheck.getId(), rgDataCheck.getSelectItem(), false); // 触发极性变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CS: { // CS通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& Integer.parseInt(params[1]) != vCs.getSelectIndex()*/) {
                        vCs.setSelectIndex(Integer.parseInt(params[1])); // 更新CS选中项
                        onCheckChanged(vCs.getId(), vCs.getSelectItem(), false); // 触发冲突调整
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CSWITCH: { // CS极性变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int csIndex = Integer.parseInt(params[1]); // 获取极性索引
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                           /* && csIndex != rgCsCheck.getSelectIndex()*/) {
                        rgCsCheck.setSelectIndex(csIndex); // 更新CS极性选中项
                        onCheckChanged(rgCsCheck.getId(), rgCsCheck.getSelectItem(), false); // 触发极性变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_CSENABLE: { // CS使能开关变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    boolean b=Boolean.parseBoolean(params[1]); // 获取使能状态
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) { // 匹配当前串口编号
                        csSwitch.setState(b); // 更新CS开关状态
                        onToggleStateChangedListener.onToggleStateChanged(csSwitch, csSwitch.isState()); // 触发开关变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERSPI_BITS: { // 位数变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            /*&& Integer.parseInt(params[1]) != vBits.getSelectIndex()*/) {
                        vBits.setSelectIndex(Integer.parseInt(params[1])); // 更新位数选中项
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false); // 触发位数变更处理
                    }
                    break;
                }
            }
        }
    };

    /** EventBus观察者，监听SpiBus参数变更事件，从SpiBus对象同步参数到UI控件 */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 转换事件数据类型
            if (spiBus == null) return; // SpiBus未初始化则跳过
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 匹配总线参数事件ID
                    && spiBus.equals(eventBase.getData())) { // 匹配当前SpiBus对象
                if (spiBus.getClkChIdx() != vClk.getSelectIndex()) { // CLK通道不一致
                    vClk.setSelectIndex(spiBus.getClkChIdx()); // 同步CLK选中项
                    onCheckChanged(vClk.getId(), vClk.getSelectItem(), true); // 触发冲突调整（来自EventBus）
                }
                boolean clkCheck = spiBus.getClkSample() == SpiBus.SPI_CLK_FALL_EDGE; // 判断CLK是否下降沿采样
                if (rgClkCheck.getSelectIndex() != spiBus.getClkSample()) { // CLK极性不一致
                    setRadioGroupCheck(rgClkCheck, clkCheck); // 同步CLK极性选中项
                    onCheckChanged(rgClkCheck.getId(), rgClkCheck.getSelectItem(), true); // 触发极性变更（来自EventBus）
                }
                if (spiBus.getDataChIdx() != vData.getSelectIndex()) { // DATA通道不一致
                    vData.setSelectIndex(spiBus.getDataChIdx()); // 同步DATA选中项
                    onCheckChanged(vData.getId(), vData.getSelectItem(), true); // 触发冲突调整（来自EventBus）
                }
                boolean dataCheck = spiBus.getDataIdleLevel() == SpiBus.IDLE_LEVEL_LOW; // 判断DATA是否低电平空闲
                if (rgDataCheck.getSelectIndex() != spiBus.getDataIdleLevel()) { // DATA极性不一致
                    setRadioGroupCheck(rgDataCheck, dataCheck); // 同步DATA极性选中项
                    onCheckChanged(rgDataCheck.getId(), rgDataCheck.getSelectItem(), true); // 触发极性变更（来自EventBus）
                }
                if (spiBus.getCsChIdx() != vCs.getSelectIndex()) { // CS通道不一致
                    vCs.setSelectIndex(spiBus.getCsChIdx()); // 同步CS选中项
                    onCheckChanged(vCs.getId(), vCs.getSelectItem(), true); // 触发冲突调整（来自EventBus）
                }
                boolean csCheck = spiBus.getCsIdleLevel() == SpiBus.IDLE_LEVEL_LOW; // 判断CS是否低电平空闲
                if (rgCsCheck.getSelectIndex() != spiBus.getCsIdleLevel()) { // CS极性不一致
                    setRadioGroupCheck(rgCsCheck, csCheck); // 同步CS极性选中项
                    onCheckChanged(rgCsCheck.getId(), rgCsCheck.getSelectItem(), true); // 触发极性变更（来自EventBus）
                }
                if (csSwitch.isState() != spiBus.isCsValid()) { // CS使能状态不一致
                    csSwitch.setState(spiBus.isCsValid()); // 同步CS开关状态
                    onSwitchChanged(csSwitch.getId(), csSwitch.isState(), true); // 触发开关变更（来自EventBus）
                }
                String bits = spiBus.getBits() + "bit"; // 构造位数显示文本（如"8bit"）
                if (!vBits.getSelectItem().getText().equals(bits)) { // 位数不一致
                    if (vBits.setSelectText(bits)) { // 按文本匹配设置选中项
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), true); // 触发位数变更（来自EventBus）
                    }
                }
            }
        }
    };

    /** CS开关状态变更监听器，用户切换CS使能开关时触发 */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton(); // 播放按键音效
            onSwitchChanged(csSwitch.getId(), state, false); // 处理CS开关变更（来自UI操作）
        }
    };

    /** 选择控件点击监听器，处理CLK/DATA/CS/位数/极性选项的点击事件 */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            onCheckChanged(viewId, item, false); // 处理选项变更（来自UI操作）
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 刷新UI后回调（SPI未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 刷新UI前回调（SPI未使用）
        }
    };

    /**
     * SPI选项变更核心处理方法，处理CLK/DATA/CS/位数/极性的选择变更，
     * 自动调整通道冲突（CLK/DATA/CS不允许选择同一通道），
     * 同步SpiBus对象、下发SCPI命令、更新缓存和消息体
     *
     * @param viewId        触发变更的控件ID
     * @param item          选中的选项项
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vClk.getId()) { // CLK通道变更
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setClkChIdx(item.getIndex()); // 设置SpiBus的CLK通道索引
            }

            msgDetailsSpi.setClk(item); // 更新消息体CLK选项
            if (vData.getSelectIndex() == item.getIndex()) { // DATA与CLK冲突，需自动调整DATA
                int index = vData.getSelectIndex(); // 获取DATA当前索引
                vData.setSelectIndex(index == vData.getSelectCount() - 1 ? 0 : index + 1); // DATA跳到下一个可用通道
                if (!isFromEventBus) {
                    spiBus.setDataChIdx(vData.getSelectIndex()); // 同步SpiBus的DATA通道
                }
            }
            if (csSwitch.isState()) { // CS使能开启时需检查CS冲突
                if (vCs.getSelectIndex() == item.getIndex()) { // CS与CLK冲突
                    int index = vCs.getSelectIndex(); // 获取CS当前索引
                    int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1; // CS跳到下一个可用通道
                    if (index2 != vData.getSelectIndex()) { // 新位置与DATA不冲突
                        vCs.setSelectIndex(index2); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    } else { // 新位置仍与DATA冲突，再跳一步
                        int index3 = ((index2 == vCs.getSelectCount() - 1) ? 0 : index2 + 1); // 再跳一步
                        vCs.setSelectIndex(index3); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    }
                } else { // CS与CLK不冲突，但需检查CS与DATA是否冲突
                    if (vCs.getSelectIndex() == vData.getSelectIndex()) { // CS与DATA冲突
                        int index = vCs.getSelectIndex(); // 获取CS当前索引
                        int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1; // CS跳到下一个可用通道
                        vCs.setSelectIndex(index2); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex())); // 缓存CLK索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex())); // 缓存DATA索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex())); // 缓存CS索引
            msgDetailsSpi.setData(vData.getSelectItem()); // 同步消息体DATA
            msgDetailsSpi.setCs(vCs.getSelectItem()); // 同步消息体CS
            msgDetailsSpi.setClk(vClk.getSelectItem()); // 同步消息体CLK
            sendMsg(isFromEventBus); // 通知父布局
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false); // 下发CLK通道SCPI
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false); // 下发DATA通道SCPI
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false); // 下发CS通道SCPI
        } else if (viewId == vData.getId()) { // DATA通道变更
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setDataChIdx(item.getIndex()); // 设置SpiBus的DATA通道索引
            }

            msgDetailsSpi.setData(item); // 更新消息体DATA选项
            if (vClk.getSelectIndex() == item.getIndex()) { // CLK与DATA冲突，需自动调整CLK
                int index = vClk.getSelectIndex(); // 获取CLK当前索引
                vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1); // CLK跳到下一个可用通道
                if (!isFromEventBus) {
                    spiBus.setClkChIdx(vClk.getSelectIndex()); // 同步SpiBus的CLK通道
                }
            }
            if (csSwitch.isState()) { // CS使能开启时需检查CS冲突
                if (vCs.getSelectIndex() == item.getIndex()) { // CS与DATA冲突
                    int index = vCs.getSelectIndex(); // 获取CS当前索引
                    int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1; // CS跳到下一个可用通道
                    if (index2 != vClk.getSelectIndex()) { // 新位置与CLK不冲突
                        vCs.setSelectIndex(index2); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    } else { // 新位置仍与CLK冲突，再跳一步
                        int index3 = ((index2 == vCs.getSelectCount() - 1) ? 0 : index2 + 1); // 再跳一步
                        vCs.setSelectIndex(index3); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    }
                } else { // CS与DATA不冲突，但需检查CS与CLK是否冲突
                    if (vCs.getSelectIndex() == vClk.getSelectIndex()) { // CS与CLK冲突
                        int index = vCs.getSelectIndex(); // 获取CS当前索引
                        int index2 = index == vCs.getSelectCount() - 1 ? 0 : index + 1; // CS跳到下一个可用通道
                        vCs.setSelectIndex(index2); // 设置CS为新位置
                        if (!isFromEventBus) {
                            spiBus.setCsChIdx(vCs.getSelectIndex()); // 同步SpiBus的CS通道
                        }
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex())); // 缓存CLK索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex())); // 缓存DATA索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex())); // 缓存CS索引
            msgDetailsSpi.setClk(vClk.getSelectItem()); // 同步消息体CLK
            msgDetailsSpi.setCs(vCs.getSelectItem()); // 同步消息体CS
            msgDetailsSpi.setData(vData.getSelectItem()); // 同步消息体DATA
            sendMsg(isFromEventBus); // 通知父布局
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false); // 下发CLK通道SCPI
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false); // 下发DATA通道SCPI
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false); // 下发CS通道SCPI
        } else if (viewId == vCs.getId()) { // CS通道变更
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setCsChIdx(item.getIndex()); // 设置SpiBus的CS通道索引
            }

            msgDetailsSpi.setCs(item); // 更新消息体CS选项
            if (vClk.getSelectIndex() == item.getIndex()) { // CLK与CS冲突，需自动调整CLK
                int index = vClk.getSelectIndex(); // 获取CLK当前索引
                vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1); // CLK跳到下一个可用通道
                if (!isFromEventBus) {
                    spiBus.setClkChIdx(vClk.getSelectIndex()); // 同步SpiBus的CLK通道
                }
            }
            if (vData.getSelectIndex() == item.getIndex()) { // DATA与CS冲突，需自动调整DATA
                int index = vData.getSelectIndex(); // 获取DATA当前索引
                int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1; // DATA跳到下一个可用通道
                if (index2 != vClk.getSelectIndex()) { // 新位置与CLK不冲突
                    vData.setSelectIndex(index2); // 设置DATA为新位置
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex()); // 同步SpiBus的DATA通道
                    }
                } else { // 新位置仍与CLK冲突，再跳一步
                    int index3 = ((index2 == vData.getSelectCount() - 1) ? 0 : index2 + 1); // 再跳一步
                    vData.setSelectIndex(index3); // 设置DATA为新位置
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex()); // 同步SpiBus的DATA通道
                    }
                }
            } else { // DATA与CS不冲突，但需检查DATA与CLK是否冲突
                if (vData.getSelectIndex() == vClk.getSelectIndex()) { // DATA与CLK冲突
                    int index = vData.getSelectIndex(); // 获取DATA当前索引
                    int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1; // DATA跳到下一个可用通道
                    vData.setSelectIndex(index2); // 设置DATA为新位置
                    if (!isFromEventBus) {
                        spiBus.setDataChIdx(vData.getSelectIndex()); // 同步SpiBus的DATA通道
                    }
                }
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex())); // 缓存CLK索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex())); // 缓存DATA索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + serialsNumber, String.valueOf(vCs.getSelectIndex())); // 缓存CS索引
            msgDetailsSpi.setClk(vClk.getSelectItem()); // 同步消息体CLK
            msgDetailsSpi.setData(vData.getSelectItem()); // 同步消息体DATA
            msgDetailsSpi.setCs(vCs.getSelectItem()); // 同步消息体CS
            sendMsg(isFromEventBus); // 通知父布局
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false); // 下发CLK通道SCPI
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false); // 下发DATA通道SCPI
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false); // 下发CS通道SCPI
        } else if (viewId == vBits.getId()) { // 位数变更
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setBits(Integer.parseInt(item.getText().replace("bit", ""))); // 解析位数（如"8bit"→8）
            }
            Command.get().getBus_spi().setBits(serialsNumber - 1, item.getIndex(), false); // 下发位数SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_BIT + serialsNumber, String.valueOf(item.getIndex())); // 缓存位数索引
            msgDetailsSpi.setBit(item); // 更新消息体位数
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setSpiBits(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setSpiBits(item.getIndex());
//            }
            int ch= TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
            SerialBusManage.getInstance().getSerialBus(ch).setSpiBits(item.getIndex()); // 设置串口总线SPI位数

            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == rgClkCheck.getId()) { // CLK极性变更
            int index = rgClkCheck.getSelectIndex(); // 获取极性索引（0=上升沿/1=下降沿）
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setClkSample(index); // 设置CLK采样边沿
            }
            Command.get().getBus_spi().setClockSwitch(serialsNumber - 1, index, false); // 下发CLK极性SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLKCHECK + serialsNumber, String.valueOf(index == 1)); // 缓存CLK极性
            msgDetailsSpi.setClkLow(index == 1); // 更新消息体CLK极性（1=低电平有效）
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == rgDataCheck.getId()) { // DATA极性变更
            int index = rgDataCheck.getSelectIndex(); // 获取极性索引（0=高电平/1=低电平）
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setDataIdleLevel(index); // 设置DATA空闲电平
            }
            Command.get().getBus_spi().setDataSwitch(serialsNumber - 1, index, false); // 下发DATA极性SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATACHECK + serialsNumber, String.valueOf(index == 1)); // 缓存DATA极性
            msgDetailsSpi.setDataLow(index == 1); // 更新消息体DATA极性（1=低电平有效）
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == rgCsCheck.getId()) { // CS极性变更
            int index = rgCsCheck.getSelectIndex(); // 获取极性索引（0=高电平/1=低电平）
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setCsIdleLevel(index); // 设置CS空闲电平
            }
            Command.get().getBus_spi().setCsSwitch(serialsNumber - 1, index, false); // 下发CS极性SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSCHECK + serialsNumber, String.valueOf(index == 1)); // 缓存CS极性
            msgDetailsSpi.setCsLow(index == 1); // 更新消息体CS极性（1=低电平有效）
            sendMsg(isFromEventBus); // 通知父布局
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
    }

    /**
     * CS使能开关变更处理方法，开启CS时自动调整CLK/DATA避免与CS通道冲突，
     * 关闭CS时禁用CS相关控件
     *
     * @param viewId        触发变更的控件ID
     * @param state         开关新状态（true=开启，false=关闭）
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onSwitchChanged(int viewId, boolean state, boolean isFromEventBus) {
        if (viewId == csSwitch.getId()) { // CS开关变更
            msgDetailsSpi.setCsSwitch(state); // 更新消息体CS使能状态
            vCs.setEnabled(state); // CS通道选择可用性随开关状态变化
            rgCsCheck.setEnabled(state); // CS极性选择可用性随开关状态变化
            if (state) { // CS开启时需检查并解决通道冲突
                int select = vCs.getSelectIndex(); // 获取CS当前通道索引
                if (vClk.getSelectIndex() == select) { // CLK与CS冲突
                    int index = vClk.getSelectIndex(); // 获取CLK当前索引
                    vClk.setSelectIndex(index == vClk.getSelectCount() - 1 ? 0 : index + 1); // CLK跳到下一个可用通道
                }
                if (vData.getSelectIndex() == select) { // DATA与CS冲突
                    int index = vData.getSelectIndex(); // 获取DATA当前索引
                    int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1; // DATA跳到下一个可用通道
                    if (index2 != vClk.getSelectIndex()) { // 新位置与CLK不冲突
                        vData.setSelectIndex(index2); // 设置DATA为新位置
                    } else { // 新位置仍与CLK冲突，再跳一步
                        int index3 = ((index2 == vData.getSelectCount() - 1) ? 0 : index2 + 1); // 再跳一步
                        vData.setSelectIndex(index3); // 设置DATA为新位置
                    }
                } else { // DATA与CS不冲突，但需检查DATA与CLK是否冲突
                    if (vData.getSelectIndex() == vClk.getSelectIndex()) { // DATA与CLK冲突
                        int index = vData.getSelectIndex(); // 获取DATA当前索引
                        int index2 = index == vData.getSelectCount() - 1 ? 0 : index + 1; // DATA跳到下一个可用通道
                        vData.setSelectIndex(index2); // 设置DATA为新位置
                    }
                }
                msgDetailsSpi.setClk(vClk.getSelectItem()); // 同步消息体CLK（冲突调整后可能变化）
                msgDetailsSpi.setData(vData.getSelectItem()); // 同步消息体DATA（冲突调整后可能变化）
                msgDetailsSpi.setCsSwitch(state); // 同步消息体CS使能
            }
            if (!isFromEventBus) { // 非EventBus触发时更新SpiBus
                spiBus.setCsValid(state); // 设置SpiBus的CS使能状态
            }
            Command.get().getBus_spi().setCsEnable(serialsNumber - 1, state , false); // 下发CS使能SCPI
            Command.get().getBus_spi().setClock(serialsNumber - 1, vClk.getSelectIndex(), false); // 下发CLK通道SCPI（冲突调整后）
            Command.get().getBus_spi().setData(serialsNumber-1,vData.getSelectIndex(),false); // 下发DATA通道SCPI（冲突调整后）
            Command.get().getBus_spi().setCs(serialsNumber-1,vCs.getSelectIndex(),false); // 下发CS通道SCPI

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber, String.valueOf(state)); // 缓存CS使能状态
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + serialsNumber, String.valueOf(vClk.getSelectIndex())); // 缓存CLK索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + serialsNumber, String.valueOf(vData.getSelectIndex())); // 缓存DATA索引
            sendMsg(isFromEventBus); // 通知父布局

            SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
        }
    }

    /**
     * 获取当前串口通道编号
     *
     * @return 串口编号(1~4)
     */
    public int getSerialsNumber() {
        return serialsNumber; // 返回串口编号
    }
}
