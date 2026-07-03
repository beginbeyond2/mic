// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopLayoutSample.java
//  核心职责：采样功能顶层Fragment容器，管理3个子页面（模式/记录长度/分段存储）的切换
//  架构设计：Fragment + ChildFragmentManager管理子Fragment，通过RxBus传递消息
//  数据流向：用户切换标题 → onCheckChanged → show/hide子Fragment → sendMsg通知标题栏
//  依赖关系：依赖TopLayoutSampleMode/Depth/Segmented子Fragment、RxBus事件总线、CacheUtil缓存
//  使用场景：采样功能顶层容器，用户在模式/记录长度/分段存储之间切换
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入标题视图基类
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动的标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 采样功能顶层Fragment - 管理3个子页面的切换和消息传递
 */
public class TopLayoutSample extends Fragment { // 继承Fragment，采样功能顶层容器
    /** 模式子页面索引 */
    public static final int DETAIL_MODE = 0; // 模式子页面索引
    /** 记录长度子页面索引 */
    public static final int DETAIL_DEPTH = 1; // 记录长度子页面索引
    /** 分段存储子页面索引 */
    public static final int DETAIL_SEGMENTED = 2; // 分段存储子页面索引

    private Context context; // 上下文对象
    private RelativeLayout sampleDetail; // 子Fragment容器布局
    private TopViewTitleWithScroll sampleTitle; // 顶部标题栏（带滚动）
    private TopLayoutSampleMode sampleModeLayout;                   //模式子Fragment
    private TopLayoutSampleDepth sampleDepthLayout;                 //记录长度子Fragment
    private TopLayoutSampleSegmented sampleSegmentedLayout;         //分段存储子Fragment

    /** 采样消息对象，用于RxBus通信 */
    private TopMsgSample msgSample; // 采样消息对象

    /** 子Fragment标签数组 */
    private String[] tags = {"sampleModeLayout", "sampleDepthLayout", "sampleSegmentedLayout"}; // 子Fragment标签
    /** 子Fragment数组，用于状态恢复 */
    private Fragment[] fragments = new Fragment[3]; // 子Fragment数组

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
        return inflater.inflate(R.layout.layout_sample, container, false); // 填充采样布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view, savedInstanceState); // 初始化视图
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件和子Fragment
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图方法
        if (savedInstanceState != null) { // 如果有保存的状态（如屏幕旋转恢复）
            for (int i = 0; i < tags.length; i++) { // 遍历所有标签
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 根据标签查找已存在的子Fragment
            }
        }

        sampleModeLayout = fragments[0] == null ? new TopLayoutSampleMode() : (TopLayoutSampleMode) fragments[0]; // 获取或创建模式Fragment
        sampleDepthLayout = fragments[1] == null ? new TopLayoutSampleDepth() : (TopLayoutSampleDepth) fragments[1]; // 获取或创建记录长度Fragment
        sampleSegmentedLayout = fragments[2] == null ? new TopLayoutSampleSegmented() : (TopLayoutSampleSegmented) fragments[2]; // 获取或创建分段存储Fragment
        if (savedInstanceState == null) { // 如果是首次创建（非状态恢复）
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .add(R.id.sampleDetail, sampleModeLayout, tags[0]) // 添加模式Fragment
                    .add(R.id.sampleDetail, sampleDepthLayout, tags[1]) // 添加记录长度Fragment
                    .add(R.id.sampleDetail, sampleSegmentedLayout, tags[2]) // 添加分段存储Fragment
                    .hide(sampleDepthLayout) // 隐藏记录长度
                    .hide(sampleSegmentedLayout) // 隐藏分段存储
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        sampleDetail = (RelativeLayout) view.findViewById(R.id.sampleDetail); // 获取子Fragment容器
        sampleTitle = (TopViewTitleWithScroll) view.findViewById(R.id.sampleTitle); // 获取标题栏
        sampleTitle.setData(R.array.sample, R.array.sampleVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题栏数据

//        sampleModeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
//        sampleDepthLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);
//        sampleSegmentedLayout.setOnDetailSendMsgListener(onDetailSendMsgListener);

        msgSample = new TopMsgSample(); // 创建采样消息对象
        msgSample.setSampleTitle(sampleTitle.getSelected()); // 设置标题栏选中项
//        msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
        msgSample.setFromEventBus(false); // 设置为非EventBus来源
    }

    /**
     * 初始化RxBus事件订阅
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 从缓存恢复标题栏选中状态
     */
    private void setCache() { // 从缓存恢复状态
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE); // 读取采样页面选中索引
        sampleTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(sampleTitle, sampleTitle.getSelected(), false); // 触发页面切换
    }

