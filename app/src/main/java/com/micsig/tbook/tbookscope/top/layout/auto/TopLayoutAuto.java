package com.micsig.tbook.tbookscope.top.layout.auto; // 自动功能容器布局Fragment所在包

import android.content.Context; // 导入上下文类，用于访问系统服务
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入响应式消息总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入消息枚举定义
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题选项数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入顶部标题视图组件
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动的顶部标题视图组件

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动功能(Auto)容器页面Fragment                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：管理自动设置和自动量程两个子页面的切换，汇总子页面消息并广播          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：容器Fragment，内嵌TopLayoutAutoSet和TopLayoutAutoRange子Fragment  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：子Fragment → OnDetailSendMsgListener → TopMsgAuto → RxBus广播    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：TopLayoutAutoSet/TopLayoutAutoRange(子页面)、TopMsgAuto(消息模型) │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部面板选择"Auto"时展示此容器页面，可切换自动设置/自动量程    │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAuto extends Fragment { // 自动功能容器页面Fragment
    public static final int DETAIL_SET = 0; // 自动设置子页面索引常量
    public static final int DETAIL_RANGE = 1; // 自动量程子页面索引常量

    private Context context; // 上下文对象，用于访问系统服务和资源
    private TopViewTitleWithScroll autoTitle; // 带滚动的自动功能标题视图，用于切换子页面
    private RelativeLayout autoDetail; // 自动功能详情容器布局，承载子Fragment
    private TopLayoutAutoSet layoutAutoSet;         //自动设置 // 自动设置子Fragment实例
    private TopLayoutAutoRange layoutAutoRange;     //自动量程 // 自动量程子Fragment实例

    private TopMsgAuto msgAuto; // 自动功能消息数据模型，用于RxBus广播

    private String[] tags = {"AutoSet", "AutoRange"}; // 子Fragment的Tag标识数组，用于Fragment事务查找
    private Fragment[] fragments = new Fragment[2]; // 子Fragment实例数组，用于状态恢复

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
        return inflater.inflate(R.layout.layout_auto, container, false); // 加载自动功能容器布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     *
     * @param view               Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写Fragment的onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view, savedInstanceState); // 初始化视图控件和子Fragment
        initControl(); // 初始化事件监听和消息订阅
    }

    /**
     * 初始化视图控件和子Fragment，处理Fragment状态恢复
     *
     * @param view               Fragment的根视图
     * @param savedInstanceState 保存的实例状态，非null时恢复已有Fragment
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图控件
        if (savedInstanceState != null) { // 有保存的状态，需要恢复Fragment
            for (int i = 0; i < tags.length; i++) { // 遍历所有Tag
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 通过Tag查找已存在的Fragment
            }
        }
        layoutAutoSet = fragments[0] == null ? new TopLayoutAutoSet() : (TopLayoutAutoSet) fragments[0]; // 恢复或创建自动设置Fragment
        layoutAutoRange = fragments[1] == null ? new TopLayoutAutoRange() : (TopLayoutAutoRange) fragments[1]; // 恢复或创建自动量程Fragment
        if (savedInstanceState == null) { // 首次创建，需要添加Fragment事务
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .add(R.id.autoDetail, layoutAutoSet, tags[0]) // 添加自动设置Fragment
                    .add(R.id.autoDetail, layoutAutoRange, tags[1]) // 添加自动量程Fragment
                    .hide(layoutAutoRange) // 默认隐藏自动量程Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        autoDetail = (RelativeLayout) view.findViewById(R.id.autoDetail); // 查找详情容器布局
        autoTitle = (TopViewTitleWithScroll) view.findViewById(R.id.autoTitle); // 查找标题视图
        autoTitle.setData(R.array.auto, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据（选项数组、切换监听器、点击监听器）

        layoutAutoSet.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置自动设置的消息监听器
        layoutAutoRange.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置自动量程的消息监听器

        msgAuto = new TopMsgAuto(); // 创建自动功能消息数据模型
        msgAuto.setAutoTitle(autoTitle.getSelected()); // 设置当前选中的标题
        msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail()); // 设置默认详情为自动设置数据
        msgAuto.setFromEventBus(false); // 标记非EventBus来源
    }

    /**
     * 初始化消息订阅，监听缓存加载事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
    }

    /**
     * 从缓存恢复UI状态，恢复上次选中的子页面
     */
    private void setCache() { // 从缓存恢复状态
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO); // 读取自动功能选中索引缓存值
        autoTitle.setSelected(index); // 设置标题选中项
        onCheckChanged(autoTitle, autoTitle.getSelected(), false); // 触发子页面切换
    }

    /**
     * 隐藏所有子Fragment，为显示目标子Fragment做准备
     */
    private void detailLayoutSetGone() { // 隐藏所有子Fragment
        getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                .hide(layoutAutoSet) // 隐藏自动设置Fragment
                .hide(layoutAutoRange) // 隐藏自动量程Fragment
                .commitAllowingStateLoss(); // 提交事务
    }

    /**
     * 通过RxBus广播自动功能消息
     */
    private void sendMsg() { // 广播自动功能消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_AUTO, msgAuto); // 发送自动功能消息到消息总线
    }

    /**
     * 缓存加载消息消费者，当收到缓存加载事件时恢复UI状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载消息
            setCache(); // 从缓存恢复UI状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAuto, true); // 标记本页面缓存已加载完成
        }
    };

    /**
     * 标题项点击监听器，播放按键音效
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题项点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /**
     * 标题选中项变化处理，切换子Fragment并更新消息数据
     *
     * @param view           触发变化的视图
     * @param item           新选中的标题选项数据
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) { // 标题选中项变化处理
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO, String.valueOf(item.getIndex())); // 更新自动功能选中索引缓存
        detailLayoutSetGone(); // 隐藏所有子Fragment
        switch (item.getIndex()) { // 根据选中索引切换子页面
            case DETAIL_SET: // 自动设置页面
                getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                        .show(layoutAutoSet).commitAllowingStateLoss(); // 显示自动设置Fragment
                msgAuto.setAutoTitle(item); // 更新消息中的标题数据
                msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail()); // 更新消息中的详情数据为自动设置
                msgAuto.setFromEventBus(false); // 标记非EventBus来源
                sendMsg(); // 广播自动功能消息
                break; // 结束自动设置处理
            case DETAIL_RANGE: // 自动量程页面
                getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                        .show(layoutAutoRange).commitAllowingStateLoss(); // 显示自动量程Fragment
                msgAuto.setAutoTitle(item); // 更新消息中的标题数据
                msgAuto.setAutoDetail(layoutAutoRange.getAutoDetail()); // 更新消息中的详情数据为自动量程
                msgAuto.setFromEventBus(false); // 标记非EventBus来源
                sendMsg(); // 广播自动功能消息
                break; // 结束自动量程处理
        }
    }

    /**
     * 标题切换监听器，委托给onCheckChanged处理
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题切换监听器
        @Override // 覆写checkChanged方法
        public void checkChanged(View view, TopAllBeanTitle item) { // 标题切换回调
            onCheckChanged(view, item, false); // 委托给onCheckChanged处理
        }
    };

    /**
     * 详情消息发送监听器，接收子Fragment的消息并更新数据模型后广播
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 详情消息发送监听器
        @Override // 覆写onClick方法
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 子Fragment消息回调
            if (msgAuto.getAutoTitle() == null) { // 标题数据为空
                msgAuto.setAutoTitle(autoTitle.getSelected()); // 使用当前选中的标题
            }
            if (fragment.equals(layoutAutoSet)) { // 消息来自自动设置Fragment
                msgAuto.setAutoDetail(layoutAutoSet.getMsgAutoDetail()); // 更新详情数据为自动设置
                msgAuto.getAutoTitle().setRxMsgSelect(false); // 取消标题选中状态
                msgAuto.setFromEventBus(isFromEventBus); // 设置EventBus来源标识
                sendMsg(); // 广播自动功能消息
            } else if (fragment.equals(layoutAutoRange)) { // 消息来自自动量程Fragment
                msgAuto.setAutoDetail(layoutAutoRange.getAutoDetail()); // 更新详情数据为自动量程
                msgAuto.getAutoTitle().setRxMsgSelect(false); // 取消标题选中状态
                msgAuto.setFromEventBus(isFromEventBus); // 设置EventBus来源标识
                sendMsg(); // 广播自动功能消息
            }
        }
    };

    /**
     * 获取当前自动功能的选中索引
     *
     * @return 选中索引，0=自动设置，1=自动量程
     */
    public int getAutoIdx(){ // 获取当前自动功能选中索引
        return autoTitle.getSelected().getIndex(); // 返回标题选中项的索引
    }
}
