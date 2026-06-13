package com.micsig.tbook.ui.top.view.title;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.micsig.tbook.ui.MRadioButton;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.ScreenUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/5.
 */

public class TopViewTitle {
    public static final int WIDTH_SHOW = 1800;
    public static final int ItemWidth=145;

    private Context context;
    private View inflate;
    private ArrayList<TopAllBeanTitle> allList;
    private ArrayList<TopShowBeanTitle> showList;
    private RadioGroup radioGroup;
    private RadioButton checkedRadioButton;
    private OnCheckChangedTitleListener onCheckChangedTitleListener;
    private View.OnClickListener onItemClickListener;

    private int itemWidth;
    private int itemBgResIdCheck, itemBgResIdUnCheck, itemBgResIdUnCheckUnLine;
    private int flag = TopViewTitleWithScroll.TWO_TITLE;
    private int itemTextSize;
    private int itemTextColorSelect;
    private int itemTextColorUnSelect;

    private boolean addView = false;

    public interface OnCheckChangedTitleListener {
        void checkChanged(View view, TopAllBeanTitle item);
    }

    public TopViewTitle(Context context) {
        this.context = context;
        initView(context);
    }

    public void setData(String[] array, boolean[] arrayVisible, OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        allList = new ArrayList<TopAllBeanTitle>();
        showList = new ArrayList<TopShowBeanTitle>();
        for (int i = 0; i < array.length; i++) {
            TopAllBeanTitle beanTitle = new TopAllBeanTitle(allList.size(), array[i], arrayVisible[i]);
            allList.add(beanTitle);
            if (arrayVisible[i]) {
                showList.add(new TopShowBeanTitle(showList.size(), beanTitle.getIndex(), beanTitle.getText()));
            }
        }
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
        this.onItemClickListener = onItemClickListener;
        updateView();
    }

    public void setListener(OnCheckChangedTitleListener onCheckChangedTitleListener, View.OnClickListener onItemClickListener) {
        this.onCheckChangedTitleListener = onCheckChangedTitleListener;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateItemText(int index, String text) {
        ((RadioButton) radioGroup.getChildAt(index)).setText(text);
        showList.get(index).setText(text);
        allList.get(showList.get(index).getIndexAll()).setText(text);
    }

    public void setBg(int itemBgResIdCheck, int itemBgResIdUnCheck, int itemBgResIdUnCheckUnLine) {
        this.itemBgResIdCheck = itemBgResIdCheck;
        this.itemBgResIdUnCheck = itemBgResIdUnCheck;
        this.itemBgResIdUnCheckUnLine = itemBgResIdUnCheckUnLine;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setItemTextSize(int itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public RadioButton getCheckedRadioButton() {
        return checkedRadioButton;
    }

    public TopAllBeanTitle getSelected() {
        int num = addView ? radioGroup.getChildCount() - 1 : radioGroup.getChildCount();
        for (int i = 0; i < num; i++) {
            if (radioGroup.getCheckedRadioButtonId() == radioGroup.getChildAt(i).getId()) {
                return allList.get(showList.get(i).getIndexAll());
            }
        }
        return null;
    }

    public ArrayList<TopAllBeanTitle> getAllList() {
        return allList;
    }

    public ArrayList<TopShowBeanTitle> getShowList() {
        return showList;
    }

    public TopAllBeanTitle getItem(int index) {
        return allList.get(index);
    }

    public void setSelected(int index) {
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getIndexAll() == index) {
                radioGroup.check(radioGroup.getChildAt(i).getId());
                checkedChangeListener.onCheckedChanged(radioGroup, radioGroup.getChildAt(i).getId());
                checkedRadioButton = (RadioButton) radioGroup.getChildAt(i);
                break;
            }
        }
    }

    public void setEnable(int index, boolean enable) {
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getIndexAll() == index) {
                radioGroup.getChildAt(i).setEnabled(enable);
                break;
            }
        }
    }

    public View getInflate() {
        return inflate;
    }

    private void initView(Context context) {
        inflate = View.inflate(context, R.layout.view_toptitleview, null);
        itemWidth = ItemWidth;
//        itemBgResIdCheck = R.drawable.bg_topviewtitle_bg_check;
//        itemBgResIdUnCheck = R.drawable.bg_topviewtitle_bg_uncheck;
//        itemBgResIdUnCheckUnLine = R.drawable.bg_topviewtitle_bg_uncheck_unline;
        itemTextSize = 20;
        itemTextColorSelect = R.color.textColorNewTopTitleSelect;
        itemTextColorUnSelect = R.color.textColorNewTopTitleUnSelect;
    }

    private void updateView() {
        radioGroup = (RadioGroup) inflate.findViewById(R.id.topTitleRadioGroup);
        if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
            radioGroup.setBackgroundResource(R.drawable.bg_topviewtitle_item_unselect);
//            radioGroup.setBackgroundColor(context.getResources().getColor(R.color.color_backcolor_mainMenu_title));
        } else {
            radioGroup.setBackgroundColor(context.getResources().getColor(R.color.color_backcolor_mainMenu_title));
        }
        radioGroup.removeAllViews();
