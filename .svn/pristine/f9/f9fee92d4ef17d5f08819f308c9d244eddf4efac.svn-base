package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/5/3.
 */

public class RightViewSelect extends RelativeLayout {
    private static final String TAG = "RightViewSelect";

    public static final int ARRAYORIENTATION_VERTICAL = 1;
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW2 = 2;
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW3 = 3;
    public static final int ARRAYORIENTATION_HORIZONTALEACHROW4 = 4;

    public static final int EACH_74_39 = 1;
    public static final int EACH_74_39_2 = 2;
    public static final int EACH_74_34 = 3;
    public static final int EACH_120_60 = 4;
    public static final int EACH_108_54 = 5;

    private Context context;
    private int chIdx=-1;
    private CharSequence[] array;
    private int listPadLeft;
    private int textLines = 1;
    private int itemWidth;
    private int itemHeight;
    private int itemMargin;
    private int arrayOrientation;
//    private int itemBgViewResId;
    private boolean itemStartAndEndBgHalfCorner;
    private int itemHalfCornerEach;
    private int firstShowCount;
    private int firstNoneCount;
    private RecyclerView listView;
    private RightAdapterSelect adapter;
    private String preString;
    private ArrayList<RightBeanSelect> list;
    private OnItemClickListener onItemClickListener;
    private OnItemClick2Listener onItemClick2Listener;
    /**
     * 前后两次设置是否可以相同，默认不可以相同
     */
    private boolean doubleCheckEqual = false;

    /**
     * 除了形参类型，并没有什么区别
     */
    public interface OnItemClickListener {
        void onItemClick(int viewId, RightBeanSelect item);

        /**
         * 播放声音
         *
         * @param isCheckedSuccess 是否选择成功：当前如果已经选择了，再次选择，则为失败（false）
         */
        void onClickSound(boolean isCheckedSuccess);

        /**
         * 点击之后（点击指：onitemClick），进行刷新
         *
         * @param viewId
         * @param isCurClickForce 此次点击是否生效，即是否执行了onItemClick();
         */
        void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce);

