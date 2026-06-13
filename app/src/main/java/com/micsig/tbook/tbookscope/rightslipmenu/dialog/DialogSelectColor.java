package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DialogSelectColor extends ConstraintLayout {

    private static final String TAG = "DialogSelectColor";
    public static final int FROM_CHANNEL = 1;
    public static final int FROM_MATHREF = 2;
    private final Context context;
    private ConstraintLayout rootViewGroup;
    private RecyclerView recyclerView;
    private SelectColorAdapter colorAdapter;
    private OnDismissListener onDismissListener;
    private int chIndex;
    private View maskView;
    private RelativeLayout rlContentView;

    public DialogSelectColor(Context context) {
        this(context, null);
    }

    public DialogSelectColor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogSelectColor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public interface OnDismissListener {
        void onDismiss(int chIndex, String colorStr);
    }

    private void initView() {
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_select_color, this);
        View outView = rootViewGroup.findViewById(R.id.outView);
        outView.setOnClickListener(onClickListener);
        recyclerView = rootViewGroup.findViewById(R.id.recyclerView);
        colorAdapter = new SelectColorAdapter(context, createColors(), itemClickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setAdapter(colorAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        rlContentView = findViewById(R.id.rl_contentView);
        maskView = findViewById(R.id.maskView);
    }

    private ArrayList<String> createColors() {
        ArrayList<String> colors = new ArrayList<>();
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math1));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math2));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math3));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math4));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math5));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math6));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math7));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.Math8));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R1));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R2));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R3));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R4));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R5));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R6));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R7));
        colors.add(SvgNodeInfo.getDefaultColor(TChan.R8));
        return colors;
    }

    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.outView:
                hide();
                break;
        }
    };

    public void setData(int showType, int chIndex, DialogSelectColor.OnDismissListener onDismissListener) {
        this.chIndex = chIndex;
        this.onDismissListener = onDismissListener;
        setViewPosition(showType);
        show();
    }

    public void setViewPosition(int showType) {
        int contentX, contentY, maskWidth, maskHeight, maskX, maskY;
        switch (showType) {
            case FROM_MATHREF://在Math/Ref菜单中显示
                contentX = 1167;
                contentY = 280;
                maskWidth = 819;//RightLayoutRef宽
                maskHeight = 907;//RightLayoutRef高
                maskX = 980;//RightLayoutRef layoutX
                maskY = 210;//RightLayoutRef layoutY
                break;
            default://在模拟通道菜单中显示
                contentX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);;
                contentY = (int) context.getResources().getDimension(R.dimen.rightDialogY);
                maskWidth = (int) context.getResources().getDimension(R.dimen.rightSlipChannelWidth);
                maskHeight = (int) context.getResources().getDimension(R.dimen.rightDialogHeight);
                maskX = (int) context.getResources().getDimension(R.dimen.rightChannelDialogX);
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


    private void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_SELECT_COLOR);
//        Tools.PrintControlsLocation(TAG, rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_SELECT_COLOR);
    }

    private SelectColorAdapter.OnItemClickListener itemClickListener = new SelectColorAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, String colorStr) {
            colorStr = SvgNodeInfo.getGenColor(chIndex,colorStr);
            onDismissListener.onDismiss(chIndex, colorStr);
            handleSelectColor(chIndex, colorStr);
            hide();
        }
    };

    //颜色值改变的起始
    private void handleSelectColor(int chIndex, String colorStr) {
        Logger.i(TAG, "channel= " + chIndex + " ,selectColor= " + colorStr);
        SvgNodeInfo.setChannelColor(chIndex, colorStr);//改变颜色值
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorStr);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorStr);//发消息通知颜色值改变了
    }

}
