package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysNode;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.util.StrUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/3.
 */

public class DialogRefRecall extends AbsoluteLayout {
//    public static final int ACTION_REFRECALL_UP = -1;
//    public static final int ACTION_REFRECALL_FINISH = 0;
//    public static final int ACTION_REFRECALL_DOWN = 1;

    private View clickView;
    private Context context;
    private RecyclerView recyclerView;
    private DialogRefRecallAdapter adapter;
    private ArrayList<DialogRefRecallBean> list;
    private OnDialogItemClickListener onDialogItemClickListener;

    private ViewGroup rootViewGroup;

    public interface OnDialogItemClickListener {
        void onItemClick(View clickView, DialogRefRecallBean bean);
    }

    public DialogRefRecall(Context context) {
        this(context, null);
    }

    public DialogRefRecall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogRefRecall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[481, 36]	220	500
    private void init() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_recall, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });
        initView(rootViewGroup);
        hide();

        RxBus.getInstance().getObservable(RxEnum.DIALOG_REFRECALL_CHANGED).subscribe(consumerRefRecallChanged);
    }

    private void initView(View view) {
//        TextView back = (TextView) view.findViewById(R.id.back);
//        back.setOnClickListener(onClickListener);
        recyclerView = (RecyclerView) view.findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        list = getList();
        adapter = new DialogRefRecallAdapter(context, list);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    Logger.i("DialogRefRecall:newState:" + newState);
//                }
//            }
//        });
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_REFRECALL);
        Tools.PrintControlsLocation("DialogRefRecall",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_REFRECALL);
    }

    public void setData(View clickView, String item, OnDialogItemClickListener onDialogItemClickListener) {
        this.clickView = clickView;
        this.onDialogItemClickListener = onDialogItemClickListener;
        //list.clear();
        list = getList();
        if (!StrUtil.isEmpty(item)) {
            for (DialogRefRecallBean bean : list) {
                bean.setSelect(item.equals(bean.getTitle()));
                if (bean.isSelect()) {
                    scrollToPos(bean.getIndex());
                }
            }
        } else {
            for (DialogRefRecallBean bean : list) {
                bean.setSelect(false);
            }
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
        show();
    }

    public void moveOnlyScroll(boolean isUp) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean bean = list.get(i);
            if (bean.isSelect()) {
                index = bean.getIndex();
                break;
            }
        }
        if (isUp) {
            if (index != 0) {
                index -= 1;
            } else {
                index = list.size() - 1;
            }
        } else {
            if (index != list.size() - 1) {
                index += 1;
            } else {
                index = 0;
            }
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition();
        if (firstCompletely > index) {
            layoutManager.scrollToPositionWithOffset(index,0);
        } else if (lastCompletely < index) {
            layoutManager.scrollToPositionWithOffset(index - 9, -30);
        }
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean bean = list.get(i);
            bean.setSelect(bean.getIndex() == index);
            if (bean.isSelect()) {
                scrollToPos(bean.getIndex());
            }
        }

        adapter.notifyDataSetChanged();

        confirm();
    }

    private void scrollToPos(int pos) {
        if (list.size() > 9 && pos > 4 && pos < list.size() - 5) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            linearLayoutManager.scrollToPositionWithOffset(pos, 225);
        }
    }

    private ArrayList<DialogRefRecallBean> getList() {
        ArrayList<DialogRefRecallBean> list = new ArrayList<DialogRefRecallBean>();
        File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_REFWAVE);
        if (files == null) return list;
        for (int i = 0; i < files.length; i++) {
            long time = files[i].lastModified();
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
            DialogRefRecallBean item = new DialogRefRecallBean();
            item.setIndex(i);
            item.setLastModifyTime(time);
            item.setPathFile(files[i].getAbsolutePath());
            item.setSelect(false);
            item.setTime(ctime);
            item.setTitle(files[i].getName().replace(".mwav", "").replace(".wav", ""));
            list.add(item);
        }
        Collections.sort(list, new Comparator<DialogRefRecallBean>() {

            @Override
            public int compare(DialogRefRecallBean o1, DialogRefRecallBean o2) {
                long i = o2.getLastModifyTime() - o1.getLastModifyTime();
                if (i == 0) {
                    return o2.getTitle().compareTo(o1.getTitle());
                } else {
                    return Long.compare(o2.getLastModifyTime(), o1.getLastModifyTime());
                }
            }
        });
        for (int i = 0; i < list.size(); i++) {
            DialogRefRecallBean item = list.get(i);
            item.setIndex(i);
        }
        return list;
    }

    public void confirm() {
        if (onDialogItemClickListener != null) {
            DialogRefRecallBean bean = null;
            for (DialogRefRecallBean item : list) {
                if (item.isSelect()) {
                    bean = item;
                    break;
                }
            }
            onDialogItemClickListener.onItemClick(clickView, bean);
        }
    }

    private Consumer<String> consumerRefRecallChanged = new Consumer<String>() {
        @Override
        public void accept(String recallInfo) throws Exception {
            String[] params = recallInfo.split(CommandMsgToUI.PARAM_SPLIT);
            int integer = Integer.parseInt(params[0]);
            int channelNumber = Integer.parseInt(params[1]);
            switch (integer) {
                case ExternalKeysNode.ACTION_REFRECALL_FINISH:
                    DialogRefRecallBean bean = null;
                    for (DialogRefRecallBean item : list) {
                        if (item.isSelect()) {
                            bean = item;
                            break;
                        }
                    }
                    if (bean != null) {
                        onItemClickListener.onItemClick(bean);
                    }
                    hide();
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_UP:
                    moveOnlyScroll(true);
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_DOWN:
                    moveOnlyScroll(false);
                    break;
            }
        }
    };

//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            PlaySound.getInstance().playButton();
//            hide();
//        }
//    };

    private DialogRefRecallAdapter.OnItemClickListener onItemClickListener = new DialogRefRecallAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(DialogRefRecallBean item) {
            PlaySound.getInstance().playButton();
            for (DialogRefRecallBean bean : list) {
                bean.setSelect(bean.getIndex() == item.getIndex());
                if (bean.isSelect()) {
                    confirm();
                }
            }

            adapter.notifyDataSetChanged();

            hide();
        }
    };
}
