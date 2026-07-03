package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.util.ViewUtils;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;

/*
 * +----------------------------------------------------------------------+
 * |                      DialogChannelLabel                              |
 * |                     通道标签设置对话框                                 |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供示波器通道标签的网格选择界面，                              |
 * |          支持预设标签选择和自定义标签输入（通过文本键盘）                    |
 * | 架构设计: 继承RelativeLayout的自定义对话框视图，                          |
 * |          使用RecyclerView+GridLayoutManager展示4列标签网格，             |
 * |          DialogChannelLabelAdapter负责列表项渲染和交互                   |
 * | 数据流向: 外部调用setData()传入通道号/当前标签 -> 用户选择/自定义 ->       |
 * |          OnDismissListener回调返回最终标签字符串                         |
 * | 依赖关系: RxBus(对话框开关事件), CacheUtil(自定义标签缓存),               |
 * |          TopDialogTextKeyBoard(文本键盘), BitmapUtil(通道颜色),         |
 * |          DialogChannelLabelAdapter(网格适配器)                         |
 * | 使用场景: 通道标签设置菜单项点击后弹出，用于设置通道显示名称                |
 * +----------------------------------------------------------------------+
 */
public class DialogChannelLabel extends RelativeLayout {
    public static final int INDEX_USERDEFINE = 1;  // 自定义标签在列表中的索引位置
    public static final int FROM_CHANNEL = 1;  // 来源标识：通道菜单
    public static final int FROM_MATHREF = 2;  // 来源标识：Math/Ref菜单

    private Context context;  // 上下文引用
    private String cacheKey;  // 缓存键，用于存取自定义标签
    private OnDismissListener onDismissListener;  // 对话框关闭回调监听器
    private DialogChannelLabelAdapter adapter;  // RecyclerView适配器
    private ArrayList<RightBeanSelect> list = new ArrayList<>();  // 标签数据列表
    private RadioButton tvUserDefine;  // 自定义标签单选按钮
    private TopDialogTextKeyBoard dialogTextKeyBoard;  // 文本键盘对话框（懒加载）
    private RelativeLayout rlContentView;  // 内容区域布局
    private View maskView;  // 遮罩视图

    private ViewGroup rootViewGroup;  // 根视图组

