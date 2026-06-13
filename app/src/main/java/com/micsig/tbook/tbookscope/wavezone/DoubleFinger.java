package com.micsig.tbook.tbookscope.wavezone; // 波形显示区域包，包含示波器波形显示的核心组件，1

import android.util.Log; // Android日志工具类，用于记录调试信息，1
import android.view.MotionEvent; // Android触摸事件类，封装触摸屏的交互数据，1

/**
 * 双指手势处理器 - 示波器波形显示区域的双指交互核心组件
 * 
 * 【模块定位】
 * - 所属模块：wavezone（波形显示区域模块）
 * - 核心职责：识别和处理双指触摸手势，包括滑动、缩放等操作
 * - 架构层级：手势识别层，位于触摸事件和业务逻辑之间
 * 
 * 【核心职责】
 * 1. 双指手势识别：识别水平滑动、垂直滑动、水平缩放、垂直缩放四种手势
 * 2. 手势方向判定：根据触摸点的移动轨迹判定手势的具体方向和类型
 * 3. 手势回调通知：通过监听器接口将识别结果通知给上层业务组件
 * 4. 阈值过滤：使用触摸阈值过滤微小抖动，提高手势识别的准确性
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    WaveZoneDisplay_YT                        │
 * │                   (波形显示区域主类)                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建实例
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    DoubleFinger                              │
 * │                   (双指手势处理器)                            │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  手势识别引擎                                         │  │
 * │  │  - 水平滑动识别                                       │  │
 * │  │  - 垂直滑动识别                                       │  │
 * │  │  - 水平缩放识别                                       │  │
 * │  │  - 垂直缩放识别                                       │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 回调通知
 * ┌─────────────────────────────────────────────────────────────┐
 * │              DoubleFingerlistener接口                        │
 * │                   (手势监听器)                               │
 * │  - onHorizontalSlide()  水平滑动回调                        │
 * │  - onHorizontalZoom()   水平缩放回调                        │
 * │  - onVerticalSlide()    垂直滑动回调                        │
 * │  - onVerticalZoom()     垂直缩放回调                        │
 * │  - onBegin()            手势开始回调                        │
 * │  - onEnd()              手势结束回调                        │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 业务响应
 * ┌─────────────────────────────────────────────────────────────┐
 * │              WaveZoneDisplay_YT业务逻辑                      │
 * │  - 触发时基移动                                             │
 * │  - 波形垂直位移                                             │
 * │  - 电压刻度缩放                                             │
 * │  - 光标位置调整                                             │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 上层依赖：WaveZoneDisplay_YT（YT模式波形显示类）
 * - 下层依赖：MotionEvent（Android触摸事件类）
 * - 接口依赖：DoubleFingerlistener（手势监听器接口）
 * - 无外部库依赖
 * 
 * 【使用场景】
 * 1. YT模式波形显示：用户双指操作调整波形位置和缩放
 * 2. 触发时基调节：双指水平滑动调整触发时间位置
 * 3. 电压刻度缩放：双指垂直缩放调整电压刻度
 * 4. 波形垂直位移：双指垂直滑动调整波形垂直位置
 * 5. 光标联动操作：双指操作调整光标位置
 * 
 * 【关键算法】
 * 1. 手势方向判定算法：
 *    - 记录两个触摸点的初始位置和移动位置
 *    - 计算移动距离和方向向量
 *    - 根据移动距离超过阈值判定手势类型
 *    - 同向移动判定为滑动，反向移动判定为缩放
 * 
 * 2. 阈值过滤算法：
 *    - 使用TOUCH_THRESHOLD（15像素）作为最小移动阈值
 *    - 只有超过阈值的移动才触发手势回调
 *    - 防止微小抖动误触发手势
 * 
 * 【数据流向】
 * MotionEvent → onDoubleFinger() → 手势识别 → DoubleFingerlistener回调 → 业务逻辑
 * 
 * 【性能考虑】
 * - 手势识别实时性：每次触摸移动事件都会触发识别
 * - 回调频率控制：使用阈值过滤减少回调频率
 * - 内存占用：固定大小的数组存储触摸点数据
 * 
 * 【注意事项】
 * 1. 必须在触摸事件处理流程中调用onDoubleFinger方法
 * 2. 监听器回调在触摸事件线程中执行，避免耗时操作
 * 3. 手势识别状态在ACTION_DOWN时重置
 * 4. 双指手势识别需要两个触摸点同时存在
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/6/9
 * @see WaveZoneDisplay_YT
 * @see MotionEvent
 * @see DoubleFingerlistener
 */
