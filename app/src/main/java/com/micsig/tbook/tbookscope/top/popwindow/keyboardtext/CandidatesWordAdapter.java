package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

import android.content.Context; // 导入上下文类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.TextView; // 导入文本视图类

import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView类

import com.micsig.tbook.tbookscope.R; // 导入资源类

import java.util.List; // 导入列表类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 候选词列表适配器                              │
 * │ 核心职责：将候选词数据绑定到RecyclerView的列表项视图                │
 * │ 架构设计：RecyclerView.Adapter适配器模式，内部Holder持有TextView    │
 * │ 数据流向：KeyBoardCandidatesItem列表 → Holder绑定 → TextView显示   │
 * │ 依赖关系：KeyBoardCandidatesItem, RxBus                            │
 * │ 使用场景：TopDialogCandidatesWord中的候选词横向滚动列表             │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/6.
 */

class CandidatesWordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> { // 候选词适配器
    private Context context; // 上下文对象
    private List<KeyBoardCandidatesItem> list; // 候选词数据列表
    private OnCandidatesWordListener onCandidatesWordListener; // 候选词点击监听器

    /**
     * 候选词点击监听接口
     */
    interface OnCandidatesWordListener { // 候选词点击监听接口
        /**
         * 候选词被点击时回调
         * @param item 被点击的候选词数据项
         */
        void onCandidateWord(KeyBoardCandidatesItem item); // 候选词点击回调
    }

    /**
     * 设置候选词点击监听器
     * @param onCandidatesWordListener 监听器
     */
    public void setOnCandidatesWordListener(OnCandidatesWordListener onCandidatesWordListener) { // 设置监听器
        this.onCandidatesWordListener = onCandidatesWordListener; // 赋值监听器
    }

    /**
     * 构造函数
     * @param context 上下文对象
     * @param list 候选词数据列表
     */
    public CandidatesWordAdapter(Context context, List<KeyBoardCandidatesItem> list) { // 构造函数
        this.context = context; // 保存上下文
        this.list = list; // 保存数据列表
    }

    /**
     * 创建ViewHolder
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return ViewHolder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 创建ViewHolder
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_canditatesword, parent, false)); // 加载布局并创建Holder
    }

    /**
     * 绑定数据到ViewHolder
     * @param holder ViewHolder实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) { // 绑定数据
        ((Holder) holder).bind(list.get(position)); // 调用Holder的bind方法
    }

    /**
     * 获取列表项数量
     * @return 候选词数量
     */
    @Override
    public int getItemCount() { // 获取数量
        return list.size(); // 返回列表大小
    }

    /**
     * ViewHolder内部类，持有候选词TextView
     */
    class Holder extends RecyclerView.ViewHolder { // ViewHolder内部类
        TextView textView; // 候选词文本视图

        /**
         * 构造函数
         * @param itemView 列表项视图
         */
        Holder(View itemView) { // 构造函数
            super(itemView); // 调用父类构造
            textView = (TextView) itemView.findViewById(R.id.text); // 获取文本视图
        }

        /**
         * 绑定候选词数据到视图
         * @param item 候选词数据项
         */
        void bind(final KeyBoardCandidatesItem item) { // 绑定数据方法
            textView.setText(item.getText()); // 设置候选词文字
            if (item.isSelect()) { // 如果是选中状态
                textView.setBackgroundColor(context.getResources().getColor(R.color.bgNewTopViewSelect)); // 设置选中背景色
                textView.setTextColor(context.getResources().getColor(R.color.textColorNewTopTitleUnSelect)); // 设置文字颜色
            } else { // 未选中状态
                textView.setBackgroundColor(context.getResources().getColor(R.color.bgNewTopAllLayout)); // 设置默认背景色
                textView.setTextColor(context.getResources().getColor(R.color.textColorNewTopTitleUnSelect)); // 设置文字颜色
            }
            textView.setOnClickListener(new View.OnClickListener() { // 设置点击监听
                @Override
                public void onClick(View v) { // 点击事件回调
                    if (onCandidatesWordListener != null) { // 如果监听器不为空
                        onCandidatesWordListener.onCandidateWord(item); // 回调候选词点击
                    }
                }
            });
        }
    }
}
