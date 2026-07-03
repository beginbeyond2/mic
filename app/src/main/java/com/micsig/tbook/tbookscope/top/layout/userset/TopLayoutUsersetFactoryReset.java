package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Scope; // 导入示波器核心
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 出厂设置 → Fragment页面                               │
 * │ 核心职责：执行出厂设置恢复，清除所有用户配置并恢复默认值                    │
 * │ 架构设计：Fragment子类，通过RxBus监听SCPI命令触发出厂设置                  │
 * │ 数据流向：按钮点击/SCPI命令 → 清除缓存 → 恢复默认配置 → 刷新界面          │
 * │ 依赖关系：Scope、CacheUtil、HorizontalAxis、MainActivity                   │
 * │ 使用场景：用户设置界面中"出厂设置"子页面，点击按钮恢复出厂配置             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 出厂设置Fragment，提供恢复出厂设置功能。
 * <p>
 * 点击"出厂设置"按钮后：
 * <ol>
 *   <li>禁用命令发送</li>
 *   <li>清除所有缓存配置</li>
 *   <li>初始化默认缓存状态</li>
 *   <li>重置水平轴时基为2ms</li>
 *   <li>重新加载所有配置并刷新界面</li>
 * </ol>
 *
 * @author Administrator
 * @since 2017/4/11
 */
public class TopLayoutUsersetFactoryReset extends Fragment {
    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 出厂设置按钮 */
    private Button factory; // 出厂设置按钮

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
        return inflater.inflate(R.layout.layout_usersetfactoryreset, container, false); // 加载出厂设置布局
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
        initData(); // 初始化数据
        initControl(); // 初始化控制逻辑
    }

    /**
     * 初始化控制逻辑，订阅RxBus命令消息。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令消息
    }

    /**
     * 初始化视图控件，绑定按钮点击监听器。
     *
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        factory = (Button) view.findViewById(R.id.factory); // 查找出厂设置按钮
        factory.setOnClickListener(onClickListener); // 设置按钮点击监听
    }

    /**
     * 初始化数据（当前为空实现）。
     */
    private void initData() { // 初始化数据
    }

    /** 命令消息到UI消费者 */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令消息到UI消费者
        @Override // 覆写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_USERSET_FACTORYRESET: // 出厂设置命令
                    onClickListener.onClick(factory); // 模拟按钮点击
                    break; // 结束分支
            }
        }
    };

    /** 出厂设置按钮点击监听器 */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 出厂设置按钮点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击出厂设置按钮
            PlaySound.getInstance().playButton(); // 播放按钮音效

            Scope.getInstance().enableCommand(false); // 禁用命令发送
            CacheUtil.get().clearCacheMap(); // 清除缓存配置
            CacheUtil.get().clearOtherMap(); // 清除其他配置
            CacheUtil.get().initStateCacheLoad(); // 初始化默认缓存状态
            CacheUtil.get().setClickFactoryReset(true); // 标记已点击出厂设置

            ((MainActivity) context).preMainLoadCahceProcess(); // 预加载缓存处理
            { // 重置水平轴时基

                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS); // 设置时基为2ms
                horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0); // 设置时基位置为0
//                horizontalAxis.correctTimePose();
//                if (Scope.getInstance().isZoom()) {
//                    WaveManage.get().setPositionY(IWave.Ch1, GlobalVar.get().getMainWave().y / 5 - 20);
//                    WaveManage.get().setPositionY(IWave.Ch2, GlobalVar.get().getMainWave().y / 5 * 2 - 40);
//                    WaveManage.get().setPositionY(IWave.Ch3, GlobalVar.get().getMainWave().y / 5 * 3 - 60);
//                    WaveManage.get().setPositionY(IWave.Ch4, GlobalVar.get().getMainWave().y / 5 * 4 - 80);
//                    Command.get().getDisplay().Zoom(false, true);
//                }
//                ExternalKeysProtocol.closeShift();
            }
            ((MainActivity) context).updateMainLoadCaheProcess(false); // 更新缓存加载处理
            ((MainActivity) context).postMainLoadCacheProcess(); // 后置缓存加载处理
        }
    };
}
