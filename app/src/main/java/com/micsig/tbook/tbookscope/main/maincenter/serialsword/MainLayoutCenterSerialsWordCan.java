package com.micsig.tbook.tbookscope.main.maincenter.serialsword;  // 定义串口CAN总线文本显示Fragment类的包路径

import android.os.Bundle;  // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater;  // 导入布局填充器类，用于创建视图
import android.view.MotionEvent;  // 导入触摸事件类，用于处理滚动交互
import android.view.View;  // 导入视图基类
import android.view.ViewGroup;  // 导入视图组类，Fragment容器
import android.widget.Button;  // 导入按钮控件，用于清空数据
import android.widget.RelativeLayout;  // 导入相对布局，滚动条容器
import android.widget.TextView;  // 导入文本视图控件，用于显示统计信息

import androidx.annotation.Nullable;  // 导入Nullable注解，标记可空参数
import androidx.fragment.app.Fragment;  // 导入Fragment基类
import androidx.recyclerview.widget.LinearLayoutManager;  // 导入线性布局管理器，用于RecyclerView

import com.micsig.tbook.tbookscope.LoadCache;  // 导入加载缓存消息类
import com.micsig.tbook.tbookscope.R;  // 导入资源类，访问布局和控件ID
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;  // 导入右侧串口布局类，获取串口类型常量
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound;  // 导入声音播放工具类
import com.micsig.tbook.tbookscope.tools.Tools;  // 导入通用工具类
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;  // 导入串口总线管理类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;  // 导入串口总线文本数据结构类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;  // 导入串口总线文本解析类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialTxtBuffer;  // 导入串口文本缓冲区类

import java.text.NumberFormat;  // 导入数字格式化类，用于百分比显示
import java.util.ArrayList;  // 导入动态数组类
import java.util.Iterator;  // 导入迭代器类
import java.util.concurrent.LinkedBlockingQueue;  // 导入阻塞队列类，线程安全数据存储

import io.reactivex.rxjava3.functions.Consumer;  // 导入RxJava消费者接口


/**
 * ***********************************************************************************
 * * CAN总线文本显示Fragment类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - CAN总线协议解码文本数据的显示和统计模块
 * *
 * * 【核心职责】
 * *   1. 显示CAN总线解码后的文本数据列表（帧ID、数据、时间戳等）
 * *   2. 统计并显示CAN总线帧总数、空间帧数、错误帧数及错误率
 * *   3. 支持运行模式实时刷新和停止模式历史查看
 * *   4. 实现触摸滚动和外部按键滚动功能
 * *   5. 提供时间显示格式切换（毫秒显示开关）
 * *   6. 支持清空缓存数据功能
 * *
 * * 【架构设计】
 * *   继承Fragment，实现IForceRefreshUI强制刷新接口：
 * *   - 使用RecyclerView显示全屏数据列表（停止模式）
 * *   - 使用自定义SingleScreenTextView显示单屏数据（运行模式）
 * *   - 通过LinkedBlockingQueue获取线程安全的CAN数据队列
 * *   - 利用RxBus订阅缓存加载事件
 * *   - 采用自适应滚动条显示数据位置
 * *
 * * 【数据流向】
 * *   输入：
 * *     → SerialBusManage获取CAN数据队列和缓冲区
 * *     → CacheUtil读取运行状态和通道类型
 * *     → RxBus接收LoadCache缓存加载事件
 * *     → 用户触摸事件触发滚动
 * *   输出：
 * *     → RecyclerView/SingleScreenTextView显示CAN文本列表
 * *     → TextView显示帧统计信息（总数、空间帧、错误帧）
 * *     → SerialBusManage清空缓冲区
 * *
 * * 【依赖关系】
 * *   上层依赖：MainLayoutCenterSerialsWordDetail父容器Fragment
 * *   下层依赖：MainAdapterCenterSerialsWordCan适配器、SerialBusManage数据管理
 * *   平级依赖：CacheUtil、RxBus、PlaySound、Tools等工具类
 * *
 * * 【使用场景】
 * *   当用户配置通道为CAN协议时，显示CAN总线解码的文本数据
 * *   在Run模式实时刷新显示最新数据，在Stop模式支持滚动查看历史数据
 * ***********************************************************************************
 */
