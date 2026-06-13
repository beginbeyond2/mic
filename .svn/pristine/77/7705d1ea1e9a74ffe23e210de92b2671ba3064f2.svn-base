package com.molihuan.pathselector.fragment.impl;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.molihuan.utilcode.util.FileUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.daimajia.swipe.SwipeLayout;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.adapter.FileListAdapter;
import com.molihuan.pathselector.dialog.impl.DeleteConfirmDialog;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.fragment.AbstractFileShowFragment;
import com.molihuan.pathselector.listener.FileItemListener;
import com.molihuan.pathselector.service.IFileDataManager;
import com.molihuan.pathselector.service.impl.PathFileManager;
import com.molihuan.pathselector.utils.CommonTools;
import com.molihuan.pathselector.utils.DToastDialog;
import com.molihuan.pathselector.utils.DisplayNameTools;
import com.molihuan.pathselector.utils.FileTools;
import com.molihuan.pathselector.utils.MConstants;
import com.molihuan.pathselector.utils.Mtools;
import com.xuexiang.xtask.XTask;
import com.xuexiang.xtask.core.ITaskChainEngine;
import com.xuexiang.xtask.core.param.ITaskResult;
import com.xuexiang.xtask.core.step.impl.TaskChainCallbackAdapter;
import com.xuexiang.xtask.core.step.impl.TaskCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName: FileShowFragment
 * @Author: molihuan
 * @Date: 2022/11/22/18:19
 * @Description:
 */
public class FileShowFragment extends AbstractFileShowFragment implements OnItemClickListener, OnItemLongClickListener,DeleteConfirmDialog.OnDeleteListener {
    protected RecyclerView mRecView;

    //最初路径
    protected String initPath;
    //当前路径
    protected String currentPath;

    //List和Adapter
    private List<FileBean> selectedFileList;
    private List<FileBean> allFileList;
    private FileListAdapter fileListAdapter;
    private List<FileBean> filesOnlyList;

    //单选
    private Boolean radio;
    //排序类型
    private MConstants.SortRules sortType;
    //文件显示类型
    private List<String> showFileTypes;
    //选择类型
    private List<String> selectFileTypes;
    //路径管理者
    private IFileDataManager pathFileManager;
    //uri管理者
    private IFileDataManager uriFileManager;
    //当前是否为多选模式
    private boolean multipleSelectionMode = false;
    //多选数量
    private int selectedNumber = 0;
    //fileItem监听
    private FileItemListener fileItemListener;

    private boolean fileSelectionMode = false;

    private  DToastDialog dToastDialog = new DToastDialog();

    private int mCurrentPage = 0;
    private static final int PAGE_SIZE = 50;

    private String mCurrentTargetDir = "";
    @Override
    public void setInitPath(String initPath) {
        this.initPath = initPath;
    }

    @Override
    public int setFragmentViewId() {
        return R.layout.fragment_file_show_mlh;
    }

    @Override
    public void getComponents(View view) {
        mRecView = view.findViewById(R.id.recv_file_show);

    }

    @Override
    public void initData() {
        super.initData();

        //初始化选择列表
        selectedFileList = new ArrayList<>();
        //获取路径管理者
        pathFileManager = psf.getPathFileManager();
        //获取uri管理者
        uriFileManager = psf.getUriFileManager();
        //获取初始路径并设置当前路径
        initPath = mConfigDataReference.get().rootPath;
        currentPath = initPath;
        //获取配置数据
        radio = mConfigDataReference.get().radio;
        sortType = mConfigDataReference.get().sortType;
        showFileTypes = CommonTools.asStringList(mConfigDataReference.get().showFileTypes);
        selectFileTypes = CommonTools.asStringList(mConfigDataReference.get().selectFileTypes);
        //获取监听器
        fileItemListener = mConfigDataReference.get().fileItemListener;
        filesOnlyList = new ArrayList<>();
        //获取文件列表数据
        allFileList = initFileList();
//        if(psf.getSelectConfigData().)

    }

