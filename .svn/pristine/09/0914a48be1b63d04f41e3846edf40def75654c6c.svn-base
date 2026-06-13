package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.ArrayList;


public class SelectColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final ArrayList<String> colorList;
    private final OnItemClickListener onItemClickListener;
    interface OnItemClickListener {
        void onItemClick(View itemView, String item);
    }

    public SelectColorAdapter(Context context, ArrayList<String> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.colorList = list;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_select_color, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        Button colorBtn;

        public Holder(View itemView) {
            super(itemView);
            colorBtn = (Button) itemView.findViewById(R.id.selectColor);
        }

        private void bind(final int position) {
            colorBtn.setBackgroundColor(Color.parseColor(colorList.get(position)));
            colorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, colorList.get(position));
                    }
                }
            });
        }
    }
}
