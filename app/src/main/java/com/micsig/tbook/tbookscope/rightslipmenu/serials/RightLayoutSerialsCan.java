package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.smart.Property;
import com.micsig.tbook.scope.Bus.CanBus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.ExKeysMsgRightCanPercent;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                      CAN/CAN-FD串口协议配置布局                            |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— CAN/CAN-FD协议参数配置的UI布局    |
 * | 核心职责: 提供CAN总线的信号源、信号类型、波特率、FD波特率、采样点及       |
 * |          ISO模式配置界面，处理用户交互和外部事件                          |
 * | 架构设计: RelativeLayout子类，包含source/signal/baudRate/FDBaudRate/     |
 * |          userDefine/FDUserDefine/percent/ISO等控件，                     |
 * |          采用观察者模式监听RxBus和EventBus事件                           |
 * | 数据流向: 用户UI → RightLayoutSerialsCan → Command(SCPI) → 硬件         |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsCan, CanBus, Command,          |
 * |          ChannelFactory, CacheUtil, RxBus, TopDialogNumberKeyBoard      |
 * | 使用场景: 用户在右侧菜单选择CAN协议后，此布局显示CAN/CAN-FD参数配置      |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * CAN/CAN-FD串口协议配置布局
 * <p>
 * 提供CAN总线的信号源通道、信号类型、标准/FD波特率、自定义波特率、
 * 采样点和ISO模式等参数的配置界面。支持CAN-FD功能根据设备配置启用/禁用。
 * </p>
 */
public class RightLayoutSerialsCan extends RelativeLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 信号源/信号类型/标准波特率/FD波特率选择控件 */
    private RightViewSelect vSource, vSignal, vBaudRate, vFDBaudRate; // CAN配置选择控件
    /** 自定义波特率/FD自定义波特率编辑控件 */
    private RightViewUserDefineEdit tvUserDefine, tvFDUserDefine; // 自定义波特率编辑控件
    /** 数字键盘对话框 */
    private TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框
    /** 采样点/FD采样点百分比显示 */
    private TextView tvBaudRatePercent, tvFdPercent; // 采样点百分比显示
    /** ISO模式选择控件 */
    private RightViewSelect vISO; // ISO模式选择控件

    /** CAN协议详情消息体 */
    private RightMsgSerialsCan msgDetailsCan; // CAN协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** CAN总线数据对象 */
    private CanBus canBus; // CAN总线数据对象

    /** 根视图 */
    private ViewGroup rootView; // 根视图

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsCan(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsCan(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsCan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_serials_can, this); // 加载CAN布局
        vSource = (RightViewSelect) findViewById(R.id.source); // 信号源选择控件
        vSignal = (RightViewSelect) findViewById(R.id.signal); // 信号类型选择控件
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate); // 标准波特率选择控件
        vFDBaudRate = (RightViewSelect) findViewById(R.id.fdBaudRate); // FD波特率选择控件
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsCan); // 自定义波特率编辑控件
        tvFDUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsCanFD); // FD自定义波特率编辑控件
        tvBaudRatePercent = (TextView) findViewById(R.id.baudRatePercent); // 采样点百分比显示
        tvFdPercent = (TextView) findViewById(R.id.fdPercent); // FD采样点百分比显示
        vISO = (RightViewSelect) findViewById(R.id.iSO); // ISO模式选择控件

        vSource.setArray(GlobalVar.get().getChannelsName()); // 设置信号源通道选项

        vSource.setOnItemClickListener(onItemClickListener); // 绑定信号源点击监听
        vSignal.setOnItemClickListener(onItemClickListener); // 绑定信号类型点击监听
        vBaudRate.setOnItemClickListener(onItemClickListener); // 绑定标准波特率点击监听
        vFDBaudRate.setOnItemClickListener(onItemClickListener); // 绑定FD波特率点击监听
        tvUserDefine.setOnEditClickListener(onEditClickListener); // 绑定自定义波特率编辑监听
        tvFDUserDefine.setOnEditClickListener(onEditClickListener); // 绑定FD自定义波特率编辑监听
        tvBaudRatePercent.setOnClickListener(onClickListener); // 绑定采样点点击监听
        tvFdPercent.setOnClickListener(onClickListener); // 绑定FD采样点点击监听
        vISO.setOnItemClickListener(onItemClickListener); // 绑定ISO模式点击监听

