package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/12/6.
 */

public class TopDialogCandidatesWord extends RelativeLayout {
    public static final int ACTION_CANDIDATES_LEFT = -1;
    public static final int ACTION_CANDIDATES_RIGHT = 1;
    public static final int ACTION_CANDIDATES_FINISH = 0;

    private Context context;
    private PinyinIME pinyinIME;
    private TextView wordTextView;
    private Button nextView;
    private RecyclerView choiceRecyclerView;
    private CandidatesWordAdapter adapter;
    private List<KeyBoardCandidatesItem> list = new ArrayList<>();
    private OnDialogClickWordListener onDialogClickWordListener;

    interface OnDialogClickWordListener {
        /**
         * @return 如果此次设置不成功，则返回false，否则返回true
         */
        boolean onClickWord(String s);
    }

    public TopDialogCandidatesWord(Context context) {
        this(context, null);
    }

    public TopDialogCandidatesWord(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogCandidatesWord(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.shape_frame_bg_black);
        View view = inflate(context, R.layout.dialog_candidatesword, this);
        wordTextView = (TextView) view.findViewById(R.id.word);
        nextView = (Button) view.findViewById(R.id.nextView);
        choiceRecyclerView = (RecyclerView) view.findViewById(R.id.choiceRecyclerView);

        choiceRecyclerView.requestFocus();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        choiceRecyclerView.setLayoutManager(layoutManager);
        adapter = new CandidatesWordAdapter(context, list);
        adapter.setOnCandidatesWordListener(onCandidatesWordListener);
        choiceRecyclerView.setAdapter(adapter);
        hide();

        nextView.setOnClickListener(onClickListener);
        pinyinIME = new PinyinIME(context);
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CANDIDATE_CHANGED).subscribe(consumerCandidate);
    }

    public void setData(String pinyin, OnDialogClickWordListener onDialogClickWordListener) {
        wordTextView.setText(pinyin);
        this.onDialogClickWordListener = onDialogClickWordListener;
        list.clear();
        List<String> stringList = pinyinIME.getChoiceList(pinyin);
        for (int i = 0; i < stringList.size(); i++) {
            list.add(new KeyBoardCandidatesItem(i, stringList.get(i), i == 0));
        }
        choiceRecyclerView.getLayoutManager().scrollToPosition(0);
        adapter.notifyDataSetChanged();
        if (StrUtil.isEmpty(pinyin)) {
            hide();
        } else {
            show();
        }
    }

    public String getPinyin() {
        return wordTextView.getText().toString();
    }

    public void hide() {
        wordTextView.setText("");
        list.clear();
        adapter.notifyDataSetChanged();
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TEXTKEYBOARD_CANDIDATESWORD);
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TEXTKEYBOARD_CANDIDATESWORD);
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    private void moveOnlyScroll(RecyclerView recyclerView, boolean isRight) {
        int index = 0;
        for (KeyBoardCandidatesItem item : list) {
            if (item.isSelect()) {
                index = item.getIndex();
                break;
            }
        }
        if (isRight) {
            if (index != list.size() - 1) {
                index += 1;
            } else {
                index = 0;
            }
        } else {
            if (index != 0) {
                index -= 1;
            } else {
                index = list.size() - 1;
            }
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition();
        if (firstCompletely > index) {
            layoutManager.scrollToPosition(index);
        } else if (lastCompletely < index) {
            layoutManager.scrollToPosition(index);
        }
        for (KeyBoardCandidatesItem item : list) {
            item.setSelect(item.getIndex() == index);
        }
        adapter.notifyDataSetChanged();
    }

    private void selectItem(KeyBoardCandidatesItem item) {
        if (onDialogClickWordListener != null) {
            boolean value = onDialogClickWordListener.onClickWord(item.getText());
            if (!value) return;
        }
        wordTextView.setText("");
        list.clear();
        List<String> stringList = pinyinIME.getPredictList(item.getText());
        for (int i = 0; i < stringList.size(); i++) {
            list.add(new KeyBoardCandidatesItem(i, stringList.get(i), i == 0));
        }
        choiceRecyclerView.getLayoutManager().scrollToPosition(0);
        adapter.notifyDataSetChanged();
        if (list.size() == 0) {
            hide();
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            moveOnlyScroll(choiceRecyclerView, true);
        }
    };

    private Consumer<Integer> consumerCandidate = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (TopDialogCandidatesWord.this.getVisibility() != VISIBLE) {
                return;
            }
            if (integer == ACTION_CANDIDATES_LEFT) {
                moveOnlyScroll(choiceRecyclerView, false);
            } else if (integer == ACTION_CANDIDATES_RIGHT) {
                moveOnlyScroll(choiceRecyclerView, true);
            } else if (integer == ACTION_CANDIDATES_FINISH) {
                int index = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isSelect()) {
                        index = i;
                        break;
                    }
                }
                selectItem(list.get(index));
            }
        }
    };

    private CandidatesWordAdapter.OnCandidatesWordListener onCandidatesWordListener = new CandidatesWordAdapter.OnCandidatesWordListener() {
        @Override
        public void onCandidateWord(KeyBoardCandidatesItem item) {
            PlaySound.getInstance().playButton();
            selectItem(item);
        }
    };
}
