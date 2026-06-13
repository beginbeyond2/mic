package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import android.util.Log;  // 导入Log类：Android日志输出工具


/**
 * 误差趋近算法 - 示波器校准参数自动调整算法
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：算法层 - 校准算法</li>
 *   <li>设计模式：状态机模式</li>
 *   <li>职责类型：校准参数自动调整、误差趋近计算</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现误差趋近算法，自动调整校准参数</li>
 *   <li>支持零点校准和系数校准两种模式</li>
 *   <li>跟踪最佳校准值，确保校准精度</li>
 *   <li>使用状态机控制调整过程（大幅度→小幅度）</li>
 * </ul>
 * 
 * <p><b>算法原理：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   Wucha ─ 误差趋近算法状态机                                              │
 * │                                                                          │
 * │   状态0（初始状态）：                                                      │
 * │   ├── 根据误差方向确定初始调整方向                                         │
 * │   └── 进入状态1                                                           │
 * │                                                                          │
 * │   状态1（大幅度调整期-第一阶段）：                                          │
 * │   ├── 根据误差值计算调整步长（ix = fx / volStep）                          │
 * │   ├── 大幅度调整设置值                                                    │
 * │   ├── 检测方向变化（过零点）                                               │
 * │   └── 方向变化时进入状态2                                                  │
 * │                                                                          │
 * │   状态2（大幅度调整期-第二阶段）：                                          │
 * │   ├── 继续大幅度调整                                                      │
 * │   ├── 再次检测方向变化                                                    │
 * │   └── 方向变化时进入状态3（小幅度调整）                                     │
 * │                                                                          │
 * │   状态3+（小幅度调整期）：                                                  │
 * │   ├── 每次调整1个单位                                                     │
 * │   ├── 达到预定次数后完成校准                                               │
 * │   └── 输出最佳校准值                                                      │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>校准类型：</b>
 * <ul>
 *   <li><b>零点校准（zero_or_coef=true）：</b>
 *       直接使用误差值fx = nowVol - stdVol</li>
 *   <li><b>系数校准（zero_or_coef=false）：</b>
 *       使用相对误差值fx = (nowVol - stdVol) / stdVol</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>通道零点校准：调整通道的零点偏移</li>
 *   <li>通道增益校准：调整通道的增益系数</li>
 *   <li>探头校准：调整探头的补偿参数</li>
 *   <li>其他需要自动调整整数参数的校准场景</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * Wucha wucha = new Wucha(0.01f, 1.0f, true);  // 步进0.01，标准值1.0，零点校准
 * wucha.setLimtVol(0, 255);  // 设置参数范围0-255
 * 
 * while (!wucha.isFinished()) {
 *     float nowVol = measureVoltage();  // 测量当前电压
 *     int newSet = wucha.process(nowVol, lastSet);  // 计算新的设置值
 *     setCalibrateParam(newSet);  // 设置校准参数
 *     lastSet = newSet;
 * }
 * 
 * int bestValue = wucha.getBestSetVol();  // 获取最佳校准值
 * </pre>
 * 
 * @author xuj
 * @version 1.0
 * @since 2018/7/19
 */
public class Wucha {
    
    // ==================== 成员变量 ====================
    
    /** 状态机状态：0=初始，1=大幅度调整第一阶段，2=大幅度调整第二阶段，3+=小幅度调整 */
    private int wuChaCnt;  // 状态机计数器
    
    /** 每次步进值：调整时每个单位对应的电压变化量 */
    private float volStep;  // 步进值，决定调整的精度
    
    /** 标准值：校准的目标值 */
    private float stdVol;  // 期望达到的标准电压值
    
    /** 校准类型标志：true=零点校准，false=系数校准 */
    private boolean zero_or_coef;  // =1零点类别校准，=0系数类别校准
    
    /** 变化方向：用于检测过零点 */
    private int direct;  // 0=正向，-1=负向，3+=小幅度调整次数
    
    /** 最佳误差值跟踪：记录最小的误差值 */
    private float bestVol;  // 最佳误差值（nowVol - stdVol）
    
    /** 最佳值记录：[0]=标准值，[1]=实际测量值 */
    private float bestVol_rcd[]=new float[2];  // 记录最佳值时的输入和标准
    
    /** 最佳设置值备份：记录误差最小时的设置值 */
    private int bakCoef;  // 最佳设置值
    
    /** 上次的设置值：用于状态2时回退 */
    private int lastCoef;  // 上次设置值备份
    
