package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.annotation.SuppressLint; // 导入抑制Lint警告注解
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.CheckBox; // 导入复选框控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入串口消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - TXT Mix子页面Fragment                          ║
 * ║  核心职责: 管理Display-TXT Mix页面的串口文本组合选择UI交互和状态同步          ║
 * ║  架构设计: 子Fragment，通过OnDetailSendMsgListener向父Fragment传递数据       ║
 * ║  数据流向: CheckBox → onClickListener → CacheUtil → sendMsg → 父Fragment ║
 * ║  依赖关系: RxBus、CacheUtil、TChan、RightMsgSerials                       ║
 * ║  使用场景: 用户在Display-TXT Mix页面选择S1~S4串口文本组合时携带数据           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by limh on 2024/8/8.
 */

public class TopLayoutDisplayTxtMix extends Fragment { // TXT Mix显示子页面Fragment，管理S1~S4串口文本组合选择

    private Context context; // Fragment关联的Activity上下文
    private CheckBox checkS1, checkS2, checkS3, checkS4; // S1~S4串口通道复选框
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器
    private ViewGroup rootView; // 根视图组引用
    private TopMsgDisplayTxtMix displayDetail; // TXT Mix显示详情数据模型

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
        return inflater.inflate(R.layout.layout_displaytxtmix, container, false); // 加载TXT Mix布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取关联的Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件，绑定S1~S4复选框和点击监听器
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图
        rootView = (ViewGroup) view; // 保存根视图引用
        checkS1 = (CheckBox) view.findViewById(R.id.check_s1); // 获取S1复选框
        checkS2 = (CheckBox) view.findViewById(R.id.check_s2); // 获取S2复选框
        checkS3 = (CheckBox) view.findViewById(R.id.check_s3); // 获取S3复选框
        checkS4 = (CheckBox) view.findViewById(R.id.check_s4); // 获取S4复选框

        checkS1.setOnClickListener(onClickListener); // 设置S1点击监听器
        checkS2.setOnClickListener(onClickListener); // 设置S2点击监听器
        checkS3.setOnClickListener(onClickListener); // 设置S3点击监听器
        checkS4.setOnClickListener(onClickListener); // 设置S4点击监听器
    }

    /**
     * 初始化数据，从UI控件读取当前值并填充到详情数据模型
     */
    private void initData() { // 初始化数据
        displayDetail = new TopMsgDisplayTxtMix(); // 创建TXT Mix详情对象
        displayDetail.setS1Select(checkS1.isChecked()); // 设置S1选择状态
        displayDetail.setS2Select(checkS2.isChecked()); // 设置S2选择状态
        displayDetail.setS3Select(checkS3.isChecked()); // 设置S3选择状态
        displayDetail.setS4Select(checkS4.isChecked()); // 设置S4选择状态
    }

    /**
     * 初始化事件控制，订阅缓存加载、右侧其他和串口消息事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther); // 订阅右侧其他事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials); // 订阅串口消息事件
    }


    /**
     * 从缓存恢复TXT Mix页面的配置状态
     */
    private void setCache() { // 设置缓存
        //是否选择了Txt组合
        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1); // 获取缓存的S1选择状态
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2); // 获取缓存的S2选择状态
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3); // 获取缓存的S3选择状态
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4); // 获取缓存的S4选择状态
        checkS1.setChecked(s1Select); // 设置S1复选框状态
        checkS2.setChecked(s2Select); // 设置S2复选框状态
        checkS3.setChecked(s3Select); // 设置S3复选框状态
        checkS4.setChecked(s4Select); // 设置S4复选框状态

        //通道是否打开，未打开时置灰
        boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1); // 获取S1通道打开状态
        boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2); // 获取S2通道打开状态
        boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3); // 获取S3通道打开状态
        boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4); // 获取S4通道打开状态
        //全部可点击
        checkS1.setEnabled(true); // S1复选框可点击
        checkS2.setEnabled(true); // S2复选框可点击
        checkS3.setEnabled(true); // S3复选框可点击
        checkS4.setEnabled(true); // S4复选框可点击
        //更新数据
        displayDetail.setS1Select(s1Select); // 更新详情数据-S1选择状态
        displayDetail.setS2Select(s2Select); // 更新详情数据-S2选择状态
        displayDetail.setS3Select(s3Select); // 更新详情数据-S3选择状态
        displayDetail.setS4Select(s4Select); // 更新详情数据-S4选择状态
        sendMsg(false); // 发送消息
    }

    /**
     * 通过监听器发送消息到父Fragment
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息
        Tools.PrintControlsLocation("TopLayoutDisplayTxtMix", rootView); // 打印控件位置调试信息
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 获取TXT Mix显示详情数据
     * @return TXT Mix显示详情数据模型
     */
    public TopMsgDisplayTxtMix getDisplayDetail() { // 获取详情数据
        return displayDetail; // 返回详情数据
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器
    }

    /**
     * 复选框点击监听器，更新选择状态和缓存并发送消息
     */
    @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
    private final View.OnClickListener onClickListener = v -> { // 复选框点击监听器（Lambda表达式）
        switch (v.getId()) { // 根据视图ID分发
            case R.id.check_s1: // S1复选框
                displayDetail.setS1Select(checkS1.isChecked()); // 更新详情数据-S1选择状态
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1, String.valueOf(checkS1.isChecked())); // 缓存S1选择状态
                break; // 退出case
            case R.id.check_s2: // S2复选框
                displayDetail.setS2Select(checkS2.isChecked()); // 更新详情数据-S2选择状态
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2, String.valueOf(checkS2.isChecked())); // 缓存S2选择状态
                break; // 退出case
            case R.id.check_s3: // S3复选框
                displayDetail.setS3Select(checkS3.isChecked()); // 更新详情数据-S3选择状态
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3, String.valueOf(checkS3.isChecked())); // 缓存S3选择状态
                break; // 退出case
            case R.id.check_s4: // S4复选框
                displayDetail.setS4Select(checkS4.isChecked()); // 更新详情数据-S4选择状态
                CacheUtil.get().putMap(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4, String.valueOf(checkS4.isChecked())); // 缓存S4选择状态
                break; // 退出case
        }
        sendMsg(false); // 发送消息
    };

    /**
     * 缓存加载事件消费者，恢复缓存状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override // 重写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载事件
            setCache(); // 恢复缓存状态
        }
    };

    /**
     * 右侧其他消息消费者，刷新缓存状态（通道开关变化时）
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() { // 右侧其他消息消费者
        @Override // 重写accept方法
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception { // 处理右侧其他消息
            setCache(); // 刷新缓存状态
        }
    };

    /**
     * 串口消息消费者，刷新缓存状态（串口配置变化时）
     */
    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() { // 串口消息消费者
        @Override // 重写accept方法
        public void accept(RightMsgSerials rightMsgSerials) throws Exception { // 处理串口消息
            setCache(); // 刷新缓存状态
        }
    };

}
