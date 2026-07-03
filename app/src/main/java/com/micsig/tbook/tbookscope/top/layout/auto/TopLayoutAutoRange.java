package com.micsig.tbook.tbookscope.top.layout.auto; // 自动量程布局Fragment所在包

import android.content.Context; // 导入上下文类，用于访问系统服务
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Auto.Auto; // 导入自动功能核心控制类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，用于查询通道状态
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧面板其他消息类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，用于发送硬件指令
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令UI消息类，用于接收硬件回调
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入响应式消息总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入消息枚举定义
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.MSwitchBox; // 导入自定义开关控件

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动量程(Auto Range)详情页面Fragment                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供自动量程界面的交互逻辑，包括量程/垂直/水平/电平四个开关控制       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：Fragment子页面，嵌入TopLayoutAuto中，采用观察者模式响应缓存和命令   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：UI控件 → TopMsgAutoRange → OnDetailSendMsgListener → TopLayoutAuto│
 * │           RxBus(缓存/命令/右侧面板) → Consumer → UI更新 → Command → 硬件    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：TopMsgAutoRange(数据模型)、Command(指令)、Auto(控制)、CacheUtil    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部面板选择"自动量程"时展示此页面，配置量程自动参数           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAutoRange extends Fragment { // 自动量程详情页面Fragment
    private Context context; // 上下文对象，用于访问系统服务和资源
    private MSwitchBox rgRange, rgVertical, rgHorizontal, rgLevel; // 量程/垂直/水平/电平四个开关控件

    private TopMsgAutoRange autoDetail; // 自动量程详情数据模型
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

    /**
     * 创建Fragment视图，加载布局文件
     *
     * @param inflater           布局填充器
     * @param container          父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标注返回值可为空
    @Override // 覆写Fragment的onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_autorange, container, false); // 加载自动量程布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     *
     * @param view               Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写Fragment的onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图控件
        initData(); // 初始化数据模型
        initControl(); // 初始化事件监听和消息订阅
    }

    /**
     * 初始化数据模型，将UI控件当前状态同步到数据模型
     */
    private void initData() { // 初始化数据
        autoDetail = new TopMsgAutoRange(); // 创建自动量程详情数据模型
        autoDetail.setRange(rgRange.isState()); // 同步量程开关状态到数据模型
        autoDetail.setVertical(rgVertical.isState()); // 同步垂直方向开关状态到数据模型
        autoDetail.setHorizontal(rgHorizontal.isState()); // 同步水平方向开关状态到数据模型
        autoDetail.setLevel(rgLevel.isState()); // 同步电平开关状态到数据模型
    }

    /**
     * 初始化视图控件，绑定布局中的UI元素和事件监听器
     *
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图控件
        rgRange = (MSwitchBox) view.findViewById(R.id.rangeDetail); // 查找量程开关控件
        rgRange.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置量程开关监听器
        rgVertical = (MSwitchBox) view.findViewById(R.id.verticalDetail); // 查找垂直方向开关控件
        rgVertical.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置垂直方向开关监听器
        rgHorizontal = (MSwitchBox) view.findViewById(R.id.horizontalDetail); // 查找水平方向开关控件
        rgHorizontal.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置水平方向开关监听器
        rgLevel = (MSwitchBox) view.findViewById(R.id.levelDetail); // 查找电平开关控件
        rgLevel.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置电平开关监听器
    }

    /**
     * 初始化消息订阅，监听缓存加载、右侧面板和硬件命令回调
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧面板其他消息
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅硬件命令回调消息
    }

    /**
     * 从缓存恢复UI状态，同步到硬件和数据模型
     * 根据设备是否支持自动量程功能决定UI可用性
     */
    private void setCache() { // 从缓存恢复状态
        if (Tools.isEnableAutoRange()) { // 设备支持自动量程功能
            int range = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE); // 读取量程开关缓存值（0=开启）
            int vertical = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL); // 读取垂直方向缓存值
            int horizontal = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL); // 读取水平方向缓存值
            int level = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL); // 读取电平缓存值

            this.rgRange.setState(range == 0); // 设置量程开关状态（0=开启）
            this.rgVertical.setState(vertical == 0); // 设置垂直方向开关状态
            this.rgHorizontal.setState(horizontal == 0); // 设置水平方向开关状态
            this.rgLevel.setState(level == 0); // 设置电平开关状态

            rgVertical.setEnabled(range == 0); // 量程开启时垂直方向才可用
            rgHorizontal.setEnabled(range == 0); // 量程开启时水平方向才可用
            rgLevel.setEnabled(range == 0); // 量程开启时电平才可用

            Command.get().getAuto().range(range == 0, false); // 通过命令中间件设置量程状态（不发送到硬件）
            Command.get().getAuto().rangeVertical(vertical == 0, false); // 通过命令中间件设置垂直方向状态
            Command.get().getAuto().rangeHorizoncal(horizontal == 0, false); // 通过命令中间件设置水平方向状态
            Command.get().getAuto().rangeLevel(level == 0, false); // 通过命令中间件设置电平状态

            Auto.getInstance().setAutoRangeEnable(range == 0); // 设置自动量程使能
            Auto.getInstance().setAutoVerticalEnable(vertical == 0); // 设置自动垂直方向使能
            Auto.getInstance().setAutoHorizontalEnable(horizontal == 0); // 设置自动水平方向使能
            Auto.getInstance().setAutoLevleEnable(level == 0); // 设置自动电平使能

            autoDetail.setRange(this.rgRange.isState()); // 同步量程状态到数据模型
            autoDetail.setVertical(this.rgVertical.isState()); // 同步垂直方向状态到数据模型
            autoDetail.setHorizontal(this.rgHorizontal.isState()); // 同步水平方向状态到数据模型
            autoDetail.setLevel(this.rgLevel.isState()); // 同步电平状态到数据模型
            sendMsg(false); // 发送消息通知父级（非EventBus来源）
        } else { // 设备不支持自动量程功能
            rgRange.setState(false); // 关闭量程开关
            rgRange.setEnabled(false); // 禁用量程开关
            rgVertical.setEnabled(false); // 禁用垂直方向开关
            rgHorizontal.setEnabled(false); // 禁用水平方向开关
            rgLevel.setEnabled(false); // 禁用电平开关
        }
    }

    /**
     * 获取自动量程详情数据模型
     *
     * @return 自动量程详情数据模型实例
     */
    public TopMsgAutoRange getAutoDetail() { // 获取自动量程详情数据模型
        return autoDetail; // 返回数据模型
    }

    /**
     * 设置详情消息发送监听器
     *
     * @param onDetailSendMsgListener 详情消息发送监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器引用
    }

    /**
     * 通过监听器发送消息通知父级页面
     *
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息通知父级
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 缓存加载消息消费者，当收到缓存加载事件时恢复UI状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载消息
            setCache(); // 从缓存恢复UI状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAutoRange, true); // 标记本页面缓存已加载完成
        }
    };

    /**
     * 右侧面板其他消息消费者，处理通道开关状态变化对自动量程的影响
     * 当串口通道关闭时恢复自动量程功能，串口通道开启时禁用自动量程
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 右侧面板消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 处理右侧面板消息
            boolean isSerials = ChannelFactory.isChOpen(ChannelFactory.S1) // 检查串口通道S1是否开启
                    || ChannelFactory.isChOpen(ChannelFactory.S2) // 检查串口通道S2是否开启
                    || ChannelFactory.isChOpen(ChannelFactory.S3) // 检查串口通道S3是否开启
                    || ChannelFactory.isChOpen(ChannelFactory.S4); // 检查串口通道S4是否开启
            if (!isSerials) { // 无串口通道开启
                rgRange.setEnabled(true); // 启用量程开关
                rgVertical.setEnabled(true); // 启用垂直方向开关
                rgHorizontal.setEnabled(true); // 启用水平方向开关
                rgLevel.setEnabled(true); // 启用电平开关
                if (Tools.isEnableAutoRange()) { // 设备支持自动量程
                    int range = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE); // 读取量程缓存值
                    int vertical = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL); // 读取垂直方向缓存值
                    int horizontal = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL); // 读取水平方向缓存值
                    int level = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL); // 读取电平缓存值

                    rgRange.setState(range == 0); // 设置量程开关状态
                    rgVertical.setState(vertical == 0); // 设置垂直方向开关状态
                    rgHorizontal.setState(horizontal == 0); // 设置水平方向开关状态
                    rgLevel.setState(level == 0); // 设置电平开关状态

                    rgVertical.setEnabled(range == 0); // 量程开启时垂直方向才可用
                    rgHorizontal.setEnabled(range == 0); // 量程开启时水平方向才可用
                    rgLevel.setEnabled(range == 0); // 量程开启时电平才可用

                    Command.get().getAuto().range(range == 0, false); // 通过命令中间件设置量程状态
                    Command.get().getAuto().rangeVertical(vertical == 0, false); // 通过命令中间件设置垂直方向状态
                    Command.get().getAuto().rangeHorizoncal(horizontal == 0, false); // 通过命令中间件设置水平方向状态
                    Command.get().getAuto().rangeLevel(level == 0, false); // 通过命令中间件设置电平状态

                    Auto.getInstance().setAutoRangeEnable(range == 0); // 设置自动量程使能
                    Auto.getInstance().setAutoVerticalEnable(vertical == 0); // 设置自动垂直方向使能
                    Auto.getInstance().setAutoHorizontalEnable(horizontal == 0); // 设置自动水平方向使能
                    Auto.getInstance().setAutoLevleEnable(level == 0); // 设置自动电平使能

                    autoDetail.setRange(rgRange.isState()); // 同步量程状态到数据模型
                    autoDetail.setVertical(rgVertical.isState()); // 同步垂直方向状态到数据模型
                    autoDetail.setHorizontal(rgHorizontal.isState()); // 同步水平方向状态到数据模型
                    autoDetail.setLevel(rgLevel.isState()); // 同步电平状态到数据模型
                    sendMsg(false); // 发送消息通知父级
                }
            } else { // 有串口通道开启
                rgRange.setState(false); // 关闭量程开关
                rgRange.setEnabled(false); // 禁用量程开关
                rgVertical.setEnabled(false); // 禁用垂直方向开关
                rgHorizontal.setEnabled(false); // 禁用水平方向开关
                rgLevel.setEnabled(false); // 禁用电平开关
            }
        }
    };

    /**
     * 硬件命令回调消息消费者，处理来自硬件的自动量程参数变更通知
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令回调消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 处理命令回调消息
            if (Tools.isEnableAutoRange()) { // 设备支持自动量程
                switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                    case CommandMsgToUI.FLAG_AUTO_RANGE: // 量程开关变更命令
                        int range = Integer.parseInt(commandMsgToUI.getParam()); // 解析量程参数
                        if (rgRange.isState() != (range == 0)) { // 当前UI状态与命令不一致
                            rgRange.setState(range == 0); // 更新量程开关状态
                            onCheckChanged(rgRange, rgRange.isState(), false); // 触发开关变化回调
                        }
                        break; // 结束量程处理
                    case CommandMsgToUI.FLAG_AUTO_RANGEVERTICAL: // 垂直方向开关变更命令
                        int rangeVer = Integer.parseInt(commandMsgToUI.getParam()); // 解析垂直方向参数
                        if (rgVertical.isState() != (rangeVer == 0)) { // 当前UI状态与命令不一致
                            rgVertical.setState(rangeVer == 0); // 更新垂直方向开关状态
                            onCheckChanged(rgVertical, rgVertical.isState(), false); // 触发开关变化回调
                        }
                        break; // 结束垂直方向处理
                    case CommandMsgToUI.FLAG_AUTO_RANGEHORIZONTAL: // 水平方向开关变更命令
                        int rangeHor = Integer.parseInt(commandMsgToUI.getParam()); // 解析水平方向参数
                        if (rgHorizontal.isState() != (rangeHor == 0)) { // 当前UI状态与命令不一致
                            rgHorizontal.setState(rangeHor == 0); // 更新水平方向开关状态
                            onCheckChanged(rgHorizontal, rgHorizontal.isState(), false); // 触发开关变化回调
                        }
                        break; // 结束水平方向处理
                    case CommandMsgToUI.FLAG_AUTO_RANGELEVEL: // 电平开关变更命令
                        int rangeLevel = Integer.parseInt(commandMsgToUI.getParam()); // 解析电平参数
                        if (rgLevel.isState() != (rangeLevel == 0)) { // 当前UI状态与命令不一致
                            rgLevel.setState(rangeLevel == 0); // 更新电平开关状态
                            onCheckChanged(rgLevel, rgLevel.isState(), false); // 触发开关变化回调
                        }
                        break; // 结束电平处理
                }
            }
        }
    };

    /**
     * 开关状态变化监听器，统一处理四个开关的状态变化
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() { // 开关状态变化监听器
        @Override // 覆写onToggleStateChanged方法
        public void onToggleStateChanged(MSwitchBox view, boolean state) { // 开关状态变化回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            onCheckChanged(view, state, false); // 委托给onCheckChanged处理
        }
    };

    /**
     * 开关状态变化处理，根据不同开关ID执行对应的业务逻辑
     *
     * @param view           触发变化的开关控件
     * @param state          新的开关状态
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(MSwitchBox view, boolean state, boolean isFromEventBus) { // 开关状态变化处理
        if (Tools.isEnableAutoRange()) { // 设备支持自动量程
            if (view.getId() == R.id.rangeDetail) { // 量程开关变化
                Command.get().getAuto().range(state, false); // 通过命令中间件设置量程状态
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_RANGE, String.valueOf(state ? 0 : 1)); // 更新量程缓存值
                if (!isFromEventBus) { // 非EventBus来源
                    Auto.getInstance().setAutoRangeEnable(state); // 设置自动量程使能
                }
                rgVertical.setEnabled(state); // 量程开启时垂直方向才可用
                rgHorizontal.setEnabled(state); // 量程开启时水平方向才可用
                rgLevel.setEnabled(state); // 量程开启时电平才可用
                autoDetail.setRange(state); // 更新数据模型中的量程状态
                sendMsg(isFromEventBus); // 发送消息通知父级
                RxBus.getInstance().post(RxEnum.TOPLAYOUT_AUTO_CHANGED,state); // 广播自动量程变化消息
            } else if (view.getId() == R.id.verticalDetail) { // 垂直方向开关变化
                Command.get().getAuto().rangeVertical(state, false); // 通过命令中间件设置垂直方向状态
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_VERTICAL, String.valueOf(state ? 0 : 1)); // 更新垂直方向缓存值
                if (!isFromEventBus) { // 非EventBus来源
                    Auto.getInstance().setAutoVerticalEnable(state); // 设置自动垂直方向使能
                }
                autoDetail.setVertical(state); // 更新数据模型中的垂直方向状态
                sendMsg(isFromEventBus); // 发送消息通知父级
            } else if (view.getId() == R.id.horizontalDetail) { // 水平方向开关变化
                Command.get().getAuto().rangeHorizoncal(state, false); // 通过命令中间件设置水平方向状态
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_HORIZONTAL, String.valueOf(state ? 0 : 1)); // 更新水平方向缓存值
                if (!isFromEventBus) { // 非EventBus来源
                    Auto.getInstance().setAutoHorizontalEnable(state); // 设置自动水平方向使能
                }
                autoDetail.setHorizontal(state); // 更新数据模型中的水平方向状态
                sendMsg(isFromEventBus); // 发送消息通知父级
            } else if (view.getId() == R.id.levelDetail) { // 电平开关变化
                Command.get().getAuto().rangeLevel(state, false); // 通过命令中间件设置电平状态
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_RANGE_LEVEL, String.valueOf(state ? 0 : 1)); // 更新电平缓存值
                if (!isFromEventBus) { // 非EventBus来源
                    Auto.getInstance().setAutoLevleEnable(state); // 设置自动电平使能
                }
                autoDetail.setLevel(state); // 更新数据模型中的电平状态
                sendMsg(isFromEventBus); // 发送消息通知父级
            }
        }
    }
}
