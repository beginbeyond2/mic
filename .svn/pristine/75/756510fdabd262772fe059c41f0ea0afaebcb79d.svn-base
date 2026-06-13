package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutUsersetFactoryReset extends Fragment {
    private Context context;
    private Button factory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetfactoryreset, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initData();
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void initView(View view) {
        factory = (Button) view.findViewById(R.id.factory);
        factory.setOnClickListener(onClickListener);
    }

    private void initData() {
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_USERSET_FACTORYRESET:
                    onClickListener.onClick(factory);
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();

            Scope.getInstance().enableCommand(false);
            CacheUtil.get().clearCacheMap();
            CacheUtil.get().clearOtherMap();
            CacheUtil.get().initStateCacheLoad();
            CacheUtil.get().setClickFactoryReset(true);

            ((MainActivity) context).preMainLoadCahceProcess();
            {

                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS);
                horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0);
//                horizontalAxis.correctTimePose();
//                if (Scope.getInstance().isZoom()) {
//                    WaveManage.get().setPositionY(IWave.Ch1, GlobalVar.get().getMainWave().y / 5 - 20);
//                    WaveManage.get().setPositionY(IWave.Ch2, GlobalVar.get().getMainWave().y / 5 * 2 - 40);
//                    WaveManage.get().setPositionY(IWave.Ch3, GlobalVar.get().getMainWave().y / 5 * 3 - 60);
//                    WaveManage.get().setPositionY(IWave.Ch4, GlobalVar.get().getMainWave().y / 5 * 4 - 80);
//                    Command.get().getDisplay().Zoom(false, true);
//                }
//                ExternalKeysProtocol.closeShift();
            }
            ((MainActivity) context).updateMainLoadCaheProcess(false);
            ((MainActivity) context).postMainLoadCacheProcess();
        }
    };
}
