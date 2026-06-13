package com.micsig.tbook.scope.vertical;  // 定义包名：示波器垂直轴管理模块

import android.util.Log;  // 导入Log类：Android日志工具

import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入HwConfig类：硬件配置管理
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.probe.BaseProbe;  // 导入BaseProbe类：探头基类

/**
 * 垂直轴管理类 - 模拟通道垂直档位和探头管理
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.vertical（示波器垂直轴管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 垂直轴管理</li>
 *   <li>设计模式：常量类 + 状态管理</li>
 *   <li>职责类型：定义垂直档位常量、管理档位状态、探头类型管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义模拟通道垂直档位常量（250μV ~ 10V）</li>
 *   <li>定义探头类型常量（电压/电流/功率/dB等）</li>
 *   <li>管理档位ID、微调系数、探头参数</li>
 *   <li>提供档位值与ID的双向转换</li>
 * </ul>
 * 
 * <p><b>垂直档位架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   VerticalAxis - 模拟通道垂直轴管理                                       │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   垂直档位表（DANG_*）                                            │   │
 * │   │                                                                   │   │
 * │   │   档位ID    档位值      说明                                       │   │
 * │   │   ─────────────────────────────────────                          │   │
 * │   │   DANG_250uV  250μV    最小档位                                   │   │
 * │   │   DANG_500uV  500μV                                              │   │
 * │   │   DANG_1mV    1mV                                                 │   │
 * │   │   DANG_2mV    2mV                                                 │   │
 * │   │   DANG_5mV    5mV                                                 │   │
 * │   │   DANG_10mV   10mV                                                │   │
 * │   │   DANG_20mV   20mV                                                │   │
 * │   │   DANG_50mV   50mV                                                │   │
 * │   │   DANG_100mV  100mV                                               │   │
 * │   │   DANG_200mV  200mV                                               │   │
 * │   │   DANG_500mV  500mV                                               │   │
 * │   │   DANG_1V     1V                                                  │   │
 * │   │   DANG_2V     2V                                                  │   │
 * │   │   DANG_5V     5V                                                  │   │
 * │   │   DANG_10V    10V       最大档位                                   │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   探头类型（PROBE_TYPE_*）                                        │   │
 * │   │                                                                   │   │
 * │   │   VOL = 0    电压探头（V）                                        │   │
 * │   │   CUR = 1    电流探头（A）                                        │   │
 * │   │   VA = 2     电压电流积（VA）                                     │   │
 * │   │   AV = 3     电流电压积（AV）                                     │   │
 * │   │   W = 4      功率探头（W）                                        │   │
 * │   │   DB = 5     分贝探头（dB）                                       │   │
 * │   │   ...        其他自定义类型                                       │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   档位计算公式                                                    │   │
 * │   │                                                                   │   │
 * │   │   实际档位值 = 档位值 * 微调系数 * 探头倍数                        │   │
 * │   │                                                                   │   │
 * │   │   例如：                                                          │   │
 * │   │   档位ID = DANG_1V (1V/div)                                       │   │
 * │   │   微调系数 = 1.5                                                  │   │
 * │   │   探头倍数 = 10X                                                  │   │
 * │   │   实际档位值 = 1V * 1.5 * 10 = 15V/div                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   VerticalAxis（模拟通道垂直轴）
 *       │
 *       ├── VerticalAxisMathDual（数学双波形垂直轴）
 *       ├── VerticalAxisMathFft（数学FFT垂直轴）
 *       └── VerticalAxisRef（参考通道垂直轴）
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>Channel类管理模拟通道的垂直档位</li>
 *   <li>调整垂直档位时调用setScaleId()方法</li>
 *   <li>设置探头时调用setProbe()方法</li>
 *   <li>获取实际档位值时调用getScaleVal()方法</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：HwConfig（硬件配置，获取最小档位限制）</li>
 *   <li>依赖：BaseProbe（探头基类，自动探头管理）</li>
 *   <li>被依赖：Channel（模拟通道）</li>
 *   <li>被继承：VerticalAxisMathDual、VerticalAxisMathFft、VerticalAxisRef</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/15
 * @see Channel 模拟通道
 * @see BaseProbe 探头基类
 * @see VerticalAxisMathDual 数学双波形垂直轴
 * @see VerticalAxisMathFft 数学FFT垂直轴
 */
