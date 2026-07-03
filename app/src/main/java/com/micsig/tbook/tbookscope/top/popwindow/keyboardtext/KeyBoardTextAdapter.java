package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

import android.content.Context; // 导入上下文类
import android.util.TypedValue; // 导入类型值工具类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.Button; // 导入按钮类

import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView类

import com.micsig.tbook.tbookscope.R; // 导入资源类

import java.util.List; // 导入列表类
import java.util.Objects; // 导入对象工具类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 按键列表适配器                                │
 * │ 核心职责：将按键数据绑定到RecyclerView的网格项视图，处理按键点击事件 │
 * │ 架构设计：RecyclerView.Adapter + GridLayoutManager.SpanSizeLookup │
 * │          实现SpecialKey接口，根据按键索引分发不同点击事件            │
 * │ 数据流向：KeyBoardTextItem列表 → Holder绑定 → 按钮显示+点击回调    │
 * │ 依赖关系：KeyBoardTextItem, PlaySound                              │
 * │ 使用场景：TopDialogTextKeyBoard中的按键网格布局                     │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/1.
 */

public class KeyBoardTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements KeyBoardTextItem.SpecialKey { // 文本键盘适配器
    private Context context; // 上下文对象
    private List<KeyBoardTextItem> list; // 按键数据列表
    private boolean upper = false; // 是否大写模式
    private OnItemClickListener onItemClickListener; // 按键点击监听器

    /**
     * 获取按键点击监听器
     * @return 监听器
     */
    public OnItemClickListener getOnItemClickListener() { // 获取监听器
        return onItemClickListener; // 返回监听器
    }

    /**
     * 设置按键点击监听器
     * @param onItemClickListener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) { // 设置监听器
        this.onItemClickListener = onItemClickListener; // 赋值监听器
    }

    /**
     * 按键点击事件监听接口
     */
    public interface OnItemClickListener { // 按键点击监听接口
        /**
         * 长按删除（快速清空）
         */
        void onQuickDelete(); // 长按删除回调

        /**
         * 单次删除
         */
        void onDelete(); // 删除回调

        /**
         * 确认
         */
        void onEnter(); // 确认回调

        /**
         * 大写切换
         * @param isUpper 切换后是否为大写
         */
        void onUpper(boolean isUpper); // 大写切换回调

        /**
         * 隐藏键盘
         */
        void onHide(); // 隐藏回调

        /**
         * 切换到数字键盘
         */
        void onNumber(); // 数字切换回调

        /**
         * 切换到符号键盘
         */
        void onSymbol(); // 符号切换回调

        /**
         * 语言切换
         * @param isEng 点击之前的状态是否是Eng
         */
        void onEnglish(boolean isEng); // 语言切换回调

        /**
         * 其他普通按键
         * @param keyWord 按键文字
         */
        void onOtherKey(String keyWord); // 其他按键回调
    }

    /**
     * 获取是否大写模式
     * @return true表示大写
     */
    public boolean isUpper() { // 获取大写状态
        return upper; // 返回大写状态
    }

    /**
     * 设置大写模式
     * @param upper 是否大写
     */
    public void setUpper(boolean upper) { // 设置大写状态
        this.upper = upper; // 赋值大写状态
    }

    /**
     * 构造函数
     * @param context 上下文对象
     * @param list 按键数据列表
     */
    public KeyBoardTextAdapter(Context context, List<KeyBoardTextItem> list) { // 构造函数
        this.context = context; // 保存上下文
        this.list = list; // 保存数据列表
    }

    /**
     * 获取指定位置的SpanSize（按键占用格数）
     * @param position 列表位置
     * @return 占用格数
     */
    public int getSpanSize(int position) { // 获取SpanSize
        KeyBoardTextItem item = list.get(position); // 获取数据项
        return item.getSize(); // 返回占用格数
    }

