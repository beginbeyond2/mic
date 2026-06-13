package com.micsig.tbook.ui.top.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.FileBeanToStr;
import com.molihuan.pathselector.entity.FileBean;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TopViewSpinner extends LinearLayout {

    private Context context;
    private int headWidth, spinnerWidth;
    private String titleString;
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayList<FileBean> beanList = new ArrayList<>();
    private onItemSelectListener itemSelectListener;
    private ArrayAdapter<String> adapterForSpinner;
    private int selectPosition = AdapterView.INVALID_POSITION;
    private TextView headTxtView;
    private Spinner spinner;
    private int itemLayoutId = -1;

    public interface onItemSelectListener {
        void onItemSelected(FileBean str);
    }

    public TopViewSpinner(Context context) {
        this(context, null);
    }

    public TopViewSpinner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.view_top_spinner, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSpinner);
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSpinner_headWidth, 100);
        spinnerWidth = ta.getDimensionPixelSize(R.styleable.TopViewSpinner_editWidth, 200);
        ta.recycle();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        headTxtView = findViewById(R.id.headView);
        spinner = findViewById(R.id.action_spinner);
        updateView();
    }

    public void setData(String title, ArrayList<FileBean> dataList, int itemLayoutId, onItemSelectListener listener) {
        this.titleString = title;
        this.beanList = dataList;
        this.dataList = FileBeanToStr.getDisPlayStrList(dataList);
        this.itemSelectListener = listener;
        this.itemLayoutId = itemLayoutId;
        updateView();
    }

    public void updateDataList(ArrayList<FileBean> dataList, FileBean selectStr) {
        this.dataList = FileBeanToStr.getDisPlayStrList(dataList);
        this.beanList = dataList;
        adapterForSpinner.clear();
        adapterForSpinner.addAll(this.dataList);
        adapterForSpinner.notifyDataSetChanged();
        if ((selectStr == null) || (!beanList.contains(selectStr))) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(this.dataList.indexOf(selectStr.getPath()));
        }
    }

    private void updateView() {
        headTxtView.setText(titleString);
        LinearLayout.LayoutParams headParams = (LayoutParams) headTxtView.getLayoutParams();
        headParams.width = headWidth;
        headTxtView.setLayoutParams(headParams);

        LinearLayout.LayoutParams spinnerParams = (LayoutParams) spinner.getLayoutParams();
        spinnerParams.width = spinnerWidth;
        spinner.setLayoutParams(spinnerParams);


//        adapterForSpinner = new ArrayAdapter<>(context, R.layout.layout_item_for_save_directory, dataList);
        adapterForSpinner = new ArrayAdapter<>(context, itemLayoutId, dataList);
        spinner.setAdapter(adapterForSpinner);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

    }

    private final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //选中的保存目录
            selectPosition = position;
            if (itemSelectListener != null) {
//                itemSelectListener.onItemSelected(adapterForSpinner.getItem(position));
                itemSelectListener.onItemSelected(beanList.get(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

//    public String getSelect() {
//        return adapterForSpinner.getItem(selectPosition);
//    }


    public int getSelectPosition() {
        return spinner.getSelectedItemPosition();
    }

    public String getSelectItem() {
        int pos = spinner.getSelectedItemPosition();
        if (pos != AdapterView.INVALID_POSITION) {
            if (spinner.getSelectedItem() == null) {
                return "";
            }
            return beanList.get(pos).getPath();
        } else {
            if (beanList.size() > 0) {
                return beanList.get(0).getPath();
            } else {
                return "";
            }
        }
    }

    public String getDisPlaySelectItem() {
        int pos = spinner.getSelectedItemPosition();
        if (pos != AdapterView.INVALID_POSITION) {
            if (spinner.getSelectedItem() == null) {
                return "";
            }
            return beanList.get(pos).getDisplayName();
        } else {
            if (beanList.size() > 0) {
                return beanList.get(0).getDisplayName();
            } else {
                return "";
            }
        }
    }


    public Spinner getSpinner() {
        return spinner;
    }


    @SuppressLint("ResourceType")
    public void setReadOnly(boolean enabled) {
        super.setEnabled(enabled);
        TextView selectedView = (TextView) spinner.getSelectedView();
        if(!enabled){
            spinner.setEnabled(false);
            if(selectedView!=null){
                selectedView.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewDisable));
            }
        }else {
            selectedView.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.colorChCommon));
            spinner.setEnabled(true);
        }
    }
}
