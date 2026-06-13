package com.micsig.tbook.tbookscope.first;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.util.App;

public class LocaleChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            Logger.e("ACTION_LOCALE_CHANGED");
            App.finish();
        }
    }
}
