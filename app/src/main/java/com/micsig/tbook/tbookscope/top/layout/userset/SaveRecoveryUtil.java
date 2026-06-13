package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.content.SharedPreferences;

import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.util.HashMap;

/**
 * Created by yangj on 2017/7/17.
 */

public class SaveRecoveryUtil {
    public static final int SAVE_RECOVERY_NUMBER = 10;
    private static final String SAVE_RECOVERY_STRING = "SAVE_RECOVERY_";

    public static HashMap<String, String> getSaveRecoveryData(int index) {
        HashMap<String, String> resultMap = new HashMap<>();
        if (index >= 0 && index < SAVE_RECOVERY_NUMBER) {
            SharedPreferences preferences = App.get().getSharedPreferences(SAVE_RECOVERY_STRING + index, Context.MODE_PRIVATE);
            HashMap<String, String> curMap = CacheUtil.get().getCacheMap();
            for (String s : curMap.keySet()) {
                resultMap.put(s, preferences.getString(s, curMap.get(s)));
            }
        }
        return resultMap;
    }

    public static void putSaveRecoveryData(int index) {
        if (index >= 0 && index < SAVE_RECOVERY_NUMBER) {
            SharedPreferences preferences = App.get().getSharedPreferences(SAVE_RECOVERY_STRING + index, Context.MODE_PRIVATE);
            HashMap<String, String> curMap = CacheUtil.get().getCacheMap();
            for (String s : curMap.keySet()) {
                preferences.edit().putString(s, curMap.get(s)).commit();
            }
        }
    }
}
