package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

import android.content.Context; // 导入上下文类
import android.util.AttributeSet; // 导入属性集类
import android.view.View; // 导入视图基类
import android.widget.Button; // 导入按钮类
import android.widget.RelativeLayout; // 导入相对布局类
import android.widget.TextView; // 导入文本视图类

import androidx.annotation.Nullable; // 导入可空注解类
import androidx.recyclerview.widget.LinearLayoutManager; // 导入线性布局管理器类
import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView类

import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import java.util.ArrayList; // 导入数组列表类
import java.util.List; // 导入列表类

import io.reactivex.rxjava3.functions.Consumer; // 导入Rx消费者接口


/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 拼音候选词选择面板                            │
 * │ 核心职责：显示拼音输入的候选词列表，支持左右滚动和选择               │
 * │ 架构设计：继承RelativeLayout，内嵌RecyclerView横向列表              │
 * │          通过RxBus接收旋钮左右/确认事件                            │
 * │ 数据流向：PinyinIME → setData → RecyclerView → selectItem → 回调  │
 * │ 依赖关系：PinyinIME, CandidatesWordAdapter, RxBus                  │
 * │ 使用场景：中文拼音输入时显示候选词，支持旋钮和触摸操作               │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/6.
 */

public class TopDialogCandidatesWord extends RelativeLayout { // 候选词选择面板
    public static final int ACTION_CANDIDATES_LEFT = -1; // 旋钮左移动作常量
    public static final int ACTION_CANDIDATES_RIGHT = 1; // 旋钮右移动作常量
    public static final int ACTION_CANDIDATES_FINISH = 0; // 旋钮确认动作常量

    private Context context; // 上下文对象
    private PinyinIME pinyinIME; // 拼音输入法引擎
    private TextView wordTextView; // 拼音显示文本
    private Button nextView; // 翻页按钮
    private RecyclerView choiceRecyclerView; // 候选词列表
    private CandidatesWordAdapter adapter; // 候选词适配器
    private List<KeyBoardCandidatesItem> list = new ArrayList<>(); // 候选词数据列表
    private OnDialogClickWordListener onDialogClickWordListener; // 候选词选择回调

    /**
     * 候选词选择回调接口
     */
    interface OnDialogClickWordListener { // 候选词选择回调接口
        /**
         * 候选词被选中时回调
         * @param s 选中的文字
         * @return 如果此次设置不成功，则返回false，否则返回true
         */
        boolean onClickWord(String s); // 候选词选中回调
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogCandidatesWord(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogCandidatesWord(Context context, @Nullable AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogCandidatesWord(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() { // 初始化方法
        setBackgroundResource(R.drawable.shape_frame_bg_black); // 设置黑色边框背景
        View view = inflate(context, R.layout.dialog_candidatesword, this); // 加载候选词布局
        wordTextView = (TextView) view.findViewById(R.id.word); // 获取拼音显示文本
        nextView = (Button) view.findViewById(R.id.nextView); // 获取翻页按钮
        choiceRecyclerView = (RecyclerView) view.findViewById(R.id.choiceRecyclerView); // 获取候选词列表

        choiceRecyclerView.requestFocus(); // 请求焦点
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false); // 创建水平线性布局管理器
        choiceRecyclerView.setLayoutManager(layoutManager); // 设置布局管理器
        adapter = new CandidatesWordAdapter(context, list); // 创建适配器
        adapter.setOnCandidatesWordListener(onCandidatesWordListener); // 设置候选词点击监听
        choiceRecyclerView.setAdapter(adapter); // 设置适配器
        hide(); // 初始隐藏

        nextView.setOnClickListener(onClickListener); // 设置翻页按钮点击监听
        pinyinIME = new PinyinIME(context); // 创建拼音输入法引擎
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CANDIDATE_CHANGED).subscribe(consumerCandidate); // 订阅旋钮候选词变化事件
    }

    /**
     * 设置拼音和回调，查询候选词并显示
     * @param pinyin 输入的拼音
     * @param onDialogClickWordListener 候选词选择回调
     */
    public void setData(String pinyin, OnDialogClickWordListener onDialogClickWordListener) { // 设置数据方法
        wordTextView.setText(pinyin); // 显示拼音
        this.onDialogClickWordListener = onDialogClickWordListener; // 保存回调
        list.clear(); // 清空候选词列表
        List<String> stringList = pinyinIME.getChoiceList(pinyin); // 查询候选词
        for (int i = 0; i < stringList.size(); i++) { // 遍历候选词
            list.add(new KeyBoardCandidatesItem(i, stringList.get(i), i == 0)); // 创建候选词数据项（第一个选中）
        }
        choiceRecyclerView.getLayoutManager().scrollToPosition(0); // 滚动到起始位置
        adapter.notifyDataSetChanged(); // 通知数据变化
        if (StrUtil.isEmpty(pinyin)) { // 如果拼音为空
            hide(); // 隐藏面板
        } else { // 拼音非空
            show(); // 显示面板
        }
    }

    /**
     * 获取当前显示的拼音
     * @return 拼音字符串
     */
    public String getPinyin() { // 获取拼音方法
        return wordTextView.getText().toString(); // 返回拼音文本
    }

    /**
     * 隐藏候选词面板
     */
    public void hide() { // 隐藏方法
        wordTextView.setText(""); // 清空拼音
        list.clear(); // 清空候选词列表
        adapter.notifyDataSetChanged(); // 通知数据变化
        setVisibility(GONE); // 设置不可见
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TEXTKEYBOARD_CANDIDATESWORD); // 发送控件可见性变化事件
    }

