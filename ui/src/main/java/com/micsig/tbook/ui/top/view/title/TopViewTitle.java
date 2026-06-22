package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.micsig.tbook.ui.MRadioButton;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;

import java.util.ArrayList;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           TopViewTitle                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 顶部标题栏视图管理类                                                ║
 * ║ 核心职责: 创建和管理标题栏RadioButton列表，处理选中状态变化                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌─────────────────┐                                                       ║
 * ║   │   TopViewTitle   │                                                      ║
 * ║   └────────┬────────┘                                                       ║
 * ║            │ 组合                                                            ║
 * ║            ▼                                                                ║
 * ║   ┌─────────────────┐     ┌──────────────────┐                             ║
 * ║   │    RadioGroup    │     │   MRadioButton   │                             ║
 * ║   └─────────────────┘     └──────────────────┘                             ║
 * ║            │                                                                ║
 * ║            ▼                                                                ║
 * ║   ┌─────────────────┐     ┌──────────────────┐                             ║
 * ║   │ TopAllBeanTitle │     │ TopShowBeanTitle │                             ║
 * ║   │   (全部列表)     │     │   (显示列表)      │                             ║
 * ║   └─────────────────┘     └──────────────────┘                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   setData() ──▶ 创建数据列表 ──▶ updateView() ──▶ 创建RadioButton           ║
 * ║   用户点击 ──▶ checkedChangeListener ──▶ OnCheckChangedTitleListener回调    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - TopAllBeanTitle: 全部项数据模型                                         ║
 * ║   - TopShowBeanTitle: 显示项数据模型                                        ║
 * ║   - MRadioButton: 自定义RadioButton                                         ║
 * ║   - TopViewTitleWithScroll: 父容器，使用此类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   TopViewTitle title = new TopViewTitle(context);                          ║
 * ║   String[] titles = {"设置", "测量", "存储"};                                ║
 * ║   boolean[] visible = {true, true, true};                                   ║
 * ║   title.setData(titles, visible, listener, clickListener);                  ║
 * ║   title.setSelected(0); // 选中第一项                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 注意事项:                                                                    ║
 * ║   1. 支持可见性控制，只显示visible为true的项                                  ║
 * ║   2. 支持FIRST_TITLE和TWO_TITLE两种显示模式                                  ║
 * ║   3. 项宽度固定为ItemWidth（145像素）                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopViewTitle {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示区域总宽度 */
    public static final int WIDTH_SHOW = 1800;

    /** 每个标题项的宽度 */
    public static final int ItemWidth=145;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 上下文与数据
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 根视图 */
    private View inflate;

    /** 全部项列表（包含可见和不可见项） */
    private ArrayList<TopAllBeanTitle> allList;

    /** 显示项列表（仅包含可见项） */
    private ArrayList<TopShowBeanTitle> showList;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - UI组件
    // ═══════════════════════════════════════════════════════════════════════════════

    /** RadioGroup容器 */
    private RadioGroup radioGroup;

    /** 当前选中的RadioButton */
    private RadioButton checkedRadioButton;

    /** 选中变化监听器 */
    private OnCheckChangedTitleListener onCheckChangedTitleListener;

    /** 点击监听器 */
    private View.OnClickListener onItemClickListener;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 配置项
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 项宽度 */
    private int itemWidth;

    /** 选中/未选中/未选中下划线背景资源ID */
    private int itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine;

    /** 标题模式标志 */
    private int flag = TopViewTitleWithScroll.TWO_TITLE;

    /** 文本字号 */
    private int itemTextSize;

    /** 选中文本颜色 */
    private int itemTextColorSelect;

    /** 未选中文本颜色 */
    private int itemTextColorUnSelect;

    /** 是否添加了填充视图 */
    private boolean addView = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 选中变化监听接口
     */
    public interface OnCheckChangedTitleListener {
        /**
         * 选中变化回调
         * ══════════════════════════════════════════════════════════════════════════
         * @param view 视图
         * @param item 选中的数据项
         */
        void checkChanged(View view, TopAllBeanTitle item);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    public TopViewTitle(Context context) {
        this.context = context;
        initView(context);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数据
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据文本数组和可见性数组创建数据列表，更新视图
     * ══════════════════════════════════════════════════════════════════════════════
     * @param array                     文本数组
     * @param arrayVisible              可见性数组
     * @param onCheckChangedTitleListener 选中变化监听器
     * @param onItemClickListener       点击监听器
     */
    public void setData(String[] array, boolean[] arrayVisible, OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        // 创建全部项列表
        allList = new ArrayList<TopAllBeanTitle>();
        showList = new ArrayList<TopShowBeanTitle>();
        for (int i = 0; i < array.length; i++) {
            TopAllBeanTitle beanTitle = new TopAllBeanTitle(allList.size(), array[i], arrayVisible[i]);
            allList.add(beanTitle);
            // 如果可见，添加到显示列表
            if (arrayVisible[i]) {
                showList.add(new TopShowBeanTitle(showList.size(), beanTitle.getIndex(), beanTitle.getText()));
            }
        }
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
        this.onItemClickListener = onItemClickListener;
        updateView();
    }

    /**
     * 设置监听器
     * ══════════════════════════════════════════════════════════════════════════════
     * @param onCheckChangedTitleListener 选中变化监听器
     * @param onItemClickListener         点击监听器
     */
    public void setListener(OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 更新指定项的文本
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 显示列表索引
     * @param text  新文本
     */
    public void updateItemText(int index, String text) {
        ((RadioButton) radioGroup.getChildAt(index)).setText(text);
        showList.get(index).setText(text);
        allList.get(showList.get(index).getIndexAll()).setText(text);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 配置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置背景资源
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemBgResIdCheck       选中背景
     * @param itemBgResIdUnCheck     未选中背景
     * @param itemBgResIdUnCheckUnLine 未选中下划线背景
     */
    public void setBg(int itemBgResIdCheck, int itemBgResIdUnCheck, int itemBgResIdUnCheckUnLine) {
        this.itemBgResIdCheck = itemBgResIdCheck;
        this.itemBgResIdUnCheck = itemBgResIdUnCheck;
        this.itemBgResIdUnCheckUnLine = itemBgResIdUnCheckUnLine;
    }

    /**
     * 设置标题模式标志
     * ══════════════════════════════════════════════════════════════════════════════
     * @param flag 标志值（FIRST_TITLE或TWO_TITLE）
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * 设置文本字号
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemTextSize 字号（像素）
     */
    public void setItemTextSize(int itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取RadioGroup
     * ══════════════════════════════════════════════════════════════════════════════
     * @return RadioGroup实例
     */
    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    /**
     * 获取当前选中的RadioButton
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中的RadioButton
     */
    public RadioButton getCheckedRadioButton() {
        return checkedRadioButton;
    }

    /**
     * 获取选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中的TopAllBeanTitle，无选中项返回null
     */
    public TopAllBeanTitle getSelected() {
        int num = addView ? radioGroup.getChildCount() - 1 : radioGroup.getChildCount();
        for (int i = 0; i < num; i++) {
            if (radioGroup.getCheckedRadioButtonId() == radioGroup.getChildAt(i).getId()) {
                return allList.get(showList.get(i).getIndexAll());
            }
        }
        return null;
    }

    /**
     * 获取全部项列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 全部项列表
     */
    public ArrayList<TopAllBeanTitle> getAllList() {
        return allList;
    }

    /**
     * 获取显示项列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 显示项列表
     */
    public ArrayList<TopShowBeanTitle> getShowList() {
        return showList;
    }

    /**
     * 获取指定索引的项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 全部列表索引
     * @return 对应的TopAllBeanTitle
     */
    public TopAllBeanTitle getItem(int index) {
        return allList.get(index);
    }

    /**
     * 设置选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 全部列表索引
     */
    public void setSelected(int index) {
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getIndexAll() == index) {
                radioGroup.check(radioGroup.getChildAt(i).getId());
                checkedChangeListener.onCheckedChanged(radioGroup, radioGroup.getChildAt(i).getId());
                checkedRadioButton = (RadioButton) radioGroup.getChildAt(i);
                break;
            }
        }
    }

    /**
     * 设置指定项的启用状态
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index  全部列表索引
     * @param enable 是否启用
     */
    public void setEnable(int index, boolean enable) {
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getIndexAll() == index) {
                radioGroup.getChildAt(i).setEnabled(enable);
                break;
            }
        }
    }

    /**
     * 获取根视图
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 根视图
     */
    public View getInflate() {
        return inflate;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化视图
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    private void initView(Context context) {
        inflate = View.inflate(context, R.layout.view_toptitleview, null);
        itemWidth = ItemWidth;
        itemTextSize = 20;
        itemTextColorSelect = R.color.textColorNewTopTitleSelect;
        itemTextColorUnSelect = R.color.textColorNewTopTitleUnSelect;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视图更新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新视图
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   创建RadioButton并添加到RadioGroup
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void updateView() {
        radioGroup = (RadioGroup) inflate.findViewById(R.id.topTitleRadioGroup);
        // 设置背景
        if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
            radioGroup.setBackgroundResource(R.drawable.bg_topviewtitle_item_unselect);
        } else {
            radioGroup.setBackgroundColor(context.getResources().getColor(R.color.color_backcolor_mainMenu_title));
        }
        radioGroup.removeAllViews();
        // 创建RadioButton
        for (int i = 0; i < showList.size(); i++) {
            final MRadioButton radioButton = new MRadioButton(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            radioButton.setLayoutParams(layoutParams);
            // 隐藏原生按钮图标
            radioButton.setButtonDrawable(context.getResources().getDrawable(android.R.color.transparent));
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setBackground(null);
            radioButton.setText(showList.get(i).getText());
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
            // 设置文字颜色
            if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
                radioButton.setTextColor(context.getResources().getColorStateList(itemTextColorUnSelect));
            } else {
                radioButton.setTextColor(context.getResources().getColorStateList(i == 0 ? itemTextColorSelect : itemTextColorUnSelect));
            }
            radioButton.setLineSpacing(0, 0.9f);
            // 设置点击监听
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(v);
                    }
                    checkedChangeListener.onCheckedChanged(radioGroup, radioButton.getId());
                }
            });
            radioGroup.addView(radioButton);
            // 默认选中第一项
            if (i == 0) {
                radioGroup.check(radioButton.getId());
                checkedRadioButton = radioButton;
            }
        }
        // 如果总宽度不足，添加填充视图
        if (itemWidth * showList.size() < WIDTH_SHOW) {
            addView = true;
            RadioButton radioButton = new RadioButton(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WIDTH_SHOW - itemWidth * showList.size(), ViewGroup.LayoutParams.MATCH_PARENT);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setButtonDrawable(context.getResources().getDrawable(android.R.color.transparent));
            radioButton.setEnabled(false);
            radioGroup.addView(radioButton);
        } else {
            addView = false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 选中变化监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * RadioGroup选中变化监听器
     */
    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int count = addView ? group.getChildCount() - 1 : group.getChildCount();
            for (int i = 0; i < count; i++) {
                RadioButton radioButton2 = (RadioButton) group.getChildAt(i);
                ScreenUtil.getViewLocation(radioButton2);
                if (checkedId == group.getChildAt(i).getId()) {
                    // 选中项
                    checkedRadioButton = radioButton2;
                    if (flag != TopViewTitleWithScroll.FIRST_TITLE) {
                        radioButton2.setTextColor(context.getResources().getColor(itemTextColorSelect));
                    }
                    // 回调选中变化监听器
                    if (onCheckChangedTitleListener != null) {
                        onCheckChangedTitleListener.checkChanged(inflate, allList.get(showList.get(i).getIndexAll()));
                    }
                } else {
                    // 未选中项
                    if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
                        radioButton2.setCompoundDrawables(null, null, null, null);
                    } else {
                        radioButton2.setTextColor(context.getResources().getColor(itemTextColorUnSelect));
                    }
                }
            }
        }
    };
}