        /**
         * 点击之前（点击指：onitemClick），进行刷新
         *
         * @param viewId
         */
        void onItemClickBeforRefreshUI(int viewId);
    }

    /**
     * 除了形参类型，并没有什么区别
     */
    public interface OnItemClick2Listener {
        void onItemClick2(int parentsViewId, View itemView, RightBeanSelect item);
    }

    /**
     * 除了形参类型，并没有什么区别
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 除了形参类型，并没有什么区别
     */
    public void setOnItemClick2Listener(OnItemClick2Listener onItemClick2Listener) {
        this.onItemClick2Listener = onItemClick2Listener;
    }

    public void setPreString(String preString) {
        this.preString = preString;
        boolean flag = false;
        for (RightBeanSelect item : list) {
            if (item.getText().equals(preString)) {
                flag = true;
                break;
            }
            if (item.isUserDefine(context)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            boolean isCheck = false;
            for (RightBeanSelect item : list) {
                if (item.getText().equals(preString)) {
                    item.setCheck(true);
                    isCheck = true;
                } else {
                    item.setCheck(false);
                }
            }
            if (!isCheck) {
                for (RightBeanSelect item : list) {
                    if (item.isUserDefine(context)) {
                        item.setCheck(true);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    public void setEnabledBg(int itemBgViewResId) {
//        adapter.setItemBgViewResId(itemBgViewResId);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (RightBeanSelect item : list) {
            item.setEnable(enabled);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * @return 本次设置是否有数据变化
     */
    public boolean setEnabled(int index, boolean enable) {
        boolean change = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIndex() == index) {
                change = list.get(i).isEnable() != enable;
                list.get(i).setEnable(enable);
            }
        }
        adapter.notifyDataSetChanged();
        return change;
    }

    public void clearSelect() {
        for (RightBeanSelect item : list) {
            item.setCheck(false);
        }
        adapter.notifyDataSetChanged();
    }

    public int getSelectIndex() {
        int define = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIndex() != -1 && define == -1) {
                define = i;
            }
            if (list.get(i).isCheck()) {
                return i;
            }
        }
        return define;
    }

    public void setSelectIndex(int index) {
        for (RightBeanSelect item : list) {
            if (item.getIndex() == index) {
                item.setCheck(true);
            } else {
                item.setCheck(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public boolean setSelectText(String text) {
        if (TextUtils.isEmpty(text)) return false;
        boolean has = false;
        for (RightBeanSelect item : list) {
            if (item.getText().equals(text)) {
                has = true;
            }
        }
        if (has) {
            for (RightBeanSelect item : list) {
                if (item.getText().equals(text)) {
                    item.setCheck(true);
                } else {
                    item.setCheck(false);
                }
            }
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public int getSelectCount() {
        return list.size();
    }

    public RightViewSelect(Context context) {
        this(context, null);
    }

    public RightViewSelect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightViewSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    public ArrayList<RightBeanSelect> getList() {
        return list;
    }

    public RightBeanSelect getSelectItem() {
        return list.get(getSelectIndex());
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_rightslipselect, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightSlipViewSelect);
        array = ta.getTextArray(R.styleable.RightSlipViewSelect_array);
        listPadLeft = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_listPadLeft, 5);
        itemWidth = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemWidth, 120);
        itemHeight = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemHeight, 60);
        itemMargin = ta.getDimensionPixelSize(R.styleable.RightSlipViewSelect_itemMargin, 0);
        arrayOrientation = ta.getInt(R.styleable.RightSlipViewSelect_arrayOrientation, ARRAYORIENTATION_VERTICAL);
//        itemBgViewResId = ta.getResourceId(R.styleable.RightSlipViewSelect_itemBgViewResId, R.drawable.selector_rightslip_button);
        itemStartAndEndBgHalfCorner = ta.getBoolean(R.styleable.RightSlipViewSelect_itemStartAndEndBgHalfCorner, false);
        itemHalfCornerEach = ta.getInt(R.styleable.RightSlipViewSelect_itemHalfCornerEach, EACH_74_39);
        firstShowCount = ta.getInt(R.styleable.RightSlipViewSelect_firstShowCount, 0);
        firstNoneCount = 0;
        textLines = ta.getInt(R.styleable.RightSlipViewSelect_itemTextLines, 1);
        ta.recycle();
        if (array != null && array.length > 0) {
            updateView();
        }
    }

    public void setControlColorByChIdx(int chIdx){
        this.chIdx=chIdx;
//        switch (chIdx){
//            case 0: itemBgViewResId= R.drawable.selector_rightslip_button_ch1;
//            break;
//            case 1: itemBgViewResId= R.drawable.selector_rightslip_button_ch2;
//            break;
//            case 2: itemBgViewResId= R.drawable.selector_rightslip_button_ch3;
//            break;
//            case 3: itemBgViewResId= R.drawable.selector_rightslip_button_ch4;
//            break;
//            case 4: itemBgViewResId= R.drawable.selector_rightslip_button_math;
//            break;
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//                itemBgViewResId= R.drawable.selector_rightslip_button_ref;
//                break;
//            case 9: itemBgViewResId= R.drawable.selector_rightslip_button_s1;
//            break;
//            case 10: itemBgViewResId= R.drawable.selector_rightslip_button_s2;
//            break;
//        }
        updateView();

    }

    public void setOnlyControlColorByChIdx(int chIdx){
        adapter.setControlColorByCh(chIdx);
        adapter.notifyItemChanged(adapter.getSelectPosition());
    }

    public void setArray(CharSequence[] array) {
        if (array != null && array.length > 0) {
            this.array = array;
            updateView();
        }
    }

    public void setDoubleCheckEqual(boolean doubleCheckEqual) {
        this.doubleCheckEqual = doubleCheckEqual;
    }

    int itemMarginTop = 5;

    public void setItemMarginTop(int itemMarginTop) {
        this.itemMarginTop = itemMarginTop;
    }

    private void updateView() {
        listView = (RecyclerView) findViewById(R.id.listView);
        if (firstShowCount != 0) {
            switch (arrayOrientation) {
                case ARRAYORIENTATION_VERTICAL:
                    firstNoneCount = 0;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW2:
                    firstNoneCount = 2 - firstShowCount;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW3:
                    firstNoneCount = 3 - firstShowCount;
                    break;
                case ARRAYORIENTATION_HORIZONTALEACHROW4:
                    firstNoneCount = 4 - firstShowCount;
                    break;
                default:
                    firstNoneCount = 0;
                    break;
            }
        } else {
            firstNoneCount = 0;
        }

        listView.setLayoutManager(getLayoutManager());
        list = new ArrayList<RightBeanSelect>();
        for (int i = 0; i < firstNoneCount; i++) {
            RightBeanSelect beanSelect = new RightBeanSelect(-1, "", false);
            list.add(beanSelect);
        }
        for (int i = 0; i < array.length; i++) {
            if (StrUtil.isEmpty(array[i].toString())==false) {
                RightBeanSelect beanSelect = new RightBeanSelect(i, array[i].toString(), i == 0);
                list.add(beanSelect);
            }
        }
        adapter = new RightAdapterSelect(context, itemWidth, itemHeight, list, onAdapterListener);

        adapter.setItemMargin(itemMargin);
        adapter.setTextLines(textLines);
//        adapter.setItemBgViewResId(itemBgViewResId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter.setControlColorByCh(chIdx);
        }
        adapter.setItemStartAndEndBgHalfCorner(itemStartAndEndBgHalfCorner, itemHalfCornerEach);
        listView.setAdapter(adapter);
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        GridLayoutManager gridLayoutManager;
        switch (arrayOrientation) {
            case ARRAYORIENTATION_VERTICAL:
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            case ARRAYORIENTATION_HORIZONTALEACHROW2:
                return new GridLayoutManager(context, 2);
            case ARRAYORIENTATION_HORIZONTALEACHROW3:
                return new GridLayoutManager(context, 3);
            case ARRAYORIENTATION_HORIZONTALEACHROW4:
                return new GridLayoutManager(context, 4);
            default:
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        }
    }

    private RightAdapterSelect.OnItemClickListener onAdapterListener = new RightAdapterSelect.OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, RightBeanSelect item) {
            boolean checkEqual = false;//前后两次设置是否相同
            ScreenUtil.getViewLocation(itemView);
            for (RightBeanSelect bean : list) {
                if (bean.getIndex() == item.getIndex()) {
                    if (!bean.isCheck()) {
                        bean.setCheck(true);
                    } else {
                        checkEqual = true;
                    }
                } else {
                    if (bean.isCheck()) {
                        bean.setCheck(false);
                    }
                }
            }
            if (onItemClickListener != null) {
                onItemClickListener.onClickSound(checkEqual || doubleCheckEqual);
                onItemClickListener.onItemClickBeforRefreshUI(RightViewSelect.this.getId());
            }
            boolean isCurClickForce;
            if (!checkEqual || doubleCheckEqual) {
                isCurClickForce = true;
                adapter.notifyDataSetChanged();
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(RightViewSelect.this.getId(), item);
                }
                if (onItemClick2Listener != null) {
                    onItemClick2Listener.onItemClick2(RightViewSelect.this.getId(), itemView, item);
                }
            } else {
                isCurClickForce = false;
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickAfterRefreshUI(RightViewSelect.this.getId(), isCurClickForce);
            }
        }
    };
}
