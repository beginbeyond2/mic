package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.os.Build; // 导入Build系统信息
import android.os.Bundle; // 导入Bundle
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RelativeLayout; // 导入相对布局

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入标题视图
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入NonNull注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer


/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 主容器 → Fragment页面                                 │
 * │ 核心职责：管理用户设置页面的标题导航和子Fragment切换                        │
 * │ 架构设计：容器Fragment，通过FragmentManager管理5个子Fragment的显示/隐藏    │
 * │ 数据流向：TopViewTitle → 本类 → 子Fragment切换 → TopMsgUserset消息        │
 * │ 依赖关系：5个子Fragment、TopMsgUserset、RxBus、CacheUtil                   │
 * │ 使用场景：用户设置主页面，提供出厂设置/自校准/无线键盘/触发时钟/参考时基   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 用户设置主容器Fragment，管理标题导航和子Fragment切换。
 * <p>
 * 包含5个子页面：
 * <ul>
 *   <li>出厂设置 (FactoryReset)</li>
 *   <li>自动校准 (SelfAdjust)</li>
 *   <li>无线键盘 (WirelessKeyboard) - 仅ETO型号显示</li>
 *   <li>触发/时钟输入输出 (UserSetAuxOut)</li>
 *   <li>参考时基调节 (RefTimeBase)</li>
 * </ul>
 *
 * @author Administrator
 * @since 2017/4/11
 */
public class TopLayoutUserset extends Fragment {

    /** 子页面索引：出厂设置 */
    public static final int DETAIL_FACTORYRESET = 0; // 子页面索引：出厂设置
    /** 子页面索引：自动校准 */
    public static final int DETAIL_SELFADJUST = 1; // 子页面索引：自动校准
    /** 子页面索引：无线键盘 */
    public static final int DETAIL_WIRELESSKEYBOARD= 2; // 子页面索引：无线键盘
    /** 子页面索引：触发/时钟输入输出 */
    public static final int DETAIL_USERSETAUXOUT = 3; // 子页面索引：触发/时钟输入输出
    /** 子页面索引：参考时基调节 */
    public static final int DETAIL_REF_TIMEBASE = 4; // 子页面索引：参考时基调节

    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 标题导航视图（带滚动） */
    private TopViewTitleWithScroll settingTitle; // 标题导航视图
    /** 子Fragment容器 */
    private RelativeLayout settingDetail; // 子Fragment容器

    /** 出厂设置子Fragment */
    private TopLayoutUsersetFactoryReset layoutFactoryReset;    //出厂设置 // 出厂设置子Fragment
    /** 自动校准子Fragment */
    private TopLayoutUsersetSelfAdjust layoutSelfAdjust;        //自动校准 // 自动校准子Fragment
    /** 无线键盘子Fragment */
    private TopLayoutUsersetWirelessKeyBoard layoutWireless;    //无线键盘 // 无线键盘子Fragment
    /** 触发/时钟输入输出子Fragment */
    private TopLayoutUserSetAuxOut layoutAuxOut;                //触发/时钟输入输出 // 触发/时钟输入输出子Fragment
    /** 参考时基调节子Fragment */
    private TopLayoutUserSetRefTimeBase layoutRefTimeBase;      //Ref时基调节 // 参考时基调节子Fragment



    /** 用户设置消息对象 */
    private TopMsgUserset msgUserset; // 用户设置消息对象

