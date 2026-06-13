package com.micsig.tbook.scope.Auto;  // 定义包名：示波器自动设置功能模块

import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供通道采样状态查询
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态数据结构，包含频率测量数据

/**
 * 频率计数器管理器 - 信号频率测量执行器
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Auto（示波器自动设置功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 频率测量执行层</li>
 *   <li>职责类型：频率计算、FPGA状态数据处理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>从FPGA状态数据中提取频率测量原始数据</li>
 *   <li>根据N和T值计算信号频率</li>
 *   <li>更新FreqCounter的频率测量结果</li>
 * </ul>
 * 
 * <p><b>频率计算公式：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   频率计算原理                                                            │
 * │                                                                          │
 * │   原始公式：f = N * 1e9 / (T * fpga时钟周期)                              │
 * │   其中：                                                                  │
 * │   - N：周期计数器值（FPGA统计的信号周期数）                                │
 * │   - T：时间计数器值（FPGA统计的时间）                                      │
 * │   - fpga时钟周期：FPGA内部时钟周期，单位ns                                 │
 * │                                                                          │
 * │   实际公式（fpga时钟周期=8ns时）：                                         │
 * │   f = N * 3.75e8 / T                                                     │
 * │   = N * 1e9 / (T * 8 / 3)                                                │
 * │   = N * 3 * 1e8 / T                                                      │
 * │                                                                          │
 * │   常量说明：                                                              │
 * │   - 3.75e8 = 1e9 / 8 * 3 = 375,000,000                                  │
 * │   - 该常量由FPGA时钟频率决定                                              │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>调用流程：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FreqCounterManage 调用流程                                              │
 * │                                                                          │
 * │   AutoService.onDo()                                                     │
 * │        │                                                                 │
 * │        ▼                                                                 │
 * │   freqCounterManage.freqDo(fpgaStatus)                                   │
 * │        │                                                                 │
 * │        ├── 获取FPGA频率数据（FpgaFreq）                                    │
 * │        │                                                                 │
 * │        ├── 检查数据有效性                                                  │
 * │        │                                                                 │
 * │        ├── 检查目标通道是否在采样                                          │
 * │        │                                                                 │
 * │        ├── 计算频率值：fz = N * 3.75e8 / T                               │
 * │        │                                                                 │
 * │        └── 更新FreqCounter的频率值                                        │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：FreqCounter（频率计数器，获取目标通道、更新频率值）</li>
 *   <li>依赖：Scope（示波器核心管理，查询通道采样状态）</li>
 *   <li>依赖：FPGA_Status（FPGA状态数据，包含频率测量原始数据）</li>
 *   <li>被依赖：AutoService（自动设置服务，调用freqDo执行频率测量）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-13
 * @see FreqCounter 频率计数器
 * @see AutoService 自动设置服务
 */
public class FreqCounterManage {
    
    /**
     * 执行频率测量计算
     * 
     * <p>从FPGA状态数据中提取频率测量原始数据（N和T值），
     * 计算信号频率并更新到FreqCounter。
     * 
     * <p><b>计算公式：</b>
     * <ul>
     *   <li>N = 0 时：频率 = 0（无信号或直流）</li>
     *   <li>N > 0 时：频率 = N * 3.75e8 / T（单位：Hz）</li>
     * </ul>
     * 
     * <p><b>执行条件：</b>
     * <ul>
     *   <li>FPGA频率数据有效（freq.isVaild() = true）</li>
     *   <li>目标通道在采样列表中</li>
     * </ul>
     * 
     * @param fpgaStatus FPGA状态数据，包含频率测量原始数据
     * @return true表示频率测量成功且有效，false表示测量失败或无效
     */
    public boolean freqDo(FPGA_Status fpgaStatus){
        FPGA_Status.FpgaFreq freq = fpgaStatus.getFreq();  // 获取FPGA频率测量数据对象
        if(freq.isVaild()){  // 检查频率数据是否有效
            // 算式：fz = N*1e9/(T*fpga时钟周期-单位ns)，计算的结果单位为Hz
            // mini的时钟周期值为8ns
            double fz = -1;  // 初始化频率值为-1（无效状态）
            int chIdx = FreqCounter.getInstance().getChIdx();  // 获取频率测量的目标通道索引
            if(Scope.getInstance().isChannelInSample(chIdx)){  // 检查目标通道是否在采样列表中
                if(freq.getN() == 0){  // 检查周期计数器是否为0（无信号或直流）
                    fz = 0;  // 频率为0，表示无周期信号
                }else{  // 有周期信号
                    fz = freq.getN()*3.75e8/freq.getT();  // 计算频率：f = N * 3.75e8 / T
                }
                FreqCounter.getInstance().setFreqVal(fz);  // 更新频率计数器的频率值
                return true;  // 返回true，表示频率测量成功
            }
        }else{  // 频率数据无效
            FreqCounter.getInstance().setFreqVal(-1);  // 设置频率值为-1（无效状态）
        }
        return false;  // 返回false，表示频率测量失败或无效
    }
}
