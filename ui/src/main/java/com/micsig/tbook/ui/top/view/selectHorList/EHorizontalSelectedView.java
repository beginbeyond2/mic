package com.micsig.tbook.ui.top.view.selectHorList;  // 包声明：定义类的命名空间

import android.content.Context;                      // Android上下文环境，用于访问系统资源和能力
import android.content.res.TypedArray;               // 类型化数组，用于读取XML自定义属性
import android.graphics.Canvas;                      // 画布类，用于绘制自定义View的内容
import android.graphics.Color;                       // 颜色类，用于定义颜色值
import android.graphics.Paint;                       // 画笔类，用于绑定绘制样式和颜色
import android.graphics.Rect;                        // 矩形类，用于测量文本边界
import android.graphics.drawable.StateListDrawable;  // 状态列表绑定，用于根据状态显示不同绑定
import android.util.AttributeSet;                    // 属性集，用于读取XML属性
import android.view.KeyEvent;                        // 按键事件，用于处理键盘输入
import android.view.MotionEvent;                     // 触摸事件，用于处理用户触摸操作
import android.view.View;                            // 视图基类，所有UI组件的父类

import com.micsig.base.Logger;                       // 日志工具类，用于输出调试信息
import com.micsig.tbook.scope.Scope;                 // 作用域类，用于数据绑定
import com.micsig.tbook.scope.ScopeMessage;          // 作用域消息类，用于消息传递
import com.micsig.tbook.ui.R;                        // 资源类，用于访问应用资源

import java.util.ArrayList;                          // 动态数组类，用于存储数据列表
import java.util.List;                               // 列表接口，定义列表操作规范

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        EHorizontalSelectedView                                ║
 * ║                        水平滚动选择视图                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块定位: UI组件层 > 选择器组件 > 水平选择器                                  ║
 * ║  核心职责: 提供水平滚动选择功能，支持触摸滑动和按键选择                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                    ║
 * ║    - 继承自View，实现自定义绘制                                               ║
 * ║    - 支持触摸滑动和按键两种交互方式                                           ║
 * ║    - 通过回调接口通知外部选择结果                                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║    setData() → 内部数据存储 → onDraw()绘制 → 用户交互 → 回调通知             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                    ║
 * ║    - Android SDK (View, Canvas, Paint等)                                      ║
 * ║    - com.micsig.base.Logger (日志输出)                                        ║
 * ║    - com.micsig.tbook.ui.R (资源引用)                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用示例:                                                                    ║
 * ║    EHorizontalSelectedView view = new EHorizontalSelectedView(context);      ║
 * ║    view.setData(Arrays.asList("选项1", "选项2", "选项3"));                    ║
 * ║    view.setOnRollingListener((pos, text) -> {                                ║
 * ║        // 处理选择回调                                                        ║
 * ║    });                                                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author micsig
 * @version 1.0.0
 * @since 2024-01-01
 */
public class EHorizontalSelectedView extends View {
    
    // ==================== 常量定义 ====================
    
    /** 日志标签，用于标识当前类的日志输出 */
    private static final String TAG = "EHorizontalSelectedView";
    
    // ==================== 成员变量 ====================
    
    /** Android上下文对象，用于访问系统资源和能力 */
    private Context mContext;
    
    /** 非选中项画笔，用于绘制非选中状态的文本 */
    private Paint mOtherPaint;
    
    /** 选中项画笔，用于绘制选中状态的文本（通常有不同颜色和大小） */
    private Paint mSelectPaint;
    
    /** 背景画笔，用于绘制选中区域和非选中区域的背景 */
    private Paint bgPaint;
    
    /** 选择器显示的数据列表，存储所有可选项目 */
    private List<String> data = new ArrayList<>();
    
    /** 可见数量，表示屏幕上同时显示的项目数量 */
    private int seeSize;
    
    /** 文本边界矩形，用于测量文本的宽度和高度 */
    private Rect mRect = new Rect();
    
    /** 当前选中位置的索引，从0开始计数 */
    private int selectIndex = 0;
    
    /** 手指按下时的X坐标，用于计算滑动距离 */
    private float downX;
    
    /** 是否为点击操作的标志，true表示点击，false表示滑动 */
    private boolean isClick = false;
    
