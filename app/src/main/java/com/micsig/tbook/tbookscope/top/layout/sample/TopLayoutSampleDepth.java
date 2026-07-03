// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopLayoutSampleDepth.java
//  核心职责：记录长度子页面Fragment，管理存储深度选择和硬件指令收发
//  架构设计：Fragment + RxBus观察者模式 + EventFactory事件观察者
//  数据流向：用户选择深度 → Command发送硬件指令 → EventUIObserver接收深度变更 → 刷新UI
//  依赖关系：依赖TopViewRadioGroup单选组、MemDepthFactory存储深度工厂、Command硬件指令
//  使用场景：采样功能记录长度子页面，用户选择存储深度（如Auto/7K/70K/700K等）
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者
import com.micsig.tbook.scope.Sample.IMemDepth; // 导入存储深度接口
import com.micsig.tbook.scope.Sample.MemDepthFactory; // 导入存储深度工厂
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令工厂
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.util.App; // 导入应用工具
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组

import java.util.Arrays; // 导入数组工具
import java.util.List; // 导入列表接口

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 记录长度Fragment - 管理存储深度选择和硬件指令收发
 */
public class TopLayoutSampleDepth extends Fragment { // 继承Fragment，记录长度子页面
    private Context context; // 上下文对象
    /** 存储深度单选组 */
    private TopViewRadioGroup viewDepth; // 存储深度选择控件

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_sample_depth, container, false); // 填充记录长度布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        viewDepth = (TopViewRadioGroup) view.findViewById(R.id.depth); // 获取存储深度单选组
        viewDepth.setData(R.string.sampleDepth, R.array.sampleDepth, onCheckChangedListener); // 设置深度选项数据
    }

    /**
     * 初始化数据（空实现）
     */
    private void initData() { // 初始化数据（空实现）
    }

    /**
     * 初始化RxBus事件订阅和EventFactory观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        EventFactory.addEventObserver(EventFactory.EVENT_MEM_DEPTH, eventUIObserver); // 注册存储深度事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_FORCE_MEM_DEPTH, eventUIObserver); // 注册强制存储深度事件观察者
    }

    /**
     * 从缓存恢复存储深度状态
     */
    private void setCache() { // 从缓存恢复状态
        refreshMemDepth(false); // 刷新存储深度（非EventBus来源）
    }

    /**
     * 缓存加载消费者 - 恢复存储深度状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleDepth, true); // 标记记录长度页面缓存加载完成
        }
    };

    /**
     * 命令到UI消费者 - 处理硬件返回的记录长度命令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_USERSET_LENGTH: // 记录长度命令
                    if (viewDepth.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果与当前选择不同
                        viewDepth.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新选中索引
                        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(viewDepth.getSelected().getIndex())); // 保存到缓存
                        MemDepthFactory.getMemDepth().setMemDepthItem(viewDepth.getSelected().getIndex()); // 更新存储深度项
                    }
                    break;
            }
        }
    };

    /**
     * 存储深度单选组变更监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 深度变更监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中回调
            Command.get().getUserset().setLength(item.getIndex(), false); // 发送用户设置记录长度命令
            Command.get().getSample().Mdepth(item.getIndex(),false); // 发送采样记录长度命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(item.getIndex())); // 保存到缓存
            MemDepthFactory.getMemDepth().setMemDepthItem(item.getIndex()); // 更新存储深度项
            refreshMemDepth(true); // 刷新存储深度（EventBus来源）

        }
    };

    /**
     * 事件观察者 - 监听存储深度变更事件
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件观察者
        @Override
        public void update(Object data) { // 事件更新回调
            if (((EventBase) data).getId() == EventFactory.EVENT_MEM_DEPTH // 如果是存储深度事件
                    || ((EventBase) data).getId() == EventFactory.EVENT_FORCE_MEM_DEPTH) { // 或强制存储深度事件
                refreshMemDepth(true); // 刷新存储深度（EventBus来源）
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(MemDepthFactory.getMemDepth().getMemDepthItem())); // 保存到缓存
            }
        }
    };

    /**
     * 刷新存储深度选项和选中状态
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void refreshMemDepth(boolean isFromEventBus) { // 刷新存储深度
        IMemDepth memDepth = MemDepthFactory.getMemDepth(); // 获取存储深度对象
        List<String> memDepthList = memDepth.getMemDepthItemName(); // 获取深度名称列表
        String[] memDepths = new String[memDepthList.size()]; // 创建名称数组
        memDepthList.toArray(memDepths); // 列表转数组
        if (!Arrays.equals(memDepths, viewDepth.getArray())) { // 如果深度列表有变化
            if (memDepths[0].equalsIgnoreCase("Auto")) { // 如果第一项是Auto
                String[] strings = context.getResources().getStringArray(R.array.sampleDepth); // 获取资源中的深度数组
                memDepths[0] = strings[0]; // 替换为本地化的Auto文本
            }
            viewDepth.setData(App.get().getString(R.string.sampleDepth), memDepths, onCheckChangedListener); // 更新深度选项数据
        }
        int depth; // 深度索引
        if (!isFromEventBus) { // 如果非EventBus来源（从缓存读取）
            depth = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH); // 从缓存读取深度索引
        } else { // 如果来自EventBus（从硬件读取）
            depth = memDepth.getMemDepthItem(); // 从硬件获取深度索引
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth)); // 保存到缓存
        }
        if (depth >= memDepthList.size()) { // 如果深度索引超出范围
            depth = 0; // 重置为0
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_DEPTH, String.valueOf(depth)); // 保存到缓存
        }
        viewDepth.setSelectedIndex(depth); // 设置选中索引
        if (!isFromEventBus) { // 如果非EventBus来源
            if (memDepth.getMemDepthItem() != viewDepth.getSelected().getIndex()) { // 如果硬件深度与UI不同
                memDepth.setMemDepthItem(viewDepth.getSelected().getIndex()); // 同步硬件深度
            }
        }
        Command.get().getUserset().setLength(viewDepth.getSelected().getIndex(), false); // 发送用户设置记录长度命令
        Command.get().getSample().Mdepth(viewDepth.getSelected().getIndex(),false); // 发送采样记录长度命令

        if (CacheUtil.get().isLoadParamComplete()) { // 如果参数加载完成
            TopMsgSampleDepth msgSampleDepth = new TopMsgSampleDepth(isFromEventBus); // 创建深度消息
            msgSampleDepth.setDepth(viewDepth.getSelected().getIndex()); // 设置深度索引
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLEDEPTH, msgSampleDepth); // 通过RxBus发送深度消息
        }
    }
}
