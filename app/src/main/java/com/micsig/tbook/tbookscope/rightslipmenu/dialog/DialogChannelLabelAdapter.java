package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.BitmapUtil;

import java.util.ArrayList;

/*
 * +----------------------------------------------------------------------+
 * |                   DialogChannelLabelAdapter                          |
 * |                   通道标签网格适配器                                   |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包 -> DialogChannelLabel的内部适配器     |
 * | 核心职责: 为DialogChannelLabel提供RecyclerView网格布局的数据绑定和视图渲染, |
 * |          管理None/自定义项(2列跨距)与普通预设项(1列跨距)的差异化显示      |
 * | 架构设计: 标准RecyclerView.Adapter实现，内部Holder类持有RadioButton，    |
 * |          通过getItemViewType返回跨距数实现GridLayoutManager的合并列      |
 * | 数据流向: list数据源 -> Holder绑定 -> 视图渲染 -> 点击回调OnItemClickListener |
 * | 依赖关系: RightBeanSelect(标签数据模型), BitmapUtil(通道颜色选择器生成)   |
 * | 使用场景: 仅被DialogChannelLabel内部创建和使用                          |
 * +----------------------------------------------------------------------+
 */
public class DialogChannelLabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;  // 上下文引用
    private ArrayList<RightBeanSelect> list;  // 标签数据列表
    private OnItemClickListener onItemClickListener;  // 项点击监听器
    private ColorStateList itemTextColorResId;  // 文字颜色状态列表

    private int chIdx;  // 通道索引，用于生成对应颜色

    /**
     * 项点击监听接口。
     * <p>
     * 当标签项被点击时回调，传递点击的视图和数据项。
     */
    interface OnItemClickListener {
        /** 标签项点击回调 */
        void onItemClick(View itemView, RightBeanSelect item);
    }

    /**
     * 构造方法，初始化数据源和监听器。
     *
     * @param context             上下文
     * @param list                标签数据列表
     * @param onItemClickListener 项点击监听器
     */
    public DialogChannelLabelAdapter(Context context, ArrayList<RightBeanSelect> list, OnItemClickListener onItemClickListener) {
        this.context = context;  // 保存上下文
        this.list = list;  // 保存数据列表
        this.onItemClickListener = onItemClickListener;  // 保存点击监听器
    }

    /**
     * 创建ViewHolder，填充标签项布局。
     *
     * @param parent   父视图组
     * @param viewType 视图类型（未使用）
     * @return 新创建的Holder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_right_channellabel, parent, false));  // 填充标签项布局
    }

    /**
     * 绑定ViewHolder数据，委托给Holder内部bind方法。
     *
     * @param holder   ViewHolder实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(position);  // 委托给Holder的bind方法
    }

    /**
     * 获取数据列表大小。
     *
     * @return 列表项数量
     */
    @Override
    public int getItemCount() {
        return list.size();  // 返回列表大小
    }

    /**
     * 获取指定位置的视图类型，用于GridLayoutManager的跨距计算。
     * <p>
     * 位置0（None）和位置1（自定义）返回2，表示跨2列；
     * 其他位置返回1，表示占1列。
     *
     * @param position 列表位置
     * @return 跨距数（2或1）
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1) {  // None项和自定义项
            return 2;  // 跨2列
        } else {  // 普通预设项
            return 1;  // 占1列
        }
    }

    /**
     * 设置通道索引，用于后续生成对应颜色的选择器。
     *
     * @param chIdx 通道索引
     */
    @SuppressLint("ResourceType")
    public void setControlColorByChIdx(int chIdx){
        this.chIdx=chIdx;  // 保存通道索引
    }

    /**
     * 列表项ViewHolder，持有RadioButton并处理绑定逻辑。
     * <p>
     * 自定义项（position==1）使用特殊样式（无背景、底部居中对齐），
     * 其他项使用通道对应颜色的选择器背景和居中对齐。
     */
    class Holder extends RecyclerView.ViewHolder {
        RadioButton textView;  // 标签单选按钮

        /**
         * 构造方法，查找RadioButton控件。
         *
         * @param itemView 项根视图
         */
        public Holder(View itemView) {
            super(itemView);  // 调用父类构造
            textView = (RadioButton) itemView.findViewById(R.id.textView);  // 查找RadioButton
        }

        /**
         * 绑定数据到视图。
         * <p>
         * 根据位置区分自定义项和普通项的显示样式，
         * 设置文本、选中状态和点击监听。
         *
         * @param position 列表位置
         */
        private void bind(final int position) {
            if (position == 1) {  // 自定义标签项
                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);  // 水平居中+底部对齐
                textView.setPadding(4, 0, 4, 0);  // 设置内边距
//                StateListDrawable d=BitmapUtil.genSelectorDrawable(context,chIdx);
//                textView.setBackground(d);
                textView.setBackground(null);  // 自定义项不设置背景
            } else {  // None项和普通预设项
                textView.setGravity(Gravity.CENTER);  // 居中对齐
                textView.setPadding(0, 0, 0, 0);  // 无内边距
                StateListDrawable d=BitmapUtil.genSelectorDrawable(context,chIdx);  // 生成通道颜色选择器背景
                textView.setBackground(d);  // 设置选择器背景
            }

            itemTextColorResId=BitmapUtil.genSelectorColor(context,chIdx);  // 生成通道文字颜色
            textView.setTextColor(itemTextColorResId);  // 设置文字颜色
            textView.setText(list.get(position).getText());  // 设置标签文本
            textView.setChecked(list.get(position).isCheck());  // 设置选中状态
            textView.setOnClickListener(new View.OnClickListener() {  // 设置点击监听
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {  // 如果设置了点击监听器
                        onItemClickListener.onItemClick(itemView, list.get(position));  // 回调点击事件
                    }
                }
            });
        }
    }
}
