package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;

/**
 * 1800M点存储深度实现类（V2版本）
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 存储深度具体实现</li>
 *   <li>设计模式：继承 + 策略模式</li>
 *   <li>适用产品：MHO系列高端示波器（1800M存储深度，V2硬件版本）</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现1800M点存储深度的具体计算逻辑（V2硬件版本）</li>
 *   <li>管理6个存储深度档位（Auto/1800M/180M/18M/1.8M/180K）</li>
 *   <li>根据时基档位自动计算最优存储深度</li>
 *   <li>支持段采样模式下的存储深度计算</li>
 *   <li>支持多ADC芯片配置的存储深度分配</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>为V2硬件版本的1800M存储深度示波器提供专用实现</li>
 *   <li>实现Auto模式下时基与存储深度的智能匹配</li>
 *   <li>支持多通道采样时的存储深度分配（2/4/8通道）</li>
 *   <li>优化不同时基档位下的采样性能</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * IMemDepth (接口)
 *   │
 *   └── MemDepth (抽象类)
 *          │
 *          ├── MemDepth1800M (V1版本 - 1800M点存储深度实现)
 *          ├── MemDepth1800MV2 (当前类 - V2版本)
 *          ├── MemDepth360M (360M点存储深度实现)
 *          └── MemDepth36M (36M点存储深度实现)
 * </pre>
 * 
 * <p><b>存储深度档位说明：</b>
 * <pre>
 * 档位索引 │ 档位名称         │ 2通道深度  │ 4通道深度  │ 8通道深度
 * ─────────┼──────────────────┼────────────┼────────────┼──────────
 *    0     │ Auto             │ 自动计算   │ 自动计算   │ 自动计算
 *    1     │ 1800/900/450M    │ 1800M点    │ 900M点     │ 450M点
 *    2     │ 180/90/45M       │ 180M点     │ 90M点      │ 45M点
 *    3     │ 18/9/4.5M        │ 18M点      │ 9M点       │ 4.5M点
 *    4     │ 1800/900/450K    │ 1.8M点     │ 900K点     │ 450K点
 *    5     │ 180/90/45K       │ 180K点     │ 90K点      │ 45K点
 * </pre>
 * 
 * <p><b>V1与V2版本差异：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 特性              │ MemDepth1800M (V1)    │ MemDepth1800MV2 (V2) │
 * ├────────────────────┼───────────────────────┼──────────────────────┤
 * │ 基准值            │ 18000                 │ 9000 (2通道时×2)     │
 * │ 通道系数计算      │ chCnt / 2             │ chCnt / ADC数量      │
 * │ 2通道特殊处理     │ 无                    │ 基准值×2，系数=1     │
 * │ 段采样数组        │ [180M,18M,1.8M,180K]  │ [90M,9M,900K,90K]    │
 * │ Auto超限处理      │ 直接返回最大值        │ 循环减半直到满足     │
 * └────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>Auto模式计算策略：</b>
 * <ul>
 *   <li>时基≤50ms/div：使用最大存储深度（18M/通道系数）</li>
 *   <li>时基>50ms/div：根据时基动态计算，保证屏幕显示完整</li>
 *   <li>超限处理：当计算值超过最大值时，循环减半直到满足条件</li>
 *   <li>最小存储深度：屏幕宽度（保证至少一屏数据）</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：MemDepth（存储深度抽象基类）</li>
 *   <li>依赖：Scope（获取通道数和运行状态）</li>
 *   <li>依赖：HorizontalAxis（获取时基档位）</li>
 *   <li>依赖：SegmentSample（段采样管理）</li>
 *   <li>依赖：HwConfig（硬件配置，获取ADC数量和时钟）</li>
 *   <li>依赖：ScopeBase（屏幕宽度等基础配置）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户切换存储深度档位时</li>
 *   <li>时基档位变化时自动调整存储深度</li>
 *   <li>通道数变化时重新计算存储深度</li>
 *   <li>段采样模式下的存储深度计算</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 2.0
 * @since 2018
 * @see IMemDepth 存储深度接口
 * @see MemDepth 存储深度抽象基类
 * @see MemDepth1800M V1版本1800M点存储深度实现
 * @see MemDepth360M 360M点存储深度实现
 * @see MemDepth36M 36M点存储深度实现
 * @see HorizontalAxis 水平轴管理
 * @see SegmentSample 段采样管理
 * @see HwConfig 硬件配置
 */
