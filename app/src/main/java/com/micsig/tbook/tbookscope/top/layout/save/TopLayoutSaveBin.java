package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.graphics.Color; // 导入颜色类
import android.graphics.PixelFormat; // 导入像素格式类
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
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref右侧消息类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxBus事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举类
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented; // 导入采样分段消息类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘对话框类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard; // 导入文本键盘对话框类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.util.FileSelector; // 导入文件选择器类
import com.micsig.tbook.ui.FixedSizeHashSet; // 导入固定大小HashSet类
import com.micsig.tbook.ui.MMainMenuChannel; // 导入主菜单通道类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部编辑框类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入顶部下拉选择器类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean类
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道视图类
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice; // 导入通道多选视图类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入顶部单选组类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类
import com.molihuan.pathselector.PathSelector; // 导入路径选择器类
import com.molihuan.pathselector.dao.SelectConfigData; // 导入选择配置数据类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean实体类
import com.molihuan.pathselector.entity.FontBean; // 导入字体Bean实体类
import com.molihuan.pathselector.fragment.BasePathSelectFragment; // 导入路径选择Fragment基类
import com.molihuan.pathselector.fragment.impl.PathSelectFragment; // 导入路径选择Fragment实现类
import com.molihuan.pathselector.listener.CommonItemListener; // 导入通用项监听器
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl; // 导入配置数据构建器实现类
import com.molihuan.pathselector.utils.DToastDialog; // 导入自定义Toast对话框类
import com.molihuan.pathselector.utils.MConstants; // 导入路径选择器常量类


