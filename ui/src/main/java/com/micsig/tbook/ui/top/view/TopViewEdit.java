package com.micsig.tbook.ui.top.view; // 定义包路径，属于UI模块的顶部视图组件

import android.content.Context; // 导入上下文类，用于访问系统资源和功能
import android.content.res.TypedArray; // 导入类型化数组，用于读取自定义属性
import android.graphics.Color; // 导入颜色类，用于颜色解析和设置
import android.util.AttributeSet; // 导入属性集，用于XML属性解析
import android.util.TypedValue; // 导入类型值，用于尺寸单位转换
import android.view.Gravity; // 导入重力类，用于视图对齐方式设置
import android.view.View; // 导入视图基类，提供基础视图功能
import android.view.ViewGroup; // 导入视图组基类，提供布局参数管理
import android.widget.LinearLayout; // 导入线性布局，作为本视图的父类
import android.widget.TextView; // 导入文本视图，用于显示标题和编辑内容

import com.micsig.base.Logger; // 导入日志工具类，用于调试信息输出
import com.micsig.tbook.ui.R; // 导入资源类，包含布局和样式资源引用
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类，用于字符串判空处理

import java.util.Arrays; // 导入数组工具类，用于数组转字符串输出

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                              TopViewEdit 类文档                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件层 - 顶部视图模块 - 编辑框视图组件                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                    │
 * │   1. 提供带标题的可点击编辑框视图，支持标题和编辑内容的显示                    │
 * │   2. 支持水平和垂直两种布局模式，适应不同的UI场景需求                          │
 * │   3. 提供丰富的自定义属性配置（宽度、字体大小、对齐方式等）                    │
 * │   4. 实现点击事件回调机制，支持外部处理编辑框点击事件                          │
 * │   5. 提供文本设置和获取接口，便于数据绑定和交互                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                    │
 * │   继承关系: TopViewEdit extends LinearLayout                                 │
 * │   组合模式: 内部组合TextView(headView + editView)实现标题和编辑框             │
 * │   回调模式: 通过OnClickEditListener接口实现点击事件回调                       │
 * │   属性解析: 通过TypedArray读取XML自定义属性                                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                    │
 * │   XML属性 → TypedArray解析 → 成员变量存储 → 视图初始化 → UI渲染              │
 * │   外部调用setText → editView更新 → UI刷新                                    │
 * │   用户点击editView → 触发onClick → 回调OnClickEditListener                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                    │
 * │   外部依赖: LinearLayout, TextView, Context, AttributeSet                    │
 * │   内部依赖: R.layout.view_editwithhead, R.styleable.TopViewEdit              │
 * │   工具依赖: Logger, StrUtil                                                  │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                    │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.top.view.TopViewEdit                                 │
 * │       android:layout_width="match_parent"                                    │
 * │       android:layout_height="wrap_content"                                   │
 * │       app:head="标题文本"                                                     │
 * │       app:editHint="提示文本"                                                │
 * │       app:orientationV="false" />                                           │
 * │                                                                              │
 * │   Java代码:                                                                  │
 * │   TopViewEdit topViewEdit = findViewById(R.id.topViewEdit);                 │
 * │   topViewEdit.setOnClickEditListener((v, text) -> {                         │
 * │       // 处理点击事件                                                         │
 * │   });                                                                        │
 * │   topViewEdit.setText("显示内容");                                           │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 版本历史:                                                                    │
 * │   v1.0.0 - 2017/04/21 - yangj - 初始创建，实现基础编辑框功能                 │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author yangj
 * @version 1.0.0
 * @since 2017/04/21
 */

public class TopViewEdit extends LinearLayout { // 继承LinearLayout，提供线性布局容器功能
    
    // ================================ 成员变量定义 ================================
    
    /** 上下文对象，用于访问系统资源和功能 */
    private Context context;
    
    /** 标题文本内容 */
    private String headText;
    
    /** 编辑框提示文本内容 */
    private String editHintText;
    
    /** 编辑框宽度（单位：像素） */
    private int editWidth;
    
    /** 标题宽度（单位：像素） */
    private int headWidth;
    
    /** 标题字体大小（单位：像素） */
    private int headTextSize;
    
    /** 标题底部边距（单位：像素），仅在垂直布局时生效 */
    private int headMarginBottom;
    
    /** 编辑框内容是否居中显示 */
    private boolean editCenter;
    
