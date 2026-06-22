package com.micsig.tbook.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                            左侧位置指示器视图
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 波形显示区域左侧的位置标签控件，显示各通道的垂直位置标签，支持自动调整避免重叠，
 * 最多支持8个通道的位置显示。
 *
 * 【核心职责】
 * 1. 显示各通道的垂直位置标签（数值+单位）
 * 2. 自动调整标签位置避免重叠
 * 3. 支持通道可见性控制
 * 4. 根据通道颜色显示对应颜色的标签
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     LeftPositionView                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  数据层: list<LeftPosBean> / width / height / startPos         │
 * │  算法层: sort() / fixPos()                                     │
 * │  绘制层: onDraw() / setData() / setItemVisible()               │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * 外部设置数据 → setData() → 更新LeftPosBean → sort()排序 → fixPos()调整位置 → onDraw()渲染
 *
 * 【位置调整算法】
 * 1. 按实际位置排序所有可见通道
 * 2. 检测相邻标签是否重叠
 * 3. 重叠时计算中心位置并均匀分布
 * 4. 确保标签不超出显示边界
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ ScreenUtil   │────>│ 屏幕工具类           │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────┐     ┌──────────────────────┐
 * │ TBookUtil    │────>│ 单位常量定义         │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────┐     ┌──────────────────────┐
 * │ R.color      │────>│ 通道颜色资源         │
 * └──────────────┘     └──────────────────────┘
 *
 * 【使用示例】
 * LeftPositionView leftPosView = findViewById(R.id.leftPosition);
 * leftPosView.setData(0, 100.0, "100", "mV", 0);  // 设置CH1位置
 * leftPosView.setItemVisible(0, true);             // 显示CH1标签
 * leftPosView.setAllVisible(true);                 // 显示整个控件
 *
 * 【注意事项】
 * 1. 最多支持8个通道（CH1~CH8）
 * 2. 标签高度固定为30像素（textAllHeight）
 * 3. fixPos()方法调用两次以确保位置稳定
 * 4. 数值格式化为3位有效数字
 */
public class LeftPositionView extends View {
    // ═════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═════════════════════════════════════════════════════════════════════════════

    private static final String TAG = "LeftPositionView";

    /** 通道1索引 */
    public static final int CH1 = 0;
    /** 通道2索引 */
    public static final int CH2 = 1;
    /** 通道3索引 */
    public static final int CH3 = 2;
    /** 通道4索引 */
    public static final int CH4 = 3;
    /** 通道5索引 */
    public static final int CH5 = 4;
    /** 通道6索引 */
    public static final int CH6 = 5;
    /** 通道7索引 */
    public static final int CH7 = 6;
    /** 通道8索引 */
    public static final int CH8 = 7;

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /** 上下文对象 */
    private Context context;

    /** 绘制画笔 */
    private Paint paint;

    /** 控件宽度 */
    private int width, height;

    /** 文本总高度 */
    private int textAllHeight;

    /** 起始位置 */
    private int startPos;

    /** 整体可见性 */
    private boolean visible;

