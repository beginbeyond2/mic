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


public class MainLayoutCenterSerialsWordLin extends Fragment implements SerialBusManage.IForceRefreshUI {
    private Button btnClear;
    private TextView tvMsg;
    private TextView time;
    private UnFlingRecyclerView allScreenView;
    private LinearLayoutManager allScreenLayoutManager;
    private MainAdapterCenterSerialsWordLin allScreenAdapter;
    private ArrayList<SerialBusTxtStruct.LinStruct> allScreenList;
    private View singleScreenScrollBar;
    private SerialsWordLinSingleScreenTextView singleScreenView;
    private ArrayList<SerialBusTxtStruct.LinStruct> singleScreenList;

    private int chType = ISerialsWord.TYPE_S1;

    public void setChType(int chType) {
        this.chType = chType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_lin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);
        tvMsg = (TextView) view.findViewById(R.id.msg);
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);
        time = (TextView) view.findViewById(R.id.time);
        singleScreenView = (SerialsWordLinSingleScreenTextView) view.findViewById(R.id.singleScreenTextView);
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.recyclerView);

        btnClear.setOnClickListener(onClickListener);
        scrollBarLayout.setOnTouchListener(onTouchListener);
        time.setOnClickListener(onClickListener);

        singleScreenList = new ArrayList<>();
        allScreenList = new ArrayList<>();

        allScreenLayoutManager = new LinearLayoutManager(getContext());
        allScreenLayoutManager.setStackFromEnd(true);
        allScreenView.setLayoutManager(allScreenLayoutManager);
        allScreenAdapter = new MainAdapterCenterSerialsWordLin(getContext(), allScreenList);
        allScreenView.setAdapter(allScreenAdapter);

        SerialBusManage.getInstance().AddForceRefresh(this);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    //region inferface

    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_LIN, false);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(linListTotal, serialTxtBuffer, false);
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
    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (linListTotal == null) return;
        allScreenList.clear();
        int showLine = 20;
        int listSize;
        if (chType == ISerialsWord.TYPE_S12) listSize = SerialTxtBuffer.getLinS1S2CurrScreenSize();
        else listSize = serialTxtBuffer.getLinCurrScreenSize();
        if (!isAll && listSize >= showLine) {
            int i = 0;
            singleScreenList.clear();
            for (Iterator iter = linListTotal.iterator(); iter.hasNext(); ) {
                if (i > listSize) break;
                if (i >= (listSize - showLine)) {
                    singleScreenList.add((SerialBusTxtStruct.LinStruct) iter.next());
                } else {
                    iter.next();
                }
                i++;
            }

            int totalSize = chType == ISerialsWord.TYPE_S12 ? SerialTxtBuffer.getLinS1S2CurrSize() : serialTxtBuffer.getLinCurrSize();
            int curSize = singleScreenList.size();
            int totalHeight = singleScreenView.getMeasuredHeight();
//            Logger.e("Lin:updateLastList:listSize:" + serialTxtBuffer.getLinCurrScreenSize()
//                    + ",curSize:" + curSize + ",linListTotal:" + linListTotal);
//            Logger.d("Lin:updateLastList:totalSize:" + totalSize + ",curSize:" + curSize + ",list:" + singleScreenList);
            singleScreenScrollBar.setVisibility(View.VISIBLE);
            singleScreenView.setVisibility(View.VISIBLE);
            allScreenView.setVisibility(View.GONE);

            if (totalSize == 0) return;

            singleScreenView.setData(singleScreenList, showLine, allScreenAdapter.isShowMs());
            ViewGroup.LayoutParams layoutParams = singleScreenScrollBar.getLayoutParams();
            layoutParams.height = Math.max(totalHeight * curSize / totalSize, 6);
            singleScreenScrollBar.setLayoutParams(layoutParams);
            allScreenAdapter.notifyDataSetChanged();
        } else {
            singleScreenScrollBar.setVisibility(View.GONE);
            singleScreenView.setVisibility(View.GONE);
            allScreenView.setVisibility(View.VISIBLE);

            allScreenList.addAll(linListTotal);
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
        if (!run) {
            SerialBusTxtStructParse.getInstance().InterruptedParse(chType);
            while (SerialBusTxtStructParse.getInstance().getParsing(chType)) {
                Tools.sleep(10);
            }
            Tools.sleep(100);
        }

        LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_LIN, !run);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(linListTotal, serialTxtBuffer, !run);
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