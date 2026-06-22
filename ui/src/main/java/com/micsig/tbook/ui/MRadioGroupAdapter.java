package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.view.LayoutInflater;  // 布局填充器
import android.view.View;  // Android视图基类
import android.view.ViewGroup;  // 视图组容器
import android.widget.RadioButton;  // 单选按钮控件
import android.widget.TextView;  // 文本视图控件

import androidx.annotation.NonNull;  // 非空注解
import androidx.constraintlayout.widget.ConstraintLayout;  // 约束布局
import androidx.recyclerview.widget.RecyclerView;  // RecyclerView列表控件

import com.micsig.tbook.ui.bean.RadioButtonBean;  // 单选按钮数据Bean类

import java.util.List;  // List接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           MRadioGroupAdapter                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - MRadioGroup的RecyclerView适配器                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承RecyclerView.Adapter，为MRadioGroup提供列表项视图                     │
 * │   2. 管理RadioButtonBean数据列表的展示                                         │
 * │   3. 处理单选项的点击事件和状态更新                                             │
 * │   4. 支持动态设置项尺寸和可见性                                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────────┐                                                   │
 * │   │ RecyclerView.Adapter│  ← 继承自RecyclerView适配器基类                     │
 * │   └──────────┬──────────┘                                                   │
 * │              ↓                                                               │
 * │   ┌─────────────────────┐      ┌───────────────────┐                        │
 * │   │ MRadioGroupAdapter  │ ───→ │   RadioButtonBean │  数据模型               │
 * │   └──────────┬──────────┘      └───────────────────┘                        │
 * │              ↓                                                               │
 * │   ┌─────────────────────┐                                                   │
 * │   │        Holder       │  内部ViewHolder类                                  │
 * │   └─────────────────────┘                                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   List<RadioButtonBean> → onCreateViewHolder → onBindViewHolder → 视图展示    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - RecyclerView.Adapter: 基类，提供适配器框架                                 │
 * │   - RadioButtonBean: 数据模型，存储单选项信息                                   │
 * │   - Context: Android上下文，用于资源访问                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   List<RadioButtonBean> list = new ArrayList<>();                           │
 * │   MRadioGroupAdapter adapter = new MRadioGroupAdapter(context, 120, 60, list);│
 * │   recyclerView.setAdapter(adapter);                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author Liwb                                                                 │
 * │ @date 2024-2-27 11:12                                                       │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MRadioGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    // =========================== 成员变量定义 ===========================
    
    /** Android上下文环境 */  // 用于布局填充和资源访问
    private Context context;
    
    /** 单选按钮数据列表 */  // 存储所有RadioButtonBean对象
    private List<RadioButtonBean> list;
    
    /** 单选项尺寸 */  // itemWidth: 单选项宽度(像素)
    private int itemWidth, itemHeight;  // itemHeight: 单选项高度(像素)

    // =========================== 构造方法 ===========================
    
    /**
     * 构造方法
     * 
     * @param context Android上下文环境
     * @param itemWidth 单选项宽度(像素)
     * @param itemHeight 单选项高度(像素)
     * @param list 单选按钮数据列表
     */
    public MRadioGroupAdapter(Context context, int itemWidth, int itemHeight, List<RadioButtonBean> list) {
        this.context = context;  // 保存上下文引用
        this.itemWidth = itemWidth;  // 保存单选项宽度
        this.itemHeight = itemHeight;  // 保存单选项高度
        this.list = list;  // 保存数据列表引用
    }

    // =========================== 数据管理方法 ===========================
    
    /**
     * 设置数据列表
     * 用于动态更新适配器数据
     * 
     * @param list 新的数据列表
     */
    public void setList(List<RadioButtonBean> list) {
        this.list = list;  // 更新数据列表引用
    }

    // =========================== Adapter生命周期方法 ===========================
    
    /**
     * 创建ViewHolder
     * 加载列表项布局并创建ViewHolder实例
     * 
     * @param parent 父容器
     * @param viewType 视图类型（本适配器不使用多类型）
     * @return 新创建的ViewHolder实例
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_radiobutton, parent, false);  // 加载列表项布局

        return new Holder(view);  // 创建并返回Holder实例
    }

    /**
     * 绑定数据到ViewHolder
     * 将RadioButtonBean数据绑定到列表项视图
     * 
     * @param holder 要绑定的ViewHolder
     * @param position 列表项位置
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));  // 调用Holder的bind方法绑定数据
    }

    /**
     * 获取列表项数量
     * 
     * @return 数据列表的大小
     */
    @Override
    public int getItemCount() {
        return list.size();  // 返回列表大小
    }

    // =========================== ViewHolder内部类 ===========================
    
    /**
     * ViewHolder内部类
     * 持有列表项视图的引用，负责数据绑定和视图更新
     */
    class Holder extends RecyclerView.ViewHolder {
        
        /** 单选按钮控件 */  // 显示选项文本和选中状态
        RadioButton textView;
        
        /** 根布局容器 */  // 用于设置边距和可见性
        ConstraintLayout layout;
        
        /**
         * 构造方法
         * 初始化视图引用并设置尺寸
         * 
         * @param itemView 列表项视图
         */
        public Holder(@NonNull View itemView) {
            super(itemView);  // 调用父类构造
            layout = itemView.findViewById(R.id.layout);  // 获取根布局引用
            textView = itemView.findViewById(R.id.textView);  // 获取RadioButton引用
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();  // 获取RadioButton布局参数
            layoutParams.width = itemWidth;  // 设置宽度
            layoutParams.height = itemHeight;  // 设置高度
            textView.setLayoutParams(layoutParams);  // 应用布局参数
        }

        /**
         * 设置列表项的可见性
         * 根据数据项的可见性状态更新视图
         * 
         * @param item RadioButtonBean数据项
         */
        public void setVisible(RadioButtonBean item) {
            boolean isVisible = item.getVisible() == View.VISIBLE;  // 判断是否可见
            if (isVisible) {  // 如果可见
                layout.setPadding(item.getItemLeftMargin(), item.getItemTopMargin(), item.getItemRightMargin(), item.getItemBottomMargin());  // 设置边距
                layout.setVisibility(View.VISIBLE);  // 设置为可见
            } else {  // 如果不可见
                layout.setVisibility(View.GONE);  // 设置为GONE
            }
        }

        /**
         * 绑定数据到视图
         * 将RadioButtonBean数据绑定到列表项视图
         * 
         * @param item 要绑定的RadioButtonBean数据项
         */
        public void bind(RadioButtonBean item) {
            if (item.getIndex() == -1) {  // 检查是否为占位项（index为-1）
                itemView.setVisibility(View.INVISIBLE);  // 设置为不可见
                return;  // 直接返回，不进行后续绑定
            }
            setVisible(item);  // 设置可见性

            if (item.isEnableBeforeColor()) {  // 如果启用了前置颜色
                textView.setTextColor(item.getBeforeColor());  // 设置文本颜色
            }
            // textView.setBackgroundResource(item.getResIdBackGround());  // 设置背景资源（已注释）
            textView.setText(item.getText());  // 设置显示文本
            textView.setChecked(item.isCheck());  // 设置选中状态
            textView.setEnabled(item.isEnable());  // 设置启用状态
            textView.setOnClickListener((v) -> {  // 设置点击监听器
                if (item.getOnClick() != null) {  // 检查回调是否为空
                    item.getOnClick().accept(itemView, item);  // 触发点击回调
                }
            });
        }
    }
}