public class VerticalAxis {
    
    /** 档位常量：无效档位 */
    public static final int DANG_NONE = -1;  // 无效档位标识
    
    /** 档位常量：最小档位索引 */
    public static final int DANG_MIN = 0;  // 最小档位索引
    
    /** 档位常量：250μV/div - 最小档位 */
    public static final int DANG_250uV = DANG_MIN;  // 250μV档位（索引0）
    
    /** 档位常量：500μV/div */
    public static final int DANG_500uV = DANG_250uV + 1;  // 500μV档位（索引1）
    
    /** 档位常量：1mV/div */
    public static final int DANG_1mV   = DANG_500uV + 1;  // 1mV档位（索引2）
    
    /** 档位常量：2mV/div */
    public static final int DANG_2mV   = DANG_1mV + 1;  // 2mV档位（索引3）
    
    /** 档位常量：5mV/div */
    public static final int DANG_5mV   = DANG_2mV + 1;  // 5mV档位（索引4）
    
    /** 档位常量：10mV/div */
    public static final int DANG_10mV  = DANG_5mV + 1;  // 10mV档位（索引5）
    
    /** 档位常量：20mV/div */
    public static final int DANG_20mV  = DANG_10mV + 1;  // 20mV档位（索引6）
    
    /** 档位常量：50mV/div */
    public static final int DANG_50mV  = DANG_20mV + 1;  // 50mV档位（索引7）
    
    /** 档位常量：100mV/div */
    public static final int DANG_100mV = DANG_50mV + 1;  // 100mV档位（索引8）
    
    /** 档位常量：200mV/div */
    public static final int DANG_200mV = DANG_100mV + 1;  // 200mV档位（索引9）
    
    /** 档位常量：500mV/div */
    public static final int DANG_500mV = DANG_200mV + 1;  // 500mV档位（索引10）
    
    /** 档位常量：1V/div */
    public static final int DANG_1V    = DANG_500mV + 1;  // 1V档位（索引11）
    
    /** 档位常量：2V/div */
    public static final int DANG_2V    = DANG_1V + 1;  // 2V档位（索引12）
    
    /** 档位常量：5V/div */
    public static final int DANG_5V    = DANG_2V + 1;  // 5V档位（索引13）
    
    /** 档位常量：10V/div - 最大档位 */
    public static final int DANG_10V   = DANG_5V + 1;  // 10V档位（索引14）
    
    /** 档位常量：最大档位索引 */
    public static final int DANG_MAX = DANG_10V;  // 最大档位索引
    
    /** 档位常量：档位总数 */
    public static final int DANG_CNT = DANG_MAX - DANG_MIN + 1;  // 档位总数（15个）

    /** 探头类型常量：电压探头（V） */
    public static final int PROBE_TYPE_VOL = 0;  // 电压探头
    
    /** 探头类型常量：电流探头（A） */
    public static final int PROBE_TYPE_CUR = 1;  // 电流探头
    
    /** 探头类型常量：电压电流积（VA） */
    public static final int PROBE_TYPE_VA = 2;  // VA探头
    
    /** 探头类型常量：电流电压积（AV） */
    public static final int PROBE_TYPE_AV = 3;  // AV探头
    
    /** 探头类型常量：功率探头（W） */
    public static final int PROBE_TYPE_W = 4;  // 功率探头
    
    /** 探头类型常量：分贝探头（dB） */
    public static final int PROBE_TYPE_DB = 5;  // dB探头
    
    /** 探头类型常量：未知类型 */
    public static final int PROBE_TYPE_WENHAO = 6;  // 未知类型
    
    /** 探头类型常量：电压比（V/V） */
    public static final int PROBE_TYPE_VV= 7;  // V/V探头
    
    /** 探头类型常量：电流比（A/A） */
    public static final int PROBE_TYPE_AA= 8;  // A/A探头
    
    /** 探头类型常量：电压转电压（V→V） */
    public static final int PROBE_TYPE_VTOV = 9;  // V→V探头
    
