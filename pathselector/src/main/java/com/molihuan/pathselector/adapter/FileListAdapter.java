package com.molihuan.pathselector.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import androidx.core.content.ContextCompat;

import com.blankj.molihuan.utilcode.util.StringUtils;
import com.blankj.molihuan.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.daimajia.swipe.SwipeLayout;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.dialog.impl.DeleteConfirmDialog;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.fragment.AbstractFileShowFragment;
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl;
import com.molihuan.pathselector.utils.MConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @ClassName: FileListAdapter
 * @Author: molihuan
 * @Date: 2022/11/22/21:55
 * @Description:
 */
public class FileListAdapter extends BaseQuickAdapter<FileBean, BaseViewHolder> implements LoadMoreModule {
//    protected SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData();

    protected final Context context;
    private AbstractFileShowFragment fileShowFragment = ConfigDataBuilderImpl.getInstance().getSelectConfigData().fileShowFragment;

    public FileListAdapter(int layoutResId, @Nullable List<FileBean> data, Context context) {
        super(layoutResId, data);
        this.context = context;
    }
    public SwipeLayout openLayout = null;
    @SuppressLint("SetTextI18n")
    @Override
    protected void convert(@NonNull BaseViewHolder holder, FileBean fileBean) {

        SwipeLayout swipeLayout = holder.getView(R.id.swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.setDragDistance(500);
        swipeLayout.setClickToClose(true);
        if(fileShowFragment.isMultipleSelectionMode()){
            swipeLayout.setSwipeEnabled(false);
        }else{
            swipeLayout.setSwipeEnabled(true);
        }
        ImageView tvDelete = holder.getView(R.id.tv_delete);
        tvDelete.setOnClickListener(v -> {
            DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,fileBean, (DeleteConfirmDialog.OnDeleteListener) fileShowFragment);
            deleteConfirmDialog.show();
        });

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                holder.findView(R.id.swipe_layout).setClickable(false);
                holder.findView(R.id.swipe_layout).setLongClickable(false);
                if(openLayout !=null && openLayout != swipeLayout){
                    openLayout.close();
                }
                openLayout = swipeLayout;
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                holder.findView(R.id.swipe_layout).setClickable(false);
                holder.findView(R.id.swipe_layout).setLongClickable(false);

            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                holder.findView(R.id.swipe_layout).setClickable(false);
                holder.findView(R.id.swipe_layout).setLongClickable(false);
            }

            @Override
            public void onClose(SwipeLayout layout) {
                holder.findView(R.id.swipe_layout).setClickable(true);
                holder.findView(R.id.swipe_layout).setLongClickable(true);
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                holder.findView(R.id.swipe_layout).setClickable(false);
                holder.findView(R.id.swipe_layout).setLongClickable(false);
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
        LinearLayout linlContainer = holder.getView(R.id.linl_item_file_container);//总容器
        ImageView imgvIco = holder.getView(R.id.imgv_item_file_ico);//文件图标
        ImageView imgvEnter = holder.getView(R.id.imgv_item_file_enter);//右边进入图片
        TextView tvName = holder.getView(R.id.tv_item_file_name);//文件名称
        TextView tvDetail = holder.getView(R.id.tv_item_file_detail);//文件详细信息
//        CheckBox checkBox = holder.getView(R.id.checkbox_item_file_choose);//多选框
        ImageView checkBox = holder.getView(R.id.checkbox_item_file_choose);
        if (fileBean.getPath() == null) {
            //说明是缓存filebean
            linlContainer.setVisibility(View.GONE);//隐藏总容器
            return;
        } else if (fileBean.getSize() == MConstants.FILEBEAN_BACK_FLAG) {
            linlContainer.setVisibility(View.VISIBLE);
            //说明是返回fileBean
            imgvIco.setImageResource(R.drawable.ic_titlebar_back_mlh);
            imgvEnter.setVisibility(View.INVISIBLE);
            tvName.setText(fileBean.getName());
            tvName.setTextColor(ContextCompat.getColor(context,R.color.folder_text_color));
            tvDetail.setText("");
            checkBox.setVisibility(View.INVISIBLE);
//            checkBox.setChecked(false);
        } else {
            linlContainer.setVisibility(View.VISIBLE);
            //如果是文件夹且当前不是多选模式则设置可以进入的图标
//            imgvIco.setImageResource(fileBean.getFileIcoType());
            //文件夹取消detail，只有文件显示
            if (fileBean.isDir() && !fileShowFragment.isMultipleSelectionMode()) {
                imgvEnter.setVisibility(View.INVISIBLE);
                imgvIco.setImageResource(R.drawable.icon_folder_close);
                tvDetail.setVisibility(View.GONE);
            } else {
                //如果不是文件夹则设置成原来的图标
                imgvEnter.setVisibility(View.INVISIBLE);
                imgvIco.setImageResource(fileBean.getFileIcoType());
                tvDetail.setVisibility(View.VISIBLE);
            }
            //如果CheckBox需要显示则显示
//            checkBox.setChecked(fileBean.getBoxChecked());
            //如果被选中之后，还有判断是不是文件夹，是文件夹的话就更改图标，如果不是文件夹就只更新checkBox和tvName，其他不用动
            if(fileBean.getBoxChecked()){
                checkBox.setVisibility(View.VISIBLE);
                tvName.setTextColor(ContextCompat.getColor(context, R.color.select_bule));
                tvDetail.setVisibility(View.VISIBLE);
                if(fileBean.isDir()){
                    imgvIco.setImageResource(R.drawable.icon_folder_open);
                    tvDetail.setVisibility(View.GONE);
                }
            } else{
                checkBox.setVisibility(View.GONE);
                tvName.setTextColor(ContextCompat.getColor(context,R.color.folder_text_color));
                if(fileBean.isDir()){
                    imgvIco.setImageResource(R.drawable.icon_folder_close);
                    tvDetail.setVisibility(View.GONE);
                }
            }
            tvName.setText(fileBean.getName());
            //文件文件夹时间----大小时间
            if (fileBean.isDir()) {
                String dirDetail = String.format(StringUtils.getString(R.string.filebeanitem_dir_detail_mlh), fileBean.getChildrenDirNumber(), fileBean.getChildrenFileNumber());
                tvDetail.setText(dirDetail);
            } else {
                tvDetail.setText(TimeUtils.millis2String(fileBean.getModifyTime(), "yy-MM-dd HH:mm  ") + fileBean.getSizeString());
            }


        }

    }
}
