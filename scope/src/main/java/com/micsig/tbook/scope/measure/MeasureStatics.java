package com.micsig.tbook.scope.measure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MeasureStatics - 示波器测量统计类                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Measure模块的测量统计类，位于measure包下，                                  ║
 * ║   提供示波器测量值的统计分析功能。                                            ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 统计测量值的最小值、最大值、平均值                                      ║
 * ║   2. 计算测量值的标准差（均方根差）                                          ║
 * ║   3. 管理测量统计项的启用/禁用状态                                           ║
 * ║   4. 提供统计结果访问接口                                                    ║
 * ║                                                                              ║
 * ║ 【统计指标】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        统计指标说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【当前值】Val                                                       │ ║
 * ║   │   - 最近一次测量值                                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【最小值】Min                                                       │ ║
 * ║   │   - 所有测量值中的最小值                                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【最大值】Max                                                       │ ║
 * ║   │   - 所有测量值中的最大值                                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【平均值】Average                                                   │ ║
 * ║   │   - 所有测量值的算术平均值                                           │ ║
 * ║   │   - 计算公式：Average = Σ(val) / N                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【标准差】Mqd (Mean Quadratic Deviation)                           │ ║
 * ║   │   - 测量值的均方根差，反映数据的离散程度                             │ ║
 * ║   │   - 计算公式：Mqd = √(Σ(val - Average)² / N)                        │ ║
 * ║   │                                                                      │ ║
 * ║   │   【样本数】Nums                                                      │ ║
 * ║   │   - 当前统计的测量值数量                                             │ ║
 * ║   │   - 最大值：10000                                                    │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据结构】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                    MeasureStatics结构                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   MeasureStatics                                                     │ ║
 * ║   │   ├── measure: Measure (关联的测量对象)                              │ ║
 * ║   │   └── maps: Map<Integer, MeasureItem> (测量项统计映射)               │ ║
 * ║   │       └── MeasureItem (内部类)                                       │ ║
 * ║   │           ├── measureStaticsBean: MeasureStaticsBean (统计结果)      │ ║
 * ║   │           ├── list: List<Double> (测量值列表，最多10000个)           │ ║
 * ║   │           └── sum: double (测量值累加和)                             │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【统计流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 启用统计项  │───▶│ 测量计算    │───▶│ 统计计算    │                   ║
 * ║   │ setEnable   │    │ MeasureCalc │    │ calcStatics │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 自动测量统计：周期、频率、幅度等的统计分析                             ║
 * ║   2. 测量稳定性分析：通过标准差判断测量值的稳定性                           ║
 * ║   3. 测量范围分析：通过最大值、最小值了解测量值的分布范围                   ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 使用synchronized保护共享数据                                            ║
 * ║   - LinkedHashMap保证遍历顺序                                               ║
 * ║                                                                              ║
 * ║ 【性能优化】                                                                 ║
 * ║   - 滑动窗口：最多保留10000个测量值                                         ║
 * ║   - 增量计算：维护sum变量，避免每次重新计算总和                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Measure: 测量类，获取测量值                                             ║
 * ║   - MeasureStaticsBean: 统计结果数据类                                      ║
 * ║   - MeasureType: 测量类型定义                                               ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器测量统计类
 * 负责统计测量值的最小值、最大值、平均值、标准差等统计信息
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>统计项管理：启用/禁用指定测量类型的统计</li>
 *   <li>统计计算：计算最小值、最大值、平均值、标准差</li>
 *   <li>滑动窗口：最多保留10000个测量值</li>
 *   <li>结果访问：获取指定测量类型的统计结果</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建统计对象
 * MeasureStatics statics = new MeasureStatics(measure);
 *
 * // 启用周期统计
 * statics.setStaticsItemEnable(Measure.MeasureType.MEASURE_PERIOD, true);
 *
 * // 执行统计计算
 * statics.calcStatics();
 *
 * // 获取统计结果
 * MeasureStaticsBean bean = statics.getStatics(Measure.MeasureType.MEASURE_PERIOD);
 * </pre>
 *
 * @see Measure
 * @see MeasureStaticsBean
 * @see Measure.MeasureType
 */
public class MeasureStatics {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "MeasureStatics";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 关联的测量对象，用于获取测量值 */
    private final Measure measure;

