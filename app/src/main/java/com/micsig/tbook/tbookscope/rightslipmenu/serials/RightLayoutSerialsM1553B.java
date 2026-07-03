package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Bus.MILSTD1553BBus;
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                   MIL-STD-1553B串口协议配置布局                            |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— M1553B协议参数配置的UI布局       |
 * | 核心职责: 提供MIL-STD-1553B总线的信号源和显示格式配置界面，             |
 * |          处理用户交互和外部事件                                           |
 * | 架构设计: RelativeLayout子类，包含source/display选择控件，               |
 * |          采用观察者模式监听RxBus和EventBus事件                           |
 * | 数据流向: 用户UI → RightLayoutSerialsM1553B → Command(SCPI) → 硬件     |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsM1553b, MILSTD1553BBus,       |
 * |          Command, ChannelFactory, CacheUtil, RxBus                       |
 * | 使用场景: 用户在右侧菜单选择M1553B协议后，此布局显示参数配置             |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * MIL-STD-1553B串口协议配置布局
 * <p>
 * 提供MIL-STD-1553B总线的信号源通道和显示格式（二进制/十六进制）配置界面。
 * 切换显示格式时自动转换触发条件数据的进制表示。
 * </p>
 */
public class RightLayoutSerialsM1553B extends RelativeLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** 信号源/显示格式选择控件 */
    private RightViewSelect vSource, vDisplay; // 信号源和显示格式选择控件

    /** M1553B协议详情消息体 */
    private RightMsgSerialsM1553b msgDetailsM1553B; // M1553B协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** M1553B总线数据对象 */
    private MILSTD1553BBus m1553bBus; // M1553B总线数据对象

    /**
     * 单参数构造函数
     *
     * @param context 上下文
     */
    public RightLayoutSerialsM1553B(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public RightLayoutSerialsM1553B(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutSerialsM1553B(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_m1553b, this); // 加载M1553B布局
        vSource = (RightViewSelect) findViewById(R.id.source); // 信号源选择控件
        vDisplay = (RightViewSelect) findViewById(R.id.display); // 显示格式选择控件

        vSource.setArray(GlobalVar.get().getChannelsName()); // 设置信号源通道名称数组

        vSource.setOnItemClickListener(onItemClickListener); // 绑定信号源点击监听
        vDisplay.setOnItemClickListener(onItemClickListener); // 绑定显示格式点击监听

        initData(); // 初始化默认数据
    }

    /**
     * 初始化默认数据，创建消息体并设置当前选择项
     */
    private void initData() {
        msgDetailsM1553B = new RightMsgSerialsM1553b(); // 创建M1553B详情消息对象
        msgDetailsM1553B.setSource(vSource.getSelectItem()); // 设置当前信号源
        msgDetailsM1553B.setDisplay(vDisplay.getSelectItem()); // 设置当前显示格式
    }

    /**
     * 从缓存恢复M1553B配置状态，同步UI控件、下发SCPI命令、更新总线对象
     */
    void setCache() {
        int source = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + serialsNumber); // 读取缓存的信号源索引
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber); // 读取缓存的显示格式索引
        vSource.setSelectIndex(source); // 恢复信号源选择
        vDisplay.setSelectIndex(display); // 恢复显示格式选择

        Command.get().getTrigger_m1553B().setSource(serialsNumber - 1, source, false); // 下发触发信号源SCPI
        Command.get().getTrigger_m1553B().setDisplay(serialsNumber - 1, display, false); // 下发触发显示格式SCPI

        Command.get().getBus_1553B().Channel(serialsNumber-1,source,false); // 下发总线通道SCPI
        Command.get().getBus_1553B().Display(serialsNumber-1,display,false); // 下发总线显示格式SCPI

        m1553bBus.setSrcChIdx(source); // 更新总线对象信号源
        m1553bBus.setDisplayFormat(display); // 更新总线对象显示格式

