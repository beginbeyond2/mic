package com.micsig.tbook.ui.top.view.radioGroup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.micsig.tbook.ui.MTextView;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.util.ScreenUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class TopVIewMultiSelectRadioButton extends  LinearLayout{
    private final String TAG = "TopViewRadioGroup";

    private Context context;
    private String head;
    private CharSequence[] array;
    private LinearLayout radioGroup;
    private TopVIewMultiSelectRadioButton.OnCheckChangedListener onCheckChangedListener;
    private MTextView headView;


    private int headWidth;
    private int itemWidth;
    private int itemHeight;
    private int bgRadioGroup;
    private int bgRadioButton;
    private int itemTextColor;
    private int promptTxtColor;
    private int itemTextSize;
//    private List<Boolean> listChecked = new ArrayList<Boolean>();

    public List<Integer> selectedRadio = new ArrayList<Integer>();
    public interface OnCheckChangedListener {
        void onClick(TopVIewMultiSelectRadioButton view, TopBeanChannel item);

        /**
         * 播放声音
         *
         * @param isCheckedSuccess 是否选择成功：当前如果已经选择了，再次选择，则为失败（false）
         */
        void onClickSound(boolean isCheckedSuccess);

        /**
         * 触发提示
         */
        void onPrompt(TopVIewMultiSelectRadioButton view);
    }

    public TopVIewMultiSelectRadioButton(Context context) {
        this(context, null);
    }

    public TopVIewMultiSelectRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopVIewMultiSelectRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);

    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_selectmultigroupwithhead, this);
        setOrientation(HORIZONTAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewRadioGroup);
        head = ta.getString(R.styleable.TopViewRadioGroup_head);
        array = ta.getTextArray(R.styleable.TopViewRadioGroup_array);
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_headWidth, 100);
        itemWidth = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemWidth, 120);
        itemHeight = ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_itemHeight, 60);
        bgRadioGroup = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioGroup, R.drawable.bg_radiogroup_selectwithhead);
        bgRadioButton = ta.getResourceId(R.styleable.TopViewRadioGroup_bgRadioButton, R.drawable.bg_radiobutton_middle);
        itemTextColor = ta.getColor(R.styleable.TopViewRadioGroup_itemTextColor, getResources().getColor(R.color.textColorNewTopViewEnable));
        itemTextSize=ta.getDimensionPixelSize(R.styleable.TopViewRadioGroup_android_textSize,20);
        ta.recycle();

        headView=findViewById(R.id.head);
        headView.setTextSize(TypedValue.COMPLEX_UNIT_PX,itemTextSize);

        if (array != null && array.length > 0) {
            updateView();
        }
    }

    public void setOnListener(TopVIewMultiSelectRadioButton.OnCheckChangedListener onCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public void setHeadWidth(int headWidth) {
        this.headWidth = headWidth;
    }


    public void setData(String head, String[] array, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) {
        this.head = head;
        this.array = array;
        this.onCheckChangedListener = listener;
        updateView();

    }

    public void setData(int headResId, int arrayResId, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) {
        this.head = context.getString(headResId);
        this.array = context.getResources().getStringArray(arrayResId);
        this.onCheckChangedListener = listener;
        updateView();
    }

    public void setData(String head, int arrayResId, TopVIewMultiSelectRadioButton.OnCheckChangedListener listener) {
        this.head = head;
        this.array = context.getResources().getStringArray(arrayResId);
        this.onCheckChangedListener = listener;
        updateView();
    }

    @SuppressLint("ResourceType")
    private void updateView() {
        MTextView headView = (MTextView) findViewById(R.id.head);
        ViewGroup.LayoutParams headParams = headView.getLayoutParams();
        headParams.width = headWidth;
        headView.setLayoutParams(headParams);
        if (!StrUtil.isEmpty(head)) {
            headView.setText(head);
        } else {
            headView.setVisibility(View.GONE);
        }

        radioGroup = (LinearLayout) findViewById(R.id.topViewMultiRadioGroup);
        radioGroup.setBaselineAligned(false);
        radioGroup.removeAllViews();
        if (array != null && array.length >= 2) {
            for (int i = 0; i < array.length; i++) {
                RadioButton radioButton = new RadioButton(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, itemHeight);
                radioButton.setLayoutParams(layoutParams);
                radioButton.setGravity(Gravity.CENTER);
                radioButton.setButtonDrawable(null);
//                radioButton.setBackgroundResource(bgRadioButton);
                if (i == 0) {
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_left);
                } else if (i == array.length - 1) {
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_right);
                } else {
                    radioButton.setBackgroundResource(R.drawable.bg_radiobutton_middle);
                }
                radioButton.setText(array[i]);
                if (isSpecialSymbol(array[i])) {
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize + 4);
                } else {
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize);
                }

