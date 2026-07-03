package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysNode;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.util.StrUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/3.
 */

/*
 * +=============================================================================+
 * |                          DialogRefRecall                                     |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 参考波形调出（Ref Recall）对话框                       |
 * | 核心职责 : 展示本地保存的参考波形文件列表，支持选择与调出；                        |
 * |           支持外部旋钮上下滚动与确认操作                                        |
 * | 架构设计 : 继承 AbsoluteLayout，以自定义 View 形式嵌入 MainViewGroup；           |
 * |           使用 RecyclerView + DialogRefRecallAdapter 展示文件列表              |
 * | 数据流向 : getList() 扫描参考波形文件 → 填充列表 → 用户选择/旋钮操作 →           |
 * |           OnDialogItemClickListener 回传选中项                                |
 * | 依赖关系 : RxBus（对话框开关+旋钮事件）、SaveManage（文件扫描）、                  |
 * |           DialogRefRecallAdapter/Bean（列表适配器/数据模型）、ExternalKeysNode   |
 * | 使用场景 : 在 Ref 通道设置菜单中，用户点击"调出"时弹出此对话框选择参考波形文件       |
 * +=============================================================================+
 */
public class DialogRefRecall extends AbsoluteLayout {
//    public static final int ACTION_REFRECALL_UP = -1;
//    public static final int ACTION_REFRECALL_FINISH = 0;
//    public static final int ACTION_REFRECALL_DOWN = 1;

    private View clickView;                                                     // 触发此对话框的源视图
    private Context context;                                                    // 上下文引用
    private RecyclerView recyclerView;                                          // 参考波形文件列表
    private DialogRefRecallAdapter adapter;                                     // 列表适配器
    private ArrayList<DialogRefRecallBean> list;                                // 参考波形文件数据列表
    private OnDialogItemClickListener onDialogItemClickListener;                // 列表项点击回调监听器

    private ViewGroup rootViewGroup;                                            // 布局根视图组

    /**
     * 对话框列表项点击监听接口
     * <p>当用户选中某个参考波形文件后回调</p>
     */
    public interface OnDialogItemClickListener {
        /** 列表项点击回调
         * @param clickView 触发对话框的源视图
         * @param bean      选中的参考波形数据Bean
         */
        void onItemClick(View clickView, DialogRefRecallBean bean);
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public DialogRefRecall(Context context) {
        this(context, null);                                                    // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogRefRecall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终构造入口）
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogRefRecall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                    // 调用父类构造
        this.context = context;                                                 // 保存上下文引用
        init();                                                                 // 执行初始化
    }

    //[481, 36]	220	500

