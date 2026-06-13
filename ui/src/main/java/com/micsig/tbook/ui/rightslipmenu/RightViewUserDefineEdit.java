package com.micsig.tbook.ui.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.StrUtil;

public class RightViewUserDefineEdit extends RelativeLayout {
    private Context context;
    private TextView editClickView, editUserDefineView;
    private OnEditClickListener onEditClickListener;
    private String defaultStr = "UserDefine";

    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    public interface OnEditClickListener {
        void onEditClick(RightViewUserDefineEdit view, String text);
    }

    public RightViewUserDefineEdit(Context context) {
        this(context, null);
    }

    public RightViewUserDefineEdit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightViewUserDefineEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        View.inflate(context, R.layout.view_userdefine_edit, this);
        editClickView = (TextView) findViewById(R.id.editClickView);
        editUserDefineView = (TextView) findViewById(R.id.editUserDefineView);
        editClickView.setOnClickListener(onClickListener);
        defaultStr = context.getResources().getString(R.string.serialsUserDefine);
        editUserDefineView.setText(defaultStr);
    }

    @SuppressLint("SetTextI18n")
    public void setText(String text) {
        if (StrUtil.isEmpty(text)) {
            editUserDefineView.setText(defaultStr);
            editUserDefineView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        } else {
            editUserDefineView.setText(defaultStr + "\n" + text);
            editUserDefineView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18);
        }
    }
    @SuppressLint("ResourceType")
    public void setControlColorByChIdx(int chIdx){
        int itemBgViewResId=0;
        ColorStateList itemTextColor = null;
        switch (chIdx){
            case 0: itemBgViewResId= R.drawable.selector_rightslip_button_ch1;
                itemTextColor=getResources().getColorStateList( R.drawable.selector_rightslip_select_item_textcolor_ch1);
                break;
            case 1: itemBgViewResId= R.drawable.selector_rightslip_button_ch2;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch2);
                break;
            case 2: itemBgViewResId= R.drawable.selector_rightslip_button_ch3;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch3);
                break;
            case 3: itemBgViewResId= R.drawable.selector_rightslip_button_ch4;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ch4);
                break;
            case 4: itemBgViewResId= R.drawable.selector_rightslip_button_math;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_math);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                itemBgViewResId= R.drawable.selector_rightslip_button_ref;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_ref);
                break;
            case 9: itemBgViewResId= R.drawable.selector_rightslip_button_s1;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s1);
                break;
            case 10: itemBgViewResId= R.drawable.selector_rightslip_button_s2;
                itemTextColor=getResources().getColorStateList(R.drawable.selector_rightslip_select_item_textcolor_s2);
                break;
        }
//        editShowView.setTextColor(itemTextColor);
        editUserDefineView.setTextColor(itemTextColor);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editClickView.setEnabled(enabled);
        editUserDefineView.setEnabled(enabled);
    }

    public String getText() {
        return editUserDefineView.getText().toString().replace(defaultStr, "").replace("\n", "");
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(RightViewUserDefineEdit.this, editUserDefineView.getText().toString().replace(defaultStr, "").replace("\n", ""));
            }
        }
    };
}
