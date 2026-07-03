package com.micsig.tbook.tbookscope.tools;  // 屏幕控制工具类所在包

import android.annotation.SuppressLint;  // 用于抑制lint警告的注解
import android.content.Context;  // Android上下文，用于获取资源和系统服务
import android.os.Handler;  // Android消息处理器，用于线程间通信
import android.os.Message;  // Android消息对象，用于Handler传递消息
import android.util.Log;  // Android日志工具
import android.view.MotionEvent;  // 触摸事件对象
import android.view.View;  // Android视图基类
import android.widget.FrameLayout;  // 帧布局容器
import android.widget.TextView;  // 文本视图控件

import com.micsig.tbook.tbookscope.MainActivity;  // 主Activity
import com.micsig.tbook.tbookscope.MainViewGroup;  // 主视图容器
import com.micsig.tbook.tbookscope.R;  // 资源ID常量类
import com.micsig.tbook.ui.MProgress;  // 自定义进度条控件

import java.util.HashMap;  // 哈希映射集合
import java.util.Map;  // Map接口
import java.util.concurrent.atomic.AtomicInteger;  // 原子整数，用于线程安全计数

/**
 * Created by liwb on 2018/5/24.
 * <pre class="prettyprint">
 *  ScreenControls:是一个屏幕控制类，控制触摸屏是否锁屏，外部按键是否锁死等一些操作。
 * </pre>
 */

/*
 * +=============================================================================================================+
 * |                                          ScreenControls 屏幕控制类                                            |
 * +=============================================================================================================+
 * | 模块定位：tbookscope.tools 工具模块                                                                           |
 * |-------------------------------------------------------------------------------------------------------------|
 * | 核心职责：                                                                                                    |
 * |   1. 管理屏幕锁定/解锁状态（基于位标志实现多种锁定原因的叠加）                                                      |
 * |   2. 控制触摸屏遮罩层的显示与隐藏                                                                              |
 * |   3. 管理外部按键锁定状态                                                                                      |
 * |   4. 控制进度条（CSV加载进度、自校准、出厂校准等）的显示                                                           |
 * |   5. 通过Handler在UI线程上更新视图状态                                                                          |
 * |-------------------------------------------------------------------------------------------------------------|
 * | 架构设计：                                                                                                    |
 * |   - 单例模式（静态内部类持有者方式），保证全局唯一实例                                                             |
 * |   - 位标志（Bitmask）机制管理多种锁定原因，支持多个锁定源同时存在                                                   |
 * |   - Handler消息驱动，确保视图更新在主线程执行                                                                    |
 * |   - 观察者模式（IScreenLockListener），通知外部锁定状态变化                                                       |
 * |-------------------------------------------------------------------------------------------------------------|
 * | 数据流向：                                                                                                    |
 * |   外部模块 → lockScreen()/unLockScreen() → lockflag位标志更新 → Handler消息 → processLockScreen() → UI更新      |
 * |   CSV加载 → csvupdate() → 进度条更新 → progress.post() → UI线程刷新                                              |
 * |   触摸事件 → setMaskLayerLayoutOnTouchListener() → 遮罩层触摸反馈                                                |
 * |-------------------------------------------------------------------------------------------------------------|
 * | 依赖关系：                                                                                                    |
 * |   - MainViewGroup：主视图容器，提供遮罩层布局                                                                   |
 * |   - MainActivity：主Activity，用于获取提示文本视图                                                               |
 * |   - MProgress：自定义进度条控件                                                                                |
 * |   - IScreenLockListener：屏幕锁定状态监听器接口                                                                 |
 * |-------------------------------------------------------------------------------------------------------------|
 * | 使用场景：                                                                                                    |
 * |   - CSV文件加载时锁定屏幕并显示进度                                                                             |
 * |   - 自校准/出厂校准过程中锁定屏幕防止误操作                                                                      |
 * |   - 探头操作时锁定屏幕                                                                                         |
 * |   - 外部按键锁定/解锁                                                                                          |
 * +=============================================================================================================+
 */
