package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialTxtBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * ***********************************************************************************
 * * I2C总线文本显示Fragment类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - I2C总线协议解码文本数据的显示模块
 * *
 * * 【核心职责】
 * *   1. 显示I2C总线解码后的文本数据列表（地址、数据、读写标志、时间戳等）
 * *   2. 支持运行模式实时刷新和停止模式历史查看
 * *   3. 实现触摸滚动和外部按键滚动功能
 * *   4. 提供时间显示格式切换（毫秒显示开关）
 * *   5. 支持清空缓存数据功能
 * *
 * * 【架构设计】
 * *   继承Fragment，实现IForceRefreshUI强制刷新接口：
 * *   - 使用RecyclerView显示全屏数据列表（停止模式）
 * *   - 使用自定义SingleScreenTextView显示单屏数据（运行模式）
 * *   - 通过LinkedBlockingQueue获取线程安全的I2C数据队列
 * *   - 利用RxBus订阅缓存加载事件
 * *   - 采用自适应滚动条显示数据位置
 * *
 * * 【数据流向】
 * *   输入：
 * *     → SerialBusManage获取I2C数据队列和缓冲区
 * *     → CacheUtil读取运行状态和通道类型
 * *     → RxBus接收LoadCache缓存加载事件
 * *     → 用户触摸事件触发滚动
 * *   输出：
 * *     → RecyclerView/SingleScreenTextView显示I2C文本列表
 * *     → SerialBusManage清空缓冲区
 * *
 * * 【依赖关系】
 * *   上层依赖：MainLayoutCenterSerialsWordDetail父容器Fragment
 * *   下层依赖：MainAdapterCenterSerialsWordI2c适配器、SerialBusManage数据管理
 * *   平级依赖：CacheUtil、RxBus、PlaySound、Tools等工具类
 * *
 * * 【使用场景】
 * *   当用户配置通道为I2C协议时，显示I2C总线解码的文本数据
 * *   在Run模式实时刷新显示最新数据，在Stop模式支持滚动查看历史数据
 * ***********************************************************************************
 */
public class MainLayoutCenterSerialsWordI2c extends Fragment implements SerialBusManage.IForceRefreshUI {
    private Button btnClear;  // 清空按钮，用于清除所有I2C数据缓存
    private TextView tvMsg;  // 消息显示控件（已弃用）
    private TextView time;  // 时间显示控件，点击切换毫秒显示开关
    private UnFlingRecyclerView allScreenView;  // 全屏数据列表视图，用于Stop模式显示所有历史数据
    private LinearLayoutManager allScreenLayoutManager;  // 线性布局管理器，管理RecyclerView布局
    private MainAdapterCenterSerialsWordI2c allScreenAdapter;  // I2C数据适配器，绑定数据到视图
    private ArrayList<SerialBusTxtStruct.I2cStruct> allScreenList;  // 全屏数据列表，存储所有I2C帧数据
    private View singleScreenScrollBar;  // 单屏模式滚动条视图，显示数据位置指示
    private SerialsWordI2cSingleScreenTextView singleScreenView;  // 单屏数据视图，用于Run模式高效显示
    private ArrayList<SerialBusTxtStruct.I2cStruct> singleScreenList;  // 单屏数据列表，存储当前显示的I2C帧数据

    private int chType = ISerialsWord.TYPE_S1;  // 通道类型，默认为S1通道

