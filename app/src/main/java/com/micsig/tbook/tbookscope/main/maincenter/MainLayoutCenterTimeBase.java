package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径,位于主界面中心区域的时基选择模块

/*
 * ========================================================================================
 *                                                                                  *
 *                     MainLayoutCenterTimeBase                                          *
 *                         主界面中心时基选择布局                                         *
 *                                                                                  *
 * ========================================================================================
 * 
 * 【模块定位】
 *   位于main.maincenter包中,作为示波器时基(Time Base)选择功能的UI布局容器,
 *   负责显示和管理时基档位选择列表,是时基调节功能的可视化交互界面
 * 
 * 【核心职责】
 *   1. 生成和显示时基档位列表(普通通道/数学运算FFT/参考通道)
 *   2. 处理时基档位的点击选择事件
 *   3. 根据不同通道类型动态生成对应的时基列表
 *   4. 监听时基档位变化事件并更新UI显示
 *   5. 提供时基列表的显示/隐藏控制
 * 
 * 【架构设计】
 *   采用自定义RelativeLayout布局容器模式,内部封装MTimeBaseSelector时基选择器组件。
 *   使用观察者模式(Observer)监听时基变化事件,通过RxJava响应式框架处理UI更新。
 *   采用策略模式根据不同通道类型(普通/Math FFT/Ref)生成不同的时基列表。
 *   使用回调接口(OnClickItemListener)将用户选择事件传递给外部调用者。
 * 
 * 【数据流向】
 *   HorizontalAxis(水平轴时基数据) → MainLayoutCenterTimeBase(生成时基列表)
 *   → MTimeBaseSelector(UI显示) → 用户点击选择 → OnClickItemListener回调
 *   → 外部调用者处理时基变更 → 更新底层时基设置
 * 
 * 【依赖关系】
 *   依赖: HorizontalAxis(水平轴)、HorizontalAxisMath(数学运算轴)、HorizontalAxisRef(参考通道轴)
 *         MTimeBaseSelector(时基选择器UI)、ChannelFactory(通道工厂)、RxBus(响应式总线)
 *   被依赖: MainViewGroup(主界面容器)、时基调节相关UI组件
 * 
 * 【使用场景】
 *   1. 用户点击时基调节按钮时弹出时基档位选择列表
 *   2. 切换通道类型时动态更新时基列表(普通/Math FFT/Ref不同类型)
 *   3. 底层时基档位范围变化时自动刷新时基列表显示
 *   4. 需要查找特定时基档位对应的索引位置时进行查询
 */

import android.content.Context; // 导入Android上下文类
import android.text.TextUtils; // 导入文本工具类
import android.util.AttributeSet; // 导入属性集类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图容器基类
import android.widget.RelativeLayout; // 导入相对布局类

import androidx.annotation.Nullable; // 导入可空注解

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Data.WaveData; // 导入波形数据类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath; // 导入数学运算水平轴类
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef; // 导入参考通道水平轴类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图容器类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入响应式总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入响应式枚举类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.MTimeBaseSelector; // 导入时基选择器UI组件
import com.micsig.tbook.ui.util.TBookUtil; // 导入工具类

import java.util.ArrayList; // 导入ArrayList列表类
import java.util.List; // 导入List接口
import java.util.Observable; // 导入被观察者类
import java.util.Observer; // 导入观察者接口

/**
 * 主界面中心时基选择布局类
 * 
 * 用于显示和管理示波器的时基档位选择列表,支持普通通道、数学运算FFT和参考通道
 * 三种不同类型的时基列表显示。通过观察者模式监听时基变化事件,自动更新UI显示。
 * 用户点击选择时基档位后通过回调接口通知外部调用者处理时基变更。
 * 
 * @author yangj
 * @version 1.0
 * @since 2017/12/15
 */
public class MainLayoutCenterTimeBase extends RelativeLayout {
    
    /** 日志标签 */
    private static final String TAG = "MainLayoutCenterTimeBase";
    
    /** Android上下文对象 */
    private Context context;
    
