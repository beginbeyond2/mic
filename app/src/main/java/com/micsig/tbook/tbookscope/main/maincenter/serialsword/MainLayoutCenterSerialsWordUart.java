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
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialTxtBuffer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.rxjava3.functions.Consumer;


public class MainLayoutCenterSerialsWordUart extends Fragment implements SerialBusManage.IForceRefreshUI {
    private static final String TAG = "MainLayoutCenterSerialsWordUart";
    private Button btnClear;
    private TextView tipLayout;
    private RelativeLayout dataLayout;
    private View dividerX;
    private TextView title1, title2, title3;
    private TextView tvMsgUartFrameTitle, tvMsgUartErrorTitle;
    private TextView tvMsgUartFrameDetail, tvMsgUartErrorDetail;
    private UnFlingRecyclerView allScreenView;
    private LinearLayoutManager allScreenLayoutManager;
    private MainAdapterCenterSerialsWordUart allScreenAdapter;
    private ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>> allScreenList;
    private View singleScreenScrollBar;
    private SerialsWordUartSingleScreenTextView singleScreenView;
    private ArrayList<SerialBusTxtStruct.UartStruct> singleScreenList;

    private int chType = ISerialsWord.TYPE_S1;
    private String title = "";
    private HashMap<String, Integer> checkMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> bitsMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> displayMap = new HashMap<String, Integer>();