    /** 探头类型常量：电流转电流（A→A） */
    public static final int PROBE_TYPE_ATOA = 10;  // A→A探头
    
    /** 探头类型常量：空类型 */
    public static final int PROBE_TYPE_EMPTY = 11;  // 空类型
    
    /** 探头类型常量：自定义类型 */
    public static final int PROBE_TYPE_CUSTOM = 12;  // 自定义类型
    
    /** 探头类型常量：最小类型索引 */
    public static final int PROBE_TYPE_MIN = PROBE_TYPE_VOL;  // 最小探头类型
    
    /** 探头类型常量：最大类型索引 */
    public static final int PROBE_TYPE_MAX = PROBE_TYPE_CUSTOM;  // 最大探头类型
    
    /** 探头类型常量：类型总数 */
    public static final int PROBE_TYPE_CNT = PROBE_TYPE_MAX-PROBE_TYPE_MIN+1;  // 探头类型总数
    
    /** 最小档位索引（可动态调整） */
    private static int minGear = DANG_MIN;  // 最小档位索引
    
    /** 最大档位索引（可动态调整） */
    private static int maxGear = DANG_MAX;  // 最大档位索引
    
    /**
     * 检查档位ID是否有效（静态方法）
     * 
     * <p>检查给定的档位ID是否在有效范围内（DANG_MIN ~ DANG_MAX）。
     * 用于校准等需要使用所有档位的场合。
     * 
     * @param scaleId 档位ID
     * @return true表示有效，false表示无效
     */
    public static boolean isValidScaleId(int scaleId){
        //return (scaleId >= minGear && scaleId <= maxGear);
        return (scaleId >= DANG_MIN && scaleId <= DANG_MAX);  // 检查是否在所有档位范围内，校准时需要用到所有档位
    }
    
    /**
     * 检查档位ID是否有效（扩展方法）
     * 
     * <p>检查给定的档位ID是否在当前允许的范围内（minGear ~ maxGear）。
     * minGear和maxGear可以根据硬件配置动态调整。
     * 
     * @param scaleId 档位ID
     * @return true表示有效，false表示无效
     */
    public static boolean isValidScaleIdExt(int scaleId){
        return (scaleId >= minGear && scaleId <= maxGear);  // 检查是否在动态范围内
    }

    /** 当前档位ID */
    private int scaleId = DANG_1V;  // 档位ID，默认1V/div
    
    /** 微调系数（范围：0.1 ~ 2.5） */
    private double fineScale = 1.0;  // 微调系数，默认1.0
    
    /** 探头类型 */
    private int ProbeType = PROBE_TYPE_VOL;  // 探头类型，默认电压探头
    
    /** 探头描述字符串 */
    private String probeStr = "V";  // 探头描述，默认"V"
    
    /** 探头倍数（如1X、10X等） */
    private double ProbeRate = 1.0;  // 探头倍数，默认1.0


    /** 自动探头对象（可为null） */
    private BaseProbe probe = null;  // 自动探头对象
    
    /**
     * 设置自动探头
     * 
     * <p>设置自动探头对象，启用自动探头功能。
     * 设置后，探头类型和倍数将从探头对象获取。
     * 
     * @param baseProbe 探头对象，null表示禁用自动探头
     */
    public synchronized void setProbe(BaseProbe baseProbe){
        probe = baseProbe;  // 保存探头对象
        bAutoProbe = probe != null;  // 更新自动探头标志


    }
    
    /**
     * 获取自动探头对象
     * 
     * @return 探头对象，如果未设置则返回null
     */
    public synchronized BaseProbe getProbe(){
        return probe;  // 返回探头对象
    }

    /**
     * 默认构造方法
     * 
     * <p>创建VerticalAxis实例，使用默认值：
     * <ul>
     *   <li>档位ID：DANG_1V（1V/div）</li>
     *   <li>微调系数：1.0</li>
     *   <li>探头类型：PROBE_TYPE_VOL（电压）</li>
     *   <li>探头倍数：1.0</li>
     * </ul>
     */
    public VerticalAxis(){

    }

