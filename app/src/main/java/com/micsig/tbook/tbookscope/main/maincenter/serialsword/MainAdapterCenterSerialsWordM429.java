package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

public class MainAdapterCenterSerialsWordM429 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<SerialBusTxtStruct.Arinc429Struct> list;
    private boolean showMs = true;
    private int formHeightDetail;

    public MainAdapterCenterSerialsWordM429(Context context, ArrayList<SerialBusTxtStruct.Arinc429Struct> list) {
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
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_m429, parent, false));
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
        private SerialsWordM429SingleRowTextView m429TextView;

        public Holder(View itemView) {
            super(itemView);
            m429TextView = (SerialsWordM429SingleRowTextView) itemView.findViewById(R.id.bean);
        }

        public void bind(SerialBusTxtStruct.Arinc429Struct bean) {
            ViewGroup.LayoutParams layoutParams = m429TextView.getLayoutParams();
            layoutParams.height = formHeightDetail * ((bean.Data.trim().length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_429 + 1);
            m429TextView.setLayoutParams(layoutParams);
            m429TextView.setData(bean, showMs);
        }
    }
}
