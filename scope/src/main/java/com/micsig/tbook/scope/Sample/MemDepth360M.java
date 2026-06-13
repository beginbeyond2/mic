package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;


/**
 * 360M点存储深度实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 存储深度具体实现</li>
 *   <li>设计模式：继承MemDepth抽象类，实现具体存储深度计算</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现360M点存储深度的具体计算逻辑</li>
 *   <li>提供6个存储深度档位（Auto/360M/36M/3.6M/360K/36K）</li>
 *   <li>支持自动模式和段采样模式的存储深度计算</li>
 *   <li>实现时基与存储深度的精确映射</li>
 * </ul>
 * 
 * <p><b>存储深度档位：</b>
 * <pre>
 * 索引  名称           双通道(默认)   四通道
 * ───────────────────────────────────────────────
 * 0     Auto          自动计算       自动计算
 * 1     360/180M      360M点         180M点
 * 2     36/18M        36M点          18M点
 * 3     3.6/1.8M      3.6M点         1.8M点
 * 4     360/180K      360K点         180K点
 * 5     36/18K        36K点          18K点
 * </pre>
 * 
 * <p><b>通道系数特点：</b>
 * <ul>
 *   <li>与MemDepth18M不同，本类使用特殊的通道系数计算</li>
 *   <li>4通道: 系数=1</li>
 *   <li>其他(2通道): 系数=2</li>
 * </ul>
 * 
 * <p><b>适用产品：</b>
 * <ul>
 *   <li>MHO38/MHO28（中端示波器）</li>
 *   <li>需要较大存储深度的应用场景</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：MemDepth（存储深度抽象基类）</li>
 *   <li>依赖：Scope（获取通道数和运行模式）</li>
 *   <li>依赖：HorizontalAxis（获取时基档位）</li>
 *   <li>依赖：SegmentSample（段采样管理）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户切换存储深度档位时计算实际存储深度</li>
 *   <li>时基档位变化时自动计算存储深度（Auto模式）</li>
 *   <li>段采样模式下计算段存储深度</li>
 *   <li>滚屏模式和慢时基模式的特殊处理</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see MemDepth 存储深度抽象基类
 * @see IMemDepth 存储深度接口
 * @see MemDepth1800M 1800M点存储深度实现
 * @see MemDepth18M 18M点存储深度实现
 * @see SegmentSample 段采样管理
 */
public class MemDepth360M extends MemDepth {
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数设置存储深度基准值</li>
     *   <li>初始化存储深度档位名称列表</li>
     * </ul>
     * 
     * <p><b>档位初始化：</b>
     * <ul>
     *   <li>索引0: "Auto" - 自动模式</li>
     *   <li>索引1: "360/180M" - 360M/180M点</li>
     *   <li>索引2: "36/18M" - 36M/18M点</li>
     *   <li>索引3: "3.6/1.8M" - 3.6M/1.8M点</li>
     *   <li>索引4: "360/180K" - 360K/180K点</li>
     *   <li>索引5: "36/18K" - 36K/18K点</li>
     * </ul>
     * 
     * @param memDepth 存储深度基准值（通常为360,000,000）
     */
    public MemDepth360M(int memDepth) {
        // 调用父类构造函数
        super(memDepth);
        
        // 初始化存储深度档位名称列表
        addItem("Auto");                    // 索引0：自动模式
        addItem("360/180M");                // 索引1：360M/180M点
        addItem("36/18M");                  // 索引2：36M/18M点
        addItem("3.6/1.8M");                // 索引3：3.6M/1.8M点
        addItem("360/180K");                // 索引4：360K/180K点
        addItem("36/18K");                  // 索引5：36K/18K点
    }

    /**
     * 获取初始化时的存储深度档位名称
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回所有可用档位名称的拼接字符串</li>
     *   <li>用于配置保存和恢复</li>
     * </ul>
     * 
     * @return 档位名称字符串
     */
    @Override
    public String getMemDepthInitName() {
        return "Auto,360/180M,36/18M,3.6/1.8M,360/180K,36/18K";
    }

