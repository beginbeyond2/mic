package com.micsig.tbook.tbookscope.wavezone.display;

import android.content.Context;                                           // Android上下文，用于获取资源
import android.graphics.Bitmap;                                           // 位图类，用于光标图标渲染
import android.graphics.Canvas;                                           // 画布类，用于CPU绘制
import android.graphics.Color;                                            // 颜色工具类

import com.chillingvan.canvasgl.ICanvasGL;                                // OpenGL画布接口，用于GPU加速绘制
import com.micsig.tbook.tbookscope.GlobalVar;                            // 全局变量，存储波形区域尺寸等
import com.micsig.tbook.tbookscope.R;                                     // 资源ID引用
import com.micsig.tbook.tbookscope.util.App;                              // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;                        // 缓存工具，持久化光标位置等配置
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;                    // 工作模式接口（YT/XY/YTZOOM）
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;        // 测量管理器
import com.micsig.tbook.ui.wavezone.IWave;                                // 波形组件接口
import com.micsig.tbook.ui.wavezone.TChan;                                // 通道/光标类型常量定义

import java.util.ArrayList;                                               // 动态数组
import java.util.List;                                                    // 列表接口

/**
 * Created by liwb on 2017/5/5.
 * 光标管理
 *
 * +===================================================================================================+
 * |                                        CursorManage_XY                                            |
 * |                                      XY模式光标管理器                                              |
 * +===================================================================================================+
 * |                                                                                                   |
 * | 【模块定位】                                                                                      |
 * |   XY模式下示波器波形区域的光标管理器，负责XY模式下的光标创建、选择、移动和绘制                   |
 * |                                                                                                   |
 * | 【核心职责】                                                                                      |
 * |   1. 管理XY模式下的4条光标线（水平2条+垂直2条）的创建和初始化                                    |
 * |   2. 处理光标的选择（单选/多选）、移动（像素级）和缩放（双光标聚拢/扩散）                        |
 * |   3. 通过CursorLabel管理光标测量数据的显示                                                        |
 * |   4. 支持Canvas和ICanvasGL两种绘制方式                                                           |
 * |                                                                                                   |
 * | 【架构设计】                                                                                      |
 * |   实现IWorkMode接口，支持工作模式切换。                                                          |
 * |   内部维护一个Cursor_impIWave列表，每个元素代表一条光标线。                                       |
 * |   被CursorManage门面类在XY模式下调用，不直接对外暴露。                                            |
 * |                                                                                                   |
 * | 【数据流向】                                                                                      |
 * |   CursorManage → CursorManage_XY → Cursor_impIWave列表 → Canvas/ICanvasGL绘制                    |
 * |   CacheUtil → 构造函数读取光标初始位置                                                           |
 * |   CursorLabel → MeasureManage → 测量数据显示                                                     |
 * |                                                                                                   |
 * | 【依赖关系】                                                                                      |
 * |   - Cursor_impIWave：光标线实现类（实现IWave接口）                                                |
 * |   - CursorLabel：光标测量标签管理器                                                               |
 * |   - CacheUtil：缓存工具（读取光标初始位置）                                                       |
 * |   - MeasureManage：测量管理器（管理光标测量可见性）                                               |
 * |   - GlobalVar：全局变量（获取波形区域尺寸）                                                       |
 * |   - TChan：通道/光标类型常量                                                                      |
 * |                                                                                                   |
 * | 【使用场景】                                                                                      |
 * |   - XY模式下的光标显示与操作                                                                      |
 * |   - XY模式下的光标测量值显示                                                                      |
 * |   - 由CursorManage在XY模式下委托调用                                                              |
 * +===================================================================================================+
 */


