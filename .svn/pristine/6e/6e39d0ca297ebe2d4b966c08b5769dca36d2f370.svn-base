package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsDetailFlag;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/6/12.
 */

public abstract class TopLayoutTriggerSerialsBaseDetail extends Fragment implements SerialsDetailFlag, IDigits {
    protected int bits = 2;
    protected int digits = DIGITS_16;
    protected int serialsNumber;
    protected TopDialogNumberKeyBoard dialogKeyBoard;

    private OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener;

    public interface OnSerialsDetailSendMsgListener {
        void onClick(Fragment detail, ISerialsDetail serialsDetail, boolean isFromEventBus);
    }

    public void setOnSerialsDetailSendMsgListener(OnSerialsDetailSendMsgListener onSerialsDetailSendMsgListener) {
        this.onSerialsDetailSendMsgListener = onSerialsDetailSendMsgListener;
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
    }

    protected int getSerialsNumber() {
        return serialsNumber ;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        initControl();
        dialogKeyBoard = (TopDialogNumberKeyBoard) getActivity().findViewById(R.id.dialogNumberKeyBoard);
    }

    protected abstract void initView(View view);

    protected abstract int getLayoutResId();

    public abstract ISerialsDetail getSerialsDetail(int detailFlag);

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(@NonNull RightMsgSerials rightMsgSerials) throws Exception {
            setConsumer(rightMsgSerials);
        }
    };

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTriggerSerialsBaseDetail, true);
        }
    };

    protected abstract void setCache();

    protected abstract void setConsumer(RightMsgSerials rightMsgSerials);

    protected void sendMsg(ISerialsDetail serialsDetail, boolean isFromEventBus) {
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);
        boolean isSerialsSelect = ((index == TopLayoutTrigger.DETAIL_S1 && serialsNumber == CacheUtil.S1)
                || (index == TopLayoutTrigger.DETAIL_S2 && serialsNumber == CacheUtil.S2)
                || (index == TopLayoutTrigger.DETAIL_S3 && serialsNumber == CacheUtil.S3)
                || (index == TopLayoutTrigger.DETAIL_S4 && serialsNumber == CacheUtil.S4)
        );
        //当且仅当当前trigger列表选中的是该项时，才向外发送消息
        if (onSerialsDetailSendMsgListener != null && isSerialsSelect) {
            onSerialsDetailSendMsgListener.onClick(this, serialsDetail, isFromEventBus);
        }
    }

    protected TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) {

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) {
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true));
            setOnCheckChangedListener(view, item);
        }
    };


    protected abstract void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item);

    protected TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(final TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true));
            setOnClickEditListener(v, text);
        }
    };

    protected abstract void setOnClickEditListener(final TopViewEdit v, String text);

    /**
     * 将带空格的二进制十六进制转换，转换之后依然带空格
     */
    protected String HexBin(String text, int preDigits, int digits) {
        return SerialsUtils.HexBin(text, preDigits, digits);
    }

    /**
     * 将目标数字去空格，补位数，重新计算空格
     */
    protected String reCalcSpace(String s, int bits, int digits) {
        return SerialsUtils.reCalcSpace(s, bits, digits);
    }

    /**
     * 将任意进制带空格数字转换为10进制int
     *
     * @param text   数字
     * @param digits 进制
     * @return
     */
    protected int toD(String text, int digits) {
        return SerialsUtils.toD(text, digits);
    }

    protected long toDLong(String text, int digits) {
        return SerialsUtils.toDLong(text, digits);
    }

    protected int getConditionValue(int indexCondition) {
        return SerialsUtils.getConditionValueToEventBus(indexCondition);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden==false)
            Tools.PrintControlsLocation(super.getClass().getSimpleName(),(ViewGroup) super.getView());
    }
}
