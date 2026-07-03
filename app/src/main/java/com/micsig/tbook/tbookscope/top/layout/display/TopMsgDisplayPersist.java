package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中状态的布尔数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal; // 导入水平列表选择项数据Bean

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 余辉(Persist)显示详情数据模型                    ║
 * ║  核心职责: 封装Display菜单下"余辉"子页面的所有可配置项数据                     ║
 * ║  架构设计: IDisplayDetail策略接口的具体实现，作为TopMsgDisplay的详情载体      ║
 * ║  数据流向: TopLayoutDisplayPersist → TopMsgDisplayPersist → TopMsgDisplay║
 * ║  依赖关系: TopBeanChannel、RxBooleanWithSelect、TopBeanHorizontal          ║
 * ║  使用场景: 用户在Display-余辉页面修改余辉模式/清除/调节时间时携带数据           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayPersist implements IDisplayDetail { // 余辉显示详情数据模型，实现IDisplayDetail接口

    private TopBeanChannel persist, fftPersist; // 常规余辉模式、FFT余辉模式
    private RxBooleanWithSelect clear, fftClear; // 常规余辉清除标记、FFT余辉清除标记
    private TopBeanHorizontal adjust, fftAdjust; // 常规余辉调节时间、FFT余辉调节时间

    /**
     * 获取常规余辉模式
     * @return 常规余辉模式通道数据Bean
     */
    public TopBeanChannel getPersist() { // 获取常规余辉模式
        return persist; // 返回常规余辉模式数据
    }

    /**
     * 设置常规余辉模式，非首次设置时自动取消其他项选中并标记当前项
     * @param persist 常规余辉模式通道数据Bean
     */
    public void setPersist(TopBeanChannel persist) { // 设置常规余辉模式
        if (this.persist == null) { // 首次设置
            this.persist = persist; // 直接赋值
        } else { // 非首次设置（切换余辉模式时）
            this.persist = persist; // 更新余辉模式引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.persist.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取FFT余辉模式
     * @return FFT余辉模式通道数据Bean
     */
    public TopBeanChannel getFftPersist() { // 获取FFT余辉模式
        return fftPersist; // 返回FFT余辉模式数据
    }

    /**
     * 设置FFT余辉模式，非首次设置时自动取消其他项选中并标记当前项
     * @param fftPersist FFT余辉模式通道数据Bean
     */
    public void setFftPersist(TopBeanChannel fftPersist) { // 设置FFT余辉模式
        if (this.fftPersist == null) { // 首次设置
            this.fftPersist = fftPersist; // 直接赋值
        } else { // 非首次设置（切换FFT余辉模式时）
            this.fftPersist = fftPersist; // 更新FFT余辉模式引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.fftPersist.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }


    /**
     * 获取常规余辉清除标记
     * @return 带选中状态的布尔数据Bean
     */
    public RxBooleanWithSelect getClear() { // 获取常规余辉清除标记
        return clear; // 返回清除标记数据
    }

    /**
     * 设置常规余辉清除标记，非首次设置时自动取消其他项选中并标记当前项
     * @param clear 清除标记布尔值
     */
    public void setClear(boolean clear) { // 设置常规余辉清除标记
        if (this.clear == null) { // 首次设置
            this.clear = new RxBooleanWithSelect(clear); // 创建带选中状态的布尔对象
        } else { // 非首次设置
            this.clear.setValue(clear); // 更新布尔值
            setAllUnSelect(); // 取消所有项的选中状态
            this.clear.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取FFT余辉清除标记
     * @return 带选中状态的布尔数据Bean
     */
    public RxBooleanWithSelect getFftClear() { // 获取FFT余辉清除标记
        return fftClear; // 返回FFT清除标记数据
    }

    /**
     * 设置FFT余辉清除标记，非首次设置时自动取消其他项选中并标记当前项
     * @param fftClear FFT清除标记布尔值
     */
    public void setFftClear(boolean fftClear) { // 设置FFT余辉清除标记
        if (this.fftClear == null) { // 首次设置
            this.fftClear = new RxBooleanWithSelect(fftClear); // 创建带选中状态的布尔对象
        } else { // 非首次设置
            this.fftClear.setValue(fftClear); // 更新布尔值
            setAllUnSelect(); // 取消所有项的选中状态
            this.fftClear.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取常规余辉调节时间
     * @return 常规余辉调节时间水平列表数据Bean
     */
    public TopBeanHorizontal getAdjust() { // 获取常规余辉调节时间
        return adjust; // 返回调节时间数据
    }

    /**
     * 设置常规余辉调节时间，非首次设置时自动取消其他项选中并标记当前项
     * @param adjust 常规余辉调节时间水平列表数据Bean
     */
    public void setAdjust(TopBeanHorizontal adjust) { // 设置常规余辉调节时间
        if (this.adjust == null) { // 首次设置
            this.adjust = adjust; // 直接赋值
        } else { // 非首次设置
            this.adjust = adjust; // 更新调节时间引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.adjust.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 获取FFT余辉调节时间
     * @return FFT余辉调节时间水平列表数据Bean
     */
    public TopBeanHorizontal getFftAdjust() { // 获取FFT余辉调节时间
        return fftAdjust; // 返回FFT调节时间数据
    }

    /**
     * 设置FFT余辉调节时间，非首次设置时自动取消其他项选中并标记当前项
     * @param fftAdjust FFT余辉调节时间水平列表数据Bean
     */
    public void setFftAdjust(TopBeanHorizontal fftAdjust) { // 设置FFT余辉调节时间
        if (this.fftAdjust == null) { // 首次设置
            this.fftAdjust = fftAdjust; // 直接赋值
        } else { // 非首次设置
            this.fftAdjust = fftAdjust; // 更新FFT调节时间引用
            setAllUnSelect(); // 取消所有项的选中状态
            this.fftAdjust.setRxMsgSelect(true); // 标记当前项为选中状态
        }
    }

    /**
     * 取消所有可配置项的选中状态，确保同一时刻只有一个项被标记为选中
     */
    private void setAllUnSelect() { // 取消所有项选中状态
        persist.setRxMsgSelect(false); // 取消常规余辉模式选中
        fftPersist.setRxMsgSelect(false); // 取消FFT余辉模式选中
        clear.setRxMsgSelect(false); // 取消常规清除标记选中
        fftClear.setRxMsgSelect(false); // 取消FFT清除标记选中
        adjust.setRxMsgSelect(false); // 取消常规调节时间选中
        fftAdjust.setRxMsgSelect(false); // 取消FFT调节时间选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     * @return 包含所有余辉配置项的字符串
     */
    @Override // 重写toString方法
    public String toString() { // 返回字符串表示
        return "TopMsgDisplayPersist{" + // 类名前缀
                "persist=" + persist + // 常规余辉模式信息
                ", clear=" + clear + // 常规清除标记信息
                ", adjust=" + adjust + // 常规调节时间信息
                ", fftPersist=" + fftPersist + // FFT余辉模式信息
                ", fftClear=" + fftClear + // FFT清除标记信息
                ", fftAdjust=" + fftAdjust + // FFT调节时间信息
                '}'; // 类名后缀
    }
}
