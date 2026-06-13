package com.molihuan.pathselector.fragment.impl;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.adapter.MorePopupAdapter;
import com.molihuan.pathselector.dialog.impl.SelectStorageDialog;
import com.molihuan.pathselector.entity.FontBean;
import com.molihuan.pathselector.fragment.AbstractTitlebarFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.utils.MConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TitlebarFragment
 * @Author: molihuan
 * @Date: 2022/11/22/18:15
 * @Description:
 */
public class TitlebarFragment extends AbstractTitlebarFragment implements View.OnClickListener, OnItemClickListener, OnItemLongClickListener {

    protected View positionView;                      //定位视图
    protected RelativeLayout relParent;               //父控件
    protected ImageView backImgView;                  //返回按钮
    protected ImageView storageImgView;                  //内存卡按钮

    protected ImageView searchImgView;                //搜索按钮
    protected ImageView moreImgView;                  //更多选项
    protected TextView mainTitleTv;                   //主标题
    protected TextView subtitleTv;                    //副标题(跑马灯还没实现)
    protected TextView oneOptionTv;                   //一个选项

    protected ImageView addFolderView;         //添加文件夹

    protected ImageView currentPathView;         //选中当前路径

    protected ImageView deleteView;         //删除按钮
    protected PopupWindow optionsPopup;               //选项 PopupWindow

    protected MorePopupAdapter morePopupAdapter;               //选项 PopupWindow数据适配器
    protected List<CommonItemListener> morePopupItemListeners;     //选项列表

    protected CommonItemListener currentPathConfirmListeners;

    protected CommonItemListener addFolderListener;

    protected CommonItemListener deleteListener;

    protected FontBean mainTitle;                     //主标题字样式
    protected FontBean subtitle;                      //副标题字样式
    protected boolean isDialogBuild;                   //是否是dialog模式

    protected TextView morePopupItemTv;

    public SelectStorageDialog getSelectStorageDialog() {
        return selectStorageDialog;
    }

    protected SelectStorageDialog selectStorageDialog;

    private Handler timeHandler = new Handler(Looper.getMainLooper());

    private Runnable refreshRunnable;
    @Override
    public int setFragmentViewId() {
        return R.layout.fragment_titlebar_mlh;
    }

    @Override
    public void getComponents(View view) {
        positionView = view.findViewById(R.id.view_position_titlebar);
        relParent = view.findViewById(R.id.rel_titlebar);
        backImgView = view.findViewById(R.id.imgv_back_titlebar);
        storageImgView = view.findViewById(R.id.imgv_storage_titlebar);

        searchImgView = view.findViewById(R.id.imgv_seach_titlebar);
        moreImgView = view.findViewById(R.id.imgv_more_options_titlebar);
        mainTitleTv = view.findViewById(R.id.tv_main_title_titlebar);
        subtitleTv = view.findViewById(R.id.tv_subtitle_titlebar);
        oneOptionTv = view.findViewById(R.id.tv_one_option_titlebar);
        currentPathView = view.findViewById(R.id.tv_current_path);
        addFolderView = view.findViewById(R.id.tv_add_folder);
        deleteView = view.findViewById(R.id.tv_delete);

    }

    @Override
    public void initData() {
        super.initData();
        mainTitle = mConfigDataReference.get().titlebarMainTitle;
        subtitle = mConfigDataReference.get().titlebarSubtitleTitle;

        //将监听回调列表转换为数组
        if (morePopupItemListeners == null) {
            morePopupItemListeners = new ArrayList<>();
            if (mConfigDataReference.get().morePopupItemListeners != null) {
                for (CommonItemListener listener : mConfigDataReference.get().morePopupItemListeners) {
                    morePopupItemListeners.add(listener);
                }
            }
        }
        if(currentPathConfirmListeners == null){
            currentPathConfirmListeners = mConfigDataReference.get().currentPathConfirmListener;
        }

        if(addFolderListener == null){
            addFolderListener = mConfigDataReference.get().addFolderListener;
        }
        if(deleteListener == null){
            deleteListener = mConfigDataReference.get().deleteListener;
        }

        if (mConfigDataReference.get().buildType == MConstants.BUILD_DIALOG) {
            isDialogBuild = true;
        }


    }
    @Override
    public void initView() {
        super.initView();
        relParent.setBackgroundColor(mConfigDataReference.get().titlebarBG);
        setViewSize();
        setTitleFont();
        setOptions();

    }

