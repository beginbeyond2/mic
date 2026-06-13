package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * @auother Liwb
 * @description:
 * @data:2023-9-1 10:25
 */
public class DialogMathFFTPersist extends AbsoluteLayout {
    private Context context;
    private ViewGroup rootViewGroup;
    private DialogProbeMultiple.OnDismissListener onDismissListener;
    private String cacheKey;
    private RightViewSelect multiple;
    private String[] list;


    public DialogMathFFTPersist(Context context) {
        this(context,null);
    }

    public DialogMathFFTPersist(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DialogMathFFTPersist(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public DialogMathFFTPersist(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
           this.context=context;
           init();
    }
    private void init() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_math_fft_persist, this);
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
        multiple.setOnItemClickListener(onItemClickListener);
        list = context.getResources().getStringArray(R.array.mathFftPersistValue);
    }

    public void show() {
        Tools.PrintControlsLocation("persistence",rootViewGroup);
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MATH_FFT_PERSIST);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MATH_FFT_PERSIST);
    }
    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    private RightViewSelect.OnItemClickListener onItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            if (!StrUtil.isEmpty(cacheKey)) {
                CacheUtil.get().putMap(cacheKey, item.getText());
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

    public void setData(int chIdx,String preString, String cacheKey, DialogProbeMultiple.OnDismissListener onDismissListener) {
        multiple.setControlColorByChIdx(chIdx);
        this.cacheKey = cacheKey;
            for (String item : list) {
                if (item.equals(preString)) {
                    multiple.setPreString(preString);
                }
            }
        this.onDismissListener = onDismissListener;
        show();
    }

}
