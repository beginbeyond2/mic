package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入顶部标题视图
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动的顶部标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 顶部Display菜单容器Fragment                     ║
 * ║  核心职责: 管理Display菜单下所有子页面的切换、消息传递和缓存恢复              ║
 * ║  架构设计: 容器Fragment模式，内嵌6个子Fragment通过show/hide切换              ║
 * ║  数据流向: 子Fragment → onDetailSendMsgListener → sendMsg → RxBus        ║
 * ║  依赖关系: TopLayoutDisplayCommon/Waveform/Graticule/Persist/FftInfo/TxtMix║
 * ║  使用场景: 用户点击顶部Display菜单时，展示对应的子页面并通知主界面更新           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by Administrator on 2017/4/6.
 */
public class TopLayoutDisplay extends Fragment { // Display菜单容器Fragment，管理6个子页面的切换与消息传递

    public static final int DETAIL_COMMON = 0; // 子页面索引：常规
    public static final int DETAIL_WAVEFORM = 1; // 子页面索引：波形
    public static final int DETAIL_GRATICULE = 2; // 子页面索引：方格图
    public static final int DETAIL_PERSIST = 3; // 子页面索引：余辉
    public static final int DETAIL_FFT_INFO = 4; // 子页面索引：FFT Info
    public static final int DETAIL_TXT_MIX = 5; // 子页面索引：TXT Mix

    private Context context; // Fragment关联的Activity上下文
    private TopViewTitleWithScroll titleWithHead; // 顶部标题栏（带滚动）
    private RelativeLayout displayDetail; // 子Fragment容器布局
    private TopViewTitleWithScroll displayTitle; // Display标题栏视图
    private TopLayoutDisplayCommon displayCommonLayout;             //常规 // 常规子Fragment
    private TopLayoutDisplayWaveform displayWaveformLayout;         //波形 // 波形子Fragment
    private TopLayoutDisplayGraticule displayGraticuleLayout;       //方格图 // 方格图子Fragment
    private TopLayoutDisplayPersist displayPersistLayout;           //余辉 // 余辉子Fragment
    private TopLayoutDisplayFftInfo displayFftInfoLayout;           //FFT Info // FFT Info子Fragment
    private TopLayoutDisplayTxtMix displayTxtMixLayout;             //TXT Mix // TXT Mix子Fragment

    private TopMsgDisplay msgDisplay; // Display消息封装对象，用于向主界面传递数据

    private String[] tags = {"CommonLayout", "WaveformLayout", "GraticuleLayout", "PersistLayout", "FftInfoLayout", "TxtMixLayout"}; // 子Fragment的Tag标识数组
    private Fragment[] fragments = new Fragment[tags.length]; // 子Fragment数组，用于状态恢复

