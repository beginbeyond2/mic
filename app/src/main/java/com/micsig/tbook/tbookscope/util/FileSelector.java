package com.micsig.tbook.tbookscope.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.molihuan.pathselector.PathSelector;
import com.molihuan.pathselector.dao.SelectConfigData;
import com.molihuan.pathselector.dialog.impl.DeleteConfirmDialog;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.entity.FontBean;
import com.molihuan.pathselector.fragment.BasePathSelectFragment;
import com.molihuan.pathselector.fragment.impl.FileShowFragment;
import com.molihuan.pathselector.fragment.impl.PathSelectFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.service.IConfigDataBuilder;
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl;
import com.molihuan.pathselector.utils.DToastDialog;
import com.molihuan.pathselector.utils.DisplayNameTools;
import com.molihuan.pathselector.utils.MConstants;
import com.molihuan.pathselector.utils.ReflectTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

public class FileSelector {


    public WindowManager windowManager;

    private TopDialogTextKeyBoard fileSelectorTextKeyBoard;

    public PathSelectHandler pathSelectHandler;

    public DToastDialog dToastDialog = new DToastDialog();

    private PathSelectFragment pathSelectFragment;

    public  Context context;

    public  FileSelector (Context context,PathSelectHandler pathSelectHandler){
        this.pathSelectHandler = pathSelectHandler;
        this.context = context;

        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params;
        int layout_paras_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_paras_type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        fileSelectorTextKeyBoard = new TopDialogTextKeyBoard(context);
        fileSelectorTextKeyBoard.showTitle();
        windowManager.addView(fileSelectorTextKeyBoard, params);

        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);;
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_FILESELECTOR).subscribe(consumerHideFileSelector);;
    }

    public  FileSelector (PathSelectHandler pathSelectHandler){
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);;
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_FILESELECTOR).subscribe(consumerHideFileSelector);;
        this.pathSelectHandler = pathSelectHandler;
    }

    public PathSelectFragment buildSaveFileSelector(String rootPath, String disPlayPath, Fragment fragment, Context context) {
        ConfigDataBuilderImpl builder;
        this.context = context;
        if (fragment == null) {
            builder = (ConfigDataBuilderImpl) PathSelector.build(context, MConstants.BUILD_DIALOG);
        } else {
            builder = (ConfigDataBuilderImpl) PathSelector.build(fragment, MConstants.BUILD_DIALOG);
        }
        String mainTitle = getDisplayPathSegment(disPlayPath);
        builder.setTitlebarBG(Color.BLACK)
                .setTitlebarMainTitle(new FontBean((mainTitle), 16, context.getColor(R.color.folder_text_color)))
                .setShowSelectStorageBtn(true)
                .setMaxCount(1)
                .setShowTabbarFragment(true)
                .setShowFileTypes("")
                .setRootPath(rootPath)
                .setAddFolderListener(new CommonItemListener(new FontBean("", 14)) {
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                        handleAddClick(currentPath, pathSelectFragment,context);
                        return false;
                    }
                })
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 14, Color.WHITE)) {
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                        if(selectedFiles.isEmpty()){
                            FileBean fileBean = new FileBean(currentPath);
                            DisplayNameTools displayNameTools =new DisplayNameTools(context);
                            displayNameTools.convertSinglePathToDisplayName(fileBean);
                            pathSelectHandler.addPathToSet(fileBean);

                        }else {
                            pathSelectHandler.addPathToSet(selectedFiles.get(0));
                        }
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();
                        return true;
                    }
                })
                .setHandleItemListeners(
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.x_close)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment());
                                deleteConfirmDialog.show();
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                StringBuilder builder = new StringBuilder();
                                for (FileBean fileBean : selectedFiles) {
                                    if (fileBean.isDir()) {
                                        builder.append(fileBean.getPath());
                                    } else {
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.file_selection_not_supported));
                                        return false;
                                    }
                                }
                                DToast.get().showBottom(context.getResources().getString(R.string.seleced_folder) + builder);
                                pathSelectFragment.openCloseMultipleMode(false);
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();

                                pathSelectHandler.addPathToSet(selectedFiles.get(0));
                                return true;
                            }
                        }
                ).show();


        return pathSelectFragment;
    }


    public PathSelectFragment buildSaveFileRefSelector(String rootPath, String disPlayPath, Fragment fragment, Context context) {
        ConfigDataBuilderImpl builder;
        this.context = context;
        if (fragment == null) {
            builder = (ConfigDataBuilderImpl) PathSelector.build(context, MConstants.BUILD_DIALOG);
        } else {
            builder = (ConfigDataBuilderImpl) PathSelector.build(fragment, MConstants.BUILD_DIALOG);
        }
        String mainTitle = getDisplayPathSegment(disPlayPath);
        return builder.setTitlebarBG(Color.BLACK)
                .setTitlebarMainTitle(new FontBean((mainTitle), 16, context.getColor(R.color.folder_text_color)))
                .setShowSelectStorageBtn(true)
                .setMaxCount(1)
                .setShowTabbarFragment(true)
                .setShowFileTypes("")
                .setRootPath(rootPath)
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 14, Color.WHITE)) {
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                        if(selectedFiles.isEmpty()){
                            FileBean fileBean = new FileBean(currentPath);
                            DisplayNameTools displayNameTools =new DisplayNameTools(context);
                            displayNameTools.convertSinglePathToDisplayName(fileBean);
                            pathSelectHandler.addPathToSet(fileBean);
                        }else {
                            pathSelectHandler.addPathToSet(selectedFiles.get(0));
                        }
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();
                        return true;
                    }
                })
                .setHandleItemListeners(
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.x_close)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment());
                                deleteConfirmDialog.show();
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                StringBuilder builder = new StringBuilder();
                                for (FileBean fileBean : selectedFiles) {
                                    if (fileBean.isDir()) {
                                        builder.append(fileBean.getPath());
                                    } else {
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.file_selection_not_supported));
                                        return false;
                                    }
                                }
                                DToast.get().showBottom(context.getResources().getString(R.string.seleced_folder) + builder);
                                pathSelectFragment.openCloseMultipleMode(false);
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();
                                pathSelectHandler.addPathToSet(selectedFiles.get(0));
                                return true;
                            }
                        }
                )
                .show();

    }

    public void buildInvokeFileSelector(String rootPath, String disPlayPath, Fragment fragment, Context context, boolean isFilesShowOnly, String[] waveFileType) {
        this.context = context;
        String mainTitle = getDisplayPathSegment(disPlayPath);
        PathSelector.build(fragment, MConstants.BUILD_DIALOG)
                .setTitlebarBG(Color.BLACK)
                .setTitlebarMainTitle(new FontBean(mainTitle, 16, context.getColor(R.color.folder_text_color)))
                .setShowSelectStorageBtn(true)
                .setShowTabbarFragment(true)
                .setRootPath(rootPath)
                .setMaxCount(1)
                //设置是否只显示文件
                .setIsFilesOnlyMode(isFilesShowOnly)
                //设置过滤类型
                .setShowFileTypes(waveFileType)
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 16, Color.WHITE)) {
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                        StringBuilder builder = new StringBuilder();
                        if (selectedFiles.size() == 0) {
                            dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported));
                            return false;
                        }
                        for (FileBean fileBean : selectedFiles) {
                            if (fileBean.isDir()) {
                                dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported));
                                return false;
                            } else {
                                builder.append(fileBean.getPath());
                            }
                        }
                        pathSelectHandler.addPathToSet(selectedFiles.get(0));
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();
                        return true;
                    }
                })
                .setHandleItemListeners(
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.cancle)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment());
                                deleteConfirmDialog.show();
                                pathSelectFragment.openCloseMultipleMode(false);
                                return false;
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) {
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                StringBuilder builder = new StringBuilder();
                                for (FileBean fileBean : selectedFiles) {
                                    if (fileBean.isDir()) {
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported));
                                        return false;
                                    } else {
                                        builder.append(fileBean.getPath());
                                    }
                                }
                                pathSelectFragment.openCloseMultipleMode(false);
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();
                                pathSelectHandler.addPathToSet(selectedFiles.get(0));
                                return true;
                            }
                        }

                )
                .show();
    }



    public void handleAddClick(String currentPath, BasePathSelectFragment pathSelectFragment, Context context) {
        fileSelectorTextKeyBoard.setData(context.getString(R.string.new_folder), TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() {
            @Override
            public void onDismiss(String result) {
                File newFolder = new File(currentPath, result);
                if (!newFolder.exists()) {
                    boolean isSuccess = newFolder.mkdirs();
                    if (isSuccess) {
                        dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getName() + " " + context.getResources().getString(R.string.toast_create_success));
                        pathSelectFragment.updateFileList();
                    } else {
                        dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getAbsolutePath() + " " + context.getResources().getString(R.string.toast_create_failure));
                    }
                } else {
                    dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getName() + " " + context.getResources().getString(R.string.error_already_exists));
                }
            }
        });
    }



    public Consumer<Boolean> consumerHideFileSelector = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();
            if(mConfigData.buildController!=null){
                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss();

            }
        }
    };

    private Consumer<Boolean> consumerHideKeyboard = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            fileSelectorTextKeyBoard.hide();
        }

    };
    public  String getDisplayPathSegment(String path){
        if(path==null)
            return "";
        String trimmed = path.replaceAll("^/+","").replaceAll("/+$","");
        int slashIndex = trimmed.indexOf('/');
        trimmed = slashIndex == -1 ? trimmed: trimmed.substring(0,slashIndex);
        if(trimmed.equals("Internal shared storage")){
            if(context!=null){
                trimmed = context.getString(R.string.internal_storage);
            }
        }
        return trimmed;
    }

    public interface PathSelectHandler{
        void addPathToSet(FileBean selectedBean);
    }

    public boolean isFAT32(String rootPath){
        List<StorageVolume> storageVolumes = ReflectTools.getStorageVolumes(context);
        for(StorageVolume volueme:storageVolumes){
            if(volueme.getState().equals("mounted")){
                String preFix = volueme.getDirectory().toString();
                if(rootPath.startsWith(preFix)){
                    String type = ReflectTools.getFileSystemType(context,volueme);
                    if(type.equals("FAT32")){
                        Log.d("FAT#2", volueme.getDescription(context)+"isFAT32:") ;
                        return true;
                    }else{
                        Log.d("FAT#2", volueme.getDescription(context)+"not FAT32:" + type) ;

                        return false;
                    }
                }
            }

        }

        return false;
    }
}