public class ScreenControls {
    private static final String TAG="ScreenControls";  // 日志标签，用于Log输出标识

    //region  单例
    /**
     * 静态内部类持有者，用于实现线程安全的懒加载单例模式。
     * JVM在加载ScreenControlsHolder时才会创建实例，保证了线程安全和延迟加载。
     */
    private static class ScreenControlsHolder {
        public static final ScreenControls instance = new ScreenControls();  // 单例实例，类加载时创建
    }

    /**
     * 获取ScreenControls的单例实例。
     *
     * @return ScreenControls的全局唯一实例
     */
    public static ScreenControls getInstance() {
        return ScreenControls.ScreenControlsHolder.instance;  // 通过静态内部类持有者返回单例
    }
    //endregion

    public Handler handler;  // Handler对象，用于向主线程发送和处理消息
    public final int MSG_LOCKSCREEN = 0xAF001;  // 消息标识：处理屏幕锁定/解锁的UI更新
    public final int MSG_LOCKSTATEHIDE = 0xAF002;  // 消息标识：隐藏锁定状态提示（延迟1秒后隐藏）

    private FrameLayout MaskLayerLayout;  // 遮罩层布局，覆盖在屏幕上方阻止触摸操作
    private MainViewGroup mainViewGroup;  // 主视图容器，持有所有UI控件的根容器

    private boolean isExternalKey = false;  // 外部按键标志，true表示当前为外部按键触发
    private Context context;  // 应用上下文，用于资源访问和UI操作

    private MProgress progress;  // 自定义进度条控件，显示加载/校准进度
    private View progressView;  // 进度条所在的父视图容器
    private View lockState;  // 锁定状态提示视图，触摸锁屏时短暂显示

    private TextView tipsSelfAdjust;  // 自校准提示文本视图
    private TextView tipsFactoryAdjust;  // 出厂校准提示文本视图

//    private TextView tipsAutoZeroing;  // 自动调零提示文本视图（已注释，暂未使用）

    /**
     * 屏幕锁定状态监听器接口。
     * 当屏幕锁定或解锁时，通过此接口通知注册的监听器。
     */
    public interface IScreenLockListener{
        /**
         * 屏幕锁定状态变化回调。
         *
         * @param bLockScreen true表示屏幕已锁定，false表示屏幕已解锁
         */
        void onLockScreen(boolean bLockScreen);  // 锁定状态变化回调方法
    }
    private IScreenLockListener screenLockListener;  // 屏幕锁定状态监听器实例

