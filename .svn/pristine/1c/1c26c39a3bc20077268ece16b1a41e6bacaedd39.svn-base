package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/5/4.
 */

public class DialogBaudRate extends AbsoluteLayout {
    private Context context;
    private RightViewSelect baudRate;
    private RightViewUserDefineEdit defineEdit;
    private TopDialogNumberKeyBoard dialogKeyBoard;
    private double preDouble;
    private String preBs;
    private OnDismissListener onDismissListener;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public DialogBaudRate(Context context) {
        this(context, null);
    }

    public DialogBaudRate(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogBaudRate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[366, 366]	335	170
    private void init() {
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_baudrate, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });
        initView(rootViewGroup);
        hide();
    }

    private void initView(View view) {
        defineEdit = (RightViewUserDefineEdit) view.findViewById(R.id.defineEdit);
        baudRate = (RightViewSelect) view.findViewById(R.id.baudRate);
        baudRate.setDoubleCheckEqual(true);
        baudRate.setOnItemClick2Listener(onItemClick2Listener);
        defineEdit.setOnEditClickListener(onEditClickListener);
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BAUDRATE);
        Tools.PrintControlsLocation("DialogBaudRate",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BAUDRATE);
    }

    public void setData(int chIdx,String preString, OnDismissListener onDismissListener) {
        baudRate.setControlColorByChIdx(chIdx);
        if (!StrUtil.isEmpty(preString)) {
            String number = preString.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
            preDouble = Double.parseDouble(number);
            preBs = preString.replace(number, "");
        } else {
            preDouble = 0;
            preBs = TopDialogNumberKeyBoard.KEYBOARD_KBS;
        }
        this.onDismissListener = onDismissListener;
        boolean flag = false;
        for (RightBeanSelect item : baudRate.getList()) {
            if (item.getText().equals(preString)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            baudRate.setPreString(preString);
            defineEdit.setText("");
        } else {
            baudRate.clearSelect();
            defineEdit.setText(preString);
        }

        show();
    }

    private void onResult(String result) {
        hide();
        if (onDismissListener != null) {
            onDismissListener.onDismiss(result);
        }
    }

    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            baudRate.clearSelect();
            if (dialogKeyBoard == null) {
                dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
            }
            dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onKeyBoardDismissListener);
        }
    };

    private RightViewSelect.OnItemClick2Listener onItemClick2Listener = new RightViewSelect.OnItemClick2Listener() {
        @Override
        public void onItemClick2(int parentsViewId, View itemView, RightBeanSelect item) {
            PlaySound.getInstance().playButton();
            if (parentsViewId == baudRate.getId()) {
                if (!item.isUserDefine(context)) {
                    onResult(item.getText());
                } else {
                    if (dialogKeyBoard == null) {
                        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);
                    }
                    dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onKeyBoardDismissListener);
                }
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onKeyBoardDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            String unit = "";
            if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_MBS)) {
                unit = TopDialogNumberKeyBoard.KEYBOARD_MBS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "");
            } else if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_KBS)) {
                unit = TopDialogNumberKeyBoard.KEYBOARD_KBS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "");
            } else {
                unit = TopDialogNumberKeyBoard.KEYBOARD_BS;
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");
            }

            if (result.length() > 4 && result.substring(0, 4).contains(".")) {
                result = result.substring(0, 5);
            } else if (result.length() > 4) {
                result = result.substring(0, 4);
            }

            onResult(result + unit);
        }
    };
}