//        tvUserDefine.setVisibility(GONE);

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsCan = new RightMsgSerialsCan(); // 创建CAN详情消息对象
        msgDetailsCan.setSource(vSource.getSelectItem()); // 设置当前信号源
        msgDetailsCan.setSignal(vSignal.getSelectItem()); // 设置当前信号类型
        msgDetailsCan.setBaudRate(vBaudRate.getSelectItem()); // 设置当前波特率
        msgDetailsCan.setFDBaudRate(vFDBaudRate.getSelectItem()); // 设置当前FD波特率
        msgDetailsCan.setBaudRateDefine(""); // 清空自定义波特率
        msgDetailsCan.setFDBaudRateDefine(""); // 清空FD自定义波特率
        msgDetailsCan.setISO(vISO.getSelectItem()); // 设置当前ISO模式
    }

    /**
     * 从缓存恢复CAN配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + serialsNumber); // 读取缓存信号源索引
        int signal = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serialsNumber); // 读取缓存信号类型索引
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_BAUDRATE + serialsNumber); // 读取缓存波特率索引
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber); // 读取缓存自定义波特率

        int fdBaudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + serialsNumber); // 读取缓存FD波特率索引
        String fdUserDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber); // 读取缓存FD自定义波特率
        String percent = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_PERCENT + serialsNumber); // 读取缓存采样点
        String fdPercent = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDPERCENT + serialsNumber); // 读取缓存FD采样点
        int fdISO = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_ISO + serialsNumber); // 读取缓存ISO模式索引

        vSource.setSelectIndex(source); // 设置信号源选中项
        vSignal.setSelectIndex(signal); // 设置信号类型选中项
        if (!StrUtil.isEmpty(userDefine)) { // 有自定义波特率
            tvUserDefine.setText(userDefine); // 显示自定义波特率文本
            vBaudRate.clearSelect(); // 清除标准波特率选中
        } else { // 无自定义波特率
            tvUserDefine.setText(""); // 清空自定义波特率文本
            vBaudRate.setSelectIndex(baudRate); // 设置标准波特率选中项
        }

        if (!StrUtil.isEmpty(fdUserDefine)) { // 有FD自定义波特率
            tvFDUserDefine.setText(fdUserDefine); // 显示FD自定义波特率文本
            vFDBaudRate.clearSelect(); // 清除FD标准波特率选中
        } else { // 无FD自定义波特率
            tvFDUserDefine.setText(""); // 清空FD自定义波特率文本
            vFDBaudRate.setSelectIndex(fdBaudRate); // 设置FD标准波特率选中项
        }
        tvBaudRatePercent.setText(percent + "%"); // 显示采样点百分比
        tvFdPercent.setText(fdPercent + "%"); // 显示FD采样点百分比
        vISO.setSelectIndex(fdISO); // 设置ISO模式选中项

        Command.get().getTrigger_can().setSource(serialsNumber - 1, source, false); // 下发触发源SCPI
        Command.get().getTrigger_can().setIdle(serialsNumber - 1, signal, false); // 下发空闲电平SCPI
        Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, getBaudRateValue(), false); // 下发触发波特率SCPI

        Command.get().getBus_can().Channel(serialsNumber - 1, source, false); // 下发CAN通道SCPI
        Command.get().getBus_can().Signal(serialsNumber - 1, signal, false); // 下发CAN信号SCPI
        Command.get().getBus_can().ISO(serialsNumber - 1, fdISO, false); // 下发ISO模式SCPI
        if (!StrUtil.isEmpty(userDefine)) { // 有自定义波特率
            vBaudRate.clearSelect(); // 清除标准波特率选中
            Command.get().getBus_can().BaudRate(serialsNumber - 1, -1, false); // 下发标准波特率-1表示使用自定义
            Command.get().getBus_can().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(userDefine), false); // 下发自定义波特率SCPI
        } else { // 无自定义波特率
            tvUserDefine.setText(""); // 清空自定义波特率文本
            Command.get().getBus_can().BaudRate(serialsNumber - 1, baudRate, false); // 下发标准波特率SCPI
            Command.get().getBus_can().UserBaud(serialsNumber - 1, -1, false); // 下发自定义波特率-1表示不使用
        }
        Command.get().getBus_can().SAMPlepoint(serialsNumber - 1, Double.parseDouble(percent), false); // 下发采样点SCPI
        Command.get().getBus_can().FDSAmplepoint(serialsNumber - 1, Double.parseDouble(fdPercent), false); // 下发FD采样点SCPI
        if (!StrUtil.isEmpty(fdUserDefine)) { // 有FD自定义波特率
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(fdUserDefine), false); // 下发FD自定义波特率SCPI
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false); // 下发FD自定义波特率-1（冗余调用）
        } else { // 无FD自定义波特率
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false); // 下发FD自定义波特率-1表示不使用
            Command.get().getBus_can().FDBAudrate(serialsNumber - 1, fdBaudRate, false); // 下发FD标准波特率SCPI
        }


        canBus.setSrcChIdx(source); // 同步CanBus信号源通道索引
        canBus.setSignal(signal); // 同步CanBus信号类型
        canBus.setBaudRate(getBaudRateValue()); // 同步CanBus标准波特率
        if (!(ScopeConfig.getConfig().isBusEnable(Property.BUS_CAN_FD) || App.IsDebug())) { // CAN-FD未启用或非调试模式
            canBus.setFDBandRate(0); // FD波特率设为0
            tvFDUserDefine.setText(""); // 清空FD自定义波特率
            vFDBaudRate.setSelectIndex(0); // FD波特率选中第一项
            vFDBaudRate.setEnabled(false); // 禁用FD波特率选择
            tvFdPercent.setEnabled(false); // 禁用FD采样点
            tvFDUserDefine.setEnabled(false); // 禁用FD自定义波特率编辑
            vISO.setEnabled(false); // 禁用ISO模式选择
        } else { // CAN-FD已启用
            vFDBaudRate.setEnabled(true); // 启用FD波特率选择
            tvFdPercent.setEnabled(true); // 启用FD采样点
            tvFDUserDefine.setEnabled(true); // 启用FD自定义波特率编辑
            vISO.setEnabled(true); // 启用ISO模式选择
            canBus.setFDBandRate(getFDBaudRateValue()); // 同步CanBus FD波特率
            canBus.setSamplePlace1(Double.parseDouble(percent) * 1.0 / 100); // 同步CanBus采样点1
            canBus.setSamplePlace2(Double.parseDouble(fdPercent) * 1.0 / 100); // 同步CanBus采样点2
            canBus.setISO(fdISO == 0); // 同步CanBus ISO模式（索引0=ISO开启）
        }

        msgDetailsCan.setSource(vSource.getSelectItem()); // 同步消息体信号源
        msgDetailsCan.setSignal(vSignal.getSelectItem()); // 同步消息体信号类型
        msgDetailsCan.setBaudRate(vBaudRate.getSelectItem()); // 同步消息体标准波特率
        msgDetailsCan.setBaudRateDefine(tvUserDefine.getText()); // 同步消息体自定义波特率
        msgDetailsCan.setFDBaudRate(vFDBaudRate.getSelectItem()); // 同步消息体FD波特率
        msgDetailsCan.setFDBaudRateDefine(tvFDUserDefine.getText()); // 同步消息体FD自定义波特率
        msgDetailsCan.setISO(vISO.getSelectItem()); // 同步消息体ISO模式
//        if (baudRate == vBaudRate.getSelectCount() - 1) {
//            this.tvUserDefine.setVisibility(VISIBLE);
//            vBaudRate.clearSelect();
//            RightBeanSelect bean = msgDetailsCan.getBaudRate();
//            msgDetailsCan.setBaudRate(new RightBeanSelect(bean.getIndex(), userDefine, bean.isCheck()));
//        } else {
//            this.tvUserDefine.setVisibility(GONE);
//        }
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.CAN) { // 当前串口类型为CAN时
            sendMsg(false); // 发送消息通知父布局
        }
    }

    /** 初始化控件，订阅RxBus和EventBus事件 */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令回传事件
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_RIGHTCAN_PERCENT).subscribe(consumerExKeysRightCanPercent); // 订阅外键采样点调整事件
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam); // 注册EventBus总线参数观察者
    }

    /**
     * 获取标准波特率的整数值（自定义优先，否则取预设值）
     *
     * @return 波特率整数值
     */
    private int getBaudRateValue() {
        if (StrUtil.isEmpty(tvUserDefine.getText())) { // 无自定义波特率
            return TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()); // 取预设波特率
        } else { // 有自定义波特率
            return TBookUtil.getIntFromBaudRate(tvUserDefine.getText().toString()); // 取自定义波特率
        }
    }

    /**
     * 获取FD波特率的整数值（自定义优先，否则取预设值）
     *
     * @return FD波特率整数值
     */
    private int getFDBaudRateValue() {
        if (StrUtil.isEmpty(tvFDUserDefine.getText())) { // 无FD自定义波特率
            return TBookUtil.getIntFromBaudRate(vFDBaudRate.getSelectItem().getText()); // 取预设FD波特率
        } else { // 有FD自定义波特率
            return TBookUtil.getIntFromBaudRate(tvFDUserDefine.getText().toString()); // 取自定义FD波特率
        }
    }

    /**
     * 设置串口通道编号，并获取对应的CanBus对象
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
        canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 从通道获取CAN总线对象

    }

    /**
     * 根据通道索引设置所有选择控件的主题颜色
     *
     * @param chIdx TChan通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx); // 设置信号源控件颜色
        vSignal.setControlColorByChIdx(chIdx); // 设置信号类型控件颜色
        vBaudRate.setControlColorByChIdx(chIdx); // 设置标准波特率控件颜色
        vFDBaudRate.setControlColorByChIdx(chIdx); // 设置FD波特率控件颜色
        vISO.setControlColorByChIdx(chIdx); // 设置ISO模式控件颜色
    }

    /**
     * 获取CAN协议详情消息体
     *
     * @return CAN详情消息对象
     */
    public RightMsgSerialsCan getMsgDetailsCan() {
        return msgDetailsCan; // 返回CAN详情消息体
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
     * 通过监听器发送消息通知父布局CAN配置已变更
     *
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) { // 监听器非空时
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsCan.this, isFromEventBus); // 回调通知父布局
        }
    }

    /** RxBus缓存加载消费者，接收到MAIN_LOAD_CACHE事件时从缓存恢复CAN配置 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道为空则跳过
            setCache(); // 从缓存恢复CAN配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_MainHolderLeftMenu, true); // 标记左侧菜单缓存已加载
        }
    };

    /** RxBus命令回传消费者，接收硬件返回的CAN参数变更通知并同步到UI */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志位分发处理
                case CommandMsgToUI.FLAG_TRIGGERCAN_SOURCE: { // 触发源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (serialsNumber - 1 == Integer.parseInt(params[0]) // 匹配当前串口编号
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) { // 与当前选中项不同
                        vSource.setSelectIndex(Integer.parseInt(params[1])); // 更新信号源选中项
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_IDLE: { // 空闲电平变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (serialsNumber - 1 == Integer.parseInt(params[0]) // 匹配当前串口编号
                            && vSignal.getSelectIndex() != Integer.parseInt(params[1])) { // 与当前选中项不同
                        vSignal.setSelectIndex(Integer.parseInt(params[1])); // 更新信号类型选中项
                        onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERCAN_BAUDRATE: { // 触发波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (serialsNumber - 1 == Integer.parseInt(params[0])) { // 匹配当前串口编号
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1])); // 整数波特率转显示文本
                        if (vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && (!vBaudRate.getSelectItem().getText().equals(baudRate)) // 标准波特率不匹配
                                || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().equals(baudRate))) { // 自定义波特率不匹配
                            if (vBaudRate.setSelectText(baudRate)) { // 能匹配到预设波特率
//                                tvUserDefine.setVisibility(GONE);
                                tvUserDefine.setText(""); // 清空自定义波特率
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发标准波特率变更
                            } else { // 无法匹配预设，使用自定义波特率
//                                tvUserDefine.setVisibility(VISIBLE);
                                vBaudRate.clearSelect(); // 清除标准波特率选中
                                tvUserDefine.setText(baudRate); // 显示自定义波特率文本
                                vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中最后一项（自定义项）
                                onTextChanged(tvUserDefine.getId(), tvUserDefine.getText(), true); // 触发自定义波特率变更
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_Can_Channel: { // CAN通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int ch = Integer.parseInt(params[1]); // 获取通道索引
                    if ((serialsNumber - 1) == sNum) { // 匹配当前串口编号
                        vSource.setSelectIndex(ch); // 更新信号源选中项
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false); // 触发变更处理
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_Signal: { // CAN信号变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int signal = Integer.parseInt(params[1]); // 获取信号索引
                    Logger.i(Command.TAG, "signal:" + signal); // 日志输出信号值
                    if (serialsNumber - 1 == sNum) { // 匹配当前串口编号
                        vSignal.setSelectIndex(signal); // 更新信号类型选中项
                        onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), false); // 触发变更处理
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_BaudRate: { // CAN标准波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int baudrate = Integer.parseInt(params[1]); // 获取波特率索引
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        tvUserDefine.setText(""); // 清空自定义波特率
                        vBaudRate.setSelectIndex(baudrate); // 更新标准波特率选中项
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), false); // 触发变更处理
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_UserBaud: { // CAN自定义波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    float userbaud = Integer.parseInt(params[1]) / 1000.0f; // 转换为kb/s单位
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        vBaudRate.clearSelect(); // 清除标准波特率选中
                        onTextChanged(tvUserDefine.getId(), userbaud + "kb/s", false); // 触发自定义波特率变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_SamplePoint: { // CAN采样点变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    String percent = (params[1]); // 获取采样点值
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        onTextChanged(tvBaudRatePercent.getId(), percent, false); // 触发采样点变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDBaudrate: { // CAN FD标准波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int index = Integer.parseInt(params[1]); // 获取FD波特率索引
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        tvFDUserDefine.setText(""); // 清空FD自定义波特率
                        vFDBaudRate.setSelectIndex(index); // 更新FD标准波特率选中项
                        onItemClickListener.onItemClick(vFDBaudRate.getId(), vFDBaudRate.getSelectItem()); // 触发FD波特率变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDUserBaud: { // CAN FD自定义波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    String unit = "kb/s"; // 默认单位kb/s
                    float baudrate = Integer.parseInt(params[1]) / 1000.0f; // 转换为kb/s单位
                    if (baudrate > 1000) { // 超过1000kb/s使用Mb/s单位
                        baudrate = baudrate / 1000.0f; // 转换为Mb/s
                        unit = "Mb/s"; // 单位设为Mb/s
                    }
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        vFDBaudRate.clearSelect(); // 清除FD标准波特率选中
                        onTextChanged(tvFDUserDefine.getId(), baudrate + unit, false); // 触发FD自定义波特率变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_FDSamplePoint: { // CAN FD采样点变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    String percent = (params[1]); // 获取FD采样点值
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        onTextChanged(tvFdPercent.getId(), percent, false); // 触发FD采样点变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Can_ISO: { // CAN ISO模式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum = Integer.parseInt(params[0]); // 获取串口编号
                    int iso = Integer.parseInt(params[1]); // 获取ISO模式索引
                    if (sNum == serialsNumber - 1) { // 匹配当前串口编号
                        vISO.setSelectIndex(iso); // 更新ISO模式选中项
                        onCheckChanged(vISO.getId(), vISO.getSelectItem(), true); // 触发ISO变更（来自EventBus）
                    }
                }
                break;
            }
        }
    };

    /** RxBus外键采样点调整消费者，接收旋钮/外键事件调整CAN/FD采样点百分比 */
    private Consumer<ExKeysMsgRightCanPercent> consumerExKeysRightCanPercent = new Consumer<ExKeysMsgRightCanPercent>() {
        @Override
        public void accept(ExKeysMsgRightCanPercent msgPercent) throws Exception {
            if (msgPercent.isS1() == (serialsNumber == RightMsgSerials.SERIALS_S1)) { // 匹配当前串口编号（S1或S2/S3/S4）
                TextView textView = msgPercent.isTop() ? tvBaudRatePercent : tvFdPercent; // 判断是标准还是FD采样点
                double percent = Double.parseDouble(textView.getText().toString().replace("%", "")); // 获取当前采样点值
                percent = msgPercent.isAdd() ? percent + msgPercent.getCount() : percent - msgPercent.getCount(); // 根据增减方向计算新值
                percent = Math.max(percent, 0); // 限制最小值0
                percent = Math.min(percent, 99); // 限制最大值99
                onTextChanged(textView.getId(), String.valueOf(percent), false); // 触发采样点变更
            }
        }
    };

    /** EventBus观察者，监听CanBus参数变更事件，从CanBus对象同步参数到UI控件 */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 转换事件数据类型
            if (canBus == null) return; // CanBus未初始化则跳过
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 匹配总线参数事件ID
                    && canBus.equals(eventBase.getData())) { // 匹配当前CanBus对象

                if (canBus.getSrcChIdx() != vSource.getSelectIndex()) { // 信号源通道不一致
                    vSource.setSelectIndex(canBus.getSrcChIdx()); // 同步信号源选中项
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true); // 触发信号源变更（来自EventBus）
                }
                if (canBus.getSignal() != vSignal.getSelectIndex()) { // 信号类型不一致
                    vSignal.setSelectIndex(canBus.getSignal()); // 同步信号类型选中项
                    onCheckChanged(vSignal.getId(), vSignal.getSelectItem(), true); // 触发信号类型变更（来自EventBus）
                }
                String baudRate = TBookUtil.getBaudRateFromInt(canBus.getBaudRate()); // 整数波特率转显示文本
                if (vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && (!vBaudRate.getSelectItem().getText().equals(baudRate)) // 标准波特率不匹配
                        || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().toString().equals(baudRate))) { // 自定义波特率不匹配
                    if (vBaudRate.setSelectText(baudRate)) { // 能匹配到预设波特率
//                        tvUserDefine.setVisibility(GONE);
                        tvUserDefine.setText(""); // 清空自定义波特率
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发标准波特率变更（来自EventBus）
                    } else { // 无法匹配预设，使用自定义波特率
//                        tvUserDefine.setVisibility(VISIBLE);
                        vBaudRate.clearSelect(); // 清除标准波特率选中
                        tvUserDefine.setText(baudRate); // 显示自定义波特率文本
                        vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中最后一项（自定义项）
                        onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true); // 触发自定义波特率变更（来自EventBus）
                    }
                }
                if (canBus.isISO() != (vISO.getSelectIndex() == 0)) { // ISO模式不一致
                    vISO.setSelectIndex(canBus.isISO() ? 0 : 1); // 同步ISO模式选中项
                    onCheckChanged(vISO.getId(), vISO.getSelectItem(), true); // 触发ISO变更（来自EventBus）
                }
            }
        }
    };

    /** 采样点点击监听器，弹出数字键盘输入采样点百分比 */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton(); // 播放按键音效
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
            ScreenUtil.getViewLocation(v); // 获取控件屏幕位置
            if (v.getId() == tvBaudRatePercent.getId()) { // 点击标准采样点
                dialogKeyBoard.setFloatData(Double.parseDouble(tvBaudRatePercent.getText().toString().replace("%", "")) // 当前采样点值
                        , 0.0, 99.9 // 最小值0，最大值99.9
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result); // 移除前导零
                                if (result.endsWith(".0")) { // 去除整数后缀.0
                                    result = result.replace(".0", "");
                                }
                                onTextChanged(tvBaudRatePercent.getId(), result, false); // 触发采样点变更
                            }
                        });
            } else if (v.getId() == tvFdPercent.getId()) { // 点击FD采样点
                dialogKeyBoard.setFloatData(Double.parseDouble(tvFdPercent.getText().toString().replace("%", "")) // 当前FD采样点值
                        , 0.0, 99.9 // 最小值0，最大值99.9
                        , new TopDialogNumberKeyBoard.OnDismissListener() {
                            @Override
                            public void onDismiss(String result) {
                                result = TBookUtil.getNumRemovePreZero(result); // 移除前导零
                                if (result.endsWith(".0")) { // 去除整数后缀.0
                                    result = result.replace(".0", "");
                                }
                                onTextChanged(tvFdPercent.getId(), result, false); // 触发FD采样点变更
                            }
                        });
            }
        }
    };

    /** 自定义波特率编辑点击监听器，弹出数字键盘输入自定义波特率值 */
    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton(); // 播放按键音效
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
            if (view.getId() == tvUserDefine.getId()) { // 点击标准波特率编辑框
                String s = tvUserDefine.getText(); // 获取当前文本
                if (!StrUtil.isEmpty(s)) { // 有已有文本
                    String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "") // 去除kb/s单位
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "") // 去除Mb/s单位
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_BS, ""); // 去除b/s单位
                    double preDouble = Double.parseDouble(number); // 解析数值部分
                    String preBs = s.replace(number, ""); // 提取单位部分
                    dialogKeyBoard.setBaudRateData(preDouble, preBs // 设置键盘初始数据
                            , 10 * 1000, 5 * 1000 * 1000 // 最小10kb/s，最大5000Mb/s
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvUserDefine.getId(), result, false); // 触发自定义波特率变更
                                }
                            });
                } else { // 无已有文本
                    dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS // 默认从0kb/s开始
                            , 10 * 1000, 5 * 1000 * 1000 // 最小10kb/s，最大5000Mb/s
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvUserDefine.getId(), result, false); // 触发自定义波特率变更
                                }
                            });
                }
            } else if (view.getId() == tvFDUserDefine.getId()) { // 点击FD波特率编辑框
                String s = tvFDUserDefine.getText(); // 获取当前文本
                if (!StrUtil.isEmpty(s)) { // 有已有文本
                    String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "") // 去除kb/s单位
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "") // 去除Mb/s单位
                            .replace(TopDialogNumberKeyBoard.KEYBOARD_BS, ""); // 去除b/s单位
                    double preDouble = Double.parseDouble(number); // 解析数值部分
                    String preBs = s.replace(number, ""); // 提取单位部分
                    dialogKeyBoard.setBaudRateData(preDouble, preBs // 设置键盘初始数据
                            , 10 * 1000, 12 * 1000 * 1000 // 最小10kb/s，最大12000Mb/s
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvFDUserDefine.getId(), result, false); // 触发FD自定义波特率变更
                                }
                            });
                } else { // 无已有文本
                    dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS // 默认从0kb/s开始
                            , 10 * 1000, 12 * 1000 * 1000 // 最小10kb/s，最大12000Mb/s
                            , new TopDialogNumberKeyBoard.OnDismissListener() {
                                @Override
                                public void onDismiss(String result) {
                                    onTextChanged(tvFDUserDefine.getId(), result, false); // 触发FD自定义波特率变更
                                }
                            });
                }
            }
        }
    };

    /** 选择控件点击监听器，处理CAN各选项的点击事件 */
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
            // 刷新UI后回调（CAN未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 刷新UI前回调（CAN未使用）
        }
    };

    /**
     * CAN选项变更核心处理方法，处理信号源/信号类型/标准波特率/FD波特率/ISO模式的选择变更，
     * 同步CanBus对象、下发SCPI命令、更新缓存和消息体
     *
     * @param viewId        触发变更的控件ID
     * @param item          选中的选项项
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vSource.getId()) { // 信号源变更
            if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                canBus.setSrcChIdx(item.getIndex()); // 设置CanBus信号源通道索引
            }
            Command.get().getTrigger_can().setSource(serialsNumber - 1, item.getIndex(), false); // 下发触发源SCPI
            Command.get().getBus_can().Channel(serialsNumber - 1, item.getIndex(), false); // 下发CAN通道SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + serialsNumber, String.valueOf(item.getIndex())); // 缓存信号源索引
            msgDetailsCan.setSource(item); // 更新消息体信号源
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vSignal.getId()) { // 信号类型变更
            if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                canBus.setSignal(item.getIndex()); // 设置CanBus信号类型
            }
            Command.get().getTrigger_can().setIdle(serialsNumber - 1, item.getIndex(), false); // 下发空闲电平SCPI
            Command.get().getBus_can().Signal(serialsNumber - 1, item.getIndex(), false); // 下发CAN信号SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serialsNumber, String.valueOf(item.getIndex())); // 缓存信号类型索引
            msgDetailsCan.setSignal(item); // 更新消息体信号类型
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vBaudRate.getId()) { // 标准波特率变更
            Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false); // 下发触发波特率SCPI
            Command.get().getBus_can().BaudRate(serialsNumber - 1, item.getIndex(), false); // 下发CAN标准波特率SCPI
//            Command.get().getBus_can().BaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false);
            Command.get().getBus_can().UserBaud(serialsNumber - 1, -1, false); // 下发自定义波特率-1表示不使用

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_BAUDRATE + serialsNumber, String.valueOf(item.getIndex())); // 缓存标准波特率索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber, ""); // 清空自定义波特率缓存
            tvUserDefine.setText(""); // 清空自定义波特率文本
            if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                int baudRate = getBaudRateValue(); // 获取波特率整数值
                canBus.setBaudRate(baudRate); // 设置CanBus标准波特率
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
            }
            msgDetailsCan.setBaudRate(item); // 更新消息体标准波特率
            sendMsg(isFromEventBus); // 通知父布局
            if (serialsNumber == RightMsgSerials.SERIALS_S1) { // S1通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_CAN); // 通知S1 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) { // S2通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_CAN); // 通知S2 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) { // S3通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_CAN); // 通知S3 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) { // S4通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_CAN); // 通知S4 CAN控件可见
            }
        } else if (viewId == vFDBaudRate.getId()) { // FD波特率变更
            Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, -1, false); // 下发FD自定义波特率-1表示不使用
            Command.get().getBus_can().FDBAudrate(serialsNumber - 1, item.getIndex(), false); // 下发FD标准波特率SCPI

            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + serialsNumber, String.valueOf(item.getIndex())); // 缓存FD波特率索引
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber, ""); // 清空FD自定义波特率缓存
            tvFDUserDefine.setText(""); // 清空FD自定义波特率文本
            if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                int baudRate = getFDBaudRateValue(); // 获取FD波特率整数值
                canBus.setFDBandRate(baudRate); // 设置CanBus FD波特率
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
            }
            msgDetailsCan.setFDBaudRate(item); // 更新消息体FD波特率
            sendMsg(isFromEventBus); // 通知父布局
            if (serialsNumber == RightMsgSerials.SERIALS_S1) { // S1通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_CAN); // 通知S1 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) { // S2通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_CAN); // 通知S2 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) { // S3通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_CAN); // 通知S3 CAN控件可见
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) { // S4通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_CAN); // 通知S4 CAN控件可见
            }
        } else if (viewId == vISO.getId()) { // ISO模式变更
            Command.get().getBus_can().ISO(serialsNumber - 1, item.getIndex(), false); // 下发ISO模式SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_ISO + serialsNumber, String.valueOf(item.getIndex())); // 缓存ISO模式索引
            if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                canBus.setISO(item.getIndex() == 0); // 设置CanBus ISO模式（索引0=ISO开启）
            }
            msgDetailsCan.setISO(item); // 更新消息体ISO模式
            sendMsg(isFromEventBus); // 通知父布局
        }

        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
    }

    /**
     * 文本变更处理方法，处理自定义波特率/FD自定义波特率/采样点/FD采样点的输入变更，
     * 包含波特率显示格式截断逻辑（kb/s截4位，Mb/s截4-5位含小数点）
     *
     * @param viewId        触发变更的控件ID
     * @param result        输入结果文本
     * @param isFromEventBus true=来自EventBus事件，false=来自用户UI操作
     */
    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) { // 标准自定义波特率变更
            String s = result.replace("kb/s", ""); // 去除kb/s单位
            if (!result.equals(s)) { // 包含kb/s单位
                if (s.length() > 4) { // 数值超过4位需截断
                    if (s.substring(0, 4).contains(".")) { // 含小数点截5位（含小数点）
                        result = s.substring(0, 5);
                    } else { // 纯整数截4位
                        result = s.substring(0, 4);
                    }
                    result += "kb/s"; // 拼回单位
                }
            } else { // 不含kb/s，检查Mb/s
                s = result.replace("Mb/s", ""); // 去除Mb/s单位
                if (s.length() > 4) { // 数值超过4位需截断
                    if (s.substring(0, 4).contains(".")) { // 含小数点截5位
                        result = s.substring(0, 5);
                    } else { // 纯整数截4位
                        result = s.substring(0, 4);
                    }
                    result += "Mb/s"; // 拼回单位
                }
            }
            vBaudRate.clearSelect(); // 清除标准波特率选中
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) { // 与当前值不同
                if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                    int baudRate = TBookUtil.getIntFromBaudRate(result); // 解析波特率整数值
                    canBus.setBaudRate(baudRate); // 设置CanBus标准波特率
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
                }
                Command.get().getTrigger_can().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发触发波特率SCPI
                Command.get().getBus_can().BaudRate(serialsNumber - 1, -1, false); // 下发标准波特率-1表示使用自定义
                Command.get().getBus_can().UserBaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发自定义波特率SCPI

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_USERDEFINE + serialsNumber, result); // 缓存自定义波特率
                tvUserDefine.setText(result); // 显示自定义波特率文本
                msgDetailsCan.setBaudRateDefine(result); // 更新消息体自定义波特率
                sendMsg(isFromEventBus); // 通知父布局
                SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
            }
        } else if (viewId == tvFDUserDefine.getId()) { // FD自定义波特率变更
            String s = result.replace("kb/s", ""); // 去除kb/s单位
            if (!result.equals(s)) { // 包含kb/s单位
                if (s.length() > 4) { // 数值超过4位需截断
                    if (s.substring(0, 4).contains(".")) { // 含小数点截5位
                        result = s.substring(0, 5);
                    } else { // 纯整数截4位
                        result = s.substring(0, 4);
                    }
                    result += "kb/s"; // 拼回单位
                }
            } else { // 不含kb/s，检查Mb/s
                s = result.replace("Mb/s", ""); // 去除Mb/s单位
                if (s.length() > 4) { // 数值超过4位需截断
                    if (s.substring(0, 4).contains(".")) { // 含小数点截5位
                        result = s.substring(0, 5);
                    } else { // 纯整数截4位
                        result = s.substring(0, 4);
                    }
                    result += "Mb/s"; // 拼回单位
                }
            }
            vFDBaudRate.clearSelect(); // 清除FD标准波特率选中
            if (!result.equalsIgnoreCase(tvFDUserDefine.getText().toString())) { // 与当前值不同
                if (!isFromEventBus) { // 非EventBus触发时更新CanBus
                    int baudRate = TBookUtil.getIntFromBaudRate(result); // 解析FD波特率整数值
                    canBus.setFDBandRate(baudRate); // 设置CanBus FD波特率
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 根据波特率设置水平时基
                }

                Command.get().getBus_can().FDUSerbaud(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发FD自定义波特率SCPI
                Command.get().getBus_can().FDBAudrate(serialsNumber - 1, -1, false); // 下发FD标准波特率-1表示使用自定义

                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + serialsNumber, result); // 缓存FD自定义波特率
                tvFDUserDefine.setText(result); // 显示FD自定义波特率文本
                msgDetailsCan.setFDBaudRateDefine(result); // 更新消息体FD自定义波特率
                sendMsg(isFromEventBus); // 通知父布局
                SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲区
            }
        } else if (viewId == tvBaudRatePercent.getId()) { // 采样点变更
            if (StrUtil.isEmpty(result)) { // 空值处理
                result = "0"; // 默认0
            }
            result = result.replace("%", ""); // 去除百分号
            tvBaudRatePercent.setText(result + "%"); // 显示采样点百分比
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_PERCENT + serialsNumber, result); // 缓存采样点值
            canBus.setSamplePlace1(Double.parseDouble(result) / 100); // 同步CanBus采样点1（百分比转小数）
            Command.get().getBus_can().SAMPlepoint(serialsNumber - 1, Double.parseDouble(result), false); // 下发采样点SCPI
        } else if (viewId == tvFdPercent.getId()) { // FD采样点变更
            if (StrUtil.isEmpty(result)) { // 空值处理
                result = "0"; // 默认0
            }
            result = result.replace("%", ""); // 去除百分号
            tvFdPercent.setText(result + "%"); // 显示FD采样点百分比
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_CAN_FDPERCENT + serialsNumber, result); // 缓存FD采样点值
            canBus.setSamplePlace2(Double.parseDouble(result) / 100); // 同步CanBus采样点2（百分比转小数）
            Command.get().getBus_can().FDSAmplepoint(serialsNumber - 1, Double.parseDouble(result), false); // 下发FD采样点SCPI
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