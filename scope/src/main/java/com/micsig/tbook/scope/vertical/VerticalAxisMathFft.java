package com.micsig.tbook.scope.vertical;  // 定义包名：示波器垂直轴管理模块

/**
 * 数学FFT运算垂直轴管理类 - 管理数学通道FFT频谱分析的垂直档位
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.vertical（示波器垂直轴管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 数学运算垂直轴管理</li>
 *   <li>设计模式：继承模式 + 双模式管理</li>
 *   <li>职责类型：定义FFT档位、支持dB/RMS双模式转换</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义FFT运算的垂直档位常量（dB模式和RMS模式）</li>
 *   <li>提供dB模式档位管理（1dB ~ 500dB）</li>
 *   <li>提供RMS模式档位管理（500μV ~ 10V）</li>
 *   <li>支持dB与RMS之间的双向转换</li>
 * </ul>
 * 
 * <p><b>FFT垂直轴架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   VerticalAxisMathFft - FFT垂直轴管理                                    │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   双模式支持                                                      │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────────────┐    ┌─────────────────────┐             │   │
 * │   │   │   dB模式（对数）     │    │   RMS模式（线性）    │             │   │
 * │   │   │                     │    │                     │             │   │
 * │   │   │   1dB/div           │    │   500μV/div         │             │   │
 * │   │   │   2dB/div           │    │   1mV/div           │             │   │
 * │   │   │   5dB/div           │    │   2mV/div           │             │   │
 * │   │   │   10dB/div          │    │   5mV/div           │             │   │
 * │   │   │   20dB/div          │    │   10mV/div          │             │   │
 * │   │   │   50dB/div          │    │   20mV/div          │             │   │
 * │   │   │   100dB/div         │    │   50mV/div          │             │   │
 * │   │   │   200dB/div         │    │   100mV/div         │             │   │
 * │   │   │   500dB/div         │    │   200mV/div         │             │   │
 * │   │   │                     │    │   500mV/div         │             │   │
 * │   │   │   共9个档位         │    │   1V/div            │             │   │
 * │   │   │                     │    │   2V/div            │             │   │
 * │   │   │                     │    │   5V/div            │             │   │
 * │   │   │                     │    │   10V/div           │             │   │
 * │   │   │                     │    │                     │             │   │
 * │   │   │                     │    │   共14个档位        │             │   │
 * │   │   └─────────────────────┘    └─────────────────────┘             │   │
 * │   │                                                                   │   │
 * │   │   模式切换：通过probeType切换（PROBE_TYPE_DB / PROBE_TYPE_VOL）   │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   dB与RMS转换公式                                                 │   │
 * │   │                                                                   │   │
 * │   │   dB = 20 × log₁₀(V / Vref)                                      │   │
 * │   │                                                                   │   │
 * │   │   其中 Vref = 1V（参考电压）                                      │   │
 * │   │                                                                   │   │
 * │   │   例如：                                                          │   │
 * │   │   1V = 0dB, 10V = 20dB, 100mV = -20dB                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>FFT频谱分析说明：</b>
 * <pre>
 * FFT（快速傅里叶变换）将时域信号转换为频域信号：
 *   ├── 横轴：频率（Hz）
 *   ├── 纵轴：幅度
 *   │   ├── RMS模式：线性刻度，单位为V
 *   │   └── dB模式：对数刻度，单位为dB
 *   └── 用途：分析信号的频率成分、谐波失真、噪声等
 * 
 * dB模式优势：
 *   ├── 可以显示更宽的动态范围
 *   ├── 便于观察小信号和大信号同时存在的情况
 *   └── 符合电子工程习惯
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   VerticalAxis（模拟通道垂直轴）
 *       │
 *       └── VerticalAxisMathFft（FFT垂直轴）
 *               │
 *               ├── 重写：setScaleId()、getScaleId()、getScaleVal()等
 *               └── 扩展：dB模式档位、RMS模式档位、双模式转换
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>MathFFTWave类管理FFT运算的垂直档位</li>
 *   <li>MathChannel切换到FFT运算模式时使用此类</li>
 *   <li>用户切换dB/RMS显示模式时调用相关方法</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：VerticalAxis（垂直轴基类）</li>
 *   <li>被依赖：MathFFTWave（FFT运算实现）</li>
 *   <li>被依赖：MathChannel（数学通道）</li>
 * </ul>
 * 
 * @author Administrator
 * @version 1.0
 * @since 2018/9/7
 * @see VerticalAxis 垂直轴基类
 * @see MathFFTWave FFT运算实现
 * @see MathChannel 数学通道
 */
