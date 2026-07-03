package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;  // 导入上下文类
import android.content.res.TypedArray;  // 导入类型数组类，用于读取自定义属性
import android.graphics.Bitmap;  // 导入位图类
import android.graphics.Paint;  // 导入画笔类，用于文本样式设置
import android.graphics.Rect;  // 导入矩形类，用于视图边界计算
import android.os.Handler;  // 导入Handler类，用于延时处理
import android.text.Html;  // 导入Html类，用于解析HTML文本
import android.util.AttributeSet;  // 导入属性集类，用于XML属性读取
import android.view.MotionEvent;  // 导入触摸事件类
import android.view.View;  // 导入视图基类
import android.widget.Button;  // 导入按钮控件
import android.widget.LinearLayout;  // 导入线性布局
import android.widget.RelativeLayout;  // 导入相对布局
import android.widget.TextView;  // 导入文本视图控件

import com.micsig.base.Logger;  // 导入日志工具类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入通道工厂类，用于通道索引常量
import com.micsig.tbook.tbookscope.MainMsgSlip;  // 导入主界面滑动消息封装类
import com.micsig.tbook.tbookscope.MainViewGroup;  // 导入主视图组类，用于右侧滑动标识
import com.micsig.tbook.tbookscope.R;  // 导入资源类
import com.micsig.tbook.tbookscope.menu.SliderZone;  // 导入滑动区域接口
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;  // 导入串口布局常量类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;  // 导入串口消息常量类
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools;  // 导入工具类
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 导入缓存工具类
import com.micsig.tbook.ui.util.StrUtil;  // 导入字符串工具类
import com.micsig.tbook.ui.wavezone.TChan;  // 导入通道工具类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     右侧串口总线主项布局控件                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   主界面右侧通道列表中的串口总线主项UI控件，用于显示和管理串口通道状态              │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 显示串口通道的基本信息（通道名称、总线类型、电平阈值等）                      │
 * │   2. 提供通道选中/未选中状态切换功能                                          │
 * │   3. 处理单击、双击、滑动关闭等触摸交互事件                                    │
 * │   4. 支持多种串口协议类型（UART/LIN/CAN/SPI/I2C/M1553B/M429）的显示配置         │
 * │   5. 实现滑动区域接口，支持右侧滑动菜单功能                                    │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   继承关系：LinearLayout ← MainRightLayoutItemSerialsMaster                 │
 * │                ↓ implements                                                 │
 * │           SliderZone.ISliderZone（滑动区域接口）                              │
 * │   设计模式：自定义View组合模式，通过XML布局文件加载子视图                        │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   输入：用户触摸事件 → 手势识别 → 事件回调/总线消息发送                          │
 * │   输出：UI状态更新（文本、颜色、背景） → RxBus消息广播                           │
 * │   配置：外部设置串口类型、通道号、电平阈值等参数                                │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   内部依赖：ChannelFactory（通道工厂）、Tools（工具类）、RxBus（事件总线）       │
 * │   外部资源：layout_mainright_serials_master_for_eight.xml（布局文件）          │
 * │   接口依赖：SliderZone.ISliderZone、OnAllClickListener、SlidCloseChannelListener│
 * │                                                                             │
 * │ 【使用场景】                                                                 │
 * │   1. 主界面右侧通道列表中，显示S1-S4四个串口通道的状态信息                       │
 * │   2. 单击通道项触发选中/切换操作                                              │
 * │   3. 双击通道项打开对应的串口配置滑动菜单                                      │
 * │   4. 向下滑动通道项关闭对应串口通道                                           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainRightLayoutItemSerialsMaster extends LinearLayout implements SliderZone.ISliderZone {
    private Context context;  // 上下文对象引用
    private boolean checked = true;  // 通道启用状态标志，true表示启用，false表示禁用
    private String text = "S1";  // 通道名称文本，默认为S1
    private int chIndex = ChannelFactory.S1;  // 通道索引，对应ChannelFactory中的S1-S4常量
    private RelativeLayout bgChannelMaster;  // 通道主背景容器，用于设置选中/未选中背景
    private TextView tvChText;  // 通道名称文本视图（S1-S4）
    private TextView tvBusType;  // 总线类型文本视图，显示协议类型（如UART、SPI等）
    private LinearLayout rlTextLayout;  // 文本布局容器，包含电平阈值等信息
    private LinearLayout llTextLayout1, llTextLayout2;  // 第一行和第二行文本布局容器
    private View spaceLine;  // 分隔线视图
    private TextView tv1;  // 第一行文本视图，显示电平阈值或波特率
    private TextView tv2;  // 第二行文本视图，显示电平阈值或波特率
    private TextView tv3;  // 第三行文本视图，显示电平阈值或波特率
    private TextView tv4;  // 第四行文本视图，显示电平阈值或波特率
    private Button btnClick;  // 点击响应按钮，用于拦截触摸事件
    private OnAllClickListener onAllClickListener;  // 单击事件监听器
    private SlidCloseChannelListener mSlideCloseListener;  // 滑动关闭事件监听器

    private int serialsType = RightLayoutSerials.SERIALS_UART;  // 串口协议类型，默认为UART
    private int text1Ch = 1;//1-4  // 第一行文本对应的通道号（1-4）
    private int text2Ch = 1;  // 第二行文本对应的通道号
    private int text3Ch = 1;  // 第三行文本对应的通道号
    private boolean isSelect = false;  // 是否处于选中状态标志

    /**
     * 单击事件监听器接口
     * 用于响应通道项的单击操作
     */
    public interface OnAllClickListener {
        /**
         * 单击事件回调方法
         * @param v 被点击的通道项视图
         */
        void onClick(MainRightLayoutItemSerialsMaster v);
    }

    /**
     * 滑动关闭通道事件监听器接口
     * 用于响应向下滑动关闭通道的操作
     */
    public interface SlidCloseChannelListener {
        /**
         * 滑动关闭事件回调方法
         * @param v 被滑动关闭的通道项视图
         */
        void onSlidCloseChannel(MainRightLayoutItemSerialsMaster v);
    }

    /**
     * 设置单击事件监听器
     * @param onAllClickListener 单击事件监听器实例
     */
    public void setOnAllClickListener(OnAllClickListener onAllClickListener) {
        this.onAllClickListener = onAllClickListener;  // 保存单击监听器引用
    }

    /**
     * 设置滑动关闭事件监听器
     * @param mSlideCloseListener 滑动关闭事件监听器实例
     */
    public void setOnSlidCloseChannelListener(SlidCloseChannelListener mSlideCloseListener) {
        this.mSlideCloseListener = mSlideCloseListener;  // 保存滑动关闭监听器引用
    }

    /**
     * 单参数构造函数
     * 用于代码中动态创建控件
     * @param context 上下文对象
     */
    public MainRightLayoutItemSerialsMaster(Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * 用于XML布局文件中创建控件
     * @param context 上下文对象
     * @param attrs XML属性集
     */
    public MainRightLayoutItemSerialsMaster(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 三参数构造函数
     * 完整的构造函数，包含默认样式属性
     * @param context 上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainRightLayoutItemSerialsMaster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        init(context, attrs, defStyleAttr);  // 执行初始化方法
    }

    /**
     * 初始化方法
     * 读取自定义属性，加载布局文件，初始化视图
     * @param context 上下文对象
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;  // 保存上下文引用
        inflate(context, R.layout.layout_mainright_serials_master_for_eight, this);  // 加载布局文件
        text = "S1";  // 设置默认通道名称
        chIndex = ChannelFactory.S1;  // 设置默认通道索引
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemSerialsMaster);  // 获取自定义属性数组
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemSerialsMaster_checked, true);  // 读取checked属性，默认true
        text = ta.getString(R.styleable.MainRightLayoutItemSerialsMaster_text);  // 读取text属性
        ta.recycle();  // 回收TypedArray资源
        initView();  // 初始化视图控件
        setChecked(checked);  // 设置初始选中状态
    }

    /**
     * 初始化视图控件
     * 获取布局文件中的控件引用并设置初始状态
     */
    private void initView() {
        bgChannelMaster = findViewById(R.id.bgChannelMaster);  // 获取背景容器
        tvChText = (TextView) findViewById(R.id.channelText);  // 获取通道名称文本视图
        tvBusType = (TextView) findViewById(R.id.serialsType);  // 获取总线类型文本视图
        rlTextLayout = (LinearLayout) findViewById(R.id.textLayout);  // 获取文本布局容器
        spaceLine = findViewById(R.id.space_line);  // 获取分隔线视图
        llTextLayout1 = findViewById(R.id.textLayout1);  // 获取第一行文本布局
        llTextLayout2 = findViewById(R.id.textLayout2);  // 获取第二行文本布局
        tv1 = (TextView) findViewById(R.id.text1);  // 获取第一个文本视图
        tv2 = (TextView) findViewById(R.id.text2);  // 获取第二个文本视图
        tv3 = (TextView) findViewById(R.id.text3);  // 获取第三个文本视图
        tv4 = (TextView) findViewById(R.id.text4);  // 获取第四个文本视图
        btnClick = (Button) findViewById(R.id.btnClick);  // 获取点击按钮

       // btnClick.setOnClickListener(onClickListener);  // 注释掉的点击监听器设置
        btnClick.setOnTouchListener(onTouchListener);  // 设置触摸监听器，用于处理复杂手势

        tvChText.setText(text);  // 设置通道名称文本
        if (text.contains("S1")) {  // 判断是否为S1通道
            tvChText.setTextColor(getResources().getColor(R.color.color_S1));  // 设置S1通道颜色
            chIndex = ChannelFactory.S1;  // 设置通道索引
            tvBusType.setTextColor(getResources().getColor(R.color.color_S1));  // 设置总线类型文本颜色
        } else if (text.equals("S2")) {  // 判断是否为S2通道
            tvChText.setTextColor(getResources().getColor(R.color.color_S2));  // 设置S2通道颜色
            chIndex = ChannelFactory.S2;  // 设置通道索引
            tvBusType.setTextColor(getResources().getColor(R.color.color_S2));  // 设置总线类型文本颜色
        } else if (text.equals("S3")) {  // 判断是否为S3通道
            tvChText.setTextColor(getResources().getColor(R.color.color_S3));  // 设置S3通道颜色
            chIndex = ChannelFactory.S3;  // 设置通道索引
            tvBusType.setTextColor(getResources().getColor(R.color.color_S3));  // 设置总线类型文本颜色
        } else if (text.equals("S4")) {  // 判断是否为S4通道
            tvChText.setTextColor(getResources().getColor(R.color.color_S4));  // 设置S4通道颜色
            chIndex = ChannelFactory.S4;  // 设置通道索引
            tvBusType.setTextColor(getResources().getColor(R.color.color_S4));  // 设置总线类型文本颜色
        }
    }

    /**
     * 设置通道启用状态
     * 根据启用状态切换显示的UI元素
     * @param checked true表示启用，false表示禁用
     */
    public void setChecked(boolean checked) {
        this.checked = checked;  // 保存状态
        if (checked) {  // 启用状态
            tvChText.setVisibility(GONE);  // 隐藏通道名称文本
            tvBusType.setVisibility(VISIBLE);  // 显示总线类型文本
            rlTextLayout.setVisibility(VISIBLE);  // 显示文本布局容器
        } else {  // 禁用状态
            tvChText.setVisibility(VISIBLE);  // 显示通道名称文本
            tvBusType.setVisibility(GONE);  // 隐藏总线类型文本
            rlTextLayout.setVisibility(GONE);  // 隐藏文本布局容器
        }
    }

    /**
     * 更新背景样式
     * 根据选中状态设置不同的背景drawable
     * @param isSelect true表示选中状态，false表示未选中状态
     */
    public void updateBackground(boolean isSelect) {
        this.isSelect = isSelect;  // 保存选中状态
        if (isSelect) {  // 选中状态
            bgChannelMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_select));  // 设置选中背景
        } else {  // 未选中状态
            bgChannelMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_disable));  // 设置禁用背景
        }
    }

    /**
     * 获取选中状态
     * @return true表示选中，false表示未选中
     */
    public boolean isSelect() {
        return isSelect;  // 返回选中状态
    }

    /**
     * 获取通道启用状态
     * @return true表示启用，false表示禁用
     */
    public boolean isChecked() {
        return checked;  // 返回启用状态
    }

    /**
     * 获取通道名称
     * @return 通道名称字符串（S1-S4）
     */
    public String getName() {
        return text;  // 返回通道名称
    }

    /**
     * 设置通道名称
     * @param text 通道名称字符串（S1-S4）
     */
    public void setName(String text) {
        this.text = text;  // 保存通道名称
        tvChText.setText(text);  // 更新显示的通道名称
    }

    /**
     * 设置串口总线类型文本（带序号）
     * 根据通道索引添加对应的圆圈序号
     * @param serialsType 总线类型文本
     */
    public void setSerialsType(String serialsType) {
        switch (chIndex) {  // 根据通道索引判断
            case ChannelFactory.S1:  // S1通道
                serialsType = "① " + serialsType;  // 添加圆圈序号①
                break;
            case ChannelFactory.S2:  // S2通道
                serialsType = "② " + serialsType;  // 添加圆圈序号②
                break;
            case ChannelFactory.S3:  // S3通道
                serialsType = "③ " + serialsType;  // 添加圆圈序号③
                break;
            case ChannelFactory.S4:  // S4通道
                serialsType = "④ " + serialsType;  // 添加圆圈序号④
                break;
        }
        serialsType = serialsType.replaceAll("[\\r\\n]+", "");  // 移除换行符
        this.tvBusType.setText(serialsType);  // 设置总线类型文本
    }

    /**
     * 设置串口协议类型
     * @param serialsType 串口协议类型常量（UART/LIN/CAN/SPI/I2C/M1553B/M429）
     */
    public void setSerialsType(int serialsType) {
        this.serialsType = serialsType;  // 保存串口协议类型
    }

    /**
     * 获取串口协议类型
     * @return 串口协议类型常量
     */
    public int getSerialsType() {
        return this.serialsType;  // 返回串口协议类型
    }

    /**
     * 设置除了spi、i2c模式以外的模式下，两个通道的通道号
     * 用于UART、LIN、CAN、M1553B等单通道协议
     * @param ch 通道号（1-4）
     */
    public void setCommonCh(int ch) {
        this.text1Ch = ch;  // 保存通道号
    }

    /**
     * 获取普通模式的通道号
     * @return 通道号（1-4）
     */
    public int getCommonCh() {
        return this.text1Ch;  // 返回通道号
    }

    /**
     * 获取通道索引
     * @return 通道索引（ChannelFactory.S1-S4）
     */
    public int getChIndex() {
        return chIndex;  // 返回通道索引
    }

    /**
     * 设置i2c模式下，两个通道的通道号
     * I2C协议需要两个通道（SDA和SCL）
     * @param text1Ch 第一个通道号（SDA）
     * @param text2Ch 第二个通道号（SCL）
     */
    public void setI2cCh(int text1Ch, int text2Ch) {
        this.text1Ch = text1Ch;  // 保存第一个通道号
        this.text2Ch = text2Ch;  // 保存第二个通道号
    }

    /**
     * 获取I2C模式第一个通道号
     * @return 第一个通道号（SDA）
     */
    public int getI2cNo1() {
        return this.text1Ch;  // 返回第一个通道号
    }

    /**
     * 获取I2C模式第二个通道号
     * @return 第二个通道号（SCL）
     */
    public int getI2cNo2() {
        return this.text2Ch;  // 返回第二个通道号
    }

    /**
     * 设置spi模式下，两个通道的通道号
     * SPI协议需要三个通道（MOSI、MISO、CLK）
     * @param text1Ch 第一个通道号（MOSI）
     * @param text2Ch 第二个通道号（MISO）
     * @param text3Ch 第三个通道号（CLK）
     */
    public void setSpiCh(int text1Ch, int text2Ch, int text3Ch) {
        this.text1Ch = text1Ch;  // 保存第一个通道号
        this.text2Ch = text2Ch;  // 保存第二个通道号
        this.text3Ch = text3Ch;  // 保存第三个通道号
    }

    /**
     * 获取SPI模式第一个通道号
     * @return 第一个通道号（MOSI）
     */
    public int getSpiNo1() {
        return this.text1Ch;  // 返回第一个通道号
    }

    /**
     * 获取SPI模式第二个通道号
     * @return 第二个通道号（MISO）
     */
    public int getSpiNo2() {
        return this.text2Ch;  // 返回第二个通道号
    }

    /**
     * 获取SPI模式第三个通道号
     * @return 第三个通道号（CLK）
     */
    public int getSpiNo3() {
        return this.text3Ch;  // 返回第三个通道号
    }

    /**
     * 设置阈值电平值的显示，只有当当前显示的channel与当前改变的channel相同时，才会修改
     * 根据不同的串口协议类型，更新对应通道的电平阈值显示
     * @param curCh 本次修改的通道号
     * @param levelType 电平类型（高电平/低电平）
     * @param levelMode 电平模式
     */
    public void setCommonValueLevel(int curCh, int levelType, int levelMode) {
        String val = Tools.getChannelLevel(curCh, levelType, levelMode);  // 获取电平值字符串
        switch (serialsType) {  // 根据串口类型判断
            case RightLayoutSerials.SERIALS_UART:  // UART协议
            case RightLayoutSerials.SERIALS_LIN:  // LIN协议
            case RightLayoutSerials.SERIALS_CAN:  // CAN协议
            case RightLayoutSerials.SERIALS_M1553B:  // M1553B协议
                if (text1Ch == curCh) {  // 判断通道是否匹配
                    setSerialsTextLine1(val);  // 更新第一行文本显示
                }
                break;
            case RightLayoutSerials.SERIALS_SPI:  // SPI协议
                if (text1Ch == curCh) {  // 判断第一个通道是否匹配
                    setSerialsTextLine1(val);  // 更新第一行文本显示
                } else if (text2Ch == curCh) {  // 判断第二个通道是否匹配
                    setSerialsTextLine2(val);  // 更新第二行文本显示
                } else if (text3Ch == curCh && tv3.getVisibility() == VISIBLE) {  // 判断第三个通道是否匹配且可见
                    setSerialsTextLine3(val);  // 更新第三行文本显示
                }
                break;
            case RightLayoutSerials.SERIALS_I2C:  // I2C协议
                if (text1Ch == curCh) {  // 判断第一个通道是否匹配
                    setSerialsTextLine1(val);  // 更新第一行文本显示
                } else if (text2Ch == curCh) {  // 判断第二个通道是否匹配
                    setSerialsTextLine2(val);  // 更新第二行文本显示
                }
                break;
            case RightLayoutSerials.SERIALS_M429:  // M429协议
                if (text1Ch == curCh) {  // 判断通道是否匹配
                    if (levelType == Tools.LevelType_High) {  // 判断是否为高电平
                        setSerialsTextLine1(val);  // 高电平显示在第一行
                    } else {  // 低电平
                        setSerialsTextLine2(val);  // 低电平显示在第二行
                    }
                }
                break;
        }
    }

    /**
     * 设置第一行串口文本显示
     * @param text 要显示的文本内容
     */
    public void setSerialsTextLine1(String text) {
        if (!StrUtil.isEmpty(text)) {  // 判断文本是否非空
            text = text.replace("b/s", "");  // 移除波特率单位"b/s"
//            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            Rect textRect = Tools.getTextRect(text, tv1.getPaint());  // 注释掉的文本矩形计算代码
//            if (textRect.width() >= 45) {  // 注释掉的文本宽度判断代码
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            } else {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            }
            tv1.setVisibility(View.VISIBLE);  // 设置文本视图可见
            tv1.setText(text);  // 设置文本内容
        } else {  // 文本为空
            tv1.setVisibility(View.GONE);  // 隐藏文本视图
        }
    }

    /**
     * 设置第二行串口文本显示
     * @param text 要显示的文本内容
     */
    public void setSerialsTextLine2(String text) {
        if (!StrUtil.isEmpty(text)) {  // 判断文本是否非空
            text = text.replace("b/s", "");  // 移除波特率单位"b/s"
            tv2.setVisibility(View.VISIBLE);  // 设置文本视图可见
            tv2.setText(text);  // 设置文本内容
        } else {  // 文本为空
            tv2.setVisibility(View.GONE);  // 隐藏文本视图
        }
    }

    /**
     * 设置第三行串口文本显示
     * @param text 要显示的文本内容
     */
    public void setSerialsTextLine3(String text) {
        if (!StrUtil.isEmpty(text)) {  // 判断文本是否非空
            text = text.replace("b/s", "");  // 移除波特率单位"b/s"
            tv3.setVisibility(View.VISIBLE);  // 设置文本视图可见
            tv3.setText(text);  // 设置文本内容
        } else {  // 文本为空
            tv3.setVisibility(View.GONE);  // 隐藏文本视图
        }
    }

    /**
     * 获取第三行文本视图的可见性
     * @return 可见性状态（View.VISIBLE/GONE/INVISIBLE）
     */
    public int getSerialsTextLine3Visible() {
        return tv3.getVisibility();  // 返回可见性状态
    }

    /**
     * 获取第一行串口文本内容
     * @return 第一行文本字符串
     */
    public String getSerialsTextLine1() {
        return tv1.getText().toString();  // 返回第一行文本
    }

    /**
     * 获取第二行串口文本内容
     * @return 第二行文本字符串
     */
    public String getSerialsTextLine2() {
        return tv2.getText().toString();  // 返回第二行文本
    }

    /**
     * 获取第三行串口文本内容
     * @return 第三行文本字符串
     */
    public String getSerialsTextLine3() {
        return tv3.getText().toString();  // 返回第三行文本
    }

    /**
     * 设置串口文本显示（完整版本）
     * 支持四行文本、自定义颜色和下划线样式
     * @param text1 第一行文本
     * @param text2 第二行文本
     * @param text3 第三行文本
     * @param text4 第四行文本
     * @param color1 第一行文本颜色
     * @param color2 第二行文本颜色
     * @param color3 第三行文本颜色
     * @param color4 第四行文本颜色
     * @param line1 第一行是否显示下划线
     * @param line2 第二行是否显示下划线
     * @param line3 第三行是否显示下划线
     * @param line4 第四行是否显示下划线
     */
    public void setSerialsText(String text1, String text2, String text3, String text4
            , int color1, int color2, int color3, int color4
            , boolean line1, boolean line2, boolean line3, boolean line4) {
        // 处理第一行文本
        if (!StrUtil.isEmpty(text1)) {  // 判断文本是否非空
            text1 = text1.replace("b/s", "");  // 移除波特率单位"b/s"
//            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            Rect textRect = Tools.getTextRect(text1, tv1.getPaint());  // 注释掉的文本矩形计算代码
//            if (textRect.width() >= 45) {  // 注释掉的文本宽度判断代码
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            } else {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);  // 注释掉的文本大小设置代码
//            }
            if (line1) {  // 判断是否需要下划线
                tv1.setText(text1);  // 设置文本内容
                tv1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);  // 设置下划线和抗锯齿标志
            } else {  // 不需要下划线
                tv1.setText(text1);  // 设置文本内容
                tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);  // 只设置抗锯齿标志
            }
            tv1.setTextColor(color1);  // 设置文本颜色
            tv1.setVisibility(View.VISIBLE);  // 设置视图可见
        } else {  // 文本为空
            tv1.setVisibility(View.GONE);  // 隐藏视图
        }
        // 处理第二行文本
        if (!StrUtil.isEmpty(text2)) {  // 判断文本是否非空
            text2 = text2.replace("b/s", "");  // 移除波特率单位"b/s"
            if (line2) {  // 判断是否需要下划线
                tv2.setText(text2);  // 设置文本内容
                tv2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);  // 设置下划线和抗锯齿标志
            } else {  // 不需要下划线
                tv2.setText(text2);  // 设置文本内容
                tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);  // 只设置抗锯齿标志
            }
            tv2.setTextColor(color2);  // 设置文本颜色
            tv2.setVisibility(View.VISIBLE);  // 设置视图可见
        } else {  // 文本为空
            tv2.setVisibility(View.GONE);  // 隐藏视图
        }
        // 处理第三行文本
        if (!StrUtil.isEmpty(text3)) {  // 判断文本是否非空
            text3 = text3.replace("b/s", "");  // 移除波特率单位"b/s"
            if (line3) {  // 判断是否需要下划线
                tv3.setText(text3);  // 设置文本内容
                tv3.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);  // 设置下划线和抗锯齿标志
            } else {  // 不需要下划线
                tv3.setText(text3);  // 设置文本内容
                tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);  // 只设置抗锯齿标志
            }
            tv3.setTextColor(color3);  // 设置文本颜色
            tv3.setVisibility(View.VISIBLE);  // 设置视图可见
        } else {  // 文本为空
            tv3.setVisibility(View.GONE);  // 隐藏视图
        }
        // 处理第四行文本
        if (!StrUtil.isEmpty(text4)) {  // 判断文本是否非空
            text4 = text4.replace("b/s", "");  // 移除波特率单位"b/s"
            if (line4) {  // 判断是否需要下划线
                tv4.setText(Html.fromHtml("<u>" + text4 + "</u>"));  // 使用HTML标签设置下划线文本
            } else {  // 不需要下划线
                tv4.setText(text4);  // 设置文本内容
            }
            tv4.setTextColor(color4);  // 设置文本颜色
            tv4.setVisibility(View.VISIBLE);  // 设置视图可见
        } else {  // 文本为空
            tv4.setVisibility(View.GONE);  // 隐藏视图
        }

        // 根据第一行和第二行的可见性控制第一行布局容器
        if (tv1.getVisibility() != View.VISIBLE && tv2.getVisibility() != View.VISIBLE) {  // 第一行和第二行都不可见
            llTextLayout1.setVisibility(View.GONE);  // 隐藏第一行布局容器
        } else {  // 至少有一行可见
            llTextLayout1.setVisibility(View.VISIBLE);  // 显示第一行布局容器
        }

        // 根据第三行和第四行的可见性控制第二行布局容器
        if (tv3.getVisibility() != View.VISIBLE && tv4.getVisibility() != View.VISIBLE) {  // 第三行和第四行都不可见
            llTextLayout2.setVisibility(View.GONE);  // 隐藏第二行布局容器
            spaceLine.setVisibility(View.GONE);  // 隐藏分隔线
        } else {  // 至少有一行可见
            llTextLayout2.setVisibility(View.VISIBLE);  // 显示第二行布局容器
            spaceLine.setVisibility(View.VISIBLE);  // 显示分隔线
        }
    }

    /**
     * 触摸事件监听器
     * 用于处理单击、双击、滑动关闭等复杂手势操作
     */
    private OnTouchListener onTouchListener = new OnTouchListener() {
        private View curView;  // 当前触摸的视图
        private int oldX, oldY;  // 触摸起始坐标
        private Handler handler = new Handler();  // Handler用于延时处理双击事件
        private int clickCount = 0;  // 点击计数器，用于区分单击和双击

        /**
         * 触摸事件处理方法
         * @param view 触摸的视图
         * @param motionEvent 触摸事件对象
         * @return 是否消费事件
         */
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {  // 获取触摸事件类型
                case MotionEvent.ACTION_DOWN:  // 手指按下事件
                    this.oldX = (int) motionEvent.getX();  // 记录起始X坐标
                    this.oldY = (int) motionEvent.getY();  // 记录起始Y坐标
                    this.curView = view;  // 保存当前视图引用
//                    OnMouseDown(view);  // 注释掉的鼠标按下处理
                    break;  // 跳出switch
                case MotionEvent.ACTION_UP:  // 手指抬起事件
                    float offsetX = (float) (motionEvent.getX() - oldX);  // 计算X方向偏移量
                    float offsetY = (float) (motionEvent.getY() - oldY);  // 计算Y方向偏移量
                    if (offsetY > 10 && (offsetX <= 5 || offsetY / offsetX >= 1.0f)) {  // 判断是否为向下滑动手势
//                        CloseChannel(view);//关闭通道  // 注释掉的关闭通道方法调用
                        RxBus.getInstance().post(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL, TChan.toUiChNo(getChIndex()));//删除通道  // 发送删除通道消息
                        break;  // 跳出switch
                    }
                    clickCount++;  // 点击计数器加1
                    int timeout = 300;  // 双击判断超时时间（毫秒）
                    Rect rect = Tools.getViewRect(view);  // 获取视图矩形区域
                    if (curView != null && curView == view && (Math.abs(offsetY) < 10 && Math.abs(offsetX) < 10) /*rect.contains((int)motionEvent.getRawX(), (int) motionEvent.getRawY())*/) {  // 判断是否为点击事件（非滑动）
                        OnMouseUp(view);  // 触发鼠标抬起事件
                        handler.postDelayed(() -> {  // 延时处理点击事件
                            if (clickCount == 1) {  // 单击事件
                                // 单击事件处理（空实现）
                            } else if (clickCount > 1) {  // 双击或多击事件
                                onDoubleMouseUp(view);  // 触发双击事件
                            }
                            handler.removeCallbacksAndMessages(null);//清空handler延时  // 清空Handler消息队列
                            clickCount = 0;//计数清零  // 重置点击计数器
                        }, timeout);  // 延时timeout毫秒执行
                    }
                    break;  // 跳出switch
                case MotionEvent.ACTION_CANCEL:  // 触摸取消事件
                    break;  // 跳出switch
            }
            return false;  // 返回false，不消费事件
        }

        /**
         * 关闭通道方法（已弃用）
         * 现在通过RxBus消息机制关闭通道
         * @param view 触摸的视图
         */
        private void CloseChannel(View view) {
            int serialNo = RightMsgSerials.SERIALS_S1;  // 默认串口号为S1
            if (chIndex == ChannelFactory.S1) {  // 判断是否为S1通道
                serialNo = RightMsgSerials.SERIALS_S1;  // 设置串口号为S1
            } else if (chIndex == ChannelFactory.S2) {  // 判断是否为S2通道
                serialNo = RightMsgSerials.SERIALS_S2;  // 设置串口号为S2
            } else if (chIndex == ChannelFactory.S3) {  // 判断是否为S3通道
                serialNo = RightMsgSerials.SERIALS_S3;  // 设置串口号为S3
            } else {  // S4通道
                serialNo = RightMsgSerials.SERIALS_S4;  // 设置串口号为S4
            }
            if (mSlideCloseListener != null) {  // 判断监听器是否存在
                mSlideCloseListener.onSlidCloseChannel(MainRightLayoutItemSerialsMaster.this);  // 触发滑动关闭回调
            }
            RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS_CLOSE, serialNo);  // 发送关闭串口消息

        }

        /**
         * 鼠标抬起（单击）事件处理
         * @param view 触摸的视图
         */
        private void OnMouseUp(View view) {
            onClickListener.onClick(view);  // 触发单击监听器
        }

        /**
         * 双击事件处理
         * 打开对应的串口配置滑动菜单
         * @param view 触摸的视图
         */
      private void onDoubleMouseUp(View view) {
        //双击打开  // 双击打开滑动菜单
          openSlip(view);  // 调用打开滑动菜单方法
      }

    };

    /**
     * 打开滑动菜单
     * 根据通道索引打开对应的右侧滑动配置菜单
     * @param view 触摸的视图
     */
    private void openSlip(View view) {
//        if (ViewUtils.getInstance().isFastDoubleClick(btnClick.getId())) {  // 注释掉的快速双击判断代码
//        DToast.get().show("双击");  // 注释掉的Toast提示代码
        boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);  // 获取滑动菜单缩放标志
        boolean slowTimeBase = Tools.isSlowTimeBase();  // 判断是否为慢速时基模式
        if (zoom && slowTimeBase) {  // 如果同时满足缩放和慢速时基
            return;  // 直接返回，不打开菜单
        }
        //关闭垂直调节按钮  // 发送关闭垂直调节按钮消息
        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);  // 发送隐藏垂直刻度消息
        if (chIndex == ChannelFactory.S1) {  // 判断是否为S1通道
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S1, true));  // 发送打开S1滑动菜单消息
        } else if (chIndex == ChannelFactory.S2) {  // 判断是否为S2通道
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S2, true));  // 发送打开S2滑动菜单消息
        } else if (chIndex == ChannelFactory.S3) {  // 判断是否为S3通道
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S3, true));  // 发送打开S3滑动菜单消息
        } else if (chIndex == ChannelFactory.S4) {  // 判断是否为S4通道
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S4, true));  // 发送打开S4滑动菜单消息
        }