//        radioGroup.setOnCheckedChangeListener(checkedChangeListener);
        for (int i = 0; i < showList.size(); i++) {
            final MRadioButton radioButton = new MRadioButton(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setButtonDrawable(context.getResources().getDrawable(android.R.color.transparent));
            if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
//                radioButton.setCompoundDrawables(null, null, null, context.getDrawable(R.drawable.top_menu_first_select));
            }
            radioButton.setGravity(Gravity.CENTER);
//            radioButton.setBackgroundResource(i == 0 ? itemBgResIdCheck : itemBgResIdUnCheck);
            radioButton.setBackground(null);
            radioButton.setText(showList.get(i).getText());
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
            if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
                radioButton.setTextColor(context.getResources().getColorStateList(itemTextColorUnSelect));
            } else {
                radioButton.setTextColor(context.getResources().getColorStateList(i == 0 ? itemTextColorSelect : itemTextColorUnSelect));
            }
            radioButton.setLineSpacing(0, 0.9f);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(v);
                    }
                    checkedChangeListener.onCheckedChanged(radioGroup, radioButton.getId());
                }
            });
            radioGroup.addView(radioButton);
            if (i == 0) {
                radioGroup.check(radioButton.getId());
                checkedRadioButton = radioButton;
            }
        }
        if (itemWidth * showList.size() < WIDTH_SHOW) {
            addView = true;
            RadioButton radioButton = new RadioButton(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WIDTH_SHOW - itemWidth * showList.size(), ViewGroup.LayoutParams.MATCH_PARENT);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setButtonDrawable(context.getResources().getDrawable(android.R.color.transparent));
            radioButton.setEnabled(false);
//            radioButton.setBackgroundResource(itemBgResIdUnCheckUnLine);
            radioGroup.addView(radioButton);
        } else {
            addView = false;
        }
    }

    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int count = addView ? group.getChildCount() - 1 : group.getChildCount();
            for (int i = 0; i < count; i++) {
                RadioButton radioButton2 = (RadioButton) group.getChildAt(i);
                ScreenUtil.getViewLocation(radioButton2);
                if (checkedId == group.getChildAt(i).getId()) {
                    checkedRadioButton = radioButton2;
                    if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
//                        radioButton2.setCompoundDrawables(null, null, null, context.getDrawable(R.drawable.top_menu_first_select));
                    } else {
                        radioButton2.setTextColor(context.getResources().getColor(itemTextColorSelect));
                    }
//                    radioButton2.setBackgroundResource(itemBgResIdCheck);
                    if (onCheckChangedTitleListener != null) {
                        onCheckChangedTitleListener.checkChanged(inflate, allList.get(showList.get(i).getIndexAll()));
                    }
                } else {
                    if (flag == TopViewTitleWithScroll.FIRST_TITLE) {
                        radioButton2.setCompoundDrawables(null, null, null, null);
                    } else {
                        radioButton2.setTextColor(context.getResources().getColor(itemTextColorUnSelect));
                    }
//                    if (i != count - 1 && checkedId == group.getChildAt(i + 1).getId()) {
//                        radioButton2.setBackgroundResource(itemBgResIdUnCheckUnLine);
//                    } else {
//                        radioButton2.setBackgroundResource(itemBgResIdUnCheck);
//                    }
                }
            }
        }
    };
}