    /** 通道位置数据列表 */
    private ArrayList<LeftPosBean> list = new ArrayList<LeftPosBean>();

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 单参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建LeftPositionView实例，仅传入Context。
     *
     * 【参数说明】
     * @param context 上下文对象
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public LeftPositionView(Context context) {
        this(context, null);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - XML属性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建LeftPositionView实例，支持XML属性。
     *
     * 【参数说明】
     * @param context 上下文对象
     * @param attrs   XML属性集
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public LeftPositionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 完整参数
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建LeftPositionView实例，初始化所有资源和默认数据。
     *
     * 【参数说明】
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     *
     * 【初始化流程】
     * 1. 设置控件尺寸
     * 2. 创建画笔
     * 3. 初始化8个通道的默认数据
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public LeftPositionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 保存上下文
        this.context = context;
        // 设置默认宽度
        width = 72;
        // 设置默认高度
        height = 650;
        // 创建画笔
        paint = new Paint();
        // 设置文本大小
        paint.setTextSize(16);
        // 启用抗锯齿
        paint.setAntiAlias(true);
        // 默认可见
        visible = true;

        // 起始位置为0
        startPos = 0;
        // 文本高度30像素
        textAllHeight = 30;

        // 初始化8个通道的默认数据
        list.add(new LeftPosBean(0, getResources().getColor(R.color.color_Ch1), 100, 100, "100", "mV"));
        list.add(new LeftPosBean(1, getResources().getColor(R.color.color_Ch2), 200, 200, "1", "V"));
        list.add(new LeftPosBean(2, getResources().getColor(R.color.color_Ch3), 300, 300, "100", "mV"));
        list.add(new LeftPosBean(3, getResources().getColor(R.color.color_Ch4), 400, 400, "10.2", "V"));
        list.add(new LeftPosBean(4, getResources().getColor(R.color.color_Ch5), 500, 500, "100", "mV"));
        list.add(new LeftPosBean(5, getResources().getColor(R.color.color_Ch6), 600, 600, "1", "V"));
        list.add(new LeftPosBean(6, getResources().getColor(R.color.color_Ch7), 700, 700, "100", "mV"));
        list.add(new LeftPosBean(7, getResources().getColor(R.color.color_Ch8), 800, 800, "1", "V"));
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 绘制方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 绘制方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 绘制所有可见通道的位置标签。
     *
     * 【绘制流程】
     * 1. 检查整体可见性
     * 2. 排序通道数据
     * 3. 调整标签位置避免重叠
     * 4. 逐个绘制标签
     *
     * 【参数说明】
     * @param canvas 画布对象
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 检查整体可见性
        if (!visible) {
            return;
        }
        // 计算文本X坐标
        int x;
        // 计算文本Y坐标
        int y;

        // 排序通道数据
        sort();
        // 调整标签位置避免重叠（调用两次确保稳定）
        fixPos();
        fixPos();

        // 遍历绘制所有可见通道标签
        for (LeftPosBean bean : list) {
            // 跳过不可见通道
            if (!bean.visible) {
                break;
            }
            // 计算文本居中位置
            x = (width - ScreenUtil.getTextWidth(paint, bean.number+bean.unit))/2;
            // 获取调整后的Y位置
            y = bean.tmpPos;
            // 设置通道颜色
            paint.setColor(bean.color);
            // 绘制文本（数值+单位）
            canvas.drawText(bean.number+bean.unit, x, y + 5, paint);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 位置调整算法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 排序方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 根据位置对通道数据进行排序，不可见通道排在最后。
     *
     * 【排序规则】
     * 1. 可见通道按位置从小到大排序
     * 2. 不可见通道按原始索引排序，排在可见通道之后
     * 3. 位置超出显示范围的通道被限制在边界内
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void sort() {
        // 遍历所有通道
        for (int i = 0; i < list.size(); i++) {
            // 处理不可见通道
            if (!list.get(i).visible) {
                // 不可见通道设置极大值，排序后排在最后
                list.get(i).tmpPos = Integer.MAX_VALUE - i * textAllHeight * 2;
            } else {
                // 可见通道使用实际位置
                list.get(i).tmpPos = list.get(i).pos;
                // 限制位置在显示范围内
                if (list.get(i).tmpPos < startPos) {
                    list.get(i).tmpPos = startPos;
                } else if (list.get(i).tmpPos > height) {
                    list.get(i).tmpPos = height;
                }
            }
        }
        // 按临时位置排序
        Collections.sort(list, new Comparator<LeftPosBean>() {
            @Override
            public int compare(LeftPosBean lhs, LeftPosBean rhs) {
                return lhs.tmpPos - rhs.tmpPos;
            }
        });
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 位置调整方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 调整标签位置以避免重叠，支持最多4个通道的重叠处理。
     *
     * 【调整算法】
     * 1. 检测相邻标签是否重叠（间距小于textAllHeight）
     * 2. 重叠时计算中心位置
     * 3. 根据重叠数量均匀分布标签
     * 4. 确保标签不超出显示边界
     *
     * 【处理场景】
     * - 2个标签重叠：以中心为基准，上下各偏移半个高度
     * - 3个标签重叠：以中心为基准，均匀分布
     * - 4个标签重叠：以中心为基准，均匀分布
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void fixPos() {
        // 获取排序后的8个通道数据
        LeftPosBean bean1 = list.get(0);
        LeftPosBean bean2 = list.get(1);
        LeftPosBean bean3 = list.get(2);
        LeftPosBean bean4 = list.get(3);
        LeftPosBean bean5 = list.get(4);
        LeftPosBean bean6 = list.get(5);
        LeftPosBean bean7 = list.get(6);
        LeftPosBean bean8 = list.get(7);

        // 计算可见通道数量
        int visibleCount = 0;
        visibleCount += (list.get(0).visible ? 1 : 0);
        visibleCount += (list.get(1).visible ? 1 : 0);
        visibleCount += (list.get(2).visible ? 1 : 0);
        visibleCount += (list.get(3).visible ? 1 : 0);
        visibleCount += (list.get(4).visible ? 1 : 0);
        visibleCount += (list.get(5).visible ? 1 : 0);
        visibleCount += (list.get(6).visible ? 1 : 0);
        visibleCount += (list.get(7).visible ? 1 : 0);

        // 处理前4个通道的重叠情况
        // 检测bean1和bean2是否重叠
        if (bean2.tmpPos - bean1.tmpPos < textAllHeight) {
            // 计算bean1和bean2的中心位置
            int center12 = (bean1.tmpPos + bean2.tmpPos) / 2;
            // 检测bean3是否也重叠
            if (bean3.tmpPos - center12 < textAllHeight * 3 / 2) {
                // 计算bean1、bean2、bean3的中心位置
                int center123 = (bean1.tmpPos + bean2.tmpPos + bean3.tmpPos) / 3;
                // 检测bean4是否也重叠
                if (bean4.tmpPos - center123 < textAllHeight * 2) {
                    // 4个标签重叠，均匀分布
                    int center1234 = (bean1.tmpPos + bean2.tmpPos + bean3.tmpPos + bean4.tmpPos) / 4;
                    bean1.tmpPos = center1234 - textAllHeight * 3 / 2;
                    bean2.tmpPos = center1234 - textAllHeight / 2;
                    bean3.tmpPos = center1234 + textAllHeight / 2;
                    bean4.tmpPos = center1234 + textAllHeight * 3 / 2;
                } else {
                    // 3个标签重叠，均匀分布
                    bean1.tmpPos = center123 - textAllHeight;
                    bean2.tmpPos = center123;
                    bean3.tmpPos = center123 + textAllHeight;
                }
            } else {
                // 2个标签重叠，上下分布
                bean1.tmpPos = center12 - textAllHeight / 2;
                bean2.tmpPos = center12 + textAllHeight / 2;
                // 检测bean3和bean4是否重叠
                if (bean4.tmpPos - bean3.tmpPos < textAllHeight) {
                    int center34 = (bean3.tmpPos + bean4.tmpPos) / 2;
                    bean3.tmpPos = center34 - textAllHeight / 2;
                    bean4.tmpPos = center34 + textAllHeight / 2;
                }
            }
        } else if (bean3.tmpPos - bean2.tmpPos < textAllHeight) {
            // bean2和bean3重叠
            int center23 = (bean2.tmpPos + bean3.tmpPos) / 2;
            if (bean4.tmpPos - center23 < textAllHeight * 3 / 2) {
                int center234 = (bean2.tmpPos + bean3.tmpPos + bean4.tmpPos) / 3;
                bean2.tmpPos = center234 - textAllHeight;
                bean3.tmpPos = center234;
                bean4.tmpPos = center234 + textAllHeight;
            } else {
                bean2.tmpPos = center23 - textAllHeight / 2;
                bean3.tmpPos = center23 + textAllHeight / 2;
            }
        } else if (bean4.tmpPos - bean3.tmpPos < textAllHeight) {
            // bean3和bean4重叠
            int center34 = (bean3.tmpPos + bean4.tmpPos) / 2;
            bean3.tmpPos = center34 - textAllHeight / 2;
            bean4.tmpPos = center34 + textAllHeight / 2;
        }

        // 确保标签不超出显示边界
        if (visibleCount >= 1) {
            bean1.tmpPos = (int) Math.max(bean1.tmpPos, startPos + textAllHeight / 2.0);
            bean1.tmpPos = (int) Math.min(bean1.tmpPos, height - textAllHeight * (visibleCount - 1.0 / 2));
        }
        if (visibleCount >= 2) {
            bean2.tmpPos = (int) Math.max(bean2.tmpPos, startPos + textAllHeight * 3.0 / 2);
            bean2.tmpPos = (int) Math.min(bean2.tmpPos, height - textAllHeight * (visibleCount - 3.0 / 2));
        }
        if (visibleCount >= 3) {
            bean3.tmpPos = (int) Math.max(bean3.tmpPos, startPos + textAllHeight * 5.0 / 2);
            bean3.tmpPos = (int) Math.min(bean3.tmpPos, height - textAllHeight * (visibleCount - 5.0 / 2));
        }
        if (visibleCount == 4) {
            bean4.tmpPos = (int) Math.max(bean4.tmpPos, startPos + textAllHeight * 7.0 / 2);
            bean4.tmpPos = (int) Math.min(bean4.tmpPos, height - textAllHeight * (visibleCount - 7.0 / 2));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置通道可见性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置指定通道标签的可见性。
     *
     * 【参数说明】
     * @param chIndex 通道索引（CH1~CH8）
     * @param visible  true表示可见，false表示不可见
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setItemVisible(int chIndex, boolean visible) {
        // 查找对应通道
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).chIndex == chIndex) {
                // 设置可见性
                list.get(i).visible = visible;
                break;
            }
        }
        // 触发重绘
        invalidate();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置整体可见性
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置整个控件的可见性。
     *
     * 【参数说明】
     * @param visible true表示可见，false表示不可见
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setAllVisible(boolean visible) {
        this.visible = visible;
        invalidate();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 设置通道数据
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 设置指定通道的位置数据，包括位置、数值和单位。
     *
     * 【参数说明】
     * @param chIndex 通道索引（CH1~CH8）
     * @param pos     垂直位置（像素）
     * @param number  数值字符串
     * @param unit    单位字符串
     * @param startPos 起始位置
     *
     * 【处理流程】
     * 1. 四舍五入位置值
     * 2. 查找对应通道
     * 3. 更新位置和数值
     * 4. 处理单位前缀
     * 5. 格式化数值
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public void setData(int chIndex, double pos, String number, String unit, int startPos) {
        // 四舍五入位置值
        int temp = (int)Math.round(pos);
        // 保存起始位置
        this.startPos = startPos;
        // 查找对应通道索引
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).chIndex == chIndex) {
                index = i;
            }
        }
        // 更新位置数据
        list.get(index).pos = temp;
        list.get(index).tmpPos = temp;
        list.get(index).number = number;
        list.get(index).unit = unit;

        // 处理单位前缀（检查数值是否已包含单位前缀）
        for (int i = 0; i < 2; i++) {
            // 获取单位前缀数组
            String[] ss = i == 0 ? TBookUtil.unit1 : TBookUtil.unit2;
            for (String s : ss) {
                // 检查数值是否以单位前缀结尾
                if (!StrUtil.isEmpty(s) && list.get(index).number.endsWith(s)) {
                    // 将单位前缀移到单位前面
                    list.get(index).number = list.get(index).number.replace(s, "");
                    list.get(index).unit = s + list.get(index).unit;
                    break;
                }
            }
        }
        // 格式化数值为3位有效数字
        list.get(index).number = fixNumber(list.get(index).number);
        // 触发重绘
        invalidate();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 格式化数值
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 将数值格式化为3位有效数字，不足补0，超出截断。
     *
     * 【参数说明】
     * @param num 原始数值字符串
     *
     * 【返回值】
     * @return 格式化后的数值字符串
     *
     * 【格式化规则】
     * - 整数：补齐为X.XX格式
     * - 小数：保留3位有效数字
     * - 负数：保留负号，格式化数字部分
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private String fixNumber(String num) {
        // 有效数字位数
        int count = 3;
        // 处理负数
        if (num.contains("-")) {
            return "-" + fixNumber(num.replace("-", ""));
        }
        // 处理小数
        if (num.contains(".")) {
            // 不足3位，补0
            if (num.length() < count + 1) {
                while (num.length() < count + 1) {
                    num = num + "0";
                }
            } else if (num.length() > count + 1) {
                // 超过3位，截断
                num = num.substring(0, count + 1);
                // 去除末尾的小数点
                if (num.endsWith(".")) {
                    num = num.substring(0, num.length() - 1);
                }
            }
        } else if (!num.contains(".")) {
            // 整数，补齐为X.XX格式
            if (num.length() < count) {
                num = num + ".";
                while (num.length() < count + 1) {
                    num = num + "0";
                }
            }
        }
        return num;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 内部类
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 左侧位置数据Bean
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 封装单个通道的位置显示数据。
     *
     * 【属性说明】
     * - chIndex: 通道索引
     * - color: 标签颜色
     * - pos: 实际位置（像素）
     * - tmpPos: 显示位置（调整后）
     * - number: 数值字符串
     * - unit: 单位字符串
     * - visible: 是否可见
     * ═══════════════════════════════════════════════════════════════════════════
     */
    class LeftPosBean {
        /** 通道索引 */
        int chIndex;
        /** 标签颜色 */
        int color;
        /** 实际位置（像素） */
        int pos;
        /** 显示位置（调整后） */
        int tmpPos;
        /** 数值字符串 */
        String number;
        /** 单位字符串 */
        String unit;
        /** 是否可见 */
        boolean visible;

        /**
         * 构造方法
         * @param chIndex 通道索引
         * @param color   标签颜色
         * @param pos     实际位置
         * @param tmpPos  显示位置
         * @param number  数值字符串
         * @param unit    单位字符串
         */
        public LeftPosBean(int chIndex, int color, int pos, int tmpPos, String number, String unit) {
            this.chIndex = chIndex;
            this.color = color;
            this.pos = pos;
            this.tmpPos = tmpPos;
            this.number = number;
            this.unit = unit;
            // 默认可见
            this.visible = true;
        }

        /**
         * 字符串转换
         * @return 格式化的字符串表示
         */
        @Override
        public String toString() {
            return "LeftPosBean{" +
                    "chIndex=" + chIndex +
                    ", color=" + color +
                    ", pos=" + pos +
                    ", tmpPos=" + tmpPos +
                    ", number='" + number + '\'' +
                    ", unit='" + unit + '\'' +
                    ", visible=" + visible +
                    '}';
        }
    }
}
