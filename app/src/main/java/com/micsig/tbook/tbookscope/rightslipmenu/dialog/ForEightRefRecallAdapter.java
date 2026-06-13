package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

/**
 * 新的RefRecall adapter
 */

public class ForEightRefRecallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<DialogRefRecallBean> list = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int refChan;
    private long mLastClickTime = 0;

    public void setList(ArrayList<DialogRefRecallBean> list) {
        this.list = list;
    }

     public interface OnItemClickListener {
        void onItemClick(DialogRefRecallBean item);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ForEightRefRecallAdapter(Context context, ArrayList<DialogRefRecallBean> list, int refChan) {
        this.context = context;
        this.list = list;
        this.refChan = refChan;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_refrecall_for_eight, parent, false);
        return new Holder(mView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public DialogRefRecallBean getSelectItem() {
        DialogRefRecallBean selectItem = null;
        for (DialogRefRecallBean item : list) {
            if (item.isSelect()) {
                selectItem = item;
                break;
            }
        }
        return selectItem;
    }

    class Holder extends RecyclerView.ViewHolder {
        private TextView title, time, path;
        private View dividerLeft, dividerTop, dividerRight, dividerBottom;

        public Holder(View itemView) {
            super(itemView);
            itemView.setBackgroundColor(context.getResources().getColor(R.color.bg_slip_backcolor));
            title = (TextView) itemView.findViewById(R.id.title);
            time = (TextView) itemView.findViewById(R.id.time);
            path = (TextView) itemView.findViewById(R.id.path);

            dividerLeft = itemView.findViewById(R.id.dividerLeft);
            dividerTop = itemView.findViewById(R.id.dividerTop);
            dividerRight = itemView.findViewById(R.id.dividerRight);
            dividerBottom = itemView.findViewById(R.id.dividerBottom);
        }

        public void bind(DialogRefRecallBean item) {
            title.setText(item.getTitle());
            time.setText(item.getTime());
            path.setText(item.getPathFile());
            if (item.isSelect()) {
                dividerLeft.setBackgroundColor(TChan.getChannelColor(context, refChan));
                dividerTop.setBackgroundColor(TChan.getChannelColor(context, refChan));
                dividerRight.setBackgroundColor(TChan.getChannelColor(context, refChan));
                dividerBottom.setBackgroundColor(TChan.getChannelColor(context, refChan));
            } else {
                dividerLeft.setBackground(null);
                dividerTop.setBackgroundColor(context.getResources().getColor(R.color.divider_right_bottom));
                dividerRight.setBackground(null);
                if (item.getIndex() == list.size() - 1) {//最后一个画bottom线
                    dividerBottom.setBackgroundColor(context.getResources().getColor(R.color.divider_right_bottom));
                } else {
                    dividerBottom.setBackground(null);
                }
            }
//
            itemView.setOnTouchListener(new View.OnTouchListener() {
                private boolean hasMove = false;
                private float oldX, oldY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            hasMove = false;
                            oldX = event.getX();
                            oldY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float offsetX = Math.abs(event.getX() - oldX);
                            float offsetY = Math.abs(event.getY() - oldY);
                            if (offsetX > 5 || offsetY > 5) {
                                hasMove = true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                           long  timeCurrent = System.currentTimeMillis();
                            if (!hasMove && Math.abs(timeCurrent - mLastClickTime) > 300) {//只是简单的点击事件
                                mLastClickTime = timeCurrent;
                                onItemClickListener.onItemClick(item);
                            }
                            break;
                    }
                    return true;
                }
            });

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (onItemClickListener != null) {
//                        onItemClickListener.onItemClick(item);
//                    }
//                }
//            });
        }


    }
}