    /**
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据档位索引和通道数计算实际存储深度</li>
     *   <li>支持固定档位和Auto自动模式</li>
     *   <li>支持段采样模式</li>
     * </ul>
     * 
     * <p><b>通道系数计算（特殊）：</b>
     * <pre>
     * 与MemDepth18M不同，本类使用特殊的通道系数：
     *   4通道: 系数 = 1
     *   其他(2通道): 系数 = 2
     * 
     * 这意味着：
     *   双通道模式下，存储深度减半
     *   四通道模式下，存储深度不变
     * </pre>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 实际存储深度 = 基准值 × 倍率 ÷ 通道系数
     * 
     * 其中：
     *   基准值 = 36000
     *   通道系数 = 1(4通道) 或 2(其他)
     * 
     * 各档位倍率：
     *   档位1: ×10000 (360M点)
     *   档位2: ×1000  (36M点)
     *   档位3: ×100   (3.6M点)
     *   档位4: ×10    (360K点)
     *   档位5: ×1     (36K点)
     * </pre>
     * 
     * @param chCnt 采样通道数（2/4）
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth(int chCnt) {
        // 获取段采样实例
        SegmentSample segmentSample = SegmentSample.getInstance();
        
        // 基准值：36000（用于计算各档位）
        int cfg = 36000;

        // 通道系数计算（特殊逻辑）
        // 与MemDepth18M不同，这里4通道系数为1，其他为2
        int coef = 1;
        if (chCnt == 4) {
            coef = 1;   // 四通道模式：系数为1
        } else {
            coef = 2;   // 双通道模式：系数为2
        }
        
        // 根据档位索引计算存储深度
        switch (getMemDepthItem()) {
            case 1:
                // 档位1: 360M/180M点
                // 计算: 36000 × 10000 ÷ 系数
                return cfg * 1000 * 10 / coef;
                
            case 2:
                // 档位2: 36M/18M点
                // 计算: 36000 × 1000 ÷ 系数
                return cfg * 1000 / coef;
                
            case 3:
                // 档位3: 3.6M/1.8M点
                // 计算: 36000 × 100 ÷ 系数
                return cfg * 100 / coef;
                
            case 4:
                // 档位4: 360K/180K点
                // 计算: 36000 × 10 ÷ 系数
                return cfg * 10 / coef;
                
            case 5:
                // 档位5: 36K/18K点
                // 计算: 36000 × 1 ÷ 系数
                return cfg * 1 / coef;
                
            default:
            case 0:
                // Auto模式：自动计算存储深度
                int a = getAuto(cfg, coef);
                
                // 如果启用了段采样模式
                if (segmentSample.isSegmentEnable()) {
                    // 计算段采样存储深度
                    int s = getSegment(cfg, coef);
                    // 取Auto计算值和段计算值的较小值
                    if (s > a) {
                        s = a;
                    }
                    return s;
                }
                return a;
        }
    }
    
    /**
     * 获取实际采样存储深度（自动获取通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从Scope获取当前采样通道数</li>
     *   <li>调用getSampleMemDepth(chCnt)计算实际存储深度</li>
     * </ul>
     * 
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth() {
        Scope scope = Scope.getInstance();
        // 获取当前采样通道数并计算存储深度
        return getSampleMemDepth(scope.getChannelSampOnCnt());
    }


    /**
     * 计算段采样模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据段数计算合适的存储深度</li>
     *   <li>选择能够满足段数要求的最小存储深度</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ol>
     *   <li>获取目标段数</li>
     *   <li>段数乘以通道系数×2调整</li>
     *   <li>遍历存储深度数组[360M, 36M, 3.6M, 360K, 3.6K]</li>
     *   <li>计算每个深度能支持的段数</li>
     *   <li>返回满足段数要求的深度</li>
     * </ol>
     * 
     * @param cfg 基准值（36000）
     * @param coef 通道系数
     * @return 段采样存储深度
     */
    private int getSegment(int cfg, int coef) {
        // 获取段采样实例
        SegmentSample segmentSample = SegmentSample.getInstance();
        
        // 获取目标段数（+1是因为段数从0开始计数）
        int num = segmentSample.getSegmentNums() + 1;
        
        // 存储深度数组：从大到小排列
        // 360M → 36M → 3.6M → 360K → 3.6K
        int[] zunArray = {360 * 1000 * 1000, 36 * 1000 * 1000, 36 * 100 * 1000, 360 * 1000, 3600};
        
        int s = 0;
        
        // 段数乘以通道系数×2进行调整
        // 这是为了确保有足够的存储空间
        num *= coef * 2;
        
        // 遍历存储深度数组，找到满足段数要求的深度
        for (int i = 0; i < zunArray.length; i++) {
            // 计算当前深度能支持的段数
            s = segmentSample.getSegmentSample(zunArray[i]);
            // 如果支持的段数大于等于目标段数，返回当前深度
            if (num <= s) {
                s = zunArray[i];
                break;
            }
        }
        
        // 返回计算后的存储深度（除以通道系数）
        return s / coef;
    }

