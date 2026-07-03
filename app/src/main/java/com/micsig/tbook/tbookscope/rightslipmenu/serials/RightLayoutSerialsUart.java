package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.UartBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBaudRate;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                       UART串口协议配置布局                                 |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— UART协议参数配置的UI布局          |
 * | 核心职责: 提供UART总线的接收通道、空闲电平、校验位、数据位、显示格式、    |
 * |          标准波特率及自定义波特率配置界面，处理用户交互和外部事件          |
 * | 架构设计: LinearLayout子类，包含rx/idle/check/bits/display/baudRate/     |
 * |          userDefine控件，采用观察者模式监听RxBus和EventBus事件            |
 * | 数据流向: 用户UI → RightLayoutSerialsUart → Command(SCPI) → 硬件         |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsUart, UartBus, Command,        |
 * |          ChannelFactory, CacheUtil, RxBus, TopDialogNumberKeyBoard      |
 * | 使用场景: 用户在右侧菜单选择UART协议后，此布局显示UART参数配置           |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * UART串口协议配置布局
 * <p>
 * 提供UART总线的接收通道(Rx)、空闲电平、校验位、数据位数、
 * 显示格式(HEX/BIN/ASC)、标准/自定义波特率等参数的配置界面。
 * 支持进制转换触发条件数据同步更新。
 * </p>
 */
