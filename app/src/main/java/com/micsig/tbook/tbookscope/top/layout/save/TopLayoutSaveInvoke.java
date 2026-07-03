package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle状态保存类
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
 * │ 模块定位：保存/调用功能 → 顶部布局 → 调用子容器（Invoke Container）           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为调用功能的子Fragment容器，管理WAV/CSV/Setting/Session四个         │
 * │          子Tab页面的切换显示，通过Fragment事务控制子页面的显示和隐藏             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，使用TopViewTitleWithScroll作为Tab标题栏，              │
 * │          通过getChildFragmentManager管理子Fragment的添加/显示/隐藏，           │
 * │          通过OnDetailSendMsgListener将子页面消息传递给父级                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(缓存索引) → invokeTitle(标题选择) →                     │
 * │          Fragment事务(切换子页面) → 子Fragment(Wav/Csv/Setting/Session)       │
 * │          → OnDetailSendMsgListener → RxBus(TOPLAYOUT_INVOKE)                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewTitleWithScroll, TopLayoutInvokeWav,                       │
 * │          TopLayoutInvokeCsv, TopLayoutInvokeSetting,                        │
 * │          TopLayoutInvokeSession, CacheUtil, RxBus                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在保存菜单选择"调用"Tab时显示此容器，                             │
 * │          通过子Tab切换WAV/CSV/设置/会话四种调用类型                            │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutSaveInvoke extends Fragment {
    /** 子页面索引：调用WAV波形 */
    public static final int DETAIL_INVOKE_WAV = 0;
    /** 子页面索引：调用CSV数据 */
    public static final int DETAIL_INVOKE_CSV = 1;
    /** 子页面索引：调用设置（恢复配置） */
    public static final int DETAIL_INVOKE_RECOVERY = 2;
    /** 子页面索引：调用会话（恢复Session） */
    public static final int DETAIL_INVOKE_SESSION = 3;

    /** Fragment所在的上下文环境 */
    private Context context;
    /** 调用详情容器布局，用于承载子Fragment */
    private RelativeLayout invokeDetail;
    /** 可滚动的标题栏，用于切换WAV/CSV/Setting/Session */
    private TopViewTitleWithScroll invokeTitle;
    /** WAV调用子页面 */
    private TopLayoutInvokeWav wavLayout;               //invoke wave
    /** CSV调用子页面 */
    private TopLayoutInvokeCsv csvLayout;                //invoke csv
    /** 设置调用子页面 */
    private TopLayoutInvokeSetting settingLayout;        //invoke setting
    /** 会话调用子页面 */
    private TopLayoutInvokeSession sessionLayout;        //invoke session

    /** 调用消息对象，用于向父级传递当前调用状态 */
    private TopMsgSaveInvoke msgInvoke;

    /** 子Fragment的Tag标签数组，用于Fragment事务查找 */
    private final String[] tags = {"WavLayout", "CsvLayout", "SettingLayout", "SessionLayout"};
    /** 子Fragment数组，保存四个子页面的引用 */
    private Fragment[] fragments = new Fragment[4];

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的调用子容器布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke, container, false); // 填充调用子容器布局
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

        wavLayout = fragments[0] == null ? new TopLayoutInvokeWav() : (TopLayoutInvokeWav) fragments[0]; // 创建或恢复WAV页面
        csvLayout = fragments[1] == null ? new TopLayoutInvokeCsv() : (TopLayoutInvokeCsv) fragments[1]; // 创建或恢复CSV页面
        settingLayout = fragments[2] == null ? new TopLayoutInvokeSetting() : (TopLayoutInvokeSetting) fragments[2]; // 创建或恢复Setting页面
        sessionLayout = fragments[3] == null ? new TopLayoutInvokeSession() : (TopLayoutInvokeSession) fragments[3]; // 创建或恢复Session页面

        if (savedInstanceState == null) { // 如果是首次创建（非恢复）
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .add(R.id.invokeDetail, wavLayout, tags[0]) // 添加WAV页面
                    .add(R.id.invokeDetail, csvLayout, tags[1]) // 添加CSV页面
                    .add(R.id.invokeDetail, settingLayout, tags[2]) // 添加Setting页面
                    .add(R.id.invokeDetail, sessionLayout, tags[3]) // 添加Session页面
                    .hide(csvLayout) // 隐藏CSV页面
                    .hide(settingLayout) // 隐藏Setting页面
                    .hide(sessionLayout) // 隐藏Session页面
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        invokeDetail = (RelativeLayout) view.findViewById(R.id.invokeDetail); // 获取详情容器布局
        invokeTitle = (TopViewTitleWithScroll) view.findViewById(R.id.invokeTitle); // 获取标题栏
        String[] array = context.getResources().getStringArray(R.array.topInvokeType); // 获取调用类型标题数组
        boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
        for (int i = 0; i < array.length; i++) { // 遍历所有标题
            arrayVisible[i] = true; // 全部设为可见
        }
        invokeTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据、选择监听器和点击监听器

        wavLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置WAV页面消息监听器
        csvLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置CSV页面消息监听器
        settingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置Setting页面消息监听器
        sessionLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置Session页面消息监听器

        msgInvoke = new TopMsgSaveInvoke(); // 创建调用消息对象
        msgInvoke.setSaveTitle(invokeTitle.getSelected()); // 设置当前选中的标题
        msgInvoke.setSaveDetail(wavLayout.getSaveDetail()); // 设置当前WAV页面的保存详情
        msgInvoke.setFromEventBus(false); // 标记非来自EventBus
    }

    /**
     * 初始化事件控制，订阅RxBus缓存加载事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 从缓存恢复标题选择状态
     */
    private void setCache() {
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_INVOKE_STORE); // 获取缓存中保存的调用Tab索引
        invokeTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(invokeTitle, invokeTitle.getSelected(), false); // 触发页面切换
    }

    /**
     * 通过RxBus发送调用消息
     */
    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_INVOKE, msgInvoke); // 发送调用消息事件
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
     * 子页面详情消息发送监听器，当子页面状态变化时通知父级
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 创建监听器
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 子页面消息回调
            if (msgInvoke.getSaveTitle() == null) { // 如果消息中的标题为空
                msgInvoke.setSaveTitle(invokeTitle.getSelected()); // 使用当前选中标题
            }
            if (fragment.equals(wavLayout)) { // 如果是WAV页面
                msgInvoke.setSaveDetail(wavLayout.getSaveDetail()); // 设置WAV页面保存详情
                msgInvoke.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(csvLayout)) { // 如果是CSV页面
                msgInvoke.setSaveDetail(csvLayout.getSaveDetail()); // 设置CSV页面保存详情
                msgInvoke.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(settingLayout)) { // 如果是Setting页面
                msgInvoke.setSaveDetail(settingLayout.getSaveDetail()); // 设置Setting页面保存详情
                msgInvoke.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            }else if(fragment.equals(sessionLayout)) { // 如果是Session页面
                msgInvoke.setSaveDetail(sessionLayout.getSaveDetail()); // 设置Session页面保存详情
                msgInvoke.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            }
        }
    };

    /**
     * 处理标题选择变化，切换子Fragment的显示
     * @param view 触发的视图
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) {
        if (view.getId() == invokeTitle.getId()) { // 如果是标题栏触发的
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_STORE, String.valueOf(item.getIndex())); // 保存当前选中索引到缓存
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .hide(wavLayout) // 隐藏WAV页面
                    .hide(csvLayout) // 隐藏CSV页面
                    .hide(settingLayout) // 隐藏Setting页面
                    .hide(sessionLayout) // 隐藏Session页面
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引显示对应页面
                case DETAIL_INVOKE_WAV: // WAV页面
                    getChildFragmentManager().beginTransaction()
                            .show(wavLayout).commitAllowingStateLoss(); // 显示WAV页面
                    msgInvoke.setSaveTitle(item); // 设置消息标题
                    msgInvoke.setSaveDetail(wavLayout.getSaveDetail()); // 设置WAV保存详情
                    msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break;
                case DETAIL_INVOKE_CSV: // CSV页面
                    getChildFragmentManager().beginTransaction()
                            .show(csvLayout).commitAllowingStateLoss(); // 显示CSV页面
                    msgInvoke.setSaveTitle(item); // 设置消息标题
                    msgInvoke.setSaveDetail(csvLayout.getSaveDetail()); // 设置CSV保存详情
                    msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break;
                case DETAIL_INVOKE_RECOVERY: // Setting页面
                    getChildFragmentManager().beginTransaction()
                            .show(settingLayout).commitAllowingStateLoss(); // 显示Setting页面
                    msgInvoke.setSaveTitle(item); // 设置消息标题
                    msgInvoke.setSaveDetail(settingLayout.getSaveDetail()); // 设置Setting保存详情
                    msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break;
                case DETAIL_INVOKE_SESSION: // Session页面
                    getChildFragmentManager().beginTransaction()
                            .show(sessionLayout).commitAllowingStateLoss(); // 显示Session页面
                    msgInvoke.setSaveTitle(item); // 设置消息标题
                    msgInvoke.setSaveDetail(sessionLayout.getSaveDetail()); // 设置Session保存详情
                    msgInvoke.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break;
            }
        }
    }

    /**
     * 获取当前调用页面的选中索引
     * @return 当前选中的Tab索引（0=WAV, 1=CSV, 2=Setting, 3=Session）
     */
    public int getSaveIdx(){
        return invokeTitle.getSelected().getIndex(); // 返回当前选中项的索引
    }
}
