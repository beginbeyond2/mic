package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

/**
 * 新的RefRecall adapter
 */

/*
 * +=============================================================================+
 * |                     ForEightRefRecallAdapter                                 |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 八通道参考波形调出的列表适配器（新版本）                    |
 * | 核心职责 : 将 DialogRefRecallBean 列表数据绑定到 RecyclerView 列表项视图；          |
 * |           支持触摸手势（区分点击与滑动），防止快速重复点击                           |
 * | 架构设计 : 标准 RecyclerView.Adapter 模式，内部 Holder 使用 OnTouchListener       |
 * |           替代 OnClickListener，以区分点击与滑动操作                              |
 * | 数据流向 : 外部传入列表 → Adapter 绑定数据 → 用户触摸点击 →                       |
 * |           OnItemClickListener 回传选中Bean                                     |
 * | 依赖关系 : DialogRefRecallBean（数据模型）、TChan（通道颜色）、                    |
 * |           R.layout.item_refrecall_for_eight（列表项布局）                         |
 * | 使用场景 : 八通道示波器中，参考波形调出对话框的列表渲染                             |
 * +=============================================================================+
 */
public class ForEightRefRecallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;                                                    // 上下文引用
    private ArrayList<DialogRefRecallBean> list = new ArrayList<>();            // 参考波形文件数据列表
    private OnItemClickListener onItemClickListener;                            // 列表项点击监听器
    private int refChan;                                                        // 参考通道编号，用于获取通道颜色
    private long mLastClickTime = 0;                                            // 上次点击时间戳，用于防抖

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
     public interface OnItemClickListener {
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
     * @param refChan 参考通道编号
     */
    public ForEightRefRecallAdapter(Context context, ArrayList<DialogRefRecallBean> list, int refChan) {
        this.context = context;                                                 // 保存上下文引用
        this.list = list;                                                       // 保存数据列表引用
        this.refChan = refChan;                                                 // 保存参考通道编号
    }

    /**
     * 创建列表项 ViewHolder
     * @param parent   父视图组
     * @param viewType 视图类型
     * @return 新创建的 Holder 实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_refrecall_for_eight, parent, false); // 加载列表项布局
        return new Holder(mView);                                               // 创建并返回 Holder
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
     * 获取当前选中的列表项
     * @return 选中的 Bean，无选中项时返回 null
     */
    public DialogRefRecallBean getSelectItem() {
        DialogRefRecallBean selectItem = null;                                  // 选中的Bean
        for (DialogRefRecallBean item : list) {                                 // 遍历列表
            if (item.isSelect()) {                                              // 找到选中项
                selectItem = item;                                              // 记录选中Bean
                break;                                                          // 跳出循环
            }
        }
        return selectItem;                                                      // 返回选中项
    }

    /**
     * 列表项 ViewHolder，负责单个列表项的视图绑定与触摸交互
     * <p>使用 OnTouchListener 替代 OnClickListener，以区分点击和滑动操作</p>
     */
    class Holder extends RecyclerView.ViewHolder {
        private TextView title, time, path;                                     // 标题、时间、路径文本
        private View dividerLeft, dividerTop, dividerRight, dividerBottom;      // 四边分割线

        /**
         * 构造方法，初始化列表项内的子视图
         * @param itemView 列表项根视图
         */
        public Holder(View itemView) {
            super(itemView);                                                     // 调用父类构造
            itemView.setBackgroundColor(context.getResources().getColor(R.color.bg_slip_backcolor)); // 设置列表项背景色
            title = (TextView) itemView.findViewById(R.id.title);               // 获取标题 TextView
            time = (TextView) itemView.findViewById(R.id.time);                 // 获取时间 TextView
            path = (TextView) itemView.findViewById(R.id.path);                 // 获取路径 TextView

            dividerLeft = itemView.findViewById(R.id.dividerLeft);              // 获取左侧分割线
            dividerTop = itemView.findViewById(R.id.dividerTop);                // 获取顶部分割线
            dividerRight = itemView.findViewById(R.id.dividerRight);            // 获取右侧分割线
            dividerBottom = itemView.findViewById(R.id.dividerBottom);           // 获取底部分割线
        }

        /**
         * 绑定数据到列表项视图
         * <p>设置文本、选中高亮效果、触摸点击监听（区分点击与滑动）</p>
         * @param item 参考波形数据Bean
         */
        public void bind(DialogRefRecallBean item) {
            title.setText(item.getTitle());                                      // 设置标题文本
            time.setText(item.getTime());                                        // 设置时间文本
            path.setText(item.getPathFile());                                    // 设置路径文本
            if (item.isSelect()) {                                               // 选中状态：四边高亮为通道颜色
                dividerLeft.setBackgroundColor(TChan.getChannelColor(context, refChan)); // 左侧分割线高亮
                dividerTop.setBackgroundColor(TChan.getChannelColor(context, refChan));  // 顶部分割线高亮
                dividerRight.setBackgroundColor(TChan.getChannelColor(context, refChan)); // 右侧分割线高亮
                dividerBottom.setBackgroundColor(TChan.getChannelColor(context, refChan)); // 底部分割线高亮
            } else {                                                            // 未选中状态
                dividerLeft.setBackground(null);                                 // 左侧分割线清除背景
                dividerTop.setBackgroundColor(context.getResources().getColor(R.color.divider_right_bottom)); // 顶部分割线设置默认颜色
                dividerRight.setBackground(null);                                // 右侧分割线清除背景
                if (item.getIndex() == list.size() - 1) {//最后一个画bottom线
                    dividerBottom.setBackgroundColor(context.getResources().getColor(R.color.divider_right_bottom)); // 末项底部分割线设置默认颜色
                } else {                                                        // 非末项
                    dividerBottom.setBackground(null);                           // 底部分割线清除背景
                }
            }
//
            itemView.setOnTouchListener(new View.OnTouchListener() {             // 设置触摸监听（替代OnClickListener）
                private boolean hasMove = false;                                 // 是否发生了滑动
                private float oldX, oldY;                                        // 按下时的坐标

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {                                 // 根据触摸动作类型分发
                        case MotionEvent.ACTION_DOWN:                            // 按下
                            hasMove = false;                                     // 重置滑动标志
                            oldX = event.getX();                                 // 记录按下X坐标
                            oldY = event.getY();                                 // 记录按下Y坐标
                            break;                                              // 结束按下分支
                        case MotionEvent.ACTION_MOVE:                            // 移动
                            float offsetX = Math.abs(event.getX() - oldX);      // 计算X方向偏移量
                            float offsetY = Math.abs(event.getY() - oldY);      // 计算Y方向偏移量
                            if (offsetX > 5 || offsetY > 5) {                   // 偏移超过5像素视为滑动
                                hasMove = true;                                  // 标记发生了滑动
                            }
                            break;                                              // 结束移动分支
                        case MotionEvent.ACTION_UP:                              // 抬起
                        case MotionEvent.ACTION_CANCEL:                          // 取消
                           long  timeCurrent = System.currentTimeMillis();      // 获取当前时间戳
                            if (!hasMove && Math.abs(timeCurrent - mLastClickTime) > 300) {//只是简单的点击事件 // 未滑动且距上次点击超过300ms
                                mLastClickTime = timeCurrent;                   // 更新上次点击时间
                                onItemClickListener.onItemClick(item);           // 回调列表项点击事件
                            }
                            break;                                              // 结束抬起/取消分支
                    }
                    return true;                                                // 消费触摸事件
                }
            });

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (onItemClickListener != null) {
//                        onItemClickListener.onItemClick(item);
//                    }
//                }
//            });
        }


    }
}
