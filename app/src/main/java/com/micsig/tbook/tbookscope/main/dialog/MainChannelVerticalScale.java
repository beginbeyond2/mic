package com.micsig.tbook.tbookscope.main.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelMaster;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.ui.wavezone.TChan;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * @Description: 各个通道垂直挡位调节通用类
 * @Author: limh
 * @CreateDate: 2024/5/14 11:04
 */

/*
 * +===========================================================================+
 * |                       MainChannelVerticalScale                            |
 * |                        通道垂直挡位调节组件                                 |
 * +===========================================================================+
 * | 模块定位: 示波器各通道垂直挡位快捷调节UI组件                                 |
 * | 核心职责: 提供通道垂直挡位的上下调节按钮，响应用户操作发送调节指令             |
 * | 架构设计: 继承ConstraintLayout，采用RxJava事件驱动，Handler延时隐藏           |
 * | 数据流向: RxBus接收显示请求 -> 调整位置显示 -> 点击按钮 -> 发送调节指令       |
 * | 依赖关系: Context, RxBus, Handler, MainRightLayoutItemChannelMaster, TChan  |
 * | 使用场景: 用户需要快速调节某通道的垂直挡位时触发显示                          |
 * +===========================================================================+
 */
public class MainChannelVerticalScale extends ConstraintLayout {

    private static final String TAG = "MainChannelVerticalScale";  // 日志标签
    private Context context;  // 应用上下文
    private Button btnTop, btnBottom;  // 上下调节按钮
    private ImageView imgBackSrc;  // 背景图片视图
    private int channelNumber;//当前操作的通道号  // 当前通道编号
    private MainRightLayoutItemChannelMaster masterView;//当前对应的masterView  // 对应的主控视图
    private int leftLimit = 1098;//屏幕上左侧限制显示位置  // 左边界限制
    private int rightLimit = 1704;//屏幕上右侧限制显示位置  // 右边界限制

    private boolean isShow = false;  // 显示状态标识

    private View rootView;  // 根视图引用

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public MainChannelVerticalScale(@NonNull Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public MainChannelVerticalScale(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图和控制逻辑
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainChannelVerticalScale(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        initView();  // 初始化视图
        initControl();  // 初始化控制
    }



    /**
     * 初始化事件控制
     * 订阅RxJava事件消息
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_VERTICAL_SCALE).subscribe(consumerShowVscale);//显示垂直挡位View  // 订阅显示事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE).subscribe(consumerHideVscale);//关闭垂直挡位调节View  // 订阅强制隐藏事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlipToOther);//接收slip滑出消息  // 订阅滑出事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_VERTICAL_SCALE_MOVE).subscribe(consumerMovePosition);//移动位置  // 订阅位置移动事件
    }

    /**
     * 初始化视图组件
     * 加载布局并绑定按钮事件
     */
    private void initView() {
        rootView = (ViewGroup) View.inflate(context, R.layout.view_channel_vertical_adjust, this);  // 加载布局文件
        btnTop = rootView.findViewById(R.id.btnTop);  // 获取向上调节按钮
        btnBottom = rootView.findViewById(R.id.btnBottom);  // 获取向下调节按钮
        imgBackSrc = rootView.findViewById(R.id.img_back_src);  // 获取背景图片
        btnTop.setOnClickListener(onClicklistener);  // 设置向上按钮点击监听
        btnBottom.setOnClickListener(onClicklistener);  // 设置向下按钮点击监听
    }

    /**
     * 按钮点击事件监听器
     * 处理上下调节按钮点击
     */
    private View.OnClickListener onClicklistener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            isShow = true;  // 标记为显示状态
            switch (v.getId()) {  // 根据按钮ID分发处理
                case R.id.btnTop:  // 向上调节按钮
                    Logger.d(TAG, "调整档位-----btnTop------postChange" );  // 记录日志
                    postChange(true);  // 执行向上调节
                    break;
                case R.id.btnBottom:  // 向下调节按钮
                    Logger.d(TAG, "调整档位-----btnBottom------postChange" );  // 记录日志
                    postChange(false);  // 执行向下调节
                    break;
                case R.id.outView:  // 外部区域点击
                    isShow = false;  // 标记为隐藏状态
                    hideVerticalScale();  // 隐藏调节视图
                    break;
            }
        }
    };