    /**
     * 发送采样消息到RxBus
     */
    private void sendMsg() { // 发送采样消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLE, msgSample); // 通过RxBus发送采样消息
    }

    /**
     * 缓存加载消费者 - 恢复标题栏选中状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSample, true); // 标记采样页面缓存加载完成
        }
    };

    /**
     * 标题栏点击监听器 - 播放音效
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题栏点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /**
     * 标题栏选中变更监听器
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题栏选中变更监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选中变更回调
            onCheckChanged(view, item, false); // 触发页面切换
        }
    };

//    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() {
//        @Override
//        public void onClick(Fragment fragment, boolean isFromEventBus) {
//            if (msgSample.getSampleTitle() == null) {
//                msgSample.setSampleTitle(sampleTitle.getSelected());
//            }
//            if (fragment.equals(sampleModeLayout)) {
//                msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            } else if (fragment.equals(sampleDepthLayout)) {
//                msgSample.setSampleDetail(sampleDepthLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            } else if (fragment.equals(sampleSegmentedLayout)) {
//                msgSample.setSampleDetail(sampleSegmentedLayout.getSampleDetail());
//                msgSample.getSampleTitle().setRxMsgSelect(false);
//                msgSample.setFromEventBus(isFromEventBus);
//                sendMsg();
//            }
//        }
//    };

    /**
     * 标题栏选中变更核心处理 - 切换子Fragment显示
     * @param view 触发变更的视图
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) { // 页面切换核心处理
        if (view.getId() == sampleTitle.getId()) { // 如果是标题栏触发
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE, String.valueOf(item.getIndex())); // 保存选中索引到缓存
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .hide(sampleModeLayout) // 隐藏模式Fragment
                    .hide(sampleDepthLayout) // 隐藏记录长度Fragment
                    .hide(sampleSegmentedLayout) // 隐藏分段存储Fragment
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引切换
                case DETAIL_MODE:             //模式
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(sampleModeLayout).commitAllowingStateLoss(); // 显示模式Fragment
                    msgSample.setSampleTitle(item); // 更新消息标题
//                    msgSample.setSampleDetail(sampleModeLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus); // 设置来源标志
                    sendMsg(); // 发送消息
                    break;
                case DETAIL_DEPTH:             //记录长度
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(sampleDepthLayout).commitAllowingStateLoss(); // 显示记录长度Fragment
                    msgSample.setSampleTitle(item); // 更新消息标题
//                    msgSample.setSampleDetail(sampleDepthLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus); // 设置来源标志
                    sendMsg(); // 发送消息
                    break;
                case DETAIL_SEGMENTED:             //分段存储
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(sampleSegmentedLayout).commitAllowingStateLoss(); // 显示分段存储Fragment
                    msgSample.setSampleTitle(item); // 更新消息标题
//                    msgSample.setSampleDetail(sampleSegmentedLayout.getSampleDetail());
                    msgSample.setFromEventBus(isFromEventBus); // 设置来源标志
                    sendMsg(); // 发送消息
                    break;
            }
        }
    }

    /**
     * 获取当前采样页面选中索引
     * @return 选中索引（0=模式,1=记录长度,2=分段存储）
     */
    public int getSampleIdx(){ // 获取采样页面选中索引
        return sampleTitle.getSelected().getIndex(); // 返回标题栏选中索引
    }
}
