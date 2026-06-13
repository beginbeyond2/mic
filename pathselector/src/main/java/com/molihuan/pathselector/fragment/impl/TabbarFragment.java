package com.molihuan.pathselector.fragment.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.adapter.TabbarListAdapter;
import com.molihuan.pathselector.dialog.impl.SelectStorageDialog;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.entity.StorageBean;
import com.molihuan.pathselector.entity.TabbarFileBean;
import com.molihuan.pathselector.fragment.AbstractTabbarFragment;
import com.molihuan.pathselector.service.IFileDataManager;
import com.molihuan.pathselector.service.impl.PathFileManager;
import com.molihuan.pathselector.utils.DisplayNameTools;
import com.molihuan.pathselector.utils.MConstants;
import com.molihuan.pathselector.utils.Mtools;
import com.molihuan.pathselector.utils.ReflectTools;
import com.xuexiang.xtask.XTask;
import com.xuexiang.xtask.core.ITaskChainEngine;
import com.xuexiang.xtask.core.param.ITaskResult;
import com.xuexiang.xtask.core.step.impl.TaskChainCallbackAdapter;
import com.xuexiang.xtask.core.step.impl.TaskCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName: TabbarFragment
 * @Author: molihuan
 * @Date: 2022/11/22/18:20
 * @Description:
 */
public class TabbarFragment extends AbstractTabbarFragment implements View.OnClickListener, OnItemClickListener {
    protected ImageView storageImgView;
    protected RecyclerView breadRecView;            //面包屑 RecyclerView
    protected TabbarListAdapter tabbarListAdapter;

    protected List<TabbarFileBean> tabbarList;             //数据
    //路径管理者
    private IFileDataManager pathFileManager;

    protected SelectStorageDialog selectStorageDialog;

    private String initPath;


    @Override
    public int setFragmentViewId() {
        return R.layout.fragment_tabbar_mlh;
    }

    @Override
    public void getComponents(View view) {
        storageImgView = view.findViewById(R.id.imgv_select_storage_tabbar);
        breadRecView = view.findViewById(R.id.recv_file_bread_tabbar);
    }

    @Override
    public void initData() {
        super.initData();
        //获取路径管理者
        pathFileManager = psf.getPathFileManager();
        initPath = mConfigDataReference.get().rootPath;
        //获取数据
        tabbarList = initTabbarList();

    }

    @Override
    public void initView() {

        if (mConfigDataReference.get().showSelectStorageBtn && !mConfigDataReference.get().showTitlebarFragment) {
            storageImgView.setVisibility(View.VISIBLE);
        }

        breadRecView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));//设置布局管理者
        tabbarListAdapter = new TabbarListAdapter(R.layout.item_tabbar_mlh, tabbarList);//适配器添加数据
        breadRecView.setAdapter(tabbarListAdapter);//RecyclerView设置适配器
        tabbarListAdapter.setOnItemClickListener(this);
        //更新
        updateTabbarList();

    }

    @Override
    public void setListeners() {
        storageImgView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imgv_select_storage_tabbar) {
            showSelectStorageDialog();

        }
    }

    public void showSelectStorageDialog() {

        if (selectStorageDialog == null) {
            selectStorageDialog = new SelectStorageDialog(mActivity);
        }

        selectStorageDialog.show();
    }


    @Override
    public TabbarListAdapter getTabbarListAdapter() {
        return tabbarListAdapter;
    }

    @Override
    public List<TabbarFileBean> getTabbarList() {
        return tabbarList;
    }

    private List<TabbarFileBean> initTabbarList() {
        return pathFileManager.initTabbarList(initPath, tabbarList);
    }

    @Override
    public List<TabbarFileBean> updateTabbarList() {

        return updateTabbarList(psf.getCurrentPath());
    }

    HashSet<String> filterPath = new HashSet<>();

    public void initFilterSet() {
        filterPath.add("storage");
        filterPath.add("emulated");
    }

    @Override
    public List<TabbarFileBean> updateTabbarList(String path) {
        //开始异步获取数据
        XTask.getTaskChain()
                .addTask(XTask.getTask(new TaskCommand() {
                    @Override
                    public void run() throws Exception {
                        //TODO 开始异步处理数据
                        tabbarList = pathFileManager.updateTabbarList(initPath, path, tabbarList, tabbarListAdapter);

                    }
                }))
                .setTaskChainCallback(new TaskChainCallbackAdapter() {
                    @Override
                    public void onTaskChainCompleted(@NonNull ITaskChainEngine engine, @NonNull ITaskResult result) {
                        //更新ui
                        //刷新
                        initFilterSet();
                        //是否需要使用uri
                        Iterator<TabbarFileBean> iterator = tabbarList.iterator();
                        while (iterator.hasNext()) {
                            TabbarFileBean fileBean = iterator.next();
                            if (filterPath.contains(fileBean.getName())) {
                                iterator.remove();
                            }
                        }
                        DisplayNameTools displayNameTools = new DisplayNameTools(getContext());
                        if (!tabbarList.isEmpty()) {
                            displayNameTools.convertTabbarFileToDisplayName(tabbarList.get(0));

                        }
                        pathFileManager.refreshFileTabbar(null, tabbarListAdapter, PathFileManager.TYPE_REFRESH_TABBAR);
                        int newPosition = tabbarListAdapter.getItemCount() - 1;
                        if (newPosition >= 0) {
                            breadRecView.smoothScrollToPosition(newPosition);
                        }


                    }
                })
                .start();

        return tabbarList;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void refreshTabbarList() {
        tabbarListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        if (adapter instanceof TabbarListAdapter) {
            TabbarFileBean item = tabbarList.get(position);
            String path = item.getPath();
            psf.updateFileList(path);
            updateTabbarList(path);

        }
    }
}