    /**
     * 对话框关闭监听接口。
     * <p>
     * 当用户选择标签后关闭对话框时回调onDismiss返回结果。
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回最终标签字符串（空串表示None） */
        void onDismiss(String result);
    }

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogChannelLabel(Context context) {
        this(context, null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogChannelLabel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并调用init()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogChannelLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;               // 保存上下文引用
        init(context, attrs, defStyleAttr);   // 执行初始化
    }

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_channellabel布局，设置外部区域点击关闭监听，
     * 初始化内部视图控件，然后隐藏对话框。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_channellabel, this);  // 填充布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();      // 隐藏对话框
                return false;  // 不消费触摸事件
            }
        });
        initView(rootViewGroup);  // 初始化内部视图控件
        hide();  // 默认隐藏对话框
    }


    /**
     * 初始化内部视图控件，配置RecyclerView网格布局和适配器。
     *
     * @param view 根视图，用于查找子控件
     */
    private void initView(View view) {
        tvUserDefine = view.findViewById(R.id.userDefine);  // 自定义标签单选按钮
        tvUserDefine.setText("(" + getResources().getString(R.string.serialsUserDefine) + ")");  // 设置自定义标签提示文字
//        tvUserDefine.setText("");

        rlContentView = findViewById(R.id.rl_contentView);  // 内容区域布局
        maskView = findViewById(R.id.maskView);  // 遮罩视图

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);  // 标签网格RecyclerView
        initList();  // 初始化标签数据列表
        adapter = new DialogChannelLabelAdapter(getContext(), list, onItemClickListener);  // 创建适配器
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);  // 4列网格布局
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {  // 设置跨度查找
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position);  // None和自定义项占2列，其他占1列
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);  // 设置布局管理器
        recyclerView.setAdapter(adapter);  // 设置适配器
    }

    /**
     * 初始化标签数据列表，从字符串数组资源加载预设标签。
     */
    private void initList() {
        list.clear();  // 清空列表
        String[] stringArray = App.get().getResources().getStringArray(R.array.channelLabel);  // 从资源获取标签数组
        for (int i = 0; i < stringArray.length; i++) {  // 遍历标签数组
            list.add(new RightBeanSelect(i, stringArray[i], i == 0));  // 添加到列表，默认选中第一项（None）
        }
    }

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_CHANNELLABEL);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogChannelLabel",rootViewGroup);  // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_CHANNELLABEL);  // 发送对话框关闭事件
    }

    /**
     * 根据通道索引设置自定义按钮的背景色和文字颜色。
     *
     * @param chIdx 通道索引
     */
    @SuppressLint("ResourceType")
    private void setControlColorByChIdx(int chIdx){
        // 以下注释为旧版按通道分switch设置颜色的逻辑，已替换为BitmapUtil通用方案
//        switch (chIdx){
//            case 0: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch1;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch1);
//                break;
//            case 1: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch2;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch2);
//                break;
//            case 2: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch3;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch3);
//                break;
//            case 3: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch4;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch4);
//                break;
//            case 4: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_math;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_math);
//                break;
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//                itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ref;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ref);
//                break;
//            case 9: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_s1;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s1);
//                break;
//            case 10: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_s2;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s2);
//                break;
//        }

        StateListDrawable d= BitmapUtil.genSelectorDrawable(context,chIdx);  // 生成通道对应的选择器背景
        ColorStateList itemTextColorResId =BitmapUtil.genSelectorColor(context,chIdx);  // 生成通道对应的文字颜色
        tvUserDefine.setBackground(d);  // 设置自定义按钮背景
        tvUserDefine.setTextColor(itemTextColorResId);  // 设置自定义按钮文字颜色
    }

    /**
     * 根据显示类型设置对话框和遮罩的位置与尺寸。
     * <p>
     * 通道标签和Math/Ref标签的弹窗位置不同。
     *
     * @param showType 显示类型（FROM_CHANNEL=通道, FROM_MATHREF=Math/Ref）
     */
    public void setViewPosition(int showType) {
        int contentX, contentY, maskWidth, maskHeight, maskX, maskY;  // 内容区域和遮罩的位置尺寸
        switch (showType) {  // 根据显示类型选择布局参数
            case FROM_MATHREF:  // Math/Ref菜单来源
                contentX = 1167;  // 内容X坐标
                contentY = 280;   // 内容Y坐标
                maskWidth = 819;  // 遮罩宽度（RightLayoutRef宽）
                maskHeight = 907; // 遮罩高度（RightLayoutRef高）
                maskX = 980;      // 遮罩X坐标（RightLayoutRef layoutX）
                maskY = 210;      // 遮罩Y坐标（RightLayoutRef layoutY）
                break;
            default://channel 默认  // 通道菜单来源
                contentX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);  // 内容X坐标
//                contentY = (int) context.getResources().getDimension(R.dimen.newRightDialogY);
                contentY = (int) context.getResources().getDimension(R.dimen.rightDialogY);  // 内容Y坐标
                maskWidth = (int) context.getResources().getDimension(R.dimen.rightSlipChannelWidth);  // 遮罩宽度
                maskHeight = (int) context.getResources().getDimension(R.dimen.rightDialogHeight);  // 遮罩高度
                maskX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);  // 遮罩X坐标
