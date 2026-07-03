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
 * │                         UART总线列表显示适配器                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                │
 * │   RecyclerView适配器，负责将UART总线解码数据绑定到列表视图，实现数据可视化展示   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                │
 * │   1. 管理UART总线数据列表的显示与更新（支持多通道数据分组显示）               │
 * │   2. 支持多种显示格式配置（HEX/ASCII等）                                    │
 * │   3. 管理串口参数配置（数据位、校验位等）                                    │
 * │   4. 支持多通道数据显示（S1/S2/S3/S4）                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                │
 * │   继承RecyclerView.Adapter，采用ViewHolder模式：                            │
 * │   - 外部类：适配器主体，管理数据列表和显示配置                                │
 * │   - 内部类Holder：ViewHolder，负责单行视图的数据绑定                         │
 * │   特点：支持多通道数据分组，每个列表项可包含多个UART数据帧                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                │
 * │   ArrayList<ArrayList<UartStruct>>数据列表 → Adapter → Holder →           │
 * │   SerialsWordUartSingleRowTextView（单行视图组件）                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                │
 * │   依赖：SerialBusTxtStruct.UartStruct（UART数据结构）                      │
 * │   依赖：SerialsWordUartSingleRowTextView（UART单行显示视图）                │
 * │   依赖：ISerialsWord（常量定义）                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用场景】                                                                │
 * │   在串行总线解码列表界面中，用于显示UART协议的解码结果，每行显示一组UART数据帧  │
 * │   支持HEX/ASCII显示格式切换，支持不同通道的数据显示                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MainAdapterCenterSerialsWordUart extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context; // Android上下文对象，用于获取资源和创建视图
    private ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>> list; // UART总线数据列表，存储所有待显示的UART数据帧组（外层ArrayList表示行，内层ArrayList表示每行的多个数据帧）
    private String showType = SerialsWordUartSingleRowTextView.TYPE_HEX_OTHER; // 显示类型：HEX或其他格式，默认为HEX_OTHER
    private int chType = ISerialsWord.TYPE_S1; // 通道类型：S1/S2/S3/S4或组合，默认为S1通道
    private int bits,check; // 串口参数：bits为数据位数，check为校验类型

    /**
     * 构造函数：初始化UART总线列表适配器
     *
     * @param context Android上下文对象，用于访问资源和创建视图
     * @param list UART总线数据列表，包含所有待显示的UART数据帧组
     */
    public MainAdapterCenterSerialsWordUart(Context context, ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>> list) {
        this.context = context; // 保存上下文对象引用
        this.list = list; // 保存UART数据列表引用
    }

    /**
     * 设置串口数据位数
     *
     * @param bits 数据位数（如8位、7位等）
     */
    public void setBits(int bits){
        this.bits=bits; // 更新数据位配置
    }

    /**
     * 设置串口校验类型
     *
     * @param check 校验类型（如无校验、奇校验、偶校验等）
     */
    public void setCheck(int check){
        this.check=check; // 更新校验类型配置
    }

    /**
     * 设置显示类型
     *
     * @param showType 显示类型字符串（HEX格式或ASCII格式等）
     */
    public void setListType(String showType) {
        this.showType = showType; // 更新显示类型配置
    }

    /**
     * 设置通道类型
     *
     * @param chType 通道类型（S1/S2/S3/S4或组合）
     */
    public void setChType(int chType) {
        this.chType = chType; // 更新通道类型配置
    }

    /**
     * 获取当前显示类型
     *
     * @return 当前显示类型字符串
     */
    public String getShowType() {
        return showType; // 返回显示类型配置
    }

    /**
     * 获取当前通道类型
     *
     * @return 当前通道类型
     */
    public int getChType() {
        return chType; // 返回通道类型配置
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
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_uart, parent, false)); // 加载UART单行布局并创建Holder
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
        return list.size(); // 返回UART数据列表的大小
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │                     UART列表项ViewHolder内部类                          │
     * ├─────────────────────────────────────────────────────────────────────────┤
     * │ 负责单组UART数据帧的显示和布局管理                                        │
     * │ 支持多通道、多格式的数据显示                                              │
     * └─────────────────────────────────────────────────────────────────────────┘
     */
    class Holder extends RecyclerView.ViewHolder {
        private SerialsWordUartSingleRowTextView textView; // UART单行数据显示视图组件

        /**
         * ViewHolder构造函数
         *
         * @param itemView 单行项视图
         */
        public Holder(View itemView) {
            super(itemView); // 调用父类构造函数
            textView = (SerialsWordUartSingleRowTextView) itemView.findViewById(R.id.text); // 从布局中获取UART数据视图组件
        }


        /**
         * 绑定UART数据帧组到视图
         * 将串口参数、显示格式、通道类型和数据传递给视图组件显示
         *
         * @param bean UART数据帧组（包含多个UART帧数据）
         */
        public void bind(ArrayList<SerialBusTxtStruct.UartStruct> bean) {
            textView.setList(bits,check,showType, chType, bean); // 设置UART数据到视图组件，传入数据位、校验位、显示类型、通道类型和数据列表
        }
    }
}