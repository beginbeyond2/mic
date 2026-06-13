package com.micsig.tbook.tbookscope.first;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.SplashDialog;
import com.micsig.tbook.tbookscope.config.IConfig;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.Screen;

import java.util.List;

/**
 * Created by yangj on 2017/7/4.
 */

public class FirstActivity extends AppCompatActivity {
    private static final String TAG = "FirstActivity";

    private static final int REQUEST_CODE_OVERLAY = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        if(isValidProduct()) {
            setContentView(R.layout.layout_first);
            requestPermissions();

            if(App.isMainActivity()){
                onShowStartListener.onShowStart();
            }else {
                if (!SplashDialog.get().isVisible()) {
                    if (!Settings.canDrawOverlays(FirstActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                    } else {
                        SplashDialog.get().showDialog(getParamsType(), onShowStartListener);
                    }
                }
                Screen.getScreen(App.get());
            }
        }else{
            Toast.makeText(this,R.string.app_system_supported,Toast.LENGTH_LONG).show();

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY) {
            SplashDialog.get().showDialog(getParamsType(), onShowStartListener);
        }
    }

    private SplashDialog.OnShowStartListener onShowStartListener = new SplashDialog.OnShowStartListener() {
        @Override
        public void onShowStart() {
            Intent intent = new Intent(FirstActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    };

    private int getParamsType() {
        int paramsType;
        if (Settings.canDrawOverlays(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            paramsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            paramsType = WindowManager.LayoutParams.TYPE_TOAST;
        }
        return paramsType;
    }
    private void requestPermissions(){
        XXPermissions.with(this)
            .permission(Permission.Group.STORAGE)
            .request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        Log.d(TAG,"获取存储权限成功");
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    if (never) {
                        Log.d(TAG,"被永久拒绝授权，请手动授予存储权限");
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(FirstActivity.this, permissions);
                    } else {
                        Log.d(TAG,"获取存储权限失败");
                    }
                }
            });
    }
    private boolean isValidProduct(){
        IConfig config = ScopeConfig.getConfig();
        return config.isValidProduct();
    }
}