    /**
     * 限制最小档位值
     * 
     * <p>根据硬件配置限制最小档位值。
     * 不同阻抗（1MΩ/50Ω）有不同的最小档位限制。
     * 
     * @param v 输入档位值
     * @param resistanceType 阻抗类型（Channel.RESISTANCE_1M 或 Channel.RESISTANCE_50）
     * @return 限制后的档位值
     */
    public static double clampMin(double v,int resistanceType){
        double digitV = HwConfig.getInstance().getDigitDang(resistanceType);  // 获取硬件配置的最小档位
        return Math.max(v,digitV);  // 返回较大的值
    }

    /**
     * 带参数的构造方法
     * 
     * <p>创建VerticalAxis实例并指定初始参数。
     * 
     * @param scaleId 档位ID
     * @param fine 微调系数
     * @param probeType 探头类型
     * @param probeRate 探头倍数
     */
    public VerticalAxis(int scaleId,double fine,int probeType,double probeRate){
        this.scaleId = scaleId;  // 设置档位ID
        this.fineScale = fine;  // 设置微调系数
        this.ProbeType = probeType;  // 设置探头类型
        this.ProbeRate = probeRate;  // 设置探头倍数
    }

    /**
     * 设置档位ID
     * 
     * <p>设置当前档位ID，会进行有效性检查。
     * 
     * @param scaleId 档位ID
     */
    public synchronized void setScaleId(int scaleId){
        if(isValidScaleId(scaleId))  // 检查档位ID是否有效
            this.scaleId = scaleId;  // 设置档位ID
    }
    
    /**
     * 设置微调系数
     * 
     * <p>设置档位的微调系数，用于在固定档位之间进行精细调整。
     * 
     * @param fine 微调系数
     */
    public synchronized void setFineScale(double fine) {
        fineScale = fine;  // 设置微调系数
    }

    /**
     * 获取档位ID
     * 
     * @return 当前档位ID
     */
    public synchronized int getScaleId(){
        return scaleId;  // 返回档位ID
    }
    
    /**
     * 获取档位值（不含微调和探头倍数）
     * 
     * <p>返回当前档位ID对应的档位值。
     * 
     * @return 档位值（单位：V/div）
     */
    public double getScaleIdVal(){
        return getScaleIdVal(scaleId);  // 返回档位值
    }
    
    /**
     * 获取微调系数
     * 
     * @return 微调系数
     */
    public synchronized double getFineScale() { return fineScale; }  // 返回微调系数

    /**
     * 获取实际档位值
     * 
     * <p>计算实际档位值 = 档位值 * 微调系数 * 探头倍数。
     * 
     * @return 实际档位值（单位：V/div）
     */
    public double getScaleVal(){
        return getScaleIdVal() * getFineScale() * getProbeRate();  // 返回实际档位值
    }

    /**
     * 获取探头类型
     * 
     * <p>如果设置了自动探头，则从探头对象获取类型；
     * 否则返回存储的探头类型。
     * 
     * @return 探头类型
     */
    public int getProbeType() {
        if(probe != null){  // 检查是否有自动探头
            return probe.getProbeType();  // 从探头对象获取类型
        }
        return ProbeType;  // 返回存储的探头类型
    }
    
    /** 自动探头使能标志 */
    private volatile boolean bAutoProbe = false;  // 自动探头使能标志，volatile保证线程可见性
    
    /**
     * 检查是否启用自动探头
     * 
     * @return true表示启用自动探头，false表示禁用
     */
    public synchronized boolean isAutoProbe(){
        return bAutoProbe;  // 返回自动探头使能标志
    }


    /**
     * 设置探头类型
     * 
     * @param probeType 探头类型
     */
    public void setProbeType(int probeType) {
        ProbeType = probeType;  // 设置探头类型
    }
    
    /**
     * 设置探头描述字符串
     * 
     * @param probeStr 探头描述字符串
     */
    public void setProbeStr(String probeStr){
        this.probeStr = probeStr;  // 设置探头描述
    }
    
    /**
     * 获取探头描述字符串
     * 
     * @return 探头描述字符串
     */
    public String getProbeStr(){
        return this.probeStr;  // 返回探头描述
    }
    
