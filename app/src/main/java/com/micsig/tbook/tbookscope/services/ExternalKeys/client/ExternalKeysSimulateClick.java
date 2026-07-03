package com.micsig.tbook.tbookscope.services.ExternalKeys.client; // 外部按键客户端模块包

import android.app.Instrumentation; // 导入Instrumentation，用于注入输入事件
import android.os.SystemClock; // 导入系统时钟，用于获取启动时间
import android.view.MotionEvent; // 导入运动事件，用于模拟触摸
import android.view.View; // 导入视图，用于分发触摸事件

import java.io.IOException; // 导入IO异常

/**
 * Created by liwb on 2017/12/6.
 * 测试模拟点击
 */

/**
 * +----------------------------------------------------------------------+
 * |                       外部按键模拟点击工具类                           |
 * +----------------------------------------------------------------------+
 * | 模块定位：ExternalKeys模块客户端侧，提供模拟触摸事件的能力            |
 * |           （点击、拖拽），用于物理按键触发UI操作                       |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 模拟点击操作（通过shell命令或Instrumentation注入）               |
 * |   2. 模拟拖拽操作（按下→移动→抬起）                                   |
 * |   3. 模拟仅抬起操作（拖拽释放）                                       |
 * |   4. 提供模拟点击状态标志，供外部判断当前是否为模拟事件               |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   采用Runnable内部类封装不同类型的触摸事件序列                        |
 * |   通过Instrumentation.sendPointerSync()注入系统级触摸事件             |
 * |   每种操作在独立线程中执行，避免阻塞UI线程                            |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   外部按键输入 → 本类模拟触摸事件 → 系统InputDispatcher → 目标View    |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - Instrumentation  (系统事件注入)                                    |
 * |   - MotionEvent      (触摸事件构造)                                    |
 * |   - INodeControlOperation (点击后回调接口)                             |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   物理按键/旋钮按下后，需要模拟触摸点击或拖拽UI控件时使用             |
 * +----------------------------------------------------------------------+
 */
public class ExternalKeysSimulateClick {
    private boolean imitateClick = false; // 模拟点击标志位，标记当前是否为模拟事件

    /**
     * 设置模拟点击标志
     *
     * @param imitateClick true=当前为模拟点击事件，false=非模拟事件
     */
    public void setImitateClick(boolean imitateClick) {
        this.imitateClick = imitateClick; // 设置模拟点击标志
    }

    /**
     * 获取模拟点击标志
     *
     * @return true=当前为模拟点击事件，false=非模拟事件
     */
    public boolean isImitateClick() {
        return imitateClick; // 返回模拟点击标志
    }

