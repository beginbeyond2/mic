package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.scope.measure.MeasureStaticsBean;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @auother Liwb
 * @description:
 * @data:2023-10-27 11:00
 */

/*
 * +===========================================================================+
 * |                       DialogMeasureStaticsItem                            |
 * |                         测量统计项单个条目组件                              |
 * +===========================================================================+
 * | 模块定位: 测量统计对话框中的单个测量项条目                                   |
 * | 核心职责: 显示单个测量项的统计数据（当前值、平均值、最大值、最小值等）         |
 * | 架构设计: 继承ConstraintLayout，作为DialogMeasureStatics的子项              |
 * | 数据流向: MeasureStaticsBean -> DialogMeasureStaticsItem -> TextView       |
 * | 依赖关系: Context, MeasureManage, MeasureStaticsBean, TChan                |
 * | 使用场景: 被DialogMeasureStatics动态创建和更新，显示各通道测量参数           |
 * +===========================================================================+
 */
public class DialogMeasureStaticsItem extends ConstraintLayout {
    private Context context;  // 应用上下文
    private ViewGroup rootView;  // 根视图容器

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public DialogMeasureStaticsItem(@NonNull Context context) {
        this(context,null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public DialogMeasureStaticsItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图组件
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogMeasureStaticsItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context=context;  // 保存上下文引用
        initView();  // 初始化视图
    }





    private TextView row1,row2,row3,row4,row5,row6,title;  // 各行文本视图和标题
    private List<Boolean> listParam=new ArrayList<>();  // 参数显示状态列表
    private List<TextView> listView=new ArrayList<>();  // 文本视图列表

    /**
     * 点击事件回调接口
     * 当条目被点击时触发
     */
    public Consumer<View> OnTxtClickEvent;  // 点击事件回调

    /**
     * 初始化视图组件
     * 加载布局并绑定各控件
     */
    private void initView() {
        rootView= (ViewGroup) View.inflate(context, R.layout.dialog_measure_statics_item,this);  // 加载布局文件
        row1=rootView.findViewById(R.id.txt_row1);  // 获取第1行文本
        row2=rootView.findViewById(R.id.txt_row2);  // 获取第2行文本
        row3=rootView.findViewById(R.id.txt_row3);  // 获取第3行文本
        row4=rootView.findViewById(R.id.txt_row4);  // 获取第4行文本
        row5=rootView.findViewById(R.id.txt_row5);  // 获取第5行文本
        row6=rootView.findViewById(R.id.txt_row6);  // 获取第6行文本
        title=rootView.findViewById(R.id.txt_title);  // 获取标题文本

        // 设置点击监听器（Android N及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  // 检查Android版本
            this.setOnClickListener((v)->{  // 设置点击监听
                if (OnTxtClickEvent!=null) OnTxtClickEvent.accept(DialogMeasureStaticsItem.this);  // 触发回调事件
            });
        }
        // 将文本视图添加到列表
        listView.add(row1);  // 添加第1行
        listView.add(row2);  // 添加第2行
        listView.add(row3);  // 添加第3行
        listView.add(row4);  // 添加第4行
        listView.add(row5);  // 添加第5行
        listView.add(row6);  // 添加第6行
    }

    /**
     * 清空所有文本内容
     * 重置各行为空字符串
     */
    public void clearTxt(){
        row1.setText("");  // 清空第1行
        row2.setText("");  // 清空第2行
        row3.setText("");  // 清空第3行
        row4.setText("");  // 清空第4行
        row5.setText("");  // 清空第5行
        row6.setText("");  // 清空第6行
        title.setText("");  // 清空标题
        title.setVisibility(GONE);  // 隐藏标题
    }

    /**
     * 更新参数可见性
     * 根据缓存配置决定显示哪些统计参数
     */
    public void UpdateParamVisible(){
        listParam.clear();  // 清空参数列表
        boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);  // 获取当前值显示配置
        boolean mean=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN);  // 获取平均值显示配置
        boolean max=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX);  // 获取最大值显示配置
        boolean min=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN);  // 获取最小值显示配置
        boolean delta=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA);  // 获取差值显示配置
        boolean count=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT);  // 获取计数显示配置
        listParam.add(all);  // 添加当前值配置
        listParam.add(mean);  // 添加平均值配置
        listParam.add(max);  // 添加最大值配置
        listParam.add(min);  // 添加最小值配置
        listParam.add(delta);  // 添加差值配置
        listParam.add(count);  // 添加计数配置
