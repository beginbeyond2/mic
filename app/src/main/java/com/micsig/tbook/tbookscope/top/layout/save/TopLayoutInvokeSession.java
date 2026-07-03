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
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 导入确认对话框类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref消息类
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave; // 导入加载Ref/CSV/WAV对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean; // 导入Ref回调Bean
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
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
 * ║  模块定位: 保存功能模块 - 会话文件载入界面                              ║
 * ║  核心职责: 提供会话文件(.mss)的浏览、选择和载入恢复功能                  ║
 * ║  架构设计: Fragment + RxBus事件驱动，通过SaveRecoverySession恢复会话   ║
 * ║  数据流向: .mss文件 → SaveRecoverySession.restore() → CacheUtil恢复   ║
 * ║           → MainActivity刷新界面 → RxBus通知状态更新                  ║
 * ║  依赖关系: 依赖 SaveRecoverySession/Scope/Command/RxBus/CacheUtil等    ║
 * ║  使用场景: 用户在顶部滑动菜单中选择会话文件并恢复仪器状态                ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * 会话文件载入界面Fragment
 * <p>提供会话文件(.mss)的浏览、选择和载入恢复功能，恢复示波器的完整工作状态</p>
 */
public class TopLayoutInvokeSession extends Fragment { // 会话载入界面Fragment

    /** 日志标签 */
    private static final String TAG = "TopLayoutInvokeSession"; // 日志标签
    /** 浏览类型：会话 */
    private static final int BROWSE_SESSION = 3;//会话

    /** 上下文对象 */
    public Context context; // Activity上下文
    /** 会话路径下拉选择器 */
    private TopViewSpinner sessionSpinner; // 会话路径下拉选择器
    /** 载入和浏览按钮 */
    private Button btnSessionInvoke, btnSessionBrowse; // 载入/浏览按钮
    /** 仅显示文件复选框 */
    private CheckBox chIsFilesShowOnly; // 仅显示文件复选框
    /** 会话路径集合，最多10个 */
    private final FixedSizeHashSet<FileBean> sessionPathSet = new FixedSizeHashSet<>(10); // 会话路径集合
    /** 加载Ref/CSV/WAV对话框 */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave; // 通道映射对话框

    /** 是否仅显示文件标志 */
    private boolean isFilesShowOnly = true; // 仅显示文件标志
    /** 对话框Toast */
    private DToastDialog dToastDialog = new DToastDialog(); // 对话框Toast
    /** 详情消息监听器 */
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息监听器
    /** 确认对话框 */
    private DialogOk dialogOnlyOk; // 确认对话框
    /** 文件选择器 */
    private FileSelector fileSelector; // 文件选择器

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
        return inflater.inflate(R.layout.layout_save_invoke_session, container, false); // 填充会话载入布局
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

        chIsFilesShowOnly = view.findViewById(R.id.check_file_only_show); // 获取仅显示文件复选框
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取通道选择图标
        chIsFilesShowOnly.setBackground(null); // 清除背景
        chIsFilesShowOnly.setButtonDrawable(null); // 清除按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界
        chIsFilesShowOnly.setCompoundDrawables(drawable, null, null, null); // 设置左侧图标
        chIsFilesShowOnly.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置选中变化监听

