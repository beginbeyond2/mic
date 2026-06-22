package com.micsig.tbook.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
 *                                  TopViewNumberPicker - 数字选择器控件
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 示波器UI组件模块，用于数值输入的自定义滚轮选择器控件。
 *
 * 【核心职责】
 * 1. 显示两组数值（主数值和辅助数值）
 * 2. 支持触摸滑动选择数字
 * 3. 支持展开/收起交互效果
 * 4. 自动处理正负号切换
 * 5. 提供数值变化回调
 *
 * 【架构设计】
 * 继承自View，采用自定义绘制和触摸处理：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                  TopViewNumberPicker                             │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │ 数据层      │  │ 交互层      │  │ 渲染层                  │  │
 * │  │ s1, s2     │  │ onTouchEvent│  │ onDraw                 │  │
 * │  │ isExpand   │  │ changeShow  │  │ drawSelectBorder       │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据格式】
 * - 主数值(s1): 格式为"±X.XXXX"，共6位（含小数点）
 * - 辅助数值(s2): 格式为"±X"，共1位
 * - 例如: s1="+5.6789", s2="-1"
 *
 * 【交互流程】
 * 1. 点击数字位 → 展开选择器
 * 2. 滑动选择 → 实时预览
 * 3. 松开手指 → 确认选择并回调
 * 4. 3秒无操作 → 自动收起
 *
 * 【依赖关系】
 * - View: Android视图基类
 * - OnNumberPickerListener: 数值变化监听接口
 *
 * 【使用示例】
 * TopViewNumberPicker picker = new TopViewNumberPicker(context);
 * picker.setData("5.6789", "-1", (s1, s2) -> {
 *     // 处理数值变化
 *     Log.d("Picker", "s1=" + s1 + ", s2=" + s2);
 * });
 *
 * 【注意事项】
 * 1. 数值格式必须符合要求，否则会自动添加正号
 * 2. 展开状态下，滑动距离会自动对齐到整数倍itemHeight
 * 3. 正负号位只能切换正负，数字位可滚动选择0-9
 * 4. 控件高度固定为itemHeight * 10
 *
 * @author micsig
 * @version 1.0
 */
public class TopViewNumberPicker extends View {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 上下文与画笔
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 应用上下文 */
    private Context context;

    /** 绘制用的画笔 */
    private Paint paint;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 数据与状态
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 当前展示的列表索引，-1表示不展示 */
    private int isExpand = -1;

    /** 单个项目的宽度和高度 */
    private int itemWidth, itemHeight;

    /** 间距宽度 */
    private int spaceWidth;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 颜色资源
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 分隔线颜色 */
    private int dividerColor, bgColor, textColor;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 显示内容
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 主数值（格式：±X.XXXX） */
    private String s1, s2;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 布局参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示区域矩形 */
    private Rect showRect, expandRect;

    /** 数值变化监听器 */
    private OnNumberPickerListener onNumberPickerListener;

    /** 项目内边距 */
    private int margin=9;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 数值变化监听接口
     */
    public interface OnNumberPickerListener {
        /**
         * 当数值发生变化时回调
         *
         * @param s1 主数值（不含正号）
         * @param s2 辅助数值（不含正号）
         */
        void onChangedShow(String s1, String s2);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 简单构造方法
     *
     * @param context 应用上下文
     */
    public TopViewNumberPicker(Context context) {
        this(context, null);
    }

    /**
     * XML属性构造方法
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     */
    public TopViewNumberPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 完整构造方法
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopViewNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 初始化视图
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        // 设置默认数值
        s1 = "-5.6789";
        s2 = "-1";
        margin = 8;

        // 设置项目尺寸
        itemWidth = 64 + margin;
        itemHeight = 64 + margin;
        spaceWidth = 16;

        // 获取颜色资源
        dividerColor = context.getResources().getColor(R.color.scaleDivider);
        bgColor = context.getResources().getColor(R.color.bgDialog);
        textColor = context.getResources().getColor(R.color.textColorNewRightViewEnable);

        // 初始化画笔
        paint = new Paint();
        paint.setTextSize(22);
        paint.setAntiAlias(true);

        // 初始化矩形
        showRect = new Rect(0, 0, 0, 0);
        expandRect = new Rect(0, 0, 0, 0);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制视图
     *
     * @param canvas 画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 如果需要更新显示，先执行更新
        if (isChangeShow) {
            changeShow();
        }

