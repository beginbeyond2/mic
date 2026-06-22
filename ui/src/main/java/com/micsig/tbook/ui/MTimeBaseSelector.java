package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.annotation.SuppressLint; // 抑制lint警告的注解
import android.content.Context; // Android上下文对象
import android.graphics.Canvas; // 画布对象，用于自定义绘制
import android.graphics.Color; // 颜色类
import android.graphics.Paint; // 画笔对象
import android.graphics.RadialGradient; // 径向渐变
import android.graphics.Rect; // 矩形区域
import android.graphics.RectF; // 矩形区域（浮点型）
import android.graphics.Shader; // 着色器基类
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.view.MotionEvent; // 触摸事件
import android.view.View; // Android视图基类
import android.view.ViewGroup; // Android视图组基类

import androidx.annotation.Nullable; // 可空性注解

import java.util.ArrayList; // 动态数组
import java.util.List; // 列表接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    MTimeBaseSelector - 时基选择器控件                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 选择器控件                                           ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供示波器时基（时间/格）选择功能                                         ║
 * ║   2. 支持网格布局显示多个时基选项                                             ║
 * ║   3. 支持触摸选择和滑动切换                                                   ║
 * ║   4. 提供选中状态的高亮显示                                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MTimeBaseSelector extends View                                    ║
 * ║   内部类: MRadioButton - 单个时基选项的数据模型                                ║
 * ║   布局方式: 网格布局，每行最多12个选项                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   initDataUI设置数据 → onDraw绘制 → onTouchEvent处理触摸 → 回调通知           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: 无                                                                ║
 * ║   外部依赖: Android SDK (View, Canvas, Paint等)                               ║
 * ║   资源依赖: R.color.* (颜色资源)                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   MTimeBaseSelector selector = findViewById(R.id.time_base_selector);         ║
 * ║   selector.initDataUI(timeBaseList);                                          ║
 * ║   selector.setOnClickEvent(new OnClickEvent() {                               ║
 * ║       void onTouchDown(int index, String value) { ... }                       ║
 * ║       void onTouchUp(int index, String value) { ... }                         ║
 * ║       void onItemChange(int index, String value) { ... }                      ║
 * ║   });                                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 每行最多显示12个选项                                                     ║
 * ║   2. 选项高度固定为80像素                                                     ║
 * ║   3. 宽度根据总宽度自动计算                                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @version 1.0
 */

public class MTimeBaseSelector extends View {
    // ================================ 常量定义 ================================
    
    private String TAG = "MTimeBaseSelector"; // 日志标签

    // ================================ 成员变量定义 ================================
    
    private Context context; // Android上下文对象
    private List<MRadioButton> list = new ArrayList<>(); // 时基选项列表
    private Paint paint = new Paint(); // 画笔对象
    private Rect rectText = new Rect(); // 文本测量矩形
    private OnClickEvent onClickEvent; // 点击事件监听器
    private int currItemIndex = 0; // 当前选中的选项索引
    private final int backGroudLineColor = Color.parseColor("#012c4e"); // 背景线条颜色

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 接口：OnClickEvent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   时基选择器点击事件回调接口
     *   提供按下、抬起和选项变化三种事件回调
     */
    public interface OnClickEvent { // 点击事件回调接口
        /**
         * 触摸按下事件回调
         * @param index 选中的选项索引
         * @param value 选中的选项值
         */
        void onTouchDown(int index, String value); // 按下事件

        /**
         * 触摸抬起事件回调
         * @param index 选中的选项索引
         * @param value 选中的选项值
         */
        void onTouchUp(int index, String value); // 抬起事件