public class RightLayoutSerialsUart extends LinearLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 接收通道/空闲电平/校验位/数据位/显示格式/波特率选择控件 */
    private RightViewSelect vRx, vIdle, vCheck, vBits, vDisplay, vBaudRate; // UART配置选择控件
    /** 自定义波特率编辑控件 */
    private RightViewUserDefineEdit tvUserDefine; // 自定义波特率编辑控件
    /** 波特率对话框 */
    private DialogBaudRate dialogBaudRate; // 波特率对话框
    /** 数字键盘对话框 */
    private TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框
    /** UART协议详情消息体 */
    private RightMsgSerialsUart msgDetailsUart; // UART协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** UART总线数据对象 */
    private UartBus uartBus; // UART总线数据对象

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsUart(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsUart(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsUart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_uart, this); // 加载UART布局
        setOrientation(VERTICAL); // 设置垂直排列
        vRx = (RightViewSelect) findViewById(R.id.rx); // 接收通道选择控件
        vIdle = (RightViewSelect) findViewById(R.id.idle); // 空闲电平选择控件
        vCheck = (RightViewSelect) findViewById(R.id.check); // 校验位选择控件
        vBits = (RightViewSelect) findViewById(R.id.bits); // 数据位选择控件
        vDisplay = (RightViewSelect) findViewById(R.id.display); // 显示格式选择控件
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate); // 波特率选择控件
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsUart); // 自定义波特率编辑控件

        vRx.setArray(GlobalVar.get().getChannelsName()); // 设置接收通道选项

        vRx.setOnItemClickListener(onItemClickListener); // 绑定接收通道点击监听
        vIdle.setOnItemClickListener(onItemClickListener); // 绑定空闲电平点击监听
        vCheck.setOnItemClickListener(onItemClickListener); // 绑定校验位点击监听
        vBits.setOnItemClickListener(onItemClickListener); // 绑定数据位点击监听
        vDisplay.setOnItemClickListener(onItemClickListener); // 绑定显示格式点击监听
        vBaudRate.setOnItemClickListener(onItemClickListener); // 绑定波特率点击监听
        tvUserDefine.setOnEditClickListener(onEditClickListener); // 绑定自定义波特率编辑监听

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsUart = new RightMsgSerialsUart(); // 创建UART详情消息对象
        msgDetailsUart.setRx(vRx.getSelectItem()); // 设置当前接收通道
        msgDetailsUart.setIdleLevel(vIdle.getSelectItem()); // 设置当前空闲电平
        msgDetailsUart.setCheck(vCheck.getSelectItem()); // 设置当前校验位
        msgDetailsUart.setBits(vBits.getSelectItem()); // 设置当前数据位
        msgDetailsUart.setDisplay(vDisplay.getSelectItem()); // 设置当前显示格式
        msgDetailsUart.setBaudRateDefine(tvUserDefine.getText().toString().trim()); // 设置自定义波特率
        msgDetailsUart.setBaudRate(vBaudRate.getSelectItem()); // 设置当前波特率
    }

    /**
     * 从缓存恢复UART配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    public void setCache() {
        int rx = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + serialsNumber); // 读取缓存接收通道索引
        int idle = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_IDLE + serialsNumber); // 读取缓存空闲电平索引
        int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + serialsNumber); // 读取缓存校验位索引
        int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + serialsNumber); // 读取缓存数据位索引
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber); // 读取缓存显示格式索引
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber); // 读取缓存波特率索引
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber); // 读取缓存自定义波特率

        vRx.setSelectIndex(rx); // 设置接收通道选中项
        vIdle.setSelectIndex(idle); // 设置空闲电平选中项
        vCheck.setSelectIndex(check); // 设置校验位选中项
        vBits.setSelectIndex(bits); // 设置数据位选中项
        vDisplay.setSelectIndex(display); // 设置显示格式选中项
        if (!StrUtil.isEmpty(userDefine)) { // 有自定义波特率
            tvUserDefine.setText(userDefine); // 显示自定义波特率文本
            vBaudRate.clearSelect(); // 清除标准波特率选中
        } else { // 无自定义波特率
            tvUserDefine.setText(""); // 清空自定义波特率文本
            vBaudRate.setSelectIndex(baudRate); // 设置标准波特率选中项
        }

        Command.get().getTrigger_uart().setSource(serialsNumber - 1, rx, false); // 下发触发源SCPI
        Command.get().getTrigger_uart().setIdle(serialsNumber - 1, idle, false); // 下发空闲电平SCPI
        Command.get().getTrigger_uart().setCheck(serialsNumber - 1, check, false); // 下发校验位SCPI
        Command.get().getTrigger_uart().setBits(serialsNumber - 1, TBookUtil.getIntFromBaudRate(String.valueOf(bits)), false); // 下发数据位SCPI
        Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, getBaudRateValueToScope(), false); // 下发触发波特率SCPI
        Command.get().getTrigger_uart().setDisplay(serialsNumber - 1, display, false); // 下发显示格式SCPI

        Command.get().getBus_uart().Rx(serialsNumber-1,rx,false); // 下发UART接收通道SCPI
        Command.get().getBus_uart().IdLevel(serialsNumber-1,idle,false); // 下发UART空闲电平SCPI
        Command.get().getBus_uart().Width(serialsNumber-1,bits,false); // 下发UART数据位宽SCPI
        if (!StrUtil.isEmpty(userDefine)) { // 有自定义波特率
            Command.get().getBus_uart().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(userDefine), false); // 下发自定义波特率SCPI
            Command.get().getBus_uart().BaudRate(serialsNumber - 1, -1, false); // 下发标准波特率-1表示使用自定义
        } else { // 无自定义波特率
            Command.get().getBus_uart().BaudRate(serialsNumber - 1, baudRate, false); // 下发标准波特率SCPI
            Command.get().getBus_uart().UserBaud(serialsNumber - 1, -1, false); // 下发自定义波特率-1表示不使用
        }

        Command.get().getBus_uart().Display(serialsNumber-1,display,false); // 下发UART显示格式SCPI

        uartBus.setRxChIdx(rx); // 同步UartBus接收通道索引
        uartBus.setIdleLevel(idle); // 同步UartBus空闲电平
        uartBus.setVerify(check == 2 ? UartBus.UART_EVEN_VERIFY : check); // 同步UartBus校验位（索引2=偶校验特殊映射）
        uartBus.setBits(Integer.parseInt(this.vBits.getSelectItem().getText().replace("bit", ""))); // 同步UartBus数据位数
        uartBus.setBaudRate(getBaudRateValueToScope()); // 同步UartBus波特率
        uartBus.setDisplayFormat(getDisplayToScope(display)); // 同步UartBus显示格式

//        int chNo = serialsNumber == RightMsgSerials.SERIALS_S1 ? IWave.S1 : IWave.S2;
        int chNo= TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
        SerialBusManage.getInstance().getSerialBus(chNo).setUartChecked(check == 0); // 设置串口总线UART校验使能
        SerialBusManage.getInstance().getSerialBus(chNo).setUartBits(bits + 5); // 设置串口总线UART数据位（索引+5=实际位数）
        SerialBusManage.getInstance().getSerialBus(chNo).setUartEncoding(display); // 设置串口总线UART编码格式

        msgDetailsUart.setRx(vRx.getSelectItem()); // 同步消息体接收通道
        msgDetailsUart.setIdleLevel(vIdle.getSelectItem()); // 同步消息体空闲电平
        msgDetailsUart.setCheck(vCheck.getSelectItem()); // 同步消息体校验位
        msgDetailsUart.setBits(vBits.getSelectItem()); // 同步消息体数据位
        msgDetailsUart.setDisplay(vDisplay.getSelectItem()); // 同步消息体显示格式
        msgDetailsUart.setBaudRate(vBaudRate.getSelectItem()); // 同步消息体波特率
        msgDetailsUart.setBaudRateDefine(tvUserDefine.getText()); // 同步消息体自定义波特率
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.UART) { // 当前串口类型为UART时
            sendMsg(false); // 发送消息通知父布局
        }
    }

    /** 初始化控件，订阅RxBus和EventBus事件 */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令回传事件
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam); // 注册EventBus总线参数观察者
    }

    /**
     * 获取波特率的整数值用于下发到硬件（自定义优先，否则取预设值）
     *
     * @return 波特率整数值
     */
    private int getBaudRateValueToScope() {
        if (StrUtil.isEmpty(tvUserDefine.getText())) { // 无自定义波特率
            return TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()); // 取预设波特率
        } else { // 有自定义波特率
            return TBookUtil.getIntFromBaudRate(tvUserDefine.getText().toString()); // 取自定义波特率
        }
    }

    /**
     * 将UI显示格式索引转换为内部显示格式常量
     *
     * @param displayIndex UI显示格式索引（0=HEX, 1=BIN, 2=ASC）
     * @return 内部显示格式常量
     */
    private int getDisplayToScope(int displayIndex) {
        if (displayIndex == 0) { // HEX
            return IBus.DISPLAY_HEX_DISPLAY; // 十六进制显示
        } else if (displayIndex == 1) { // BIN
            return IBus.DISPLAY_BIN_DISPLAY; // 二进制显示
        } else { // ASC
            return IBus.DISPLAY_ASC_DISPLAY; // ASCII显示
        }
    }

    /**
     * 将内部显示格式常量转换为UI显示格式索引
     *
     * @param displayIndex 内部显示格式常量
     * @return UI显示格式索引（0=HEX, 1=BIN, 2=ASC）
     */
    private int getDisplayFromScope(int displayIndex) {
        if (displayIndex == IBus.DISPLAY_HEX_DISPLAY) { // 十六进制
            return 0; // UI索引0
        } else if (displayIndex == IBus.DISPLAY_BIN_DISPLAY) { // 二进制
            return 1; // UI索引1
        } else { // ASCII
            return 2; // UI索引2
        }
    }

    /**
     * 设置串口通道编号，并获取对应的UartBus对象
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber; // 保存串口编号
        int tChan=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
        int fpgaCh=TChan.toFpgaChNo(tChan); // 转换为FPGA通道编号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道对象
        setControlColorByChIdx(tChan); // 根据通道设置控件颜色
        if (serialChannel == null) return; // 通道为空则返回
        uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 从通道获取UART总线对象
    }

    /**
     * 获取当前串口通道编号
     *
     * @return 串口编号(1~4)
     */
    public int getSerialsNumber() {
        return serialsNumber; // 返回串口编号
    }

    /**
     * 根据通道索引设置所有选择控件的主题颜色
     *
     * @param chIdx TChan通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vRx.setControlColorByChIdx(chIdx); // 设置接收通道控件颜色
        vIdle.setControlColorByChIdx(chIdx); // 设置空闲电平控件颜色
        vCheck.setControlColorByChIdx(chIdx); // 设置校验位控件颜色
        vBits.setControlColorByChIdx(chIdx); // 设置数据位控件颜色
        vDisplay.setControlColorByChIdx(chIdx); // 设置显示格式控件颜色
        vBaudRate.setControlColorByChIdx(chIdx); // 设置波特率控件颜色
    }

    /**
     * 获取UART协议详情消息体
     *
     * @return UART详情消息对象
     */
    public RightMsgSerialsUart getMsgDetailsUart() {
        return msgDetailsUart; // 返回UART详情消息体
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
     * 通过监听器发送消息通知父布局UART配置已变更
     *
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) { // 监听器非空时
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsUart.this, isFromEventBus); // 回调通知父布局
        }
    }

    /** RxBus缓存加载消费者，接收到MAIN_LOAD_CACHE事件时从缓存恢复UART配置 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道为空则跳过
            setCache(); // 从缓存恢复UART配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsUart, true); // 标记UART缓存已加载
        }
    };

    /** RxBus命令回传消费者，接收硬件返回的UART参数变更通知并同步到UI */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志位分发处理
                case CommandMsgToUI.FLAG_TRIGGERUART_SOURCE: { // 触发源变更
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            && Integer.parseInt(params[1]) != vRx.getSelectIndex()) { // 与当前选中项不同
                        vRx.setSelectIndex(Integer.parseInt(params[1])); // 更新接收通道选中项
                        onCheckChanged(vRx.getId(), vRx.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_IDLE: { // 空闲电平变更
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            && Integer.parseInt(params[1]) != vIdle.getSelectIndex()) { // 与当前选中项不同
                        vIdle.setSelectIndex(Integer.parseInt(params[1])); // 更新空闲电平选中项
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_CHECK: { // 校验位变更
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            && Integer.parseInt(params[1]) != vCheck.getSelectIndex()) { // 与当前选中项不同
                        vCheck.setSelectIndex(Integer.parseInt(params[1])); // 更新校验位选中项
                        onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_BITS: { // 数据位变更
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            && Integer.parseInt(params[1]) != vBits.getSelectIndex()) { // 与当前选中项不同
                        vBits.setSelectIndex(Integer.parseInt(params[1])); // 更新数据位选中项
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_BAUDRATE: { // 触发波特率变更
//                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
//                    String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1]));
//                    if (Integer.parseInt(params[0]) == serialsNumber - 1
//                            && !tvUserDefine.getText().toString().equals(baudRate)) {
//                        onTextChanged(tvUserDefine.getId(), baudRate, false);
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) { // 匹配当前串口编号
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1])); // 整数波特率转显示文本
                        if ((vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && !vBaudRate.getSelectItem().getText().equals(baudRate)) // 标准波特率不匹配
                                || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().toString().equals(baudRate))) { // 自定义波特率不匹配
                            if (vBaudRate.setSelectText(baudRate)) { // 能匹配到预设波特率
//                                tvUserDefine.setVisibility(GONE);
                                tvUserDefine.setText(""); // 清空自定义波特率
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发标准波特率变更
                            } else { // 无法匹配预设，使用自定义波特率
//                                tvUserDefine.setVisibility(VISIBLE);
                                vBaudRate.clearSelect(); // 清除标准波特率选中
                                tvUserDefine.setText(baudRate); // 显示自定义波特率文本
                                vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中最后一项（自定义项）
                                onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true); // 触发自定义波特率变更
                            }
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERUART_DISPLAY: { // 显示格式变更
                    String params[] = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配当前串口编号
                            && Integer.parseInt(params[1]) != vDisplay.getSelectIndex()) { // 与当前选中项不同
                        vDisplay.setSelectIndex(Integer.parseInt(params[1])); // 更新显示格式选中项
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_Bus_Uart_Rx:{ // UART接收通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int rx = Integer.parseInt(params[1]); // 获取接收通道索引
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        vRx.setSelectIndex(rx); // 更新接收通道选中项
                        onCheckChanged(vRx.getId(), vRx.getSelectItem(), false); // 触发变更处理
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Uart_IdLevel:{ // UART空闲电平变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int idle = Integer.parseInt(params[1]); // 获取空闲电平索引
                    if (sNum==serialsNumber-1) { // 匹配当前串口编号
                        vIdle.setSelectIndex(idle); // 更新空闲电平选中项
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false); // 触发变更处理
                    }
                }break;

                case CommandMsgToUI.FLAG_Bus_Uart_BaudRate: { // UART标准波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int rateIndex = Integer.parseInt(params[1]); // 获取波特率索引
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 不匹配则返回
                    vBaudRate.setSelectIndex(rateIndex); // 更新标准波特率选中项
                    tvUserDefine.setText(""); // 清空自定义波特率
                    onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), false); // 触发变更处理
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Uart_UserBaud: { // UART自定义波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    float rate = Integer.parseInt(params[1]) / 1000.0f; // 转换为kb/s单位
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 不匹配则返回
                    vBaudRate.clearSelect(); // 清除标准波特率选中
                    onTextChanged(tvUserDefine.getId(), rate + "kb/s", false); // 触发自定义波特率变更
                }
                break;

                case CommandMsgToUI.FLAG_Bus_Uart_Check:{ // UART校验位变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int check = Integer.parseInt(params[1]); // 获取校验位索引
                    if (sNum==serialsNumber-1) { // 匹配当前串口编号
                        vCheck.setSelectIndex(check); // 更新校验位选中项
                        onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), false); // 触发变更处理
                    }
                }break;

                case CommandMsgToUI.FLAG_Bus_Uart_Width:{ // UART数据位宽变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int width = Integer.parseInt(params[1]); // 获取位宽索引
                    if (sNum==serialsNumber-1) { // 匹配当前串口编号
                        vBits.setSelectIndex(width); // 更新数据位选中项
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), false); // 触发变更处理
                    }
                }break;
                case CommandMsgToUI.FLAG_Bus_Uart_Display:{ // UART显示格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int display = Integer.parseInt(params[1]); // 获取显示格式索引
                    if (sNum==serialsNumber-1) { // 匹配当前串口编号
                        vDisplay.setSelectIndex(display); // 更新显示格式选中项
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false); // 触发变更处理
                    }
                }break;

            }
        }
    };

    /** EventBus观察者，监听UartBus参数变更事件，从UartBus对象同步参数到UI控件 */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 转换事件数据类型
            if (uartBus == null) return; // UartBus未初始化则跳过
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 匹配总线参数事件ID
                    && uartBus.equals(eventBase.getData())) { // 匹配当前UartBus对象
                if (uartBus.getRxChIdx() != vRx.getSelectItem().getIndex()) { // 接收通道不一致
                    vRx.setSelectIndex(uartBus.getRxChIdx()); // 同步接收通道选中项
                    onCheckChanged(vRx.getId(), vRx.getSelectItem(), true); // 触发变更（来自EventBus）
                }
                if (uartBus.getIdleLevel() != vIdle.getSelectItem().getIndex()) { // 空闲电平不一致
                    vIdle.setSelectIndex(uartBus.getIdleLevel()); // 同步空闲电平选中项
                    onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), true); // 触发变更（来自EventBus）
                }
                int verify = uartBus.getVerify(); // 获取校验位
                verify = verify == UartBus.UART_EVEN_VERIFY ? 2 : verify; // 偶校验特殊映射到索引2
                if (verify != vCheck.getSelectItem().getIndex()) { // 校验位不一致
                    vCheck.setSelectIndex(verify); // 同步校验位选中项
                    onCheckChanged(vCheck.getId(), vCheck.getSelectItem(), true); // 触发变更（来自EventBus）
                }
                String bit = String.valueOf(uartBus.getBits()) + "bit"; // 构造位数显示文本（如"8bit"）
                if (!vBits.getSelectItem().getText().equals(bit)) { // 位数不一致
                    if (vBits.setSelectText(bit)) { // 按文本匹配设置选中项
                        onCheckChanged(vBits.getId(), vBits.getSelectItem(), true); // 触发变更（来自EventBus）
                    }
                }
                String baudRate = TBookUtil.getBaudRateFromInt(uartBus.getBaudRate()); // 整数波特率转显示文本
                if (!vBaudRate.getSelectItem().getText().equals(baudRate) || !tvUserDefine.getText().toString().equals(baudRate)) { // 波特率不一致
                    if (vBaudRate.setSelectText(baudRate)) { // 能匹配到预设波特率
                        tvUserDefine.setText(""); // 清空自定义波特率
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发变更（来自EventBus）
                    } else { // 无法匹配预设，使用自定义波特率
                        vBaudRate.clearSelect(); // 清除标准波特率选中
                        tvUserDefine.setText(baudRate); // 显示自定义波特率文本
                        vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中最后一项（自定义项）
                        onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true); // 触发变更（来自EventBus）
                    }
                }
                int display = getDisplayFromScope(uartBus.getDisplayFormat()); // 内部显示格式转UI索引
                if (vDisplay.getSelectItem().getIndex() != display) { // 显示格式不一致
                    vDisplay.setSelectIndex(display); // 同步显示格式选中项
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true); // 触发变更（来自EventBus）
                }
            }
        }
    };


    /** 自定义波特率编辑点击监听器，弹出数字键盘输入自定义波特率值 */
    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton(); // 播放按键音效
            String s = tvUserDefine.getText(); // 获取当前文本
            if (dialogKeyBoard == null) // 数字键盘未初始化
                dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
            if (!StrUtil.isEmpty(s)) { // 有已有文本
                String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, ""); // 去除单位
                double preDouble = Double.parseDouble(number); // 解析数值部分
                String preBs = s.replace(number, ""); // 提取单位部分
                dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onDismissListener); // 设置键盘初始数据，最小1200b/s，最大8Mb/s
            } else { // 无已有文本
                dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS, 1200, 8 * 1000 * 1000, onDismissListener); // 默认从0kb/s开始
            }
        }
    };

    /** 数字键盘关闭监听器，键盘关闭时触发自定义波特率变更 */
    private TopDialogNumberKeyBoard.OnDismissListener onDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvUserDefine.getId(), result, false); // 触发自定义波特率变更
        }
    };

    /**
     * 调试方法，打印UART串口总线参数信息
     */
    private void debugSerial(){
        int tChan=TChan.S1; // 使用S1通道调试
        int fpgaChan=TChan.toFpgaChNo(tChan); // 转换为FPGA通道编号
        SerialChannel sNum= ChannelFactory.getSerialChannel(fpgaChan); // 获取串口通道
        if (sNum == null) return; // 通道为空则返回
        boolean isOpen=sNum.isOpen(); // 获取通道开启状态
        int busType= sNum.getBusType(); // 获取总线类型
        switch (busType){ // 根据总线类型分发
            case IBus.UART:{ // UART类型
                UartBus bus= (UartBus) sNum.getBus(busType); // 获取UartBus对象
                int ch= bus.getRxChIdx(); // 获取接收通道索引
                int level=bus.getIdleLevel(); // 获取空闲电平
                int check=bus.getVerify(); // 获取校验位
                int bits=bus.getBits(); // 获取数据位数
                int baudrate=bus.getBaudRate(); // 获取波特率
                int display=bus.getDisplayFormat(); // 获取显示格式
                int triggerType= bus.getTriggerType(); // 获取触发类型
                Channel chan= ChannelFactory.getDynamicChannel(ch); // 获取动态通道
                if (chan==null) return; // 通道为空则返回
                double discreetS1 = chan.getBusPrimaryLevel(); // 获取主电平
                double discreetS2 = chan.getBusSecondaryLevel(); // 获取副电平
//                Log.d(Tag.Debug,
//                        String.format("RightLayoutSerials.debugSerial: isOpen:%b ,type:%d, rx:%d,level:%d,check:%d,bits:%d,baudrate:%d,display:%d,triggerType:%d, level1:%f,level2:%f ",
//                                isOpen,busType,ch,level,check,bits,baudrate,display,triggerType,discreetS1,discreetS2));
            }break;
        }
        int trigType= TriggerFactory.getTriggerType(); // 获取触发类型
        //Log.d(Tag.Debug, String.format("RightLayoutSerialsUart.debugSerial: %d",trigType ));


    }

    /** 选择控件点击监听器，处理UART各选项的点击事件 */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            onCheckChanged(viewId, item, false); // 处理选项变更（来自UI操作）
            debugSerial(); // 调试打印
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 刷新UI后回调（UART未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 刷新UI前回调（UART未使用）
        }
    };

    /**
     * UART选项变更核心处理方法，处理接收通道/空闲电平/校验位/数据位/显示格式/波特率的选择变更，
     * 同步UartBus对象、下发SCPI命令、更新缓存和消息体
     *
     * @param viewId        触发变更的控件ID
     * @param item          选中的选项项
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vRx.getId()) { // 接收通道变更
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                uartBus.setRxChIdx(item.getIndex()); // 设置UartBus接收通道索引
            }
            Command.get().getTrigger_uart().setSource(serialsNumber - 1, item.getIndex(), false); // 下发触发源SCPI
            Command.get().getBus_uart().Rx(serialsNumber-1,item.getIndex(),false); // 下发UART接收通道SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + serialsNumber, String.valueOf(item.getIndex())); // 缓存接收通道索引
            msgDetailsUart.setRx(vRx.getSelectItem()); // 更新消息体接收通道
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vIdle.getId()) { // 空闲电平变更
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                uartBus.setIdleLevel(item.getIndex()); // 设置UartBus空闲电平
            }
            Command.get().getTrigger_uart().setIdle(serialsNumber - 1, item.getIndex(), false); // 下发空闲电平SCPI
            Command.get().getBus_uart().IdLevel(serialsNumber-1,item.getIndex(),false); // 下发UART空闲电平SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_IDLE + serialsNumber, String.valueOf(item.getIndex())); // 缓存空闲电平索引
            msgDetailsUart.setIdleLevel(vIdle.getSelectItem()); // 更新消息体空闲电平
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vCheck.getId()) { // 校验位变更
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                if (item.getIndex() == 2) { // 偶校验（索引2特殊映射）
                    uartBus.setVerify(UartBus.UART_EVEN_VERIFY); // 设置偶校验
                } else { // 其他校验位（无校验/奇校验）
                    uartBus.setVerify(item.getIndex()); // 设置校验位值
                }
            }
            Command.get().getTrigger_uart().setCheck(serialsNumber - 1, item.getIndex(), false); // 下发校验位SCPI
            Command.get().getBus_uart().Check(serialsNumber-1,item.getIndex(),false); // 下发UART校验位SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + serialsNumber, String.valueOf(item.getIndex())); // 缓存校验位索引
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartChecked(item.getIndex() == 0);
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartChecked(item.getIndex() == 0);
//            }
            int ch=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
            SerialBusManage.getInstance().getSerialBus(ch).setUartChecked(item.getIndex() == 0); // 设置UART校验使能

            //SerialBusStructParse.get().uartSettingStruct.setChecked( item.getIndex()==0);
            msgDetailsUart.setCheck(vCheck.getSelectItem()); // 更新消息体校验位
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vBits.getId()) { // 数据位变更
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                uartBus.setBits(Integer.parseInt(item.getText().replace("bit", ""))); // 解析位数并设置
            }
            Command.get().getTrigger_uart().setBits(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false); // 下发数据位SCPI
            Command.get().getBus_uart().Width(serialsNumber-1,item.getIndex(),false); // 下发UART数据位宽SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + serialsNumber, String.valueOf(item.getIndex())); // 缓存数据位索引
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartBits(item.getIndex() + 5);
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartBits(item.getIndex() + 5);
//            }
            int ch=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
            SerialBusManage.getInstance().getSerialBus(ch).setUartBits(item.getIndex() + 5); // 设置UART数据位数（索引+5）
            //SerialBusStructParse.get().uartSettingStruct.setUartLength(item.getIndex()+5);
            msgDetailsUart.setBits(vBits.getSelectItem()); // 更新消息体数据位
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vDisplay.getId()) { // 显示格式变更
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                uartBus.setDisplayFormat(getDisplayToScope(item.getIndex())); // 转换并设置显示格式
            }
            Command.get().getTrigger_uart().setDisplay(serialsNumber - 1, item.getIndex(), false); // 下发显示格式SCPI
            Command.get().getBus_uart().Display(serialsNumber-1,item.getIndex(),false); // 下发UART显示格式SCPI

            int preDigits; // 原始进制位数
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber) == 1) { // 之前是二进制
                preDigits = IDigits.DIGITS_2; // 原始2位
            } else { // 其他情况（HEX或ASC）
                preDigits = IDigits.DIGITS_16; // 原始16位
            }
            setTopSerialsUartData(preDigits, item.getIndex() == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16); // 同步进制转换触发条件数据
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber, String.valueOf(item.getIndex())); // 缓存显示格式索引
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).setUartEncoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).setUartEncoding(item.getIndex());
//            }
            int ch=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道编号
            SerialBusManage.getInstance().getSerialBus(ch).setUartEncoding(item.getIndex()); // 设置UART编码格式
            //SerialBusStructParse.get().uartSettingStruct.setEncoding(item.getIndex());
            msgDetailsUart.setDisplay(vDisplay.getSelectItem()); // 更新消息体显示格式
            sendMsg(isFromEventBus); // 通知父布局
        }  else if (viewId == vBaudRate.getId()) { // 标准波特率变更
            Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false); // 下发触发波特率SCPI
            Command.get().getBus_uart().BaudRate(serialsNumber-1,item.getIndex(),false); // 下发UART标准波特率SCPI
            Command.get().getBus_uart().UserBaud(serialsNumber-1,-1,false); // 下发自定义波特率-1表示不使用
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber, String.valueOf(item.getIndex())); // 缓存标准波特率索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber, ""); // 清空自定义波特率缓存
            tvUserDefine.setText(""); // 清空自定义波特率文本
            if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                int baudRate = getBaudRateValueToScope(); // 获取波特率整数值
                uartBus.setBaudRate(baudRate); // 设置UartBus标准波特率
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
            }
            msgDetailsUart.setBaudRate(item); // 更新消息体标准波特率
            sendMsg(isFromEventBus); // 通知父布局
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_UART);
//            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) {
//                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_UART);
//            }
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
    }

    /**
     * 设置UART进制转换触发条件数据，根据显示格式转换4组触发数据的进制
     *
     * @param preDigits  原始进制位数（2或16）
     * @param nextDigits 新的进制位数（2或16）
     */
    private void setTopSerialsUartData(int preDigits, int nextDigits) {
        int bits = Integer.parseInt(vBits.getSelectItem().getText().replace("bit", "")); // 获取当前数据位数
        String key; // 缓存key
        for (int i = 0; i < 4; i++) { // 遍历4组触发数据（DATA/0DATA/1DATA/XDATA）
            if (i == 0) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + serialsNumber; // DATA缓存key
            } else if (i == 1) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + serialsNumber; // 0DATA缓存key
            } else if (i == 2) {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + serialsNumber; // 1DATA缓存key
            } else {
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + serialsNumber; // XDATA缓存key
            }
            String edit = CacheUtil.get().getString(key); // 读取当前触发数据
            edit = SerialsUtils.HexBin(edit, preDigits, nextDigits); // 进制转换（HEX↔BIN）
            edit = SerialsUtils.reCalcSpace(edit, nextDigits == IDigits.DIGITS_16 ? 2 : bits, nextDigits); // 重算空格位置
            CacheUtil.get().putMap(key, edit); // 写回缓存
        }
    }

    /**
     * 自定义波特率文本变更处理方法，处理自定义波特率的输入变更，
     * 包含波特率显示格式截断逻辑（数值截4位，含小数点截5位）
     *
     * @param viewId        触发变更的控件ID
     * @param result        输入结果文本
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) { // 自定义波特率变更
//            tvUserDefine.setText(result);
//            if (!isFromEventBus) {
//                int baudRate = TBookUtil.getIntFromBaudRate(result);
//                uartBus.setBaudRate(baudRate);
//                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate));
//            }
//            Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false);
//            Command.get().getBus_uart().BaudRate(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false);
//            Command.get().getBus_uart().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false);
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_BAUDRATE + serialsNumber, result);
//            msgDetailsUart.setBaudRate(result);
//            sendMsg(isFromEventBus);
//            SerialBusManage.getInstance().clearSerialBusTxtBuffer();

            String unit = ""; // 单位变量初始化
            if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_MBS)) { // Mb/s单位
                unit = TopDialogNumberKeyBoard.KEYBOARD_MBS; // 保存单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, ""); // 去除单位
            } else if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_KBS)) { // kb/s单位
                unit = TopDialogNumberKeyBoard.KEYBOARD_KBS; // 保存单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, ""); // 去除单位
            } else { // b/s单位
                unit = TopDialogNumberKeyBoard.KEYBOARD_BS; // 保存单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_BS, ""); // 去除单位
            }

            if (result.length() > 4 && result.substring(0, 4).contains(".")) { // 含小数点且超过4位
                result = result.substring(0, 5); // 截5位（含小数点）
            } else if (result.length() > 4) { // 不含小数点超过4位
                result = result.substring(0, 4); // 截4位
            }
            result = result + unit; // 拼回单位
            vBaudRate.clearSelect(); // 清除标准波特率选中
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) { // 与当前值不同
                if (!isFromEventBus) { // 非EventBus触发时更新UartBus
                    int baudRate = TBookUtil.getIntFromBaudRate(result); // 解析波特率整数值
                    uartBus.setBaudRate(baudRate); // 设置UartBus标准波特率
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
                }
                Command.get().getTrigger_uart().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发触发波特率SCPI
                Command.get().getBus_uart().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发自定义波特率SCPI
                Command.get().getBus_uart().BaudRate(serialsNumber - 1, -1, false); // 下发标准波特率-1表示使用自定义
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_UART_USERDEFINE + serialsNumber, result); // 缓存自定义波特率
                tvUserDefine.setText(result); // 显示自定义波特率文本
                msgDetailsUart.setBaudRateDefine(result); // 更新消息体自定义波特率
                sendMsg(isFromEventBus); // 通知父布局
                SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
            }
        }
    }
}
