package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

/**
 * Created by yangj on 2017/5/9.
 */

/*
 * +=============================================================================+
 * |                        DialogProbeMultiple                                  |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 探头倍率选择对话框                                     |
 * | 核心职责 : 提供探头倍率的预设选项列表与自定义倍率输入，关闭后回调选中结果            |
 * | 架构设计 : 继承 AbsoluteLayout，以自定义 View 形式嵌入 MainViewGroup 中；         |
 * |           内含 RightViewSelect（预设列表）与 RightViewUserDefineEdit（自定义输入） |
 * | 数据流向 : setData() 接收通道号/缓存键/上一次值 → 展示列表 → 用户选择 →           |
 * |           OnDismissListener.onDismiss() 回传结果                               |
 * | 依赖关系 : RxBus（对话框开关通知）、CacheUtil（自定义倍率缓存）、                   |
 * |           TopDialogFloatKeyBoard（浮层键盘输入）、RightViewSelect/UserDefineEdit  |
 * | 使用场景 : 在通道设置菜单中，用户点击探头倍率项时弹出此对话框选择或自定义倍率值        |
 * +=============================================================================+
 */
public class DialogProbeMultiple extends AbsoluteLayout {
    private Context context;                                                    // 上下文引用
    private OnDismissListener onDismissListener;                                // 对话框关闭回调监听器
    private String cacheKey;                                                    // 缓存键，用于存取用户自定义倍率值
    private RightViewSelect multiple;                                           // 预设倍率选择列表控件
    private RightViewUserDefineEdit userDefineMultiple;                         // 自定义倍率编辑控件
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;                         // 浮层软键盘，用于自定义输入
    private String[] list;                                                      // 预设倍率字符串数组

    private ViewGroup rootViewGroup;                                            // 布局根视图组

