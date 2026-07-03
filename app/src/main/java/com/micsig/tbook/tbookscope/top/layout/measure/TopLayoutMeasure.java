// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopLayoutMeasureCommon.java
//  核心职责：常规测量页面的核心Fragment，管理测量项的选择/取消、硬件消息收发、测量数据显示
//  架构设计：Fragment + RxBus观察者模式，通过Consumer订阅各类事件，通过Command发送硬件指令
//  数据流向：用户操作 → selectListAdd/delMeasureItem → Command发送硬件指令 → EventUIObserver接收测量数据 → 更新UI
//  依赖关系：依赖MeasureAdapter/MeasureBean数据层、Command硬件指令层、RxBus事件总线、MeasureManage测量管理器
//  使用场景：测量功能常规页面，用户选择/取消测量项、配置延时/相位/TValue参数、查看测量结果
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.content.res.Configuration; // 导入配置类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.top.layout.frequencymeter.TopLayoutFrequencyMeter; // 导入频率计布局
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入标题视图基类
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动的标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * Created by Administrator on 2017/4/6.
 */
public class TopLayoutMeasure extends Fragment { // 继承Fragment，作为测量功能的顶层容器
    public static final int DETAIL_COMMON = 0; // 常规测量子页面索引
    public static final int DETAIL_STATICS = 1; // 统计测量子页面索引
    public static final int DETAIL_FREQUENCY = 2; // 频率计/计数器子页面索引
    public static final int DETAIL_SETTING = 3; // 测量设置子页面索引

    private Context context; // 上下文对象
    private RelativeLayout measureDetail; // 子Fragment容器布局
    private TopViewTitleWithScroll measureTitle; // 顶部标题栏（带滚动）
    private TopLayoutMeasureCommon measureCommonLayout;             //常规测量子Fragment
    private TopLayoutMeasureSetting measureSettingLayout;           //Setting设置子Fragment
    private TopLayoutMeasureStatics measureStaticsLayout;           //Statics统计子Fragment
//    private TopLayoutMeasureCounter measureCounterLayout;           //Counter计数器子Fragment
    private TopLayoutFrequencyMeter measureCounterLayout;           //Counter频率计子Fragment

    private TopMsgMeasure msgMeasure; // 测量消息对象，用于RxBus通信

    private String[] tags = {"CommonLayout", "StaticsLayout", "CounterLayout","SettingLayout"}; // 子Fragment标签数组
    private Fragment[] fragments = new Fragment[4]; // 子Fragment数组，用于状态恢复

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
        return inflater.inflate(R.layout.layout_measure, container, false); // 填充测量布局
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

