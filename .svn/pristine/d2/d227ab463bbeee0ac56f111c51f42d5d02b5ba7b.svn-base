package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

public class MainAdapterCenterSerialsWordI2c extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<SerialBusTxtStruct.I2cStruct> list;
    private boolean showMs = true;
    private int formHeightDetail;

    public MainAdapterCenterSerialsWordI2c(Context context, ArrayList<SerialBusTxtStruct.I2cStruct> list) {
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
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_i2c, parent, false));
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
        private SerialsWordI2cSingleRowTextView i2cTextView;

        public Holder(View itemView) {
            super(itemView);
            i2cTextView = (SerialsWordI2cSingleRowTextView) itemView.findViewById(R.id.bean);
        }

        public void bind(SerialBusTxtStruct.I2cStruct bean) {
            ViewGroup.LayoutParams layoutParams = i2cTextView.getLayoutParams();
            layoutParams.height = formHeightDetail * ((bean.Data.trim().length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_I2C + 1);
            i2cTextView.setLayoutParams(layoutParams);
            i2cTextView.setData(bean, showMs);
        }
    }
}