public class VerticalAxisMathFft extends VerticalAxis{
    
    /** 日志标签 */
    private static final String TAG = "VerticalAxisMathFft";  // 日志输出标签
    
    /**
     * dB模式档位常量定义
     * 
     * <p>用于FFT频谱分析的对数刻度显示，单位为dB/div。
     * dB（分贝）是对数单位，可以表示更宽的动态范围。
     */
    
    /** dB模式档位常量：最小档位索引 */
    public static final int DANG_DBV_MIN    = 0;  // dB模式最小档位索引
    
    /** dB模式档位常量：1dB/div */
    public static final int DANG_DBV_1DB    = DANG_DBV_MIN;  // 1dB档位（索引0）
    
    /** dB模式档位常量：2dB/div */
    public static final int DANG_DBV_2DB    = 1;  // 2dB档位（索引1）
    
    /** dB模式档位常量：5dB/div */
    public static final int DANG_DBV_5DB    = 2;  // 5dB档位（索引2）
    
    /** dB模式档位常量：10dB/div */
    public static final int DANG_DBV_10DB   = 3;  // 10dB档位（索引3）
    
    /** dB模式档位常量：20dB/div */
    public static final int DANG_DBV_20DB   = 4;  // 20dB档位（索引4）
    
    /** dB模式档位常量：50dB/div */
    public static final int DANG_DBV_50DB   = 5;  // 50dB档位（索引5）
    
    /** dB模式档位常量：100dB/div */
    public static final int DANG_DBV_100DB  = 6;  // 100dB档位（索引6）
    
    /** dB模式档位常量：200dB/div */
    public static final int DANG_DBV_200DB  = 7;  // 200dB档位（索引7）
    
    /** dB模式档位常量：500dB/div - 最大档位 */
    public static final int DANG_DBV_500DB  = 8;  // 500dB档位（索引8）
    
    /** dB模式档位常量：最大档位索引 */
    public static final int DANG_DBV_MAX    = DANG_DBV_500DB;  // dB模式最大档位
    
    /** dB模式档位常量：档位总数 */
    public static final int DANG_DBV_CNT    = DANG_DBV_MAX-DANG_DBV_MIN+1;  // dB模式档位总数（9个）
    
    /**
     * RMS模式档位常量定义
     * 
     * <p>用于FFT频谱分析的线性刻度显示，单位为V/div。
     * RMS（均方根）表示信号的有效值。
     */
    
    /** RMS模式档位常量：最小档位索引 */
    public static final int DANG_RMS_MIN    = 0;  // RMS模式最小档位索引
    
    /** RMS模式档位常量：500μV/div */
    public static final int DANG_RMS_500uV  = DANG_RMS_MIN;  // 500μV档位（索引0，约-66dB）
    
    /** RMS模式档位常量：1mV/div */
    public static final int DANG_RMS_1mV    = 1;  // 1mV档位（索引1，-60dB）
    
    /** RMS模式档位常量：2mV/div */
    public static final int DANG_RMS_2mV    = 2;  // 2mV档位（索引2，约-54dB）
    
    /** RMS模式档位常量：5mV/div */
    public static final int DANG_RMS_5mV    = 3;  // 5mV档位（索引3，约-46dB）
    
    /** RMS模式档位常量：10mV/div */
    public static final int DANG_RMS_10mV   = 4;  // 10mV档位（索引4，-40dB）
    
    /** RMS模式档位常量：20mV/div */
    public static final int DANG_RMS_20mV   = 5;  // 20mV档位（索引5，约-34dB）
    
