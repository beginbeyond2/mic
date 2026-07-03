package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.annotation.SuppressLint; // 导入SuppressLint注解
import android.content.Context; // 导入上下文类
import android.graphics.Color; // 导入颜色类
import android.graphics.drawable.Drawable; // 导入Drawable图形类
import android.os.Bundle; // 导入Bundle状态保存类
import android.text.TextUtils; // 导入文本工具类
import android.util.Log; // 导入日志工具类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件类
import android.widget.CheckBox; // 导入复选框控件类
import android.widget.CompoundButton; // 导入复合按钮基类
import android.widget.TextView; // 导入文本视图控件类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Data.LoadCsv; // 导入CSV加载类
import com.micsig.tbook.scope.Data.SaveRecoverySession; // 导入会话保存恢复类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef; // 导入参考水平轴类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.R; // 导入资源引用类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref右侧消息类
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave; // 导入Ref加载对话框类
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean; // 导入Ref召回数据Bean类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxBus事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.util.FileSelector; // 导入文件选择器类
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入波形管理类
import com.micsig.tbook.ui.FixedSizeHashSet; // 导入固定大小HashSet类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入顶部下拉选择器类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类
import com.molihuan.pathselector.PathSelector; // 导入路径选择器类
import com.molihuan.pathselector.dao.SelectConfigData; // 导入选择配置数据类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean实体类
import com.molihuan.pathselector.entity.FontBean; // 导入字体Bean实体类
import com.molihuan.pathselector.fragment.BasePathSelectFragment; // 导入路径选择Fragment基类
import com.molihuan.pathselector.listener.CommonItemListener; // 导入通用项监听器
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl; // 导入配置数据构建器实现类
import com.molihuan.pathselector.utils.DToastDialog; // 导入自定义Toast对话框类
import com.molihuan.pathselector.utils.MConstants; // 导入路径选择器常量类