    /**
     * 对话框关闭监听接口
     * <p>当用户选择完倍率后，通过此接口回调选中结果</p>
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回选中的倍率字符串 */
        void onDismiss(String result);
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public DialogProbeMultiple(Context context) {
        this(context, null);                                                    // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogProbeMultiple(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终构造入口）
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogProbeMultiple(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                    // 调用父类构造
        this.context = context;                                                 // 保存上下文引用
        init();                                                                 // 执行初始化
    }

    //[440, 37]	260	358

    /**
     * 初始化对话框布局与交互
     * <p>加载布局、设置外部区域触摸关闭、初始化子视图、默认隐藏</p>
     */
    private void init() {
        setClickable(true);                                                     // 设置可点击，拦截触摸事件
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_probemultiple, this); // 加载对话框布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();                                                         // 触摸外部区域则隐藏对话框
                return false;                                                   // 不消费事件，继续传递
            }
        });
        initView(rootViewGroup);                                                // 初始化子视图控件
        hide();                                                                 // 默认隐藏对话框
    }

    /**
     * 初始化子视图控件及监听器
     * @param view 根视图
     */
    private void initView(View view) {
        multiple = (RightViewSelect) view.findViewById(R.id.multiple);          // 获取预设倍率选择列表控件
        userDefineMultiple = view.findViewById(R.id.userDefineMultiple);        // 获取自定义倍率编辑控件
        multiple.setOnItemClickListener(onItemClickListener);                   // 设置预设列表点击监听
        userDefineMultiple.setOnEditClickListener(onEditClickListener);         // 设置自定义编辑点击监听
        list = context.getResources().getStringArray(R.array.channelProbeTypeMultiple); // 加载预设倍率字符串数组
    }

    /**
     * 判断指定探头倍率是否存在于预设列表中
     * @param probeRate 探头倍率字符串
     * @return 存在返回 true，否则返回 false
     */
    public boolean isExistProbeRate(String probeRate) {
        for (String aList : list) {                                             // 遍历预设列表
            if (probeRate.equals(aList)) {                                      // 匹配比较
                return true;                                                    // 找到匹配项
            }
        }
        return false;                                                           // 未找到匹配项
    }

    /**
     * 显示对话框
     * <p>设置可见性并通过 RxBus 发送对话框打开事件</p>
     */
    public void show() {
        setVisibility(VISIBLE);                                                 // 设置对话框可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_PROBEMULTIPLE); // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogProbeMultiple",rootViewGroup);       // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     * <p>设置不可见并通过 RxBus 发送对话框关闭事件</p>
     */
    public void hide() {
        setVisibility(GONE);                                                    // 设置对话框不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_PROBEMULTIPLE); // 发送对话框关闭事件
    }


    /**
     * 判断对话框当前是否可见
     * @return 可见返回 true，否则返回 false
     */
    public boolean isShow() {
        return getVisibility() == VISIBLE;                                      // 判断可见性状态
    }

    /**
     * 设置对话框数据并显示
     * @param channelNum        通道编号，用于控件颜色标识
     * @param preString         上一次选中的倍率值
     * @param cacheKey          缓存键，用于读取/存储自定义倍率值
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(int channelNum,String preString, String cacheKey, OnDismissListener onDismissListener) {
//        Log.d(Tag.Debug, String.format("DialogProbeMultiple.setData: %d",channelNum ));
        multiple.setControlColorByChIdx(channelNum);                            // 根据通道号设置预设列表颜色
        userDefineMultiple.setControlColorByChIdx(channelNum);                  // 根据通道号设置自定义编辑控件颜色


        this.cacheKey = cacheKey;                                               // 保存缓存键
        String userDefine = CacheUtil.get().getString(cacheKey);                // 从缓存读取用户自定义倍率值
        if (StrUtil.isEmpty(userDefine)) {                                      // 如果没有自定义值
            userDefineMultiple.setText("");                                     // 清空自定义编辑框
            for (String item : list) {                                          // 遍历预设列表
                if (item.equals(preString)) {                                   // 匹配上一次选中值
                    multiple.setPreString(preString);                           // 设置预设列表的选中项
                }
            }
        } else {                                                                // 如果有自定义值
            userDefineMultiple.setText(userDefine);                             // 显示自定义值
            multiple.setSelectIndex(-1);                                        // 取消预设列表的选中项
        }
        this.onDismissListener = onDismissListener;                             // 保存关闭回调监听器
        show();                                                                 // 显示对话框
    }

    /**
     * 预设倍率列表项点击监听器
     * <p>点击预设项后清除自定义缓存、隐藏对话框、回调选中结果</p>
     */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();                               // 播放按钮音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            if (!StrUtil.isEmpty(cacheKey)) {                                   // 如果缓存键非空
                CacheUtil.get().putMap(cacheKey, "");                           // 清除自定义倍率缓存
            }
            hide();                                                             // 隐藏对话框
            if (onDismissListener != null) {                                    // 如果回调监听器存在
                onDismissListener.onDismiss(item.getText());                    // 回调选中项文本
            }
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    /**
     * 自定义倍率编辑点击监听器
     * <p>点击自定义输入后弹出浮层键盘，用户输入完成后校验范围并回调结果</p>
     */
    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            if (dialogFloatKeyBoard == null) {                                  // 如果浮层键盘未初始化
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 从 Activity 获取浮层键盘
                dialogFloatKeyBoard.bringToFront();                             // 将键盘提升到最前层
            }
            dialogFloatKeyBoard.setFloatmkData(text.replace("X", ""), view, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置键盘数据（去除X后缀）
                @Override
                public void onDismiss(View fromView, String show) {
                    if (StrUtil.isEmpty(show) || "0".equals(show)) {            // 空值或0则无效，不处理
                        return;                                                 // 直接返回
                    }
                    double d = TBookUtil.getDoubleFromM(show);                  // 将带单位的字符串转为数值
                    if (d < 0.001) {                                            // 小于0.001则设为最小值1m
                        show = "1m";                                            // 设置最小倍率
                    } else if (d > 1000 * 1000) {                               // 大于1,000,000则设为最大值999k
                        show = "999k";                                          // 设置最大倍率
                    } else {                                                    // 在有效范围内，格式化显示
                        String unit = "";                                       // 单位字符串
                        if (show.contains("m")) {                               // 包含m（毫）单位
                            unit = "m";                                         // 设置单位为m
                            show = show.replace("m", "");                       // 去除单位后提取数值
                        } else if (show.contains("k")) {                        // 包含k（千）单位
                            unit = "k";                                         // 设置单位为k
                            show = show.replace("k", "");                       // 去除单位后提取数值
                        }
                        if (show.length() >= 3) {                               // 数值部分长度超过3位时截取
                            String substring = show.substring(0, 3);            // 截取前3位
                            if (substring.contains(".") && show.length() >= 4) { // 若含小数点且总长>=4
                                substring = show.substring(0, 4);               // 截取前4位（含小数点）
                            }
                            show = substring;                                   // 使用截取后的字符串
                        }
                        show = show + unit;                                     // 拼回单位
                    }
                    userDefineMultiple.setText(show + "X");                     // 设置自定义编辑框文本（加X后缀）
                    if (!StrUtil.isEmpty(cacheKey)) {                           // 如果缓存键非空
                        CacheUtil.get().putMap(cacheKey, userDefineMultiple.getText()); // 缓存自定义倍率值
                    }
                    hide();                                                     // 隐藏对话框
                    if (onDismissListener != null) {                            // 如果回调监听器存在
                        onDismissListener.onDismiss(userDefineMultiple.getText()); // 回调自定义倍率值
                    }
                }
            });
        }
    };
}
