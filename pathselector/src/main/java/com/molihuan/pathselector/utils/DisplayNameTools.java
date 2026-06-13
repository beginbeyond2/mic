package com.molihuan.pathselector.utils;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageVolume;

import com.molihuan.pathselector.R;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.entity.StorageBean;
import com.molihuan.pathselector.entity.TabbarFileBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayNameTools {
    List<StorageVolume> storageVolumes;
    Map<String, String> volumeRootPathToDisplayName;

    public DisplayNameTools(Context context) {
        storageVolumes = ReflectTools.getStorageVolumes(context);
        volumeRootPathToDisplayName = new HashMap<>();
        for (StorageVolume volume : storageVolumes) {
            String dir = "";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(volume.getDirectory()==null) {
                    return;
                }
                dir = volume.getDirectory().toString();
            }
            if (dir == null) {
                continue;
            }
            String description = volume.getDescription(context);
            if(description.equals("内部存储空间")){
                description = ("Internal shared storage");
            }else {
                description = description.trim();
            }
            volumeRootPathToDisplayName.put(dir, description);
        }
    }

    public void convertListPathToDisplayName(List<FileBean> fileBeanList) {
        for (FileBean fileBean : fileBeanList) {
            String path = fileBean.getPath();
            for (Map.Entry<String, String> entry : volumeRootPathToDisplayName.entrySet()) {
                String volumeRootPath = entry.getKey();
                String volumeDisplayName = entry.getValue();
                if (path.startsWith(volumeRootPath) && fileBean.getDisplayName()==null) {
                    String relativePath = path.substring(volumeRootPath.length());
                    String newPathDisplay = "/" + volumeDisplayName + (relativePath);
                    if (newPathDisplay.endsWith("/")) {
                        newPathDisplay = newPathDisplay.substring(0, newPathDisplay.length() - 1);
                    }
                    fileBean.setDisplayName(newPathDisplay);
                }

            }
        }
    }

    public void convertSinglePathToDisplayName(FileBean fileBean) {
            String path = fileBean.getPath();
            for (Map.Entry<String, String> entry : volumeRootPathToDisplayName.entrySet()) {
                String volumeRootPath = entry.getKey();
                String volumeDisplayName = entry.getValue();
                if (path.startsWith(volumeRootPath) && fileBean.getDisplayName()==null) {
                    String relativePath = path.substring(volumeRootPath.length());
                    String newPathDisplay = "/" + volumeDisplayName + (relativePath);
                    if (newPathDisplay.endsWith("/")) {
                        newPathDisplay = newPathDisplay.substring(0, newPathDisplay.length() - 1);
                    }
                    fileBean.setDisplayName(newPathDisplay);
                }

            }
    }

    public void convertTabbarFileToDisplayName(TabbarFileBean tabbarFileBean) {

        for (Map.Entry<String, String> entry : volumeRootPathToDisplayName.entrySet()) {
            String volumeRootPath = entry.getKey();
            String volumeDisplayName = entry.getValue();
            if(volumeRootPath.equals(tabbarFileBean.getPath())){
                tabbarFileBean.setName(volumeDisplayName);
            }
        }

    }
}