    protected void setViewSize() {

        if (isDialogBuild) {
            int icoSize = 65;

            relParent.getLayoutParams().height = 115;

            backImgView.getLayoutParams().height = icoSize;
            backImgView.getLayoutParams().width = icoSize;

            storageImgView.getLayoutParams().height = icoSize;
            storageImgView.getLayoutParams().width = icoSize;

            searchImgView.getLayoutParams().height = icoSize;
            searchImgView.getLayoutParams().width = icoSize;

            moreImgView.getLayoutParams().height = icoSize;
            moreImgView.getLayoutParams().width = icoSize;

        } else {

        }
    }

    protected void setOptions() {
        if (!mConfigDataReference.get().showSelectStorageBtn) {
            storageImgView.setVisibility(View.GONE);
        }

        if (morePopupItemListeners == null || morePopupItemListeners.size() == 0) {
            //没有选项
            moreImgView.setVisibility(View.GONE);
        } else if (morePopupItemListeners.size() == 1) {
            //一个选项
            moreImgView.setVisibility(View.GONE);
            oneOptionTv.setVisibility(View.GONE);
//            FontBean font = morePopupItemListeners.get(0).getFontBean();
//            oneOptionTv.setBackgroundResource(R.drawable.folder_plus);
//            oneOptionTv.setText(font.getText());
//            oneOptionTv.setTextColor(font.getColor());
//            oneOptionTv.setTextSize(font.getSize());

        }
        if(currentPathConfirmListeners !=null){
            currentPathView.setVisibility(View.VISIBLE);
        }
        if(addFolderListener !=null){
            addFolderView.setVisibility(View.VISIBLE);
        }
        else {
            //多个选项的字样式设置通过Adapter来设置
        }
    }

    @Override
    public void setDeleteVisible(boolean visible){
        if(deleteListener !=null && visible){
            deleteView.setVisibility(View.VISIBLE);
        }else if(deleteListener !=null)
            deleteView.setVisibility(View.GONE);
    }

    protected void setTitleFont() {
        if (mainTitle != null) {
            mainTitleTv.setText(mainTitle.getText());
            mainTitleTv.setTextColor(mainTitle.getColor());
            mainTitleTv.setTextSize(mainTitle.getSize());
        }
        if (subtitle != null) {
            subtitleTv.setText(subtitle.getText());
            subtitleTv.setTextColor(subtitle.getColor());
            subtitleTv.setTextSize(subtitle.getSize());
        }
    }

    @Override
    public void setMainTitle(FontBean font) {
        if (mainTitle != null) {
            mainTitleTv.setText(font.getText());
            mainTitleTv.setTextColor(font.getColor());
            mainTitleTv.setTextSize(font.getSize());
        }

    }