public class CursorManage_XY implements IWorkMode {                        // 实现IWorkMode工作模式接口
    private static final String TAG = "CursorManage_XY";                   // 日志标签
    //光标选择范围
    private Context context=App.get().getApplicationContext();             // 应用上下文，用于获取颜色资源
    private static final int CURSOR_OFFSET_SELECT = 20;                   // 光标触摸选择范围（像素），手指在此范围内视为选中
    private static final int alpha=0xFF;                                   // 光标颜色默认透明度（不透明）

    private List<Cursor_impIWave> curList = new ArrayList<Cursor_impIWave>(); // 光标线列表，存储4条光标线
    private Bitmap[][] resBmp;                                             // 光标图标位图数组：[光标类型][选中/未选中]
    private CursorLabel cursorLabel;                                       // 光标测量标签管理器

    /**
     * 构造函数：初始化XY模式下的4条光标线并从缓存读取初始位置
     * @param resBmp 光标图标位图数组，由CursorManage传入
     */
    public CursorManage_XY(Bitmap[][] resBmp) {

        this.resBmp=resBmp;                                                // 保存位图资源引用
        double xyY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_1); // 从缓存读取水平光标1位置
        double xyY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_2); // 从缓存读取水平光标2位置
        int xyX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_1);       // 从缓存读取垂直光标1位置
        int xyX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_2);       // 从缓存读取垂直光标2位置
//        Logger.i(TAG, "CursorManage_XY() ==>"
//                +" xyY1:"+xyY1+" xyY2:"+xyY2+" xyX1:"+xyX1+" xyX2:"+xyX2);

        Cursor_impIWave cur = new Cursor_impIWave(this.resBmp, TChan.Cursor_row_1, IWorkMode.WorkMode_XY,true); // 创建水平光标1
        cur.setLineNameId(TChan.Cursor_row_1);                            // 设置光标线名称ID
        cur.setColor(Color.rgb(200, 200, 200));                           // 设置光标颜色为浅灰色
        cur.setSelected(false);                                           // 默认未选中
        cur.setX(0);                                                      // 水平光标X坐标固定为0（横贯整个宽度）
        cur.setY(xyY1);                                                   // 设置Y坐标为缓存值
        cur.setVisible(true);                                             // 设置可见
        curList.add(cur);                                                 // 添加到光标列表

        cur = new Cursor_impIWave(this.resBmp,TChan.Cursor_row_2, IWorkMode.WorkMode_XY,true); // 创建水平光标2
        cur.setLineNameId(TChan.Cursor_row_2);                            // 设置光标线名称ID
        cur.setColor(Color.rgb(200, 200, 200));                           // 设置光标颜色为浅灰色
        cur.setSelected(true);                                            // 默认选中
        cur.setX(0);                                                      // 水平光标X坐标固定为0
        cur.setY(xyY2);                                                   // 设置Y坐标为缓存值
        cur.setVisible(true);                                             // 设置可见
        curList.add(cur);                                                 // 添加到光标列表

        cur = new Cursor_impIWave(this.resBmp,TChan.Cursor_col_1, IWorkMode.WorkMode_XY,true); // 创建垂直光标1
        cur.setLineNameId(TChan.Cursor_col_1);                            // 设置光标线名称ID
        cur.setColor(Color.rgb(200, 200, 200));                           // 设置光标颜色为浅灰色
        cur.setSelected(false);                                           // 默认未选中
        cur.setX(xyX1);                                                   // 设置X坐标为缓存值
        cur.setY(0);                                                      // 垂直光标Y坐标固定为0（纵贯整个高度）
        cur.setVisible(true);                                             // 设置可见
        curList.add(cur);                                                 // 添加到光标列表

        cur = new Cursor_impIWave(this.resBmp,TChan.Cursor_col_2, IWorkMode.WorkMode_XY,true); // 创建垂直光标2
        cur.setLineNameId(TChan.Cursor_col_2);                            // 设置光标线名称ID
        cur.setColor(Color.rgb(200, 200, 200));                           // 设置光标颜色为浅灰色
        cur.setSelected(false);                                           // 默认未选中
        cur.setX(xyX2);                                                   // 设置X坐标为缓存值
        cur.setY(0);                                                      // 垂直光标Y坐标固定为0
        cur.setVisible(true);                                             // 设置可见
        curList.add(cur);                                                 // 添加到光标列表

        CancelAllHightShow();                                             // 初始化时取消所有高亮显示
        cursorLabel=new CursorLabel(curList);                             // 创建光标测量标签管理器
        cursorLabel.initMeasure();                                        // 初始化测量标签
    }

    //region 单例创建
