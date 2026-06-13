package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.Screen;

import java.util.List;

/**
 * Created by yangj on 2017/4/27.
 */

public class SerialsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Serials> serialsList;
    private Context context;
    private OnSerialsItemClickListener onItemClickListener;

    public SerialsAdapter(Context context, List<Serials> serialsList) {
        this.serialsList = serialsList;
        this.context = context;
    }

    interface OnSerialsItemClickListener {
        void itemClick(List<Serials> serialsList, Serials serials);
    }

    public void setOnItemClickListener(OnSerialsItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_triggerserials, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(serialsList.get(position));
    }

    public Serials getSelected() {
        for (Serials serials : serialsList) {
            if (serials.isEnabled() && serials.isSelected()) {
                return serials;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return serialsList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView text;

        public Holder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        void bind(final Serials serials) {
            text.setText(serials.getName());
            text.setEnabled(serials.isEnabled());
            text.setSelected(serials.isSelected());
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < serialsList.size(); i++) {
                        Serials item = serialsList.get(i);
                        if (item.getId() == serials.getId()) {
                            item.setSelected(true);
                        } else {
                            item.setSelected(false);
                        }
                    }
                    if (onItemClickListener != null) {
                        Screen.getViewLocation(text);
                        onItemClickListener.itemClick(serialsList, serials);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}