    public void setChType(int chType) {
        this.chType = chType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_uart, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btnClear = (Button) view.findViewById(R.id.clear);
        tipLayout = (TextView) view.findViewById(R.id.tipLayout);
        dataLayout = (RelativeLayout) view.findViewById(R.id.dataLayout);
        dividerX = view.findViewById(R.id.dividerX);
        title1 = (TextView) view.findViewById(R.id.title1);
        title2 = (TextView) view.findViewById(R.id.title2);
        title3 = (TextView) view.findViewById(R.id.title3);
        tvMsgUartFrameTitle = (TextView) view.findViewById(R.id.uartFrameTitle);
        tvMsgUartErrorTitle = (TextView) view.findViewById(R.id.uartErrorTitle);
        tvMsgUartFrameDetail = (TextView) view.findViewById(R.id.uartFrameDetail);
        tvMsgUartErrorDetail = (TextView) view.findViewById(R.id.uartErrorDetail);
        singleScreenView = (SerialsWordUartSingleScreenTextView) view.findViewById(R.id.singleScreenTextView);
        RelativeLayout scrollBarLayout = (RelativeLayout) view.findViewById(R.id.scrollBarLayout);
        singleScreenScrollBar = view.findViewById(R.id.scrollBarView);
        allScreenView = (UnFlingRecyclerView) view.findViewById(R.id.leftRecyclerView);

        btnClear.setOnClickListener(onClickListener);
        scrollBarLayout.setOnTouchListener(onTouchListener);

        singleScreenList = new ArrayList<SerialBusTxtStruct.UartStruct>();
        allScreenList = new ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>>();

        allScreenAdapter = new MainAdapterCenterSerialsWordUart(getContext(), allScreenList);
        allScreenLayoutManager = new LinearLayoutManager(getContext());
        allScreenLayoutManager.setStackFromEnd(true);
        allScreenView.setLayoutManager(allScreenLayoutManager);
        allScreenView.setAdapter(allScreenAdapter);

        SerialBusManage.getInstance().AddForceRefresh(this);

        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);
    }

    //region 接口

    @Override
    public void onForceRefreshUI() {
        LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = SerialBusManage.getInstance()
                .getSerialTxtBuffer(chType).getUartQueueTotal();
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(uartListTotal, serialTxtBuffer, false);
    }

    //endregion

    private void setCache() {
        int typeIndex = CacheUtil.S1;
        if (this.chType == ISerialsWord.TYPE_S4) {
            typeIndex = CacheUtil.S4;
        } else if (this.chType == ISerialsWord.TYPE_S3) {
            typeIndex = CacheUtil.S3;
        } else if (this.chType == ISerialsWord.TYPE_S2) {
            typeIndex = CacheUtil.S2;
        }
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + typeIndex);
        int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + typeIndex);
        int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + typeIndex);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dividerX.getLayoutParams();
        if (display == 0 && bits == 4 && check == 0) {//hex,9bit,none校验
            title1.setVisibility(View.VISIBLE);
            title2.setVisibility(View.VISIBLE);
            title3.setVisibility(View.GONE);
            title1.setText(R.string.serialsWordUartTitleHex);
            dividerX.setVisibility(View.VISIBLE);
            int marginStart = (int) getResources().getDimension(R.dimen.uart_hex_bit9_width);
            layoutParams.setMarginStart(marginStart);
        } else if (display == 0) {//hex,其他bit
            title1.setVisibility(View.VISIBLE);
            title2.setVisibility(View.VISIBLE);
            title3.setVisibility(View.GONE);
            title1.setText(R.string.serialsWordUartTitleHex);
            dividerX.setVisibility(View.VISIBLE);
            int marginStart = (int) getResources().getDimension(R.dimen.uart_hex_bitOther_width);
            layoutParams.setMarginStart(marginStart);
        } else if (display == 1) {//bin
            title1.setVisibility(View.VISIBLE);
            title2.setVisibility(View.VISIBLE);
            title3.setVisibility(View.GONE);
            title1.setText(R.string.serialsWordUartTitleBin);
            dividerX.setVisibility(View.VISIBLE);
            int marginStart = (int) getResources().getDimension(R.dimen.uart_bin_width);
            layoutParams.setMarginStart(marginStart);
        } else {//ascii
            title1.setVisibility(View.GONE);
            title2.setVisibility(View.GONE);
            title3.setVisibility(View.VISIBLE);
            dividerX.setVisibility(View.GONE);
//            int marginStart= (int) getResources().getDimension(R.dimen.uart_ascii_width);
//            layoutParams.setMarginStart(marginStart);
        }
        dividerX.setLayoutParams(layoutParams);
        if (chType != ISerialsWord.TYPE_S12) {
//            tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsg)
//                    , "0000000000", "0000000000", "0%"));
            tvMsgUartFrameTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsgFrameTitle));
            tvMsgUartErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsgErrorTitle));
            tvMsgUartFrameDetail.setText("0000000000");
            tvMsgUartErrorDetail.setText("0000000000(0%)");
        } else {
            tvMsgUartFrameTitle.setText("");
            tvMsgUartErrorTitle.setText("");
            tvMsgUartFrameDetail.setText("");
            tvMsgUartErrorDetail.setText("");
        }

        boolean run = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP);
        allScreenView.setVisibility(!run ? View.VISIBLE : View.GONE);
        singleScreenView.setVisibility(run ? View.VISIBLE : View.GONE);


    }

    private void setTipLayoutVisible() {
        if (chType == ISerialsWord.TYPE_S12) {
            int s1Check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + CacheUtil.S1);
            int s1Bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + CacheUtil.S1);
            int s1Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + CacheUtil.S1);

            int s2Check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + CacheUtil.S2);
            int s2Bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + CacheUtil.S2);
            int s2Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + CacheUtil.S2);

            int s3Check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + CacheUtil.S3);
            int s3Bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + CacheUtil.S3);
            int s3Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + CacheUtil.S3);

            int s4Check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + CacheUtil.S4);
            int s4Bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + CacheUtil.S4);
            int s4Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + CacheUtil.S4);
            checkMap.put("S1", s1Check);
            checkMap.put("S2", s2Check);
            checkMap.put("S3", s3Check);
            checkMap.put("S4", s4Check);
            bitsMap.put("S1", s1Bits);
            bitsMap.put("S2", s2Bits);
            bitsMap.put("S3", s3Bits);
            bitsMap.put("S4", s4Bits);
            displayMap.put("S1", s1Display);
            displayMap.put("S2", s2Display);
            displayMap.put("S3", s3Display);
            displayMap.put("S4", s4Display);