    /** 每个项目的宽度（像素），根据View宽度和可见数量计算 */
    private int mItemWidth;
    
    /** 滑动偏移量，用于实现平滑滚动效果 */
    private float mOffset;
    
    /** 选中项文本颜色 */
    private int selectColor;
    
    /** 非选中项文本颜色 */
    private int otherColor;
    
    /** 选中区域背景颜色资源ID */
    private int selectBackground;
    
    /** 非选中区域背景颜色资源ID */
    private int otherBackground;
    
    /** 非选中项文本大小（像素） */
    private float otherTextSize;
    
    /** 选中项文本大小（像素） */
    private float selectTextSize;
    
    // ==================== 构造方法 ====================
    
    /**
     * 单参数构造方法
     * <p>用于在代码中动态创建视图实例</p>
     * 
     * @param context Android上下文对象
     */
    public EHorizontalSelectedView(Context context) {
        this(context, null);  // 调用双参数构造方法
    }

    /**
     * 双参数构造方法
     * <p>用于在XML布局文件中定义视图时自动调用</p>
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集，包含布局文件中定义的属性
     */
    public EHorizontalSelectedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造方法
    }

    /**
     * 三参数构造方法（完整构造方法）
     * <p>支持自定义默认样式，完成视图的初始化工作</p>
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集，包含布局文件中定义的属性
     * @param defStyleAttr 默认样式属性，用于定义视图的默认样式
     */
    public EHorizontalSelectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造方法
        this.mContext = context;  // 保存上下文引用
        initAttrs(attrs);  // 初始化自定义属性
        initPaint();  // 初始化画笔对象
    }
    
    // ==================== 初始化方法 ====================
    
    /**
     * 初始化自定义属性
     * <p>从XML属性集中读取自定义属性值，包括文本大小、颜色、可见数量等</p>
     * 
     * @param attrs XML属性集
     */
    private void initAttrs(AttributeSet attrs) {
        // 获取类型化数组，用于读取自定义属性
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.EHorizontalSelectedView);
        
        // 读取非选中项文本大小，默认20像素
        otherTextSize = typedArray.getDimension(R.styleable.EHorizontalSelectedView_otherTextSize, 20);
        
        // 读取选中项文本大小，默认20像素
        selectTextSize = typedArray.getDimension(R.styleable.EHorizontalSelectedView_selectTextSize, 20);
        
        // 读取可见数量，默认为1
        seeSize = typedArray.getInteger(R.styleable.EHorizontalSelectedView_seeSize, 1);
        
        // 读取非选中项文本颜色，默认红色
        otherColor = typedArray.getColor(R.styleable.EHorizontalSelectedView_otherColor, Color.RED);
        
        // 读取选中项文本颜色，默认黄色
        selectColor = typedArray.getColor(R.styleable.EHorizontalSelectedView_selectColor, Color.YELLOW);
        
        // 设置非选中区域背景颜色资源ID
        otherBackground = R.color.bg_topmenu_color;
        
        // 设置选中区域背景颜色资源ID
        selectBackground = R.color.color_Backcolor_MainMenu2;
        
        // 回收类型化数组，释放资源
        typedArray.recycle();
    }

    /**
     * 初始化画笔对象
     * <p>创建并配置选中项画笔、非选中项画笔和背景画笔</p>
     */
    private void initPaint() {
        // 创建非选中项画笔
        mOtherPaint = new Paint();
        mOtherPaint.setAntiAlias(true);  // 启用抗锯齿，使文本更平滑
        mOtherPaint.setTextSize(otherTextSize);  // 设置文本大小
        mOtherPaint.setColor(otherColor);  // 设置文本颜色

        // 创建选中项画笔
        mSelectPaint = new Paint();
        mSelectPaint.setAntiAlias(true);  // 启用抗锯齿
        mSelectPaint.setColor(selectColor);  // 设置文本颜色
        mSelectPaint.setTextSize(selectTextSize);  // 设置文本大小

        // 创建背景画笔
        bgPaint = new Paint();
    }
    
    // ==================== 绘制方法 ====================
    
    /**
     * 绑定绘制方法
     * <p>自定义View的核心方法，负责在画布上绑定绘制所有内容，包括背景和文本</p>
     * 
     * @param canvas 画布对象，用于绑定绘制图形和文本
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);  // 调用父类绘制方法
        
        // 获取View的宽度和高度
        int width = getWidth();  // 获取View宽度
        int height = getHeight();  // 获取View高度
        
        // 检查可见数量是否有效
        if (seeSize == 0) {
            return;  // 可见数量为0，直接返回不绘制
        }
        
        // 计算每个项目的宽度
        mItemWidth = width / seeSize;  // 根据可见数量平分宽度
        
        // 遍历所有数据项，找出最宽的文本
        int tmp = 0;  // 临时变量，存储最大文本宽度
        for (String datum : data) {
            // 获取当前文本的边界
            mOtherPaint.getTextBounds(datum, 0, datum.length(), mRect);
            int textWidth = mRect.width();  // 获取文本宽度
            // 更新最大宽度
            if (textWidth > tmp) {
                tmp = textWidth;
            }
        }
        
        // 修正项目宽度，确保能容纳最宽的文本
        mItemWidth = Math.max(mItemWidth, tmp);  // 取计算宽度和最大文本宽度中的较大值
        
        // 重新计算可见数量，确保不超出View宽度
        seeSize = width / mItemWidth;
        
        // 绘制背景区域（分为左、中、右三部分）
        // 绘制左侧非选中区域背景
        bgPaint.setColor(getResources().getColor(otherBackground));  // 设置非选中背景色
        canvas.drawRect(0, 0, mItemWidth * (seeSize - 1) / 2, height, bgPaint);  // 绘制左侧矩形
        
        // 绘制中间选中区域背景
        bgPaint.setColor(getResources().getColor(selectBackground));  // 设置选中背景色
        canvas.drawRect(mItemWidth * (seeSize - 1) / 2, 0, mItemWidth * (seeSize + 1) / 2, height, bgPaint);  // 绘制中间矩形
        
        // 绘制右侧非选中区域背景
        bgPaint.setColor(getResources().getColor(otherBackground));  // 设置非选中背景色
        canvas.drawRect(mItemWidth * (seeSize + 1) / 2, 0, width, height, bgPaint);  // 绘制右侧矩形
        
        // 遍历所有数据项，绘制文本
        for (int j = 0; j < data.size(); j++) {
            String datum = data.get(j);  // 获取当前项文本
            
            // 获取文本边界
            mOtherPaint.getTextBounds(datum, 0, datum.length(), mRect);
            int textWidth = mRect.width();  // 文本宽度
            int textHeight = mRect.height();  // 文本高度
            
            // 根据是否为选中项，使用不同的绘制方式
            if (j != selectIndex) {
                // 绘制非选中项
                if (j < selectIndex) {
                    // 当前项在选中项左侧
                    int a = selectIndex - j;  // 计算与选中项的距离
                    // 计算绘制位置：中心位置 - 文本宽度/2 - 距离*项目宽度 + 偏移量
                    canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 - a * mItemWidth + mOffset, height / 2 + textHeight / 2, mOtherPaint);
                } else {
                    // 当前项在选中项右侧
                    int a = j - selectIndex;  // 计算与选中项的距离
                    // 计算绘制位置：中心位置 - 文本宽度/2 + 距离*项目宽度 + 偏移量
                    canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 + a * mItemWidth + mOffset, height / 2 + textHeight / 2, mOtherPaint);
                }
            } else {
                // 绘制选中项（使用选中画笔）
                canvas.drawText(datum, mItemWidth * seeSize / 2 - textWidth / 2 + mOffset, height / 2 + textHeight / 2, mSelectPaint);
            }
        }
    }
    
    // ==================== 触摸事件处理 ====================
    
    /**
     * 触摸事件处理方法
     * <p>处理用户的触摸操作，包括按下、移动和抬起，实现滑动选择和点击选择功能</p>
     * 
     * @param event 触摸事件对象，包含触摸位置和动作类型
     * @return true表示消费了事件，false表示不处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 根据动作类型分别处理
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  // 手指按下
                downX = event.getX();  // 记录按下的X坐标
                isClick = true;  // 初始标记为点击操作
                break;
                
            case MotionEvent.ACTION_MOVE:  // 手指移动
                float scrollX = event.getX();  // 获取当前X坐标
                mOffset = scrollX - downX;  // 计算偏移量
                
                // 判断是否为滑动操作（移动距离超过10像素）
                if (Math.abs(mOffset) > 10) {
                    isClick = false;  // 标记为滑动操作
                }
                
                // 处理向右滑动（选择前一项）
                if (scrollX > downX) {
                    // 如果滑动距离大于半个项目宽度，则切换到前一项
                    if (scrollX - downX >= mItemWidth / 2) {
                        if (selectIndex > 0) {  // 确保不是第一项
                            mOffset = mOffset - mItemWidth;  // 调整偏移量
                            selectIndex = selectIndex - 1;  // 选中索引减1
                            downX = downX + mItemWidth;  // 更新参考点
                            // 触发滚动回调
                            if (mOnRollingListener != null) {
                                mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                            }
                        }
                    }
                } else {
                    // 处理向左滑动（选择后一项）
                    if (downX - scrollX >= mItemWidth / 2) {
                        if (selectIndex < data.size() - 1) {  // 确保不是最后一项
                            mOffset = mOffset + mItemWidth;  // 调整偏移量
                            selectIndex = selectIndex + 1;  // 选中索引加1
                            downX = downX - mItemWidth;  // 更新参考点
                            // 触发滚动回调
                            if (mOnRollingListener != null) {
                                mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));
                            }
                        }
                    }
                }
                invalidate();  // 请求重绘
                break;
                
            case MotionEvent.ACTION_UP:  // 手指抬起
                if (isClick) {
                    // 处理点击操作
                    float moveX = event.getX() - getMeasuredWidth() / 2;  // 计算点击位置相对于中心的偏移
                    int moveCount = 0;  // 需要移动的项目数
                    
                    if (Math.abs(moveX) < mItemWidth / 2) {
                        // 点击位置在中心区域，不做处理
                        break;
                    } else if (moveX > 0) {
                        // 点击位置在右侧，计算需要向后移动的项目数
                        moveCount = (int) ((moveX - mItemWidth / 2) / mItemWidth + 1);
                        // 确保不超出数据范围
                        if (selectIndex + moveCount > data.size() - 1) {
                            selectIndex = data.size() - 1;
                        } else {
                            selectIndex = selectIndex + moveCount;
                        }
                    } else if (moveX < 0) {
                        // 点击位置在左侧，计算需要向前移动的项目数
                        moveCount = (int) ((-1 * moveX - mItemWidth / 2) / mItemWidth + 1) * -1;
                        // 确保不超出数据范围
                        if (selectIndex + moveCount < 0) {
                            selectIndex = 0;
                        } else {
                            selectIndex = selectIndex + moveCount;
                        }
                    }
                    // 触发点击回调
                    if (mOnRollingListener != null) {
                        mOnRollingListener.onClick(selectIndex, data.get(selectIndex));
                    }
                }
                // 重置偏移量，实现回弹效果
                mOffset = 0;
                invalidate();  // 请求重绘
                break;
                
            default:
                break;
        }
        return true;  // 消费触摸事件
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 获取当前选中位置
     * 
     * @return 当前选中项的索引位置
     */
    public int getSelectIndex() {
        return selectIndex;
    }

    /**
     * 设置当前选中位置
     * <p>如果指定的索引超出数据范围，将自动调整到有效范围内</p>
     * 
     * @param selectIndex 要设置的选中位置索引
     */
    public void setSelectIndex(int selectIndex) {
        // 边界检查，确保索引在有效范围内
        if (selectIndex > data.size()) {
            selectIndex = data.size() - 1;  // 超出范围则设置为最后一项
        }
        this.selectIndex = selectIndex;  // 更新选中索引
        invalidate();  // 请求重绘
    }

    /**
     * 设置选中项文本颜色
     * 
     * @param color 颜色值
     */
    public void setSelectTextColor(int color) {
        this.selectColor = color;  // 更新选中颜色
        invalidate();  // 请求重绘
    }

    /**
     * 获取选中项文本颜色
     * 
     * @return 选中项的颜色值
     */
    public int getSelectColor() {
        return selectColor;
    }

    /**
     * 设置非选中项文本颜色
     * 
     * @param color 颜色值
     */
    public void setOtherTextColor(int color) {
        this.otherColor = color;  // 更新非选中颜色
        invalidate();  // 请求重绘
    }

    /**
     * 获取当前选中项的文本内容
     * 
     * @return 选中项的文本字符串
     */
    public String getSelectText() {
        return data.get(selectIndex);
    }

    /**
     * 设置选择器数据
     * <p>设置数据后会自动获取焦点，以便响应按键事件</p>
     * 
     * @param data 数据列表
     */
    public void setData(List<String> data) {
        this.data = data;  // 保存数据引用
        setFocusable(true);  // 设置可获取焦点
        setFocusableInTouchMode(true);  // 设置触摸模式下可获取焦点
        requestFocus();  // 请求获取焦点
        invalidate();  // 请求重绘
    }

    /**
     * 设置可见数量
     * 
     * @param seeSize 可见的项目数量
     */
    public void setSeeSize(int seeSize) {
        this.seeSize = seeSize;  // 更新可见数量
        invalidate();  // 请求重绘
    }
    
    // ==================== 回调接口 ====================
    
    /** 滚动监听器实例 */
    private OnRollingListener mOnRollingListener;

    /**
     * 设置滚动监听器
     * 
     * @param onRollingListener 监听器实例
     */
    public void setOnRollingListener(OnRollingListener onRollingListener) {
        mOnRollingListener = onRollingListener;
    }

    /**
     * 滚动监听接口
     * <p>定义了滚动和点击事件的回调方法</p>
     */
    public interface OnRollingListener {
        /**
         * 滚动事件回调
         * <p>当用户通过滑动或按键改变选中项时触发</p>
         * 
         * @param position 新的选中位置索引
         * @param s 选中项的文本内容
         */
        void onRolling(int position, String s);

        /**
         * 点击事件回调
         * <p>当用户点击某个项目时触发</p>
         * 
         * @param position 点击的位置索引
         * @param s 点击项的文本内容
         */
        void onClick(int position, String s);
    }

    /**
     * 设置非选中项文本大小
     * 
     * @param otherTextSize 文本大小（像素）
     */
    public void setOtherTextSize(float otherTextSize) {
        this.otherTextSize = otherTextSize;
    }

    /**
     * 设置选中项文本大小
     * 
     * @param selectTextSize 文本大小（像素）
     */
    public void setSelectTextSize(float selectTextSize) {
        this.selectTextSize = selectTextSize;
    }
    
    // ==================== 按键事件处理 ====================

    /**
     * 按键事件处理方法
     * <p>处理方向键和确认键，实现键盘导航选择功能</p>
     * 
     * @param keyCode 按键代码
     * @param event 按键事件对象
     * @return true表示消费了事件，false表示不处理
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.i(TAG, "keyCode= " + keyCode);  // 输出按键日志
        
        // 根据按键代码分别处理
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:  // 方向键左
            case KeyEvent.KEYCODE_DPAD_UP:    // 方向键上
                // 向前选择（选择前一项）
                if (mOnRollingListener != null) {
                    if (selectIndex >= 1) {  // 确保不是第一项
                        selectIndex = selectIndex - 1;  // 选中索引减1
                        mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));  // 触发回调
                        invalidate();  // 请求重绘
                    }
                }
                return true;  // 消费事件
                
            case KeyEvent.KEYCODE_DPAD_RIGHT:  // 方向键右
            case KeyEvent.KEYCODE_DPAD_DOWN:   // 方向键下
                // 向后选择（选择后一项）
                if (selectIndex < data.size() - 1) {  // 确保不是最后一项
                    selectIndex = selectIndex + 1;  // 选中索引加1
                    mOnRollingListener.onRolling(selectIndex, data.get(selectIndex));  // 触发回调
                    invalidate();  // 请求重绘
                }
                return true;  // 消费事件
                
            case KeyEvent.KEYCODE_ENTER:        // 回车键
            case KeyEvent.KEYCODE_NUMPAD_ENTER: // 小键盘回车键
                // 确认选择，隐藏选择器
                if (getParent().getParent() instanceof TopViewSelectHorListToList) {
                    ((TopViewSelectHorListToList) getParent().getParent()).hide();  // 调用父容器的隐藏方法
                }
                return true;  // 消费事件
                
            default:
                // 其他按键交给父类处理
                return super.onKeyDown(keyCode, event);
        }
    }
}
