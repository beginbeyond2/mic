package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/4/13.
 */

public class TopViewTitleWithScroll extends RelativeLayout {
    public static final int WIDTH_SHOW = TopViewTitle.WIDTH_SHOW;

    public static final int FIRST_TITLE = 1;
    public static final int TWO_TITLE = 2;
    private Context context;
    private View view;
    private String[] array;
    private boolean[] arrayVisible;
    private TextView selectTitleLeft, selectTitleRight;
    private TopViewHorScroll scrollView;
    private TopViewTitle topViewTitle;
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener;
    private IOnScrollViewSliderListener onScrollViewSliderListener;
    private View.OnClickListener onItemClickListener;
    private int flag = TWO_TITLE;
    private View vFlag;

    public void setOnScrollViewSliderListener(IOnScrollViewSliderListener onScrollViewSliderListener) {
        this.onScrollViewSliderListener = onScrollViewSliderListener;
    }

    private int itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine;
    private int layoutResId;
    private int itemTextSize;

    public interface IOnScrollViewSliderListener {
        void onSliderStop();
    }


    public TopViewTitleWithScroll(Context context) {
        this(context, null);
    }

    public TopViewTitleWithScroll(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewTitleWithScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    public void setData(int arrayResId, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = context.getResources().getStringArray(arrayResId);
        arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = true;
        }
        setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener);
    }

    public void setData(String[] array, boolean[] arrayVisible, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = array;
        this.arrayVisible = arrayVisible;
        this.onItemClickListener = onItemClickListener;
        updateView();
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
    }

    public void setData(int arrayResId, int arraysVisibleResId, TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.array = context.getResources().getStringArray(arrayResId);;
        String[] arraysVisible = context.getResources().getStringArray(arraysVisibleResId);
        arrayVisible = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayVisible[i] = Boolean.parseBoolean(arraysVisible[i]);
        }
        this.onItemClickListener = onItemClickListener;
        updateView();
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
    }

    public void updateItemText(int index, String s) {
        if (selectTitleLeft.getVisibility() == VISIBLE && getItem(index).getText().equals(selectTitleLeft.getText().toString())) {
            selectTitleLeft.setText(s);
        }
        if (selectTitleRight.getVisibility() == VISIBLE && getItem(index).getText().equals(selectTitleRight.getText().toString())) {
            selectTitleRight.setText(s);
        }
        topViewTitle.updateItemText(index, s);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        view = View.inflate(context, R.layout.view_topviewtitlewithhead, this);
        View topViewTitleDivider = view.findViewById(R.id.topViewTitleDivider);
//        setPadding(0, 5, 0, 0);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewTitleWithScroll);
        flag = ta.getInt(R.styleable.TopViewTitleWithScroll_itemBgViewFlag, TWO_TITLE);
        if (flag == FIRST_TITLE) {
            itemBgResIdCheck = R.drawable.bg_topviewtitle_item_select;
            itemBgResIdUnCheck = R.drawable.bg_topviewtitle_item_unselect;
            itemBgResIdUnCheckUnLine = R.drawable.bg_topviewtitle_item_unselect;

            layoutResId = android.R.color.black;
            topViewTitleDivider.setVisibility(GONE);
        } else {
            itemBgResIdCheck = R.drawable.bg_topviewtitle_item_select;
            itemBgResIdUnCheck = R.drawable.bg_topviewtitle_item_unselect;
            itemBgResIdUnCheckUnLine = R.drawable.bg_topviewtitle_item_unselect;

            layoutResId = android.R.color.black;
            topViewTitleDivider.setVisibility(GONE);
        }
        itemTextSize = ta.getDimensionPixelSize(R.styleable.TopViewTitleWithScroll_itemTextSize, 24);
        ta.recycle();
    }

    private void updateView() {
        selectTitleLeft = (TextView) findViewById(R.id.selectTitleLeft);
        selectTitleRight = (TextView) findViewById(R.id.selectTitleRight);
        RelativeLayout leftLayout = (RelativeLayout) findViewById(R.id.leftLayout);
        RelativeLayout rightLayout = (RelativeLayout) findViewById(R.id.rightLayout);
        selectTitleLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
        selectTitleRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
        selectTitleLeft.setBackgroundResource(itemBgResIdCheck);
        selectTitleRight.setBackgroundResource(itemBgResIdCheck);
        leftLayout.setBackgroundResource(layoutResId);
        rightLayout.setBackgroundResource(layoutResId);
        selectTitleLeft.setOnClickListener(onClickListener);
        selectTitleRight.setOnClickListener(onClickListener);
        selectTitleLeft.setVisibility(GONE);
        selectTitleRight.setVisibility(GONE);
        scrollView = (TopViewHorScroll) findViewById(R.id.scrollView);
        topViewTitle = new TopViewTitle(context);
        topViewTitle.setBg(itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine);
        topViewTitle.setFlag(flag);
        topViewTitle.setItemTextSize(itemTextSize);
        topViewTitle.setData(array, arrayVisible, onCheckChangedListener, onItemClickListener);
        scrollView.removeAllViews();
        scrollView.addView(topViewTitle.getInflate());
        scrollView.setOnScrollListener(onScrollChangedListener);

        leftLayout.setVisibility(GONE);
        rightLayout.setVisibility(GONE);
        vFlag = findViewById(R.id.firstTitleFlag);
        if (flag == FIRST_TITLE) {
            vFlag.setVisibility(VISIBLE);
        } else {
            vFlag.setVisibility(GONE);
        }
    }

    public TopAllBeanTitle getItem(int index) {
        return topViewTitle.getItem(index);
    }

    public TopAllBeanTitle getSelected() {
        return topViewTitle.getSelected();
    }

    public ArrayList<TopAllBeanTitle> getAllList() {
        return topViewTitle.getAllList();
    }

    public ArrayList<TopShowBeanTitle> getShowList() {
        return topViewTitle.getShowList();
    }

    public void setSelected(int index) {
        topViewTitle.setListener(null, null);
        topViewTitle.setSelected(index);
        setRightViewVisible(index);
        setListHead();
        topViewTitle.setListener(onCheckChangedListener, onItemClickListener);
    }

    public void setEnable(int index, boolean enable) {
        topViewTitle.setEnable(index, enable);
    }

    public void moveOnlyScroll(int index) {
        int[] initXPosition = getInitXPosition();
        View view = topViewTitle.getRadioGroup().getChildAt(index);
        int[] childLoc = new int[2];
        view.getLocationOnScreen(childLoc);
        if ((selectTitleLeft.getVisibility() != VISIBLE || index == 0) && childLoc[0] < 2) {
            scrollView.scrollTo(initXPosition[index], 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        } else if (selectTitleLeft.getVisibility() == VISIBLE && childLoc[0] < 2 + view.getWidth() && index != 0) {
            scrollView.scrollTo(initXPosition[index - 1], 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        } else if ((selectTitleRight.getVisibility() != VISIBLE || index == topViewTitle.getRadioGroup().getChildCount() - 1)
                && childLoc[0] > ((WIDTH_SHOW + 2) - view.getWidth())) {
            scrollView.scrollTo((initXPosition[index] + view.getWidth() - (WIDTH_SHOW + 2)), 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        } else if (selectTitleRight.getVisibility() == VISIBLE && childLoc[0] > ((WIDTH_SHOW + 2) - view.getWidth() * 2)
                && index != topViewTitle.getRadioGroup().getChildCount() - 1) {
            scrollView.scrollTo((initXPosition[index] + view.getWidth() * 2 - (WIDTH_SHOW + 2)), 0);
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
            setListHead();
        }
    }

    private int[] getInitXPosition() {
//        int[] initXPosition = new int[]{2, 92, 182, 272, 362, 452, 542, 632, 722, 812, 902};
        int count = topViewTitle.getRadioGroup().getChildCount();
        int width = topViewTitle.getRadioGroup().getChildAt(0).getWidth();
        int[] initXPosition = new int[count];
        for (int i = 0; i < count; i++) {
            initXPosition[i] = width * i + 2;
        }
        return initXPosition;
    }

//    private boolean first = true;

    public void setCacheSelect(final int cacheSelect) {
//        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (first) {
//                    first = false;
        setSelected(cacheSelect);
        setRightViewVisible(cacheSelect);
//                }
//            }
//        });
    }

    private void setRightViewVisible(int select) {
        if (select >= 7) {
            selectTitleRight.setVisibility(VISIBLE);
            selectTitleRight.setText(topViewTitle.getSelected().getText());
            selectTitleLeft.setVisibility(GONE);
        } else {
            selectTitleRight.setVisibility(GONE);
            selectTitleLeft.setVisibility(GONE);
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int width = topViewTitle.getCheckedRadioButton().getWidth();
            if (v.getId() == R.id.selectTitleLeft) {
                if (selectTitleLeft.getVisibility() == VISIBLE) {
                    scrollView.smoothScrollTo(topViewTitle.getSelected().getIndex() * width, 0);
                }
            } else if (v.getId() == R.id.selectTitleRight) {
                if (selectTitleRight.getVisibility() == VISIBLE) {
                    scrollView.smoothScrollTo(topViewTitle.getSelected().getIndex() * width + width - WIDTH_SHOW, 0);
                }
            }
        }
    };

    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedListener = new TopViewTitle.OnCheckChangedTitleListener() {
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) {
            setListHead();
            if (onCheckChangedTitleListener != null) {
                onCheckChangedTitleListener.checkChanged(TopViewTitleWithScroll.this, item);
            }
        }
    };

    private TopViewHorScroll.OnScrollListener onScrollChangedListener = new TopViewHorScroll.OnScrollListener() {
        @Override
        public void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy) {
            TopViewTitleWithScroll.this.onScrollChanged(scrollView, x, y, oldx, oldy);
            setListHead();
        }

        @Override
        public void onStop() {
            if (onScrollViewSliderListener != null) {
                onScrollViewSliderListener.onSliderStop();
            }
        }
    };

    public void onScrollChanged(TopViewHorScroll scrollView, int x, int y, int oldx, int oldy) {
        int parentWidth = ((View) scrollView.getParent()).getWidth();
        int Width = scrollView.getChildAt(0).getWidth();
        int currX = (int) scrollView.getChildAt(0).getX() - (oldx - x);
        if (currX >= (parentWidth - Width) || currX <= 0) {
            // scrollView.getChildAt(0).setX(currX );  这个用&& 的
            scrollView.scrollBy(oldx - x, 0);
        }

    }

    private void setListHead() {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        RadioButton child = topViewTitle.getCheckedRadioButton();
        int[] childLoc = new int[2];
        int[] scrollLoc = new int[2];
        child.getLocationOnScreen(childLoc);//相对于屏幕左上角的位置
        scrollView.getLocationOnScreen(scrollLoc);
        int childLocX = childLoc[0];
        int scrollLocX = scrollLoc[0];
        if (childLocX - scrollLocX < 0) {
            selectTitleLeft.setVisibility(VISIBLE);
            selectTitleLeft.setText(child.getText());
            selectTitleRight.setVisibility(GONE);
        } else if (childLocX + child.getWidth() - scrollLocX - scrollView.getWidth() > 0) {
            selectTitleRight.setVisibility(VISIBLE);
            selectTitleRight.setText(child.getText());
            selectTitleLeft.setVisibility(GONE);
        } else {
            selectTitleLeft.setVisibility(GONE);
            selectTitleRight.setVisibility(GONE);
        }

        if (flag == FIRST_TITLE) {
            int select = 0;
            for (int i = 0; i < topViewTitle.getShowList().size(); i++) {
                if (topViewTitle.getShowList().get(i).getIndexAll() == topViewTitle.getSelected().getIndex()) {
                    select = topViewTitle.getShowList().get(i).getIndexShow();
                }
            }
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) vFlag.getLayoutParams();
            layoutParams.width=TopViewTitle.ItemWidth;
            layoutParams.setMargins(select * TopViewTitle.ItemWidth, 0, 0, 0);
            vFlag.setLayoutParams(layoutParams);
        }
    }
}
