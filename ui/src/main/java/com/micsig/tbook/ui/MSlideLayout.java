package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.util.AttributeSet;  // XML属性集类
import android.view.InputDevice;  // 输入设备类
import android.view.MotionEvent;  // 触摸事件类
import android.view.VelocityTracker;  // 速度追踪器
import android.view.View;  // Android视图基类
import android.view.ViewConfiguration;  // 视图配置类
import android.widget.AbsoluteLayout;  // 绝对布局（已废弃，但仍使用）

import java.util.ArrayList;  // 动态数组列表
import java.util.List;  // List接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                             MSlideLayout                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 可滑动切换的布局容器                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承AbsoluteLayout，提供可滑动切换的布局容器                               │
 * │   2. 支持上下滑动切换子视图                                                    │
 * │   3. 实现滑动动画效果和透明度渐变                                              │
 * │   4. 支持滑动方向检测和速度追踪                                                │
 * │   5. 提供滑动切换的回调接口                                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │  AbsoluteLayout │  ← 继承自绝对布局                                       │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐                                                       │
 * │   │  MSlideLayout   │  ← 扩展触摸事件处理，实现滑动切换                        │
 * │   └─────────────────┘                                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   触摸事件 → 方向判断 → 视图切换 → 动画效果 → 回调通知                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - AbsoluteLayout: 基类，提供绝对布局功能                                     │
 * │   - VelocityTracker: 速度追踪器                                              │
 * │   - ViewConfiguration: 视图配置                                              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MSlideLayout                                         │
 * │       android:id="@+id/slide_layout"                                        │
 * │       android:layout_width="match_parent"                                   │
 * │       android:layout_height="match_parent">                                 │
 * │       <View ... />                                                          │
 * │       <View ... />                                                          │
 * │   </com.micsig.tbook.ui.MSlideLayout>                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author Liwb                                                                 │
 * │ @date 2022-2-9 19:47                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MSlideLayout extends AbsoluteLayout {
    
    // =========================== 类常量定义 ===========================
    
    /** 日志标签 */  // 用于调试输出
    public static final String TAG = MSlideLayout.class.getSimpleName();

    /** 滑动范围无效大小阈值，滑动小于此值视为无效 */  // 80像素
    public static final int MinSliderRange = 80;
    
    /** 滑动方向常量：无方向 */  // 默认值
    public static final int SliderDir_None = 0x00;
    
    /** 滑动方向常量：从左到右 */  // 水平滑动方向
    public static final int SliderDir_LeftToRight = 0x01;
    
    /** 滑动方向常量：从右到左 */  // 水平滑动方向（取反）
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight;
    
    /** 滑动方向常量：从上到下 */  // 垂直滑动方向
    public static final int SliderDir_TopToBottom = 0x02;
    
    /** 滑动方向常量：从下到上 */  // 垂直滑动方向（取反）
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom;
    
    /** 滑动方向最小识别大小（X方向） */  // 20像素
    public static final int SlipDirectionSize_X = 20;
    
    /** 滑动方向最小识别大小（Y方向） */  // 20像素
    public static final int SlipDirectionSize_Y = 20;
    
    /** 最小滑动距离 */  // 25像素
    public static final int SLIDE_DISTANCE_MIN = 25;

    // =========================== 成员变量定义 ===========================
    
    /** Android上下文环境 */  // 用于资源访问
    private Context context;
    
    /** 可见子视图列表 */  // 存储所有可见的子视图
    private List<View> items = new ArrayList<>();
    
    /** 触摸坐标 */  // downX: 按下时的X坐标
    private int downX, downY, moveX, moveY;  // downY: 按下时的Y坐标; moveX: 移动时的X坐标; moveY: 移动时的Y坐标
    
    /** 速度追踪器 */  // 用于追踪滑动速度
    private VelocityTracker vTracker = null;
    
    /** Y方向速度 */  // 计算得到的垂直滑动速度
    private float velocityY;
    
    /** 触摸点ID */  // 用于多点触控
    private int pointerId;
    
    /** 最大滑动速度 */  // 从ViewConfiguration获取
    private int maxVelocity;
    
    /** 当前滑动方向 */  // 使用SliderDir_*常量
    private int slipDir = SliderDir_None;

    // =========================== 接口定义 ===========================
    
    /**
     * 标签页切换监听接口
     */
    public interface OnTabChanged {
        /**
         * 标签页切换回调
         * 
         * @param channel true表示第一个标签页，false表示其他
         */
        void onTabChanged(boolean channel);
    }

    /** 标签页切换监听器 */  // 外部设置的回调
    private OnTabChanged onTabChanged;

    /** 当前显示的视图索引 */  // 从0开始
    private int ShowIndex = 0;

    /** 是否禁止移动 */  // true时禁用滑动切换
    private boolean noMove;

    // =========================== Getter/Setter方法 ===========================
    
    /**
     * 获取是否禁止移动
     * 
     * @return true表示禁止移动，false表示允许移动
     */
    public boolean isNoMove() {
        return noMove;  // 返回禁止移动状态
    }

    /**
     * 设置是否禁止移动
     * 
     * @param noMove true表示禁止移动，false表示允许移动
     */
    public void setNoMove(boolean noMove) {
        this.noMove = noMove;  // 设置禁止移动状态
    }

    /**
     * 获取标签页切换监听器
     * 
     * @return OnTabChanged监听器实例
     */
    public OnTabChanged getOnTabChanged() {
        return onTabChanged;  // 返回监听器
    }

    /**
     * 设置标签页切换监听器
     * 
     * @param onTabChanged 监听器实例
     */
    public void setOnTabChanged(OnTabChanged onTabChanged) {
        this.onTabChanged = onTabChanged;  // 设置监听器
    }

    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MSlideLayout(Context context) {
        this(context, null);  // 调用双参数构造
    }

    /**
     * 双参数构造方法
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     */
    public MSlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造
    }

    /**
     * 三参数构造方法（完整构造）
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MSlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;  // 保存上下文引用
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();  // 获取系统最大滑动速度

    }

    // =========================== 布局方法 ===========================
    
    /**
     * 布局方法
     * 在布局时收集所有可见的子视图
     * 
     * @param changed 布局是否发生变化
     * @param l 左边界
     * @param t 上边界
     * @param r 右边界
     * @param b 下边界
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (items.size() <= 0) {  // 如果子视图列表为空
            items.clear();  // 清空列表
            for (int i = 0; i < this.getChildCount(); i++) {  // 遍历所有子视图
                if (this.getChildAt(i).getVisibility() == VISIBLE) {  // 检查是否可见
                    items.add(this.getChildAt(i));  // 添加到可见列表
                }
            }
        }
        super.onLayout(changed, l, t, r, b);  // 调用父类布局方法
    }

    // =========================== 触摸事件处理 ===========================
    
    /**
     * 触摸事件拦截方法
     * 判断是否需要拦截触摸事件
     * 
     * @param ev 触摸事件
     * @return true表示拦截事件，false表示不拦截
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {  // 根据动作类型处理
            case MotionEvent.ACTION_DOWN:  // 按下事件
                if (noMove) {  // 如果禁止移动
                    break;  // 不处理
                }
                downX = (int) ev.getX();  // 记录按下X坐标
                downY = (int) ev.getY();  // 记录按下Y坐标
                slipDir = SliderDir_None;  // 重置滑动方向
                break;
            case MotionEvent.ACTION_MOVE:  // 移动事件
                if (noMove) {  // 如果禁止移动
                    break;  // 不处理
                }
                moveX = (int) ev.getX();  // 记录移动X坐标
                moveY = (int) ev.getY();  // 记录移动Y坐标
                // Logger.i(FPGACommand.TAG,"offsetY:"+Math.abs(moveY - downY));  // 调试日志
                if (Math.abs(moveY - downY) > SLIDE_DISTANCE_MIN) {  // 检查Y方向移动距离是否超过阈值
                    return true;  // 如果是滑动，就自己处理，也就是交给onTouchEvent；其他情况就传下去
                }
                break;
            case MotionEvent.ACTION_UP:  // 抬起事件
                break;
        }
        return super.onInterceptTouchEvent(ev);  // 调用父类拦截方法
    }

    /**
     * 触摸事件处理方法
     * 实现滑动切换逻辑
     * 
     * @param event 触摸事件
     * @return true表示事件已处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (vTracker == null) {  // 如果速度追踪器为空
            vTracker = VelocityTracker.obtain();  // 创建新的速度追踪器
        } else {  // 速度追踪器已存在
            vTracker.clear();  // 清空追踪器
        }
        vTracker.addMovement(event);  // 添加移动事件到追踪器
        switch (event.getAction() & MotionEvent.ACTION_MASK) {  // 处理触摸事件
            case MotionEvent.ACTION_DOWN:  // 按下事件
                velocityY = 0;  // 重置Y方向速度
                downX = (int) event.getX();  // 记录按下X坐标
                downY = (int) event.getY();  // 记录按下Y坐标
                slipDir = SliderDir_None;  // 重置滑动方向
                break;
            case MotionEvent.ACTION_MOVE:  // 移动事件
                if (noMove) {  // 如果禁止移动
                    break;  // 不处理
                }
                // Logger.i(FPGACommand.TAG,"slipDir:"+slipDir);  // 调试日志
                vTracker.computeCurrentVelocity(1000, maxVelocity);  // 计算当前速度
                float vY = vTracker.getYVelocity(pointerId);  // 获取Y方向速度
                if (vY != 0) {  // 如果速度不为0
                    velocityY = vY;  // 更新Y方向速度
                }
                moveX = (int) event.getX();  // 记录移动X坐标
                moveY = (int) event.getY();  // 记录移动Y坐标
                if (slipDir == SliderDir_None) {  // 如果滑动方向未确定
                    slipDir = getSlipDirection(downX, downY, moveX, moveY);  // 计算滑动方向
                    // Logger.i(FPGACommand.TAG,"slipDir:"+slipDir+",downX:"+downX+",downY:"+downY+",moveX:"+moveX+",moveY:"+moveY);  // 调试日志
                }
                if (slipDir == SliderDir_TopToBottom) {  // 从上往下滑动
                    View CurView = getCurView();  // 获取当前视图
                    View PreView = getPreView();  // 获取前一个视图
                    if (CurView == PreView) break;  // 如果是同一个视图，跳过
                    CurView.setVisibility(VISIBLE);  // 显示当前视图
                    PreView.setVisibility(VISIBLE);  // 显示前一个视图
                    int offsetY = Math.abs(moveY - downY);  // 计算Y方向偏移量
                    CurView.setAlpha((CurView.getHeight() - offsetY) * 1.0f / (CurView.getHeight() - 200));  // 设置当前视图透明度
                    CurView.setY(offsetY);  // 设置当前视图Y位置
                    PreView.setY(offsetY - PreView.getHeight());  // 设置前一个视图Y位置
                }
                if (slipDir == SliderDir_BottomToTop) {  // 从下往上滑动
                    View CurView = getCurView();  // 获取当前视图
                    View NextView = getNextView();  // 获取下一个视图
                    if (CurView == NextView) break;  // 如果是同一个视图，跳过
                    CurView.setVisibility(VISIBLE);  // 显示当前视图
                    NextView.setVisibility(VISIBLE);  // 显示下一个视图
                    int offsetY = Math.abs(moveY - downY);  // 计算Y方向偏移量
                    CurView.setAlpha((CurView.getHeight() - offsetY) * 1.0f / (CurView.getHeight() - 200));  // 设置当前视图透明度
                    CurView.setY(0 - offsetY);  // 设置当前视图Y位置
                    NextView.setY(NextView.getHeight() - offsetY);  // 设置下一个视图Y位置
                }
                break;

            case MotionEvent.ACTION_UP: {  // 抬起事件
                View CurView = getCurView();  // 获取当前视图
                View PreView = getPreView();  // 获取前一个视图
                View NextView = getNextView();  // 获取下一个视图
                if (CurView == NextView) break;  // 如果是同一个视图，跳过

                // 再计算一下滑动方向
                moveX = (int) event.getX();  // 获取抬起时的X坐标
                moveY = (int) event.getY();  // 获取抬起时的Y坐标
                int upSlipDir = getSlipDirection(downX, downY, moveX, moveY);  // 计算最终滑动方向
                int range = Math.abs(moveY - downY);  // 计算滑动距离
                if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {  // 如果是鼠标事件
                    velocityY = moveY - downY;  // 使用位移作为速度
                    velocityY *= 1.5;  // 放大速度
                }
                // Logger.i(FPGACommand.TAG,"velocity:"+velocityY+",slipDir:"+slipDir);  // 调试日志
                if (upSlipDir == SliderDir_TopToBottom) {  // 从上往下滑动
                    if (velocityY > MinSliderRange) {  // 如果速度足够大
                        ShowIndexLayout(PreView);  // 显示前一个视图
                    } else {  // 速度不够
                        ShowIndexLayout(CurView);  // 保持当前视图
                    }
                } else if (upSlipDir == SliderDir_BottomToTop) {  // 从下往上滑动
                    if (velocityY < 0 - MinSliderRange) {  // 如果速度足够大（负值表示向上）
                        ShowIndexLayout(NextView);  // 显示下一个视图
                    } else {  // 速度不够
                        ShowIndexLayout(CurView);  // 保持当前视图
                    }
                } else {  // 其他方向
                    ShowIndexLayout(CurView);  // 保持当前视图
                }
                slipDir = SliderDir_None;  // 重置滑动方向
            } break;
            case MotionEvent.ACTION_CANCEL: {  // 取消事件
                View CurView = getCurView();  // 获取当前视图
                ShowIndexLayout(CurView);  // 保持当前视图
                slipDir = SliderDir_None;  // 重置滑动方向
            } break;
        }

        return true;  // 返回true表示事件已处理
    }

    // =========================== 视图获取方法 ===========================
    
    /**
     * 根据索引获取视图
     * 
     * @param showIndex 视图索引
     * @return 对应的视图，索引无效返回null
     */
    private View getIndexView(int showIndex) {
        if (showIndex >= 0 && showIndex < items.size()) {  // 检查索引有效性
            return items.get(showIndex);  // 返回对应视图
        } else {  // 索引无效
            return null;  // 返回null
        }
    }
    
    /**
     * 获取当前显示的视图
     * 
     * @return 当前显示的视图
     */
    private View getCurView() {
        return getIndexView(ShowIndex);  // 返回当前索引对应的视图
    }
    
    /**
     * 获取前一个视图
     * 索引循环，到达开头则跳到末尾
     * 
     * @return 前一个视图
     */
    private View getPreView() {
        int preView = ShowIndex - 1;  // 计算前一个索引
        if (preView < 0) {  // 如果索引小于0
            preView = items.size() - 1;  // 跳到末尾
        }
        return getIndexView(preView);  // 返回前一个视图
    }
    
    /**
     * 获取下一个视图
     * 索引循环，到达末尾则跳到开头
     * 
     * @return 下一个视图
     */
    private View getNextView() {
        int nextView = ShowIndex + 1;  // 计算下一个索引
        if (nextView >= items.size()) {  // 如果索引超出范围
            nextView = 0;  // 跳到开头
        }
        return getIndexView(nextView);  // 返回下一个视图
    }
    
    /**
     * 获取当前显示视图的索引
     * 
     * @return 当前视图索引
     */
    public int getCurViewIdx() {
        return ShowIndex;  // 返回当前索引
    }

    // =========================== 滑动方向检测 ===========================
    
    /**
     * 计算滑动方向
     * 根据起点和终点坐标判断滑动方向
     * 
     * @param oldX 起点X坐标
     * @param oldY 起点Y坐标
     * @param newX 终点X坐标
     * @param newY 终点Y坐标
     * @return 滑动方向常量（SliderDir_*）
     */
    public static int getSlipDirection(int oldX, int oldY, int newX, int newY) {
        int slipDir;  // 滑动方向
        if (Math.abs(oldY - newY) > SlipDirectionSize_Y && Math.abs(oldY - newY) > Math.abs(oldX - newX) * 2) {  // 判断是否为垂直滑动
            // 说明是上下滑动
            if (oldY - newY > 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {  // 向上滑动
                slipDir = SliderDir_BottomToTop;  // 从下到上
            } else if (oldY - newY < 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {  // 向下滑动
                slipDir = SliderDir_TopToBottom;  // 从上到下
            } else {  // 不满足阈值
                slipDir = SliderDir_None;  // 无方向
            }
        } else {  // 水平滑动
            // 说明是左右滑动
            if (oldX - newX > 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {  // 向左滑动
                slipDir = SliderDir_RightToLeft;  // 从右到左
            } else if (oldX - newX < 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {  // 向右滑动
                slipDir = SliderDir_LeftToRight;  // 从左到右
            } else {  // 不满足阈值
                slipDir = SliderDir_None;  // 无方向
            }
        }

        return slipDir;  // 返回滑动方向
    }

    // =========================== 视图切换方法 ===========================
    
    /**
     * 隐藏所有子视图
     * 将所有子视图设置为GONE并恢复透明度
     */
    private void hideAllItemLayout() {
        for (int i = 0; i < items.size(); i++) {  // 遍历所有子视图
            items.get(i).setVisibility(GONE);  // 设置为GONE
            items.get(i).setAlpha(1);  // 恢复透明度
        }
    }

    /**
     * 显示指定视图
     * 隐藏其他视图，显示目标视图，并触发回调
     * 
     * @param view 要显示的视图
     */
    private void ShowIndexLayout(View view) {
        hideAllItemLayout();  // 隐藏所有视图
        for (int i = 0; i < items.size(); i++) {  // 遍历所有子视图
            if (items.get(i) == view && ShowIndex != i) {  // 找到目标视图且索引不同
                ShowIndex = i;  // 更新当前索引
                if (onTabChanged != null) {  // 如果设置了回调
                    onTabChanged.onTabChanged(ShowIndex == 0 ? true : false);  // 触发回调
                }
                break;  // 跳出循环
            }
        }
        view.setVisibility(VISIBLE);  // 显示目标视图
        view.setX(0);  // 重置X位置
        view.setY(0);  // 重置Y位置
    }
    
    /**
     * 显示指定索引的视图
     * 
     * @param index 要显示的视图索引
     */
    public void ShowIndexLayout(int index) {
        if (index < 0 || index >= items.size()) return;  // 检查索引有效性
        ShowIndex = index;  // 更新当前索引
        hideAllItemLayout();  // 隐藏所有视图
        items.get(index).setVisibility(VISIBLE);  // 显示目标视图
        items.get(index).setX(0);  // 重置X位置
        items.get(index).setY(0);  // 重置Y位置
    }
}