        sessionSpinner = view.findViewById(R.id.session_spinner); // 获取会话路径下拉选择器
        sessionSpinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置下拉数据
                getSessionFileList(), R.layout.layout_item_for_save_directory, onSessionPathItemSelectListener); // 设置标题/数据/布局/监听器
        btnSessionBrowse = view.findViewById(R.id.btn_session_browse); // 获取浏览按钮
        btnSessionBrowse.setOnClickListener(onClickListener); // 浏览按钮设置点击监听
        btnSessionInvoke = view.findViewById(R.id.btn_session_invoke); // 获取载入按钮
        btnSessionInvoke.setOnClickListener(onClickListener); // 载入按钮设置点击监听

        dialogOnlyOk = (DialogOk) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OK); // 获取确认对话框
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
        addPathToSessionSet(pathStr); // 添加到会话路径集合
    }

    /**
     * 添加路径到会话路径集合并更新UI
     * @param pathStr 路径
     */
    private void addPathToSessionSet(FileBean pathStr) { // 添加路径到会话集合
        handleAddPath(pathStr); // 处理路径添加
        sessionPathSet.add(pathStr); // 添加到集合
        sessionSpinner.updateDataList(getSessionFileList(), null); // 更新下拉选择器
        saveSessionPathToCache(); // 保存路径到缓存
    }

    /**
     * 处理路径添加，避免重复
     * @param pathStr 要添加的路径
     * @return true表示是新路径
     */
    private boolean handleAddPath(FileBean pathStr) { // 处理路径添加
        boolean canAdd = true; // 是否可添加标志
        FileBean temp = null; // 临时存储已存在的路径
        for (FileBean fileBean : sessionPathSet) { // 遍历已有路径
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径已存在
                canAdd = false; // 标记不可添加
                temp = fileBean; // 记录已存在的路径
                break; // 跳出循环
            }
        }
        if(temp != null) { // 如果找到已存在的路径
            sessionPathSet.remove(temp); // 移除旧的
        }
        return canAdd; // 返回是否可添加
    }


    /**
     * 获取会话文件列表（倒序）
     * @return 文件Bean列表
     */
    private ArrayList<FileBean> getSessionFileList() { // 获取会话文件列表
        return sessionPathSet.getReverseList(); // 返回倒序列表
//        return sessionPathSet.getPositiveList();
    }

    /**
     * 保存会话路径到缓存
     */
    private void saveSessionPathToCache() { // 保存会话路径到缓存
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH, // 缓存显示路径
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH, // 缓存绝对路径
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(sessionPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP));

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT, sessionSpinner.getSelectItem()); // 缓存当前路径
    }

    /** 路径下拉选择器项选中监听器 */
    TopViewSpinner.onItemSelectListener onSessionPathItemSelectListener = str -> { // 路径选中监听
        if (Tools.fileIsExists(str.getPath())) { // 如果路径存在
            CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT, str.getDisplayName()); // 缓存当前路径
            addPathToSessionSet(str); // 添加到会话集合
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
            case R.id.btn_session_browse: // 浏览按钮
                handleBrowse(); // 处理浏览
                break;
            case R.id.btn_session_invoke: // 载入按钮
                loadSessionFile(); // 载入会话文件
                break;
        }
    };

    /**
     * 删除空路径项
     * @param filePath 要删除的路径
     */
    private void deleteEmptyItem(FileBean filePath) { // 删除空项
        deleteEmptySessionItem(filePath); // 删除空的会话项
    }

    /**
     * 删除空的会话路径项
     * @param filePath 要删除的路径
     */
    private void deleteEmptySessionItem(FileBean filePath) { // 删除空会话项
        boolean deleteSuccess = sessionPathSet.remove(filePath); // 从集合中移除
        if (deleteSuccess) { // 如果移除成功
            sessionSpinner.updateDataList(getSessionFileList(), null); // 更新下拉选择器
            saveSessionPathToCache(); // 保存路径到缓存
        }
    }


    /**
     * 处理浏览操作
     */
    private void handleBrowse() { // 处理浏览
        handleBrowseClick(); // 处理浏览点击
    }

    /**
     * 处理浏览按钮点击，打开会话文件选择器
     */
    private void handleBrowseClick() { // 处理浏览点击
        String spinnerSelectPath= sessionSpinner.getSelectItem(); // 获取当前选中路径
        String disPlay = sessionSpinner.getDisPlaySelectItem(); // 获取显示名称
        File file = new File(spinnerSelectPath); // 创建文件对象

        if(!file.exists()){ // 如果路径不存在
            spinnerSelectPath = "/storage/emulated/0"; // 默认内部存储路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 默认显示名
        }else{ // 如果路径存在
            spinnerSelectPath = file.getParent(); // 使用父目录
        }
        String[] sessionFileType = {"mss"}; // 会话文件类型过滤
        fileSelector.buildInvokeFileSelector(spinnerSelectPath, disPlay, this, context, isFilesShowOnly, sessionFileType); // 构建载入文件选择器
    }

    /** Toast消息字符串 */
    String toastStr = ""; // Toast消息字符串

    /** 是否正在加载会话标志 */
    boolean bloadSession = false; // 是否正在加载会话

    /**
     * 线程安全地获取加载会话状态
     * @return 是否正在加载
     */
    synchronized boolean isLoadSession(){ // 获取加载状态
        return bloadSession; // 返回加载状态
    }

    /**
     * 线程安全地设置加载会话状态
     * @param bSave 是否正在加载
     */
    synchronized void setLoadSession(boolean bSave){ // 设置加载状态
        this.bloadSession = bSave; // 赋值加载状态
    }

    /**
     * 载入会话文件，恢复示波器工作状态
     */
    private void loadSessionFile() { // 载入会话文件
        if(isLoadSession()){ // 如果正在加载
            return; // 直接返回，避免重复加载
        }
        final String path = sessionSpinner.getSelectItem(); // 获取选中路径
        if (StrUtil.isEmpty(path)) { // 如果路径为空
            DToast.get().show(R.string.top_slip_select_file_first); // 提示先选择文件
            return; // 返回
        }
        if(!Tools.fileIsExists(path)) { // 如果文件不存在
            DToast.get().show(R.string.select_flie_not_exist); // 提示文件不存在
            return; // 返回
        }
        setLoadSession(true); // 设置正在加载标志
        Scope scope = Scope.getInstance(); // 获取示波器实例
        boolean oldIsRun = scope.isRun(); // 记录当前运行状态
        if (scope.isRun()) { // 如果示波器正在运行
//            scope.setRun(false);
            Command.get().getFunctionMenu().Stop(true); // 停止运行
        }else{ // 如果示波器未在运行
            Command.get().getSample().SegmentedStop(true); // 停止分段采集
        }
        new Thread(new Runnable() { // 在新线程中执行会话恢复
            @Override
            public void run() { // 线程执行体
                ms_sleep(1000); // 等待1秒确保停止完成
                File file = new File(path); // 创建文件对象
                if (file.exists()) { // 如果文件存在
                    HashMap<String, HashMap<String, String>> map = new HashMap<>(); // 创建会话数据映射
                    SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance(); // 获取会话保存恢复实例
                    saveRecoverySession.restore(map, path); // 执行会话恢复

                    while (!saveRecoverySession.isDone()) { // 等待恢复完成
                        ms_sleep(100); // 每100毫秒检查一次
                        Log.i(TAG, "LoadSession progress:" + saveRecoverySession.getSaveRecoveryProgress()); // 打印恢复进度
                        showLoadProgress(saveRecoverySession.getSaveRecoveryProgress()); // 显示恢复进度
                    }
                    toastStr = context.getResources().getString(R.string.top_slip_recovery_session_success); // 设置成功提示
                    if (saveRecoverySession.getStatus() == SaveRecoverySession.S_FAIL) { // 如果恢复失败
                        Logger.i(TAG, "LoadSession errcode= " + saveRecoverySession.getErrCode()); // 打印错误码
                        if (saveRecoverySession.getErrCode() == SaveRecoverySession.ERR_PRODUCT) { // 如果是产品型号不匹配
                            loadFileNotSupport(scope, oldIsRun); // 处理不支持的文件
                            return; // 终止恢复
                        }
                        toastStr = context.getResources().getString(R.string.top_slip_recovery_session_failed); // 设置失败提示
                        showLoadProgress(100); // 显示进度100%
                    }
                    requireActivity().runOnUiThread(new Runnable() { // 切换到UI线程
                        @Override
                        public void run() { // UI线程执行
                            Scope.getInstance().enableCommand(false); // 禁用命令
                            CacheUtil.get().initStateCacheLoad(); // 初始化缓存加载状态

                            ((MainActivity) context).preMainLoadCahceProcess(); // 预加载缓存处理

                            HashMap<String, String> curMap = map.get(CacheUtil.DefaultSaveName); // 获取默认缓存数据
                            HashMap<String, String> dstCurMap = CacheUtil.get().getCurrMap(); // 获取当前缓存
//                            HashMap<String, String> dstOtherMap = CacheUtil.get().getCurrOtherMap();
                            dstCurMap.clear(); // 清空当前缓存
//                            dstOtherMap.clear();
                            if (curMap != null) { // 如果恢复数据不为空
                                dstCurMap.putAll(curMap); // 将恢复数据写入当前缓存
                            }
                            CacheUtil.get().clearTempSaveParam(dstCurMap); // 清除临时保存参数
                            CacheUtil.get().checkMSSStoreMap(); // 检查MSS存储映射

                            CacheUtil.get().putMapInForce(CacheUtil.MAIN_LEFT_RUNSTOP, String.valueOf(false)); // 强制设置运行/停止状态
                            ((MainActivity) context).updateMainLoadCaheProcess(false); // 更新主界面加载缓存
                            ((MainActivity) context).postMainLoadCacheProcess(); // 后加载缓存处理

                            int recoverySelect = CacheUtil.get().getInt(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1"); // 获取恢复通道选择
                            RxBus.getInstance().post(RxEnum.MQ_MSG_RECOVERY_SELECT, recoverySelect); // 通知通道选择恢复

                            Log.d("SaveRecoverySession", toastStr); // 打印提示日志
                            DToast.get().show(toastStr); // 显示提示
                            setLoadSession(false); // 清除加载标志
                        }
                    });
                }else{ // 如果文件不存在
                    setLoadSession(false); // 清除加载标志
                }
            }
        }).start(); // 启动恢复线程
    }


    /**
     * 显示加载进度
     * @param progress 进度百分比
     */
    private void showLoadProgress(int progress) { // 显示加载进度
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
     * 处理不支持的文件类型
     * @param scope 示波器实例
     * @param isRun 原始运行状态
     */
    private void loadFileNotSupport(Scope scope, boolean isRun) { // 处理不支持的文件
        requireActivity().runOnUiThread(() -> { // 切换到UI线程
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕被锁定
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
            }
            screenControls.setProgressValue(0); // 重置进度值
            dialogOnlyOk.setData(context.getResources().getString(R.string.file_not_support), null, null); // 设置不支持提示对话框
            setLoadSession(false); // 清除加载标志
            if (scope.isRun() == isRun) return; // 如果运行状态一致则返回
            if (isRun) { // 如果原始状态为运行
                Command.get().getFunctionMenu().Run(true); // 恢复运行
            } else { // 如果原始状态为停止
                Command.get().getFunctionMenu().Stop(true); // 恢复停止
            }
        });
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
        restoreSessionPath(); // 恢复会话路径
    }

    /**
     * 从缓存恢复会话路径
     */
    private void restoreSessionPath() { // 恢复会话路径
        sessionPathSet.clear(); // 清空路径集合
        String sessionCurrentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_PATH_CURRENT); // 获取当前路径缓存
        String sessionPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_PATH); // 获取显示路径列表缓存
        String abSessionPathListStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH); // 获取绝对路径列表缓存

        ArrayList<String> sessionPathList = StrUtil.getListFromString(sessionPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示路径列表
        ArrayList<String> abSessionPathList = StrUtil.getListFromString(abSessionPathListStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表

        if (!Tools.fileIsExists(sessionCurrentPath)) { // 如果当前路径不存在
            sessionCurrentPath = null; // 设为null
        }


        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建文件Bean列表
        FileBean currentBean = new FileBean(); // 创建当前路径Bean
        for (int i = 0; i < abSessionPathList.size(); i++) { // 遍历绝对路径列表
            if (!Tools.fileIsExists(abSessionPathList.get(i))) continue; // 跳过不存在的路径
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(abSessionPathList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(sessionPathList.get(i)); // 设置显示名称
            if(abSessionPathList.get(i).equals(sessionCurrentPath)) { // 如果是当前路径
                currentBean.setPath(abSessionPathList.get(i)); // 设置当前路径绝对路径
                currentBean.setDisplayName(sessionPathList.get(i)); // 设置当前路径显示名称
            }
            dataList.add(fileBean); // 添加到列表
        }


        sessionPathSet.addAll(dataList); // 添加所有路径到集合
        sessionSpinner.updateDataList(getSessionFileList(), null); // 更新下拉选择器

        isFilesShowOnly = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_INVOKE_SESSION_FILE_FILTER); // 获取文件过滤缓存
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
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_INVOKE_SESSION_FILE_FILTER, String.valueOf(isChecked)); // 缓存过滤状态
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
