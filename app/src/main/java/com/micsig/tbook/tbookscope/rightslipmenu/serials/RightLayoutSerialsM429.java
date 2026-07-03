package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Bus.ARINC429Bus;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                    ARINC429(M429)串口协议配置布局                          |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— ARINC429协议参数配置的UI布局      |
 * | 核心职责: 提供ARINC429总线的信号源、数据格式、显示格式、波特率配置界面， |
 * |          处理用户交互和外部事件，切换显示格式时自动转换触发条件数据进制   |
 * | 架构设计: RelativeLayout子类，包含source/format/display/baudRate选择控件，|
 * |          采用观察者模式监听RxBus和EventBus事件                           |
 * | 数据流向: 用户UI → RightLayoutSerialsM429 → Command(SCPI) → 硬件        |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsM429, ARINC429Bus, Command,   |
 * |          ChannelFactory, CacheUtil, RxBus                               |
 * | 使用场景: 用户在右侧菜单选择ARINC429协议后，此布局显示M429参数配置       |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * ARINC429(M429)串口协议配置布局
 * <p>
 * 提供ARINC429总线的信号源通道、数据格式（LD/LDS/LSDS）、
 * 显示格式（二进制/十六进制）和波特率等参数的配置界面。
 * 切换显示格式时自动转换触发条件数据的进制表示。
 * </p>
 */
