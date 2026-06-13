package com.micsig.tbook.scope.measure;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MeasureStaticsBean - 测量统计数据Bean类                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Measure模块的测量统计数据Bean类，位于measure包下，                          ║
 * ║   用于封装和传递测量统计结果数据。                                            ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 存储测量统计结果（当前值、最小值、最大值、平均值、标准差）               ║
 * ║   2. 提供统计数据的访问和修改接口                                            ║
 * ║   3. 标识统计数据所属的通道和测量类型                                        ║
 * ║                                                                              ║
 * ║ 【数据字段】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        数据字段说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【标识字段】                                                        │ ║
 * ║   │   - chIdx        通道索引（只读）                                     │ ║
 * ║   │   - measureType  测量类型（只读）                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【统计数据字段】                                                    │ ║
 * ║   │   - val          当前测量值                                          │ ║
 * ║   │   - minVal       最小测量值                                          │ ║
 * ║   │   - maxVal       最大测量值                                          │ ║
 * ║   │   - averageVal   平均测量值                                          │ ║
 * ║   │   - mqdVal       标准差（均方根差）                                   │ ║
 * ║   │   - nums         样本数量                                            │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【统计指标说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                                                                      │ ║
 * ║   │   【当前值 Val】                                                      │ ║
 * ║   │   最近一次测量值，反映当前测量状态                                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【最小值 MinVal】                                                   │ ║
 * ║   │   所有测量值中的最小值，反映测量的下限                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【最大值 MaxVal】                                                   │ ║
 * ║   │   所有测量值中的最大值，反映测量的上限                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【平均值 AverageVal】                                               │ ║
 * ║   │   所有测量值的算术平均值，反映测量的平均水平                           ║
 * ║   │   计算公式：Average = Σ(val) / N                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   【标准差 MqdVal】                                                   │ ║
 * ║   │   均方根差（Mean Quadratic Deviation），反映数据的离散程度           │ ║
 * ║   │   计算公式：Mqd = √(Σ(val - Average)² / N)                          │ ║
 * ║   │   数值越小表示测量越稳定                                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【样本数 Nums】                                                     │ ║
 * ║   │   当前统计的测量值数量，最大值为10000                                 │ ║
 * ║   │                                                                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. UI显示：在测量统计界面显示统计结果                                     ║
 * ║   2. 数据传递：在模块间传递测量统计数据                                     ║
 * ║   3. 数据存储：保存测量统计结果                                            ║
 * ║   4. 稳定性分析：通过标准差判断测量稳定性                                   ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 数据传输对象（DTO）模式                                                ║
 * ║   - JavaBean规范：私有字段、公共getter/setter方法                          ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 非线程安全，外部调用需要同步保护                                        ║
 * ║   - 建议在单线程环境中使用或使用synchronized保护                            ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - MeasureStatics: 测量统计类，创建和使用此Bean                           ║
 * ║   - Measure.MeasureType: 测量类型定义                                      ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 测量统计数据Bean类
 * 用于封装测量统计结果，包括当前值、最小值、最大值、平均值、标准差等
 *
 * <p><b>数据字段：</b></p>
 * <ul>
 *   <li>chIdx: 通道索引（只读）</li>
 *   <li>measureType: 测量类型（只读）</li>
 *   <li>val: 当前测量值</li>
 *   <li>minVal: 最小测量值</li>
 *   <li>maxVal: 最大测量值</li>
 *   <li>averageVal: 平均测量值</li>
 *   <li>mqdVal: 标准差（均方根差）</li>
 *   <li>nums: 样本数量</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建统计Bean
 * MeasureStaticsBean bean = new MeasureStaticsBean(chIdx, measureType);
 *
 * // 设置统计数据
 * bean.setVal(1.234);
 * bean.setMinVal(1.100);
 * bean.setMaxVal(1.300);
 * bean.setAverageVal(1.200);
 * bean.setMqdVal(0.050);
 * bean.setNums(100);
 *
 * // 获取统计数据
 * double currentVal = bean.getVal();
 * double avgVal = bean.getAverageVal();
 * </pre>
 *
 * @see MeasureStatics
 * @see Measure.MeasureType
 */
