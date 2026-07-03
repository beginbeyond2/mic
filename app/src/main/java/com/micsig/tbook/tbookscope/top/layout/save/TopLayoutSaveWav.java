package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.graphics.drawable.Drawable; // 导入Drawable图形类
import android.os.Bundle; // 导入Bundle状态保存类
import android.os.Handler; // 导入Handler消息处理类
import android.os.Message; // 导入Message消息类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.view.WindowManager; // 导入窗口管理器类
import android.widget.Button; // 导入按钮控件类
import android.widget.CheckBox; // 导入复选框控件类
import android.widget.CompoundButton; // 导入复合按钮基类
import android.widget.RadioButton; // 导入单选按钮类
import android.widget.TextView; // 导入文本视图控件类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源引用类
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel; // 导入确认取消对话框类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息转UI类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入8通道Ref消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxBus事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举类
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘对话框类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard; // 导入文本键盘对话框类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.util.FileSelector; // 导入文件选择器类
import com.micsig.tbook.ui.FixedSizeHashSet; // 导入固定大小HashSet类
import com.micsig.tbook.ui.MMainMenuChannel; // 导入主菜单通道控件类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入自定义下拉框类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean类
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道视图类
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice; // 导入通道多选视图类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举工具类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean实体类
import com.molihuan.pathselector.fragment.impl.PathSelectFragment; // 导入路径选择Fragment实现类
import com.molihuan.pathselector.utils.DToastDialog; // 导入路径选择器Toast对话框类


import java.io.File; // 导入文件类
import java.util.ArrayList; // 导入动态数组类
import java.util.HashSet; // 导入HashSet类
import java.util.Iterator; // 导入迭代器类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * Created by Administrator on 2017/4/5.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 保存子页面 → WAV波形保存（Wav Save）     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供WAV波形保存功能的UI界面和逻辑控制，包括通道选择、保存路径管理、   │
 * │          文件名编辑与序号递增，通过SaveManage执行WAV文件保存，                │
 * │          支持通道可见性动态更新、Ref通道保存、快速保存等功能                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，通过MMainMenuChannel实现通道选择，                    │
 * │          通过TopViewSpinner管理保存路径列表，通过TopViewEdit编辑文件名，       │
 * │          通过SaveManage执行WAV保存，通过EventFactory监听保存进度，             │
 * │          通过RxBus订阅通道状态变化事件                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(缓存恢复) → UI控件(初始化状态) →                        │
 * │          RxBus(通道状态变化) → channelShow(通道可见性更新) →                  │
 * │          用户点击保存 → doSaveWave(执行保存) →                                │
 * │          SaveManage.allSaveEntrance(保存WAV) → 回调(成功/失败) →             │
 * │          autoAddSuffixNum(序号递增)                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：MMainMenuChannel, TopViewSpinner, TopViewEdit, FileSelector,      │
 * │          CacheUtil, SaveManage, ScreenControls, Command, RxBus,            │
 * │          EventFactory, ChannelFactory, TChan, TopDialogTextKeyBoard,        │
 * │          TopDialogNumberKeyBoard, DialogOkCancel, FileUtils                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在保存菜单选择"WAV"Tab时显示此页面，                            │
 * │          选择通道和保存路径后点击保存按钮执行WAV波形保存操作                    │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

public class TopLayoutSaveWav extends Fragment {
    /** 日志标签 */
    private static final String TAG = "TopLayoutSave"; // 日志标签字符串
    /** 保存到本地存储的常量索引 */
    public static final int SAVEINLOCAL = 0;// Tools.SaveType_LOCAL // 保存到本地
    /** 保存到U盘的常量索引 */
    public static final int SAVEINUDISK = 1;// Tools.SaveType_UDISK // 保存到U盘

