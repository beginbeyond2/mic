package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.functions.Consumer;

/*
 * +----------------------------------------------------------------------+
 * |                    DialogLoadRefCsvWave                               |
 * |                   加载Ref CSV波形对话框                                |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供CSV波形文件加载时的通道-Ref映射设置界面，                     |
 * |          支持将CSV中的通道数据映射到指定的Ref通道（R1~R8）                  |
 * | 架构设计: 继承ConstraintLayout的自定义对话框视图，                        |
 * |          使用TopViewChannelMultipleChoice实现顶部Ref选择和               |
 * |          底部通道/Math/Ref的多选CheckBox组，                             |
 * |          顶部Ref勾选与底部通道勾选形成一一映射关系                         |
 * | 数据流向: 外部调用setData()传入CSV中包含的通道列表 ->                      |
 * |          用户勾选映射关系 -> 点击确定 ->                                  |
 * |          OnDismissListener回调返回channelToRef映射表                     |
 * | 依赖关系: RxBus(对话框开关/颜色选择事件), CacheUtil(Ref状态缓存),         |
 * |          TopViewChannelMultipleChoice(多选控件), TChan(通道工具),        |
 * |          ChannelFactory(通道常量), DToast(提示)                        |
 * | 使用场景: 从CSV文件加载参考波形时，选择哪些通道数据映射到哪些Ref通道        |
 * +----------------------------------------------------------------------+
 */
public class DialogLoadRefCsvWave extends ConstraintLayout {

    private static final String TAG = "DialogLoadRefCsvWave";  // 日志标签
    private final Context context;  // 上下文引用
    private ConstraintLayout rootViewGroup;  // 根视图组
    private OnDismissListener dismissListener;  // 对话框关闭回调监听器
    private LinearLayout linearLayout;  // 内容区域线性布局
    private TopViewChannelMultipleChoice finalRefChoice, channelChoice, mathChoice, refChoice;  // 顶部Ref选择/通道选择/Math选择/Ref选择控件
    private final HashMap<Integer, Boolean> refOpenMap = new HashMap<>();//当前Ref通道的打开状态  // Ref通道启用状态映射
    private final List<CheckBox> allContentCheckBox = new ArrayList<>();  // 所有底部CheckBox（通道+Math+Ref）集合
    private final ConcurrentHashMap<Integer, Integer> channelToRef = new ConcurrentHashMap<>();// ref channel  // 通道到Ref的映射表（key=Ref通道号, value=源通道号）
    private View finalRefChoiceView, channelChoiceView, mathChoiceView, refChoiceView;  // 各选择控件的视图

