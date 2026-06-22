package com.micsig.tbook.ui.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.BitmapUtil;

import java.util.ArrayList;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          RightAdapterSelect                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: 右侧滑菜单选择列表适配器                                            ║
 * ║ 核心职责: 为RightViewSelect提供RecyclerView适配器，管理选项项的显示和交互      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 架构设计:                                                                    ║
 * ║   ┌────────────────────┐                                                    ║
 * ║   │  RightAdapterSelect │ ──继承──▶ RecyclerView.Adapter                    ║
 * ║   └─────────┬──────────┘                                                    ║
 * ║             │ 内部类                                                         ║
 * ║             ▼                                                               ║
 * ║   ┌────────────────────┐                                                    ║
 * ║   │       Holder       │ ──继承──▶ RecyclerView.ViewHolder                  ║
 * ║   │   (RadioButton)    │                                                    ║
 * ║   └────────────────────┘                                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 数据流向:                                                                    ║
 * ║   RightBeanSelect列表 ──▶ onBindViewHolder() ──▶ Holder.bind() ──▶ UI显示   ║
 * ║   用户点击 ──▶ RadioButton.onClick() ──▶ OnItemClickListener回调            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 依赖关系:                                                                    ║
 * ║   - RightBeanSelect: 数据项模型                                             ║
 * ║   - RightViewSelect: 父容器视图                                             ║
 * ║   - BitmapUtil: 位图工具类，用于生成动态背景                                 ║
 * ║   - RadioButton: 单选按钮控件                                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 使用示例:                                                                    ║
 * ║   ArrayList<RightBeanSelect> list = new ArrayList<>();                      ║
 * ║   list.add(new RightBeanSelect(0, "选项1", true));                          ║
 * ║   RightAdapterSelect adapter = new RightAdapterSelect(                      ║
 * ║       context, 120, 60, list, listener);                                    ║
 * ║   adapter.setControlColorByCh(0); // 设置通道1颜色                           ║
 * ║   recyclerView.setAdapter(adapter);                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 注意事项:                                                                    ║
 * ║   1. 使用RadioButton作为Item视图，支持选中状态显示                           ║
 * ║   2. 支持通道颜色主题，根据通道索引动态生成背景和文字颜色                     ║
 * ║   3. 支持半圆角背景样式，首尾项显示特殊背景                                  ║
 * ║   4. 需要API 23+支持动态颜色生成                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class RightAdapterSelect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 上下文与配置
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 项目宽度 */
    private int itemWidth, itemHeight;

    /** 通道索引，用于颜色主题 */
    private int chIdx=-1;

    /** 左半圆角背景资源ID */
    private int itemBgViewResIdLeft,itemBgViewResIdRight;

    /** 文字颜色状态列表 */
    private ColorStateList itemTextViewResId;

    /** 是否启用首尾半圆角背景 */
    private boolean itemStartAndEndBgHalfCorner;

    /** 半圆角尺寸规格 */
    private int itemHalfCornerEach;

    /** 数据项列表 */
    private ArrayList<RightBeanSelect> list;

    /** 点击监听器 */
    private OnItemClickListener onItemClickListener;

    /** 项目间距 */
    private int itemMargin = 0;

    /** 文本最大行数 */
    private int maxTextLines = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 项目点击监听接口
     */
    interface OnItemClickListener {
        /**
         * 项目点击回调
         * ══════════════════════════════════════════════════════════════════════════
         * @param itemView 被点击的Item视图
         * @param item     被点击的数据项
         */
        void onItemClick(View itemView, RightBeanSelect item);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 配置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置项目间距
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemMargin 间距值（像素）
     */
    public void setItemMargin(int itemMargin) {
        this.itemMargin = itemMargin;
    }

    /**
     * 设置文本最大行数
     * ══════════════════════════════════════════════════════════════════════════════
     * @param textLines 最大行数
     */
    public void setTextLines(int textLines) {
        this.maxTextLines = textLines;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造方法
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   初始化适配器，设置默认背景资源和文字颜色
     * ══════════════════════════════════════════════════════════════════════════════
     * @param context             上下文对象
     * @param itemWidth           项目宽度
     * @param itemHeight          项目高度
     * @param list                数据项列表
     * @param onItemClickListener 点击监听器
     */
    @SuppressLint("ResourceType")
    public RightAdapterSelect(Context context, int itemWidth, int itemHeight, ArrayList<RightBeanSelect> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.list = list;
        this.onItemClickListener = onItemClickListener;

        // 初始化半圆角背景资源
        itemBgViewResIdLeft= R.drawable.selector_semicircle_left;
        itemBgViewResIdRight= R.drawable.selector_semicircle_right;
        // 初始化文字颜色状态列表（API 23+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            itemTextViewResId=context.getColorStateList(R.drawable.selector_rightslip_select_item_textcolor);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 背景配置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置首尾半圆角背景
     * ══════════════════════════════════════════════════════════════════════════════
     * @param itemStartAndEndBgHalfCorner 是否启用半圆角背景
     * @param itemHalfCornerEach          尺寸规格
     */
    public void setItemStartAndEndBgHalfCorner(boolean itemStartAndEndBgHalfCorner, int itemHalfCornerEach) {
        this.itemStartAndEndBgHalfCorner = itemStartAndEndBgHalfCorner;
        this.itemHalfCornerEach = itemHalfCornerEach;
    }

    /**
     * 根据通道索引设置控件颜色
     * ══════════════════════════════════════════════════════════════════════════════
     * 功能说明:
     *   根据通道索引动态生成文字颜色状态列表
     * ══════════════════════════════════════════════════════════════════════════════
     * @param chIdx 通道索引（0-3为普通通道，4为Math，5-8为Ref，9-10为S1/S2）
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    public void setControlColorByCh(int chIdx){
        this.chIdx=chIdx;
        // 使用BitmapUtil生成动态颜色选择器
        itemTextViewResId=BitmapUtil.genSelectorColor(context,chIdx);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // RecyclerView.Adapter 方法实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 创建ViewHolder
     * ══════════════════════════════════════════════════════════════════════════════
     * @param parent   父容器
     * @param viewType 视图类型
     * @return ViewHolder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载Item布局
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_rightslip_select, parent, false));
    }

    /**
     * 绑定ViewHolder
     * ══════════════════════════════════════════════════════════════════════════════
     * @param holder   ViewHolder实例
     * @param position 位置索引
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));
    }

    /**
     * 获取选中项位置
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 选中项位置索引，无选中项返回-1
     */
    public int getSelectPosition() {
        int position = -1;
        // 遍历查找选中项
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isCheck()) {
                position = i;
                break;
            }
        }
        return position;
    }

    /**
     * 获取项目总数
     * ══════════════════════════════════════════════════════════════════════════════
     * @return 项目总数
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ViewHolder 内部类
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * ViewHolder内部类
     * 管理Item视图的显示和交互
     */
    class Holder extends RecyclerView.ViewHolder {
        /** RadioButton控件 */
        RadioButton textView;

        /**
         * 构造方法
         * ══════════════════════════════════════════════════════════════════════════
         * @param itemView Item视图
         */
        public Holder(View itemView) {
            super(itemView);
            // 获取RadioButton控件
            textView = (RadioButton) itemView.findViewById(R.id.textView);
            // 设置控件尺寸
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.width = itemWidth;
            layoutParams.height = itemHeight;
            textView.setLayoutParams(layoutParams);
            // 设置文本最大行数
            textView.setMaxLines(maxTextLines);
        }

        /**
         * 绑定数据到视图
         * ══════════════════════════════════════════════════════════════════════════
         * 功能说明:
         *   根据数据项设置视图的显示状态、背景和点击事件
         * ══════════════════════════════════════════════════════════════════════════
         * @param item 数据项
         */
        public void bind(final RightBeanSelect item) {
            // 空白占位项处理
            if (item.getIndex() == -1) {
                itemView.setVisibility(View.INVISIBLE);
                return;
            }
            // 显示实际项
            itemView.setVisibility(View.VISIBLE);
            // 设置文本和状态
            textView.setText(item.getText());
            textView.setChecked(item.isCheck());
            textView.setEnabled(item.isEnable());
            textView.setTextColor(itemTextViewResId);
            // 设置边距
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (itemStartAndEndBgHalfCorner) {
                // 半圆角模式下无边距
                layoutParams.setMargins(0, 0, 0, 0);
            } else {
                // 普通模式设置边距
                layoutParams.setMargins(itemMargin, itemMargin, itemMargin, itemMargin);
            }
            itemView.setLayoutParams(layoutParams);
            // 设置背景
            if (itemStartAndEndBgHalfCorner && list.size() >= 2 && (item.getIndex() == 0 || item.getIndex() == list.size() - 1)) {
                // 首尾项半圆角背景
                switch(itemHalfCornerEach) {
                    case RightViewSelect.EACH_74_39_2:
                        // 74x39像素变体2
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_39_2_left : R.drawable.bg_halfcorner_74_39_2_right);
                        break;
                    case RightViewSelect.EACH_74_34:
                        // 74x34像素
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_34_left : R.drawable.bg_halfcorner_74_34_right);
                        break;
                    case RightViewSelect.EACH_120_60:
                        // 120x60像素，使用动态生成的背景
                        Drawable itemBgLeft=BitmapUtil.genSelectorLeftDrawable(context,chIdx);
                        Drawable itemBgRight=BitmapUtil.genSelectorRightDrawable(context,chIdx);
                        textView.setBackground(item.getIndex()==0?itemBgLeft:itemBgRight);
                        break;
                    case RightViewSelect.EACH_108_54:
                        // 108x54像素，使用动态生成的背景
                        Drawable drawableLeft=BitmapUtil.genSelectorLeftDrawable(context,chIdx);
                        Drawable drawableRight=BitmapUtil.genSelectorRightDrawable(context,chIdx);
                        // 设置边界尺寸
                        drawableLeft.setBounds(0, 0, 108, 54);
                        drawableRight.setBounds(0, 0, 108, 54);
                        textView.setBackground(item.getIndex() == 0 ? drawableLeft : drawableRight);
                        break;
                    case RightViewSelect.EACH_74_39:
                    default:
                        // 74x39像素（默认）
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_39_left : R.drawable.bg_halfcorner_74_39_right);
                        break;
                }
            } else {
                // 普通项背景，使用动态生成的选择器背景
                StateListDrawable drawable=BitmapUtil.genSelectorDrawable(context,chIdx);
                textView.setBackground(drawable);
            }
            // 设置点击事件
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, item);
                    }
                }
            });
        }
    }
}
