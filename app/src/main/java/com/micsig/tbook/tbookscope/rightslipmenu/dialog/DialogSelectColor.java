package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 * +=============================================================================+
 * |                        DialogSelectColor                                     |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 通道颜色选择对话框                                     |
 * | 核心职责 : 提供4x4网格颜色选择面板，供用户选择通道/数学/参考波形的显示颜色           |
 * | 架构设计 : 继承 ConstraintLayout，使用 RecyclerView + SelectColorAdapter          |
 * |           以网格形式展示可选颜色；支持两种弹出位置（通道菜单 / MathRef菜单）         |
 * | 数据流向 : setData() 接收通道索引与回调 → 用户选择颜色 →                          |
 * |           OnDismissListener 回传通道索引与颜色值 → handleSelectColor() 更新全局    |
 * | 依赖关系 : RxBus（对话框开关）、SvgNodeInfo（颜色映射/设置）、                     |
 * |           SelectColorAdapter（颜色网格适配器）、CacheUtil（颜色缓存）              |
 * | 使用场景 : 在通道/数学/参考菜单中点击颜色项时弹出，选择该通道的显示颜色              |
 * +=============================================================================+
 */
public class DialogSelectColor extends ConstraintLayout {

    private static final String TAG = "DialogSelectColor";                      // 日志标签
    public static final int FROM_CHANNEL = 1;                                   // 来源标识：模拟通道菜单
    public static final int FROM_MATHREF = 2;                                   // 来源标识：数学/参考菜单
    private final Context context;                                              // 上下文引用
    private ConstraintLayout rootViewGroup;                                     // 布局根视图组
    private RecyclerView recyclerView;                                          // 颜色网格列表
    private SelectColorAdapter colorAdapter;                                    // 颜色网格适配器
    private OnDismissListener onDismissListener;                                // 对话框关闭回调监听器
    private int chIndex;                                                        // 当前操作的通道索引
    private View maskView;                                                      // 背景遮罩视图
    private RelativeLayout rlContentView;                                       // 内容区域容器

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public DialogSelectColor(Context context) {
        this(context, null);                                                    // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogSelectColor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终构造入口）
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogSelectColor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                    // 调用父类构造
        this.context = context;                                                 // 保存上下文引用
        initView();                                                             // 执行视图初始化
    }

