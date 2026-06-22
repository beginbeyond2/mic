package com.micsig.tbook.tbookscope.util; // // 工具类包，存放应用级工具组件

/*
 * =====================================================================
 * |  _____  ____  _   _  _____   _____  _    _  _____  _____           |
 * | |  __ \|  _ \| \ | ||  __ \ |_   _|| |  | ||_   _||  __ \          |
 * | | |  | || | | |  \| || |  | |  | |  | |__| |  | |  | |  | |         |
 * | | |  | || | | | . ` || |  | |  | |  |  __  |  | |  | |  | |         |
 * | | |__| || |_| | |\  || |__| | _| |_ | |  | | _| |_ | |__| |         |
 * | |_____/ |____/|_| \_||_____/ |_____||_|  |_||_____||_____/          |
 * |                                                                     |
 * |  模块名称: DToast (自定义Toast管理类)                                  |
 * |  所属层级: com.micsig.tbook.tbookscope.util                         |
 * |  核心职责: 统一管理应用内Toast提示的显示与隐藏                          |
 * |                                                                     |
 * |  架构设计:                                                           |
 * |    DToast采用懒汉式单例模式，使用自定义TextView替代系统Toast。          |
 * |    通过Handler消息机制精确控制显示/隐藏时间，避免系统Toast队列堆积。     |
 * |    支持居中显示(show)和底部显示(showBottom)两种位置模式。              |
 * |                                                                     |
 * |  数据流向:                                                           |
 * |    show(msg) / showBottom(msg) / showDelay(msg)                     |
 * |      └─> 计算文本尺寸与布局位置                                       |
 * |      └─> 设置LayoutParams                                           |
 * |      └─> handler.sendEmptyMessage(MSG_TOAST_SHOW)                   |
 * |      └─> handler.handleMessage()                                    |
 * |            ├─ MSG_TOAST_SHOW: 显示 → 延迟发送MSG_TOAST_HIDE          |
 * |            ├─ MSG_TOAST_HIDE: 隐藏 → 清除延迟消息                    |
 * |            └─ MSG_TOAST_SHOW_DELAY: 延迟后调用show()                 |
 * |                                                                     |
 * |  依赖关系:                                                           |
 * |    - GlobalVar      : 获取波形区域尺寸用于居中计算                     |
 * |    - MainActivity   : 获取Toast的TextView控件                        |
 * |    - App            : 获取Resources用于字符串资源                     |
 * |                                                                     |
 * |  使用示例:                                                           |
 * |    DToast.get().init(context);        // 初始化（MainActivity中调用）  |
 * |    DToast.get().show("提示信息");      // 居中显示Toast               |
 * |    DToast.get().showBottom("底部提示"); // 底部显示Toast              |
 * |    DToast.get().showDelay("延迟提示"); // 1秒后显示Toast              |
 * |    DToast.get().hide();               // 立即隐藏Toast               |
 * |                                                                     |
 * =====================================================================
 */

import android.content.Context; // // Android上下文接口
import android.graphics.Paint; // // 画笔，用于测量文本尺寸
import android.graphics.PixelFormat; // // 像素格式常量
import android.graphics.Rect; // // 矩形，用于文本边界测量
import android.os.Handler; // // Handler消息机制，控制Toast显示/隐藏时序
import android.os.Message; // // Handler消息对象
import android.text.TextUtils; // // 文本工具类，判空
import android.view.View; // // 视图基类，控制可见性
import android.view.WindowManager; // // 窗口管理器
import android.widget.AbsoluteLayout; // // 绝对布局，用于精确控制Toast位置
import android.widget.TextView; // // 文本视图，Toast显示载体

import com.micsig.tbook.tbookscope.GlobalVar; // // 全局运行时变量，获取波形区域尺寸
import com.micsig.tbook.tbookscope.MainActivity; // // 主界面Activity，获取Toast控件
import com.micsig.tbook.tbookscope.R; // // 资源ID常量


/**
 * Toast统一管理类。
 * 懒汉式单例，使用自定义TextView替代系统Toast，
 * 通过Handler消息机制控制显示/隐藏时序，
 * 支持居中显示和底部显示两种模式。
 */
public class DToast {

    /** Handler消息：显示Toast */
    public static final int MSG_TOAST_SHOW = 0xab; // // 显示Toast消息标识

    /** Handler消息：隐藏Toast */
    public static final int MSG_TOAST_HIDE = 0xac; // // 隐藏Toast消息标识

    /** Handler消息：延迟显示Toast */
    public static final int MSG_TOAST_SHOW_DELAY = 0xad; // // 延迟显示Toast消息标识

    /** Toast默认显示时长（毫秒），500ms后自动隐藏 */
    private static final int msgShowTime = 500;//ms // // Toast显示持续时间

    /** Toast延迟显示的延迟时长（毫秒），1秒后触发显示 */
    private static final int msgShowTimeDelay = 1000;//ms // // 延迟显示等待时间