    /**
     * 设置通道类型
     * @param chType 通道类型常量（TYPE_S1/TYPE_S2/TYPE_S3/TYPE_S4/TYPE_S12）
     */
    public void setChType(int chType) {
        this.chType = chType;  // 保存通道类型到成员变量
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_i2c, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);
        tvMsg = (TextView) view.findViewById(R.id.msg);
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);
        time = (TextView) view.findViewById(R.id.time);
        singleScreenView = (SerialsWordI2cSingleScreenTextView) view.findViewById(R.id.singleScreenTextView);
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.recyclerView);

        btnClear.setOnClickListener(onClickListener);
        scrollBarLayout.setOnTouchListener(onTouchListener);
        time.setOnClickListener(onClickListener);

        singleScreenList = new ArrayList<>();
        allScreenList = new ArrayList<>();

        allScreenLayoutManager = new LinearLayoutManager(getContext());
        allScreenLayoutManager.setStackFromEnd(true);
        allScreenView.setLayoutManager(allScreenLayoutManager);
        allScreenAdapter = new MainAdapterCenterSerialsWordI2c(getContext(), allScreenList);
        allScreenView.setAdapter(allScreenAdapter);

        SerialBusManage.getInstance().AddForceRefresh(this);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    //region  interface

    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_I2C,false);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(i2cListTotal, serialTxtBuffer, false);
    }

    //endregion
    private void setCache() {
        boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        allScreenView.setVisibility(!run ? View.VISIBLE : View.GONE);
        singleScreenView.setVisibility(run ? View.VISIBLE : View.GONE);

    }

    /**
     * @param isAll 本次更新的是否是全部列表
     */
    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (i2cListTotal == null) return;
        allScreenList.clear();
        int showLine = 20;
        int listSize ;
        if (chType==ISerialsWord.TYPE_S12) listSize=SerialTxtBuffer.getI2cS1S2CurrScreenSize();
        else listSize= serialTxtBuffer.getI2cCurrScreenSize();
        if (!isAll && listSize >= showLine) {
            int i = 0;
            singleScreenList.clear();
            for (Iterator iter = i2cListTotal.iterator(); iter.hasNext(); ) {
                if (i > listSize) break;
                if (i >= (listSize - showLine)) {
                    singleScreenList.add((SerialBusTxtStruct.I2cStruct) iter.next());
                } else {
                    iter.next();
                }
                i++;
            }
//            int totalSize = listSize;
            int totalSize=chType==ISerialsWord.TYPE_S12?SerialTxtBuffer.getI2cS1S2CurrSize():serialTxtBuffer.getI2cCurrSize();
            int curSize = singleScreenList.size();
            int totalHeight = singleScreenView.getMeasuredHeight();
            singleScreenScrollBar.setVisibility(View.VISIBLE);
            singleScreenView.setVisibility(View.VISIBLE);
            allScreenView.setVisibility(View.GONE);

            if (totalSize==0) return;

            singleScreenView.setData(singleScreenList, showLine, allScreenAdapter.isShowMs());
            ViewGroup.LayoutParams layoutParams = singleScreenScrollBar.getLayoutParams();
            layoutParams.height = Math.max(totalHeight * curSize / totalSize, 6);
            singleScreenScrollBar.setLayoutParams(layoutParams);
            allScreenAdapter.notifyDataSetChanged();
        } else {
            singleScreenScrollBar.setVisibility(View.GONE);
            singleScreenView.setVisibility(View.GONE);
            allScreenView.setVisibility(View.VISIBLE);

            allScreenList.addAll(i2cListTotal);
            allScreenLayoutManager.setStackFromEnd(allScreenList.size() >= showLine);
            allScreenView.scrollToPosition(allScreenList.size() - 1);
            allScreenAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollMove(int moveCount) {
        if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP)) {
            if (moveCount < 0) {
                int position = allScreenLayoutManager.findFirstCompletelyVisibleItemPosition();
                allScreenLayoutManager.scrollToPosition(position + moveCount);
            } else {
                int position = allScreenLayoutManager.findLastCompletelyVisibleItemPosition();
                allScreenLayoutManager.scrollToPosition(position + moveCount);
            }
            allScreenAdapter.notifyDataSetChanged();
        }
    }

    public void setRunStop(boolean run) {
        if (!run){
            SerialBusTxtStructParse.getInstance().InterruptedParse(chType);
            while (SerialBusTxtStructParse.getInstance().getParsing(chType)){
                Tools.sleep(10);
            }
            Tools.sleep(100);
        }

        LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_I2C,!run);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(i2cListTotal, serialTxtBuffer, !run);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            switch (v.getId()) {
                case R.id.clear:
                    SerialBusManage.getInstance().clearSerialBusTxtBuffer();
                    break;
                case R.id.time:
                    allScreenAdapter.setShowMs(!allScreenAdapter.isShowMs());
                    allScreenAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private float yDown, yMove;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yMove = event.getY() - yDown;
                    setScrollMove((int) (allScreenAdapter.getItemCount() * yMove / allScreenView.getMeasuredHeight()));
                    yDown = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    };
}