        /**
         * 选项变化事件回调（滑动时触发）
         * @param index 新选中的选项索引
         * @param value 新选中的选项值
         */
        void onItemChange(int index, String value); // 选项变化事件
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     */
    public MTimeBaseSelector(Context context) { // 构造方法：仅传入Context
        this(context, null); // 调用双参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     */
    public MTimeBaseSelector(Context context, @Nullable AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成初始化
     *   设置画笔属性和背景颜色
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     */
    public MTimeBaseSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        paint.setTextSize(20); // 设置文本大小
        paint.setAntiAlias(true); // 启用抗锯齿
        this.setBackgroundColor(getResources().getColor(R.color.color_Division_Line_595959)); // 设置背景颜色
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：initDataUI
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化数据和UI，创建时基选项列表
     *   根据数据量自动计算视图尺寸
     * 
     * 【参数说明】
     *   @param list 时基值字符串列表
     * 
     * 【处理流程】
     *   1. 清空现有选项列表
     *   2. 为每个时基值创建MRadioButton对象
     *   3. 计算并设置视图尺寸
     *   4. 请求重绘
     */
    public void initDataUI(List<String> list) { // 初始化数据和UI
        this.list.clear(); // 清空现有列表
        for (int i = 0; i < list.size(); i++) { // 遍历输入列表
            this.list.add(new MRadioButton(false, list.get(i), i)); // 创建并添加选项
        }
        ViewGroup.LayoutParams params = this.getLayoutParams(); // 获取布局参数
        params.width = 1802; // 设置固定宽度
        params.height = (int) (Math.ceil(1.0 * list.size() / MRadioButton.LENGTH) * MRadioButton.HEIGHT + Math.floor(1.0 * list.size() / MRadioButton.LENGTH) + 2); // 计算高度

        this.setLayoutParams(params); // 应用布局参数
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setSelectItems
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   根据时基值设置选中项
     * 
     * 【参数说明】
     *   @param s 要选中的时基值字符串
     */
    public void setSelectItems(String s) { // 设置选中项
        for (int i = 0; i < list.size(); i++) { // 遍历所有选项
            if (list.get(i).mText.equals(s)) { // 如果找到匹配项
                setSelectIndex(i); // 设置选中索引
                currItemIndex = i; // 更新当前选中索引
                return; // 返回
            }
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setSelectIndex
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   根据索引设置选中项
     * 
     * 【参数说明】
     *   @param index 要选中的选项索引
     * 
     * 【返回值】
     *   @return 当前实例，支持链式调用
     */
    private MTimeBaseSelector setSelectIndex(int index) { // 设置选中索引
        if (index >= 0 && index < list.size()) { // 检查索引有效性
            clearSelect(); // 清除所有选中状态
            list.get(index).setChecked(true); // 设置指定项为选中
            currItemIndex = index; // 更新当前选中索引
            invalidate(); // 请求重绘
        }
        return this; // 返回当前实例
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：clearSelect
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   清除所有选项的选中状态
     */
    private void clearSelect() { // 清除选中状态
        for (int i = 0; i < list.size(); i++) { // 遍历所有选项
            list.get(i).setChecked(false); // 设置为未选中
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getSelectIndex
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   根据触摸位置获取选中的选项索引
     * 
     * 【参数说明】
     *   @param event 触摸事件对象
     * 
     * 【返回值】
     *   @return 选中的选项索引，如果未找到返回-1
     */
    private int getSelectIndex(MotionEvent event) { // 获取触摸位置的选项索引
        int index = -1; // 初始化索引为-1
        int x = (int) event.getX(); // 获取触摸X坐标
        int y = (int) event.getY(); // 获取触摸Y坐标
        for (int i = 0; i < list.size(); i++) { // 遍历所有选项
            if (list.get(i).contains(x, y)) { // 如果触摸点在选项区域内
                return i; // 返回该索引
            }
        }
        return index; // 返回索引
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setOnClickEvent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置点击事件监听器
     * 
     * 【参数说明】
     *   @param onClickEvent 点击事件监听器实例
     */
    public void setOnClickEvent(OnClickEvent onClickEvent) { // 设置点击事件监听器
        this.onClickEvent = onClickEvent; // 保存监听器引用
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onTouchEvent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理触摸事件，实现选项选择和滑动切换
     * 
     * 【参数说明】
     *   @param event 触摸事件对象
     * 
     * 【返回值】
     *   @return 始终返回true，表示消费了事件
     * 
     * 【事件处理】
     *   ACTION_DOWN: 记录按下位置，触发onTouchDown回调
     *   ACTION_MOVE: 检测滑动，触发onItemChange回调
     *   ACTION_UP: 触发onTouchUp回调
     *   ACTION_CANCEL: 清除选中状态
     */
    @SuppressLint("ClickableViewAccessibility") // 抑制触摸警告
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 处理触摸事件
        switch (event.getAction()) { // 根据动作类型分发
            case MotionEvent.ACTION_DOWN: { // 按下事件
                int index = getSelectIndex(event); // 获取触摸位置的索引
                if (index < 0) break; // 如果无效则跳过
                setSelectIndex(index); // 设置选中项
                String value = list.get(index).mText; // 获取选中值
                if (onClickEvent != null) onClickEvent.onTouchDown(index, value); // 触发按下回调

            }
            break;
            case MotionEvent.ACTION_MOVE: { // 移动事件
                int index = getSelectIndex(event); // 获取触摸位置的索引
                if (index < 0) break; // 如果无效则跳过
                String value = list.get(index).mText; // 获取当前值
                if (onClickEvent != null && index != currItemIndex) // 如果设置了监听器且索引变化
                    onClickEvent.onItemChange(index, value); // 触发变化回调
                setSelectIndex(index); // 更新选中项
            }
            break;
            case MotionEvent.ACTION_UP: { // 抬起事件
                int index = getSelectIndex(event); // 获取触摸位置的索引
                if (index < 0) break; // 如果无效则跳过
                setSelectIndex(index); // 设置选中项
                String value = list.get(index).mText; // 获取选中值
                if (onClickEvent != null) onClickEvent.onTouchUp(index, value); // 触发抬起回调
            }
            break;
            case MotionEvent.ACTION_CANCEL: { // 取消事件
                clearSelect(); // 清除选中状态
            }
            break;
        }

        return true; // 返回true表示消费了事件
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onDraw
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   绘制时基选择器视图
     *   以网格形式绘制所有选项，选中项高亮显示
     * 
     * 【参数说明】
     *   @param canvas 画布对象
     * 
     * 【绘制流程】
     *   1. 计算每个选项的位置
     *   2. 绘制选项背景
     *   3. 绘制选项文本
     *   4. 处理选中状态的高亮
     */
    @SuppressLint("ResourceAsColor") // 抑制资源颜色警告
    @Override
    protected void onDraw(Canvas canvas) { // 绘制视图

        float x = 0, y = -1 * MRadioButton.HEIGHT; // 初始化绘制坐标
        for (int i = 0; i < list.size(); i++) { // 遍历所有选项
            if (i % MRadioButton.LENGTH == 0) { // 如果是新的一行
                y += MRadioButton.HEIGHT + 1; // 更新Y坐标
                x = 1; // 重置X坐标
            }

            if (currItemIndex == i) { // 如果是当前选中项
                paint.setColor(list.get(i).selectBackColor); // 设置选中背景色
                int reviseX = x == 1 ? -1 : 0; // 计算修正值
                canvas.drawRect(x, y, x + MRadioButton.WIDTH + reviseX, y + MRadioButton.HEIGHT, paint); // 绘制选中背景
                paint.setStyle(Paint.Style.STROKE); // 设置描边样式

                paint.setColor(list.get(i).selectBorderColor); // 设置选中边框色
            } else { // 如果不是选中项
                paint.setColor(list.get(i).backColor); // 设置普通背景色
            }
            int reviseX = x == 1 ? -1 : 0; // 计算修正值
            canvas.drawRect(x, y, x + MRadioButton.WIDTH + reviseX, y + MRadioButton.HEIGHT, paint); // 绘制选项背景
            list.get(i).setRadioButtonRect(x, y); // 设置选项的矩形区域
            if (currItemIndex == i) { // 如果是选中项
                paint.setColor(list.get(i).selectTextColor); // 设置选中文本色
            } else { // 如果不是选中项
                paint.setColor(list.get(i).textColor); // 设置普通文本色
            }
            paint.setStyle(Paint.Style.FILL); // 设置填充样式
            paint.setShader(null); // 清除着色器
            paint.getTextBounds(list.get(i).mText, 0, list.get(i).mText.length(), rectText); // 测量文本边界
            canvas.drawText(list.get(i).mText, x + (MRadioButton.WIDTH - rectText.width()) / 2, (MRadioButton.HEIGHT - rectText.height()) / 2 + rectText.height() + y, paint); // 绘制文本

            x += MRadioButton.WIDTH + 1 + reviseX; // 更新X坐标
        }
        if (x < getWidth() && list.size() > 0) { // 如果还有剩余空间
            paint.setColor(list.get(0).backColor); // 设置背景色
            for (; x < getWidth(); ) { // 填充剩余空间
                float newX = (x + MRadioButton.WIDTH); // 计算新X坐标
                canvas.drawRect(x, y, newX, y + MRadioButton.HEIGHT, paint); // 绘制空白区域
                x = newX + 1; // 更新X坐标
            }
        }

        paint.setColor(list.get(0).backColor); // 设置背景色
        canvas.drawRect(this.getWidth() - 2, 0, this.getWidth(), this.getHeight(), paint); // 绘制右边框

    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                      MRadioButton - 时基选项数据模型                          ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 【模块定位】                                                                  ║
     * ║   UI组件库 > 自定义控件 > 时基选择器 > 选项模型                               ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 【核心职责】                                                                  ║
     * ║   1. 存储单个时基选项的数据                                                  ║
     * ║   2. 管理选项的选中状态                                                      ║
     * ║   3. 提供触摸检测功能                                                        ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 【成员变量】                                                                  ║
     * ║   LENGTH: 每行最大选项数（12个）                                              ║
     * ║   WIDTH: 单个选项宽度（自动计算）                                             ║
     * ║   HEIGHT: 单个选项高度（80像素）                                              ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    class MRadioButton { // 时基选项数据模型
        public final static int LENGTH = 12; // 每行最大选项数
        public final static float WIDTH = ((1803 - (LENGTH + 1)) / LENGTH); // 单个选项宽度（自动计算）
        public final static int HEIGHT = 80; // 单个选项高度

        // 颜色配置
        public int backColor = Color.rgb(0, 0, 0x0C); // 普通背景色
        public int selectBorderColor = Color.parseColor("#02608d"); // 选中边框色
        public int textColor = Color.parseColor("#026998"); // 普通文本色
        public int selectTextColor = Color.WHITE; // 选中文本色
        public int selectBackColor = Color.BLACK; // 选中背景色

        // 状态和数据
        private boolean mIsUiChecked; // 是否选中
        private String mText; // 选项文本
        private int mIndex; // 选项索引
        private RectF radioButtonRect = new RectF(); // 选项矩形区域

        /**
         * ═══════════════════════════════════════════════════════════════════════════
         * 构造方法
         * ═══════════════════════════════════════════════════════════════════════════
         * 【功能说明】
         *   创建时基选项实例
         * 
         * 【参数说明】
         *   @param isUiChecked 初始选中状态
         *   @param text 选项文本
         *   @param index 选项索引
         */
        MRadioButton(boolean isUiChecked, String text, int index) { // 构造方法
            mIsUiChecked = isUiChecked; // 设置选中状态
            mText = text; // 设置文本
            mIndex = index; // 设置索引
            textColor = getResources().getColor(R.color.textColor); // 从资源获取文本色
            backColor = getResources().getColor(R.color.bg_main_outside); // 从资源获取背景色
            selectTextColor = getResources().getColor(R.color.textColor); // 从资源获取选中文本色
            selectBorderColor = getResources().getColor(R.color.bgNewRightViewSelect); // 从资源获取选中边框色
            selectBackColor = getResources().getColor(R.color.color_Backcolor_MainMenu2); // 从资源获取选中背景色
        }

        /**
         * ═══════════════════════════════════════════════════════════════════════════
         * 方法：setRadioButtonRect
         * ═══════════════════════════════════════════════════════════════════════════
         * 【功能说明】
         *   设置选项的矩形区域
         * 
         * 【参数说明】
         *   @param x 左上角X坐标
         *   @param y 左上角Y坐标
         */
        public void setRadioButtonRect(float x, float y) { // 设置矩形区域
            radioButtonRect.set(x, y, x + MRadioButton.WIDTH, y + MRadioButton.HEIGHT); // 设置矩形坐标
        }

        /**
         * ═══════════════════════════════════════════════════════════════════════════
         * 方法：setChecked
         * ═══════════════════════════════════════════════════════════════════════════
         * 【功能说明】
         *   设置选中状态
         * 
         * 【参数说明】
         *   @param checked 是否选中
         */
        public void setChecked(boolean checked) { // 设置选中状态
            this.mIsUiChecked = checked; // 更新选中状态
        }

        /**
         * ═══════════════════════════════════════════════════════════════════════════
         * 方法：contains
         * ═══════════════════════════════════════════════════════════════════════════
         * 【功能说明】
         *   检测指定坐标是否在选项区域内
         * 
         * 【参数说明】
         *   @param x X坐标
         *   @param y Y坐标
         * 
         * 【返回值】
         *   @return true如果在区域内，false否则
         */
        public boolean contains(int x, int y) { // 检测坐标是否在区域内
            return radioButtonRect.contains(x, y); // 返回检测结果
        }

        /**
         * ═══════════════════════════════════════════════════════════════════════════
         * 方法：getSelectShader
         * ═══════════════════════════════════════════════════════════════════════════
         * 【功能说明】
         *   获取选中状态的径向渐变着色器
         * 
         * 【参数说明】
         *   @param x 渐变中心X坐标
         *   @param y 渐变中心Y坐标
         * 
         * 【返回值】
         *   @return 径向渐变着色器实例
         */
        public Shader getSelectShader(float x, float y) { // 获取选中着色器
            Shader selectShader = new RadialGradient(x, y, WIDTH / 3 * 2, // 创建径向渐变
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, selectBackColor}, null, Shader.TileMode.REPEAT); // 渐变颜色数组
            return selectShader; // 返回着色器
        }
    }

}
