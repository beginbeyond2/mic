package com.micsig.tbook.tbookscope.wavezone.measure;

import android.content.Context;                                       // 上下文类，用于获取应用环境信息
import android.graphics.Bitmap;                                       // 位图类，用于绘制光标测量的显示内容
import android.graphics.Canvas;                                       // 画布类，用于在位图上绘制文本和图形
import android.graphics.Color;                                        // 颜色类，提供颜色常量
import android.graphics.Paint;                                        // 画笔类，控制文本和图形的绘制样式
import android.graphics.Point;                                        // 点类，存储x/y坐标
import android.graphics.PorterDuff;                                   // PorterDuff混合模式
import android.graphics.PorterDuffXfermode;                           // PorterDuff混合模式转换器
import android.graphics.Rect;                                         // 矩形类，用于测量文本边界

import com.chillingvan.canvasgl.ICanvasGL;                            // OpenGL画布接口，用于GPU加速渲染
import com.micsig.tbook.tbookscope.GlobalVar;                         // 全局变量管理类
import com.micsig.tbook.tbookscope.tools.Tools;                       // 工具类
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasureCommon; // 顶部测量布局公共常量
import com.micsig.tbook.tbookscope.util.App;                          // 应用上下文工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;                // 工作模式接口
import com.micsig.tbook.ui.wavezone.IWave;                            // 波形接口
import com.micsig.tbook.ui.wavezone.TChan;                            // 通道工具类