    /** 子Fragment标签数组 */
    private String[] tags = {"FactoryReset", "SelfAdjust", "WirelessKeyboard", "UserSetAuxOut", "RefTimeBase"}; // 子Fragment标签数组
    /** 子Fragment数组 */
    private Fragment[] fragments = new Fragment[5]; // 子Fragment数组

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
        return inflater.inflate(R.layout.layout_userset, container, false); // 加载用户设置布局
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
        initView(view, savedInstanceState); // 初始化视图
        initControl(); // 初始化控制逻辑
    }

    /**
     * 初始化视图控件，设置标题导航和子Fragment。
     *
     * @param view               根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图
        settingDetail = (RelativeLayout) view.findViewById(R.id.settingDetail); // 查找子Fragment容器
        settingTitle = (TopViewTitleWithScroll) view.findViewById(R.id.settingTitle); // 查找标题导航视图

        String[] array = context.getResources().getStringArray(R.array.setting); // 获取设置项标题数组
        boolean[] settingVisible = new boolean[array.length]; // 创建可见性数组
        settingVisible[0] = true; // 出厂设置可见
        settingVisible[1] = true; // 自动校准可见
        settingVisible[2] = Build.MODEL.equalsIgnoreCase("ETO"); // 无线键盘仅ETO型号可见
        settingVisible[3] = true; // 触发/时钟输入输出可见
        settingVisible[4] = true; // 参考时基调节可见
        settingTitle.setData(array, settingVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据

        if (savedInstanceState != null) { // 有保存的实例状态
            for (int i = 0; i < tags.length; i++) { // 遍历标签数组
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 从FragmentManager恢复Fragment
            }
        }

        layoutFactoryReset = fragments[0] == null ? new TopLayoutUsersetFactoryReset() : (TopLayoutUsersetFactoryReset) fragments[0]; // 初始化或恢复出厂设置Fragment
        layoutSelfAdjust = fragments[1] == null ? new TopLayoutUsersetSelfAdjust() : (TopLayoutUsersetSelfAdjust) fragments[1]; // 初始化或恢复自动校准Fragment
        layoutWireless = fragments[2] == null ? new TopLayoutUsersetWirelessKeyBoard() : (TopLayoutUsersetWirelessKeyBoard) fragments[2]; // 初始化或恢复无线键盘Fragment
        layoutAuxOut = fragments[3] == null ? new TopLayoutUserSetAuxOut() : (TopLayoutUserSetAuxOut) fragments[3]; // 初始化或恢复触发/时钟Fragment
        layoutRefTimeBase = fragments[4] == null ? new TopLayoutUserSetRefTimeBase() : (TopLayoutUserSetRefTimeBase) fragments[4]; // 初始化或恢复参考时基Fragment
        if (savedInstanceState == null) { // 首次创建
            getChildFragmentManager().beginTransaction() // 开启Fragment事务

                    .add(R.id.settingDetail, layoutFactoryReset, tags[0]) // 添加出厂设置Fragment
                    .add(R.id.settingDetail, layoutSelfAdjust, tags[1]) // 添加自动校准Fragment
                    .add(R.id.settingDetail, layoutWireless, tags[2]) // 添加无线键盘Fragment
                    .add(R.id.settingDetail, layoutAuxOut, tags[3]) // 添加触发/时钟Fragment
                    .add(R.id.settingDetail, layoutRefTimeBase, tags[4]) // 添加参考时基Fragment
                    .hide(layoutFactoryReset) // 隐藏出厂设置
                    .hide(layoutSelfAdjust) // 隐藏自动校准
                    .hide(layoutWireless) // 隐藏无线键盘
                    .hide(layoutAuxOut) // 隐藏触发/时钟
                    .hide(layoutRefTimeBase) // 隐藏参考时基
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        msgUserset = new TopMsgUserset(); // 创建用户设置消息对象
        msgUserset.setUsersetTitle(settingTitle.getSelected()); // 设置当前选中标题
    }

    /**
     * 初始化控制逻辑，订阅RxBus事件。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTaskState); // 订阅自动保存按钮状态更新
    }

    /**
     * 从缓存恢复用户设置选中状态。
     */
    private void setCache() { // 从缓存恢复设置
        int userset = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET); // 读取用户设置选中索引
        settingTitle.setSelected(userset); // 设置标题选中项
        onCheckChangedTitleListener.checkChanged(settingTitle, settingTitle.getSelected()); // 触发标题切换
    }

    /**
     * 发送用户设置消息到RxBus。
     */
    private void sendMsg() { // 发送用户设置消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_USERSET, msgUserset); // 发送用户设置消息事件
    }

    /** 缓存加载事件消费者 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override // 覆写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复设置
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutUserset, true); // 标记本模块缓存已加载
        }
    };

    /** 标题项点击监听器（播放音效） */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题项点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击标题项
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }
    };

    /** 标题选中变化监听器，切换子Fragment显示 */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题选中变化监听器
        @Override // 覆写标题切换回调
        public void checkChanged(View view, TopAllBeanTitle item) { // 标题选中变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET, String.valueOf(item.getIndex())); // 缓存选中索引
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .hide(layoutFactoryReset) // 隐藏出厂设置
                    .hide(layoutSelfAdjust) // 隐藏自动校准
                    .hide(layoutWireless) // 隐藏无线键盘
                    .hide(layoutAuxOut) // 隐藏触发/时钟
                    .hide(layoutRefTimeBase) // 隐藏参考时基
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引显示对应Fragment
                case DETAIL_FACTORYRESET: // 出厂设置
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(layoutFactoryReset).commitAllowingStateLoss(); // 显示出厂设置
                    msgUserset.setUsersetTitle(item); // 更新消息对象
                    sendMsg(); // 发送消息
                    break; // 结束分支
                case DETAIL_SELFADJUST: // 自动校准
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(layoutSelfAdjust).commitAllowingStateLoss(); // 显示自动校准
                    msgUserset.setUsersetTitle(item); // 更新消息对象
                    sendMsg(); // 发送消息
                    break; // 结束分支
                case DETAIL_WIRELESSKEYBOARD: // 无线键盘
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(layoutWireless).commitAllowingStateLoss(); // 显示无线键盘
                    msgUserset.setUsersetTitle(item); // 更新消息对象
                    sendMsg(); // 发送消息
                    break; // 结束分支
                case DETAIL_USERSETAUXOUT: // 触发/时钟输入输出
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(layoutAuxOut).commitAllowingStateLoss(); // 显示触发/时钟
                    msgUserset.setUsersetTitle(item); // 更新消息对象
                    sendMsg(); // 发送消息
                    break; // 结束分支
                case DETAIL_REF_TIMEBASE: // 参考时基调节
                    getChildFragmentManager().beginTransaction() // 开启事务
                            .show(layoutRefTimeBase).commitAllowingStateLoss(); // 显示参考时基
                    msgUserset.setUsersetTitle(item); // 更新消息对象
                    sendMsg(); // 发送消息
                    break; // 结束分支
            }
        }
    };

    /**
     * 获取当前用户设置选中索引。
     *
     * @return 选中项索引
     */
    public int getUserIdx(){ // 获取当前选中索引
        return settingTitle.getSelected().getIndex(); // 返回标题选中项索引
    }

    /** 自动保存任务状态消费者 */
    private Consumer<Boolean> consumerAutoSaveTaskState = new Consumer<Boolean>() { // 自动保存任务状态消费者
        @Override // 覆写accept方法
        public void accept(Boolean setState) throws Throwable { // 接收自动保存状态
            requireActivity().runOnUiThread(new Runnable() { // 在UI线程执行
                @Override // 覆写run方法
                public void run() { // UI线程执行体
                    if (settingTitle.getSelected().getIndex() == DETAIL_SELFADJUST) { // 当前选中的是自动校准
                        settingTitle.setSelected(DETAIL_FACTORYRESET); // 切换到出厂设置
                    }
                    settingTitle.setEnable(DETAIL_SELFADJUST, setState); // 设置自动校准项是否可用
                }
            });
        }
    };
}
