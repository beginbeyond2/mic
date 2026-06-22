package com.micsig.tbook.ui.top.view; // 包声明：顶部视图组件包，包含顶部区域的自定义UI控件

// ==================== 导入区域 ====================
import android.content.Context; // Android上下文对象，用于访问系统资源和功能
import android.content.res.TypedArray; // 类型化数组，用于读取XML自定义属性
import android.util.AttributeSet; // 属性集，用于XML布局中的属性解析
import android.view.Gravity; // 重力常量，控制子视图的对齐方式
import android.view.View; // 视图基类，Android UI组件的基础
import android.widget.Button; // 按钮控件，用于显示百分比或作为点击区域
import android.widget.LinearLayout; // 线性布局容器，水平或垂直排列子视图
import android.widget.SeekBar; // 滑动条控件，用于选择数值范围
import android.widget.TextView; // 文本视图，用于显示标题和阈值标签

import com.micsig.tbook.ui.R; // UI模块资源文件，包含布局、样式和属性定义

import org.w3c.dom.Text; // DOM文本接口（注意：此导入未被使用，建议移除）

// ==================== 类级文档 ====================
/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          TopViewSeekBar                                       ║
 * ║                    带标题的滑动条视图组件                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   所属模块：UI组件层 -> 顶部视图模块                                            ║
 * ║   包路径  ：com.micsig.tbook.ui.top.view                                      ║
 * ║   布局文件：R.layout.view_seekbarwithhead                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供带标题标签的滑动条控件，支持标题、滑动条、百分比显示的水平布局           ║
 * ║   2. 支持自定义标题宽度(headWidth)和滑动条宽度(seekBarWidth)                    ║
 * ║   3. 支持触发灵敏度模式，显示高低阈值标签替代百分比按钮                           ║
 * ║   4. 提供进度变化回调接口，支持外部监听滑动条状态变化                             ║
 * ║   5. 封装SeekBar的初始化和事件处理逻辑，简化外部调用                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系：LinearLayout -> TopViewSeekBar                                    ║
 * ║   组合模式：组合TextView(标题) + SeekBar(滑动条) + Button/TextView(显示区)     ║
 * ║   视图结构：[标题TextView] [低阈值TextView] [SeekBar] [高阈值TextView] [Button] ║
 * ║   模式类型：复合视图控件(Compound View)，通过XML布局膨胀创建子视图              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   输入：setData() -> 设置标题、最大值、初始值、监听器                           ║
 * ║   处理：SeekBar滑动 -> mChangeListener -> 更新百分比显示 -> 回调外部监听器      ║
 * ║   输出：getProgress() -> 返回当前进度值                                        ║
 * ║   模式切换：setTriggerSeekBar() -> 切换显示模式(普通/触发灵敏度)               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   布局依赖：view_seekbarwithhead.xml                                          ║
 * ║   属性依赖：R.styleable.TopViewSeekBar (headWidth, seekBarWidth)              ║
 * ║   资源依赖：R.id.title, R.id.seekbar, R.id.show, R.id.triggerSensitivity*    ║
 * ║   外部依赖：SeekBar.OnSeekBarChangeListener (进度变化回调接口)                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   // XML布局中使用                                                            ║
 * ║   <com.micsig.tbook.ui.top.view.TopViewSeekBar                               ║
 * ║       android:layout_width="wrap_content"                                     ║
 * ║       android:layout_height="wrap_content"                                    ║
 * ║       app:headWidth="120px"                                                   ║
 * ║       app:seekBarWidth="500px" />                                             ║
 * ║                                                                               ║
 * ║   // Java代码中初始化                                                          ║
 * ║   TopViewSeekBar seekBar = findViewById(R.id.mySeekBar);                      ║
 * ║   seekBar.setData("音量", 100, 50, new SeekBar.OnSeekBarChangeListener() {    ║
 * ║       @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {║
 * ║           // 处理进度变化                                                      ║
 * ║       }                                                                       ║
 * ║   });                                                                         ║
 * ║                                                                               ║
 * ║   // 启用触发灵敏度模式                                                        ║
 * ║   seekBar.setTriggerSeekBar(true);                                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 必须调用setData()方法初始化数据后才能正常使用                              ║
 * ║   2. 触发灵敏度模式下，百分比按钮会被隐藏，显示高低阈值标签                       ║
 * ║   3. 第15行导入org.w3c.dom.Text未被使用，建议移除                               ║
 * ║   4. onClickListener中硬编码了进度增量值10，可能需要根据业务调整                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author Administrator
 * @version 1.0
 * @since 2017/4/6
 * @see LinearLayout
 * @see SeekBar.OnSeekBarChangeListener
 */