    /**
     * 计算Auto模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据时基档位精确映射存储深度</li>
     *   <li>支持滚屏模式和慢时基模式的特殊处理</li>
     *   <li>每个时基档位都有对应的存储深度值</li>
     * </ul>
     * 
     * <p><b>特殊模式处理：</b>
     * <ul>
     *   <li>滚屏模式（isInScrollMode）：返回3.6M/系数</li>
     *   <li>慢时基模式（isInSlowScaleMode）：返回3.6M/系数</li>
     * </ul>
     * 
     * <p><b>时基档位与存储深度映射表：</b>
     * <pre>
     * 时基档位        存储深度(双通道)    存储深度(四通道)
     * ──────────────────────────────────────────────────
     * <5ms/div       360M               360M
     * 5ms/div        180M               90M
     * 2ms/div        72M                36M
     * 1ms/div        36M                18M
     * 500μs/div      18M                9M
     * 200μs/div      7.2M               3.6M
     * 100μs/div      3.6M               1.8M
     * 50μs/div       1.8M               900K
     * 20μs/div       720K               360K
     * 10μs/div       360K               180K
     * 5μs/div        180K               90K
     * 2μs/div        72K                36K
     * 1μs/div        36K                18K
     * 500ns/div      18K                9K
     * 200ns/div      7.2K               3.6K
     * 100ns/div      3.6K               1.8K
     * ≤50ns/div      1800               900
     * </pre>
     * 
     * @param cfg 基准值（36000）
     * @param coef 通道系数
     * @return Auto模式存储深度
     */
    private int getAuto(int cfg, int coef) {
        Scope scope = Scope.getInstance();
        HorizontalAxis xAxis = HorizontalAxis.getInstance();
        
        // 特殊模式处理：滚屏模式或慢时基模式
        // 这两种模式使用固定的存储深度（3.6M/系数）
        if (scope.isInScrollMode() || scope.isInSlowScaleMode()) {
            return cfg * 100 / coef;  // 3.6M点
        }

        // 获取当前主时基档位ID
        int mainScaleId = xAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);

        // 快速时基档位（<5ms/div）：使用最大存储深度
        if (mainScaleId < HorizontalAxis.TSI_5mS) {
            return cfg * 1000 * 10 / coef;  // 360M点
        }
        
