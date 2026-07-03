package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.micsig.tbook.scope.Bus.I2CBus;
import com.micsig.tbook.scope.Bus.IBus;
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
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/*
 * +--------------------------------------------------------------------------+
 * |                       I2C串口协议配置布局                                  |
 * +--------------------------------------------------------------------------+
 * | 模块定位: rightslipmenu.serials 子包 —— I2C协议参数配置的UI布局           |
 * | 核心职责: 提供I2C总线的SDA/SCL通道选择界面，处理用户交互和外部事件，      |
 * |          通过Command下发SCPI指令并通过OnSerialsDetailSendMsgListener       |
 * |          通知父布局发送消息                                               |
 * | 架构设计: RelativeLayout子类，包含SDA/SCL两个通道选择控件，              |
 * |          采用观察者模式监听RxBus和EventBus事件                           |
 * | 数据流向: 用户UI → RightLayoutSerialsI2c → Command(SCPI) → 硬件         |
 * |          → OnSerialsDetailSendMsgListener → RightLayoutSerials → RxBus  |
 * | 依赖关系: RightViewSelect, RightMsgSerialsI2c, I2CBus, Command,         |
 * |          ChannelFactory, CacheUtil, RxBus                                |
 * | 使用场景: 用户在右侧菜单选择I2C协议后，此布局显示SDA/SCL通道选择         |
 * +--------------------------------------------------------------------------+
 */

/**
 * Created by yangj on 2017/5/4.
 */

/**
 * I2C串口协议配置布局
 * <p>
 * 提供I2C总线的SDA数据线和SCL时钟线通道选择界面，
 * 当SDA和SCL选择同一通道时自动调整另一个通道选择，
 * 避免冲突。支持从缓存恢复状态和响应外部SCPI命令。
 * </p>
 */
public class RightLayoutSerialsI2c extends RelativeLayout {
    /** 上下文引用 */
    private Context context; // 上下文引用
    /** SDA数据线通道选择控件 / SCL时钟线通道选择控件 */
    private RightViewSelect vSda, vScl; // SDA/SCL通道选择控件

    /** I2C协议详情消息体 */
    private RightMsgSerialsI2c msgDetailsI2c; // I2C协议详情消息体
    /** 详情变更发送消息监听器 */
    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener; // 详情变更监听器
    /** 串口通道编号 */
    private int serialsNumber; // 串口通道编号

    /** I2C总线数据对象 */
    private I2CBus i2cBus; // I2C总线数据对象