    /**
     * 创建Fragment视图，加载布局文件
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标记返回值可为空
    @Override // 重写onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_display, container, false); // 加载Display布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取关联的Activity上下文
        initView(view, savedInstanceState); // 初始化视图
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件，包括子Fragment的创建/恢复、标题栏设置、消息监听器绑定
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图
        if (savedInstanceState != null) { // 有保存的状态（如屏幕旋转恢复）
            for (int i = 0; i < tags.length; i++) { // 遍历所有Tag
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 通过Tag恢复Fragment引用
            }
        }

        displayCommonLayout = fragments[0] == null ? new TopLayoutDisplayCommon() : (TopLayoutDisplayCommon) fragments[0]; // 恢复或创建常规Fragment
        displayWaveformLayout = fragments[1] == null ? new TopLayoutDisplayWaveform() : (TopLayoutDisplayWaveform) fragments[1]; // 恢复或创建波形Fragment
        displayGraticuleLayout = fragments[2] == null ? new TopLayoutDisplayGraticule() : (TopLayoutDisplayGraticule) fragments[2]; // 恢复或创建方格图Fragment
        displayPersistLayout = fragments[3] == null ? new TopLayoutDisplayPersist() : (TopLayoutDisplayPersist) fragments[3]; // 恢复或创建余辉Fragment
        displayFftInfoLayout = fragments[4] == null ? new TopLayoutDisplayFftInfo() : (TopLayoutDisplayFftInfo) fragments[4]; // 恢复或创建FFT Info Fragment
        displayTxtMixLayout = fragments[5] == null ? new TopLayoutDisplayTxtMix() : (TopLayoutDisplayTxtMix) fragments[5]; // 恢复或创建TXT Mix Fragment
        if (savedInstanceState == null) { // 首次创建，需要添加子Fragment
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .add(R.id.displayDetail, displayCommonLayout, tags[0]) // 添加常规Fragment
                    .add(R.id.displayDetail, displayWaveformLayout, tags[1]) // 添加波形Fragment
                    .add(R.id.displayDetail, displayGraticuleLayout, tags[2]) // 添加方格图Fragment
                    .add(R.id.displayDetail, displayPersistLayout, tags[3]) // 添加余辉Fragment
                    .add(R.id.displayDetail, displayFftInfoLayout, tags[4]) // 添加FFT Info Fragment
                    .add(R.id.displayDetail, displayTxtMixLayout, tags[5]) // 添加TXT Mix Fragment
                    .hide(displayWaveformLayout) // 隐藏波形Fragment
                    .hide(displayGraticuleLayout) // 隐藏方格图Fragment
                    .hide(displayPersistLayout) // 隐藏余辉Fragment
                    .hide(displayFftInfoLayout) // 隐藏FFT Info Fragment
                    .hide(displayTxtMixLayout) // 隐藏TXT Mix Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        displayDetail = (RelativeLayout) view.findViewById(R.id.displayDetail); // 获取子Fragment容器
        displayTitle = (TopViewTitleWithScroll) view.findViewById(R.id.displayTitle); // 获取Display标题栏
        displayTitle.setData(R.array.display, onCheckChangedTitleListener, onItemClickListener); // 设置标题栏数据和监听器

        titleWithHead = (TopViewTitleWithScroll) view.findViewById(R.id.displayTitle); // 获取标题栏引用（用于后续判断当前页面）

        displayCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定常规Fragment消息监听器
        displayWaveformLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定波形Fragment消息监听器
        displayGraticuleLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定方格图Fragment消息监听器
        displayPersistLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定余辉Fragment消息监听器
        displayFftInfoLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定FFT Info Fragment消息监听器
        displayTxtMixLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 绑定TXT Mix Fragment消息监听器

        msgDisplay = new TopMsgDisplay(); // 创建Display消息对象
        msgDisplay.setDisplayTitle(displayTitle.getSelected()); // 设置当前选中的标题
        msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail()); // 设置当前常规Fragment的详情数据
        msgDisplay.setFromEventBus(false); // 标记非EventBus来源
    }

    /**
     * 初始化事件控制，订阅缓存加载事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 从缓存恢复Display页面的选中状态
     */
    private void setCache() { // 设置缓存
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY); // 获取缓存的Display页面索引
        displayTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(displayTitle, displayTitle.getSelected(), false); // 触发页面切换
    }

    /**
     * 通过RxBus发送Display消息到主界面
     */
    private void sendMsg() { // 发送消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_DISPLAY, msgDisplay); // 发送Display消息事件
    }

    /**
     * 缓存加载事件的消费者，恢复缓存状态并标记加载完成
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override // 重写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplay, true); // 标记Display页面缓存已加载
        }
    };

    /**
     * 标题项点击监听器，播放按键音效
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题项点击监听器
        @Override // 重写onClick方法
        public void onClick(View v) { // 处理点击事件
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /**
     * 标题选中切换监听器，触发子页面切换
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题选中切换监听器
        @Override // 重写checkChanged方法
        public void checkChanged(View view, TopAllBeanTitle item) { // 处理标题切换事件
            onCheckChanged(view, item, false); // 触发页面切换（非EventBus来源）
        }
    };

    /**
     * 子Fragment详情消息发送监听器，接收子Fragment的数据变更并转发到主界面
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 详情消息发送监听器
        @Override // 重写onClick方法
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 处理子Fragment消息
            if (msgDisplay.getDisplayTitle() == null) { // 标题为空时初始化
                msgDisplay.setDisplayTitle(displayTitle.getSelected()); // 设置当前选中的标题
            }
            if (fragment.equals(displayCommonLayout)) { // 来自常规Fragment
                msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail()); // 设置常规详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            } else if (fragment.equals(displayWaveformLayout)) { // 来自波形Fragment
                msgDisplay.setDisplayDetail(displayWaveformLayout.getDisplayDetail()); // 设置波形详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            } else if (fragment.equals(displayGraticuleLayout)) { // 来自方格图Fragment
                msgDisplay.setDisplayDetail(displayGraticuleLayout.getDisplayDetail()); // 设置方格图详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            } else if (fragment.equals(displayPersistLayout)) { // 来自余辉Fragment
                msgDisplay.setDisplayDetail(displayPersistLayout.getDisplayDetail()); // 设置余辉详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            } else if (fragment.equals(displayFftInfoLayout)) { // 来自FFT Info Fragment
                msgDisplay.setDisplayDetail(displayFftInfoLayout.getDisplayDetail()); // 设置FFT Info详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            } else if (fragment.equals(displayTxtMixLayout)) { // 来自TXT Mix Fragment
                msgDisplay.setDisplayDetail(displayTxtMixLayout.getDisplayDetail()); // 设置TXT Mix详情数据
                msgDisplay.getDisplayTitle().setRxMsgSelect(false); // 取消标题选中高亮
                msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                sendMsg(); // 发送消息
            }
        }
    };

    /**
     * 处理标题切换事件，隐藏所有子Fragment后显示目标Fragment并发送消息
     * @param view 触发切换的视图
     * @param item 选中的标题项数据
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) { // 处理标题切换
        if (view.getId() == displayTitle.getId()) { // 确认是Display标题栏触发的切换
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY, String.valueOf(item.getIndex())); // 缓存当前选中的页面索引
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .hide(displayCommonLayout) // 隐藏常规Fragment
                    .hide(displayWaveformLayout) // 隐藏波形Fragment
                    .hide(displayGraticuleLayout) // 隐藏方格图Fragment
                    .hide(displayPersistLayout) // 隐藏余辉Fragment
                    .hide(displayFftInfoLayout) // 隐藏FFT Info Fragment
                    .hide(displayTxtMixLayout) // 隐藏TXT Mix Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
            switch (item.getIndex()) { // 根据选中索引切换到对应子页面
                case DETAIL_COMMON:             //常规 // 常规页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayCommonLayout).commitAllowingStateLoss(); // 显示常规Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayCommonLayout.getDisplayDetail()); // 设置常规详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
                case DETAIL_WAVEFORM:             //波形 // 波形页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayWaveformLayout).commitAllowingStateLoss(); // 显示波形Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayWaveformLayout.getDisplayDetail()); // 设置波形详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
                case DETAIL_GRATICULE:             //方格图 // 方格图页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayGraticuleLayout).commitAllowingStateLoss(); // 显示方格图Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayGraticuleLayout.getDisplayDetail()); // 设置方格图详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
                case DETAIL_PERSIST:             //余辉 // 余辉页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayPersistLayout).commitAllowingStateLoss(); // 显示余辉Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayPersistLayout.getDisplayDetail()); // 设置余辉详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
                case DETAIL_FFT_INFO:             //FFT Info // FFT Info页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayFftInfoLayout).commitAllowingStateLoss(); // 显示FFT Info Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayFftInfoLayout.getDisplayDetail()); // 设置FFT Info详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
                case DETAIL_TXT_MIX:             //TXT Mix // TXT Mix页面
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(displayTxtMixLayout).commitAllowingStateLoss(); // 显示TXT Mix Fragment
                    msgDisplay.setDisplayTitle(item); // 设置消息标题
                    msgDisplay.setDisplayDetail(displayTxtMixLayout.getDisplayDetail()); // 设置TXT Mix详情数据
                    msgDisplay.setFromEventBus(isFromEventBus); // 设置消息来源标记
                    sendMsg(); // 发送消息
                    break; // 退出case
            }
        }
    }

    /**
     * 获取当前Display页面的选中索引
     * @return 当前选中的子页面索引
     */
    public int getDisplayIdx(){ // 获取当前Display页面索引
        return displayTitle.getSelected().getIndex(); // 返回标题栏选中项的索引
    }


    /**
     * Serials word 显现消失的时候，设置Display标签下的所有标签的可点击性
     * @param serialsWordVisible true表示串口文本可见，需禁用其他标签；false表示恢复
     */
    public void setSerialsWordVisible(boolean serialsWordVisible) { // 设置串口文本可见性对标签的影响
        displayTitle.setEnable(DETAIL_COMMON, !serialsWordVisible); // 常规标签可点击性
        displayTitle.setEnable(DETAIL_WAVEFORM, !serialsWordVisible); // 波形标签可点击性
        displayTitle.setEnable(DETAIL_GRATICULE, !serialsWordVisible); // 方格图标签可点击性
        displayTitle.setEnable(DETAIL_PERSIST, !serialsWordVisible); // 余辉标签可点击性
        displayTitle.setEnable(DETAIL_FFT_INFO, !serialsWordVisible); // FFT Info标签可点击性
        displayTitle.setEnable(DETAIL_TXT_MIX, true); // TXT Mix标签始终可点击

        if (serialsWordVisible && displayTitle.getSelected().getIndex() != DETAIL_TXT_MIX) { // 串口可见且当前不在TXT Mix页
            displayTitle.moveOnlyScroll(DETAIL_TXT_MIX); // 仅滚动到TXT Mix位置
            displayTitle.setSelected(DETAIL_TXT_MIX); // 选中TXT Mix标签
            onCheckChanged(displayTitle, displayTitle.getSelected(), false); // 切换到TXT Mix页面
        }

    }

    /**
     * 判断当前是否显示常规页面
     * @return true表示当前是常规页面
     */
    public boolean isShowLayoutCommom() { // 判断是否显示常规页面
        return DETAIL_COMMON == titleWithHead.getSelected().getIndex(); // 比较当前选中索引与常规索引
    }

    /**
     * 切换到常规页面
     */
    public void showLayoutCommon() { // 显示常规页面
        titleWithHead.setSelected(DETAIL_COMMON); // 选中常规标签
        onCheckChanged(titleWithHead, titleWithHead.getSelected(), false); // 切换到常规页面
    }

    
}