    /** 是否采用垂直布局模式 */
    private boolean orientationV;
    
    /** 标题是否靠左对齐，false则靠右对齐 */
    private boolean headViewLeft;
    
    /** 标题视图组件，用于显示headText */
    private TextView headView;
    
    /** 编辑框视图组件，用于显示和编辑内容 */
    private TextView editView;
    
    /** 点击事件监听器，用于回调点击事件 */
    private OnClickEditListener onClickEditListener;

    // ================================ 接口定义 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    OnClickEditListener 接口文档                    │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 编辑框点击事件监听器接口                                    │
     * │ 使用场景: 当用户点击编辑框时，通过此接口回调通知外部处理逻辑            │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 方法说明:                                                            │
     * │   onClickEdit(TopViewEdit v, String text) - 点击事件回调方法        │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setOnClickEditListener(new OnClickEditListener() {   │
     * │       @Override                                                     │
     * │       public void onClickEdit(TopViewEdit v, String text) {        │
     * │           // 处理点击事件                                            │
     * │       }                                                             │
     * │   });                                                               │
     * └────────────────────────────────────────────────────────────────────┘
     */
    public interface OnClickEditListener { // 定义点击事件监听器接口
        /**
         * 编辑框点击事件回调方法
         * 
         * @param v 被点击的TopViewEdit视图对象，用于获取视图上下文信息
         * @param text 当前编辑框中的文本内容，用于传递用户输入数据
         */
        void onClickEdit(TopViewEdit v, String text); // 定义点击回调方法签名
    }

    // ================================ 公共方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setOnClickEditListener 方法文档                 │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 设置编辑框点击事件监听器                                    │
     * │ 调用时机: 在视图初始化后，需要处理点击事件时调用                       │
     * │ 注意事项: 如果不设置监听器，点击编辑框将不会触发任何回调               │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   onClickEditListener - 点击事件监听器对象，可为null清除监听器        │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setOnClickEditListener((v, text) -> {                │
     * │       Log.d("TAG", "点击了编辑框，内容: " + text);                  │
     * │   });                                                               │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param onClickEditListener 点击事件监听器对象，用于接收点击回调
     */
    public void setOnClickEditListener(OnClickEditListener onClickEditListener) { // 设置点击监听器
        this.onClickEditListener = onClickEditListener; // 保存监听器引用
    }

