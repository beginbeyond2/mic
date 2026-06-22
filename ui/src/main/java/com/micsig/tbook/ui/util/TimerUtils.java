package com.micsig.tbook.ui.util;

/**
 * 定时器工具类
 * 
 * <p>提供类似C#中Timer的定时执行功能，支持单次执行和循环执行。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>定时执行：按指定间隔时间执行回调</li>
 *   <li>单次/循环：支持单次执行后自动停止或循环执行</li>
 *   <li>可控启停：支持启动和停止定时器</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * TimerUtils timer = new TimerUtils(new TimerUtils.TimeOutEvent() {
 *     @Override
 *     public void onTimeOut() {
 *         // 定时执行的代码
 *     }
 * });
 * 
 * timer.setIntervalMs(1000); // 设置间隔1秒
 * timer.setExecOne(false);   // 设置循环执行
 * timer.start();             // 启动定时器
 * 
 * // 停止定时器
 * timer.stop();
 * }</pre>
 * 
 * @author Liwb
 * @version 1.0
 * @since 2022-2-23
 */
public class TimerUtils {
    
    /** 日志标签 */
    private static final String TAG = TimerUtils.class.getSimpleName();

    /**
     * 超时事件回调接口
     * 
     * <p>定义定时器超时时的回调方法。</p>
     */
    public interface TimeOutEvent {
        
        /**
         * 超时回调方法
         * 
         * <p>当定时器达到指定间隔时间时调用此方法。</p>
         */
        public void onTimeOut();
    }

    /** 定时器工作线程 */
    private Thread thread;
    
    /** 开始时间戳（毫秒），使用volatile保证多线程可见性 */
    private volatile long beginTime;
    
    /** 定时器运行状态标志 */
    private boolean isRun = false;

    //region 属性
    
    /** 定时间隔时间（毫秒），默认1000ms */
    private int intervalMs = 1000;
    
    /** 是否只执行一次，true表示执行一次后停止，false表示循环执行 */
    private boolean execOne = true;
    
    /** 超时事件回调 */
    private TimeOutEvent event = null;

    /**
     * 获取定时间隔时间
     *
     * @return 定时间隔时间（毫秒）
     */
    public int getIntervalMs() {
        return intervalMs;
    }

    /**
     * 设置定时间隔时间
     * 
     * <p>设置定时器触发的时间间隔。</p>
     *
     * @param intervalMs 间隔时间（毫秒）
     */
    public void setIntervalMs(int intervalMs) {
        this.intervalMs = intervalMs;
    }

    /**
     * 获取是否只执行一次
     *
     * @return true表示只执行一次，false表示循环执行
     */
    public boolean isExecOne() {
        return execOne;
    }

    /**
     * 设置是否只执行一次
     * 
     * <p>设置定时器的执行模式：</p>
     * <ul>
     *   <li>true：执行一次后自动停止</li>
     *   <li>false：循环执行直到调用stop()</li>
     * </ul>
     *
     * @param execOne 是否只执行一次
     */
    public void setExecOne(boolean execOne) {
        this.execOne = execOne;
    }

    /**
     * 获取超时事件回调
     *
     * @return 超时事件回调接口
     */
    public TimeOutEvent getEvent() {
        return event;
    }

    /**
     * 设置超时事件回调
     * 
     * <p>设置定时器超时时要执行的事件回调。</p>
     *
     * @param event 超时事件回调接口
     */
    public void setEvent(TimeOutEvent event) {
        this.event = event;
    }
    //endregion

    /**
     * 构造函数
     * 
     * <p>创建定时器实例并设置超时事件回调。</p>
     *
     * @param event 超时事件回调接口
     */
    public TimerUtils(TimeOutEvent event) {
        this.event = event;
    }

    /**
     * 定时器工作线程任务
     * 
     * <p>内部类，实现定时检查和回调执行的逻辑。</p>
     */
    class DoWork implements Runnable {
        @Override
        public void run() {
            try {
                // 循环检查是否到达定时时间
                while (!thread.isInterrupted()) {
                    // 获取当前时间
                    long curTime = System.currentTimeMillis();
                    
                    // 检查是否到达间隔时间
                    if (curTime - beginTime >= intervalMs) {
                        // 更新开始时间
                        beginTime = curTime;
                        
                        // 执行超时回调
                        if (event != null) {
                            event.onTimeOut();
                        }
                        
                        // 如果只执行一次，退出循环
                        if (execOne == true) {
                            return;
                        }
                    }

                    // 休眠10ms后再次检查
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                // 发生异常时退出
                return;
            }
        }
    }

    /**
     * 启动定时器
     * 
     * <p>启动定时器，开始计时。如果线程不存在或已停止，则创建新线程。</p>
     * <p>定时器启动后会按照设定的间隔时间触发超时事件。</p>
     */
    public void start() {
        try {
            // 设置运行状态
            isRun = true;
            
            // 记录开始时间
            beginTime = System.currentTimeMillis();
            
            // 检查线程是否存在且存活
            if (thread == null || thread.isAlive() == false) {
                // 创建新线程
                thread = new Thread(new DoWork());
            }

            // 启动线程
            thread.start();
        } catch (Exception e) {
            // 发生异常时直接返回
            return;
        }
    }

    /**
     * 停止定时器
     * 
     * <p>停止定时器，中断工作线程并等待线程结束。</p>
     * <p>调用此方法后，定时器将不再触发超时事件。</p>
     */
    public void stop() {
        // 设置运行状态为false
        isRun = false;
        
        // 检查线程是否存在
        if (thread != null) {
            // 中断线程
            thread.interrupt();
            try {
                // 等待线程结束
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // 清空线程引用
        thread = null;
    }

}
