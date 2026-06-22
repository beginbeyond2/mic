package com.micsig.tbook.ui.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                          用户自定义编辑视图
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 右侧滑菜单中的用户自定义编辑控件，用于显示和编辑用户自定义值，
 * 支持点击编辑和通道颜色主题。
 *
 * 【核心职责】
 * 1. 显示用户自定义标签和编辑值
 * 2. 支持点击编辑回调
 * 3. 支持通道颜色主题切换
 * 4. 支持启用/禁用状态
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    RightViewUserDefineEdit                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  视图层: editClickView / editUserDefineView                    │
 * │  数据层: defaultStr / setText() / getText()                    │
 * │  样式层: setControlColorByChIdx()                              │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * setText() → 更新TextView → 显示自定义值
 * 点击 → onEditClick回调 → 外部处理编辑
 *
 * 【通道颜色映射】
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  通道索引   │  背景资源                          │  文字颜色资源  │
 * ├──────────────────────────────────────────────────────────────────┤
 * │  0 (CH1)   │  selector_rightslip_button_ch1    │  ch1颜色       │
 * │  1 (CH2)   │  selector_rightslip_button_ch2    │  ch2颜色       │
 * │  2 (CH3)   │  selector_rightslip_button_ch3    │  ch3颜色       │
 * │  3 (CH4)   │  selector_rightslip_button_ch4    │  ch4颜色       │
 * │  4 (Math)  │  selector_rightslip_button_math   │  math颜色      │
 * │  5-8 (Ref) │  selector_rightslip_button_ref    │  ref颜色       │
 * │  9 (S1)    │  selector_rightslip_button_s1     │  s1颜色        │
 * │  10 (S2)   │  selector_rightslip_button_s2     │  s2颜色        │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 【使用示例】
 * RightViewUserDefineEdit editView = findViewById(R.id.userDefineEdit);
 * editView.setText("1.5V");                    // 设置自定义值
 * editView.setControlColorByChIdx(0);          // 设置CH1颜色主题
 * editView.setOnEditClickListener(listener);   // 设置编辑回调
 *
 * 【注意事项】
 * 1. 布局文件：R.layout.view_userdefine_edit
 * 2. 默认文本：R.string.serialsUserDefine
 * 3. 空值时显示默认文本，非空时显示默认文本+自定义值
 */
public class RightViewUserDefineEdit extends RelativeLayout {
    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 点击编辑按钮 */
    private TextView editClickView, editUserDefineView;

    /** 编辑点击回调 */
    private OnEditClickListener onEditClickListener;

    /** 默认文本（"UserDefine"） */
    private String defaultStr = "UserDefine";

    // ═════════════════════════════════════════════════════════════════════════════
    // 回调接口
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 设置编辑点击回调
     */
    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    /**
     * 编辑点击回调接口
     */
    public interface OnEditClickListener {
        /**
         * 编辑点击回调
         * @param view 当前视图
         * @param text 当前文本（不含默认文本）
         */
        void onEditClick(RightViewUserDefineEdit view, String text);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    public RightViewUserDefineEdit(Context context) {
        this(context, null);
    }

    public RightViewUserDefineEdit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightViewUserDefineEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化视图
     */
    private void initView() {
        // 加载布局
        View.inflate(context, R.layout.view_userdefine_edit, this);
        // 获取视图引用
        editClickView = (TextView) findViewById(R.id.editClickView);
        editUserDefineView = (TextView) findViewById(R.id.editUserDefineView);
        // 设置点击监听
        editClickView.setOnClickListener(onClickListener);
        // 获取默认文本
        defaultStr = context.getResources().getString(R.string.serialsUserDefine);
        // 设置默认文本
        editUserDefineView.setText(defaultStr);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 设置文本
     * @param text 自定义值文本
     */
    @SuppressLint("SetTextI18n")
    public void setText(String text) {
        if (StrUtil.isEmpty(text)) {
            // 空值：显示默认文本
            editUserDefineView.setText(defaultStr);
            editUserDefineView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        } else {
            // 非空：显示默认文本+自定义值（换行）
            editUserDefineView.setText(defaultStr + "\n" + text);
            editUserDefineView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18);
        }
    }

    /**
     * 根据通道索引设置颜色主题
     * @param chIdx 通道索引（0-10）
     */
    @SuppressLint("ResourceType")
    public void setControlColorByChIdx(int chIdx){
        int itemBgViewResId=0;
        ColorStateList itemTextColor = null;
        switch (chIdx){
            case 0: itemBgViewResId= R.drawable.selector_rightslip_button_ch1;
                itemTextColor=getResources().getColorStateList( R.drawable.selector_rightslip_select_item_textcolor_ch1);
                break;
            case 1: itemBgViewResId= R.drawable.selector_rightslip_button_ch2;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch2);
                break;
            case 2: itemBgViewResId= R.drawable.selector_rightslip_button_ch3;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch3);
                break;
            case 3: itemBgViewResId= R.drawable.selector_rightslip_button_ch4;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch4);
                break;
            case 4: itemBgViewResId= R.drawable.selector_rightslip_button_math;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_math);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                itemBgViewResId= R.drawable.selector_rightslip_button_ref;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ref);
                break;
            case 9: itemBgViewResId= R.drawable.selector_rightslip_button_s1;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s1);
                break;
            case 10: itemBgViewResId= R.drawable.selector_rightslip_button_s2;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s2);
                break;
        }
        // 设置文字颜色
        editUserDefineView.setTextColor(itemTextColor);
    }

    /**
     * 设置启用状态
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editClickView.setEnabled(enabled);
        editUserDefineView.setEnabled(enabled);
    }

    /**
     * 获取文本（不含默认文本）
     * @return 自定义值文本
     */
    public String getText() {
        return editUserDefineView.getText().toString().replace(defaultStr, "").replace("\n", "");
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 点击监听
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 点击监听器
     */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(RightViewUserDefineEdit.this, editUserDefineView.getText().toString().replace(defaultStr, "").replace("\n", ""));
            }
        }
    };
}
