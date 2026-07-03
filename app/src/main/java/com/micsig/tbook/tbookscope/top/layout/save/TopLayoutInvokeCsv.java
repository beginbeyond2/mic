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
import com.micsig.tbook.tbookscope.LoadCache; // 导入加载缓存事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref消息类
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave; // 导入加载Ref/CSV/WAV对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean; // 导入Ref回调Bean
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入播放音效工具类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理工具类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制工具类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.tbookscope.util.FileSelector; // 导入文件选择器类
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入波形管理类
import com.micsig.tbook.ui.FixedSizeHashSet; // 导入固定大小HashSet类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入顶部下拉选择视图类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类
import com.molihuan.pathselector.PathSelector; // 导入路径选择器类
import com.molihuan.pathselector.dao.SelectConfigData; // 导入选择配置数据类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean类
import com.molihuan.pathselector.entity.FontBean; // 导入字体Bean类
import com.molihuan.pathselector.fragment.BasePathSelectFragment; // 导入路径选择Fragment基类
import com.molihuan.pathselector.listener.CommonItemListener; // 导入通用项监听器
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl; // 导入配置数据构建器
import com.molihuan.pathselector.utils.DToastDialog; // 导入对话框Toast类
import com.molihuan.pathselector.utils.MConstants; // 导入常量类

import java.io.File; // 导入文件类
import java.text.SimpleDateFormat; // 导入简单日期格式化类
import java.util.ArrayList; // 导入动态数组类
import java.util.Date; // 导入日期类
import java.util.HashMap; // 导入HashMap类
import java.util.Iterator; // 导入迭代器类
import java.util.List; // 导入列表接口
import java.util.Map; // 导入映射接口
import java.util.concurrent.ConcurrentHashMap; // 导入并发HashMap类

import io.reactivex.rxjava3.annotations.NonNull; // 导入NonNull注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - CSV文件载入界面                              ║
 * ║  核心职责: 提供CSV文件的浏览、选择和载入功能，将CSV数据加载到Ref通道     ║
 * ║  架构设计: Fragment + RxBus事件驱动，通过LoadCsv解析CSV数据            ║
 * ║  数据流向: CSV文件 → LoadCsv解析 → DialogLoadRefCsvWave通道映射       ║
 * ║           → Ref通道加载 → RxBus通知UI更新                            ║
 * ║  依赖关系: 依赖 LoadCsv/SaveManage/ChannelFactory/RxBus/CacheUtil等   ║
 * ║  使用场景: 用户在顶部滑动菜单中选择CSV文件并载入到参考通道              ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * CSV文件载入界面Fragment
 * <p>提供CSV文件的浏览、选择和载入功能，支持将CSV数据映射到Ref参考通道</p>
 */
public class TopLayoutInvokeCsv extends Fragment { // CSV载入界面Fragment

    /** 日志标签 */
    private static final String TAG = "TopLayoutInvokeCsv"; // 日志标签
    /** 上下文对象 */
    public Context context; // Activity上下文
    /** 波形路径下拉选择器 */
    private TopViewSpinner waveSpinner; // 波形路径下拉选择器
    /** 载入和浏览按钮 */
    private Button btnWaveInvoke, btnWaveBrowse; // 载入/浏览按钮
    /** 仅显示文件复选框 */
    private CheckBox chIsFilesShowOnly; // 仅显示文件复选框
    /** 波形路径集合，最多10个 */
    private final FixedSizeHashSet<FileBean> wavePathSet = new FixedSizeHashSet<>(10); // 波形路径集合
    /** 加载Ref/CSV/WAV对话框 */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave; // 通道映射对话框

