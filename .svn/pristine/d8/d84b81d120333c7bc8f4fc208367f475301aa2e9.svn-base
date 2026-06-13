package com.micsig.tbook.tbookscope.top.layout.measure;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/4/27.
 */

public class MeasureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<MeasureBean> measureBeanList;
    private int[] colors;
    private int colorDefault;
    private OnItemClickListener itemClickListener;

    interface OnItemClickListener {
        void onClick(MeasureAdapter adapter, MeasureBean measureBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MeasureAdapter(Context context, ArrayList<MeasureBean> measureBeanList) {
        this.context = context;
        this.measureBeanList = measureBeanList;
        colors = SvgNodeInfo.getColorsIntForView();
        colorDefault = Color.parseColor(SvgNodeInfo.getColorCommon());
    }

    public void updateColors() {
        colors = SvgNodeInfo.getColorsIntForView();
        measureBeanList.forEach(bean -> {
            if (bean.isSelect()) {
                notifyItemChanged(measureBeanList.indexOf(bean));
            }
        });
    }

    public ArrayList<MeasureBean> getMeasureBeanList() {
        return measureBeanList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_measure, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(measureBeanList.get(position));
    }

    public OnItemClickListener getItemClickListener() {
        return  this.itemClickListener;
    }

    @Override
    public int getItemCount() {
        return measureBeanList.size();
    }

    private class Holder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivName;

        Holder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivName = (ImageView) itemView.findViewById(R.id.ivName);
        }

        void bind(final MeasureBean measureBean) {
            ivName.setImageResource(measureBean.getDrawableResId());
            tvName.setText(measureBean.getName());
            if (measureBean.getChannel() != 0 && measureBean.isSelect()) {
                tvName.setTextColor(colors[measureBean.getChannel() - 1]);
                tvName.setBackgroundResource(R.drawable.ic_mousedown_box);
            } else {
                tvName.setTextColor(colorDefault);
                tvName.setBackground(null);
            }

//            if (measureBean.getChannel() == 0) {
//                tvName.setTextColor(colorDefault);
//                tvName.setBackground(null);
//            } else {
//                tvName.setTextColor(colors[measureBean.getChannel() - 1]);
//                tvName.setBackgroundResource(R.drawable.ic_mousedown_box);
//            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        Screen.getViewLocation(v);
                        itemClickListener.onClick(MeasureAdapter.this, measureBean);
                    }
                }
            });
        }
    }
}
