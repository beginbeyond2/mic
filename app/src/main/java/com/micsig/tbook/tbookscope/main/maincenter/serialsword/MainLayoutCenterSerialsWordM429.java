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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.rxjava3.functions.Consumer;


public class MainLayoutCenterSerialsWordM429 extends Fragment implements SerialBusManage.IForceRefreshUI {
    private Button btnClear;
    private TextView tvMsgM429WordTitle, tvMsgM429ErrorTitle;
    private TextView tvMsgM429WordDetail, tvMsgM429ErrorDetail;
    private TextView time;
    private UnFlingRecyclerView allScreenView;
    private LinearLayoutManager allScreenLayoutManager;
    private MainAdapterCenterSerialsWordM429 allScreenAdapter;
    private ArrayList<SerialBusTxtStruct.Arinc429Struct> allScreenList;
    private View singleScreenScrollBar;
    private SerialsWordM429SingleScreenTextView singleScreenView;
    private ArrayList<SerialBusTxtStruct.Arinc429Struct> singleScreenList;

    private int chType = ISerialsWord.TYPE_S1;

    public void setChType(int chType) {
        this.chType = chType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_m429, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);
        tvMsgM429WordTitle = (TextView) view.findViewById(R.id.m429WordTitle);
        tvMsgM429ErrorTitle = (TextView) view.findViewById(R.id.m429ErrorTitle);
        tvMsgM429WordDetail = (TextView) view.findViewById(R.id.m429WordDetail);
        tvMsgM429ErrorDetail = (TextView) view.findViewById(R.id.m429ErrorDetail);
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);
        time = (TextView) view.findViewById(R.id.time);
        singleScreenView = (SerialsWordM429SingleScreenTextView) view.findViewById(R.id.singleScreenTextView);
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.recyclerView);

        btnClear.setOnClickListener(onClickListener);
        scrollBarLayout.setOnTouchListener(onTouchListener);
        time.setOnClickListener(onClickListener);

        singleScreenList = new ArrayList<>();
        allScreenList = new ArrayList<>();

        allScreenLayoutManager = new LinearLayoutManager(getContext());
        allScreenLayoutManager.setStackFromEnd(true);
        allScreenView.setLayoutManager(allScreenLayoutManager);
        allScreenAdapter = new MainAdapterCenterSerialsWordM429(getContext(), allScreenList);
        allScreenView.setAdapter(allScreenAdapter);

        SerialBusManage.getInstance().AddForceRefresh(this);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    //region interface

    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> m429ListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_M429, false);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(m429ListTotal, serialTxtBuffer, false);
    }

    //endregion

    private void setCache() {
        if (chType != ISerialsWord.TYPE_S12) {
//            tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordM429Msg)
//                    , "0000000000", "0000000000", "0%"));
            tvMsgM429WordTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordM429MsgFrameTitle));
            tvMsgM429ErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordM429MsgErrorTitle));
            tvMsgM429WordDetail.setText("0000000000");
            tvMsgM429ErrorDetail.setText("0000000000(0%)");
        } else {
            tvMsgM429WordTitle.setText("");
            tvMsgM429ErrorTitle.setText("");
            tvMsgM429WordDetail.setText("");
            tvMsgM429ErrorDetail.setText("");
        }

        boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        allScreenView.setVisibility(!run ? View.VISIBLE : View.GONE);
        singleScreenView.setVisibility(run ? View.VISIBLE : View.GONE);
    }

    /**
     * @param isAll 本次更新的是否是全部列表
     */
    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> m429ListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (m429ListTotal == null) return;
        allScreenList.clear();
        int showLine = 20;
        int listSize;
        if (chType == ISerialsWord.TYPE_S12)
            listSize = SerialTxtBuffer.getArinc429S1S2CurrScreenSize();
        else listSize = serialTxtBuffer.getArinc429CurrScreenSize();
        if (!isAll && listSize >= showLine) {
            int i = 0;
            singleScreenList.clear();
            for (Iterator iter = m429ListTotal.iterator(); iter.hasNext(); ) {
                if (i > listSize) break;
                if (i >= (listSize - showLine)) {
                    singleScreenList.add((SerialBusTxtStruct.Arinc429Struct) iter.next());
                } else {
                    iter.next();
                }
                i++;
            }

//            int totalSize = listSize;
            int totalSize = chType == ISerialsWord.TYPE_S12 ? SerialTxtBuffer.getArinc429S1S2CurrSize() : serialTxtBuffer.getArinc429CurrSize();
            int curSize = singleScreenList.size();
            int totalHeight = singleScreenView.getMeasuredHeight();
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

            allScreenList.addAll(m429ListTotal);
            allScreenLayoutManager.setStackFromEnd(allScreenList.size() >= showLine);
            allScreenView.scrollToPosition(allScreenList.size() - 1);
            allScreenAdapter.notifyDataSetChanged();
        }

        if (chType == ISerialsWord.TYPE_S12) {
            return;
        }
        long m429Total = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getArinc429TotalFrame();
        long m429Error = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getArinc429ErrorFrame();
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        if (chType != ISerialsWord.TYPE_S12) {
            if (isAdded()) {
//                tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordM429Msg)
//                        , String.format("%010d", m429Total)
//                        , String.format("%010d", m429Error)
//                        , numberFormat.format(m429Error * 1.0 / m429Total)));
                tvMsgM429WordTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordM429MsgFrameTitle));
                tvMsgM429ErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordM429MsgErrorTitle));
                tvMsgM429WordDetail.setText(String.valueOf(m429Total));
                tvMsgM429ErrorDetail.setText(m429Error + "(" + numberFormat.format(m429Error * 1.0 / m429Total) + ")");
            }
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

        LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> m429ListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_M429, !run);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(m429ListTotal, serialTxtBuffer, !run);
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