    /** RMS模式档位常量：50mV/div */
    public static final int DANG_RMS_50mV   = 6;  // 50mV档位（索引6，约-26dB）
    
    /** RMS模式档位常量：100mV/div */
    public static final int DANG_RMS_100mV  = 7;  // 100mV档位（索引7，-20dB）
    
    /** RMS模式档位常量：200mV/div */
    public static final int DANG_RMS_200mV  = 8;  // 200mV档位（索引8，约-14dB）
    
    /** RMS模式档位常量：500mV/div */
    public static final int DANG_RMS_500mV  = 9;  // 500mV档位（索引9，约-6dB）
    
    /** RMS模式档位常量：1V/div */
    public static final int DANG_RMS_1V     = 10;  // 1V档位（索引10，0dB）
    
    /** RMS模式档位常量：2V/div */
    public static final int DANG_RMS_2V     = 11;  // 2V档位（索引11，约6dB）
    
    /** RMS模式档位常量：5V/div */
    public static final int DANG_RMS_5V     = 12;  // 5V档位（索引12，约14dB）
    
    /** RMS模式档位常量：10V/div - 最大档位 */
    public static final int DANG_RMS_10V    = 13;  // 10V档位（索引13，20dB）
    
    /** RMS模式档位常量：最大档位索引 */
    public static final int DANG_RMS_MAX    = DANG_RMS_10V;  // RMS模式最大档位
    
    /** RMS模式档位常量：档位总数 */
    public static final int DANG_RMS_CNT    = DANG_RMS_MAX - DANG_RMS_MIN + 1;  // RMS模式档位总数（14个）


    /** dB模式当前档位ID */
    private int scaleId = DANG_DBV_MIN;  // dB模式档位ID，默认最小档位
    
    /** RMS模式当前档位ID */
    private int scaleIdInRms = DANG_RMS_MIN;  // RMS模式档位ID，默认最小档位
    
    /** dB模式微调系数 */
    private double fineScale = 1.0;  // dB模式微调系数，默认1.0
    
    /** RMS模式微调系数 */
    private double fineScaleInRms = 1.0;  // RMS模式微调系数，默认1.0

    /**
     * 默认构造方法
     * 
     * <p>创建VerticalAxisMathFft实例，使用默认值：
     * <ul>
     *   <li>dB模式档位ID：DANG_DBV_MIN</li>
     *   <li>RMS模式档位ID：DANG_RMS_MIN</li>
     *   <li>微调系数：1.0</li>
     * </ul>
     */
    public VerticalAxisMathFft(){}

    //region DBV FUNCTIONS - dB模式功能方法
    
    /**
     * 检查dB模式档位ID是否有效
     * 
     * <p>检查给定的档位ID是否在dB模式有效范围内（DANG_DBV_MIN ~ DANG_DBV_MAX）。
     * 
     * @param scaleId dB模式档位ID
     * @return true表示有效，false表示无效
     */
    public static boolean isValidScaleId(int scaleId){
        return (scaleId >= DANG_DBV_MIN && scaleId <= DANG_DBV_MAX);  // 检查是否在有效范围内
    }
    
    /**
     * 设置dB模式档位ID
     * 
     * <p>设置当前dB模式的档位ID，会进行有效性检查。
     * 
     * @param scaleId dB模式档位ID
     */
    @Override
    public void setScaleId(int scaleId){
        if(isValidScaleId(scaleId)) this.scaleId = scaleId;  // 有效则设置
    }
    
    /**
     * 获取dB模式档位ID
     * 
     * @return 当前dB模式档位ID
     */
    @Override
    public int getScaleId(){
        return scaleId;  // 返回dB模式档位ID
    }
    
    /**
     * 根据档位值获取档位ID
     * 
     * <p>调用dB模式的档位查找方法。
     * 
     * @param scaleIdVal 档位值
     * @return 档位ID
     */
    @Override
    public int getScaleId(double scaleIdVal){
        return getScaleIdInDb(scaleIdVal);  // 调用dB模式查找方法
    }



