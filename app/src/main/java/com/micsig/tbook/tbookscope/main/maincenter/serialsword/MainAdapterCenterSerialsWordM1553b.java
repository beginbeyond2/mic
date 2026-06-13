package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

public class MainAdapterCenterSerialsWordM1553b extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<SerialBusTxtStruct.MilSTD1553bStruct> list;
    private boolean showMs = true;
    private int formHeightDetail;

    public MainAdapterCenterSerialsWordM1553b(Context context, ArrayList<SerialBusTxtStruct.MilSTD1553bStruct> list) {
        this.context = context;
        this.list = list;
        formHeightDetail = (int) context.getResources().getDimension(R.dimen.formHeightDetail);
    }

    public void setShowMs(boolean showMs) {
        this.showMs = showMs;
    }

    public boolean isShowMs() {
        return showMs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_m1553b, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private SerialsWordM1553bSingleRowTextView m1553bTextView;

        public Holder(View itemView) {
            super(itemView);
            m1553bTextView = (SerialsWordM1553bSingleRowTextView) itemView.findViewById(R.id.bean);
        }


        public void bind(SerialBusTxtStruct.MilSTD1553bStruct bean) {
            ViewGroup.LayoutParams layoutParams = m1553bTextView.getLayoutParams();
            layoutParams.height = formHeightDetail * ((bean.Data.trim().length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_1553B + 1);
            m1553bTextView.setLayoutParams(layoutParams);
            m1553bTextView.setData(bean, showMs);
        }
    }
}
