package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.content.Context; // 导入上下文类
import android.graphics.Rect; // 导入矩形类
import android.os.Bundle; // 导入Bundle类
import android.os.Message; // 导入消息类
import android.util.AttributeSet; // 导入属性集类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.RelativeLayout; // 导入相对布局类

import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载接口
import com.micsig.tbook.tbookscope.MainActivity; // 导入主活动类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.top.layout.auto.TopLayoutAuto; // 导入自动布局
import com.micsig.tbook.tbookscope.top.layout.cursor.TopLayoutCursor; // 导入光标布局
import com.micsig.tbook.tbookscope.top.layout.display.TopLayoutDisplay; // 导入显示布局
import com.micsig.tbook.tbookscope.top.layout.factoryCalibration.TopLayoutFactoryCalibration; // 导入工厂校准布局
import com.micsig.tbook.tbookscope.top.layout.frequencymeter.TopLayoutFrequencyMeter; // 导入频率计布局
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasure; // 导入测量布局
import com.micsig.tbook.tbookscope.top.layout.sample.TopLayoutSample; // 导入采样布局
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSave; // 导入保存布局
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSaveStore; // 导入保存存储布局
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发布局
import com.micsig.tbook.tbookscope.top.layout.userset.TopLayoutUserset; // 导入用户设置布局
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式Bean
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入标题视图
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入带滚动标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入Rx非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入Rx消费者接口


/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 主布局容器                                 │
 * │ 核心职责：管理顶部菜单标签页和对应Fragment的切换显示                 │
 * │ 架构设计：继承RelativeLayout，内嵌TopViewTitleWithScroll和10个     │
 * │          Fragment子页面，通过RxBus响应工作模式切换和缓存加载事件     │
 * │ 数据流向：标题选择 → Fragment切换 → RxBus广播模式状态              │
 * │ 依赖关系：TopViewTitleWithScroll, 各TopLayout*, RxBus, CacheUtil  │
 * │ 使用场景：示波器顶部菜单栏的总控制器，管理测量/保存/光标等子页面     │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by Administrator on 2017/4/5.
 */
public class TopLayoutPopWindow extends RelativeLayout { // 顶部布局弹出窗口，继承相对布局
    public static final int DETAIL_MEASURE = 0; // 测量页面索引
    public static final int DETAIL_SAVE = 1; // 保存页面索引

    public static final int DETAIL_CURSOR=2; // 光标页面索引
    public static final int DETAIL_SAMPLE = 3; // 采样页面索引
    public static final int DETAIL_DISPLAY = 4; // 显示页面索引
    public static final int DETAIL_TRIGGER = 5; // 触发页面索引
    public static final int DETAIL_AUTO = 6; // 自动页面索引
    public static final int DETAIL_USERSET = 7; // 用户设置页面索引
    public static final int DETAIL_FREQUENCYMETER = 8; // 频率计页面索引
    public static final int DETAIL_FACTORYCALIBRATION = 9; // 工厂校准页面索引

    private Context context; // 上下文对象
    private TopViewTitleWithScroll titleWithHead; // 带滚动的标题视图
    private RelativeLayout detailLayout; // 详情布局容器
    private View topSlipBoundary; // 顶部滑动边界视图
    private TopLayoutMeasure layoutMeasure;                     //测量 // 测量布局
    private TopLayoutSave layoutSave;                           //保存 // 保存布局
    private TopLayoutCursor layoutCursor;                       //光标 // 光标布局
    private TopLayoutSample layoutSample;                       //采样 // 采样布局
    private TopLayoutDisplay layoutDisplay;                     //显示 // 显示布局
    private TopLayoutTrigger layoutTrigger;                     //触发 // 触发布局
    private TopLayoutAuto layoutAuto;                           //自动 // 自动布局
    private TopLayoutFrequencyMeter layoutFrequencyMeter;       //频率计 // 频率计布局
    private TopLayoutUserset layoutUserset;                     //用户设置 // 用户设置布局
    private TopLayoutFactoryCalibration layoutFactoryCalibration;//工厂校准 // 工厂校准布局

