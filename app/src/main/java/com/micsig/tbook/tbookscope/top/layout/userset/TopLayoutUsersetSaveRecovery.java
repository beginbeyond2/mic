package com.micsig.tbook.tbookscope.top.layout.userset;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener;
import com.micsig.tbook.tbookscope.top.layout.save.ISaveDetail;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;

import java.util.ArrayList;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutUsersetSaveRecovery extends Fragment {
    private static final String TAG = "TopLayoutUsersetSaveRecovery";
    private Context context;
    private SaveRecoveryAdapter adapter;
    private ArrayList<SaveRecovery> list = new ArrayList<SaveRecovery>();
    private TopDialogTextKeyBoard layoutTextKeyBoard;

    private String defaultName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_usersetsaverecovery, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        initView(view);
        initControl();
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
    }

    private void initView(View view) {
        RecyclerView rvList = (RecyclerView) view.findViewById(R.id.rvList);
//        rvList.setLayoutManager(new LinearLayoutManager(context));
        rvList.setLayoutManager(new GridLayoutManager(context,2));
        for (int i = 0; i < SaveRecoveryUtil.SAVE_RECOVERY_NUMBER; i++) {
            String s = CacheUtil.get().getOtherMapValue(CacheUtil.USERSET + i);
            list.add(new SaveRecovery(i, s));
        }

        adapter = new SaveRecoveryAdapter(context, list);
        adapter.setOnSaveRecoveryClickListener(onSaveRecoveryClickListener);
        rvList.setAdapter(adapter);

        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_USERSET_NAME:
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    curBean = list.get(Integer.parseInt(params[0]));
                    onDialogDismissListener.onDismiss(params[1]);
                    break;
                case CommandMsgToUI.FLAG_USERSET_SAVE:
                    onSaveRecoveryClickListener.onClickStorage(list.get(Integer.parseInt(commandMsgToUI.getParam())));
                    break;
                case CommandMsgToUI.FLAG_USERSET_RECOVERY:
                    onSaveRecoveryClickListener.onClickRecovery(list.get(Integer.parseInt(commandMsgToUI.getParam())));
                    break;

                case CommandMsgToUI.FLAG_STOTAGE_CONSAVE:{
                    String fileName=commandMsgToUI.getParam();
                    curBean = list.get(0);
                    onDialogDismissListener.onDismiss(fileName);
                }break;
                case CommandMsgToUI.FLAG_STOTAGE_CONSAVE_START:{
//                    Logger.i(Command.TAG,"start!");
                    onSaveRecoveryClickListener.onClickStorage(list.get(0));
                }break;
                case CommandMsgToUI.FLAG_STOTAGE_CONLOAD:{
                    String fileName=commandMsgToUI.getParam();
                    curBean = list.get(0);
                    onDialogDismissListener.onDismiss(fileName);
                    onSaveRecoveryClickListener.onClickRecovery(list.get(0));
                }break;
            }
        }
    };

    private SaveRecoveryAdapter.OnSaveRecoveryClickListener onSaveRecoveryClickListener = new SaveRecoveryAdapter.OnSaveRecoveryClickListener() {
        @Override
        public void onClickStorage(SaveRecovery saveRecovery) {
            PlaySound.getInstance().playButton();
            Command.get().getUserset().setRecovery(saveRecovery.getIndex(), false);
//            PrefUtil.putString("userset" + saveRecovery.getIndex(), saveRecovery.getName());
//            SaveRecoveryUtil.putSaveRecoveryData(saveRecovery.getIndex());
            try {
                int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
                CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + saveRecovery.getIndex(), String.valueOf(channelSelect));
                SaveManage.getInstance().saveUserSet(saveRecovery.getName(), CacheUtil.get().getCacheMap(), new SaveManage.SaveCallBack() {
                    @Override
                    public void onResult(boolean success, String msg) {
                        String tempMsg = msg.substring(msg.lastIndexOf(saveRecovery.getName()));
                        DToast.get().show(tempMsg);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClickRecovery(SaveRecovery saveRecovery) {
            PlaySound.getInstance().playButton();
            Command.get().getUserset().setSave(saveRecovery.getIndex(), false);

//            HashMap<String, String> map = SaveRecoveryUtil.getSaveRecoveryData(saveRecovery.getIndex());
//            CacheUtil.get().putMapAll(map);
//            RxBus.get().post(RxEnum.MAIN_LOAD_CACHE, new LoadCache());
            Scope.getInstance().enableCommand(false);
            CacheUtil.get().initStateCacheLoad();
            boolean loadSuccess = false;
            try {

                ((MainActivity) context).preMainLoadCahceProcess();
                loadSuccess = SaveManage.getInstance().loadUserSet(saveRecovery.getName(), CacheUtil.get().getCacheMap());
//                Logger.i("recoveryName= " + saveRecovery.getName() + " loadSuccess= " + loadSuccess);
                if (!loadSuccess) {
                    //配置载入失败则清空配置载入默认配置值
                    CacheUtil.get().clearCacheMap();
                    HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
                    horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS);
                    horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0);
//                    horizontalAxis.correctTimePose();
//                    ExternalKeysProtocol.closeShift();
                    DToast.get().show(R.string.saveRecoveryFileIsNotExist);
                }
                //刷新界面

                ((MainActivity) context).updateMainLoadCaheProcess(loadSuccess);
                ((MainActivity) context).postMainLoadCacheProcess();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Scope.getInstance().enableCommand(true);
            } finally {
                if (loadSuccess) {
                    int recoverySelect = CacheUtil.get().getInt(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + saveRecovery.getIndex());
                    RxBus.getInstance().post(RxEnum.MQ_MSG_RECOVERY_SELECT, recoverySelect);
                } else {
                    RxBus.getInstance().post(RxEnum.MQ_MSG_RECOVERY_SELECT, 0);
                }
            }
        }

        @Override
        public void onClickEdit(final SaveRecovery saveRecovery) {
            PlaySound.getInstance().playButton();
            curBean = saveRecovery;
            layoutTextKeyBoard.setData(saveRecovery.getName(), TopDialogTextKeyBoard.INPUT_TYPE_ALL, 21, onDialogDismissListener);
        }
    };

    private SaveRecovery curBean;

    private TopDialogTextKeyBoard.OnDialogDismissListener onDialogDismissListener = new TopDialogTextKeyBoard.OnDialogDismissListener() {
        @Override
        public void onDismiss(String result) {
            Command.get().getUserset().setNames(curBean.getIndex(), result, false);
            list.get(curBean.getIndex()).setName(result);
            adapter.notifyDataSetChanged();
            CacheUtil.get().putOtherMapAndSave(CacheUtil.USERSET + curBean.getIndex(), result);
        }
    };

    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    public ISaveDetail getSaveDetail() {
        return null;
    }
}
