package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

/**
 * Created by yangj on 2017/5/9.
 */

public class DialogProbeMultiple extends AbsoluteLayout {
    private Context context;
    private OnDismissListener onDismissListener;
    private String cacheKey;
    private RightViewSelect multiple;
    private RightViewUserDefineEdit userDefineMultiple;
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    private String[] list;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public DialogProbeMultiple(Context context) {
        this(context, null);
    }

    public DialogProbeMultiple(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogProbeMultiple(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[440, 37]	260	358
    private void init() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_probemultiple, this);

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
        multiple = (RightViewSelect) view.findViewById(R.id.multiple);
        userDefineMultiple = view.findViewById(R.id.userDefineMultiple);
        multiple.setOnItemClickListener(onItemClickListener);
        userDefineMultiple.setOnEditClickListener(onEditClickListener);
        list = context.getResources().getStringArray(R.array.channelProbeTypeMultiple);
    }

    public boolean isExistProbeRate(String probeRate) {
        for (String aList : list) {
            if (probeRate.equals(aList)) {
                return true;
            }
        }
        return false;
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_PROBEMULTIPLE);
        Tools.PrintControlsLocation("DialogProbeMultiple",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_PROBEMULTIPLE);
    }


    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    public void setData(int channelNum,String preString, String cacheKey, OnDismissListener onDismissListener) {
//        Log.d(Tag.Debug, String.format("DialogProbeMultiple.setData: %d",channelNum ));
        multiple.setControlColorByChIdx(channelNum);
        userDefineMultiple.setControlColorByChIdx(channelNum);


        this.cacheKey = cacheKey;
        String userDefine = CacheUtil.get().getString(cacheKey);
        if (StrUtil.isEmpty(userDefine)) {
            userDefineMultiple.setText("");
            for (String item : list) {
                if (item.equals(preString)) {
                    multiple.setPreString(preString);
                }
            }
        } else {
            userDefineMultiple.setText(userDefine);
            multiple.setSelectIndex(-1);
        }
        this.onDismissListener = onDismissListener;
        show();
    }

    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            if (!StrUtil.isEmpty(cacheKey)) {
                CacheUtil.get().putMap(cacheKey, "");
            }
            hide();
            if (onDismissListener != null) {
                onDismissListener.onDismiss(item.getText());
            }
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            if (dialogFloatKeyBoard == null) {
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
                dialogFloatKeyBoard.bringToFront();
            }
            dialogFloatKeyBoard.setFloatmkData(text.replace("X", ""), view, new TopDialogFloatKeyBoard.OnDismissListener() {
                @Override
                public void onDismiss(View fromView, String show) {
                    if (StrUtil.isEmpty(show) || "0".equals(show)) {
                        return;
                    }
                    double d = TBookUtil.getDoubleFromM(show);
                    if (d < 0.001) {
                        show = "1m";
                    } else if (d > 1000 * 1000) {
                        show = "999k";
                    } else {
                        String unit = "";
                        if (show.contains("m")) {
                            unit = "m";
                            show = show.replace("m", "");
                        } else if (show.contains("k")) {
                            unit = "k";
                            show = show.replace("k", "");
                        }
                        if (show.length() >= 3) {
                            String substring = show.substring(0, 3);
                            if (substring.contains(".") && show.length() >= 4) {
                                substring = show.substring(0, 4);
                            }
                            show = substring;
                        }
                        show = show + unit;
                    }
                    userDefineMultiple.setText(show + "X");
                    if (!StrUtil.isEmpty(cacheKey)) {
                        CacheUtil.get().putMap(cacheKey, userDefineMultiple.getText());
                    }
                    hide();
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss(userDefineMultiple.getText());
                    }
                }
            });
        }
    };
}
