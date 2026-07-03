package com.micsig.tbook.tbookscope.main.maincenter.serialsword;  // 定义串口文本提示Fragment类的包路径

import android.os.Bundle;  // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater;  // 导入布局填充器类，用于创建视图
import android.view.View;  // 导入视图基类
import android.view.ViewGroup;  // 导入视图组类，Fragment容器
import android.widget.Button;  // 导入按钮控件，用于调试功能
import android.widget.TextView;  // 导入文本视图控件，用于显示提示信息

import androidx.annotation.Nullable;  // 导入Nullable注解，标记可空参数
import androidx.fragment.app.Fragment;  // 导入Fragment基类

import com.micsig.tbook.tbookscope.R;  // 导入资源类，访问布局和控件ID
import com.micsig.tbook.tbookscope.tools.PlaySound;  // 导入声音播放工具类
import com.micsig.tbook.tbookscope.util.App;  // 导入应用工具类，用于判断调试模式
import com.micsig.tbook.ui.util.ScreenUtil;  // 导入屏幕工具类，用于获取视图位置

/**
 * ***********************************************************************************
 * * 串口文本提示信息显示Fragment类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - 串口总线文本显示模块的提示信息页面
 * *
 * * 【核心职责】
 * *   1. 显示通道未配置或配置不一致时的提示文本
 * *   2. 提供调试模式下的清空按钮和位置测试功能
 * *   3. 支持动态更新提示文本内容
 * *   4. 作为默认显示页面，引导用户配置串口通道
 * *
 * * 【架构设计】
 * *   继承Fragment作为简单文本显示页面：
 * *   - 使用TextView显示提示信息
 * *   - 使用Button提供调试功能（仅在调试模式显示）
 * *   - 通过setTip方法动态更新提示文本
 * *   - 作为Detail容器的默认Fragment显示
 * *
 * * 【数据流向】
 * *   输入：
 * *     → Detail容器调用setTip设置提示文本
 * *     → 用户点击调试按钮触发位置测试
 * *   输出：
 * *     → TextView显示提示文本
 * *     → ScreenUtil获取视图位置信息
 * *
 * * 【依赖关系】
 * *   上层依赖：MainLayoutCenterSerialsWordDetail父容器Fragment
 * *   下层依赖：PlaySound、ScreenUtil、App等工具类
 * *   平级依赖：无
 * *
 * * 【使用场景】
 * *   当串口通道未开启或协议类型未配置时显示默认提示页面
 * *   在S12组合通道配置不一致时显示配置不一致提示
 * *   在调试模式下提供额外的调试功能按钮
 * ***********************************************************************************
 */
public class MainLayoutCenterSerialsWordTip extends Fragment {
    private TextView tvTip;  // 提示文本显示控件，显示配置提示信息
    private Button btnClear;  // 清空/调试按钮，仅在调试模式显示

    private int type = ISerialsWord.TYPE_S1;  // 通道类型，默认为S1通道

    /**
     * 设置通道类型
     * @param type 通道类型常量（TYPE_S1/TYPE_S2/TYPE_S3/TYPE_S4/TYPE_S12）
     */
    public void setChType(int type) {
        this.type = type;  // 保存通道类型到成员变量
    }

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器视图组
     * @param savedInstanceState 保存的状态Bundle
     * @return 填充后的视图对象
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_tip, container, false);  // 填充提示布局文件
    }

    /**
     * 视图创建完成回调，初始化控件和调试按钮
     * @param view 创建的视图对象
     * @param savedInstanceState 保存的状态Bundle
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tvTip = (TextView) view.findViewById(R.id.tip);  // 获取提示文本控件
        btnClear = (Button) view.findViewById(R.id.clear);  // 获取清空按钮控件
        btnClear.setOnClickListener(onClickListener);  // 为清空按钮设置点击监听器
        btnClear.setVisibility(App.IsDebug() ? View.VISIBLE : View.GONE);  // 仅在调试模式显示清空按钮
    }

    /**
     * 设置提示文本内容（字符串参数）
     * @param tip 提示文本字符串
     */
    public void setTip(String tip) {
        tvTip.setText(tip);  // 设置提示文本内容
    }

    /**
     * 设置提示文本内容（资源ID参数）
     * @param resId 提示文本资源ID
     */
    public void setTip(int resId) {
        tvTip.setText(resId);  // 设置提示文本内容，使用资源ID
    }

    /**
     * 视图点击监听器，处理调试按钮点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        /**
         * 处理点击事件
         * @param v 被点击的视图
         */
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();  // 播放按钮点击音效
            ScreenUtil.getViewLocation(v);  // 获取视图位置信息，用于调试
        }
    };
}
