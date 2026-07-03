package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.graphics.drawable.Drawable; // 导入Drawable图形类
import android.os.Bundle; // 导入Bundle状态保存类
import android.util.Log; // 导入日志工具类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件类
import android.widget.CheckBox; // 导入复选框控件类
import android.widget.CompoundButton; // 导入复合按钮基类
import android.widget.RadioButton; // 导入单选按钮控件类
import android.widget.RelativeLayout; // 导入相对布局类
import android.widget.TextView; // 导入文本视图控件类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类
import androidx.lifecycle.ViewModelProvider; // 导入ViewModel提供者类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Data.AutoSave; // 导入自动保存数据类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.IChannel; // 导入通道接口
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入播放音效工具类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息监听器
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail; // 导入测量详情接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘对话框
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard; // 导入文本键盘对话框
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.util.FileSelector; // 导入文件选择器类
import com.micsig.tbook.tbookscope.util.TopViewTimeSelector; // 导入时间选择器类
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理类
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理类
import com.micsig.tbook.ui.FixedSizeHashSet; // 导入固定大小HashSet类
import com.micsig.tbook.ui.MMainMenuChannel; // 导入主菜单通道视图类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部编辑视图类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入顶部下拉选择视图类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean类
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道视图类
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice; // 导入通道多选视图类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入单选组视图类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean类

import java.io.File; // 导入文件类
import java.time.LocalDateTime; // 导入本地日期时间类
import java.util.ArrayList; // 导入动态数组类
import java.util.HashMap; // 导入HashMap类
import java.util.List; // 导入列表接口
import java.util.stream.Collectors; // 导入流收集器类

import io.reactivex.rxjava3.annotations.NonNull; // 导入NonNull注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - 自动保存配置界面                             ║
 * ║  核心职责: 提供自动保存功能的UI配置界面，包括保存类型/通道/时间/路径等   ║
 * ║  架构设计: Fragment + MVVM，通过RxBus接收事件，ViewModel管理序号状态   ║
 * ║  数据流向: UI交互 → CacheUtil → AutoSave → AutoSaveTaskManager       ║
 * ║  依赖关系: 依赖 AutoSave/AutoSaveTaskCondition/TaskSuffixNumModel     ║
 * ║           /RxBus/CacheUtil/FileSelector等                             ║
 * ║  使用场景: 用户在顶部滑动菜单中配置自动保存参数并启动/停止自动保存       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by Administrator on 2017/4/6.
 */

/**
 * 自动保存配置界面Fragment
 * <p>提供自动保存功能的完整UI配置，包括保存类型选择、通道选择、时间设置、路径浏览等</p>
 */
public class TopLayoutAutoSave extends Fragment { // 自动保存配置界面Fragment
    /** 日志标签 */
    private static final String TAG = "TopLayoutCursorCommon"; // 日志标签
    /** 上下文对象 */
    private Context context; // Activity上下文
    /** 通道选择视图 */
    private MMainMenuChannel viewChannel; // 通道选择视图控件

    /** 保存类型列表 */
    private List<Integer> saveType = new ArrayList<>(); // 保存类型编码列表
    /** 开始时间和停止时间选择器 */
    private TopViewTimeSelector topViewStartTimeSelector, topViewStopTimeSelector; // 开始/停止时间选择器
    /** 通道显示状态数组，28个通道的可见性 */
    private boolean[] channelShow = { // 通道显示状态数组
            false, false, false, false, false, false, false, false, // Ch1-Ch8
            false, false, false, false, false, false, false, false, // Math1-Math8
            false, false, false, false, false, false, false, false, // R1-R8
            false, false, false, false // 预留
    };

    /** 保存路径下拉选择器 */
    private TopViewSpinner spinner; // 保存路径下拉选择器

    /** 停止条件、时间间隔、保存模式单选组 */
    private TopViewRadioGroup rgStopCondition,intervalGroup,saveModeGroup; // 停止条件/间隔/模式单选组

    /** 保存类型复选框 */
    private CheckBox checkWAV,checkCSV,checkBIN,checkPicture,checkSession; // WAV/CSV/BIN/截图/会话复选框