    /**
     * 单参数构造函数
     */
    public RightLayoutSerialsI2c(Context context) {
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     */
    public RightLayoutSerialsI2c(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数，初始化视图和控件
     */
    public RightLayoutSerialsI2c(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
        initControl(); // 初始化控件
    }

    /**
     * 初始化视图控件，加载布局并绑定事件监听器
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_serials_i2c, this);
        vSda = (RightViewSelect) findViewById(R.id.sda);
        vScl = (RightViewSelect) findViewById(R.id.scl);

        String[] channels = GlobalVar.get().getChannelsName();
        vSda.setArray(channels);
        vScl.setArray(channels);

        vSda.setOnItemClickListener(onItemClickListener);
        vScl.setOnItemClickListener(onItemClickListener);

        initData();
    }

    /** 初始化默认数据 */
    private void initData() {
        msgDetailsI2c = new RightMsgSerialsI2c();
        msgDetailsI2c.setSda(vSda.getSelectItem());
        vScl.setSelectIndex(1);
        msgDetailsI2c.setScl(vScl.getSelectItem());
    }

    /** 从缓存恢复I2C配置状态 */
    void setCache() {
        int sda = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber);
        int scl = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber);
        vSda.setSelectIndex(sda);
        vScl.setSelectIndex(scl);

        Command.get().getTrigger_iic().setSource(serialsNumber - 1, sda, false);
        Command.get().getTrigger_iic().setClock(serialsNumber - 1, scl, false);

        Command.get().getBus_iic().SDA(serialsNumber-1,sda,false);
        Command.get().getBus_iic().SCL(serialsNumber-1,scl,false);

        i2cBus.setSdaChIdx(sda);
        i2cBus.setSclChIdx(scl);

        msgDetailsI2c.setSda(vSda.getSelectItem());
        msgDetailsI2c.setScl(vScl.getSelectItem());
        if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) == CacheUtil.I2C) {
            sendMsg(false);
        }
    }

    /** 初始化控件，订阅RxBus和EventBus事件 */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        EventFactory.addEventObserver(EventFactory.EVENT_BUS_PARAM, eventBusParam);
    }

    /**
     * 设置串口通道编号，并获取对应的I2CBus对象
     *
     * @param serialsNumber 串口编号(1~4)
     */
    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
        int tChan= TChan.toSerialTChan(serialsNumber);
        int fpgaChan=TChan.toFpgaChNo(tChan);
        setControlColorByChIdx(tChan);
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaChan);
        if (serialChannel == null) return;
        i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C);
    }
    /**
     * 根据通道索引设置控件颜色
     */
    private void setControlColorByChIdx(int chIdx) {
        vSda.setControlColorByChIdx(chIdx);
        vScl.setControlColorByChIdx(chIdx);
    }
    /**
     * 获取I2C详情消息体
     */
    public RightMsgSerialsI2c getMsgDetailsI2c() {
        return msgDetailsI2c;
    }

    /**
     * 设置详情变更发送消息监听器
     */
    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    /**
     * 通过监听器发送详情变更消息
     */
    private void sendMsg(boolean isFromEventBus) {
        if (onSerialsDetailSendMsgListener != null) {
            onSerialsDetailSendMsgListener.onClick(RightLayoutSerialsI2c.this, isFromEventBus);
        }
    }

    /**
     * 缓存加载事件消费者，收到缓存加载事件后恢复I2C配置
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(TChan.toSerialTChan(serialsNumber))); // 获取串口通道
            if (serialChannel == null) return; // 通道不存在则返回
            setCache(); // 从缓存恢复配置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutSerialsI2c, true); // 标记I2C缓存已加载
        }
    };

    /**
     * 命令回传事件消费者，处理外部SCPI命令回传的I2C参数变更
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_TRIGGERIIC_SOURCE: { // 触发SDA源变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && vSda.getSelectIndex() != Integer.parseInt(params[1])) { // 值不同才更新
                        vSda.setSelectIndex(Integer.parseInt(params[1])); // 更新SDA选择
                        onCheckChanged(vSda.getId(), vSda.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_TRIGGERIIC_CLOCK: { // 触发SCL时钟变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    if (Integer.parseInt(params[0]) == serialsNumber - 1 // 匹配通道编号
                            && vScl.getSelectIndex() != Integer.parseInt(params[1])) { // 值不同才更新
                        vScl.setSelectIndex(Integer.parseInt(params[1])); // 更新SCL选择
                        onCheckChanged(vScl.getId(), vScl.getSelectItem(), false); // 触发变更处理
                    }
                    break;
                }

                case CommandMsgToUI.FLAG_Bus_IIC_SDA:{ // 总线SDA变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    int ch=Integer.parseInt(params[1]); // SDA索引
                    if (sNum != serialsNumber - 1) return; // 非当前通道则返回
                    vSda.setSelectIndex(ch); // 更新SDA选择
                    onCheckChanged(vSda.getId(),vSda.getSelectItem(),false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_Bus_IIC_SCL:{ // 总线SCL变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int sNum=Integer.parseInt(params[0]); // 通道编号
                    int ch=Integer.parseInt(params[1]); // SCL索引
                    if (sNum != serialsNumber - 1) return; // 非当前通道则返回
                    vScl.setSelectIndex(ch); // 更新SCL选择
                    onCheckChanged(vScl.getId(),vScl.getSelectItem(),false); // 触发变更处理
                }break;
            }
        }
    };

    /**
     * EventBus总线参数观察者，监听I2C总线参数变化并同步UI
     */
    private EventUIObserver eventBusParam = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data; // 获取事件数据
            if (i2cBus == null) return; // 总线对象为空则返回
            if (eventBase.getId() == EventFactory.EVENT_BUS_PARAM // 事件类型匹配
                    && i2cBus.equals(eventBase.getData())) { // 总线对象匹配
                if (i2cBus.getSdaChIdx() != vSda.getSelectIndex()) { // SDA不一致
                    vSda.setSelectIndex(i2cBus.getSdaChIdx()); // 同步SDA
                    onCheckChanged(vSda.getId(), vSda.getSelectItem(), true); // 触发变更
                }
                if (i2cBus.getSclChIdx() != vScl.getSelectIndex()) { // SCL不一致
                    vScl.setSelectIndex(i2cBus.getSclChIdx()); // 同步SCL
                    onCheckChanged(vScl.getId(), vScl.getSelectItem(), true); // 触发变更
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
     * 通道选择变更处理，处理SDA/SCL冲突并下发SCPI命令
     *
     * @param viewId         变更的控件ID
     * @param item           选中的项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        if (viewId == vSda.getId()) { // SDA变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                i2cBus.setSdaChIdx(item.getIndex()); // 更新总线SDA
            }
            msgDetailsI2c.setSda(item); // 更新消息体SDA
            if (item.getIndex() == vScl.getSelectIndex()) { // SDA与SCL冲突
                int index = vScl.getSelectIndex(); // 获取当前SCL索引
                vScl.setSelectIndex(index == vScl.getSelectCount() - 1 ? 0 : index + 1); // 自动调整SCL到下一个索引
                if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                    i2cBus.setSclChIdx(vScl.getSelectIndex()); // 更新总线SCL
                }
                msgDetailsI2c.setScl(vScl.getSelectItem()); // 更新消息体SCL
                msgDetailsI2c.setSda(item); // 重新设置SDA以标记RxMsg选中
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber, String.valueOf(vSda.getSelectIndex())); // 保存SDA缓存
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber, String.valueOf(vScl.getSelectIndex())); // 保存SCL缓存
            sendMsg(isFromEventBus); // 通知父布局

            Command.get().getTrigger_iic().setSource(serialsNumber - 1, vSda.getSelectIndex(), false); // 下发触发SDA SCPI
            Command.get().getBus_iic().SDA(serialsNumber-1,vSda.getSelectIndex(),false); // 下发总线SDA SCPI
            Command.get().getTrigger_iic().setClock(serialsNumber - 1, vScl.getSelectIndex(), false); // 下发触发SCL SCPI
            Command.get().getBus_iic().SCL(serialsNumber-1,vScl.getSelectIndex(),false); // 下发总线SCL SCPI
        } else if (viewId == vScl.getId()) { // SCL变更
            if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                i2cBus.setSclChIdx(item.getIndex()); // 更新总线SCL
            }
            msgDetailsI2c.setScl(item); // 更新消息体SCL
            if (item.getIndex() == vSda.getSelectIndex()) { // SCL与SDA冲突
                int index = vSda.getSelectIndex(); // 获取当前SDA索引
                vSda.setSelectIndex(index == vSda.getSelectCount() - 1 ? 0 : index + 1); // 自动调整SDA到下一个索引
                if (!isFromEventBus) { // 非EventBus事件才更新总线对象
                    i2cBus.setSdaChIdx(vSda.getSelectIndex()); // 更新总线SDA
                }
                msgDetailsI2c.setSda(vSda.getSelectItem()); // 更新消息体SDA
                msgDetailsI2c.setScl(item); // 重新设置SCL以标记RxMsg选中
            }
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + serialsNumber, String.valueOf(vSda.getSelectIndex())); // 保存SDA缓存
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + serialsNumber, String.valueOf(vScl.getSelectIndex())); // 保存SCL缓存
            sendMsg(isFromEventBus); // 通知父布局

            Command.get().getTrigger_iic().setSource(serialsNumber - 1, vSda.getSelectIndex(), false); // 下发触发SDA SCPI
            Command.get().getBus_iic().SDA(serialsNumber-1,vSda.getSelectIndex(),false); // 下发总线SDA SCPI
            Command.get().getTrigger_iic().setClock(serialsNumber - 1, vScl.getSelectIndex(), false); // 下发触发SCL SCPI
            Command.get().getBus_iic().SCL(serialsNumber-1,vScl.getSelectIndex(),false); // 下发总线SCL SCPI
        }
        SerialBusManage.getInstance().clearSerialBusTxtBuffer(); // 清除串口总线文本缓冲
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
