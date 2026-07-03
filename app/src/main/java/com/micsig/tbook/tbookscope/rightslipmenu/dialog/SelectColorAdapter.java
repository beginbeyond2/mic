package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.ArrayList;

/*
 * +=============================================================================+
 * |                       SelectColorAdapter                                     |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 通道颜色选择对话框的网格适配器                            |
 * | 核心职责 : 将颜色字符串列表绑定到 4x4 网格的颜色按钮上，每个按钮显示对应颜色          |
 * | 架构设计 : 标准 RecyclerView.Adapter 模式，内部 Holder 持有一个 Button 作为色块     |
 * | 数据流向 : DialogSelectColor 创建颜色列表 → Adapter 绑定颜色到按钮 →               |
 * |           用户点击 → OnItemClickListener 回传颜色值                              |
 * | 依赖关系 : DialogSelectColor（宿主对话框）、R.layout.item_select_color（色块布局）   |
 * | 使用场景 : 被 DialogSelectColor 使用，渲染4x4颜色选择网格                         |
 * +=============================================================================+
 */
public class SelectColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;                                              // 上下文引用
    private final ArrayList<String> colorList;                                  // 颜色字符串列表
    private final OnItemClickListener onItemClickListener;                      // 颜色项点击监听器

    /**
     * 颜色项点击监听接口
     */
    interface OnItemClickListener {
        /** 颜色项点击回调
         * @param itemView 被点击的视图
         * @param item     选中的颜色字符串
         */
        void onItemClick(View itemView, String item);
    }

    /**
     * 构造方法
     * @param context              上下文
     * @param list                 颜色字符串列表
     * @param onItemClickListener  颜色项点击监听器
     */
    public SelectColorAdapter(Context context, ArrayList<String> list, OnItemClickListener onItemClickListener) {
        this.context = context;                                                 // 保存上下文引用
        this.colorList = list;                                                  // 保存颜色列表引用
        this.onItemClickListener = onItemClickListener;                         // 保存点击监听器
    }

    /**
     * 创建颜色项 ViewHolder
     * @param parent   父视图组
     * @param viewType 视图类型
     * @return 新创建的 Holder 实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_select_color, parent, false)); // 加载色块布局并创建 Holder
    }

    /**
     * 绑定数据到 ViewHolder
     * @param holder   ViewHolder 实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(position);                                       // 委托给 Holder.bind() 绑定颜色
    }

    /**
     * 获取颜色项总数
     * @return 颜色列表大小
     */
    @Override
    public int getItemCount() {
        return colorList.size();                                                // 返回颜色项数量
    }

    /**
     * 颜色项 ViewHolder，负责单个颜色按钮的视图绑定与点击交互
     */
    class Holder extends RecyclerView.ViewHolder {
        Button colorBtn;                                                        // 颜色按钮

        /**
         * 构造方法，初始化颜色按钮
         * @param itemView 列表项根视图
         */
        public Holder(View itemView) {
            super(itemView);                                                     // 调用父类构造
            colorBtn = (Button) itemView.findViewById(R.id.selectColor);        // 获取颜色按钮控件
        }

        /**
         * 绑定颜色数据到按钮
         * @param position 列表位置
         */
        private void bind(final int position) {
            colorBtn.setBackgroundColor(Color.parseColor(colorList.get(position))); // 设置按钮背景为对应颜色
            colorBtn.setOnClickListener(new View.OnClickListener() {             // 设置按钮点击监听
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {                           // 如果点击监听器存在
                        onItemClickListener.onItemClick(itemView, colorList.get(position)); // 回调颜色项点击事件
                    }
                }
            });
        }
    }
}
