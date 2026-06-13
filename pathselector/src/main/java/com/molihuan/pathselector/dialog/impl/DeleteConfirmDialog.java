package com.molihuan.pathselector.dialog.impl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.molihuan.pathselector.R;
import com.molihuan.pathselector.dialog.BaseDialog;
import com.molihuan.pathselector.entity.FileBean;

public class DeleteConfirmDialog extends BaseDialog {

    private TextView content;
    private Button deleteButton,cancleButton;


    private OnDeleteListener listener;

    private FileBean fileBean;

    public DeleteConfirmDialog(@NonNull Context context,FileBean fileBean, OnDeleteListener listener) {
        super(context);
        this.fileBean = fileBean;
        this.listener = listener;
    }

    @Override
    public int setContentViewID() {
        return R.layout.dialog_delete_confirm;
    }

    @Override
    public void getComponents() {
        content = findViewById(R.id.dialog_delect_confirm_content);
    }

    @Override
    public void initView(){
        setCanceledOnTouchOutside(true);
        getWindow().setLayout(mConfigData.pathSelectDialogWidth * 92 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(fileBean.isDir()){
            content.setText(R.string.delete_folder);
        }else {
            content.setText(R.string.delete_file);
        }
        deleteButton = findViewById(R.id.btn_delete);
        cancleButton = findViewById(R.id.btn_cancel);
        deleteButton.setOnClickListener( v->{
            if(listener!=null){
                listener.onDelete(fileBean);
            }
            dismiss();
        });
        int postion = 0;
        cancleButton.setOnClickListener(v->{
            if(listener!=null){
                listener.onCancel(postion);
            }
            dismiss();
        });

    }

    public interface OnDeleteListener{
        void onDelete(FileBean fileBean);
        void onCancel(int position);
    }
}
