package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.content.Context; // 导入上下文类
import android.graphics.Color; // 导入颜色类
import android.graphics.PixelFormat; // 导入像素格式类
import android.graphics.drawable.Drawable; // 导入Drawable图形类
import android.os.Bundle; // 导入Bundle状态保存类
import android.view.Gravity; // 导入对齐方式类
import android.view.LayoutInflater; // 导入布局填充器类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.view.WindowManager; // 导入窗口管理器类
import android.widget.Button; // 导入按钮控件类
import android.widget.CheckBox; // 导入复选框控件类
import android.widget.CompoundButton; // 导入复合按钮基类
import android.widget.FrameLayout; // 导入帧布局类
import android.widget.TextView; // 导入文本视图控件类

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源引用类
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel; // 导入确认取消对话框类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxBus事件总线类
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举类
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理类
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
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框类
import com.micsig.tbook.ui.top.view.TopViewSpinner; // 导入自定义下拉框类
import com.micsig.tbook.ui.util.FileBeanToStr; // 导入文件Bean转字符串工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.molihuan.pathselector.PathSelector; // 导入路径选择器类
import com.molihuan.pathselector.dao.SelectConfigData; // 导入选择配置数据类
import com.molihuan.pathselector.entity.FileBean; // 导入文件Bean实体类
import com.molihuan.pathselector.entity.FontBean; // 导入字体Bean实体类
import com.molihuan.pathselector.fragment.BasePathSelectFragment; // 导入基础路径选择Fragment类
import com.molihuan.pathselector.listener.CommonItemListener; // 导入通用项监听器类
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl; // 导入配置数据构建器实现类
import com.molihuan.pathselector.utils.DToastDialog; // 导入路径选择器Toast对话框类
import com.molihuan.pathselector.utils.MConstants; // 导入路径选择器常量类