    // ================================ 构造方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    TopViewEdit(Context) 构造方法文档               │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 单参数构造方法，用于代码中动态创建视图                       │
     * │ 调用场景: 在Java代码中直接new TopViewEdit(context)时调用             │
     * │ 注意事项: 此构造方法会调用双参数构造，最终调用三参数构造完成初始化     │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   context - 上下文对象，不能为null                                   │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   TopViewEdit topViewEdit = new TopViewEdit(this);                 │
     * │   layoutParams.add(topViewEdit);                                    │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param context 上下文对象，用于访问系统资源和功能
     */
    public TopViewEdit(Context context) { // 单参数构造方法
        this(context, null); // 调用双参数构造方法，attrs传null
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    TopViewEdit(Context, AttributeSet) 构造方法文档 │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 双参数构造方法，用于XML布局中创建视图                       │
     * │ 调用场景: 系统从XML布局文件inflate视图时自动调用                      │
     * │ 注意事项: 此构造方法会调用三参数构造完成最终初始化                     │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   context - 上下文对象，不能为null                                   │
     * │   attrs - XML属性集，包含布局文件中定义的属性                         │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   系统自动调用，无需手动调用                                          │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param context 上下文对象，用于访问系统资源和功能
     * @param attrs XML属性集，用于读取自定义属性值
     */
    public TopViewEdit(Context context, AttributeSet attrs) { // 双参数构造方法
        this(context, attrs, 0); // 调用三参数构造方法，defStyleAttr传0
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    TopViewEdit(Context, AttributeSet, int) 构造方法│
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 完整参数构造方法，执行实际的初始化逻辑                       │
     * │ 调用场景: 被其他构造方法链式调用，或自定义样式时调用                   │
     * │ 初始化流程:                                                          │
     * │   1. 调用父类构造方法                                                │
     * │   2. 保存上下文引用                                                  │
     * │   3. 执行视图初始化                                                  │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   context - 上下文对象，不能为null                                   │
     * │   attrs - XML属性集，包含布局文件中定义的属性                         │
     * │   defStyleAttr - 默认样式属性，0表示使用系统默认                      │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   通常由其他构造方法调用，也可自定义样式时直接调用                     │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param context 上下文对象，用于访问系统资源和功能
     * @param attrs XML属性集，用于读取自定义属性值
     * @param defStyleAttr 默认样式属性资源ID
     */
    public TopViewEdit(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout的构造方法
        this.context = context; // 保存上下文引用，供后续使用
        initView(attrs, defStyleAttr); // 执行视图初始化逻辑
    }

    // ================================ 私有方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    initView 方法文档                               │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 初始化视图，解析属性并设置布局                              │
     * │ 执行流程:                                                            │
     * │   1. 加载布局文件                                                   │
     * │   2. 解析自定义属性                                                 │
     * │   3. 设置布局方向和对齐方式                                          │
     * │   4. 初始化标题视图                                                 │
     * │   5. 初始化编辑框视图并设置点击监听                                   │
     * │   6. 应用数据到视图                                                 │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   attrs - XML属性集，包含布局文件中定义的属性                         │
     * │   defStyleAttr - 默认样式属性资源ID                                  │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 注意事项:                                                            │
     * │   - TypedArray使用后必须recycle()回收资源                            │
     * │   - 点击事件会记录视图位置信息用于调试                                │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param attrs XML属性集，用于读取自定义属性值
     * @param defStyleAttr 默认样式属性资源ID
     */
    private void initView(AttributeSet attrs, int defStyleAttr) { // 初始化视图方法
        // ========== 第一步：加载布局文件 ==========
        View.inflate(context, R.layout.view_editwithhead, this); // 将布局文件填充到当前视图
        
        // ========== 第二步：解析自定义属性 ==========
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewEdit); // 获取自定义属性数组
        
        // 解析布局方向属性，默认为false（水平布局）
        orientationV = ta.getBoolean(R.styleable.TopViewEdit_orientationV, false); // 读取垂直布局标志
        
        // 解析标题文本属性
        headText = ta.getString(R.styleable.TopViewEdit_head); // 读取标题文本
        
        // 解析编辑框提示文本属性
        editHintText = ta.getString(R.styleable.TopViewEdit_editHint); // 读取编辑框提示文本
        
        // 解析编辑框居中属性，默认为false
        editCenter = ta.getBoolean(R.styleable.TopViewEdit_editCenter, false); // 读取编辑框居中标志
        
        // 解析标题对齐属性，默认为true（靠左）
        headViewLeft = ta.getBoolean(R.styleable.TopViewEdit_headViewGravityLeft, true); // 读取标题靠左标志
        
        // 解析编辑框宽度属性，默认350像素
        editWidth = ta.getDimensionPixelSize(R.styleable.TopViewEdit_editWidth, 350); // 读取编辑框宽度
        
        // 解析标题宽度属性，默认120像素
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewEdit_headWidth, 120); // 读取标题宽度
        
        // 解析标题字体大小属性，默认20像素
        headTextSize = ta.getDimensionPixelSize(R.styleable.TopViewEdit_headTextSize, 20); // 读取标题字体大小
        
        // 解析标题底部边距属性，默认10像素
        headMarginBottom = ta.getDimensionPixelSize(R.styleable.TopViewEdit_headMarginBottom, 10); // 读取标题底部边距
        
        // 回收TypedArray资源，避免内存泄漏
        ta.recycle(); // 必须调用recycle回收资源
        
        // ========== 第三步：设置布局方向和对齐方式 ==========
        // 根据orientationV设置布局方向
        setOrientation(orientationV ? VERTICAL : HORIZONTAL); // 设置为垂直或水平布局
        
        // 根据布局方向设置对齐方式
        setGravity(orientationV ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL); // 垂直时水平居中，水平时垂直居中
        
        // ========== 第四步：初始化标题视图 ==========
        headView = (TextView) findViewById(R.id.headView); // 获取标题视图引用
        headView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headTextSize); // 设置标题字体大小（像素单位）
        