    /** DToast单例引用 */
    private static DToast toast; // // 懒汉式单例引用

    /** Toast显示用的TextView控件，位于MainActivity布局中 */
    private TextView tvToast; // // Toast显示载体TextView

    /** 左侧工具栏宽度（像素），用于计算Toast水平偏移 */
    private int startX; // // 左侧工具栏宽度偏移量

    /**
     * 私有构造函数，防止外部实例化。
     */
    private DToast() { // // 私有构造，单例模式
    }

    /**
     * 获取DToast单例。
     * 懒汉式实现，首次调用时创建实例。
     * @return DToast单例
     */
    public static DToast get() { // // 获取DToast单例
        if (toast == null) { // // 单例未创建
            toast = new DToast(); // // 创建新实例
        }
        return toast; // // 返回单例
    }


    /**
     * 初始化DToast。
     * 从MainActivity布局中获取Toast的TextView控件，
     * 并读取左侧工具栏宽度用于位置计算。
     *
     * @param context 上下文，必须是MainActivity实例
     */
    public void init(Context context) { // // 初始化DToast
        tvToast = (TextView) ((MainActivity) context).findViewById(R.id.toast); // // 获取Toast的TextView控件
        startX = (int) context.getResources().getDimension(R.dimen.leftBarWidth); // // 读取左侧工具栏宽度
    }

    /**
     * 居中显示Toast消息。
     * 自动计算文本尺寸，动态调整Toast大小和位置，
     * 使其在波形区域居中显示。支持多行文本（\n分隔）。
     *
     * @param msg 要显示的消息文本，为空则不显示
     */
    public void show(String msg) { // // 居中显示Toast
        if (TextUtils.isEmpty(msg)) return; // // 消息为空则不显示

        String[] strings; // // 按行拆分的消息数组
        if (msg.contains("\n")) { // // 消息包含换行符
            strings = msg.split("\n"); // // 按换行符拆分
        } else { // // 单行消息
            strings = new String[1]; // // 创建单元素数组
            strings[0] = msg; // // 赋值消息
        }
        int width = 0, height = 50; // // 初始化宽度和高度（高度基础值50px）
        for (String string : strings) { // // 遍历每一行
            width = Math.max(width, getTextWidth(string)); // // 取最大行宽
            height = height + (int) (getTextHeight(string) * 1.5); // // 累加行高（1.5倍行间距）
        }
        width = 200 + width; // // 宽度加上左右边距200px
        int x = GlobalVar.get().getMainWave().x / 2 + startX - width / 2; // // 计算水平居中位置（波形区域中心+左侧偏移）
        int y = GlobalVar.get().getMainWave().y / 2 + GlobalVar.get().getMainTop().y - height / 2; // // 计算垂直居中位置（波形区域中心+顶部偏移）
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) tvToast.getLayoutParams(); // // 获取当前布局参数
        layoutParams.width = width; // // 设置宽度
        layoutParams.height = height; // // 设置高度
        layoutParams.x = x; // // 设置水平位置
        layoutParams.y = y; // // 设置垂直位置

        tvToast.setLayoutParams(layoutParams); // // 应用布局参数
        tvToast.setText(msg); // // 设置显示文本
        handler.sendEmptyMessage(MSG_TOAST_SHOW); // // 发送显示消息
    }


    /**
     * 底部显示Toast消息。
     * 与show()类似，但垂直位置固定在波形区域底部（y=1000）。
     *
     * @param msg 要显示的消息文本
     */
    public void showBottom(String msg) { // // 底部显示Toast
//        if (TextUtils.isEmpty(msg)) return;\ // // 已注释：空消息也允许显示
        String[] strings; // // 按行拆分的消息数组
        if (msg.contains("\n")) { // // 消息包含换行符
            strings = msg.split("\n"); // // 按换行符拆分
        } else { // // 单行消息
            strings = new String[1]; // // 创建单元素数组
            strings[0] = msg; // // 赋值消息
        }
        int width = 0, height = 50; // // 初始化宽度和高度
        for (String string : strings) { // // 遍历每一行
            width = Math.max(width, getTextWidth(string)); // // 取最大行宽
            height = height + (int) (getTextHeight(string) * 1.5); // // 累加行高
        }
        width = 200 + width; // // 宽度加上左右边距
        int x = GlobalVar.get().getMainWave().x / 2 + startX - width / 2; // // 计算水平居中位置
        int y = GlobalVar.get().getMainWave().y / 2 + GlobalVar.get().getMainTop().y + height / 2; // // 计算底部位置（向下偏移）
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) tvToast.getLayoutParams(); // // 获取当前布局参数
        layoutParams.width = width; // // 设置宽度
        layoutParams.height = height; // // 设置高度
        layoutParams.x = x; // // 设置水平位置
        layoutParams.y = 1000; // // 固定垂直位置在底部（硬编码1000px）