//        if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//            SerialBusManage.getInstance().getSerialBus(IWave.S1).set1553bEncoding(display);
//        } else {
//            SerialBusManage.getInstance().getSerialBus(IWave.S2).set1553bEncoding(display);
//        }
        int ch= TChan.toSerialTChan(serialsNumber); // 转换为通道索引
        SerialBusManage.getInstance().getSerialBus(ch).set1553bEncoding(display); // 设置1553B编码方式

        msgDetailsM1553B.setSource(vSource.getSelectItem()); // 同步消息体信号源
        msgDetailsM1553B.setDisplay(vDisplay.getSelectItem()); // 同步消息体显示格式
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.M1553B) { // 当前协议为M1553B时
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
     * 设置串口通道编号，并获取对应的MILSTD1553BBus对象
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
        m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B); // 获取M1553B总线对象
    }

    /**
     * 根据通道索引设置控件颜色
     *
     * @param chIdx 通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        vSource.setControlColorByChIdx(chIdx); // 设置信号源控件颜色
        vDisplay.setControlColorByChIdx(chIdx); // 设置显示格式控件颜色
    }

    /**
     * 获取M1553B详情消息体
     *
     * @return M1553B协议详情消息
     */
    public RightMsgSerialsM1553b getMsgDetailsM1553B() {
        return msgDetailsM1553B; // 返回M1553B详情消息
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
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsM1553B.this, isFromEventBus); // 通知父布局
        }
    }

    /**
     * 缓存加载事件消费者，收到缓存加载事件后恢复M1553B配置
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道不存在则返回
            setCache(); // 从缓存恢复配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsM1553B, true); // 标记M1553B缓存已加载
        }
    };

    /**
     * 命令回传事件消费者，处理外部SCPI命令回传的M1553B参数变更
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_TRIGGERM1553B_SOURCE: { // 触发信号源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && Integer.parseInt(params[1]) != vSource.getSelectIndex()) { // 值不同才更新
                        vSource.setSelectIndex(Integer.parseInt(params[1])); // 更新信号源选择
                        onCheckChanged(vSource.getId(), vSource.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERM1553B_DISPLAY: { // 触发显示格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && Integer.parseInt(params[1]) != vDisplay.getSelectIndex()) { // 值不同才更新
                        vDisplay.setSelectIndex(Integer.parseInt(params[1])); // 更新显示格式选择
                        onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_Bus_1553B_Channel:{ // 总线通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String sNum=params[0]; // 通道编号参数
                    int ch=Integer.parseInt(params[1]); // 信号源索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vSource.setSelectIndex(ch); // 更新信号源选择
                    onCheckChanged(vSource.getId(),vSource.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_1553B_Display:{ // 总线显示格式变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String sNum=params[0]; // 通道编号参数
                    int ch=Integer.parseInt(params[1]); // 显示格式索引参数
                    if (Integer.parseInt(params[0]) != serialsNumber - 1) return; // 非当前通道则返回
                    vDisplay.setSelectIndex(ch); // 更新显示格式选择
                    onCheckChanged(vDisplay.getId(),vDisplay.getSelectItem(),false); // 触发变更处理
                }break;
            }
        }
    };

    /**
     * EventBus总线参数观察者，监听M1553B总线参数变化并同步UI
     */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件数据
            if (m1553bBus == null) return; // 总线对象为空则返回
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 事件类型匹配
                    && m1553bBus.equals(eventBase.getData())) { // 总线对象匹配
                if (m1553bBus.getSrcChIdx() != vSource.getSelectIndex()) { // 信号源不一致
                    vSource.setSelectIndex(m1553bBus.getSrcChIdx()); // 同步信号源
                    onCheckChanged(vSource.getId(), vSource.getSelectItem(), true); // 触发变更
                }
                if (m1553bBus.getDisplayFormat() != vDisplay.getSelectIndex()) { // 显示格式不一致
                    vDisplay.setSelectIndex(m1553bBus.getDisplayFormat()); // 同步显示格式
                    onCheckChanged(vDisplay.getId(), vDisplay.getSelectItem(), true); // 触发变更
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
     * 通道选择变更处理，根据控件ID分别处理信号源/显示格式变更，
     * 更新总线对象、下发SCPI命令、保存缓存、发送消息通知
     *
     * @param viewId         变更的控件ID
     * @param item           选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vSource.getId()) { // 信号源变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                m1553bBus.setSrcChIdx(vSource.getSelectIndex()); // 更新总线信号源
            }
            Command.get().getTrigger_m1553B().setSource(serialsNumber - 1, item.getIndex(), false); // 下发触发信号源SCPI
            Command.get().getBus_1553B().Channel(serialsNumber-1,item.getIndex(),false); // 下发总线通道SCPI
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
            msgDetailsM1553B.setSource(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        } else if (viewId == vDisplay.getId()) { // 显示格式变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                m1553bBus.setDisplayFormat(vDisplay.getSelectIndex()); // 更新总线显示格式
            }
            Command.get().getTrigger_m1553B().setDisplay(serialsNumber - 1, item.getIndex(), false); // 下发触发显示格式SCPI
            Command.get().getBus_1553B().Display(serialsNumber-1,item.getIndex(),false); // 下发总线显示格式SCPI
            int preDigits; // 之前的进制位数
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber) == 0) { // 之前为二进制
                preDigits = IDigits.DIGITS_2; // 二进制位数
            } else { // 之前为十六进制
                preDigits = IDigits.DIGITS_16; // 十六进制位数
            }
            setTopSerialsM1553bData(preDigits, item.getIndex() == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16); // 转换触发条件数据进制
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber, String.valueOf(item.getIndex())); // 保存缓存
//            if (serialsNumber == RightMsgSerials.SERIALS_S1) {
//                SerialBusManage.getInstance().getSerialBus(IWave.S1).set1553bEncoding(item.getIndex());
//            } else {
//                SerialBusManage.getInstance().getSerialBus(IWave.S2).set1553bEncoding(item.getIndex());
//            }
            int ch=TChan.toSerialTChan(serialsNumber); // 转换为通道索引
            SerialBusManage.getInstance().getSerialBus(ch).set1553bEncoding(item.getIndex()); // 设置1553B编码方式

            msgDetailsM1553B.setDisplay(item); // 更新消息体
            sendMsg(isFromEventBus); // 通知父布局
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲
    }

    /**
     * 切换M1553B触发条件数据的进制表示，同时更新缓存
     *
     * @param preDigits   之前的进制位数
     * @param nextDigits  目标进制位数
     */
    private void setTopSerialsM1553bData(int preDigits, int nextDigits) {
        String key; // 缓存键
        for (int i = 0; i < 3; i++) { // 遍历三种触发条件数据
            if (i == 0) { // 第一种：RT地址
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + serialsNumber; // RT地址缓存键
            } else if (i == 1) { // 第二种：命令字
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + serialsNumber; // 命令字缓存键
            } else { // 第三种：数据字
                key = CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + serialsNumber; // 数据字缓存键
            }
            String edit = CacheUtil.get().getString(key); // 从缓存读取数据
            edit = SerialsUtils.HexBin(edit, preDigits, nextDigits); // 进制转换
            edit = SerialsUtils.reCalcSpace(edit, nextDigits == IDigits.DIGITS_16 ? 4 : 16, nextDigits); // 重算空格间距
            CacheUtil.get().putMap(key, edit); // 更新缓存
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
