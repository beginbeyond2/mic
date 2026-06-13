package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/5/3.
 */

public class DialogRefRecallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<DialogRefRecallBean> list;
    private OnItemClickListener onItemClickListener;

    public void setList(ArrayList<DialogRefRecallBean> list) {
        this.list = list;
    }

    interface OnItemClickListener {
        void onItemClick(DialogRefRecallBean item);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public DialogRefRecallAdapter(Context context, ArrayList<DialogRefRecallBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_dialogrefrecall, parent, false));
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
        TextView title;
        TextView time;
        View dividerTop, dividerBottom, dividerLeft, dividerRight;

        public Holder(View itemView) {
            super(itemView);
            itemView.setBackgroundColor(context.getResources().getColor(R.color.bg_slip_backcolor));
            title = (TextView) itemView.findViewById(R.id.title);
            time = (TextView) itemView.findViewById(R.id.time);
            dividerTop = itemView.findViewById(R.id.dividerTop);
            dividerBottom = itemView.findViewById(R.id.dividerBottom);
            dividerLeft = itemView.findViewById(R.id.dividerLeft);
            dividerRight = itemView.findViewById(R.id.dividerRight);
        }

        public void bind(final DialogRefRecallBean item) {
            title.setText(item.getTitle());
            time.setText(item.getTime());
            if (item.isSelect()) {
                dividerTop.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref));
                dividerBottom.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref));
                dividerLeft.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref));
                dividerRight.setBackgroundColor(context.getResources().getColor(R.color.color_btn_ref));
            } else {
                dividerTop.setBackground(null);
                dividerBottom.setBackground(null);
                dividerLeft.setBackground(null);
                dividerRight.setBackground(null);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(item);
                    }
                }
            });
        }
    }
}