    /** 设置值最小值限制 */
    private int limt_min;  // 参数最小值
    
    /** 设置值最大值限制 */
    private int limt_max;  // 参数最大值
    
    /** 完成标志：true表示校准已完成 */
    private boolean finished;  // 校准完成标志
    
    /** 日志标签 */
    private final String TAG="calibrate:Wucha";  // 日志输出标签

    // ==================== 构造方法 ====================
    
    /**
     * 默认构造方法：初始化误差趋近算法
     * 
     * <p>调用reStart()初始化所有状态变量。
     */
    public Wucha() {
        reStart();  // 初始化状态
    }

    /**
     * 带参数构造方法：初始化误差趋近算法并设置参数
     * 
     * @param _volStep 步进值，每个单位对应的电压变化量
     * @param _stdVol 标准值，校准的目标值
     * @param _zero_or_coef 校准类型，true=零点校准，false=系数校准
     */
    public Wucha(float _volStep, float _stdVol, boolean _zero_or_coef) {
        this();  // 调用默认构造方法
        volStep =_volStep;  // 设置步进值
        stdVol = _stdVol;  // 设置标准值
        zero_or_coef = _zero_or_coef;  // 设置校准类型
    }

    // ==================== 状态控制方法 ====================
    
    /**
     * 重置算法状态
     * 
     * <p>将所有状态变量重置为初始值，用于重新开始校准。
     */
    public void reStart() {
        wuChaCnt = 0;  // 重置状态机为初始状态
        bakCoef = 255;  // 初始化最佳设置值为255（无效值）
        bestVol = 255;  // 初始化最佳误差为255（无效值）
        finished = false;  // 重置完成标志
    }

    // ==================== 核心算法方法 ====================
    
    /**
     * 执行误差趋近算法处理
     * 
     * <p>根据当前测量值和上次设置值，计算新的设置值。
     * 使用状态机控制调整过程，从大幅度调整逐渐过渡到小幅度调整。
     * 
     * <p><b>算法流程：</b>
     * <ol>
     *   <li>计算误差值fx = nowVol - stdVol</li>
     *   <li>跟踪最佳值（误差绝对值最小的点）</li>
     *   <li>根据状态机状态执行调整：
     *     <ul>
     *       <li>状态0：确定初始方向</li>
     *       <li>状态1/2：大幅度调整</li>
     *       <li>状态3+：小幅度调整</li>
     *     </ul>
     *   </li>
     *   <li>返回新的设置值</li>
     * </ol>
     * 
     * @param nowVol 当前测量值（实际电压）
     * @param lastSet 上次的设置值
     * @return 新的设置值
     */
    public int process(float nowVol, int lastSet) {
        //printf("lastSet=%d\n",lastSet);
        int value = lastSet;  // 初始化返回值为上次设置值
        float fx1 = nowVol - stdVol;  // 计算误差值（实际值 - 标准值）
        if(Math.abs(fx1) < Math.abs(bestVol))
        {// 跟踪最佳值：如果当前误差绝对值小于历史最佳
            bestVol = fx1;  // 更新最佳误差值
            bakCoef = lastSet;  // 记录对应的设置值
            bestVol_rcd[0] = stdVol;  // 记录标准值
            bestVol_rcd[1] = nowVol;  // 记录实际测量值
            //printf("bestvol=%f,%f\n",bestVol_rcd[0],bestVol_rcd[1]);
        }
        float fx=fx1;  // 复制误差值用于后续计算
        if(!zero_or_coef)  // 如果是系数校准
            fx /= stdVol;  // 使用相对误差：fx = (nowVol - stdVol) / stdVol

        int ix = (int)(fx/volStep);  // 计算调整步数：误差 / 步进值
        //mprintf("fx=%f,step=%f,ix=%d\n",fx1,volStep,ix);

        //test
//    value = lastSet+kk;
//    if(value > limt_max)
//        value = limt_min;
//    else if(value < limt_min)
//        value = limt_max;
//    return value;

        if(wuChaCnt==0)  // 状态0：初始状态
        {
            if(fx > 0)  // 误差为正（实际值 > 标准值）
                direct = 0;  // 设置方向为正向（需要减小）
            else  // 误差为负（实际值 < 标准值）
                direct = -1;  // 设置方向为负向（需要增大）
            wuChaCnt = 1;  // 进入状态1
        }
        //-------
        if(wuChaCnt == 1 || wuChaCnt == 2)// 注意，这里不是else if
        {// 大幅度调整期：状态1和状态2
            int direct1;  // 当前方向
            if(fx > 0)  // 误差为正
                direct1 = 0;  // 方向为正向
            else  // 误差为负
                direct1 = -1;  // 方向为负向

            value -= ix;  // 大幅度调整：value = value - 步数
            //mprintf("value=%d\n",value);
            if(value > limt_max)  // 检查上限
                value = limt_max;  // 限制到最大值
            else if(value < limt_min)  // 检查下限
                value = limt_min;  // 限制到最小值

            //mPrintf("direct=%d,direct1=%d\n",direct,direct1);

            boolean done=false;  // 阶段完成标志
            if(direct1 != direct || value == lastSet)  // 方向变化或达到极限
                done = true;  // 标记阶段完成

            if(wuChaCnt == 1)  // 状态1：大幅度调整第一阶段
            {
                if(done)  // 如果检测到方向变化
                {
                    wuChaCnt = 2;  // 进入状态2
                    lastCoef = lastSet;  // 记录当前设置值
                    //mPrintf("lastCoef=%d\n",lastCoef);
                    direct = direct1;  // 更新方向
                }
            }
            else if(done)// 注意是else if，目前是direct需要翻转两次，1次有可能跨度太大
            {
                // 从上次位置开始调节
                value = lastCoef;  // 回退到上次记录的位置
                // 次数调节到direct为止
                direct = 3+Math.abs(value-lastSet);  // 计算小幅度调整次数
                direct += 3;  // 至少再来3次
                wuChaCnt = 3;  // 进入状态3（小幅度调整）
                Log.i(TAG,"need count "+(direct-2)+" chi to finised");  // 输出预计调整次数
            }
        }
        else// 小幅度调整期：状态3及以上
        {
            int k=volStep < 0 ? -1 : 1;  // 确定调整方向系数
            if(nowVol > stdVol) value -= k;  // 实际值偏大，减小设置值
            else value += k;  // 实际值偏小，增大设置值
            if(value > limt_max)  // 检查上限
                value = limt_max;  // 限制到最大值
            else if(value < limt_min)  // 检查下限
                value = limt_min;  // 限制到最小值
            if(++wuChaCnt > direct)  // 达到预定调整次数
            {// 完成调节
                if(!finished)  // 如果尚未标记完成
                {
                    finished = true;  // 标记校准完成
                    Log.i(TAG, "\t==== Hit the best vol: "+bestVol+"---"+bakCoef+" ====");  // 输出最佳值日志
                }
            }
        }

        // mprintf("value=%d\n", value);
        return value;  // 返回计算出的新设置值
    }

