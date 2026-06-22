package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.widget.RelativeLayout; // 相对布局，作为基类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         MCheckBox - 自定义复选框布局控件                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 复选框布局组件                                        ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供基于RelativeLayout的复选框容器控件                                     ║
 * ║   2. 自动加载预定义的复选框布局资源                                            ║
 * ║   3. 简化复选框的使用，封装布局细节                                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MCheckBox extends RelativeLayout                                  ║
 * ║   设计模式: 组合模式，封装布局资源                                             ║
 * ║   布局资源: R.layout.layout_checkbox                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   构造方法 → inflate布局资源 → 添加到当前ViewGroup                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: 无                                                                ║
 * ║   外部依赖: Android SDK (RelativeLayout, Context等)                           ║
 * ║   资源依赖: R.layout.layout_checkbox                                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   XML布局:                                                                   ║
 * ║   <com.micsig.tbook.ui.MCheckBox                                             ║
 * ║       android:layout_width="wrap_content"                                     ║
 * ║       android:layout_height="wrap_content" />                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 布局内容由R.layout.layout_checkbox定义                                   ║
 * ║   2. 可通过findViewById获取内部子视图                                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @version 1.0
 */

public class MCheckBox extends RelativeLayout {
    // ================================ 成员变量定义 ================================
    
    private Context context; // Android上下文对象引用

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法，用于代码中动态创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     */
    public MCheckBox(Context context) { // 构造方法：仅传入Context
        this(context, null); // 调用双参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法，用于XML布局文件中创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     */
    public MCheckBox(Context context, AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成初始化
     *   加载预定义的复选框布局资源
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     */
    public MCheckBox(Context context, AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类RelativeLayout的构造方法
        init(context, attrs, defStyleAttr); // 调用初始化方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：init
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化方法，保存上下文引用并加载布局资源
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) { // 初始化方法
        this.context = context; // 保存上下文引用
        inflate(context, R.layout.layout_checkbox, this); // 加载复选框布局资源到当前ViewGroup
    }
}
