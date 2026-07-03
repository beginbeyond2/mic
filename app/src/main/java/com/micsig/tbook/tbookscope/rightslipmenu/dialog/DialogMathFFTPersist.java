package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * @auother Liwb
 * @description:
 * @data:2023-9-1 10:25
 */

/*
 * +----------------------------------------------------------------------+
 * |                     DialogMathFFTPersist                             |
 * |                    Math FFT持久化设置对话框                            |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供Math FFT持久化次数的预设选择界面，                           |
 * |          用户从预设列表中选择持久化次数（如Off/2/4/8/16等）               |
 * | 架构设计: 继承AbsoluteLayout的自定义对话框视图，                          |
 * |          内嵌RightViewSelect预设列表，选中后回调关闭                     |
 * | 数据流向: 外部调用setData()传入当前值 -> 用户选择预设项 ->               |
 * |          OnDismissListener回调返回选择结果                              |
 * | 依赖关系: RxBus(对话框开关事件), PlaySound(按键音效),                    |
 * |          CacheUtil(持久化值缓存), RightViewSelect(预设列表控件),        |
 * |          StrUtil(字符串工具)                                           |
 * | 使用场景: Math FFT持久化设置菜单项点击后弹出                             |
 * +----------------------------------------------------------------------+
 */
public class DialogMathFFTPersist extends AbsoluteLayout {
    private Context context;  // 上下文引用
    private ViewGroup rootViewGroup;  // 根视图组
    private DialogProbeMultiple.OnDismissListener onDismissListener;  // 对话框关闭回调监听器（复用DialogProbeMultiple的接口）
    private String cacheKey;  // 缓存键，用于存取持久化值
    private RightViewSelect multiple;  // 预设选择列表控件
    private String[] list;  // 持久化值字符串数组（从资源加载）


    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogMathFFTPersist(Context context) {
        this(context,null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogMathFFTPersist(Context context, AttributeSet attrs) {
        this(context, attrs,0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，委托给四参数构造。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogMathFFTPersist(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);  // 委托给四参数构造
    }

    /**
     * 四参数构造方法，初始化上下文并调用init()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     * @param defStyleRes  默认样式资源
     */
    public DialogMathFFTPersist(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);  // 调用父类构造
           this.context=context;  // 保存上下文引用
           init();  // 执行初始化
    }

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_math_fft_persist布局，设置外部区域点击关闭监听，
     * 初始化预设选择控件，然后隐藏对话框。
     */
    private void init() {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_math_fft_persist, this);  // 填充布局
        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();      // 隐藏对话框
                return false;  // 不消费触摸事件
            }
        });
        initView(rootViewGroup);  // 初始化内部视图控件
        hide();  // 默认隐藏对话框
    }

    /**
     * 初始化内部视图控件，绑定预设选择列表和点击监听器。
     *
     * @param view 根视图，用于查找子控件
     */
    private void initView(View view) {
        multiple = (RightViewSelect) view.findViewById(R.id.multiple);  // 预设选择列表控件
        multiple.setOnItemClickListener(onItemClickListener);  // 设置列表项点击监听
        list = context.getResources().getStringArray(R.array.mathFftPersistValue);  // 从资源加载持久化值数组
    }

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        Tools.PrintControlsLocation("persistence",rootViewGroup);  // 打印控件位置调试信息
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MATH_FFT_PERSIST);  // 发送对话框打开事件
    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MATH_FFT_PERSIST);  // 发送对话框关闭事件
    }

    /**
     * 判断对话框是否正在显示。
     *
     * @return true=正在显示，false=已隐藏
     */
    public boolean isShow() {
        return getVisibility() == VISIBLE;  // 判断可见性
    }

    /**
     * 预设列表项点击监听器。
     * <p>
     * 点击预设项时：
     * 1. 播放按键音效
     * 2. 缓存选择值
     * 3. 隐藏对话框
     * 4. 回调onDismiss返回选择结果
     */
    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();  // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            if (!StrUtil.isEmpty(cacheKey)) {  // 缓存键非空
                CacheUtil.get().putMap(cacheKey, item.getText());  // 缓存选择的持久化值
            }
            hide();  // 隐藏对话框
            if (onDismissListener != null) {  // 如果设置了关闭监听器
                onDismissListener.onDismiss(item.getText());  // 回调选择结果
            }
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 列表项点击后UI刷新回调（未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 列表项点击前UI刷新回调（未使用）
        }
    };

    /**
     * 设置对话框数据并显示。
     * <p>
     * 根据通道索引设置控件颜色，在预设列表中选中当前值，然后显示对话框。
     *
     * @param chIdx            通道索引，用于设置控件颜色
     * @param preString        当前持久化值字符串
     * @param cacheKey         缓存键
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(int chIdx,String preString, String cacheKey, DialogProbeMultiple.OnDismissListener onDismissListener) {
        multiple.setControlColorByChIdx(chIdx);  // 根据通道索引设置控件颜色
        this.cacheKey = cacheKey;  // 保存缓存键
            for (String item : list) {  // 遍历预设值列表
                if (item.equals(preString)) {  // 匹配当前值
                    multiple.setPreString(preString);  // 选中对应的预设项
                }
            }
        this.onDismissListener = onDismissListener;  // 保存关闭监听器
        show();  // 显示对话框
    }

}