public class DoubleFinger {

    /**
     * 双指手势监听器接口 - 定义双指手势的回调方法
     * 
     * 【接口定位】
     * - 功能定位：手势事件回调接口
     * - 使用场景：上层组件实现此接口接收手势通知
     * 
     * 【回调方法】
     * - onHorizontalSlide：水平滑动回调
     * - onHorizontalZoom：水平缩放回调
     * - onVerticalSlide：垂直滑动回调
     * - onVerticalZoom：垂直缩放回调
     * - onBegin：手势开始回调
     * - onEnd：手势结束回调
     */
    public interface DoubleFingerlistener{
        /**
         * 水平滑动回调方法
         * @param val 滑动的数值增量，正数向右滑动，负数向左滑动，单位为阈值倍数
         */
        public void onHorizontalSlide(double val); // 水平滑动回调，参数为滑动增量值，1
        
        /**
         * 水平缩放回调方法
         * @param y 缩放中心的垂直坐标，用于确定缩放参考点
         * @param v 缩放比例，当前两点距离与初始两点距离的比值
         */
        public void onHorizontalZoom(double y,double v); // 水平缩放回调，参数为缩放中心和比例，1
        
        /**
         * 垂直滑动回调方法
         * @param val 滑动的数值增量，正数向上滑动，负数向下滑动，单位为阈值倍数
         */
        public void onVerticalSlide(double val); // 垂直滑动回调，参数为滑动增量值，1
        
        /**
         * 垂直缩放回调方法
         * @param y 缩放中心的垂直坐标，用于确定缩放参考点
         * @param v 缩放比例，当前两点距离与初始两点距离的比值
         */
        public void onVerticalZoom(double y,double v); // 垂直缩放回调，参数为缩放中心和比例，1
        
        /**
         * 手势开始回调方法
         * 在双指按下时触发，通知上层组件手势操作即将开始
         */
        public void onBegin(); // 手势开始回调，在双指按下时触发，1
        
        /**
         * 手势结束回调方法
         * 在双指抬起时触发，通知上层组件手势操作已经结束
         */
        public void onEnd(); // 手势结束回调，在双指抬起时触发，1
    }

    /**
     * 双指按下时的X坐标数组，存储两个触摸点的初始X坐标
     * 数组索引0对应第一个触摸点，索引1对应第二个触摸点
     * 取值范围：屏幕坐标范围内的浮点数
     */
    private float [] fine_down_x = new float[2]; // 双指按下时的X坐标数组，长度为2，1
    
    /**
     * 双指按下时的Y坐标数组，存储两个触摸点的初始Y坐标
     * 数组索引0对应第一个触摸点，索引1对应第二个触摸点
     * 取值范围：屏幕坐标范围内的浮点数
     */
    private float [] fine_down_y = new float[2]; // 双指按下时的Y坐标数组，长度为2，1

    /**
     * 双指移动过程中的X坐标数组，存储两个触摸点的当前X坐标
     * 用于计算滑动增量，数组索引0对应第一个触摸点，索引1对应第二个触摸点
     * 取值范围：屏幕坐标范围内的浮点数
     */
    private float [] fine_move_x = new float[2]; // 双指移动过程中的X坐标数组，长度为2，1
    
    /**
     * 双指移动过程中的Y坐标数组，存储两个触摸点的当前Y坐标
     * 用于计算滑动增量，数组索引0对应第一个触摸点，索引1对应第二个触摸点
     * 取值范围：屏幕坐标范围内的浮点数
     */
    private float [] fine_move_y = new float[2]; // 双指移动过程中的Y坐标数组，长度为2，1
    
    /**
     * 触摸移动阈值，用于过滤微小抖动
     * 只有移动距离超过此阈值才认为是有效的手势操作
     * 取值：15像素，可根据实际体验调整
     */
    private static int TOUCH_THRESHOLD = 15; // 触摸移动阈值，单位为像素，值为15，1
    
    /**
     * 水平滑动手势标识常量
     * 当识别为水平滑动手势时，fine_dir变量设置为此值
     * 取值：1
     */
    private static final int FINE_HORIZONTAL_SLIDE = 1; // 水平滑动手势标识，值为1，1
    
    /**
     * 水平缩放手势标识常量
     * 当识别为水平缩放手势时，fine_dir变量设置为此值
     * 取值：2
     */
    private static final int FINE_HORIZONTAL_ZOOM = 2; // 水平缩放手势标识，值为2，1
    