    private TopMsgPopWindow msgPopWindow; // 消息弹出窗口数据模型

    private Fragment[] fragments = new Fragment[10]; // Fragment数组，存储10个子页面
    private String[] tags = {"Measure", "Save","Cursor", "Sample", "Display", "Trigger" // Fragment标签数组
            , "Auto",  "Userset","FrequencyMeter", "FactoryCalibration"};

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopLayoutPopWindow(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopLayoutPopWindow(Context context, AttributeSet attrs) { // 双参数构造
        super(context, attrs); // 调用父类构造
        this.context = context; // 保存上下文
        initView(); // 初始化视图
        initControl(); // 初始化控制（RxBus订阅）
    }

    /**
     * 初始化RxBus事件订阅
     */
    private void initControl() { // 初始化控制方法
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变化事件
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible); // 订阅SerialWord可见性事件
    }

    View mainView; // 主视图引用


    /**
     * 初始化视图，加载布局和标题数据
     */
    private void initView() { // 初始化视图方法
        mainView = View.inflate(context, R.layout.layout_toppopwindow, this); // 加载主布局

//        setOrientation(VERTICAL);
        detailLayout = (RelativeLayout) findViewById(R.id.topPopDetailPopwindow); // 获取详情布局容器
        titleWithHead = (TopViewTitleWithScroll) findViewById(R.id.topPopTitleWithHead); // 获取标题视图
        topSlipBoundary = findViewById(R.id.topSlipBoundary); // 获取滑动边界视图

//        initLayout();
        if (GlobalVar.get().isFactoryCalibration()) { // 如果是工厂校准模式
            String[] array = context.getResources().getStringArray(R.array.popArrayTitleCalibration); // 获取含校准的标题数组
//            Log.d("Debug", String.format("initView: %s", Arrays.toString(array) ));
            boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
            for (int i = 0; i < array.length; i++) { // 遍历所有标题
                if (i != DETAIL_FREQUENCYMETER) { // 频率计不可见
                    arrayVisible[i] = true; // 其他项可见
                } else { // 频率计
                    arrayVisible[i] = false; // 设为不可见
                }
            }
            titleWithHead.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据
        } else { // 非工厂校准模式
            String[] array = context.getResources().getStringArray(R.array.popArrayTitle); // 获取普通标题数组
//            Log.d("Debug", String.format("initView: %s", Arrays.toString(array) ));
            boolean[] arrayVisible = new boolean[array.length]; // 创建可见性数组
            for (int i = 0; i < array.length; i++) { // 遍历所有标题
                if (i != DETAIL_FREQUENCYMETER) { // 频率计不可见
                    arrayVisible[i] = true; // 其他项可见
                } else { // 频率计
                    arrayVisible[i] = false; // 设为不可见
                }
            }
            titleWithHead.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题数据
        }

        msgPopWindow = new TopMsgPopWindow(DETAIL_MEASURE, true, false); // 创建消息弹出窗口，默认测量页+YT模式
    }

    /**
     * 设置保存的实例状态，恢复Fragment
     * @param savedInstanceState 保存的实例状态
     */
    public void setSavedInstanceState(Bundle savedInstanceState) { // 设置保存的实例状态
        initLayout(savedInstanceState); // 初始化布局（带状态恢复）
    }