        // 根据时基档位精确映射存储深度
        // 每个档位都有对应的存储深度值，确保采样率匹配
        switch (mainScaleId) {
            case HorizontalAxis.TSI_5mS:
                return 180000000 / coef;    // 180M点
            case HorizontalAxis.TSI_2mS:
                return 72000000 / coef;     // 72M点
            case HorizontalAxis.TSI_1mS:
                return 36000000 / coef;     // 36M点
            case HorizontalAxis.TSI_500uS:
                return 18000000 / coef;     // 18M点
            case HorizontalAxis.TSI_200uS:
                return 7200000 / coef;      // 7.2M点
            case HorizontalAxis.TSI_100uS:
                return 3600000 / coef;      // 3.6M点
            case HorizontalAxis.TSI_50uS:
                return 1800000 / coef;      // 1.8M点
            case HorizontalAxis.TSI_20uS:
                return 720000 / coef;       // 720K点
            case HorizontalAxis.TSI_10uS:
                return 360000 / coef;       // 360K点
            case HorizontalAxis.TSI_5uS:
                return 180000 / coef;       // 180K点
            case HorizontalAxis.TSI_2uS:
                return 72000 / coef;        // 72K点
            case HorizontalAxis.TSI_1uS:
                return 36000 / coef;        // 36K点
            case HorizontalAxis.TSI_500nS:
                return 18000 / coef;        // 18K点
            case HorizontalAxis.TSI_200nS:
                return 7200 / coef;         // 7.2K点
            case HorizontalAxis.TSI_100nS:
                return 3600 / coef;         // 3.6K点
            case HorizontalAxis.TSI_50nS:
            case HorizontalAxis.TSI_20nS:
            case HorizontalAxis.TSI_10nS:
            case HorizontalAxis.TSI_5nS:
            case HorizontalAxis.TSI_2nS:
            case HorizontalAxis.TSI_1nS:
            default:
                // 超快速时基：使用最小存储深度
                return 1800;                // 1800点
        }
    }

    /**
     * 获取时基档位对应的存储深度比例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>默认实现返回1.0</li>
     *   <li>表示不调整存储深度比例</li>
     * </ul>
     * 
     * @param timeScaleId 时基档位ID
     * @return 存储深度比例（固定返回1.0）
     */
    @Override
    public double getTimeScaleRatio(int timeScaleId) {
        return 1.0;
    }

    /**
     * 获取时基档位对应的存储深度比例（指定档位）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>默认实现返回1.0</li>
     *   <li>表示不调整存储深度比例</li>
     * </ul>
     * 
     * @param timeScaleId 时基档位ID
     * @param memDepthItemIdx 存储深度档位索引
     * @return 存储深度比例（固定返回1.0）
     */
    @Override
    public double getTimeScaleRatio(int timeScaleId, int memDepthItemIdx) {
        return 1.0;
    }

    /**
     * 判断是否为特殊时基档位
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>固定返回true</li>
     *   <li>表示360M存储深度实现支持特殊时基处理</li>
     * </ul>
     * 
     * @return 固定返回true
     */
    @Override
    public boolean isSpecialTimeScale() {
        return true;
    }

    /**
     * 获取数学运算通道耦合数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回数学运算通道可用的耦合样本点数</li>
     *   <li>用于数学运算功能（加减乘除FFT等）</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>返回{360000}，表示数学运算可用的样本点数</li>
     * </ul>
     * 
     * @return 数学运算耦合数组
     */
    @Override
    public int[] getMathCouArray() {
        return new int[]{360000};
    }

    /**
     * 将存储深度字符串转换为菜单索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据存储深度数值字符串查找对应的菜单索引</li>
     *   <li>支持不同通道数下的存储深度值</li>
     * </ul>
     * 
     * <p><b>映射规则：</b>
     * <pre>
     * 字符串值              索引    档位名称
     * ─────────────────────────────────────────
     * "Auto"               0       Auto
     * "360000000"          1       360/180M (双通道360M)
     * "180000000"          1       360/180M (四通道180M)
     * "36000000"           2       36/18M (双通道36M)
     * "18000000"           2       36/18M (四通道18M)
     * "3600000"            3       3.6/1.8M (双通道3.6M)
     * "1800000"            3       3.6/1.8M (四通道1.8M)
     * "360000"             4       360/180K (双通道360K)
     * "180000"             4       360/180K (四通道180K)
     * "36000"              5       36/18K (双通道36K)
     * "18000"              5       36/18K (四通道18K)
     * </pre>
     * 
     * @param memDepth 存储深度数值字符串
     * @return 菜单索引，未找到返回-1
     */
    @Override
    public int memDepth2menuIdx(String memDepth) {
        int idx = -1;
        
        switch (memDepth) {
            case "360000000":   // 双通道360M点
            case "180000000":   // 四通道180M点
                idx = 1;        // 对应"360/180M"档位
                break;
                
            case "36000000":    // 双通道36M点
            case "18000000":    // 四通道18M点
                idx = 2;        // 对应"36/18M"档位
                break;
                
            case "3600000":     // 双通道3.6M点
            case "1800000":     // 四通道1.8M点
                idx = 3;        // 对应"3.6/1.8M"档位
                break;
                
            case "360000":      // 双通道360K点
            case "180000":      // 四通道180K点
                idx = 4;        // 对应"360/180K"档位
                break;
                
            case "36000":       // 双通道36K点
            case "18000":       // 四通道18K点
                idx = 5;        // 对应"36/18K"档位
                break;
                
            default:
                // 检查是否为Auto模式
                if ("Auto".equalsIgnoreCase(memDepth)) {
                    idx = 0;    // 对应"Auto"档位
                }
                break;
        }
        
        return idx;
    }
}
