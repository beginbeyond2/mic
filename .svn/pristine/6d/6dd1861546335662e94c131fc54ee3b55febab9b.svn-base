package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

public class MainAdapterCenterSerialsWordCan extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<SerialBusTxtStruct.CanStruct> list;
    private boolean showMs = true;
    private int formHeightDetail;

    public MainAdapterCenterSerialsWordCan(Context context, ArrayList<SerialBusTxtStruct.CanStruct> list) {
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
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_can, parent, false));
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
        private SerialsWordCanSingleRowTextView canTextView;

        public Holder(View itemView) {
            super(itemView);
            canTextView = (SerialsWordCanSingleRowTextView) itemView.findViewById(R.id.bean);
        }


        public void bind(SerialBusTxtStruct.CanStruct bean) {
            ViewGroup.LayoutParams layoutParams = canTextView.getLayoutParams();
            layoutParams.height = formHeightDetail * ((bean.Data.trim().length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_CAN + 1);
            canTextView.setLayoutParams(layoutParams);
            canTextView.setData(bean, showMs);
        }
    }
}