    /** 时基档位列表 */
    private List<TimeBaseScale> list;
    
    /** RecyclerView布局容器 */
    private RelativeLayout recyclerLayout;
    
    /** 时基列表适配器(已废弃,改用MTimeBaseSelector) */
    private MainAdapterCenterTimeBase adapter;
    
    /** 时基项点击监听器 */
    private OnClickItemListener onClickItemListener;
    
    /** 自定义时基选择器组件 */
    private MTimeBaseSelector mTimeBaseSelector;
    
    /** 根视图容器 */
    private ViewGroup rootViewGroup;

    /**
     * 时基项点击监听接口
     * 
     * 定义时基档位被点击时的回调方法,用于将用户选择事件传递给外部调用者。
     */
    public interface OnClickItemListener {
        /**
         * 时基项点击回调方法
         * 
         * @param scale 被点击的时基档位对象
         */
        void onClickItem(TimeBaseScale scale);
    }

    /**
     * 设置时基项点击监听器
     * 
     * @param onClickItemListener 时基项点击监听器对象
     */
    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener; // 设置点击监听器
    }

    /**
     * 构造方法(单参数)
     * 
     * @param context Android上下文对象
     */
    public MainLayoutCenterTimeBase(Context context) {
        this(context, null); // 调用双参数构造方法
    }

    /**
     * 构造方法(双参数)
     * 
     * @param context Android上下文对象
     * @param attrs 属性集对象
     */
    public MainLayoutCenterTimeBase(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * 构造方法(三参数)
     * 
     * 完成布局初始化,注册时基变化事件观察者。
     * 
     * @param context Android上下文对象
     * @param attrs 属性集对象
     * @param defStyleAttr 默认样式属性
     */
    public MainLayoutCenterTimeBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        this.context = context; // 保存上下文对象
        initView(); // 初始化视图组件
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE_LIST, xAxis); // 注册时基变化事件观察者
    }

    /**
     * 初始化视图组件
     * 
     * 加载布局文件,初始化时基选择器组件,设置点击监听,生成时基列表。
     */
    private void initView() {
        rootViewGroup = (ViewGroup) inflate(context, R.layout.layout_maincenter_timebase, this); // 加载布局文件到根视图容器

        findViewById(R.id.outView).setOnClickListener(new OnClickListener() { // 设置外部区域点击监听
            @Override
            public void onClick(View v) {
                hide(); // 点击外部区域时隐藏时基列表
            }
        });
        recyclerLayout = (RelativeLayout) findViewById(R.id.recyclerLayout); // 获取RecyclerView布局容器

        mTimeBaseSelector = new MTimeBaseSelector(context); // 创建自定义时基选择器组件
        mTimeBaseSelector.setOnClickEvent(onClickEvent); // 设置时基选择器点击事件回调
        recyclerLayout.addView(mTimeBaseSelector); // 将时基选择器添加到布局容器

        list = new ArrayList<TimeBaseScale>(); // 创建时基档位列表
        setList(); // 设置时基列表数据
        generateChannelList(); // 生成通道时基列表
        setViewLocation(); // 设置视图位置
        hide(); // 默认隐藏时基列表
    }

    /**
     * 设置时基列表数据
     * 
     * 将时基档位列表转换为字符串列表,并更新时基选择器UI显示。
     */
    private void setList() {
        if (list == null) return; // 判断列表是否为null,为null则直接返回
        List<String> sList = new ArrayList<>(); // 创建字符串列表
        for (int i = 0; i < list.size(); i++) { // 遍历时基档位列表
            sList.add(list.get(i).getScale()); // 将时基档位的字符串表示添加到字符串列表
        }
        mTimeBaseSelector.initDataUI(sList); // 初始化时基选择器UI数据
    }

    /**
     * 设置视图位置
     * 
     * 根据屏幕尺寸和布局参数调整时基选择器视图的位置和边距。
     */
    private void setViewLocation() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) recyclerLayout.getLayoutParams(); // 获取布局参数
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.leftBarWidth); // 设置左边距为左侧工具栏宽度
        int offset = 4; // 设置偏移量默认值
        if (ScopeBase.getNewHeight() == 1000) { // 判断屏幕高度是否为1000(特定型号)
            offset = 6; // 调整偏移量
        }
        layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.mainBottomHeight) + offset; // 设置底部边距为底部高度加偏移量
        recyclerLayout.setLayoutParams(layoutParams); // 应用布局参数到视图
    }

    /**
     * 生成通道时基列表
     * 
     * 根据水平轴时基数据生成普通通道的时基档位列表。
     * 
     * @return 生成的时基档位列表
     */
    public List<TimeBaseScale> generateChannelList() {
        list.clear(); // 清空当前列表
        List<Double> axis = HorizontalAxis.getInstance().getxAxis(); // 获取水平轴时基数据列表
        for (int i = 0; i < axis.size(); i++) { // 遍历时基数据列表
            String s = TBookUtil.getMFromDouble(axis.get(i)); // 将时基数值转换为字符串表示
            if (TextUtils.isEmpty(s)) continue; // 判断字符串是否为空,为空则跳过
            list.add(new TimeBaseScale(i, s + "s")); // 创建时基档位对象并添加到列表
        }
        setList(); // 更新时基选择器UI显示
        setViewLocation(); // 更新视图位置
        return list; // 返回生成的时基列表
    }

    /**
     * 生成数学运算FFT波形时基列表
     * 
     * 根据数学运算通道的FFT水平轴数据生成时基档位列表(频率单位Hz)。
     * 
     * @param chIdx 数学运算通道索引
     * @return 生成的时基档位列表
     */
    public List<TimeBaseScale> generateMathFFTWaveList(int chIdx) {
        list.clear(); // 清空当前列表
        HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT(); // 获取数学通道的FFT水平轴
        List<Double> xAxisList = horizontalAxisMath.getxAxis(); // 获取FFT频率轴数据列表

        String tail = "Hz"; // 设置单位后缀为Hz(频率单位)
        if (!xAxisList.isEmpty()) { // 判断频率轴数据列表是否非空
            for (int i = 0; i < xAxisList.size(); i++) { // 遍历频率轴数据列表
                if (i < horizontalAxisMath.getMinScaleId() || i > horizontalAxisMath.getMaxScaleId()) continue; // 判断是否在有效范围内,超出范围则跳过
                String s = TBookUtil.getMFromDouble(xAxisList.get(i)); // 将频率数值转换为字符串表示
                if (TextUtils.isEmpty(s)) continue; // 判断字符串是否为空,为空则跳过
                list.add(new TimeBaseScale(i, s + tail)); // 创建时基档位对象并添加到列表
            }
        }
        setList(); // 更新时基选择器UI显示
        setViewLocation(); // 更新视图位置
        return list; // 返回生成的时基列表
    }

    /**
     * 仅生成通道时基列表(不更新UI)
     * 
     * 根据水平轴时基数据生成普通通道的时基档位列表,但不更新UI显示。
     * 用于外部查询和索引查找场景。
     * 
     * @return 生成的时基档位列表
     */
    public List<TimeBaseScale> onlyGenerateChannelList() {
        List<TimeBaseScale> list = new ArrayList<TimeBaseScale>(); // 创建新的时基档位列表
        List<Double> axis = HorizontalAxis.getInstance().getxAxis(); // 获取水平轴时基数据列表
        for (int i = 0; i < axis.size(); i++) { // 遍历时基数据列表
            String s = TBookUtil.getMFromDouble(axis.get(i)); // 将时基数值转换为字符串表示
            if (TextUtils.isEmpty(s)) continue; // 判断字符串是否为空,为空则跳过
            list.add(new TimeBaseScale(i, s + "s")); // 创建时基档位对象并添加到列表
        }
        return list; // 返回生成的时基列表
    }

    /**
     * 仅生成参考通道时基列表(不更新UI)
     * 
     * 根据参考通道的水平轴数据生成时基档位列表,但不更新UI显示。
     * 用于外部查询和索引查找场景。
     * 
     * @param refChannel 参考通道对象,为null时使用当前顶层参考通道
     * @return 生成的时基档位列表
     */
    public List<TimeBaseScale> onlyGenerateRefChannelList(RefChannel refChannel) {
        List<TimeBaseScale> list = new ArrayList<TimeBaseScale>(); // 创建新的时基档位列表
        if (refChannel == null) { // 判断参考通道对象是否为null
            refChannel = (RefChannel) ChannelFactory.getInstance().getTopRefChannel(); // 获取当前顶层参考通道
        }
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef(); // 获取参考通道的水平轴
        List<Double> xAxisList = horizontalAxisRef.getxAxis(); // 获取水平轴数据列表
        String tail = "s"; // 设置单位后缀默认为s(时间单位)
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) { // 判断参考类型是否为Math FFT
            tail = "Hz"; // 调整单位后缀为Hz(频率单位)
        }
        if (!xAxisList.isEmpty()) { // 判断水平轴数据列表是否非空
            for (int i = 0; i < xAxisList.size(); i++) { // 遍历水平轴数据列表
                String s = TBookUtil.getMFromDouble(xAxisList.get(i)); // 将数值转换为字符串表示
                if (TextUtils.isEmpty(s)) continue; // 判断字符串是否为空,为空则跳过
                list.add(new TimeBaseScale(i, s + tail)); // 创建时基档位对象并添加到列表
            }
        }
        return list; // 返回生成的时基列表
    }

    /**
     * 生成参考通道时基列表
     * 
     * 根据参考通道的水平轴数据生成时基档位列表,并根据参考类型自动设置单位(时间s或频率Hz)。
     * 
     * @param refChannel 参考通道对象,为null时使用当前顶层参考通道
     * @return 生成的时基档位列表
     */
    public List<TimeBaseScale> generateRefChannelList(RefChannel refChannel) {
        list.clear(); // 清空当前列表
        if (refChannel == null) { // 判断参考通道对象是否为null
            refChannel = (RefChannel) ChannelFactory.getInstance().getTopRefChannel(); // 获取当前顶层参考通道
        }
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef(); // 获取参考通道的水平轴
        List<Double> xAxisList = horizontalAxisRef.getxAxis(); // 获取水平轴数据列表
        String tail = "s"; // 设置单位后缀默认为s(时间单位)
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) { // 判断参考类型是否为Math FFT
            tail = "Hz"; // 调整单位后缀为Hz(频率单位)
        }
        if (!xAxisList.isEmpty()) { // 判断水平轴数据列表是否非空
            for (int i = 0; i < xAxisList.size(); i++) { // 遍历水平轴数据列表
                String s = TBookUtil.getMFromDouble(xAxisList.get(i)); // 将数值转换为字符串表示
                if (TextUtils.isEmpty(s)) continue; // 判断字符串是否为空,为空则跳过
                list.add(new TimeBaseScale(i, s + tail)); // 创建时基档位对象并添加到列表
            }
        }
        setList(); // 更新时基选择器UI显示
        setViewLocation(); // 更新视图位置
        return list; // 返回生成的时基列表
    }

    /**
     * 获取时基列表
     * 
     * 根据当前激活通道的类型和时基设置,返回对应的时基档位列表。
     * 支持普通通道、数学运算FFT和参考通道三种类型。
     * 
     * @return 对应的时基档位列表
     */
    public List<TimeBaseScale> getList() {
        int chIdx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        int timeBaseIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 获取参考通道时基设置索引
        if (timeBaseIndex == 0) { // 判断时基设置是否为跟随模拟通道模式
            if (ChannelFactory.isRefCh(chIdx)) { // 判断当前通道是否为参考通道
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx); // 获取参考通道对象
                if (refChannel.getRefType() == WaveData.FFT_WAVE) { // 判断参考类型是否为FFT波形
                    return generateRefChannelList(refChannel); // 返回参考通道FFT时基列表
                } else { // 参考类型为普通波形
                    return generateChannelList(); // 返回普通通道时基列表
                }
            }
            if (ChannelFactory.isMath_FFT_Ch(chIdx)) { // 判断当前通道是否为数学运算FFT通道
                return generateMathFFTWaveList(chIdx); // 返回数学运算FFT时基列表
            } else { // 当前通道为普通通道
                return generateChannelList(); // 返回普通通道时基列表
            }
        } else { // 时基设置为独立模式
            if (ChannelFactory.isRefCh(chIdx)) { // 判断当前通道是否为参考通道
                return generateRefChannelList(null); // 返回顶层参考通道时基列表
            } else if (ChannelFactory.isMath_FFT_Ch(chIdx)) { // 判断当前通道是否为数学运算FFT通道
                return generateMathFFTWaveList(chIdx); // 返回数学运算FFT时基列表
            } else { // 当前通道为普通通道或数学双波形通道
                return generateChannelList(); // 返回普通通道时基列表
            }
        }
    }

    /**
     * 获取内部时基列表
     * 
     * 返回当前保存的时基档位列表对象。
     * 
     * @return 内部时基档位列表
     */
    public List<TimeBaseScale> getList1() {
        return list; // 返回时基档位列表
    }

    /**
     * 获取普通时基索引
     * 
     * 根据时基字符串查找在普通通道时基列表中的索引位置。
     * 
     * @param scale 时基字符串(如"1ms")
     * @return 时基索引位置,未找到则返回0
     */
    public int getNormalIndex(String scale) {
        scale = scale.replace(" ", ""); // 去除字符串中的空格
        scale = scale.replace("u", "μ"); // 将字母u替换为希腊字母μ(微秒单位符号)

        List<TimeBaseScale> scaleList = generateChannelList(); // 生成普通通道时基列表
        for (int i = 0; i < scaleList.size(); i++) { // 遍历时基列表
            String str = scaleList.get(i).getScale(); // 获取列表中的时基字符串
            str = str.replace(" ", ""); // 去除字符串中的空格
            if (str.equals(scale)) { // 判断时基字符串是否匹配
                return i; // 返回匹配的索引位置
            }
        }
        return 0; // 未找到匹配项,返回默认索引0
    }

    /**
     * 获取数学运算FFT时基索引
     * 
     * 根据时基字符串查找在数学运算FFT时基列表中的索引位置。
     * 
     * @param scale 时基字符串(频率单位,如"1kHz")
     * @return 时基索引位置,未找到或非FFT通道则返回0
     */
    public int getMathFFTIndex(String scale) {
        int chIdx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
        if (ChannelFactory.isMath_FFT_Ch(chIdx)) { // 判断当前通道是否为数学运算FFT通道
            List<TimeBaseScale> scaleList = generateMathFFTWaveList(chIdx); // 生成数学运算FFT时基列表
            for (int i = 0; i < scaleList.size(); i++) { // 遍历时基列表
                if (scaleList.get(i).getScale().equals(scale)) { // 判断时基字符串是否匹配
                    return i; // 返回匹配的索引位置
                }
            }
        }
        return 0; // 未找到匹配项或非FFT通道,返回默认索引0
    }

    /**
     * 获取参考通道时基索引
     * 
     * 根据时基字符串查找在参考通道时基列表中的索引位置。
     * 
     * @param refChannel 参考通道对象
     * @param scale 时基字符串(时间或频率单位)
     * @return 时基索引位置,未找到则返回0
     */
    public int getRefIndex(RefChannel refChannel, String scale) {
        List<TimeBaseScale> scaleList = generateRefChannelList(refChannel); // 生成参考通道时基列表
        for (int i = 0; i < scaleList.size(); i++) { // 遍历时基列表
            if (scaleList.get(i).getScale().equals(scale)) { // 判断时基字符串是否匹配
                return i; // 返回匹配的索引位置
            }
        }
        return 0; // 未找到匹配项,返回默认索引0
    }

    /**
     * 显示时基列表
     * 
     * 设置时基列表为可见状态,并发送对话框打开事件通知。
     */
    public void show() {
        setVisibility(VISIBLE); // 设置视图为可见状态
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_CENTERTIMEBASE); // 发送对话框打开事件通知
        Tools.PrintControlsLocation("MainLayoutCenterTimeBase", rootViewGroup); // 打印视图位置信息(用于调试)
    }

    /**
     * 显示时基列表并设置选中项
     * 
     * 设置时基列表为可见状态,指定选中项,并发送对话框打开事件通知。
     * 
     * @param s 要选中的时基字符串
     */
    public void show(String s) {
        mTimeBaseSelector.setSelectItems(s); // 设置时基选择器的选中项
        setVisibility(VISIBLE); // 设置视图为可见状态
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_CENTERTIMEBASE); // 发送对话框打开事件通知
    }

    /**
     * 隐藏时基列表
     * 
     * 设置时基列表为隐藏状态,并发送对话框关闭事件通知。
     */
    public void hide() {
        setVisibility(GONE); // 设置视图为隐藏状态
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_CENTERTIMEBASE); // 发送对话框关闭事件通知
    }

    /**
     * 时基选择器点击事件回调
     * 
     * 处理时基选择器的触摸按下、抬起和项变更事件,将事件传递给外部监听器。
     */
    private MTimeBaseSelector.OnClickEvent onClickEvent = new MTimeBaseSelector.OnClickEvent() {
        /**
         * 触摸按下事件回调
         * 
         * @param index 触摸项的索引位置
         * @param value 触摸项的值字符串
         */
        @Override
        public void onTouchDown(int index, String value) {
            if (onClickItemListener != null) { // 判断外部监听器是否为null
                onClickItemListener.onClickItem(new TimeBaseScale(index, value)); // 调用外部监听器的点击回调方法
            }
        }

        /**
         * 触摸抬起事件回调
         * 
         * @param index 触摸项的索引位置
         * @param value 触摸项的值字符串
         */
        @Override
        public void onTouchUp(int index, String value) {
            hide(); // 隐藏时基列表
        }

        /**
         * 项变更事件回调
         * 
         * @param index 变更项的索引位置
         * @param value 变更项的值字符串
         */
        @Override
        public void onItemChange(int index, String value) {
            if (onClickItemListener != null) { // 判断外部监听器是否为null
                onClickItemListener.onClickItem(new TimeBaseScale(index, value)); // 调用外部监听器的点击回调方法
            }
        }
    };

    /**
     * 时基适配器点击监听器(已废弃)
     * 
     * 处理时基列表项的点击事件,隐藏列表并通知外部监听器。
     */
    private MainAdapterCenterTimeBase.OnClickListener onClickListener = new MainAdapterCenterTimeBase.OnClickListener() {
        /**
         * 点击回调方法
         * 
         * @param scale 被点击的时基档位对象
         */
        @Override
        public void onClick(TimeBaseScale scale) {
            hide(); // 隐藏时基列表
            if (onClickItemListener != null) { // 判断外部监听器是否为null
                onClickItemListener.onClickItem(scale); // 调用外部监听器的点击回调方法
            }
        }
    };

    /**
     * 水平轴时基变化观察者
     * 
     * 监听水平轴时基档位范围变化事件,更新缓存并刷新时基列表显示。
     */
    private Observer xAxis = new Observer() {
        /**
         * 观察者更新回调方法
         * 
         * @param observable 被观察者对象
         * @param data 事件数据对象
         */
        @Override
        public void update(Observable observable, Object data) {
            EventBase eventBase = (EventBase) data; // 将事件数据转换为EventBase对象
            if (eventBase.getId() == EventFactory.EVENT_TIME_SCALE_LIST) { // 判断事件ID是否为时基列表变化事件
                MainLayoutCenterTimeBase.this.post(new Runnable() { // 在UI线程中执行任务
                    @Override
                    public void run() {
                        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_MIN, String.valueOf(HorizontalAxis.getMinGear())); // 缓存时基最小档位值
                        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_TIMEBASE_MAX, String.valueOf(HorizontalAxis.getMaxGear())); // 缓存时基最大档位值
                        getList(); // 刷新时基列表显示
                    }
                });
            }
        }
    };
}