//                maskY = (int) context.getResources().getDimension(R.dimen.newRightDialogY);
                maskY = (int) context.getResources().getDimension(R.dimen.rightDialogY);  // 遮罩Y坐标
                break;
        }

        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) rlContentView.getLayoutParams();  // 获取内容区域布局参数
        layoutParams.x = contentX;  // 设置X坐标
        layoutParams.y = contentY;  // 设置Y坐标
        rlContentView.setLayoutParams(layoutParams);  // 应用布局参数

        AbsoluteLayout.LayoutParams layoutParams1 = (AbsoluteLayout.LayoutParams) maskView.getLayoutParams();  // 获取遮罩布局参数
        layoutParams1.width = maskWidth;   // 设置遮罩宽度
        layoutParams1.height = maskHeight;  // 设置遮罩高度
        layoutParams1.x = maskX;           // 设置遮罩X坐标
        layoutParams1.y = maskY;           // 设置遮罩Y坐标
        maskView.setLayoutParams(layoutParams1);  // 应用布局参数
    }

    /**
     * 设置对话框数据并显示（带显示类型参数）。
     * <p>
     * 先根据showType设置弹窗位置，再设置数据和显示。
     *
     * @param chanNum           通道序号
     * @param preString         当前标签字符串
     * @param cacheKey          自定义标签缓存键
     * @param showType          显示类型（FROM_CHANNEL/FROM_MATHREF）
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(int chanNum, String preString, String cacheKey,
                        int showType, OnDismissListener onDismissListener) {
        setViewPosition(showType);  // 设置弹窗位置
        setData(chanNum, preString, cacheKey, onDismissListener);  // 设置数据并显示
    }

    /**
     * 设置对话框数据并显示（核心逻辑）。
     * <p>
     * 初始化标签列表，根据当前标签值设置选中状态，
     * 处理自定义标签的缓存恢复和选中逻辑。
     *
     * @param chanNum           通道序号，用于设置颜色
     * @param preString         当前标签字符串
     * @param cacheKey          自定义标签缓存键
     * @param onDismissListener 关闭回调监听器
     */
    private void setData(int chanNum, String preString, String cacheKey, OnDismissListener onDismissListener) {
        initList();  // 重新初始化标签列表
        setControlColorByChIdx(chanNum);  // 设置自定义按钮颜色
        adapter.setControlColorByChIdx(chanNum);  // 设置适配器中的通道颜色
        String string = CacheUtil.get().getString(cacheKey);  // 从缓存读取自定义标签
        if (!StrUtil.isEmpty(string)) {  // 缓存中有自定义标签
            list.get(INDEX_USERDEFINE).setText(string);  // 设置自定义标签文本
//            tvUserDefine.setVisibility(GONE);
            tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);  // 左对齐（自定义标签文字较长）
        } else {  // 缓存中没有自定义标签
            list.get(INDEX_USERDEFINE).setText("");  // 清空自定义标签文本
//            tvUserDefine.setVisibility(VISIBLE);
            tvUserDefine.setGravity(Gravity.CENTER);  // 居中（占位文字较短）
        }
        this.cacheKey = cacheKey;  // 保存缓存键
        int index=0;  // 选中项索引
        if (StrUtil.isEmpty(preString)) {  // 当前标签为空
            for (int i = 0; i < list.size(); i++) {  // 遍历标签列表
                if (i == 0) {//选中none  // None项设为选中
                    list.get(i).setCheck(true);  // 选中None
                    index=i;  // 记录索引
                    list.get(INDEX_USERDEFINE).setText("");  // 清空自定义标签
                    tvUserDefine.setGravity(Gravity.CENTER);  // 居中
                } else {  // 其他项取消选中
                    list.get(i).setCheck(false);  // 取消选中
                }
            }
        } else {  // 当前标签非空
            for (int i = 0; i < list.size(); i++) {  // 遍历标签列表
                if (list.get(i).getText().equalsIgnoreCase(preString)) {  // 文本匹配（忽略大小写）
                    list.get(i).setCheck(true);  // 选中匹配项
                    index = i;  // 记录索引
                    if (index == 0) break;  // 如果匹配None，无需继续
                } else {  // 不匹配
                    list.get(i).setCheck(false);  // 取消选中
                }
            }

            if (index == 0) {  // 没有匹配的预设项（index仍为0说明不是None）
//                onTextKeyBoardDismissListener.onDismiss(preString);
                index = 1;  // 强制设为自定义标签索引
                list.get(0).setCheck(false);  // 取消None的选中
            }
            if (index != 1) {  // 选中的不是自定义项
                list.get(INDEX_USERDEFINE).setText("");  // 清空自定义标签
                tvUserDefine.setGravity(Gravity.CENTER);  // 居中
            } else {  // 选中的是自定义项
                tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);  // 左对齐
            }
        }

        if (index==1){  // 当前选中自定义标签
            tvUserDefine.setChecked(true);  // 自定义按钮选中
        }else {  // 当前选中的是预设标签
            tvUserDefine.setChecked(false);  // 自定义按钮取消选中
        }

        boolean checkNumber = false;  // 标记列表中是否有选中项
        for (int i = 0; i < list.size(); i++) {  // 遍历标签列表
            if (list.get(i).isCheck()) {  // 发现选中项
                checkNumber = true;  // 标记有选中项
            }
        }

        if (!checkNumber) {  // 列表中没有选中项（异常情况）
            if (StrUtil.isEmpty(preString)) {  // 当前标签为空
                list.get(0).setCheck(true);  // 默认选中None
            } else {  // 当前标签非空
                list.get(INDEX_USERDEFINE).setCheck(true);  // 选中自定义项
                list.get(INDEX_USERDEFINE).setText(preString);  // 设置自定义标签文本
                CacheUtil.get().putMap(cacheKey, preString);  // 缓存自定义标签
            }
        }
        adapter.notifyDataSetChanged();  // 通知适配器数据变更
        this.onDismissListener = onDismissListener;  // 保存关闭监听器
        show();  // 显示对话框
    }

    /**
     * 标签项点击监听器。
     * <p>
     * 点击None（索引0）时直接回调空串并关闭；
     * 点击自定义（索引1）时弹出文本键盘输入；
     * 点击其他预设项时直接回调标签文本并关闭。
     */
    private DialogChannelLabelAdapter.OnItemClickListener onItemClickListener = new DialogChannelLabelAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, RightBeanSelect item) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);  // 获取RecyclerView
            Logger.d("DialogChannelLabel");  // 调试日志
            for (int i = 0; i < recyclerView.getChildCount(); i++) {  // 遍历子视图
                Screen.getViewLocation(recyclerView.getChildAt(i));  // 打印子视图位置信息
            }
            Logger.d("DialogChannelLabel:"+item.getIndex());  // 打印点击项索引
            boolean b=false;  // 临时变量（未使用）
            for (int i = 0; i < list.size(); i++) {  // 遍历标签列表
                b=i==item.getIndex();  // 判断是否为当前点击项
                list.get(i).setCheck(i == item.getIndex());  // 仅选中当前点击项
            }
            if (item.getIndex()==1){  // 点击自定义项
                tvUserDefine.setChecked(true);  // 自定义按钮选中
            }else {  // 点击非自定义项
                tvUserDefine.setChecked(false);  // 自定义按钮取消选中
            }
            adapter.notifyDataSetChanged();  // 通知适配器数据变更
            if (onDismissListener != null) {  // 如果设置了关闭监听器
                if (item.getIndex() == 0) {  // 点击None项
                    onDismissListener.onDismiss("");  // 回调空串
                    hide();  // 隐藏对话框
                } else if (item.getIndex() == 1) {  // 点击自定义项
                    if (dialogTextKeyBoard == null) {  // 文本键盘对话框懒加载
                        dialogTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);  // 从MainActivity获取
                    }
                    dialogTextKeyBoard.setData(item.getText(), TopDialogTextKeyBoard.HANDLE_TYPE_CHANNEL_LABEL, TopDialogTextKeyBoard.INPUT_TYPE_ALL, 30, onTextKeyBoardDismissListener);  // 设置键盘数据（最大30字符）
                } else {  // 点击预设标签项
                    onDismissListener.onDismiss(item.getText());  // 回调标签文本
                    hide();  // 隐藏对话框
                }
            }
        }
    };

    /**
     * 文本键盘对话框关闭监听器。
     * <p>
     * 当文本键盘关闭时，根据输入结果更新自定义标签状态：
     * - 空输入则恢复默认占位显示
     * - 非空输入则更新自定义标签文本并缓存
     * 最终回调onDismiss并关闭对话框。
     */
    private TopDialogTextKeyBoard.OnDialogDismissListener onTextKeyBoardDismissListener = new TopDialogTextKeyBoard.OnDialogDismissListener() {
        @Override
        public void onDismiss(String result) {
            if (onDismissListener != null) {  // 如果设置了关闭监听器
                if (StrUtil.isEmpty(result)) {  // 输入为空
//                    tvUserDefine.setVisibility(VISIBLE);
                    tvUserDefine.setGravity(Gravity.CENTER);  // 居中（占位文字）
                } else {  // 输入非空
//                    tvUserDefine.setVisibility(GONE);
                    tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);  // 左对齐
                }
                list.get(INDEX_USERDEFINE).setText(result);  // 更新自定义标签文本
                CacheUtil.get().putMap(cacheKey, result);  // 缓存自定义标签
                adapter.notifyDataSetChanged();  // 通知适配器数据变更
                onDismissListener.onDismiss(result);  // 回调结果
                hide();  // 隐藏对话框
            }
        }
    };
}