//        tvToast.setLayoutParams(layoutParams); // // 已注释：布局参数应用
        tvToast.setText(msg); // // 设置显示文本
        handler.sendEmptyMessage(MSG_TOAST_SHOW); // // 发送显示消息
    }

    /**
     * 居中显示Toast消息（资源ID版本）。
     * 将字符串资源ID转换为字符串后调用show(String)。
     *
     * @param resId 字符串资源ID
     */
    public void show(int resId) { // // 居中显示Toast（资源ID版本）
        show(App.get().getResources().getString(resId)); // // 获取字符串资源并显示
    }

    /**
     * 延迟显示Toast消息。
     * 延迟1秒（msgShowTimeDelay）后在居中位置显示消息。
     * 通过Handler的sendMessageDelayed实现延迟。
     *
     * @param msg 要延迟显示的消息文本
     */
    public void showDelay(String msg) { // // 延迟显示Toast
        Message message = new Message(); // // 创建消息对象
        message.obj = msg; // // 携带消息文本
        message.what = MSG_TOAST_SHOW_DELAY; // // 设置消息类型为延迟显示
        handler.sendMessageDelayed(message, msgShowTimeDelay); // // 延迟1秒后发送消息
    }

    /**
     * 延迟显示Toast消息（资源ID版本）。
     * 将字符串资源ID转换为字符串后调用showDelay(String)。
     *
     * @param resId 字符串资源ID
     */
    public void showDelay(int resId) { // // 延迟显示Toast（资源ID版本）
        showDelay(App.get().getResources().getString(resId)); // // 获取字符串资源并延迟显示
    }

    /**
     * 立即隐藏Toast。
     * 发送MSG_TOAST_HIDE消息，Handler收到后将TextView设为GONE。
     */
    public void hide() { // // 隐藏Toast
        handler.sendEmptyMessage(MSG_TOAST_HIDE); // // 发送隐藏消息
    }

    /**
     * 测量文本绘制宽度。
     * 使用Paint.getTextBounds()获取文本的像素宽度。
     *
     * @param text 要测量的文本
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) { // // 测量文本宽度
        Paint paint = new Paint(); // // 创建画笔
        Rect rect = new Rect(); // // 创建边界矩形
        paint.getTextBounds(text, 0, text.length(), rect); // // 测量文本边界
        return rect.width(); // // 返回宽度
    }

    /**
     * 测量文本绘制高度。
     * 使用Paint.getTextBounds()获取文本的像素高度。
     *
     * @param text 要测量的文本
     * @return 文本高度（像素）
     */
    private int getTextHeight(String text) { // // 测量文本高度
        Paint paint = new Paint(); // // 创建画笔
        Rect rect = new Rect(); // // 创建边界矩形
        paint.getTextBounds(text, 0, text.length(), rect); // // 测量文本边界
        return rect.height(); // // 返回高度
    }

    /**
     * Handler消息处理器，控制Toast的显示/隐藏时序。
     * 三种消息处理：
     *   MSG_TOAST_SHOW:      显示Toast，并延迟msgShowTime后自动隐藏
     *   MSG_TOAST_HIDE:      隐藏Toast，并清除待执行的延迟显示消息
     *   MSG_TOAST_SHOW_DELAY: 延迟触发后，调用show()居中显示
     */
    private Handler handler = new Handler() { // // Handler消息处理器
        @Override
        public void handleMessage(Message msg) { // // 处理消息
            super.handleMessage(msg); // // 调用父类处理
            switch (msg.what) { // // 根据消息类型分发
                case MSG_TOAST_SHOW: // // 显示Toast消息
                    tvToast.setVisibility(View.VISIBLE); // // 设置TextView可见
//                    tvToast.setBackground(RenderScriptGaussianBlur.getInstance().getDrawable()); // // 已注释：高斯模糊背景
//                    RenderScriptGaussianBlur.getInstance().getWaveBmp(tvToast); // // 已注释：波形截图背景

                    if (handler.hasMessages(MSG_TOAST_HIDE)) { // // 已有待执行的隐藏消息
                        handler.removeMessages(MSG_TOAST_HIDE); // // 移除旧的隐藏消息，避免提前隐藏
                    }
                    handler.sendEmptyMessageDelayed(MSG_TOAST_HIDE, msgShowTime); // // 延迟500ms后发送隐藏消息
                    break; // // 结束处理
                case MSG_TOAST_HIDE: // // 隐藏Toast消息
                    if (handler.hasMessages(MSG_TOAST_SHOW_DELAY)) { // // 已有待执行的延迟显示消息
                        handler.removeMessages(MSG_TOAST_SHOW_DELAY); // // 移除延迟显示消息，避免隐藏后又被显示
                    }
                    tvToast.setVisibility(View.GONE); // // 设置TextView不可见
                    break; // // 结束处理
                case MSG_TOAST_SHOW_DELAY: // // 延迟显示Toast消息
                    show(String.valueOf(msg.obj)); // // 调用show()居中显示消息
                    break; // // 结束处理
            }
        }
    };
}
