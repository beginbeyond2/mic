package com.molihuan.pathselector.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageVolume;

import com.molihuan.pathselector.R;
import com.molihuan.pathselector.dialog.impl.SelectStorageDialog;
import com.molihuan.pathselector.entity.FontBean;
import com.molihuan.pathselector.entity.StorageBean;
import com.molihuan.pathselector.fragment.BasePathSelectFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UsbReceiverTools extends BroadcastReceiver {
    public List<StorageBean> newStorageList;

    public Context context;

    protected BasePathSelectFragment psf;             //总fragment


    public UsbReceiverTools(Context context, BasePathSelectFragment psf){
        this.context = context;
        this.psf = psf;
    }
    @Override
    public void onReceive(Context context, Intent intent){
        newStorageList = new ArrayList<>();
        //通过反射的方式得到所有的存储路径（内部存储+外部存储）
        List<StorageVolume> storageVolumes = ReflectTools.getStorageVolumes(context);
        if(storageVolumes == null || storageVolumes.isEmpty()){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            newStorageList = storageVolumes.stream()
                    .filter(Objects::nonNull)
                    .filter(volume-> volume.getDirectory()!=null)
                    .filter(v -> v.getState().equals("mounted"))
                    .map(volume -> new StorageBean(volume.getDirectory().getPath(),
                            false, volume.getDescription(context)))
                    .collect(Collectors.toList());
            //设置数据和监听
        }
        String action = intent.getAction();
        Uri uri = intent.getData();
        String currentModifyPath = "";
        if(uri !=null){
            currentModifyPath = uri.getPath();
        }
        if(psf.getTitlebarFragment().getSelectStorageDialog()!=null){
            psf.getTitlebarFragment().getSelectStorageDialog().updateStorageList(newStorageList);
        }
        if((Intent.ACTION_MEDIA_EJECT.equals(action)
                ||Intent.ACTION_MEDIA_REMOVED.equals(action)
                ||Intent.ACTION_MEDIA_UNMOUNTED.equals(action))&&psf.getCurrentPath().startsWith(currentModifyPath)){
                psf.updateTabbarList("/storage/emulated/0");
                psf.updateFileList("/storage/emulated/0");
                psf.getTitlebarFragment().setMainTitle(new FontBean(context.getResources().getString(R.string.internal_storage_mlh), 16, context.getColor(R.color.folder_text_color)));

        }

    }
}