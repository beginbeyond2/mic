package com.micsig.tbook.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class FixedSizeHashSet<E> extends LinkedHashSet<E> {
    private final int maxSize;

    public FixedSizeHashSet(int maxSize) {
        super();
        if (maxSize <= 0) {
            throw new IllegalArgumentException("FixedSizeHashSet Max size must be positive!");
        }
        this.maxSize = maxSize;
    }


    @Override
    public boolean add(E e) {
        if (contains(e)) {
            remove(e);
        }
        if (size() >= maxSize) {
            Iterator<E> iterator = iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        return super.add(e);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public ArrayList<E> getReverseList() {//反向
        ArrayList<E> list = new ArrayList<>(this);
        Collections.reverse(list);
        return list;
    }

    public ArrayList<E> getPositiveList() {//正向
        return new ArrayList<>(this);
    }


    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("FixedSizeHashSet [%d/%d elements] %s", size(), maxSize, getReverseList().toString());
    }

}
