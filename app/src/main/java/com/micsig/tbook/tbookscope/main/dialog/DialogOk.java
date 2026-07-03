package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * |                            DialogOk                                       |
 * |                          确认对话框组件                                    |
 * +===========================================================================+
 * | 模块定位: 示波器应用程序确认对话框                                          |
 * | 核心职责: 显示提示消息并提供确认按钮，用于用户确认操作                        |
 * | 架构设计: 继承RelativeLayout，通过回调机制处理确认事件                       |
 * | 数据流向: 外部调用setData() -> DialogOk显示 -> 点击确认 -> 回调通知          |
 * | 依赖关系: Context, RxBus, PlaySound, MainViewGroup                         |
 * | 使用场景: 系统消息提示、操作确认、错误警告等需要用户确认的场景                |
 * +===========================================================================+
 */
public class DialogOk extends RelativeLayout {
    private static final String TAG = "DialogOk";  // 日志标签

    private Context context;  // 应用上下文
    private TextView tvPrompt;  // 提示文本视图
    private Button btnOk;  // 确认按钮
    private Object data;  // 回调数据
    private OnOkClickListener onOkClickListener;  // 确认点击回调

    private boolean exitToHome;  // 是否退出到主页标识
    private ViewGroup rootViewGroup;  // 根视图容器

    /**
     * 确认按钮点击监听接口
     */
    public interface OnOkClickListener {
        /**
         * 点击确认按钮回调
         * @param data dialog 点击确定后，需要传递的消息
         */
        void onClick(View v, Object data);
    }

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public DialogOk(Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public DialogOk(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图组件
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogOk(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        initView();  // 初始化视图
    }

    /**
     * 初始化视图组件
     * 加载布局并设置按钮点击事件
     */
    private void initView() {
        rootViewGroup= (ViewGroup) inflate(context, R.layout.dialog_ok, this);  // 加载布局文件

        tvPrompt = (TextView) findViewById(R.id.txtPrompt);  // 获取提示文本
        btnOk = findViewById(R.id.btnOK);  // 获取确认按钮
        btnOk.setOnClickListener(new OnClickListener() {  // 设置按钮点击监听
            @Override
            public void onClick(View v) {
                PlaySound.getInstance().playButton();  // 播放按钮音效
                Logger.i(TAG, "BtnOK");//[360, 300]	80, 40  // 记录日志
                hide();  // 隐藏对话框
                if (onOkClickListener != null) {  // 检查回调是否存在
                    onOkClickListener.onClick(DialogOk.this, data);  // 执行确认回调
                }
            }
        });

    }

    //region 公共

    /**
     * 获取确认按钮实例
     * @return 确认按钮
     */
    public Button getBtnOk() {
        return btnOk;  // 返回按钮引用
    }

    /**
     * 检查对话框是否用于退出到主页
     * @return true表示退出到主页，false表示普通对话框
     */
    public boolean isDialogExitToHome() {
        return exitToHome;  // 返回退出标识
    }

    /**
     * 设置对话框数据和回调（使用资源ID）
     * @param msgResId dialog 显示的消息的资源id
     * @param data dialog 点击确定后，需要传递的消息
     * @param onOkClickListener dialog 点击确定后，需要执行的动作
     */
    public void setData(int msgResId, Object data, OnOkClickListener onOkClickListener) {
        exitToHome = data != null && data instanceof MainMsgExitToHome;  // 检查是否为退出到主页消息
        tvPrompt.setText(msgResId);  // 设置提示文本
        this.data = data;  // 保存回调数据
        this.onOkClickListener = onOkClickListener;  // 保存回调监听
        show();  // 显示对话框
    }

    /**
     * 设置对话框数据和回调（使用字符串）
     * @param msg 提示消息文本
     * @param data dialog 点击确定后，需要传递的消息
     * @param onOkClickListener dialog 点击确定后，需要执行的动作
     */
    public void setData(String msg, Object data, OnOkClickListener onOkClickListener) {
        exitToHome = data != null && data instanceof MainMsgExitToHome;  // 检查是否为退出到主页消息
        tvPrompt.setText(msg);  // 设置提示文本
        this.data = data;  // 保存回调数据
        this.onOkClickListener = onOkClickListener;  // 保存回调监听
        show();  // 显示对话框
    }
    //endregion

    /**
     * 显示对话框
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_OK);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogOk",rootViewGroup);  // 打印控件位置信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() {
        setVisibility(GONE);  // 设置隐藏
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_OK);  // 发送对话框关闭事件
    }

    /**
     * 检查对话框是否正在显示
     * @return true表示正在显示，false表示已隐藏
     */
    public boolean isShow() {
        return getVisibility() == VISIBLE;  // 检查可见性
    }

    /**
     * 获取提示文本内容
     * @return 提示文本字符串
     */
    public String getText() {
        return tvPrompt.getText().toString();  // 返回文本内容
    }
}