    /**
     * 显示候选词面板
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TEXTKEYBOARD_CANDIDATESWORD); // 发送控件可见性变化事件
    }

    /**
     * 判断候选词面板是否正在显示
     * @return true表示正在显示
     */
    public boolean isShowing() { // 判断是否显示方法
        return getVisibility() == VISIBLE; // 返回可见性
    }

    /**
     * 仅滚动选中项，不触发选择
     * @param recyclerView 候选词列表
     * @param isRight 是否向右滚动
     */
    private void moveOnlyScroll(RecyclerView recyclerView, boolean isRight) { // 仅滚动选中项方法
        int index = 0; // 当前选中索引
        for (KeyBoardCandidatesItem item : list) { // 遍历候选词
            if (item.isSelect()) { // 如果是选中项
                index = item.getIndex(); // 获取索引
                break; // 跳出循环
            }
        }
        if (isRight) { // 向右滚动
            if (index != list.size() - 1) { // 不是最后一个
                index += 1; // 右移一位
            } else { // 是最后一个
                index = 0; // 循环到第一个
            }
        } else { // 向左滚动
            if (index != 0) { // 不是第一个
                index -= 1; // 左移一位
            } else { // 是第一个
                index = list.size() - 1; // 循环到最后一个
            }
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager(); // 获取布局管理器
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition(); // 获取第一个完全可见项位置
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition(); // 获取最后一个完全可见项位置
        if (firstCompletely > index) { // 如果选中项在可见区域之前
            layoutManager.scrollToPosition(index); // 滚动到选中项
        } else if (lastCompletely < index) { // 如果选中项在可见区域之后
            layoutManager.scrollToPosition(index); // 滚动到选中项
        }
        for (KeyBoardCandidatesItem item : list) { // 遍历候选词
            item.setSelect(item.getIndex() == index); // 更新选中状态
        }
        adapter.notifyDataSetChanged(); // 通知数据变化
    }

    /**
     * 选中一个候选词，触发联想词查询
     * @param item 选中的候选词数据项
     */
    private void selectItem(KeyBoardCandidatesItem item) { // 选中候选词方法
        if (onDialogClickWordListener != null) { // 如果回调不为空
            boolean value = onDialogClickWordListener.onClickWord(item.getText()); // 回调选中文字
            if (!value) return; // 如果回调返回false，不继续
        }
        wordTextView.setText(""); // 清空拼音
        list.clear(); // 清空候选词列表
        List<String> stringList = pinyinIME.getPredictList(item.getText()); // 查询联想词
        for (int i = 0; i < stringList.size(); i++) { // 遍历联想词
            list.add(new KeyBoardCandidatesItem(i, stringList.get(i), i == 0)); // 创建联想词数据项
        }
        choiceRecyclerView.getLayoutManager().scrollToPosition(0); // 滚动到起始位置
        adapter.notifyDataSetChanged(); // 通知数据变化
        if (list.size() == 0) { // 如果没有联想词
            hide(); // 隐藏面板
        }
    }

    /**
     * 翻页按钮点击监听器
     */
    private OnClickListener onClickListener = new OnClickListener() { // 翻页按钮监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            moveOnlyScroll(choiceRecyclerView, true); // 向右滚动
        }
    };

    /**
     * 旋钮候选词变化事件消费者
     */
    private Consumer<Integer> consumerCandidate = new Consumer<Integer>() { // 旋钮事件消费者
        @Override
        public void accept(Integer integer) throws Exception { // 接收事件
            if (TopDialogCandidatesWord.this.getVisibility() != VISIBLE) { // 如果面板不可见
                return; // 不处理
            }
            if (integer == ACTION_CANDIDATES_LEFT) { // 左移
                moveOnlyScroll(choiceRecyclerView, false); // 向左滚动
            } else if (integer == ACTION_CANDIDATES_RIGHT) { // 右移
                moveOnlyScroll(choiceRecyclerView, true); // 向右滚动
            } else if (integer == ACTION_CANDIDATES_FINISH) { // 确认
                int index = 0; // 选中索引
                for (int i = 0; i < list.size(); i++) { // 遍历候选词
                    if (list.get(i).isSelect()) { // 如果是选中项
                        index = i; // 记录索引
                        break; // 跳出循环
                    }
                }
                selectItem(list.get(index)); // 选中该项
            }
        }
    };

    /**
     * 候选词点击监听器
     */
    private CandidatesWordAdapter.OnCandidatesWordListener onCandidatesWordListener = new CandidatesWordAdapter.OnCandidatesWordListener() { // 候选词点击监听器
        @Override
        public void onCandidateWord(KeyBoardCandidatesItem item) { // 候选词点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            selectItem(item); // 选中该候选词
        }
    };
}