    /**
     * 通过shell命令模拟点击（指定固定坐标101,101）
     * 使用input tap命令执行点击，无需INJECT_EVENTS权限
     */
    public static void click() {
        int x = 101; // 点击X坐标
        int y = 101; // 点击Y坐标

        String[] order = { // 构建shell命令数组
                "input", // input命令
                "tap", // tap子命令（模拟点击）
                "" + x, // X坐标字符串
                "" + y  // Y坐标字符串
        };
        try {
            new ProcessBuilder(order).start(); // 执行shell命令模拟点击
        } catch (IOException e) { // 捕获IO异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }


    /**
     * 通过MotionEvent模拟点击指定视图上的坐标
     * 使用dispatchGenericMotionEvent分发触摸事件
     *
     * @param view 目标视图
     * @param x    点击X坐标
     * @param y    点击Y坐标
     */
    public static void click(View view, float x, float y) {

        long downTime = SystemClock.uptimeMillis(); // 获取按下时间戳

        //装疯
        MotionEvent downEvent = MotionEvent.obtain(downTime, downTime, // 构造按下事件
                MotionEvent.ACTION_DOWN, x, y, 0); // ACTION_DOWN，坐标(x,y)，无元数据
        downTime += 1000; // 按下时间加1000ms模拟按压时长

        //卖傻
        MotionEvent upEvent = MotionEvent.obtain(downTime, downTime, // 构造抬起事件
                MotionEvent.ACTION_UP, x, y, 0); // ACTION_UP，坐标(x,y)，无元数据

//        view.onTouchEvent(downEvent);
//        view.onTouchEvent(upEvent);
//        view.dispatchTouchEvent(downEvent);
//        view.dispatchTouchEvent(upEvent);
        view.dispatchGenericMotionEvent(downEvent); // 分发按下通用运动事件
        view.dispatchGenericMotionEvent(upEvent); // 分发抬起通用运动事件

        downEvent.recycle(); // 回收按下事件
        upEvent.recycle(); // 回收抬起事件
    }


    /**
     * 使用这个方法要加权限
     * <uses-permission android:name="android.permission.INJECT_EVENTS"/>
     */
    Instrumentation inst = new Instrumentation(); // Instrumentation实例，用于注入系统输入事件

    /**
     * 模拟点击（使用Instrumentation注入，需要INJECT_EVENTS权限）
     * 在新线程中执行点击，点击完成后回调INodeControlOperation
     *
     * @param x         点击X坐标
     * @param y         点击Y坐标
     * @param operation 点击后的回调操作，可为null
     */
    public void click(float x, float y, INodeControlOperation operation) {
        RClick r = new RClick(x, y, operation); // 创建点击Runnable
        Thread thread = new Thread(r); // 创建新线程
        thread.start(); // 启动线程执行点击
    }

    /**
     * 模拟拖拽操作（按下→移动→抬起）
     * 在新线程中执行完整拖拽事件序列
     *
     * @param x  起始X坐标
     * @param y  起始Y坐标
     * @param x1 终止X坐标
     * @param y1 终止Y坐标
     */
    public void drag(float x, float y, float x1, float y1) {
        RDrag rd = new RDrag(x, y, x1, y1); // 创建拖拽Runnable
        Thread thread = new Thread(rd); // 创建新线程
        thread.start(); // 启动线程执行拖拽
    }

    /**
     * 模拟拖拽操作（按下→移动→抬起），使用metaState=1
     * 与drag的区别在于metaState参数不同
     *
     * @param x  起始X坐标
     * @param y  起始Y坐标
     * @param x1 终止X坐标
     * @param y1 终止Y坐标
     */
    public void drag_downMove(float x, float y, float x1, float y1) {
        RDrag_DownMove rd = new RDrag_DownMove(x, y, x1, y1); // 创建按下移动Runnable
        Thread thread = new Thread(rd); // 创建新线程
        thread.start(); // 启动线程执行拖拽
    }

    /**
     * 模拟拖拽释放操作（仅抬起事件）
     * 不发送按下和移动事件，仅发送ACTION_UP
     *
     * @param x  按下X坐标（未使用）
     * @param y  按下Y坐标（未使用）
     * @param x1 抬起X坐标
     * @param y1 抬起Y坐标
     */
    public void drag_up(float x, float y, float x1, float y1) {
        RDrag_Up rd = new RDrag_Up(x, y, x1, y1); // 创建抬起Runnable
        Thread thread = new Thread(rd); // 创建新线程
        thread.start(); // 启动线程执行抬起
    }

    /**
     * +----------------------------------------------------------------------+
     * | 拖拽Runnable：模拟完整拖拽手势（按下→移动→抬起）                      |
     * +----------------------------------------------------------------------+
     * | 使用Instrumentation.sendPointerSync注入触摸事件                       |
     * | metaState=0，表示无修饰键按下                                         |
     * +----------------------------------------------------------------------+
     */
    class RDrag implements Runnable {
        private float x, y; // 起始坐标
        private float x1, y1; // 终止坐标

        /**
         * 构造拖拽Runnable
         *
         * @param x  起始X坐标
         * @param y  起始Y坐标
         * @param x1 终止X坐标
         * @param y1 终止Y坐标
         */
        public RDrag(float x, float y, float x1, float y1) {
            this.x = x; // 保存起始X
            this.y = y; // 保存起始Y
            this.x1 = x1; // 保存终止X
            this.y1 = y1; // 保存终止Y
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis(); // 获取按下时间戳
            long eventTime = SystemClock.uptimeMillis(); // 获取事件时间戳
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)); // 注入按下事件
            eventTime = SystemClock.uptimeMillis(); // 更新事件时间
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x1, y1, 0)); // 注入移动事件
            eventTime = SystemClock.uptimeMillis(); // 更新事件时间
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 0)); // 注入抬起事件
        }
    }

    /**
     * +----------------------------------------------------------------------+
     * | 按下移动Runnable：模拟拖拽手势（按下→移动→抬起）                      |
     * +----------------------------------------------------------------------+
     * | 与RDrag的区别在于metaState=1，表示有修饰键状态                        |
     * +----------------------------------------------------------------------+
     */
    class RDrag_DownMove implements Runnable {
        private float x, y; // 起始坐标
        private float x1, y1; // 终止坐标

        /**
         * 构造按下移动Runnable
         *
         * @param x  起始X坐标
         * @param y  起始Y坐标
         * @param x1 终止X坐标
         * @param y1 终止Y坐标
         */
        public RDrag_DownMove(float x, float y, float x1, float y1) {
            this.x = x; // 保存起始X
            this.y = y; // 保存起始Y
            this.x1 = x1; // 保存终止X
            this.y1 = y1; // 保存终止Y
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis(); // 获取按下时间戳
            long eventTime = SystemClock.uptimeMillis(); // 获取事件时间戳
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 1)); // 注入按下事件(metaState=1)
            eventTime = SystemClock.uptimeMillis(); // 更新事件时间
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x1, y1, 1)); // 注入移动事件(metaState=1)
            eventTime = SystemClock.uptimeMillis(); // 更新事件时间
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 1)); // 注入抬起事件(metaState=1)
        }
    }

    /**
     * +----------------------------------------------------------------------+
     * | 拖拽抬起Runnable：仅模拟抬起事件                                      |
     * +----------------------------------------------------------------------+
     * | 只发送ACTION_UP事件，用于配合之前的按下操作完成拖拽释放               |
     * | metaState=0，表示无修饰键按下                                         |
     * +----------------------------------------------------------------------+
     */
    class RDrag_Up implements Runnable {
        private float x, y; // 按下坐标（未使用）
        private float x1, y1; // 抬起坐标

        /**
         * 构造抬起Runnable
         *
         * @param x  按下X坐标（未使用）
         * @param y  按下Y坐标（未使用）
         * @param x1 抬起X坐标
         * @param y1 抬起Y坐标
         */
        public RDrag_Up(float x, float y, float x1, float y1) {
            this.x = x; // 保存按下X（未使用）
            this.y = y; // 保存按下Y（未使用）
            this.x1 = x1; // 保存抬起X
            this.y1 = y1; // 保存抬起Y
        }

        @Override
        public void run() {
            long downTime = SystemClock.uptimeMillis(); // 获取按下时间戳
            long eventTime = SystemClock.uptimeMillis(); // 获取事件时间戳
//            inst.sendPointerSync(MotionEvent.obtain(downTime,eventTime, MotionEvent.ACTION_DOWN, x, y, 0));
//            eventTime = SystemClock.uptimeMillis();
//            inst.sendPointerSync(MotionEvent.obtain(downTime,eventTime, MotionEvent.ACTION_MOVE, x1, y1, 0));
//            eventTime = SystemClock.uptimeMillis();
            inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x1, y1, 0)); // 仅注入抬起事件
        }
    }


    /**
     * +----------------------------------------------------------------------+
     * | 点击Runnable：模拟点击操作（按下→抬起）                               |
     * +----------------------------------------------------------------------+
     * | 使用Instrumentation注入点击事件，metaState=1                          |
     * | 点击完成后回调INodeControlOperation接口                                |
     * | 通过imitateClick标志标记模拟点击状态                                   |
     * +----------------------------------------------------------------------+
     */
    class RClick implements Runnable {
        private float x, y; // 点击坐标
        private INodeControlOperation operation; // 点击后回调操作

        /**
         * 构造点击Runnable
         *
         * @param x         点击X坐标
         * @param y         点击Y坐标
         * @param operation 点击后的回调操作，可为null
         */
        public RClick(float x, float y, INodeControlOperation operation) {
            this.x = x; // 保存点击X
            this.y = y; // 保存点击Y
            this.operation = operation; // 保存回调操作
        }

        @Override
        public void run() {
            imitateClick = false; // 按下前标记为非模拟状态
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 1)); // 注入按下事件(metaState=1)
            imitateClick = true; // 标记为模拟点击中
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 1)); // 注入抬起事件(metaState=1)
            imitateClick = true; // 保持模拟点击标志
            if (operation != null) operation.simulateMotionEventActionUp(); // 执行回调操作
        }
    }


    /**
     * +----------------------------------------------------------------------+
     * | 节点控制操作回调接口                                                  |
     * +----------------------------------------------------------------------+
     * | 用于在模拟点击的ACTION_UP事件后执行额外的节点控制逻辑                  |
     * | 例如：更新节点选中状态、触发UI刷新等                                   |
     * +----------------------------------------------------------------------+
     */
    public interface INodeControlOperation {
        /**
         * 模拟运动事件ACTION_UP后的回调方法
         * 在模拟点击的抬起事件发送后调用
         */
        void simulateMotionEventActionUp();
    }
}
