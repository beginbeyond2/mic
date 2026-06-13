package com.micsig.base.keyevent;

import android.view.KeyEvent;

public class KeyEventUtil {


    public static boolean isConfirmKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
    }

    public static boolean isBackKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_DEL;
    }

    public static boolean isForwardDel(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_FORWARD_DEL;
    }


    //tab ↑ ↓ ← →
    public static boolean isIgnoreKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_TAB
                || keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    public static boolean isLeft(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT;
    }

    public static boolean isRight(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    /**
     * 标签限制输入屏幕  冒号 分号 逗号
     */
    public static boolean ignoreKeyForLabel(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return (event.isShiftPressed() && keyCode == KeyEvent.KEYCODE_SEMICOLON)
                || keyCode == KeyEvent.KEYCODE_SEMICOLON
                || keyCode == KeyEvent.KEYCODE_COMMA;
    }

    public static boolean isNegative(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_NUMPAD_SUBTRACT
                || keyCode == KeyEvent.KEYCODE_MINUS;
    }


    public static boolean isPositive(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return (event.isShiftPressed() && keyCode == KeyEvent.KEYCODE_EQUALS)
                        || keyCode == KeyEvent.KEYCODE_NUMPAD_ADD;
    }

}