import java.io.File; // 导入文件类
import java.util.ArrayList; // 导入动态数组类
import java.util.List; // 导入列表接口类

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：保存/调用功能 → 顶部布局 → 保存子页面 → 设置保存（Setting Save）    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供设置（Setting）保存功能的UI界面和逻辑控制，包括保存路径的选择     │
 * │          与管理，文件名的编辑与序号递增，通过SaveManage将当前配置保存为         │
 * │          .SaveRecovery文件                                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：继承Fragment，通过TopViewSpinner管理保存路径列表，                  │
 * │          通过TopViewEdit编辑文件名，通过SaveManage执行设置保存，              │
 * │          通过CacheUtil管理缓存状态                                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：CacheUtil(缓存恢复) → UI控件(初始化状态) →                        │
 * │          用户点击保存 → saveSetting(检查文件) → doSaveSetting(执行保存) →     │
 * │          SaveManage.saveUserSetToPath(保存配置) → 回调(成功/失败提示) →      │
 * │          autoAddSuffixNum(序号递增)                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖组件：TopViewSpinner, TopViewEdit, FileSelector, CacheUtil,             │
 * │          SaveManage, RxBus, TopDialogTextKeyBoard,                         │
 * │          TopDialogNumberKeyBoard, DialogOkCancel, FileUtils                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在保存菜单选择"设置"Tab时显示此页面，                            │
 * │          配置保存路径和文件名后点击保存按钮执行设置保存操作                      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class TopLayoutSaveSetting extends Fragment {
    /** Fragment所在的上下文环境 */
    private Context context; // Activity上下文


    /** 保存路径下拉框控件 */
    private TopViewSpinner spinner; // 路径选择下拉框
    /** 文件名编辑框控件 */
    private TopViewEdit saveNameEdit; // 保存名称编辑框
    /** 文件名添加序号复选框 */
    private CheckBox fileNameAdd; // 文件名添加序号复选框
    /** 序号显示文本控件 */
    private TextView txtSuffixNum; // 序号文本显示
    /** 保存按钮和浏览按钮 */
    private Button btnSave, btnBrowse; // 保存按钮和浏览按钮
    /** 设置保存路径集合，固定大小为10，用于MRU路径列表 */
    private final FixedSizeHashSet<FileBean> settingPathSet = new FixedSizeHashSet<>(10); // 固定大小路径集合
    /** 文本键盘对话框和文件选择器文本键盘对话框 */
    private TopDialogTextKeyBoard layoutTextKeyBoard,fileSelectorTextKeyBoard; // 文本键盘对话框引用
    /** Toast提示字符串 */
    private String toastStr = ""; // Toast提示文本
    /** 数字键盘对话框 */
    protected TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框引用

    /** 窗口管理器，用于动态添加/移除键盘视图 */
    private WindowManager windowManager; // 窗口管理器

    /** 路径选择器Toast对话框 */
    public DToastDialog dToastdialog = new DToastDialog(); // 路径选择器Toast
    /** 确认取消对话框 */
    private DialogOkCancel dialogOk; // 确认取消对话框引用

    /** 文件选择器，用于浏览和选择保存路径 */
    private FileSelector fileSelector ; // 文件选择器实例

    /**
     * 创建Fragment的视图
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的设置保存布局视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_save_setting, container, false); // 填充设置保存布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity(); // 获取所在Activity作为上下文
        initView(view); // 初始化视图控件
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化事件控制，订阅RxBus缓存加载事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件

    }

    /**
     * 初始化所有视图控件，绑定监听器
     * @param view 根视图
     */
    private void initView(View view) {
        spinner = view.findViewById(R.id.topSpinner); // 获取路径下拉框控件
        saveNameEdit = view.findViewById(R.id.saveName); // 获取文件名编辑框控件
        fileNameAdd = view.findViewById(R.id.check_file_name_add); // 获取序号复选框控件
        Drawable drawable = context.getResources().getDrawable(R.drawable.btn_select_channel_all); // 获取全选图标Drawable
        fileNameAdd.setBackground(null); // 清除复选框背景
        fileNameAdd.setButtonDrawable(null); // 清除复选框默认按钮图标
        drawable.setBounds(0, 0, 22, 22); // 设置图标边界大小
        fileNameAdd.setCompoundDrawables(drawable, null, null, null); // 将图标设置到左侧
        txtSuffixNum = view.findViewById(R.id.txt_index_num); // 获取序号文本控件
        btnSave = view.findViewById(R.id.btn_save); // 获取保存按钮
        btnBrowse = view.findViewById(R.id.btn_browse); // 获取浏览按钮

        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), // 设置下拉框标题
                getSettingPathList(), R.layout.layout_item_for_save_directory, onItemSelectListener); // 设置路径数据和监听器
        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 获取文本键盘对话框

        btnBrowse.setOnClickListener(onClickListener); // 设置浏览按钮点击监听器
        btnSave.setOnClickListener(onClickListener); // 设置保存按钮点击监听器
        txtSuffixNum.setOnClickListener(onClickListener); // 设置序号文本点击监听器
        saveNameEdit.setOnClickEditListener(onClickEditListener); // 设置文件名编辑框点击监听器
        fileNameAdd.setOnCheckedChangeListener(onCheckBoxChangedListener); // 设置复选框选中变化监听器

        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘对话框
        dialogOk = (DialogOkCancel) ((MainActivity) context).getMainViewGroup().getDialog(MainViewGroup.DIALOG_OKCANCEL); // 获取确认取消对话框
        fileSelector = new FileSelector(context,(selectedPath) -> { // 创建文件选择器
            addSelectToPathSet(selectedPath); // 选中路径后添加到路径集合
        });
    }


    /**
     * 获取设置保存路径列表（逆序，最近使用的在前）
     * @return 路径列表
     */
    private ArrayList<FileBean> getSettingPathList() {
        return settingPathSet.getReverseList(); // 返回逆序路径列表
    }

    /**
     * 将选中的路径添加到路径集合，并更新UI和缓存
     * @param pathStr 选中的文件路径Bean
     */
    private void addSelectToPathSet(FileBean pathStr) {
        handleAddPath(pathStr); // 处理路径添加（去重）
        settingPathSet.add(pathStr); // 添加到路径集合
        spinner.updateDataList(getSettingPathList(), null); // 更新下拉框数据
        saveSettingPathToCache(); // 保存路径到缓存
    }

    /**
     * 处理路径添加逻辑，如果路径已存在则先移除旧的再添加新的（实现MRU效果）
     * @param pathStr 要添加的文件路径Bean
     * @return true表示路径是新添加的，false表示路径已存在（已移除旧的）
     */
    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true; // 是否可以添加标志
        FileBean temp = null; // 临时保存已存在的路径Bean
        for (FileBean fileBean : settingPathSet) { // 遍历路径集合
            if (fileBean.getPath().equals(pathStr.getPath())) { // 如果路径相同
                canAdd = false; // 标记不可添加
                temp = fileBean; // 保存已存在的Bean
                break; // 跳出循环
            }
        }
        if (temp != null) { // 如果找到已存在的路径
            settingPathSet.remove(temp); // 移除旧的路径
        }
        return canAdd; // 返回是否为新路径
    }

    /**
     * 将设置保存路径信息保存到缓存
     */
    public void saveSettingPathToCache() {
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH, // 保存显示名称路径列表
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 转换为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH, // 保存绝对路径列表
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(settingPathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 转换为字符串保存

        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT, spinner.getSelectItem()); // 保存当前选中路径
    }

    /**
     * 设置详情消息发送监听器（此页面未使用）
     * @param onDetailSendMsgListener 消息发送监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) {

    }

    /**
     * 获取保存详情（此页面未实现，返回null）
     * @return null
     */
    public ISaveDetail getSaveDetail() {
        return null; // 返回null
    }

    /** 路径下拉框选中监听器，选中路径后更新缓存和路径集合 */
    TopViewSpinner.onItemSelectListener onItemSelectListener = str -> { // Lambda表达式
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT, str.getPath()); // 保存当前选中路径到缓存
//        DToast.get().show(str);
        addSelectToPathSet(str); // 添加到路径集合并更新UI
    };


    /** 缓存加载事件的RxJava消费者，恢复缓存状态 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 创建消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收到缓存加载事件
            setCache(); // 恢复缓存状态
        }
    };


    /**
     * 从缓存恢复所有UI控件状态
     */
    private void setCache() {
        settingPathSet.clear(); // 清空路径集合
        String pathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH); // 从缓存读取显示名称路径字符串
        String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH); // 从缓存读取绝对路径字符串
        String currentPath = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_PATH_CURRENT); // 从缓存读取当前选中路径
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示名称路径列表
        ArrayList<String> adPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表

        ArrayList<FileBean> dataList = new ArrayList<>(); // 创建文件Bean列表
        FileBean currentBean = new FileBean(); // 创建当前选中路径Bean
        for (int i = 0; i < adPathCacheList.size(); i++) { // 遍历绝对路径列表
            FileBean fileBean = new FileBean(); // 创建文件Bean
            fileBean.setPath(adPathCacheList.get(i)); // 设置绝对路径
            fileBean.setDisplayName(pathCacheList.get(i)); // 设置显示名称
            if(adPathCacheList.get(i).equals(currentPath)) { // 如果是当前选中路径
                currentBean.setPath(adPathCacheList.get(i)); // 设置当前Bean的绝对路径
                currentBean.setDisplayName(pathCacheList.get(i)); // 设置当前Bean的显示名称
            }
            dataList.add(fileBean); // 添加到列表
        }

        settingPathSet.addAll(dataList); // 将所有路径添加到集合
        spinner.updateDataList(getSettingPathList(), null); // 更新下拉框数据

        String settingName = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_NAME); // 从缓存读取设置名称
        if (settingName.isEmpty()) { // 如果名称为空
            settingName = Tools.generateName(); // 自动生成名称
        }
        saveNameEdit.setText(settingName); // 设置文件名编辑框文本


        boolean isFileNumAddCheck = CacheUtil.get().getOtherBoolean(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK); // 从缓存读取序号复选框状态
        String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM); // 从缓存读取序号值
        txtSuffixNum.setEnabled(isFileNumAddCheck); // 设置序号文本是否可用
        txtSuffixNum.setText(suffixNum); // 设置序号文本值
        if (fileNameAdd.isChecked() != isFileNumAddCheck) { // 如果复选框状态与缓存不一致
            fileNameAdd.setChecked(isFileNumAddCheck); // 设置复选框状态（会触发监听器）
        } else { // 如果状态一致
            onCheckBoxChangedListener.onCheckedChanged(fileNameAdd, isFileNumAddCheck); // 手动触发监听器
        }

    }

    /** 按钮点击事件监听器，处理浏览、保存、序号输入 */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 创建点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == btnBrowse.getId()) { // 如果点击浏览按钮
                handleBrowseClick(); // 处理浏览点击
            } else if (v.getId() == btnSave.getId()) { // 如果点击保存按钮
                saveSetting(); // 执行保存设置
            } else if (v.getId() == txtSuffixNum.getId()) { // 如果点击序号文本
                dialogKeyBoard.setDecimalData(3, IDigits.DIGITS_10, onNumSubFixListener); // 弹出数字键盘输入序号
            }
        }
    };

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
        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM, text); // 保存序号到缓存

    }


    /** 文件名编辑框点击监听器，弹出文本键盘编辑文件名 */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() { // 创建监听器
        @Override
        public void onClickEdit(TopViewEdit v, String text) { // 点击编辑回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            if (v.getId() == saveNameEdit.getId()) { // 如果是文件名编辑框
                String suffixNum = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM); // 从缓存读取序号
                String suffix = "_" + suffixNum; // 构建序号后缀
                if (text.endsWith(suffix)) { // 如果文件名以序号后缀结尾
                    text = text.substring(0, text.length() - suffix.length()); // 去除序号后缀
                }
                layoutTextKeyBoard.setData(text, TopDialogTextKeyBoard.HANDLE_TYPE_SAVE_SESSION, TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() { // 设置文本键盘数据
                    @Override
                    public void onDismiss(String result) { // 键盘关闭回调
                        saveNameEdit.setText(result); // 设置编辑框文本
                        txtSuffixNum.setText("000"); // 重置序号为000
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_NAME, result); // 保存文件名到缓存
                        CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM, "000"); // 保存序号到缓存
                    }
                });
            }
        }
    };


    /** 序号复选框选中变化监听器 */
    private CompoundButton.OnCheckedChangeListener onCheckBoxChangedListener = new CompoundButton.OnCheckedChangeListener() { // 创建监听器
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 选中状态变化回调
            if (buttonView.getId() == fileNameAdd.getId()) { // 如果是序号复选框
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK, String.valueOf(isChecked)); // 保存复选框状态到缓存
                txtSuffixNum.setEnabled(isChecked); // 设置序号文本是否可用
                RxBus.getInstance().post(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE, CacheUtil.SAVE_TYPE_SETTING + CacheUtil.WAVE_STORE_PATH_SLIP + isChecked); // 发送序号更新事件
            }
        }
    };

    /**
     * 处理浏览按钮点击，打开文件选择器浏览保存路径
     */
    private void handleBrowseClick() {
        String spinnerSelectedPath = spinner.getSelectItem(); // 获取当前选中的路径
        String disPlay = spinner.getDisPlaySelectItem(); // 获取当前选中路径的显示名称
        File file = new File(spinnerSelectedPath); // 创建文件对象

        if(!file.exists() || !file.isDirectory()){ // 如果路径不存在或不是目录
            spinnerSelectedPath = "/storage/emulated/0"; // 使用默认内部存储路径
            disPlay = context.getResources().getString(R.string.internal_storage); // 使用内部存储显示名称
        }

        fileSelector.buildSaveFileSelector(spinnerSelectedPath, disPlay, this, context); // 构建并显示文件选择器
    }


    /**
     * 执行保存设置操作，检查文件是否存在后决定是否覆盖
     */
    private void saveSetting() {
        if (StrUtil.isEmpty(spinner.getSelectItem())) { // 如果选中路径为空
            DToast.get().show(R.string.top_slip_directory_save_to); // 提示选择保存目录
            return; // 返回
        }
        final String filePath = spinner.getSelectItem() + File.separator + getFinaleName() + ".SaveRecovery"; // 构建完整文件路径
        if (SaveManage.getInstance().checkFileExists(filePath)) { // 如果文件已存在
//            DToast.get().show(String.format(getString(R.string.msgTopSaveNameExisted), saveNameEdit.getText() + ".SaveRecovery"));
//            autoAddSuffixNum();
            dialogOk.setData(btnSave, R.string.top_slip_save_file_exists, filePath, null, onOkCancelClickListener); // 弹出覆盖确认对话框
        } else { // 如果文件不存在
            doSaveSetting(filePath); // 直接执行保存
        }
    }

    /**
     * 获取最终文件名（含序号后缀）
     * @return 完整文件名
     */
    private String getFinaleName() {
        String finalName = saveNameEdit.getText(); // 获取编辑框中的文件名
        if(fileNameAdd.isChecked()) { // 如果勾选了添加序号
            finalName = finalName + "_" + txtSuffixNum.getText(); // 拼接序号后缀
        }
        return finalName; // 返回最终文件名
    }


    /**
     * 实际执行设置保存操作
     * @param filePath 保存文件路径
     */
    private void doSaveSetting(String filePath) {
        if (!FileUtils.checkFolderExists(spinner.getSelectItem(),context.getResources().getString(R.string.internal_storage))) { // 检查保存目录是否存在
            DToast.get().show(R.string.top_slip_save_wave_path_unable); // 提示路径不可用
            return; // 返回
        }

        try { // 尝试保存
            int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 获取当前通道选择
            CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(channelSelect)); // 保存通道选择到缓存
            SaveManage.getInstance().saveUserSetToPath(filePath, CacheUtil.get().getCacheMap(), new SaveManage.SaveCallBack() { // 执行设置保存
                @Override
                public void onResult(boolean success, String msg) { // 保存结果回调
                    DToast.get().show(msg); // 显示保存结果提示
                    Logger.i("SaveSetting isSuccess= " + success); // 打印日志
                    if (success) { // 如果保存成功
                        autoAddSuffixNum(); // 自动递增序号
                    }
                }
            });
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * 文件名序号自动递增
     */
    private void autoAddSuffixNum() {//文件名序号递增
        if (!fileNameAdd.isChecked()) return; // 如果未勾选添加序号则返回
        int oldSuffixNum = Integer.parseInt(txtSuffixNum.getText().toString().trim()); // 解析当前序号
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 3); // 序号加1并格式化为3位
        if (onNumSubFixListener != null) { // 如果监听器不为空
            onNumSubFixListener.onDismiss(tempNum); // 触发监听器更新序号
        }
    }



    /**
     * 线程休眠指定毫秒数
     * @param ms 休眠毫秒数
     */
    private void ms_sleep(long ms) {
        try { // 尝试休眠
            Thread.sleep(ms); // 线程休眠
        } catch (InterruptedException e) { // 捕获中断异常
            throw new RuntimeException(e); // 抛出运行时异常
        }
    }


    /** 确认取消对话框的点击监听器，处理文件覆盖确认 */
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() { // 创建监听器
        @Override
        public void onOkClick(View v, Object data) { // 确认按钮点击回调
            if (v == null || data == null) return; // 参数为空则返回
            doSaveSetting((String) data); // 执行设置保存
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