        measureCommonLayout = fragments[0] == null ? new TopLayoutMeasureCommon() : (TopLayoutMeasureCommon) fragments[0]; // 获取或创建常规测量Fragment
        measureStaticsLayout = fragments[1] == null ? new TopLayoutMeasureStatics() : (TopLayoutMeasureStatics) fragments[1]; // 获取或创建统计Fragment
//        measureCounterLayout = fragments[3] == null ? new TopLayoutMeasureCounter() : (TopLayoutMeasureCounter) fragments[3];
        measureCounterLayout = fragments[2] == null ? new TopLayoutFrequencyMeter() : (TopLayoutFrequencyMeter) fragments[2]; // 获取或创建频率计Fragment
        measureSettingLayout = fragments[3] == null ? new TopLayoutMeasureSetting() : (TopLayoutMeasureSetting) fragments[3]; // 获取或创建设置Fragment
        if (savedInstanceState == null) { // 如果是首次创建（非状态恢复）
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .add(R.id.measureDetail, measureCommonLayout, tags[0]) // 添加常规测量Fragment
                    .add(R.id.measureDetail, measureStaticsLayout, tags[1]) // 添加统计Fragment
                    .add(R.id.measureDetail, measureCounterLayout, tags[2]) // 添加频率计Fragment
                    .add(R.id.measureDetail, measureSettingLayout, tags[3]) // 添加设置Fragment
                    .hide(measureCommonLayout) // 隐藏常规测量
                    .hide(measureSettingLayout) // 隐藏设置
                    .hide(measureStaticsLayout) // 隐藏统计
                    .hide(measureCounterLayout) // 隐藏频率计
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        measureDetail = (RelativeLayout) view.findViewById(R.id.measureDetail); // 获取子Fragment容器
        measureTitle = (TopViewTitleWithScroll) view.findViewById(R.id.measureTitle); // 获取标题栏
        String[] array = context.getResources().getStringArray(R.array.measure); // 获取测量Tab名称数组
        boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
        for (int i = 0; i < array.length; i++) { // 遍历所有Tab
            if (i == 0 || i==1 || i==2 || i == 3) { // 前四个Tab可见
                arrayVisible[i] = true; // 设置为可见
            } else { // 其余Tab
                arrayVisible[i] = false; // 设置为不可见
            }
        }
        measureTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题栏数据和监听器

        measureCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置常规测量消息监听
        measureSettingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置设置消息监听
        measureStaticsLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置统计消息监听
        measureCounterLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置频率计消息监听

        msgMeasure = new TopMsgMeasure(); // 创建测量消息对象
        msgMeasure.setMeasureTitle(measureTitle.getSelected()); // 设置当前选中的标题项
        msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail()); // 设置当前测量详情
        msgMeasure.setFromEventBus(false); // 标记非来自EventBus
    }

    /**
     * 初始化事件控制，注册RxBus订阅
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 从缓存恢复测量页面状态
     */
    private void setCache() { // 设置缓存状态
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE); // 获取缓存中的Tab索引
        measureTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(measureTitle, measureTitle.getSelected(), false); // 触发Tab切换
    }

    /**
     * 发送测量消息到RxBus
     */
    private void sendMsg() { // 发送消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE, msgMeasure); // 通过RxBus发送测量消息
    }

    /**
     * 缓存加载消费者 - 接收缓存加载事件后恢复状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasure, true); // 标记测量页面缓存已加载
        }
    };

    /**
     * 标题项点击监听器 - 播放按键音效
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题项点击监听器
        @Override
        public void onClick(View v) { // 点击事件处理
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /**
     * 标题选中变更监听器 - 触发子Fragment切换
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题选中变更监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选中变更回调
            onCheckChanged(view, item, false); // 触发Tab切换逻辑
        }
    };

    /**
     * 详情发送消息监听器 - 子Fragment通知顶层发送测量消息
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 详情发送消息监听器
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 子Fragment点击回调
            if (msgMeasure.getMeasureTitle() == null) { // 如果消息中的标题为空
                msgMeasure.setMeasureTitle(measureTitle.getSelected()); // 设置当前选中标题
            }
            if (fragment.equals(measureCommonLayout)) { // 如果来自常规测量Fragment
                msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail()); // 设置常规测量详情
                msgMeasure.getMeasureTitle().setRxMsgSelect(false); // 标记非RxMsg选择
                msgMeasure.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(measureSettingLayout)) { // 如果来自设置Fragment
                msgMeasure.setMeasureDetail(measureSettingLayout.getMeasureDetail()); // 设置设置详情
                msgMeasure.getMeasureTitle().setRxMsgSelect(false); // 标记非RxMsg选择
                msgMeasure.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(measureStaticsLayout)) { // 如果来自统计Fragment
                msgMeasure.setMeasureDetail(measureStaticsLayout.getMeasureDetail()); // 设置统计详情
                msgMeasure.getMeasureTitle().setRxMsgSelect(false); // 标记非RxMsg选择
                msgMeasure.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(measureCounterLayout)) { // 如果来自频率计Fragment
                msgMeasure.setMeasureDetail(measureCounterLayout.getMeasureDetail()); // 设置频率计详情
                msgMeasure.getMeasureTitle().setRxMsgSelect(false); // 标记非RxMsg选择
                msgMeasure.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            }
        }
    };

    /**
     * 切换到常规测量页面
     */
    public void showMeasureCommon(){ // 显示常规测量页面
        measureTitle.setSelected(0); // 选中第一个Tab
        onCheckChanged(measureTitle, measureTitle.getSelected(), false); // 触发Tab切换
    }

    /**
     * 获取当前选中的测量Tab索引
     * @return Tab索引，0=常规，1=统计，2=计数器，3=设置
     */
    public int getMeasureIdx(){ // 获取当前测量Tab索引
        if(measureTitle != null // 如果标题栏不为空
                && measureTitle.getSelected() != null) { // 且有选中项
            return measureTitle.getSelected().getIndex(); // 返回选中项索引
        }else{ // 否则
            return 0; // 默认返回0
        }
    }

    /**
     * Tab切换核心逻辑 - 隐藏所有子Fragment，显示目标子Fragment
     * @param view 触发切换的视图
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) { // Tab切换核心方法
        if (view.getId() == measureTitle.getId()) { // 确认是标题栏触发的切换
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE, String.valueOf(item.getIndex())); // 保存当前Tab索引到缓存
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .hide(measureCommonLayout) // 隐藏常规测量
                    .hide(measureSettingLayout) // 隐藏设置
                    .hide(measureStaticsLayout) // 隐藏统计
                    .hide(measureCounterLayout) // 隐藏频率计
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引切换
                case DETAIL_COMMON: // 常规测量
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(measureCommonLayout).commitAllowingStateLoss(); // 显示常规测量Fragment
                    msgMeasure.setMeasureTitle(item); // 设置消息标题
                    msgMeasure.setMeasureDetail(measureCommonLayout.getMeasureDetail()); // 设置消息详情
                    msgMeasure.setFromEventBus(isFromEventBus); // 设置来源标记
                    sendMsg(); // 发送消息
                    break; // 跳出
                case DETAIL_SETTING: // 设置
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(measureSettingLayout).commitAllowingStateLoss(); // 显示设置Fragment
                    msgMeasure.setMeasureTitle(item); // 设置消息标题
                    msgMeasure.setMeasureDetail(measureSettingLayout.getMeasureDetail()); // 设置消息详情
                    msgMeasure.setFromEventBus(isFromEventBus); // 设置来源标记
                    sendMsg(); // 发送消息
                    break; // 跳出
                case DETAIL_STATICS: // 统计
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(measureStaticsLayout).commitAllowingStateLoss(); // 显示统计Fragment
                    msgMeasure.setMeasureTitle(item); // 设置消息标题
                    msgMeasure.setMeasureDetail(measureStaticsLayout.getMeasureDetail()); // 设置消息详情
                    msgMeasure.setFromEventBus(isFromEventBus); // 设置来源标记
                    sendMsg(); // 发送消息
                    break; // 跳出
                case DETAIL_FREQUENCY: // 频率计
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(measureCounterLayout).commitAllowingStateLoss(); // 显示频率计Fragment
                    msgMeasure.setMeasureTitle(item); // 设置消息标题
                    msgMeasure.setMeasureDetail(measureCounterLayout.getMeasureDetail()); // 设置消息详情
                    msgMeasure.setFromEventBus(isFromEventBus); // 设置来源标记
                    sendMsg(); // 发送消息
                    break; // 跳出
            }
        }
    }
}