public class MemDepth1800MV2 extends MemDepth {
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数设置存储深度基准值</li>
     *   <li>初始化6个存储深度档位名称</li>
     * </ul>
     * 
     * <p><b>档位说明：</b>
     * <ul>
     *   <li>Auto：自动模式，根据时基自动选择</li>
     *   <li>1800/900/450M：最大档位，支持2/4/8通道</li>
     *   <li>180/90/45M：高档位</li>
     *   <li>18/9/4.5M：中档位</li>
     *   <li>1800/900/450K：低档位</li>
     *   <li>180/90/45K：最小档位</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>MemDepthFactory根据硬件型号创建存储深度对象时</li>
     * </ul>
     * 
     * @param memDepth 存储深度基准值（1,800,000,000点）
     */
    public MemDepth1800MV2(int memDepth) {
        super(memDepth);
        addItem("Auto");
        addItem("1800/900/450M");
        addItem("180/90/45M");
        addItem("18/9/4.5M");
        addItem("1800/900/450K");
        addItem("180/90/45K");
    }

    /**
     * 获取初始化时的存储深度档位名称字符串
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回所有档位名称的逗号分隔字符串</li>
     *   <li>用于配置保存和恢复</li>
     * </ul>
     * 
     * <p><b>返回格式：</b>
     * <pre>"Auto,1800/900/450M,180/90/45M,18/9/4.5M,1800/900/450K,180/90/45K"</pre>
     * 
     * @return 档位名称字符串
     */
    @Override
    public String getMemDepthInitName() {
        return "Auto,1800/900/450M,180/90/45M,18/9/4.5M,1800/900/450K,180/90/45K";
    }

    /**
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据档位索引和通道数计算实际存储深度</li>
     *   <li>支持Auto模式和固定档位模式</li>
     *   <li>支持段采样模式下的特殊计算</li>
     *   <li>支持多ADC芯片配置</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 基准值 = 9000（2通道时×2）
     * 通道系数 = 通道数 / ADC数量
     * 
     * 2通道特殊处理：
     *   基准值 *= 2
     *   通道系数 = 1
     * 
     * 档位计算：
     *   档位1: 基准值 * 1000 * 100 / 通道系数 = 1800M/900M/450M
     *   档位2: 基准值 * 1000 * 10 / 通道系数 = 180M/90M/45M
     *   档位3: 基准值 * 1000 / 通道系数 = 18M/9M/4.5M
     *   档位4: 基准值 * 100 / 通道系数 = 1.8M/900K/450K
     *   档位5: 基准值 * 10 / 通道系数 = 180K/90K/45K
     * </pre>
     * 
     * <p><b>V2版本特性：</b>
     * <ul>
     *   <li>通道系数基于ADC数量计算，而非固定除以2</li>
     *   <li>2通道模式有特殊处理逻辑</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>FPGA配置采样参数时</li>
     *   <li>计算采样率和时间窗口时</li>
     *   <li>波形数据显示时</li>
     * </ul>
     * 
     * @param chCnt 采样通道数（2/4/8）
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth(int chCnt) {

        SegmentSample segmentSample = SegmentSample.getInstance();
        HwConfig hwConfig = HwConfig.getInstance();
        int cfg = 9000 ;
        int coef = chCnt / hwConfig.getAdcNums();
        if(chCnt == 2){
            cfg *= 2;
            coef = 1;
        }
        switch(getMemDepthItem()){
            case 1: return cfg * 1000 * 100 / coef;
            case 2: return cfg * 1000 * 10 / coef;
            case 3: return cfg * 1000 / coef;
            case 4: return cfg * 100 / coef;
            case 5: return cfg * 10 / coef;
            default:
            case 0:
            {
                int a = getAuto(cfg,coef,chCnt);
                if(segmentSample.isSegmentEnable()){
                    int s = getSegment(a,coef,chCnt);
                    if(s > a){
                        s = a;
                    }
                    return s;
                }
                return a;
            }
        }
    }
    
    /**
     * 获取实际采样存储深度（自动获取通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从Scope获取当前采样通道数</li>
     *   <li>调用重载方法计算实际存储深度</li>
     * </ul>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * getSampleMemDepth() → Scope.getChannelSampOnCnt() → getSampleMemDepth(chCnt)
     * </pre>
     * 
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth() {
        Scope scope = Scope.getInstance();
        return getSampleMemDepth(scope.getChannelSampOnCnt());
    }


    /**
     * 计算段采样模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据段数和通道数计算合适的存储深度</li>
     *   <li>确保每段有足够的存储空间</li>
     *   <li>从预设的存储深度数组中选择满足条件的最小值</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ul>
     *   <li>获取段数（加1为额外段）</li>
     *   <li>遍历存储深度数组，找到第一个满足条件的值</li>
     *   <li>考虑通道系数进行分配</li>
     *   <li>2通道模式有特殊处理（存储深度×2）</li>
     * </ul>
     * 
     * <p><b>存储深度数组：</b>
     * <pre>
     * zunArray = [90M, 9M, 900K, 90K]
     * 注：2通道时每个值×2
     * </pre>
     * 
     * <p><b>V2版本特性：</b>
     * <ul>
     *   <li>存储深度数组为V1版本的一半</li>
     *   <li>2通道模式需要特殊处理存储深度值</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户启用段采样功能时</li>
     *   <li>段数变化时重新计算存储深度</li>
     * </ul>
     * 
     * @param a Auto模式计算的存储深度（用于上限限制）
     * @param coef 通道系数
     * @param chCnt 采样通道数
     * @return 段采样模式下的存储深度（单位：点）
     */
    private int getSegment(int a,int coef,int chCnt){

        SegmentSample segmentSample = SegmentSample.getInstance();
        int num = segmentSample.getSegmentNums() + 1;
        int [] zunArray = {90_000_000,9_000_000,900_000,90_000};
        for (int j : zunArray) {
            if(chCnt == 2){
                coef = 1;
                j *= 2;
            }
            int cnt = segmentSample.getSegmentSample(j/coef);
            if (num <= cnt/coef) {
                return j / coef;
            }
        }
        return zunArray[zunArray.length - 1]/coef;
    }

