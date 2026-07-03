package com.micsig.tbook.tbookscope.main.mainbottom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                   MainBottomMeasureItem - 底部测量项显示组件                     │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   主界面底部测量项显示容器，用于展示和管理测量项列表，支持拖拽排序和删除             │
 * │                                                                              │
 * │ 【核心职责】                                                                   │
 * │   1. 显示测量项列表（电压、时间、频率等测量结果）                                 │
 * │   2. 支持测量项的长按选择和拖拽移动                                             │
 * │   3. 实现测量项位置的交换和删除功能                                             │
 * │   4. 根据工作模式和工作状态控制显示隐藏                                         │
 * │   5. 响应测量数据刷新和对话框状态变化                                           │
 * │                                                                              │
 * │ 【架构设计】                                                                   │
 * │   继承ConstraintLayout的自定义视图组件                                         │
 * │   采用GridLayout作为测量项容器，支持动态添加和移除                                │
 * │   使用RxBus进行消息订阅，响应测量数据更新                                        │
 * │   内置拖拽监听器实现测量项的交互操作                                            │
 * │                                                                              │
 * │ 【数据流向】                                                                   │
 * │   MeasureManage → MainBottomMeasureItem → TextView列表 → 界面显示             │
 * │   用户交互 → DragTouchListener → 位置交换逻辑 → MeasureManage数据更新           │
 * │   RxBus消息 → MainBottomMeasureItem → 显示状态更新                             │
 * │                                                                              │
 * │ 【依赖关系】                                                                   │
 * │   被依赖：MainViewGroup                                                       │
 * │   依赖：MeasureManage（测量数据管理）、RxBus（消息总线）、WorkModeManage（工作模式）│
 * │         CacheUtil（缓存工具）、TChan（通道工具）、GlobalVar（全局变量）           │
 * │                                                                              │
 * │ 【使用场景】                                                                   │
 * │   1. YT模式下显示测量项列表                                                    │
 * │   2. XY模式下隐藏测量项                                                        │
 * │   3. 串口文本模式下隐藏测量项                                                  │
 * │   4. 测量统计对话框打开时隐藏测量项                                            │
 * │   5. 用户拖拽测量项调整位置                                                    │
 * │   6. 用户拖拽测量项到列表外删除                                                │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @auother Liwb
 * @description: 底部测量项显示和管理组件，支持拖拽排序和删除
 * @data:2023-10-26 16:19
 */
public class MainBottomMeasureItem extends ConstraintLayout {
    
    /** 上下文对象，用于获取资源和创建视图 */ // 上下文引用
    private Context context;
    
    /** 根视图容器，包含所有子组件 */ // 根视图引用
    private ViewGroup rootView;
    
    /**
     * 单参数构造函数，在代码中动态创建时使用
     * 
     * @param context 上下文对象，用于获取资源和访问环境
     */
    public MainBottomMeasureItem(@NonNull Context context) {
        this(context,null); // 调用双参数构造函数
    }