    /** Fragment所在的上下文环境 */
    private Context context; // Activity上下文
    /** 保存位置单选组控件 */
    private TopViewRadioGroup saveTo; // 保存位置单选组
    /** 文件名编辑框控件 */
    private TopViewEdit saveNameEdit; // 保存名称编辑框
    /** 通道选择控件 */
    private MMainMenuChannel viewChannel; // 通道选择控件
    /** 文本键盘对话框和文件选择器文本键盘对话框 */
    private TopDialogTextKeyBoard layoutTextKeyBoard, fileSelectorTextKeyBoard; // 文本键盘对话框引用
    /** WAV保存消息对象 */
    private TopMsgSaveWave topMsgSaveWave; // WAV保存消息对象
    /** 保存按钮 */
    private Button btnSave; // 保存按钮
    /** 保存路径下拉框控件 */
    private TopViewSpinner spinner; // 路径选择下拉框
    /** 详情消息发送监听器 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息监听器

    /** 保存测试按钮（调试用） */
    private Button saveTest; // 保存测试按钮
    /** 路径选择Fragment */
    private PathSelectFragment selector; // 路径选择Fragment引用

    /** 浏览按钮 */
    private Button btnBrowse; // 浏览按钮
    /** 文件名添加序号复选框 */
    private CheckBox checkFileNameAdd; // 文件名添加序号复选框
    /** 序号显示文本控件 */
    private TextView txtSuffixNum; // 序号文本显示
    /** WAV保存路径集合，固定大小为10，用于MRU路径列表 */
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10); // 固定大小路径集合
    /** 数字键盘对话框 */
    protected TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框引用

    /** 窗口管理器 */
    public WindowManager windowManager; // 窗口管理器

    /** 路径选择器Toast对话框 */
    public DToastDialog dToastdialog = new DToastDialog(); // 路径选择器Toast
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    /** 通道可见性数组，28个通道（8CH+8Math+8Ref+4Seg） */
    private boolean[] channelShow = { // 通道可见性数组
            false, false, false, false, false, false, false, false, // CH1-CH8
            false, false, false, false, false, false, false, false, // Math1-Math8
            false, false, false, false, false, false, false, false, // Ref1-Ref8
            false, false, false, false // Seg1-Seg4
    };

    /** 分段保存消息对象 */
    private TopMsgSaveSegments msgSaveSegments = new TopMsgSaveSegments(); // 分段保存消息
    /** 确认取消对话框 */
    private DialogOkCancel dialogOk; // 确认取消对话框引用

    /** 文件选择器 */
    private FileSelector fileSelector ; // 文件选择器实例
    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的WAV保存布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_wav, container, false); // 填充WAV保存布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getContext(); // 获取Fragment上下文
        initView(view); // 初始化视图控件
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化所有视图控件，绑定监听器
     * @param view 根视图
     */
    private void initView(View view) {
        viewChannel = view.findViewById(R.id.chanSaveWave); // 获取通道选择控件
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 设置通道选择监听器

        saveNameEdit = (TopViewEdit) view.findViewById(R.id.saveName); // 获取文件名编辑框控件
        saveNameEdit.setOnClickEditListener(onClickEditListener); // 设置文件名编辑框点击监听器
        btnSave = view.findViewById(R.id.btn_save); // 获取保存按钮
        btnSave.setOnClickListener(onClickListener); // 设置保存按钮点击监听器
        saveTo = (TopViewRadioGroup) view.findViewById(R.id.saveTo); // 获取保存位置单选组控件
        saveTo.setData(R.string.save_saveto, R.array.saveTo, onCheckChangedListener); // 设置保存位置数据

        spinner = view.findViewById(R.id.topSpinner); // 获取路径下拉框控件
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置下拉框标题
                getPreviousDirectory(), R.layout.layout_item_for_save_directory, onItemSelectListener); // 设置路径数据和监听器
        btnBrowse = view.findViewById(R.id.btn_browse); // 获取浏览按钮
        btnBrowse.setOnClickListener(onClickListener); // 设置浏览按钮点击监听器
        txtSuffixNum = view.findViewById(R.id.txt_index_num); // 获取序号文本控件
        txtSuffixNum.setOnClickListener(onClickListener); // 设置序号文本点击监听器
        checkFileNameAdd = view.findViewById(R.id.check_file_name_add); // 获取序号复选框控件
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取全选图标Drawable
        checkFileNameAdd.setBackground(null); // 清除复选框背景
        checkFileNameAdd.setButtonDrawable(null); // 清除复选框默认按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界大小
        checkFileNameAdd.setCompoundDrawables(drawable, null, null, null); // 将图标设置到左侧
        checkFileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置复选框选中变化监听器


        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 获取文本键盘对话框

        topMsgSaveWave = new TopMsgSaveWave(new boolean[]{true, true, true}); // 创建WAV保存消息对象

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL); // 获取确认取消对话框
        fileSelector = new FileSelector(context,(selectedPath) -> { // 创建文件选择器
//            test(selectedPath);
            addPathToPathSet(selectedPath); // 选中路径后添加到路径集合
        });
    }

    /**
     * 获取WAV保存路径列表（逆序，最近使用的在前）
     * @return 路径列表
     */
    private ArrayList<FileBean> getPreviousDirectory() {
        return pathSet.getReverseList(); // 返回逆序路径列表
    }

    /**
     * 将选中的路径添加到路径集合，并更新UI和缓存
     * @param pathStr 选中的文件路径Bean
     */
    private void addPathToPathSet(FileBean pathStr) {
        handleAddPath(pathStr); // 处理路径添加（去重）
        pathSet.add(pathStr); // 添加到路径集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉框数据
        savePathToCache(); // 保存路径到缓存
    }

    /**
     * 处理路径添加逻辑，如果路径已存在则先移除旧的再添加新的（实现MRU效果）
     * @param pathStr 要添加的文件路径Bean
     * @return true表示路径是新添加的，false表示路径已存在（已移除旧的）
     */
    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true; // 是否可以添加标志
        FileBean temp = null; // 临时保存已存在的路径Bean
        for (FileBean fileBean : pathSet) { // 遍历路径集合
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径相同
                temp = fileBean; // 保存已存在的Bean
                canAdd = false; // 标记不可添加
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的路径
            pathSet.remove(temp); // 移除旧的路径
        }
        return canAdd; // 返回是否为新路径
    }

    /**
     * 将WAV保存路径信息保存到缓存
     */
    public void savePathToCache() {

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_WAV, // 保存显示名称路径列表
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 转换为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV, // 保存绝对路径列表
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 转换为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, spinner.getSelectItem()); // 保存当前选中路径
    }

    /**
     * 初始化事件控制，订阅RxBus事件和EventFactory观察者
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅右侧通道消息事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧其他消息事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅Ref通道变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令转UI事件
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKSAVE).subscribe(consumerMainBottomQuickSave); // 订阅快速保存事件
//        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);
        EventFactory.addEventObserver(EventFactory.EVENT_SAVEBIN_RUN, eventSaveBinObserver); // 添加保存进度事件观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择事件
//        setChannelShow();
    }

    /**
     * 从缓存恢复所有UI控件状态
     */
    private void setCache() {
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT); // 从缓存读取通道选择

        String waveName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_WAV); // 从缓存读取WAV文件名
        if (waveName.isEmpty()) { // 如果名称为空
            waveName = Tools.generateName(); // 自动生成名称
        }
        saveNameEdit.setText(waveName); // 设置文件名编辑框文本
        channelShowChange(); // 更新通道显示模式
        Command.get().getStorage().Save(channelSelect, SAVEINLOCAL, false); // 同步通道选择到硬件
        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false); // 同步文件名到硬件
        Command.get().getStorage().Save_Type(0, false); // 同步保存类型到硬件
        Command.get().getStorage().Save_ALLSegments(false, false); // 同步分段保存设置到硬件

        viewChannel.setChangeListener(null, null); // 临时移除监听器
        viewChannel.setChecked(channelSelect); // 设置通道选中状态
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 恢复监听器
        saveTo.clearCheck(); // 清除保存位置选中
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1); // 从缓存读取CH1开启状态
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2); // 从缓存读取CH2开启状态
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3); // 从缓存读取CH3开启状态
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4); // 从缓存读取CH4开启状态
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5); // 从缓存读取CH5开启状态
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6); // 从缓存读取CH6开启状态
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7); // 从缓存读取CH7开启状态
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8); // 从缓存读取CH8开启状态

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 如果是2通道型号
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) { // 如果是4通道型号
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
            channelShow[2] = ch3; // 设置CH3可见性
            channelShow[3] = ch4; // 设置CH4可见性
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) { // 如果是8通道型号
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
            channelShow[2] = ch3; // 设置CH3可见性
            channelShow[3] = ch4; // 设置CH4可见性
            channelShow[4] = ch5; // 设置CH5可见性
            channelShow[5] = ch6; // 设置CH6可见性
            channelShow[6] = ch7; // 设置CH7可见性
            channelShow[7] = ch8; // 设置CH8可见性
        }
        TChan.foreachCh1ToR8(chan -> { // 遍历CH1到Ref8所有通道
            if (TChan.isMath(chan)) { // 如果是Math通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 从缓存读取Math开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 从缓存读取用户添加标志
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置Math通道可见性
            }
            if (TChan.isRef(chan)) { // 如果是Ref通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 从缓存读取Ref开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 从缓存读取用户添加标志
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置Ref通道可见性
            }
        });
        setChannelShow(); // 更新通道显示
        viewChannel.getViewChannelMultipleChoice().unCheckAll(); // 取消所有多选

        restorePath(); // 恢复保存路径

        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_WAV); // 从缓存读取序号复选框状态
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV); // 从缓存读取序号值
        txtSuffixNum.setEnabled(isFileNumAddCheck); // 设置序号文本是否可用
        txtSuffixNum.setText(suffixNum); // 设置序号文本值
        if (checkFileNameAdd.isChecked() != isFileNumAddCheck) { // 如果复选框状态与缓存不一致
            checkFileNameAdd.setChecked(isFileNumAddCheck); // 设置复选框状态（会触发监听器）
        } else { // 如果状态一致
            onCheckBoxChangedListener.onCheckedChanged(checkFileNameAdd, isFileNumAddCheck); // 手动触发监听器
        }
    }

    /**
     * 从缓存恢复保存路径列表
     */
    private void restorePath() {
        pathSet.clear(); // 清空路径集合
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_WAV);//显示路径 // 从缓存读取显示路径字符串
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV);//绝对路径 // 从缓存读取绝对路径字符串

        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV); // 从缓存读取当前选中路径

        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示路径列表
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表