//        Log.d("Tag.Debug", String.format("UpdateParamVisible: %s", Arrays.toString(listParam.toArray())));
        // 根据配置设置各行可见性
        for(int i=0;i<listParam.size();i++){  // 遍历参数列表
            listView.get(i).setVisibility(listParam.get(i)?VISIBLE:GONE);  // 设置可见性
        }
    }

    /**
     * 设置标题文本
     * 显示各统计参数的标题
     */
    public void setTitle(){
        row1.setText(R.string.dialog_measure_static_curr_value);  // 设置当前值标题
        row2.setText(R.string.dialog_measure_static_avg_value);  // 设置平均值标题
        row3.setText(R.string.dialog_measure_static_max_value);  // 设置最大值标题
        row4.setText(R.string.dialog_measure_static_min_value);  // 设置最小值标题
        row5.setText(R.string.dialog_measure_static_mean_var_value );  // 设置差值标题
        row6.setText(R.string.dialog_measure_static_count_value);  // 设置计数标题
    }

    /**
     * 隐藏标题
     */
    public void setTitleHide(){
        title.setVisibility(GONE);  // 隐藏标题视图
    }

    /**
     * 更新视图显示内容
     * 根据测量项数据刷新显示
     * @param item 测量项数据结构
     * @param isEnable 是否启用点击选中功能
     */
    public void UpdateView(MeasureManage.MeasureItemStruct item,boolean isEnable){
        int iWaveCh=item.getChannelId();  // 获取通道ID
        int measureId=item.getMeasureId();  // 获取测量ID
        int color= TChan.getChannelColor(context,iWaveCh);  // 获取通道颜色
        title.setTextColor(color);  // 设置标题颜色
        title.setText(MeasureManage.getEnclosedNumber(item.getNo() + 1) + item.getMeasureName());  // 设置标题文本（带编号）
        // 设置选中状态背景
        if (item.isSelected() && isEnable){  // 已选中且启用点击
            this.setBackgroundResource(R.drawable.measure_item_select);  // 设置选中背景
        }else {  // 未选中或禁用点击
            this.setBackground(null);  // 清除背景
        }
        title.setVisibility(VISIBLE);  // 显示标题
        //update param
        Measure measure=getHardwareMeasure(iWaveCh-1);  // 获取硬件测量对象
        MeasureStaticsBean bean= measure.getMeasureStatics(measureId+16);  // 获取统计数据



        // 更新各行数据
        if (bean!=null && bean.getNums()>0){  // 数据有效且有测量次数
            row1.setText(Tools.updateMeasureData(iWaveCh-1,measureId,(float)bean.getVal()));  // 设置当前值
            row2.setText(Tools.updateMeasureData(iWaveCh-1,measureId,(float)bean.getAverageVal()));  // 设置平均值
            row3.setText(Tools.updateMeasureData(iWaveCh-1,measureId,(float)bean.getMaxVal()));  // 设置最大值
            row4.setText(Tools.updateMeasureData(iWaveCh-1,measureId,(float)bean.getMinVal()));  // 设置最小值
            row5.setText(Tools.updateMeasureData(iWaveCh-1,measureId,(float)bean.getMqdVal()));  // 设置差值
            row6.setText(String.valueOf(bean.getNums()));  // 设置计数
        }else{  // 无有效数据
            row1.setText("__._");  // 显示占位符
            row2.setText("__._");  // 显示占位符
            row3.setText("__._");  // 显示占位符
            row4.setText("__._");  // 显示占位符
            row5.setText("__._");  // 显示占位符
            row6.setText("__._");  // 显示占位符
        }
    }


    /**
     * 获取硬件测量对象
     * @param chId 通道ID
     * @return Measure对象，找不到则返回null
     */
    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;  // 基础通道引用
        if (ChannelFactory.isDynamicCh(chId)) {  // 判断是否为动态通道
            baseChannel = ChannelFactory.getDynamicChannel(chId);  // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) {  // 判断是否为数学运算通道
            baseChannel = ChannelFactory.getMathChannel(chId);  // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) {  // 判断是否为参考通道
            baseChannel = ChannelFactory.getRefChannel(chId);  // 获取参考通道
        }
        if (baseChannel != null) {  // 通道有效
            return baseChannel.getMeasure();  // 返回测量对象
        }
        return null;  // 返回空
    }

}