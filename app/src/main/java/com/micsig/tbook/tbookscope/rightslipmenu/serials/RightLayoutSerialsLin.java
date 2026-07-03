package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.LinBus;
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
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
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
 * |                       LIN串口协议配置布局                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— LIN协议参数配置的UI布局           |
 * | 核心职责: 提供LIN总线的信号源、LIN类型、空闲电平、波特率配置界面，       |
 * |          处理用户交互和外部事件                                           |
 * | 架构设计: RelativeLayout子类，包含source/linType/idle/baudRate/          |
 * |          userDefine控件，采用观察者模式监听RxBus和EventBus事件           |
 * | 数据流向: 用户UI → RightLayoutSerialsLin → Command(SCPI) → 硬件         |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsLin, LinBus, Command,          |
 * |          ChannelFactory, CacheUtil, RxBus, TopDialogNumberKeyBoard      |
 * | 使用场景: 用户在右侧菜单选择LIN协议后，此布局显示LIN参数配置             |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * LIN串口协议配置布局
 * <p>
 * 提供LIN总线的信号源通道、LIN协议版本、空闲电平、
 * 波特率及自定义波特率等参数的配置界面。
 * </p>
 */
public class RightLayoutSerialsLin extends RelativeLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 信号源/LIN类型/空闲电平/波特率选择控件 */
    private RightViewSelect vSource, vLinType, vIdle, vBaudRate; // LIN配置选择控件
    /** 自定义波特率编辑控件 */
    private RightViewUserDefineEdit tvUserDefine; // 自定义波特率编辑控件
    /** 数字键盘对话框 */
    private TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框

    /** LIN协议详情消息体 */
    private RightMsgSerialsLin msgDetailsLin; // LIN协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    public int serialsNumber; // 串口通道编号
    /** 根视图 */
    private ViewGroup rootView; // 根视图

    /** LIN总线数据对象 */
    LinBus linBus; // LIN总线数据对象

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsLin(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsLin(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsLin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        rootView=(ViewGroup) View.inflate(context, R.layout.layout_right_serials_lin, this); // 加载LIN布局

        vSource = (RightViewSelect) findViewById(R.id.source); // 信号源选择控件
        vLinType = (RightViewSelect) findViewById(R.id.linType); // LIN类型选择控件
        vIdle = (RightViewSelect) findViewById(R.id.idle); // 空闲电平选择控件
        vBaudRate = (RightViewSelect) findViewById(R.id.baudRate); // 波特率选择控件
        tvUserDefine = (RightViewUserDefineEdit) findViewById(R.id.userDefineSerialsLin); // 自定义波特率编辑控件

        vSource.setArray(GlobalVar.get().getChannelsName()); // 设置信号源通道名称数组

        vSource.setOnItemClickListener(onItemClickListener); // 绑定信号源点击监听
        vLinType.setOnItemClickListener(onItemClickListener); // 绑定LIN类型点击监听
        vIdle.setOnItemClickListener(onItemClickListener); // 绑定空闲电平点击监听
        vBaudRate.setOnItemClickListener(onItemClickListener); // 绑定波特率点击监听
        tvUserDefine.setOnEditClickListener(onEditClickListener); // 绑定自定义波特率编辑监听

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框

        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsLin = new RightMsgSerialsLin(); // 创建LIN详情消息对象
        msgDetailsLin.setSource(vSource.getSelectItem()); // 设置当前信号源
        msgDetailsLin.setLinType(vLinType.getSelectItem()); // 设置当前LIN类型
        msgDetailsLin.setIdleLevel(vIdle.getSelectItem()); // 设置当前空闲电平
        msgDetailsLin.setBaudRate(vBaudRate.getSelectItem()); // 设置当前波特率
        msgDetailsLin.setBaudRateDefine(""); // 初始化自定义波特率为空
    }

    /**
     * 从缓存恢复LIN配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    public void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + serialsNumber); // 读取缓存的信号源索引
        int linType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + serialsNumber); // 读取缓存的LIN类型索引
        int idle = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_IDLE + serialsNumber); // 读取缓存的空闲电平索引
        int baudRate = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_BAUDRATE + serialsNumber); // 读取缓存的波特率索引
        String userDefine = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber); // 读取缓存的自定义波特率
        vSource.setSelectIndex(source); // 恢复信号源选择
        vLinType.setSelectIndex(linType); // 恢复LIN类型选择
        vIdle.setSelectIndex(idle); // 恢复空闲电平选择
        if (!StrUtil.isEmpty(userDefine)) { // 有自定义波特率
            tvUserDefine.setText(userDefine); // 恢复自定义波特率文本
            vBaudRate.clearSelect(); // 清除波特率预设选择
        } else { // 无自定义波特率
            tvUserDefine.setText(""); // 清空自定义波特率文本
            vBaudRate.setSelectIndex(baudRate); // 恢复波特率预设选择
        }

        Command.get().getTrigger_lin().setSource(serialsNumber - 1, source, false); // 下发触发信号源SCPI
        Command.get().getTrigger_lin().setLinType(serialsNumber - 1, source, false); // 下发触发LIN类型SCPI
        Command.get().getTrigger_lin().setIdle(serialsNumber - 1, idle, false); // 下发触发空闲电平SCPI
        Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, getBaudRateValueToScope(), false); // 下发触发波特率SCPI

        Command.get().getBus_lin().Channel(serialsNumber-1,source,false); // 下发总线通道SCPI
        Command.get().getBus_lin().LinType(serialsNumber - 1, linType, false); // 下发总线LIN类型SCPI
        Command.get().getBus_lin().IdLevel(serialsNumber-1,idle,false); // 下发总线空闲电平SCPI
        if (!StrUtil.isEmpty(userDefine)){ // 有自定义波特率
            Command.get().getBus_lin().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(userDefine),false); // 下发总线自定义波特率SCPI
            Command.get().getBus_lin().BaudRate(serialsNumber-1,-1,false); // 清除预设波特率
        }else { // 无自定义波特率
            Command.get().getBus_lin().BaudRate(serialsNumber-1,baudRate,false); // 下发总线预设波特率SCPI
            Command.get().getBus_lin().UserBaud(serialsNumber-1,-1,false); // 清除自定义波特率
        }

        linBus.setSrcChIdx(source); // 更新总线对象信号源
        linBus.setLinType(linType); // 更新总线对象LIN类型
        linBus.setIdleLevel(idle); // 更新总线对象空闲电平
        linBus.setBaudRate(getBaudRateValueToScope()); // 更新总线对象波特率

        msgDetailsLin.setSource(vSource.getSelectItem()); // 同步消息体信号源
        msgDetailsLin.setLinType(vLinType.getSelectItem()); // 同步消息体LIN类型
        msgDetailsLin.setIdleLevel(vIdle.getSelectItem()); // 同步消息体空闲电平
        msgDetailsLin.setBaudRate(vBaudRate.getSelectItem()); // 同步消息体波特率
        msgDetailsLin.setBaudRateDefine(tvUserDefine.getText()); // 同步消息体自定义波特率
//        if (baudRate == vBaudRate.getSelectCount() - 1) {
//            tvUserDefine.setVisibility(VISIBLE);
//            vBaudRate.clearSelect();
//            RightBeanSelect bean = msgDetailsLin.getBaudRate();
//            msgDetailsLin.setBaudRate(new RightBeanSelect(bean.getIndex(), userDefine, bean.isCheck()));
//        } else {
//            tvUserDefine.setVisibility(GONE);
//        }
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.LIN) { // 当前协议为LIN时
            sendMsg(false); // 发送消息通知父布局
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
     * 获取实际波特率值：优先使用自定义波特率，否则使用预设波特率
     *
     * @return 波特率整型值
     */
    private int getBaudRateValueToScope() {
        if (StrUtil.isEmpty(tvUserDefine.getText())) { // 无自定义波特率
            return TBookUtil.getIntFromBaudRate(vBaudRate.getSelectItem().getText()); // 使用预设波特率
        } else { // 有自定义波特率
            return TBookUtil.getIntFromBaudRate(tvUserDefine.getText().toString()); // 使用自定义波特率
        }
    }

    /**
     * 设置串口通道编号，并获取对应的LinBus对象
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber; // 保存串口编号
        int tChan= TChan.toSerialTChan(serialsNumber); // 转换为TChan通道索引
        int fpgaChan=TChan.toFpgaChNo(tChan); // 转换为FPGA通道编号
        setControlColorByChIdx(tChan); // 设置控件颜色
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan); // 获取串口通道对象
        if (serialChannel == null) return; // 通道不存在则返回
        linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线对象

    }

    /**
     * 根据通道索引设置控件颜色
     *
     * @param chIdx 通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx); // 设置信号源控件颜色
        vLinType.setControlColorByChIdx(chIdx); // 设置LIN类型控件颜色
        vIdle.setControlColorByChIdx(chIdx); // 设置空闲电平控件颜色
        vBaudRate.setControlColorByChIdx(chIdx); // 设置波特率控件颜色
    }

    /**
     * 获取LIN详情消息体
     *
     * @return LIN协议详情消息
     */
    public RightMsgSerialsLin getMsgDetailsLin() {
        return msgDetailsLin; // 返回LIN详情消息
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
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsLin.this, isFromEventBus); // 通知父布局
        }
    }

    /**
     * 缓存加载事件消费者，收到缓存加载事件后恢复LIN配置
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道不存在则返回
            setCache(); // 从缓存恢复配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsLin, true); // 标记LIN缓存已加载
        }
    };

    /**
     * 命令回传事件消费者，处理外部SCPI命令回传的LIN参数变更
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_TRIGGERLIN_SOURCE: { // 触发信号源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && Integer.parseInt(params[1]) != vSource.getSelectIndex()) { // 值不同才更新
                        vSource.setSelectIndex(Integer.parseInt(params[1])); // 更新信号源选择
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_TYPE: { // 触发LIN类型变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && Integer.parseInt(params[1]) != vLinType.getSelectIndex()) { // 值不同才更新
                        vLinType.setSelectIndex(Integer.parseInt(params[1])); // 更新LIN类型选择
                        onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_IDLE: { // 触发空闲电平变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && Integer.parseInt(params[1]) != vIdle.getSelectIndex()) { // 值不同才更新
                        vIdle.setSelectIndex(Integer.parseInt(params[1])); // 更新空闲电平选择
                        onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERLIN_BAUDRATE: { // 触发波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1) { // 匹配通道编号
                        String baudRate = TBookUtil.getBaudRateFromInt(Integer.parseInt(params[1])); // 转换波特率值
                        if ((vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 && !vBaudRate.getSelectItem().getText().equals(baudRate)) // 预设波特率不匹配
                                || (vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 && !tvUserDefine.getText().toString().equals(baudRate))) { // 自定义波特率不匹配
                            if (vBaudRate.setSelectText(baudRate)) { // 尝试匹配预设波特率
//                                tvUserDefine.setVisibility(GONE);
                                tvUserDefine.setText(""); // 清空自定义波特率
                                onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发变更
                            } else { // 无法匹配预设
//                                tvUserDefine.setVisibility(VISIBLE);
                                vBaudRate.clearSelect(); // 清除预设选择
                                tvUserDefine.setText(baudRate); // 设置自定义波特率
                                vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中"自定义"项
                                onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true); // 触发文本变更
                            }
                        }
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_Lin_BaudRate:{ // 总线波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    int rateIndex=Integer.parseInt(params[1]); // 波特率索引
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return; // 非当前通道则返回
                    vBaudRate.setSelectIndex(rateIndex); // 更新波特率选择
                    tvUserDefine.setText(""); // 清空自定义波特率
                    onCheckChanged(vBaudRate.getId(),vBaudRate.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_Userbaud:{ // 总线自定义波特率变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    float rate=Integer.parseInt(params[1])/1000.0f; // 转换为kb/s单位
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return; // 非当前通道则返回
                    vBaudRate.clearSelect(); // 清除预设选择
                    onTextChanged(tvUserDefine.getId(),rate+"kb/s",false); // 触发自定义波特率变更

                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_Channel:{ // 总线通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    int ch=Integer.parseInt(params[1]); // 信号源索引
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return; // 非当前通道则返回
                    vSource.setSelectIndex(ch); // 更新信号源选择
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false); // 触发变更处理

                }break;
                case CommandMsgToUI.FLAG_Bus_Lin_TYPE: { // 总线LIN类型变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
//                    int sNum = Integer.parseInt(params[0]);
                    int linS = Integer.parseInt(params[1]); // LIN类型索引
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vLinType.setSelectIndex(linS); // 更新LIN类型选择
                    onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), false); // 触发变更处理
                }
                break;
                case CommandMsgToUI.FLAG_Bus_Lin_IdLevel:{ // 总线空闲电平变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    int idle=Integer.parseInt(params[1]); // 空闲电平索引
                    if (Integer.parseInt(params[0]) != serialsNumber - 1)return; // 非当前通道则返回
                    vIdle.setSelectIndex(idle); // 更新空闲电平选择
                    onCheckChanged(vIdle.getId(),vIdle.getSelectItem(),false); // 触发变更处理
                }break;

            }
        }
    };

    /**
     * EventBus总线参数观察者，监听LIN总线参数变化并同步UI
     */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件数据
            if (linBus == null) return; // 总线对象为空则返回
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 事件类型匹配
                    && linBus.equals(eventBase.getData())) { // 总线对象匹配
                if (linBus.getSrcChIdx() != vSource.getSelectItem().getIndex()) { // 信号源不一致
                    vSource.setSelectIndex(linBus.getSrcChIdx()); // 同步信号源
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true); // 触发变更
                }
                if (linBus.getLinType() != vLinType.getSelectItem().getIndex()) { // LIN类型不一致
                    vLinType.setSelectIndex(linBus.getLinType()); // 同步LIN类型
                    onCheckChanged(vLinType.getId(), vLinType.getSelectItem(), true); // 触发变更
                }
                if (linBus.getIdleLevel() != vIdle.getSelectItem().getIndex()) { // 空闲电平不一致
                    vIdle.setSelectIndex(linBus.getIdleLevel()); // 同步空闲电平
                    onCheckChanged(vIdle.getId(), vIdle.getSelectItem(), true); // 触发变更
                }
                String baudRate = TBookUtil.getBaudRateFromInt(linBus.getBaudRate()); // 转换波特率
                if ((/*vBaudRate.getSelectItem().getIndex() != vBaudRate.getSelectCount() - 1 &&*/ !vBaudRate.getSelectItem().getText().equals(baudRate)) // 预设波特率不匹配
                        || (/*vBaudRate.getSelectItem().getIndex() == vBaudRate.getSelectCount() - 1 &&*/ !tvUserDefine.getText().toString().equals(baudRate))) { // 自定义波特率不匹配
                    if (vBaudRate.setSelectText(baudRate)) { // 尝试匹配预设波特率
//                        tvUserDefine.setVisibility(GONE);
                        tvUserDefine.setText(""); // 清空自定义波特率
                        onCheckChanged(vBaudRate.getId(), vBaudRate.getSelectItem(), true); // 触发变更
                    } else { // 无法匹配预设
//                        tvUserDefine.setVisibility(VISIBLE);
                        vBaudRate.clearSelect(); // 清除预设选择
                        tvUserDefine.setText(baudRate); // 设置自定义波特率
                        vBaudRate.setSelectIndex(vBaudRate.getSelectCount() - 1); // 选中"自定义"项
                        onTextChanged(tvUserDefine.getId(), tvUserDefine.getText().toString(), true); // 触发文本变更
                    }
                }
            }
        }
    };

    /**
     * 自定义波特率编辑点击监听器，弹出数字键盘设置自定义波特率
     */
    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            PlaySound.getInstance().playButton(); // 播放按键音效
            String s = tvUserDefine.getText(); // 获取当前自定义波特率文本
            if(dialogKeyBoard == null) dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘
            if (!StrUtil.isEmpty(s)) { // 已有自定义波特率
                String number = s.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, ""); // 提取数字部分
                double preDouble = Double.parseDouble(number); // 转为浮点数
                String preBs = s.replace(number, ""); // 提取单位部分
                dialogKeyBoard.setBaudRateData(preDouble, preBs, 2400, 625 * 1000, onDismissListener); // 设置键盘初始值（范围2.4k~625k）
            } else { // 无自定义波特率
                dialogKeyBoard.setBaudRateData(0, TopDialogNumberKeyBoard.KEYBOARD_KBS, 2400, 625 * 1000, onDismissListener); // 默认0kb/s
            }
        }
    };

    /**
     * 数字键盘关闭监听器，将用户输入结果传递给onTextChanged
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            onTextChanged(tvUserDefine.getId(), result, false); // 触发文本变更处理
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
     * 通道选择变更处理，根据控件ID分别处理信号源/LIN类型/空闲电平/波特率变更，
     * 更新总线对象、下发SCPI命令、保存缓存、发送消息通知
     *
     * @param viewId         变更的控件ID
     * @param item           选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        //  Tools.PrintControlsLocation("linTypeLocation", rootView);
        if (viewId == vSource.getId()) { // 信号源变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                linBus.setSrcChIdx(item.getIndex()); // 更新总线信号源
            }
            Command.get().getTrigger_lin().setSource(serialsNumber - 1, item.getIndex(), false); // 下发触发信号源SCPI
            Command.get().getBus_lin().Channel(serialsNumber-1,item.getIndex(),false); // 下发总线通道SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsLin.setSource(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vLinType.getId()) { // LIN类型变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                linBus.setLinType(item.getIndex()); // 更新总线LIN类型
            }
            Command.get().getTrigger_lin().setLinType(serialsNumber - 1, item.getIndex(), false); // 下发触发LIN类型SCPI
            Command.get().getBus_lin().LinType(serialsNumber - 1, item.getIndex(), false); // 下发总线LIN类型SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_TYPE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsLin.setLinType(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vIdle.getId()) { // 空闲电平变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                linBus.setIdleLevel(item.getIndex()); // 更新总线空闲电平
            }
            Command.get().getTrigger_lin().setIdle(serialsNumber - 1, item.getIndex(), false); // 下发触发空闲电平SCPI
            Command.get().getBus_lin().IdLevel(serialsNumber-1,item.getIndex(),false); // 下发总线空闲电平SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_IDLE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsLin.setIdleLevel(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vBaudRate.getId()) { // 波特率变更
            Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(item.getText()), false); // 下发触发波特率SCPI
            Command.get().getBus_lin().BaudRate(serialsNumber-1,item.getIndex(),false); // 下发总线波特率SCPI
            Command.get().getBus_lin().UserBaud(serialsNumber-1,-1,false); // 清除自定义波特率
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_BAUDRATE + serialsNumber, String.valueOf(item.getIndex())); // 保存波特率缓存
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber, ""); // 清空自定义波特率缓存
            tvUserDefine.setText(""); // 清空自定义波特率文本
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                int baudRate = getBaudRateValueToScope(); // 获取实际波特率值
                linBus.setBaudRate(baudRate); // 更新总线波特率
                HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 同步更新水平时基
            }
            msgDetailsLin.setBaudRate(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
            if (serialsNumber == RightMsgSerials.SERIALS_S1) { // S1通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS1_LIN); // 通知可见性变更
            } else if (serialsNumber == RightMsgSerials.SERIALS_S2) { // S2通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS2_LIN); // 通知可见性变更
            } else if (serialsNumber == RightMsgSerials.SERIALS_S3) { // S3通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS3_LIN); // 通知可见性变更
            } else if (serialsNumber == RightMsgSerials.SERIALS_S4) { // S4通道
                RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_RIGHTSERIALS4_LIN); // 通知可见性变更
            }
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲
    }

    /**
     * 自定义波特率文本变更处理，解析输入值、更新总线对象、下发SCPI命令、保存缓存
     *
     * @param viewId         变更的控件ID
     * @param result         用户输入的波特率字符串
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(int viewId, String result, boolean isFromEventBus) {
        if (viewId == tvUserDefine.getId()) { // 自定义波特率编辑框变更
            String s = result.replace("kb/s", ""); // 去除kb/s单位
            if (s.length() > 4 && s.substring(0, 4).contains(".")) { // 有小数点且超过4位
                result = s.substring(0, 5); // 截取5位（含小数点）
                result += "kb/s"; // 重新添加单位
            } else if (s.length() > 4) { // 无小数点且超过4位
                result = s.substring(0, 4); // 截取4位
                result += "kb/s"; // 重新添加单位
            }
            vBaudRate.clearSelect(); // 清除预设波特率选择
            if (!result.equalsIgnoreCase(tvUserDefine.getText().toString())) { // 值不同才更新
                if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                    int baudRate = TBookUtil.getIntFromBaudRate(result); // 转换为整型波特率
                    linBus.setBaudRate(baudRate); // 更新总线波特率
                    HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.BaudrateToTimeScale(baudRate)); // 同步更新水平时基
                }
                Command.get().getTrigger_lin().setBaudRate(serialsNumber - 1, TBookUtil.getIntFromBaudRate(result), false); // 下发触发波特率SCPI
                Command.get().getBus_lin().UserBaud(serialsNumber-1,TBookUtil.getIntFromBaudRate(result),false); // 下发总线自定义波特率SCPI
                Command.get().getBus_lin().BaudRate(serialsNumber-1,-1,false); // 清除预设波特率
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_LIN_USERDEFINE + serialsNumber, result); // 保存自定义波特率缓存
                tvUserDefine.setText(result); // 更新编辑框文本
                msgDetailsLin.setBaudRateDefine(result); // 更新消息体
                sendMsg(isFromEventBus); // 通知父布局
                SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲
            }
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
