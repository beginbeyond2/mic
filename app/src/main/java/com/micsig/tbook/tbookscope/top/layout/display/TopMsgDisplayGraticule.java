package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入带选中状态的整型数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 方格图(Graticule)显示详情数据模型               ║
 * ║  核心职责: 封装Display菜单下"方格图"子页面的可配置项数据                      ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayGraticule → TopMsgDisplayGraticule → ...      ║
 * ║  依赖关系: TopBeanChannel、RxIntWithSelect                                ║
 * ║  使用场景: 用户在Display-方格图页面修改显示模式或网格亮度时携带数据             ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayGraticule implements IDisplayDetail { // 方格图显示详情数据模型，实现IDisplayDetail接口

    private TopBeanChannel displayMode; // 方格图显示模式（全网格/十字点/十字线/仅边框）
    private RxIntWithSelect intensity; // 网格亮度值

    /**
     * 获取方格图显示模式
     * @return 显示模式通道数据Bean
     */
    public TopBeanChannel getDisplayMode() { // 获取显示模式
        return displayMode; // 返回显示模式数据
    }

    /**
     * 设置方格图显示模式，非首次设置时自动取消其他项选中并标记当前项
     * @param displayMode 显示模式通道数据Bean
     */
    public void setDisplayMode(TopBeanChannel displayMode) { // 设置显示模式
        if (this.displayMode == null) { // 首次设置
            this.displayMode = displayMode; // 直接赋值
        } else { // 非首次设置（切换模式时）
            this.displayMode = displayMode; // 更新模式引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.displayMode.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取网格亮度值
     * @return 带选中状态的整型数据Bean
     */
    public RxIntWithSelect getIntensity() { // 获取网格亮度
        return intensity; // 返回亮度数据
    }

    /**
     * 设置网格亮度值，非首次设置时自动取消其他项选中并标记当前项
     * @param intensity 网格亮度整数值
     */
    public void setIntensity(int intensity) { // 设置网格亮度
        if (this.intensity == null) { // 首次设置
            this.intensity = new RxIntWithSelect(intensity); // 创建带选中状态的整型对象
        } else { // 非首次设置
            this.intensity.setValue(intensity); // 更新亮度值
            setAllUnSelect(); // 取消所有项的选中状态
            this.intensity.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 取消所有可配置项的选中状态，确保同一时刻只有一个项被标记为选中
     */
    private void setAllUnSelect() { // 取消所有项选中状态
        displayMode.setRxMsgSelect(false); // 取消显示模式选中
        intensity.setRxMsgSelect(false); // 取消网格亮度选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含显示模式和亮度的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayGraticule{" + // 类名前缀
                "displayMode=" + displayMode + // 显示模式信息
                ", intensity=" + intensity + // 亮度信息
                '}'; // 类名后缀
    }
}