    /** 帧数停止条件文本 */
    private TextView frameStopText; // 帧数停止条件文本
    /** 保存路径集合，最多10个 */
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10); // 保存路径集合

    /** 开始/停止/浏览按钮 */
    private Button startBtn, stopBtn, btnBrowse; // 开始/停止/浏览按钮

    /** 保存文件名编辑框 */
    private TopViewEdit saveNameEdit; // 保存文件名编辑框
    /** 文件名添加序号复选框 */
    private CheckBox checkFileNameAdd; // 文件名添加序号复选框

    /** 文件选择器 */
    private FileSelector fileSelector; // 文件选择器

    /** 文本键盘对话框 */
    private TopDialogTextKeyBoard layoutTextKeyBoard; // 文本键盘对话框

    /** 后缀序号文本 */
    private TextView txtSuffixNum; // 后缀序号文本显示

    /** 数字键盘对话框 */
    protected TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框

    /** 后缀序号ViewModel */
    private TaskSuffixNumModel taskSuffixNumModel; // 后缀序号ViewModel

    /** 保存类型多选布局容器 */
    private RelativeLayout multiSaveType; // 保存类型多选布局容器
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //default

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_save_autosave, container, false); // 填充自动保存布局
    }

    /**
     * 视图创建完成回调
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化所有视图控件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图控件
        viewChannel = view.findViewById(R.id.chanAutoSave); // 获取通道选择视图
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 设置通道监听器
        spinner = view.findViewById(R.id.topSpinner); // 获取保存路径下拉选择器
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置下拉数据
                getPreviousDirectory(), R.layout.layout_item_for_save_directory, onItemSelectListener); // 设置标题/数据/布局/监听器
        topViewStartTimeSelector = view.findViewById(R.id.startCondition); // 获取开始时间选择器
        topViewStartTimeSelector.setNowTime(); // 设置为当前时间
        topViewStopTimeSelector = view.findViewById(R.id.stopTimeCondition); // 获取停止时间选择器
        topViewStopTimeSelector.setNowTime(); // 设置为当前时间
        rgStopCondition = view.findViewById(R.id.stopRadioGroup); // 获取停止条件单选组
        frameStopText = view.findViewById(R.id.stopFrameCondition); // 获取帧数停止条件文本
        startBtn = view.findViewById(R.id.btnStart); // 获取开始按钮
        stopBtn = view.findViewById(R.id.btnStop); // 获取停止按钮
        intervalGroup = view.findViewById(R.id.interval); // 获取时间间隔单选组
        saveModeGroup = view.findViewById(R.id.saveMode); // 获取保存模式单选组
//        saveTypeGroup = view.findViewById(R.id.saveType);
        multiSaveType = view.findViewById(R.id.saveTypeMulti); // 获取保存类型多选布局
        checkWAV = view.findViewById(R.id.check_wav); // 获取WAV复选框
        checkCSV = view.findViewById(R.id.check_csv); // 获取CSV复选框
        checkBIN = view.findViewById(R.id.check_bin); // 获取BIN复选框
        checkPicture = view.findViewById(R.id.check_picture); // 获取截图复选框
        checkSession = view.findViewById(R.id.check_session); // 获取会话复选框
        checkWAV.setOnClickListener(onClickListener); // WAV复选框设置点击监听
        checkCSV.setOnClickListener(onClickListener); // CSV复选框设置点击监听
        checkBIN.setOnClickListener(onClickListener); // BIN复选框设置点击监听
        checkPicture.setOnClickListener(onClickListener); // 截图复选框设置点击监听
        checkSession.setOnClickListener(onClickListener); // 会话复选框设置点击监听
        rgStopCondition.setData(R.string.stopCondition, R.array.stopCondition, onCheckChangedListener); // 设置停止条件数据
        intervalGroup.setData(R.string.interval,R.array.intervalTime,onCheckChangedListener); // 设置时间间隔数据
        saveModeGroup.setData(R.string.autoSaveMode,R.array.autoSaveMode,onCheckChangedListener); // 设置保存模式数据
        btnBrowse = view.findViewById(R.id.btn_browse); // 获取浏览按钮
        btnBrowse.setOnClickListener(onClickListener); // 浏览按钮设置点击监听
        startBtn.setOnClickListener(onClickListener); // 开始按钮设置点击监听
        stopBtn.setOnClickListener(onClickListener); // 停止按钮设置点击监听
        checkFileNameAdd = view.findViewById(R.id.check_file_name_add); // 获取文件名添加序号复选框
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_grey); // 获取灰色选择按钮图标
        checkFileNameAdd.setBackground(null); // 清除背景
        checkFileNameAdd.setButtonDrawable(null); // 清除按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界
        checkFileNameAdd.setCompoundDrawables(drawable, null, null, null); // 设置左侧图标
        checkFileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置选中状态变化监听
        fileSelector = new FileSelector(context,(selectedPath) -> { // 创建文件选择器
            addSelectToPathSet(selectedPath); // 添加选中路径到集合
        });
        saveNameEdit = (TopViewEdit) view.findViewById(R.id.saveName); // 获取保存文件名编辑框
        saveNameEdit.setOnClickEditListener(onClickEditListener); // 设置编辑点击监听
        txtSuffixNum = view.findViewById(R.id.txt_index_num); // 获取后缀序号文本
        txtSuffixNum.setOnClickListener(onClickListener); // 序号文本设置点击监听
        frameStopText.setOnClickListener(onClickListener); // 帧数文本设置点击监听
        checkFileNameAdd.setEnabled(false); // 禁用文件名添加序号复选框
        checkFileNameAdd.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewDisable)); // 设置禁用颜色
        checkFileNameAdd.setChecked(true); // 默认选中

        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 获取文本键盘对话框
        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
        taskSuffixNumModel = new ViewModelProvider(this).get(TaskSuffixNumModel.class); // 获取序号ViewModel
        taskSuffixNumModel.getTextLiveData().observe(getViewLifecycleOwner(),text-> { // 观察序号变化
            txtSuffixNum.setText(text); // 更新序号文本显示
        });
    }


    /**
     * 初始化事件控制，订阅RxBus事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅通道变化事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅其他设置变化事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅Ref变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅加载缓存事件
//        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
//        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKSAVE).subscribe(consumerMainBottomQuickSave);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择事件
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE).subscribe(consumerAutoSaveTask); // 订阅自动保存按钮状态事件
        RxBus.getInstance().getObservable(RxEnum.MSG_UPDATE_AUTO_SAVE_STOP_BUTTON_STATE).subscribe(consumerAutoSaveTaskStopButton); // 订阅停止按钮状态事件
        RxBus.getInstance().getObservable(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT).subscribe(consumerAutoSaveScreenShot); // 订阅自动保存截图事件

    }


    /**
     * 加载缓存事件消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 加载缓存消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收加载缓存事件
            setCache(); // 设置缓存到UI
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSaveWave, true); // 标记加载完成
        }
    };

    /**
     * 从缓存恢复UI状态
     */
    private void setCache() { // 从缓存恢复UI状态
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT); // 获取通道选择缓存

        channelShowChange(); // 更新通道显示模式
        boolean wavSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_WAV); // 获取WAV选中状态
        boolean csvSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_CSV); // 获取CSV选中状态
        boolean binSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_BIN); // 获取BIN选中状态
        boolean pictureSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_PICTURE); // 获取截图选中状态
        boolean sessionSelect = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_SESSION); // 获取会话选中状态

        checkWAV.setChecked(wavSelect); // 设置WAV复选框状态
        checkCSV.setChecked(csvSelect); // 设置CSV复选框状态
        checkBIN.setChecked(binSelect); // 设置BIN复选框状态
        checkPicture.setChecked(pictureSelect); // 设置截图复选框状态
        checkSession.setChecked(sessionSelect); // 设置会话复选框状态

        viewChannel.setChangeListener(null, null); // 临时移除监听器避免触发
        viewChannel.setChecked(channelSelect); // 设置通道选中状态
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 恢复监听器
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1); // 获取Ch1开启状态
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2); // 获取Ch2开启状态
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3); // 获取Ch3开启状态
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4); // 获取Ch4开启状态
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5); // 获取Ch5开启状态
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6); // 获取Ch6开启状态
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7); // 获取Ch7开启状态
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8); // 获取Ch8开启状态

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 2通道模式
            channelShow[0] = ch1; // 设置Ch1显示
            channelShow[1] = ch2; // 设置Ch2显示
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) { // 4通道模式
            channelShow[0] = ch1; // 设置Ch1显示
            channelShow[1] = ch2; // 设置Ch2显示
            channelShow[2] = ch3; // 设置Ch3显示
            channelShow[3] = ch4; // 设置Ch4显示
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) { // 8通道模式
            channelShow[0] = ch1; // 设置Ch1显示
            channelShow[1] = ch2; // 设置Ch2显示
            channelShow[2] = ch3; // 设置Ch3显示
            channelShow[3] = ch4; // 设置Ch4显示
            channelShow[4] = ch5; // 设置Ch5显示
            channelShow[5] = ch6; // 设置Ch6显示
            channelShow[6] = ch7; // 设置Ch7显示
            channelShow[7] = ch8; // 设置Ch8显示
        }
        TChan.foreachCh1ToR8(chan -> { // 遍历所有通道
            if (TChan.isMath(chan)) { // 如果是Math通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 获取Math选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 获取用户添加状态
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置Math通道显示
            }
            if (TChan.isRef(chan)) { // 如果是Ref通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 获取Ref选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 获取用户添加状态
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置Ref通道显示
            }
        });
        viewChannel.getViewChannelMultipleChoice().unCheckAll(); // 取消所有多选

        setChannelShow(); // 更新通道显示
        restorePath(); // 恢复保存路径
        String fileName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_NAME); // 获取保存文件名缓存
        if (fileName.isEmpty()) { // 如果文件名为空
            fileName = Tools.generateName(); // 自动生成文件名
        }
        saveNameEdit.setText(fileName); // 设置文件名