    /**
     * 创建ViewHolder
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return ViewHolder实例
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 创建ViewHolder
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_text_keyboard_button, parent, false)); // 加载布局并创建Holder
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
     * @return 按键数量
     */
    @Override
    public int getItemCount() { // 获取数量
        return list.size(); // 返回列表大小
    }

    /**
     * ViewHolder内部类，持有按键Button
     */
    class Holder extends RecyclerView.ViewHolder { // ViewHolder内部类
        Button button; // 按键按钮

        /**
         * 构造函数
         * @param itemView 列表项视图
         */
        public Holder(View itemView) { // 构造函数
            super(itemView); // 调用父类构造
            button = (Button) itemView.findViewById(R.id.text); // 获取按钮
        }

        /**
         * 绑定按键数据到视图，根据按键类型设置不同的点击行为
         * @param item 按键数据项
         */
        public void bind(final KeyBoardTextItem item) { // 绑定数据方法
            switch (item.getIndex()) { // 根据按键索引设置文字大小
                case INDEX_PLACEHOLDER: // 占位键
                case INDEX_ENTER: // 确认键
                case INDEX_HIDE: // 隐藏键
                case INDEX_NUMBER: // 数字切换键
                case INDEX_SYMBOL: // 符号切换键
                case INDEX_LANG: // 语言切换键
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20); // 功能键使用较小字号20px
                    break;
                default: // 其他按键
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24); // 普通键使用较大字号24px
                    break;
            }

            itemView.setVisibility(View.VISIBLE); // 默认可见
            itemView.setClickable(true); // 默认可点击
            button.setBackgroundResource(R.drawable.selector_rightslip_button); // 设置按钮背景
            if (item.getIndex() == INDEX_DELETE) { // 删除键
                button.setText("←"); // 显示左箭头
                button.setBackgroundResource(R.drawable.selector_rightslip_button); // 设置背景
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onDelete(); // 回调删除
                        }
                    }
                });
                button.setOnLongClickListener(new View.OnLongClickListener() { // 长按监听
                    @Override
                    public boolean onLongClick(View v) { // 长按回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onQuickDelete(); // 回调快速删除
                        }
                        return false; // 不消费长按事件
                    }
                });
            } else if (item.getIndex() == INDEX_PLACEHOLDER) { // 占位键
                itemView.setVisibility(View.INVISIBLE); // 不可见
                itemView.setClickable(false); // 不可点击
            } else if (item.getIndex() == INDEX_ENTER) { // 确认键
                button.setText(item.getWord()); // 显示确认文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onEnter(); // 回调确认
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_UPPER) { // 大写切换键
                button.setText("↑"); // 显示上箭头
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        upper = !upper; // 切换大小写状态
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onUpper(upper); // 回调大写切换
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_HIDE) { // 隐藏键
                button.setText(item.getWord()); // 显示隐藏文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onHide(); // 回调隐藏
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_NUMBER) { // 数字切换键
                button.setText(item.getWord()); // 显示数字文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onNumber(); // 回调数字切换
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_SPACE) { // 空格键
                button.setText(" "); // 显示空格
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onOtherKey(" "); // 回调空格输入
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_SYMBOL) { // 符号切换键
                button.setText(item.getWord()); // 显示符号文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onSymbol(); // 回调符号切换
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_LANG) { // 语言切换键
                button.setText(item.getWord()); // 显示语言文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onEnglish(Objects.equals(item.getWord(), LANG_ENG)); // 回调语言切换（传入当前是否英文）
                        }
                    }
                });
            } else { // 其他普通按键
                button.setText(item.getWord()); // 显示按键文字
                button.setOnClickListener(new View.OnClickListener() { // 点击监听
                    @Override
                    public void onClick(View v) { // 点击回调
                        if (onItemClickListener != null) { // 监听器不为空
                            onItemClickListener.onOtherKey(item.getWord()); // 回调普通按键输入
                        }
                    }
                });
            }
        }
    }
}
