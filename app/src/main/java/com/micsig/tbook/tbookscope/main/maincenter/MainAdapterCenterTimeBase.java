package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径为主界面中心区域模块

import android.content.Context; // 导入上下文环境类,用于获取资源和系统服务
import android.view.LayoutInflater; // 导入布局加载器,用于动态加载XML布局文件
import android.view.View; // 导入视图基类,所有UI组件的父类
import android.view.ViewGroup; // 导入视图容器类,用于管理子视图的布局参数
import android.widget.TextView; // 导入文本视图控件,用于显示文本信息

import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView控件,用于高效显示大量数据列表

import com.micsig.tbook.tbookscope.R; // 导入应用资源类,包含所有资源的ID引用

import java.util.List; // 导入List集合接口,用于存储有序的数据集合

/**
 * Created by yangj on 2017/12/15.
 */

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          MainAdapterCenterTimeBase                           ║
 * ║                         中央时基适配器类(RV适配器)                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║【模块定位】                                                                   ║
 * ║  位于main.maincenter包中,作为示波器主界面中央区域时基选择列表的RecyclerView   ║
 * ║  数据适配器,负责将时基档位数据绑定到UI列表视图上                              ║
 * ║【核心职责】                                                                   ║
 * ║  1. 管理TimeBaseScale对象的列表数据                                          ║
 * ║  2. 创建并绑定列表项视图(Holder)                                             ║
 * ║  3. 处理用户点击事件并回调给监听器                                            ║
 * ║  4. 提供数据项数量查询接口                                                    ║
 * ║【架构设计】                                                                   ║
 * ║  采用RecyclerView.Adapter标准架构模式                                        ║
 * ║  - ViewHolder模式: Holder内部类持有视图引用,优化性能                         ║
 * ║  - 监听器模式: OnClickListener接口解耦点击事件处理                           ║
 * ║  - 观察者模式: 数据变化自动触发UI更新                                         ║
 * ║【数据流向】                                                                   ║
 * ║  TimeBaseScale列表 → Adapter → Holder → TextView显示                        ║
 * ║  用户点击 → Holder → OnClickListener回调 → 业务处理                          ║
 * ║【依赖关系】                                                                   ║
 * ║  上层依赖: MainLayoutCenterTimeBase(使用本适配器)                            ║
 * ║  数据依赖: TimeBaseScale(时基档位数据模型)                                   ║
 * ║  UI依赖: RecyclerView, TextView, 布局文件item_maincenter_timebase.xml        ║
 * ║【使用场景】                                                                   ║
 * ║  在主界面中央弹出时基选择对话框时,用于显示所有可选时基档位列表                ║
 * ║  用户可通过点击列表项快速切换示波器的时基设置                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainAdapterCenterTimeBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context; // 上下文环境引用,用于访问资源和创建视图
    private List<TimeBaseScale> list; // 时基档位列表数据源,存储所有可选的时基值
    private OnClickListener onClickListener; // 点击事件监听器,用于处理用户的点击回调

    /**
     * 点击事件监听器接口
     * 定义点击时基项时的回调方法
     */
    public interface OnClickListener {
        /**
         * 点击回调方法
         * @param scale 被点击的时基档位对象
         */
        void onClick(TimeBaseScale scale); // 点击回调方法,传递被选中的时基档位对象
    }

    /**
     * 设置点击事件监听器
     * @param onClickListener 点击监听器实例
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener; // 保存监听器引用,用于后续点击回调
    }

    /**
     * 构造方法
     * 初始化适配器的基本参数
     * @param context 上下文环境,用于加载布局和获取资源
     * @param list 时基档位数据列表,将显示在RecyclerView中
     */
    public MainAdapterCenterTimeBase(Context context, List<TimeBaseScale> list) {
        this.context = context; // 保存上下文引用,后续创建视图时需要
        this.list = list; // 保存数据源列表,提供数据绑定支持
    }

    /**
     * 创建ViewHolder
     * RecyclerView调用此方法创建新的列表项视图容器
     * @param parent 父容器,即RecyclerView本身
     * @param viewType 视图类型(本适配器只有一种类型)
     * @return 新创建的Holder实例,包含列表项视图
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_maincenter_timebase, parent, false)); // 加载布局文件并创建Holder对象
    }

    /**
     * 绑定数据到ViewHolder
     * RecyclerView调用此方法将数据绑定到指定位置的视图
     * @param holder ViewHolder实例,包含待绑定的视图
     * @param position 数据在列表中的位置索引
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position)); // 调用Holder的bind方法绑定对应位置的数据
    }

    /**
     * 获取数据项数量
     * RecyclerView调用此方法确定列表总长度
     * @return 数据列表的总大小
     */
    @Override
    public int getItemCount() {
        return list.size(); // 返回数据源列表的大小,决定显示多少项
    }

    /**
     * ViewHolder内部类
     * 持有列表项视图引用,实现视图复用以提高性能
     */
    class Holder extends RecyclerView.ViewHolder {
        TextView textView; // 文本视图引用,用于显示时基档位字符串

        /**
         * Holder构造方法
         * 初始化视图引用
         * @param itemView 列表项根视图,从布局文件加载而来
         */
        public Holder(View itemView) {
            super(itemView); // 调用父类构造方法保存itemView引用
            textView = (TextView) itemView.findViewById(R.id.tm); // 从布局中查找TextView控件并保存引用
        }

        /**
         * 绑定数据方法
         * 将时基数据绑定到视图并设置点击事件
         * @param s 待绑定的时基档位对象
         */
        public void bind(final TimeBaseScale s) {
            textView.setText(s.getScale()); // 设置TextView显示时基档位字符串(如"1ms")
            textView.setOnClickListener(new View.OnClickListener() { // 设置点击监听器
                /**
                 * 点击事件回调
                 * @param v 被点击的视图对象
                 */
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) { // 检查监听器是否已设置
                        onClickListener.onClick(s); // 触发回调,传递被点击的时基对象
                    }
                }
            });
        }
    }
}