import java.io.File; // 导入文件类
import java.util.ArrayList; // 导入动态数组类
import java.util.Iterator; // 导入迭代器类
import java.util.List; // 导入列表接口

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * Created by Administrator on 2017/4/5.
 */

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → BIN文件保存（Save Bin）                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供BIN二进制波形文件的保存功能界面，用户可选择通道、设置文件名、       │
 * │          选择保存路径，执行BIN格式波形数据的保存操作                            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，通过TopViewChannel选择保存通道，                      │
 * │          通过TopViewSpinner选择保存路径，通过SaveManage执行保存，              │
 * │          通过RxBus监听通道状态、命令等事件，通过EventObserver监听保存进度       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：用户选择(通道/路径/文件名) → SaveManage.allSaveEntrance() →        │
 * │          ScreenControls(锁屏/进度) → SaveCallBack(结果回调) →               │
 * │          autoAddSuffixNum(序号递增)                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewChannel, TopViewSpinner, SaveManage, CacheUtil,            │
 * │          RxBus, Command, ChannelFactory, ScreenControls, FileUtils          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在Store页面选择BIN保存类型时，配置通道、路径和文件名后保存         │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutSaveBin extends Fragment {
    /** 日志标签 */
    private static final String TAG = "TopLayoutBin";
    /** 保存位置常量：本地存储 */
    public static final int SAVEINLOCAL = 0;// Tools.SaveType_LOCAL
    /** 保存位置常量：U盘存储 */
    public static final int SAVEINUDISK = 1;// Tools.SaveType_UDISK

    /** Fragment所在的上下文环境 */
    private Context context;
    /** 文件名编辑框 */
    private TopViewEdit saveNameEdit;
    /** 通道选择视图 */
    private MMainMenuChannel viewChannel;
    /** 文本键盘对话框（文件名输入） */
    private TopDialogTextKeyBoard layoutTextKeyBoard, fileSelectorTextKeyBoard;
    /** 全部分段复选框 */
    private CheckBox cbAllSegments;
    /** 波形保存消息对象 */
    private TopMsgSaveWave topMsgSaveWave;
    /** 保存按钮 */
    private Button btnSave;
    /** 路径下拉选择器 */
    private TopViewSpinner spinner;
    /** 详情消息发送监听器 */
    private OnDetailSendMsgListener onDetailSendMsgListener;

    /** 测试保存按钮（调试用） */
    private Button saveTest;
    /** 路径选择Fragment */
    private PathSelectFragment selector;

    /** 浏览按钮 */
    private Button btnBrowse;
    /** 文件名添加序号复选框 */
    private CheckBox checkFileNameAdd;
    /** 序号文本显示 */
    private TextView txtSuffixNum;
    /** 保存路径集合，固定最大容量10 */
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10);
    /** 数字键盘对话框 */
    protected TopDialogNumberKeyBoard dialogKeyBoard;

    /** 窗口管理器 */
    public WindowManager windowManager;

    /** 自定义Toast对话框 */
    public DToastDialog dToastdialog = new DToastDialog();
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    /** 通道显示可见性数组，28个通道的显示状态 */
    private boolean[] channelShow = {
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            false, false, false, false
    };

    /** 分段保存消息对象 */
    private TopMsgSaveSegments msgSaveSegments = new TopMsgSaveSegments();
    /** 确认取消对话框 */
    private DialogOkCancel dialogOk;

    /** 文件选择器 */
    private FileSelector fileSelector ;

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的BIN保存布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_bin, container, false); // 填充BIN保存布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getContext(); // 获取上下文
        initView(view); // 初始化视图控件
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化所有视图控件
     * @param view 根视图
     */
    private void initView(View view) {
        viewChannel = view.findViewById(R.id.chanSaveBin); // 获取通道选择视图
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 设置通道选择监听器
        cbAllSegments = view.findViewById(R.id.allSegments); // 获取全部分段复选框
        saveNameEdit = (TopViewEdit) view.findViewById(R.id.saveName); // 获取文件名编辑框
        saveNameEdit.setOnClickEditListener(onClickEditListener); // 设置编辑框点击监听器
        btnSave = view.findViewById(R.id.btn_save); // 获取保存按钮
        btnSave.setOnClickListener(onClickListener); // 设置保存按钮点击监听器

        spinner = view.findViewById(R.id.topSpinner); // 获取路径下拉选择器
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置默认提示文本
                getPreviousDirectory(), R.layout.layout_item_for_save_directory, onItemSelectListener); // 设置数据列表、布局和选择监听器
        btnBrowse = view.findViewById(R.id.btn_browse); // 获取浏览按钮
        btnBrowse.setOnClickListener(onClickListener); // 设置浏览按钮点击监听器
        txtSuffixNum = view.findViewById(R.id.txt_index_num); // 获取序号文本
        txtSuffixNum.setOnClickListener(onClickListener); // 设置序号文本点击监听器
        checkFileNameAdd = view.findViewById(R.id.check_file_name_add); // 获取文件名添加序号复选框
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取复选框自定义图标
        checkFileNameAdd.setBackground(null); // 清除背景
        checkFileNameAdd.setButtonDrawable(null); // 清除默认按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界大小
        checkFileNameAdd.setCompoundDrawables(drawable, null, null, null); // 将自定义图标设置到左侧
        checkFileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置复选框选中变化监听器


        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 从MainActivity获取文本键盘对话框

        topMsgSaveWave = new TopMsgSaveWave(new boolean[]{true, true, true}); // 创建波形保存消息，三种类型都可用
        cbAllSegments.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置全部分段复选框监听器

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 从MainActivity获取数字键盘对话框
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL); // 获取确认取消对话框
        fileSelector = new FileSelector(context,(selectedPath) -> { // 创建文件选择器
            addPathToPathSet(selectedPath); // 文件选择回调
        });
    }

    /**
     * 获取路径列表（倒序，最近使用的在前）
     * @return 路径Bean列表
     */
    private ArrayList<FileBean> getPreviousDirectory() {
        return pathSet.getReverseList(); // 返回倒序列表
    }

    /**
     * 将路径添加到路径集合，并更新UI和缓存
     * @param pathStr 要添加的文件路径Bean
     */
    private void addPathToPathSet(FileBean pathStr) {
        handleAddPath(pathStr); // 处理重复路径
        pathSet.add(pathStr); // 添加到集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉选择器数据
        savePathToCache(); // 保存路径到缓存
    }

    /**
     * 处理添加路径时的重复检查
     * @param pathStr 要检查的文件路径Bean
     * @return true表示可以新增，false表示路径已存在
     */
    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true; // 默认可以添加
        FileBean temp = null; // 记录已存在的同路径项
        for (FileBean fileBean : pathSet) { // 遍历已有路径集合
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径相同
                temp = fileBean; // 记录已存在的项
                canAdd = false; // 标记不可新增
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的项
            pathSet.remove(temp); // 先移除旧的
        }
        return canAdd; // 返回是否为新增
    }

    /**
     * 将BIN保存路径信息保存到缓存
     */
    public void savePathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_BIN, // 保存显示名称路径列表（BIN类型）
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将路径集合转为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_BIN, // 保存绝对路径列表（BIN类型）
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将绝对路径集合转为字符串保存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_BIN, spinner.getSelectItem()); // 保存当前选中的路径
    }

    /**
     * 初始化事件控制，订阅RxBus事件和EventObserver
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅右侧通道变化事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧其他变化事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅Ref通道变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令转UI事件
//        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKSAVE).subscribe(consumerMainBottomQuickSave);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerTopSampleSegmented); // 订阅采样分段变化事件
        EventFactory.addEventObserver(EventFactory.EVENT_SAVEBIN_RUN, eventSaveBinObserver); // 注册BIN保存进度事件观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择事件
//        setChannelShow();
    }

    /**
     * 从缓存恢复BIN保存页面的所有状态
     */
    private void setCache() {
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT); // 获取缓存中的通道选择

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_CHECK, String.valueOf(false), true); // 重置全部分段为未选中
        cbAllSegments.setChecked(false); // 取消全部分段复选框
        cbAllSegments.setTextColor(getResources().getColor(R.color.textColorNewTopViewEnable)); // 设置灰色文字

        String waveName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_BIN); // 获取缓存的文件名
        if (waveName.isEmpty()) { // 如果文件名为空
            waveName = Tools.generateName(); // 自动生成文件名
        }
        saveNameEdit.setText(waveName); // 设置文件名
        channelShowChange(); // 更新通道显示
        Command.get().getStorage().Save(channelSelect, SAVEINLOCAL, false); // 发送保存源命令
