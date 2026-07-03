package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Sample.IMemDepth; // 导入存储深度接口
import com.micsig.tbook.scope.Sample.MemDepthFactory; // 导入存储深度工厂
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.util.App; // 导入应用全局实例
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组

import java.util.Arrays; // 导入数组工具
import java.util.List; // 导入列表接口

import io.reactivex.rxjava3.annotations.NonNull; // 导入NonNull注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 存储深度 → Fragment页面                               │
 * │ 核心职责：管理示波器存储深度的选择和同步                                    │
 * │ 架构设计：Fragment子类，通过RxBus和EventBus双通道监听变化                  │
 * │ 数据流向：CacheUtil/MemDepthFactory → 本类UI → Command → FPGA             │
 * │ 依赖关系：MemDepthFactory、Command、CacheUtil、RxBus、EventFactory         │
 * │ 使用场景：用户设置界面中存储深度选择子页面                                  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 存储深度设置Fragment，管理示波器采样存储深度的选择。
 * <p>
 * 存储深度选项由MemDepthFactory动态提供，支持：
 * <ul>
 *   <li>缓存加载时恢复设置</li>
 *   <li>SCPI命令远程修改</li>
 *   <li>EventBus事件同步（如采样模式变化导致深度选项变化）</li>
 * </ul>
 *
 * @author Administrator
 * @since 2017/4/11
 */
public class TopLayoutUsersetDepth extends Fragment {
    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 存储深度单选组 */
    private TopViewRadioGroup viewDepth; // 存储深度单选组

    /**
     * 创建Fragment视图，加载布局文件。
     *
     * @param inflater           布局填充器
     * @param container          父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 创建的视图
     */
    @Nullable // 可空返回值注解
    @Override // 覆写创建视图方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_usersetdepth, container, false); // 加载存储深度布局
    }

    /**
     * 视图创建完成后的初始化。
     *
     * @param view               创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写视图创建完成方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化控制逻辑
    }

    /**
     * 初始化视图控件，设置存储深度单选组数据。
     *
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        viewDepth = (TopViewRadioGroup) view.findViewById(R.id.depth); // 查找存储深度单选组
        viewDepth.setData(R.string.usersetDepth, R.array.usersetDepth, onCheckChangedListener); // 设置深度数据
    }

    /**
     * 初始化数据（当前为空实现）。
     */
    private void initData() { // 初始化数据
    }

    /**
     * 初始化控制逻辑，订阅RxBus和EventBus事件。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令消息
        EventFactory.addEventObserver(EventFactory.EVENT_MEM_DEPTH, eventUIObserver); // 订阅存储深度变化事件
        EventFactory.addEventObserver(EventFactory.EVENT_FORCE_MEM_DEPTH, eventUIObserver); // 订阅强制存储深度变化事件
    }

    /**
     * 从缓存恢复存储深度设置。
     */
    private void setCache() { // 从缓存恢复设置
        refreshMemDepth(false); // 刷新存储深度（非EventBus来源）
    }

    /** 缓存加载事件消费者 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override // 覆写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复设置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutUsersetDepth, true); // 标记本模块缓存已加载
        }
    };

    /** 命令消息到UI消费者 */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令消息到UI消费者
        @Override // 覆写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_USERSET_LENGTH: // 存储深度命令
                    if (viewDepth.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 索引不同才更新
                        viewDepth.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 设置选中项
                        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(viewDepth.getSelected().getIndex())); // 缓存深度索引
                        MemDepthFactory.getMemDepth().setMemDepthItem(viewDepth.getSelected().getIndex()); // 更新存储深度项
                    }
                    break; // 结束分支
            }
        }
    };

    /** 单选组选中变化监听器 */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组选中变化监听器
        @Override // 覆写音效回调
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override // 覆写拦截提示回调
        public void onPrompt(TopViewRadioGroup view) { // 拦截提示回调（未使用）

        }

        @Override // 覆写点击回调
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变化回调
            Command.get().getUserset().setLength(item.getIndex(), false); // 发送存储深度设置命令
            Command.get().getSample().Mdepth(item.getIndex(),false); // 发送采样深度命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(item.getIndex())); // 缓存深度索引
            MemDepthFactory.getMemDepth().setMemDepthItem(item.getIndex()); // 更新存储深度项
        }
    };

    /** 事件UI观察者，监听存储深度变化事件 */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者
        @Override // 覆写update方法
        public void update(Object data) { // 接收事件数据
            if (((EventBase) data).getId() == EventFactory.EVENT_MEM_DEPTH // 存储深度变化事件
                    || ((EventBase) data).getId() == EventFactory.EVENT_FORCE_MEM_DEPTH) { // 或强制存储深度变化事件
                refreshMemDepth(true); // 刷新存储深度（EventBus来源）
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(MemDepthFactory.getMemDepth().getMemDepthItem())); // 缓存当前深度
            }
        }
    };

    /**
     * 刷新存储深度选项和选中状态。
     * <p>
     * 根据MemDepthFactory动态更新深度选项列表，并根据来源（缓存/EventBus）确定选中项。
     *
     * @param isFromEventBus true=来自EventBus事件，false=来自缓存加载
     */
    private void refreshMemDepth(boolean isFromEventBus) { // 刷新存储深度
        IMemDepth memDepth = MemDepthFactory.getMemDepth(); // 获取存储深度实例
        List<String> memDepthList = memDepth.getMemDepthItemName(); // 获取深度选项名称列表
        String[] memDepths = new String[memDepthList.size()]; // 创建数组
        memDepthList.toArray(memDepths); // 列表转数组
        if (!Arrays.equals(memDepths, viewDepth.getArray())) { // 深度选项有变化
            viewDepth.setData(App.get().getString(R.string.usersetDepth), memDepths, onCheckChangedListener); // 更新深度选项数据
        }
        int depth; // 深度索引
        if (!isFromEventBus) { // 非EventBus来源
            depth = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH); // 从缓存读取深度
        } else { // EventBus来源
            depth = memDepth.getMemDepthItem(); // 从MemDepth获取当前深度
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth)); // 缓存深度
        }
        if (depth >= memDepthList.size()) { // 深度索引超出范围
            depth = 0; // 重置为0
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth)); // 缓存重置后的深度
        }
        viewDepth.setSelectedIndex(depth); // 设置选中项



        if (!isFromEventBus) { // 非EventBus来源
            if (memDepth.getMemDepthItem() != viewDepth.getSelected().getIndex()) { // MemDepth与UI不一致
                memDepth.setMemDepthItem(viewDepth.getSelected().getIndex()); // 同步MemDepth
            }
        }
        Command.get().getUserset().setLength(viewDepth.getSelected().getIndex(), false); // 发送深度设置命令
        Command.get().getSample().Mdepth(viewDepth.getSelected().getIndex(),false); // 发送采样深度命令
    }
}