        // 合并数值字符串（去除小数点）
        String s = (s1 + s2).replace(".", "");
        Logger.d("onDraw1:" + s);

        // 获取文本尺寸
        int textWidth = getTextWidth("0");
        int textHeight = getTextHeight("0");

        // 绘制显示区域边框
        paint.setColor(dividerColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        showRect = new Rect(1, itemHeight * 5, itemWidth * 8 + spaceWidth, itemHeight * 6);

        // 绘制主数值区域边框
        canvas.drawRect(showRect.left, showRect.top, showRect.left + itemWidth * 6 , showRect.bottom, paint);
        // 绘制辅助数值区域边框
        canvas.drawRect(showRect.right - itemWidth * 2, showRect.top, showRect.right, showRect.bottom, paint);

        // 填充背景色
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        canvas.drawRect(showRect.left, showRect.top, showRect.left + itemWidth * 6 , showRect.bottom, paint);
        canvas.drawRect(showRect.right - itemWidth * 2, showRect.top, showRect.right , showRect.bottom, paint);

        // 绘制每个数字位
        for (int i = 0; i < 8; i++) {
            String text = String.valueOf(s.charAt(i));
            int x;
            int y = itemHeight * 6 - itemHeight / 2 + textHeight / 2;

            // 计算X坐标（辅助数值需要加间距）
            if (i >= 6) {
                x = itemWidth * i + itemWidth / 2 - textWidth / 2 + spaceWidth;
            } else {
                x = itemWidth * i + itemWidth / 2 - textWidth / 2;
            }

            // 绘制数字
            paint.setColor(textColor);
            canvas.drawText(text, x, y, paint);

            // 在第1位后绘制小数点
            if (i == 1) {
                canvas.drawText(".", x + textWidth + 5, y, paint);
            }

            // 如果当前位展开，绘制选择器
            if (isExpand == i) {
                if (i == 0 || i == 6) {
                    // 正负号位：只显示+和-两个选项
                    paint.setColor(dividerColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);

                    if (i >= 6) {
                        expandRect = new Rect(itemWidth * i + spaceWidth, itemHeight * 4, itemWidth * (i + 1) + spaceWidth, itemHeight * 6);
                    } else {
                        expandRect = new Rect(itemWidth * i, itemHeight * 4, itemWidth * (i + 1), itemHeight * 6);
                    }

                    canvas.drawRect(expandRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(bgColor);

                    if (i >= 6) {
                        canvas.drawRect(expandRect.left, expandRect.top, expandRect.right, expandRect.bottom, paint);
                    } else {
                        canvas.drawRect(expandRect.left + 1, expandRect.top, expandRect.right, expandRect.bottom, paint);
                    }

                    // 绘制选中边框
                    drawSelectBorder(canvas,i);

                    // 绘制正负号选项
                    paint.setColor(textColor);
                    String symbol = text.equals("-") ? "+" : "-";
                    String[] symbols = {symbol, text};

                    for (int j = 0; j < 50; j++) {
                        int expandY = y + itemHeight * (-25 + j) + moveY;
                        if (expandY >= itemHeight * 4 + 10 && expandY <= itemHeight * 6) {
                            canvas.drawText(symbols[j % 2], x, expandY, paint);
                        }
                    }
                } else {
                    // 数字位：显示0-9十个选项
                    paint.setColor(dividerColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);

                    if (i >= 6) {
                        expandRect = new Rect(itemWidth * i + spaceWidth, 0, itemWidth * (i + 1) + spaceWidth, itemHeight * 10 - 1);
                    } else {
                        expandRect = new Rect(itemWidth * i, 0, itemWidth * (i + 1), itemHeight * 10 - 1);
                    }

                    canvas.drawRect(expandRect, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(bgColor);

                    if (i >= 6) {
                        canvas.drawRect(expandRect.left, expandRect.top + 1, expandRect.right , expandRect.bottom, paint);
                    } else {
                        canvas.drawRect(expandRect.left, expandRect.top + 1, expandRect.right, expandRect.bottom, paint);
                    }

                    // 绘制选中边框
                    drawSelectBorder(canvas,i);

                    // 绘制数字选项
                    paint.setColor(textColor);
                    int number = Integer.valueOf(text);

                    for (int j = 0; j < 50; j++) {
                        int expandY = y + itemHeight * (-25 + j) + moveY;
                        if (expandY >= 0 && expandY <= itemHeight * 10) {
                            canvas.drawText(String.valueOf((number + 35 + j) % 10), x, expandY, paint);
                        }
                    }
                }
            }
        }
    }

    /**
     * 绘制选中项边框
     *
     * @param canvas 画布
     * @param selectedIndex 选中的索引
     */
    private void drawSelectBorder(Canvas canvas,int selectedIndex){
        Rect itemRect;

        if (selectedIndex >= 6) {
            // 辅助数值区域
            int x=itemWidth * selectedIndex + spaceWidth+margin;
            int y=itemHeight*5+margin;
            itemRect=new Rect(x,y,x+itemWidth-margin*2,y+itemHeight-margin*2);
        } else {
            // 主数值区域
            int x=itemWidth * selectedIndex+margin;
            int y=itemHeight*5+margin;
            itemRect=new Rect(x,y,x+itemWidth-margin*2,y+itemHeight-margin*2);
        }

        // 绘制黑色背景作为选中效果
        paint.setColor(Color.BLACK);
        canvas.drawRect(itemRect,paint);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置显示数据
     *
     * @param s1 主数值（格式：X.XXXX）
     * @param s2 辅助数值（格式：X）
     * @param onNumberPickerListener 数值变化监听器
     */
    public void setData(String s1, String s2, OnNumberPickerListener onNumberPickerListener) {
        // 确保数值有正负号
        if (s1.charAt(0) != '+' && s1.charAt(0) != '-') {
            s1 = "+" + s1;
        }
        if (s2.charAt(0) != '+' && s2.charAt(0) != '-') {
            s2 = "+" + s2;
        }

        this.isExpand = -1;  // 收起展开状态
        this.s1 = s1;
        this.s2 = s2;
        this.onNumberPickerListener = onNumberPickerListener;
        invalidate();
    }

    /**
     * 打开指定位置的展开选择器
     *
     * @param index 要展开的位置索引（0-7）
     */
    public void openExpand(int index) {
        this.isExpand = index;
        handler.sendEmptyMessage(MSG_EXPAND_DELAY);  // 启动自动收起计时
        invalidate();
    }

    /**
     * 增加或减少指定位置的数值
     *
     * @param index 位置索引（0-7）
     * @param isAdd true为增加，false为减少
     */
    public void addOne(int index, boolean isAdd) {
        if (isExpand != index) {
            openExpand(index);
        }
        handler.sendEmptyMessage(MSG_EXPAND_DELAY);
        moveY = isAdd ? -itemHeight : itemHeight;  // 设置移动方向
        isChangeShow = true;
        invalidate();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触摸处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查触摸点是否在显示范围内
     *
     * @param x X坐标
     * @param y Y坐标
     * @return true表示在范围内
     */
    private boolean isContain(int x, int y) {
        if (showRect.contains(x, y)) {
            return true;
        }
        if (isExpand != -1 && expandRect.contains(x, y)) {
            return true;
        }
        return false;
    }

    /**
     * 更新显示数值
     * 根据移动距离计算新的数值
     */
    private void changeShow() {
        if (isExpand != -1) {
            switch (isExpand) {
                case 0: {
                    // 主数值正负号位
                    int change = moveY / itemHeight % 2;
                    if (change == 1) {
                        if (s1.substring(0, 1).equals("+")) {
                            s1 = "-" + s1.substring(1);
                        } else if (s1.substring(0, 1).equals("-")) {
                            s1 = "+" + s1.substring(1);
                        }
                    }
                }
                break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5: {
                    // 主数值数字位
                    int change = moveY / itemHeight;
                    String s11 = s1;
                    s1 = s1.replace(".", "");
                    int number = Integer.valueOf(s1.substring(1, isExpand + 1)) - change;
                    number += 100000;  // 防止负数

                    String numberStr = String.valueOf(number);
                    if (numberStr.length() >= isExpand) {
                        numberStr = numberStr.substring(numberStr.length() - isExpand);
                    } else {
                        // 补零
                        for (int i = 0; i < isExpand - numberStr.length(); i++) {
                            numberStr = "0" + numberStr;
                        }
                    }

                    // 重组数值字符串
                    s1 = s1.substring(0, 1) + numberStr + (isExpand == 5 ? "" : s1.substring(isExpand + 1));
                    s1 = s1.substring(0, 2) + "." + s1.substring(2);  // 添加小数点
                    Logger.d("changeShow:" + s11 + "," + s1 + "," + isExpand);
                }
                break;
                case 6: {
                    // 辅助数值正负号位
                    int change = moveY / itemHeight % 2;
                    if (change == 1) {
                        if (s2.substring(0, 1).equals("+")) {
                            s2 = "-" + s2.substring(1);
                        } else if (s2.substring(0, 1).equals("-")) {
                            s2 = "+" + s2.substring(1);
                        }
                    }
                }
                break;
                case 7: {
                    // 辅助数值数字位
                    int change = moveY / itemHeight % 10;
                    int number = Integer.valueOf(s2.substring(1)) - change;
                    number = (number + 30) % 10;  // 模10循环
                    s2 = s2.substring(0, 1) + number;
                }
                break;
            }
        }

        // 回调数值变化
        if (onNumberPickerListener != null) {
            onNumberPickerListener.onChangedShow(
                    s1.replace("+", ""), s2.replace("+", ""));
        }

        Logger.d("onDraw2:" + (s1 + s2).replace(".", "") + ",moveY:" + moveY);
        isChangeShow = false;
        moveY = 0;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 触摸起始坐标 */
    int startX, startY;

    /** Y轴移动距离 */
    int moveY;

    /** 是否需要更新显示 */
    boolean isChangeShow = false;

    /**
     * 处理触摸事件
     *
     * @param event 触摸事件
     * @return true表示事件已处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检查触摸点是否在显示范围内
                if (!isContain((int) event.getX(), (int) event.getY())) {
                    return super.onTouchEvent(event);
                }

                moveY = 0;
                startX = (int) event.getX();
                startY = (int) event.getY();

                // 如果触摸展开区域，重置自动收起计时
                if (expandRect.contains(startX, startY)) {
                    handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                }

                // 如果触摸显示区域，展开对应位置
                if (showRect.contains(startX, startY)) {
                    if (startX <= itemWidth * 6) {
                        isExpand = startX / itemWidth;
                    } else if (startX >= itemWidth * 6 + spaceWidth) {
                        isExpand = (startX - spaceWidth) / itemWidth;
                    }
                    invalidate();
                }

                Logger.d("MotionEvent.ACTION_DOWN:" + moveY);
                break;

            case MotionEvent.ACTION_MOVE:
                // 处理展开区域的滑动
                if (expandRect.contains(startX, startY)) {
                    int curY = (int) event.getY();
                    moveY = curY - startY;
                    handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                    invalidate();
                }
                Logger.d("MotionEvent.ACTION_MOVE:" + moveY);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 计算最终移动距离
                if (moveY == 0) {
                    // 点击操作：根据点击位置计算移动
                    if (expandRect.contains(startX, startY)) {
                        moveY = -(((int) event.getY()) / itemHeight - 5) * itemHeight;
                    }
                } else if (moveY > 0) {
                    // 向下滑动：四舍五入到最近的整数倍
                    moveY = (moveY + itemHeight / 2) / itemHeight * itemHeight;
                } else {
                    // 向上滑动：四舍五入到最近的整数倍
                    moveY = (moveY - itemHeight / 2) / itemHeight * itemHeight;
                }

                Logger.d("MotionEvent.ACTION_UP:" + moveY);
                isChangeShow = true;
                invalidate();
                handler.sendEmptyMessage(MSG_EXPAND_DELAY);
                break;
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Handler消息处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 延迟收起消息 */
    private static final int MSG_EXPAND_DELAY = 0x146;

    /** 隐藏展开消息 */
    private static final int MSG_EXPAND_HIDE = 0x147;

    /** Handler用于处理自动收起逻辑 */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_EXPAND_DELAY) {
                // 移除之前的隐藏消息
                if (handler.hasMessages(MSG_EXPAND_HIDE)) {
                    handler.removeMessages(MSG_EXPAND_HIDE);
                }
                // 3秒后自动收起
                handler.sendEmptyMessageDelayed(MSG_EXPAND_HIDE, 3 * 1000);
            } else if (msg.what == MSG_EXPAND_HIDE) {
                // 收起展开状态
                isExpand = -1;
                invalidate();
            }
        }
    };

    // ═══════════════════════════════════════════════════════════════════════════════
    // 文本测量辅助方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 文本测量用的临时矩形 */
    private Rect rect = new Rect();

    /**
     * 获取文本宽度
     *
     * @param text 待测量的文本
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return w;
    }

    /**
     * 获取文本高度
     *
     * @param text 待测量的文本
     * @return 文本高度（像素）
     */
    private int getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        int w = rect.width();
        int h = rect.height();
        return h;
    }
}
