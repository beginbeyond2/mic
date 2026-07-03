package com.micsig.tbook.tbookscope.main.maincenter.serialsword;  // 定义串口字显示主布局中心区域类的包路径

import android.content.Context;  // 导入上下文类，用于获取应用环境和资源
import android.os.Bundle;  // 导入Bundle类，用于保存和恢复Fragment状态
import android.os.Handler;  // 导入Handler类，用于处理消息队列和定时任务
import android.os.Message;  // 导入Message类，用于Handler消息传递
import android.util.AttributeSet;  // 导入属性集类，用于XML布局属性解析
import android.view.View;  // 导入View类，Android视图基类
import android.widget.RadioButton;  // 导入单选按钮控件，用于通道选择
import android.widget.RadioGroup;  // 导入单选组控件，管理多个RadioButton
import android.widget.RelativeLayout;  // 导入相对布局，作为本类基类

import androidx.fragment.app.Fragment;  // 导入Fragment类，用于Fragment管理

import com.micsig.tbook.tbookscope.LoadCache;  // 导入加载缓存消息类
import com.micsig.tbook.tbookscope.MainActivity;  // 导入主活动类，用于Fragment管理
import com.micsig.tbook.tbookscope.R;  // 导入资源类，访问布局和控件ID
import com.micsig.tbook.tbookscope.main.maincenter.MainLeftMsgMenuRunStop;  // 导入运行停止消息类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;  // 导入右侧其他消息类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;  // 导入右侧串口布局类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;  // 导入右侧串口消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound;  // 导入声音播放工具类
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;  // 导入顶部显示消息类
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;  // 导入文本混合显示类
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 导入缓存工具类
import com.micsig.tbook.ui.util.ScreenUtil;  // 导入屏幕工具类

import io.reactivex.rxjava3.functions.Consumer;  // 导入RxJava消费者接口


/**
 * ***********************************************************************************
 * * 串口总线文本显示主布局容器类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - 串口总线文本信息显示模块的顶层容器和通道切换控制器
 * *
 * * 【核心职责】
 * *   1. 管理S1/S2/S3/S4/S12五个串口通道的文本显示Fragment
 * *   2. 实现通道标签页的动态显示和切换逻辑
 * *   3. 监听RxBus事件并响应缓存加载、运行状态、配置变更等消息
 * *   4. 根据通道开启状态和串口类型动态更新标签标题
 * *   5. 处理外部按键事件触发文本列表滚动
 * *
 * * 【架构设计】
 * *   继承RelativeLayout作为容器布局，采用Fragment动态管理架构：
 * *   - 使用FragmentManager管理5个MainLayoutCenterSerialsWordDetail Fragment实例
 * *   - 通过RadioGroup实现通道标签页的互斥选择
 * *   - 利用RxBus订阅多个事件源，实现松耦合的消息驱动
 * *   - 使用Handler实现定时刷新机制
 * *
 * * 【数据流向】
 * *   输入：
 * *     → CacheUtil读取通道开启状态、串口类型、当前选中标签
 * *     → RxBus接收LoadCache、MainRightMsgOthers、RightMsgSerials等消息
 * *     → 用户点击RadioButton触发通道切换
 * *   输出：
 * *     → FragmentManager切换显示对应的Detail Fragment
 * *     → CacheUtil保存当前选中标签状态
 * *     → RxBus发送更新旋钮坐标消息
 * *
 * * 【依赖关系】
 * *   上层依赖：MainActivity提供Fragment管理器
 * *   下层依赖：MainLayoutCenterSerialsWordDetail各通道详情Fragment
 * *   平级依赖：CacheUtil、RxBus、PlaySound、ScreenUtil等工具类
 * *
 * * 【使用场景】
 * *   当用户查看串口总线解码后的文本数据时使用，支持多通道并行显示和单独查看
 * *   在Run模式下定时刷新显示，在Stop模式下支持用户手动滚动查看历史数据
 * ***********************************************************************************
 */