public class TopViewSeekBar extends LinearLayout {

    // ==================== 成员变量区域 ====================
    
    /** Android上下文对象，用于访问资源、启动Activity等系统操作 */
    private Context context;
    
    /** 滑动条标题文本，显示在左侧标题TextView中 */
    private String seekbarTitle;

    /** 触发灵敏度低阈值标签，在触发灵敏度模式下显示在滑动条左侧 */
    private TextView triggerLow;
    
    /** 触发灵敏度度高阈值标签，在触发灵敏度模式下显示在滑动条右侧 */
    private TextView triggerHigh;
    
    /** 滑动条最大值，决定了进度范围的上限 */
    private int seekbarMax;
    
    /** 滑动条初始值，组件初始化时的默认进度 */
    private int seekbarInit;
    
    /** 百分比显示按钮，用于显示当前进度的百分比形式，也可点击增加进度 */
    private Button show;
    
    /** 标题区域的宽度（像素），可通过XML属性headWidth自定义 */
    private int headWidth;
    
    /** 滑动条区域的宽度（像素），可通过XML属性seekBarWidth自定义 */
    private int seekbarWidth;
    
    /** 内部SeekBar控件引用，用于获取和设置进度值 */
    private SeekBar seekBar;
    
    /** 外部传入的进度变化监听器，用于回调进度变化事件给调用者 */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener;