    /** 测量项统计映射表，Key为测量类型，Value为MeasureItem */
    private final Map<Integer,MeasureItem> maps = new LinkedHashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造MeasureStatics实例
     *
     * @param measure 关联的测量对象
     */
    public MeasureStatics(Measure measure){
        this.measure = measure;                                                      // 保存测量对象引用
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 统计项管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置统计项启用状态
     * 线程安全方法
     *
     * @param measureType 测量类型
     * @param bEnable true: 启用统计
     *                false: 禁用统计
     */
    public void setStaticsItemEnable(@Measure.MeasureType.MeasureItemType int measureType,boolean bEnable){
        synchronized (this){                                                          // 同步保护
            if(maps.containsKey(measureType)){                                       // 统计项已存在
                if(!bEnable){                                                        // 禁用统计
                    maps.remove(measureType);                                        // 移除统计项
                }
            }else{                                                                   // 统计项不存在
                if(bEnable) {                                                        // 启用统计
                    maps.put(measureType, new MeasureItem(measure.getChIdx(), measureType)); // 创建统计项
                }
            }
        }
    }

    /**
     * 清除所有统计项
     * 线程安全方法
     */
    public void clear(){
        synchronized (this){                                                          // 同步保护
            maps.clear();                                                             // 清空映射表
        }
    }

    /**
     * 重置所有统计项
     * 清空统计数据，重置统计结果
     * 线程安全方法
     */
    public void reset(){
        synchronized (this) {                                                         // 同步保护
            for (MeasureItem m:maps.values()                                         // 遍历所有统计项
                 ) {
                m.reset();                                                           // 重置统计项
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 统计结果访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取指定测量类型的统计结果
     * 线程安全方法
     *
     * @param measureType 测量类型
     * @return 统计结果Bean，如果统计项不存在返回null
     */
    public MeasureStaticsBean getStatics(@Measure.MeasureType.MeasureItemType int measureType){
        MeasureStaticsBean measureStaticsBean = null;                                // 初始化返回值
        synchronized (this) {                                                         // 同步保护
            MeasureItem measureItem = maps.get(measureType);                         // 获取统计项
            if (measureItem != null) {                                               // 统计项存在
                measureStaticsBean = measureItem.getMeasureStaticsBean();            // 获取统计结果
            }
        }
        return measureStaticsBean;                                                   // 返回统计结果
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 统计计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 执行统计计算
     * 遍历所有启用的测量项，更新统计信息
     * 线程安全方法
     */
    public void calcStatics(){
        synchronized (this) {                                                         // 同步保护
            for (int i = Measure.MeasureType.MEASURE_FIRST; i <= Measure.MeasureType.MEASURE_LAST; i++) { // 遍历所有测量类型
                if(maps.containsKey(i)                                               // 统计项存在
                        && measure.isMeasureItemEnable(i)                            // 测量项已启用
                        && measure.isMeasureItemValid(i)){                           // 测量项有效
                    MeasureItem measureItem = maps.get(i);                           // 获取统计项
                    if(measureItem != null) {                                        // 统计项有效
                        measureItem.calcStatics(measure.getMeasureItemVal(i));       // 执行统计计算
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部类 - MeasureItem
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 测量统计项内部类
     * 负责单个测量类型的统计计算
     *
     * <p><b>统计指标：</b></p>
     * <ul>
     *   <li>当前值（Val）：最近一次测量值</li>
     *   <li>最小值（Min）：所有测量值中的最小值</li>
     *   <li>最大值（Max）：所有测量值中的最大值</li>
     *   <li>平均值（Average）：所有测量值的算术平均值</li>
     *   <li>标准差（Mqd）：均方根差，反映数据的离散程度</li>
     *   <li>样本数（Nums）：当前统计的测量值数量</li>
     * </ul>
     */
    private static class MeasureItem{

        // ────────────────────────────────────────────────────────────────────────────
        // 常量定义
        // ────────────────────────────────────────────────────────────────────────────

        /** 最大样本数，超过此数量时采用滑动窗口策略 */
        private static final int MAX_NUMS = 10000;

        // ────────────────────────────────────────────────────────────────────────────
        // 成员变量
        // ────────────────────────────────────────────────────────────────────────────

        /** 统计结果Bean */
        private final MeasureStaticsBean measureStaticsBean;

        /** 测量值列表，使用滑动窗口策略，最多保留MAX_NUMS个值 */
        private final List<Double> list = new ArrayList<>();

        /** 测量值累加和，用于快速计算平均值 */
        private double sum = 0;

        // ────────────────────────────────────────────────────────────────────────────
        // 构造方法
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 构造MeasureItem实例
         *
         * @param chIdx 通道索引
         * @param measureType 测量类型
         */
        public MeasureItem(int chIdx,int measureType){
            measureStaticsBean = new MeasureStaticsBean(chIdx,measureType);          // 创建统计结果Bean
        }

        // ────────────────────────────────────────────────────────────────────────────
        // 重置方法
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 重置统计数据
         * 清空测量值列表，重置所有统计结果
         */
        public void reset(){
            list.clear();                                                             // 清空测量值列表
            sum = 0;                                                                  // 重置累加和
            measureStaticsBean.setVal(0);                                             // 重置当前值
            measureStaticsBean.setAverageVal(0);                                      // 重置平均值
            measureStaticsBean.setMaxVal(0);                                          // 重置最大值
            measureStaticsBean.setMinVal(0);                                          // 重置最小值
            measureStaticsBean.setMqdVal(0);                                          // 重置标准差
            measureStaticsBean.setNums(0);                                            // 重置样本数
        }

        // ────────────────────────────────────────────────────────────────────────────
        // 添加测量值方法
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 添加测量值到统计列表
         * 使用滑动窗口策略，超过MAX_NUMS时移除最旧的值
         *
         * @param val 测量值
         */
        private void add(double val){
            sum += val;                                                               // 累加到总和
            list.add(val);                                                            // 添加到列表
            if(list.size() > MAX_NUMS){                                               // 超过最大样本数
                sum -= list.get(0);                                                   // 从总和中减去最旧的值
                list.remove(0);                                                       // 移除最旧的值
            }
        }

        // ────────────────────────────────────────────────────────────────────────────
        // 获取统计结果方法
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 获取统计结果Bean
         *
         * @return 统计结果Bean
         */
        public MeasureStaticsBean getMeasureStaticsBean(){
            return measureStaticsBean;                                                // 返回统计结果
        }

        // ────────────────────────────────────────────────────────────────────────────
        // 统计计算方法
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * 执行统计计算
         * 计算最小值、最大值、平均值、标准差
         *
         * <p><b>计算公式：</b></p>
         * <ul>
         *   <li>平均值：Average = Σ(val) / N</li>
         *   <li>标准差：Mqd = √(Σ(val - Average)² / N)</li>
         * </ul>
         *
         * @param val 当前测量值
         */
        public void calcStatics(double val){
            add(val);                                                                 // 添加测量值

            int nums = list.size();                                                  // 获取样本数
            double min,max, v1,v2,ss;                                                // 声明统计变量
            ss = 0;                                                                   // 初始化平方和
            max = min = list.get(0);                                                 // 初始化最大值、最小值
            v1 = sum/nums;                                                           // 计算平均值

            // 遍历所有测量值，计算最大值、最小值、标准差
            for(int i=0;i<nums;i++){                                                 // 遍历测量值列表
                v2 = list.get(i);                                                    // 获取当前测量值
                if(v2 > max){                                                        // 大于当前最大值
                    max = v2;                                                        // 更新最大值
                }else if(v2 < min){                                                  // 小于当前最小值
                    min = v2;                                                        // 更新最小值
                }
                v2 -= v1;                                                            // 计算与平均值的差
                ss += (v2 * v2);                                                     // 累加平方差
            }

            // 更新统计结果
            measureStaticsBean.setVal(val);                                          // 设置当前值
            measureStaticsBean.setAverageVal(v1);                                    // 设置平均值
            measureStaticsBean.setMaxVal(max);                                       // 设置最大值
            measureStaticsBean.setMinVal(min);                                       // 设置最小值
            measureStaticsBean.setMqdVal(Math.sqrt(ss/nums));                        // 设置标准差
            measureStaticsBean.setNums(nums);                                        // 设置样本数

        }
    }
}