//        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
        Command.get().getStorage().Save_Type(2, false); // 发送保存类型为BIN(2)命令
        Command.get().getStorage().Save_ALLSegments(false, false); // 发送不分段保存命令

        viewChannel.setChangeListener(null, null); // 临时移除监听器避免触发
        viewChannel.setChecked(channelSelect); // 设置选中通道
        viewChannel.setChangeListener(onChannelItemClickListener, onChannelItemTestListener); // 恢复监听器
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1); // 获取CH1通道开关状态
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2); // 获取CH2通道开关状态
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3); // 获取CH3通道开关状态
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4); // 获取CH4通道开关状态
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5); // 获取CH5通道开关状态
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6); // 获取CH6通道开关状态
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7); // 获取CH7通道开关状态
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8); // 获取CH8通道开关状态

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 2通道机型
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) { // 4通道机型
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
            channelShow[2] = ch3; // 设置CH3可见性
            channelShow[3] = ch4; // 设置CH4可见性
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) { // 8通道机型
            channelShow[0] = ch1; // 设置CH1可见性
            channelShow[1] = ch2; // 设置CH2可见性
            channelShow[2] = ch3; // 设置CH3可见性
            channelShow[3] = ch4; // 设置CH4可见性
            channelShow[4] = ch5; // 设置CH5可见性
            channelShow[5] = ch6; // 设置CH6可见性
            channelShow[6] = ch7; // 设置CH7可见性
            channelShow[7] = ch8; // 设置CH8可见性
        }
        TChan.foreachCh1ToR8(chan -> { // 遍历所有通道
            if (TChan.isMath(chan)) { // 如果是Math通道
//                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
//                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
//                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
                channelShow[TChan.toFpgaChNo(chan)] = false;//bin不需要显示Math通道 // BIN格式不支持Math通道
            }
            if (TChan.isRef(chan)) { // 如果是Ref通道
//                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
//                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
//                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
                channelShow[TChan.toFpgaChNo(chan)] = false;//bin不需要显示Ref通道 // BIN格式不支持Ref通道
            }
        });
        setChannelShow(); // 更新通道显示
        setAllSegmentsVisible(); // 更新全部分段可见性
        viewChannel.getViewChannelMultipleChoice().unCheckAll(); // 取消所有多选

        restorePath(); // 恢复路径

        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_BIN); // 获取序号添加开关状态
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_BIN); // 获取缓存的序号
        txtSuffixNum.setEnabled(isFileNumAddCheck); // 设置序号文本可用性
        txtSuffixNum.setText(suffixNum); // 设置序号文本
        if (checkFileNameAdd.isChecked() != isFileNumAddCheck) { // 如果当前状态与缓存不一致
            checkFileNameAdd.setChecked(isFileNumAddCheck); // 设置复选框状态（会触发监听器）
        } else { // 状态一致
            onCheckBoxChangedListener.onCheckedChanged(checkFileNameAdd, isFileNumAddCheck); // 手动触发监听器
        }
    }

    /**
     * 从缓存恢复保存路径列表
     */
    private void restorePath() {
        pathSet.clear(); // 清空当前路径集合
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + CacheUtil.WAVE_TYPE_BIN); // 获取显示名称路径缓存
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_BIN); // 获取绝对路径缓存

        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_BIN); // 获取当前选中路径
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示名称路径列表
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表
//        if (!Tools.fileIsExists(currentPath)) {
//            currentPath = null;
//        }

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建数据列表
        FileBean currentBean = new FileBean(); // 创建当前选中项Bean
        for (int i = 0; i < abPathCacheList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue; // 跳过不存在的文件
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abPathCacheList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(pathCacheList.get(i)); // 设置显示名称
            if(pathCacheList.get(i).equals(currentPath)) { // 如果是当前选中的路径
                currentBean.setPath(abPathCacheList.get(i)); // 设置当前Bean的路径
                currentBean.setDisplayName(pathCacheList.get(i)); // 设置当前Bean的显示名称
            }
            dataList.add(fileBean); // 添加到数据列表
        }

        pathSet.addAll(dataList); // 将恢复的数据添加到路径集合
        spinner.updateDataList(getPreviousDirectory(), null); // 更新下拉选择器数据