//        if (!Tools.fileIsExists(currentPath)) {
//            currentPath = null;
//        }


        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建文件Bean列表
        FileBean currentBean = new FileBean(); // 创建当前选中路径Bean
        for (int i = 0; i < abPathCacheList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue; // 如果路径不存在则跳过
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abPathCacheList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(pathCacheList.get(i)); // 设置显示名称
            Logger.i(TAG, "currentPath= " + currentPath + " ,pathCacheList= " + pathCacheList.get(i)); // 打印日志
            if(abPathCacheList.get(i).equals(currentPath)) { // 如果是当前选中路径
                currentBean.setPath(abPathCacheList.get(i)); // 设置当前Bean的绝对路径
                currentBean.setDisplayName(pathCacheList.get(i)); // 设置当前Bean的显示名称
            }
            dataList.add(fileBean); // 添加到列表
        }

        pathSet.addAll(dataList); // 将所有路径添加到集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉框数据

//        for (FileBean pathStr : dataList) {
//            addPathToPathSet(pathStr);
//        }
    }

    /**
     * 通过监听器发送消息
     */
    private void sendMsg() {
        if (onDetailSendMsgListener != null) { // 如果监听器不为空
            onDetailSendMsgListener.onClick(this, false); // 触发消息发送
        }
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 消息发送监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器引用
    }

    /**
     * 获取保存详情
     * @return WAV保存消息对象
     */
    public ISaveDetail getSaveDetail() {
        return topMsgSaveWave; // 返回WAV保存消息对象
    }

    /** 右侧通道消息事件的RxJava消费者，更新通道可见性 */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 创建消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收到通道消息
            TChan.foreachChan(chan -> { // 遍历所有通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 从缓存读取通道开启状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 更新通道可见性
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /** 右侧其他消息事件的RxJava消费者，更新Math/Ref通道可见性 */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 创建消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收到其他消息
            TChan.foreachMath(chan -> { // 遍历Math通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 从缓存读取Math开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 从缓存读取用户添加标志
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新Math通道可见性
            });
            TChan.foreachRef(chan -> { // 遍历Ref通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 从缓存读取Ref开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 从缓存读取用户添加标志
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新Ref通道可见性
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /** Ref通道变化事件的RxJava消费者，更新指定Ref通道可见性 */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // 创建消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收到Ref消息
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber()); // 从缓存读取用户添加标志
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser; // 更新指定Ref通道可见性
            setChannelShow(); // 更新通道显示
        }
    };

    /** 缓存加载事件的RxJava消费者，恢复缓存状态 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSaveWave, true); // 标记此菜单已加载缓存
        }
    };

    /**
     * 更新通道显示，设置通道可见性并通知UI刷新
     */
    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow, true); // 设置通道可见性
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知UI刷新通道控件
    }

    /** 通道颜色选择事件的RxJava消费者，更新通道颜色 */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 创建消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收到颜色选择事件
            if (colorInfo.isEmpty()) return; // 如果颜色信息为空则返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 打印日志
            String[] info = colorInfo.split(";"); // 解析颜色信息
            int chIndex = Integer.parseInt(info[0]); // 获取通道索引
            String colorStr = info[1]; // 获取颜色值
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道颜色
        }
    };

    /** 命令转UI事件的RxJava消费者，处理远程保存参数变更 */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 创建消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收到命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_STOTAGE_SAVE: { // 保存通道选择变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int channelIndex = Integer.parseInt(params[0]); // 获取通道索引
                    int saveIndex = Integer.parseInt(params[1]); // 获取保存索引
                    if (!ChannelFactory.isChOpen(channelIndex)) { // 如果通道未开启
                        return; // 返回
                    }
                    viewChannel.setChecked(channelIndex); // 设置通道选中状态
                    break; // 结束处理
                }
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_SOURCE: { // 保存源通道变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int channelIndex = Integer.parseInt(params[0]); // 获取通道索引
                    if (!ChannelFactory.isChOpen(channelIndex)) { // 如果通道未开启
                        return; // 返回
                    }
                    viewChannel.setChecked(channelIndex); // 设置通道选中状态
                }
                break; // 结束处理
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_LOCATION: { // 保存位置变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int saveIndex = Integer.parseInt(params[0]); // 获取保存索引
                }
                break; // 结束处理
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_FILENAME: { // 文件名变更
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    String fileName = (params[0]); // 获取文件名
                    saveNameEdit.setText(fileName); // 设置文件名编辑框文本
                }
                break; // 结束处理
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_START: { // 远程触发保存
                    onClickListener.onClick(btnSave); // 模拟保存按钮点击
                }
                break; // 结束处理
            }
        }
    };

    /** 快速保存事件的RxJava消费者，快速保存成功后递增序号 */
    private Consumer<Boolean> consumerMainBottomQuickSave = new Consumer<Boolean>() { // 创建消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // 接收到快速保存事件
            Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false); // 同步文件名到硬件
            if (aBoolean) {//快速保存成功
                autoAddSuffixNum(); // 自动递增序号
            }
        }
    };


    /** Ref保存消息对象 */
    private TopMsgSaveRef msgSaveRef = new TopMsgSaveRef(); // Ref保存消息对象

    /** 通道单选点击监听器，切换保存通道 */
    TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 创建监听器
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) { // 通道选择变化回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            Command.get().getStorage().Save_Source(checkedIndex, false); // 同步通道选择到硬件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(checkedIndex)); // 保存通道选择到缓存
        }
    };

    /** 通道多选测试监听器（当前为空实现） */
    TopViewChannelMultipleChoice.onTestListener onChannelItemTestListener = new TopViewChannelMultipleChoice.onTestListener() { // 创建监听器
        @Override
        public void onTest(CheckBox checkBox) { // 多选测试回调
//            List<Integer> lisSelects = viewChannel.getViewChannelMultipleChoice().getSelectChannel();
//            boolean showBin = true;
//            for (Integer chanIdx : lisSelects) {
//                if (ChannelFactory.isMathCh(chanIdx) || ChannelFactory.isRefCh(chanIdx)) {
//                    showBin = false;
//                    break;
//                }
//            }
//            saveType.setEnabled(2, showBin);
//            topMsgSaveWave.setSaveTypeEnable(2, showBin);
//            updateSaveToState();
        }
    };

    /** 保存位置单选组变化监听器，处理保存到Ref通道 */
    TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 创建监听器

        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调
            DToast.get().show(R.string.topSaveSaveInToast); // 显示保存位置提示
        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            onCheckChanged(view, item); // 委托给处理方法
        }
    };

    /** 文件名编辑框点击监听器，弹出文本键盘编辑文件名 */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() { // 创建监听器
        @Override
        public void onClickEdit(TopViewEdit v, String text) { // 点击编辑回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == saveNameEdit.getId()) { // 如果是文件名编辑框
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV); // 从缓存读取序号
                String suffix = "_" + suffixNum; // 构建序号后缀
                if (text.endsWith(suffix)) { // 如果文件名以序号后缀结尾
                    text = text.substring(0, text.length() - suffix.length()); // 去除序号后缀
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() { // 设置文本键盘数据
                    @Override
                    public void onDismiss(String result) { // 键盘关闭回调
                        saveNameEdit.setText(result); // 设置编辑框文本
                        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false); // 同步文件名到硬件
                        txtSuffixNum.setText("000"); // 重置序号为000
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_WAV, result); // 保存文件名到缓存
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV, "000"); // 保存序号到缓存
                    }
                });
            }
        }
    };

    /**
     * 处理保存位置变化，保存到Ref通道
     * @param view 触发的视图
     * @param item 选中的通道项
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item) {
        if (view.getId() == R.id.saveTo) { // 如果是保存位置单选组
            msgSaveRef.setFromIdChannelFactory(viewChannel.getSelectChannel()); // 设置源通道
            msgSaveRef.setSaveToRefId(item.getIndex() + 1); // 设置目标Ref通道ID
            RxBus.getInstance().post(RxEnum.TOPSLIP_SAVE_REF, msgSaveRef); // 发送Ref保存事件
            handler.sendEmptyMessageDelayed(1, 500); // 延迟500ms清除选中
        }
    }

    /**
     * 切换通道显示模式为多选模式
     */
    public void channelShowChange() {
        viewChannel.setAllSelectShow(false);//csv时显示多选控件 // 设置为多选模式
        setChannelShow(); // 更新通道显示
//        updateSaveToState();
    }

