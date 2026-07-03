// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/MeasureAdapter.java
//  核心职责：测量项列表的RecyclerView适配器，负责测量项和已选测量项的列表渲染与交互
//  架构设计：适配器模式，继承RecyclerView.Adapter，使用内部Holder类管理列表项视图
//  数据流向：MeasureBean数据列表 → Adapter → RecyclerView列表展示
//  依赖关系：依赖MeasureBean数据模型、SvgNodeInfo颜色配置、Screen工具类
//  使用场景：TopLayoutMeasureCommon中用于展示所有测量项和已选测量项的网格列表
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类，用于访问应用资源和类
import android.graphics.Color; // 导入颜色类，用于解析颜色字符串
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.ImageView; // 导入图片视图控件
import android.widget.TextView; // 导入文本视图控件

import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView基类

import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.util.Screen; // 导入屏幕工具类，用于获取视图位置
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // 导入SVG节点信息类，用于获取颜色配置

import java.util.ArrayList; // 导入动态数组类

/**
 * 测量项适配器 - 负责测量项列表和已选测量项列表的数据绑定与视图渲染
 * Created by yangj on 2017/4/27.
 */
public class MeasureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> { // 继承RecyclerView适配器，泛型指定为通用ViewHolder
    private Context context; // 上下文对象，用于资源访问
    private ArrayList<MeasureBean> measureBeanList; // 测量项数据列表
    private int[] colors; // 通道颜色数组，用于区分不同通道的测量项
    private int colorDefault; // 默认颜色值，用于未选中状态的文字颜色
    private OnItemClickListener itemClickListener; // 列表项点击事件监听器

    /**
     * 列表项点击事件监听接口
     */
    interface OnItemClickListener { // 定义内部点击事件监听接口
        /**
         * 点击回调方法
         * @param adapter 触发事件的适配器实例
         * @param measureBean 被点击的测量项数据
         */
        void onClick(MeasureAdapter adapter, MeasureBean measureBean); // 点击回调，传递适配器和测量项数据
    }

    /**
     * 设置列表项点击事件监听器
     * @param itemClickListener 点击事件监听器
     */
    public void setItemClickListener(OnItemClickListener itemClickListener) { // 设置外部点击事件监听器
        this.itemClickListener = itemClickListener; // 保存监听器引用
    }

    /**
     * 构造函数 - 初始化适配器
     * @param context 上下文对象
     * @param measureBeanList 测量项数据列表
     */
    public MeasureAdapter(Context context, ArrayList<MeasureBean> measureBeanList) { // 适配器构造函数
        this.context = context; // 保存上下文引用
        this.measureBeanList = measureBeanList; // 保存测量项数据列表
        colors = SvgNodeInfo.getColorsIntForView(); // 从SVG配置获取各通道颜色整型数组
        colorDefault = Color.parseColor(SvgNodeInfo.getColorCommon()); // 解析默认颜色字符串为整型颜色值
    }

    /**
     * 更新通道颜色配置，刷新已选中项的显示
     */
    public void updateColors() { // 更新颜色配置方法
        colors = SvgNodeInfo.getColorsIntForView(); // 重新获取通道颜色数组
        measureBeanList.forEach(bean -> { // 遍历所有测量项
            if (bean.isSelect()) { // 如果该测量项处于选中状态
                notifyItemChanged(measureBeanList.indexOf(bean)); // 通知该项数据已变更，触发重新绑定
            }
        });
    }

    /**
     * 获取测量项数据列表
     * @return 测量项数据列表
     */
    public ArrayList<MeasureBean> getMeasureBeanList() { // 获取测量项数据列表
        return measureBeanList; // 返回数据列表引用
    }

    /**
     * 创建列表项视图持有者
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return 新创建的ViewHolder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 创建ViewHolder
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_measure, parent, false)); // 填充测量项布局并创建Holder
    }

    /**
     * 绑定数据到列表项视图
     * @param holder 视图持有者
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) { // 绑定数据到视图
        ((Holder) holder).bind(measureBeanList.get(position)); // 将指定位置的测量项数据绑定到Holder
    }

    /**
     * 获取列表项点击事件监听器
     * @return 点击事件监听器
     */
    public OnItemClickListener getItemClickListener() { // 获取当前设置的点击事件监听器
        return  this.itemClickListener; // 返回监听器引用
    }

    /**
     * 获取列表项总数
     * @return 数据列表大小
     */
    @Override
    public int getItemCount() { // 获取列表项数量
        return measureBeanList.size(); // 返回数据列表的大小
    }

    /**
     * 内部ViewHolder类 - 管理测量项列表项的视图引用
     */
    private class Holder extends RecyclerView.ViewHolder { // 内部ViewHolder类
        private TextView tvName; // 测量项名称文本视图
        private ImageView ivName; // 测量项图标图片视图

        /**
         * ViewHolder构造函数
         * @param itemView 列表项根视图
         */
        Holder(View itemView) { // Holder构造函数
            super(itemView); // 调用父类构造函数
            tvName = (TextView) itemView.findViewById(R.id.tvName); // 查找名称文本视图
            ivName = (ImageView) itemView.findViewById(R.id.ivName); // 查找图标图片视图
        }

        /**
         * 绑定测量项数据到视图
         * @param measureBean 测量项数据对象
         */
        void bind(final MeasureBean measureBean) { // 绑定数据方法
            ivName.setImageResource(measureBean.getDrawableResId()); // 设置测量项图标资源
            tvName.setText(measureBean.getName()); // 设置测量项名称文本
            if (measureBean.getChannel() != 0 && measureBean.isSelect()) { // 如果通道非零且处于选中状态
                tvName.setTextColor(colors[measureBean.getChannel() - 1]); // 设置文字颜色为对应通道颜色
                tvName.setBackgroundResource(R.drawable.ic_mousedown_box); // 设置选中背景框
            } else { // 否则为未选中状态
                tvName.setTextColor(colorDefault); // 设置文字颜色为默认颜色
                tvName.setBackground(null); // 清除背景
            }

//            if (measureBean.getChannel() == 0) {
//                tvName.setTextColor(colorDefault);
//                tvName.setBackground(null);
//            } else {
//                tvName.setTextColor(colors[measureBean.getChannel() - 1]);
//                tvName.setBackgroundResource(R.drawable.ic_mousedown_box);
//            }
            itemView.setOnClickListener(new View.OnClickListener() { // 设置列表项点击监听器
                @Override
                public void onClick(View v) { // 点击事件处理
                    if (itemClickListener != null) { // 如果设置了点击监听器
                        Screen.getViewLocation(v); // 获取并记录视图位置信息
                        itemClickListener.onClick(MeasureAdapter.this, measureBean); // 回调点击事件，传递适配器和数据
                    }
                }
            });
        }
    }
}
