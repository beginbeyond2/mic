package com.micsig.tbook.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.R;


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                            分段适配布局控件
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 分段存储模式下的帧信息显示布局，用于显示当前选中帧的帧ID和时间信息，
 * 作为分段单次视图的辅助显示组件。
 *
 * 【核心职责】
 * 1. 显示帧ID（帧编号）
 * 2. 显示帧时间戳
 * 3. 提供数据设置和获取接口
 * 4. 使用XML布局文件定义视图结构
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    MainLayoutSegmentedFit                       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  视图层: layoutFit / tvFrame / tvTime                          │
 * │  数据层: setBean() / getBean()                                  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * setBean() → 更新TextView → 显示帧信息
 *
 * 【布局结构】
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  LinearLayout (layoutFit)                                        │
 * │  ├── 背景框                                                      │
 * │  └── 内容区域                                                    │
 * │      ├── TextView (tvFrame) - 帧ID                              │
 * │      └── TextView (tvTime) - 帧时间                             │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ R.layout     │────>│ view_segmentedfit    │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────────┐ ┌──────────────────────┐
 * │SegmentedSingleBean│─>│ 帧数据模型          │
 * └──────────────────┘ └──────────────────────┘
 *
 * 【使用示例】
 * MainLayoutSegmentedFit fitLayout = findViewById(R.id.fitLayout);
 * fitLayout.setBean(bean);           // 设置帧数据
 * SegmentedSingleBean bean = fitLayout.getBean();  // 获取帧数据
 *
 * 【注意事项】
 * 1. 布局文件：R.layout.view_segmentedfit
 * 2. 背景样式：R.drawable.shape_frame_bg_black
 * 3. getBean()方法会创建新的SegmentedSingleBean对象
 *
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainLayoutSegmentedFit extends LinearLayout {
    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 根布局容器 */
    private LinearLayout layoutFit;

    /** 帧ID显示文本 */
    private TextView tvFrame;

    /** 帧时间显示文本 */
    private TextView tvTime;

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 单参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainLayoutSegmentedFit实例，仅传入Context。
     *
     * 【参数说明】
     * @param context 上下文对象
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainLayoutSegmentedFit(Context context) {
        this(context, null);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - XML属性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainLayoutSegmentedFit实例，支持XML属性。
     *
     * 【参数说明】
     * @param context 上下文对象
     * @param attrs   XML属性集
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainLayoutSegmentedFit(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 完整参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建MainLayoutSegmentedFit实例，初始化视图组件。
     *
     * 【参数说明】
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     *
     * 【初始化流程】
     * 1. 保存上下文
     * 2. 加载XML布局
     * 3. 获取视图引用
     * 4. 设置背景样式
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainLayoutSegmentedFit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 保存上下文
        this.context = context;
        // 初始化视图
        init();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 初始化方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 加载XML布局并初始化视图组件引用。
     *
     * 【初始化流程】
     * 1. 加载R.layout.view_segmentedfit布局
     * 2. 获取根布局引用
     * 3. 获取TextView引用
     * 4. 设置背景样式
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void init() {
        // 加载XML布局到当前LinearLayout
        View inflate = inflate(context, R.layout.view_segmentedfit, this);
        // 获取根布局引用
        layoutFit = (LinearLayout) inflate.findViewById(R.id.dialogFitLayout);
        // 获取帧ID文本视图
        tvFrame = (TextView) inflate.findViewById(R.id.dialogFitFrame);
        // 获取帧时间文本视图
        tvTime = (TextView) inflate.findViewById(R.id.dialogFitTime);
        // 设置背景样式
        layoutFit.setBackgroundResource(R.drawable.shape_frame_bg_black);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置帧数据
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置要显示的帧数据，更新TextView显示内容。
     *
     * 【参数说明】
     * @param bean 帧数据对象，包含帧ID和时间信息
     *
     * 【显示内容】
     * - tvFrame: 显示帧ID（bean.getFrameId()）
     * - tvTime: 显示帧时间（bean.getTimeMs()）
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setBean(SegmentedSingleBean bean) {
        // 设置帧ID文本
        tvFrame.setText(String.valueOf(bean.getFrameId()));
        // 设置帧时间文本
        tvTime.setText(String.valueOf(bean.getTimeMs()));
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 获取帧数据
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 从当前显示内容构建并返回帧数据对象。
     *
     * 【返回值】
     * @return 新创建的SegmentedSingleBean对象，包含当前显示的帧ID和时间
     *
     * 【注意事项】
     * 此方法会创建新的SegmentedSingleBean对象，而非返回原始对象引用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public SegmentedSingleBean getBean() {
        // 从TextView获取帧ID和时间，创建新的数据对象
        return new SegmentedSingleBean(
                Integer.valueOf(tvFrame.getText().toString()), tvTime.getText().toString());
    }
}
