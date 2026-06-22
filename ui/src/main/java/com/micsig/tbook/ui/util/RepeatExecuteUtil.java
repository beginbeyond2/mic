package com.micsig.tbook.ui.util;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时执行工具类
 * 
 * <p>提供基于定时线程池的重复执行任务功能，主要用于长按按钮时的加速调节。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>定时执行：以固定延迟周期性执行任务</li>
 *   <li>加速调节：执行次数越多，步进值越大</li>
 *   <li>可控启停：支持启动和停止定时任务</li>
 * </ul>
 * 
 * <p>加速调节策略：</p>
 * <ul>
 *   <li>前10次：步进值为1（慢速调节）</li>
 *   <li>11-20次：步进值为3（中速调节）</li>
 *   <li>20次以后：步进值为5（快速调节）</li>
 * </ul>
 * 
 * <p>使用场景：示波器参数调节按钮的长按加速功能。</p>
 * 
 * @author Liwb
 * @version 1.0
 * @since 2022-4-22
 */
public class RepeatExecuteUtil {

    /** 定时执行服务，用于周期性执行任务 */
    private static ScheduledExecutorService scheduledExecutorService;
    
    /** 执行状态标志，记录是否至少执行过一次任务 */
    private static boolean bUpdateAction = false;
    
    /**
     * 启动定时执行任务（用于加减调节）
     * 
     * <p>创建单线程定时执行器，周期性发送调节消息到Handler。</p>
     * <p>如果已有任务在执行，会先停止之前的任务。</p>
     * <p>初始延迟1000ms后开始执行，之后每20ms执行一次。</p>
     * 
     * <p>加速策略：</p>
     * <ul>
     *   <li>前10次执行：步进值=1</li>
     *   <li>第11-20次执行：步进值=3</li>
     *   <li>第20次以后执行：步进值=5</li>
     * </ul>
     *
     * @param viewId  视图ID，用作消息的what字段，用于区分不同的调节目标
     * @param handler 消息处理器，接收调节消息并执行实际操作
     * 
     * @see #stopAddOrSubtract() 停止定时执行
     */
    public static void updateAddOrSubtract(int viewId, final Handler handler) {
        // 先停止之前的任务（如果有）
        stopAddOrSubtract();
        
        // 重置执行状态标志
        bUpdateAction = false;
        
        // 保存视图ID（用于消息标识）
        final int vid = viewId;
        
        // 执行次数计数器（使用数组以便在匿名类中修改）
        final int[] repeat = {0};
        
        // 创建单线程定时执行器
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        
        // 启动定时任务：初始延迟1000ms，之后每20ms执行一次
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // 标记已执行过任务
                bUpdateAction = true;

                // 增加执行次数
                repeat[0]++;
                
                // 根据执行次数计算步进值（加速调节）
                int offset = 0;
                if (repeat[0] <= 10) {
                    // 前10次：慢速调节，步进值为1
                    offset = 1;
                } else if (repeat[0] <= 20) {
                    // 第11-20次：中速调节，步进值为3
                    offset = 3;
                } else {
                    // 第20次以后：快速调节，步进值为5
                    offset = 5;
                }
                
                // 创建并发送消息到Handler
                Message msg = Message.obtain();
                msg.what = vid;      // 设置消息类型（视图ID）
                msg.obj = offset;    // 设置步进值
                handler.sendMessage(msg);
            }
        }, 1000, 20, TimeUnit.MILLISECONDS); // 初始延迟1000ms，周期20ms

    }

    /**
     * 停止定时执行任务
     * 
     * <p>立即关闭定时执行器，停止所有正在执行的任务。</p>
     * <p>使用shutdownNow()确保立即停止，不会等待正在执行的任务完成。</p>
     *
     * @return 返回是否至少执行过一次任务
     *         <ul>
     *           <li>true：至少执行过一次任务</li>
     *           <li>false：从未执行过任务（可能是初始延迟期间就被停止）</li>
     *         </ul>
     */
    public static boolean stopAddOrSubtract() {
        // 检查定时执行器是否存在
        if (scheduledExecutorService != null) {
            // 立即关闭定时执行器
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
        // 返回执行状态
        return bUpdateAction;
    }

}
