package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle

import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 截图设置 → Fragment页面                               │
 * │ 核心职责：管理截图的时间戳和屏幕反转设置                                    │
 * │ 架构设计：Fragment子类，通过RxBus监听缓存加载事件                          │
 * │ 数据流向：CacheUtil ↔ 本类UI ↔ 截图/显示模块                              │
 * │ 依赖关系：CacheUtil、RxBus、TopViewRadioGroup                              │
 * │ 使用场景：用户设置界面中截图相关设置子页面                                  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 截图设置Fragment，管理截图时间戳和屏幕反转选项。
 * <p>
 * 包含两个单选组：
 * <ul>
 *   <li>时间戳 - 截图是否包含时间戳</li>
 *   <li>屏幕反转 - 截图是否反转显示</li>
 * </ul>
 */
public class TopLayoutUsersetCapture extends Fragment {
    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 时间戳单选组 */
    private TopViewRadioGroup tvTimestamp; // 时间戳单选组
    /** 屏幕反转单选组 */
    private TopViewRadioGroup tvScreenInvert; // 屏幕反转单选组

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
        return inflater.inflate(R.layout.layout_usersetcapture, container, false); // 加载截图设置布局
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
        initControl(); // 初始化控制逻辑
    }

    /**
     * 初始化视图控件，设置单选组数据和监听器。
     *
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        tvTimestamp = (TopViewRadioGroup) view.findViewById(R.id.timestamp); // 查找时间戳单选组
        tvTimestamp.setData(R.string.timestamp, R.array.timestamp, onCheckChangedListener); // 设置时间戳数据
        tvScreenInvert = (TopViewRadioGroup) view.findViewById(R.id.screenInvert); // 查找屏幕反转单选组
        tvScreenInvert.setData(R.string.screenInvert, R.array.screenInvert, onCheckChangedListener); // 设置屏幕反转数据
    }

    /**
     * 初始化控制逻辑，订阅RxBus事件。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 从缓存恢复截图设置状态。
     */
    private void setCache() { // 从缓存恢复设置
        boolean timestamp = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP); // 读取时间戳缓存
        tvTimestamp.setSelectedIndex(timestamp ? 0 : 1); // 设置时间戳选中项，true=0(开)，false=1(关)
        boolean screenInvert = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT); // 读取屏幕反转缓存
        tvScreenInvert.setSelectedIndex(screenInvert ? 0 : 1); // 设置屏幕反转选中项
    }

    /** 缓存加载事件消费者 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override // 覆写accept方法
        public void accept(LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复设置
        }
    };

    /** 单选组选中变化监听器 */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组选中变化监听器
        @Override // 覆写点击回调
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变化回调
            if (view.getId() == R.id.timestamp) { // 时间戳变化
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_TIMESTAMP, String.valueOf(item.getIndex() == 0)); // 缓存时间戳状态，0=开
            } else if (view.getId() == R.id.screenInvert) { // 屏幕反转变化
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_SCREENINVERT, String.valueOf(item.getIndex() == 0)); // 缓存屏幕反转状态
            }
        }

        @Override // 覆写音效回调
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调（未使用）

        }

        @Override // 覆写拦截提示回调
        public void onPrompt(TopViewRadioGroup view) { // 拦截提示回调（未使用）

        }
    };
}
