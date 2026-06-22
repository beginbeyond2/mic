package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.graphics.Canvas;  // 画布类，用于自定义绘制
import android.text.TextPaint;  // 文本画笔类
import android.util.AttributeSet;  // XML属性集类

import androidx.annotation.Nullable;  // 可空注解
import androidx.appcompat.widget.AppCompatTextView;  // 兼容性文本视图基类

import com.micsig.tbook.ui.util.ScreenUtil;  // 屏幕工具类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                               MTextView                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 自定义文本视图控件                                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承AppCompatTextView，提供自定义文本绘制功能                              │
 * │   2. 支持多行文本的精确位置绘制                                                 │
 * │   3. 支持换行符(\n)分隔的多行文本显示                                           │
 * │   4. 实现右对齐的文本绘制方式                                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │ AppCompatTextView│  ← 继承自兼容性文本视图                                 │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐                                                       │
 * │   │    MTextView    │  ← 重写onDraw实现自定义文本绘制                          │
 * │   └─────────────────┘                                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   getText() → 文本测量 → 分割处理 → Canvas绘制                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - AppCompatTextView: 基类，提供文本视图基础功能                               │
 * │   - ScreenUtil: 屏幕工具类，用于文本尺寸测量                                    │
 * │   - Canvas: 画布类，用于绘制文本                                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MTextView                                            │
 * │       android:id="@+id/text_view"                                           │
 * │       android:layout_width="wrap_content"                                   │
 * │       android:layout_height="wrap_content"                                  │
 * │       android:text="第一行\n第二行" />                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 注意事项:                                                                     │
 * │   - 文本绘制采用右对齐方式                                                     │
 * │   - 多行文本使用\n作为分隔符                                                   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MTextView extends AppCompatTextView {
    
    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MTextView(Context context) {
        this(context, null);  // 调用双参数构造
    }

    /**
     * 双参数构造方法
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     */
    public MTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造
    }

    /**
     * 三参数构造方法（完整构造）
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造方法
    }

    // =========================== 绘制方法 ===========================
    
    /**
     * 自定义绘制方法
     * 重写onDraw实现多行文本的右对齐绘制
     * 
     * @param canvas 画布对象，用于绘制文本
     * 
     * 绘制逻辑:
     * 1. 获取文本内容和画笔
     * 2. 测量控件尺寸和文本尺寸
     * 3. 检查是否包含换行符
     * 4. 单行文本：右对齐居中绘制
     * 5. 多行文本：逐行计算位置并绘制
     */
    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();  // 获取文本内容
        TextPaint paint = getPaint();  // 获取文本画笔
        paint.setColor(getTextColors().getDefaultColor());  // 设置文本颜色
        int measureWidth = getMeasuredWidth();  // 获取控件测量宽度
        int measureHeight = getMeasuredHeight();  // 获取控件测量高度
        int textWidth = ScreenUtil.getTextWidth3(paint, text);  // 测量文本宽度
        int textHeight = ScreenUtil.getTextHeight(paint, text);  // 测量文本高度
        if (text.contains("\n")) {  // 检查是否包含换行符（多行文本）
            String[] strings = text.split("\n");  // 按换行符分割文本
            int itemHeight = ScreenUtil.getTextHeight(paint, "Sp");  // 获取单行文本高度
            for (int i = 0; i < strings.length; i++) {  // 遍历每一行
                textWidth = ScreenUtil.getTextWidth3(paint, strings[i]);  // 测量当前行文本宽度
                canvas.drawText(strings[i], measureWidth - textWidth, measureHeight / 2 + itemHeight / 2 * (-1 * strings.length + (i + 1) * 2) + i * 5 - 5, paint);  // 绘制当前行文本，计算Y坐标实现垂直居中
            }
        } else {  // 单行文本
            canvas.drawText(text, measureWidth - textWidth, measureHeight / 2 + textHeight / 2, paint);  // 右对齐居中绘制文本
        }
    }
}