    /**
     * 查找最接近的dB档位ID
     * 
     * <p>在两个相邻dB档位之间查找最接近给定值的档位ID。
     * 
     * @param scaleId 当前档位ID
     * @param scaleIdVal 目标档位值
     * @return 最接近的档位ID，无法确定返回-1
     */
    private static int findNearScale(int scaleId, double scaleIdVal) {
        double valLeft = dbvIdToValue(scaleId);  // 获取当前档位值
        double valRight = dbvIdToValue(scaleId + 1);  // 获取下一档位值
        if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) <= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近当前档位
            return scaleId;  // 返回当前档位ID
        } else if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) >= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近下一档位
            return scaleId + 1;  // 返回下一档位ID
        } else {
            return -1;  // 无法确定
        }
    }



    /**
     * 根据dB值获取dB模式档位ID
     * 
     * <p>在dB档位表中查找与给定dB值匹配的档位ID。
     * 
     * @param scaleIdInDb dB值
     * @return dB模式档位ID
     */
    public static int getScaleIdInDb(double scaleIdInDb){
        for (int i = DANG_DBV_MIN; i <= DANG_DBV_CNT; i++) {  // 遍历dB档位表
            if (i < DANG_DBV_CNT && findNearScale(i, scaleIdInDb) != -1) {  // 检查是否在两个档位之间
                return findNearScale(i, scaleIdInDb);  // 返回最接近的档位ID
            } else if (isValidScaleId(i, scaleIdInDb)) {  // 检查是否精确匹配
                return i;  // 返回匹配的档位ID
            }
        }
        return DANG_DBV_MAX;  // 未找到返回最大档位
    }
    
    /**
     * 获取当前dB模式档位值
     * 
     * <p>返回当前dB模式档位ID对应的档位值。
     * 
     * @return dB模式档位值（单位：dB/div）
     */
    @Override
    public double getScaleIdVal(){
        return dbvIdToValue(this.scaleId);  // 调用静态方法获取档位值
    }
    
    /**
     * 根据dB模式档位ID获取档位值
     * 
     * <p>调用静态方法dbvIdToValue。
     * 
     * @param scaleId dB模式档位ID
     * @return dB模式档位值（单位：dB/div）
     */
    @Override
    public double getScaleIdVal(int scaleId){
        return dbvIdToValue(scaleId);  // 调用静态方法
    }
    
    /**
     * 获取dB模式实际档位值
     * 
     * <p>计算实际档位值 = 档位值 * 微调系数。
     * 
     * @return 实际档位值（单位：dB/div）
     */
    @Override
    public double getScaleVal(){
        return getScaleIdVal()*getFineScale();  // 返回实际档位值
    }
    
    /**
     * 获取dB模式微调系数
     * 
     * @return dB模式微调系数
     */
    @Override
    public double getFineScale(){
        return fineScale;  // 返回微调系数
    }
    
    /**
     * 设置dB模式微调系数
     * 
     * @param fine 微调系数
     */
    @Override
    public void setFineScale(double fine){
        fineScale = fine;  // 设置微调系数
    }

    /**
     * 将RMS档位ID转换为dB档位ID
     * 
     * <p>根据RMS档位值计算对应的dB值，然后查找最接近的dB档位。
     * 使用公式：dB = 20 × log₁₀(V)
     * 
     * <p><b>转换示例：</b>
     * <pre>
     *   1V → 0dB → 对应dB档位
     *   10V → 20dB → 对应dB档位
     *   100mV → -20dB → 对应dB档位（取绝对值）
     * </pre>
     * 
     * @param _vScale RMS模式档位ID
     * @return 对应的dB模式档位ID
     */
    public int vScaleInRmsToDbv(int _vScale) {
        float dbv = (float) Math.abs(20*Math.log10(getScaleIdValInRms(_vScale)));  // 计算dB值（取绝对值）
        for(int i = DANG_DBV_MAX; i>= DANG_DBV_MIN; i--){  // 从大到小遍历dB档位
            if(dbv >= dbvIdToValue(i)) {  // 找到第一个小于等于目标值的档位
                return i;  // 返回该档位ID
            }
        }
        return DANG_DBV_MIN;  // 未找到返回最小档位
    }

    /**
     * 将dB模式档位ID转换为档位值
     * 
     * <p>查询dB档位表中指定ID对应的档位值。
     * 
     * <p><b>档位值对照表：</b>
     * <ul>
     *   <li>DANG_DBV_1DB → 1dB</li>
     *   <li>DANG_DBV_2DB → 2dB</li>
     *   <li>DANG_DBV_5DB → 5dB</li>
     *   <li>...以此类推</li>
     * </ul>
     * 
     * @param dbvScaleId dB模式档位ID
     * @return dB模式档位值，无效ID返回-1
     */
    public static double dbvIdToValue(int dbvScaleId){
        switch(dbvScaleId)  // 根据档位ID返回对应值
        {
            case DANG_DBV_1DB:      return 1;  // 1dB
            case DANG_DBV_2DB:      return 2;  // 2dB
            case DANG_DBV_5DB:      return 5;  // 5dB
            case DANG_DBV_10DB:     return 10;  // 10dB
            case DANG_DBV_20DB:     return 20;  // 20dB
            case DANG_DBV_50DB:     return 50;  // 50dB
            case DANG_DBV_100DB:    return 100;  // 100dB
            case DANG_DBV_200DB:    return 200;  // 200dB
            case DANG_DBV_500DB:    return 500;  // 500dB
            default:                return -1;  // 无效ID返回-1
        }
    }


    /**
     * 检查dB档位值是否与档位ID匹配
     * 
     * <p>判断给定的dB档位值是否在档位ID对应值的±10%范围内。
     * 
     * @param scaleId dB模式档位ID
     * @param scaleIdVal dB档位值
     * @return true表示匹配，false表示不匹配
     */
    private static boolean isValidScaleId(int scaleId,double scaleIdVal){
        double val = dbvIdToValue(scaleId);  // 获取档位ID对应的值
        if(scaleIdVal>(val-val/10) && scaleIdVal<(val+val/10)){  // 检查是否在±10%范围内
            return  true;  // 匹配
        }
        return false;  // 不匹配
    }
    //endregion
    
    //region RMS FUNCTIONS - RMS模式功能方法
    
    /**
     * 检查RMS模式档位ID是否有效
     * 
     * <p>检查给定的档位ID是否在RMS模式有效范围内（DANG_RMS_MIN ~ DANG_RMS_MAX）。
     * 
     * @param scaleIdInRms RMS模式档位ID
     * @return true表示有效，false表示无效
     */
    public static boolean isValidScaleIdInRms(int scaleIdInRms){
        return (scaleIdInRms >= DANG_RMS_MIN && scaleIdInRms <= DANG_RMS_MAX);  // 检查是否在有效范围内
    }
    
    /**
     * 设置RMS模式档位ID
     * 
     * <p>设置当前RMS模式的档位ID，会进行有效性检查。
     * 
     * @param scaleIdInRms RMS模式档位ID
     */
    public void setScaleIdInRms(int scaleIdInRms) {
        if(isValidScaleIdInRms(scaleIdInRms)) this.scaleIdInRms = scaleIdInRms;  // 有效则设置
    }
    
    /**
     * 获取RMS模式档位ID
     * 
     * @return 当前RMS模式档位ID
     */
    public int getScaleIdInRms(){
        return scaleIdInRms;  // 返回RMS模式档位ID
    }


    /**
     * 查找最接近的RMS档位ID
     * 
     * <p>在两个相邻RMS档位之间查找最接近给定值的档位ID。
     * 
     * @param scaleId 当前档位ID
     * @param scaleIdVal 目标档位值
     * @return 最接近的档位ID，无法确定返回-1
     */
    private static int findNearScaleInRums(int scaleId, double scaleIdVal) {
        double valLeft = getScaleIdValInRms(scaleId);  // 获取当前档位值
        double valRight = getScaleIdValInRms(scaleId + 1);  // 获取下一档位值
        if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) <= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近当前档位
            return scaleId;  // 返回当前档位ID
        } else if (valLeft <= scaleIdVal && valRight >= scaleIdVal && Math.abs(valLeft - scaleIdVal) >= Math.abs(valRight - scaleIdVal)) {  // 目标值更接近下一档位
            return scaleId + 1;  // 返回下一档位ID
        } else {
            return -1;  // 无法确定
        }
    }


    /**
     * 根据RMS值获取RMS模式档位ID
     * 
     * <p>在RMS档位表中查找与给定RMS值匹配的档位ID。
     * 
     * @param scaleIdInRms RMS值（单位：V）
     * @return RMS模式档位ID，未找到返回-1
     */
    public static int getScaleIdInRms(double scaleIdInRms) {
        for (int i = DANG_RMS_MIN; i <= DANG_RMS_CNT; i++) {  // 遍历RMS档位表
            if (i < DANG_RMS_CNT && findNearScaleInRums(i, scaleIdInRms) != -1) {  // 检查是否在两个档位之间
                return findNearScaleInRums(i, scaleIdInRms);  // 返回最接近的档位ID
            } else if (isValidScaleIdInRms(i, scaleIdInRms)) {  // 检查是否精确匹配
                return i;  // 返回匹配的档位ID
            }
        }
        return -1;  // 未找到返回-1
    }

    /**
     * 获取当前RMS模式档位值
     * 
     * <p>返回当前RMS模式档位ID对应的档位值。
     * 
     * @return RMS模式档位值（单位：V/div）
     */
    public double getScaleIdValInRms(){
        return rmsIdToValue(this.scaleIdInRms);  // 调用静态方法获取档位值
    }

    /**
     * 根据RMS模式档位ID获取档位值（静态方法）
     * 
     * <p>查询RMS档位表中指定ID对应的档位值。
     * 
     * <p><b>档位值对照表（含对应dB值）：</b>
     * <ul>
     *   <li>DANG_RMS_500uV → 500μV（约-66dB）</li>
     *   <li>DANG_RMS_1mV → 1mV（-60dB）</li>
     *   <li>DANG_RMS_1V → 1V（0dB）</li>
     *   <li>DANG_RMS_10V → 10V（20dB）</li>
     * </ul>
     * 
     * @param scaleIdInRms RMS模式档位ID
     * @return RMS模式档位值（单位：V/div）
     */
    public static double getScaleIdValInRms(int scaleIdInRms) {
        return rmsIdToValue(scaleIdInRms);  // 调用静态方法
    }

    /**
     * 获取RMS模式实际档位值
     * 
     * <p>返回当前RMS模式档位值（不含微调系数）。
     * 
     * @return RMS模式档位值（单位：V/div）
     */
    public double getScaleValInRms(){
        return getScaleIdValInRms();  // 返回档位值
    }

    /**
     * 获取RMS模式微调系数
     * 
     * @return RMS模式微调系数
     */
    public double getFineScaleInRms(){
        return fineScaleInRms;  // 返回微调系数
    }
    
    /**
     * 设置RMS模式微调系数
     * 
     * @param fineInRms 微调系数
     */
    public void setFineScaleInRms(double fineInRms){
        fineScaleInRms = fineInRms;  // 设置微调系数
    }

    /**
     * 将RMS模式档位ID转换为档位值
     * 
     * <p>查询RMS档位表中指定ID对应的档位值。
     * 注释中包含了每个档位对应的dB值参考。
     * 
     * @param rmsScaleId RMS模式档位ID
     * @return RMS模式档位值（单位：V/div）
     */
    public static double rmsIdToValue(int rmsScaleId) {
        switch (rmsScaleId)  // 根据档位ID返回对应值
        {
            case DANG_RMS_500uV:    return 500e-6;  // 500μV = 500×10⁻⁶V（约-66dB）
            case DANG_RMS_1mV:      return 1e-3;    // 1mV = 1×10⁻³V（-60dB）
            case DANG_RMS_2mV:      return 2e-3;    // 2mV（约-54dB）
            case DANG_RMS_5mV:      return 5e-3;    // 5mV（约-46dB）
            case DANG_RMS_10mV:     return 10e-3;   // 10mV（-40dB）
            case DANG_RMS_20mV:     return 20e-3;   // 20mV（约-34dB）
            case DANG_RMS_50mV:     return 50e-3;   // 50mV（约-26dB）
            case DANG_RMS_100mV:    return 100e-3;  // 100mV（-20dB）
            case DANG_RMS_200mV:    return 200e-3;  // 200mV（约-14dB）
            case DANG_RMS_500mV:    return 500e-3;  // 500mV（约-6dB）
            case DANG_RMS_1V:       return 1;       // 1V（0dB）
            case DANG_RMS_2V:       return 2;       // 2V（约6dB）
            case DANG_RMS_5V:       return 5;       // 5V（约14dB）
            case DANG_RMS_10V:
            default:                return 10;      // 10V（20dB）
        }
    }

    /**
     * 根据RMS值获取RMS模式档位ID
     * 
     * <p>根据给定的RMS值查找最接近的档位ID。
     * 使用容差值进行匹配，确保档位选择的准确性。
     * 
     * <p><b>匹配算法：</b>
     * <ul>
     *   <li>从大到小遍历档位</li>
     *   <li>使用容差值（5e-3、5e-6、5e-9）进行匹配</li>
     *   <li>返回第一个匹配的档位ID</li>
     * </ul>
     * 
     * @param rmsScale RMS值（单位：V）
     * @return RMS模式档位ID
     */
    public int rmsIdFromValue(double rmsScale) {
        if(rmsScale+5e-3 >= 10) return DANG_RMS_10V;  // 10V档位
        else if(rmsScale+5e-3 >= 5) return DANG_RMS_5V;  // 5V档位
        else if(rmsScale+5e-3 >= 2) return DANG_RMS_2V;  // 2V档位
        else if(rmsScale+5e-3 >= 1) return DANG_RMS_1V;  // 1V档位
        else if(rmsScale+5e-6 >= 500e-3) return DANG_RMS_500mV;  // 500mV档位
        else if(rmsScale+5e-6 >= 200e-3) return DANG_RMS_200mV;  // 200mV档位
        else if(rmsScale+5e-6 >= 100e-3) return DANG_RMS_100mV;  // 100mV档位
        else if(rmsScale+5e-6 >= 50e-3) return DANG_RMS_50mV;  // 50mV档位
        else if(rmsScale+5e-6 >= 20e-3) return DANG_RMS_20mV;  // 20mV档位
        else if(rmsScale+5e-6 >= 10e-3) return DANG_RMS_10mV;  // 10mV档位
        else if(rmsScale+5e-9 >= 5e-3) return DANG_RMS_5mV;  // 5mV档位
        else if(rmsScale+5e-9 >= 2e-3) return DANG_RMS_2mV;  // 2mV档位
        else if(rmsScale+5e-9 >= 1e-3) return DANG_RMS_1mV;  // 1mV档位
        else if(rmsScale+5e-9 >= 500e-6) return DANG_RMS_500uV;  // 500μV档位
        else return DANG_RMS_MIN;  // 返回最小档位
    }

    /**
     * 检查RMS档位值是否与档位ID匹配
     * 
     * <p>判断给定的RMS档位值是否在档位ID对应值的±10%范围内。
     * 
     * @param scaleIdInRms RMS模式档位ID
     * @param scaleIdValInRms RMS档位值
     * @return true表示匹配，false表示不匹配
     */
    private static boolean isValidScaleIdInRms(int scaleIdInRms,double scaleIdValInRms){
        double val = getScaleIdValInRms(scaleIdInRms);  // 获取档位ID对应的值
        if(scaleIdValInRms>(val-val/10) && scaleIdValInRms<(val+val/10)){  // 检查是否在±10%范围内
            return  true;  // 匹配
        }
        return false;  // 不匹配
    }
    //endregion

}
