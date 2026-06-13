package com.micsig.base.keyevent;

import android.view.KeyEvent;
import android.view.View;

import com.micsig.base.Logger;

import java.util.HashSet;
import java.util.Set;

public class KeyEventFilter implements View.OnKeyListener {

    private static final String TAG = "KeyEventFilter";
    private final View.OnKeyListener originalListener;
    private final Set<Integer> filteredKeyCoders;
    private final boolean consumerFilteredKeys;

    /**
     * @param onKeyListener        原始的监听器
     * @param filteredKeyCoders    需要过滤的按键组合
     * @param consumerFilteredKeys 是否消费过滤的按键(true:消费掉  false：继续传递)
     */
    public KeyEventFilter(View.OnKeyListener onKeyListener, Set<Integer> filteredKeyCoders, boolean consumerFilteredKeys) {
        this.originalListener = onKeyListener;
        this.filteredKeyCoders = filteredKeyCoders != null ? filteredKeyCoders : new HashSet<>();
        this.consumerFilteredKeys = consumerFilteredKeys;
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //先检查是否为过滤的按键
        boolean isContains = filteredKeyCoders.contains(keyCode);
        Logger.d(TAG, "keyCode= " + keyCode + " ,keyInFilterKey= " + isContains + " ,consumer= " + consumerFilteredKeys);
        if (isContains) {
            return consumerFilteredKeys;//根据配置决定是否过滤按键事件
        }
        if (originalListener != null) {
            return originalListener.onKey(v, keyCode, event);
        }
        return false;
    }

    /**
     * 添加新的过滤按键
     */
    private void addFilteredKeyCode(int keyCode) {
        filteredKeyCoders.add(keyCode);
    }

    /**
     * 移除过滤的按键
     */
    private void removeFilteredKeyCode(int keyCode) {
        filteredKeyCoders.remove(keyCode);
    }

    /**
     * 清空所有过滤的按键
     */
    private void clearFilteredKeyCode() {
        filteredKeyCoders.clear();
    }

}