    /** 是否仅显示文件标志 */
    private boolean isFilesShowOnly = true; // 仅显示文件标志
    /** 对话框Toast */
    private DToastDialog dToastDialog = new DToastDialog(); // 对话框Toast
    /** 详情消息监听器 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息监听器
    /** 文件选择器 */
    private FileSelector fileSelector ; // 文件选择器

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
        return inflater.inflate(R.layout.layout_save_invoke_wav, container, false); // 填充WAV载入布局（CSV复用）
    }

    /**
     * 视图创建完成回调
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成
        this.context = getActivity(); // 获取Activity上下文
        initView(view, savedInstanceState); // 初始化视图
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化所有视图控件
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图控件
        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv); // 获取通道映射对话框
        waveSpinner = view.findViewById(R.id.wave_spinner); // 获取路径下拉选择器
        waveSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置下拉数据
                getWaveFileList(), R.layout.layout_item_for_save_directory, onWavePathItemSelectListener); // 设置标题/数据/布局/监听器
        btnWaveBrowse = view.findViewById(R.id.btn_wave_browse); // 获取浏览按钮
        btnWaveBrowse.setOnClickListener(onClickListener); // 浏览按钮设置点击监听
        btnWaveInvoke = view.findViewById(R.id.btn_wave_invoke); // 获取载入按钮
        btnWaveInvoke.setOnClickListener(onClickListener); // 载入按钮设置点击监听

        chIsFilesShowOnly = view.findViewById(R.id.check_file_only_show); // 获取仅显示文件复选框
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取通道选择图标
        chIsFilesShowOnly.setBackground(null); // 清除背景
        chIsFilesShowOnly.setButtonDrawable(null); // 清除按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界
        chIsFilesShowOnly.setCompoundDrawables(drawable, null, null, null); // 设置左侧图标
        chIsFilesShowOnly.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置选中变化监听

        fileSelector = new FileSelector(context, (selectedPath) -> { // 创建文件选择器
            addSelectToPathSet(selectedPath); // 添加选中路径到集合
        });
    }

    /**
     * 初始化事件控制，订阅RxBus事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅加载缓存事件
    }

    /**
     * 添加选中路径到路径集合
     * @param pathStr 选中的路径
     */
    private void addSelectToPathSet(FileBean pathStr) { // 添加选中路径
        addPathToWaveSet(pathStr); // 添加到波形路径集合
    }

    /**
     * 添加路径到波形路径集合并更新UI
     * @param pathStr 路径
     */
    private void addPathToWaveSet(FileBean pathStr) { // 添加路径到波形集合
        handleAddPath(pathStr); // 处理路径添加
        wavePathSet.add(pathStr); // 添加到集合
        waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器
        saveWavePathToCache(); // 保存路径到缓存
    }

    /**
     * 处理路径添加，避免重复
     * @param pathStr 要添加的路径
     * @return true表示是新路径
     */
    private boolean handleAddPath(FileBean pathStr) { // 处理路径添加
        boolean canAdd = true; // 是否可添加标志
        FileBean temp = null; // 临时存储已存在的路径
        for (FileBean fileBean : wavePathSet) { // 遍历已有路径
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径已存在
                canAdd = false; // 标记不可添加
                temp = fileBean; // 记录已存在的路径
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的路径
            wavePathSet.remove(temp); // 移除旧的
        }
        return canAdd; // 返回是否可添加
    }

    /**
     * 获取波形文件列表（倒序）
     * @return 文件Bean列表
     */
    private ArrayList<FileBean> getWaveFileList() { // 获取波形文件列表
        return wavePathSet.getReverseList(); // 返回倒序列表
    }

    /**
     * 保存波形路径到缓存
     */
    public void saveWavePathToCache() { // 保存波形路径到缓存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_CSV, // 缓存显示路径
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_CSV, // 缓存绝对路径
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(wavePathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV, waveSpinner.getSelectItem()); // 缓存当前路径

    }


    /** 路径下拉选择器项选中监听器 */
    TopViewSpinner.onItemSelectListener onWavePathItemSelectListener = str -> { // 路径选中监听
        if (Tools.fileIsExists(str.getPath())) { // 如果路径存在
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV, str.getDisplayName()); // 缓存当前路径
            addPathToWaveSet(str); // 添加到波形集合
        } else { // 如果路径不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            deleteEmptyItem(str); // 删除空项
        }
    };

    /** 点击事件监听器 */
    @SuppressLint("NonConstantResourceId")
    private View.OnClickListener onClickListener = v -> { // 点击事件监听
        PlaySound.getInstance().playButton(); // 播放按钮音效
        switch (v.getId()) { // 根据点击的控件ID分发
            case R.id.btn_wave_browse: // 浏览按钮
                handleBrowse(); // 处理浏览
                break;
            case R.id.btn_wave_invoke: // 载入按钮
                loadRefFromFile(); // 从文件载入Ref
                break;
        }
    };

    /**
     * 删除空路径项
     * @param filePath 要删除的路径
     */
    private void deleteEmptyItem(FileBean filePath) { // 删除空项
        deleteEmptyWaveItem(filePath); // 删除空的波形项
    }

    /**
     * 删除空的波形路径项
     * @param filePath 要删除的路径
     */
    private void deleteEmptyWaveItem(FileBean filePath) { // 删除空波形项
        boolean deleteSuccess = wavePathSet.remove(filePath); // 从集合中移除
        if (deleteSuccess) { // 如果移除成功
            waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器
            saveWavePathToCache(); // 保存路径到缓存
        }
    }


    /**
     * 处理浏览操作
     */
    private void handleBrowse() { // 处理浏览
        handleBrowseClick(); // 处理浏览点击
    }

    /**
     * 处理浏览按钮点击，打开CSV文件选择器
     */
    private void handleBrowseClick() { // 处理浏览点击
        String spinnerSelectPath= waveSpinner.getSelectItem(); // 获取当前选中路径
        String disPlay = waveSpinner.getDisPlaySelectItem(); // 获取显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象

        if(!file.exists()){ // 如果路径不存在
            spinnerSelectPath = "/storage/emulated/0"; // 默认内部存储路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 默认显示名
        }else{ // 如果路径存在
            spinnerSelectPath = file.getParent(); // 使用父目录
        }
        String[] waveFileType = {"csv"}; // CSV文件类型过滤
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, waveFileType); // 构建载入文件选择器
    }

    /**
     * 从文件载入Ref通道数据
     */
    private void loadRefFromFile() { // 从文件载入Ref
        String wavePath = waveSpinner.getSelectItem(); // 获取选中路径
        if (StrUtil.isEmpty(wavePath)) { // 如果路径为空
            DToast.get().show(R.string.top_slip_select_file_first); // 提示先选择文件
            return; // 返回
        }
        if(!Tools.fileIsExists(wavePath)) { // 如果文件不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            return; // 返回
        }
        loadCsvChannelFromFile(wavePath); // 加载CSV通道数据
    }

    /** CSV中包含的通道列表 */
    private ArrayList<Integer> channelInCsv = new ArrayList<>(); // CSV中包含的通道列表

    /**
     * 从CSV文件加载通道数据
     * @param csvPath CSV文件路径
     */
    private void loadCsvChannelFromFile(String csvPath) { // 加载CSV通道数据
        LoadCsv loadCsv = new LoadCsv(); // 创建CSV加载器
        boolean loadSuccess = false; // 加载成功标志
        try { // 捕获异常
            loadSuccess = loadCsv.load(csvPath); // 加载CSV文件
        } catch (Exception e) { // 捕获异常
            e.printStackTrace(); // 打印异常堆栈
        } finally { // 最终处理
            if (!loadSuccess) { // 如果加载失败
                DToast.get().show(context.getResources().getString(R.string.file_load_fail)); // 提示加载失败
            }
        }
        Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + loadSuccess); // 打印通道数和加载状态
        if (!loadSuccess) return; // 加载失败则返回
        channelInCsv.clear(); // 清空通道列表
        channelInCsv = loadCsv.getCsvInfos(); // 获取CSV中的通道信息
        for (int c : channelInCsv) { // 遍历通道
            Log.d(TAG, "ch" + c); // 打印通道号
        }
        DialogRefRecallBean item = createRecallBean(csvPath); // 创建回调Bean
        dialogLoadRefCsvWave.setData(channelInCsv, new DialogLoadRefCsvWave.OnDismissListener() { // 设置通道映射对话框数据
            @Override
            public void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef) { // 对话框关闭回调
                if (channelToRef.size() <= 0) return; // 如果没有映射则返回
                loadCsvInBackGround(channelToRef, loadCsv, item); // 后台加载CSV到Ref
            }
        });
    }

    /**
     * 创建Ref回调Bean
     * @param csvPath CSV文件路径
     * @return 回调Bean对象
     */
    private DialogRefRecallBean createRecallBean(String csvPath) { // 创建回调Bean
        File file = new File(csvPath); // 创建文件对象
        long time = file.lastModified(); // 获取最后修改时间
        @SuppressLint("SimpleDateFormat") String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)); // 格式化时间
        DialogRefRecallBean item = new DialogRefRecallBean(); // 创建回调Bean
        item.setLastModifyTime(time); // 设置最后修改时间
        item.setPathFile(csvPath); // 设置文件路径
        item.setTime(ctime); // 设置格式化时间
        item.setTitle(file.getName()); // 设置标题为文件名
        return item; // 返回回调Bean
    }

    /**
     * 后台加载CSV数据到Ref通道
     * @param channelToRef 通道到Ref的映射
     * @param loadCsv CSV加载器
     * @param item 回调Bean
     */
    public void loadCsvInBackGround(ConcurrentHashMap<Integer, Integer> channelToRef, LoadCsv loadCsv, DialogRefRecallBean item) { // 后台加载CSV
        new Thread(() -> { // 在新线程中执行
            Logger.d(TAG, "channelToRef= " + channelToRef.toString()); // 打印通道映射日志
            for (Map.Entry<Integer, Integer> entry : channelToRef.entrySet()) { // 遍历映射
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_FROM + TChan.toUiChNo(entry.getKey()), // 缓存Ref数据来源
                        item.getPathFile() + ";" + TChan.toUiChNo(entry.getValue()));
            }
            loadCsv.setLoadCsvProgress(val -> updateLoadProgress(val)); // 设置加载进度回调
            loadCsv.loadToRef(channelToRef); // 加载CSV到Ref通道
            while (!loadCsv.isFinish()) { // 等待加载完成
                ms_sleep(100); // 每100毫秒检查一次
            }
            requireActivity().runOnUiThread(() -> { // 切换到UI线程
                channelToRef.forEach((key, value) -> { // 遍历映射
                    ChannelFactory.chOpen(key); // 开启通道
                    setCacheMapValue(key, item); // 设置缓存值
                });
            });
        }).start(); // 启动线程
    }

    /**
     * 更新加载进度
     * @param progress 进度百分比
     */
    private void updateLoadProgress(int progress) { // 更新加载进度
        requireActivity().runOnUiThread(() -> { // 切换到UI线程
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            if (progress < 0 || progress >= 100) { // 如果进度无效或已完成
                if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕被锁定
                    screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
                }
            } else { // 如果进度在进行中
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕未被锁定
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕
                }
                screenControls.setProgressValue(progress); // 设置进度值
            }
        });
    }


    /**
     * 设置Ref通道的缓存值
     * @param key 通道索引
     * @param item 回调Bean
     */
    private void setCacheMapValue(int key, DialogRefRecallBean item) { // 设置缓存值
        if (item == null) return; // 如果Bean为空则返回
        int chanId = TChan.toUiChNo(key); // 转换为UI通道号
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chanId, String.valueOf(true)); // 缓存用户添加标志
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + chanId, String.valueOf(true)); // 缓存Ref选中状态
        int rightRefSlipType = 1;//csv
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + chanId, String.valueOf(rightRefSlipType)); // 缓存Ref类型为CSV
        double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(chanId)).getRefTimeScaleVal(); // 获取Ref时间比例值
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + chanId, getStringRefScale(TChan.toFpgaChNo(chanId), scaleVal)); // 缓存Ref比例
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + chanId, item.getPathFile()); // 缓存当前Ref数据路径
        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip(); // 隐藏所有滑动对话框
        String label = getLabelFromChannel(key); // 获取通道标签
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + chanId, label); // 缓存通道标签
        WaveManage.get().setChannelLabel(chanId, label); // 设置通道标签
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_STATE, chanId); // 通知Ref状态更新
    }

    /**
     * 获取Ref比例字符串
     * @param refIndex Ref通道索引
     * @param scale 比例值
     * @return 比例字符串
     */
    public static String getStringRefScale(int refIndex, double scale) { // 获取Ref比例字符串
        RefChannel refChannel = ChannelFactory.getRefChannel(refIndex); // 获取Ref通道
        if (refChannel == null) return ""; // 通道为空返回空字符串
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef(); // 获取Ref水平轴
        String tail = "s"; // 默认时间单位
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) { // 如果是FFT类型
            tail = "Hz"; // 使用频率单位
        }
        String s = TBookUtil.getMFromDouble(scale); // 格式化比例值
        if (TextUtils.isEmpty(s)) return ""; // 空值返回空字符串
        return s + tail; // 返回比例值加单位
    }

    /**
     * 从通道号获取标签
     * @param chNo 通道号
     * @return 标签字符串
     */
    public String getLabelFromChannel(int chNo) { // 获取通道标签
        String label = ""; // 标签字符串
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo); // 获取Ref通道
        if (refChannel != null) { // 如果通道不为空
            label = refChannel.getLabel(); // 获取标签
        }
        return label; // 返回标签
    }


    /**
     * 线程休眠工具方法
     * @param ms 休眠时间（毫秒）
     */
    private void ms_sleep(long ms) { // 线程休眠
        try { // 捕获中断异常
            Thread.sleep(ms); // 让当前线程休眠
        } catch (InterruptedException e) { // 捕获中断异常
            throw new RuntimeException(e); // 包装为运行时异常
        }
    }

    /** 加载缓存事件消费者 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 加载缓存消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收加载缓存事件
            setCache(); // 设置缓存
        }
    };

    /**
     * 从缓存恢复UI状态
     */
    private void setCache() { // 设置缓存
        restoreWavePath(); // 恢复波形路径
    }

    /**
     * 从缓存恢复波形路径
     */
    private void restoreWavePath() { // 恢复波形路径
        wavePathSet.clear(); // 清空路径集合
        String currentWavePath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH_CURRENT + CacheUtil.WAVE_TYPE_CSV); // 获取当前路径缓存
        String wavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_PATH + CacheUtil.WAVE_TYPE_CSV); // 获取显示路径列表缓存
        String abWavePathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_CSV); // 获取绝对路径列表缓存
        ArrayList<String> wavePathList = StrUtil.getListFromString(wavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示路径列表
        ArrayList<String> abWavePathList = StrUtil.getListFromString(abWavePathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表
        if (!Tools.fileIsExists(currentWavePath)) { // 如果当前路径不存在
            currentWavePath = null; // 设为null
        }

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建文件Bean列表
        FileBean currentBean = new FileBean(); // 创建当前路径Bean
        for (int i = 0; i < abWavePathList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abWavePathList.get(i))) continue; // 跳过不存在的路径
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abWavePathList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(wavePathList.get(i)); // 设置显示名称
            if(abWavePathList.get(i).equals(currentWavePath)) { // 如果是当前路径
                currentBean.setPath(abWavePathList.get(i)); // 设置当前路径绝对路径
                currentBean.setDisplayName(wavePathList.get(i)); // 设置当前路径显示名称
            }
            dataList.add(fileBean); // 添加到列表
        }

        wavePathSet.addAll(dataList); // 添加所有路径到集合
        waveSpinner.updateDataList(getWaveFileList(), null); // 更新下拉选择器

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_CSV); // 获取文件过滤缓存
        chIsFilesShowOnly.setChecked(isFilesShowOnly); // 设置复选框状态
    }

    /** 仅显示文件复选框变化监听器 */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 复选框变化监听
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == chIsFilesShowOnly.getId()) { // 仅显示文件复选框
                if (isChecked) { // 如果选中
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.color_Text_white)); // 设置白色文字
                    isFilesShowOnly = true; // 设置仅显示文件标志
                } else { // 如果取消选中
                    chIsFilesShowOnly.setTextColor(getResources().getColor(R.color.textColorNewTopViewEnable)); // 设置启用色文字
                    isFilesShowOnly = false; // 清除仅显示文件标志
                }
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_WAVE_FILE_FILTER + CacheUtil.WAVE_TYPE_CSV, String.valueOf(isChecked)); // 缓存过滤状态
            }
        }
    };

    /**
     * 发送详情消息
     */
    private void sendMsg() { // 发送消息
        if (onDetailSendMsgListener != null) { // 如果监听器不为空
            onDetailSendMsgListener.onClick(this, false); // 发送点击消息
        }
    }

    /**
     * 设置详情消息监听器
     * @param onDetailSendMsgListener 详情消息监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器
    }

    /**
     * 获取保存详情
     * @return 保存详情接口（当前返回null）
     */
    public ISaveDetail getSaveDetail() { // 获取保存详情
        return null; // 返回null
    }

}