public class RightLayoutSerialsM429 extends RelativeLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 信号源/数据格式/显示格式/波特率选择控件 */
    private RightViewSelect vSource, vFormat, vDisplay, vBaudRate; // M429配置选择控件

    /** M429协议详情消息体 */
    private RightMsgSerialsM429 msgDetailsM429; // M429协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** ARINC429总线数据对象 */
    private ARINC429Bus a429Bus; // ARINC429总线数据对象

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsM429(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsM429(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsM429(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_m429, this); // 加载M429布局
        vSource = (RightViewSelect) findViewById(R.id.source); // 信号源选择控件
        vFormat = (RightViewSelect) findViewById(R.id.format); // 数据格式选择控件
        vDisplay = (RightViewSelect) findViewById(R.id.display); // 显示格式选择控件
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate); // 波特率选择控件

        vSource.setArray(GlobalVar.get().getChannelsName()); // 设置信号源通道名称数组

        vSource.setOnItemClickListener(onItemClickListener); // 绑定信号源点击监听
        vFormat.setOnItemClickListener(onItemClickListener); // 绑定格式点击监听
        vDisplay.setOnItemClickListener(onItemClickListener); // 绑定显示格式点击监听
        vBaudRate.setOnItemClickListener(onItemClickListener); // 绑定波特率点击监听

        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsM429 = new RightMsgSerialsM429(); // 创建M429详情消息对象
        msgDetailsM429.setSource(vSource.getSelectItem()); // 设置当前信号源
        msgDetailsM429.setFormat(vFormat.getSelectItem()); // 设置当前数据格式
        msgDetailsM429.setDisplay(vDisplay.getSelectItem()); // 设置当前显示格式
        msgDetailsM429.setBaudRate(vBaudRate.getSelectItem()); // 设置当前波特率
    }

    /**
     * 从缓存恢复M429配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber); // 读取缓存的信号源索引
        int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber); // 读取缓存的数据格式索引
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber); // 读取缓存的显示格式索引
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_BAUDRATE + serialsNumber); // 读取缓存的波特率索引
        vSource.setSelectIndex(source); // 恢复信号源选择
        vFormat.setSelectIndex(format); // 恢复数据格式选择
        vDisplay.setSelectIndex(display); // 恢复显示格式选择
        vBaudRate.setSelectIndex(baudRate); // 恢复波特率选择

        Command.get().getTrigger_m429().setSource(serialsNumber - 1, source, false); // 下发触发信号源SCPI
        Command.get().getTrigger_m429().setFormat(serialsNumber - 1, format, false); // 下发触发格式SCPI
        Command.get().getTrigger_m429().setDisplay(serialsNumber - 1, display, false); // 下发触发显示格式SCPI
        Command.get().getTrigger_m429().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()), false); // 下发触发波特率SCPI

        Command.get().getBus_429().Source(serialsNumber-1,source,false); // 下发总线信号源SCPI
        Command.get().getBus_429().Format(serialsNumber-1,format,false); // 下发总线格式SCPI
        Command.get().getBus_429().Display(serialsNumber-1,display,false); // 下发总线显示格式SCPI
        Command.get().getBus_429().BaudRate(serialsNumber-1,baudRate,false); // 下发总线波特率SCPI

        a429Bus.setSrcChIdx(source); // 更新总线对象信号源
        a429Bus.setFormat(getFormatValueToScope(format)); // 更新总线对象格式（转换为内部枚举值）
        a429Bus.setDisplayFormat(display); // 更新总线对象显示格式
        a429Bus.setBaudRate(TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText())); // 更新总线对象波特率

//        if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//            SerialBusManage.getInstance().getSerialBus(TChan.S1).set429Encoding(display);
//        } else {
//            SerialBusManage.getInstance().getSerialBus(TChan.S2).set429Encoding(display);
//        }
        int chIdx=TChan.toSerialTChan(serialsNumber); // 转换为通道索引
        SerialBusManage.getInstance().getSerialBus(chIdx).set429Encoding(display); // 设置429编码方式

        msgDetailsM429.setSource(vSource.getSelectItem()); // 同步消息体信号源
        msgDetailsM429.setFormat(vFormat.getSelectItem()); // 同步消息体格式
        msgDetailsM429.setDisplay(vDisplay.getSelectItem()); // 同步消息体显示格式
        msgDetailsM429.setBaudRate(vBaudRate.getSelectItem()); // 同步消息体波特率
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.M429) { // 当前协议为M429时
            sendMsg(false); // 发送消息通知父布局
        }
    }

    /**
     * 将UI格式索引转换为总线内部格式常量
     *
     * @param format UI格式索引（0=LD, 1=LDS, 2+=LSDS）
     * @return ARINC429Bus格式常量
     */
    private int getFormatValueToScope(int format) {
        if (format == 0) { // LD格式
            return ARINC429Bus.ARINC429_LABEL_DATA; // Label+Data
        } else if (format == 1) { // LDS格式
            return ARINC429Bus.ARINC429_LABEL_DATA_SSM; // Label+Data+SSM
        } else { // LSDS格式
            return ARINC429Bus.ARINC429_LABEL_SDI_DATA_SSM; // Label+SDI+Data+SSM
        }
    }

    /**
     * 将总线内部格式常量转换为UI格式索引
     *
     * @param format ARINC429Bus格式常量
     * @return UI格式索引（0, 1, 2）
     */
    private int getFormatValueFromScope(int format) {
        if (format == ARINC429Bus.ARINC429_LABEL_DATA) { // Label+Data格式
            return 0; // 对应索引0
        } else if (format == ARINC429Bus.ARINC429_LABEL_DATA_SSM) { // Label+Data+SSM格式
            return 1; // 对应索引1
        } else { // Label+SDI+Data+SSM格式
            return 2; // 对应索引2
        }
    }


    /**
     * 初始化控件，订阅RxBus和EventBus事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令回传事件
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam); // 注册总线参数事件观察者
    }

    /**
     * 设置串口通道编号，并获取对应的ARINC429Bus对象
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber; // 保存串口编号
        int tChan=TChan.toSerialTChan(serialsNumber); // 转换为TChan通道索引
        int fpgaChan=TChan.toFpgaChNo(tChan); // 转换为FPGA通道编号
        setControlColorByChIdx(tChan); // 设置控件颜色
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan); // 获取串口通道对象
        if (serialChannel == null) return; // 通道不存在则返回
        a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 获取ARINC429总线对象
    }

    /**
     * 根据通道索引设置控件颜色
     *
     * @param chIdx 通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx); // 设置信号源控件颜色
        vFormat.setControlColorByChIdx(chIdx); // 设置格式控件颜色
        vDisplay.setControlColorByChIdx(chIdx); // 设置显示格式控件颜色
        vBaudRate.setControlColorByChIdx(chIdx); // 设置波特率控件颜色
    }

    /**
     * 获取M429详情消息体
     *
     * @return M429协议详情消息
     */
    public RightMsgSerialsM429 getMsgDetailsM429() {
        return msgDetailsM429; // 返回M429详情消息
    }

    /**
     * 设置详情变更发送消息监听器
     *
     * @param onSerialsDetailSendMsgListener 监听器实例
     */
    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener; // 保存监听器
    }

    /**
     * 通过监听器发送详情变更消息
     *
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) { // 监听器不为空
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsM429.this, isFromEventBus); // 通知父布局
        }
    }

    /**
     * 缓存加载事件消费者，收到缓存加载事件后恢复M429配置
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道不存在则返回
            setCache(); // 从缓存恢复配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsM429, true); // 标记M429缓存已加载
        }
    };

    /**
     * 命令回传事件消费者，处理外部SCPI命令回传的M429参数变更
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_TRIGGERM429_SOURCE: { // 触发信号源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) { // 值不同才更新
                        vSource.setSelectIndex(Integer.parseInt(params[1])); // 更新信号源选择
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_FORMAT: { // 触发格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) { // 值不同才更新
                        vFormat.setSelectIndex(Integer.parseInt(params[1])); // 更新格式选择
                        onCheckChanged(vFormat.getId(), vFormat.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_DISPLAY: { // 触发显示格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && vSource.getSelectIndex() != Integer.parseInt(params[1])) { // 值不同才更新
                        vDisplay.setSelectIndex(Integer.parseInt(params[1])); // 更新显示格式选择
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM429_BAUDRATE: { // 触发波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) { // 匹配通道编号
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1])); // 将波特率值转为显示文本
                        if (!vBaudRate.getSelectItem().getText().equals(baudRate)) { // 值不同才更新
                            if (vBaudRate.setSelectText(baudRate)) { // 尝试匹配预设波特率
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 匹配成功，触发变更
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_429_Channel:{ // 总线信号源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String ch=params[0]; // 通道编号参数
                    int source=Integer.parseInt(params[1]); // 信号源索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vSource.setSelectIndex(source); // 更新信号源选择
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_429_Format:{ // 总线格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String ch=params[0]; // 通道编号参数
                    int format=Integer.parseInt(params[1]); // 格式索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vFormat.setSelectIndex(format); // 更新格式选择
                    onCheckChanged(vFormat.getId(),vFormat.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_429_display:{ // 总线显示格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String ch=params[0]; // 通道编号参数
                    int display=Integer.parseInt(params[1]); // 显示格式索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vDisplay.setSelectIndex(display); // 更新显示格式选择
                    onCheckChanged(vDisplay.getId(),vDisplay.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_429_Baudrate:{ // 总线波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String ch=params[0]; // 通道编号参数
                    int baudrate=Integer.parseInt(params[1]); // 波特率索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vBaudRate.setSelectIndex(baudrate); // 更新波特率选择
                    onCheckChanged(vBaudRate.getId(),vBaudRate.getSelectItem(),false); // 触发变更处理
                }break;

            }
        }
    };

    /**
     * EventBus总线参数观察者，监听ARINC429总线参数变化并同步UI
     */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件数据
            if (a429Bus == null) return; // 总线对象为空则返回
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 事件类型匹配
                    && a429Bus.equals(eventBase.getData())) { // 总线对象匹配
                if (a429Bus.getSrcChIdx() != vSource.getSelectIndex()) { // 信号源不一致
                    vSource.setSelectIndex(a429Bus.getSrcChIdx()); // 同步信号源
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true); // 触发变更
                }
                int format = getFormatValueFromScope(a429Bus.getFormat()); // 转换格式为UI索引
                if (format != vFormat.getSelectIndex()) { // 格式不一致
                    vFormat.setSelectIndex(format); // 同步格式
                    onCheckChanged(vFormat.getId(), vFormat.getSelectItem(), true); // 触发变更
                }
                if (a429Bus.getDisplayFormat() != vDisplay.getSelectIndex()) { // 显示格式不一致
                    vDisplay.setSelectIndex(a429Bus.getDisplayFormat()); // 同步显示格式
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true); // 触发变更
                }
                String baudRate = TBookUtil.getBaudRateFromInt(a429Bus.getBaudRate()); // 转换波特率为显示文本
                if (!vBaudRate.getSelectItem().getText().equals(baudRate)) { // 波特率不一致
                    if (vBaudRate.setSelectText(baudRate)) { // 尝试匹配预设波特率
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发变更
                    }
                }
            }
        }
    };

    /**
     * 选择控件点击监听器，播放按键音并触发参数变更
     */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            onCheckChanged(viewId, item, false); // 触发参数变更处理
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 空实现 - 点击后刷新UI回调（未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 空实现 - 点击前刷新UI回调（未使用）
        }
    };

    /**
     * 通道选择变更处理，根据控件ID分别处理信号源/格式/显示格式/波特率变更，
     * 更新总线对象、下发SCPI命令、保存缓存、发送消息通知
     *
     * @param viewId         变更的控件ID
     * @param item           选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vSource.getId()) { // 信号源变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                a429Bus.setSrcChIdx(vSource.getSelectIndex()); // 更新总线信号源
            }
            Command.get().getTrigger_m429().setSource(serialsNumber - 1, item.getIndex(), false); // 下发触发信号源SCPI
            Command.get().getBus_429().Source(serialsNumber-1,item.getIndex(),false); // 下发总线信号源SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsM429.setSource(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vFormat.getId()) { // 数据格式变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                a429Bus.setFormat(getFormatValueToScope(vFormat.getSelectIndex())); // 更新总线格式
            }
            Command.get().getTrigger_m429().setFormat(serialsNumber - 1, item.getIndex(), false); // 下发触发格式SCPI
            Command.get().getBus_429().Format(serialsNumber-1,item.getIndex(),false); // 下发总线格式SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsM429.setFormat(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vDisplay.getId()) { // 显示格式变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                a429Bus.setDisplayFormat(vDisplay.getSelectIndex()); // 更新总线显示格式
            }
            Command.get().getTrigger_m429().setDisplay(serialsNumber - 1, item.getIndex(), false); // 下发触发显示格式SCPI
            Command.get().getBus_429().Display(serialsNumber-1,item.getIndex(),false); // 下发总线显示格式SCPI
            int preDigits; // 之前的进制位数
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber) == 0) { // 之前为二进制
                preDigits = IDigits.DIGITS_2; // 二进制位数
            } else { // 之前为十六进制
                preDigits = IDigits.DIGITS_16; // 十六进制位数
            }
            setTopSerialsM429Data(preDigits, item.getIndex() == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16); // 转换触发条件数据进制
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsM429.setDisplay(item); // 更新消息体
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).set429Encoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).set429Encoding(item.getIndex());
//            }
            int chIdx=TChan.toSerialTChan(serialsNumber); // 转换为通道索引
            SerialBusManage.getInstance().getSerialBus(chIdx).set429Encoding(item.getIndex()); // 设置429编码方式

            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vBaudRate.getId()) { // 波特率变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                int baudRate = TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()); // 获取波特率整型值
                a429Bus.setBaudRate(baudRate); // 更新总线波特率
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 同步更新水平时基
            }



            Command.get().getTrigger_m429().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false); // 下发触发波特率SCPI
            Command.get().getBus_429().BaudRate(serialsNumber-1,item.getIndex(),false); // 下发总线波特率SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M429_BAUDRATE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsM429.setBaudRate(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲
    }

    /**
     * 切换M429触发条件数据的进制表示，同时更新缓存
     *
     * @param preDigits   之前的进制位数
     * @param nextDigits  目标进制位数
     */
    private void setTopSerialsM429Data(int preDigits, int nextDigits) {
        String key; // 缓存键
        for (int i = 0; i < 2; i++) { // 遍历两种触发条件数据
            if (i == 0) { // 第一种：数据触发条件
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_DATA + serialsNumber; // 数据缓存键
            } else { // 第二种：标签数据触发条件
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + serialsNumber; // 标签数据缓存键
            }
            String s = "setTopSerialsM429Data:"; // 日志前缀
            String data = CacheUtil.get().getString(key); // 从缓存读取数据
            s += data; // 追加原始数据
            data = SerialsUtils.HexBin(data, preDigits, nextDigits); // 进制转换
            s += ("," + data); // 追加转换后数据
            data = SerialsUtils.reCalcSpace(data, nextDigits == IDigits.DIGITS_2 ? 23 : 6, nextDigits); // 重算空格间距
            s += ("," + data); // 追加重算后数据
            CacheUtil.get().putMap(key, data); // 更新缓存
            Logger.d("setTopSerialsM429Data:"+s); // 打印调试日志
        }
    }

    /**
     * 获取串口通道编号
     *
     * @return 串口通道编号
     */
    public int getSerialsNumber() {
        return serialsNumber; // 返回串口编号
    }
}
