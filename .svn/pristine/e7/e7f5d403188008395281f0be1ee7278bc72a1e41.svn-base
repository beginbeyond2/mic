package com.micsig.tbook.ui.util;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @auother Liwb
 * @description:    定时线程池执行任务
 * @data:2022-4-22 16:32
 */
public class RepeatExecuteUtil {

    private static ScheduledExecutorService scheduledExecutorService;
    private static boolean bUpdateAction=false;
    public static void updateAddOrSubtract(int viewId,final Handler handler){
        stopAddOrSubtract();
        bUpdateAction=false;
        final int vid=viewId;
        final int[] repeat = {0};
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                bUpdateAction=true;

                repeat[0]++;
                int offset=0;
                if (repeat[0]<=10){
                    offset=1;
                }else if (repeat[0]<=20){
                    offset=3;
                }else {
                    offset=5;
                }
                Message msg=Message.obtain();
                msg.what=vid;
                msg.obj=offset;
                handler.sendMessage(msg);
            }
        },1000,20, TimeUnit.MILLISECONDS);

    }

    /**
     * 停止执行
     * @return 返回是否执行过一次
     */
    public static boolean stopAddOrSubtract(){
        if (scheduledExecutorService!=null){
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService=null;
        }
        return bUpdateAction;
    }

}
