package com.micsig.tbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.micsig.tbook.ui.bean.RadioButtonBean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-27 11:11
 */
public class MRadioGroup extends ConstraintLayout {
    private Context context;
    private CharSequence[] arrays;
    private int[] arraysColor;
    private int itemWidth, itemHeight, itemLeftMargin, itemRightMargin, itemTopMargin, itemBottomMargin;
    private RecyclerView listView;
    private MRadioGroupAdapter adapter;
    private int showStyle, firstLineShowCount, perLineShowTotalCount;
    private List<RadioButtonBean> list;
    private TextView txtPrompt;
    private int orientation;
    private String promptText;
    private int promptInterval;
    public Consumer<RadioButtonBean> OnIndexChange;

    public MRadioGroup(@NonNull Context context) {
        this(context, null);
    }

    public MRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MRadioGroup);
        arrays = ta.getTextArray(R.styleable.MRadioGroup_arrays);

        itemWidth = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemWidth, 120);
        itemHeight = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemHeight, 60);
        itemLeftMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemLeftMargin, 0);
        itemTopMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemTopMargin, 0);
        itemRightMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemRightMargin, 0);
        itemBottomMargin = ta.getDimensionPixelSize(R.styleable.MRadioGroup_itemBottomMargin, 0);

        promptText = ta.getString(R.styleable.MRadioGroup_promptText);
        promptInterval = ta.getDimensionPixelSize(R.styleable.MRadioGroup_promptInterval, 0);
        showStyle = ta.getInt(R.styleable.MRadioGroup_showStyle, 0);
        perLineShowTotalCount = ta.getInt(R.styleable.MRadioGroup_perLineShowTotalCount, 4);
        firstLineShowCount = ta.getInt(R.styleable.MRadioGroup_firstLineShowCount, perLineShowTotalCount);

        //默认为0，即水平，horizontal
        orientation = ta.getInt(R.styleable.MRadioGroup_android_orientation, 0);
        ta.recycle();
        initView();
    }

    private void initView() {
        View.inflate(context, R.layout.control_micsig_radiogroup, this);
        listView = findViewById(R.id.listView);
        txtPrompt = findViewById(R.id.txtPrompt);
        if (promptText == null) {
            txtPrompt.setVisibility(GONE);
        } else {
            txtPrompt.setText(promptText);
            txtPrompt.setVisibility(VISIBLE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.setMargins(promptInterval, 0, 0, 0);
            listView.setLayoutParams(layoutParams);
            this.requestLayout();
        }

        listView.setLayoutManager(getLayoutManager());
        list = getInitList();
        adapter = new MRadioGroupAdapter(context, itemWidth, itemHeight, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void setSpanSize(int count){
        ((StaggeredGridLayoutManager)listView.getLayoutManager()).setSpanCount(count);
    }
    public int getInitSpanSize(){
        return perLineShowTotalCount;
    }

    public void setArray(CharSequence[] array) {
        if (array != null && array.length > 0) {
            this.arrays = array;
            updateView();
        }
    }

    public void setColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            this.arraysColor = colors;
            updateView();
        }
    }


    public void setPreString(String preString) {
        boolean flag = false;
        for (RadioButtonBean item : list) {
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
            for (RadioButtonBean item : list) {
                if (item.getText().equals(preString)) {
                    item.setCheck(true);
                    isCheck = true;
                } else {
                    item.setCheck(false);
                }
            }
            if (!isCheck) {
                for (RadioButtonBean item : list) {
                    if (item.isUserDefine(context)) {
                        item.setCheck(true);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private List<RadioButtonBean> getInitList() {
        List<RadioButtonBean> list = new ArrayList<>();
        boolean enableBeforeColor= this.arraysColor!=null && (this.arraysColor.length==this.arrays.length)?true:false;
        if (orientation == 0) { //水平排列
            if (showStyle == 0) {
                int diff = perLineShowTotalCount - firstLineShowCount;
                for (int i = 0; i < diff; i++) {
                    list.add(new RadioButtonBean(-1, "", false,false,0, 0, null));
                }
                for (int i = 0; i < arrays.length; i++) {
                    if (enableBeforeColor){
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_button_common, this::OnClick));
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                    }else {
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_button_common, this::OnClick));
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                    }
//                    list.add(new RadioButtonBean(i, arrays[i].toString(), false, R.drawable.selector_button_common, this::OnClick));
//                    list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                }
            } else if (showStyle == 1) {
                if (enableBeforeColor){
                    list.add(new RadioButtonBean(0, arrays[0].toString(), false,true,arraysColor[0], R.drawable.selector_radio_circle_left, this::OnClick));
                    list.add(new RadioButtonBean(1, arrays[1].toString(), false,true,arraysColor[1], R.drawable.selector_radio_circle_right, this::OnClick));
                }else {
                    list.add(new RadioButtonBean(0, arrays[0].toString(), false, false,0,R.drawable.selector_radio_circle_left, this::OnClick));
                    list.add(new RadioButtonBean(1, arrays[1].toString(), false, false,0,R.drawable.selector_radio_circle_right, this::OnClick));
                }
//                list.add(new RadioButtonBean(0, arrays[0].toString(), false, R.drawable.selector_radio_circle_left, this::OnClick));
//                list.add(new RadioButtonBean(1, arrays[1].toString(), false, R.drawable.selector_radio_circle_right, this::OnClick));
            } else if (showStyle == 2) {
                for (int i = 0; i < arrays.length; i++) {
                    if (enableBeforeColor){
                        if (i == 0) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_rect_left, this::OnClick));
                        } else if (i == arrays.length - 1) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_rect_right, this::OnClick));
                        } else {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_rect_middle, this::OnClick));
                        }
                    }else {
                        if (i == 0) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_rect_left, this::OnClick));
                        } else if (i == arrays.length - 1) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_rect_right, this::OnClick));
                        } else {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_rect_middle, this::OnClick));
                        }
                    }