    /**
     * 执行挡位调节
     * 更新背景图并发送调节指令
     * @param isClickTop true表示向上调节，false表示向下调节
     */
    private void postChange(boolean isClickTop) {
        if (isClickTop) {  // 点击向上按钮
            imgBackSrc.setImageResource(R.drawable.svg_right_chx_button_u_88x174);  // 设置向上背景图
        } else {  // 点击向下按钮
            imgBackSrc.setImageResource(R.drawable.svg_right_chx_button_d_88x174);  // 设置向下背景图
        }
        if (myHandler.hasMessages(MSG_HIDE)) {  // 检查是否有隐藏消息
            myHandler.removeMessages(MSG_HIDE);  // 移除隐藏消息
        }
        myHandler.sendEmptyMessage(MSG_HIDE);  // 发送隐藏消息

        if (myHandler.hasMessages(HANDLE_MSG)) {  // 检查是否有还原背景消息
            myHandler.removeMessages(HANDLE_MSG);  // 移除还原消息
        }
        Logger.d(TAG, "调整档位-----1------postChange isClickTop= " + isClickTop);  // 记录日志
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);  // 延时200ms发送还原背景消息
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE, isClickTop + CommandMsgToUI.PARAM_SPLIT + channelNumber);  // 发送调节指令
    }

    /**
     * 强制隐藏消费者
     * 接收强制隐藏事件并隐藏视图
     */
    private Consumer<Boolean> consumerHideVscale = new Consumer<Boolean>() {//由于正常只会显示一个垂直挡位调节按钮，多以不用管时按个通道的显示，直接关闭即可。
        @Override
        public void accept(Boolean forceHide) throws Throwable {
            if (forceHide) {  // 强制隐藏标志为true
                hideVerticalScale();  // 隐藏调节视图
            }
        }
    };

    /**
     * 显示消费者
     * 接收显示请求并更新视图位置
     */
    private Consumer<MainRightLayoutItemChannelMaster> consumerShowVscale = new Consumer<MainRightLayoutItemChannelMaster>() {
        @Override
        public void accept(MainRightLayoutItemChannelMaster masterView) throws Throwable {
            if (masterView.getVisibility() != View.VISIBLE) return;  // 主控视图不可见则返回
            channelNumber = TChan.toUiChNo(masterView.getChIndex());  // 获取UI通道号
            MainChannelVerticalScale.this.masterView = masterView;  // 保存主控视图引用
            updateViewLayout(masterView);//显示并调整位置  // 更新视图位置布局
            if (myHandler.hasMessages(MSG_HIDE)) {  // 检查是否有隐藏消息
                myHandler.removeMessages(MSG_HIDE);  // 移除隐藏消息
            }
            myHandler.sendEmptyMessageDelayed(MSG_HIDE, 3000);  // 延时3秒发送隐藏消息
        }
    };

    /**
     * 更新视图布局位置
     * 根据通道类型调整调节按钮的显示位置
     * @param masterView 主控视图
     */
    private void updateViewLayout(MainRightLayoutItemChannelMaster masterView) {
        Rect rect = Screen.getViewLocation(masterView);  // 获取主控视图屏幕位置
        Logger.d(TAG, "channelNumber= " + channelNumber + " rect= " + rect);//获取masterView相对屏幕位置  // 记录日志
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) rootView.getLayoutParams();  // 获取布局参数
        // 88 174为垂直按钮的宽和高 6 9为偏移量
        if (TChan.isChan(channelNumber)) {  // 判断是否为物理通道
            layoutParams.x = rect.left - 88 - 6;  // 设置X坐标（左侧偏移）
            if (channelNumber == TChan.Ch1) { //Ch1 顶部对齐  // CH1通道顶部对齐
                layoutParams.y = rect.top;  // Y坐标等于顶部
            } else if (channelNumber == TChan.Ch8) { //Ch8 底部对齐  // CH8通道底部对齐
                layoutParams.y = rect.bottom - 174;  // Y坐标等于底部减高度
            } else { //Ch2-Ch7 水平中线对齐  // CH2-CH7通道中线对齐
                layoutParams.y = (rect.top + rect.bottom) / 2 - 174 / 2;  // Y坐标等于中线减半高
            }
        } else if (TChan.isMath(channelNumber) || TChan.isRef(channelNumber)) {//垂直中线对齐  // Math或Ref通道
            layoutParams.x = (rect.left + rect.right) / 2 - 88 / 2;  // X坐标水平居中
            layoutParams.y = rect.top - 174 - 9;  // Y坐标位于顶部上方
        }
        Logger.d(TAG, "layoutParams= " + layoutParams.x + "  " + layoutParams.y);  // 记录日志
        rootView.setLayoutParams(layoutParams);  // 应用布局参数
        //Math/Ref对应的垂直挡位与显示区域限制
        if (!TChan.isChan(channelNumber) && (layoutParams.x < leftLimit || layoutParams.x > rightLimit)) {  // Math/Ref通道超出显示区域
            hideVerticalScale();  // 隐藏调节视图
        } else {  // 在显示区域内
            setVisibility(VISIBLE);  // 显示视图
        }
    }

    /**
     * 位置移动消费者
     * 接收位置移动事件并更新布局
     */
    private Consumer<Object> consumerMovePosition = new Consumer<Object>() {
        @Override
        public void accept(Object o) throws Throwable {
            if (getVisibility() == View.VISIBLE && masterView != null) {  // 视图可见且主控视图有效
                updateViewLayout(masterView);  // 更新视图布局
            }
        }
    };


    /**
     * 滑出事件消费者
     * 当通道滑出面板打开时隐藏调节视图
     */
    private Consumer<MainMsgSlip> consumerMainSlipToOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            if (mainMsgSlip.isOpen()) {  // 滑出面板打开
                switch (mainMsgSlip.getSlip()) {  // 根据滑出类型分发处理
                    case MainViewGroup.RIGHTSLIP_CH1:  // CH1滑出
                    case MainViewGroup.RIGHTSLIP_CH2:  // CH2滑出
                    case MainViewGroup.RIGHTSLIP_CH3:  // CH3滑出
                    case MainViewGroup.RIGHTSLIP_CH4:  // CH4滑出
                    case MainViewGroup.RIGHTSLIP_CH5:  // CH5滑出
                    case MainViewGroup.RIGHTSLIP_CH6:  // CH6滑出
                    case MainViewGroup.RIGHTSLIP_CH7:  // CH7滑出
                    case MainViewGroup.RIGHTSLIP_CH8:  // CH8滑出
//                        if (TChan.isChan(channelNumber)) {
                            hideVerticalScale();  // 隐藏调节视图
//                        }
                        break;
                }
            }
        }
    };


    private static final int HANDLE_MSG = 1;//点击后还原背景  // 还原背景消息标识
    private static final int MSG_HIDE = 2; //隐藏垂直挡位调节按钮  // 隐藏视图消息标识

    /**
     * Handler消息处理器
     * 处理背景还原和视图隐藏
     */
    private final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);  // 调用父类处理
            switch (msg.what) {  // 根据消息类型分发处理
                case HANDLE_MSG:  // 还原背景消息
                    imgBackSrc.setImageResource(R.drawable.svg_right_chx_button_88x174);  // 还原默认背景图
                    break;
                case MSG_HIDE:  // 隐藏视图消息
                    if (isShow) {  // 当前处于显示状态
                        if (myHandler.hasMessages(MSG_HIDE)) {  // 检查是否有待处理消息
                            myHandler.removeMessages(MSG_HIDE);  // 移除消息
                        }
                        isShow = false;  // 标记为隐藏状态
                        myHandler.sendEmptyMessageDelayed(MSG_HIDE, 3000);//3秒后隐藏  // 延时3秒再次发送隐藏消息
                    } else {  // 当前已不在显示状态
                        setVisibility(GONE);  // 隐藏视图
                    }
                    break;
            }
        }
    };

    /**
     * 隐藏垂直挡位调节视图
     * 清除所有Handler消息
     */
    public void hideVerticalScale() {
        setVisibility(GONE);  // 隐藏视图
        myHandler.removeCallbacksAndMessages(null);  // 清除所有Handler消息
    }

    private final Rect r = new Rect();  // 位置矩形缓存

    /**
     * 检查指定点是否在视图范围内
     * @param x X坐标
     * @param y Y坐标
     * @return true表示点在范围内，false表示不在范围内
     */
    public boolean containsPoint(int x, int y) {
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());  // 设置矩形范围
        return x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE;  // 检查点是否在范围内且视图可见
    }

    /**
     * 检查触摸事件是否为按下动作且在视图范围内
     * @param event 触摸事件
     * @return true表示按下动作在范围内，false表示不满足条件
     */
    public boolean containsDownPoint(MotionEvent event) {
        int x = (int) event.getX();  // 获取触摸X坐标
        int y = (int) event.getY();  // 获取触摸Y坐标
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());  // 设置矩形范围
        boolean b = x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE && event.getAction() == MotionEvent.ACTION_DOWN;  // 检查条件
        return b;  // 返回检查结果
    }
}