package com.micsig.tbook.tbookscope.top.layout.cursor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.MSwitchBox;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * @auother Liwb
 * @description:
 * @data:2025-7-14 16:23
 */
public class TopLayoutCursorSetting extends Fragment {
    private static String TAG = "TopLayoutCursorSetting";
    private Context context;
    private MSwitchBox radioTrace;

    private MSwitchBox radioCursorMode;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_cursor_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControls();
    }

    private void initView(View view){
        initChannelView(view);
    }
    private void initChannelView(View view){
        radioTrace=view.findViewById(R.id.radioCursorTrace);
        radioCursorMode = view.findViewById(R.id.radioCursorMode);
        radioTrace.setOnToggleStateChangedListener(this::onClickTrace);
        radioCursorMode.setOnToggleStateChangedListener(this::onClickMode);
    }




    private void initControls(){
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(this::CommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MSG_TVALUE_ENABLE).subscribe(consumerTValue);
    }

    private void CommandToUI(Object o) {
        CommandMsgToUI commandMsgToUI=(CommandMsgToUI) o;
        switch (commandMsgToUI.getFlag()) {
            case CommandMsgToUI.FLAG_CURSOR_SETTING_TRACE:
                boolean enable=Boolean.parseBoolean(commandMsgToUI.getParam());
                radioTrace.setState(enable);
                onClickTrace(radioTrace,enable);
                break;
        }
    }

    private Consumer<Boolean> consumerTValue = new Consumer<Boolean>() {
        @Override
        public void accept(@NonNull Boolean update) throws Exception {
            Logger.i(TAG, "isTVtrace= " + MeasureManage.getInstance().isCursorTValueTrace());
            radioTrace.setEnabled(!MeasureManage.getInstance().isCursorTValueTrace());
        }
    };


    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true);
        }
    };
    private void setCache(){
        boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_CURSOR_SETTING_TRANCE);
        radioTrace.setState(b);
        CursorManage.getInstance().setEnableCursorTrance(b);
        b = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_CURSOR_SETTING_MODE);
        radioCursorMode.setState(b);
        CursorManage.getInstance().setLabelNotFollowCursor(b);
    }

    private void onClickTrace(MSwitchBox mSwitchBox, boolean b) {
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_SETTING_TRANCE,String.valueOf(b));
        CursorManage.getInstance().setEnableCursorTrance(b);
    }

    private void onClickMode(MSwitchBox mSwitchBox, boolean b) {
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_SETTING_MODE,String.valueOf(b));
        CursorManage.getInstance().setLabelNotFollowCursor(b);
    }

    public IMeasureDetail getCursorDetail() {
        return null;
    }
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }
}
