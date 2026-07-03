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
 * Created by Administrator on 2017/4/6.
 */
/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 保存子容器（Store Container）            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：作为保存功能的子Fragment容器，管理WAV/CSV/BIN/Setting/Picture/      │
 * │          Session六个子Tab页面的切换显示，通过Fragment事务控制子页面的显示和隐藏  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，使用TopViewTitleWithScroll作为Tab标题栏，              │
 * │          通过getChildFragmentManager管理子Fragment的添加/显示/隐藏，           │
 * │          通过OnDetailSendMsgListener将子页面消息传递给父级                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(缓存索引) → saveTitle(标题选择) →                       │
 * │          Fragment事务(切换子页面) → 子Fragment(Wav/Csv/Bin/Setting/           │
 * │          Picture/Session) → OnDetailSendMsgListener →                        │
 * │          RxBus(TOPLAYOUT_SAVE)                                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewTitleWithScroll, TopLayoutSaveWav, TopLayoutSaveCsv,       │
 * │          TopLayoutSaveBin, TopLayoutSaveSetting, TopLayoutSavePicture,      │
 * │          TopLayoutSaveSession, CacheUtil, RxBus                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在保存菜单选择"保存"Tab时显示此容器，                             │
 * │          通过子Tab切换WAV/CSV/BIN/设置/图片/会话六种保存类型                   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutSaveStore extends Fragment {
    /** 保存到本地存储的常量索引 */
    public static final int SAVEINLOCAL = 0;// Tools.SaveType_LOCAL // 保存到本地
    /** 保存到U盘的常量索引 */
    public static final int SAVEINUDISK = 1;// Tools.SaveType_UDISK // 保存到U盘

    /** 子页面索引：保存WAV波形 */
    public static final int DETAIL_WAV = 0; // WAV保存页面索引
    /** 子页面索引：保存CSV数据 */
    public static final int DETAIL_CSV = 1; // CSV保存页面索引
    /** 子页面索引：保存BIN数据 */
    public static final int DETAIL_BIN = 2; // BIN保存页面索引
    /** 子页面索引：保存设置 */
    public static final int DETAIL_SETTING = 3; // 设置保存页面索引
    /** 子页面索引：保存图片 */
    public static final int DETAIL_PICTURE = 4; // 图片保存页面索引
    /** 子页面索引：保存会话 */
    public static final int DETAIL_SESSION = 5; // 会话保存页面索引

    /** Fragment所在的上下文环境 */
    private Context context; // Activity上下文
    /** 保存详情容器布局，用于承载子Fragment */
    private RelativeLayout saveDetail; // 详情容器布局
    /** 可滚动的标题栏，用于切换WAV/CSV/BIN/Setting/Picture/Session */
    private TopViewTitleWithScroll saveTitle; // 可滚动标题栏
    /** WAV保存子页面 */
    private TopLayoutSaveWav saveWaveLayout;               //save wave // WAV保存页面
    /** CSV保存子页面 */
    private TopLayoutSaveCsv saveCsvLayout;                //save csv // CSV保存页面
    /** BIN保存子页面 */
    private TopLayoutSaveBin saveBinLayout;                //save bin // BIN保存页面
    /** 设置保存子页面 */
    private TopLayoutSaveSetting saveSettingLayout;        //setting // 设置保存页面
    /** 图片保存子页面 */
    private TopLayoutSavePicture savePictureLayout;        //picture // 图片保存页面
    /** 会话保存子页面 */
    private TopLayoutSaveSession sessionLayout;            //state // 会话保存页面

    /** 保存消息对象，用于向父级传递当前保存状态 */
    private TopMsgSaveStore msgSave; // 保存消息对象

    /** 子Fragment的Tag标签数组，用于Fragment事务查找 */
    private final String[] tags = {"WavLayout", "CsvLayout", "BinLayout", "SettingLayout", "PictureLayout", "StateLayout"}; // 子Fragment标签数组
    /** 子Fragment数组，保存六个子页面的引用 */
    private Fragment[] fragments = new Fragment[6]; // 子Fragment数组

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的保存子容器布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_store, container, false); // 填充保存子容器布局
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

        saveWaveLayout = fragments[0] == null ? new TopLayoutSaveWav() : (TopLayoutSaveWav) fragments[0]; // 创建或恢复WAV页面
        saveCsvLayout = fragments[1] == null ? new TopLayoutSaveCsv() : (TopLayoutSaveCsv) fragments[1]; // 创建或恢复CSV页面
        saveBinLayout = fragments[2] == null ? new TopLayoutSaveBin() : (TopLayoutSaveBin) fragments[2]; // 创建或恢复BIN页面
        saveSettingLayout = fragments[3] == null ? new TopLayoutSaveSetting() : (TopLayoutSaveSetting) fragments[3]; // 创建或恢复Setting页面
        savePictureLayout = fragments[4] == null ? new TopLayoutSavePicture() : (TopLayoutSavePicture) fragments[4]; // 创建或恢复Picture页面
        sessionLayout = fragments[5] == null ? new TopLayoutSaveSession() : (TopLayoutSaveSession) fragments[5]; // 创建或恢复Session页面
        if (savedInstanceState == null) { // 如果是首次创建（非恢复）
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .add(R.id.saveDetail, saveWaveLayout, tags[0]) // 添加WAV页面
                    .add(R.id.saveDetail, saveCsvLayout, tags[1]) // 添加CSV页面
                    .add(R.id.saveDetail, saveBinLayout, tags[2]) // 添加BIN页面
                    .add(R.id.saveDetail, saveSettingLayout, tags[3]) // 添加Setting页面
                    .add(R.id.saveDetail, savePictureLayout, tags[4]) // 添加Picture页面
                    .add(R.id.saveDetail, sessionLayout, tags[5]) // 添加Session页面
                    .hide(saveCsvLayout) // 隐藏CSV页面
                    .hide(saveBinLayout) // 隐藏BIN页面
                    .hide(saveSettingLayout) // 隐藏Setting页面
                    .hide(savePictureLayout) // 隐藏Picture页面
                    .hide(sessionLayout) // 隐藏Session页面
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        saveDetail = (RelativeLayout) view.findViewById(R.id.saveDetail); // 获取详情容器布局
        saveTitle = (TopViewTitleWithScroll) view.findViewById(R.id.saveTitle); // 获取标题栏
        String[] array = context.getResources().getStringArray(R.array.topSaveType); // 获取保存类型标题数组
        boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
        for (int i = 0; i < array.length; i++) { // 遍历所有标题
            arrayVisible[i] = true; // 全部设为可见
        }
        saveTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据、选择监听器和点击监听器

        saveWaveLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置WAV页面消息监听器
        saveCsvLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置CSV页面消息监听器
        saveBinLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置BIN页面消息监听器
        saveSettingLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置Setting页面消息监听器
        savePictureLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置Picture页面消息监听器
        sessionLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置Session页面消息监听器

        msgSave = new TopMsgSaveStore(); // 创建保存消息对象
        msgSave.setSaveTitle(saveTitle.getSelected()); // 设置当前选中的标题
        msgSave.setSaveDetail(saveWaveLayout.getSaveDetail()); // 设置当前WAV页面的保存详情
        msgSave.setFromEventBus(false); // 标记非来自EventBus
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
        int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE_STORE); // 获取缓存中保存的保存Tab索引
        saveTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(saveTitle, saveTitle.getSelected(), false); // 触发页面切换
    }

    /**
     * 通过RxBus发送保存消息
     */
    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAVE, msgSave); // 发送保存消息事件
    }

    /** 缓存加载事件的RxJava消费者，恢复缓存状态 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSave, true); // 标记此菜单已加载缓存
        }
    };

    /** 标题项点击监听器 */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 创建点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }
    };

    /** 标题选择变化监听器 */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 创建选择变化监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选择变化回调
            onCheckChanged(view, item, false); // 委托给处理方法
        }
    };

    /** 子页面详情消息发送监听器，当子页面状态变化时通知父级 */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 创建监听器
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 子页面消息回调
            if (msgSave.getSaveTitle() == null) { // 如果消息中的标题为空
                msgSave.setSaveTitle(saveTitle.getSelected()); // 使用当前选中标题
            }
            if (fragment.equals(saveWaveLayout)) { // 如果是WAV页面
                msgSave.setSaveDetail(saveWaveLayout.getSaveDetail()); // 设置WAV页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(saveCsvLayout)) { // 如果是CSV页面
                msgSave.setSaveDetail(saveCsvLayout.getSaveDetail()); // 设置CSV页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(saveBinLayout)) { // 如果是BIN页面
                msgSave.setSaveDetail(saveBinLayout.getSaveDetail()); // 设置BIN页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(saveSettingLayout)) { // 如果是Setting页面
                msgSave.setSaveDetail(saveSettingLayout.getSaveDetail()); // 设置Setting页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if (fragment.equals(savePictureLayout)) { // 如果是Picture页面
                msgSave.setSaveDetail(savePictureLayout.getSaveDetail()); // 设置Picture页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送消息
            } else if(fragment.equals(sessionLayout)) { // 如果是Session页面
                msgSave.setSaveDetail(sessionLayout.getSaveDetail()); // 设置Session页面保存详情
                msgSave.getSaveTitle().setRxMsgSelect(false); // 取消Rx消息选中
                msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
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
        if (view.getId() == saveTitle.getId()) { // 如果是标题栏触发的
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_STORE, String.valueOf(item.getIndex())); // 保存当前选中索引到缓存
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .hide(saveWaveLayout) // 隐藏WAV页面
                    .hide(saveCsvLayout) // 隐藏CSV页面
                    .hide(saveBinLayout) // 隐藏BIN页面
                    .hide(saveSettingLayout) // 隐藏Setting页面
                    .hide(savePictureLayout) // 隐藏Picture页面
                    .hide(sessionLayout) // 隐藏Session页面
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引显示对应页面
                case DETAIL_WAV: // WAV页面
                    getChildFragmentManager().beginTransaction()
                            .show(saveWaveLayout).commitAllowingStateLoss(); // 显示WAV页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(saveWaveLayout.getSaveDetail()); // 设置WAV保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(0)); // 保存类型为WAV
                    sendMsg(); // 发送消息
                    break; // 结束WAV处理
                case DETAIL_CSV: // CSV页面
                    getChildFragmentManager().beginTransaction()
                            .show(saveCsvLayout).commitAllowingStateLoss(); // 显示CSV页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(saveCsvLayout.getSaveDetail()); // 设置CSV保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(1)); // 保存类型为CSV
                    sendMsg(); // 发送消息
                    break; // 结束CSV处理
                case DETAIL_BIN: // BIN页面
                    getChildFragmentManager().beginTransaction()
                            .show(saveBinLayout).commitAllowingStateLoss(); // 显示BIN页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(saveBinLayout.getSaveDetail()); // 设置BIN保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(2)); // 保存类型为BIN
                    sendMsg(); // 发送消息
                    break; // 结束BIN处理
                case DETAIL_SETTING: // Setting页面
                    getChildFragmentManager().beginTransaction()
                            .show(saveSettingLayout).commitAllowingStateLoss(); // 显示Setting页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(saveSettingLayout.getSaveDetail()); // 设置Setting保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break; // 结束Setting处理
                case DETAIL_PICTURE: // Picture页面
                    getChildFragmentManager().beginTransaction()
                            .show(savePictureLayout).commitAllowingStateLoss(); // 显示Picture页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(savePictureLayout.getSaveDetail()); // 设置Picture保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break; // 结束Picture处理
                case DETAIL_SESSION: // Session页面
                    getChildFragmentManager().beginTransaction()
                            .show(sessionLayout).commitAllowingStateLoss(); // 显示Session页面
                    msgSave.setSaveTitle(item); // 设置消息标题
                    msgSave.setSaveDetail(sessionLayout.getSaveDetail()); // 设置Session保存详情
                    msgSave.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                    sendMsg(); // 发送消息
                    break; // 结束Session处理
            }
        }
    }

    /**
     * 获取当前保存Tab的选中索引
     * @return 选中索引
     */
    public int getSaveIdx(){
        return saveTitle.getSelected().getIndex(); // 返回当前选中项的索引
    }
}
