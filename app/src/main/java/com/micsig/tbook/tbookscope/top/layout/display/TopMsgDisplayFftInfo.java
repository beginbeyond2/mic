package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中状态的布尔数据Bean
import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入带选中状态的整型数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - FFT Info显示详情数据模型                        ║
 * ║  核心职责: 封装Display菜单下"FFT Info"子页面的可配置项数据                    ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayFftInfo → TopMsgDisplayFftInfo → TopMsgDisplay║
 * ║  依赖关系: RxIntWithSelect、RxBooleanWithSelect                          ║
 * ║  使用场景: 用户在Display-FFT Info页面修改FFT信息索引或显示开关时携带数据       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by limh on 2024/8/6.
 */

public class TopMsgDisplayFftInfo implements IDisplayDetail { // FFT Info显示详情数据模型，实现IDisplayDetail接口

    private RxIntWithSelect fftInfoIndex; // FFT信息显示索引（如频率计/峰值频率等选项）
    private RxBooleanWithSelect showFftInfo; // FFT信息显示开关（当前未启用）

    /**
     * 获取FFT信息显示索引
     * @return 带选中状态的整型数据Bean
     */
    public RxIntWithSelect getFftInfoIndex() { // 获取FFT信息索引
        return fftInfoIndex; // 返回FFT信息索引数据
    }

    /**
     * 设置FFT信息显示索引，非首次设置时自动取消其他项选中并标记当前项
     * @param fftInfoIndex FFT信息索引值
     */
    public void setFftInfoIndex(int fftInfoIndex) { // 设置FFT信息索引
        if (this.fftInfoIndex == null) { // 首次设置
            this.fftInfoIndex = new RxIntWithSelect(fftInfoIndex); // 创建带选中状态的整型对象
        } else { // 非首次设置
            this.fftInfoIndex.setValue(fftInfoIndex); // 更新索引值
            setAllUnSelect(); // 取消所有项的选中状态
            this.fftInfoIndex.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }


    /**
     * 获取FFT信息显示开关状态
     * @return 带选中状态的布尔数据Bean
     */
    public RxBooleanWithSelect isShowFftInfo() { // 获取FFT信息显示开关
        return showFftInfo; // 返回FFT信息显示开关数据
    }

    /**
     * 设置FFT信息显示开关状态，非首次设置时自动取消其他项选中并标记当前项
     * @param showFftInfo FFT信息显示开关布尔值
     */
    public void setShowFftInfo(boolean showFftInfo) { // 设置FFT信息显示开关
        if (this.showFftInfo == null) { // 首次设置
            this.showFftInfo = new RxBooleanWithSelect(showFftInfo); // 创建带选中状态的布尔对象
        } else { // 非首次设置
            this.showFftInfo.setValue(showFftInfo); // 更新布尔值
            setAllUnSelect(); // 取消所有项的选中状态
            this.showFftInfo.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 取消所有可配置项的选中状态，确保同一时刻只有一个项被标记为选中
     */
    private void setAllUnSelect() { // 取消所有项选中状态
//        showFftInfo.setRxMsgSelect(false);
        fftInfoIndex.setRxMsgSelect(false); // 取消FFT信息索引选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含FFT信息索引的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayFftInfo{" + // 类名前缀
//                "showFftInfo=" + showFftInfo +
                ", fftInfoIndex=" + fftInfoIndex + // FFT信息索引
                '}'; // 类名后缀
    }
}
