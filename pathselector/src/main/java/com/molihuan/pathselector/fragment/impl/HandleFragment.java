package com.molihuan.pathselector.fragment.impl;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.molihuan.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.adapter.HandleListAdapter;
import com.molihuan.pathselector.entity.FontBean;
import com.molihuan.pathselector.fragment.AbstractHandleFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.utils.MConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: HandleFragment
 * @Author: molihuan
 * @Date: 2022/11/22/18:19
 * @Description:
 */
public class HandleFragment extends AbstractHandleFragment implements OnItemClickListener, OnItemLongClickListener {

    protected RecyclerView mRecView;
    protected TextView handleItemTv;

    protected List<CommonItemListener> handleItemListeners;     //选项列表
    protected HandleListAdapter handleListAdapter;     //选项适配器

    protected FontBean fontBean;                      //字样式
    protected boolean isDialogBuild;                   //是否是dialog模式

    @Override
    public int setFragmentViewId() {
        return R.layout.fragment_handle_mlh;
    }

    @Override
    public void getComponents(View view) {
        mRecView = view.findViewById(R.id.recv_handle);
    }


    @Override
    public void initData() {
        super.initData();

        //将监听回调列表转换为数组
        if (handleItemListeners == null) {
            handleItemListeners = new ArrayList<>();
            fontBean = mConfigDataReference.get().handleItemListeners[0].getFontBean();//只需要一份样式
            if (mConfigDataReference.get().handleItemListeners != null) {
                for (CommonItemListener listener : mConfigDataReference.get().handleItemListeners) {
                    handleItemListeners.add(listener);
                }
            }

        }

        if (mConfigDataReference.get().buildType == MConstants.BUILD_DIALOG) {
            isDialogBuild = true;
        }


    }


    @Override
    public void initView() {
        //通过回调的方法获取mRecView宽度并设置其item宽度并设置数据适配器
        SizeUtils.forceGetViewSize(mRecView, new SizeUtils.OnGetSizeListener() {
            @Override
            public void onGetSize(View view) {
                //计算 mRecView item宽度
                int width = view.getMeasuredWidth() / handleItemListeners.size()+1;
                //设置适配器
                mRecView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
//                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecView.getContext(),LinearLayoutManager.HORIZONTAL);
                HorizontalSpaceItemDecoration itemDecoration = new HorizontalSpaceItemDecoration(30);

                mRecView.addItemDecoration(itemDecoration);
                //TODO  Arrays.asList 返回的类型不是 java.util.ArrayList 而是 java.util.Arrays.ArrayList 返回的 ArrayList 对象是只读的
                handleListAdapter = new HandleListAdapter(R.layout.item_handle_mlh, handleItemListeners,width);
                mRecView.setAdapter(handleListAdapter);
                handleListAdapter.setOnItemClickListener(HandleFragment.this);
                handleListAdapter.setOnItemLongClickListener(HandleFragment.this);
            }
        });

    }

    @Override
    public void setListeners() {

    }

    @Override
    public List<CommonItemListener> getHandleItemListeners() {
        return handleItemListeners;
    }

    @Override
    public HandleListAdapter getHandleListAdapter() {
        return handleListAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void refreshHandleList() {
        handleListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        if (adapter instanceof HandleListAdapter) {
            handleItemTv = v.findViewById(R.id.item_handle_tv_mlh);
            optionItemClick(v, handleItemTv, position);
        }
    }

    /**
     * 点击option回调
     *
     * @param v 点击的视图
     * @param i 点击的索引
     */
    protected void optionItemClick(View v, TextView tv, int i) {
        handleItemListeners.get(i).onClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf
        );
    }


    @Override
    public boolean onItemLongClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View v, int position) {
        if (adapter instanceof HandleListAdapter) {
            handleItemTv = v.findViewById(R.id.item_handle_tv_mlh);
            return optionItemLongClick(v, handleItemTv, position);
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
        return handleItemListeners.get(i).onLongClick(v,
                tv,
                psf.getSelectedFileList(),
                psf.getCurrentPath(),
                psf
        );
    }
    class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration{
        private final int space;


        HorizontalSpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
            int position = parent.getChildAdapterPosition(view);
            int itemCount = state.getItemCount();
            //给Item右侧添加间距，最后的不添加
            if(position<itemCount -1){
                outRect.right = space;
            }
//            outRect.left = space;
//            outRect.right = space;
        }
    }
}