    /**
     * 垂直滑动手势标识常量
     * 当识别为垂直滑动手势时，fine_dir变量设置为此值
     * 取值：3
     */
    private static final int FINE_VERTICAL_SLIDE = 3; // 垂直滑动手势标识，值为3，1
    
    /**
     * 垂直缩放手势标识常量
     * 当识别为垂直缩放手势时，fine_dir变量设置为此值
     * 取值：4
     */
    private static final int FINE_VERTICAL_ZOOM = 4; // 垂直缩放手势标识，值为4，1
    
    /**
     * 无手势标识常量
     * 当未识别到任何手势时，fine_dir变量设置为此值
     * 取值：0
     */
    private static final int FINE_DIR_NONE = 0; // 无手势标识，值为0，1
    
    /**
     * 当前识别的手势方向标识
     * 取值范围：FINE_DIR_NONE、FINE_HORIZONTAL_SLIDE、FINE_HORIZONTAL_ZOOM、
     *          FINE_VERTICAL_SLIDE、FINE_VERTICAL_ZOOM
     * 初始值：FINE_DIR_NONE（无手势）
     */
    private int fine_dir = FINE_DIR_NONE; // 当前手势方向标识，初始值为无手势，1
    
    /**
     * 双指初始距离数组，存储按下时的两点距离
     * 数组索引0存储X方向的距离，索引1存储Y方向的距离
     * 用于缩放计算时作为基准距离
     * 取值范围：大于0的浮点数
     */
    private float [] L = {0,0}; // 双指初始距离数组，索引0为X方向距离，索引1为Y方向距离，1
    
    /**
     * 双指手势监听器实例引用
     * 用于回调通知上层组件手势识别结果
     * 可为null，表示无监听器注册
     */
    DoubleFingerlistener fingerlistener; // 双指手势监听器实例，用于回调通知，1
    
    /**
     * 构造方法 - 初始化双指手势处理器
     * 
     * 【功能说明】
     * 创建DoubleFinger实例并注册手势监听器
     * 
     * 【参数说明】
     * @param fingerlistener 双指手势监听器实例，用于接收手势回调通知
     *                       可为null，表示不接收回调
     * 
     * 【调用时机】
     * 在WaveZoneDisplay_YT等需要处理双指手势的组件初始化时调用
     * 
     * 【使用示例】
     * DoubleFinger doubleFinger = new DoubleFinger(new DoubleFingerlistener() {
     *     @Override
     *     public void onHorizontalSlide(double val) {
     *         // 处理水平滑动
     *     }
     *     // ... 其他回调方法
     * });
     */
    public DoubleFinger(DoubleFingerlistener fingerlistener){
        this.fingerlistener = fingerlistener; // 保存传入的监听器实例，1

    }

