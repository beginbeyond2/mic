package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入带选中状态的整型数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 波形(Waveform)显示详情数据模型                  ║
 * ║  核心职责: 封装Display菜单下"波形"子页面的所有可配置项数据                     ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayWaveform → TopMsgDisplayWaveform → ...        ║
 * ║  依赖关系: TopBeanChannel、RxIntWithSelect                                ║
 * ║  使用场景: 用户在Display-波形页面修改绘制类型/背景色/亮度时携带数据             ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayWaveform implements IDisplayDetail { // 波形显示详情数据模型，实现IDisplayDetail接口

    private TopBeanChannel drawType; // 绘制类型（点模式/线模式等）
    private TopBeanChannel background; // 背景颜色（黑色/白色）
    private RxIntWithSelect brightness; // 波形亮度值

    /**
     * 获取背景颜色选择
     * @return 背景颜色通道数据Bean
     */
    public TopBeanChannel getBackground(){return background;} // 获取背景颜色选择

    /**
     * 设置背景颜色选择，非首次设置时自动取消其他项选中并标记当前项
     * @param background 背景颜色通道数据Bean
     */
    public void setBackground(TopBeanChannel background){ // 设置背景颜色选择
        if (this.background == null) { // 首次设置
            this.background = background; // 直接赋值
        } else { // 非首次设置（切换背景色时）
            this.background = background; // 更新背景色引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.background.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取绘制类型选择
     * @return 绘制类型通道数据Bean
     */
    public TopBeanChannel getDrawType() { // 获取绘制类型
        return drawType; // 返回绘制类型数据
    }

    /**
     * 设置绘制类型选择，非首次设置时自动取消其他项选中并标记当前项
     * @param drawType 绘制类型通道数据Bean
     */
    public void setDrawType(TopBeanChannel drawType) { // 设置绘制类型
        if (this.drawType == null) { // 首次设置
            this.drawType = drawType; // 直接赋值
        } else { // 非首次设置（切换绘制类型时）
            this.drawType = drawType; // 更新绘制类型引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.drawType.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取波形亮度值
     * @return 带选中状态的整型数据Bean
     */
    public RxIntWithSelect getBrightness() { // 获取波形亮度
        return brightness; // 返回亮度数据
    }

    /**
     * 设置波形亮度值，非首次设置时自动取消其他项选中并标记当前项
     * @param brightness 波形亮度整数值
     */
    public void setBrightness(int brightness) { // 设置波形亮度
        if (this.brightness == null) { // 首次设置
            this.brightness = new RxIntWithSelect(brightness); // 创建带选中状态的整型对象
        } else { // 非首次设置
            this.brightness.setValue(brightness); // 更新亮度值
            setAllUnSelect(); // 取消所有项的选中状态
            this.brightness.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 取消所有可配置项的选中状态，确保同一时刻只有一个项被标记为选中
     */
    private void setAllUnSelect() { // 取消所有项选中状态
        drawType.setRxMsgSelect(false); // 取消绘制类型选中
        brightness.setRxMsgSelect(false); // 取消波形亮度选中
        background.setRxMsgSelect(false); // 取消背景颜色选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含绘制类型、背景色和亮度的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayWaveform{" + // 类名前缀
                "drawType=" + drawType + // 绘制类型信息
                ", background=" + background + // 背景颜色信息
                ", brightness=" + brightness + // 亮度信息
                '}'; // 类名后缀
    }
}
