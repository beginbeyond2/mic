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


public class MainLayoutCenterSerialsWordCan extends Fragment implements SerialBusManage.IForceRefreshUI {
    private Button btnClear;
    private TextView time;
    private TextView tvMsgCanTotalTitle, tvMsgCanSpaceTitle, tvMsgCanErrorTitle;
    private TextView tvMsgCanTotalDetail, tvMsgCanSpaceDetail, tvMsgCanErrorDetail;
    private UnFlingRecyclerView allScreenView;
    private LinearLayoutManager allScreenLayoutManager;
    private MainAdapterCenterSerialsWordCan allScreenAdapter;
    private ArrayList<SerialBusTxtStruct.CanStruct> allScreenList;
    private View singleScreenScrollBar;
    private SerialsWordCanSingleScreenTextView singleScreenView;
    private ArrayList<SerialBusTxtStruct.CanStruct> singleScreenList;

    private int chType = ISerialsWord.TYPE_S1;

    public void setChType(int chType) {
        this.chType = chType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_can, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);
        tvMsgCanTotalTitle = (TextView) view.findViewById(R.id.canTotalTitle);
        tvMsgCanSpaceTitle = (TextView) view.findViewById(R.id.canSpaceTitle);
        tvMsgCanErrorTitle = (TextView) view.findViewById(R.id.canErrorTitle);
        tvMsgCanTotalDetail = (TextView) view.findViewById(R.id.canTotalDetail);
        tvMsgCanSpaceDetail = (TextView) view.findViewById(R.id.canSpaceDetail);
        tvMsgCanErrorDetail = (TextView) view.findViewById(R.id.canErrorDetail);
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);
        time = (TextView) view.findViewById(R.id.time);
        singleScreenView = (SerialsWordCanSingleScreenTextView) view.findViewById(R.id.singleScreenTextView);
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.recyclerView);

        btnClear.setOnClickListener(onClickListener);
        scrollBarLayout.setOnTouchListener(onTouchListener);
        time.setOnClickListener(onClickListener);

        singleScreenList = new ArrayList<>();
        allScreenList = new ArrayList<>();

        allScreenLayoutManager = new LinearLayoutManager(getContext());
        allScreenLayoutManager.setStackFromEnd(true);
        allScreenView.setLayoutManager(allScreenLayoutManager);
        allScreenAdapter = new MainAdapterCenterSerialsWordCan(getContext(), allScreenList);
        allScreenView.setAdapter(allScreenAdapter);

        SerialBusManage.getInstance().AddForceRefresh(this);

        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    //region  接口
    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_CAN, false);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(canListTotal, serialTxtBuffer, false);
    }

    //endregion
    private void setCache() {
        if (chType != ISerialsWord.TYPE_S12) {
//            tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsg)
//                    , "0000000000", "0000000000", "0000000000", "0%"));
            tvMsgCanTotalTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgTotalTitle));
            tvMsgCanSpaceTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgSpaceTitle));
            tvMsgCanErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgErrorTitle));
            tvMsgCanTotalDetail.setText("0000000000");
            tvMsgCanSpaceDetail.setText("0000000000");
            tvMsgCanErrorDetail.setText("0000000000(0%)");
        } else {
            tvMsgCanTotalTitle.setText("");
            tvMsgCanSpaceTitle.setText("");
            tvMsgCanErrorTitle.setText("");
            tvMsgCanTotalDetail.setText("");
            tvMsgCanSpaceDetail.setText("");
            tvMsgCanErrorDetail.setText("");
        }

        boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        allScreenView.setVisibility(!run ? View.VISIBLE : View.GONE);
        singleScreenView.setVisibility(run ? View.VISIBLE : View.GONE);
    }

    /**
     * @param isAll 本次更新的是否是全部列表
     */
    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (canListTotal == null) return;
        allScreenList.clear();
        int showLine = 20;
        int listSize;
        if (chType == ISerialsWord.TYPE_S12) listSize = SerialTxtBuffer.getCanS1S2CurrScreenSize();
        else listSize = serialTxtBuffer.getCanCurrScreenSize();

        if (!isAll && listSize >= showLine) {
            int i = 0;
            singleScreenList.clear();
            for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) {
                if (i > listSize) break;
                if (i >= (listSize - showLine)) {
                    singleScreenList.add((SerialBusTxtStruct.CanStruct) iter.next());
                } else {
                    iter.next();
                }
                i++;
            }
            int totalSize = chType == ISerialsWord.TYPE_S12 ? SerialTxtBuffer.getCanS1S2CurrSize() : serialTxtBuffer.getCanCurrSize();
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

            allScreenList.addAll(canListTotal);
            allScreenLayoutManager.setStackFromEnd(allScreenList.size() >= showLine);
            allScreenView.scrollToPosition(allScreenList.size() - 1);
            allScreenAdapter.notifyDataSetChanged();
        }

        if (chType == ISerialsWord.TYPE_S12) {
            return;
        }
        long canTotal = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanTotalFrame();
        long canSpace = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanSpaceFrame();
        long canError = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getCanErrorFrame();
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        if (chType != ISerialsWord.TYPE_S12) {
            if (isAdded()) {
//                tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsg)
//                        , String.format("%010d", canTotal)
//                        , String.format("%010d", canSpace)
//                        , String.format("%010d", canError)
//                        , numberFormat.format(canError * 1.0 / canTotal)));
                tvMsgCanTotalTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgTotalTitle));
                tvMsgCanSpaceTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgSpaceTitle));
                tvMsgCanErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordCanMsgErrorTitle));
                tvMsgCanTotalDetail.setText(String.valueOf(canTotal));
                tvMsgCanSpaceDetail.setText(String.valueOf(canSpace));
                tvMsgCanErrorDetail.setText(canError + "(" + numberFormat.format(canError * 1.0 / canTotal) + ")");
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

        LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_CAN, !run);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(canListTotal, serialTxtBuffer, !run);
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