//            if (s1Check != s2Check || s1Bits != s2Bits || s1Display != s2Display) {
//                tipLayout.setVisibility(View.VISIBLE);
//                dataLayout.setVisibility(View.GONE);
//            } else {
//                tipLayout.setVisibility(View.GONE);
//                dataLayout.setVisibility(View.VISIBLE);
//                setCache();
//            }
            if (getS12Check() && getS12Bits() && getS12Display()) {
                tipLayout.setVisibility(View.GONE);
                dataLayout.setVisibility(View.VISIBLE);
                setCache();
            } else {
                tipLayout.setVisibility(View.VISIBLE);
                dataLayout.setVisibility(View.GONE);
            }
        } else {
            tipLayout.setVisibility(View.GONE);
            dataLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean getS12Check() {
        String[] titles = title.split("&");
        boolean finalCheck = true;
        String preKey = "";
        String nowKey = "";
        for (String s : titles) {
            if (checkMap.containsKey(s)) {
                preKey = nowKey;
                nowKey = s;
                if(preKey.isEmpty()) continue;
                finalCheck = finalCheck && (Objects.equals(checkMap.get(preKey), checkMap.get(nowKey)));
            }
        }
        return finalCheck;
    }

    private boolean getS12Bits() {
        String[] titles = title.split("&");
        boolean finalBits = true;
        String preKey = "";
        String nowKey = "";
        for (String s : titles) {
            if (bitsMap.containsKey(s)) {
                preKey = nowKey;
                nowKey = s;
                if(preKey.isEmpty()) continue;
                finalBits = finalBits && (Objects.equals(bitsMap.get(preKey), bitsMap.get(nowKey)));
            }
        }
        return finalBits;
    }

    private boolean getS12Display() {
        String[] titles = title.split("&");
        boolean finalDisplay = true;
        String preKey = "";
        String nowKey = "";
        for (String s : titles) {
            if (displayMap.containsKey(s)) {
                preKey = nowKey;
                nowKey = s;
                if(preKey.isEmpty()) continue;
                finalDisplay = finalDisplay && (Objects.equals(displayMap.get(preKey), displayMap.get(nowKey)));
            }
        }
        return finalDisplay;
    }


    private void updateLastList(LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal, SerialTxtBuffer serialTxtBuffer, boolean isAll) {
        if (uartListTotal == null) return;
        allScreenList.clear();
        int showLine = 20;
        int typeIndex = CacheUtil.S1;
        if (this.chType == ISerialsWord.TYPE_S4) {
            typeIndex = CacheUtil.S4;
        } else if (this.chType == ISerialsWord.TYPE_S3) {
            typeIndex = CacheUtil.S3;
        } else if (this.chType == ISerialsWord.TYPE_S2) {
            typeIndex = CacheUtil.S2;
        }
        int display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + typeIndex);
        int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + typeIndex);
        int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + typeIndex);
        //二进制每行7组数据，ASCII码每行67组数据，十六进制9字节每行12组数据，十六进制其他字节每行16组数据
        allScreenAdapter.setBits(bits);
        allScreenAdapter.setCheck(check);
        int countOfLine;
        if (display == 0 && bits == 4 && check == 0) { //hex_bit9
            countOfLine = 19 + 10;
            allScreenAdapter.setListType(SerialsWordUartSingleRowTextView.TYPE_HEX_NINE);
        } else if (display == 0) { //hex_bit_other
            countOfLine = 23 + 12;
            allScreenAdapter.setListType(SerialsWordUartSingleRowTextView.TYPE_HEX_OTHER);
        } else if (display == 1) { //bin
            countOfLine = 11 + 2;
            allScreenAdapter.setListType(SerialsWordUartSingleRowTextView.TYPE_BIN);
        } else {    //ascii
            countOfLine = 89;
            allScreenAdapter.setListType(SerialsWordUartSingleRowTextView.TYPE_AXCII);
        }
        int count = countOfLine * showLine;
        int listSize;
        if (chType == ISerialsWord.TYPE_S12) listSize = SerialTxtBuffer.getUartS1S2CurrScreenSize();
        else {
            listSize = serialTxtBuffer.getUartCurrScreenSize();
        }
        if (!isAll && listSize >= count) {
            singleScreenList.clear();
            Iterator iter = uartListTotal.iterator();
            int remainder = listSize % countOfLine == 0 ? 0 : (countOfLine - listSize % countOfLine);
            for (int i = listSize - count + remainder, j = 0; /*i < listSize &&*/ iter.hasNext(); j++) {
                if (j != i) {
                    iter.next();
                    continue;
                }
                singleScreenList.add((SerialBusTxtStruct.UartStruct) iter.next());
                i++;
            }

            singleScreenScrollBar.setVisibility(View.VISIBLE);
            singleScreenView.setVisibility(View.VISIBLE);
            allScreenView.setVisibility(View.GONE);
            int totalSize = chType == ISerialsWord.TYPE_S12 ? SerialTxtBuffer.getUartS1S2CurrSize() : serialTxtBuffer.getUartCurrSize();
            int curSize = singleScreenList.size();
            int totalHeight = singleScreenView.getMeasuredHeight();

            if (totalSize == 0) return;

            singleScreenView.setList(bits, check, allScreenAdapter.getShowType(), chType, countOfLine, showLine, singleScreenList);
            ViewGroup.LayoutParams layoutParams = singleScreenScrollBar.getLayoutParams();
            layoutParams.height = Math.max(totalHeight * curSize / totalSize, 8);
            singleScreenScrollBar.setLayoutParams(layoutParams);
            allScreenAdapter.notifyDataSetChanged();
        } else {
            int i = 0;
            for (Iterator iter = uartListTotal.iterator(); iter.hasNext(); i++) {
                if (i % countOfLine == 0) {
                    allScreenList.add(new ArrayList<SerialBusTxtStruct.UartStruct>());
                }
                allScreenList.get(allScreenList.size() - 1).add((SerialBusTxtStruct.UartStruct) iter.next());
            }

            singleScreenScrollBar.setVisibility(View.GONE);
            singleScreenView.setVisibility(View.GONE);
            allScreenView.setVisibility(View.VISIBLE);

            allScreenLayoutManager.setStackFromEnd(allScreenList.size() >= showLine);
            allScreenAdapter.setChType(chType);
            allScreenView.scrollToPosition(allScreenList.size() - 1);
            allScreenAdapter.notifyDataSetChanged();
        }

        if (chType == ISerialsWord.TYPE_S12) {
            return;
        }
        long uartTotalData = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getUartTotalData();
        long uartErrorData = SerialBusManage.getInstance().getSerialTxtBuffer(chType).getUartErrorData();
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        if (chType != ISerialsWord.TYPE_S12) {
            if (isAdded()) {
//                tvMsg.setText(String.format(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsg)
//                        , String.format("%010d", uartTotalData)
//                        , String.format("%010d", uartErrorData)
//                        , numberFormat.format(uartErrorData * 1.0 / uartTotalData)));
                tvMsgUartFrameTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsgFrameTitle));
                tvMsgUartErrorTitle.setText(getString(com.micsig.tbook.ui.R.string.serialsWordUartMsgErrorTitle));
                tvMsgUartFrameDetail.setText(String.valueOf(uartTotalData));
                tvMsgUartErrorDetail.setText(uartErrorData + "(" + uartErrorData * 1.0 / uartTotalData + ")");
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
        LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = SerialBusManage.getInstance()
                .getSerialTxtBufferQueue(chType, RightLayoutSerials.SERIALS_UART, !run);
        SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
        updateLastList(uartListTotal, serialTxtBuffer, !run);

    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
            setTipLayoutVisible();
        }
    };

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            if (rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) {
                if ((rightMsgSerials.isSerials1() && chType == ISerialsWord.TYPE_S1)
                        || (rightMsgSerials.isSerials2() && chType == ISerialsWord.TYPE_S2)
                        || (rightMsgSerials.isSerials3() && chType == ISerialsWord.TYPE_S3)
                        || (rightMsgSerials.isSerials4() && chType == ISerialsWord.TYPE_S4)
                ) {
                    setCache();
                    LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = SerialBusManage.getInstance()
                            .getSerialTxtBuffer(chType).getUartQueueTotal();
                    SerialTxtBuffer serialTxtBuffer = SerialBusManage.getInstance().getSerialTxtBuffer(chType);
                    updateLastList(uartListTotal, serialTxtBuffer, false);
                }

                setTipLayoutVisible();
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            SerialBusManage.getInstance().clearSerialBusTxtBuffer();
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

    private Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) {
                setTipLayoutVisible();//增加或者减少组合的时候需要重新判断设置一致性
            }
        }
    };

}