    /**
     * 初始化屏幕控制器，绑定视图组件和监听器。
     * 在MainViewGroup初始化时调用，设置遮罩层、进度条、提示文本等UI组件，
     * 并创建Handler用于主线程消息处理。
     *
     * @param mainViewGroup     主视图容器，提供遮罩层布局
     * @param context           应用上下文，用于资源访问
     * @param screenLockListener 屏幕锁定状态监听器
     */
    @SuppressLint("ClickableViewAccessibility")  // 抑制触摸事件相关的lint警告
    public void init(MainViewGroup mainViewGroup, Context context,IScreenLockListener screenLockListener) {
        this.context = context;  // 保存应用上下文引用
        this.mainViewGroup = mainViewGroup;  // 保存主视图容器引用
        this.screenLockListener = screenLockListener;  // 保存屏幕锁定监听器引用
        MaskLayerLayout = (FrameLayout) mainViewGroup.findViewById(R.id.MaskLayerLayout);  // 获取遮罩层帧布局
        View view = View.inflate(this.context, R.layout.layout_main_lock_progress, this.MaskLayerLayout);  // 将锁定进度布局填充到遮罩层中
        lockState=view.findViewById(R.id.lock_screentClickState);  // 获取锁定状态提示视图
        progressView = view.findViewById(R.id.progressView);  // 获取进度条父视图
        progress = (MProgress) view.findViewById(R.id.progress);  // 获取自定义进度条控件
        view.setVisibility(View.GONE);  // 初始时隐藏锁定进度布局
        view.requestLayout();  // 请求重新布局，确保视图参数生效


        tipsSelfAdjust = (TextView) ((MainActivity) context).findViewById(R.id.tipsSelfAdjust);  // 从MainActivity获取自校准提示文本

        tipsFactoryAdjust = (TextView) ((MainActivity) context).findViewById(R.id.tipsFactoryAdjust);  // 从MainActivity获取出厂校准提示文本

//        tipsAutoZeroing = (TextView) ((MainActivity) context).findViewById(R.id.briefChannelAutoZeroing);  // 获取自动调零提示文本（已注释）

        handler = new Handler() {  // 创建主线程Handler，处理屏幕锁定相关消息
            @Override
            public void handleMessage(Message msg) {  // 处理接收到的消息
                if (msg.what == MSG_LOCKSCREEN) {  // 如果是屏幕锁定/解锁消息
                    processLockScreen();  // 处理屏幕锁定状态更新
                }else if(msg.what == MSG_LOCKSTATEHIDE){  // 如果是隐藏锁定状态提示消息
                    lockState.setVisibility(View.GONE);  // 隐藏锁定状态提示
                }
            }
        };
    }

    /**
     * 设置遮罩层的触摸事件监听。
     * 当屏幕处于锁定状态且非外部按键触发时，在触摸按下时显示锁定提示，
     * 松开或取消时隐藏提示，给用户视觉反馈表明屏幕已锁定。
     *
     * @param ev 触摸事件对象
     */
    public void setMaskLayerLayoutOnTouchListener(MotionEvent ev){
        if (isLockScreen() && isExternalKey==false && ev.getMetaState()!=1){  // 屏幕已锁定、非外部按键、非特殊按键状态时处理
            switch (ev.getAction() & MotionEvent.ACTION_MASK){  // 获取触摸动作类型（含多点触控）
                case MotionEvent.ACTION_DOWN:  // 手指按下事件
                    lockState.setVisibility(View.VISIBLE);  // 显示锁定状态提示
                    Message msg=new Message();  // 创建延迟隐藏消息
                    msg.what=MSG_LOCKSTATEHIDE;  // 设置消息类型为隐藏锁定状态
                    handler.sendMessageDelayed(msg,1000);  // 延迟1秒后发送隐藏消息
                    break;
                case MotionEvent.ACTION_CANCEL:  // 触摸事件被取消
                case MotionEvent.ACTION_UP:  // 手指抬起事件
                    lockState.setVisibility(View.GONE);  // 立即隐藏锁定状态提示
                    break;
            }
        }
    }

    /**
     * 在UI线程上执行Runnable任务。
     * 提供便捷方法，使外部模块可以通过ScreenControls在主线程上执行操作。
     *
     * @param runnable 需要在UI线程上执行的任务
     */
    public void onUI(Runnable runnable){
        handler.post(runnable);  // 通过Handler将任务投递到主线程消息队列执行
    }


    private volatile int lockflag = 0;  // 锁定标志位，使用volatile保证多线程可见性，位标志叠加多种锁定原因

    public static final int LOCK_SCREEN = (1<<0);  // 位标志：屏幕锁定（第0位，值0x01）

    public static final int LOCK_PROGRESS = (1<<1);  // 位标志：进度条锁定（第1位，值0x02）

    public static final int LOCK_SELF_ADJUST = (1<<2);  // 位标志：自校准锁定（第2位，值0x04）
    public static final int LOCK_FACTORY_ADJUST = (1<<3);  // 位标志：出厂校准锁定（第3位，值0x08）
    public static final int LOCK_KEY = (1<<4);  // 位标志：按键锁定（第4位，值0x10）

