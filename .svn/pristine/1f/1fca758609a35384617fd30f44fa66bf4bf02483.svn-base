package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.R;

import java.util.List;
import java.util.Objects;

/**
 * Created by yangj on 2017/12/1.
 */

public class KeyBoardTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements KeyBoardTextItem.SpecialKey {
    private Context context;
    private List<KeyBoardTextItem> list;
    private boolean upper = false;
    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onQuickDelete();

        void onDelete();

        void onEnter();

        void onUpper(boolean isUpper);

        void onHide();

        void onNumber();

        void onSymbol();

        /**
         * @param isEng 点击之前的状态是否是Eng
         */
        void onEnglish(boolean isEng);

        void onOtherKey(String keyWord);
    }

    public boolean isUpper() {
        return upper;
    }

    public void setUpper(boolean upper) {
        this.upper = upper;
    }

    public KeyBoardTextAdapter(Context context, List<KeyBoardTextItem> list) {
        this.context = context;
        this.list = list;
    }

    public int getSpanSize(int position) {
        KeyBoardTextItem item = list.get(position);
        return item.getSize();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_text_keyboard_button, parent, false));
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
        Button button;

        public Holder(View itemView) {
            super(itemView);
            button = (Button) itemView.findViewById(R.id.text);
        }

        public void bind(final KeyBoardTextItem item) {
            switch (item.getIndex()) {
                case INDEX_PLACEHOLDER:
                case INDEX_ENTER:
                case INDEX_HIDE:
                case INDEX_NUMBER:
                case INDEX_SYMBOL:
                case INDEX_LANG:
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
                    break;
                default:
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24);
                    break;
            }

            itemView.setVisibility(View.VISIBLE);
            itemView.setClickable(true);
            button.setBackgroundResource(R.drawable.selector_rightslip_button);
            if (item.getIndex() == INDEX_DELETE) {
                button.setText("←");
                button.setBackgroundResource(R.drawable.selector_rightslip_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onDelete();
                        }
                    }
                });
                button.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onQuickDelete();
                        }
                        return false;
                    }
                });
            } else if (item.getIndex() == INDEX_PLACEHOLDER) {
                itemView.setVisibility(View.INVISIBLE);
                itemView.setClickable(false);
            } else if (item.getIndex() == INDEX_ENTER) {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onEnter();
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_UPPER) {
                button.setText("↑");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upper = !upper;
                        if (onItemClickListener != null) {
                            onItemClickListener.onUpper(upper);
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_HIDE) {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onHide();
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_NUMBER) {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onNumber();
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_SPACE) {
                button.setText(" ");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onOtherKey(" ");
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_SYMBOL) {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onSymbol();
                        }
                    }
                });
            } else if (item.getIndex() == INDEX_LANG) {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onEnglish(Objects.equals(item.getWord(), LANG_ENG));
                        }
                    }
                });
            } else {
                button.setText(item.getWord());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onOtherKey(item.getWord());
                        }
                    }
                });
            }
        }
    }
}