    /**
     * 双参数构造函数，在XML布局中创建时使用
     * 
     * @param context 上下文对象
     * @param attrs   XML属性集，包含布局参数
     */
    public MainBottomMeasureItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0); // 调用三参数构造函数
    }

    /**
     * 三参数构造函数，包含默认样式
     * 
     * @param context      上下文对象
     * @param attrs        XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MainBottomMeasureItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造函数
        this.context=context; // 保存上下文引用
        initView(); // 初始化视图组件
        initControls(); // 初始化控制逻辑
    }

    /** 测量项空白占位布局容器 */ // 空白占位布局
    private ConstraintLayout spaceLayout;
    
    /** 测量项列表网格布局容器 */ // 网格布局容器
    private GridLayout listView;
    
    /** 测量项文本视图列表，用于显示测量结果 */ // 文本视图列表
    private List<TextView> listTxtView=new ArrayList<>();
    
    /** 左箭头按钮，用于滚动列表 */ // 左箭头按钮
    private ImageView btnLeft,btnRight;
    
    /** 主视图组引用，用于访问主界面功能 */ // 主视图组引用
    private MainViewGroup mainViewGroup;
    
    /** 删除测量项的拖动范围最小值，向上拖动超过此值则删除 */ // 删除阈值常量
    private static final int DELETE_ITEM_INSTANCE = 50;
    
    /**
     * 初始化视图组件，创建测量项列表容器和各个测量项
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        rootView= (ViewGroup) View.inflate(context, R.layout.layout_main_bottom_measure_item,this); // 加载布局文件
        listView=rootView.findViewById(R.id.listView); // 获取网格布局容器
        spaceLayout = rootView.findViewById(R.id.measureSpaceLayout); // 获取空白占位布局
        for(int i=0;i< GlobalVar.get().getMeasureItemCount();i++){ // 循环创建测量项视图
            TextView txt=new TextView(context); // 创建文本视图
            txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,18); // 设置字体大小
            txt.setClickable(true); // 设置可点击
            txt.setGravity(Gravity.CENTER | Gravity.LEFT); // 设置文本对齐方式
            txt.setEllipsize(TextUtils.TruncateAt.END); // 设置文本超出时省略
            txt.setSingleLine(); // 设置单行显示
            txt.setBackground(null); // 清除默认背景
            txt.setOnLongClickListener(v->{ // 设置长按监听器
                startSelection(v); // 开始选择拖拽
                return true; // 返回true表示消费事件
            });
            txt.setOnTouchListener(new DragTouchListener()); // 设置触摸监听器实现拖拽

            listView.addView(txt); // 将文本视图添加到网格布局
            listTxtView.add(txt); // 将文本视图添加到列表管理
            txt.getLayoutParams().height=(int) getResources().getDimension(R.dimen.mainBottomMeasureItemHeight0); // 设置初始高度
            txt.getLayoutParams().width = 180; // 设置固定宽度
        }
        btnLeft=rootView.findViewById(R.id.leftArrow); // 获取左箭头按钮
        btnRight=rootView.findViewById(R.id.rightArrow); // 获取右箭头按钮

        List<MeasureManage.MeasureItemStruct> list=MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量项列表

        MeasureManage.getInstance().getMeasureItem().OnRefresh=(b)->{ // 设置刷新回调
            updateTxtView(); // 更新文本视图显示
        };

    }

    /** 当前选中的测量项位置索引 */ // 选中位置索引
    int selectPosition = -1;

    /**
     * 开始选择拖拽操作，记录被选中的视图位置
     * 
     * @param view 被长按选中的视图
     */
    private void startSelection(View view) {
        selectPosition = indexOfChild(view); // 记录选中位置
    }

    /**
     * 从视图创建位图图像，用于拖拽显示
     * 
     * @param view 需要转换为位图的视图
     * @return Bitmap 视图的位图表示
     */
    private Bitmap createBitmapFromView(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) { // 检查视图尺寸是否有效
            view.measure( // 测量视图尺寸
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), // 宽度测量规格
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED) // 高度测量规格
            );
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()); // 布置视图
        }

        Matrix matrix = new Matrix(); // 创建变换矩阵
        if(view.getAnimation() != null ){ // 检查是否有动画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 检查SDK版本
                matrix = view.getAnimationMatrix(); // 获取动画矩阵
            }
        } else {
            matrix = new Matrix(); // 创建新矩阵
            matrix.setTranslate(view.getTranslationX(), view.getTranslationY()); // 设置平移
            matrix.preScale(view.getScaleX(), view.getScaleY()); // 设置缩放
            matrix.preRotate(view.getRotation(), view.getPivotX(), view.getPivotY()); // 设置旋转
        }

        RectF bounds = new RectF(0, 0, view.getWidth(), view.getHeight()); // 创建边界矩形
        matrix.mapRect(bounds); // 应用矩阵变换

        int width = (int) bounds.width(); // 获取变换后宽度
        int height = (int) bounds.height(); // 获取变换后高度
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // 创建位图
        Canvas canvas = new Canvas(bitmap); // 创建画布

        canvas.translate(-bounds.left, -bounds.top); // 平移画布
        canvas.concat(matrix); // 应用矩阵
        view.draw(canvas); // 绘制视图到画布
        return bitmap; // 返回位图
    }

    /**
     * 拖拽触摸监听器，实现测量项的拖拽移动、位置交换和删除功能
     */
    private class DragTouchListener implements OnTouchListener {
        /** 拖拽偏移量X坐标 */ // X偏移量
        private float dx, dy;
        /** 触摸按下时的坐标 */ // 按下坐标
        private float downX, downY, deltaX, deltaY;

        /**
         * 处理触摸事件，实现拖拽逻辑
         * 
         * @param view  被触摸的视图
         * @param event 触摸事件
         * @return boolean 是否消费事件
         */
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) { // 根据动作类型处理
                case MotionEvent.ACTION_DOWN: // 按下事件
                    if (isMeasureItemClickEnable()) { // 检查是否允许点击
                        OnTxtClickListener(view); // 触发点击回调
                    }
                    dx = view.getX() - event.getRawX(); // 计算X偏移量
                    dy = view.getY() - event.getRawY(); // 计算Y偏移量
                    downX = event.getRawX(); // 记录按下X坐标
                    downY = event.getRawY(); // 记录按下Y坐标
                    view.animate() // 开始缩放动画
                            .scaleX(1.16f) // 放大X比例
                            .scaleY(1.16f) // 放大Y比例
                            .setDuration(100) // 动画持续时间
                            .start(); // 启动动画
                    break;
                case MotionEvent.ACTION_MOVE: // 移动事件
                    view.animate() // 移动动画
                            .x(event.getRawX() + dx) // 设置X位置
                            .y(event.getRawY() + dy) // 设置Y位置
                            .setDuration(0) // 无延迟
                            .start(); // 启动动画
                    break;
                case MotionEvent.ACTION_CANCEL: // 取消事件
                case MotionEvent.ACTION_UP: // 松开事件
                    deltaY = Math.abs(event.getRawY() - downY); // 计算Y位移量
                    deltaX = Math.abs(event.getRawX() - downX); // 计算X位移量
                    if (deltaY < 5 && deltaX < 5) { // 判断是否为点击而非拖拽
                        view.animate() // 还原缩放
                                .scaleX(1.0f) // 恢复X比例
                                .scaleY(1.0f) // 恢复Y比例
                                .setDuration(100) // 动画持续时间
                                .start(); // 启动动画
                        break; // 结束处理
                    }
                    checkPositionSwap(view); // 检查位置交换
                    view.animate() // 还原所有变换
                            .translationX(0) // 清除X平移
                            .translationY(0) // 清除Y平移
                            .translationZ(0) // 清除Z平移
                            .scaleX(1f) // 恢复X缩放
                            .scaleY(1f) // 恢复Y缩放
                            .setDuration(0) // 无延迟
                            .start(); // 启动动画
                    break;
            }
            return true; // 返回true表示消费事件
        }
    }

    /**
     * 检查拖拽后的位置交换或删除操作
     * 
     * @param movingView 正在移动的视图
     */
    private void checkPositionSwap(View movingView) {
        Rect rect = new Rect(); // 创建矩形
        movingView.getHitRect(rect); // 获取视图矩形区域
        Point point = new Point(); // 创建点对象
        point.x = (rect.left + rect.right) / 2; // 计算中心X坐标
        point.y = (rect.top + rect.bottom) / 2 + Tools.getViewRect(listView).top; // 计算中心Y坐标

        Rect rectListView = Tools.getViewRect(listView); // 获取列表矩形
        int measureListCount = MeasureManage.getInstance().getMeasureItem().getMeasureList().size(); // 获取测量项数量
        Logger.i("MainBottomMeasureItem movingView rect= " + rect + " point= " + point + " ,rectListView.top= " + rectListView + " ,measureListCount= " + measureListCount); // 记录日志
        if (rectListView.top - point.y > DELETE_ITEM_INSTANCE) { // 判断是否超出删除范围
            deleteSelectMeasure(movingView); // 删除测量项
        } else {
            for (int i = 0; i < listView.getChildCount(); i++) { // 遍历所有子视图
                View target = listView.getChildAt(i); // 获取目标视图
                if (target == movingView) continue; // 跳过自身
                Rect targetRect = Tools.getViewRect(target); // 获取目标矩形
                Logger.i("MainBottomMeasureItem i= " + i + " target rect= " + targetRect + " contains= " + targetRect.contains(point.x, point.y)); // 记录日志
                if (targetRect.contains(point.x, point.y) || (i >= measureListCount && point.x > targetRect.left && point.x < targetRect.right && point.y > rectListView.top)) { // 判断是否在目标区域内
                    changeItemView(listView.indexOfChild(movingView), i); // 交换位置
                    break; // 结束循环
                } else if ((point.x > targetRect.left && point.x < targetRect.right && point.y > rectListView.bottom)) { // 判断是否在底部区域
                    changeItemView(listView.indexOfChild(movingView), i % 10 + 30); // 移动到新行
                    break; // 结束循环
                }
            }
        }
        changeItemViewHeight(); // 更新视图高度
    }

    /**
     * 删除被选中的测量项
     * 
     * @param movingView 正在拖拽的视图
     */
    private void deleteSelectMeasure(View movingView) {
        int tempIndex = listView.indexOfChild(movingView); // 获取视图索引
        int emptyNum = 0; // 空位计数
        for (int i = 0; i < tempIndex; i++) { // 遍历前面的视图
            if (listView.getChildAt(i).getVisibility() != View.VISIBLE) { // 检查是否隐藏
                emptyNum++; // 增加空位计数
            }
        }
        RxBus.getInstance().post(RxEnum.MQ_MSG_DELETE_MEASURE_ITEM_POS, movingView.getTag().toString()); // 发送删除消息
    }

    /**
     * 改变测量项视图的位置，处理位置交换和插入逻辑
     * 
     * @param fromPos 原位置索引
     * @param toPos   目标位置索引
     */
    private void changeItemView(int fromPos, int toPos) {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量列表
        int oldSize = list.size(); // 获取原列表大小
        if(toPos >= oldSize) { // 检查目标位置是否超出范围
            for (int i = oldSize; i <= toPos; i++) { // 填充空位
                MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(-1,-1, -1, "", ""); // 创建空测量项
                MeasureManage.getInstance().getMeasureItem().addEmptyMeasureItem(i, item); // 添加空项
            }
        }
        list = MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 重新获取列表
        String updateStr  = ""; // 更新字符串
        boolean isSwap; // 交换标识
        if (list.get(toPos).getChannelId() < 0) { // 检查目标位置是否为空
            Tools.swapElement(list, fromPos, toPos); // 交换元素
            isSwap = true; // 设置交换标识
        } else {
            moveItemView(fromPos, toPos); // 移动视图
            isSwap = false; // 设置移动标识
        }
        updateStr = fromPos + "," + toPos + "," + isSwap; // 构建更新字符串
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_MEASURE_ITEM_POS, updateStr); // 发送更新消息

        MeasureManage.getInstance().getMeasureItem().clearRowEmpty(); // 清除空行
        updateTxtView(); // 更新视图显示

    }

    /**
     * 移动测量项到新位置，处理位置调整和空位填充
     * 
     * @param fromPos 原位置索引
     * @param toPos   目标位置索引
     */
    private void moveItemView(int fromPos, int toPos) {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量列表
        MeasureManage.MeasureItemStruct itemFrom = list.get(fromPos); // 获取源项
        MeasureManage.MeasureItemStruct itemTo = list.get(toPos); // 获取目标项
        MeasureManage.MeasureItemStruct itemEmpty = MeasureManage.getInstance().new MeasureItemStruct(-1,-1, -1, "", ""); // 创建空项
        if (fromPos < toPos) { // 向后移动
            list.set(fromPos, itemEmpty); // 设置原位置为空
            if (list.size() == GlobalVar.get().getMeasureItemCount()) { // 检查是否满容量
                boolean hasEmpty = false; // 空位存在标识
                int emptyIndex = -1; // 空位索引
                for (int i = toPos; i < list.size(); i++) { // 向后查找空位
                    if (list.get(i).getChannelId() < 0) { // 检查是否为空
                        hasEmpty = true; // 设置空位存在
                        emptyIndex = i; // 记录空位索引
                        break; // 结束查找
                    }
                }
                if (hasEmpty) { // 如果有空位
                    list.remove(emptyIndex); // 移除空位
                    list.add(toPos, itemFrom); // 添加到目标位置
                } else { // 无空位
                    MeasureManage.MeasureItemStruct itemLast = list.get(list.size() - 1); // 获取最后一项
                    list.remove(itemLast); // 移除最后一项
                    list.add(toPos, itemFrom); // 添加到目标位置
                    for (int i = 0; i < list.size(); i++) { // 查找空位
                        if (list.get(i).getChannelId() < 0) { // 检查是否为空
                            list.set(i, itemLast); // 将最后一项放到空位
                            break; // 结束查找
                        }
                    }
                }
            } else {
                for (int i = toPos; i < list.size(); i++) { // 向后查找空位
                    if (list.get(i).getChannelId() < 0) { // 检查是否为空
                        list.remove(i); // 移除空位
                        break; // 结束查找
                    }
                }
                list.add(toPos, itemFrom); // 添加到目标位置
            }
        } else { // 向前移动
            list.set(fromPos, itemEmpty); // 设置原位置为空

            boolean hasEmpty = false; // 空位存在标识
            int emptyIndex = -1; // 空位索引
            for (int i = toPos; i <= fromPos; i++) { // 在范围内查找空位
                if (list.get(i).getChannelId() < 0) { // 检查是否为空
                    hasEmpty = true; // 设置空位存在
                    emptyIndex = i; // 记录空位索引
                    break; // 结束查找
                }
            }
            if (hasEmpty) { // 如果有空位
                list.remove(emptyIndex); // 移除空位
                list.add(toPos, itemFrom); // 添加到目标位置
            }
        }
    }

    /**
     * 交换两个测量项的位置（备用方案）
     * 
     * @param fromPos 原位置索引
     * @param toPos   目标位置索引
     */
    private void swapItems(int fromPos, int toPos) {
        if(fromPos == toPos || toPos >= listView.getChildCount()) return; // 检查参数有效性
        Logger.i("MainBottomMeasureItem swapItems from= " + fromPos + " ,to= " + toPos); // 记录日志
        View fromView = listView.getChildAt(fromPos); // 获取源视图
        View toView =  listView.getChildAt(toPos); // 获取目标视图

        GridLayout.LayoutParams params1 = (GridLayout.LayoutParams) fromView.getLayoutParams(); // 获取源参数
        GridLayout.LayoutParams params2 = (GridLayout.LayoutParams) toView.getLayoutParams(); // 获取目标参数

        GridLayout.Spec row1 = params1.rowSpec; // 获取源行规格
        GridLayout.Spec col1 = params1.columnSpec; // 获取源列规格

        GridLayout.Spec tempRow1 = row1; // 临时保存行规格
        GridLayout.Spec tempCol1 = col1; // 临时保存列规格

        params1.rowSpec = params2.rowSpec; // 交换行规格
        params1.columnSpec = params2.columnSpec; // 交换列规格

        params2.rowSpec = tempRow1; // 设置目标行规格
        params2.columnSpec = tempCol1; // 设置目标列规格

        fromView.setLayoutParams(params1); // 应用源参数
        toView.setLayoutParams(params2); // 应用目标参数
        Collections.swap(listTxtView, fromPos, toPos); // 交换列表位置
    }

    /**
     * 初始化控制逻辑，订阅RxBus消息
     */
    private void initControls(){

        RxBus.getInstance().getObservable(RxEnum.DIALOG_OPEN).subscribe(onDialogOpen); // 订阅对话框打开消息
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CLOSE).subscribe(onDialogClose); // 订阅对话框关闭消息

        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(onSerialTxtChange); // 订阅分段状态消息
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(onWorkModeChange); // 订阅工作模式变化
    }

    /**
     * 处理测量项文本视图的点击事件
     * 
     * @param view 被点击的视图
     */
    private void OnTxtClickListener(View view) {
        int idx= Tools.indexOf(listTxtView,(v)->v.equals(view)); // 查找点击项索引
        MeasureManage.getInstance().getMeasureItem().setSelectItem(idx); // 设置选中项
    }

    /**
     * 更测量项文本视图的显示内容
     */
    private void updateTxtView(){
        List<MeasureManage.MeasureItemStruct> list=MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量列表
        for(int i=0;i<listTxtView.size();i++){ // 遍历所有文本视图
            TextView txt= listTxtView.get(i); // 获取文本视图
            if (i>=list.size()){ // 检查是否超出列表范围
                txt.setText(""); // 清空文本
                txt.setBackground(null); // 清除背景
                txt.setVisibility(GONE); // 设置隐藏
                continue; // 继续下一个
            }
            txt.setVisibility(VISIBLE); // 设置可见
            MeasureManage.MeasureItemStruct bean=list.get(i); // 获取测量项数据
            if (bean.getChannelId() < 0) { // 检查是否为空项
                txt.setVisibility(View.INVISIBLE); // 设置不可见
            }
            txt.setTag(bean.getChannelId() + " " + bean.getMeasureId()); // 设置标签
            txt.setText(MeasureManage.getEnclosedNumber(bean.getNo() + 1) + bean.getMeasureName()+":"+bean.getData()); // 设置文本内容
            txt.setTextColor(TChan.getChannelColor(context, bean.getChannelId())); // 设置文本颜色
            if (list.get(i).isSelected() && isMeasureItemClickEnable()){ // 检查是否选中且可点击
                txt.setBackgroundResource(R.drawable.measure_item_select); // 设置选中背景
            }else {
                txt.setBackground(null); // 清除背景
            }

        }
    }

    /**
     * 根据测量项行数调整视图高度
     */
    public void changeItemViewHeight() {
        int row = MeasureManage.getInstance().getMeasureItem().getMeasureRowCount(); // 获取行数
        if (row == 1) { // 单行情况
            for (int i = 0; i < listView.getChildCount(); i++) { // 遍历子视图
                ViewGroup.LayoutParams layoutParams = listView.getChildAt(i).getLayoutParams(); // 获取布局参数
                layoutParams.height = (int) getResources().getDimension(R.dimen.mainBottomMeasureItemHeight0); // 设置单行高度
                listView.getChildAt(i).setLayoutParams(layoutParams); // 应用参数
            }
        } else { // 多行情况
            for (int i = 0; i < listView.getChildCount(); i++) { // 遍历子视图
                ViewGroup.LayoutParams layoutParams = listView.getChildAt(i).getLayoutParams(); // 获取布局参数
                layoutParams.height = (int) getResources().getDimension(R.dimen.mainBottomMeasureItemHeight1); // 设置多行高度
                listView.getChildAt(i).setLayoutParams(layoutParams); // 应用参数
            }
        }
        requestLayout(); // 请求重新布局
    }

    /**
     * 判断测量项是否允许点击选择
     * 
     * @return boolean 是否允许点击
     */
    private boolean isMeasureItemClickEnable() {
        return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR); // 从缓存获取设置
    }

    /**
     * 隐藏测量项列表
     */
    private void hideMeasureItem(){
        listView.setVisibility(INVISIBLE); // 设置不可见
    }
    
    /**
     * 显示测量项列表
     */
    private void showMeasureItem(){
        listView.setVisibility(VISIBLE); // 设置可见
    }
    
    /**
     * 对话框打开消息消费者，处理测量统计对话框打开时隐藏测量项
     */
    private Consumer<Integer> onDialogOpen = integer -> {
        if (integer==MainViewGroup.DIALOG_MEASURE_STATICS){ // 检查是否为测量统计对话框
            hideMeasureItem(); // 隐藏测量项
        }
    };
    
    /**
     * 对话框关闭消息消费者，处理对话框关闭时恢复测量项显示
     */
    private Consumer<Integer> onDialogClose=(i)->{
        if (i==MainViewGroup.DIALOG_MEASURE_STATICS && WorkModeManage.getInstance().getmWorkMode()!=IWorkMode.WorkMode_XY){ // 检查条件
            showMeasureItem(); // 显示测量项
        }
    };


    /**
     * 分段状态变化消息消费者，控制测量项的显示隐藏
     */
    private Consumer<TopMsgSegmentedState> onSerialTxtChange=(msg)->{
        boolean b= CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT) ; // 获取串口文本状态
        boolean b1=WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_XY; // 获取XY模式状态
        boolean b2=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 获取全测量状态

        if (b || b1 || b2) hideMeasureItem(); // 条件满足时隐藏
        else showMeasureItem(); // 否则显示
    };
    
    /**
     * 工作模式变化消息消费者，XY模式下隐藏测量项
     */
    private Consumer<WorkModeBean> onWorkModeChange=(msg)->{

         if (WorkModeManage.getInstance().getmWorkMode()== IWorkMode.WorkMode_XY){ // 检查是否为XY模式
             hideMeasureItem(); // 隐藏测量项
         }else {
             showMeasureItem(); // 显示测量项
         }
    };
}