package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           RightViewSelect                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 右侧滑菜单选择视图组件                                              ║
 * ║ 核心职责: 提供可配置的选择列表视图，支持垂直和网格布局                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌─────────────────┐                                                       ║
 * ║   │  RightViewSelect │ ──继承──▶ RelativeLayout                             ║
 * ║   └────────┬────────┘                                                       ║
 * ║            │ 组合                                                            ║
 * ║            ▼                                                                ║
 * ║   ┌─────────────────┐     ┌──────────────────┐                             ║
 * ║   │   RecyclerView  │────▶│ RightAdapterSelect│                             ║
 * ║   └─────────────────┘     └──────────────────┘                             ║
 * ║            │                     │                                          ║
 * ║            ▼                     ▼                                          ║
 * ║   ┌─────────────────┐     ┌──────────────────┐                             ║
 * ║   │LayoutManager    │     │ RightBeanSelect  │                             ║
 * ║   │(Linear/Grid)    │     │ (数据项模型)      │                             ║
 * ║   └─────────────────┘     └──────────────────┘                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   XML属性 ──▶ initView() ──▶ updateView() ──▶ Adapter ──▶ RecyclerView显示  ║
 * ║   用户点击 ──▶ Adapter回调 ──▶ onAdapterListener ──▶ OnItemClickListener     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - RightAdapterSelect: RecyclerView适配器                                  ║
 * ║   - RightBeanSelect: 数据项模型                                             ║
 * ║   - RecyclerView: 列表容器                                                  ║
 * ║   - GridLayoutManager/LinearLayoutManager: 布局管理器                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   XML:                                                                       ║
 * ║   <com.micsig.tbook.ui.rightslipmenu.RightViewSelect                        ║
 * ║       app:array="@array/timebase_options"                                   ║
 * ║       app:arrayOrientation="2"                                              ║
 * ║       app:itemWidth="120dp"                                                 ║
 * ║       app:itemHeight="60dp" />                                              ║
 * ║                                                                              ║
 * ║   Java:                                                                      ║
 * ║   RightViewSelect view = findViewById(R.id.select_view);                    ║
 * ║   view.setOnItemClickListener(listener);                                     ║
 * ║   view.setSelectIndex(0);                                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 注意事项:                                                                    ║
 * ║   1. 支持多种布局模式：垂直列表、2列网格、3列网格、4列网格                     ║
 * ║   2. 支持通道颜色主题，根据通道索引自动切换颜色                               ║
 * ║   3. 支持半圆角背景样式，首尾项显示特殊背景                                   ║
 * ║   4. doubleCheckEqual控制是否允许重复选择同一项                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class RightViewSelect extends RelativeLayout {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义 - 布局方向
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "RightViewSelect";

    /** 垂直列表布局 - 每行一个项目 */
    public static final int ARRAYORIENTATION_VERTICAL = 1;

    /** 水平网格布局 - 每行2个项目 */
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW2 = 2;

    /** 水平网格布局 - 每行3个项目 */
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW3 = 3;

    /** 水平网格布局 - 每行4个项目 */
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW4 = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义 - 项目尺寸规格
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 尺寸规格：74x39像素 */
    public static final int EACH_74_39 = 1;

    /** 尺寸规格：74x39像素（变体2） */
    public static final int EACH_74_39_2 = 2;

    /** 尺寸规格：74x34像素 */
    public static final int EACH_74_34 = 3;

    /** 尺寸规格：120x60像素 */
    public static final int EACH_120_60 = 4;

    /** 尺寸规格：108x54像素 */
    public static final int EACH_108_54 = 5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 上下文与配置
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 通道索引，用于颜色主题 */
    private int chIdx=-1;

    /** 选项文本数组 */
    private CharSequence[] array;

    /** 列表左侧内边距 */
    private int listPadLeft;

    /** 文本最大行数 */
    private int textLines = 1;

    /** 项目宽度 */
    private int itemWidth;

    /** 项目高度 */
    private int itemHeight;

    /** 项目间距 */
    private int itemMargin;

    /** 布局方向 */
    private int arrayOrientation;

    /** 是否启用首尾半圆角背景 */
    private boolean itemStartAndEndBgHalfCorner;

    /** 半圆角尺寸规格 */
    private int itemHalfCornerEach;

    /** 首次显示数量（用于网格布局占位） */
    private int firstShowCount;

    /** 首行空白占位数量 */
    private int firstNoneCount;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - UI组件与数据
    // ═══════════════════════════════════════════════════════════════════════════════

    /** RecyclerView列表视图 */
    private RecyclerView listView;

    /** 列表适配器 */
    private RightAdapterSelect adapter;

    /** 预设选中项文本 */
    private String preString;

    /** 数据项列表 */
    private ArrayList<RightBeanSelect> list;

    /** 项目点击监听器 */
    private OnItemClickListener onItemClickListener;

    /** 项目点击监听器（扩展版） */
    private OnItemClick2Listener onItemClick2Listener;

    /**
     * 前后两次设置是否可以相同，默认不可以相同
     * true: 允许重复选择同一项
     * false: 不允许重复选择同一项
     */
    private boolean doubleCheckEqual = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义 - OnItemClickListener
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 项目点击监听接口
     * 提供点击事件回调和UI刷新控制
     */
    public interface OnItemClickListener {
        /**
         * 项目点击回调
         * ══════════════════════════════════════════════════════════════════════════
         * @param viewId 视图ID
         * @param item   被点击的数据项
         */
        void onItemClick(int viewId, RightBeanSelect item);

        /**
         * 播放点击声音
         * ══════════════════════════════════════════════════════════════════════════
         * @param isCheckedSuccess 是否选择成功：当前如果已经选择了，再次选择，则为失败（false）
         */
        void onClickSound(boolean isCheckedSuccess);

        /**
         * 点击之后刷新UI
         * ══════════════════════════════════════════════════════════════════════════
         * @param viewId           视图ID
         * @param isCurClickForce  此次点击是否生效，即是否执行了onItemClick()
         */
        void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce);

        /**
         * 点击之前刷新UI
         * ══════════════════════════════════════════════════════════════════════════
         * @param viewId 视图ID
         */
        void onItemClickBeforRefreshUI(int viewId);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义 - OnItemClick2Listener
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 项目点击监听接口（扩展版）
     * 提供额外的View参数
     */
    public interface OnItemClick2Listener {
        /**
         * 项目点击回调
         * ══════════════════════════════════════════════════════════════════════════
         * @param parentsViewId 父视图ID
         * @param itemView      被点击的Item视图
         * @param item          被点击的数据项
         */
        void onItemClick2(int parentsViewId, View itemView, RightBeanSelect item);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 监听器设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置项目点击监听器
     * ══════════════════════════════════════════════════════════════════════════════
     * @param onItemClickListener 点击监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置项目点击监听器（扩展版）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param onItemClick2Listener 点击监听器
     */
    public void setOnItemClick2Listener(OnItemClick2Listener onItemClick2Listener) {
        this.onItemClick2Listener = onItemClick2Listener;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 选中状态管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置预设选中项文本
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据文本内容设置选中项，支持用户自定义项匹配
     * ══════════════════════════════════════════════════════════════════════════════
     * @param preString 预设选中的文本
     */
    public void setPreString(String preString) {
        this.preString = preString;
        // 遍历查找匹配项或用户自定义项
        boolean flag = false;
        for (RightBeanSelect item : list) {
            // 检查文本是否匹配
            if (item.getText().equals(preString)) {
                flag = true;
                break;
            }
            // 检查是否为用户自定义项
            if (item.isUserDefine(context)) {
                flag = true;
                break;
            }
        }
        // 如果找到匹配项，更新选中状态
        if (flag) {
            boolean isCheck = false;
            for (RightBeanSelect item : list) {
                if (item.getText().equals(preString)) {
                    item.setCheck(true);
                    isCheck = true;
                } else {
                    item.setCheck(false);
                }
            }
            // 如果文本未匹配，尝试选中用户自定义项
            if (!isCheck) {
                for (RightBeanSelect item : list) {
                    if (item.isUserDefine(context)) {
                        item.setCheck(true);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置启用背景资源
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemBgViewResId 背景资源ID（当前未使用）
     */
    public void setEnabledBg(int itemBgViewResId) {
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置整体启用状态
     * ══════════════════════════════════════════════════════════════════════════════
     * @param enabled 是否启用
     */
    @Override
    public void setEnabled(boolean enabled) {
        // 遍历设置所有项的启用状态
        for (RightBeanSelect item : list) {
            item.setEnable(enabled);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置指定索引项的启用状态
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index  项索引
     * @param enable 是否启用
     * @return 本次设置是否有数据变化
     */
    public boolean setEnabled(int index, boolean enable) {
        boolean change = false;
        // 查找并更新指定索引项
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIndex() == index) {
                change = list.get(i).isEnable() != enable;
                list.get(i).setEnable(enable);
            }
        }
        adapter.notifyDataSetChanged();
        return change;
    }

    /**
     * 清除所有选中状态
     * ══════════════════════════════════════════════════════════════════════════════
     */
    public void clearSelect() {
        // 遍历清除所有项的选中状态
        for (RightBeanSelect item : list) {
            item.setCheck(false);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 获取当前选中项索引
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中项索引，无选中项时返回第一个有效项索引
     */
    public int getSelectIndex() {
        int define = -1;
        for (int i = 0; i < list.size(); i++) {
            // 记录第一个有效项索引
            if (list.get(i).getIndex() != -1 && define == -1) {
                define = i;
            }
            // 查找选中项
            if (list.get(i).isCheck()) {
                return i;
            }
        }
        return define;
    }

    /**
     * 设置选中项索引
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 要选中的项索引
     */
    public void setSelectIndex(int index) {
        // 遍历设置选中状态
        for (RightBeanSelect item : list) {
            if (item.getIndex() == index) {
                item.setCheck(true);
            } else {
                item.setCheck(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 根据文本设置选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param text 要选中的文本
     * @return 是否设置成功
     */
    public boolean setSelectText(String text) {
        if (TextUtils.isEmpty(text)) return false;
        // 检查文本是否存在
        boolean has = false;
        for (RightBeanSelect item : list) {
            if (item.getText().equals(text)) {
                has = true;
            }
        }
        // 如果存在，设置选中状态
        if (has) {
            for (RightBeanSelect item : list) {
                if (item.getText().equals(text)) {
                    item.setCheck(true);
                } else {
                    item.setCheck(false);
                }
            }
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    /**
     * 获取选项总数
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选项总数
     */
    public int getSelectCount() {
        return list.size();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法（单参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    public RightViewSelect(Context context) {
        this(context, null);
    }

    /**
     * 构造方法（双参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     * @param attrs   属性集
     */
    public RightViewSelect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法（三参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context      上下文对象
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightViewSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取数据列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 数据项列表
     */
    public ArrayList<RightBeanSelect> getList() {
        return list;
    }

    /**
     * 获取当前选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中的数据项
     */
    public RightBeanSelect getSelectItem() {
        return list.get(getSelectIndex());
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化视图
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   解析XML属性，初始化视图配置
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context      上下文对象
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        // 加载布局文件
        View.inflate(context, R.layout.view_rightslipselect, this);
        // 获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightSlipViewSelect);
        array = ta.getTextArray(R.styleable.RightSlipViewSelect_array);
        listPadLeft = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_listPadLeft, 5);
        itemWidth = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemWidth, 120);
        itemHeight = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemHeight, 60);
        itemMargin = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemMargin, 0);
        arrayOrientation = ta.getInt(R.styleable.RightSlipViewSelect_arrayOrientation, ARRAYORIENTATION_VERTICAL);
        itemStartAndEndBgHalfCorner = ta.getBoolean(R.styleable.RightSlipViewSelect_itemStartAndEndBgHalfCorner, false);
        itemHalfCornerEach = ta.getInt(R.styleable.RightSlipViewSelect_itemHalfCornerEach, EACH_74_39);
        firstShowCount = ta.getInt(R.styleable.RightSlipViewSelect_firstShowCount, 0);
        firstNoneCount = 0;
        textLines = ta.getInt(R.styleable.RightSlipViewSelect_itemTextLines, 1);
        ta.recycle();
        // 如果有数据，更新视图
        if (array != null && array.length > 0) {
            updateView();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 颜色主题设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据通道索引设置控件颜色
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   设置通道索引并重新创建视图，应用对应的颜色主题
     * ══════════════════════════════════════════════════════════════════════════════
     * @param chIdx 通道索引（0-3为普通通道，4为Math，5-8为Ref，9-10为S1/S2）
     */
    public void setControlColorByChIdx(int chIdx){
        this.chIdx=chIdx;
        updateView();
    }

    /**
     * 仅更新控件颜色（不重建视图）
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   仅更新适配器的颜色主题，刷新当前选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param chIdx 通道索引
     */
    public void setOnlyControlColorByChIdx(int chIdx){
        adapter.setControlColorByCh(chIdx);
        adapter.notifyItemChanged(adapter.getSelectPosition());
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置选项数组
     * ══════════════════════════════════════════════════════════════════════════════
     * @param array 选项文本数组
     */
    public void setArray(CharSequence[] array) {
        if (array != null && array.length > 0) {
            this.array = array;
            updateView();
        }
    }

    /**
     * 设置是否允许重复选择
     * ══════════════════════════════════════════════════════════════════════════════
     * @param doubleCheckEqual true允许重复选择同一项，false不允许
     */
    public void setDoubleCheckEqual(boolean doubleCheckEqual) {
        this.doubleCheckEqual = doubleCheckEqual;
    }

    /** 项目顶部间距 */
    int itemMarginTop = 5;

    /**
     * 设置项目顶部间距
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemMarginTop 顶部间距值
     */
    public void setItemMarginTop(int itemMarginTop) {
        this.itemMarginTop = itemMarginTop;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视图更新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新视图
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   创建布局管理器、数据列表和适配器，绑定到RecyclerView
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void updateView() {
        listView = (RecyclerView) findViewById(R.id.listView);
        // 计算首行空白占位数量
        if (firstShowCount != 0) {
            switch (arrayOrientation) {
                case ARRAYORIENTATION_VERTICAL:
                    firstNoneCount = 0;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW2:
                    firstNoneCount = 2 - firstShowCount;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW3:
                    firstNoneCount = 3 - firstShowCount;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW4:
                    firstNoneCount = 4 - firstShowCount;
                    break;
                default:
                    firstNoneCount = 0;
                    break;
            }
        } else {
            firstNoneCount = 0;
        }

        // 设置布局管理器
        listView.setLayoutManager(getLayoutManager());
        // 创建数据列表
        list = new ArrayList<RightBeanSelect>();
        // 添加空白占位项
        for (int i = 0; i < firstNoneCount; i++) {
            RightBeanSelect beanSelect = new RightBeanSelect(-1, "", false);
            list.add(beanSelect);
        }
        // 添加实际数据项
        for (int i = 0; i < array.length; i++) {
            if (StrUtil.isEmpty(array[i].toString())==false) {
                // 默认选中第一项
                RightBeanSelect beanSelect = new RightBeanSelect(i, array[i].toString(), i == 0);
                list.add(beanSelect);
            }
        }
        // 创建并配置适配器
        adapter = new RightAdapterSelect(context, itemWidth, itemHeight, list, onAdapterListener);
        adapter.setItemMargin(itemMargin);
        adapter.setTextLines(textLines);
        // 设置通道颜色主题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter.setControlColorByCh(chIdx);
        }
        // 设置半圆角背景
        adapter.setItemStartAndEndBgHalfCorner(itemStartAndEndBgHalfCorner, itemHalfCornerEach);
        listView.setAdapter(adapter);
    }

    /**
     * 获取布局管理器
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据布局方向创建对应的LayoutManager
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 布局管理器实例
     */
    private RecyclerView.LayoutManager getLayoutManager() {
        GridLayoutManager gridLayoutManager;
        switch (arrayOrientation) {
            case ARRAYORIENTATION_VERTICAL:
                // 垂直线性布局
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            case ARRAYORIENTATION_HORIZONTALEACHROW2:
                // 2列网格布局
                return new GridLayoutManager(context, 2);
            case ARRAYORIENTATION_HORIZONTALEACHROW3:
                // 3列网格布局
                return new GridLayoutManager(context, 3);
            case ARRAYORIENTATION_HORIZONTALEACHROW4:
                // 4列网格布局
                return new GridLayoutManager(context, 4);
            default:
                // 默认垂直线性布局
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 适配器回调
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 适配器点击监听器
     * 处理项目点击事件，更新选中状态并回调外部监听器
     */
    private RightAdapterSelect.OnItemClickListener onAdapterListener = new RightAdapterSelect.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, RightBeanSelect item) {
            boolean checkEqual = false;// 前后两次设置是否相同
            // 获取点击位置
            ScreenUtil.getViewLocation(itemView);
            // 更新选中状态
            for (RightBeanSelect bean : list) {
                if (bean.getIndex() == item.getIndex()) {
                    if (!bean.isCheck()) {
                        bean.setCheck(true);
                    } else {
                        checkEqual = true;
                    }
                } else {
                    if (bean.isCheck()) {
                        bean.setCheck(false);
                    }
                }
            }
            // 回调点击前刷新
            if (onItemClickListener != null) {
                onItemClickListener.onClickSound(checkEqual || doubleCheckEqual);
                onItemClickListener.onItemClickBeforRefreshUI(RightViewSelect.this.getId());
            }
            // 判断点击是否生效
            boolean isCurClickForce;
            if (!checkEqual || doubleCheckEqual) {
                isCurClickForce = true;
                adapter.notifyDataSetChanged();
                // 回调点击事件
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(RightViewSelect.this.getId(), item);
                }
                if (onItemClick2Listener != null) {
                    onItemClick2Listener.onItemClick2(RightViewSelect.this.getId(), itemView, item);
                }
            } else {
                isCurClickForce = false;
            }
            // 回调点击后刷新
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickAfterRefreshUI(RightViewSelect.this.getId(), isCurClickForce);
            }
        }
    };
}