    @Override
    public void initView() {
        //第一次则设置Adapter和监听
        if (fileListAdapter == null) {
            mRecView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));//设置布局管理者
            mRecView.setBackgroundColor(getContext().getResources().getColor(R.color.black_mlh));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecView.getContext(), LinearLayoutManager.VERTICAL);
            mRecView.addItemDecoration(dividerItemDecoration);
            if (isFilesOnlyMode()) {
//                fileListAdapter = new FileListAdapter(R.layout.item_file_mlh, filesOnlyList, getContext());//适配器添加数据
                fileListAdapter = new FileListAdapter(R.layout.item_file_mlh,new ArrayList<>(),getContext());
                fileListAdapter.getLoadMoreModule().setOnLoadMoreListener(()-> {
                    loadNextPage();
                });
            } else {
                fileListAdapter = new FileListAdapter(R.layout.item_file_mlh, new ArrayList<>(), getContext());//适配器添加数据
                fileListAdapter.getLoadMoreModule().setOnLoadMoreListener(()-> {
                    loadCommonNextPage();
                });
            }
            mRecView.setAdapter(fileListAdapter);//RecyclerView设置适配器
            fileListAdapter.setOnItemClickListener(this);
            fileListAdapter.setOnItemLongClickListener(this);

        }

        //更新
        updateFileList();

    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onDelete(FileBean fileBean){
        if(deleteFileSafety(fileBean)){
            updateFileList();
            dToastDialog.show(psf.getDialog(),getContext().getString(R.string.delete_file_success));
        }else {
            dToastDialog.show(psf.getDialog(),getContext().getString(R.string.delete_file_fail));
        }
    }

    @Override
    public void onCancel(int position) {
        updateFileList();
        psf.getTitlebarFragment().setDeleteVisible(false);
    }

    @Override
    public String getCurrentPath() {
        return currentPath;
    }


    @Override
    public List<FileBean> getSelectedFileList() {
        if (!isFilesOnlyMode()) {
            selectedFileList = pathFileManager.getSelectedFileList(allFileList, selectedFileList);
        } else {
            selectedFileList = pathFileManager.getSelectedFileList(filesOnlyList, selectedFileList);
        }
        return selectedFileList;
    }

    @Override
    public List<FileBean> getFileList() {
        return allFileList;
    }

    private List<FileBean> initFileList() {

        if (FileTools.needUseUri(currentPath)) {
            allFileList = uriFileManager.initFileList(currentPath, allFileList);
        } else {
            allFileList = pathFileManager.initFileList(currentPath, allFileList);
        }

        return allFileList;
    }

    @Override
    public List<FileBean> updateFileList() {
        return updateFileList(currentPath);
    }

    HashSet<String> filterFolder = new HashSet<>();

    public void initFilterSet() {
        filterFolder.add(".appMarkerCache");
        filterFolder.add(".UserGuideCache");
        filterFolder.add("Alarms");
        filterFolder.add("Android");
    }


    @Override
    public List<FileBean> updateFileList(String path) {
        //更新当前路径
        this.currentPath = path;

        //开始异步获取文件列表数据
        XTask.getTaskChain()
                .addTask(XTask.getTask(new TaskCommand() {
                    @Override
                    public void run() throws Exception {
                        //是否需要使用uri
                        //如果是只看文件的模式，就获取所有该类型的文件
                        if (isFilesOnlyMode()) {
                            startFilesOnlyLoad();
                        }
                        if(!isFilesOnlyMode()){
                            if (FileTools.needUseUri(path)) {
                                startAllFilesLoad(path);
                            } else {
                                startAllFilesLoad(path);
                            }
                        }


                    }
                }))
                .setTaskChainCallback(new TaskChainCallbackAdapter() {
                    @Override
                    public void onTaskChainCompleted(@NonNull ITaskChainEngine engine, @NonNull ITaskResult result) {
                        initFilterSet();
                        //是否需要使用uri
                        Iterator<FileBean> iterator = allFileList.iterator();
                        while (iterator.hasNext()) {
                            FileBean fileBean = iterator.next();
                            if (filterFolder.contains(fileBean.getName()) || fileBean.getName().startsWith(".")) {
                                iterator.remove();
                            }
                        }
                        DisplayNameTools displayNameTools = new DisplayNameTools(mConfigDataReference.get().context);
                        displayNameTools.convertListPathToDisplayName(fileListAdapter.getData());
                        //更新ui
                        if (FileTools.needUseUri(path)) {
                            //刷新
                            uriFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
                        } else {
                            //刷新
                            pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);

                        }

                    }
                })
                .start();


        return allFileList;
    }

    @Override
    public void refreshFileList() {
        pathFileManager.refreshFileTabbar(fileListAdapter, psf.getTabbarListAdapter(), PathFileManager.TYPE_REFRESH_FILE_TABBAR);
    }

    @Override
    public FileListAdapter getFileListAdapter() {
        return this.fileListAdapter;
    }


    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        if (adapter instanceof FileListAdapter) {
            if(((FileListAdapter) adapter).openLayout!=null){
                ((FileListAdapter) adapter).openLayout.close();
                ((FileListAdapter) adapter).openLayout = null;
                return;
            }
            if (!isFilesOnlyMode()) {
                if(position <0 || position>= allFileList.size()){
                    return;
                }
                FileBean item = allFileList.get(position);
                //如果当前已经是多选模式
                if (multipleSelectionMode && (!radio)) {
                    //多选模式下不能点击返回item
                    //选择类型正确
                    if (FileTools.selectTypeCompliance(item.getFileExtension(), selectFileTypes)) {
                        if (fileSelectionMode && item.isDir()) {
                            updateFileList(item.getPath());//更新当前路径
                            //刷新面包屑
                            psf.updateTabbarList();
                        } else {
                            if (mConfigDataReference.get().maxCount == 1) {
                                pathFileManager.setBoxChecked(allFileList, null, false);
                                item.setBoxChecked(true);
                            } else {
                                //如果已经勾选了
                                if (item.getBoxChecked()) {
                                    item.setBoxChecked(false);
                                    selectedNumber--;
                                }
                                if (selectedNumber + 1 <= mConfigDataReference.get().maxCount || mConfigDataReference.get().maxCount == -1) {
                                    //没有勾选且没有超过最大数量、或最大数量是-1则不限制
                                    item.setBoxChecked(true);
                                    selectedNumber++;
                                } else {
                                    //超过选择的最大数量
                                    Mtools.toast(getString(R.string.tip_filebeanitem_select_limit_exceeded_mlh));

                                }

                            }
                        }
                        //如果只选择一个

                    } else {
                        Mtools.toast(getString(R.string.tip_filebeanitem_select_error_type_mlh));
                    }

                    pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);

                } else {
                    //如果是文件夹
                    if (item.isDir()) {
                        updateFileList(item.getPath());//更新当前路径
                        //刷新面包屑
                        psf.updateTabbarList();

                    } else {
                        //选择类型正确
                        if (FileTools.selectTypeCompliance(item.getFileExtension(), selectFileTypes)) {
//                        openCloseMultipleMode(!multipleSelectionMode);
                            pathFileManager.setBoxChecked(allFileList, null, false);
                            item.setBoxChecked(true);
                            pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
                            multipleSelectionMode = true;
                            fileSelectionMode = true;
                        }

                    }
                }
            } else {
                if(position <0 || position>= filesOnlyList.size()){
                    return;
                }
                FileBean item = filesOnlyList.get(position);
                psf.getTitlebarFragment().setDeleteVisible(true);
                if (multipleSelectionMode && (!radio)) {
                    if(item.getBoxChecked()){
                        pathFileManager.setBoxChecked(filesOnlyList, null, false);
                        multipleSelectionMode = false;
                        fileSelectionMode = false;
                        psf.getTitlebarFragment().setDeleteVisible(false);
                    }
                    else {
                        pathFileManager.setBoxChecked(filesOnlyList, null, false);
                        item.setBoxChecked(true);
                    }
                    pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
                } else {
                    if (FileTools.selectTypeCompliance(item.getFileExtension(), selectFileTypes)) {
                        pathFileManager.setBoxChecked(allFileList, null, false);
                        item.setBoxChecked(true);
                        pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
                        multipleSelectionMode = true;
                        fileSelectionMode = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        SwipeLayout swipeLayout = v.findViewById(R.id.swipe_layout);
        if(swipeLayout!=null && swipeLayout.getOpenStatus() != SwipeLayout.Status.Close){
            return false;
        }
        if (adapter instanceof FileListAdapter) {
            FileBean fileBean = new FileBean();
            mRecView.smoothScrollToPosition(position);
            if (!isFilesOnlyMode()) {
                fileBean = allFileList.get(position);
                if (!radio) {
                    //如果设置了fileItem监听
                    if (fileItemListener != null) {
                        boolean handled = fileItemListener.onLongClick(v, fileBean, currentPath, psf);
                        //已经处理完了就不需要再处理了
                        if (handled) {
                            return true;
                        }
                    }
                    if (fileSelectionMode == true) {
                        fileSelectionMode = false;
                        pathFileManager.setCheckBoxVisible(allFileList, null, false);
                        openCloseMultipleMode(fileBean, true);
                    } else {
                        openCloseMultipleMode(fileBean, !multipleSelectionMode);
                    }
                }
                return true;
            } else {
                fileBean = filesOnlyList.get(position);
                if (!radio) {
                    //如果设置了fileItem监听
                    if (fileItemListener != null) {
                        boolean handled = fileItemListener.onLongClick(v, fileBean, currentPath, psf);
                        //已经处理完了就不需要再处理了
                        if (handled) {
                            return true;
                        }
                    }
                    if (fileSelectionMode == true) {
                        fileSelectionMode = false;
                        pathFileManager.setCheckBoxVisible(filesOnlyList, null, false);
                        openCloseMultipleMode(fileBean, true);
                    } else {
                        openCloseMultipleMode(fileBean, !multipleSelectionMode);
                    }
                    return true;
                }
                //根据配置判断是否可以是使用多选,返回item不能长按

            }
            return false;

        }
        return true;
    }


    @Override
    public void selectAllFile(boolean status) {
        //只有允许多选并且当前是多选的情况下才可以
        if (!radio && multipleSelectionMode) {
            pathFileManager.setBoxChecked(allFileList, null, status);
            pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
        }
    }

    @Override
    public void openCloseMultipleMode(@Nullable FileBean fileBean, boolean status) {
        //长按进行多选模式切换
        multipleSelectionMode = status;
        //显示隐藏checkbox
        if (!isFilesOnlyMode()) {
            pathFileManager.setCheckBoxVisible(allFileList, null, multipleSelectionMode);
        } else {
            pathFileManager.setCheckBoxVisible(filesOnlyList, null, multipleSelectionMode);
        }

        psf.handleShowHide(multipleSelectionMode);

        //如果是多选模式则勾选当前长按的选项
        if (multipleSelectionMode) {
            //选择类型正确
            if (fileBean != null && FileTools.selectTypeCompliance(fileBean.getFileExtension(), selectFileTypes)) {
                fileBean.setBoxChecked(true);
                selectedNumber++;
            }
        }
        //刷新
        pathFileManager.refreshFileTabbar(fileListAdapter, null, PathFileManager.TYPE_REFRESH_FILE);
    }

    @Override
    public void openCloseMultipleMode(boolean status) {
        openCloseMultipleMode(null, status);
    }

    @Override
    public boolean isMultipleSelectionMode() {
        return multipleSelectionMode;
    }

    @Override
    public boolean onBackPressed() {
        //如果当前是多选模式则先退出多选模式
        if (multipleSelectionMode) {
            openCloseMultipleMode(false);
            selectedNumber = 0;
            return true;
        }

        String path = allFileList.get(0).getPath();
        //路径超过了最初的路径则直接返回
        if (!path.startsWith(initPath)) {
            return false;
        } else {
            //更新当前路径
            currentPath = path;
            updateFileList(currentPath);
            //刷新面包屑
            psf.updateTabbarList();
            return true;
        }

    }

    public boolean isFilesOnlyMode() {
        return psf.getSelectConfigData().isFilesOnly;
    }

    public boolean deleteFileSafety(FileBean fileBean) {
        File realFile = new File(fileBean.getPath());
        try {
            if (realFile.exists()) {
                FileUtils.delete(realFile);
            }
            return true;
        } catch (Exception e) {
            Log.e("Delete", "删除失败" + realFile.getName(), e);
        }
        return false;
    }

    private void startFilesOnlyLoad(){
        new Thread(() ->{
            List<FileBean> tempList = new ArrayList<>();
            if (isFilesOnlyMode()) {
                filesOnlyList.clear();
                pathFileManager.getFilesOnlyList(currentPath, tempList, showFileTypes);
                if(filesOnlyList!=null){
                    filesOnlyList = pathFileManager.sortFileList(tempList, MConstants.SortRules.SORT_TIME_DESC, currentPath);
                }
            }
            if(getActivity()!=null){
                getActivity().runOnUiThread(() -> {
                    filesOnlyList = tempList;
                    mCurrentPage = 0;
                    loadFirstPage();

                });
            }

        }).start();
    }

    private void startAllFilesLoad(String path){
        new Thread(() ->{
            List<FileBean> tempList = new ArrayList<>();
            if (FileTools.needUseUri(path)) {
                allFileList = uriFileManager.updateFileList(psf, initPath, path, tempList, fileListAdapter, showFileTypes);
                //排序
                allFileList = uriFileManager.sortFileList(tempList, mConfigDataReference.get().sortType, currentPath);
            } else {
                allFileList = pathFileManager.updateFileList(psf, initPath, path, tempList, fileListAdapter, showFileTypes);
                //排序
                allFileList = pathFileManager.sortFileList(tempList, MConstants.SortRules.SORT_TIME_DESC, currentPath);
            }
            if(getActivity()!=null){
                getActivity().runOnUiThread(() -> {
                    allFileList = tempList;
                    mCurrentPage = 0;
                    loadCommonFirstPage();
                });
            }

        }).start();
    }

    private void loadFirstPage(){
        List<FileBean> firstPage = getBatchData();
        fileListAdapter.setList(firstPage);
        DisplayNameTools displayNameTools = new DisplayNameTools(mConfigDataReference.get().context);
        displayNameTools.convertListPathToDisplayName(fileListAdapter.getData());
    }
    private void loadCommonFirstPage(){
        List<FileBean> firstPage = getCommonBatchData();
        fileListAdapter.setList(firstPage);
        DisplayNameTools displayNameTools = new DisplayNameTools(mConfigDataReference.get().context);
        displayNameTools.convertListPathToDisplayName(fileListAdapter.getData());
    }
    private void loadNextPage(){
        List<FileBean> nextBatch = getBatchData();
        if(!nextBatch.isEmpty()){
            fileListAdapter.addData(nextBatch);
            fileListAdapter.getLoadMoreModule().loadMoreComplete();
        }else {
            fileListAdapter.getLoadMoreModule().loadMoreEnd();
        }
        DisplayNameTools displayNameTools = new DisplayNameTools(mConfigDataReference.get().context);
        displayNameTools.convertListPathToDisplayName(fileListAdapter.getData());
    }

    private void loadCommonNextPage(){
        List<FileBean> nextBatch = getCommonBatchData();
        if(!nextBatch.isEmpty()){
            fileListAdapter.addData(nextBatch);
            fileListAdapter.getLoadMoreModule().loadMoreComplete();
        }else {
            fileListAdapter.getLoadMoreModule().loadMoreEnd();
        }
        DisplayNameTools displayNameTools = new DisplayNameTools(mConfigDataReference.get().context);
        displayNameTools.convertListPathToDisplayName(fileListAdapter.getData());
    }
    private List<FileBean> getBatchData(){
        int start = mCurrentPage * PAGE_SIZE;
        if(start>filesOnlyList.size()){
            return new ArrayList<>();
        }
        int end = Math.min(start +PAGE_SIZE, filesOnlyList.size());
        mCurrentPage++;
        return new ArrayList<>(filesOnlyList.subList(start,end));
    }
    private List<FileBean> getCommonBatchData(){
        int start = mCurrentPage * PAGE_SIZE;
        if(start>allFileList.size()){
            return new ArrayList<>();
        }
        int end = Math.min(start +PAGE_SIZE, allFileList.size());
        mCurrentPage++;
        return new ArrayList<>(allFileList.subList(start,end));
    }
}
