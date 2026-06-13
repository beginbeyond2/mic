package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/4/27.
 */

public class SaveRecoveryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<SaveRecovery> list;
    private OnSaveRecoveryClickListener onSaveRecoveryClickListener;

    public interface OnSaveRecoveryClickListener {
        void onClickStorage(SaveRecovery saveRecovery);

        void onClickRecovery(SaveRecovery saveRecovery);

        void onClickEdit(SaveRecovery saveRecovery);
    }

    void setOnSaveRecoveryClickListener(OnSaveRecoveryClickListener onSaveRecoveryClickListener) {
        this.onSaveRecoveryClickListener = onSaveRecoveryClickListener;
    }

    SaveRecoveryAdapter(Context context, ArrayList<SaveRecovery> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_oscillograph, parent, false));
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
        TextView name;
        Button storage;
        Button recovery;

        Holder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            storage = (Button) itemView.findViewById(R.id.storage);
            recovery = (Button) itemView.findViewById(R.id.recovery);
        }

        void bind(final SaveRecovery saveRecovery) {
            name.setText(saveRecovery.getName());
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSaveRecoveryClickListener != null) {
                        onSaveRecoveryClickListener.onClickEdit(saveRecovery);
                    }
                }
            });
            storage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveRecovery.setName(name.getText().toString());
                    if (onSaveRecoveryClickListener != null) {
                        onSaveRecoveryClickListener.onClickStorage(saveRecovery);
                    }
                }
            });
            recovery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSaveRecoveryClickListener != null) {
                        onSaveRecoveryClickListener.onClickRecovery(saveRecovery);
                    }
                }
            });
        }
    }
}