//        for (FileBean pathStr : dataList) {
//            addPathToPathSet(pathStr);
//        }
    }

    /**
     * 发送消息通知父级
     */
    private void sendMsg() {
        if (onDetailSendMsgListener != null) { // 如果监听器不为空
            onDetailSendMsgListener.onClick(this, false); // 通知父级点击事件
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
     * 获取保存详情接口
     * @return 波形保存消息对象
     */
    public ISaveDetail getSaveDetail() {
        return topMsgSaveWave; // 返回波形保存消息
    }

    /**
     * 右侧通道变化事件的RxJava消费者
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 创建消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收到通道变化事件
            TChan.foreachChan(chan -> { // 遍历所有通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 获取通道开关状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 更新通道可见性
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * 右侧其他变化事件（Math/Ref）的RxJava消费者
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 创建消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收到其他变化事件
            TChan.foreachMath(chan -> { // 遍历Math通道
//                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan);
//                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan);
//                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
                channelShow[TChan.toFpgaChNo(chan)] = false;//bin不需要显示Math通道 // BIN不支持Math
            });
            TChan.foreachRef(chan -> { // 遍历Ref通道
//                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan);
//                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan);
//                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser;
                channelShow[TChan.toFpgaChNo(chan)] = false;//bin不需要显示Ref通道 // BIN不支持Ref
            });
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * Ref通道变化事件的RxJava消费者
     */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // 创建消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收到Ref变化事件
            //哪个通道变化 设置哪个通道
//            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber());
//            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser;
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = false; // BIN不支持Ref
            setChannelShow(); // 更新通道显示
        }
    };

    /**
     * 缓存加载事件的RxJava消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSaveWave, true); // 标记此菜单已加载缓存
        }
    };

    /**
     * 更新通道显示可见性
     */
    private void setChannelShow() {
        viewChannel.setItemVisible(channelShow, true); // 设置通道可见性
        setAllSegmentsVisible(); // 更新全部分段可见性
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知控件可见性变化
    }

    /**
     * 通道颜色选择事件的RxJava消费者
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 创建消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收到颜色选择事件
            if (colorInfo.isEmpty()) return; // 空信息则返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 记录颜色信息日志
            String[] info = colorInfo.split(";"); // 分割通道索引和颜色值
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色字符串
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道颜色
        }
    };

    /**
     * 命令转UI事件的RxJava消费者
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 创建消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收到命令转UI事件
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_STOTAGE_SAVE: { // 保存源命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数
                    int channelIndex = Integer.parseInt(params[0]); // 解析通道索引
                    int saveIndex = Integer.parseInt(params[1]); // 解析保存索引
                    if (!ChannelFactory.isChOpen(channelIndex)) { // 如果通道未打开
                        return; // 返回
                    }
                    viewChannel.setChecked(channelIndex); // 设置选中通道
                    break;
                }
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_SOURCE: { // 保存源选择命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数
                    int channelIndex = Integer.parseInt(params[0]); // 解析通道索引
                    if (!ChannelFactory.isChOpen(channelIndex)) { // 如果通道未打开
                        return; // 返回
                    }
                    viewChannel.setChecked(channelIndex); // 设置选中通道
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_LOCATION: { // 保存位置命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数
                    int saveIndex = Integer.parseInt(params[0]); // 解析保存索引
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_FILENAME: { // 保存文件名命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数
                    String fileName = (params[0]); // 获取文件名
                    saveNameEdit.setText(fileName); // 设置文件名
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_START: { // 开始保存命令
                    onClickListener.onClick(btnSave); // 触发保存按钮点击
                }
                break;
                case CommandMsgToUI.FLAG_STOTAGE_SAVE_ALLSEGMENTS: { // 全部分段命令
                    boolean b = Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析布尔参数
                    cbAllSegments.setChecked(b); // 设置全部分段复选框
                    onCheckBoxChangedListener.onCheckedChanged(cbAllSegments, b); // 触发监听器
                }
                break;
            }
        }
    };

//    private Consumer<Integer> consumerMainBottomQuickSave = new Consumer<Integer>() {
//        @Override
//        public void accept(Integer integer) throws Exception {
//            saveNameEdit.setText(generateName());
//            Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
//        }
//    };

    /**
     * 采样分段变化事件的RxJava消费者
     */
    private Consumer<TopMsgSampleSegmented> consumerTopSampleSegmented = new Consumer<TopMsgSampleSegmented>() { // 创建消费者
        @Override
        public void accept(TopMsgSampleSegmented msgSegmented) throws Exception { // 接收到分段变化事件
            setAllSegmentsVisible(); // 更新全部分段可见性
            if (!msgSegmented.getState().isValue()) { // 如果分段状态为关闭
                cbAllSegments.setChecked(false); // 取消全部分段复选框
            }
        }
    };


    /** Ref保存消息对象 */
    private TopMsgSaveRef msgSaveRef = new TopMsgSaveRef();

    /**
     * 设置全部分段复选框的可见性
     * @return 可见性是否发生变化
     */
    private boolean setAllSegmentsVisible() {
        boolean change = false; // 可见性变化标志
        int segmentState = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE); // 获取分段状态
        int nowSelect = viewChannel.getSelectChannel(); // 获取当前选中通道
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT); // 获取缓存中的通道选择
        if (channelSelect != nowSelect) { // 如果通道选择不一致
            channelSelect = nowSelect; // 使用当前选中通道
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(channelSelect)); // 更新缓存
        }
        if (ChannelFactory.isDynamicCh(channelSelect) && segmentState == 0) { // 如果是动态通道且分段未激活
            if (cbAllSegments.getVisibility() != View.VISIBLE) { // 如果当前不可见
                change = true; // 标记变化
            }
            cbAllSegments.setVisibility(View.VISIBLE); // 设置可见
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_VISIBLE, String.valueOf(true), true); // 缓存可见状态
        } else { // 非动态通道或分段已激活
            if (cbAllSegments.getVisibility() != View.GONE) { // 如果当前可见
                change = true; // 标记变化
            }
            cbAllSegments.setVisibility(View.GONE); // 设置不可见
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_VISIBLE, String.valueOf(false), true); // 缓存不可见状态


        }
        if (change) { // 如果可见性发生变化
            sendMsgSaveSegments(); // 发送分段消息
        }
        return change; // 返回变化标志
    }

    /**
     * 通道选择点击监听器
     */
    TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 创建监听器
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) { // 通道选择变化回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            Command.get().getStorage().Save_Source(checkedIndex, false); // 发送保存源命令
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT, String.valueOf(checkedIndex)); // 缓存通道选择
            boolean sendMsg = setAllSegmentsVisible(); // 更新全部分段可见性
            if (!sendMsg) { // 如果可见性未变化
                sendMsgSaveSegments(); // 仍需发送分段消息
            }
        }
    };

    /**
     * 通道多选测试监听器
     */
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
//            setAllSegmentsVisible();
//            updateSaveToState();
        }
    };



    /**
     * 保存类型选择变化监听器
     */
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
//            onCheckChanged(view, item);
        }
    };

    /**
     * 文件名编辑框点击监听器
     */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() { // 创建监听器
        @Override
        public void onClickEdit(TopViewEdit v, String text) { // 编辑框点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == saveNameEdit.getId()) { // 如果是文件名编辑框
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_BIN); // 获取缓存的序号
                String suffix = "_" + suffixNum; // 构造序号后缀
                if (text.endsWith(suffix)) { // 如果文件名以序号后缀结尾
                    text = text.substring(0, text.length() - suffix.length()); // 去除序号后缀
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() { // 弹出文本键盘
                    @Override
                    public void onDismiss(String result) { // 键盘关闭回调
                        saveNameEdit.setText(result); // 设置新文件名
//                        Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
                        txtSuffixNum.setText("000"); // 重置序号
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_NAME + CacheUtil.WAVE_TYPE_BIN, result); // 缓存文件名
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_BIN, "000"); // 缓存序号
                    }
                });
            }
        }
    };

    /*private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item) {
        if (view.getId() == R.id.saveType) {
            Command.get().getStorage().Save_Type(item.getIndex(), false);
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_TYPE, String.valueOf(item.getIndex()));
            Command.get().getStorage().Save_Filename(saveNameEdit.getText().toString(), false);
            channelShowChange(item.getIndex());
        } else
        if (view.getId() == R.id.saveTo) {
            msgSaveRef.setFromIdChannelFactory(viewChannel.getSelectChannel());
            msgSaveRef.setSaveToRefId(item.getIndex() + 1);
            RxBus.getInstance().post(RxEnum.TOPSLIP_SAVE_REF, msgSaveRef);
            handler.sendEmptyMessageDelayed(1, 500);
        }
    }*/

    /**
     * 更新通道显示为BIN模式（不显示多选控件）
     */
    public void channelShowChange() {
        viewChannel.setAllSelectShow(false);//csv时显示多选控件 // BIN模式不显示多选控件
        setChannelShow(); // 更新通道显示
//        updateSaveToState();
    }