    /**
     * 计算Auto模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前时基档位自动选择最优存储深度</li>
     *   <li>平衡采样率和时间窗口</li>
     *   <li>保证屏幕显示完整波形</li>
     * </ul>
     * 
     * <p><b>计算策略：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────┐
     * │ 模式判断                                                 │
     * ├─────────────────────────────────────────────────────────┤
     * │ 时基 ≤ 50ms/div                                         │
     * │   → 返回 基准值/通道系数（最大存储深度）                 │
     * ├─────────────────────────────────────────────────────────┤
     * │ 时基 > 50ms/div（动态计算）                              │
     * │   计算公式：                                             │
     * │   l = 时基值 × 屏幕格数 × ADC时钟 × 1e6                 │
     * │   如果 l ≤ 基准值：                                      │
     * │     返回 l/通道系数（保证至少屏幕宽度）                  │
     * │   否则：                                                 │
     * │     如果 l 是基准值的整数倍：                            │
     * │       返回 基准值/通道系数                               │
     * │     否则：                                               │
     * │       循环减半直到 l ≤ 基准值                            │
     * │       返回 l/通道系数                                    │
     * └─────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>V2版本特性：</b>
     * <ul>
     *   <li>基准值为9M（2通道时18M）</li>
     *   <li>超限时有循环减半处理逻辑</li>
     *   <li>2通道模式有特殊处理</li>
     * </ul>
     * 
     * <p><b>设计原理：</b>
     * <ul>
     *   <li>快速时基需要高采样率，使用大存储深度</li>
     *   <li>慢速时基需要长时间窗口，动态计算存储深度</li>
     *   <li>最小存储深度为屏幕宽度，保证至少一屏数据</li>
     *   <li>超限时循环减半，避免存储深度不足</li>
     * </ul>
     * 
     * <p><b>计算参数说明：</b>
     * <ul>
     *   <li>屏幕格数：ScopeBase.getHorizonGridCnt()</li>
     *   <li>ADC时钟：HwConfig.getMaxAdInClk()</li>
     *   <li>屏幕宽度：ScopeBase.getWidth()</li>
     * </ul>
     * 
     * @param cfg 存储深度基准值（9000，此参数在方法内被覆盖为9M）
     * @param coef 通道系数
     * @param chCnt 采样通道数
     * @return Auto模式下的存储深度（单位：点）
     */
    private int getAuto(int cfg,int coef,int chCnt){
        cfg = 9_000_000;
        if(chCnt == 2){
            cfg *= 2;
            coef = 1;
        }
        HorizontalAxis xAxis = HorizontalAxis.getInstance();

        int mainScaleId = xAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);
        if(mainScaleId <= HorizontalAxis.TSI_50mS ){
            return cfg / coef;
        }
        long l = (long)(HorizontalAxis.stdTimeScaleIdVal(mainScaleId)
                * ScopeBase.getHorizonGridCnt()
                * HwConfig.getMaxAdInClk()
                * 1e6 + 0.01);

