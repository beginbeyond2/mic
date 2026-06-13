package com.micsig.tbook.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.ui.bean.RadioButtonBean;

import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-27 11:12
 */
public class MRadioGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<RadioButtonBean> list;
    private int itemWidth,itemHeight;


    public MRadioGroupAdapter(Context context,int itemWidth,int itemHeight,List<RadioButtonBean> list){
        this.context=context;
        this.itemWidth=itemWidth;
        this.itemHeight=itemHeight;
        this.list=list;
    }

    public void setList(List<RadioButtonBean> list){
        this.list=list;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.item_radiobutton,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Holder)holder).bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class Holder extends RecyclerView.ViewHolder{
        RadioButton textView;
        ConstraintLayout layout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            layout=itemView.findViewById(R.id.layout);
            textView=itemView.findViewById(R.id.textView);
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.width = itemWidth;
            layoutParams.height = itemHeight;
            textView.setLayoutParams(layoutParams);
        }

        public void setVisible(RadioButtonBean item){
            boolean isVisible=item.getVisible()==View.VISIBLE;
            if (isVisible){
                layout.setPadding(item.getItemLeftMargin(),item.getItemTopMargin(),item.getItemRightMargin(),item.getItemBottomMargin());
                layout.setVisibility(View.VISIBLE);
            }  else {
                layout.setVisibility(View.GONE);
            }
        }


        public void bind(RadioButtonBean item){
            if (item.getIndex()==-1){
                itemView.setVisibility(View.INVISIBLE);
                return;
            }
            setVisible(item);

            if (item.isEnableBeforeColor()){
                textView.setTextColor(item.getBeforeColor());
            }
//            textView.setBackgroundResource(item.getResIdBackGround());
            textView.setText(item.getText());
            textView.setChecked(item.isCheck());
            textView.setEnabled(item.isEnable());
            textView.setOnClickListener((v)->{
                if (item.getOnClick()!=null){
                    item.getOnClick().accept(itemView,item);
                }
            });
        }
    }
}