//    private void updateSaveToState() {
//        int type = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
//        int selectCount = viewChannel.getViewChannelMultipleChoice().getSelectCount();
//        boolean disAble = type == 1 && selectCount > 1;//只有此时需要disable
//        saveTo.setEnabled(!disAble);
//    }

    /**
     * 复选框选中变化监听器（全部分段/文件名添加序号）
     */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 创建监听器
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == cbAllSegments.getId()) { // 全部分段复选框
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_CHECK, String.valueOf(isChecked)); // 缓存状态
                Command.get().getStorage().Save_ALLSegments(isChecked, false); // 发送分段保存命令
                if (isChecked) { // 如果选中
                    cbAllSegments.setTextColor(getResources().getColor(R.color.color_Text_white)); // 白色文字
                } else { // 未选中
                    cbAllSegments.setTextColor(getResources().getColor(R.color.textColorNewTopViewEnable)); // 灰色文字
                }
//                int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_CHANNEL_SELECT);
//                setSaveTypeEnable(channelSelect);
                sendMsgSaveSegments(); // 发送分段消息
            } else if (buttonView.getId() == checkFileNameAdd.getId()) { // 文件名添加序号复选框
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + CacheUtil.WAVE_TYPE_BIN, String.valueOf(isChecked)); // 缓存状态
                txtSuffixNum.setEnabled(isChecked); // 设置序号文本可用性
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.WAVE_TYPE_BIN + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked); // 通知序号更新
            }
        }
    };

    /**
     * 发送分段保存消息
     */
    private void sendMsgSaveSegments() {
        msgSaveSegments.setVisibleSegments(cbAllSegments.getVisibility() == View.VISIBLE); // 设置分段可见性
//        msgSaveSegments.setEnableTypeWav(rgSaveType.isEnabled(0));
//        msgSaveSegments.setEnableTypeCsv(rgSaveType.isEnabled(1));
//        msgSaveSegments.setEnableTypeBin(rgSaveType.isEnabled(2));
        RxBus.getInstance().post(RxEnum.TOPSLIP_SAVE_SEGMENT, msgSaveSegments); // 发送分段消息事件
    }