    /**
     * 对话框关闭监听接口
     * <p>用户选择颜色后，通过此接口回传通道索引与颜色值</p>
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调
         * @param chIndex  通道索引
         * @param colorStr 选中的颜色字符串
         */
        void onDismiss(int chIndex, String colorStr);
    }

    /**
     * 初始化视图控件与适配器
     * <p>加载布局、设置外部区域点击关闭、初始化颜色网格 RecyclerView</p>
     */
    private void initView() {
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_select_color, this); // 加载对话框布局
        View outView = rootViewGroup.findViewById(R.id.outView);                // 获取外部区域视图
        outView.setOnClickListener(onClickListener);                            // 设置外部区域点击监听
        recyclerView = rootViewGroup.findViewById(R.id.recyclerView);           // 获取 RecyclerView 控件
        colorAdapter = new SelectColorAdapter(context, createColors(), itemClickListener); // 创建颜色适配器
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4); // 创建4列网格布局管理器
        recyclerView.setAdapter(colorAdapter);                                  // 绑定适配器
        recyclerView.setLayoutManager(gridLayoutManager);                       // 设置布局管理器

        rlContentView = findViewById(R.id.rl_contentView);                     // 获取内容区域容器
        maskView = findViewById(R.id.maskView);                                 // 获取背景遮罩视图
    }

    /**
     * 创建可选颜色列表
     * <p>从 SvgNodeInfo 获取8个数学通道默认色 + 8个参考通道默认色，共16种颜色</p>
     * @return 颜色字符串列表
     */
    private ArrayList<String> createColors() {
        ArrayList<String> colors = new ArrayList<>();                           // 创建颜色列表
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math1));                   // 添加 Math1 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math2));                   // 添加 Math2 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math3));                   // 添加 Math3 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math4));                   // 添加 Math4 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math5));                   // 添加 Math5 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math6));                   // 添加 Math6 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math7));                   // 添加 Math7 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math8));                   // 添加 Math8 通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R1));                      // 添加 R1 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R2));                      // 添加 R2 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R3));                      // 添加 R3 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R4));                      // 添加 R4 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R5));                      // 添加 R5 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R6));                      // 添加 R6 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R7));                      // 添加 R7 参考通道默认颜色
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R8));                      // 添加 R8 参考通道默认颜色
        return colors;                                                          // 返回颜色列表
    }

    /**
     * 全局点击监听器，处理外部区域点击关闭对话框
     */
    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {                                                    // 根据点击视图ID分发
            case R.id.outView:                                                  // 点击外部区域
                hide();                                                         // 隐藏对话框
                break;                                                          // 结束分支
        }
    };

    /**
     * 设置对话框数据并显示
     * @param showType          来源类型（FROM_CHANNEL / FROM_MATHREF），决定弹出位置
     * @param chIndex           通道索引
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(int showType, int chIndex, DialogSelectColor.OnDismissListener onDismissListener) {
        this.chIndex = chIndex;                                                 // 保存通道索引
        this.onDismissListener = onDismissListener;                             // 保存回调监听器
        setViewPosition(showType);                                              // 根据来源类型设置弹出位置
        show();                                                                 // 显示对话框
    }

    /**
     * 根据来源类型设置对话框与遮罩的弹出位置
     * @param showType 来源类型（FROM_CHANNEL / FROM_MATHREF）
     */
    public void setViewPosition(int showType) {
        int contentX, contentY, maskWidth, maskHeight, maskX, maskY;            // 内容位置与遮罩位置/尺寸
        switch (showType) {                                                     // 根据来源类型设置坐标
            case FROM_MATHREF://在Math/Ref菜单中显示
                contentX = 1167;                                                // 内容区域X坐标
                contentY = 280;                                                 // 内容区域Y坐标
                maskWidth = 819;//RightLayoutRef宽                               // 遮罩宽度
                maskHeight = 907;//RightLayoutRef高                              // 遮罩高度
                maskX = 980;//RightLayoutRef layoutX                             // 遮罩X坐标
                maskY = 210;//RightLayoutRef layoutY                             // 遮罩Y坐标
                break;                                                          // 结束MathRef分支
            default://在模拟通道菜单中显示
                contentX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);; // 内容区域X坐标
                contentY = (int) context.getResources().getDimension(R.dimen.rightDialogY);        // 内容区域Y坐标
                maskWidth = (int) context.getResources().getDimension(R.dimen.rightSlipChannelWidth); // 遮罩宽度
                maskHeight = (int) context.getResources().getDimension(R.dimen.rightDialogHeight);   // 遮罩高度
                maskX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);     // 遮罩X坐标
                maskY = (int) context.getResources().getDimension(R.dimen.rightDialogY);            // 遮罩Y坐标
                break;                                                          // 结束默认分支
        }

        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) rlContentView.getLayoutParams(); // 获取内容区域布局参数
        layoutParams.x = contentX;                                              // 设置X坐标
        layoutParams.y = contentY;                                              // 设置Y坐标
        rlContentView.setLayoutParams(layoutParams);                            // 应用布局参数

        AbsoluteLayout.LayoutParams layoutParams1 = (AbsoluteLayout.LayoutParams) maskView.getLayoutParams(); // 获取遮罩布局参数
        layoutParams1.width = maskWidth;                                        // 设置遮罩宽度
        layoutParams1.height = maskHeight;                                      // 设置遮罩高度
        layoutParams1.x = maskX;                                                // 设置遮罩X坐标
        layoutParams1.y = maskY;                                                // 设置遮罩Y坐标
        maskView.setLayoutParams(layoutParams1);                                // 应用布局参数
    }


    /**
     * 显示对话框
     * <p>设置可见性并通过 RxBus 发送对话框打开事件</p>
     */
    private void show() {
        setVisibility(VISIBLE);                                                 // 设置对话框可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_SELECT_COLOR); // 发送对话框打开事件
//        Tools.PrintControlsLocation(TAG, rootViewGroup);
    }

    /**
     * 隐藏对话框
     * <p>设置不可见并通过 RxBus 发送对话框关闭事件</p>
     */
    public void hide() {
        setVisibility(GONE);                                                    // 设置对话框不可见
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_SELECT_COLOR); // 发送对话框关闭事件
    }

    /**
     * 颜色项点击监听器
     * <p>点击颜色后，将颜色值映射到通道颜色、回调结果、更新全局颜色设置、隐藏对话框</p>
     */
    private SelectColorAdapter.OnItemClickListener itemClickListener = new SelectColorAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, String colorStr) {
            colorStr = SvgNodeInfo.getGenColor(chIndex,colorStr);               // 根据通道索引生成颜色值
            onDismissListener.onDismiss(chIndex, colorStr);                     // 回调选中的通道索引与颜色值
            handleSelectColor(chIndex, colorStr);                               // 处理颜色变更（更新全局设置）
            hide();                                                             // 隐藏对话框
        }
    };

    //颜色值改变的起始

    /**
     * 处理颜色选择后的全局更新
     * <p>更新 SvgNodeInfo 中的通道颜色、缓存颜色值、通过 RxBus 通知颜色变更</p>
     * @param chIndex  通道索引
     * @param colorStr 新颜色值
     */
    private void handleSelectColor(int chIndex, String colorStr) {
        Logger.i(TAG, "channel= " + chIndex + " ,selectColor= " + colorStr);    // 记录日志
        SvgNodeInfo.setChannelColor(chIndex, colorStr);//改变颜色值              // 更新全局通道颜色
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorStr); // 缓存颜色值
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorStr);//发消息通知颜色值改变了 // 发送颜色变更事件
    }

}
