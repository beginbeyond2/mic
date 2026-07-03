package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/5/3.
 */

/*
 * +=============================================================================+
 * |                      DialogRefRecallAdapter                                  |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 参考波形调出对话框的列表适配器                          |
 * | 核心职责 : 将 DialogRefRecallBean 列表数据绑定到 RecyclerView 的列表项视图         |
 * | 架构设计 : 标准 RecyclerView.Adapter 模式，内部定义 Holder 作为 ViewHolder         |
 * | 数据流向 : DialogRefRecall 传入列表 → Adapter 绑定数据到视图 → 用户点击 →         |
 * |           OnItemClickListener 回传选中Bean                                    |
 * | 依赖关系 : DialogRefRecallBean（数据模型）、R.layout.item_dialogrefrecall（列表项布局）|
 * | 使用场景 : 被 DialogRefRecall 使用，渲染参考波形文件列表                         |
 * +=============================================================================+
 */
public class DialogRefRecallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;                                                    // 上下文引用
    private ArrayList<DialogRefRecallBean> list;                                // 参考波形文件数据列表
    private OnItemClickListener onItemClickListener;                            // 列表项点击监听器

    /**
     * 设置数据列表
     * @param list 新的参考波形数据列表
     */
    public void setList(ArrayList<DialogRefRecallBean> list) {
        this.list = list;                                                       // 更新数据列表引用
    }

    /**
     * 列表项点击监听接口
     */
    interface OnItemClickListener {
        /** 列表项点击回调
         * @param item 被点击的参考波形数据Bean
         */
        void onItemClick(DialogRefRecallBean item);
    }

    /**
     * 设置列表项点击监听器
     * @param onItemClickListener 点击监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;                         // 保存点击监听器
    }

    /**
     * 构造方法
     * @param context 上下文
     * @param list    参考波形数据列表
     */
    public DialogRefRecallAdapter(Context context, ArrayList<DialogRefRecallBean> list) {
        this.context = context;                                                 // 保存上下文引用
        this.list = list;                                                       // 保存数据列表引用
    }

    /**
     * 创建列表项 ViewHolder
     * @param parent   父视图组
     * @param viewType 视图类型
     * @return 新创建的 Holder 实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_dialogrefrecall, parent, false)); // 加载列表项布局并创建 Holder
    }

    /**
     * 绑定数据到 ViewHolder
     * @param holder   ViewHolder 实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));                              // 委托给 Holder.bind() 绑定数据
    }

    /**
     * 获取列表项总数
     * @return 列表大小
     */
    @Override
    public int getItemCount() {
        return list.size();                                                      // 返回列表项数量
    }

    /**
     * 列表项 ViewHolder，负责单个列表项的视图绑定与交互
     */
    class Holder extends RecyclerView.ViewHolder {
        TextView title;                                                         // 波形文件名标题
        TextView time;                                                          // 波形文件修改时间
        View dividerTop, dividerBottom, dividerLeft, dividerRight;              // 四边分割线（选中时高亮）

        /**
         * 构造方法，初始化列表项内的子视图
         * @param itemView 列表项根视图
         */
        public Holder(View itemView) {
            super(itemView);                                                     // 调用父类构造
            itemView.setBackgroundColor(context.getResources().getColor(R.color.bg_slip_backcolor)); // 设置列表项背景色
            title = (TextView) itemView.findViewById(R.id.title);               // 获取标题 TextView
            time = (TextView) itemView.findViewById(R.id.time);                 // 获取时间 TextView
            dividerTop = itemView.findViewById(R.id.dividerTop);                // 获取顶部分割线
            dividerBottom = itemView.findViewById(R.id.dividerBottom);           // 获取底部分割线
            dividerLeft = itemView.findViewById(R.id.dividerLeft);              // 获取左侧分割线
            dividerRight = itemView.findViewById(R.id.dividerRight);            // 获取右侧分割线
        }

        /**
         * 绑定数据到列表项视图
         * @param item 参考波形数据Bean
         */
        public void bind(final DialogRefRecallBean item) {
            title.setText(item.getTitle());                                      // 设置标题文本
            time.setText(item.getTime());                                        // 设置时间文本
            if (item.isSelect()) {                                               // 如果当前项被选中
                dividerTop.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref)); // 顶部分割线高亮
                dividerBottom.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref)); // 底部分割线高亮
                dividerLeft.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref)); // 左侧分割线高亮
                dividerRight.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref)); // 右侧分割线高亮
            } else {                                                            // 未选中状态
                dividerTop.setBackground(null);                                  // 顶部分割线清除背景
                dividerBottom.setBackground(null);                               // 底部分割线清除背景
                dividerLeft.setBackground(null);                                 // 左侧分割线清除背景
                dividerRight.setBackground(null);                                // 右侧分割线清除背景
            }
            itemView.setOnClickListener(new View.OnClickListener() {             // 设置列表项点击监听
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {                           // 如果点击监听器存在
                        onItemClickListener.onItemClick(item);                   // 回调列表项点击事件
                    }
                }
            });
        }
    }
}
