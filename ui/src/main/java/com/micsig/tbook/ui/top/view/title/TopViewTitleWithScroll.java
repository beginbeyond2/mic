package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;

import java.util.ArrayList;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                       TopViewTitleWithScroll                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 顶部标题栏带滚动的组合视图                                          ║
 * ║ 核心职责: 提供可水平滚动的标题栏，支持左右指示器显示                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌───────────────────────────┐                                             ║
 * ║   │  TopViewTitleWithScroll   │ ──继承──▶ RelativeLayout                   ║
 * ║   └─────────────┬─────────────┘                                             ║
 * ║                 │ 组合                                                       ║
 * ║                 ▼                                                           ║
 * ║   ┌───────────────────────────┐     ┌──────────────────┐                   ║
 * ║   │    TopViewHorScroll       │     │   TopViewTitle   │                   ║
 * ║   │    (水平滚动容器)          │────▶│  (标题栏管理)     │                   ║
 * ║   └───────────────────────────┘     └──────────────────┘                   ║
 * ║                 │                                                           ║
 * ║                 ▼                                                           ║
 * ║   ┌───────────────────────────┐     ┌──────────────────┐                   ║
 * ║   │  selectTitleLeft/Right    │     │  vFlag (指示器)   │                   ║
 * ║   │  (左右指示器)              │     │                  │                   ║
 * ║   └───────────────────────────┘     └──────────────────┘                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   setData() ──▶ updateView() ──▶ TopViewTitle.setData()                    ║
 * ║   滚动事件 ──▶ onScrollChanged() ──▶ setListHead() ──▶ 更新左右指示器       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - TopViewTitle: 标题栏管理类                                              ║
 * ║   - TopViewHorScroll: 水平滚动视图                                          ║
 * ║   - TopAllBeanTitle/TopShowBeanTitle: 数据模型                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   XML:                                                                       ║
 * ║   <com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll                ║
 * ║       app:itemBgViewFlag="1"                                                ║
 * ║       app:itemTextSize="24sp" />                                            ║
 * ║                                                                              ║
 * ║   Java:                                                                      ║
 * ║   TopViewTitleWithScroll view = findViewById(R.id.titleView);               ║
 * ║   view.setData(R.array.titles, listener, clickListener);                    ║
 * ║   view.setSelected(0);                                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 注意事项:                                                                    ║
 * ║   1. 支持FIRST_TITLE和TWO_TITLE两种显示模式                                  ║
 * ║   2. 当选中项超出可视区域时，显示左右指示器                                   ║
 * ║   3. FIRST_TITLE模式下显示滑动指示器                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopViewTitleWithScroll extends RelativeLayout {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示区域总宽度 */
    public static final int WIDTH_SHOW = TopViewTitle.WIDTH_SHOW;

    /** 一级标题模式（带滑动指示器） */
    public static final int FIRST_TITLE = 1;

    /** 二级标题模式 */
    public static final int TWO_TITLE = 2;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 上下文与数据
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 根视图 */
    private View view;

    /** 标题文本数组 */
    private String[] array;

    /** 可见性数组 */
    private boolean[] arrayVisible;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - UI组件
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 左侧指示器（当选中项在左侧不可见时显示） */
    private TextView selectTitleLeft, selectTitleRight;

    /** 水平滚动视图 */
    private TopViewHorScroll scrollView;

    /** 标题栏管理类 */
    private TopViewTitle topViewTitle;

    /** 滑动指示器（FIRST_TITLE模式） */
    private View vFlag;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 选中变化监听器 */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener;

    /** 滑动停止监听器 */
    private IOnScrollViewSliderListener onScrollViewSliderListener;

    /** 点击监听器 */
    private View.OnClickListener onItemClickListener;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 配置项
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 标题模式标志 */
    private int flag = TWO_TITLE;

    /** 背景资源ID */
    private int itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine;

    /** 布局资源ID */
    private int layoutResId;

    /** 文本字号 */
    private int itemTextSize;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 滑动停止监听接口
     */
    public interface IOnScrollViewSliderListener {
        /**
         * 滑动停止回调
         */
        void onSliderStop();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 监听器设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置滑动停止监听器
     * ══════════════════════════════════════════════════════════════════════════════
     * @param onScrollViewSliderListener 滑动停止监听器
     */
    public void setOnScrollViewSliderListener(IOnScrollViewSliderListener onScrollViewSliderListener) {
        this.onScrollViewSliderListener = onScrollViewSliderListener;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法（单参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     */
    public TopViewTitleWithScroll(Context context) {
        this(context, null);
    }

    /**
     * 构造方法（双参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context 上下文对象
     * @param attrs   属性集
     */
    public TopViewTitleWithScroll(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法（三参数）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context      上下文对象
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewTitleWithScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数据（从资源ID加载）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param arrayResId                  文本数组资源ID
     * @param onCheckChangedTitleListener 选中变化监听器
     * @param onItemClickListener         点击监听器
     */
    public void setData(int arrayResId, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = context.getResources().getStringArray(arrayResId);
        arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = true;
        }
        setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);
    }

    /**
     * 设置数据（文本数组和可见性数组）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param array                       文本数组
     * @param arrayVisible                可见性数组
     * @param onCheckChangedTitleListener 选中变化监听器
     * @param onItemClickListener         点击监听器
     */
    public void setData(String[] array, boolean[] arrayVisible, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = array;
        this.arrayVisible = arrayVisible;
        this.onItemClickListener = onItemClickListener;
        updateView();
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
    }

    /**
     * 设置数据（从资源ID加载文本和可见性）
     * ══════════════════════════════════════════════════════════════════════════════
     * @param arrayResId                  文本数组资源ID
     * @param arraysVisibleResId          可见性数组资源ID
     * @param onCheckChangedTitleListener 选中变化监听器
     * @param onItemClickListener         点击监听器
     */
    public void setData(int arrayResId, int arraysVisibleResId, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = context.getResources().getStringArray(arrayResId);;
        String[] arraysVisible = context.getResources().getStringArray(arraysVisibleResId);
        arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = Boolean.parseBoolean(arraysVisible[i]);
        }
        this.onItemClickListener = onItemClickListener;
        updateView();
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
    }

    /**
     * 更新指定项的文本
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 全部列表索引
     * @param s     新文本
     */
    public void updateItemText(int index, String s) {
        if (selectTitleLeft.getVisibility() == VISIBLE && getItem(index).getText().equals(selectTitleLeft.getText().toString())) {
            selectTitleLeft.setText(s);
        }
        if (selectTitleRight.getVisibility() == VISIBLE && getItem(index).getText().equals(selectTitleRight.getText().toString())) {
            selectTitleRight.setText(s);
        }
        topViewTitle.updateItemText(index, s);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化视图
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context      上下文对象
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        view = View.inflate(context, R.layout.view_topviewtitlewithhead, this);
        View topViewTitleDivider = view.findViewById(R.id.topViewTitleDivider);
        // 解析自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewTitleWithScroll);
        flag = ta.getInt(R.styleable.TopViewTitleWithScroll_itemBgViewFlag, TWO_TITLE);
        if (flag == FIRST_TITLE) {
            // 一级标题模式
            itemBgResIdCheck = R.drawable.bg_topviewtitle_item_select;
            itemBgResIdUnCheck = R.drawable.bg_topviewtitle_item_unselect;
            itemBgResIdUnCheckUnLine = R.drawable.bg_topviewtitle_item_unselect;
            layoutResId = android.R.color.black;
            topViewTitleDivider.setVisibility(GONE);
        } else {
            // 二级标题模式
            itemBgResIdCheck = R.drawable.bg_topviewtitle_item_select;
            itemBgResIdUnCheck = R.drawable.bg_topviewtitle_item_unselect;
            itemBgResIdUnCheckUnLine = R.drawable.bg_topviewtitle_item_unselect;
            layoutResId = android.R.color.black;
            topViewTitleDivider.setVisibility(GONE);
        }
        itemTextSize = ta.getDimensionPixelSize(R.styleable.TopViewTitleWithScroll_itemTextSize, 24);
        ta.recycle();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视图更新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新视图
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void updateView() {
        selectTitleLeft = (TextView) findViewById(R.id.selectTitleLeft);
        selectTitleRight = (TextView) findViewById(R.id.selectTitleRight);
        RelativeLayout leftLayout = (RelativeLayout) findViewById(R.id.leftLayout);
        RelativeLayout rightLayout = (RelativeLayout) findViewById(R.id.rightLayout);
        // 设置指示器样式
        selectTitleLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
        selectTitleRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
        selectTitleLeft.setBackgroundResource(itemBgResIdCheck);
        selectTitleRight.setBackgroundResource(itemBgResIdCheck);
        leftLayout.setBackgroundResource(layoutResId);
        rightLayout.setBackgroundResource(layoutResId);
        selectTitleLeft.setOnClickListener(onClickListener);
        selectTitleRight.setOnClickListener(onClickListener);
        selectTitleLeft.setVisibility(GONE);
        selectTitleRight.setVisibility(GONE);
        // 创建标题栏
        scrollView = (TopViewHorScroll) findViewById(R.id.scrollView);
        topViewTitle = new TopViewTitle(context);
        topViewTitle.setBg(itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine);
        topViewTitle.setFlag(flag);
        topViewTitle.setItemTextSize(itemTextSize);
        topViewTitle.setData(array, arrayVisible, onCheckChangedListener, onItemClickListener);
        scrollView.removeAllViews();
        scrollView.addView(topViewTitle.getInflate());
        scrollView.setOnScrollListener(onScrollChangedListener);
        // 隐藏左右布局
        leftLayout.setVisibility(GONE);
        rightLayout.setVisibility(GONE);
        // 设置滑动指示器
        vFlag = findViewById(R.id.firstTitleFlag);
        if (flag == FIRST_TITLE) {
            vFlag.setVisibility(VISIBLE);
        } else {
            vFlag.setVisibility(GONE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定索引的项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 全部列表索引
     * @return TopAllBeanTitle
     */
    public TopAllBeanTitle getItem(int index) {
        return topViewTitle.getItem(index);
    }

    /**
     * 获取选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中的TopAllBeanTitle
     */
    public TopAllBeanTitle getSelected() {
        return topViewTitle.getSelected();
    }

    /**
     * 获取全部项列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 全部项列表
     */
    public ArrayList<TopAllBeanTitle> getAllList() {
        return topViewTitle.getAllList();
    }

    /**
     * 获取显示项列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 显示项列表
     */
    public ArrayList<TopShowBeanTitle> getShowList() {
        return topViewTitle.getShowList();
    }

    /**
     * 设置选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 全部列表索引
     */
    public void setSelected(int index) {
        topViewTitle.setListener(null, null);
        topViewTitle.setSelected(index);
        setRightViewVisible(index);
        setListHead();
        topViewTitle.setListener(onCheckChangedListener, onItemClickListener);
    }

    /**
     * 设置指定项的启用状态
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index  全部列表索引
     * @param enable 是否启用
     */
    public void setEnable(int index, boolean enable) {
        topViewTitle.setEnable(index, enable);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 滚动控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 仅移动滚动视图
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据选中项位置自动滚动，确保选中项可见
     * ══════════════════════════════════════════════════════════════════════════════
     * @param index 显示列表索引
     */
    public void moveOnlyScroll(int index) {
        int[] initXPosition = getInitXPosition();
        View view = topViewTitle.getRadioGroup().getChildAt(index);
        int[] childLoc = new int[2];
        view.getLocationOnScreen(childLoc);
        // 检查是否需要向左滚动
        if ((selectTitleLeft.getVisibility() != VISIBLE || index == 0) && childLoc[0] < 2) {
            scrollView.scrollTo(initXPosition[index], 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        } else if (selectTitleLeft.getVisibility() == VISIBLE && childLoc[0] < 2 + view.getWidth() && index != 0) {
            scrollView.scrollTo(initXPosition[index - 1], 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        }
        // 检查是否需要向右滚动
        else if ((selectTitleRight.getVisibility() != VISIBLE || index == topViewTitle.getRadioGroup().getChildCount() - 1)
                && childLoc[0] > ((WIDTH_SHOW + 2) - view.getWidth())) {
            scrollView.scrollTo((initXPosition[index] + view.getWidth() - (WIDTH_SHOW + 2)), 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        } else if (selectTitleRight.getVisibility() == VISIBLE && childLoc[0] > ((WIDTH_SHOW + 2) - view.getWidth() * 2)
                && index != topViewTitle.getRadioGroup().getChildCount() - 1) {
            scrollView.scrollTo((initXPosition[index] + view.getWidth() * 2 - (WIDTH_SHOW + 2)), 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        }
    }

    /**
     * 获取初始X位置数组
     * ══════════════════════════════════════════════════════════════════════════════
     * @return X位置数组
     */
    private int[] getInitXPosition() {
        int count = topViewTitle.getRadioGroup().getChildCount();
        int width = topViewTitle.getRadioGroup().getChildAt(0).getWidth();
        int[] initXPosition = new int[count];
        for (int i = 0; i < count; i++) {
            initXPosition[i] = width * i + 2;
        }
        return initXPosition;
    }

    /**
     * 设置缓存选中项
     * ══════════════════════════════════════════════════════════════════════════════
     * @param cacheSelect 缓存选中索引
     */
    public void setCacheSelect(final int cacheSelect) {
        setSelected(cacheSelect);
        setRightViewVisible(cacheSelect);
    }

    /**
     * 设置右侧指示器可见性
     * ══════════════════════════════════════════════════════════════════════════════
     * @param select 选中索引
     */
    private void setRightViewVisible(int select) {
        if (select >= 7) {
            selectTitleRight.setVisibility(VISIBLE);
            selectTitleRight.setText(topViewTitle.getSelected().getText());
            selectTitleLeft.setVisibility(GONE);
        } else {
            selectTitleRight.setVisibility(GONE);
            selectTitleLeft.setVisibility(GONE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 点击监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 指示器点击监听器
     */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int width = topViewTitle.getCheckedRadioButton().getWidth();
            if (v.getId() == R.id.selectTitleLeft) {
                if (selectTitleLeft.getVisibility() == VISIBLE) {
                    scrollView.smoothScrollTo(topViewTitle.getSelected().getIndex() * width, 0);
                }
            } else if (v.getId() == R.id.selectTitleRight) {
                if (selectTitleRight.getVisibility() == VISIBLE) {
                    scrollView.smoothScrollTo(topViewTitle.getSelected().getIndex() * width + width - WIDTH_SHOW, 0);
                }
            }
        }
    };

    // ═══════════════════════════════════════════════════════════════════════════════
    // 选中变化监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 标题选中变化监听器
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            setListHead();
            if (onCheckChangedTitleListener != null) {
                onCheckChangedTitleListener.checkChanged(TopViewTitleWithScroll.this, item);
            }
        }
    };

    // ═══════════════════════════════════════════════════════════════════════════════
    // 滚动监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 滚动监听器
     */
    private TopViewHorScroll.OnScrollListener onScrollChangedListener = new TopViewHorScroll.OnScrollListener() {
        @Override
        public void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy) {
            TopViewTitleWithScroll.this.onScrollChanged(scrollView, x, y, oldx, oldy);
            setListHead();
        }

        @Override
        public void onStop() {
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
        }
    };

    /**
     * 滚动变化处理
     * ══════════════════════════════════════════════════════════════════════════════
     * @param scrollView 滚动视图
     * @param x          当前X坐标
     * @param y          当前Y坐标
     * @param oldx       之前X坐标
     * @param oldy       之前Y坐标
     */
    public void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy) {
        int parentWidth = ((View) scrollView.getParent()).getWidth();
        int Width = scrollView.getChildAt(0).getWidth();
        int currX = (int) scrollView.getChildAt(0).getX() - (oldx - x);
        if (currX >= (parentWidth - Width) || currX <= 0) {
            scrollView.scrollBy(oldx - x, 0);
        }
    }

    /**
     * 设置列表头部指示器
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据选中项位置更新左右指示器的显示状态
     * ══════════════════════════════════════════════════════════════════════════════
     */
    private void setListHead() {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        RadioButton child = topViewTitle.getCheckedRadioButton();
        int[] childLoc = new int[2];
        int[] scrollLoc = new int[2];
        child.getLocationOnScreen(childLoc);// 相对于屏幕左上角的位置
        scrollView.getLocationOnScreen(scrollLoc);
        int childLocX = childLoc[0];
        int scrollLocX = scrollLoc[0];
        // 检查选中项是否在左侧不可见
        if (childLocX - scrollLocX < 0) {
            selectTitleLeft.setVisibility(VISIBLE);
            selectTitleLeft.setText(child.getText());
            selectTitleRight.setVisibility(GONE);
        }
        // 检查选中项是否在右侧不可见
        else if (childLocX + child.getWidth() - scrollLocX - scrollView.getWidth() > 0) {
            selectTitleRight.setVisibility(VISIBLE);
            selectTitleRight.setText(child.getText());
            selectTitleLeft.setVisibility(GONE);
        } else {
            selectTitleLeft.setVisibility(GONE);
            selectTitleRight.setVisibility(GONE);
        }
        // FIRST_TITLE模式下更新滑动指示器位置
        if (flag == FIRST_TITLE) {
            int select = 0;
            for (int i = 0; i < topViewTitle.getShowList().size(); i++) {
                if (topViewTitle.getShowList().get(i).getIndexAll() == topViewTitle.getSelected().getIndex()) {
                    select = topViewTitle.getShowList().get(i).getIndexShow();
                }
            }
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) vFlag.getLayoutParams();
            layoutParams.width=TopViewTitle.ItemWidth;
            layoutParams.setMargins(select * TopViewTitle.ItemWidth, 0, 0, 0);
            vFlag.setLayoutParams(layoutParams);
        }
    }
}