//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 1:
//                    saveTo.clearCheck();
//                    break;
//            }
//        }
//    };

    /**
     * 按钮点击事件监听器
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 创建监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == btnSave.getId()) { // 保存按钮
                String finalInput = getFinaleName(); // 获取最终文件名
                String filePath = spinner.getSelectItem() + File.separator + finalInput + ".bin"; // 构造完整文件路径
                if (FileUtils.checkFileExists(filePath)) { // 如果文件已存在
                    dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener); // 弹出确认覆盖对话框
                } else { // 文件不存在
                    doSaveWave(filePath); // 直接保存
                }
            } else if (v.getId() == btnBrowse.getId()) { // 浏览按钮
                handleBrowseClick(); // 处理浏览操作
            } else if (v.getId() == txtSuffixNum.getId()) { // 序号文本
                dialogKeyBoard.setDecimalData(3, IDigits.DIGITS_10, onNumSubFixListener); // 弹出数字键盘
            }
        }
    };

    /**
     * 获取最终文件名（含序号后缀）
     * @return 最终文件名
     */
    private String getFinaleName() {
        String finalName = saveNameEdit.getText(); // 获取编辑框文件名
        if(checkFileNameAdd.isChecked()) { // 如果勾选了添加序号
            finalName = finalName + "_" + txtSuffixNum.getText(); // 拼接序号后缀
        }
        return finalName; // 返回最终文件名
    }


    /**
     * 执行BIN波形保存
     * @param filePath 完整文件路径
     */
    private void doSaveWave(String filePath) {
        String finalInput = getFinaleName(); // 获取最终文件名
        int ch = viewChannel.getSelectChannel(); // 获取选中通道

        if (StrUtil.isEmpty(spinner.getSelectItem())) { // 如果未选择保存路径
            DToast.get().show(R.string.top_slip_directory_save_to); // 提示选择保存路径
            return; // 返回
        }

        if (!FileUtils.checkFolderExists(spinner.getSelectItem(), context.getResources().getString(R.string.internal_storage))) { // 如果保存路径不可用
            DToast.get().show(R.string.top_slip_save_wave_path_unable); // 提示路径不可用
            return; // 返回
        }

        if (ChannelFactory.isMathCh(ch) || ChannelFactory.isRefCh(ch)) { // 如果是Math或Ref通道
            DToast.get().show(R.string.msgTopSaveBinNoSupport); // 提示BIN不支持此通道
            return; // 返回
        }

        if (!ChannelFactory.isChOpen(ch)) { // 如果通道未打开
            DToast.get().show(R.string.msgTopSaveCsvNotSelect); // 提示通道未选中
            return; // 返回
        }

        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁屏显示进度
        SaveManage.getInstance().allSaveEntrance(ch, 2, spinner.getSelectItem(), finalInput, null, new SaveManage.SaveCallBack() { // 调用保存入口（类型2=BIN）
            @Override
            public void onResult(boolean success, String msg) { // 保存结果回调
                ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
                if (success) { // 如果保存成功
                    SaveManage.getInstance().putCacheName(finalInput); // 缓存文件名
                    FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
                    autoAddSuffixNum(); // 自动递增序号
                } else { // 保存失败
                    FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
                }
                DToast.get().show(msg); // 显示结果提示
            }
        });
//        Command.get().getStorage().Save_Filename(finalInput, false);
    }

    /**
     * 文件名序号自动递增
     */
    private void autoAddSuffixNum() {//文件名序号递增
        if (!checkFileNameAdd.isChecked()) return; // 未勾选添加序号则返回
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim()); // 获取当前序号
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3); // 递增并格式化为3位
        if (onNumSubFixListener != null) { // 如果监听器不为空
            onNumSubFixListener.onDismiss(tempNum); // 触发序号更新
        }
    }


    /**
     * 数字键盘关闭监听器
     */
    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 创建监听器
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(result); // 处理输入结果
        }
    };

    /**
     * 处理序号输入结果
     * @param text 输入的序号文本
     */
    private void onTextListener(String text) {
        txtSuffixNum.setText(text); // 设置序号文本
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + CacheUtil.WAVE_TYPE_BIN, text); // 缓存序号
    }

    /**
     * 处理浏览按钮点击，打开文件选择器
     */
    private void handleBrowseClick() {
        String spinnerSelectPath= spinner.getSelectItem(); // 获取当前选中路径
        String disPlay = spinner.getDisPlaySelectItem(); // 获取显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象

        if(!file.exists() || !file.isDirectory()){ // 如果路径不存在或不是目录
            spinnerSelectPath = "/storage/emulated/0"; // 使用内部存储根目录
            disPlay = context.getResources().getString(R.string.internal_storage); // 显示"内部存储"
        }
        fileSelector.buildSaveFileSelector(spinnerSelectPath, disPlay, this, context); // 构建保存文件选择器
    }

    /**
     * 路径下拉选择器项选择监听器
     */
    TopViewSpinner.onItemSelectListener onItemSelectListener = new TopViewSpinner.onItemSelectListener() { // 创建监听器
        @Override
        public void onItemSelected(FileBean str) { // 选中项变化回调
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_BIN, str.getPath()); // 缓存当前路径
            addPathToPathSet(str); // 添加到路径集合
        }
    };

    /**
     * BIN保存进度事件的UI观察者
     */
    EventUIObserver eventSaveBinObserver = new EventUIObserver() { // 创建观察者
        @Override
        public void update(Object data) { // 接收到事件更新
            EventBase base = (EventBase) data; // 转换为事件基类
            if (base == null) return; // 事件为空则返回
            int progress = 0; // 保存进度
            switch (base.getId()) { // 根据事件ID分发
                case EventFactory.EVENT_SAVEBIN_RUN: // BIN保存运行事件
                    progress = (int) ((EventBase) data).getData(); // 获取进度值
                    break;
            }
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            if (progress < 0 || progress >= 100) { // 如果进度完成或异常
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果未锁屏
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁屏
                } else { // 已锁屏
                    screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
                }
            } else { // 进度进行中
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果未锁屏
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁屏
                }
                screenControls.setProgressValue(progress); // 更新进度值
            }
        }
    };


    /**
     * 确认取消对话框的点击监听器（文件覆盖确认）
     */
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() { // 创建监听器
        @Override
        public void onOkClick(View v, Object data) { // 确认点击回调
            Logger.i("Click ok"); // 记录日志
            if (v == null || data == null) return; // 空值保护
            FileUtils.createBakFile((String) data); // 创建备份文件
            doSaveWave((String) data); // 执行保存
        }

        @Override
        public void onCancelClick(View v, Object data) { // 取消点击回调
            //Do nothing
            Logger.i("Click cancel"); // 记录日志
        }

        @Override
        public void onDialogClose(View view) { // 对话框关闭回调
        }
    };

}