public class MeasureStaticsBean {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 标识字段（只读）
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 通道索引，标识统计数据所属的通道 */
    private final int chIdx;

    /** 测量类型，标识统计数据的测量类型（周期、频率、幅度等） */
    private final int measureType;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 统计数据字段
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 当前测量值，最近一次测量的结果 */
    private double val=0;

    /** 平均测量值，所有测量值的算术平均值 */
    private double averageVal=0;

    /** 最大测量值，所有测量值中的最大值 */
    private double maxVal=0;

    /** 最小测量值，所有测量值中的最小值 */
    private double minVal=0;

    /** 标准差（均方根差），反映测量值的离散程度，数值越小表示测量越稳定 */
    private double mqdVal=0;

    /** 样本数量，当前统计的测量值数量，最大值为10000 */
    private int nums = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造MeasureStaticsBean实例
     *
     * @param chIdx 通道索引
     * @param measureType 测量类型
     */
    public MeasureStaticsBean(int chIdx,int measureType){
        this.chIdx = chIdx;                                                          // 设置通道索引
        this.measureType = measureType;                                              // 设置测量类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 标识字段访问方法（只读）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取通道索引
     *
     * @return 通道索引
     */
    public int getChIdx() {
        return chIdx;                                                                // 返回通道索引
    }

    /**
     * 获取测量类型
     *
     * @return 测量类型
     * @see Measure.MeasureType
     */
    public int getMeasureType() {
        return measureType;                                                          // 返回测量类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Setter方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置当前测量值
     *
     * @param val 当前测量值
     */
    public void setVal(double val) {
        this.val = val;                                                              // 设置当前值
    }

    /**
     * 设置平均测量值
     *
     * @param averageVal 平均测量值
     */
    public void setAverageVal(double averageVal) {
        this.averageVal = averageVal;                                                // 设置平均值
    }

    /**
     * 设置最大测量值
     *
     * @param maxVal 最大测量值
     */
    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;                                                        // 设置最大值
    }

    /**
     * 设置最小测量值
     *
     * @param minVal 最小测量值
     */
    public void setMinVal(double minVal) {
        this.minVal = minVal;                                                        // 设置最小值
    }

    /**
     * 设置标准差（均方根差）
     *
     * @param mqdVal 标准差值
     */
    public void setMqdVal(double mqdVal) {
        this.mqdVal = mqdVal;                                                        // 设置标准差
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Getter方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前测量值
     *
     * @return 当前测量值
     */
    public double getVal() {
        return val;                                                                  // 返回当前值
    }

    /**
     * 获取平均测量值
     *
     * @return 平均测量值
     */
    public double getAverageVal() {
        return averageVal;                                                           // 返回平均值
    }

    /**
     * 获取最大测量值
     *
     * @return 最大测量值
     */
    public double getMaxVal() {
        return maxVal;                                                               // 返回最大值
    }

    /**
     * 获取最小测量值
     *
     * @return 最小测量值
     */
    public double getMinVal() {
        return minVal;                                                               // 返回最小值
    }

    /**
     * 获取标准差（均方根差）
     *
     * @return 标准差值
     */
    public double getMqdVal() {
        return mqdVal;                                                               // 返回标准差
    }

    /**
     * 获取样本数量
     *
     * @return 样本数量
     */
    public int getNums() {
        return nums;                                                                 // 返回样本数
    }

    /**
     * 设置样本数量
     *
     * @param nums 样本数量
     */
    public void setNums(int nums) {
        this.nums = nums;                                                            // 设置样本数
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Object方法重写
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 返回对象的字符串表示
     * 用于调试和日志输出
     *
     * @return 包含所有字段值的字符串
     */
    @Override
    public String toString() {
        return "MeasureStaticsBean{" +                                               // 类名
                "chIdx=" + chIdx +                                                   // 通道索引
                ", measureType=" + measureType +                                     // 测量类型
                ", val=" + val +                                                     // 当前值
                ", averageVal=" + averageVal +                                       // 平均值
                ", maxVal=" + maxVal +                                               // 最大值
                ", minVal=" + minVal +                                               // 最小值
                ", mqdVal=" + mqdVal +                                               // 标准差
                '}';                                                                 // 结束括号
    }
}