    /**
     * 获取探头倍数
     * 
     * <p>如果设置了自动探头，则从探头对象获取倍数；
     * 否则返回存储的探头倍数。
     * 
     * @return 探头倍数
     */
    public double getProbeRate() {
        if(probe != null){  // 检查是否有自动探头
            return probe.getProbeRate();  // 从探头对象获取倍数
        }
        return ProbeRate;  // 返回存储的探头倍数
    }

    /**
     * 设置探头倍数
     * 
     * <p>如果设置了自动探头，则更新探头对象的倍数；
     * 否则更新存储的探头倍数。
     * 
     * @param probeRate 探头倍数
     */
    public synchronized void setProbeRate(double probeRate) {
        if(probe != null){  // 检查是否有自动探头
            probe.setProbeRate(probeRate);  // 更新探头对象的倍数
        } else {
            ProbeRate = probeRate;  // 更新存储的探头倍数
        }
    }

    /**
     * 根据档位ID获取档位值
     * 
     * @param scaleId 档位ID
     * @return 档位值（单位：V/div）
     */
    public  double getScaleIdVal(int scaleId){
        return getScaleIdValById(scaleId);  // 调用静态方法获取档位值
    }
    
    /**
     * 检查档位值是否与档位ID匹配
     * 
     * <p>判断给定的档位值是否在档位ID对应值的±10%范围内。
     * 
     * @param scaleId 档位ID
     * @param scaleIdVal 档位值
     * @return true表示匹配，false表示不匹配
     */
    private  boolean isValidScaleId(int scaleId,double scaleIdVal){
        double val = getScaleIdVal(scaleId);  // 获取档位ID对应的值
        if(scaleIdVal>(val-val/10) && scaleIdVal<(val+val/10)){  // 检查是否在±10%范围内
            return  true;  // 匹配
        }
        return false;  // 不匹配
    }


    /**
     * 获取最小档位索引
     * 
     * @return 最小档位索引
     */
    public static int getMinGear() {
        return minGear;  // 返回最小档位索引
    }

    /**
     * 设置最小档位索引
     * 
     * <p>用于根据硬件配置动态调整最小档位。
     * 
     * @param gear 最小档位索引
     */
    public static void setMinGear(int gear) {
        minGear = gear;  // 设置最小档位索引
    }

    /**
     * 获取最大档位索引
     * 
     * @return 最大档位索引
     */
    public static int getMaxGear() {
        return maxGear;  // 返回最大档位索引
    }

    /**
     * 设置最大档位索引
     * 
     * <p>用于根据硬件配置动态调整最大档位。
     * 
     * @param gear 最大档位索引
     */
    public static void setMaxGear(int gear) {
        maxGear = gear;  // 设置最大档位索引
    }

    /**
     * 根据档位值获取档位ID
     * 
     * <p>在档位表中查找与给定档位值匹配的档位ID。
     * 
     * @param scaleIdVal 档位值
     * @return 档位ID，未找到返回-1
     */
    public  int getScaleId(double scaleIdVal){
        for(int i=minGear;i<=maxGear;i++){  // 遍历档位表
            if(isValidScaleId(i,scaleIdVal)){  // 检查是否匹配
                return i;  // 返回匹配的档位ID
            }
        }
        return -1;  // 未找到返回-1
    }



