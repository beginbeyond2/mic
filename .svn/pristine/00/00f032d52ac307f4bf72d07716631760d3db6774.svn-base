package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.util.ViewUtils;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;

public class DialogChannelLabel extends RelativeLayout {
    public static final int INDEX_USERDEFINE = 1;
    public static final int FROM_CHANNEL = 1;
    public static final int FROM_MATHREF = 2;

    private Context context;
    private String cacheKey;
    private OnDismissListener onDismissListener;
    private DialogChannelLabelAdapter adapter;
    private ArrayList<RightBeanSelect> list = new ArrayList<>();
    private RadioButton tvUserDefine;
    private TopDialogTextKeyBoard dialogTextKeyBoard;
    private RelativeLayout rlContentView;
    private View maskView;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public DialogChannelLabel(Context context) {
        this(context, null);
    }

    public DialogChannelLabel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogChannelLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_channellabel, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });
        initView(rootViewGroup);
        hide();
    }


    private void initView(View view) {
        tvUserDefine = view.findViewById(R.id.userDefine);
        tvUserDefine.setText("(" + getResources().getString(R.string.serialsUserDefine) + ")");
//        tvUserDefine.setText("");

        rlContentView = findViewById(R.id.rl_contentView);
        maskView = findViewById(R.id.maskView);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        initList();
        adapter = new DialogChannelLabelAdapter(getContext(), list, onItemClickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position);
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initList() {
        list.clear();
        String[] stringArray = App.get().getResources().getStringArray(R.array.channelLabel);
        for (int i = 0; i < stringArray.length; i++) {
            list.add(new RightBeanSelect(i, stringArray[i], i == 0));
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_CHANNELLABEL);
        Tools.PrintControlsLocation("DialogChannelLabel",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_CHANNELLABEL);
    }

    @SuppressLint("ResourceType")
    private void setControlColorByChIdx(int chIdx){

//        switch (chIdx){
//            case 0: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch1;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch1);
//                break;
//            case 1: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch2;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch2);
//                break;
//            case 2: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch3;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch3);
//                break;
//            case 3: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ch4;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch4);
//                break;
//            case 4: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_math;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_math);
//                break;
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//                itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_ref;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ref);
//                break;
//            case 9: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_s1;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s1);
//                break;
//            case 10: itemBgViewResId= com.micsig.tbook.ui.R.drawable.selector_rightslip_button_s2;
//                itemTextColorResId=context.getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s2);
//                break;
//        }

        StateListDrawable d= BitmapUtil.genSelectorDrawable(context,chIdx);
        ColorStateList itemTextColorResId =BitmapUtil.genSelectorColor(context,chIdx);
        tvUserDefine.setBackground(d);
        tvUserDefine.setTextColor(itemTextColorResId);
    }

    public void setViewPosition(int showType) {
        int contentX, contentY, maskWidth, maskHeight, maskX, maskY;
        switch (showType) {
            case FROM_MATHREF:
                contentX = 1167;
                contentY = 280;
                maskWidth = 819;//RightLayoutRef宽
                maskHeight = 907;//RightLayoutRef高
                maskX = 980;//RightLayoutRef layoutX
                maskY = 210;//RightLayoutRef layoutY
                break;
            default://channel 默认
                contentX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);
//                contentY = (int) context.getResources().getDimension(R.dimen.newRightDialogY);
                contentY = (int) context.getResources().getDimension(R.dimen.rightDialogY);
                maskWidth = (int) context.getResources().getDimension(R.dimen.rightSlipChannelWidth);
                maskHeight = (int) context.getResources().getDimension(R.dimen.rightDialogHeight);
                maskX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);
//                maskY = (int) context.getResources().getDimension(R.dimen.newRightDialogY);
                maskY = (int) context.getResources().getDimension(R.dimen.rightDialogY);
                break;
        }

        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) rlContentView.getLayoutParams();
        layoutParams.x = contentX;
        layoutParams.y = contentY;
        rlContentView.setLayoutParams(layoutParams);

        AbsoluteLayout.LayoutParams layoutParams1 = (AbsoluteLayout.LayoutParams) maskView.getLayoutParams();
        layoutParams1.width = maskWidth;
        layoutParams1.height = maskHeight;
        layoutParams1.x = maskX;
        layoutParams1.y = maskY;
        maskView.setLayoutParams(layoutParams1);
    }

    public void setData(int chanNum, String preString, String cacheKey,
                        int showType, OnDismissListener onDismissListener) {
        setViewPosition(showType);
        setData(chanNum, preString, cacheKey, onDismissListener);
    }

    private void setData(int chanNum, String preString, String cacheKey, OnDismissListener onDismissListener) {
        initList();
        setControlColorByChIdx(chanNum);
        adapter.setControlColorByChIdx(chanNum);
        String string = CacheUtil.get().getString(cacheKey);
        if (!StrUtil.isEmpty(string)) {
            list.get(INDEX_USERDEFINE).setText(string);
//            tvUserDefine.setVisibility(GONE);
            tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            list.get(INDEX_USERDEFINE).setText("");
//            tvUserDefine.setVisibility(VISIBLE);
            tvUserDefine.setGravity(Gravity.CENTER);
        }
        this.cacheKey = cacheKey;
        int index=0;
        if (StrUtil.isEmpty(preString)) {
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {//选中none
                    list.get(i).setCheck(true);
                    index=i;
                    list.get(INDEX_USERDEFINE).setText("");
                    tvUserDefine.setGravity(Gravity.CENTER);
                } else {
                    list.get(i).setCheck(false);
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getText().equalsIgnoreCase(preString)) {
                    list.get(i).setCheck(true);
                    index = i;
                    if (index == 0) break;
                } else {
                    list.get(i).setCheck(false);
                }
            }

            if (index == 0) {
//                onTextKeyBoardDismissListener.onDismiss(preString);
                index = 1;
                list.get(0).setCheck(false);
            }
            if (index != 1) {
                list.get(INDEX_USERDEFINE).setText("");
                tvUserDefine.setGravity(Gravity.CENTER);
            } else {
                tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }

        if (index==1){
            tvUserDefine.setChecked(true);
        }else {
            tvUserDefine.setChecked(false);
        }

        boolean checkNumber = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isCheck()) {
                checkNumber = true;
            }
        }

        if (!checkNumber) {
            if (StrUtil.isEmpty(preString)) {
                list.get(0).setCheck(true);
            } else {
                list.get(INDEX_USERDEFINE).setCheck(true);
                list.get(INDEX_USERDEFINE).setText(preString);
                CacheUtil.get().putMap(cacheKey, preString);
            }
        }
        adapter.notifyDataSetChanged();
        this.onDismissListener = onDismissListener;
        show();
    }

    private DialogChannelLabelAdapter.OnItemClickListener onItemClickListener = new DialogChannelLabelAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, RightBeanSelect item) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            Logger.d("DialogChannelLabel");
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                Screen.getViewLocation(recyclerView.getChildAt(i));
            }
            Logger.d("DialogChannelLabel:"+item.getIndex());
            boolean b=false;
            for (int i = 0; i < list.size(); i++) {
                b=i==item.getIndex();
                list.get(i).setCheck(i == item.getIndex());
            }
            if (item.getIndex()==1){
                tvUserDefine.setChecked(true);
            }else {
                tvUserDefine.setChecked(false);
            }
            adapter.notifyDataSetChanged();
            if (onDismissListener != null) {
                if (item.getIndex() == 0) {
                    onDismissListener.onDismiss("");
                    hide();
                } else if (item.getIndex() == 1) {
                    if (dialogTextKeyBoard == null) {
                        dialogTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
                    }
                    dialogTextKeyBoard.setData(item.getText(), TopDialogTextKeyBoard.HANDLE_TYPE_CHANNEL_LABEL, TopDialogTextKeyBoard.INPUT_TYPE_ALL, 30, onTextKeyBoardDismissListener);
                } else {
                    onDismissListener.onDismiss(item.getText());
                    hide();
                }
            }
        }
    };

    private TopDialogTextKeyBoard.OnDialogDismissListener onTextKeyBoardDismissListener = new TopDialogTextKeyBoard.OnDialogDismissListener() {
        @Override
        public void onDismiss(String result) {
            if (onDismissListener != null) {
                if (StrUtil.isEmpty(result)) {
//                    tvUserDefine.setVisibility(VISIBLE);
                    tvUserDefine.setGravity(Gravity.CENTER);
                } else {
//                    tvUserDefine.setVisibility(GONE);
                    tvUserDefine.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                list.get(INDEX_USERDEFINE).setText(result);
                CacheUtil.get().putMap(cacheKey, result);
                adapter.notifyDataSetChanged();
                onDismissListener.onDismiss(result);
                hide();
            }
        }
    };
}