    /**
     * 双指手势处理方法 - 核心手势识别和处理逻辑
     * 
     * 【功能说明】
     * 处理触摸事件，识别双指手势类型并回调通知监听器
     * 
     * 【参数说明】
     * @param event 触摸事件对象，包含触摸点数量、坐标、动作类型等信息
     * 
     * 【返回值说明】
     * @return 返回true表示已处理双指手势，返回false表示未处理或不满足双指条件
     * 
     * 【调用时机】
     * 在WaveZoneDisplay_YT的onTouchEvent方法中调用，每次触摸事件都会触发
     * 
     * 【处理流程】
     * 1. ACTION_DOWN：重置手势状态
     * 2. ACTION_POINTER_DOWN：记录双指按下时的初始位置
     * 3. ACTION_MOVE：识别手势类型并计算增量，触发回调
     * 4. ACTION_POINTER_UP：触发手势结束回调
     * 
     * 【手势识别算法】
     * 1. 水平滑动判定：两点X方向同向移动且距离超过阈值
     * 2. 水平缩放判定：两点X方向反向移动或距离变化超过阈值
     * 3. 垂直滑动判定：两点Y方向同向移动且距离超过阈值
     * 4. 垂直缩放判定：两点Y方向反向移动或距离变化超过阈值
     * 
     * 【使用示例】
     * @Override
     * public boolean onTouchEvent(MotionEvent event) {
     *     if(doubleFinger.onDoubleFinger(event)) {
     *         return true; // 双指手势已处理
     *     }
     *     // 处理其他触摸事件
     * }
     */
    public boolean onDoubleFinger(MotionEvent event){

        int pointCount = event.getPointerCount(); // 获取当前触摸点数量，1
        boolean bRet = false; // 返回值标志，初始为false表示未处理，1
        if(fingerlistener != null) { // 检查监听器是否已注册，1
            switch (event.getActionMasked()) { // 根据触摸动作类型进行分支处理，1
                case MotionEvent.ACTION_DOWN: // 处理单指按下事件，1
                    fine_dir = FINE_DIR_NONE; // 重置手势方向为无手势，1
                    fine_down_x[0] = event.getX(); // 记录第一个触摸点的按下X坐标，1
                    fine_down_y[0] = event.getY(); // 记录第一个触摸点的按下Y坐标，1
                    break; // 跳出当前case分支，1
                case MotionEvent.ACTION_POINTER_DOWN: // 处理多指按下事件（第二指按下），1
                    if (pointCount == 2) { // 检查是否为双指按下，1
                        for (int i = 0; i < 2; i++) { // 循环遍历两个触摸点，1
                            fine_move_x[i] = fine_down_x[i] = event.getX(i); // 记录按下时的X坐标到按下和移动数组，1
                            fine_move_y[i] = fine_down_y[i] = event.getY(i); // 记录按下时的Y坐标到按下和移动数组，1
                        } // 结束循环，1
                        L[0] = Math.abs(fine_down_x[0] - fine_down_x[1]); // 计算按下时两点的X方向距离，1
                        L[1] = Math.abs(fine_down_y[0] - fine_down_y[1]); // 计算按下时两点的Y方向距离，1
                        fingerlistener.onBegin(); // 触发手势开始回调，通知监听器，1
                    } // 结束双指按下判断，1
                    break; // 跳出当前case分支，1
                case MotionEvent.ACTION_POINTER_UP: // 处理多指抬起事件（其中一指抬起），1
                    if (pointCount == 2) { // 检查是否为双指状态下的抬起，1
                        fingerlistener.onEnd(); // 触发手势结束回调，通知监听器，1
                    } // 结束双指抬起判断，1
                    break; // 跳出当前case分支，1
                case MotionEvent.ACTION_MOVE: // 处理触摸移动事件，1
                    if(pointCount == 1) { // 检查是否为单指移动，1
                        if(fine_dir == FINE_DIR_NONE){ // 检查手势方向是否未确定，1
                            if (Math.abs(event.getX() - fine_down_x[0]) > TOUCH_THRESHOLD){ // 判断X方向移动是否超过阈值，1
                                fine_dir = FINE_HORIZONTAL_SLIDE; // 设置手势方向为水平滑动，1
                            }else if(Math.abs(event.getY() - fine_down_y[0]) > TOUCH_THRESHOLD){ // 判断Y方向移动是否超过阈值，1
                                fine_dir = FINE_VERTICAL_SLIDE; // 设置手势方向为垂直滑动，1
                            } // 结束手势方向判定，1
                        } // 结束手势方向未确定判断，1
                    }else if (pointCount == 2) { // 检查是否为双指移动，这是主要的手势识别逻辑，1

                        float[] x = {0, 0}; // 创建X方向移动距离数组，初始值为0，1
                        float[] y = {0, 0}; // 创建Y方向移动距离数组，初始值为0，1
                        float[] l = {0, 0}; // 创建当前两点距离数组，初始值为0，1

                        for (int i = 0; i < 2; i++) { // 循环遍历两个触摸点，1
                            x[i] = event.getX(i) - fine_down_x[i]; // 计算第i个点的X方向移动距离，1
                            y[i] = event.getY(i) - fine_down_y[i]; // 计算第i个点的Y方向移动距离，1
                        } // 结束循环，1

                        l[0] = Math.abs(event.getX(0) - event.getX(1)); // 计算当前两点的X方向距离，1
                        l[1] = Math.abs(event.getY(0) - event.getY(1)); // 计算当前两点的Y方向距离，1


                        if (fine_dir == FINE_DIR_NONE) { // 检查手势方向是否未确定，需要识别手势类型，1
                            if (Math.abs(x[0]) > TOUCH_THRESHOLD && Math.abs(x[1]) > TOUCH_THRESHOLD) { // 判断两点X方向移动是否都超过阈值，1
                                fine_dir = (x[0] * x[1]) > 0 ? FINE_HORIZONTAL_SLIDE : FINE_HORIZONTAL_ZOOM; // 同向为滑动，反向为缩放，1
                            } else if (Math.abs(y[0]) > TOUCH_THRESHOLD && Math.abs(y[1]) > TOUCH_THRESHOLD) { // 判断两点Y方向移动是否都超过阈值，1
                                fine_dir = (y[0] * y[1]) > 0 ? FINE_VERTICAL_SLIDE : FINE_VERTICAL_ZOOM; // 同向为滑动，反向为缩放，1
                            }else if(Math.abs(l[0] - L[0]) > TOUCH_THRESHOLD * 2){ // 判断X方向距离变化是否超过阈值（用于缩放判定），1
                                fine_dir = FINE_HORIZONTAL_ZOOM; // 设置手势方向为水平缩放，1
                            }else if(Math.abs(l[1] - L[1]) > TOUCH_THRESHOLD * 2){ // 判断Y方向距离变化是否超过阈值（用于缩放判定），1
                                fine_dir = FINE_VERTICAL_ZOOM; // 设置手势方向为垂直缩放，1
                            } // 结束手势类型识别，1
                        } // 结束手势方向未确定判断，1
                        boolean bSlide = false; // 滑动处理标志，初始为false，1
                        if (fine_dir != FINE_DIR_NONE) { // 检查是否已识别到手势，1
                            switch (fine_dir) { // 根据手势类型进行分支处理，1
                                case FINE_HORIZONTAL_SLIDE: { // 处理水平滑动手势，1
                                    float val = (fine_move_x[0] - event.getX(0) + fine_move_x[1] - event.getX(1)) / TOUCH_THRESHOLD * 2; // 计算滑动增量值（阈值倍数），1
                                    if (Math.abs(val) > 1) { // 判断滑动增量是否超过1个阈值单位，1
                                        fingerlistener.onHorizontalSlide(val); // 触发水平滑动回调，传递增量值，1
                                        bSlide = true; // 设置滑动处理标志为true，1
                                    } // 结束滑动增量判断，1
                                } // 结束水平滑动处理块，1
                                    break; // 跳出当前case分支，1
                                case FINE_HORIZONTAL_ZOOM: // 处理水平缩放手势，1
                                    fingerlistener.onHorizontalZoom((event.getX(0) + event.getX(1)) / 2, // 计算缩放中心X坐标（两点中点），1
                                            Math.abs(event.getX(0) - event.getX(1)) / Math.abs(fine_down_x[0] - fine_down_x[1])); // 计算缩放比例（当前距离/初始距离），1
                                    break; // 跳出当前case分支，1
                                case FINE_VERTICAL_SLIDE: { // 处理垂直滑动手势，1
                                    float val = (fine_move_y[0] - event.getY(0) + fine_move_y[1] - event.getY(1)) /  TOUCH_THRESHOLD * 2; // 计算滑动增量值（阈值倍数），1
                                    if (Math.abs(val) > 1) { // 判断滑动增量是否超过1个阈值单位，1
                                        fingerlistener.onVerticalSlide(val); // 触发垂直滑动回调，传递增量值，1
                                        bSlide = true; // 设置滑动处理标志为true，1
                                    } // 结束滑动增量判断，1
                                } // 结束垂直滑动处理块，1
                                    break; // 跳出当前case分支，1
                                case FINE_VERTICAL_ZOOM: // 处理垂直缩放手势，1
                                    fingerlistener.onVerticalZoom((event.getY(0) + event.getY(1)) / 2, // 计算缩放中心Y坐标（两点中点），1
                                            Math.abs(event.getY(0) - event.getY(1)) / Math.abs(fine_down_y[0] - fine_down_y[1])); // 计算缩放比例（当前距离/初始距离），1
                                    break; // 跳出当前case分支，1
                            } // 结束手势类型分支，1
                        } // 结束手势已识别判断，1
                        if(bSlide) { // 检查是否处理了滑动，需要更新移动坐标，1
                            for (int i = 0; i < 2; i++) { // 循环遍历两个触摸点，1
                                fine_move_x[i] = event.getX(i); // 更新第i个点的移动X坐标，用于下次计算增量，1
                                fine_move_y[i] = event.getY(i); // 更新第i个点的移动Y坐标，用于下次计算增量，1
                            } // 结束循环，1
                        } // 结束滑动处理判断，1
                        bRet = true; // 设置返回值为true，表示已处理双指手势，1
                    } // 结束双指移动判断，1
                    break; // 跳出当前case分支，1
            } // 结束触摸动作分支，1
        } // 结束监听器判断，1

        return bRet; // 返回处理结果，true表示已处理，false表示未处理，1
    } // 结束onDoubleFinger方法，1
} // 结束DoubleFinger类定义，1