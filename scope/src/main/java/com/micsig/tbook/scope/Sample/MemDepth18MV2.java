package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;


/**
 * 18M点存储深度实现类（V2版本）
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
 *   <li>实现18M点存储深度的具体计算逻辑（V2优化版本）</li>
 *   <li>提供4个存储深度档位（Auto/18M/1.8M/180K）</li>
 *   <li>支持自动模式和段采样模式的存储深度计算</li>
 *   <li>实现时基与存储深度的动态关联计算</li>
 * </ul>
 * 
 * <p><b>存储深度档位：</b>
 * <pre>
 * 索引  名称              双通道      四通道(2ADC)   四通道(4ADC)
 * ──────────────────────────────────────────────────────────────
 * 0     Auto             自动计算    自动计算       自动计算
 * 1     18/9/4.5M        18M点       9M点           4.5M点
 * 2     1800/900/450K    1.8M点      900K点         450K点
 * 3     180/90/45K       180K点      90K点          45K点
 * </pre>
 * 
 * <p><b>V2版本特点（与MemDepth18M的区别）：</b>
 * <ul>
 *   <li><b>动态ADC适配：</b>通道系数根据实际ADC数量动态计算，而非固定除以2</li>
 *   <li><b>双通道优化：</b>双通道模式下基准值翻倍，系数固定为1，提高存储深度利用率</li>
 *   <li><b>智能Auto计算：</b>当计算值超过最大深度时，自动降采样而非直接截断</li>
 *   <li><b>段采样优化：</b>段采样模式下针对双通道有特殊处理逻辑</li>
 * </ul>
 * 
 * <p><b>通道系数计算规则：</b>
 * <pre>
 * V2版本通道系数 = 通道数 ÷ ADC数量
 * 
 * 特殊情况：
 *   双通道模式: 系数固定为1，基准值翻倍
 *   四通道(2ADC): 系数 = 4 ÷ 2 = 2
 *   四通道(4ADC): 系数 = 4 ÷ 4 = 1
 * </pre>
 * 
 * <p><b>适用产品：</b>
 * <ul>
 *   <li>MHO68 V2（新一代中端示波器）</li>
 *   <li>支持多ADC配置的产品</li>
 *   <li>需要更灵活存储深度分配的场景</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：MemDepth（存储深度抽象基类）</li>
 *   <li>依赖：Scope（获取通道数）</li>
 *   <li>依赖：HorizontalAxis（获取时基档位）</li>
 *   <li>依赖：SegmentSample（段采样管理）</li>
 *   <li>依赖：HwConfig（硬件配置，获取ADC数量和最大采样率）</li>
 *   <li>依赖：ScopeBase（获取屏幕宽度和水平格数）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户切换存储深度档位时计算实际存储深度</li>
 *   <li>时基档位变化时自动计算存储深度（Auto模式）</li>
 *   <li>段采样模式下计算段存储深度</li>
 *   <li>配置恢复时根据存储深度字符串查找档位索引</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 2.0
 * @since 2019
 * @see MemDepth 存储深度抽象基类
 * @see IMemDepth 存储深度接口
 * @see MemDepth18M 18M点存储深度实现（V1版本）
 * @see MemDepth1800MV2 1800M点存储深度实现（V2版本）
 * @see HwConfig 硬件配置管理
 * @see SegmentSample 段采样管理
 */
