package com.micsig.tbook.tbookscope.util; // 工具包，存放示波器应用的各类工具类

import android.graphics.Bitmap; // 位图类，用于从View创建截图
import android.graphics.Canvas; // 画布类，用于将View绘制到Bitmap上
import android.os.Build; // 系统版本信息，用于API兼容性判断
import android.view.View; // 视图基类，作为各种操作的目标对象
import android.view.ViewTreeObserver; // 视图树观察者，用于监听布局变化

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                         ViewUtils - 视图工具类                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：MHO示波器Android应用 → 工具模块(util) → 视图辅助                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                    │
 * │   1. 防快速双击判断（500ms内同一View的重复点击视为无效）                        │
 * │   2. 布局完成后执行一次性回调（doAfterLayout）                                 │
 * │   3. 从View创建Bitmap截图                                                    │
 * │   4. 对话框偏移量管理（Float/Formula/Number/MathRefBus四种对话框的Y偏移）      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                    │
 * │   - 双重检查锁（DCL）单例模式，volatile保证可见性                              │
 * │   - 防双击功能为实例方法（依赖实例状态lastClickTime/lastClickViewId）          │
 * │   - 布局回调和截图功能为静态方法（无状态依赖）                                  │
 * │   - 对话框偏移量为静态变量，全局共享                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                    │
 * │   防双击：onClick → isFastDoubleClick(viewId) → true(忽略)/false(处理)       │
 * │   布局回调：doAfterLayout(view, runnable) → OnGlobalLayoutListener → runnable│
 * │   截图：createBitmapFromView(view) → measure/layout → draw(Canvas) → Bitmap │
 * │   偏移量：setDialogXxxOffset(int) → 静态变量 → getDialogXxxOffset() → 对话框 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                    │
 * │   - View/ViewTreeObserver：Android视图系统                                    │
 * │   - Bitmap/Canvas：Android图形系统                                            │
 * │   - Build：API版本兼容性判断                                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                    │
 * │   // 防双击                                                                    │
 * │   if (ViewUtils.getInstance().isFastDoubleClick(v.getId())) return;          │
 * │   // 布局完成后执行                                                            │
 * │   ViewUtils.doAfterLayout(view, () -> { view.getWidth(); });                 │
 * │   // 从View创建截图                                                           │
 * │   Bitmap bmp = ViewUtils.createBitmapFromView(view);                         │
 * │   // 设置对话框偏移量                                                          │
 * │   ViewUtils.setDialogFloatOffset(20);                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class ViewUtils {

    /** 防快速双击的时间间隔阈值（毫秒），两次点击间隔小于此值视为无效双击 */
    private static final long diffTime = 500; // 双击间隔阈值：500ms

    /** 上一次点击的时间戳（毫秒），用于计算两次点击间隔 */
    private long lastClickTime; // 上次点击时间

    /** 上一次点击的View资源ID，用于区分不同View的点击事件 */
    private int lastClickViewId; // 上次点击的View ID

    /** 单例实例，volatile保证多线程可见性 */
    private static volatile ViewUtils instance; // DCL单例实例

    /** Float对话框的Y轴偏移量（像素），用于对话框位置微调 */
    private static int dialogFloatOffset = 0; // Float对话框偏移量

    /** Formula对话框的Y轴偏移量（像素），用于对话框位置微调 */
    private static int dialogFormulaOffset = 0; // Formula对话框偏移量

    /** Number对话框的Y轴偏移量（像素），用于对话框位置微调 */
    private static int dialogNumberOffset = 0; // Number对话框偏移量

    /** MathRefBus对话框的Y轴偏移量（像素），默认-3，用于对话框位置微调 */
    private static int mathRefBusOffset = -3; // MathRefBus对话框偏移量，默认-3

    /**
     * 获取ViewUtils单例实例。
     * 使用双重检查锁（Double-Checked Locking）模式，保证线程安全且高效。
     * volatile修饰instance防止指令重排序导致的初始化不完整问题。
     *
     * @return ViewUtils唯一实例
     */
    public static ViewUtils getInstance() { // 获取单例实例
        if (instance == null) { // 第一次检查（无锁，快速路径）
            synchronized (ViewUtils.class) { // 加锁
                if (instance == null) { // 第二次检查（锁内，确保只创建一次）
                    instance = new ViewUtils(); // 创建实例
                }
            }
        }
        return instance; // 返回实例
    }

    /**
     * 判断是否为快速双击。
     * 如果同一个View在500ms内被点击两次，则认为是无效的快速双击。
     * 判断逻辑：
     * 1. 上次点击时间有效（>0）
     * 2. 两次点击间隔有效（>0）
     * 3. 同一个View（lastClickViewId == viewId）
     * 4. 间隔时间小于阈值（<500ms）
     * 以上条件同时满足才判定为快速双击，否则更新点击记录。
     *
     * @param viewId 被点击View的资源ID（view.getId()）
     * @return true=是快速双击（应忽略）；false=不是快速双击（应处理）
     */
    public boolean isFastDoubleClick(int viewId) { // 防快速双击判断
        long time = System.currentTimeMillis(); // 获取当前时间戳
        long timeD = time - lastClickTime; // 计算两次点击间隔（毫秒）
        if (0 < lastClickTime && 0 < timeD && lastClickViewId == viewId && timeD < diffTime) { // 满足快速双击条件
            return true; // 是快速双击，应忽略
        } else { // 不是快速双击
            lastClickTime = time; // 更新上次点击时间
            lastClickViewId = viewId; // 更新上次点击的View ID
            return false; // 不是快速双击，应处理
        }
    }

    /**
     * 在视图布局完成后执行一次性操作。
     * 通过ViewTreeObserver.OnGlobalLayoutListener监听布局变化，
     * 布局完成后自动移除监听器并执行回调，避免重复触发。
     * 典型场景：在View添加到窗口后获取其宽高。
     *
     * @param view     目标视图，需要监听其布局完成
     * @param runnable 布局完成后执行的回调
     */
    public static void doAfterLayout(final View view, final Runnable runnable) { // 布局完成后回调
        final ViewTreeObserver observer = view.getViewTreeObserver(); // 获取视图树观察者
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // 添加全局布局监听器
            @Override
            public void onGlobalLayout() { // 布局变化回调
                // 确保移除监听器，避免重复触发
                removeListener(observer, this); // 移除当前监听器
                // 执行回调
                runnable.run(); // 执行用户回调
            }
        });
    }

    /**
     * 移除OnGlobalLayoutListener监听器，兼容不同API版本。
     * API 16+ 使用 removeOnGlobalLayoutListener()
     * API 16以下 使用 removeGlobalOnLayoutListener()（已废弃但必须使用）
     * 移除前检查observer是否存活，避免抛出IllegalStateException。
     *
     * @param observer 视图树观察者
     * @param listener 要移除的监听器
     */
    private static void removeListener(ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener listener) { // 移除布局监听器（兼容版本）
        if (observer.isAlive()) { // 观察者仍然存活
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // API 16+
                observer.removeOnGlobalLayoutListener(listener); // 使用新API移除
            } else { // API 16以下
                observer.removeGlobalOnLayoutListener(listener); // 使用废弃API移除（兼容旧版本）
            }
        }
    }

    /**
     * 从View创建Bitmap截图。
     * 如果View尚未测量布局（宽高<=0），先手动执行measure和layout。
     * 然后创建ARGB_8888格式的Bitmap，通过Canvas将View绘制到Bitmap上。
     *
     * 注意：如果View有硬件加速层，可能无法正确绘制。
     * 注意：手动measure/layout可能导致与实际布局不一致，仅在View未布局时使用。
     *
     * @param view 要截图的视图
     * @return 包含View视觉内容的Bitmap对象（ARGB_8888格式）
     */
    public static Bitmap createBitmapFromView(View view) { // 从View创建Bitmap
        if (view.getWidth() <= 0 || view.getHeight() <= 0) { // View尚未测量或布局
            view.measure( // 手动测量View
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), // 宽度：未指定模式
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED) // 高度：未指定模式
            );
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()); // 手动布局View
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888); // 创建ARGB_8888位图
        Canvas canvas = new Canvas(bitmap); // 创建画布并关联Bitmap

        view.draw(canvas); // 将View绘制到Canvas（即Bitmap）上
        return bitmap; // 返回截图Bitmap
    }


    /**
     * 获取Float对话框的Y轴偏移量
     * @return 偏移量（像素）
     */
    public static int getDialogFloatOffset() { // 获取Float对话框偏移量
        return dialogFloatOffset; // 返回当前偏移值
    }

    /**
     * 设置Float对话框的Y轴偏移量
     * @param dialogFloatOffset 偏移量（像素）
     */
    public static void setDialogFloatOffset(int dialogFloatOffset) { // 设置Float对话框偏移量
        ViewUtils.dialogFloatOffset = dialogFloatOffset; // 更新静态偏移值
    }

    /**
     * 获取Formula对话框的Y轴偏移量
     * @return 偏移量（像素）
     */
    public static int getDialogFormulaOffset() { // 获取Formula对话框偏移量
        return dialogFormulaOffset; // 返回当前偏移值
    }

    /**
     * 设置Formula对话框的Y轴偏移量
     * @param dialogFormulaOffset 偏移量（像素）
     */
    public static void setDialogFormulaOffset(int dialogFormulaOffset) { // 设置Formula对话框偏移量
        ViewUtils.dialogFormulaOffset = dialogFormulaOffset; // 更新静态偏移值
    }

    /**
     * 获取Number对话框的Y轴偏移量
     * @return 偏移量（像素）
     */
    public static int getDialogNumberOffset() { // 获取Number对话框偏移量
        return dialogNumberOffset; // 返回当前偏移值
    }

    /**
     * 设置Number对话框的Y轴偏移量
     * @param dialogNumberOffset 偏移量（像素）
     */
    public static void setDialogNumberOffset(int dialogNumberOffset) { // 设置Number对话框偏移量
        ViewUtils.dialogNumberOffset = dialogNumberOffset; // 更新静态偏移值
    }

    /**
     * 获取MathRefBus对话框的Y轴偏移量
     * @return 偏移量（像素），默认-3
     */
    public static int getMathRefBusOffset() { // 获取MathRefBus对话框偏移量
        return mathRefBusOffset; // 返回当前偏移值
    }

    /**
     * 设置MathRefBus对话框的Y轴偏移量
     * @param mathRefBusOffset 偏移量（像素）
     */
    public static void setMathRefBusOffset(int mathRefBusOffset) { // 设置MathRefBus对话框偏移量
        ViewUtils.mathRefBusOffset = mathRefBusOffset; // 更新静态偏移值
    }
}
