package com.micsig.tbook.scope.surface;

import android.util.Log;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.channel.ChannelFactory;

import java.util.Arrays;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                SlideFinger - 手指滑动处理类                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的手指滑动处理类，实现示波器波形的触摸平移功能。                 ║
 * ║   管理多通道的水平和垂直偏移量，支持波形拖拽操作。                             ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 处理手指触摸事件（按下、移动、抬起）                                     ║
 * ║   2. 管理滑动方向（水平/垂直）                                               ║
 * ║   3. 维护各通道的偏移量                                                      ║
 * ║   4. 协调采样状态（暂停/恢复）                                               ║
 * ║   5. 发送波形偏移事件                                                        ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   单例模式（Singleton）：全局唯一实例，使用双重检查锁定实现线程安全           ║
 * ║                                                                              ║
 * ║ 【滑动方向定义】                                                             ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │  MOVE_LEFTRIGHT (1)  │  水平滑动，波形左右平移                      │    ║
 * ║   │  MOVE_UPDOWN (2)     │  垂直滑动，波形上下平移                      │    ║
 * ║   │  其他值              │  无滑动/滑动结束                            │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【触摸事件流程】                                                             ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ fingerDown  │───▶│ fingerMove  │───▶│  fingerUp   │                   ║
 * ║   │  (按下)     │    │  (移动)     │    │  (抬起)     │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║         │                  │                  │                            ║
 * ║         ▼                  ▼                  ▼                            ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 记录起点    │    │ 更新位置    │    │ 累加偏移量  │                   ║
 * ║   │ 发送选择事件│    │ 发送偏移事件│    │ 恢复采样    │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【偏移量管理】                                                               ║
 * ║   ┌───────────────────────────────────────────────────────────────────┐    ║
 * ║   │  通道索引        │  xOffset[]        │  yOffset[]                  │    ║
 * ║   │  CH1 (0)         │  通道1水平偏移    │  通道1垂直偏移              │    ║
 * ║   │  CH2 (1)         │  通道2水平偏移    │  通道2垂直偏移              │    ║
 * ║   │  ...             │  ...              │  ...                        │    ║
 * ║   │  MATH1 (N-2)     │  数学通道1偏移    │  数学通道1偏移              │    ║
 * ║   │  MATH2 (N-1)     │  数学通道2偏移    │  数学通道2偏移              │    ║
 * ║   └───────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 波形水平平移（时基偏移）                                                ║
 * ║   2. 波形垂直平移（通道偏移）                                                ║
 * ║   3. 数学通道平移                                                            ║
 * ║   4. 参考波形平移                                                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   使用synchronized关键字保护共享变量的访问。                                  ║
 * ║   单例实例使用双重检查锁定创建。                                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - ChannelFactory: 通道工厂，提供通道类型判断                               ║
 * ║   - EventFactory: 事件工厂，发送波形事件                                     ║
 * ║   - Sample: 采样控制，暂停/恢复采样                                          ║
 * ║   - ScopeMessage: 示波器消息，控制命令使能                                   ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SlideFinger {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private static final String TAG = "SlideFinger";

    /**
     * 滑动方向常量：左右滑动
     * 用于标识水平方向的波形平移操作
     */
    public final static int MOVE_LEFTRIGHT = 1;

    /**
     * 滑动方向常量：上下滑动
     * 用于标识垂直方向的波形平移操作
     */
    public final static int MOVE_UPDOWN = 2;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例实例
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 单例实例
     * 使用双重检查锁定保证线程安全的延迟初始化
     */
    private static SlideFinger instance;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取单例实例
     * 使用双重检查锁定（Double-Checked Locking）保证线程安全
     *
     * @return SlideFinger单例实例
     */
    public static SlideFinger getInstance(){
        synchronized (SlideFinger.class){                                           // 类级别同步锁
            if(instance == null){                                                   // 第一次检查：实例是否已创建
                instance = new SlideFinger();                                       // 创建单例实例
            }
        }
        return instance;                                                            // 返回单例实例
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 触摸位置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 手指按下时的X坐标
     * 用于计算滑动偏移量
     */
    private float downX, downY;

    /**
     * 当前手指位置坐标
     * 实时更新，用于计算当前偏移量
     */
    private float X, Y;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 偏移量数组
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 各通道水平偏移量数组
     * 数组大小：物理通道数 + 数学通道数
     * 存储每个通道累积的水平偏移量
     */
    private float []xOffset = new float[ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT];

    /**
     * 各通道垂直偏移量数组
     * 数组大小：物理通道数 + 数学通道数
     * 存储每个通道累积的垂直偏移量
     */
    private float []yOffset = new float[ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT];

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 状态
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 当前操作的通道索引
     * 默认值：CH1（通道1）
     * 取值范围：0 ~ (通道总数-1)
     */
    private int chIdx = ChannelFactory.CH1;

    /**
     * 滑动方向
     * 取值：MOVE_LEFTRIGHT(1)、MOVE_UPDOWN(2)、其他值(无滑动)
     * 默认值：-1（无滑动）
     */
    private int slideDirection = -1;

    /**
     * 备份的采样状态
     * 在滑动开始时保存，滑动结束时恢复
     */
    private int bakSampleState;

    /**
     * 移动标志
     * true: 手指正在移动
     * false: 手指未移动或已抬起
     */
    private boolean bMove = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 私有构造方法
     * 单例模式，外部不能直接实例化
     */
    private SlideFinger(){
        // 空构造方法
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 毫秒级休眠
     * 让当前线程休眠指定的毫秒数
     *
     * @param ms 休眠时间，单位：毫秒
     */
    public void ms_sleep(long ms){
        try {
            Thread.sleep(ms);                                                       // 调用线程休眠方法
        } catch (InterruptedException e) {                                          // 捕获中断异常
            e.printStackTrace();                                                    // 打印异常堆栈
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道类型判断方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 判断当前通道是否为数学通道
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 是数学通道
     *         false: 不是数学通道
     */
    public synchronized boolean isMathCh(){
        return ChannelFactory.isMathCh(chIdx);                                      // 调用ChannelFactory判断
    }

    /**
     * 判断当前通道是否为参考通道
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 是参考通道
     *         false: 不是参考通道
     */
    public synchronized boolean isRef() {
        return ChannelFactory.isRefCh(chIdx);                                       // 调用ChannelFactory判断
    }

    /**
     * 检查当前通道是否有效
     * 有效通道包括：动态通道、数学通道
     *
     * @return true: 有效通道
     *         false: 无效通道
     */
    private boolean isValid(){
        return ChannelFactory.isDynamicCh(chIdx) || ChannelFactory.isMathCh(chIdx); // 判断是否为动态通道或数学通道
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 滑动方向设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置滑动方向
     * 根据滑动方向执行不同的操作：
     * - 开始滑动：暂停采样，禁用命令
     * - 结束滑动：累加偏移量，恢复采样
     *
     * <p><b>状态转换：</b></p>
     * <pre>
     *   无滑动 → 开始滑动：备份采样状态，暂停采样
     *   滑动中 → 结束滑动：累加偏移量，恢复采样
     * </pre>
     *
     * @param slideDirection 滑动方向
     *                       MOVE_LEFTRIGHT: 左右滑动
     *                       MOVE_UPDOWN: 上下滑动
     *                       其他值: 结束滑动
     */
    public void setSlideDirection(int slideDirection){
        Sample sample  = Sample.getInstance();                                      // 获取采样单例
        ScopeMessage scopeMessage = ScopeMessage.getInstance();                    // 获取消息单例
        switch (slideDirection){
            case SlideFinger.MOVE_LEFTRIGHT:                                        // 左右滑动
            case SlideFinger.MOVE_UPDOWN:                                           // 上下滑动
                if(isValid()) {                                                     // 检查通道是否有效
                    bakSampleState = sample.getSampleState();                       // 备份当前采样状态
                    scopeMessage.enableCommand(false);                              // 禁用命令处理
                    scopeMessage.touchPause();                                      // 触摸暂停采样
                }
                break;
            default:                                                                // 结束滑动
                if(this.slideDirection == MOVE_LEFTRIGHT                            // 检查之前是否在滑动
                        || this.slideDirection == MOVE_UPDOWN) {
                    if(isValid()) {                                                 // 检查通道是否有效
                        synchronized (this) {                                       // 同步块，保护偏移量更新
                            xOffset[this.chIdx] += X - downX;                       // 累加水平偏移量
                            yOffset[this.chIdx] += Y - downY;                       // 累加垂直偏移量
                        }
                        if (bakSampleState != Sample.SAMPLE_STOP) {                 // 检查备份的采样状态
                            //sample.setSampleState(Sample.SAMPLE_TRANSIENT_RUN);   // [已注释] 设置瞬态运行
                            sample.frozenSample(bakSampleState);                    // 解冻采样，恢复之前的状态
                        }
                        scopeMessage.touchResume();                                 // 触摸恢复采样
                        scopeMessage.enableCommand(true);                           // 启用命令处理
                    }
                }
                this.chIdx = ChannelFactory.CH_CNT;                                 // 重置通道索引为无效值
                break;
        }
        this.slideDirection = slideDirection;                                       // 更新滑动方向
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 状态查询方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查是否正在滑动
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 正在滑动
     *         false: 未滑动
     */
    public synchronized boolean isSlide(){
        return isValid() && bMove && (slideDirection == MOVE_LEFTRIGHT || slideDirection == MOVE_UPDOWN); // 检查有效性和滑动状态
    }

    /**
     * 获取滑动方向
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return 滑动方向值
     */
    public synchronized int getSlideDirection(){
        return this.slideDirection;                                                 // 返回滑动方向
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道索引设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置当前操作的通道索引
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @param chIdx 通道索引
     *              取值范围：0 ~ (通道总数-1)
     */
    public synchronized void setChIdx(int chIdx){
        this.chIdx = chIdx;                                                         // 设置通道索引
    }

    /**
     * 获取当前操作的通道索引
     *
     * @return 通道索引
     */
    public int getChIdx(){
        return chIdx;                                                               // 返回通道索引
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 手指按下事件处理
     * 记录按下位置，发送波形标签选择事件
     *
     * @param x 按下位置的X坐标
     * @param y 按下位置的Y坐标
     */
    public void fingerDownXY(float x, float y){
        synchronized (this) {                                                       // 同步块，保护位置变量
            this.downX = x;                                                         // 记录按下时的X坐标
            this.downY = y;                                                         // 记录按下时的Y坐标
            this.X = x;                                                             // 初始化当前X坐标
            this.Y = y;                                                             // 初始化当前Y坐标
        }
        if (isValid()) {                                                            // 检查通道是否有效
            // 发送波形标签选择事件，参数为坐标字符串"x;y"
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_WAVE_LABEL_SELECT, (int) x + ";" + (int) y), true);
        }
    }

    /**
     * 手指移动事件处理
     * 更新当前位置，发送波形偏移事件
     *
     * @param x 移动位置的X坐标
     * @param y 移动位置的Y坐标
     */
    public void fingerMoveXY(float x, float y){
        synchronized (this) {                                                       // 同步块，保护状态变量
            bMove = true;                                                           // 设置移动标志为true
            this.X = x;                                                             // 更新当前X坐标
            this.Y = y;                                                             // 更新当前Y坐标
        }
        if(isValid()) {                                                             // 检查通道是否有效
            EventFactory.sendEvent(EventFactory.EVENT_WAVE_OFFSET);                 // 发送波形偏移事件
        }
    }

    /**
     * 手指移动标签事件处理
     * 发送波形标签移动事件
     *
     * @param x 移动位置的X坐标
     * @param y 移动位置的Y坐标
     */
    public void fingerMoveLabel(float x, float y) {
        // 发送波形标签移动事件，参数为坐标字符串"x;y"
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_WAVE_LABEL_MOVE, (int) x + ";" + (int) y), true);
    }

    /**
     * 手指抬起事件处理
     * 更新最终位置，重置移动标志
     *
     * @param x 抬起位置的X坐标
     * @param y 抬起位置的Y坐标
     */
    public void fingerUp(float x, float y){
        fingerMoveXY(x, y);                                                         // 更新最终位置
        synchronized (this) {                                                       // 同步块，保护状态变量
            bMove = false;                                                          // 重置移动标志为false
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 偏移量获取方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取水平偏移量
     * 返回当前滑动的水平偏移量（包含累积偏移）
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return 水平偏移量（像素）
     *         如果通道有效，返回：当前偏移 + 累积偏移
     *         如果通道无效，返回：当前偏移
     */
    public synchronized float getHorizontalOffset(){
        if(isValid()) {                                                             // 检查通道是否有效
            return X - downX + xOffset[this.chIdx];                                 // 返回当前偏移 + 累积偏移
        }else{
            return X - downX;                                                       // 仅返回当前偏移
        }
    }

    /**
     * 获取垂直偏移量
     * 返回当前滑动的垂直偏移量（包含累积偏移）
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return 垂直偏移量（像素）
     *         如果通道有效，返回：当前偏移 + 累积偏移
     *         如果通道无效，返回：当前偏移
     */
    public synchronized float getVerticalOffset(){
        if(isValid()) {                                                             // 检查通道是否有效
            return Y - downY + yOffset[this.chIdx];                                 // 返回当前偏移 + 累积偏移
        }else{
            return Y - downY;                                                       // 仅返回当前偏移
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 重置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 重置所有偏移量
     * 将所有通道的水平偏移量和垂直偏移量清零
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     */
    public synchronized void reset(){
        Arrays.fill(xOffset, 0);                                                    // 将水平偏移数组全部填充为0
        Arrays.fill(yOffset, 0);                                                    // 将垂直偏移数组全部填充为0
    }

}