    // ==================== 构造方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【构造方法】单参数构造器
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   仅使用Context创建组件实例，适用于Java代码动态创建场景。
     *   内部委托给三参数构造器，attrs和defStyleAttr使用默认值null和0。
     * 
     * 【参数说明】
     *   @param context Android上下文对象，不能为null
     *                  用于访问资源、主题、系统服务等
     * 
     * 【使用场景】
     *   在Java代码中动态创建TopViewSeekBar实例时使用
     * 
     * 【调用链】
     *   this(context, null) -> TopViewSeekBar(Context, AttributeSet)
     *                       -> TopViewSeekBar(Context, AttributeSet, int)
     * ═══════════════════════════════════════════════════════════════════════
     */
    public TopViewSeekBar(Context context) { // 构造方法入口：接收Context参数
        this(context, null); // 委托给双参数构造器，attrs传null表示无XML属性
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【构造方法】双参数构造器
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   使用Context和AttributeSet创建组件实例，适用于XML布局文件中声明。
     *   内部委托给三参数构造器，defStyleAttr使用默认值0。
     * 
     * 【参数说明】
     *   @param context    Android上下文对象，不能为null
     *   @param attrs      XML属性集，包含布局文件中定义的属性值
     *                     如app:headWidth、app:seekBarWidth等自定义属性
     * 
     * 【使用场景】
     *   当在XML布局文件中声明此组件时，系统会调用此构造器
     * 
     * 【调用链】
     *   this(context, attrs, 0) -> TopViewSeekBar(Context, AttributeSet, int)
     * ═══════════════════════════════════════════════════════════════════════
     */
    public TopViewSeekBar(Context context, AttributeSet attrs) { // 构造方法入口：接收Context和属性集
        this(context, attrs, 0); // 委托给三参数构造器，defStyleAttr传0表示使用默认样式
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【构造方法】三参数构造器（主构造器）
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   完整的构造方法，执行所有初始化逻辑。
     *   调用父类构造器、保存Context引用、初始化视图。
     * 
     * 【参数说明】
     *   @param context      Android上下文对象，不能为null
     *   @param attrs        XML属性集，可为null（Java代码创建时）
     *   @param defStyleAttr 默认样式属性，用于应用主题样式
     *                       0表示不应用特定样式，使用系统默认
     * 
     * 【执行流程】
     *   1. 调用父类LinearLayout的构造方法完成基础初始化
     *   2. 保存Context引用供后续使用
     *   3. 调用initView()完成视图初始化
     * ═══════════════════════════════════════════════════════════════════════
     */
    public TopViewSeekBar(Context context, AttributeSet attrs, int defStyleAttr) { // 构造方法入口：接收全部参数
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout的构造方法，完成基础初始化
        this.context = context; // 保存Context引用，供后续资源访问和方法调用使用
        initView(context, attrs, defStyleAttr); // 调用初始化方法，完成视图的创建和属性解析
    }

    // ==================== 初始化方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】initView - 视图初始化
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   执行视图的初始化操作，包括：
     *   1. 膨胀XML布局文件并附加到当前LinearLayout
     *   2. 设置布局方向为水平排列
     *   3. 设置子视图垂直居中对齐
     *   4. 解析并应用自定义XML属性
     * 
     * 【参数说明】
     *   @param context      Android上下文对象
     *   @param attrs        XML属性集，包含自定义属性值
     *   @param defStyleAttr 默认样式属性
     * 
     * 【处理逻辑】
     *   1. View.inflate()：将R.layout.view_seekbarwithhead布局文件
     *      膨胀并直接附加到this（当前LinearLayout实例）
     *   2. setOrientation(HORIZONTAL)：设置子视图水平排列
     *   3. setGravity(CENTER_VERTICAL)：设置子视图垂直居中
     *   4. TypedArray：读取自定义属性headWidth和seekBarWidth
     *   5. recycle()：回收TypedArray资源，避免内存泄漏
     * 
     * 【注意事项】
     *   TypedArray必须调用recycle()回收，否则会导致内存泄漏
     * ═══════════════════════════════════════════════════════════════════════
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) { // 私有初始化方法，仅在构造器中调用
        View.inflate(context, R.layout.view_seekbarwithhead, this); // 膨胀布局文件view_seekbarwithhead.xml并附加到当前视图
        setOrientation(HORIZONTAL); // 设置LinearLayout的方向为水平，子视图从左到右排列
        setGravity(Gravity.CENTER_VERTICAL); // 设置子视图在垂直方向上居中对齐
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSeekBar); // 获取自定义属性的类型化数组
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSeekBar_headWidth, 100); // 读取标题宽度属性，默认100像素
        seekbarWidth = ta.getDimensionPixelSize(R.styleable.TopViewSeekBar_seekBarWidth, 480); // 读取滑动条宽度属性，默认480像素
        ta.recycle(); // 回收TypedArray资源，这是必须的操作，防止内存泄漏
    }

    // ==================== 数据设置方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】setData - 设置滑动条数据（字符串标题）
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   配置滑动条的所有必要数据，包括标题、范围、初始值和监听器。
     *   调用此方法后会触发updateView()更新UI显示。
     * 
     * 【参数说明】
     *   @param seekbarTitle        滑动条标题文本，显示在左侧标题区域
     *                              例如："音量"、"亮度"、"灵敏度"等
     *   @param seekbarMax          滑动条最大值，决定进度范围上限
     *                              例如：100表示百分比，255表示音量级别
     *   @param seekbarInit         滑动条初始进度值，应在[0, seekbarMax]范围内
     *   @param seekBarChangeListener 进度变化监听器，可为null
     *                                用于接收进度变化、开始拖动、停止拖动事件
     * 
     * 【使用示例】
     *   seekBar.setData("音量", 100, 50, new SeekBar.OnSeekBarChangeListener() {
     *       @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
     *           Log.d("TAG", "当前进度: " + progress);
     *       }
     *       @Override public void onStartTrackingTouch(SeekBar sb) {}
     *       @Override public void onStopTrackingTouch(SeekBar sb) {}
     *   });
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void setData(String seekbarTitle, int seekbarMax, int seekbarInit, SeekBar.OnSeekBarChangeListener seekBarChangeListener) { // 公开方法：设置滑动条数据
        this.seekbarTitle = seekbarTitle; // 保存标题文本到成员变量
        this.seekbarMax = seekbarMax; // 保存最大值到成员变量
        this.seekbarInit = seekbarInit; // 保存初始值到成员变量
        this.seekBarChangeListener = seekBarChangeListener; // 保存监听器引用到成员变量
        updateView(); // 调用updateView()方法，根据新数据更新UI显示
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】setData - 设置滑动条数据（字符串资源ID）
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   使用字符串资源ID设置标题的重载方法。
     *   内部通过context.getString()将资源ID转换为字符串，然后调用主setData方法。
     * 
     * 【参数说明】
     *   @param seekbarTitleResId   标题字符串资源ID，如R.string.volume_title
     *                              会自动根据当前语言环境加载对应字符串
     *   @param seekbarMax          滑动条最大值
     *   @param seekbarInit         滑动条初始进度值
     *   @param seekBarChangeListener 进度变化监听器，可为null
     * 
     * 【优势】
     *   支持国际化，标题文本会根据系统语言自动切换
     * 
     * 【使用示例】
     *   seekBar.setData(R.string.volume_title, 100, 50, listener);
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void setData(int seekbarTitleResId, int seekbarMax, int seekbarInit, SeekBar.OnSeekBarChangeListener seekBarChangeListener) { // 公开方法：使用资源ID设置数据
        setData(context.getString(seekbarTitleResId), seekbarMax, seekbarInit, seekBarChangeListener); // 将资源ID转换为字符串后调用主setData方法
    }

    // ==================== 视图更新方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】updateView - 更新视图显示
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   根据成员变量中的数据更新UI显示，包括：
     *   1. 设置标题文本和宽度
     *   2. 设置百分比按钮的初始文本和状态
     *   3. 配置SeekBar的最大值、初始进度和宽度
     *   4. 绑定SeekBar的进度变化监听器
     * 
     * 【执行流程】
     *   ┌─────────────────────────────────────────────────────────────┐
     *   │ 1. 获取标题TextView，设置文本和宽度                            │
     *   │ 2. 获取百分比Button，设置初始百分比文本                         │
     *   │ 3. 设置Button点击监听器（虽然被禁用）                           │
     *   │ 4. 禁用Button点击功能                                         │
     *   │ 5. 获取SeekBar，设置最大值和初始进度                           │
     *   │ 6. 设置SeekBar宽度                                            │
     *   │ 7. 绑定SeekBar监听器                                          │
     *   │ 8. 清除SeekBar的点击监听（防止误触）                            │
     *   └─────────────────────────────────────────────────────────────┘
     * 
     * 【注意事项】
     *   - 第88行seekBar.setOnClickListener(null)是为了防止点击其他区域时SeekBar响应
     *   - 百分比计算公式：seekbarInit * 100 / seekbarMax
     *   - Button被设置为禁用状态(setEnabled(false))，仅用于显示
     * ═══════════════════════════════════════════════════════════════════════
     */
    private void updateView() { // 私有方法：更新视图显示，在setData()后调用
        // ========== 标题TextView配置 ==========
        TextView title = (TextView) findViewById(R.id.title); // 通过ID获取布局中的标题TextView
        title.setText(seekbarTitle); // 设置标题文本内容
        LinearLayout.LayoutParams lpTitle = (LayoutParams) title.getLayoutParams(); // 获取标题的布局参数
        lpTitle.width = headWidth; // 设置标题区域的宽度为自定义的headWidth
        title.setLayoutParams(lpTitle); // 应用修改后的布局参数

        // ========== 百分比Button配置 ==========
        show = (Button) findViewById(R.id.show); // 通过ID获取布局中的百分比显示Button
        show.setText(seekbarInit * 100 / seekbarMax + "%"); // 计算并设置初始百分比文本，例如"50%"
        show.setOnClickListener(onClickListener); // 设置点击监听器（虽然按钮被禁用，但仍保留监听器引用）
        show.setEnabled(false); // 禁用按钮的点击功能，使其仅作为显示用途

        // ========== SeekBar配置 ==========
        seekBar = (SeekBar) findViewById(R.id.seekbar); // 通过ID获取布局中的SeekBar控件
        seekBar.setMax(seekbarMax); // 设置滑动条的最大值为自定义的seekbarMax
        seekBar.setProgress(seekbarInit); // 设置滑动条的初始进度为seekbarInit
        LinearLayout.LayoutParams lpSeekbar = (LayoutParams) seekBar.getLayoutParams(); // 获取SeekBar的布局参数
        lpSeekbar.width = seekbarWidth; // 设置滑动条区域的宽度为自定义的seekbarWidth
        seekBar.setLayoutParams(lpSeekbar); // 应用修改后的布局参数
        seekBar.setOnSeekBarChangeListener(mChangeListener); // 绑定内部进度变化监听器
        seekBar.setOnClickListener(null); // 清除SeekBar的点击监听，防止点击其他区域时SeekBar有响应
    }

    // ==================== 进度访问方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】getProgress - 获取当前进度
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   获取滑动条当前的进度值。
     * 
     * 【返回值】
     *   @return 当前进度值，范围[0, seekbarMax]
     * 
     * 【使用场景】
     *   当需要获取用户设置的滑动条值时调用
     * 
     * 【使用示例】
     *   int currentVolume = seekBar.getProgress();
     * ═══════════════════════════════════════════════════════════════════════
     */
    public int getProgress() { // 公开方法：获取当前进度值
        return seekBar.getProgress(); // 返回SeekBar的当前进度
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】setProgress - 设置当前进度
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   以编程方式设置滑动条的进度值。
     *   设置后会触发onProgressChanged回调。
     * 
     * 【参数说明】
     *   @param progress 要设置的进度值，应在[0, seekbarMax]范围内
     *                   超出范围的值会被自动限制在有效范围内
     * 
     * 【使用场景】
     *   从存储加载设置值、恢复默认值、或程序控制进度时使用
     * 
     * 【使用示例】
     *   seekBar.setProgress(75); // 设置进度为75
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void setProgress(int progress) { // 公开方法：设置进度值
        seekBar.setProgress(progress); // 设置SeekBar的进度值
    }

    // ==================== 内部监听器区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【监听器】onClickListener - 按钮点击监听器
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   百分比按钮的点击监听器。
     *   每次点击将进度增加10个单位。
     * 
     * 【注意事项】
     *   由于updateView()中调用了show.setEnabled(false)，
     *   此监听器实际上不会触发，按钮仅用于显示百分比。
     *   如果需要启用点击功能，需要移除setEnabled(false)调用。
     * 
     * 【潜在改进】
     *   进度增量值10是硬编码的，建议改为可配置参数
     * ═══════════════════════════════════════════════════════════════════════
     */
    private OnClickListener onClickListener = new OnClickListener() { // 创建按钮点击监听器实例
        @Override
        public void onClick(View v) { // 点击事件回调方法
            seekBar.setProgress(seekBar.getProgress() + 10); // 将当前进度增加10
        }
    };

    // ==================== 触发灵敏度模式方法区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【方法】setTriggerSeekBar - 设置触发灵敏度模式
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   切换滑动条的显示模式：
     *   - 普通模式：显示标题 + 滑动条 + 百分比按钮
     *   - 触发灵敏度模式：显示标题 + 低阈值标签 + 滑动条 + 高阈值标签
     * 
     * 【参数说明】
     *   @param isTriggerSeekBar true表示启用触发灵敏度模式
     *                           false表示使用普通模式（当前未实现false逻辑）
     * 
     * 【UI变化】
     *   启用触发灵敏度模式后：
     *   ┌──────────────────────────────────────────────────────────┐
     *   │ [标题] [低] [========滑动条========] [高]                  │
     *   └──────────────────────────────────────────────────────────┘
     *   替代原来的：
     *   ┌──────────────────────────────────────────────────────────┐
     *   │ [标题] [========滑动条========] [50%]                     │
     *   └──────────────────────────────────────────────────────────┘
     * 
     * 【使用场景】
     *   用于触发灵敏度设置界面，需要显示高低阈值范围提示
     * 
     * 【注意事项】
     *   当前实现只处理了isTriggerSeekBar=true的情况，
     *   如果需要从触发模式切换回普通模式，需要补充false分支逻辑
     * ═══════════════════════════════════════════════════════════════════════
     */
    public void setTriggerSeekBar(boolean isTriggerSeekBar) { // 公开方法：设置触发灵敏度模式
        if (isTriggerSeekBar) { // 判断是否启用触发灵敏度模式
            triggerLow = findViewById(R.id.triggerSensitivityLow); // 获取低阈值标签TextView
            triggerHigh = findViewById(R.id.triggerSensitivityHigh); // 获取高阈值标签TextView
            show = (Button) findViewById(R.id.show); // 重新获取百分比按钮引用
            triggerLow.setVisibility(View.VISIBLE); // 设置低阈值标签为可见
            triggerHigh.setVisibility(View.VISIBLE); // 设置高阈值标签为可见
            show.setVisibility(View.GONE); // 隐藏百分比按钮，使用GONE完全移除布局空间
        }
        // 注意：缺少else分支，无法从触发模式切换回普通模式
    }

    // ==================== SeekBar监听器区域 ====================

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * 【监听器】mChangeListener - SeekBar进度变化监听器
     * ═══════════════════════════════════════════════════════════════════════
     * 【功能描述】
     *   内部SeekBar进度变化监听器，负责：
     *   1. 更新百分比按钮的显示文本
     *   2. 将事件转发给外部监听器（如果已设置）
     * 
     * 【事件流程】
     *   ┌─────────────────────────────────────────────────────────────┐
     *   │ 用户拖动SeekBar                                              │
     *   │      ↓                                                       │
     *   │ onProgressChanged() 被调用                                   │
     *   │      ↓                                                       │
     *   │ 更新百分比按钮文本 (如 "50%")                                 │
     *   │      ↓                                                       │
     *   │ 调用外部监听器的onProgressChanged()                          │
     *   └─────────────────────────────────────────────────────────────┘
     * 
     * 【方法说明】
     *   - onProgressChanged：进度变化时调用，实时更新百分比显示
     *   - onStartTrackingTouch：用户开始拖动时调用
     *   - onStopTrackingTouch：用户停止拖动时调用
     * 
     * 【设计模式】
     *   使用委托模式（Delegation Pattern），将事件转发给外部监听器
     * ═══════════════════════════════════════════════════════════════════════
     */
    private SeekBar.OnSeekBarChangeListener mChangeListener = new SeekBar.OnSeekBarChangeListener() { // 创建SeekBar监听器实例
        
        /**
         * ═════════════════════════════════════════════════════════════════
         * 【回调】onProgressChanged - 进度变化回调
         * ═════════════════════════════════════════════════════════════════
         * 【触发时机】
         *   滑动条进度发生变化时调用，包括用户拖动和程序设置
         * 
         * 【参数说明】
         *   @param seekBar   触发事件的SeekBar实例
         *   @param progress  当前进度值，范围[0, max]
         *   @param fromUser  true表示用户拖动触发，false表示程序设置触发
         * 
         * 【处理逻辑】
         *   1. 更新百分比按钮文本为当前进度百分比
         *   2. 如果设置了外部监听器，转发事件
         * ═════════════════════════════════════════════════════════════════
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 进度变化回调方法
            show.setText(progress + "%"); // 更新百分比按钮文本为当前进度百分比，如"75%"
            if (seekBarChangeListener != null) { // 检查是否设置了外部监听器
                seekBarChangeListener.onProgressChanged(seekBar, progress, fromUser); // 转发事件给外部监听器
            }
        }

        /**
         * ═════════════════════════════════════════════════════════════════
         * 【回调】onStartTrackingTouch - 开始拖动回调
         * ═════════════════════════════════════════════════════════════════
         * 【触发时机】
         *   用户开始拖动滑动条滑块时调用
         * 
         * 【参数说明】
         *   @param seekBar 触发事件的SeekBar实例
         * 
         * 【使用场景】
         *   可用于暂停某些操作、显示提示信息等
         * ═════════════════════════════════════════════════════════════════
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始拖动回调方法
            if (seekBarChangeListener != null) { // 检查是否设置了外部监听器
                seekBarChangeListener.onStartTrackingTouch(seekBar); // 转发事件给外部监听器
            }
        }

        /**
         * ═════════════════════════════════════════════════════════════════
         * 【回调】onStopTrackingTouch - 停止拖动回调
         * ═════════════════════════════════════════════════════════════════
         * 【触发时机】
         *   用户停止拖动滑动条滑块时调用
         * 
         * 【参数说明】
         *   @param seekBar 触发事件的SeekBar实例
         * 
         * 【使用场景】
         *   可用于保存最终值、执行耗时操作等
         * ═════════════════════════════════════════════════════════════════
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止拖动回调方法
            if (seekBarChangeListener != null) { // 检查是否设置了外部监听器
                seekBarChangeListener.onStopTrackingTouch(seekBar); // 转发事件给外部监听器
            }
        }
    }; // 监听器实例定义结束
} // 类定义结束