    /**
     * 初始化布局，创建或恢复Fragment
     * @param savedInstanceState 保存的实例状态，可为null
     */
    private void initLayout(Bundle savedInstanceState) { // 初始化布局方法
        if (savedInstanceState != null) { // 如果有保存的状态
            for (int i = 0; i < tags.length; i++) { // 遍历所有标签
                fragments[i] = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag(tags[i]); // 通过标签查找已存在的Fragment
            }
        }

        layoutMeasure = fragments[DETAIL_MEASURE] == null ? new TopLayoutMeasure() : (TopLayoutMeasure) fragments[DETAIL_MEASURE]; // 获取或创建测量Fragment
        layoutSave = fragments[DETAIL_SAVE] == null ? new TopLayoutSave() : (TopLayoutSave) fragments[DETAIL_SAVE]; // 获取或创建保存Fragment
        layoutCursor=fragments[DETAIL_CURSOR]==null ?new TopLayoutCursor():(TopLayoutCursor)fragments[DETAIL_CURSOR]; // 获取或创建光标Fragment
        layoutSample = fragments[DETAIL_SAMPLE] == null ? new TopLayoutSample() : (TopLayoutSample) fragments[DETAIL_SAMPLE]; // 获取或创建采样Fragment
        layoutDisplay = fragments[DETAIL_DISPLAY] == null ? new TopLayoutDisplay() : (TopLayoutDisplay) fragments[DETAIL_DISPLAY]; // 获取或创建显示Fragment
        layoutTrigger = fragments[DETAIL_TRIGGER] == null ? new TopLayoutTrigger() : (TopLayoutTrigger) fragments[DETAIL_TRIGGER]; // 获取或创建触Fragment
        layoutAuto = fragments[DETAIL_AUTO] == null ? new TopLayoutAuto() : (TopLayoutAuto) fragments[DETAIL_AUTO]; // 获取或创建自动Fragment
        layoutFrequencyMeter = fragments[DETAIL_FREQUENCYMETER] == null ? new TopLayoutFrequencyMeter() : (TopLayoutFrequencyMeter) fragments[DETAIL_FREQUENCYMETER]; // 获取或创建频率计Fragment
        layoutUserset = fragments[DETAIL_USERSET] == null ? new TopLayoutUserset() : (TopLayoutUserset) fragments[DETAIL_USERSET]; // 获取或创建用户设置Fragment
        layoutFactoryCalibration = fragments[DETAIL_FACTORYCALIBRATION] == null ? new TopLayoutFactoryCalibration() : (TopLayoutFactoryCalibration) fragments[DETAIL_FACTORYCALIBRATION]; // 获取或创建工厂校准Fragment

        if (savedInstanceState == null) { // 如果没有保存的状态（首次创建）
            ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启Fragment事务
                    .add(R.id.topPopDetailPopwindow, layoutMeasure, tags[DETAIL_MEASURE]) // 添加测量Fragment
                    .add(R.id.topPopDetailPopwindow, layoutSave, tags[DETAIL_SAVE]) // 添加保存Fragment
                    .add(R.id.topPopDetailPopwindow, layoutCursor,tags[DETAIL_CURSOR]) // 添加光标Fragment
                    .add(R.id.topPopDetailPopwindow, layoutSample, tags[DETAIL_SAMPLE]) // 添加采样Fragment
                    .add(R.id.topPopDetailPopwindow, layoutDisplay, tags[DETAIL_DISPLAY]) // 添加显示Fragment
                    .add(R.id.topPopDetailPopwindow, layoutTrigger, tags[DETAIL_TRIGGER]) // 添加触Fragment
                    .add(R.id.topPopDetailPopwindow, layoutAuto, tags[DETAIL_AUTO]) // 添加自动Fragment
                    .add(R.id.topPopDetailPopwindow, layoutFrequencyMeter, tags[DETAIL_FREQUENCYMETER]) // 添加频率计Fragment
                    .add(R.id.topPopDetailPopwindow, layoutUserset, tags[DETAIL_USERSET]) // 添加用户设置Fragment
                    .add(R.id.topPopDetailPopwindow, layoutFactoryCalibration, tags[DETAIL_FACTORYCALIBRATION]) // 添加工厂校准Fragment
                    .hide(layoutSave) // 隐藏保存Fragment
                    .hide(layoutCursor) // 隐藏光标Fragment
                    .hide(layoutSample) // 隐藏采样Fragment
                    .hide(layoutDisplay) // 隐藏显示Fragment
                    .hide(layoutTrigger) // 隐藏触Fragment
                    .hide(layoutAuto) // 隐藏自动Fragment
                    .hide(layoutFrequencyMeter) // 隐藏频率计Fragment
                    .hide(layoutUserset) // 隐藏用户设置Fragment
                    .hide(layoutFactoryCalibration) // 隐藏工厂校准Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }
    }

    /**
     * 从缓存恢复标题选中状态
     */
    private void setCache() { // 设置缓存方法
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP); // 从缓存获取上次选中的索引
        titleWithHead.moveOnlyScroll(0); // 先滚动到起始位置
        titleWithHead.setSelected(index); // 设置选中索引
        onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
        resetParamsHandle.sendEmptyMessageDelayed(index, 200); // 延迟200ms发送重置参数消息
    }

