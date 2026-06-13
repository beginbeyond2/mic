package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;

/**
 * 36M点存储深度实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 存储深度具体实现</li>
 *   <li>设计模式：继承 + 策略模式</li>
 *   <li>适用产品：MHO系列36M存储深度示波器</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现36M点存储深度的具体计算逻辑</li>
 *   <li>管理5个存储深度档位（Auto/36M/3.6M/360K/36K）</li>
 *   <li>根据时基档位自动计算最优存储深度</li>
 *   <li>支持段采样模式下的存储深度计算</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>为36M存储深度示波器提供专用实现</li>
 *   <li>实现Auto模式下时基与存储深度的智能匹配</li>
 *   <li>支持多通道采样时的存储深度分配</li>
 *   <li>优化不同时基档位下的采样性能</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * IMemDepth (接口)
 *   │
 *   └── MemDepth (抽象类)
 *          │
 *          ├── MemDepth1800M (1800M点存储深度实现)
 *          ├── MemDepth360M (360M点存储深度实现)
 *          └── MemDepth36M (当前类 - 36M点存储深度实现)
 * </pre>
 * 
 * <p><b>存储深度档位说明：</b>
 * <pre>
 * 档位索引 │ 档位名称    │ 2通道深度  │ 4通道深度
 * ─────────┼─────────────┼────────────┼──────────
 *    0     │ Auto        │ 自动计算   │ 自动计算
 *    1     │ 36/18M      │ 36M点      │ 18M点
 *    2     │ 3.6/1.8M    │ 3.6M点     │ 1.8M点
 *    3     │ 360/180K    │ 360K点     │ 180K点
 *    4     │ 36/18K      │ 36K点      │ 18K点
 * </pre>
 * 
 * <p><b>Auto模式计算策略：</b>
 * <ul>
 *   <li>滚屏模式/慢时基模式：固定使用360K点</li>
 *   <li>时基≥500μs/div：使用36M点</li>
 *   <li>时基500μs~100ns/div：根据时基线性递减</li>
 *   <li>时基≤50ns/div：固定使用1800点</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：MemDepth（存储深度抽象基类）</li>
 *   <li>依赖：Scope（获取通道数和运行状态）</li>
 *   <li>依赖：HorizontalAxis（获取时基档位）</li>
 *   <li>依赖：SegmentSample（段采样管理）</li>
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
 * @version 1.0
 * @since 2018-7-19
 * @see IMemDepth 存储深度接口
 * @see MemDepth 存储深度抽象基类
 * @see MemDepth1800M 1800M点存储深度实现
 * @see MemDepth360M 360M点存储深度实现
 * @see HorizontalAxis 水平轴管理
 * @see SegmentSample 段采样管理
 */