        if(l <= cfg){
            long n = l/coef;
            if(n < ScopeBase.getWidth()){
                return ScopeBase.getWidth();
            }else{
                return (int)n;
            }
        }else{
            if((l % cfg) != 0){
                while (l > cfg){
                    l/=2;
                }
                return (int)(l / coef);
            }else {
                return cfg / coef;
            }
        }
    }

    /**
     * 获取时基档位对应的存储深度比例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回时基档位对应的存储深度调整比例</li>
     *   <li>1800MV2存储深度实现固定返回1.0</li>
     * </ul>
     * 
     * <p><b>设计说明：</b>
     * <ul>
     *   <li>1800MV2存储深度示波器不需要时基比例调整</li>
     *   <li>所有时基档位使用标准存储深度计算</li>
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
     *   <li>返回指定时基档位和存储深度档位下的比例</li>
     *   <li>1800MV2存储深度实现固定返回1.0</li>
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
     *   <li>1800MV2存储深度示波器不支持特殊时基处理</li>
     *   <li>返回false表示不需要进行时基比例调整</li>
     * </ul>
     * 
     * <p><b>设计说明：</b>
     * <ul>
     *   <li>与MemDepth36M不同，1800MV2版本使用标准时基计算</li>
     *   <li>不需要滚屏模式或慢时基模式的特殊处理</li>
     * </ul>
     * 
     * @return false表示不支持特殊时基处理
     */
    @Override
    public boolean isSpecialTimeScale() {
        return false;
    }

    /**
     * 获取数学运算通道耦合数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回数学运算通道可用的耦合通道配置</li>
     *   <li>用于数学运算功能（加减乘除FFT等）</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>返回{360000}表示数学运算使用360K点存储深度</li>
     *   <li>该值与存储深度档位相关</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>配置数学运算通道时</li>
     *   <li>FFT运算时确定数据长度</li>
     * </ul>
     * 
     * @return 数学运算耦合通道数组
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
     *   <li>用于配置恢复或SCPI命令解析</li>
     * </ul>
     * 
     * <p><b>映射规则：</b>
     * <pre>
     * 存储深度值                      │ 菜单索引 │ 档位名称
     * ────────────────────────────────┼──────────┼────────────────
     * "1800000000"/"900000000"/"450000000" │    1    │ 1800/900/450M
     * "180000000"/"90000000"/"45000000"    │    2    │ 180/90/45M
     * "18000000"/"9000000"/"4500000"       │    3    │ 18/9/4.5M
     * "1800000"/"900000"/"450000"          │    4    │ 1800/900/450K
     * "180000"/"90000"/"45000"             │    5    │ 180/90/45K
     * "Auto"                               │    0    │ Auto
     * 其他                                 │   -1    │ 未找到
     * </pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>每个档位对应三个存储深度值（2/4/8通道）</li>
     *   <li>不区分大小写匹配"Auto"</li>
     * </ul>
     * 
     * @param memDepth 存储深度字符串
     * @return 菜单索引，未找到返回-1
     */
    @Override
    public int memDepth2menuIdx(String memDepth) {
        int idx = -1;

        switch (memDepth){
            case "1800000000":
            case "900000000":
            case "450000000":
                idx = 1;
                break;
            case "180000000":
            case "90000000":
            case "45000000":
                idx = 2;
                break;
            case "18000000":
            case "9000000":
            case "4500000":
                idx = 3;
                break;
            case "1800000":
            case "900000":
            case "450000":
                idx = 4;
                break;
            case "180000":
            case "90000":
            case "45000":
                idx = 5;
                break;
            default:
                if("Auto".equalsIgnoreCase(memDepth)){
                    idx = 0;
                }
                break;
        }
        return idx;
    }
}