public class MainLayoutCenterSerialsWordCan extends Fragment implements SerialBusManage.IForceRefreshUI {
    private Button btnClear;  // 清空按钮，用于清除所有CAN数据缓存
    private TextView time;  // 时间显示控件，点击切换毫秒显示开关
    private TextView tvMsgCanTotalTitle, tvMsgCanSpaceTitle, tvMsgCanErrorTitle;  // CAN帧统计标题文本控件
    private TextView tvMsgCanTotalDetail, tvMsgCanSpaceDetail, tvMsgCanErrorDetail;  // CAN帧统计详情数值文本控件
    private UnFlingRecyclerView allScreenView;  // 全屏数据列表视图，用于Stop模式显示所有历史数据
    private LinearLayoutManager allScreenLayoutManager;  // 线性布局管理器，管理RecyclerView布局
    private MainAdapterCenterSerialsWordCan allScreenAdapter;  // CAN数据适配器，绑定数据到视图
    private ArrayList<SerialBusTxtStruct.CanStruct> allScreenList;  // 全屏数据列表，存储所有CAN帧数据
    private View singleScreenScrollBar;  // 单屏模式滚动条视图，显示数据位置指示
    private SerialsWordCanSingleScreenTextView singleScreenView;  // 单屏数据视图，用于Run模式高效显示
    private ArrayList<SerialBusTxtStruct.CanStruct> singleScreenList;  // 单屏数据列表，存储当前显示的CAN帧数据

    private int chType = ISerialsWord.TYPE_S1;  // 通道类型，默认为S1通道