public class MemDepth36M extends MemDepth {
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数设置存储深度基准值</li>
     *   <li>初始化5个存储深度档位名称</li>
     * </ul>
     * 
     * <p><b>档位说明：</b>
     * <ul>
     *   <li>Auto：自动模式，根据时基自动选择</li>
     *   <li>36/18M：最大档位，2通道36M/4通道18M</li>
     *   <li>3.6/1.8M：中高档位</li>
     *   <li>360/180K：中低档位</li>
     *   <li>36/18K：最小档位</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>MemDepthFactory根据硬件型号创建存储深度对象时</li>
     * </ul>
     * 
     * @param memDepth 存储深度基准值（36,000,000点）
     */
    public MemDepth36M(int memDepth) {
        super(memDepth);
        addItem("Auto");
        addItem("36/18M");
        addItem("3.6/1.8M");
        addItem("360/180K");
        addItem("36/18K");
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
     * <pre>"Auto,36/18M,3.6/1.8M,360/180K,36/18K"</pre>
     * 
     * @return 档位名称字符串
     */
    @Override
    public String getMemDepthInitName() {
        return "Auto,36/18M,3.6/1.8M,360/180K,36/18K";
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
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据档位索引和通道数计算实际存储深度</li>
     *   <li>支持Auto模式和固定档位模式</li>
     *   <li>支持段采样模式下的特殊计算</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 通道系数 = (通道数 == 4) ? 1 : 2
     * 基准值 = 3600
     * 
     * 档位计算：
     *   档位1: 3600 * 1000 * 10 / 通道系数 = 36M或18M
     *   档位2: 3600 * 1000 / 通道系数 = 3.6M或1.8M
     *   档位3: 3600 * 100 / 通道系数 = 360K或180K
     *   档位4: 3600 * 10 / 通道系数 = 36K或18K
     * </pre>
     * 
     * <p><b>Auto模式逻辑：</b>
     * <ul>
     *   <li>段采样模式：调用getSegment()计算段存储深度</li>
     *   <li>普通模式：调用getAuto()根据时基自动计算</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>FPGA配置采样参数时</li>
     *   <li>计算采样率和时间窗口时</li>
     *   <li>波形数据显示时</li>
     * </ul>
     * 
     * @param chCnt 采样通道数（2或4）
     * @return 实际采样存储深度（单位：点）
     */
    @Override
    public int getSampleMemDepth(int chCnt) {

        SegmentSample segmentSample = SegmentSample.getInstance();
        int cfg = 3600;

        int coef = 1;

        switch(chCnt)
        {
            case 4:
                coef = 1;
                break;
            default:
                coef = 2;
                break;
        }
        switch(getMemDepthItem()){
            case 1: return cfg * 1000 * 10 / coef;
            case 2: return cfg * 1000 / coef;
            case 3: return cfg * 100 / coef;
            case 4: return cfg * 10 / coef;
            default:
            case 0:
            {
                int a = getAuto(cfg,coef);
                if(segmentSample.isSegmentEnable()){
                    int s = getSegment(cfg,coef);
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
     * 计算段采样模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据段数和通道数计算合适的存储深度</li>
     *   <li>确保每段有足够的存储空间</li>
     *   <li>从预设的存储深度数组中选择合适的值</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ul>
     *   <li>获取段数（加1为额外段）</li>
     *   <li>计算所需的总段数（考虑通道系数）</li>
     *   <li>从存储深度数组中选择满足条件的最小值</li>
     * </ul>
     * 
     * <p><b>存储深度数组：</b>
     * <pre>
     * zunArray = [36M, 3.6M, 360K, 3.6K]
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户启用段采样功能时</li>
     *   <li>段数变化时重新计算存储深度</li>
     * </ul>
     * 
     * @param cfg 存储深度基准值（3600）
     * @param coef 通道系数（1或2）
     * @return 段采样模式下的存储深度（单位：点）
     */
    private int getSegment(int cfg,int coef){

        SegmentSample segmentSample = SegmentSample.getInstance();
        int num = segmentSample.getSegmentNums() + 1;
        int [] zunArray = {36*1000*1000,36*100*1000,360*1000,3600};
        int s = 0;
        num *= coef * 2;
        for(int i=0;i<zunArray.length;i++){
            s = segmentSample.getSegmentSample(zunArray[i]);
            if(num <= s){
                s = zunArray[i];
                break;
            }
        }
        return s/coef;
    }

    /**
     * 计算Auto模式下的存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前时基档位自动选择最优存储深度</li>
     *   <li>平衡采样率和时间窗口</li>
     *   <li>优化波形显示效果</li>
     * </ul>
     * 
     * <p><b>计算策略：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────┐
     * │ 模式判断                                                 │
     * ├─────────────────────────────────────────────────────────┤
     * │ 滚屏模式/慢时基模式                                      │
     * │   → 返回 360K/180K 点（固定值）                          │
     * ├─────────────────────────────────────────────────────────┤
     * │ 时基 ≥ 500μs/div                                        │
     * │   → 返回 36M/18M 点（最大存储深度）                      │
     * ├─────────────────────────────────────────────────────────┤
     * │ 时基 500μs ~ 100ns/div（线性递减）                       │
     * │   500μs → 18M/9M                                        │
     * │   200μs → 7.2M/3.6M                                     │
     * │   100μs → 3.6M/1.8M                                     │
     * │   50μs  → 1.8M/900K                                     │
     * │   20μs  → 720K/360K                                     │
     * │   10μs  → 360K/180K                                     │
     * │   5μs   → 180K/90K                                      │
     * │   2μs   → 72K/36K                                       │
     * │   1μs   → 36K/18K                                       │
     * │   500ns → 18K/9K                                        │
     * │   200ns → 7.2K/3.6K                                     │
     * │   100ns → 3.6K/1.8K                                     │
     * ├─────────────────────────────────────────────────────────┤
     * │ 时基 ≤ 50ns/div                                         │
     * │   → 返回 1800 点（最小存储深度，保证高采样率）           │
     * └─────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>设计原理：</b>
     * <ul>
     *   <li>快速时基需要高采样率，使用大存储深度</li>
     *   <li>慢速时基需要长时间窗口，使用小存储深度</li>
     *   <li>滚屏模式固定使用中等存储深度</li>
     * </ul>
     * 
     * @param cfg 存储深度基准值（3600）
     * @param coef 通道系数（1或2）
     * @return Auto模式下的存储深度（单位：点）
     */
    private int getAuto(int cfg,int coef){
        Scope scope = Scope.getInstance();
        HorizontalAxis xAxis = HorizontalAxis.getInstance();
        if(scope.isInScrollMode() || scope.isInSlowScaleMode()){
            return cfg * 100/coef;
        }

        int mainScaleId = xAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);

        if(mainScaleId < HorizontalAxis.TSI_500uS)
            return cfg *1000 * 10/coef;
        switch(mainScaleId)
        {
            case HorizontalAxis.TSI_500uS: return 18000000/coef;
            case HorizontalAxis.TSI_200uS: return 7200000/coef;
            case HorizontalAxis.TSI_100uS: return 3600000/coef;
            case HorizontalAxis.TSI_50uS:  return 1800000/coef;
            case HorizontalAxis.TSI_20uS:  return 720000/coef;
            case HorizontalAxis.TSI_10uS:  return 360000/coef;
            case HorizontalAxis.TSI_5uS:   return 180000/coef;
            case HorizontalAxis.TSI_2uS:   return 72000/coef;
            case HorizontalAxis.TSI_1uS:   return 36000/coef;
            case HorizontalAxis.TSI_500nS: return 18000/coef;
            case HorizontalAxis.TSI_200nS: return 7200/coef;
            case HorizontalAxis.TSI_100nS: return 3600/coef;
            case HorizontalAxis.TSI_50nS:
            case HorizontalAxis.TSI_20nS:
            case HorizontalAxis.TSI_10nS:
            case HorizontalAxis.TSI_5nS:
            case HorizontalAxis.TSI_2nS:
            case HorizontalAxis.TSI_1nS:
            default:        return 1800;
        }
    }

    /**
     * 获取时基档位对应的存储深度比例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回时基档位对应的存储深度调整比例</li>
     *   <li>36M存储深度实现固定返回1.0</li>
     * </ul>
     * 
     * <p><b>设计说明：</b>
     * <ul>
     *   <li>36M存储深度示波器不需要时基比例调整</li>
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
     *   <li>36M存储深度实现固定返回1.0</li>
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
     *   <li>36M存储深度示波器支持特殊时基处理</li>
     *   <li>返回true表示需要进行时基比例调整</li>
     * </ul>
     * 
     * <p><b>特殊时基处理：</b>
     * <ul>
     *   <li>滚屏模式下的时基调整</li>
     *   <li>慢时基模式下的采样优化</li>
     * </ul>
     * 
     * @return true表示支持特殊时基处理
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
     * 存储深度值          │ 菜单索引 │ 档位名称
     * ────────────────────┼──────────┼──────────
     * "36000000"/"18000000" │    1    │ 36/18M
     * "3600000"/"1800000"   │    2    │ 3.6/1.8M
     * "360000"/"180000"     │    3    │ 360/180K
     * "36000"/"18000"       │    4    │ 36/18K
     * "Auto"                │    0    │ Auto
     * 其他                  │   -1    │ 未找到
     * </pre>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>每个档位对应两个存储深度值（2通道/4通道）</li>
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
            case "36000000":
            case "18000000":
                idx = 1;
                break;
            case "3600000":
            case "1800000":
                idx = 2;
                break;
            case "360000":
            case "180000":
                idx = 3;
                break;
            case "36000":
            case "18000":
                idx = 4;
                break;
            default:
                if("Auto".equalsIgnoreCase(memDepth)){
                    idx = 0;
                }
        }
        return idx;
    }
}