/**
 * ***********************************************************************************
 * * 串口总线文本显示主布局容器类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - 串口总线文本信息显示模块的顶层容器和通道切换控制器
 * *
 * * 【核心职责】
 * *   1. 管理S1/S2/S3/S4/S12五个串口通道的文本显示Fragment
 * *   2. 实现通道标签页的动态显示和切换逻辑
 * *   3. 监听RxBus事件并响应缓存加载、运行状态、配置变更等消息
 * *   4. 根据通道开启状态和串口类型动态更新标签标题
 * *   5. 处理外部按键事件触发文本列表滚动
 * *
 * * 【架构设计】
 * *   继承RelativeLayout作为容器布局，采用Fragment动态管理架构：
 * *   - 使用FragmentManager管理5个MainLayoutCenterSerialsWordDetail Fragment实例
 * *   - 通过RadioGroup实现通道标签页的互斥选择
 * *   - 利用RxBus订阅多个事件源，实现松耦合的消息驱动
 * *   - 使用Handler实现定时刷新机制
 * *
 * * 【数据流向】
 * *   输入：
 * *     → CacheUtil读取通道开启状态、串口类型、当前选中标签
 * *     → RxBus接收LoadCache、MainRightMsgOthers、RightMsgSerials等消息
 * *     → 用户点击RadioButton触发通道切换
 * *   输出：
 * *     → FragmentManager切换显示对应的Detail Fragment
 * *     → CacheUtil保存当前选中标签状态
 * *     → RxBus发送更新旋钮坐标消息
 * *
 * * 【依赖关系】
 * *   上层依赖：MainActivity提供Fragment管理器
 * *   下层依赖：MainLayoutCenterSerialsWordDetail各通道详情Fragment
 * *   平级依赖：CacheUtil、RxBus、PlaySound、ScreenUtil等工具类
 * *
 * * 【使用场景】
 * *   当用户查看串口总线解码后的文本数据时使用，支持多通道并行显示和单独查看
 * *   在Run模式下定时刷新显示，在Stop模式下支持用户手动滚动查看历史数据
 * ***********************************************************************************
 */
public class MainLayoutCenterSerialsWord extends RelativeLayout {
    private Context context;  // 应用上下文对象，用于资源访问和Fragment管理  // 应用上下文对象，用于资源访问和Fragment管理
    private MainLayoutCenterSerialsWordDetail s1Layout, s2Layout, s3Layout, s4Layout, s12Layout;  // 五个通道的详情Fragment实例，s12Layout代表所有总线组合视图
    private MainLayoutCenterSerialsWordDetail visibleLayout;  // 当前可见的详情Fragment引用
    private RadioButton serialsTitleS1, serialsTitleS2, serialsTitleS3, serialsTitleS4, serialsTitleS12;  // 五个通道标签单选按钮控件
    private RadioGroup radioGroup;  // 标签按钮组，管理单选按钮的互斥选择

    private String[] tags = {"serialsWordS1", "serialsWordS2", "serialsWordS3", "serialsWordS4", "serialsWordS12"};  // Fragment标签数组，用于Fragment恢复查找
    private Fragment[] fragments = new Fragment[5];  // Fragment数组，用于状态恢复时存储已存在的Fragment实例

    /**
     * 单参数构造方法
     * @param context 应用上下文对象
     */
    public MainLayoutCenterSerialsWord(Context context) {
        this(context, null, 0);  // 调用完整构造方法，传入默认属性和样式
    }

    /**
     * 双参数构造方法，用于XML布局解析
     * @param context 应用上下文对象
     * @param attrs XML属性集
     */
    public MainLayoutCenterSerialsWord(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用完整构造方法，传入默认样式
    }

