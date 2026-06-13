package com.molihuan.pathselector.utils;

import android.os.Bundle;
import android.os.Parcel;

import java.util.HashMap;

public class BundleUtil {

    public static long getBundleSize(final Bundle bundle) {
        if (bundle == null) return 0L;
        Parcel parcel = Parcel.obtain();
        long size;
        try {
            parcel.writeBundle(bundle);
            size = parcel.dataSize();
        } finally {
            parcel.recycle();
        }
        return size;
    }

    public static HashMap<String, Long> getBundleKetSize(final Bundle bundle) {
        HashMap<String, Long> sizeMap = new HashMap<>();
        if (bundle == null) return sizeMap;
        for (String key : bundle.keySet()) {
            Parcel parcel = Parcel.obtain();
            long size;
            try {
                parcel.writeBundle(bundle);
                size = parcel.dataSize();
                sizeMap.put(key, size);
            } finally {
                parcel.recycle();
            }
        }
        return sizeMap;
    }
}
