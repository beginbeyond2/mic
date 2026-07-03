// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopLayoutMeasureCounter.java
//  核心职责：计数器测量子页面Fragment（已弃用，改用TopLayoutFrequencyMeter）
//  架构设计：Fragment子页面，包含计数器类型和信号源选择
//  数据流向：用户选择 → TopViewRadioGroup → 待实现硬件指令
//  依赖关系：依赖TopViewRadioGroup控件、OnDetailSendMsgListener接口
//  使用场景：测量功能中的计数器子页面（当前已被频率计替代）
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件

/**
 * 计数器测量Fragment - 计数器功能的子页面（已弃用，改用频率计）
 */
public class TopLayoutMeasureCounter extends Fragment { // 继承Fragment，计数器子页面
    private Context context; // 上下文对象

    private TopViewRadioGroup measureCounterType; // 计数器类型选择单选组
    private TopViewRadioGroup measureCounterSource; // 计数器信号源选择单选组
    private TextView reset; // 重置按钮文本视图

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_measure_counter, container, false); // 填充计数器布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        measureCounterType = (TopViewRadioGroup) view.findViewById(R.id.measureCounterType); // 获取计数器类型单选组
        measureCounterSource = (TopViewRadioGroup) view.findViewById(R.id.measureCounterSource); // 获取信号源单选组
        reset = (TextView) view.findViewById(R.id.reset); // 获取重置按钮
    }

    /**
     * 设置详情发送消息监听器（空实现）
     * @param onDetailSendMsgListener 监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息监听器（空实现）

    }

    /**
     * 获取测量详情接口（返回null）
     * @return null
     */
    public IMeasureDetail getMeasureDetail() { // 获取测量详情
        return null; // 返回null
    }
}
