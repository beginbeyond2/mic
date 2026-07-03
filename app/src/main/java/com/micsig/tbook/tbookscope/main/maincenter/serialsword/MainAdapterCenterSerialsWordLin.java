package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                         LIN总线列表显示适配器                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                │
 * │   RecyclerView适配器，负责将LIN总线解码数据绑定到列表视图，实现数据可视化展示    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                │
 * │   1. 管理LIN总线数据列表的显示与更新                                        │
 * │   2. 动态计算每行数据显示高度，支持数据换行显示                              │
 * │   3. 控制时间戳毫秒显示开关                                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                │
 * │   继承RecyclerView.Adapter，采用ViewHolder模式：                            │
 * │   - 外部类：适配器主体，管理数据列表和显示配置                                │
 * │   - 内部类Holder：ViewHolder，负责单行视图的数据绑定                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                │
 * │   SerialBusTxtStruct.LinStruct数据列表 → Adapter → Holder →               │
 * │   SerialsWordLinSingleRowTextView（单行视图组件）                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                │
 * │   依赖：SerialBusTxtStruct.LinStruct（LIN数据结构）                        │
 * │   依赖：SerialsWordLinSingleRowTextView（LIN单行显示视图）                  │
 * │   依赖：ISerialsWord（常量定义）                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用场景】                                                                │
 * │   在串行总线解码列表界面中，用于显示LIN协议的解码结果，每行显示一个LIN帧的       │
 * │   时间戳、ID、数据等信息                                                    │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainAdapterCenterSerialsWordLin extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context; // Android上下文对象，用于获取资源和创建视图
    private ArrayList<SerialBusTxtStruct.LinStruct> list; // LIN总线数据列表，存储所有待显示的LIN帧数据
    private boolean showMs = true; // 是否显示毫秒时间戳标志，默认为true
    private int formHeightDetail; // 单行数据显示的基础高度值，从资源文件中获取

    /**
     * 构造函数：初始化LIN总线列表适配器
     *
     * @param context Android上下文对象，用于访问资源和创建视图
     * @param list LIN总线数据列表，包含所有待显示的LIN帧数据
     */
    public MainAdapterCenterSerialsWordLin(Context context, ArrayList<SerialBusTxtStruct.LinStruct> list) {
        this.context = context; // 保存上下文对象引用
        this.list = list; // 保存LIN数据列表引用
        formHeightDetail = (int) context.getResources().getDimension(R.dimen.formHeightDetail); // 从资源文件中获取单行数据显示的基础高度
    }

    /**
     * 设置是否显示毫秒时间戳
     *
     * @param showMs true表示显示毫秒，false表示不显示毫秒
     */
    public void setShowMs(boolean showMs) {
        this.showMs = showMs; // 更新毫秒显示标志
    }

    /**
     * 获取当前毫秒显示状态
     *
     * @return 当前是否显示毫秒时间戳
     */
    public boolean isShowMs() {
        return showMs; // 返回毫秒显示标志
    }

    /**
     * 创建ViewHolder实例
     * 当RecyclerView需要新的ViewHolder来显示数据项时调用
     *
     * @param parent 父视图组
     * @param viewType 视图类型（本适配器中未使用多种视图类型）
     * @return 新创建的Holder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_lin, parent, false)); // 加载LIN单行布局并创建Holder
    }

    /**
     * 绑定数据到ViewHolder
     * 将指定位置的数据绑定到ViewHolder上显示
     *
     * @param holder 要绑定的ViewHolder
     * @param position 数据在列表中的位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position)); // 调用Holder的bind方法绑定数据
    }

    /**
     * 获取数据项总数
     *
     * @return 列表中数据项的总数
     */
    @Override
    public int getItemCount() {
        return list.size(); // 返回LIN数据列表的大小
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │                     LIN列表项ViewHolder内部类                           │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 负责单个LIN帧数据的显示和布局管理                                         │
     * │ 根据数据长度动态调整行高，支持多行显示                                     │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    class Holder extends RecyclerView.ViewHolder {
        private SerialsWordLinSingleRowTextView linTextView; // LIN单行数据显示视图组件

        /**
         * ViewHolder构造函数
         *
         * @param itemView 单行项视图
         */
        public Holder(View itemView) {
            super(itemView); // 调用父类构造函数
            linTextView = (SerialsWordLinSingleRowTextView) itemView.findViewById(R.id.bean); // 从布局中获取LIN数据视图组件
        }


        /**
         * 绑定LIN帧数据到视图
         * 根据数据长度计算并设置行高，然后显示数据
         *
         * @param bean LIN帧数据结构
         */
        public void bind(SerialBusTxtStruct.LinStruct bean) {
            ViewGroup.LayoutParams layoutParams = linTextView.getLayoutParams(); // 获取视图布局参数
            layoutParams.height = formHeightDetail * ((bean.Data.trim().length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_LIN + 1); // 根据数据长度计算行高：数据字符数除以每行最大字符数，向上取整后乘以单行高度
            linTextView.setLayoutParams(layoutParams); // 应用新的布局参数
            linTextView.setData(bean, showMs); // 设置LIN数据到视图组件，传入毫秒显示标志
        }
    }
}