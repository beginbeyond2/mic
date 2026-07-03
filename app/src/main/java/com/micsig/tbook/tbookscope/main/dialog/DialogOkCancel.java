package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;

/**
 * Created by liwb on 2018/8/7.
 */

/*
 * +===========================================================================+
 * |                          DialogOkCancel                                   |
 * |                       确认取消对话框组件                                    |
 * +===========================================================================+
 * | 模块定位: 示波器应用程序确认取消对话框                                      |
 * | 核心职责: 显示提示消息并提供确认和取消两个按钮，用于需要用户二次确认的操作    |
 * | 架构设计: 继承RelativeLayout，通过回调机制处理确认和取消事件                 |
 * | 数据流向: 外部调用setData() -> DialogOkCancel显示 -> 点击按钮 -> 回调通知   |
 * | 依赖关系: Context, RxBus, PlaySound, MainViewGroup                         |
 * | 使用场景: 重要操作确认、删除确认、退出确认等需要用户明确选择的场景            |
 * +===========================================================================+
 */
public class DialogOkCancel extends RelativeLayout {
    private static final String TAG = "DialogOkCancel";  // 日志标签

    private Context context;  // 应用上下文
    private TextView tvPrompt;  // 提示文本视图
    private Object okData, cancelData;  // 确认和取消的回调数据
    private OnOkCancelClickListener onOkCancelClickListener;  // 点击回调监听
    private View clickView;  // 触发对话框的源视图

    private boolean exitToHome;  // 是否退出到主页标识
    private ViewGroup rootViewGroup;  // 根视图容器

    /**
     * 确认取消按钮点击监听接口
     */
    public interface OnOkCancelClickListener {
        /**
         * 点击确认按钮回调
         * @param data dialog 点击确定后，需要传递的消息
         */
        void onOkClick(View v, Object data);

        /**
         * 点击取消按钮回调
         * @param data dialog 点击取消后，需要传递的消息
         */
        void onCancelClick(View v, Object data);

        /**
         * 对话框关闭回调
         */
        void onDialogClose(View v);
    }

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public DialogOkCancel(Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public DialogOkCancel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图组件
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogOkCancel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        initView();  // 初始化视图
    }

    /**
     * 初始化视图组件
     * 加载布局并设置按钮点击事件
     */
    private void initView() {
        rootViewGroup= (ViewGroup) inflate(context, R.layout.dialog_okcancel, this);  // 加载布局文件

        tvPrompt = (TextView) findViewById(R.id.txtPrompt);  // 获取提示文本
        findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {  // 设置确认按钮点击监听
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();  // 播放按钮音效
                Logger.i(TAG, "BtnOK");//[210, 302]	69	38  // 记录日志
                hide();  // 隐藏对话框
                onOkCancelClickListener.onOkClick(clickView, okData);  // 执行确认回调
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {  // 设置取消按钮点击监听
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();  // 播放按钮音效
                Logger.i(TAG, "BtnCancel");//[521, 302]	69	38  // 记录日志
                hide();  // 隐藏对话框
                onOkCancelClickListener.onCancelClick(clickView, cancelData);  // 执行取消回调
            }
        });

    }

    //region 公共

    /**
     * 检查对话框是否用于退出到主页
     * @return true表示退出到主页，false表示普通对话框
     */
    public boolean isDialogExitToHome() {
        return exitToHome;  // 返回退出标识
    }

    /**
     * 设置对话框数据和回调
     * @param clickView 触发对话框的源视图
     * @param msgResId dialog 显示的消息的资源id
     * @param okData dialog 点击确定后，需要传递的消息
     * @param cancelData dialog 点击取消后，需要传递的消息
     * @param onOkCancelClickListener dialog 点击确定后，需要执行的动作
     */
    public void setData(View clickView, int msgResId, Object okData, Object cancelData, OnOkCancelClickListener onOkCancelClickListener) {
        exitToHome = okData != null && okData instanceof MainMsgExitToHome;  // 检查是否为退出到主页消息
        tvPrompt.setText(msgResId);  // 设置提示文本
        this.clickView = clickView;  // 保存源视图
        this.okData = okData;  // 保存确认数据
        this.cancelData = cancelData;  // 保存取消数据
        this.onOkCancelClickListener = onOkCancelClickListener;  // 保存回调监听
        show();  // 显示对话框
    }
    //endregion

    /**
     * 显示对话框
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_OKCANCEL);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogOkCancel",rootViewGroup);  // 打印控件位置信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() {
        onOkCancelClickListener.onDialogClose(clickView);  // 执行对话框关闭回调
        setVisibility(GONE);  // 设置隐藏
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_OKCANCEL);  // 发送对话框关闭事件
    }

    /**
     * 检查对话框是否正在显示
     * @return true表示正在显示，false表示已隐藏
     */
    public boolean isShow(){
        return getVisibility()==VISIBLE;  // 检查可见性
    }

    /**
     * 模拟点击确认按钮
     */
    public void pressOK(){
        findViewById(R.id.btnOK).performClick();  // 触发确认按钮点击
    }

    /**
     * 模拟点击取消按钮
     */
    public void PressCancel(){
        findViewById(R.id.btnCancel).performClick();  // 触发取消按钮点击
    }
}