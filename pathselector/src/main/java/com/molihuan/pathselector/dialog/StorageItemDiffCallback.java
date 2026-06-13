package com.molihuan.pathselector.dialog;

import androidx.recyclerview.widget.DiffUtil;

import com.molihuan.pathselector.entity.StorageBean;

import java.util.List;

public class StorageItemDiffCallback extends DiffUtil.Callback {

    private final List<StorageBean> oldList;
    private final List<StorageBean> newList;

    public StorageItemDiffCallback(List<StorageBean> oldList, List<StorageBean> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getRootPath().equals(newList.get(newItemPosition).getRootPath());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
//        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        return oldList.get(oldItemPosition).getRootPath().equals(newList.get(newItemPosition).getRootPath());

    }
}