//    private static class Cursor_Holder {
//        private static final CursorManage_XY instance = new CursorManage_XY();
//    }
//
//    public static final CursorManage_XY get() {
//        return Cursor_Holder.instance;
//    }

    /**
     * 绘制光标到CPU画布
     * 遍历所有可见光标进行绘制，并绘制测量标签
     * @param canvas CPU画布
     */
    public void draw(Canvas canvas) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getVisible()) c.draw(canvas);                           // 如果光标可见则绘制
        }
        cursorLabel.drawMeasure();                                        // 绘制测量标签
    }

    /**
     * 绘制光标到OpenGL画布
     * 遍历所有可见光标进行绘制，并绘制测量标签
     * @param canvas OpenGL画布
     */
    public void draw(ICanvasGL canvas) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getVisible()) c.draw(canvas);                           // 如果光标可见则绘制
        }
        cursorLabel.drawMeasure(canvas);                                  // 绘制测量标签到OpenGL画布
    }

    //endregion

    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;                                // 当前工作模式，默认YT

    /**
     * 切换工作模式
     * XY模式下仅切换水平光标2的工作模式，并刷新标签位置
     * @param workMode 目标工作模式
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        if (this.mWorkMode == workMode) return;                           // 模式相同则无需切换
        this.mWorkMode = workMode;                                        // 更新当前工作模式
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (TChan.isCursorRow2(c.getLineNameID()))                    // 仅对水平光标2切换模式
                c.switchWorkMode(workMode);                               // 切换光标的工作模式
        }
        cursorLabel.refreshLabelPos();                                    // 刷新标签位置

    }

    //endregion


    //region 设置事件接口
    /**
     * 更新光标测量数据
     */
    public void setData(){

        cursorLabel.setData();                                            // 委托标签管理器更新数据
    }

    /**
     * 设置水平测量标签是否可见
     * @param b true显示，false隐藏
     */
    public void setRowMeasureVisible(boolean b){
        cursorLabel.setRowMeasureVisible(b);                              // 委托标签管理器设置水平测量可见性
    }

    /**
     * 设置垂直测量标签是否可见
     * @param b true显示，false隐藏
     */
    public void setColMeasureVisible(boolean b){
        cursorLabel.setColMeasureVisible(b);                              // 委托标签管理器设置垂直测量可见性
    }

    /**
     * 移动光标标签偏移量
     * @param offsetX X方向偏移
     * @param offsetY Y方向偏移
     */
    public void MoveLabel(int offsetX,int offsetY){
        cursorLabel.MoveLabel(offsetX,offsetY);                           // 委托标签管理器移动标签
    }

    /**
     * 获取水平光标是否可见
     * @return true表示水平光标可见
     */
    public boolean getRowVisible() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (TChan.isCursorRow2(c.getCursorType()))                    // 找到水平光标
                return c.getVisible();                                    // 返回水平光标的可见性
        }
        return false;                                                     // 默认不可见
    }

    /**
     * 获取垂直光标是否可见
     * @return true表示垂直光标可见
     */
    public boolean getColVisible() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (TChan.isCursorCol2(c.getCursorType()))                    // 找到垂直光标
                return c.getVisible();                                    // 返回垂直光标的可见性
        }
        return false;                                                     // 默认不可见
    }

    /**
     * 获取水平光标1的Y坐标位置
     * @return 水平光标1的像素Y坐标
     */
    public double getRow1Position() {
        return curList.get(0).getY();                                     // 列表第0个元素是水平光标1
    }

    /**
     * 获取水平光标2的Y坐标位置
     * @return 水平光标2的像素Y坐标
     */
    public double getRow2Position() {
        return curList.get(1).getY();                                     // 列表第1个元素是水平光标2
    }

    /**
     * 获取垂直光标1的X坐标位置
     * @return 垂直光标1的像素X坐标
     */
    public long getCol1Position() {
        return curList.get(2).getX();                                     // 列表第2个元素是垂直光标1
    }

    /**
     * 获取垂直光标2的X坐标位置
     * @return 垂直光标2的像素X坐标
     */
    public long getCol2Position() {
        return curList.get(3).getX();                                     // 列表第3个元素是垂直光标2
    }

    /**
     * 判断是否选中了水平光标
     * @return true表示选中了水平光标1或水平光标2
     */
    public boolean isRowSelect(){
        return curList.get(0).isSelected() || curList.get(1).isSelected(); // 水平光标1或2被选中
    }

    /***
     * 设置横向光标线是否显示
     * @param visible 是否显示光标线
     */
    public void setRowVisible(int workMode, boolean visible) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (TChan.isCursorRow2(c.getCursorType()))                    // 找到水平光标
                c.setVisible(visible);                                    // 设置可见性
        }
        MeasureManage.getInstance().setRowVisible(workMode, visible);     // 同步更新测量管理器的水平可见性
    }

    /***
     * 设置纵向光标线是否显示
     * @param visible 是否显示光标线
     */
    public void setColVisible(int workMode, boolean visible) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (TChan.isCursorCol2(c.getCursorType()))                    // 找到垂直光标
                c.setVisible(visible);                                    // 设置可见性
        }
        MeasureManage.getInstance().setColVisible(workMode, visible);     // 同步更新测量管理器的垂直可见性
    }

    /***
     * 设置光标切换事件。
     * @param onSelectChangeEvent 光标选择变更事件回调
     */
    public void setOnSelectChangeEvent(IWave.OnSelectChangeEvent onSelectChangeEvent) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            c.setOnSelectChangeEvent(onSelectChangeEvent);                // 设置选择变更事件
        }
    }

    /***
     * 设置光标移动事件
     * @param onMovingWaveEvent 光标移动事件回调
     */
    public void setOnMovingWaveEvent(IWave.OnMovingWaveEvent onMovingWaveEvent) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            c.setOnMovingWaveEvent(onMovingWaveEvent);                    // 设置移动事件
        }
    }

    /**
     * 刷新所有光标显示
     */
    public void refresh(){
        for(Cursor_impIWave c:curList){                                   // 遍历所有光标
            c.refresh();                                                  // 刷新单条光标
        }
    }

    /***
     * 设置光标颜色
     * @param color 目标颜色值
     */
    private void setCursorColor(int color) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            int cc=Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)); // 重新设置透明度为不透明
            c.setColor(cc);                                               // 设置光标颜色
