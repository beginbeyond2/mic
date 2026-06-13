package com.micsig.tbook.tbookscope.main.maincenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.List;

/**
 * Created by yangj on 2017/12/15.
 */

public class MainAdapterCenterTimeBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<TimeBaseScale> list;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(TimeBaseScale scale);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public MainAdapterCenterTimeBase(Context context, List<TimeBaseScale> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_maincenter_timebase, parent, false));
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
        TextView textView;

        public Holder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tm);
        }

        public void bind(final TimeBaseScale s) {
            textView.setText(s.getScale());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(s);
                    }
                }
            });
        }
    }
}