    /**
     * 设置通道类型
     * @param chType 通道类型常量（TYPE_S1/TYPE_S2/TYPE_S3/TYPE_S4/TYPE_S12）
     */
    public void setChType(int chType) {
        this.chType = chType;  // 保存通道类型到成员变量
    }

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器视图组
     * @param savedInstanceState 保存的状态Bundle
     * @return 填充后的视图对象
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_can, container, false);  // 填充CAN布局文件
    }

    /**
     * 视图创建完成回调，初始化控件和数据
     * @param view 创建的视图对象
     * @param savedInstanceState 保存的状态Bundle
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);  // 获取清空按钮控件
        tvMsgCanTotalTitle = (TextView) view.findViewById(R.id.canTotalTitle);  // 获取总帧数标题控件
        tvMsgCanSpaceTitle = (TextView) view.findViewById(R.id.canSpaceTitle);  // 获取空间帧标题控件
        tvMsgCanErrorTitle = (TextView) view.findViewById(R.id.canErrorTitle);  // 获取错误帧标题控件
        tvMsgCanTotalDetail = (TextView) view.findViewById(R.id.canTotalDetail);  // 获取总帧数详情控件
        tvMsgCanSpaceDetail = (TextView) view.findViewById(R.id.canSpaceDetail);  // 获取空间帧详情控件
        tvMsgCanErrorDetail = (TextView) view.findViewById(R.id.canErrorDetail);  // 获取错误帧详情控件
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);  // 获取滚动条布局容器
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);  // 获取滚动条视图控件
        time = (TextView) view.findViewById(R.id.time);  // 获取时间显示控件
        singleScreenView = (SerialsWordCanSingleScreenTextView) view.findViewById(R.id.singleScreenTextView);  // 获取单屏文本视图
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.recyclerView);  // 获取全屏列表视图

        btnClear.setOnClickListener(onClickListener);  // 为清空按钮设置点击监听器
        scrollBarLayout.setOnTouchListener(onTouchListener);  // 为滚动条布局设置触摸监听器
        time.setOnClickListener(onClickListener);  // 为时间控件设置点击监听器

        singleScreenList = new ArrayList<>();  // 初始化单屏数据列表
        allScreenList = new ArrayList<>();  // 初始化全屏数据列表

        allScreenLayoutManager = new LinearLayoutManager(getContext());  // 创建线性布局管理器
        allScreenLayoutManager.setStackFromEnd(true);  // 设置从底部开始堆叠，新数据从底部添加
        allScreenView.setLayoutManager(allScreenLayoutManager);  // 为RecyclerView设置布局管理器
        allScreenAdapter = new MainAdapterCenterSerialsWordCan(getContext(), allScreenList);  // 创建CAN数据适配器
        allScreenView.setAdapter(allScreenAdapter);  // 为RecyclerView设置适配器

        SerialBusManage.getInstance().AddForceRefresh(this);  // 注册强制刷新监听器到串口总线管理器

        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);  // 订阅缓存加载事件
    }

    //region  接口
    /**
     * 强制刷新UI接口实现，从数据管理器获取最新数据并更新显示
     */
    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()  // 从串口总线管理器获取CAN数据队列
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_CAN, false);  // 获取当前通道的CAN队列，不强制刷新
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);  // 获取串口文本缓冲区对象
        updateLastList(canListTotal, serialTxtBuffer, false);  // 更新数据列表显示，参数false表示非全量更新
    }

    //endregion
    /**
     * 设置缓存配置，初始化统计信息显示和视图可见性
     */
    private void setCache() {
        if (chType != ISerialsWord.TYPE_S12) {  // 如果不是S12组合通道
//            tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsg)
//                    , "0000000000", "0000000000", "0000000000", "0%"));
            tvMsgCanTotalTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgTotalTitle));  // 设置总帧数标题文本
            tvMsgCanSpaceTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgSpaceTitle));  // 设置空间帧标题文本
            tvMsgCanErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgErrorTitle));  // 设置错误帧标题文本
            tvMsgCanTotalDetail.setText("0000000000");  // 初始化总帧数数值为默认值
            tvMsgCanSpaceDetail.setText("0000000000");  // 初始化空间帧数值为默认值
            tvMsgCanErrorDetail.setText("0000000000(0%)");  // 初始化错误帧数值为默认值（含百分比）
        } else {  // 如果是S12组合通道
            tvMsgCanTotalTitle.setText("");  // 清空总帧数标题
            tvMsgCanSpaceTitle.setText("");  // 清空空间帧标题
            tvMsgCanErrorTitle.setText("");  // 清空错误帧标题
            tvMsgCanTotalDetail.setText("");  // 清空总帧数数值
            tvMsgCanSpaceDetail.setText("");  // 清空空间帧数值
            tvMsgCanErrorDetail.setText("");  // 清空错误帧数值
        }

        boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);  // 获取运行停止状态
        allScreenView.setVisibility(!run ? View.VISIBLE : View.GONE);  // 停止模式显示全屏列表，运行模式隐藏
        singleScreenView.setVisibility(run ? View.VISIBLE : View.GONE);  // 运行模式显示单屏视图，停止模式隐藏
    }

    /**
     * 更新数据列表显示
     * @param canListTotal CAN总线数据队列
     * @param serialTxtBuffer 串口文本缓冲区
     * @param isAll 是否为全量更新（true表示加载全部历史数据）
     */
    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (canListTotal == null) return;  // 如果数据队列为空，直接返回
        allScreenList.clear();  // 清空全屏数据列表
        int showLine = 20;  // 设置单屏显示行数常量
        int listSize;  // 定义列表大小变量
        if (chType == ISerialsWord.TYPE_S12) listSize = SerialTxtBuffer.getCanS1S2CurrScreenSize();  // S12组合通道获取组合屏幕大小
        else listSize = serialTxtBuffer.getCanCurrScreenSize();  // 单通道获取当前屏幕大小

        if (!isAll && listSize >= showLine) {  // 非全量更新且数据量超过显示行数
            int i = 0;  // 初始化迭代计数器
            singleScreenList.clear();  // 清空单屏数据列表
            for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) {  // 遍历数据队列迭代器
                if (i > listSize) break;  // 超过列表大小则跳出循环
                if (i >= (listSize - showLine)) {  // 如果索引在显示范围内
                    singleScreenList.add((SerialBusTxtStruct.CanStruct) iter.next());  // 添加数据到单屏列表
                } else {
                    iter.next();  // 跳过不在显示范围内的数据
                }
                i++;  // 增加计数器
            }
            int totalSize = chType == ISerialsWord.TYPE_S12 ? SerialTxtBuffer.getCanS1S2CurrSize() : serialTxtBuffer.getCanCurrSize();  // 获取总数据大小
            int curSize = singleScreenList.size();  // 获取当前显示数据大小
            int totalHeight = singleScreenView.getMeasuredHeight();  // 获取单屏视图总高度
            singleScreenScrollBar.setVisibility(View.VISIBLE);  // 显示滚动条
            singleScreenView.setVisibility(View.VISIBLE);  // 显示单屏视图
            allScreenView.setVisibility(View.GONE);  // 隐藏全屏列表

            if (totalSize == 0) return;  // 如果总数据为0，直接返回
            singleScreenView.setData(singleScreenList, showLine, allScreenAdapter.isShowMs());  // 设置单屏视图数据
            ViewGroup.LayoutParams layoutParams = singleScreenScrollBar.getLayoutParams();  // 获取滚动条布局参数
            layoutParams.height = Math.max(totalHeight * curSize / totalSize, 6);  // 计算滚动条高度，最小为6像素
            singleScreenScrollBar.setLayoutParams(layoutParams);  // 应用滚动条高度参数
            allScreenAdapter.notifyDataSetChanged();  // 通知适配器数据更新
        } else {  // 全量更新或数据量不足
            singleScreenScrollBar.setVisibility(View.GONE);  // 隐藏滚动条
            singleScreenView.setVisibility(View.GONE);  // 隐藏单屏视图
            allScreenView.setVisibility(View.VISIBLE);  // 显示全屏列表

            allScreenList.addAll(canListTotal);  // 将所有数据添加到全屏列表
            allScreenLayoutManager.setStackFromEnd(allScreenList.size() >= showLine);  // 数据超过显示行数时从底部堆叠
            allScreenView.scrollToPosition(allScreenList.size() - 1);  // 滚动到最后一条数据
            allScreenAdapter.notifyDataSetChanged();  // 通知适配器数据更新
        }

        if (chType == ISerialsWord.TYPE_S12) {  // S12组合通道不显示统计信息
            return;  // 直接返回
        }
        long canTotal = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanTotalFrame();  // 获取CAN总帧数
        long canSpace = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanSpaceFrame();  // 获取CAN空间帧数
        long canError = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanErrorFrame();  // 获取CAN错误帧数
        NumberFormat numberFormat = NumberFormat.getPercentInstance();  // 创建百分比格式化器
        numberFormat.setMinimumFractionDigits(1);  // 设置最小小数位数为1位
        if (chType != ISerialsWord.TYPE_S12) {  // 如果不是S12组合通道
            if (isAdded()) {  // 如果Fragment已添加到Activity
//                tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsg)
//                        , String.format("%010d", canTotal)
//                        , String.format("%010d", canSpace)
//                        , String.format("%010d", canError)
//                        , numberFormat.format(canError * 1.0 / canTotal)));
                tvMsgCanTotalTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgTotalTitle));  // 设置总帧数标题
                tvMsgCanSpaceTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgSpaceTitle));  // 设置空间帧标题
                tvMsgCanErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgErrorTitle));  // 设置错误帧标题
                tvMsgCanTotalDetail.setText(String.valueOf(canTotal));  // 显示总帧数数值
                tvMsgCanSpaceDetail.setText(String.valueOf(canSpace));  // 显示空间帧数值
                tvMsgCanErrorDetail.setText(canError + "(" + numberFormat.format(canError * 1.0 / canTotal) + ")");  // 显示错误帧数值和错误率百分比
            }
        }
    }

    /**
     * 设置滚动移动，响应外部按键或触摸事件
     * @param moveCount 移动数据条数（负数向上滚动，正数向下滚动）
     */
    public void setScrollMove(int moveCount) {
        if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP)) {  // 仅在停止模式下响应滚动
            if (moveCount < 0) {  // 向上滚动
                int position = allScreenLayoutManager.findFirstCompletelyVisibleItemPosition();  // 获取第一个完全可见项位置
                allScreenLayoutManager.scrollToPosition(position + moveCount);  // 滚动到计算位置
            } else {  // 向下滚动
                int position = allScreenLayoutManager.findLastCompletelyVisibleItemPosition();  // 获取最后一个完全可见项位置
                allScreenLayoutManager.scrollToPosition(position + moveCount);  // 滚动到计算位置
            }
            allScreenAdapter.notifyDataSetChanged();  // 通知适配器数据更新
        }
    }

    /**
     * 设置运行停止状态，控制数据刷新和显示模式
     * @param run true为运行模式，false为停止模式
     */
    public void setRunStop(boolean run) {
        if (!run) {  // 停止模式
            SerialBusTxtStructParse.getInstance().InterruptedParse(chType);  // 中断解析线程
            while (SerialBusTxtStructParse.getInstance().getParsing(chType)) {  // 等待解析完成
                Tools.sleep(10);  // 线程休眠10毫秒
            }
            Tools.sleep(100);  // 线程休眠100毫秒，确保数据稳定
        }

        LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()  // 从串口总线管理器获取CAN数据队列
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_CAN, !run);  // 获取队列，停止模式获取全部历史数据
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);  // 获取串口文本缓冲区
        updateLastList(canListTotal, serialTxtBuffer, !run);  // 更新数据列表，停止模式全量更新
    }

    /**
     * 加载缓存消息消费者，响应缓存加载事件
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        /**
         * 接收缓存加载消息
         * @param loadCache 缓存加载消息对象
         * @throws Exception RxJava异常
         */
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();  // 调用缓存设置方法
        }
    };

    /**
     * 视图点击监听器，处理按钮点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        /**
         * 处理点击事件
         * @param v 被点击的视图
         */
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();  // 播放按钮点击音效
            switch (v.getId()) {  // 根据视图ID判断点击源
                case R.id.clear:  // 清空按钮点击
                    SerialBusManage.getInstance().clearSerialBusTxtBuffer();  // 清空串口总线文本缓冲区
                    break;  // 跳出switch
                case R.id.time:  // 时间显示控件点击
                    allScreenAdapter.setShowMs(!allScreenAdapter.isShowMs());  // 切换毫秒显示开关
                    allScreenAdapter.notifyDataSetChanged();  // 通知适配器数据更新
                    break;  // 跳出switch
            }
        }
    };

    private float yDown, yMove;  // 触摸事件Y坐标变量

    /**
     * 视图触摸监听器，处理滚动条拖拽事件
     */
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        /**
         * 处理触摸事件
         * @param v 被触摸的视图
         * @param event 触摸事件对象
         * @return 是否消费事件
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {  // 根据动作类型处理
                case MotionEvent.ACTION_DOWN:  // 手指按下
                    yDown = event.getY();  // 记录按下时Y坐标
                    break;  // 跳出switch
                case MotionEvent.ACTION_MOVE:  // 手指移动
                    yMove = event.getY() - yDown;  // 计算移动距离
                    setScrollMove((int) (allScreenAdapter.getItemCount() * yMove / allScreenView.getMeasuredHeight()));  // 根据移动距离计算滚动位置
                    yDown = event.getY();  // 更新按下Y坐标为当前坐标
                    break;  // 跳出switch
                case MotionEvent.ACTION_UP:  // 手指抬起
                    break;  // 跳出switch
            }
            return false;  // 返回false表示不消费事件
        }
    };
}