import java.io.File; // 导入文件类
import java.text.SimpleDateFormat; // 导入简单日期格式化类
import java.util.ArrayList; // 导入动态数组类
import java.util.Date; // 导入日期类
import java.util.HashMap; // 导入哈希映射类
import java.util.Iterator; // 导入迭代器类
import java.util.List; // 导入列表接口
import java.util.Map; // 导入映射接口
import java.util.concurrent.ConcurrentHashMap; // 导入并发哈希映射类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → WAV波形文件调用（Invoke Wav）            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供WAV波形文件的浏览、选择和载入功能界面，                           │
 * │          用户可通过此Fragment浏览存储中的WAV文件，将其载入到Ref参考通道中        │
 * │          作为参考波形显示                                                     │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，作为顶部滑动菜单中的一个Tab页面，                       │
 * │          通过RxBus订阅缓存加载事件，通过FileSelector进行文件浏览，             │
 * │          通过SaveManage.readRef()载入WAV文件到Ref通道                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(路径缓存) → wavePathSet(路径集合) →                       │
 * │          waveSpinner(下拉列表) → SaveManage.readRef()(载入WAV) →             │
 * │          RefChannel(参考通道显示) → RxBus(通知右侧Ref菜单更新)                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewSpinner, FileSelector, SaveManage, CacheUtil,              │
 * │          RxBus, ChannelFactory, Command, WaveManage, TChan                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部菜单选择"调用→波形"时显示此界面，                          │
 * │          浏览并选择WAV/mwav文件来载入参考波形                                  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutInvokeWav extends Fragment {

    /** 日志标签，用于标识当前类的日志输出 */
    private static final String TAG = "TopLayoutInvokeWav"; // 日志标签
    /** Fragment所在的上下文环境，即Activity */
    public Context context; // Activity上下文
    /** WAV文件路径下拉选择器，用于显示和选择已保存的WAV文件路径 */
    private TopViewSpinner waveSpinner; // WAV文件路径下拉选择器
    /** 调用按钮，点击后载入选中的WAV文件到Ref通道 */
    private Button btnWaveInvoke; // 调用按钮
    /** 浏览按钮，点击后打开文件浏览器选择WAV文件 */
    private Button btnWaveBrowse; // 浏览按钮
    /** 是否仅显示文件的复选框，控制文件浏览器中是否只显示文件 */
    private CheckBox chIsFilesShowOnly; // 仅显示文件复选框
    /** WAV文件路径集合，固定最大容量10，存储最近使用的WAV文件路径 */
    private final FixedSizeHashSet<FileBean> wavePathSet = new FixedSizeHashSet<>(10); // WAV文件路径集合
    /** Ref/Csv加载对话框，此界面中未直接使用但保留引用 */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave; // Ref/Csv加载对话框

    /** 是否仅显示文件的标志位，默认为true */
    private boolean isFilesShowOnly =true; // 仅显示文件标志
    /** 自定义Toast对话框，用于显示提示信息 */
    private DToastDialog dToastDialog = new DToastDialog(); // 对话框Toast
    /** 详情消息发送监听器，用于与父级通信 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息监听器
    /** 文件选择器，用于浏览和选择WAV文件 */
    private FileSelector fileSelector; // 文件选择器


    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的WAV文件调用布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke_wav, container, false); // 填充WAV文件调用布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity(); // 获取所在Activity作为上下文
        initView(view, savedInstanceState); // 初始化视图控件
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化所有视图控件
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图控件
        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv); // 从MainActivity获取Ref加载对话框
        waveSpinner = view.findViewById(R.id.wave_spinner); // 获取WAV文件路径下拉选择器
        waveSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置默认提示文本
                getWaveFileList(), R.layout.layout_item_for_save_directory, onWavePathItemSelectListener); // 设置数据列表、布局和选择监听器
        btnWaveBrowse = view.findViewById(R.id.btn_wave_browse); // 获取浏览按钮
        btnWaveBrowse.setOnClickListener(onClickListener); // 设置浏览按钮点击监听器
        btnWaveInvoke = view.findViewById(R.id.btn_wave_invoke); // 获取调用按钮
        btnWaveInvoke.setOnClickListener(onClickListener); // 设置调用按钮点击监听器

        chIsFilesShowOnly = view.findViewById(R.id.check_file_only_show); // 获取"仅显示文件"复选框
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取复选框自定义图标
        chIsFilesShowOnly.setBackground(null); // 清除背景
        chIsFilesShowOnly.setButtonDrawable(null); // 清除默认按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界大小
        chIsFilesShowOnly.setCompoundDrawables(drawable, null, null, null); // 将自定义图标设置到左侧
        chIsFilesShowOnly.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置复选框选中变化监听器

        fileSelector = new FileSelector(context, (selectedPath) -> { // 创建文件选择器
            addSelectToPathSet(selectedPath); // 文件选择回调，将选中路径添加到路径集合
        });
    }

    /**
     * 初始化事件控制，订阅RxBus缓存加载事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /**
     * 将文件选择器选中的路径添加到路径集合
     * @param pathStr 选中的文件路径Bean
     */
    private void addSelectToPathSet(FileBean pathStr) { // 添加选中路径到集合
        addPathToWaveSet(pathStr); // 添加到WAV文件路径集合
    }

    /**
     * 将路径添加到WAV文件路径集合，并更新UI和缓存
     * @param pathStr 要添加的文件路径Bean
     */
    private void addPathToWaveSet(FileBean pathStr) { // 添加路径到WAV文件集合
        handleAddPath(pathStr); // 处理重复路径（先移除旧的再添加新的）
        wavePathSet.add(pathStr); // 将路径添加到集合
        waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器数据列表
        saveWavePathToCache(); // 保存路径到缓存
    }

    /**
     * 处理添加路径时的重复检查，如果路径已存在则先移除旧的
     * @param pathStr 要检查的文件路径Bean
     * @return true表示可以新增，false表示路径已存在（已移除旧的）
     */
    private boolean handleAddPath(FileBean pathStr) { // 处理路径添加去重
        boolean canAdd = true; // 默认可以添加
        FileBean temp = null; // 记录已存在的同路径项
        for (FileBean fileBean : wavePathSet) { // 遍历已有路径集合
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径相同
                canAdd = false; // 标记不可新增
                temp = fileBean; // 记录已存在的项
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的项
            wavePathSet.remove(temp); // 先移除旧的，后续会添加新的（实现移到最前）
        }
        return canAdd; // 返回是否为新增
    }


    /**
     * 获取WAV文件列表（倒序，最近使用的在前）
     * @return WAV文件路径Bean列表
     */
    private ArrayList<FileBean> getWaveFileList() { // 获取WAV文件列表
        return wavePathSet.getReverseList(); // 返回倒序列表，最近添加的排在前面
    }

    /**
     * 将WAV文件路径信息保存到缓存
     */
    public void saveWavePathToCache() { // 保存WAV路径到缓存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_WAV, // 保存显示名称路径列表（WAV类型）
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将路径集合转为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV, // 保存绝对路径列表（WAV类型）
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将绝对路径集合转为字符串保存

        Logger.i(TAG, "currentPath= " + waveSpinner.getSelectItem()); // 记录当前选中路径日志
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, waveSpinner.getSelectItem()); // 保存当前选中的路径

    }

    /**
     * WAV文件路径下拉选择器的项选择监听器
     */
    TopViewSpinner.onItemSelectListener onWavePathItemSelectListener = str -> { // 选中某项时的回调
        if (Tools.fileIsExists(str.getPath())) { // 如果选中的路径文件存在
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV, str.getDisplayName()); // 更新当前选中路径缓存
            addPathToWaveSet(str); // 添加到路径集合（移到最前）
        } else { // 文件不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            deleteEmptyItem(str); // 删除无效路径项
        }
    };

    /**
     * 按钮点击事件监听器
     */
    @SuppressLint("NonConstantResourceId")
    private View.OnClickListener onClickListener = v -> { // 按钮点击回调
        PlaySound.getInstance().playButton(); // 播放按键音效
        switch (v.getId()) { // 根据按钮ID分发
            case R.id.btn_wave_browse: // 浏览按钮
                handleBrowse(); // 处理浏览操作
                break;
            case R.id.btn_wave_invoke: // 调用按钮
                loadRefFromFile(); // 载入WAV文件到Ref通道
                break;
        }
    };

    /**
     * 删除无效的路径项
     * @param filePath 要删除的文件路径Bean
     */
    private void deleteEmptyItem(FileBean filePath) { // 删除无效路径项
        deleteEmptyWaveItem(filePath); // 委托给删除WAV文件项方法
    }

    /**
     * 从路径集合和下拉列表中删除无效的WAV文件项
     * @param filePath 要删除的文件路径Bean
     */
    private void deleteEmptyWaveItem(FileBean filePath) { // 删除空WAV文件项
        boolean deleteSuccess = wavePathSet.remove(filePath); // 从集合中移除
        if (deleteSuccess) { // 如果移除成功
            waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器数据
            saveWavePathToCache(); // 保存更新后的路径到缓存
        }
    }


    /**
     * 处理浏览按钮点击
     */
    private void handleBrowse() { // 处理浏览操作
        handleBrowseClick(); // 委托给实际浏览处理方法
    }

    /**
     * 实际处理浏览操作，构建文件选择器并打开
     */
    private void handleBrowseClick() { // 处理浏览点击
        String spinnerSelectPath= waveSpinner.getSelectItem(); // 获取当前下拉选择器选中的路径
        String disPlay = waveSpinner.getDisPlaySelectItem(); // 获取当前选中项的显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象

        if (!file.exists()) { // 如果当前选中路径不存在
            spinnerSelectPath = "/storage/emulated/0"; // 使用内部存储根目录作为默认路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 显示"内部存储"
        } else { // 路径存在
            spinnerSelectPath = file.getParent(); // 使用父目录作为起始路径
        }
        String[] waveFileType = {"wav", "mwav"}; // WAV文件类型过滤器
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, waveFileType); // 构建并打开文件选择器
    }

    /**
     * 从选中的WAV文件载入参考波形
     */
    private void loadRefFromFile() { // 从文件载入Ref
        String wavePath = waveSpinner.getSelectItem(); // 获取当前选中的WAV文件路径
        if (StrUtil.isEmpty(wavePath)) { // 如果未选择文件
            DToast.get().show(R.string.top_slip_select_file_first); // 提示先选择文件
            return; // 返回
        }
        if(!Tools.fileIsExists(wavePath)) { // 如果选中文件不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            return; // 返回
        }
        Logger.i("TopLayoutSaveInvoke loadRefFile= " + wavePath); // 记录载入文件路径日志
        loadWavChannelFromFile(wavePath);//wav // 载入WAV文件到Ref通道
    }

    /**
     * 将WAV文件载入到可用的Ref参考通道
     * @param wavPath WAV文件的绝对路径
     */
    private void loadWavChannelFromFile(String wavPath) { // 载入WAV文件到Ref通道
        int refChan = getUsableRefChannelNumber(); // 获取可用的Ref通道编号
        if (!TChan.isRef(refChan)) return; // 如果不是有效的Ref通道则返回
        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChan)); // 获取Ref通道对象
        if (refChannel == null) return; // 如果通道对象为空则返回
        DialogRefRecallBean item = createRecallBean(wavPath); // 创建Ref召回数据Bean
        boolean isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChan), wavPath); // 读取WAV文件到Ref通道
        if (isLoadSuccess) { // 如果载入成功
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan, String.valueOf(true)); // 缓存Ref通道已选中
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan, String.valueOf(true)); // 缓存Ref通道由用户添加
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + refChan, String.valueOf(0)); // 缓存Ref类型为0（WAV）
            double scaleVal = refChannel.getRefTimeScaleVal(); // 获取Ref通道的时间刻度值
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + refChan, getStringRefScale(TChan.toFpgaChNo(refChan), scaleVal)); // 缓存Ref时间刻度字符串
            setLabel(refChannel.getLabel(), refChan); // 设置Ref通道标签
            ChannelFactory.chEnable(TChan.toFpgaChNo(refChan), true); // 使能Ref通道
            Command.get().getChannel().Display(TChan.toFpgaChNo(refChan), true, false); // 发送显示通道命令
            Command.get().getReference().Enable(TChan.toFpgaChNo(refChan), true, false); // 发送使能Ref命令
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan, item.getPathFile()); // 缓存当前Ref数据选择路径
            RightMsgRefForEight msgRef = new RightMsgRefForEight(); // 创建Ref右侧消息对象
            msgRef.setRefChannelNumber(refChan); // 设置Ref通道编号
            msgRef.setRefChecked(true); // 设置Ref已选中
            msgRef.setLabel(refChannel.getLabel()); // 设置Ref标签
            RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_REF, msgRef); // 发送Ref更新事件，通知右侧菜单刷新
        }
    }

    /**
     * 根据文件路径创建Ref召回数据Bean
     * @param csvPath 文件路径（虽然参数名为csvPath，实际也用于WAV文件）
     * @return 包含文件信息的Ref召回数据Bean
     */
    private DialogRefRecallBean createRecallBean(String csvPath) { // 创建Ref召回数据Bean
        File file = new File(csvPath); // 创建文件对象
        long time = file.lastModified(); // 获取文件最后修改时间
        @SuppressLint("SimpleDateFormat") String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)); // 格式化修改时间
        DialogRefRecallBean item = new DialogRefRecallBean(); // 创建Ref召回Bean
        item.setLastModifyTime(time); // 设置最后修改时间戳
        item.setPathFile(csvPath); // 设置文件路径
        item.setTime(ctime); // 设置格式化的修改时间
        item.setTitle(file.getName().replace(".mwav", "").replace(".wav", "").replace(".csv", "")); // 设置标题（去除扩展名）
        return item; // 返回创建的Bean
    }

    /**
     * 设置Ref通道的标签，同时更新缓存和波形管理
     * @param label 标签名称
     * @param refChan Ref通道编号
     */
    private void setLabel(String label, int refChan) { // 设置Ref通道标签
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + refChan, label); // 缓存通道标签
        WaveManage.get().setChannelLabel(refChan, label); // 设置波形管理中的通道标签
        setChannelLabel(TChan.toFpgaChNo(refChan), label); // 设置Ref通道对象的标签
    }

    /**
     * 设置Ref通道对象的标签
     * @param chNo FPGA通道编号
     * @param label 标签名称
     */
    public void setChannelLabel(int chNo, String label) { // 设置Ref通道对象标签
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo); // 根据FPGA通道号获取Ref通道
        if (refChannel != null) { // 如果通道不为空
            refChannel.setLabel(label); // 设置标签
        }
    }

    /**
     * 获取可用的Ref通道编号（找到第一个未被用户添加的Ref通道）
     * @return 可用的Ref通道编号，如果都被占用则返回最大的Ref通道编号
     */
    private int getUsableRefChannelNumber() { // 获取可用的Ref通道编号
        final int[] minRefChan = {TChan.R8 + TChan.Ch1}; // 初始化为最大的Ref通道编号
        TChan.foreachRef(refChan -> { // 遍历所有Ref通道
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan); // 获取Ref选中状态
            boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan); // 获取是否由用户添加
            if (!refAddByUser) { // 如果该Ref通道未被用户添加
                minRefChan[0] = Math.min(minRefChan[0], refChan); // 取最小的未被用户添加的通道编号
            }
        });
        return minRefChan[0]; // 返回可用的Ref通道编号
    }

    /**
     * 获取Ref时间刻度的字符串表示
     * @param refIndex Ref通道的FPGA索引
     * @param scale 时间刻度值
     * @return 格式化后的时间刻度字符串（如"2ms"、"5kHz"）
     */
    public static String getStringRefScale(int refIndex, double scale) { // 获取Ref时间刻度字符串
        RefChannel refChannel = ChannelFactory.getRefChannel(refIndex); // 获取Ref通道对象
        if (refChannel == null) return ""; // 通道为空则返回空字符串
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef(); // 获取Ref水平轴对象
        String tail = "s"; // 默认时间单位后缀为秒
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) { // 如果是FFT类型Ref
            tail = "Hz"; // 使用频率单位后缀
        }
        String s = TBookUtil.getMFromDouble(scale); // 将刻度值转换为带前缀的字符串（如2m、5k）
        if (TextUtils.isEmpty(s)) return ""; // 转换结果为空则返回空字符串
        return s + tail; // 拼接数值和单位后缀
    }

    /**
     * 缓存加载事件的RxJava消费者
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
        }
    };

    /**
     * 恢复缓存状态
     */
    private void setCache() { // 恢复缓存状态
        restoreWavePath(); // 恢复WAV文件路径
    }

    /**
     * 从缓存中恢复WAV文件路径列表和选择状态
     */
    private void restoreWavePath() { // 从缓存恢复WAV文件路径
        wavePathSet.clear(); // 清空当前路径集合
        String currentWavePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_WAV); // 获取当前选中的WAV路径

        String wavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_WAV); // 获取显示名称路径列表字符串
        String abWavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV); // 获取绝对路径列表字符串
        ArrayList<String> wavePathList = StrUtil.getListFromString(wavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示名称路径列表
        ArrayList<String> abWavePathList = StrUtil.getListFromString(abWavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表
        if (!Tools.fileIsExists(currentWavePath)) { // 如果当前路径文件不存在
            currentWavePath = null; // 置空当前路径
        }

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建数据列表
        FileBean currentBean = new FileBean(); // 创建当前选中项Bean
        for (int i = 0; i < abWavePathList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abWavePathList.get(i))) continue; // 跳过不存在的文件
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abWavePathList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(wavePathList.get(i)); // 设置显示名称
            if (abWavePathList.get(i).equals(currentWavePath)) { // 如果是当前选中的路径
                currentBean.setPath(abWavePathList.get(i)); // 设置当前Bean的路径
                currentBean.setDisplayName(wavePathList.get(i)); // 设置当前Bean的显示名称
            }
            dataList.add(fileBean); // 添加到数据列表
        }

        wavePathSet.addAll(dataList); // 将恢复的数据添加到路径集合
        waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器数据

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_WAV); // 从缓存恢复"仅显示文件"状态
        chIsFilesShowOnly.setChecked(isFilesShowOnly); // 设置复选框选中状态
    }

    /**
     * "仅显示文件"复选框选中变化监听器
     */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 创建监听器
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == chIsFilesShowOnly.getId()) { // 如果是"仅显示文件"复选框
                if (isChecked) { // 如果选中
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.color_Text_white)); // 设置白色文字
                    isFilesShowOnly = true; // 设置标志为true
                } else { // 如果取消选中
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.textColorNewTopViewEnable)); // 设置灰色文字
                    isFilesShowOnly = false; // 设置标志为false
                }
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_WAV, String.valueOf(isChecked)); // 保存到缓存
            }
        }
    };

    /**
     * 发送消息通知父级
     */
    private void sendMsg() { // 发送消息通知父级
        if (onDetailSendMsgListener != null) { // 如果监听器不为空
            onDetailSendMsgListener.onClick(this, false); // 通知父级点击事件
        }
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 消息发送监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 保存监听器引用
    }

    /**
     * 获取保存详情接口（WAV文件调用不需要，返回null）
     * @return 保存详情接口，此Fragment返回null
     */
    public ISaveDetail getSaveDetail() { // 获取保存详情
        return null; // WAV文件调用不需要保存详情
    }

}
