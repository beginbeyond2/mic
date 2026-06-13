package com.micsig.tbook.ui.util;

import com.molihuan.pathselector.entity.FileBean;

import java.util.ArrayList;

public class FileBeanToStr {


    public static ArrayList<String> getDisPlayStrList(ArrayList<FileBean> fileBeans) {
        ArrayList<String> list = new ArrayList<>();
        for (FileBean fileBean : fileBeans) {
            list.add(fileBean.getDisplayName());
        }
        return list;
    }


    public static ArrayList<String> getAbsoluteStrList(ArrayList<FileBean> fileBeans) {
        ArrayList<String> list = new ArrayList<>();
        for (FileBean fileBean : fileBeans) {
            list.add(fileBean.getPath());
        }
        return list;
    }



}
