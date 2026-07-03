package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle状态保存类
import android.util.Log; // 导入日志工具类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.R; // 导入资源引用类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxBus事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题Bean类
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入标题视图类
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入可滚动标题视图类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 保存主容器（Save Container）             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为保存功能的顶层Fragment容器，管理Store/Invoke/AutoSave三个         │
 * │          子Tab页面的切换显示，通过Fragment事务控制子页面的显示和隐藏             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，使用TopViewTitleWithScroll作为Tab标题栏，              │
 * │          通过getChildFragmentManager管理子Fragment的添加/显示/隐藏，           │
 * │          通过RxBus订阅缓存加载事件和自动保存任务状态事件                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(缓存索引) → saveTitle(标题选择) →                       │
 * │          Fragment事务(切换子页面) → 子Fragment(Store/Invoke/AutoSave)        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewTitleWithScroll, TopLayoutSaveStore,                       │
 * │          TopLayoutSaveInvoke, TopLayoutAutoSave, CacheUtil, RxBus           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部菜单选择"保存"时显示此容器，                               │
 * │          通过Tab切换Store(存储)/Invoke(调用)/AutoSave(自动保存)子页面          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutSave extends Fragment {

    /** 子页面索引：Store存储 */
    public static final int DETAIL_STORE = 0;
    /** 子页面索引：Invoke调用 */
    public static final int DETAIL_INVOKE = 1;
    /** 子页面索引：AutoSave自动保存 */
    public static final int DETAIL_AUTOSAVE = 2;

    /** Fragment所在的上下文环境 */
    private Context context;
    /** 保存详情容器布局，用于承载子Fragment */
    private RelativeLayout saveDetail;
    /** 可滚动的标题栏，用于切换Store/Invoke/AutoSave */
    private TopViewTitleWithScroll saveTitle;

    /** Store存储子页面 */
    private TopLayoutSaveStore storeLayout;
    /** Invoke调用子页面 */
    private TopLayoutSaveInvoke invokeLayout;

    /** AutoSave自动保存子页面 */
    private TopLayoutAutoSave autoSaveLayout;

    /** 子Fragment的Tag标签数组，用于Fragment事务查找 */
    private final String[] tags = {"StoreLayout", "InvokeLayout","AutoSave"};
    /** 子Fragment数组，保存三个子页面的引用 */
    private final Fragment[] fragments = new Fragment[3];

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的保存主容器布局视图
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save, container, false); // 填充保存主容器布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity(); // 获取所在Activity作为上下文
        initView(view, savedInstanceState); // 初始化视图控件
        initControl(); // 初始化事件控制
    }


    /**
     * 初始化所有视图控件，包括子Fragment的创建和添加
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) {
        if (savedInstanceState != null) { // 如果有保存的状态（如屏幕旋转）
            for (int i = 0; i < tags.length; i++) { // 遍历所有Tag
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 从FragmentManager中恢复Fragment
            }
        }
        storeLayout = fragments[0] == null ? new TopLayoutSaveStore() : (TopLayoutSaveStore) fragments[0]; // 创建或恢复Store页面
        invokeLayout = fragments[1] == null ? new TopLayoutSaveInvoke() : (TopLayoutSaveInvoke) fragments[1]; // 创建或恢复Invoke页面
        autoSaveLayout = fragments[2] == null ? new TopLayoutAutoSave() : (TopLayoutAutoSave) fragments[2]; // 创建或恢复AutoSave页面
        if (savedInstanceState == null) { // 如果是首次创建（非恢复）
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .add(R.id.saveDetail, storeLayout, tags[0]) // 添加Store页面
                    .add(R.id.saveDetail, invokeLayout, tags[1]) // 添加Invoke页面
                    .add(R.id.saveDetail, autoSaveLayout, tags[2]) // 添加AutoSave页面
                    .hide(invokeLayout) // 隐藏Invoke页面
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }
        saveDetail = (RelativeLayout) view.findViewById(R.id.saveDetail); // 获取详情容器布局
        saveTitle = (TopViewTitleWithScroll) view.findViewById(R.id.saveTitle); // 获取标题栏
        String[] array = context.getResources().getStringArray(R.array.save); // 获取保存Tab标题数组
        boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
        for (int i = 0; i < array.length; i++) { // 遍历所有标题
            arrayVisible[i] = true; // 全部设为可见
        }
        saveTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据、选择监听器和点击监听器
    }

    /**
     * 初始化事件控制，订阅RxBus事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState); // 订阅自动保存任务状态更新事件

    }

    /**
     * 从缓存恢复标题选择状态
     */
    private void setCache() {
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE); // 获取缓存中保存的Tab索引
        saveTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(saveTitle, saveTitle.getSelected(), false); // 触发页面切换
    }

    /**
     * 缓存加载事件的RxJava消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSave, true); // 标记此菜单已加载缓存
        }
    };

    /**
     * 自动保存任务状态变化的RxJava消费者
     */
    private Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() { // 创建消费者
        @Override
        public void accept(Boolean setState) throws Throwable { // 接收到自动保存任务状态
            requireActivity().runOnUiThread(new Runnable() { // 切换到UI线程
                @Override
                public void run() {
                    Log.d("TAGTest", "run: " +setState); // 记录状态变化日志
                    if(setState){ // 如果自动保存任务正在运行
                        saveTitle.setEnable(DETAIL_STORE,true); // Store页面可用
                        saveTitle.setEnable(DETAIL_INVOKE,true); // Invoke页面可用
                    }else { // 自动保存任务未运行
                        saveTitle.setEnable(DETAIL_STORE,false); // Store页面不可用（禁用）
                        saveTitle.setEnable(DETAIL_INVOKE,false); // Invoke页面不可用（禁用）

                    }
                }
            });

        }
    };

    /**
     * 标题项点击监听器
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 创建点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /**
     * 标题选择变化监听器
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 创建选择变化监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选择变化回调
            onCheckChanged(view, item, false); // 委托给处理方法
        }
    };

    /**
     * 处理标题选择变化，切换子Fragment的显示
     * @param view 触发的视图
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == saveTitle.getId()) { // 如果是标题栏触发的
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE, String.valueOf(item.getIndex())); // 保存当前选中索引到缓存
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .hide(storeLayout) // 隐藏Store页面
                    .hide(invokeLayout) // 隐藏Invoke页面
                    .hide(autoSaveLayout) // 隐藏AutoSave页面
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引显示对应页面
                case DETAIL_STORE: // Store页面
                    getChildFragmentManager().beginTransaction()
                            .show(storeLayout).commitAllowingStateLoss(); // 显示Store页面
                    break;
                case DETAIL_INVOKE: // Invoke页面
                    getChildFragmentManager().beginTransaction()
                            .show(invokeLayout).commitAllowingStateLoss(); // 显示Invoke页面
                    break;
                case DETAIL_AUTOSAVE: // AutoSave页面
                    getChildFragmentManager().beginTransaction()
                            .show(autoSaveLayout).commitAllowingStateLoss(); // 显示AutoSave页面
                    break;
            }
        }
    }

    /**
     * 获取当前保存页面的选中索引
     * @return 当前选中的Tab索引（0=Store, 1=Invoke, 2=AutoSave）
     */
    public int getSaveIdx(){
        return saveTitle.getSelected().getIndex(); // 返回当前选中项的索引
    }

}