    /**
     * 根据档位ID获取档位值（静态方法）
     * 
     * <p>查询档位表中指定ID对应的档位值。
     * 
     * <p><b>档位值对照表：</b>
     * <ul>
     *   <li>DANG_250uV → 250×10⁻⁶ V</li>
     *   <li>DANG_500uV → 500×10⁻⁶ V</li>
     *   <li>DANG_1mV → 1×10⁻³ V</li>
     *   <li>...以此类推</li>
     * </ul>
     * 
     * @param scaleId 档位ID
     * @return 档位值（单位：V/div）
     */
    public static double getScaleIdValById(int scaleId){
        switch (scaleId){  // 根据档位ID返回对应值
            case DANG_250uV:
                return 250e-6;  // 250μV = 250×10⁻⁶ V
            case DANG_500uV:
                return 500e-6;  // 500μV = 500×10⁻⁶ V
            case DANG_1mV:
                return 1e-3;  // 1mV = 1×10⁻³ V
            case DANG_2mV:
                return 2e-3;  // 2mV = 2×10⁻³ V
            case DANG_5mV:
                return 5e-3;  // 5mV = 5×10⁻³ V
            case DANG_10mV:
                return 10e-3;  // 10mV = 10×10⁻³ V
            case DANG_20mV:
                return 20e-3;  // 20mV = 20×10⁻³ V
            case DANG_50mV:
                return 50e-3;  // 50mV = 50×10⁻³ V
            case DANG_100mV:
                return 100e-3;  // 100mV = 100×10⁻³ V
            case DANG_200mV:
                return 200e-3;  // 200mV = 200×10⁻³ V
            case DANG_500mV:
                return 500e-3;  // 500mV = 500×10⁻³ V
            case DANG_1V:
                return 1;  // 1V
            case DANG_2V:
                return 2;  // 2V
            case DANG_5V:
                return 5;  // 5V
            case DANG_10V:
            default:
                return 10;  // 10V（默认值）
        }
    }

    /**
     * 检查档位值是否与档位ID匹配（静态方法）
     * 
     * <p>判断给定的档位值是否在档位ID对应值的±10%范围内。
     * 
     * @param scaleId 档位ID
     * @param scaleIdVal 档位值
     * @return true表示匹配，false表示不匹配
     */
    private static  boolean isValidScaleIdByValue(int scaleId,double scaleIdVal){
        double val = getScaleIdValById(scaleId);  // 获取档位ID对应的值
        if(scaleIdVal>(val-val/10) && scaleIdVal<(val+val/10)){  // 检查是否在±10%范围内
            return  true;  // 匹配
        }
        return false;  // 不匹配
    }

    /**
     * 查找最接近的档位ID
     * 
     * <p>在两个相邻档位之间查找最接近给定值的档位ID。
     * 
     * @param scaleId 当前档位ID
     * @param scaleIdVal 目标档位值
     * @return 最接近的档位ID，无法确定返回-1
     */
    private static int findNearScale(int scaleId, double scaleIdVal) {
        double valLeft = getScaleIdValById(scaleId);  // 获取当前档位值
        double valRight = getScaleIdValById(scaleId + 1);  // 获取下一档位值
        if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) <= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近当前档位
            return scaleId;  // 返回当前档位ID
        } else if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) >= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近下一档位
            return scaleId + 1;  // 返回下一档位ID
        } else {
            return -1;  // 无法确定
        }
    }


    /**
     * 根据档位值获取档位ID（静态方法）
     * 
     * <p>在档位表中查找最接近给定值的档位ID。
     * 用于自动档位调整时确定合适的档位。
     * 
     * @param scaleIdVal 档位值
     * @return 档位ID，未找到返回-1
     */
    public static int getScaleIdByValue(double scaleIdVal) {
        for (int i = minGear; i <= maxGear; i++) {  // 遍历档位表
            if (i < maxGear && findNearScale(i, scaleIdVal) != -1) {  // 检查是否在两个档位之间
                return findNearScale(i, scaleIdVal);  // 返回最接近的档位ID
            } else if (isValidScaleIdByValue(i, scaleIdVal)) {  // 检查是否匹配
                return i;  // 返回匹配的档位ID
            }
        }
        return -1;  // 未找到返回-1
    }


    /**
     * 转换为字符串表示
     * 
     * <p>返回VerticalAxis对象的字符串表示，用于调试。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "VerticalAxis{" +  // 类名
                "scaleId=" + scaleId +  // 档位ID
                ", fineScale=" + fineScale +  // 微调系数
                ", ProbeType=" + ProbeType +  // 探头类型
                ", probeStr='" + probeStr + '\'' +  // 探头描述
                ", ProbeRate=" + ProbeRate +  // 探头倍数
                ", probe=" + probe +  // 探头对象
                ", bAutoProbe=" + bAutoProbe +  // 自动探头标志
                '}';  // 结束
    }
}