    /**
     * 对话框关闭监听接口。
     * <p>
     * 当用户确认映射关系后回调，返回通道到Ref的映射表。
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回通道到Ref的映射表 */
        void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef);
    }

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogLoadRefCsvWave(Context context) {
        this(context, null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogLoadRefCsvWave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并加载布局和事件订阅。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogLoadRefCsvWave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;               // 保存上下文引用
        initView();                           // 初始化视图
        initControl();                        // 初始化事件订阅
    }

    /**
     * 初始化事件订阅。
     * <p>
     * 订阅RxBus的通道颜色选择事件，用于实时更新对话框中的通道颜色。
     */
    private void initControl() {
        //EventFactory.addEventObserver(EventFactory.EVENT_LOADCSV_RUN, eventLoadCsvObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);  // 订阅通道颜色选择事件
    }

    /**
     * 设置对话框数据并显示。
     * <p>
     * 清空之前的映射关系，更新Ref通道状态和CSV通道可用状态。
     *
     * @param channelInCsv    CSV文件中包含的通道索引列表
     * @param dismissListener 关闭回调监听器
     */
    public void setData(List<Integer> channelInCsv, OnDismissListener dismissListener) {
        channelToRef.clear();  // 清空映射表
        this.dismissListener = dismissListener;  // 保存关闭监听器
        updateFinalRefChoiceView();  // 更新顶部Ref选择视图
        updateCsvChoiceState(channelInCsv);  // 更新CSV通道可用状态
        show();  // 显示对话框
    }

    /**
     * 初始化视图布局。
     * <p>
     * 加载dialog_load_ref_csv布局，初始化通道名称映射，
     * 设置确定按钮和外部区域的点击监听。
     */
    public void initView() {
        initNameToChan();  // 初始化通道名称到通道号的映射
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_load_ref_csv, this);  // 填充布局
        linearLayout = rootViewGroup.findViewById(R.id.ll_content);  // 内容区域布局
        Button btnOk = findViewById(R.id.btnOK);  // 确定按钮
        btnOk.setOnClickListener(onClickListener);  // 设置确定按钮点击监听
        View outView = rootViewGroup.findViewById(R.id.outView);  // 外部区域
        setContentView();  // 初始化多选控件内容
        outView.setOnClickListener(onClickListener);  // 设置外部区域点击监听
    }

    /**
     * 初始化多选控件内容。
     * <p>
     * 创建顶部Ref选择、底部通道/Math/Ref选择4个多选控件，
     * 配置数据源和布局参数，添加到内容布局中，
     * 收集所有底部CheckBox到allContentCheckBox列表。
     */
    private void setContentView() {
        finalRefChoice = new TopViewChannelMultipleChoice(context, false, onTopItemOnlyClickListener);  // 顶部Ref选择（仅选中模式）
        channelChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);  // 底部通道选择
        mathChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);  // 底部Math选择
        refChoice = new TopViewChannelMultipleChoice(context, false, onBottomItemOnlyClickListener);  // 底部Ref选择

        finalRefChoice.setData(R.array.popArrayRef, R.array.popArrayRefColor, 110, 90, 2);  // 设置Ref数据（2列跨距）
        channelChoice.setData(R.array.channelsNameEight, R.array.popArrayChannelEightColor, 110, 70, 0);  // 设置通道数据
        mathChoice.setData(R.array.popArrayMath, R.array.popArrayMathColor, 110, 70, 1);  // 设置Math数据
        refChoice.setData(R.array.popArrayRef, R.array.popArrayRefColor, 110, 70, 2);  // 设置Ref数据

        finalRefChoiceView = finalRefChoice.getInflate();  // 获取顶部Ref选择视图
        channelChoiceView = channelChoice.getInflate();  // 获取通道选择视图
        mathChoiceView = mathChoice.getInflate();  // 获取Math选择视图
        refChoiceView = refChoice.getInflate();  // 获取Ref选择视图

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  // 创建布局参数
                LinearLayout.LayoutParams.WRAP_CONTENT,  // 宽度自适应
                LinearLayout.LayoutParams.WRAP_CONTENT  // 高度自适应
        );
        layoutParams.gravity = Gravity.START;  // 左对齐
        layoutParams.setMarginStart(10);  // 左边距10dp

        finalRefChoiceView.setLayoutParams(layoutParams);  // 应用布局参数
        channelChoiceView.setLayoutParams(layoutParams);  // 应用布局参数
        mathChoiceView.setLayoutParams(layoutParams);  // 应用布局参数
        refChoiceView.setLayoutParams(layoutParams);  // 应用布局参数

        linearLayout.addView(finalRefChoiceView, 0);  // 添加顶部Ref选择到布局（位置0）
        linearLayout.addView(channelChoiceView, 2);  // 添加通道选择到布局（位置2）
        linearLayout.addView(mathChoiceView, 3);  // 添加Math选择到布局（位置3）
        linearLayout.addView(refChoiceView, 4);  // 添加Ref选择到布局（位置4）

        allContentCheckBox.clear();  // 清空底部CheckBox集合
        for (int i = 0; i < channelChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历通道CheckBox
            allContentCheckBox.add((CheckBox) channelChoice.getCheckBoxs().getChildAt(i));  // 添加到集合
        }

        for (int i = 0; i < mathChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历Math CheckBox
            allContentCheckBox.add((CheckBox) mathChoice.getCheckBoxs().getChildAt(i));  // 添加到集合
        }

        for (int i = 0; i < refChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历Ref CheckBox
            allContentCheckBox.add((CheckBox) refChoice.getCheckBoxs().getChildAt(i));  // 添加到集合
        }
    }

    /**
     * 全局点击事件监听器。
     * <p>
     * 处理外部区域点击（关闭对话框）和确定按钮点击（提交映射关系）。
     */
    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {  // 根据点击的视图ID分发
            case R.id.outView:  // 点击外部区域
                hide();  // 隐藏对话框
                break;
            case R.id.btnOK:  // 点击确定按钮
                sendChToRef();  // 提交映射关系
                hide();  // 隐藏对话框
                break;
        }
    };

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_LOAF_REF_CSV);  // 发送对话框打开事件
        Tools.PrintControlsLocation(TAG, rootViewGroup);  // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框，清理所有选中状态和缓存，并通过RxBus通知关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        //clear All  // 清理所有选中状态
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历顶部Ref CheckBox
            CheckBox refCheckBox = (CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i);  // 获取Ref CheckBox
            refCheckBox.setChecked(false);  // 取消选中
            refCheckBox.setText(refCheckBox.getText().toString().split("\\(")[0]);  // 恢复文本（去除括号内的映射通道名）
        }
        for (CheckBox checkBox : allContentCheckBox) {  // 遍历所有底部CheckBox
            checkBox.setChecked(false);  // 取消选中
        }
        TChan.foreachRef(refChan -> {  // 遍历所有Ref通道
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + refChan, String.valueOf(0));  // 重置CSV索引缓存
        });
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_LOAF_REF_CSV);  // 发送对话框关闭事件
    }

    /**
     * 提交通道到Ref的映射关系。
     * <p>
     * 如果映射表和监听器均有效，则回调onDismiss传递映射表。
     */
    private void sendChToRef() {
        if (channelToRef == null || dismissListener == null) return;  // 映射表或监听器为空则返回
        dismissListener.onDismiss(channelToRef);  // 回调映射表
    }

    /**
     * 更新当前Ref通道的启用状态。
     * <p>
     * 从缓存读取每个Ref通道是否由用户添加启用，更新refOpenMap。
     *
     * @return 更新后的Ref通道启用状态映射
     */
    private HashMap<Integer, Boolean> updateRefChannelState() {//更新当前Ref的显示状态
        refOpenMap.clear();  // 清空状态映射
        TChan.foreachRef(refChan -> {  // 遍历所有Ref通道
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan);  // 读取Ref勾选状态
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan);  // 读取用户是否添加了该Ref
            refOpenMap.put(refChan, refAddByUser);  // 记录Ref启用状态
        });
        return refOpenMap;  // 返回状态映射
    }

    /**
     * 更新顶部Ref选择视图的显示状态。
     * <p>
     * 根据Ref通道启用状态设置CheckBox的选中状态和文字颜色。
     */
    private void updateFinalRefChoiceView() {
        updateRefChannelState();  // 更新Ref通道状态
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历顶部Ref CheckBox
            ((CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i)).setChecked(refOpenMap.get(i + TChan.R1));  // 设置选中状态
            ((CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i)).setTextColor(TChan.getChannelColor(context, TChan.toRefTChan(TChan.toUiChNo(i))));  // 设置通道颜色
        }
    }

    /**
     * 根据CSV中包含的通道列表更新底部CheckBox的可用状态。
     * <p>
     * CSV中不包含的通道禁用（灰色），包含的通道启用并着色。
     *
     * @param channelInCsv CSV中包含的通道索引列表
     */
    private void updateCsvChoiceState(List<Integer> channelInCsv) {
        for (CheckBox checkBox : allContentCheckBox) {  // 先禁用所有底部CheckBox
            checkBox.setEnabled(false);  // 禁用
            checkBox.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewRightViewDisable));  // 灰色文字
        }
        for (Integer index : channelInCsv) {  // 启用CSV中包含的通道
            allContentCheckBox.get(index).setEnabled(true);  // 启用
            allContentCheckBox.get(index).setTextColor(TChan.getChannelColor(context, TChan.toUiChNo(index)));  // 通道颜色
        }
    }

    /**
     * 底部CheckBox勾选变化时，更新顶部Ref选择视图的对应状态。
     * <p>
     * 当底部通道被勾选时，自动在顶部找到一个空闲的Ref并建立映射；
     * 当底部通道取消勾选时，解除映射并恢复Ref文本。
     * 如果没有空闲的Ref，提示用户。
     *
     * @param bottomCheckBox 被操作的底部CheckBox
     */
    private void updateTopCheckState(CheckBox bottomCheckBox) {
        String bottomText = bottomCheckBox.getText().toString();  // 底部CheckBox文本（通道名）
        int chan = getChannelByName(bottomText);  // 获取通道号
        boolean canAdd = false;  // 标记是否成功添加/移除映射
        for (int i = 0; i < finalRefChoice.getCheckBoxs().getChildCount(); i++) {  // 遍历顶部Ref CheckBox
            CheckBox refCheckBox = (CheckBox) finalRefChoice.getCheckBoxs().getChildAt(i);  // 获取Ref CheckBox
            String refText = refCheckBox.getText().toString();  // Ref文本
            int refChan = getChannelByName(refText.split("\\(")[0]);  // 从Ref文本提取通道号
            if (bottomCheckBox.isChecked()) {  // 底部勾选 -> 建立映射
                if (refCheckBox.isChecked()) continue;  // 该Ref已被占用，跳过
                canAdd = true;  // 标记成功
                refCheckBox.setChecked(true);  // 选中Ref
                refCheckBox.setTag(chan);  // 记录映射的通道号
                refCheckBox.setText(String.format("%s(%s)", refText, bottomText));  // 更新Ref文本（显示映射关系）
            } else {  // 底部取消勾选 -> 解除映射
                if (!(refText.contains("(")  // Ref文本不包含括号（无映射）
                        && refText.split("\\(")[1].replace(")", "")  // 提取括号内的通道名
                        .equals(bottomText))) continue;  // 映射的不是当前通道，跳过
                refCheckBox.setChecked(false);  // 取消选中Ref
                refCheckBox.setTag(null);  // 清除映射标记
                canAdd = true;  // 标记成功
                channelToRef.remove(refChan);  // 从映射表移除
                refCheckBox.setText(refText.split("\\(")[0]);  // 恢复Ref文本
            }
            Object tag = refCheckBox.getTag();  // 获取映射标记
            if (tag == null) continue;  // 无映射则跳过
            if (tag instanceof Integer) {  // 标记为通道号
                int chanTag = (Integer) tag;  // 获取通道号
                channelToRef.put(refChan, chanTag);  // 更新映射表
            }
            break;  // 处理完一个Ref即跳出
        }
        if (!canAdd) {  // 没有可用的Ref
            bottomCheckBox.setChecked(false);  // 恢复底部取消勾选
            DToast.get().show(context.getResources().getString(R.string.all_ref_checked));  // 提示所有Ref已被占用
        }
        finalRefChoice.updateChild();  // 刷新顶部Ref视图
    }


    /**
     * 顶部Ref CheckBox勾选变化时，更新底部通道选择视图的对应状态。
     * <p>
     * 当顶部Ref取消勾选时，解除映射并取消对应底部通道的勾选；
     * 当顶部Ref勾选时，自动在底部找到一个可用的通道建立映射。
     *
     * @param topCheckBox 被操作的顶部Ref CheckBox
     */
    private void updateBottomCheckState(CheckBox topCheckBox) {
        int refChan = getChannelByName(topCheckBox.getText().toString().split("\\(")[0]);  // 从Ref文本提取通道号
        if (!topCheckBox.isChecked()) {  // 顶部Ref取消勾选
            topCheckBox.setText(topCheckBox.getText().toString().split("\\(")[0]);  // 恢复Ref文本
            if (!channelToRef.containsKey(refChan)) return;  // 映射表中无此Ref则返回
            int channel = channelToRef.get(refChan);//map里ref对应的channel  // 获取映射的源通道号
            channelToRef.remove(refChan);  // 从映射表移除
            String chName = getNameById(channel);  // 获取源通道名称
            for (CheckBox checkBox1 : allContentCheckBox) {  // 遍历底部CheckBox
                if (!checkBox1.getText().toString().equals(chName)) continue;  // 不匹配则跳过
                checkBox1.setChecked(false);  // 取消底部勾选
                break;  // 找到后跳出
            }
        } else {  // 顶部Ref勾选 -> 自动寻找底部通道
            boolean canAdd = false;  // 标记是否找到可用通道
            for (CheckBox checkBox1 : allContentCheckBox) {  // 遍历底部CheckBox
                if (checkBox1.isChecked() || !checkBox1.isEnabled()) continue;  // 已勾选或禁用则跳过
                canAdd = true;  // 标记找到可用通道
                checkBox1.setChecked(true);  // 勾选底部通道
                int channel = getChannelByName(checkBox1.getText().toString());  // 获取通道号
                topCheckBox.setTag(channel);  // 记录映射的通道号
                channelToRef.put(refChan, channel);  // 更新映射表
                topCheckBox.setText(String.format("%s(%s)", topCheckBox.getText(), checkBox1.getText()));  // 更新Ref文本（显示映射关系）
                break;  // 找到后跳出
            }
            if (!canAdd) {  // 没有可用的通道
                topCheckBox.setChecked(false);  // 恢复取消勾选
                DToast.get().show(context.getResources().getString(R.string.dialog_no_ref_data_checked));  // 提示没有可用的CSV通道数据
            }
        }
    }


    /**
     * 底部通道CheckBox的仅选中监听器。
     * <p>
     * 底部CheckBox点击时，更新顶部Ref选择的对应状态。
     */
    private TopViewChannelMultipleChoice.onItemOnlyClickListener onBottomItemOnlyClickListener = new TopViewChannelMultipleChoice.onItemOnlyClickListener() {

        @Override
        public void onlyClick(CheckBox checkBox) {
            //底部checkbox点击引起的变化
            updateTopCheckState(checkBox);  // 更新顶部Ref状态
        }
    };

    /**
     * 顶部Ref CheckBox的仅选中监听器。
     * <p>
     * 顶部Ref点击时，更新底部通道选择的对应状态。
     */
    private TopViewChannelMultipleChoice.onItemOnlyClickListener onTopItemOnlyClickListener = new TopViewChannelMultipleChoice.onItemOnlyClickListener() {

        @Override
        public void onlyClick(CheckBox checkBox) {
            //顶部checkbox点击引起的变化
            updateBottomCheckState(checkBox);  // 更新底部通道状态
        }
    };


    private HashMap<String, Integer> mapChanName;  // 通道名称到通道号的映射表

    /**
     * 初始化通道名称到通道号的映射表。
     * <p>
     * 包含CH1~CH8、M1~M8、R1~R8、S1~S4的映射。
     */
    private void initNameToChan() {
        if (mapChanName == null) {  // 映射表未初始化
            mapChanName = new HashMap<>();  // 创建映射表
            mapChanName.put("CH1", ChannelFactory.CH1);  // CH1映射
            mapChanName.put("CH2", ChannelFactory.CH2);  // CH2映射
            mapChanName.put("CH3", ChannelFactory.CH3);  // CH3映射
            mapChanName.put("CH4", ChannelFactory.CH4);  // CH4映射
            mapChanName.put("CH5", ChannelFactory.CH5);  // CH5映射
            mapChanName.put("CH6", ChannelFactory.CH6);  // CH6映射
            mapChanName.put("CH7", ChannelFactory.CH7);  // CH7映射
            mapChanName.put("CH8", ChannelFactory.CH8);  // CH8映射

            mapChanName.put("M1", ChannelFactory.MATH1);  // M1映射
            mapChanName.put("M2", ChannelFactory.MATH2);  // M2映射
            mapChanName.put("M3", ChannelFactory.MATH3);  // M3映射
            mapChanName.put("M4", ChannelFactory.MATH4);  // M4映射
            mapChanName.put("M5", ChannelFactory.MATH5);  // M5映射
            mapChanName.put("M6", ChannelFactory.MATH6);  // M6映射
            mapChanName.put("M7", ChannelFactory.MATH7);  // M7映射
            mapChanName.put("M8", ChannelFactory.MATH8);  // M8映射

            mapChanName.put("R1", ChannelFactory.REF1);  // R1映射
            mapChanName.put("R2", ChannelFactory.REF2);  // R2映射
            mapChanName.put("R3", ChannelFactory.REF3);  // R3映射
            mapChanName.put("R4", ChannelFactory.REF4);  // R4映射
            mapChanName.put("R5", ChannelFactory.REF5);  // R5映射
            mapChanName.put("R6", ChannelFactory.REF6);  // R6映射
            mapChanName.put("R7", ChannelFactory.REF7);  // R7映射
            mapChanName.put("R8", ChannelFactory.REF8);  // R8映射

            mapChanName.put("S1", ChannelFactory.S1);  // S1映射
            mapChanName.put("S2", ChannelFactory.S2);  // S2映射
            mapChanName.put("S3", ChannelFactory.S3);  // S3映射
            mapChanName.put("S4", ChannelFactory.S4);  // S4映射
        }
    }

    /**
     * 根据通道名称获取通道号。
     *
     * @param chName 通道名称（如"CH1"、"M3"、"R5"）
     * @return 通道号，未找到返回-1
     */
    public int getChannelByName(String chName) {
        if (mapChanName == null) initNameToChan();  // 确保映射表已初始化
        return mapChanName.getOrDefault(chName, -1);  // 查找映射，默认-1
    }

    /**
     * 根据通道号获取通道名称。
     *
     * @param chan 通道号
     * @return 通道名称，未找到返回"NULL"
     */
    public String getNameById(int chan) {
        if (mapChanName == null) initNameToChan();  // 确保映射表已初始化
        String chName = "NULL";  // 默认名称
        for (Map.Entry<String, Integer> entry : mapChanName.entrySet()) {  // 遍历映射表
            if (entry.getValue().equals(chan)) {  // 通道号匹配
                return entry.getKey();  // 返回通道名称
            }
        }
        return chName;  // 未找到返回"NULL"
    }

    /**
     * RxBus通道颜色选择事件消费者。
     * <p>
     * 当外部触发通道颜色变更时，实时更新对话框中各多选控件的通道颜色。
     * 事件数据格式："通道索引;颜色值"
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;  // 空数据则跳过
            Logger.i(TAG, "selectColorInfo= " + colorInfo);  // 打印调试日志
            String[] info = colorInfo.split(";");  // 分割通道索引和颜色值
            int chIndex = Integer.parseInt(info[0]);  // 解析通道索引
            String colorStr = info[1];  // 获取颜色值
            finalRefChoice.setChannelColorForDialogCSv(chIndex, colorStr);  // 更新顶部Ref颜色
//            channelChoice.setChannelColor(chIndex, colorStr);
            mathChoice.setChannelColorForDialogCSv(chIndex, colorStr);  // 更新Math颜色
            refChoice.setChannelColorForDialogCSv(chIndex, colorStr);  // 更新Ref颜色
        }
    };

}
