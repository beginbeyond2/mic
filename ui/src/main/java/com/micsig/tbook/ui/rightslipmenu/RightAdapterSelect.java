package com.micsig.tbook.ui.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.BitmapUtil;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/5/3.
 */

public class RightAdapterSelect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private int itemWidth, itemHeight;
//    private int itemBgViewResId;
    private int chIdx=-1;
    private int itemBgViewResIdLeft,itemBgViewResIdRight;
    private ColorStateList itemTextViewResId;
    private boolean itemStartAndEndBgHalfCorner;
    private int itemHalfCornerEach;
    private ArrayList<RightBeanSelect> list;
    private OnItemClickListener onItemClickListener;

    private int itemMargin = 0;
    private int maxTextLines = 1;

    interface OnItemClickListener {
        void onItemClick(View itemView, RightBeanSelect item);
    }

    public void setItemMargin(int itemMargin) {
        this.itemMargin = itemMargin;
    }

    public void setTextLines(int textLines) {
        this.maxTextLines = textLines;
    }

    @SuppressLint("ResourceType")
    public RightAdapterSelect(Context context, int itemWidth, int itemHeight, ArrayList<RightBeanSelect> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.list = list;
        this.onItemClickListener = onItemClickListener;

        itemBgViewResIdLeft= R.drawable.selector_semicircle_left;
        itemBgViewResIdRight= R.drawable.selector_semicircle_right;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            itemTextViewResId=context.getColorStateList(R.drawable.selector_rightslip_select_item_textcolor);
        }
    }

    public void setItemStartAndEndBgHalfCorner(boolean itemStartAndEndBgHalfCorner, int itemHalfCornerEach) {
        this.itemStartAndEndBgHalfCorner = itemStartAndEndBgHalfCorner;
        this.itemHalfCornerEach = itemHalfCornerEach;
    }

//    public void setItemBgViewResId(int itemBgViewResId) {
//        this.itemBgViewResId = itemBgViewResId;
//    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    public void setControlColorByCh(int chIdx){
        this.chIdx=chIdx;
        itemTextViewResId=BitmapUtil.genSelectorColor(context,chIdx);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_rightslip_select, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(list.get(position));
    }

    public int getSelectPosition() {
        int position = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isCheck()) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        RadioButton textView;

        public Holder(View itemView) {
            super(itemView);
            textView = (RadioButton) itemView.findViewById(R.id.textView);
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.width = itemWidth;
            layoutParams.height = itemHeight;
            textView.setLayoutParams(layoutParams);
            textView.setMaxLines(maxTextLines);
        }

        public void bind(final RightBeanSelect item) {
//            Log.d("Tag.Debug", String.format("Holder.bind: %d",chIdx ));
            if (item.getIndex() == -1) {
                itemView.setVisibility(View.INVISIBLE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);
            textView.setText(item.getText());
            textView.setChecked(item.isCheck());
            textView.setEnabled(item.isEnable());
            textView.setTextColor(itemTextViewResId);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (itemStartAndEndBgHalfCorner) {
                layoutParams.setMargins(0, 0, 0, 0);
            } else {
                layoutParams.setMargins(itemMargin, itemMargin, itemMargin, itemMargin);
            }
            itemView.setLayoutParams(layoutParams);
            if (itemStartAndEndBgHalfCorner && list.size() >= 2 && (item.getIndex() == 0 || item.getIndex() == list.size() - 1)) {
                switch(itemHalfCornerEach) {
                    case RightViewSelect.EACH_74_39_2:
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_39_2_left : R.drawable.bg_halfcorner_74_39_2_right);
                        break;
                    case RightViewSelect.EACH_74_34:
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_34_left : R.drawable.bg_halfcorner_74_34_right);
                        break;
                    case RightViewSelect.EACH_120_60:
                        Drawable itemBgLeft=BitmapUtil.genSelectorLeftDrawable(context,chIdx);
                        Drawable itemBgRight=BitmapUtil.genSelectorRightDrawable(context,chIdx);
                        textView.setBackground(item.getIndex()==0?itemBgLeft:itemBgRight);
//                        textView.setBackgroundResource(item.getIndex() == 0 ? itemBgViewResIdLeft: itemBgViewResIdRight);
                        break;
                    case RightViewSelect.EACH_108_54:
                        Drawable drawableLeft=BitmapUtil.genSelectorLeftDrawable(context,chIdx);
                        Drawable drawableRight=BitmapUtil.genSelectorRightDrawable(context,chIdx);
//                        Drawable drawableLeft = context.getResources().getDrawable(itemBgViewResIdLeft);
                        drawableLeft.setBounds(0, 0, 108, 54);
//                        Drawable drawableRight = context.getResources().getDrawable(itemBgViewResIdRight);
                        drawableRight.setBounds(0, 0, 108, 54);
                        textView.setBackground(item.getIndex() == 0 ? drawableLeft : drawableRight);
                        break;
                    case RightViewSelect.EACH_74_39:
                    default:
                        textView.setBackgroundResource(item.getIndex() == 0 ? R.drawable.bg_halfcorner_74_39_left : R.drawable.bg_halfcorner_74_39_right);
                        break;
                }
            } else {
                StateListDrawable drawable=BitmapUtil.genSelectorDrawable(context,chIdx);
                textView.setBackground(drawable);
//                textView.setBackgroundResource(itemBgViewResId);
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, item);
                    }
                }
            });
        }
    }
}