    /**
     * 完整构造方法，初始化视图和控制器
     * @param context 应用上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainLayoutCenterSerialsWord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类RelativeLayout构造方法
        this.context = context;  // 保存上下文引用
        initView(attrs, defStyleAttr);  // 初始化视图控件
//        initLayout();  // 注释掉的布局初始化代码
        initControl();  // 初始化事件控制器
    }

    /**
     * 初始化控制器，订阅RxBus事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);  // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);  // 订阅右侧其他消息事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);  // 订阅右侧串口消息事件
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_RUNSTOP).subscribe(consumerMainLeftMenu);  // 订阅左侧菜单运行停止事件
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_SERIALSWORD).subscribe(consumerExternalKeysSerialsWord);  // 订阅外部按键串口字事件
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);  // 订阅串口字可见性事件

        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);  // 订阅顶部显示事件，用于更新组合标题
    }

    /**
     * 初始化视图控件，从布局文件加载并绑定控件
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maincenter_serialsword, this);  // 从布局文件加载视图到当前容器

        radioGroup = (RadioGroup) findViewById(R.id.serialsWordTitleGroup);  // 获取标签按钮组控件
        serialsTitleS1 = (RadioButton) findViewById(R.id.serialsWordTitleS1);  // 获取S1通道标签按钮
        serialsTitleS2 = (RadioButton) findViewById(R.id.serialsWordTitleS2);  // 获取S2通道标签按钮
        serialsTitleS3 = (RadioButton) findViewById(R.id.serialsWordTitleS3);  // 获取S3通道标签按钮
        serialsTitleS4 = (RadioButton) findViewById(R.id.serialsWordTitleS4);  // 获取S4通道标签按钮
        serialsTitleS12 = (RadioButton) findViewById(R.id.serialsWordTitleS12);  // 获取S12组合通道标签按钮
        serialsTitleS1.setOnClickListener(onClickListener);  // 为S1标签设置点击监听器
        serialsTitleS2.setOnClickListener(onClickListener);  // 为S2标签设置点击监听器
        serialsTitleS3.setOnClickListener(onClickListener);  // 为S3标签设置点击监听器
        serialsTitleS4.setOnClickListener(onClickListener);  // 为S4标签设置点击监听器
        serialsTitleS12.setOnClickListener(onClickListener);  // 为S12标签设置点击监听器
    }

    /**
     * 设置保存的实例状态，恢复Fragment布局
     * @param savedInstanceState 保存的状态Bundle对象
     */
    public void setSavedInstanceState(Bundle savedInstanceState) {
        initLayout(savedInstanceState);  // 调用布局初始化方法，传入状态Bundle
    }