//        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM);
        txtSuffixNum.setEnabled(true); // 启用序号文本
        checkFileNameAdd.setChecked(true); // 默认选中添加序号
        int stopCondition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_CONDITION); // 获取停止条件缓存
        rgStopCondition.setSelectedIndex(stopCondition); // 设置停止条件选中项
        onCheckChangedListener.onClick(rgStopCondition,rgStopCondition.getSelected()); // 触发停止条件变化
        int interval = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_Interval); // 获取时间间隔缓存
        intervalGroup.setSelectedIndex(interval); // 设置时间间隔选中项
        int saveMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_MODE); // 获取保存模式缓存
        saveModeGroup.setSelectedIndex(saveMode); // 设置保存模式选中项
        String stopTime = CacheUtil.get().getString(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME); // 获取停止时间缓存
        if(!stopTime.equals("")){ // 如果停止时间不为空
            topViewStopTimeSelector.setTimeSelectorByString(stopTime); // 设置停止时间
        }else { // 如果停止时间为空
            topViewStopTimeSelector.setNowTime(); // 设置为当前时间
        }
        String stopFrame = CacheUtil.get().getString(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_FRAME); // 获取停止帧数缓存
        if(!stopFrame.equals("")){ // 如果停止帧数不为空
            frameStopText.setText(stopFrame); // 设置停止帧数
        }else { // 如果停止帧数为空
            frameStopText.setText("0000001"); // 设置默认帧数
        }
    }

    /**
     * 设置信号源
     * @param sourceIndex 信号源索引
     */
    private void setSource(int sourceIndex) { // 设置信号源
        int ch = sourceIndex; // 通道索引
        if (sourceIndex == 24) { // 如果是自动选择
            ch = ChannelFactory.getChActivate(); // 获取当前激活通道
            if (!ChannelFactory.isChOpen(ch)) { // 如果通道未开启
                ch = -1; // 设为无效通道
            }
        }
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(sourceIndex)); // 缓存信号源
        CursorManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch)); // 设置光标通道颜色
        MeasureManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch)); // 设置测量通道颜色
    }

    /**
     * 通道单项选择监听器
     */
    TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 通道单项选择监听
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) { // 选中项变化回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
//            Command.get().getStorage().Save_Source(checkedIndex, false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(checkedIndex)); // 缓存通道选择
        }
    };

    /**
     * 通道多选测试监听器
     */
    TopViewChannelMultipleChoice.onTestListener onChannelItemTestListener = new TopViewChannelMultipleChoice.onTestListener() { // 通道多选监听
        @Override
        public void onTest(CheckBox checkBox) { // 多选变化回调
            List<Integer> lisSelects = viewChannel.getViewChannelMultipleChoice().getSelectChannel(); // 获取选中通道列表
            boolean showBin = true; // 是否显示BIN选项
            for (Integer chanIdx : lisSelects) { // 遍历选中通道
                if (ChannelFactory.isMathCh(chanIdx) || ChannelFactory.isRefCh(chanIdx)) { // 如果是Math或Ref通道
                    showBin = false; // 不显示BIN选项
                    break; // 跳出循环
                }
            }
        }
    };


    /**
     * 设置通道显示状态
     */
    private void setChannelShow() { // 设置通道显示
        viewChannel.setItemVisible(channelShow, true); // 设置通道可见性
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知通道可见性变化
    }


    /**
     * 获取光标详情
     * @return 测量详情接口（当前返回null）
     */
    public IMeasureDetail getCursorDetail() { // 获取光标详情
        return null; // 返回null
    }

    /**
     * 设置详情消息监听器
     * @param onDetailSendMsgListener 详情消息监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息监听器

    }


    /**
     * 通道颜色选择事件消费者
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 通道颜色选择消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收颜色信息
            if (colorInfo.isEmpty()) return; // 空信息则忽略
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 打印颜色信息日志
            String[] info = colorInfo.split(";"); // 拆分颜色信息
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色字符串
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道颜色
        }
    };


    /**
     * 自动保存按钮状态事件消费者
     */
    private Consumer<Boolean> consumerAutoSaveTask = new Consumer<Boolean>() { // 自动保存按钮状态消费者
        @Override
        public void accept(Boolean setState) throws Throwable { // 接收按钮状态
            requireActivity().runOnUiThread(new Runnable() { // 切换到UI线程
                @Override
                public void run() { // UI线程执行
                    if(setState){ // 如果设置为已停止
                        onStopRefresh(); // 刷新停止状态UI
                    }
                }
            });

        }
    };

    /**
     * 停止后刷新UI状态，恢复所有控件为可编辑
     */
    private void onStopRefresh(){ // 停止后刷新UI
        viewChannel.setReadOnly(true); // 通道选择设为只读（可编辑）
        topViewStartTimeSelector.setReadOnly(true); // 开始时间设为只读
        rgStopCondition.setReadOnly(true); // 停止条件设为只读
        topViewStopTimeSelector.setReadOnly(true); // 停止时间设为只读
        rgStopCondition.setReadOnly(true); // 停止条件设为只读
        intervalGroup.setReadOnly(true); // 时间间隔设为只读
        saveModeGroup.setReadOnly(true); // 保存模式设为只读
//        saveTypeGroup.setEnabled(true);
        for(int i=0;i<multiSaveType.getChildCount();i++){ // 遍历保存类型子控件
            View  child = multiSaveType.getChildAt(i); // 获取子控件
            if(child instanceof  CheckBox){ // 如果是复选框
                CheckBox cb = (CheckBox) child; // 转为复选框
                if(cb.isChecked()){ // 如果已选中
                    cb.setClickable(true); // 设为可点击
                }else{ // 如果未选中
                    cb.setEnabled(true); // 设为可用
                }
            }
        }
        btnBrowse.setEnabled(true); // 浏览按钮设为可用
        txtSuffixNum.setEnabled(true); // 序号文本设为可用
        spinner.setReadOnly(true); // 路径选择器设为只读
        saveNameEdit.setEnabled(true); // 文件名编辑框设为可用
        saveNameEdit.setClickable(true); // 文件名编辑框设为可点击
        startBtn.setVisibility(View.VISIBLE); // 显示开始按钮
        stopBtn.setVisibility(View.GONE); // 隐藏停止按钮
        topViewStartTimeSelector.setNowTime(); // 重置开始时间为当前时间
        startBtn.setEnabled(true); // 开始按钮设为可用
    }

    /**
     * 自动保存停止按钮状态事件消费者
     */
    private Consumer<Boolean> consumerAutoSaveTaskStopButton = new Consumer<Boolean>() { // 停止按钮状态消费者
        @Override
        public void accept(Boolean setState) throws Throwable { // 接收按钮状态
            requireActivity().runOnUiThread(new Runnable() { // 切换到UI线程
                @Override
                public void run() { // UI线程执行
                    Log.d(TAG, "runAutoTaskSaveButtonState: " +setState); // 打印按钮状态日志
                    if(setState){ // 如果设为可用
                        stopBtn.setEnabled(true); // 停止按钮设为可用
                    }else { // 如果设为禁用
                        stopBtn.setEnabled(false); // 停止按钮设为禁用
                    }
                }
            });

        }
    };

    /**
     * 自动保存截图事件消费者
     */
    private Consumer<Boolean> consumerAutoSaveScreenShot = new Consumer<Boolean>() { // 自动保存截图消费者
        @Override
        public void accept(Boolean setState) throws Throwable { // 接收截图事件
            ((MainActivity) getActivity()).autoSaveScreenShot(); // 调用主Activity自动保存截图

        }
    };

    /**
     * 右侧通道变化事件消费者
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 通道变化消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收通道变化消息
            TChan.foreachChan(chan -> { // 遍历所有通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 获取通道开启状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 更新通道显示状态
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * 右侧其他设置变化事件消费者
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 其他设置变化消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收其他设置变化消息
            TChan.foreachMath(chan -> { // 遍历Math通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 获取Math选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 获取用户添加状态
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新Math通道显示
            });
            TChan.foreachRef(chan -> { // 遍历Ref通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 获取Ref选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 获取用户添加状态
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新Ref通道显示
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * Ref通道变化事件消费者
     */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // Ref变化消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收Ref变化消息
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber()); // 获取用户添加状态
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser; // 更新Ref通道显示
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * 切换通道显示模式为多选
     */
    public void channelShowChange() { // 切换通道显示模式
        viewChannel.setAllSelectShow(true);//csv时显示多选控件
        setChannelShow(); // 更新通道显示
    }

    /**
     * 全局点击事件监听器
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击事件监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            if (v.getId() == startBtn.getId()) { // 点击开始按钮
                boolean startResult = validAutoSaveTaskParameter(); // 验证参数并启动
                if (startResult) { // 如果启动成功
                    viewChannel.setReadOnly(false); // 通道选择设为只读
                    topViewStartTimeSelector.setReadOnly(false); // 开始时间设为只读
                    topViewStopTimeSelector.setReadOnly(false); // 停止时间设为只读
                    rgStopCondition.setReadOnly(false); // 停止条件设为只读
                    intervalGroup.setReadOnly(false); // 时间间隔设为只读
                    saveModeGroup.setReadOnly(false); // 保存模式设为只读
                    btnBrowse.setEnabled(false); // 浏览按钮设为禁用
                    txtSuffixNum.setEnabled(false); // 序号文本设为禁用
                    spinner.setReadOnly(false); // 路径选择器设为只读
                    saveNameEdit.setEnabled(false); // 文件名编辑框设为禁用
                    startBtn.setVisibility(View.GONE); // 隐藏开始按钮
                    stopBtn.setVisibility(View.VISIBLE); // 显示停止按钮
                    stopBtn.setEnabled(true); // 停止按钮设为可用
                    for(int i=0;i<multiSaveType.getChildCount();i++){ // 遍历保存类型子控件
                        View  child = multiSaveType.getChildAt(i); // 获取子控件
                        if(child instanceof  CheckBox){ // 如果是复选框
                            CheckBox cb = (CheckBox) child; // 转为复选框
                            if(cb.isChecked()){ // 如果已选中
                                cb.setClickable(false); // 设为不可点击
                            }else{ // 如果未选中
                                cb.setEnabled(false); // 设为禁用
                            }
                        }
                    }
                }
            } else if ((v.getId() == stopBtn.getId())) { // 点击停止按钮
                viewChannel.setReadOnly(true); // 通道选择设为可编辑
                topViewStartTimeSelector.setReadOnly(true); // 开始时间设为可编辑
                rgStopCondition.setReadOnly(true); // 停止条件设为可编辑
                topViewStopTimeSelector.setReadOnly(true); // 停止时间设为可编辑
                rgStopCondition.setReadOnly(true); // 停止条件设为可编辑
                intervalGroup.setReadOnly(true); // 时间间隔设为可编辑
                saveModeGroup.setReadOnly(true); // 保存模式设为可编辑
                btnBrowse.setEnabled(true); // 浏览按钮设为可用
                txtSuffixNum.setEnabled(true); // 序号文本设为可用
                spinner.setReadOnly(true); // 路径选择器设为可编辑
                saveNameEdit.setEnabled(true); // 文件名编辑框设为可用
                saveNameEdit.setClickable(true); // 文件名编辑框设为可点击
                startBtn.setVisibility(View.VISIBLE); // 显示开始按钮
                stopBtn.setVisibility(View.GONE); // 隐藏停止按钮
                AutoSave.getInstance().stop(); // 停止自动保存
                topViewStartTimeSelector.setNowTime(); // 重置开始时间
                for(int i=0;i<multiSaveType.getChildCount();i++){ // 遍历保存类型子控件
                    View  child = multiSaveType.getChildAt(i); // 获取子控件
                    if(child instanceof  CheckBox){ // 如果是复选框
                        CheckBox cb = (CheckBox) child; // 转为复选框
                        if(cb.isChecked()){ // 如果已选中
                            cb.setClickable(true); // 设为可点击
                        }else{ // 如果未选中
                            cb.setEnabled(true); // 设为可用
                        }
                    }
                }
            }else if (v.getId() == btnBrowse.getId()) { // 点击浏览按钮
                handleBrowseClick(); // 处理浏览点击
            }else if (v.getId() == txtSuffixNum.getId()) { // 点击序号文本
                dialogKeyBoard.setDecimalData(7, IDigits.DIGITS_10, onNumSubFixListener); // 弹出数字键盘
            }else if (v.getId() == frameStopText.getId()) { // 点击帧数文本
                dialogKeyBoard.setDecimalData(7, IDigits.DIGITS_10, onNumFrameListener); // 弹出数字键盘
            }else if(v.getId() == checkWAV.getId()){ // 点击WAV复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_WAV, String.valueOf(checkWAV.isChecked())); // 缓存WAV选中状态
            }else if(v.getId() == checkCSV.getId()){ // 点击CSV复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_CSV, String.valueOf(checkCSV.isChecked())); // 缓存CSV选中状态
            }else if(v.getId() == checkBIN.getId()){ // 点击BIN复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.WAVE_TYPE_BIN, String.valueOf(checkBIN.isChecked())); // 缓存BIN选中状态
            }else if(v.getId() == checkPicture.getId()){ // 点击截图复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_PICTURE, String.valueOf(checkPicture.isChecked())); // 缓存截图选中状态
            }else if(v.getId() == checkSession.getId()){ // 点击会话复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_TYPE + CacheUtil.SAVE_TYPE_SESSION, String.valueOf(checkSession.isChecked())); // 缓存会话选中状态
            }
        }
    };

    /**
     * 获取之前保存的目录列表
     * @return 目录文件Bean列表
     */
    private ArrayList<FileBean> getPreviousDirectory() { // 获取之前保存的目录列表
        return pathSet.getReverseList(); // 返回倒序路径列表
    }

    /**
     * 处理浏览按钮点击，打开文件选择器
     */
    private void handleBrowseClick() { // 处理浏览按钮点击
        String spinnerSelectPath= spinner.getSelectItem(); // 获取当前选中路径
        String disPlay = spinner.getDisPlaySelectItem(); // 获取显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象
        if(!file.exists() || !file.isDirectory()){ // 如果路径不存在或不是目录
            spinnerSelectPath = "/storage/emulated/0"; // 默认内部存储路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 默认显示名
        }
        fileSelector.buildSaveFileSelector(spinnerSelectPath, disPlay, this, context); // 构建保存文件选择器
    }

    /**
     * 处理添加路径到集合
     * @param pathStr 要添加的路径
     * @return true表示是新路径，false表示已存在
     */
    private boolean handleAddPath(FileBean pathStr) { // 处理添加路径
        boolean canAdd = true; // 是否可添加标志
        FileBean temp = null; // 临时存储已存在的路径
        for (FileBean fileBean : pathSet) { // 遍历已有路径
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径已存在
                temp = fileBean; // 记录已存在的路径
                canAdd = false; // 标记不可添加
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的路径
            pathSet.remove(temp); // 移除旧的
        }
        return canAdd; // 返回是否可添加
    }

    /**
     * 将保存路径保存到缓存
     */
    public void savePathToCache() { // 保存路径到缓存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH, // 缓存显示路径
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH, // 缓存绝对路径
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT, spinner.getSelectItem()); // 缓存当前选中路径
    }

    /**
     * 单选组变化监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调
        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            onCheckChanged(view, item, false, true, true); // 处理选中变化
        }
    };

    /**
     * 处理单选组选中变化
     * @param view 单选组视图
     * @param item 选中的项
     * @param isFromEventBus 是否来自事件总线
     * @param isUser 是否用户操作
     * @param setCache 是否设置缓存
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus, boolean isUser, boolean setCache) { // 处理选中变化
        if (view.getId() == R.id.stopRadioGroup) { // 停止条件变化
            if (item.getIndex() == 1) { // 按时间停止
                topViewStopTimeSelector.setVisibility(View.VISIBLE); // 显示停止时间选择器
                frameStopText.setVisibility(View.GONE); // 隐藏帧数文本
            } else if (item.getIndex() == 2) { // 按帧数停止
                frameStopText.setVisibility(View.VISIBLE); // 显示帧数文本
                topViewStopTimeSelector.setVisibility(View.GONE); // 隐藏停止时间选择器
            } else if (item.getIndex() == 0) { // 无限制
                frameStopText.setVisibility(View.GONE); // 隐藏帧数文本
                topViewStopTimeSelector.setVisibility(View.GONE); // 隐藏停止时间选择器
            }
            RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_AUTOSAVE_STATE,item.getIndex()); // 同步外部自动保存状态
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_CONDITION,String.valueOf(item.getIndex())); // 缓存停止条件
        }else if(view.getId() == R.id.interval){ // 时间间隔变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_Interval,String.valueOf(item.getIndex())); // 缓存时间间隔
        }else if(view.getId() == R.id.saveMode){ // 保存模式变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SAVE_MODE,String.valueOf(item.getIndex())); // 缓存保存模式
        }
    }

    /**
     * 编辑框点击监听器
     */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() { // 编辑框点击监听
        @Override
        public void onClickEdit(TopViewEdit v, String text) { // 编辑框点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            if (v.getId() == saveNameEdit.getId()) { // 点击文件名编辑框
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() { // 弹出文本键盘
                    @Override
                    public void onDismiss(String result) { // 键盘关闭回调
                        saveNameEdit.setText(result); // 设置文件名
                        txtSuffixNum.setText("0000000"); // 重置序号
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM + CacheUtil.SAVE_TYPE_AUTOSAVE, "0000000"); // 缓存序号
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_NAME, result); // 缓存文件名
                    }
                });
            }
        }
    };

    /**
     * 自动保存监听器，处理保存过程的各个阶段回调
     */
    private AutoSave.IAutoSaveListener autoSaveListener = new AutoSave.IAutoSaveListener() { // 自动保存监听器

        private ScreenControls screenControls = null; // 屏幕控制实例
        private int lockFlag = 0; // 锁屏标志
        @Override
        public void onBegin() { // 保存开始回调
            if(screenControls == null){ // 如果屏幕控制未初始化
                screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            }
            RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,false); // 通知按钮状态为运行中
            getActivity().runOnUiThread(()->{ // 切换到UI线程
                ((MainActivity)getActivity()).sendAutoSave(true); // 发送自动保存开始信号
            });
        }

        @Override
        public void onEnd() { // 保存结束回调
            int errCode = AutoSave.getInstance().getErrCode(); // 获取错误码
            getActivity().runOnUiThread(()->{ // 切换到UI线程
                if(errCode != 0){ // 如果有错误
                    Log.i(TAG,"errCode:" + errCode); // 打印错误码
                }
                if(screenControls.isLockScreen(lockFlag)) { // 如果屏幕被锁定
                    screenControls.unLockScreen(lockFlag); // 解锁屏幕
                }
                lockFlag = 0; // 重置锁屏标志
                ((MainActivity)getActivity()).sendAutoSave(false); // 发送自动保存结束信号
            });
            RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true); // 通知按钮状态为已停止

        }

        @Override
        public void onSaveBefore(boolean bProgress) { // 保存前回调
            lockFlag = ScreenControls.LOCK_SCREEN; // 设置锁屏标志
            if(bProgress){ // 如果需要显示进度
                lockFlag |= ScreenControls.LOCK_PROGRESS; // 添加进度锁屏标志
            }
            if(!screenControls.isLockScreen(lockFlag)) { // 如果屏幕未被锁定
                screenControls.lockScreen(lockFlag); // 锁定屏幕
            }
        }

        @Override
        public void onProgress(int val) { // 保存进度回调
            getActivity().runOnUiThread(()->{ // 切换到UI线程
                screenControls.setProgressValue(val); // 设置进度值
            });
        }

        @Override
        public void onSaveAfter(boolean bProgress) { // 保存后回调
            int flag = lockFlag; // 获取锁屏标志
            if(bProgress){ // 如果有进度
                flag = ScreenControls.LOCK_PROGRESS; // 只解锁进度
                lockFlag &= ~flag; // 从锁屏标志中移除进度标志
            }else{ // 如果没有进度
                getActivity().runOnUiThread(()->{ // 切换到UI线程
                    AutoSave autoSave = AutoSave.getInstance(); // 获取自动保存实例
                    onTextListener(String.format("%07d",autoSave.getSuffixCode())); // 更新序号显示
                });
            }
            if(screenControls.isLockScreen(flag)) { // 如果指定标志的屏幕被锁定
                screenControls.unLockScreen(flag); // 解锁屏幕
            }
        }

        @Override
        public void onPicture(String filePath, String fileName) { // 截图保存回调
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath); // 缓存截图路径
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,fileName); // 缓存截图文件名
            RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true); // 发送截图事件
        }

        @Override
        public HashMap<String, HashMap<String, String>> onCurCache() { // 获取当前缓存回调
            HashMap<String, HashMap<String, String>> map = new HashMap<>(); // 创建缓存映射
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap()); // 保存默认缓存
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap()); // 保存其他缓存
            return map; // 返回缓存映射
        }
    };

    /**
     * 验证自动保存任务参数并启动
     * @return true表示验证通过并已启动，false表示验证失败
     */
    public boolean validAutoSaveTaskParameter() { // 验证参数并启动自动保存
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(), context.getResources().getString(R.string.internal_storage))) { // 检查保存目录是否存在
            DToast.get().show(R.string.top_slip_save_wave_path_unable); // 提示路径不可用
            return false; // 返回验证失败
        }
        LocalDateTime startTime = topViewStartTimeSelector.getTime(); // 获取开始时间
        LocalDateTime stopTime = topViewStopTimeSelector.getTime(); // 获取停止时间
        int stopConditionType = rgStopCondition.getSelected().getIndex(); // 获取停止条件类型
        List<Integer> selectList = viewChannel.getAllSelectChannel();//保存CSV时选中的channel
        if(checkWAV.isChecked()){ // 如果选中WAV
            saveType.add(AutoSaveTaskCondition.SaveType.WAV.getCode()); // 添加WAV类型编码
        }
        if(checkCSV.isChecked()){ // 如果选中CSV
            saveType.add(AutoSaveTaskCondition.SaveType.CSV.getCode()); // 添加CSV类型编码
        }
        if(checkBIN.isChecked()){ // 如果选中BIN
            saveType.add(AutoSaveTaskCondition.SaveType.BIN.getCode()); // 添加BIN类型编码
        }
        if(checkPicture.isChecked()){ // 如果选中截图
            saveType.add(AutoSaveTaskCondition.SaveType.PICTURE.getCode()); // 添加截图类型编码
        }
        if(checkSession.isChecked()){ // 如果选中会话
            saveType.add(AutoSaveTaskCondition.SaveType.SESSION.getCode()); // 添加会话类型编码
        }
        List<AutoSaveTaskCondition.SaveType> typeList = saveType.stream() // 将编码列表转换为枚举列表
                .map(AutoSaveTaskCondition.SaveType::fromCode) // 映射编码到枚举
                .collect(Collectors.toList()); // 收集为列表
        saveType.clear(); // 清空临时列表
        if (typeList == null || typeList.size() <= 0) { // 如果没有选择保存类型
            DToast.get().show(R.string.msgTopSaveTypeNotSelect); // 提示未选择保存类型
            return false; // 返回验证失败
        }
        if(typeList.contains(AutoSaveTaskCondition.SaveType.WAV)|| typeList.contains(AutoSaveTaskCondition.SaveType.BIN)|| typeList.contains(AutoSaveTaskCondition.SaveType.CSV)){ // 如果包含WAV/BIN/CSV
            if (selectList == null || selectList.size() <= 0) { // 如果没有选择通道
                DToast.get().show(R.string.msgTopSaveCsvNotSelect); // 提示未选择通道
                return false; // 返回验证失败
            }
        }
        if(spinner.getSelectItem().isEmpty()){ // 如果保存路径为空
            DToast.get().show(R.string.top_slip_directory_save_to); // 提示选择保存目录
            return false; // 返回验证失败
        }
        if(!ifEnoughSpace()){ // 如果磁盘空间不足
            return false; // 返回验证失败
        }
        if ((startTime.isAfter(stopTime) || startTime.isEqual(stopTime))&& rgStopCondition.getSelected().getIndex()==1) { // 按时间停止时，停止时间必须晚于开始时间
            DToast.get().show(R.string.top_slip_stop_must_later_start); // 提示停止时间必须晚于开始时间
            return false; // 返回验证失败
        } else if((startTime.isBefore(stopTime)&& stopConditionType==1) || stopConditionType==2 || stopConditionType == 0){ // 参数验证通过
            String stopValue = ""; // 停止条件值
            if(stopConditionType == 1){ // 按时间停止
                stopValue = topViewStopTimeSelector.getTime().toString(); // 获取停止时间字符串
            }else if(stopConditionType==2){ // 按帧数停止
                stopValue  = frameStopText.getText().toString(); // 获取停止帧数
            }
            AutoSave autoSave = AutoSave.getInstance(); // 获取自动保存实例
            List<IChannel> channels = new ArrayList<>(); // 创建通道列表
            for(Integer integer:selectList){ // 遍历选中通道
                channels.add(ChannelFactory.getValidChannel(integer)); // 添加有效通道
            }
            autoSave.setChannels(channels); // 设置通道
            autoSave.setStartDateTime(topViewStartTimeSelector.getTime()); // 设置开始时间
            autoSave.setStopCondition(stopConditionType); // 设置停止条件类型
            if(stopConditionType == AutoSave.STOP_CONDITION_TIME){ // 按时间停止
                autoSave.setStopConditionTime(LocalDateTime.parse(stopValue)); // 设置停止时间
            }else if(stopConditionType == AutoSave.STOP_CONDITION_FRAMES){ // 按帧数停止
                autoSave.setStopConditionFrames(Integer.parseInt(stopValue)); // 设置停止帧数
            }
            autoSave.setFrameInterval(AutoSaveTaskCondition.TimeInterval.fromCode(intervalGroup.getSelected().getIndex()).getTime()); // 设置帧间隔
            autoSave.setSaveMode(saveModeGroup.getSelected().getIndex()); // 设置保存模式
            int type = 0; // 保存类型位掩码
            for(AutoSaveTaskCondition.SaveType t:typeList){ // 遍历保存类型
                type |= 1 << t.getCode(); // 按位或组合类型
            }
            autoSave.setSaveType(type); // 设置保存类型
            autoSave.setFilePath(spinner.getSelectItem().trim()); // 设置保存路径
            autoSave.setPrefixName(saveNameEdit.getText().trim()); // 设置文件名前缀
            autoSave.setSuffixCode(Integer.parseInt(txtSuffixNum.getText().toString().trim())); // 设置后缀序号
            autoSave.setAutoSaveListener(autoSaveListener); // 设置自动保存监听器
            if(!autoSave.isRun()) { // 如果自动保存未在运行
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip(); // 隐藏所有滑动对话框
                autoSave.start(); // 启动自动保存
            }
        }
        return true; // 返回验证通过
    }

    /**
     * 后缀序号数字键盘关闭监听器
     */
    private TopDialogNumberKeyBoard.OnDismissListener  onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 序号键盘关闭监听
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(result); // 处理序号输入
        }
    };

    /**
     * 处理后缀序号文本输入
     * @param text 输入的序号文本
     */
    private void onTextListener(String text) { // 处理序号输入
        int inputSuffix = Integer.parseInt(text.trim()); // 解析输入为整数
        if(inputSuffix>AutoSave.MAX_SUFFIXCODE){ // 如果超过最大值
            inputSuffix = AutoSave.MAX_SUFFIXCODE; // 限制为最大值
        }
        text = String.valueOf(inputSuffix); // 转为字符串
        text = KeyBoardNumberUtil.toBits(text,7); // 格式化为7位数字
        txtSuffixNum.setText(text); // 设置序号文本
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM + CacheUtil.SAVE_TYPE_AUTOSAVE, text); // 缓存序号
    }

    /**
     * 帧数数字键盘关闭监听器
     */
    private TopDialogNumberKeyBoard.OnDismissListener  onNumFrameListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 帧数键盘关闭监听
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onFrameTextListener(result); // 处理帧数输入
        }
    };

    /**
     * 处理帧数文本输入
     * @param text 输入的帧数文本
     */
    private void onFrameTextListener(String text) { // 处理帧数输入
        if(text.equals("0000000")){ // 如果输入为0
            DToast.get().show(R.string.inputCorrectNframe); // 提示输入正确的帧数
            return; // 返回
        }
        int inputStopCount = Integer.parseInt(text.trim()); // 解析输入为整数
        if(inputStopCount>AutoSave.MAX_SUFFIXCODE){ // 如果超过最大值
            inputStopCount = AutoSave.MAX_SUFFIXCODE; // 限制为最大值
        }
        text = String.valueOf(inputStopCount); // 转为字符串
        text = KeyBoardNumberUtil.toBits(text,7); // 格式化为7位数字
        Log.d(TAG, "onFrameTextListener: "+text); // 打印帧数日志
        frameStopText.setText(text); // 设置帧数文本
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_FRAME, text); // 缓存帧数
    }

    /**
     * 获取最终文件名（含序号）
     * @return 最终文件名字符串
     */
    private String getFinaleName() { // 获取最终文件名
        String finalName = saveNameEdit.getText(); // 获取基础文件名
        if(checkFileNameAdd.isChecked()) { // 如果选中添加序号
            finalName = finalName + "_" + txtSuffixNum.getText(); // 拼接序号
        }
        return finalName; // 返回最终文件名
    }

    /**
     * 文件名添加序号复选框变化监听器
     */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 复选框变化监听
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == checkFileNameAdd.getId()) { // 文件名添加序号复选框
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK + CacheUtil.SAVE_TYPE_AUTOSAVE, String.valueOf(isChecked)); // 缓存选中状态
                txtSuffixNum.setEnabled(isChecked); // 设置序号文本可用性
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_AUTOSAVE + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked); // 通知序号更新
            }
        }
    };

    /**
     * 从缓存恢复保存路径
     */
    private void restorePath() { // 恢复保存路径
        pathSet.clear(); // 清空路径集合
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH); // 获取显示路径缓存
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH); // 获取绝对路径缓存
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT); // 获取当前路径缓存
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示路径列表
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建文件Bean列表
        FileBean currentBean = new FileBean(); // 创建当前路径Bean
        for (int i = 0; i < abPathCacheList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue; // 跳过不存在的路径
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abPathCacheList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(pathCacheList.get(i)); // 设置显示名称
            Logger.i(TAG, "abPath= " + abPathCacheList.get(i) + " ,disPath= " + pathCacheList.get(i) + " ,currnt= " + currentPath); // 打印路径日志
            if(abPathCacheList.get(i).equals(currentPath)) { // 如果是当前路径
                currentBean.setPath(abPathCacheList.get(i)); // 设置当前路径绝对路径
                currentBean.setDisplayName(pathCacheList.get(i)); // 设置当前路径显示名称
            }
            dataList.add(fileBean); // 添加到列表
        }
        pathSet.addAll(dataList); // 添加所有路径到集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉选择器数据
    }

    /**
     * 检查磁盘空间是否充足
     * @return true表示空间充足，false表示空间不足
     */
    public boolean ifEnoughSpace(){ // 检查磁盘空间
        File saveDir = new File(spinner.getSelectItem()); // 获取保存目录
        long freeSpace = saveDir.getFreeSpace(); // 获取可用空间
        long estimatedSize = 10 * 1024 * 1024L *1024; // 阈值：10GB
        if(freeSpace < estimatedSize){ // 如果可用空间不足
            DToast.get().show(R.string.no_storage_space); // 提示空间不足
            return false; // 返回空间不足
        }else { // 如果空间充足
            return true; // 返回空间充足
        }
    }

    /**
     * 路径下拉选择器项选中监听器
     */
    TopViewSpinner.onItemSelectListener onItemSelectListener = fileBean -> { // 路径选中监听
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_AUTO_SAVE_PATH_CURRENT, fileBean.getPath()); // 缓存当前选中路径
//        DToast.get().show(str);
        addSelectToPathSet(fileBean); // 添加选中路径到集合
    };

    /**
     * 添加选中路径到路径集合
     * @param pathStr 选中的路径
     */
    private void addSelectToPathSet(FileBean pathStr) { // 添加路径到集合
        handleAddPath(pathStr); // 处理路径添加
        pathSet.add(pathStr); // 添加到集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉选择器
        savePathToCache(); // 保存路径到缓存
    }
}
