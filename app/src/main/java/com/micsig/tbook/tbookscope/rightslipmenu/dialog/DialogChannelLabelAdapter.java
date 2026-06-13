package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.util.BitmapUtil;

import java.util.ArrayList;


public class DialogChannelLabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<RightBeanSelect> list;
    private OnItemClickListener onItemClickListener;
    private ColorStateList itemTextColorResId;

    private int chIdx;

    interface OnItemClickListener {
        void onItemClick(View itemView, RightBeanSelect item);
    }

    public DialogChannelLabelAdapter(Context context, ArrayList<RightBeanSelect> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_right_channellabel, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    @SuppressLint("ResourceType")
    public void setControlColorByChIdx(int chIdx){
        this.chIdx=chIdx;
    }

    class Holder extends RecyclerView.ViewHolder {
        RadioButton textView;

        public Holder(View itemView) {
            super(itemView);
            textView = (RadioButton) itemView.findViewById(R.id.textView);
        }

        private void bind(final int position) {
            if (position == 1) {
                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                textView.setPadding(4, 0, 4, 0);
//                StateListDrawable d=BitmapUtil.genSelectorDrawable(context,chIdx);
//                textView.setBackground(d);
                textView.setBackground(null);
            } else {
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(0, 0, 0, 0);
                StateListDrawable d=BitmapUtil.genSelectorDrawable(context,chIdx);
                textView.setBackground(d);
            }

            itemTextColorResId=BitmapUtil.genSelectorColor(context,chIdx);
            textView.setTextColor(itemTextColorResId);
            textView.setText(list.get(position).getText());
            textView.setChecked(list.get(position).isCheck());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, list.get(position));
                    }
                }
            });
        }
    }
}