    /**
     * 初始化对话框布局与交互
     * <p>加载布局、设置外部区域触摸关闭、初始化子视图、订阅旋钮事件</p>
     */
    private void init() {
        setClickable(true);                                                     // 设置可点击，拦截触摸事件
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_recall, this); // 加载对话框布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();                                                         // 触摸外部区域则隐藏对话框
                return false;                                                   // 不消费事件，继续传递
            }
        });
        initView(rootViewGroup);                                                // 初始化子视图控件
        hide();                                                                 // 默认隐藏对话框

        RxBus.getInstance().getObservable(RxEnum.DIALOG_REFRECALL_CHANGED).subscribe(consumerRefRecallChanged); // 订阅旋钮操作事件
    }

    /**
     * 初始化子视图控件及适配器
     * @param view 根视图
     */
    private void initView(View view) {
//        TextView back = (TextView) view.findViewById(R.id.back);
//        back.setOnClickListener(onClickListener);
        recyclerView = (RecyclerView) view.findViewById(R.id.listView);         // 获取 RecyclerView 列表控件
        recyclerView.setLayoutManager(new LinearLayoutManager(context));        // 设置线性布局管理器
        list = getList();                                                       // 扫描获取参考波形文件列表
        adapter = new DialogRefRecallAdapter(context, list);                    // 创建列表适配器
        adapter.setOnItemClickListener(onItemClickListener);                    // 设置列表项点击监听
        recyclerView.setAdapter(adapter);                                       // 绑定适配器到 RecyclerView
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    Logger.i("DialogRefRecall:newState:" + newState);
//                }
//            }
//        });
    }

    /**
     * 显示对话框
     * <p>设置可见性并通过 RxBus 发送对话框打开事件</p>
     */
    public void show() {
        setVisibility(VISIBLE);                                                 // 设置对话框可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_REFRECALL); // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogRefRecall",rootViewGroup);           // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     * <p>设置不可见并通过 RxBus 发送对话框关闭事件</p>
     */
    public void hide() {
        setVisibility(GONE);                                                    // 设置对话框不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_REFRECALL); // 发送对话框关闭事件
    }

    /**
     * 设置对话框数据并显示
     * @param clickView                触发此对话框的源视图
     * @param item                     当前选中项标题，用于高亮定位
     * @param onDialogItemClickListener 列表项点击回调监听器
     */
    public void setData(View clickView, String item, OnDialogItemClickListener onDialogItemClickListener) {
        this.clickView = clickView;                                             // 保存源视图引用
        this.onDialogItemClickListener = onDialogItemClickListener;             // 保存回调监听器
        //list.clear();
        list = getList();                                                       // 重新扫描获取参考波形文件列表
        if (!StrUtil.isEmpty(item)) {                                           // 如果当前有选中项
            for (DialogRefRecallBean bean : list) {                             // 遍历列表
                bean.setSelect(item.equals(bean.getTitle()));                   // 设置选中状态
                if (bean.isSelect()) {                                          // 如果是选中项
                    scrollToPos(bean.getIndex());                               // 滚动到选中项位置
                }
            }
        } else {                                                                // 无选中项
            for (DialogRefRecallBean bean : list) {                             // 遍历列表
                bean.setSelect(false);                                          // 全部取消选中
            }
        }
        adapter.setList(list);                                                  // 更新适配器数据
        adapter.notifyDataSetChanged();                                          // 通知适配器数据变化
        show();                                                                 // 显示对话框
    }

    /**
     * 仅滚动选中项（不触发确认），用于外部旋钮上下操作
     * @param isUp true=向上移动选中项，false=向下移动选中项
     */
    public void moveOnlyScroll(boolean isUp) {
        int index = 0;                                                          // 当前选中项索引
        for (int i = 0; i < list.size(); i++) {                                 // 遍历列表查找当前选中项
            DialogRefRecallBean bean = list.get(i);                             // 获取列表项
            if (bean.isSelect()) {                                              // 找到选中项
                index = bean.getIndex();                                        // 记录选中索引
                break;                                                          // 跳出循环
            }
        }
        if (isUp) {                                                             // 向上移动
            if (index != 0) {                                                   // 非首项
                index -= 1;                                                     // 索引减1
            } else {                                                            // 已是首项
                index = list.size() - 1;                                        // 循环到末项
            }
        } else {                                                                // 向下移动
            if (index != list.size() - 1) {                                     // 非末项
                index += 1;                                                     // 索引加1
            } else {                                                            // 已是末项
                index = 0;                                                      // 循环到首项
            }
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager(); // 获取布局管理器
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition(); // 获取首个完全可见项位置
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition();   // 获取末个完全可见项位置
        if (firstCompletely > index) {                                          // 目标在可视区域上方
            layoutManager.scrollToPositionWithOffset(index,0);                  // 滚动到目标位置
        } else if (lastCompletely < index) {                                    // 目标在可视区域下方
            layoutManager.scrollToPositionWithOffset(index - 9, -30);           // 滚动使目标可见（带偏移）
        }
        for (int i = 0; i < list.size(); i++) {                                 // 遍历列表更新选中状态
            DialogRefRecallBean bean = list.get(i);                             // 获取列表项
            bean.setSelect(bean.getIndex() == index);                           // 设置新的选中状态
            if (bean.isSelect()) {                                              // 如果是选中项
                scrollToPos(bean.getIndex());                                   // 精细滚动到选中项
            }
        }

        adapter.notifyDataSetChanged();                                          // 通知适配器数据变化

        confirm();                                                              // 触发确认回调
    }

    /**
     * 滚动列表到指定位置，使目标项居中显示
     * @param pos 目标位置索引
     */
    private void scrollToPos(int pos) {
        if (list.size() > 9 && pos > 4 && pos < list.size() - 5) {             // 列表超过9项且目标不在首尾附近
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager(); // 获取布局管理器
            linearLayoutManager.scrollToPositionWithOffset(pos, 225);           // 滚动到目标位置（偏移225像素使居中）
        }
    }

    /**
     * 扫描参考波形文件目录，构建文件列表
     * <p>读取 REFWAVE 目录下的文件，按修改时间倒序排列</p>
     * @return 参考波形数据列表
     */
    private ArrayList<DialogRefRecallBean> getList() {
        ArrayList<DialogRefRecallBean> list = new ArrayList<DialogRefRecallBean>(); // 创建结果列表
        File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_REFWAVE); // 从参考波形目录获取文件数组
        if (files == null) return list;                                         // 无文件则返回空列表
        for (int i = 0; i < files.length; i++) {                                // 遍历文件数组
            long time = files[i].lastModified();                                // 获取文件最后修改时间
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)); // 格式化修改时间
            DialogRefRecallBean item = new DialogRefRecallBean();               // 创建数据Bean
            item.setIndex(i);                                                   // 设置索引
            item.setLastModifyTime(time);                                       // 设置最后修改时间戳
            item.setPathFile(files[i].getAbsolutePath());                       // 设置文件绝对路径
            item.setSelect(false);                                              // 默认不选中
            item.setTime(ctime);                                                // 设置格式化后的时间字符串
            item.setTitle(files[i].getName().replace(".mwav", "").replace(".wav", "")); // 设置文件名（去除后缀）作为标题
            list.add(item);                                                     // 添加到列表
        }
        Collections.sort(list, new Comparator<DialogRefRecallBean>() {           // 按修改时间倒序排序

            @Override
            public int compare(DialogRefRecallBean o1, DialogRefRecallBean o2) {
                long i = o2.getLastModifyTime() - o1.getLastModifyTime();       // 比较修改时间（倒序）
                if (i == 0) {                                                   // 修改时间相同
                    return o2.getTitle().compareTo(o1.getTitle());              // 按标题字母倒序
                } else {                                                        // 修改时间不同
                    return Long.compare(o2.getLastModifyTime(), o1.getLastModifyTime()); // 按时间倒序
                }
            }
        });
        for (int i = 0; i < list.size(); i++) {                                 // 排序后重新设置索引
            DialogRefRecallBean item = list.get(i);                             // 获取列表项
            item.setIndex(i);                                                   // 更新索引为排序后的位置
        }
        return list;                                                            // 返回排好序的列表
    }

    /**
     * 确认当前选中项，触发外部回调
     * <p>查找当前选中项并回调 OnDialogItemClickListener</p>
     */
    public void confirm() {
        if (onDialogItemClickListener != null) {                                // 如果回调监听器存在
            DialogRefRecallBean bean = null;                                    // 选中的Bean
            for (DialogRefRecallBean item : list) {                             // 遍历列表
                if (item.isSelect()) {                                          // 找到选中项
                    bean = item;                                                // 记录选中Bean
                    break;                                                      // 跳出循环
                }
            }
            onDialogItemClickListener.onItemClick(clickView, bean);            // 回调选中项
        }
    }

    /**
     * 旋钮操作事件消费者
     * <p>处理外部旋钮的上移、下移、确认操作</p>
     */
    private Consumer<String> consumerRefRecallChanged = new Consumer<String>() {
        @Override
        public void accept(String recallInfo) throws Exception {
            String[] params = recallInfo.split(CommandMsgToUI.PARAM_SPLIT);     // 拆分参数
            int integer = Integer.parseInt(params[0]);                          // 解析操作类型
            int channelNumber = Integer.parseInt(params[1]);                    // 解析通道编号
            switch (integer) {                                                  // 根据操作类型分发
                case ExternalKeysNode.ACTION_REFRECALL_FINISH:                  // 确认操作
                    DialogRefRecallBean bean = null;                            // 选中的Bean
                    for (DialogRefRecallBean item : list) {                     // 遍历列表
                        if (item.isSelect()) {                                  // 找到选中项
                            bean = item;                                        // 记录选中Bean
                            break;                                              // 跳出循环
                        }
                    }
                    if (bean != null) {                                         // 如果有选中项
                        onItemClickListener.onItemClick(bean);                  // 触发列表项点击逻辑
                    }
                    hide();                                                     // 隐藏对话框
                    break;                                                      // 结束确认操作分支
                case ExternalKeysNode.ACTION_REFRECALL_UP:                      // 上移操作
                    moveOnlyScroll(true);                                       // 向上滚动选中项
                    break;                                                      // 结束上移操作分支
                case ExternalKeysNode.ACTION_REFRECALL_DOWN:                    // 下移操作
                    moveOnlyScroll(false);                                      // 向下滚动选中项
                    break;                                                      // 结束下移操作分支
            }
        }
    };

//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            PlaySound.getInstance().playButton();
//            hide();
//        }
//    };

    /**
     * 列表项点击监听器
     * <p>点击后设置选中状态、播放音效、确认回调、隐藏对话框</p>
     */
    private DialogRefRecallAdapter.OnItemClickListener onItemClickListener = new DialogRefRecallAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(DialogRefRecallBean item) {
            PlaySound.getInstance().playButton();                               // 播放按钮音效
            for (DialogRefRecallBean bean : list) {                             // 遍历列表
                bean.setSelect(bean.getIndex() == item.getIndex());             // 更新选中状态
                if (bean.isSelect()) {                                          // 如果是选中项
                    confirm();                                                  // 触发确认回调
                }
            }

            adapter.notifyDataSetChanged();                                      // 通知适配器数据变化

            hide();                                                             // 隐藏对话框
        }
    };
}
