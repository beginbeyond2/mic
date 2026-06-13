package com.micsig.base.keyevent;

import android.view.View;

import java.util.HashSet;
import java.util.Set;

public class KeyFilterBuilder {
    private View.OnKeyListener originalListener;
    private final Set<Integer> filteredKeyCoders = new HashSet<>();
    private boolean consumerFilteredKeys = true;


    /**
     * 设置原始监听器件
     */
    public KeyFilterBuilder setOriginalListener(View.OnKeyListener listener) {
        this.originalListener = listener;
        return this;
    }

    /**
     * 设置是否消费过滤的按键
     */
    public KeyFilterBuilder setConsumerFilteredKeys(boolean consume) {
        this.consumerFilteredKeys = consume;
        return this;
    }

    /**
     * 添加单个按键
     */
    public KeyFilterBuilder addKeyCode(int keyCode) {
        filteredKeyCoders.add(keyCode);
        return this;
    }

    /**
     * 添加按键组合
     */
    public KeyFilterBuilder addKeyCodeSet(Set<Integer> keyCodes) {
        filteredKeyCoders.addAll(keyCodes);
        return this;
    }


    /**
     * 最终为了构建KeyEventFilter
     * @return
     */
    public KeyEventFilter build() {
        return new KeyEventFilter(originalListener, filteredKeyCoders, consumerFilteredKeys);
    }

}
