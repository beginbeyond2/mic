package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;

import java.util.ArrayList;

public class MainAdapterCenterSerialsWordUart extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>> list;
    private String showType = SerialsWordUartSingleRowTextView.TYPE_HEX_OTHER;
    private int chType = ISerialsWord.TYPE_S1;
    private int bits,check;

    public MainAdapterCenterSerialsWordUart(Context context, ArrayList<ArrayList<SerialBusTxtStruct.UartStruct>> list) {
        this.context = context;
        this.list = list;
    }

    public void setBits(int bits){
        this.bits=bits;
    }
    public void setCheck(int check){
        this.check=check;
    }
    public void setListType(String showType) {
        this.showType = showType;
    }

    public void setChType(int chType) {
        this.chType = chType;
    }

    public String getShowType() {
        return showType;
    }

    public int getChType() {
        return chType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_serialsword_uart, parent, false));
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
        private SerialsWordUartSingleRowTextView textView;

        public Holder(View itemView) {
            super(itemView);
            textView = (SerialsWordUartSingleRowTextView) itemView.findViewById(R.id.text);
        }


        public void bind(ArrayList<SerialBusTxtStruct.UartStruct> bean) {
            textView.setList(bits,check,showType, chType, bean);
        }
    }
}
