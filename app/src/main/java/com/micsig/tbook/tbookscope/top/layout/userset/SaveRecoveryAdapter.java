package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件
import android.widget.TextView; // 导入文本视图控件

import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView

import com.micsig.tbook.tbookscope.R; // 导入资源引用

import java.util.ArrayList; // 导入动态数组

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 存储/恢复 → 列表适配器                                │
 * │ 核心职责：为存储/恢复列表提供RecyclerView适配器，绑定数据和点击事件          │
 * │ 架构设计：适配器模式，持有SaveRecovery数据列表，通过ViewHolder渲染列表项    │
 * │ 数据流向：TopLayoutUsersetSaveRecovery → SaveRecoveryAdapter(本类) → UI    │
 * │ 依赖关系：依赖SaveRecovery数据模型、RecyclerView框架                       │
 * │ 使用场景：存储/恢复界面的RecyclerView列表适配                               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 存储/恢复列表适配器，将SaveRecovery数据绑定到RecyclerView列表项视图。
 * <p>
 * 每个列表项包含：名称文本、存储按钮、恢复按钮。
 * 通过OnSaveRecoveryClickListener回调将点击事件传递给外部。
 *
 * @author yangj
 * @since 2017/4/27
 */
public class SaveRecoveryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> { // 继承RecyclerView适配器
    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 存储/恢复数据列表 */
    private ArrayList<SaveRecovery> list; // 存储/恢复数据列表
    /** 点击事件回调监听器 */
    private OnSaveRecoveryClickListener onSaveRecoveryClickListener; // 点击事件回调监听器

    /**
     * 存储/恢复点击事件监听接口，定义存储、恢复、编辑三种操作回调。
     */
    public interface OnSaveRecoveryClickListener { // 点击事件监听接口
        /**
         * 点击存储按钮回调。
         *
         * @param saveRecovery 被点击的存储记录
         */
        void onClickStorage(SaveRecovery saveRecovery); // 点击存储按钮回调

        /**
         * 点击恢复按钮回调。
         *
         * @param saveRecovery 被点击的存储记录
         */
        void onClickRecovery(SaveRecovery saveRecovery); // 点击恢复按钮回调

        /**
         * 点击编辑（名称）回调。
         *
         * @param saveRecovery 被点击的存储记录
         */
        void onClickEdit(SaveRecovery saveRecovery); // 点击编辑名称回调
    }

    /**
     * 设置点击事件监听器。
     *
     * @param onSaveRecoveryClickListener 监听器实例
     */
    void setOnSaveRecoveryClickListener(OnSaveRecoveryClickListener onSaveRecoveryClickListener) { // 设置点击事件监听器
        this.onSaveRecoveryClickListener = onSaveRecoveryClickListener; // 赋值监听器
    }

    /**
     * 构造方法，初始化适配器。
     *
     * @param context 上下文对象
     * @param list    存储/恢复数据列表
     */
    SaveRecoveryAdapter(Context context, ArrayList<SaveRecovery> list) { // 构造方法
        this.context = context; // 赋值上下文
        this.list = list; // 赋值数据列表
    }

    /**
     * 创建ViewHolder，加载列表项布局。
     *
     * @param parent   父视图组
     * @param viewType 视图类型
     * @return 新创建的ViewHolder
     */
    @Override // 覆写创建ViewHolder方法
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 创建ViewHolder
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_oscillograph, parent, false)); // 填充列表项布局并创建Holder
    }

    /**
     * 绑定数据到ViewHolder。
     *
     * @param holder   ViewHolder实例
     * @param position 列表位置
     */
    @Override // 覆写绑定数据方法
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) { // 绑定数据到ViewHolder
        ((Holder) holder).bind(list.get(position)); // 调用Holder的bind方法绑定数据
    }

    /**
     * 获取列表项总数。
     *
     * @return 列表大小
     */
    @Override // 覆写获取项目数方法
    public int getItemCount() { // 获取列表项总数
        return list.size(); // 返回列表大小
    }

    /**
     * 列表项ViewHolder，持有名称、存储按钮、恢复按钮的引用，并绑定点击事件。
     */
    class Holder extends RecyclerView.ViewHolder { // 内部ViewHolder类
        /** 名称文本视图 */
        TextView name; // 名称文本视图
        /** 存储按钮 */
        Button storage; // 存储按钮
        /** 恢复按钮 */
        Button recovery; // 恢复按钮

        /**
         * 构造方法，初始化视图引用。
         *
         * @param itemView 列表项根视图
         */
        Holder(View itemView) { // 构造方法
            super(itemView); // 调用父类构造
            name = (TextView) itemView.findViewById(R.id.name); // 查找名称文本视图
            storage = (Button) itemView.findViewById(R.id.storage); // 查找存储按钮
            recovery = (Button) itemView.findViewById(R.id.recovery); // 查找恢复按钮
        }

        /**
         * 绑定数据到视图，设置名称文本和各按钮点击事件。
         *
         * @param saveRecovery 要绑定的存储/恢复数据
         */
        void bind(final SaveRecovery saveRecovery) { // 绑定数据到视图
            name.setText(saveRecovery.getName()); // 设置名称文本
            name.setOnClickListener(new View.OnClickListener() { // 设置名称点击监听
                @Override // 覆写onClick方法
                public void onClick(View v) { // 点击名称
                    if (onSaveRecoveryClickListener != null) { // 判断监听器不为空
                        onSaveRecoveryClickListener.onClickEdit(saveRecovery); // 触发编辑回调
                    }
                }
            });
            storage.setOnClickListener(new View.OnClickListener() { // 设置存储按钮点击监听
                @Override // 覆写onClick方法
                public void onClick(View v) { // 点击存储按钮
                    saveRecovery.setName(name.getText().toString()); // 将当前名称更新到数据对象
                    if (onSaveRecoveryClickListener != null) { // 判断监听器不为空
                        onSaveRecoveryClickListener.onClickStorage(saveRecovery); // 触发存储回调
                    }
                }
            });
            recovery.setOnClickListener(new View.OnClickListener() { // 设置恢复按钮点击监听
                @Override // 覆写onClick方法
                public void onClick(View v) { // 点击恢复按钮
                    if (onSaveRecoveryClickListener != null) { // 判断监听器不为空
                        onSaveRecoveryClickListener.onClickRecovery(saveRecovery); // 触发恢复回调
                    }
                }
            });
        }
    }
}