//                radioButton.setTextColor(itemTextColor);
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color));
                //radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,itemTextSize);
                radioButton.setTag(false);
                radioGroup.addView(radioButton);
                radioButton.setOnClickListener(v ->{
                    int position = radioGroup.indexOfChild(radioButton);
                    Log.d(TAG, "updateView: " + position);
                    radioButton.setSelected(!radioButton.isSelected());
                    if(radioButton.isSelected()){
                        selectedRadio.add(radioGroup.indexOfChild(radioButton));
                        radioButton.setChecked(true);
                    }else {
                        //因为remove有两种类型，remove int会按照坐标remove，显示装箱，可以remove对象
                        selectedRadio.remove(Integer.valueOf(radioGroup.indexOfChild(radioButton)));
                        radioButton.setChecked(false);
                    }
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color));
                });
            }
        }
    }

    private boolean isSpecialSymbol(CharSequence charSequence) {
        return "<".contentEquals(charSequence) || ">".contentEquals(charSequence)
                || "<>".contentEquals(charSequence)
                || "=".contentEquals(charSequence) || "≠".contentEquals(charSequence);
    }

    /**
     * 设置index位置的按钮是否有点击提示
     *
     * @param index index位置
     * @param tag   是否有点击提示
     */
    public boolean setRadioButtonOnPromptState(int index, Boolean tag) {
        if (index < radioGroup.getChildCount()) {
            radioGroup.getChildAt(index).setTag(tag);
            return true;
        }
        return false;
    }

    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }


    public void setItemTextColor(int textColor){
        this.itemTextColor=textColor;
        updateView();
    }
    public void setPromptTxtColor(int textColor){
        this.promptTxtColor=textColor;
        this.headView.setTextColor(textColor);
    }

    public boolean getEnabled(int index) {
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled();

    }

    public boolean isEnabled(int index) {
        return radioGroup.isEnabled() && radioGroup.getChildAt(index).isEnabled();
    }

    public boolean isEnabled() {
        return radioGroup.isEnabled();
    }

    private View.OnTouchListener itemTouchListener = new View.OnTouchListener() {
        View v = null;
        boolean moveOut = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    this.v = v;
                    moveOut = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getX() < 0 || event.getX() > v.getWidth()
                            || event.getY() < 0 || event.getY() > v.getHeight()) {
                        moveOut = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (((Boolean) v.getTag())) {
                        if (onCheckChangedListener != null) {
                            onCheckChangedListener.onPrompt(TopVIewMultiSelectRadioButton.this);
                        }
                        return true;
                    }
                    if (this.v == null || this.v != v) break;
                    if (!moveOut) {
                        itemClickListener.onClick(v);
                    } else {
                        return true;
                    }
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener itemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                ScreenUtil.getViewLocation(radioButton);
                if (onCheckChangedListener != null) {
                    if (v.getId() == radioButton.getId()) {
//                        onCheckChangedListener.onClickSound(listChecked.get(i) != radioButton.isChecked());
                        onCheckChangedListener.onClick(TopVIewMultiSelectRadioButton.this, new TopBeanChannel(i, radioButton.getText().toString()));
                    }
                }
//                ra.set(i, radioButton.isChecked());
            }
        }
    };

    @SuppressLint("ResourceType")
    @Override
    public void setEnabled(boolean enabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if(!enabled){
                if (!radioButton.isSelected()) {
                    radioButton.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable));
                    radioButton.setEnabled(false);
                } else {
                    radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color));
                    radioButton.setClickable(false);
                }
            }else {
                radioButton.setTextColor(getResources().getColorStateList(R.drawable.selector_text_color));
                radioButton.setEnabled(true);
                radioButton.setClickable(true);

            }

        }
    }
}