/**
 * Created by liwb on 2017/11/6.
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           CurSorMeasureBase                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：示波器光标测量显示的基类，负责在波形区域绘制光标测量结果浮窗         ║
 * ║ 核心职责：                                                                  ║
 * ║   1. 维护光标测量参数结构（Y1/Y2/ΔY, X1/X2/ΔX/1/ΔX, S）                  ║
 * ║   2. 根据行光标/列光标的可见性动态绘制测量结果文本                            ║
 * ║   3. 支持Canvas和ICanvasGL两种渲染方式                                      ║
 * ║ 架构设计：                                                                  ║
 * ║   - 实现IMeasure接口，统一测量绘制规范                                      ║
 * ║   - 实现IWorkMode接口，支持YT/XY等工作模式切换                              ║
 * ║   - 子类CursorMeasure_YT/CursorMeasure_XY继承此类，按模式差异化处理         ║
 * ║ 数据流向：                                                                  ║
 * ║   外部设置参数(setParam) → 更新内部参数结构 → 触发draw()重绘位图 → 渲染显示  ║
 * ║ 依赖关系：                                                                  ║
 * ║   - GlobalVar：获取光标测量浮窗的显示位置                                   ║
 * ║   - TChan：获取通道颜色信息                                                 ║
 * ║   - TopLayoutMeasureCommon：获取测量数据初始占位符                          ║
 * ║ 使用场景：                                                                  ║
 * ║   光标测量模式下，用户拖动光标线后，波形区域右上角显示测量数值浮窗            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class CurSorMeasureBase implements MeasureManage.IMeasure, IWorkMode {

    /***
     * 光标参数结构
     */
    public class CursorMeasureStruct {
        public String row1;                                             // 行光标1的Y值
        public String row2;                                             // 行光标2的Y值
        public String detaRow;                                          // 行光标差值ΔY

        public String col1;                                             // 列光标1的X值
        public String col2;                                             // 列光标2的X值
        public String detaCol;                                          // 列光标差值ΔX
        public String detaTCol;                                         // 列光标差值倒数1/ΔX

        public String S;                                                // 面积值S
    }


    //region  属性
    protected boolean rowCursorVisible = true;                         // 行光标是否可见，默认可见
    protected boolean colCursorVisible = true;                         // 列光标是否可见，默认可见
    private int channelId = 1;                                         // 当前关联的通道ID，默认通道1
    private Context context=App.get().getApplicationContext();         // 应用上下文，用于获取资源

    /**
     * 获取当前关联的通道ID
     * @return 通道ID
     */
    public int getChannelId() {
        return channelId;                                               // 返回当前通道ID
    }

    /**
     * 设置关联的通道ID，并更新显示
     * @param channelId 通道ID
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;                                     // 更新通道ID
        if (TChan.isCh1ToS4(channelId)==false) {                       // 如果通道不是CH1~S4范围内的有效通道
            setParam("---", "---", "---", "---",                       // 设置所有参数为占位符"---"
                    "---", "---", "---", "---");
        } else {
            draw();                                                     // 有效通道则重新绘制
        }
    }

    /**
     * 查询行光标是否可见
     * @return true表示可见
     */
    public boolean isRowCursorVisible() {
        return rowCursorVisible;                                        // 返回行光标可见性
    }

    /**
     * 设置行光标可见性，并更新显示
     * @param rowCursorVisible true表示可见
     */
    public void setRowCursorVisible(boolean rowCursorVisible) {
        this.rowCursorVisible = rowCursorVisible;                      // 更新行光标可见性
        draw();                                                         // 重新绘制
    }

    /**
     * 查询列光标是否可见
     * @return true表示可见
     */
    public boolean isColCursorVisible() {
        return colCursorVisible;                                        // 返回列光标可见性
    }

    /**
     * 设置列光标可见性，并更新显示
     * @param colCursorVisible true表示可见
     */
    public void setColCursorVisible(boolean colCursorVisible) {
        this.colCursorVisible = colCursorVisible;                      // 更新列光标可见性
        draw();                                                         // 重新绘制
    }

    //endregion
    private CursorMeasureStruct param = null;                          // 光标测量参数结构实例

    private int showX = GlobalVar.get().getMeasureCursorPosition(WorkMode_YT).x; // 浮窗显示的X坐标
    private int showY = GlobalVar.get().getMeasureCursorPosition(WorkMode_YT).y; // 浮窗显示的Y坐标
    private int padding = 13;                                          // 浮窗内文本的内边距
    protected Paint p;                                                  // 画笔对象，控制绘制样式
    protected Bitmap bmp;                                               // 离屏位图，存储浮窗渲染结果
    protected Canvas mCanvas;                                           // 离屏画布，绘制到bmp上
    protected boolean isChanageBitmap = false;                         // 位图是否已变更，用于GL渲染刷新判断
    private ICanvasGL canvasGL;                                         // GL画布引用，用于纹理刷新

    /**
     * 通知GL画布刷新纹理内容
     */
    public void onRefresh(){
        if(canvasGL != null){                                           // 如果GL画布引用不为空
            canvasGL.onRefreshTexture();                                // 刷新纹理，使新内容可见
        }
    }

    /**
     * 构造函数：初始化光标测量参数结构、离屏位图、画布和画笔
     */
    public CurSorMeasureBase() {
        param = new CursorMeasureStruct();                              // 创建光标参数结构实例
        bmp = Bitmap.createBitmap(200, 220, Bitmap.Config.ARGB_8888);  // 创建200x220的ARGB位图
        mCanvas = new Canvas(bmp);                                      // 将画布绑定到位图
        p = new Paint();                                                // 创建画笔
        p.setTextSize(20);                                              // 设置文本大小为20
        p.setAntiAlias(true);                                           // 开启抗锯齿
        draw();                                                         // 执行初始绘制
    }

    //region IWorkMode 接口
    /**
     * 切换工作模式时更新浮窗显示位置
     * @param workMode 工作模式（YT/XY等）
     */
    @Override
    public void switchWorkMode(@IWorkMode.WorkMode int workMode) {
        Point p = GlobalVar.get().getMeasureCursorPosition(workMode);   // 根据工作模式获取浮窗位置
        showX = p.x;                                                    // 更新X坐标
        showY = p.y;                                                    // 更新Y坐标
    }


    //endregion

    /**
     * 使用Canvas方式绘制光标测量浮窗
     * @param canvas 目标画布
     */
    @Override
    public void draw(Canvas canvas) {
        if (colCursorVisible || rowCursorVisible) {                     // 如果行光标或列光标可见
            synchronized (bmp) {                                        // 同步锁定位图，防止并发修改
                canvas.drawBitmap(bmp, showX, showY, null);            // 将离屏位图绘制到目标画布上
            }
        }
    }

    /**
     * 使用ICanvasGL方式绘制光标测量浮窗（当前已注释掉实现）
     * @param canvas GL画布
     */
    @Override
    public void draw(ICanvasGL canvas) {

//        if (colCursorVisible || rowCursorVisible) {
//            synchronized (bmp) {
//                if (isChanageBitmap) canvas.invalidateTextureContent(bmp);
//                canvas.drawBitmap(bmp, showX, showY);
//                isChanageBitmap = false;
//            }
//        }
    }

    //region 显示
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式混合器
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);     // 源模式混合器

    /**
     * 核心绘制方法：根据光标可见性在离屏位图上绘制测量结果浮窗
     * 包括清除旧内容、计算文本尺寸、绘制背景圆角矩形、绘制文本行
     */
    protected void draw() {
        synchronized (bmp) {                                            // 同步锁定位图，保证线程安全
            p.setXfermode(clearMode);                                   // 设置画笔为清除模式
            mCanvas.drawPaint(p);                                       // 清除位图上所有已有内容
            p.setXfermode(srcMode);                                     // 恢复画笔为源模式（正常绘制）
            int textAllHeight = 0;                                      // 文本区域总高度
            int textAllWidth = 0;                                       // 文本区域总宽度
            String row1s = "Y1:" + (param.row1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row1); // 行光标1文本
            String row2s = "Y2:" + (param.row2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row2); // 行光标2文本
            String detaRows = "ΔY:" + (param.detaRow == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaRow); // ΔY文本
            String col1s = "X1:" + (param.col1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col1); // 列光标1文本
            String col2s = "X2:" + (param.col2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col2); // 列光标2文本
            String detaCols = "ΔX:" + (param.detaCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaCol); // ΔX文本
            String detaTCols = "1/ΔX:" + (param.detaTCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaTCol); // 1/ΔX文本
            String ss = "S:" + (param.S == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.S); // S面积文本
            if (rowCursorVisible && colCursorVisible) {                 // 行光标和列光标都可见
                textAllWidth = Math.max(textAllWidth, getTextWidth(row1s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(row2s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaRows)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(col1s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(col2s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaCols)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaTCols)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(ss)); // 取最大文本宽度
                textAllHeight = (getTextHeight() + 5) * 8;             // 8行文本的总高度（Y1,Y2,ΔY,X1,X2,ΔX,1/ΔX,S）
            } else if (rowCursorVisible) {                              // 仅行光标可见
                textAllWidth = Math.max(textAllWidth, getTextWidth(row1s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(row2s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaRows)); // 取最大文本宽度
                textAllHeight = (getTextHeight() + 5) * 3;             // 3行文本的总高度（Y1,Y2,ΔY）
            } else if (colCursorVisible) {                              // 仅列光标可见
                textAllWidth = Math.max(textAllWidth, getTextWidth(col1s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(col2s)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaCols)); // 取最大文本宽度
                textAllWidth = Math.max(textAllWidth, getTextWidth(detaTCols)); // 取最大文本宽度
                textAllHeight = (getTextHeight() + 5) * 4;             // 4行文本的总高度（X1,X2,ΔX,1/ΔX）
            }
            if (textAllHeight != 0 && textAllWidth != 0) {             // 如果有内容需要绘制
                if (TChan.isRef(channelId)) {                           // 如果是Ref参考通道
                    p.setColor(TChan.getChannelColor(context, TChan.RefActive)); // 设置Ref通道颜色
                } else {
                    p.setColor(TChan.getChannelColor(context, channelId)); // 设置当前通道颜色
                }
                p.setAlpha(255);                                        // 设置完全不透明
                p.setStyle(Paint.Style.STROKE);                        // 设置画笔为描边模式（画边框）
                textAllHeight += (padding * 2);                         // 加上上下内边距
                textAllWidth += (padding * 2);                          // 加上左右内边距
                mCanvas.drawRoundRect(1, 1, textAllWidth - 1, textAllHeight - 1, 6f, 6f, p); // 绘制圆角边框
                p.setStyle(Paint.Style.FILL);                          // 设置画笔为填充模式
                p.setColor(Color.BLACK);                                // 设置填充色为黑色
                p.setAlpha(204);                                        // 设置半透明（约80%不透明度）
                mCanvas.drawRoundRect(1, 1, textAllWidth - 1, textAllHeight - 1, 6f, 6f, p); // 绘制半透明黑色背景
            }

            p.setAlpha(255);                                            // 恢复完全不透明
            p.setColor(App.get().getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewEnable)); // 设置文本颜色
            if (rowCursorVisible && colCursorVisible) {                 // 行光标和列光标都可见
                int y = drawRow();                                      // 绘制行光标数据（Y1,Y2,ΔY），返回当前Y坐标
                y = drawCol(y);                                         // 绘制列光标数据（X1,X2,ΔX），返回当前Y坐标
                y = drawDeltaX(y);                                      // 绘制1/ΔX数据，返回当前Y坐标
                drawS(y);                                               // 绘制S面积数据
            } else if (rowCursorVisible) {                              // 仅行光标可见
                drawRow();                                              // 绘制行光标数据（Y1,Y2,ΔY）
            } else if (colCursorVisible) {                              // 仅列光标可见
                int y = drawCol(padding - 5);                           // 绘制列光标数据，起始Y从padding-5开始
                drawDeltaX(y);                                          // 绘制1/ΔX数据
            }

            isChanageBitmap = true;                                     // 标记位图已变更
            onRefresh();                                                // 通知GL刷新纹理
        }
    }

    /**
     * 绘制行光标测量数据（Y1, Y2, ΔY）
     * @return 最后绘制行的Y坐标
     */
    protected int drawRow() {
        int x = padding, y = padding;                                   // 起始坐标从内边距开始
        String text = "Y1:" + (param.row1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row1); // 拼接Y1文本
        y = y + getTextHeight();                                        // Y坐标下移一个文本高度
        mCanvas.drawText(text, x, y, p);                                // 在位图上绘制Y1文本

        text = "Y2:" + (param.row2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.row2); // 拼接Y2文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移一个文本高度加5像素间距
        mCanvas.drawText(text, x, y, p);                                // 在位图上绘制Y2文本

        text = "ΔY:" + (param.detaRow == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaRow); // 拼接ΔY文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移一个文本高度加5像素间距
        mCanvas.drawText(text, x, y, p);                                // 在位图上绘制ΔY文本

        return y;                                                       // 返回当前Y坐标，供后续绘制使用
    }

    /**
     * 绘制列光标测量数据（X1, X2, ΔX）
     * @param y 起始Y坐标
     * @return 最后绘制行的Y坐标
     */
    protected int drawCol(int y) {
        int x = padding;                                                // X坐标从内边距开始
        String text = "X1:" + (param.col1 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col1); // 拼接X1文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移
        mCanvas.drawText(text, x, y, p);                                // 绘制X1文本

        text = "X2:" + (param.col2 == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.col2); // 拼接X2文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移
        mCanvas.drawText(text, x, y, p);                                // 绘制X2文本

        text = "ΔX:" + (param.detaCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaCol); // 拼接ΔX文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移
        mCanvas.drawText(text, x, y, p);                                // 绘制ΔX文本

        return y;                                                       // 返回当前Y坐标
    }

    /**
     * 绘制1/ΔX数据
     * @param y 起始Y坐标
     * @return 最后绘制行的Y坐标
     */
    protected int drawDeltaX(int y) {
        int x = padding;                                                // X坐标从内边距开始
        String text = "1/ΔX:" + (param.detaTCol == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.detaTCol); // 拼接1/ΔX文本
        y = y + getTextHeight() + 5;                                    // Y坐标下移
        mCanvas.drawText(text, x, y, p);                                // 绘制1/ΔX文本
        return y;                                                       // 返回当前Y坐标
    }

    /**
     * 绘制S面积数据
     * @param y 起始Y坐标
     */
    protected void drawS(int y) {
        String text = "S:" + (param.S == null ? TopLayoutMeasureCommon.MEASURE_DATA_INIT : param.S); // 拼接S文本
        int x = padding;                                                // X坐标从内边距开始
        y = y + getTextHeight() + 5;                                    // Y坐标下移
        mCanvas.drawText(text, x, y, p);                                // 绘制S文本
    }

    private Rect rect = new Rect();                                     // 文本边界矩形，用于测量文本尺寸

    /**
     * 获取标准文本的高度
     * @return 文本像素高度
     */
    private int getTextHeight() {
        String text="123456789mV";                                      // 使用标准字符测量高度
        p.getTextBounds(text, 0, text.length(), rect);                  // 测量文本边界
        int w = rect.width();                                           // 获取宽度（未使用）
        int h = rect.height();                                          // 获取高度
        return h;                                                       // 返回文本高度
    }

    /**
     * 获取指定文本的像素宽度
     * @param text 待测量的文本
     * @return 文本像素宽度
     */
    public int getTextWidth(String text) {
        p.getTextBounds(text, 0, text.length(), rect);                  // 测量文本边界
        int w = rect.width();                                           // 获取宽度
        int h = rect.height();                                          // 获取高度（未使用）
        return w;                                                       // 返回文本宽度
    }
    //endregion

    /**
     * 设置光标测量参数，并触发重绘
     * @param row1    行光标1的Y值
     * @param row2    行光标2的Y值
     * @param detaRow 行光标差值ΔY
     * @param col1    列光标1的X值
     * @param col2    列光标2的X值
     * @param detaCol 列光标差值ΔX
     * @param detaTCol 列光标差值倒数1/ΔX
     * @param S       面积值
     */
    public void setParam(String row1, String row2, String detaRow, String col1, String col2, String detaCol,
                         String detaTCol, String S) {
        param.row1 = row1;                                              // 设置行光标1值
        param.row2 = row2;                                              // 设置行光标2值
        param.detaRow = detaRow;                                        // 设置ΔY值
        param.col1 = col1;                                              // 设置列光标1值
        param.col2 = col2;                                              // 设置列光标2值
        param.detaCol = detaCol;                                        // 设置ΔX值
        param.detaTCol = detaTCol;                                      // 设置1/ΔX值
        param.S = S;                                                    // 设置S面积值
        draw();                                                         // 参数变更后触发重绘
    }
}