    // ==================== 参数设置方法 ====================
    
    /**
     * 设置步进值
     * 
     * @param _volStep 步进值，每个单位对应的电压变化量
     */
    void setVolStep(float _volStep){volStep = _volStep;}
    
    /**
     * 设置为系数校准模式
     */
    void setZero_or_coef(){zero_or_coef = false;}
    
    /**
     * 设置校准类型
     * 
     * @param _zero_or_coef true=零点校准，false=系数校准
     */
    void setZero_or_coef(boolean _zero_or_coef){zero_or_coef = _zero_or_coef;}
    
    /**
     * 设置标准值
     * 
     * @param _stdVol 标准值，校准的目标值
     */
    void setStdVol(float _stdVol) {stdVol = _stdVol;}
    
    /**
     * 设置设置值的范围限制
     * 
     * @param min 设置值最小值
     * @param max 设置值最大值
     */
    void setLimtVol(int min, int max){limt_min=min;limt_max=max;}

    // ==================== 结果获取方法 ====================
    
    /**
     * 检查校准是否完成
     * 
     * @return true表示校准已完成，false表示仍在进行
     */
    boolean isFinished(){return finished;}
    
    /**
     * 获取最佳设置值
     * 
     * <p>返回误差最小时的设置值。
     * 
     * @return 最佳设置值
     */
    int getBestSetVol(){return bakCoef;}
    
    /**
     * 获取最佳误差值
     * 
     * <p>返回最小误差值（nowVol - stdVol）。
     * 
     * @return 最佳误差值
     */
    float getBestCalVol(){return bestVol;}
    
    /**
     * 获取最佳值记录
     * 
     * <p>返回最佳值时的标准值和实际测量值。
     * 
     * @return 数组，[0]=标准值，[1]=实际测量值
     */
    float[] getBestCalVol_rcd(){return bestVol_rcd;}
}
