package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

import android.content.Context; // Android上下文对象
import android.view.LayoutInflater; // 布局填充器，用于加载XML布局
import android.view.View; // Android视图基类
import android.view.ViewGroup; // Android视图组基类
import android.widget.TextView; // Android文本视图控件

import androidx.recyclerview.widget.RecyclerView; // AndroidX RecyclerView控件

import com.micsig.tbook.tbookscope.R; // 应用资源ID引用类
import com.micsig.tbook.tbookscope.util.Screen; // 屏幕工具类

import java.util.List; // Java列表接口

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    SerialsAdapter（串行协议列表适配器）                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/SerialsAdapter.java                                     ║
 * ║ 核心职责: 为串行触发协议选项列表提供RecyclerView适配器                        ║
 * ║ 架构设计: 继承RecyclerView.Adapter，内部Holder模式绑定视图                   ║
 * ║ 数据流向: Serials列表数据 → RecyclerView展示 → 用户点击选择                  ║
 * ║ 依赖关系: 依赖Serials数据类、Screen工具类                                   ║
 * ║ 使用场景: TopLayoutTriggerSerials中串行协议选项网格列表的适配                 ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/4/27.
 */

public class SerialsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> { // 串行协议选项的RecyclerView适配器
    private List<Serials> serialsList; // 串行协议选项数据列表
    private Context context; // Android上下文对象
    private OnSerialsItemClickListener onItemClickListener; // 列表项点击事件监听器

    /**
     * 构造方法，初始化适配器
     * @param context Android上下文
     * @param serialsList 串行协议选项数据列表
     */
    public SerialsAdapter(Context context, List<Serials> serialsList) { // 适配器构造方法
        this.serialsList = serialsList; // 保存串行协议列表引用
        this.context = context; // 保存上下文引用
    }

    /**
     * 列表项点击事件监听接口
     */
    interface OnSerialsItemClickListener { // 列表项点击事件回调接口
        /**
         * 当列表项被点击时回调
         * @param serialsList 当前串行协议列表
         * @param serials 被点击的串行协议项
         */
        void itemClick(List<Serials> serialsList, Serials serials); // 列表项点击回调方法
    }

    /**
     * 设置列表项点击事件监听器
     * @param onItemClickListener 点击事件监听器
     */
    public void setOnItemClickListener(OnSerialsItemClickListener onItemClickListener) { // 设置点击监听器
        this.onItemClickListener = onItemClickListener; // 保存监听器引用
    }

    /**
     * 创建ViewHolder，加载列表项布局
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return 新创建的ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 创建ViewHolder
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_triggerserials, parent, false)); // 加载列表项布局并创建Holder
    }

    /**
     * 绑定数据到ViewHolder
     * @param holder 目标ViewHolder
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) { // 绑定数据到ViewHolder
        ((Holder) holder).bind(serialsList.get(position)); // 将对应位置的数据绑定到Holder
    }

    /**
     * 获取当前选中的串行协议项
     * @return 选中的Serials对象，如果没有选中则返回null
     */
    public Serials getSelected() { // 获取当前选中的串行协议项
        for (Serials serials : serialsList) { // 遍历所有串行协议项
            if (serials.isEnabled() && serials.isSelected()) { // 判断是否启用且选中
                return serials; // 返回选中的项
            }
        }
        return null; // 没有选中项则返回null
    }

    /**
     * 获取列表项数量
     * @return 列表大小
     */
    @Override
    public int getItemCount() { // 获取列表项数量
        return serialsList.size(); // 返回列表大小
    }

    /**
     * 列表项的ViewHolder内部类，负责单个列表项的视图绑定和点击处理
     */
    class Holder extends RecyclerView.ViewHolder { // 列表项ViewHolder
        TextView text; // 列表项文本视图

        /**
         * ViewHolder构造方法
         * @param itemView 列表项根视图
         */
        public Holder(View itemView) { // ViewHolder构造方法
            super(itemView); // 调用父类构造方法
            text = (TextView) itemView.findViewById(R.id.text); // 查找文本视图控件
        }

        /**
         * 将Serials数据绑定到视图
         * @param serials 要绑定的串行协议数据
         */
        void bind(final Serials serials) { // 绑定数据到视图
            text.setText(serials.getName()); // 设置协议名称文本
            text.setEnabled(serials.isEnabled()); // 设置文本视图的启用状态
            text.setSelected(serials.isSelected()); // 设置文本视图的选中状态
            text.setOnClickListener(new View.OnClickListener() { // 设置点击监听器
                @Override
                public void onClick(View v) { // 点击事件处理
                    for (int i = 0; i < serialsList.size(); i++) { // 遍历所有列表项
                        Serials item = serialsList.get(i); // 获取当前项
                        if (item.getId() == serials.getId()) { // 判断是否为点击的项
                            item.setSelected(true); // 设置为选中
                        } else {
                            item.setSelected(false); // 设置为未选中
                        }
                    }
                    if (onItemClickListener != null) { // 如果监听器不为空
                        Screen.getViewLocation(text); // 获取视图位置信息
                        onItemClickListener.itemClick(serialsList, serials); // 回调点击事件
                    }
                    notifyDataSetChanged(); // 通知数据变化刷新列表
                }
            });
        }
    }
}