    public static final int LOCK_PROBE = (1<<5);  // 位标志：探头锁定（第5位，值0x20）

    public static final int LOCK_LOADCSV = (1<<16);  // 位标志：CSV加载锁定（第16位，值0x10000）

    public static final int LOCK_LOADCSV_MASK = 0xFF << 16;  // CSV加载锁定位掩码（第16~23位），用于批量检测CSV相关锁定


    /**
     * 设置进度条的当前进度值。
     *
     * @param value 进度值（0~100）
     * @return 当前ScreenControls实例，支持链式调用
     */
    public ScreenControls setProgressValue(int value) {
        if (progress != null) progress.setProgress(value);  // 进度条非空时设置进度值
        return this;  // 返回自身，支持链式调用
    }

    /**
     * 处理屏幕锁定/解锁的UI更新。
     * 根据当前lockflag位标志的状态，控制遮罩层、进度条、校准提示等视图的显示与隐藏。
     * 此方法在主线程Handler中调用，确保UI操作线程安全。
     */
    private void processLockScreen(){
        Log.d(TAG,"lockflag:" + Integer.toHexString(lockflag));  // 输出当前锁定标志的十六进制值到日志
        int v = (lockflag & LOCK_PROGRESS) != 0?View.VISIBLE:View.GONE;  // 判断是否有进度锁定，决定进度视图可见性
        if((lockflag & LOCK_LOADCSV_MASK) != 0){  // 如果存在任何CSV加载锁定
            v = View.VISIBLE;  // 强制显示进度视图
        }

        progressView.setVisibility(v);  // 设置进度视图的可见性
        progressView.requestLayout();  // 请求重新布局
        if((lockflag & LOCK_PROGRESS) == 0){  // 如果没有进度锁定
//            setProgressValue(0);  // 重置进度值为0（已注释）
        }
//        tipsSelfAdjust.setVisibility((lockflag & LOCK_SELF_ADJUST) != 0 ? View.VISIBLE : View.GONE);  // 根据自校准锁定标志设置提示可见性（已注释）
        tipsFactoryAdjust.setVisibility((lockflag & LOCK_FACTORY_ADJUST) != 0 ? View.VISIBLE : View.GONE);  // 根据出厂校准锁定标志设置提示可见性
//        tipsAutoZeroing.setVisibility((lockflag & LOCK_PROBE) != 0 ? View.VISIBLE : View.GONE);  // 根据探头锁定标志设置提示可见性（已注释）
        this.MaskLayerLayout.setVisibility(lockflag != 0 ? View.VISIBLE:View.GONE);  // 只要有任何锁定标志，就显示遮罩层
    }

    Map<Integer,Integer> refcsvmap = new HashMap<>();  // CSV加载引用映射，key为CSV锁定标志位，value为对应进度值

    /**
     * 更新CSV加载进度。
     * 当屏幕处于CSV加载锁定状态时，更新各CSV文件的加载进度，
     * 并计算综合进度显示在进度条上。
     *
     * @param flag CSV加载的锁定标志位
     * @param val  当前进度值（0~100）
     */
    public synchronized void csvupdate(int flag,int val){
        if(lockflag != 0  // 当前存在锁定状态
                && ((flag & LOCK_LOADCSV_MASK) != 0) ){  // 且该标志属于CSV加载锁定范围
            refcsvmap.put(flag,val);  // 更新对应CSV标志的进度值
            AtomicInteger s = new AtomicInteger();  // 原子整数，用于线程安全地累加进度值
            refcsvmap.forEach((key,value)->{  // 遍历所有CSV进度条目
                s.addAndGet(value);  // 累加各CSV的进度值
            });
            progress.post(()->{  // 在进度条关联的UI线程上执行
                setProgressValue(20 + s.get()/refcsvmap.size() * 80 / 100);  // 计算综合进度：基础20% + 各CSV平均进度*80%
            });
        }
    }