//                    if (i == 0) {
//                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, R.drawable.selector_radio_round_rect_left, this::OnClick));
//                    } else if (i == arrays.length - 1) {
//                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, R.drawable.selector_radio_round_rect_right, this::OnClick));
//                    } else {
//                        list.add(new RadioButtonBean(i, arrays[i].toString(), false, R.drawable.selector_radio_round_rect_middle, this::OnClick));
//                    }
                }
            }
        } else { //垂直排列
            if (showStyle == 0) {
                int diff = perLineShowTotalCount - firstLineShowCount;
                for (int i = 0; i < diff; i++) {
                    list.add(new RadioButtonBean(-1, "", false,false,0, 0, null));
                }
                for (int i = 0; i < arrays.length; i++) {
                    if (enableBeforeColor){
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_button_common, this::OnClick));
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                    }else{
                        list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_button_common, this::OnClick));
                        list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                    }
//                    list.add(new RadioButtonBean(i, arrays[i].toString(), false, R.drawable.selector_button_common, this::OnClick));
//                    list.get(list.size() - 1).setItemMargin(itemLeftMargin, itemTopMargin, itemRightMargin, itemBottomMargin);
                }
            } else {
                for (int i = 0; i < arrays.length; i++) {
                    if (enableBeforeColor){
                        if (i == 0) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_vertical_rect_top, this::OnClick));
                        } else if (i == arrays.length - 1) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_vertical_rect_middle, this::OnClick));
                        } else {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,true,arraysColor[i], R.drawable.selector_radio_round_vertical_rect_bottom, this::OnClick));
                        }
                    }else {
                        if (i == 0) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_vertical_rect_top, this::OnClick));
                        } else if (i == arrays.length - 1) {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_vertical_rect_middle, this::OnClick));
                        } else {
                            list.add(new RadioButtonBean(i, arrays[i].toString(), false,false,0, R.drawable.selector_radio_round_vertical_rect_bottom, this::OnClick));
                        }
                    }

                }
            }
        }
        return list;
    }

    private void updateView() {
        if (promptText == null) {
            txtPrompt.setVisibility(GONE);
        } else {
            txtPrompt.setText(promptText);
            txtPrompt.setVisibility(VISIBLE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.setMargins(promptInterval, 0, 0, 0);
            listView.setLayoutParams(layoutParams);
            this.requestLayout();
        }

        list = getInitList();
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }

    public void initData(String head, String[] array,int[] colors, Consumer<RadioButtonBean> OnIndexChange) {
        this.promptText = head;
        this.arrays = array;
        this.arraysColor=colors;
        this.OnIndexChange = OnIndexChange;
        updateView();
    }


    private void OnClick(View itemView, RadioButtonBean item) {

        for (RadioButtonBean bean : list) {
            if (item.getIndex() == bean.getIndex()) {
                bean.setCheck(true);
                if (OnIndexChange != null) OnIndexChange.accept(bean);
            } else {
                bean.setCheck(false);
            }
        }

        adapter.notifyDataSetChanged();
    }
    public void OnClick(int index)
    {
        RadioButtonBean bean=list.get(index);
        for (int i=0;i<list.size();i++) {
            RadioButtonBean item=list.get(i);
            if (item.getIndex() == bean.getIndex() && item.getVisible()==VISIBLE) {
                bean.setCheck(true);
                if (OnIndexChange != null) OnIndexChange.accept(bean);
            } else {
                bean.setCheck(false);
            }
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        if (orientation == 0) { //水平排列
            if (showStyle == 0) {
                return new StaggeredGridLayoutManager( perLineShowTotalCount,StaggeredGridLayoutManager.VERTICAL);
            } else {
                return new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            }
        } else { //垂直排列
            if (showStyle == 0) {
                return new StaggeredGridLayoutManager( 1,StaggeredGridLayoutManager.HORIZONTAL);
            } else {
                return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            }
        }

    }

    public void setSelectIndex(int index) {
        for (RadioButtonBean bean : list) {
            if (bean.getIndex() == index) {
                bean.setCheck(true);
            } else {
                bean.setCheck(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public int getSelectIndex() {
        int index = -1;
        for (RadioButtonBean bean : list) {
            if (bean.isCheck()) {
                index = bean.getIndex();
            }
        }
        return index;
    }
    public boolean getChecked(int idx){
        for(int i=0;i<list.size();i++){
            if (i==idx){
                return list.get(i).isCheck();
            }
        }
        return false;
    }

    public void clearSelect() {
        for (RadioButtonBean bean : list) {
            bean.setCheck(false);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 是否都没有选择
     * @return
     */
    public boolean isAllUnCheck(){
        for (RadioButtonBean bean:list){
            if (bean.isCheck()){
                return false;
            }
        }
        return true;
    }

    public int getFirstVisibleIdx(){
        for(RadioButtonBean bean:list){
            if (bean.getVisible()==VISIBLE){
                return bean.getIndex();
            }
        }
        return -1;
    }

    public int getItemVisible(int index){
        for(RadioButtonBean bean:list){
            if (bean.getIndex()==index){
                return bean.getVisible();
            }
        }
        return View.GONE;
    }

    public void setItemVisible(int index,int visible){
        for(RadioButtonBean bean:list){
            if (bean.getIndex()==index){
                bean.setVisible(visible);
            }
        }
        adapter.notifyDataSetChanged();
    }
    public void setItemEnable(int index, boolean enable) {
        for (RadioButtonBean bean : list) {
            if (bean.getIndex() == index) {
                bean.setEnable(enable);
            }
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void setEnabled(boolean enabled) {
        for (RadioButtonBean bean : list) {
            bean.setEnable(enabled);
        }
        adapter.notifyDataSetChanged();
    }


//    public int getContentIdx(int content) {
//        int result = -1;
//        try {
//            int[] list = new int[arrays.length];
//            for (int i = 0; i < list.length; i++) {
//                int baudRate = (int) UtilsUnit.UnitStringToDouble(arrays[i].toString(), UtilsUnit.Unit_bs);
//                if (baudRate == content) {
//                    result = i;
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            result = -1;
//        }
//        return result;
//    }
//
//    public boolean hasContain(String content) {
//        int idx = Tools.indexOf(arrays, s -> content.equals(s));
//        return idx >= 0;
//    }
}
