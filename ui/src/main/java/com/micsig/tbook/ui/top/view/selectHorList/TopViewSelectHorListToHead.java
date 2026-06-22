package com.micsig.tbook.ui.top.view.selectHorList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     TopViewSelectHorListToHead                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 带标题的水平选择列表视图                                            ║
 * ║ 核心职责: 显示标题和当前选中值，点击触发选择列表弹出                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌─────────────────────────────┐                                           ║
 * ║   │ TopViewSelectHorListToHead  │ ──继承──▶ LinearLayout                    ║
 * ║   └──────────────┬──────────────┘                                           ║
 * ║                  │ 包含                                                      ║
 * ║                  ▼                                                           ║
 * ║   ┌─────────────────────────────┐                                           ║
 * ║   │  [标题] [当前值▼]            │                                           ║
 * ║   └─────────────────────────────┘                                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   setData() ──▶ updateView() ──▶ 显示标题和当前值                            ║
 * ║   用户点击 ──▶ onClickListener回调                                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - TopViewSelectHorListToList: 弹出选择列表                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   TopViewSelectHorListToHead view = new TopViewSelectHorListToHead(context);║
 * ║   view.setData("时基", "1ms", clickListener);                                ║
 * ║   view.setText("10ms"); // 更新显示值                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopViewSelectHorListToHead extends LinearLayout {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 标题文本 */
    private String head;

    /** 显示文本 */
    private String show;

    /** 显示文本控件 */
    private TextView tvShow;

    /** 点击监听器 */
    private View.OnClickListener onClickListener;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法（单参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    public TopViewSelectHorListToHead(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    /**
     * 构造方法（双参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     * @param attrs   属性集
     */
    public TopViewSelectHorListToHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化视图
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void initView() {
        View.inflate(context, R.layout.view_selecthorizontallistwithhead, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数据
     * ══════════════════════════════════════════════════════════════════════════════
     * @param head     标题文本
     * @param show     显示文本
     * @param listener 点击监听器
     */
    public void setData(String head, String show, View.OnClickListener listener) {
        this.head = head;
        this.show = show;
        this.onClickListener = listener;
        updateView();
    }

    /**
     * 设置数据（从资源ID加载）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param headResId 标题资源ID
     * @param showResId 显示文本资源ID
     * @param listener  点击监听器
     */
    public void setData(int headResId, int showResId, View.OnClickListener listener) {
        this.head = context.getString(headResId);
        this.show = context.getString(showResId);
        this.onClickListener = listener;
        updateView();
    }

    /**
     * 更新视图
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void updateView() {
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvShow = (TextView) findViewById(R.id.show);
        tvTitle.setText(head);
        tvShow.setText(show);
        tvShow.setOnClickListener(onClickListener);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 其他方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置启用状态
     * ══════════════════════════════════════════════════════════════════════════════
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tvShow.setEnabled(enabled);
    }

    /**
     * 设置显示文本
     * ══════════════════════════════════════════════════════════════════════════════
     * @param showString 显示文本
     */
    public void setText(String showString) {
        if (StrUtil.isEmpty(showString)) {
            showString = "---";
        }
        tvShow.setText(showString);
    }

    /**
     * 获取显示文本
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 显示文本
     */
    public String getText() {
        if ("---".equals(tvShow.getText().toString())) {
            return "";
        }
        return tvShow.getText().toString();
    }

    /**
     * 获取显示文本控件
     * ══════════════════════════════════════════════════════════════════════════════
     * @return TextView实例
     */
    public TextView getTvShow() {
        return tvShow;
    }
}