//            c.setColor(color);
        }
    }

    /**
     * 设置光标通道颜色
     * 根据通道号获取对应颜色并应用到所有光标
     * @param ChNo 通道号，-1表示无特定通道
     */
    public void setCursorChannelColor(int ChNo) {
        setCursorColor(TChan.getChannelColor(context,ChNo));              // 根据通道号获取颜色并设置
//        switch (ChNo) {
//            case IWave.Ch1:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch1));
//                break;
//            case IWave.Ch2:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch2));
//                break;
//            case IWave.Ch3:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch3));
//                break;
//            case IWave.Ch4:
//                setCursorColor(App.get().getResources().getColor(R.color.color_Ch4));
//                break;
//        }

    }

    /***
     * 选中光标增加1个像素位移
     */
    public void addPixMove() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                 // 如果光标被选中
                c.movePix(1);                                             // 移动1像素
            }
        }
    }

    /**
     * 加减num个像素
     * @param num 为正增加，为负减少
     */
    public void addPixMove(int num) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                 // 如果光标被选中
                c.movePix(num);                                           // 移动指定像素数
            }
        }
    }

    /**
     * 如果是正数，则两条光标线向中间聚拢，如果是负数，则向两边扩散...
     */
    public void zoomPixMove(int num) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                 // 如果光标被选中
                if (c.getCursorType() == TChan.Cursor_col_1 || c.getCursorType() == TChan.Cursor_row_1) { // 第1条光标
                    c.movePix(num);                                       // 正向移动
                }
                if (c.getCursorType() == TChan.Cursor_col_2 || c.getCursorType() == TChan.Cursor_row_2) { // 第2条光标
                    c.movePix(num * -1);                                  // 反向移动（聚拢/扩散效果）
                }
            }
        }
    }

    /***
     *减一个像素
     */
    public void subPixMove() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                 // 如果光标被选中
                c.movePix(-1);                                            // 减少1像素
            }
        }
    }


    /**
     * 初始化垂直光标X位置到波形区1/4和3/4处
     */
    public void initCursorX() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getCursorType() == TChan.Cursor_col_1) {                // 垂直光标1
                c.setX(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY) / 4); // 设置到1/4宽度处
            }
            if (c.getCursorType() == TChan.Cursor_col_2) {                // 垂直光标2
                c.setX(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY) / 4 * 3); // 设置到3/4宽度处
            }
        }
    }

    /**
     * 初始化水平光标Y位置到波形区1/4和3/4处
     */
    public void initCursorY() {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getCursorType() == TChan.Cursor_row_1) {                // 水平光标1
                c.setY(GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY) / 4); // 设置到1/4高度处
            }
            if (c.getCursorType() == TChan.Cursor_row_2) {                // 水平光标2
                c.setY(GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY) / 4 * 3); // 设置到3/4高度处
            }
        }
    }

    /**
     * 设置指定类型光标的位置
     * @param cursorType 光标类型标识
     * @param position 目标位置（垂直光标设置X，水平光标设置Y）
     */
    public void setCursor(int cursorType, double position) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getCursorType() == cursorType) {                        // 匹配光标类型
                if (TChan.isCursorCol2(cursorType)) {                     // 垂直光标
                    c.setX(Math.round(position));                          // 设置X坐标（四舍五入）
                } else if (TChan.isCursorRow2(cursorType)) {              // 水平光标
                    c.setY(position);                                      // 设置Y坐标
                }
            }
        }
    }

    /**
     * 设置指定类型光标的偏移位置（相对移动）
     * @param cursorType 光标类型标识
     * @param offset 偏移像素数
     */
    public void setCursorOffsetPos(int cursorType,int offset){
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getCursorType() == cursorType) {                        // 匹配光标类型
                if (TChan.isCursorCol2(cursorType)) {                     // 垂直光标
                    c.movePix(offset);                                    // 按偏移量移动
                } else if (TChan.isCursorRow2(cursorType)) {              // 水平光标
                    c.movePix(offset);                                    // 按偏移量移动
                }
            }
        }
    }

    /**
     * 获取指定类型光标的位置
     * @param cursorType 光标类型标识
     * @return 光标位置，0表示未找到
     */
    public long getCursor(int cursorType) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.getCursorType() == cursorType) {                        // 匹配光标类型
                if (TChan.isCursorCol2(cursorType)) {                     // 垂直光标
                    return c.getX();                                      // 返回X坐标
                } else if (TChan.isCursorRow2(cursorType)) {              // 水平光标
                    return Math.round(c.getY());                           // 返回Y坐标（四舍五入）
                }
            }
        }
        return 0;                                                         // 未找到返回0
    }
    //endregion


    //region  桌面操作接口
    /**
     * 取消所有光标的高亮显示
     * 将所有光标颜色的透明度重置为不透明
     */
    public void CancelAllHightShow(){
        for(int i=0;i< curList.size();i++){                                // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            int color= c.getColor();                                       // 获取当前颜色
            int cc=Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)); // 重置透明度为不透明
            c.setColor(cc);                                               // 设置新颜色
        }
    }

    /**
     * 设置指定光标的高亮颜色
     * 将匹配光标的颜色透明度设为不透明（高亮）
     * @param index 光标标识
     */
    public void setSelectHighColor(int index){
        for(int i=0;i<curList.size();i++){                                 // 遍历所有光标
            Cursor_impIWave c=curList.get(i);                              // 获取光标对象
            if (c.getLineNameID()==index){                                 // 匹配光标标识
                int color= c.getColor();                                   // 获取当前颜色
                int cc=Color.argb(0xFF,Color.red(color),Color.green(color),Color.blue(color)); // 设置为不透明（高亮）
                c.setColor(cc);                                           // 设置新颜色
            }
        }
    }

    /***
     * 返回选择的光标线
     * 先检查是否选中了测量标签，再检查是否选中了光标线
     * @param x 按下的x坐标
     * @param y 按下的Y坐标
     * @return 返回-1 没有选中，大于等于0则选中一个。
     */
    public int selectCursor(int x, double y) {
        int finalSelect = cursorLabel.selectMeasure(x, (int) Math.round(y));//先判断是不是测量值方框上
        if (finalSelect >= 0) return finalSelect;                         // 选中了测量标签，直接返回
        for (int i = 0; i < curList.size(); i++) {                         // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            if (!c.getVisible()) continue;                                 // 跳过不可见光标
            if (TChan.isCursorCol2(c.getCursorType())) {                   // 垂直光标
                if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) {     // X坐标在触摸范围内
                    finalSelect = c.getCursorType();                       // 记录选中的光标类型
                }
            } else if (TChan.isCursorRow2(c.getCursorType())) {            // 水平光标
                if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) {    // Y坐标在触摸范围内
                    finalSelect = c.getCursorType();                       // 记录选中的光标类型
                }
            }
        }
        return finalSelect;                                               // 返回选中结果
    }

    /**
     * 根据坐标和指定光标ID选择光标
     * 支持按光标ID分类选择（全选/水平多选/垂直多选）
     * @param x 按下X坐标
     * @param y 按下Y坐标
     * @param cursorId 指定光标类型标识
     * @return 选中的光标类型，-1表示未选中
     */
    public int selectCursor(int x, int y,int cursorId) {
        for (int i = 0; i < curList.size(); i++) {                         // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            if (!c.getVisible()) continue;                                 // 跳过不可见光标
            if (cursorId==TChan.Cursor_all){                               // 全选模式
                if (TChan.isCursorCol2(c.getCursorType())) {               // 垂直光标
                    if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) return c.getCursorType(); // X坐标在范围内则选中
                } else if (TChan.isCursorRow2(c.getCursorType())) {        // 水平光标
                    if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) return c.getCursorType(); // Y坐标在范围内则选中
                }
            }else if (cursorId==TChan.Cursor_row_3 || cursorId==TChan.Cursor_row_4){ // 水平多选模式
                if (TChan.isCursorRow2(c.getCursorType())) {               // 水平光标
                    if ((Math.abs(c.getY() - y) <= CURSOR_OFFSET_SELECT)) return c.getCursorType(); // Y坐标在范围内则选中
                }
            }else if (cursorId==TChan.Cursor_col_3 || cursorId==TChan.Cursor_col_4){ // 垂直多选模式
                if (TChan.isCursorCol2(c.getCursorType())) {               // 垂直光标
                    if (Math.abs(c.getX() - x) <= CURSOR_OFFSET_SELECT) return c.getCursorType(); // X坐标在范围内则选中
                }
            }

            //if ((Math.abs(c.getX()-x)<=CURSOR_OFFSET_SELECT) || (Math.abs(c.getY()-y)<=CURSOR_OFFSET_SELECT)){
            //    return i;
            //}
        }
        return -1;                                                        // 未选中返回-1
    }

    /***
     * 设置选择的光标
     * 选中指定光标，取消其他光标的选中状态
     * @param index 光标的序号
     */
    public void setSelectCursor(int index) {
        for (int i = 0; i < curList.size(); i++) {                         // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            if (c.getCursorType() == index) {                              // 匹配指定光标
                c.setSelected(true);                                      // 选中该光标
            } else {                                                       // 其他光标
                c.setSelected(false);                                     // 取消选中
            }
        }
    }

    /***
     * 多选择光标
     * 同时选中两个指定光标，取消其他光标的选中状态
     * @param index1 第一个光标标识
     * @param index2 第二个光标标识
     */
    public void setMultiSelectCursor(int index1, int index2) {
        for (int i = 0; i < curList.size(); i++) {                         // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            if (c.getCursorType() == index1 || c.getCursorType() == index2) { // 匹配两个指定光标
                c.setSelected(true);                                      // 选中
            } else {                                                       // 其他光标
                c.setSelected(false);                                     // 取消选中
            }
        }
    }

    /**
     * 多选择光标（4个光标）
     * 同时选中四个指定光标，取消其他光标的选中状态
     * @param index1 第一个光标标识
     * @param index2 第二个光标标识
     * @param index3 第三个光标标识
     * @param index4 第四个光标标识
     */
    public void setMultiSelectCursor(int index1, int index2,int index3,int index4) {
        for (int i = 0; i < curList.size(); i++) {                         // 遍历所有光标
            Cursor_impIWave c = curList.get(i);                            // 获取光标对象
            if (c.getCursorType() == index1 || c.getCursorType() == index2 || c.getCursorType()==index3 || c.getCursorType()==index4) { // 匹配四个指定光标
                c.setSelected(true);                                      // 选中
            } else {                                                       // 其他光标
                c.setSelected(false);                                     // 取消选中
            }
        }
    }

    /***
     * 移动选中的光标（相对偏移量）
     * 水平光标改变Y坐标，垂直光标改变X坐标
     * @param x X方向偏移量
     * @param y Y方向偏移量
     */
    public void moveSelectCursor(int x, double y) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                  // 如果光标被选中
                if (TChan.isCursorRow2(c.getCursorType())) {               // 水平光标
                    c.setY(c.getY()-y);                                    // 减去Y偏移量（向上为正）
                } else if (TChan.isCursorCol2(c.getCursorType())) {        // 垂直光标
                    c.setX(c.getX()-x);                                    // 减去X偏移量（向左为正）
                }
            }
        }
    }

    /***
     * 多光标线移动（相对坐标）
     * 水平光标改变Y坐标，垂直光标改变X坐标
     * @param x X方向偏移量
     * @param y Y方向偏移量
     */
    public void moveMultiSelectCursor(int x, int y) {
        for (Cursor_impIWave c : curList) {                                // 遍历所有光标
            if (c.isSelected() == true) {                                  // 如果光标被选中
                if (TChan.isCursorRow2(c.getCursorType())) {               // 水平光标
                    c.setY(c.getY() - y);                                 // 减去Y偏移量
                } else if (TChan.isCursorCol2(c.getCursorType())) {        // 垂直光标
                    c.setX(c.getX() - x);                                 // 减去X偏移量
                }
            }
        }
    }

    /**
     * 获取多光标选择结果
     * 根据光标选中状态判断多选类型（全选/水平多选/垂直多选）
     * @param x 按下X坐标（未使用，保留接口一致性）
     * @param y 按下Y坐标（未使用，保留接口一致性）
     * @return 多光标选择标识，-1表示无多选
     */
    public int getMultiSelectCursor(int x,int y){
        if (getRowVisible() && getColVisible()){                           // 水平和垂直光标都可见
            if (curList.get(0).isSelected() && curList.get(1).isSelected() // 4个光标都选中
                    && curList.get(2).isSelected() && curList.get(3).isSelected()){
                return TChan.Cursor_all;                                   // 返回全选标识
            }else if (curList.get(0).isSelected() && curList.get(1).isSelected()){ // 水平2条都选中
                return TChan.Cursor_row_4;                                 // 返回水平多选标识
            }else if (curList.get(2).isSelected() && curList.get(3).isSelected()){ // 垂直2条都选中
                return TChan.Cursor_col_4;                                 // 返回垂直多选标识
            }

        }else if (getRowVisible()){                                       // 仅水平光标可见
            if (curList.get(0).isSelected() && curList.get(1).isSelected()){ // 水平2条都选中
                return TChan.Cursor_row_4;                                 // 返回水平多选标识
            }
        }else if (getColVisible()){                                       // 仅垂直光标可见
            if (curList.get(2).isSelected() && curList.get(3).isSelected()){ // 垂直2条都选中
                return TChan.Cursor_col_4;                                 // 返回垂直多选标识
            }
        }
        return -1;                                                        // 无多选返回-1
    }

    /**
     * 获取当前选中的光标标识
     * 根据光标可见性和选中状态确定当前选中了哪条光标
     * @return 光标类型标识，0表示未选中
     */
    public int getCurrSelectCursor(){
        int select=0;                                                     // 初始化选中结果
        if (getColVisible() && getRowVisible()){                          // 水平和垂直光标都可见
            if (curList.get(0).isSelected() && curList.get(1).isSelected()){ // 水平2条都选中
                select= TChan.Cursor_row_3;                               // 水平双光标跟踪模式
            }else if (curList.get(2).isSelected() && curList.get(3).isSelected()){ // 垂直2条都选中
                select=TChan.Cursor_col_3;                                // 垂直双光标跟踪模式
            }else {                                                        // 单选模式
                for (Cursor_impIWave c : curList) {                        // 遍历所有光标
                    if (c.isSelected()) {                                  // 找到选中的光标
                        select = c.getLineNameID();                        // 记录选中光标标识
                        break;                                            // 退出循环
                    }
                }
            }
            if (select==0){ select= TChan.Cursor_row_1;setSelectCursor(select);} // 无选中则默认选中水平光标1
        }else if(getColVisible()){                                        // 仅垂直光标可见
            if (curList.get(2).isSelected() && curList.get(3).isSelected()){ // 垂直2条都选中
                select= TChan.Cursor_col_3;                               // 垂直双光标跟踪模式
            }else if (curList.get(2).isSelected()) {select=TChan.Cursor_col_1; } // 垂直光标1选中
            else if (curList.get(3).isSelected()){select= TChan.Cursor_col_2;} // 垂直光标2选中
            else {select=TChan.Cursor_col_1;setSelectCursor(select); }    // 无选中则默认选中垂直光标1
        }else if (getRowVisible()){                                       // 仅水平光标可见
            if (curList.get(0).isSelected() &&  curList.get(1).isSelected()){ // 水平2条都选中
                select= TChan.Cursor_row_3;                               // 水平双光标跟踪模式
            }else if (curList.get(0).isSelected()) select=TChan.Cursor_row_1; // 水平光标1选中
            else if (curList.get(1).isSelected()) select= TChan.Cursor_row_2; // 水平光标2选中
            else {select= TChan.Cursor_row_1;setSelectCursor(select); }   // 无选中则默认选中水平光标1
        }else {                                                           // 无光标可见
            return select=0;                                              // 返回0表示无选中
        }
        return select;                                                    // 返回选中结果
    }
    //endregion

}
