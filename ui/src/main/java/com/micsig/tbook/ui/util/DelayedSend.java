package com.micsig.tbook.ui.util;

/**
 * 延迟发送工具类
 * 
 * <p>提供线程延迟执行机制，用于在指定延迟后执行任务。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>延迟执行：调用delayedSend()后，在下一个检查周期执行回调</li>
 *   <li>防抖动：连续多次调用delayedSend()只会触发一次回调执行</li>
 *   <li>可控周期：支持自定义检查周期（默认100ms）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * DelayedSend delayedSend = new DelayedSend(50, new DelayedSend.DelayedSendTo() {
 *     @Override
 *     public void delayedSendMessage() {
 *         // 延迟执行的代码（运行在非UI线程）
 *     }
 * });
 * 
 * // 触发延迟执行
 * delayedSend.delayedSend();
 * 
 * // 关闭线程
 * delayedSend.closeThread();
 * }</pre>
 * 
 * <p>注意：回调方法运行在非UI线程，如需更新UI请使用Handler或runOnUiThread。</p>
 * 
 * @author yangj
 * @version 1.0
 * @since 2017/9/13
 */
public class DelayedSend {
    
    /** 循环控制标志，为false时线程退出 */
    private boolean loop = true;
    
    /** 发送请求标志，为true时触发回调执行 */
    private boolean send = false;
    
    /** 延迟发送线程 */
    private Thread thread;

    /**
     * 构造函数（使用默认延迟100ms）
     * 
     * <p>创建延迟发送实例，检查周期为100毫秒。</p>
     *
     * @param delayedSendTo 延迟发送回调接口，在延迟后执行
     */
    public DelayedSend(final DelayedSendTo delayedSendTo) {
        this(100, delayedSendTo);
    }

    /**
     * 构造函数（使用自定义延迟）
     * 
     * <p>创建延迟发送实例，使用指定的检查周期。</p>
     * <p>构造时即启动后台线程，线程会持续运行直到调用closeThread()。</p>
     *
     * @param delay         检查周期（毫秒），值越小响应越快但CPU占用越高
     * @param delayedSendTo 延迟发送回调接口，在延迟后执行
     */
    public DelayedSend(final int delay, final DelayedSendTo delayedSendTo) {
        // 创建后台线程
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 设置线程名称，便于调试和日志追踪
                Thread.currentThread().setName("DelaySend");
                
                // 循环检查发送请求
                while (loop) {
                    // 检查是否有发送请求
                    if (send) {
                        // 清除发送标志（防止重复执行）
                        send = false;
                        // 执行回调方法
                        delayedSendTo.delayedSendMessage();
                    }
                    
                    // 线程休眠指定的延迟时间
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        // 线程被中断时打印堆栈跟踪
                        e.printStackTrace();
                    }
                }
            }
        });
        
        // 启动线程
        thread.start();
    }

    /**
     * 触发延迟发送
     * 
     * <p>设置发送标志，在下一个检查周期执行回调。</p>
     * <p>此方法是线程安全的，可以从任何线程调用。</p>
     * <p>连续多次调用此方法只会触发一次回调执行（防抖动效果）。</p>
     */
    public void delayedSend() {
        // 设置发送标志，触发回调执行
        send = true;
    }

    /**
     * 关闭延迟发送线程
     * 
     * <p>设置循环标志为false，使线程在下一个检查周期退出。</p>
     * <p>调用此方法后，实例将不再可用，如需重新使用请创建新实例。</p>
     */
    public void closeThread() {
        // 设置循环标志为false，线程将在下一个周期退出
        loop = false;
    }

    /**
     * 延迟发送回调接口
     * 
     * <p>定义延迟执行的回调方法。</p>
     * <p>注意：回调方法运行在非UI线程（DelaySend线程）。</p>
     */
    public interface DelayedSendTo {
        
        /**
         * 延迟发送消息回调方法
         * 
         * <p>此方法在DelaySend线程中执行，不是UI线程。</p>
         * <p>如需更新UI，请使用Handler、runOnUiThread或其他线程切换机制。</p>
         */
        void delayedSendMessage();
    }
}
