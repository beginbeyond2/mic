package com.micsig.tbook.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class MSelectionEditText extends AppCompatEditText {
    private OnSelectionChanged onSelectionChanged;

    public interface OnSelectionChanged {
        void onSelectionChanged(int selStart, int selEnd);
    }

    public OnSelectionChanged getOnSelectionChanged() {
        return onSelectionChanged;
    }

    public void setOnSelectionChanged(OnSelectionChanged onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    public MSelectionEditText(Context context) {
        super(context);
    }

    public MSelectionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MSelectionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(selStart, selEnd);
        }
    }

    public void setSelectionFromUser(int index) {
        if (index > getText().length()) {
            index = getText().length();
        }
        OnSelectionChanged onSelectionChanged = this.onSelectionChanged;
        this.onSelectionChanged = null;
        setSelection(index);
        this.onSelectionChanged = onSelectionChanged;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        OnSelectionChanged onSelectionChanged = this.onSelectionChanged;
        this.onSelectionChanged = null;
        super.setText(text, type);
        this.onSelectionChanged = onSelectionChanged;
    }
}