//    private void updateSaveToState() {
//        int type = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
//        int selectCount = viewChannel.getViewChannelMultipleChoice().getSelectCount();
//        boolean disAble = type == 1 && selectCount > 1;//只有此时需要disable
//        saveTo.setEnabled(!disAble);
//    }

    /** 序号复选框选中变化监听器 */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 创建监听器
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == checkFileNameAdd.getId()) { // 如果是序号复选框
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_WAV, String.valueOf(isChecked)); // 保存复选框状态到缓存
                txtSuffixNum.setEnabled(isChecked); // 设置序号文本是否可用
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.WAVE_TYPE_WAV + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked); // 发送序号更新事件
            }
        }
    };


    /** Handler消息处理，用于延迟清除保存位置选中 */
    private Handler handler = new Handler() { // 创建Handler
        @Override
        public void handleMessage(Message msg) { // 处理消息
            super.handleMessage(msg); // 调用父类方法
            switch (msg.what) { // 根据消息类型
                case 1: // 清除选中消息
                    saveTo.clearCheck(); // 清除保存位置选中
                    break; // 结束处理
            }
        }
    };

    /** 按钮点击事件监听器，处理保存、浏览、序号输入 */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 创建点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == btnSave.getId()) { // 如果点击保存按钮
                String finalInput = getFinaleName(); // 获取最终文件名
                String filePath = spinner.getSelectItem() + File.separator + finalInput + ".mwav"; // 构建完整文件路径

                if (FileUtils.checkFileExists(filePath)) { // 如果文件已存在
//                    DToast.get().show(String.format(getString(R.string.msgTopSaveNameExisted), finalInput + suffix));
//                    autoAddSuffixNum();
                    dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener); // 弹出覆盖确认对话框
                } else { // 如果文件不存在
                    doSaveWave(filePath); // 直接执行保存
                }
            } else if (v.getId() == btnBrowse.getId()) { // 如果点击浏览按钮
                handleBrowseClick(); // 处理浏览点击
            } else if (v.getId() == txtSuffixNum.getId()) { // 如果点击序号文本
                dialogKeyBoard.setDecimalData(3, IDigits.DIGITS_10, onNumSubFixListener); // 弹出数字键盘输入序号
            }
        }
    };

    /**
     * 获取最终文件名（含序号后缀）
     * @return 完整文件名
     */
    private String getFinaleName() {
        String finalName = saveNameEdit.getText(); // 获取编辑框中的文件名
        if(checkFileNameAdd.isChecked()) { // 如果勾选了添加序号
            finalName = finalName + "_" + txtSuffixNum.getText(); // 拼接序号后缀
        }
        return finalName; // 返回最终文件名
    }


    /**
     * 实际执行WAV保存操作
     * @param filePath 保存文件路径
     */
    private void doSaveWave(String filePath) {
        String finalInput = getFinaleName(); // 获取最终文件名
        int ch = viewChannel.getSelectChannel(); // 获取选中的通道索引

        if (StrUtil.isEmpty(spinner.getSelectItem())) { // 如果选中路径为空
            DToast.get().show(R.string.top_slip_directory_save_to); // 提示选择保存目录
            return; // 返回
        }

        if (!FileUtils.checkFolderExists(spinner.getSelectItem(), context.getResources().getString(R.string.internal_storage))) { // 检查保存目录是否存在
            DToast.get().show(R.string.top_slip_save_wave_path_unable); // 提示路径不可用
            return; // 返回
        }

        if (!ChannelFactory.isChOpen(ch)) { // 如果选中的通道未开启
            DToast.get().show(R.string.msgTopSaveCsvNotSelect); // 提示未选择通道
            return; // 返回
        }

        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕显示进度
        SaveManage.getInstance().allSaveEntrance(ch, 0, spinner.getSelectItem(), finalInput, null, new SaveManage.SaveCallBack() { // 执行WAV保存
            @Override
            public void onResult(boolean success, String msg) { // 保存结果回调
                ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
                if (success) { // 如果保存成功
                    SaveManage.getInstance().putCacheName(finalInput); // 缓存文件名
                    FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
                    autoAddSuffixNum(); // 自动递增序号
                } else { // 如果保存失败
                    FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份恢复
                }
                DToast.get().show(msg); // 显示保存结果提示
            }
        });
        Command.get().getStorage().Save_Filename(finalInput, false); // 同步文件名到硬件
    }

    /**
     * 文件名序号自动递增
     */
    private void autoAddSuffixNum() {//文件名序号递增
        if (!checkFileNameAdd.isChecked()) return; // 如果未勾选添加序号则返回
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim()); // 解析当前序号
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3); // 序号加1并格式化为3位
        if (onNumSubFixListener != null) { // 如果监听器不为空
            onNumSubFixListener.onDismiss(tempNum); // 触发监听器更新序号
        }
    }


    /** 数字键盘关闭监听器，将输入结果设置到序号文本 */
    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 创建关闭监听器
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(result); // 处理输入结果
        }
    };

    /**
     * 处理序号文本输入结果，更新UI和缓存
     * @param text 输入的序号文本
     */
    private void onTextListener(String text) {
        txtSuffixNum.setText(text); // 设置序号文本
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_WAV, text); // 保存序号到缓存
    }

    /**
     * 处理浏览按钮点击，打开文件选择器浏览保存路径
     */
    private void handleBrowseClick() {
        String spinnerSelectPath= spinner.getSelectItem(); // 获取当前选中的路径
        String disPlay = spinner.getDisPlaySelectItem(); // 获取当前选中路径的显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象

        if(!file.exists() || !file.isDirectory()){ // 如果路径不存在或不是目录
            spinnerSelectPath = "/storage/emulated/0"; // 使用默认内部存储路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 使用内部存储显示名称
        }
        this.selector = fileSelector.buildSaveFileSelector(spinnerSelectPath, disPlay, this, context); // 构建并显示文件选择器
    }
    /**
     * 测试方法（调试用）
     * @param fileBean 文件Bean
     */
    public void test(FileBean fileBean){
        String display = fileBean.getDisplayName(); // 获取显示名称
        String path = fileBean.getPath(); // 获取路径
    }
    /** 路径下拉框选中监听器，选中路径后更新缓存和路径集合 */
    TopViewSpinner.onItemSelectListener onItemSelectListener = new TopViewSpinner.onItemSelectListener() { // 创建监听器
        @Override
        public void onItemSelected(FileBean str) { // 选中回调
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, str.getPath()); // 保存当前选中路径到缓存
            //选中的记录置顶
            addPathToPathSet(str); // 添加到路径集合并更新UI
        }
    };

    /** 保存进度事件UI观察者，更新保存进度条 */
    EventUIObserver eventSaveBinObserver = new EventUIObserver() { // 创建观察者
        @Override
        public void update(Object data) { // 接收到事件更新
            EventBase base = (EventBase) data; // 转换为事件基类
            if (base == null) return; // 如果事件为空则返回
            int progress = 0; // 保存进度
            switch (base.getId()) { // 根据事件ID分发处理
                case EventFactory.EVENT_SAVEBIN_RUN: // 保存进度事件
                    progress = (int) ((EventBase) data).getData(); // 获取进度值
                    break; // 结束处理
            }
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            if (progress < 0 || progress >= 100) { // 如果进度完成或无效
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕未锁定
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕
                } else { // 如果屏幕已锁定
                    screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
                }
            } else { // 如果进度进行中
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕未锁定
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕
                }
                screenControls.setProgressValue(progress); // 更新进度值
            }
        }
    };


    /** 确认取消对话框的点击监听器，处理文件覆盖确认 */
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() { // 创建监听器
        @Override
        public void onOkClick(View v, Object data) { // 确认按钮点击回调
            Logger.i("Click ok"); // 打印日志
            if (v == null || data == null) return; // 参数为空则返回
            FileUtils.createBakFile((String) data); // 创建备份文件
            doSaveWave((String) data); // 执行WAV保存
        }

        @Override
        public void onCancelClick(View v, Object data) { // 取消按钮点击回调
            //Do nothing
            Logger.i("Click cancel"); // 打印取消日志
        }

        @Override
        public void onDialogClose(View view) { // 对话框关闭回调
        }
    };

}
