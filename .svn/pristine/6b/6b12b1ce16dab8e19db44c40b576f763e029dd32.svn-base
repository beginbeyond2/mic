package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;

/**
 * @auother Liwb
 * @description:
 * @data:2025-8-5 16:44
 */
public class DialogManage {
    private static DialogManage instance=new DialogManage();
    public static DialogManage getIns(){return instance;}

    private Context context;
    private DialogOk dialogOk;
    private DialogOkCancel dialogOkCancel;

    public void init(Context context)
    {
        this.context = context;
        dialogOk= (DialogOk) ((MainActivity) context).findViewById(R.id.dialogOk);
        dialogOkCancel= (DialogOkCancel) ((MainActivity) context).findViewById(R.id.dialogOkCancel);
    }

    public DialogOk getDialogOk(){
        return dialogOk;
    }
    public DialogOkCancel getDialogOkCancel(){
        return dialogOkCancel;
    }
}