        // 根据headViewLeft设置标题对齐方式
        if (headViewLeft) { // 如果标题靠左对齐
            headView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT); // 设置垂直居中且靠左
        } else { // 否则标题靠右对齐
            headView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT); // 设置垂直居中且靠右
        }
        
        // ========== 第五步：初始化编辑框视图 ==========
        editView = (TextView) findViewById(R.id.editView); // 获取编辑框视图引用
        editView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headTextSize); // 设置编辑框字体大小（像素单位）
        
        // 设置编辑框点击监听器
        editView.setOnClickListener(new OnClickListener() { // 设置点击事件监听器
            @Override
            public void onClick(View v) { // 点击事件处理方法
                // 创建数组用于存储视图在屏幕上的位置坐标
                int[] ints = new int[2]; // 初始化坐标数组，用于存储x和y坐标
                
                // 获取视图在屏幕上的位置
                v.getLocationOnScreen(ints); // 将视图左上角坐标存入数组
                
                // 记录调试信息：位置坐标、宽度和高度
                Logger.i("TopViewEdit:" + Arrays.toString(ints) + "\t" + v.getWidth() + "\t" + v.getHeight()); // 输出调试日志
                
                // 如果设置了点击监听器，则回调
                if (onClickEditListener != null) { // 检查监听器是否存在
                    onClickEditListener.onClickEdit(TopViewEdit.this, editView.getText().toString()); // 回调点击事件
                }
            }
        });
        
        // ========== 第六步：应用数据到视图 ==========
        setData(); // 调用setData方法应用解析的属性数据
    }

    // ================================ 重写方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setEnabled 方法文档                             │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 设置视图的启用状态                                         │
     * │ 行为说明:                                                            │
     * │   - 启用状态：编辑框可点击，响应点击事件                              │
     * │   - 禁用状态：编辑框变灰，不响应点击事件                              │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   enabled - true启用，false禁用                                     │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setEnabled(false); // 禁用编辑框                      │
     * │   topViewEdit.setEnabled(true);  // 启用编辑框                      │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param enabled true表示启用，false表示禁用
     */
    @Override
    public void setEnabled(boolean enabled) { // 重写setEnabled方法
        super.setEnabled(enabled); // 调用父类方法设置整体视图状态
        editView.setEnabled(enabled); // 同步设置编辑框的启用状态
    }

    // ================================ 私有方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setData 方法文档（无参）                         │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 将成员变量中的数据应用到视图上                              │
     * │ 执行流程:                                                            │
     * │   1. 设置标题文本（如果不为空）                                       │
     * │   2. 设置编辑框提示文本（如果不为空）                                 │
     * │   3. 设置编辑框宽度                                                  │
     * │   4. 设置编辑框内容对齐方式                                          │
     * │   5. 设置标题宽度和边距                                              │
     * │   6. 设置标题对齐方式（垂直布局时）                                   │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 注意事项:                                                            │
     * │   - 标题宽度会根据是否有标题文本动态调整                              │
     * │   - 垂直布局时会添加标题底部边距                                      │
     * │   - 垂直布局时标题会强制居中                                          │
     * └────────────────────────────────────────────────────────────────────┘
     */
    private void setData() { // 应用数据到视图的私有方法
        // ========== 第一步：设置标题文本 ==========
        if (!StrUtil.isEmpty(headText)) { // 如果标题文本不为空
            headView.setText(headText); // 设置标题视图的文本内容
        }
        
        // ========== 第二步：设置编辑框提示文本 ==========
        if (!StrUtil.isEmpty(editHintText)) { // 如果编辑框提示文本不为空
            editView.setText(editHintText); // 设置编辑框视图的提示文本
        }
        
        // ========== 第三步：设置编辑框宽度 ==========
        ViewGroup.LayoutParams editParams = editView.getLayoutParams(); // 获取编辑框的布局参数
        editParams.width = editWidth; // 设置编辑框宽度
        editView.setLayoutParams(editParams); // 应用新的布局参数
        
        // ========== 第四步：设置编辑框内容对齐方式 ==========
        if (editCenter) { // 如果设置了编辑框内容居中
            editView.setGravity(Gravity.CENTER); // 设置编辑框内容居中对齐
        }
        
        // ========== 第五步：设置标题宽度和边距 ==========
        LinearLayout.LayoutParams headParams = (LinearLayout.LayoutParams) headView.getLayoutParams(); // 获取标题的布局参数
        // 如果标题文本不为空则使用headWidth，否则宽度设为0（隐藏标题）
        headParams.width = !StrUtil.isEmpty(headText) ? headWidth : 0; // 动态设置标题宽度
        
        // 如果是垂直布局，设置标题底部边距
        if (orientationV) { // 如果是垂直布局模式
            headParams.setMargins(0, 0, 0, headMarginBottom); // 设置标题底部边距（左、上、右、下）
        }
        headView.setLayoutParams(headParams); // 应用新的布局参数
        
        // ========== 第六步：设置标题对齐方式（垂直布局时） ==========
        if (orientationV) { // 如果是垂直布局模式
            headView.setGravity(Gravity.CENTER); // 强制设置标题居中对齐
        }
    }

    // ================================ 公共方法 ================================
    
    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setData 方法文档（双参）                         │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 同时设置标题和编辑框提示文本                                │
     * │ 使用场景: 动态更新视图内容时调用                                      │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   headText - 标题文本内容，可为null或空字符串                         │
     * │   editHintText - 编辑框提示文本，可为null或空字符串                   │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setData("用户名", "请输入用户名");                     │
     * │   topViewEdit.setData("密码", "请输入密码");                         │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param headText 标题文本内容
     * @param editHintText 编辑框提示文本
     */
    public void setData(String headText, String editHintText) { // 设置标题和编辑框提示文本
        this.headText = headText; // 保存标题文本到成员变量
        this.editHintText = editHintText; // 保存编辑框提示文本到成员变量
        setData(); // 调用setData方法应用数据到视图
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    getHead 方法文档                                │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 获取当前标题文本                                           │
     * │ 返回值: 当前标题文本字符串，可能为null                                │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   String title = topViewEdit.getHead();                            │
     * │   Log.d("TAG", "当前标题: " + title);                               │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @return 标题文本内容，可能为null
     */
    public String getHead() { // 获取标题文本
        return headText; // 返回标题文本成员变量
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setEdit 方法文档                                │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 设置编辑框显示的文本内容                                    │
     * │ 使用场景: 需要显示预设文本或更新编辑框内容时调用                       │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   editText - 要显示的文本内容，可为null                               │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setEdit("张三"); // 设置编辑框内容                     │
     * │   topViewEdit.setEdit("");      // 清空编辑框内容                    │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param editText 要设置的文本内容
     */
    public void setEdit(String editText) { // 设置编辑框文本（兼容方法）
        setText(editText); // 调用setText方法设置文本
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    getText 方法文档                                │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 获取编辑框中的文本内容                                      │
     * │ 返回值: 编辑框中的文本内容（已去除首尾空格）                           │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 注意事项: 返回值会自动调用trim()去除首尾空格                          │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   String content = topViewEdit.getText();                          │
     * │   if (!content.isEmpty()) {                                        │
     * │       // 处理非空内容                                                │
     * │   }                                                                │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @return 编辑框中的文本内容（已去除首尾空格）
     */
    public String getText() { // 获取编辑框文本
        return editView.getText().toString().trim(); // 返回编辑框文本并去除首尾空格
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setText 方法文档                                │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 设置编辑框显示的文本内容                                    │
     * │ 使用场景: 需要显示预设文本或更新编辑框内容时调用                       │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   text - 要显示的文本内容，可为null                                   │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setText("显示内容");                                  │
     * │   topViewEdit.setText(null);  // 清空编辑框                         │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param text 要设置的文本内容
     */
    public void setText(String text) { // 设置编辑框文本
        editView.setText(text); // 设置编辑框视图的文本内容
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────┐
     * │                    setEditColor 方法文档                           │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 功能描述: 设置编辑框的背景颜色                                        │
     * │ 使用场景: 需要根据状态改变编辑框背景色时调用                           │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 参数说明:                                                            │
     * │   color - 颜色字符串，格式为#RRGGBB或#AARRGGBB                       │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 注意事项:                                                            │
     * │   - 颜色格式必须合法，否则会抛出异常                                  │
     * │   - 推荐使用格式: "#FF0000" (红色) 或 "#80FF0000" (半透明红色)       │
     * ├────────────────────────────────────────────────────────────────────┤
     * │ 使用示例:                                                            │
     * │   topViewEdit.setEditColor("#FFFFFF"); // 白色背景                  │
     * │   topViewEdit.setEditColor("#80FF0000"); // 半透明红色背景          │
     * └────────────────────────────────────────────────────────────────────┘
     * 
     * @param color 颜色字符串，格式为#RRGGBB或#AARRGGBB
     */
    public void setEditColor(String color) { // 设置编辑框背景颜色
        editView.setBackgroundColor(Color.parseColor(color)); // 解析颜色字符串并设置背景色
    }

}
