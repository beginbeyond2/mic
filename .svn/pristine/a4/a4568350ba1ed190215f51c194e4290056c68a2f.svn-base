package com.micsig.tbook.ui.top.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.ui.R;

/**
 * Created by Administrator on 2017/4/6.
 */

public class TopViewSave extends LinearLayout {
    private Context context;
    private TextView input;
    private String titleString;
    private String inputString;
    private String confirmString;
    private int headWidth, editWidth, buttonWidth;
    private OnSaveClickListener onSaveClickListener;

    public interface OnSaveClickListener {
        void clickConfirm(TopViewSave view, String input);

        void inputClick(TopViewSave view, String text);
    }

    public TopViewSave(Context context) {
        this(context, null);
    }

    public TopViewSave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewSave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_savewithhead, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSave);
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_headWidth, 100);
        editWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_editWidth, 200);
        buttonWidth = ta.getDimensionPixelSize(R.styleable.TopViewSave_buttonWidth, 120);
        ta.recycle();
        updateView();
    }

    public void setData(String title, String preInput, String confirm, OnSaveClickListener listener) {
        this.titleString = title;
        this.inputString = preInput;
        this.confirmString = confirm;
        this.onSaveClickListener = listener;
        updateView();
    }

    public void setData(int titleResId, int preInputResId, int confirmResId, OnSaveClickListener listener) {
        this.titleString = context.getString(titleResId);
        this.inputString = context.getString(preInputResId);
        this.confirmString = context.getString(confirmResId);
        this.onSaveClickListener = listener;
        updateView();
    }

    private void updateView() {
        TextView title = (TextView) findViewById(R.id.title);
        input = (TextView) findViewById(R.id.input);
        Button confirm = (Button) findViewById(R.id.confirm);
        title.setText(titleString);
        input.setText(inputString);
        confirm.setText(confirmString);
        LinearLayout.LayoutParams lpTitle = (LayoutParams) title.getLayoutParams();
        lpTitle.width = headWidth;
        title.setLayoutParams(lpTitle);
        LinearLayout.LayoutParams lpInput = (LayoutParams) input.getLayoutParams();
        lpInput.width = editWidth;
        input.setLayoutParams(lpInput);
        LinearLayout.LayoutParams lpConfirm = (LayoutParams) confirm.getLayoutParams();
        lpConfirm.width = buttonWidth;
        confirm.setLayoutParams(lpConfirm);

//        input.setShowSoftInputOnFocus(false);
//        //下面两行作用：屏蔽edittext的复制粘贴功能
//        input.setLongClickable(false);
//        input.setCustomSelectionActionModeCallback(onInputActionModeListener);
        input.setOnClickListener(onInputClickListener);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int[] ints1 = new int[2];
//                input.getLocationOnScreen(ints1);
//                Logger.i("TopViewChannel,input:" + Arrays.toString(ints1) + "\t" + input.getWidth() + "\t" + input.getHeight());
//                int[] ints2 = new int[2];
//                v.getLocationOnScreen(ints2);
//                Logger.i("TopViewSave,confirm:" + Arrays.toString(ints2) + "\t" + v.getWidth() + "\t" + v.getHeight());
                if (onSaveClickListener != null) {
                    onSaveClickListener.clickConfirm(TopViewSave.this, input.getText().toString());
                }
            }
        });
    }

    public void setText(String text) {
        input.setText(text);
    }

    public String getText() {
        return input.getText().toString();
    }

    private OnClickListener onInputClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onSaveClickListener != null) {
                onSaveClickListener.inputClick(TopViewSave.this, input.getText().toString());
            }
        }
    };

    private ActionMode.Callback onInputActionModeListener = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };
}
