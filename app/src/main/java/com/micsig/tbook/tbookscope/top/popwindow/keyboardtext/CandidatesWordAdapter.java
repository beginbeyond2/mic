package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.List;

/**
 * Created by yangj on 2017/12/6.
 */

class CandidatesWordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<KeyBoardCandidatesItem> list;
    private OnCandidatesWordListener onCandidatesWordListener;

    interface OnCandidatesWordListener {
        void onCandidateWord(KeyBoardCandidatesItem item);
    }

    public void setOnCandidatesWordListener(OnCandidatesWordListener onCandidatesWordListener) {
        this.onCandidatesWordListener = onCandidatesWordListener;
    }

    public CandidatesWordAdapter(Context context, List<KeyBoardCandidatesItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_canditatesword, parent, false));
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

        Holder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

        void bind(final KeyBoardCandidatesItem item) {
            textView.setText(item.getText());
            if (item.isSelect()) {
                textView.setBackgroundColor(context.getResources().getColor(R.color.bgNewTopViewSelect));
                textView.setTextColor(context.getResources().getColor(R.color.textColorNewTopTitleUnSelect));
            } else {
                textView.setBackgroundColor(context.getResources().getColor(R.color.bgNewTopAllLayout));
                textView.setTextColor(context.getResources().getColor(R.color.textColorNewTopTitleUnSelect));
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCandidatesWordListener != null) {
                        onCandidatesWordListener.onCandidateWord(item);
                    }
                }
            });
        }
    }
}
