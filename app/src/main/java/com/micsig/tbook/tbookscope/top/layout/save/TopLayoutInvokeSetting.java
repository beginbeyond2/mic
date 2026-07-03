package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.annotation.SuppressLint; // 导入SuppressLint注解
import android.content.Context; // 导入上下文类
import android.graphics.Color; // 导入颜色类
import android.graphics.drawable.Drawable; // 导入Drawable图形类
import android.icu.text.RelativeDateTimeFormatter; // 导入相对时间格式化类
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
import android.widget.Toast; // 导入Toast提示类

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
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入顶部单选组类
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
import com.molihuan.pathselector.utils.Mtools; // 导入路径选择器工具类

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
 * │ 模块定位：保存/调用功能 → 顶部布局 → 设置文件调用（Invoke Setting）          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供设置文件（SaveRecovery）的浏览、选择和载入功能界面，             │
 * │          用户可通过此Fragment浏览存储中的设置文件，并将其载入到示波器中         │
 * │          恢复之前的仪器配置状态                                                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，作为顶部滑动菜单中的一个Tab页面，                       │
 * │          通过RxBus订阅缓存加载事件，通过FileSelector进行文件浏览，             │
 * │          通过SaveManage.loadUserSetFromFilePath()载入设置文件                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(路径缓存) → settingPathSet(路径集合) →                   │
 * │          settingSpinner(下拉列表) → SaveManage(载入设置) →                   │
 * │          MainActivity(刷新界面)                                               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewSpinner, FileSelector, SaveManage, CacheUtil,              │
 * │          RxBus, HorizontalAxis, Scope, MainActivity                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部菜单选择"调用→设置"时显示此界面，                          │
 * │          浏览并选择SaveRecovery文件来恢复示波器配置                            │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutInvokeSetting extends Fragment {

    /** 日志标签，用于标识当前类的日志输出 */
    private static final String TAG = "TopLayoutInvokeSetting"; // 日志标签
    /** Fragment所在的上下文环境，即Activity */
    public Context context; // Activity上下文
    /** 设置文件路径下拉选择器，用于显示和选择已保存的设置文件路径 */
    private TopViewSpinner settingSpinner; // 设置文件路径下拉选择器
    /** 调用按钮，点击后载入选中的设置文件 */
    private Button btnSettingInvoke; // 调用按钮
    /** 浏览按钮，点击后打开文件浏览器选择设置文件 */
    private Button btnSettingBrowse; // 浏览按钮
    /** 是否仅显示文件的复选框，控制文件浏览器中是否只显示文件 */
    private CheckBox chIsFilesShowOnly; // 仅显示文件复选框
    /** 设置文件路径集合，固定最大容量10，存储最近使用的设置文件路径 */
    private final FixedSizeHashSet<FileBean> settingPathSet = new FixedSizeHashSet<>(10); // 设置文件路径集合
    /** Ref/Csv加载对话框，此界面中未直接使用但保留引用 */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave; // Ref/Csv加载对话框

    /** 是否仅显示文件的标志位，默认为true */
    private boolean isFilesShowOnly = true; // 仅显示文件标志
    /** 自定义Toast对话框，用于显示提示信息 */
    private DToastDialog dToastDialog = new DToastDialog(); // 对话框Toast
    /** 详情消息发送监听器，用于与父级通信 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息监听器
    /** 文件选择器，用于浏览和选择设置文件 */
    private FileSelector fileSelector; // 文件选择器

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的设置文件调用布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_invoke_setting, container, false); // 填充设置文件调用布局
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

        settingSpinner = view.findViewById(R.id.setting_spinner); // 获取设置文件路径下拉选择器
        settingSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置默认提示文本
                getSettingFileList(), R.layout.layout_item_for_save_directory, onSettingPathItemSelectListener); // 设置数据列表、布局和选择监听器
        btnSettingBrowse = view.findViewById(R.id.btn_setting_browse); // 获取浏览按钮
        btnSettingBrowse.setOnClickListener(onClickListener); // 设置浏览按钮点击监听器
        btnSettingInvoke = view.findViewById(R.id.btn_setting_invoke); // 获取调用按钮
        btnSettingInvoke.setOnClickListener(onClickListener); // 设置调用按钮点击监听器

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
        addPathToSettingSet(pathStr); // 添加到设置文件路径集合
    }

    /**
     * 将路径添加到设置文件路径集合，并更新UI和缓存
     * @param pathStr 要添加的文件路径Bean
     */
    private void addPathToSettingSet(FileBean pathStr) { // 添加路径到设置文件集合
        handleAddPath(pathStr); // 处理重复路径（先移除旧的再添加新的）
        settingPathSet.add(pathStr); // 将路径添加到集合
        settingSpinner.updateDataList(getSettingFileList(), null); // 更新下拉选择器数据列表
        saveSettingPathToCache(); // 保存路径到缓存
    }

    /**
     * 处理添加路径时的重复检查，如果路径已存在则先移除旧的
     * @param pathStr 要检查的文件路径Bean
     * @return true表示可以新增，false表示路径已存在（已移除旧的）
     */
    private boolean handleAddPath(FileBean pathStr) { // 处理路径添加去重
        boolean canAdd = true; // 默认可以添加
        FileBean temp = null; // 记录已存在的同路径项
        for (FileBean fileBean : settingPathSet) { // 遍历已有路径集合
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径相同
                canAdd = false; // 标记不可新增
                temp = fileBean; // 记录已存在的项
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的项
            settingPathSet.remove(temp); // 先移除旧的，后续会添加新的（实现移到最前）
        }
        return canAdd; // 返回是否为新增
    }


    /**
     * 获取设置文件列表（倒序，最近使用的在前）
     * @return 设置文件路径Bean列表
     */
    private ArrayList<FileBean> getSettingFileList() { // 获取设置文件列表
        return settingPathSet.getReverseList(); // 返回倒序列表，最近添加的排在前面
//        return settingPathSet.getPositiveList();
    }

    /**
     * 将设置文件路径信息保存到缓存
     */
    public void saveSettingPathToCache() { // 保存路径到缓存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SETTING_FILE_PATH, // 保存显示名称路径列表
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将路径集合转为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SETTING_FILE_ABSOLUTE_PATH, // 保存绝对路径列表
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 将绝对路径集合转为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SETTING_FILE_PATH_CURRENT, settingSpinner.getSelectItem()); // 保存当前选中的路径
    }


    /**
     * 设置文件路径下拉选择器的项选择监听器
     */
    TopViewSpinner.onItemSelectListener onSettingPathItemSelectListener = str -> { // 选中某项时的回调
        if (Tools.fileIsExists(str.getPath())) { // 如果选中的路径文件存在
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SETTING_FILE_PATH_CURRENT, str.getDisplayName()); // 更新当前选中路径缓存
            addPathToSettingSet(str); // 添加到路径集合（移到最前）
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
            case R.id.btn_setting_browse: // 浏览按钮
                handleBrowse(); // 处理浏览操作
                break;
            case R.id.btn_setting_invoke: // 调用按钮
                loadSettingFile(); // 载入设置文件
                break;
        }
    };

    /**
     * 删除无效的路径项
     * @param filePath 要删除的文件路径Bean
     */
    private void deleteEmptyItem(FileBean filePath) { // 删除无效路径项
        deleteEmptySettingItem(filePath); // 委托给删除设置文件项方法
    }


    /**
     * 从路径集合和下拉列表中删除无效的设置文件项
     * @param filePath 要删除的文件路径Bean
     */
    private void deleteEmptySettingItem(FileBean filePath) { // 删除空设置文件项
        boolean deleteSuccess = settingPathSet.remove(filePath); // 从集合中移除
        if (deleteSuccess) { // 如果移除成功
            settingSpinner.updateDataList(getSettingFileList(), null); // 更新下拉选择器数据
            saveSettingPathToCache(); // 保存更新后的路径到缓存
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
        String spinnerSelectPath= settingSpinner.getSelectItem(); // 获取当前下拉选择器选中的路径
        String disPlay = settingSpinner.getDisPlaySelectItem(); // 获取当前选中项的显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象
        File f = new File("/storage/emulated/0/smart"); // 检查smart目录是否存在
        String scopePath; // 示波器存储路径名
        if(f.exists()){ // 如果smart目录存在
            scopePath= Tools.SMART_PATH; // 使用smart路径
        }else{ // smart目录不存在
            scopePath= Tools.SCOPE_PATH; // 使用scope路径
        }
        if(!file.exists()){ // 如果当前选中路径不存在
            spinnerSelectPath = "/storage/emulated/0/"+scopePath+"/default"; // 使用默认路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 显示"内部存储"
        }else{ // 路径存在
            spinnerSelectPath = file.getParent(); // 使用父目录作为起始路径
        }
        String[] settingFileType = {"SaveRecovery"}; // 设置文件类型过滤器
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, settingFileType); // 构建并打开文件选择器
    }


    /**
     * 载入选中的设置文件，恢复示波器配置
     */
    private void loadSettingFile() { // 载入设置文件
        if (StrUtil.isEmpty(settingSpinner.getSelectItem())) { // 如果未选择文件
            DToast.get().show(R.string.top_slip_select_file_first); // 提示先选择文件
            return; // 返回
        }
        if (!Tools.fileIsExists(settingSpinner.getSelectItem())) { // 如果选中文件不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            return; // 返回
        }
        Scope.getInstance().enableCommand(false); // 禁用命令发送，防止载入过程中冲突
        CacheUtil.get().initStateCacheLoad(); // 初始化缓存加载状态
        boolean loadSuccess = false; // 载入成功标志
        try { // 捕获中断异常
            ((MainActivity) context).preMainLoadCahceProcess(); // 执行载入前处理
            loadSuccess = SaveManage.getInstance().loadUserSetFromFilePath(settingSpinner.getSelectItem(), CacheUtil.get().getCacheMap()); // 从文件路径载入用户设置
            Logger.i("recoveryName= " + settingSpinner.getSelectItem() + " loadSuccess= " + loadSuccess); // 记录载入结果日志
            if (!loadSuccess) { // 如果载入失败
                //配置载入失败则清空配置载入默认配置值
                CacheUtil.get().clearCacheMap(); // 清空缓存映射
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS); // 恢复默认时基2ms
                horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0); // 恢复默认时基位置0
                DToast.get().show(R.string.saveRecoveryFileIsNotExist); // 提示设置文件不存在或无效
            }
            //刷新界面
            ((MainActivity) context).updateMainLoadCaheProcess(loadSuccess); // 更新主界面载入进度
            ((MainActivity) context).postMainLoadCacheProcess(); // 执行载入后处理
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈
        } finally { // 最终处理
            Scope.getInstance().enableCommand(true); // 无论成功与否，重新启用命令发送
        }
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
        restoreSettingPath(); // 恢复设置文件路径
    }

    /**
     * 从缓存中恢复设置文件路径列表和选择状态
     */
    private void restoreSettingPath() { // 从缓存恢复设置文件路径
        settingPathSet.clear(); // 清空当前路径集合
        String settingCurrentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SETTING_FILE_PATH_CURRENT); // 获取当前选中的路径
        String settingPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SETTING_FILE_PATH); // 获取显示名称路径列表字符串
        String abSettingPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SETTING_FILE_ABSOLUTE_PATH); // 获取绝对路径列表字符串
        ArrayList<String> settingPathList = StrUtil.getListFromString(settingPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示名称路径列表
        ArrayList<String> abSettingPathList = StrUtil.getListFromString(abSettingPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表
        if (!Tools.fileIsExists(settingCurrentPath)) { // 如果当前路径文件不存在
            settingCurrentPath = null; // 置空当前路径
        }

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建数据列表
        FileBean currentBean = new FileBean(); // 创建当前选中项Bean
        for (int i = 0; i < abSettingPathList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abSettingPathList.get(i))) continue; // 跳过不存在的文件
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abSettingPathList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(abSettingPathList.get(i)); // 设置显示名称（设置文件用绝对路径显示）
            if(settingPathList.get(i).contains(settingCurrentPath)) { // 如果是当前选中的路径
                currentBean.setPath(abSettingPathList.get(i)); // 设置当前Bean的路径
                currentBean.setDisplayName(settingPathList.get(i)); // 设置当前Bean的显示名称
            }
            dataList.add(fileBean); // 添加到数据列表
        }

        settingPathSet.addAll(dataList); // 将恢复的数据添加到路径集合
        settingSpinner.updateDataList(getSettingFileList(), null); // 更新下拉选择器数据

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_SETTING_FILE_FILTER); // 从缓存恢复"仅显示文件"状态
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
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_SETTING_FILE_FILTER, String.valueOf(isChecked)); // 保存到缓存
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
     * 获取保存详情接口（设置文件调用不需要，返回null）
     * @return 保存详情接口，此Fragment返回null
     */
    public ISaveDetail getSaveDetail() { // 获取保存详情
        return null; // 设置文件调用不需要保存详情
    }

}