    /**
     * 初始化布局，创建或恢复五个通道的详情Fragment
     * @param savedInstanceState 保存的状态Bundle对象，用于Fragment恢复
     */
    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {  // 如果有保存的状态
            for (int i = 0; i < tags.length; i++) {  // 循环查找已存在的Fragment
                fragments[i] = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag(tags[i]);  // 通过标签查找Fragment
            }
        }
        s1Layout = fragments[0] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[0];  // 创建或恢复S1 Fragment
        s1Layout.setChType(ISerialsWord.TYPE_S1);  // 设置S1通道类型
        s2Layout = fragments[1] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[1];  // 创建或恢复S2 Fragment
        s2Layout.setChType(ISerialsWord.TYPE_S2);  // 设置S2通道类型
        s3Layout = fragments[2] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[2];  // 创建或恢复S3 Fragment
        s3Layout.setChType(ISerialsWord.TYPE_S3);  // 设置S3通道类型
        s4Layout = fragments[3] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[3];  // 创建或恢复S4 Fragment
        s4Layout.setChType(ISerialsWord.TYPE_S4);  // 设置S4通道类型
        s12Layout = fragments[4] == null ? new MainLayoutCenterSerialsWordDetail() : (MainLayoutCenterSerialsWordDetail) fragments[4];  // 创建或恢复S12 Fragment
        s12Layout.setChType(ISerialsWord.TYPE_S12);  // 设置S12组合通道类型
        if (savedInstanceState == null) {  // 如果是新创建状态
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()  // 开启Fragment事务
                    .add(R.id.serialsWordDetailLayout, s1Layout, tags[0])  // 添加S1 Fragment
                    .add(R.id.serialsWordDetailLayout, s2Layout, tags[1])  // 添加S2 Fragment
                    .add(R.id.serialsWordDetailLayout, s3Layout, tags[2])  // 添加S3 Fragment
                    .add(R.id.serialsWordDetailLayout, s4Layout, tags[3])  // 添加S4 Fragment
                    .add(R.id.serialsWordDetailLayout, s12Layout, tags[4])  // 添加S12 Fragment
                    .hide(s2Layout)  // 隐藏S2 Fragment
                    .hide(s3Layout)  // 隐藏S3 Fragment
                    .hide(s4Layout)  // 隐藏S4 Fragment
                    .hide(s12Layout)  // 隐藏S12 Fragment
                    .commitAllowingStateLoss();  // 提交事务，允许状态丢失
        }
        visibleLayout = s1Layout;  // 默认显示S1 Fragment
    }

    /**
     * 从缓存设置通道配置，更新标签标题并切换到当前选中标签
     */
    private void setCache() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);  // 获取S1通道开启状态
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);  // 获取S2通道开启状态
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);  // 获取S3通道开启状态
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);  // 获取S4通道开启状态
        int s1Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);  // 获取S1串口类型索引
        int s2Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);  // 获取S2串口类型索引
        int s3Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);  // 获取S3串口类型索引
        int s4Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);  // 获取S4串口类型索引
        String s1title = getResources().getString(R.string.serialsWordTitleS1);  // 获取S1默认标题
        String s2title = getResources().getString(R.string.serialsWordTitleS2);  // 获取S2默认标题
        String s3title = getResources().getString(R.string.serialsWordTitleS3);  // 获取S3默认标题
        String s4title = getResources().getString(R.string.serialsWordTitleS4);  // 获取S4默认标题
        if (s1Check) {  // 如果S1通道开启
            switch (s1Index) {  // 根据串口类型设置标题
                case RightLayoutSerials.SERIALS_UART:
                    s1title = "S1:UART";  // 设置为UART类型标题
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s1title = "S1:LIN";  // 设置为LIN类型标题
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s1title = "S1:CAN";  // 设置为CAN类型标题
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s1title = "S1:SPI";  // 设置为SPI类型标题
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s1title = "S1:I2C";  // 设置为I2C类型标题
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s1title = "S1:429";  // 设置为429类型标题
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s1title = "S1:1553B";  // 设置为1553B类型标题
                    break;
            }
        }
        if (s2Check) {  // 如果S2通道开启
            switch (s2Index) {  // 根据串口类型设置标题
                case RightLayoutSerials.SERIALS_UART:
                    s2title = "S2:UART";  // 设置为UART类型标题
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s2title = "S2:LIN";  // 设置为LIN类型标题
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s2title = "S2:CAN";  // 设置为CAN类型标题
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s2title = "S2:SPI";  // 设置为SPI类型标题
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s2title = "S2:I2C";  // 设置为I2C类型标题
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s2title = "S2:429";  // 设置为429类型标题
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s2title = "S2:1553B";  // 设置为1553B类型标题
                    break;
            }
        }
        if (s3Check) {  // 如果S3通道开启
            switch (s3Index) {  // 根据串口类型设置标题
                case RightLayoutSerials.SERIALS_UART:
                    s3title = "S3:UART";  // 设置为UART类型标题
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s3title = "S3:LIN";  // 设置为LIN类型标题
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s3title = "S3:CAN";  // 设置为CAN类型标题
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s3title = "S3:SPI";  // 设置为SPI类型标题
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s3title = "S3:I2C";  // 设置为I2C类型标题
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s3title = "S3:429";  // 设置为429类型标题
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s3title = "S3:1553B";  // 设置为1553B类型标题
                    break;
            }
        }
        if (s4Check) {  // 如果S4通道开启
            switch (s4Index) {  // 根据串口类型设置标题
                case RightLayoutSerials.SERIALS_UART:
                    s4title = "S4:UART";  // 设置为UART类型标题
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    s4title = "S4:LIN";  // 设置为LIN类型标题
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    s4title = "S4:CAN";  // 设置为CAN类型标题
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    s4title = "S4:SPI";  // 设置为SPI类型标题
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    s4title = "S4:I2C";  // 设置为I2C类型标题
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    s4title = "S4:429";  // 设置为429类型标题
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    s4title = "S4:1553B";  // 设置为1553B类型标题
                    break;
            }
        }
        serialsTitleS1.setText(s1title);  // 设置S1标签文本
        s1Layout.setTitle(s1title);  // 设置S1详情页标题
        serialsTitleS2.setText(s2title);  // 设置S2标签文本
        s2Layout.setTitle(s2title);  // 设置S2详情页标题
        serialsTitleS3.setText(s3title);  // 设置S3标签文本
        s3Layout.setTitle(s3title);  // 设置S3详情页标题
        serialsTitleS4.setText(s4title);  // 设置S4标签文本
        s4Layout.setTitle(s4title);  // 设置S4详情页标题
        setSerialsS12Title();  // 设置S12组合标题
        int tab = CacheUtil.get().getInt(CacheUtil.SERIAL_TXT_CURRTAB);  // 获取当前选中标签索引
        if (tab == ISerialsWord.TYPE_S1) {  // 如果选中S1标签
            radioGroup.check(serialsTitleS1.getId());  // 设置S1单选按钮选中
            onClickTitle(serialsTitleS1.getId());  // 触发S1标签点击
        } else if (tab == ISerialsWord.TYPE_S2) {  // 如果选中S2标签
            radioGroup.check(serialsTitleS2.getId());  // 设置S2单选按钮选中
            onClickTitle(serialsTitleS2.getId());  // 触发S2标签点击
        } else if (tab == ISerialsWord.TYPE_S3) {  // 如果选中S3标签
            radioGroup.check(serialsTitleS3.getId());  // 设置S3单选按钮选中
            onClickTitle(serialsTitleS3.getId());  // 触发S3标签点击
        } else if (tab == ISerialsWord.TYPE_S4) {  // 如果选中S4标签
            radioGroup.check(serialsTitleS4.getId());  // 设置S4单选按钮选中
            onClickTitle(serialsTitleS4.getId());  // 触发S4标签点击
        } else if (tab == ISerialsWord.TYPE_S12) {  // 如果选中S12标签
            radioGroup.check(serialsTitleS12.getId());  // 设置S12单选按钮选中
            onClickTitle(serialsTitleS12.getId());  // 触发S12标签点击
        } else {  // 其他情况默认选中S1
            radioGroup.check(serialsTitleS1.getId());  // 设置S1单选按钮选中
            onClickTitle(serialsTitleS1.getId());  // 触发S1标签点击
        }
        setTabSelect();  // 设置标签选择状态
    }

    /**
     * 获取当前显示的标签索引
     * @return 标签索引（0=S1, 1=S2, 2=S3, 3=S4, 4=S12），默认返回-1
     */
    public int getShowTitle(){
        int r=-1;  // 初始化返回值为-1
        if (serialsTitleS1.isChecked()){  // 如果S1标签选中
            r=0;  // 返回0
        }
        if (serialsTitleS2.isChecked()){  // 如果S2标签选中
            r=1;  // 返回1
        }
        if (serialsTitleS3.isChecked()){  // 如果S3标签选中
            r=2;  // 返回2
        }
        if (serialsTitleS4.isChecked()){  // 如果S4标签选中
            r=3;  // 返回3
        }
        if (serialsTitleS12.isChecked()){  // 如果S12标签选中
            r=4;  // 返回4
        }
        return r;  // 返回标签索引

    }

    /**
     * 外部按键串口字消息消费者，处理滚动事件
     */
    private Consumer<Integer> consumerExternalKeysSerialsWord = new Consumer<Integer>() {
        @Override
        public void accept(Integer msgSerialsWord) throws Exception {
            if (visibleLayout == s1Layout) {  // 如果当前显示S1
                s1Layout.setScrollMove(msgSerialsWord);  // 设置S1滚动移动
            } else if (visibleLayout == s2Layout) {  // 如果当前显示S2
                s2Layout.setScrollMove(msgSerialsWord);  // 设置S2滚动移动
            } else if (visibleLayout == s3Layout) {  // 如果当前显示S3
                s3Layout.setScrollMove(msgSerialsWord);  // 设置S3滚动移动
            } else if (visibleLayout == s4Layout) {  // 如果当前显示S4
                s4Layout.setScrollMove(msgSerialsWord);  // 设置S4滚动移动
            } else if (visibleLayout == s12Layout) {  // 如果当前显示S12
                s12Layout.setScrollMove(msgSerialsWord);  // 设置S12滚动移动
            }
        }
    };

    private static final int MSG_RUN = 1;  // Handler消息类型常量：运行状态消息
    /**
     * Handler处理器，处理定时刷新任务
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);  // 调用父类消息处理
            switch (msg.what) {  // 根据消息类型处理
                case MSG_RUN:  // 运行状态消息
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 如果串口总线文本显示开关开启
                        s1Layout.setRunStop(true);  // 设置S1为运行状态
                        s2Layout.setRunStop(true);  // 设置S2为运行状态
                        s3Layout.setRunStop(true);  // 设置S3为运行状态
                        s4Layout.setRunStop(true);  // 设置S4为运行状态
                        s12Layout.setRunStop(true);  // 设置S12为运行状态
                    }
                    handler.sendEmptyMessageDelayed(MSG_RUN, 250);  // 发送延时消息，250ms后再次刷新
                    break;
            }
        }
    };

    /**
     * 左侧菜单运行停止消息消费者
     */
    private Consumer<MainLeftMsgMenuRunStop> consumerMainLeftMenu = new Consumer<MainLeftMsgMenuRunStop>() {
        @Override
        public void accept(MainLeftMsgMenuRunStop mainLeftMsgMenuRunStop) throws Exception {
            boolean run = (mainLeftMsgMenuRunStop.getRunState() == MainLeftMsgMenuRunStop.RUN);  // 判断是否为运行状态
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 如果串口总线文本显示开关开启
                s1Layout.setRunStop(run);  // 设置S1运行停止状态
                s2Layout.setRunStop(run);  // 设置S2运行停止状态
                s3Layout.setRunStop(run);  // 设置S3运行停止状态
                s4Layout.setRunStop(run);  // 设置S4运行停止状态
                s12Layout.setRunStop(run);  // 设置S12运行停止状态
            }
            if (handler.hasMessages(MSG_RUN)) {  // 如果Handler有运行消息
                handler.removeMessages(MSG_RUN);  // 移除运行消息
            }
            if (run) {  // 如果是运行状态
                handler.sendEmptyMessage(MSG_RUN);  // 发送运行消息启动定时刷新
            }
        }
    };

    /**
     * 串口字可见性消息消费者
     */
    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);  // 获取运行停止状态
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 如果串口总线文本显示开关开启
                s1Layout.setRunStop(run);  // 设置S1运行停止状态
                s2Layout.setRunStop(run);  // 设置S2运行停止状态
                s3Layout.setRunStop(run);  // 设置S3运行停止状态
                s4Layout.setRunStop(run);  // 设置S4运行停止状态
                s12Layout.setRunStop(run);  // 设置S12运行停止状态
            }
            if (handler.hasMessages(MSG_RUN)) {  // 如果Handler有运行消息
                handler.removeMessages(MSG_RUN);  // 移除运行消息
            }
            if (run) {  // 如果是运行状态
                handler.sendEmptyMessage(MSG_RUN);  // 发送运行消息启动定时刷新
            }
        }
    };

    /**
     * 加载缓存消息消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();  // 调用缓存设置方法
        }
    };

    /**
     * 右侧其他消息消费者，处理通道开启关闭事件
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            setCache();  // 调用缓存设置方法

            //获取通道是否打开
            boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);  // 获取S1通道开启状态
            boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);  // 获取S2通道开启状态
            boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);  // 获取S3通道开启状态