    /***
     * 锁屏幕
     * 通过位或操作将指定标志位加入lockflag，实现多种锁定原因的叠加。
     * 锁定后发送消息更新UI，并通知屏幕锁定监听器。
     *
     * @param flag 锁定标志位（如LOCK_SCREEN、LOCK_PROGRESS、LOCK_LOADCSV等）
     * @return 当前ScreenControls实例，支持链式调用
     */
    public ScreenControls lockScreen(int flag) {
//        Log.d(TAG, "lockScreen() called with: flag = [" + Integer.toHexString(flag) + "]" + "," + Integer.toHexString(lockflag));  // 调试日志（已注释）
        synchronized (this) {  // 同步块，保证lockflag位操作的线程安全
            lockflag |= flag;  // 位或操作，将指定标志位置1（叠加锁定原因）
        }

        refcsvmap.clear();  // 清空CSV进度映射，准备重新记录
        for(int i=16;i<24;i++){  // 遍历第16~23位（CSV加载锁定位范围）
            if((lockflag & (1<<16)) != 0){  // 如果第16位（LOCK_LOADCSV）被置位
                refcsvmap.put(flag,0);  // 将当前锁定标志加入CSV映射，初始进度为0
            }
        }


        handler.sendEmptyMessage(MSG_LOCKSCREEN);  // 发送消息，触发主线程UI更新
        if(screenLockListener != null){  // 如果注册了屏幕锁定监听器
            screenLockListener.onLockScreen(lockflag != 0);  // 通知监听器当前锁定状态（lockflag非0即为锁定）
        }
        return this;  // 返回自身，支持链式调用
    }

    /**
     * 解锁屏幕。
     * 通过位与取反操作将指定标志位从lockflag中移除，实现精确解锁。
     * 解锁后发送消息更新UI，并通知屏幕锁定监听器。
     *
     * @param flag 需要解除的锁定标志位
     * @return 当前ScreenControls实例，支持链式调用
     */
    public ScreenControls unLockScreen(int flag) {
//        Log.d(TAG, "unLockScreen() called with: flag = [" + Integer.toHexString(flag) + "]");  // 调试日志（已注释）
        synchronized (this) {  // 同步块，保证lockflag位操作的线程安全
            lockflag &= ~flag;  // 位与取反操作，将指定标志位清0（移除锁定原因）
        }

        handler.sendEmptyMessage(MSG_LOCKSCREEN);  // 发送消息，触发主线程UI更新
        if(screenLockListener != null){  // 如果注册了屏幕锁定监听器
            screenLockListener.onLockScreen(lockflag != 0);  // 通知监听器当前锁定状态（可能仍有其他锁定原因）
        }
        return this;  // 返回自身，支持链式调用
    }



    /**
     * 判断指定标志位是否处于锁定状态。
     * 检查lockflag中是否包含所有指定的标志位。
     *
     * @param flag 需要检查的锁定标志位
     * @return true表示指定标志位全部处于锁定状态，false表示未锁定
     */
    public boolean isLockScreen(int flag){
        return (lockflag & flag) == flag;  // 位与操作，判断指定标志位是否全部置位
    }

    /**
     * 判断屏幕是否处于任何锁定状态。
     *
     * @return true表示屏幕已被任何原因锁定，false表示完全解锁
     */
    public boolean isLockScreen() {
        return lockflag != 0;  // lockflag非0即表示存在至少一个锁定原因
    }

    /**
     * 判断是否应锁定外部按键。
     * 当存在除LOCK_KEY以外的任何锁定标志时，外部按键应被锁定。
     *
     * @return true表示应锁定外部按键，false表示不锁定
     */
    public boolean isExternalKey() {
        return (lockflag & (~LOCK_KEY)) != 0 ;  // 排除LOCK_KEY位后，检查是否还有其他锁定标志
    }
}