    /**
     * 显示测量布局页面
     */
    public void showLayoutMeasure() { // 显示测量页面
        titleWithHead.setSelected(DETAIL_MEASURE); // 选中测量标签
        layoutMeasure.showMeasureCommon(); // 显示测量通用页面
        onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
    }

    /**
     * 显示触发布局页面
     */
    public void showLayoutTrigger() { // 显示触发页面
        titleWithHead.setSelected(DETAIL_TRIGGER); // 选中触发标签
        onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
    }

    /**
     * 显示采样布局页面
     */
    public void showLayoutSample(){ // 显示采样页面
        titleWithHead.setSelected(DETAIL_SAMPLE); // 选中采样标签
        onCheckChange(titleWithHead,titleWithHead.getSelected()); // 触发选中变化
    }

    /**
     * 显示显示布局页面
     */
    public void showLayoutDisplay() { // 显示显示页面
        titleWithHead.setSelected(DETAIL_DISPLAY); // 选中显示标签
        onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
    }

    /**
     * 判断当前是否显示测量通用页面
     * @return true表示当前是测量通用页面
     */
    public boolean isShowLayoutMeasureCommon(){ // 判断是否显示测量通用页面
        boolean b= DETAIL_MEASURE == titleWithHead.getSelected().getIndex(); // 判断一级菜单是否为测量
        boolean b1=layoutMeasure.DETAIL_COMMON==layoutMeasure.getMeasureIdx(); // 判断二级菜单是否为通用
        return b && b1; // 两个条件同时满足
    }

    /**
     * 判断当前是否显示测量页面
     * @return true表示当前是测量页面
     */
    public boolean isShowLayoutMeasure() { // 判断是否显示测量页面
        return DETAIL_MEASURE == titleWithHead.getSelected().getIndex(); // 返回一级菜单是否为测量
    }

    /**
     * 判断当前是否显示触发页面
     * @return true表示当前是触发页面
     */
    public boolean isShowLayoutTrigger() { // 判断是否显示触发页面
        return DETAIL_TRIGGER == titleWithHead.getSelected().getIndex(); // 返回一级菜单是否为触发
    }

    /**
     * 判断当前是否显示采样页面
     * @return true表示当前是采样页面
     */
    public boolean isShowLayoutSample(){ // 判断是否显示采样页面
        return DETAIL_SAMPLE==titleWithHead.getSelected().getIndex(); // 返回一级菜单是否为采样
    }

    /**
     * 判断当前是否显示显示页面
     * @return true表示当前是显示页面
     */
    public boolean isShowLayoutDisplay() { // 判断是否显示显示页面
        return DETAIL_DISPLAY == titleWithHead.getSelected().getIndex(); // 返回一级菜单是否为显示
    }


    /**
     * 获取显示布局对象
     * @return 显示布局
     */
    public TopLayoutDisplay getLayoutDisPlay() { // 获取显示布局
        return layoutDisplay; // 返回显示布局对象
    }