//        }  // 注释掉的代码块结束
    }

    /**
     * 单击事件监听器
     * 响应按钮点击事件
     */
    private OnClickListener onClickListener = new OnClickListener() {
        /**
         * 点击事件处理方法
         * @param v 被点击的视图
         */
        @Override
        public void onClick(View v) {
            if (v.getId() == btnClick.getId()) {  // 判断是否为按钮点击
                onAllClickListener.onClick(MainRightLayoutItemSerialsMaster.this);  // 触发自定义点击回调
            }
        }
    };

    //region    integerface  // 接口实现区域
    private Rect availableSliderRect = null;  // 可用滑动区域矩形

    /**
     * 获取可用滑动区域矩形
     * 实现SliderZone.ISliderZone接口方法
     * @return 可用滑动区域矩形对象
     */
    @Override
    public Rect getAvailableSliderRect() {
        availableSliderRect = Tools.getViewRect(this);  // 获取当前视图的矩形区域
        if (this.checked) {  // 判断通道是否启用
            Bitmap bmp = Tools.readSvgBmp(R.drawable.svg_right_serialbus); //((BitmapDrawable) getResources().getDrawable(R.drawable.svg_right_serialbus)).getBitmap();  // 读取串口总线图标位图
            int offsetY = (this.getHeight() - bmp.getHeight()) / 2;  // 计算Y方向偏移量
            availableSliderRect.set(availableSliderRect.left, availableSliderRect.top + offsetY, availableSliderRect.right, availableSliderRect.bottom - offsetY);  // 调整滑动区域矩形
        } else {  // 通道禁用
            Bitmap bmp = Tools.readSvgBmp(R.drawable.svg_right_ch1234_close); //((BitmapDrawable) getResources().getDrawable(R.drawable.svg_right_ch1234_close)).getBitmap();  // 读取关闭状态图标位图
            int offsetY = (this.getHeight() - bmp.getHeight()) / 2;  // 计算Y方向偏移量
            availableSliderRect.set(availableSliderRect.left, availableSliderRect.top + offsetY, availableSliderRect.right, availableSliderRect.bottom - offsetY);  // 调整滑动区域矩形
        }
        return availableSliderRect;  // 返回可用滑动区域矩形
    }
    //endregion  // 接口实现区域结束

}