public class MemDepth18MV2 extends MemDepth {
    
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
     *   <li>索引1: "18/9/4.5M" - 18M/9M/4.5M点</li>
     *   <li>索引2: "1800/900/450K" - 1.8M/900K/450K点</li>
     *   <li>索引3: "180/90/45K" - 180K/90K/45K点</li>
     * </ul>
     * 
     * @param memDepth 存储深度基准值（通常为18,000,000）
     */
    public MemDepth18MV2(int memDepth) {
        // 调用父类构造函数
        super(memDepth);
        
        // 初始化存储深度档位名称列表
        addItem("Auto");                    // 索引0：自动模式
        addItem("18/9/4.5M");               // 索引1：18M/9M/4.5M点
        addItem("1800/900/450K");           // 索引2：1.8M/900K/450K点
        addItem("180/90/45K");              // 索引3：180K/90K/45K点
//        addItem("9/4.5K");                 // 索引4：已注释，暂不启用
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
     * @return 档位名称字符串，格式："Auto,18/9/4.5M,1800/900/450K,180/90/45K"
     */
    @Override
    public String getMemDepthInitName() {
        return "Auto,18/9/4.5M,1800/900/450K,180/90/45K";
    }

    /**
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据档位索引和通道数计算实际存储深度</li>
     *   <li>支持固定档位和Auto自动模式</li>
     *   <li>支持段采样模式</li>
     *   <li>V2版本：动态根据ADC数量计算通道系数</li>
     * </ul>
     * 
     * <p><b>V2版本计算逻辑：</b>
     * <pre>
     * 1. 获取基准值和ADC数量
     * 2. 计算通道系数 = 通道数 ÷ ADC数量
     * 3. 双通道特殊处理：
     *    - 基准值翻倍（900 → 1800）
     *    - 系数固定为1
     * 4. 根据档位计算最终存储深度
     * </pre>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 实际存储深度 = 基准值 × 倍率 ÷ 通道系数
     * 
     * 其中：
     *   基准值 = 900（双通道时×2 = 1800）
     *   通道系数 = 通道数 ÷ ADC数量（双通道时固定为1）
     * 
     * 各档位倍率：
     *   档位1: ×10000 (18M点)
     *   档位2: ×1000  (1.8M点)
     *   档位3: ×100   (180K点)
     * </pre>
     * 
     * <p><b>Auto模式逻辑：</b>
     * <ul>
     *   <li>快速时基（<50ms/div）：使用最大存储深度</li>
     *   <li>慢速时基（≥50ms/div）：根据时基自动计算</li>
     *   <li>段采样模式：取Auto计算值和段计算值的较小值</li>
     * </ul>
     * 
     * @param chCnt 采样通道数（2/4）
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth(int chCnt) {
        // 获取段采样实例
        SegmentSample segmentSample = SegmentSample.getInstance();
        
        // 获取硬件配置实例
        HwConfig hwConfig = HwConfig.getInstance();
        
        // 基准值：900（V2版本基础值）
        int cfg = 900;
        
        // V2版本：通道系数根据ADC数量动态计算
        // 系数 = 通道数 ÷ ADC数量
        int coef = chCnt / hwConfig.getAdcNums();
        
        // 双通道特殊处理
        // V2版本优化：双通道时基准值翻倍，系数固定为1
        // 这样可以充分利用存储资源，提高双通道存储深度
        if (chCnt == 2) {
            cfg *= 2;       // 基准值翻倍：900 → 1800
            coef = 1;       // 系数固定为1
        }
        
        // 根据档位索引计算存储深度
        switch (getMemDepthItem()) {
            case 1:
                // 档位1: 18M/9M/4.5M点
                // 计算: 基准值 × 10000 ÷ 系数
                return cfg * 1000 * 10 / coef;
                
            case 2:
                // 档位2: 1.8M/900K/450K点
                // 计算: 基准值 × 1000 ÷ 系数
                return cfg * 1000 / coef;
                
            case 3:
                // 档位3: 180K/90K/45K点
                // 计算: 基准值 × 100 ÷ 系数
                return cfg * 100 / coef;
                
//            case 4:
//                // 档位4: 18K/9K/4.5K点（已注释，暂不启用）
//                return cfg * 10 / coef;
                
            default:
            case 0:
                // Auto模式：自动计算存储深度
                int a = getAuto(cfg, coef, chCnt);
                
                // 如果启用了段采样模式
                if (segmentSample.isSegmentEnable()) {
                    // 计算段采样存储深度
                    int s = getSegment(cfg, coef, chCnt);
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
     *   <li>V2版本：针对双通道有特殊优化处理</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ol>
     *   <li>获取目标段数</li>
     *   <li>遍历存储深度数组[9M, 900K, 90K]</li>
     *   <li>双通道特殊处理：存储深度翻倍，系数固定为1</li>
     *   <li>计算每个深度能支持的段数</li>
     *   <li>返回满足段数要求的最小深度</li>
     * </ol>
     * 
     * <p><b>双通道优化说明：</b>
     * <pre>
     * V2版本针对双通道的特殊处理：
     *   - 存储深度翻倍（充分利用双通道资源）
     *   - 系数固定为1（简化计算）
     *   - 段数计算时考虑通道因素
     * </pre>
     * 
     * @param cfg 基准值
     * @param coef 通道系数
     * @param chCnt 通道数
     * @return 段采样存储深度
     */
    private int getSegment(int cfg, int coef, int chCnt) {
        // 获取段采样实例
        SegmentSample segmentSample = SegmentSample.getInstance();
        
        // 获取目标段数（+1是因为段数从0开始计数）
        int num = segmentSample.getSegmentNums() + 1;
        
        // 存储深度数组：从大到小排列
        // 9M → 900K → 90K
        // 注意：V2版本最大存储深度为9M（单ADC配置）
        int[] zunArray = {9_000_000, 900_000, 90_000};
        
        // 遍历存储深度数组，找到满足段数要求的最小深度
        for (int j : zunArray) {
            // 双通道特殊处理
            // V2版本优化：双通道时存储深度翻倍，系数固定为1
            if (chCnt == 2) {
                coef = 1;
                j *= 2;      // 存储深度翻倍
            }
            
            // 计算当前深度能支持的段数
            int cnt = segmentSample.getSegmentSample(j / coef);
            
            // 如果支持的段数大于等于目标段数，返回当前深度
            if (num <= cnt / coef) {
                return j / coef;
            }
        }
        
        // 如果所有深度都不满足，返回最小深度
        return zunArray[zunArray.length - 1] / coef;
    }

    /**
     * 计算Auto模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据时基档位自动计算合适的存储深度</li>
     *   <li>快速时基使用最大深度，慢速时基按需计算</li>
     *   <li>V2版本：当计算值超过最大深度时，智能降采样</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ol>
     *   <li>设置最大存储深度为9M点</li>
     *   <li>双通道特殊处理：最大深度翻倍为18M</li>
     *   <li>获取当前时基档位ID</li>
     *   <li>如果时基<50ms/div，返回最大存储深度</li>
     *   <li>否则计算需要的样本点数：时基 × 水平格数 × 最大采样率</li>
     *   <li>如果计算值超过最大深度，循环除以2直到不超过</li>
     *   <li>确保至少一屏的样本点数</li>
     * </ol>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 需要样本点 = 时基值 × 水平格数 × 最大ADC时钟 × 1e6
     * 
     * 其中：
     *   时基值: 秒为单位
     *   水平格数: 通常为14
     *   最大ADC时钟: MHz为单位
     * </pre>
     * 
     * <p><b>V2版本智能降采样：</b>
     * <pre>
     * 当计算值超过最大存储深度时：
     *   V1版本: 直接返回最大深度
     *   V2版本: 循环除以2，找到合适的降采样深度
     * 
     * 示例：
     *   计算值 = 50M，最大深度 = 18M
     *   第一次: 50M ÷ 2 = 25M（仍超过）
     *   第二次: 25M ÷ 2 = 12.5M（不超过）
     *   返回: 12.5M
     * </pre>
     * 
     * @param cfg 基准值
     * @param coef 通道系数
     * @param chCnt 通道数
     * @return Auto模式存储深度
     */
    private int getAuto(int cfg, int coef, int chCnt) {
        // 设置最大存储深度为9M点（V2版本基础值）
        cfg = 9_000_000;
        
        // 双通道特殊处理：最大深度翻倍为18M
        if (chCnt == 2) {
            cfg *= 2;       // 9M → 18M
            coef = 1;       // 系数固定为1
        }
        
        // 获取水平轴实例
        HorizontalAxis xAxis = HorizontalAxis.getInstance();

        // 获取当前主时基档位ID
        int mainScaleId = xAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);
        
        // 快速时基档位（<50ms/div）：使用最大存储深度
        // TSI_50mS = 13，小于13表示时基更快
        if (mainScaleId < HorizontalAxis.TSI_50mS) {
            return cfg / coef;
        }
        
        // 慢速时基档位：根据时基计算需要的存储深度
        // 计算公式：时基值 × 水平格数 × 最大ADC时钟
        long l = (long) (HorizontalAxis.stdTimeScaleIdVal(mainScaleId)
                * ScopeBase.getHorizonGridCnt()      // 水平格数（如14）
                * HwConfig.getMaxAdInClk()           // 最大ADC时钟（MHz）
                * 1e6 + 0.01);                       // 转换为Hz
        
        // 判断是否超过最大存储深度
        if (l <= cfg) {
            // 未超过最大深度，按计算值分配
            long n = l / coef;
            // 确保至少一屏的样本点数
            if (n < ScopeBase.getWidth()) {
                return ScopeBase.getWidth();
            } else {
                return (int) n;
            }
        } else {
            // V2版本智能降采样：当计算值超过最大深度时
            // 检查是否能整除最大深度
            if ((l % cfg) != 0) {
                // 不能整除，循环除以2直到不超过最大深度
                // 这样可以获得更合适的降采样深度
                while (l > cfg) {
                    l /= 2;
                }
                return (int) (l / coef);
            } else {
                // 能整除，直接返回最大深度
                return cfg / coef;
            }
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
     *   <li>表示18M V2存储深度实现支持特殊时基处理</li>
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
     * "18000000"           1       18/9/4.5M (双通道18M)
     * "9000000"            1       18/9/4.5M (四通道2ADC: 9M)
     * "4500000"            1       18/9/4.5M (四通道4ADC: 4.5M)
     * "1800000"            2       1800/900/450K (双通道1.8M)
     * "900000"             2       1800/900/450K (四通道2ADC: 900K)
     * "450000"             2       1800/900/450K (四通道4ADC: 450K)
     * "180000"             3       180/90/45K (双通道180K)
     * "90000"              3       180/90/45K (四通道2ADC: 90K)
     * "45000"              3       180/90/45K (四通道4ADC: 45K)
     * </pre>
     * 
     * @param memDepth 存储深度数值字符串
     * @return 菜单索引，未找到返回-1
     */
    @Override
    public int memDepth2menuIdx(String memDepth) {
        int idx = -1;
        
        switch (memDepth) {
            case "18000000":    // 双通道18M点
            case "9000000":     // 四通道(2ADC) 9M点
            case "4500000":     // 四通道(4ADC) 4.5M点
                idx = 1;        // 对应"18/9/4.5M"档位
                break;
                
            case "1800000":     // 双通道1.8M点
            case "900000":      // 四通道(2ADC) 900K点
            case "450000":      // 四通道(4ADC) 450K点
                idx = 2;        // 对应"1800/900/450K"档位
                break;
                
            case "180000":      // 双通道180K点
            case "90000":       // 四通道(2ADC) 90K点
            case "45000":       // 四通道(4ADC) 45K点
                idx = 3;        // 对应"180/90/45K"档位
                break;
                
            default:
                // 检查是否为Auto模式
                if ("Auto".equalsIgnoreCase(memDepth)) {
                    idx = 0;    // 对应"Auto"档位
                }
        }
        
        return idx;
    }
}
