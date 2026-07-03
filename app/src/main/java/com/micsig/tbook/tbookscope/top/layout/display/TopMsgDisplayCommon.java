package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中状态的布尔数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 常规(Common)显示详情数据模型                    ║
 * ║  核心职责: 封装Display菜单下"常规"子页面的所有可配置项数据                     ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayCommon → TopMsgDisplayCommon → TopMsgDisplay  ║
 * ║  依赖关系: TopBeanChannel、RxBooleanWithSelect                            ║
 * ║  使用场景: 用户在Display-常规页面修改水平参考/时基模式/滚动/CCT时携带数据       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayCommon implements IDisplayDetail { // 常规显示详情数据模型，实现IDisplayDetail接口

    private TopBeanChannel horRef; // 水平参考通道选择（如CH1/CH2等）
    private TopBeanChannel timeBase; // 时基模式选择（YT模式/XY模式）
    private RxBooleanWithSelect roll; // 滚动屏幕开关状态
    private RxBooleanWithSelect cct; // CCT（点击选择）开关状态

    private float alpha = 0.85f; // 波形透明度，默认0.85

    /**
     * 获取波形透明度
     * @return 透明度值（0.0~1.0）
     */
    public float getAlpha() { // 获取透明度
        return alpha; // 返回透明度值
    }

    /**
     * 设置波形透明度，内部将输入值映射到实际透明度范围
     * @param alpha 输入的透明度比例值（0.0~1.0）
     */
    public void setAlpha(float alpha) { // 设置透明度
        this.alpha = (1  -  alpha * 0.5f); // 映射透明度：输入0→输出1.0，输入1→输出0.5
    }

    /**
     * 获取水平参考通道选择
     * @return 水平参考通道数据Bean
     */
    public TopBeanChannel getHorRef() { // 获取水平参考通道
        return horRef; // 返回水平参考通道数据
    }

    /**
     * 设置水平参考通道选择，非首次设置时自动取消其他项选中并标记当前项
     * @param horRef 水平参考通道数据Bean
     */
    public void setHorRef(TopBeanChannel horRef) { // 设置水平参考通道
        if (this.horRef == null) { // 首次设置
            this.horRef = horRef; // 直接赋值
        } else { // 非首次设置（切换通道时）
            this.horRef = horRef; // 更新通道引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.horRef.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取时基模式选择
     * @return 时基模式通道数据Bean
     */
    public TopBeanChannel getTimeBase() { // 获取时基模式
        return timeBase; // 返回时基模式数据
    }

    /**
     * 设置时基模式选择，非首次设置时自动取消其他项选中并标记当前项
     * @param timeBase 时基模式通道数据Bean
     */
    public void setTimeBase(TopBeanChannel timeBase) { // 设置时基模式
        if (this.timeBase == null) { // 首次设置
            this.timeBase = timeBase; // 直接赋值
        } else { // 非首次设置（切换模式时）
            this.timeBase = timeBase; // 更新模式引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.timeBase.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取滚动屏幕开关状态
     * @return 带选中状态的布尔数据Bean
     */
    public RxBooleanWithSelect getRoll() { // 获取滚动开关状态
        return roll; // 返回滚动开关数据
    }

    /**
     * 设置滚动屏幕开关状态，非首次设置时自动取消其他项选中并标记当前项
     * @param roll 滚动开关布尔值
     */
    public void setRoll(boolean roll) { // 设置滚动开关状态
        if (this.roll == null) { // 首次设置
            this.roll = new RxBooleanWithSelect(roll); // 创建带选中状态的布尔对象
        } else { // 非首次设置
            this.roll.setValue(roll); // 更新布尔值
            setAllUnSelect(); // 取消所有项的选中状态
            this.roll.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取CCT（点击选择）开关状态
     * @return 带选中状态的布尔数据Bean
     */
    public RxBooleanWithSelect getCct() { // 获取CCT开关状态
        return cct; // 返回CCT开关数据
    }

    /**
     * 设置CCT（点击选择）开关状态，非首次设置时自动取消其他项选中并标记当前项
     * @param cct CCT开关布尔值
     */
    public void setCct(boolean cct) { // 设置CCT开关状态
        if (this.cct == null) { // 首次设置
            this.cct = new RxBooleanWithSelect(cct); // 创建带选中状态的布尔对象
        } else { // 非首次设置
            this.cct.setValue(cct); // 更新布尔值
            setAllUnSelect(); // 取消所有项的选中状态
            this.cct.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 取消所有可配置项的选中状态，确保同一时刻只有一个项被标记为选中
     */
    private void setAllUnSelect() { // 取消所有项选中状态
        horRef.setRxMsgSelect(false); // 取消水平参考通道选中
        timeBase.setRxMsgSelect(false); // 取消时基模式选中
        roll.setRxMsgSelect(false); // 取消滚动开关选中
        cct.setRxMsgSelect(false); // 取消CCT开关选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含所有配置项的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayCommon{" + // 类名前缀
                "horRef=" + horRef + // 水平参考通道信息
                ", timeBase=" + timeBase + // 时基模式信息
                ", roll=" + roll + // 滚动开关信息
                ", cct=" + cct + // CCT开关信息
                '}'; // 类名后缀
    }
}