    /**
     * 设置触发器为串行总线1模式，如果当前已在S1-S4则不切换
     */
    public void setTriggerSerialBus1(){ // 设置触发器串行总线1
        int saveIdx= layoutTrigger.getTriggerIdx(); // 获取当前触发索引
        if (saveIdx == TopLayoutTrigger.DETAIL_S1 // 如果当前是S1
                || saveIdx == TopLayoutTrigger.DETAIL_S2 // 或者S2
                || saveIdx == TopLayoutTrigger.DETAIL_S3 // 或者S3
                || saveIdx == TopLayoutTrigger.DETAIL_S4 // 或者S4
        ) {
            return; // 已经在串行总线模式，不切换
        }else { // 不在串行总线模式
            saveTriggerAndShowIdx(TopLayoutTrigger.DETAIL_S1); // 保存当前触发索引并切换到S1
        }
    }

    /**
     * 保存当前触发索引并切换到指定索引
     * @param idx 目标触发索引
     */
    private void saveTriggerAndShowIdx(int idx){ // 保存触发索引并显示指定索引
        int saveIdx= layoutTrigger.getTriggerIdx(); // 获取当前触发索引
        CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(false)); // 缓存标记：不是选项触发
        CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_INDEX,String.valueOf(saveIdx)); // 缓存当前触发索引
        layoutTrigger.setTriggerIdx(idx); // 设置新的触发索引
    }

    /**
     * 恢复之前保存的触发索引
     */
    public void restoreTriggerIdx(){ // 恢复触发索引
        int idx= CacheUtil.get().getInt(CacheUtil.SAVE_TEMP_TRIGGER_INDEX); // 从缓存获取保存的索引
        boolean isOption=CacheUtil.get().getBoolean(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION); // 从缓存获取是否为选项触发
        if (idx<TopLayoutTrigger.DETAIL_COMMON // 索引小于通用页
                || idx>TopLayoutTrigger.DETAIL_S4 // 索引大于S4
                || idx== layoutTrigger.getTriggerIdx() // 索引与当前相同
                || isOption // 是选项触发
        ) return; // 不恢复
        layoutTrigger.setTriggerIdx(idx); // 恢复触发索引
    }

    /**
     * 获取一级菜单当前选中索引
     * @return 一级菜单索引
     */
    public int getFirstMenuIdx(){ // 获取一级菜单索引
        return titleWithHead.getSelected().getIndex(); // 返回标题选中项的索引
    }

    /**
     * 获取二级菜单当前选中索引
     * @return 二级菜单索引
     */
    public int getSecondMenuIdx(){ // 获取二级菜单索引
        int idx=getFirstMenuIdx(); // 获取一级菜单索引
        int index=0; // 默认索引为0
        switch (idx){ // 根据一级菜单索引分发
            case DETAIL_MEASURE:{ // 测量
                index= layoutMeasure.getMeasureIdx(); // 获取测量二级索引
            }break;
            case DETAIL_SAVE:{ // 保存
                index= layoutSave.getSaveIdx(); // 获取保存二级索引
            }break;
            case DETAIL_CURSOR:{ // 光标
                index= layoutCursor.getCursorIdx(); // 获取光标二级索引
            }break;
            case DETAIL_SAMPLE:{ // 采样
                index=layoutSample.getSampleIdx(); // 获取采样二级索引
            }break;
            case DETAIL_DISPLAY:{ // 显示
                index=layoutDisplay.getDisplayIdx(); // 获取显示二级索引
            }break;
            case DETAIL_TRIGGER:{ // 触发
                index=layoutTrigger.getTriggerIdx(); // 获取触发二级索引
            }break;
            case DETAIL_AUTO:{ // 自动
                index=layoutAuto.getAutoIdx(); // 获取自动二级索引
            }break;
            case DETAIL_FREQUENCYMETER:{ // 频率计（无二级菜单）

            }break;
            case DETAIL_USERSET:{ // 用户设置
                index=layoutUserset.getUserIdx(); // 获取用户设置二级索引
            }break;
            case DETAIL_FACTORYCALIBRATION:{ // 工厂校准
                index=0; // 固定为0
            }break;
        }
        return index; // 返回二级菜单索引
    }


    /**
     * 判断当前是否为YT模式
     * @return true表示当前为YT模式
     */
    public boolean isCurYTMode() { // 判断是否为YT模式
        return msgPopWindow.getYtMode().isValue(); // 返回YT模式的值
    }

    /**
     * 缓存加载事件消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 消费事件
            setCache(); // 从缓存恢复状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutPopWindow, true); // 标记本模块缓存已加载
        }
    };

    /**
     * 工作模式变化事件消费者
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() { // 工作模式变化消费者
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception { // 消费事件
            switch (workModeBean.getNextWorkMode()) { // 根据下一个工作模式分发
                case IWorkMode.WorkMode_YT: // YT模式
                case IWorkMode.WorkMode_YTZOOM: // YT缩放模式
                    if (workModeBean.getPreWorkMode() != IWorkMode.WorkMode_XY) { // 如果不是从XY模式切换过来
                        //当不是由xy模式进入yt模式或ytZoom模式的时候，则不向下进行
                        return; // 不处理
                    }
                    titleWithHead.setEnable(DETAIL_MEASURE, true); // 启用测量标签
                    titleWithHead.setEnable(DETAIL_SAVE, true); // 启用保存标签
                    titleWithHead.setEnable(DETAIL_CURSOR,true); // 启用光标标签
                    titleWithHead.setEnable(DETAIL_SAMPLE, true); // 启用采样标签
                    titleWithHead.setEnable(DETAIL_DISPLAY, true); // 启用显示标签
                    titleWithHead.setEnable(DETAIL_TRIGGER, true); // 启用触发标签
                    titleWithHead.setEnable(DETAIL_AUTO, true); // 启用自动标签
                    titleWithHead.setEnable(DETAIL_FREQUENCYMETER, true); // 启用频率计标签
                    titleWithHead.setEnable(DETAIL_USERSET, true); // 启用用户设置标签
                    if (GlobalVar.get().isFactoryCalibration()) { // 如果是工厂校准模式
                        titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, true); // 启用工厂校准标签
                    }

                    msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex()); // 更新消息窗口的选中索引
                    msgPopWindow.setYtMode(true); // 设置为YT模式
                    sendMsg(); // 广播消息
                    break;
                case IWorkMode.WorkMode_XY: // XY模式
                    titleWithHead.setEnable(DETAIL_MEASURE, false); // 禁用测量标签
                    titleWithHead.setEnable(DETAIL_SAVE, false); // 禁用保存标签
                    titleWithHead.setEnable(DETAIL_CURSOR,false); // 禁用光标标签
                    titleWithHead.setEnable(DETAIL_SAMPLE, false); // 禁用采样标签
                    titleWithHead.setEnable(DETAIL_DISPLAY, true); // 保持显示标签启用
                    titleWithHead.setEnable(DETAIL_TRIGGER, false); // 禁用触发标签
                    titleWithHead.setEnable(DETAIL_AUTO, false); // 禁用自动标签
                    titleWithHead.setEnable(DETAIL_FREQUENCYMETER, false); // 禁用频率计标签
                    titleWithHead.setEnable(DETAIL_USERSET, false); // 禁用用户设置标签
                    if (GlobalVar.get().isFactoryCalibration()) { // 如果是工厂校准模式
                        titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, false); // 禁用工厂校准标签
                    }
                    if (titleWithHead.getSelected().getIndex() != DETAIL_DISPLAY) { // 如果当前不在显示页
                        titleWithHead.setSelected(DETAIL_DISPLAY); // 切换到显示页
                        onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
                    }

                    msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex()); // 更新消息窗口的选中索引
                    msgPopWindow.setYtMode(false); // 设置为非YT模式
                    sendMsg(); // 广播消息
                    break;
            }
        }
    };

    /**
     * SerialWord可见性变化事件消费者
     */
    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() { // SerialWord可见性消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // 消费事件
            titleWithHead.setEnable(DETAIL_MEASURE, !aBoolean); // SerialWord可见时禁用测量
            titleWithHead.setEnable(DETAIL_SAVE, !aBoolean); // SerialWord可见时禁用保存
            titleWithHead.setEnable(DETAIL_CURSOR,!aBoolean); // SerialWord可见时禁用光标
            titleWithHead.setEnable(DETAIL_SAMPLE, !aBoolean); // SerialWord可见时禁用采样
            titleWithHead.setEnable(DETAIL_DISPLAY, true); // 显示始终启用
            titleWithHead.setEnable(DETAIL_TRIGGER, true); // 触发始终启用
            titleWithHead.setEnable(DETAIL_AUTO, !aBoolean); // SerialWord可见时禁用自动
            titleWithHead.setEnable(DETAIL_FREQUENCYMETER, !aBoolean); // SerialWord可见时禁用频率计
            titleWithHead.setEnable(DETAIL_USERSET, !aBoolean); // SerialWord可见时禁用用户设置
            if (GlobalVar.get().isFactoryCalibration()) { // 如果是工厂校准模式
                titleWithHead.setEnable(DETAIL_FACTORYCALIBRATION, !aBoolean); // SerialWord可见时禁用工厂校准
            }
            layoutTrigger.setSerialsWordVisible(aBoolean); // 设置触发布局的SerialWord可见性
            if (aBoolean) { // 如果SerialWord可见
                if (titleWithHead.getSelected().getIndex() != DETAIL_TRIGGER) { // 如果当前不在触发页
                    titleWithHead.setSelected(DETAIL_TRIGGER); // 切换到触发页
                    onCheckChange(titleWithHead, titleWithHead.getSelected()); // 触发选中变化
                }
            }

            layoutDisplay.setSerialsWordVisible(aBoolean); // 设置显示布局的SerialWord可见性
            //暂定 固定到Trigger标签位置
//            if (aBoolean) {
//                if (titleWithHead.getSelected().getIndex() != DETAIL_DISPLAY) {
//                    titleWithHead.setSelected(DETAIL_DISPLAY);
//                    onCheckChange(titleWithHead, titleWithHead.getSelected());
//                }
//            }

            msgPopWindow.setCheckIndex(titleWithHead.getSelected().getIndex()); // 更新消息窗口的选中索引
            msgPopWindow.setSerialWord(aBoolean); // 设置SerialWord状态
            sendMsg(); // 广播消息
        }
    };

    /**
     * 通过RxBus广播消息弹出窗口状态
     */
    private void sendMsg() { // 发送消息方法
        RxBus.getInstance().post(RxEnum.TOPSLIP_TITLE, msgPopWindow); // 广播消息弹出窗口数据
    }

    /**
     * 标题项点击监听器，播放按钮音效
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题项点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }
    };

    /**
     * 标题选中变化监听器
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题选中变化监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选中变化回调
            onCheckChange(view, item); // 委托给onCheckChange处理
        }
    };

    /**
     * 处理标题选中变化，切换Fragment显示
     * @param view 触发的视图
     * @param item 选中的标题项
     */
    private void onCheckChange(View view, TopAllBeanTitle item) { // 选中变化处理方法
        msgPopWindow.setCheckIndex(item.getIndex()); // 更新消息窗口的选中索引
        sendMsg(); // 广播消息
        if (item.getIndex() != DETAIL_FACTORYCALIBRATION) { // 如果不是工厂校准页
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP, String.valueOf(item.getIndex())); // 缓存选中索引
        }
        ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启Fragment事务
                .hide(layoutSave) // 隐藏保存Fragment
                .hide(layoutCursor) // 隐藏光标Fragment
                .hide(layoutSample) // 隐藏采样Fragment
                .hide(layoutDisplay) // 隐藏显示Fragment
                .hide(layoutTrigger) // 隐藏触Fragment
                .hide(layoutAuto) // 隐藏自动Fragment
                .hide(layoutFrequencyMeter) // 隐藏频率计Fragment
                .hide(layoutUserset) // 隐藏用户设置Fragment
                .hide(layoutMeasure) // 隐藏测量Fragment
                .hide(layoutFactoryCalibration) // 隐藏工厂校准Fragment
                .commitAllowingStateLoss(); // 提交事务（先全部隐藏）
        switch (item.getIndex()) { // 根据选中索引显示对应Fragment
            case DETAIL_MEASURE:                //测量 // 测量页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutMeasure).commitAllowingStateLoss(); // 显示测量Fragment
                break;
            case DETAIL_SAVE:                   //保存 // 保存页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutSave).commitAllowingStateLoss(); // 显示保存Fragment
                break;
            case DETAIL_CURSOR:                 //光标 // 光标页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutCursor).commitAllowingStateLoss(); // 显示光标Fragment
                break;
            case DETAIL_SAMPLE:                 //采样 // 采样页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutSample).commitAllowingStateLoss(); // 显示采样Fragment
                break;
            case DETAIL_DISPLAY:                //显示 // 显示页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutDisplay).commitAllowingStateLoss(); // 显示显示Fragment
                break;
            case DETAIL_TRIGGER:                //触发 // 触发页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutTrigger).commitAllowingStateLoss(); // 显示触Fragment
                break;
            case DETAIL_AUTO:                   //自动 // 自动页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutAuto).commitAllowingStateLoss(); // 显示自动Fragment
                break;
            case DETAIL_FREQUENCYMETER:         //频率计 // 频率计页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutFrequencyMeter).commitAllowingStateLoss(); // 显示频率计Fragment
                break;
            case DETAIL_USERSET:                //用户设置 // 用户设置页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutUserset).commitAllowingStateLoss(); // 显示用户设置Fragment
                break;
            case DETAIL_FACTORYCALIBRATION:     //工厂校准 // 工厂校准页面
                ((MainActivity) context).getSupportFragmentManager().beginTransaction() // 开启事务
                        .show(layoutFactoryCalibration).commitAllowingStateLoss(); // 显示工厂校准Fragment
                break;
        }
        resetParamsHandle.sendEmptyMessageDelayed(item.getIndex(), 200); // 延迟200ms发送重置参数消息
    }

    /**
     * 重置参数处理器，切换Fragment后重新调整布局高度
     */
    android.os.Handler resetParamsHandle = new android.os.Handler() { // 重置参数Handler
        @Override
        public void handleMessage(Message msg) { // 处理消息
            super.handleMessage(msg); // 调用父类处理

            ViewGroup.LayoutParams layoutParams = getLayoutParams(); // 获取当前布局参数
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; // 设置高度为自适应内容
            setLayoutParams(layoutParams); // 应用布局参数
        }
    };

    /**
     * 获得当前TopSlip的有效点击区域
     * @return 有效点击区域的矩形
     */
    public Rect getValidRect() { // 获取有效点击区域
        int[] boundary = new int[2]; // 边界坐标数组
        topSlipBoundary.getLocationOnScreen(boundary); // 获取边界视图的屏幕坐标
        int[] topSlip = new int[2]; // TopSlip坐标数组
        getLocationOnScreen(topSlip); // 获取自身的屏幕坐标
        return new Rect(getLeft(), topSlip[1], getRight(), boundary[1]); // 构造有效区域矩形
    }

    /**
     * 获取触发布局对象
     * @return 触发布局
     */
    public TopLayoutTrigger getLayoutTrigger() { // 获取触发布局
        return layoutTrigger; // 返回触发布局对象
    }
}