    @Override
    public void setListeners() {
        backImgView.setOnClickListener(this);
        storageImgView.setOnClickListener(this);
        searchImgView.setOnClickListener(this);
        moreImgView.setOnClickListener(this);
        oneOptionTv.setOnClickListener(this);
        currentPathView.setOnClickListener(this);
        addFolderView.setOnClickListener(this);
        deleteView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imgv_back_titlebar) {

            //返回按钮
            if (mConfigDataReference.get().buildType == MConstants.BUILD_DIALOG) {
                mConfigDataReference.get().buildController.getDialogFragment().dismissAllowingStateLoss();
            } else {
                mActivity.finish();
            }

        } else if (id == R.id.imgv_more_options_titlebar) {
            //更多按钮
            showPopupWindow();
        } else if (id == R.id.imgv_storage_titlebar) {
            //内存卡按钮
            showSelectStorageDialog();

        } else if (id == R.id.tv_one_option_titlebar) {
            //一个选项按钮
            optionItemClick(v,  v, 0);
        } else if (id == R.id.imgv_seach_titlebar) {
            //搜索按钮

        }else if(id == R.id.tv_current_path){
            currentPathClick(v,  v, 0);
        }
        else if(id == R.id.tv_add_folder){
            addFolderClick(v,  v, 0);
        }
        else if(id == R.id.tv_delete){
            deleteClick(v,  v, 0);
        }

    }

    public void showSelectStorageDialog() {
        if (selectStorageDialog == null) {
            selectStorageDialog = new SelectStorageDialog(mActivity);
        }
        //通过设置定时任务更新Usb设备列表，更改为广播，弃用。
//        selectStorageDialog.setOnShowListener(selectStorageDialog ->{
//            Log.d("设置定时任务", "设置定时任务成功: ");
//            timeHandler.post(refreshRunnable);
//        });
//        selectStorageDialog.setOnDismissListener(selectStorageDialog ->{
//            timeHandler.removeCallbacks(refreshRunnable);
//        });
//        refreshRunnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("Refresh", "run: 定时任务");
//                selectStorageDialog.onItemChanged();
//                timeHandler.postDelayed(this,1000);
//            }
//        };
        selectStorageDialog.show();

    }

    // TODO 版本处理
    @SuppressWarnings("all")
    protected void showPopupWindow() {
        if (optionsPopup == null) {
            View popView = LayoutInflater.from(mActivity).inflate(R.layout.general_recyview_mlh, null);//加载布局文件
            optionsPopup = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//设置宽度高度
            optionsPopup.setFocusable(true);
            optionsPopup.setOutsideTouchable(true);
            optionsPopup.setElevation(3);//设置阴影 (注意阴影穿透---父组件和子组件必须都设置阴影)
            RecyclerView recyclerView = popView.findViewById(R.id.general_recyclerview_mlh);
            recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

            morePopupAdapter = new MorePopupAdapter(R.layout.general_item_tv_mlh, morePopupItemListeners);
            recyclerView.setAdapter(morePopupAdapter);
            morePopupAdapter.setOnItemClickListener(this);//设置监听
            morePopupAdapter.setOnItemLongClickListener(this);//设置监听
        }

        optionsPopup.showAsDropDown(positionView, 0, 0, Gravity.RIGHT);//显示位置

    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int i) {
        if (adapter instanceof MorePopupAdapter) {
            optionsPopup.dismiss();
            morePopupItemTv = v.findViewById(R.id.general_item_textview_mlh);
            optionItemClick(v, mainTitleTv, i);
        }
    }

    /**
     * 点击option回调
     *
     * @param v 点击的视图
     * @param i 点击的索引
     */
    protected void optionItemClick(View v, View tv, int i) {
        morePopupItemListeners.get(i).onClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf
        );
    }

    protected void currentPathClick(View v, View tv, int i){
        currentPathConfirmListeners.onClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf);
    }

    protected void addFolderClick(View v, View tv, int i){
        addFolderListener.onClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf);
    }

    protected void deleteClick(View v, View tv, int i){
        deleteListener.onClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf);
    }

    @Override
    public boolean onItemLongClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        if (adapter instanceof MorePopupAdapter) {
            morePopupItemTv = v.findViewById(R.id.general_item_textview_mlh);
            return optionItemLongClick(v, morePopupItemTv, position);
        }

        return false;
    }

    /**
     * 长按option回调
     *
     * @param v 点击的视图
     * @param i 点击的索引
     */
    protected boolean optionItemLongClick(View v, TextView tv, int i) {
        return morePopupItemListeners.get(i).onLongClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf
        );
    }

    @Override
    public MorePopupAdapter getMorePopupAdapter() {
        return morePopupAdapter;
    }

    @Override
    public List<CommonItemListener> getMorePopupItemListeners() {
        return morePopupItemListeners;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void refreshMorePopup() {
        morePopupAdapter.notifyDataSetChanged();
    }

    @Override
    public TextView getOnlyOneMorePopupTextView() {
        return this.oneOptionTv;
    }




}
